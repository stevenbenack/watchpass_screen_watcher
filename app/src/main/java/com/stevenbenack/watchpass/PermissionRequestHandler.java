package com.stevenbenack.watchpass;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.view.accessibility.AccessibilityManager;

import androidx.core.app.ActivityCompat;

import java.util.List;

public class PermissionRequestHandler {
    public boolean hasAccessibilityServicePermissions(Context context, Class<? extends AccessibilityService> service) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for ( AccessibilityServiceInfo enabledService : enabledServices ) {
            ServiceInfo serviceInfo = enabledService.getResolveInfo().serviceInfo;

            if ( serviceInfo.packageName.equals(context.getPackageName()) && serviceInfo.name.equals(service.getName()) )
                return true;
        }
        return false;
    }

    public boolean hasPermission(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestUnGrantedPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }
}