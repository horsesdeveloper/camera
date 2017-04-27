package app.horses.camera.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import app.horses.camera.R;

/**
 * @author Brian Salvattore
 */
@Deprecated
public class PreviewActivity extends AppCompatActivity {

    private static final String TAG = PreviewActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

    }
}
