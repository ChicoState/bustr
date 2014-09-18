package com.bustr.utilities;

import java.io.Serializable;

public class ImagePacket implements Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private String name;
   private byte[] data;
   private float lat, lng;

   public ImagePacket(String pName, byte[] pData, float pLat, float pLng) {
      name = pName;
      data = pData;
      lat = pLat;
      lng = pLng;
   }

   public byte[] getData() {
      return data;
   }
   
   public String getName() {
      return name;
   }
   
   public float getLat() {
      return lat;      
   }
   
   public float getLng() {
      return lng;
   }

}
