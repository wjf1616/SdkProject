package com.chatsdk.model.mail.battle;

public class BuffParams
{
	private int		buffID;
	private int		cdTimeType;
	private String	startTime;
	private String	endTime;
	private String	attr;


	public int getBuffID() {
		return buffID;
	}

	public void setBuffID(int buffID) {
		this.buffID = buffID;
	}

	public int getCdTimeType() {
		return cdTimeType;
	}

	public void setCdTimeType(int cdTimeType) {
		this.cdTimeType = cdTimeType;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getAttr() {
		return attr;
	}

	public void setAttr(String attr) {
		this.attr = attr;
	}

}
