package com.chatsdk.model.mail;

import android.content.ContentValues;
import android.database.Cursor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.chatsdk.controller.JniController;
import com.chatsdk.model.ChannelListItem;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MailIconName;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.TimeManager;
import com.chatsdk.model.TranslateManager;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.db.DBHelper;
import com.chatsdk.util.LogUtil;

import org.apache.commons.lang.StringUtils;

public class MailData extends ChannelListItem
{
	private int						type						= -1;
	private int						createTime;
	/** 是否未读，大于1则是 */
	private int						status;
	private int						reply;
	private int						rewardStatus;
	/** 需要读语言文件 */
	private int						itemIdFlag;
	/** 0未保存,1保存,2删除保存过 */
	private int						save;
	private String					uid							= "";
	private String					title						= "";
	private String					contents					= "";
	private String					fromName					= "";
	private String					fromUid						= "";
	private String					rewardId					= "";
	public  String					translationId				= ""; //用来存储邮件的发送者的语言
	private int						mbLevel						= 0;
	public int                      flag                         = 0;
	public String			       channelId					= "";
	public long				       failTime                   = 0;

	private transient String		version						= "";
	public transient String			pic							= "";

	// 运行时属性
	public transient int			tabType						= -1;
	public transient int			isFreeAll						= -1;
	public transient int			catchLeader						= -1;
	public transient int			captureHeroId				= 0;
	public transient int			captureHeroLevel			= 0;
	public transient boolean		issamplereport				= false;
//	public int getIsFreeAll() {
//		return isFreeAll;
//	}
//
//	public void setIsFreeAll(int isFreeAll) {
//		this.isFreeAll = isFreeAll;
//	}
//
//	public int getCatchLeader() {
//		return catchLeader;
//	}
//
//	public void setCatchLeader(int catchLeader) {
//		this.catchLeader = catchLeader;
//	}

	public transient boolean		isAtk;
	public transient int			parseVersion				= -1;
	public transient boolean		needParseByForce			= false;
	/** 邮件是否打开过，对于复杂邮件，如果打开过就不再进行json解析 */
	public transient boolean		hasMailOpend				= false;
	public transient boolean		isKnightMail				= false;
	public transient boolean		isBattlegameMail				= false;
	public transient boolean		isKnightActivityFinishMail	= false;
	public transient boolean		hasParseForKnight			= false;
	public transient boolean		isArenagameMail				= false; //末日角斗场

	public transient boolean		isShaMogameMail				= false; //金字塔争夺战

	// 显示属性
	public transient String			nameText					= "";
	public transient String			contentText					= "";
	public transient String			mailIcon					= "";
	public transient String			timeText					= "";
	//public transient String			failTimeText				= "";
	public transient boolean		usePersonalPic				= false;
	public transient ChatChannel	channel						= null;
	

	private static final int		PARSE_VERSION_BASIS			= 1;
	private static final int		CURRENT_PARSE_VERSION		= PARSE_VERSION_BASIS;

	public MailData()
	{
	}

	@JSONField(serialize = false)
	public boolean isParseVersionOld()
	{
		return parseVersion < CURRENT_PARSE_VERSION;
	}

	public void setNeedParseByForce(boolean needParseByForce)
	{
		this.needParseByForce = needParseByForce;
	}

	public void updateParseVersion()
	{
		parseVersion = CURRENT_PARSE_VERSION;
	}

	public void setMailData(MailData mailData)
	{
		type = mailData.type;
		createTime = mailData.createTime;
		status = mailData.status;
		reply = mailData.reply;
		rewardStatus = mailData.rewardStatus;
		itemIdFlag = mailData.itemIdFlag;
		save = mailData.save;
		mbLevel = mailData.mbLevel;
		uid = mailData.uid;
		title = mailData.title;
		if (type == MailManager.MAIL_PERSIDENT_SEND){
			title = LanguageManager.getLangByKey("81000083");
		}
		contents = mailData.contents;
		fromName = mailData.fromName;
		fromUid = mailData.fromUid;
		rewardId = mailData.rewardId;
		version = mailData.version;
		channelId = mailData.channelId;
		parseVersion = mailData.parseVersion;
		translationId = mailData.translationId;
		needParseByForce = mailData.needParseByForce;
		hasMailOpend = mailData.hasMailOpend;
		isKnightMail = mailData.isKnightMail;
		isKnightActivityFinishMail = mailData.isKnightActivityFinishMail;
		hasParseForKnight = mailData.hasParseForKnight;
		failTime = mailData.failTime;
		issamplereport = mailData.issamplereport;
	}

	public void setMailDealStatus()
	{
	}

