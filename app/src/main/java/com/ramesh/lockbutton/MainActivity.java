package com.ramesh.lockbutton;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView tvStatus;
    private Button btnAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tv_status);
        btnAction = findViewById(R.id.btn_action);

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceState();
    }

    private void updateServiceState() {
        boolean isRunning = isAccessibilityServiceEnabled(this, FloatingLockService.class);
        if (isRunning) {
            tvStatus.setText("Status: Active\nFloating button is on your screen.");
            tvStatus.setTextColor(0xFF4ADE80); // Bright green
            btnAction.setText("Open Accessibility Settings");
        } else {
            tvStatus.setText("Status: Disabled\nTurn on 'Floating Lock Service' in Accessibility.");
            tvStatus.setTextColor(0xFFF87171); // Soft red
            btnAction.setText("Enable in Accessibility Settings");
        }
    }

    private static boolean isAccessibilityServiceEnabled(Context context, Class<?> serviceClass) {
        String expectedServiceName = context.getPackageName() + "/" + serviceClass.getName();
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        if (TextUtils.isEmpty(enabledServices)) {
            return false;
        }
        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServices);
        while (colonSplitter.hasNext()) {
            String componentName = colonSplitter.next();
            if (componentName.equalsIgnoreCase(expectedServiceName)) {
                return true;
            }
        }
        return false;
    }
}

