package com.parse.samples;

public class Test {

	public static String getReply(String message) {
		return "how are you?".equals(message) ? "I am fine. Thanks :)" : "How are you doing?";
	}

	public static void main(String[] args) {

		String message = "how are you?";
		String reply = "how are you?".equals(message) ? "I am fine. Thanks :)" : "How are you doing?";
		System.out.println(reply);

		int i = 0;
		while (i++ < 10) {
			System.out.println("Cool");
		}
	}
}
