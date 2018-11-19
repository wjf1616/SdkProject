package com.chatsdk.model.mail.battle;

import com.chatsdk.model.mail.monster.RateRewardValueParams;

public class RewardParams
{
	private int	v;
	private RateRewardValueParams value;
	private int type;
	private int	t;

	public int getV()
	{
		return v;
	}

	public void setV(int v)
	{
		this.v = v;
	}

	public int getT()
	{
		return t;
	}

	public void setT(int t)
	{
		this.t = t;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public RateRewardValueParams getValue() {
		return value;
	}

	public void setValue(RateRewardValueParams value) {
		this.value = value;
	}
}
