package org.example.domain.repositories;

import com.google.gson.Gson;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.Optional;

public interface Repository<T> {
    JedisPool dbPool = new JedisPool();
    Gson objMapper = new Gson();

    void save(T value);
    void save(T value, String userName);
    void saveAll(Map<String, T> map);
    void saveAll(Map<String, T> map, String userName);
    Optional<T> find(String userName);
    Optional<T> find(String userName, String name);
    Optional<Map<String, T>> findAll();
    Optional<Map<String, T>> findAll(String userName);
}
