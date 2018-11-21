package com.chat.host;

import com.chatsdk.IHost;
import com.chatsdk.controller.ServiceInterface;

public class GameHost implements IHost
{
	public boolean treatAsDummyHost = true;
	@Override
	public void sendMessage(String msg)
	{

	}

	@Override
	public void sendMailMsg(String toName, String title, final String content, final String allianceUid, final String uid,
			boolean isFirstMsg, int type, String sendLocalTime, String targetUid)
	{
	}

	@Override
	public void sendChatMessage(final String msg, int type, String sendLocalTime, int post, String media)
	{
	}

	@Override
	public void postNewHornMessage(String hornJson)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void requestMoreMail(String fromUid, String uid, int count)
	{

	}

	public String	actionAfterResume	= null;

	@Override
	public void setActionAfterResume(String action, String uid, String name, String attachmentId, boolean returnToChatAfterPopup)
	{
		actionAfterResume = action;
		ServiceInterface.flyHint("", "", "选中命令" + actionAfterResume, 0, 0, false);
	}

	@Override
	public void onResume(int chatType)
	{

	}

	@Override
	public void joinAnnounceInvitation(String allianceId)
	{
		ServiceInterface.onJoinAnnounceInvitationSuccess();
	}

	@Override
	public Object[] getChatLangArray()
	{
		return null;
	}

	@Override
	public void unShieldPlayer(String uid, String name)
	{

	}

	@Override
	public void shieldPlayer(String uid)
	{

	}

	@Override
	public void unBanPlayer(String uid)
	{

	}

	@Override
	public void banPlayerByIndex(String uid, int banTimeIndex)
	{

	}

	@Override
	public void onBackPressed()
	{

	}

	@Override
	public void onTextChanged(String msg)
	{

	}

	@Override
	public void set2dxViewHeight(int height, int usableHeightSansKeyboard)
	{

	}

	@Override
	public Object[] getChatInfoArray(int chatInfoNo, String msgType)
	{
		return null;
	}

	@Override
	public void sendHornMessage(String msg, boolean usePoint, String sendLocalTime)
	{

	}

	@Override
	public int isHornEnough()
	{
		return 500;
	}

	@Override
	public boolean isCornEnough(int price)
	{
		return true;
	}

	@Override
	public int getHornBanedTime()
	{
		return 100;
	}

	@Override
	public int getCurrentServerTime()//暂时废弃
	{
		return 0;
	}

	@Override
	public void inviteChatRoomMember(String groupId, String memberNameStr, String memberUidStr)
	{

	}

	@Override
	public void kickChatRoomMember(String groupId, String memberNameStr, String memberUidStr)
	{

	}

	@Override
	public void quitChatRoom(String groupId)
	{

	}

	@Override
	public void modifyChatRoomName(String groupId, String name)
	{

	}

	@Override
	public void sendChatRoomMsg(String groupId, String msg, String sendLocalTime)
	{

	}

	@Override
	public void getChatRoomMsgRecord(String groupId, int start, int count)
	{

	}

	@Override
	public Object[] getUserInfoArray(int index)
	{
		return null;
	}

	@Override
	public Object[] getSearchedUserInfoArray(int index){return null; }

	@Override
	public String getCustomHeadPicUrl(String uid, int headPicVer)
	{
		return null;
	}

	@Override
	public String getCustomHeadPic(String customHeadPicUrl)
	{
		return null;
	}

	@Override
	public void getMultiUserInfo(String uidsStr)
	{
	}

	@Override
	public void getAllianceMember()
	{

	}

	@Override
	public void postCurChannel(int channel)
	{
	}

	@Override
	public void selectChatRoomMember(String roomName, String memberNameStr, String memberUidStr)
	{
	}

	@Override
	public void callXCApi()
	{
	}

	@Override
	public void createChatRoom(String memberNameStr, String memberUidStr, String roomName, String contents)
	{

	}

	@Override
	public void getMsgBySeqId(int minSeqId, int maxSeqId, int channelType, String channelId)
	{

	}

	@Override
	public void searchPlayer(String key,int curPage)
	{
	}

	@Override
	public void transportMailInfo(long mailInfo, boolean isShowDetectMail)
	{

	}

	@Override
	public Object[] getMailDataArray(int mailDataIndex,int flag)
	{
		return null;
	}

	@Override
	public void deleteSingleMail(int tabType, int type, String mailUid, String fromUid)
	{

	}

	@Override
	public void deleteMutiMail(String mailUids, String types)
	{

	}

	@Override
	public void testMailCommand()
	{

	}

	@Override
	public void getUpdateMail(String time)
	{

	}

	@Override
	public void readMail(String mailUid, int type)
	{

	}

	@Override
	public void postUnreadMailNum(int unReadCount,int unSysReadCount,int allSysCount,int fightNum)
	{

	}

	@Override
	public void getNewMailFromServer(String latestMailUid, String createTime, int count)
	{

	}

	@Override
	public void getRoomMailFromServer(String comandStr, String updateTime)
	{

	}

	@Override
	public void getNewPersonMailFromServer(String latestMailUid, String createTime, int count)
	{

	}

	@Override
	public String getNameById(String xmlId)
	{
		return null;
	}

	@Override
	public String getPropById(String xmlId, String proName)
	{
		return "";
	}

	@Override
	public String getPropByIdGroup(String xmlId, String proName,String groupId)
	{
		return "";
	}

