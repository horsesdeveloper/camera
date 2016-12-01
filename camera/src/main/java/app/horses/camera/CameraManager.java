package app.horses.camera;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;

import app.horses.camera.ui.activity.TakeActivity;
import app.horses.camera.util.Methods;

import static app.horses.camera.util.Constants.REQUEST_TAKE;

/**
 * @author Brian Salvattore
 */
public class CameraManager {

    private static final String TAG = CameraManager.class.getSimpleName();
    private CallbackManager callbackManager;

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

    public static void init(CameraManager cameraManager) {
        instance = cameraManager;
    }

    public Builder getBuilder() {
        return builder;
    }

    public static Application getApplication() {
        return instance.builder.application;
    }

    public static int getColorPrimary() {
        return instance.builder.primaryColor;
    }

    @SuppressWarnings("WeakerAccess")
    public static class Builder {

        protected String packageName = CameraManager.class.getPackage().getName();
        protected int primaryColor = 0;

        protected boolean cropSquare = false;
        protected boolean gallery = false;
        protected boolean frontCamera = false;

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

        public CameraManager build() {
            return new CameraManager(this);
        }

        public boolean isCropSquare() {
            return cropSquare;
        }

        public boolean isGallery() {
            return gallery;
        }

        public boolean isFrontCamera() {
            return frontCamera;
        }
    }
}
