package com.yorku.parking.booking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BasicBookingTest {
    
    @Test
    public void testConstructorWithValidRate() {
        BasicBooking booking = new BasicBooking(10.0);
        assertNotNull(booking);
        assertEquals(10.0, booking.getCost());
    }
    
    @Test
    public void testConstructorWithZeroRate() {
        BasicBooking booking = new BasicBooking(0.0);
        assertNotNull(booking);
        assertEquals(0.0, booking.getCost());
    }
    
    @Test
    public void testConstructorWithNegativeRate() {
        assertThrows(IllegalArgumentException.class, () -> {
            new BasicBooking(-5.0);
        });
    }
    
    @Test
    public void testGetCost() {
        BasicBooking booking = new BasicBooking(15.0);
        assertEquals(15.0, booking.getCost());
    }
    
    @Test
    public void testGetCostWithDecimal() {
        BasicBooking booking = new BasicBooking(12.5);
        assertEquals(12.5, booking.getCost());
    }
    
    @Test
    public void testGetCostPrecision() {
        BasicBooking booking = new BasicBooking(10.0);
        assertEquals(10.0, booking.getCost(), 0.001);
    }
    
    @Test
    public void testGetCostNotNegative() {
        BasicBooking booking = new BasicBooking(20.0);
        assertTrue(booking.getCost() >= 0);
    }
    
    @Test
    public void testGetCostType() {
        BasicBooking booking = new BasicBooking(25.0);
        assertTrue(Double.class.isInstance(booking.getCost()));
    }
    
    @Test
    public void testGetCostNotTooHigh() {
        BasicBooking booking = new BasicBooking(1000.0);
        assertTrue(booking.getCost() < 10000.0);
    }
    
    @Test
    public void testGetCostConsistency() {
        BasicBooking booking = new BasicBooking(30.0);
        double cost1 = booking.getCost();
        double cost2 = booking.getCost();
        assertEquals(cost1, cost2);
    }
} 