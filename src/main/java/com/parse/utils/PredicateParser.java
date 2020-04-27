package com.parse.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.parse.models.Case;
import com.parse.models.PredicateInfo;
import com.parse.models.ReplacementInfo;

/**
 * The utility class PredicateParser. It holds implementations to parse
 * predicates from various control statements.
 */
public class PredicateParser {

	/**
	 * The atomic predicate counter
	 */
	private static AtomicInteger predicateCounter = new AtomicInteger();

	/**
	 * The 'for' statement pattern
	 */
	private static final Pattern FOR_PATTERN = Pattern.compile("for \\(([^\\;]*)\\;([^\\;]*)\\;([^\\;]*)\\)");

	/**
	 * The 'while' statement pattern
	 */
	private static final Pattern WHILE_PATTERN = Pattern.compile("(while \\()(.*)(\\))");

	/**
	 * The 'do-while' statement pattern
	 */
	private static final Pattern DO_WHILE_PATTERN = Pattern.compile("(\\} while \\()(.*)(\\)\\;)");

	/**
	 * The if statement pattern
	 */
	private static final Pattern IF_PATTERN = Pattern.compile("(if \\()(.*)(\\))");

	/**
	 * The else-if statement pattern
	 */
	private static final Pattern ELSE_IF_PATTERN = Pattern.compile("(else if \\()(.*)(\\))");

	/**
	 * The ternary statement pattern at initialization
	 */
	private static final Pattern TERNARY_STATEMENT_PATTERN_INIT = Pattern
			.compile("((\\w+ )?\\w+ \\= )(.*)(\\?.*\\:.*)");

	/**
	 * The ternary statement pattern at return
	 */
	private static final Pattern TERNARY_STATEMENT_PATTERN_RETURN = Pattern.compile("(return )(.*)(\\?.*\\:.*)");

	/**
	 * The switch pattern
	 */
	private static final Pattern SWITCH_PATTERN = Pattern.compile("switch \\((.*)\\)");

	/**
	 * The switch case pattern
	 */
	private static final Pattern SWITCH_CASE_PATTERN = Pattern.compile("[case|default]( .*)?\\:");

	private PredicateParser() {
		// Its a utility class. Thus instantiation is not allowed.
	}

	/**
	 * Processes the var initialization statement
	 * 
	 * @param statement The statement
	 * @return The processed statement
	 */
	private static String processVarInitializationStatement(String statement) {

		if (statement.trim().matches("^[\\w_]+ [\\w_]+ \\=.*")) {
			return statement + ";";
		} else {
			char[] chars = statement.trim().toCharArray();
			int totalChars = chars.length;
			int bracketCount = 0;
			for (int i = 0; i < totalChars; i++) {
				if (chars[i] == '(' || chars[i] == '<' || chars[i] == '{' || chars[i] == '[') {
					bracketCount++;
				} else if (chars[i] == ')' || chars[i] == '>' || chars[i] == '}' || chars[i] == ']') {
					bracketCount--;
				} else if (chars[i] == ',') {
					if (bracketCount == 0) {
						chars[i] = ';';
					}
				}
			}

			return new String(chars) + ";";
		}
	}

	/**
	 * Processes the var change statement
	 * 
	 * @param statement The statement
	 * @return The processed statement
	 */
	private static String processVarChangeStatement(String statement) {

		char[] chars = statement.trim().toCharArray();
		int totalChars = chars.length;
		int bracketCount = 0;
		for (int i = 0; i < totalChars; i++) {
			if (chars[i] == '(') {
				bracketCount++;
			} else if (chars[i] == ')') {
				bracketCount--;
			} else if (chars[i] == ',') {
				if (bracketCount == 0) {
					chars[i] = ';';
				}
			}
		}

		return new String(chars) + ";";
	}

	/**
	 * Processes the 'for' statement
	 * 
	 * @param statement The statement
	 * @return The processed predicate information
	 */
	public static PredicateInfo processForStatement(String statement) {

		Matcher matcher = FOR_PATTERN.matcher(statement);
		if (matcher.find()) {
			String control = matcher.group(2).trim();
			if (StringUtils.isBlank(control) || StringUtils.equals("true", control)) {
				return null;
			}

			String varInitializationStatement = processVarInitializationStatement(matcher.group(1));
			String varChangeStatement = processVarChangeStatement(matcher.group(3));

			String predicateName = "P_" + predicateCounter.getAndIncrement();
			return new PredicateInfo(predicateName, control, "FOR",
					StringUtils.join("boolean", " ", predicateName, "=", control, ";"),
					StringUtils.join(predicateName, "=", control, ";"),
					StringUtils.join("while(", predicateName, ")", "{"), varInitializationStatement,
					varChangeStatement);
		}
		return null;
	}

