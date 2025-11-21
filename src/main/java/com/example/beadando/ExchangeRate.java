package com.example.beadando;

public class ExchangeRate {
    private String date;
    private Double value;

    public ExchangeRate(String date, Double value) {
        this.date = date;
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public Double getValue() {
        return value;
    }
}