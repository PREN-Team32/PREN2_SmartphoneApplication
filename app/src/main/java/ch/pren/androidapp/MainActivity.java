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
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import ch.pren.Wireless.AsyncTaskRecieveObject;
import ch.pren.Wireless.AsyncTaskSendObject;
import ch.pren.camera.PhotoHandler;
import ch.pren.camera.CameraPreview;
import ch.pren.detector.AngleCalculator;
import ch.pren.detector.Detector;
import ch.pren.detector.ImageHandler;
import ch.pren.model.ConfigurationItem;
import ch.pren.model.ValueItem;
import ch.pren.usbconnector.UsbService;

/**
 * Created by Thomas on 20.03.2015.
 */
public class MainActivity extends Activity {

    public static final String DEBUG_TAG = "PREN_T32: ";
    private Camera camera;
    private CameraPreview mPreview;
    private PhotoHandler photoHandler;
    private UsbService usbService;
    private MyHandler mHandler;
    private ValueItem valueItem;



    private long zeitBegin;
    private long zeitEndeSendData;
    private long zeitReceiveInputBoard;
    private long zeitGesamtSendData;
    private long zeitGesamt;
    private SequenceHandler sequenceHandler;
    private boolean sequenceStarted;
    public static Activity activity = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        valueItem = ValueItem.getInstance();


        mHandler = new MyHandler(this);
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it

        try {
            camera = Camera.open();

            // ----------- Width Height lesen und einstellen ------------

            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPictureSizes();
            for(Camera.Size size : sizes){
                Log.d(DEBUG_TAG, "Height: " + size.height + "  Width: " + size.width);
            }
            params.setPictureSize(1280, 960);
            //params.setPictureSize(2592,1944);
            camera.setParameters(params);

            // ---------- Ende ------------------
            
            camera.setDisplayOrientation(90);
            mPreview = new CameraPreview(this, camera);

            // set Preview für Camera
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
            zeitBegin = System.currentTimeMillis();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        activity = this;
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


    public void takePic() {
        try {
            // if (ConfigFileReaded == true) {
                camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                camera.startPreview();
            // }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
            photoHandler.onPictureTaken(data, camera);
            valueItem.originalImage = Base64.encodeToString(data, Base64.DEFAULT);
            detectBasket(data);
            Log.d(DEBUG_TAG, "onPictureTaken - jpeg");
        }
    };


    private void detectBasket(byte[] rawImage) {
        try {
            ConfigurationItem configurationItem = ConfigurationItem.getInstance();
            Detector detector = new Detector(rawImage);
            detector.setLuminanceThreshold(configurationItem.luminanceThreshold);
            detector.setVisitedPixels(configurationItem.visitedPixels);
            ImageHandler.setObservedHeight(configurationItem.heightToObserve);
            ImageHandler.setObservedWidth(configurationItem.widthToObserve);
            detector.setPixeltocm(configurationItem.pixelToCm);

            double calculatedAngle = detector.start();
            Log.d("Method DetectBasket" , "Bevor configItem.start singal check");
            if (configurationItem.startSignal) {
                Log.d("Method DetectBasket" , "In configItem.start singal check");
                Toast.makeText(getApplicationContext(), "Start Signal erhalten", Toast.LENGTH_SHORT).show();
                sendCommandsToBoard(calculatedAngle, (int) detector.getGebrauchteZeit());
                Log.d("Method DetectBasket" , "Nach configItem.start singal check");

            }

            ValueItem valueItem = ValueItem.getInstance();
            if(configurationItem.startSignal == false) {
                Log.d("ValueItem", "ValueItem.Finsihed: " + valueItem.finished);
                valueItem.calculatedAngle = calculatedAngle;
                valueItem.totalTimeUsed = (int) detector.getGebrauchteZeit();
                valueItem.mainArea = detector.getMainAreaX();
                valueItem.foundShape = detector.getIsBucketShape();
                valueItem.finished = false;
                saveEditedImageInDir(detector.getEditedImage());

            }
            SendValueItem();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendCommandsToBoard(final double angle, int usedtime) {

        final int rpmBLDC_zero = 3300;
        final int pwmDC = 32;

        String angleAsSteps = AngleCalculator.getStepsAsString(angle);
        int toReadjustRPM = calculateRpmFittingAngle(angle);

        final String[] dataStringsForAngle = { "l6480 move " + angleAsSteps + "\n\r" };
        final String[] dataStringsForReadjustingRPM = {"BLDC use 1\n\r", "BLDC setrpm " + toReadjustRPM + "\n\r"};
        final String[] dataStringsForSupplier = {"DC setpwm " + pwmDC + "\n\r" };
        final String[] dataStringsForShutdownDC = { "DC off\n\r" };
        final String[] dataStringsForShutdown = { "BLDC use 0\n\r"};
        final String[] dataStringsForShutdown1 = { "BLDC off\n\r"};
        final String[] dataStringsForShutdown2 = { "BLDC use 1\n\r"};
        final String[] dataStringsForShutdown3 = { "BLDC off\n\r"};
        /*
        Toast.makeText(context, "Sent data to Board: " + angleAsSteps, Toast.LENGTH_SHORT).show();
        zeitEndeSendData = System.currentTimeMillis();
        zeitGesamtSendData = zeitEndeSendData - zeitBegin;
        Log.d(DEBUG_TAG, "Gebrauchte Zeit von takePicture bis senden des Winkels:: " + zeitGesamtSendData);
        */

        /*Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run(){*/
                try {

                    sequenceHandler = new SequenceHandler(dataStringsForAngle);

                    Thread.sleep(200);
                    sequenceHandler = new SequenceHandler(dataStringsForReadjustingRPM);

                    Thread.sleep(1000);
                    sequenceHandler = new SequenceHandler(dataStringsForSupplier);

                    Thread.sleep(1500);
                    AsyncTaskSendObject asyncTaskSendObject = new AsyncTaskSendObject(11111);
                    ValueItem valueItem = ValueItem.getInstance();
                    valueItem.foundShape = false;
                    valueItem.totalTimeUsed = usedtime;
                    valueItem.calculatedAngle = 0;
                    valueItem.editedImage = "a";
                    valueItem.originalImage = "a";
                    valueItem.mainArea = 0;
                    valueItem.objectBorder = 0;
                    valueItem.finished = true;
                    Log.d("Before Finish senden", "Finsih wurde noch nicht gesendet");
                    valueItem.overrideValues(valueItem);
                    asyncTaskSendObject.execute().get();
                    Log.d("Finish Senden", "Finish wurde gesendet");

                    Thread.sleep(2500);

                    sequenceHandler = new SequenceHandler(dataStringsForShutdown);
                    Thread.sleep(100);
                    sequenceHandler = new SequenceHandler(dataStringsForShutdown1);
                    Thread.sleep(100);
                    sequenceHandler = new SequenceHandler(dataStringsForShutdown2);
                    Thread.sleep(100);
                    sequenceHandler = new SequenceHandler(dataStringsForShutdown3);
                    Thread.sleep(100);

                    sequenceHandler = new SequenceHandler(dataStringsForShutdownDC);

                } catch (InterruptedException | ExecutionException e) {
                    Log.d(DEBUG_TAG, "Interrupted Thread in sendCommandsToBoard");
                }
            //}
       // });
        //thread1.start();
    }

    private int calculateRpmFittingAngle(final double angle){
        int lowestRPM = 2600;
        int highestRPM = 2700;
        double maxAngle = 19.45;
        double absAngle = Math.abs(angle);
        return (int) (lowestRPM +(((highestRPM - lowestRPM) / maxAngle) * absAngle));
    }


    private void saveEditedImageInDir(final Bitmap editedImage) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        editedImage.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        valueItem.editedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
        photoHandler.savePictureToDir(byteArray);
    }

    // ----------------------------- onClick Methoden --------------------------

    public void onClickWireless(View view) {
        recieveConfItem();
    }

    public void onClickStartMotor(View view) {
        int startRPM_BLDC_zero = 3300;
        int startRPM_BLDC_one = 2600;
        String[] dataStrings = { "BLDC use 0\n\r", "BLDC setrpm " + startRPM_BLDC_zero + "\n\r",
                "BLDC on\n\r", "BLDC use 1\n\r", "BLDC setrpm " + startRPM_BLDC_one + "\n\r", "BLDC on\n\r"  };
        sequenceHandler = new SequenceHandler(dataStrings);
    }

    //--------------------------------------  Wireless relevanten Methoden    ---------------------------------------------------------
    //<editor-fold desc="Wireless">

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);

        mPreview.mCamera.stopPreview();
        mPreview.getHolder().removeCallback(mPreview);
        camera.stopPreview();
        camera.setPreviewCallback(null);
        camera.release();
        camera = null;

    }

