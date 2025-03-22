package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;

public class AdminPanel extends JFrame {
    private JTextArea logArea;
    private JComboBox<String> userDropdown;
    private JComboBox<String> parkingLotDropdown;
    private JComboBox<String> parkingSpaceDropdown;
    private JButton disableUserButton, enableUserButton, deleteUserButton, 
                    updateRatesButton, generateManagerButton,
                    addLotButton, disableLotButton, enableLotButton,
                    disableSpaceButton, enableSpaceButton;

    public AdminPanel(boolean isSuperManager) {
        setTitle("Admin Panel - Manager");
        setSize(750, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        JPanel controlPanel = new JPanel(new GridLayout(15, 1, 5, 5));

        userDropdown = new JComboBox<>(loadUsers());
        controlPanel.add(userDropdown);

        disableUserButton = new JButton("Disable User");
        enableUserButton = new JButton("Enable User");
        deleteUserButton = new JButton("Delete User");
        updateRatesButton = new JButton("Update Parking Rates");
        generateManagerButton = new JButton("Generate Manager Accounts");

        if (isSuperManager) {
            controlPanel.add(disableUserButton);
            controlPanel.add(enableUserButton);
            controlPanel.add(deleteUserButton);
            controlPanel.add(updateRatesButton);
            controlPanel.add(generateManagerButton);
        }

        controlPanel.add(new JLabel("--- Parking Lot Management ---"));
        parkingLotDropdown = new JComboBox<>(loadParkingLots());
        controlPanel.add(parkingLotDropdown);

        addLotButton = new JButton("Add New Parking Lot");
        enableLotButton = new JButton("Enable Parking Lot");
        disableLotButton = new JButton("Disable Parking Lot");
        controlPanel.add(addLotButton);
        controlPanel.add(enableLotButton);
        controlPanel.add(disableLotButton);

        controlPanel.add(new JLabel("--- Parking Space Management ---"));
        parkingSpaceDropdown = new JComboBox<>(loadParkingSpaces());
        controlPanel.add(parkingSpaceDropdown);
        enableSpaceButton = new JButton("Enable Parking Space");
        disableSpaceButton = new JButton("Disable Parking Space");
        controlPanel.add(enableSpaceButton);
        controlPanel.add(disableSpaceButton);

        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        // Button actions (restricted by role)
        if (isSuperManager) {
            disableUserButton.addActionListener(e -> updateUser("disabled"));
            enableUserButton.addActionListener(e -> updateUser("enabled"));
            deleteUserButton.addActionListener(e -> deleteUser());
            updateRatesButton.addActionListener(e -> updateRates());
            generateManagerButton.addActionListener(e -> generateManagerAccounts());
        }

        addLotButton.addActionListener(e -> addParkingLot());
        enableLotButton.addActionListener(e -> updateLotStatus("enabled"));
        disableLotButton.addActionListener(e -> updateLotStatus("disabled"));
        enableSpaceButton.addActionListener(e -> updateSpaceStatus("enabled"));
        disableSpaceButton.addActionListener(e -> updateSpaceStatus("disabled"));

        setVisible(true);
    }

    private String[] loadUsers() {
        List<String> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 1) {
                    users.add(parts[0]);
                }
            }
        } catch (IOException e) {
            logArea.append("Error loading users.\n");
        }
        return users.toArray(new String[0]);
    }

    private String[] loadParkingLots() {
        return new String[]{"Lot A", "Lot B"};
    }

    private String[] loadParkingSpaces() {
        String[] spaces = new String[100];
        for (int i = 0; i < 100; i++) {
            spaces[i] = "Space-" + (i + 1);
        }
        return spaces;
    }

    private void updateUser(String status) {
        String selectedUser = (String) userDropdown.getSelectedItem();
        if (selectedUser == null) return;

        List<String> updated = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(selectedUser)) {
                    updated.add(parts[0] + "," + parts[1] + "," + status);
                } else {
                    updated.add(line);
                }
            }
        } catch (IOException e) {
            logArea.append("Error updating user.\n");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/users.csv"))) {
            for (String entry : updated) {
                writer.write(entry);
                writer.newLine();
            }
            logArea.append("User " + selectedUser + " updated to " + status + ".\n");
        } catch (IOException e) {
            logArea.append("Error saving user data.\n");
        }
    }

    private void deleteUser() {
        String selectedUser = (String) userDropdown.getSelectedItem();
        if (selectedUser == null) return;

        List<String> retained = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(selectedUser + ",")) {
                    retained.add(line);
                }
            }
        } catch (IOException e) {
            logArea.append("Error deleting user.\n");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/users.csv"))) {
            for (String entry : retained) {
                writer.write(entry);
                writer.newLine();
            }
            logArea.append("User " + selectedUser + " deleted.\n");
        } catch (IOException e) {
            logArea.append("Error saving users.\n");
        }
    }

    private void updateRates() {
        String input = JOptionPane.showInputDialog(this, "Enter new rates (Student,Faculty,Staff,Visitor):");
        if (input != null && input.matches("\\d+(,\\d+){3}")) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/rates.csv"))) {
                writer.write(input);
                logArea.append("Rates updated.\n");
            } catch (IOException e) {
                logArea.append("Error saving rates.\n");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid format. Use: 5,8,10,15");
        }
    }

    private void generateManagerAccounts() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("src/main/resources/users.csv", true))) {
            String input = JOptionPane.showInputDialog(this, "Enter number of manager accounts to generate:");
            if (input == null) return;
            int count = Integer.parseInt(input);
            for (int i = 0; i < count; i++) {
                String username = "manager" + UUID.randomUUID().toString().substring(0, 5);
                String password = UUID.randomUUID().toString().substring(0, 8);
                bw.write(username + "," + password + ",enabled");
                bw.newLine();
                logArea.append("Generated: " + username + " / " + password + "\n");
            }
        } catch (IOException | NumberFormatException e) {
            logArea.append("Error generating accounts.\n");
        }
    }

    private void addParkingLot() {
        String name = JOptionPane.showInputDialog(this, "Enter new Parking Lot name:");
        if (name != null && !name.isEmpty()) {
            logArea.append("Parking Lot '" + name + "' added with 100 spaces.\n");
        }
    }

    private void updateLotStatus(String status) {
        String lot = (String) parkingLotDropdown.getSelectedItem();
        if (lot != null) {
            logArea.append("Lot " + lot + " marked as " + status + ".\n");
        }
    }

    private void updateSpaceStatus(String status) {
        String space = (String) parkingSpaceDropdown.getSelectedItem();
        if (space != null) {
            logArea.append("Parking Space " + space + " marked as " + status + ".\n");
        }
    }
}
