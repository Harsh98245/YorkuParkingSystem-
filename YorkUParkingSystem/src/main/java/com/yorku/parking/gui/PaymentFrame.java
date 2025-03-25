package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import com.yorku.parking.payment.*;
import com.yorku.parking.utils.BookingUtil;

public class PaymentFrame extends JFrame {
    private JTextField cardNumberField, expiryField, cvvField;
    private JComboBox<String> methodDropdown;
    private final String username, plate, space;
    private final int hours;
	private double amount;
	private Runnable onPaymentSuccess;

    public PaymentFrame(String username, String plate, String space, double cost, Runnable onPaymentSuccess) {
        this.username = username;
        this.plate = plate;
        this.space = space;
        this.amount = cost;
        this.onPaymentSuccess = onPaymentSuccess;
		this.hours = 0;

        setTitle("Payment");
        setSize(420, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        applyFlatLafStyling();

        JPanel panel = new JPanel(new GridLayout(6, 2, 12, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        panel.add(new JLabel("Payment Method:"));
        methodDropdown = new JComboBox<>(new String[]{"Credit Card", "Mobile Payment"});
        panel.add(methodDropdown);

        panel.add(new JLabel("Card Number / Phone:"));
        cardNumberField = new JTextField();
        panel.add(cardNumberField);

        panel.add(new JLabel("Expiry Date / UPI PIN:"));
        expiryField = new JTextField();
        panel.add(expiryField);

        panel.add(new JLabel("CVV / PIN:"));
        cvvField = new JTextField();
        panel.add(cvvField);

        JButton payButton = new JButton("Pay Now");
        payButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        payButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        payButton.addActionListener(e -> processPayment());

        panel.add(new JLabel()); // empty cell
        panel.add(payButton);

        add(panel);
        setVisible(true);
    }

    private void applyFlatLafStyling() {
        UIManager.put("Button.arc", 20);
        UIManager.put("Component.arc", 15);
        UIManager.put("ProgressBar.arc", 15);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
    }

    private void processPayment() {
        String method = (String) methodDropdown.getSelectedItem();
        String number = cardNumberField.getText().trim();
        String expiry = expiryField.getText().trim();
        String cvv = cvvField.getText().trim();

        if (number.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        double rate = getUserRate();
        double amount = rate * hours;

        PaymentStrategy strategy;
        if ("Credit Card".equals(method)) {
            strategy = new CreditCardPayment(number, expiry, cvv);
        } else {
            strategy = new MobilePayment(number, cvv);
        }

        PaymentContext context = new PaymentContext(strategy);
        boolean success = context.processPayment(amount);

        if (success) {
            BookingUtil.saveBooking(username, plate, space, hours);
            BookingUtil.updateSpaceStatus(space, "Occupied");
            logPayment(method, number);
            JOptionPane.showMessageDialog(this, "Payment Successful. Thank you!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Payment Failed. Try again.");
        }
    }

    private double getUserRate() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("Student", 5.0);
        rates.put("Faculty", 8.0);
        rates.put("NonFaculty", 10.0);
        rates.put("Visitor", 15.0);

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/users.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equalsIgnoreCase(username)) {
                    return rates.getOrDefault(parts[2].trim(), 15.0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 15.0; // Default fallback
    }

    private void logPayment(String method, String number) {
        try (PrintWriter out = new PrintWriter(new FileWriter("src/main/resources/payments.csv", true))) {
            out.println(username + "," + method + "," + number + "," + plate + "," + space + "," + hours);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
