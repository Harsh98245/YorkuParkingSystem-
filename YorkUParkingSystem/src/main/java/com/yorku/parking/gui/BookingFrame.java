package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;

public class BookingFrame extends JFrame {
    private JComboBox<String> parkingDropdown;
    private JTextField plateField, hoursField;
    private JButton bookButton, cancelButton, extendButton;
    private String username;
    private final Map<String, Double> rates = new HashMap<>();

    public BookingFrame(String username) {
        this.username = username;
        setTitle("Book Parking");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        rates.put("Student", 5.0);
        rates.put("Faculty", 8.0);
        rates.put("Non-Faculty Staff", 10.0);
        rates.put("Visitor", 15.0);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        panel.add(new JLabel("Licence Plate:"));
        plateField = new JTextField();
        panel.add(plateField);

        panel.add(new JLabel("Select Parking Space:"));
        parkingDropdown = new JComboBox<>(loadAvailableSpaces());
        panel.add(parkingDropdown);

        panel.add(new JLabel("Booking Duration (hrs):"));
        hoursField = new JTextField();
        panel.add(hoursField);

        bookButton = new JButton("Book");
        cancelButton = new JButton("Cancel Booking");
        extendButton = new JButton("Extend Booking");

        panel.add(bookButton);
        panel.add(cancelButton);
        panel.add(extendButton);

        bookButton.addActionListener(e -> bookSpace());
        cancelButton.addActionListener(e -> cancelBooking());
        extendButton.addActionListener(e -> extendBooking());

        add(panel);
        setVisible(true);
    }

    private String[] loadAvailableSpaces() {
    	java.util.List<String> available = new java.util.ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3 && parts[2].equalsIgnoreCase("Available")) {
                    available.add(parts[0] + " - " + parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return available.toArray(new String[0]);
    }

    private void bookSpace() {
        String plate = plateField.getText().trim();
        String selected = (String) parkingDropdown.getSelectedItem();
        String durationText = hoursField.getText().trim();

        if (plate.isEmpty() || selected == null || durationText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try {
            int hours = Integer.parseInt(durationText);
            double cost = getUserRate() * hours;
            JOptionPane.showMessageDialog(this, "Booking successful!\nDeposit: $" + getUserRate() + "\nTotal: $" + cost);

            try (PrintWriter out = new PrintWriter(new FileWriter("src/main/resources/bookings.csv", true))) {
                out.println(username + "," + plate + "," + selected + "," + hours);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving booking to file.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            updateSpaceStatus(selected, "Occupied");

            dispose();
            new BookingFrame(username);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid hours.");
        }
    }

    private void cancelBooking() {
        String selectedSpace = (String) parkingDropdown.getSelectedItem();
        if (selectedSpace == null) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.");
            return;
        }

        File inputFile = new File("src/main/resources/bookings.csv");
        File tempFile = new File("src/main/resources/bookings_temp.csv");

        boolean bookingFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(username) && line.contains(selectedSpace)) {
                    bookingFound = true;
                    continue;
                }
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bookingFound) {
            tempFile.renameTo(inputFile);
            updateSpaceStatus(selectedSpace, "Available");
            JOptionPane.showMessageDialog(this, "Booking cancelled and space marked available.");
            dispose();
            new BookingFrame(username);
        } else {
            JOptionPane.showMessageDialog(this, "No matching booking found.");
        }
    }

    private void extendBooking() {
        String selectedSpace = (String) parkingDropdown.getSelectedItem();
        String extraHoursStr = JOptionPane.showInputDialog(this, "Enter additional hours:");
        
        if (selectedSpace == null || extraHoursStr == null) {
            return;
        }

        try {
            int extraHours = Integer.parseInt(extraHoursStr);
            File inputFile = new File("src/main/resources/bookings.csv");
            File tempFile = new File("src/main/resources/bookings_temp.csv");

            boolean updated = false;

            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                 PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 4 && parts[0].equals(username) && parts[2].equals(selectedSpace)) {
                        int newDuration = Integer.parseInt(parts[3]) + extraHours;
                        writer.println(parts[0] + "," + parts[1] + "," + parts[2] + "," + newDuration);
                        updated = true;
                    } else {
                        writer.println(line);
                    }
                }
            }

            if (updated) {
                tempFile.renameTo(inputFile);
                double cost = getUserRate() * extraHours;
                JOptionPane.showMessageDialog(this, "Booking extended. Additional cost: $" + cost);
                dispose();
                new BookingFrame(username);
            } else {
                JOptionPane.showMessageDialog(this, "No booking found to extend.");
            }

        } catch (NumberFormatException | IOException e) {
            JOptionPane.showMessageDialog(this, "Invalid input or error extending booking.");
            e.printStackTrace();
        }
    }

    private double getUserRate() {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/users.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 3 && values[0].equals(username)) {
                    return rates.getOrDefault(values[2], 15.0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 15.0;
    }

    private void updateSpaceStatus(String spaceInfo, String status) {
    	java.util.List<String> lines = new java.util.ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if ((parts[0] + " - " + parts[1]).equals(spaceInfo)) {
                    lines.add(parts[0] + "," + parts[1] + "," + status);
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter out = new PrintWriter(new FileWriter("src/main/resources/parking_spaces.csv"))) {
            for (String updated : lines) {
                out.println(updated);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
