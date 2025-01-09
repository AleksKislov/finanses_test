package org.example.application;

import org.example.domain.entities.Expense;
import org.example.domain.entities.User;

public class Logger {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";

    public void logln(String msg) {
        System.out.println(msg);
    };

    public void error(String msg) {
        logln(RED + msg + RESET);
    }

    public void infoln(String msg) {
        System.out.println(BLUE + msg + RESET);
    };

    public void log(String msg) {
        System.out.print(msg);
    };

    public void enterOptionLetter() {
        enterInput("Введите лат. букву пункта меню: ");
    }

    public void invalidOption() {
        this.error("Неверный выбор опции меню, попробуйте еще раз.");
    }

    public void enterInput(String msg) {
        this.log(YELLOW + msg + RESET);
    }

    public void displayUser(User user) {
        this.logln("\n=== Текущий пользователь: " + user.name() + " ===");
    }

    public void displayExpense(Expense expense) {
        int amount = expense.getAmount().amount();
        infoln(expense.getName() + ": " + amount + ", Оставшийся бюджет: " + (expense.isUseBudget() ? expense.getBudget().amount() - amount : "не установлен") );
    }
}
