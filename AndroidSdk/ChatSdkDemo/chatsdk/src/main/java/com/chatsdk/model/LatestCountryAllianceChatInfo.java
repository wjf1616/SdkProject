package com.chatsdk.model;

public class LatestCountryAllianceChatInfo
{
	private LatestChatInfo latestCountryChatInfo = null;
	private LatestChatInfo latestAllianceChatInfo = null;
	private LatestChatInfo latestTopChatRoomChatInfo = null;
	
	public LatestChatInfo getLatestCountryChatInfo()
	{
		return latestCountryChatInfo;
	}
	public void setLatestCountryChatInfo(LatestChatInfo latestCountryChatInfo)
	{
		this.latestCountryChatInfo = latestCountryChatInfo;
	}
	public LatestChatInfo getLatestAllianceChatInfo()
	{
		return latestAllianceChatInfo;
	}
	public void setLatestAllianceChatInfo(LatestChatInfo latestAllianceChatInfo)
	{
		this.latestAllianceChatInfo = latestAllianceChatInfo;
	}

	public LatestChatInfo getLatestTopChatRoomChatInfo()
	{
		return latestTopChatRoomChatInfo;
	}
	public void setLatestTopChatRoomChatInfo(LatestChatInfo latestTopChatRoomChatInfo)
	{
		this.latestTopChatRoomChatInfo = latestTopChatRoomChatInfo;
	}
}