    //ToDo: Fill method for recieveing ConfItem
    private void recieveConfItem() {
        try {
            AsyncTaskRecieveObject asyncConnection = new AsyncTaskRecieveObject(11111);
            asyncConnection.execute().get();
            
            takePic();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    //ValueItem wird in ein ByteArray geparst und gesendet
    private void SendValueItem() {
        try {
            ConfigurationItem configurationItem = ConfigurationItem.getInstance();

            if(configurationItem.startSignal == false) {
                AsyncTaskSendObject asyncTaskSendObject = new AsyncTaskSendObject(11111);
                asyncTaskSendObject.execute().get();
            }
            recieveConfItem();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //</editor-fold>


    //   ----------------------------- USB Service + Helper Methoden ----------------------------------------------
    //<editor-fold desc="USB Service">
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



    /**
     * Sende ein Packet von Kommandos an das Freedom Board
     */
    private class SequenceHandler {
        private String[] dataStrings;
        private int counter = 0;

        public SequenceHandler(String[] dataStrings) {
            this.dataStrings = dataStrings;
            try {
                sequenceStarted = true;
                usbService.write(dataStrings[0].getBytes("US-ASCII"));
                counter++;
            }catch(Exception e){
                Toast.makeText(getApplication(), "Failure in Sending data-package to USB", Toast.LENGTH_SHORT).show();
            }
        }

        public void receiveUSBMessage(){
            try {
                if(counter <= dataStrings.length) {
                    Thread.sleep(80);
                    usbService.write(dataStrings[counter].getBytes("US-ASCII"));
                    counter++;
                }else{
                    sequenceStarted = counter <= dataStrings.length;
                }
            }catch(Exception e){
               Log.d(DEBUG_TAG,"Failure in Receiving/Sending data-package to USB");
            }
        }
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
	 */
    private class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity)
        {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg)
        {
            Log.d(DEBUG_TAG, "Handler received a msg");

           if(msg.what == UsbService.MESSAGE_FROM_SERIAL_PORT){
                    if(mActivity.get().sequenceStarted){
                        mActivity.get().sequenceHandler.receiveUSBMessage();
                        Log.d(DEBUG_TAG, "Message from Board: " + msg.getData().toString());
                    }else {
                        Log.d(DEBUG_TAG, "Handler: no suitable use for Input from Board");
                    }
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

    //</editor-fold>

}