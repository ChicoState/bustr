package com.bustr.server;

import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.junit.Test;

import com.bustr.packets.BustrPacket;
import com.bustr.packets.ImagePacket;
import com.bustr.packets.SignalPacket;
import com.bustr.packets.SignalPacket.BustrSignal;

public class ServerTest2 {
	
	@Test
	public void extractBytesTest() {
		
	}

	@Test
	public void getCurrentDateTimeTest() {
		
	}
	
	@Test 
	public void sendFailureTest() {
		
	}
	
	@Test
	public void sendSuccessTest() {
		
	}
	
	@Test
	public void upVoteTest() {
		try {
			String imagePath = "10.jpg";
			System.out
					.println("[+] Sending upvote packet to server, with imagePath="
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
	}

	@Test
	public void downVoteTest() {
		try {
			String imagePath = "11.jpg";
			System.out
					.println("[+] Sending upvote packet to server, with imagePath="
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
	}

	@Test
	public void requestSignalTest() {
		try {
			System.out.println("Sending request packet to server.");
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
			while ((res = (BustrPacket) input.readObject()) != null) {

				if (res instanceof SignalPacket) {
					SignalPacket inSignal = (SignalPacket) res;
					if (inSignal.getSignal() == BustrSignal.SUCCESS) {
						System.out.println("Got a SUCCESS packet");
						assertTrue(true);
						return;
					} else if (inSignal.getSignal() == BustrSignal.FAILURE) {
						System.out.println("Got a FAILURE packet");
						assertTrue(true);
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
	}

}
