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
import com.chatsdk.controller.JniController;

public class MainChannelAdapter extends ChannelAdapter
{
	public MainChannelAdapter(ChannelListActivity context, ChannelListFragment fragment)
	{
		super(context, fragment);

		reloadData();
	}

	public void reloadData()
	{
		list.clear();
		list.addAll(ChannelManager.getInstance().getAllMailChannel());
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
        ChannelListItem item = getItem(position);
        if (item != null && item instanceof ChatChannel)
        {
            ChatChannel channel = (ChatChannel) item;
            if (!channel.channelID.equals(MailManager.CHANNELID_MONSTER) && !channel.channelID.equals(MailManager.CHANNELID_MOD)
                    && !channel.channelID.equals(MailManager.CHANNELID_RESOURCE)
                    && !channel.channelID.equals(MailManager.CHANNELID_KNIGHT)
                    && !channel.channelID.equals(MailManager.CHANNELID_RESOURCE_HELP)
                    && !channel.channelID.equals(MailManager.CHANNELID_MISSILE)
					&& !channel.channelID.equals(MailManager.CHANNELID_GIFT)
					&& !channel.channelID.equals(MailManager.CHANNELID_MOBILIZATION_CENTER)
					&& !channel.channelID.equals(MailManager.CHANNELID_COMBOTFACTORY_FIRE))

                return VIEW_TYPE_READ;
            else
                return VIEW_TYPE_NONE;
        }
        return VIEW_TYPE_NONE;
	}

	public static final String[]	MAIN_CHANNEL_ORDERS	= {
			MailManager.CHANNELID_MESSAGE,
			MailManager.CHANNELID_ALLIANCE,
			MailManager.CHANNELID_FIGHT,
			MailManager.CHANNELID_EVENT,
			MailManager.CHANNELID_NOTICE,
			MailManager.CHANNELID_STUDIO,
			MailManager.CHANNELID_SYSTEM,
			MailManager.CHANNELID_MOD,
			MailManager.CHANNELID_RESOURCE,
			MailManager.CHANNELID_MONSTER,
			MailManager.CHANNELID_KNIGHT,
			MailManager.CHANNELID_MISSILE,
			MailManager.CHANNELID_GIFT,
			MailManager.CHANNELID_BATTLEGAME,
			MailManager.CHANNELID_ARENAGAME,
			MailManager.CHANNELID_SHAMOGAME,
			MailManager.CHANNELID_SHAMOEXPLORE,
			MailManager.CHANNELID_BORDERFIGHT,
			MailManager.CHANNELID_SHAMOGOLDDIGGER,
			MailManager.CHANNELID_MOBILIZATION_CENTER,
			MailManager.CHANNELID_COMBOTFACTORY_FIRE
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
				ChannelArr_1[i]=MailManager.CHANNELID_MESSAGE;
			}
			else if(i==1)
			{
				ChannelArr_1[i]=MailManager.CHANNELID_ALLIANCE;
			}
			else if(i==2)
			{
				ChannelArr_1[i]=MailManager.CHANNELID_FIGHT;
			}
			else if(i==3)
			{
				ChannelArr_1[i]=MailManager.CHANNELID_EVENT;
			}
			else if(i==4)
			{
				ChannelArr_1[i]=MailManager.CHANNELID_NOTICE;
			}
			else if(i==5)
			{
				ChannelArr_1[i]=MailManager.CHANNELID_STUDIO;
			}
			else if(i==6)
			{
				ChannelArr_1[i]=MailManager.CHANNELID_SYSTEM;
			}
			else
			{
				String type = MAIN_CHANNEL_ORDERS[i];

				int order = JniController.getInstance().excuteJNIMethod("getMailOrderById", new Object[] {type});

				if(order>=1)
				{
					if(order>len-6)
					{
						canUseOrder=false;
						break;
					}
					ChannelArr_1[order+6]=type;
				}
				else
				{
					canUseOrder=false;
					break;
				}


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
		else
		{
			for (int i = MAIN_CHANNEL_ORDERS.length - 1; i >= 0; i--)
			{
				String type = MAIN_CHANNEL_ORDERS[i];
				for (int j = 0; j < list.size(); j++)
				{
					ChatChannel channel = (ChatChannel) list.get(j);
					if (channel == null)
						continue;
//                if (ChannelManager.getInstance().needParseFirstChannel(channel.channelID)
//						|| (ChannelManager.getInstance().getFirstChannelID() != null && (channel.isDialogChannel() || channel.channelID.equals(MailManager.CHANNELID_MOD)) && ChannelManager.getInstance().getFirstChannelID().equals("")) )
//                    ChannelManager.getInstance().parseFirstChannelID();

					if (channel.channelID.equals(type))
					{
						moveToHead(j);
						break;
					}
				}
			}
		}

		ChannelManager.getInstance().parseFirstChannelIDNew();
        notifyDataSetChangedOnUI();

	}

	private void moveToHead(int i)
	{
		list.add(0, list.remove(i));
	}
}
