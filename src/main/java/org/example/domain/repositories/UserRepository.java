package org.example.domain.repositories;

import org.example.domain.entities.User;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class UserRepository implements Repository<User> {
    private final String KEY_PREFIX = "user:";

    private String getKey(String userName) {
        return KEY_PREFIX + userName;
    }

    private String getUserNameFromKey(String key) {
        return key.replace(KEY_PREFIX, "");
    }

    public void save(User user) {
        try (Jedis jedis = dbPool.getResource()) {
            String key = getKey(user.name());
            jedis.set(key, objMapper.toJson(user));
        } catch (Exception e) {
            throw new RuntimeException("Error saving user to db", e);
        }

    }

    @Override
    public void save(User value, String userName) {

    }

    @Override
    public void saveAll(Map<String, User> map, String userName) {

    }

    @Override
    public void saveAll(Map<String, User> users) {
//        for (User user : users.values()) {
//            save(user);
//        }
    }

    @Override
    public Optional<User> find(String name) {
        try (Jedis jedis = dbPool.getResource()) {
            String json = jedis.get(getKey(name));
            if (json != null) return Optional.of(objMapper.fromJson(json, User.class));
        } catch (Exception e) {
            throw new RuntimeException("Error finding user by name", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> find(String userName, String name) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, User>> findAll() {
        try (Jedis jedis = dbPool.getResource()) {
            String pattern = KEY_PREFIX + "*";
            Set<String> keys = jedis.keys(pattern);

            Map<String, User> users = new HashMap<>();
            for (String key : keys) {
                Optional<User> user = find(getUserNameFromKey(key));
                if (user.isEmpty()) continue;
                users.put(user.get().name(), user.get());
            }
            return Optional.of(users);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error getting all users from db", e);
        }
    }

    @Override
    public Optional<Map<String, User>> findAll(String userName) {
        return Optional.empty();
    }
}
