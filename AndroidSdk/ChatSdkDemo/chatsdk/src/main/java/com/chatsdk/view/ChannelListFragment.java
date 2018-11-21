package com.chatsdk.view;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.swipemenulistview.SwipeMenu;
import com.swipemenulistview.SwipeMenuCreator;
import com.swipemenulistview.SwipeMenuItem;
import com.swipemenulistview.SwipeMenuListView;
import com.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.controller.MenuController;
import com.chatsdk.controller.ServiceInterface;
import com.chatsdk.model.ChannelListItem;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.FlyMutiRewardInfo;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.TimeManager;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.db.DBManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.ScaleUtil;
import com.chatsdk.view.actionbar.ActionBarFragment;
import com.chatsdk.view.adapter.AbstractMailListAdapter;
import com.chatsdk.view.adapter.MainChannelAdapter;
import com.chatsdk.view.adapter.MsgChannelAdapter;
import com.chatsdk.view.adapter.SysMailAdapter;
import com.pullrefresh.PullToRefreshBase;
import com.pullrefresh.PullToRefreshBase.OnRefreshListener;
import com.pullrefresh.PullToRefreshSwipeListView;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChannelListFragment extends ActionBarFragment
{
	protected AbstractMailListAdapter		adapter				= null;

	protected SwipeMenuListView				mListView;
	protected PullToRefreshSwipeListView	channelListPullView;
	protected TextView						tip_no_mail_textView;
	private RelativeLayout					channelListFragmentLayout;

	private RelativeLayout					mailButtonBarLayout;
	//private ImageView						mailButtonBarWrite;
//	private ImageView						mailButtonBarReward;
	private LinearLayout					mailCheckBoxlayout;
	private View							mailButtonBarAll;
	private TextView						checkboxLabel;
	private Button							mailButtonBarDelete;

	private ImageView						mailButtonBarDeleteAll; //服务于一键删除
	private Button							mailButtonSendMail; //服务于一键删除
	private Button							mailButtonBarReadAll; //服务于一键已读

	public String							channelId			= "";
	private boolean							allSelectedValue	= false;
	private boolean							isSecondLvList = false;

	protected ChannelListActivity			channelListActivity;

	public boolean isInEditMode()
	{
		return isInEditMode;
	}

	public SwipeMenuListView getListView()
	{
		return mListView;
	}

	private Button cs__action_enterEditButton ,cs__action_returnEditButton;

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
		if (channelId.equals(MailManager.CHANNELID_MOD))
		{
			ChatServiceController.isContactMod = true;
		}
		else
		{
			ChatServiceController.isContactMod = false;
		}
		int layoutId;
		if(ChatServiceController.getInstance().isArOrPrGameLang()){
			layoutId = R.layout.ar__channel_list_new;
		}else{
			layoutId = R.layout.cs__channel_list_new;
		}
		return inflater.inflate(layoutId, container, false);
	}

	protected static boolean	dataChanged	= false;

	public static void onChannelAdd()
	{
		dataChanged = true;
		if (ChatServiceController.getChannelListFragment() != null)
		{
			try
			{
				// 少量NullPointerException异常
				ChatServiceController.getChannelListFragment().reload();
				dataChanged = false;
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	public static void onMailAdded()
	{
		// TODO 判断是否已经在一级列表中
		onChannelAdd();
	}

	public static void onMsgAdded(ChatChannel channel)
	{
		channel.refreshRenderData();
		ChannelManager.getInstance().addToLoadedChannel(channel);

		ChannelListFragment fragment = ChatServiceController.getMsgListFragment();
		if (fragment != null && fragment.adapter != null && fragment.adapter.list != null)
		{
			for (int i = 0; i < fragment.adapter.list.size(); i++)
			{
				ChannelListItem item = fragment.adapter.list.get(i);
				if (item instanceof ChatChannel && channel.channelID.equals(((ChatChannel) item).channelID))
				{
					dataChanged = false;
					fragment.adapter.refreshOrder();
					return;
				}
			}

			// 重新加载
			onChannelAdd();
		}

		// 如果处于主界面，未读数变了，需要刷新界面
		if (ChatServiceController.getMainListFragment() != null)
		{
			ChatServiceController.getMainListFragment().notifyDataSetChanged();
		}
	}

	private static void refreshModChannel(ChatChannel chatChannel)
	{
		if (chatChannel != null && chatChannel.isModChannel())
			dataChanged = true;
	}

	public static void onChannelRefresh()
	{
		dataChanged = true;
		if (ChatServiceController.getMainListFragment() != null && ChatServiceController.getMainListFragment().adapter != null)
		{
			ChatServiceController.getMainListFragment().refreshChannel();
		}
	}

	public static void onMailDataAdded(final MailData mailData)
	{
		dataChanged = true;
		if (ChatServiceController.getSysMailListFragment() != null)
		{
			ChatServiceController.getSysMailListFragment().refreshMailDataList(mailData);
		}
	}

	public static void onMailDataRefresh(final MailData mailData)
	{
		dataChanged = true;
		if (ChatServiceController.getSysMailListFragment() != null)
		{
			ChatServiceController.getSysMailListFragment().updateMailDataList(mailData);
		}
	}

	public void reload()
	{
		if (adapter != null && activity != null)
		{
			activity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						adapter.reloadData();
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}
	}

	public void onResume()
	{
		super.onResume();

		refreshTitleLabel();

		if (!this.inited)
		{
			activity.showProgressBar();
			onBecomeVisible();
			return;
		}

		if (dataChanged || ChannelManager.getInstance().isInRootChannelList)
		{
			reload();
			dataChanged = false;
		}
		else
		{
			if (adapter != null)
				adapter.refreshOrder();
		}
		if (ChannelManager.getInstance().isInRootChannelList)
			ChannelManager.getInstance().isInRootChannelList = false;
	}

	public void refreshScrollLoadEnabled()
	{
		channelListPullView.setPullLoadEnabled(false);
		channelListPullView.setPullRefreshEnabled(false);

		channelListPullView.setScrollLoadEnabled(false);
	}

	public void setNoMailTipVisible(boolean isVisble)
	{
		if (tip_no_mail_textView != null)
			tip_no_mail_textView.setVisibility(isVisble ? View.VISIBLE : View.GONE);
	}

	private boolean	isInEditMode	= false;

	public void onViewCreated(final View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		Intent intent = this.getActivity().getIntent();//add at 20171031 for get isSecondLvList from intent
		if(intent!=null){
			isSecondLvList = intent.getBooleanExtra("isSecondLvList",false);
		}
		if(channelId.equals(MailManager.CHANNELID_MESSAGE) || channelId.equals(MailManager.CHANNELID_MOD)){
			isSecondLvList = true;
		}
		channelListFragmentLayout = (RelativeLayout) view.findViewById(R.id.channelListFragmentLayout);

		channelListPullView = (PullToRefreshSwipeListView) view.findViewById(R.id.channelList_PullView);
		channelListPullView.setLanguage(LanguageManager.getLangByKey(LanguageKeys.TIP_LOADING));
		// 极少量RuntimeException com.swipemenulistview.SwipeMenuListView
		// cannot be cast to com.swipemenulistview.SwipeMenuListView
		mListView = channelListPullView.getRefreshableView();
		mListView.setCacheColorHint(0x00000000);
        mListView.setDivider(null);

		channelListPullView.setOnRefreshListener(new OnRefreshListener<SwipeMenuListView>()
		{
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<SwipeMenuListView> refreshView)
			{
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<SwipeMenuListView> refreshView)
			{
				if (adapter != null && adapter.hasMoreData())
				{
					LogUtil.trackPageView("LoadMoreList-" + channelId);
					adapter.loadMoreData();
				}
			}
		});

		tip_no_mail_textView = (TextView) view.findViewById(R.id.tip_no_mail);
		if (tip_no_mail_textView != null)
		{
			tip_no_mail_textView.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NO_MAIL));
			tip_no_mail_textView.setVisibility(View.GONE);
		}

		mailButtonBarLayout = (RelativeLayout) view.findViewById(R.id.mailButtonBarLayout);
//		mailButtonBarWrite = (ImageView) view.findViewById(R.id.mailButtonBarWrite);
//		mailButtonBarReward = (ImageView) view.findViewById(R.id.mailButtonBarReward);
		mailCheckBoxlayout = (LinearLayout) view.findViewById(R.id.channel_item_checkbox_layout);
		mailButtonBarAll = view.findViewById(R.id.mailButtonBarAll);
		checkboxLabel = (TextView) view.findViewById(R.id.checkboxLabel);
		mailButtonBarDelete = (Button) view.findViewById(R.id.mailButtonBarDelete);
		mailButtonBarDelete.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_DELETE));//删除
		mailButtonSendMail = (Button) view.findViewById(R.id.mailButtonSendMail);
		mailButtonSendMail.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_SEND_MAIL));//删除
		mailButtonBarReadAll= (Button) view.findViewById(R.id.mailButtonBarReadAll);
		mailButtonBarReadAll.setText(LanguageManager.getLangByKey(LanguageKeys.MAIL_BAR_ONEKEY_READALL));//设置一键已读文字
		cs__action_enterEditButton = (Button) view.findViewById(R.id.cs__action_enterEditButton);//进入编辑按钮实例化
		cs__action_returnEditButton = (Button) view.findViewById(R.id.cs__action_returnEditButton);//退出编辑按钮实例化
		if (ChatServiceController.mail_all_read)
		{
			showBottomBar(true);//motify at 20171027
		}else{
			showBottomBar(false);//motify at 20171027
		}
		refreshTitleLabel();
		checkboxLabel.setText(LanguageManager.getLangByKey(LanguageKeys.LABEL_CHECK_ALL));
