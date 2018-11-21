package com.chatsdk.view.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.ServiceInterface;
import com.chatsdk.model.ChannelListItem;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.ResUtil;
import com.chatsdk.view.ChannelListActivity;
import com.chatsdk.view.ChannelListFragment;

public class SysMailAdapter extends AbstractMailListAdapter
{
	public ChatChannel	parentChannel;

	public SysMailAdapter(ChannelListActivity context, ChannelListFragment fragment)
	{
		super(context, fragment);

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
			int dbCount = parentChannel.getSysMailCountInDB();
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "dbCount",dbCount, "parentChannel.mailDataList", parentChannel.mailDataList.size());
			return dbCount > parentChannel.mailDataList.size();
		}
		
	}

	public synchronized void loadMoreData()
	{
		MailData lastItem = null;
		if (parentChannel.mailDataList.size() > 0)
		{
			lastItem = parentChannel.mailDataList.get(parentChannel.mailDataList.size() - 1);
		}
		ChannelManager.getInstance().loadMoreSysMailFromDB(parentChannel, lastItem != null ? lastItem.getCreateTime() : -1);
	}
	
	public synchronized void loadMoreData(boolean b)
	{
		ChannelManager.getInstance().loadMoreSysMailFromDB(parentChannel, -1);
	}
	
	public void reloadData()
	{
		parentChannel = ChannelManager.getInstance().getChannel(context.channelType, fragment.channelId);
		if (list.size() < ChannelManager.LOAD_MORE_COUNT && parentChannel.mailDataList.size() < ChannelManager.LOAD_MORE_COUNT
				&& hasMoreData())
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "reloadData1", list.size(), "hasMoreData", hasMoreData(),"isReLoad");
			context.showProgressBar();
			isLoadingMore = true;
			loadMoreData();
		}
		else
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "reloadData3", list.size(), "hasMoreData", hasMoreData(),"isReLoad");
			refreshAdapterList();
		}


		if(ChatServiceController.mail_all_delete)
		{
			ChannelManager.getInstance().getAllSysMailFromDB(parentChannel);
			allMaillist.clear();
			if (context.channelType != -1 && !fragment.channelId.equals(""))
			{
				if (parentChannel != null)
				{
					allMaillist.addAll(parentChannel.getAllMailDataList());
				}
			}
		}


	}
	
	public void refreshAdapterList()
	{
		list.clear();
		if (context.channelType != -1 && !fragment.channelId.equals(""))
		{
			if (parentChannel != null)
			{
				list.addAll(parentChannel.getMailDataList());
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "mailDataList:",parentChannel.mailDataList.size());
			}
		}
		refreshOrder();
		fragment.setNoMailTipVisible(list.size() <= 0);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		MailData mailData = (MailData) getItem(position);
		if (mailData == null)
			return null;

//		parentChannel.refreshRenderData();
		convertView = super.getView(position, convertView, parent);

		convertView.setBackgroundDrawable(null);
		if(ChannelManager.channelReadedMails !=null){
			if(ChannelManager.channelReadedMails.equals(mailData.getUid()))
				convertView.setBackgroundColor(Color.WHITE);
		}
		CategoryViewHolder holder = (CategoryViewHolder) convertView.getTag();
		int bgColor = MailManager.getColorByChannelId(parentChannel.channelID);
		holder.setContent(context, mailData, false, null, mailData.nameText, mailData.contentText, mailData.getCreateTime(),
				fragment.isInEditMode(), position, bgColor,mailData.failTime);
		setIcon(mailData, holder.item_icon);
		refreshMenu();

		return convertView;
	}

	@Override
	public int getItemViewType(int position)
	{
		ChannelListItem item = getItem(position);
		if (item != null)
		{
			if (item.isUnread())
				return VIEW_TYPE_READ_AND_DELETE;
			else
				return VIEW_TYPE_DELETE;
		}
		return VIEW_TYPE_READ_AND_DELETE;
	}

	private void setIcon(MailData mailData, ImageView iconView)
	{
		String mailIcon = mailData.mailIcon;
		if (mailIcon.equals(""))
		{
			int defaultId = ResUtil.getId(context, "drawable", "g026");
			try
			{
				if (defaultId != 0)
					iconView.setImageDrawable(context.getResources().getDrawable(defaultId));
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
		else
		{
			int idFlag = ResUtil.getId(context, "drawable", mailIcon);
			try
			{
				if (idFlag != 0)
				{
					// 极少情况可能发生 Fatal Exception: java.lang.OutOfMemoryError
					// ，且没有被try捕获
					iconView.setImageDrawable(context.getResources().getDrawable(idFlag));
				}
				else
				{
					int defaultId = ResUtil.getId(context, "drawable", "g026");
					if (defaultId != 0)
						iconView.setImageDrawable(context.getResources().getDrawable(defaultId));
				}
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	public void destroy()
	{
		parentChannel = null;
		super.destroy();
	}
}
