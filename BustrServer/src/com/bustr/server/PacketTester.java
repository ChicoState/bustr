package com.bustr.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import com.bustr.packets.BustrPacket;
import com.bustr.packets.ImagePacket;
import com.bustr.packets.SignalPacket;
import com.bustr.packets.SignalPacket.BustrSignal;

public class PacketTester {
	public PacketTester() {
	}
	
	public void main(){
		PacketTester pt = new PacketTester();
		pt.sendRequest();
		
	}
	
	public void sendRequest(){
		try {
			Socket sock = new Socket("localhost", 8000);
			ObjectOutputStream output = new ObjectOutputStream(sock.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(sock.getInputStream());
			
			SignalPacket sp = new SignalPacket(BustrSignal.IMAGE_REQUEST);
			sp.setLat((float) 39.7276);
			sp.setLng((float)-121.855);
			output.writeObject(sp);
			BustrPacket res = (BustrPacket)input.readObject();
			if(res instanceof SignalPacket)
			{
				SignalPacket inSignal = (SignalPacket)res;
				if(inSignal.getSignal() == BustrSignal.SUCCESS)
				{
					System.out.println("Got a SUCCESS packet");
				}
				else if(inSignal.getSignal() == BustrSignal.FAILURE)
				{
					System.out.println("Got a FAILURE packet");
				}
			}
			else if(res instanceof ImagePacket)
			{
				System.out.println("Got an ImagePacket with info "+res.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
