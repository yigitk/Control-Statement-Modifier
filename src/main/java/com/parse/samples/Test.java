package com.parse.samples;

import org.apache.commons.lang3.StringUtils;

public class Test {

	public static void main(String[] args) {

		if (StringUtils.equalsAny("adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd", // perform
																														// addition
																														// block-wise,
																														// to
																														// ensure
																														// good
																														// cache
																														// behavior
				"adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd",
				"adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd",
				"adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd",
				"adasdasdasdasdasd")) {
			System.out.println("cool");
		} else if (StringUtils.equalsAny("adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd",
				"adasdasdasdasdasd", // perform
				// addition
				// block-wise,
				// to
				// ensure
				// good
				// cache
				// behavior
				"adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd",
				"adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd",
				"adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd", "adasdasdasdasdasd",
				"adasdasdasdasdasd")) {
			System.out.println("cool");
		}
	}
}