	/**
	 * Processes the 'while' statement
	 * 
	 * @param statement The statement
	 * @return The processed predicate information
	 */
	public static PredicateInfo processWhileStatement(String statement) {

		Matcher matcher = WHILE_PATTERN.matcher(statement);
		if (matcher.find()) {
			String control = matcher.group(2).trim();
			if (!StringUtils.equals("true", control)) {
				String predicateName = "P_" + predicateCounter.getAndIncrement();
				return new PredicateInfo(predicateName, control, "WHILE",
						StringUtils.join("boolean", " ", predicateName, "=", control, ";"),
						StringUtils.join(predicateName, "=", control, ";"),
						StringUtils.join(matcher.group(1), predicateName, matcher.group(3), "{"), null, null);
			}
		}
		return null;
	}

	/**
	 * Processes the 'do-while' statement
	 * 
	 * @param statement The statement
	 * @return The processed predicate information
	 */
	public static PredicateInfo processDoWhileStatement(String statement) {

		Matcher matcher = DO_WHILE_PATTERN.matcher(statement);
		if (matcher.find()) {
			String control = matcher.group(2).trim();
			if (!StringUtils.equals("true", control)) {
				String predicateName = "P_" + predicateCounter.getAndIncrement();
				return new PredicateInfo(predicateName, control, "DO-WHILE",
						StringUtils.join("boolean", " ", predicateName, " ", "=", "false;"),
						StringUtils.join(predicateName, "=", control, ";"),
						StringUtils.join(matcher.group(1), predicateName, matcher.group(3)), null, null);
			}
		}
		return null;
	}

	/**
	 * Processes the 'if' statement
	 * 
	 * @param statement The statement
	 * @return The processed predicate information
	 */
	public static PredicateInfo processIfStatement(String statement) {

		Matcher matcher = IF_PATTERN.matcher(statement);
		if (matcher.find()) {
			String control = matcher.group(2).trim();
			String predicateName = "P_" + predicateCounter.getAndIncrement();
			return new PredicateInfo(predicateName, control, "IF",
					StringUtils.join("boolean", " ", predicateName, "=", control, ";"),
					StringUtils.join(predicateName, "=", control, ";"),
					StringUtils.join(matcher.group(1), predicateName, matcher.group(3), "{"), null, null);
		}
		return null;
	}

	/**
	 * Processes the 'else-if' statement
	 * 
	 * @param statement The statement
	 * @return The processed predicate information
	 */
	public static PredicateInfo processElseIfStatement(String statement) {

		Matcher matcher = ELSE_IF_PATTERN.matcher(statement);
		if (matcher.find()) {
			String control = matcher.group(2).trim();
			String predicateName = "P_" + predicateCounter.getAndIncrement();
			return new PredicateInfo(predicateName, control, "ELSE-IF",
					StringUtils.join("boolean", " ", predicateName, "=", control, ";"),
					StringUtils.join(predicateName, "=", control, ";"),
					StringUtils.join(matcher.group(1), predicateName, matcher.group(3), "{"), null, null);
		}
		return null;
	}

	/**
	 * Replaces strings in the statement
	 * 
	 * @param statement The statement
	 * @return The replacement info
	 */
	private static ReplacementInfo replaceStrings(String statement) {

		char[] chars = statement.toCharArray();
		int totalChars = chars.length;
		Integer stringCounter = 0;
		Integer indexCounter = 0;
		StringBuilder statementBuilder = new StringBuilder();
		HashMap<String, String> replacementMap = new HashMap<>();

		while (indexCounter < totalChars) {
			if (chars[indexCounter] == '\"') {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("\"");
				indexCounter++;
				while (indexCounter < totalChars) {
					if (chars[indexCounter] == '\"') {
						stringBuilder.append(chars[indexCounter]);
						if (chars[indexCounter - 1] != '\\') {
							break;
						}
					} else {
						stringBuilder.append(chars[indexCounter]);
					}
					indexCounter++;
				}

				String replacement = "#Predicate_Replacement" + stringCounter++;
				replacementMap.put(replacement, stringBuilder.toString());
				statementBuilder.append(replacement);
			} else {
				statementBuilder.append(chars[indexCounter]);
			}
			indexCounter++;
		}

		return new ReplacementInfo(statementBuilder.toString(), replacementMap);
	}

