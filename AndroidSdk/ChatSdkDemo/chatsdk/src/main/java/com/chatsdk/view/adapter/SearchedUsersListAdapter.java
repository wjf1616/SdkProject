package com.chatsdk.view.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.chatsdk.view.MemberSelectorFragment;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by with on 17/6/13.
 */

public class SearchedUsersListAdapter extends BaseAdapter {
    private Context _context;
    // header titles
    // child data in format of header title, child title
    private ArrayList<UserInfo> _searUsersData;
    public MemberSelectorFragment fragment = null;
    private LayoutInflater inflater;
    private ArrayList<String> memberUidArray;
    private HashMap<String, Boolean> selectStateMap;

    public SearchedUsersListAdapter(Context context, MemberSelectorFragment fragment, ArrayList<UserInfo> searUsersData, boolean isAllianceMember) {
        this._context = context;
        this._searUsersData = searUsersData;
        this.inflater = ((LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        this.fragment = fragment;

        memberUidArray = UserManager.getInstance().getSelectMemberUidArr();
        if (memberUidArray == null) {
            memberUidArray = new ArrayList<String>();
        }
        for (int i = 0; i < memberUidArray.size(); i++) {
            LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "memberUidArray " + i, memberUidArray.get(i));
        }
        LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_VIEW, "isAllianceMember", isAllianceMember);
        resetSelectStateMap(isAllianceMember);
    }

