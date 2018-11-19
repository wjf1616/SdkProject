package com.chatsdk.view;


import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.swipemenulistview.SwipeMenu;
import com.swipemenulistview.SwipeMenuCreator;
import com.swipemenulistview.SwipeMenuItem;
import com.swipemenulistview.SwipeMenuListView;
import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.controller.MenuController;
import com.chatsdk.controller.ServiceInterface;
import com.chatsdk.model.ChannelListItem;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChannelView;
import com.chatsdk.model.ChatBanInfo;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.FallObject;
import com.chatsdk.model.FallingView;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.MsgItem;
import com.chatsdk.model.TimeManager;
import com.chatsdk.model.UserInfo;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.db.DBManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.net.WebSocketManager;
import com.chatsdk.util.CompatibleApiUtil;
import com.chatsdk.util.ImageUtil;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.ResUtil;
import com.chatsdk.util.ScaleUtil;
import com.chatsdk.view.actionbar.ActionBarFragment;
import com.chatsdk.view.adapter.ChatRoomChannelAdapter;
import com.chatsdk.view.adapter.ChatRoomListAdapter;
import com.chatsdk.view.autoscroll.ScrollText;
import com.chatsdk.view.autoscroll.ScrollTextManager;
import com.chatsdk.view.listview.ListViewLoadListener;
import com.chatsdk.view.listview.PullDownToLoadMoreView;
import com.pullrefresh.PullToRefreshBase;
import com.pullrefresh.PullToRefreshSwipeListView;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Activity.RESULT_OK;

public class ChatFragment extends ActionBarFragment
{
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	protected RelativeLayout	messagesListFrameLayout;
	private FrameLayout			noAllianceFrameLayout;
	private RelativeLayout		relativeLayout1;
	protected LinearLayout		buttonsLinearLayout;
	private RelativeLayout		unread_layout;
	private TextView			unread_count_text;
	protected LinearLayout		bottomLayout;
	protected EditText			replyField;
	private RelativeLayout		messageBox;
//	protected LinearLayout		header;
	private MenuItem			attachScreenshotMenu;
	private TextView			wordCount;
	protected Button			voice_btn;
	protected Button			addReply;
	private Button				buttonCountry;
	private Button				buttonAlliance;
	private Button				buttonChatRoom;
	private ArrayList<Button>	channelButton;
	//private ImageView			imageView1;
	//protected ImageView			imageView2;
	private Button				buttonJoinAlliance;
	private TextView			noAllianceTipText;
	private Timer				mTimer;
	private TimerTask			mTimerTask;
	private Timer				mLanterTimer;
	private TimerTask			mLanterTimerTask;
	private CheckBox			horn_checkbox;
	private LinearLayout		horn_tip_layout;
	private RelativeLayout		horn_scroll_layout;
	private TextView			horn_text_tip;
	private TextView			horn_name;
	private ScrollText			horn_scroll_text;
	private LinearLayout		tooltipLayout;
	private TextView			tooltipLabel;
	private ImageView			tooltipArrow;
	private ImageView			horn_close_btn;
	private LinearLayout		hs__dragon_chat_tip_layout;
	private TextView			dragon_chat_tip_text;
	private View 				sendRedPackage;
	private ImageView			icon_net_btn;

	private RelativeLayout		anchor_info_layout; //所有直播信息界面
	private ImageView			anchor_pic_icon;
	private TextView			listenNumText;
	private TextView			anchor_room_name;
	private TextView			anchor_state_text;
	private TextView			anchor_tip_text;
	private Button 				voice_player_btn;
	private FallingView			fall_lantern_view;
	private boolean				changeAnchorComplete = false;

	private int					oldAdapterCount				= 0;
	private int					loadMoreCount				= 0;

	protected int				loadingHeaderHeight;
	protected boolean			isKeyBoardFirstShowed		= false;
	private int					curMaxInputLength			= 500;

	public static boolean		rememberPosition			= false;

	private static String		savedText					= "";
	private boolean				isJoinAlliancePopupShowing	= false;
	public static String		gmailAccount				= "";

	public boolean				isKeyBoradShowing			= false;
	public boolean				isKeyBoradChange			= false;

	private boolean				isSelectMemberBtnEnable		= false;
	private boolean				isNeedShowWifi				= false;

	// ---------------------聊天室新增-----------------------
	protected ChatRoomListAdapter 			chatRoomAdapter = null;
	protected SwipeMenuListView 			mListView;
	protected PullToRefreshSwipeListView 	channelListPullView;
	private RelativeLayout					channelListFragmentLayout;
	protected TextView						tip_no_mail_textView;
	public String							channelId = "";
	protected static boolean				dataChanged	= false;
	protected static int	chatRoomlastScrollX				= -1;
	protected static int	chatRoomlastScrollY				= -1;
	private Handler mHandler;

	public void setNoMailTipVisible(boolean isVisble)
	{
		if (tip_no_mail_textView != null)
			tip_no_mail_textView.setVisibility(isVisble ? View.VISIBLE : View.GONE);
	}

