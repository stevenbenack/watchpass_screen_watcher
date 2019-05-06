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

/**
 * Service starts from the main screen. This service stays in the background and always runs, even outside of the app.
 * Within the service, we create a virtual display / projection of the current screen. We basically emulate the screen
 * on top of the current screen within the service. Because we create the virtual display within our own service,
 * Android allows us to take screenshots of this.
 */
public class VirtualScreenCaptureService extends Service {
    private static final String TAG = "ScreenCapService";

    public static final String EXTRA_RESULT_CODE = "resultCode";
    public static final String EXTRA_RESULT_INTENT = "resultIntent";

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

    // On create, start our projection of the screen and run this projection in the background (using the looper handler)
    @Override
    public void onCreate() {
        super.onCreate();

        mediaProjectionManager = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    /* On start of the service, create the media projection of the virtual screen on top of the current screen.
     * This is a projection of a virtual display of the current screen - the user won't know the current screen is
     * actually just a projection of a virtual screen on top of the current screen and not actually the screen.
     */
    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        mediaProjection = mediaProjectionManager.getMediaProjection(i.getIntExtra(EXTRA_RESULT_CODE, -1),
                (Intent)i.getParcelableExtra(EXTRA_RESULT_INTENT));

        // Image read manager to listen for when virtual display has an image available to screenshot
        imageReadManagerListener = new ImageReadManagerListener(this);

        /* virtual display that we project onto the user's screen. It has the same dimensions as the current user's
         * current screen; the user does not notice any difference in the projection. Based on my debugging, this does
         * consume some amount of processing power, with any reasonably new device, this will not be noticeable to the
         * user
         */
        virtualDisplay = mediaProjection.createVirtualDisplay(getResources().getString(R.string.virtual_screen_name),
                imageReadManagerListener.getWidth(), imageReadManagerListener.getHeight(), getResources().getDisplayMetrics().densityDpi,
                VIRTUAL_DISPLAY_FLAG, imageReadManagerListener.getSurface(), null, handler);

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
