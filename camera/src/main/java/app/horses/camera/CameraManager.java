package app.horses.camera;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.ColorRes;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import app.horses.camera.ui.activity.GetActivity;
import app.horses.camera.ui.activity.TakeActivity;
import app.horses.camera.util.CameraException;
import app.horses.camera.util.Constants;
import app.horses.camera.util.Methods;

import static app.horses.camera.util.Constants.REQUEST_TAKE;

/**
 * @author Brian Salvattore
 */
public class CameraManager {

    private static final String TAG = CameraManager.class.getSimpleName();
    private CallbackManager callbackManager;

    public static final int FORMAT_JPG = Constants.FORMAT_JPG;
    public static final int FORMAT_PNG = Constants.FORMAT_PNG;
    public static final int FORMAT_WEBP = Constants.FORMAT_WEBP;

    private static CameraManager instance;

    private Builder builder;

    public static CameraManager getInstance() {
        return instance;
    }

    public CameraManager(Builder builder) {
        this.builder = builder;
        Methods.init(builder.application);
    }

    public static void init(Application application) {
        instance = new Builder(application)
                /*.setPackageName(CameraManager.class.getPackage().getName())
                .setPrimaryColor(R.color.defaultColor)*/
                .build();
    }

    public static void openCamera(Activity activity) {

        activity.startActivityForResult(new Intent(activity, TakeActivity.class), REQUEST_TAKE);
    }

    public static void openCamera(Activity activity,String path) {
        Intent intent=new Intent(activity, TakeActivity.class);
        intent.putExtra(Constants.EXTRA_FOLDER_PATH,path);
        activity.startActivityForResult(intent, REQUEST_TAKE);
    }

    public static void init(CameraManager cameraManager) {
        instance = cameraManager;
    }

    public Builder getBuilder() {
        return builder;
    }

    public static Application getApplication() {
        return instance.builder.application;
    }

    @ColorRes
    public static  int getColorPrimary() {
        return instance.builder.primaryColor;
    }

    public static boolean isCropSquare() {
        return instance.builder.cropSquare;
    }

    public static boolean isCropView() {
        return instance.builder.cropView;
    }

    public static boolean isGallery() {
        return instance.builder.gallery;
    }

    public static boolean isFrontCamera() {
        return instance.builder.frontCamera;
    }

    public static boolean isFlash() {
        return instance.builder.flash;
    }

    public static int getQuality() {
        return instance.builder.quality;
    }

    @Format
    public static int getFormat() {
        return instance.builder.format;
    }

    public static void openCamera2(Activity activity) {

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            CallbackManager.getCallback().onFailureCamera(new CameraException("permission denied for camera"));
            return;
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            CallbackManager.getCallback().onFailureCamera(new CameraException("permission denied for storage"));
            return;
        }

        activity.startActivityForResult(new Intent(activity, GetActivity.class), REQUEST_TAKE);
    }

    @IntDef({FORMAT_JPG, FORMAT_PNG, FORMAT_WEBP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Format {
    }

    @SuppressWarnings("WeakerAccess")
    public static class Builder {

        protected String packageName = CameraManager.class.getPackage().getName();
        protected int primaryColor = 0;

        protected boolean cropSquare = false;
        protected boolean gallery = false;
        protected boolean frontCamera = false;
        protected boolean cropView = false;
        protected boolean flash = false;

        protected int quality = 100;
        protected int format = CameraManager.FORMAT_JPG;

        protected Application application;

        public Builder(Application application) {
            this.application = application;
        }

        public Builder setPackageName(@StringRes int packageName) {
            this.packageName = application.getText(packageName).toString();
            return this;
        }

        public Builder setPackageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder setPrimaryColor(@ColorRes int primaryColor) {
            this.primaryColor = primaryColor;
            return this;
        }

        public Builder enableCropSquare(boolean cropSquare) {
            this.cropSquare = cropSquare;
            return this;
        }

        public Builder enableGallery(boolean gallery) {
            this.gallery = gallery;
            return this;
        }

        public Builder enableFrontCamera(boolean frontCamera) {
            this.frontCamera = frontCamera;
            return this;
        }

        public Builder enableFlash(boolean flash) {
            this.flash = flash;
            return this;
        }

        public Builder setQuality(int quality) {
            this.quality = quality;
            return this;
        }

        public Builder setFormat(@Format int format) {
            this.format = format;
            return this;
        }

        public Builder enableCropView(boolean cropView) {
            this.cropView = cropView;
            return this;
        }

        public CameraManager build() {
            return new CameraManager(this);
        }
    }
}
