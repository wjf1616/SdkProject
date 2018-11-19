package com.chatsdk.model;

import android.content.pm.ApplicationInfo;

import com.chatsdk.util.MathUtil;

public class ApplicationItem extends ChannelListItem
{
	public ApplicationInfo	appInfo;
	private boolean			isLock;
	private boolean			hasReward;
	public int				showMute;
	public boolean			showUreadAsText;
	public int				time;
	public String			summary;
	public long             failTime;

	public ApplicationItem(ApplicationInfo applicationInfo)
	{
		appInfo = applicationInfo;

//		unreadCount = MathUtil.random(1, 10) > 5 ? 0 : 1;
//		if (unreadCount == 1)
//		{
//			unreadCount = MathUtil.random(1, 10) > 5 ? MathUtil.random(1, 30) : MathUtil.random(30, 1000);
//		}
		showUreadAsText = MathUtil.random(1, 10) > 5;
		isLock = MathUtil.random(0, 10) > 5;
		hasReward = MathUtil.random(0, 10) > 5;
		showMute = MathUtil.random(0, 10) > 5 ? 1 : 2;
		time = TimeManager.getInstance().getCurrentTime() - MathUtil.random(0, 100 * 24 * 3600);
		failTime = TimeManager.getInstance().getCurrentTime() - MathUtil.random(0, 100 * 24 * 3600);
		summary = MathUtil.random(0, 10) > 5 ? "我来了，希望能和大家成为朋友，一起打造属于我们的帝国。(系统)"
				: "[ui_silver.png]+3k [ui_food.png]+3k [diamond.png]+3k  [ui_gold.png]+3k [ui_wood.png]+3k [ui_iron.png]+3k";
	}

	public boolean isUnread()
	{
		return unreadCount > 0;
	}

	public long getChannelTime()
	{
		return time;
	}

	public boolean hasReward()
	{
		return hasReward;
	}

	public boolean isLock()
	{
		return isLock;
	}

	public long getChannelFailTime(){return failTime;}
}
