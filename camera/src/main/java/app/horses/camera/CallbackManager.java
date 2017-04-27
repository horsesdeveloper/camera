package app.horses.camera;

import android.content.Intent;
import android.util.Log;

import app.horses.camera.view.CallbackView;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static app.horses.camera.util.Constants.REQUEST_TAKE;
import static app.horses.camera.util.Constants.RESULT_ERROR;

/**
 * @author Brian Salvattore
 */
public class CallbackManager  {

    private static final String TAG = CallbackManager.class.getSimpleName();

    private static CallbackManager instance;
    private CallbackView callback;

    public CallbackManager() {
        instance = this;
    }

    public void setCallback(CallbackView callback) {
        this.callback = callback;
    }

    public static CallbackManager getInstance() {
        return instance;
    }

    public static CallbackView getCallback() {
        return getInstance().callback;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "]");

        if (requestCode == REQUEST_TAKE) {

            switch (resultCode) {

                case RESULT_OK:
                    String path = data.getStringExtra("uri");
                    callback.onSuccessCamera(path);
                    break;
                /*case RESULT_CANCELED:
                    callback.cancelCamera();
                    break;*/
                case RESULT_ERROR:
                    callback.onFailureCamera(new Throwable("kgfiffgfit"));
                    break;
            }
        }
    }
}
