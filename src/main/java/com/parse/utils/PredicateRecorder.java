package com.parse.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.parse.models.PredicateInfo;

/**
 * The utility class PredicateRecorder. It holds implementation to records all
 * predicates found in the code.
 */
public class PredicateRecorder {

	/**
	 * The predicate pattern
	 */
	private static final Pattern PREDICATE_PATTERN = Pattern.compile("(boolean )?(P_\\d+) \\=.*");

	private PredicateRecorder() {
		// Its a utility class. Thus instantiation is not allowed.
	}

	static {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("predicates.txt"))) {
			writer.append("");
		} catch (IOException ioException) {
			System.out.println("Error recording the predicates.");
		}
	}

	/**
	 * Creates the predicate file
	 * 
	 * @param The               code path
	 * @param predicateInfoList The predicates info list
	 */
	public static void create(String codePath, List<PredicateInfo> predicateInfoList) {

		HashMap<String, String> predicateLineNumberMap = new HashMap<>();
		predicateInfoList.forEach(predicateInfo -> predicateLineNumberMap.put(predicateInfo.getName(), ""));

		try (BufferedWriter writer = new BufferedWriter(new FileWriter("predicates.txt", true))) {
			List<String> lines = Files.readAllLines(Paths.get(codePath));
			Integer lineNumber = 1;
			for (String line : lines) {
				Matcher matcher = PREDICATE_PATTERN.matcher(line.trim());
				if (matcher.find()) {
					String predicate = matcher.group(2);
					if (predicateLineNumberMap.containsKey(predicate)) {
						String value = predicateLineNumberMap.get(predicate);
						if (StringUtils.isBlank(value)) {
							predicateLineNumberMap.put(predicate, lineNumber.toString());
						} else {
							predicateLineNumberMap.put(predicate, value + ", " + lineNumber.toString());
						}
					}
				}
				lineNumber++;
			}

			predicateInfoList.forEach(predicateInfo -> {
				try {
					writer.append(StringUtils.join(
							Arrays.asList(predicateInfo.getName(), predicateInfo.getControl().trim(),
									predicateInfo.getType(), predicateLineNumberMap.get(predicateInfo.getName())),
							", "));
					writer.newLine();
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			});
			writer.flush();
		} catch (IOException ioException) {
			System.out.println("Error recording the predicates.");
		}
	}
}
