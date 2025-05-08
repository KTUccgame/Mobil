package com.example.mobilclicker;

import java.util.ArrayList;
import java.util.List;

public class UpgradeMapper {

    /**
     * Converts an UpgradeDefinition to an Upgrade entity for database storage.
     */
    public static Upgrade fromDefinition(UpgradeDefinition definition) {
        return new Upgrade(
                definition.getId(),
                definition.getName(),
                0, // amount starts at 0
                definition.getBaseValue(),
                definition.getCost(),
                definition.getMaxAmount()
        );
    }

    /**
     * Converts a list of UpgradeDefinition objects to a list of Upgrade entities.
     */
    public static List<Upgrade> fromDefinitions(List<UpgradeDefinition> definitions) {
        List<Upgrade> upgrades = new ArrayList<>();
        for (UpgradeDefinition def : definitions) {
            upgrades.add(fromDefinition(def));
        }
        return upgrades;
    }

    /**
     * Converts an Upgrade entity back into an UpgradeDefinition using static metadata.
     */
    public static UpgradeDefinition toDefinition(Upgrade upgrade) {
        // Fetch the base definition from the manager to restore metadata like description, maxAmount, etc.
        UpgradeDefinition base = UpgradeManager.getUpgrade(upgrade.getId());

        if (base == null) {
            // Fallback in case metadata is missing — avoid crashing
            return new UpgradeDefinition(
                    upgrade.getId(),
                    upgrade.getName(),
                    upgrade.getBaseCost(),
                    upgrade.getBaseValue(),
                    "No description",
                    false,
                    new ArrayList<>(),
                    true,
                    1 // default max amount
            );
        }

        // Clone the base definition but override cost/value if needed
        return new UpgradeDefinition(
                upgrade.getId(),
                base.getName(),
                upgrade.getBaseCost(),
                upgrade.getBaseValue(),
                base.getDescription(),
                base.doesUnlockOthers(),
                base.getUnlocksUpgradeIds(),
                base.isInitiallyAvailable(),
                base.getMaxAmount()
        );
    }
}
