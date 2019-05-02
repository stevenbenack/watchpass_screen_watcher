package com.stevenbenack.watchpass;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ScreenCaptureService extends Service {
    private static final String TAG = "ScreenCapService";

    static final String EXTRA_RESULT_CODE="resultCode";
    static final String EXTRA_RESULT_INTENT="resultIntent";
    static final int VIRT_DISPLAY_FLAGS=
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private MediaProjection projection;
    private VirtualDisplay vdisplay;
    final private HandlerThread handlerThread=new HandlerThread(getClass().getSimpleName(),
            android.os.Process.THREAD_PRIORITY_BACKGROUND);
    private Handler handler;
    private MediaProjectionManager mgr;
    private WindowManager wmgr;
    private ImageTransmogrifier it;

    @Override
    public void onCreate() {
        super.onCreate();

        mgr=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        wmgr=(WindowManager)getSystemService(WINDOW_SERVICE);

        handlerThread.start();
        handler=new Handler(handlerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        projection=
                mgr.getMediaProjection(i.getIntExtra(EXTRA_RESULT_CODE, -1),
                        (Intent)i.getParcelableExtra(EXTRA_RESULT_INTENT));

        it=new ImageTransmogrifier(this);

        MediaProjection.Callback cb=new MediaProjection.Callback() {
            @Override
            public void onStop() {
                vdisplay.release();
            }
        };

        vdisplay=projection.createVirtualDisplay("andprojector",
                it.getWidth(), it.getHeight(),
                getResources().getDisplayMetrics().densityDpi,
                VIRT_DISPLAY_FLAGS, it.getSurface(), null, handler);
        projection.registerCallback(cb, handler);

        return(START_NOT_STICKY);
    }

    @Override
    public void onDestroy() {
        projection.stop();

        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        ImageTransmogrifier newIt=new ImageTransmogrifier(this);

        if (newIt.getWidth()!=it.getWidth() ||
                newIt.getHeight()!=it.getHeight()) {
            ImageTransmogrifier oldIt=it;

            it=newIt;
            vdisplay.resize(it.getWidth(), it.getHeight(),
                    getResources().getDisplayMetrics().densityDpi);
            vdisplay.setSurface(it.getSurface());

            oldIt.close();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new AssertionError("Not supported.");
    }

    WindowManager getWindowManager() {
        return(wmgr);
    }

    Handler getHandler() {
        return(handler);
    }

    void updateImage(byte[] newPng) {
//        for (WebSocket socket : getWebSockets()) {
//            socket.send("screen/"+Long.toString(SystemClock.uptimeMillis()));
//        }
        Log.d("SCREENSHOT", "HERE");
        savePhoto(newPng);
    }

    private void savePhoto(byte[] newPng) {
        File directory = new File(Environment.getExternalStorageDirectory() + "/Download/WatchPass/");
        if ( !directory.exists() || !directory.isDirectory() ) {
            directory.mkdirs();
        }

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        File myPath = new File(directory, getString(R.string.screenshot_name) + ts + ".jpg");
        Log.d(TAG, "photo path: " + myPath);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(myPath);
            fos.write(newPng);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    protected void buildForegroundNotification(NotificationCompat.Builder b) {
//        Intent iActivity=new Intent(this, MainActivity.class);
//        PendingIntent piActivity=PendingIntent.getActivity(this, 0,
//                iActivity, 0);
//
//        b.setContentTitle(getString(R.string.app_name))
//                .setContentIntent(piActivity)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setTicker(getString(R.string.app_name));
//    }

//    private class ScreenshotRequestCallback
//            implements HttpServerRequestCallback {
//        @Override
//        public void onRequest(AsyncHttpServerRequest request,
//                              AsyncHttpServerResponse response) {
//            response.setContentType("image/png");
//
//            byte[] png=latestPng.get();
//            ByteArrayInputStream bais=new ByteArrayInputStream(png);
//
//            response.sendStream(bais, png.length);
//        }
//    }
}
