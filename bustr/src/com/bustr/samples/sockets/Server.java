package com.bustr.samples.sockets;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

   private static ServerSocket ss;
   private static Socket socket;
   private final static int port = 800; // Arbitrary pretty much

   public static void main(String[] args) {

      try {
         ss = new ServerSocket(port);
         System.out.println("Server is listening on port " + port);
         socket = ss.accept(); // Will wait until client connects on port 780
         ObjectOutputStream output = new ObjectOutputStream(
               socket.getOutputStream());
         ObjectInputStream input = new ObjectInputStream(
               socket.getInputStream());
         CustomPacket packet = (CustomPacket)input.readObject();
         System.out.println("id: " + packet.id + ". message: " + packet.message);
         socket.close();
         ss.close();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
      }
   }
}