	@Override
	public String getPropByNoGroup(String xmlId, String proName,int type)
	{
		return "";
	}

	@Override
	public String getPropByIdType(String xmlId, String proName,String groutId,int type)
	{
		return "";
	}

	public  int getMailOrderById(String xmlId)
	{
		return 0;
	}


	@Override
	public String getPointByIndex(int occupyPointId)
	{
		return null;
	}

	@Override
	public String getParseNameAndContent(long mailInfo)
	{
		return null;
	}

	@Override
	public String getPicByType(int type, int value)
	{
		return null;
	}

	@Override
	public String getLang(String key)
	{
		return "lang." + key;
	}

	@Override
	public String getLang1ByKey(String lang, String key1)
	{
		return "lang." + key1;
	}

	@Override
	public String getLang2ByKey(String lang, String key1, String key2)
	{
		return "lang." + key1 + "@" + key2;
	}

	@Override
	public void translateMsgByLua(String originMsg, String targetLang)
	{

	}

	@Override
	public void readChatMail(String fromUser, boolean isModMail)
	{

	}

	@Override
	public void notifyWebSocketStatus( boolean isWebSocketStatus)
	{

	}

	@Override
	public void csPingBack(boolean isConnected, double ping)
	{

	}

	@Override
	public void recordStepByHttpWithNoWbsocket(String deviceName)
	{

	}

	@Override
	public boolean canTransalteByLua()
	{
		return false;
	}

	@Override
	public boolean isContainsForbiddenWords(String msg)
	{
        return false;
	}

	@Override
	public boolean isNumericStr(String uid)
	{
		return false;
	}

	@Override
	public void reportCustomHeadImg(String uid)
	{
	}

	@Override
	public String getLang3ByKey(String lang, String key1, String key2, String key3)
	{
		return "lang." + key1 + "@" + key2 + "@" + key3;
	}

	@Override
	public String getLang4ByKey(String lang, String key1, String key2, String key3, String key4)
	{
		return "lang." + key1 + "@" + key2 + "@" + key3 +"@" + key4;
	}

	@Override
	public String getLang5ByKey(String lang, String key1, String key2, String key3, String key4, String key5)
	{
		return "lang." + key1 + "@" + key2 + "@" + key3 +"@" + key4 +"@" + key5;
	}

	@Override
	public void rewardMutiMail(String mailUids, String types, boolean needDelete)
	{
	}

	@Override
	public void reportPlayerChatContent(String uid, String msg)
	{
	}

	@Override
	public void translateOptimize(String method, String originalLang, String userLang, String msg, String translationMsg)
	{
	}

	@Override
	public String getPointByMapTypeAndIndex(int occupyPointId, int serverType)
	{
		return null;
	}

	@Override
	public void readMutiMail(String mailUids)
	{
	}

	@Override
	public void readDialogMail(int type, boolean isModMail, String types)
	{
	}

	@Override
	public void postDetectMailInfo(String jsonStr)
	{
	}

	@Override
	public void changeNickName()
	{
	}

	@Override
	public void postDeletedDetectMailInfo(String jsonStr)
	{
	}

	@Override
	public void postChangedDetectMailInfo(String jsonStr)
	{
	}

	@Override
	public void postNotifyMailPopup()
	{
	}

	@Override
	public void showDetectMailFromAndroid(String mailUid)
	{
	}

	@Override
	public void getRedPackageStatus(String attachment)
	{
	}

	@Override
	public void sendRedPackage(String attachment) {

	}

	@Override
	public void postFriendLatestMail(String jsonStr)
	{
	}

	@Override
	public void completeInitDatabase()
	{
	}

	@Override
	public void unBanPlayerNotice(String uid)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void banPlayerNoticeByIndex(String uid, int banTimeIndex)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postChatLatestInfo(String chatJson)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] getCommonPic(String plistName,String picName)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public  boolean getNativeGetIsShowStatusBar()
	{
		return false;
	}
	
	@Override
	public void getLatestChatMessage()
	{
		
	}

	@Override
	public int getCanCreateChatRoomNum()
	{
		return 0;
	}

	@Override
	public int getFrameState()
	{
		return 2;
    }
    
//    @Override
//	public boolean checkIsOpenByKey(String key)
//	{
//		return false;
//	}

	@Override
	public boolean isNeedRemoveArenaRoom(String roomId)
	{
		return false;
	}

	@Override
	public void updateArenaChatRoomAni(boolean isShow)
	{
	}

	@Override
	public boolean getIsInTempAlliance() {
		return false;
	}

    public void nativeFbEventDone(String event, String data){}
	public void postToCppSwithOn(boolean isAnchorHost, boolean status){

	}
	public void postToCppBCState(boolean isAnchorHost, int status){
	}
	public int getServerPingValue(String server, String port, String protocol) {
		return 0;
	}

	public void queryServerPing() {
		
	}

	public void sendServerStatus(String server, String port, String protocol, int status) {

	}

	public boolean isChina() {
		return false;
	}
	public  void updateBattleBallsState(boolean isShow,int num)
	{

	}

	public boolean isActivityOpen(int post){
		return false;
	}

	public String getNPCNameById(String npcId){
		return "";
	};
	public boolean isNeedAddWarZone(){
		return false;
	};

	public String getWarZoneFilterStr(){
		return "";
	}

	@Override
	public String getFlag(){
		return "";
	}

	@Override
	public String getLanguageChatRoom(){return "";}
}
