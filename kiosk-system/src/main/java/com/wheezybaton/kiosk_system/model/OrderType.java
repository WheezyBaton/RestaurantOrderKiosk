package com.wheezybaton.kiosk_system.model;

public enum OrderType {
    EAT_IN("Na miejscu"),
    TAKE_AWAY("Na wynos");

    private final String displayName;

    OrderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}