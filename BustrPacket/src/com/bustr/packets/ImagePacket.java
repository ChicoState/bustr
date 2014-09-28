package com.bustr.packets;

public class ImagePacket extends BustrPacket {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private String name;
   private byte[] data;
   private float  lat, lng;
   private String caption;

   public ImagePacket(String pName, byte[] pData, float pLat, float pLng,
         String pCaption) {
      name    = pName;
      data    = pData;
      lat     = pLat;
      lng     = pLng;
      caption = pCaption;
   }
   
   @Override
   public String toString(){
	   String s = name + ": " + lat + " : " + lng + " : " + caption;
	   return s;
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
   
   public String getCaption() {
      return caption;
   }

}
