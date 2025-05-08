package com.example.mobilclicker;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditActivity extends AppCompatActivity {
    private EditText editName, editAmount, editBaseValue, editBaseCost, editMaxAmount;
    private Button saveButton, cancelButton;
    private Spinner upgradeSpinner;
    private String upgradeId; // To store the ID of the selected upgrade
    private Upgrade selectedUpgrade; // To hold the selected upgrade's data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Initialize the views
        editName = findViewById(R.id.edit_name);
        editAmount = findViewById(R.id.edit_amount);
        editBaseValue = findViewById(R.id.edit_base_value);
        editBaseCost = findViewById(R.id.edit_base_cost);
        editMaxAmount = findViewById(R.id.edit_max_amount);

        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);
        upgradeSpinner = findViewById(R.id.upgrade_spinner);

        // Populate spinner with upgrade options
        populateUpgradeSpinner();

        // Set OnClickListeners
        saveButton.setOnClickListener(v -> saveUpgradeChanges());
        cancelButton.setOnClickListener(v -> finish());  // Close without saving changes
    }

    // Populate the spinner with upgrade names only
    private void populateUpgradeSpinner() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        new Thread(() -> {
            List<Upgrade> upgrades = db.upgradeDAO().getAllUpgrades();
            List<String> upgradeNames = new ArrayList<>();
            Map<String, String> upgradeIdMap = new HashMap<>();

            // Extract the names of all upgrades
            for (Upgrade upgrade : upgrades) {
                upgradeNames.add(upgrade.getName());
                upgradeIdMap.put(upgrade.getName(), upgrade.getId());
            }

            runOnUiThread(() -> {
                // Create an adapter to bind the names to the spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(EditActivity.this,
                        android.R.layout.simple_spinner_item, upgradeNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                upgradeSpinner.setAdapter(adapter);

                // Set the first upgrade as the default selected item
                upgradeSpinner.setSelection(0);

                // Get the selected upgrade and populate the fields
                selectedUpgrade = upgrades.get(0); // First upgrade as default
                populateFieldsWithUpgradeData(selectedUpgrade);


                // Add an item selected listener to update fields when the spinner selection changes
                upgradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // Find the selected upgrade based on the name
                        String selectedUpgradeName = (String) parentView.getItemAtPosition(position);
                        upgradeId = upgradeIdMap.get(selectedUpgradeName);
                        // Find the corresponding upgrade object
                        for (Upgrade upgrade : upgrades) {
                            if (upgrade.getName().equals(selectedUpgradeName)) {
                                selectedUpgrade = upgrade;
                                break;
                            }
                        }

                        // Update fields with the selected upgrade data
                        populateFieldsWithUpgradeData(selectedUpgrade);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // Do nothing
                    }
                });
            });
        }).start();
    }

    // Populate the fields with the selected upgrade's data
    private void populateFieldsWithUpgradeData(Upgrade upgrade) {
        if (upgrade != null) {
            // Fill the fields with the upgrade's data
            editName.setText(upgrade.getName());
            editAmount.setText(String.valueOf(upgrade.getAmount()));
            editBaseValue.setText(String.valueOf(upgrade.getBaseValue()));
            editBaseCost.setText(String.valueOf(upgrade.getBaseCost()));
            editMaxAmount.setText(String.valueOf(upgrade.getMaxAmount()));
        }
    }

    // Save the changes to the selected upgrade
    // Save the changes to the selected upgrade
    private void saveUpgradeChanges() {
        // Get the new data from the EditText fields
        String name = editName.getText().toString().trim();
        String amountStr = editAmount.getText().toString().trim();
        String baseValueStr = editBaseValue.getText().toString().trim();
        String baseCostStr = editBaseCost.getText().toString().trim();
        String maxAmountStr = editMaxAmount.getText().toString().trim();

        // Check if any field is empty
        if (name.isEmpty() || amountStr.isEmpty() || baseValueStr.isEmpty() || baseCostStr.isEmpty() || maxAmountStr.isEmpty()) {
            Toast.makeText(this, "Laukai negali būti tušti!", Toast.LENGTH_SHORT).show();
            return; // Stop execution if any field is empty
        }

        // Convert values to correct data types
        int amount = Integer.parseInt(amountStr);
        double baseValue = Double.parseDouble(baseValueStr);
        double baseCost = Double.parseDouble(baseCostStr);
        int maxAmount = Integer.parseInt(maxAmountStr);
        String id = selectedUpgrade.getId();

        // Update the upgrade object
        Upgrade updatedUpgrade = new Upgrade(id, name, amount, baseValue, baseCost, maxAmount);

        // Update the upgrade in the database
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.upgradeDAO().update(updatedUpgrade);

            runOnUiThread(() -> Toast.makeText(EditActivity.this, "Atnaujinta sėkmingai!", Toast.LENGTH_SHORT).show());
            finish();
        }).start();
    }



}
