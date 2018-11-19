package com.chatsdk.model.mail.monster;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.chatsdk.controller.JniController;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.LogUtil;

public class MonsterMailData extends MailData
{
	private int							unread;
	private int							totalNum;
	private List<MonsterMailContents>	monster;

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

	public List<MonsterMailContents> getMonster()
	{
		return monster;
	}

	public void setMonster(List<MonsterMailContents> monster)
	{
		this.monster = monster;
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
				monster = new ArrayList<MonsterMailContents>();
				MonsterMailContents detail = JSON.parseObject(getContents(), MonsterMailContents.class);
				if (detail == null)
					return;
				detail.setUid(getUid());
				long time = ((long) getCreateTime()) * 1000;
				detail.setCreateTime("" + time);
				detail.setType(getType());
				monster.add(detail);
				hasMailOpend = true;
				if (detail == null || needParseByForce)
					return;

				DefParams def = detail.getDef();
				if (def == null)
					return;
				String name = "";
				String level = "";
				if (StringUtils.isNotEmpty(def.getId()))
				{
					name = JniController.getInstance().excuteJNIMethod("getNameById", new Object[] { def.getId() });
					level = JniController.getInstance().excuteJNIMethod("getPropById", new Object[] { def.getId(), "level" });
				}
				name += " Lv.";
				name += level;
				contentText = name;
				contentText += "  ";

				int monsterResult = -1;
				if (StringUtils.isNotEmpty(detail.getStat()))
				{
					monsterResult = 4;
				}
				else
				{
					if (detail.getFirstKill() == 1)
					{
						monsterResult = 1;
					}
					else if (def.getMchp() > 0)
					{
						monsterResult = 2;
					}
					else
					{
						monsterResult = 3;
					}
				}

				if (monsterResult == 1)
				{
					contentText += LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_103758);
				}
				else if (monsterResult == 2)
				{
					contentText += LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105118);
				}
				else if (monsterResult == 4)
				{
					contentText += LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_103786);
				}
				else
				{
					contentText += LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105117);
				}
				if (contentText.length() > 50)
				{
					contentText = contentText.substring(0, 50);
					contentText = contentText + "...";
				}
			}
			catch (Exception e)
			{
				LogUtil.trackMessage("[MonsterMailData parseContents error]: contents:" + getContents());
			}
		}
	}
}
