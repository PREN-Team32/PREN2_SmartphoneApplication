/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.pren.detector;

import ch.pren.androidapp.MainActivity;

/**
 *
 * @author Niklaus
 */
public class AngleCalculator {
    private static double pixelToCm = 0.07463;
    private static final int fullImageWidthInPx = 2592;
    private static final int fullBucketWidthInPx = 520;
    
    private static double getAngle(int objectBorder) {
        double angle = 0.0;
        double ankathete = 170;
        double gegenkathete;
        int midPositionInPx = (fullImageWidthInPx / 2);
        if(objectBorder >= midPositionInPx) {
            gegenkathete = objectBorder - midPositionInPx;
            angle = Math.toDegrees(Math.atan2(gegenkathete, ankathete));
            return angle*(-1);
            //returns negative angle if object on right side
        }
        else {
            gegenkathete = midPositionInPx - objectBorder;
            angle = Math.toDegrees(Math.atan2(gegenkathete, ankathete));
            return angle;
            //returns positive angle if object on left side
        }
    }
    
    public static byte getSteps(int objectBorder) throws IllegalArgumentException {
        byte steps = 0;
        double angle = getAngle(objectBorder);
        System.out.println("#AngleCalculator: Resulting angle from given Coordinate = " + angle + "°");
        
        if(angle < 20.5) {
            steps = (byte)Math.abs(angle/1.8);
        }
        else {
            throw new IllegalArgumentException("#AngleCalculator: Angle (" + angle + "°) is too large!!!");
        }
        return steps;
    }

    public static void setPixelToCm(double pixelToCm) {
        AngleCalculator.pixelToCm = pixelToCm;
    }
}
