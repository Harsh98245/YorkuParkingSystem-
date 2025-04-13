package com.yorku.parking.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.yorku.parking.service.BookingService;
import com.yorku.parking.service.ParkingService;
import com.yorku.parking.utils.SessionManager;

import net.miginfocom.swing.MigLayout;

public class DashboardFrame extends JFrame {
    private String username;
    private boolean isManager;
    private JTextArea bookingInfoArea;
    private JComboBox<String> parkingLotDropdown;
    private JTextArea sensorArea;
    private BookingService bookingService;
    private ParkingService parkingService;

    public DashboardFrame(String username, boolean isManager) {
        this.username = username;
        this.isManager = isManager;
        this.bookingService = new BookingService();
        this.parkingService = new ParkingService();

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Dashboard - " + (isManager ? "Manager" : "Client"));
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new MigLayout("wrap 2", "[][grow]", "[][grow][]"));

        bookingInfoArea = new JTextArea(10, 50);
        bookingInfoArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(bookingInfoArea);
        panel.add(scrollPane, "span, growx");

        setupButtons(panel);

        if (isManager) {
            setupManagerControls(panel);
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

    private void setupButtons(JPanel panel) {
        JButton bookButton = new JButton("Book Parking");
        JButton cancelButton = new JButton("Cancel Booking");
        JButton extendButton = new JButton("Extend Booking");
        JButton backButton = new JButton("Back");
        JButton refreshButton = new JButton("Refresh");
        JButton notifyButton = new JButton("Notifications");
        JButton navButton = new JButton("Navigate to My Spot");

        bookButton.addActionListener(e -> {
            dispose();
            new BookingFrame(username);
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

        List<String> bookingsList = bookingService.getBookingsForUser(username);
        if (!bookingsList.isEmpty()) {
            panel.add(cancelButton);
            panel.add(extendButton);
        }

        panel.add(refreshButton);
        panel.add(notifyButton);
        panel.add(navButton, "span, growx");
    }

    private void setupManagerControls(JPanel panel) {
        panel.add(new JLabel("=== Manage Parking Lots ==="), "span");
        parkingLotDropdown = new JComboBox<>(parkingService.loadParkingLots());
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

    private void goback() {
        dispose();
        new WelcomeFrame();
    }

    private void loadBookings() {
        List<String> bookings = bookingService.getBookingsForUser(username);
        StringBuilder sb = new StringBuilder("=== Your Active Bookings ===\n\n");
        if (bookings.isEmpty()) {
            sb.append("No active bookings found.\n");
        } else {
            for (String booking : bookings) {
                String[] parts = booking.split(",");
                if (parts.length >= 5) {
                    sb.append("Booking ID: ").append(parts[0]).append("\n");
                    sb.append("License Plate: ").append(parts[2]).append("\n");
                    sb.append("Parking Space: ").append(parts[3]).append("\n");
                    sb.append("Duration: ").append(parts[4]).append(" hours\n");
                    sb.append("-------------------\n");
                }
            }
        }
        bookingInfoArea.setText(sb.toString());
    }

    private void cancelBooking() {
        List<String> bookings = bookingService.getBookingsForUser(username);
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
            if (selectedBooking != null && bookingService.cancelBooking(selectedBooking)) {
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully.");
                loadBookings();
            } else {
                JOptionPane.showMessageDialog(this, "Error cancelling booking.");
            }
        }
    }

    private void extendBooking() {
        List<String> bookings = bookingService.getBookingsForUser(username);
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

            String[] timeOptions = generateTimeOptions();
            JComboBox<String> timeDropdown = new JComboBox<>(timeOptions);
            int timeResult = JOptionPane.showConfirmDialog(this, timeDropdown,
                    "Select new end time:", JOptionPane.OK_CANCEL_OPTION);

            if (timeResult == JOptionPane.OK_OPTION) {
                String selectedTime = (String) timeDropdown.getSelectedItem();
                if (selectedTime == null) return;

                try {
                    String[] parts = selectedBooking.split(",");
                    if (parts.length < 5) {
                        JOptionPane.showMessageDialog(this, "Invalid booking format.");
                        return;
                    }

                    int currentDuration = Integer.parseInt(parts[4].trim());
                    String[] timeParts = selectedTime.split(":");
                    int hour = Integer.parseInt(timeParts[0]);
                    if (selectedTime.contains("PM") && hour != 12) hour += 12;

                    int newDuration = hour - 13 + 1;
                    int extraHours = newDuration - currentDuration;

                    if (extraHours <= 0) {
                        JOptionPane.showMessageDialog(this, "End time must be later than current booking.");
                        return;
                    }

                    if (bookingService.extendBooking(selectedBooking, newDuration)) {
                        String role = bookingService.getUserRole(username);
                        double rate = bookingService.calculateRate(role);
                        double additionalPayment = rate * extraHours;
                        
                        new PaymentFrame(username, parts[3], role, extraHours, additionalPayment, false);
                        JOptionPane.showMessageDialog(this, "Booking extended to " + selectedTime);
                        loadBookings();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error extending booking.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error while extending booking.");
                }
            }
        }
    }

    private String[] generateTimeOptions() {
        String[] timeOptions = new String[22];
        int index = 0;
        for (int hour = 13; hour <= 23; hour++) {
            timeOptions[index++] = String.format("%d:00 PM", hour % 12 == 0 ? 12 : hour % 12);
            timeOptions[index++] = String.format("%d:30 PM", hour % 12 == 0 ? 12 : hour % 12);
        }
        return timeOptions;
    }

    private void setLotStatus(String status) {
        String selectedLot = (String) parkingLotDropdown.getSelectedItem();
        if (selectedLot != null && parkingService.setLotStatus(selectedLot, status)) {
            JOptionPane.showMessageDialog(this, "Updated lot: " + selectedLot);
            loadSensorData();
        } else {
            JOptionPane.showMessageDialog(this, "Error updating lot status.");
        }
    }

    private void loadSensorData() {
        List<String> sensorData = parkingService.getSensorData();
        sensorArea.setText(String.join("\n", sensorData));
    }
}