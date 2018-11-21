package com.chatsdk.controller;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.chatsdk.IHost;
import com.chatsdk.R;

import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.LatestChatInfo;
import com.chatsdk.model.LatestCountryAllianceChatInfo;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.MsgItem;
import com.chatsdk.model.TimeManager;
import com.chatsdk.model.TranslateManager;
import com.chatsdk.model.UserInfo;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.net.WebSocketManager;
import com.chatsdk.util.FilterWordsManager;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.NetworkUtil;
import com.chatsdk.view.ChannelListActivity;
import com.chatsdk.view.ChannelListFragment;
import com.chatsdk.view.ChatActivity;
import com.chatsdk.view.ChatFragment;
import com.chatsdk.view.ChatRoomSettingActivity;
import com.chatsdk.view.MainListFragment;
import com.chatsdk.view.MemberSelectorFragment;
import com.chatsdk.view.MsgMailListFragment;
import com.chatsdk.view.SysMailListFragment;
import com.chatsdk.view.actionbar.MyActionBarActivity;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class ChatServiceController
{
	public static Activity					hostActivity;																	// IF或wrapper的activity，原生未打开时依然存在
	public static ExecutorService services = Executors.newCachedThreadPool();
	private static Class<?>					hostClass;
	private static MyActionBarActivity		currentActivity;
	private static Activity					currentV2Activity;
	public IHost                            host;

	public static boolean					isNativeShowing					= false;										// 仅在IF.onResume中重置为false，主要被IF使用
	public static boolean					isNativeStarting				= false;										// 主要被原生activity使用
	public static boolean					isGoHomeOrLockScreen			= false;
	// C++传入的参数
	public static boolean					isContactMod					= false;
	public static boolean					isHornItemUsed					= false;										// 是否使用喇叭道具
	public static boolean					isCreateChatRoom				= false;
	private static int						currentChatType					= -1;											// 刚进入时由C++设置，在java中可修改，退出后会再给C++

	public static int						serverId;
	/** crossFightSrcServerId = -1 表示没有跨服， >=0表示现在处于跨服状态 */
	public static int						crossFightSrcServerId;
	public static boolean					isReturningToGame				= false;										// 仅在打开原生activity时重置为false，在IF.onResume中重置false的话，会导致无法记忆二级邮件列表
	public static int						sortType						= -1;
	public static boolean					isDefaultTranslateEnable		= true;										// 默认翻译开关
	public static boolean					isFriendEnable					= false;										// 好友功能开关
	public static boolean					isShowFrameEffNativeView		= false;										// 是否显示原生战斗红屏闪动动画
	// public static boolean					isDetectInfoEnable				= false;										// 侦察战报更新开关
	public static boolean					isShowProgressBar				= false;										// 是否显示loading动画

	public static long						oldReportContentTime			= 0;
	public static long						REPORT_CONTENT_TIME_INTERVAL	= 30000;
	public static int						red_package_during_time			= 24;											// 红包到期时间
	public static int						system_red_package_Limit_time	= 4;											// 新加入群的玩家红包限制领取时间
	public static String					kingUid							= "";											// 国王的UID
	public static String					banTime							= "24|72|168|-1";									// 国王的UID
	public static boolean					isListViewFling					= false;
	public static int						serverType						= -1;
	public static boolean					needShowAllianceDialog			= false;										// 需要在联盟聊天输入框显示特定的dialog
	public static String					switch_chat_k10					= "cn_uc,cn1,cn_mihy,cn_wdj,cn_ewan,cn_anzhi";
	public static String					switch_chat_k11					= "5|6";
	public static int						currentLevel					= 1;
	public static int						sendTimeTextHeight				= 0;
	public static boolean 					isNewYearStyleMsg				= false;
    public static boolean 					isSVIPStyleMsg					= false;										// svip气泡开关
	public static boolean 					isFestivalRedPackageEnable		= false;										// 节日红包开关
	public static boolean					isChatRoomEnable				= false;										// 聊天室开关
	public static boolean					isWarZoneRoomEnable				= false;										// 战区聊天室开关
	public static String					topChatRoomUid					= "";											// 置顶的聊天室id
	public static String					curAreaRoomId					= "";											// 当前竞技场聊天室id
	public static String					warZoneRoomId					= "";											// 当前战区聊天室id
	public static String					m_roomGroupKey					= "";											// 当前战区聊天室组
	public static int						warzone_al_lv					= 0;											// 联盟等级限制
	public static int						warzone_city_lv					= 0;											// 基地城市等级限制
	public static boolean					isAddWarZoneRoom				= false;										// 是否加入战区聊天室

	public static boolean 					chat_v2_on						= false; 										//是否使用v2聊天版
	public static boolean 					chat_v2_stickers_on				= false; 										//是否使用Stickers大表情图
	public static boolean 					chat_pictures_on				= false; 										//是否使用自定义图
	public static boolean 					chat_pictures_emoticons_on		= false; 										//多媒体图片大小限制
	public static boolean 					chat_language_on				= false; 										//语言聊天室
	public static boolean 					chat_v2_personal				= false;  										//个人聊天

	private ScheduledExecutorService		service							= null;
	private Timer							audioTimer						= null;
	private TimerTask						audioTimerTask					= null;
	private int								currentAoduiSendLocalTime		= 0;
	public static boolean 					standalone_chat_room			= false;
	public static boolean 					chat_send_target				= false;
	public static boolean					battlefield_chat_room			= false;
	public static boolean					chat_room_remote_invite			= false;
	public static boolean					mail_all_delete					= false;
	public static boolean					convenient_contact				= false;
	public static boolean					new_battlemail				= false;
	public static boolean					new_resourcebattlemail				= false;

	public static boolean					scoutmail				= false;
	public static boolean					mass_boss						= false;
	public static boolean					monster_mail				    = false;
	public static boolean					chat_msg_independent			= false;
	public static boolean					career_chat						= false; //职业显示与否
	public static String	                lastSecondChannelId		= "";
	public static boolean	                rememberSecondChannelId;
	public static boolean                   isCurrentSecondList            	= false;
	public static boolean                   isInTempAlliance	            = false;
	public static boolean                   isTabRoom	            		= false;

	public static String					curLiveRoomId				   	= "";
	public static boolean					isFromBd				   		= false;
	public static boolean					isInLiveRoom				   	= false;
	public static String 					liveUid 						= "";
	public static String 					liveRoomName					= "";
	public static String 					liveTipContent					= "";
	public static boolean  					livePushStatus 					= false;
	public static boolean  					livePullStatus 					= false;

	public static boolean 					front_end_badwords				= false;//前台屏蔽badwords(汉语)开关值
	public static boolean 					korean_shielding				= false;//前台屏蔽badwords(韩语)开关值
	public static boolean 					special_symbol_check			= false;//前台屏蔽特殊字符开关值
	public static boolean 					mail_all_read					= false;//所有邮件一键已读开关
	public static boolean 					mail_button_hide				= false;//所有邮件一键已读开关
	public static long                      openMailTime = 0;
	public static boolean 					new_system_message				= false;//系统语言显示优化开关值

	public static ArrayList<String>        redPackgeMsgGotArray             = new ArrayList<String>(); //自己抢到过的红包

	private static JSONObject 				languageJsonObject 				= null; 	//多语言聊天配置
	public static boolean 					isReturnFromScreenLock 			= false; 	//聊天-锁屏
	public static boolean 					isxternaletworkebug 			= false; 	//外网调试

	public static boolean 					defence_select_switch 			= false; 	//破城杀敌
	public static boolean 					chat_tab			 			= false; 	//新版聊天切换标签夜
	public static int 						maxCharacter 					= 500;  //默认500个字符

	private static ChatServiceController	instance;

	public static ChatServiceController getInstance()
	{
		if (instance == null)
		{
			instance = new ChatServiceController();
		}
		return instance;
	}

	private ChatServiceController()
	{
		service = Executors.newSingleThreadScheduledExecutor();
	}
	public static boolean isInDragonSencen()
	{
		return serverType == 3;
	}
	public static boolean isInAncientSencen()
	{
		return serverType == 2;
	}

	public static void init(Activity a, IHost host)
	{
		hostActivity = a;
		hostClass = a.getClass();
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "hostClass", hostClass.getName());

		getInstance().host = host;
	}

	public void reset()
	{
		UserManager.getInstance().reset();
		TranslateManager.getInstance().reset();
		ChannelManager.getInstance().reset();
		MailManager.getInstance().clearData();
		ChatServiceController.redPackgeMsgGotArray.clear();
		topChatRoomUid = "";
	}

	public static void setCurrentActivity(MyActionBarActivity a)
	{
		currentActivity = a;
	}

	public static MyActionBarActivity getCurrentActivity()
	{
		return currentActivity;
	}

	//聊天v2
	public static Activity getCurrentV2Activity()
	{
		return currentV2Activity;
	}

	public static void setCurrentV2Activity(Activity context)
	{
		currentV2Activity = context;
	}


	public static boolean	isRunning	= false;

	public static boolean isAppInForeGround()
	{
		if (currentActivity != null)
		{
			return isRunning;
		}
		return false;
	}

	public static void setCurrentChannelType(int type)
	{
		currentChatType = type;
	}

	public static int getCurrentChannelType()
	{
		return currentChatType;
	}

	private static long	oldSendTime	= 0;	// 上一次发送时间

	private static boolean isSendIntervalValid()
	{
		boolean isValid = true;
		long sendTime = System.currentTimeMillis();
		if ((sendTime - oldSendTime) < ConfigManager.sendInterval) {
			//聊天v2
			Activity activity = null;
			if(ChatServiceController.chat_v2_on) {
				activity = getCurrentV2Activity();
			}
			else
			{
				activity = getChatActivity();
			}

			//发送信息频繁提醒
			if (activity != null) {
				ServiceInterface.safeMakeText(activity, LanguageManager.getLangByKey(LanguageKeys.TIP_SENDMSG_WARN), Toast.LENGTH_SHORT);
			}
			isValid = false;
		}

		return isValid;
	}

	public static boolean isChatRestrict()
	{
		boolean result = false;
		if (currentChatType == DBDefinition.CHANNEL_TYPE_COUNTRY)
		{
			UserInfo user = UserManager.getInstance().getCurrentUser();
			String uid = UserManager.getInstance().getCurrentUserId();
			if (StringUtils.isNotEmpty(uid) && uid.length() >= 3)
			{
				String uidPostfix = uid.substring(uid.length() - 3, uid.length());
				if (StringUtils.isNumeric(uidPostfix))
				{
					int serverId = Integer.parseInt(uidPostfix);
					uidPostfix = "" + serverId;
					if (user != null && StringUtils.isNotEmpty(user.userName))
					{
						if (user.userName.startsWith("Empire") && user.userName.endsWith(uidPostfix))
							return true;
						else
							return false;
					}
				}

			}
		}
		return result;
	}

	public static void sendDummyAudioMsg(long length,int sendLocalTime)
	{
		if (ChatServiceController.getCurrentChannelType() < 0 || !isSendIntervalValid() || getChatFragment() == null)
			return;
		if (isChatRestrict())
		{
			MenuController.showChatRestrictConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CHAT_RESTRICT));
			return;
		}
		getChatFragment().clearInput();
		ChatChannel channel = ChannelManager.getInstance().getAllianceChannel();
		if (channel == null)
			return;
		int post = MsgItem.MSG_TYPE_AUDIO;
		MsgItem msgItem = new MsgItem(UserManager.getInstance().getCurrentUser().uid, true, true, DBDefinition.CHANNEL_TYPE_ALLIANCE, post,
				"" + length, sendLocalTime);
		msgItem.sendState = MsgItem.SENDING;
		msgItem.createTime = sendLocalTime;
		msgItem.initUserForSendedMsg();
		if (channel.msgList != null && channel.msgList.size() > 0 && currentChatType != DBDefinition.CHANNEL_TYPE_USER)
		{
			MsgItem lastItem = channel.msgList.get(channel.msgList.size() - 1);
			if (lastItem != null)
				msgItem.sequenceId = lastItem.sequenceId + 1;
		}
		// 此时插入的数据只包括uid、msg、sendLocalTime、sendState、post、channelType
		channel.sendingMsgList.add(msgItem);
		channel.addDummyMsg(msgItem);
		channel.getTimeNeedShowMsgIndex();
		getChatFragment().afterSendMsgShowed(ChatServiceController.getCurrentChannelType());
		oldSendTime = System.currentTimeMillis();
	}
	public static void sendAudioMsgToServer(String media,String sendLocalTime)
	{
		ChatChannel channel = ChannelManager.getInstance().getAllianceChannel();
		if (channel == null)
			return;
		int sendTime = 0;
		if(StringUtils.isNumeric(sendLocalTime))
			sendTime = Integer.parseInt(sendLocalTime);
		if (channel.sendingMsgList != null && channel.sendingMsgList.size() > 0)
		{
			MsgItem sendingItem = null;
			if(sendTime>0)
			{
				for(MsgItem msgItem: channel.sendingMsgList)
				{
					if(msgItem!=null && msgItem.sendLocalTime == sendTime)
					{
						sendingItem = msgItem;
						break;
					}
				}
			}
			else
			{
				sendingItem = channel.sendingMsgList.get(channel.sendingMsgList.size() - 1);
			}
			if (sendingItem != null)
			{
				sendingItem.media = media;
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG,"sendingItem.media",sendingItem.media);
				if (!WebSocketManager.isSendFromWebSocket(channel.channelType))
				{
					sendMsg2Server(channel, sendingItem.msg, false, false, sendingItem.sendLocalTime, MsgItem.MSG_TYPE_AUDIO, media);
				}
				else
				{
					sendMsg2WSServer(channel, sendingItem.msg, false, false, sendingItem.sendLocalTime, MsgItem.MSG_TYPE_AUDIO, media);
				}
			}
		}
	}

	// 发送消息
	public static void sendMsg(final String messageText, final boolean isHornMsg, boolean usePoint, String audioUrl)
	{
		int currentChannelType = ChatServiceController.getCurrentChannelType();
		ChatChannel channel = ChannelManager.getInstance().getChannel(currentChannelType);
		if (channel == null) {
			LogUtil.trackMessage("sendMsg() channel is null: currentChatType=" + ChatServiceController.getCurrentChannelType()
					+ " fromUid=" + UserManager.getInstance().getCurrentMail().opponentUid);
			return;
		}
		sendMsg(messageText,isHornMsg,usePoint,audioUrl,channel);
	}

	public static void sendMsg(final String messageText, final boolean isHornMsg, boolean usePoint, String audioUrl, final ChatChannel channel) {
		int currentChannelType = ChatServiceController.getCurrentChannelType();
		if (currentChannelType < 0 || !isSendIntervalValid() || channel == null){
			return;
		}

		if (getChatFragment() != null){
			getChatFragment().clearInput();
		}

		Activity activity = null;
		if (ChatServiceController.chat_v2_on){
			activity = ChatServiceController.getCurrentV2Activity();
		}
		else
		{
			activity = ChatServiceController.getCurrentActivity();
		}

		int sendLocalTime = TimeManager.getInstance().getCurrentTime();
		int gapTime = getChatSendGapTime(channel,sendLocalTime);
		boolean isChatLimmit = JniController.getInstance().excuteJNIMethod("isChatLimmit", new Object[]{currentChannelType,gapTime}); //从C++获得是否聊天限制
		if(isChatLimmit){
			String tipStr = LanguageManager.getLangByKey("170759");  //170759 = 您的聊天速度过快，请稍后再试
			if (activity != null && !activity.isFinishing()){
				ServiceInterface.safeMakeText(activity,tipStr,Toast.LENGTH_LONG);
			}
			return;
		}

		//战区聊天限制
		if(channel.channelID.contains("warzone_")){
			String warZoneTipStr = JniController.getInstance().excuteJNIMethod("getWarZoneFilterStr",null);
			if(StringUtils.isNotEmpty(warZoneTipStr)){
				if (activity != null && !activity.isFinishing()){
					ServiceInterface.safeMakeText(activity,warZoneTipStr,Toast.LENGTH_LONG);
				}
				return;
			}
		}

		int post = isHornMsg ? 6 : MsgItem.MSGITEM_TYPE_MESSAGE;
		if (!ChatServiceController.chat_v2_on && StringUtils.isNotEmpty(audioUrl)) {
			post = MsgItem.MSG_TYPE_AUDIO;
		}

		final int channelType = (post == MsgItem.MSG_TYPE_AUDIO ? DBDefinition.CHANNEL_TYPE_ALLIANCE : ChatServiceController
				.getCurrentChannelType());


		// 创建消息对象，加入正在发送列表
		MsgItem msgItem = new MsgItem(UserManager.getInstance().getCurrentUser().uid, true, true, channelType, post, messageText,
				sendLocalTime);

		msgItem.sendState = MsgItem.SENDING;
		msgItem.createTime = sendLocalTime;
		if (StringUtils.isNotEmpty(audioUrl)) {
			msgItem.media = audioUrl;
		}
		msgItem.initUserForSendedMsg();

		// 此时插入的数据只包括uid、msg、sendLocalTime、sendState、post、channelType
		channel.sendingMsgList.add(msgItem);

		// 加入model，更新视图
		try
		{
			channel.addDummyMsg(msgItem);
			channel.getTimeNeedShowMsgIndex();

			//聊天v2  非v2聊天
			if (getChatFragment() != null){
				// 发送后的行为（跳到最后一行）
				getChatFragment().afterSendMsgShowed(currentChannelType);
				trackSendAction(channel.channelType, isHornMsg, WebSocketManager.isSendFromWebSocket(channel.channelType), false,
						msgItem.isAudioMessage());
			}

			String strMsg;
			if (ChatServiceController.isNeedReplaceBadWords()) {
				strMsg = FilterWordsManager.replaceSensitiveWord(messageText, 1, "*");
			}
			else {
				strMsg = messageText;
			}
				// 实际发给后台
			if (WebSocketManager.isSendFromWebSocket(channel.channelType) && !isHornMsg)
			{
				sendMsg2WSServer(channel, strMsg, isHornMsg, usePoint, sendLocalTime, msgItem.post, msgItem.media);
			}
			else
			{
				sendMsg2Server(channel, strMsg, isHornMsg, usePoint, sendLocalTime, msgItem.post, msgItem.media);
			}
			oldSendTime = System.currentTimeMillis();

			//聊天v2,断网时,先把消息刷出来,等服务器发成功了再刷新
			if(ChatServiceController.chat_v2_on && !NetworkUtil.isNetworkAvailable()) {
				msgItem.isNewMsg  = true;
				final MsgItem[] chatInfoArr = new MsgItem[1];
				chatInfoArr[0] = msgItem;

				ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (ServiceInterface.getServiceDelegate() != null) {
							ServiceInterface.getServiceDelegate().notifyMsgAdd(channel.channelID, channelType, chatInfoArr);
						}
					}
				});
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	private static void trackSendAction(int channelType, boolean isHornMsg, boolean isWS, boolean resend, boolean isAudio)
	{
		LogUtil.trackPageView("SendMessage-" + channelType + (isHornMsg ? "-horn" : "") + (isWS ? "-ws" : "") + (resend ? "-resend" : "")
				+ (isAudio ? "-audio" : ""));
	}


	private static void sendMsg2WSServer(ChatChannel channel, String messageText, boolean isHornMessage, boolean usePoint,
			int sendLocalTime, int post, String media)
	{
		Activity activity = null;
		if (ChatServiceController.chat_v2_on){
			activity = ChatServiceController.getCurrentV2Activity();
		}
		else
		{
			activity = ChatServiceController.getCurrentActivity();
		}

		boolean isForbiddenWord = FilterWordsManager.containsForbiddenWords(messageText);
		if (isForbiddenWord) {
			if(!ConfigManager.isNewShieldingEnabled){
				String tipStr = LanguageManager.getLangByKey("170769");
				if (activity != null && !activity.isFinishing()){
					ServiceInterface.safeMakeText(activity,tipStr,Toast.LENGTH_LONG);
				}
			}
			return;
		}

		if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY)
		{
			//聊天v2
			if(ChatServiceController.chat_v2_on) {
				WebSocketManager.getInstance().sendRoomMsg(messageText, sendLocalTime, channel, post, media);
			}
			else {
				WebSocketManager.getInstance().sendRoomMsg(messageText, sendLocalTime, channel);
			}
		}
		else if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_ALLIANCE)
		{
			WebSocketManager.getInstance().sendRoomMsg(messageText, sendLocalTime, channel, post, media);
		}else if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			WebSocketManager.getInstance().sendChatRoomMsg(messageText, sendLocalTime, channel, post, media);
		}
	}

	private static void sendMsg2Server(ChatChannel channel, String messageText, boolean isHornMessage, boolean usePoint, int sendLocalTime,
			int post, String media)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "channelType", ChatServiceController.getCurrentChannelType(),
				"messageText", messageText, "isHornMessage", isHornMessage, "usePoint", usePoint, "sendLocalTime", sendLocalTime, "post",
				post, "media", media);
		if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			// standalone_chat_room独立聊天服务器聊天室, chat_send_target消息发送目标：1服务器，0独立聊天
			if(ChatServiceController.getInstance().standalone_chat_room
					&& !ChatServiceController.getInstance().chat_send_target){
				WebSocketManager.getInstance().sendChatRoomMsg(messageText, sendLocalTime, channel, post, media);
			}else{
				JniController.getInstance().excuteJNIVoidMethod("sendChatRoomMsg",
						new Object[]{UserManager.getInstance().getCurrentMail().opponentUid, messageText, Integer.toString(sendLocalTime)});
			}
		}
		else if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_USER)
		{
			String toName = "";
			String allianceUid = "";
			String fromUid = ChannelManager.getInstance().getModChannelFromUid(UserManager.getInstance().getCurrentMail().opponentUid);
			if (fromUid.equals(UserManager.getInstance().getCurrentUser().uid))
			{
				toName = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE);
				allianceUid = UserManager.getInstance().getCurrentUser().allianceId;
			}
			else
			{
				toName = UserManager.getInstance().getCurrentMail().opponentName;
				UserInfo user=  UserManager.getInstance().getUser(UserManager.getInstance().getCurrentMail().opponentUid);
				if(channel.customName!=null&&!channel.customName.equals("")){
					toName = channel.customName;
				}
				if(user!=null){
					if(!user.userName.equals("")){
					    toName = user.userName;
					}
				}
			}
			String targetUid = fromUid;

			int type = ChatServiceController.isContactMod ? MsgItem.MAIL_MOD_PERSON : UserManager.getInstance().getCurrentMail().type;

			JniController.getInstance().excuteJNIVoidMethod(
					"sendMailMsg",
					new Object[] {
							toName,
							"",
							messageText,
							allianceUid,
							UserManager.getInstance().getCurrentMail().mailUid,
							Boolean.valueOf(UserManager.getInstance().getCurrentMail().isCurChannelFirstVisit),
							Integer.valueOf(type),
							Integer.toString(sendLocalTime),
							targetUid });
		}
		else if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY)
		{
			if (!isHornMessage)
			{
				JniController.getInstance().excuteJNIVoidMethod(
						"sendChatMessage",
						new Object[] {
								messageText,
								Integer.valueOf(DBDefinition.CHANNEL_TYPE_COUNTRY),
								Integer.toString(sendLocalTime),
								Integer.valueOf(post),
								media });
			}
			else
			{
				JniController.getInstance().excuteJNIVoidMethod("sendHornMessage",
						new Object[] { messageText, Boolean.valueOf(usePoint), Integer.toString(sendLocalTime) });

				if (!usePoint)
				{
					ConfigManager.isFirstUserHorn = false;
				}
				else
				{
					ConfigManager.isFirstUserCornForHorn = false;
				}
			}
		}
		else if (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_ALLIANCE)
		{
			JniController.getInstance().excuteJNIVoidMethod(
					"sendChatMessage",
					new Object[] {
							messageText,
							Integer.valueOf(DBDefinition.CHANNEL_TYPE_ALLIANCE),
							Integer.toString(sendLocalTime),
							Integer.valueOf(post),
							media });
		}
	}

	public synchronized void setSendRedPackage()
	{
		ChatServiceController.doHostAction("sendRedPackage", "","", "", true);
//		try
//		{
//			ChatServiceController.showGameActivity(ChatServiceController.getCurrentActivity(), true);
//		}
//		catch (Exception e)
//		{
//			LogUtil.printException(e);
//		}
	}
	 public synchronized void showLiveListView(){
		 ChatServiceController.doHostAction("showBroadCastListView", "","", "", true);
	 }
	public void refreshVoiceReadState()
	{
		if (getChatFragment() != null)
			getChatFragment().notifyDataSetChanged(DBDefinition.CHANNEL_TYPE_ALLIANCE, false);
	}
	// 重发消息
	public static void resendAudioMsg(MsgItem msgItem)
	{
		if (!isSendIntervalValid())
			return;
		if (isChatRestrict())
		{
			MenuController.showChatRestrictConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CHAT_RESTRICT));
			return;
		}
		msgItem.sendState = MsgItem.SENDING;
		if (getChatFragment() != null)
			getChatFragment().notifyDataSetChanged(DBDefinition.CHANNEL_TYPE_ALLIANCE, false);
		ChatChannel channel = ChannelManager.getInstance().getAllianceChannel();
		if (channel != null)
		{
			if (!WebSocketManager.isSendFromWebSocket(DBDefinition.CHANNEL_TYPE_ALLIANCE))
			{
				sendMsg2Server(channel, msgItem.msg, false, false, msgItem.sendLocalTime, MsgItem.MSG_TYPE_AUDIO, msgItem.media);
			}
			else
			{
				sendMsg2WSServer(channel, msgItem.msg, false, false, msgItem.sendLocalTime, MsgItem.MSG_TYPE_AUDIO, msgItem.media);
			}
		}
	}
	public static void resendMsg(MsgItem msgItem, boolean isHornMsg, boolean usePoint)
	{
		if (!isSendIntervalValid())
			return;

		// 显示转圈
		msgItem.sendState = MsgItem.SENDING;
		final MsgItem item = msgItem;

		if (getChatFragment() != null) {
			getChatFragment().notifyDataSetChanged(item.channelType, false);
		}

		ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
		if (channel != null)
		{
			trackSendAction(channel.channelType, isHornMsg, WebSocketManager.isSendFromWebSocket(channel.channelType), true,
					msgItem.isAudioMessage());
			if (!WebSocketManager.isSendFromWebSocket(channel.channelType))
			{
				sendMsg2Server(channel, msgItem.msg, isHornMsg, usePoint, msgItem.sendLocalTime, msgItem.post, msgItem.media);
			}
			else
			{
				sendMsg2WSServer(channel, msgItem.msg, isHornMsg, usePoint, msgItem.sendLocalTime, msgItem.post, msgItem.media);
			}
		}
	}

	/**
	 * 与上一条消息的时间判断，让当前时间不能早于它（本地时间可能比服务器时间慢）
	 * <P>
	 * 服务器返回的时间不会覆盖这个时间
	 * <P>
	 * 如果本地时间快慢都会有问题（慢了时间会与旧的一样，快了会与后来的他人消息顺序错乱）
	 * <P>
	 * 应该改成用服务器时间（进入ChatActivity时的服务器时间 + delta）
	 * <P>
	 */
	public static String getTime(int index)
	{
		String lastTime = "";
		int lastTimeUTC = 0;
		ArrayList<MsgItem> msgList = ChannelManager.getInstance().getCurMsgListByIndex(index);
		if (msgList != null && msgList.size() > 0)
		{
			MsgItem item = msgList.get(msgList.size() - 1);
			if (item != null)
			{
				lastTime = item.getSendTime();
				lastTimeUTC = item.createTime;
			}
		}

		Date lastDate = new Date((long) lastTimeUTC * 1000);
		Date curDate = new Date(TimeManager.getInstance().getCurrentTimeMS());

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
		String time = formatter.format(curDate);
		if (lastTimeUTC > 0 && lastDate != null && lastDate.after(curDate))
		{
			time = lastTime;
		}

		return time;
	}

	public static void doHostAction(String action, String uid, String name, String attachmentId, boolean returnToChatAfterPopup)
	{
		doHostAction(action, uid, name, attachmentId, returnToChatAfterPopup, false);
	}

	public static void doHostAction(String action, String uid, String name, String attachmentId, boolean returnToChatAfterPopup,
			boolean reverseAnimation)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "action", action, "returnToChat", returnToChatAfterPopup);

		JniController.getInstance().excuteJNIVoidMethod("setActionAfterResume",
				new Object[] { action, uid, name, attachmentId, Boolean.valueOf(returnToChatAfterPopup) });

		try
		{
			if(ChatServiceController.chat_v2_on) {
				Activity curActivity = ChatServiceController.getCurrentV2Activity();

//				//新版聊天,主动退出,避免切换到游戏黑一下屏幕
//				if (curActivity != null){
//					curActivity.onBackPressed();
//				}

				//邮件等功能,用的还是旧聊天启动
				if (curActivity == null){
					curActivity = ChatServiceController.getCurrentActivity();
				}
				ChatServiceController.showGameActivity(curActivity, reverseAnimation);
			}
			else
			{
				ChatServiceController.showGameActivity(ChatServiceController.getCurrentActivity(), reverseAnimation);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public static void toggleFullScreen(final boolean fullscreen, final boolean noTitle, final Activity activity)
	{
		activity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				try
				{
					// TODO 删除noTitle参数
					if (noTitle)
					{
						activity.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
					}
					else
					{
						// activity.requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
						// activity.requestWindowFeature(Window.FEATURE_ACTION_BAR);//
						// 去掉标题栏
					}
					if (fullscreen)
					{
						activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
					}
					else
					{
						activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
					}
				}
				catch (Exception e)
				{
				}
			}
		});
	}

	// 重发消息
	public void notifyCurrentDataSetChanged()
	{
		if (getChatFragment() == null)
			return;

		ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (ChatServiceController.getCurrentActivity() != null)
					{
						if (getChatFragment() != null)
						{
							getChatFragment().notifyDataSetChanged(getChatFragment().getCurrentChannelView().channelType,false);
							getChatFragment().notifyDataSetChanged();
						}
						else if (getMemberSelectorFragment() != null)
						{
							getMemberSelectorFragment().notifyDataSetChanged();
						}
						else if (getChannelListFragment() != null)
						{
							getChannelListFragment().notifyDataSetChanged();
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

	public static void showGameActivity(Activity a)
	{
		showGameActivity(a, false);
	}

	public static void showGameActivity(Activity a, boolean reverseAnimation)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW);
		isReturningToGame = true;
		ServiceInterface.clearActivityStack();
		if(a==null) return;
		Intent intent = new Intent(a, hostClass);
		if(intent!=null){
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			a.startActivity(intent);
			if (!reverseAnimation)
			{
				a.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			}
			else
			{
				a.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left_fast);
			}
		}
	}

	public static boolean isInChatRoom()
	{
		return currentChatType == DBDefinition.CHANNEL_TYPE_CHATROOM && ChatServiceController.topChatRoomUid.contains("custom");
	}

	public static boolean isInLiveRoom() {
		return ChatServiceController.isFromBd && ChatServiceController.curLiveRoomId.contains("live_");
	}
	public static boolean isInUserMail()
	{
		return (currentChatType == DBDefinition.CHANNEL_TYPE_USER);
	}

	public static boolean isInMailDialog()
	{
		return (currentChatType == DBDefinition.CHANNEL_TYPE_USER || currentChatType == DBDefinition.CHANNEL_TYPE_CHATROOM || ChatServiceController.isFromBd);
	}

	public static boolean isInWarZoneRoom()
	{
		return (currentChatType == DBDefinition.CHANNEL_TYPE_CHATROOM);
	}


	public static MemberSelectorFragment getMemberSelectorFragment()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity().fragment != null
				&& ChatServiceController.getCurrentActivity().fragment instanceof MemberSelectorFragment)
		{
			return (MemberSelectorFragment) ChatServiceController.getCurrentActivity().fragment;
		}
		return null;
	}

	public static ChatActivity getChatActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity() instanceof ChatActivity)
		{
			return (ChatActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}

	public static ChatRoomSettingActivity getChatRoomSettingActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null
				&& ChatServiceController.getCurrentActivity() instanceof ChatRoomSettingActivity)
		{
			return (ChatRoomSettingActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}

	public static ChatFragment getChatFragment()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity().fragment != null
				&& ChatServiceController.getCurrentActivity().fragment instanceof ChatFragment)
		{
			return (ChatFragment) ChatServiceController.getCurrentActivity().fragment;
		}
		return null;
	}

