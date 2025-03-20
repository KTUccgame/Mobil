package com.example.mobilclicker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UpgradeDAO {
    @Insert
    void insert(Upgrade upgrade);
    @Update
    void updateUpgrade(Upgrade upgrade);
    @Query("DELETE FROM upgrade")
    void deleteAll();
    @Query("SELECT * from upgrade ORDER BY upgrade_name ASC")
    List<Upgrade> getAllUpgrades();
    @Query("SELECT * FROM upgrade WHERE id = :upgradeId LIMIT 1")
    Upgrade getUpgradeById(long upgradeId); // specific upgrade by id
    @Query("SELECT * FROM upgrade WHERE upgrade_name = :upgradeName LIMIT 1")
    Upgrade getUpgradeByName(String upgradeName); // specific upgrade by name
    @Query("UPDATE upgrade SET upgrade_amount = upgrade_amount + 1 WHERE id = :upgradeId")
    void incrementUpgrade(long upgradeId); // upgrade++ ( buying upgrade )
    @Query("UPDATE upgrade SET upgrade_base_cost = :newCost WHERE id = :upgradeId")
    void updateUpgradeCost(long upgradeId, double newCost);// upgrade cost scaling
}
