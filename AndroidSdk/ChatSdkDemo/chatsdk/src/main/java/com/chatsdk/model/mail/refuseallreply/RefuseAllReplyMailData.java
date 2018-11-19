package com.chatsdk.model.mail.refuseallreply;

import com.alibaba.fastjson.JSON;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.LogUtil;

public class RefuseAllReplyMailData extends MailData
{
	private RefuseAllReplyMailContents	detail;

	public RefuseAllReplyMailContents getDetail()
	{
		return detail;
	}

	public void setDetail(RefuseAllReplyMailContents detail)
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
				detail = JSON.parseObject(getContents(), RefuseAllReplyMailContents.class);
				hasMailOpend = true;
			}
			catch (Exception e)
			{
				LogUtil.trackMessage("[RefuseAllReplyMailData parseContents error]: contents:" + getContents());
			}
		}
	}
}
