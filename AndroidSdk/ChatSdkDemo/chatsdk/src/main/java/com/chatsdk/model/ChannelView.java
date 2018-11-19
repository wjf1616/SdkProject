package com.chatsdk.model;

import java.util.ArrayList;

import android.widget.ListView;

import com.chatsdk.view.MessagesAdapter;
import com.chatsdk.view.listview.PullDownToLoadMoreView;

public class ChannelView
{
	private boolean					isLoadingStart;
	public ListView					messagesListView;
	private MessagesAdapter			messagesAdapter;
	public PullDownToLoadMoreView	pullDownToLoadListView;
	public boolean					isFirstVisit;
	public int						tab;
	public int						channelType;
	public ChatChannel				chatChannel;

	public ChannelView()
	{
		init();
	}

	public boolean getLoadingStart()
	{
		return isLoadingStart;
	}

	public void setLoadingStart(boolean b)
	{
		isLoadingStart = b;
	}

	public void init()
	{
		if (messagesAdapter != null)
			messagesAdapter.destroy();
		if (messagesListView != null)
		{
			messagesListView.setOnScrollListener(null);
			messagesListView.setAdapter(null);
		}
		if (pullDownToLoadListView != null)
		{
			pullDownToLoadListView.setListViewTouchListener(null);
			pullDownToLoadListView.setListViewLoadListener(null);
			pullDownToLoadListView.removeAllViews();
		}
		isLoadingStart = false;
		isFirstVisit = true;
		messagesListView = null;
		messagesAdapter = null;
		pullDownToLoadListView = null;
		chatChannel = null;
	}

	public void setMessagesAdapter(MessagesAdapter adapter)
	{
		messagesAdapter = adapter;
	}

	public MessagesAdapter getMessagesAdapter()
	{
		return messagesAdapter;
	}
}
