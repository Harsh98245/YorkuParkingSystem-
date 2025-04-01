package com.yorku.parking.gui;

import com.yorku.parking.payment.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class PaymentFrame extends JFrame {
    private JComboBox<String> methodDropdown;
    private JTextField cardNumberField, expiryField, cvvField;
    private JTextField mobileNumberField, otpField;
    private JButton payButton, cancelButton, sendOtpButton;
    private JPanel inputPanel;
    private String username, plate, space, role;
    private double amount, total;
    private Runnable onPaymentSuccess;
    private String generatedOtp = "";
    private int hours;

    public PaymentFrame(String username, String space, String role, int hours, double total) {
        this.username = username;
        this.space = space;
        this.role = role;
        this.hours = hours;
        this.total = total;

        setTitle("Payment");
        setSize(420, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        initComponents();  // Initialize the components
        setVisible(true);
    }

    private void initComponents() {
        // Setup for payment method dropdown
        JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        methodPanel.add(new JLabel("Payment Method:"));
        methodDropdown = new JComboBox<>(new String[]{"Credit Card", "Mobile Payment"});
        methodPanel.add(methodDropdown);
        add(methodPanel, BorderLayout.NORTH);

        // Panel for input fields based on the selected method
        inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        add(inputPanel, BorderLayout.CENTER);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        payButton = new JButton("Pay");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(payButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add listeners
        methodDropdown.addActionListener(e -> updateFields());
        payButton.addActionListener(this::handlePayment);
        cancelButton.addActionListener(e -> dispose());

        updateFields();  // Initial setup
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
        // Handle payment logic here
        String method = (String) methodDropdown.getSelectedItem();
        PaymentStrategy strategy;
        String identifier;

        // Determine deposit and total based on role
//        double rate;
//        if ("Student".equals(role)) {
//            rate = 5.0;
//        } else if ("Faculty".equals(role)) {
//            rate = 8.0;
//        } else if ("Non-Faculty".equals(role)) {
//            rate = 10.0;
//        } else {
//            rate = 15.0;  // For "Visitor"
//        }
//
//        double deposit = rate;  // Deposit for the selected hours
//        double total = rate*hours+ deposit ;
//
//        // Show deposit and total in a dialog box
//        JOptionPane.showMessageDialog(this, "Deposit: $" + deposit + "\nTotal: $" + total);

        // Proceed to payment
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
        boolean success = context.processPayment(total);

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
