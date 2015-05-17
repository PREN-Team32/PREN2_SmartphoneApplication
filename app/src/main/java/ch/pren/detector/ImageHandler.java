package ch.pren.detector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import static ch.pren.androidapp.MainActivity.DEBUG_TAG;


/**
 * Created Nikk
 */
public class ImageHandler {
    protected static int INITIAL_IMAGE_WIDTH = (int)(1280); //888
    protected static int INITIAL_IMAGE_HEIGHT = (int)(960); //500
    protected static int BORDER_LEFT = 140;
    protected static int BORDER_TOP = 250;
    protected static int WINDOW_WIDTH = 960;
    protected static int WINDOW_HEIGHT = 100;

    public static void setObservedWidth(int WIDTH_TO_OBSERVE) {
        ImageHandler.BORDER_LEFT = WIDTH_TO_OBSERVE;
    }

    public static void setObservedHeight(int HEIGHT_TO_OBSERVE) {
        ImageHandler.BORDER_TOP = HEIGHT_TO_OBSERVE;
    }

    public static Bitmap loadImage(byte[] bytes) {
        try {
            Bitmap tmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Log.i("Tmp Bitmap", "MiauzGenau: Width:" + tmp.getWidth() + "  Height:" + tmp.getHeight());
            Bitmap image = null;
            Bitmap finishedImage = null;

            //Resize the picture to 888x500 px (= INITIAL_IMAGE_WIDHT & _HEIGHT)
            //image = Bitmap.createBitmap(tmp, 0, 0, INITIAL_IMAGE_WIDTH, INITIAL_IMAGE_HEIGHT);


            //Cut out the black borders (background)
            finishedImage = Bitmap.createBitmap(tmp, (BORDER_LEFT) , (BORDER_TOP) , WINDOW_WIDTH, WINDOW_HEIGHT);

            Log.d(DEBUG_TAG, "ImageHandler: Image loaded (with byte[])");
            //ToDo: Just testing
            return finishedImage;
            //return image;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return null;
        }


    }
}