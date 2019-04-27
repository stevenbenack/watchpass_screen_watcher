package com.stevenbenack.watchpass;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;


public class DrawScreenService extends Service {
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
        floatingDrawnLayout = inflater.inflate(R.layout.draw_over_screen, null);

        final WindowManager.LayoutParams parameters;
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            parameters = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            parameters = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        parameters.x = 0;
        parameters.y = 50;
        parameters.gravity = Gravity.BOTTOM | Gravity.LEFT;

        windowManager.addView(floatingDrawnLayout, parameters);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if ( floatingDrawnLayout != null )
            windowManager.removeView(floatingDrawnLayout);
    }
}
