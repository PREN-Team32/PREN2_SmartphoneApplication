package ch.pren.Wireless;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import ch.pren.model.ConfigurationItem;
import ch.pren.model.ValueItem;

/**
 * Created by livio on 29.04.2015.
 */
public class AsyncTaskSendObject extends AsyncTask<Void, Void, Void> {
    String dstAddress;
    int dstPort;
    String response = "";

    public AsyncTaskSendObject(String addr, int port) {
        dstAddress = addr;
        dstPort = port;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        Socket socket = null;
        try {

            socket = new Socket(dstAddress, dstPort);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            Log.d("SendSocket", "Streams openend");
            ValueItem valueItem = ValueItem.getInstance();

            objectOutputStream.writeObject(valueItem);
            Log.d("SendSocket", "Value Item gesendet");
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
