package com.example.mobilclicker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import android.widget.SearchView;
import android.widget.Toast;

public class DatabaseInfoActivity extends AppCompatActivity {

    private TextView databaseInfoTextView;
    private SearchView searchView;
    private List<Upgrade> allUpgrades; // Visi duomenys atmintyje

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_info);

        databaseInfoTextView = findViewById(R.id.databaseInfoTextView);
        searchView = findViewById(R.id.searchView);

        insertRealData();
        displayDatabaseInfo();
    }

    private void insertRealData() {
        AppDatabase db = AppDatabase.getInstance(this);
        new Thread(() -> {
            List<Upgrade> upgrades = db.upgradeDAO().getAllUpgrades();
            if (upgrades.isEmpty()) {
                Toast.makeText(this, "Could not get data from database", Toast.LENGTH_SHORT).show();
            }
        }).start();
    }

    private void displayDatabaseInfo() {
        AppDatabase db = AppDatabase.getInstance(this);
        new Thread(() -> {
            allUpgrades = db.upgradeDAO().getAllUpgrades();

            runOnUiThread(() -> {
                updateDisplayedData(allUpgrades); // Išvedimas
                setupSearch(); // Įjungiam paiešką
            });
        }).start();
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText);
                return false;
            }
        });
    }

    private void filterData(String query) {
        List<Upgrade> filtered = new ArrayList<>();
        for (Upgrade upgrade : allUpgrades) {
            if (upgrade.getName().toLowerCase().contains(query.toLowerCase()) || upgrade.getId().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(upgrade);
            }
        }
        updateDisplayedData(filtered);
    }

    private void updateDisplayedData(List<Upgrade> upgrades) {
        StringBuilder databaseInfo = new StringBuilder();
        for (Upgrade upgrade : upgrades) {
            databaseInfo.append("ID: ").append(upgrade.getId())
                    .append("\nName: ").append(upgrade.getName())
                    .append("\nAmount: ").append(upgrade.getAmount())
                    .append("\nBase Value: ").append(upgrade.getBaseValue())
                    .append("\nBase Cost: ").append(upgrade.getBaseCost())
                    .append("\nMax Amount: ").append(upgrade.getMaxAmount())
                    .append("\n\n");
        }

        if (databaseInfo.length() == 0) {
            databaseInfo.append("Nothing found.");
        }

        databaseInfoTextView.setText(databaseInfo.toString());
    }
}
