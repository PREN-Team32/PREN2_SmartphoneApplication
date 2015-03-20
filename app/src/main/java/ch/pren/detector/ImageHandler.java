package ch.pren.detector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


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

    public static Bitmap loadImage(String filepath) {
        Bitmap bitmap = null;

        try {
            bitmap = BitmapFactory.decodeFile(filepath);

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        return bitmap;
    }
}