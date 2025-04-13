package com.yorku.parking.gui;

import org.junit.jupiter.api.*;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PaymentLogicTest {
    private static final String TEST_PAYMENTS_FILE = "src/main/resources/payments.csv";
    private static final String BACKUP_PAYMENTS = "src/main/resources/payments_backup.csv";
    private static final String TEST_RATES_FILE = "src/main/resources/rates.csv";
    private static final String BACKUP_RATES = "src/main/resources/rates_backup.csv";

    @BeforeEach
    void setUp() throws IOException {
        // Backup and create test files
        backupAndCreateTestFile(TEST_PAYMENTS_FILE, BACKUP_PAYMENTS);
        backupAndCreateTestFile(TEST_RATES_FILE, BACKUP_RATES, "5,8,10,15");
    }

    @AfterEach
    void tearDown() throws IOException {
        // Restore original files
        restoreFile(BACKUP_PAYMENTS, TEST_PAYMENTS_FILE);
        restoreFile(BACKUP_RATES, TEST_RATES_FILE);
    }

    @ParameterizedTest
    @CsvSource({
        "student,2,10.0",
        "faculty,2,16.0",
        "nonfaculty,2,20.0",
        "visitor,2,30.0"
    })
    void testPaymentCalculation(String role, int hours, double expectedAmount) throws IOException {
        double amount = calculatePayment(role, hours);
        assertEquals(expectedAmount, amount);
    }

    @Test
    void testInvalidRole() {
        assertThrows(IllegalArgumentException.class, () -> 
            calculatePayment("invalid_role", 2));
    }

    @Test
    void testZeroHours() {
        assertThrows(IllegalArgumentException.class, () -> 
            calculatePayment("student", 0));
    }

    @Test
    void testNegativeHours() {
        assertThrows(IllegalArgumentException.class, () -> 
            calculatePayment("student", -1));
    }

    @Test
    void testPaymentLogging() throws IOException {
        String username = "testUser";
        String space = "A1";
        String role = "student";
        double amount = 10.0;
        String paymentMethod = "Credit Card";
        String paymentDetails = "4111111111111111";

        logPayment(username, space, role, amount, paymentMethod, paymentDetails);

        List<String> lines = Files.readAllLines(Path.of(TEST_PAYMENTS_FILE));
        String lastPayment = lines.get(lines.size() - 1);

        assertTrue(lastPayment.contains(username));
        assertTrue(lastPayment.contains(space));
        assertTrue(lastPayment.contains(role));
        assertTrue(lastPayment.contains(String.valueOf(amount)));
        assertTrue(lastPayment.contains(paymentMethod));
        assertTrue(lastPayment.contains(paymentDetails));
    }

    @Test
    void testCreditCardValidation() {
        // Valid card numbers
        assertDoesNotThrow(() -> validateCreditCard("4111111111111111"));
        assertDoesNotThrow(() -> validateCreditCard("5555555555554444"));
        
        // Invalid card numbers
        assertThrows(IllegalArgumentException.class, () -> validateCreditCard("411"));
        assertThrows(IllegalArgumentException.class, () -> validateCreditCard("abcd1234efgh5678"));
        assertThrows(IllegalArgumentException.class, () -> validateCreditCard(""));
        assertThrows(IllegalArgumentException.class, () -> validateCreditCard(null));
    }

    private double calculatePayment(String role, int hours) throws IOException {
        if (hours <= 0) {
            throw new IllegalArgumentException("Hours must be positive");
        }

        String rates = Files.readString(Path.of(TEST_RATES_FILE));
        String[] rateArray = rates.split(",");
        
        double rate = switch (role.toLowerCase()) {
            case "student" -> Double.parseDouble(rateArray[0]);
            case "faculty" -> Double.parseDouble(rateArray[1]);
            case "nonfaculty" -> Double.parseDouble(rateArray[2]);
            case "visitor" -> Double.parseDouble(rateArray[3]);
            default -> throw new IllegalArgumentException("Invalid role");
        };

        return rate * hours;
    }

    private void logPayment(String username, String space, String role, double amount, 
                          String paymentMethod, String paymentDetails) throws IOException {
        String timestamp = LocalDateTime.now().toString();
        String paymentRecord = String.format("%s,%s,%s,%s,%s,%.2f,%s%n",
            timestamp, username, space, role, paymentMethod, amount, paymentDetails);
        
        Files.write(Path.of(TEST_PAYMENTS_FILE), 
                   paymentRecord.getBytes(), 
                   StandardOpenOption.CREATE, 
                   StandardOpenOption.APPEND);
    }

    private void validateCreditCard(String cardNumber) {
        if (cardNumber == null || 
            cardNumber.length() != 16 || 
            !cardNumber.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid credit card number");
        }
    }

    private void backupAndCreateTestFile(String original, String backup, String... contents) throws IOException {
        if (Files.exists(Path.of(original))) {
            Files.copy(Path.of(original), Path.of(backup), StandardCopyOption.REPLACE_EXISTING);
        }
        if (contents.length > 0) {
            Files.write(Path.of(original), String.join("\n", contents).getBytes());
        } else {
            new FileWriter(original).close(); // Create empty file
        }
    }

    private void restoreFile(String backup, String original) throws IOException {
        if (Files.exists(Path.of(backup))) {
            Files.move(Path.of(backup), Path.of(original), StandardCopyOption.REPLACE_EXISTING);
        }
    }
} 