package com.parse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.google.googlejavaformat.java.JavaFormatterOptions.Style;
import com.parse.constants.Keywords;
import com.parse.models.PredicateInfo;
import com.parse.utils.CodeFormatter;
import com.parse.utils.IndentSpaceParser;
import com.parse.utils.PredicateParser;
import com.parse.utils.PredicateRecorder;

/**
 * The controller TaskExecutor. It holds the control of the application.
 */
public class TaskExecutor {

	private static Formatter formatter = new Formatter(JavaFormatterOptions.builder().style(Style.GOOGLE).build());

	/**
	 * The list of predicate information
	 */
	private static List<PredicateInfo> predicateInfoList;

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
		statementBuilder.append(removeComment(lines.get(startPos)));
		startPos++;

		while (IndentSpaceParser.getIndentSpacesCount(lines.get(startPos)) > indentedSpaceCount + 4) {
			statementBuilder.append(removeComment(lines.get(startPos)));
			startPos++;
		}

		PredicateInfo predicateInfo = PredicateParser.processForStatement(statementBuilder.toString());

		if (predicateInfo != null) {
			predicateInfoList.add(predicateInfo);
			updatedLines.add(spaces + predicateInfo.getVarInitializationStatement());
			updatedLines.add(spaces + predicateInfo.getInitializationStatement());
			updatedLines.add(spaces + predicateInfo.getParentStatement());
		} else {
			updatedLines.add(spaces + statementBuilder.toString());
			return startPos - 1;
		}
		List<String> innerBodyLines = new ArrayList<>();
		int bodyLineCounter = startPos;
		int parentLineNumber = startPos;
		while (bodyLineCounter < totalLines) {
			String line = lines.get(bodyLineCounter);
			if (StringUtils.isNotBlank(line.trim())) {
				if (IndentSpaceParser.getIndentSpacesCount(line) > indentedSpaceCount) {
					innerBodyLines.add(line);
				} else {
					break;
				}
			}
			bodyLineCounter++;
		}

		updatedLines.addAll(process(innerBodyLines, parentLineNumber));
		if (predicateInfo != null) {
			updatedLines.add(spaces + "\t" + predicateInfo.getVarChangeStatement());
			updatedLines.add(spaces + "\t" + predicateInfo.getReuseStatement());
		}

