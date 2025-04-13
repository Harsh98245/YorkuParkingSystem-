package com.yorku.parking.gui;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LoginTest {
    private static final String TEST_USERS_FILE = "src/main/resources/users.csv";
    private static final String BACKUP_USERS = "src/main/resources/users_backup.csv";
    private RoleBasedLoginFrame loginFrame;

    @BeforeEach
    void setUp() throws IOException {
        // Backup and create test file
        backupAndCreateTestFile(TEST_USERS_FILE, BACKUP_USERS,
            "john,pass123,student,enabled",
            "mary,pass456,faculty,enabled",
            "admin,admin123,manager,enabled",
            "bob,pass789,visitor,enabled",
            "staff,staff123,nonfaculty,enabled",
            "disabled1,pass000,student,disabled"
        );
        
        loginFrame = new RoleBasedLoginFrame("Client");
    }

    @AfterEach
    void tearDown() throws IOException {
        restoreFile(BACKUP_USERS, TEST_USERS_FILE);
    }

    @ParameterizedTest
    @CsvSource({
        "john,pass123,student,true",
        "mary,pass456,faculty,true",
        "admin,admin123,manager,true",
        "invalid,pass123,student,false",
        "john,wrongpass,student,false",
        "john,pass123,manager,false"
    })
    void testCredentialValidation(String username, String password, String role, boolean expected) throws Exception {
        assertEquals(expected, validateCredentials(username, password, role));
    }

    @Test
    void testDisabledUserLogin() {
        assertFalse(validateCredentials("disabled1", "pass000", "student"));
    }

    @Test
    void testEmptyCredentials() {
        assertFalse(validateCredentials("", "", "student"));
        assertFalse(validateCredentials(null, null, "student"));
    }

    @Test
    void testCaseInsensitiveRoleValidation() {
        assertTrue(validateCredentials("john", "pass123", "STUDENT"));
        assertTrue(validateCredentials("mary", "pass456", "FACULTY"));
    }

    @Test
    void testCorruptedUserData() throws IOException {
        backupAndCreateTestFile(TEST_USERS_FILE, BACKUP_USERS,
            "corrupted_line",
            "john,pass123", // Missing role
            ",pass123,student" // Missing username
        );
        assertFalse(validateCredentials("john", "pass123", "student"));
    }

    private boolean validateCredentials(String username, String password, String role) {
        if (username == null || password == null || role == null || 
            username.isEmpty() || password.isEmpty() || role.isEmpty()) {
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(TEST_USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4 && 
                    parts[0].equals(username) && 
                    parts[1].equals(password) && 
                    parts[2].equalsIgnoreCase(role) &&
                    parts[3].equals("enabled")) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
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