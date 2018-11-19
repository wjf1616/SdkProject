package com.chatsdk.host;

public class GameHost implements IHost
{
	public native void sendMessage(String msg);

	public native void sendMailMsg(String toName, String title, String content, String allianceUid, String uid, boolean isFirstMsg,
			int type, String sendLocalTime, String targetUid);

	public native void requestMoreMail(String fromUid, String uid, int count);

	public native void sendChatMessage(String msg, int type, String sendLocalTime, int post, String media);

	public native void postNewHornMessage(String hornJson);

	public native void setActionAfterResume(String action, String uid, String name, String attachmentId, boolean returnToChatAfterPopup);

	public native void onResume(int chatType);

	public native void joinAnnounceInvitation(String allianceId);

	/**
	 * 返回一个ChatLanguage数组
	 */
	public native Object[] getChatLangArray();

	/**
	 * 返回一个聊天消息数组
	 */
	public native Object[] getChatInfoArray(int chatInfoNo, String msgType);

	/**
	 * 返回邮件数据数组
	 */
	public native Object[] getMailDataArray(int mailDataIndex,int flag);

	/**
	 * 解除屏蔽玩家
	 */
	public native void unShieldPlayer(String uid, String name);

	/**
	 * 屏蔽玩家
	 */
	public native void shieldPlayer(String uid);

	/**
	 * 解除禁言玩家
	 */
	public native void unBanPlayer(String uid);

	/**
	 * 禁言玩家
	 */
	public native void banPlayerByIndex(String uid, int banTimeIndex);

	/**
	 * 解除禁言玩家喇叭消息
	 */
	public native void unBanPlayerNotice(String uid);

	/**
	 * 禁言玩家喇叭消息
	 */
	public native void banPlayerNoticeByIndex(String uid, int banTimeIndex);
	/**
	 * 通知cocos2d-x当前在哪个频道
	 */
	public native void postCurChannel(int channel);

	/**
	 * 通知cocos2d-x已经关闭邮件展示界面（x号关闭）
	 */
	public native void closeMailPopUpViewByX();

	public native void onBackPressed();

	public native void onTextChanged(String msg);

	public native void set2dxViewHeight(int height, int usableHeightSansKeyboard);

	public native void sendHornMessage(String msg, boolean usePoint, String sendLocalTime);

	public native int isHornEnough();

	public native boolean isCornEnough(int price);

	public native int getHornBanedTime();

	public native int getCurrentServerTime();//暂时废弃

	public native Object[] getUserInfoArray(int index);

	public native Object[] getSearchedUserInfoArray(int index);
	/**
	 * 创建群聊
	 */
	public native void createChatRoom(String memberNameStr, String memberUidStr, String roomName, String contents);

	/**
	 * 邀请加入群聊
	 */
	public native void inviteChatRoomMember(String groupId, String memberNameStr, String memberUidStr);

	/**
	 * 将玩家移除群聊
	 */
	public native void kickChatRoomMember(String groupId, String memberNameStr, String memberUidStr);

	/**
	 * 退出群聊
	 */
	public native void quitChatRoom(String groupId);

	/**
	 * 修改群聊名称
	 */
	public native void modifyChatRoomName(String groupId, String name);

	/**
	 * 发送群聊消息
	 */
	public native void sendChatRoomMsg(String groupId, String msg, String sendLocalTime);

	/**
	 * 获取群聊消息记录
	 */
	public native void getChatRoomMsgRecord(String groupId, int start, int count);

	public native String getCustomHeadPicUrl(String uid, int headPicVer);

	public native String getCustomHeadPic(String customHeadPicUrl);

	public native void getMultiUserInfo(String uidsStr);

	public native void getAllianceMember();

	public native void selectChatRoomMember(String roomName, String memberNameStr, String memberUidStr);

	/**
	 * 点击google翻译行云打点
	 */
	public native void callXCApi();

	public native void getMsgBySeqId(int minSeqId, int maxSeqId, int channelType, String channelId);

	public native void searchPlayer(String key,int curPage);

	/**
	 * 将mail数据传递给C++端
	 */
	public native void transportMailInfo(long mailInfo, boolean isShowDetectMail,boolean isForViewChange);

	public native void deleteSingleMail(int tabType, int type, String mailUid, String fromUid);

	public native void deleteMailsByTypes(String types);

	public native void deleteMutiMail(String mailUids, String types);

