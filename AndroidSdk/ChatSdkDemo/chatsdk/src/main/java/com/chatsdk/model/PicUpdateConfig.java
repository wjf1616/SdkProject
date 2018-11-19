package com.chatsdk.model;

import java.util.List;

public class PicUpdateConfig
{
	private int version;
	private List<String> update;
	public int getVersion()
	{
		return version;
	}
	public void setVersion(int version)
	{
		this.version = version;
	}
	public List<String> getUpdate()
	{
		return update;
	}
	public void setUpdate(List<String> update)
	{
		this.update = update;
	}
	
	
}
