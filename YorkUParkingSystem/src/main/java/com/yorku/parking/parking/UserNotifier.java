package com.yorku.parking.parking;

public class UserNotifier implements Observer {
    private String name;

    public UserNotifier(String name) {
        this.name = name;
    }

    @Override
    public void update(String status) {
        System.out.println(name + " notified: " + status);
    }
}