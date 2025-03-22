package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class ParkingLotManagerFrame extends JFrame {
    private JComboBox<String> lotDropdown;
    private JComboBox<String> spaceDropdown;
    private JButton enableLotBtn, disableLotBtn, enableSpaceBtn, disableSpaceBtn;

    private java.util.List<String> lotList = new ArrayList<>();
    private java.util.List<String> spaceList = new ArrayList<>();

    public ParkingLotManagerFrame() {
        setTitle("Parking Lot Manager");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        loadLots();
        loadSpaces();

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        // Lot control
        panel.add(new JLabel("Select Parking Lot:"));
        lotDropdown = new JComboBox<>(lotList.toArray(new String[0]));
        panel.add(lotDropdown);

        enableLotBtn = new JButton("Enable Lot");
        disableLotBtn = new JButton("Disable Lot");
        panel.add(enableLotBtn);
        panel.add(disableLotBtn);

        // Space control
        panel.add(new JLabel("Select Parking Space:"));
        spaceDropdown = new JComboBox<>(spaceList.toArray(new String[0]));
        panel.add(spaceDropdown);

        enableSpaceBtn = new JButton("Enable Space");
        disableSpaceBtn = new JButton("Disable Space");
        panel.add(enableSpaceBtn);
        panel.add(disableSpaceBtn);

        add(panel);

        enableLotBtn.addActionListener(e -> updateLotStatus("Enabled"));
        disableLotBtn.addActionListener(e -> updateLotStatus("Disabled"));
        enableSpaceBtn.addActionListener(e -> updateSpaceStatus("Available"));
        disableSpaceBtn.addActionListener(e -> updateSpaceStatus("Maintenance"));

        setVisible(true);
    }

    private void loadLots() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            Set<String> lots = new LinkedHashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    lots.add(parts[2]); // Lot name
                }
            }
            lotList.addAll(lots);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSpaces() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    spaceList.add(parts[0] + " - " + parts[1]); // ID + location
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLotStatus(String status) {
        String selectedLot = (String) lotDropdown.getSelectedItem();
        if (selectedLot == null) return;

        java.util.List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[2].equals(selectedLot)) {
                    parts[3] = status;
                    line = String.join(",", parts);
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter("src/main/resources/parking_spaces.csv"))) {
            for (String l : lines) writer.println(l);
            JOptionPane.showMessageDialog(this, "Lot updated successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateSpaceStatus(String status) {
        String selected = (String) spaceDropdown.getSelectedItem();
        if (selected == null) return;

        String selectedID = selected.split(" - ")[0];

        java.util.List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 1 && parts[0].equals(selectedID)) {
                    parts[3] = status;
                    line = String.join(",", parts);
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter("src/main/resources/parking_spaces.csv"))) {
            for (String l : lines) writer.println(l);
            JOptionPane.showMessageDialog(this, "Space updated successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
