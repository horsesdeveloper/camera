package app.horses.camera.util;

import android.support.v4.content.ContextCompat;

import app.horses.camera.CameraManager;
import app.horses.camera.R;

/**
 * @author Brian Salvattore
 */
public class ColorUtils {

    private static final int DEFAULT_COLOR = R.color.defaultColor;

    public static int getPrimaryColor() {
       return ContextCompat.getColor(CameraManager.getApplication(), CameraManager.getColorPrimary() == 0 ? DEFAULT_COLOR : CameraManager.getColorPrimary());
    }
}
