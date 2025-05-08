package com.example.mobilclicker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpgradesFragment extends Fragment {
    private MainActivity mainActivity;
    private PlayFragment playFragment;
    private AtomicBoolean isGeneratorActive = new AtomicBoolean(false);
    private Handler handler = new Handler();


    private Map<String, Integer> upgradeLevels = new HashMap<>();
    private Map<String, Integer> upgradePrices = new HashMap<>();
    private LinearLayout upgradesLayout;
    private int generatorsOwned = 0;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        playFragment = (PlayFragment) requireActivity()
                .getSupportFragmentManager().findFragmentByTag("PLAY_FRAGMENT");

        if (playFragment == null) {
            Log.e("UpgradesFragment", "PlayFragment is null!");
        }




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upgrades, container, false);


        upgradesLayout = view.findViewById(R.id.upgrades_layout);


        fetchUpgradesFromDatabase();

        Button databaseInfoButton = view.findViewById(R.id.database_info_button);
        Button editButton = view.findViewById(R.id.edit_Button);

        // Hide buttons initially
        databaseInfoButton.setVisibility(View.GONE);
        editButton.setVisibility(View.GONE);

        // CHECK ADMIN STATUS USING currentProfileId
        new Thread(() -> {
            boolean isAdmin = isAdmin();
            AppDatabase2 db = AppDatabase2.getInstance(getContext());
            ProfileSettingsDAO profileSettingsDAO = db.profileDAO();

            MainActivity mainActivity = (MainActivity) getActivity();
            long currentProfileId = mainActivity.currentProfileId; // Get Profile ID

            // Update UI on the main thread
            getActivity().runOnUiThread(() -> {
                databaseInfoButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                editButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            });

            databaseInfoButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), DatabaseInfoActivity.class);
                startActivity(intent);
            });

            editButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), EditActivity.class);
                startActivity(intent);
            });

        }).start();

        return view;
    }

    // Inside UpgradesFragment
    public void fetchUpgradesFromDatabase() {
        AppDatabase db = AppDatabase.getInstance(getContext());

        new Thread(() -> {
            List<Upgrade> upgrades = db.upgradeDAO().getAllUpgrades(); // Fetch all upgrades

            getActivity().runOnUiThread(() -> {
                //Clear existing upgrade buttons
                upgradesLayout.removeAllViews();

                for (Upgrade upgrade : upgrades) {
                    Button upgradeButton = new Button(getContext());
                    String id = upgrade.getId();
                    int level = upgrade.getAmount();
                    int price = (int) upgrade.getBaseCost();
                    int maxAmount = (int) upgrade.getMaxAmount();

                    upgradeLevels.put(id, level);
                    upgradePrices.put(id, price);

                    if (level >= maxAmount) {
                        upgradeButton.setText(upgrade.getName() + " Maxed Out");
                    } else {
                        upgradeButton.setText(upgrade.getName() + " (Level: " + level + ", Cost: " + price + " points)");

                        upgradeButton.setOnClickListener(v -> {
                            int upgradePrice = upgradePrices.get(id);
                            if (playFragment.get_score() >= upgradePrice) {
                                playFragment.subtractPoints(upgradePrice);

                                new Thread(() -> {
                                    Upgrade upgradeEntity = db.upgradeDAO().getUpgradeById(id);
                                    if (upgradeEntity != null) {
                                        upgradeEntity.setAmount(upgradeEntity.getAmount() + 1);
                                        upgradeEntity.setCost(upgradeEntity.getBaseCost() + 10);
                                        db.upgradeDAO().update(upgradeEntity);

                                        // Execute the upgrade action here
                                        getActivity().runOnUiThread(() -> {
                                            // Execute the upgrade action after purchase
                                            UpgradeManager.executeUpgradeAction(id, playFragment);
                                            fetchUpgradesFromDatabase(); // 🔹 Refresh UI correctly
                                        });
                                    }
                                }).start();
                            }
                        });
                    }

                    upgradesLayout.addView(upgradeButton);
                }
            });
        }).start();
    }



    private void updateUpgradeInDatabase(Upgrade upgrade) {
        // Get the instance of the database
        AppDatabase db = AppDatabase.getInstance(getContext());

        // Update the upgrade in the database on a background thread
        new Thread(() -> {
            // Modify the upgrade's level and cost
            upgrade.setAmount(upgradeLevels.get(upgrade.getId()));
            upgrade.setCost(upgradePrices.get(upgrade.getId()));

            // Update the upgrade in the database
            db.upgradeDAO().update(upgrade);

            Log.d("UpgradesFragment", "Upgrade updated in the database: " + upgrade.getName());
        }).start();
    }


    public void resetUpgrades() {
        generatorsOwned = 0;
        upgradeLevels.clear();
        upgradePrices.clear();
        isGeneratorActive.set(false);
        handler.removeCallbacksAndMessages(null);

        upgradesLayout.removeAllViews(); // Reset the buttons visually

        onCreateView(getLayoutInflater(), (ViewGroup) getView().getParent(), null); // Reload layout

    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh upgrade list
        fetchUpgradesFromDatabase();

        // Execute actions for upgrades that are active
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<Upgrade> upgrades = db.upgradeDAO().getAllUpgrades();

            getActivity().runOnUiThread(() -> {
                for (Upgrade upgrade : upgrades) {
                    String id = upgrade.getId();
                    int level = upgrade.getAmount();

                    // 🔥 Check if the upgrade should be executed based on its level
                    if (level > 0) {
                        UpgradeManager.executeUpgradeAction(id, playFragment);
                    }
                }
            });
        }).start();
    }



    private boolean isAdmin() {
        AppDatabase2 db = AppDatabase2.getInstance(getContext());
        ProfileSettingsDAO profileSettingsDAO = db.profileDAO();


        MainActivity mainActivity = (MainActivity) getActivity();
        long currentProfileId = mainActivity.currentProfileId;

        ProfileSettings profile = profileSettingsDAO.findById(currentProfileId);
        return profile != null && profile.isAdminCheck();
    }

}

