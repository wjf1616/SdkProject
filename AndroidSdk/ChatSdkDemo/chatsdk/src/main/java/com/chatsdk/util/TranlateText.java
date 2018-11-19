package com.chatsdk.util;

public class TranlateText
{
	private String	trans	= "";
	private String	orig	= "";

	public TranlateText()
	{
	}

	@Override
	public String toString()
	{

		return "[TranlateText:]  trans: " + trans + " orig: " + orig;
	}

	public String getTrans()
	{
		return trans;
	}

	public void setTrans(String trans)
	{
		this.trans = trans;
	}

	public String getOrig()
	{
		return orig;
	}

	public void setOrig(String orig)
	{
		this.orig = orig;
	}
}
