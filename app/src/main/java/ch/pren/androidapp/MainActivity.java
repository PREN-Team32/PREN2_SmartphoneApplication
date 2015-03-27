package ch.pren.androidapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import ch.pren.bluetooth.BluetoothConnection;
import ch.pren.camera.PhotoHandler;
import ch.pren.camera.CameraPreview;
import ch.pren.detector.Detector;

/**
 * Created by Thomas on 20.03.2015.
 */
public class MainActivity extends Activity {

    public static final String DEBUG_TAG = "PREN_T32: ";
    private Camera camera;
    private CameraPreview mPreview;
    private PhotoHandler photoHandler;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


       try {
            camera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        Camera.Parameters parameters = camera.getParameters();
        parameters.setRotation(90);
        parameters.setFocusMode(parameters.FOCUS_MODE_AUTO);
        */
        camera.setDisplayOrientation(90);
        mPreview = new CameraPreview(this, camera);

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);


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

    @Override
    protected void onPause() {
        super.onPause();

        if (camera != null) {
            mPreview.mCamera.stopPreview();
            mPreview.getHolder().removeCallback(mPreview);
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
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
            photoHandler = new PhotoHandler(getApplicationContext());
            photoHandler.onPictureTaken(data,camera);
            detectBasket(data);
            Log.d(DEBUG_TAG, "onPictureTaken - jpeg");
        }
    };


    public void detectBasket(byte[] rawImage){
        Detector detector = new Detector(rawImage);
        detector.start();

        Log.d(DEBUG_TAG, detector.getEditedImage().toString());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        detector.getEditedImage().compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        photoHandler.savePictureToDir(byteArray);

    }

    public void onClickBluetooth(View view) {

        Intent intent = new Intent(this, BluetoothConnection.class);
        startActivity(intent);
    }
}
