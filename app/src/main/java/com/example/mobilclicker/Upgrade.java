package com.example.mobilclicker;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity
public class Upgrade {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @NonNull
    @ColumnInfo(name = "upgrade_name")
    private String name;
    @ColumnInfo(name = "upgrade_amount")
    private int amount;
    @ColumnInfo(name = "upgrade_base_value")
    private double baseValue;
    @ColumnInfo(name = "upgrade_base_cost")
    private double baseCost;

    public Upgrade(long id, @NonNull String name, int amount, double baseValue, double baseCost) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.baseValue = baseValue;
        this.baseCost = baseCost;
    }

    public long getId() {return id;}
    public void setId(long id) {this.id = id;}
    @NonNull
    public String getName() {return name;}
    public void setName(@NonNull String name) {this.name = name;}
    public int getAmount() {return amount;}
    public void setAmount(int amount) {this.amount = amount;}
    public double getBaseValue() {return baseValue;}
    public void setBaseValue(double baseValue) {this.baseValue = baseValue;}
    public double getBaseCost() {return baseCost;}
    public void setBaseCost(double baseCost) {this.baseCost = baseCost;}
}
