package com.yorku.parking.utils;

import java.util.HashSet;
import java.util.Set;

public class SessionManager {
    private static final Set<String> activeUsers = new HashSet<>();

    public static boolean isLoggedIn(String username) {
        return activeUsers.contains(username);
    }

    public static void login(String username) {
        activeUsers.add(username);
    }

    public static void logout(String username) {
        activeUsers.remove(username);
    }
}