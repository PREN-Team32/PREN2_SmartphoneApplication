/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.pren.model;
import java.io.Serializable;
import java.util.Observable;


/**
 *
 * Wrapperclass used to store and transmit the configuration details for the detector.
 * @author Nikk
 */
public class ConfigurationItem extends Observable implements Serializable {
    private static ConfigurationItem singeltonInstance;
    private static final long serialVersionUID = 80833161384221638L;

    //Struct-Values:
    public float luminanceThreshold;

    //Width & Height which will be analysed (Rest of Image will be cut off)
    public int widthToObserve;
    public int heightToObserve;

    //Amount of visited adjacent Pixels to determine a shape.
    public int visitedPixels;

    //Boolean flag to indicate start
    public boolean startSignal;

    //Factor for AngleCalculation
    public double pixelToCm;

    private ConfigurationItem() {
        this.luminanceThreshold = 0.3f;
        this.widthToObserve = 488;
        this.heightToObserve = 500;
        this.visitedPixels = 3;
        this.startSignal = false;
        this.pixelToCm = 0.07463d;
    }

    public static ConfigurationItem getInstance() {
        if (singeltonInstance == null) {
            singeltonInstance = new ConfigurationItem();
        }
        return singeltonInstance;
    }

    public void overrideConfig(ConfigurationItem newConfig) {
        singeltonInstance = newConfig;
        setChanged();
        notifyObservers();
    }
}  
