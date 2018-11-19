package com.chatsdk.model.mail.resouce;

import java.util.List;

import com.chatsdk.model.mail.battle.RewardParams;

public class ResourceMailContents
{

	private String				uid;
	private int					level;
	private List<RewardParams>	reward;
	private List<DropParams>	drop;
	private int					pointId;
	private List<ExtraRewardParams> extraReward;
	private int  rewardResult;
	private int  collectRewardNum;
	private String				createTime;
	private long desertExp;


	public long getDesertExp()
	{
		return desertExp;
	}

	public void setDesertExp(long desertExp)
	{
		this.desertExp = desertExp;
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public List<RewardParams> getReward()
	{
		return reward;
	}

	public void setReward(List<RewardParams> reward)
	{
		this.reward = reward;
	}
	
	public List<ExtraRewardParams> getExtraReward()
	{
		return extraReward;
	}

	public void setExtraReward(List<ExtraRewardParams> extraReward)
	{
		this.extraReward = extraReward;
	}

	public List<DropParams> getDrop()
	{
		return drop;
	}

	public void setDrop(List<DropParams> drop)
	{
		this.drop = drop;
	}

	public int getPointId()
	{
		return pointId;
	}

	public void setPointId(int pointId)
	{
		this.pointId = pointId;
	}
	
	public int getRewardResult()
	{
		return rewardResult;
	}

	public void setRewardResult(int rewardResult)
	{
		this.rewardResult = rewardResult;
	}
	
	public int getCollectRewardNum()
	{
		return collectRewardNum;
	}

	public void setCollectRewardNum(int collectRewardNum)
	{
		this.collectRewardNum = collectRewardNum;
	}
	
	public String getUid()
	{
		return uid;
	}

	public void setUid(String uid)
	{
		this.uid = uid;
	}

	public String getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(String createTime)
	{
		this.createTime = createTime;
	}

}
