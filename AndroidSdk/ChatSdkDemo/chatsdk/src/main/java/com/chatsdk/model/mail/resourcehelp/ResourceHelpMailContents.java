package com.chatsdk.model.mail.resourcehelp;

import java.util.List;

import com.chatsdk.model.mail.battle.RewardParams;

public class ResourceHelpMailContents
{
	private String				uid;
	private String				fromName;
	private String				pic;
	private String				alliance;
	private int					level;
	private List<RewardParams>	reward;
	private String				traderAlliance;
	private int					pointId;
	private String				createTime;

	public List<RewardParams> getReward()
	{
		return reward;
	}

	public void setReward(List<RewardParams> reward)
	{
		this.reward = reward;
	}

	public String getTraderAlliance()
	{
		return traderAlliance;
	}

	public void setTraderAlliance(String traderAlliance)
	{
		this.traderAlliance = traderAlliance;
	}

	public int getPointId()
	{
		return pointId;
	}

	public void setPointId(int pointId)
	{
		this.pointId = pointId;
	}

	public String getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(String createTime)
	{
		this.createTime = createTime;
	}

	public String getUid()
	{
		return uid;
	}

	public void setUid(String uid)
	{
		this.uid = uid;
	}

	public String getFromName()
	{
		return fromName;
	}

	public void setFromName(String fromName)
	{
		this.fromName = fromName;
	}

	public String getPic()
	{
		return pic;
	}

	public void setPic(String pic)
	{
		this.pic = pic;
	}

	public String getAlliance()
	{
		return alliance;
	}

	public void setAlliance(String alliance)
	{
		this.alliance = alliance;
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

}