	/**
	 * 用于从数据库获取消息
	 */
	public MailData(Cursor c)
	{
		try
		{
			type = c.getInt(c.getColumnIndex(DBDefinition.MAIL_TYPE));
			createTime = c.getInt(c.getColumnIndex(DBDefinition.MAIL_CREATE_TIME));
			status = c.getInt(c.getColumnIndex(DBDefinition.MAIL_STATUS));
			reply = c.getInt(c.getColumnIndex(DBDefinition.MAIL_REPLY));
			rewardStatus = c.getInt(c.getColumnIndex(DBDefinition.MAIL_REWARD_STATUS));
			itemIdFlag = c.getInt(c.getColumnIndex(DBDefinition.MAIL_ITEM_ID_FLAG));
			save = c.getInt(c.getColumnIndex(DBDefinition.MAIL_SAVE_FLAG));
			uid = c.getString(c.getColumnIndex(DBDefinition.MAIL_ID));
			channelId = c.getString(c.getColumnIndex(DBDefinition.MAIL_CHANNEL_ID));
			title = c.getString(c.getColumnIndex(DBDefinition.MAIL_TITLE));
			contents = c.getString(c.getColumnIndex(DBDefinition.MAIL_CONTENTS));
			fromName = c.getString(c.getColumnIndex(DBDefinition.MAIL_FROM_NAME));
			fromUid = c.getString(c.getColumnIndex(DBDefinition.MAIL_FROM_USER_ID));
			rewardId = c.getString(c.getColumnIndex(DBDefinition.MAIL_REWARD_ID));
			if (type == MailManager.MAIL_SYSUPDATE)
				version = fromUid;
			nameText = c.getString(c.getColumnIndex(DBDefinition.MAIL_TITLE_TEXT));
			contentText = c.getString(c.getColumnIndex(DBDefinition.MAIL_SUMMARY));
			translationId = c.getString(c.getColumnIndex(DBDefinition.MAIL_LANGUAGE));
			parseVersion = c.getInt(c.getColumnIndex(DBDefinition.PARSE_VERSION));
			mbLevel = c.getInt(c.getColumnIndex(DBDefinition.MAIL_REWARD_LEVEL));
			flag = c.getInt(c.getColumnIndex(DBDefinition.MAIL_FLAG));
			failTime = c.getLong(c.getColumnIndex(DBDefinition.MAIL_FAIL_TIME));
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	@JSONField(serialize = false)
	public ContentValues getContentValues()
	{
		ContentValues cv = new ContentValues();
		cv.put(DBDefinition.COLUMN_TABLE_VER, DBHelper.CURRENT_DATABASE_VERSION);
		cv.put(DBDefinition.MAIL_TYPE, type);
		cv.put(DBDefinition.MAIL_CREATE_TIME, createTime);
		cv.put(DBDefinition.MAIL_STATUS, status);
		cv.put(DBDefinition.MAIL_REPLY, reply);
		cv.put(DBDefinition.MAIL_REWARD_STATUS, rewardStatus);
		cv.put(DBDefinition.MAIL_ITEM_ID_FLAG, itemIdFlag);
		cv.put(DBDefinition.MAIL_SAVE_FLAG, save);
		cv.put(DBDefinition.MAIL_ID, uid);
		cv.put(DBDefinition.MAIL_CHANNEL_ID, channelId);
		cv.put(DBDefinition.MAIL_TITLE, title);
		cv.put(DBDefinition.MAIL_CONTENTS, contents);
		cv.put(DBDefinition.MAIL_FROM_NAME, fromName);
		cv.put(DBDefinition.MAIL_FROM_USER_ID, fromUid);
		cv.put(DBDefinition.MAIL_REWARD_ID, rewardId);
		cv.put(DBDefinition.MAIL_TITLE_TEXT, nameText);
		cv.put(DBDefinition.MAIL_SUMMARY, contentText);
		cv.put(DBDefinition.MAIL_LANGUAGE, translationId);
		cv.put(DBDefinition.PARSE_VERSION, parseVersion);
		cv.put(DBDefinition.MAIL_REWARD_LEVEL, mbLevel);
		cv.put(DBDefinition.MAIL_FLAG, flag);
		cv.put(DBDefinition.MAIL_FAIL_TIME, failTime);
		return cv;
	}

	public boolean canDelete()
	{
		boolean ret = true;
		if ((!rewardId.equals("") && rewardStatus == 0) || save == 1)
			ret = false;
		return ret;
	}

	public void parseContents()
	{
		if (type == MailManager.MAIL_SYSUPDATE)
			version = fromUid;
		parseMailCellIcon();
		//timeText = TimeManager.getReadableTime(createTime);
		//failTimeText = TimeManager.getTimeFormatWithFailTime(failTime);
		if (needParseContent())
		{
			parseMailTypeTab();
			parseMailName();
			parseContentText();
//			language = ConfigManager.getInstance().gameLang;
			updateParseVersion();
		}
	}

	@JSONField(serialize = false)
	public boolean isUserMail()
	{
		if (type <= MailManager.MAIL_USER || type == MailManager.MAIL_Alliance_ALL || type == MailManager.CHAT_ROOM
				|| type == MailManager.MAIL_MOD_SEND || type == MailManager.MAIL_MOD_PERSONAL)
			return true;
		return false;
	}

	public void parseMailTypeTab()
	{
		if (type >= 0 && type < MailManager.MAIL_TYPE_COUNT)
		{
			if (type == MailManager.MAIL_BATTLE_REPORT || type == MailManager.MAIL_DETECT || type == MailManager.MAIL_DETECT_REPORT|| type == MailManager.Mail_DETECT_ARENA|| type == MailManager.Mail_DETECT_REPORT_ARENA
					|| type == MailManager.MAIL_ENCAMP || type == MailManager.MAIL_WORLD_BOSS|| type == MailManager.Mail_BORDERFIGHT
					|| type == MailManager.Mail_BATTLEGAMEFIGHT|| type == MailManager.Mail_SHAMOGAMEFIGHT|| type == MailManager.Mail_CASTLEGAMEFIGHT
					|| type == MailManager.Mail_ARENAGAMEFIGHT|| type == MailManager.Mail_KNIGHTGAMEFIGHT|| type == MailManager.Mail_SHAMOKNIGHTGAMEFIGHT
					|| type == MailManager.Mail_GAMEFIGHTACTIVITY|| type == MailManager.MAIL_SHAMOGOLDDIGEGER|| type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYER_RESOURCE
					|| type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYERCITY || type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_THRONE || type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_ALLIANCETOWER|| type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_EXPEDITION
					|| type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_PYRAMID|| type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_MOBILIZATION|| type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_DESERTGRAVE
					|| type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_CASTLE
					|| type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_NOTCOVERDOMAIN
					|| type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_FIGHTDOMAINMARL
					|| type == MailManager.MAIL_NEWVERSION_ARENA_THRONE || type == MailManager.MAIL_NEWVERSION_ARENA_TREBUCHET
					|| type == MailManager.MAIL_WORLD_MONSTER_ELITE || type == MailManager.MAIL_SC_FINGHT_RUINS || type == MailManager.MAIL_SC_FINGHT_CITY
					|| type == MailManager.MAIL_TYPE_ALLIANCE_BOSS || type == MailManager.MAIL_TYPE_ALLIANCE_BOSS_TREASURE
					)
			{
				tabType = MailManager.MAILTAB_FIGHT;
			}
			else if (type == MailManager.ALL_SERVICE)
			{
				tabType = MailManager.MAILTAB_STUDIO;
			}
			else if (type == MailManager.MAIL_FRESHER || type == MailManager.MAIL_SYSNOTICE || type == MailManager.MAIL_SYSUPDATE)
			{
				tabType = MailManager.MAILTAB_NOTICE;
			}
			else if (type <= MailManager.MAIL_USER || type == MailManager.MAIL_Alliance_ALL/* || type == MailManager.CHAT_ROOM*/)
			{
				tabType = MailManager.MAILTAB_USER;
			}
			else if (type == MailManager.MAIL_MOD_SEND || type == MailManager.MAIL_MOD_PERSONAL)
			{
				if (UserManager.getInstance().getCurrentUser().mGmod == 2 || UserManager.getInstance().getCurrentUser().mGmod == 5)
				{
					tabType = MailManager.MAILTAB_MOD;
				}
				else
				{
					tabType = MailManager.MAILTAB_USER;
				}
			}
			else
			{
				tabType = MailManager.MAILTAB_SYSTEM;
			}
		}
	}

	private void parseMailCellIcon()
	{
		switch (type)
		{
			case MailManager.MAIL_SYSTEM:
			case MailManager.Mail_ACTIVITY_NEW:
			case MailManager.MAIL_NEWACTIVITY_PERSONALARM:
			case MailManager.MAIL_NEWACTIVITY_ALLIANCERAM:
			case MailManager.MAIL_NEWACTIVITY_GREATKING:
			case MailManager.MAIL_NEWACTIVITY_DESERT:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_SYSTEM);
				break;
			case MailManager.MAIL_BATTLE_REPORT:
			case MailManager.Mail_BORDERFIGHT:
			case MailManager.Mail_BATTLEGAMEFIGHT:
			case MailManager.Mail_SHAMOGAMEFIGHT:
			case MailManager.Mail_CASTLEGAMEFIGHT:
			case MailManager.Mail_ARENAGAMEFIGHT:
			case MailManager.Mail_KNIGHTGAMEFIGHT:
			case MailManager.Mail_SHAMOKNIGHTGAMEFIGHT:
			case MailManager.Mail_GAMEFIGHTACTIVITY:
			case MailManager.MAIL_SHAMOGOLDDIGEGER:
			case MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYERCITY:
			case MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYER_RESOURCE:
			case MailManager.MAIL_NEWVERSION_BATTLEREPORT_THRONE:
			case MailManager.MAIL_NEWVERSION_BATTLEREPORT_ALLIANCETOWER:
			case MailManager.MAIL_NEWVERSION_BATTLEREPORT_EXPEDITION:
			case MailManager.MAIL_NEWVERSION_BATTLEREPORT_PYRAMID:
			case MailManager.MAIL_NEWVERSION_BATTLEREPORT_MOBILIZATION:
			case MailManager.MAIL_NEWVERSION_BATTLEREPORT_DESERTGRAVE:
			case MailManager.MAIL_NEWVERSION_BATTLEREPORT_CASTLE:
			case MailManager.MAIL_NEWVERSION_BATTLEREPORT_NOTCOVERDOMAIN:
			case MailManager.MAIL_NEWVERSION_BATTLEREPORT_FIGHTDOMAINMARL:
			case MailManager.MAIL_NEWVERSION_ARENA_THRONE:
			case MailManager.MAIL_NEWVERSION_ARENA_TREBUCHET:
			case MailManager.MAIL_SC_FINGHT_RUINS:
			case MailManager.MAIL_SC_FINGHT_CITY:
			{
				if (isAtk)
				{
					mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CITY_VICTORY);
				}
				else
				{
					mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CITY_DEFENT_VICTORY);
				}
				break;
			}
			case MailManager.MAIL_RESOURCE:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_RESOURCE);
				break;
			case MailManager.MAIL_DETECT:
			case MailManager.Mail_DETECT_ARENA:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_DETECT);
				break;
			case MailManager.MAIL_DETECT_REPORT:
			case MailManager.Mail_DETECT_REPORT_ARENA:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_DETECT);
				break;
			case MailManager.MAIL_ENCAMP:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CAMP);
				break;
			case MailManager.MAIL_FRESHER:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_SYSTEM);
				break;
			case MailManager.MAIL_WOUNDED:
				mailIcon = pic;
				usePersonalPic = true;
				break;
			case MailManager.MAIL_DIGONG:
				mailIcon = pic;
				usePersonalPic = true;
				break;
			case MailManager.ALL_SERVICE:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_SYSTEM);
				break;
			case MailManager.WORLD_NEW_EXPLORE:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_EXPLORE);
				break;
			case MailManager.MAIL_SYSNOTICE:
				if (StringUtils.isNotEmpty(title) && title.equals("114020"))
					mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_SYSTEM_VIP);
				else
					mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_SYSTEM);
				break;
			case MailManager.MAIL_SYSUPDATE:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_ANNOUNCEMENT);
				break;
			case MailManager.MAIL_ALLIANCEINVITE:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_INVITE_JOIN_ALLIANCE);
				break;
			case MailManager.MAIL_ATTACKMONSTER:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_MONSTER);
				break;
			case MailManager.MAIL_GIFT_BUY_EXCHANGE:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_GIFT);
				break;
			case MailManager.MAIL_MISSILE:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_MISSILE);
				break;
			case MailManager.WORLD_MONSTER_SPECIAL:
				mailIcon = pic;
				usePersonalPic = true;
				break;
			case MailManager.MAIL_Alliance_ALL:
				mailIcon = pic;
				usePersonalPic = true;
				break;
			case MailManager.MAIL_INVITE_TELEPORT:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_INVITE_MOVE);
				break;
			case MailManager.MAIL_ALLIANCE_KICKOUT:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_INVITE_KICKEDOUT);
				break;
			case MailManager.MAIL_WORLD_BOSS:
			case MailManager.MAIL_WORLD_MONSTER_ELITE:
			case MailManager.MAIL_TYPE_ALLIANCE_BOSS:
			case MailManager.MAIL_TYPE_ALLIANCE_BOSS_TREASURE:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_REPORT);
				break;
			case MailManager.CHAT_ROOM:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_CHAT_ROOM);
				break;
			case MailManager.MAIL_REFUSE_ALL_APPLY:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_INVITE_REJECTED);
				break;
			case MailManager.MAIL_MOBILIZATION_CENTER:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_MOBILIZATION);//待更换图片资源
				break;
			case MailManager.MAIL_COMBOTFACTORY_FIRE:
