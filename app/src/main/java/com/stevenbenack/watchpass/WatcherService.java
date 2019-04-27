package com.stevenbenack.watchpass;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class WatcherService extends AccessibilityService {
    private static final String TAG = "CustomAccessibility";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo source = accessibilityEvent.getSource();
        if (source == null) {
            return;
        }
        Log.d(TAG, "onAccessibilityEvent: getCollectionInfo " + source.getCollectionInfo());
        Log.d(TAG, "onAccessibilityEvent: getLabeledBy " + source.getLabeledBy());
        Log.d(TAG, "onAccessibilityEvent: getViewIdResourceName " + source.getViewIdResourceName());
        Log.d(TAG, "onAccessibilityEvent: describeContents " + source.describeContents());
        Log.d(TAG, "onAccessibilityEvent: getClassName " + source.getClassName());
        Log.d(TAG, "onAccessibilityEvent: getContentDescription " + source.getContentDescription());
    }


    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        Log.d(TAG, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

//        info.eventTypes = AccessibilityEvent.TYPE_WINDOWS_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;

        info.packageNames = new String[]{"com.stevenbenack.watchpass", "com.touchtype.swiftkey.beta",
                "com.android.inputmethod.latin"};

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;

        info.notificationTimeout = 0;
        this.setServiceInfo(info);
    }
}