	public static void onChannelAdd()
	{
		dataChanged = true;
		if (ChatServiceController.getChatFragment() != null)
		{
			try
			{
				// 少量NullPointerException异常
				ChatServiceController.getChatFragment().reload();
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

		ChatFragment fragment = ChatServiceController.getChatFragment();
		if (fragment != null && fragment.chatRoomAdapter != null && fragment.chatRoomAdapter.list != null)
		{
			for (int i = 0; i < fragment.chatRoomAdapter.list.size(); i++)
			{
				ChannelListItem item = fragment.chatRoomAdapter.list.get(i);
				if (item instanceof ChatChannel && channel.channelID.equals(((ChatChannel) item).channelID))
				{
					dataChanged = false;
					fragment.chatRoomAdapter.refreshOrder();
					return;
				}
			}

			// 重新加载
			onChannelAdd();
		}

		// 如果处于主界面，未读数变了，需要刷新界面
		if (ChatServiceController.getChatFragment() != null)
		{
			ChatServiceController.getChatFragment().notifyDataSetChanged();
		}
	}

	public static void onMailDataAdded(final MailData mailData)
	{
		dataChanged = true;
		if (ChatServiceController.getChatFragment() != null)
		{
			ChatServiceController.getChatFragment().refreshMailDataList(mailData);
		}
	}

	public static void onChannelRefresh()
	{
		dataChanged = true;
		if (ChatServiceController.getChatFragment() != null && ChatServiceController.getChatFragment().chatRoomAdapter != null)
		{
			ChatServiceController.getChatFragment().refreshChannel();
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
					if (chatRoomAdapter != null  && mailData != null && StringUtils.isNotEmpty(mailData.getChannelId()))
					{
						chatRoomAdapter.list.add(mailData);
						chatRoomAdapter.refreshOrder();
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});

	}

	public void actualDeleteSingleChannel(ChatChannel channel)
	{
		List<ChannelListItem> channels = new ArrayList<ChannelListItem>();
		channels.add(channel);
		actualDeleteChannels(channels);
	}

	protected void actualDeleteChannels(List<ChannelListItem> channels)
	{
		for (int i = 0; i < channels.size(); i++)
		{
			if (channels.get(i) != null && channels.get(i) instanceof ChatChannel)
			{
				ChatChannel channel = (ChatChannel) channels.get(i);
				if (channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
					continue;

				if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
				{
					ChannelManager.getInstance().deleteChannel(channel);
//					ChannelManager.getInstance().deleteChatroomChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, channel.channelID));
					if (chatRoomAdapter != null) {
						chatRoomAdapter.list.remove(channel);
					}
					ChannelManager.getInstance().calulateAllChannelUnreadNum();
				}
			}
		}

		afterDeleteMsgChannel();
	}

	private void afterDeleteMsgChannel()
	{
		boolean hasMoreData = false;
		if (!ChatServiceController.isContactMod)
		{
			ChatChannel messageChannel = ChannelManager.getInstance().getMessageChannel();
			List<ChatChannel> messageChannelList = ChannelManager.getInstance().getLoadedChatRoomChannel();
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

		if (hasMoreData && chatRoomAdapter != null)
			chatRoomAdapter.loadMoreData();
	}

	protected void refreshChannel()
	{
		notifyDataSetChanged();
		dataChanged = false;
	}

	public void refreshScrollLoadEnabled()
	{
		channelListPullView.setPullLoadEnabled(false);
		channelListPullView.setPullRefreshEnabled(false);

		channelListPullView.setScrollLoadEnabled(false);
	}

	public void reload()
	{
		if (chatRoomAdapter != null && activity != null)
		{
			activity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						chatRoomAdapter.reloadData();
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
						if (chatRoomAdapter != null)
						{
							chatRoomAdapter.isLoadingMore = false;
							chatRoomAdapter.refreshAdapterList();
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

	public void notifyDataSetChanged()
	{
		try
		{
			if (chatRoomAdapter != null)

				chatRoomAdapter.notifyDataSetChangedOnUI();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	protected void openChannel(ChatChannel channel)
	{
		ChatServiceController.isCreateChatRoom = false;
		// 打开具体聊天
		if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			//ServiceInterface.popActivity(activity);
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "fromUid", channel.channelID,
					"channel.customName:", channel.getCustomName());
			int mailType = channel.isModChannel() ? MailManager.MAIL_MOD_PERSONAL : MailManager.MAIL_USER;
			ServiceInterface.setMailInfo(channel.channelID, channel.latestId, channel.getCustomName(), mailType);
			ServiceInterface.showChatActivity(activity, channel.channelType, false);
			JniController.getInstance().excuteJNIVoidMethod("readChatMail", new Object[] { channel.channelID, Boolean.valueOf(false) });
		}
	}

	protected void onListItemClick(AdapterView<?> adapterView, View view, int arg2)
	{
		ChannelListItem item = (ChannelListItem) adapterView.getItemAtPosition(arg2);
		if (item == null)
		{
			return;
		}

		openItem(item);
	}

	protected void openItem(ChannelListItem item)
	{
		if (item != null && item instanceof ChatChannel)
		{
			openChannel((ChatChannel) item);
		}
	}

	private int dp2px(int dp)
	{
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
	}
	// ---------------------聊天室新增-----------------------

	public boolean isSelectMemberBtnEnable()
	{
		return isSelectMemberBtnEnable;
	}

	public ChannelView getCurrentChannel()
	{
		return getCurrentChannelView();
	}

	public ChatFragment()
	{
		isKeyBoardFirstShowed = false;
		initChannelViews();
	}

	public void refreshMemberSelectBtn()
	{
		if (!(ChatServiceController.isInMailDialog() && !ChatServiceController.isCreateChatRoom)) {
			isSelectMemberBtnEnable = false;
			return;
		}

		try
		{
			if(ChatServiceController.isFromBd){
				isSelectMemberBtnEnable = false;
				if(ChatServiceController.isAnchorHost && ChatServiceController.isInSelfLiveRoom()) {
					isSelectMemberBtnEnable = true;
				}
				return;
			}
			if (ChatServiceController.isInChatRoom()
					&& (UserManager.getInstance().getCurrentMail().opponentUid.equals("")
					|| !ChannelManager.getInstance().getIsMemberFlag(UserManager.getInstance().getCurrentMail().opponentUid)))
			{
				isSelectMemberBtnEnable = false;
				return;
			}
			ArrayList<String> memberUidArray = UserManager.getInstance().getSelectMemberUidArr();
			if (memberUidArray == null)
			{
				isSelectMemberBtnEnable = false;
				return;
			}

			HashMap<String, UserInfo> memberInfoMap = UserManager.getInstance().getChatRoomMemberInfoMap();
			isSelectMemberBtnEnable = true;
			if (memberUidArray == null
					|| memberUidArray.size() <= 0
					|| (memberUidArray != null && memberUidArray.size() > 0 && !memberUidArray.contains(UserManager.getInstance()
							.getCurrentUserId())))
				isSelectMemberBtnEnable = false;
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void setSelectMemberBtnState()
	{
		if (getMemberSelectButton() != null)
		{
			getMemberSelectButton().setVisibility(isSelectMemberBtnEnable ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	private void showMessageBox()
	{
		this.messageBox.setVisibility(View.VISIBLE);
		this.buttonsLinearLayout.setVisibility(View.VISIBLE);
		this.anchor_info_layout.setVisibility(View.GONE);
		if(ChatServiceController.isInLiveRoom()){
			this.buttonsLinearLayout.setVisibility(View.GONE);
			this.anchor_info_layout.setVisibility(View.VISIBLE);
		}
		refreshWordCount();

		if (this.attachScreenshotMenu != null)
		{
			this.attachScreenshotMenu.setVisible(true);
		}
	}

	public void refreshUnreadCount(){
		ArrayList<ChannelListItem> channelListItems= null;
		if(chatRoomAdapter != null){
			channelListItems= chatRoomAdapter.list;
		}
		int unreadCoount = 0;
		if(channelListItems == null) {
			unread_layout.setVisibility(View.GONE);
			return;
		}
		for(int i = 0,len = channelListItems.size();i<len;i++){
			ChannelListItem channelListItem = channelListItems.get(i);
			if(channelListItem.unreadCount > 0)
				unreadCoount += channelListItem.unreadCount;
		}
		if(unreadCoount > 0){
			unread_layout.setVisibility(View.VISIBLE);
			if(unreadCoount > 99 ){
				unread_count_text.setText("99+");
			}else{
				unread_count_text.setText(unreadCoount+"");
			}
		}else{
			unread_layout.setVisibility(View.GONE);
		}
	}
	public void saveState()
	{
		for (int i = 0; i < getChannelViewCount(); i++)
		{
			ChannelView channelView = getChannelView(i);
			if (channelView != null)
			{
				ChatChannel channel = channelView.chatChannel;
				if (channel != null && channelView.messagesListView != null)
				{
					channel.lastPosition.x = channelView.messagesListView.getFirstVisiblePosition();
					View v = channelView.messagesListView.getChildAt(0);
					channel.lastPosition.y = (v == null) ? 0 : (v.getTop() - channelView.messagesListView.getPaddingTop());
				}
			}
		}
		savedText = replyField.getText().toString();
	}

	protected boolean	isJustCreated	= true;

	public void checkFirstGlobalLayout()
	{
		if (isJustCreated)
		{
			isJustCreated = false;
			refreshTab();
		}

		if (oldChatFragmentHeight == -1 && computeUsableHeight() > 0)
		{
			oldChatFragmentHeight = computeUsableHeight();

		}
		else if (oldChatFragmentHeight > computeUsableHeight())
		{
			oldChatFragmentHeight = computeUsableHeight();
			if (isKeyBoardFirstShowed)
				isKeyBoradShowing = true;
			if (!rememberPosition)
			{
				gotoLastLine();
			}
			else
			{
				rememberPosition = false;
			}
		}
		else if (oldChatFragmentHeight == computeUsableHeight())
		{
			if (isKeyBoradChange)
			{
				keyBoardChangeCount++;
			}
			if (keyBoardChangeCount == 2)
			{
				isKeyBoradChange = false;
			}
		}
		else if (oldChatFragmentHeight < computeUsableHeight())
		{
			keyBoardChangeCount = 0;
			isKeyBoradChange = true;
			oldChatFragmentHeight = computeUsableHeight();
			isKeyBoradShowing = false;
			isKeyBoardFirstShowed = true;
		}

		int usableHeightNow = computeUsableHeight();

		if (usableHeight == -1 && usableHeightNow > 0)
		{
			usableHeight = usableHeightNow;
		}

		if (usableHeight != -1 && usableHeight > usableHeightNow)
		{
			if (!isSystemBarResized)
			{
				isSystemBarResized = true;
				return;
			}
			for (int i = 0; i < getChannelViewCount(); i++)
			{
				if (getChannelView(i).chatChannel != null && getChannelView(i).chatChannel.lastPosition.x == -1
						&& getChannelView(i).messagesListView != null && getChannelView(i).getMessagesAdapter() != null)
				{
					getChannelView(i).messagesListView.setSelection(getChannelView(i).getMessagesAdapter().getCount() - 1);
				}
			}
			usableHeight = usableHeightNow;
		}
	}

	protected int		keyBoardChangeCount		= 0;
	protected int		oldChatFragmentHeight	= -1;
	protected boolean	isSystemBarResized		= false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.activity = ((ChatActivity) getActivity());
		if(ChatServiceController.getInstance().isArOrPrGameLang()){
			return inflater.inflate(ResUtil.getId(this, "layout", "ar_cs__messages_fragment"), container, false);
		}else{
			return inflater.inflate(ResUtil.getId(this, "layout", "cs__messages_fragment"), container, false);
		}
	}

	private FrameLayout.LayoutParams getLayoutParams()
	{
		FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		param.gravity = Gravity.CENTER;
		return param;
	}

	public void refreshIsInLastScreen(final int channelType)
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (isSameChannel(channelType))
					{
						try
						{
							inLastScreen = isInLastScreen();
						}
						catch (Exception e)
						{
							LogUtil.printException(e);
						}
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});

	}

	private boolean isInLastScreen()
	{
		// messagesListView存在时messagesListView.getChildAt(0)也可能为0
		if (getCurrentChannel() == null || getCurrentChannel().getMessagesAdapter() == null
				|| getCurrentChannel().getMessagesAdapter().getCount() == 0 || getCurrentChannel().messagesListView == null)
		{
			return true;
		}
		// 遍历从view.getFirstVisiblePosition()可见高度及到最下方的各个item的高度，计算这高度和是否小于一定的值（1.6屏）
		View v = getCurrentChannel().messagesListView.getChildAt(0);
		if (v == null)
		{
			return true;
		}

		// 第一个item被上方盖住的部分
		int firstOffset = v.getTop() - getCurrentChannel().messagesListView.getPaddingTop();

		int totalHeight = v.getHeight() + firstOffset;
		if ((getCurrentChannel().getMessagesAdapter().getCount() - getCurrentChannel().messagesListView.getFirstVisiblePosition()) > 20)
		{
			return false;
		}

		for (int i = (getCurrentChannel().messagesListView.getFirstVisiblePosition() + 1); i < getCurrentChannel().getMessagesAdapter()
				.getCount(); i++)
		{
			View listItem = getCurrentChannel().getMessagesAdapter().getView(i, null, getCurrentChannel().messagesListView);
			listItem.measure(MeasureSpec.makeMeasureSpec(getCurrentChannel().messagesListView.getWidth(), MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			int h = listItem.getMeasuredHeight();
			totalHeight += h + getCurrentChannel().messagesListView.getDividerHeight();
		}

		if (totalHeight <= (getCurrentChannel().messagesListView.getHeight() * 1.75))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	boolean	inLastScreen	= false;

	public void updateListPositionForNewMsg(int channelType, boolean isSelfMsg,int post)
	{
		if (!isSameChannel(channelType) || getCurrentChannel().messagesListView == null || getCurrentChannel().getMessagesAdapter() == null)
		{
			return;
		}

		if (!isSelfMsg)
		{
			if (isKeyBoradShowing)
			{
				gotoLastLine();
			}
//			else if (inLastScreen||post==150)
			else if (post==150)
			{
				gotoLastLine();
			}
		}
		inLastScreen = false;
	}

	public void updateListPositionForOldMsg(int channelType, int loadCount, final boolean needMergeSendTime)
	{
		final ListView listView = getCurrentChannel().messagesListView;
		if (!isSameChannel(channelType) || listView == null || getCurrentChannel().getMessagesAdapter() == null)
		{
			return;
		}
		loadMoreCount = loadCount;

		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (!getCurrentChannel().chatChannel.isLoadingAllNew)
					{
						int heightOffest = getCurrentChannel().pullDownToLoadListView.getPullDownHeight();
						if (needMergeSendTime)
						{
							if (ChatServiceController.sendTimeTextHeight != 0)
								heightOffest += ChatServiceController.sendTimeTextHeight + ScaleUtil.dip2px(activity, 15);
							else
								heightOffest += ScaleUtil.dip2px(activity, 44);
						}
						listView.setSelectionFromTop(loadMoreCount, heightOffest);
					}
					else
					{
						listView.setSelectionFromTop(0, 0);
					}
					refreshToolTip();
					getCurrentChannel().pullDownToLoadListView.hideProgressBar();
					stopTimerTask();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public void changeChatRoomName(String name)
	{
		if (!name.equals(""))
		{
			if(StringUtils.isNotEmpty(name) && name.length()>15)
			{
				name = name.substring(0, 15);
				name+= "...";
			}
			getTitleLabel().setText(name);
		}
	}

	public void setEditText(String text)
	{
		if (replyField != null)
			replyField.setText(text);
	}

	public void notifyDataSetChanged(final int channelType, final boolean needCalculateShowTimeIndex)
	{
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					MessagesAdapter adapter = getChannelView(ChannelManager.channelType2tab(channelType)).getMessagesAdapter();
					if (adapter != null)
					{
						if (needCalculateShowTimeIndex)
						{
							ChatChannel channel = ChannelManager.getInstance().getChannel(channelType);
							if (channel != null)
								channel.getTimeNeedShowMsgIndex();
						}

						adapter.notifyDataSetChanged();
					}

					refreshHasMoreData();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	// 可能和notifyDataSetChanged差不了太多
	public void refreshListItem(MsgItem msgItem)
	{
		try
		{
			MessagesAdapter adapter = getChannelView(ChannelManager.channelType2tab(msgItem.channelType)).getMessagesAdapter();
			final ListView list = getChannelView(ChannelManager.channelType2tab(msgItem.channelType)).messagesListView;
			if (adapter != null && list != null)
			{
				int start = list.getFirstVisiblePosition();
				for (int i = start, j = list.getLastVisiblePosition(); i <= j; i++)
					if (msgItem == list.getItemAtPosition(i))
					{
						View view = list.getChildAt(i - start);
						list.getAdapter().getView(i, view, list);
						activity.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								// 会导致可见的item全部重新调用setConvertView
								list.invalidateViews();
							}
						});
						break;
					}
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void afterSendMsgShowed(int channelType)
	{
		ListView listView = getCurrentChannel().messagesListView;
		MessagesAdapter adapter = getCurrentChannel().getMessagesAdapter();

		if (listView != null && adapter != null && isSameChannel(channelType))
		{
			gotoLastLine();
		}
	}

	public void resetMoreDataStart(int channelType)
	{
		if (isSameChannel(channelType))
		{
			getCurrentChannel().setLoadingStart(false);
		}
	}

	protected void gotoLastLine()
	{
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (getCurrentChannel() != null && getCurrentChannel().messagesListView != null
							&& getCurrentChannel().getMessagesAdapter() != null)
					{
						// getCurrentChannel().messagesListView.setAdapter(getCurrentChannel().getMessagesAdapter());
						getCurrentChannel().messagesListView.setSelection(getCurrentChannel().getMessagesAdapter().getCount() - 1);
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	protected boolean isSameChannel(int channelType)
	{
		if (getCurrentChannel() == null)
			return false;
		return getCurrentChannel().channelType == channelType;
	}

	public SwipeMenuListView getListView()
	{
		return mListView;
	}
	public static final String AUDIO_MIX_ACTION = "AudioMix";
	private Intent mIntentAudioMix = new Intent(AUDIO_MIX_ACTION);
	private boolean mAudioMixOn;
	private boolean mAudioMixPause;
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		ConfigManager.activityType = 0;
		super.onViewCreated(view, savedInstanceState);
		if(!ChatServiceController.isInChatRoom())
			refreshMemberSelectBtn();
		this.noAllianceFrameLayout = (FrameLayout) view.findViewById(R.id.hs__noAllianceLayout);
		this.relativeLayout1 = (RelativeLayout) view.findViewById(R.id.relativeLayout1);
		this.buttonsLinearLayout = (LinearLayout) view.findViewById(R.id.buttonsLinearLayout);
		this.unread_layout = (RelativeLayout) view.findViewById(R.id.unread_layout);
		unread_count_text = (TextView) view.findViewById(R.id.unread_count_text);
		this.bottomLayout  = (LinearLayout) view.findViewById(R.id.bottom_layout);
		this.messagesListFrameLayout = (RelativeLayout) view.findViewById(R.id.hs__messagesListLayout);
		//imageView1 = (ImageView) view.findViewById(R.id.imageView1);
		//imageView2 = (ImageView) view.findViewById(R.id.imageView2);
		horn_checkbox = (CheckBox) view.findViewById(R.id.horn_checkbox);
		horn_tip_layout = (LinearLayout) view.findViewById(R.id.horn_tip_layout);
		horn_text_tip = (TextView) view.findViewById(R.id.horn_text_tip);
		horn_text_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_HORN_TEXT));
		horn_scroll_text = (ScrollText) view.findViewById(R.id.horn_scroll_text);
		horn_name = (TextView) view.findViewById(R.id.horn_name);
		horn_scroll_layout = (RelativeLayout) view.findViewById(R.id.horn_scroll_layout);
		horn_scroll_layout.setVisibility(View.GONE);
		horn_close_btn = (ImageView) view.findViewById(R.id.horn_close_btn);
		hs__dragon_chat_tip_layout = (LinearLayout) view.findViewById(R.id.hs__dragon_chat_tip_layout);
		dragon_chat_tip_text = (TextView) view.findViewById(R.id.dragon_chat_tip_text);
		dragon_chat_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_DRAGON_CHAT));

		icon_net_btn = (ImageView) view.findViewById(R.id.icon_net_btn) ;
		icon_net_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				MenuController.showNetPingShowAndChange();
			}
		});
		
		// ----------------语音直播------------
 		anchor_info_layout = (RelativeLayout) view.findViewById(R.id.anchor_info_layout);
 		listenNumText = (TextView) view.findViewById(R.id.listenNumText);
 		anchor_room_name = (TextView) view.findViewById(R.id.anchor_room_name);
 		anchor_pic_icon = (ImageView) view.findViewById(R.id.anchor_icon);
 		anchor_state_text = (TextView) view.findViewById(R.id.anchor_state_text);
 		anchor_tip_text = (TextView)view.findViewById(R.id.anchor_tip_text);
 		voice_player_btn = (Button)view.findViewById(R.id.voice_play_btn);
// 		refreshLiveView();
// 		anchor_pic_icon.setOnClickListener(new OnClickListener() {
// 			@Override
// 			public void onClick(View view) {
// 				ChatServiceController.getInstance().showAnchorInfoView();
// 			}
// 		});
 
// 		if (activity != null && !activity.isFinishing()) {//80000843 = 未开播
// 			ServiceInterface.safeMakeText(activity,LanguageManager.getLangByKey("80000843"), Toast.LENGTH_LONG);
// 		}

// 		voice_player_btn.setOnClickListener(new OnClickListener() {
// 			@Override
// 			public void onClick(View view) {
// 				if(ChatServiceController.livePushStatus){
// 					if (ChatServiceController.livePullStatus) {
// 						ChatServiceController.livePullStatus = false;
// //						voice_player_btn.setBackgroundDrawable(activity.getResources().getDrawable(ResUtil.getId(activity, "drawable", "voice_pause")));
// 						voice_player_btn.setText(LanguageManager.getLangByKey(LanguageKeys.PLAY_TITLE));
// 						voice_player_btn.setTextColor(ResUtil.getColor(getContext(),"start_title"));
// 						Log.d("BroadCast", "收听直播开始");
// 						JniController.getInstance().excuteJNIVoidMethod("postToCppSwithOn", new Object[]{false,ChatServiceController.livePullStatus});
// 						return;
// 					} else {
// 						ChatServiceController.livePullStatus = true;
// //						voice_player_btn.setBackgroundDrawable(activity.getResources().getDrawable(ResUtil.getId(activity, "drawable", "voice_play")));
// 						voice_player_btn.setText(LanguageManager.getLangByKey(LanguageKeys.STOP_PLAY_TITLE));
// 						voice_player_btn.setTextColor(ResUtil.getColor(getContext(),"stop_title"));
// 						Log.d("BroadCast", "收听直播结束");
// 						JniController.getInstance().excuteJNIVoidMethod("postToCppSwithOn", new Object[]{false,ChatServiceController.livePullStatus});
// 						return;
// 					}
// 				}else{
// 					if (activity != null && !activity.isFinishing()) {//80000843 = 未开播
// 						ServiceInterface.safeMakeText(activity, LanguageManager.getLangByKey("80000843"), Toast.LENGTH_LONG);
// 					}
// 				}
// 			}
// 		});
// 		ViewTreeObserver anchorObsever = anchor_info_layout.getViewTreeObserver();
// 		anchorObsever.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
// 			@Override
// 			public void onGlobalLayout() {
// 				if(changeAnchorComplete){
// 					return;
// 				}
// 				RelativeLayout.LayoutParams params4 = (RelativeLayout.LayoutParams) messagesListFrameLayout.getLayoutParams();
// 				if(anchor_info_layout != null && anchor_info_layout.getVisibility() == View.VISIBLE && anchor_info_layout.getHeight() > 0){
// 					params4.topMargin = anchor_info_layout.getHeight();
// 					messagesListFrameLayout.setLayoutParams(params4);
// 					changeAnchorComplete = true;
// 				}

// 			}
// 		});


		// ----------------聊天室------------
		channelListFragmentLayout = (RelativeLayout) view.findViewById(R.id.channelListFragmentLayout);
		channelListPullView = (PullToRefreshSwipeListView) view.findViewById(R.id.channelListPullView);
		channelListPullView.setLanguage(LanguageManager.getLangByKey(LanguageKeys.TIP_LOADING));

		mListView = channelListPullView.getRefreshableView();
		mListView.setCacheColorHint(0x00000000);
		mListView.setDivider(null);
		channelListPullView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<SwipeMenuListView>()
		{
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<SwipeMenuListView> refreshView)
			{
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<SwipeMenuListView> refreshView)
			{
				if (chatRoomAdapter != null && chatRoomAdapter.hasMoreData())
				{
					LogUtil.trackPageView("LoadMoreList-" + channelId);
					chatRoomAdapter.loadMoreData();
				}
			}
		});

		tip_no_mail_textView = (TextView) view.findViewById(R.id.tip_no_mail);
		if (tip_no_mail_textView != null)
		{
			tip_no_mail_textView.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NO_CHATROOM));
			tip_no_mail_textView.setVisibility(View.GONE);
		}

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			// 在本例中 arg2==arg3
			public void onItemClick(AdapterView<?> adapterView, View view, int arg2, long arg3)
			{
				onListItemClick(adapterView, view, arg2);
			}
		});

		SwipeMenuCreator creator = new SwipeMenuCreator()
		{
			@Override
			public void create(SwipeMenu menu)
			{
				switch (menu.getViewType())
				{
					case ChatRoomListAdapter.VIEW_TYPE_TOP:
						SwipeMenuItem topItem = new SwipeMenuItem(getActivity());
						topItem.setBackground(new ColorDrawable(Color.rgb(0x80, 0x80, 0x80)));
						topItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_TOP));
						topItem.setTitleSize(ScaleUtil.getAdjustTextSize(12, ConfigManager.scaleRatio));
						topItem.setTitleColor(Color.WHITE);
						topItem.setWidth(dp2px(ScaleUtil.getAdjustTextSize(90, ConfigManager.scaleRatio)));
						menu.addMenuItem(topItem);
						break;
					case ChatRoomListAdapter.VIEW_TYPE_UNTOP:
						SwipeMenuItem unTopItem = new SwipeMenuItem(getActivity());
						unTopItem.setBackground(new ColorDrawable(Color.rgb(0x80, 0x80, 0x80)));
						unTopItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_UNTOP));
						unTopItem.setTitleSize(ScaleUtil.getAdjustTextSize(12, ConfigManager.scaleRatio));
						unTopItem.setTitleColor(Color.WHITE);
						unTopItem.setWidth(dp2px(ScaleUtil.getAdjustTextSize(90, ConfigManager.scaleRatio)));
						menu.addMenuItem(unTopItem);
						break;

					case ChatRoomListAdapter.VIEW_TYPE_NONE:
						break;
				}
			}
		};
		mListView.setMenuCreator(creator);

		mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener()
		{
			@Override
			public void onMenuItemClick(int position, SwipeMenu menu, int index)
			{
				if(position >= chatRoomAdapter.list.size()){
					return;
				}

				ChannelListItem curItem = chatRoomAdapter.list.get(position);
				ChatChannel curChannel = (ChatChannel) curItem;
				if(curChannel.settings!=null && curChannel.settings.equals("0")) {
					for (int i = 0; i < chatRoomAdapter.list.size(); i++) {
						ChannelListItem item = chatRoomAdapter.list.get(i);
						ChatChannel channel = (ChatChannel) item;
						if (channel != null && (channel.settings.equals("1") || channel.settings.equals("2")) ) {
							String content = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_TOP_WARN);
							MenuController.topChatRoomConfirm((ChatActivity) getActivity(), content, curChannel);
							return;
						}
					}

					curChannel.settings = "1";
					ChatServiceController.topChatRoomUid = curChannel.channelID;
					JniController.getInstance().excuteJNIVoidMethod("getLatestChatMessage", null);
				}else{
					curChannel.settings = "0";
					if(curChannel.channelID.equals(ChatServiceController.topChatRoomUid)) {
						ChatServiceController.topChatRoomUid = "";
					}
				}
				DBManager.getInstance().updateChannel(curChannel);
				notifyDataSetChanged();

			}
		});

		/********************************************************/
		fall_lantern_view = (FallingView)view.findViewById(R.id.fall_lantern_view);
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what){
					case 1:
						if(fall_lantern_view.getFallObjectNum() == 0) {
							fall_lantern_view.setVisibility(View.VISIBLE);
							FallObject.Builder builder = new FallObject.Builder(ChatServiceController.hostActivity.getResources().getDrawable(R.drawable.lantern));
							FallObject fallObject = builder
									.setSpeed(5, false)
									.setSize(100, 100, false)
									.setWind(10, true, true)
									.build();
							fall_lantern_view.addFallObject(fallObject, 15);
							fall_lantern_view.start();
						}else{
							fall_lantern_view.setVisibility(View.VISIBLE);
							fall_lantern_view.start();
						}
						ChatServiceController.isInLantern = true;
						break;
					case 2:
						if(ChatServiceController.isInLantern) {
							fall_lantern_view.cancel();
							endAnimation();
						}
						break;

				}
			}
		};
		// ----------------聊天室------------

		if (!lazyLoading)
			renderList();

		this.replyField = ((EditText) view.findViewById(ResUtil.getId(this.activity, "id", "hs__messageText")));
		this.wordCount = ((TextView) view.findViewById(ResUtil.getId(this.activity, "id", "wordCountTextView")));
		if (!ChatServiceController.isInMailDialog())
		{
			if (ConfigManager.websocket_network_state!=ConfigManager.WEBSOCKET_NETWORK_CONNECTED)
			{
				getTitleLabel().setText(LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_CONNECTING));
			}
			else
			{
				if(ChatServiceController.isInLiveRoom()){
					getTitleLabel().setText(ChatServiceController.liveRoomName);
				}else{
					getTitleLabel().setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_CHAT));
				}
			}
		}
		else
		{
			String title = UserManager.getInstance().getCurrentMail().opponentName;
			if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_USER)
			{
				String fromUid = ChannelManager.getInstance().getModChannelFromUid(UserManager.getInstance().getCurrentMail().opponentUid);
				if (StringUtils.isNotEmpty(fromUid))
				{
					if (fromUid.equals(UserManager.getInstance().getCurrentUserId()))
					{
						title = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE);
					}
					else
					{
						UserInfo fromUser = UserManager.getInstance().getUser(fromUid);
						if (fromUser != null && StringUtils.isNotEmpty(fromUser.userName))
						{
							title = fromUser.userName;
						}
					}
				}

			}

			if (ChatServiceController.isInChatRoom() && title.length() > 16)
			{
				title = LanguageManager.getLangByKey(LanguageKeys.TITLE_CHATROOM);
				if (title.equals(""))
					title = "Group";
			}
			if (UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
				title += "(MOD)";
			getTitleLabel().setText(title);
		}

