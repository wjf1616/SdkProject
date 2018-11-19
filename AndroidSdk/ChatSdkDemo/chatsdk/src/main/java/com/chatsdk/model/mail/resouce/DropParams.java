package com.chatsdk.model.mail.resouce;

public class DropParams
{
	private DropValueParams	value;
	private int				type;

	public DropValueParams getValue()
	{
		return value;
	}

	public void setValue(DropValueParams value)
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
