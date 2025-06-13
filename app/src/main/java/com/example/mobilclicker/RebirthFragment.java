package com.example.mobilclicker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class RebirthFragment extends Fragment {
    private int currentTowerType = 0; // 0=default, 1=gyro, 2=gold
    private boolean[] towerUnlocked = {true, false, false};
    private int[] towerPurchasePrices = {0, 2,2,4, 4,6};
    private Button shopButton1, shopButton2, shopButton3, shopButton4, shopButton5, shopButton6;
    private boolean[] purchasedButtons = new boolean[6];
    private MainActivity mainActivity;
    private PlayFragment playFragment;
    private TextView rebirth_textbox;
    private UpgradesFragment upgradesFragment;
    private int resetCount = 0;

    private SharedViewModel sharedViewModel;

    public RebirthFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rebirth, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);


        if (getActivity() instanceof MainActivity) {
            mainActivity = (MainActivity) getActivity();
        } else {
            mainActivity = null;
        }
        shopButton1 = view.findViewById(R.id.rebirth_option_one);
        shopButton2 = view.findViewById(R.id.rebirth_option_two);
        shopButton3 = view.findViewById(R.id.rebirth_option_three);
        shopButton4 = view.findViewById(R.id.rebirth_option_four);
        shopButton5 = view.findViewById(R.id.rebirth_option_five);
        shopButton6 = view.findViewById(R.id.rebirth_option_six);
        Button resetButton = view.findViewById(R.id.reset_button);
        rebirth_textbox = view.findViewById(R.id.rebirth_level_textbox);
        updateAllButtons();
        updateRebirthPreview();
        resetButton.setOnClickListener(v -> {
            if (playFragment.get_score() > 1000) {
                double scoreUsed = playFragment.get_score();
                double score2Increase = Math.log(scoreUsed / 1000) + 1000;
                playFragment.set_score_2(playFragment.get_score_2() + score2Increase);
                resetCount += 1;
                resetGameData();
                updateRebirthPreview();
                Toast.makeText(getContext(),
                        "Gained " + String.format("%.2f", score2Increase) + " rebirth tokens!",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(),
                        "You need at least 1000 points to reset! Current rebirth tokens is " + playFragment.get_score_2(),
                        Toast.LENGTH_SHORT).show();
            }
            updateAllButtons();
        });
        shopButton1.setOnClickListener(v -> {
            currentTowerType = 0;
            sharedViewModel.setCurrentTowerType(0);
            updateAllButtons();
        });
        shopButton3.setOnClickListener(v -> {
            if (!towerUnlocked[1]) {
                if (playFragment.get_score_2() >= towerPurchasePrices[2]) {
                    playFragment.set_score_2(playFragment.get_score_2() - towerPurchasePrices[2]);
                    towerUnlocked[1] = true;
                    currentTowerType = 1;
                    sharedViewModel.setCurrentTowerType(1);
                    updateAllButtons();
                }
            } else {
                currentTowerType = 1;

                sharedViewModel.setCurrentTowerType(1);
                updateAllButtons();
            }
        });
        shopButton5.setOnClickListener(v -> {
            if (!towerUnlocked[2]) {
                if (playFragment.get_score_2() >= towerPurchasePrices[4]) {
                    playFragment.set_score_2(playFragment.get_score_2() - towerPurchasePrices[4]);
                    towerUnlocked[2] = true;
                    currentTowerType = 2;
                    sharedViewModel.setCurrentTowerType(2);
                    updateAllButtons();
                }
            } else {
                currentTowerType = 2;
                sharedViewModel.setCurrentTowerType(2);
                updateAllButtons();
            }
        });
        shopButton2.setOnClickListener(v -> {
            if (!purchasedButtons[1] && playFragment.get_score_2() >= towerPurchasePrices[1]) {
                playFragment.set_score_2(playFragment.get_score_2() - towerPurchasePrices[1]);
                purchasedButtons[1] = true;
                playFragment.setClickMultiplier((float)(playFragment.getClickMultiplier() * 2));
                updateAllButtons();
            }
        });
        shopButton4.setOnClickListener(v -> {
            if (!purchasedButtons[3] && playFragment.get_score_2() >= towerPurchasePrices[3]) {
                playFragment.set_score_2(playFragment.get_score_2() - towerPurchasePrices[3]);
                purchasedButtons[3] = true;
                playFragment.setClickMultiplier((float)(playFragment.getClickMultiplier() * 1.5));
                updateAllButtons();
            }
        });
        shopButton6.setOnClickListener(v -> {
            if (playFragment.get_score_2() >= towerPurchasePrices[5]) {
                playFragment.set_score_2(playFragment.get_score_2() - towerPurchasePrices[5]);
                playFragment.setClickMultiplier((float)(playFragment.getClickMultiplier() * 2));
                updateAllButtons();
            }
        });
        return view;
    }
    private void updateAllButtons() {
        updateTowerButton(shopButton1, 0);
        updateTowerButton(shopButton3, 1);
        updateTowerButton(shopButton5, 2);
        updateUpgradeButton(shopButton2, towerPurchasePrices[1], 1);
        updateUpgradeButton(shopButton4, towerPurchasePrices[3], 3);
        updateRepeatableButton(shopButton6, towerPurchasePrices[5]);
        updateRebirthPreview();
    }
    private void updateTowerButton(Button button, int towerIndex) {
        if (!towerUnlocked[towerIndex] && towerIndex > 0) {
            if (playFragment.get_score_2() >= towerPurchasePrices[towerIndex]) {
                button.setEnabled(true);
                button.setAlpha(1.0f);
                button.setText("Buy " + towerPurchasePrices[towerIndex*2]);
            } else {
                button.setEnabled(false);
                button.setAlpha(0.5f);
                button.setText("Need " + towerPurchasePrices[towerIndex*2]);
            }
        } else {
            button.setEnabled(true);
            button.setAlpha(currentTowerType == towerIndex ? 1.0f : 0.8f);
            button.setText(currentTowerType == towerIndex ? "Current" : getOriginalButtonText(towerIndex));
        }
    }
    private void updateUpgradeButton(Button button, int price, int buttonIndex) {
        if (purchasedButtons[buttonIndex]) {
            button.setEnabled(false);
            button.setAlpha(0.3f);
            button.setText("Purchased");
        } else if (playFragment.get_score_2() >= price) {
            button.setEnabled(true);
            button.setAlpha(1.0f);
            button.setText("Buy" + price);
        } else {
            button.setEnabled(false);
            button.setAlpha(0.5f);
            button.setText("Need " + price);
        }
    }
    private void updateRepeatableButton(Button button, int price) {
        if (playFragment.get_score_2() >= price) {
            button.setEnabled(true);
            button.setAlpha(1.0f);
            button.setText("Buy " + price);
        } else {
            button.setEnabled(false);
            button.setAlpha(0.5f);
            button.setText("Need " + price);
        }
    }
    private String getOriginalButtonText(int buttonIndex) {
        switch(buttonIndex) {
            case 0: return "Default";
            case 1: return "Gyro 2";
            case 2: return "Gold 3";
            default: return "Upgrade";
        }
    }
    private void updateRebirthPreview() {
        if (playFragment == null) return;

        double currentScore = playFragment.get_score();
        double currentScore2 = playFragment.get_score_2();
        String previewText;

        if (currentScore > 10) {
            double potentialGain = Math.log(currentScore / 1000) + 1;
            double newTotal = currentScore2 + potentialGain;

            previewText = "Reset Count: " + resetCount +
                    " \nCurrent tokens: " + String.format("%.2f", currentScore2) +
                    "\nPotential gain: " + String.format("%.2f", potentialGain) +
                    "\nNew total: " + String.format("%.2f", newTotal) +
                    "\n\n(Need 1000 points to reset)";
        } else {
            double neededPoints = 1000 - currentScore;
            previewText = "Reset Count: " + resetCount +
                    "\nCurrent tokens: " + String.format("%.2f", currentScore2) +
                    "\nNeed " + String.format("%.0f", neededPoints) + " more points to reset" +
                    "\n\nPotential gain at 1000: +1.00 tokens";
        }
        rebirth_textbox.setText(previewText);
    }
    public void setUpgradesFragment(UpgradesFragment upgradesFragment) {
        this.upgradesFragment = upgradesFragment;
    }
    public void setPlayFragment(PlayFragment playFragment) {
        this.playFragment = playFragment;
    }
    public void resetGameData() {
        if (mainActivity == null) {
            Toast.makeText(getContext(), "Error: Main activity not accessible!", Toast.LENGTH_SHORT).show();
            return;
        }
        mainActivity.getSharedPreferences("upgrade_prefs", mainActivity.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        mainActivity.resetScore();
        AppDatabase db = AppDatabase.getInstance(getContext());
        new Thread(() -> {
            db.upgradeDAO().resetAllUpgrades();
        }).start();

        UpgradeManager.stopPointGeneration();
    }
    public int getCurrentTowerType()
    {
        if( currentTowerType >-1) {
            return currentTowerType;
        }
        return 0;
    }
}