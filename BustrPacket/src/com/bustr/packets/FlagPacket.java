package com.bustr.packets;


public class FlagPacket extends BustrPacket{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum BustrFlag {
		ILLEGAL_IMAGE, DUPLICATE_IMAGE, FANTASTIC_IMAGE, NSFW
	}
	
	private BustrFlag flag;
	
	public FlagPacket(FlagPacket.BustrFlag pFlag){
		flag = pFlag; 
	}

	public BustrFlag getFlag() {
		return flag;
	}

	public void setFlag(BustrFlag flag) {
		this.flag = flag;
	}
		
}

