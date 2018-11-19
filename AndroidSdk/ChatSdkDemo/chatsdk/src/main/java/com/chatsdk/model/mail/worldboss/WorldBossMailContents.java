package com.chatsdk.model.mail.worldboss;

import java.util.List;

import com.chatsdk.model.mail.monster.DefParams;

public class WorldBossMailContents
{
	private DefParams			def;
	private List<AttListParams>	attList;
	private int					xy;

	public DefParams getDef()
	{
		return def;
	}

	public void setDef(DefParams def)
	{
		this.def = def;
	}

	public List<AttListParams> getAttList()
	{
		return attList;
	}

	public void setAttList(List<AttListParams> attList)
	{
		this.attList = attList;
	}

	public int getXy()
	{
		return xy;
	}

	public void setXy(int xy)
	{
		this.xy = xy;
	}
}
