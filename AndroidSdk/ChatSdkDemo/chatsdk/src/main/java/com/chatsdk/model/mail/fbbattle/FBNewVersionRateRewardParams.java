package com.chatsdk.model.mail.fbbattle;

public class FBNewVersionRateRewardParams
{
	public FBNewVersionRateRewardValueParams getValue() {
		return value;
	}

	public void setValue(FBNewVersionRateRewardValueParams value) {
		this.value = value;
	}

	private FBNewVersionRateRewardValueParams	value;

	private int						type;

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

}
