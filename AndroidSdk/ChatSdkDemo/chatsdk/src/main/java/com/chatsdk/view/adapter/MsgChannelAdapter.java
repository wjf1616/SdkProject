package com.chatsdk.view.adapter;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.ServiceInterface;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.util.LogUtil;
import com.chatsdk.view.ChannelListActivity;
import com.chatsdk.view.ChannelListFragment;

public class MsgChannelAdapter extends ChannelAdapter
{
	protected String	mChannelId	= "";
	private boolean		initLoaded	= false;

	public MsgChannelAdapter(ChannelListActivity context, ChannelListFragment fragment, String channelId)
	{
		super(context, fragment);
		mChannelId = channelId;
		if (channelId.equals(MailManager.CHANNELID_MOD))
		{
			ChatServiceController.isContactMod = true;
		}
		reloadData();
	}

	public boolean hasMoreData()
	{
		if (ServiceInterface.isHandlingGetNewMailMsg)
		{
			return false;
		}
		else
		{
			int count = getAllMsgChannels().size();

			return count > list.size();
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ChatChannel channel = (ChatChannel) getItem(position);
		if (channel != null)
			channel.refreshRenderData();
		return super.getView(position, convertView, parent);
	}

	public synchronized void loadMoreData()
	{
		initLoaded = true;
		List<ChatChannel> allMsgChannels = getAllMsgChannels();
		List<ChatChannel> loadedMsgChannels = getLoadedMsgChannels();

		int moreCount = loadedMsgChannels.size() + 10;
		int actualCount = allMsgChannels.size() > moreCount ? moreCount : allMsgChannels.size();

		List<ChatChannel> subMsgChannels = allMsgChannels.subList(0, actualCount);
		
		int addCnt = 0;

		List<ChatChannel> tempList = new ArrayList<ChatChannel>();
		for (int i = 0; i < subMsgChannels.size(); i++)
		{
			ChatChannel chatChannel = subMsgChannels.get(i);
			if (chatChannel != null && !ChannelManager.isChannelInList(chatChannel, loadedMsgChannels))
			{
				addCnt++;
				tempList.add(chatChannel);
			}
		}
		
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "allMsgChannels.size()", allMsgChannels.size(), "moreCount",
				moreCount, "actualCount", actualCount, "subMsgChannels.size()", subMsgChannels.size(), "addCnt", addCnt);

		List<ChatChannel> LoadedMessageChannels = ChannelManager.getInstance().getLoadedMessageChannel();
		for (int i = 0; i < tempList.size(); i++)
		{
			ChatChannel chatChannel = tempList.get(i);
			if (chatChannel != null && !chatChannel.hasInitLoaded())
			{
				chatChannel.loadMoreMsg();
			}
			LoadedMessageChannels.add(chatChannel);
		}

		tempList.clear();

		for (int i = 0; i < LoadedMessageChannels.size(); i++)
		{
			ChatChannel chatChannel = LoadedMessageChannels.get(i);
			if (chatChannel != null
					&& !ChannelManager.isChannelInList(chatChannel, list)
					&& chatChannel.channelType == DBDefinition.CHANNEL_TYPE_USER)
			{
				tempList.add(chatChannel);
			}
		}

		for (int i = 0; i < tempList.size(); i++)
		{
			ChatChannel chatChannel = tempList.get(i);
			if (chatChannel != null)
			{
				list.add(chatChannel);
			}
		}

		refreshOrder();

		fragment.onLoadMoreComplete();
	}

	public void reloadData()
	{
		List<ChatChannel> loadedMsgChannels = ChannelManager.getInstance().getLoadedMsgChannel();
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "loadedMsgChannels.size()", loadedMsgChannels.size());

		list.clear();

		// 第一次进入列表时，可能loadedMsgChannels已经有内容（刚收到的消息），此时需要loadMoreData一次，否则会只显示刚收到的channel
		if (loadedMsgChannels.size() == 0 || !initLoaded)
		{
			LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_VIEW, "    初次加载");
			loadMoreData();
		}
		else
		{
			LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_VIEW, "    重新加载");
			list.addAll(loadedMsgChannels);
		}

		refreshOrder();

		fragment.setNoMailTipVisible(list.size() <= 0);
	}
	
	public void refreshAdapterList()
	{
		list.clear();
		List<ChatChannel> loadedMsgChannels = ChannelManager.getInstance().getLoadedMsgChannel();
		list.addAll(loadedMsgChannels);
		refreshOrder();
		fragment.setNoMailTipVisible(list.size() <= 0);
	}

	private List<ChatChannel> getAllMsgChannels()
	{
		if (mChannelId.equals(MailManager.CHANNELID_MOD))
		{
			return ChannelManager.getInstance().getAllModChannel();
		}
		else if (mChannelId.equals(MailManager.CHANNELID_MESSAGE))
		{
			return ChannelManager.getInstance().getAllMsgChannel();
		}
		return new ArrayList<ChatChannel>();
	}

	private List<ChatChannel> getLoadedMsgChannels()
	{
		if (mChannelId.equals(MailManager.CHANNELID_MOD))
		{
			return ChannelManager.getInstance().getLoadedModChannel();
		}
		else if (mChannelId.equals(MailManager.CHANNELID_MESSAGE))
		{
			return ChannelManager.getInstance().getLoadedMsgChannel();
		}
		return new ArrayList<ChatChannel>();
	}

	@Override
	public void destroy()
	{
		super.destroy();
		ChatServiceController.isContactMod = false;
	}
}
