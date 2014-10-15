package com.bustr.activities;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bustr.R;
import com.bustr.packets.ImagePacket;
import com.bustr.packets.SignalPacket;
import com.bustr.packets.SignalPacket.BustrSignal;
import com.bustr.utilities.BustrGrid;
import com.bustr.utilities.CameraPreview;
import com.bustr.utilities.ResourceProvider;

public class CameraActivity extends Activity {

   // Detect available cameras
   PackageManager pm;
   boolean camBack;
   boolean camFront;

   // Logcat tag used for Bustr debugging
   private final static String LOGTAG = "BUSTR";

   // Shared preferences reference
   private SharedPreferences sharedPrefs;

   // Shared preferences editor
   private SharedPreferences.Editor prefEditor;

   // Corresponds to front or rear camera
   private int cam;

   // Reference to hardware camera
   private Camera mCamera;

   // CameraPreview that renders to View
   private CameraPreview mPreview;

   // ShutterCallback instance handles taking picture
   private ShutterCallback shutterCallback;

   // PictureCallback handles jpg byte[]
   private PictureCallback pictureCallbackJPG;

   // Auto focus callback
   private AutoFocusCallback autoFocusCallback;

   // Byte[] to store image data
   private byte[] bytes;

   // Location Manager service
   LocationManager lm;

   // Caption to be attached to the image
   private String caption = "";

   // Boolean value to track when picture is taking
   private boolean takingPicture = false;

   // GUI elements -------------------------------------------------------------
   private ToggleButton btn_flash;
   private Button btn_discard;
   private Button btn_snap;
   private Button btn_flip;
   private Button btn_keep;
   private ProgressBar progress;

