package com.bustr.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.bustr.utilities.ImagePacket;

public class Server {

   private final int port = 8000;
   private ServerSocket ss;
   private Socket socket;

   public Server() {

      System.out.printf("listening on port %d...\n", port);
      try {
         ss = new ServerSocket(port);
      } catch (IOException e1) {
         e1.printStackTrace();
      }
      while (true) {
         try {
            socket = ss.accept();
            Worker worker = new Worker(socket);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   public static void main(String[] args) {
      Server server = new Server();
   }

   private class Worker extends Thread {

      private Socket socket;

      public Worker(Socket pSocket) {
         socket = pSocket;
         start();
      }

      @Override
      public void run() {
         try {
            System.out.println("\n" + getCurrentDateTime());
            System.out.println("   Connected to " + socket.getInetAddress().getHostName());
            ObjectOutputStream output = new ObjectOutputStream(
                  socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(
                  socket.getInputStream());
            ImagePacket packet = (ImagePacket) input.readObject();
            File dir = new File("uploads");
            if(!dir.exists())
               dir.mkdir();
            FileOutputStream fos = new FileOutputStream(new File(dir, 
            	packet.getName()));
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(packet.getData());
            fos.flush();
            bos.close();
            System.out.println("   '" + packet.getName() + "' written to uploads directory.");
            System.out.println("   " + "Latitude: " + packet.getLat() + ", Longitude: " + packet.getLng());
            socket.close();
         } catch (IOException e) {
            e.printStackTrace();
         } catch (ClassNotFoundException e) {
            e.printStackTrace();
         }
      }
   }
   
   public String getCurrentDateTime() {
      
      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      Date date = new Date();
      return dateFormat.format(date);
      
   }

}
