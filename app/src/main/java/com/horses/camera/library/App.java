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
        /*CameraManager.init(
                new CameraManager.Builder(this)
                    .setPrimaryColor(R.color.colorAccent)
                    .enableCrop(true)
                    .build());*/

        /** Customization */
        CameraManager.init(
                new CameraManager.Builder(this)
                        .setPrimaryColor(R.color.colorAccent)
                        .setCaptureIcon(R.drawable.custom_shutter)
                        .setSaveText("Guardar")
                        .setRetryText("Nueva")
                        .enableCropSquare(false)
                        .enableCrop(true)
                        .enableFrontCamera(true)
                        .build());
    }
}
