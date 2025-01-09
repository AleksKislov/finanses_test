package org.example.application;

import lombok.Getter;
import org.example.domain.entities.*;
import org.example.domain.repositories.ExpenseRepository;
import org.example.domain.repositories.IncomeRepository;
import org.example.domain.repositories.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import javax.security.auth.login.CredentialException;
import java.util.*;

public class UserService {
    private final UserRepository userRepo = new UserRepository();
    private final ExpenseRepository expenseRepo = new ExpenseRepository();
    private final IncomeRepository incomeRepo = new IncomeRepository();

    @Getter
    private final User user;
    private final Wallet wallet;

    public UserService(String userName, String password, boolean isRegister) throws CredentialException {
        this.user = isRegister ? register(userName, password) : authenticate(userName, password);
        this.wallet = loadWallet();
    }

    private User authenticate(String userName, String password) throws CredentialException {
        Optional<User> user = userRepo.find(userName);
        if (user.isEmpty()) {
            throw new CredentialException("User name not found");
        }

        if (!BCrypt.checkpw(password, user.get().password())) {
            throw new CredentialException("Password is not correct");
        }

        return user.get();
    }

    private User register(String userName, String password) throws CredentialException {
        User user = new User(userName, BCrypt.hashpw(password, BCrypt.gensalt()));
        Optional<User> existing = userRepo.find(user.name());
        if (existing.isPresent()) throw new CredentialException("Пользователь с таким именем уже существует!");
        userRepo.save(user);
        return user;
    }

    private Wallet loadWallet() {
        Optional<Map<String, Expense>> expenses = expenseRepo.findAll(user.name());
        Optional<Map<String, Income>> incomes = incomeRepo.findAll(user.name());
        return new Wallet(expenses.orElseGet(HashMap::new), incomes.orElseGet(HashMap::new));
    }

    public void saveWallet() {
        String userName = this.user.name();
        expenseRepo.saveAll(wallet.getAllExpenses(), userName);
        incomeRepo.saveAll(wallet.getAllIncome(), userName);
    }

    public void addExpense(Expense expense) throws IllegalStateException {
        // проверить что достаточно денег
        int currentIncome = getTotalIncome();
        if (currentIncome < expense.getAmount().amount()) {
            throw new IllegalStateException("На счету не достаточно средств для таких расходов");
        }

        // все ок, добавляем расходы
        this.wallet.addExpense(expense);
    }

    public boolean checkIfExceedBudget(String name) {
        Expense expense = this.wallet.getAllExpenses().get(name);
        if (!expense.isUseBudget()) return false;

        int newSum = expense.getBudget().amount() - expense.getAmount().amount();
        return newSum < 0;
    }

    public void addIncome(Income income) {
        this.wallet.addIncome(income);
    }

    public WalletInfo getTotal() {
        int allIncome = getTotalIncome();
        int allExpense = this.wallet.getExpensesAmount(new String[0]);

        int total = allIncome - allExpense;
        return new WalletInfo(allIncome, allExpense, total);
    }

    public int getTotalIncome() {
        return this.wallet.getIncomeAmount(new String[0]);
    }

    public WalletInfo getTotalByCategories(String[] names) {
        int allIncome = this.wallet.getIncomeAmount(names);
        int allExpense = this.wallet.getExpensesAmount(names);

        int total = allIncome - allExpense;
        return new WalletInfo(allIncome, allExpense, total);
    }

    public Map<String, Income> getIncomeInfo() {
        return this.wallet.getAllIncome();
    }

    public Map<String, Expense> getExpenseInfo() {
        return this.wallet.getAllExpenses();
    }

    public ArrayList<Income> getSelectedIncomes(String[] names) {
        ArrayList<Income> arr = new ArrayList<>();
        for (String name : names) {
            Income income = this.wallet.getAllIncome().get(name);
            if (income != null) arr.add(income);
        }
        return arr;
    }

    public ArrayList<Expense> getSelectedExpenses(String[] names) {
        ArrayList<Expense> arr = new ArrayList<>();
        for (String name : names) {
            Expense expense = this.wallet.getAllExpenses().get(name);
            if (expense != null) arr.add(expense);
        }
        return arr;
    }

    public Set<String> getNotExistingCategories(Set<String> names) {
        names.removeIf(name -> this.wallet.getAllExpenses().containsKey(name));
        names.removeIf(name -> this.wallet.getAllIncome().containsKey(name));
        return names;
    }

    public void transferMoney(String toUser, int sum) throws CredentialException {
        // получить пользователя
        Optional<User> anotherUser = userRepo.find(toUser);
        if (anotherUser.isEmpty()) throw new CredentialException("User name not found");

        String categoryName = "transfer";

        Money money = new Money(sum);
        Expense expense = new Expense(categoryName);
        expense.add(money);
        addExpense(expense);

        Income income = new Income(categoryName);
        income.add(money);
        incomeRepo.save(income, anotherUser.get().name());
    }
}
