package com.example.mobilclicker;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    AppDatabase _db;
    AppDatabase2 _db2;
    UpgradeDAO _upgradeDAO;
    ProfileSettingsDAO _profileDAO;
    Button _button;
    TextView _textview;
    int _score = 0;

    boolean isUser = true;

    int clickpower=1;


    private UpgradesFragment upgradesFragment; // Add a reference to UpgradesFragment
    private RebirthFragment rebirthFragment;   // Add a reference to RebirthFragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemIconTintList(null);

        // Button and text initialization
        _button = findViewById(R.id.button);
        _textview = findViewById(R.id.textview);
        _db = AppActivity.getDatabase();
        _upgradeDAO = _db.upgradeDAO();
        _db2 = AppActivity.getDatabase2();
        _profileDAO = _db2.profileDAO();

        // Temporary upgrade logic
        if (_upgradeDAO.getUpgradeByName("Click Multiplier") == null) {

            Upgrade upgrade = new Upgrade(0, "Click Multiplier", 0, 1, 10);

            _upgradeDAO.insert(upgrade);
            _upgradeDAO.getUpgradeByName("Click Multiplier").setId(1);
        }

        //_upgradeDAO.incrementUpgrade(0);
        Upgrade tempUpgrade = _upgradeDAO.getUpgradeByName("Click Multiplier");
        Log.w("db", "Upgrade " + tempUpgrade.getName() + " " + tempUpgrade.getAmount() + " " + tempUpgrade.getBaseValue());

        _button.setOnClickListener(view -> {
            addPoint();
            _textview.setText("" +_score);

            Log.i("i", "onclick " + _score);
            Log.i("i", "currently is a user: " + isUser);
        });
        // Navigation menu logic
        bottomNavigationView.setOnItemSelectedListener(item -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_play) {
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                return true;
            } else if (item.getItemId() == R.id.nav_upgrades) {
                if (upgradesFragment == null) {
                    upgradesFragment = new UpgradesFragment();
                }
                selectedFragment = upgradesFragment;
            } else if (item.getItemId() == R.id.nav_rebirth) {
                if (rebirthFragment == null) {
                    rebirthFragment = new RebirthFragment();
                    rebirthFragment.setUpgradesFragment(upgradesFragment); // Pass reference
                }
                selectedFragment = rebirthFragment;
            } else if (item.getItemId() == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .addToBackStack(null) // Allow back navigation
                        .commit();
            }

            return true;
        });

        // Set initial screen to Play
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_play);
        }
    }

    // Point management methods
    public void addPoint() {
        _score=_score+clickpower;
        updateScore();
    }

    public void addPoint(int amount) {
        _score += amount;
        updateScore();
    }

    public int get_score() {
        return _score;
    }

    public void subtractPoints(int amount) {
        _score -= amount;
        updateScore();
    }

    private void updateScore() {
        _textview.setText("" + _score);
    }

    // Reset score and stop point generation
    public void resetScore() {
        _score = 0;  // Reset score
        updateScore();  // Update the UI

        // Stop point generation from UpgradesFragment
        if (upgradesFragment != null) {
            upgradesFragment.stopPointGeneration();
        }
    }
}
