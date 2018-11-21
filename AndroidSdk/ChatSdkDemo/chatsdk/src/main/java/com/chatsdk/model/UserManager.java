package com.chatsdk.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import android.util.Log;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.controller.ServiceInterface;
import com.chatsdk.model.db.DBManager;
import com.chatsdk.util.LogUtil;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class UserManager
{
	private static UserManager						instance;
	private String									currentUserId;
	private MailInfo								currentMail;
	private ArrayList<String>						banUidList;
	private ArrayList<ChatBanInfo>                  banInfoCountryList;  //禁言玩家信息列表
	private ArrayList<ChatBanInfo>                  banInfoAllianceList; //禁言联盟玩家列表
	private ArrayList<String>						banNoticeUidList;
	private ArrayList<String>						blockUidList;
	private ArrayList<String>						reportUidList;
	private ArrayList<MsgItem>						reportContentList;
	private ArrayList<MsgItem>						reportContentTranslationList;
	private ArrayList<UserInfo>						userList;
	private HashMap<String, ArrayList<UserInfo>>	allianceMemberMap;
	/** 好友列表 */
	private HashMap<String, UserInfo>				friendMemberMap;
	/** 用于联盟成员等级排序 */
	private HashMap<String, Integer>				rankMap;
	public HashMap<String, UserInfo>				allianceMemberInfoMap;
	/** 非联盟成员信息map */
	public HashMap<String, UserInfo>				nonAllianceMemberInfoMap;
	private ScheduledExecutorService				service;
	private TimerTask								timerTask;
	private long									lastAddUidTime					= 0;
	private long									lastCallSuccessTime				= 0;
	private long									lastCallTime					= 0;
	private long									CALL_TIME_OUT					= 8000;
	private static final int						GET_USER_INFO_UID_COUNT			= 20;
	/** 实际向后台发送了请求的uid列表 */
	private ArrayList<String>						fechingUids						= new ArrayList<String>();
	/** 请求的uid队列 */
	private ArrayList<String>						queueUids						= new ArrayList<String>();
	/** 获取不到信息的uid列表 */
	private ArrayList<String>						unknownUids						= new ArrayList<String>();

	public static final int							BLOCK_LIST						= 1;
	public static final int							BAN_LIST						= 2;
	public static final int							REPORT_LIST						= 3;
	public static final int							REPORT_CONTETN_LIST				= 4;
	public static final int							REPORT_TRANSLATION_LIST			= 5;
	public static final int							BAN_NOTICE_LIST					= 6;

	public static final int							NOTIFY_USERINFO_TYPE_ALLIANCE	= 0;
	public static final int							NOTIFY_USERINFO_TYPE_FRIEND		= 1;

	public boolean isInitUserInfo = false;
	private static ExecutorService executorService			= null;
	private static ExecutorService executorServiceUids      = null;
	private UserManager()
	{
		reset();
	}

	public void reset()
	{
		executorService = Executors.newFixedThreadPool(4);
		executorServiceUids = Executors.newSingleThreadExecutor();
		banInfoCountryList = new ArrayList<ChatBanInfo>();
		banInfoAllianceList = new ArrayList<ChatBanInfo>();
		banUidList = new ArrayList<String>();
		banNoticeUidList = new ArrayList<String>();
		blockUidList = new ArrayList<String>();
		reportUidList = new ArrayList<String>();
		reportContentList = new ArrayList<MsgItem>();
		reportContentTranslationList = new ArrayList<MsgItem>();
		userList = new ArrayList<UserInfo>();
		currentMail = new MailInfo();
		allianceMemberMap = new HashMap<String, ArrayList<UserInfo>>();
		rankMap = new HashMap<String, Integer>();
		allianceMemberInfoMap = new HashMap<String, UserInfo>();
		nonAllianceMemberInfoMap = new HashMap<String, UserInfo>();
		friendMemberMap = new HashMap<String, UserInfo>();
		isInitUserInfo = false;
	}

	public void runOnExecutorService(Runnable runnable)
	{
		if(runnable!=null)
			executorService.execute(runnable);
	}

	public void runOnExcrutorServiceUids(Runnable runnable)
	{
		if(runnable!=null)
			executorServiceUids.execute(runnable);
	}

	public static UserManager getInstance()
	{
		if (instance == null)
		{
			synchronized (UserManager.class)
			{
				if (instance == null)
				{
					instance = new UserManager();
				}
			}
		}
		return instance;
	}

	/**
	 * 仅在get不到的时候才调用
	 */
	public void addUser(UserInfo user)
	{
		if (!isUserExists(user.uid))
		{
			_addUser(user);

			if (!user.isDummy)
			{
				DBManager.getInstance().insertUser(user);
			}
		}
	}

	/**
	 * 实际添加，不触发数据库刷新
	 */
	private void _addUser(UserInfo user)
	{
		synchronized(this) {
			userList.add(user);
		}
	}

	public void updateUser(UserInfo user)
	{
		synchronized(this) {
			for (int i = 0; i < userList.size(); i++) {
				if (user == null
						|| user.uid == null
						|| userList.get(i) == null
						|| userList.get(i).uid == null)
					continue;
				if (user.uid.equals(userList.get(i).uid)) {
					userList.set(i, user);
				}
			}
		}
		DBManager.getInstance().updateUser(user);
	}

	public boolean isUserExists(String userID)
	{
		synchronized(this) {
			for (int i = 0; i < userList.size(); i++) {
				if (userList.get(i) == null || userList.get(i).uid == null)
					return false;
				if (userID.equals(userList.get(i).uid))
					return true;
			}
		}
		return false;
	}

	/**
	 * 从userList中删除
	 */
	public void removeUser(String userID)
	{
		if (isUserExists(userID))
		{
			synchronized(this) {
				userList.remove(getUser(userID));
			}
		}
	}

	/**
	 * 如果UserManager获取不到，就从DB获取
	 */
	public UserInfo getUser(String userID)
	{
		synchronized(this) {
			for (int i = 0; i < userList.size(); i++) {
				if (userList.get(i) != null
						&& userID.equals(userList.get(i).uid))
					return userList.get(i);
			}
		}
		UserInfo result = null;
		result = DBManager.getInstance().getUser(userID);
		if (result != null)
		{
			_addUser(result);
		}

		return result;
	}

	public void setCurrentUserId(String id)
	{
		if (!StringUtils.isEmpty(id))
			currentUserId = id;
	}

	public String getCurrentUserId()
	{
		return currentUserId;
	}

	public UserInfo getCurrentUser()
	{
		if (!StringUtils.isEmpty(currentUserId))
		{
			UserInfo user = getUser(currentUserId);
			if (user == null)
			{
				user = new UserInfo();
				user.uid = currentUserId;
				addUser(user);
			}
			return user;
		}
		else
		{
			LogUtil.trackMessage("UserManager.getCurrentUser() currentUserId is empty");
			return null;
		}
	}

	/**
	 * 初始登录时会调，此时数据库还未初始化
	 */
	public void updateCurrentUser()
	{
		DBManager.getInstance().updateUser(getCurrentUser());
	}

	public boolean isCurrentUserInAlliance()
	{
		if (getCurrentUser() != null && StringUtils.isNotEmpty(getCurrentUser().allianceId))
			return true;
		return false;
	}

	public MailInfo getCurrentMail()
	{
		return currentMail;
	}

	public void addRestrictUser(String uid, int type)
	{
		if (!isInRestrictList(uid, type))
		{
			if (type == BLOCK_LIST)
				blockUidList.add(uid);
			else if (type == BAN_LIST)
				banUidList.add(uid);
			else if (type == REPORT_LIST)
				reportUidList.add(uid);
			else if (type == BAN_NOTICE_LIST)
				banNoticeUidList.add(uid);
		}
	}

	/**
	 * 添加被禁言人信息——————终对国家与联盟频道不区分，禁言某个人国家与联盟同时禁言
	 * @author lzh
	 * @time 17/2/8 下午1:51
	 */
	public void addBanUser(ChatBanInfo banInfo, int type)
	{
		if(banInfo!=null){
			if (!isInBanList(banInfo,type)){
				if (type == 1){
					banInfoCountryList.add(banInfo);
				}else  if (type == 2){
					banInfoAllianceList.add(banInfo);
				}
			}
		}
	}

	/**
	 * 删除被禁言人信息
	 * @author lzh
	 * @time 17/2/8 下午1:52
	 */
	public void removeBanUser(ChatBanInfo banInfo, int type)
	{
		if (type == 1)
		{
			for (int i = 0; i < banInfoCountryList.size(); i++)
			{
				ChatBanInfo info = banInfoCountryList.get(i);
				if (info.uid.equals(banInfo.uid))
				{
					banInfoCountryList.remove(i);
				}
			}
		}
		else if (type == 2)
		{
			for (int i = 0; i < banInfoAllianceList.size(); i++)
			{
				ChatBanInfo info = banInfoAllianceList.get(i);
				if (info.uid.equals(banInfo.uid))
				{
					banInfoAllianceList.remove(i);
				}
			}
		}
	}

	/**
	 * 判断被禁言人信息是否已经在列表中
	 * @author lzh
	 * @time 17/2/8 下午1:53
	 */
	public boolean isInBanList(ChatBanInfo banInfo, int type)
	{
		boolean b = false;
		if(type == 1){              //禁言国家
			for (int i = 0; i < banInfoCountryList.size(); i++)
			{
				ChatBanInfo info = banInfoCountryList.get(i);
				long nowTime = TimeManager.getInstance().getCurrentTime();
				long banTime = info.banTime;

				if (banInfo.uid.equals(info.uid)){
					if(nowTime > banTime){
						b = false;
					}else{
						b = true;
					}
				}
			}
		}else if(type == 2){        //禁言联盟
			for (int i = 0; i < banInfoAllianceList.size(); i++)
			{
				ChatBanInfo info = banInfoAllianceList.get(i);
				long nowTime = TimeManager.getInstance().getCurrentTime();
				long banTime = info.banTime;
				if (banInfo.uid.equals(info.uid)){
					if(nowTime > banTime){
						b = false;
					}else{
						b = true;
					}
				}
			}
		}
		if(!b){
			banInfoCountryList.clear();
			banInfoAllianceList.clear();
		}
		return b;
	}

	/**
	 * 判断当前玩家是否在被禁言的列表中，且在列表中并返回禁言信息
	 * @author lzh
	 * @time 17/2/8 下午2:01
	 */
	public ChatBanInfo isHaveUidBan(int type)
	{
		UserInfo userInfo = getCurrentUser();
		if(type == 1){              //国家
			for (int i = 0; i < banInfoCountryList.size(); i++)
			{
				ChatBanInfo info = banInfoCountryList.get(i);
				long nowTime = TimeManager.getInstance().getCurrentTime();
				long banTime = info.banTime;

				if (info.uid.equals(userInfo.uid) && (banTime != -1 && nowTime < banTime)|| banTime == -1){
					return info;
				}
			}
		}else if(type == 2){        //联盟
			for (int i = 0; i < banInfoAllianceList.size(); i++)
			{
				ChatBanInfo info = banInfoAllianceList.get(i);
				long nowTime = TimeManager.getInstance().getCurrentTime();
				long banTime = info.banTime;

				if (info.uid.equals(userInfo.uid) && (banTime != -1 && nowTime < banTime)|| banTime == -1){
					return info;
				}
			}
		}
		return null;
	}


	public String getShieldSql()
	{
		if(blockUidList == null || blockUidList.size() <=0)
			return "";
		String result = "";
		for (int i = 0; i < blockUidList.size(); i++)
		{
			String uid = blockUidList.get(i);
			if(StringUtils.isNotEmpty(uid))
				result += " AND UserID <> " + uid;
		}
		return result;
	}

	public void addReportContent(MsgItem item, int type)
	{
		if (!isInReportContentList(item, type))
		{
			if (type == REPORT_CONTETN_LIST)
				reportContentList.add(item);
			else
				reportContentTranslationList.add(item);
		}
	}

	public void removeRestrictUser(String uid, int type)
	{
		if (type == BLOCK_LIST)
		{
			for (int i = 0; i < blockUidList.size(); i++)
			{
				String n = blockUidList.get(i);
				if (n.equals(uid))
				{
					blockUidList.remove(i);
				}
			}
		}
		else if (type == BAN_LIST)
		{
			for (int i = 0; i < banUidList.size(); i++)
			{
				String n = banUidList.get(i);
				if (n.equals(uid))
				{
					banUidList.remove(i);
				}
			}
		}
		else if (type == BAN_NOTICE_LIST)
		{
			for (int i = 0; i < banNoticeUidList.size(); i++)
			{
				String n = banNoticeUidList.get(i);
				if (n.equals(uid))
				{
					banNoticeUidList.remove(i);
				}
			}
		}
	}

	public boolean isInReportContentList(MsgItem msgItem, int type)
	{
		if (type == REPORT_CONTETN_LIST && reportContentList != null && reportContentList.contains(msgItem))
			return true;
		else if (type == REPORT_TRANSLATION_LIST && reportContentTranslationList != null && reportContentTranslationList.contains(msgItem))
			return true;
		return false;
	}

	public boolean isInRestrictList(String uid, int type)
	{
		boolean b = false;
		if (type == BLOCK_LIST)
		{
			for (int i = 0; i < blockUidList.size(); i++)
			{
				String n = blockUidList.get(i);
				if (n.equals(uid)){
					b = true;
				}
			}
		}
		else if (type == BAN_LIST)
		{
			for (int i = 0; i < banUidList.size(); i++)
			{
				String n = banUidList.get(i);
				if (n.equals(uid)){
					b = true;
				}
			}
		}
		else if (type == BAN_NOTICE_LIST)
		{
			for (int i = 0; i < banNoticeUidList.size(); i++)
			{
				String n = banNoticeUidList.get(i);
				if (n.equals(uid)){
					b = true;
				}
			}
		}
		else if (type == REPORT_LIST)
		{
			for (int i = 0; i < reportUidList.size(); i++)
			{
				String n = reportUidList.get(i);
				if (n.equals(uid)){
					b = true;
				}
			}
		}

		return b;
	}

	public static boolean isNumeric(String str){
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if( !isNum.matches() ){
			return false;
		}
		return true;
	}

	/**
	 * 检查uid指向的用户在db中是否存在且是最新的，如果不是则从后台获取用户信息
	 * <p>
	 * 如果用户不存在，会创建一个dummy user
	 * <p>
	 * 
	 * @param name
	 *            可为""，如果指定的话，创建dummy user时，设置其name
	 * @param updateTime
	 *            为0时只检查存在性(认为是新的，可能是db中以前没存)，大于0时检查新旧性
	 */
	public static void checkUser(String uid, String name, int updateTime)
	{
		UserInfo user = UserManager.getInstance().getUser(uid);
		
		boolean isOld = false;
		if (user != null)
		{  
			isOld = updateTime > 0 ? updateTime > user.lastUpdateTime : false;
			if(user.userName==null||user.userName.equals("")){
				user.userName=name==null?"":name;
//                DBManager.getInstance().updateUser(user);
			}
			
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "uid", uid, "user", user, "updateTime", updateTime,
					"user.lastUpdateTime", user.lastUpdateTime, "isOld", isOld);
		}
		
		// 以前有!user.isValid()条件，是多余的。dummy
		// user只有本函数创建，如果是dummy的，说明已经获取过了，不需要再次获取
		if (user == null || (isOld && !user.uid.equals(UserManager.getInstance().getCurrentUserId())) || user.lang == null)
		{
			if (user != null && !ChatServiceController.getInstance().isUsingDummyHost())
			{
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "uid", uid, "user", user, "updateTime", updateTime,
						"user.lastUpdateTime", user.lastUpdateTime, "isOld", isOld);
			}
			else if(!ChatServiceController.getInstance().isUsingDummyHost())
			{
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "uid", uid, "user", user, "updateTime", updateTime,
						"isOld", isOld);
			}

			if (user == null)
			{
				user = new UserInfo(uid);
				if (StringUtils.isNotEmpty(name))
					user.userName = name;
				UserManager.getInstance().addUser(user);

				if(uid.equals("system")){// gm后台发的红包uid==system
					return;
				}
			}

			ArrayList<String> uids = new ArrayList<String>();
			//对uid中包含@字符进行过滤
			if (uid.contains("@")){
				uid = uid.substring(0,uid.lastIndexOf("@"));
			}

			boolean isNumericStr = isNumeric(uid);
			if (!isNumericStr){//uid不是数字字符串，不添加到请求列表
				return;
			}
			uids.add(uid);
			UserManager.getInstance().getMultiUserInfo(uids);
		}
	}

	private static String array2Str(ArrayList<String> arr)
	{
		String result = "";
		for (int i = 0; i < arr.size(); i++)
		{
			if (i > 0)
			{
				result += ",";
			}
			result += arr.get(i);
		}
		return result;
	}

	private synchronized void getMultiUserInfo(ArrayList<String> uids)
	{
		if (ChatServiceController.getInstance().isUsingDummyHost())
		{
			return;
		}
		
		synchronized (this)
		{
			boolean hasNewUid = false;

			for (int i = 0; i < uids.size(); i++)
			{
				String uid = uids.get(i);
				if (!fechingUids.contains(uid) && !queueUids.contains(uid) && !unknownUids.contains(uid))
				{
					// LogUtil.printVariablesWithFuctionName(Log.INFO,
					// LogUtil.TAG_MSG, "uid", uid, "fechingUids",
					// array2Str(fechingUids),
					// "queueUids", array2Str(queueUids), "user",
					// UserManager.getInstance().getUser(uid));

					queueUids.add(uid);
					hasNewUid = true;
					lastAddUidTime = System.currentTimeMillis();
				}
			}

			if (hasNewUid && service == null)
			{
				startTimer();
			}
		}
	}

	private synchronized void startTimer()
	{
		if (service != null)
		{
			return;
		}

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG);

		service = Executors.newSingleThreadScheduledExecutor();
		timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					checkUidQueue();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		};

		service.scheduleWithFixedDelay(timerTask, 100, 500, TimeUnit.MILLISECONDS);
	}

	private boolean isQueueClear()
	{
		return queueUids.size() == 0 && fechingUids.size() == 0;
	}

	private synchronized void checkUidQueue()
	{
		if (isQueueClear())
		{
			return;
		}

		synchronized (this)
		{
			long now = System.currentTimeMillis();

			if ((now - lastAddUidTime) > 500 && (!isCalling() || isLastCallTimeOut()))
			{
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "now", now, "lastAddUidTime", lastAddUidTime
						, "lastCallSuccessTime", lastCallSuccessTime, "lastCallTime", lastCallTime);
				callJNI();
			}
		}
	}

	private boolean isCalling()
	{
		return (lastCallSuccessTime - System.currentTimeMillis()) > 0;
	}

	private boolean isLastCallTimeOut()
	{
		return lastCallTime > 0 && (System.currentTimeMillis() - lastCallTime) > CALL_TIME_OUT;
	}

	private synchronized void callJNI()
	{
//		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "fechingUids", array2Str(fechingUids), "queueUids", array2Str(queueUids));
		if (fechingUids.size() > 0 && isCalling() && isLastCallTimeOut())
			{
				// 1.如果正有别的命令在调用，会放弃调用，导致超时; 2.后台接口未返回，导致超时
				LogUtil.trackMessage("超时：fechingUids is not empty");
				LogUtil.printVariablesWithFuctionName(Log.WARN, LogUtil.TAG_MSG, "超时：fechingUids is not empty");
				// 加入到unknownUids，等消息返回后在清除onReceiveUserInfo。
				for (int i = 0; i < fechingUids.size(); i++)
				{
					unknownUids.add(fechingUids.get(i));
					LogUtil.printVariables(Log.WARN, LogUtil.TAG_MSG, fechingUids.get(i));
				}
				fechingUids.clear();
				return;
		}
		int count = queueUids.size() > (GET_USER_INFO_UID_COUNT - fechingUids.size()) ? (GET_USER_INFO_UID_COUNT - fechingUids.size())
				: queueUids.size();
		for (int i = 0; i < count; i++)
		{
			fechingUids.add(queueUids.remove(0));
		}
		String uidsStr = ChatChannel.getMembersString(fechingUids);
//		LogUtil.printVariables(Log.INFO, LogUtil.TAG_MSG, "fechingUidsNew", array2Str(fechingUids), "queueUidsNew", array2Str(queueUids));
		JniController.getInstance().excuteJNIVoidMethod("getMultiUserInfo", new Object[] { uidsStr });
	}
	public synchronized void onServerActualCalled()
	{
		lastCallTime = System.currentTimeMillis();
		lastCallSuccessTime = System.currentTimeMillis() * 2;
	}

	public void onReceiveUserInfo(Object[] userInfoArray) {
		if (userInfoArray == null)
			return;

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG);
		for (int i = 0; i < userInfoArray.length; i++) {
			UserInfo user = (UserInfo) userInfoArray[i];
			LogUtil.printVariables(Log.INFO, LogUtil.TAG_MSG, "uid", user.uid);
			if (friendMemberMap.containsKey(user.uid)) {
				putFriendMemberInMap(user);
			}
			if (!allianceMemberInfoMap.containsKey(user.uid))
				putChatRoomMemberInMap(user);

			user.initNullField();
			UserInfo oldUser = getUser(user.uid);

			if (fechingUids.contains(user.uid)) {
				fechingUids.remove(user.uid);
			}

			// 消息回来如果在unknownUids列表里就清除
			if (unknownUids.contains(user.uid)) {
				unknownUids.remove(user.uid);
			}

			if (oldUser == null) {
				LogUtil.trackMessage("onReceiveUserInfo(): oldUser is null (impossible): " + user.uid);
				LogUtil.printVariablesWithFuctionName(Log.WARN, LogUtil.TAG_MSG, "oldUser is null (impossible):" + user.uid);
				addUser(user);
			} else if (oldUser.isDummy || user.lastUpdateTime > oldUser.lastUpdateTime || (oldUser.lang == null && user.lang != null)) {
				updateUser(user);
			} else {
				LogUtil.trackMessage("onReceiveUserInfo(): user is not newer: " + user.uid);
				LogUtil.printVariablesWithFuctionName(Log.WARN, LogUtil.TAG_MSG, "user is not newer:" + user.uid);
				LogUtil.printVariables(Log.WARN, LogUtil.TAG_MSG,
						"compare user:\n" + LogUtil.compareObjects(new Object[]{oldUser, user}));
			}
		}
		if (fechingUids.size() > 0) {
			LogUtil.trackMessage("取不到：fechingUids is not empty");
//			LogUtil.printVariablesWithFuctionName(Log.WARN, LogUtil.TAG_MSG, "取不到：", array2Str(fechingUids));
			for (int i = 0; i < fechingUids.size(); i++) {
				unknownUids.add(fechingUids.get(i));
				LogUtil.printVariables(Log.WARN, LogUtil.TAG_MSG, fechingUids.get(i));
			}
			fechingUids.clear();
		}

		lastCallSuccessTime = System.currentTimeMillis();
		lastAddUidTime = System.currentTimeMillis();

		ChatServiceController.getInstance().notifyCurrentDataSetChanged();
	}

	public void onReceiveSearchUserInfo(Object[] userInfoArray)
	{
		if (userInfoArray == null)
			return;

		final ArrayList<UserInfo> userArr = new ArrayList<UserInfo>();
		ArrayList<String> nonAllianceMemberArr = getSelctedMemberArr(false);
		for (int i = 0; i < userInfoArray.length; i++)
		{
			UserInfo user = (UserInfo) userInfoArray[i];
//			if (nonAllianceMemberArr.contains(user.uid))
//				continue;
			userArr.add(user);
			putChatRoomMemberInMap(user);
			user.initNullField();
			UserInfo oldUser = getUser(user.uid);

			if (oldUser == null)
			{
				addUser(user);
			}
			else if (oldUser.isDummy || user.lastUpdateTime > oldUser.lastUpdateTime || (oldUser.lang == null && user.lang != null))
			{
				updateUser(user);
				ChatServiceController.getInstance().notifyCurrentDataSetChanged();
			}
		}
		ChatServiceController.hostActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (ChatServiceController.getMemberSelectorFragment() != null)
					{
						ChatServiceController.getMemberSelectorFragment().refreshSearchListData(userArr);
					}

					//聊天v2
					if(ChatServiceController.chat_v2_on) {
						ServiceInterface.getServiceDelegate().refreshSearchListData(userArr);
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});

	}

	public void putNonAllianceInMap(UserInfo user)
	{
		if (user == null)
			return;
		String uid = user.uid;
		nonAllianceMemberInfoMap.put(uid, user);
	}

	public void putChatRoomMemberInMap(UserInfo user)
	{
		if (UserManager.getInstance().getCurrentUser() == null)
			return  ;
		int rank = user.allianceRank;
		String uid = user.uid;
		String allianceId = UserManager.getInstance().getCurrentUser().allianceId;
		if (allianceId != null && allianceId.equals(user.allianceId))
		{
			allianceMemberInfoMap.put(uid, user);

			if (rank > 0)
			{
				String rankKey = getRankLang(rank);
				rankMap.put(rankKey, Integer.valueOf(rank));
				resetAllianceRank(rankKey);
				ArrayList<UserInfo> userArr = allianceMemberMap.get(rankKey);
				boolean isInRank = false;
				for (int i = 0; i < userArr.size(); i++)
				{
					UserInfo info = userArr.get(i);
					if (info.uid.equals(user.uid))
					{
						allianceMemberMap.get(rankKey).remove(info);
						allianceMemberMap.get(rankKey).add(user);
						isInRank = true;
						return;
					}
				}
				if (!isInRank)
					allianceMemberMap.get(rankKey).add(user);
			}
		}
		else
		{
			putNonAllianceInMap(user);
		}
	}

	public void putFriendMemberInMap(UserInfo user)
	{
		if (user == null)
			return;
		String uid = user.uid;
		friendMemberMap.put(uid, user);
	}

	public String getRankLang(int rank)
	{
		String rankStr = "";
		switch (rank)
		{
			case 1:
				rankStr = LanguageManager.getLangByKey(LanguageKeys.TITLE_RANK1);
				break;
			case 2:
				rankStr = LanguageManager.getLangByKey(LanguageKeys.TITLE_RANK2);
				break;
			case 3:
				rankStr = LanguageManager.getLangByKey(LanguageKeys.TITLE_RANK3);
				break;
			case 4:
				rankStr = LanguageManager.getLangByKey(LanguageKeys.TITLE_RANK4);
				break;
			case 5:
				rankStr = LanguageManager.getLangByKey(LanguageKeys.TITLE_RANK5);
				break;
		}
		return rankStr;
	}

	private void resetAllianceRank(String key)
	{
		if (allianceMemberMap.containsKey(key))
			return;
		ArrayList<UserInfo> userInfoArray = new ArrayList<UserInfo>();
		allianceMemberMap.put(key, userInfoArray);
	}

	public void clearAllianceMember()
	{
		if (allianceMemberMap != null)
			allianceMemberMap.clear();
		if (allianceMemberInfoMap != null)
			allianceMemberInfoMap.clear();
		if (rankMap != null)
			rankMap.clear();
	}

	public void clearFriendMember()
	{
		if (friendMemberMap != null)
			friendMemberMap.clear();
	}

	public void clearNonAllianceMember()
	{
		if (nonAllianceMemberInfoMap != null)
			nonAllianceMemberInfoMap.clear();
	}

	public HashMap<String, ArrayList<UserInfo>> getChatRoomMemberMap()
	{
		return allianceMemberMap;
	}

	public HashMap<String, UserInfo> getChatRoomMemberInfoMap()
	{
		return allianceMemberInfoMap;
	}

	public HashMap<String, UserInfo> getNonAllianceMemberInfoMap()
	{
		return nonAllianceMemberInfoMap;
	}

	public HashMap<String, Integer> getRankMap()
	{
		return rankMap;
	}

	public boolean isMoreThanOneMember()
	{
		boolean ret = false;
		Set<String> keySet = allianceMemberMap.keySet();
		if (keySet.size() > 1)
		{
			ret = true;
		}
		else if (keySet.size() == 1)
		{
			for (String key : keySet)
			{
				ret = allianceMemberMap.get(key).size() > 1;
			}
		}

		return ret;
	}

	public String createUidStr(ArrayList<String> uidArr)
	{
		String uidStr = "";
		for (int i = 0; i < uidArr.size(); i++)
		{
			if (!uidArr.get(i).equals(""))
			{
				if (!uidStr.equals(""))
					uidStr = uidStr + "|" + uidArr.get(i);
				else
					uidStr = uidArr.get(i);
			}
		}
		return uidStr;

	}

	public String createNameStr(ArrayList<String> uidArr)
	{
		String nameStr = "";
		for (int i = 0; i < uidArr.size(); i++)
		{
			if (!uidArr.get(i).equals(""))
			{
				String uid = uidArr.get(i);

				UserInfo user = null;
				if (allianceMemberInfoMap.containsKey(uid))
				{
					user = allianceMemberInfoMap.get(uid);
				}
				else if (nonAllianceMemberInfoMap.containsKey(uid))
				{
					user = nonAllianceMemberInfoMap.get(uid);
				}
				else if (friendMemberMap.containsKey(uid))
				{
					user = friendMemberMap.get(uid);
				}

				if (user == null)
					user = getUser(uid);

				if (user != null)
				{
					if (!nameStr.equals(""))
						nameStr = nameStr + "、" + user.userName;
					else
						nameStr = user.userName;
				}
			}
		}
		return nameStr;
	}

	public ArrayList<String> getSelectMemberUidArr()
	{
		ArrayList<String> memberUidArray = new ArrayList<String>();
		if (UserManager.getInstance().getCurrentUser()==null )
			return memberUidArray;
		if (ChatServiceController.isCreateChatRoom)
		{
			if (!UserManager.getInstance().getCurrentUser().uid.equals(""))
				memberUidArray.add(UserManager.getInstance().getCurrentUser().uid);
		}
		else
		{
			if (!ChatServiceController.isInMailDialog())
				return memberUidArray;
			if (!ChatServiceController.isInChatRoom())
			{
				if (!UserManager.getInstance().getCurrentUser().uid.equals(""))
					memberUidArray.add(UserManager.getInstance().getCurrentUser().uid);
			}
			else
			{
				memberUidArray = ChannelManager.getInstance().getChatRoomMemberArrayByKey(getCurrentMail().opponentUid);
			}
		}
		return memberUidArray;
	}

	public ArrayList<String> getSelctedMemberArr(boolean isFromAlliance)
	{
		ArrayList<String> memberUidArray = new ArrayList<String>();
		if (UserManager.getInstance().getCurrentUser() == null)
			return memberUidArray;
		boolean isInAlliance = !UserManager.getInstance().getCurrentUser().allianceId.equals("");

		if (ChatServiceController.isCreateChatRoom || (ChatServiceController.isInMailDialog() && !ChatServiceController.isInChatRoom()))
		{
			if (((isInAlliance && isFromAlliance) || (!isInAlliance && !isFromAlliance))
					&& !UserManager.getInstance().getCurrentUser().uid.equals(""))
				memberUidArray.add(UserManager.getInstance().getCurrentUser().uid);
		}
		else if (ChatServiceController.isInChatRoom())
		{
			memberUidArray = new ArrayList<String>();
			HashMap<String, UserInfo> memberInfoMap = UserManager.getInstance().getChatRoomMemberInfoMap();
			Set<String> uidKeySet = memberInfoMap.keySet();
			List<String> userArray = ChannelManager.getInstance().getChatRoomMemberArrayByKey(getCurrentMail().opponentUid);
			for (int i = 0; i < userArray.size(); i++)
			{
				String uid = userArray.get(i);
				if (!uid.equals("") && (isFromAlliance && uidKeySet.contains(uid)) || (!isFromAlliance && !uidKeySet.contains(uid)))
					memberUidArray.add(uid);
			}
		}
		return memberUidArray;
	}

	public ArrayList<String> getFriendMemberArr()
	{
		ArrayList<String> memberUidArray = new ArrayList<String>();

		if (friendMemberMap != null && friendMemberMap.size() > 0)
		{
			Set<String> uidKeySet = friendMemberMap.keySet();
			for (String uid : uidKeySet)
			{
				if (StringUtils.isNotEmpty(uid))
					memberUidArray.add(uid);
			}
		}
		return memberUidArray;
	}

	public HashMap<String, ArrayList<UserInfo>> getFriendMemberMap(String key, List<String> uidArr)
	{
		HashMap<String, ArrayList<UserInfo>> map = new HashMap<String, ArrayList<UserInfo>>();
		if (uidArr != null && uidArr.size() > 0)
		{
			ArrayList<UserInfo> userArr = new ArrayList<UserInfo>();
			for (int i = 0; i < uidArr.size(); i++)
			{
				String uid = uidArr.get(i);
				if (!uid.equals(""))
				{
					if (friendMemberMap.containsKey(uid) && friendMemberMap.get(uid) != null && !friendMemberMap.get(uid).isDummy)
						userArr.add(friendMemberMap.get(uid));
					else
					{
						LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "uid",uid);
						checkUser(uid, "", 0);
						UserInfo user = getUser(uid);
						if (user != null)
						{
							userArr.add(user);
						}
					}
				}
			}

			if (userArr.size() > 0)
				map.put(key, userArr);
		}
		return map;
	}

	public ArrayList<UserInfo> getJoinedMemberMap(String key, List<String> uidArr)
	{
		ArrayList<UserInfo> map = new ArrayList<UserInfo>();

		if (uidArr != null && uidArr.size() > 0)
		{
			ArrayList<UserInfo> userArr = new ArrayList<UserInfo>();
			HashMap<String, UserInfo> memberInfoMap = UserManager.getInstance().getNonAllianceMemberInfoMap();
			for (int i = 0; i < uidArr.size(); i++)
			{
				String uid = uidArr.get(i);
				if (!uid.equals(""))
				{
					if (memberInfoMap.containsKey(uid))
						userArr.add(memberInfoMap.get(uid));
					else
					{
						LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "uid",uid);
						checkUser(uid, "", 0);
						UserInfo user = getUser(uid);
						if (user != null)
						{
							userArr.add(user);
						}
					}
				}
			}

			if (userArr.size() > 0)
				map = userArr;
//				map.put(key, userArr);
		}
		return map;
	}
}
