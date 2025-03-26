package com.yorku.parking.gui;

import javax.swing.*;

import com.yorku.parking.utils.SessionManager;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import net.miginfocom.swing.MigLayout;
import com.yorku.parking.utils.BookingUtil;
import java.util.List;
import java.util.ArrayList;

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
       // panel.add(new JLabel("=== Your Bookings ==="), "span");
        panel.add(scrollPane, "span, growx");

        // Booking-related buttons
        JButton bookButton = new JButton("Book Parking");
        JButton cancelButton = new JButton("Cancel Booking");
        JButton extendButton = new JButton("Extend Booking");
        JButton backButton = new JButton("Back");
        
        JButton refreshButton = new JButton("Refresh");
        JButton notifyButton = new JButton("Notifications");
        JButton navButton = new JButton("Navigate to My Spot");
        
        bookButton.addActionListener(e -> {
            dispose();
            new BookingFrame(username);  // Go to booking GUI
        });
        backButton.addActionListener(e -> goback());
        cancelButton.addActionListener(e -> cancelBooking());
        extendButton.addActionListener(e -> extendBooking());
        refreshButton.addActionListener(e -> loadBookings());
        notifyButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "No new notifications."));
        navButton.addActionListener(e -> new NavigationFrame(username));


        if (!isManager) {
            panel.add(bookButton, "span, growx");
            panel.add(backButton, "span, growx");
        }

        // Only show cancel/extend buttons if booking exists
        List<String> bookingsList = BookingUtil.getBookingsForUser(username);
        String bookingText = String.join("\n", bookingsList).trim();

        if (!bookingsList.isEmpty()) { // Means user has at least one booking
            panel.add(cancelButton);
            panel.add(extendButton);
        }


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

    private void goback() {
		dispose();
		new WelcomeFrame();
	}

	private void loadBookings() {
        List<String> bookings = BookingUtil.getBookingsForUser(username);
        StringBuilder sb = new StringBuilder("=== Your Bookings ===\n");
        for (String booking : bookings) {
            sb.append(booking).append("\n");
        }
        bookingInfoArea.setText(sb.toString());
    }

    private void cancelBooking() {
        java.util.List<String> bookings = BookingUtil.getBookingsForUser(username);
        if (bookings.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No bookings to cancel.");
            return;
        }

        String[] options = bookings.toArray(new String[0]);
        JComboBox<String> bookingDropdown = new JComboBox<>(options);
        int result = JOptionPane.showConfirmDialog(this, bookingDropdown, 
                "Select a booking to cancel", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String selectedBooking = (String) bookingDropdown.getSelectedItem();
            if (selectedBooking != null) {
                String parkingSpot = selectedBooking.split(",")[2].trim();  // Extract PSxxx - A1
                BookingUtil.removeBookingLine(selectedBooking); // from bookings.csv
                BookingUtil.updateSpaceStatus(parkingSpot, "Available"); // in parking_spaces.csv
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully.");
                loadBookings(); // refresh GUI
            }
        }
    }



    private void updateSpaceStatus(String spaceId, String status) {
        File inputFile = new File("src/main/resources/parking_spaces.csv");
        File tempFile = new File("src/main/resources/parking_spaces_temp.csv");

        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    System.out.println("Comparing: " + parts[0].trim() + " vs " + spaceId.trim());
                    if (parts[0].trim().equalsIgnoreCase(spaceId.trim())) {
                        writer.println(parts[0] + "," + parts[1] + "," + parts[2] + "," + status);
                        updated = true;
                        continue;
                    }
                }
                writer.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        inputFile.delete();
        tempFile.renameTo(inputFile);

        if (!updated) {
            System.out.println("⚠ Space not found: " + spaceId);
            JOptionPane.showMessageDialog(this, "❌ Could not update space status for: " + spaceId);
        }
    }
    private void extendBooking() {
        List<String> bookings = BookingUtil.getBookingsForUser(username);
        if (bookings.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No bookings to extend.");
            return;
        }

        String[] options = bookings.toArray(new String[0]);
        JComboBox<String> bookingDropdown = new JComboBox<>(options);

        int result = JOptionPane.showConfirmDialog(this, bookingDropdown,
                "Select booking to extend", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String selectedBooking = (String) bookingDropdown.getSelectedItem();
            if (selectedBooking == null) return;

            // Generate time slots from 1:00 PM to 11:30 PM
            String[] timeOptions = new String[22];
            int index = 0;
            for (int hour = 13; hour <= 23; hour++) {
                timeOptions[index++] = String.format("%d:00 PM", hour % 12 == 0 ? 12 : hour % 12);
                timeOptions[index++] = String.format("%d:30 PM", hour % 12 == 0 ? 12 : hour % 12);
            }

            JComboBox<String> timeDropdown = new JComboBox<>(timeOptions);
            int timeResult = JOptionPane.showConfirmDialog(this, timeDropdown,
                    "Select new end time:", JOptionPane.OK_CANCEL_OPTION);

            if (timeResult == JOptionPane.OK_OPTION) {
                String selectedTime = (String) timeDropdown.getSelectedItem();
                if (selectedTime == null) return;

                try {
                    // Extract only the space and duration from selectedBooking
                    String[] parts = selectedBooking.split(",");
                    if (parts.length < 4) {
                        JOptionPane.showMessageDialog(this, "Invalid booking format.");
                        return;
                    }

                    int currentDuration = Integer.parseInt(parts[3].trim());

                    // Convert selectedTime to hour in 24-hour format
                    String[] timeParts = selectedTime.split(":");
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = timeParts[1].startsWith("30") ? 30 : 0;
                    if (selectedTime.contains("PM") && hour != 12) hour += 12;
                    if (selectedTime.contains("AM") && hour == 12) hour = 0;

                    int newDuration = (hour * 60 + minute) / 60;  // Convert to hour unit
                    int extraHours = newDuration - currentDuration;

                    if (extraHours <= 0) {
                        JOptionPane.showMessageDialog(this, "End time must be later than current booking.");
                        return;
                    }

                    // Update CSV
                    File inputFile = new File("src/main/resources/bookings.csv");
                    File tempFile = new File("src/main/resources/bookings_temp.csv");

                    boolean updated = false;

                    try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                         PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.equals(selectedBooking)) {
                                writer.println(parts[0] + "," + parts[1] + "," + parts[2] + "," + newDuration);
                                updated = true;
                            } else {
                                writer.println(line);
                            }
                        }
                    }

                    if (updated) {
                        inputFile.delete();
                        tempFile.renameTo(inputFile);
                        JOptionPane.showMessageDialog(this, "Booking extended to " + selectedTime);
                        loadBookings();
                    } else {
                        JOptionPane.showMessageDialog(this, "Booking not found.");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error while extending booking.");
                }
            }
        }
    }


    private String[] generateTimeOptions() {
    	java.util.List<String> times = new java.util.ArrayList<>();
        int hour = 0;
        int minute = 0;

        while (hour < 24) {
            String ampm = hour < 12 ? "AM" : "PM";
            int displayHour = (hour == 0 || hour == 12) ? 12 : hour % 12;

            times.add(String.format("%d:%02d %s", displayHour, minute, ampm));

            minute += 30;
            if (minute == 60) {
                minute = 0;
                hour++;
            }
        }

        return times.toArray(new String[0]);
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