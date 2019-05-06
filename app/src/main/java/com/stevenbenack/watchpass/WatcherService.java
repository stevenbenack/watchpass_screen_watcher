package com.stevenbenack.watchpass;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.greenrobot.eventbus.EventBus;

/**
 * Our app accessibility class that listens for specific changes in user actions. In this case, we are listening for
 * when the user does actions from certain apps; specifically, we are listening for when the user starts typing from
 * the keyboard. It then broadcasts on our event bus that the user has typed something.
 * This service is invisible to the user - they cannot see that this service is always running on their phone
 */
public class WatcherService extends AccessibilityService {
    private static final String TAG = "CustomAccessibility";

    /*
     * Class that is called by the Android system when our subscribed Accessibility event occurs. On this event, we
     * broadcast on our event bus that the event occurred.
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo source = accessibilityEvent.getSource();
        if (source == null) {
            return;
        }
        Log.d(TAG, "onAccessibilityEvent");

        // Broadcast that the event happened
        EventBus.getDefault().post(new UserEvents.UserAccessibilityEvent());
    }


    @Override
    public void onInterrupt() {

    }

    /*
     * On the creation / connection of this service (in this case, as soon as the user gives the app
     * Accessibility permission from the app home screen), declare what this service should listen for
     */
    @Override
    protected void onServiceConnected() {
        Log.d(TAG, "onServiceConnected");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

      info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;

        /* Request permission from these packages - in this case, my own app, swiftkey keyboard, and the default
         * Android keyboard. Can add more apps as necessary
        */
        info.packageNames = new String[]{"com.stevenbenack.watchpass", "com.touchtype.swiftkey.beta",
                "com.android.inputmethod.latin"};

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;

        info.notificationTimeout = 0;
        this.setServiceInfo(info);
    }
}
