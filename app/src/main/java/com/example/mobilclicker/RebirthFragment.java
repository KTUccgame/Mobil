package com.example.mobilclicker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class RebirthFragment extends Fragment {

    private MainActivity mainActivity;
    private UpgradesFragment upgradesFragment; // Reference to UpgradesFragment

    public RebirthFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment view
        View view = inflater.inflate(R.layout.fragment_rebirth, container, false);
        mainActivity = (MainActivity) getActivity();

        // Add reset button functionality
        Button resetButton = view.findViewById(R.id.reset_button);

        resetButton.setOnClickListener(v -> {
            // Reset the game data
            resetGameData();
        });

        return view;
    }

    public void setUpgradesFragment(UpgradesFragment upgradesFragment) {
        this.upgradesFragment = upgradesFragment;
    }
    private void resetGameData() {
        if (mainActivity != null) {
            // Clear SharedPreferences
            mainActivity.getSharedPreferences("upgrade_prefs", mainActivity.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();

            // Reset the score
            mainActivity.resetScore();

            // Stop point generation through upgradesFragment
            if (upgradesFragment != null) {
                upgradesFragment.stopPointGeneration();
            } else {
                Toast.makeText(mainActivity, "UpgradesFragment is not available", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
