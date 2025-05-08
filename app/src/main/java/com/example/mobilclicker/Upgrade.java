package com.example.mobilclicker;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Upgrade {

    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "upgrade_name")
    private String name;

    @ColumnInfo(name = "upgrade_amount")
    private int amount;

    @ColumnInfo(name = "upgrade_base_value")
    private double baseValue;

    @ColumnInfo(name = "upgrade_cost")
    private double baseCost;

    // New field for maxAmount to limit the number of upgrades
    @ColumnInfo(name = "upgrade_max_amount")
    private int maxAmount;

    // Constructor with maxAmount added
    public Upgrade(@NonNull String id, String name, int amount, double baseValue, double baseCost, int maxAmount) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.baseValue = baseValue;
        this.baseCost = baseCost;
        this.maxAmount = maxAmount; // Set maxAmount
    }

    // Getter and Setter methods placed together for readability

    @NonNull public String getId() { return id; }  public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }  public void setName(String name) { this.name = name; }

    public int getAmount() { return amount; }  public void setAmount(int amount) { this.amount = amount; }

    public double getBaseValue() { return baseValue; }  public void setBaseValue(double baseValue) { this.baseValue = baseValue; }

    public double getBaseCost() { return baseCost; }  public void setCost(double cost) { this.baseCost = cost; }

    // Getter and Setter for maxAmount
    public int getMaxAmount() { return maxAmount; }  public void setMaxAmount(int maxAmount) { this.maxAmount = maxAmount; }
}
