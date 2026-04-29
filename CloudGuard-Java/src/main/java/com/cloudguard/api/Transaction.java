package com.cloudguard.api;

public class Transaction {
    private String userId;
    private double amount;
    private String location;

    // Constructor
    public Transaction(String userId, double amount, String location) {
        this.userId = userId;
        this.amount = amount;
        this.location = location;
    }

    // Getters
    public String getUserId() { return userId; }
    public double getAmount() { return amount; }
    public String getLocation() { return location; }
}