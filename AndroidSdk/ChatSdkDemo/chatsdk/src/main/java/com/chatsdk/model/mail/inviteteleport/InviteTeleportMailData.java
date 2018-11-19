package com.chatsdk.model.mail.inviteteleport;

import com.alibaba.fastjson.JSON;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.LogUtil;

public class InviteTeleportMailData extends MailData
{
	private InviteTeleportMailContents	detail;

	public InviteTeleportMailContents getDetail()
	{
		return detail;
	}

	public void setDetail(InviteTeleportMailContents detail)
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
				detail = JSON.parseObject(getContents(), InviteTeleportMailContents.class);
				hasMailOpend = true;
			}
			catch (Exception e)
			{
				LogUtil.trackMessage("[InviteTeleportMailContents parseContents error]: contents:" + getContents());
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
