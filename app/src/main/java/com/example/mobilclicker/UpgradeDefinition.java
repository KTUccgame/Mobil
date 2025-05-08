// UpgradeDefinition.java
package com.example.mobilclicker;

import java.util.List;

public class UpgradeDefinition {
    private final String id;
    private final String name;
    private final double cost;
    private final double baseValue;
    private final String description;
    private final boolean unlocksOthers;
    private final List<String> unlocksUpgradeIds; // IDs of upgrades this one unlocks
    private final boolean initiallyAvailable; // Whether it's shown at the start or unlocked later
    private boolean isPurchased = false; // Tracks if the upgrade has been purchased
    private final int maxAmount;

    public UpgradeDefinition(String id, String name, double cost, double baseValue,
                             String description, boolean unlocksOthers,
                             List<String> unlocksUpgradeIds, boolean initiallyAvailable, int maxAmount) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.baseValue = baseValue;
        this.description = description;
        this.unlocksOthers = unlocksOthers;
        this.unlocksUpgradeIds = unlocksUpgradeIds;
        this.initiallyAvailable = initiallyAvailable;
        this.maxAmount = maxAmount;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getCost() {
        return cost;
    }

    public double getBaseValue() {
        return baseValue;
    }

    public String getDescription() {
        return description;
    }

    public boolean doesUnlockOthers() {
        return unlocksOthers;
    }

    public List<String> getUnlocksUpgradeIds() {
        return unlocksUpgradeIds;
    }

    public boolean isInitiallyAvailable() {
        return initiallyAvailable;
    }

    public boolean isPurchased() {
        return isPurchased;
    }

    public void purchase() {
        this.isPurchased = true;
    }

    public int getMaxAmount() {
        return maxAmount;
    }





}
