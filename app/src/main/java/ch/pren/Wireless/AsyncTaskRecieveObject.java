package ch.pren.Wireless;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import ch.pren.androidapp.MainActivity;
import ch.pren.model.ConfigurationItem;
import ch.pren.model.ValueItem;

/**
 * Created by livio on 29.04.2015.
 */
public class AsyncTaskRecieveObject extends AsyncTask<Void, Void, Void> {
    String dstAddress;
    int dstPort;
    String response = "";

    public AsyncTaskRecieveObject(String addr, int port) {
        dstAddress = addr;
        dstPort = port;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        Socket socket = null;
        try {

            socket = new Socket(dstAddress, dstPort);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStreamn = new ObjectInputStream(socket.getInputStream());


            ConfigurationItem configurationItem = ConfigurationItem.getInstance();
            configurationItem = (ConfigurationItem) objectInputStreamn.readObject();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
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

