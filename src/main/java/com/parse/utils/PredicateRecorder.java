package com.parse.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The utility class PredicateRecorder. It holds implementation to records all
 * predicates found in the code.
 */
public class PredicateRecorder {

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
	 * Records the predicate statement
	 * 
	 * @param predicateStatement The predicate statement
	 */
	public static void record(String predicateStatement) {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter("predicates.txt", true))) {
			writer.append(predicateStatement);
			writer.newLine();
		} catch (IOException ioException) {
			System.out.println("Error recording the predicates.");
		}
	}
}
