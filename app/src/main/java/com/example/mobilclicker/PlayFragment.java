package com.example.mobilclicker;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.ArrayList;
import java.util.List;

public class PlayFragment extends Fragment implements SensorEventListener{
    private TextView _textview;
    private ImageButton _button;
    private int _score = 0;
    private float initialScore = 0f;
    private float targetDistance = 0f;
    private String currentPowerUpName = null;
    private SharedViewModel sharedViewModel;


    private double _score_2 = 0;
    private float clickMultiplier = 1;
    private int clickpower = 1;
    private AppDatabase _db;
    private AppDatabase2 _db2;
    private UpgradeDAO _upgradeDAO;
    private ProfileSettingsDAO _profileDAO;
    boolean isUser = true;
    public long currentProfileId = 1;
    private long pressStartTime = 0;
    private List<ImageView> enemyList = new ArrayList<>();
    private long timeElapsed = 0; // Laikas, praleistas žaidime (milisekundėmis)
    private long timeForNextIncrease = 30000; // Laikas po kurio priešų atsiradimo dažnis padidės (pavyzdžiui, kas 30 sekundžių)
    private long startTime; // Laiko žymė žaidimo pradžiai
    private int enemyCount = 0;
    boolean isTriplePurchased = false;
    // gyro?
    private LinearLayout gyroComponent;
    private MainActivity mainActivity;

    private CountDownTimer countDownTimer;
    private static final long TRACKING_DURATION_MS = 30_000;
    public int towerType = 0; // 0 default, 1 gyro, 2 gold
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;
    private ImageView directionIndicator;
    private TextView directionText;
    private TextView timerText;
    private int currentTargetDirection;
    private long directionMatchStartTime = 0;
    private boolean isDirectionMatched = false;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    private ProgressBar powerUpProgressBar;
    private TextView powerUpNameText;



    private TextView trackingText;
    private ProgressBar trackingProgressBar;

    private float currentDistance;
    private FusedLocationProviderClient fusedLocationClient;

    private float totalDistanceTravelled = 0f;
    private Location lastLocation;

    private static final int[] DIRECTIONS = {
            0, // up
            1, // right
            2, // down
            3  // left
    };
    private static final String[] DIRECTION_NAMES = {
            "Tilt UP",
            "Tilt RIGHT",
            "Tilt DOWN",
            "Tilt LEFT"
    };
    private static final int[] DIRECTION_ICONS = {
            R.drawable.gyro_arrow_up,
            R.drawable.gyro_arrow_right,
            R.drawable.gyro_arrow_down,
            R.drawable.gyro_arrow_left
    };
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }
    }
