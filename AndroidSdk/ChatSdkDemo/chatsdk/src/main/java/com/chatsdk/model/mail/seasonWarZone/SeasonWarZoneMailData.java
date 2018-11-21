package com.chatsdk.model.mail.seasonWarZone;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.MathUtil;

public class SeasonWarZoneMailData extends MailData
{
	private SeasonWarZoneMailContents	detail;

	public SeasonWarZoneMailContents getDetail()
	{
		return detail;
	}

	public void setDetail(SeasonWarZoneMailContents detail)
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
				detail = JSON.parseObject(getContents(), SeasonWarZoneMailContents.class);
				hasMailOpend = true;
				if (detail == null || needParseByForce)
					return;

				nameText = LanguageManager.getLangByKey("170145"); //凯特琳
				if (contentText.length() > 50)
				{
					contentText = contentText.substring(0, 50);
					contentText = contentText + "...";
				}
			}
			catch (Exception e)
			{
				LogUtil.trackMessage("[ExploreMailContents parseContents error]: contents:" + getContents());
			}
		}
	}
}
