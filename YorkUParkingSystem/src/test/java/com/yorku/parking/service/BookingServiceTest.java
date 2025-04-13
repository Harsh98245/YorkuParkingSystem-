package com.yorku.parking.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {
    private static final String TEST_BOOKINGS_FILE = "src/main/resources/bookings.csv";
    private static final String TEST_USERS_FILE = "src/main/resources/users.csv";
    private static final String BACKUP_BOOKINGS = "src/main/resources/bookings_backup.csv";
    private static final String BACKUP_USERS = "src/main/resources/users_backup.csv";
    
    private BookingService bookingService;

    @BeforeEach
    void setUp() throws IOException {
        // Backup existing files
        backupFile(TEST_BOOKINGS_FILE, BACKUP_BOOKINGS);
        backupFile(TEST_USERS_FILE, BACKUP_USERS);
        
        // Create test bookings file
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_BOOKINGS_FILE))) {
            writer.println("BK001,user1,ABC123,A1,2");
            writer.println("BK002,user2,XYZ789,B1,3");
        }
        
        // Create test users file
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_USERS_FILE))) {
            writer.println("user1,pass123,Student,enabled");
            writer.println("user2,pass456,Faculty,enabled");
        }
        
        bookingService = new BookingService();
    }

    @AfterEach
    void tearDown() throws IOException {
        // Restore original files
        restoreFile(BACKUP_BOOKINGS, TEST_BOOKINGS_FILE);
        restoreFile(BACKUP_USERS, TEST_USERS_FILE);
    }

    @Test
    void testGetBookingsForUser() {
        List<String> bookings = bookingService.getBookingsForUser("user1");
        assertEquals(1, bookings.size());
        assertTrue(bookings.stream().anyMatch(b -> b.startsWith("BK001")));
    }

    @Test
    void testGetBookingsForNonexistentUser() {
        List<String> bookings = bookingService.getBookingsForUser("nonexistent");
        assertTrue(bookings.isEmpty());
    }

    @Test
    void testCancelBooking() {
        String booking = "BK001,user1,ABC123,A1,2";
        assertTrue(bookingService.cancelBooking(booking));
        List<String> remainingBookings = readAllLines(TEST_BOOKINGS_FILE);
        assertFalse(remainingBookings.contains(booking));
    }

    @Test
    void testCancelInvalidBooking() {
        assertFalse(bookingService.cancelBooking("InvalidBooking"));
    }

    @Test
    void testExtendBooking() {
        String booking = "BK001,user1,ABC123,A1,2";
        assertTrue(bookingService.extendBooking(booking, 4));
        List<String> bookings = readAllLines(TEST_BOOKINGS_FILE);
        assertTrue(bookings.stream()
            .anyMatch(line -> line.matches("BK001,user1,ABC123,A1,4")));
    }

    @Test
    void testGetUserRole() {
        assertEquals("Student", bookingService.getUserRole("user1"));
        assertEquals("Faculty", bookingService.getUserRole("user2"));
        assertEquals("visitor", bookingService.getUserRole("nonexistent"));
    }

    @ParameterizedTest
    @CsvSource({
        "student,5.0",
        "faculty,8.0",
        "nonfaculty,10.0",
        "visitor,15.0"
    })
    void testCalculateRate(String role, double expectedRate) {
        assertEquals(expectedRate, bookingService.calculateRate(role));
    }

    @Test
    void testCancelBookingWithInvalidFormat() {
        assertFalse(bookingService.cancelBooking("InvalidFormat"));
    }

    @Test
    void testCancelBookingWithIOException() throws IOException {
        // Temporarily make the file read-only
        File bookingsFile = new File(TEST_BOOKINGS_FILE);
        bookingsFile.setReadOnly();
        try {
            assertFalse(bookingService.cancelBooking("BK001,user1,ABC123,A1,2"));
        } finally {
            bookingsFile.setWritable(true);
        }
    }

    @Test
    void testExtendBookingWithIOException() throws IOException {
        // Temporarily make the file read-only
        File bookingsFile = new File(TEST_BOOKINGS_FILE);
        bookingsFile.setReadOnly();
        try {
            assertFalse(bookingService.extendBooking("BK001,user1,ABC123,A1,2", 4));
        } finally {
            bookingsFile.setWritable(true);
        }
    }

    @Test
    void testGetBookingsForUserWithIOException() throws IOException {
        // Temporarily make the file read-only
        File bookingsFile = new File(TEST_BOOKINGS_FILE);
        bookingsFile.setReadOnly();
        try {
            List<String> bookings = bookingService.getBookingsForUser("user1");
            assertTrue(bookings.isEmpty());
        } finally {
            bookingsFile.setWritable(true);
        }
    }

    @Test
    void testGetUserRoleWithIOException() throws IOException {
        // Temporarily make the file read-only
        File usersFile = new File(TEST_USERS_FILE);
        usersFile.setReadOnly();
        try {
            assertEquals("visitor", bookingService.getUserRole("user1"));
        } finally {
            usersFile.setWritable(true);
        }
    }

    @Test
    void testGetBookingsForUserWithInvalidFormat() throws IOException {
        // Create test bookings file with invalid format
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_BOOKINGS_FILE))) {
            writer.println("InvalidFormat");
            writer.println("BK001"); // Missing fields
        }
        
        List<String> bookings = bookingService.getBookingsForUser("user1");
        assertTrue(bookings.isEmpty());
    }

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

    private List<String> readAllLines(String file) {
        try {
            return Files.readAllLines(Path.of(file));
        } catch (IOException e) {
            fail("Failed to read file: " + e.getMessage());
            return null;
        }
    }
} 