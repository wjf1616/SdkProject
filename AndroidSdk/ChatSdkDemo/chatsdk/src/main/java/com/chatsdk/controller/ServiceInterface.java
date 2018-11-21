package com.chatsdk.controller;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.chatsdk.R;
import com.chatsdk.model.ChannelListItem;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatBanInfo;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.FlyMutiRewardInfo;
import com.chatsdk.model.FriendLatestMail;
import com.chatsdk.model.LanguageItem;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.LatestChatInfo;
import com.chatsdk.model.LatestCountryAllianceChatInfo;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.MsgItem;
import com.chatsdk.model.StickManager;
import com.chatsdk.model.TimeManager;
import com.chatsdk.model.TranslateManager;
import com.chatsdk.model.UserInfo;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.db.ChatTable;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.db.DBManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.model.mail.updatedata.MailUpdateData;
import com.chatsdk.net.WSServerInfo;
import com.chatsdk.net.WebSocketManager;
import com.chatsdk.net.WebSocketStatusHandler;
import com.chatsdk.net.XiaoMiToolManager;
import com.chatsdk.util.FileVideoUtils;
import com.chatsdk.util.FilterWordsManager;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.PermissionManager;
import com.chatsdk.util.TranslatedByLuaResult;
import com.chatsdk.util.toast.ToastCompat;
import com.chatsdk.view.ChannelListActivity;
import com.chatsdk.view.ChannelListFragment;
import com.chatsdk.view.ChatActivity;
import com.chatsdk.view.ChatFragment;
import com.chatsdk.view.ChatRoomNameModifyActivity;
import com.chatsdk.view.ChatRoomSettingActivity;
import com.chatsdk.view.ICocos2dxScreenLockListener;
import com.chatsdk.view.MemberSelectorActivity;
import com.chatsdk.view.WriteMailActivity;
import com.chatsdk.view.actionbar.MyActionBarActivity;
import com.chatsdk.view.autoscroll.ScrollTextManager;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;


public class ServiceInterface
{
	public static String	allianceIdJoining;
	private static boolean	oldHornMsgPushed	= false;
	private static ServiceInterfaceDelegate delegate = null;

	public static void setServiceDelegate(ServiceInterfaceDelegate del)
	{
		delegate = del;
	}
	public static ServiceInterfaceDelegate getServiceDelegate(){
		return delegate;
	}

	public static void onJoinAnnounceInvitationSuccess()
	{
		UserManager.getInstance().getCurrentUser().allianceId = allianceIdJoining;
		ChatServiceController.hostActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (ChatServiceController.getChatFragment() != null)
					{
						ChatServiceController.getChatFragment().onJoinAnnounceInvitationSuccess();
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public static void setGameLanguage(String gameLanguage)
	{
		ConfigManager.getInstance().gameLang = gameLanguage;

		//聊天v2 - 加载多语言聊天室配置
		if (ChatServiceController.chat_v2_on && ChatServiceController.chat_language_on){
			ChatServiceController.initLanguageChatRoomConfig();
		}
	}

	public static void setTranslateURL(String url)
	{
		ConfigManager.getInstance().translateURL = url;
	}

	public static void toggleFullScreen(final boolean enabled)
	{
		ChatServiceController.toggleFullScreen(enabled, true, ChatServiceController.hostActivity);
	}

	public static void setMailInfo(String mailFromUid, String mailUid, String mailName, int mailType)
	{
		UserManager.getInstance().getCurrentMail().opponentUid = mailFromUid;
		UserManager.getInstance().getCurrentMail().mailUid = mailUid;
		UserManager.getInstance().getCurrentMail().opponentName = mailName;
		UserManager.getInstance().getCurrentMail().type = mailType;
		if (mailType == MailManager.MAIL_MOD_PERSONAL || mailType == MailManager.MAIL_MOD_SEND)
			ChatServiceController.isContactMod = true;
	}

	public static void setContactModState()
	{
		ChatServiceController.isContactMod = true;
	}

	public static void resetPlayerFirstJoinAlliance()
	{		// 偶会报引用的对象为空,可能是由于doReload导致的,这里加个判断
		if(UserManager.getInstance().getCurrentUser() != null)
//			UserManager.getInstance().getCurrentUser().isFirstJoinAlliance=false;
		ConfigManager.getInstance().isFirstJoinAlliance=false;
		ServiceInterface.resetPlayerIsInAlliance();

	}

	public static void postNoMoreMessage(int channelType)
	{
		if (ChatServiceController.getChatFragment() != null)
		{
			ChatServiceController.getChatFragment().resetMoreDataStart(channelType);
		}
	}

	public static void deleteChatRoom(String groupId)
	{
		ChannelManager.getInstance().deleteChatroomChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, groupId));
	}

