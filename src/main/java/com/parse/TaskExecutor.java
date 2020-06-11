package com.parse;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.google.googlejavaformat.java.JavaFormatterOptions.Style;
import com.parse.constants.Keywords;
import com.parse.models.PredicateInfo;
import com.parse.utils.IndentSpaceParser;
import com.parse.utils.JavaFormatter;
import com.parse.utils.PredicateParser;
import com.parse.utils.PredicateRecorder;

/**
 * The controller TaskExecutor. It holds the control of the application.
 */
public class TaskExecutor {

	private static Formatter gooleFormatter = new Formatter(JavaFormatterOptions.builder().style(Style.GOOGLE).build());

	private static JavaFormatter formatter = new JavaFormatter();

	/**
	 * The list of predicate information
	 */
	private static List<PredicateInfo> predicateInfoList;

	/**
	 * Removes comment from the line of code
	 * 
	 * @param line The line
	 * @return The stripped line
	 */
	private static String removeSingleLineComment(String line) {

		char[] chars = line.toCharArray();
		int counter = 0;
		int totalChars = chars.length;
		StringBuilder codeBuilder = new StringBuilder();
		while (counter < totalChars) {
			if (chars[counter] == '"') {
				codeBuilder.append(chars[counter++]);
				while (counter < totalChars) {
					codeBuilder.append(chars[counter]);
					if (chars[counter] == '"' && counter - 1 >= 0 && chars[counter - 1] != '\\') {
						break;
					}
					counter++;
				}
			} else if (chars[counter] == '/' && counter + 1 < totalChars && chars[counter + 1] == '/') {
				break;
			} else {
				codeBuilder.append(chars[counter]);
			}
			counter++;
		}

		return codeBuilder.toString();
	}

	/**
	 * Removes multi-line comments from the code
	 */
	private static String removeMultilineComment(String code) {

		char[] chars = code.toCharArray();
		StringBuilder codeBuilder = new StringBuilder();
		int counter = 0;
		int totalChars = chars.length;
		while (counter < totalChars) {
			if (chars[counter] == '"') {
				codeBuilder.append(chars[counter++]);
				while (counter < totalChars) {
					codeBuilder.append(chars[counter]);
					if (chars[counter] == '"' && counter - 1 >= 0 && chars[counter - 1] != '\\') {
						break;
					}
					counter++;
				}
			} else if (chars[counter] == '/' && counter + 1 < totalChars && chars[counter + 1] == '*') {
				while (counter < totalChars) {
					if (chars[counter] == '"') {
						while (counter < totalChars) {
							if (chars[counter] == '"' && counter - 1 >= 0 && chars[counter - 1] != '\\') {
								break;
							}
							counter++;
						}
					} else if (chars[counter] == '*' && counter + 1 < totalChars && chars[counter + 1] == '/') {
						counter++;
						break;
					}
					counter++;
				}
			} else {
				codeBuilder.append(chars[counter]);
			}
			counter++;
		}
		return codeBuilder.toString();
	}