	/**
	 * Processes the initialization ternary statement
	 * 
	 * @param statement The statement
	 * @return The predicate information
	 */
	public static PredicateInfo processInitializationTernaryStatement(String statement) {

		ReplacementInfo replacementInfo = replaceStrings(statement);
		Matcher matcher = TERNARY_STATEMENT_PATTERN_INIT.matcher(replacementInfo.getUpdatedString());
		if (matcher.find()) {
			String updatedString = replacementInfo.getUpdatedString();
			int refIndex = updatedString.indexOf("?");
			if (!areBracketsEqual(updatedString.substring(0, refIndex))) { // The ternary statement is present inside
																			// parenthesis
				StringBuilder statementBuilder = new StringBuilder();
				statementBuilder.append(" )");
				int count = 1;
				int index = refIndex - 2;
				while (index >= 0 && count != 0) {
					char ch = updatedString.charAt(index);
					if (ch == '(') {
						count--;
					} else if (ch == ')') {
						count++;
					}
					index--;
					statementBuilder.append(ch);
				}
				index++;

				String control = statementBuilder.reverse().toString();

				// Reverting back the replacements in predicate
				for (Entry<String, String> entry : replacementInfo.getReplacementMap().entrySet()) {
					control = control.replaceAll(entry.getKey(), entry.getValue());
				}

				String predicateName = "P_" + predicateCounter.getAndIncrement();
				String parentStatement = StringUtils.join(updatedString.substring(0, index + 1), predicateName,
						updatedString.substring(refIndex));
				// Reverting back the replacements in predicate
				for (Entry<String, String> entry : replacementInfo.getReplacementMap().entrySet()) {
					parentStatement = parentStatement.replaceAll(entry.getKey(), entry.getValue());
				}

				return new PredicateInfo(predicateName, control, "TERNARY",
						StringUtils.join("boolean", " ", predicateName, "=", control, ";"),
						StringUtils.join(predicateName, "=", control, ";"), parentStatement, null, null);

			} else {
				String control = matcher.group(3);
				// Reverting back the replacements in predicate
				for (Entry<String, String> entry : replacementInfo.getReplacementMap().entrySet()) {
					control = control.replaceAll(entry.getKey(), entry.getValue());
				}

				String predicateName = "P_" + predicateCounter.getAndIncrement();
				String parentStatement = StringUtils.join(matcher.group(1), predicateName, matcher.group(4));
				// Reverting back the replacements in predicate
				for (Entry<String, String> entry : replacementInfo.getReplacementMap().entrySet()) {
					parentStatement = parentStatement.replaceAll(entry.getKey(), entry.getValue());
				}

				return new PredicateInfo(predicateName, control, "TERNARY",
						StringUtils.join("boolean", " ", predicateName, "=", control, ";"),
						StringUtils.join(predicateName, "=", control, ";"), parentStatement, null, null);
			}
		}
		return null;
	}

	/**
	 * Checks if the brackets are equal in the statements
	 * 
	 * @param statement The statement
	 * @return The equality status
	 */
	private static boolean areBracketsEqual(String statement) {

		char[] chars = statement.toCharArray();
		int open = 0;
		int close = 0;
		for (char ch : chars) {
			if (ch == '(') {
				open++;
			} else if (ch == ')') {
				close++;
			}
		}

		return open == close;
	}

