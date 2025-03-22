package com.yorku.parking.models;

public class Student implements User {
    @Override
    public void displayUserType() {
        System.out.println("User Type: Student");
    }

    @Override
    public double getParkingRate() {
        return 5.0; // Student parking rate
    }
}
