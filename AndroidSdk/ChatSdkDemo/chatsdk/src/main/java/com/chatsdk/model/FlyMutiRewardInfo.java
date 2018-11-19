package com.chatsdk.model;

import java.util.List;

public class FlyMutiRewardInfo
{
	private List<FlyRewardInfo>	flyToolReward;
	private List<FlyRewardInfo>	flyReward;

	public List<FlyRewardInfo> getFlyToolReward()
	{
		return flyToolReward;
	}

	public void setFlyToolReward(List<FlyRewardInfo> flyToolReward)
	{
		this.flyToolReward = flyToolReward;
	}

	public List<FlyRewardInfo> getFlyReward()
	{
		return flyReward;
	}

	public void setFlyReward(List<FlyRewardInfo> flyReward)
	{
		this.flyReward = flyReward;
	}
}
