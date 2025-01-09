package org.example.domain.repositories;

import org.example.domain.entities.Income;
import org.example.domain.entities.Income;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class IncomeRepository implements Repository<Income> {
    private final String KEY_PREFIX = "income:";

    private String getKey(String userName, String name) {
        return KEY_PREFIX + userName + ":" + name;
    }

    private String getNameFromKey(String key, String userName) {
        return key.replace(KEY_PREFIX + userName + ":", "");
    }

    public void save(Income income) {
    }

    @Override
    public void save(Income income, String userName) {
        try (Jedis jedis = dbPool.getResource()) {
            String key = getKey(userName, income.getName());
            jedis.set(key, objMapper.toJson(income));
        } catch (Exception e) {
            throw new RuntimeException("Error saving " + income.getName() + " to db", e);
        }
    }

    @Override
    public void saveAll(Map<String, Income> incomes) {
//        for (Income income : incomes.values()) {
//            save(income);
//        }
    }

    @Override
    public void saveAll(Map<String, Income> incomes, String userName) {
        for (Income income : incomes.values()) {
            save(income, userName);
        }
    }

    @Override
    public Optional<Income> find(String userName) {
        return Optional.empty();
    }

    @Override
    public Optional<Income> find(String userName, String name) {
        try (Jedis jedis = dbPool.getResource()) {
            String json = jedis.get(getKey(userName, name));
            return Optional.of(objMapper.fromJson(json, Income.class));
        } catch (Exception e) {
            throw new RuntimeException("Error finding income" + name + " for user " + userName, e);
        }
    }

    @Override
    public Optional<Map<String, Income>> findAll() {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Income>> findAll(String userName) {
        try (Jedis jedis = dbPool.getResource()) {
            String pattern = KEY_PREFIX + userName + ":" + "*";
            Set<String> keys = jedis.keys(pattern);

            Map<String, Income> incomes = new HashMap<>();
            for (String key : keys) {
                Optional<Income> income = find(userName, getNameFromKey(key, userName));
                if (income.isEmpty()) continue;
                incomes.put(income.get().getName(), income.get());
            }
            return Optional.of(incomes);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error getting all incomes from db for user " + userName, e);
        }
    }
}
