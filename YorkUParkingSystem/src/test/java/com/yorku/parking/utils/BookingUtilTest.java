package com.yorku.parking.utils;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BookingUtilTest {
    private static final String TEST_BOOKINGS_FILE = "src/main/resources/bookings.csv";
    private static final String TEST_SPACES_FILE = "src/main/resources/parking_spaces.csv";
    private static final String BACKUP_BOOKINGS = "src/main/resources/bookings_backup.csv";
    private static final String BACKUP_SPACES = "src/main/resources/spaces_backup.csv";

    @BeforeEach
    void setUp() throws IOException {
        // Backup existing files
        backupFile(TEST_BOOKINGS_FILE, BACKUP_BOOKINGS);
        backupFile(TEST_SPACES_FILE, BACKUP_SPACES);
        
        // Create test booking file
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_BOOKINGS_FILE))) {
            writer.println("BK123,testUser,ABC123,A1,2");
            writer.println("BK124,otherUser,XYZ789,B2,3");
            writer.println("BK125,testUser,DEF456,C3,1");
        }
        
        // Create test spaces file
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_SPACES_FILE))) {
            writer.println("A1,Standard,Available");
            writer.println("B2,Premium,Occupied");
            writer.println("C3,Handicap,Available");
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Restore original files
        restoreFile(BACKUP_BOOKINGS, TEST_BOOKINGS_FILE);
        restoreFile(BACKUP_SPACES, TEST_SPACES_FILE);
    }

    @Test
    void testGetBookingsForUser() {
        List<String> bookings = BookingUtil.getBookingsForUser("testUser");
        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().anyMatch(b -> b.contains("BK123")));
        assertTrue(bookings.stream().anyMatch(b -> b.contains("BK125")));
    }

    @Test
    void testUpdateSpaceStatus() {
        String spaceInfo = "A1 - Standard (Available)";
        BookingUtil.updateSpaceStatus(spaceInfo, "Occupied");
        
        try {
            List<String> lines = Files.readAllLines(Path.of(TEST_SPACES_FILE));
            assertTrue(lines.stream().anyMatch(line -> line.equals("A1,Standard,Occupied")));
        } catch (IOException e) {
            fail("Failed to read spaces file: " + e.getMessage());
        }
    }

    @Test
    void testRemoveBookingLine() {
        String bookingToRemove = "BK123,testUser,ABC123,A1,2";
        BookingUtil.removeBookingLine(bookingToRemove);
        
        try {
            List<String> lines = Files.readAllLines(Path.of(TEST_BOOKINGS_FILE));
            assertEquals(2, lines.size());
            assertFalse(lines.stream().anyMatch(line -> line.equals(bookingToRemove)));
        } catch (IOException e) {
            fail("Failed to read bookings file: " + e.getMessage());
        }
    }

    @Test
    void testSaveBooking() {
        BookingUtil.saveBooking("newUser", "GHI789", "D4", 4);
        
        try {
            List<String> lines = Files.readAllLines(Path.of(TEST_BOOKINGS_FILE));
            assertTrue(lines.stream().anyMatch(line -> 
                line.matches("BK\\d+,newUser,GHI789,D4,4")));
        } catch (IOException e) {
            fail("Failed to read bookings file: " + e.getMessage());
        }
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
} 