package com.yorku.parking.parking;

public class ParkingSystem {
    private static ParkingSystem instance;

    private ParkingSystem() {}

    public static ParkingSystem getInstance() {
        if (instance == null) {
            instance = new ParkingSystem();
        }
        return instance;
    }

    public void manageParking() {
        System.out.println("Managing Parking System...");
    }
}