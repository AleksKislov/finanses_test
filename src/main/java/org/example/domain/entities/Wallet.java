package org.example.domain.entities;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class Wallet {
    private final Map<String, Expense> allExpenses;
    private final Map<String, Income> allIncome;

    public Wallet(Map<String, Expense> expenses, Map<String, Income> incomes) {
        this.allExpenses = expenses;
        this.allIncome = incomes;
    }

    public void addExpense(Expense expense) {
        // проверить существует ли уже такая category
        String expenseName = expense.getName();
        Expense currentExpense = allExpenses.get(expenseName);
        if (currentExpense != null) {
            currentExpense.add(expense.getAmount());
            if (expense.isUseBudget()) currentExpense.setBudget(expense.getBudget());
            allExpenses.put(expenseName, currentExpense);
        } else {
            allExpenses.put(expenseName, expense);
        }
    }

    public void addIncome(Income income) {
        // проверить существует ли уже такая category
        String incomeName = income.getName();
        Income currentIncome = allIncome.get(incomeName);
        if (currentIncome != null) {
            currentIncome.add(income.getAmount());
            allIncome.put(incomeName, currentIncome);
        } else {
            allIncome.put(incomeName, income);
        }
    }

    public int getIncomeAmount(String[] incomeNames) {
        return getAmount(allIncome, incomeNames);
    }

    public int getExpensesAmount(String[] expenseNames) {
        return getAmount(allExpenses, expenseNames);
    }

    private int getAmount(@NotNull Map<String, ? extends Category> categories, String[] names) {
        Set<String> set = new HashSet<>(List.of(names));
        int amount = 0;
        for (Category cat : categories.values()) {
            if (set.isEmpty() || set.contains(cat.getName())) {
                amount += cat.amount.amount();
            }
        }

        return amount;
    }
}
