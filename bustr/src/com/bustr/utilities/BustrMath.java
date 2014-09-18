package com.bustr.utilities;

public class BustrMath {

   public static float gridDimension(double gpsDimension) {
      
      return (float)Math.round(gpsDimension*10000)/10000f;
      
   }
   
}
