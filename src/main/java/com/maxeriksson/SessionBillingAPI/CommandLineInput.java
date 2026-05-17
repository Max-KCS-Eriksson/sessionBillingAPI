package com.maxeriksson.SessionBillingAPI;

import java.util.List;
import java.util.Scanner;

/** CommandLineInput */
public class CommandLineInput {

    private final Scanner SCANNER;

    public CommandLineInput() {
        SCANNER = new Scanner(System.in);
    }

    public boolean inputConfirmation(String prompt) {
        prompt += " (y/n)";

        List<String> validOptions = List.of("y", "yes", "n", "no");

        String answer = "";
        while (!validOptions.contains(answer)) {
            answer = inputString(prompt);
            if (!validOptions.contains(answer)) {
                System.out.println("Invalid input - yes or no only. Try again.");
            }
        }
        return answer.equals("y") || answer.equals("yes");
    }

    public String inputString(String prompt) {
        String input = "";
        while (input.isBlank()) {
            System.out.print(prompt + " > ");

            input = SCANNER.nextLine().trim();
            if (input.isBlank()) {
                System.out.println("Invalid input. Try again.");
            }
        }
        return input;
    }

    public int inputInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(inputString(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input - only accept integers. Try again.");
            }
        }
    }

    public double inputDouble(String prompt) {
        while (true) {
            try {
                String localeCorrection = inputString(prompt).replace(",", ".");
                return Double.parseDouble(localeCorrection);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input - only accept numerics. Try again.");
            }
        }
    }

    public void close() {
        SCANNER.close();
    }
}
