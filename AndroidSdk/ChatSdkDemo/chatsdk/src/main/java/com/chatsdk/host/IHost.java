package com.chatsdk.host;

public interface IHost
{
	public void sendMessage(String msg);

	public void sendMailMsg(String toName, String title, String content, String allianceUid, String uid, boolean isFirstMsg, int type,
			String sendLocalTime, String targetUid);

	public void requestMoreMail(String fromUid, String uid, int count);

	public void sendChatMessage(String msg, int type, String sendLocalTime, int post, String media);

	public void postNewHornMessage(String hornJson);

	public void setActionAfterResume(String action, String uid, String name, String attachmentId, boolean returnToChatAfterPopup);

	public void onResume(int chatType);

	public void joinAnnounceInvitation(String allianceId);

	/**
	 * 返回一个ChatLanguage数组
	 */
	public Object[] getChatLangArray();

	/**
	 * 返回一个聊天消息数组
	 */
	public Object[] getChatInfoArray(int chatInfoNo, String msgType);

	/**
	 * 返回邮件数据数组
	 */
	public Object[] getMailDataArray(int mailDataIndex,int flag);

	/**
	 * 解除屏蔽玩家
	 */
	public void unShieldPlayer(String uid, String name);

	/**
	 * 屏蔽玩家
	 */
	public void shieldPlayer(String uid);

	/**
	 * 解除禁言玩家
	 */
	public void unBanPlayer(String uid);

	/**
	 * 禁言玩家
	 */
	public void banPlayerByIndex(String uid, int banTimeIndex);
	
	/**
	 * 解除禁言玩家喇叭消息
	 */
	public void unBanPlayerNotice(String uid);

	/**
	 * 禁言玩家喇叭消息
	 */
	public void banPlayerNoticeByIndex(String uid, int banTimeIndex);

	/**
	 * 通知cocos2d-x当前在哪个频道
	 */
	public void postCurChannel(int channel);

	/**
	 * 点击google翻译行云打点
	 */
	public void callXCApi();

	public void onBackPressed();

	public void onTextChanged(String msg);

	public void set2dxViewHeight(int height, int usableHeightSansKeyboard);

	public void sendHornMessage(String msg, boolean usePoint, String sendLocalTime);

	/**
	 * 是否有足够的item，如果足够返回0，否则返回所需金币数
	 */
	public int isHornEnough();

	public boolean isCornEnough(int price);

	public int getHornBanedTime();

	public int getCurrentServerTime();//暂时废弃

	public Object[] getUserInfoArray(int index);

	public Object[] getSearchedUserInfoArray(int index);
	/**
	 * 创建群聊
	 */
	public void createChatRoom(String memberNameStr, String memberUidStr, String roomName, String contents);

	/**
	 * 邀请加入群聊
	 */
	public void inviteChatRoomMember(String groupId, String memberNameStr, String memberUidStr);

	/**
	 * 将玩家移除群聊
	 */
	public void kickChatRoomMember(String groupId, String memberNameStr, String memberUidStr);

	/**
	 * 退出群聊
	 */
	public void quitChatRoom(String groupId);

	/**
	 * 修改群聊名称
	 */
	public void modifyChatRoomName(String groupId, String name);

	/**
	 * 发送群聊消息
	 */
	public void sendChatRoomMsg(String groupId, String msg, String sendLocalTime);

	/**
	 * 获取群聊消息记录
	 */
	public void getChatRoomMsgRecord(String groupId, int start, int count);

	/**
	 * 自定义头像网络URL
	 */
	public String getCustomHeadPicUrl(String uid, int headPicVer);
	
	

	/**
	 * 自定义头像本地路径
	 */
	public String getCustomHeadPic(String customHeadPicUrl);

	public void getMultiUserInfo(String uidsStr);

	public void getAllianceMember();

	public void selectChatRoomMember(String roomName, String memberNameStr, String memberUidStr);

	/**
	 * 获取消息记录（聊天和聊天室）
	 */
	public void getMsgBySeqId(int minSeqId, int maxSeqId, int channelType, String channelId);

