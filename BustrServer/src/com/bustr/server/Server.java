package com.bustr.server;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
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

import javax.imageio.ImageIO;

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

	public byte[] extractBytes(String ImageName) throws IOException {
		// open image
		File imgPath = new File(ImageName);
		BufferedImage bufferedImage = ImageIO.read(imgPath);

		// get DataBufferBytes from Raster
		WritableRaster raster = bufferedImage.getRaster();
		DataBufferByte data = (DataBufferByte) raster.getDataBuffer();

		return (data.getData());
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
					packet = (BustrPacket) input.readObject();
					if (packet instanceof ImagePacket) {
						ImagePacket ipacket = (ImagePacket) packet;
						output.writeObject(new SignalPacket(
								SignalPacket.BustrSignal.SUCCESS));
						input.close();
						output.close();
						File dir = new File("/home/bustr/Desktop/uploads");
						if (!dir.exists())
							dir.mkdir();
						FileOutputStream fos = new FileOutputStream(new File(
								dir, Integer.toString(imageNum) + ".jpg"));
						BufferedOutputStream bos = new BufferedOutputStream(fos);
						bos.write(ipacket.getData());
						fos.flush();
						bos.close();
						dir = new File("/home/bustr/Desktop/comments");
						if (!dir.exists())
							dir.mkdir();
						FileWriter fw = new FileWriter("comments/"
								+ Integer.toString(imageNum) + ".txt");
						PrintWriter pal = new PrintWriter(fw);
						pal.printf("%s", ipacket.getCaption());
						fw.close();
						pal.close();
						try {
							stmt = connection.createStatement();
						} catch (Exception e) {
							e.printStackTrace();
						}

						String sql = "INSERT INTO imageData VALUES ( \"dummy\", "
								+ ipacket.getLat()
								+ ", "
								+ ipacket.getLng()
								+ ", "
								+ "\"uploads/"
								+ imageNum
								+ ".jpg\", 0,"
								+ "\"comments/"
								+ imageNum
								+ ".txt\", \""
								+ ipacket.getCaption()
								+ "\", "
								+ "CURRENT_TIMESTAMP );";

						try {
							stmt.executeUpdate(sql);
						} catch (Exception e) {
							e.printStackTrace();
						}
						imageNum++;

						System.out
								.println("   Sending statement to mysql server:\n "
										+ "    " + sql);
						System.out.println("   " + "Latitude: "
								+ ipacket.getLat() + ", Longitude: "
								+ ipacket.getLng());
						System.out.println("   Caption: "
								+ ipacket.getCaption());
						socket.close();
					} else if (packet instanceof SignalPacket) {

						float epsilon = 0.0005f;
						ImagePacket outpacket = null;
						SignalPacket spacket = (SignalPacket) packet;
						System.out.println("Recieved image request from "
								+ spacket.getLat() + ", " + spacket.getLng());
						stmt = connection.createStatement();
						if (spacket.getSignal() == BustrSignal.IMAGE_REQUEST) {
							String sql = "SELECT * FROM imageData WHERE lat BETWEEN ROUND("
									+ Float.toString(spacket.getLat() - epsilon)
									+ ", 4) AND ROUND("
									+ Float.toString(spacket.getLat() + epsilon)
									+ ", 4) AND lng BETWEEN ROUND("
									+ Float.toString(spacket.getLng() - epsilon)
									+ ", 4) AND ROUND("
									+ Float.toString(spacket.getLng() + epsilon)
									+ ",4);";
							System.out.println("Sending stmt to db");
							System.out.println("    " + sql);

							for (rs = stmt.executeQuery(sql); rs.next();) {
								String commentPath = "/home/bustr/Desktop/"
										+ rs.getString("commentPath");
								byte[] data = extractBytes("/home/bustr/Desktop/"
										+ rs.getString("imagePath"));

								BufferedReader br = new BufferedReader(
										new FileReader(new File(commentPath)));
								String caption = br.readLine();
								br.close();
								String userName = rs.getString("userName");
								Float lat = rs.getFloat("Lat");
								Float lng = rs.getFloat("Lng");

								outpacket = new ImagePacket(userName, data,
										lat, lng, caption);
								System.out
										.println("\nWriting out ImagePacket to user");
								System.out
										.println("----------------------------------");
								System.out.println("###  " + userName
										+ "  ###  " + lat.toString() + ", "
										+ lng.toString() + "  ###  " + caption);
								output.writeObject(outpacket);
								System.out.println("Done!\n");

							}
						}
					} else { 
						System.out.println("YARR MATIE THAT BE AN UNRECOGNIZED PACKET TYPE: FATAL SHIVER ME TIMBERS ERROR");
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

	public String getCurrentDateTime() {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);

	}

}
