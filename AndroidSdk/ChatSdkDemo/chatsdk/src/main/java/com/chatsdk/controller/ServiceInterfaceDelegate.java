package com.chatsdk.controller;

import android.app.Activity;

import org.json.JSONArray;
import com.chatsdk.model.MsgItem;
import com.chatsdk.model.UserInfo;

import java.util.ArrayList;

/**
 * Created by imac_liudi on 2018/3/13.
 */

public interface ServiceInterfaceDelegate {
    public void showNewActivity(Activity host, String name, int channelType, boolean rememberPosition);
    public void notifyMsgAdd(final  String channelId, final int channelType, final MsgItem[] chatInfoArr);
    public void updateDialogs();
    public void refreshSearchListData(final ArrayList<UserInfo> userArr);
    public void postRedPackageStatus(String redPackageUid, int status);
    public void refreshRedPackageNum();
    public void loadChatChannel();
    public void loadChatResources();
    public void notifyLeaveChatRoom(String language);
    public void loadChatRoomMembers();
    public void pushChatRoomMembers(JSONArray array);

}
