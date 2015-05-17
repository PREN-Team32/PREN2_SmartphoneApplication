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
    private static double pixelToCm = 0.1272;
    private static final int fullImageWidthInPx = ImageHandler.WINDOW_WIDTH;
    private static final int fullBucketWidthInPx = ImageHandler.WINDOW_HEIGHT;
    private static final int halfBucketWidthInPx = 140;
    private static int bucketMidPosition = 0;
    
    public static double getAngle(int objectBorder, boolean isLeft) {
        double angle = 0.0;
        double ankathete = 170;
        double gegenkathete;
        int midPositionInPx = (fullImageWidthInPx / 2);
        if(isLeft) {
            bucketMidPosition = objectBorder + halfBucketWidthInPx;
        }
        else {
            bucketMidPosition = objectBorder - halfBucketWidthInPx;
        }
        if(objectBorder >= midPositionInPx) {            ;
            gegenkathete = (bucketMidPosition - midPositionInPx)*pixelToCm;
            angle = Math.toDegrees(Math.atan2(gegenkathete, ankathete));
            return angle;
            //returns negative angle if object on right side
        }
        else {
            if(bucketMidPosition < 0) {
                bucketMidPosition = 0;
            }
            gegenkathete = (midPositionInPx - bucketMidPosition)*pixelToCm;
            angle = Math.toDegrees(Math.atan2(gegenkathete, ankathete));
            return angle*(-1);
            //returns positive angle if object on left side
        }
    }


    public static void setPixelToCm(double pixelToCm) {
        AngleCalculator.pixelToCm = pixelToCm;
    }
}
