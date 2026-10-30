package com.ramesh.lockbutton;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;

public class FloatingLockService extends AccessibilityService {

    private WindowManager windowManager;
    private View floatingView;
    private View screenshotView;
    private WindowManager.LayoutParams layoutParams;
    private SharedPreferences prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final String PREFS_NAME = "lock_btn_prefs";
    private static final String KEY_PERCENT_X = "btn_percent_x";
    private static final String KEY_PERCENT_Y = "btn_percent_y";

    private final Runnable dismissScreenshotRunnable = new Runnable() {
        @Override
        public void run() {
            hideScreenshotButton();
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Event-driven zero CPU usage: do not process accessibility events
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        showFloatingButton();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        hideScreenshotButton();
        if (floatingView != null && windowManager != null) {
            int buttonSize = dpToPx(52);
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int maxX = Math.max(0, dm.widthPixels - buttonSize);
            int maxY = Math.max(0, dm.heightPixels - buttonSize);

            float percentX = Math.max(0f, Math.min(1f, prefs.getFloat(KEY_PERCENT_X, 0.95f)));
            float percentY = Math.max(0f, Math.min(1f, prefs.getFloat(KEY_PERCENT_Y, 0.4f)));

            layoutParams.x = Math.max(0, Math.min(Math.round(percentX * maxX), maxX));
            layoutParams.y = Math.max(0, Math.min(Math.round(percentY * maxY), maxY));

            try {
                windowManager.updateViewLayout(floatingView, layoutParams);
            } catch (Exception ignored) {
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showFloatingButton() {
        if (floatingView != null) {
            return;
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (windowManager == null) {
            return;
        }

        int buttonSize = dpToPx(52);

        ImageView button = new ImageView(this);
        button.setContentDescription(getString(R.string.app_name));
        button.setHapticFeedbackEnabled(true);

        // Circular dark translucent background with subtle rim
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(0xA018181B); // 62% opacity deep dark zinc
        bg.setStroke(dpToPx(1.5f), 0x40FFFFFF); // Subtle bright border
        button.setBackground(bg);

        // Native vector lock icon
        button.setImageResource(R.drawable.ic_lock);
        int padding = dpToPx(14);
        button.setPadding(padding, padding, padding, padding);

        // FLAG_LAYOUT_NO_LIMITS removed: enforce display boundaries
        layoutParams = new WindowManager.LayoutParams(
                buttonSize,
                buttonSize,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        layoutParams.gravity = Gravity.TOP | Gravity.START;

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int maxX = Math.max(0, dm.widthPixels - buttonSize);
        int maxY = Math.max(0, dm.heightPixels - buttonSize);

        float percentX = prefs.getFloat(KEY_PERCENT_X, -1f);
        float percentY = prefs.getFloat(KEY_PERCENT_Y, -1f);

        if (percentX < 0 || percentY < 0) {
            // Backward compatibility for legacy coordinate keys
            int legacyX = prefs.getInt("btn_x", maxX);
            int legacyY = prefs.getInt("btn_y", dpToPx(250));
            percentX = maxX > 0 ? (float) legacyX / maxX : 0.95f;
            percentY = maxY > 0 ? (float) legacyY / maxY : 0.4f;
        }

        percentX = Math.max(0f, Math.min(1f, percentX));
        percentY = Math.max(0f, Math.min(1f, percentY));

        layoutParams.x = Math.round(percentX * maxX);
        layoutParams.y = Math.round(percentY * maxY);

        button.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean isLongPressTriggered = false;
            private boolean isDragging = false;
            private static final int DRAG_THRESHOLD_PX = 15;

            private final Runnable longPressRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!isDragging) {
                        isLongPressTriggered = true;
                        button.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        showVolumeSlider();
                    }
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        hideScreenshotButton();
                        initialX = layoutParams.x;
                        initialY = layoutParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isLongPressTriggered = false;
                        isDragging = false;
                        button.setAlpha(0.6f);
                        handler.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout());
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float diffX = Math.abs(event.getRawX() - initialTouchX);
                        float diffY = Math.abs(event.getRawY() - initialTouchY);
                        if (!isDragging && (diffX > DRAG_THRESHOLD_PX || diffY > DRAG_THRESHOLD_PX)) {
                            isDragging = true;
                            handler.removeCallbacks(longPressRunnable);
                        }
                        if (isDragging) {
                            int deltaX = (int) (event.getRawX() - initialTouchX);
                            int deltaY = (int) (event.getRawY() - initialTouchY);

                            DisplayMetrics dmMove = getResources().getDisplayMetrics();
                            int maxMoveX = Math.max(0, dmMove.widthPixels - buttonSize);
                            int maxMoveY = Math.max(0, dmMove.heightPixels - buttonSize);

                            // Strict clamping: button remains 100% inside screen bounds at all times
                            layoutParams.x = Math.max(0, Math.min(initialX + deltaX, maxMoveX));
                            layoutParams.y = Math.max(0, Math.min(initialY + deltaY, maxMoveY));
                            windowManager.updateViewLayout(floatingView, layoutParams);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacks(longPressRunnable);
                        button.setAlpha(1.0f);

                        if (isLongPressTriggered) {
                            isLongPressTriggered = false;
                            return true;
                        }

                        if (!isDragging) {
                            // Tap: instantly lock the screen
                            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
                        } else {
                            // Save percentage coordinates so position scales across screen rotations
                            DisplayMetrics dmUp = getResources().getDisplayMetrics();
                            int maxUpX = Math.max(0, dmUp.widthPixels - buttonSize);
                            int maxUpY = Math.max(0, dmUp.heightPixels - buttonSize);

                            float pX = maxUpX > 0 ? (float) layoutParams.x / maxUpX : 0f;
                            float pY = maxUpY > 0 ? (float) layoutParams.y / maxUpY : 0f;
                            pX = Math.max(0f, Math.min(1f, pX));
                            pY = Math.max(0f, Math.min(1f, pY));

                            prefs.edit()
                                    .putFloat(KEY_PERCENT_X, pX)
                                    .putFloat(KEY_PERCENT_Y, pY)
                                    .apply();

                            showScreenshotButton();
                        }
                        return true;
                }
                return false;
            }
        });

        floatingView = button;
        try {
            windowManager.addView(floatingView, layoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showScreenshotButton() {
        hideScreenshotButton();
        if (floatingView == null || windowManager == null) {
            return;
        }

        int screenshotSize = dpToPx(38);
        int lockSize = dpToPx(52);
        int margin = dpToPx(10);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        WindowManager.LayoutParams sParams = new WindowManager.LayoutParams(
                screenshotSize,
                screenshotSize,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        sParams.gravity = Gravity.TOP | Gravity.START;

        // Horizontally centered relative to the lock button
        sParams.x = layoutParams.x + (lockSize - screenshotSize) / 2;

        // Position vertically: by default on top (above lock button).
        // If in proximity to the top border, place on bottom.
        int topBorderThreshold = Math.max(screenHeight / 3, dpToPx(140));
        if (layoutParams.y < topBorderThreshold) {
            sParams.y = layoutParams.y + lockSize + margin;
        } else {
            sParams.y = layoutParams.y - screenshotSize - margin;
        }

        // Clamp inside screen bounds
        sParams.x = Math.max(margin, Math.min(sParams.x, screenWidth - screenshotSize - margin));
        sParams.y = Math.max(margin, Math.min(sParams.y, screenHeight - screenshotSize - margin));

        ImageView sButton = new ImageView(this);
        sButton.setContentDescription("Screenshot");
        sButton.setHapticFeedbackEnabled(true);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(0xA018181B);
        bg.setStroke(dpToPx(1.5f), 0x40FFFFFF);
        sButton.setBackground(bg);

        sButton.setImageResource(R.drawable.ic_camera);
        int pad = dpToPx(9);
        sButton.setPadding(pad, pad, pad, pad);

        sButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sButton.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
                takeCleanScreenshot();
            }
        });

        screenshotView = sButton;
        try {
            windowManager.addView(screenshotView, sParams);
            handler.postDelayed(dismissScreenshotRunnable, 1000);
        } catch (Exception e) {
            screenshotView = null;
        }
    }

    private void hideScreenshotButton() {
        handler.removeCallbacks(dismissScreenshotRunnable);
        if (windowManager != null && screenshotView != null) {
            try {
                windowManager.removeView(screenshotView);
            } catch (Exception ignored) {
            }
            screenshotView = null;
        }
    }

    private void takeCleanScreenshot() {
        hideScreenshotButton();
        if (floatingView != null) {
            floatingView.setVisibility(View.INVISIBLE);
        }

        // 80ms delay (~5 display frames) ensures SurfaceFlinger composes without any overlay buttons
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT);

                // Restore floating lock button after screenshot capture completes
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (floatingView != null) {
                            floatingView.setVisibility(View.VISIBLE);
                        }
                    }
                }, 600);
            }
        }, 80);
    }

    private void showVolumeSlider() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
        }
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        hideScreenshotButton();
        if (windowManager != null && floatingView != null) {
            try {
                windowManager.removeView(floatingView);
            } catch (Exception ignored) {
            }
            floatingView = null;
        }
    }
}
