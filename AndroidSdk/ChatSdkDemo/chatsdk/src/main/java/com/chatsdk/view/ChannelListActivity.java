package com.chatsdk.view;

import android.os.Bundle;

import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.model.MailManager;
import com.chatsdk.util.ImageUtil;
import com.chatsdk.util.LogUtil;
import com.chatsdk.view.actionbar.MyActionBarActivity;

public class ChannelListActivity extends MyActionBarActivity
{
	public int	channelType;

	public ChannelListFragment getFragment()
	{
		return (ChannelListFragment) fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Bundle extras = getIntent().getExtras();

		boolean isSecondLvList = false;
		boolean isGoBack = false;
		String channelId = "";
		if (extras != null)
		{
			this.bundle = new Bundle(extras);
			if (extras.containsKey("channelType"))
			{
				channelType = extras.getInt("channelType");
//				ChatServiceController.setCurrentChannelType(channelType);
			}
			
			if (extras.containsKey("isSecondLvList"))
			{
				isSecondLvList = extras.getBoolean("isSecondLvList");
			}
			if (extras.containsKey("isGoBack"))
			{
				isGoBack = extras.getBoolean("isGoBack");
			}

			if (extras.containsKey("channelId"))
			{
				channelId = extras.getString("channelId");
			}
		}

		if (!isSecondLvList)
		{
			if (channelId.equals(MailManager.CHANNELID_MOD) || channelId.equals(MailManager.CHANNELID_MESSAGE))
			{
				if(!isGoBack) LogUtil.trackPageView("ShowChannelList-" + channelId);
				fragmentClass = MsgMailListFragment.class;
			}
			else if(channelId.equals(MailManager.CHANNELID_EVENT))
			{
				if(!isGoBack) LogUtil.trackPageView("ShowChannelList-" + channelId);
				fragmentClass = EventMainListFragment.class;
			}
			else
			{
				if(!isGoBack && !ChatServiceController.canJumpToSecondaryList())
				{
					LogUtil.trackPageView("ShowChannelList");
				}
				fragmentClass = MainListFragment.class;
			}
		}
		else
		{
			if(!isGoBack) LogUtil.trackPageView("ShowChannelList-" + channelId);
			fragmentClass = SysMailListFragment.class;
		}

		ChatServiceController.toggleFullScreen(true, true, this);

		super.onCreate(savedInstanceState);
	}
	
	protected void showBackground()
	{
		ImageUtil.setYRepeatingBG(this, fragmentLayout, R.drawable.mail_list_bg);
	}

	@Override
	public void onResume()
	{
		super.onResume();

//		ChatServiceController.setCurrentChannelType(channelType);
	}

	@Override
	public void onDestroy()
	{
		if (ChatServiceController.isReturningToGame && !ChannelListFragment.preventSecondChannelId)
		{
			ChatServiceController.rememberSecondChannelId = true;
		}
		else
		{
			ChatServiceController.rememberSecondChannelId = false;
		}
		super.onDestroy();

//		System.gc();
//		System.runFinalization();
	}

	public void onBackButtonClick()
	{
		if (fragment != null && fragment instanceof ChannelListFragment && ((ChannelListFragment) fragment).handleBackPressed())
		{
			return;
		}
		super.onBackButtonClick();
	}

	@Override
	public void onBackPressed()
	{
		if (fragment != null && fragment instanceof ChannelListFragment && ((ChannelListFragment) fragment).handleBackPressed())
		{
			return;
		}
		super.onBackPressed();
	}

	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus)
		{
			// 从这里调的话，其它没问题，但退出系统邮件后，两层activity的打开动画都会看到，不如onResume看起来只打开了一层
			// getFragment().onBecomeVisible();
		}
		else
		{

		}
	}

	public void hideProgressBar()
	{
		// 首次进入列表，加载系统邮件时，防止关掉进度圈
		if (!(getFragment() != null && getFragment().adapter != null && getFragment().adapter.isLoadingMore))
		{
			super.hideProgressBar();
		}
	}
}
