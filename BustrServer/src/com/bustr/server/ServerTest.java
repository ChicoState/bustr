package com.bustr.server;

import static org.junit.Assert.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import com.bustr.packets.BustrPacket;
import com.bustr.packets.ImagePacket;
import com.bustr.packets.SignalPacket;
import com.bustr.packets.SignalPacket.BustrSignal;

import org.junit.Test;

public class ServerTest {

	
	
	@Test
	public void upVoteTest() {
		try {
			String imagePath = "10.jpg";
			System.out.println("[+] Sending upvote packet to server, with imagePath="+imagePath);
			Socket sock = new Socket("localhost", 8000);
			ObjectOutputStream output = new ObjectOutputStream(sock.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(sock.getInputStream());
			
			SignalPacket sp = new SignalPacket(BustrSignal.REP_UPVOTE);
			sp.setImageName(imagePath);
			output.writeObject(sp);
			BustrPacket res;
			while((res = (BustrPacket)input.readObject()) != null){
			
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
			ObjectOutputStream output = new ObjectOutputStream(sock.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(sock.getInputStream());
			
			SignalPacket sp = new SignalPacket(BustrSignal.IMAGE_REQUEST);
			sp.setLat((float) 39.7276);
			sp.setLng((float)-121.855);
			output.writeObject(sp);
			BustrPacket res;
			while((res = (BustrPacket)input.readObject()) != null){
			
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
					System.out.println("Got an ImagePacket with info "
							+ res.toString());
					assertTrue(true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

}








