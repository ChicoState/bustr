package com.bustr.utilities;

import java.io.Serializable;

public class ImagePacket implements Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private String name;
   private byte[] data;

   public ImagePacket(String pName, byte[] pData) {
      name = pName;
      data = pData;
   }

   public byte[] getData() {
      return data;
   }
   
   public String getName() {
      return name;
   }

}
