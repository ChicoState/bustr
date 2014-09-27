package com.bustr.packets;

public class SignalPacket extends BustrPacket {

   /**
    * 
    */
   public enum BustrSignal {
      FAILURE, SUCCESS, IMAGE_REQUEST
   }
 
   private static final long serialVersionUID = 1L;
   private BustrSignal signal;
   private float lat, lng;
   
   public float getLat() {
      return lat;
   }

   public void setLat(float lat) {
      this.lat = lat;
   }

   public float getLng() {
      return lng;
   }

   public void setLng(float lng) {
      this.lng = lng;
   }   
   
   public SignalPacket(BustrSignal pSignal) {
      signal = pSignal;
   }

   public SignalPacket(BustrSignal pSignal, float pLat, float pLng){
      signal = pSignal;
      lat = pLat;
      lng = pLng;
   }

   public BustrSignal getSignal() {
      return signal;
   }

}