//		getTitleLabel().setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View view)
//			{
//				 ChatServiceController.getInstance().host.testMailCommand();
				// String test =
				// "{\"flyToolReward\":[{\"itemPic\":\"item012.png\",\"itemNum\":1},"
				// + "{\"itemPic\":\"item4002.png\",\"itemNum\":2},"
				// + "{\"itemPic\":\"item012.png\",\"itemNum\":1},"
				// + "{\"itemPic\":\"item400.png\",\"itemNum\":2},"
				// + "{\"itemPic\":\"item012.png\",\"itemNum\":1},"
				// + "{\"itemPic\":\"item408.png\",\"itemNum\":8},"
				// + "{\"itemPic\":\"item011.png\",\"itemNum\":9},"
				// + "{\"itemPic\":\"item201.png\",\"itemNum\":10},"
				// + "{\"itemPic\":\"item403.png\",\"itemNum\":11},"
				// + "{\"itemPic\":\"item403.png\",\"itemNum\":11},"
				// +
				// "{\"itemPic\":\"item402.png\",\"itemNum\":15}],\"flyReward\":[{\"itemPic\":\"ui_gold.png\",\"itemNum\":1},{\"itemPic\":\"item402.png\",\"itemNum\":2},{\"itemPic\":\"item400.png\",\"itemNum\":3}]}";
				// ServiceInterface.postMutiRewardItem(test);
				// ChatServiceController.getInstance().host.changeMailListSwitch(!ChatServiceController.isNewMailListEnable);
				// ChatServiceController.isNewMailListEnable =
				// !ChatServiceController.isNewMailListEnable;
//			}
//		});
		showEditButton(true);
		getEditButton().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				enterEditMode();
			}
		});
		getReturnButton().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				refreshUI();
			}
		});
		//显示返回主界面的按键
		getReturnGameUIButton().setVisibility(View.VISIBLE);//add at 20171102
		getReturnGameUIButton().setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view)
			{
				ServiceInterface.activityStackExit();
			}
		});

		cs__action_enterEditButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				enterEditMode();
			}
		});
		cs__action_returnEditButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				refreshUI();
			}
		});
		mailButtonBarAll.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				selectAll();
			}
		});
		if(mailCheckBoxlayout!= null) {
			mailCheckBoxlayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					((CheckBox) mailButtonBarAll).setChecked(!((CheckBox) mailButtonBarAll).isChecked());
					selectAll();
				}
			});
		}
		if (checkboxLabel != null)
		{
			checkboxLabel.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					((CheckBox) mailButtonBarAll).setChecked(!((CheckBox) mailButtonBarAll).isChecked());
					selectAll();
				}
			});
		}

		mailButtonBarDelete.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				operateMultiple(ChannelManager.OPERATION_DELETE_MUTI);
			}
		});
		mailButtonSendMail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				writeNewMail();
			}
		});
		mailButtonBarReadAll.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
//				在这里处理一键已读操作 modify at 20171101
				operateMultiple(ChannelManager.OPERATION_REWARD_MUTI);
			}
		});
//		mailButtonBarWrite.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View view)
//			{
//				exitEditMode();
//				writeNewMail();
//			}
//		});

