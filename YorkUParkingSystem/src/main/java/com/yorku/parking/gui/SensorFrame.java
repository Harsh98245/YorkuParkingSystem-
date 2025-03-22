package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class SensorFrame extends JFrame {
    private JTextArea outputArea;
    private JButton refreshButton;

    public SensorFrame() {
        setTitle("Parking Space Sensor Data");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        refreshButton = new JButton("Refresh Sensor Data");
        refreshButton.addActionListener(e -> loadSensorData());

        add(scrollPane, BorderLayout.CENTER);
        add(refreshButton, BorderLayout.SOUTH);

        loadSensorData(); // Initial load
        setVisible(true);
    }

    private void loadSensorData() {
        outputArea.setText("");  // Clear previous content
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String id = parts[0];
                    String location = parts[1];
                    String lot = parts[2];
                    String status = parts[3];

                    // Simulated sensor reading
                    String carInfo = status.equalsIgnoreCase("Occupied") ? "Car Detected (Plate: ABC1234)" : "No car";

                    outputArea.append("Space ID: " + id + "\n");
                    outputArea.append("Location: " + location + "\n");
                    outputArea.append("Lot: " + lot + "\n");
                    outputArea.append("Status: " + status + "\n");
                    outputArea.append("Sensor: " + carInfo + "\n\n");
                }
            }
        } catch (IOException e) {
            outputArea.setText("Error loading sensor data.");
            e.printStackTrace();
        }
    }
}
