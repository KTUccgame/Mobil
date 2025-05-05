package com.example.mobilclicker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
    public long currentProfileId = 1;
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
        /*
        _button.setOnClickListener(view -> {
            addPoint();
            _textview.setText("" +_score);
            new Thread(() -> {
                ProfileSettings profile = _profileDAO.loadAllByIds(new int[]{(int) currentProfileId}).get(0); // assuming profile 1 for now
                if (profile.isNumberBox()) {
                    runOnUiThread(this::clickPopup);
                }
            }).start();
            //if()
            // method here to show pop up an image + amount of points from click
        });
        */
        // ↓ errpr but not real error?
        _button.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                addPoint();
                _textview.setText("" + _score);

                float touchX = event.getRawX();
                float touchY = event.getRawY();

                new Thread(() -> {
                    ProfileSettings profile = _profileDAO.loadAllByIds(new int[]{(int) currentProfileId}).get(0);
                    if (profile.isNumberBox()) {
                        runOnUiThread(() -> {
                            clickPopup(touchX, touchY);
                            clickXml(touchX, touchY);
                        });
                    }

                }).start();
            }
            return false;
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
    */
    public void clickXml(float x, float y) {
        ImageView circle = new ImageView(this);
        ViewGroup rootLayout = findViewById(android.R.id.content);
        int size = (int) (30 * getResources().getDisplayMetrics().density);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);
        rootLayout.addView(circle, params);
        circle.setX(x - size / 2f);
        circle.setY(y - size / 2f);
        AnimatedVectorDrawable morph = (AnimatedVectorDrawable)
                ContextCompat.getDrawable(this, R.drawable.circle_morph);
        circle.setImageDrawable(morph);
        morph.start();
        float angleDegrees = (float) (Math.random() * 60 - 30); // -+ left/right
        double angleRadians = Math.toRadians(angleDegrees);
        float v0 = 1000f; // starting upward
        float g = 2500f;  // constant downward
        float duration = 3.2f;
        long animationDurationMs = (long) (duration * 500);
        float vx = (float) (v0 * Math.sin(angleRadians));
        float vy = (float) (-v0 * Math.cos(angleRadians)); // - is upward
        long startTime = System.currentTimeMillis();
        ValueAnimator animator = ValueAnimator.ofFloat(0f, duration);
        animator.setDuration(animationDurationMs);
        animator.setInterpolator(null);
        animator.addUpdateListener(animation -> {
            float t = (System.currentTimeMillis() - startTime) / 1000f;
            if (t > duration) t = duration;
            float dx = vx * t;
            float dy = vy * t + 0.5f * g * t * t;
            circle.setX(x - size / 2f + dx);
            circle.setY(y - size / 2f + dy);
            circle.setAlpha(1f - (t / duration));
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rootLayout.removeView(circle);
            }
        });
        animator.start();
    }
    public void clickPopup(float x, float y) {
        TextView popup = new TextView(this);
        popup.setText("+" + clickpower);
        popup.setTextSize(24);
        popup.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        popup.setAlpha(1f);
        ViewGroup rootLayout = findViewById(android.R.id.content);
        popup.setX(x - 50);
        popup.setY(y - 100);
        rootLayout.addView(popup);
        popup.animate()
                .translationYBy(-150f)
                .alpha(0f)
                .setDuration(800)
                .withEndAction(() -> rootLayout.removeView(popup))
                .start();
    }
    public void setCurrentProfileId(long id) {
        currentProfileId = id;
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
