package ch.pren.model;

import java.io.Serializable;

/**
 * Wrapperclass used to store and transmit the results of the object detector
 *
 * @author Niklaus
 */
public class ValueItem implements Serializable {

    private static ValueItem instance = null;

    private static final long serialVersionUID = 90833161384221638L;

    protected ValueItem() {
    }

    public static ValueItem getInstance() {
        if (instance == null) {
            instance = new ValueItem();
        }
        return instance;
    }


    public String originalImage;
    public String editedImage;

    public int mainArea;
    public int totalTimeUsed;
    public boolean foundShape;
    public int calculatedAngle;
}
