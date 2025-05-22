package com.example.demo;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SpellService {
    private SpellStrategy strategy;
    public SpellService(SpellStrategy strategy) {
        this.strategy = strategy;
    }
    public String correctQuery(String query) {
        return Arrays.stream(query.split("\\s+")).map(this::correct).collect(Collectors.joining(" "));
    }
    public String correct(String query) {
        if(query.contains(":")) {
            String[] split = query.split(":",2);
            String key =strategy.correct(split[0]);
            String value = strategy.correct(split[1]);
            return key + ":" + value;
        }else return strategy.correct(query);

    }
}
