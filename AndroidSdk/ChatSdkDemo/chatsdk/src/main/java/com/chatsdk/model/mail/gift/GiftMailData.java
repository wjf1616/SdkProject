package com.chatsdk.model.mail.gift;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.chatsdk.controller.JniController;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.LogUtil;

public class GiftMailData extends MailData
{
	private int							unread;
	private int							totalNum;
	private List<GiftMailContents>	gift;

	public int getUnread()
	{
		return unread;
	}

	public void setUnread(int unread)
	{
		this.unread = unread;
	}

	public int getTotalNum()
	{
		return totalNum;
	}

	public void setTotalNum(int totalNum)
	{
		this.totalNum = totalNum;
	}

	public List<GiftMailContents> getGift()
	{
		return gift;
	}

	public void setGift(List<GiftMailContents> gift)
	{
		this.gift = gift;
	}

	public void parseContents()
	{
		super.parseContents();
		if (!getContents().equals(""))
		{
			try
			{
				if (getStatus() == 0)
					setUnread(1);
				else
					setUnread(0);
				setTotalNum(1);
				gift = new ArrayList<GiftMailContents>();
				GiftMailContents detail = new GiftMailContents();
				if (detail == null)
					return;
				detail.setMailID(getUid());
				long time = ((long) getCreateTime()) * 1000;
				detail.setCreateTime("" + time);
				detail.setType(getType());
				detail.setContents(getContents());
				detail.setRewardId(getRewardId());
				detail.setTitle(getTitle());

				gift.add(detail);
				hasMailOpend = true;
				if (detail == null || needParseByForce)
					return;
				if (contentText.length() > 50)
				{
					contentText = contentText.substring(0, 50);
					contentText = contentText + "...";
				}
			}
			catch (Exception e)
			{
				LogUtil.trackMessage("[GiftMailData parseContents error]: contents:" + getContents());
			}
		}
	}
}
