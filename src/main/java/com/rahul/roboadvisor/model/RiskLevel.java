package com.rahul.roboadvisor.model;

/**
 * Risk appetite of the investor. The adjustment is added to the
 * base equity percentage (100 - age).
 */
public enum RiskLevel {
    LOW(-15),
    MEDIUM(0),
    HIGH(15);

    private final int equityAdjustment;

    RiskLevel(int equityAdjustment) {
        this.equityAdjustment = equityAdjustment;
    }

    public int getEquityAdjustment() {
        return equityAdjustment;
    }

    public static RiskLevel from(String value) {
        if (value == null || value.isBlank()) {
            return MEDIUM;
        }
        try {
            return RiskLevel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Risk must be LOW, MEDIUM or HIGH");
        }
    }
}
