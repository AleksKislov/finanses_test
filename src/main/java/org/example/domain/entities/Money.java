package org.example.domain.entities;

public record Money(int amount) {
    public Money {
        if (amount < 0) {
            throw new IllegalArgumentException("money amount should be positive or 0");
        }
    }

    public Money add(Money sum) {
        return new Money(this.amount + sum.amount());
    }

    public Money subtract(Money sum) {
        return new Money(this.amount - sum.amount());
    }

    public static Money zero() {
        return new Money(0);
    }
}
