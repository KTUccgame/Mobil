package com.example.mobilclicker;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class DatabaseInfoActivity extends AppCompatActivity {

    private TextView databaseInfoTextView; // TextView to display database information

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_info); // Set the layout

        // Initialize the TextView
        databaseInfoTextView = findViewById(R.id.databaseInfoTextView);

        // Insert dummy data if the database is empty
        insertRealData();

        // Fetch and display database info
        displayDatabaseInfo();
    }

    private void insertRealData() {
        AppDatabase db = AppDatabase.getInstance(this);

        // Run on a background thread to avoid blocking the UI
        new Thread(() -> {
            List<Upgrade> upgrades = db.upgradeDAO().getAllUpgrades();
            Log.d("DatabaseInfo", "Upgrade count: " + upgrades.size()); // Log count to see if we have upgrades

            if (upgrades.isEmpty()) {
                // Insert real data into the database based on UpgradeDefinitions
                for (UpgradeDefinition def : UpgradeManager.getAllDefinitions()) {
                    Upgrade upgrade = UpgradeMapper.fromDefinition(def);
                    db.upgradeDAO().insert(upgrade);
                    Log.d("DatabaseInfo", "Inserted real upgrade: " + upgrade.getName());
                }

                Log.d("DatabaseInfo", "Inserted real data into the database");
            } else {
                Log.d("DatabaseInfo", "Database already contains data.");
            }
        }).start();
    }


    private void displayDatabaseInfo() {
        // Get the instance of the database
        AppDatabase db = AppDatabase.getInstance(this);

        // Run the database query on a background thread
        new Thread(() -> {
            List<Upgrade> upgrades = db.upgradeDAO().getAllUpgrades(); // Fetch all upgrades from the database
            Log.d("DatabaseInfo", "Fetched " + upgrades.size() + " upgrades from the database."); // Log size

            StringBuilder databaseInfo = new StringBuilder(); // To hold the information to display

            // Format the data for display
            for (Upgrade upgrade : upgrades) {
                databaseInfo.append("ID: ").append(upgrade.getId())
                        .append("\nName: ").append(upgrade.getName())
                        .append("\nAmount: ").append(upgrade.getAmount())
                        .append("\nBase Value: ").append(upgrade.getBaseValue())
                        .append("\nBase Cost: ").append(upgrade.getBaseCost())
                        .append("\nMax Amount: ").append(upgrade.getMaxAmount())
                        .append("\n\n");
            }

            // Update the UI with the database info
            runOnUiThread(() -> {
                if (databaseInfo.length() == 0) {
                    databaseInfo.append("No upgrades available in the database.");
                }
                databaseInfoTextView.setText(databaseInfo.toString());
            });

            // Log for debugging purposes
            Log.d("DatabaseInfo", "Database info displayed");
        }).start();
    }
}
