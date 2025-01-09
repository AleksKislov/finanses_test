package org.example.domain.repositories;

import org.example.domain.entities.Expense;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ExpenseRepository implements Repository<Expense> {
    private final String KEY_PREFIX = "expense:";

    private String getKey(String userName, String name) {
        return KEY_PREFIX + userName + ":" + name;
    }

    private String getNameFromKey(String key, String userName) {
        return key.replace(KEY_PREFIX + userName + ":", "");
    }

    public void save(Expense expense) {

    }

    @Override
    public void save(Expense expense, String userName) {
        try (Jedis jedis = dbPool.getResource()) {
            String key = getKey(userName, expense.getName());
            jedis.set(key, objMapper.toJson(expense));
        } catch (Exception e) {
            throw new RuntimeException("Error saving " + expense.getName() + " to db", e);
        }
    }

    @Override
    public void saveAll(Map<String, Expense> expenses) {
//        for (Expense expense : expenses.values()) {
//            save(expense);
//        }
    }

    @Override
    public void saveAll(Map<String, Expense> expenses, String userName) {
        for (Expense expense : expenses.values()) {
            save(expense, userName);
        }
    }

    @Override
    public Optional<Expense> find(String userName) {
        return Optional.empty();
    }

    @Override
    public Optional<Expense> find(String userName, String name) {
        try (Jedis jedis = dbPool.getResource()) {
            String json = jedis.get(getKey(userName, name));
            return Optional.of(objMapper.fromJson(json, Expense.class));
        } catch (Exception e) {
            throw new RuntimeException("Error finding expense" + name + " for user " + userName, e);
        }
    }

    @Override
    public Optional<Map<String, Expense>> findAll() {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Expense>> findAll(String userName) {
        try (Jedis jedis = dbPool.getResource()) {
            String pattern = KEY_PREFIX + userName + ":" + "*";
            Set<String> keys = jedis.keys(pattern);

            Map<String, Expense> expenses = new HashMap<>();
            for (String key : keys) {
                Optional<Expense> expense = find(userName, getNameFromKey(key, userName));
                if (expense.isEmpty()) continue;
                expenses.put(expense.get().getName(), expense.get());
            }
            return Optional.of(expenses);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error getting all expenses from db for user " + userName, e);
        }
    }
}
