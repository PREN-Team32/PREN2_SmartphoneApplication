package ch.pren.usbconnector;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


public class UsbService extends Service
{
	public static final String ACTION_USB_READY = "USB_READY";
	private static final String ACTION_USB_PERMISSION = "USB_PERMISSION";
	public static final String ACTION_USB_ATTACHED = "USB_DEVICE_ATTACHED";
	public static final String ACTION_USB_DETACHED = "USB_DEVICE_DETACHED";
	public static final String ACTION_USB_NOT_SUPPORTED = "USB_NOT_SUPPORTED";
	public static final String ACTION_NO_USB = "NO_USB";
	public static final String ACTION_USB_PERMISSION_GRANTED = "USB_PERMISSION_GRANTED";
	public static final String ACTION_USB_PERMISSION_NOT_GRANTED = "USB_PERMISSION_NOT_GRANTED";
	public static final String ACTION_USB_DISCONNECTED = "USB_DISCONNECTED";
	public static final String ACTION_CDC_DRIVER_NOT_WORKING ="ACTION_CDC_DRIVER_NOT_WORKING";
	public static final String ACTION_USB_DEVICE_NOT_WORKING = "ACTION_USB_DEVICE_NOT_WORKING";
	
	private static final int BAUD_RATE = 38400; // BaudRate. Change this value if you need
	public static final int MESSAGE_FROM_SERIAL_PORT = 0;
    private final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    private final Charset ASCII_CHARSET = Charset.forName("US-ASCII");

	
	public static boolean SERVICE_CONNECTED = false;
	
	private IBinder binder = new UsbBinder();

    private Object lock;
	private Context context;
	private Handler mHandler;
	private UsbManager usbManager;
	private UsbDevice device;
	private UsbDeviceConnection connection;
	private UsbSerialDevice serialPort;
    private boolean hasBeenWrote = false;
	
	/*
	 * onCreate will be executed when service is started. It configures an IntentFilter to listen for
	 * incoming Intents (USB ATTACHED, USB DETACHED...) and it tries to open a serial port.
	 */
	@Override
	public void onCreate()
	{
		this.context = this;
		UsbService.SERVICE_CONNECTED = true;
		setFilter();
		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		findSerialPortDevice();
	}
	
	/* MUST READ about services
	 * http://developer.android.com/guide/components/services.html
	 * http://developer.android.com/guide/components/bound-services.html
	 */
	@Override
	public IBinder onBind(Intent intent) 
	{
		return binder;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return Service.START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		UsbService.SERVICE_CONNECTED = false;
	}
	
	/*
	 * This function will be called from MainActivity to write data through Serial Port
	 */
	public void write(byte[] data)
	{

		if(serialPort != null)
			serialPort.write(data);
                Log.d("thomSerial", "write: " + data.toString());


	}
	
	public void setHandler(Handler mHandler)
	{
		this.mHandler = mHandler;
	}
	
	private void findSerialPortDevice()
	{
		// This snippet will try to open the first encountered usb device connected, excluding usb root hubs
		HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
		if(!usbDevices.isEmpty())
		{
			boolean keep = true;
			for(Map.Entry<String, UsbDevice> entry : usbDevices.entrySet())
			{
				device = entry.getValue();
				int deviceVID = device.getVendorId();
                Log.d("thomSerial","" + deviceVID);
				int devicePID = device.getProductId();
                Log.d("thomSerial", "" + devicePID);
				if(deviceVID != 0x1d6b || (devicePID != 0x0001 || devicePID != 0x0002 || devicePID != 0x0003))
				{
					// There is a device connected to our Android device. Try to open it as a Serial Port.

                    Log.d("thomSerial", "before request permisson");
					requestUserPermission();
					keep = false;
				}else
				{
					connection = null;
					device = null;
				}

				if(!keep)
					break;
			}
			if(!keep)
			{
				// There is no USB devices connected (but usb host were listed). Send an intent to MainActivity.
				Intent intent = new Intent(ACTION_NO_USB);
				sendBroadcast(intent);
			}
		}else
		{
			// There is no USB devices connected. Send an intent to MainActivity
			Intent intent = new Intent(ACTION_NO_USB);
			sendBroadcast(intent);
		}
	}