	public void searchPlayer(String key,int curPage);

	public void transportMailInfo(long mailInfo, boolean isShowDetectMail);

	public void deleteSingleMail(int tabType, int type, String mailUid, String fromUid);

	public void deleteMutiMail(String mailUids, String types);

	public void rewardMutiMail(String mailUids, String types, boolean needDelete);

	public void readMail(String mailUid, int type);

	public void readDialogMail(int type, boolean isModMail, String types);

	public void readMutiMail(String mailUids);

	public void readChatMail(String fromUser, boolean isModMail);

	public void notifyWebSocketStatus(boolean isWebSocketStatus);

	public void csPingBack(boolean isConnected, double ping);

	public void recordStepByHttpWithNoWbsocket(String deviceName);

	public void testMailCommand();

	public void getUpdateMail(String time);

	public void postUnreadMailNum(int unReadCount,int unSysReadCount,int allSysCount,int fightNum);

	public void getNewMailFromServer(String latestMailUid,String createTime,int count);

	public void getRoomMailFromServer(String comandStr, String updateTime);

	public void getNewPersonMailFromServer(String latestMailUid,String createTime,int count);

	public void reportPlayerChatContent(String uid, String msg);

	public String getNameById(String xmlId);

	public String getPropById(String xmlId, String proName);

	public String getPropByIdGroup(String xmlId, String proName,String groupId);

	public String getPropByNoGroup(String xmlId, String proName,int type);

	public String getPropByIdType(String xmlId, String proName,String groutId,int type);

	public int getMailOrderById(String xmlId);

	public String getPointByIndex(int occupyPointId);

	public String getPointByMapTypeAndIndex(int occupyPointId, int serverType);

	public String getPicByType(int type, int value);

	public String getParseNameAndContent(long mailInfo);

	public String getLang(String lang);

	public String getLang1ByKey(String lang, String key1);

	public String getLang2ByKey(String lang, String key1, String key2);

	public String getLang3ByKey(String lang, String key1, String key2, String key3);

	public String getLang4ByKey(String lang, String key1, String key2, String key3 ,String key4);

	public String getLang5ByKey(String lang, String key1, String key2, String key3 ,String key4 ,String key5);

	public void translateMsgByLua(String originMsg, String targetLang);

	public boolean canTransalteByLua();

	public boolean isContainsForbiddenWords(String msg);

	public boolean isNumericStr(String uid);

	public void reportCustomHeadImg(String uid);

	public void translateOptimize(String method, String originalLang, String userLang, String msg, String translationMsg);

	public void postDetectMailInfo(String jsonStr);

	public void postDeletedDetectMailInfo(String jsonStr);

	public void postChangedDetectMailInfo(String jsonStr);

	public void changeNickName();

	public void postNotifyMailPopup();

	public void showDetectMailFromAndroid(String mailUid);

	public void getRedPackageStatus(String attachment);

	public void sendRedPackage(String attachment);
	
	public void postChatLatestInfo(String chatJson);

	public void postFriendLatestMail(String jsonStr);

	public void completeInitDatabase();
	
	public byte[] getCommonPic(String plistName,String picName);
	
	public boolean getNativeGetIsShowStatusBar();
	
	public void getLatestChatMessage();

	public int getCanCreateChatRoomNum();

	public int getFrameState();
    
//	public boolean checkIsOpenByKey(String key);

	public boolean isNeedRemoveArenaRoom(String roomId);

	public void updateArenaChatRoomAni(boolean isShow);

	public boolean getIsInTempAlliance();

	public void nativeFbEventDone(String event, String data);

	public void postToCppSwithOn(boolean isAnchorHost, boolean status);
	public void postToCppBCState(boolean isAnchorHost, int status);

	public void updateBattleBallsState(boolean isShow ,int num);

	public boolean isActivityOpen(int post);

	public String getNPCNameById(String npcId);

	public boolean isNeedAddWarZone();

	public String getWarZoneFilterStr();

	public String getFlag();
	/**
	 * 语言聊天室-配置
	 */
	public String getLanguageChatRoom();
}