//		mailButtonBarReward.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View view)
//			{
//				operateMultiple(ChannelManager.OPERATION_REWARD_MUTI);
//			}
//		});

		SwipeMenuCreator creator = new SwipeMenuCreator()
		{
			@Override
			public void create(SwipeMenu menu)
			{
				switch (menu.getViewType())
				{
					case AbstractMailListAdapter.VIEW_TYPE_DELETE:
						SwipeMenuItem deleteItem = new SwipeMenuItem(channelListActivity.getApplicationContext());
						deleteItem.setBackground(new ColorDrawable(Color.rgb(0xD0, 0x70, 0x50)));
						deleteItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.DELETE));
						deleteItem.setTitleSize(ScaleUtil.getAdjustTextSize(12, ConfigManager.scaleRatio));
						deleteItem.setTitleColor(Color.WHITE);
						deleteItem.setWidth(dp2px(ScaleUtil.getAdjustTextSize(90, ConfigManager.scaleRatio)));
						menu.addMenuItem(deleteItem);
						break;
					case AbstractMailListAdapter.VIEW_TYPE_READ_AND_DELETE:
						SwipeMenuItem readItem = new SwipeMenuItem(channelListActivity.getApplicationContext());
						readItem.setBackground(new ColorDrawable(Color.rgb(0x80, 0x80, 0x80)));
						readItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.MENU_MARKASREAD));
						readItem.setTitleSize(ScaleUtil.getAdjustTextSize(12, ConfigManager.scaleRatio));
						readItem.setTitleColor(Color.WHITE);
						readItem.setWidth(dp2px(ScaleUtil.getAdjustTextSize(90, ConfigManager.scaleRatio)));
						menu.addMenuItem(readItem);

						SwipeMenuItem deleteItem2 = new SwipeMenuItem(channelListActivity.getApplicationContext());
						deleteItem2.setBackground(new ColorDrawable(Color.rgb(0xD0, 0x70, 0x50)));
						deleteItem2.setTitle(LanguageManager.getLangByKey(LanguageKeys.DELETE));
						deleteItem2.setTitleSize(ScaleUtil.getAdjustTextSize(12, ConfigManager.scaleRatio));
						deleteItem2.setTitleColor(Color.WHITE);
						deleteItem2.setWidth(dp2px(ScaleUtil.getAdjustTextSize(90, ConfigManager.scaleRatio)));
						menu.addMenuItem(deleteItem2);
						break;
					case AbstractMailListAdapter.VIEW_TYPE_READ:
						SwipeMenuItem readItem2 = new SwipeMenuItem(channelListActivity.getApplicationContext());
						readItem2.setBackground(new ColorDrawable(Color.rgb(0x80, 0x80, 0x80)));
						readItem2.setTitle(LanguageManager.getLangByKey(LanguageKeys.MENU_MARKASREAD));
						readItem2.setTitleSize(ScaleUtil.getAdjustTextSize(12, ConfigManager.scaleRatio));
						readItem2.setTitleColor(Color.WHITE);
						readItem2.setWidth(dp2px(ScaleUtil.getAdjustTextSize(90, ConfigManager.scaleRatio)));
						menu.addMenuItem(readItem2);
						break;
					case AbstractMailListAdapter.VIEW_TYPE_NONE:
						break;
				}
			}
		};
		mListView.setMenuCreator(creator);

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			// 在本例中 arg2==arg3
			public void onItemClick(AdapterView<?> adapterView, View view, int arg2, long arg3)
			{
				onListItemClick(adapterView, view, arg2);
			}
		});

		mListView.setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			@Override
			public void onMenuItemClick(int position, SwipeMenu menu, int index)
			{

				if (adapter instanceof MainChannelAdapter)
				{
					if (index == 0)
					{
						onReadMenuClick(position);
					}
				}
				else
				{
					ChannelListItem item = adapter.getItem(position);
					if (item != null)
					{
						if (item.isUnread())
						{
							switch (index)
							{
								case 0:
									onReadMenuClick(position);
									break;
								case 1:
									onDeleteMenuClick(position);
									break;
							}
						}
						else
						{
							if (index == 0)
							{
								onDeleteMenuClick(position);
							}
						}
					}
				}

			}
		});

		onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				adjustHeight();
			}
		};
		channelListFragmentLayout.getChildAt(0).getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

		((ChannelListActivity) getActivity()).fragment = this;
	}

	private void selectAll()
	{
		if (adapter != null && adapter.list != null)
		{
			allSelectedValue = !allSelectedValue;
			for (Iterator<?> iterator = adapter.list.iterator(); iterator.hasNext();)
			{
				ChannelListItem item = (ChannelListItem) iterator.next();
				if (item != null)
					item.checked = allSelectedValue;
			}
			notifyDataSetChanged();
		}
	}

	public void refreshTitleLabel()
	{
		if (ServiceInterface.isHandlingGetNewMailMsg)
		{
			getTitleLabel().setText(LanguageManager.getLangByKey(LanguageKeys.TIP_LOADING));
		}
		else
		{
			setTitleLabel();
		}
	}

	protected void setTitleLabel()
	{
		getTitleLabel().setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_MAIL));
	}

	protected void writeNewMail()
	{
		ServiceInterface.showWriteMailActivity(channelListActivity, false, null, null, null);
	}

	protected void openContactsView()
	{
		ChatServiceController.doHostAction("showFrequntContactsView", "", "", "", true, true);
	}

	public void onLoadMoreComplete()
	{
		notifyDataSetChanged();

		if (activity != null)
		{
			activity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					try
					{
						channelListPullView.onPullDownRefreshComplete();
						channelListPullView.onPullUpRefreshComplete();
						refreshScrollLoadEnabled();

						if (adapter != null)
						{
							adapter.isLoadingMore = false;
							adapter.refreshAdapterList();
						}
						activity.hideProgressBar();
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}
	}

	protected void onBecomeVisible()
	{
		if (inited)
			return;

		jumpToSecondaryList();

		timerDelay = 0;
		startTimer();
	}

	protected void jumpToSecondaryList()
	{

	}

	protected void createList()
	{
		adapter.fragment = this;
		refreshScrollLoadEnabled();
	}

	protected void renderList()
	{
		mListView.setAdapter(adapter);
		restorePosition();
		showEditButton(true);
		activity.hideProgressBar();
	}

	protected void restorePosition()
	{
	}

	private String appendStr(String originStr, String appendStr)
	{
		String ret = originStr;
		if (StringUtils.isNotEmpty(appendStr) && !ret.contains(appendStr))
		{
			if (ret.equals(""))
				ret = appendStr;
			else
				ret += "," + appendStr;
		}
		return ret;
	}

	protected void enterEditMode()
	{
		isInEditMode = true;
		showEditButton(false);
//		showBottomBar(true);//motify at 20171027
		notifyDataSetChanged();//add at 20171102  for left select stated
	}

	protected void exitEditMode()
	{
		isInEditMode = false;
		showEditButton(true);
//		showBottomBar(false);//motify at 20171027
	}

	protected void refreshUI()
	{
		if (activity != null)
		{
			activity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					try
					{
						allSelectedValue = false;
						((CheckBox) mailButtonBarAll).setChecked(allSelectedValue);
						if (adapter != null && adapter.list != null) {
							for (Iterator<?> iterator = adapter.list.iterator(); iterator.hasNext(); ) {
								ChannelListItem item = (ChannelListItem) iterator.next();
								if (item != null)
								{
									item.checked = allSelectedValue;
								}
							}
							notifyDataSetChanged();
						}
						exitEditMode();
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}
	}

	public void notifyDataSetChanged()
	{
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(activity == null || activity.isFinishing())
					return;
				activity.hideProgressBar();
				showEditButton(!isInEditMode);
			}
		});

		try
		{
			if (adapter != null)
				adapter.notifyDataSetChangedOnUI();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void showMutiRewardFlyAnimation(FlyMutiRewardInfo rewardInfo)
	{
		if (activity != null)
			activity.showFlyMutiReward(rewardInfo);
	}

	private void openDummyChannel(ChannelListItem item, int index)
	{
		if (index % 2 == 0)
		{
			ServiceInterface.showChatActivity(channelListActivity, DBDefinition.CHANNEL_TYPE_COUNTRY, false);
		}
		else
		{
			ServiceInterface.showChannelListActivity(channelListActivity, true, DBDefinition.CHANNEL_TYPE_OFFICIAL, "dummyList", false);
		}
	}

	protected void openChannel(ChatChannel channel)
	{
		ChannelManager.getInstance().currChoseChannel = channel;
		ChatServiceController.isCreateChatRoom = false;
		// 打开具体聊天
		if (channel.channelType < DBDefinition.CHANNEL_TYPE_OFFICIAL || channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER /*|| channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM*/)
			{
				if (channel.channelID.equals(MailManager.CHANNELID_MOD) || channel.channelID.equals(MailManager.CHANNELID_MESSAGE))
				{
					if (ChatServiceController.chat_v2_on && ChatServiceController.chat_v2_personal){
						ServiceInterface.showNewChatActivity(channelListActivity,-1,false);
					}
					else
					{
						ServiceInterface.showChannelListActivity(channelListActivity, false, DBDefinition.CHANNEL_TYPE_USER, channel.channelID,
								false);
					}
				}
				else
				{
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "fromUid", channel.channelID,
							"channel.customName:", channel.getCustomName());
					int mailType = channel.isModChannel() ? MailManager.MAIL_MOD_PERSONAL : MailManager.MAIL_USER;
					ServiceInterface.setMailInfo(channel.channelID, channel.latestId, channel.getCustomName(), mailType);
					ServiceInterface.showChatActivity(channelListActivity, channel.channelType, false);
					if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER && !channel.channelID.equals(MailManager.CHANNELID_MOD)
							&& !channel.channelID.equals(MailManager.CHANNELID_MESSAGE) && StringUtils.isNotEmpty(channel.latestId))
					{
						if (ChatServiceController.isContactMod)
						{
							String fromUid = ChannelManager.getInstance().getModChannelFromUid(channel.channelID);
							JniController.getInstance()
									.excuteJNIVoidMethod("readChatMail", new Object[] { fromUid, Boolean.valueOf(true) });
						}
						else
						{
							JniController.getInstance().excuteJNIVoidMethod("readChatMail",
									new Object[] { channel.channelID, Boolean.valueOf(false) });
						}
					}
				}
			}
			else
			{
				ServiceInterface.showChatActivity(channelListActivity, channel.channelType, false);
			}
		}
		// 打开二级列表
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if(channel.channelID.equals(MailManager.CHANNELID_FIGHT)){
				int num = channel.unreadCount;
				JniController.getInstance().excuteJNIVoidMethod("updateBattleBallsState",new Object[]{Boolean.valueOf(false),Integer.valueOf(num)});
			}
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "channelID", channel.channelID);
			if (channel.channelID.equals(MailManager.CHANNELID_RESOURCE)
					|| channel.channelID.equals(MailManager.CHANNELID_RESOURCE_HELP)
					|| channel.channelID.equals(MailManager.CHANNELID_KNIGHT)
					|| channel.channelID.equals(MailManager.CHANNELID_MISSILE)
					|| channel.channelID.equals(MailManager.CHANNELID_GIFT))
			{
				MailData mail = null;
				if (channel.channelID.equals(MailManager.CHANNELID_GIFT))
					mail = channel.getGiftMailData();
				else if (channel.channelID.equals(MailManager.CHANNELID_MISSILE))
					mail = channel.getMissleMailData();
				else if (channel.channelID.equals(MailManager.CHANNELID_RESOURCE))
					mail = channel.getResourceMailData();
				else if (channel.channelID.equals(MailManager.CHANNELID_KNIGHT)){
					channel.removeMailDataList();
					mail = channel.getKnightMailData();
				}
				else if (channel.channelID.equals(MailManager.CHANNELID_RESOURCE_HELP))
					mail = channel.getResourceHelpMailData();
				if (mail != null)
				{
					LogUtil.trackPageView("openChannel-" + channel.channelID);
					transportAndShowMailData(mail);
					if (channel.channelID.equals(MailManager.CHANNELID_KNIGHT))
					{
						
						String uids = channel.getMailUidsByConfigType(DBManager.CONFIG_TYPE_READ);
						LogUtil.trackPageView("getMailUidsByConfigType+" +uids);
						if (StringUtils.isNotEmpty(uids))
						{
							JniController.getInstance().excuteJNIVoidMethod("readMutiMail", new Object[] { uids });
						}
					}
					else
					{
						JniController.getInstance().excuteJNIVoidMethod("readDialogMail",
								new Object[] { Integer.valueOf(mail.getType()), Boolean.valueOf(false), "" });
					}
				}
				else
				{
					LogUtil.printVariablesWithFuctionName(Log.WARN, LogUtil.TAG_ALL, "resource or monster mail is null！");
				}
			}
			else
			{
				ServiceInterface.showChannelListActivity(channelListActivity, true, DBDefinition.CHANNEL_TYPE_OFFICIAL, channel.channelID,
						false);
			}
		}
	}

	private void transportAndShowMailData(MailData mailData)
	{
		if (mailData != null)
		{
			if (MailManager.getInstance().isInTransportedMailList(mailData.getUid()))
			{
				System.out.println("transportAndShowMailData isInTransportedMailList");
				MailManager.getInstance().setShowingMailUid("");
				ChatServiceController.doHostAction("showMailPopup", mailData.getUid(), "", "", true, true);
			}
			else
			{
				System.out.println("transportAndShowMailData not isInTransportedMailList:" + mailData.getUid());
				MailManager.getInstance().setShowingMailUid(mailData.getUid());
				MailManager.getInstance().transportMailData(mailData);
				activity.showProgressBar();
			}

//			if (!mailData.getChannelId().equals(MailManager.CHANNELID_RESOURCE)
//					&& !mailData.getChannelId().equals(MailManager.CHANNELID_KNIGHT)
//					&& !mailData.getChannelId().equals(MailManager.CHANNELID_MONSTER)
//					&& !mailData.getChannelId().equals(MailManager.CHANNELID_GIFT)
//			        && !mailData.getChannelId().equals(MailManager.CHANNELID_MISSILE))
//			{
//				MailManager.getInstance().transportNeiberMailData(mailData, true, true);
//			}

//			MailManager.getInstance().transportMailData(mailData);
//			if (!mailData.getChannelId().equals(MailManager.CHANNELID_RESOURCE)
//					&& !mailData.getChannelId().equals(MailManager.CHANNELID_KNIGHT)
//					&& !mailData.getChannelId().equals(MailManager.CHANNELID_MONSTER)
//					&& !mailData.getChannelId().equals(MailManager.CHANNELID_MISSILE))
//			{
//				MailManager.getInstance().transportNeiberMailData(mailData, true, true);
//			}
//			ChatServiceController.doHostAction("showMailPopup", mailData.getUid(), "", "", true, true);
		}
	}

	protected void openMail(MailData mailData)
	{
		if (mailData != null)
		{
			LogUtil.trackPageView("ShowSysMail-" + mailData.channelId);
			ChannelManager.getInstance().settingMailReaded(mailData);
			transportAndShowMailData(mailData);
			if (mailData.isUnread())
			{
				// 更新mail
				mailData.setStatus(1);
				JniController.getInstance().excuteJNIVoidMethod("readMail",
						new Object[] { mailData.getUid(), Integer.valueOf(mailData.getType()) });
				DBManager.getInstance().updateMail(mailData);

				// 更新channel
				ChatChannel parentChannel = ((SysMailAdapter) adapter).parentChannel;
				if (parentChannel.unreadCount > 0)
				{
					parentChannel.unreadCount--;
					ChannelManager.getInstance().calulateAllChannelUnreadNum();
				}
				parentChannel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
				DBManager.getInstance().updateChannel(parentChannel);
			}
		}
	}

	private void open(ApplicationInfo item)
	{
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(item.packageName);
		List<ResolveInfo> resolveInfoList = channelListActivity.getPackageManager().queryIntentActivities(resolveIntent, 0);
		if (resolveInfoList != null && resolveInfoList.size() > 0)
		{
			ResolveInfo resolveInfo = resolveInfoList.get(0);
			String activityPackageName = resolveInfo.activityInfo.packageName;
			String className = resolveInfo.activityInfo.name;

			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			ComponentName componentName = new ComponentName(activityPackageName, className);

			intent.setComponent(componentName);
			startActivity(intent);
			channelListActivity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		}
	}

	public boolean handleBackPressed()
	{
		if (isInEditMode)
		{
			refreshUI();
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * @param show   true/false 展示  进入/退出编辑  按钮
	 * */
	public void showEditButton(boolean show)
	{
		try{

			cs__action_enterEditButton.setVisibility(show && isSecondLvList ? View.VISIBLE : View.GONE);
			cs__action_returnEditButton.setVisibility(!show && isSecondLvList ? View.VISIBLE : View.GONE);
//		getEditButton().setVisibility(show ? View.VISIBLE : View.GONE);
//		getReturnButton().setVisibility(!show ? View.VISIBLE : View.GONE);
			mailButtonBarAll.setVisibility(!show && isSecondLvList ? View.VISIBLE : View.GONE);//add at 20171027
			checkboxLabel.setVisibility(!show && isSecondLvList ? View.VISIBLE : View.GONE);//add at 20171027
			mailButtonBarReadAll.setVisibility(View.GONE);
			if(isSecondLvList) {
				if (show &&
						ChannelManager.getInstance().currChoseChannel != null &&
						(ChannelManager.getInstance().currChoseChannel.hasReward() ||
								ChannelManager.getInstance().currChoseChannel.isUnread())) {//add at 20171027
					mailButtonBarReadAll.setVisibility(View.VISIBLE);
				} else {
//				ChatChannel msgChannel = ChannelManager.getInstance().getMessageChannel();
//				if(ChatServiceController.mail_button_hide && channelId.equals(MailManager.CHANNELID_MESSAGE) && msgChannel!=null
//						&& (msgChannel.hasReward()||msgChannel.isUnread())){
//					mailButtonBarReadAll.setVisibility(View.VISIBLE);
//				}else{

					if ( ChatServiceController.mail_all_read && adapter != null && adapter.list.size() > 0) {
						int count = adapter.list.size();
						for (int i = 0; i < count; i++) {
							ChannelListItem channel = adapter.list.get(i);
							if (channel instanceof ChatChannel) {
								List<String> mailDatas = ((ChatChannel) channel).getMailUidArrayByConfigType(DBManager.CONFIG_TYPE_REWARD);
								if (channel.unreadCount > 0 || mailDatas.size() > 0) {
									mailButtonBarReadAll.setVisibility(View.VISIBLE);
									return;
								}
							}
						}
					}

					//判断是否有未领取奖励邮件

//				}
				}
			}
			if(mailButtonBarDelete == null)
				mailButtonBarDelete = (Button) getActivity().findViewById(R.id.mailButtonBarDelete);
			mailButtonBarDelete.setVisibility(!show && isSecondLvList ? View.VISIBLE : View.GONE);//add at 20171027
			mailButtonSendMail.setVisibility(show && !isSecondLvList ? View.VISIBLE : View.GONE);//add at 20171027
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void showBottomBar(boolean show)
	{
		mailButtonBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	public void adjustHeight()
	{
		adjustSizeCompleted = true;
	}

	private int dp2px(int dp)
	{
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
	}

	protected static int	lastScrollX				= -1;
	protected static int	lastScrollY				= -1;
	protected static int	secondLastScrollX		= -1;
	protected static int	secondLastScrollY		= -1;
	public static boolean	preventSecondChannelId	= false;

	protected Point getCurrentPos()
	{
		if (mListView == null)
		{
			return null;
		}
		int x = mListView.getFirstVisiblePosition();
		View v = mListView.getChildAt(0);
		int y = (v == null) ? 0 : (v.getTop() - mListView.getPaddingTop());

		return new Point(x, y);
	}

	protected void onDeleteMenuClick(int position)
	{
	}

	protected void onReadMenuClick(int channel)
	{
	}

	protected void deleteDummyItem(int position)
	{
		adapter.list.remove(position);

		notifyDataSetChanged();
	}

	protected void readDummyItem(int position)
	{
	}


	protected void readChannel(int position)
	{
		ChatChannel channel = (ChatChannel) adapter.getItem(position);
		if (channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
		{
			return;
		}

		if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			ChatServiceController.getChannelListFragment().actualReadSingleChannel(channel);
		}
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			ChatServiceController.getChannelListFragment().actualReadSingleChannel(channel);
		}
	}


	protected void deleteChannel(int position)
	{
		ChatChannel channel = (ChatChannel) adapter.getItem(position);
		if (channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
		{
			return;
		}

		if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			if(ChatServiceController.getChannelListFragment() != null){
				ChatServiceController.getChannelListFragment().actualDeleteSingleChannel(channel);
			}
		}
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			if(ChatServiceController.getChannelListFragment() != null) {
				ChatServiceController.getChannelListFragment().actualDeleteSingleChannel(channel);
			}
		}
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			boolean hasCannotDeleteMail = channel.cannotOperatedForMuti(ChannelManager.OPERATION_DELETE_MUTI);
			String content = "";
			if (hasCannotDeleteMail)
			{
				content = LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_NOTIFY_REWARD_OR_LOCK);
			}
			else
			{
				content = LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_THESE_COMFIRM);
			}
			MenuController.showDeleteChannelConfirm(content, channel);
		}
	}

	protected void deleteSysMail(int position)
	{
		if (adapter.getCount() <= 0)
			return;

		MailData mailData = (MailData) adapter.getItem(position);
		if (mailData == null)
			return;
		if (mailData.getSave() == 1)
		{
			MenuController.showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_NOTIFY_LOCK));
		}
		else
		{
			if(mailData.hasReward())
			{
//				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "rewardid", mailData.getRewardId(), "rewardStatus",
//						mailData.getRewardStatus());
//				MenuController.showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_NOTIFY_REWARD));
				List<ChannelListItem> mail = new ArrayList<ChannelListItem>();
				mail.add(mailData);
				actualDeleteSysMailsAfterReward(mail);
			}
			else
			{
				if(ChatServiceController.getChannelListFragment() != null) {
					ChatServiceController.getChannelListFragment().actualDeleteSingleSysMail(mailData);
				}
			}
		}
	}

	protected void readSysMail(int position)
	{
		if (adapter.getCount() <= 0)
			return;

		MailData mailData = (MailData) adapter.getItem(position);
		if (mailData != null)
		{
			ChatServiceController.getChannelListFragment().actualReadSingleSysMail(mailData);
		}
	}

	private void operateMultiple(int type)
	{
		ArrayList<ChannelListItem> checkedItems = new ArrayList<ChannelListItem>();

		if(adapter.list.size()<=0)
		{
			return;
		}

		if(type == ChannelManager.OPERATION_DELETE_MUTI_ALL)
		{
			boolean isChannel=false;
			ChannelListItem item_test = adapter.list.get(0);

			if (item_test instanceof ChatChannel) {
				isChannel=true;
			}

			if (isChannel)
			{
					for (Iterator<?> iterator = adapter.list.iterator(); iterator.hasNext();)
				{
					ChannelListItem item = (ChannelListItem) iterator.next();

					if (item.checked)
					{
						checkedItems.add(item);
					}
				}
			}
			else
			{
				for (Iterator<?> iterator = adapter.allMaillist.iterator(); iterator.hasNext();)
				{
					ChannelListItem item = (ChannelListItem) iterator.next();

					checkedItems.add(item);
				}
			}
		}
		else
		{
			for (Iterator<?> iterator = adapter.list.iterator(); iterator.hasNext();)
			{
				ChannelListItem item = (ChannelListItem) iterator.next();

				if(type == ChannelManager.OPERATION_REWARD_MUTI){
					checkedItems.add(item);
				}else if (item.checked)
				{
					checkedItems.add(item);
				}
			}
		}



		String content = "";
		boolean hasCannotOperateMutiMail = false;

		boolean hasMailData = false;
		for (int i = 0; i < checkedItems.size(); i++)
		{
			ChannelListItem item = checkedItems.get(i);
			if (item == null)
				continue;

			if (item instanceof ChatChannel)
			{
				ChatChannel channel = (ChatChannel) item;
				if (channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL && channel.cannotOperatedForMuti(type))
				{
					hasCannotOperateMutiMail = true;
					hasMailData = true;
					break;
				}
			}
			else if (item instanceof MailData)
			{
				MailData mailData = (MailData) item;
				if (mailData != null
						&& mailData.channel != null
						&& mailData.channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL
						&& (((type == ChannelManager.OPERATION_DELETE_MUTI||type == ChannelManager.OPERATION_DELETE_MUTI_ALL) && !mailData.canDelete()) || (type == ChannelManager.OPERATION_REWARD_MUTI && mailData
								.hasReward())))
				{
					hasCannotOperateMutiMail = true;
					hasMailData = true;
					break;
				}
			}
		}

		if (type == ChannelManager.OPERATION_REWARD_MUTI)
		{
			if (hasMailData)
				hasCannotOperateMutiMail = false;
		}

		if (hasCannotOperateMutiMail || (type == ChannelManager.OPERATION_REWARD_MUTI && !hasCannotOperateMutiMail && !hasMailData))
		{
			if (type == ChannelManager.OPERATION_REWARD_MUTI)
			{
				if(channelId.equals(MailManager.CHANNELID_MESSAGE)) {//add at 20171113 for messageChannel onekeyReadAll
					List<ChatChannel> messageChannelArr = ChannelManager.getInstance().getAllMsgChannel();
					if (messageChannelArr != null && messageChannelArr.size() > 0){
						for (int i = 0; i < messageChannelArr.size(); i++){
							ChatChannel messageChannel = messageChannelArr.get(i);
							if (messageChannel != null){
								messageChannel.markAsRead();
							}
						}
					}
					ChatChannel msgChannel = ChannelManager.getInstance().getMessageChannel();
					if (msgChannel != null){
						msgChannel.unreadCount = 0;
						msgChannel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
						DBManager.getInstance().updateChannel(msgChannel);
						ChannelManager.getInstance().calulateAllChannelUnreadNum();
					}
					JniController.getInstance().excuteJNIVoidMethod("readDialogMail",
							new Object[] { Integer.valueOf(1), Boolean.valueOf(false), "0,1,20" });
					notifyDataSetChanged();
				}else{
					actualRewardSysMails(checkedItems,type);
				}
				content = LanguageManager.getLangByKey(LanguageKeys.TIP_MAIL_NOREWARD);
				return;
			}
			else if (type == ChannelManager.OPERATION_DELETE_MUTI)
			{
				content = LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_NOTIFY_REWARD_OR_LOCK) + "\n"
						+ LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_THESE_COMFIRM);
			}
		}
		else
		{
			if (type == ChannelManager.OPERATION_REWARD_MUTI)
			{
//				content = LanguageManager.getLangByKey(LanguageKeys.TIP_REWARD_THESE_MAIL);
                if (ChatServiceController.getChannelListFragment() != null) {
                    ChatServiceController.getChannelListFragment().comfirmOperateMutiMail(checkedItems, type);
                }
                return;
			}
			else if (type == ChannelManager.OPERATION_DELETE_MUTI)
			{
				content = LanguageManager.getLangByKey(LanguageKeys.MAIL_DELETE_THESE_COMFIRM);
			}
		}

		MenuController.showOperateMutiMail(content, checkedItems, type);
	}

	public void actualDeleteSingleSysMail(MailData mailData)
	{
		JniController.getInstance().excuteJNIVoidMethod("deleteSingleMail",
				new Object[] { Integer.valueOf(mailData.tabType), Integer.valueOf(mailData.getType()), mailData.getUid(), "" });
		ChannelManager.getInstance().deleteSysMailFromChannel(mailData.channel, mailData.getUid(), false);
		adapter.list.remove(mailData);
		adapter.notifyDataSetChangedOnUI();
	}

	public void actualReadSingleSysMail(MailData mailData)
	{
		if (mailData.isUnread())
		{
			// 更新mail
			mailData.setStatus(1);
			JniController.getInstance().excuteJNIVoidMethod("readMail",
					new Object[] { mailData.getUid(), Integer.valueOf(mailData.getType()) });
			DBManager.getInstance().updateMail(mailData);

			// 更新channel
			ChatChannel parentChannel = ((SysMailAdapter) adapter).parentChannel;
			if (parentChannel.unreadCount > 0)
			{
				parentChannel.unreadCount--;
				ChannelManager.getInstance().calulateAllChannelUnreadNum();
			}
			parentChannel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
			DBManager.getInstance().updateChannel(parentChannel);
		}
		adapter.notifyDataSetChangedOnUI();
	}

	public void actualDeleteSingleChannel(ChatChannel channel)
	{
		List<ChannelListItem> channels = new ArrayList<ChannelListItem>();
		channels.add(channel);
		actualDeleteChannels(channels);
	}

	public void actualReadSingleChannel(ChatChannel channel)
	{
		if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER || channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			if (channel.channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
			{
				String fromUid = ChannelManager.getInstance().getModChannelFromUid(channel.channelID);
				JniController.getInstance().excuteJNIVoidMethod("readChatMail", new Object[] { fromUid, Boolean.valueOf(true) });
			}
			else
			{
				JniController.getInstance().excuteJNIVoidMethod("readChatMail", new Object[] { channel.channelID, Boolean.valueOf(false) });
			}
			channel.markAsRead();
		}
	}

	public void comfirmOperateMutiMail(List<ChannelListItem> checkedItems, int type)
	{
	}

	protected void actualDeleteChannels(List<ChannelListItem> channels)
	{
		String uids = "";
		for (int i = 0; i < channels.size(); i++)
		{
			if (channels.get(i) != null && channels.get(i) instanceof ChatChannel)
			{
				ChatChannel channel = (ChatChannel) channels.get(i);
				if (channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
					continue;

				if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
				{
					List<String> uidArray = channel.getChannelDeleteUidArray();
					if (uidArray.size() > 0)
					{
						String mailUid = uidArray.get(0);
						uids = appendStr(uids, mailUid);
					}

					ChannelManager.getInstance().deleteChannel(channel);
					adapter.list.remove(channel);
				}
				else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
				{
					ChannelManager.getInstance().deleteChannel(channel);
					adapter.list.remove(channel);
				}
				else if (channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
				{
					List<String> uidArray = channel.getChannelDeleteUidArray();
					String mailUids = getUidsByArray(uidArray);
					uids = appendStr(uids, mailUids);

					boolean hasCannotDeleteMail = channel.cannotOperatedForMuti(ChannelManager.OPERATION_DELETE_MUTI);
					if (hasCannotDeleteMail)
					{
						for (int j = 0; j < uidArray.size(); j++)
						{
							String mailUid = uidArray.get(j);
							if (!mailUid.equals(""))
								ChannelManager.getInstance().deleteSysMailFromChannel(channel, mailUid, true);
						}
						DBManager.getInstance().deleteSysMailChannel(channel.getChatTable());
						channel.querySysMailCountFromDB();
						ChannelManager.getInstance().calulateAllChannelUnreadNum();

					}
					else
					{
						ChannelManager.getInstance().deleteChannel(channel);
						adapter.list.remove(channel);
					}

				}
			}
		}

		afterDeleteMsgChannel();

		adapter.notifyDataSetChangedOnUI();
		if (StringUtils.isNotEmpty(uids))
		{
			JniController.getInstance().excuteJNIVoidMethod("deleteMutiMail", new Object[] { uids, "" });
		}
	}

	private void afterDeleteMsgChannel()
	{
		boolean hasMoreData = false;
		if (!ChatServiceController.isContactMod)
		{
			ChatChannel messageChannel = ChannelManager.getInstance().getMessageChannel();
			List<ChatChannel> messageChannelList = ChannelManager.getInstance().getLoadedMsgChannel();
			if (messageChannel != null && messageChannelList != null && messageChannelList.size() == 0
					&& DBManager.getInstance().isMessageChannelExist())
			{
				hasMoreData = true;
			}
		}
		else
		{
			ChatChannel modChannel = ChannelManager.getInstance().getModChannel();
			List<ChatChannel> modChannelList = ChannelManager.getInstance().getLoadedModChannel();
			if (modChannel != null && modChannelList != null && modChannelList.size() == 0 && DBManager.getInstance().isModChannelExist())
			{
				hasMoreData = true;
			}
		}

		if (hasMoreData)
			adapter.loadMoreData();
	}

	protected void actualRewardChannels(List<ChannelListItem> channels)
	{
		String uids = "";
		for (int i = 0; i < channels.size(); i++)
		{
			if (channels.get(i) != null && channels.get(i) instanceof ChatChannel)
			{
				ChatChannel channel = (ChatChannel) channels.get(i);
				if (channel != null && channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
				{
					List<String> uidArray = channel.getChannelRewardUidArray();
					String mailUids = getUidsByArray(uidArray);
					String type = channel.getChannelRewardTypes();

					uids = appendStr(uids, mailUids);
				}
			}
		}

		if (!(uids.equals("")))
		{
			JniController.getInstance().excuteJNIVoidMethod("rewardMutiMail", new Object[] { uids, "" ,false});
//			activity.showRewardLoadingPopup();
		}
	}

	protected synchronized void actualDeleteSysMails(List<ChannelListItem> sysMails)
	{
		String uids = "";

		ChatChannel channel = null;
		int canDeleteStatus = 0;
		boolean hasDetectMail = false;
		for (int i = 0; i < sysMails.size(); i++)
		{
			MailData mailData = (MailData) sysMails.get(i);
			if (mailData != null && mailData.channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
			{
				if (mailData.canDelete())
				{
					channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData.getChannelId());
					uids = appendStr(uids, mailData.getUid());
					if (channel != null && StringUtils.isNotEmpty(mailData.getUid()))
					{
						if (!hasDetectMail && (mailData.getType() == MailManager.MAIL_DETECT_REPORT||mailData.getType() == MailManager.Mail_NEW_SCOUT_REPORT_FB))
							hasDetectMail = true;
						ChannelManager.getInstance().deleteSysMailFromChannel(channel, mailData.getUid(), true);
					}
					adapter.list.remove(mailData);
					if (canDeleteStatus == 0)
						canDeleteStatus = 1;
				}
				else
				{
					if (canDeleteStatus == 1)
						canDeleteStatus = 2;
				}
			}
		}

		if (hasDetectMail)
			DBManager.getInstance().getDetectMailInfo();

		ChannelManager.getInstance().calulateAllChannelUnreadNum();

		if (canDeleteStatus == 1 || canDeleteStatus == 2) // 只能删一部分
		{
			adapter.notifyDataSetChangedOnUI();
		}

		if (channel != null && channel.mailDataList.size() == 0&&!DBManager.getInstance().hasMailDataInDB(channel.channelID))
		{
				ChannelManager.getInstance().deleteChannel(channel);
		}

		if (StringUtils.isNotEmpty(uids))
		{
			JniController.getInstance().excuteJNIVoidMethod("deleteMutiMail", new Object[] { uids, "" });
		}
	}

	protected void actualDeleteSysMailsAfterReward(List<ChannelListItem> sysMails)
	{
		//先领取有奖励的邮件,同时删除无奖励的邮件,等领取完奖励回来再删除领完奖的邮件
		String uids = "";

		for (int i = 0; i < sysMails.size(); i++)
		{
			MailData mailData = (MailData) sysMails.get(i);
			if (mailData != null && mailData.channel != null && mailData.channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
			{
				if (mailData.hasReward())
				{
					uids = appendStr(uids, mailData.getUid());
					sysMails.remove(i);
					i -= 1;
				}
			}
		}

		if (!(uids.equals("")))
		{
			JniController.getInstance().excuteJNIVoidMethod("rewardMutiMail", new Object[] { uids, "" ,true});
		}
		actualDeleteSysMails(sysMails);
	}


	protected void actualRewardSysMails(List<ChannelListItem> sysMails,int type)
	{
		String uids = "";
		String readUids = "";
		if (ChatServiceController.mail_all_read && type == ChannelManager.OPERATION_REWARD_MUTI ) {
			if(sysMails.size() >=0) {
				MailData mailData = (MailData) sysMails.get(0);
				String uidsOne = mailData.channel.getMailUidsByConfigType(DBManager.CONFIG_TYPE_READ);
				if (StringUtils.isNotEmpty(uidsOne)) {
					readUids = uidsOne;
				}
				String uidsTwo = mailData.channel.getMailUidsByConfigType(DBManager.CONFIG_TYPE_REWARD);
				if (StringUtils.isNotEmpty(uidsTwo)) {
					uids = uidsTwo;
				}
			}
		}else{
			for (int i = 0; i < sysMails.size(); i++)
			{
				MailData mailData = (MailData) sysMails.get(i);
				if (mailData != null && mailData.channel != null && mailData.channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
				{
					if (mailData.hasReward())
					{
						uids = appendStr(uids, mailData.getUid());
					}
					if (mailData.isUnread())
					{
						readUids = appendStr(readUids, mailData.getUid());
					}
				}
			}
		}

		if (!(uids.equals("")))
		{
			JniController.getInstance().excuteJNIVoidMethod("rewardMutiMail", new Object[] { uids, "", false });
		}
		if(!(readUids.equals(""))){
			JniController.getInstance().excuteJNIVoidMethod("readMutiMail", new Object[] { readUids });
		}
	}

	public static String getUidsByArray(List<String> uidArray)
	{
		String uids = "";
		for (int i = 0; i < uidArray.size(); i++)
		{
			String uid = uidArray.get(i);
			if (!uid.equals("") && !uids.contains(uid))
			{
				if (uids.equals(""))
					uids = uid;
				else
					uids += "," + uid;
			}
		}
		return uids;
	}

	protected void onListItemClick(AdapterView<?> adapterView, View view, int arg2)
	{
		ChannelListItem item = (ChannelListItem) adapterView.getItemAtPosition(arg2);
		if (item == null)
		{
			return;
		}
		if (isInEditMode)
		{
			CheckBox checkbox = (CheckBox) view.findViewById(R.id.channel_checkBox);
			item.checked = !item.checked;
			checkbox.setChecked(item.checked);
			return;
		}

		openItem(item);
	}

	protected void openItem(ChannelListItem item)
	{
	}

	private ViewTreeObserver.OnGlobalLayoutListener	onGlobalLayoutListener;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onDestroy()
	{
		if (adapter != null)
		{
			adapter.destroy();
			adapter = null;
		}

		if (mListView != null)
		{
			mListView.clearAdapter();
			mListView.setMenuCreator(null);
			mListView.setOnItemClickListener(null);
			mListView.setOnMenuItemClickListener(null);
			mListView = null;
		}

		if (channelListPullView != null)
		{
			channelListPullView.setOnRefreshListener(null);
			channelListPullView = null;
		}

//		if (mailButtonBarReward != null) {
//			mailButtonBarReward.setOnClickListener(null);
//			mailButtonBarReward = null;
//		}
		if (mailButtonBarAll != null)
		{
			mailButtonBarAll.setOnClickListener(null);
			mailButtonBarAll = null;
		}
		if (mailButtonBarDelete != null)
		{
			mailButtonBarDelete.setOnClickListener(null);
			mailButtonBarDelete = null;
		}
		if (mailButtonBarReadAll != null)
		{
			mailButtonBarReadAll.setOnClickListener(null);
			mailButtonBarReadAll = null;
		}
//		if (mailButtonBarWrite != null)
//		{
//			mailButtonBarWrite.setOnClickListener(null);
//			mailButtonBarWrite = null;
//		}
		if(mailCheckBoxlayout != null){
			mailButtonBarLayout.setOnClickListener(null);
			mailButtonBarLayout = null;
		}
		if (checkboxLabel != null)
		{
			checkboxLabel.setOnClickListener(null);
			checkboxLabel = null;
		}

		tip_no_mail_textView = null;

		mailButtonBarLayout = null;

//		if (getTitleLabel() != null)
//		{
//			getTitleLabel().setOnClickListener(null);
//		}
		if (getEditButton() != null)
		{
			getEditButton().setOnClickListener(null);
		}
		if (getReturnButton() != null)
		{
			getReturnButton().setOnClickListener(null);
		}
		if (getReturnGameUIButton() != null)
		{
			getReturnGameUIButton().setOnClickListener(null);
			getReturnGameUIButton().setVisibility(View.GONE);
		}

		if (activity != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
			if (content != null && content.getChildAt(0) != null && content.getChildAt(0).getViewTreeObserver() != null)
			{
				content.getChildAt(0).getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
			}
		}
		onGlobalLayoutListener = null;

		if (getActivity() != null)
		{
			((ChannelListActivity) getActivity()).fragment = null;
		}
		MailManager.getInstance().setShowingMailUid("");
		super.onDestroy();
	}
}
