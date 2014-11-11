package com.bustr.packets;

import java.util.Vector;

import com.bustr.helpers.Comment;

public class SignalPacket extends BustrPacket {

	/**
    * 
    */
	public enum BustrSignal {
		FAILURE, SUCCESS, IMAGE_LIST_REQUEST, IMAGE_REQUEST, REP_UPVOTE, REP_DOWNVOTE, IMAGE_LIST, NEW_USER, USER_AUTH, NEW_COMMENT;
	}

	private static final long serialVersionUID = 1L;
	private BustrSignal signal;
	private float lat, lng;
	private String imageName;
	private String user, pass;
	private Vector<String> imageList;
	private Comment comment;

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public Vector<String> getImageList() {
		return imageList;
	}

	public void setImageList(Vector<String> pImageList) {
		this.imageList = pImageList;
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

	public SignalPacket(Comment newComment, String pImagePath) {
		signal = BustrSignal.NEW_COMMENT;
		comment = newComment;
		imageName = pImagePath;
	}
	
	public SignalPacket(Vector<String> pImageList) {
		signal = BustrSignal.IMAGE_LIST;
		imageList = pImageList;
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
