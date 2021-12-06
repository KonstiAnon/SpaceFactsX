package main;


import Api.ApiMiner;

import java.util.Scanner;

public class Terminal {
    ApiMiner miner = new ApiMiner();
    Scanner scan = new Scanner(System.in);


    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        System.out.println("Welcome to the SpaceX facts generator! (Not affiliated with SpaceX in any way shape or form)");
        while (true) {
            terminal.help();
            terminal.nextCommand();
        }
    }


    private static void help() {
        System.out.println("List of available stats:");
        System.out.println("\t[1]Find out more about launches.");
        System.out.println("\t[2]Find out more about astronauts.");
        System.out.println("\t[0]Exit");
    }

    private void nextCommand() {
        switch (scan.nextLine()) {
            case "1" -> miner.launchFacts();
            case "2" -> miner.astronautFacts();

            case "0" -> {
                System.out.println("Thank you for using this tool :)");
                System.exit(0);
            }
            default -> System.out.println("The command you entered was invalid, please choose a number between 1 and 2");

        }
    }
}
