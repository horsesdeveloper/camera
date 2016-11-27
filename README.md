# Camera Material

Download
--------

```groovy
dependencies {
  compile 'com.horsesdeveloper:camera:0.1.3'
}
```
Configuration
--------------------


```java
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        /** Simple Usage (default values) */
        CameraManager.init(this);

        /** Complete usage */
        CameraManager.init(
                new CameraManager.Builder(this)
                    .setPrimaryColor(R.color.colorPrimary)
                    .build());

        ...
    }
}
```

```java
public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager = new CallbackManager();
    ...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        /** Open Camera*/
        CameraManager.openCamera(this);

        /** Set Callback*/
        callbackManager.setCallback(this);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void successCamera(String path) {
        Log.i(TAG, "successCamera: " + path);
    }

    @Override
    public void errorCamera() {
        Log.i(TAG, "errorCamera");
    }

    @Override
    public void cancelCamera() {
        Log.i(TAG, "cancelCamera");
    }
}
```
