package com.yorku.parking.models;

public class UserFactory {
    public static User createUser(String type) {
        switch (type.toUpperCase()) {
            case "STUDENT":
                return new Student();  // No need to cast
            case "FACULTY":
                return new Faculty();  // No need to cast
            default:
                throw new IllegalArgumentException("Invalid User Type: " + type);
        }
    }
}

