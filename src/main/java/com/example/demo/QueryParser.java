package com.example.demo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {

    private final Map<String, List<String>> filters = new HashMap<>();

    public QueryParser(String query) {
        Pattern pattern = Pattern.compile("(path|content|filename):([^\\s]+)");
        Matcher matcher = pattern.matcher(query);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            filters.computeIfAbsent(key, k -> new ArrayList<>()).add(value.toLowerCase());
        }
    }

    public List<String> get(String key) {
        return filters.getOrDefault(key, Collections.emptyList());
    }

    public boolean has(String key) {
        return filters.containsKey(key);
    }
}
