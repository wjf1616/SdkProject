package com.chatsdk.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.UserInfo;
import com.chatsdk.model.UserManager;
import com.chatsdk.util.CompatibleApiUtil;
import com.chatsdk.util.ImageUtil;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.ResUtil;

public class ExpandableListAdapter extends BaseExpandableListAdapter
{
	private Context									_context;
	// header titles
	private List<String>							_listDataHeader;
	// child data in format of header title, child title
	private HashMap<String, ArrayList<UserInfo>>	_listDataChild;
	public MemberSelectorFragment					fragment	= null;
	private LayoutInflater							inflater;
	private ArrayList<String>						memberUidArray;
	private HashMap<String, Boolean>				selectStateMap;

	public ExpandableListAdapter(Context context, MemberSelectorFragment fragment, List<String> listDataHeader,
			HashMap<String, ArrayList<UserInfo>> listChildData, boolean isAllianceMember)
	{
		this._context = context;
		this._listDataHeader = listDataHeader;
		this._listDataChild = listChildData;
		this.inflater = ((LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		this.fragment = fragment;

		memberUidArray = UserManager.getInstance().getSelectMemberUidArr();
		if (memberUidArray == null)
		{
			memberUidArray = new ArrayList<String>();
		}
		for (int i = 0; i < memberUidArray.size(); i++)
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "memberUidArray " + i, memberUidArray.get(i));
		}
		LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_VIEW, "isAllianceMember", isAllianceMember);
		resetSelectStateMap(isAllianceMember);
	}

	public void setSelectState(String uid, Boolean selected)
	{
		if (selectStateMap != null)
		{
			selectStateMap.put(uid, selected);
		}
	}

	public HashMap<String, Boolean> getSelectStateMap()
	{
		return selectStateMap;
	}

	private void resetSelectStateMap(boolean isAllianceMember)
	{
		selectStateMap = new HashMap<String, Boolean>();

		if (isAllianceMember)
		{
			HashMap<String, UserInfo> memberInfoMap = UserManager.getInstance().getChatRoomMemberInfoMap();
			if (memberInfoMap != null && memberInfoMap.size() > 0)
			{
				Set<String> uidKeySet = memberInfoMap.keySet();
				for (String uid : uidKeySet)
				{
					if (!uid.equals(""))
						selectStateMap.put(uid, Boolean.FALSE);
				}
			}
			if (memberUidArray != null && memberUidArray.size() > 0)
			{
				for (int i = 0; i < memberUidArray.size(); i++)
				{
					String uid = memberUidArray.get(i);
					if (selectStateMap.containsKey(uid))
					{
						selectStateMap.put(uid, Boolean.TRUE);
					}
				}
				if (!ChatServiceController.isCreateChatRoom && !ChatServiceController.isInChatRoom()
						&& !UserManager.getInstance().getCurrentMail().opponentUid.equals(""))
				{
					if (selectStateMap.containsKey(UserManager.getInstance().getCurrentMail().opponentUid))
					{
						selectStateMap.put(UserManager.getInstance().getCurrentMail().opponentUid, Boolean.TRUE);
					}
				}
			}
		}
		else
		{
			String resultStr = "";
			if (ChatServiceController.isFriendEnable)
			{
				resultStr = LanguageManager.getLangByKey(LanguageKeys.TITLE_FRIEND_LIST);
				if (StringUtils.isEmpty(resultStr))
					resultStr = "好友列表";
			}
			else
			{
				resultStr = LanguageManager.getLangByKey(LanguageKeys.TIP_SEARCH_RESULT);
				if (StringUtils.isEmpty(resultStr))
					resultStr = "搜索结果";
			}

			ArrayList<String> memberArr = UserManager.getInstance().getSelectMemberUidArr();
			if (memberArr == null)
			{
				memberArr = new ArrayList<String>();
			}

			if (_listDataChild.containsKey(resultStr))
			{
				ArrayList<UserInfo> userArr = _listDataChild.get(resultStr);
				if (userArr != null && userArr.size() > 0)
				{
					for (int i = 0; i < userArr.size(); i++)
					{
						UserInfo user = (UserInfo) (userArr.get(i));
						if (user != null)
						{
							String uid = user.uid;
							if (!uid.equals(""))
							{
								if (memberArr.contains(uid) || fragment.memberUidAdded.contains(uid))
									selectStateMap.put(uid, Boolean.TRUE);
								else
									selectStateMap.put(uid, Boolean.FALSE);
							}

						}
					}
				}
			}

			if (!ChatServiceController.isFriendEnable)
			{
				ArrayList<String> nonAllianceArr = UserManager.getInstance().getSelctedMemberArr(false);
				if (nonAllianceArr != null && nonAllianceArr.size() > 0)
				{
					for (int i = 0; i < nonAllianceArr.size(); i++)
					{
						String uid = nonAllianceArr.get(i);
						if (!uid.equals(""))
							selectStateMap.put(uid, Boolean.TRUE);
					}
				}
			}

		}
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon)
	{
		if (this._listDataHeader.size() > 0 && groupPosition < this._listDataHeader.size() && _listDataChild != null)
		{
			String key = this._listDataHeader.get(groupPosition);
			if (key != null && !key.equals(""))
			{
				ArrayList<UserInfo> userArr = this._listDataChild.get(key);
				if (userArr != null && userArr.size() > childPosititon)
				{
					return userArr.get(childPosititon);
				}
			}
		}
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		return childPosition;
	}

	class OnUserCheckedChangeListener implements OnCheckedChangeListener
	{
		public OnUserCheckedChangeListener(UserHolder holder)
		{
			mHolder = holder;
		}

		private UserHolder	mHolder;

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			String posStr = mHolder.checkBox.getTag().toString();
			if (posStr.equals(""))
				return;
			String[] posArr = posStr.split("_");
			int groupPosition = Integer.parseInt(posArr[0]);
			int childPosition = Integer.parseInt(posArr[1]);
			UserInfo userInfo = (UserInfo) getChild(groupPosition, childPosition);
			if (userInfo == null)
				return;

			HashMap<String, UserInfo> memberInfoMap = UserManager.getInstance().getChatRoomMemberInfoMap();
			Set<String> allianceUidKeySet = null;
			if (memberInfoMap != null && memberInfoMap.size() > 0)
			{
				allianceUidKeySet = memberInfoMap.keySet();
			}
			if (isChecked)
			{
				if (!selectStateMap.get(userInfo.uid))
				{
					selectStateMap.put(userInfo.uid, Boolean.TRUE);
					if (fragment.memberTab == 1)
					{
						if (!memberUidArray.contains(userInfo.uid) && !fragment.memberUidAdded.contains(userInfo.uid))
						{
							fragment.memberUidAdded.add(userInfo.uid);
						}
						if (fragment.memberUidRemoved.contains(userInfo.uid))
						{
							fragment.memberUidRemoved.remove(userInfo.uid);
						}
					}
					else if (fragment.memberTab == 2)
					{
						if (allianceUidKeySet != null && allianceUidKeySet.contains(userInfo.uid))
						{
							if (fragment.memberUidRemoved.contains(userInfo.uid))
							{
								fragment.memberUidRemoved.remove(userInfo.uid);
							}
						}
						else
						{
							if (fragment.commonMemberUidRemoved.contains(userInfo.uid))
							{
								fragment.commonMemberUidRemoved.remove(userInfo.uid);
							}
						}

						if (!memberUidArray.contains(userInfo.uid))
						{
							if (allianceUidKeySet != null && allianceUidKeySet.contains(userInfo.uid))
							{
								if (!fragment.memberUidAdded.contains(userInfo.uid))
								{
									fragment.memberUidAdded.add(userInfo.uid);
								}
							}
							else
							{
								if (!fragment.commonMemberUidAdded.contains(userInfo.uid))
								{
									fragment.commonMemberUidAdded.add(userInfo.uid);
								}
							}
						}
					}
					fragment.onSelectionChanged();
				}
			}
			else
			{
				if (selectStateMap.get(userInfo.uid))
				{
					selectStateMap.put(userInfo.uid, Boolean.FALSE);
					if (fragment.memberTab == 1)
					{
						if (fragment.memberUidAdded.contains(userInfo.uid))
						{
							fragment.memberUidAdded.remove(userInfo.uid);
						}
						if (memberUidArray.contains(userInfo.uid) && !fragment.memberUidRemoved.contains(userInfo.uid))
						{
							fragment.memberUidRemoved.add(userInfo.uid);
						}
					}
					else if (fragment.memberTab == 2)
					{
						if (allianceUidKeySet != null && allianceUidKeySet.contains(userInfo.uid))
						{
							if (fragment.memberUidAdded.contains(userInfo.uid))
							{
								fragment.memberUidAdded.remove(userInfo.uid);
							}
						}
						else
						{
							if (fragment.commonMemberUidAdded.contains(userInfo.uid))
							{
								fragment.commonMemberUidAdded.remove(userInfo.uid);
							}
						}

						if (memberUidArray.contains(userInfo.uid))
						{
							if (allianceUidKeySet != null && allianceUidKeySet.contains(userInfo.uid))
							{
								if (!fragment.memberUidRemoved.contains(userInfo.uid))
								{
									fragment.memberUidRemoved.add(userInfo.uid);
								}
							}
							else
							{
								if (!fragment.commonMemberUidRemoved.contains(userInfo.uid))
								{
									fragment.commonMemberUidRemoved.add(userInfo.uid);
								}
							}
						}
					}
					fragment.onSelectionChanged();
				}
			}
		}
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		UserInfo userInfo = null;
		UserHolder holder = null;
		if (groupPosition < 0 || groupPosition >= _listDataHeader.size() || childPosition < 0)
		{
			return null;
		}
		else
		{
			userInfo = (UserInfo) getChild(groupPosition, childPosition);
			if (userInfo == null)
			{
				return null;
			}
		}

		if (convertView == null)
		{
			convertView = this.inflater.inflate(R.layout.list_item, null);
			holder = new UserHolder();
			holder.findView(convertView);
			holder.checkBox.setOnCheckedChangeListener(new OnUserCheckedChangeListener(holder));
		}
		else
		{
			holder = (UserHolder) convertView.getTag();
		}

		if (holder == null)
			return null;

		if(StringUtils.isNotEmpty(userInfo.uid))
			holder.headImage.setTag(userInfo.uid);
		ImageUtil.setHeadImage(_context, userInfo.headPic, holder.headImage, userInfo);
		String name = "";
		if (UserManager.getInstance().getCurrentUser() != null && userInfo.serverId > 0) {
			name = UserManager.getInstance().getCurrentUser().getNameWithServerId(userInfo.asn, userInfo.userName, 0)+"#"+userInfo.serverId;
		}else{
			name = userInfo.userName;
		}
		boolean isKickEnable = true;
		boolean isCreater = true;
		if (ChatServiceController.isInChatRoom() && !ChatServiceController.isCreateChatRoom)
		{
			String founderUid = ChannelManager.getInstance()
					.getChatRoomFounderByKey(UserManager.getInstance().getCurrentMail().opponentUid);
			if (founderUid != null && !founderUid.equals("") && founderUid.equals(userInfo.uid))
			{
				String creater = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_CREATER);
				if (creater.equals(""))
					creater = "创建者";
				name += " (" + creater + ")";
			}

			if (founderUid != null && !founderUid.equals("") && founderUid.equals(UserManager.getInstance().getCurrentUserId()))
			{
				isCreater = true;
			}
			else
			{
				isCreater = false;
			}
		}
		if (memberUidArray != null && memberUidArray.size() > 0 && memberUidArray.contains(userInfo.uid)/* && !isCreater*/)
		{
			isKickEnable = false;
		}
		holder.userName.setText(name);
		holder.checkBox.setTag("" + groupPosition + "_" + childPosition);
		holder.checkBox.setChecked(selectStateMap.containsKey(userInfo.uid) && selectStateMap.get(userInfo.uid));
		if (!isKickEnable
				|| (!UserManager.getInstance().getCurrentUser().uid.equals("") && UserManager.getInstance().getCurrentUser().uid
						.equals(userInfo.uid))
				|| (!ChatServiceController.isCreateChatRoom && !ChatServiceController.isInChatRoom()
						&& !UserManager.getInstance().getCurrentMail().opponentUid.equals("") && UserManager.getInstance().getCurrentMail().opponentUid
							.equals(userInfo.uid)))
		{
			holder.checkBox.setEnabled(false);
			CompatibleApiUtil.getInstance().setButtonAlpha(holder.checkBox, false);
		}
		else
		{
			holder.checkBox.setEnabled(true);
			CompatibleApiUtil.getInstance().setButtonAlpha(holder.checkBox, true);
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		if (this._listDataHeader.size() > 0 && groupPosition < this._listDataHeader.size() && this._listDataChild != null)
		{
			String key = this._listDataHeader.get(groupPosition);
			if (key != null && !key.equals(""))
			{
				ArrayList<UserInfo> userArr = this._listDataChild.get(key);
				if (userArr != null)
					return userArr.size();
			}
		}
		return 0;
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		if (this._listDataHeader != null)
			return this._listDataHeader.get(groupPosition);
		return null;
	}

	@Override
	public int getGroupCount()
	{
		if (this._listDataHeader != null)
			return this._listDataHeader.size();
		return 0;
	}

	@Override
	public long getGroupId(int groupPosition)
	{
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		if (getGroup(groupPosition) == null)
			return null;
		String headerTitle = (String) getGroup(groupPosition);
		if (convertView == null)
		{
			LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(ResUtil.getId(this._context, "layout", "list_group"), null);
		}

		TextView lblListHeader = (TextView) convertView.findViewById(ResUtil.getId(this._context, "id", "lblListHeader"));
		lblListHeader.setTypeface(null, Typeface.BOLD);
		lblListHeader.setText(headerTitle);

		return convertView;
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return true;
	}

	public class UserHolder
	{
		public ImageView	headImage;
		public TextView		userName;
		public CheckBox		checkBox;

		public void findView(View convertView)
		{
			headImage = ((ImageView) convertView.findViewById(R.id.headImage));
			userName = ((TextView) convertView.findViewById(R.id.lblListItem));
			checkBox = ((CheckBox) convertView.findViewById(R.id.checkBox));
			convertView.setTag(this);
		}
	}
}
