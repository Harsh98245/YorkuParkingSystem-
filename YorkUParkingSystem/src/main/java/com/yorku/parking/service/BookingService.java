package com.yorku.parking.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class BookingService {
    private static final String BOOKINGS_FILE = "src/main/resources/bookings.csv";
    private static final String PARKING_SPACES_FILE = "src/main/resources/parking_spaces.csv";
    private static final String USERS_FILE = "src/main/resources/users.csv";

    public List<String> getBookingsForUser(String username) {
        List<String> bookings = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(BOOKINGS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[1].equals(username)) {
                    bookings.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public boolean cancelBooking(String selectedBooking) {
        try {
            String parkingSpot = selectedBooking.split(",")[3].trim();
            removeBookingLine(selectedBooking);
            updateSpaceStatus(parkingSpot, "Available");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean extendBooking(String selectedBooking, int newDuration) {
        String[] parts = selectedBooking.split(",");
        if (parts.length < 5) return false;

        File inputFile = new File(BOOKINGS_FILE);
        File tempFile = new File(BOOKINGS_FILE + ".temp");
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(selectedBooking)) {
                    writer.println(parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3] + "," + newDuration);
                    updated = true;
                } else {
                    writer.println(line);
                }
            }

            if (updated) {
                inputFile.delete();
                tempFile.renameTo(inputFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return updated;
    }

    public String getUserRole(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equalsIgnoreCase(username)) {
                    return parts[2];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "visitor";
    }

    public double calculateRate(String role) {
        return switch (role.toLowerCase()) {
            case "student" -> 5.0;
            case "faculty" -> 8.0;
            case "nonfaculty" -> 10.0;
            default -> 15.0;
        };
    }

    private void removeBookingLine(String bookingLine) {
        File inputFile = new File(BOOKINGS_FILE);
        File tempFile = new File(BOOKINGS_FILE + ".temp");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().equals(bookingLine.trim())) {
                    writer.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        inputFile.delete();
        tempFile.renameTo(inputFile);
    }

    private void updateSpaceStatus(String spaceInfo, String status) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(PARKING_SPACES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equals(spaceInfo)) {
                    lines.add(parts[0] + "," + parts[1] + "," + status);
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(PARKING_SPACES_FILE))) {
            for (String updatedLine : lines) {
                writer.println(updatedLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}