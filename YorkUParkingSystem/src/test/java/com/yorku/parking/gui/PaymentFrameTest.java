package com.yorku.parking.gui;

import com.yorku.parking.payment.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentFrameTest {
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_SPACE = "A1";
    private static final String TEST_ROLE = "student";
    private static final int TEST_HOURS = 2;
    private static final double TEST_AMOUNT = 10.0;
    private static final String PAYMENTS_FILE = "src/main/resources/payments.csv";
    private static final String TEMP_PAYMENTS_FILE = "src/main/resources/payments_test.csv";
    private PaymentProcessor paymentProcessor;

    @BeforeEach
    void setUp() {
        // Create backup of payments file
        try {
            if (Files.exists(Path.of(PAYMENTS_FILE))) {
                Files.copy(Path.of(PAYMENTS_FILE), Path.of(TEMP_PAYMENTS_FILE), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        paymentProcessor = new PaymentProcessor(TEST_USERNAME, TEST_SPACE, TEST_ROLE, TEST_HOURS, TEST_AMOUNT);
    }

    @AfterEach
    void tearDown() {
        // Restore payments file
        try {
            if (Files.exists(Path.of(TEMP_PAYMENTS_FILE))) {
                Files.move(Path.of(TEMP_PAYMENTS_FILE), Path.of(PAYMENTS_FILE), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ParameterizedTest
    @CsvSource({
        "student,5.0",
        "faculty,8.0",
        "nonfaculty,10.0",
        "visitor,15.0"
    })
    void testRateCalculation(String role, double expectedRate) {
        assertEquals(expectedRate, paymentProcessor.calculateRate(role));
    }

    @Test
    void testPaymentLogging() throws IOException {
        // Clear the file first
        new FileWriter(PAYMENTS_FILE).close();
        
        String testMethod = "Credit Card";
        String testNumber = "1234567890123456";
        
        paymentProcessor.logPayment(testMethod, testNumber);

        // Verify the payment was logged
        List<String> lines = Files.readAllLines(Path.of(PAYMENTS_FILE));
        assertFalse(lines.isEmpty());
        String lastLine = lines.get(lines.size() - 1);
        assertTrue(lastLine.contains(TEST_USERNAME));
        assertTrue(lastLine.contains(testMethod));
        assertTrue(lastLine.contains(testNumber));
        assertTrue(lastLine.contains(String.valueOf(TEST_AMOUNT)));
    }

    @Test
    void testInvalidCreditCardNumber() {
        String invalidCardNumber = "123";
        assertThrows(IllegalArgumentException.class, () -> 
            paymentProcessor.validateCreditCard(invalidCardNumber));
    }

    @Test
    void testValidCreditCardNumber() {
        String validCardNumber = "1234567890123456";
        assertDoesNotThrow(() -> 
            paymentProcessor.validateCreditCard(validCardNumber));
    }

    @Test
    void testPaymentCalculation() {
        double expectedAmount = TEST_HOURS * paymentProcessor.calculateRate(TEST_ROLE);
        assertEquals(expectedAmount, paymentProcessor.calculateTotalAmount());
    }
} 