//	public static ImageDetailFragment getImageDetailFragment()
//	{
//		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity() instanceof ImagePagerActivity
//				&& ((ImagePagerActivity) ChatServiceController.getCurrentActivity()) != null)
//		{
//			return ((ImagePagerActivity) ChatServiceController.getCurrentActivity()).fragment;
//		}
//		return null;
//	}
//	public static AllianceShareDetailActivity getAllianceShareDetailActivity()
//	{
//		if (ChatServiceController.getCurrentActivity() != null
//				&& ChatServiceController.getCurrentActivity() instanceof AllianceShareDetailActivity)
//		{
//			return (AllianceShareDetailActivity) ChatServiceController.getCurrentActivity();
//		}
//		return null;
//	}
//	public static ChatRoomSettingActivity getChatRoomSettingActivity()
//	{
//		if (ChatServiceController.getCurrentActivity() != null
//				&& ChatServiceController.getCurrentActivity() instanceof ChatRoomSettingActivity)
//		{
//			return (ChatRoomSettingActivity) ChatServiceController.getCurrentActivity();
//		}
//		return null;
//	}
//	public static AllianceShareListActivity getAllianceShareListActivity()
//	{
//		if (ChatServiceController.getCurrentActivity() != null
//				&& ChatServiceController.getCurrentActivity() instanceof AllianceShareListActivity)
//		{
//			return (AllianceShareListActivity) ChatServiceController.getCurrentActivity();
//		}
//		return null;
//	}
//	public static AllianceShareCommentListActivity getAllianceShareCommentListActivity()
//	{
//		if (ChatServiceController.getCurrentActivity() != null
//				&& ChatServiceController.getCurrentActivity() instanceof AllianceShareCommentListActivity)
//		{
//			return (AllianceShareCommentListActivity) ChatServiceController.getCurrentActivity();
//		}
//		return null;
//	}
	public static ChannelListActivity getChannelListActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity() instanceof ChannelListActivity)
		{
			return (ChannelListActivity) ChatServiceController.getCurrentActivity();
		}
		return null;
	}

	public static ChannelListFragment getChannelListFragment()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity().fragment != null
				&& ChatServiceController.getCurrentActivity().fragment instanceof ChannelListFragment)
		{
			return (ChannelListFragment) ChatServiceController.getCurrentActivity().fragment;
		}
		return null;
	}

	public static SysMailListFragment getSysMailListFragment()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity().fragment != null
				&& ChatServiceController.getCurrentActivity().fragment instanceof SysMailListFragment)
		{
			return (SysMailListFragment) ChatServiceController.getCurrentActivity().fragment;
		}
		return null;
	}

	public static MainListFragment getMainListFragment()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity().fragment != null
				&& ChatServiceController.getCurrentActivity().fragment instanceof MainListFragment)
		{
			return (MainListFragment) ChatServiceController.getCurrentActivity().fragment;
		}
		return null;
	}

	public static MsgMailListFragment getMsgListFragment()
	{
		if (ChatServiceController.getCurrentActivity() != null && ChatServiceController.getCurrentActivity().fragment != null
				&& ChatServiceController.getCurrentActivity().fragment instanceof MsgMailListFragment)
		{
			return (MsgMailListFragment) ChatServiceController.getCurrentActivity().fragment;
		}
		return null;
	}

	public static boolean isInTheSameChannel(String channelId)
	{
		if (getChatFragment() != null && getChatFragment().getCurrentChannel() != null
				&& getChatFragment().getCurrentChannel().chatChannel != null
				&& StringUtils.isNotEmpty(getChatFragment().getCurrentChannel().chatChannel.channelID))
		{
			return getChatFragment().getCurrentChannel().chatChannel.channelID.equals(channelId);
		}
		else if(getChatRoomSettingActivity()!=null && UserManager.getInstance().getCurrentMail()!=null
				&& UserManager.getInstance().getCurrentMail().opponentUid.equals(channelId) && currentChatType == DBDefinition.CHANNEL_TYPE_CHATROOM)
			return true;
		return false;
	}

	public static boolean isInCrossFightServer()
	{
		return crossFightSrcServerId > 0;
	}

	public static boolean isParseEnable()
	{
		if (getChannelListFragment() != null || getChatFragment() != null || getMemberSelectorFragment() != null)
			return true;
		return false;
	}

	public static boolean isCafebazaar()
	{
		return ChatServiceController.hostActivity.getPackageName().equals("com.more.dayzsurvival.cafebazaar");
	}

	public static boolean isBazinama()
	{
		return ChatServiceController.hostActivity.getPackageName().equals("com.more.dayzsurvival.bazinama");
	}

	public static boolean isInnerVersion()
	{
		return ChatServiceController.hostActivity.getPackageName().equals("com.more.dayzsurvival.debug");
	}

	public static boolean isBetaVersion()
	{
		return ChatServiceController.hostActivity.getPackageName().equals("com.more.dayzsurvival.beta");
	}

	public static boolean isPersonVersion(){
		String[] items = ChatServiceController.hostActivity.getResources().getStringArray(R.array.hs_packagename);
		int len = items.length;
		if(items != null && len > 0){
			for(int i = 0; i<len;i++){
				String personPackage = "com.more.dayzsurvival." + items[i];
				if(ChatServiceController.hostActivity.getPackageName().equals(personPackage)){
					return true;
				}
			}
		}
		return false;
	}
	public static int getChatRestrictLevel()
	{
		int level = 5;
		String channelName = MailManager.getInstance().getPublishChannelName();
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channelName", channelName);
		if (StringUtils.isEmpty(channelName) || StringUtils.isNotEmpty(ChatServiceController.switch_chat_k11))
			return level;
		String[] switchArr = ChatServiceController.switch_chat_k11.split("\\|");
		if (switchArr.length != 2)
			return level;
		if (ChatServiceController.switch_chat_k10.contains(channelName))
		{
			if (StringUtils.isNumeric(switchArr[1]))
				level = Integer.parseInt(switchArr[1]);
		}
		else
		{
			if (StringUtils.isNumeric(switchArr[0]))
				level = Integer.parseInt(switchArr[0]);
		}
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "level", level);
		return level;
	}

	public static boolean isChatRestrictForLevel()
	{
		if (currentChatType == DBDefinition.CHANNEL_TYPE_COUNTRY)
			return currentLevel < getChatRestrictLevel();
		return false;
	}

	public static boolean canJumpToSecondaryList()
	{
		return rememberSecondChannelId && StringUtils.isNotEmpty(lastSecondChannelId);
	}

	public void postLatestChatMessage(MsgItem msgItem)
	{
		if (msgItem == null)
			return;

		LatestCountryAllianceChatInfo chatInfo = new LatestCountryAllianceChatInfo();
		LatestChatInfo latestChatInfo = new LatestChatInfo();
		latestChatInfo.setMsgInfo(msgItem);
		if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
			chatInfo.setLatestCountryChatInfo(latestChatInfo);
		else if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
			chatInfo.setLatestAllianceChatInfo(latestChatInfo);
		else if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
			chatInfo.setLatestTopChatRoomChatInfo(latestChatInfo);

		try
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "chatInfo", chatInfo);
			String lateChatMessage = JSON.toJSONString(chatInfo);
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "lateChatMessage", lateChatMessage);
			JniController.getInstance().excuteJNIVoidMethod("postChatLatestInfo", new Object[] { lateChatMessage });
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean isArOrPrGameLang(){
		if(ConfigManager.getInstance().gameLang.equals("ar")||ConfigManager.getInstance().gameLang.equals("pr")){
			return true;
		}
		return false;
	}

	//获得两次发送消息的间隔时间 s(秒)
	public static int getChatSendGapTime(ChatChannel channel,int sendLocalTime){
		ArrayList<MsgItem>  msgList =channel.msgList;
		int lastSendTime = 0;
		for (int i = msgList.size()-1;i>0;i--){
			MsgItem msg = msgList.get(i);
			if(msg == null){
				break;
			}else if(UserManager.getInstance().getCurrentUserId()!=null&&(msg.uid.equals(UserManager.getInstance().getCurrentUserId()))){
				lastSendTime = msg.sendLocalTime ;
				break;
			}
		}
		return (sendLocalTime - lastSendTime);
	}

	public boolean isDifferentDate(MsgItem item, List<MsgItem> items)
	{
		if (item == null || items == null)
			return true;
		int index = items.indexOf(item);
		ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
		if (channel != null && channel.getMsgIndexArrayForTimeShow() != null && channel.getMsgIndexArrayForTimeShow().size() > 0)
		{
			if (channel.getMsgIndexArrayForTimeShow().contains(Integer.valueOf(index)))
				return true;
		}
		else
		{
			if (index == 0)
			{
				return true;
			}
			else if (index > 0 && items.get(index - 1) != null)
			{
				return !item.getSendTimeYMD().equals(items.get(index - 1).getSendTimeYMD());
			}
		}

		return false;
	}
	public synchronized void setGameMusiceEnable(boolean enable)
	{
		if (!enable)
		{
			JniController.getInstance().excuteJNIVoidMethod("setGameMusicEnable", new Object[] { Boolean.valueOf(false) });
		}
		else
		{
			if (audioTimer != null)
				return;
			audioTimer = new Timer();
			audioTimerTask = new TimerTask()
			{
				@Override
				public void run()
				{
					JniController.getInstance().excuteJNIVoidMethod("setGameMusicEnable", new Object[] { Boolean.valueOf(true) });
					stopAudioTimer();
				}
			};
			audioTimer.schedule(audioTimerTask, 1000);
		}
	}
	private synchronized void stopAudioTimer()
	{
		try
		{
			if (audioTimer != null)
			{
				audioTimer.cancel(); // NullPointerException发生数量少
				audioTimer.purge(); // NullPointerException发生数量多
				audioTimer = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

//	public static boolean checkIsOpenByKey(String key)
//	{
//		return JniController.getInstance().excuteJNIMethod("checkIsOpenByKey", new Object[] { key });
//	}

	public static boolean isTopChatRoom(){
		if(topChatRoomUid==""){
			List<ChatChannel> messageChannelArr = ChannelManager.getInstance().getAllChatRoomChannel();
			Iterator<ChatChannel> it = messageChannelArr.iterator();
			while(it.hasNext()) {
				ChatChannel channel = it.next();
				// 没有置顶选第一个聊天室
				if(topChatRoomUid==""){
					topChatRoomUid = channel.channelID;
				}
				// 找到置顶聊天室
				if (channel.settings!=null && channel.settings.equals("1")){
					topChatRoomUid = channel.channelID;
				}

				// 找到竞技场聊天室
				if (channel.settings!=null && channel.settings.equals("2")){
					if(!ChatServiceController.battlefield_chat_room)
					{
						continue;
					}
					topChatRoomUid = channel.channelID;
					return true;
				}

			}

			if(topChatRoomUid=="")
				return false;
		}
		return true;
	}

	public static boolean isNeedRemoveArenaRoom(String roomId){
		boolean isRemove = true;
		try
		{
			isRemove = JniController.getInstance().excuteJNIMethod("isNeedRemoveArenaRoom", new Object[] { roomId });
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return isRemove;
	}

	// 是否显示竞技场聊天室在主ui的提示动画
	public static void updateArenaChatRoomAni(boolean isShow)
	{
		try
		{
			JniController.getInstance().excuteJNIMethod("updateArenaChatRoomAni", new Object[] { isShow });
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

    public void sendMsgTipToRoom(String msgTip) {
        UserInfo user = UserManager.getInstance().getCurrentUser();
        JSONObject msgs = new JSONObject();
        try {
			msgs.put("seqId", 0);
			msgs.put("sender", "system");
			if (curAreaRoomId != null && ChannelManager.getInstance().isArenaChatRoom(curAreaRoomId)) {
				msgs.put("roomId", curAreaRoomId);
			}else{
				return;
			}
			msgs.put("msg", msgTip);
			int time = TimeManager.getInstance().getCurrentTime();
			msgs.put("sendTime", time);
			msgs.put("serverTime", time);
			JSONObject senderInfoTmp = new JSONObject();
			senderInfoTmp.put("userName",LanguageManager.getLangByKey("170145"));
			senderInfoTmp.put("lastUpdateTime",user.lastUpdateTime);
			msgs.put("senderInfo", senderInfoTmp);
			msgs.put("group", "");
			msgs.put("extra", null);
			msgs.put("originalLang", user.lang);
            WebSocketManager.getInstance().formatMsgForChatRoom(msgs, 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public static boolean getIsInTempAlliance(){
		Boolean isTempAlliance = JniController.getInstance().excuteJNIMethod("getIsInTempAlliance",new Object[]{});
		if(isTempAlliance != null)
			return isTempAlliance.booleanValue();
		return false;
    }
    
	public static boolean isChatSendCN(){
		String flag = JniController.getInstance().excuteJNIMethod("getFlag",new Object[]{});
		boolean isCn = false;
		if (flag.equals("CN") || flag.equals("CountryFlag")) {
		 isCn = true;
		}
		if(isCn )//&& !ChatServiceController.chat_send_cn) {
		{
			return true;
		}
		return false;
	}

	public static boolean isChinaCountry(){
		String flag = JniController.getInstance().excuteJNIMethod("getFlag",new Object[]{});
		boolean isCn = false;
		if (flag.equals("CN") || flag.equals("CountryFlag")) {
			isCn = true;
		}
		return isCn;
	}


	public static boolean isNeedReplaceBadWords(){
		String langName = ConfigManager.getInstance().gameLang;
		if((!langName.equals("ko") && ChatServiceController.front_end_badwords && ChatServiceController.isChinaCountry())
		||(langName.equals("ko") && ChatServiceController.korean_shielding)){
			return true;
		}
		return false;
	}

	public static String replaceSpecialSymbolChar(String text) {

		if (ChatServiceController.special_symbol_check) {
			StringBuilder temp = new StringBuilder();
			for (int i = 0; i < text.length(); i++) {
				int _specialChar = text.charAt(i);
				//判断是否为英文和数字
				//8194 8195 8201 8204 8205 8206 8207
				if ((i < text.length()) && (_specialChar == 8204 || _specialChar == 3100 || _specialChar == 3149 || _specialChar == 3102 || _specialChar == 3134
						|| _specialChar == 8194 || _specialChar == 8195 || _specialChar == 8201 || _specialChar == 8205 || _specialChar == 8206 || _specialChar == 8207)) {
					temp.append("*");
				}else{
					temp.append((char)_specialChar);
				}
			}
			return temp.toString();
		}
		return text;
	}
	public static boolean isAnchorHost(){
		if(StringUtils.isEmpty(UserManager.getInstance().getCurrentUserId()))
			return false;
		return UserManager.getInstance().getCurrentUserId().equals(ChatServiceController.liveUid);
	}

	public static boolean isActivityOpen(MsgItem item){
		boolean isOpen = JniController.getInstance().excuteJNIMethod("isActivityOpen",new Object[]{item.post});
		return isOpen;
	}
 

	//获取游戏语言
	public static String getGameLanguage(){
		return ConfigManager.getInstance().gameLang;
	}

	//获取语言聊天室配置
	public static void initLanguageChatRoomConfig(){
		if (isLanguageChatConfigFinish()){
			return;
		}

		String jsonString = JniController.getInstance().excuteJNIMethod("getLanguageChatRoom",new Object[]{});
		LogUtil.trackMessage("getLanguageChatRoom : " + jsonString);

		try {
			languageJsonObject = new JSONObject(jsonString);
		} catch (JSONException e){
			e.printStackTrace();
		}
	}

	//正确获取到语言聊天室配置
	public static boolean isLanguageChatConfigFinish() {
		return languageJsonObject != null && languageJsonObject.length() > 0;
	}

	//获取聊天室配置
	public static JSONObject getLanguageJsonObject(){
		if (!isLanguageChatConfigFinish()){
			initLanguageChatRoomConfig();
		}
		return languageJsonObject;
	}

	//获取语言频道-频道数量 (每个语言频道分频道数量相同，只固定读取一个即可)
	public static int getChatLanguageRoomMaxCount(String languageType){
		int maxCount = 0;

		try{
			JSONObject obj = ChatServiceController.getLanguageJsonObject();
			Iterator<String> itrtr = obj.keys();
			while(itrtr.hasNext()){
				String xmlId = itrtr.next();
				JSONObject element = obj.getJSONObject(xmlId);

				String type = element.getString("type");
				if (languageType.equals(type)) {
					maxCount += 1;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return maxCount;
	}

	//获取配置-字段
	public static String getLanguageJsonObjectByKey(String xmlId,String keyName){
		if (!isLanguageChatConfigFinish()){
			initLanguageChatRoomConfig();
		}

		String res = "";
		try {
			if (languageJsonObject != null) {
				JSONObject element = languageJsonObject.getJSONObject(xmlId);
				res = element.getString(keyName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	//获取聊天室频道配置ID
	public static String getChatRoomXmlId(String languageType, String channels){
		String xmlId = "1";

		try{
			JSONObject obj = ChatServiceController.getLanguageJsonObject();
			Iterator<String> itrtr = obj.keys();
			while(itrtr.hasNext()){
				xmlId = itrtr.next();
				JSONObject element = obj.getJSONObject(xmlId);

				String type = element.getString("type");
				String cIndex  = element.getString("channels");
				if (languageType.equals(type) && channels.equals(cIndex)) {
					return xmlId;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return xmlId;
	}

	//获取语言聊天室类型
	public static ArrayList<String> getChatRoomTypes(){
		ArrayList<String> types = new ArrayList<>();

		try{
			JSONObject obj = ChatServiceController.getLanguageJsonObject();
			Iterator<String> itrtr = obj.keys();
			while(itrtr.hasNext()){
				String xmlId = itrtr.next();
				JSONObject element = obj.getJSONObject(xmlId);

				String type = element.getString("type");
				if (types.indexOf(type) == -1){
					types.add(type);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return types;
	}

	//添加战区聊天室
	public void addWarZoneRoom(){
		boolean isAddWarZoneRoom = JniController.getInstance().excuteJNIMethod("isNeedAddWarZone",null);
		//判断是否加入战区聊天室
		if(isAddWarZoneRoom && !ChatServiceController.isAddWarZoneRoom){
			ChatServiceController.isAddWarZoneRoom = true;
			WebSocketManager.getInstance().joinWarZoneRoom();
		}else if(!isAddWarZoneRoom && ChatServiceController.isAddWarZoneRoom){
			WebSocketManager.getInstance().leaveWarZoneRoom();
			ChannelManager.getInstance().deleteChannel(ChannelManager.getInstance().getWarZoneChannel());
		}
	}

	//离开战区聊天室
	public void levelWarZoneRoom(){
		boolean isAddWarZoneRoom = JniController.getInstance().excuteJNIMethod("isNeedAddWarZone",null);
		if (!isAddWarZoneRoom && ChatServiceController.isAddWarZoneRoom) {
			WebSocketManager.getInstance().leaveWarZoneRoom();
			ChannelManager.getInstance().deleteChannel(ChannelManager.getInstance().getWarZoneChannel());
		}
	}

}
