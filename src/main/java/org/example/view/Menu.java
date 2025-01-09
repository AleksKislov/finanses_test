package org.example.view;

import lombok.extern.slf4j.Slf4j;
import org.example.application.Logger;
import org.example.application.UserService;
import org.example.domain.entities.Expense;
import org.example.domain.entities.Income;
import org.example.domain.entities.Money;
import org.example.domain.entities.WalletInfo;

import javax.security.auth.login.CredentialException;
import java.util.*;

@Slf4j
public class Menu {
    private final Scanner scanner = new Scanner(System.in);
    private final Logger logger = new Logger();
    private UserService userService;

    public void runMainMenu() {
        boolean running = true;

        while (running) {
            displayMainMenu();

            switch (scanner.nextLine()) {
                case "a":
                    authenticate("зарегистрированного", false);
                    break;
                case "b":
                    authenticate("нового", true);
                    break;
                case "c":
                    logger.logln("Завершаем работу...");
                    running = false;
                    break;
                default:
                    logger.invalidOption();
            }
        }
        scanner.close();
    }

    private void displayMainMenu() {
        logger.logln("\n=== Основное Меню ===");
        logger.logln("a. Логин");
        logger.logln("b. Регистрация");
        logger.logln("c. Завершить работу");
        logger.enterOptionLetter();
    }

    private void authenticate(String userType, boolean isRegister) {
        boolean success = false;

        while (true) {
            logger.logln("\n=== Вход для " + userType + " пользователя ===");
            logger.logln("a. Назад");
            logger.enterInput("Введите свое имя: ");

            String name = scanner.nextLine();
            if (name.equals("a")) break;

            logger.enterInput("Теперь введите свой пароль: ");

            String password = scanner.nextLine();

            try {
                this.userService = new UserService(name, password, isRegister);
                success = true;
                break;
            } catch (CredentialException e) {
                logger.error("Ошибка: " + e.getMessage());
            }
        }

        if (success) displayUserMenu();
    }

    private void displayUserMenu() {
        boolean running = true;


        while (running) {
        logger.displayUser(this.userService.getUser());
        logger.logln("a. Добавить категорию доходов");
        logger.logln("b. Добавить категорию расходов (опционально: и ее бюджет)");
        logger.logln("c. Отобразить информацию кошелька");
        logger.logln("d. Сделать перевод другому пользователю");
        logger.logln("e. Выход пользователя и сохраниние текущих данных");
        logger.enterOptionLetter();

            switch (scanner.nextLine()) {
                case "a":
                    addIncome();
                    break;
                case "b":
                    addExpense();
                    break;
                case "c":
                    displayWalletInfoMenu();
                    break;
                case "d":
                    transferMoney();
                    break;
                case "e":
                    logger.logln("Пользователь разлогинился, сохраняем данные...");
                    userService.saveWallet();
                    running = false;
                    break;
                default:
                    logger.invalidOption();
            }
        }
    }

    private void transferMoney() {
        logger.displayUser(this.userService.getUser());
        logger.logln("a. Назад");
        logger.enterInput("Введите имя другого пользователя: ");

        String userName = scanner.nextLine();
        if (userName.equals("a")) return;

        // проверяем, что перевод не самому себе
        if (userName.trim().equals(userService.getUser().name())) {
            logger.error("Нельзя переводить деньги самому себе");
            return;
        }

        logger.enterInput("Теперь введите сумму перевода: ");

        try {
            String sumStr = scanner.nextLine();

            int sum = Integer.parseInt(sumStr);
            if (sum <= 0) throw new Error();
            userService.transferMoney(userName, sum);

        } catch (NumberFormatException | CredentialException | IllegalStateException e) {
            if (e instanceof NumberFormatException) {
                logger.error("Сумма должна быть целым числом больше 0");
            } else {
                logger.error(e.getMessage());
            }
            return;
        }

        logger.logln("Перевод выполнен успешно");
    }

