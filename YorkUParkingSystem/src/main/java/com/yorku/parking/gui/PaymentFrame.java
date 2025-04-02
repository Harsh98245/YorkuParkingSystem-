package com.yorku.parking.gui;

import com.yorku.parking.payment.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Random;

public class PaymentFrame extends JFrame {
    private JComboBox<String> methodDropdown;
    private JTextField cardNumberField, expiryField, cvvField;
    private JTextField mobileNumberField, otpField;
    private JButton payButton, cancelButton, sendOtpButton;
    private JPanel inputPanel;
    private String username, plate, space, role;
    private double amount;
    private Runnable onPaymentSuccess;
    private String generatedOtp = "";
    private int hours;
    private boolean isDeposit;

    public PaymentFrame(String username, String space, String role, int hours, double amount, boolean isDeposit) {
        this.username = username;
        this.space = space;
        this.role = role;
        this.hours = hours;
        this.amount = amount;
        this.isDeposit = isDeposit;

        setTitle("Payment");
        setSize(420, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
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
            if (!isDeposit) {
                removePreviousDeposit(); // delete earlier deposit if this is a checkout
            }
            logPayment(method, identifier);
            JOptionPane.showMessageDialog(this, "Payment of $" + amount + " Successful. Thank you!");
            dispose();
            new DashboardFrame(username, false);
            if (onPaymentSuccess != null) {
                onPaymentSuccess.run();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Payment Failed. Try again.");
        }
    }
    

    private void removePreviousDeposit() {
        try {
            File inputFile = new File("src/main/resources/payments.csv");
            File tempFile = new File("src/main/resources/payments_temp.csv");

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            PrintWriter writer = new PrintWriter(new FileWriter(tempFile));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains(username) || !line.contains(String.valueOf(1.0 * getRate(role)))) {
                    writer.println(line);
                }
            }

            writer.close();
            reader.close();

            if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
                System.err.println("Failed to update payment file.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void logPayment(String method, String number) {
        try (PrintWriter out = new PrintWriter(new FileWriter("src/main/resources/payments.csv", true))) {
            String paymentId = "PAY" + new Random().nextInt(1000000);
            out.println(paymentId + "," + username + "," + method + "," + number + "," + amount);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private double getRate(String role) {
        return switch (role.toLowerCase()) {
            case "student" -> 5.0;
            case "faculty" -> 8.0;
            case "nonfaculty" -> 10.0;
            default -> 15.0;
        };
    }
    
    

    public void setOnPaymentSuccess(Runnable onPaymentSuccess) {
        this.onPaymentSuccess = onPaymentSuccess;
    }
}
