package com.chatsdk.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.TextView;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.db.DBHelper;
import com.chatsdk.model.db.DBManager;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.ResUtil;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.chatsdk.R.drawable.item;
import static org.apache.commons.lang.StringUtils.isNotEmpty;


public final class MsgItem
{
	public final static int	SENDING							= 0;
	public final static int	SEND_FAILED						= 1;
	public final static int	SEND_SUCCESS					= 2;

	/***********适配的界面类型*********************/
	public final static int	MSGITEM_TYPE_MESSAGE			= 0;
	public final static int	MSGITEM_TYPE_GIF				= 1;
	public final static int	MSGITEM_TYPE_PIC				= 2;
	public final static int	MSGITEM_TYPE_REDPACKAGE			= 3;
	public final static int	MSGITEM_TYPE_CHATROM_TIP		= 4;
	public final static int	MSGITEM_TYPE_NEW_MESSAGE_TIP	= 5;
	public final static int	MSGITEM_TYPE_AUDIO				= 6;
	public final static int	MSGITEM_TYPE_MESSAGE_COMMENT	= 7;
	/***********消息类型*********************/
	public final static int	MSG_TYPE_MESSAGE				= 0;
	public final static int	MSG_TYPE_ALLIANCE_CREATE		= 1;
	public final static int	MSG_TYPE_ALLIANCE				= 2;	//联盟的一些信息
	public final static int	MSG_TYPE_INVITE					= 3;	//联盟邀请 和联盟宣言发生变化
	public final static int	MSG_TYPE_BATTLE_SHARE			= 4;
	public final static int	MSG_TYPE_INVESTIGATEREPORT		= 5;
	public final static int	MSG_TYPE_LOUDSPEAKER			= 6;
	public final static int	MSG_TYPE_EQUIPSHARE				= 7;
	
	public final static int	MSG_TYPE_ALLIANCE_JOIN			= 8;
	public final static int	MSG_TYPE_ALLIANCE_RALLY			= 9;
	public final static int	MSG_TYPE_LOTTERY_SHARE			= 10;
	public final static int	MSG_TYPE_ALLIANCETASK_SHARE		= 11;
	public final static int	MSG_TYPE_RED_PACKAGE			= 12;
	public final static int	MSG_TYPE_COR_SHARE				= 13;
	public final static int	MSG_TYPE_ALLIANCE_TREASURE		= 14;
	public final static int	MSG_TYPE_AUDIO					= 15;
	public final static int	MSG_TYPE_ALLIANCEHELP			= 16;
	public final static int	MSG_TYPE_ALLIANCE_OFFICER		= 17;
	public final static int	MSG_TYPE_ALLIANCE_MONTHCARDBOX	= 18;
	public final static int	MSG_TYPE_SEVEN_DAY				= 19;
	public final static int	MSG_TYPE_MISSILE_SHARE			= 20;
	public final static int	MSG_TYPE_ALLIANCE_GROUP_BUG_SHARE= 21;
	public final static int	MSG_TYPE_CREATE_EQUIP_SHARE     = 22;
	public final static int	MSG_TYPE_NEW_CREATE_EQUIP_SHARE = 23;
	public final static int	MSG_TYPE_USE_ITEM_SHARE 		= 24;
	public final static int MSG_TYPE_GIFT_MAIL_SHARE		= 25;
	public final static int MSG_TYPE_FAVOUR_POINT_SHARE		= 26;
	public final static int MSG_TYPE_WOUNDED_SHARE		    = 27;//联盟庇护所伤兵分享
	public final static int MSG_TYPE_MEDAL_SHARE		    = 28;//第八件装备勋章分享
	public final static int MSG_TYPE_SHAMO_INHESION_SHARE   = 29;//天赋沙漠分享
	public final static int MSG_TYPE_SHAMO_FORMATION_BATTLE_SHARE   = 30;//天赋沙漠分享
	public final static int MSG_TYPE_FB_SCOUT_REPORT_SHARE   = 31;//新侦查邮件分享

	public final static int MSG_TYPE_FB_ACTIVITY_HERO_SHARE   = 32;//查看自由城建积分兑换英雄活动
	public final static int MSG_TYPE_FB_FORMATION_SHARE  = 33;//查看自由城建编队分享

	public final static int MSG_TYPE_SCIENCE_MAX__SHARE		= 34;//科技MAX分享
	public final static int MSG_TYPE_ALLIANCE_COMMON_SHARE	= 35;//新联盟转盘等带有多语言ID的
	public final static int MSG_TYPE_SEVENDAY_NEW_SHARE		= 36;//新末日投资
	public final static int MSG_TYPE_ALLIANCE_ATTACT_SHARE		= 37;//联盟集结BOSS
	public final static int MSG_TYPE_ALLIANCE_ARMS_RACE_SHARE		= 38;//联盟军备竞赛活动类型
	public final static int MSG_TYPE_ENEMY_PUTDOWN_POINT_SHARE		= 39;///敌方联盟在本战区内放置集结点
	/** 增加post时要变更这个值 */
	public final static int	MSG_TYPE_MAX_VALUE				= MSG_TYPE_ENEMY_PUTDOWN_POINT_SHARE;

	public final static int	MSG_TYPE_CHATROOM_TIP			= 100;
	public final static int	MSG_TYPE_USER_AD_TIP			= 150;
	public final static int	MSG_TYPE_AREA_MSG_TIP			= 180;
	public final static int	MSG_TYPE_MOD					= 200;
	public final static int	MAIL_MOD_PERSON					= 23;

	public final static int	HANDLED							= 0;
	public final static int	UNHANDLE						= 1;
	public final static int	NONE_MONEY						= 2;
	public final static int	FINISH							= 3;

	public final static int	VOICE_UNREAD					= 0;
	public final static int	VOICE_READ						= 1;

