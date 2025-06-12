package com.example.mobilclicker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.*;
import android.util.Log;
import android.util.Size;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.*;

public class CameraColorFragment extends Fragment {

    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;

    private ImageReader captureImageReader;
    private ImageReader previewImageReader;

    private Handler backgroundHandler;
    private HandlerThread backgroundThread;

    private TextView colorPromptText, timerText, scoreText;
    private Button captureButton, endButton;
    private ImageView colorFeedbackIcon;

    private ColorOverlayView colorOverlayView;

    private int targetColor;
    private float multiplier = 1f;
    private long roundTime = 30000;
    private final long minRoundTime = 3000;

    private boolean isGameRunning = false;
    private SharedViewModel sharedViewModel;
    private CountDownTimer countDownTimer;

    private int score = 0;

    private final Size previewSize = new Size(320, 240);

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

        colorOverlayView = view.findViewById(R.id.colorOverlay);

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
        colorOverlayView.clear();

        if (voluntary) {
            Toast.makeText(getContext(), "Žaidimas baigtas! Gavai " + score + " taškų.", Toast.LENGTH_SHORT).show();

            sharedViewModel.sendCameraRoundScore(score);
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
        int[][] basicColors = {
                {255, 0, 0},   // Red
                {0, 255, 0},   // Green
                {0, 0, 255},   // Blue
                {255, 255, 0}, // Yellow
                {255, 165, 0} // Orange
        };

        Random rand = new Random();
        int index = rand.nextInt(basicColors.length);

        int r = basicColors[index][0];
        int g = basicColors[index][1];
        int b = basicColors[index][2];

        targetColor = Color.rgb(r, g, b);
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
            captureRequestBuilder.addTarget(captureImageReader.getSurface());
            cameraCaptureSession.capture(captureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) requireContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];

            // Pasirenkame maksimalų dydį, bet čia gali patobulinti pagal kamerų paramą
            Size largestSize = new Size(1920, 1080);
            captureImageReader = ImageReader.newInstance(largestSize.getWidth(), largestSize.getHeight(), ImageFormat.JPEG, 1);
            captureImageReader.setOnImageAvailableListener(captureImageListener, backgroundHandler);

            previewImageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.YUV_420_888, 2);
            previewImageReader.setOnImageAvailableListener(previewImageListener, backgroundHandler);

            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void startPreview() {
        if (cameraDevice == null || !textureView.isAvailable()) return;

        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(1920, 1080);

        Surface previewSurface = new Surface(texture);

        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);
            captureRequestBuilder.addTarget(previewImageReader.getSurface());

            cameraDevice.createCaptureSession(
                    Arrays.asList(previewSurface, captureImageReader.getSurface(), previewImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            cameraCaptureSession = session;
                            try {
                                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(getContext(), "Camera configuration failed", Toast.LENGTH_SHORT).show();
                        }
                    }, backgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final ImageReader.OnImageAvailableListener captureImageListener = reader -> {
        Image image = reader.acquireLatestImage();
        if (image == null) return;

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        image.close();

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        if (bitmap == null) return;

        int cropSize = 100;
        int startX = Math.max(0, bitmap.getWidth() / 2 - cropSize / 2);
        int startY = Math.max(0, bitmap.getHeight() / 2 - cropSize / 2);
        int cropWidth = Math.min(cropSize, bitmap.getWidth() - startX);
        int cropHeight = Math.min(cropSize, bitmap.getHeight() - startY);

        Bitmap centerCropBitmap = Bitmap.createBitmap(bitmap, startX, startY, cropWidth, cropHeight);

        int dominantColor = getDominantColor(centerCropBitmap);

        requireActivity().runOnUiThread(() -> showColorFeedback(dominantColor));
        Log.d("Ateina2", "Tikrinam2");

    };

    private final ImageReader.OnImageAvailableListener previewImageListener = reader -> {
        if (!isGameRunning) {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                image.close();
            }
            return;
        }


        Image image = reader.acquireLatestImage();
        if (image == null) return;

        Bitmap bitmap = yuvToBitmap(image);
        image.close();

        if (bitmap == null) return;

        int dominantColor = getDominantColor(bitmap);
        Point dominantPoint = findDominantColorPosition(bitmap);
        Rect boundingBox = findDominantColorBoundingBox(bitmap, dominantColor);

        requireActivity().runOnUiThread(() -> {
            if (!isGameRunning) {
                colorOverlayView.clear();
                return;
            }
            if (dominantPoint == null) {
                colorOverlayView.clear();
                return;
            }
            if (dominantPoint != null) {

                int r = Color.red(targetColor);
                int g = Color.green(targetColor);
                int b = Color.blue(targetColor);

                int tr = Color.red(dominantColor);
                int tg = Color.green(dominantColor);
                int tb = Color.blue(dominantColor);

                Log.d("CrosshairColor", "Under crosshair: R=" + r + ", G=" + g + ", B=" + b);
                Log.d("TargetColor", "Dominant color: R=" + tr + ", G=" + tg + ", B=" + tb);
            }


            // Convert coordinates to full TextureView size
            float scaleX = (float) textureView.getWidth() / bitmap.getWidth();
            float scaleY = (float) textureView.getHeight() / bitmap.getHeight();

            Point scaledPoint = new Point((int) (dominantPoint.x * scaleX), (int) (dominantPoint.y * scaleY));
            Rect scaledRect = new Rect(
                    (int) (boundingBox.left * scaleX),
                    (int) (boundingBox.top * scaleY),
                    (int) (boundingBox.right * scaleX),
                    (int) (boundingBox.bottom * scaleY)
            );

            colorOverlayView.setCrosshairPoint(scaledPoint);
            colorOverlayView.setBoundingBox(scaledRect);
        });
    };


    private void showColorFeedback(int dominantColor) {
        int tolerance = 10;
        Log.d("Ateina", "tikrinam");
        if (colorsAreClose(dominantColor, targetColor, tolerance)) {
            colorFeedbackIcon.setImageResource(R.drawable.ic_check_green);
            score += Math.round(multiplier);
            multiplier *= 1.5f;
            roundTime = Math.max(minRoundTime, (long) (roundTime * 0.75));
            Log.d("Tinka", "Spalvos arti");
        } else {
            colorFeedbackIcon.setImageResource(R.drawable.ic_cross_red);
            multiplier = 1f;
            roundTime = 30000;
            Log.d("Ne tokia spalva", "Spalvos nearti");
        }

        updateUI();
    }

    private boolean colorsAreClose(int color1, int color2, int tolerance) {
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);

        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);

        return Math.abs(r1 - r2) < tolerance &&
                Math.abs(g1 - g2) < tolerance &&
                Math.abs(b1 - b2) < tolerance;
    }

    private int getDominantColor(Bitmap bitmap) {
        // Simple average color
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        long r = 0, g = 0, b = 0;
        int count = 0;

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int color : pixels) {
            r += Color.red(color);
            g += Color.green(color);
            b += Color.blue(color);
            count++;
        }

        r /= count;
        g /= count;
        b /= count;

        return Color.rgb((int) r, (int) g, (int) b);
    }

    private Point findDominantColorPosition(Bitmap bitmap) {
        int dominantColor = getDominantColor(bitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int tolerance = 30;
        long sumX = 0, sumY = 0;
        int count = 0;

        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                int pixel = bitmap.getPixel(x, y);
                if (colorsAreClose(pixel, dominantColor, tolerance)) {
                    sumX += x;
                    sumY += y;
                    count++;
                }
            }
        }

        int threshold = 100; // minimalus pikselių kiekis, kad būtų laikoma spalva rasta

        if (count < threshold) {
            // Spalva nerasta pakankamai stipriai
            return null;
        }

        int avgX = (int) (sumX / count);
        int avgY = (int) (sumY / count);

        return new Point(avgX, avgY);
    }



    private Rect findDominantColorBoundingBox(Bitmap bitmap, int dominantColor) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int tolerance = 30;

        int left = width, top = height, right = 0, bottom = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                if (colorsAreClose(pixel, dominantColor, tolerance)) {
                    if (x < left) left = x;
                    if (x > right) right = x;
                    if (y < top) top = y;
                    if (y > bottom) bottom = y;
                }
            }
        }

        if (left > right || top > bottom) {
            int centerX = width / 2;
            int centerY = height / 2;
            return new Rect(centerX - 20, centerY - 20, centerX + 20, centerY + 20);
        }

        return new Rect(left, top, right, bottom);
    }

    private int colorDistance(int c1, int c2) {
        int r1 = Color.red(c1);
        int g1 = Color.green(c1);
        int b1 = Color.blue(c1);

        int r2 = Color.red(c2);
        int g2 = Color.green(c2);
        int b2 = Color.blue(c2);

        int dr = r1 - r2;
        int dg = g1 - g2;
        int db = b1 - b2;

        return dr * dr + dg * dg + db * db;
    }

    private Bitmap yuvToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        // U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21,
                image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, out);
        byte[] jpegBytes = out.toByteArray();

        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.length);
    }

    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
            checkPermissionsAndOpenCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {}

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {}
    };

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackgroundThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void navigateToPlayFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            checkPermissionsAndOpenCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }

        if (captureImageReader != null) {
            captureImageReader.close();
            captureImageReader = null;
        }

        if (previewImageReader != null) {
            previewImageReader.close();
            previewImageReader = null;
        }
    }
}
