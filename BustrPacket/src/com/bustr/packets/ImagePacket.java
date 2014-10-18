package com.bustr.packets;

import java.util.Vector;

public class ImagePacket extends BustrPacket {

	/**
    * 
    */
	private static final long serialVersionUID = 1L;
	private Vector<String> messages;
	private String userName;
	private String imageName;
	private byte[] data;
	private float lat, lng;
	private String caption; 
	private int rep = 0;

	
	public void addMessage(String s)
	{
		messages.add(s);
	}
	
	
	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public Vector<String> getMessages() {
		return messages;
	}

	public void setMessages(Vector<String> messages) {
		this.messages = messages;
	}

	public ImagePacket(String pName, byte[] pData, float pLat, float pLng,
			String pCaption) {
		userName = pName;
		data = pData;
		lat = pLat;
		lng = pLng;
		caption = pCaption;
		messages = new Vector<String>();
	}

	public ImagePacket(String pName, byte[] pData, float pLat, float pLng,
			String pCaption, int pRep, String pImageName) {
		userName = pName;
		data = pData;
		lat = pLat;
		lng = pLng;
		caption = pCaption;
		rep = pRep;
		imageName = pImageName;
	}

	@Override
	public String toString() {
		String s = userName + ": " + lat + " : " + lng + " : " + caption;
		return s;
	}

	public byte[] getData() {
		return data;
	}

	public String getName() {
		return userName;
	}

	public float getLat() {
		return lat;
	}

	public float getLng() {
		return lng;
	}

	public String getCaption() {
		return caption;
	}

	public int getRep() {
		return rep;
	}
	
	public void setImageName(String name) {
	   imageName = name;
	}
	
	public String getImageName() {
	   return imageName;
	}

}