//		if (ChatServiceController.isChatRestrictForLevel())
		if(false)
		{
			replyField.setEnabled(false);
			replyField.setHint(LanguageManager.getLangByKey(LanguageKeys.CHAT_RESTRICT_TIP,
					"" + ChatServiceController.getChatRestrictLevel()));
		}
		else
		{
			replyField.setEnabled(true);
			replyField.setHint("");
			if (ChatServiceController.needShowAllianceDialog)
				replyField.setText(LanguageManager.getLangByKey(LanguageKeys.INPUT_ALLIANCE_DIALOG));
			else
				replyField.setText(savedText);
		}

		addReply = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "hs__sendMessageBtn"));
		voice_btn = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "hs__voiceMessageBtn"));
//		addReply.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_SEND));

		this.messageBox = ((RelativeLayout) view.findViewById(ResUtil.getId(this.activity, "id", "relativeLayout1")));
//		header = ((LinearLayout) view.findViewById(ResUtil.getId(this.activity, "id", "headerRelativeLayout")));

//		getMemberSelectButton().setVisibility(isSelectMemberBtnEnable ? View.VISIBLE : View.GONE);
		getMemberSelectButton().setVisibility(View.GONE);
		
		buttonCountry = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "buttonCountry"));
		buttonAlliance = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "buttonAllie"));
		buttonChatRoom = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "buttonChatRoom"));
		if (UserManager.getInstance().getCurrentUser()!=null && !UserManager.getInstance().getCurrentUser().allianceId.equals("")&& ChatServiceController.isInTempAlliance)
		{
			buttonAlliance.setText(LanguageManager.getLangByKey(LanguageKeys.TEMP_ALLIANCE));
		}
		else
		{
			buttonAlliance.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_ALLIANCE));
		}
		buttonCountry.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_COUNTRY));
		buttonChatRoom.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CHATROOM_NAME));
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonCountry, true);
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonAlliance, false);
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonChatRoom, false);

		tooltipLayout = ((LinearLayout) view.findViewById(ResUtil.getId(this.activity, "id", "tooltipLayout")));
		tooltipLabel = ((TextView) view.findViewById(ResUtil.getId(this.activity, "id", "tooltipLabel")));
		tooltipArrow = ((ImageView) view.findViewById(ResUtil.getId(this.activity, "id", "tooltipArrow")));
		showToolTip(false);
		tooltipLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onClickToolTip();
			}
		});

