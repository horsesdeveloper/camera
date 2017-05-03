package com.horses.camera.library;

import android.app.Application;

import app.horses.camera.CameraManager;

/**
 * @author Brian Salvattore
 */
public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        /** Simple Usage (default values) */
        //CameraManager.init(this);

        /** Complete usage */
        CameraManager.init(
                new CameraManager.Builder(this)
                    .setPrimaryColor(R.color.colorPrimary)
                    .enableCropView(true)
                    .enableCropSquare(false)
                    .enableFrontCamera(true)
                    .enableFlash(true)
                    .setQuality(80)
                    .setFormat(CameraManager.FORMAT_WEBP)
                    .build());
    }
}