//				mailIcon = "mail_list_head_bg_battle";//战备工厂使用
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_SYSTEM);
				break;
			case MailManager.MAIL_NEWACTIVITY_WORLDCUP:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_WORLDCUP);
				break;
			default:
				break;
		}

		if (isWorldBossKillRewardMail())
			mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_REPORT);
		if (mailIcon == null || mailIcon.equals(""))
		{
			mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_SYSTEM);
		}
	}

	public void parseMailName()
	{

		if (type == MailManager.MAIL_RESOURCE_HELP)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_RESOURCEHELP);
		}
		else if (type == MailManager.MAIL_MOD_SEND || type == MailManager.MAIL_MOD_PERSONAL)
		{
			nameText = "[MOD]" + fromName;
		}
		else if (type == MailManager.MAIL_ALLIANCEINVITE)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_INVITE);
		}
		else if(type == MailManager.MAIL_PERSIDENT_SEND){
			nameText = fromName;
		}
		else if (type == MailManager.MAIL_ALLIANCE_KICKOUT)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_QUITALLIANCE);
		}
		else if (type == MailManager.MAIL_GIFT)
		{
			if (UserManager.getInstance().getUser(fromUid) != null && !UserManager.getInstance().getUser(fromUid).asn.equals(""))
			{
				nameText = "(" + UserManager.getInstance().getUser(fromUid).asn + ")" + fromName;
			}
			else
			{
				nameText = fromName;
			}
		}
		else if (type == MailManager.MAIL_WORLD_BOSS || type == MailManager.MAIL_WORLD_MONSTER_ELITE || isWorldBossKillRewardMail()
				|| type == MailManager.MAIL_TYPE_ALLIANCE_BOSS)
		{
			String bossName = "";
			String bossId = "";
			try
			{
				JSONObject jsonObject = JSON.parseObject(this.contents);
				if(jsonObject!=null)
				{
					JSONObject jsonItem = jsonObject.getJSONObject("def");
					bossId = jsonItem.getString("id");
				}
			}
			catch (Exception e)
			{
				String[] vector = contents.split("\\|");
				bossId = vector[vector.length-1];
			}
			if(type == MailManager.MAIL_TYPE_ALLIANCE_BOSS){
				bossName = JniController.getInstance().excuteJNIMethod("getAllianceBossName",new Object[]{bossId});
			}else {
				bossName = JniController.getInstance().excuteJNIMethod("getNameById", new Object[]{bossId});
			}
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_WORLDBOSS,bossName);
		}else if(type == MailManager.MAIL_TYPE_ALLIANCE_BOSS_TREASURE){
			String bossName = LanguageManager.getLangByKey(LanguageKeys.ALLIANCE_BOSS_TREASURE);
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ALLIANCE_TREASURE,bossName);
		}
		else if(type == MailManager.Mail_ACTIVITY_NEW && title.equals("79000027")){
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_EVENT);
		}
		else if (type == MailManager.MAIL_RESOURCE)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_NAME_RESOURCE);
		}
		else if (type == MailManager.MAIL_FRESHER || type == MailManager.MAIL_SYSTEM)
		{
			if (StringUtils.isNotEmpty(title) && title.equals("115429"))
			{
				if (StringUtils.isNotEmpty(fromName))
					nameText = fromName;
				else
					nameText = LanguageManager.getLangByKey(LanguageKeys.NPC_NAME);
			}
		}
		else if (type == MailManager.MAIL_REFUSE_ALL_APPLY)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_115464);
		}
		else if (type == MailManager.MAIL_ATTACKMONSTER)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_103715);
		}
		else if (type == MailManager.MAIL_GIFT_BUY_EXCHANGE)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_GIFTCH);
		}
		else if (type == MailManager.MAIL_MISSILE)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_109516);
		}
		else if (type == MailManager.MAIL_MOBILIZATION_CENTER){
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MOBILIZATION);
		}
		else if (type == MailManager.MAIL_DETECT||type == MailManager.Mail_DETECT_ARENA)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MENU_DETECTREPORT);
			if (itemIdFlag == 0 && StringUtils.isNotEmpty(title))
			{
//				NSString *catapultStr = [NSString stringWithMultilingualWithKey:@"200641"]; // 部队
//				if([self.title isEqualToString:@"1"])
//				{
//					catapultStr = [NSString stringWithMultilingualWithKey:@"200635"]; //200635 = 城市
//				}else if([self.title isEqualToString:@"2"]){
//				catapultStr =  [NSString stringWithMultilingualWithKey:@"200641"]; // 部队
//			}else if([self.title isEqualToString:@"3"]){
//				catapultStr = [NSString stringWithMultilingualWithKey:@"200636"]; // 资源点
//			}else if([self.title isEqualToString:@"10"]){
//				catapultStr = [NSString stringWithMultilingualWithKey:@"200639"]; // 发射中心
//			}else if([self.title isEqualToString:@"12"]){
//				catapultStr = [NSString stringWithMultilingualWithKey:@"110172"]; // 巨炮
//			}else if ([self.title isEqualToString:@"26"]){
//				catapultStr = [NSString stringWithMultilingualWithKey:@"115480"]; // 联盟资源站
//			}
//				self.nameText = [NSString stringWithMultilingualWithKey:@"105615" andWithPaddingString1:catapultStr];
				String catapulStr = LanguageManager.getLangByKey("200641");// 200641 = 部队
				if (title.equals("1"))
				{
					catapulStr = LanguageManager.getLangByKey("200635");// 200635 = 城市
				}else if(title.equals("2")){
					catapulStr = LanguageManager.getLangByKey("200641");// 200641 = 部队
				}else if(title.equals("3")){
					catapulStr = LanguageManager.getLangByKey("200636");// 200636 = 资源点
				}else if(title.equals("10")){
					catapulStr = LanguageManager.getLangByKey("200639");// 200639 = 发射中心
				}else if(title.equals("12")){
					catapulStr = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_110172);// 110172 = 巨炮
				}else if(title.equals("26")) {
					catapulStr = LanguageManager.getLangByKey("115480");// 115480 = 联盟资源站
				}
				else if(title.equals("58")) {
					catapulStr = LanguageManager.getLangByKey("99000002");// 第二基地
				}
				nameText = LanguageManager.getLangByKey("105615", catapulStr); // 105615 = 您的{0}被侦查了
			}
		}
		else if (type == MailManager.ALL_SERVICE)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_STUDIO);
		}
		else if (type == MailManager.MAIL_SYSUPDATE)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_NOTICE);
		}
		else if (type == MailManager.MAIL_COMBOTFACTORY_FIRE){//战备工厂<二级cell的titleName>
			nameText = LanguageManager.getLangByKey(LanguageKeys.TIP_SYSTEM);
		}
		else if(type == MailManager.MAIL_ALLIANCE_COMMON){
			nameText = LanguageManager.getLangByKey("115000");
		}


		if(StringUtils.isNotEmpty(fromName) && StringUtils.isNumeric(fromName) && !fromName.contains("."))
		{
			nameText = LanguageManager.getLangByKey(fromName);
		}

		if (nameText == null || nameText.equals(""))
		{
			if (tabType == MailManager.MAILTAB_FIGHT)
			{
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_FIGHT);
			}else if(this.isChannelEventMail(channelId)){
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_EVENT);
			}
			else if (tabType == MailManager.MAILTAB_MOD)
			{
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_MOD);
			}
			else if (tabType == MailManager.MAILTAB_STUDIO)
			{
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_STUDIO);
			}
			else if (tabType == MailManager.MAILTAB_SYSTEM)
			{
				nameText = LanguageManager.getLangByKey(LanguageKeys.TIP_SYSTEM);
			}
			else if (tabType == MailManager.MAILTAB_NOTICE)
			{
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_NOTICE);
			}else if(channelId != null && channelId.equals(MailManager.CHANNELID_ALLIANCE)) {
				nameText = LanguageManager.getLangByKey("115000"); //联盟
			}
		}

		if ((type != MailManager.MAIL_DETECT||type != MailManager.Mail_DETECT_ARENA)&&(fromName.equals("3000001") || fromName.equals("3000002")))
			nameText = LanguageManager.getLangByKey(LanguageKeys.NPC_NAME);

	}

	public boolean isChannelEventMail(String channelId){
		if(channelId != null && (channelId.equals(MailManager.CHANNELID_EVENT_NORMAL) ||
				channelId.equals(MailManager.CHANNELID_EVENT_ALLIANCERAM) ||
				channelId.equals(MailManager.CHANNELID_EVENT_GREATKING) ||
				channelId.equals(MailManager.CHANNELID_EVENT_DESERT) ||
				channelId.equals(MailManager.CHANNELID_EVENT_PERSONALARM))){
			return true;
		}
		return false;
	}
	public void parseContentText()
	{
		if (StringUtils.isNotEmpty(title))
		{
			contentText = title.replaceAll("\n", " ");
			if (title.equals("A105550")) {
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_AMBULANCEFULL_TITLE);
			}else if (title.equals("105734"))
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_CASTLEMOVE);
			else if (title.equals("138067"))
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_BATTLE_TITLE);
			else if (title.equals("114010"))
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_NEWPLAYER_MOVECASTAL);
			else if (title.equals("115295"))
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_INVITE_MOVECASTAL);
			else if (title.equals("114012"))
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_ALLIANCE_APPLY);
			else if (title.equals("105718"))
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_ENEMY_KILL);
			else if (title.equals("133017"))
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_ALLIANCE_REWARD);
			else if (StringUtils.isNumeric(title))
			{
				contentText = LanguageManager.getLangByKey(title);
			}
		}

		if (itemIdFlag == 1)
		{
			String contentArr[] = contents.split("\\|");
			int num = contentArr.length;

			switch (num)
			{
				case 5:
					if (contentArr[0].equals("115336"))
					{
						String name = LanguageManager.getLangByKey(LanguageKeys.MAIL_ALLIANCE_CASTLE, contentArr[4]);
						contentText = LanguageManager.getLangByKey(contentArr[3], name);
					}
					break;
				case 6:
				{
					String name = "";
					if (contentArr[1].equals("") || contentArr[2].equals(""))
					{
						name = LanguageManager.getLangByKey(LanguageKeys.MAIL_ALLIANCE_CASTLE, contentArr[5]);
					}
					else
					{
						name = contentArr[1];
					}
					contentText = LanguageManager.getLangByKey(contentArr[4], name);
				}
				break;
				default:
					break;
			}
		}

		if (type == MailManager.MAIL_DONATE)
		{
			String str[] = contents.split("\\|");
			if (str.length > 4)
			{
				String userName = str[1];
				if (!str[2].equals(""))
				{
					userName = "(" + str[2] + ")" + userName;
				}
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_GIFT, userName);
			}
		}
		else if (type == MailManager.MAIL_DIGONG || type == MailManager.WORLD_NEW_EXPLORE)
		{
			contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_WORLDEXPLORE);
		}
		else if (type == MailManager.MAIL_WOUNDED)
		{
			contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_WOUNDED);
		}
		else if (type == MailManager.MAIL_DETECT||type == MailManager.Mail_DETECT_ARENA)
		{
			if (itemIdFlag == 1)
			{
				if (!contents.equals("") && contents.length() > 6)
				{
					String content[] = contents.split("\\|");
					int num = content.length;
					if(contents.contains("BattlefieldServerFlag"))
					{
						isArenagameMail=true;
						num=num-1;
					}
					if(num>=2)
					{
						if (content[0].equals("105554"))
						{
							contentText = LanguageManager.getLangByKey("105554",content[1]);
						}else if(content[0].equals("200633")){
							String tempStr = LanguageManager.getLangByKey(content[1]);
							contentText = LanguageManager.getLangByKey("200633", tempStr);
						}else if((content[0].equals("82000335")||content[0].equals("82000336")) && num >= 3){
							contentText = LanguageManager.getLangByKey(content[0],content[1],content[2]);
						}else if(content[0].equals("82000344")||content[0].equals("82000345")){
							contentText = LanguageManager.getLangByKey(content[0],content[1]);
						}
					}
				}

			}
			else
			{
				if (title.equals("1"))
				{
					contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105523);
				}
				else
				{
					contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105567);
				}

				String content[] = contents.split("\\|");
				int num = content.length;
				if(contents.contains("BattlefieldServerFlag"))
				{
					isArenagameMail=true;
					num=num-1;
				}
				switch (num)
				{
					case 1:
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105524, contents);
						break;
					case 3:
					case 4:
					{
						String name = content[0];
						String type = content[1];
						if(num == 4){
							String serverIdPos[] = content[3].split(",");
							if(UserManager.getInstance().getCurrentUser()!=null && serverIdPos!= null && serverIdPos.length > 0){
								String serverId = serverIdPos[0];
								name = UserManager.getInstance().getCurrentUser().getNameWithServerId("",name,Integer.parseInt(serverId));
							}
						}
						if (type.equals("1"))
						{
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_137429, name);
						}
						else if (type.equals("2"))
						{
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_137431, name);
						}
						else if (type.equals("3"))
						{
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_137430, name);
						}
						else if (type.equals("10"))
						{
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105524, name);
							nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_140181,
									LanguageManager.getLangByKey("110070"));
						}
						else if (type.equals("12"))
						{
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105524, name);
							nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_140181,
									LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_110172));
						}
						else if (type.equals("58"))
						{
							contentText = LanguageManager.getLangByKey("99000312", name); //{0}侦查了您的第二基地
						}
						else
						{
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105524, name);
						}
						break;
					}
					default:
						break;
				}

			}
		}
		else if (type == MailManager.MAIL_GIFT)
		{
			contentText = contents;
		}else if (type == MailManager.MAIL_GIVE_SOLDIER){
			contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_101040);;
		}
		else if (type == MailManager.MAIL_COMBOTFACTORY_FIRE) {//二级cell的内容显示为射击报告
			contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_COMBATFACTORY_FIRE);
		}
		else if (type == MailManager.MAIL_GIFT_RECEIVE){
			try
			{
				JSONObject jsonObject = JSON.parseObject(this.contents);
				if(jsonObject!=null)
				{
					fromName = jsonObject.getString("senderName");
					int senderServerId = jsonObject.getIntValue("senderServerId");
					String senderAlAbbr = jsonObject.getString("senderAlAbbr");
					if(UserManager.getInstance().getCurrentUser()!=null) {
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_111813, UserManager.getInstance().getCurrentUser().getNameWithServerId(senderAlAbbr, fromName, senderServerId));
					}
				}
			}
			catch (Exception e)
			{
			}
		}else if(isWorldBossKillRewardMail())
		{
			String bossName = "";
			String bossId = "";
			String[] vector = contents.split("\\|");
			bossId = vector[vector.length-1];
			bossName = JniController.getInstance().excuteJNIMethod("getNameById",new Object[]{bossId});
			contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_CONTENT_BOSSREWARD,bossName);  //  137460 = {0}消灭奖励
		}else if(type == MailManager.MAIL_TYPE_ALLIANCE_BOSS_TREASURE){
			String bossName = LanguageManager.getLangByKey(LanguageKeys.ALLIANCE_BOSS_TREASURE);
			String content = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ALLIANCE_TREASURE,bossName);
			String myTeam = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_MYARMY);
			contentText = myTeam.concat(content);
		}
		if (StringUtils.isNotEmpty(contentText) && contentText.length() > 50)
		{
			contentText = contentText.substring(0, 50);
			contentText = contentText + "...";
		}

	}

	@JSONField(serialize = false)
	public boolean isComplexMail()
	{
		if (type == MailManager.MAIL_BATTLE_REPORT || type == MailManager.MAIL_RESOURCE || type == MailManager.MAIL_DETECT_REPORT|| type == MailManager.Mail_DETECT_REPORT_ARENA
				|| type == MailManager.MAIL_ENCAMP || type == MailManager.WORLD_NEW_EXPLORE || type == MailManager.MAIL_ALLIANCEINVITE
				|| type == MailManager.MAIL_ALLIANCEAPPLY || type == MailManager.MAIL_ATTACKMONSTER|| type == MailManager.MAIL_GIFT_BUY_EXCHANGE
				|| type == MailManager.MAIL_MISSILE || type == MailManager.MAIL_RESOURCE_HELP
				|| type == MailManager.MAIL_INVITE_TELEPORT || type == MailManager.MAIL_ALLIANCE_KICKOUT
				|| type == MailManager.MAIL_WORLD_BOSS || type == MailManager.MAIL_REFUSE_ALL_APPLY||type == MailManager.Mail_BORDERFIGHT
				|| type == MailManager.Mail_BATTLEGAMEFIGHT||type == MailManager.Mail_SHAMOGAMEFIGHT||type == MailManager.Mail_CASTLEGAMEFIGHT||type == MailManager.Mail_ARENAGAMEFIGHT
				|| type == MailManager.Mail_KNIGHTGAMEFIGHT||type == MailManager.Mail_SHAMOKNIGHTGAMEFIGHT||type == MailManager.Mail_GAMEFIGHTACTIVITY||type == MailManager.MAIL_SHAMOGOLDDIGEGER||type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYER_RESOURCE
				|| type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYERCITY || type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_THRONE || type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_ALLIANCETOWER || type == MailManager.MAIL_MOBILIZATION_CENTER
				|| type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_EXPEDITION
				|| type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_PYRAMID||type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_MOBILIZATION||type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_CASTLE
				|| type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_DESERTGRAVE
				|| type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_NOTCOVERDOMAIN
				|| type == MailManager.MAIL_NEWVERSION_BATTLEREPORT_FIGHTDOMAINMARL
				|| type == MailManager.MAIL_NEWVERSION_ARENA_THRONE || type == MailManager.MAIL_NEWVERSION_ARENA_TREBUCHET
				|| type == MailManager.MAIL_WORLD_MONSTER_ELITE || type == MailManager.MAIL_SC_FINGHT_RUINS || type == MailManager.MAIL_SC_FINGHT_CITY
				|| type == MailManager.MAIL_TYPE_ALLIANCE_BOSS || type == MailManager.MAIL_TYPE_ALLIANCE_BOSS_TREASURE
				)
		{
			return true;
		}
		return false;
	}

	public boolean needParseContent()
	{
		if (StringUtils.isNotEmpty(nameText) && StringUtils.isNotEmpty(contentText) && !isParseVersionOld()
				&& !StringUtils.isNumeric(nameText) && !StringUtils.isNumeric(contentText) && StringUtils.isNotEmpty(translationId)
				&& TranslateManager.getInstance().isLangSameAsTargetLang(translationId))
		{
			return false;
		}
		return true;
	}

	//@JSONField(serialize = true)
	public String getChannelId()
	{
		return getNewChannelId();
	}

	public void setChannelId(String channelId)
	{
		this.channelId = channelId;
	}

	//	@JSONField(serialize = false)
	public String getOldChannelId()
	{
		String channelId = "";
		if (tabType < 0)
			parseMailTypeTab();
		switch (tabType)
		{
			case MailManager.MAILTAB_SYSTEM:
				channelId = MailManager.CHANNELID_SYSTEM;
				break;
			case MailManager.MAILTAB_NOTICE:
				channelId = MailManager.CHANNELID_NOTICE;
				break;
			case MailManager.MAILTAB_STUDIO:
				channelId = MailManager.CHANNELID_STUDIO;
				break;
			case MailManager.MAILTAB_FIGHT:
				channelId = MailManager.CHANNELID_FIGHT;
				break;
			case MailManager.MAILTAB_MOD:
				channelId = MailManager.CHANNELID_MOD;
				break;
			default:
				break;
		}

		if (type == MailManager.MAIL_RESOURCE)
			channelId = MailManager.CHANNELID_RESOURCE;
		else if (type == MailManager.MAIL_ATTACKMONSTER)
			channelId = MailManager.CHANNELID_MONSTER;
		else if (type == MailManager.MAIL_GIFT_BUY_EXCHANGE)
			channelId = MailManager.CHANNELID_GIFT;
		else if (type == MailManager.MAIL_MISSILE)
			channelId = MailManager.CHANNELID_MISSILE;
		else if (type == MailManager.MAIL_RESOURCE_HELP)
			channelId = MailManager.CHANNELID_RESOURCE_HELP;
		else if (type == MailManager.MAIL_MOBILIZATION_CENTER){
			channelId = MailManager.CHANNELID_MOBILIZATION_CENTER;
		}else  if (type == MailManager.MAIL_COMBOTFACTORY_FIRE){
			channelId = MailManager.CHANNELID_COMBOTFACTORY_FIRE;
		}
		return channelId;
	}

	@JSONField(serialize = false)
	public boolean isKnightMail()
	{
		return isKnightMail;
	}

	@JSONField(serialize = false)
	public void setIsKnightMail(boolean flag)
	{
		isKnightMail = flag;
	}

	@JSONField(serialize = false)
	public boolean isKnightActivityFinish()
	{
		return isKnightActivityFinishMail;
	}

	@JSONField(serialize = false)
	public boolean isBattlegameMail()
	{
		return isBattlegameMail;
	}


	@JSONField(serialize = false)
	public boolean isArenagameMail()
	{
		return isArenagameMail;
	}

	@JSONField(serialize = false)
	public boolean isShaMogameMail()
	{
		return isShaMogameMail;
	}




	//@JSONField(serialize = false)
	public String getNewChannelId()
	{
		String channelId = "";
		if (type == MailManager.MAIL_SYSTEM)
		{
			if (title.equals("114111") || title.equals("105726") || title.equals("105727") || title.equals("105728")
					|| title.equals("105729") || title.equals("105730") || title.equals("115429")|| title.equals("87003105")
					|| title.equals("90100190"))
			{
				channelId = MailManager.CHANNELID_ALLIANCE;
			}
			else if (isWorldBossKillRewardMail() || isKnightActivityStartMail() || title.equals("150335"))
			{
				channelId = MailManager.CHANNELID_EVENT_NORMAL;
			}
			else
			{
				channelId = MailManager.CHANNELID_SYSTEM;
			}
		}
		else if (type == MailManager.MAIL_BATTLE_REPORT)
		{
//			if (!hasParseForKnight)
//			{
//				needParseByForce = true;
//				MailManager.getInstance().parseMailDataContent(this);
//			}
			if (isKnightMail())
			{
				channelId = MailManager.CHANNELID_KNIGHT;
			}
			else if (isKnightActivityFinish())
			{
				channelId = MailManager.CHANNELID_EVENT;
			}
			else if (isBattlegameMail())
			{
				channelId = MailManager.CHANNELID_BATTLEGAME;
			}
			else if (isArenagameMail())
			{
				channelId = MailManager.CHANNELID_ARENAGAME;
			}
			else if (isShaMogameMail())
			{
				channelId = MailManager.CHANNELID_SHAMOGAME;
			}
			else
			{
				channelId = MailManager.CHANNELID_FIGHT;
			}
		}
		else if(type == MailManager.MAIL_DETECT||type == MailManager.MAIL_DETECT_REPORT || type == MailManager.Mail_DETECT_REPORT_ARENA || type == MailManager.Mail_DETECT_ARENA)
		{
			if (isArenagameMail() || type == MailManager.Mail_DETECT_REPORT_ARENA || type == MailManager.Mail_DETECT_ARENA)
			{
				channelId = MailManager.CHANNELID_ARENAGAME;
			}
			else
			{
				channelId = MailManager.CHANNELID_FIGHT;
			}
		}
		else
		{
			switch (type)
			{
				case MailManager.MAIL_ENCAMP:
				case MailManager.WORLD_NEW_EXPLORE:
				case MailManager.WORLD_MONSTER_SPECIAL:
				case MailManager.Mail_CASTLE_ACCOUNT:
				case MailManager.Mail_CASTLEGAMEFIGHT:
				case MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYERCITY:
				case MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYER_RESOURCE:
				case MailManager.MAIL_NEWVERSION_BATTLEREPORT_THRONE:
				case MailManager.MAIL_NEWVERSION_BATTLEREPORT_ALLIANCETOWER:
				case MailManager.MAIL_NEWVERSION_BATTLEREPORT_DESERTGRAVE:
				case MailManager.MAIL_NEWVERSION_BATTLEREPORT_CASTLE:
				case MailManager.MAIL_SC_FINGHT_RUINS:
				case MailManager.MAIL_SC_FINGHT_CITY:
					channelId = MailManager.CHANNELID_FIGHT;
					break;
				case MailManager.MAIL_ALLIANCEINVITE:
				case MailManager.MAIL_Alliance_ALL:
				case MailManager.MAIL_ALLIANCEAPPLY:
				case MailManager.MAIL_ALLIANCE_KICKOUT:
				case MailManager.MAIL_INVITE_TELEPORT:
				case MailManager.MAIL_REFUSE_ALL_APPLY:
				case MailManager.MAIL_RESOURCE_HELP:
				case MailManager.MAIL_DONATE:
				case MailManager.MAIL_ALLIANCE_UNSIGN:
				case MailManager.MAIL_ALLIANCE_COMMON:
				case MailManager.MAIL_ALLIANCEOFFICEAPPLY:
				case MailManager.MAIL_ALLIANCE_PACKAGE:
				case MailManager.MAIL_ALLIANCE_DONATE:
				case MailManager.MAIL_TYPE_ALLIANCE_BOSS:
				case MailManager.MAIL_TYPE_ALLIANCE_BOSS_TREASURE:
					channelId = MailManager.CHANNELID_ALLIANCE;
					break;
				case MailManager.ALL_SERVICE:
				case MailManager.MAIL_SYSUPDATE:
				case MailManager.MAIL_VOTE:
					channelId = MailManager.CHANNELID_STUDIO;
					break;
				case MailManager.MAIL_GIFT:
				case MailManager.MAIL_SYSNOTICE:
				case MailManager.MAIL_SERVICE:
				case MailManager.MAIL_FRESHER:
				case MailManager.MAIL_USERHERO:
				case MailManager.MAIL_WOUNDED:
				case MailManager.MAIL_MOVE_FORTRESS:
				case MailManager.MAIL_PERSIDENT_SEND:
				case MailManager.MAIL_GIVE_SOLDIER:
				case MailManager.MAIL_GIFT_RECEIVE:
					channelId = MailManager.CHANNELID_SYSTEM;
					break;
				case MailManager.MAIL_ATTACKMONSTER:
					channelId = MailManager.CHANNELID_MONSTER;
					break;
				case MailManager.MAIL_GIFT_BUY_EXCHANGE:
					channelId = MailManager.CHANNELID_GIFT;
					break;
				case MailManager.MAIL_MISSILE:
					channelId = MailManager.CHANNELID_MISSILE;
					break;
				case MailManager.MAIL_RESOURCE:
					channelId = MailManager.CHANNELID_RESOURCE;
					break;
				case MailManager.MAIL_WORLD_BOSS:
				case MailManager.Mail_GAMEFIGHTACTIVITY:
				case MailManager.Mail_ACTIVITY_NEW:
				case MailManager.MAIL_WORLD_MONSTER_ELITE:
				case MailManager.MAIL_NEWACTIVITY_WORLDCUP:
					channelId = MailManager.CHANNELID_EVENT_NORMAL;
					break;
				case MailManager.MAIL_NEWACTIVITY_PERSONALARM:
					channelId = MailManager.CHANNELID_EVENT_PERSONALARM;
					break;
				case MailManager.MAIL_NEWACTIVITY_ALLIANCERAM:
					channelId = MailManager.CHANNELID_EVENT_ALLIANCERAM;
					break;
				case MailManager.MAIL_NEWACTIVITY_GREATKING:
					channelId = MailManager.CHANNELID_EVENT_GREATKING;
					break;
				case MailManager.MAIL_NEWACTIVITY_DESERT:
					channelId = MailManager.CHANNELID_EVENT_DESERT;
					break;
				case MailManager.MAIL_ShaMoExplore:
					channelId = MailManager.CHANNELID_SHAMOEXPLORE;
					break;
				case MailManager.Mail_BORDERFIGHT:
				case MailManager.MAIL_NEWVERSION_BATTLEREPORT_EXPEDITION:
					channelId = MailManager.CHANNELID_BORDERFIGHT;
					break;
				case MailManager.Mail_BATTLEGAMEFIGHT:
					channelId = MailManager.CHANNELID_BATTLEGAME;
					break;
				case MailManager.Mail_SHAMOGAMEFIGHT:
				case MailManager.MAIL_NEWVERSION_BATTLEREPORT_PYRAMID:
					channelId = MailManager.CHANNELID_SHAMOGAME;
					break;
				case MailManager.Mail_ARENAGAMEFIGHT:
				case MailManager.MAIL_NEWVERSION_ARENA_THRONE:
				case MailManager.MAIL_NEWVERSION_ARENA_TREBUCHET:
					channelId = MailManager.CHANNELID_ARENAGAME;
					break;
				case MailManager.Mail_KNIGHTGAMEFIGHT:
				case MailManager.Mail_SHAMOKNIGHTGAMEFIGHT:
					channelId = MailManager.CHANNELID_KNIGHT;
					break;
				case MailManager.MAIL_SHAMOGOLDDIGEGER:
				case MailManager.MAIL_NEWVERSION_BATTLEREPORT_NOTCOVERDOMAIN:
				case MailManager.MAIL_NEWVERSION_BATTLEREPORT_FIGHTDOMAINMARL:
					channelId = MailManager.CHANNELID_SHAMOGOLDDIGGER;
					break;
				case MailManager.MAIL_MOBILIZATION_CENTER:
					channelId = MailManager.CHANNELID_MOBILIZATION_CENTER;
					break;
				case MailManager.MAIL_COMBOTFACTORY_FIRE:
				case MailManager.MAIL_NEWVERSION_BATTLEREPORT_MOBILIZATION:
					channelId = MailManager.CHANNELID_COMBOTFACTORY_FIRE;
					break;
			}
		}

		return channelId;
	}

	@Override
	public String toString()
	{
		return "[MailData]: uid = " + uid + " type = " + type + " title:" + title;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public int getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(int createTime)
	{
		this.createTime = createTime;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	@JSONField(serialize = false)
	public boolean isUnread()
	{
		boolean b = false;
		if(failTime>0){
			//long time = ChatServiceController.getInstance().host.getCurrentServerTime();
			long time = TimeManager.getInstance().getCurrentTimeMS();
			b = ((failTime-time) > 0 || issamplereport)?true:false;
		}else if(failTime == 0){
			b = true;
		}

		return status == 0 && b==true;
	}

	public int getReply()
	{
		return reply;
	}

	public void setReply(int reply)
	{
		this.reply = reply;
	}

	public boolean hasReward()
	{
		return !rewardId.equals("") && rewardStatus == 0;
	}

	@JSONField(serialize = false)
	public boolean isLock()
	{
		return getSave() == 1;
	}

	public int getRewardStatus()
	{
		return rewardStatus;
	}

	public void setRewardStatus(int rewardStatus)
	{
		this.rewardStatus = rewardStatus;
	}

	public int getItemIdFlag()
	{
		return itemIdFlag;
	}

	public void setItemIdFlag(int itemIdFlag)
	{
		this.itemIdFlag = itemIdFlag;
	}

	public int getSave()
	{
		return save;
	}

	public void setSave(int save)
	{
		this.save = save;
	}

	public String getUid()
	{
		return uid;
	}

	public void setUid(String uid)
	{
		this.uid = uid;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getContents()
	{
		return contents;
	}

	public void setContents(String contents)
	{
		this.contents = contents;
	}

	public String getFromName()
	{
		return fromName;
	}

	public void setFromName(String fromName)
	{
		this.fromName = fromName;
	}

	public String getFromUid()
	{
		return fromUid;
	}

	public void setFromUid(String fromUid)
	{
		this.fromUid = fromUid;
	}

	public String getRewardId()
	{
		return rewardId;
	}

	public void setRewardId(String rewardId)
	{
		this.rewardId = rewardId;
	}

	@JSONField(serialize = false)
	public String getVersion()
	{
		return version;
	}

	@JSONField(serialize = false)
	public void setVersion(String version)
	{
		this.version = version;
	}

	@JSONField(serialize = false)
	public long getChannelTime()
	{
		return getCreateTime();
	}

	@JSONField(serialize = false)
	public boolean isWorldBossKillRewardMail()
	{
		if (itemIdFlag == 1 && StringUtils.isNotEmpty(title) && title.equals("137460"))
			return true;
		return false;
	}

	@JSONField(serialize = false)
	public boolean isKnightActivityStartMail()
	{
		if (StringUtils.isNotEmpty(title) && title.equals("133270"))
			return true;
		return false;
	}

	public int getMbLevel()
	{
		return mbLevel;
	}

	public void setMbLevel(int mbLevel)
	{
		this.mbLevel = mbLevel;
	}

	public boolean isChannelMail()
	{
		String channelId = getChannelId();
		if (StringUtils.isNotEmpty(channelId)
				&& (channelId.equals(MailManager.CHANNELID_KNIGHT) || channelId.equals(MailManager.CHANNELID_RESOURCE) || channelId
				.equals(MailManager.CHANNELID_MONSTER) || channelId.equals(MailManager.CHANNELID_GIFT)|| channelId
				.equals(MailManager.CHANNELID_MISSILE) || channelId.equals(MailManager.CHANNELID_COMBOTFACTORY_FIRE)
				|| channelId.equals(MailManager.CHANNELID_MOBILIZATION_CENTER)))
			return true;
		return false;
	}

	//邮件是否过期
	public boolean isFailMail()
	{
		long time = TimeManager.getInstance().getCurrentTimeMS();
		if (time > failTime && failTime !=0 && issamplereport){
			return true;
		}else{
			return false;
		}
	}

}
