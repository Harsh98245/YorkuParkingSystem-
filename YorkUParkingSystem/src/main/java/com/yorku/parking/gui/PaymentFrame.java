package com.yorku.parking.gui;

import com.yorku.parking.payment.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PaymentFrame extends JFrame {
    private JComboBox<String> methodDropdown;
    private JTextField cardNumberField, expiryField, cvvField;
    private JButton payButton, cancelButton;
    private String username, plate, space;
    private double amount;
    private Runnable onPaymentSuccess;

    public PaymentFrame(String username, String plate, String space, double amount, Runnable onPaymentSuccess) {
        this.username = username;
        this.plate = plate;
        this.space = space;
        this.amount = amount;
        this.onPaymentSuccess = onPaymentSuccess;

        setTitle("Payment");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        panel.add(new JLabel("Payment Method:"));
        methodDropdown = new JComboBox<>(new String[]{"Credit Card", "Mobile Payment"});
        panel.add(methodDropdown);

        panel.add(new JLabel("Card/Mobile Number:"));
        cardNumberField = new JTextField();
        panel.add(cardNumberField);

        panel.add(new JLabel("Expiry Date (MM/YY):"));
        expiryField = new JTextField();
        panel.add(expiryField);

        panel.add(new JLabel("CVV / Pin:"));
        cvvField = new JTextField();
        panel.add(cvvField);

        payButton = new JButton("Pay");
        cancelButton = new JButton("Cancel");

        panel.add(payButton);
        panel.add(cancelButton);

        payButton.addActionListener(this::handlePayment);
        cancelButton.addActionListener(e -> dispose());

        add(panel);
        setVisible(true);
    }

    private void handlePayment(ActionEvent e) {
        String method = (String) methodDropdown.getSelectedItem();
        String number = cardNumberField.getText().trim();
        String expiry = expiryField.getText().trim();
        String cvv = cvvField.getText().trim();

        if (number.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        PaymentStrategy strategy;
        if (method.equals("Credit Card")) {
            strategy = new CreditCardPayment(number, expiry, cvv);
        } else {
            strategy = new MobilePayment(number, cvv); // expiry not used
        }

        PaymentContext context = new PaymentContext(strategy);
        boolean success = context.processPayment(amount);

        if (success) {
            logPayment(method, number);
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
