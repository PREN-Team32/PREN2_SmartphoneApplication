package ch.pren.detector;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import static ch.pren.detector.ImageHandler.WINDOW_WIDTH;
import static ch.pren.androidapp.MainActivity.DEBUG_TAG;

/**
 * Created by Nikk
 */
public class Detector {
    private Bitmap editedImage;
    private Bitmap originalImage;
    private int brightPixCount = 0;
    private int darkPixCount = 0;
    private int mainAreaX;
    private int mainAreaY;
    private long gebrauchteZeit;
    private int objectBorder;
    private boolean isBucketShape;
    private double pixeltocm;

    public long getGebrauchteZeit() {
        return gebrauchteZeit;
    }

    public int getMainAreaX() {
        return mainAreaX;
    }

    //Zu konfigurierende Variabeln
    protected static float LUMINANCETHRESHOLD = 0.3f;
    protected static int VISITED_PIXELS = 3; //Amount of visited adjacent Pixels to determine a shape.

    //Zur Zeitmessung
    private long zeitVorher;
    private long zeitNachher;


    public Detector(byte[] rawImage) {
        originalImage = ImageHandler.loadImage(rawImage);
        editedImage = originalImage;
    }

    public Bitmap getEditedImage() {
        return editedImage;
    }

    public Bitmap getOriginalImage() {
        return originalImage;
    }

    public boolean getIsBucketShape(){ return isBucketShape; }

    public static void setLuminanceThreshold(float LUMINANCETHRESHOLD) {
        Detector.LUMINANCETHRESHOLD = LUMINANCETHRESHOLD;
    }

    public static void setVisitedPixels(int VISITED_PIXELS) {
        Detector.VISITED_PIXELS = VISITED_PIXELS;
    }

    public void setPixeltocm(double pixeltocm) {
        this.pixeltocm = pixeltocm;
    }



    public double start() throws BucketNotFoundException {
        //Step 1:
        //Looping through all Pixels, determine luminance and evaluate it against LUMINANCETHRESHOLD
        //to determine whether to color the pixel black or white
        //
        //Useful Hint:
        //Make sure to make the outer loop over the y-coordinate. This will likely make the code much
        //faster, as it will be accessing the image data in the order it's stored in memory. (As rows of pixels.)
        zeitVorher = System.currentTimeMillis();
        for (int y = 0; y < editedImage.getHeight(); y++) {
            for (int x = 0; x < editedImage.getWidth(); x++) {
                int  clr   = editedImage.getPixel(x, y);
                int  red   = (clr & 0x00ff0000) >> 16;
                int  green = (clr & 0x0000ff00) >> 8;
                int  blue  =  clr & 0x000000ff;


                //calc luminance in range 0.0 to 1.0; using SRGB luminance constants
                float luminance = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;

                //choose brightness threshold as appropriate:
                if (luminance >= LUMINANCETHRESHOLD) {
                    editedImage.setPixel(x, y, Color.WHITE);
                    brightPixCount++;
                } else {
                    editedImage.setPixel(x, y, Color.BLACK);
                    darkPixCount++;
                }
            }
        }
        //Step 2:
        //Core detection mechanism
        this.objectBorder = findObject(calculateMainArea());

        //Step 3:
        //Evaluate results
        AngleCalculator.setPixelToCm(this.pixeltocm);
        boolean isLeft = (mainAreaX > WINDOW_WIDTH /2);
        double angleInDegrees = AngleCalculator.getAngle(objectBorder, isLeft);
        zeitNachher = System.currentTimeMillis();
        this.gebrauchteZeit = zeitNachher - zeitVorher;
        Log.d(DEBUG_TAG, "#Detektor: Object detected at X = " + objectBorder);
        Log.d(DEBUG_TAG, "#Detektor: Bright | Dark Pixels = " + brightPixCount + " | " + darkPixCount);
        Log.d(DEBUG_TAG, "#Detektor: Time used: " + gebrauchteZeit + " ms");
        return angleInDegrees;
    }

