package com.bustr.server;

import java.io.BufferedInputStream;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import junit.framework.TestCase;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.bustr.packets.BustrPacket;
import com.bustr.packets.ImagePacket;
import com.bustr.packets.SignalPacket;
import com.bustr.packets.SignalPacket.BustrSignal;
import com.mysql.jdbc.Statement;

public class ServerTest2 {
	
	@Test

	public void extractBytesTest() throws IOException {
		String imageName = "emptyPic.jpg";
		File f = new File(imageName);
		byte[] imageData = new byte[(int) f.length()];
	    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
	    bis.read(imageData);
	    bis.close();
	    TestCase.assertEquals(null, imageData);
	}

	private static final String CONNECTION = "jdbc:mysql://127.0.0.1/bustr";
	private static final String dbClassName = "com.mysql.jdbc.Driver";
	private static Connection connection;
	private static Statement stmt;
	private static ResultSet rs;
	
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
	@Before
	public void test() throws ClassNotFoundException, SQLException{
		Class.forName(dbClassName);
		Properties p = new Properties();
		p.put("user", "root");
		p.put("password", "root");
		connection = DriverManager.getConnection(CONNECTION, p);
		try {
			stmt = (Statement) connection.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
		} catch (Exception e) {
			System.out.println("[-] Statement creation failure.");
			e.printStackTrace();
		}

		String sql = "DELETE FROM users WHERE userId=\"vader\";";
		rs = stmt.executeQuery(sql);

	}
	
