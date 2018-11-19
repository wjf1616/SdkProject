package com.chatsdk.view.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.model.ChannelListItem;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.MailManager;
import com.chatsdk.view.ChannelListActivity;
import com.chatsdk.view.ChannelListFragment;
import com.chatsdk.controller.JniController;import java.util.ArrayList;

public class EventMainChannelAdapter extends ChannelAdapter
{
	public EventMainChannelAdapter(ChannelListActivity context, ChannelListFragment fragment)
	{
		super(context, fragment);

		reloadData();
	}

	public void reloadData()
	{
		list.clear();
		ArrayList<ChannelListItem>	testlist=ChannelManager.getInstance().getAllMailChannel();
		list.addAll(ChannelManager.getInstance().getAllEventChannel());

		refreshOrder();
		fragment.setNoMailTipVisible(list.size() <= 0);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ChatChannel channel = (ChatChannel) getItem(position);
		if (channel != null)
			channel.refreshRenderData();

		return super.getView(position, convertView, parent);
	}

	@Override
	public int getItemViewType(int position)
	{

		return VIEW_TYPE_READ;
	}
	public static final String[]	MAIN_CHANNEL_ORDERS	= {
			MailManager.CHANNELID_EVENT_PERSONALARM,
			MailManager.CHANNELID_EVENT_ALLIANCERAM,
			MailManager.CHANNELID_EVENT_GREATKING,
			MailManager.CHANNELID_EVENT_DESERT,
			MailManager.CHANNELID_EVENT_NORMAL
	};


	public void refreshOrder()
	{
		int len =  MAIN_CHANNEL_ORDERS.length;

		//是否使用配置排序
		boolean canUseOrder=true;

		String[] ChannelArr_1=new String[len];

		for (int i = 0;i < len ;i++ )
		{
			if(i==0)
			{
				ChannelArr_1[i]=MailManager.CHANNELID_EVENT_PERSONALARM;
			}
			else if(i==1)
			{
				ChannelArr_1[i]=MailManager.CHANNELID_EVENT_ALLIANCERAM;
			}
			else if(i==2)
			{
				ChannelArr_1[i]=MailManager.CHANNELID_EVENT_GREATKING;
			}
			else if(i==3)
			{
				ChannelArr_1[i]=MailManager.CHANNELID_EVENT_DESERT;
			}
			else if(i==4)
			{
				ChannelArr_1[i]=MailManager.CHANNELID_EVENT_NORMAL;
			}

		}

		if(canUseOrder)
		{
			for (int i = ChannelArr_1.length - 1; i >= 0; i--)
			{
				String type = ChannelArr_1[i];
				for (int j = 0; j < list.size(); j++)
				{
					ChatChannel channel = (ChatChannel) list.get(j);
					if (channel == null)
						continue;
					if (channel.channelID.equals(type))
					{
						moveToHead(j);
						break;
					}
				}
			}
		}

		notifyDataSetChangedOnUI();

	}

	private void moveToHead(int i)
	{
		list.add(0, list.remove(i));
	}
}
