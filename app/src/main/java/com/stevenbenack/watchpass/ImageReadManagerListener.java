package com.stevenbenack.watchpass;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class ImageReadManagerListener implements ImageReader.OnImageAvailableListener {
    private static final String TAG = "ImageReadListener";
    private final int width;
    private final int height;
    private final ImageReader imageReader;
    private Bitmap latestBitmap;
    private boolean onDidUserEvent;


    ImageReadManagerListener(VirtualScreenCaptureService svc) {
        this.latestBitmap = null;
        this.onDidUserEvent = false;
        Display display=svc.getWindowManager().getDefaultDisplay();
        Point size=new Point();

        display.getSize(size);

        int width = size.x;
        int height = size.y;

        while ( (width * height) > 2210760) {
            width /= 2;
            height /= 2;
        }

        this.width = width;
        this.height = height;

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 8);
        imageReader.setOnImageAvailableListener(this, svc.getHandler());

        EventBus.getDefault().register(this);
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        final Image image = imageReader.acquireLatestImage();


        if (image != null && onDidUserEvent) {
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            int bitmapWidth = width + rowPadding / pixelStride;

            if (latestBitmap == null || latestBitmap.getWidth() != bitmapWidth || latestBitmap.getHeight() != height) {
                if (latestBitmap != null) {
                    latestBitmap.recycle();
                }
                latestBitmap=Bitmap.createBitmap(bitmapWidth, height, Bitmap.Config.ARGB_8888);
            }

            latestBitmap.copyPixelsFromBuffer(buffer);

            image.close();
            onDidUserEvent = false;


            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap cropped=Bitmap.createBitmap(latestBitmap, 0, 0, width, height);

            cropped.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

            byte[] newPng = byteArrayOutputStream.toByteArray();

//            svc.updateImage(newPng);
            Log.d(TAG, "New Screen capture");
            savePhoto(newPng);
        }
        else if(image != null) {
            image.close();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(UserEvents.UserAccessibilityEvent event) {
        /* Do something */
        Log.d(TAG, "ACCESSIBILITY EVENT HERE");
//        takeScreenshot();
        this.onDidUserEvent = true;
    }

    Surface getSurface() {
        return(imageReader.getSurface());
    }

    private void savePhoto(byte[] newPng) {
        File directory = new File(Environment.getExternalStorageDirectory() + "/Download/WatchPass/");
        if ( !directory.exists() || !directory.isDirectory() ) {
            directory.mkdirs();
        }

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        File myPath = new File(directory, "WatchPassScreenshot" + ts + ".jpg");
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

    int getWidth() {
        return(width);
    }

    int getHeight() {
        return(height);
    }

    void close() {
        imageReader.close();
    }
}