    @Override
    public int getCount() {
        if (_searUsersData != null)
            return _searUsersData.size();
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return _searUsersData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        UserInfo userInfo = (UserInfo) _searUsersData.get(i);
        SearchedUsersListAdapter.UserHolder holder = null;


        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.list_item, null);
            holder = new SearchedUsersListAdapter.UserHolder();
            holder.findView(convertView);
            holder.checkBox.setOnCheckedChangeListener(new SearchedUsersListAdapter.OnUserCheckedChangeListener(holder));
        } else {
            holder = (SearchedUsersListAdapter.UserHolder) convertView.getTag();
        }

        if (holder == null)
            return null;

        if (StringUtils.isNotEmpty(userInfo.uid))
            holder.headImage.setTag(userInfo.uid);
        ImageUtil.setHeadImage(_context, userInfo.headPic, holder.headImage, userInfo);
        String name = "";
        if (UserManager.getInstance().getCurrentUser() != null && userInfo.serverId > 0) {
            name = UserManager.getInstance().getCurrentUser().getNameWithServerId(userInfo.asn, userInfo.userName, 0) + "#" + userInfo.serverId;
        } else {
            name = userInfo.userName;
        }
        boolean isKickEnable = true;
        boolean isCreater = true;
        if (ChatServiceController.isInChatRoom() && !ChatServiceController.isCreateChatRoom) {
            String founderUid = ChannelManager.getInstance()
                    .getChatRoomFounderByKey(UserManager.getInstance().getCurrentMail().opponentUid);
            if (founderUid != null && !founderUid.equals("") && founderUid.equals(userInfo.uid)) {
                String creater = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_CREATER);
                if (creater.equals(""))
                    creater = "创建者";
                name += " (" + creater + ")";
            }

            if (founderUid != null && !founderUid.equals("") && founderUid.equals(UserManager.getInstance().getCurrentUserId())) {
                isCreater = true;
            } else {
                isCreater = false;
            }
        }
        if (memberUidArray != null && memberUidArray.size() > 0 && memberUidArray.contains(userInfo.uid)/* && !isCreater*/) {
            isKickEnable = false;
        }
        holder.userName.setText(name);
        holder.checkBox.setTag(i);
        holder.checkBox.setChecked(selectStateMap.containsKey(userInfo.uid) && selectStateMap.get(userInfo.uid));
        if (!isKickEnable
                || (!UserManager.getInstance().getCurrentUser().uid.equals("") && UserManager.getInstance().getCurrentUser().uid
                .equals(userInfo.uid))
                || (!ChatServiceController.isCreateChatRoom && !ChatServiceController.isInChatRoom()
                && !UserManager.getInstance().getCurrentMail().opponentUid.equals("") && UserManager.getInstance().getCurrentMail().opponentUid
                .equals(userInfo.uid))) {
            holder.checkBox.setEnabled(false);
            CompatibleApiUtil.getInstance().setButtonAlpha(holder.checkBox, false);
        } else {
            holder.checkBox.setEnabled(true);
            CompatibleApiUtil.getInstance().setButtonAlpha(holder.checkBox, true);
        }
        return convertView;
    }

    public void setSelectState(String uid, Boolean selected) {
        if (selectStateMap != null) {
            selectStateMap.put(uid, selected);
        }
    }

    public HashMap<String, Boolean> getSelectStateMap() {
        return selectStateMap;
    }

    private void resetSelectStateMap(boolean isAllianceMember) {
        if (selectStateMap == null)
            selectStateMap = new HashMap<String, Boolean>();

        if (isAllianceMember) {
            HashMap<String, UserInfo> memberInfoMap = UserManager.getInstance().getChatRoomMemberInfoMap();
            if (memberInfoMap != null && memberInfoMap.size() > 0) {
                Set<String> uidKeySet = memberInfoMap.keySet();
                for (String uid : uidKeySet) {
                    if (!uid.equals(""))
                        selectStateMap.put(uid, Boolean.FALSE);
                }
            }
            if (memberUidArray != null && memberUidArray.size() > 0) {
                for (int i = 0; i < memberUidArray.size(); i++) {
                    String uid = memberUidArray.get(i);
                    if (selectStateMap.containsKey(uid)) {
                        selectStateMap.put(uid, Boolean.TRUE);
                    }
                }
                if (!ChatServiceController.isCreateChatRoom && !ChatServiceController.isInChatRoom()
                        && !UserManager.getInstance().getCurrentMail().opponentUid.equals("")) {
                    if (selectStateMap.containsKey(UserManager.getInstance().getCurrentMail().opponentUid)) {
                        selectStateMap.put(UserManager.getInstance().getCurrentMail().opponentUid, Boolean.TRUE);
                    }
                }
            }
        } else {
            String resultStr = "";
            resultStr = LanguageManager.getLangByKey(LanguageKeys.TIP_SEARCH_RESULT);
            if (StringUtils.isEmpty(resultStr))
                resultStr = "搜索结果";
            ArrayList<String> memberArr = UserManager.getInstance().getSelectMemberUidArr();
            if (memberArr == null) {
                memberArr = new ArrayList<String>();
            }

            //
            if (_searUsersData.size() > 0) {
                for (int i = 0; i < _searUsersData.size(); i++) {
                    UserInfo user = (UserInfo) (_searUsersData.get(i));
                    if (user != null) {
                        String uid = user.uid;
                        if (!uid.equals("")) {
                            if (memberArr.contains(uid) || fragment.memberUidAdded.contains(uid)||fragment.commonMemberUidAdded.contains(uid))
                                selectStateMap.put(uid, Boolean.TRUE);
                            else
                                selectStateMap.put(uid, Boolean.FALSE);
                        }

                    }
                }
            }
//
            if (!ChatServiceController.isCreateChatRoom) {
                ArrayList<String> nonAllianceArr = UserManager.getInstance().getSelctedMemberArr(false);
                if (nonAllianceArr != null && nonAllianceArr.size() > 0) {
                    for (int i = 0; i < nonAllianceArr.size(); i++) {
                        String uid = nonAllianceArr.get(i);
                        if (!uid.equals(""))
                            selectStateMap.put(uid, Boolean.TRUE);
                    }
                }
            }

        }
    }

    class OnUserCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        public OnUserCheckedChangeListener(SearchedUsersListAdapter.UserHolder holder) {
            mHolder = holder;
        }

        private SearchedUsersListAdapter.UserHolder mHolder;


        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String posStr = mHolder.checkBox.getTag().toString();
            if (posStr.equals(""))
                return;
            int childPosition = Integer.parseInt(posStr);
            UserInfo userInfo = (UserInfo) _searUsersData.get(childPosition);
            if (userInfo == null)
                return;

            HashMap<String, UserInfo> memberInfoMap = UserManager.getInstance().getChatRoomMemberInfoMap();
            Set<String> allianceUidKeySet = null;
            if (memberInfoMap != null && memberInfoMap.size() > 0) {
                allianceUidKeySet = memberInfoMap.keySet();
            }
            if (isChecked) {
                if (!selectStateMap.get(userInfo.uid)) {
                    selectStateMap.put(userInfo.uid, Boolean.TRUE);
                    if (fragment.memberTab == 1) {
                        if (!memberUidArray.contains(userInfo.uid) && !fragment.memberUidAdded.contains(userInfo.uid)) {
                            fragment.memberUidAdded.add(userInfo.uid);
                        }
                        if (fragment.memberUidRemoved.contains(userInfo.uid)) {
                            fragment.memberUidRemoved.remove(userInfo.uid);
                        }
                    } else if (fragment.memberTab == 2) {
                        if (allianceUidKeySet != null && allianceUidKeySet.contains(userInfo.uid)) {
                            if (fragment.memberUidRemoved.contains(userInfo.uid)) {
                                fragment.memberUidRemoved.remove(userInfo.uid);
                            }
                        } else {
                            if (fragment.commonMemberUidRemoved.contains(userInfo.uid)) {
                                fragment.commonMemberUidRemoved.remove(userInfo.uid);
                            }
                        }

                        if (!memberUidArray.contains(userInfo.uid)) {
                            if (allianceUidKeySet != null && allianceUidKeySet.contains(userInfo.uid)) {
                                if (!fragment.memberUidAdded.contains(userInfo.uid)) {
                                    fragment.memberUidAdded.add(userInfo.uid);
                                }
                            } else {
                                if (!fragment.commonMemberUidAdded.contains(userInfo.uid)) {
                                    fragment.commonMemberUidAdded.add(userInfo.uid);
                                }
                            }
                        }
                    }
                    fragment.onSelectionChanged();
                }
            } else {
                if (selectStateMap.get(userInfo.uid)) {
                    selectStateMap.put(userInfo.uid, Boolean.FALSE);
                    if (fragment.memberTab == 1) {
                        if (fragment.memberUidAdded.contains(userInfo.uid)) {
                            fragment.memberUidAdded.remove(userInfo.uid);
                        }
                        if (memberUidArray.contains(userInfo.uid) && !fragment.memberUidRemoved.contains(userInfo.uid)) {
                            fragment.memberUidRemoved.add(userInfo.uid);
                        }
                    } else if (fragment.memberTab == 2) {
                        if (allianceUidKeySet != null && allianceUidKeySet.contains(userInfo.uid)) {
                            if (fragment.memberUidAdded.contains(userInfo.uid)) {
                                fragment.memberUidAdded.remove(userInfo.uid);
                            }
                        } else {
                            if (fragment.commonMemberUidAdded.contains(userInfo.uid)) {
                                fragment.commonMemberUidAdded.remove(userInfo.uid);
                            }
                        }

                        if (memberUidArray.contains(userInfo.uid)) {
                            if (allianceUidKeySet != null && allianceUidKeySet.contains(userInfo.uid)) {
                                if (!fragment.memberUidRemoved.contains(userInfo.uid)) {
                                    fragment.memberUidRemoved.add(userInfo.uid);
                                }
                            } else {
                                if (!fragment.commonMemberUidRemoved.contains(userInfo.uid)) {
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

    public class UserHolder {
        public ImageView headImage;
        public TextView userName;
        public CheckBox checkBox;

        public void findView(View convertView) {
            headImage = ((ImageView) convertView.findViewById(R.id.headImage));
            userName = ((TextView) convertView.findViewById(R.id.lblListItem));
            checkBox = ((CheckBox) convertView.findViewById(R.id.checkBox));
            convertView.setTag(this);
        }
    }
}
