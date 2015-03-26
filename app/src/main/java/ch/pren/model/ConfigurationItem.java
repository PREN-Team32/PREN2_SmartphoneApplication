package ch.pren.model;


import java.io.Serializable;

/**
 * Wrapperclass used to store and transmit the configuration details for the detector.
 *
 * @author Nikk
 */
public class ConfigurationItem implements Serializable {
    private static ConfigurationItem instance = null;


    protected ConfigurationItem() {
    }

    public static ConfigurationItem getInstance() {
        if (instance == null) {
            instance = new ConfigurationItem();
        }
        return instance;
    }

    public int luminanceThreshold;

    //Width & Height which will be analysed (Rest of Image will be cut off)
    public int withToObserve;
    public int heightToObserve;

    //Amount of visited adjacent Pixels to determine a shape.
    public int visitedPixels;

    //Boolean flag to indicate start
    public boolean startSignal;
}
