package app.horses.camera.ui.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import app.horses.camera.CameraManager;
import app.horses.camera.R;
import app.horses.camera.util.CameraUtil;
import app.horses.camera.util.ColorUtils;
import app.horses.camera.util.Constants;
import app.horses.camera.util.Methods;
import app.horses.camera.util.SimpleAnimatorListener;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static app.horses.camera.util.Constants.EXTRA_FILENAME_PATH;
import static app.horses.camera.util.Constants.EXTRA_FOLDER_PATH;
import static app.horses.camera.util.Constants.RESULT_ERROR;

@SuppressWarnings("deprecation")
public class TakeActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        EasyPermissions.PermissionCallbacks {

    private static final String TAG = TakeActivity.class.getSimpleName();
    private static final int RC_WRITESD_PERMISSIONS_REQUIRED =101;

    private static final int PHOTO_SIZE = 1200;
    private static final int MIN_PREVIEW_PIXELS = 480 * 320;
    private static final double MAX_ASPECT_DISTORTION = 0.15;

    public static final int CAMERA_POSITION_UNKNOWN = 0;
    public static final int CAMERA_POSITION_FRONT = 1;
    public static final int CAMERA_POSITION_BACK = 2;

    public static final int FLASH_MODE_OFF = 0;
    public static final int FLASH_MODE_ALWAYS_ON = 1;
    public static final int FLASH_MODE_AUTO = 2;

    private int cameraPosition = CAMERA_POSITION_UNKNOWN;
    private int flashMode = FLASH_MODE_OFF;
    private float pointX, pointY;
    private static final int FOCUS = 1;
    private static final int ZOOM = 2;
    private int mode;
    private float dist;
    private int curZoomValue = 0;

    private boolean isCropEnabled = false;
    private boolean isSquare = false;

    private int step = 0;

    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    protected SurfaceView surface;
    protected LinearLayout layout;
    protected ImageView take;
    protected CropImageView preview;
    protected View shutter;
    protected View ripple;
    protected View controllersCamera;
    protected View controllersAccept;
    protected Button retry;
    protected Button save;

    private Bitmap saveBitmap = null;

    private Camera camera;
    private SurfaceHolder holder;

    private int width = 0;
    private int height = 0;

