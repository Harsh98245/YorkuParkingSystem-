package com.yorku.parking.payment;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_SPACE = "A1";
    private static final String TEST_ROLE = "student";
    private static final int TEST_HOURS = 3;
    private static final double TEST_AMOUNT = 15.0;
    private static final String PAYMENTS_FILE = "src/main/resources/payments.csv";
    private static final String BACKUP_PAYMENTS = "src/main/resources/payments_backup.csv";
    
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;
    private PaymentProcessor paymentProcessor;

    @BeforeEach
    void setUp() throws IOException {
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        
        // Backup payments file
        backupFile(PAYMENTS_FILE, BACKUP_PAYMENTS);
        
        // Create empty payments file if it doesn't exist
        if (!Files.exists(Path.of(PAYMENTS_FILE))) {
            Files.createFile(Path.of(PAYMENTS_FILE));
        }
        
        paymentProcessor = new PaymentProcessor(TEST_USERNAME, TEST_SPACE, TEST_ROLE, TEST_HOURS, TEST_AMOUNT);
    }

    @AfterEach
    void tearDown() throws IOException {
        System.setOut(originalOut);
        outContent.reset();
        
        // Restore payments file
        restoreFile(BACKUP_PAYMENTS, PAYMENTS_FILE);
    }

    // Test data providers
    static Stream<Arguments> paymentTestData() {
        return Stream.of(
            // type, strategy, expectedOutput, amount, expectedAmount
            Arguments.of(
                "Credit Card",
                new CreditCardPayment("1234567890123456", "12/25", "123"),
                "Processing credit card payment",
                50.0,
                "$50.0"
            ),
            Arguments.of(
                "Mobile",
                new MobilePayment("1234567890", "1234"),
                "Processing mobile payment",
                25.50,
                "$25.5"
            ),
            Arguments.of(
                "Credit Card",
                new CreditCardPayment("1234567890123456", "12/25", "123"),
                "Processing credit card payment",
                0.0,
                "$0.0"
            ),
            Arguments.of(
                "Mobile",
                new MobilePayment("1234567890", "1234"),
                "Processing mobile payment",
                1000.0,
                "$1000.0"
            )
        );
    }

    // Payment Strategy Tests
    @ParameterizedTest
    @MethodSource("paymentTestData")
    void testPaymentProcessing(String type, PaymentStrategy strategy, String expectedOutput, 
                             double amount, String expectedAmount) {
        PaymentContext context = new PaymentContext(strategy);
        assertTrue(context.processPayment(amount));
        String output = outContent.toString();
        assertTrue(output.contains(expectedOutput));
        assertTrue(output.contains(expectedAmount));
    }

    @Test
    void testMultiplePayments() {
        PaymentStrategy strategy = new CreditCardPayment("1234567890123456", "12/25", "123");
        PaymentContext context = new PaymentContext(strategy);
        
        context.processPayment(10.0);
        context.processPayment(20.0);
        
        String output = outContent.toString();
        assertTrue(output.contains("$10.0"));
        assertTrue(output.contains("$20.0"));
    }

    // Payment Rate Tests
    @ParameterizedTest
    @CsvSource({
        "student,5.0",
        "faculty,8.0",
        "nonfaculty,10.0",
        "visitor,15.0"
    })
    void testParkingRatesForDifferentRoles(String role, double expectedRate) {
        assertEquals(expectedRate, paymentProcessor.calculateRate(role));
    }

    @Test
    void testInvalidRoleThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            paymentProcessor.calculateRate("invalid_role"));
    }

    @Test
    void testTotalAmountCalculation() {
        // For student rate (5.0) and 3 hours
        double expectedTotal = 15.0; // 5.0 * 3
        assertEquals(expectedTotal, paymentProcessor.calculateTotalAmount());
    }

    // Payment Validation Tests
    @ParameterizedTest
    @ValueSource(strings = {
        "123", // too short
        "12345678901234567", // too long
        "123abc4567890123", // contains letters
        "" // empty
    })
    void testInvalidCreditCardNumbers(String invalidCard) {
        assertThrows(IllegalArgumentException.class, () -> 
            paymentProcessor.validateCreditCard(invalidCard));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1234567890123456",
        "4111111111111111",
        "5555555555554444"
    })
    void testValidCreditCardNumbers(String validCard) {
        assertDoesNotThrow(() -> 
            paymentProcessor.validateCreditCard(validCard));
    }

    // Payment Logging Tests
    @Test
    void testPaymentLogging() throws IOException {
        String testMethod = "Credit Card";
        String testCardNumber = "4111111111111111";
        
        paymentProcessor.logPayment(testMethod, testCardNumber);

        List<String> lines = Files.readAllLines(Path.of(PAYMENTS_FILE));
        assertFalse(lines.isEmpty(), "Payment log should not be empty");
        
        String lastPayment = lines.get(lines.size() - 1);
        assertTrue(lastPayment.contains(TEST_USERNAME), "Payment should contain username");
        assertTrue(lastPayment.contains(TEST_SPACE), "Payment should contain parking space");
        assertTrue(lastPayment.contains(TEST_ROLE), "Payment should contain role");
        assertTrue(lastPayment.contains(testMethod), "Payment should contain payment method");
        assertTrue(lastPayment.contains(String.valueOf(TEST_AMOUNT)), "Payment should contain amount");
        assertTrue(lastPayment.contains(testCardNumber), "Payment should contain payment details");
    }

    

    @Test
    void testPaymentLoggingWithSpecialCharacters() throws IOException {
        String paymentMethod = "PayPal";
        String details = "user@email.com,with,commas";
        
        paymentProcessor.logPayment(paymentMethod, details);
        
        List<String> lines = Files.readAllLines(Path.of(PAYMENTS_FILE));
        String lastLine = lines.get(lines.size() - 1);
        
        assertTrue(lastLine.contains(paymentMethod));
        assertTrue(lastLine.contains(details));
    }

    // Helper methods
    private void backupFile(String source, String backup) throws IOException {
        if (Files.exists(Path.of(source))) {
            Files.copy(Path.of(source), Path.of(backup), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void restoreFile(String backup, String original) throws IOException {
        if (Files.exists(Path.of(backup))) {
            Files.move(Path.of(backup), Path.of(original), StandardCopyOption.REPLACE_EXISTING);
        }
    }
} 