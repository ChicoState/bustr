package com.bustr.activities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.bustr.utilities.BustrViewerAdapter;
import com.bustr.utilities.ResourceProvider;

public class ViewerActivity extends FragmentActivity {

   private ArrayList<Bitmap> images = new ArrayList<Bitmap>();
   private ViewPager pager;
   private BustrViewerAdapter adapter;
   private ResourceProvider resources;

   private Socket socket;
   private ObjectOutputStream output;
   private ObjectInputStream input;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_viewer);

      resources = ResourceProvider.instance(ViewerActivity.this);

      // Wire GUI elements -----------------------------------------------------
      pager = (ViewPager) findViewById(R.id.pager);

      new PrepareDownload().execute();
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

   private class PrepareDownload extends AsyncTask<Void, Void, Integer> {

      private SignalPacket imageCountPacket;

      @Override
      protected Integer doInBackground(Void... arg0) {
         try {
            socket = new Socket(InetAddress.getByName("50.173.32.127"), 8000);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            output.writeObject(new SignalPacket(BustrSignal.IMAGE_REQUEST,
                  39.7296f, -121.835f));
            imageCountPacket = (SignalPacket) input.readObject();
         } catch (UnknownHostException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         } catch (ClassNotFoundException e) {
            e.printStackTrace();
         }
         return imageCountPacket.getImageCount();
      }

      @Override
      protected void onPostExecute(Integer result) {
         super.onPostExecute(result);
         adapter = new BustrViewerAdapter(getSupportFragmentManager(), result);
         pager.setAdapter(adapter);
         new Downloader().execute();
      }
   }

   private class Downloader extends AsyncTask<Void, Bitmap, BustrSignal> {

      BustrPacket packet;
      int imageNum = 0;
      
      @Override
      protected void onPreExecute() {
         super.onPreExecute();
      }

      @Override
      protected BustrSignal doInBackground(Void... params) {
         while (true) {
            try {
               packet = (BustrPacket) input.readObject();
               if (packet instanceof ImagePacket) {
                  byte[] data = ((ImagePacket) packet).getData();
                  Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
                        data.length);
                  if (bitmap == null)
                     throw new AssertionError("Bitmap is null");
                  onProgressUpdate(bitmap);
               }
               else if (packet instanceof SignalPacket) {
                  return ((SignalPacket) packet).getSignal();
               }
            } catch (OptionalDataException e) {
               e.printStackTrace();
            } catch (ClassNotFoundException e) {
               e.printStackTrace();
            } catch (IOException e) {
               e.printStackTrace();
            } catch (AssertionError ae) {
               ae.printStackTrace();
            }
         }
      }

      @Override
      protected void onProgressUpdate(Bitmap... values) {
         final Bitmap bmp = values[0];
         super.onProgressUpdate(values);
         runOnUiThread(new Runnable(){
            @Override
            public void run() {
               adapter.setImage(imageNum++, bmp);
               adapter.notifyDataSetChanged(); 
            }            
         });
      }

      @Override
      protected void onPostExecute(BustrSignal result) {
         super.onPostExecute(result);
         if (result == BustrSignal.SUCCESS) {
            Toast.makeText(ViewerActivity.this,
                  "Downloaded " + images.size() + "images.", Toast.LENGTH_LONG)
                  .show();
         }
         try {
            socket.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }
}