	/**
	 * Processes the if statement
	 * 
	 * @param lines
	 * @param updatedLines
	 * @param startPos
	 * @param totalLines
	 * @return
	 */
	private static int processIf(List<String> lines, List<String> updatedLines, int startPos, int totalLines,
			Integer pos) {

		// Getting the current indentation of for statement
		String spaces = IndentSpaceParser.getIndentSpaces(lines.get(startPos));
		int indentedSpaceCount = IndentSpaceParser.getIndentSpacesCount(lines.get(startPos));

		// The statement might be present in multiple lines, thus merging all
		StringBuilder statementBuilder = new StringBuilder();
		statementBuilder.append(removeSingleLineComment(lines.get(startPos)));
		startPos++;

		if (startPos < totalLines) {
			String codeline = lines.get(startPos);
			while (startPos < totalLines && (StringUtils.isBlank(codeline)
					|| (!codeline.trim().startsWith("}") && (codeline.trim().startsWith("//")
							|| codeline.trim().startsWith("/*") || codeline.trim().startsWith("*")
							|| IndentSpaceParser.getIndentSpacesCount(codeline) != indentedSpaceCount + 4)))) {
				statementBuilder.append(removeSingleLineComment(codeline));
				startPos++;
				codeline = lines.get(startPos);
			}
		}

		String statement = removeMultilineComment(statementBuilder.toString());
		PredicateInfo predicateInfo = PredicateParser.processIfStatement(statement);

		if (predicateInfo != null) {
			predicateInfoList.add(predicateInfo);
			updatedLines.add(predicateInfo.getPredicateInitStatement());
			updatedLines.add(predicateInfo.getConvertedStatement());
		} else {
			updatedLines.add(spaces + statement);
			if (!statement.trim().endsWith("{")) {
				updatedLines.add("{");
			}
		}
		List<String> innerBodyLines = new ArrayList<>();
		int bodyLineCounter = startPos;
		while (bodyLineCounter < totalLines) {
			String line = lines.get(bodyLineCounter);
			if (StringUtils.isNotBlank(line.trim())) {
				if (line.startsWith("//") || IndentSpaceParser.getIndentSpacesCount(line) > indentedSpaceCount) {
					innerBodyLines.add(line);
				} else {
					break;
				}
			}
			bodyLineCounter++;
		}

		updatedLines.addAll(process(innerBodyLines));

		if (bodyLineCounter < totalLines
				&& IndentSpaceParser.getIndentSpacesCount(lines.get(bodyLineCounter)) == indentedSpaceCount) {
			String line = lines.get(bodyLineCounter).trim();
			if (line.matches("}( //.*)?")) {
				updatedLines.add("}");
				return bodyLineCounter;
			} else {
				bodyLineCounter--;
				updatedLines.add("}");
			}
		} else {
			bodyLineCounter--;
			updatedLines.add(spaces + "}");
		}

		return bodyLineCounter;
	}

	/**
	 * Processes the else-if statements
	 * 
	 * @param lines
	 * @param updatedLines
	 * @param totalLines
	 * @param pos
	 * @param bodyLineCounter
	 * @return
	 */
	private static int processElseIf(List<String> lines, List<String> updatedLines, int totalLines, Integer pos,
			int bodyLineCounter) {

		String line = lines.get(bodyLineCounter + 1).trim();
		while (bodyLineCounter + 1 < totalLines
				&& (line.startsWith(Keywords.ELSE_IF_I) || line.startsWith(Keywords.ELSE_IF_II))) {

			bodyLineCounter++;

			// Getting the current indentation of for statement
			String spaces = IndentSpaceParser.getIndentSpaces(lines.get(bodyLineCounter));
			int indentedSpaceCount = IndentSpaceParser.getIndentSpacesCount(lines.get(bodyLineCounter));

			// The statement might be present in multiple lines, thus merging all
			StringBuilder statementBuilder = new StringBuilder();
			statementBuilder.append(removeSingleLineComment(lines.get(bodyLineCounter)));
			bodyLineCounter++;

			if (bodyLineCounter < totalLines) {
				String codeline = lines.get(bodyLineCounter);
				while (bodyLineCounter < totalLines && (StringUtils.isEmpty(codeline)
						|| (!codeline.trim().startsWith("}") && (codeline.trim().startsWith("//")
								|| StringUtils.isEmpty(codeline.trim()) || codeline.trim().startsWith("/*")
								|| codeline.trim().startsWith("*")
								|| IndentSpaceParser.getIndentSpacesCount(codeline) != indentedSpaceCount + 4)))) {
					statementBuilder.append(removeSingleLineComment(codeline));
					bodyLineCounter++;
					codeline = lines.get(bodyLineCounter);
				}
			}

			String statement = removeMultilineComment(statementBuilder.toString());
			PredicateInfo predicateInfo = PredicateParser.processElseIfStatement(statement);

			if (predicateInfo != null) {
				predicateInfoList.add(predicateInfo);
				updatedLines.add(pos++, predicateInfo.getPredicateInitStatement());
				updatedLines.add(predicateInfo.getConvertedStatement());
			} else {
				updatedLines.add(spaces + statement);
				if (!statement.trim().endsWith("{")) {
					updatedLines.add("{");
				}
			}

			List<String> innerBodyLines = new ArrayList<>();
			while (bodyLineCounter < totalLines) {
				line = lines.get(bodyLineCounter);
				if (StringUtils.isNotBlank(line.trim())) {
					if (line.startsWith("//") || IndentSpaceParser.getIndentSpacesCount(line) > indentedSpaceCount) {
						innerBodyLines.add(line);
					} else {
						break;
					}
				}
				bodyLineCounter++;
			}

			updatedLines.addAll(process(innerBodyLines));

			if (bodyLineCounter < totalLines
					&& IndentSpaceParser.getIndentSpacesCount(lines.get(bodyLineCounter)) == indentedSpaceCount) {
				line = lines.get(bodyLineCounter).trim();
				if (line.matches("}( //.*)?")) {
					updatedLines.add("}");
					return bodyLineCounter;
				} else {
					bodyLineCounter--;
					updatedLines.add("}");
				}
			} else {
				bodyLineCounter--;
				updatedLines.add(spaces + "}");
			}
		}

		return bodyLineCounter;
	}

