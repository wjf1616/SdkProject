package com.chatsdk.model.mail.worldexplore;

import java.util.List;

public class ExploreMailContents
{

	private int							point;
	private long						exploredTime;
	private List<ExporeRewardParams>	reward;
	private String						trap;

	public int getPoint()
	{
		return point;
	}

	public void setPoint(int point)
	{
		this.point = point;
	}

	public long getExploredTime()
	{
		return exploredTime;
	}

	public void setExploredTime(long exploredTime)
	{
		this.exploredTime = exploredTime;
	}

	public List<ExporeRewardParams> getReward()
	{
		return reward;
	}

	public void setReward(List<ExporeRewardParams> reward)
	{
		this.reward = reward;
	}

	public String getTrap()
	{
		return trap;
	}

	public void setTrap(String trap)
	{
		this.trap = trap;
	}

}
