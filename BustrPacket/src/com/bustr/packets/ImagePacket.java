package com.bustr.packets;

public class ImagePacket extends BustrPacket {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private String name;
   private byte[] data;
   private float lat, lng;
   private String caption;
   private int rep = 0;

   public ImagePacket(String pName, byte[] pData, float pLat, float pLng,
         String pCaption) {
      name = pName;
      data = pData;
      lat = pLat;
      lng = pLng;
      caption = pCaption;
   }

   public ImagePacket(String pName, byte[] pData, float pLat, float pLng,
         String pCaption, int pRep) {
      name = pName;
      data = pData;
      lat = pLat;
      lng = pLng;
      caption = pCaption;
      rep = pRep;
   }

   @Override
   public String toString() {
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

   public int getRep() {
      return rep;
   }

}
