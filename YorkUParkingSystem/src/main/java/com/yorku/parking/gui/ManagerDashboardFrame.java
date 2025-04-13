package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.List;

public class ManagerDashboardFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> lotDropdown;
    private JComboBox<String> spaceDropdown;
    private JTextArea logArea;
    private JPanel loginPanel;
    private JPanel dashboardPanel;

    public ManagerDashboardFrame() {
        setTitle("Manager Dashboard");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        buildLoginPanel();
        setVisible(true);
    }

    private void buildLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(15);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(15);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(this::handleLogin);

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> {
            dispose();
            new WelcomeFrame();
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(loginBtn, gbc);

        gbc.gridx = 1;
        loginPanel.add(backBtn, gbc);

        add(loginPanel, BorderLayout.CENTER);
    }

    private void buildDashboardPanel() {
        dashboardPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(2, 3, 10, 10));
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

        JButton backButton = new JButton("Main Menu");
        backButton.addActionListener(e -> {
            dispose();
            new WelcomeFrame();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(refreshButton);
        bottomPanel.add(backButton);

        dashboardPanel.add(topPanel, BorderLayout.NORTH);
        dashboardPanel.add(scrollPane, BorderLayout.CENTER);
        dashboardPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.");
            return;
        }

        File file = new File("src/main/resources/users.csv"); // Update the path if necessary
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean loginSuccess = false;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    loginSuccess = true;
                    break;
                }
            }

            if (loginSuccess) {
                JOptionPane.showMessageDialog(this, "Login successful!");
                remove(loginPanel);
                buildDashboardPanel();
                add(dashboardPanel, BorderLayout.CENTER);
                revalidate();
                repaint();
                refreshStatus();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error reading manager credentials.");
            ex.printStackTrace();
        }
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
        if (logArea == null) return;
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