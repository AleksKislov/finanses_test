package org.example.domain.entities;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class Expense extends Category {
    private Money budget = Money.zero();
    private boolean useBudget = false;

    public Expense(String name) {
        super(name, CategoryType.EXPENSE);
    }

    public void setBudget(Money amount) {
        this.budget = amount;
        this.useBudget = true;
    }

//    public void unsetBudget() {
//        this.budget = Money.zero();
//        this.useBudget = false;
//    }
//
//    public void increaseBudget(Money sum) {
//        this.budget = this.budget.add(sum);
//    }
//
//    public void decreaseBudget(Money sum) {
//        this.budget = this.budget.subtract(sum);
//    }
}