	public native void rewardMutiMail(String mailUids, String types, boolean needDelete);

	public native void readDialogMail(int type, boolean isModMail, String types);

	public native void testMailCommand();

	public native void deleteAllPersonMail();


	public native void getUpdateMail(String time);

	public native void readMail(String mailUid, int type);

	public native void readMutiMail(String mailUids);

	public native void postUnreadMailNum(int unReadCount,int unSysReadCount,int allSysCount);

	public native void getNewMailFromServer(String latestMailUid,String createTime,int count);

	public native void getRoomMailFromServer(String comandStr, String updateTime);

	public native void getNewPersonMailFromServer(String latestMailUid,String createTime,int count);

	public native String getNameById(String xmlId);

	public native String getNPCNameById(String npcId);

	public native String getAllianceBossName(String npcId);

	public native String getPropById(String xmlId, String proName);

	public native String getPropByIdGroup(String xmlId, String proName,String groupId);
	public native String getPropByIdType(String xmlId, String proName,String groutId,int type);

	public native int getMailOrderById(String xmlId);

	public native String getPointByIndex(int occupyPointId);

	public native String getPointByMapTypeAndIndex(int occupyPointId, int serverType);

	public native String getParseNameAndContent(long mailInfo);

	public native String getPicByType(int type, int value);
	
	public native String getLang(String lang);
	
	public native String getLang1ByKey(String lang, String key1);

	public native String getLang2ByKey(String lang, String key1, String key2);

	public native String getLang3ByKey(String lang, String key1, String key2, String key3);

	public native String getLang4ByKey(String lang, String key1, String key2, String key3 ,String key4);

	public native String getLang5ByKey(String lang, String key1, String key2, String key3 ,String key4 ,String key5);

	public native void translateMsgByLua(String originMsg, String targetLang);

	public native void readChatMail(String fromUser, boolean isModMail);

	public native void notifyWebSocketStatus(boolean isWebSocketStatus);

	public native void csPingBack(boolean isConnected, double ping);

	public native void recordStepByHttpWithNoWbsocket(String deviceName);

	public native boolean canTransalteByLua();

	public native boolean isContainsForbiddenWords(String msg);

	public native boolean isNumericStr(String uid);

	public native void reportCustomHeadImg(String uid);

	public native void banPlayerPic(String uid);

	public native void reportPlayerChatContent(String uid, String msg);

	public native void translateOptimize(String method, String originalLang, String userLang, String msg, String translationMsg);

	public native void postDetectMailInfo(String jsonStr);

	public native void changeNickName();

	public native void postDeletedDetectMailInfo(String jsonStr);

	public native void postChangedDetectMailInfo(String jsonStr);

	public native void postNotifyMailPopup();

	public native void showDetectMailFromAndroid(String mailUid);

	public native void getRedPackageStatus(String attachment);

	public native void sendRedPackage(String attachment);

	public native void postFriendLatestMail(String jsonStr);

	public native void completeInitDatabase();
	
	public native void postChatLatestInfo(String chatJson);
	
	public native byte[] getCommonPic(String plistName,String picName);
	
	public native boolean getNativeGetIsShowStatusBar();
	
	public native void getLatestChatMessage();
	public native void showGoBackGameDialog();
	public native int getCanCreateChatRoomNum();

	public native boolean isChatLimmit(int type,int gapTime);

	public native int  getFrameState();

//	public native boolean checkIsOpenByKey(String key);

	public native boolean isNeedRemoveArenaRoom(String roomId);

	public native void updateArenaChatRoomAni(boolean isShow);

	@Override
	public native boolean getIsInTempAlliance();

	public native void nativeFbEventDone(String event, String data);
	public native void postToCppSwithOn(boolean isAnchorHost, boolean status);
	public native void postToCppBCState(boolean isAnchorHost, int status);
	public native void postToCppRefreshRoomNumber(String roomId, int roomNumber);
	public native void postToCppRefreshLiveRoomInfo();

	public native int getServerPingValue(String server, String port, String protocol);
	public native void queryServerPing();

	public native void sendServerStatus(String server, String port, String protocol, int status);

	public native void recordChat();

	public native boolean isChina();

	public native void updatePopLayout(float rate);

	public native String getFlag();

	public native String getNewsCenterShowMsg(String newsId, String titleParams);

	public native String getScienceSharedMsg(String scienceType);

	public native String getLanguageChatRoom();
}
