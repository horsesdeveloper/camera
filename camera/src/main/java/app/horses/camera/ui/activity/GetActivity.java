package app.horses.camera.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.media.ExifInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.cameraview.CameraView;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import app.horses.camera.CameraManager;
import app.horses.camera.R;
import app.horses.camera.util.CameraException;
import app.horses.camera.util.CameraUtil;
import app.horses.camera.util.ColorUtils;

/*import com.edmodo.cropper.CropImageView;*/

/**
 * @author Brian Salvattore
 */
public class GetActivity extends AppCompatActivity {

    private static final String TAG = GetActivity.class.getSimpleName();

    private int step = 0;

    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    private CameraView cameraView;
    //private ImageView thumbnail;
    private View shutter;
    private CropImageView preview;
    private View controllersCamera;
    private View controllersAccept;
    private SpinKitView spin;

    // TODO: 2/05/2017 change local
    @SuppressWarnings("FieldCanBeLocal")
    private boolean cropSquare = false;
    private boolean cropView = false;

    final private int[] flashModes = new int[] { R.drawable.ic_flash_auto_white_24dp, R.drawable.ic_flash_on_white_24dp, R.drawable.ic_flash_off_white_24dp };
    final private int[] facingModes = new int[] { R.drawable.ic_camera_front_white_24dp, R.drawable.ic_camera_rear_white_24dp };
    private int facing = CameraView.FACING_BACK;
    private int flash = CameraView.FLASH_AUTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(0x80000000);
            window.setStatusBarColor(CameraUtil.darkenColor(ColorUtils.getPrimaryColor()));
        }

        cameraView = (CameraView) findViewById(R.id.cameraView);
        //thumbnail = (ImageView) findViewById(R.id.thumbnail);
        shutter = findViewById(R.id.shutter);
        preview = (CropImageView) findViewById(R.id.preview);
        controllersCamera = findViewById(R.id.camera);
        controllersAccept = findViewById(R.id.accept);
        spin = (SpinKitView) findViewById(R.id.spin);

        spin.setColor(ColorUtils.getPrimaryColor());

        cropView = CameraManager.isCropView();
        cropSquare = CameraManager.isCropSquare();

        findViewById(R.id.flash).setVisibility(CameraManager.isFlash() ? View.VISIBLE : View.GONE);
        findViewById(R.id.facing).setVisibility(CameraManager.isFrontCamera() ? View.VISIBLE : View.GONE);
        findViewById(R.id.layout).setBackgroundColor(ColorUtils.getPrimaryColor());

        preview.setFixedAspectRatio(cropSquare);
        preview.setScaleType(CropImageView.ScaleType.FIT_CENTER);

        cameraView.addCallback(getCameraCallback());
        cameraView.setFacing(facing);
        cameraView.setFlash(flash);
        cameraView.setAutoFocus(true);
        cameraView.setAdjustViewBounds(true);
        forceMacroFocusMode();

        findViewById(R.id.take).setOnClickListener(getTakeListener());
        findViewById(R.id.retry).setOnClickListener(getRetryListener());
        findViewById(R.id.save).setOnClickListener(getSaveListener());
        findViewById(R.id.facing).setOnClickListener(getFacingListener());
        findViewById(R.id.flash).setOnClickListener(getFlashListener());

        if (!isFlashEnable()) {
            findViewById(R.id.flash).setVisibility(View.GONE);
            findViewById(R.id.flash).setOnClickListener(null);
            Log.wtf(TAG, "onCreate: ", new CameraException("flash is not support"));
        }

        if (!isFrontCameraEnable()) {
            findViewById(R.id.facing).setVisibility(View.GONE);
            findViewById(R.id.facing).setOnClickListener(null);
            Log.wtf(TAG, "onCreate: ", new CameraException("front camera is not support"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        switch (step) {
            case 0:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case 1:
                step = 0;

                preview.setVisibility(View.GONE);

                controllersCamera.setVisibility(View.VISIBLE);
                controllersAccept.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged() called with: newConfig = [" + newConfig.orientation + "]");
    }

    private Bitmap compressBitmap(byte[] data) {

        Matrix matrix = new Matrix();

        try {
            ExifInterface exif = new ExifInterface(new ByteArrayInputStream(data));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d(TAG, "compressBitmap() called with: orientation = [" + orientation + "]");

            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    break;
                /*case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;*/
                case ExifInterface.ORIENTATION_ROTATE_90:
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(90);
                    break;
                /*case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;*/
            }
        }
        catch (IOException ignore) { }

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        final int widthBitmap = bitmap.getWidth();
        final int heightBitmap = bitmap.getHeight();

        Log.d(TAG, "compressBitmap() called with: widthBitmap = [" + widthBitmap + "], heightBitmap = [" + heightBitmap + "]");

        if (widthBitmap > heightBitmap) {
            matrix.setRotate(90);
        }

        final int maxWidth = 1800;
        float newScale = (float) (maxWidth * 1.0 / widthBitmap);

        matrix.postScale(newScale, newScale);

        if (facing == CameraView.FACING_FRONT) {
            matrix.postScale(1, -1, newScale/2, newScale/2);
        }

        try {
            return Bitmap.createBitmap(bitmap, 0, 0, widthBitmap, heightBitmap, matrix, true);
        }
        finally {
            bitmap.recycle();
        }
    }

    private String saveFile(Bitmap bitmap) {

        int quality = CameraManager.getQuality();
        Bitmap.CompressFormat format;
        String suffix;

        switch (CameraManager.getFormat()) {
            case CameraManager.FORMAT_JPG:
                suffix = "jpg";
                format = Bitmap.CompressFormat.JPEG;
                break;
            case CameraManager.FORMAT_PNG:
                suffix = "png";
                format = Bitmap.CompressFormat.PNG;
                break;
            case CameraManager.FORMAT_WEBP:
            default:
                suffix = "webp";
                format = Bitmap.CompressFormat.WEBP;
                break;
        }


        String name = String.format(getCacheDir()  + "/%s.%s", new Date().getTime(), suffix);

        File file = new File(name);

        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            bitmap.compress(format, quality, os);
            os.flush();
            os.close();
        }
        catch (IOException e) {
            Log.e(TAG, "doInBackground: file=[" + name + "]", e);
        }
        finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException ignore) { }
            }
        }

        Log.d(TAG, "saveFile() called with: name = [" + name + "]");
        Log.d(TAG, "saveFile() called with: file = [" + (file.length() / 1024) + "]");

        return name;
    }

    private void savePhoto(Bitmap bitmap) {
        new AsyncTask<Bitmap, Void, String>() {

            @Override
            protected String doInBackground(Bitmap... bitmaps) {
                return saveFile(bitmaps[0]);
            }

            @Override
            protected void onPostExecute(String file) {
                spin.setVisibility(View.GONE);

                Intent intent = new Intent();
                intent.putExtra("uri", file);

                setResult(RESULT_OK, intent);
                finish();
            }
        }.execute(bitmap);
    }

    private void savePhotoWithoutPreview(byte[] data) {
        new AsyncTask<byte[], Void, String>() {

            @Override
            protected String doInBackground(byte[]... bytes) {
                return saveFile(compressBitmap(bytes[0]));
            }

            @Override
            protected void onPostExecute(String file) {
                spin.setVisibility(View.GONE);

                Intent intent = new Intent();
                intent.putExtra("uri", file);

                setResult(RESULT_OK, intent);
                finish();
            }
        }.execute(data);
    }

    private void executePreview(byte[] data) {
        new AsyncTask<byte[], Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(byte[]... bytes) {
                return compressBitmap(bytes[0]);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                step = 1;

                spin.setVisibility(View.GONE);
                findViewById(R.id.take).setEnabled(true);

                //thumbnail.setVisibility(View.GONE);
                preview.setVisibility(View.VISIBLE);

                controllersCamera.setVisibility(View.GONE);
                controllersAccept.setVisibility(View.VISIBLE);

                preview.setImageBitmap(bitmap);
            }
        }.execute(data);
    }

    private void animateShutter() {
        shutter.setVisibility(View.VISIBLE);
        shutter.setAlpha(0.f);

        ObjectAnimator alphaInAnim = ObjectAnimator.ofFloat(shutter, "alpha", 0f, 0.8f);
        alphaInAnim.setDuration(100);
        alphaInAnim.setStartDelay(100);
        alphaInAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator alphaOutAnim = ObjectAnimator.ofFloat(shutter, "alpha", 0.8f, 0f);
        alphaOutAnim.setDuration(200);
        alphaOutAnim.setInterpolator(DECELERATE_INTERPOLATOR);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(alphaInAnim, alphaOutAnim);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                shutter.setVisibility(View.GONE);
            }
        });

        animatorSet.start();
    }

    private CameraView.Callback getCameraCallback() {
        return new CameraView.Callback() {
            @Override
            public void onPictureTaken(final CameraView cameraView, final byte[] data) {
                if (cropView) {
                    executePreview(data);
                }
                else {
                    savePhotoWithoutPreview(data);
                }
            }
        };
    }

    @SuppressWarnings("deprecation")
    private int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    private boolean isFlashEnable() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private boolean isFrontCameraEnable() {
        return getNumberOfCameras() == 2;
    }

    @SuppressWarnings("deprecation")
    private void forceMacroFocusMode() {
        cameraView.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
    }

    private View.OnClickListener getFacingListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facing = facing == CameraView.FACING_BACK ? CameraView.FACING_FRONT : CameraView.FACING_BACK;
                cameraView.setFacing(facing);
                int drawable = facing == CameraView.FACING_BACK ? facingModes[1] : facingModes[0];
                ((ImageButton) v).setImageDrawable(ContextCompat.getDrawable(GetActivity.this, drawable));

                if (CameraManager.isFlash()) findViewById(R.id.flash).setVisibility(facing == CameraView.FACING_BACK ? View.VISIBLE : View.GONE);
            }
        };
    }

    private View.OnClickListener getFlashListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int drawable;
                switch (flash) {
                    case CameraView.FLASH_AUTO:
                        drawable = flashModes[1];
                        flash = CameraView.FLASH_ON;
                        break;
                    case CameraView.FLASH_ON:
                        drawable = flashModes[2];
                        flash = CameraView.FLASH_OFF;
                        break;
                    case CameraView.FLASH_OFF:
                    default:
                        drawable = flashModes[0];
                        flash = CameraView.FLASH_AUTO;
                        break;
                }
                cameraView.setFlash(flash);
                ((ImageButton) v).setImageDrawable(ContextCompat.getDrawable(GetActivity.this, drawable));
            }
        };
    }

    private View.OnClickListener getTakeListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spin.setVisibility(View.VISIBLE);
                findViewById(R.id.take).setEnabled(false);
                animateShutter();
                cameraView.takePicture();
            }
        };
    }

    private View.OnClickListener getSaveListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spin.setVisibility(View.VISIBLE);
                savePhoto(preview.getCroppedImage());
            }
        };
    }

    private View.OnClickListener getRetryListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        };
    }
}
