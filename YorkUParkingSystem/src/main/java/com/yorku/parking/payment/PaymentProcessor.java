package com.yorku.parking.payment;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentProcessor {
    private final String username;
    private final String parkingSpace;
    private final String role;
    private final int hours;
    private final double amount;
    private static final String PAYMENTS_FILE = "src/main/resources/payments.csv";

    public PaymentProcessor(String username, String parkingSpace, String role, int hours, double amount) {
        this.username = username;
        this.parkingSpace = parkingSpace;
        this.role = role;
        this.hours = hours;
        this.amount = amount;
    }

    public double calculateRate(String role) {
        return switch (role.toLowerCase()) {
            case "student" -> 5.0;
            case "faculty" -> 8.0;
            case "nonfaculty" -> 10.0;
            case "visitor" -> 15.0;
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };
    }

    public double calculateTotalAmount() {
        return hours * calculateRate(role);
    }

    public void validateCreditCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16 || !cardNumber.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid credit card number");
        }
    }

    public void logPayment(String paymentMethod, String paymentDetails) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        String paymentRecord = String.format("%s,%s,%s,%s,%s,%.2f,%s%n",
            timestamp,
            username,
            parkingSpace,
            role,
            paymentMethod,
            amount,
            paymentDetails
        );

        Files.write(
            Path.of(PAYMENTS_FILE),
            paymentRecord.getBytes(),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        );
    }
} 