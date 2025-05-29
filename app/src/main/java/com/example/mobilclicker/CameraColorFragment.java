package com.example.mobilclicker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.nio.ByteBuffer;
import java.util.Random;

public class CameraColorFragment extends Fragment {

    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;
    private ImageReader imageReader;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;

    private TextView colorPromptText, timerText, scoreText;
    private Button captureButton, endButton;
    private ImageView colorFeedbackIcon;

    private int targetColor;
    private float multiplier = 1f;
    private long roundTime = 30000;
    private final long minRoundTime = 3000;

    private boolean isGameRunning = false;
    private SharedViewModel sharedViewModel;
    private CountDownTimer countDownTimer;

    private int score = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera_color, container, false);

        textureView = view.findViewById(R.id.textureView);
        colorPromptText = view.findViewById(R.id.colorPromptText);
        timerText = view.findViewById(R.id.timerText);
        scoreText = view.findViewById(R.id.scoreText);
        captureButton = view.findViewById(R.id.captureBtn);
        endButton = view.findViewById(R.id.endGameBtn);
        colorFeedbackIcon = view.findViewById(R.id.colorFeedbackIcon);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        startBackgroundThread();

        textureView.setSurfaceTextureListener(textureListener);

        captureButton.setText("Pradėti žaidimą");

        captureButton.setOnClickListener(v -> {
            if (!isGameRunning) {
                startGameRound();
                captureButton.setText("Fotografuoti");
            } else {
                takePicture();
            }
        });

        endButton.setOnClickListener(v -> {
            endGame(true);
            navigateToPlayFragment();
        });

        return view;
    }

    private void checkPermissionsAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            openCamera();
        }
    }

    private void startGameRound() {
        if (!isGameRunning) {
            isGameRunning = true;
        }

        generateRandomColor();
        updateUI();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(roundTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText("Laikas: " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                Toast.makeText(getContext(), "Laikas baigėsi! Praradai visus taškus!", Toast.LENGTH_SHORT).show();
                endGame(false);
            }
        };
        countDownTimer.start();
    }

    private void endGame(boolean voluntary) {
        if (countDownTimer != null) countDownTimer.cancel();
        isGameRunning = false;
        captureButton.setText("Pradėti žaidimą");

        if (voluntary) {
            Toast.makeText(getContext(), "Žaidimas baigtas! Gavai " + score + " taškų.", Toast.LENGTH_SHORT).show();

            sharedViewModel.sendCameraRoundScore(score);
            // Pridedam kameros surinktus taškus prie bendro score

            roundTime = 30000;
            multiplier = 1f;
            score = 0;
            updateUI();
        } else {
            Toast.makeText(getContext(), "Laikas baigėsi! Praradai visus taškus!", Toast.LENGTH_SHORT).show();
            score = 0;
            updateUI();
        }
    }


    private void generateRandomColor() {
        Random rand = new Random();
        targetColor = Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    private void updateUI() {
        Integer currentScore = sharedViewModel.getTotalScore().getValue();
        if (currentScore == null) currentScore = 0;

        String hexColor = String.format("%06X", (0xFFFFFF & targetColor));
        colorPromptText.setText("Rask spalvą: #" + hexColor);

        colorPromptText.setBackgroundColor(targetColor);
        scoreText.setText("Taškai: " + score);

        if (!isGameRunning) {
            timerText.setText("Laikas: " + (roundTime / 1000) + "s");
        }
    }

    private void takePicture() {
        if (cameraDevice == null) return;

        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.addTarget(imageReader.getSurface());
            cameraCaptureSession.capture(captureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) requireContext().getSystemService(getContext().CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            Size[] sizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.JPEG);
            Size size = sizes[0];

            imageReader = ImageReader.newInstance(size.getWidth(), size.getHeight(), ImageFormat.JPEG, 1);
            imageReader.setOnImageAvailableListener(reader -> {
                Image image = reader.acquireNextImage();
                if (image != null) {
                    Bitmap bitmap = imageToBitmap(image);
                    image.close();

                    int dominantColor = getDominantColor(bitmap);

                    requireActivity().runOnUiThread(() -> {
                        if (isColorSimilar(dominantColor, targetColor)) {
                            score += (int)(10 * multiplier);
                            multiplier += 0.2f;
                            roundTime = Math.max(minRoundTime, roundTime - 500);

                            colorFeedbackIcon.setImageResource(R.drawable.ic_check_green);
                            colorFeedbackIcon.setVisibility(View.VISIBLE);
                            colorFeedbackIcon.postDelayed(() -> colorFeedbackIcon.setVisibility(View.GONE), 1000);

                            startGameRound();
                        } else {
                            colorFeedbackIcon.setImageResource(R.drawable.ic_cross_red);
                            colorFeedbackIcon.setVisibility(View.VISIBLE);
                            colorFeedbackIcon.postDelayed(() -> colorFeedbackIcon.setVisibility(View.GONE), 1000);
                        }
                        updateUI();
                    });
                }
            }, backgroundHandler);

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    SurfaceTexture texture = textureView.getSurfaceTexture();
                    texture.setDefaultBufferSize(size.getWidth(), size.getHeight());
                    Surface surface = new Surface(texture);

                    try {
                        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        captureRequestBuilder.addTarget(surface);
                        cameraDevice.createCaptureSession(
                                java.util.Arrays.asList(surface, imageReader.getSurface()),
                                new CameraCaptureSession.StateCallback() {
                                    @Override
                                    public void onConfigured(@NonNull CameraCaptureSession session) {
                                        cameraCaptureSession = session;
                                        try {
                                            cameraCaptureSession.setRepeatingRequest(
                                                    captureRequestBuilder.build(), null, backgroundHandler);
                                        } catch (CameraAccessException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    @Override public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
                                }, backgroundHandler
                        );
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override public void onDisconnected(@NonNull CameraDevice camera) { camera.close(); }
                @Override public void onError(@NonNull CameraDevice camera, int error) { camera.close(); }
            }, backgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Bitmap imageToBitmap(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private int getDominantColor(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        return bitmap.getPixel(0, 0);
    }

    private boolean isColorSimilar(int c1, int c2) {
        int r1 = Color.red(c1), g1 = Color.green(c1), b1 = Color.blue(c1);
        int r2 = Color.red(c2), g2 = Color.green(c2), b2 = Color.blue(c2);
        int threshold = 50;
        return Math.abs(r1 - r2) < threshold &&
                Math.abs(g1 - g2) < threshold &&
                Math.abs(b1 - b2) < threshold;
    }

    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            checkPermissionsAndOpenCamera();
        }
        @Override public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}
        @Override public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) { return false; }
        @Override public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
    };

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (backgroundThread != null) backgroundThread.quitSafely();
        if (cameraDevice != null) cameraDevice.close();
    }

    private void navigateToPlayFragment() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Reikalingas kameros leidimas", Toast.LENGTH_SHORT).show();
            }
        }
    }
}