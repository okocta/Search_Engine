package com.example.demo;

import java.util.ArrayList;
import java.util.List;

public class LoggingListener implements Observer{

    private final List<String> history = new ArrayList<>();
    @Override
    public void update(String query) {
        history.add(query.toLowerCase());
    }
    public List<String> getHistory(int limit) {
        return history.subList(Math.max(0, history.size()- limit), history.size());
    }
    public long getCountTerm(String term) {
        return history.stream()
                .filter(q -> q.contains(term.toLowerCase()))
                .count();

    }

}
