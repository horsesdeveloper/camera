package app.horses.camera.util;

import app.horses.camera.CameraManager;
import app.horses.camera.R;

/**
 * @author Brian Salvattore
 */
public class ColorUtils {

    private static final int DEFAULT_COLOR = R.color.defaultColor;

    @SuppressWarnings({"ResourceType", "deprecation"})
    public static int getPrimaryColor() {

        return CameraManager
                .getApplication()
                .getResources()
                .getColor(CameraManager.getColorPrimary() == 0 ? DEFAULT_COLOR : CameraManager.getColorPrimary());
    }
}
