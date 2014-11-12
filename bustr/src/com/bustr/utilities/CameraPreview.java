package com.bustr.utilities;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview 
extends SurfaceView 
implements SurfaceHolder.Callback {

    private final String LOGTAG = "BUSTR";
    
    // Private fields ----------------------------------------------------------
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private PreviewCallback previewCallback;
    
    public boolean takingPicture = false;
    
    @SuppressWarnings("deprecation")
    public CameraPreview(Context context, Camera camera, boolean pSC, PreviewCallback pbc) {
        super(context);
        previewCallback = pbc;
        mCamera = camera;        
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    // Surface has been created, now tell the camera where to draw preview -----
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);            
            mCamera.startPreview();
            mCamera.setPreviewCallback(previewCallback);
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
            mCamera.setPreviewCallback(previewCallback);
        }
        catch(Exception e) {
            Log.d(LOGTAG, e.toString());
        }
    }
    
    public void stopEverything() {
       mCamera.stopPreview();
       mCamera.setPreviewCallback(null);
       mCamera.release();
       mCamera = null;
       mHolder.removeCallback(CameraPreview.this);       
    }
}
