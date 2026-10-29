package com.ramesh.lockbutton;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;

public class FloatingLockService extends AccessibilityService {

    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams layoutParams;
    private SharedPreferences prefs;

    private static final String PREFS_NAME = "lock_btn_prefs";
    private static final String KEY_X = "btn_x";
    private static final String KEY_Y = "btn_y";

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

        layoutParams = new WindowManager.LayoutParams(
                buttonSize,
                buttonSize,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );

        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.x = prefs.getInt(KEY_X, dpToPx(20));
        layoutParams.y = prefs.getInt(KEY_Y, dpToPx(250));

        button.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private long touchStartTime;
            private static final int CLICK_THRESHOLD_PX = 15;
            private static final int CLICK_TIMEOUT_MS = 250;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = layoutParams.x;
                        initialY = layoutParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        touchStartTime = System.currentTimeMillis();
                        button.setAlpha(0.6f);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);
                        layoutParams.x = initialX + deltaX;
                        layoutParams.y = initialY + deltaY;
                        windowManager.updateViewLayout(floatingView, layoutParams);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        button.setAlpha(1.0f);
                        float diffX = Math.abs(event.getRawX() - initialTouchX);
                        float diffY = Math.abs(event.getRawY() - initialTouchY);
                        long duration = System.currentTimeMillis() - touchStartTime;

                        if (diffX < CLICK_THRESHOLD_PX && diffY < CLICK_THRESHOLD_PX && duration < CLICK_TIMEOUT_MS) {
                            // Single tap: lock the phone immediately
                            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
                        } else {
                            // Dragged: remember the new coordinates
                            prefs.edit()
                                    .putInt(KEY_X, layoutParams.x)
                                    .putInt(KEY_Y, layoutParams.y)
                                    .apply();
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

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (windowManager != null && floatingView != null) {
            try {
                windowManager.removeView(floatingView);
            } catch (Exception ignored) {
            }
            floatingView = null;
        }
    }
}

