package com.example.mobilclicker;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeManager {
    private static final Map<String, UpgradeDefinition> upgradeDefinitions = new HashMap<>();
    private static Handler handler = new Handler(Looper.getMainLooper());

    private static boolean isGeneratorActive = false;
    private static int generatorsOwned = 0;

    static {
        upgradeDefinitions.put("clicking", new UpgradeDefinition(
                "clicking",
                "Clicking",
                0,
                0,
                "Unlocks clicking power upgrades.",
                true,
                List.of(), // No requirements
                true,
                1
        ));

        upgradeDefinitions.put("clicking_power", new UpgradeDefinition(
                "clicking_power",
                "Clicking Power",
                15,
                15,
                "Increases your click power by +1.",
                false,
                List.of("clicking"),
                false, // Initially locked
                100
        ));

        upgradeDefinitions.put("point_generator", new UpgradeDefinition(
                "point_generator",
                "Point Generator",
                20,
                1,
                "Generates 1 point per second.",
                false,
                List.of(),
                true,
                2
        ));

        upgradeDefinitions.put("enemy_spawner", new UpgradeDefinition(
                "enemy_spawner",
                "Enemy Spawner",
                30, // Base cost
                15,
                "Spawns enemies every 5 seconds.",
                false, // Initially locked
                List.of(), // Can be unlocked after clicking upgrade
                false, // Initially locked
                1 // Maximum amount
        ));

        upgradeDefinitions.put("auto_shot", new UpgradeDefinition(
                "auto_shot",
                "Auto Shooter",
                25, // Base cost
                0,
                "Shoots",
                false, // Initially locked
                List.of(), // Can be unlocked after clicking upgrade
                false, // Initially locked
                10 // Maximum amount
        ));

        upgradeDefinitions.put("tras", new UpgradeDefinition(
                "ateas",
                "Testas",
                25, // Base cost
                0,
                "Shoots",
                false, // Initially locked
                List.of(), // Can be unlocked after clicking upgrade
                false, // Initially locked
                10 // Maximum amount
        ));

        // Pridedame upgrade, kuris aktyvuoja dvigubus taškus, kai naudojami trys pirštai
        upgradeDefinitions.put("triple_points_with_three_fingers", new UpgradeDefinition(
                "triple_points_with_three_fingers",
                "Triple Points with Three Fingers",
                50,  // Kaina už upgrade
                0,   // Reikia 0 taškų pradžioje
                "Get triple points when clicking with three fingers.",
                false,  // Užrakinta iš pradžių
                List.of(),  // Reikalauja 'clicking' upgrade
                false,  // Iš pradžių užrakinta
                1  // Vienas max
        ));





    }

    // Returns an upgrade definition by ID
    public static UpgradeDefinition getUpgrade(String id) {
        return upgradeDefinitions.get(id);
    }

    // Returns all upgrade definitions
    public static Collection<UpgradeDefinition> getAllDefinitions() {
        return upgradeDefinitions.values();
    }

    // Unlock other upgrades when an upgrade is purchased
    private static void unlockOtherUpgrades(UpgradeDefinition upgrade) {
        for (String unlockedUpgradeId : upgrade.getUnlocksUpgradeIds()) {
            UpgradeDefinition unlockedUpgrade = upgradeDefinitions.get(unlockedUpgradeId);
            if (unlockedUpgrade != null && !unlockedUpgrade.isInitiallyAvailable()) {
                unlockedUpgrade.purchase(); // Mark as purchased (and available)
            }
        }
    }

    private static void startPointGeneration(PlayFragment playFragment) {
        if (isGeneratorActive) {
            handler.removeCallbacksAndMessages(null); // Remove previous callbacks if any
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (playFragment != null) {
                    playFragment.addPoint(generatorsOwned);
                }
                if (isGeneratorActive) {
                    handler.postDelayed(this, 1000); // Continue generating points every second
                }
            }
        }, 1000);

        isGeneratorActive = true; // Mark the generator as active
    }

    // Stop point generation (can be called from somewhere else if needed)
    public static void stopPointGeneration() {
        isGeneratorActive = false;
        handler.removeCallbacksAndMessages(null);
    }

    public static boolean purchaseUpgrade(String upgradeId, PlayFragment playFragment) {
        UpgradeDefinition upgrade = upgradeDefinitions.get(upgradeId);
        if (upgrade != null && !upgrade.isPurchased()) {
            double price = upgrade.getCost();
            if (playFragment.get_score() >= price) {
                playFragment.subtractPoints((int) price);
                upgrade.purchase();

                // Get database instance
                AppDatabase db = AppDatabase.getInstance(playFragment.getContext());
                new Thread(() -> {
                    Upgrade upgradeEntity = db.upgradeDAO().getUpgradeById(upgradeId);
                    if (upgradeEntity != null) {
                        upgradeEntity.setAmount(upgradeEntity.getAmount() + 1);
                        upgradeEntity.setCost(upgradeEntity.getBaseCost() + 10);
                        db.upgradeDAO().update(upgradeEntity);
                    }
                }).start();

                // ✅ Execute upgrade action dynamically
                executeUpgradeAction(upgradeId, playFragment);

                unlockOtherUpgrades(upgrade);
                return true; // Successful purchase
            }
        }
        return false; // Either upgrade is already purchased or not enough points
    }

    // Dynamically execute the corresponding upgrade actions
    public static void executeUpgradeAction(String upgradeId, PlayFragment playFragment) {
        Map<String, Runnable> upgradeActions = new HashMap<>();

        // Dynamically define upgrade actions
        upgradeActions.put("clicking_power", () -> {
            playFragment.increaseClickPower();

        });
        upgradeActions.put("point_generator", () -> {
            generatorsOwned++;
            if (!isGeneratorActive) {
                startPointGeneration(playFragment);
            }
        });

        // Execute the corresponding action if it exists
        Runnable action = upgradeActions.get(upgradeId);
        if (action != null) {
            action.run();
        }
    }




}