	public static void deleteMail(String id, int channelType, int type)
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_USER/* || channelType == DBDefinition.CHANNEL_TYPE_CHATROOM*/)
		{
			ChatChannel channel = ChannelManager.getInstance().getChannel(channelType, id);
			if (channel == null)
				return;
			ChannelManager.getInstance().deleteChannel(channel);
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if (type == MailManager.MAIL_RESOURCE || type == MailManager.MAIL_RESOURCE_HELP
					|| type == MailManager.MAIL_ATTACKMONSTER || type == MailManager.MAIL_MISSILE || type == MailManager.MAIL_GIFT_BUY_EXCHANGE|| id.equals("knight") )
			{
				String channelId = "";
				if (type == MailManager.MAIL_RESOURCE)
					channelId = MailManager.CHANNELID_RESOURCE;
				else if (type == MailManager.MAIL_RESOURCE_HELP)
					channelId = MailManager.CHANNELID_RESOURCE_HELP;
				else if (type == MailManager.MAIL_ATTACKMONSTER)
					channelId = MailManager.CHANNELID_MONSTER;
				else if (type == MailManager.MAIL_MISSILE)
					channelId = MailManager.CHANNELID_MISSILE;
				else if (type == MailManager.MAIL_GIFT_BUY_EXCHANGE)
					channelId = MailManager.CHANNELID_GIFT;
				else if (id.equals("knight"))
					channelId = MailManager.CHANNELID_KNIGHT;
				ChatChannel channel = ChannelManager.getInstance().getChannel(channelType, channelId);
				if (channel != null)
				{
					channel.clearAllSysMail();
				}
			}
			else
			{
				MailData mail = DBManager.getInstance().getSysMailByID(id);
				if (mail != null)
				{
					ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mail.getChannelId());
					if (channel == null)
						return;
					ChannelManager.getInstance().deleteSysMailFromChannel(channel, id, false);
				}
			}

			if (ChatServiceController.hostActivity == null)
				return;
			ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						if (ChatServiceController.getChannelListFragment() != null)
						{
							ChatServiceController.getChannelListFragment().notifyDataSetChanged();
						}
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}
	}

	public static void setChannelMemberArray(int channelType, String fromUid, String uidStr, String roomName)
	{
		ChannelManager.getInstance().setChannelMemberArray(fromUid, uidStr, roomName);

		if (ChatServiceController.getChatRoomSettingActivity() != null) {
			ChatServiceController.getChatRoomSettingActivity().refreshData();
//			ChatServiceController.getChatRoomSettingActivity().refreshTitle();
		}

		if (ChatServiceController.hostActivity == null || channelType != DBDefinition.CHANNEL_TYPE_CHATROOM)
			return;
		ChatServiceController.hostActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (ChatServiceController.getChatFragment() != null)
					{
						if (!ChatServiceController.getChatFragment().isSelectMemberBtnEnable())
						{
							ChatServiceController.getChatFragment().refreshMemberSelectBtn();
							ChatServiceController.getChatFragment().setSelectMemberBtnState();
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

	public static void updateChannelMemberArray(int channelType, String fromUid, String uidStr, int op)
	{
		// 退回到列表 op=2 增加 3退出 4踢人
		boolean isRemove = false;
		if(uidStr.indexOf(UserManager.getInstance().getCurrentUserId())!=-1 && op>2 || uidStr.equals("")){
			isRemove = true;
			if(op==4 && fromUid.equals(UserManager.getInstance().getCurrentMail().opponentUid) || op==3){
				popTopActivity();
			}
		}

		ChannelManager.getInstance().updateChannelMemberArray(fromUid, uidStr, op);

		if (ChatServiceController.getChatRoomSettingActivity() != null) {
			ChatServiceController.getChatRoomSettingActivity().refreshData();
			ChatServiceController.getChatRoomSettingActivity().refreshTitle();
		}

		if (ChatServiceController.hostActivity == null)
			return;

		final String roomid = fromUid;
		final boolean delChannel = isRemove;
		ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {

					ChatChannel channel = ChannelManager.getInstance().getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, roomid));
					if (channel == null) {
						return;
					}

					//聊天v2
					if(ChatServiceController.chat_v2_on) {
						if (delChannel) {
							ChannelManager.getInstance().deleteChannel(channel);
							if (ServiceInterface.getServiceDelegate() != null) {
								ServiceInterface.getServiceDelegate().updateDialogs();
							}
						}

					}
					else
					{
						if (activityStack.size() > 0) {
							MyActionBarActivity chatActivity = activityStack.get(0);
							if (chatActivity != null && chatActivity.fragment != null) {
								ChatFragment chatfragment = (ChatFragment) chatActivity.fragment;

								if (!chatfragment.isSelectMemberBtnEnable()) {
									chatfragment.refreshMemberSelectBtn();
									chatfragment.setSelectMemberBtnState();
								}

								if (delChannel) {
									chatfragment.actualDeleteSingleChannel(channel);
								}
								chatfragment.reload();
							}
						} else {
							if (delChannel) {
								ChannelManager.getInstance().deleteChannel(channel);
							}
						}
					}

				} catch (Exception e) {
					LogUtil.printException(e);
				}
			}
		});
	}

	public static void setChatRoomFounder(String groupId, String founderUid)
	{
		ChannelManager.getInstance().setChatRoomFounder(groupId, founderUid);
	}

	public static void sendRoomTipMsg(String msgTip)
	{
		ChatServiceController.getInstance().sendMsgTipToRoom(msgTip);
	}
	public static void addPatternForbiddenWords(String pattern)
	{
		FilterWordsManager.patternForbiddenWords.add(pattern);
	}

	public static void addPatternForSafeWords(String pattern)
	{
		FilterWordsManager.patternForSafeWords.add(pattern);
	}
	public static void postBadWordsStringToJava(String pattern)
	{
		FilterWordsManager.initBadWords(pattern);
	}

	public static void clearPatternForbiddenWords()
	{
		FilterWordsManager.patternForbiddenWords.clear();
	}

	public static void clearPatternForSafeWords()
	{
		FilterWordsManager.patternForSafeWords.clear();
	}

	public static boolean containsForSafeWords(String msg)
	{
		boolean b = false;
		b = FilterWordsManager.containsForSafeWords(msg);
		return b;
	}

	public static void setChatHorn(boolean enableChatHorn)
	{
		ConfigManager.enableChatHorn = enableChatHorn;
	}

	public static void setRedPackage(boolean enableRedPackage)
	{
		ConfigManager.isRedPackageEnabled = enableRedPackage;
	}

	public static void setRedPackageShake(boolean shakeEnabled)
	{
		ConfigManager.isRedPackageShakeEnabled = shakeEnabled;
	}

	public static void setNewLanguageShielding(boolean newShieldingEnabled)
	{
		ConfigManager.isNewShieldingEnabled = newShieldingEnabled;
	}
	private static void save2DB(MsgItem[] infoArr, final int channelType, final String channelId, String customName)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "channelType", channelType, "channelId", channelId, "size",
				infoArr.length);

		ChatChannel channel = ChannelManager.getInstance().getChannel(channelType, channelId);
		if (infoArr.length == 0 || channel == null)
			return;

		if (channelType == DBDefinition.CHANNEL_TYPE_USER || channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
			channel.customName = customName;

		DBManager.getInstance().insertMessages(infoArr, channel.getChatTable());
	}

	private static void updateDBMsg(MsgItem msg, final int channelType, final String fromId)
	{
		ChatChannel channel = ChannelManager.getInstance().getChannel(channelType, fromId);
		if (channel == null)
			return;

		DBManager.getInstance().updateMyMessageStatus(msg, channel.getChatTable());
	}

	/**
	 * 获取多条历史消息时，此函数会被多次调用（具体次数由MailCell、MailController决定），多次刷新界面（游戏中看不出来）
	 * 
	 * @param channelId
	 *            如果是邮件则总是指对方的uid，如果是聊天室为uid，如果是聊天fromUid为"0"
	 * @param customName
	 *            如果是邮件则总是指对方的name，如果是聊天室为自定义名称，如果是聊天为""
	 */
	public static void notifyMessageIndex(final int chatInfoIndex,final String channelId,final String customName,final boolean isModMsg)
{
	if (chatInfoIndex < 0 || StringUtils.isEmpty(channelId))
		return;
	Runnable run = new Runnable()
	{
		@Override
		public void run()
		{
			handleMailMsgIndex(chatInfoIndex, channelId,customName,isModMsg);
		}
	};
	MailManager.getInstance().runOnExecutorChatMailService(run);
}

	private static synchronized void handleMailMsgIndex( int chatInfoIndex, String channelId, String customName, boolean isModMsg)
	{
		if (chatInfoIndex < 0)
			return;

		final Object[] chatInfoArr = ChatServiceController.getInstance().host.getChatInfoArray(chatInfoIndex, channelId);

		if (isModMsg && !channelId.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
			channelId += DBDefinition.CHANNEL_ID_POSTFIX_MOD;
		if (chatInfoArr == null || chatInfoArr.length <= 0)
			return;

		MsgItem[] _itemArray = new MsgItem[chatInfoArr.length];
		for (int i = 0; i < chatInfoArr.length; i++)
		{
			Object obj = chatInfoArr[i];
			if (obj != null)
			{
				_itemArray[i] = (MsgItem) obj;
			}
		}

		if (WebSocketManager.isRecieveFromWebSocket(_itemArray[0].channelType))
			return;

		handleMessage(_itemArray, channelId, customName, true, true);

		if(MailManager.hasMoreNewPersonMailToGet)
		{
			if(StringUtils.isNotEmpty(MailManager.latestPersonMailUidFromGetNew) && !MailManager.latestPersonMailUidFromGetNew.equals("0"))
			{
				String lastMailInfo = ChannelManager.getInstance().getLatestPersonMailInfo();
				if(StringUtils.isNotEmpty(lastMailInfo))
				{
					String[] mailInfos = lastMailInfo.split("\\|");
					long time = Long.parseLong(mailInfos[1])*1000;
					JniController.getInstance().excuteJNIVoidMethod("getNewPersonMailFromServer", new Object[]{MailManager.latestPersonMailUidFromGetNew,String.valueOf(time),10});
				}
			}
		}
	}

	public static synchronized MailData parseMailData(MailData mailData, boolean isFromDB)
	{
		MailData mail = null;
		try
		{
			mail = MailManager.getInstance().parseMailDataContent(mailData);
			boolean needUpdateDB = false;
			if (isFromDB)
			{
				boolean needParsedBefore = mailData.needParseContent();
				boolean needParsedNow = mail.needParseContent();
				needUpdateDB = needParsedBefore && !needParsedNow;
			}
			return handleMailData(DBDefinition.CHANNEL_TYPE_OFFICIAL, mail, isFromDB, needUpdateDB);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		return null;
	}

	private static ArrayList<Integer>	mailDataIndexArray	= new ArrayList<Integer>();

	public static void notifyMailDataIndex(final int mailDataIndex, boolean isGetNew, final boolean isFlag)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "mailDataIndex", mailDataIndex, "isGetNew", isGetNew, "isFlag", isFlag);
		if (isGetNew)
		{
			//mailDataIndexArray.add(Integer.valueOf(mailDataIndex));
			Runnable run = new Runnable()
			{
				@Override
				public void run()
				{
					handleMailDataIndexForGetNew(mailDataIndex,1);
				}
			};
			MailManager.getInstance().runOnExecutorService(run);
		}
		else
		{
			Runnable run = new Runnable()
			{
				@Override
				public void run()
				{
					handleMailDataIndex(mailDataIndex, false,isFlag);
				}
			};
			MailManager.getInstance().runOnExecutorService(run);
		}
	}

	public static boolean	isHandlingGetNewMailMsg	= false;

	public static void handleGetNewMailMsg(final String channelInfo)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "handleGetNewMailMsg1_", mailDataIndexArray.size());
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "handleGetNewMailMsg1_", mailDataIndexArray.size());
		
		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				synchronized (ServiceInterface.class)
				{
					isHandlingGetNewMailMsg = true;

					for (int i = 0; i < mailDataIndexArray.size(); i++)
					{
						handleMailDataIndex(mailDataIndexArray.get(i).intValue(), true,true);
					}
					mailDataIndexArray.clear();

					postChannelInfo(channelInfo);

					isHandlingGetNewMailMsg = false;

					ChatServiceController.hostActivity.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								if (ChatServiceController.getChannelListFragment() != null)
								{
									ChatServiceController.getChannelListFragment().refreshTitleLabel();
								}
							}
							catch (Exception e)
							{
								LogUtil.printException(e);
							}
						}
					});
				}
			}
		};

		Thread thread = new Thread(run);
		thread.start();
	}
	
	private static synchronized void handleMailDataIndexForGetNew(final int mailDataIndex, final int isFlag)
	{
		if (mailDataIndex < 0)
			return;

		final Object[] mailDataArr = ChatServiceController.getInstance().host.getMailDataArray(mailDataIndex,isFlag);
		if (mailDataArr == null)
			return;

		boolean hasDetectMail = false;
		String channelId = "";

		for (int i = 0; i < mailDataArr.length; i++)
		{
			MailData mailData = (MailData) mailDataArr[i];
			if (mailData == null)
				continue;
			if (!hasDetectMail && (mailData.getType() == MailManager.MAIL_DETECT_REPORT||mailData.getType() == MailManager.Mail_NEW_SCOUT_REPORT_FB))
				hasDetectMail = true;
			mailData.parseMailTypeTab();
			mailData.setNeedParseByForce(true);
			mailData = MailManager.getInstance().parseMailDataContent(mailData);
			ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData.getChannelId());
			if (channel != null)
			{
				mailData.flag = isFlag==1?1:0;
				DBManager.getInstance().insertMailData(mailData, channel);
				channel.refreshRenderData();
				channel.updateSysMailCountFromDB(1);
				if(mailData.isUnread()) {
					channel.updateUnreadSysMailCountFromDB(1);
				}
//				if(ChannelManager.getInstance().needParseFirstChannel(channel.channelID))
//					ChannelManager.getInstance().parseFirstChannelID();

				// 若重登陆（getNew且没有reset）时，channel中已经有系统邮件，loadMore不会加载新收到的邮件，需要直接加入channel
				if (channel.hasSysMailInList())
				{
					final MailData mail = parseMailData(mailData, false);
					if (mail != null){

						channel.addNewMailData(mail);

//						ChatServiceController.hostActivity.runOnUiThread(new Runnable()
//						{
//							@Override
//							public void run()
//							{
//								try
//								{
//									ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL,
//											mail.getChannelId());
//									if (channel != null)
//									{
//										channel.addNewMailData(mail);
//									}
////									ChannelManager.getInstance().calulateAllChannelUnreadNum();
//								}
//								catch (Exception e)
//								{
//									LogUtil.printException(e);
//								}
//							}
//						});
					}
				}
			}
			
			
		}
		ChannelManager.getInstance().calulateAllChannelUnreadNum();

		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, 
				"hasMoreNewMailToGet", MailManager.hasMoreNewMailToGet, "latestMailUidFromGetNew", MailManager.latestMailUidFromGetNew,
				"hasDetectMail", hasDetectMail);
		
		if(MailManager.hasMoreNewMailToGet)
		{
			if(StringUtils.isNotEmpty(MailManager.latestMailUidFromGetNew) && !MailManager.latestMailUidFromGetNew.equals("0"))
			{
				MailData mail = DBManager.getInstance().getSysMailByID(MailManager.latestMailUidFromGetNew);
				if(mail!=null)
				{
					long time = (long)(mail.getCreateTime())*1000;
					JniController.getInstance().excuteJNIVoidMethod("getNewMailFromServer", new Object[]{MailManager.latestMailUidFromGetNew,String.valueOf(time),20});
				}
			}
		}
		else
		{
			ChannelManager.getInstance().prepareSystemMailChannel();
			ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						if (ChatServiceController.getChannelListFragment() != null)
						{
							ChatServiceController.getChannelListFragment().refreshTitleLabel();
						}
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}

		if (StringUtils.isNotEmpty(ChannelManager.currentOpenedChannel) && ChannelManager.currentOpenedChannel.equals(channelId))
			ChannelManager.getInstance().postNotifyPopup(channelId);

		if (hasDetectMail)
			DBManager.getInstance().getDetectMailInfo();
		ChannelListFragment.onMailAdded();
		ChatFragment.onMailAdded();
	}

	private static synchronized void handleMailDataIndex(final int mailDataIndex, boolean isGetNew,boolean isFlag)
	{
		if (mailDataIndex < 0)
			return;
		int flag = 0;
		if(isGetNew) {
			flag = 1;
		}
		final Object[] mailDataArr = ChatServiceController.getInstance().host.getMailDataArray(mailDataIndex, flag);
		if(mailDataArr == null) return;

		boolean hasDetectMail = false;
		String channelId = "";
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "handleMailDataIndex", mailDataArr.length, "isGetNew", isGetNew,"isFlag",isFlag);
		for (int i = 0; i < mailDataArr.length; i++)
		{
			MailData mailData = (MailData)mailDataArr[i];
			if (mailData == null)
				continue;
			if (!hasDetectMail && (mailData.getType() == MailManager.MAIL_DETECT_REPORT||mailData.getType() == MailManager.Mail_NEW_SCOUT_REPORT_FB))
				hasDetectMail = true;
//			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "mailData.contentText", mailData);
			if (!isGetNew)
			{
				
				final MailData mail = parseMailData(mailData, false);
				if (mail != null)
				{
					ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL,
							mail.getChannelId());
					if (channel != null) {
						channel.addNewMailData(mail, false);
					}
					ChannelManager.getInstance().calulateAllChannelUnreadNum();
//					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "mailData.contentText", mail.contentText);
//					ChatServiceController.hostActivity.runOnUiThread(new Runnable()
//					{
//						@Override
//						public void run()
//						{
//							try
//							{
//								ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL,
//										mail.getChannelId());
//								if (channel != null)
//								{
//									//channel.addNewMailData(mail);
//									channel.addNewMailData(mail,false);
//								}
//								ChannelManager.getInstance().calulateAllChannelUnreadNum();
//							}
//							catch (Exception e)
//							{
//								LogUtil.printException(e);
//							}
//						}
//					});

					if (StringUtils.isEmpty(channelId))
						channelId = mail.getChannelId();
				}

			}
			else
			{
				mailData.parseMailTypeTab();
				ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData.getChannelId());
				if (channel != null)
				{
					mailData.flag = isFlag==true?1:0;
					DBManager.getInstance().insertMailData(mailData, channel);
					channel.refreshRenderData();
					channel.updateSysMailCountFromDB(1);
					if(mailData.isUnread())
						channel.updateUnreadSysMailCountFromDB(1);
//					if(ChannelManager.getInstance().needParseFirstChannel(channel.channelID))
//						ChannelManager.getInstance().parseFirstChannelID();
				}
			}
		}

		if (StringUtils.isNotEmpty(ChannelManager.currentOpenedChannel) && ChannelManager.currentOpenedChannel.equals(channelId))
			ChannelManager.getInstance().postNotifyPopup(channelId);

		if (hasDetectMail)
			DBManager.getInstance().getDetectMailInfo();
		ChannelListFragment.onMailAdded();
		ChatFragment.onMailAdded();
	}
	
	public static MailData handleMailData(int channelType, final MailData mailData, boolean isFromDB, boolean needUpdateDB)
	{
		if (mailData == null)
			return null;

		boolean isWorldBossKillRewardMail = false;
		if (mailData.isWorldBossKillRewardMail())
		{
			isWorldBossKillRewardMail = true;
			mailData.setType(MailManager.MAIL_WORLD_BOSS);
		}

		final ChatChannel channel = ChannelManager.getInstance().getChannel(channelType, mailData.getChannelId());
		if (channel == null)
			return mailData;
		if (!isFromDB)
		{
			mailData.flag = 0;
			DBManager.getInstance().insertMailData(mailData, channel);
			channel.updateSysMailCountFromDB(1);
		}
		else
		{
			mailData.flag = 1;
			if (needUpdateDB || isWorldBossKillRewardMail)
			{
				mailData.channelId = mailData.getChannelId();
				DBManager.getInstance().updateMail(mailData);
			}
		}

		return mailData;
	}

	public static void handleMessage(final MsgItem[] chatInfoArr, final String channelId, final String customName,
			final boolean calulateUnread, final boolean isFromServer)
	{
		for (int i = 0; i < chatInfoArr.length; i++)
		{
			if (isFromServer && chatInfoArr[i].hasTranslation())
				chatInfoArr[i].translatedLang = ConfigManager.getInstance().gameLang;
			if (!chatInfoArr[i].isRedPackageMessage())
				chatInfoArr[i].sendState = MsgItem.SEND_SUCCESS;
			// 存储用户信息
			chatInfoArr[i].initUserForReceivedMsg(channelId, customName);
			if (TranslateManager.getInstance().hasTranslated(chatInfoArr[i]))
				chatInfoArr[i].hasTranslated = true;
			else
				chatInfoArr[i].hasTranslated = false;
		}

		final int channelType = chatInfoArr[0].channelType;
		final boolean isNewMessage = chatInfoArr[0].isNewMsg;

		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "channelType", channelType, "channelId", channelId,
				"calulateUnread", calulateUnread, "isFromServer", isFromServer);
		if (isFromServer)
		{
			save2DB(chatInfoArr, channelType, channelId, customName);
		}

		if (ChatServiceController.getChatFragment() != null && isNewMessage)
		{
			ChatServiceController.getChatFragment().refreshIsInLastScreen(channelType);
		}

		try
		{
			// TODO 如果消息属于当前的聊天窗口，则无需刷新unread
			if (calulateUnread && (channelType == DBDefinition.CHANNEL_TYPE_USER || channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)) {
				ChannelManager.getInstance().calulateAllChannelUnreadNum();
			}

			handleMessage2(channelType, isNewMessage, chatInfoArr, channelId, customName, isFromServer);
			if(channelType == DBDefinition.CHANNEL_TYPE_CHATROOM){
				if(channelType == DBDefinition.CHANNEL_TYPE_CHATROOM){
					final Activity currentActivity;
					if(ChatServiceController.chat_v2_on) {
						//聊天v2
						currentActivity = ChatServiceController.getCurrentV2Activity();
					}
					else
					{
						currentActivity = ChatServiceController.getCurrentActivity();
					}

					if (currentActivity != null) {
						currentActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								//新聊天需要,添加同步未读聊天
								if(ChatServiceController.chat_v2_on) {
									LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "new chat v2 need add....");
								}
								else {
									if (ChatServiceController.getChatFragment() != null) {
										ChatServiceController.getChatFragment().refreshUnreadCount();
									}
								}

							}
						});
					}
				}
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}

		if (ConfigManager.autoTranlateMode > 0)
		{
			for (int i = 0; i < chatInfoArr.length; i++)
			{
				TranslateManager.getInstance().loadTranslation(chatInfoArr[i], null);
			}
		}
	}

	private static void handleMessage2(final int channelType, final boolean isNewMessage, final MsgItem[] chatInfoArr,
			final String channelId, String customName, boolean isFromServer)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "isNewMessage", isNewMessage, "isFromServer", isFromServer);
		final ChatChannel channel = ChannelManager.getInstance().getChannel(channelType, channelId);
		if (channel == null)
			return;

		if (channelType == DBDefinition.CHANNEL_TYPE_USER || channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			channel.customName = customName;
		}

		if (chatInfoArr.length <= 0)
			return;

		ArrayList<MsgItem> msgList = channel.msgList;
		ArrayList<MsgItem> sendingMsgList = channel.sendingMsgList;
		ChannelManager.getInstance().setHasRequestDataBeforeFlag(channelType, channelId, true);
		if (isNewMessage)
		{
			for (int i = 0; i < chatInfoArr.length; i++)
			{
				MsgItem sendingMsg = null;
				MsgItem recievedMsg = chatInfoArr[i];
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "recievedMsg.msg", recievedMsg.msg,
						"recievedMsg sendLocalTime", recievedMsg.sendLocalTime, "recievedMsg.isSelfMsg()", recievedMsg.isSelfMsg(),
						"recievedMsg.isSystemMessage()", recievedMsg.isSystemMessage(), "recievedMsg.isHornMessage()",
						recievedMsg.isHornMessage());
				if (msgList != null && msgList.size() > 0)
				{
					for (int j = 0; j < sendingMsgList.size(); j++)
					{
						MsgItem sendMsg = sendingMsgList.get(j);
						if (sendMsg == null)
							continue;
						LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "sendMsg.msg", sendMsg.msg,
								"sendMsg.sendLocalTime", sendMsg.sendLocalTime);
						if (sendMsg.sendLocalTime != 0 && sendMsg.sendLocalTime == recievedMsg.sendLocalTime)
						{
							sendingMsg = sendMsg;
						}
					}
				}

				// 我发的消息
				if (sendingMsg != null && recievedMsg.isSelfMsg() && (!recievedMsg.isSystemMessage() || recievedMsg.isHornMessage()))
				{
					sendingMsg.sendState = MsgItem.SEND_SUCCESS;
					sendingMsgList.remove(sendingMsg);
					channel.replaceDummyMsg(recievedMsg, msgList.indexOf(sendingMsg));
				}
				else
				{
					channel.addNewMsg(recievedMsg,true);
				}

				if (ChatServiceController.getChatFragment() != null)
				{
//					ChatServiceController.getChatFragment().notifyDataSetChanged();
//					ChatServiceController.getChatFragment().notifyDataSetChanged(channelType, true);
//					ChatServiceController.getChatFragment().updateListPositionForNewMsg(channelType, recievedMsg.isSelfMsg,recievedMsg.post);
//
//					final MsgItem msgItem = recievedMsg;
//					ChatServiceController.hostActivity.runOnUiThread(new Runnable()
//					{
//						@Override
//						public void run()
//						{
//							try
//							{
//								if (msgItem.isHornMessage() && ChatServiceController.getChatFragment() != null)
//									ChatServiceController.getChatFragment().showHornScrollText(msgItem);
//							}
//							catch (Exception e)
//							{
//								LogUtil.printException(e);
//							}
//						}
//					});

				}
				else
				{
					if (recievedMsg.isHornMessage())
					{
						ScrollTextManager.getInstance().clear();
						ScrollTextManager.getInstance().push(recievedMsg);
					}
				}
			}
		}
		else
		{
			int loadCount = 0;
			MsgItem oldFirstItem = null;
			if (channel.msgList != null && channel.msgList.size() > 0)
				oldFirstItem = channel.msgList.get(0);
			for (int i = 0; i < chatInfoArr.length; i++)
			{
				boolean isAddSuccess = channel.addHistoryMsg(chatInfoArr[i],false);
				if (isAddSuccess)
					loadCount++;
			}

			if (loadCount > 0)
				channel.getLoadedTimeNeedShowMsgIndex(loadCount);

			if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
			{
				if (!oldHornMsgPushed && channel.msgList != null && channel.msgList.size() > 0)
				{
					for (int i = 0; i < channel.msgList.size(); i++)
					{
						MsgItem msgItem = channel.msgList.get(i);
						if (msgItem != null && msgItem.isHornMessage())
						{
							ScrollTextManager.getInstance().clear();
							ScrollTextManager.getInstance().push(msgItem);
							oldHornMsgPushed = true;
						}
					}
				}
			}

			if (ChatServiceController.getChatFragment() != null)
			{
				ChatServiceController.getChatFragment().notifyDataSetChanged(channelType, false);
				ChatServiceController.getChatFragment().updateListPositionForOldMsg(channelType, loadCount,
						!ChatServiceController.getInstance().isDifferentDate(oldFirstItem, channel.msgList));
				ChatServiceController.getChatFragment().resetMoreDataStart(channelType);
			}
		}

		//聊天v2
		if(ChatServiceController.chat_v2_on) {
			ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (ServiceInterface.getServiceDelegate() != null) {
						ServiceInterface.getServiceDelegate().notifyMsgAdd(channel.channelID, channelType, chatInfoArr);
					}
				}
			});
		}

		if (isFromServer)
		{
			// 会触发reload，仅在服务器端来了新消息才调用
			if(channel.channelType == DBDefinition.CHANNEL_TYPE_USER) {
				ChannelListFragment.onMsgAdded(channel);
			}
			if(channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM && channel.channelID.contains("custom")) {
				ChatFragment.onMsgAdded(channel);
			}
		}

		if (WebSocketManager.isRecieveFromWebSocket(channelType) && (channel.isCountryChannel() || channel.isAllianceChannel() || channel.isChatRoomChannel()))
		{
//			sendChatLatestMessage(channel);
			JniController.getInstance().excuteJNIVoidMethod("postChatLatestInfo", new Object[]{""});
		}
	}

	public static void notifyChatRoomNameChanged(final String modifyName)
	{
		UserManager.getInstance().getCurrentMail().opponentName = modifyName;

		if (ChatServiceController.getChatRoomSettingActivity() != null)
		{
			ChatServiceController.getChatRoomSettingActivity().refreshChatRoomName();
//			ChatServiceController.getChatRoomSettingActivity().refreshTitle();
		}

		ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (ChatServiceController.getChatFragment() != null
							&& ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_CHATROOM) {
						ChatServiceController.getChatFragment().changeChatRoomName(modifyName);
					}
				} catch (Exception e) {
					LogUtil.printException(e);
				}
			}
		});
	}

	public static void postChannelNoMoreData(int channelType, boolean hasNoMoreData)
	{
		if (StringUtils.isEmpty(UserManager.getInstance().getCurrentUserId()))
			return;

		ChannelManager.getInstance().setNoMoreDataFlag(ChannelManager.channelType2tab(channelType), hasNoMoreData);
	}

	public static void removeAllMailByUid(String fromUid)
	{
		if (StringUtils.isEmpty(UserManager.getInstance().getCurrentUserId()))
			return;

		ChannelManager.getInstance().removeAllMailMsgByUid(fromUid);
	}

	public static void setCurrentUserId(String uidStr)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "uid", uidStr);
		UserManager.getInstance().setCurrentUserId(uidStr);
		MailManager.getInstance().clearData();
	}

	private static UserInfo	currentUserClone;

	/**
	 * 初始登录、重新登录、切服时会调用
	 * 
	 * @param worldTime
	 *            utc时间，单位为s
	 */
	public static void setPlayerInfo(int country, int worldTime, int gmod, int headPicVer, String name, String careerName, String uidStr, String picStr,
			int vipLevel, int svipLevel,int vipEndTime, int lastUpdateTime, int crossFightSrcServerId,int monthCard,int isShowServerId,int level,long gold,int vipframe)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "uid", uidStr, "name", name, "country", country,
				"crossFightSrcServerId", crossFightSrcServerId);

		TimeManager.getInstance().setServerBaseTime(worldTime);
		
		UserInfo userInfo = UserManager.getInstance().getCurrentUser();
		if(userInfo!=null)
		{
			currentUserClone = (UserInfo)userInfo.clone();
			UserManager.getInstance().getCurrentUser().serverId = country;
			UserManager.getInstance().getCurrentUser().headPicVer = headPicVer;
			UserManager.getInstance().getCurrentUser().mGmod = gmod;
			UserManager.getInstance().getCurrentUser().userName = name;
			UserManager.getInstance().getCurrentUser().careerName = careerName;
			UserManager.getInstance().getCurrentUser().headPic = picStr;
			UserManager.getInstance().getCurrentUser().vipLevel = vipLevel;
			UserManager.getInstance().getCurrentUser().svipLevel = svipLevel;
            UserManager.getInstance().getCurrentUser().vipframe = vipframe;
			UserManager.getInstance().getCurrentUser().vipEndTime = vipEndTime;
			UserManager.getInstance().getCurrentUser().lastUpdateTime = lastUpdateTime;
			UserManager.getInstance().getCurrentUser().crossFightSrcServerId = crossFightSrcServerId;
			UserManager.getInstance().getCurrentUser().monthCard = monthCard;
			UserManager.getInstance().getCurrentUser().chatShowServerId = isShowServerId;
			UserManager.getInstance().getCurrentUser().gold = gold;
			UserManager.getInstance().getCurrentUser().level = level;
			UserManager.getInstance().updateUser(UserManager.getInstance().getCurrentUser());
		}

		ChatServiceController.crossFightSrcServerId = crossFightSrcServerId;
		ChannelManager.getInstance().getCountryChannel();
		MailManager.getInstance().clearData();

		if (WebSocketManager.isWebSocketEnabled())
		{
			WebSocketManager.getInstance().setUserInfo();
		}

		//聊天 v2
		if (ChatServiceController.chat_v2_on){
			if (ServiceInterface.getServiceDelegate() != null) {
				ServiceInterface.getServiceDelegate().loadChatChannel();
			}
		}
	}

	public static void setPlayerAllianceJoinTime(int joinTime)
	{

		if(UserManager.getInstance().getCurrentUser()!=null) {
			UserManager.getInstance().getCurrentUser().joinAllianceTime = joinTime;
		}
	}

	/**
	 * 初始登录时会调用 打开聊天时，会紧接着setPlayerInfo后面调
	 * 重新登录、切服等时候，会调C++的parseData()刷新联盟信息，也调用此函数
	 */
	public static void setPlayerAllianceInfo(String asnStr, String allianceIdStr, int alliancerank, boolean isFirstJoinAlliance ,int createServer)
	{
		//LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "allianceIdStr", allianceIdStr, "current allianceId", UserManager.getInstance().getCurrentUser().allianceId);

		// 变更联盟（退出联盟）
		if (UserManager.getInstance().isCurrentUserInAlliance()
				&& !UserManager.getInstance().getCurrentUser().allianceId.equals(allianceIdStr) && ChannelManager.isInited()
				&& ChannelManager.getInstance().getAllianceChannel().msgList != null)
		{
			ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						ChannelManager.getInstance().getAllianceChannel().resetMsgChannel();
						if (ChatServiceController.getChatFragment() != null)
						{
							ChatServiceController.getChatFragment().notifyDataSetChanged(DBDefinition.CHANNEL_TYPE_ALLIANCE, true);
						}
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}

		boolean isJoinAlliance = false;
		ChatServiceController.isInTempAlliance = ChatServiceController.getIsInTempAlliance();
		// 加入联盟
		if (ConfigManager.useWebSocketServer && !UserManager.getInstance().isCurrentUserInAlliance()
				&& StringUtils.isNotEmpty(allianceIdStr))
		{
			isJoinAlliance = true;
		}

		if (currentUserClone == null)
		{
			UserInfo userInfo = UserManager.getInstance().getCurrentUser();
			if(userInfo!=null){
				currentUserClone = (UserInfo) userInfo.clone();
			}
		}
			

		if(UserManager.getInstance().getCurrentUser()!=null){
			UserManager.getInstance().getCurrentUser().asn = asnStr;
			UserManager.getInstance().getCurrentUser().allianceId = allianceIdStr;
			UserManager.getInstance().getCurrentUser().allianceRank = alliancerank;
			UserManager.getInstance().getCurrentUser().createServer = createServer;
		}

		ConfigManager.getInstance().isFirstJoinAlliance = isFirstJoinAlliance;
		// 使用旧后台、且db中没有联盟时，需要将allianceChannel加入channelMap，以免getChannelInfo中没有联盟
	    ChannelManager.getInstance().getAllianceChannel();
		if (isJoinAlliance)
		{
			// 可能在登陆时调用，此时ws未初始化，调用无效
			if(UserManager.getInstance().getCurrentUser()!=null) {
				WebSocketManager.getInstance().joinRoom();
			}
		}
		
		if (currentUserClone!=null&&!currentUserClone.equalsLogically(UserManager.getInstance().getCurrentUser()))
		{
			LogUtil.printVariables(
					Log.INFO,
					LogUtil.TAG_CORE,
					"current user updated:\n"
							+ LogUtil.compareObjects(new Object[] { UserManager.getInstance().getCurrentUser(), currentUserClone }));
			UserManager.getInstance().updateCurrentUser();
		}
		
//        JniController.getInstance().excuteJNIVoidMethod("getLatestChatMessage", null);
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
		UserManager.getInstance().isInitUserInfo = true;
	}
	
	public static void connect2WS()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
		if (WebSocketManager.isWebSocketEnabled())
		{
			WebSocketManager.getInstance().setStatusListener(WebSocketStatusHandler.getInstance());
			if( LogUtil.nativeIsFLOG()) {
				LogUtil.nativeFLOG("CS Java Connect WS");
			}
			WebSocketManager.getInstance().connect();
		}
	}

	public static void startPingCS()
	{
		if (WebSocketManager.isWebSocketEnabled())
		{
			WebSocketManager.getInstance().startPingCurrentServer();
		}
	}

	// TODO 与setPlayerAllianceInfo总是成对调用，没必要调
	// 但切服之后可能需要调
	public static void resetPlayerIsInAlliance()
	{
		if (StringUtils.isEmpty(UserManager.getInstance().getCurrentUserId()))
			return;

		LogUtil.printVariables(Log.INFO, LogUtil.TAG_CORE, "actual resetPlayerIsInAlliance()");

		UserManager.getInstance().clearAllianceMember();

		if (UserManager.getInstance().getCurrentUser().allianceId.equals(""))
			return;

		if (ChannelManager.isInited())
		{
			ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						if (UserManager.getInstance().isCurrentUserInAlliance()
								&& ChannelManager.getInstance().getAllianceChannel().msgList != null)
						{
							// 有时候会发生nullPointer异常
							ChannelManager.getInstance().getAllianceChannel().msgList.clear();
							if (ChatServiceController.getChatFragment() != null)
							{
								ChatServiceController.getChatFragment().notifyDataSetChanged(DBDefinition.CHANNEL_TYPE_ALLIANCE, true);
							}
						}
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});

			ChannelManager.getInstance().setNoMoreDataFlag(1, false);
		}

		if (ConfigManager.useWebSocketServer)
		{
			// 可能在登陆时调用，此时ws未初始化，调用无效
			WebSocketManager.getInstance().leaveAllianceRoom();
		}
		UserManager.getInstance().getCurrentUser().asn = "";
		UserManager.getInstance().getCurrentUser().allianceId = "";
		UserManager.getInstance().getCurrentUser().allianceRank = -1;
		UserManager.getInstance().getCurrentUser().joinAllianceTime = 0;
		UserManager.getInstance().updateCurrentUser();
	}

	/**
	 * 锁屏时调用 以前是切换tab时才会获取数据，用chat.get接口，如果发现已经有数据，就不会再获取，所以得先clear一次
	 */
	public static void clearCountryMsg()
	{
	}

	// 以前就未调用
	public static void clearMailMsg()
	{
	}

	/**
	 * 论坛重新登录
	 */
