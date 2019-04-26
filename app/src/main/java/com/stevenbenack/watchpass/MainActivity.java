package com.stevenbenack.watchpass;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int EXTERN_WRITE_PERMISSION_CODE = 1;

    @BindView(R.id.password_field)
    EditText passwordField;
    @BindView(R.id.screenshot_button)
    Button testButton;
    @BindView(R.id.root_layout)
    ConstraintLayout rootLayout;
    @BindView(R.id.floating_screen_button)
    Button floatingScreenButton;

    private PermissionRequestHandler permissionRequestHandler = new PermissionRequestHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if ( !permissionRequestHandler.hasAccessibilityServicePermissions(this, WatcherService.class) ) {
            // ask user to allow permission
            Log.d(TAG, "Does not have Watcher Accessibility Service Enabled");
            showAccessibilityPermissionDialog();
        }

        if ( !permissionRequestHandler.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {
            permissionRequestHandler.requestUnGrantedPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERN_WRITE_PERMISSION_CODE);
        }
        Log.d(TAG, "has write permission? " +
                permissionRequestHandler.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE));

        testButton.setOnClickListener(v -> takeScreenshot());
    }

    private void showAccessibilityPermissionDialog() {
        AlertDialog.Builder permissionDialog = new AlertDialog.Builder(this);
        permissionDialog.setTitle(R.string.dialog_title);
        permissionDialog.setMessage(R.string.dialog_text);
        permissionDialog.setPositiveButton(R.string.dialog_affirmative_button_text, (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, 0);
        });
        permissionDialog.setNegativeButton(R.string.dialog_cancel_button_text, (dialog, which) -> dialog.dismiss());
        permissionDialog.show();
    }


    private void takeScreenshot() {
        View v = rootLayout.getRootView();
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        v.setDrawingCacheEnabled(true);
        Bitmap b = v.getDrawingCache();
        File directory = new File(Environment.getExternalStorageDirectory() + "/Download/WatchPass/");
        if ( !directory.exists() || !directory.isDirectory() ) {
            directory.mkdirs();
        }

        File myPath = new File(directory, getString(R.string.screenshot_name) + ".jpg");
        Log.d(TAG, "photo path: " + myPath);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(myPath);
            b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), b, "Screen", "screen");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
