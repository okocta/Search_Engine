package com.example.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchProxy implements Search {
    private RealSearch realSearch;
    private Map<String,List<TextFile>> cache = new HashMap<>();
    public SearchProxy(RealSearch realSearch) {
        this.realSearch = realSearch;
    }

    @Override
    public List<TextFile> search(String query) {
        if(cache.containsKey(query.toLowerCase())) {
            System.out.println("Returned from cache");
            return cache.get(query.toLowerCase());
        }

        List<TextFile> result = realSearch.search(query);
        cache.put(query.toLowerCase(), result);
        return result;
    }
}
