package com.yorku.parking.payment;

public class MobilePayment implements PaymentStrategy {
    private String phone;
    private String pin;

    public MobilePayment(String phone, String pin) {
        this.phone = phone;
        this.pin = pin;
    }

    @Override
    public boolean pay(double amount) {
        System.out.println("Processing mobile payment of $" + amount);
        return true;
    }
}