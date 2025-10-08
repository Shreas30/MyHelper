package com.myhelper.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    // List of permissions your app needs
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCheckPermissions = findViewById(R.id.btnCheckSMS);

        // 🔹 Auto check permissions when app opens
        if (!hasAllPermissions()) {
            requestAllPermissions();
        } else {
            Toast.makeText(this, "✅ All permissions already granted!", Toast.LENGTH_SHORT).show();
        }

        // 🔹 Manual button check (your original functionality)
        btnCheckPermissions.setOnClickListener(v -> {
            if (!hasAllPermissions()) {
                requestAllPermissions();
            } else {
                Toast.makeText(this, "✅ All permissions already granted!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Check if all permissions are granted
    private boolean hasAllPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // Request permissions
    private void requestAllPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    // Handle user response
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "✅ All permissions granted! App can now receive SMS and use location.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "⚠️ Some permissions were denied. Certain features may not work.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