//		// 红包
//		sendRedPackage = view.findViewById(ResUtil.getId(this.activity, "id", "send_red_package"));
//		sendRedPackage.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				onSendRedPackage();
//			}
//		});

        channelButton = new ArrayList<Button>();
        channelButton.add(buttonCountry);
        channelButton.add(buttonAlliance);
        channelButton.add(buttonChatRoom);

        buttonJoinAlliance = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "joinAllianceBtn"));
        buttonJoinAlliance.setText(LanguageManager.getLangByKey(LanguageKeys.MENU_JOIN));

        buttonJoinAlliance.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ChatServiceController.doHostAction("joinAllianceBtnClick", "", "", "", true);
            }
        });

        horn_close_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideHornScrollText();
            }
        });

        noAllianceTipText = ((TextView) view.findViewById(ResUtil.getId(this.activity, "id", "joinAllianceTipText")));
        noAllianceTipText.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_JOIN_ALLIANCE));

        refreshSendButton();

        for (int i = 0; i < channelButton.size(); i++) {
			final int index = i;
            channelButton.get(i).setTag(getChannelView(i));
            channelButton.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
					try {
						if(ChatServiceController.getCurrentChannelType() != index) {
							endAnimation();
							fall_lantern_view.cancel();
							fall_lantern_view.setVisibility(View.GONE);
						}
						ChannelView channel = ((ChannelView) view.getTag());

						if (ConfigManager.isEnterArena && channel.tab == TAB_COUNTRY) {
							String tipStr = LanguageManager.getLangByKey("172335");
							ServiceInterface.flyHint("", "", tipStr, 2, 0, false);
							return;
						}
						channel.setLoadingStart(false);
						showTab(channel.tab);
						refreshNetState(channel.tab, isNeedShowWifi);
						if (channel.tab == TAB_COUNTRY) {
							ChatServiceController.isTabRoom = false;
							JniController.getInstance().excuteJNIVoidMethod("postCurChannel",
									new Object[]{Integer.valueOf(DBDefinition.CHANNEL_TYPE_COUNTRY)});
						} else if (channel.tab == TAB_ALLIANCE) {
							ChatServiceController.isTabRoom = false;
							JniController.getInstance().excuteJNIVoidMethod("postCurChannel",
									new Object[]{Integer.valueOf(DBDefinition.CHANNEL_TYPE_ALLIANCE)});
						} else if (channel.tab == TAB_CHATROOM) {
							ChatServiceController.isTabRoom = true;
	//						JniController.getInstance().excuteJNIVoidMethod("postCurChannel",
	//								new Object[] { Integer.valueOf(DBDefinition.CHANNEL_TYPE_CHATROOM) });
						}
						if (channel.tab == TAB_ALLIANCE && UserManager.getInstance().getCurrentUser().allianceId.equals("")) {
							return;
						}

						// TODO isFirstVisit变量是否还必要
						getChannelView(0).isFirstVisit = !ChannelManager.getInstance().getCountryChannel().hasRequestDataBefore;
						if (UserManager.getInstance().isCurrentUserInAlliance()) {
							getChannelView(1).isFirstVisit = !ChannelManager.getInstance().getAllianceChannel().hasRequestDataBefore;
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}


					// if (channel.isFirstVisit)
					// {
					// if (getCurrentChannel().tab == TAB_COUNTRY)
					// {
					// ChatServiceController.getInstance().host.requestChatMsg(0);
					// }
					// else if (getCurrentChannel().tab == TAB_ALLIANCE)
					// {
					// requestMsgRecord(DBDefinition.CHANNEL_TYPE_ALLIANCE);
					// }
					//
					// channel.isFirstVisit = false;
					// }
					// else if (getCurrentChannel().tab == TAB_ALLIANCE &&
					// ChannelManager.getInstance().getCurMsgListByIndex(DBDefinition.CHANNEL_TYPE_ALLIANCE).size()<=0)
					// {
					// requestMsgRecord(DBDefinition.CHANNEL_TYPE_ALLIANCE);
					// }
                }
            });
        }

        getMemberSelectButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyBoard();
                //ServiceInterface.showMemberSelectorActivity(activity, true);
                if (ChatServiceController.isFromBd) {
                    ChatServiceController.getInstance().showLiveRoomSetting();
                } else {
                    ServiceInterface.showChatRoomSettingActivity(activity);
                }
            }
        });

        addReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ChatBanInfo banInfo = UserManager.getInstance().isHaveUidBan(1);
                if (banInfo != null) {

                    // 发送按钮点击后关闭软键盘
                    if (getActivity() != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(addReply.getApplicationWindowToken(), 0);
                    }
                    String tipStr = "";
                    if (banInfo.banTime == -1) {
                        tipStr = LanguageManager.getLangByKey("171307");
                    } else {
                        tipStr = LanguageManager.getLangByKey("105201", "", TimeManager.getInstance().getTimeFormatWithRemainTime((int) banInfo.banTime));
                    }
                    ServiceInterface.flyHint("", "", tipStr, 3, 0, false);
                    return;
                }


                String replyText = replyField.getText().toString().trim();

                if (!TextUtils.isEmpty(replyText)) {
//					if(replyText.endsWith("png"))
//					{
//						//System.out.println("setCommonImage");
//						ImageUtil.setCommonImage(activity, replyText, imageView2);
//					}

                    // 换行>5提示：171253=发送的内容中存在过多换行，不能发送。
                    String[] sourceStrArray = replyText.split("\n");
                    if (sourceStrArray.length > 5) {
                        ServiceInterface.flyHint("", "", LanguageManager.getLangByKey("171253"), 3, 0, false);
                        return;
                    }

                    if (horn_checkbox.isChecked() && ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY) {
                        int hornBanedTime = JniController.getInstance().excuteJNIMethod("getHornBanedTime", null);
                        if (hornBanedTime == 0) {
                            int price = JniController.getInstance().excuteJNIMethod("isHornEnough", null);
                            String horn = LanguageManager.getLangByKey(LanguageKeys.TIP_HORN);
                            if (price == 0) {
                                if (ConfigManager.isFirstUserHorn)
                                    MenuController.showSendHornMessageConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_USEITEM, horn),
                                            replyText);
                                else
                                    ChatServiceController.sendMsg(replyText, true, false, null);
                            } else if (price > 0) {
                                if (ConfigManager.isFirstUserCornForHorn)
                                    MenuController.showSendHornWithCornConfirm(
                                            LanguageManager.getLangByKey(LanguageKeys.TIP_ITEM_NOT_ENOUGH, horn), replyText, price);
                                else {
                                    boolean isCornEnough = JniController.getInstance().excuteJNIMethod("isCornEnough",
                                            new Object[]{Integer.valueOf(price)});
                                    if (isCornEnough) {
                                        ChatServiceController.sendMsg(replyText, true, true, null);
                                    } else {
                                        MenuController.showCornNotEnoughConfirm(LanguageManager
                                                .getLangByKey(LanguageKeys.TIP_CORN_NOT_ENOUGH));
                                    }
                                }
                            }
                        }
                    } else {
                        ChatServiceController.sendMsg(replyText, false, false, null);
                    }


                }

                // 发送按钮点击后关闭软键盘
                if (getActivity() != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(addReply.getApplicationWindowToken(), 0);
                }
            }
        });
		voice_btn.setVisibility(View.GONE);
		if(ConfigManager.isSpeechRecognition) {
			voice_btn.setVisibility(View.VISIBLE);
			voice_btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					try {
						startVoiceRecognitionActivity();
					} catch (Exception e) {
						LogUtil.printException(e);
					}
				}
			});
		}
        this.replyField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == 4) {
                    addReply.performClick();
                }
                return false;
            }
        });

        replyField.setOnTouchListener(new View.OnTouchListener(){


			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
					if(ChatServiceController.isNeedReName && !ChatServiceController.isInMailDialog()){
						MenuController.showContentConfirm(LanguageManager.getLangByKey("81000713"));
						return true;
					}
//				if (ChatServiceController.isChatRestrict())
//				{
//					MenuController.showChatRestrictConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CHAT_RESTRICT));
//				}
				}
				return false;
			}
		});

        textChangedListener = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                replyField.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshWordCount();
                    }
                });
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshSendButton();
            }
        };
        this.replyField.addTextChangedListener(textChangedListener);