	private void setFilter()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_USB_PERMISSION);
		filter.addAction(ACTION_USB_DETACHED);
		filter.addAction(ACTION_USB_ATTACHED);
		registerReceiver(usbReceiver , filter);
	}
	
	/*
	 * Request user permission. The response will be received in the BroadcastReceiver
	 */
	private void requestUserPermission()
	{
		PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION),0);
		usbManager.requestPermission(device, mPendingIntent);
        Log.d("thomSerial", "permission requested");
	}


    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            Log.d("thomSerial", "onReceivedData() ");

            try {
    /*

                // JUNGE => Was schikts zu dem Board? Den sendeneden String auf ASCII Abändern!!!!!!!!!!!!!!!
                // ALSO: kann auch ASCII anzeigen. => Problem irgendwo beim hex zu ascii
                //http://docs.oracle.com/javase/tutorial/i18n/text/string.html
                // Kill the fucking APP after onPause() or so!!! misbehaves every time...


                // byte in ascii => UTF-8
                Charset charset = Charset.forName("UTF-8"); // oder US-ASCII
                String data = new String(arg0, charset);


               // ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(cbuf);

                 String data1 = new String(arg0, "US-ASCII");

                if (mHandler != null)
                    mHandler.obtainMessage(MESSAGE_FROM_SERIAL_PORT, data1).sendToTarget();

                Log.d("thomSerial", "This msg should be sent to Handler _ data1: " + data1);


               // String data = new String(arg0 , UTF8_CHARSET );
                //byte[] r = data1.getBytes(UTF8_CHARSET);
                //String data = new String(r, UTF8_CHARSET);
*/
                //String data2 = HexData.hexToString(arg0);
                //String data3 = convertHexToString(data2);


                String data3 = new String(arg0, "UTF-8");






                if (mHandler != null)
                    mHandler.obtainMessage(MESSAGE_FROM_SERIAL_PORT, data3).sendToTarget();

                Log.d("thomSerial", "This msg should be sent to Handler _ data3: " + data3);






            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };




	




	public class UsbBinder extends Binder
	{
		public UsbService getService()
		{
			return UsbService.this;
		}
	}
	
	/*
	 * Different notifications from OS will be received here (USB attached, detached, permission responses...)
	 * About BroadcastReceiver: http://developer.android.com/reference/android/content/BroadcastReceiver.html
	 */
	private final BroadcastReceiver usbReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context arg0, Intent arg1) 
		{
			if(arg1.getAction().equals(ACTION_USB_PERMISSION))
			{
				boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
				if(granted) // User accepted our USB connection. Try to open the device as a serial port
				{
					Intent intent = new Intent(ACTION_USB_PERMISSION_GRANTED);
					arg0.sendBroadcast(intent);
					connection = usbManager.openDevice(device);
                    Log.d("thomSerial", "usbManager open Device()"  + device.getDeviceName() + device.toString());
                    new ConnectionThread().run();

				}else // User not accepted our USB connection. Send an Intent to the Main Activity
				{
					Intent intent = new Intent(ACTION_USB_PERMISSION_NOT_GRANTED);
					arg0.sendBroadcast(intent);
				}
			}else if(arg1.getAction().equals(ACTION_USB_ATTACHED))
			{
				findSerialPortDevice(); // A USB device has been attached. Try to open it as a Serial port
			}else if(arg1.getAction().equals(ACTION_USB_DETACHED))
			{
				// Usb device was disconnected. send an intent to the Main Activity
				Intent intent = new Intent(ACTION_USB_DISCONNECTED);
				arg0.sendBroadcast(intent);
			}
		}
	};
	
	/*
	 * A simple thread to open a serial port.
	 * Although it should be a fast operation. moving usb operations away from UI thread is a good thing.
	 */
	private class ConnectionThread extends Thread
	{



		@Override
		public void run()
		{
			serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);


			if(serialPort != null)
			{
				if(serialPort.open())
				{
                    serialPort.debug(true);
                    serialPort.setBaudRate(BAUD_RATE);
					serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
					serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
					serialPort.setParity(UsbSerialInterface.PARITY_NONE);
					serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

                    serialPort.read(mCallback);
					
					// Everything went as expected. Send an intent to MainActivity
					Intent intent = new Intent(ACTION_USB_READY);
					context.sendBroadcast(intent);




                }else
				{
					// Serial port could not be opened, maybe an I/O error or if CDC driver was chosen, it does not really fit
					// Send an Intent to Main Activity
					if(serialPort instanceof CDCSerialDevice)
					{
						Intent intent = new Intent(ACTION_CDC_DRIVER_NOT_WORKING);
                        Log.d("thomSerial", "ACTION_CDC_DRIVER_NOT_WORKING");
						context.sendBroadcast(intent);
					}else
					{
						Intent intent = new Intent(ACTION_USB_DEVICE_NOT_WORKING);
                        Log.d("thomSerial", "ACTION_USB_DRIVER_NOT_WORKING");
						context.sendBroadcast(intent);
					}
				}
			}else
			{
				// No driver for given device, even generic CDC driver could not be loaded
				Intent intent = new Intent(ACTION_USB_NOT_SUPPORTED);
                Log.d("thomSerial", "ACTION_USB_NOT SUPPORTED");
				context.sendBroadcast(intent);
			}

		}
	}





}
