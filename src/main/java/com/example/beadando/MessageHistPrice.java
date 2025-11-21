package com.example.beadando;

public class MessageHistPrice {
    private String instrument;
    private String granularity;

    // Getterek és Setterek
    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }
}