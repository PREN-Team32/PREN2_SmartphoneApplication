package ch.pren.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Thomas on 12.03.2015.
 */
public class PhotoHandler implements Camera.PictureCallback {


    public static final String FILEPATH = "storage/emulated/0/";
    private final Context context;
    private String DEBUG_TAG = "camera";


    public PhotoHandler(Context context) {
        this.context = context;
    }

    /**
     * Wichtig!! Diese Methode speichert das raw-Image das direkt von der Camera kommt.
     * Das edited Image vom Detektor (SW-Bild) wird in der Methode unten (savePictureToDir)
     * gespeichert. Bitte Richtig verweden!
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        File pictureFileDir = getDir();


        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d(DEBUG_TAG, "Can't create directory to save image.");
            Toast.makeText(context, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
            return;

        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] bitmapData = stream.toByteArray();

        // ------ raw-Image ----------------------
        String photoFile = "picture.jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(bitmapData);
            fos.close();
            Toast.makeText(context, "New Image saved:  " + photoFile, Toast.LENGTH_SHORT).show();
        } catch (Exception error) {
            Log.d(DEBUG_TAG, "File" + filename + "not saved: " + error.getMessage());
            Toast.makeText(context, "Image could not be saved.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Wichtig!! Diese Methode speichert das edited-Image dasvom Detektor (SW-Bild) kommt.
     *
     * @param data
     */
    public void savePictureToDir(byte[] data) {

        File pictureFileDir = getDir();

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d(DEBUG_TAG, "Can't create directory to save image.");
            Toast.makeText(context, "Can't create directory to save image.", Toast.LENGTH_SHORT).show();
            return;

        }


        // -------------- edited Image ------------------------------
        String photoFile = "editedPicture.jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            Toast.makeText(context, "New Image saved:  " + photoFile, Toast.LENGTH_SHORT).show();
        } catch (Exception error) {
            Log.d(DEBUG_TAG, "File " + filename + " not saved: " + error.getMessage());
            Toast.makeText(context, "Image could not be saved.", Toast.LENGTH_SHORT).show();
        }
    }

    private File getDir() {

        File sdDir = new File(FILEPATH);
        return new File(sdDir, "PREN_T32");
    }
}