	/**
	 * Processes else statement
	 * 
	 * @param lines
	 * @param updatedLines
	 * @param startPos
	 * @param totalLines
	 * @param pos
	 * @param bodyLineCounter
	 * @return
	 */
	private static int processElse(List<String> lines, List<String> updatedLines, int totalLines, Integer pos,
			int bodyLineCounter) {

		String line = lines.get(bodyLineCounter + 1).trim();
		if (bodyLineCounter + 1 < totalLines
				&& (line.startsWith(Keywords.ELSE_I) || line.startsWith(Keywords.ELSE_II))) {

			bodyLineCounter++;
			String spaces = IndentSpaceParser.getIndentSpaces(lines.get(bodyLineCounter));
			int indentedSpaceCount = IndentSpaceParser.getIndentSpacesCount(lines.get(bodyLineCounter));
			updatedLines.add("else {");
			bodyLineCounter++;

			List<String> innerBodyLines = new ArrayList<>();

			while (bodyLineCounter < totalLines) {
				line = lines.get(bodyLineCounter);
				if (StringUtils.isNotBlank(line.trim())) {
					if (line.startsWith("//") || IndentSpaceParser.getIndentSpacesCount(line) > indentedSpaceCount) {
						innerBodyLines.add(line);
					} else {
						break;
					}
				}
				bodyLineCounter++;
			}

			updatedLines.addAll(process(innerBodyLines));

			if (bodyLineCounter < totalLines
					&& IndentSpaceParser.getIndentSpacesCount(lines.get(bodyLineCounter)) == indentedSpaceCount) {
				line = lines.get(bodyLineCounter).trim();
				if (line.matches("}( //.*)?")) {
					updatedLines.add("}");
					return bodyLineCounter;
				} else {
					bodyLineCounter--;
					updatedLines.add("}");
				}
			} else {
				bodyLineCounter--;
				updatedLines.add(spaces + "}");
			}
		}

		return bodyLineCounter;
	}

	/**
	 * Processes the if-elseif-else statements
	 * 
	 * @param lines        The actual lines
	 * @param updatedLines The updated lines
	 * @param startPos     The if-else start position
	 * @param totalLines   Total lines in its parent snippet
	 * @return The end position of if-else statements
	 */
	private static int processIfElseifElse(List<String> lines, List<String> updatedLines, int startPos,
			int totalLines) {

		Integer pos = updatedLines.size();
		int bodyLineCounter = processIf(lines, updatedLines, startPos, totalLines, pos);

		// Parsing the else-if statements, if present
		if (bodyLineCounter + 1 < totalLines) {
			bodyLineCounter = processElseIf(lines, updatedLines, totalLines, pos, bodyLineCounter);
		}

		// Parsing the else condition
		if (bodyLineCounter + 1 < totalLines) {
			bodyLineCounter = processElse(lines, updatedLines, totalLines, pos, bodyLineCounter);
		}

		return bodyLineCounter;
	}

