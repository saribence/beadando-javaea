package com.example.beadando;

public class MessageOpenPosition {
    private String instrument;
    private Integer units;

    // Getterek és Setterek
    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }
}