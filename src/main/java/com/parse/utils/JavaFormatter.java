package com.parse.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

public class JavaFormatter {

	CodeFormatter codeFormatter;

	private static final Map<String, Object> DEFAULT_FORMATTER_OPTIONS;

	static {
		DEFAULT_FORMATTER_OPTIONS = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

		DEFAULT_FORMATTER_OPTIONS.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		DEFAULT_FORMATTER_OPTIONS.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_INDENT_PARAMETER_DESCRIPTION,
				DefaultCodeFormatterConstants.FALSE);
		DEFAULT_FORMATTER_OPTIONS.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS,
				DefaultCodeFormatterConstants.createAlignmentValue(true,
						DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE,
						DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
		DEFAULT_FORMATTER_OPTIONS
				.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_CONSTRUCTOR_DECLARATION,
						DefaultCodeFormatterConstants.createAlignmentValue(false,
								DefaultCodeFormatterConstants.WRAP_COMPACT,
								DefaultCodeFormatterConstants.INDENT_DEFAULT));

		// Formats custom file headers if provided
		DEFAULT_FORMATTER_OPTIONS.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_HEADER,
				DefaultCodeFormatterConstants.TRUE);
		DEFAULT_FORMATTER_OPTIONS.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "160");
		DEFAULT_FORMATTER_OPTIONS.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "120");
	}

	public JavaFormatter() {

		this.codeFormatter = ToolFactory.createCodeFormatter(new HashMap<>(DEFAULT_FORMATTER_OPTIONS),
				ToolFactory.M_FORMAT_EXISTING);
	}

	/**
	 * Formats the code
	 * 
	 * @param code The unformatted code
	 * @return The formatted code
	 */
	public String format(String code) {

		final TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS,
				code, 0, code.length(), 0, System.getProperty("line.separator"));

		if (edit == null) {
			return code;
		}

		IDocument document = new Document(code);
		try {
			edit.apply(document);
		} catch (Exception e) {
			throw new RuntimeException("Failed to format the generated source code.", e);
		}

		return document.get();
	}
}
