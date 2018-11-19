package com.chatsdk.model.mail.allianceinvite;

import com.alibaba.fastjson.JSON;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.LogUtil;

public class AllianceInviteMailData extends MailData
{
	private AllianceInviteMailContents	detail;

	public AllianceInviteMailContents getDetail()
	{
		return detail;
	}

	public void setDetail(AllianceInviteMailContents detail)
	{
		this.detail = detail;
	}

	public void parseContents()
	{
		super.parseContents();
		if(!needParseByForce)
			return;
		if (!getContents().equals(""))
		{
			try
			{
				detail = JSON.parseObject(getContents(), AllianceInviteMailContents.class);
				hasMailOpend = true;
			}
			catch (Exception e)
			{
				LogUtil.trackMessage("[RefuseAllReplyMailData parseContents error]: contents:" + getContents());
			}
		}
	}

	@Override
	public void setMailDealStatus()
	{
		if (detail != null)
		{
			detail.setDeal(1);
			if (!getContents().equals("") && getContents().contains("deal"))
			{
				setContents(JSON.toJSONString(detail));
			}
		}
	}
}
