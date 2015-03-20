package ch.pren.androidapp;

import android.app.Activity;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.test.ActivityTestCase;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import ch.pren.camera.PhotoHandler;
import ch.pren.camera.CameraPreview;

/**
 * Created by Thomas on 20.03.2015.
 */
public class MainActivity extends Activity {

    protected static final String DEBUG_TAG = "PREN_T32: ";
    private Camera camera;
    private CameraPreview mPreview;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bluetooth Connection aufbauen


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_class, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Button_onCLick für erstellen eines Photos
     * @param view
     */
    public void onClickPhoto(View view) {

        camera.takePicture(shutterCallback, rawCallback, jpegCallback);
        Log.d(DEBUG_TAG, "take Picture");
    }

    /**
     * Picture Callback beim shutter
     */
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            Log.d(DEBUG_TAG, "onShutter down");
        }
    };

    /**
     * Picture Callback für raw-Daten
     */
    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(DEBUG_TAG, "onPictureTaken - raw");
        }
    };

    /**
     * Picture Callback für jpeg.
     */
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            PhotoHandler photoHandler = new PhotoHandler(getApplicationContext());
            photoHandler.onPictureTaken(data,camera);
            Log.d(DEBUG_TAG, "onPictureTaken - jpeg");
        }
    };
}
