package com.stevenbenack.watchpass;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class DrawScreenService extends Service {
    private static final String TAG = "DrawScreenService";

    private WindowManager windowManager;
    private View floatingDrawnLayout;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        LayoutInflater inflater = LayoutInflater.from(this);
        floatingDrawnLayout = inflater.inflate(R.layout.activity_draw_over_screen, null);

        final WindowManager.LayoutParams parameters;
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            parameters = new WindowManager.LayoutParams(
//                    WindowManager.LayoutParams.WRAP_CONTENT,
//                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            parameters = new WindowManager.LayoutParams(
//                    WindowManager.LayoutParams.WRAP_CONTENT,
//                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

//        parameters.x = 0;
//        parameters.y = 50;
//        parameters.gravity = Gravity.BOTTOM | Gravity.LEFT;

        windowManager.addView(floatingDrawnLayout, parameters);

        if( floatingDrawnLayout != null ) {
            EventBus.getDefault().register(this);
        }

        takeScreenshot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if( EventBus.getDefault().isRegistered(this) ) {
            EventBus.getDefault().unregister(this);
        }

        if ( floatingDrawnLayout != null ) {
            windowManager.removeView(floatingDrawnLayout);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(UserEvents.UserAccessibilityEvent event) {
        /* Do something */
        Log.d(TAG, "ACCESSIBILITY EVENT HERE");
        takeScreenshot();
    }

    private void takeScreenshot() {
        View v =
                floatingDrawnLayout.getRootView();
//                rootLayout.getRootView();

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
