package com.yorku.parking.gui;

import com.formdev.flatlaf.FlatIntelliJLaf;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;


public class AdminPanel extends JFrame {
    private JTextArea logArea;
    private JComboBox<String> userDropdown, parkingLotDropdown, parkingSpaceDropdown;
    private JButton disableUserButton, enableUserButton, deleteUserButton,
                    updateRatesButton, generateManagerButton,
                    addLotButton, disableLotButton, enableLotButton,
                    disableSpaceButton, enableSpaceButton;

    public AdminPanel(boolean isSuperManager) {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        UIManager.put("Button.arc", 20);
        UIManager.put("Component.arc", 15);
        UIManager.put("ProgressBar.arc", 15);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));

        setTitle("Admin Panel - " + (isSuperManager ? "Super Manager" : "Manager"));
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new MigLayout("fill, wrap 2", "[grow][fill, 300!]", "[grow]"));

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, "span 2, grow, wrap");

        JPanel panel = new JPanel(new MigLayout("wrap 1, fillx, insets 10", "[fill]"));
        panel.setBackground(Color.WHITE);

        if (isSuperManager) {
            panel.add(new JLabel("User Management"), "gapy 10");
            userDropdown = new JComboBox<>(loadUsers());
            panel.add(userDropdown);

            disableUserButton = new JButton("Disable User");
            enableUserButton = new JButton("Enable User");
            deleteUserButton = new JButton("Delete User");
            updateRatesButton = new JButton("Update Parking Rates");
            generateManagerButton = new JButton("Generate Manager Accounts");

            panel.add(disableUserButton);
            panel.add(enableUserButton);
            panel.add(deleteUserButton);
            panel.add(updateRatesButton);
            panel.add(generateManagerButton);

            disableUserButton.addActionListener(e -> updateUser("disabled"));
            enableUserButton.addActionListener(e -> updateUser("enabled"));
            deleteUserButton.addActionListener(e -> deleteUser());
            updateRatesButton.addActionListener(e -> updateRates());
            generateManagerButton.addActionListener(e -> generateManagerAccounts());
        }

        panel.add(new JLabel("Parking Lot Management"), "gapy 10");
        parkingLotDropdown = new JComboBox<>(loadParkingLots());
        panel.add(parkingLotDropdown);

        addLotButton = new JButton("Add Parking Lot");
        enableLotButton = new JButton("Enable Parking Lot");
        disableLotButton = new JButton("Disable Parking Lot");

        panel.add(addLotButton);
        panel.add(enableLotButton);
        panel.add(disableLotButton);

        addLotButton.addActionListener(e -> addParkingLot());
        enableLotButton.addActionListener(e -> updateLotStatus("enabled"));
        disableLotButton.addActionListener(e -> updateLotStatus("disabled"));

        panel.add(new JLabel("Parking Space Management"), "gapy 10");
        parkingSpaceDropdown = new JComboBox<>(loadParkingSpaces());
        panel.add(parkingSpaceDropdown);

        enableSpaceButton = new JButton("Enable Parking Space");
        disableSpaceButton = new JButton("Disable Parking Space");

        panel.add(enableSpaceButton);
        panel.add(disableSpaceButton);

        enableSpaceButton.addActionListener(e -> updateSpaceStatus("enabled"));
        disableSpaceButton.addActionListener(e -> updateSpaceStatus("disabled"));

        add(panel);
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
            logArea.append("User " + selectedUser + " updated to " + status + "\n");
        } catch (IOException e) {
            logArea.append("Error writing user file.\n");
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
            logArea.append("Error saving user file.\n");
        }
    }

    private void updateRates() {
        String input = JOptionPane.showInputDialog(this, "Enter rates (Student,Faculty,Staff,Visitor):");
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
            String input = JOptionPane.showInputDialog(this, "Enter number of accounts to generate:");
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
        String name = JOptionPane.showInputDialog(this, "Enter Parking Lot name:");
        if (name != null && !name.isEmpty()) {
            logArea.append("Parking Lot '" + name + "' added.\n");
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
            logArea.append("Space " + space + " marked as " + status + ".\n");
        }
    }
}