	/**
	 * Processes the for loop
	 * 
	 * @param lines        The actual lines
	 * @param updatedLines The updated lines
	 * @param startPos     The for loop start position
	 * @param totalLines   Total lines in its parent snippet
	 * @return The end position of for loop
	 */
	private static int processForLoop(List<String> lines, List<String> updatedLines, int startPos, int totalLines) {

		// Getting the current indentation of for statement
		String spaces = IndentSpaceParser.getIndentSpaces(lines.get(startPos));
		int indentedSpaceCount = IndentSpaceParser.getIndentSpacesCount(lines.get(startPos));

		// The statement might be present in multiple lines, thus merging all
		StringBuilder statementBuilder = new StringBuilder();
		statementBuilder.append(removeSingleLineComment(lines.get(startPos)));
		startPos++;

		if (startPos < totalLines) {
			String codeline = lines.get(startPos);
			while (startPos < totalLines && (StringUtils.isBlank(codeline)
					|| (!codeline.trim().startsWith("}") && (codeline.trim().startsWith("//")
							|| codeline.trim().startsWith("/*") || codeline.trim().startsWith("*")
							|| IndentSpaceParser.getIndentSpacesCount(codeline) != indentedSpaceCount + 4)))) {
				statementBuilder.append(removeSingleLineComment(codeline));
				startPos++;
				codeline = lines.get(startPos);
			}
		}

		String statement = removeMultilineComment(statementBuilder.toString());
		PredicateInfo predicateInfo = PredicateParser.processForStatement(statement);

		if (predicateInfo != null) {
			predicateInfoList.add(predicateInfo);
			updatedLines.add(predicateInfo.getPredicateInitStatement());
			updatedLines.add(predicateInfo.getConvertedStatement());
		} else {
			updatedLines.add(spaces + statement);
			if (!statement.trim().endsWith("{")) {
				updatedLines.add("{");
			}
		}

		List<String> innerBodyLines = new ArrayList<>();
		int bodyLineCounter = startPos;
		while (bodyLineCounter < totalLines) {
			String line = lines.get(bodyLineCounter);
			if (StringUtils.isNotBlank(line.trim())) {
				if (line.startsWith("//") || IndentSpaceParser.getIndentSpacesCount(line) > indentedSpaceCount) {
					innerBodyLines.add(line);
				} else {
					break;
				}
			}
			bodyLineCounter++;
		}

		updatedLines.addAll(process(innerBodyLines));

		if (bodyLineCounter < totalLines
				&& IndentSpaceParser.getIndentSpacesCount(lines.get(bodyLineCounter)) == indentedSpaceCount
				&& lines.get(bodyLineCounter).trim().matches("}( //.*)?")) {
			updatedLines.add(lines.get(bodyLineCounter));
		} else {
			bodyLineCounter--;
			updatedLines.add("}");
		}
		return bodyLineCounter;
	}

