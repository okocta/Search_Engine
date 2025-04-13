package com.example.demo;

import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private final List<Observer> observerList = new ArrayList<>();
    public void addObserver(Observer observer) {
        observerList.add(observer);
    }
    public void removeObserver(Observer observer) {
        observerList.remove(observer);
    }
    public void notifyObservers(String query) {
        for (Observer observer : observerList) {
                observer.update(query);
        }
    }

}
