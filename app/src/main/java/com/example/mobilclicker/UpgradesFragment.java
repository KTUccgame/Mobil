package com.example.mobilclicker;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.concurrent.atomic.AtomicBoolean;

public class UpgradesFragment extends Fragment {
    private AtomicBoolean isGeneratorActive = new AtomicBoolean(false);
    private Handler handler = new Handler();
    private MainActivity mainActivity;
    int generatorPrice = 10;
    private int generatorsOwned = 0;
    private TextView generatorPriceText;
    private Button pointGeneratorButton;
    private Button upgradeButton;
    public UpgradesFragment() {
        // Reikalingas tuščias konstruktorius
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upgrades, container, false);
        mainActivity = (MainActivity) getActivity();
        pointGeneratorButton = view.findViewById(R.id.point_generator_button);
        upgradeButton=view.findViewById(R.id.point_upgrade_button);

        // Pakrauname išsaugotus duomenis
        loadGeneratorData();
        pointGeneratorButton.setText("Point Generator (" + generatorPrice + " points)");
        pointGeneratorButton.setOnClickListener(v -> {
            if (mainActivity.get_score() >= generatorPrice) {
                mainActivity.subtractPoints(generatorPrice);
                //generatorsOwned++;
                //generatorPrice += 10;
                // ASDFGHJK
                mainActivity._db.upgradeDAO().incrementUpgrade(1);
                Log.w("db","" + mainActivity._db.upgradeDAO().getUpgradeById(1).getAmount());
                //Log.w("db", " " + mainActivity._db.upgradeDAO().getUpgradeByName("Click Multiplier").getId());
                // ASDFGHJK
                generatorsOwned = mainActivity._db.upgradeDAO().getUpgradeById(1).getAmount();

                generatorPrice = mainActivity._db.upgradeDAO().getUpgradeById(1).getAmount() * 10 + 10;
                pointGeneratorButton.setText("Point Generator (" + generatorPrice + " points)");
                // Išsaugome duomenis
                saveGeneratorData();

                if (isGeneratorActive.compareAndSet(false, true)) {
                    startPointGeneration();
                }
            }
        });
        upgradeButton.setText("Click Upgrade ("+ generatorPrice+ " points)");
        upgradeButton.setOnClickListener(v -> {
            if (mainActivity.get_score()>=generatorPrice){
                mainActivity.subtractPoints(generatorPrice);
                //mainActivity._db.upgradeDAO().incrementUpgrade(2);
                //Log.w("db", "" + mainActivity._db.upgradeDAO().getUpgradeById(2).getAmount()*10);
                //generatorsOwned = mainActivity._db.upgradeDAO().getUpgradeById(2).getAmount();
                //generatorPrice = mainActivity._db.upgradeDAO().getUpgradeById(2).getAmount() * 10;
                upgradeButton.setText("Click upgrade (" + generatorPrice + " points)");
                mainActivity.clickpower++;
            }
        });

        return view;
    }
    private void startPointGeneration() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mainActivity != null) {
                    mainActivity.addPoint(generatorsOwned);
                }
                if (isGeneratorActive.get()) {
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }
    public void stopPointGeneration() {
        isGeneratorActive.set(false);
        handler.removeCallbacksAndMessages(null);

        Upgrade upgrade = mainActivity._db.upgradeDAO().getUpgradeById(1);
        upgrade.setBaseCost(10); // Reset cost
        upgrade.setAmount(0);    // Reset amount
        mainActivity._db.upgradeDAO().updateUpgrade(upgrade); // Save changes
    }

    private void saveGeneratorData() {
        if (mainActivity == null) return;
        mainActivity.getSharedPreferences("upgrade_prefs", mainActivity.MODE_PRIVATE)
                .edit()
                .putInt("generatorsOwned", generatorsOwned)
                .putInt("generatorPrice", generatorPrice)
                .putBoolean("isGeneratorActive", isGeneratorActive.get()) // Įrašome generatoriaus aktyvumo būseną
                .apply();
    }
    private void loadGeneratorData() {
        if (mainActivity == null) return;
        generatorsOwned = mainActivity.getSharedPreferences("upgrade_prefs", mainActivity.MODE_PRIVATE)
                .getInt("generatorsOwned", 0);
        generatorPrice = mainActivity.getSharedPreferences("upgrade_prefs", mainActivity.MODE_PRIVATE)
                .getInt("generatorPrice", 10);

        // Atkuriame generatoriaus aktyvumo būseną iš SharedPreferences
        boolean generatorWasActive = mainActivity.getSharedPreferences("upgrade_prefs", mainActivity.MODE_PRIVATE)
                .getBoolean("isGeneratorActive", false); // Numatytoji reikšmė - false
        // Jei generatorius buvo aktyvus, pradėti jo generavimą
        if (generatorWasActive && !isGeneratorActive.get()) {
            isGeneratorActive.set(true);
            startPointGeneration();  // Atkuriame generavimo funkcionalumą, jei generatorius buvo aktyvus
        }
    }
}
