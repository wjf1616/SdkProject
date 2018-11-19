package com.chatsdk.model.mail.gift;

import java.util.List;

public class GiftMailContents
{
	private String					mailID;
	private int						type;
	private String					createTime;
	private String					rewardId;
	private String					contents;
	private String					title;


	public String getMailID()
	{
		return mailID;
	}

	public void setMailID(String uid)
	{
		this.mailID = uid;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public String getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(String createTime)
	{
		this.createTime = createTime;
	}

	public String getRewardId()
	{
		return rewardId;
	}

	public void setRewardId(String RewardId)
	{
		this.rewardId = RewardId;
	}

	public String getContents()
	{
		return contents;
	}

	public void setContents(String Contents)
	{
		this.contents = Contents;
	}
	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}
}
