package com.yorku.parking.utils;

import java.io.*;
import java.util.*;

public class AccountGeneratorUtil {
    private static final String USER_FILE = "src/main/resources/users.csv";
    private static final Random random = new Random();

    public static String[] generateManagerAccount() throws IOException {
        String username;
        do {
            username = "manager_" + (1000 + random.nextInt(9000));
        } while (isUsernameTaken(username));

        String password = generateStrongPassword(10);
        saveAccount(username, password);
        return new String[]{username, password};
    }

    private static boolean isUsernameTaken(String username) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.split(",")[0].equals(username)) return true;
            }
        }
        return false;
    }

    private static void saveAccount(String username, String password) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(USER_FILE, true))) {
            out.println(username + "," + password + ",Manager");
        }
    }

    private static String generateStrongPassword(int length) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String symbols = "!@#$%^&*()-_+=<>?";
        String all = upper + lower + digits + symbols;

        StringBuilder password = new StringBuilder();
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(symbols.charAt(random.nextInt(symbols.length())));

        for (int i = 4; i < length; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }
        return password.toString();
    }
}