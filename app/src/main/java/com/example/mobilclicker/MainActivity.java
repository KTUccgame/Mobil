package com.example.mobilclicker;

import android.animation.ObjectAnimator;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    boolean isUser = true;
    public long currentProfileId = 1;
    private UpgradesFragment upgradesFragment;
    private RebirthFragment rebirthFragment;
    private PlayFragment playFragment;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemIconTintList(null);

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


// Nustatyti pradžios ekraną į PlayFragment
        if (savedInstanceState == null) {
            playFragment = new PlayFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, playFragment, "PLAY_FRAGMENT") // Pridedame TAG
                    .commit();
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

    public void setCurrentProfileId(long id) {
        currentProfileId = id;
    }


    // Reset score and stop point generation
    public void resetScore() {

        // Pasirūpiname, kad PlayFragment atnaujintų rezultatą
        if (playFragment != null) {
            playFragment.updateScore(0);
            playFragment.resetClickPower(); // Resetina kiekvieno paspaudimo galią
        }

        // Sustabdyti taškų generavimą ir atstatyti atnaujinimus
        if (upgradesFragment != null) {
            upgradesFragment.stopPointGeneration();
            upgradesFragment.resetUpgrades(); // Naujas metodas visiems atnaujinimams resetinti
        }
    }
}