//		getShowFriendButton().setVisibility(ChatServiceController.isInMailDialog() ? View.GONE : View.VISIBLE);
//		getShowFriendButton().setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View view)
//			{
//				ChatServiceController.doHostAction("showFriend", "", "", "", false);
//			}
//		});

        showMessageBox();

        horn_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                refreshBottomUI(isChecked);
                if (isChecked)
                    ConfigManager.isHornBtnEnable = true;
                else
                    ConfigManager.isHornBtnEnable = false;
            }
        });

        onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                checkFirstGlobalLayout();
                adjustHeight();
            }
        };
        messagesListFrameLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        ((ChatActivity) getActivity()).fragment = this;

        if (!lazyLoading) {
            refreshToolTip();
            refreshHasMoreData();
        }

        if (ScrollTextManager.getInstance().getScrollQueueLength() > 0) {
            MsgItem msgItem = ScrollTextManager.getInstance().getNextText();
            if (msgItem != null)
                showHornScrollText(msgItem);
        }
    }

    public void showHornScrollText(MsgItem msgItem) {
        if (!msgItem.isHornMessage())
            return;
        // 目前不在聊天里显示滚动消息
//		if (horn_scroll_layout != null)
//		{
//			horn_scroll_layout.setVisibility(View.VISIBLE);
//			ScrollTextManager.getInstance().showScrollText(msgItem, horn_scroll_text, horn_name, horn_scroll_layout);
//		}

    }

    public void hideHornScrollText() {
        ScrollTextManager.getInstance().shutDownScrollText(horn_scroll_text);
    }

    private boolean lazyLoading = true;

    protected void onBecomeVisible() {
        if (inited)
            return;

        timerDelay = 500;
        startTimer();
    }

    public static final int CHANNEL_COUNT = 4;
    private int currentChannelViewIndex;
    private ArrayList<ChannelView> channelViews;

    private void initChannelViews() {
        channelViews = new ArrayList<ChannelView>();
        for (int i = 0; i < CHANNEL_COUNT; i++) {
            ChannelView channelView = new ChannelView();

            channelView.tab = i;

            channelViews.add(channelView);
        }
    }

    public int getChannelViewCount() {
        return CHANNEL_COUNT;
    }

    public ChannelView getChannelView(int index) {
        return channelViews.get(index);
    }

    public void setChannelViewIndex(int i) {
        if (i >= 0 && i < channelViews.size()) {
            currentChannelViewIndex = i;
        }
    }

    public ChannelView getCurrentChannelView() {
        try {
            if (channelViews == null || currentChannelViewIndex < 0 || currentChannelViewIndex >= channelViews.size())
                return null;
            return channelViews.get(currentChannelViewIndex);
        } catch (Exception e) {
            return null;
        }
    }

    public ChannelView getCountryChannelView() {
        try {
            return getChannelView(0);
        } catch (Exception e) {
            return null;
        }
    }

    public void resetChannelView() {
        for (int i = 0; i < CHANNEL_COUNT; i++) {
            if (getChannelView(i).chatChannel != null) {
                getChannelView(i).chatChannel.setChannelView(null);
            }
            getChannelView(i).init();
        }
    }

    protected void createList() {
        chatRoomAdapter = new ChatRoomChannelAdapter((ChatActivity) getActivity(), this);
        chatRoomAdapter.fragment = this;
        refreshScrollLoadEnabled();
        super.createList();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void renderList() {
        for (int i = 0; i < getChannelViewCount(); i++) {
            final ChannelView channelView = getChannelView(i);
            ChatChannel chatChannel = null;
            if (i < 2) {
                chatChannel = ChannelManager.getInstance().getChannel(i, "");
                channelView.channelType = i;
            } else if (i == 2) {
                channelView.channelType = 3;
                mListView.setAdapter(chatRoomAdapter);
                restorePosition();
                continue;
            } else if (ChatServiceController.isInMailDialog() || ChatServiceController.isInLiveRoom()) {
                String channelId = UserManager.getInstance().getCurrentMail().opponentUid;
                if (ChatServiceController.isContactMod && !channelId.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
                    channelId += DBDefinition.CHANNEL_ID_POSTFIX_MOD;
                chatChannel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType(), channelId);
                channelView.channelType = ChatServiceController.getCurrentChannelType();
            } else {
                continue;
            }
            if (chatChannel != null) {
                LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "chatChannel", chatChannel, "msgList.size()", chatChannel.msgList.size());
                chatChannel.clearFirstNewMsg();
                chatChannel.setChannelView(channelView);
            }
            channelView.chatChannel = chatChannel;

            PullDownToLoadMoreView pullDownToLoadListView = new PullDownToLoadMoreView(activity);
            pullDownToLoadListView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

            pullDownToLoadListView.setTopViewInitialize(true);
            pullDownToLoadListView.setAllowPullDownRefersh(false);
            pullDownToLoadListView.setBottomViewWithoutScroll(false);
            pullDownToLoadListView.setListViewLoadListener(mListViewLoadListener);
            pullDownToLoadListView.setListViewTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    onContentAreaTouched();
                    return false;
                }
            });

            ListView messagesListView = new ListView(activity);
            messagesListView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            messagesListView.setVerticalFadingEdgeEnabled(false);
            messagesListView.setCacheColorHint(Color.TRANSPARENT);
            messagesListView.setDivider(null);

            // TODO 增加摩擦力，测试流畅性
            // scroll speed decreases as friction increases. a value of 2 worked
            // well in an emulator; i need to test it on a real device
            // messagesListView.setFriction(ViewConfiguration.getScrollFriction()
            // * 2);

            if (chatChannel != null && chatChannel.msgList != null) {
                // this.getActivity() 可能为null
                channelView.setMessagesAdapter(new MessagesAdapter(this, 17367043, chatChannel.msgList));
            } else {
                channelView.setMessagesAdapter(new MessagesAdapter(this, 17367043, new ArrayList<MsgItem>()));
            }
            messagesListView.setAdapter(channelView.getMessagesAdapter());

            messagesListView.setOnScrollListener(mOnScrollListener);
            messagesListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
            messagesListView.setKeepScreenOn(true);

            pullDownToLoadListView.addView(messagesListView);

            channelView.pullDownToLoadListView = pullDownToLoadListView;
            channelView.messagesListView = messagesListView;

            // TODO 删除不用代码
            messagesListView.post(new Runnable() {
                @Override
                public void run() {
                    if (channelView != null && channelView.messagesListView != null) {
                        // 可能出NullPointerException异常
                        // channelView.messagesListView.setSelection(channelView.messagesListView.getCount());
                    }
                }
            });

            if (chatChannel != null) {
                if (chatChannel.lastPosition.x != -1 && rememberPosition) {
                    channelView.messagesListView.setSelectionFromTop(chatChannel.lastPosition.x, chatChannel.lastPosition.y);
                } else {
                    channelView.messagesListView.setSelection(channelView.getMessagesAdapter().getCount() - 1);
                }
            }

            messagesListFrameLayout.addView(pullDownToLoadListView);
        }
        if (lazyLoading) {
            refreshTab();
        }
        activity.hideProgressBar();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void restorePosition() {
        int lastX = chatRoomlastScrollX;
        int lastY = chatRoomlastScrollY;
        if (lastX != -1) {
            mListView.setSelectionFromTop(lastX, lastY);
        }
        chatRoomlastScrollX = chatRoomlastScrollY = -1;
    }

    protected void refreshTab() {
        if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_ALLIANCE) {
            showTab(TAB_ALLIANCE);
        } else if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY) {
            showTab(TAB_COUNTRY);
        } else if (ChatServiceController.isInMailDialog() || ChatServiceController.isInLiveRoom()) {
            showTab(TAB_MAIL);
        } else {
            showTab(TAB_COUNTRY);
        }
        refreshWordCount();
    }

    private void refreshSendButton() {
        if (this.replyField.getText().length() == 0) {
            addReply.setEnabled(false);
//			CompatibleApiUtil.getInstance().setButtonAlpha(addReply, false);
        } else {
            addReply.setEnabled(true);
//			CompatibleApiUtil.getInstance().setButtonAlpha(addReply, true);
        }
    }

    public void showToolTip(boolean b) {
//		tooltipLayout.setVisibility(b ? View.VISIBLE : View.GONE); //此功能先不加，因为ios还没有此功能
        tooltipLayout.setVisibility(View.GONE);
    }

    /**
     * 点击发送红包
     */
    private void onSendRedPackage() {
        ChatServiceController.getInstance().setSendRedPackage();
    }

    private void onClickToolTip() {
        ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
        if (channel == null) return;

        if (!WebSocketManager.isRecieveFromWebSocket(channel.channelType)) {
            if (channel.canLoadAllNew()) {
                getCurrentChannel().setLoadingStart(true);
                oldAdapterCount = getCurrentChannel().getMessagesAdapter().getCount();
                loadMoreCount = 0;
                channel.isLoadingAllNew = true;
                channel.hasLoadingAllNew = true;
                ChannelManager.getInstance().loadAllNew(channel);

                refreshToolTip();
            }
        } else {
            if (channel.wsNewMsgCount > ChannelManager.LOAD_ALL_MORE_MIN_COUNT) {
                channel.wsNewMsgCount = 0;
                updateListPositionForOldMsg(channel.channelType, 0, false);
            }
        }
    }

    public void refreshToolTip() {
        ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
        // 未加入联盟时，channel不存在
        if (channel == null || isInMail()) {
            return;
        }

        if (!WebSocketManager.isRecieveFromWebSocket(channel.channelType)) {
            refreshToolTipInGameServer(channel);
        } else {
            refreshToolTipInWSServer(channel);
        }
    }

    private void refreshToolTipInGameServer(ChatChannel channel) {
        if (channel != null && channel.canLoadAllNew()) {
            String newMsgCount = channel.getNewMsgCount() < ChannelManager.LOAD_ALL_MORE_MAX_COUNT ? channel.getNewMsgCount() + ""
                    : ChannelManager.LOAD_ALL_MORE_MAX_COUNT + "+";
            tooltipLabel.setText(LanguageManager.getLangByKey(LanguageKeys.NEW_MESSAGE_ALERT, newMsgCount));
            showToolTip(true);
        } else {
            showToolTip(false);
        }
    }

    private void refreshToolTipInWSServer(ChatChannel channel) {
        // 第一次加载历史消息后，重置channel.wsNewMsgCount
        // TODO 应该改为显示到第一条消息后重置
        // if(channel.wsNewMsgCount > 0 && channel.msgList.size() !=
        // channel.wsNewMsgCount)
        // {
        // channel.wsNewMsgCount = 0;
        // }

        if (channel != null && channel.wsNewMsgCount > ChannelManager.LOAD_ALL_MORE_MIN_COUNT) {
            String newMsgCount = channel.wsNewMsgCount < ChannelManager.LOAD_ALL_MORE_MAX_COUNT ? channel.wsNewMsgCount + ""
                    : ChannelManager.LOAD_ALL_MORE_MAX_COUNT + "+";
            tooltipLabel.setText(LanguageManager.getLangByKey(LanguageKeys.NEW_MESSAGE_ALERT, newMsgCount));
            showToolTip(true);
        } else {
            showToolTip(false);
        }
    }

    public void clearInput() {
        replyField.setText("");
    }

    private void refreshBottomUI(boolean isChecked) {
        if (!isChecked)
            ChatServiceController.isHornItemUsed = false;
        boolean isHornUI = isChecked && ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY
                && ConfigManager.enableChatHorn;
        String background = isHornUI ? "send_horn_btn" : "send_normal_btn";
        String bottomBg = isHornUI ? "send_horn_bg" : "send_normal_bg";
        String inputBg = isHornUI ? "text_field_box" : "text_field_box";

        int addReplyId = ResUtil.getId(activity, "drawable", background);
        int relativeLayout1Id = ResUtil.getId(activity, "drawable", bottomBg);
        int replyFieldId = ResUtil.getId(activity, "drawable", inputBg);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            addReply.setBackgroundDrawable(null);
            addReply.setBackgroundDrawable(activity.getResources().getDrawable(addReplyId));
            relativeLayout1.setBackgroundDrawable(null);
            relativeLayout1.setBackgroundDrawable(activity.getResources().getDrawable(relativeLayout1Id));
            replyField.setBackgroundDrawable(null);
            replyField.setBackgroundDrawable(activity.getResources().getDrawable(replyFieldId));
        } else {
            addReply.setBackground(null);
            addReply.setBackground(activity.getResources().getDrawable(addReplyId));
            relativeLayout1.setBackground(null);
            relativeLayout1.setBackground(activity.getResources().getDrawable(relativeLayout1Id));
            replyField.setBackground(null);
            replyField.setBackground(activity.getResources().getDrawable(replyFieldId));
        }