	/**
	 * Processes the while loop
	 * 
	 * @param lines        The actual lines
	 * @param updatedLines The updated lines
	 * @param startPos     The while loop start position
	 * @param totalLines   Total lines in its parent snippet
	 * @return The end position of while loop
	 */
	private static int processWhileLoop(List<String> lines, List<String> updatedLines, int startPos, int totalLines) {

		// Getting the current indentation of for statement
		String spaces = IndentSpaceParser.getIndentSpaces(lines.get(startPos));
		int indentedSpaceCount = IndentSpaceParser.getIndentSpacesCount(lines.get(startPos));

		// The statement might be present in multiple lines, thus merging all
		StringBuilder statementBuilder = new StringBuilder();
		statementBuilder.append(removeSingleLineComment(lines.get(startPos)));
		startPos++;

		if (startPos < totalLines) {
			String codeline = lines.get(startPos);
			while (startPos < totalLines && (StringUtils.isBlank(codeline)
					|| (!codeline.trim().startsWith("}") && (codeline.trim().startsWith("//")
							|| codeline.trim().startsWith("/*") || codeline.trim().startsWith("*")
							|| IndentSpaceParser.getIndentSpacesCount(codeline) != indentedSpaceCount + 4)))) {
				statementBuilder.append(removeSingleLineComment(codeline));
				startPos++;
				codeline = lines.get(startPos);
			}
		}

		String statement = removeMultilineComment(statementBuilder.toString());
		PredicateInfo predicateInfo = PredicateParser.processWhileStatement(statement);

		if (predicateInfo != null) {
			predicateInfoList.add(predicateInfo);
			updatedLines.add(predicateInfo.getPredicateInitStatement());
			updatedLines.add(predicateInfo.getConvertedStatement());
		} else {
			updatedLines.add(spaces + statement);
			if (!statement.trim().endsWith("{")) {
				updatedLines.add("{");
			}
		}

		List<String> innerBodyLines = new ArrayList<>();
		int bodyLineCounter = startPos;
		while (bodyLineCounter < totalLines) {
			String line = lines.get(bodyLineCounter);
			if (StringUtils.isNotBlank(line.trim())) {
				if (line.startsWith("//") || IndentSpaceParser.getIndentSpacesCount(line) > indentedSpaceCount) {
					innerBodyLines.add(line);
				} else {
					break;
				}
			}
			bodyLineCounter++;
		}

		updatedLines.addAll(process(innerBodyLines));

		if (bodyLineCounter < totalLines
				&& IndentSpaceParser.getIndentSpacesCount(lines.get(bodyLineCounter)) == indentedSpaceCount
				&& lines.get(bodyLineCounter).trim().matches("}( //.*)?")) {
			updatedLines.add(lines.get(bodyLineCounter));
		} else {
			bodyLineCounter--;
			updatedLines.add(spaces + "}");
		}

		return bodyLineCounter;
	}

	/**
	 * Processes the do-while loop
	 * 
	 * @param lines        The actual lines
	 * @param updatedLines The updated lines
	 * @param startPos     The do-while loop start position
	 * @param totalLines   Total lines in its parent snippet
	 * @return The end position of do-while loop
	 */
	private static int processDoWhileLoop(List<String> lines, List<String> updatedLines, int startPos, int totalLines) {

		int pos = updatedLines.size();
		updatedLines.add(lines.get(startPos));
		int indentedSpaceCount = IndentSpaceParser.getIndentSpacesCount(lines.get(startPos));

		List<String> innerBodyLines = new ArrayList<>();
		int bodyLineCounter = startPos + 1;
		while (bodyLineCounter < totalLines) {
			String line = lines.get(bodyLineCounter);
			if (StringUtils.isNotBlank(line.trim())) {
				if (line.startsWith("//") || IndentSpaceParser.getIndentSpacesCount(line) > indentedSpaceCount) {
					innerBodyLines.add(line);
				} else {
					break;
				}
			}
			bodyLineCounter++;
		}

		// The statement might be present in multiple lines, thus merging all
		StringBuilder statementBuilder = new StringBuilder();
		statementBuilder.append(removeSingleLineComment(lines.get(bodyLineCounter)));
		bodyLineCounter++;

		if (bodyLineCounter < totalLines) {
			String codeline = lines.get(bodyLineCounter);
			while (bodyLineCounter < totalLines && (StringUtils.isBlank(codeline)
					|| (!codeline.trim().endsWith(";") && (codeline.trim().startsWith("//")
							|| codeline.trim().startsWith("/*") || codeline.trim().startsWith("*"))))) {
				statementBuilder.append(removeSingleLineComment(codeline));
				bodyLineCounter++;
				codeline = lines.get(bodyLineCounter);
			}
		}

		String statement = removeMultilineComment(statementBuilder.toString());
		PredicateInfo predicateInfo = PredicateParser.processDoWhileStatement(statement);

		if (predicateInfo != null) {
			predicateInfoList.add(predicateInfo);
			updatedLines.addAll(process(innerBodyLines));
			updatedLines.add(pos++, predicateInfo.getPredicateInitStatement());
			updatedLines.add(predicateInfo.getConvertedStatement());
		} else {
			return startPos;
		}

		return bodyLineCounter - 1;
	}

