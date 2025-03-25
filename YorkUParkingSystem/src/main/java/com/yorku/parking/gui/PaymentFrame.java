package com.yorku.parking.gui;

import com.yorku.parking.payment.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Random;

public class PaymentFrame extends JFrame {
    private JComboBox<String> methodDropdown;
    private JTextField cardNumberField, expiryField, cvvField;
    private JTextField mobileNumberField, otpField;
    private JButton payButton, cancelButton, sendOtpButton;
    private JPanel inputPanel;
    private String username, plate, space;
    private double amount;
    private Runnable onPaymentSuccess;
    private String generatedOtp = "";

    public PaymentFrame(String username, String plate, String space, double amount, Runnable onPaymentSuccess) {
        this.username = username;
        this.plate = plate;
        this.space = space;
        this.amount = amount;
        this.onPaymentSuccess = onPaymentSuccess;

        setTitle("Payment");
        setSize(420, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        methodPanel.add(new JLabel("Payment Method:"));
        methodDropdown = new JComboBox<>(new String[]{"Credit Card", "Mobile Payment"});
        methodPanel.add(methodDropdown);
        add(methodPanel, BorderLayout.NORTH);

        inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        payButton = new JButton("Pay");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(payButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        methodDropdown.addActionListener(e -> updateFields());
        payButton.addActionListener(this::handlePayment);
        cancelButton.addActionListener(e -> dispose());

        updateFields();
        setVisible(true);
    }

    private void updateFields() {
        inputPanel.removeAll();
        String method = (String) methodDropdown.getSelectedItem();

        if ("Credit Card".equals(method)) {
            cardNumberField = new JTextField();
            expiryField = new JTextField();
            cvvField = new JTextField();

            inputPanel.add(new JLabel("Card Number:"));
            inputPanel.add(cardNumberField);
            inputPanel.add(new JLabel("Expiry Date (MM/YY):"));
            inputPanel.add(expiryField);
            inputPanel.add(new JLabel("CVV:"));
            inputPanel.add(cvvField);
        } else {
            mobileNumberField = new JTextField();
            otpField = new JTextField();
            sendOtpButton = new JButton("Send OTP");

            sendOtpButton.addActionListener(e -> {
                generatedOtp = String.valueOf(new Random().nextInt(900000) + 100000);
                JOptionPane.showMessageDialog(this, "OTP sent: " + generatedOtp);
            });

            inputPanel.add(new JLabel("Mobile Number:"));
            inputPanel.add(mobileNumberField);
            inputPanel.add(new JLabel("OTP (sent via email):"));
            JPanel otpPanel = new JPanel(new BorderLayout());
            otpPanel.add(otpField, BorderLayout.CENTER);
            otpPanel.add(sendOtpButton, BorderLayout.EAST);
            inputPanel.add(otpPanel);
        }

        inputPanel.revalidate();
        inputPanel.repaint();
    }

    private void handlePayment(ActionEvent e) {
        String method = (String) methodDropdown.getSelectedItem();
        PaymentStrategy strategy;
        String identifier;

        if ("Credit Card".equals(method)) {
            String number = cardNumberField.getText().trim();
            String expiry = expiryField.getText().trim();
            String cvv = cvvField.getText().trim();

            if (!number.matches("\\d{16}")) {
                JOptionPane.showMessageDialog(this, "Card number must be 16 digits.");
                return;
            }

            if (!cvv.matches("\\d{3}")) {
                JOptionPane.showMessageDialog(this, "CVV must be 3 digits.");
                return;
            }

            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
                YearMonth expDate = YearMonth.parse(expiry, formatter);
                YearMonth now = YearMonth.now();
                YearMonth maxAllowed = YearMonth.now().plusYears(4);
                if (expDate.isBefore(now) || expDate.isAfter(maxAllowed)) {
                    JOptionPane.showMessageDialog(this, "Expiry date must be within 4 years from now.");
                    return;
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid expiry date format. Use MM/YY.");
                return;
            }

            strategy = new CreditCardPayment(number, expiry, cvv);
            identifier = number;

        } else {
            String mobile = mobileNumberField.getText().trim();
            String otp = otpField.getText().trim();

            if (!mobile.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(this, "Mobile number must be 10 digits.");
                return;
            }

            if (!otp.equals(generatedOtp)) {
                JOptionPane.showMessageDialog(this, "Incorrect OTP.");
                return;
            }

            strategy = new MobilePayment(mobile, otp);
            identifier = mobile;
        }

        PaymentContext context = new PaymentContext(strategy);
        boolean success = context.processPayment(amount);

        if (success) {
            logPayment(method, identifier);
            JOptionPane.showMessageDialog(this, "Payment Successful. Thank you!");
            dispose();
            if (onPaymentSuccess != null) {
                onPaymentSuccess.run();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Payment Failed. Try again.");
        }
    }

    private void logPayment(String method, String number) {
        try (PrintWriter out = new PrintWriter(new FileWriter("src/main/resources/payments.csv", true))) {
            out.println(username + "," + method + "," + number + "," + amount);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}