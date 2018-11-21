package com.chatsdk.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Point;
import android.util.Log;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.model.db.ChatTable;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.db.DBHelper;
import com.chatsdk.model.db.DBManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.model.mail.battle.BattleMailContents;
import com.chatsdk.model.mail.battle.BattleMailData;
import com.chatsdk.model.mail.gift.GiftMailContents;
import com.chatsdk.model.mail.gift.GiftMailData;
import com.chatsdk.model.mail.missile.MissileMailContents;
import com.chatsdk.model.mail.missile.MissileMailData;
import com.chatsdk.model.mail.monster.MonsterMailContents;
import com.chatsdk.model.mail.monster.MonsterMailData;
import com.chatsdk.model.mail.resouce.ResourceMailContents;
import com.chatsdk.model.mail.resouce.ResourceMailData;
import com.chatsdk.model.mail.resourcehelp.ResourceHelpMailContents;
import com.chatsdk.model.mail.resourcehelp.ResourceHelpMailData;
import com.chatsdk.net.WebSocketManager;
import com.chatsdk.util.FilterWordsManager;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.SortUtil;
import com.chatsdk.view.ChannelListFragment;
import com.chatsdk.view.ChatFragment;

public class ChatChannel extends ChannelListItem implements Serializable
{
	private static final long	serialVersionUID		= -4351092186878517042L;

	public int					channelType				= -1;
	public String				channelID;                        //
	public int					dbMinSeqId				= -1;
	public int					dbMaxSeqId				= -1;
	/** 聊天室成员uid列表 */
	public ArrayList<String>	memberUidArray			= new ArrayList<String>();
	/** 聊天室房主 */
	public String				roomOwner;
	/** 是否是聊天室成员 */
	public boolean				isMember				= false;
	/** 聊天室自定义名称 */
	public String				customName				= "";
	/** 最近消息时间 */
	public long					latestTime				= -1;
	/** 最近修改时间，仅针对系统邮件 */
	public long					latestModifyTime		= -1;
	/** 最近消息的id（邮件专用） */
	public String				latestId				= "0";
	/** 聊天室设置 */
	public String				settings;

	// 运行时属性
	/** 消息对象List，保存所有消息 */
	public ArrayList<MsgItem>	msgList					= new ArrayList<MsgItem>();
	/** 正在发送的消息 */
	public ArrayList<MsgItem>	sendingMsgList			= new ArrayList<MsgItem>();
	/** 是否获取到消息过 */
	public boolean				hasRequestDataBefore	= false;
	/** 是否没有更多消息了 */
	public boolean				noMoreDataFlag			= false;
	private int					sysMailCountInDB		= 0;
	private int					sysUnreadMailCountInDB		= 0;
	public Point				lastPosition			= new Point(-1, -1);

	public int					serverMinSeqId;
	public int					serverMaxSeqId;

	public long					serverMaxTime;
	public long					serverMinTime;
	/** 连ws后台时，登陆后从history.roomsv2接口加载到的新消息数量 **/
	public int					wsNewMsgCount;

	/** 收取前db的最大id */
	public int					prevDBMaxSeqId;
	/** 是否正在批量加载新消息 */
	public boolean				isLoadingAllNew			= false;
	/** 是否已经批量加载过新消息 */
	public boolean				hasLoadingAllNew		= false;
	public int					firstNewMsgSeqId;
	/** 最近的一条邮件信息 */
	public MailData				latestMailData			= null;
	public boolean				isMemberUidChanged		= false;

	// 显示属性
	public String				nameText				= "";
	public String				contentText				= "";
	public String				channelIcon				= "";
	public UserInfo				channelShowUserInfo		= null;
	public String				timeText				= "";
	public boolean				usePersonalPic			= false;
	public MsgItem				showItem				= null;
	private ChannelView			channelView				= null;
	public List<String>			mailUidList				= new ArrayList<String>();

	/** 系统邮件的邮件对象 */
	public List<MailData>		mailDataList			= new ArrayList<MailData>();


	/** 系统邮件的邮件对象 */
	public List<MailData>		allmailDataList			= new ArrayList<MailData>();

	public int 					latestLoadedMailCreateTime		= -1;
	private List<Integer>		msgTimeIndexArray		= null;
	
	private boolean 			calculateSysMailCountInDB = false;
	private boolean 			calculateUnreadSysMailCountInDB = false;

	public ChatChannel()
	{
	}

	public String getSetting(){
		return settings;
	}

	public List<MailData>  getMailDataList()
	{
		Log.d("getMailDataList() ", "pro_mailDataList = " + mailDataList.size());
		boolean isExist = false;//存在过期未读取的邮件 需要更新气泡
		Iterator<MailData> it = mailDataList.iterator();
		while (it.hasNext()) {
			MailData mail = it.next();
			if(mail.isFailMail()) {
				if (mail.getStatus()==0){
					Log.d("getMailDataList() ", "Status = " + mail.getStatus());
					isExist = true;
					unreadCount--;
				}
				it.remove();
			}
		}

		if (isExist){
			Log.d("getMailDataList() ", "isExist = " + isExist);
			ChannelManager.getInstance().calulateAllChannelUnreadNum();
		}
		Log.d("getMailDataList() ", "after_mailDataList = " + mailDataList.size());
		return mailDataList;
	}

	public List<MailData>  getAllMailDataList()
	{
		Iterator<MailData> it = allmailDataList.iterator();
		while (it.hasNext()) {
			MailData mail = it.next();
			if(mail.isFailMail())
			{
				it.remove();
			}
		}

		return allmailDataList;
	}


	/**
	 * 显示着的最新sequenceId，与server、数据库的max一样
	 */
	public int getViewMaxId()
	{
		int result = 0;
		for (int i = 0; i < msgList.size(); i++)
		{
			result = msgList.get(i).sequenceId > result ? msgList.get(i).sequenceId : result;
		}
		return result;
	}

	/**
	 * 显示着的最老sequenceId
	 */
	public int getViewMinId()
	{
		int result = msgList.size() > 0 ? msgList.get(0).sequenceId : 0;
		for (int i = 0; i < msgList.size(); i++)
		{
			result = msgList.get(i).sequenceId < result ? msgList.get(i).sequenceId : result;
		}
		return result;
	}

	/**
	 * DB中的最新sequenceId
	 */
	public int getDBMaxId()
	{
		return DBManager.getInstance().getMaxDBSeqId(getChatTable());
	}

	/**
	 * DB中的最新消息ID（邮件专用）
	 */
	public String getDBLatestId()
	{
		return DBManager.getInstance().getLatestId(getChatTable());
	}

	/**
	 * DB中的最新sequenceId
	 */
	public int getDBMinId()
	{
		return DBManager.getInstance().getMinDBSeqId(getChatTable());
	}

	/**
	 * 能否显示新消息数量提示
	 */
	public boolean canLoadAllNew()
	{
		return getNewMsgCount() > ChannelManager.LOAD_ALL_MORE_MIN_COUNT && getNewMsgActualCount() > 0 && !isNotInitedInDB()
				&& !isLoadingAllNew && !hasLoadingAllNew;
	}

	/**
	 * channel表的seqId字段尚未被初始化
	 */
	public boolean isNotInitedInDB()
	{
		return prevDBMaxSeqId <= 0;
	}

	/**
	 * 服务器有而本地没有的最早id
	 */

	//

	/**
	 * 未收取的新消息的最小id
	 */
	public int getNewMsgMinSeqId()
	{
		if (isNotInitedInDB())
			return serverMaxSeqId;

		return serverMinSeqId > prevDBMaxSeqId ? serverMinSeqId : (prevDBMaxSeqId + 1);
	}

	/**
	 * 未收取的新消息的最大id
	 */
	public int getNewMsgMaxSeqId()
	{
		if (getChannelView() != null)
		{
			return getChannelView().chatChannel.getMinSeqId() - 1;
		}
		else
		{
			return 0;
		}
	}

	/**
	 * 收取前尚未加载的新消息数（可能会因为serverMaxSeqId而变化？）
	 */
	public int getNewMsgCount()
	{
		return serverMaxSeqId - getNewMsgMinSeqId() + 1;
	}

