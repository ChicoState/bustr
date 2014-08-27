package com.bustr.utilities;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview 
extends SurfaceView 
implements SurfaceHolder.Callback{

    private final String LOGTAG = "BUSTR";
    
    // Private fields ----------------------------------------------------------
    private SurfaceHolder mHolder;
    private Camera mCamera;
    
    @SuppressWarnings("deprecation")
    public CameraPreview(Context context, Camera camera) {
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
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }
        catch(Exception e) {
            Log.d(LOGTAG, e.toString());
        }
    }


}