	/** 数据库使用的id */
	public int				_id;
	public int				tableVer;
	public int				sequenceId;
	/** 用来标识邮件的id */
	public String			mailId;
	/** uid，群聊时才会存数据库 */
	public String			uid								= "";
	/** 频道类型 */
	public int				channelType						= -1;
	/** 收到的消息会在C++中初始化此字段，对应后台传回来的createTime */
	public int				createTime						= 0;
	/** 数据库中名为type：是否为系统信息，“0”表示不是，非“0”表示是 */
	public int				post							= -1;
	/** 消息体 */
	public String			msg								= "";
	/** 评论内容 */
	public String			shareComment					= "";
	/** 翻译信息 */
	public String			translateMsg					= "";
	/** 源语言 */
	public String			originalLang					= "";
	/** 翻译后的语言 */
	public String			translatedLang					= "";
	/**
	 * 对于自己发的消息,发送状态，0正在发送，1发送失败，2发送成功 红包消息时，表示红包的领取状态,1未领取，0领取过,2被抢光了,3到期了
	 * */
	public int				sendState						= -1;
	public int				readStateBefore					= -1;
	/** 战报UID，侦察战报UID,装备ID等 */
	public String			attachmentId					= "";

	public String			media							= "";

	public String           roomId                          = "";

	// 运行时属性
	/** 是否是自己的信息 */
	public boolean			isSelfMsg;
	/** 是否是新消息 */
	public boolean			isNewMsg;
	public String			currentText						= "";
	/** 是否被翻译过 */
	public boolean			hasTranslated;
	public boolean			isSendDataShowed				= false;
	public int				lastUpdateTime					= 0;
	/** 本地发送时间戳 */
	public int				sendLocalTime					= 0;
	public boolean			isTranslateByGoogle				= false;
	public boolean			isFirstNewMsg					= false;
	/**
	 * 0:不是第一条 1:第一条且新消息数小于等于200条 2:第一条且新消息数超过200条
	 * */
	public int				firstNewMsgState				= 0;
	/** msgItem所属的Channel */
	public ChatChannel		chatChannel						= null;
	/** 是否强制翻译，点击翻译菜单后置为true，点击原文置为false */
	public boolean			isTranslatedByForce				= false;
	/** 是否做过强制翻译，点击翻译菜单后置为true */
	public boolean			hasTranslatedByForce			= false;
	//是否被强制显示原文
	public boolean			isOriginalLangByForce			= false;
	public boolean			isAudioDownloading				= false;

	public boolean			isMsgBadReplace					= false;
	public boolean			isTransMsgBadReplace					= false;

	// 被C++使用
	/** 发送者名称 */
	public String			name;
	/** 联盟简称 */
	public String			asn;
	/** vip信息 */
	public String			vip;
	/** 系统头像 */
	public String			headPic;
	public int				gmod;
	/** 自定义头像 */
	public int				headPicVer;

	//'@'相关
	public boolean mentioned = false;

	/**
	 * C++创建的对象可能没有默认值赋值，需要补上
	 */
	public void initNullField()
	{
		if (currentText == null)
		{
			currentText = "";
		}
	}

	public MsgItem()
	{

	}

