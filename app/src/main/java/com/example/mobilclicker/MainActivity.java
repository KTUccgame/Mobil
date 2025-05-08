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


    private int currentBackgroundIndex = 0;
    private int[] backgrounds = {
            R.drawable.menu_background_1,
            R.drawable.menu_background_2,
            R.drawable.menu_background_3
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
            // Keičiam foną į sekantį
            currentBackgroundIndex = (currentBackgroundIndex + 1) % backgrounds.length;
            bottomNavigationView.setBackgroundResource(backgrounds[currentBackgroundIndex]);
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

                Log.i("a", "clicked on play i think OUTSIDE");
                // ANIM HERE
                if (playFragment == null) {
                    playFragment = new PlayFragment();
                }
                selectedFragment = playFragment;
                View navView = bottomNavigationView.findViewById(R.id.nav_play);
                Animation set = AnimationUtils.loadAnimation(this, R.anim.play_extender);
                navView.startAnimation(set);
            } else if (itemId == R.id.nav_upgrades) {
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

                Log.i("a", "clicked on settings i think");
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
            // Clear existing upgrades from the database
            db.upgradeDAO().deleteAll();

            // List of upgrades to insert
            List<Upgrade> upgradesToInsert = new ArrayList<>();


            // Example: Convert UpgradeDefinitions to Upgrade entities
            for (UpgradeDefinition def : UpgradeManager.getAllDefinitions()) {
                // Directly map the definition to an Upgrade entity
                upgradesToInsert.add(UpgradeMapper.fromDefinition(def));
            }

            // Insert all new upgrades into the database
            db.upgradeDAO().insertAll(upgradesToInsert);
            Log.d("UpgradeInit", "Re-inserted all default upgrades into database");
        }).start();

  
    public void setColorBg(int BGcolor)
    {
        getWindow().getDecorView().setBackgroundColor(BGcolor);
    }
      
    /*
    public void clickPopup()
    {
        TextView popup = new TextView(this);
        popup.setText("+" + clickpower);
        popup.setTextSize(24f);
        popup.setTextColor(Color.YELLOW);
        //popup.setTypeface(null, Typeface.BOLD);
        popup.setAlpha(0f);

        RelativeLayout rootLayout = findViewById(R.id.tempLayout);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        popup.setLayoutParams(params);
        rootLayout.addView(popup);
        popup.animate()
                .alpha(1f)
                .translationYBy(-100f)
                .setDuration(700)
                .withEndAction(() -> rootLayout.removeView(popup))
                .start();

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
