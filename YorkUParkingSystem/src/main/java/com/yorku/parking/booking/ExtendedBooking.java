package com.yorku.parking.booking;

/**
 * Represents an extended booking that adds extra hours to a basic booking.
 */
public class ExtendedBooking implements Booking {
    private Booking booking;
    private int extraHours;
    private double extraRatePerHour = 5.0; // Additional cost per extra hour

    public ExtendedBooking(Booking booking, int extraHours) {
        if (extraHours < 0) {
            throw new IllegalArgumentException("Extra hours cannot be negative.");
        }
        this.booking = booking;
        this.extraHours = extraHours;
    }

    @Override
    public double getCost() {
        return booking.getCost() + (extraHours * extraRatePerHour);
    }

    public int getExtraHours() {
        return extraHours;
    }

    public void setExtraHours(int extraHours) {
        if (extraHours < 0) {
            throw new IllegalArgumentException("Extra hours cannot be negative.");
        }
        this.extraHours = extraHours;
    }

    public Booking getBaseBooking() {
        return booking;
    }

    public double getExtraCost() {
        return extraHours * extraRatePerHour;
    }
}
