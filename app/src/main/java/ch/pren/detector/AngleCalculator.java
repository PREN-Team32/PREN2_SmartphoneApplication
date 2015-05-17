
package ch.pren.detector;


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

    public static String getStepsAsString(final double angle){
        // 1° entspricht 1964 mic steps
        if(angle <= 0){
            double notSignedAngle = Math.abs(angle);
            // links ist f
            return "f " + ( (int) (notSignedAngle * 1964) + 1);
        }else {
            return "r " + ( (int) (angle * 1964));
        }
    }


    public static void setPixelToCm(double pixelToCm) {
        AngleCalculator.pixelToCm = pixelToCm;
    }
}