	/**
	 * Processes the return ternary statement
	 * 
	 * @param statement The statement
	 * @return The predicate information
	 */
	public static PredicateInfo processReturnTernaryStatement(String statement) {

		ReplacementInfo replacementInfo = replaceStrings(statement);
		Matcher matcher = TERNARY_STATEMENT_PATTERN_RETURN.matcher(replacementInfo.getUpdatedString());
		if (matcher.find()) {
			String updatedString = replacementInfo.getUpdatedString();
			int refIndex = updatedString.indexOf("?");
			if (!areBracketsEqual(updatedString.substring(0, refIndex))) { // The ternary statement is present inside
																			// parenthesis
				StringBuilder statementBuilder = new StringBuilder();
				statementBuilder.append(" )");
				int count = 1;
				int index = refIndex - 2;
				while (index >= 0 && count != 0) {
					char ch = updatedString.charAt(index);
					if (ch == '(') {
						count--;
					} else if (ch == ')') {
						count++;
					}
					index--;
					statementBuilder.append(ch);
				}
				index++;

				String control = statementBuilder.reverse().toString();

				// Reverting back the replacements in predicate
				for (Entry<String, String> entry : replacementInfo.getReplacementMap().entrySet()) {
					control = control.replaceAll(entry.getKey(), entry.getValue());
				}

				String predicateName = "P_" + predicateCounter.getAndIncrement();
				String parentStatement = StringUtils.join(updatedString.substring(0, index + 1), predicateName,
						updatedString.substring(refIndex));
				// Reverting back the replacements in predicate
				for (Entry<String, String> entry : replacementInfo.getReplacementMap().entrySet()) {
					parentStatement = parentStatement.replaceAll(entry.getKey(), entry.getValue());
				}

				return new PredicateInfo(predicateName, control, "TERNARY",
						StringUtils.join("boolean", " ", predicateName, "=", control, ";"),
						StringUtils.join(predicateName, "=", control, ";"), parentStatement, null, null);

			} else {
				String control = matcher.group(2);
				// Reverting back the replacements in predicate
				for (Entry<String, String> entry : replacementInfo.getReplacementMap().entrySet()) {
					control = control.replaceAll(entry.getKey(), entry.getValue());
				}

				String predicateName = "P_" + predicateCounter.getAndIncrement();
				String parentStatement = StringUtils.join(matcher.group(1), predicateName, matcher.group(3));
				// Reverting back the replacements in predicate
				for (Entry<String, String> entry : replacementInfo.getReplacementMap().entrySet()) {
					parentStatement = parentStatement.replaceAll(entry.getKey(), entry.getValue());
				}

				return new PredicateInfo(predicateName, control, "TERNARY",
						StringUtils.join("boolean", " ", predicateName, "=", control, ";"),
						StringUtils.join(predicateName, "=", control, ";"), parentStatement, null, null);
			}
		}
		return null;
	}

	/**
	 * Gets the first switch operand
	 * 
	 * @param statement The switch statement
	 * @return The first operand
	 */
	public static String getSwitchFirstOperand(String statement) {

		Matcher matcher = SWITCH_PATTERN.matcher(statement);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Parses the cases from switch inner body
	 * 
	 * @param switchBodyLines The switch body lines
	 * @return The cases
	 */
	public static List<Case> processCases(List<String> switchBodyLines) {

		List<Case> cases = new ArrayList<>();

		if (!switchBodyLines.isEmpty()) {
			int totalInnerBodyLines = switchBodyLines.size();
			int lineCounter = 0;
			int spaceCount = IndentSpaceParser.getIndentSpacesCount(switchBodyLines.get(0));
			String spaces = IndentSpaceParser.getIndentSpaces(switchBodyLines.get(0));

			while (lineCounter < totalInnerBodyLines) {
				String line = switchBodyLines.get(lineCounter);
				if (!line.trim().equals("default:") && line.endsWith("default:")) {
					line = line.substring(0, line.lastIndexOf("default:"));
					switchBodyLines.add(lineCounter + 1, spaces + "default:");
					totalInnerBodyLines++;
				}

				Matcher matcher = SWITCH_CASE_PATTERN.matcher(line.trim());
				String operand = "";
				if (matcher.find()) {
					operand = matcher.group(1) == null ? "" : matcher.group(1).trim();
					List<String> body = new ArrayList<>();
					boolean withBreak = false;

					lineCounter++;
					while (lineCounter < totalInnerBodyLines
							&& IndentSpaceParser.getIndentSpacesCount(switchBodyLines.get(lineCounter)) > spaceCount) {
						if (switchBodyLines.get(lineCounter).trim().equals("break;")) {
							withBreak = true;
							lineCounter++;
							break;
						}
						body.add(switchBodyLines.get(lineCounter));
						lineCounter++;
					}
					cases.add(new Case(operand, body, withBreak));
				} else {
					lineCounter++;
				}
			}
		}
		return cases;
	}
}
