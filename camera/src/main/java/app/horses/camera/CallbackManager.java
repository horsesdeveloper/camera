package app.horses.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import app.horses.camera.view.CallbackView;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static app.horses.camera.util.Constants.REQUEST_GALLERY;
import static app.horses.camera.util.Constants.REQUEST_TAKE;
import static app.horses.camera.util.Constants.RESULT_ERROR;

/**
 * @author Brian Salvattore
 */
public class CallbackManager  {

    private static final String TAG = CallbackManager.class.getSimpleName();

    private CallbackView callback;

    public void setCallback(CallbackView callback) {
        this.callback = callback;
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "]");

        if (requestCode == REQUEST_TAKE) {

            switch (resultCode) {

                case RESULT_OK:
                    String path = data.getStringExtra("uri");
                    callback.successCamera(path);
                    break;
                case RESULT_CANCELED:
                    callback.cancelCamera();
                    break;
                case RESULT_ERROR:
                    callback.errorCamera();
                    break;
            }

        } else if (requestCode == REQUEST_GALLERY){
            switch (resultCode) {

                case RESULT_OK:
                    Uri selectedImage = data.getData();
                    String path = getRealPathFromURI(activity,selectedImage);
                    callback.successCamera(path);
                    break;
                case RESULT_CANCELED:
                    callback.cancelCamera();
                    break;
                case RESULT_ERROR:
                    callback.errorCamera();
                    break;
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
