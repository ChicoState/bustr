package com.bustr.utilities;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;

public class ResourceProvider {

   private static ResourceProvider self = null;
   private static Context context;
   private Typeface fontopo;
   private static final String server = "50.137.32.127";
   private static final int port = 8000;

   public static ResourceProvider instance(Context c) {
      context = c;
      if (self == null)
         self = new ResourceProvider();
      return self;
   }

   private ResourceProvider() {
      fontopo = Typeface.createFromAsset(context.getAssets(), "fontopo.ttf");
   }

   public Typeface getFont() {
      return fontopo;
   }   
   
   public String getServer() {
      return server;
   }

   public int getPort() {
      return port;
   }
   
   public SocketAddress socketAddress() {
      return new InetSocketAddress(server, port);
   }

   public Bitmap rotateBmp(Bitmap bmp) {
      Matrix mtx = new Matrix();
      mtx.postRotate(90);
      bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
            bmp.getHeight(), mtx, true);
      return bmp;
   }

   public String getDate() {
      DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
      Calendar cal = Calendar.getInstance();
      return dateFormat.format(cal.getTime());
   }
   
}
