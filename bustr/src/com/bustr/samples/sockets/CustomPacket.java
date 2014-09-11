package com.bustr.samples.sockets;
import java.io.Serializable;


public class CustomPacket implements Serializable {
   public String message;
   public int id;
   
   public CustomPacket(int pId, String pMessage) {
      id = pId;
      message = pMessage;
   }
}
