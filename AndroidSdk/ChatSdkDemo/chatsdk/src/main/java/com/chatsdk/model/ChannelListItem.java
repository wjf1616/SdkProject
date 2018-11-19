package com.chatsdk.model;

public class ChannelListItem
{
	public transient boolean	checked		= false;
	public transient int		unreadCount	= 0;

	public transient int		allCount	= 0;

	public boolean isUnread()
	{
		return false;
	}

	public long getChannelTime()
	{
		return 0;
	}

	public boolean hasReward()
	{
		return false;
	}

	public boolean isLock()
	{
		return false;
	}

	public String getSetting(){
		return "";
	}

}