    private int findObject(int mainArea) throws BucketNotFoundException {
        int rgbCurrentPixel;
        int xCoordinate;
        Log.d(DEBUG_TAG, "#Detektor: Attempting to find bucket..");
        //Seek shape of the basket, starting from the right side.
        if(mainArea < WINDOW_WIDTH /2) {
            xCoordinate = Integer.MIN_VALUE;
            for (int y = editedImage.getHeight()-1; y > 0; y--) {
                //Care for visitedFields variable (x must be larger!!)
                for (int x = editedImage.getWidth()-5; x > 5; x--) {
                    rgbCurrentPixel = editedImage.getPixel(x, y);
                    if(rgbCurrentPixel == Color.BLACK) {
                        if(isBucketShape(x, y, false)) {
                            //System.out.print("(" + x + ", " + y + ") /");
                            editedImage.setPixel(x, y, Color.RED);
                            if(x > xCoordinate) {
                                xCoordinate = x;
                            }
                        }
                    }
                }
            }
        }
        //Seek shape of the basket, starting from the left side.
        else if(mainArea > WINDOW_WIDTH /2) {
            xCoordinate = Integer.MAX_VALUE;
            for (int y = 0; y < editedImage.getHeight(); y++) {
                //Care for visitedFields variable (x must be larger!!)
                for (int x = 5; x < editedImage.getWidth()-5; x++) {
                    if(isBucketShape(x, y, true)) {
                        //System.out.print("(" + x + ", " + y + ") /");
                        editedImage.setPixel(x, y, Color.RED);
                        if(x < xCoordinate) {
                            xCoordinate = x;
                        }
                    }
                }
            }
        }
        //Else, basket must be in the middle.
        else {
            xCoordinate = WINDOW_WIDTH /2;
        }

        if(xCoordinate == Integer.MIN_VALUE || xCoordinate == Integer.MAX_VALUE) {
            System.err.println("#Detektor: NO SHAPE FOUND.");
            throw new BucketNotFoundException();
        }
        return xCoordinate;
    }

    private int calculateMainArea() throws BucketNotFoundException {
        try {
            int totalX = 0;
            int totalY = 0;
            int blackPixCount = 0;
            for (int y = 0; y < editedImage.getHeight(); y++) {
                for (int x = 0; x < editedImage.getWidth(); x++) {
                    int rgbCode = editedImage.getPixel(x, y);
                    if (rgbCode == Color.BLACK) {
                        totalX += x;
                        totalY += y;
                        blackPixCount++;
                    }
                }
            }
            if(blackPixCount == 0) {
                System.err.println("#Detector: 0 black pixels were found in the picture.");
                throw new BucketNotFoundException();
            }
            else {
                this.mainAreaX = totalX / blackPixCount;
                this.mainAreaY = totalY / blackPixCount;
                Log.d(DEBUG_TAG, "#Detector: Found Main Area at coordinates: " + mainAreaX + " / " + mainAreaY);
                return mainAreaX;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    private boolean isBucketShape(int x, int y, boolean fromLeft) {
        isBucketShape = true;
        int[] rgbToLeft = new int[VISITED_PIXELS];
        int[] rgbToRight = new int[VISITED_PIXELS];

        for(int i = 0; i < VISITED_PIXELS; i++) {
            rgbToLeft[i] = editedImage.getPixel(x - (i + 1), y);
            rgbToRight[i] = editedImage.getPixel(x + (i + 1), y);
        }

        if(fromLeft) {
            for(int i = 0; i < VISITED_PIXELS; i++) {
                if(rgbToLeft[i] != Color.WHITE || rgbToRight[i] != Color.BLACK) {
                    isBucketShape = false;
                }
            }
        }
        else {
            for(int i = 0; i < VISITED_PIXELS; i++) {
                if(rgbToLeft[i] != Color.BLACK || rgbToRight[i] != Color.WHITE) {
                    isBucketShape = false;
                }
            }
        }
        return isBucketShape;
    }



    public double calculateAngle(int objectBorder, boolean fromRight) {
        double angle = 0.0;


        return angle;
    }
}
