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

    @Insert
    void insertAll(List<Upgrade> upgrades); // Add this line to insert a list of upgrades

    @Update
    void update(Upgrade upgrade);

    @Query("DELETE FROM upgrade")
    void deleteAll();

    @Query("SELECT * FROM upgrade ORDER BY upgrade_name ASC")
    List<Upgrade> getAllUpgrades();

    @Query("SELECT * FROM upgrade WHERE id = :upgradeId LIMIT 1")
    Upgrade getUpgradeById(String upgradeId); // specific upgrade by id

    @Query("SELECT * FROM upgrade WHERE upgrade_name = :upgradeName LIMIT 1")
    Upgrade getUpgradeByName(String upgradeName); // specific upgrade by name

    @Query("UPDATE upgrade SET upgrade_amount = upgrade_amount + 1 WHERE id = :upgradeId")
    void incrementUpgrade(long upgradeId); // upgrade++ (buying upgrade)

    @Query("UPDATE upgrade SET upgrade_cost = :newCost WHERE id = :upgradeId")
    void updateUpgradeCost(long upgradeId, double newCost); // upgrade cost scaling

    // Additional methods
    @Query("SELECT * FROM upgrade ORDER BY upgrade_amount DESC")
    List<Upgrade> getAllUpgradesByAmount(); // List upgrades sorted by amount (descending)


    @Query("SELECT * FROM upgrade WHERE upgrade_cost = :cost")
    List<Upgrade> getUpgradesByBaseCost(double cost); // Get upgrades by base cost (if needed)

    // Add the count query
    @Query("SELECT COUNT(*) FROM upgrade")
    int getUpgradeCount(); // Count the total number of upgrades in the database

    @Query("UPDATE upgrade SET upgrade_amount = 0, upgrade_cost = upgrade_base_value")
    void resetAllUpgrades();

    @Query("DELETE FROM upgrade WHERE id NOT IN (:ids)")
    void deleteByIdsNotInList(List<String> ids);
}
