package com.bustr.utilities;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview 
extends SurfaceView 
implements SurfaceHolder.Callback {

    private final String LOGTAG = "BUSTR";
    
    // Private fields ----------------------------------------------------------
    private SurfaceHolder mHolder;
    private Camera mCamera;
    
    @SuppressWarnings("deprecation")
    public CameraPreview(Context context, Camera camera, boolean pSC) {
        super(context);
        mCamera = camera;        
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    // Surface has been created, now tell the camera where to draw preview -----
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);            
            mCamera.startPreview();
        }
        catch(IOException e) {
            Log.d(LOGTAG, e.toString());
        } 
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Not used; Camera release happens in calling activity        
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        if(mHolder.getSurface() == null) {
            return;
        }
        
        try {
            mCamera.stopPreview();
        }
        catch(Exception e) {}
        
        try {
            Log.d("camera", "SURFACE CHANGED");
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }
        catch(Exception e) {
            Log.d(LOGTAG, e.toString());
        }
    }

    public static void setCameraDisplayOrientation(Activity activity,
            int cameraId, android.hardware.Camera camera, boolean singleCamera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 90; break;
        }

        int result;
        Camera.Parameters params = camera.getParameters();
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
//            params.setRotation(result+180);
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
            params.setRotation(result);
        }
//        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(params);        
        camera.setDisplayOrientation(result);
    }
    
    public void stopEverything() {
       mCamera.stopPreview();
       mCamera.setPreviewCallback(null);
       mCamera.release();
       mCamera = null;
       mHolder.removeCallback(CameraPreview.this);       
    }
}
