package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class NavigationFrame extends JFrame {
    private JTextField spaceIdField;
    private JTextArea outputArea;
    private JButton navigateButton;
    private JButton refreshButton;
    private JButton backButton;

    public NavigationFrame(String username) {
        setTitle("Navigation - " + username);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Enter Parking Space ID:"));
        spaceIdField = new JTextField(10);
        inputPanel.add(spaceIdField);

        navigateButton = new JButton("Find Route");
        navigateButton.addActionListener(e -> navigateToSpace());
        inputPanel.add(navigateButton);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> spaceIdField.setText(""));
        inputPanel.add(refreshButton);

        backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(e -> {
            dispose();
            new DashboardFrame(username, false);
        });
        inputPanel.add(backButton);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        setVisible(true);
    }

    private void navigateToSpace() {
        String id = spaceIdField.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Parking Space ID.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            boolean found = false;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4 && parts[0].equalsIgnoreCase(id)) {
                    found = true;
                    String location = parts[1];
                    String lot = parts[2];
                    String status = parts[3];

                    outputArea.setText("📍 Navigation Details\n---------------------------\n");
                    outputArea.append("🔹 Space ID: " + parts[0] + "\n");
                    outputArea.append("📌 Lot: " + lot + "\n");
                    outputArea.append("📍 Location: " + location + "\n");
                    outputArea.append("🚦 Status: " + status + "\n\n");

                    outputArea.append("➡ Directions:\n");
                    outputArea.append("1. Enter campus gate 3\n");
                    outputArea.append("2. Follow signs to Lot " + lot + "\n");
                    outputArea.append("3. Turn left at checkpoint near building " + location + "\n");
                    outputArea.append("4. Find your spot: " + parts[0] + "\n");
                    break;
                }
            }

            if (!found) {
                outputArea.setText("❌ Parking Space ID not found.");
            }

        } catch (IOException e) {
            outputArea.setText("⚠️ Error reading parking_spaces.csv");
            e.printStackTrace();
        }
    }
}
