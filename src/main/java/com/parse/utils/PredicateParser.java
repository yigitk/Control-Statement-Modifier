package com.parse.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.parse.models.PredicateInfo;
import com.parse.models.ProcessedStatementInfo;

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
	 * The atomic boolean counter
	 */
	private static AtomicInteger booleanCounter = new AtomicInteger();

	/**
	 * The if statement pattern
	 */
	private static final Pattern IF_PATTERN = Pattern.compile("(if \\()(.*)(\\))");

	/**
	 * The else-if statement pattern
	 */
	private static final Pattern ELSE_IF_PATTERN = Pattern.compile("(else if \\()(.*)(\\))");

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

	private PredicateParser() {
		// Its a utility class. Thus instantiation is not allowed.
	}

	private static ProcessedStatementInfo processStatement(String statement, Integer predicateId,
			boolean insertPredicate) {

		ProcessedStatementInfo processedStatementInfo = new ProcessedStatementInfo();
		char[] chars = statement.toCharArray();
		int totalChars = chars.length;
		int counter = 0;
		int endIndex = -1;
		StringBuilder statementBuilder = new StringBuilder();
		List<String> predicates = new ArrayList<>();

		while (counter < totalChars) {
			if (chars[counter] == '"') {
				statementBuilder.append(chars[counter++]);
				while (counter < totalChars) {
					if (chars[counter] == '"' && chars[counter - 1] != '\\') {
						statementBuilder.append(chars[counter++]);
						break;
					}
					statementBuilder.append(chars[counter++]);
				}
			} else if (chars[counter] == '\'') {
				statementBuilder.append(chars[counter++]);
				while (counter < totalChars) {
					if (chars[counter] == '\'' && chars[counter - 1] != '\\') {
						statementBuilder.append(chars[counter++]);
						break;
					}
					statementBuilder.append(chars[counter++]);
				}
			} else if ((chars[counter] == '|' && chars[counter + 1] == '|')
					|| (chars[counter] == '&' && chars[counter + 1] == '&')) {
				insertPredicate = true;
				endIndex = statementBuilder.length();
				statementBuilder.append(chars[counter++]);
				statementBuilder.append(chars[counter++]);
				StringBuilder subStatementBuilder = new StringBuilder();
				while (counter < totalChars) {
					subStatementBuilder.append(chars[counter++]);
				}
				ProcessedStatementInfo subdiv = processStatement(subStatementBuilder.toString(), predicateId, true);
				statementBuilder.append(subdiv.getConvertedStatement());
				predicates.addAll(subdiv.getPredicates());
				break;
			} else if (chars[counter] == '(') {
				statementBuilder.append(chars[counter++]);
				int bracesCount = 1;
				StringBuilder subStatementBuilder = new StringBuilder();
				while (counter < totalChars) {
					if (chars[counter] == '"') {
						subStatementBuilder.append(chars[counter++]);
						while (counter < totalChars) {
							if (chars[counter] == '"' && chars[counter - 1] != '\\') {
								subStatementBuilder.append(chars[counter++]);
								break;
							}
							subStatementBuilder.append(chars[counter++]);
						}
					} else if (chars[counter] == '\'') {
						subStatementBuilder.append(chars[counter++]);
						while (counter < totalChars) {
							if (chars[counter] == '\'' && chars[counter - 1] != '\\') {
								subStatementBuilder.append(chars[counter++]);
								break;
							}
							subStatementBuilder.append(chars[counter++]);
						}
					} else if (chars[counter] == '(') {
						subStatementBuilder.append(chars[counter++]);
						bracesCount++;
					} else if (chars[counter] == ')') {
						bracesCount--;
						if (bracesCount > 0) {
							subStatementBuilder.append(chars[counter++]);
						} else {
							break;
						}
					} else {
						subStatementBuilder.append(chars[counter++]);
					}
				}
				ProcessedStatementInfo subdiv = processStatement(subStatementBuilder.toString(), predicateId, false);
				statementBuilder.append(subdiv.getConvertedStatement());
				subStatementBuilder.append(")");
				predicates.addAll(subdiv.getPredicates());
			} else {
				statementBuilder.append(chars[counter++]);
			}
		}

		if (insertPredicate) {
			String predicate = "P" + predicateId + "_" + booleanCounter.getAndIncrement();
			if (endIndex == -1) {
				endIndex = statementBuilder.length();
			}
			statementBuilder.insert(endIndex, "))");
			statementBuilder.insert(0, "(" + predicate + "=" + "(");
			predicates.add(predicate);
		}
		processedStatementInfo.setConvertedStatement(statementBuilder.toString());
		processedStatementInfo.setPredicates(predicates);
		processedStatementInfo.setPredicateStatement("");
		return processedStatementInfo;
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
			if (!StringUtils.equals("true", control)) {
				Integer counter = predicateCounter.getAndIncrement();
				booleanCounter = new AtomicInteger();
				ProcessedStatementInfo processedStatementInfo = processStatement(control, counter, true);
				String predicateName = "P" + counter;
				StringBuilder predicateInitStatementBuilder = new StringBuilder();
				for (String predicate : processedStatementInfo.getPredicates()) {
					predicateInitStatementBuilder
							.append(StringUtils.join("boolean", " ", predicate, "=", "false", ";"));
				}

				String convertedStatement = StringUtils.join("if(", processedStatementInfo.getConvertedStatement(), ")",
						"{");
				return new PredicateInfo(predicateName, "IF", processedStatementInfo.getConvertedStatement(),
						predicateInitStatementBuilder.toString(), convertedStatement);
			}
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
			if (!StringUtils.equals("true", control)) {
				Integer counter = predicateCounter.getAndIncrement();
				booleanCounter = new AtomicInteger();
				ProcessedStatementInfo processedStatementInfo = processStatement(control, counter, true);
				String predicateName = "P" + counter;
				StringBuilder predicateInitStatementBuilder = new StringBuilder();
				for (String predicate : processedStatementInfo.getPredicates()) {
					predicateInitStatementBuilder
							.append(StringUtils.join("boolean", " ", predicate, "=", "false", ";"));
				}

				String convertedStatement = StringUtils.join("else if(", processedStatementInfo.getConvertedStatement(),
						")", "{");
				return new PredicateInfo(predicateName, "ELSE-IF", processedStatementInfo.getConvertedStatement(),
						predicateInitStatementBuilder.toString(), convertedStatement);
			}
		}
		return null;
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
			if (StringUtils.isNotBlank(control) && !StringUtils.equals("true", control)) {

				Integer counter = predicateCounter.getAndIncrement();
				booleanCounter = new AtomicInteger();
				ProcessedStatementInfo processedStatementInfo = processStatement(control, counter, true);
				String predicateName = "P" + counter;
				StringBuilder predicateInitStatementBuilder = new StringBuilder();
				for (String predicate : processedStatementInfo.getPredicates()) {
					predicateInitStatementBuilder
							.append(StringUtils.join("boolean", " ", predicate, "=", "false", ";"));
				}

				String convertedStatement = StringUtils.join("for(", matcher.group(1), ";",
						processedStatementInfo.getConvertedStatement(), ";", matcher.group(3), ")", "{");
				return new PredicateInfo(predicateName, "FOR", processedStatementInfo.getConvertedStatement(),
						predicateInitStatementBuilder.toString(), convertedStatement);
			}
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
				Integer counter = predicateCounter.getAndIncrement();
				booleanCounter = new AtomicInteger();
				ProcessedStatementInfo processedStatementInfo = processStatement(control, counter, true);
				String predicateName = "P" + counter;
				StringBuilder predicateInitStatementBuilder = new StringBuilder();
				for (String predicate : processedStatementInfo.getPredicates()) {
					predicateInitStatementBuilder
							.append(StringUtils.join("boolean", " ", predicate, "=", "false", ";"));
				}

				String convertedStatement = StringUtils.join("while(", processedStatementInfo.getConvertedStatement(),
						")", "{");
				return new PredicateInfo(predicateName, "WHILE", processedStatementInfo.getConvertedStatement(),
						predicateInitStatementBuilder.toString(), convertedStatement);
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
				String predicateName = "P" + predicateCounter.getAndIncrement();
				String predicateInitStatement = StringUtils.join("boolean", " ", predicateName, "=", "false", ";");
				String convertedStatement = StringUtils.join("} while(", predicateName, "=", control, ")", ";");
				return new PredicateInfo(predicateName, "DO-WHILE", control, predicateInitStatement, convertedStatement);
			}
		}
		return null;
	}

}
