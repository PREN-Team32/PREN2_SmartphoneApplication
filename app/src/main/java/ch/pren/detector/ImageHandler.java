package ch.pren.detector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import static ch.pren.androidapp.MainActivity.DEBUG_TAG;


/**
 * Created Nikk
 */
public class ImageHandler {
    protected static int INITIAL_IMAGE_WIDTH = 888;
    protected static int INITIAL_IMAGE_HEIGHT = 500;
    protected static int WIDTH_TO_OBSERVE = 488;
    protected static int HEIGHT_TO_OBSERVE = 500;

    public static void setObservedWidth(int WIDTH_TO_OBSERVE) {
        ImageHandler.WIDTH_TO_OBSERVE = WIDTH_TO_OBSERVE;
    }

    public static void setObservedHeight(int HEIGHT_TO_OBSERVE) {
        ImageHandler.HEIGHT_TO_OBSERVE = HEIGHT_TO_OBSERVE;
    }

    public static Bitmap loadImage(final String filepath) {

        Bitmap image;
        Bitmap tmp;
        Bitmap finishedImage = null;

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        //image = Bitmap.createBitmap(INITIAL_IMAGE_WIDTH, INITIAL_IMAGE_WIDTH, conf);

        try {
            tmp = BitmapFactory.decodeFile(filepath);
            //Resize the picture to 888x500 px (= INITIAL_IMAGE_WIDHT & _HEIGHT)
            image = Bitmap.createBitmap(tmp, 0, 0, INITIAL_IMAGE_WIDTH, INITIAL_IMAGE_HEIGHT);

            //Cut out the black borders (background)
            finishedImage = Bitmap.createBitmap(image, (INITIAL_IMAGE_WIDTH - WIDTH_TO_OBSERVE) / 2, (INITIAL_IMAGE_HEIGHT - HEIGHT_TO_OBSERVE) / 2, WIDTH_TO_OBSERVE, HEIGHT_TO_OBSERVE);

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        Log.d(DEBUG_TAG, "ImageHandler: Image loaded (from path)");
        return finishedImage;
    }



    public static Bitmap loadImage(byte[] bytes) {

        Bitmap tmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap image;
        Bitmap finishedImage = null;

        try {
            //Resize the picture to 888x500 px (= INITIAL_IMAGE_WIDHT & _HEIGHT)
            image = Bitmap.createBitmap(tmp, 0, 0, INITIAL_IMAGE_WIDTH, INITIAL_IMAGE_HEIGHT);

            //Cut out the black borders (background)
            finishedImage = Bitmap.createBitmap(image,(INITIAL_IMAGE_WIDTH-WIDTH_TO_OBSERVE)/2,(INITIAL_IMAGE_HEIGHT-HEIGHT_TO_OBSERVE)/2 , WIDTH_TO_OBSERVE, HEIGHT_TO_OBSERVE);

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        Log.d(DEBUG_TAG, "ImageHandler: Image loaded (with byte[])");
        return finishedImage;
    }
}