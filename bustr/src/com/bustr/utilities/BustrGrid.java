package com.bustr.utilities;

import android.location.Location;
import android.location.LocationManager;

public class BustrGrid {
   
   public static float gridLat(LocationManager lm) {
      double lat = exactLat(lm);
      return (float) Math.round(lat * 10000) / 10000f;
   }

   public static float gridLon(LocationManager lm) {
      double lon = exactLon(lm);
      return (float) Math.round(lon * 10000) / 10000f;
   }

   public static double exactLat(LocationManager lm) {
      final Location loc;
      loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      return loc.getLatitude();
   }

   public static double exactLon(LocationManager lm) {
      final Location loc;
      loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      return loc.getLongitude();
   }
   
}