	/**
	 * 当前尚未加载的新消息数（除去已加载的）
	 */
	public int getNewMsgActualCount()
	{
		return getNewMsgMaxSeqId() - getNewMsgMinSeqId() + 1;
	}

	/**
	 * 找到指定section在服务器中的交集数量
	 */
	public int getServerSectionCount(int upperId, int lowerId)
	{
		if (serverMinSeqId == -1 && serverMaxSeqId == -1)
		{
			return 0;
		}
		int minId = Math.min(upperId, lowerId);
		int maxId = Math.max(upperId, lowerId);
		int upper = Math.min(maxId, serverMaxSeqId);
		int lower = Math.max(minId, serverMinSeqId);
		return upper - lower + 1;
	}

	public ChatTable getChatTable()
	{
		return ChatTable.createChatTable(channelType, channelID);
	}

	public void setChannelView(ChannelView v)
	{
		channelView = v;
	}

	public ChannelView getChannelView()
	{
		return channelView;
	}

	public static String getMembersString(ArrayList<String> members)
	{
		String uidsStr = "";
		if (members == null)
			return uidsStr;

		for (int i = 0; i < members.size(); i++)
		{
			try
			{
				uidsStr += (i > 0 ? "|" : "") + members.get(i);
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
		return uidsStr;
	}

	public void setMember(boolean isMember)
	{
		this.isMember = isMember;
	}

	public boolean isMember()
	{
		return isMember;
	}

	public boolean getNoMoreDataFlag(int index)
	{
		return serverMinSeqId <= getViewMinId();
	}

	public boolean containCurrentUser()
	{
		// 已经退出的国家
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY && !channelID.equals(UserManager.getInstance().getCurrentUser().serverId + ""))
		{
			return false;
		}
		// 已经退出的联盟
		if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE && !channelID.equals(UserManager.getInstance().getCurrentUser().allianceId))
		{
			return false;
		}
		// 已经退出的聊天室
		if (channelType == DBDefinition.CHANNEL_TYPE_CHATROOM && !channelID.contains("warzone_") && !isMember)
		{
			return false;
		}
		return true;
	}

	private boolean isInMailDataList(MailData mailData)
	{
		if (mailUidList != null && mailUidList.contains(mailData.getUid()))
			return true;
		return false;
	}

