package com.example.mobilclicker;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.room.Room;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    boolean isUser = true;
    public long currentProfileId = 1;
    private UpgradesFragment upgradesFragment;
    private RebirthFragment rebirthFragment;
    private PlayFragment playFragment;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDetector shakeDetector;

    private AppDatabase db; // Declare the Room database


    // Tikimybė kiekvienam fonui (sumos turėtų būti 100)
    // Tikimybės, kad fonas pasirodys: 7 fonai, pvz., 10%, 20%, 15%, 5%, 10%, 25%, 15%
    int[] probabilities = {20, 20, 20, 1, 5, 10, 1};



    private int currentBackgroundIndex = 0;
    private int[] backgrounds = {
            R.drawable.menu_background_1,
            R.drawable.menu_background_2,
            R.drawable.menu_background_3,
            R.drawable.menu_background_4,
            R.drawable.menu_background_5,
            R.drawable.menu_background_6,
            R.drawable.menu_background_7
    };


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemIconTintList(null);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeDetector = new ShakeDetector();


        shakeDetector.setOnShakeListener(() -> {
            // Generuojame atsitiktinį skaičių nuo 0 iki 100
            int randomChance = new Random().nextInt(100); // Atsitiktinis skaičius nuo 0 iki 99

            // Kuriame akumuliuotą tikimybę
            int accumulatedChance = 0;
            for (int i = 0; i < probabilities.length; i++) {
                accumulatedChance += probabilities[i];

                // Jei atsitiktinis skaičius yra mažesnis už akumuliuotą tikimybę, pasirinkime tą foną
                if (randomChance < accumulatedChance) {
                    currentBackgroundIndex = i;  // Pasirenkame foną pagal indeksą
                    bottomNavigationView.setBackgroundResource(backgrounds[currentBackgroundIndex]);
                    break;  // Baigiame ciklą, nes radome, kuris fonas bus pasirinktas
                }
            }
        });





        // Initialize the Room database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app_database")
                .fallbackToDestructiveMigration()
                .build();

        // Initialize upgrades if needed (insert defaults if they don't exist in the DB)
        initializeUpgradesIfNeeded(db);

        // Fragmentų valdymas
        bottomNavigationView.setOnItemSelectedListener(item -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment selectedFragment = null;

            int itemId = item.getItemId();

            if (itemId == R.id.nav_play) {

                Log.i("debug", "clicked on play i think OUTSIDE");
                // ANIM HERE
                if (playFragment == null) {
                    playFragment = new PlayFragment();
                }
                selectedFragment = playFragment;
                View navView = bottomNavigationView.findViewById(R.id.nav_play);
                Animation set = AnimationUtils.loadAnimation(this, R.anim.play_extender);
                navView.startAnimation(set);
            } else if (itemId == R.id.nav_upgrades) {
                Log.i("debug", "clicked on Upgrades");
                if (upgradesFragment == null) {
                    upgradesFragment = new UpgradesFragment();

                }
                selectedFragment = upgradesFragment;

                // ANIMACIJA ant nav_upgrades mygtuko
                View navView = bottomNavigationView.findViewById(R.id.nav_upgrades);
                if (navView != null) {
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.move_up_and_down);
                    navView.startAnimation(animation);
                }

            } else if (itemId == R.id.nav_rebirth) {
                Log.i("debug", "clicked on Rebirth tab");
                if (rebirthFragment == null) {
                    rebirthFragment = new RebirthFragment();
                    rebirthFragment.setUpgradesFragment(upgradesFragment);
                }
                selectedFragment = rebirthFragment;
                // ANIMACIJA ant nav_rebirth mygtuko
                View navView = bottomNavigationView.findViewById(R.id.nav_rebirth);
                if (navView != null) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(navView, "rotationY", 0f, 360f);
                    animator.setDuration(600);
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator.start();
                }

            } else if (itemId == R.id.nav_settings) {

                Log.i("debug", "clicked on settings i think");
                selectedFragment = new SettingsFragment();
                View navView = bottomNavigationView.findViewById(R.id.nav_settings);
                Animation set = AnimationUtils.loadAnimation(this, R.anim.hyperspace_jump);
                navView.startAnimation(set);

            }

            if (selectedFragment != null) {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .addToBackStack(null)
                        .commit();
            }

            return true;
        });

        // Set the initial fragment to PlayFragment if no saved instance
        if (savedInstanceState == null) {
            playFragment = new PlayFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, playFragment, "PLAY_FRAGMENT") // Add the TAG
                    .commit();
        }
    }

    // This method will insert default upgrades into the database if they don't already exist
    public void initializeUpgradesIfNeeded(AppDatabase db) {
        new Thread(() -> {
            // Gauti visus įrašus iš duomenų bazės
            List<Upgrade> existingUpgrades = db.upgradeDAO().getAllUpgrades();

            // List of upgrades to insert
            List<Upgrade> upgradesToInsert = new ArrayList<>();
            List<String> upgradeIdsInManager = new ArrayList<>();

            // Pirmiausia pridedame tuos, kurie nėra duomenų bazėje
            for (UpgradeDefinition def : UpgradeManager.getAllDefinitions()) {
                String id = def.getId();
                upgradeIdsInManager.add(id);  // Įrašome visus ID, esančius UpgradeManager

                // Patikriname, ar šis ID jau egzistuoja duomenų bazėje
                if (db.upgradeDAO().getUpgradeById(id) == null) {
                    upgradesToInsert.add(UpgradeMapper.fromDefinition(def));  // Jei nėra, pridedame į įrašų sąrašą
                }
            }

            // Pašaliname visus įrašus, kurių ID nėra sąraše
            db.upgradeDAO().deleteByIdsNotInList(upgradeIdsInManager);

            // Įterpiame visus naujus upgrade įrašus
            db.upgradeDAO().insertAll(upgradesToInsert);
            Log.d("UpgradeInit", "Re-inserted new upgrades and removed outdated ones.");
        }).start();
    }




    public void setColorBg(int BGcolor)
    {
        getWindow().getDecorView().setBackgroundColor(BGcolor);
    }



    public void setCurrentProfileId(long id) {
        currentProfileId = id;
    }

    // Reset score and stop point generation
    public void resetScore() {

        // Pasirūpiname, kad PlayFragment atnaujintų rezultatą
        if (playFragment != null) {
            playFragment.updateScore(0);
        }


    }

    public void setAdminStatus(long profileId, boolean isAdmin) {
        new Thread(() -> {
            AppDatabase2 db = AppDatabase2.getInstance(getApplicationContext());
            ProfileSettingsDAO profileSettingsDAO = db.profileDAO();

            ProfileSettings profile = profileSettingsDAO.findById(profileId);
            if (profile != null) {
                profile.setAdminCheck(isAdmin);
                profileSettingsDAO.updateProfile(profile);
                Log.d("AdminUpdate", "Admin status updated: " + isAdmin);
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(shakeDetector);
        super.onPause();
    }
}