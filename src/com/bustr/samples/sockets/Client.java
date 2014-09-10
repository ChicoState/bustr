package com.bustr.samples.sockets;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

   private static Socket socket;
   private static final int port = 800;

   public static void main(String[] args) {
      try {
         socket = new Socket(InetAddress.getByName("localhost"), port);
         ObjectOutputStream output = new ObjectOutputStream(
               socket.getOutputStream());
         ObjectInputStream input = new ObjectInputStream(
               socket.getInputStream());
         output.writeObject(new CustomPacket(42, "hello world"));
         System.out.println("Client sent packet.");
         socket.close();         
      } catch (UnknownHostException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

}