   // Initializes camera instance and location manager -------------------------
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Log.d(LOGTAG, "OnCreate");
      setContentView(R.layout.activity_camera);
      lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
            new LocationListener() {
               @Override
               public void onStatusChanged(String provider, int status,
                     Bundle extras) {
                  // TODO Auto-generated method stub

               }

               @Override
               public void onProviderEnabled(String provider) {
                  // TODO Auto-generated method stub

               }

               @Override
               public void onProviderDisabled(String provider) {
                  // TODO Auto-generated method stub

               }

               @Override
               public void onLocationChanged(Location location) {
                  // TODO Auto-generated method stub

               }
            });
      sharedPrefs = PreferenceManager
            .getDefaultSharedPreferences(getBaseContext());
      prefEditor = sharedPrefs.edit();
      pm = getPackageManager();
      camBack = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
      camFront = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
      if (camFront && camBack) {
         cam = sharedPrefs.getInt("camera",
               Camera.CameraInfo.CAMERA_FACING_BACK);
      } else {
         cam = 0;
      }

      // Wire GUI elements -----------------------------------------------------
      Typeface tf = ResourceProvider.instance(getApplicationContext())
            .getFont();
      btn_snap = (Button) findViewById(R.id.btn_snap);
      btn_flip = (Button) findViewById(R.id.btn_flip);
      btn_keep = (Button) findViewById(R.id.btn_keep);
      btn_discard = (Button) findViewById(R.id.btn_discard);
      btn_flash = (ToggleButton) findViewById(R.id.btn_flash);
      progress = (ProgressBar) findViewById(R.id.uploadProgress);
      btn_snap.setTypeface(tf);
      btn_keep.setTypeface(tf);
      btn_discard.setTypeface(tf);
      if (camFront && camBack) {
         btn_flip.setVisibility(View.VISIBLE);
      }

      // Camera callback -------------------------------------------------------
      shutterCallback = new ShutterCallback() {
         @Override
         public void onShutter() {
            // Currently unused
         }
      };

      // After JPG created callback --------------------------------------------
      pictureCallbackJPG = new PictureCallback() {
         @Override
         public void onPictureTaken(final byte[] pBytes, Camera cam) {
            // Camera.Parameters params = cam.getParameters();
            // TODO: params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            // cam.setParameters(params);
            bytes = pBytes;
            btn_keep.setVisibility(View.VISIBLE);
            btn_discard.setVisibility(View.VISIBLE);
            btn_snap.setVisibility(View.GONE);
            OnClickListener listener = new OnClickListener() {
               @Override
               public void onClick(View v) {
                  if (v.getId() == R.id.btn_keep) {
                     getCaptionFromUser();
                  } else if (v.getId() == R.id.btn_discard) {
                     btn_keep.setVisibility(View.GONE);
                     btn_discard.setVisibility(View.GONE);
                     btn_snap.setVisibility(View.VISIBLE);
                     mCamera.startPreview();
                  }
               }
            };
            btn_keep.setOnClickListener(listener);
            btn_discard.setOnClickListener(listener);
         }
      };

      // Auto focus even thandlerr ---------------------------------------------
      autoFocusCallback = new AutoFocusCallback() {
         @Override
         public void onAutoFocus(boolean focused, Camera pCam) {
            Log.d(LOGTAG, "Auto focus callback");
            if (takingPicture && focused == true) {
               mCamera.takePicture(shutterCallback, null, pictureCallbackJPG);
               takingPicture = false;
            }
         }
      };

      // Setup snap button -----------------------------------------------------
      btn_snap.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
               if (btn_flash.isChecked()) {
                  Camera.Parameters params = mCamera.getParameters();
                  params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                  mCamera.setParameters(params);
               }
               takingPicture = true;
               mCamera.autoFocus(autoFocusCallback);
            } else {
               promptEnableGPS();
            }
         }
      });

      // Setup flip button -----------------------------------------------------
      btn_flip.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            switchCamera();
         }
      });

      // Verify GPS service is enabled -----------------------------------------
      if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
         promptEnableGPS();
      }
   }

   // Asynchronous image upload class ------------------------------------------
   private class Uploader extends AsyncTask<Void, Void, BustrSignal> {

      // Grid location of image
      private float lat, lng;

      // Calculates grid dimensions
      public Uploader() {
         progress.setVisibility(View.VISIBLE);
         lat = BustrGrid.gridLat(lm);
         lng = BustrGrid.gridLon(lm);
      }

      // Uploads image to server asynchronously --------------------------------
      @Override
      protected BustrSignal doInBackground(Void... params) {
         Socket socket = null;
         BustrSignal returnCode = null;
         ObjectOutputStream output;
         ObjectInputStream input;
         String randomName = Long.toString(new Random().nextLong()) + ".jpg";
         try {
            socket = new Socket(InetAddress.getByName("50.173.32.127"), 8000);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            output.writeObject(new ImagePacket(randomName, bytes, lat, lng,
                  caption));
            returnCode = ((SignalPacket) input.readObject()).getSignal();
            output.close();
            input.close();
            socket.close();
         } catch (Exception e) {
            returnCode = BustrSignal.FAILURE;
            Log.e(LOGTAG, e.toString());
         }
         return returnCode;
      }

      // Displays return code message after attempting upload ------------------
      @Override
      protected void onPostExecute(BustrSignal result) {
         progress.setVisibility(View.GONE);
         String result_message = "Unexpected signal returned";
         if (result == BustrSignal.SUCCESS) {
            result_message = "Upload Successful";
         } else if (result == BustrSignal.FAILURE) {
            result_message = "Upload Failed";
         }
         Toast.makeText(getBaseContext(), result_message, Toast.LENGTH_LONG)
               .show();
      }
   }

   // Toggles active camera and saves to shared preferences --------------------
   public void switchCamera() {
      if (cam == Camera.CameraInfo.CAMERA_FACING_BACK) {
         prefEditor.putInt("camera", Camera.CameraInfo.CAMERA_FACING_FRONT)
               .commit();
      } else {
         prefEditor.putInt("camera", Camera.CameraInfo.CAMERA_FACING_BACK)
               .commit();
      }
      startActivity(new Intent(this, CameraActivity.class));
      finish();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present. -
      getMenuInflater().inflate(R.menu.camera, menu);
      return true;
   }

   @Override
   protected void onStop() {
      super.onStop();
      Log.d(LOGTAG, "onStop");
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml. --------------
      int id = item.getItemId();
      if (id == R.id.action_settings) {
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   // Returns camera instance, returns null if not available -------------------
   public static Camera getCameraInstance() {
      Camera c = null;
      try {
         c = Camera.open();
      } catch (Exception e) {
         Log.e(LOGTAG, e.toString());
      }
      return c;
   }

   // Stops camera and camera preview when activity is paused ------------------
   @Override
   protected void onPause() {
      super.onPause();
      Log.d(LOGTAG, "onPause");
      mPreview.stopEverything();
   }

   // Re-initializes camera and checks if GPS is enabled -----------------------
   @Override
   protected void onResume() {
      super.onResume();
      Log.d(LOGTAG, "OnResume");

      mCamera = Camera.open(cam);
      Camera.Parameters params = mCamera.getParameters();
      List<Size> imageSizes = params.getSupportedPictureSizes();
      Collections.reverse(imageSizes);
      Size currentSize = imageSizes.get(0);
      // for (Size size : imageSizes) {
      // Log.d(LOGTAG, size.width + " x " + size.height);
      // if (size.width < 960) {
      // currentSize = size;
      // }
      // else {
      Toast.makeText(CameraActivity.this,
            "Picture size: " + currentSize.width + " x " + currentSize.height,
            Toast.LENGTH_SHORT).show();
      // break;
      // }
      // }
      mPreview = new CameraPreview(this, mCamera, !(camFront && camBack));
      params.set("orientation", "portrait");
      params.setPictureSize(currentSize.width, currentSize.height);
      mCamera.setParameters(params);
      FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
      CameraPreview.setCameraDisplayOrientation(this, cam, mCamera,
            !(camFront && camBack));
      btn_flash.setChecked(false);
      preview.addView(mPreview);
   }

   // Requests that user enable GPS service ------------------------------------
   private void promptEnableGPS() {
      AlertDialog.OnClickListener listener = new AlertDialog.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int which) {
            Intent gpsOptionsIntent = new Intent(
                  android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsOptionsIntent);
         }
      };
      new AlertDialog.Builder(this).setTitle("GPS Required")
            .setMessage("Please enable GPS location")
            .setNeutralButton("Ok", listener).setCancelable(false)
            .setIcon(android.R.drawable.ic_dialog_alert).show();
   }

   // Prompt user to provide image caption -------------------------------------
   private void getCaptionFromUser() {
      final EditText captionInput = new EditText(this);
      AlertDialog.OnClickListener listener = new AlertDialog.OnClickListener() {
         @Override
         public void onClick(DialogInterface arg0, int arg1) {
            caption = captionInput.getText().toString();
            new Uploader().execute();
         }
      };
      new AlertDialog.Builder(this).setTitle("Add a caption?")
            .setView(captionInput).setNeutralButton("Ok", listener)
            .setIcon(android.R.drawable.ic_input_get).show();
   }
}
