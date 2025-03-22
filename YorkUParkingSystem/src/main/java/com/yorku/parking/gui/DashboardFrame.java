package com.yorku.parking.gui;

import javax.swing.*;

import com.yorku.parking.utils.SessionManager;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import net.miginfocom.swing.MigLayout;
import com.yorku.parking.utils.BookingUtil;

public class DashboardFrame extends JFrame {
    private String username;
    private boolean isManager;
    private JTextArea bookingInfoArea;
    private JComboBox<String> parkingLotDropdown;
    private JTextArea sensorArea;

    public DashboardFrame(String username, boolean isManager) {
        this.username = username;
        this.isManager = isManager;

        setTitle("Dashboard - " + (isManager ? "Manager" : "Client"));
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new MigLayout("wrap 2", "[][grow]", "[][grow][]"));

        bookingInfoArea = new JTextArea(10, 50);
        bookingInfoArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(bookingInfoArea);
        panel.add(new JLabel("=== Your Bookings ==="), "span");
        panel.add(scrollPane, "span, growx");

        JButton cancelButton = new JButton("Cancel Booking");
        JButton extendButton = new JButton("Extend Booking");
        JButton refreshButton = new JButton("Refresh");
        JButton notifyButton = new JButton("Notifications");
        JButton navButton = new JButton("Navigate to My Spot");

        cancelButton.addActionListener(e -> cancelBooking());
        extendButton.addActionListener(e -> extendBooking());
        refreshButton.addActionListener(e -> loadBookings());
        notifyButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "No new notifications."));
        navButton.addActionListener(e -> new NavigationFrame(username));

        panel.add(cancelButton);
        panel.add(extendButton);
        panel.add(refreshButton);
        panel.add(notifyButton);
        panel.add(navButton, "span, growx");

        if (isManager) {
            panel.add(new JLabel("=== Manage Parking Lots ==="), "span");
            parkingLotDropdown = new JComboBox<>(loadParkingLots());
            JButton enableLotButton = new JButton("Enable Lot");
            JButton disableLotButton = new JButton("Disable Lot");

            enableLotButton.addActionListener(e -> setLotStatus("Available"));
            disableLotButton.addActionListener(e -> setLotStatus("Maintenance"));

            panel.add(new JLabel("Select Lot:"));
            panel.add(parkingLotDropdown, "growx");
            panel.add(enableLotButton);
            panel.add(disableLotButton);

            panel.add(new JLabel("=== Live Sensor States ==="), "span");
            sensorArea = new JTextArea(6, 50);
            sensorArea.setEditable(false);
            JScrollPane sensorScrollPane = new JScrollPane(sensorArea);
            JButton refreshSensors = new JButton("Refresh Sensors");
            refreshSensors.addActionListener(e -> loadSensorData());

            panel.add(sensorScrollPane, "span, growx");
            panel.add(refreshSensors, "span, right");
        }

        add(panel);
        loadBookings();
        if (isManager) loadSensorData();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                SessionManager.logout(username);
            }
        });

        setVisible(true);
    }

    private void loadBookings() {
        bookingInfoArea.setText(BookingUtil.getBookingsForUser(username));
    }

    private void cancelBooking() {
        JOptionPane.showMessageDialog(this, "Cancel feature coming soon.");
    }

    private void extendBooking() {
        JOptionPane.showMessageDialog(this, "Extend feature coming soon.");
    }

    private String[] loadParkingLots() {
        Set<String> lots = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    lots.add(parts[2].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lots.toArray(new String[0]);
    }

    private void setLotStatus(String status) {
        String selectedLot = (String) parkingLotDropdown.getSelectedItem();
        if (selectedLot == null) return;
        try {
            File inputFile = new File("src/main/resources/parking_spaces.csv");
            File tempFile = new File("src/main/resources/temp.csv");

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4 && parts[2].equals(selectedLot)) {
                    parts[3] = status;
                    line = String.join(",", parts);
                }
                writer.write(line);
                writer.newLine();
            }
            reader.close();
            writer.close();
            inputFile.delete();
            tempFile.renameTo(inputFile);
            JOptionPane.showMessageDialog(this, "Updated lot: " + selectedLot);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadSensorData() {
        sensorArea.setText("");
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    sensorArea.append("[" + parts[0] + "] Location: " + parts[1] + " | Lot: " + parts[2] + " | Status: " + parts[3] + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 
