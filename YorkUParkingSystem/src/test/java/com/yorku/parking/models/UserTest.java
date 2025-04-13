package com.yorku.parking.models;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.io.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;
    private User user;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    static Stream<Arguments> userTypes() {
        return Stream.of(
            Arguments.of("Student", new Student(), 5.0, "User Type: Student"),
            Arguments.of("Faculty", new Faculty(), 8.0, "Faculty User Created")
        );
    }

    @ParameterizedTest
    @MethodSource("userTypes")
    void testGetParkingRate(String type, User user, double expectedRate, String displayMessage) {
        assertEquals(expectedRate, user.getParkingRate());
    }

    @ParameterizedTest
    @MethodSource("userTypes")
    void testDisplayUserType(String type, User user, double rate, String expectedMessage) {
        user.displayUserType();
        assertEquals(expectedMessage + "\r\n", outContent.toString());
    }

    @ParameterizedTest
    @MethodSource("userTypes")
    void testInstanceOfUser(String type, User user, double rate, String displayMessage) {
        assertTrue(user instanceof User);
    }

    @ParameterizedTest
    @MethodSource("userTypes")
    void testParkingRateNotNegative(String type, User user, double rate, String displayMessage) {
        assertTrue(user.getParkingRate() >= 0);
    }

    @ParameterizedTest
    @MethodSource("userTypes")
    void testParkingRatePrecision(String type, User user, double expectedRate, String displayMessage) {
        assertEquals(expectedRate, user.getParkingRate(), 0.001);
    }

    @ParameterizedTest
    @MethodSource("userTypes")
    void testDisplayUserTypeNotEmpty(String type, User user, double rate, String displayMessage) {
        user.displayUserType();
        assertFalse(outContent.toString().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("userTypes")
    void testDisplayUserTypeContainsRole(String type, User user, double rate, String displayMessage) {
        user.displayUserType();
        assertTrue(outContent.toString().contains(type));
    }

    @ParameterizedTest
    @MethodSource("userTypes")
    void testParkingRateType(String type, User user, double rate, String displayMessage) {
        assertTrue(Double.class.isInstance(user.getParkingRate()));
    }

    @ParameterizedTest
    @MethodSource("userTypes")
    void testParkingRateNotZero(String type, User user, double rate, String displayMessage) {
        assertNotEquals(0.0, user.getParkingRate());
    }

    @ParameterizedTest
    @MethodSource("userTypes")
    void testParkingRateNotTooHigh(String type, User user, double rate, String displayMessage) {
        assertTrue(user.getParkingRate() < 100.0);
    }

    @Test
    void testParkingRateComparison() {
        Student student = new Student();
        Faculty faculty = new Faculty();
        assertTrue(student.getParkingRate() < faculty.getParkingRate());
    }
} 