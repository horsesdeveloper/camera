package app.horses.camera.view;

/**
 * @author Brian Salvattore
 */
public interface CallbackView {
    void successCamera(String path); // path can return scanned QR Code
    void errorCamera();
    void cancelCamera();
}