		if (bodyLineCounter < totalLines
				&& IndentSpaceParser.getIndentSpacesCount(lines.get(bodyLineCounter)) == indentedSpaceCount
				&& lines.get(bodyLineCounter).trim().equals("}")) {
			updatedLines.add(lines.get(bodyLineCounter));
		} else {
			bodyLineCounter--;
			updatedLines.add(spaces + "}");
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
		statementBuilder.append(removeComment(lines.get(startPos)));
		startPos++;

		while (IndentSpaceParser.getIndentSpacesCount(lines.get(startPos)) > indentedSpaceCount + 4) {
			statementBuilder.append(removeComment(lines.get(startPos)));
			startPos++;
		}

		PredicateInfo predicateInfo = PredicateParser.processWhileStatement(statementBuilder.toString());

		if (predicateInfo != null) {
			predicateInfoList.add(predicateInfo);
			updatedLines.add(spaces + predicateInfo.getInitializationStatement());
			updatedLines.add(spaces + predicateInfo.getParentStatement());
		}
		List<String> innerBodyLines = new ArrayList<>();
		int bodyLineCounter = startPos;
		int parentLineNumber = startPos;
		while (bodyLineCounter < totalLines) {
			String line = lines.get(bodyLineCounter);
			if (StringUtils.isNotBlank(line.trim())) {
				if (IndentSpaceParser.getIndentSpacesCount(line) > indentedSpaceCount) {
					innerBodyLines.add(line);
				} else {
					break;
				}
			}
			bodyLineCounter++;
		}

		updatedLines.addAll(process(innerBodyLines, parentLineNumber));
		if (predicateInfo != null) {
			updatedLines.add(spaces + "\t" + predicateInfo.getReuseStatement());
		}

		if (bodyLineCounter < totalLines
				&& IndentSpaceParser.getIndentSpacesCount(lines.get(bodyLineCounter)) == indentedSpaceCount
				&& lines.get(bodyLineCounter).trim().equals("}")) {
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
		String spaces = IndentSpaceParser.getIndentSpaces(lines.get(startPos));
		int indentedSpaceCount = IndentSpaceParser.getIndentSpacesCount(lines.get(startPos));
		updatedLines.add(lines.get(startPos));

		List<String> innerBodyLines = new ArrayList<>();
		int bodyLineCounter = startPos + 1;
		int parentLineNumber = startPos;
		while (bodyLineCounter < totalLines) {
			String line = lines.get(bodyLineCounter);
			if (StringUtils.isNotBlank(line.trim())) {
				if (IndentSpaceParser.getIndentSpacesCount(line) > indentedSpaceCount) {
					innerBodyLines.add(line);
				} else {
					break;
				}
			}
			bodyLineCounter++;
		}

		updatedLines.addAll(process(innerBodyLines, parentLineNumber));

		// The statement might be present in multiple lines, thus merging all
		StringBuilder statementBuilder = new StringBuilder();
		statementBuilder.append(removeComment(lines.get(bodyLineCounter)));
		bodyLineCounter++;

		while (IndentSpaceParser.getIndentSpacesCount(lines.get(bodyLineCounter)) > indentedSpaceCount + 4) {
			statementBuilder.append(removeComment(lines.get(bodyLineCounter)));
			bodyLineCounter++;
		}

		PredicateInfo predicateInfo = PredicateParser.processDoWhileStatement(statementBuilder.toString());
		if (predicateInfo != null) {
			predicateInfoList.add(predicateInfo);
			updatedLines.add(spaces + "\t" + predicateInfo.getReuseStatement());
			updatedLines.add(spaces + predicateInfo.getParentStatement());
		}
		updatedLines.add(pos, spaces + predicateInfo.getInitializationStatement());
		return bodyLineCounter - 1;
	}

	/**
	 * Removes comment from the line of code
	 * 
	 * @param line The line
	 * @return The stripped line
	 */
	private static String removeComment(String line) {

		line = line.trim();
		if (line.contains("//")) {
			return line.substring(0, line.lastIndexOf("//")).trim();
		}
		return line;
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
		statementBuilder.append(removeComment(lines.get(startPos)));
		startPos++;

		while (IndentSpaceParser.getIndentSpacesCount(lines.get(startPos)) > indentedSpaceCount + 4) {
			statementBuilder.append(removeComment(lines.get(startPos)));
			startPos++;
		}

		PredicateInfo predicateInfo = PredicateParser.processIfStatement(statementBuilder.toString());

		if (predicateInfo != null) {
			predicateInfoList.add(predicateInfo);
			updatedLines.add(pos++, spaces + predicateInfo.getInitializationStatement());
			updatedLines.add(spaces + predicateInfo.getParentStatement());
		}
		List<String> innerBodyLines = new ArrayList<>();
		int bodyLineCounter = startPos;
		int parentLineNumber = startPos;
		while (bodyLineCounter < totalLines) {
			String line = lines.get(bodyLineCounter);
			if (StringUtils.isNotBlank(line.trim())) {
				if (IndentSpaceParser.getIndentSpacesCount(line) > indentedSpaceCount) {
					innerBodyLines.add(line);
				} else {
					break;
				}
			}
			bodyLineCounter++;
		}

		updatedLines.addAll(process(innerBodyLines, parentLineNumber));

		if (bodyLineCounter < totalLines
				&& IndentSpaceParser.getIndentSpacesCount(lines.get(bodyLineCounter)) == indentedSpaceCount) {
			String line = lines.get(bodyLineCounter).trim();
			if (line.equals("}")) {
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
			statementBuilder.append(removeComment(lines.get(bodyLineCounter)));
			bodyLineCounter++;

			while (IndentSpaceParser.getIndentSpacesCount(lines.get(bodyLineCounter)) > indentedSpaceCount + 4) {
				statementBuilder.append(removeComment(lines.get(bodyLineCounter)));
				bodyLineCounter++;
			}

			PredicateInfo predicateInfo = PredicateParser.processElseIfStatement(statementBuilder.toString());

			if (predicateInfo != null) {
				predicateInfoList.add(predicateInfo);
				updatedLines.add(pos++, spaces + predicateInfo.getInitializationStatement());
				updatedLines.add(spaces + predicateInfo.getParentStatement());
			}

			int parentLineNumber = bodyLineCounter;
			List<String> innerBodyLines = new ArrayList<>();
			while (bodyLineCounter < totalLines) {
				line = lines.get(bodyLineCounter);
				if (StringUtils.isNotBlank(line.trim())) {
					if (IndentSpaceParser.getIndentSpacesCount(line) > indentedSpaceCount) {
						innerBodyLines.add(line);
					} else {
						break;
					}
				}
				bodyLineCounter++;
			}

			updatedLines.addAll(process(innerBodyLines, parentLineNumber));

			if (bodyLineCounter < totalLines
					&& IndentSpaceParser.getIndentSpacesCount(lines.get(bodyLineCounter)) == indentedSpaceCount) {
				line = lines.get(bodyLineCounter).trim();
				if (line.equals("}")) {
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
	 * Processes the ternary assignment if found
	 * 
	 * @param lines
	 * @param updatedLines
	 * @param startPos
	 * @param totalLines
	 * @return
	 */
	private static int processTernaryAssignmentIfFound(List<String> lines, List<String> updatedLines, int startPos,
			int totalLines, boolean isReturnStatement) {

		// Getting the current indentation of for statement
		int indentedSpaceCount = IndentSpaceParser.getIndentSpacesCount(lines.get(startPos));

		// The statement might be present in multiple lines, thus merging all
		StringBuilder statementBuilder = new StringBuilder();
		statementBuilder.append(removeComment(lines.get(startPos)));
		startPos++;

		while (IndentSpaceParser.getIndentSpacesCount(lines.get(startPos)) > indentedSpaceCount) {
			statementBuilder.append(removeComment(lines.get(startPos)));
			startPos++;
		}

		PredicateInfo predicateInfo = PredicateParser.processTernaryStatement(statementBuilder.toString(),
				isReturnStatement);
		if (predicateInfo != null) {
			predicateInfoList.add(predicateInfo);
			updatedLines.add(predicateInfo.getInitializationStatement());
			updatedLines.add(predicateInfo.getParentStatement());
		} else {
			updatedLines.add(statementBuilder.toString());
		}

		return startPos - 1;
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
			int parentLineNumber = bodyLineCounter;
			updatedLines.add("else {");
			bodyLineCounter++;

			List<String> innerBodyLines = new ArrayList<>();

			while (bodyLineCounter < totalLines) {
				line = lines.get(bodyLineCounter);
				if (StringUtils.isNotBlank(line.trim())) {
					if (IndentSpaceParser.getIndentSpacesCount(line) > indentedSpaceCount) {
						innerBodyLines.add(line);
					} else {
						break;
					}
				}
				bodyLineCounter++;
			}

			updatedLines.addAll(process(innerBodyLines, parentLineNumber));

			if (bodyLineCounter < totalLines
					&& IndentSpaceParser.getIndentSpacesCount(lines.get(bodyLineCounter)) == indentedSpaceCount) {
				line = lines.get(bodyLineCounter).trim();
				if (line.equals("}")) {
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
	 * Processes the lines of code
	 * 
	 * @param lines            The lines
	 * @param parentLineNumber The parent moduleline number
	 * @return The processed lines of code
	 */
	private static List<String> process(List<String> lines, int parentLineNumber) {

		List<String> updatedLines = new ArrayList<>();
		int totalLines = lines.size();

		for (int i = 0; i < totalLines; i++) {
			if (lines.get(i).trim().startsWith(Keywords.FOR)) {
				System.out.println(String.format("Processing of for loop started. Line no: %d", parentLineNumber + i));
				i = processForLoop(lines, updatedLines, i, totalLines);
				System.out
						.println(String.format("Processing of for loop completed. Line no: %d", parentLineNumber + i));
			} else if (lines.get(i).trim().startsWith(Keywords.WHILE)) {
				System.out
						.println(String.format("Processing of while loop started. Line no: %d", parentLineNumber + i));
				i = processWhileLoop(lines, updatedLines, i, totalLines);
				System.out.println(
						String.format("Processing of while loop completed. Line no: %d", parentLineNumber + i));
			} else if (lines.get(i).trim().startsWith(Keywords.DO)) {
				System.out.println(
						String.format("Processing of do-while loop started. Line no: %d", parentLineNumber + i));
				i = processDoWhileLoop(lines, updatedLines, i, totalLines);
				System.out.println(
						String.format("Processing of do-while loop completed. Line no: %d", parentLineNumber + i));
			} else if (lines.get(i).trim().startsWith(Keywords.IF)) {
				System.out.println(
						String.format("Processing of if statements started. Line no: %d", parentLineNumber + i));
				i = processIfElseifElse(lines, updatedLines, i, totalLines);
				System.out.println(
						String.format("Processing of if statements completed. Line no: %d", parentLineNumber + i));
			} else if (lines.get(i).trim().matches("^\\w+ \\w+ \\=.*")) {
				System.out.println(
						String.format("Processing of ternary stament started. Line no: %d", parentLineNumber + i));
				i = processTernaryAssignmentIfFound(lines, updatedLines, i, totalLines, false);
				System.out.println(
						String.format("Processing of ternary stament completed. Line no: %d", parentLineNumber + i));
			} else if (lines.get(i).trim().matches("^return.*")) {
				System.out.println(
						String.format("Processing of ternary stament started. Line no: %d", parentLineNumber + i));
				i = processTernaryAssignmentIfFound(lines, updatedLines, i, totalLines, true);
				System.out.println(
						String.format("Processing of ternary stament completed. Line no: %d", parentLineNumber + i));
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
	 * @param fileName The file name
	 */
	private static void saveUpdatedCode(String code, String fileName) {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
			writer.write(code);
			writer.flush();
		} catch (IOException ioException) {
			System.out.println("Error saving the updated code.");
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

		if (!Files.exists(Paths.get(args[0]))) {
			try {
				Files.createDirectories(Paths.get(args[0]));
			} catch (IOException ioException) {
				System.out.println("Error creating the output directory.");
			}
		}

		try {
			String formattedJava = CodeFormatter.format(Paths.get(args[1]));
			predicateInfoList = new ArrayList<>();
			List<String> updatedLines = process(Arrays.asList(formattedJava.split("\n")), 0);

			// Saving the updated code
			StringBuilder codeBuilder = new StringBuilder();
			for (String line : updatedLines) {
				codeBuilder.append(line);
				codeBuilder.append("\n");
			}
			String formattedUpdatedCode = formatter.formatSource(codeBuilder.toString());
			saveUpdatedCode(formattedUpdatedCode,
					args[0] + File.separator + args[1].substring(args[1].lastIndexOf(File.separator) + 1));

			// Creating the predicates file
			PredicateRecorder.create(
					args[0] + File.separator + args[1].substring(args[1].lastIndexOf(File.separator) + 1),
					predicateInfoList);
		} catch (FormatterException formatterException) {
			System.out.println("Error formatting the code.");
		}
	}
}
