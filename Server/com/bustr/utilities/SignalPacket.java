package com.bustr.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.sql.*;
import com.bustr.packets.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;

public class Server {

   private final int port = 8000;
   private ServerSocket ss;
   private Socket socket;
   private static final String CONNECTION = "jdbc:mysql://127.0.0.1/bustr";
   private static final String dbClassName = "com.mysql.jdbc.Driver";
   private static Connection connection;
   private Statement stmt;
   private static int imageNum = 0;

   public Server() throws ClassNotFoundException,SQLException{
	   
	  System.out.printf("Connecting to database\n");
	  
	  Class.forName(dbClassName);
	  Properties p = new Properties();
	  p.put("user","root");
	  p.put("password","root");
	  connection = DriverManager.getConnection(CONNECTION,p);
	  
	   
	   
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

   public static void main(String[] args) throws ClassNotFoundException,SQLException {
      Server server = new Server();
   }

   private class Worker extends Thread {

      private Socket socket;
      private ImagePacket packet;
      
      public Worker(Socket pSocket) {
         socket = pSocket;
         start();
      }

      @Override
      public void run() {
         try {
            System.out.println("\n" + getCurrentDateTime());
            System.out.println("   Connected to " + socket.getInetAddress()
                  .getHostName());
            ObjectOutputStream output = new ObjectOutputStream(
                  socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(
                  socket.getInputStream());
            try {
               packet = (ImagePacket) input.readObject();
            } catch (Exception e) {
               System.out.println("NETWORK READ ERROR");
            }
            output.writeObject(new SignalPacket(SignalPacket.BustrSignal.SUCCESS));
            input.close();
            output.close();
            File dir = new File("uploads");
            if(!dir.exists())
               dir.mkdir();
            FileOutputStream fos = new FileOutputStream(new File(dir, Integer.toString(imageNum)+".jpg"));
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(packet.getData());
            fos.flush();
            bos.close();
            dir = new File("comments");
            if(!dir.exists())
                dir.mkdir();
            FileWriter fw = new FileWriter("comments/"+Integer.toString(imageNum)+".txt");
            PrintWriter pal = new PrintWriter(fw);
            pal.printf("%s", packet.getCaption());
            fw.close();
            pal.close();
            try{ stmt = connection.createStatement(); }
            catch (Exception e){e.printStackTrace(); }
            
            String sql = "INSERT INTO imageData VALUES (\"dummy\"," +
            				packet.getLat() +", " + packet.getLng() + 
            				", " + "\"uploads/" + imageNum +".jpg\", 0," + 
            				"\"comments/"+imageNum+".txt\", \""+packet.getCaption() + "\");";
            
            try{ stmt.executeUpdate(sql);}
            catch(Exception e) {e.printStackTrace(); }
            
            sql = "select * from imageData;";
            try{ stmt.execute(sql); }
            catch(Exception e){e.printStackTrace();}
            imageNum++;
            
            System.out.println("   Sending statement to mysql server\n "+ "    " + stmt);
            System.out.println("   '" + packet.getName() + 
                  "' written to uploads directory.");
            System.out.println("   " + "Latitude: " + packet.getLat() + 
                  ", Longitude: " + packet.getLng());
            System.out.println("   Caption: " + packet.getCaption());
            socket.close();
         } catch (IOException e) {
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
