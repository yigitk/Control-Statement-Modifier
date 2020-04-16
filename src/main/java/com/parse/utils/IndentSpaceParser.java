package com.parse.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The utility class IndentSpaceParser. It holds implementation to parse indents
 * from the lines of code.
 */
public class IndentSpaceParser {

	/**
	 * The indented line pattern
	 */
	private static final Pattern INDENTED_LINE_PATTERN = Pattern.compile("^( *).*$");

	private IndentSpaceParser() {
		// Its a utility class. Thus instantiation is now allowed.
	}

	/**
	 * Gets the indent spaces count from the line
	 * 
	 * @param line The line
	 * @return The spaces count
	 */
	public static int getIndentSpacesCount(String line) {

		Matcher matcher = INDENTED_LINE_PATTERN.matcher(line);
		if (matcher.find()) {
			return matcher.group(1).length();
		}
		return 0;
	}

	/**
	 * Gets the indent spaces count from the line
	 * 
	 * @param line The line
	 * @return The spaces count
	 */
	public static String getIndentSpaces(String line) {

		Matcher matcher = INDENTED_LINE_PATTERN.matcher(line);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return "";
	}
}
