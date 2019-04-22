package com.stevenbenack.watchpass;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.password_field)
    EditText passwordField;
    @BindView(R.id.test_button)
    Button testButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!isAccessibilityServicePermissions(this, WatcherService.class)) {
            // ask user to allow permission
            Log.d(TAG, "Does not have Watcher Accessibility Service Enabled");
        }

        AlertDialog.Builder permissionDialog = new AlertDialog.Builder(this);
        permissionDialog.setTitle(R.string.dialog_title);
        permissionDialog.setMessage(R.string.dialog_text);
        permissionDialog.setPositiveButton(R.string.dialog_affirmative_button_text, (dialog, which) -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, 0);
        });
        permissionDialog.setNegativeButton(R.string.dialog_cancel_button_text, (dialog, which) -> dialog.dismiss());
        permissionDialog.show();
    }

    public static boolean isAccessibilityServicePermissions(Context context, Class<? extends AccessibilityService> service) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo serviceInfo = enabledService.getResolveInfo().serviceInfo;

            if (serviceInfo.packageName.equals(context.getPackageName()) && serviceInfo.name.equals(service.getName()))
                return true;
        }
        return false;
    }

}
