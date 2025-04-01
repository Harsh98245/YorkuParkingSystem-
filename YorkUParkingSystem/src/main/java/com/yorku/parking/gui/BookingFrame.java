package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import com.yorku.parking.payment.*;

public class BookingFrame extends JFrame {
    private String username;
    private JComboBox<String> parkingSpaceDropdown;
    private JTextField plateField;
    private JComboBox<String> startTimeDropdown, endTimeDropdown;
    private JButton bookButton, backButton;

    public BookingFrame(String username) {
        this.username = username;
        setTitle("Book Parking");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.add(new JLabel("Licence Plate:"));
        plateField = new JTextField();
        panel.add(plateField);

        panel.add(new JLabel("Select Parking Space:"));
        parkingSpaceDropdown = new JComboBox<>();
        loadAvailableSpaces();
        panel.add(parkingSpaceDropdown);

        panel.add(new JLabel("Start Time:"));
        startTimeDropdown = new JComboBox<>(generateTimeOptions());
        panel.add(startTimeDropdown);

        panel.add(new JLabel("End Time:"));
        endTimeDropdown = new JComboBox<>(generateTimeOptions());
        panel.add(endTimeDropdown);

        bookButton = new JButton("Book");
        panel.add(bookButton);
        backButton = new JButton("Back");
        panel.add(backButton);

        bookButton.addActionListener(e -> bookSpace());
        backButton.addActionListener(e -> goBack());

        add(panel);
        setVisible(true);
    }

    private String[] generateTimeOptions() {
        String[] times = new String[24];
        for (int i = 0; i < 24; i++) {
            times[i] = String.format("%02d:00", i);
        }
        return times;
    }

    private void loadAvailableSpaces() {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3 && parts[2].equalsIgnoreCase("Available")) {
                    parkingSpaceDropdown.addItem(parts[0] + "-" + parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int calculateHours(String startTime, String endTime) {
        return Integer.parseInt(endTime.split(":")[0]) - Integer.parseInt(startTime.split(":")[0]);
    }

    private void updateSpaceStatus(String selectedSpace, String status) {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"));
             PrintWriter out = new PrintWriter(new FileWriter("src/main/resources/parking_spaces_temp.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String spaceId = parts[0] + "-" + parts[1];
                if (spaceId.equals(selectedSpace)) {
                    out.println(parts[0] + "," + parts[1] + "," + status);
                } else {
                    out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void bookSpace() {
        String licensePlate = plateField.getText().trim();
        String selectedSpace = (String) parkingSpaceDropdown.getSelectedItem();
        String startTime = (String) startTimeDropdown.getSelectedItem();
        String endTime = (String) endTimeDropdown.getSelectedItem();

        if (licensePlate.isEmpty() || selectedSpace == null || startTime == null || endTime == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        String role = getUserRole(username);
        if (role == null) {
            JOptionPane.showMessageDialog(this, "User role not found.");
            return;
        }

        double rate = switch (role.toLowerCase()) {
            case "student" -> 5.0;
            case "faculty" -> 8.0;
            case "nonfaculty" -> 10.0;
            default -> 15.0;
        };

        int hours = calculateHours(startTime, endTime);
        if (hours <= 0) {
            JOptionPane.showMessageDialog(this, "End time must be after start time.");
            return;
        }


        double deposit = rate;
        double total = rate * hours;
        double remaining = total - deposit;

        String bookingId = "BK" + new Random().nextInt(1000000);

        String message = String.format("A deposit of $%.2f will be charged now.\nRemaining $%.2f will be paid at checkout.\nBooking ID: %s", deposit, remaining, bookingId);
        int option = JOptionPane.showConfirmDialog(this, message, "Confirm Booking", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try (PrintWriter out = new PrintWriter(new FileWriter("src/main/resources/bookings.csv", true))) {
                out.println(bookingId + "," + username + "," + licensePlate + "," + selectedSpace + "," + hours);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving booking.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            updateSpaceStatus(selectedSpace, "Occupied");
            dispose();
            new PaymentFrame(username, selectedSpace, role, hours, deposit, true); // true = deposit payment
        }
    }

    private String getUserRole(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/users.csv"))) {
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
        return null;
    }

    private void goBack() {
        dispose();
        new WelcomeFrame();
    }
}
