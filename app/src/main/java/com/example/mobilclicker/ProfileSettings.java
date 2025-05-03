package com.example.mobilclicker;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ProfileSettings {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "username")
    private String name;
    @ColumnInfo(name = "soundBox")
    private boolean soundBox;
    @ColumnInfo(name = "volumeBox")
    private boolean volumeBox;

    /*
    @ColumnInfo(name = "upgrade_amount")
    private int amount;
    @ColumnInfo(name = "upgrade_base_value")
    private double baseValue;
    @ColumnInfo(name = "upgrade_base_cost")
    private double baseCost;
*/
    public ProfileSettings(long id) {
        this.id = id;
    }
    public ProfileSettings(long id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {return id;}
    public void setId(long id) {this.id = id;}
    @NonNull
    public String getName() {return name;}
    public void setName(@NonNull String name) {this.name = name;}
    /*
    public int getAmount() {return amount;}
    public void setAmount(int amount) {this.amount = amount;}
    public double getBaseValue() {return baseValue;}
    public void setBaseValue(double baseValue) {this.baseValue = baseValue;}
    public double getBaseCost() {return baseCost;}
    public void setBaseCost(double baseCost) {this.baseCost = baseCost;}
    */
}
