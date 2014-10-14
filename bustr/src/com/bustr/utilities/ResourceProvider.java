package com.bustr.utilities;

import android.content.Context;
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

}
