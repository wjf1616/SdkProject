package com.chatsdk.model.mail.resouce;

public class ExtraRewardValueParams
{
	private int		count;
	private int		rewardAdd;
	private long	vanishTime;
	private String	uuid;
	private String	itemId;

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public int getRewardAdd()
	{
		return rewardAdd;
	}

	public void setRewardAdd(int rewardAdd)
	{
		this.rewardAdd = rewardAdd;
	}

	public long getVanishTime()
	{
		return vanishTime;
	}

	public void setVanishTime(long vanishTime)
	{
		this.vanishTime = vanishTime;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getItemId()
	{
		return itemId;
	}

	public void setItemId(String itemId)
	{
		this.itemId = itemId;
	}

}
