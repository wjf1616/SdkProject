package com.chatsdk.view.adapter;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.model.ChannelListItem;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.SortUtil;
import com.chatsdk.view.ChatActivity;
import com.chatsdk.view.ChatFragment;

public class ChatRoomListAdapter extends BaseAdapter
{
	public static final int	VIEW_TYPE_NONE				= 0;
	public static final int	VIEW_TYPE_DELETE			= 1;
	public static final int	VIEW_TYPE_READ_AND_DELETE	= 2;
	public static final int	VIEW_TYPE_READ				= 3;
	public static final int VIEW_TYPE_TOP 				= 4;
	public static final int VIEW_TYPE_UNTOP 			= 5;

	public ChatFragment			fragment;
	protected ChatActivity		context;
	public ArrayList<ChannelListItem>	list	= new ArrayList<ChannelListItem>();
	public boolean						isLoadingMore;

	public ChatRoomListAdapter(ChatActivity context, ChatFragment fragment)
	{
		this.context = context;
		this.fragment = fragment;
	}

	public void reloadData()
	{
	}

	public void refreshOrder()
	{
		SortUtil.getInstance().refreshChatRoomListOrder(list, ChannelListItem.class);

		notifyDataSetChangedOnUI();
	}

	public void refreshAdapterList()
	{

	}

	public boolean hasMoreData()
	{
		return false;
	}

	public synchronized void loadMoreData()
	{
	}

	public int getCount()
	{
		return list.size();
	}

	@Override
	public int getViewTypeCount()
	{
		return 6;
	}

	@Override
	public int getItemViewType(int position)
	{
		return VIEW_TYPE_TOP;
	}

	@Override
	public ChannelListItem getItem(int position)
	{
		if (position >= 0 && position < list.size())
			return list.get(position);
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			ChatChannel channel = (ChatChannel) getItem(position);
			if (channel == null)
				return null;

            if (fragment instanceof ChatFragment)
            {
                if(ChatServiceController.getInstance().isArOrPrGameLang()){
                    convertView = View.inflate(context, R.layout.ar__channel_list_item_mail, null);
                }else{
                    convertView = View.inflate(context, R.layout.cs__channel_list_item_mail, null);
                }
                convertView.setTag(new MailViewHolder(convertView));
            }

			int bgColor = 0;
			CategoryViewHolder holder = (CategoryViewHolder) convertView.getTag();
			holder.setContent(context, channel, true, null, channel.nameText, channel.contentText, channel.getChannelTime(),
					false, position, bgColor,0);

			holder.item_icon.setTag(channel.channelID);
			holder.setChatRoomColor();
		}
		return convertView;
	}

	public void notifyDataSetChangedOnUI()
	{
		if (context == null)
			return;

		context.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					notifyDataSetChanged();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public void destroy()
	{
		list.clear();
		notifyDataSetChanged();
		fragment = null;
		context = null;
		list = null;
	}
}
