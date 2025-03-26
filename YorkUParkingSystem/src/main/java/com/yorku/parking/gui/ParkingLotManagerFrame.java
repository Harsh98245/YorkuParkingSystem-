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

    private java.util.List<String> lotList = Arrays.asList("LotA", "LotB", "LotC", "LotD", "LotE");
    private java.util.List<String> spaceList = new ArrayList<>();

    public ParkingLotManagerFrame() {
        setTitle("Parking Lot Manager");
        setSize(600, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        // === Lot Dropdown ===
        panel.add(new JLabel("Select Parking Lot:"));
        lotDropdown = new JComboBox<>(lotList.toArray(new String[0]));
        panel.add(lotDropdown);

        enableLotBtn = new JButton("Enable Lot");
        disableLotBtn = new JButton("Disable Lot");
        panel.add(enableLotBtn);
        panel.add(disableLotBtn);

        // === Space Dropdown (depends on lot) ===
        panel.add(new JLabel("Select Parking Space:"));
        spaceDropdown = new JComboBox<>();
        panel.add(spaceDropdown);
        updateSpacesForSelectedLot();

        enableSpaceBtn = new JButton("Enable Space");
        disableSpaceBtn = new JButton("Disable Space");
        panel.add(enableSpaceBtn);
        panel.add(disableSpaceBtn);

        lotDropdown.addActionListener(e -> updateSpacesForSelectedLot());

        enableLotBtn.addActionListener(e -> updateLotStatus("Available"));
        disableLotBtn.addActionListener(e -> updateLotStatus("Disabled"));
        enableSpaceBtn.addActionListener(e -> updateSpaceStatus("Available"));
        disableSpaceBtn.addActionListener(e -> updateSpaceStatus("Maintenance"));

        add(panel);
        setVisible(true);
    }

    private void updateSpacesForSelectedLot() {
        String selectedLot = (String) lotDropdown.getSelectedItem();
        if (selectedLot == null) return;

        spaceList.clear();
        spaceDropdown.removeAllItems();

        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4 && parts[2].equals(selectedLot)) {
                    String spaceName = parts[0] + " - " + parts[1]; // ID - Label
                    spaceList.add(spaceName);
                    spaceDropdown.addItem(spaceName);
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
                if (parts.length >= 4 && parts[2].equals(selectedLot)) {
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
            JOptionPane.showMessageDialog(this, "All spaces in " + selectedLot + " set to " + status);
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateSpacesForSelectedLot(); // refresh dropdown
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
                if (parts.length >= 4 && parts[0].equals(selectedID)) {
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
            JOptionPane.showMessageDialog(this, "Space " + selected + " set to " + status);
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateSpacesForSelectedLot(); // refresh dropdown
    }
}