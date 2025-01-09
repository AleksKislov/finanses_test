package org.example.domain.entities;

public record User(String name, String password) {

    public User(String name, String password) {
        if (name.length() < 2) {
            throw new IllegalArgumentException("user name should be at least 2 characters " + name);
        }
        if (password.length() < 2) {
            throw new IllegalArgumentException("password should be at least 5 characters");
        }

        this.name = name.trim();
        this.password = password.trim();
    }
}
