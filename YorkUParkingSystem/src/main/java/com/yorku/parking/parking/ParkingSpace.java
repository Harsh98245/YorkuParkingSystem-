package com.yorku.parking.parking;

public class ParkingSpace {
    private String spaceId;
    private boolean isOccupied;

    public ParkingSpace(String spaceId) {
        this.spaceId = spaceId;
        this.isOccupied = false; // Default: Not occupied
    }

    public String getSpaceId() {
        return spaceId;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        this.isOccupied = occupied;
    }
}