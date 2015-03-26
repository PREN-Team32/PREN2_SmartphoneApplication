package ch.pren.model;

import android.graphics.Bitmap;
import android.media.Image;

import java.io.File;
import java.io.Serializable;

/**
 * Wrapperclass used to store and transmit the results of the object detector
 *
 * @author Niklaus
 */
public class ValueItem implements Serializable {

    private static ValueItem instance = null;


    protected ValueItem() {
    }

    public static ValueItem getInstance() {
        if (instance == null) {
            instance = new ValueItem();
        }
        return instance;
    }


    public byte[] originalImage;
    public byte[] editedImage;

    public int mainArea;
    public int totalTimeUsed;
    public boolean foundShape;
    public int calculatedAngle;
}
