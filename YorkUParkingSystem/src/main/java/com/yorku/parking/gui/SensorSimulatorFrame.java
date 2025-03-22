package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SensorSimulatorFrame extends JFrame {
    private JTextArea outputArea;
    private JButton simulateButton;

    public SensorSimulatorFrame() {
        setTitle("Sensor Simulator");
        setSize(550, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        simulateButton = new JButton("Simulate Sensors");
        simulateButton.addActionListener(e -> simulateSensors());

        add(scrollPane, BorderLayout.CENTER);
        add(simulateButton, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void simulateSensors() {
        List<String> lines = new ArrayList<>();
        Random rand = new Random();
        outputArea.setText("");

        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String spaceId = parts[0];
                    String location = parts[1];
                    String lot = parts[2];
                    String status = parts[3];

                    if (status.equalsIgnoreCase("Available")) {
                        boolean occupied = rand.nextBoolean();

                        if (occupied) {
                            String fakePlate = generateFakePlate();
                            status = "Occupied:" + fakePlate;
                            outputArea.append("Sensor: " + spaceId + " → Car Detected: " + fakePlate + "\n");
                        } else {
                            status = "Available";
                            outputArea.append("Sensor: " + spaceId + " → No car detected.\n");
                        }

                        lines.add(spaceId + "," + location + "," + lot + "," + status);
                    } else {
                        // Keep Maintenance and already Occupied spaces unchanged
                        lines.add(line);
                        outputArea.append("Sensor: " + spaceId + " → Skipped (" + status + ")\n");
                    }
                }
            }
        } catch (IOException e) {
            outputArea.append("Error reading parking_spaces.csv\n");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/parking_spaces.csv"))) {
            for (String updatedLine : lines) {
                writer.write(updatedLine);
                writer.newLine();
            }
            outputArea.append("\n✅ Sensor simulation complete. Data saved.");
        } catch (IOException e) {
            outputArea.append("Error writing parking_spaces.csv\n");
        }
    }

    private String generateFakePlate() {
        Random r = new Random();
        return "CAR" + (1000 + r.nextInt(9000));
    }
}
