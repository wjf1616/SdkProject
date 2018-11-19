package com.chatsdk.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.controller.MenuController;
import com.chatsdk.controller.ServiceInterface;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.UserInfo;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.db.ChatTable;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.db.DBManager;
import com.chatsdk.model.viewholder.ViewHolderHelper;
import com.chatsdk.net.WebSocketManager;
import com.chatsdk.util.ImageUtil;
import com.chatsdk.util.RoundImageView;
import com.chatsdk.util.ScaleUtil;
import com.chatsdk.view.actionbar.MyActionBarActivity;
import com.chatsdk.view.adapter.MemberGridAdapter;
import com.nineoldandroids.view.ViewHelper;

public class ChatRoomSettingActivity extends MyActionBarActivity
{
	private NewGridView			member_grid_view;
	private MemberGridAdapter	mAdapter;
	private TextView			name_text;
	private TextView			chat_room_name;
	private TextView			tip_text;
	private TextView			level_text;
	private RelativeLayout		change_name_layout;
	private RelativeLayout		leave_layout;
	private TextView			top_button_text;
	public ImageView			top_button;
	private boolean				adjustSizeCompleted	= false;
	private boolean				isCreate			= false;
	public ProgressBar 			roomProgressBar;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		ChatServiceController.toggleFullScreen(true, true, this);
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		inflater.inflate(R.layout.chat_room_set_fragment, fragmentLayout, true);
		String title = UserManager.getInstance().getCurrentMail().opponentName;
		String roomtitle = title;
		final ChatChannel channel = ChannelManager.getInstance().getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, UserManager.getInstance().getCurrentMail().opponentUid));
		if (channel != null && channel.memberUidArray != null && channel.memberUidArray.size() > 0) {
			roomtitle = LanguageManager.getLangByKey(LanguageKeys.CHATROOM_MEMBER, channel.memberUidArray.size() + "");
		}
		titleLabel.setText(roomtitle);
		optionButton.setVisibility(View.GONE);
		returnButton.setVisibility(View.GONE);
		editButton.setVisibility(View.GONE);
		writeButton.setVisibility(View.GONE);
