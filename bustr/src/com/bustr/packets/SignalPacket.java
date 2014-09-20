package com.bustr.packets;

public class SignalPacket extends BustrPacket {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private BustrSignal signal;
   
   public SignalPacket(BustrSignal pSignal) {
      signal = pSignal;
   }
   
   public BustrSignal getSignal() {
      return signal;
   }

}