    private String folderPath;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_FOLDER_PATH)) {
            folderPath = intent.getStringExtra(EXTRA_FOLDER_PATH);
        }

        if (intent.hasExtra(EXTRA_FILENAME_PATH)) {
            fileName = intent.getStringExtra(EXTRA_FILENAME_PATH);
        }

        methodRequirePermissions();
    }

    private void initActivity(){
        setContentView(R.layout.activity_take);

        // TODO: 18/11/2016 transparent status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            /*getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);*/
            getWindow().setStatusBarColor(CameraUtil.darkenColor(ColorUtils.getPrimaryColor()));
        }

        //Prevent screen change
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (CameraManager.getInstance() != null) {
            isCropEnabled = CameraManager.getInstance().getBuilder().isCropEnabled();
        }

        if (CameraManager.getInstance() != null) {
            isSquare = CameraManager.getInstance().getBuilder().isCropSquare();
        }

        surface = (SurfaceView) findViewById(R.id.surface);
        layout = (LinearLayout) findViewById(R.id.layout);
        take = (ImageView) findViewById(R.id.take);
        preview = (CropImageView) findViewById(R.id.preview);
        shutter = findViewById(R.id.shutter);
        ripple = findViewById(R.id.ripple);
        controllersCamera = findViewById(R.id.camera);
        controllersAccept = findViewById(R.id.accept);
        retry = (Button) findViewById(R.id.retry);
        save = (Button) findViewById(R.id.save);

        layout.setBackgroundColor(ColorUtils.getPrimaryColor());

        //Camera controls customization
        if (CameraManager.getInstance() != null) {
            CameraManager.Builder cameraBuilder=CameraManager.getInstance().getBuilder();

            String retryText=cameraBuilder.getRetryText();
            retry.setText(retryText);
            retry.setTransformationMethod(null);

            String saveText = cameraBuilder.getSaveText();
            save.setText(saveText);
            save.setTransformationMethod(null);

            take.setImageResource(cameraBuilder.getIconCaptureIcon());

            isSquare = CameraManager.getInstance().getBuilder().isCropSquare();
        }


        width = Methods.getWidthScreen();
        height = Methods.getHeightScreen() - Methods.toPixels(80);
        Log.d(TAG, "onCreate() called with: width = [" + width + "], height = [" + height + "]");

        holder = surface.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.setKeepScreenOn(true);

        surface.setFocusable(true);
        surface.setBackgroundColor(TRIM_MEMORY_BACKGROUND);
        surface.getHolder().addCallback(this);

        //region surface listeners
        surface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:

                        pointX = event.getX();
                        pointY = event.getY();
                        mode = FOCUS;
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:

                        dist = spacing(event);

                        if (spacing(event) > 10f) {

                            mode = ZOOM;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:

                        mode = FOCUS;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mode == FOCUS) {

                            // FIXME: 12/10/2016 in old method this line is deprecated
                            pointFocus((int) event.getRawX(), (int) event.getRawY());
                        } else if (mode == ZOOM) {

                            float newDist = spacing(event);

                            if (newDist > 10f) {

                                float tScale = (newDist - dist) / dist;

                                if (tScale < 0) {

                                    tScale = tScale * 10;
                                }

                                addZoomIn((int) tScale);
                            }
                        }
                        break;
                }
                return false;
            }
        });

        surface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if(camera!=null){
                        pointFocus((int) pointX, (int) pointY);
                        showRipple((int) pointX, (int) pointY);
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        });
        //endregion

        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateShutter();
                previewPicture();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bitmap bitmapToSave=null;
                if(isCropEnabled){
                    bitmapToSave = preview.getCroppedImage();
                } else {
                    bitmapToSave=saveBitmap;
                }

                File filesDir;
                if(folderPath!=null){
                    filesDir = new File(folderPath);

                    //Create folder if not Exists
                    if(!filesDir.exists()){
                        filesDir.mkdirs();
                    }
                } else {
                    filesDir = getFilesDir();
                }

                File f = persistImage(bitmapToSave,filesDir);

                Log.i(TAG, "file size: " + (f.length() / 1024) + "kb");

                Intent intent = new Intent();
                intent.putExtra("uri", f.getPath());

                setResult(RESULT_OK, intent);
                finish();

                bitmapToSave.recycle();
            }
        });

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private File persistImage(Bitmap bitmap, File filesDir) {

        String name="";
        if(fileName!=null){
            name = String.format(filesDir  + "/%s.jpg", fileName);
        } else {
            name = String.format(filesDir  + "/%s.jpg", new Date().getTime());
        }

        File imageFile = new File(name);

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }

        return imageFile;
    }

    private void showRipple(int pointX, int pointY) {
        Log.d(TAG, "showRipple() called with: pointX = [" + pointX + "], pointY = [" + pointY + "]");

        final int size = Methods.toPixels(160);
        pointX = pointX - (size / 2);
        pointY = pointY - (size / 2);

        Log.d(TAG, "showRipple() called with: pointX = [" + pointX + "], pointY = [" + pointY + "]");

        ripple.animate().setDuration(0).scaleX(0).scaleY(0).x(pointX).y(pointY).alpha(0).setListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                ripple.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ripple.animate().setDuration(300).scaleX(1).scaleY(1).alpha(0.5f).setListener(new SimpleAnimatorListener(){
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        ripple.animate().setDuration(500).scaleX(2).scaleY(2).alpha(0.2f).setListener(new SimpleAnimatorListener(){
                            @Override
                            public void onAnimationEnd(Animator animator) {
                                ripple.setVisibility(View.GONE);
                            }
                        }).start();
                    }
                }).start();
            }
        }).start();
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

                surface.setVisibility(View.VISIBLE);
                preview.setVisibility(View.GONE);

                controllersCamera.setVisibility(View.VISIBLE);
                controllersAccept.setVisibility(View.GONE);

                surface.getHolder().removeCallback(TakeActivity.this);
                surfaceCreated(surface.getHolder());
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (null == camera) {

            try {

                camera = Camera.open(0);
                camera.setPreviewDisplay(holder);
                initCamera();
                camera.startPreview();
            }
            catch (Throwable e) {

                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        autoFocus();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        try {

            if (camera != null) {

                camera.stopPreview();
                camera.release();
                camera = null;
            }
        }
        catch (Exception ignored) {
            Log.d(TAG, "surfaceDestroyed: " + ignored.toString());
        }
    }

    private void autoFocus() {
        new Thread() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                try {

                    sleep(100);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (camera == null) {

                    return;
                }

                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {

                        if (success) {

                            initCamera();
                        }
                    }
                });
            }
        };
    }

    private void initCamera() {

        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);

        Camera.Size preview = findBestPreviewResolution(parameters, width, height);
        Camera.Size size = findBestPictureResolution(parameters, width, height);

        parameters.setPictureSize(size.width, size.height);
        parameters.setPreviewSize(preview.width, preview.height);

        parameters.setFocusMode(getFocusMode(parameters));

        Log.i(TAG, "initCamera: " + getFocusMode(parameters));

        setDisplay(parameters, camera);

        try {
            camera.setParameters(parameters);
        } catch (Exception e) {
            Log.wtf(TAG, "initCamera: ", e);
        }

        camera.startPreview();
        camera.cancelAutoFocus();
    }

    private void previewPicture() {
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] bytes, Camera camera1) {
                camera.stopPreview();
                camera.release();
                camera = null;

                new PreviewImageTask().execute(bytes);
            }
        });
    }

    private class PreviewImageTask extends AsyncTask<byte[], Void, Bitmap> {

        private Context context = TakeActivity.this;
        private ProgressDialog dialog = new ProgressDialog(context);

        @Override
        protected void onPreExecute() {
            dialog.setMessage(context.getResources().getString(R.string.dialog_loading));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Bitmap doInBackground(byte[]... bytes) {

            byte[] data = bytes[0];

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            Log.d(TAG, "doInBackground() called with: width = [" + width + "], height = [" + height + "]");

            final int maxWidth = 1800;
            float newScale = (float) (maxWidth * 1.0 / width);

            Matrix matrix = new Matrix();
            matrix.postScale(newScale, newScale);
            matrix.postRotate(90);

            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

            bitmap.recycle();

            Log.d(TAG, "doInBackground() called with: width = [" + resizedBitmap.getWidth() + "], height = [" + resizedBitmap.getHeight() + "]");

            return resizedBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            dialog.dismiss();

            if (bitmap == null) {
                Log.wtf(TAG, "onPostExecute: bitmap is null");

                setResult(RESULT_ERROR);
                finish();
                return;
            }

            step = 1;

            saveBitmap = bitmap;
            controllersCamera.setVisibility(View.GONE);
            controllersAccept.setVisibility(View.VISIBLE);

            //Show crop options
            if(isCropEnabled){
                surface.setVisibility(View.GONE);
                preview.setVisibility(View.VISIBLE);

                preview.setImageBitmap(saveBitmap);
                preview.setFixedAspectRatio(isSquare);
                preview.setScaleType(CropImageView.ScaleType.CENTER_INSIDE);
            }
        }
    }

    private Camera.Size findBestPreviewResolution(Camera.Parameters parameters, int w, int h) {

        Camera.Size defaultPreviewResolution = parameters.getPreviewSize();

        List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();

        if (rawSupportedSizes == null)
            return defaultPreviewResolution;

        List<Camera.Size> supportedPreviewResolutions = new ArrayList<>(rawSupportedSizes);

        Collections.sort(supportedPreviewResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {

                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;

                if (bPixels < aPixels) {

                    return -1;
                }

                if (bPixels > aPixels) {

                    return 1;
                }

                return 0;
            }
        });

        StringBuilder previewResolutionSb = new StringBuilder();

        for (Camera.Size supportedPreviewResolution : supportedPreviewResolutions) {

            previewResolutionSb.append(supportedPreviewResolution.width).append('x').append(supportedPreviewResolution.height).append(' ');
        }

        Log.v(TAG, "Supported preview resolutions: " + previewResolutionSb);

        double screenAspectRatio = (double) w / (double) h;

        Iterator<Camera.Size> it = supportedPreviewResolutions.iterator();

        while (it.hasNext()) {

            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            if (width * height < MIN_PREVIEW_PIXELS) {

                it.remove();
                continue;
            }

            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;

            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {

                it.remove();
                continue;
            }

            if (maybeFlippedWidth == w && maybeFlippedHeight == h) {

                return supportedPreviewResolution;
            }
        }

        if (!supportedPreviewResolutions.isEmpty()) {

            return supportedPreviewResolutions.get(0);
        }

        return defaultPreviewResolution;
    }

    private Camera.Size findBestPictureResolution(Camera.Parameters parameters, int w, int h) {

        List<Camera.Size> supportedPicResolutions = parameters.getSupportedPictureSizes();

        StringBuilder picResolutionSb = new StringBuilder();

        for (Camera.Size supportedPicResolution : supportedPicResolutions) {

            picResolutionSb.append(supportedPicResolution.width).append('x').append(supportedPicResolution.height).append(" ");
        }

        Log.d(TAG, "Supported picture resolutions: " + picResolutionSb);

        Camera.Size defaultPictureResolution = parameters.getPictureSize();

        Log.d(TAG, "default picture resolution " + defaultPictureResolution.width + "x"
                + defaultPictureResolution.height);

        List<Camera.Size> sortedSupportedPicResolutions = new ArrayList<>(supportedPicResolutions);

        Collections.sort(sortedSupportedPicResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {

                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        double screenAspectRatio = (double) w / (double) h;
        Iterator<Camera.Size> it = sortedSupportedPicResolutions.iterator();

        while (it.hasNext()) {

            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);

            if (distortion > MAX_ASPECT_DISTORTION) {

                it.remove();
            }
        }

        if (!sortedSupportedPicResolutions.isEmpty()) {

            return sortedSupportedPicResolutions.get(0);
        }

        return defaultPictureResolution;
    }

    private String getFocusMode(Camera.Parameters parameters) {

        String mode = Camera.Parameters.FOCUS_MODE_AUTO;

        List<String> focusModes = parameters.getSupportedFocusModes();

        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            mode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
         /*else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO))
             mode = Camera.Parameters.FOCUS_MODE_MACRO;*/

        return mode;
    }

    private void setDisplay(Camera.Parameters parameters, Camera camera) {

        if (Build.VERSION.SDK_INT >= 8) {

            setDisplayOrientation(camera, 90);
        }
        else {

            parameters.setRotation(90);
        }
    }

    private void setDisplayOrientation(Camera camera, int i) {

        Method downPolymorphic;

        try {

            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", int.class);

            if (downPolymorphic != null) {

                downPolymorphic.invoke(camera, i);
            }
        }
        catch (Exception ignore) { }
    }

    private float spacing(MotionEvent event) {

        if (event == null)
            return 0;

        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
    }

    private void addZoomIn(int delta) {

        try {

            Camera.Parameters params = camera.getParameters();

            if (!params.isZoomSupported())
                return;

            curZoomValue += delta;

            if (curZoomValue < 0) {

                curZoomValue = 0;
            }
            else if (curZoomValue > params.getMaxZoom()) {

                curZoomValue = params.getMaxZoom();
            }

            if (!params.isSmoothZoomSupported()) {

                params.setZoom(curZoomValue);
                try {
                    camera.setParameters(params);
                } catch (Exception e) {
                    Log.wtf(TAG, "addZoomIn: ", e);
                }
            }
            else {

                camera.startSmoothZoom(curZoomValue);
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void pointFocus(int x, int y) {
        try {
            camera.cancelAutoFocus();
            Camera.Parameters parameters = camera.getParameters();

            showPoint(parameters, x, y);

            camera.setParameters(parameters);
        } catch (Exception e) {
            Log.wtf(TAG, "pointFocus: ", e);
        }
        autoFocus();
    }

    private void showPoint(Camera.Parameters parameters, int x, int y) {

        if (parameters.getMaxNumMeteringAreas() > 0) {

            List<Camera.Area> areas = new ArrayList<>();

            int rectY = -x * 2000 / Methods.getWidthScreen() + 1000;
            int rectX = y * 2000 / Methods.getHeightScreen()- 1000;

            int left = rectX < -900 ? -1000 : rectX - 100;
            int top = rectY < -900 ? -1000 : rectY - 100;
            int right = rectX > 900 ? 1000 : rectX + 100;
            int bottom = rectY > 900 ? 1000 : rectY + 100;

            Rect area1 = new Rect(left, top, right, bottom);
            areas.add(new Camera.Area(area1, 800));

            parameters.setMeteringAreas(areas);
        }

        parameters.setFocusMode(getFocusMode(parameters));
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @AfterPermissionGranted(RC_WRITESD_PERMISSIONS_REQUIRED)
    private void methodRequirePermissions() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };
        if (EasyPermissions.hasPermissions(this, perms)) {
            initActivity();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "Required permissions",
                    RC_WRITESD_PERMISSIONS_REQUIRED, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        initActivity();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        setResult(RESULT_ERROR);
        finish();
    }
}