	public void addNewMailData(MailData mailData)
	{
		if (!mailData.isUserMail())
		{
			if (!isInMailDataList(mailData))
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW,"mailData.channelId",mailData.channelId);
				mailDataList.add(mailData);
				if (mailUidList != null)
					mailUidList.add(mailData.getUid());
				mailData.channel = this;
				ChannelListFragment.onMailDataAdded(mailData);
				if(mailData.channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM) {
					ChatFragment.onMailDataAdded(mailData);
				}
				refreshRenderData();
				SortUtil.getInstance().refreshNewMailListOrder(mailDataList);
			}
		}
	}	
	
	public void addNewMailData(MailData mailData,boolean update)
	{
		 LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "addNewMailData_uid", mailData.channelId, "addNewMailData_update", update);
		if (mailData.isUserMail()) return;
		String mailUid=mailData.getUid();
		if (!isInMailDataList(mailData))
		{
			addNewMailData(mailData);
		}else{
			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mailDataTM = mailDataList.get(i);
				if (mailDataTM == null)
					continue;
				if(mailDataTM.getUid().equals(mailUid))
				{
					mailDataList.remove(i);
					break;
				}
			}
			for (int i = 0; i < mailUidList.size(); i++)
			{
				String uid = mailUidList.get(i);
				if(uid.equals(mailUid))
				{
					mailUidList.remove(i);
					break;
				}
			}
			addNewMailData(mailData);
		}
	}

	private boolean isInMsgList(MsgItem msg)
	{
		for (int i = 0; i < msgList.size(); i++)
		{
			if (msgList.get(i).msg.equals(msg.msg) && msgList.get(i).createTime == msg.createTime)
			{
				return true;
			}
		}
		return false;
	}

	private boolean isInUserMailList(MsgItem msg)
	{
		if (msg != null && StringUtils.isNotEmpty(msg.mailId))
		{
			for (int i = 0; i < msgList.size(); i++)
			{
				if (StringUtils.isNotEmpty(msgList.get(i).mailId) && msgList.get(i).mailId.equals(msg.mailId))
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean addHistoryMsg(MsgItem msg ,boolean isNewMsg)
	{
		if (!isMsgExist(msg) && !isMsgIgnored(msg) && !UserManager.getInstance().isInRestrictList(msg.uid, UserManager.BLOCK_LIST))
		{
			if (msg.channelType != DBDefinition.CHANNEL_TYPE_USER && firstNewMsgSeqId > 0 && firstNewMsgSeqId == msg.sequenceId)
			{
				if (this.getNewMsgCount() < ChannelManager.LOAD_ALL_MORE_MAX_COUNT)
				{
					msg.firstNewMsgState = 1;
				}
				else
				{
					msg.firstNewMsgState = 2;
				}
			}

			addMsg(msg,isNewMsg);
			return true;
		}
		return false;
	}

	private boolean isMsgExist(MsgItem msg)
	{
		if (msg.channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			return isInUserMailList(msg);
		}
		else
		{
			return isInMsgList(msg);
		}
	}

	private boolean isMsgIgnored(MsgItem msg)
	{
		if (msg.channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			return false;
		}
		else
		{
			return !WebSocketManager.isWebSocketEnabled() && msg.sequenceId == -1;
		}
	}

	public void addMsg(MsgItem msg,boolean isNewMsg)
	{
		msg.initNullField();
		addMsgAndSort(msg,isNewMsg);
		initMsg(msg);
	}

	/**
	 * 由于后台返回的createTime与前台不一样（通常慢几秒），不能按时间排序插入，否则可能新发的消息会插到前面
	 */
	public void addDummyMsg(MsgItem msg)
	{
		msgList.add(msg);
		initMsg(msg);
	}

	/**
	 * 由于后台返回的createTime与前台不一样（通常慢几秒），不能按时间排序插入，否则会错乱
	 */
	public void replaceDummyMsg(final MsgItem msg, int index)
	{
		final int posIndex = index;
		if (ChatServiceController.isNeedReplaceBadWords()) {

			if(StringUtils.isNotEmpty(msg.shareComment) && msg.isShareCommentMsg()){
				msg.shareComment = FilterWordsManager.replaceSensitiveWord(msg.shareComment,1,"*");
			}

			if (!msg.isMsgBadReplace && !msg.isSystemMessage()) {
				msg.msg = FilterWordsManager.replaceSensitiveWord(msg.msg, 1, "*");
				msg.isMsgBadReplace = true;
			} else if (!msg.isTransMsgBadReplace && !msg.isSystemMessage()) {
				msg.translateMsg = FilterWordsManager.replaceSensitiveWord(msg.translateMsg, 1, "*");
				msg.isTransMsgBadReplace = true;
			}
		}
		if(!isMsgExist(msg)) {
			msgList.set(posIndex, msg);
		}
		ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					msgList.set(posIndex, msg);
					if (ChatServiceController.getChatFragment() != null) {
						ChatServiceController.getChatFragment().notifyDataSetChanged();
						ChatServiceController.getChatFragment().notifyDataSetChanged(channelType, true);
						ChatServiceController.getChatFragment().updateListPositionForNewMsg(channelType, msg.isSelfMsg, msg.post);
					}
				} catch (Exception e) {
					LogUtil.printException(e);
				}
			}
		});
		initMsg(msg);
	}

	private void initMsg(MsgItem msg)
	{
		msg.chatChannel = this;
		refreshRenderData();
	}

	private void addMsgAndSort(final MsgItem msg,final boolean isNewMsg)
	{
		int pos = 0;
		for (int i = 0; i < msgList.size(); i++)
		{
			if (msg.createTime > msgList.get(i).createTime
					|| (msg.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM && msg.createTime == msgList.get(i).createTime && msg.sequenceId > msgList
							.get(i).sequenceId))
			{
				pos = i + 1;
			}
			else
			{
				break;
			}
		}
        final int posIndex = pos;
		if(ChatServiceController.isNeedReplaceBadWords()) {
			if(StringUtils.isNotEmpty(msg.shareComment) && msg.isShareCommentMsg()){
				msg.shareComment = FilterWordsManager.replaceSensitiveWord(msg.shareComment,1,"*");
			}
			if (!msg.isMsgBadReplace && !msg.isSystemMessage()) {
				msg.msg = FilterWordsManager.replaceSensitiveWord(msg.msg,1,"*");
				msg.isMsgBadReplace = true;
			} else if (!msg.isTransMsgBadReplace && !msg.isSystemMessage()) {
				msg.translateMsg = FilterWordsManager.replaceSensitiveWord(msg.translateMsg,1,"*");
				msg.isTransMsgBadReplace = true;
			}
		}
		if(!isMsgExist(msg)) {
			msgList.add(posIndex, msg);
		}
		ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {

					if (ChatServiceController.getChatFragment() != null) {
						if(isNewMsg) {
							ChatServiceController.getChatFragment().notifyDataSetChanged();
							ChatServiceController.getChatFragment().notifyDataSetChanged(channelType, true);
							ChatServiceController.getChatFragment().updateListPositionForNewMsg(channelType, msg.isSelfMsg, msg.post);
						}else{

						}
					}
				} catch (Exception e) {
					LogUtil.printException(e);
				}
			}
		});

	}

	public void addNewMsg(MsgItem msg,boolean isNewMsg)
	{
		if (!isMsgExist(msg) && !isMsgIgnored(msg) && !UserManager.getInstance().isInRestrictList(msg.uid, UserManager.BLOCK_LIST))
		{
			addMsg(msg, isNewMsg);
			if (isModChannel())
			{
				ChannelManager.getInstance().latestModChannelMsg = msg.msg;
				ChatChannel modChannel = ChannelManager.getInstance().getModChannel();
				if (modChannel != null)
					modChannel.unreadCount++;
			}
			else if (isMessageChannel())
			{
				ChannelManager.getInstance().latestMessageChannelMsg = msg.msg;
				ChatChannel messageChannel = ChannelManager.getInstance().getMessageChannel();
				if (messageChannel != null)
					messageChannel.unreadCount++;
			}
		}
	}

	public void clearFirstNewMsg()
	{
		if (WebSocketManager.isRecieveFromWebSocket(channelType) && wsNewMsgCount > ChannelManager.LOAD_ALL_MORE_MIN_COUNT)
		{
			return;
		}

		firstNewMsgSeqId = 0;
		for (int i = 0; i < msgList.size(); i++)
		{
			msgList.get(i).firstNewMsgState = 0;
		}
	}

	public boolean hasReward()
	{
		for (Iterator<MailData> iterator = mailDataList.iterator(); iterator.hasNext();)
		{
			MailData mailData = (MailData) iterator.next();
			if (mailData.hasReward())
				return true;
		}
		return false;
	}

	public List<String> getChannelRewardUidArray()
	{
		List<String> rewardUidArray = new ArrayList<String>();
		if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if (mailDataList != null && mailDataList.size() > 0)
			{
				for (int i = 0; i < mailDataList.size(); i++)
				{
					MailData mailData = mailDataList.get(i);
					if (mailData.hasReward() && StringUtils.isNotEmpty(mailData.getUid()) && !rewardUidArray.contains(mailData.getUid()))
					{
						rewardUidArray.add(mailData.getUid());
					}
				}
			}
			//channelz在未进入详细列表时maildatalist为空
			if(mailDataList.size() == 0){
				List<String> mailUids = getMailUidArrayByConfigType(DBManager.CONFIG_TYPE_REWARD);
				rewardUidArray.addAll(mailUids);
			}
		}
		return rewardUidArray;
	}




	public List<String> getChannelDeleteUidArray()
	{
		List<String> deleteUidArray = new ArrayList<String>();
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
			return deleteUidArray;
		if (channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			if (StringUtils.isNotEmpty(channelID)
					&& (channelID.equals(MailManager.CHANNELID_MOD) || channelID.equals(MailManager.CHANNELID_MESSAGE))
					&& StringUtils.isNotEmpty(latestId))
			{
				deleteUidArray.add(latestId);
			}
			else
			{
				if (msgList != null && msgList.size() > 0)
				{
					MsgItem lastItem = msgList.get(0);
					for (int i = 1; i < msgList.size(); i++)
					{
						MsgItem item = msgList.get(i);
						if (item.createTime > lastItem.createTime)
							lastItem = item;
					}
					deleteUidArray.add(lastItem.mailId);
				}
			}
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if (mailDataList != null && mailDataList.size() > 0)
			{
				for (int i = 0; i < mailDataList.size(); i++)
				{
					MailData mailData = mailDataList.get(i);
					if (mailData.canDelete() && !mailData.getUid().equals("") && !deleteUidArray.contains(mailData.getUid()))
					{
						deleteUidArray.add(mailData.getUid());
					}
				}
			}
		}
		return deleteUidArray;
	}

	public boolean cannotOperatedForMuti(int type)
	{
		boolean ret = false;
		if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if (mailDataList != null && mailDataList.size() > 0)
			{
				for (int i = 0; i < mailDataList.size(); i++)
				{
					MailData mailData = mailDataList.get(i);
					if ((type == ChannelManager.OPERATION_DELETE_MUTI && !mailData.canDelete())
							|| (type == ChannelManager.OPERATION_REWARD_MUTI && mailData.hasReward()))
					{
						ret = true;
						break;
					}
				}
			}
		}
		if (type == ChannelManager.OPERATION_REWARD_MUTI)
			ret = !ret;
		return ret;
	}

	public String getChannelRewardTypes()
	{
		String types = "";
		if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if (mailDataList != null && mailDataList.size() > 0)
			{
				for (int i = 0; i < mailDataList.size(); i++)
				{
					MailData mailData = mailDataList.get(i);
					if (mailData.hasReward() && mailData.getType() > 0 && !types.contains("" + mailData.getType()))
					{
						if (types.equals(""))
							types += mailData.getType();
						else
							types += ("," + mailData.getType());
					}
				}
			}
		}
		return types;
	}



	public int getMinCreateTime()
	{
		if (msgList == null || msgList.size() == 0)
			return 0;

		int result = msgList.get(0).createTime;
		for (int i = 0; i < msgList.size(); i++)
		{
			if (msgList.get(i).createTime < result)
			{
				result = msgList.get(i).createTime;
			}
		}
		return result;
	}

	public int getMinSeqId()
	{
		if (msgList == null || msgList.size() == 0)
			return 0;

		int result = msgList.get(0).sequenceId;
		for (int i = 0; i < msgList.size(); i++)
		{
			if (msgList.get(i).sequenceId < result)
			{
				result = msgList.get(i).sequenceId;
			}
		}
		return result;
	}

	public boolean isUnread()
	{
		return unreadCount > 0;
	}

	public long getChannelTime()
	{
		return latestTime;
	}

	public void updateMailList(MailData mailData)
	{
		if (mailData == null || mailDataList == null)
			return;
		for (int i = 0; i < mailDataList.size(); i++)
		{
			MailData mail = mailDataList.get(i);
			if (mail != null && mail.getUid().equals(mailData.getUid()))
			{
				if (StringUtils.isNotEmpty(mailData.nameText))
				{
					mail.nameText = mailData.nameText;
				}
				if (StringUtils.isNotEmpty(mailData.contentText))
				{
					mail.contentText = mailData.contentText;
				}
				break;
			}
		}
	}

	public MailData getLatestMailData()
	{
		if (StringUtils.isEmpty(latestId))
		{
			String latestMailId = DBManager.getInstance().getSysMailChannelLatestId(channelID);
			if (StringUtils.isNotEmpty(latestMailId))
			{
				latestId = latestMailId;
			}
		}

		if (StringUtils.isNotEmpty(latestId))
		{
			MailData mail = DBManager.getInstance().getSysMailByID(latestId);
			if (mail != null)
			{
				return mail;
			}
		}
		else
		{
			if (mailDataList != null && mailDataList.size() > 0)
			{
				MailData mail = mailDataList.get(mailDataList.size() - 1);
				if (mail != null)
				{
					return mail;
				}
			}
		}
		return null;
	}

	public void markAsRead()
	{
		if (unreadCount > 0)
		{
			unreadCount = 0;
			latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();

			if (this.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL || this.channelType == DBDefinition.CHANNEL_TYPE_USER) {
				ChannelManager.getInstance().calulateAllChannelUnreadNum();
			}

			DBManager.getInstance().updateChannel(this);
		}
	}

	public MailData getMonsterMailData()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "getMonsterMailData1_", mailDataList.size());
		if (StringUtils.isNotEmpty(channelID) && channelID.equals(MailManager.CHANNELID_MONSTER) && mailDataList != null
				&& mailDataList.size() > 0)
		{
			MailData mail = mailDataList.get(0);
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "getMonsterMailData2_", mail == null);
			if (mail == null)
				return null;

			int unreadCount = 0;
			List<MonsterMailContents> monsterArray = new ArrayList<MonsterMailContents>();
			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mailData = mailDataList.get(i);
				if (mailData == null)
					continue;
				if (!mailData.hasMailOpend)
				{
					mailData.setNeedParseByForce(true);
					mailData = MailManager.getInstance().parseMailDataContent(mailData);
				}
				if (mailData instanceof MonsterMailData)
				{
					MonsterMailData monsterMail = (MonsterMailData) mailData;
					if (monsterMail.isUnread())
						unreadCount++;

					if (monsterMail.getMonster() == null || monsterMail.getMonster().size() <= 0)
						continue;
					MonsterMailContents monster = monsterMail.getMonster().get(0);
					if (monster != null && !monsterArray.contains(monster))
						monsterArray.add(monster);
				}
			}

			MonsterMailData newMail = new MonsterMailData();
			newMail.setMailData(mail);
			newMail.setTotalNum(DBManager.getInstance().getSysMailCountByTypeInDB(mail.getChannelId()));
			newMail.setUnread(unreadCount);
			newMail.setMonster(monsterArray);
			return newMail;
		}
		return null;
	}

	public MailData getGiftMailData()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "getGiftMailData1_", mailDataList.size());
		if (StringUtils.isNotEmpty(channelID) && channelID.equals(MailManager.CHANNELID_GIFT) && mailDataList != null
				&& mailDataList.size() > 0)
		{
			MailData mail = mailDataList.get(0);
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "getGiftMailData2_", mail == null);
			if (mail == null)
				return null;

			int unreadCount = 0;
			List<GiftMailContents> giftArray = new ArrayList<GiftMailContents>();
			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mailData = mailDataList.get(i);
				if (mailData == null)
					continue;
				if (!mailData.hasMailOpend)
				{
					mailData.setNeedParseByForce(true);
					mailData = MailManager.getInstance().parseMailDataContent(mailData);
				}
				if (mailData instanceof GiftMailData)
				{
					GiftMailData giftMail = (GiftMailData) mailData;
					if (giftMail.isUnread())
						unreadCount++;

					if (giftMail.getGift() == null || giftMail.getGift().size() <= 0)
						continue;
					GiftMailContents gift = giftMail.getGift().get(0);
					if (gift != null && !giftArray.contains(gift))
						giftArray.add(gift);
				}
			}

			GiftMailData newMail = new GiftMailData();
			newMail.setMailData(mail);
			newMail.setTotalNum(DBManager.getInstance().getSysMailCountByTypeInDB(mail.getChannelId()));
			newMail.setUnread(unreadCount);
			newMail.setGift(giftArray);
			return newMail;
		}
		return null;
	}

	public MailData getResourceMailData()
	{
		if (StringUtils.isNotEmpty(channelID) && channelID.equals(MailManager.CHANNELID_RESOURCE) && mailDataList != null
				&& mailDataList.size() > 0)
		{
			MailData mail = mailDataList.get(0);
			if (mail == null)
				return null;

			int unreadCount = 0;
			List<ResourceMailContents> collectArray = new ArrayList<ResourceMailContents>();
			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mailData = mailDataList.get(i);
				if (mailData == null)
					continue;
				if (!mailData.hasMailOpend)
				{
					mailData.setNeedParseByForce(true);
					mailData = MailManager.getInstance().parseMailDataContent(mailData);
				}
				if (mailData instanceof ResourceMailData)
				{
					ResourceMailData resourceMail = (ResourceMailData) mailData;
					if (resourceMail.isUnread())
						unreadCount++;

					if (resourceMail.getCollect() == null || resourceMail.getCollect().size() <= 0)
						continue;
					ResourceMailContents resource = resourceMail.getCollect().get(0);
					if (resource != null && !collectArray.contains(resource))
						collectArray.add(resource);
				}
			}

			ResourceMailData newMail = new ResourceMailData();
			newMail.setMailData(mail);
			newMail.setTotalNum(DBManager.getInstance().getSysMailCountByTypeInDB(mail.getChannelId()));
			newMail.setUnread(unreadCount);
			newMail.setCollect(collectArray);
			return newMail;
		}
		return null;
	}

	public MailData getKnightMailData()
	{
		if (StringUtils.isNotEmpty(channelID) && channelID.equals(MailManager.CHANNELID_KNIGHT) && mailDataList != null
				&& mailDataList.size() > 0)
		{
			MailData mail = mailDataList.get(0);
			if (mail == null)
				return null;

			int unreadCount = 0;
			boolean isLock = false;
			List<BattleMailContents> knightArray = new ArrayList<BattleMailContents>();
			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mailData = mailDataList.get(i);
				if (mailData == null)
					continue;

				if (!isLock && mailData.isLock())
					isLock = true;

				if (!mailData.hasMailOpend)
				{
					mailData.setNeedParseByForce(true);
					mailData = MailManager.getInstance().parseMailDataContent(mailData);
				}
				if (mailData instanceof BattleMailData)
				{
					BattleMailData knightMail = (BattleMailData) mailData;
					if (knightMail.isUnread())
						unreadCount++;

					if (knightMail.getKnight() == null || knightMail.getKnight().size() <= 0)
						continue;
					BattleMailContents knight = knightMail.getKnight().get(0);
					if (knight != null && !knightArray.contains(knight))
						knightArray.add(knight);
				}
			}

			BattleMailData newMail = new BattleMailData();
			newMail.setIsKnightMail(true);
			newMail.setMailData(mail);
			newMail.setSave(isLock ? 1 : 0);
			newMail.setTotalNum(DBManager.getInstance().getSysMailCountByTypeInDB(mail.getChannelId()));
			newMail.setUnread(unreadCount);
			newMail.setKnight(knightArray);
			newMail.setContents("");
			newMail.setDetail(null);
			return newMail;
		}
		return null;
	}


	public MailData getMissleMailData()
	{
		if (StringUtils.isNotEmpty(channelID) && channelID.equals(MailManager.CHANNELID_MISSILE) && mailDataList != null
				&& mailDataList.size() > 0)
		{
			MailData mail = mailDataList.get(0);
			if (mail == null)
				return null;

			int unreadCount = 0;
			boolean isLock = false;
			List<MissileMailContents> missileArray = new ArrayList<MissileMailContents>();
			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mailData = mailDataList.get(i);
				if (mailData == null)
					continue;

				if (!isLock && mailData.isLock())
					isLock = true;

				if (!mailData.hasMailOpend)
				{
					mailData.setNeedParseByForce(true);
					mailData = MailManager.getInstance().parseMailDataContent(mailData);
				}
				if (mailData instanceof MissileMailData)
				{
					MissileMailData missileMail = (MissileMailData) mailData;
					if (missileMail.isUnread())
						unreadCount++;

					if (missileMail.getMissile() == null || missileMail.getMissile().size() <= 0)
						continue;
					MissileMailContents missile = missileMail.getMissile().get(0);
					if (missile != null && !missileArray.contains(missile))
						missileArray.add(missile);
				}
			}

			MissileMailData newMail = new MissileMailData();
			newMail.setMailData(mail);
			newMail.setSave(isLock ? 1 : 0);
			newMail.setTotalNum(DBManager.getInstance().getSysMailCountByTypeInDB(mail.getChannelId()));
			newMail.setUnread(unreadCount);
			newMail.setMissile(missileArray);
			newMail.setContents("");
			newMail.setDetail(null);
			return newMail;
		}
		return null;
	}

	public MailData getResourceHelpMailData()
	{
		if (StringUtils.isNotEmpty(channelID) && channelID.equals(MailManager.CHANNELID_RESOURCE_HELP) && mailDataList != null
				&& mailDataList.size() > 0)
		{
			MailData mail = mailDataList.get(0);
			if (mail == null)
				return null;

			int unreadCount = 0;
			List<ResourceHelpMailContents> collectArray = new ArrayList<ResourceHelpMailContents>();
			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mailData = mailDataList.get(i);
				if (mailData == null)
					continue;
				if (!mailData.hasMailOpend)
				{
					mailData.setNeedParseByForce(true);
					mailData = MailManager.getInstance().parseMailDataContent(mailData);
				}
				if (mailData instanceof ResourceHelpMailData)
				{
					ResourceHelpMailData resourceHelpMail = (ResourceHelpMailData) mailData;
					if (resourceHelpMail.isUnread())
						unreadCount++;

					if (resourceHelpMail.getCollect() == null || resourceHelpMail.getCollect().size() <= 0)
						continue;
					ResourceHelpMailContents resourceHelp = resourceHelpMail.getCollect().get(0);
					if (resourceHelp != null && !collectArray.contains(resourceHelp))
						collectArray.add(resourceHelp);
				}
			}

			ResourceHelpMailData newMail = new ResourceHelpMailData();
			newMail.setMailData(mail);
			newMail.setTotalNum(DBManager.getInstance().getSysMailCountByTypeInDB(mail.getChannelId()));
			newMail.setUnread(unreadCount);
			newMail.setCollect(collectArray);
			return newMail;
		}
		return null;
	}

	public void setUnreadCount(int count)
	{
		unreadCount = count;
		DBManager.getInstance().updateChannel(this);
	}

	public void setAllCount(int count)
	{
		allCount = count;
		DBManager.getInstance().updateChannel(this);
	}

	public boolean isCountryChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY;
	}

	public boolean isAllianceChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE;
	}
	public boolean isChatRoomChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_CHATROOM;
	}

	public boolean isModChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_USER && channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD)
				&& !channelID.equals(MailManager.CHANNELID_MOD);
	}

	public boolean isMessageChannel()
	{
		return (channelType == DBDefinition.CHANNEL_TYPE_USER && !channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD)
				&& !channelID.equals(MailManager.CHANNELID_MOD) && !channelID.equals(MailManager.CHANNELID_MESSAGE))
				|| channelType == DBDefinition.CHANNEL_TYPE_CHATROOM;
	}

	public boolean isUserMailChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_USER && !channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD)
				&& DBManager.getInstance().isUserMailExistDifferentType(getChatTable(), MsgItem.MSG_TYPE_MOD);
	}

	public String getLatestId()
	{
		if (StringUtils.isNotEmpty(channelID))
			return DBManager.getInstance().getSysMailChannelLatestId(channelID);
		return "";
	}

	public long getLatestTime()
	{
		if (StringUtils.isNotEmpty(channelID))
		{
			if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
			{
				return DBManager.getInstance().getSysMailChannelLatestTime(channelID);
			}
			else
			{
				return DBManager.getInstance().getChatLatestTime(getChatTable());
			}
		}
		return 0;
	}

	public boolean hasNoItemInChannel()
	{
		if (((channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL && !channelID.equals(MailManager.CHANNELID_GIFT)
						&& !channelID.equals(MailManager.CHANNELID_RESOURCE) && !channelID.equals(MailManager.CHANNELID_KNIGHT)&& !channelID.equals(MailManager.CHANNELID_MANORFIGHT) && !channelID.equals(MailManager.CHANNELID_BORDERFIGHT)&& !channelID.equals(MailManager.CHANNELID_MISSILE)&& !channelID.equals(MailManager.CHANNELID_BATTLEGAME)&& !channelID.equals(MailManager.CHANNELID_SHAMOGAME)&& !channelID.equals(MailManager.CHANNELID_ARENAGAME)&& !channelID.equals(MailManager.CHANNELID_SHAMOEXPLORE)))
				|| (channelType == DBDefinition.CHANNEL_TYPE_USER && (channelID.equals(MailManager.CHANNELID_MOD) || channelID
						.equals(MailManager.CHANNELID_MESSAGE))))
			return false;
		boolean ret = false;
		if ((channelType == DBDefinition.CHANNEL_TYPE_USER /*|| channelType == DBDefinition.CHANNEL_TYPE_CHATROOM*/) && !hasMsgItemInDB())
		{
			ret = true;
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL && !hasMailDataInDB())
		{
			ret = true;
		}
		return ret;
	}
	public boolean hasSysMailInList()
	{
		return mailDataList != null && mailDataList.size() > 0;
	}

	public void clearAllSysMail()
	{
		if (mailDataList != null && mailDataList.size() > 0)
		{
			boolean hasDetectMail = false;
			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mail = mailDataList.get(i);
				if (mail != null && StringUtils.isNotEmpty(mail.getUid()))
				{
					DBManager.getInstance().deleteSysMail(this, mail.getUid());
					if (!hasDetectMail && (mail.getType() == MailManager.MAIL_DETECT_REPORT||mail.getType() == MailManager.Mail_NEW_SCOUT_REPORT_FB))
						hasDetectMail = true;
				}
			}
			if (hasDetectMail)
				DBManager.getInstance().getDetectMailInfo();
			mailDataList.clear();
			mailUidList.clear();
		}

		unreadCount = 0;
		ChannelListFragment.onChannelRefresh();
		ChatFragment.onChannelRefresh();
		latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
		ChannelManager.getInstance().deleteChannel(this);
	}

	private static int	loadCnt		= 0;
	private boolean		initLoaded	= false;

	public void resetMsgChannel()
	{
		initLoaded = false;
		loadCnt = 0;
		if (msgList != null && msgList.size() > 0)
			msgList.clear();

	}

	public void loadMoreMsg()
	{
		initLoaded = true;
		loadCnt++;

		// if (channelType != DBDefinition.CHANNEL_TYPE_USER &&
		// !WebSocketManager.isRecieveFromWebSocket(channelType))
		// {
		// int dbMinSeqId = (dbMaxSeqId - ChannelManager.LOAD_MORE_COUNT + 1) >
		// 0 ? (dbMaxSeqId - ChannelManager.LOAD_MORE_COUNT + 1) : 1;
		// ChannelManager.getInstance().loadMoreMsgFromDB(this, dbMinSeqId,
		// prevDBMaxSeqId, -1, false);
		// }
		// else
		// {
		// // 以前没有这个分支，15.12.2新加，针对MsgChannelAdapter中可能的调用
		// ChannelManager.getInstance().loadMoreMsgFromDB(this, -1, -1,
		// getMinCreateTime(), true);
		// }

		ChannelManager.getInstance().loadMoreMsgFromDB(this, -1, -1, getMinCreateTime(), true);
	}

	/**
	 * 聊天型channel已经完成初次加载（从网络或db加载历史消息，收到push消息）
	 */
	public boolean hasInitLoaded()
	{
		return initLoaded == true || msgList.size() > 0;
	}

	public boolean hasMsgItemInDB()
	{
		return DBManager.getInstance().hasMsgItemInTable(getChatTable());
	}

	public boolean hasMailDataInDB()
	{
		return DBManager.getInstance().hasMailDataInDB(channelID);
	}

	public List<String> getMailUidArrayByConfigType(int configType)
	{
		List<String> uidArray = new ArrayList<String>();
		List<MailData> mailList = DBManager.getInstance().getSysMailFromDB(channelID, configType);
		if (mailList != null && mailList.size() > 0)
		{
			for (int i = 0; i < mailList.size(); i++)
			{
				MailData mailData = mailList.get(i);
				if ((mailData.isUnread() || configType== DBManager.CONFIG_TYPE_REWARD) && StringUtils.isNotEmpty(mailData.getUid()) && !uidArray.contains(mailData.getUid()))
				{
					uidArray.add(mailData.getUid());
				}
			}
		}
		return uidArray;
	}

	public String getMailUidsByConfigType(int configType)
	{
		String uids = "";
		List<String> mailUidArray = getMailUidArrayByConfigType(configType);
		if (mailUidArray != null && mailUidArray.size() > 0)
		{
			uids = ChannelListFragment.getUidsByArray(mailUidArray);
		}
		return uids;
	}

	public void getTimeNeedShowMsgIndex()
	{
		if (channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL && msgList != null && msgList.size() > 0)
		{
			if (msgTimeIndexArray == null)
				msgTimeIndexArray = new ArrayList<Integer>();
			else
				msgTimeIndexArray.clear();
			int tempCreateTime = 0;
			for (int i = 0; i < msgList.size(); i++)
			{
				MsgItem msgItem = msgList.get(i);
				if (msgItem.createTime - tempCreateTime > 5 * 60)
				{
					tempCreateTime = msgItem.createTime;
					msgTimeIndexArray.add(Integer.valueOf(i));
				}
			}
		}
	}

	public void getLoadedTimeNeedShowMsgIndex(int loadCount)
	{
		if (channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL && msgList != null && msgList.size() > 0)
		{
			if (msgTimeIndexArray == null)
			{
				getTimeNeedShowMsgIndex();
			}
			else
			{
				if (msgTimeIndexArray.size() > 0)
					msgTimeIndexArray.remove(Integer.valueOf(0));
				for (int i = 0; i < msgTimeIndexArray.size(); i++)
				{
					Integer indexInt = msgTimeIndexArray.get(i);
					if (indexInt != null)
					{
						msgTimeIndexArray.set(i, Integer.valueOf(indexInt.intValue() + loadCount));
					}
				}

				int tempCreateTime = 0;
				for (int i = 0; i < msgList.size() && i < loadCount + 1; i++)
				{
					MsgItem msgItem = msgList.get(i);
					if (msgItem.createTime - tempCreateTime > 5 * 60)
					{
						tempCreateTime = msgItem.createTime;
						msgTimeIndexArray.add(Integer.valueOf(i));
					}
				}
			}
		}
	}

	public List<Integer> getMsgIndexArrayForTimeShow()
	{
		return msgTimeIndexArray;
	}

	public void querySysMailCountFromDB()
	{
		sysMailCountInDB = ChannelManager.getInstance().getSysMailDBCount(this);
		calculateSysMailCountInDB = true;

	}

	public void updateSysMailCountFromDB(int count)
	{
		if (sysMailCountInDB <= 0 || sysMailCountInDB + count < 0)
			querySysMailCountFromDB();
		else
			sysMailCountInDB += count;
	}

	public int getSysMailCountInDB()
	{
		if (!calculateSysMailCountInDB)
			querySysMailCountFromDB();
		return sysMailCountInDB;
	}

	public int getSysUnreadMailCountInDB() {
		return sysUnreadMailCountInDB;
	}

	public void queryUnreadSysMailCountFromDB()
	{
		sysUnreadMailCountInDB = ChannelManager.getInstance().getUnreadSysMailDBCount(this);
	}

	public void updateUnreadSysMailCountFromDB(int count)
	{
		if (sysUnreadMailCountInDB <= 0 || sysUnreadMailCountInDB + count < 0)
			queryUnreadSysMailCountFromDB();
		else
			sysUnreadMailCountInDB += count;
	}


	
	public void removeMailDataList()
	{
		if(mailDataList.size()>10){
			for (int i = mailDataList.size()-1; i >=10 ; i--) { 
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW);
				if(mailUidList.contains(mailDataList.get(i).getUid()))
				{
				    mailUidList.remove(mailDataList.get(i).getUid());
				}
				mailDataList.remove(i);  			 
			} 
		}
	}

	public boolean isDialogChannel()
	{
		return StringUtils.isNotEmpty(channelID)
				&& (channelID.equals(MailManager.CHANNELID_RESOURCE) || channelID.equals(MailManager.CHANNELID_KNIGHT) || channelID
						.equals(MailManager.CHANNELID_MONSTER)|| channelID.equals(MailManager.CHANNELID_GIFT) || channelID
				.equals(MailManager.CHANNELID_MISSILE)|| channelID
				.equals(MailManager.CHANNELID_BATTLEGAME)|| channelID
				.equals(MailManager.CHANNELID_ARENAGAME)|| channelID.equals(MailManager.CHANNELID_SHAMOGAME)|| channelID.equals(MailManager.CHANNELID_SHAMOEXPLORE)
				|| channelID.equals(MailManager.CHANNELID_BORDERFIGHT)|| channelID.equals(MailManager.CHANNELID_MANORFIGHT)
		);
	}
	//=====================    华丽的分割线   读取数据库   =======================//
	public ChatChannel(Cursor c)
	{
		try
		{
			channelID = c.getString(c.getColumnIndex(DBDefinition.CHANNEL_CHANNEL_ID));
			dbMinSeqId = c.getInt(c.getColumnIndex(DBDefinition.CHANNEL_MIN_SEQUENCE_ID));
			dbMaxSeqId = c.getInt(c.getColumnIndex(DBDefinition.CHANNEL_MAX_SEQUENCE_ID));
			channelType = c.getInt(c.getColumnIndex(DBDefinition.CHANNEL_TYPE));
			if (StringUtils.isNotEmpty(c.getString(c.getColumnIndex(DBDefinition.CHANNEL_CHATROOM_MEMBERS))))
			{
				String[] members = c.getString(c.getColumnIndex(DBDefinition.CHANNEL_CHATROOM_MEMBERS)).split("\\|");
				for (int i = 0; i < members.length; i++)
				{
					memberUidArray.add(members[i]);
				}
			}

			roomOwner = c.getString(c.getColumnIndex(DBDefinition.CHANNEL_CHATROOM_OWNER));
			isMember = c.getInt(c.getColumnIndex(DBDefinition.CHANNEL_IS_MEMBER)) == 1;
			customName = c.getString(c.getColumnIndex(DBDefinition.CHANNEL_CUSTOM_NAME));
			if (ChannelManager.isNeedCalculateUnreadCount(channelID))
				unreadCount = 0;
			else
				unreadCount = c.getInt(c.getColumnIndex(DBDefinition.CHANNEL_UNREAD_COUNT));
			allCount = c.getInt(c.getColumnIndex(DBDefinition.CHANNEL_ALL_COUNT));
			latestId = c.getString(c.getColumnIndex(DBDefinition.CHANNEL_LATEST_ID));
			latestTime = c.getInt(c.getColumnIndex(DBDefinition.CHANNEL_LATEST_TIME));
			latestModifyTime = c.getLong(c.getColumnIndex(DBDefinition.CHANNEL_LATEST_MODIFY_TIME));
			settings = c.getString(c.getColumnIndex(DBDefinition.CHANNEL_SETTINGS));
			initSeqId();
			// 这里加上表是否存在的判断,否则如果表不存在会造成递归死循环
//			if(DBManager.getInstance().isTableExists(getChatTable().getTableName()) &&
//				(channelType==DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE || channelType == DBDefinition.CHANNEL_TYPE_CHATROOM))
//				getMaxAndMinSeqId();
//			refreshRenderData();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}

	}

	private void initSeqId()
	{
		if (DBManager.getInstance().isTableExists(getChatTable().getTableName()) && hasSeqId())
		{
			getMaxAndMinSeqId();
		}
	}

	private boolean hasSeqId()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE
				|| channelType == DBDefinition.CHANNEL_TYPE_CHATROOM;
	}

	private void getMaxAndMinSeqId()
	{
		int maxSeqId = DBManager.getInstance().getMaxDBSeqId(getChatTable());
		int minSeqId = DBManager.getInstance().getMinDBSeqId(getChatTable());
		boolean hasChanged = false;
		if (maxSeqId != 0)
		{
			dbMaxSeqId = maxSeqId;
			hasChanged = true;
		}
		if (minSeqId != 0)
		{
			dbMinSeqId = minSeqId;
			hasChanged = true;
		}
		if (hasChanged)
			DBManager.getInstance().updateChannel(this);
		prevDBMaxSeqId = dbMaxSeqId;
	}

	public ContentValues getContentValues()
	{
		ContentValues cv = new ContentValues();
		cv.put(DBDefinition.COLUMN_TABLE_VER, DBHelper.CURRENT_DATABASE_VERSION);
		cv.put(DBDefinition.CHANNEL_CHANNEL_ID, channelID);
		cv.put(DBDefinition.CHANNEL_MIN_SEQUENCE_ID, dbMinSeqId);
		cv.put(DBDefinition.CHANNEL_MAX_SEQUENCE_ID, dbMaxSeqId);
		cv.put(DBDefinition.CHANNEL_TYPE, channelType);
		cv.put(DBDefinition.CHANNEL_CHATROOM_MEMBERS, getMembersString(memberUidArray));
		cv.put(DBDefinition.CHANNEL_CHATROOM_OWNER, roomOwner);
		cv.put(DBDefinition.CHANNEL_IS_MEMBER, isMember ? 1 : 0);
		cv.put(DBDefinition.CHANNEL_CUSTOM_NAME, customName);
		cv.put(DBDefinition.CHANNEL_UNREAD_COUNT, unreadCount);
		cv.put(DBDefinition.CHANNEL_ALL_COUNT, allCount);
		cv.put(DBDefinition.CHANNEL_LATEST_ID, latestId);
		cv.put(DBDefinition.CHANNEL_LATEST_TIME, latestTime);
		cv.put(DBDefinition.CHANNEL_LATEST_MODIFY_TIME, latestModifyTime);
		cv.put(DBDefinition.CHANNEL_SETTINGS, settings);

		return cv;
	}




	//=====================    华丽的分割线      =======================//


