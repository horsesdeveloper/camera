package com.horses.camera.library;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

import java.io.File;

import app.horses.camera.CameraManager;
import app.horses.camera.CallbackManager;
import app.horses.camera.view.CallbackView;

public class MainActivity extends AppCompatActivity implements CallbackView {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Context context;
    private CallbackManager callbackManager = new CallbackManager();

    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;

        // Default Use
        // CameraManager.openCamera(this);

        File root = Environment.getExternalStorageDirectory();
        File dirBase=new File(root, "horsesCamera");

        // Custom Path
        //CameraManager.openCamera(this,dirBase.getPath());

        // Custom path and filename without extension
        CameraManager.openCamera(this,dirBase.getPath(),"MyPhoto");

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

        Picasso.with(context).load(path).into(image);
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
