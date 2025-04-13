package com.yorku.parking.booking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExtendedBookingTest {
    
    @Test
    public void testConstructorWithValidHours() {
        BasicBooking baseBooking = new BasicBooking(10.0);
        ExtendedBooking extendedBooking = new ExtendedBooking(baseBooking, 2);
        assertNotNull(extendedBooking);
        assertEquals(20.0, extendedBooking.getCost());
    }
    
    @Test
    public void testConstructorWithZeroHours() {
        BasicBooking baseBooking = new BasicBooking(15.0);
        ExtendedBooking extendedBooking = new ExtendedBooking(baseBooking, 0);
        assertNotNull(extendedBooking);
        assertEquals(15.0, extendedBooking.getCost());
    }
    
    @Test
    public void testConstructorWithNegativeHours() {
        BasicBooking baseBooking = new BasicBooking(10.0);
        assertThrows(IllegalArgumentException.class, () -> {
            new ExtendedBooking(baseBooking, -1);
        });
    }
    
    @Test
    public void testGetCost() {
        BasicBooking baseBooking = new BasicBooking(10.0);
        ExtendedBooking extendedBooking = new ExtendedBooking(baseBooking, 3);
        assertEquals(25.0, extendedBooking.getCost());
    }
    
    @Test
    public void testGetExtraHours() {
        BasicBooking baseBooking = new BasicBooking(10.0);
        ExtendedBooking extendedBooking = new ExtendedBooking(baseBooking, 4);
        assertEquals(4, extendedBooking.getExtraHours());
    }
    
    @Test
    public void testSetExtraHours() {
        BasicBooking baseBooking = new BasicBooking(10.0);
        ExtendedBooking extendedBooking = new ExtendedBooking(baseBooking, 2);
        extendedBooking.setExtraHours(5);
        assertEquals(5, extendedBooking.getExtraHours());
    }
    
    @Test
    public void testSetExtraHoursNegative() {
        BasicBooking baseBooking = new BasicBooking(10.0);
        ExtendedBooking extendedBooking = new ExtendedBooking(baseBooking, 2);
        assertThrows(IllegalArgumentException.class, () -> {
            extendedBooking.setExtraHours(-1);
        });
    }
    
    @Test
    public void testGetBaseBooking() {
        BasicBooking baseBooking = new BasicBooking(10.0);
        ExtendedBooking extendedBooking = new ExtendedBooking(baseBooking, 2);
        assertEquals(baseBooking, extendedBooking.getBaseBooking());
    }
    
    @Test
    public void testGetExtraCost() {
        BasicBooking baseBooking = new BasicBooking(10.0);
        ExtendedBooking extendedBooking = new ExtendedBooking(baseBooking, 3);
        assertEquals(15.0, extendedBooking.getExtraCost());
    }
    
    @Test
    public void testCostCalculation() {
        BasicBooking baseBooking = new BasicBooking(20.0);
        ExtendedBooking extendedBooking = new ExtendedBooking(baseBooking, 4);
        double expectedCost = 20.0 + (4 * 5.0);
        assertEquals(expectedCost, extendedBooking.getCost());
    }
} 