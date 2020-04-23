package com.parse.samples;

public class Test {

  public static void main(String[] args) {

    // Simple for loop
    {
      int i = 0;
      boolean P_0 = i < 2;
      while (P_0) {
        System.out.println(i);
        i++;
        P_0 = i < 2;
      }
    }

    // Simple while loop
    int i = 0;
    boolean P_1 = i++ < 2;
    while (P_1) {
      System.out.println(i);
      P_1 = i++ < 2;
    }

    // Simple do-while loop
    i = 0;
    boolean P_2;
    do {
      System.out.println(i);
      P_2 = i++ < 2;
    } while (P_2);

    // Simple if condition
    i = 2;
    boolean P_3 = i < 5;
    if (P_3) {
      System.out.println("i is less than 5");
    }

    // Simple if-else condition
    i = 6;
    boolean P_4 = i < 5;
    if (P_4) {
      System.out.println("i is less than 5");
    } else {
      System.out.println("i is greater than 5");
    }

    // Simple if-elseif-else condition
    i = 10;
    boolean P_6 = i >= 5 && i < 10;
    boolean P_5 = i > 0 && i < 5;
    if (P_5) {
      System.out.println("i is in range 1-4");
    } else if (P_6) {
      System.out.println("i is in range 5-9");
    } else {
      System.out.println("i is greater than 9");
    }

    // Simple switch
    i = 0;
    boolean P_7 = Integer.valueOf(i).equals(0);
    boolean P_8 = Integer.valueOf(i).equals(1);
    if (P_7) {
      System.out.println("i is 0");
    } else if (P_8) {
      System.out.println("i is 1");
    } else {
      System.out.println("i is greater than 1");
    }

    // Simple ternary
    i = 10;
    boolean P_9 = i > 5;
    i = P_9 ? i - 2 : i + 2;
    System.out.println(i);
  }
}
