package com.chatsdk.model.mail.resouce;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.chatsdk.controller.JniController;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.model.mail.battle.RewardParams;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.MathUtil;

public class ResourceMailData extends MailData
{
	private int							unread;
	private int							totalNum;
	private List<ResourceMailContents>	collect;

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

	public List<ResourceMailContents> getCollect()
	{
		return collect;
	}

	public void setCollect(List<ResourceMailContents> collect)
	{
		this.collect = collect;
	}

	public void parseContents()
	{
		super.parseContents();
		if (!getContents().equals(""))
		{
			try
			{
				collect = new ArrayList<ResourceMailContents>();
				ResourceMailContents detail = JSON.parseObject(getContents(), ResourceMailContents.class);
				
				if (detail == null)
				{
					contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_108896);
				}
				else
				{
					detail.setUid(getUid());
					long time = ((long) getCreateTime()) * 1000;
					detail.setCreateTime("" + time);
					collect.add(detail);
					hasMailOpend = true;
					if(needParseByForce)
						return;
					
					boolean resourceResult = false;
					if (detail.getReward() == null || detail.getReward().size() <= 0)
					{
						resourceResult = false;
					}
					else
					{
						resourceResult = true;
					}

					if (resourceResult)
					{
						RewardParams reward = detail.getReward().get(0);
						if (reward != null)
						{
							int type = reward.getT();
							int value = reward.getV();
							String icon = JniController.getInstance().excuteJNIMethod("getPicByType",
									new Object[] { Integer.valueOf(type), Integer.valueOf(value) });
							contentText = "[" + icon + "]" + " + " + MathUtil.getFormatNumber(value);
						}
						else
						{
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_108896);
						}
					}
					else
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_108896);
				}
			}
			catch (Exception e)
			{
				LogUtil.trackMessage("[ResourceMailContents parseContents error]: contents:" + getContents());
			}
		}
	}
}
