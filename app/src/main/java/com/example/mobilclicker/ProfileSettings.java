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
    @ColumnInfo(name = "numberBox")
    private boolean numberBox;
    @ColumnInfo(name = "fourthBox")
    private boolean fourthBox;

    public ProfileSettings(long id) {
        this.id = id;
        this.name = "Profile " + (int)(id+1);
        this.soundBox = true;
        this.volumeBox = true;
        this.numberBox = true;
        this.fourthBox = true;
    }

    public long getId() {return id;}
    public void setId(long id) {this.id = id;}
    @NonNull
    public String getName() {return name;}
    public void setName(@NonNull String name) {this.name = name;}

    public boolean isSoundBox() {return soundBox;}
    public void setSoundBox(boolean soundBox) {this.soundBox = soundBox;}
    public boolean isVolumeBox() {return volumeBox;}
    public void setVolumeBox(boolean volumeBox) {this.volumeBox = volumeBox;}
    public boolean isNumberBox() {return numberBox;}
    public void setNumberBox(boolean numberBox) {this.numberBox = numberBox;}
    public boolean isFourthBox() {return fourthBox;}
    public void setFourthBox(boolean fourthBox) {this.fourthBox = fourthBox;}

    /*
    public int getAmount() {return amount;}
    public void setAmount(int amount) {this.amount = amount;}
    public double getBaseValue() {return baseValue;}
    public void setBaseValue(double baseValue) {this.baseValue = baseValue;}
    public double getBaseCost() {return baseCost;}
    public void setBaseCost(double baseCost) {this.baseCost = baseCost;}
    */
}
