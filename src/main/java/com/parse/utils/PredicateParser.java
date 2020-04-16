package com.parse.utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.parse.models.PredicateInfo;

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
			return new PredicateInfo(predicateName, StringUtils.join("boolean", " ", predicateName, "=", control, ";"),
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
			return new PredicateInfo(predicateName, StringUtils.join("boolean", " ", predicateName, "=", control, ";"),
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
			return new PredicateInfo(predicateName, StringUtils.join("boolean", " ", predicateName, ";"),
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
			return new PredicateInfo(predicateName, StringUtils.join("boolean", " ", predicateName, "=", control, ";"),
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
			return new PredicateInfo(predicateName, StringUtils.join("boolean", " ", predicateName, "=", control, ";"),
					StringUtils.join(predicateName, "=", control, ";"),
					StringUtils.join(matcher.group(1), predicateName, matcher.group(3), "{"), null, null);
		}
		return null;
	}
}
