package ch.pren.model;

import java.io.File;
import java.io.Serializable;

/**
 * Wrapperclass used to store and transmit the results of the object detector
 *
 * @author Niklaus
 */
public class ValueItem implements Serializable {
    public File originalImage;
    public File editedImage;

    public int mainArea;
    public int totalTimeUsed;
    public boolean foundShape;
    public int calculatedAngle;
}
