package com.chatsdk.model;

public class FriendLatestMail
{
	private String	uid;
	private String	latestMail;
	
	public FriendLatestMail(String uid,String latestMail)
	{
		this.uid = uid;
		this.latestMail = latestMail;
	}

	public String getUid()
	{
		return uid;
	}

	public void setUid(String uid)
	{
		this.uid = uid;
	}

	public String getLatestMail()
	{
		return latestMail;
	}

	public void setLatestMail(String latestMail)
	{
		this.latestMail = latestMail;
	}

}
