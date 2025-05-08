package com.example.mobilclicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SettingsFragment extends Fragment {

    private MainActivity mainActivity;
    private final String adminPW = "aa";
    private long currentProfileId = 1;
    private CheckBox soundBox, volumeBox, numberBox, fourthBox;

    TextView profileText;
    public SettingsFragment() {
        // empty to not break app
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mainActivity = (MainActivity) getActivity();

        Button profile1Button = view.findViewById(R.id.profile_1_button);
        Button profile2Button = view.findViewById(R.id.profile_2_button);
        Button profile3Button = view.findViewById(R.id.profile_3_button);
        Button adminButton = view.findViewById(R.id.admin_text_button);
        Button creditsButton = view.findViewById(R.id.credits_button);
        TextView adminText = view.findViewById(R.id.admin_text_text);
        profileText = view.findViewById(R.id.Profile_text);
        soundBox = view.findViewById(R.id.sound_checkbox);
        volumeBox = view.findViewById(R.id.volume_checkbox);
        numberBox = view.findViewById(R.id.number_checkbox);
        fourthBox = view.findViewById(R.id.fourth_checkbox);
        ImageView image = view.findViewById(R.id.imageView);
        initializeProfilesIfNeeded();

        loadProfileSettings(mainActivity.currentProfileId);

        soundBox.setOnCheckedChangeListener((buttonView, isChecked) -> saveProfileSettings());
        volumeBox.setOnCheckedChangeListener((buttonView, isChecked) -> saveProfileSettings());
        numberBox.setOnCheckedChangeListener((buttonView, isChecked) -> saveProfileSettings());
        fourthBox.setOnCheckedChangeListener((buttonView, isChecked) -> saveProfileSettings());
        profile1Button.setOnClickListener(v -> {
            loadProfileSettings(1);
        });
        profile2Button.setOnClickListener(v -> {
            loadProfileSettings(2);
        });
        profile3Button.setOnClickListener(v -> {
            loadProfileSettings(3);
        });
        creditsButton.setOnClickListener(v -> {
            Context context = v.getContext();

            View popupView = inflater.inflate(R.layout.popup_layout, null);
            popupView.setBackgroundColor(Color.WHITE);
            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // close when click outside popup


            if (width < 600) width = 600;
            if (height < 600) height = 600;
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            TextView textView = popupView.findViewById(R.id.popup_text);

            try {
                InputStream is;
                try {
                    is = context.getAssets().open("credits.txt");
                } catch (IOException e) {
                    is = context.getResources().openRawResource(R.raw.credits);
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                textView.setText(stringBuilder.toString());
                is.close();
            } catch (IOException e) {
                textView.setText("Couldn't load credits");
                e.printStackTrace();
            }
            Button closeButton = popupView.findViewById(R.id.popup_close);
            closeButton.setOnClickListener(v1 -> popupWindow.dismiss());
            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
            /*
            // Dim background when popup appears
            View container = popupWindow.getContentView().getRootView();
            if (container != null) {
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
                p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIIND;
                p.dimAmount = 0.5f; // Dim level (0.0f to 1.0f)
                wm.updateViewLayout(container, p);
            }
            */
            Animation hyperspaceJump = AnimationUtils.loadAnimation(this.mainActivity, R.anim.hyperspace_jump);
            image.startAnimation(hyperspaceJump);
        });


        adminButton.setOnClickListener(v -> {
            if(adminPW.contentEquals(adminText.getText()))
            {
                toggleUserMode();
            }
        });

        //return inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }
    private void userModeAnimation() {
        if (mainActivity == null) return;
        ImageView userAnimation = new ImageView(mainActivity);
        int imageRes = mainActivity.isUser ?
                R.drawable.play:
                R.drawable.settings;
        userAnimation.setImageResource(imageRes);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
        ViewGroup rootView = mainActivity.findViewById(android.R.id.content);
        rootView.addView(userAnimation, params);
        userAnimation.setAlpha(0f);
        userAnimation.setScaleX(0.1f);
        userAnimation.setScaleY(0.1f);
        userAnimation.animate()
            .alpha(1f)
            .scaleX(0.5f)
            .scaleY(0.5f)
            .setDuration(300)
            .withEndAction(() -> {
            userAnimation.animate()
                .alpha(0f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(300)
                .setStartDelay(1000)
                .withEndAction(() -> rootView.removeView(userAnimation))
                .start();
            })
            .start();
        if(soundBox.isChecked())
        {
            try {
                MediaPlayer.create(mainActivity,
                                mainActivity.isUser ?
                                        R.raw.wood_two:
                                        R.raw.wood_one)
                        .start();
            } catch (Exception e) {
                Log.e("UserMode", "Error playing sound", e);
            }
        }

    }
    private void toggleUserMode() {
        if (mainActivity != null) {
            SharedPreferences prefs = mainActivity.getSharedPreferences(
                    mainActivity.isUser ? "upgrade_prefs" : "settings_prefs",
                    mainActivity.MODE_PRIVATE
            );
            prefs.edit().clear().apply();
            mainActivity.isUser = !mainActivity.isUser;
            Log.w("settings", mainActivity.isUser ? "going to user mode" : "going to admin mode");
            mainActivity.runOnUiThread(this::userModeAnimation);
            new Thread(() -> {
                ProfileSettingsDAO dao = AppActivity.db2.profileDAO();
                ProfileSettings profile = dao.loadAllByIds(new int[]{(int) currentProfileId}).get(0);
                profile.setAdminCheck(!mainActivity.isUser);
                dao.updateProfile(profile);
            }).start();
        }
    }
    private void loadProfileSettings(long profileId) {
        new Thread(() -> {
            ProfileSettingsDAO dao = AppActivity.db2.profileDAO();
            ProfileSettings profile = dao.loadAllByIds(new int[]{(int) profileId}).get(0);
            requireActivity().runOnUiThread(() -> {
                currentProfileId = profile.getId();
                soundBox.setChecked(profile.isSoundBox());
                volumeBox.setChecked(profile.isVolumeBox());
                numberBox.setChecked(profile.isNumberBox());
                fourthBox.setChecked(profile.isFourthBox());
                mainActivity.isUser = !profile.isAdminCheck();
                profileText.setText(profile.getName());
                mainActivity.setCurrentProfileId(profile.getId());

                // Atnaujinti PlayFragment su nauju profilio ID
                PlayFragment playFragment = (PlayFragment) requireActivity()
                        .getSupportFragmentManager().findFragmentByTag("PLAY_FRAGMENT");
                if (playFragment != null) {
                    playFragment.setCurrentProfileId(profile.getId());
                }
            });
        }).start();
    }
    private void saveProfileSettings() {
        new Thread(() -> {
            ProfileSettingsDAO dao = AppActivity.db2.profileDAO();
            ProfileSettings profile = new ProfileSettings(currentProfileId);
            profile.setSoundBox(soundBox.isChecked());
            profile.setVolumeBox(volumeBox.isChecked());
            profile.setNumberBox(numberBox.isChecked());
            profile.setFourthBox(fourthBox.isChecked());
            profile.setAdminCheck(!mainActivity.isUser);
            dao.updateProfile(profile);
        }).start();
    }
    private void initializeProfilesIfNeeded() {
        new Thread(() -> {
            ProfileSettingsDAO dao = AppActivity.db2.profileDAO();
            if (dao.getAll().isEmpty()) {
                for (int i = 1; i <= 4; i++) {
                    ProfileSettings profile = new ProfileSettings(i);
                    dao.insert(profile);
                }
            }

        }).start();
    }

}

