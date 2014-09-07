package com.bustr.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bustr.R;
import com.bustr.utilities.CameraPreview;

public class CameraActivity extends ActionBarActivity implements
      LocationListener {

   private final static String LOGTAG = "BUSTR";

   private Camera mCamera;
   private CameraPreview mPreview;
   private LocationManager locationManager;

   private TextView lat_long_view;

   @TargetApi(Build.VERSION_CODES.GINGERBREAD)
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_camera);

      locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
            0, this);
      lat_long_view = (TextView) findViewById(R.id.lat_long_view);

      mCamera = getCameraInstance();
      mPreview = new CameraPreview(this, mCamera);
      FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
      CameraPreview.setCameraDisplayOrientation(this,
            Camera.CameraInfo.CAMERA_FACING_FRONT, mCamera);
      preview.addView(mPreview);

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
