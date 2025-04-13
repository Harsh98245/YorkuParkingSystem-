package com.yorku.parking.payment;

public class PaymentContext {
    private PaymentStrategy strategy;

//    public PaymentContext(PaymentStrategy strategy) {
//        this.strategy = strategy;
//    }

    public PaymentContext(PaymentStrategy strategy2) {
    	this.strategy = strategy2;
    	// TODO Auto-generated constructor stub
    	
	}

	public boolean processPayment(double amount) {
        return strategy.pay(amount);
    }
}