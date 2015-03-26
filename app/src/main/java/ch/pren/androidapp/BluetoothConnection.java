package ch.pren.androidapp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
//Bluetooth imports

import android.bluetooth.BluetoothAdapter;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import ch.pren.model.ValueItem;


public class BluetoothConnection extends Activity {

    private static final String TAG = "BluetoothActivity" ;
    private static final String ComputerName ="LIVIO-LAPTOP"; //Per Optione Änderbar mache, ned Hardcoded esch behinderet


    private BluetoothAdapter BA = null;


    //Für Tests
    private TextView mTitle;
    private TextView sendText;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothCommandService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter

    // Member object for Bluetooth Command Service
    private BluetoothSocket mCommandService = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.bluetoothconn);


            //Bluetooth Adapter initialiesieren & Auf Handy einschalten
            BA = BluetoothAdapter.getDefaultAdapter();

            //Checks if the device has a Bluetooth Adapter
            if (BA == null) {
                Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            mTitle = (TextView) this.findViewById(R.id.txtView);
            //TextView txtView = (TextView) this.findViewById(R.id.txtView);
        }
        catch(Exception e){
            Log.e(e.getMessage(), "Fehler onCreate aufgetreten");

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!BA.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        // otherwise set up the command service
        else {
            if (mCommandService == null)
                setupCommand();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mCommandService != null) {
            if (mCommandService.getState() == BluetoothSocket.STATE_NONE) {
                mCommandService.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCommandService != null) {
            mCommandService.stop();
        }
    }




    public void onSearchDevices(View view) {
        setDiscoverable();
    }

    public void onDiscoverDevices(View view) {

        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

    }

    private void setupCommand() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mCommandService = new BluetoothSocket(this, mHandler);
    }

    //enables Smartphone to be seen
    private void setDiscoverable() {
        if (BA.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }


    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                        case MESSAGE_STATE_CHANGE:
                            switch (msg.arg1) {
                                case BluetoothSocket.STATE_CONNECTED:
                                    mTitle.setText(R.string.title_connected_to);
                                    mTitle.append(mConnectedDeviceName);
                                    break;
                                case BluetoothSocket.STATE_CONNECTING:
                                    mTitle.setText(R.string.title_connecting);
                                    break;
                                case BluetoothSocket.STATE_LISTEN:
                                case BluetoothSocket.STATE_NONE:
                            mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = BA.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mCommandService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupCommand();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    public void onSendMessage(View view) {

        ValueItem val = new ValueItem();
        val.calculatedAngle = 12;
        val.foundShape = true;
        val.mainArea = 7;
        val.totalTimeUsed = 42;


        sendText = (TextView) findViewById(R.id.txtViewSend);



        //Test Object to Byte
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(val);
            byte[] yourBytes = bos.toByteArray();

            mCommandService.write(yourBytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }


}


