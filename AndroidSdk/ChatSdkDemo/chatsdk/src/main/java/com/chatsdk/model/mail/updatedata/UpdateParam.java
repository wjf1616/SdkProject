package com.chatsdk.model.mail.updatedata;

public class UpdateParam
{
	private String	uid;
	private int		status;			
	private int		rewardStatus;
	private int		saveFlag;			
	private long	mailLastUpdateTime;

	public String getUid()
	{
		return uid;
	}

	public void setUid(String uid)
	{
		this.uid = uid;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public int getRewardStatus()
	{
		return rewardStatus;
	}

	public void setRewardStatus(int rewardStatus)
	{
		this.rewardStatus = rewardStatus;
	}

	public int getSaveFlag()
	{
		return saveFlag;
	}

	public void setSaveFlag(int saveFlag)
	{
		this.saveFlag = saveFlag;
	}

	public long getMailLastUpdateTime()
	{
		return mailLastUpdateTime;
	}

	public void setMailLastUpdateTime(long mailLastUpdateTime)
	{
		this.mailLastUpdateTime = mailLastUpdateTime;
	}

}
