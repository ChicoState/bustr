package com.bustr.activities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bustr.R;
import com.bustr.packets.ImagePacket;
import com.bustr.packets.SignalPacket;
import com.bustr.packets.SignalPacket.BustrSignal;
import com.bustr.utilities.BustrGrid;
import com.bustr.utilities.ResourceProvider;

public class MainActivity extends Activity implements OnClickListener {

   // Private fields -----------------------------------------------------------
   private static final String LOGTAG = "BUSTR";
   private LocationManager lm;
   private Bitmap bgImage;

   // GUI Components -----------------------------------------------------------
   private TextView btn_upload, btn_viewer, banner;
   private ImageView background;
   Typeface fontopo;

   // OnCreate -----------------------------------------------------------------
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

      // Log onCreate event for life-cycle debugging
      Log.d(LOGTAG, "OnCreate()");

      // GUI Component wiring
      btn_upload = (TextView) findViewById(R.id.btn_upload);
      btn_viewer = (TextView) findViewById(R.id.btn_viewer);
      banner = (TextView) findViewById(R.id.logo_banner);
      background = (ImageView) findViewById(R.id.main_bg_img);

      // Load type-face resources and apply
      fontopo = ResourceProvider.instance(getBaseContext()).getFont();
      banner.setTypeface(fontopo);
      btn_upload.setTypeface(fontopo);
      btn_viewer.setTypeface(fontopo);

      // Register views that listen for clicks
      btn_upload.setOnClickListener(this);
      btn_viewer.setOnClickListener(this);

      if (lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
         new BgGetter().execute();
      }
   }

   // onClick event handler ----------------------------------------------------
   @SuppressLint("InlinedApi")
   @Override
   public void onClick(View view) {
      Vibrator vib = (Vibrator) getBaseContext().getSystemService(
            Context.VIBRATOR_SERVICE);
      vib.vibrate(60);
      view.setSelected(true);
      switch (view.getId()) {
      case R.id.btn_upload:
         startActivity(new Intent(this, CameraActivity.class));
         break;
      case R.id.btn_viewer:
         startActivity(new Intent(MainActivity.this, ViewerActivity.class));
         break;
      }      
   }

   @Override
   protected void onResume() {
      super.onResume();
      btn_upload.setSelected(false);
      btn_viewer.setSelected(false);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {

      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
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

   private class BgGetter extends AsyncTask<Void, Void, ImagePacket> {

      private Socket socket;
      private ObjectInputStream input;
      private ObjectOutputStream output;

      @Override
      protected ImagePacket doInBackground(Void... arg0) {
         try {
            socket = new Socket(InetAddress.getByName("50.173.32.127"), 8000);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            SignalPacket imgReq = new SignalPacket(BustrSignal.TOP_PIC);
            imgReq.setLat(BustrGrid.gridLat(lm));
            imgReq.setLng(BustrGrid.gridLon(lm));
            output.writeObject(imgReq);
            ImagePacket imagePacket = (ImagePacket) input.readObject();
            output.close();
            input.close();
            socket.close();
            return imagePacket;
         } catch (UnknownHostException e) {
            e.printStackTrace();
         } catch (IOException e) {
            Log.e(LOGTAG, "You dropped the connection!!!");
            e.printStackTrace();
         } catch (ClassNotFoundException e) {
            e.printStackTrace();
         }
         return null;
      }

      @Override
      protected void onPostExecute(ImagePacket result) {
         super.onPostExecute(result);
         if (result != null) {
            bgImage = BitmapFactory.decodeByteArray(result.getData(), 0,
                  result.getData().length);
            background.setImageBitmap(bgImage);
            background.setVisibility(View.VISIBLE);
            background.startAnimation(AnimationUtils.loadAnimation(
                  MainActivity.this, android.R.anim.fade_in));
         }
      }

   }

}
