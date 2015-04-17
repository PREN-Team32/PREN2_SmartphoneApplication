/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.pren.model;

import java.io.File;
import java.io.Serializable;
import java.util.Observable;

/**
 *
 * Wrapperclass used to store and transmit the results of the object detector
 * @author Niklaus
 */
public class ValueItem extends Observable implements Serializable {
    private static ValueItem singeltonInstance;

    //Needed for identifying the object over serialization
    private static final long serialVersionUID = 90833161384221638L;

    //Values:
    public String originalImage;
    public String editedImage;

    public int mainArea;
    public int objectBorder;
    public int totalTimeUsed;
    public boolean foundShape;
    public double calculatedAngle;

    private ValueItem() {
        this.mainArea = 0;
        this.totalTimeUsed = 0;
        this.foundShape = false;
        this.calculatedAngle = 0;
    }

    public static final ValueItem getInstance() {
        if (singeltonInstance == null) {
            singeltonInstance = new ValueItem();
        }
        return singeltonInstance;
    }

    public void overrideValues(ValueItem newValues) {
        singeltonInstance = newValues;
        setChanged();
        notifyObservers();
    }
}
