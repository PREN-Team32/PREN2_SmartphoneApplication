package ch.pren.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Thomas on 09.12.2014.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    public final static String DEBUG_TAG = "MakePhotoActivity";
    private SurfaceHolder mSurfaceHolder;
    public Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);

        this.mCamera = camera;

        this.mSurfaceHolder = this.getHolder();
        this.mSurfaceHolder.addCallback(this); // we get notified when underlying surface is created and destroyed
        this.mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //this is a deprecated method, is not requierd after 3.0
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            Log.d(DEBUG_TAG, "surface Created");
        } catch (IOException e) {

            e.printStackTrace(); }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.stopPreview();
        mCamera.release();
        Log.d(DEBUG_TAG, "surface Destroyed");
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            Log.d(DEBUG_TAG, "surface Changed");
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

}
