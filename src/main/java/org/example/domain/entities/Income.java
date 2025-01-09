package org.example.domain.entities;

import lombok.ToString;

@ToString(callSuper = true)
public class Income extends Category {
    public Income(String name) {
        super(name, CategoryType.INCOME);
    }
}