//		horn_tip_layout.setVisibility(isHornUI ? View.VISIBLE : View.GONE);
        //imageView1.setVisibility(isHornUI ? View.GONE : View.VISIBLE);
        //imageView1.setVisibility(View.INVISIBLE);
        setMaxInputLength(isHornUI);
		if(ChatServiceController.isNeedReName && !ChatServiceController.isInMailDialog() && ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_ALLIANCE) {
			replyField.setInputType(InputType.TYPE_NULL);
		}
    }

    private void setMaxInputLength(boolean isHornUI) {
        curMaxInputLength = isHornUI && ConfigManager.maxHornInputLength > 0 ? ConfigManager.maxHornInputLength : 500;
		if(ChatServiceController.special_symbol_check) {
			replyField.setFilters(new InputFilter[]{new InputFilter() {
				@Override
				public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
					return ChatServiceController.replaceSpecialSymbolChar(charSequence.toString());
				}
			}, new InputFilter.LengthFilter(curMaxInputLength)});
		}else{
			replyField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(curMaxInputLength)});
		}
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    public class LoadMoreMsgParam {
        public long requestMinTime;
        public long requestMaxTime;

        public int requestMinSeqId;
        public int requestMaxSeqId;

        public boolean useTime;

        public boolean fetchFromServer;

        public LoadMoreMsgParam(int minSeqId, int maxSeqId, boolean fetchFromServer) {
            useTime = false;
            this.requestMinSeqId = minSeqId;
            this.requestMaxSeqId = maxSeqId;
            this.fetchFromServer = fetchFromServer;
        }

        public LoadMoreMsgParam(long requestMinTime, long requestMaxTime, boolean fetchFromServer) {
            useTime = true;
            this.requestMinTime = requestMinTime;
            this.requestMaxTime = requestMaxTime;
            this.fetchFromServer = fetchFromServer;
        }

        public int getRequestCount() {
            return requestMaxSeqId - requestMinSeqId + 1;
        }
    }

    /**
     * 获取加载区间的逻辑，也是检查能否加载的逻辑
     */
    private LoadMoreMsgParam getLoadMoreMsgParam(int channelType) {
        if (!(channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE || channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)) {
            return null;
        }
        ChatChannel channel = ChannelManager.getInstance().getChannel(channelType);
        if (channel == null || channel.msgList == null || channel.getChannelView() == null) {
            return null;
        }

        if (WebSocketManager.isRecieveFromWebSocket(channelType)) {
            return getLoadMoreMsgParamBySeqId(channel);
        } else {
            return getLoadMoreMsgParamByTime(channel);
        }
    }

    private LoadMoreMsgParam getLoadMoreMsgParamByTime(ChatChannel channel) {
        int minTime = channel.getMinCreateTime();
        // 如果用时间，则肯定是webSocket服务，由于时间不连续，没法判断再前面的消息是在db还是server，所以初始化时将新消息全部加载到本地
        Pair<Long, Long> range = DBManager.getInstance().getHistoryTimeRange(channel.getChatTable(), minTime,
                ChannelManager.LOAD_MORE_COUNT);
        if (range != null) {
            return new LoadMoreMsgParam(range.first, range.second, false);
        }

        return null;
    }

    private LoadMoreMsgParam getLoadMoreMsgParamBySeqId(ChatChannel channel) {
        int viewMinSeqId = channel.getMinSeqId();

        // 不能加载: 没有消息时viewMinSeqId为0，有消息时seqId最小为1
        if (viewMinSeqId <= 1) {
            return null;
        }

        // desireMaxSeqId可能等于desireMinSeqId，仅当二者都为1时
        int desireMaxSeqId = viewMinSeqId - 1;//DBManager.getInstance().getLoadMoreMaxSeqId(channel.getChatTable(), viewMinSeqId);
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "desireMaxSeqId", desireMaxSeqId, "viewMinSeqId", viewMinSeqId);
        int desireMinSeqId = (desireMaxSeqId - 19) > 1 ? (desireMaxSeqId - 19) : 1;

        // 如果desireMaxSeqId在本地db中有，就从db加载（不一定能满20条）
        if (DBManager.getInstance().isMsgExists(channel.getChatTable(), desireMaxSeqId, -1)) {
            return new LoadMoreMsgParam(desireMinSeqId, desireMaxSeqId, false);
        }

        // 否则，如果在server范围内，从server加载
        // server中seqId连续，可以用交集判断
        Point inter = getIntersection(new Point(channel.serverMinSeqId, channel.serverMaxSeqId), new Point(desireMinSeqId, desireMaxSeqId));
        if (inter != null) {
            if (WebSocketManager.isWebSocketEnabled() && WebSocketManager.isRecieveFromWebSocket(channel.channelType)) {
                MsgItem msg = DBManager.getInstance().getChatBySequeueId(channel.getChatTable(), desireMaxSeqId + 1);
                if (msg != null) {
                    return new LoadMoreMsgParam((long) 0, (long) msg.createTime, true);
                }
            } else {
                return new LoadMoreMsgParam(inter.x, inter.y, true);
            }
        }

        // 既不在db，又不在server（再往前的也肯定不在server），则找到db中最早的，加载之
        Point range = DBManager.getInstance().getHistorySeqIdRange(channel.getChatTable(), desireMaxSeqId, ChannelManager.LOAD_MORE_COUNT);
        if (range != null) {
            return new LoadMoreMsgParam(range.x, range.y, false);
        }

        return null;
    }

    /**
     * 计算两段连续区间的交集
     *
     * @param sec1 [sec1.x, sec1.y]组成的区间
     * @param sec2 [sec2.x, sec2.y]组成的区间
     * @return null，如果无交集
     */
    public static Point getIntersection(Point sec1, Point sec2) {
        int[] fourValue = {sec1.x, sec1.y, sec2.x, sec2.y};
        Arrays.sort(fourValue); // 升序排序
        int lower = -1;
        int upper = -1;
        for (int i = 0; i < fourValue.length; i++) {
            if (fourValue[i] >= sec1.x && fourValue[i] <= sec1.y && fourValue[i] >= sec2.x && fourValue[i] <= sec2.y) {
                lower = fourValue[i];
                break;
            }
        }
        for (int i = fourValue.length - 1; i >= 0; i--) {
            if (fourValue[i] >= sec1.x && fourValue[i] <= sec1.y && fourValue[i] >= sec2.x && fourValue[i] <= sec2.y) {
                upper = fourValue[i];
                break;
            }
        }
        if (lower != -1 && upper != -1) {
            return new Point(lower, upper);
        } else {
            return null;
        }
    }

    private boolean checkMessagesAdapter() {
        if (getCurrentChannel() == null || getCurrentChannel().getMessagesAdapter() == null) {
            LogUtil.trackMessage("checkMessagesAdapter() fail: currentChannel = " + getCurrentChannel() + " messagesAdapter = "
                    + (getCurrentChannel() == null ? "null" : getCurrentChannel().getMessagesAdapter()) + " currentChatType = "
                    + ChatServiceController.getCurrentChannelType() + " chatActivity = " + ChatServiceController.getChatActivity()
                    + " chatFragment = " + ChatServiceController.getChatFragment());
            return false;
        }
        return true;
    }

    private void loadMoreMsg() {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "channelID");
        createTimerTask();

        if (!checkMessagesAdapter())
            return;

        ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
        // 极少情况下会发生
        if (channel == null)
            return;
        LoadMoreMsgParam loadMoreMsgParam = getLoadMoreMsgParam(channel.channelType);

        if (!getCurrentChannel().getLoadingStart() && loadMoreMsgParam != null) {
            LogUtil.trackPageView("LoadMoreMsg");
            getCurrentChannel().setLoadingStart(true);
            // 可能有异常 getCount() on a null object reference
            oldAdapterCount = getCurrentChannel().getMessagesAdapter().getCount();
            loadMoreCount = 0;
            channel.isLoadingAllNew = false;
            if (loadMoreMsgParam.fetchFromServer) {
                LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "从server加载消息");
                if (WebSocketManager.isWebSocketEnabled() && WebSocketManager.isRecieveFromWebSocket(channel.channelType)) {
                    String room = "";
                    if (channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY) {
                        room = WebSocketManager.getCountryRoomId();
                    } else if (channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE) {
                        room = WebSocketManager.getAllianceRoomId();
                    }
                    WebSocketManager.getInstance().getHistoryMsgs(room, 0, loadMoreMsgParam.requestMaxTime);
                } else {
                    if (ChatServiceController.chat_msg_independent) {
                        JniController.getInstance().excuteJNIVoidMethod(
                                "getMsgBySeqId",
                                new Object[]{
                                        Integer.valueOf(loadMoreMsgParam.requestMinSeqId),
                                        Integer.valueOf(loadMoreMsgParam.requestMaxSeqId),
                                        Integer.valueOf(channel.channelType),
                                        channel.channelID});
                    }
                }
            } else {
                LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "从db加载消息");
                if (!loadMoreMsgParam.useTime) {
                    ChannelManager.getInstance().loadMoreMsgFromDB(channel, loadMoreMsgParam.requestMinSeqId,
                            loadMoreMsgParam.requestMaxSeqId, -1, false);
                } else {
                    ChannelManager.getInstance().loadMoreMsgFromDB(channel, -1, -1, channel.getMinCreateTime(), true);
                }
            }
        }

    }

    private void loadMoreMail() {
        if (!checkMessagesAdapter())
            return;

        if (!getCurrentChannel().getLoadingStart() && hasMoreData()) {
            LogUtil.trackPageView("LoadMoreMail");

            ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
            ChannelManager.getInstance().loadMoreMsgFromDB(channel, -1, -1, channel.getMinCreateTime(), true);
        }

        createTimerTask();
    }

    /**
     * 时机：各个参数变化时、初始化时 server数据变化时：GetNewMsg返回时 view数据变化时：获取到新消息时
     */
    public void refreshHasMoreData() {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW
                , "isInMail()", isInMail()
                , "isGetingNewMsg", ChannelManager.getInstance().isGetingNewMsg
        );
        if (!isInMail()) {
            if (ChannelManager.getInstance().isGetingNewMsg) {
                hasMoreData = false;
            } else {
                hasMoreData = getLoadMoreMsgParam(ChatServiceController.getCurrentChannelType()) != null;
            }
        } else {
            ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
            if (channel == null) {
                LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "channel ==null");
                hasMoreData = false;
                return;
            }
            List<MsgItem> dbUserMails = DBManager.getInstance().getMsgsByTime(channel.getChatTable(), channel.getMinCreateTime(), 1);
            hasMoreData = dbUserMails.size() > 0;
            LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "hasMoreData", hasMoreData);
        }
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "hasMoreData1", hasMoreData);
    }

    private boolean isInMail() {
        return getCurrentChannel().tab == TAB_MAIL && !ChatServiceController.isInChatRoom();
    }

    public static String chatStatus = "";

    public static void setConnectionStatus(final String title) {
        chatStatus = title;
        if (!ChatServiceController.isInMailDialog() && ChatServiceController.getChatFragment() != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                public void run() {
                    if (ChatServiceController.getChatFragment() != null) {
                        if (StringUtils.isNotEmpty(title)) {
                            ChatServiceController.getChatFragment().getTitleLabel().setText(title);
                            ChatServiceController.getChatFragment().refreshNetState(ChatServiceController.getCurrentChannelType(), true);
                        } else {
                            ChatServiceController.getChatFragment().getTitleLabel()
                                    .setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_CHAT));
                        }
                    }
                }
            });
        }
    }

    public boolean hasMoreData() {
        return hasMoreData;
    }

    private ListViewLoadListener mListViewLoadListener = new ListViewLoadListener() {
        @Override
        public void refreshData() {
            if (isInMail()) {
                loadMoreMail();
            } else {
                loadMoreMsg();
            }
        }

        @Override
        public boolean getIsListViewToTop() {
            if (getCurrentChannel() == null
                    || getCurrentChannel().messagesListView == null)
                return false;
            ListView listView = getCurrentChannel().messagesListView;

            View topListView = listView.getChildAt(listView
                    .getFirstVisiblePosition());
            if ((topListView == null) || (topListView.getTop() != 0)) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public boolean getIsListViewToBottom() {
            if (getCurrentChannel() == null
                    || getCurrentChannel().messagesListView == null)
                return false;
            ListView listView = getCurrentChannel().messagesListView;
            View bottomView = listView.getChildAt(-1 + listView.getChildCount());
            if (bottomView == null)
                return false;
            if (bottomView.getBottom() > listView.getHeight()
                    || (listView.getLastVisiblePosition() != -1
                    + listView.getAdapter().getCount())) {
                return false;
            } else {
                return true;
            }
        }
    };

    private void createTimerTask() {
        stopTimerTask();
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (activity == null)
                    return;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (getCurrentChannel().pullDownToLoadListView != null) {
                                getCurrentChannel().pullDownToLoadListView.hideProgressBar();
                                resetMoreDataStart(getCurrentChannel().tab);
                            }
                        } catch (Exception e) {
                            LogUtil.printException(e);
                        }
                    }
                });
            }
        };
        if (mTimer != null)
            mTimer.schedule(mTimerTask, 2000);
    }

    private void stopTimerTask() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    private boolean hasMoreData = true;

    private OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                if (getCurrentChannel() != null
                        && getCurrentChannel().messagesListView != null) {
                    View topView = getCurrentChannel().messagesListView
                            .getChildAt(getCurrentChannel().messagesListView
                                    .getFirstVisiblePosition());
                    if ((topView != null) && (topView.getTop() == 0)
                            && !getCurrentChannel().getLoadingStart()) {
                        getCurrentChannel().pullDownToLoadListView.startTopScroll();
                    }
                }

            }

            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING)
                ChatServiceController.isListViewFling = true;
            else
                ChatServiceController.isListViewFling = false;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {
            if (getCurrentChannel() != null
                    && getCurrentChannel().pullDownToLoadListView != null
                    && getCurrentChannel().pullDownToLoadListView.getVisibility() == View.VISIBLE) {
                LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "onScroll_1");
                if (hasMoreData()) {
                    if (!getCurrentChannel().getLoadingStart()) {
                        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "onScroll_2");
                        getCurrentChannel().pullDownToLoadListView
                                .setAllowPullDownRefersh(false);
                    } else {
                        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "onScroll_3");
                        getCurrentChannel().pullDownToLoadListView
                                .setAllowPullDownRefersh(true);
                    }
                } else {
                    LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "onScroll_4");
                    getCurrentChannel().pullDownToLoadListView
                            .setAllowPullDownRefersh(true);
                }
            }
        }
    };

    public void onJoinAnnounceInvitationSuccess() {
        if (getCountryChannelView() != null) {
            // 隐藏noAllianceFrameLayout，点联盟自然会调用
            getCountryChannelView().getMessagesAdapter().onJoinAnnounceInvitationSuccess();
        }
    }

    public int getToastPosY() {
        int[] location = {0, 0};
        messagesListFrameLayout.getLocationOnScreen(location);
        return location[1] + ScaleUtil.dip2px(activity, 5);
    }

    private final int TAB_COUNTRY = 0;
    private final int TAB_ALLIANCE = 1;
    private final int TAB_CHATROOM = 2;
    private final int TAB_MAIL = 3;

    private void showTab(int tab) {
        CompatibleApiUtil.getInstance().setButtonAlpha(buttonCountry, tab == TAB_COUNTRY);
        CompatibleApiUtil.getInstance().setButtonAlpha(buttonAlliance, tab == TAB_ALLIANCE);
        CompatibleApiUtil.getInstance().setButtonAlpha(buttonChatRoom, tab == TAB_CHATROOM);
        if (tab == TAB_MAIL) {
            buttonsLinearLayout.setVisibility(View.GONE);
            unread_layout.setVisibility(View.GONE);
        } else {
            buttonsLinearLayout.setVisibility(View.VISIBLE);
            unread_layout.setVisibility(View.VISIBLE);
            //imageView2.setVisibility(View.VISIBLE);
            //imageView2.setVisibility(View.INVISIBLE);
        }

        boolean isInAlliance = false;
        // 少量异常 Attempt to read from field 'java.lang.String
        // com.chatsdk.model.UserInfo.allianceId' on a null object
        // reference
        if (UserManager.getInstance().getCurrentUser() != null) {
            isInAlliance = UserManager.getInstance().getCurrentUser().allianceId.equals("") ? false : true;
        }

        getMemberSelectButton().setVisibility(View.GONE);
        getRedPackageButton().setVisibility(View.GONE);
        getGoLiveListButton().setVisibility(View.GONE);
        getCreateChatRoomButton().setVisibility(View.GONE);
        messagesListFrameLayout.setVisibility(View.VISIBLE);
        bottomLayout.setVisibility(View.VISIBLE);
        anchor_info_layout.setVisibility(View.GONE);
        channelListFragmentLayout.setVisibility(View.GONE);
        if (tab == TAB_ALLIANCE) {
            getRedPackageButton().setVisibility(ConfigManager.isRedPackageEnabled && isInAlliance ? View.VISIBLE : View.GONE);
        } else if (tab == TAB_COUNTRY) {
            getRedPackageButton().setVisibility(ConfigManager.isRedPackageEnabled ? View.VISIBLE : View.GONE);
        } else if (tab == TAB_CHATROOM) {
            getCreateChatRoomButton().setVisibility(View.VISIBLE);
            setRedPackageSensorVisibility(View.GONE);
            messagesListFrameLayout.setVisibility(View.GONE);
            bottomLayout.setVisibility(View.GONE);
            channelListFragmentLayout.setVisibility(View.VISIBLE);

        } else if (ChatServiceController.isInChatRoom()) {
            getMemberSelectButton().setVisibility(View.VISIBLE);
            getGoLiveListButton().setVisibility(View.GONE);
        }
        if (ChatServiceController.isInLiveRoom()) {
            buttonsLinearLayout.setVisibility(View.GONE);
            anchor_info_layout.setVisibility(View.VISIBLE);
            getGoLiveListButton().setVisibility(View.VISIBLE);
            if (ChatServiceController.isAnchorHost && ChatServiceController.isInSelfLiveRoom()) {
                getMemberSelectButton().setVisibility(View.VISIBLE);
            } else {
                getMemberSelectButton().setVisibility(View.GONE);
            }

            getRedPackageButton().setVisibility(View.GONE);
            getCreateChatRoomButton().setVisibility(View.GONE);
        }

        for (int i = 0; i < getChannelViewCount(); i++) {
            if (getChannelView(i).pullDownToLoadListView != null) {
                getChannelView(i).pullDownToLoadListView.setVisibility(tab == i ? View.VISIBLE : View.GONE);
                if ((i == TAB_ALLIANCE && !isInAlliance) || (i == TAB_COUNTRY && ChatServiceController.isInDragonSencen())) {
                    getChannelView(i).pullDownToLoadListView.setVisibility(View.GONE);
                }
            }
        }

        horn_checkbox.setVisibility((tab == 0 && ConfigManager.enableChatHorn) ? View.VISIBLE : View.GONE);

        noAllianceFrameLayout.setVisibility((tab == TAB_ALLIANCE && !isInAlliance) ? View.VISIBLE : View.GONE);
        hs__dragon_chat_tip_layout.setVisibility((tab == TAB_COUNTRY && ChatServiceController.isInDragonSencen()) ? View.VISIBLE
                : View.GONE);
        relativeLayout1.setVisibility((tab == TAB_ALLIANCE && !isInAlliance) ? View.GONE : View.VISIBLE);

        if (tab == TAB_ALLIANCE && !isInAlliance && ConfigManager.getInstance().isFirstJoinAlliance && !isJoinAlliancePopupShowing) {
            try {
                ChatServiceController.doHostAction("joinAllianceBtnClick", "", "", "", true);
//				showJoinAlliancePopup();
            } catch (Exception e) {
                LogUtil.printException(e);
            }
        }

        if (tab == TAB_COUNTRY) {
            ChatServiceController.setCurrentChannelType(DBDefinition.CHANNEL_TYPE_COUNTRY);
            if (ChatServiceController.isHornItemUsed && ConfigManager.enableChatHorn) {
                horn_checkbox.setChecked(true);
                refreshBottomUI(true);
                ConfigManager.isHornBtnEnable = true;
            } else {
                horn_checkbox.setChecked(ConfigManager.isHornBtnEnable);
                refreshBottomUI(ConfigManager.isHornBtnEnable);
            }

//			if (ChatServiceController.isChatRestrictForLevel())
//			{
//				replyField.setEnabled(false);
//				replyField.setHint(LanguageManager.getLangByKey(LanguageKeys.CHAT_RESTRICT_TIP,
//						"" + ChatServiceController.getChatRestrictLevel()));
//			}
        } else {
            replyField.setEnabled(true);
            replyField.setHint("");
            if (tab == TAB_ALLIANCE) {
                ChatServiceController.setCurrentChannelType(DBDefinition.CHANNEL_TYPE_ALLIANCE);
            } else if (tab == TAB_CHATROOM) {
                ChatServiceController.isCreateChatRoom = false;
                ChatServiceController.setCurrentChannelType(DBDefinition.CHANNEL_TYPE_CHATROOM);
                notifyDataSetChanged();
            }

            refreshBottomUI(false);
        }
        setChannelViewIndex(tab);

        if (checkMessagesAdapter()) {
            oldAdapterCount = getCurrentChannel().getMessagesAdapter().getCount();
            refreshToolTip();
            this.refreshHasMoreData();

            if (getCurrentChannel().chatChannel != null) {
                getCurrentChannel().chatChannel.getTimeNeedShowMsgIndex();
                getCurrentChannel().chatChannel.markAsRead();
            }
        }
        if (tab != TAB_MAIL) {
            refreshUnreadCount();
        }
        if (ConfigManager.isRedPackageShakeEnabled) {
            // 刷新红包显示数量
            if (ChatServiceController.getChatActivity() != null) {
                ChatServiceController.getChatActivity().refreshRedPackageNum();
            }
        }
    }

    int mIndex = 0;

    private void refreshWordCount() {
        if (replyField == null || wordCount == null)
            return;

        if (replyField.getLineCount() > 2) {
            wordCount.setVisibility(View.VISIBLE);
        } else {
            wordCount.setVisibility(View.GONE);
        }
        wordCount.setText(replyField.getText().length() + "/" + curMaxInputLength);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void onBackClicked() {
    }

    private final static int sendButtonBaseWidth = 173;
    private final static int sendButtonBaseHeight = 84;
    private final static int hornCheckBoxWidth = 70;

    public void adjustHeight() {
        if (!ConfigManager.getInstance().scaleFontandUI) {
            if (addReply.getWidth() != 0 && !adjustSizeCompleted) {
                adjustSizeCompleted = true;
            }
            return;
        }

        if (addReply.getWidth() != 0 && !adjustSizeCompleted) {
            // S3手机上的尺寸(目标效果是在S3手机上调的好，界面、文字都相对于它进行缩放)
            // addReply宽度是宽度的1/4，让其高度保持长宽比，然后再计算出缩放的倍率（textRatio）
            double sendButtonRatio = (double) sendButtonBaseHeight / (double) sendButtonBaseWidth;
            float hornRatio = (float) (addReply.getWidth() * sendButtonRatio / hornCheckBoxWidth);
//			ViewHelper.setScaleX(horn_checkbox, hornRatio > 1 ? 1 : hornRatio);
//			ViewHelper.setScaleY(horn_checkbox, hornRatio > 1 ? 1 : hornRatio);

//			addReply.setLayoutParams(new LinearLayout.LayoutParams(addReply.getWidth(), (int) (addReply.getWidth() * sendButtonRatio)));

            int buttomWidth = 79;
            buttonChatRoom.setVisibility(View.GONE);
            if (ChatServiceController.isChatRoomEnable) {

                // 购买vip特权才能看到创建按钮，创建的数量达到上限也不能在创建
                int count = ChannelManager.getInstance().gettingAllRoomCount();
                boolean enbale = count > 0;
                if (!enbale) {
                    int vipPrivilege = JniController.getInstance().excuteJNIMethod("getCanCreateChatRoomNum", null);
                    enbale = vipPrivilege > 0;
                }
                if (enbale) {
                    buttomWidth = 53;
                    LinearLayout.LayoutParams buttonChatRoomParams = (LinearLayout.LayoutParams) buttonChatRoom.getLayoutParams();
                    buttonChatRoomParams.height = (int) (buttomWidth * ConfigManager.scaleRatioButton);
                    buttonChatRoom.setLayoutParams(buttonChatRoomParams);
                    buttonChatRoom.setVisibility(View.VISIBLE);
                }
            }
            LinearLayout.LayoutParams buttonCountryParams = (LinearLayout.LayoutParams) buttonCountry.getLayoutParams();
            buttonCountryParams.height = (int) (buttomWidth * ConfigManager.scaleRatioButton);
            buttonCountry.setLayoutParams(buttonCountryParams);

            LinearLayout.LayoutParams buttonAllianceParams = (LinearLayout.LayoutParams) buttonAlliance.getLayoutParams();
            buttonAllianceParams.height = (int) (buttomWidth * ConfigManager.scaleRatioButton);
            buttonAlliance.setLayoutParams(buttonAllianceParams);

            LinearLayout.LayoutParams param3 = new LinearLayout.LayoutParams((int) (13 * ConfigManager.scaleRatio),
                    (int) (17 * ConfigManager.scaleRatio), 1);
            param3.gravity = Gravity.CENTER_VERTICAL;
            tooltipArrow.setLayoutParams(param3);

            // TODO 删除不用的
            // 9.png图片两端的宽度无法放大，只放大高度显得太狭长
            // RelativeLayout.LayoutParams param2 = new
            // RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
            // LayoutParams.WRAP_CONTENT);
            // param2.setMargins(dip2px(activity, -4), dip2px(activity, -2),
            // dip2px(activity, -1), 0);
            // param2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            // imageView2.setLayoutParams(param2);
            // imageView2.setScaleType(ScaleType.FIT_XY);
            // ViewHelper.setScaleY(imageView2, (float) scaleRatioButton);

            ScaleUtil.adjustTextSize(addReply, ConfigManager.scaleRatio);
            ScaleUtil.adjustTextSize(replyField, ConfigManager.scaleRatio);
            ScaleUtil.adjustTextSize(wordCount, ConfigManager.scaleRatio);
            ScaleUtil.adjustTextSize(buttonCountry, ConfigManager.scaleRatio);
            ScaleUtil.adjustTextSize(buttonAlliance, ConfigManager.scaleRatio);
            ScaleUtil.adjustTextSize(buttonChatRoom, ConfigManager.scaleRatio);

            ScaleUtil.adjustTextSize(buttonJoinAlliance, ConfigManager.scaleRatio);
            ScaleUtil.adjustTextSize(noAllianceTipText, ConfigManager.scaleRatio);
            ScaleUtil.adjustTextSize(dragon_chat_tip_text, ConfigManager.scaleRatio);
            ScaleUtil.adjustTextSize(tooltipLabel, ConfigManager.scaleRatio);
            ScaleUtil.adjustTextSize(horn_scroll_text, ConfigManager.scaleRatio);
            ScaleUtil.adjustTextSize(horn_name, ConfigManager.scaleRatio);

            adjustSizeCompleted = true;

            if (lazyLoading) {
                activity.showProgressBar();
                onBecomeVisible();
            }
        }
    }

    public void showRedPackageConfirm(final MsgItem msgItem) {
        if (activity == null)
            return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    activity.showRedPackagePopup(msgItem);
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        });
    }

    public void hideRedPackageConfirm() {
        if (activity == null)
            return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    activity.hideRedPackagePopup();
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        });

    }

    public MsgItem getCurrentRedPackageItem() {
        if (activity != null) {
            return activity.getRedPackagePopItem();
        }
        return null;
    }

    protected void onContentAreaTouched() {
        hideSoftKeyBoard();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onDestroy() {
        stopTimerTask();
        stopLiveMemberTask();
        ConfigManager.activityType = -1;
        ChatServiceController.isContactMod = false;
        ChatServiceController.needShowAllianceDialog = false;

        if (tooltipLayout != null)
            tooltipLayout.setOnClickListener(null);
        if (buttonJoinAlliance != null)
            buttonJoinAlliance.setOnClickListener(null);
        if (channelButton != null) {
            for (int i = 0; i < channelButton.size(); i++) {
                channelButton.get(i).setTag(null);
                channelButton.get(i).setOnClickListener(null);
            }
            channelButton.clear();
            channelButton = null;
        }

        try {
            getMemberSelectButton().setOnClickListener(null);
//			if (getShowFriendButton() != null)
//			{
//				getShowFriendButton().setOnClickListener(null);
//			}
        } catch (Exception e) {
            LogUtil.printException(e);
        }

        if (addReply != null) {
            addReply.setOnClickListener(null);
            addReply = null;
        }
		if(ConfigManager.isSpeechRecognition && voice_btn != null) {
			voice_btn.setOnClickListener(null);
			voice_btn = null;
		}
        if (replyField != null) {
            replyField.setOnEditorActionListener(null);
            replyField.removeTextChangedListener(textChangedListener);
            replyField = null;
        }
        textChangedListener = null;

        if (horn_checkbox != null) {
            horn_checkbox.setOnCheckedChangeListener(null);
            horn_checkbox = null;
        }

        if (messagesListFrameLayout != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            if (messagesListFrameLayout.getViewTreeObserver() != null) {
                messagesListFrameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
            }
            messagesListFrameLayout.removeAllViews();
            messagesListFrameLayout = null;
        }
        if (unread_layout != null)
            unread_layout = null;
        onGlobalLayoutListener = null;

        mOnScrollListener = null;
        mListViewLoadListener = null;

        noAllianceFrameLayout = null;
        relativeLayout1 = null;
        buttonsLinearLayout = null;
        bottomLayout = null;
        attachScreenshotMenu = null;
        //imageView1 = null;
        //imageView2 = null;
        horn_tip_layout = null;
        horn_text_tip = null;
        wordCount = null;
        messageBox = null;
//		header = null;
        buttonCountry = null;
        buttonAlliance = null;
        buttonChatRoom = null;
        tooltipLayout = null;
        tooltipLabel = null;
        tooltipArrow = null;
        horn_close_btn = null;
        buttonJoinAlliance = null;
        noAllianceTipText = null;
        hs__dragon_chat_tip_layout = null;
        dragon_chat_tip_text = null;
        tip_no_mail_textView = null;
        horn_scroll_layout = null;
        mTimerTask = null;
        horn_name = null;
        horn_scroll_text = null;
        sendRedPackage = null;

		stopLanternTimerTask();
        if (icon_net_btn != null)
            icon_net_btn.setOnClickListener(null);
        icon_net_btn = null;
        anchor_info_layout = null; //所有直播信息界面
        if (anchor_pic_icon != null) {
            anchor_pic_icon.setOnClickListener(null);
        }
        anchor_pic_icon = null;
        anchor_room_name = null;
        anchor_state_text = null;
        anchor_tip_text = null;
        if (voice_player_btn != null) {
            voice_player_btn.setOnClickListener(null);
            voice_player_btn = null;
        }

        liveMemberTimerTask = null;
        if (chatRoomAdapter != null) {
            chatRoomAdapter.destroy();
            chatRoomAdapter = null;
        }

        if (mListView != null) {
            mListView.clearAdapter();
            mListView.setMenuCreator(null);
            mListView.setOnItemClickListener(null);
            mListView.setOnMenuItemClickListener(null);
            mListView = null;
        }

        if (channelListPullView != null) {
            channelListPullView.setOnRefreshListener(null);
            channelListPullView = null;
        }

        channelListFragmentLayout = null;
        ((ChatActivity) getActivity()).fragment = null;

        super.onDestroy();
    }

    protected ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;
    private TextWatcher textChangedListener;

    private Timer liveMemberTimer;
    private TimerTask liveMemberTimerTask;

    public void refreshMemberCount() {
        if (liveMemberTimer == null && ChatServiceController.isFromBd)
            liveMemberTimer = new Timer();
        if (liveMemberTimerTask == null && ChatServiceController.isFromBd) {
            liveMemberTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (ChatServiceController.isNeedJoinLive && !ChatServiceController.isInSelfLiveRoom()) {
                        WebSocketManager.getInstance().chatRoomInvite(ChatServiceController.curLiveRoomId, UserManager.getInstance().getCurrentUserId());
                    }
                    WebSocketManager.getInstance().getRoomMembersCount(ChatServiceController.curLiveRoomId);
                    JniController.getInstance().excuteJNIVoidMethod("postToCppRefreshLiveRoomInfo", new Object[]{});
                }
            };
            liveMemberTimer.schedule(liveMemberTimerTask, 1000, 30000);
        }

    }

    public void stopLiveMemberTask() {
        if (liveMemberTimer != null) {
            liveMemberTimer.cancel();
            liveMemberTimer.purge();
            liveMemberTimer = null;
        }
    }

    public void refreshLiveView() {
        //改名之后重新刷新界面
//		UserInfo userInfo= UserManager.getInstance().getUser(ChatServiceController.liveUid);
        UserInfo userInfo = new UserInfo();
        userInfo.headPic = "g026";
        userInfo.uid = ChatServiceController.liveUid;
        userInfo.headPicVer = ChatServiceController.livePicVer;
        ImageUtil.setHeadImage(activity, userInfo.headPic, anchor_pic_icon, userInfo);
        getTitleLabel().setText(ChatServiceController.liveRoomName);
        anchor_room_name.setText(ChatServiceController.liveUserName);
        if (listenNumText != null)
            listenNumText.setText(ChatServiceController.listenRoomNumber + "");
        if (ChatServiceController.livePushStatus) {
            anchor_state_text.setText(LanguageManager.getLangByKey("80000842"));
            anchor_state_text.setTextColor(Color.GREEN);
        } else {
            anchor_state_text.setText(LanguageManager.getLangByKey("80000843"));
            anchor_state_text.setTextColor(Color.RED);
        }
        anchor_tip_text.setText(ChatServiceController.liveTipContent);
        if ((ChatServiceController.isAnchorHost && ChatServiceController.isInSelfLiveRoom()) || (!ChatServiceController.isAnchorHost && !ChatServiceController.livePushStatus)) {
            voice_player_btn.setVisibility(View.INVISIBLE);
            anchor_state_text.setVisibility(View.VISIBLE);
        } else {
            voice_player_btn.setVisibility(View.VISIBLE);
            anchor_state_text.setVisibility(View.INVISIBLE);
        }
        if (!ChatServiceController.canPull) {
            voice_player_btn.setVisibility(View.INVISIBLE);
            anchor_state_text.setVisibility(View.VISIBLE);
        }
        if (ChatServiceController.livePullStatus) {
//			voice_player_btn.setBackgroundDrawable(activity.getResources().getDrawable(ResUtil.getId(activity, "drawable", "voice_play")));
            voice_player_btn.setText(LanguageManager.getLangByKey(LanguageKeys.STOP_PLAY_TITLE));
            voice_player_btn.setTextColor(ResUtil.getColor(this.getContext(), "stop_title"));
        } else {
//			voice_player_btn.setBackgroundDrawable(activity.getResources().getDrawable(ResUtil.getId(activity, "drawable", "voice_pause")));
            voice_player_btn.setText(LanguageManager.getLangByKey(LanguageKeys.PLAY_TITLE));
            voice_player_btn.setTextColor(ResUtil.getColor(this.getContext(), "start_title"));
        }
        refreshMemberCount();
        changeAnchorComplete = false;
    }

    public void refreshNetState(int tab, boolean show) {
        if (!ChatServiceController.chat_line_tips) {
            return;
        }
        if (icon_net_btn != null) {
            if (tab != DBDefinition.CHANNEL_TYPE_COUNTRY && tab != DBDefinition.CHANNEL_TYPE_ALLIANCE) {
                icon_net_btn.setVisibility(View.GONE);
            } else {
                isNeedShowWifi = show;
                if (show) {
                    icon_net_btn.setVisibility(View.VISIBLE);
                } else {
                    icon_net_btn.setVisibility(View.GONE);
                }
            }

        }

    }

	/**

	 * Fire an intent to start the speech recognition activity.

	 */

	private void startVoiceRecognitionActivity() {

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
		this.startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	/**

	 * Handle the results from the recognition activity.

	 */

@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
			ArrayList matches = data.getStringArrayListExtra(
					RecognizerIntent.EXTRA_RESULTS);
			String replyText = replyField.getText().toString();
			replyField.setText(replyText.concat(matches.get(0).toString()));
		}
		super.onActivityResult(requestCode, resultCode, data);

	}


	public void beginAnimation(){

		if(mLanterTimer == null) {
			mLanterTimer = new Timer();
		}
		if(mLanterTimerTask != null)
			return;
		mLanterTimerTask = new TimerTask() {
			public void run() {
				if (ChatServiceController.animateTime!=0 && (ChatServiceController.animateAllTime % ChatServiceController.animateTime) == 0) {
					if(UserManager.getInstance().userAnimateList.size() > 0) {
						String uid = UserManager.getInstance().userAnimateList.get(0);
						UserInfo user = UserManager.getInstance().getUser(uid);
						user.lastAnimateTime = TimeManager.getInstance().getCurrentTime();
						UserManager.getInstance().updateUser(user);
						DBManager.getInstance().updateUser(user);
						if(ChatServiceController.animateAllTime % ChatServiceController.animateTime == 0){
							ChatServiceController.animateState =1; //开启动画
						}
					}

					if (ChatServiceController.animateTime != 0 && (ChatServiceController.animateAllTime % ChatServiceController.animateTime) == 0
							) {
						if(ChatServiceController.animateAllTime > 0 && UserManager.getInstance().userAnimateList.size() == 0) {
							ChatServiceController.animateState = 2;
						}

						if (UserManager.getInstance().userAnimateList.size() > 0) {
							UserManager.getInstance().userAnimateList.remove(0);
							if(UserManager.getInstance().userAnimateList.size() > 0){
								ChatServiceController.animateState =0;
							}

						}
					}
					if (ChatServiceController.animateState > 0) {
						mHandler.sendEmptyMessage(ChatServiceController.animateState);

					}
				}
				ChatServiceController.animateAllTime++;

			}
		};

		mLanterTimer.schedule(mLanterTimerTask,0,1000);

	}

	public void endAnimation(){
		synchronized(UserManager.getInstance()) {
			if( UserManager.getInstance().userAnimateList != null && UserManager.getInstance().userAnimateList.size() > 0)
				UserManager.getInstance().userAnimateList.clear();
		}

		ChatServiceController.animateState = 0;
		ChatServiceController.animateAllTime = 0;
		ChatServiceController.isInLantern = false;
//		stopLanternTimerTask();

	}

	private void stopLanternTimerTask() {
		if (mLanterTimer != null) {
			if (mLanterTimerTask != null)
			{
				mLanterTimerTask.cancel();
				mLanterTimerTask = null;
			}
			mLanterTimer.cancel();
			mLanterTimer.purge();
			mLanterTimer = null;
		}
	}
}