//=====================    华丽的分割线      =======================//

	public void refreshRenderData()
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.BTN_COUNTRY);

			channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_CHAT_ROOM);
			timeText = TimeManager.getReadableTime(latestTime);

			if (msgList.size() > 0)
			{
				MsgItem msg = msgList.get(msgList.size() - 1);
				if (msg != null)
				{
					showItem = msg;
					if (!msg.translateMsg.equals(""))
						contentText = msg.translateMsg;
					else
						contentText = msg.msg;
				}
			}
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.BTN_ALLIANCE);

			channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_CHAT_ROOM);
			timeText = TimeManager.getReadableTime(latestTime);

			if (msgList.size() > 0)
			{
				MsgItem msg = msgList.get(msgList.size() - 1);
				if (msg != null)
				{
					showItem = msg;
					if (!msg.translateMsg.equals(""))
						contentText = msg.translateMsg;
					else
						contentText = msg.msg;
				}
			}
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_USER)
		{

			if (StringUtils.isEmpty(channelID))
				return;

			if(TimeManager.isInValidTime(latestTime))
			{
				latestTime = getLatestTime();
			}
			timeText = TimeManager.getReadableTime(latestTime);

			if(channelID.equals(MailManager.CHANNELID_MOD))
			{
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_MOD);
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_MOD);
				contentText = ChannelManager.getInstance().latestModChannelMsg;
				return;
			}
			else if (channelID.equals(MailManager.CHANNELID_MESSAGE))
			{
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_MESSAGE);
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_NAME_MESSAGE);
				contentText = ChannelManager.getInstance().latestMessageChannelMsg;
				return;
			}

			if (TimeManager.isInValidTime(latestTime))
			{
				latestTime = getLatestTime();
			}
			timeText = TimeManager.getReadableTime(latestTime);

			String fromUid = ChannelManager.getInstance().getModChannelFromUid(channelID);
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "fromUid", fromUid);
			UserManager.checkUser(fromUid, "", 0);

			MsgItem mail = getLatestUserMail();
			if (StringUtils.isNotEmpty(fromUid))
			{
				UserInfo fromUser = UserManager.getInstance().getUser(fromUid);
				if(!(fromUser.userName.equals(customName)) && !(fromUid.equals(UserManager.getInstance().getCurrentUserId())))
				{
					fromUser.userName = customName;
					UserManager.getInstance().updateUser(fromUser);
				}
				if (fromUser != null)
				{
					channelIcon = fromUser.headPic;
					channelShowUserInfo = fromUser;
					nameText = "";
					if (StringUtils.isNotEmpty(fromUser.asn))
					{
						nameText = "(" + fromUser.asn + ")";
					}

					if (StringUtils.isNotEmpty(fromUser.userName))
					{
						nameText += fromUser.userName;
					}
					else if (StringUtils.isNotEmpty(customName))
					{
						nameText += customName;
					}
					else
					{
						nameText += fromUser.uid;
					}
					if(mail!=null && UserManager.getInstance().getCurrentUser()!=null){
						if(fromUser.crossFightSrcServerId>0
								&& fromUser.crossFightSrcServerId != UserManager.getInstance().getCurrentUser().serverId
								&& mail.channelType == DBDefinition.CHANNEL_TYPE_USER){
							nameText += "#"+fromUser.crossFightSrcServerId;
						}
					}

				}
				if (fromUid.equals(UserManager.getInstance().getCurrentUserId()))
				{
					nameText = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE);
				}
			}
			else
			{
				nameText = channelID;
			}

			if (mail != null)
			{
				if (TimeManager.isInValidTime(latestTime))
				{
					latestTime = mail.createTime;
					timeText = TimeManager.getReadableTime(latestTime);
				}
				if (mail.canShowTranslateMsg())
				{
					contentText = mail.translateMsg;
				}
				else
				{
					contentText = mail.msg;
				}
			}
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_CHAT_ROOM);
			if (TimeManager.isInValidTime(latestTime))
			{
				latestTime = getLatestTime();
			}
			timeText = TimeManager.getReadableTime(latestTime);

			nameText = StringUtils.isNotEmpty(customName) ? customName : channelID;

			if( channelID.contains("warzone_")){
				nameText = LanguageManager.getLangByKey(LanguageKeys.BTN_WARZONE_NAME);
			}
			MsgItem mail = null;
			if (msgList.size() > 0)
			{
				if (StringUtils.isNotEmpty(latestId) && StringUtils.isNumeric(latestId)
						&& DBManager.getInstance().isTableExists(getChatTable().getTableName()))
				{
					mail = DBManager.getInstance().getChatBySequeueId(getChatTable(), Integer.parseInt(latestId));
				}
				if (mail == null)
					mail = msgList.get(msgList.size() - 1);
			}

			if (mail != null)
			{
				if (TimeManager.isInValidTime(latestTime))
				{
					latestTime = mail.createTime;
					timeText = TimeManager.getReadableTime(latestTime);
				}
				if (mail.canShowTranslateMsg())
				{
					if (mail.isTipMsg()||mail.isUserADMsg())
						contentText = mail.translateMsg;
					else
					{
						if (mail.isSelfMsg())
							contentText = LanguageManager.getLangByKey(LanguageKeys.TIP_YOU) + ":" + mail.translateMsg;
						else
							contentText = mail.getName() + ":" + mail.translateMsg;
					}
				}
				else
				{
					if (mail.isTipMsg()||mail.isUserADMsg())
						contentText = mail.msg;
					else
					{
						if (mail.isSelfMsg())
							contentText = LanguageManager.getLangByKey(LanguageKeys.TIP_YOU) + ":" + mail.msg;
						else
							contentText = mail.getName() + ":" + mail.msg;
					}
				}
			}
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{

			if (channelID.equals(MailManager.CHANNELID_FIGHT))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_FIGHT);
			else if (channelID.equals(MailManager.CHANNELID_ALLIANCE))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_ALLIANCE);
			else if (channelID.equals(MailManager.CHANNELID_MESSAGE))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_MESSAGE);
			else if (channelID.equals(MailManager.CHANNELID_EVENT))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_EVENT);


			if (channelID.equals(MailManager.CHANNELID_STUDIO))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_STUDIO);
			else if (channelID.equals(MailManager.CHANNELID_SYSTEM))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_SYSTEM);
			else if (channelID.equals(MailManager.CHANNELID_RESOURCE))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_RESOURCE);
			else if (channelID.equals(MailManager.CHANNELID_KNIGHT))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_KNIGHT);
			else if (channelID.equals(MailManager.CHANNELID_MONSTER))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_MONSTER);
			else if (channelID.equals(MailManager.CHANNELID_GIFT))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_GIFT);
			else if (channelID.equals(MailManager.CHANNELID_MISSILE))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_MISSILE);
			else if (channelID.equals(MailManager.CHANNELID_NOTICE))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_ANNOUNCEMENT);
			else if (channelID.equals(MailManager.CHANNELID_BATTLEGAME))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_BATTLEGAME);
			else if (channelID.equals(MailManager.CHANNELID_ARENAGAME))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_ARENAGAME);
			else if (channelID.equals(MailManager.CHANNELID_SHAMOGAME))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_SHAMOGAME);
			else if (channelID.equals(MailManager.CHANNELID_SHAMOEXPLORE))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_RESOURCE);
			else if (channelID.equals(MailManager.CHANNELID_BORDERFIGHT))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_SHAMOGAME);
			else if (channelID.equals(MailManager.CHANNELID_MANORFIGHT))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_SHAMOGAME);

			nameText = getSystemChannelName();

			if (channelID.equals(MailManager.CHANNELID_FIGHT) || channelID.equals(MailManager.CHANNELID_ALLIANCE)
					|| channelID.equals(MailManager.CHANNELID_EVENT) || channelID.equals(MailManager.CHANNELID_STUDIO)
					|| channelID.equals(MailManager.CHANNELID_SYSTEM) || channelID.equals(MailManager.CHANNELID_KNIGHT)
					|| channelID.equals(MailManager.CHANNELID_MONSTER) || channelID.equals(MailManager.CHANNELID_RESOURCE)
					|| channelID.equals(MailManager.CHANNELID_MISSILE)|| channelID.equals(MailManager.CHANNELID_GIFT)
					|| channelID.equals(MailManager.CHANNELID_BATTLEGAME)|| channelID.equals(MailManager.CHANNELID_ARENAGAME)
					|| channelID.equals(MailManager.CHANNELID_SHAMOGAME)|| channelID.equals(MailManager.CHANNELID_SHAMOEXPLORE)
					|| channelID.equals(MailManager.CHANNELID_BORDERFIGHT)|| channelID.equals(MailManager.CHANNELID_MANORFIGHT)
					)

				return;

			if (TimeManager.isInValidTime(latestTime))
			{
				latestTime = getLatestTime();
			}
			timeText = TimeManager.getReadableTime(latestTime);

			if (mailDataList.size() > 0)
			{
				MailData mail = getLatestMailData();
				if (mail != null)
				{
					if (TimeManager.isInValidTime(latestTime))
					{
						latestTime = mail.getCreateTime();
						timeText = TimeManager.getReadableTime(latestTime);
					}
					if (StringUtils.isEmpty(nameText))
						nameText = mail.nameText;
					contentText = mail.contentText;
					channelIcon = mail.mailIcon;
				}
			}
		}
	}
	//个人邮件最后一封邮件
	public MsgItem getLatestUserMail()
	{
		MsgItem mail = null;
		if (DBManager.getInstance().hasMsgItemInTable(getChatTable()))
		{
			String latestId = DBManager.getInstance().getUserMsgLatestId(getChatTable());
			// LogUtil.printVariablesWithFuctionName(Log.INFO,
			// LogUtil.TAG_DEBUG, "latestId", latestId);
			if (StringUtils.isNotEmpty(latestId) && DBManager.getInstance().isTableExists(getChatTable().getTableName()))
			{
				mail = DBManager.getInstance().getUserMailByID(getChatTable(), latestId);
			}
			if (mail == null && msgList != null && msgList.size() > 0)
				mail = msgList.get(msgList.size() - 1);
		}
		return mail;
	}



	public String getCustomName()
	{
		String name = "";
		if (StringUtils.isNotEmpty(customName))
		{
			name = customName;
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			UserInfo fromUser = UserManager.getInstance().getUser(channelID);
			if (fromUser != null)
			{
				if (StringUtils.isNotEmpty(fromUser.userName))
				{
					name = fromUser.userName;
					customName = name;
					DBManager.getInstance().updateChannel(this);
				}
				else
				{
					name = fromUser.uid;
				}
			}

		}
		return name;
	}

	//聊天室名称
	public String getTitleName(){
		String name = "";
		if (StringUtils.isNotEmpty(customName))
		{
			name = customName;
		}
		else
		{
			name = nameText;
		}

		return name;
	}

	//频道名称
	public String getSystemChannelName()
	{
		String name = "";
		if (channelID.equals(MailManager.CHANNELID_SYSTEM))
			name = LanguageManager.getLangByKey(LanguageKeys.TIP_SYSTEM);
		else if (channelID.equals(MailManager.CHANNELID_STUDIO))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_STUDIO);
		else if (channelID.equals(MailManager.CHANNELID_FIGHT))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_FIGHT);
		else if (channelID.equals(MailManager.CHANNELID_MOD))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_MOD);
		else if (channelID.equals(MailManager.CHANNELID_ALLIANCE))
			name = LanguageManager.getLangByKey(LanguageKeys.BTN_ALLIANCE);
		else if (channelID.equals(MailManager.CHANNELID_NOTICE))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_NOTICE);
		else if (channelID.equals(MailManager.CHANNELID_RESOURCE))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_NAME_RESOURCE);
		else if (channelID.equals(MailManager.CHANNELID_KNIGHT))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_ACTIVITYREPORT);
		else if (channelID.equals(MailManager.CHANNELID_RESOURCE_HELP))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_RESOURCEHELP);
		else if (channelID.equals(MailManager.CHANNELID_MONSTER))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_103715);
		else if (channelID.equals(MailManager.CHANNELID_GIFT))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_GIFTCH);
		else if (channelID.equals(MailManager.CHANNELID_MISSILE))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_109516);
		else if (channelID.equals(MailManager.CHANNELID_EVENT))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_EVENT);
		else if (channelID.equals(MailManager.CHANNELID_BATTLEGAME))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_BATTLEGAME);
		else if (channelID.equals(MailManager.CHANNELID_ARENAGAME))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ARENAGAME);
		else if (channelID.equals(MailManager.CHANNELID_SHAMOGAME))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_SHAMOGAME);
		else if (channelID.equals(MailManager.CHANNELID_SHAMOEXPLORE))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_SHAMOEXPLORE);
		else if (channelID.equals(MailManager.CHANNELID_BORDERFIGHT))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_BORDERFIGHT);
		else if (channelID.equals(MailManager.CHANNELID_MANORFIGHT))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MANORFIGHT);
		return name;
	}
}
//=====================    华丽的分割线      =======================//
/*
	//弃用
	public int getUnreadCountInMailList()
	{
		int unreadCount = 0;
		if(mailDataList!=null && mailDataList.size()>0)
		{
			for(MailData mail :mailDataList)
			{
				if(mail!=null && mail.isUnread())
					unreadCount++;
			}
		}
		return unreadCount;
	}

	//弃用--无用

	public List<String> getChannelUnreadUidArray()
	{
		List<String> unReadUidArray = new ArrayList<String>();
		if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if (mailDataList != null && mailDataList.size() > 0)
			{
				for (int i = 0; i < mailDataList.size(); i++)
				{
					MailData mailData = mailDataList.get(i);
					if (mailData.isUnread() && StringUtils.isNotEmpty(mailData.getUid()) && !unReadUidArray.contains(mailData.getUid()))
					{
						unReadUidArray.add(mailData.getUid());
					}
				}
			}
		}
		return unReadUidArray;
	}

	public int getUnreadSysMailCountInDB()
	{
		if (!calculateUnreadSysMailCountInDB)
			queryUnreadSysMailCountFromDB();
		return sysUnreadMailCountInDB;
	}

	public int getServerNewestId()
	{
		int dbMaxId = getDBMaxId();
		return Math.min(serverMinSeqId, dbMaxId);
	}

	public String getChannelDeleteTypes()
	{
		String types = "";
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
			return types;
		if (channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			types = "0";
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if (mailDataList != null && mailDataList.size() > 0)
			{
				for (int i = 0; i < mailDataList.size(); i++)
				{
					MailData mailData = mailDataList.get(i);
					if (mailData.canDelete() && mailData.getType() > 0 && !types.contains("" + mailData.getType()))
					{
						if (types.equals(""))
							types += mailData.getType();
						else
							types += ("," + mailData.getType());
					}
				}
			}
		}
		return types;
	}
	 */

