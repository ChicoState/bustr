package com.bustr.utilities;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class BustrGrid {

   public static float gridLat(LocationManager locMgr) {
      double lat = exactLat(locMgr);
      return (float) Math.round(lat * 10000) / 10000f;
   }

   public static float gridLon(LocationManager locMgr) {
      double lon = exactLon(locMgr);
      return (float) Math.round(lon * 10000) / 10000f;
   }

   public static double exactLat(LocationManager locMgr) {
      final Location loc;
      loc = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      return loc.getLatitude();
   }

   public static double exactLon(LocationManager locMgr) {
      final Location loc;
      loc = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      return loc.getLongitude();
   }
}
