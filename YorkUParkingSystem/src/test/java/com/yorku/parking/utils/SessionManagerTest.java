package com.yorku.parking.utils;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {
    private static final String TEST_USER = "testUser";
    private static final String TEST_USER2 = "testUser2";

    @BeforeEach
    void setUp() {
        // Ensure no users are logged in at start of each test
        SessionManager.logout(TEST_USER);
        SessionManager.logout(TEST_USER2);
    }

    @Test
    void testUserLogin() {
        assertFalse(SessionManager.isLoggedIn(TEST_USER));
        SessionManager.login(TEST_USER);
        assertTrue(SessionManager.isLoggedIn(TEST_USER));
    }

    @Test
    void testUserLogout() {
        SessionManager.login(TEST_USER);
        assertTrue(SessionManager.isLoggedIn(TEST_USER));
        SessionManager.logout(TEST_USER);
        assertFalse(SessionManager.isLoggedIn(TEST_USER));
    }

    @Test
    void testMultipleUserSessions() {
        // Login both users
        SessionManager.login(TEST_USER);
        SessionManager.login(TEST_USER2);
        
        // Verify both are logged in
        assertTrue(SessionManager.isLoggedIn(TEST_USER));
        assertTrue(SessionManager.isLoggedIn(TEST_USER2));
        
        // Logout one user
        SessionManager.logout(TEST_USER);
        
        // Verify correct user was logged out
        assertFalse(SessionManager.isLoggedIn(TEST_USER));
        assertTrue(SessionManager.isLoggedIn(TEST_USER2));
    }

    @Test
    void testLogoutNonExistentUser() {
        // Should not throw exception
        assertDoesNotThrow(() -> SessionManager.logout("nonexistentUser"));
    }

    @Test
    void testRepeatedLoginLogout() {
        // Multiple login/logout cycles
        for (int i = 0; i < 3; i++) {
            assertFalse(SessionManager.isLoggedIn(TEST_USER));
            SessionManager.login(TEST_USER);
            assertTrue(SessionManager.isLoggedIn(TEST_USER));
            SessionManager.logout(TEST_USER);
            assertFalse(SessionManager.isLoggedIn(TEST_USER));
        }
    }
} 