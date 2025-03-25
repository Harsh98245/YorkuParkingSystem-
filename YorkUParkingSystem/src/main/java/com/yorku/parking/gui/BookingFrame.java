package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class BookingFrame extends JFrame {
    private JComboBox<String> parkingDropdown;
    private JTextField plateField, hoursField;
    private JButton bookButton, cancelButton, extendButton, backButton;
    private String username;
    private final Map<String, Double> rates = new HashMap<>();

    public BookingFrame(String username) {
        this.username = username;
        setTitle("Book Parking");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
        cancelButton = null;
        extendButton = null;
        backButton = new JButton("Back");

        panel.add(bookButton);
       
        panel.add(backButton);

        bookButton.addActionListener(e -> bookSpace());
        
        backButton.addActionListener(e -> goBack());
        add(panel);
        setVisible(true);
    }

    private String[] loadAvailableSpaces() {
        java.util.List<String> available = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4 && parts[3].equalsIgnoreCase("Available")) {
                    String label = parts[0] + " - " + parts[1] + " (" + parts[2] + ")";
                    available.add(label);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return available.toArray(new String[0]);
    }
    private void goBack() {
    	dispose();
    	new DashboardFrame(username,false);
    }
    private void saveBooking(String username, String space, String plate, int hours) {
        try (PrintWriter out = new PrintWriter(new FileWriter("src/main/resources/bookings.csv", true))) {
            out.println(username + "," + plate + "," + space + "," + hours);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving booking to file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
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
            new PaymentFrame(username, plate, selected, cost, () -> {
                saveBooking(username, selected, plate, hours);
                updateSpaceStatus(selected, "Occupied");
                JOptionPane.showMessageDialog(this, "Booking confirmed and paid!");
                dispose();
                new DashboardFrame(username, false); // or true if manager
            });

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid hours.");
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
                String formatted = parts[0] + " - " + parts[1] + " (" + parts[2] + ")";
                if (formatted.equals(spaceInfo)) {
                    lines.add(parts[0] + "," + parts[1] + "," + parts[2] + "," + status);
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
