package com.bustr.helpers;



public class Comment {
   
   private String user;
   private String body;
   private String time;
   
   public Comment(String user, String time, String body) {
      this.user = user;
      this.time = time;
      this.body = body;      
   }
   
   @Override
   public String toString() {
      return user + "@" + time + ": " + body;
   }
}
