package com.yorku.parking.service;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

class ParkingServiceTest {
    private static final String TEST_SPACES_FILE = "src/main/resources/parking_spaces.csv";
    private static final String BACKUP_SPACES = "src/main/resources/parking_spaces_backup.csv";
    private ParkingService parkingService;

    @BeforeEach
    void setUp() throws IOException {
        // Backup existing file
        if (Files.exists(Path.of(TEST_SPACES_FILE))) {
            Files.copy(Path.of(TEST_SPACES_FILE), Path.of(BACKUP_SPACES), StandardCopyOption.REPLACE_EXISTING);
        }
        
        // Create test parking spaces file
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_SPACES_FILE))) {
            writer.println("A1,Standard,LotA,Available");
            writer.println("A2,Premium,LotA,Occupied");
            writer.println("B1,Standard,LotB,Available");
            writer.println("B2,Handicap,LotB,Maintenance");
            writer.println("C1,Premium,LotC,Available");
        }
        
        parkingService = new ParkingService();
    }

    @AfterEach
    void tearDown() throws IOException {
        // Restore original file
        if (Files.exists(Path.of(BACKUP_SPACES))) {
            Files.move(Path.of(BACKUP_SPACES), Path.of(TEST_SPACES_FILE), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Test
    void testLoadParkingLots() {
        String[] lots = parkingService.loadParkingLots();
        assertNotNull(lots);
        assertEquals(3, lots.length);
        assertTrue(containsLot(lots, "LotA"));
        assertTrue(containsLot(lots, "LotB"));
        assertTrue(containsLot(lots, "LotC"));
    }

    @Test
    void testSetLotStatus() {
        assertTrue(parkingService.setLotStatus("LotA", "Maintenance"));
        
        // Verify the status was updated
        List<String> lines = readAllLines();
        assertTrue(lines.stream()
            .filter(line -> line.contains("LotA"))
            .allMatch(line -> line.endsWith("Maintenance")));
    }

    @Test
    void testSetLotStatusWithNullLot() {
        assertFalse(parkingService.setLotStatus(null, "Available"));
    }

    @Test
    void testSetLotStatusWithNonexistentLot() {
        assertTrue(parkingService.setLotStatus("NonexistentLot", "Available"));
        // Should return true even if no changes were made (operation completed successfully)
    }

    @Test
    void testGetSensorData() {
        List<String> sensorData = parkingService.getSensorData();
        assertNotNull(sensorData);
        assertEquals(5, sensorData.size());
        
        // Verify data format
        assertTrue(sensorData.stream().allMatch(data -> 
            data.matches("\\[\\w+\\] Location: .+ \\| Lot: .+ \\| Status: .+")));
        
        // Verify specific entries
        assertTrue(sensorData.stream().anyMatch(data -> 
            data.contains("[A1]") && data.contains("Standard") && data.contains("Available")));
        assertTrue(sensorData.stream().anyMatch(data -> 
            data.contains("[B2]") && data.contains("Handicap") && data.contains("Maintenance")));
    }

    @Test
    void testGetSensorDataWithEmptyFile() throws IOException {
        // Clear the file
        new FileWriter(TEST_SPACES_FILE).close();
        
        List<String> sensorData = parkingService.getSensorData();
        assertNotNull(sensorData);
        assertTrue(sensorData.isEmpty());
    }



    @Test
    void testLoadParkingLotsWithIOException() throws IOException {
        // Temporarily make the file read-only
        File parkingFile = new File(TEST_SPACES_FILE);
        parkingFile.setReadOnly();
        try {
            String[] result = parkingService.loadParkingLots();
            assertEquals(0, result.length);
        } finally {
            parkingFile.setWritable(true);
        }
    }

    @Test
    void testSetLotStatusWithIOException() throws IOException {
        // Temporarily make the file read-only
        File parkingFile = new File(TEST_SPACES_FILE);
        parkingFile.setReadOnly();
        try {
            boolean result = parkingService.setLotStatus("A1", "occupied");
            assertFalse(result);
        } finally {
            parkingFile.setWritable(true);
        }
    }

    @Test
    void testGetSensorDataWithIOException() throws IOException {
        // Temporarily make the file read-only
        File parkingFile = new File(TEST_SPACES_FILE);
        parkingFile.setReadOnly();
        try {
            List<String> sensorData = parkingService.getSensorData();
            assertTrue(sensorData.isEmpty());
        } finally {
            parkingFile.setWritable(true);
        }
    }

    @Test
    void testLoadParkingLotsWithMalformedData() throws IOException {
        // Create test parking file with malformed data
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_SPACES_FILE))) {
            writer.println("InvalidFormat");
            writer.println("A1"); // Missing status
        }
        
        String[] result = parkingService.loadParkingLots();
        assertEquals(0, result.length);
    }

    @Test
    void testSetLotStatusWithMalformedData() throws IOException {
        // Create test parking file with malformed data
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_SPACES_FILE))) {
            writer.println("InvalidFormat");
            writer.println("A1"); // Missing status
        }
        
        boolean result = parkingService.setLotStatus("A1", "occupied");
        assertFalse(result);
    }

    @Test
    void testGetSensorDataWithMalformedData() throws IOException {
        // Create test parking file with malformed data
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_SPACES_FILE))) {
            writer.println("InvalidFormat");
            writer.println("A1"); // Missing status
        }
        
        List<String> sensorData = parkingService.getSensorData();
        assertTrue(sensorData.isEmpty());
    }

    private boolean containsLot(String[] lots, String lotName) {
        for (String lot : lots) {
            if (lot.trim().equals(lotName)) {
                return true;
            }
        }
        return false;
    }

    private List<String> readAllLines() {
        try {
            return Files.readAllLines(Path.of(TEST_SPACES_FILE));
        } catch (IOException e) {
            fail("Failed to read test file: " + e.getMessage());
            return null;
        }
    }
} 