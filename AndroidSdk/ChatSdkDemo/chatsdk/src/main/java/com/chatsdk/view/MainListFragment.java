package com.chatsdk.view;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.model.ChannelListItem;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.TimeManager;
import com.chatsdk.model.db.DBManager;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.ScaleUtil;
import com.chatsdk.view.adapter.MainChannelAdapter;
import com.chatsdk.view.adapter.MsgChannelAdapter;

public class MainListFragment extends ChannelListFragment
{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.activity = (ChannelListActivity) getActivity();
		channelListActivity = (ChannelListActivity) activity;

		Bundle extras = this.activity.getIntent().getExtras();
		if (extras != null)
		{
			if (extras.containsKey("channelId"))
				channelId = extras.getString("channelId");
		}
		else
		{
			channelId = "";
		}

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	/**
	 * 不override的话，父类的onViewCreated会调两次
	 */
	public void onViewCreated(final View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		showWriteButton();

	}

	protected void showWriteButton()
	{
        getEditButton().setVisibility(View.GONE);
        getReturnButton().setVisibility(View.GONE);
		getReturnGameUIButton().setVisibility(View.VISIBLE);

		if (ChatServiceController.chat_v2_personal){
			getWriteButton().setVisibility(View.GONE);
			getContactsButton().setVisibility(View.GONE);
		}
		else
		{
//			if(ChatServiceController.mail_all_read){
				getWriteButton().setVisibility(View.GONE);
//			}else{
//				getWriteButton().setVisibility(View.VISIBLE);
//				getWriteButton().setOnClickListener(new View.OnClickListener()
//				{
//					@Override
//					public void onClick(View view)
//					{
//						writeNewMail();
//					}
//				});
//			}

			if (ChatServiceController.convenient_contact) {
				getContactsButton().setVisibility(View.VISIBLE);

				getContactsButton().setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						openContactsView();
					}
				});
			}
		}
	}
	
//	public static boolean canJumpToSecondaryList()
//	{
//		return ChatServiceController.rememberSecondChannelId && StringUtils.isNotEmpty(ChatServiceController.lastSecondChannelId);
//	}

	protected void jumpToSecondaryList()
	{
//		if (canJumpToSecondaryList())
//		{
//			ServiceInterface.showChannelListActivity(channelListActivity, true, DBDefinition.CHANNEL_TYPE_OFFICIAL, ChatServiceController.lastSecondChannelId,
//					true);
//			ChatServiceController.rememberSecondChannelId = false;
//			ChatServiceController.lastSecondChannelId = "";
//			return;
//		}
	}

	protected void refreshChannel()
	{
		notifyDataSetChanged();
		dataChanged = false;
	}

	protected void createList()
	{
		if (StringUtils.isNotEmpty(channelId)
				&& (channelId.equals(MailManager.CHANNELID_MOD) || channelId.equals(MailManager.CHANNELID_MESSAGE)))
		{
			adapter = new MsgChannelAdapter(channelListActivity, this, channelId);
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "main createList1:",adapter.list.size());
		}
		else
		{
            adapter = new MainChannelAdapter(channelListActivity, this);
            LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "main createList3:",adapter.list.size());
		}
		super.createList();
	}

	public MainChannelAdapter getMainChannelAdapter()
	{
		if (adapter != null && adapter instanceof MainChannelAdapter)
		{
			return (MainChannelAdapter) adapter;
		}
		return null;
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	protected void restorePosition()
	{
		int lastX = lastScrollX;
		int lastY = lastScrollY;
		if (lastX != -1)
		{
			mListView.setSelectionFromTop(lastX, lastY);
		}
		lastScrollX = lastScrollY = -1;
	}

	protected void onDeleteMenuClick(int position)
	{
		deleteChannel(position);
	}

	protected void onReadMenuClick(int channel)
	{
		if (adapter instanceof MainChannelAdapter)
		{
			readMainChannel(channel);
		}
		else if (adapter instanceof MsgChannelAdapter)
		{
			readChannel(channel);
		}
	}


	protected void readMainChannel(int position)
	{
		ChatChannel channel = (ChatChannel) adapter.getItem(position);
		if (channel != null)
		{
			if (channel.channelID.equals(MailManager.CHANNELID_MESSAGE))
			{
				List<ChatChannel> messageChannelArr = ChannelManager.getInstance().getAllMsgChannel();
				if (messageChannelArr != null && messageChannelArr.size() > 0)
				{
					for (int i = 0; i < messageChannelArr.size(); i++)
					{
						ChatChannel messageChannel = messageChannelArr.get(i);
						if (messageChannel != null)
						{
							messageChannel.markAsRead();
						}
					}

				}

				ChatChannel msgChannel = ChannelManager.getInstance().getMessageChannel();
				if (msgChannel != null)
				{
					msgChannel.unreadCount = 0;
					msgChannel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
					DBManager.getInstance().updateChannel(msgChannel);
					ChannelManager.getInstance().calulateAllChannelUnreadNum();
				}
				JniController.getInstance().excuteJNIVoidMethod("readDialogMail",
						new Object[] { Integer.valueOf(1), Boolean.valueOf(false), "0,1,20" });
			}
			else
			{
				String uids = channel.getMailUidsByConfigType(DBManager.CONFIG_TYPE_READ);
				if (StringUtils.isNotEmpty(uids))
				{
					JniController.getInstance().excuteJNIVoidMethod("readMutiMail", new Object[] { uids });
				}
			}
		}
	}

	protected void openItem(ChannelListItem item)
	{
		if (item != null && item instanceof ChatChannel)
		{
			openChannel((ChatChannel) item);
		}
	}

	public void saveState()
	{
		if (inited && getCurrentPos() != null)
		{
			lastScrollX = getCurrentPos().x;
			lastScrollY = getCurrentPos().y;
		}
	}

	public void comfirmOperateMutiMail(List<ChannelListItem> checkedItems, int type)
	{
		if (type == ChannelManager.OPERATION_DELETE_MUTI)
			actualDeleteChannels(checkedItems);
		else if (type == ChannelManager.OPERATION_REWARD_MUTI)
			actualRewardChannels(checkedItems);

		refreshUI();
	}

	public void onDestroy()
	{
		if (getWriteButton() != null)
		{
			getWriteButton().setOnClickListener(null);
		}
		if (ChatServiceController.convenient_contact) {
			if (getContactsButton() != null)
			{
				getContactsButton().setOnClickListener(null);
			}
		}
		super.onDestroy();
	}
}