// gyro?

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play, container, false);


        _textview = view.findViewById(R.id.textview);
        _button = view.findViewById(R.id.button);

        powerUpProgressBar = view.findViewById(R.id.powerUpProgressBar);
        powerUpNameText = view.findViewById(R.id.powerUpNameText);

        startScoreProgressUpdater();

        ImageView backgroundImage = view.findViewById(R.id.background_image);  // Rasti ImageView su id background_image
        //gyro?
        directionIndicator = view.findViewById(R.id.direction_indicator);
        directionText = view.findViewById(R.id.direction_text);
        timerText = view.findViewById(R.id.timer_text);

        ImageButton worldMapBUtton = view.findViewById(R.id.world_map_button);
        worldMapBUtton.setOnClickListener(v -> {
            GameMapFragment gameMapFragment = new GameMapFragment();

            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,gameMapFragment).addToBackStack(null).commit();
        });
        //Kitu fragmentu stebetojas
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        sharedViewModel.getCurrentTowerType().observe(getViewLifecycleOwner(), towerType ->{
            updateTowerIcon(towerType);
        });

        sharedViewModel.getTotalScore().observe(getViewLifecycleOwner(), newScore -> {
            if (newScore != null) {
                addPoint(newScore);
            }
        });







        // Initialize sensor manager
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroComponent = view.findViewById(R.id.gyroComponent);
        //updateGyroVisibility();
        RebirthFragment rebirthFragment = mainActivity.getRebirthFragment();
        if ( _score_2>0 )
        {
            if ( rebirthFragment.getCurrentTowerType() == 1 )
            {
                gyroComponent.setVisibility(View.VISIBLE);
                // show sensor manager components
                setNewRandomDirection();
            }
        }
        else
        {
            gyroComponent.setVisibility(View.GONE);
            // hide sensor manager components
        }
        //gyro?
        backgroundImage.setOnTouchListener((v, event) -> {
            int pointerCount = event.getPointerCount();
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                if (pointerCount == 2) {
                    Toast.makeText(getContext(), "2 fingers detected on background!", Toast.LENGTH_SHORT).show();
                } else if (pointerCount == 3) {
                    Toast.makeText(getContext(), "3 fingers detected on background!", Toast.LENGTH_SHORT).show();
                    if(isTriplePurchased)
                        addPoint(clickpower*3);
                }
            }
            return true;
        });

        _db = AppActivity.getDatabase();
        _upgradeDAO = _db.upgradeDAO();
        _db2 = AppActivity.getDatabase2();
        _profileDAO = _db2.profileDAO();

        // Bokšto animacija
        _button.setBackgroundResource(R.drawable.tower_animation);
        AnimationDrawable towerAnimation = (AnimationDrawable) _button.getBackground();
        _button.setImageDrawable(null);
        towerAnimation.start();
        _button.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                pressStartTime = System.currentTimeMillis();

                new Thread(() -> {
                    AppDatabase db = AppDatabase.getInstance(getContext());
                    List<Upgrade> upgrades = db.upgradeDAO().getAllUpgrades(); // Get all upgrades

                    boolean isClickerPurchased = upgrades.stream().anyMatch(upgrade -> "clicking".equals(upgrade.getId()) && upgrade.getAmount() > 0);

                    // If amount > 0, the upgrade is considered purchased
                    // Exit the loop once we find that the clicker upgrade is purchased

                    getActivity().runOnUiThread(() -> {
                        if (isClickerPurchased) {
                            // Perform the click action only if the clicker upgrade is purchased
                            addPoint(); // Add points to the score
                            _textview.setText("" + _score ); // Update the score display

                            // Get the touch position
                            float touchX = event.getRawX();
                            float touchY = event.getRawY();

                            // Perform shooting at enemy (or any other logic you need)


                            // Check profile settings and show popups if necessary
                            new Thread(() -> {
                                ProfileSettings profile = _profileDAO.loadAllByIds(new int[]{(int) currentProfileId}).get(0);
                                if (profile.isNumberBox()) {
                                    requireActivity().runOnUiThread(() -> {
                                        clickPopup(touchX, touchY);
                                        clickXml(touchX, touchY);
                                    });
                                }
                            }).start();
                        } else {
                            // Optionally show a message or disable the button if the upgrade isn't purchased
                            Toast.makeText(getContext(), "Clicker upgrade not purchased yet!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            }

            if (event.getAction() == MotionEvent.ACTION_UP)
            {
                long endTime = System.currentTimeMillis();
                Log.i("hold", " holding " + endTime +" "+pressStartTime);
                if (pressStartTime > -0.1) {
                    float holdDuration = endTime - pressStartTime;

                    Toast.makeText(
                            v.getContext(),
                            "Button held for " + String.format("%.3f", holdDuration/1000f) +
                                    " seconds",
                            Toast.LENGTH_SHORT
                    ).show();
                }
           }
            return false;
        });



        return view;
    }


    @Override
    public void onResume() {
        refreshScore();
        isTriplePurchased = false;
        super.onResume();
        loadClickPowerFromDatabase();
        refreshScore(); // Grįžus į fragmentą, atnaujiname taškų skaičių
        if(enemyCount<0)
            enemyCount=0;
        // Start the enemy spawn loop if the enemy spawner upgrade is purchased
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<Upgrade> upgrades = db.upgradeDAO().getAllUpgrades(); // Get all upgrades

            boolean isEnemySpawnerPurchased = false;
            boolean isShootingPurchased = false;


            // Loop through all upgrades to check if the "enemy_spawner" upgrade is purchased
            for (Upgrade upgrade : upgrades) {
                if ("enemy_spawner".equals(upgrade.getId()) && upgrade.getAmount() > 0) {
                    isEnemySpawnerPurchased = true;
                    spawnDelay=5000;
                    if("enemy_spawner".equals(upgrade.getId()) && upgrade.getAmount() == 2){
                      spawnDelay= 1500;
                    }
                    if("enemy_spawner".equals(upgrade.getId()) && upgrade.getAmount() == 3){
                        spawnDelay= 500;
                    }
                }
                if ("auto_shot".equals(upgrade.getId()) && upgrade.getAmount() > 0){
                    isShootingPurchased = true;
                }
                if ("triple_points_with_three_fingers".equals(upgrade.getId()) && upgrade.getAmount() > 0){
                    isTriplePurchased = true;
                }

            }

            // Start the enemy spawn loop if purchased
            if (isEnemySpawnerPurchased) {
                startEnemySpawnLoop();
            }
            if (isShootingPurchased) {
                startAutomaticShooting();
            }


        }).start();
        enemyCount = 0;
        updateEnemyCountDisplay();
        // gyro !!!
        if (sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        }
        startTimer();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String powerUpName = prefs.getString("selectedPowerUpName", null);
        float distance = prefs.getFloat("selectedPowerUpDistance", 0f);

        if (powerUpName != null) {
            currentPowerUpName = powerUpName;
            targetDistance = distance;
            String prefix = "Unlock Progress: ";
            powerUpNameText.setText(prefix + currentPowerUpName);
            powerUpNameText.setVisibility(View.VISIBLE);
            powerUpProgressBar.setVisibility(View.VISIBLE);

            // Užfiksuojam _score reikšmę tą momentą, kai pasirinkome power-up,
            // kad progress skaičiuotume nuo tada
            initialScore = _score;

            prefs.edit().remove("selectedPowerUpName").remove("selectedPowerUpDistance").apply();
        }
    }

    private void updatePowerUpUI(String powerUpName, float distance) {

        int progressValue = Math.min((int) (distance / 10), 100);
        powerUpProgressBar.setProgress(progressValue);

    }


    @Override
    public void onPause() {
        super.onPause();
        stopEnemySpawnLoop(); // Sustabdo priešų kūrimo uždelsimą
        stopAutomaticShooting(); // Sustabdo automatinį šaudymą
        //gyro!!
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        stopTimer();
    }
    public void addPoint() {
        _score += clickpower * clickMultiplier;
        refreshScore();
    }
    public void setClickMultiplier(float additionalMult)
    {
        clickMultiplier += additionalMult;
    }
    public float getClickMultiplier()
    {
        return clickMultiplier;
    }
    public void updateScore(int score) {
        _score = score; // Priimame naują reikšmę
        refreshScore(); // Pritaikome ją UI
    }
    private void refreshScore() {
        if (_textview != null) {
            _textview.setText("" + _score);
        }
    }
    public void subtractPoints(int amount) {
        _score -= amount;
        refreshScore();
    }
    public void addPoint(int amount) {
        _score += amount;
        refreshScore();
    }
    public int get_score() {
        return _score;
    }
    public AppDatabase getDatabase() {
        return _db;
    }
    public void increaseClickPower() {
        clickpower++;
    }

    public void setCurrentProfileId(long id) {
        currentProfileId = id;
    }
    public void clickXml(float x, float y) {
        ImageView circle = new ImageView(requireContext());
        ViewGroup rootLayout = requireActivity().findViewById(android.R.id.content);
        int size = (int) (30 * getResources().getDisplayMetrics().density);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);
        rootLayout.addView(circle, params);
        circle.setX(x - size / 2f);
        circle.setY(y - size / 2f);
        AnimatedVectorDrawable morph = (AnimatedVectorDrawable)
                ContextCompat.getDrawable(requireContext(), R.drawable.circle_morph);
        circle.setImageDrawable(morph);
        morph.start();
        float angleDegrees = (float) (Math.random() * 60 - 30);
        double angleRadians = Math.toRadians(angleDegrees);
        float v0 = 1000f;
        float g = 2500f;
        float duration = 3.2f;
        long animationDurationMs = (long) (duration * 500);
        float vx = (float) (v0 * Math.sin(angleRadians));
        float vy = (float) (-v0 * Math.cos(angleRadians));
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
        TextView popup = new TextView(requireContext());
        popup.setText("+" + clickpower);
        popup.setTextSize(24);
        popup.setTextColor(getResources().getColor(android.R.color.holo_green_light, null));
        popup.setAlpha(1f);
        ViewGroup rootLayout = requireActivity().findViewById(android.R.id.content);
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
    private void spawnEnemy() {
        ImageView enemy = new ImageView(requireContext());
        enemy.setBackgroundResource(R.drawable.mushroom_walk_animation);
        AnimationDrawable enemyAnimation = (AnimationDrawable) enemy.getBackground();
        enemyAnimation.start();

        ViewGroup fragmentRoot = requireActivity().findViewById(R.id.play_fragment_root);
        int size = (int) (50 * getResources().getDisplayMetrics().density);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);
        fragmentRoot.addView(enemy, params);
        enemyCount++;
        updateEnemyCountDisplay();
        // Pažymime, kad priešas yra gyvas
        enemyList.add(enemy);
        float startX, startY;
        int side = (int) (Math.random() * 4);
        if (side == 0) {
            startX = (float) (Math.random() * fragmentRoot.getWidth());
            startY = -size;
        } else if (side == 1) {
            startX = (float) (Math.random() * fragmentRoot.getWidth());
            startY = fragmentRoot.getHeight();
        } else if (side == 2) {
            startX = -size;
            startY = (float) (Math.random() * fragmentRoot.getHeight());
        } else {
            startX = fragmentRoot.getWidth();
            startY = (float) (Math.random() * fragmentRoot.getHeight());
        }
        enemy.setX(startX);
        enemy.setY(startY);
        float targetX = _button.getX() + _button.getWidth() / 2;
        float targetY = _button.getY() + _button.getHeight() / 2;
        enemy.setTag("enemy");
        animateEnemy(enemy, startX, startY, targetX, targetY);
    }
    private void updateEnemyCountDisplay() {
        TextView enemyCountTextView = requireActivity().findViewById(R.id.enemy_count_text);
        if (enemyCountTextView != null) {
            enemyCountTextView.setText("Enemies: " + enemyCount);
        }
    }
    private void animateEnemy(ImageView enemy, float startX, float startY, float targetX, float targetY) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(8000);
        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            float currentX = startX + progress * (targetX - startX);
            float currentY = startY + progress * (targetY - startY);
            enemy.setX(currentX);
            enemy.setY(currentY);
            // Patikrinti, ar priešas pasiekė bokšto centrą
            float distance = (float) Math.hypot(targetX - currentX, targetY - currentY);
            if (distance < 10) { // Jei atstumas mažesnis nei 10px, pašaliname priešą
                removeEnemy(enemy);
                animator.cancel();
            }
        });
        animator.start();
    }
    // Funkcija, kuri pašalina priešą iš ekrano ir sąrašo
    private void removeEnemy(ImageView enemy) {
        ViewGroup parent = (ViewGroup) enemy.getParent();
        if (parent != null) {
            parent.removeView(enemy);
            enemyCount--;
            updateEnemyCountDisplay();
        }
        enemyList.remove(enemy); // Pašaliname priešą iš sąrašo
    }
    private Handler handler = new Handler();
    private long spawnDelay = 5000; // Pradinis atsiradimo greitis (2000ms arba 2 sekundės)
    private int additionalEnemies = 0; // Papildomų priešų skaičius
    private Runnable enemySpawnTask = new Runnable() {
        @Override
        public void run() {
            spawnEnemy(); // Kviečiame funkciją sukurti priešą

            // Didiname papildomų priešų skaičių kas sekundę
            additionalEnemies++;

            // Padidiname atsiradimo greitį (sumenkinti vėlavimą)
            //spawnDelay = Math.max(500, spawnDelay - 100); // Nesumažinkite vėlavimo žemiau 500ms

            // Kiekvieną sekundę, atsiradimo greitis trumpėja + pridedame daugiau priešų
            if (additionalEnemies >= 2) {
                handler.postDelayed(this, spawnDelay); // Greitėjantis atsiradimo greitis
                additionalEnemies = 0; // Atsinaujina kas sekundę
            } else {
                // Pradžioje uždelsimo intervalas lėtas (kas 2 sekundės)
                handler.postDelayed(this, spawnDelay);
            }
        }
    };


    private void startEnemySpawnLoop() {
        if (isResumed()) { // Patikriname, ar fragmentas yra aktyvus
            handler.post(enemySpawnTask);
        }
    }

    private void stopEnemySpawnLoop() {
        handler.removeCallbacks(enemySpawnTask);
    }


    private void startAutomaticShooting() {
        shootHandler.postDelayed(shootTask, 1000); // Pradeda automatinį šaudymą kas 1 sekundę
    }

    private void stopAutomaticShooting() {
        shootHandler.removeCallbacks(shootTask); // Sustabdo automatinius šūvius
    }



    // Kulkos šaudymo logika
    public void shootAtEnemy() {
        ViewGroup rootLayout = requireActivity().findViewById(R.id.play_fragment_root);
        if (rootLayout == null) return;

        ImageView enemy = findClosestEnemy();
        if (enemy == null || enemy.getParent() == null) return;

        ImageView projectile = new ImageView(requireContext());
        projectile.setImageResource(R.drawable.settings);

        int size = (int) (20 * getResources().getDisplayMetrics().density);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);
        rootLayout.addView(projectile, params);

        float startX = _button.getX() + _button.getWidth() / 2 - size / 2f;
        float startY = _button.getY() + _button.getHeight() / 2 - size / 2f;
        float targetX = enemy.getX() + enemy.getWidth() / 2 - size / 2f;
        float targetY = enemy.getY() + enemy.getHeight() / 2 - size / 2f;

        projectile.setX(startX);
        projectile.setY(startY);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(400);
        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            float currentX = startX + progress * (targetX - startX);
            float currentY = startY + progress * (targetY - startY);
            projectile.setX(currentX);
            projectile.setY(currentY);
        });

        //projectile.setRotationY(2 + t * 100f);
        //projectile.setRotation(2 + t * 100f);
        //circle.setRotationY(2 + t * 100f);
        //circle.setRotation(2 + t * 100f);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rootLayout.removeView(projectile);
                if (enemy != null && enemy.getParent() != null) {
                    removeEnemy(enemy); // Pašalinti priešą iš sąrašo ir ekrano
                    addPoint();
                    refreshScore();
                }
            }
        });

        animator.start();
    }

    // Raskite artimiausią priešą
    @Nullable
    private ImageView findClosestEnemy() {
        ViewGroup rootLayout = requireActivity().findViewById(R.id.play_fragment_root);
        ImageView closest = null;
        double minDistance = Double.MAX_VALUE;

        float towerX = _button.getX() + _button.getWidth() / 2;
        float towerY = _button.getY() + _button.getHeight() / 2;

        for (ImageView enemy : enemyList) {
            float enemyX = enemy.getX() + enemy.getWidth() / 2;
            float enemyY = enemy.getY() + enemy.getHeight() / 2;
            double distance = Math.hypot(towerX - enemyX, towerY - enemyY);
            if (distance < minDistance) {
                minDistance = distance;
                closest = enemy;
            }
        }
        return closest;
    }


    // Handler ir Runnable automatinio šaudymo funkcijoms
    private Handler shootHandler = new Handler();
    private Runnable shootTask = new Runnable() {
        @Override
        public void run() {
            shootAtEnemy(); // Šauna į artimiausią priešą
            shootHandler.postDelayed(this, 1000); // Šaudo kas 1 sekundę
        }
    };

    private void loadClickPowerFromDatabase() {
        AppDatabase db = AppDatabase.getInstance(getContext());
        _upgradeDAO = db.upgradeDAO();

        new Thread(() -> {
            Upgrade upgrade = _upgradeDAO.getUpgradeById("clicking_power");
            if (upgrade != null) {
                clickpower = upgrade.getAmount() + 1; // Jei naudoji 'amount' kaip lygį
            } else {
                clickpower = 1; // default
            }

            requireActivity().runOnUiThread(() -> {
                Log.d("ClickPower", "Loaded click power: " + clickpower);
            });
        }).start();
    }

    public double get_score_2() {
        return _score_2;
    }

    public void set_score_2(double _score_2) {
        this._score_2 = _score_2;
    }

    // gyro?

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;
        } else if (event.sensor == magnetometer) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            lastMagnetometerSet = true;
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            float[] rotationMatrix = new float[9];
            float[] orientation = new float[3];

            if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                SensorManager.getOrientation(rotationMatrix, orientation);

                // Convert radians to degrees
                float pitch = (float) Math.toDegrees(orientation[1]);
                float roll = (float) Math.toDegrees(orientation[2]);

                checkDeviceDirection(pitch, roll);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this implementation
    }

    private void checkDeviceDirection(float pitch, float roll) {
        boolean isMatching = false;

        // Normalize angles to 0-360 range
        pitch = (pitch + 360) % 360;
        roll = (roll + 360) % 360;

        switch(currentTargetDirection) {
            case 0: // Up (portrait)
                isMatching = (pitch > 330 || pitch < 30) && (roll > 330 || roll < 30);
                break;
            case 1: // Right (landscape right)
                isMatching = (roll > 60 && roll < 120);
                break;
            case 2: // Down (upside down)
                // More reliable upside down detection
                isMatching = (pitch > 150 && pitch < 210);
                break;
            case 3: // Left (landscape left)
                isMatching = (roll > 240 && roll < 300); // Equivalent to -60 to -120
                break;
        }

        if (isMatching) {
            if (!isDirectionMatched) {
                directionMatchStartTime = System.currentTimeMillis();
                isDirectionMatched = true;
            } else {
                long holdTime = System.currentTimeMillis() - directionMatchStartTime;
                updateTimerText(holdTime);

                if (holdTime >= 500) { // 0.5 seconds
                    onDirectionMatchSuccess();
                }
            }
        } else {
            isDirectionMatched = false;
            updateTimerText(0);
        }
    }

    private void onDirectionMatchSuccess() {
        // Reward the player
        addPoint(100);

        // Show feedback
        Toast.makeText(getContext(), "Direction matched! +100 points", Toast.LENGTH_SHORT).show();

        // Set new random direction
        setNewRandomDirection();
    }

    private void setNewRandomDirection() {
        int randomIndex = (int)(Math.random() * DIRECTIONS.length);
        currentTargetDirection = DIRECTIONS[randomIndex];

        directionIndicator.setImageResource(DIRECTION_ICONS[currentTargetDirection]);
        directionText.setText(DIRECTION_NAMES[currentTargetDirection]);
        updateTimerText(0);

        isDirectionMatched = false;
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isDirectionMatched) {
                    long holdTime = System.currentTimeMillis() - directionMatchStartTime;
                    updateTimerText(holdTime);
                }
                timerHandler.postDelayed(this, 50); // Update every 50ms
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void updateTimerText(long holdTime) {
        float seconds = holdTime / 1000f;
        String timerString = String.format("%.1fs / 0.5s", seconds);
        timerText.setText(timerString);

        // Change color based on progress (optional)
        if (holdTime >= 500) {
            timerText.setTextColor(getResources().getColor(R.color.green));
        } else {
            timerText.setTextColor(getResources().getColor(R.color.white));
        }
    }
    // gyro?

    private void updateTowerIcon(int towerType) {
        switch (towerType) {
            case 0:
                _button.setImageResource(R.drawable.tower1);
                break;
            case 1:
                _button.setImageResource(R.drawable.towergyro);
                break;
            case 2:
                _button.setImageResource(R.drawable.towergold);
                break;
        }
    }


    private void startScoreProgressUpdater() {
        final android.os.Handler handler = new android.os.Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateProgressBar();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void updateProgressBar() {
        if (currentPowerUpName == null || targetDistance <= 0) {
            powerUpProgressBar.setProgress(0);
            return;
        }


        float progressScore = _score - initialScore;

        int progress = (int) ((progressScore / targetDistance) * 100);
        if (progress > 100) progress = 100;
        if (progress < 0) progress = 0;

        powerUpProgressBar.setProgress(progress);

        if (progressScore >= targetDistance) {
            onPowerUpReached();

            // Resetinam, kad galėtume sekti kitą power-up
            currentPowerUpName = null;
            targetDistance = 0;
            initialScore = 0;
        }
    }

    private void onPowerUpReached() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getActivity(), "Pasiekei power-up: " + currentPowerUpName, Toast.LENGTH_SHORT).show()
            );
        }
    }



}
