package com.horses.camera.library;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import app.horses.camera.CameraManager;
import app.horses.camera.CallbackManager;
import app.horses.camera.view.CallbackView;

public class MainActivity extends AppCompatActivity implements CallbackView {

    private static final String TAG = MainActivity.class.getSimpleName();

    private CallbackManager callbackManager = new CallbackManager();

    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CameraManager.openCamera(this);

        callbackManager.setCallback(this);

        image = (ImageView) findViewById(R.id.image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraManager.openCamera(MainActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void successCamera(String path) {
        Log.i(TAG, "successCamera: " + path);

        path = "file:///" + path;

        ImageLoader.getInstance().displayImage(path, image);
    }

    @Override
    public void errorCamera() {
        Log.i(TAG, "errorCamera");

    }

    @Override
    public void cancelCamera() {
        Log.i(TAG, "cancelCamera");

    }
}
