package org.example;

import org.example.view.Menu;

public class Main {
    public static void main(String[] args) {
        System.out.println("Внимание! для работы нужен redis, порт 6379");
        System.out.println("Внимание! для денег используется тип Integer");

        Menu menu = new Menu();
        menu.runMainMenu();
    }
}