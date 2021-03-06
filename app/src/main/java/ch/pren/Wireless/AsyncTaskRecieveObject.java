package ch.pren.Wireless;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import ch.pren.androidapp.MainActivity;
import ch.pren.model.ConfigurationItem;
import ch.pren.model.ValueItem;

/**
 * Created by livio on 29.04.2015.
 */
public class AsyncTaskRecieveObject extends AsyncTask<Void, Void, Void> {
    int dstPort;
    String response = "";

    public AsyncTaskRecieveObject(int port) {
        dstPort = port;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        ServerSocket serversocket = null;
        try {

            serversocket = new ServerSocket(11111);
            Socket pipe = serversocket.accept();

            ObjectInputStream objectInputStreamn = new ObjectInputStream(pipe.getInputStream());
            Log.d("Recieve ConfItem", "InputStream openend");

            ConfigurationItem configurationItem = ConfigurationItem.getInstance();
            //Object o = objectInputStreamn.readObject();
            configurationItem.overrideConfig((ConfigurationItem) objectInputStreamn.readObject());



            ConfigurationItem cf = ConfigurationItem.getInstance();

            Log.d("Recieve ConfItem", "ConfigurationItem succesfully recieved");
            Log.d("Lumiance", "||Lumiance: " + cf.luminanceThreshold);
            Log.d("PixeltoCM", "" + cf.pixelToCm);
            Log.d("HeighttoObserve", " " + cf.heightToObserve);
            Log.d("WidthtoObserve", " " + cf.widthToObserve);
            Log.d("Start Singal", "Start Singal: " + cf.startSignal);
            pipe.close();
        } catch (Exception ex) {
            Log.d("RecieveConfItem", "Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            if (serversocket != null) {
                try {
                    serversocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        super.onPostExecute(result);
    }

}

