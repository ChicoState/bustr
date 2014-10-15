package com.bustr.activities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bustr.R;
import com.bustr.packets.BustrPacket;
import com.bustr.packets.ImagePacket;
import com.bustr.packets.SignalPacket;
import com.bustr.packets.SignalPacket.BustrSignal;
import com.bustr.utilities.BustrGrid;
import com.bustr.utilities.BustrViewerAdapter;

public class ViewerActivity extends FragmentActivity {

   private ViewPager pager;
   private BustrViewerAdapter adapter;

   private Socket socket;
   private ObjectOutputStream output;
   private ObjectInputStream input;

   private LocationManager lm;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_viewer);

      lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
            new LocationListener() {
               @Override
               public void onStatusChanged(String provider, int status,
                     Bundle extras) {
               }

               @Override
               public void onProviderEnabled(String provider) {
               }

               @Override
               public void onProviderDisabled(String provider) {
               }

               @Override
               public void onLocationChanged(Location location) {
               }
            });
      // Wire GUI elements -----------------------------------------------------
      pager = (ViewPager) findViewById(R.id.pager);
      pager.setOffscreenPageLimit(5);
      new PreparePager().execute();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.viewer, menu);
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

   private class PreparePager extends AsyncTask<Void, Void, Vector<String>> {

      private SignalPacket imageCountPacket;

      @Override
      protected Vector<String> doInBackground(Void... arg0) {
         try {
            socket = new Socket(InetAddress.getByName("50.173.32.127"), 8000);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            output.writeObject(new SignalPacket(BustrSignal.IMAGE_LIST_REQUEST,
                  BustrGrid.gridLat(lm), BustrGrid.gridLon(lm)));
            imageCountPacket = (SignalPacket) input.readObject();
         } catch (UnknownHostException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         } catch (ClassNotFoundException e) {
            e.printStackTrace();
         }
         return imageCountPacket.getImageList();
      }

      @Override
      protected void onPostExecute(Vector<String> result) {
         super.onPostExecute(result);
         adapter = new BustrViewerAdapter(getSupportFragmentManager(), result);
         pager.setAdapter(adapter);
         Toast.makeText(ViewerActivity.this, result + " images found.",
               Toast.LENGTH_LONG).show();
      }
   }
}