	/**
	 * Processes the lines of code
	 * 
	 * @param lines The lines
	 * @return The processed lines of code
	 */
	private static List<String> process(List<String> lines) {

		List<String> updatedLines = new ArrayList<>();
		int totalLines = lines.size();

		for (int i = 0; i < totalLines; i++) {
			if (lines.get(i).trim().startsWith(Keywords.FOR)) {
				i = processForLoop(lines, updatedLines, i, totalLines);
			} else if (lines.get(i).trim().startsWith(Keywords.WHILE)) {
				i = processWhileLoop(lines, updatedLines, i, totalLines);
			} else if (lines.get(i).trim().startsWith(Keywords.DO)) {
				i = processDoWhileLoop(lines, updatedLines, i, totalLines);
			} else if (lines.get(i).trim().startsWith(Keywords.IF)) {
				i = processIfElseifElse(lines, updatedLines, i, totalLines);
			} else {
				updatedLines.add(lines.get(i));
			}
		}
		return updatedLines;
	}

	/**
	 * Saves the updated code
	 * 
	 * @param code     The code
	 * @param filePath The file path
	 */
	private static void saveUpdatedCode(String code, Path filePath) {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
			writer.write(code);
			writer.flush();
		} catch (IOException ioException) {
			System.out.println("Error saving the updated code.");
		}
	}

	/**
	 * Processes the input file path
	 * 
	 * @param inputFilePath The input file path
	 * @param outputPath    The output path
	 */
	private static void processPath(Path inputFilePath, Path outputPath) {

		try {
			System.out.println("Processing " + inputFilePath.toString());
			String formattedJava = formatter.format(new String(Files.readAllBytes(inputFilePath)));
			predicateInfoList = new ArrayList<>();
			List<String> updatedLines = process(Arrays.asList(formattedJava.split("\n")));

			// Saving the updated code
			StringBuilder codeBuilder = new StringBuilder();
			for (String line : updatedLines) {
				codeBuilder.append(line);
				codeBuilder.append("\n");
			}
			String formattedUpdatedCode = gooleFormatter.formatSource(codeBuilder.toString());
			saveUpdatedCode(formattedUpdatedCode, inputFilePath);

			// Creating the predicates file
			PredicateRecorder.create(inputFilePath, outputPath, predicateInfoList);
			System.out.println("COMPLETED.");
		} catch (Exception exception) {
			System.out.println("Error formatting the code. File: " + inputFilePath.toString() + ", Reason: "
					+ exception.getLocalizedMessage());
		}
	}

	/**
	 * Execution starts from here
	 * 
	 * @param args The command line arguments
	 */
	public static void main(String[] args) {

		if (args.length != 2) {
			System.out.println("Invalid arguments!");
			System.exit(1);
		}

		Path outputPath = Paths.get(args[0]);
		if (!outputPath.toFile().exists()) {
			try {
				Files.createDirectories(outputPath);
			} catch (IOException ioException) {
				System.out.println("Error creating the output directory.");
			}
		}

		Path inputPath = Paths.get(args[1]);
		if (inputPath.toFile().isDirectory()) {
			try (Stream<Path> pathStream = Files.walk(inputPath, FileVisitOption.FOLLOW_LINKS)) {
				pathStream.filter(path -> path.toString().endsWith(".java"))
						.forEach(path -> processPath(path, outputPath));
			} catch (IOException e) {
				System.out.println("Error walking the directory tree");
			}
		} else {
			processPath(inputPath, outputPath);
		}
	}
}
