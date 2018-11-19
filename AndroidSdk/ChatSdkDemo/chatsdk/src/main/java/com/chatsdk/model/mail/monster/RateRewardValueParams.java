package com.chatsdk.model.mail.monster;

public class RateRewardValueParams
{
	private int		count;
	private String	para1;
	private String	para2;
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

	public String getPara1()
	{
		return para1;
	}

	public void setPara1(String para1)
	{
		this.para1 = para1;
	}

	public String getPara2()
	{
		return para2;
	}

	public void setPara2(String para2)
	{
		this.para2 = para2;
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
