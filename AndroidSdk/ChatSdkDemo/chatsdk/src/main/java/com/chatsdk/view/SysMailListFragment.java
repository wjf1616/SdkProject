package com.chatsdk.view;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.model.ChannelListItem;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.LogUtil;
import com.chatsdk.view.adapter.AppAdapter;
import com.chatsdk.view.adapter.SysMailAdapter;

import android.util.Log;

public class SysMailListFragment extends ChannelListFragment
{
	protected void setTitleLabel()
	{
		if (StringUtils.isNotEmpty(channelId))
		{
			ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
			if (channel != null)
				this.getTitleLabel().setText(channel.nameText);
		}
	}

	public void updateMailDataList(MailData mailData)
	{
		if (mailData != null && adapter != null && StringUtils.isNotEmpty(channelId) && mailData.getChannelId().equals(channelId))
		{
			MailData mail = null;
			boolean isInMailList = false;
			for (int i = 0; i < adapter.list.size(); i++)
			{
				mail = (MailData) adapter.list.get(i);
				if (mail != null && mail.getUid().equals(mailData.getUid()))
				{
					isInMailList = true;
					break;
				}
			}
			if (mail != null && isInMailList)
			{
				adapter.list.remove(mail);
				adapter.list.add(mailData);
				adapter.refreshOrder();
			}
		}
	}

	public void refreshMailDataList(final MailData mailData)
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (adapter != null && StringUtils.isNotEmpty(channelId) && mailData != null && StringUtils.isNotEmpty(mailData.getChannelId())
							&& mailData.getChannelId().equals(channelId))
					{
						adapter.list.add(mailData);
						adapter.refreshOrder();
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public void refreshScrollLoadEnabled()
	{
		channelListPullView.setPullLoadEnabled(false);
		channelListPullView.setPullRefreshEnabled(false);

		if (adapter != null && adapter.hasMoreData())
		{
			channelListPullView.setScrollLoadEnabled(true);
		}
		else
		{
			channelListPullView.setScrollLoadEnabled(false);
		}
	}

	protected void createList()
	{
		adapter = new SysMailAdapter(channelListActivity, this);
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "createList2:",adapter.list.size());
		super.createList();
	}

	protected void restorePosition()
	{
		int lastX = secondLastScrollX;
		int lastY = secondLastScrollY;
		if (lastX != -1)
		{
			mListView.setSelectionFromTop(lastX, lastY);
		}
		secondLastScrollX = secondLastScrollY = -1;
	}

	protected void onDeleteMenuClick(int position)
	{
		deleteSysMail(position);
	}

	@Override
	protected void onReadMenuClick(int channel)
	{
		readSysMail(channel);
	}

	protected void openItem(ChannelListItem item)
	{
		if (item instanceof MailData)
		{
			openMail((MailData) item);
		}
	}

	public void saveState()
	{
		if (ChatServiceController.rememberSecondChannelId && getCurrentPos() != null)
		{
			secondLastScrollX = getCurrentPos().x;
			secondLastScrollY = getCurrentPos().y;
		}

		if (ChatServiceController.rememberSecondChannelId)
		{
			ChatServiceController.lastSecondChannelId = channelId;
		}
	}

	public void comfirmOperateMutiMail(List<ChannelListItem> checkedItems, int type)
	{
		if (type == ChannelManager.OPERATION_DELETE_MUTI)
			actualDeleteSysMailsAfterReward(checkedItems);
		else if (type == ChannelManager.OPERATION_DELETE_MUTI_ALL)
			actualDeleteSysMailsAfterReward(checkedItems);
		else if (type == ChannelManager.OPERATION_REWARD_MUTI)
			actualRewardSysMails(checkedItems,type);
		else if(type == ChannelManager.OPERATION_REWARD_DELETE)
			actualDeleteSysMails(checkedItems);

		refreshUI();
		activity.showProgressBar();
	}

	public static void clossActivity()
	{
		if (ChatServiceController.getSysMailListFragment() != null && ChatServiceController.getSysMailListFragment().adapter != null)
		{
			ChatServiceController.getSysMailListFragment().exitEditMode();
			ChatServiceController.getSysMailListFragment().channelListActivity.onBackButtonClick();
		}
	}
}
