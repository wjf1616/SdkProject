package com.chatsdk.model.mail.monster;

import java.util.List;

public class MonsterMailContents
{
	private String					uid;
	private int						type;
	private String					createTime;
	private DefParams				def;
	private AttParams				att;
	private List<RateRewardParams>	rateReward;
	private String					reportUid;
	private int						xy;
	private int						firstKill;
	private String					stat;

	public String getUid()
	{
		return uid;
	}

	public void setUid(String uid)
	{
		this.uid = uid;
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

	public DefParams getDef()
	{
		return def;
	}

	public void setDef(DefParams def)
	{
		this.def = def;
	}

	public AttParams getAtt()
	{
		return att;
	}

	public void setAtt(AttParams att)
	{
		this.att = att;
	}

	public List<RateRewardParams> getRateReward()
	{
		return rateReward;
	}

	public void setRateReward(List<RateRewardParams> rateReward)
	{
		this.rateReward = rateReward;
	}

	public String getReportUid()
	{
		return reportUid;
	}

	public void setReportUid(String reportUid)
	{
		this.reportUid = reportUid;
	}

	public int getXy()
	{
		return xy;
	}

	public void setXy(int xy)
	{
		this.xy = xy;
	}

	public int getFirstKill()
	{
		return firstKill;
	}

	public void setFirstKill(int firstKill)
	{
		this.firstKill = firstKill;
	}

	public String getStat()
	{
		return stat;
	}

	public void setStat(String stat)
	{
		this.stat = stat;
	}

}
