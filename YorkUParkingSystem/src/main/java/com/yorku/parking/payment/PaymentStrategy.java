package com.yorku.parking.payment;

public interface PaymentStrategy {
    public boolean pay(double amount);
}