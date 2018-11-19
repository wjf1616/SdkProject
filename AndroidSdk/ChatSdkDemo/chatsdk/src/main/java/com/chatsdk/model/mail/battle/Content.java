package com.chatsdk.model.mail.battle;

public class Content
{
	private String	warPoint;
	private String	npcId;
	private String	defName;
	private String	atkName;
	private int		win;

	public String getWarPoint()
	{
		return warPoint;
	}

	public void setWarPoint(String warPoint)
	{
		this.warPoint = warPoint;
	}

	public String getNpcId()
	{
		return npcId;
	}

	public void setNpcId(String npcId)
	{
		this.npcId = npcId;
	}

	public String getDefName()
	{
		return defName;
	}

	public void setDefName(String defName)
	{
		this.defName = defName;
	}

	public String getAtkName()
	{
		return atkName;
	}

	public void setAtkName(String atkName)
	{
		this.atkName = atkName;
	}

	public int getWin()
	{
		return win;
	}

	public void setWin(int win)
	{
		this.win = win;
	}
}
