package com.parse.samples;

public class NestedForLoops {

	public static void main(String[] args) {
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < i; j++)
				for (int k = 0; k < j; k++)
					System.out.println(String.format("Value of i=%d \n value of j=%d", i, j));
	}
}
