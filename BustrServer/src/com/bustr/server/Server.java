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
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

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
	private static final float epsilon = 0.0001f;
	private BustrPacket packet;
	private static Connection connection;
	private static Statement stmt;
	private static ResultSet rs;
	private static int imageNum = 0;

	public Server() throws ClassNotFoundException, SQLException, IOException {

		for (int i = 0; new File("uploads/" + i + ".jpg").isFile(); i++) {
			imageNum = i + 1;
		}
		System.out.println("[=============================]");
		System.out.println("[===Welcome to Bustr Server!==]");
		System.out.println("[==We have " + imageNum + " images on file!==]\n");
		

		System.out.println("Starting memory profiler");
		Runtime runtime = Runtime.getRuntime();  

		long maxMemory = runtime.maxMemory();  
		long allocatedMemory = runtime.totalMemory();  
		long freeMemory = runtime.freeMemory();  

		System.out.println("free memory: " + freeMemory / 1024);  
		System.out.println("allocated memory: " + allocatedMemory / 1024);  
		System.out.println("max memory: " + maxMemory /1024);  
		System.out.println("total free memory: " +   
		   (freeMemory + (maxMemory - allocatedMemory)) / 1024);
		
		System.out.printf("Connecting to database\n");
		Class.forName(dbClassName);
		Properties p = new Properties();
		p.put("user", "root");
		p.put("password", "root");
		connection = DriverManager.getConnection(CONNECTION, p);
		try {
			stmt = connection.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
		} catch (Exception e) {
			System.out.println("[-] Statement creation failure.");
			e.printStackTrace();
		}

		if (!new File("private.key").isFile())
			generateKeyPair();

		System.out.printf("listening on port %d...\n", port);

		try {
			ss = new ServerSocket(port);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while (true) {
			try {
				socket = ss.accept();
				System.out.println("[+] Received a new packet");
				Worker worker = new Worker(socket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveToFile(String fileName, BigInteger mod, BigInteger exp)
			throws IOException {
		ObjectOutputStream oout = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(fileName)));
		try {
			oout.writeObject(mod);
			oout.writeObject(exp);
		} catch (Exception e) {
			throw new IOException("Unexpected error", e);
		} finally {
			oout.close();
		}
	}

	private byte[] extractBytes(String ImageName) throws IOException {
		// open image
		File f = new File(ImageName);
		byte[] imageData = new byte[(int) f.length()];
		BufferedInputStream bis = new BufferedInputStream(
				new FileInputStream(f));
		bis.read(imageData);
		bis.close();
		if (imageData == null)
			System.out
					.println("[-] Attempting to send packet with empty data.");
		return imageData;
		// BufferedImaged bufferedImage = ImageIO.read(imgPath);
		// get DataBufferBytes from Raster
		// WritableRaster raster = bufferedImage.getRaster();
		// DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
		// return (data.getData());
	}

	public static void main(String[] args) throws ClassNotFoundException,
			SQLException, IOException {
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
				System.out.println(getCurrentDateTime());
				System.out.println("   Connected to "
						+ socket.getInetAddress().getHostName());
				ObjectOutputStream output = new ObjectOutputStream(
						socket.getOutputStream());
				ObjectInputStream input = new ObjectInputStream(
						socket.getInputStream());
				try {
					try {
						System.out.print("   Getting incomming data");
						packet = (BustrPacket) input.readObject();
						System.out
								.println(" . . . . finished recieving packet");
					} catch (Exception e) {
						System.out.println("[-] Bustr Packet read error");
						e.printStackTrace();
					}
					if (packet instanceof ImagePacket) {
						ImagePacket ipacket = (ImagePacket) packet;
						System.out.println("   Handling incoming image");
						handleIncomingImage(ipacket);
						sendSuccess(output);

					} else if (packet instanceof SignalPacket) {

						SignalPacket spacket = (SignalPacket) packet;
						System.out.println("   Recieved image request from "
								+ spacket.getLat() + ", " + spacket.getLng());

						if (spacket.getSignal() == BustrSignal.IMAGE_REQUEST) {
							System.out.println("[+] IMAGE REQUEST");
							handleImageRequest(spacket, output);
						} else if (spacket.getSignal() == BustrSignal.REP_UPVOTE) {
							System.out.println("[+] UPVOTE");
							handleUpvote(spacket, output);
						} else if (spacket.getSignal() == BustrSignal.REP_DOWNVOTE) {
							System.out.println("[+] DOWNVOTE");
							handleDownvote(spacket, output);
						} else if (spacket.getSignal() == BustrSignal.NEW_USER) {
							System.out.println("[+] NEW USER");
							if(!handleNewUser(spacket, output)) System.out.println("[-] Failed new user check.");
						} else if (spacket.getSignal() == BustrSignal.USER_AUTH) {
							System.out.println("[+] USER AUTH");
							handleUserAuth(spacket, output);
						} else if (spacket.getSignal() == BustrSignal.NEW_COMMENT) {
							System.out.println("[+] NEW COMMENT");
							handleNewComment(spacket, output);
						} else if (spacket.getSignal() == BustrSignal.IMAGE_LIST_REQUEST) {
							System.out.println("[+] IMAGE LIST REQUEST");
							handleImageListRequest(spacket, output);
						} else {
							System.out.println("[-] Unrecognized signal type");
							sendFailure(output);
						}

					} else {
						System.out
								.println("YARR MATIE THAT BE AN UNRECOGNIZED PACKET TYPE: FATAL SHIVER ME TIMBERS ERROR");
						if (packet != null)
							System.out.println(packet.getClass().getName());
						else
							System.out.println("Getting a null packet");
						sendFailure(output);
					}
					System.out
							.println("   Closing socket and input output streams.");
				} catch (Exception e) {
					System.out.println("NETWORK READ ERROR");
					System.out.println(e.toString());
				} finally {
					input.close();
					output.close();
					socket.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void handleNewComment(SignalPacket spacket,
			ObjectOutputStream output) throws IOException {
		String newComment = spacket.getComment();
		if (newComment.length() == 0) {
			sendFailure(output);
			System.out.println("[-] Received an empty comment");
			return;
		}
		String user = spacket.getUser();
		String imagePath = spacket.getImageName();
		System.out.println("[+] Adding comment " + newComment + " to image "
				+ imagePath);
		File dir = new File(pathPrefix + "/uploads");
		if (!dir.exists())
			dir.mkdir();
		FileWriter fw = new FileWriter("comments/" + imagePath.substring(9, imagePath.length() - 3) + "txt", true);
		try {
			fw.append(newComment + System.getProperty("line.separator"));
		} catch (Exception e) {
			System.out.println("[-] New comment write failure with comment "
					+ newComment);
			e.printStackTrace();
		}
		fw.close();
		sendSuccess(output);

	}

	public static boolean handleUserAuth(SignalPacket spacket,
			ObjectOutputStream output) throws ClassNotFoundException,
			SQLException {
		String username = spacket.getUser();
		String password = spacket.getPass();
		System.out.println("[+] User Auth where userId=" + username + " and password="+password);
		String sql = "SELECT * FROM users WHERE userId=\"" + username
				+ "\" and userPass=\"" + password + "\";";
		try {

			rs = stmt.executeQuery(sql);
		} catch (Exception e) {
			System.out.println("[-] Failed to execute sql stmt " + sql);
			e.printStackTrace();
			connection.close();
			sendFailure(output);
			return false;
		}
		Boolean valid = false;
		for ( ; rs.next(); ) {
			valid = (rs.getString("userId").equals(username)  && rs
					.getString("userPass").equals(password) );
			System.out.println("[+] Found userID="+rs.getString("userId") + ", password=" + rs.getString("userPass"));
		}
		if (valid)
			sendSuccess(output);
		else
			sendFailure(output);
		return valid;
	}

	public static boolean handleNewUser(SignalPacket spacket,
			ObjectOutputStream output) throws SQLException,
			ClassNotFoundException {
		String username = spacket.getUser();
		String password = spacket.getPass();
		String sql = "SELECT * FROM users WHERE userId=\"" + username + "\";";
		rs = stmt.executeQuery(sql);
		if(rs.next()){
			sendFailure(output);
			return false;
		}
		sql = "INSERT INTO users VALUES ( \"" + username + "\", \""
				+ password + "\" );";

		try {
			stmt.execute(sql);
		} catch (Exception e) {
			System.out.println("[-] Failed to execute sql stmt " + sql);
			e.printStackTrace();
			connection.close();
			sendFailure(output);
			return false;
		}
		connection.close();
		sendSuccess(output);
		return true;
	}

	private void handleDownvote(SignalPacket spacket, ObjectOutputStream output) {
		System.out.println("   Downvoting " + pathPrefix
				+ spacket.getImageName());
		String sql = "UPDATE imageData SET rep = (rep - 1) WHERE imagePath=\""
				+ spacket.getImageName() + "\";";
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
		String sql = "UPDATE imageData SET rep = (rep + 1) WHERE imagePath=\""
				+ spacket.getImageName() + "\";";
		try {
			System.out.println("   Executing query: " + sql);
			stmt.executeUpdate(sql);
			sendSuccess(output);
		} catch (Exception e) {
			System.out.println("   [-] Failed to execute query: " + sql);
			sendFailure(output);
			e.printStackTrace();
		}

	}

	private void handleImageListRequest(SignalPacket spacket,
			ObjectOutputStream output) throws SQLException {
		System.out.println("[+] Handling image list request");
		SignalPacket outpacket = null;
		Vector<String> imageList = new Vector<String>();
		String sql = "SELECT * FROM imageData WHERE lat BETWEEN ROUND("
				+ Float.toString(spacket.getLat() - epsilon)
				+ ", 4) AND ROUND("
				+ Float.toString(spacket.getLat() + epsilon)
				+ ", 4) AND lng BETWEEN ROUND("
				+ Float.toString(spacket.getLng() - epsilon)
				+ ", 4) AND ROUND("
				+ Float.toString(spacket.getLng() + epsilon)
				+ ",4) ORDER BY rep DESC;";
		System.out.println("   Sending stmt to db");
		System.out.println("       " + sql);
		try {
			rs = stmt.executeQuery(sql);
		} catch (Exception e) {
			System.out.println("   [-] Failure when executing query: " + sql);
			e.printStackTrace();
		}
		for (int i = 1; rs.next(); i++) {
			System.out.println("   Getting ready to send image "
					+ rs.getString("imagePath"));
			try {
				imageList.add(rs.getString("imagePath"));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		outpacket = new SignalPacket(imageList);
		try {
			output.writeObject(outpacket);
		} catch (IOException e) {
			System.out.println("[-] Failed to send imageList Signal Packet");
			e.printStackTrace();
		}

	}

	private void handleImageRequest(SignalPacket spacket,
			ObjectOutputStream output) throws SQLException, IOException {
		System.out.println("[+] Handling image request for image "
				+ spacket.getImageName());
		ImagePacket outpacket = null;
		Vector<String> outMessages = null;
		String imagePath = "default";
		String sql = "SELECT * FROM imageData WHERE imagePath=\""
				+ spacket.getImageName() + "\"; ";
		System.out.println("   Sending stmt to db");
		System.out.println("       " + sql);

		try {
			rs = stmt.executeQuery(sql);
		} catch (Exception e) {
			System.out.println("   [-] Failure when executing query: " + sql);
			e.printStackTrace();
		}

		if (rs.next()) {
			String commentPath = rs.getString("commentPath");
			imagePath = rs.getString("imagePath");
			String userName = rs.getString("userName");
			Float lat = rs.getFloat("Lat");
			Float lng = rs.getFloat("Lng");
			int rep = rs.getInt("rep");
			String caption = null;
			byte[] data = null;
			try {
				data = extractBytes(imagePath);
			} catch (Exception e) {
				System.out.println("[-] Failed to retrieve image from "
						+ imagePath);
			}

			try {
				BufferedReader br = new BufferedReader(new FileReader(new File(
						commentPath)));
				caption = br.readLine();
				outMessages = new Vector<String>();
				String s = br.readLine();
				outMessages.add(s);
				/*
				for (String s = br.readLine(); s != null; br.readLine())
					outMessages.add(s);
				*/
				br.close();
			} catch (Exception e) {
				System.out.println("[-] Failed to retrice comment file from "
						+ commentPath);
				e.printStackTrace();
			}

			try {
				outpacket = new ImagePacket(userName, data, lat, lng, caption,
						rep, imagePath);
				if (outMessages != null)
					outpacket.setMessages(outMessages);
				else
					System.out.println("[-] Failed to add messages");
				System.out.println("   Writing out ImagePacket to user");
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
		} else {
			System.out
					.println("[-] Failed to send image with name" + imagePath);
		}
	}

	private void handleIncomingImage(ImagePacket ipacket) throws IOException {
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
			fw.append(System.getProperty("line.separator"));
		} catch (Exception e) {
			System.out.println("[-] Comment file write failure.");
			e.printStackTrace();
		}
		fw.close();
		pal.close();

		String sql = "INSERT INTO imageData VALUES ( \"dummy\", ROUND("
				+ ipacket.getLat() + ",4), ROUND(" + ipacket.getLng() + ",4), "
				+ "\"uploads/" + imageNum + ".jpg\", 0," + "\"comments/"
				+ imageNum + ".txt\", \"" + ipacket.getCaption() + "\", "
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
	}

	private static void sendSuccess(ObjectOutputStream output) {
		try {
			output.writeObject(new SignalPacket(
					SignalPacket.BustrSignal.SUCCESS));
			System.out.println("[+] Sent SUCCESS");
		} catch (Exception e) {
			System.out.println("[-] Failed to send SUCCESS");
			e.printStackTrace();
		} finally {
			try {
				// output.flush();
			} catch (Exception e) {
				System.out.println("[-] Failed to flush outputstream");
				e.printStackTrace();
			}
		}
	}

	private static void sendFailure(ObjectOutputStream output) {
		try {
			output.writeObject(new SignalPacket(
					SignalPacket.BustrSignal.FAILURE));
			System.out.println("[+] Sent FAILURE");
		} catch (Exception e) {
			System.out.println("[-] failed to send  FAILURE.");
			e.printStackTrace();
		} finally {
			try {
				// output.flush();
			} catch (Exception e) {
				System.out.println("[-] Failed to send FAILURE");
				e.printStackTrace();
			}
		}
	}

	private String getCurrentDateTime() {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);

	}

	private void generateKeyPair() throws IOException {
		System.out.println("[+] Generating new public-private keypair");
		KeyPairGenerator kpg = null;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		kpg.initialize(2048);
		KeyPair kp = kpg.genKeyPair();
		Key publicKey = kp.getPublic();
		Key privateKey = kp.getPrivate();

		KeyFactory fact = null;
		try {
			fact = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		RSAPublicKeySpec pub = null;
		try {
			pub = fact.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
		} catch (InvalidKeySpecException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		RSAPrivateKeySpec priv = null;
		try {
			priv = fact.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);
		} catch (InvalidKeySpecException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		saveToFile("public.key", pub.getModulus(), pub.getPublicExponent());
		saveToFile("private.key", priv.getModulus(), priv.getPrivateExponent());

	}

}
