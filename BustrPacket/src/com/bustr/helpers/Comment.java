package com.bustr.helpers;

import java.io.Serializable;



public class Comment implements Serializable{
   
   private String user;
   private String body;
   private String time;
   
   public Comment(String user, String time, String body) {
      this.user = user;
      this.time = time;
      this.body = body;      
   }
   
   public String getUser()
   {
	   return user;
   }
   
   public String getBody()
   {
	   return body;
   }
   
   public String getTime()
   {
	   return time;
   }
   
   @Override
   public String toString() {
      return user + "@" + time + ": " + body;
   }
}
