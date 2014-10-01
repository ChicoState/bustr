package com.bustr.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.bustr.packets.BustrPacket;
import com.bustr.packets.ImagePacket;
import com.bustr.packets.SignalPacket;
import com.bustr.packets.SignalPacket.BustrSignal;

public class Server {

   private final int port = 8000;
   private ServerSocket ss;
   private Socket socket;
   private static final String CONNECTION = "jdbc:mysql://127.0.0.1/bustr";
   private static final String dbClassName = "com.mysql.jdbc.Driver";
   private static final String pathPrefix = "/home/bustr/Desktop/";
   private static final float epsilon = 0.0005f;
   private static Connection connection;
   private Statement stmt;
   private ResultSet rs;
   private static int imageNum = 0;

   public Server() throws ClassNotFoundException, SQLException {

      for (int i = 0; new File("uploads/" + i + ".jpg").isFile(); i++) {
         imageNum = i + 1;
      }

      System.out.printf("Connecting to database\n");

      Class.forName(dbClassName);
      Properties p = new Properties();
      p.put("user", "root");
      p.put("password", "root");
      connection = DriverManager.getConnection(CONNECTION, p);

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

   private byte[] extractBytes(String ImageName) throws IOException {
      // open image
      File f = new File(ImageName);
      byte[] imageData = new byte[(int) f.length()];
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
      bis.read(imageData);
      bis.close();
      if(imageData == null) System.out.println("[-] Attempting to send packet with empty data.");
      return imageData;
      // BufferedImaged bufferedImage = ImageIO.read(imgPath);
      // get DataBufferBytes from Raster
      // WritableRaster raster = bufferedImage.getRaster();
      // DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
      // return (data.getData());
   }

   public static void main(String[] args) throws ClassNotFoundException,
         SQLException {
      Server server = new Server();
   }

   private class Worker extends Thread {

      private Socket socket;
      private BustrPacket packet;

      public Worker(Socket pSocket) {
         socket = pSocket;
         start();
      }

      @Override
      public void run() {
         try {
            System.out.println("\n" + getCurrentDateTime());
            System.out.println("   Connected to "
                  + socket.getInetAddress().getHostName());
            ObjectOutputStream output = new ObjectOutputStream(
                  socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(
                  socket.getInputStream());
            try {
               stmt = connection.createStatement();
            } catch (Exception e) {
               System.out.println("[-] Statement creation failure.");
               e.printStackTrace();
            }
            try {
               try {
                  packet = (BustrPacket) input.readObject();
               } catch (Exception e) {
                  System.out.println("[-] Bustr Packet read error");
                  e.printStackTrace();
               }
               if (packet instanceof ImagePacket) {
                  ImagePacket ipacket = (ImagePacket) packet;
                  input.close();
                  output.close();
                  handleIncomingImage(ipacket, socket);
                  sendSuccess(output);

               } else if (packet instanceof SignalPacket) {

                  ImagePacket outpacket = null;
                  SignalPacket spacket = (SignalPacket) packet;
                  System.out.println("   Recieved image request from "
                        + spacket.getLat() + ", " + spacket.getLng());

                  if (spacket.getSignal() == BustrSignal.IMAGE_REQUEST) {
                     handleImageRequest(spacket, output);
                  } else if (spacket.getSignal() == BustrSignal.REP_UPVOTE) {
                     handleUpvote(spacket, output);
                  } else if (spacket.getSignal() == BustrSignal.REP_DOWNVOTE) {
                     handleDownvote(spacket, output);
                  } else {
                     System.out.println("[-] Unrecognized signal type");
                     sendFailure(output);
                  }
               } else {
                  System.out
                        .println("YARR MATIE THAT BE AN UNRECOGNIZED PACKET TYPE: FATAL SHIVER ME TIMBERS ERROR");
                  sendFailure(output);
               }
            } catch (Exception e) {
               System.out.println("NETWORK READ ERROR");
               System.out.println(e.toString());
            }

         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   private void handleDownvote(SignalPacket spacket, ObjectOutputStream output) {
      System.out.println("   Downvoting " + pathPrefix
            + spacket.getImageName());
      String sql = "UPDATE imageData SET rep = (rep - 1) WHERE imagePath="
            + "\"uploads/" + spacket.getImageName() + "\";";
      try {
         System.out.println("   Executing query: " + sql);
         stmt.executeUpdate(sql);
      } catch (Exception e) {
         System.out.println("   [-] Failed to execute query: " + sql);
         sendFailure(output);
         e.printStackTrace();
      }
      sendSuccess(output);
   }

   private void handleUpvote(SignalPacket spacket, ObjectOutputStream output) {
      System.out.println("   Upvoting " + pathPrefix + "uploads/"
            + spacket.getImageName());
      String sql = "UPDATE imageData SET rep = (rep + 1) WHERE imagePath="
            + "\"uploads/" + spacket.getImageName() + "\";";
      try {
         System.out.println("   Executing query: " + sql);
         stmt.executeUpdate(sql);
      } catch (Exception e) {
         System.out.println("   [-] Failed to execute query: " + sql);
         sendFailure(output);
         e.printStackTrace();
      }
      sendSuccess(output);
   }

   private void handleImageRequest(SignalPacket spacket,
         ObjectOutputStream output) throws SQLException {

      ImagePacket outpacket = null;
      String sql = "SELECT * FROM imageData WHERE lat BETWEEN ROUND("
            + Float.toString(spacket.getLat() - epsilon) + ", 4) AND ROUND("
            + Float.toString(spacket.getLat() + epsilon)
            + ", 4) AND lng BETWEEN ROUND("
            + Float.toString(spacket.getLng() - epsilon) + ", 4) AND ROUND("
            + Float.toString(spacket.getLng() + epsilon) + ",4) ORDER BY rep;";
      System.out.println("   Sending stmt to db");
      System.out.println("       " + sql);

      try {
         rs = stmt.executeQuery(sql);
      } catch (Exception e) {
         System.out.println("   [-] Failure when executing query: " + sql);
         e.printStackTrace();
      }
      for (int i = 0; rs.next(); i++) {
         System.out.println("\n   Getting ready to send image response #"
               + Integer.toString(i));
         String commentPath = pathPrefix + rs.getString("commentPath");
         String imagePath = pathPrefix + rs.getString("imagePath");
         String userName = rs.getString("userName");
         Float lat = rs.getFloat("Lat");
         Float lng = rs.getFloat("Lng");
         String caption = null;
         byte[] data = null;
         try {
            data = extractBytes(imagePath);
         } catch (Exception e) {
            System.out
                  .println("[-] Failed to retrieve image from /home/bustr/Desktop/"
                        + imagePath);
         }

         try {
            BufferedReader br = new BufferedReader(new FileReader(new File(
                  commentPath)));
            caption = br.readLine();
            br.close();
         } catch (Exception e) {
            System.out
                  .println("[-] Failed to retrice comment file from /home/bustr/Desktop/"
                        + commentPath);
            e.printStackTrace();
         }

         try {
            outpacket = new ImagePacket(userName, data, lat, lng, caption);
            System.out.println("   \n   Writing out ImagePacket to user");
            System.out
                  .println("   -------------------------------------------");
            System.out.println("   ###  " + userName + "  ###  "
                  + lat.toString() + ", " + lng.toString() + "  ###  "
                  + caption);
            output.writeObject(outpacket);
            System.out.println("   Done!\n");
         } catch (Exception e) {
            System.out.println("   [-] Failed to send ImagePacket");
            e.printStackTrace();
         }

      }
      sendSuccess(output);
   }

   private void handleIncomingImage(ImagePacket ipacket, Socket socket)
         throws IOException {
      File dir = new File(pathPrefix + "/uploads");
      if (!dir.exists())
         dir.mkdir();
      FileOutputStream fos = new FileOutputStream(new File(dir,
            Integer.toString(imageNum) + ".jpg"));
      BufferedOutputStream bos = new BufferedOutputStream(fos);
      try {
         bos.write(ipacket.getData());
      } catch (Exception e) {
         System.out.println("[-] Image write failure.");
         e.printStackTrace();
      }
      fos.flush();
      bos.close();
      dir = new File(pathPrefix + "/comments");
      if (!dir.exists())
         dir.mkdir();
      FileWriter fw = new FileWriter("comments/" + Integer.toString(imageNum)
            + ".txt");
      PrintWriter pal = new PrintWriter(fw);
      try {
         pal.printf("%s", ipacket.getCaption());
      } catch (Exception e) {
         System.out.println("[-] Comment file write failure.");
         e.printStackTrace();
      }
      fw.close();
      pal.close();

      String sql = "INSERT INTO imageData VALUES ( \"dummy\", ROUND("
            + ipacket.getLat() + ",4), ROUND(" + ipacket.getLng() + ",4), "
            + "\"uploads/" + imageNum + ".jpg\", 0," + "\"comments/" + imageNum
            + ".txt\", \"" + ipacket.getCaption() + "\", "
            + "CURRENT_TIMESTAMP );";

      try {
         stmt.executeUpdate(sql);
      } catch (Exception e) {
         System.out.println("Failed to execute query: " + sql);
         e.printStackTrace();
      }
      imageNum++;

      System.out.println("   Sending statement to mysql server:\n " + "    "
            + sql);
      System.out.println("   " + "Latitude: " + ipacket.getLat()
            + ", Longitude: " + ipacket.getLng());
      System.out.println("   Caption: " + ipacket.getCaption());
      socket.close();
   }

   private void sendSuccess(ObjectOutputStream output) {
      try {
         output.writeObject(new SignalPacket(SignalPacket.BustrSignal.SUCCESS));
      } catch (Exception e) {
         System.out.println("[-] BustrSignal SUCCESS failure.");
         e.printStackTrace();
      }
   }

   private void sendFailure(ObjectOutputStream output) {
      try {
         output.writeObject(new SignalPacket(SignalPacket.BustrSignal.SUCCESS));
      } catch (Exception e) {
         System.out.println("[-] BustrSignal SUCCESS failure.");
         e.printStackTrace();
      }
   }

   private String getCurrentDateTime() {

      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      Date date = new Date();
      return dateFormat.format(date);

   }

}
