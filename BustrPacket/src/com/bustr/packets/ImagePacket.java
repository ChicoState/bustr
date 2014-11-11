package com.bustr.packets;

import java.util.Vector;

import com.bustr.helpers.Comment;

public class ImagePacket extends BustrPacket {

   public enum VoteState { NONE, UP, DOWN };
   
	private static final long serialVersionUID = 1L;
	private Vector<Comment> messages;
	private String userName;
	private String imageName;
	private byte[] data;
	private float lat, lng;
	private String caption; 
	private int rep = 0;
	private VoteState voteState = VoteState.NONE;

	
	public void addMessage(Comment c)
	{
		messages.add(c);
	}
	
	
	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public Vector<Comment> getMessages() {
		return messages;
	}

	public void setMessages(Vector<Comment> messages) {
		this.messages = messages;
	}

	public ImagePacket(String pName, byte[] pData, float pLat, float pLng,
			String pCaption) {
		userName = pName;
		data = pData;
		lat = pLat;
		lng = pLng;
		caption = pCaption;
		messages = new Vector<Comment>();
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

   public VoteState getVoteState() {
      return voteState;
   }

   public void setVoteState(VoteState voteState) {
      this.voteState = voteState;
   }
}
