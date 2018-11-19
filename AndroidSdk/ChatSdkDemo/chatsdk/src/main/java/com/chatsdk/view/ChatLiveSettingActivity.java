package com.chatsdk.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.host.DummyHost;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.UserInfo;
import com.chatsdk.model.UserManager;
import com.chatsdk.net.WebSocketManager;
import com.chatsdk.util.ImageUtil;
import com.chatsdk.util.LogUtil;
import com.chatsdk.view.actionbar.MyActionBarActivity;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class ChatLiveSettingActivity extends MyActionBarActivity
{
	public int	settingType = 0;
	private int BAN_USER = 0;
	private int ROOM_MANAGER = 1;
	private ListView banUserList;
	private BanUserItemAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		ChatServiceController.isRunning = true;
		ChatServiceController.toggleFullScreen(true, true, this);
		super.onCreate(savedInstanceState);
//		Bundle extras = getIntent().getExtras();
//		if (extras != null) {
//			this.bundle = new Bundle(extras);
//			if (extras.getInt("channelType") >= 0) {
//				settingType = extras.getInt("channelType");
//			}
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			titleLabel.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_UNBAN));

			optionButton.setVisibility(View.GONE);
			editButton.setVisibility(View.GONE);
			writeButton.setVisibility(View.GONE);
			contactsButton.setVisibility(View.GONE);
			returnButton.setVisibility(View.GONE);
			createChatRoomButton.setVisibility(View.GONE);
			redPackageButton.setVisibility(View.GONE);
			goLiveListButton.setVisibility(View.GONE);
			if (settingType == BAN_USER) {
				inflater.inflate(R.layout.cs_live_room_banuser, fragmentLayout, true);
				banUserList = (ListView) this.findViewById(R.id.ban_user_list);
				if(StringUtils.isNotEmpty(ChatServiceController.curLiveRoomId)) {
					WebSocketManager.getInstance().getBanUserForRoom(ChatServiceController.curLiveRoomId);
				}
			}else if(settingType == ROOM_MANAGER){
				inflater.inflate(R.layout.cs_live_room_banuser, fragmentLayout, true);
			}

//		}
		ChatServiceController.toggleFullScreen(true, true, this);
	}

	protected void showBackground()
	{
		fragmentLayout.setBackgroundResource(R.drawable.ui_paper_3c);
	}

	@Override
	public void onResume()
	{
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
		ChatServiceController.isRunning = false;
	}

	@Override
	public void exitActivity()
	{
		if (ChatServiceController.getInstance().isInDummyHost()
				&& ((DummyHost) (ChatServiceController.getInstance().host)).actionAfterResume != null)
		{
			((DummyHost) (ChatServiceController.getInstance().host)).actionAfterResume = null;
			return;
		}

		super.exitActivity();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
//		System.gc();
//		System.runFinalization();
	}

	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus)
		{
			// 这里调onBecomeVisible()与adjustHeight中调差不多
			// showProgressBar();
			// ((ChatFragment) fragment).onBecomeVisible();
		}
		else
		{
		}
	}

	public void refreshListView(){
		try {
			adapter = new BanUserItemAdapter(this,UserManager.getInstance().getBanLiveUidList());
			banUserList.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}catch (Exception e){
			LogUtil.printException(e);
		}

	}
	public static class BanUserItemAdapter extends BaseAdapter {
		private Context c;
		private List<String> items;
		private ArrayList<String> itemsBackup;
		private LayoutInflater		inflater;
		public BanUserItemAdapter(Context f, ArrayList<String> objects)
		{
			this.c = f;
			this.itemsBackup = objects;
			this.items = (ArrayList<String>)itemsBackup.clone();
			this.inflater = ((LayoutInflater) this.c.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		}
		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int i) {
			return items.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View convertView, ViewGroup viewGroup) {
			ViewHolder holder = new ViewHolder();
			if(convertView == null) {
				convertView = inflater.inflate(R.layout.cs__unban_user_item, viewGroup,false);
				ImageView headImage = (ImageView) convertView.findViewById(R.id.user_headImage);
				TextView userName = (TextView) convertView.findViewById(R.id.user_name);
				Button unBanBtn = (Button) convertView.findViewById(R.id.unbanBtn);
				holder.headImage = headImage;
				holder.unBanBtn = unBanBtn;
				holder.userName = userName;
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder)convertView.getTag();
			}
			final UserInfo userInfo = UserManager.getInstance().getUser(items.get(i));
			if(userInfo == null)
				return convertView;
			holder.unBanBtn.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_UNBAN));
			holder.unBanBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if(ChatServiceController.isAnchorHost) {
						WebSocketManager.getInstance().unBanLiveMember(ChatServiceController.curLiveRoomId, userInfo.uid);
					}
				}
			});
			ImageUtil.setHeadImage(c,userInfo.headPic,holder.headImage,userInfo);
			holder.userName.setText(userInfo.userName);
			return convertView;
		}
		public void notifyDataSetChanged() {
			this.items = (ArrayList<String>)this.itemsBackup.clone();
			super.notifyDataSetChanged();
		}
		public class ViewHolder{
			public ImageView headImage;
			public Button unBanBtn;
			public TextView userName;
			public ViewHolder(){

			}

		}
	}

}