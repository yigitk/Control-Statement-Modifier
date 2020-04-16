package com.parse.samples;

public class NestedDoWhileLoops {

	public static void main(String[] args) {

		int i = 0;
		int j = 0;
		int k = 0;
		do {
			do {
				do {
					System.out.println("Cool");
				} while (k < 10);
			} while (j < 10);
		} while (i < 10);
	}
}
