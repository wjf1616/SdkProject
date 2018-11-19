package com.chatsdk.model.mail.worldboss;

import com.chatsdk.model.mail.monster.AttParams;

public class AttListParams
{
	private int			picVer;
	private String		uid;
	private String		name;
	private int			leader;
	private AttParams	att;
	private String		pic;

	public int getPicVer()
	{
		return picVer;
	}

	public void setPicVer(int picVer)
	{
		this.picVer = picVer;
	}

	public String getUid()
	{
		return uid;
	}

	public void setUid(String uid)
	{
		this.uid = uid;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getLeader()
	{
		return leader;
	}

	public void setLeader(int leader)
	{
		this.leader = leader;
	}

	public AttParams getAtt()
	{
		return att;
	}

	public void setAtt(AttParams att)
	{
		this.att = att;
	}

	public String getPic()
	{
		return pic;
	}

	public void setPic(String pic)
	{
		this.pic = pic;
	}

}
