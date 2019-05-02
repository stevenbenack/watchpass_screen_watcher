package com.stevenbenack.watchpass;

public class ScreenCaptureHelper {
    private static final String TAG = "ScreenCaptureHelper";
    private static final int DO_SCREEN_CAPTURE = 123;

//    public static void startScreenCapture(Activity activity) {
//        // asks permission to record screen
//        MediaProjectionManager mpm = (MediaProjectionManager) activity.getSystemService(MEDIA_PROJECTION_SERVICE);
//        Intent intent = mpm.createScreenCaptureIntent();
//        activity.startActivityForResult(intent, DO_SCREEN_CAPTURE);
//    }
//
//    static boolean handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
//        if (requestCode != DO_SCREEN_CAPTURE) {
//            return false;
//        }
//
//        if (resultCode == Activity.RESULT_OK) {
//            Log.d(TAG, "Starting screen capture service.");
//            activity.startService(ScreenCaptureService.newIntent(activity, resultCode, data));
//        } else {
//            Log.d(TAG, "No screen capture permission");
//        }
//
//        return true;
//    }
}
