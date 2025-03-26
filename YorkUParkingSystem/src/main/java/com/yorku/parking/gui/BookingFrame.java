package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import com.yorku.parking.gui.PaymentFrame;

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

        // Panel Setup for the UI
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        panel.add(new JLabel("Licence Plate:"));
        plateField = new JTextField();
        panel.add(plateField);

        // Parking Space dropdown
        panel.add(new JLabel("Select Parking Space:"));
        parkingSpaceDropdown = new JComboBox<>();
        loadAvailableSpaces();
        panel.add(parkingSpaceDropdown);

        // Start Time dropdown
        panel.add(new JLabel("Start Time:"));
        startTimeDropdown = new JComboBox<>(generateTimeOptions());
        panel.add(startTimeDropdown);

        // End Time dropdown
        panel.add(new JLabel("End Time:"));
        endTimeDropdown = new JComboBox<>(generateTimeOptions());
        panel.add(endTimeDropdown);

        // Book Button
        bookButton = new JButton("Book");
        panel.add(bookButton);

        // Back Button
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
            times[i] = String.format("%02d:00", i); // 00:00, 01:00, etc.
        }
        return times;
    }

    private void loadAvailableSpaces() {
        List<String> available = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                // Assuming the format is: lot,space,status (3 parts)
                if (parts.length == 3 && parts[2].equalsIgnoreCase("Available")) {
                    // Format: PS001 - A1
                    available.add(parts[0] + " - " + parts[1]); // Combine Lot and Space (e.g., PS001 - A1)
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Clear existing items, if any, before adding new ones
        parkingSpaceDropdown.removeAllItems();

        // Add available spaces to the dropdown
        for (String space : available) {
            parkingSpaceDropdown.addItem(space);
        }
    }
    private int calculateHours(String startTime, String endTime) {
        // Basic hour calculation (you can improve this based on time format)
        int start = Integer.parseInt(startTime.split(":")[0]);
        int end = Integer.parseInt(endTime.split(":")[0]);
        return end - start;
    }
    private void updateSpaceStatus(String selectedSpace, String status) {
        // Update the status of the parking space in the CSV file or database
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"));
             PrintWriter out = new PrintWriter(new FileWriter("src/main/resources/parking_spaces_temp.csv"))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                // Check if the current line is for the selected parking space
                String spaceId = parts[0] + "-" + parts[1];  // Concatenating parts[0] and parts[1] to form the full parking space ID
                if (spaceId.equals(selectedSpace)) {
                    // If the space is found, update its status
                    out.println(parts[0] + "," + parts[1] + "," + status); // Update space status
                } else {
                    // If the space is not found, write the line as is
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

        // Get the user's role from your system (assuming we have a method to get it, replace this with actual logic)
        String role = getUserRole(username); // Assuming we have a method to get the user's role
        if (role == null) {
            JOptionPane.showMessageDialog(this, "User role not found.");
            return;
        }
        // Set the rate per hour based on the user's role
        double rate = 0.0;
        if (role.equalsIgnoreCase("Student")) {
            rate = 5.0;
        } else if (role.equalsIgnoreCase("Faculty")) {
            rate = 8.0;
        } else if (role.equalsIgnoreCase("NonFaculty")) {
            rate = 10.0;
        } else if (role.equalsIgnoreCase("Visitor")) {
            rate = 15.0;
        }

        // Calculate the number of hours between start and end time
        int hours = calculateHours(startTime, endTime);
        
        // Calculate the deposit and total
        double deposit = rate ; // Assuming deposit is half the total cost
        double total = rate * hours+deposit;

        // Show the confirmation dialog box with deposit and total
        String message = String.format("Deposit: $%.2f\nTotal: $%.2f", deposit, total);
        int option = JOptionPane.showConfirmDialog(this, message, "Confirm Booking", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            // Booking logic here
            // Save the booking details to a file or database as needed
            try (PrintWriter out = new PrintWriter(new FileWriter("src/main/resources/bookings.csv", true))) {
                out.println(username + "," + licensePlate + "," + selectedSpace + "," + hours);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving booking to file.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update space status
            updateSpaceStatus(selectedSpace, "Occupied");

            // Redirect to payment frame
            dispose();
         // Assuming 'role' is defined after fetching it based on username
            new PaymentFrame(username, selectedSpace, role, hours, total);



        }
        
    }
    private String getUserRole(String username) {
        // This will fetch the role from the user CSV based on the username
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/users.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equalsIgnoreCase(username)) {
                    return parts[2]; // Assuming role is the third column in users.csv
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Return null if role is not found
    }


    private void goBack() {
        dispose();
        new WelcomeFrame(); // Navigate back to the WelcomeFrame
    }
}
