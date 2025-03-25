package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;

public class ManagerDashboardFrame extends JFrame {
    private JComboBox<String> lotDropdown;
    private JComboBox<String> spaceDropdown;
    private JTextArea logArea;

    public ManagerDashboardFrame() {
        setTitle("Manager Dashboard");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Manage Lots & Spaces"));

        lotDropdown = new JComboBox<>(loadLots());
        spaceDropdown = new JComboBox<>(loadSpaces());

        JButton toggleLotButton = new JButton("Enable/Disable Lot");
        JButton toggleSpaceButton = new JButton("Enable/Disable Space");

        toggleLotButton.addActionListener(this::toggleLot);
        toggleSpaceButton.addActionListener(this::toggleSpace);

        topPanel.add(new JLabel("Select Parking Lot:"));
        topPanel.add(lotDropdown);
        topPanel.add(toggleLotButton);

        topPanel.add(new JLabel("Select Parking Space:"));
        topPanel.add(spaceDropdown);
        topPanel.add(toggleSpaceButton);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Current Status"));

        JButton refreshButton = new JButton("Refresh View");
        refreshButton.addActionListener(e -> refreshStatus());

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(refreshButton, BorderLayout.SOUTH);

        refreshStatus();
    }

    private String[] loadLots() {
        Set<String> lots = new TreeSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    lots.add(parts[0]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lots.toArray(new String[0]);
    }

    private String[] loadSpaces() {
        List<String> spaces = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    spaces.add(parts[0] + " - " + parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return spaces.toArray(new String[0]);
    }

    private void toggleLot(ActionEvent e) {
        String selectedLot = (String) lotDropdown.getSelectedItem();
        if (selectedLot != null) {
            toggleStatus(true, selectedLot);
        }
    }

    private void toggleSpace(ActionEvent e) {
        String selectedSpace = (String) spaceDropdown.getSelectedItem();
        if (selectedSpace != null) {
            String[] parts = selectedSpace.split(" - ");
            toggleStatus(false, parts[1]);
        }
    }

    private void toggleStatus(boolean isLot, String identifier) {
        File input = new File("src/main/resources/parking_spaces.csv");
        File temp = new File("src/main/resources/temp.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(input));
             PrintWriter writer = new PrintWriter(new FileWriter(temp))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    if ((isLot && parts[0].equals(identifier)) || (!isLot && parts[1].equals(identifier))) {
                        String newStatus = parts[2].equals("Available") ? "Disabled" : "Available";
                        writer.println(parts[0] + "," + parts[1] + "," + newStatus);
                    } else {
                        writer.println(line);
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        input.delete();
        temp.renameTo(input);
        refreshStatus();
    }

    private void refreshStatus() {
        logArea.setText("=== Parking Space Status ===\n");
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                logArea.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}