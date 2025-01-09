package org.example.domain.entities;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public abstract class Category {
    private final String name;
    protected Money amount = Money.zero();
    private final CategoryType type;

    protected Category(String name, CategoryType type) {
        String n = name.trim().toLowerCase();
        if (n.length() < 3) {
            throw new IllegalArgumentException("category name should be at least 3 characters");
        }

        this.name = n;
        this.type = type;
    }

    public void add(Money sum) {
        this.amount = this.amount.add(sum);
    }
}
