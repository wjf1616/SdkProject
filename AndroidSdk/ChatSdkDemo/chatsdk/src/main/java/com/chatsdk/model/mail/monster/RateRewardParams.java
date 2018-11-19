package com.chatsdk.model.mail.monster;

public class RateRewardParams
{
	private RateRewardValueParams	value;
	private int						type;

	public RateRewardValueParams getValue()
	{
		return value;
	}

	public void setValue(RateRewardValueParams value)
	{
		this.value = value;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

}
