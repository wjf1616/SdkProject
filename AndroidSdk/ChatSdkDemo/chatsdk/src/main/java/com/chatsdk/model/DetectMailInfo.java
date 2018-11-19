package com.chatsdk.model;

public class DetectMailInfo
{
	private String	name		= "";
	private String	mailUid		= "";
	private int		createTime	= 0;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getMailUid()
	{
		return mailUid;
	}

	public void setMailUid(String mailUid)
	{
		this.mailUid = mailUid;
	}

	public int getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(int createTime)
	{
		this.createTime = createTime;
	}

}
