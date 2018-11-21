package com.chatsdk.model.db;

import org.apache.commons.lang.StringUtils;
import com.chatsdk.model.MailManager;
import com.chatsdk.util.MathUtil;

public class ChatTable
{
	public String	channelID;
	public int		channelType;

	public ChatTable(String channelID, int type)
	{
		this.channelID = channelID;
		this.channelType = type;
	}

	public String getChannelName()
	{
		return channelID + DBDefinition.getPostfixForType(channelType);
	}

	public String getTableNameAndCreate()
	{
		DBManager.getInstance().prepareChatTable(this);

		return getTableName();
	}

	public boolean isChannelType()
	{
		if (StringUtils.isNotEmpty(channelID)
				&& (channelID.equals(MailManager.CHANNELID_RESOURCE) || channelID.equals(MailManager.CHANNELID_GIFT)|| channelID
						.equals(MailManager.CHANNELID_RESOURCE_HELP) ||channelID.equals(MailManager.CHANNELID_MISSILE)))
		{
			return true;
		}
		return false;
	}

	public int getMailTypeByChannelId()
	{
		int type = -1;
		if (StringUtils.isNotEmpty(channelID))
		{
			if (channelID.equals(MailManager.CHANNELID_RESOURCE))
			{
				type = MailManager.MAIL_RESOURCE;
			}
			if (channelID.equals(MailManager.CHANNELID_RESOURCE_HELP))
			{
				type = MailManager.MAIL_RESOURCE_HELP;
			}
			if (channelID.equals(MailManager.CHANNELID_GIFT))
			{
				type = MailManager.MAIL_GIFT_BUY_EXCHANGE;
			}
			if (channelID.equals(MailManager.CHANNELID_MISSILE))
			{
				type = MailManager.MAIL_MISSILE;
			}
		}
		return type;
	}

	public String getTableName()
	{
		String channelName = getChannelName();
		String md5TableId = MathUtil.md5(channelName);
		String tableName = "";
		if (channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			tableName = DBDefinition.chatTableId2Name(md5TableId);
		}
		else
		{
			tableName = DBDefinition.TABEL_MAIL;
		}

		return tableName;
	}

	public static ChatTable createChatTable(final int channelType, final String channelID)
	{
		return new ChatTable(channelID, channelType);
	}
}
