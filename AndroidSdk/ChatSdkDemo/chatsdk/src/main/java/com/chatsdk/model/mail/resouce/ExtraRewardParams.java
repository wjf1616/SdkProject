package com.chatsdk.model.mail.resouce;

public class ExtraRewardParams
{
	private ExtraRewardValueParams	value;
	private int				type;

	public ExtraRewardValueParams getValue()
	{
		return value;
	}

	public void setValue(ExtraRewardValueParams value)
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
