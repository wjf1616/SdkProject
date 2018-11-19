package com.chatsdk.model.mail.allianceapply;

import com.alibaba.fastjson.JSON;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.LogUtil;

public class AllianceApplyMailData extends MailData
{
	private AllianceApplyMailContents	detail;

	public AllianceApplyMailContents getDetail()
	{
		return detail;
	}

	public void setDetail(AllianceApplyMailContents detail)
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
				detail = JSON.parseObject(getContents(), AllianceApplyMailContents.class);
				hasMailOpend = true;
			}
			catch (Exception e)
			{
				LogUtil.trackMessage("[AllianceApplyMailContents parseContents error]: contents:" + getContents());
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
