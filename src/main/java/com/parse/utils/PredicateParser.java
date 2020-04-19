package com.parse.utils;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

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
	private static final Pattern TERNARY_STATEMENT_PATTERN_INIT = Pattern.compile("(\\w+ \\w+ \\= )(.*)(\\?.*\\:.*)");

	/**
	 * The ternary statement pattern at return
	 */
	private static final Pattern TERNARY_STATEMENT_PATTERN_RETURN = Pattern.compile("(return )(.*)(\\?.*\\:.*)");

	private PredicateParser() {
		// Its a utility class. Thus instantiation is not allowed.
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
			if (StringUtils.isBlank(control)) {
				control = "true";
			}
			String predicateName = "P_" + predicateCounter.getAndIncrement();
			return new PredicateInfo(predicateName, control, "FOR",
					StringUtils.join("boolean", " ", predicateName, "=", control, ";"),
					StringUtils.join(predicateName, "=", control, ";"),
					StringUtils.join("while(", predicateName, ")", "{"), matcher.group(1) + ";",
					matcher.group(3).replaceAll(",", ";") + ";");
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
			String predicateName = "P_" + predicateCounter.getAndIncrement();
			return new PredicateInfo(predicateName, control, "WHILE",
					StringUtils.join("boolean", " ", predicateName, "=", control, ";"),
					StringUtils.join(predicateName, "=", control, ";"),
					StringUtils.join(matcher.group(1), predicateName, matcher.group(3), "{"), null, null);
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
			String predicateName = "P_" + predicateCounter.getAndIncrement();
			return new PredicateInfo(predicateName, control, "DO-WHILE",
					StringUtils.join("boolean", " ", predicateName, ";"),
					StringUtils.join(predicateName, "=", control, ";"),
					StringUtils.join(matcher.group(1), predicateName, matcher.group(3)), null, null);
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
	 * Processes the ternary statement
	 * 
	 * @param statement         The statement
	 * @param isReturnStatement if the statement is return statement
	 * @return The predicate information
	 */
	public static PredicateInfo processTernaryStatement(String statement, boolean isReturnStatement) {

		ReplacementInfo replacementInfo = replaceStrings(statement);
		Matcher matcher = (isReturnStatement ? TERNARY_STATEMENT_PATTERN_RETURN : TERNARY_STATEMENT_PATTERN_INIT)
				.matcher(replacementInfo.getUpdatedString());
		if (matcher.find()) {
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
		return null;
	}
}
