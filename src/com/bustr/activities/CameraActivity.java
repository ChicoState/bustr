package com.bustr.activities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bustr.R;
import com.bustr.utilities.CameraPreview;
import com.bustr.utilities.ResourceProvider;

public class CameraActivity extends Activity implements LocationListener {

   private final static String LOGTAG = "BUSTR";

   private Camera mCamera;
   private CameraPreview mPreview;
   private ShutterCallback shutterCallback;
   private PictureCallback pictureCallbackJPG;

   private LocationManager locationManager;

   // GUI elements -------------------------------------------------------------
   private TextView lat_long_view;
   private Button btn_snap;

   @TargetApi(Build.VERSION_CODES.GINGERBREAD)
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_camera);

      locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
            0, this);

      // Wire GUI elements -----------------------------------------------------
      lat_long_view = (TextView) findViewById(R.id.lat_long_view);
      btn_snap = (Button) findViewById(R.id.btn_snap);
      btn_snap.setTypeface(ResourceProvider.instance(getApplicationContext())
            .getFont());

      mCamera = getCameraInstance();
      mPreview = new CameraPreview(this, mCamera);
      FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
      CameraPreview.setCameraDisplayOrientation(this,
            Camera.CameraInfo.CAMERA_FACING_FRONT, mCamera);
      preview.addView(mPreview);

      // Camera callback -------------------------------------------------------
      shutterCallback = new ShutterCallback() {
         @Override
         public void onShutter() {
            Toast.makeText(getBaseContext(), "CLICK", Toast.LENGTH_SHORT)
                  .show();
         }
      };

      pictureCallbackJPG = new PictureCallback() {
         @Override
         public void onPictureTaken(byte[] bytes, Camera cam) {
//            Bitmap bitmapPicture = BitmapFactory.decodeByteArray(bytes, 0,
//                  bytes.length);
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/saved_images");
            myDir.mkdirs();
            File file = new File(myDir, "bustr_image.jpg");
            
            try {
               BufferedOutputStream bos = new BufferedOutputStream(
                     new FileOutputStream(file));
               bos.write(bytes);
               bos.flush();
               bos.close();
            } catch (IOException ioe) {
               Log.d(LOGTAG, ioe.toString());
            }
         }
      };

      // Setup snap button -----------------------------------------------------
      btn_snap.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            mCamera.takePicture(shutterCallback, null, pictureCallbackJPG);
         }
      });
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {

      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.camera, menu);
      return true;
   }

   @Override
   protected void onStop() {
      super.onStop();
      mCamera.release();
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();
      if (id == R.id.action_settings) {
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   public static Camera getCameraInstance() {
      Camera c = null;
      try {
         c = Camera.open();
      } catch (Exception e) {
         Log.d(LOGTAG, e.toString());
      }
      return c;
   }

   @Override
   public void onLocationChanged(Location loc) {
      String lat_long = "lat: " + loc.getLatitude() + "\nlong: "
            + loc.getLongitude();
      Log.d(LOGTAG, lat_long);
      lat_long_view.setText(lat_long);
   }

   @Override
   public void onProviderDisabled(String arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void onProviderEnabled(String arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
      // TODO Auto-generated method stub

   }

}
