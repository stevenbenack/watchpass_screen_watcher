package com.stevenbenack.watchpass;

import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.view.WindowManager;

import androidx.annotation.Nullable;

public class VirtualScreenCaptureService extends Service {
    private static final String TAG = "ScreenCapService";

    public static final String EXTRA_RESULT_CODE="resultCode";
    public static final String EXTRA_RESULT_INTENT="resultIntent";

    private static final int VIRTUAL_DISPLAY_FLAG = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
            | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    final private HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName(),
            android.os.Process.THREAD_PRIORITY_BACKGROUND);

    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private Handler handler;
    private MediaProjectionManager mediaProjectionManager;
    private WindowManager windowManager;
    private ImageReadManagerListener imageReadManagerListener;


    @Override
    public void onCreate() {
        super.onCreate();

        mediaProjectionManager = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        mediaProjection = mediaProjectionManager.getMediaProjection(i.getIntExtra(EXTRA_RESULT_CODE, -1),
                (Intent)i.getParcelableExtra(EXTRA_RESULT_INTENT));

        imageReadManagerListener = new ImageReadManagerListener(this);

        MediaProjection.Callback projectionCallback = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                virtualDisplay.release();
            }
        };

        virtualDisplay = mediaProjection.createVirtualDisplay(getResources().getString(R.string.virtual_screen_name),
                imageReadManagerListener.getWidth(), imageReadManagerListener.getHeight(), getResources().getDisplayMetrics().densityDpi,
                VIRTUAL_DISPLAY_FLAG, imageReadManagerListener.getSurface(), null, handler);
        mediaProjection.registerCallback(projectionCallback, handler);

        return(START_NOT_STICKY);
    }

    @Override
    public void onDestroy() {
        mediaProjection.stop();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new AssertionError("No Binding");
    }

    WindowManager getWindowManager() {
        return(windowManager);
    }

    Handler getHandler() {
        return(handler);
    }
}
