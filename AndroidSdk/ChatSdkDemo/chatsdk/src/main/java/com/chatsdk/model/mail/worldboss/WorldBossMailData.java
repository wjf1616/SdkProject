package com.chatsdk.model.mail.worldboss;

import com.alibaba.fastjson.JSON;
import com.chatsdk.controller.JniController;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.LogUtil;

public class WorldBossMailData extends MailData
{
	private WorldBossMailContents	detail;

	public WorldBossMailContents getDetail()
	{
		return detail;
	}

	public void setDetail(WorldBossMailContents detail)
	{
		this.detail = detail;
	}

	public void parseContents()
	{
		super.parseContents();
		if (!getContents().equals(""))
		{
			try
			{
				detail = JSON.parseObject(getContents(), WorldBossMailContents.class);
				hasMailOpend = true;
				if (detail == null || needParseByForce)
					return;
				String bossId = "";
				String bossName = "";
				if(getType() == MailManager.MAIL_TYPE_ALLIANCE_BOSS_TREASURE){
					bossName = LanguageManager.getLangByKey(LanguageKeys.ALLIANCE_BOSS_TREASURE);
					String content = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ALLIANCE_TREASURE,bossName);
					String myTeam = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYARMY);
					contentText = myTeam.concat(content);
				}else if (detail.getAttList() != null && detail.getAttList().size() > 0)
				{
					String leaderName = "";
					bossId = detail.getDef().getId();
					if(getType() == MailManager.MAIL_TYPE_ALLIANCE_BOSS){
						bossName = JniController.getInstance().excuteJNIMethod("getAllianceBossName",new Object[]{bossId});
					}else {
						bossName = JniController.getInstance().excuteJNIMethod("getNameById", new Object[]{bossId});
					}
					for (int i = 0; i < detail.getAttList().size(); i++)
					{
						AttListParams att = detail.getAttList().get(i);
						if (att != null)
						{
							String name = att.getName();
							if (att.getLeader() == 1 && name != null)
							{
								leaderName = name;
								break;
							}
						}
					}
					contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_CONTENT_WORLDBOSS, leaderName,bossName); //{0}集结的队伍挑战了{1}
				}
				if (contentText.length() > 50)
				{
					contentText = contentText.substring(0, 50);
					contentText = contentText + "...";
				}
			}
			catch (Exception e)
			{
				LogUtil.trackMessage("[WorldBossMailContents parseContents error]: contents:" + getContents());
			}
		}
	}
}
