package com.yorku.parking.models;

public class Faculty implements User {
    @Override
    public void displayUserType() {
        System.out.println("Faculty User Created");
    }

    @Override
    public double getParkingRate() {
        return 8.0; // Parking rate per hour for faculty members
    }
}