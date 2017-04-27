package app.horses.camera.view;

/**
 * @author Brian Salvattore
 */
public interface CallbackView {
    void onSuccessCamera(String path);
    void onFailureCamera(Throwable throwable);
}
