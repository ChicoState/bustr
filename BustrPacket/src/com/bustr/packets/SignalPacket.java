package com.bustr.packets;

public class SignalPacket extends BustrPacket {

	/**
    * 
    */
	public enum BustrSignal {
		FAILURE, SUCCESS, IMAGE_REQUEST, REP_UPVOTE, REP_DOWNVOTE, IMAGE_COUNT, NEW_USER, USER_AUTH
	} 

	private static final long serialVersionUID = 1L;
	private BustrSignal signal;
	private float lat, lng;
	private String imageName;
	private String user, pass;
	private int imageCount;

	public int getImageCount() {
		return imageCount;
	}

	public void setImageCount(int imageCount) {
		this.imageCount = imageCount;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public float getLat() {
		return lat;
	}

	public void setLat(float lat) {
		this.lat = lat;
	}

	public float getLng() {
		return lng;
	}

	public void setLng(float lng) {
		this.lng = lng;
	}

	public SignalPacket(BustrSignal pSignal) {
		signal = pSignal;
	}

	public SignalPacket(BustrSignal pSignal, float pLat, float pLng) {
		signal = pSignal;
		lat = pLat;
		lng = pLng;
	}

	public BustrSignal getSignal() {
		return signal;
	}

}