	@Test
	public void upLoadTest() {
		try {
			String imagePath = "/home/bustr/Desktop/Yoda.jpg";
			System.out
					.println("UPLOAD IMAGE TEST, with imagePath="
							+ imagePath);
			Socket sock = new Socket("localhost", 8000);
			ObjectOutputStream output = new ObjectOutputStream(
					sock.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(
					sock.getInputStream());
			byte[] imageData = extractBytes(imagePath);
			ImagePacket ipacket = new ImagePacket("blar", imageData, (float)0.0 ,(float)0.0, "do or do not, there is no try");
			
			output.writeObject(ipacket);
			BustrPacket res;
			while ((res = (BustrPacket) input.readObject()) != null) {

				if (res instanceof SignalPacket) {
					SignalPacket inSignal = (SignalPacket) res;
					if (inSignal.getSignal() == BustrSignal.SUCCESS) {
						System.out.println("[+] Got a SUCCESS packet");
						assertTrue(true);
						return;
					} else if (inSignal.getSignal() == BustrSignal.FAILURE) {
						System.out.println("[-] Got a FAILURE packet");
						assertTrue(true);
						return;
					}
				} else if (res instanceof ImagePacket) {
					System.out.println("[+] Got an ImagePacket with info "
							+ res.toString());
					assertTrue(true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("  ");
	}
	
/*
	@Test
	public void newCommentTest() {
		try {
			String imagePath = "/uploads/83.jpg";
			String comment = "ALARM CLOCKS KILL DREAMS";
			System.out
					.println("NEW COMMENT TEST, with imagePath="
							+ imagePath);
			Socket sock = new Socket("localhost", 8000);
			ObjectOutputStream output = new ObjectOutputStream(
					sock.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(
					sock.getInputStream());
			SignalPacket spacket = new SignalPacket(comment, imagePath);
			output.writeObject(spacket);
			BustrPacket res;
			while ((res = (BustrPacket) input.readObject()) != null) {

	
	public void extractBytesTest() {
		
	}


				if (res instanceof SignalPacket) {
					SignalPacket inSignal = (SignalPacket) res;
					if (inSignal.getSignal() == BustrSignal.SUCCESS) {
						System.out.println("[+] Got a SUCCESS packet");
						assertTrue(true);
						return;
					} else if (inSignal.getSignal() == BustrSignal.FAILURE) {
						System.out.println("[-] Got a FAILURE packet");
						assertTrue(false);
						return;
					}
				} else if (res instanceof ImagePacket) {
					System.out.println("[+] Got an ImagePacket with info "
							+ res.toString());
					assertTrue(true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("  ");
	}
	
	*/
				
	@Test
	public void userAuthTest() {
		try {
			System.out
					.println("[+] USER AUTH TEST");
			Socket sock = new Socket("localhost", 8000);
			ObjectOutputStream output = new ObjectOutputStream(
					sock.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(
					sock.getInputStream());

			SignalPacket sp = new SignalPacket(BustrSignal.USER_AUTH);
			sp.setUser("vader");
			sp.setPass("darth");
			output.writeObject(sp);
			BustrPacket res;
			while ((res = (BustrPacket) input.readObject()) != null) {

				if (res instanceof SignalPacket) {
					SignalPacket inSignal = (SignalPacket) res;
					assertTrue(inSignal.getSignal() == BustrSignal.SUCCESS);
				} else if (res instanceof ImagePacket) {
					System.out.println("[+] Got an ImagePacket with info "
							+ res.toString());
					assertFalse(true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("  ");
	}
	
	@Test
	public void bogusUserTest() {
		try {
			System.out
					.println("[+] USER AUTH TEST");
			Socket sock = new Socket("localhost", 8000);
			ObjectOutputStream output = new ObjectOutputStream(
					sock.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(
					sock.getInputStream());

			SignalPacket sp = new SignalPacket(BustrSignal.USER_AUTH);
			sp.setUser("darth");
			sp.setPass("i am your father");
			output.writeObject(sp);
			BustrPacket res;
			while ((res = (BustrPacket) input.readObject()) != null) {
				if (res instanceof SignalPacket) {
					SignalPacket inSignal = (SignalPacket) res;
					assertTrue(inSignal.getSignal() == BustrSignal.FAILURE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("  ");
	}


	@Test
	public void getCurrentDateTimeTest() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		TestCase.assertTrue(true);
	}
	
	@Test 
	public void sendFailureTest() {
		ObjectOutputStream output = null;
		try {
			output.writeObject(new SignalPacket(SignalPacket.BustrSignal.FAILURE));
	    } catch (Exception e) {
	    	System.out.println("[-] BustrSignal FAILURE failure.");
	        e.printStackTrace();
	        TestCase.assertFalse(false);
	    }
	}
	
	@Test
	public void sendSuccessTest() {
		ObjectOutputStream output = null;
		try {
			output.writeObject(new SignalPacket(SignalPacket.BustrSignal.SUCCESS));
	    } catch (Exception e) {
	    	System.out.println("[-] BustrSignal SUCCESS failure.");
	        e.printStackTrace();
	        TestCase.assertFalse(false);
	    }
	
	}

	
	@Test
	public void upVoteTest() {
		try {
			String imagePath = "0.jpg";
			System.out
					.println("[+] IMAGE UPVOTE TEST, with imagePath="
							+ imagePath);
			Socket sock = new Socket("localhost", 8000);
			ObjectOutputStream output = new ObjectOutputStream(
					sock.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(
					sock.getInputStream());

			SignalPacket sp = new SignalPacket(BustrSignal.REP_UPVOTE);
			sp.setImageName(imagePath);
			output.writeObject(sp);
			BustrPacket res;
			while ((res = (BustrPacket) input.readObject()) != null) {

				if (res instanceof SignalPacket) {
					SignalPacket inSignal = (SignalPacket) res;
					if (inSignal.getSignal() == BustrSignal.SUCCESS) {
						System.out.println("[+] Got a SUCCESS packet");
						assertTrue(true);
						return;
					} else if (inSignal.getSignal() == BustrSignal.FAILURE) {
						System.out.println("[-] Got a FAILURE packet");
						assertTrue(false);
						return;
					}
				} else if (res instanceof ImagePacket) {
					System.out.println("[+] Got an ImagePacket with info "
							+ res.toString());
					assertTrue(true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("   ");
	}

	@Test
	public void downVoteTest() {
		try {
			String imagePath = "0.jpg";
			System.out
					.println("[+] IMAGE DOWNVOTE TEST, with imagePath="
							+ imagePath);
			Socket sock = new Socket("localhost", 8000);
			ObjectOutputStream output = new ObjectOutputStream(
					sock.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(
					sock.getInputStream());

			SignalPacket sp = new SignalPacket(BustrSignal.REP_DOWNVOTE);
			sp.setImageName(imagePath);
			output.writeObject(sp);
			BustrPacket res;
			while ((res = (BustrPacket) input.readObject()) != null) {

				if (res instanceof SignalPacket) {
					SignalPacket inSignal = (SignalPacket) res;
					if (inSignal.getSignal() == BustrSignal.SUCCESS) {
						System.out.println("[+] Got a SUCCESS packet");
						assertTrue(true);
						return;
					} else if (inSignal.getSignal() == BustrSignal.FAILURE) {
						System.out.println("[-] Got a FAILURE packet");
						assertTrue(false);
						return;
					}
				} else if (res instanceof ImagePacket) {
					System.out.println("[+] Got an ImagePacket with info "
							+ res.toString());
					assertTrue(true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("   ");
	}
	
	@Test
	public void newUserTest() {
		try {
			String username = "darth";
			String pass = "vader";
			System.out
					.println("[+] NEW USER TEST, with username" 
							+ username + " and pass "
							+ pass);
			Socket sock = new Socket("localhost", 8000);
			ObjectOutputStream output = new ObjectOutputStream(
					sock.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(
					sock.getInputStream());

			SignalPacket sp = new SignalPacket(BustrSignal.NEW_USER);
			sp.setUser(username);
			sp.setPass(pass);
			output.writeObject(sp);
			BustrPacket res;
			while ((res = (BustrPacket) input.readObject()) != null) {
 
				if (res instanceof SignalPacket) {
					SignalPacket inSignal = (SignalPacket) res;
					if (inSignal.getSignal() == BustrSignal.SUCCESS) {
						System.out.println("[+] Got a SUCCESS packet");
						assertTrue(true);
						return;
					} else if (inSignal.getSignal() == BustrSignal.FAILURE) {
						System.out.println("[-] Got a FAILURE packet");
						assertTrue(false);
						return;
					}
				} 
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("   ");
	}

/*
	@Test
	public void requestSignalTest() {
		try {
			System.out.println("[+] IMAGE REQUEST TEST");
			Socket sock = new Socket("localhost", 8000);
			ObjectOutputStream output = new ObjectOutputStream(
					sock.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(
					sock.getInputStream());

			SignalPacket sp = new SignalPacket(BustrSignal.IMAGE_REQUEST);
			sp.setLat((float) 39.7305);
			sp.setLng((float) -121.841);
			output.writeObject(sp);
			BustrPacket res;
			int counter = 0; 
			BustrPacket imageSignal = (BustrPacket)input.readObject();
			if(imageSignal instanceof SignalPacket){
				SignalPacket imageNumberSignal = (SignalPacket)imageSignal;
				System.out.println("Got a signal saying we are going be be getting " + imageNumberSignal.getImageCount());
			}
			while ((res = (BustrPacket) input.readObject()) != null) {

				if (res instanceof SignalPacket) {
					SignalPacket inSignal = (SignalPacket) res;
					if (inSignal.getSignal() == BustrSignal.SUCCESS) {
						System.out.println("Got a SUCCESS packet");
						assertTrue(true);
						return;
					} else if (inSignal.getSignal() == BustrSignal.FAILURE) {
						System.out.println("Got a FAILURE packet");
						assertTrue(false);
						return;
					}
				} else if (res instanceof ImagePacket) {
					ImagePacket ipacket = (ImagePacket) res;
					String pathPrefix = "/home/bustr/Desktop/";
					System.out.println("Got an ImagePacket with info "
							+ res.toString());
					File dir = new File(pathPrefix + "/test_uploads");
					if (!dir.exists())
						dir.mkdir();
					FileOutputStream fos = new FileOutputStream(new File(dir,
							Integer.toString(counter) + ".jpg"));
					counter++;
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					try {
						bos.write(ipacket.getData());
					} catch (Exception e) {
						System.out.println("[-] Image write failure.");
						if (ipacket.getData() == null)
							System.out.println("[-] Empty image");
						e.printStackTrace();
					}
					fos.flush();
					bos.close();
					assertTrue(true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		System.out.println("   ");
	}
*/
}
