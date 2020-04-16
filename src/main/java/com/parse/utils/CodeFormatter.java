package com.parse.utils;

import java.io.FileNotFoundException;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import de.hunsicker.jalopy.Jalopy;

/**
 * The utility class CodeFormatter. It holds implementation to format the java
 * code.
 */
public class CodeFormatter {

	private CodeFormatter() {
		// Its a utility class. Thus instantiation is not allowed.
	}

	/**
	 * Formats the java code
	 * 
	 * @param codePath The code file path
	 * @return The formatted code
	 */
	public static String format(Path codePath) {

		Jalopy jalopy = new Jalopy();
		try {
			StringBuffer buffer = new StringBuffer();
			jalopy.setInput(codePath.toFile());
			jalopy.setOutput(buffer);
			jalopy.format();
			return buffer.toString();
		} catch (FileNotFoundException exception) {
			System.out.println("Error: Input file not found!");
		}
		return StringUtils.EMPTY;
	}
}
