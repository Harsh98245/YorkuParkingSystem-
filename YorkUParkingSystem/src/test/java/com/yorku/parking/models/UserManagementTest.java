package com.yorku.parking.models;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import com.yorku.parking.utils.AccountGeneratorUtil;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class UserManagementTest {
    private static final String TEST_USERS_FILE = "src/main/resources/users.csv";
    private static final String TEST_RATES_FILE = "src/main/resources/rates.csv";
    private static final String BACKUP_USERS = "src/main/resources/users_backup.csv";
    private static final String BACKUP_RATES = "src/main/resources/rates_backup.csv";

    @BeforeEach
    void setUp() throws IOException {
        // Backup and create test files
        backupAndCreateTestFile(TEST_USERS_FILE, BACKUP_USERS, 
            "user1,pass1,Student,enabled",
            "user2,pass2,Faculty,enabled",
            "user3,pass3,Student,disabled",
            "manager_1001,password123,Manager,enabled");
            
        backupAndCreateTestFile(TEST_RATES_FILE, BACKUP_RATES, "5,8,10,15");
    }

    @AfterEach
    void tearDown() throws IOException {
        // Restore original files
        restoreFile(BACKUP_USERS, TEST_USERS_FILE);
        restoreFile(BACKUP_RATES, TEST_RATES_FILE);
    }

    // User Factory Tests
    static Stream<Arguments> validUserTypes() {
        return Stream.of(
            Arguments.of("STUDENT", Student.class, 5.0),
            Arguments.of("student", Student.class, 5.0),
            Arguments.of("FACULTY", Faculty.class, 8.0),
            Arguments.of("FaCuLtY", Faculty.class, 8.0)
        );
    }

    @ParameterizedTest
    @MethodSource("validUserTypes")
    void testValidUserCreation(String type, Class<?> expectedClass, double expectedRate) {
        User user = UserFactory.createUser(type);
        assertNotNull(user);
        assertTrue(expectedClass.isInstance(user));
        assertEquals(expectedRate, user.getParkingRate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "INVALID"})
    void testInvalidUserTypes(String type) {
        assertThrows(IllegalArgumentException.class, () -> UserFactory.createUser(type));
    }

    @Test
    void testNullUserType() {
        assertThrows(NullPointerException.class, () -> UserFactory.createUser(null));
    }

    // Account Generation Tests
    @Test
    void testGenerateManagerAccount() throws IOException {
        String[] account = AccountGeneratorUtil.generateManagerAccount();
        assertNotNull(account);
        assertEquals(2, account.length);
        
        // Verify username format
        assertTrue(account[0].startsWith("manager_"));
        assertTrue(account[0].substring(8).matches("\\d{4}"));
        
        // Verify password strength
        assertTrue(account[1].length() >= 10);
        assertTrue(account[1].matches(".*[A-Z].*")); // Contains uppercase
        assertTrue(account[1].matches(".*[a-z].*")); // Contains lowercase
        assertTrue(account[1].matches(".*\\d.*")); // Contains digit
        assertTrue(account[1].matches(".*[!@#$%^&*()\\-_+=<>?].*")); // Contains symbol
        
        // Verify account was saved
        List<String> lines = Files.readAllLines(Path.of(TEST_USERS_FILE));
        assertTrue(lines.stream().anyMatch(line -> 
            line.startsWith(account[0] + "," + account[1])));
    }

    @Test
    void testGenerateUniqueUsername() throws IOException {
        String[] account1 = AccountGeneratorUtil.generateManagerAccount();
        String[] account2 = AccountGeneratorUtil.generateManagerAccount();
        assertNotEquals(account1[0], account2[0]);
    }

    @Test
    void testPasswordStrength() throws IOException {
        String[] account = AccountGeneratorUtil.generateManagerAccount();
        String password = account[1];
        
        // Test minimum length and character types
        assertTrue(password.length() >= 10);
        assertTrue(password.matches(".*[A-Z].*"), "Password should contain uppercase");
        assertTrue(password.matches(".*[a-z].*"), "Password should contain lowercase");
        assertTrue(password.matches(".*\\d.*"), "Password should contain digit");
        assertTrue(password.matches(".*[!@#$%^&*()\\-_+=<>?].*"), "Password should contain symbol");
    }

    @Test
    void testAccountSavedWithManagerRole() throws IOException {
        String[] account = AccountGeneratorUtil.generateManagerAccount();
        List<String> lines = Files.readAllLines(Path.of(TEST_USERS_FILE));
        Optional<String> savedAccount = lines.stream()
            .filter(line -> line.startsWith(account[0] + "," + account[1]))
            .findFirst();
        
        assertTrue(savedAccount.isPresent());
        assertTrue(savedAccount.get().contains(",Manager,"));
    }

    // User Management Tests
    @Test
    void testUserManagement() throws IOException {
        // Test user deletion
        deleteUser("user1");
        List<String> lines = Files.readAllLines(Path.of(TEST_USERS_FILE));
        assertFalse(lines.stream().anyMatch(line -> line.startsWith("user1,")));
        assertTrue(lines.stream().anyMatch(line -> line.startsWith("user2,")));

        // Test status update
        updateUserStatus("user2", "disabled");
        lines = Files.readAllLines(Path.of(TEST_USERS_FILE));
        assertTrue(lines.stream()
            .filter(line -> line.startsWith("user2,"))
            .findFirst()
            .orElse("")
            .endsWith(",disabled"));
    }

    @ParameterizedTest
    @CsvSource({
        "6,9,11,16,true",
        "6,9,11,invalid,false",
        "6,9,11,false",
        "6,9,false"
    })
    void testRatesUpdate(String rates, boolean isValid) {
        if (isValid) {
            assertDoesNotThrow(() -> updateRates(rates));
            try {
                assertEquals(rates, Files.readString(Path.of(TEST_RATES_FILE)).trim());
            } catch (IOException e) {
                fail("Failed to read rates file");
            }
        } else {
            assertThrows(IllegalArgumentException.class, () -> updateRates(rates));
        }
    }

    @Test
    void testManagerAccountGeneration() throws IOException {
        int initialCount = Files.readAllLines(Path.of(TEST_USERS_FILE)).size();
        generateManagerAccounts(2);
        
        List<String> lines = Files.readAllLines(Path.of(TEST_USERS_FILE));
        assertEquals(initialCount + 2, lines.size());
        
        long newManagerCount = lines.stream()
            .filter(line -> line.split(",")[2].equals("Manager"))
            .count();
        assertEquals(2, newManagerCount);
    }

    // Helper Methods
    private void deleteUser(String username) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(TEST_USERS_FILE));
        List<String> updatedLines = lines.stream()
            .filter(line -> !line.startsWith(username + ","))
            .toList();
        Files.write(Path.of(TEST_USERS_FILE), updatedLines);
    }

    private void updateUserStatus(String username, String status) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(TEST_USERS_FILE));
        List<String> updatedLines = lines.stream()
            .map(line -> {
                if (line.startsWith(username + ",")) {
                    String[] parts = line.split(",");
                    return parts[0] + "," + parts[1] + "," + parts[2] + "," + status;
                }
                return line;
            })
            .toList();
        Files.write(Path.of(TEST_USERS_FILE), updatedLines);
    }

    private void updateRates(String rates) throws IOException {
        if (!rates.matches("\\d+(,\\d+){3}")) {
            throw new IllegalArgumentException("Invalid rates format");
        }
        Files.writeString(Path.of(TEST_RATES_FILE), rates);
    }

    private void generateManagerAccounts(int count) throws IOException {
        List<String> newAccounts = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            String username = "manager" + System.currentTimeMillis() + i;
            String password = "pass" + System.currentTimeMillis() + i;
            newAccounts.add(username + "," + password + ",Manager,enabled");
        }
        Files.write(Path.of(TEST_USERS_FILE), newAccounts, StandardOpenOption.APPEND);
    }

    private void backupAndCreateTestFile(String original, String backup, String... contents) throws IOException {
        if (Files.exists(Path.of(original))) {
            Files.copy(Path.of(original), Path.of(backup), StandardCopyOption.REPLACE_EXISTING);
        }
        Files.write(Path.of(original), String.join("\n", contents).getBytes());
    }

    private void restoreFile(String backup, String original) throws IOException {
        if (Files.exists(Path.of(backup))) {
            Files.move(Path.of(backup), Path.of(original), StandardCopyOption.REPLACE_EXISTING);
        }
    }
} 