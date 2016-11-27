package app.horses.camera.util;

import android.graphics.Color;
import android.support.annotation.ColorInt;

/**
 * @author Brian Salvattore
 */
public class CameraUtil {

    private static final String TAG = CameraUtil.class.getSimpleName();

    @ColorInt
    public static int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        color = Color.HSVToColor(hsv);
        return color;
    }
}
