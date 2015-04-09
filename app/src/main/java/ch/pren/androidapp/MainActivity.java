package ch.pren.androidapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import ch.pren.bluetooth.BluetoothConnection;
import ch.pren.camera.PhotoHandler;
import ch.pren.camera.CameraPreview;
import ch.pren.detector.Detector;
import ch.pren.multimedia.SoundHandler;
import ch.pren.usbconnector.UsbService;

/**
 * Created by Thomas on 20.03.2015.
 */
public class MainActivity extends Activity {

    public static final String DEBUG_TAG = "PREN_T32: ";
    private Camera camera;
    private CameraPreview mPreview;
    private PhotoHandler photoHandler;
    private Context context;
    private UsbService usbService;
    private MyHandler mHandler;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Context NICHT vor onCreate() beziehen! (stadessen wie hier mittels Methode nach OnCreate)
        context = getAppContext();

        mHandler = new MyHandler();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it


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
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    /**
     * Button_onCLick für erstellen eines Photos
     *
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
            photoHandler = new PhotoHandler(context);
            photoHandler.onPictureTaken(data, camera);
            detectBasket(data);
            Log.d(DEBUG_TAG, "onPictureTaken - jpeg");
        }
    };


    private void detectBasket(byte[] rawImage) {
        Detector detector = new Detector(rawImage);
        byte calculatedAngle = detector.start();
        sendAngleToBoard(calculatedAngle);
        saveEditedImageInDir(detector.getEditedImage());

    }

    private void sendAngleToBoard(final byte angle) {
        byte[] sendArray = new byte[1];
        sendArray[0] = angle;

        if (usbService != null) { // if UsbService was correctly bounded, send data
            try {
                usbService.write(sendArray);
                Toast.makeText(context, "Sent data: " + angle, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void saveEditedImageInDir(final Bitmap editedImage) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        editedImage.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        photoHandler.savePictureToDir(byteArray);
    }

    private void onReceiveFromBoard(final String receivedData) {
        SoundHandler soundHandler = new SoundHandler(this);
        if (receivedData.equals("f")) {
            soundHandler.play();
            // TODO: Zeit stoppen von photoclick bis hier
            Toast.makeText(context, "GAME FINISHED", Toast.LENGTH_SHORT).show();
        }
    }


    public void onClickBluetooth(View view) {

        Intent intent = new Intent(this, BluetoothConnection.class);
        startActivity(intent);
    }


    //   ----------------------------- Innere Klassen + Helper Methoden ----------------------------------------------

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
	 */
    private class MyHandler extends Handler {
        public MyHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    onReceiveFromBoard(data);
                    break;
            }
        }
    }

    /*
	 * Notifications from UsbService will be received here.
	 */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(UsbService.ACTION_USB_PERMISSION_GRANTED)) // USB PERMISSION GRANTED
            {
                Toast.makeText(arg0, "USB Ready", Toast.LENGTH_SHORT).show();
            } else if (arg1.getAction().equals(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED)) // USB PERMISSION NOT GRANTED
            {
                Toast.makeText(arg0, "USB Permission not granted", Toast.LENGTH_SHORT).show();
            } else if (arg1.getAction().equals(UsbService.ACTION_NO_USB)) // NO USB CONNECTED
            {
                Toast.makeText(arg0, "No USB connected", Toast.LENGTH_SHORT).show();
            } else if (arg1.getAction().equals(UsbService.ACTION_USB_DISCONNECTED)) // USB DISCONNECTED
            {
                Toast.makeText(arg0, "USB disconnected", Toast.LENGTH_SHORT).show();
            } else if (arg1.getAction().equals(UsbService.ACTION_USB_NOT_SUPPORTED)) // USB NOT SUPPORTED
            {
                Toast.makeText(arg0, "USB device not supported", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    private Context getAppContext() {
        return this.getApplicationContext();
    }


}
