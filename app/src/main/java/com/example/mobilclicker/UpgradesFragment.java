package com.example.mobilclicker;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.concurrent.atomic.AtomicBoolean;

public class UpgradesFragment extends Fragment {
    private PlayFragment playFragment;
    private AtomicBoolean isGeneratorActive = new AtomicBoolean(false);
    private Handler handler = new Handler();
    private int generatorPrice = 10;
    private int generatorsOwned = 0;


    private int clickUpgradePrice = 5; // Atskira kaina Click Upgrade
    private int clickUpgradeLevel = 0;


    private Button pointGeneratorButton;
    private Button upgradeButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gauti PlayFragment iš FragmentManager
        playFragment = (PlayFragment) requireActivity()
                .getSupportFragmentManager().findFragmentByTag("PLAY_FRAGMENT");

        if (playFragment == null) {
            Log.e("UpgradesFragment", "PlayFragment is null!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upgrades, container, false);

        pointGeneratorButton = view.findViewById(R.id.point_generator_button);
        upgradeButton = view.findViewById(R.id.point_upgrade_button);

        // Pakrauname generatorių duomenis
        loadGeneratorData();


        pointGeneratorButton.setText("Point Generator (" + generatorPrice + " points)");
        upgradeButton.setText("Click Upgrade (" + clickUpgradePrice + " points)");

        pointGeneratorButton.setOnClickListener(v -> {
            if (playFragment != null && playFragment.get_score() >= generatorPrice) {
                playFragment.subtractPoints(generatorPrice);
                generatorsOwned++;
                generatorPrice = generatorsOwned * 10 + 10;
                pointGeneratorButton.setText("Point Generator (" + generatorPrice + " points)");

                // Išsaugome naujas vertes
                saveGeneratorData();

                if (isGeneratorActive.compareAndSet(false, true)) {
                    startPointGeneration();
                }
            }
        });


        upgradeButton.setOnClickListener(v -> {
            if (playFragment != null && playFragment.get_score() >= clickUpgradePrice) {
                playFragment.subtractPoints(clickUpgradePrice);

                // Padidinti kiekvieno paspaudimo duodamų taškų kiekį
                playFragment.increaseClickPower();

                clickUpgradeLevel++;
                clickUpgradePrice = clickUpgradeLevel * 5 + 5;

                upgradeButton.setText("Click Upgrade (" + clickUpgradePrice + " points)");

                // Išsaugoti naujas vertes
                saveGeneratorData();
            }
        });


        return view;
    }

    private void startPointGeneration() {
        handler.postDelayed(() -> {
            if (playFragment != null) {
                playFragment.addPoint(generatorsOwned);
            }
            if (isGeneratorActive.get()) {
                handler.postDelayed(this::startPointGeneration, 1000);
            }
        }, 1000);
    }

    public void stopPointGeneration() {
        isGeneratorActive.set(false);
        handler.removeCallbacksAndMessages(null);

        if (playFragment != null) {
            Upgrade upgrade = playFragment.getDatabase().upgradeDAO().getUpgradeById(1);
            upgrade.setBaseCost(10);
            upgrade.setAmount(0);
            playFragment.getDatabase().upgradeDAO().updateUpgrade(upgrade);
        }
    }

    private void saveGeneratorData() {
        if (playFragment == null) return;

        playFragment.requireActivity().getSharedPreferences("upgrade_prefs", playFragment.requireActivity().MODE_PRIVATE)
                .edit()
                .putInt("generatorsOwned", generatorsOwned)
                .putInt("generatorPrice", generatorPrice)
                .putBoolean("isGeneratorActive", isGeneratorActive.get()) // Įrašome generatoriaus aktyvumo būseną
                .apply();
    }

    private void loadGeneratorData() {
        if (playFragment == null) return;

        generatorsOwned = playFragment.requireActivity().getSharedPreferences("upgrade_prefs", playFragment.requireActivity().MODE_PRIVATE)
                .getInt("generatorsOwned", 0);
        generatorPrice = playFragment.requireActivity().getSharedPreferences("upgrade_prefs", playFragment.requireActivity().MODE_PRIVATE)
                .getInt("generatorPrice", 10);

        // Atkuriame generatoriaus aktyvumo būseną iš SharedPreferences
        boolean generatorWasActive = playFragment.requireActivity().getSharedPreferences("upgrade_prefs", playFragment.requireActivity().MODE_PRIVATE)
                .getBoolean("isGeneratorActive", false); // Numatytoji reikšmė - false

        if (generatorWasActive && !isGeneratorActive.get()) {
            isGeneratorActive.set(true);
            startPointGeneration();  // Atkuriame generavimo funkcionalumą, jei generatorius buvo aktyvus
        }
    }


    public void resetUpgrades() {
        generatorsOwned = 0;
        generatorPrice = 10;
        clickUpgradeLevel = 0;
        clickUpgradePrice = 5;

        // Atnaujinti mygtukų tekstą
        pointGeneratorButton.setText("Point Generator (" + generatorPrice + " points)");
        upgradeButton.setText("Click Upgrade (" + clickUpgradePrice + " points)");

        // Išsaugoti resetintus duomenis
        saveGeneratorData();
    }


}