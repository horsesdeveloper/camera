package app.horses.camera.view;

/**
 * @author Brian Salvattore
 */
public interface CallbackView {
    void successCamera(String path);
    void errorCamera();
    void cancelCamera();
}