	public UserInfo getUser()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "uid",uid, "name", name);
		UserManager.checkUser(uid, name, 0);
		UserInfo user = UserManager.getInstance().getUser(uid);
		return user;
	}

	public int getMonthCard()
	{
		return getUser().monthCard;
	}

	public String getName()
	{
		return getUser()==null ? "" : getUser().userName;
	}
	
	public String getLang()
	{
		String lang = originalLang;
		if(StringUtils.isEmpty(lang) && StringUtils.isNotEmpty(getUser().lang))
			lang = getUser().lang;
		return lang;
	}

	public int getSrcServerId()
	{
		if (getUser() == null){
			return -1;
		}
		return getUser().crossFightSrcServerId;
	}

	public String getASN()
	{
		return getUser().asn;
	}

	public String getVip()
	{
		return getUser().getVipInfo();
	}
	
	public int getVipLevel()
	{
		return getUser().getVipLevel();
	}
	
	public int getSVipLevel()
	{
		return getUser().getSVipLevel();
	}

    public int getVipframe()
    {
        return getUser().getVipframe();
    }
    
	public String getHeadPic()
	{
		return getUser().headPic;
	}

	public int getGmod()
	{
		return getUser().mGmod;
	}

	public int getHeadPicVer()
	{
		return getUser().headPicVer;
	}

	public void initUserForReceivedMsg(String mailOpponentUid, String mailOpponentName)
	{
		if(lastUpdateTime > TimeManager.getInstance().getCurrentTime())
		{
			LogUtil.printVariables(Log.WARN, LogUtil.TAG_MSG, "invalid lastUpdateTime msg:\n" + LogUtil.typeToString(this));
		}
		String fromUid = ChannelManager.getInstance().getModChannelFromUid(mailOpponentUid);
		if (channelType == DBDefinition.CHANNEL_TYPE_USER && StringUtils.isNotEmpty(fromUid) && !fromUid.equals(uid) && !isSelfMsg())
		{
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "fromUid",fromUid, "mailOpponentName", mailOpponentName,"lastUpdateTime",lastUpdateTime);
			UserManager.checkUser(fromUid, mailOpponentName, lastUpdateTime);
		}
		else
		{
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "fromUid",fromUid, "name", name,"lastUpdateTime",lastUpdateTime);
			UserManager.checkUser(uid, name, lastUpdateTime);
		}
	}

	public void initUserForSendedMsg()
	{
		UserManager.getInstance().getCurrentUser();
	}

	/**
	 * 用于从数据库获取消息
	 */
	public MsgItem(Cursor c)
	{
		try
		{
			_id = c.getInt(c.getColumnIndex(BaseColumns._ID));
			tableVer = c.getInt(c.getColumnIndex(DBDefinition.COLUMN_TABLE_VER));
			sequenceId = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_SEQUENCE_ID));
			uid = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_USER_ID));
			mailId = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_MAIL_ID));
			createTime = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_CREATE_TIME));
			sendLocalTime = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_LOCAL_SEND_TIME));
			post = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_TYPE));
			channelType = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_CHANNEL_TYPE));
			msg = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_MSG));
			translateMsg = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_TRANSLATION));
			originalLang = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_ORIGINAL_LANGUAGE));
			translatedLang = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_TRANSLATED_LANGUAGE));
			sendState = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_STATUS));
			if (sendState < 0)
			{
				if (isRedPackageMessage())
				sendState = UNHANDLE;
				else if (isAudioMessage())
					sendState = VOICE_UNREAD;
			}
			attachmentId = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_ATTACHMENT_ID));
			media = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_MEDIA));
			UserManager.getInstance().getUser(uid);
			isSelfMsg = uid.equals(UserManager.getInstance().getCurrentUserId());
			isNewMsg = false;
			if (TranslateManager.getInstance().hasTranslated(this))
				this.hasTranslated = true;
			else
				this.hasTranslated = false;

			//联盟三部曲暂时处理
			int findI = msg.indexOf("_sanbuqujp20150921",0);
			if (findI > 0) {
				String dialog = msg.substring(0, findI);
				msg = LanguageManager.getLangByKey(dialog);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public ContentValues getContentValues()
	{
		ContentValues cv = new ContentValues();
		cv.put(DBDefinition.COLUMN_TABLE_VER, DBHelper.CURRENT_DATABASE_VERSION);
		cv.put(DBDefinition.CHAT_COLUMN_SEQUENCE_ID, sequenceId);
		cv.put(DBDefinition.CHAT_COLUMN_USER_ID, uid);
		cv.put(DBDefinition.CHAT_COLUMN_MAIL_ID, mailId);
		cv.put(DBDefinition.CHAT_COLUMN_CREATE_TIME, createTime);
		cv.put(DBDefinition.CHAT_COLUMN_LOCAL_SEND_TIME, sendLocalTime);
		cv.put(DBDefinition.CHAT_COLUMN_TYPE, post);
		cv.put(DBDefinition.CHAT_COLUMN_CHANNEL_TYPE, channelType);
		cv.put(DBDefinition.CHAT_COLUMN_MSG, msg);
		cv.put(DBDefinition.CHAT_COLUMN_TRANSLATION, translateMsg);
		cv.put(DBDefinition.CHAT_COLUMN_ORIGINAL_LANGUAGE, originalLang);
		cv.put(DBDefinition.CHAT_COLUMN_TRANSLATED_LANGUAGE, translatedLang);
		if (isRedPackageMessage() && sendState < 0)
			sendState = UNHANDLE;
		cv.put(DBDefinition.CHAT_COLUMN_STATUS, sendState);
		cv.put(DBDefinition.CHAT_COLUMN_ATTACHMENT_ID, attachmentId);
		cv.put(DBDefinition.CHAT_COLUMN_MEDIA, media);
		return cv;
	}

	/**
	 * 用于发送消息
	 */
	public MsgItem(String uidStr, boolean isNewMsg, boolean isSelf, int channelType, int post, String msgStr, int sendLocalTime)
	{
		this.uid = uidStr;
		this.isNewMsg = isNewMsg;
		this.isSelfMsg = isSelf && (post != 100);
		this.channelType = channelType;
		this.post = post;
		this.msg = msgStr;
		if (TranslateManager.getInstance().hasTranslated(this))
			this.hasTranslated = true;
		else
			this.hasTranslated = false;
		this.sendLocalTime = sendLocalTime;
	}

	/**
	 * 用于wrapper假消息
	 */
	public MsgItem(int seqId, boolean isNewMsg, boolean isSelf, int channelType, int post, String uidStr, String msgStr,
			String translateMsgStr, String originalLangStr, int sendLocalTime)
	{
		this.sequenceId = seqId;
		this.isNewMsg = isNewMsg;
		this.isSelfMsg = isSelf && (post != 100);
		this.channelType = channelType;
		this.msg = msgStr;
		this.uid = uidStr;
		this.post = post;
		this.translateMsg = translateMsgStr;
		this.originalLang = originalLangStr;
		this.sendLocalTime = sendLocalTime;

		setExternalInfo();
	}

	public void setExternalInfo()
	{
		if (TranslateManager.getInstance().hasTranslated(this))
		{
			this.hasTranslated = true;
		}
		else
		{
			this.hasTranslated = false;
		}

		if (isSystemHornMsg())
		{
			this.headPic = "guide_player_icon";
		}
	}

	public boolean isEqualTo(MsgItem msgItem)
	{
		if (this.isSelfMsg == msgItem.isSelfMsg && this.msg.equals(msgItem.msg))
			return true;
		return false;
	}

	public boolean isSelfMsg()
	{
		// 系统红包
		if(post == MSG_TYPE_RED_PACKAGE) {
			String[] redPackageInfoArr = attachmentId.split("\\|");
			if (redPackageInfoArr.length == 2) {
				return false;
			}
		}

		isSelfMsg = StringUtils.isNotEmpty(uid) && StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUserId())
				&& uid.equals(UserManager.getInstance().getCurrentUserId()) && post != MSG_TYPE_CHATROOM_TIP && post != MSG_TYPE_USER_AD_TIP;
		return isSelfMsg;
	}

	public boolean isInAlliance()
	{
		return !getASN().equals("");
	}

	public boolean isSystemHornMsg()
	{
		return (isHornMessage() && uid.equals("3000002"));
	}

	public boolean isTranlateDisable()
	{
		if (StringUtils.isNotEmpty(originalLang) && StringUtils.isNotEmpty(TranslateManager.getInstance().disableLang))
		{
			boolean isContainsOriginLang = false;
			if (originalLang.contains(","))
			{
				String langStr[] = originalLang.split(",");
				for (int i = 0; i < langStr.length; i++)
				{
					if (!langStr[i].equals("") && isContainsLang(TranslateManager.getInstance().disableLang, langStr[i]))
					{
						isContainsOriginLang = true;
						break;
					}
				}
			}
			else
			{
				isContainsOriginLang = isContainsLang(TranslateManager.getInstance().disableLang, originalLang);
			}

			if (isContainsOriginLang)
				return true;
		}
		return false;
	}

	private boolean isContainsLang(String disableLang, String lang)
	{
		boolean ret = false;
		if (StringUtils.isNotEmpty(disableLang) && StringUtils.isNotEmpty(originalLang))
		{
			if (disableLang.contains(lang))
				ret = true;
			else
			{
				if (((disableLang.contains("zh-CN") || disableLang.contains("zh_CN") || disableLang.contains("zh-Hans")) && TranslateManager
						.getInstance().isZh_CN(lang))
						|| ((disableLang.contains("zh-TW") || disableLang.contains("zh_TW") || disableLang.contains("zh-Hant")) && TranslateManager
								.getInstance().isZh_TW(lang)))
					ret = true;
			}
		}
		return ret;
	}

	public boolean isCustomHeadImage()
	{
		try
		{
			return getUser().getHeadPicVer() > 0 && getUser().getHeadPicVer() < 1000000 && !getUser().getCustomHeadPic().equals("");
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
			return false;
		}
	}

	public boolean isMediaMsg(){
		if (media != null && StringUtils.isNotEmpty(media) &&
				(ChatServiceController.chat_v2_stickers_on || ChatServiceController.chat_pictures_on)){
			return true;
		}
		return false;
	}

	//是否是语言聊天室的消息
	public boolean isLanguageChatRoomMsg(){
		if (ChatServiceController.chat_language_on &&
				channelType == DBDefinition.CHANNEL_TYPE_CHATROOM &&
				StringUtils.isNotEmpty(roomId) &&
				roomId.contains("custom_LanguageChatRoom_")){
			return true;
		}
		return false;
	}

	/**
	 * 是否是 Stickers表情包
	 */
	public boolean isStickersMsg() {
		if (ChatServiceController.chat_v2_stickers_on && isMediaMsg()) {
			if(media.indexOf("stickers") != -1) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 是否是 自定义图片
	 */
	public boolean isPhotoImageMsg() {
		if (ChatServiceController.chat_pictures_on && isMediaMsg()) {
			if(media.indexOf("photo") != -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断-是否显示跨服id
	 */
	public boolean isShowServerId(){
		UserInfo currInfo = UserManager.getInstance().getCurrentUser();
		boolean isEqualServer = false;
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY){
			isEqualServer = getSrcServerId() > 0 && currInfo != null && currInfo.serverId > 0 && currInfo.crossFightSrcServerId != getSrcServerId();
		}
		else if (isLanguageChatRoomMsg()){
			isEqualServer = currInfo != null && currInfo.crossFightSrcServerId != getSrcServerId();
		}else if(channelType == DBDefinition.CHANNEL_TYPE_CHATROOM ){
			isEqualServer = getSrcServerId() > 0 ;
		}

		return isEqualServer;
	}


	/**
	 * 是否是聊天室的提示消息,显示在中间
	 */
	public boolean isTipMsg()
	{
		return post == MSG_TYPE_CHATROOM_TIP;
	}

	/**
	 * 是否广告提示消息,显示在中间
	 */
	public boolean isUserADMsg()
	{
		return post == MSG_TYPE_USER_AD_TIP;
	}

	public boolean isModMsg()
	{
		return post == MSG_TYPE_MOD;
	}

	public String getAllianceLabel()
	{
		if (isInAlliance())
		{
			return "(" + getASN() + ") ";
		}
		else
		{
			return "";
		}
	}

	public String getVipLabel()
	{
		return getVip() + " ";
	}

	public boolean isAllianceCreate(){
		return post == MSG_TYPE_ALLIANCE_CREATE;
	}
	public boolean isAlllianceMessage(){
		return post == MSG_TYPE_ALLIANCE;
	}

	public boolean isBattleReport()
	{
		return post == 4;
	}

	public boolean isDetectReport()
	{
		return post == 5;
	}

	public boolean isFormationBattle(){
		return post == MSG_TYPE_SHAMO_FORMATION_BATTLE_SHARE;
	}

	public boolean isFBScoutReport(){
		return post == MSG_TYPE_FB_SCOUT_REPORT_SHARE;
	}

	public boolean isAllianceAttackMonsterShare()
	{
        return post == MSG_TYPE_ALLIANCE_ATTACT_SHARE;
	}
	public boolean isAllianceArmsRaceShare()
	{
        return post == MSG_TYPE_ALLIANCE_ARMS_RACE_SHARE;
	}
	public boolean isEnemyPutDownPointShare()
	{
		return post == MSG_TYPE_ENEMY_PUTDOWN_POINT_SHARE;
	}
	public boolean isAnnounceInvite()
	{
		return post == 3;
	}

	public boolean isHornMessage()
	{
		return post == 6;
	}

	public boolean isEquipMessage()
	{
		return post == 7;
	}

	public boolean isAllianceJoinMessage()
	{
		return post == MSG_TYPE_ALLIANCE_JOIN;
	}

	public boolean isRallyMessage()
	{
		return post == MSG_TYPE_ALLIANCE_RALLY;
	}

	public boolean isLotteryMessage()
	{
		return post == MSG_TYPE_LOTTERY_SHARE;
	}

	public boolean isSevenDayMessage(){
		return post == MSG_TYPE_SEVEN_DAY;
	}

	public boolean isAllianceGroupBuyMessage(){
		return post == MSG_TYPE_ALLIANCE_GROUP_BUG_SHARE;
	}

	public boolean isCreateEquipMessage(){
		return post == MSG_TYPE_CREATE_EQUIP_SHARE;
	}

	public boolean isNewCreateEquipMessage(){
		return post == MSG_TYPE_NEW_CREATE_EQUIP_SHARE;
	}

	public boolean isMissleReport(){
		return post == MSG_TYPE_MISSILE_SHARE;
	}

	public boolean isGiftMailShare(){
		return post == MSG_TYPE_GIFT_MAIL_SHARE;
	}

	public boolean isFavourPointShare(){
		return post == MSG_TYPE_FAVOUR_POINT_SHARE;
	}

	public boolean isWoundedShare(){
		return post == MSG_TYPE_WOUNDED_SHARE;
	}

	public boolean isEquipmentMedalShare()
	{
		return post == MSG_TYPE_MEDAL_SHARE;
	}

	public boolean isAllianceCommonShare(){
		return post == MSG_TYPE_ALLIANCE_COMMON_SHARE;
	}

	public boolean isActivityHeroShare()
	{
		return post == MSG_TYPE_FB_ACTIVITY_HERO_SHARE;
	}

	public boolean isFBFormationShare()
	{
		return post == MSG_TYPE_FB_FORMATION_SHARE;
	}

	public boolean isShamoInhesionShare()
	{
		return post == MSG_TYPE_SHAMO_INHESION_SHARE;
	}

	public boolean isScienceMaxShare(){
		return post == MSG_TYPE_SCIENCE_MAX__SHARE;
	}

	public boolean isSevenDayNewShare(){
		return post == MSG_TYPE_SEVENDAY_NEW_SHARE;
	}
	public boolean isCordinateShareMessage()
	{
		return post == MSG_TYPE_COR_SHARE;
	}
	
	public boolean isAllianceTreasureMessage()
	{
		return post == MSG_TYPE_ALLIANCE_TREASURE;
	}

	public boolean isAllianceHelpMessage()
	{
		return post == MSG_TYPE_ALLIANCEHELP;
	}
	public boolean isAllianceOfficerMessage()
	{
		return post == MSG_TYPE_ALLIANCE_OFFICER;
	}
	public boolean isAudioMessage()
	{
		return post == MSG_TYPE_AUDIO;
	}

	public boolean isAreaMsgTip(){return post == MSG_TYPE_AREA_MSG_TIP;}

	/**
	 * 判断是否是系统消息
	 */
	public boolean isSystemMessage()
	{
		return post > 0 && !isTipMsg() && !isUserADMsg() && !isModMsg() && !isAudioMessage();
	}

	public boolean isAllianceTaskMessage()
	{
		return post == MSG_TYPE_ALLIANCETASK_SHARE;
	}

	public boolean isAllianceMonthCardBoxMessage()
	{
		return post == MSG_TYPE_ALLIANCE_MONTHCARDBOX;
	}

	public boolean isRedPackageMessage()
	{
		return post == MSG_TYPE_RED_PACKAGE;
	}

	//已领取
	public boolean isReadRedPackageMessage()
	{
		if (StringUtils.isNotEmpty(attachmentId) && !isRedPackageFinish()) {
			return false;
		}

		return true;
	}

	private Date getSendUtcDate()
	{
		int t = createTime > 0 ? createTime : sendLocalTime;
		//只用于自由城建修改显示时间
		if(this.channelType == DBDefinition.CHANNEL_TYPE_USER){
			t = t + 3600*2;
		}
		Date date = new Date((long) t * 1000);
		return date;
	}

	public String getSendTime()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
		return formatter.format(getSendUtcDate());
	}

	public String getSendTimeYMD()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		return formatter.format(getSendUtcDate());
	}

	public String getSendTimeHM()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
		return formatter.format(getSendUtcDate());
	}

	public String getSendTimeToShow()
	{
//		if (TimeManager.getInstance().isToday(createTime))
//		{
//			return getSendTimeHM();
//		}

		return getSendTimeYMD();
	}

	public boolean hasTranslation()
	{
		return StringUtils.isNotEmpty(translateMsg) && !translateMsg.startsWith("{\"code\":{");
	}

	public boolean canShowTranslateMsg()
	{
		return StringUtils.isNotEmpty(msg) && !StringUtils.isNumeric(msg) && TranslateManager.getInstance().isTranslateMsgValid(this)
				&& (!isTranlateDisable() || isTranslatedByForce) && !isOriginalSameAsTargetLang() && !isOriginalLangByForce;
	}

	public boolean isOriginalSameAsTargetLang()
	{
		boolean isSame = false;
//		if (StringUtils.isNotEmpty(originalLang)
//				&& StringUtils.isNotEmpty(ConfigManager.getInstance().gameLang)
//				&& (ConfigManager.getInstance().gameLang.equals(originalLang) || TranslateManager.getInstance().isSameZhLang(originalLang,
//						ConfigManager.getInstance().gameLang)))
//			isSame = true;
		return isSame;
	}

	public boolean isVersionInvalid()
	{
		if (post > MSG_TYPE_MAX_VALUE && !isTipMsg() && !isUserADMsg() && !isModMsg() && !isAreaMsgTip() && post != MAIL_MOD_PERSON)
			return true;
		return false;
	}

	public ChatChannel getChatChannel()
	{
		ChatChannel channel = null;
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
			channel = ChannelManager.getInstance().getCountryChannel();
		else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
			channel = ChannelManager.getInstance().getAllianceChannel();
		return channel;
	}

	public void handleRedPackageFinishState()
	{
		if (!isRedPackageMessage())
			return;
		if ((sendState == UNHANDLE||sendState == HANDLED || sendState == NONE_MONEY) && isRedPackageFinish())
		{
			sendState = FINISH;
			ChatChannel channel = getChatChannel();
			if (channel != null)
				DBManager.getInstance().updateMessage(this, channel.getChatTable());
		}
	}

	public boolean isRedPackageFinish()
	{
		if (!isRedPackageMessage())
			return false;
		if (createTime + ChatServiceController.red_package_during_time * 60 * 60 < TimeManager.getInstance().getCurrentTime())
			return true;
		return false;
	}

	public boolean isGetSystemRedPackage()
	{
		if (!isRedPackageMessage())
			return false;
		if (UserManager.getInstance().getCurrentUser().joinAllianceTime + ChatServiceController.system_red_package_Limit_time * 60 * 60 < TimeManager.getInstance().getCurrentTime())
			return true;
		return false;
	}

	public boolean isKingMsg()
	{
		if (StringUtils.isNotEmpty(uid) && StringUtils.isNotEmpty(ChatServiceController.kingUid)
				&& ChatServiceController.kingUid.equals(uid))
			return true;
		return false;
	}

	public int getMsgItemType(Context context)
	{
		if (firstNewMsgState == 1 || firstNewMsgState == 2)
		{
			return MSGITEM_TYPE_NEW_MESSAGE_TIP;
		}
		else
		{
			String replacedEmoj = StickManager.getPredefinedEmoj(msg);
			if (replacedEmoj != null)
			{
				return getPicType(context, replacedEmoj);
			}
			else
			{
				if (isRedPackageMessage())
					return MSGITEM_TYPE_REDPACKAGE;
				else if (isAudioMessage())
					return MSGITEM_TYPE_AUDIO;
				else
					return getMessageType();
			}
		}
	}

	public int getMessageType()
	{
		if (isTipMsg()||isUserADMsg()) {
			return MSGITEM_TYPE_CHATROM_TIP;
		}
		else if(isShareCommentMsg()) {
			return MSGITEM_TYPE_MESSAGE_COMMENT;
		}
		else {
			return MSGITEM_TYPE_MESSAGE;
		}
	}

	public int getPicType(Context context, String fileName)
	{
		if (fileName == null)
			return -1;
		int id = ResUtil.getId(context, "drawable", fileName);
		if (id == 0)
			return -1;
		if (context.getString(id).endsWith(".gif"))
		{
			return MSGITEM_TYPE_GIF;
		}
		else
		{
			return MSGITEM_TYPE_PIC;
		}
	}
	
	public boolean isSVIPMsg()
	{
		if (getUser() != null)
		return getUser().isSVIP();
		return false;
	}
	public boolean isVIPMsg()
	{
		if (getUser() != null)
			return getUser().isVIP();
		return false;
	}
	public boolean isNotInRestrictList()
	{
		return (!isSystemMessage() && !UserManager.getInstance().isInRestrictList(uid, UserManager.BAN_LIST))
				|| (isHornMessage() && !UserManager.getInstance().isInRestrictList(uid, UserManager.BAN_NOTICE_LIST));
	}

	public boolean isInRestrictList()
	{
		return (!isSystemMessage() && UserManager.getInstance().isInRestrictList(uid, UserManager.BAN_LIST))
				|| (isHornMessage() && UserManager.getInstance().isInRestrictList(uid, UserManager.BAN_NOTICE_LIST));
	}
	
	public String getAllianceTreasureInfo(int index)
	{
		if(isAllianceTreasureMessage())
		{
			if(StringUtils.isNotEmpty(attachmentId))
			{
				String[] arr = attachmentId.split("\\|");
				if(arr.length == 3 && index<3 && index>=0)
				{
					return arr[index];
				}
			}
		}
		return "";
	}
	public void setVoiceRecordReadState()
	{
		if (isAudioMessage() && !isSelfMsg())
		{
			sendState = VOICE_READ;
			ChatChannel channel = ChannelManager.getInstance().getAllianceChannel();
			if (channel != null)
				DBManager.getInstance().updateMessage(this, channel.getChatTable());
		}
	}

	public boolean isNeedParseAttachmentId(){
		return false;
	}

	public boolean isShareCommentMsg(){
		// if(ChatServiceController.share_optimization && isNotEmpty(shareComment)
		// 		&& isFavourPointShare()){
		// 	return true;
		// }
		return false;
	}

	public boolean isOutOwner() {
		return isSelfMsg();
	}

	public String parseAttachmentId(TextView textView, String str, boolean isTranslated,boolean isForNewChat){
		String attachmentIdStr = this.attachmentId;
		try {
			if (this.isAllianceCreate() || this.isAlllianceMessage() || this.isAnnounceInvite()
					) {

			} else if (this.isBattleReport() || this.isDetectReport() || this.isFBScoutReport()) {
				if (StringUtils.isNotEmpty(attachmentIdStr)) {
					String[] attachmentIds = attachmentIdStr.split("__");
					String reportUid = attachmentIds[0];
					if (attachmentIds.length > 1) {
						//
						String[] dialogArray = attachmentIds[1].split("\\|");
						if (dialogArray.length > 0) {
							String dialogKey = dialogArray[0];
							if (StringUtils.isNotEmpty(dialogKey)) {
								str = LanguageManager.getLangByKey(dialogKey);
							} else {
								str = LanguageManager.getLangByKey("111660");
							}

							if (dialogArray.length == 2) {
								String name1 = dialogArray[1];
								if(name1.equals("200637") || name1.equals("200639") || name1.equals("200640")){
									name1 = LanguageManager.getLangByKey(name1);
								}
								str = LanguageManager.getLangByKey(dialogKey, name1);
							} else if (dialogArray.length == 3) {
								String name1 = dialogArray[1];
								String name2 = dialogArray[2];
								if(name2.equals("200637")){
									name1 = "(" + name1 + ")";
									name2 = LanguageManager.getLangByKey(name2);
									str = LanguageManager.getLangByKey(dialogKey, name1.concat(name2));
								}else{
									str = LanguageManager.getLangByKey(dialogKey, name1, name2);
								}
							}
						}
					}
				}
			}
			//6,7,8,9无用
			else if (this.isLotteryMessage()) {

			} else if (this.isAllianceTaskMessage() || this.isAllianceTreasureMessage()) {

			} else if (this.isAllianceMonthCardBoxMessage()) {

			} else if (this.isSevenDayMessage()) {
				if(StringUtils.isNotEmpty(attachmentIdStr)) {
					String[] attachmentIds = attachmentIdStr.split("\\|");
					String dialogKey = attachmentIds[0];
					str = LanguageManager.getLangByKey(dialogKey, LanguageManager.getLangByKey(attachmentIds[1]));
				}
			} else if (this.isMissleReport()) {

			} else if (this.isAllianceGroupBuyMessage()) {

			} else if (this.isGiftMailShare()) {

			} else if (this.isEquipMessage() || this.isNewCreateEquipMessage()) {
				String msgStr = this.attachmentId;
				String equipName = "";
				if (isNotEmpty(msgStr)) {
					String[] equipInfo = msgStr.split("\\|");
					if (equipInfo.length == 2) {
						equipName = LanguageManager.getLangByKey(equipInfo[1]);
					}
				}
				str = LanguageManager.getLangByKey(LanguageKeys.TIP_EQUIP_SHARE, equipName);
			} else if (this.isFavourPointShare()) {
				String commentStr = "";
				if (isTranslated) {
					if (this.translateMsg.equals("90200021")) {
						commentStr = LanguageManager.getLangByKey("90200021");
					} else {
						commentStr = this.translateMsg;
					}
					this.translateMsg = commentStr;
				} else {

					if (this.shareComment.equals("90200021")) {
						commentStr = LanguageManager.getLangByKey("90200021");
                    } else if(this.shareComment.equals("90200080")) {
                        commentStr = LanguageManager.getLangByKey("90200080");
                    }
                    else {
						commentStr = this.shareComment;
					}
				}
				if (isNotEmpty(attachmentIdStr)) {
					String[] taskInfo = attachmentIdStr.split("\\|");
					if (isNotEmpty(taskInfo[0]) && taskInfo.length >= 4) {
						String nomalStr = "(#" + taskInfo[1] + " " + taskInfo[2] + ":" + taskInfo[3] + ")";
						if (taskInfo[0].equals("81000312") || taskInfo[0].equals("81000327") || taskInfo[0].equals("90200080")) {
							String msgStr = this.msg;
							if (this.msg.equals("145538")) {
								msgStr = LanguageManager.getLangByKey(this.msg);
							}
							str = LanguageManager.getLangByKey(taskInfo[0], msgStr, nomalStr);
						} else if (taskInfo[0].equals("81000326")) {
							str = LanguageManager.getLangByKey(taskInfo[0], taskInfo[4], taskInfo[5], nomalStr);
						} else if (taskInfo[0].equals("81000324") || taskInfo[0].equals("81000325")) {
							String msgStr = LanguageManager.getLangByKey(taskInfo[5]);
							str = LanguageManager.getLangByKey(taskInfo[0], taskInfo[4], msgStr, nomalStr);
                        } else {
                            str = nomalStr;
                        }
					}
				}
				if (isForNewChat)
				{

				}
				else if (this.isShareCommentMsg()) {
					str = commentStr;
				}

			} else if (this.isWoundedShare()) {

			} else if (this.isEquipmentMedalShare()) {
                String[] attachmentIds = attachmentIdStr.split("\\|");
                if (attachmentIds.length > 1 && StringUtils.isNotEmpty(attachmentIds[1])) {
                    str = LanguageManager.getLangByKey(attachmentIds[0],attachmentIds[1]);
                }else{
                    str = this.msg;
                }
			} else if (this.isShamoInhesionShare()) {

			}
//			else if (this.isGWSysTips()) {
//				String[] attachments = attachmentIdStr.split("__");
//				String[] attachmentIds = attachments[1].split("\\|");
//				if (attachmentIds.length == 0)
//					str = this.msg;
//				String dialogKey = attachmentIds[0];
//				String pngName = "";
//				int cityOrFlagId = 0;
//				if (attachmentIds.length == 3) {
//					str = LanguageManager.getLangByKey(dialogKey, attachmentIds[1], pngName);
//				} else if (attachmentIds.length == 4) {
//					if (NumberUtils.isNumber(attachmentIds[2])) {
//						cityOrFlagId = Integer.parseInt(attachmentIds[2]);
//					}
//					if (cityOrFlagId > 1000) {
//						String cityName = LanguageManager.getLangByKey("82000992", String.valueOf(cityOrFlagId - 1000));
//						str = LanguageManager.getLangByKey(dialogKey, attachmentIds[1], cityName, attachmentIds[3]);
//					} else {
//						str = LanguageManager.getLangByKey(dialogKey, attachmentIds[1], pngName, attachmentIds[3]);
//					}
//				}
//			}
//			else if (this.isViewQuestionActivity()) {
//				str = LanguageManager.getLangByKey(attachmentIdStr);
//			} else if (this.isNewsCenterShare()) {
//				String newsIdStr = "";
//				String titleParams = "";
//				String[] attachmentIDArray = attachmentIdStr.split("_", 3); //只分割两次,防止将名字分割了
//				if (attachmentIDArray.length == 3) {
//					newsIdStr = attachmentIDArray[1];
//					titleParams = attachmentIDArray[2];
//				}
//				str = JniController.getInstance().excuteJNIMethod("getNewsCenterShowMsg", new Object[]{newsIdStr, titleParams});
//			}
			else if(isFormationBattle()){
				String []attachmentIds = attachmentIdStr.split("_",2);
				if(attachmentIds.length == 2){
					String []dialogs = attachmentIds[1].split("\\|");
					if(dialogs.length == 2) {
						str = LanguageManager.getLangByKey(dialogs[0], dialogs[1]);
					}else{
						str = this.msg;
					}
				}
			}
			else if (isFBFormationShare())
			{
				String[] attachmentIds = attachmentIdStr.split("\\|");
				if (attachmentIds.length > 1 && StringUtils.isNotEmpty(attachmentIds[1])) {
					str = LanguageManager.getLangByKey(attachmentIds[0],attachmentIds[1]);
				}else{
					str = this.msg;
				}
			}
			else if(this.isScienceMaxShare()){
				String dialogKey = "";
				String scienceType = "";
				String titleParams = "";
				String[] attachmentIDArray = attachmentIdStr.split("__"); //只分割两次,防止将名字分割了
				if (attachmentIDArray.length > 1) {
					scienceType = attachmentIDArray[0];
					dialogKey = attachmentIDArray[1];
				}
				String nameStr = JniController.getInstance().excuteJNIMethod("getNameById",new Object[]{scienceType});
				String msg = JniController.getInstance().excuteJNIMethod("getScienceSharedMsg", new Object[]{scienceType});
				str = LanguageManager.getLangByKey(dialogKey,nameStr,msg);
			} else if(this.isAllianceCommonShare() || this.isSevenDayNewShare() || this.isAllianceAttackMonsterShare()){
				if(StringUtils.isNotEmpty(attachmentIdStr)) {
					String[] attachmentIds = attachmentIdStr.split("\\|");
					String dialogKey = attachmentIds[0];
					if(attachmentIds.length == 2) {
						str = LanguageManager.getLangByKey(dialogKey, LanguageManager.getLangByKey(attachmentIds[1]));
					}else if(attachmentIds.length == 3) {
						str = LanguageManager.getLangByKey(dialogKey, attachmentIds[1], LanguageManager.getLangByKey(attachmentIds[2]));
					}else{
						str = this.msg;
					}
				}
			} else if(this.isAllianceArmsRaceShare()){
				if(StringUtils.isNotEmpty(attachmentIdStr)) {
					String[] attachmentIds = attachmentIdStr.split("\\|");
					String dialogKey = attachmentIds[0];
					if(attachmentIds.length == 2) {
						str = LanguageManager.getLangByKey(dialogKey, attachmentIds[1]);
					}else{
						str = this.msg;
					}
				}
			}
			else if(this.isEnemyPutDownPointShare()){
				if(StringUtils.isNotEmpty(attachmentIdStr)) {
					String[] attachmentIds = attachmentIdStr.split("\\|");
					String dialogKey = attachmentIds[0];
					if(attachmentIds.length >= 3)
					{
						str = LanguageManager.getLangByKey(dialogKey, attachmentIds[2]);
					}else{
						str = this.msg;
					}
				}
			}


			else if (this.isVersionInvalid())
			{
				str = LanguageManager.getLangByKey(LanguageKeys.MSG_VERSION_NO_SUPPORT);
			}
			if ((StringUtils.isEmpty(str) && ChatServiceController.getCurrentChannelType() < DBDefinition.CHANNEL_TYPE_USER && this.isSystemMessage()
					&& !this.isHornMessage())) {
				if (StringUtils.isEmpty(attachmentIdStr)) {
					str = this.msg;
				}
				String[] attachments = attachmentIdStr.split("__");
				String[] attachmentIds = attachments[attachments.length - 1].split("\\|");
				if (attachmentIds.length == 0 || (attachmentIdStr.equals(attachmentIds[0]) && !StringUtils.isNumeric(attachmentIdStr))) {
					str = this.msg;
				}else {
					String dialogKey = attachmentIds[0];
					String msg = "";
					if (attachmentIds.length == 1) {
						msg = LanguageManager.getLangByKey(dialogKey);
					} else if (attachmentIds.length == 2) {
						msg = LanguageManager.getLangByKey(dialogKey, attachmentIds[1]);
					} else if (attachmentIds.length == 3 ) {
						msg = LanguageManager.getLangByKey(dialogKey, attachmentIds[1], attachmentIds[2]);
					} else if (attachmentIds.length == 4 ) {
						msg = LanguageManager.getLangByKey(dialogKey, attachmentIds[1], attachmentIds[2], attachmentIds[3]);
					} else if (attachmentIds.length == 5) {
						msg = LanguageManager.getLangByKey(dialogKey, attachmentIds[1], attachmentIds[2], attachmentIds[3], attachmentIds[4]);
					}
					str = msg;
					this.translatedLang = ConfigManager.getInstance().gameLang;
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_CORE, "msg_dialog", msg);
				}
			}
		}catch (Exception e){

		}finally {
			if(StringUtils.isEmpty(str)) {
				str =StringUtils.isNotEmpty(this.translateMsg) ? this.translateMsg : this.msg;
			}
			return str;
		}
	}

	public boolean isSystemMessageByKey(){
		return (post >0 && post<=10 && ChatServiceController.new_system_message)
				|| (post >10 && post <= 20&& ChatServiceController.new_system_message)
				|| (post >20 && ChatServiceController.new_system_message)
				|| isScienceMaxShare() || isAllianceCommonShare() || isSevenDayNewShare() || isAllianceAttackMonsterShare() || isAllianceArmsRaceShare()|| isEnemyPutDownPointShare();
	}
}
