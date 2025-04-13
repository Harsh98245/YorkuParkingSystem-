package com.yorku.parking.booking;

/**
 * Represents a basic parking booking with a fixed rate.
 */
public class BasicBooking implements Booking {
    private double baseRate;

    public BasicBooking(double baseRate) {
        if (baseRate < 0) {
            throw new IllegalArgumentException("Base rate cannot be negative.");
        }
        this.baseRate = baseRate;
    }

    @Override
    public double getCost() {
        return baseRate;
    }
}