//		showFriend.setVisibility(View.GONE);
//		imageDelButton.setVisibility(View.GONE);
//		imageChooseComfirmButton.setVisibility(View.GONE);
//		allianceShareBtn.setVisibility(View.GONE);
//		allianceShareSend.setVisibility(View.GONE);
		member_grid_view = (NewGridView) findViewById(R.id.member_grid_view);
		mAdapter = new MemberGridAdapter(this);
		member_grid_view.setAdapter(mAdapter);

		top_button = (ImageView) findViewById(R.id.top_button);
		if(channel.settings!= null && (channel.settings.equals("1") || channel.settings.equals("2"))){
			top_button.setImageDrawable(this.getResources().getDrawable(R.drawable.btn_selected));
		}else{
			top_button.setImageDrawable(this.getResources().getDrawable(R.drawable.btn_unselected));
		}
		top_button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

				ChatChannel curChannel = ChannelManager.getInstance().getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, UserManager.getInstance().getCurrentMail().opponentUid));
				if (curChannel == null) {
					return;
				}

				if(curChannel.settings!=null && curChannel.settings.equals("0")){
					List<ChatChannel> channelList = ChannelManager.getInstance().getAllChatRoomChannel();
					Iterator<ChatChannel> it = channelList.iterator();
					while(it.hasNext()) {
						ChatChannel channel = it.next();
						if (channel.settings!=null && (channel.settings.equals("1") || channel.settings.equals("2")) ) {
							String content = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_TOP_WARN);
							MenuController.topChatRoomConfirm(ChatRoomSettingActivity.this, content, curChannel);
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
				refreshData();
			}
		});
		top_button_text = (TextView) findViewById(R.id.top_button_text);
		top_button_text.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_TOP_TEXT));
		name_text = (TextView) findViewById(R.id.name_text);
		name_text.setText(LanguageManager.getLangByKey(LanguageKeys.CHATROOM_NAME));
		chat_room_name = (TextView) findViewById(R.id.chat_room_name);
		if(StringUtils.isNotEmpty(roomtitle) && title.length()>15)
		{
			title = title.substring(0, 15);
			title+= "...";
		}
		chat_room_name.setText(title);
		change_name_layout = (RelativeLayout) findViewById(R.id.change_name_layout);
		change_name_layout.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				ServiceInterface.showChatRoomNameModifyActivity(ChatRoomSettingActivity.this);
			}
		});

		leave_layout = (RelativeLayout) findViewById(R.id.leave_layout);
		leave_layout.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				String content = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_QUIT);
				String founderUid = ChannelManager.getInstance()
						.getChatRoomFounderByKey(UserManager.getInstance().getCurrentMail().opponentUid);
				if(UserManager.getInstance().getCurrentUserId().equals(founderUid)){
					content = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_DISSMIS) + " " + content;
				}
				MenuController.quitChatRoomConfirm(ChatRoomSettingActivity.this, content);
			}
		});

		// 找到竞技场聊天室
		if (ChannelManager.getInstance().isArenaChatRoom(channel.channelID)){
			change_name_layout.setVisibility(View.GONE);
			leave_layout.setVisibility(View.GONE);
		}


		level_text = (TextView) findViewById(R.id.level_text);
		level_text.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_LEVEL_CHAT_ROOM));

		tip_text = (TextView) findViewById(R.id.tip_text);
		tip_text.setVisibility(View.GONE);
		//tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_LEVEL_CHAT_ROOM));

		ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				adjustHeight();
			}
		};
		fragmentLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

		roomProgressBar = (ProgressBar) findViewById(R.id.cs_room_progress_bar);
		hideRoomProgressBar();

		isCreate = true;
	}

	public void showRoomProgressBar() {
		roomProgressBar.setVisibility(View.VISIBLE);
	}

	public void hideRoomProgressBar() {
		if (roomProgressBar != null && roomProgressBar.getVisibility() != View.GONE)
			roomProgressBar.setVisibility(View.GONE);
	}


	public void refreshData()
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				notifyDataChanged();
			}
		});
	}
	
	public void refreshChatRoomName()
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				String title = UserManager.getInstance().getCurrentMail().opponentName;
				if(StringUtils.isNotEmpty(title) && title.length()>15)
				{
					title = title.substring(0, 15);
					title+= "...";
				}
				chat_room_name.setText(title);
			}
		});
	}
	
	public void refreshTitle()
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				String roomtitle = "";
				ChatChannel channel = ChannelManager.getInstance().getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, UserManager.getInstance().getCurrentMail().opponentUid));
				if (channel != null && channel.memberUidArray != null && channel.memberUidArray.size() > 0) {
					roomtitle = LanguageManager.getLangByKey(LanguageKeys.CHATROOM_MEMBER, channel.memberUidArray.size() + "");
				}
				if(StringUtils.isNotEmpty(roomtitle) && roomtitle.length()>15)
				{
					roomtitle = roomtitle.substring(0, 15);
					roomtitle+= "...";
				}
				titleLabel.setText(roomtitle);
			}
		});
	}

	private void adjustHeight()
	{
		if (!ConfigManager.getInstance().scaleFontandUI)
		{
			return;
		}

		if (!adjustSizeCompleted)
		{
			int length = (int) (ScaleUtil.dip2px(this, 50) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
			if (member_grid_view != null)
				member_grid_view.setColumnWidth(length);

			ScaleUtil.adjustTextSize(chat_room_name, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(name_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(top_button_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(tip_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(level_text, ConfigManager.scaleRatio);

			if (top_button != null) {
				LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) top_button.getLayoutParams();
				layoutParams.width = (int) (ScaleUtil.dip2px(this, 60) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
				layoutParams.height = (int) (ScaleUtil.dip2px(this, 20) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
				top_button.setLayoutParams(layoutParams);
			}

			adjustSizeCompleted = true;
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	protected void onPause()
	{
		isCreate = false;
		super.onPause();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if(ChatServiceController.isShowProgressBar)
			showRoomProgressBar();
		if (!isCreate) {
			notifyDataChanged();
		}
	}

	private void notifyDataChanged()
	{
		if (mAdapter != null)
		{
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if(!ChatServiceController.isShowProgressBar)
						hideRoomProgressBar();
					mAdapter.refreshData();
					if(top_button != null){
						ChatChannel curChannel = ChannelManager.getInstance().getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, UserManager.getInstance().getCurrentMail().opponentUid));
						if (curChannel != null) {
							if(curChannel.settings!=null && curChannel.settings.equals("1")){
								top_button.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.btn_selected));
							}else{
								top_button.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.btn_unselected));
							}
						}
					}
					mAdapter.notifyDataSetChanged();
				}
			});
		}
	}

	public void showMemberSelectActivity()
	{
		ServiceInterface.showMemberSelectorActivity(this, false);
	}

}
