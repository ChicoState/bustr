package com.bustr.packets;

public class SignalPacket extends BustrPacket {

   /**
    * 
    */
   public enum BustrSignal {
      FAILURE, SUCCESS, IMAGE_REQUEST
   }

   private static final long serialVersionUID = 1L;
   private String messageBody = null;

   private BustrSignal signal;

   public SignalPacket(BustrSignal pSignal) {
      signal = pSignal;
   }

   public SignalPacket(BustrSignal pSignal, String pMessageBody) {
      signal = pSignal;
      messageBody = pMessageBody;
   }

   public BustrSignal getSignal() {
      return signal;
   }

}
