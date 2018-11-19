package com.chatsdk.model.db;

import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.UserManager;

public class GetNewMsgParam
{
	public int		dbMaxSeqId;
	public String	chatTableName;

	public int		channelType;
	public String	channelId;

	public String getChannelId()
	{
		return "";
	}

	public String getChannelType()
	{
		if (chatTableName == ChannelManager.getInstance().getCountryChannel().getChatTable().getTableNameAndCreate())
		{
			channelId = ChannelManager.getInstance().getCountryChannel().getChatTable().channelID;
			channelType = ChannelManager.getInstance().getCountryChannel().channelType;
		}
		else if (UserManager.getInstance().isCurrentUserInAlliance()
				&& chatTableName == ChannelManager.getInstance().getAllianceChannel().getChatTable().getTableNameAndCreate())
		{
			channelId = ChannelManager.getInstance().getAllianceChannel().getChatTable().channelID;
			channelType = ChannelManager.getInstance().getAllianceChannel().channelType;
		}

		return "";
	}
}
