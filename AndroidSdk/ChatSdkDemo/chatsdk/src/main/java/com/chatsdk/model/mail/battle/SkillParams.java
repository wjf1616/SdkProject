package com.chatsdk.model.mail.battle;

public class SkillParams
{
	private long	startTime;
	private String	ownerId;
	private String	uuid;
	private String	skillId;
	private long	endTime;
	private int		stat;
	private long	actTime;

	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	public String getOwnerId()
	{
		return ownerId;
	}

	public void setOwnerId(String ownerId)
	{
		this.ownerId = ownerId;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getSkillId()
	{
		return skillId;
	}

	public void setSkillId(String skillId)
	{
		this.skillId = skillId;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}

	public int getStat()
	{
		return stat;
	}

	public void setStat(int stat)
	{
		this.stat = stat;
	}

	public long getActTime()
	{
		return actTime;
	}

	public void setActTime(long actTime)
	{
		this.actTime = actTime;
	}

}
