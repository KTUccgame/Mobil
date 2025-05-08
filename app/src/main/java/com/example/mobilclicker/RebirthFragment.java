package com.example.mobilclicker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class RebirthFragment extends Fragment {

    private MainActivity mainActivity;
    private PlayFragment playFragment;
    private TextView rebirth_textbox;
    private UpgradesFragment upgradesFragment; // Reference to UpgradesFragment
    private int resetCount = 0;
    public RebirthFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rebirth, container, false);

        if (getActivity() instanceof MainActivity) {
            mainActivity = (MainActivity) getActivity();
        } else {
            mainActivity = null;
        }


        Button resetButton = view.findViewById(R.id.reset_button);
        rebirth_textbox = view.findViewById(R.id.rebirth_level_textbox);
        rebirth_textbox.setText("Current reset count = " + resetCount + ", current tap multiplier = " + playFragment.getClickMultiplier());
        //resetButton.setOnClickListener(v -> resetGameData());
        resetButton.setOnClickListener(v -> {
            if (playFragment.get_score() > 1000) {
                // if total points > 1000, reset game data, increase point multiplier by 10%

                playFragment.setClickMultiplier((float)(1 * (playFragment.get_score()/1000)));// 0.1*10/1000
                resetCount += 1;
                resetGameData();
                updateRebirthText();
            }
            else
            {
                Toast.makeText(getContext(), "You need at least 1000 points to reset! current multiplier is " +playFragment.getClickMultiplier(), Toast.LENGTH_SHORT).show();
            }

        });
        return view;
    }

    public void setUpgradesFragment(UpgradesFragment upgradesFragment) {
        this.upgradesFragment = upgradesFragment;
    }
    public void updateRebirthText()
    {
        rebirth_textbox.setText("Current reset count = " + resetCount + ", current tap multiplier = " + playFragment.getClickMultiplier());
    }
    public void setPlayFragment(PlayFragment playFragment) {
        this.playFragment = playFragment;
    }
    public void resetGameData() {
        if (mainActivity == null) {
            Toast.makeText(getContext(), "Error: Main activity not accessible!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear SharedPreferences
        mainActivity.getSharedPreferences("upgrade_prefs", mainActivity.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        // Reset the score
        mainActivity.resetScore();

        // Reset upgrades in the database
        AppDatabase db = AppDatabase.getInstance(getContext());
        new Thread(() -> {
            db.upgradeDAO().resetAllUpgrades(); // Reset all upgrades in the database
            // You can also log or notify here if needed
        }).start();

        // Stop point generation through UpgradeManager
        UpgradeManager.stopPointGeneration(); // Stop point generation in the UpgradeManager
    }

}