    private void displayWalletInfoMenu() {
        boolean running = true;

        while (running) {
            logger.displayUser(this.userService.getUser());
            logger.logln("a. Итого по доходам и расходам");
            logger.logln("b. Информация по всем категориям");
            logger.logln("c. Информация по выбранным категориям");
            logger.logln("d. Назад");
            logger.enterOptionLetter();

            switch (scanner.nextLine()) {
                case "a":
                    WalletInfo info = userService.getTotal();
                    logger.infoln("Общий доход: " + info.income());
                    logger.infoln("Общие расходы: " + info.expense());
                    logger.infoln("Итого в кошельке: " + info.total());
                    break;
                case "b":
                    Map<String, Income> incomes = userService.getIncomeInfo();
                    logger.infoln("== Все категории доходов ==");
                    for (Income income : incomes.values()) {
                        logger.infoln(income.getName() + ": " + income.getAmount().amount());
                    }
                    Map<String, Expense> expenses = userService.getExpenseInfo();
                    logger.infoln("== Все бюджеты ==");
                    for (Expense expense : expenses.values()) {
                        logger.infoln(expense.getName() + ": " + (expense.isUseBudget() ? expense.getBudget().amount() : "не установлен") );
                    }
                    logger.infoln("== Все категории расходов ==");
                    for (Expense expense : expenses.values()) {
                        logger.displayExpense(expense);
                    }
                    break;
                case "c":
                    logger.enterInput("Для вывода информации по определенным категориям введите их через пробел: ");
                    String categoryNames = scanner.nextLine();
                    String[] names = categoryNames.split(" ");
                    Set<String> set = userService.getNotExistingCategories(new HashSet<>(List.of(names)));
                    if (!set.isEmpty()) logger.error("Данных категорий не существует: " + set);

                    ArrayList<Income> incomesArr = userService.getSelectedIncomes(names);
                    if (!incomesArr.isEmpty()) {
                        logger.infoln("== Доходы по категориям ==");
                        for (Income income : incomesArr) {
                            logger.infoln(income.getName() + ": " + income.getAmount().amount());
                        }
                    }

                    ArrayList<Expense> expensesArr = userService.getSelectedExpenses(names);
                    if (!expensesArr.isEmpty()) {
                        logger.infoln("== Расходы по категориям ==");
                        for (Expense expense : expensesArr) {
                            logger.displayExpense(expense);
                        }
                    }

                    logger.infoln("== ИТОГО ==");
                    WalletInfo infoObj = userService.getTotalByCategories(names);
                    logger.infoln("Общий доход по категориям: " + infoObj.income());
                    logger.infoln("Общие расходы по категориям: " + infoObj.expense());
                    logger.infoln("Итого по категориям: " + infoObj.total());
                    break;
                case "d":
                    running = false;
                    break;
                default:
                    logger.invalidOption();
            }
        }
    }

    private void addIncome() {
        logger.displayUser(this.userService.getUser());
        logger.logln("a. Назад");
        logger.enterInput("Введите название категории доходов: ");

        String name = scanner.nextLine();
        if (name.equals("a")) return;
        if (name.trim().length() < 4) {
            logger.error("Название категории должно быть минимум 3 символа");
            return;
        }

        Income income = new Income(name);

        logger.enterInput("Теперь введите сумму дохода: ");

        try {
            String sumStr = scanner.nextLine();
            if (sumStr.equals("a")) return;

            int sum = Integer.parseInt(sumStr);
            if (sum <= 0) throw new NumberFormatException();
            income.add(new Money(sum));
        } catch (NumberFormatException e) {
            logger.error("Сумма должна быть целым числом больше 0");
            return;
        }

        userService.addIncome(income);
        logger.logln("Добавлена статья дохода");
    }

    private void addExpense() {

        logger.displayUser(this.userService.getUser());
        logger.logln("a. Закончить и вернуться назад");
        logger.enterInput("Введите название категории расходов: ");

        String name = scanner.nextLine();
        if (name.equals("a")) return;
        if (name.trim().length() < 4) {
            logger.error("Название категории должно быть минимум 3 символа");
            return;
        }

        Expense expense = new Expense(name);

        logger.enterInput("Теперь введите сумму расходов: ");

        try {
            String sumStr = scanner.nextLine();
            if (sumStr.equals("a")) return;

            int sum = Integer.parseInt(sumStr);
            if (sum <= 0) throw new NumberFormatException();
            expense.add(new Money(sum));
        } catch (NumberFormatException e) {
            logger.error("Сумма должна быть целым числом больше 0");
            return;
        }

        logger.enterInput("Установить/обновить бюджет для " + expense.getName() + "? Если нет, введите \"a\", если да - введите сумму ");

        String sumStr = scanner.nextLine();
        if (sumStr.equals("a")) {
            addExpenseEnd(expense);
            return;
        }

        try {
            int sum = Integer.parseInt(sumStr);

            if (sum <= 0) throw new NumberFormatException();
            expense.setBudget(new Money(sum));
        } catch (NumberFormatException e) {
            logger.error("Бюджет должен быть целым числом больше 0");
            return;
        }

        addExpenseEnd(expense);
    }

    private void addExpenseEnd(Expense expense) {
        try {
            userService.addExpense(expense);
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
            return;
        }

        logger.logln("Добавлена статья расходов");
        boolean budgetExceeded = userService.checkIfExceedBudget(expense.getName());
        if (budgetExceeded) logger.error("Превышен бюджет для " + expense.getName());
    }
}