package com.bustr.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bustr.R;
import com.bustr.packets.ImagePacket;
import com.bustr.packets.SignalPacket;
import com.bustr.packets.SignalPacket.BustrSignal;
import com.bustr.utilities.BustrDialog;
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

   // PreviewCallback
   private PreviewCallback previewCallback;

   // Auto focus callback
   private AutoFocusCallback autoFocusCallback;

   // Byte[] to store image data
   private byte[] bytes;

   // Location Manager service
   LocationManager lm;

   // Boolean value to track when picture is taking
   private boolean takingPicture = false;

   // Stores flash enabled/disabled
   private boolean flashEnabled = false;

   // GUI elements -------------------------------------------------------------
   private ImageView btn_flash;
   private ImageView btn_snap;
   private ImageView btn_flip;
   private ImageView btn_save;
   private ImageView btn_keep;
   private ImageView btn_discard;
   private ProgressBar progress;
   private FrameLayout container;
   private FrameLayout preview;

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
      }
      else {
         cam = 0;
      }

      // Wire GUI elements -----------------------------------------------------
      Typeface tf = ResourceProvider.instance(getApplicationContext())
            .getFont();
      btn_snap = (ImageView) findViewById(R.id.btn_snap);
      btn_flip = (ImageView) findViewById(R.id.btn_flip);
      btn_keep = (ImageView) findViewById(R.id.btn_keep);
      btn_discard = (ImageView) findViewById(R.id.btn_discard);
      btn_flash = (ImageView) findViewById(R.id.btn_flash);
      btn_save = (ImageView) findViewById(R.id.btn_save);
      progress = (ProgressBar) findViewById(R.id.uploadProgress);
      preview = (FrameLayout) findViewById(R.id.camera_preview);
      container = (FrameLayout) findViewById(R.id.container);
      if (camFront && camBack) {
         btn_flip.setVisibility(View.VISIBLE);
      }

      // Auto focus event handler ----------------------------------------------
      autoFocusCallback = new AutoFocusCallback() {
         @Override
         public void onAutoFocus(boolean focused, Camera pCam) {
            Log.d(LOGTAG, "Auto focus callback");
            if (takingPicture && focused == true) {
               mPreview.takingPicture = true;
               takingPicture = false;
            }
         }
      };

      // Preview Callback event
      previewCallback = new PreviewCallback() {
         @Override
         public void onPreviewFrame(byte[] data, Camera camera) {

            if (mPreview.takingPicture == true) {

               Log.d(LOGTAG, "Picture has been taken");
               mPreview.takingPicture = false;

               Log.d(LOGTAG, "Stopping preview");
               mPreview.animate();
               mCamera.stopPreview();
               if (flashEnabled) {

                  Camera.Parameters p = mCamera.getParameters();
                  p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                  mCamera.setParameters(p);                  
               }

               Size previewSize = camera.getParameters().getPreviewSize();
               Log.d(LOGTAG, "[PREVIEW-SIZE] h: " + previewSize.height
                     + ", w: " + previewSize.width);

               YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21,
                     previewSize.width, previewSize.height, null);

               ByteArrayOutputStream baos = new ByteArrayOutputStream();

               // TODO: Scale the images to a uniform size here
               yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width,
                     previewSize.height), 80, baos);

               byte[] jdata = baos.toByteArray();

               // Convert to Bitmap
               Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0,
                     jdata.length);

               if (bmp.getWidth() > bmp.getHeight()) {
                  Log.d(LOGTAG, "Rotating bitmap...");
                  bmp = ResourceProvider.instance(CameraActivity.this)
                        .rotateBmp(bmp);
                  ByteArrayOutputStream stream = new ByteArrayOutputStream();
                  bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                  bytes = stream.toByteArray();
                  Log.d(LOGTAG, "Bitmap Rotated.");
               }
               else {
                  bytes = data;
               }

               showPostPictureButtons();

               OnClickListener listener = new OnClickListener() {
                  @Override
                  public void onClick(View v) {
                     if (v.getId() == R.id.btn_keep) {
                        getCaptionFromUser();
                     }
                     else if (v.getId() == R.id.btn_discard) {

                        showPrePictureButtons();
                        mCamera.startPreview();
                        mCamera.setPreviewCallback(previewCallback);
                     }
                     else if (v.getId() == R.id.btn_save)
                        try {
                           saveLocalCopy(bytes);
                        } catch (FileNotFoundException e) {
                           e.printStackTrace();
                        } catch (IOException e) {
                           e.printStackTrace();
                        }
                  }

               };
               btn_keep.setOnClickListener(listener);
               btn_save.setOnClickListener(listener);
               btn_discard.setOnClickListener(listener);
               Log.d(LOGTAG, "h: " + bmp.getHeight() + "w: " + bmp.getWidth());
            }
         }
      };

      // Setup snap button -----------------------------------------------------
      btn_snap.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
               if (flashEnabled) {
                  Camera.Parameters params = mCamera.getParameters();
                  params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                  // params.setRotation(90);
                  mCamera.setParameters(params);
               }
               takingPicture = true;
               mCamera.autoFocus(autoFocusCallback);
            }
            else {
               promptEnableGPS();
            }
         }
      });

      // Setup flip/flash buttons ----------------------------------------------
      OnClickListener flip_flash = new OnClickListener() {
         @Override
         public void onClick(View v) {
            if (v.getId() == R.id.btn_flip) {
               switchCamera();               
            }
            else if (v.getId() == R.id.btn_flash) {
               flashEnabled = flashEnabled ? false : true;
               btn_flash.setImageDrawable(getResources().getDrawable(
                     flashEnabled ? R.drawable.yellowflashglow
                           : R.drawable.flash));
               Log.d(LOGTAG, "Flash enabled: " + flashEnabled);
            }
         }
      };
      btn_flip.setOnClickListener(flip_flash);
      btn_flash.setOnClickListener(flip_flash);

      // Verify GPS service is enabled -----------------------------------------
      if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
         promptEnableGPS();
      }
   }

   // Asynchronous image upload class ------------------------------------------
   private class Uploader extends AsyncTask<Void, Void, BustrSignal> {

      // Grid location of image
      private float lat, lng;

      // Image caption from user
      private String caption;

      // Cosntrcutor (Calculates grid dimensions)
      public Uploader(String pCaption) {
         caption = pCaption;
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
         String username = sharedPrefs.getString("username", "no_user");
         try {
            socket = new Socket(InetAddress.getByName("50.173.32.127"), 8000);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            output.writeObject(new ImagePacket(username, bytes, lat, lng,
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
         }
         else if (result == BustrSignal.FAILURE) {
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
      }
      else {
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
      Log.d(LOGTAG, "Opening camera");
      mCamera = Camera.open(cam);
      mPreview = new CameraPreview(this, mCamera, !(camFront && camBack),
            previewCallback);
      flashEnabled = false;
      preview.addView(mPreview);
      showPrePictureButtons();

      Log.d(LOGTAG, "Starting preview");
      mCamera.startPreview();
   }

   // Requests that user enable GPS service ------------------------------------
   private void promptEnableGPS() {
      final BustrDialog GpsDialog = new BustrDialog(CameraActivity.this,
            R.layout.bustr_alert_layout_view);
      GpsDialog.setCustomTitle("Enable GPS");
      GpsDialog.setAlertMessage("Please enable GPS to continue using Bustr.");
      GpsDialog.setCancelable(false);
      OnClickListener listener = new OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent gpsOptionsIntent = new Intent(
                  android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsOptionsIntent);
            GpsDialog.dismiss();
         }
      };
      GpsDialog.setButtonListener(listener);
      GpsDialog.show();
   }

   // Prompt user to provide image caption -------------------------------------
   private void getCaptionFromUser() {

      final BustrDialog captionDialog = new BustrDialog(CameraActivity.this,
            R.layout.bustr_input_dialog_view);
      OnClickListener listener = new OnClickListener() {
         @Override
         public void onClick(View v) {
            String caption = ((EditText) captionDialog
                  .findViewById(R.id.comment_input)).getText().toString();
            new Uploader(caption).execute();
            captionDialog.dismiss();
         }
      };
      captionDialog.setCustomTitle("Add a Caption");
      captionDialog.setButtonListener(listener);
      captionDialog.show();

      // final EditText captionInput = new EditText(this);
      // AlertDialog.OnClickListener listener = new
      // AlertDialog.OnClickListener() {
      // @Override
      // public void onClick(DialogInterface arg0, int arg1) {
      // caption = captionInput.getText().toString();
      // }
      // };
      // new AlertDialog.Builder(this).setTitle("Add a caption?")
      // .setView(captionInput).setNeutralButton("Ok", listener)
      // .setIcon(android.R.drawable.ic_input_get).show();
   }

   private void saveLocalCopy(byte[] bytes) throws FileNotFoundException,
         IOException {
      File folder = new File(Environment.getExternalStorageDirectory()
            + "/Bustr/");
      folder.mkdir();
      Date date = new Date();
      File dest = new File(folder, new Timestamp(date.getTime()) + ".jpg");
      FileOutputStream outStream = new FileOutputStream(dest);
      outStream.write(bytes, 0, bytes.length);
      outStream.close();
      Toast.makeText(CameraActivity.this, "File saved.", Toast.LENGTH_SHORT)
            .show();
   }

   private void showPostPictureButtons() {
      btn_keep.setVisibility(View.VISIBLE);
      btn_discard.setVisibility(View.VISIBLE);
      btn_save.setVisibility(View.VISIBLE);
      btn_snap.setVisibility(View.GONE);
      btn_flash.setVisibility(View.GONE);
      btn_flip.setVisibility(View.GONE);
   }

   private void showPrePictureButtons() {
      btn_keep.setVisibility(View.GONE);
      btn_discard.setVisibility(View.GONE);
      btn_save.setVisibility(View.GONE);
      btn_snap.setVisibility(View.VISIBLE);
      btn_flip.setVisibility(View.VISIBLE);
      btn_flash.setVisibility(View.VISIBLE);
   }

}