//	public static void onPlayerChanged()
//	{
//		ForumFragment.isFirstLogin = true;
//	}

	public static void onLogOutCmd()
	{
		UserManager.getInstance().isInitUserInfo = false;
		WebSocketManager.getInstance().clearSocket();
	}

	public static void connectToWSManully(String server, String port, String protocol) {
		WebSocketManager.getInstance().connectToWSManully( server,  port,  protocol);
	}

	public static boolean isCurrentWSServer(String server, String port, String protocol) {
		if (WebSocketManager.isWebSocketEnabled())
		{
			try {

				WSServerInfo wi = WebSocketManager.getInstance().getCurrentServer();
				if ( wi != null ) {
					if( wi.address.equals(server) && wi.port.equals(port) && wi.protocol.equals(protocol)) {
						return true;
					}
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}

	public static void notifyChangeLanguage()
	{
		Object[] langItemArray = ChatServiceController.getInstance().host.getChatLangArray();
		if (langItemArray == null)
			return;

		LanguageItem[] langArray = new LanguageItem[langItemArray.length];
		for (int i = 0; i < langItemArray.length; i++)
		{
			Object obj = langItemArray[i];
			if (obj != null)
			{
				langArray[i] = (LanguageItem) obj;
			}
		}
		LanguageManager.initChatLanguage(langArray);
	}

	//通知-离开语言聊天室
	public static void notifyLeaveChatRoom(String language) {
		if (ChatServiceController.chat_language_on && ServiceInterface.getServiceDelegate() != null) {
			ServiceInterface.getServiceDelegate().notifyLeaveChatRoom(language);
		}
	}

	public static void onCreateChatroomSuccess()
	{
		ChatServiceController.isCreateChatRoom = false;
		ServiceInterface.showChatActivity(ChatServiceController.getCurrentActivity(), DBDefinition.CHANNEL_TYPE_CHATROOM, false);
	}

	public static boolean isDontKeepActivitiesEnabled()
	{
		int finishActivitiesEnabled = Settings.System.getInt(ChatServiceController.hostActivity.getContentResolver(),
				Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0);

		return finishActivitiesEnabled == 1;
	}

	public static void gotoDevelopmentSetting()
	{
		ChatServiceController.hostActivity.startActivityForResult(new Intent(
				android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS), 0);
	}

	public static void showChatActivityFrom2dx(int maxHornInputCount, final int chatType, int sendInterval, final boolean rememberPosition,
			boolean enableCustomHeadImg, boolean isNoticeItemUsed)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "chatType", chatType, "sendInterval", sendInterval,
				"rememberPosition", rememberPosition, "enableCustomHeadImg", enableCustomHeadImg, "isNoticeItemUsed", isNoticeItemUsed);

		ConfigManager.maxHornInputLength = maxHornInputCount;
		ConfigManager.enableCustomHeadImg = enableCustomHeadImg;
		ChatServiceController.isHornItemUsed = isNoticeItemUsed;
		ConfigManager.sendInterval = sendInterval * 1000;
		ChatServiceController.isCreateChatRoom = false;
		if (ChatServiceController.hostActivity != null)
		{
			ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						if(chatType == DBDefinition.CHANNEL_TYPE_CHATROOM && ChatServiceController.topChatRoomUid != "") {
							ChatChannel channel = ChannelManager.getInstance().getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, ChatServiceController.topChatRoomUid));
							if (channel != null) {
								ServiceInterface.setMailInfo(channel.channelID, channel.latestId, channel.getCustomName(), chatType);
							}
						}

						//聊天v2
						boolean isUserChatView = chatType != DBDefinition.CHANNEL_TYPE_USER || (ChatServiceController.chat_v2_personal && chatType == DBDefinition.CHANNEL_TYPE_USER);
						if (ChatServiceController.chat_v2_on && isUserChatView){
							ServiceInterface.showNewChatActivity(ChatServiceController.hostActivity, chatType, rememberPosition);
						}
						else
						{
							ServiceInterface.showChatActivity(ChatServiceController.hostActivity, chatType, rememberPosition);
						}
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}
	}

	public static void showMemberSelectorFrom2dx()
	{
		if (ChatServiceController.hostActivity != null)
		{
			ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						ChatServiceController.isCreateChatRoom = true;
						ServiceInterface.showMemberSelectorActivity(ChatServiceController.hostActivity, false);
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}
	}

	public static void showChannelListFrom2dx(final boolean isGoBack,final int tabType)
	{
		if (ChatServiceController.hostActivity != null)
		{
			ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try {
						if(tabType>=0){
							ServiceInterface.showChannelListActivity(ChatServiceController.hostActivity, true,
									DBDefinition.CHANNEL_TYPE_OFFICIAL, ChannelManager.getInstance().getChannelIdByTabType(tabType), isGoBack);
							return;
						}
						if (ChatServiceController.canJumpToSecondaryList()) {
							ServiceInterface.showChannelListActivity(ChatServiceController.hostActivity, ChatServiceController.rememberSecondChannelId,
									DBDefinition.CHANNEL_TYPE_OFFICIAL, ChatServiceController.lastSecondChannelId, isGoBack);
							ChatServiceController.rememberSecondChannelId = false;
							ChatServiceController.lastSecondChannelId = "";

						}else {
							ServiceInterface.showChannelListActivity(ChatServiceController.hostActivity, false,
									DBDefinition.CHANNEL_TYPE_OFFICIAL, null, isGoBack);
						}
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}
	}

	public static void notifyUserInfo(final int index)
	{
		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				handleUserInfoIndex(index);
			}
		};
		UserManager.getInstance().runOnExecutorService(run);
	}

	private static synchronized void handleUserInfoIndex(int index)
	{
		UserManager.getInstance().onReceiveUserInfo(ChatServiceController.getInstance().host.getUserInfoArray(index));
	}

	public static void notifySearchedUserInfo(int index)
	{
		UserManager.getInstance().onReceiveSearchUserInfo(ChatServiceController.getInstance().host.getSearchedUserInfoArray(index));
	}

	public static void notifyUserUids(final String uidStr, final String lastUpdateTimeStr, final int type)
	{
		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				handleUserUids(uidStr,lastUpdateTimeStr,type);
			}
		};
		UserManager.getInstance().runOnExcrutorServiceUids(run);
	}

	private static synchronized void handleUserUids(String uidStr, String lastUpdateTimeStr, int type)
	{
		if (uidStr.equals("") || lastUpdateTimeStr.equals(""))
			return;
		String[] uidArr = uidStr.split("_");
		String[] lastUpdateTimeArr = lastUpdateTimeStr.split("_");

		if (type == UserManager.NOTIFY_USERINFO_TYPE_ALLIANCE)
			UserManager.getInstance().clearAllianceMember();
		else if (type == UserManager.NOTIFY_USERINFO_TYPE_FRIEND)
			UserManager.getInstance().clearFriendMember();
		for (int i = 0; i < uidArr.length; i++)
		{
			if (!uidArr[i].equals(""))
			{
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "uidStr", uidStr, "lastUpdateTimeStr", lastUpdateTimeStr, "type", type);
				UserManager.checkUser(uidArr[i], "", 0);
				UserInfo user = UserManager.getInstance().getUser(uidArr[i]);

				if (user != null)
				{
					if (type == UserManager.NOTIFY_USERINFO_TYPE_ALLIANCE)
						UserManager.getInstance().putChatRoomMemberInMap(user);
					else if (type == UserManager.NOTIFY_USERINFO_TYPE_FRIEND)
						UserManager.getInstance().putFriendMemberInMap(user);
				}

				// lastUpdateTimeArr[i]至少为0（C++中将空字符串设为"0"），redis中有的老用户还没有被更新过时，可能会有这种情况
				// 保险起见，这里再检查一下空字符串
				int lastUpdateTime = lastUpdateTimeArr[i].equals("") ? 0 : Integer.parseInt(lastUpdateTimeArr[i]);
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "uidStr", uidArr[i], "lastUpdateTime", lastUpdateTime, "type", type);
				UserManager.checkUser(uidArr[i], "", lastUpdateTime);
			}
		}
	}

	/**
	 * C++主动关闭原生，发生在网络断开连接时，或创建聊天室之后
	 */
	public static void exitChatActivityFrom2dx(boolean needRemeberActivityStack, boolean needTipsDialog)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "needRemeberActivityStack", needRemeberActivityStack);

		if (!needRemeberActivityStack) {
			ChannelListFragment.preventSecondChannelId = true;
		}

		final boolean bNeedTipsDialog = needTipsDialog;
		ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					Activity currentActivity = null;
					if(ChatServiceController.chat_v2_on) {
						//聊天v2
						currentActivity = ChatServiceController.getCurrentV2Activity();
						if (currentActivity != null){
							currentActivity.onBackPressed();
						}
						else
						{
							currentActivity = ChatServiceController.getCurrentActivity();
						}
					}
					else
					{
						currentActivity = ChatServiceController.getCurrentActivity();
					}

					ChatServiceController.showGameActivity(currentActivity);

					//其他客户端登录 - 回调显示提示框
					if (bNeedTipsDialog){
						ChatServiceController.setCurrentChannelType(-1);
						JniController.getInstance().excuteJNIVoidMethod("showGoBackGameDialog",null);
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	/**
	 * c++ 通知原生,是否是调试模式 连接外网
	 */
	public static void notifyExternaletworkebugFrom2dx(boolean externaletworkebug)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "notifyExternaletworkebugFrom2dx", externaletworkebug);
		ChatServiceController.isxternaletworkebug = externaletworkebug;
	}

	/**
	 * 这个时机比较奇怪，可能只调了一个activity的onDestroy，就会到这里，之后才会调其它activity的onDestroy
	 */
	public static void onReturn2dxGame()
	{
	}

	//安全的MakeText
	public static void safeMakeText(final Activity activity, final String contextText, final int duration)
	{
		if (activity == null){
			return;
		}

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (activity != null && !activity.isFinishing()){
						Toast toast = ToastCompat.makeText(activity, contextText, duration);
						toast.setGravity(Gravity.TOP, 0, 0);
						toast.show();
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});

	}

	public static void safeGravityMakeText(final Activity activity, final String contextText, final int duration, final int gravity, final int xOffset, final int yOffset)
	{
		if (activity == null){
			return;
		}

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (activity != null && !activity.isFinishing()){
						Toast toast = ToastCompat.makeText(activity, contextText, duration);
						toast.setGravity(gravity, xOffset, yOffset);
						toast.show();
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});

	}

	public static void flyHint(final String icon, String titleText, String contentText, float time, float dy, boolean useDefaultIcon)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "contentText", contentText, "titleText", titleText);

		final Activity currentActivity = ChatServiceController.getCurrentActivity();
		if (!ChatServiceController.isNativeShowing || currentActivity == null) {
			return;
		}

		final String text = contentText;
		final int duration = time > 0 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;

		currentActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					if ( currentActivity.getApplicationContext()!= null && text != null) {
						//实例化一个Toast对象
						Toast toast = ToastCompat.makeText(currentActivity.getApplicationContext(),text,duration);
						toast.setGravity(Gravity.TOP, 0, 0);
						if(text.contains(LanguageManager.getLangByKey("170250"))) {
							LayoutInflater inflater = currentActivity.getLayoutInflater();

							//将布局文件转换成相应的View对象
							View layout = inflater.inflate(R.layout.custome_toast_layout, (ViewGroup) currentActivity.findViewById(R.id.toast_layout_root));

							//从layout中按照id查找TextView对象
							DisplayMetrics dm = new DisplayMetrics();
							WindowManager manager = currentActivity.getWindowManager();
							manager.getDefaultDisplay().getMetrics(dm);
							int widthPixels = dm.widthPixels;
							ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(widthPixels, ViewGroup.LayoutParams.MATCH_PARENT);
							TextView textView = (TextView) layout.findViewById(R.id.toastText);
							textView.setLayoutParams(params);

							//设置TextView的text内容
							textView.setText(text);
							toast.setView(layout);
						}

						if (currentActivity != null && !currentActivity.isFinishing()) {
							toast.show();
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

	private static Timer	flyHintTimer;

	public static void stopFlyHintTimer()
	{
		if (flyHintTimer != null)
		{
			flyHintTimer.cancel();
			flyHintTimer.purge();
		}
	}

	private static int	flyHintCount;

	public static void flySystemUpdateHint(double countDown, boolean isFlyHintLogin, boolean isLogin, String tip, String icon)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "tip", tip, "isFlyHintLogin", isFlyHintLogin, "countDown",
				countDown);

		if (!ChatServiceController.isNativeShowing || ChatServiceController.getCurrentActivity() == null)
			return;
		stopFlyHintTimer();
		flyHintTimer = new Timer();
		final String text = tip;
		flyHintCount = (int) countDown / 10;
		final boolean flyHintLogin = isFlyHintLogin;

		TimerTask timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							String showText = "";
							if (flyHintLogin)
								if (flyHintCount / 60 > 0)
									showText = text
											+ "\n"
											+ LanguageManager.getLangByKey(LanguageKeys.FLYHINT_DOWN_MIN, String.valueOf(flyHintCount / 60));
								else
									showText = text + "\n"
											+ LanguageManager.getLangByKey(LanguageKeys.FLYHINT_DOWN_SECOND, String.valueOf(flyHintCount));

							MyActionBarActivity activity = ChatServiceController.getCurrentActivity();
							if (activity != null) {
								safeGravityMakeText(activity,showText,Toast.LENGTH_LONG,Gravity.TOP, 0, activity.getToastPosY());
							}
						}
						catch (Exception e)
						{
							LogUtil.printException(e);
						}
					}
				});

				flyHintCount--;
				if (flyHintCount <= 0)
				{
					stopFlyHintTimer();
				}
			}

		};
		flyHintTimer.schedule(timerTask, 0, 10000);
	}

	public static final int	TYPE_CHAT				= 0;
	public static final int	TYPE_FORUM				= 1;
	public static final int	TYPE_MEMBER_SELECTOR	= 2;

	public static void showChatActivity(Activity a, int channelType, boolean rememberPosition)
	{
		if (a == null)
			return;

		ChatFragment.rememberPosition = rememberPosition;

		Intent intent = null;
		try
		{
			if (channelType >= 0)
			{
				// 可能出异常
				intent = new Intent(a, ChatActivity.class);
				intent.putExtra("channelType", channelType);
			}

			if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
			{
				LogUtil.trackPageView(!rememberPosition ? "ShowCountry" : "ShowCountryReturn");
			}
			else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
			{
				LogUtil.trackPageView(!rememberPosition ? "ShowAlliance" : "ShowAllianceReturn");
			}
			else if (channelType == DBDefinition.CHANNEL_TYPE_USER)
			{
				LogUtil.trackPageView(!rememberPosition ? "ShowMail" : "ShowMailReturn");
			}
			else if (channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
			{
				LogUtil.trackPageView(!rememberPosition ? "ShowChatroom" : "ShowChatroomReturn");
			}

			showActivity(a, ChatActivity.class, true, false, intent, false, false);

		}
		catch (Exception e)
		{
			LogUtil.printException(e);
			return;
		}
	}

	public static void showNewChatActivity(Activity a, int channelType, boolean rememberPosition) {
		showNewActivity(a, "Chat", channelType, rememberPosition);
	}

	public static void showChatRoomSettingActivity(Activity a)
	{
		showActivity(a, ChatRoomSettingActivity.class, true, false, null, false, false);
	}

	public static void showChatRoomNameModifyActivity(Activity a)
	{
		showActivity(a, ChatRoomNameModifyActivity.class, true, false, null, false, false);
	}

	public static void showMemberSelectorActivity(Activity a, boolean requestResult)
	{
		LogUtil.trackPageView("ShowMemberSelector");
		showActivity(a, MemberSelectorActivity.class, true, false, null, requestResult, false);
	}
	public static void showChannelListActivity(Activity a, boolean isSecondLvList, int channelType, String channelId, boolean isGoBack)
	{
		Intent intent = new Intent(a, ChannelListActivity.class);
		intent.putExtra("isSecondLvList", isSecondLvList);
		intent.putExtra("isGoBack", isGoBack);
		if (channelType >= 0)
			intent.putExtra("channelType", channelType);
		if (StringUtils.isNotEmpty(channelId))
			intent.putExtra("channelId", channelId);

		ChatServiceController.isCurrentSecondList = isSecondLvList;

		showActivity(a, ChannelListActivity.class, true, false, intent, false, isGoBack);
	}

	public static void showWriteMailActivity(Activity a, boolean clearTop, String roomName, String uidStr, String nameStr)
	{
		LogUtil.trackPageView("ShowWriteMail");
		Intent intent = null;

		if (StringUtils.isNotEmpty(roomName) || StringUtils.isNotEmpty(uidStr) || StringUtils.isNotEmpty(nameStr))
		{
			intent = new Intent(a, WriteMailActivity.class);
			intent.putExtra("roomName", roomName);
			intent.putExtra("memberUids", uidStr);
			intent.putExtra("memberNames", nameStr);
		}

		showActivity(a, WriteMailActivity.class, true, clearTop, intent, false, clearTop);
	}

	//总统发送邮件独立出来
	public static void showPresidentWriteMailActivity(Activity a, boolean clearTop, int consumeGold, int remainNum,boolean isFromCpp)
	{
		LogUtil.trackPageView("showPresidentWriteMailActivity");
		Intent intent = null;

		intent = new Intent(a, WriteMailActivity.class);
		//用Bundle携带数据
		Bundle bundle=new Bundle();
		bundle.putInt("consumeGold", consumeGold);
		bundle.putInt("remainNum", remainNum);
		bundle.putBoolean("isFromCpp", isFromCpp);
		intent.putExtras(bundle);

		showActivity(a, WriteMailActivity.class, true, clearTop, intent, false, clearTop);
	}

	private static void showActivity(Activity a, Class<?> cls, boolean newTask, boolean clearTop, Intent intent, boolean requestResult,
			boolean popStackAnimation)
	{
		ArrayList<Object> args = new ArrayList<Object>();
		args.add("class");
		args.add(cls.getSimpleName());
		if (intent != null)
		{
			for (Iterator<String> iterator = intent.getExtras().keySet().iterator(); iterator.hasNext();)
			{
				String key = (String) iterator.next();
				args.add(key);
				args.add(intent.getExtras().get(key));
			}
		}
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, (Object[]) args.toArray(new Object[0]));

		ChatServiceController.isNativeStarting = true;
		ChatServiceController.isNativeShowing = true;
		ChatServiceController.isReturningToGame = false;
		ChannelListFragment.preventSecondChannelId = false;
		if (a instanceof ICocos2dxScreenLockListener)
		{
			MyActionBarActivity.previousActivity = (ICocos2dxScreenLockListener) a;
		}

		Intent i = intent != null ? intent : new Intent(a, cls);
		if (clearTop)
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		if (!requestResult)
		{
			a.startActivity(i);
		}
		else
		{
			a.startActivityForResult(i, 0);
		}

		if (!popStackAnimation)
		{
			a.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		}
		else
		{
			a.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
	}

	private static void showNewActivity(Activity hostActivity, String name, int channelType, boolean position) {
		ChatServiceController.isNativeStarting = true;
		ChatServiceController.isNativeShowing = true;
		ChatServiceController.isReturningToGame = false;
		ChannelListFragment.preventSecondChannelId = false;

		if (delegate != null) {
			delegate.showNewActivity(hostActivity, name, channelType, position);
		}
	}

	private static ArrayList<MyActionBarActivity>	activityStack	= new ArrayList<MyActionBarActivity>();

	public static void pushActivity(MyActionBarActivity a)
	{
		if (!activityStack.contains(a))
		{
			activityStack.add(a);
		}
		else
		{
			LogUtil.printVariablesWithFuctionName(Log.WARN, LogUtil.TAG_VIEW, "pushActivity already have", activityStack.size());
		}
		LogUtil.printVariables(Log.INFO, LogUtil.TAG_VIEW, "activityStack.size()", activityStack.size());
	}

	public static void popActivity(MyActionBarActivity a)
	{
		if (activityStack.contains(a))
		{
			activityStack.remove(a);
		}
		LogUtil.printVariables(Log.INFO, LogUtil.TAG_VIEW, "activityStack.size()", activityStack.size());
	}

	public static void popTopActivity()
	{
		int size = activityStack.size();
		for (int i = size; i > 1; i--) {
			MyActionBarActivity activity = activityStack.get(i - 1);
			activity.exitActivity();
		}
	}

	public static int getNativeActivityCount()
	{
		return activityStack.size();
	}

	public static void clearActivityStack()
	{
		activityStack.clear();
	}

	public static void postIsChatRoomMemberFlag(String groupId, boolean flag)
	{
		ChannelManager.getInstance().setIsMemberFlag(groupId, flag);

		if (ChatServiceController.hostActivity == null)
			return;
		ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (ChatServiceController.getChatFragment() != null) {
						if (ChatServiceController.getChatFragment().isSelectMemberBtnEnable()) {
							ChatServiceController.getChatFragment().refreshMemberSelectBtn();
							ChatServiceController.getChatFragment().setSelectMemberBtnState();
						}
					}
				} catch (Exception e) {
					LogUtil.printException(e);
				}
			}
		});
	}
	

	public static void postChannelInfo(final String channelInfo)
	{
		ChannelManager.getInstance().isGetingNewMsg = false;
		
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "handleChannelInfo1", channelInfo);
		
		long startTime = System.currentTimeMillis();
		ChannelManager.getInstance().handleChannelInfo(channelInfo);

		if (ChatServiceController.hostActivity == null)
			return;
		ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (ChatServiceController.getChatFragment() != null) {
						ChatServiceController.getChatFragment().refreshToolTip();
					}
				} catch (Exception e) {
					LogUtil.printException(e);
				}
			}
		});
		long offsetTime = System.currentTimeMillis() - startTime;

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "handleChannelInfo耗时", offsetTime);
	}

	public static String getChannelInfo()
	{
		ChannelManager.getInstance().isGetingNewMsg = true;
		ConfigManager.setMailPullState(true);
		String result = "";
		try
		{
			result = ChannelManager.getInstance().getChannelInfo();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		return result;
	}


	public static void postMoreMailInfo(boolean hasMoreMail, String latestMailUid)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "hasMoreMail", hasMoreMail, "latestMailUid", latestMailUid);
		MailManager.hasMoreNewMailToGet = hasMoreMail;
		MailManager.latestMailUidFromGetNew = latestMailUid;
		if (!hasMoreMail)
			ConfigManager.setMailPullState(false);

		// if(hasMoreMail)
		// return;

	}


	public static void postMorePersonMailInfo(boolean hasMoreMail, String latestMailUid)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "hasMoreNewPersonMailToGet", hasMoreMail, "latestPersonMailUidFromGetNew", latestMailUid);
		MailManager.hasMoreNewPersonMailToGet = hasMoreMail;
		MailManager.latestPersonMailUidFromGetNew = latestMailUid;
	}

	public static String getLatestSystemMailInfo()
	{
		return ChannelManager.getInstance().getLatestSystemMailInfo();
	}

	public static String getLatestPersonMailInfo()
	{
		return ChannelManager.getInstance().getLatestPersonMailInfo();
	}

	public static void setAutoTranlateMode(int mode)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "mode", mode);
		ConfigManager.autoTranlateMode = mode;
	}

	public static void setDisableLang(String disableLang)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "disableLang", disableLang);
		TranslateManager.getInstance().disableLang = disableLang;
	}

	public static void setMailSave(String mailId, int saveFlag)
	{
		MailData mail = DBManager.getInstance().getSysMailByID(mailId);
		if (mail != null)
		{
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "mailId", mailId, "saveFlag", saveFlag);
			if (mail.getSave() != saveFlag)
			{
				mail.setSave(saveFlag);
				DBManager.getInstance().updateMail(mail);
			}
			ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mail.getChannelId());
			if (channel == null || channel.mailDataList == null)
				return;
			channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
			DBManager.getInstance().updateChannel(channel);
			for (int i = 0; i < channel.mailDataList.size(); i++)
			{
				MailData mailData = channel.mailDataList.get(i);
				if (mailData != null && mailId.equals(mailData.getUid()))
				{
					if (mailData.getSave() != saveFlag)
					{
						mailData.setSave(saveFlag);
					}
					break;
				}
			}
		}
	}

	public static void setMailRewardStatus(String mailId)
	{
		MailData mail = DBManager.getInstance().getSysMailByID(mailId);
		if (mail != null)
		{
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "mailId", mailId, "channelId", mail.getChannelId());
			if (mail.getRewardStatus() == 0)
			{
				mail.setRewardStatus(1);
				DBManager.getInstance().updateMail(mail);
			}
			ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mail.getChannelId());
			if (channel == null || channel.mailDataList == null)
				return;
			channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
			DBManager.getInstance().updateChannel(channel);
			for (int i = 0; i < channel.mailDataList.size(); i++)
			{
				MailData mailData = channel.mailDataList.get(i);
				if (mailData != null && mailId.equals(mailData.getUid()))
				{
					if (mailData.getRewardStatus() == 0)
					{
						mailData.setRewardStatus(1);
					}
					break;
				}
			}
		}
	}

	public static void setMutiMailRewardStatus(String mailUids)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "mailUids", mailUids);
		if (StringUtils.isEmpty(mailUids))
			return;
		String[] mailUidArr = mailUids.split(",");
		for (int i = 0; i < mailUidArr.length; i++)
		{
			if (StringUtils.isNotEmpty(mailUidArr[i]))
			{
				MailData mail = DBManager.getInstance().getSysMailByID(mailUidArr[i]);
				if (mail != null)
				{
					if (mail.getRewardStatus() == 0)
					{
						if (mail.getStatus() == 0)
							mail.setStatus(1);
						mail.setRewardStatus(1);
						DBManager.getInstance().updateMail(mail);
					}
					ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mail.getChannelId());
					if (channel == null || channel.mailDataList == null)
						return;
					channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();

					for (int j = 0; j < channel.mailDataList.size(); j++)
					{
						MailData mailData = channel.mailDataList.get(j);
						if (mailData != null && mailUidArr[i].equals(mailData.getUid()))
						{
							if (mailData.getRewardStatus() == 0)
							{
								if (mailData.getStatus() == 0)
								{
									channel.unreadCount--;
									mailData.setStatus(1);
								}
								mailData.setRewardStatus(1);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         							}
							break;
						}
					}
					DBManager.getInstance().updateChannel(channel);
				}
			}
		}

		ChannelManager.getInstance().calulateAllChannelUnreadNum();
		if (ChatServiceController.hostActivity == null)
			return;
		ChatServiceController.hostActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (ChatServiceController.getChannelListFragment() != null)
					{
						ChatServiceController.getChannelListFragment().notifyDataSetChanged();
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public static void setMutiMailRewardStatusThenDelete(String mailUids)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "mailUids", mailUids);
		if (StringUtils.isEmpty(mailUids))
			return;
		String[] mailUidArr = mailUids.split(",");
		final List<ChannelListItem> sysMails = new ArrayList<ChannelListItem>();
		for (int i = 0; i < mailUidArr.length; i++)
		{
			if (StringUtils.isNotEmpty(mailUidArr[i]))
			{
				MailData mail = DBManager.getInstance().getSysMailByID(mailUidArr[i]);
				if (mail != null)
				{
					if (mail.getRewardStatus() == 0)
					{
						if (mail.getStatus() == 0)
							mail.setStatus(1);
						mail.setRewardStatus(1);
						DBManager.getInstance().updateMail(mail);
					}
					ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mail.getChannelId());
					if (channel == null || channel.mailDataList == null)
						return;
					channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();

					for (int j = 0; j < channel.mailDataList.size(); j++)
					{
						MailData mailData = channel.mailDataList.get(j);
						if (mailData != null && mailUidArr[i].equals(mailData.getUid()))
						{
							if (mailData.getRewardStatus() == 0)
							{
								if (mailData.getStatus() == 0)
								{
									channel.unreadCount--;
									mailData.setStatus(1);
								}
								mailData.setRewardStatus(1);
							}
							sysMails.add(mailData);
							break;
						}
					}
					DBManager.getInstance().updateChannel(channel);
				}
			}
		}

		if (ChatServiceController.hostActivity == null)
			return;
		ChatServiceController.hostActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (ChatServiceController.getChannelListFragment() != null)
					{
						ChatServiceController.getChannelListFragment().comfirmOperateMutiMail(sysMails, ChannelManager.OPERATION_REWARD_DELETE);
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	private static boolean isKnightMail(String[] mailUidArr)
	{
		if (mailUidArr.length > 0)
		{
			MailData mail = DBManager.getInstance().getSysMailByID(mailUidArr[0]);
			if (mail != null)
			{
				mail.setNeedParseByForce(true);
				MailData mailData = MailManager.getInstance().parseMailDataContent(mail);
				if (mailData != null && mailData.isKnightMail())
					return true;
			}
		}
		return false;
	}

	public static void setMutiMailStatusByConfigType(String mailUids, int configType, boolean isUnLock)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "mailUids", mailUids, "configType", configType, "isUnLock",
				isUnLock);

		if (StringUtils.isEmpty(mailUids)
				|| !(configType == DBManager.CONFIG_TYPE_READ || configType == DBManager.CONFIG_TYPE_SAVE || configType == DBManager.CONFIG_TYPE_DELETE))
			return;
		String[] mailUidArr = mailUids.split(",");
		boolean hasDetectMail = false;
		ChatChannel channel = null;

		if (isKnightMail(mailUidArr))
		{
			channel = ChannelManager.getInstance().getChannel(4, "knight");
//			deleteMail("knight", DBDefinition.CHANNEL_TYPE_OFFICIAL, -1);
			for (int i = 0; i < mailUidArr.length; i++){
				MailData mail = DBManager.getInstance().getSysMailByID(mailUidArr[i]);
				if (mail != null)
				{
					
					mail.setNeedParseByForce(true);
					MailData mailData = MailManager.getInstance().parseMailDataContent(mail);
					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "knight mailData.channelId", mailData.channelId);
					if (mailData != null && mailData.isKnightMail()){
						if (configType == DBManager.CONFIG_TYPE_READ && mail.isUnread())
						{
							mail.setStatus(1);
							DBManager.getInstance().updateMail(mail);
						}
						else if (configType == DBManager.CONFIG_TYPE_SAVE)
						{
							if (!isUnLock && !mail.isLock())
							{
								mail.setSave(1);
								DBManager.getInstance().updateMail(mail);
							}
							else if (isUnLock && mail.isLock())
							{
								mail.setSave(0);
								DBManager.getInstance().updateMail(mail);
							}
						}
						else if (configType == DBManager.CONFIG_TYPE_DELETE)
						{
							deleteMail("knight", DBDefinition.CHANNEL_TYPE_OFFICIAL, -1);
						}
						
						
					}
						
				}
			}
			for (int j = 0; j < channel.mailDataList.size(); j++)
			{
				MailData mailData = channel.mailDataList.get(j);
				if (mailData != null)
				{
					if (configType == DBManager.CONFIG_TYPE_READ && mailData.isUnread())
					{
						mailData.setStatus(1);
					}
					else if (configType == DBManager.CONFIG_TYPE_SAVE)
					{
						if (!isUnLock && !mailData.isLock())
						{
							mailData.setSave(1);
						}
						else if (isUnLock && mailData.isLock())
						{
							mailData.setSave(0);
						}
					}
				}
			}
			if (configType == DBManager.CONFIG_TYPE_READ)
			{
				channel.unreadCount = 0;
				DBManager.getInstance().updateChannel(channel);
			}
			ChannelManager.getInstance().calulateAllChannelUnreadNum();

			if (ChatServiceController.hostActivity == null)
				return;
			ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						if (ChatServiceController.getChannelListFragment() != null)
						{
							ChatServiceController.getChannelListFragment().notifyDataSetChanged();
						}
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
			return;
		}

		for (int i = 0; i < mailUidArr.length; i++)
		{
			if (StringUtils.isNotEmpty(mailUidArr[i]))
			{
				MailData mail = DBManager.getInstance().getSysMailByID(mailUidArr[i]);
				if (mail != null)
				{
					channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mail.getChannelId());

					if (configType == DBManager.CONFIG_TYPE_READ && mail.isUnread())
					{
						if (channel != null)
							channel.unreadCount--;
						mail.setStatus(1);
						DBManager.getInstance().updateMail(mail);
					}
					else if (configType == DBManager.CONFIG_TYPE_SAVE)
					{
						if (!isUnLock && !mail.isLock())
						{
							mail.setSave(1);
							DBManager.getInstance().updateMail(mail);
						}
						else if (isUnLock && mail.isLock())
						{
							mail.setSave(0);
							DBManager.getInstance().updateMail(mail);
						}
					}
					else if (configType == DBManager.CONFIG_TYPE_DELETE)
					{
						ChannelManager.getInstance().deleteSysMailFromChannel(channel, mailUidArr[i], true);
						if (!hasDetectMail && (mail.getType() == MailManager.MAIL_DETECT_REPORT||mail.getType() == MailManager.Mail_NEW_SCOUT_REPORT_FB))
							hasDetectMail = true;
						continue;
					}

					if (channel == null || channel.mailDataList == null)
						return;

					for (int j = 0; j < channel.mailDataList.size(); j++)
					{
						MailData mailData = channel.mailDataList.get(j);
						if (mailData != null && mailUidArr[i].equals(mailData.getUid()))
						{
							if (configType == DBManager.CONFIG_TYPE_READ && mailData.isUnread())
							{
								mailData.setStatus(1);
								break;
							}
							else if (configType == DBManager.CONFIG_TYPE_SAVE)
							{
								if (!isUnLock && !mailData.isLock())
								{
									mailData.setSave(1);
								}
								else if (isUnLock && mailData.isLock())
								{
									mailData.setSave(0);
								}
								break;
							}
						}
					}

					if (configType == DBManager.CONFIG_TYPE_READ)
					{
						if(ChatServiceController.mail_all_read) {
							channel.unreadCount = 0;
						}
						channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
						DBManager.getInstance().updateChannel(channel);
					}
				}
			}
		}

		if (hasDetectMail)
			DBManager.getInstance().getDetectMailInfo();

		if (channel != null && channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL && configType == DBManager.CONFIG_TYPE_DELETE)
			channel.querySysMailCountFromDB();
		ChannelManager.getInstance().calulateAllChannelUnreadNum();

		if (ChatServiceController.hostActivity == null)
			return;
		ChatServiceController.hostActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (ChatServiceController.getChannelListFragment() != null)
					{
						ChatServiceController.getChannelListFragment().notifyDataSetChanged();
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public static void setMutiMailStatusByType(int type, int configType, boolean isUnLock)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "mailUids", type, "configType", configType, "isUnLock", isUnLock);

		if (type < 0
				|| !(configType == DBManager.CONFIG_TYPE_READ || configType == DBManager.CONFIG_TYPE_SAVE || configType == DBManager.CONFIG_TYPE_DELETE))
			return;
		String channelId = "";
		if (type == MailManager.MAIL_RESOURCE)
			channelId = MailManager.CHANNELID_RESOURCE;
		else if (type == MailManager.MAIL_RESOURCE_HELP)
			channelId = MailManager.CHANNELID_RESOURCE_HELP;
		else if (type == MailManager.MAIL_ATTACKMONSTER)
			channelId = MailManager.CHANNELID_MONSTER;
		else if (type == MailManager.MAIL_MISSILE)
			channelId = MailManager.CHANNELID_MISSILE;
		else if (type == MailManager.MAIL_GIFT_BUY_EXCHANGE)
			channelId = MailManager.CHANNELID_GIFT;
		else if (type == MsgItem.MAIL_MOD_PERSON)
			channelId = MailManager.CHANNELID_MOD;
		else if (type == 0 || type == 1 || type == MailManager.MAIL_Alliance_ALL)
			channelId = MailManager.CHANNELID_MESSAGE;

		if (StringUtils.isEmpty(channelId))
			return;
		if (channelId.equals(MailManager.CHANNELID_RESOURCE) || channelId.equals(MailManager.CHANNELID_RESOURCE_HELP)
				|| channelId.equals(MailManager.CHANNELID_MONSTER) ||channelId.equals(MailManager.CHANNELID_MISSILE)||channelId.equals(MailManager.CHANNELID_GIFT))
		{
			ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
			if (channel != null)
			{
				List<String> unreadUids = channel.getMailUidArrayByConfigType(configType);
				for (int i = 0; i < unreadUids.size(); i++)
				{
					String uid = unreadUids.get(i);
					if (StringUtils.isNotEmpty(uid))
					{
						MailData mail = DBManager.getInstance().getSysMailByID(uid);
						if (mail != null)
						{
							if (configType == DBManager.CONFIG_TYPE_READ && mail.isUnread())
							{
								mail.setStatus(1);
								DBManager.getInstance().updateMail(mail);
							}
							else if (configType == DBManager.CONFIG_TYPE_SAVE)
							{
								if (!isUnLock && !mail.isLock())
								{
									mail.setSave(1);
									DBManager.getInstance().updateMail(mail);
								}
								else if (isUnLock && mail.isLock())
								{
									mail.setSave(0);
									DBManager.getInstance().updateMail(mail);
								}
							}
						}
					}
				}

				for (int j = 0; j < channel.mailDataList.size(); j++)
				{
					MailData mailData = channel.mailDataList.get(j);
					if (mailData != null)
					{
						if (configType == DBManager.CONFIG_TYPE_READ && mailData.isUnread())
						{
							mailData.setStatus(1);
						}
						else if (configType == DBManager.CONFIG_TYPE_SAVE)
						{
							if (!isUnLock && !mailData.isLock())
							{
								mailData.setSave(1);
							}
							else if (isUnLock && mailData.isLock())
							{
								mailData.setSave(0);
							}
						}
					}
				}

				if (configType == DBManager.CONFIG_TYPE_READ)
				{
					channel.unreadCount = 0;
					channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
					DBManager.getInstance().updateChannel(channel);
					ChannelManager.getInstance().calulateAllChannelUnreadNum();
				}

			}
		}
		else if (channelId.equals(MailManager.CHANNELID_MESSAGE))
		{
			List<ChatChannel> messageChannelArr = ChannelManager.getInstance().getAllMsgChannel();
			if (messageChannelArr != null && messageChannelArr.size() > 0)
			{
				for (int i = 0; i < messageChannelArr.size(); i++)
				{
					ChatChannel messageChannel = messageChannelArr.get(i);
					if (messageChannel != null)
					{
						messageChannel.markAsRead();
					}
				}
			}

			ChatChannel channel = ChannelManager.getInstance().getMessageChannel();
			if (channel != null)
			{
				channel.unreadCount = 0;
				channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
				DBManager.getInstance().updateChannel(channel);
				ChannelManager.getInstance().calulateAllChannelUnreadNum();
			}

		}

		if (ChatServiceController.hostActivity == null)
			return;
		ChatServiceController.hostActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (ChatServiceController.getChannelListFragment() != null)
					{
						ChatServiceController.getChannelListFragment().notifyDataSetChanged();
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public static void postMailUpdate(String updateData)
	{
		if (updateData.equals(""))
			return;
		ChannelManager.mailUpdateData = updateData;
		try
		{
			MailUpdateData updateDate = JSON.parseObject(updateData, MailUpdateData.class);
			ChannelManager.getInstance().updateMailData(updateDate);

		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}

	}

	public static void postMailDeleteStatus(String mailUid)
	{
		if (mailUid.equals(""))
			return;
		ChannelManager.getInstance().deleteMailFrom2dx(mailUid);
	}

	public static String getLastMailUpdateTime()
	{
		String ret = "";
		long latestModifyTime = ChannelManager.getInstance().getLatestSysMailModifyTime();

		if (latestModifyTime > 0)
		{
			ret = Long.toString(latestModifyTime);
		}
		return ret;
	}

	public static void postMailDealStatus(String mailUid)
	{
		if (mailUid.equals(""))
			return;
		ChannelManager.getInstance().dealMailFrom2dx(mailUid);
	}

	public static void postTranslatedResult(String jsonRet)
	{
		if (StringUtils.isNotEmpty(jsonRet))
		{
			try
			{
				TranslatedByLuaResult result = JSON.parseObject(jsonRet, TranslatedByLuaResult.class);
				if (result != null && StringUtils.isNotEmpty(result.getOriginalMsg())
						&& TranslateManager.getInstance().isInTranslateQueue(result.getOriginalMsg()))
				{
					TranslateManager.getInstance().handleTranslateResult(result);
				}
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	public static void postTranslateByLuaStart()
	{
		TranslateManager.isTranslatedByLuaStart = true;
	}

	public static void postUIShow()
	{
		TranslateManager.isUIShow = true;
	}

	public static void setMailSortType(int sortType)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "sortType", sortType);
		ChatServiceController.sortType = sortType;
	}

	public static void setFestivalRedPackageEnable(boolean isFestival)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "isFestival", isFestival);
		ChatServiceController.isFestivalRedPackageEnable = isFestival;
	}

	public static void setChatRoomEnable(boolean isOpen)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "isChatRoomEnable", isOpen);
		ChatServiceController.isChatRoomEnable = isOpen;
	}

	public static boolean isStickMsg(String msg)
	{
		return StickManager.getPredefinedEmoj(msg) != null;
	}

	public static void setDefaultTranslateEnable(boolean isEnable)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "isEnable", isEnable);
		ChatServiceController.isDefaultTranslateEnable = isEnable;
	}

	public static void setFriendEnable(boolean isEnable)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "isEnable", isEnable);
		ChatServiceController.isFriendEnable = isEnable;
	}

	// public static void setDetectInfoEnable(boolean isEnable)
	// {
	// 	LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "isEnable", isEnable);
	// 	ChatServiceController.isDetectInfoEnable = isEnable;
	// }

	public static void setStandaloneServerEnable(int keyIndex, boolean isEnable)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "keyIndex", keyIndex, "isEnable", isEnable);
		switch (keyIndex)
		{
			case 1:
				ConfigManager.useWebSocketServer = isEnable;
				break;
			case 2:
				ConfigManager.isRecieveFromWebSocket = isEnable;
				break;
			case 3:
				ConfigManager.isSendFromWebSocket = isEnable;
				break;
		}
	}

	/**
	 * 设置玩家是否进入了竞技场 isEnterArena:true 进入，反之未进入
	 * @author lzh
	 * @time 17/2/27 下午4:49
	 */
	public static void setIsEnterArena(boolean isArena)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE,  "isArena", isArena);
		ConfigManager.isEnterArena = isArena;
	}

	public static void setIndependentLeague(boolean isIndependentLeague)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE,  "isIndependentLeague", isIndependentLeague);
		ConfigManager.isIndependentLeague = isIndependentLeague;
	}

	public static void setIsShowFrameEff(boolean isShowFrameEff){
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE,  "isShowFrameEff", isShowFrameEff);
		ChatServiceController.isShowFrameEffNativeView = isShowFrameEff;
	}
	public static void setIsBackCloseSocket(boolean close)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE,  "isCloseSocket", close);
		ConfigManager.isBackCloseSocket = close;
	}

	public static void rmDataBaseFile()
	{
		DBManager.getInstance().rmDatabaseFile();
	}

	public static void  clearSysMailFromDB(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				DBManager.getInstance().clearSysMailFromDB();
			}
		}).start();
	}

	public static void getDetectMailByMailUid(String mailUid)
	{
		if (StringUtils.isNotEmpty(mailUid))
		{
			MailData mail = DBManager.getInstance().getSysMailByID(mailUid);
			if (mail != null)
			{
				mail.setNeedParseByForce(true);
				MailData mailData = MailManager.getInstance().parseMailDataContent(mail);
				try
				{
					String jsonStr = JSON.toJSONString(mailData);
					MailManager.getInstance().transportMailInfo(jsonStr, true);
					
					//------------//
					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "mailId", mailUid, "channelId", mail.getChannelId());
					if (mail.isUnread())
					{
						mail.setStatus(1);
						JniController.getInstance().excuteJNIVoidMethod("readMail",
								new Object[] { mailData.getUid(), Integer.valueOf(mailData.getType()) });
						DBManager.getInstance().updateMail(mail);
						
						// 更新channel
						ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mail.getChannelId());
						if (channel == null || channel.mailDataList == null)
							return;
						if (channel.unreadCount > 0)
						{
							channel.unreadCount--;
							ChannelManager.getInstance().calulateAllChannelUnreadNum();
						}
						channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
						DBManager.getInstance().updateChannel(channel);
						for (int i = 0; i < channel.mailDataList.size(); i++)
						{
							MailData mailData1 = channel.mailDataList.get(i);
							if (mailData1 != null && mailUid.equals(mailData1.getUid()))
							{
								if (mailData1.isUnread())
								{
									mailData1.setStatus(1);
								}
								break;
							}
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static void loadMoreMailFromAndroid(String channelId)
	{
		if (StringUtils.isNotEmpty(channelId))
		{
			ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
			if (channel != null && channel.mailDataList != null && channel.mailDataList.size() > 0)
			{
				MailData lastMail = channel.mailDataList.get(channel.mailDataList.size() - 1);
				if (lastMail != null)
				{
					ChannelManager.getInstance().loadMoreSysMailFromDB(channel, lastMail.getCreateTime());
				}
			}
		}
	}

	public static void setChannelPopupOpen(String channelId)
	{
		ChannelManager.currentOpenedChannel = channelId;
	}

	public static void postMutiRewardItem(String jsonStr)
	{
		try
		{
			final FlyMutiRewardInfo flyMutiReward = JSON.parseObject(jsonStr, FlyMutiRewardInfo.class);
			if (flyMutiReward != null)
			{
				if (ChatServiceController.hostActivity == null)
					return;
				ChatServiceController.hostActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							if (ChatServiceController.getChannelListFragment() != null)
							{
								ChatServiceController.getChannelListFragment().showMutiRewardFlyAnimation(flyMutiReward);
							}
						}
						catch (Exception e)
						{
							LogUtil.printException(e);
						}
					}
				});
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static boolean handleRedPackageInfo(String uidStr, int status)
	{
		if(!ChatServiceController.redPackgeMsgGotArray.contains(uidStr) && status == MsgItem.HANDLED)
		{
			ChatServiceController.redPackgeMsgGotArray.add(uidStr);
		}
		Map<String, MsgItem> map = ChannelManager.getInstance().getUnHandleRedPackageMap();
		boolean hasCounteryRedPackage = false;
		boolean hasAllianceRedPackage = false;

		if(map == null)return false;
		if (!map.containsKey(uidStr)) {
			return false;
		}
		MsgItem msgItem = map.get(uidStr);
		if (msgItem != null && msgItem.sendState == MsgItem.UNHANDLE)
		{
			ChatChannel channel = null;
			if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
			{
				channel = ChannelManager.getInstance().getCountryChannel();
				hasCounteryRedPackage = true;
			}
			else if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
			{
				channel = ChannelManager.getInstance().getAllianceChannel();
				hasAllianceRedPackage = true;
			}
			if (channel != null)
			{
				if (msgItem.sendState != status)
				{
					msgItem.sendState = status;
					DBManager.getInstance().updateMessage(msgItem, channel.getChatTable());
				}

				for (int j = 0; j < channel.msgList.size(); j++)
				{
					MsgItem item = channel.msgList.get(j);
					if (item != null && msgItem.attachmentId.equals(item.attachmentId))
					{
						if (item.sendState != status)
						{
							item.sendState = status;
							channel.msgList.set(j, item);
						}
						break;
					}
				}

				if (status != MsgItem.UNHANDLE) {
					map.remove(uidStr);
				}
			}
		}
		return hasCounteryRedPackage | hasAllianceRedPackage;
	}

	public static void postRedPackageGotUids(String redpackageInfo)
	{
		String[] uidArray = redpackageInfo.split(",");
		boolean hasRedPackageChange = false;
		for (int i = 0; i < uidArray.length; i++)
		{
			if (StringUtils.isNotEmpty(uidArray[i]))
			{
				String[] redPackageInfoArr = uidArray[i].split("\\|");
				if (redPackageInfoArr.length == 2)
				{
					hasRedPackageChange = handleRedPackageInfo(redPackageInfoArr[0], Integer.parseInt(redPackageInfoArr[1]));
				}
			}
		}

		//聊天v2
		if(ChatServiceController.chat_v2_on) {
			if (ServiceInterface.getServiceDelegate() != null) {
				ServiceInterface.getServiceDelegate().refreshRedPackageNum();
			}
			return;
		}

		if (ChatServiceController.getChatActivity() == null)
			return;

		ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if(ConfigManager.isRedPackageShakeEnabled) {
						// 刷新红包显示数量
						if(ChatServiceController.getCurrentActivity()!=null)
							ChatServiceController.getChatActivity().refreshRedPackageNum();
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});

		// if (ChatServiceController.hostActivity == null ||
		// !hasRedPackageChange)
		// return;
		// notifyDataSetChangedChatFragment();
	}

	public static void notifyDataSetChangedChatFragment()
	{
		ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (ChatServiceController.getChatFragment() != null) {
						ChatServiceController.getChatFragment().notifyDataSetChanged(ChatServiceController.getCurrentChannelType(), false);
					}
				} catch (Exception e) {
					LogUtil.printException(e);
				}
			}
		});
	}

	public static void postRedPackageStatus(String redPackageUid, int status)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "redPackageUid", redPackageUid, "status", status);

		//聊天v2
		if(ChatServiceController.chat_v2_on) {
			if (ServiceInterface.getServiceDelegate() != null) {
				ServiceInterface.getServiceDelegate().postRedPackageStatus(redPackageUid, status);
			}
			boolean hasChange = handleRedPackageInfo(redPackageUid, status);
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "hasChange", hasChange);
			return;
		}

		MsgItem currentPopupItem = null;// ChatServiceController.getChatFragment().getCurrentRedPackageItem();
		if (ChatServiceController.getChatFragment() != null)
		{
			currentPopupItem = ChatServiceController.getChatFragment().getCurrentRedPackageItem();
		}

		if (StringUtils.isEmpty(redPackageUid) || status <= 0)
		{
			if (ChatServiceController.getChatFragment() != null)
				ChatServiceController.getChatFragment().hideRedPackageConfirm();

			if (currentPopupItem != null) {
				String[] redPackageInfoArr = currentPopupItem.attachmentId.split("\\|");
				ChatServiceController.doHostAction("pickRedPackage", "", currentPopupItem.msg, redPackageInfoArr[0], true);
			}
			return;
		}
		if (ChatServiceController.getChatFragment() != null)
		{
			if (currentPopupItem != null)
			{
				String[] redPackageInfoArr = currentPopupItem.attachmentId.split("\\|");
				if(status == MsgItem.NONE_MONEY && ChatServiceController.redPackgeMsgGotArray.contains(redPackageUid) ){//其他机器上抢过该红包
					currentPopupItem.sendState = MsgItem.HANDLED;
					status = MsgItem.HANDLED;
					ChatServiceController.getChatFragment().hideRedPackageConfirm();
					ChatServiceController.doHostAction("viewRedPackage", "", currentPopupItem.msg, redPackageInfoArr[0], true);
				}else {
					if (redPackageUid.equals(redPackageInfoArr[0]) && currentPopupItem.sendState == MsgItem.UNHANDLE
							&& status != MsgItem.UNHANDLE) {
						currentPopupItem.sendState = status;
						ChatServiceController.getChatFragment().showRedPackageConfirm(currentPopupItem);
					} else {
						ChatServiceController.getChatFragment().hideRedPackageConfirm();
						ChatServiceController.doHostAction("pickRedPackage", "", currentPopupItem.msg, redPackageInfoArr[0], true);
					}
				}
			}
		}
		boolean hasChange = handleRedPackageInfo(redPackageUid, status);
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "hasChange", hasChange);
		// if (ChatServiceController.hostActivity == null || !hasChange)
		// return;
		// notifyDataSetChangedChatFragment();
	}

	public static void postRedPackageDuringTime(int time)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "time", time);
		ChatServiceController.red_package_during_time = time;
	}

	public static void postSystemRedPackageLimitTime(int time)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "sysredtime", time);
		ChatServiceController.system_red_package_Limit_time = time;
	}

	public static String getFriendLatestMails(String uids)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "uids", uids);
		if (StringUtils.isEmpty(uids))
			return "";
		String[] uidArr = uids.split("_");
		List<FriendLatestMail> friendMailList = new ArrayList<FriendLatestMail>();
		for (int i = 0; i < uidArr.length; i++)
		{
			ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_USER, uidArr[i]);
			if (channel != null)
			{
				MsgItem mail = channel.getLatestUserMail();
				if (mail != null)
				{
					String latestMsg = mail.msg;
					if (StringUtils.isNotEmpty(mail.translateMsg))
						latestMsg = mail.translateMsg;
					FriendLatestMail friendMail = new FriendLatestMail(uidArr[i], latestMsg);
					if (friendMail != null)
						friendMailList.add(friendMail);
				}
			}
		}

		try
		{
			String friendMailJson = JSON.toJSONString(friendMailList);
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "friendMailJson", friendMailJson);
			return friendMailJson;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return "";
	}

	public static String getChatLatestMessage()
	{
		ChatChannel countryChannel = ChannelManager.getInstance().getCountryChannel();
		ChatChannel allianceChannel = ChannelManager.getInstance().getAllianceChannel();
		LatestCountryAllianceChatInfo chatInfo = new LatestCountryAllianceChatInfo();
		if (countryChannel != null)
		{
			MsgItem latestCountryMsg = DBManager.getInstance().getChatLatestMsg(countryChannel.getChatTable());
			if (latestCountryMsg != null)
			{
				LatestChatInfo countryInfo = new LatestChatInfo();
				countryInfo.setMsgInfo(latestCountryMsg);
				chatInfo.setLatestCountryChatInfo(countryInfo);
			}
		}
		if (allianceChannel != null)
		{
			MsgItem latestAllianceMsg = DBManager.getInstance().getChatLatestMsg(allianceChannel.getChatTable());
			if (latestAllianceMsg != null)
			{
				LatestChatInfo allianceInfo = new LatestChatInfo();
				allianceInfo.setMsgInfo(latestAllianceMsg);
				chatInfo.setLatestAllianceChatInfo(allianceInfo);
			}
		}

		if(isTopChatRoom()) {
			ChatChannel topChatRoomChannel = ChannelManager.getInstance().getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, ChatServiceController.topChatRoomUid));
			if (topChatRoomChannel != null) {
				MsgItem latestChatRoomMsg = DBManager.getInstance().getChatLatestMsg(topChatRoomChannel.getChatTable());
				if (latestChatRoomMsg != null) {
					LatestChatInfo chatRoomInfo = new LatestChatInfo();
					chatRoomInfo.setMsgInfo(latestChatRoomMsg);
					chatInfo.setLatestTopChatRoomChatInfo(chatRoomInfo);
				}
			}
		}
		String lateChatJson = "";
		try
		{
			lateChatJson = JSON.toJSONString(chatInfo);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return lateChatJson;
	}

	private static void sendChatLatestMessage(ChatChannel channel)
	{
		MsgItem latestMsgItem = DBManager.getInstance().getChatLatestMsg(channel.getChatTable());
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "latestMsgItem", latestMsgItem);
		if (latestMsgItem != null)
		{
			ChatServiceController.getInstance().postLatestChatMessage(latestMsgItem);
		}
	}

	public static void postKingUid(String kingUid)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "kingUid", kingUid);
		ChatServiceController.kingUid = kingUid;
	}

	public static void postBanTime(String banTime)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "banTime", banTime);
		ChatServiceController.banTime = banTime;
	}

	public static void postShieldUids(String shieldUids)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "shieldUids", shieldUids);
		String[] shieldArr = shieldUids.split("_");
		for (int i = 0; i < shieldArr.length; i++)
		{
			if (StringUtils.isNotEmpty(shieldArr[i]))
			{
				UserManager.getInstance().addRestrictUser(shieldArr[i], UserManager.BLOCK_LIST);
			}
		}
	}

	/**
	 * 国家与联盟频道禁言与解除禁言
	 * @author lzh
	 * @time 17/2/8 下午2:24
	 */
	public static void chatBanOrUnBan(String uid,String banGmName,long banTime, int type)
	{
		ChatBanInfo banInfo = new ChatBanInfo();
		banInfo.uid = uid;
		banInfo.banGmName = banGmName;
		banInfo.banTime = banTime;
		if(banTime == 0){
			UserManager.getInstance().removeBanUser(banInfo, type);
		}else{
			UserManager.getInstance().addBanUser(banInfo,type);
		}
	}

	public static void postServerType(int serverType)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "serverType", serverType);
		ChatServiceController.serverType = serverType;
	}

	public static void showAllianceDialog()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE);
		ChatServiceController.needShowAllianceDialog = true;
	}

	public static void postAddedMailListMail(String mailUid)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mailUid", mailUid);
		if (StringUtils.isEmpty(mailUid))
			return;
		String showMailUid = MailManager.getInstance().getShowingMailUid();
		if(StringUtils.isNotEmpty(showMailUid) && showMailUid.equals(mailUid))
		{
			ChatServiceController.doHostAction("showMailPopup", mailUid, "", "", true, true);
			MailManager.getInstance().setShowingMailUid("");
		}
		MailManager.getInstance().addMailInTransportedList(mailUid);
	}

	public static String getNeighborMail(String mailUid, int type)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mailUid", mailUid, "type", type);
		if (StringUtils.isEmpty(mailUid) || !(type == 1 || type == 2))
			return "";
		MailData mail = DBManager.getInstance().getSysMailByID(mailUid);
		if (mail != null)
		{
			if (type == 1)
				return MailManager.getInstance().transportNeiberMailData(mail, true, false);
			else if (type == 2)
				return MailManager.getInstance().transportNeiberMailData(mail, false, true);
		}
		return "";
	}

	public static void postSwitch(String switchKey, String switchValue)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "switchKey", switchKey, "switchValue", switchValue);
		if (StringUtils.isEmpty(switchKey))
			return;
		if (switchKey.equals("chat_k10"))
			ChatServiceController.switch_chat_k10 = switchValue;
		else if (switchKey.equals("chat_k11"))
			ChatServiceController.switch_chat_k11 = switchValue;
		else if(switchKey.equals("chat_bubble_k1"))
		{
			if(StringUtils.isNotEmpty(switchValue))
				ChatServiceController.isNewYearStyleMsg = switchValue.equals("1") ? true:false;
		}
	}

	public static void postSwitch(String switchKey, boolean switchValue) {
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "switchKey", switchKey, "switchValue", switchValue);

		switch (switchKey) {
			case "chat_svip":
                ChatServiceController.isSVIPStyleMsg = switchValue;
				break;
			case "standalone_chat_room":
				ChatServiceController.standalone_chat_room = switchValue;
				break;
			case "chat_send_target":
				ChatServiceController.chat_send_target = switchValue;
				break;
			case "battlefield_chat_room":
				ChatServiceController.battlefield_chat_room = switchValue;
				break;
			case "chat_room_remote_invite":
				ChatServiceController.chat_room_remote_invite = switchValue;
				break;
			case "convenient_contact":
				ChatServiceController.convenient_contact = switchValue;
				break;
			case "mail_all_delete":
				ChatServiceController.mail_all_delete = switchValue;
				break;
			case "chat_msg_independent":
				ChatServiceController.chat_msg_independent = switchValue;
				break;
			case "career_chat":
				ChatServiceController.career_chat = switchValue;
				break;
			case "new_battlemail":
				ChatServiceController.new_battlemail = switchValue;
				break;
			case "new_resourcebattlemail":
				ChatServiceController.new_resourcebattlemail = switchValue;
                break;
			case "front_end_badwords":
				ChatServiceController.front_end_badwords = switchValue;
				break;
			case "monster_mail":
				ChatServiceController.monster_mail = switchValue;
				break;
			case "scoutmail":
				ChatServiceController.scoutmail = switchValue;
				break;
			case "mass_boss":
				ChatServiceController.mass_boss = switchValue;
				break;
			case "new_system_message":
				ChatServiceController.new_system_message = switchValue;
				break;
			case "16_server_chatroomlock":
				ChatServiceController.isWarZoneRoomEnable = switchValue;
				break;
			case "new_chat":	//新版聊天v2 开关
				ChatServiceController.chat_v2_on = switchValue;
				break;
			case "new_chat_phiz":	//Stickers大表情图 开关
				ChatServiceController.chat_v2_stickers_on = switchValue;
				break;
			case "chat_pictures":	//自定义图 开关
				ChatServiceController.chat_pictures_on = switchValue;
				break;
			case "pictures_emoticons":	//图片显示大小限制 开关
				ChatServiceController.chat_pictures_emoticons_on = switchValue;
				break;
			case "language_channel":	//语言聊天室 开关
				ChatServiceController.chat_language_on = switchValue;
				break;

			case "new_personal_chat":	//新版个人聊天 开关
				ChatServiceController.chat_v2_personal = switchValue;
				break;

			case "mail_all_read":	//一键已读所有邮件(包含领取奖励) 开关
				ChatServiceController.mail_all_read = switchValue;
				break;

			case "defence_select_switch":	//破城杀敌
				ChatServiceController.defence_select_switch = switchValue;
				break;

			case "chat_tab":	//新版聊天切换标签页
				ChatServiceController.chat_tab = switchValue;
				break;

		}

	}

	public static void postPlayerLevel(int playerLevel)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "playerLevel", playerLevel);
		ChatServiceController.currentLevel = playerLevel;
	}

	public static void mergeConfig()
	{
		//System.out.println("mergeConfig");
		ConfigManager.getInstance().mergeRemoteDynamicImageMap();
	}
	
	public static void initXiaoMiSDK(String appId, String appKey,String pid, String pkey,String guid, String b2token)
	{
		String temp = "appId="+appId+"appKey= "+appKey+"pid= "+pid+"pkey= "+pkey+"guid= "+guid+"b2token= "+b2token;
		Log.d("xiaomi", "xiaomi initXiaoMiSDK= " +temp);
		XiaoMiToolManager.initActivity(ChatServiceController.getInstance().hostActivity,appId,appKey,pid,pkey,guid,b2token);
		Log.d("xiaomi", "xiaomi initXiaoMiSDK init end");
	}
	
	public static void xiaomistartRecord()
	{
		Log.d("xiaomi", "xiaomi xiaomistartRecord");
		XiaoMiToolManager.getInstance().startRecord();
	}
	
	public static void onGetMultiUserInfoActualCalled()
	{
		UserManager.getInstance().onServerActualCalled();
	}
	public static void xiaomistopRecord()
	{
		Log.d("xiaomi", "xiaomi xiaomistopRecord");
		XiaoMiToolManager.getInstance().stopRecord();
	}
	
	public static void xiaomisendAudio()
	{
		Log.d("xiaomi", "xiaomi xiaomisendVideo");
		XiaoMiToolManager.getInstance().sendAudio();
	}
	
	public static void notifyWebSocketEventType(final int type)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "type", type);
		if (ConfigManager.websocket_network_state != type)
		{
			ConfigManager.websocket_network_state = type;
			if(ChatServiceController.getCurrentActivity()!=null && (
					ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY 
					|| ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_ALLIANCE))
			{
				ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							if(ChatServiceController.getCurrentActivity()!=null)
								ChatServiceController.getCurrentActivity().refreshNetWorkState();
						}
						catch (Exception e)
						{
							LogUtil.printException(e);
						}
					}
				});
			}
				
		}
		
	}

	public static void savePngToAlbum(final String filePath)
	{
		if (ChatServiceController.getInstance().hostActivity == null)
		{
			return;
		}
		ChatServiceController.getInstance().hostActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Bitmap ret = BitmapFactory.decodeFile(filePath);
					if (ret != null)
					{
						FileVideoUtils.saveImageToAlbum(ChatServiceController.getInstance().hostActivity, ret);
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public static int getPhotoRight()
	{
		boolean flag = PermissionManager.isExternalStoragePermissionsAvaiable(ChatServiceController.getInstance().hostActivity);
		if (flag)
		{
			return 0;
		}
		PermissionManager.getExternalStoragePermissionForPNG();
		int right = 4;
		return right;
	}

	public static void showWriteMailFrom2dx(int consumeGold,int remainNum)
	{
		final int consumeGoldf = consumeGold;
		final int remainNumf = remainNum;
		final boolean isFromCpp = true;
		if (ChatServiceController.hostActivity != null)
		{
			ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						ServiceInterface.showPresidentWriteMailActivity(ChatServiceController.hostActivity, false, consumeGoldf, remainNumf,isFromCpp);
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}
	}

	public static int getSaveMailCount(){
		int count = 0;
		try {
			count = DBManager.getInstance().getSaveMailCountInDB();
		}catch (Exception e){
			LogUtil.printException(e);
		}
		return count;
	}

	public static void removeExceptChatRoom(final String chatRooms){
		String[] uidArr = chatRooms.split("#");

		// 有时还没有读出内容，导致不能删除掉数据库里缓存的聊天室，调用一下getAllMailChannel方法
		ChannelManager.getInstance().getAllMailChannel();
		List<ChatChannel> channelList = ChannelManager.getInstance().getAllChatRoomChannel();
		Iterator<ChatChannel> it = channelList.iterator();
		while(it.hasNext()){
			ChatChannel channel = it.next();
			boolean isAdd = true;
			for (int i=0; i<uidArr.length; i++) {
				if(channel.channelID.equals(uidArr[i])){
					isAdd = false;
					break;
				}
			}

			if(isAdd){
				it.remove();
//				ChannelManager.getInstance().deleteChatroomChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, channel.channelID));
				ChannelManager.getInstance().deleteChannel(channel);
			}
		}
	}


	public static boolean isTopChatRoom(){
		return ChatServiceController.getInstance().isTopChatRoom();
	}

	public static void initChatAndMail() {
		//聊天室初始化
		String comandStr = ServiceInterface.getChannelInfo();
		String updateTime = ServiceInterface.getLastMailUpdateTime();
		JniController.getInstance().excuteJNIVoidMethod("getRoomMailFromServer", new Object[]{comandStr, updateTime});

		//个人邮件初始化
		String personMailInfo = ChannelManager.getInstance().getLatestPersonMailInfo();
		if (StringUtils.isNotEmpty(personMailInfo)) {
			String[] personMailInfoArr = personMailInfo.split("\\|");
			String p_mailUid = personMailInfoArr[0];
			long p_time = Long.parseLong(personMailInfoArr[1]) * 1000;
			int p_count = Integer.parseInt(personMailInfoArr[2]);
			JniController.getInstance().excuteJNIVoidMethod("getNewPersonMailFromServer", new Object[]{p_mailUid, String.valueOf(p_time), p_count});
		}

		//系统邮件初始化
		String sysMailInfo = ChannelManager.getInstance().getLatestSystemMailInfo();
		if (StringUtils.isNotEmpty(sysMailInfo)) {
			String[] sysMailInfoArr = sysMailInfo.split("\\|");
			String s_mailUid = sysMailInfoArr[0];
			long s_time = Long.parseLong(sysMailInfoArr[1]) * 1000;
			int s_count = Integer.parseInt(sysMailInfoArr[2]);
			JniController.getInstance().excuteJNIVoidMethod("getNewMailFromServer", new Object[]{s_mailUid, String.valueOf(s_time), s_count});
		}
	}

	public static String getCurrentTimeMillis()
	{
        return Long.toString(System.currentTimeMillis());
	}

	//加入直播聊天室
	public static void joinLiveRoom(String roomId,String roomName){

		WebSocketManager.getInstance().joinLiveRoom(roomId);
//		WebSocketManager.getInstance().chatRoomChangeName(roomId,"nihao");
	}

	public static void showChatActivityFrom2dxForBC(int maxHornInputCount, final int chatType, int sendInterval, final boolean rememberPosition,
											   boolean enableCustomHeadImg, boolean isNoticeItemUsed,String params)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "chatType", chatType, "sendInterval", sendInterval,
				"rememberPosition", rememberPosition, "enableCustomHeadImg", enableCustomHeadImg, "isNoticeItemUsed", isNoticeItemUsed);
		ConfigManager.maxHornInputLength = maxHornInputCount;
		ConfigManager.enableCustomHeadImg = enableCustomHeadImg;
		ChatServiceController.isHornItemUsed = isNoticeItemUsed;
		ConfigManager.sendInterval = sendInterval * 1000;
		ChatServiceController.isCreateChatRoom = false;
		final int chatType1 = 3;
		ChatServiceController.isFromBd = true;
		ChatServiceController.isInLiveRoom = false;
		String roomId = "";
		try{

			JSONObject obj = new JSONObject(params);
			ChatServiceController.liveUid = obj.getString("gameUid");
			roomId = obj.getString("chatRoomId");
			ChatServiceController.liveTipContent = obj.getString("roomContent");
			ChatServiceController.liveRoomName = obj.getString("roomName");
			ChatServiceController.livePushStatus = obj.getString("pushStatus").equals("1") ? true:false;
			ChatServiceController.livePullStatus = obj.getString("pullStatus").equals("1") ? true:false;

		}catch (Exception e){
			LogUtil.printException(e);
		}
		if(!ChatServiceController.curLiveRoomId.equals(roomId) && roomId!=""){
			//先离开上一个语音直播聊天室
			ChatChannel channel = ChannelManager.getInstance().getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, UserManager.getInstance().getCurrentMail().opponentUid));
			if (channel == null) {
				return;
			}

			if(ChatServiceController.getInstance().standalone_chat_room && StringUtils.isNotEmpty(ChatServiceController.curLiveRoomId)
					){
				WebSocketManager.getInstance().chatRoomQuit(ChatServiceController.curLiveRoomId);
			}else{
				JniController.getInstance().excuteJNIVoidMethod("quitChatRoom",
						new Object[] { ChatServiceController.curLiveRoomId });
			}

			ChatServiceController.curLiveRoomId = roomId;
			ServiceInterface.joinLiveRoom(ChatServiceController.curLiveRoomId,ChatServiceController.curLiveRoomId);
		}
		if (ChatServiceController.hostActivity != null)
		{
			ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						if(chatType1 == DBDefinition.CHANNEL_TYPE_CHATROOM && ChatServiceController.curLiveRoomId != "") {

							ChatChannel channel = ChannelManager.getInstance().getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, ChatServiceController.curLiveRoomId));
							if (channel != null) {
								ServiceInterface.setMailInfo(channel.channelID, channel.latestId, channel.getCustomName(), chatType1);
							}
						}

						//聊天v2
						boolean isUserChatView = chatType != DBDefinition.CHANNEL_TYPE_USER || (ChatServiceController.chat_v2_personal && chatType == DBDefinition.CHANNEL_TYPE_USER);
						if (ChatServiceController.chat_v2_on && isUserChatView){
							ServiceInterface.showNewChatActivity(ChatServiceController.hostActivity, chatType1, rememberPosition);
						}
						else
						{
							ServiceInterface.showChatActivity(ChatServiceController.hostActivity, chatType1, rememberPosition);
						}
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}
	}

	public static void  refreshChatActivityInfoFrom2dxForBC(String params){
		JSONObject obj = null;
		try {
			obj = new JSONObject(params);
			if(StringUtils.isNotEmpty(obj.getString("gameUid")))
				ChatServiceController.liveUid = obj.getString("gameUid");
			if(StringUtils.isNotEmpty(obj.getString("roomContent")))
				ChatServiceController.liveTipContent = obj.getString("roomContent");
			if(StringUtils.isNotEmpty(obj.getString("roomName")))
				ChatServiceController.liveRoomName = obj.getString("roomName");
			ChatServiceController.livePushStatus = obj.getString("pushStatus").equals("1") ? true:false;
			ChatServiceController.livePullStatus = obj.getString("pullStatus").equals("1") ? true:false;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if(ChatServiceController.getCurrentActivity() != null){
			ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(ChatServiceController.getChatFragment() instanceof ChatFragment)
					ChatServiceController.getChatFragment().refreshLiveView();
				}
			});

		}
	}

	public static String getUserPicUrl(String uid){
		UserManager.checkUser(uid, "", 0);
		UserInfo userInfo = UserManager.getInstance().getUser(uid);
		if(userInfo != null && userInfo.isCustomHeadImage()){
			return userInfo.getCustomHeadPicUrl();
		}
		return "";
	}

	public static String getUserPropertyByKey(String uid,String key){
		UserManager.checkUser(uid, "", 0);
		UserInfo userInfo = UserManager.getInstance().getUser(uid);
		String result = "";
		if(userInfo != null){
			switch (key){
				case "userName":
					result = userInfo.userName;
					break;
				case "careerName":
					result = userInfo.careerName;
					break;
				case "allianceId":
					result = userInfo.allianceId;
					break;
				case "asn":
					result = userInfo.asn;
					break;
				case "allianceRank":
					result = String.valueOf(userInfo.allianceRank);
					break;
				case "headPic":
					result = userInfo.headPic;
					break;
				case "headPicVer":
					result = String.valueOf(userInfo.headPicVer);
					break;
				case "mGmod":
					result = String.valueOf(userInfo.mGmod);
					break;
				case "vipLevel":
					result = String.valueOf(userInfo.vipLevel);
					break;
				case "svipLevel":
					result = String.valueOf(userInfo.svipLevel);
					break;
                case "vipframe":
                    result = String.valueOf(userInfo.vipframe);
                    break;
				case "lang":
					result = userInfo.lang;
					break;
				default:
					break;
			}
		}
		if(result != null){
			return result;
		}
		return "";
	}


	public static void setWarZoneRoomKey(String roomKey){
		ChatServiceController.m_roomGroupKey = roomKey;

	}

	/**
	 * @see add at 20171102 关闭所有打开并存储在栈中的activity
	 * 因为该栈倒叙排列，需要倒序关闭；
	 */
	public static void activityStackExit(){
		int size = activityStack.size();
		for (int i = size; i > 0; i--) {
			ChannelListFragment.preventSecondChannelId = true;
			MyActionBarActivity activity = activityStack.get(i - 1);
			activity.exitActivity();
		}
	}

	public static String getContactUids(boolean isPresident){
		if(!isPresident){
			return "";
		}
		ConcurrentHashMap<String, ChatChannel> map = ChannelManager.getInstance().getChannelMapAll();
		StringBuilder uids = new StringBuilder();
		for (Map.Entry<String, ChatChannel> entry : map.entrySet()) {
			ChatChannel value = entry.getValue();
				if (value != null && value.channelType == DBDefinition.CHANNEL_TYPE_USER && !value.channelID.equals(UserManager.getInstance().getCurrentUserId())) {
					uids = uids.append(value.channelID);
					uids = uids.append("_");
				}
		}
		if(uids.length()-1 > 0) {
			uids = uids.deleteCharAt(uids.length() - 1);
		}
		return uids.toString();
	}
}
