package com.yorku.parking.parking;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.io.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class ParkingTest {
    private ParkingSpace parkingSpace;
    private ParkingSystem parkingSystem;
    private UserNotifier userNotifier;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        parkingSpace = new ParkingSpace("A1");
        parkingSystem = ParkingSystem.getInstance();
        userNotifier = new UserNotifier("TestUser");
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        outputStream.reset();
    }

    // Test data providers
    static Stream<Arguments> spaceIds() {
        return Stream.of(
            Arguments.of("A1"),
            Arguments.of("B2"),
            Arguments.of("C3"),
            Arguments.of(""),
            Arguments.of("123"),
            Arguments.of((Object)null)
        );
    }

    static Stream<Arguments> notifierData() {
        return Stream.of(
            Arguments.of("User1", "Test status", "User1 notified: Test status\n"),
            Arguments.of("User2", "", "User2 notified: \n"),
            Arguments.of("User3", null, "User3 notified: null\n")
        );
    }

    // ParkingSpace Tests
    @Test
    void testParkingSpaceBasics() {
        // Test initialization
        assertEquals("A1", parkingSpace.getSpaceId());
        assertFalse(parkingSpace.isOccupied());

        // Test occupancy changes
        parkingSpace.setOccupied(true);
        assertTrue(parkingSpace.isOccupied());
        parkingSpace.setOccupied(false);
        assertFalse(parkingSpace.isOccupied());
    }

    @Test
    void testMultipleSpacesManagement() {
        ParkingSpace space1 = new ParkingSpace("A1");
        ParkingSpace space2 = new ParkingSpace("B1");
        
        // Test initial state
        assertFalse(space1.isOccupied());
        assertFalse(space2.isOccupied());
        
        // Test independence
        space1.setOccupied(true);
        assertTrue(space1.isOccupied());
        assertFalse(space2.isOccupied());
        
        space2.setOccupied(true);
        assertTrue(space1.isOccupied());
        assertTrue(space2.isOccupied());
    }

    @ParameterizedTest
    @MethodSource("spaceIds")
    void testSpaceIdValidation(String spaceId) {
        ParkingSpace space = new ParkingSpace(spaceId);
        assertEquals(spaceId, space.getSpaceId());
    }

    @Test
    void testSpaceIdCaseSensitivity() {
        ParkingSpace upperSpace = new ParkingSpace("A1");
        ParkingSpace lowerSpace = new ParkingSpace("a1");
        assertNotEquals(upperSpace.getSpaceId(), lowerSpace.getSpaceId());
        
        // Test independence of similarly named spaces
        upperSpace.setOccupied(true);
        assertFalse(lowerSpace.isOccupied());
    }

    // ParkingSystem Tests
    @Test
    void testParkingSystem() {
        // Test singleton pattern
        ParkingSystem instance1 = ParkingSystem.getInstance();
        ParkingSystem instance2 = ParkingSystem.getInstance();
        assertSame(instance1, instance2);

        // Test management functionality
        parkingSystem.manageParking();
        assertEquals("Managing Parking System...\n", outputStream.toString());
    }

    // UserNotifier Tests
    @ParameterizedTest
    @MethodSource("notifierData")
    void testUserNotifier(String userName, String status, String expected) {
        UserNotifier notifier = new UserNotifier(userName);
        notifier.update(status);
        assertEquals(expected, outputStream.toString());
    }

    @Test
    void testMultipleNotifiers() {
        UserNotifier notifier1 = new UserNotifier("User1");
        UserNotifier notifier2 = new UserNotifier("User2");
        
        notifier1.update("Status1");
        assertEquals("User1 notified: Status1\n", outputStream.toString());
        
        outputStream.reset();
        notifier2.update("Status2");
        assertEquals("User2 notified: Status2\n", outputStream.toString());
    }
} 