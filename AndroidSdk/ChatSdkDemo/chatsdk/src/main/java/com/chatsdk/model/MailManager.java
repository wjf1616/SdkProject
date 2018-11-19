package com.chatsdk.model;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.chatsdk.controller.JniController;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.db.DBManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.model.mail.allianceapply.AllianceApplyMailData;
import com.chatsdk.model.mail.allianceinvite.AllianceInviteMailData;
import com.chatsdk.model.mail.alliancekickout.AllianceKickOutMailData;
import com.chatsdk.model.mail.battle.BattleMailData;
import com.chatsdk.model.mail.battleend.BattleEndMailData;
import com.chatsdk.model.mail.detectreport.DetectReportMailData;
import com.chatsdk.model.mail.gift.GiftMailData;
import com.chatsdk.model.mail.inviteteleport.InviteTeleportMailData;
import com.chatsdk.model.mail.missile.MissileMailData;
import com.chatsdk.model.mail.mobilization.MobilizationMailData;
import com.chatsdk.model.mail.monster.MonsterMailData;
import com.chatsdk.model.mail.newbattle.NewVersionBattleMailData;
import com.chatsdk.model.mail.ocupy.OcupyMailData;
import com.chatsdk.model.mail.refuseallreply.RefuseAllReplyMailData;
import com.chatsdk.model.mail.resouce.ResourceMailData;
import com.chatsdk.model.mail.resourcehelp.ResourceHelpMailData;
import com.chatsdk.model.mail.worldboss.WorldBossMailData;
import com.chatsdk.model.mail.worldexplore.WorldExploreMailData;
import com.chatsdk.util.IAnalyticTracker;
import com.chatsdk.util.LogUtil;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailManager
{
	public static final int		MAILTAB_USER			= 0;				// 玩家邮件，包括个人邮件、聊天室、联盟邮件
	public static final int		MAILTAB_SYSTEM			= 1;				// 系统邮件，对应后台的"per_sys"
	public static final int		MAILTAB_NOTICE			= 2;				// 公告邮件，包括更新、公告
	public static final int		MAILTAB_STUDIO			= 3;				// COK工作室邮件
	public static final int		MAILTAB_FIGHT			= 4;				// 战斗邮件
	public static final int		MAILTAB_MOD				= 5;				// mod邮件

	public static final String	CHANNELID_SYSTEM		= "system";
	public static final String	CHANNELID_NOTICE		= "notice";
	public static final String	CHANNELID_STUDIO		= "studio";
	public static final String	CHANNELID_FIGHT			= "fight";
	public static final String	CHANNELID_MOD			= "mod";
	public static final String	CHANNELID_RESOURCE		= "resource";
	public static final String	CHANNELID_MONSTER		= "monster";
	public static final String	CHANNELID_RESOURCE_HELP	= "resourcehelp";
	public static final String	CHANNELID_ALLIANCE		= "alliance";
	public static final String	CHANNELID_MESSAGE		= "message";
	public static final String	CHANNELID_EVENT			= "event";
	public static final String	CHANNELID_KNIGHT		= "knight";
	public static final String	CHANNELID_MISSILE		= "missile";        //导弹邮件
	public static final String	CHANNELID_GIFT			= "gift";        //礼包购买记录
	public static final String	CHANNELID_MOBILIZATION_CENTER			= "mobilization_center";        //动员中心记录

	public static final String	CHANNELID_BATTLEGAME	= "battle_game";  //战争游戏

	public static final String	CHANNELID_ARENAGAME	    = "arena_game";  //末日竞技场

	public static final String	CHANNELID_SHAMOGAME	    = "shamo_game";  //沙漠

	public static final String	CHANNELID_SHAMOEXPLORE	    = "shamo_explore";  //沙漠探索

	public static final String	CHANNELID_BORDERFIGHT	    = "border_fight";  //边境战


	public static final String	CHANNELID_SHAMOGOLDDIGGER	    = "shamo_golddigger";  //沙漠淘金者
	public static final String  CHANNELID_COMBOTFACTORY_FIRE = "combat_factory";//战备工厂(射击邮件)


	public static final String	CHANNELID_EVENT_PERSONALARM		= "personalArm_event"; //个人军备
	public static final String	CHANNELID_EVENT_ALLIANCERAM		= "allianceArm_event"; //联盟军备
	public static final String	CHANNELID_EVENT_GREATKING		= "greateKing_event"; //最强要塞邮件新分类(活动)
	public static final String	CHANNELID_EVENT_DESERT			= "desert_event"; //沙漠活动邮件新分类(活动)
	public static final String	CHANNELID_EVENT_NORMAL			= "normal_event"; //普通
	public static final int		CHANNELID_EVENT_COUNT	= 5;  //更改活动分类数量的时候更改


	public static final int		ITEM_BG_COLOR_MESSAGE	= 0xFF2E3D59;
	public static final int		ITEM_BG_COLOR_ALLIANCE	= 0xFF38693F;
	public static final int		ITEM_BG_COLOR_BATTLE	= 0xFF852828;
	public static final int		ITEM_BG_COLOR_STUDIO	= 0xFF3F4145;
	public static final int		ITEM_BG_COLOR_SYSTEM	= 0xFF7F5C13;

	public static final int		SERVER_BATTLE_FIELD		= 2;

	public static int getColorByChannelId(String channelId)
	{
		if (channelId.equals(CHANNELID_MESSAGE))
		{
			return ITEM_BG_COLOR_MESSAGE;
		}
		else if (channelId.equals(CHANNELID_ALLIANCE))
		{
			return ITEM_BG_COLOR_ALLIANCE;
		}
		else if (channelId.equals(CHANNELID_FIGHT))
		{
			return ITEM_BG_COLOR_BATTLE;
		}
		else if (channelId.equals(CHANNELID_STUDIO))
		{
			return ITEM_BG_COLOR_STUDIO;
		}
		else if (channelId.equals(CHANNELID_SYSTEM))
		{
			return ITEM_BG_COLOR_SYSTEM;
		}
		else if (channelId.equals(CHANNELID_MOD))
		{
			return ITEM_BG_COLOR_MESSAGE;
		}
		return ITEM_BG_COLOR_SYSTEM;
	}

	// 邮件类型
	public static final int			MAIL_SELF_SEND					= 0;
	public static final int			MAIL_USER						= 1;
	public static final int			MAIL_SYSTEM						= 2;
	public static final int			MAIL_SERVICE					= 3;
	public static final int			MAIL_BATTLE_REPORT				= 4;
	public static final int			MAIL_RESOURCE					= 5;
	public static final int			MAIL_DETECT						= 6;
	public static final int			MAIL_GENERAL_TRAIN				= 7;
	public static final int			MAIL_DETECT_REPORT				= 8;
	public static final int			MAIL_ENCAMP						= 9;
	public static final int			MAIL_FRESHER					= 10;
	public static final int			MAIL_WOUNDED					= 11;
	public static final int			MAIL_DIGONG						= 12;
	public static final int			ALL_SERVICE						= 13;
	public static final int			WORLD_NEW_EXPLORE				= 14;
	public static final int			MAIL_SYSNOTICE					= 15;
	public static final int			MAIL_SYSUPDATE					= 16;
	public static final int			MAIL_ALLIANCEINVITE				= 17;
	public static final int			MAIL_ATTACKMONSTER				= 18;
	public static final int			WORLD_MONSTER_SPECIAL			= 19;
	public static final int			MAIL_Alliance_ALL				= 20;
	public static final int			MAIL_RESOURCE_HELP				= 21;
	public static final int			MAIL_PERSONAL					= 22;
	public static final int			MAIL_MOD_PERSONAL				= 23;
	public static final int			MAIL_MOD_SEND					= 24;
	public static final int			MAIL_ALLIANCEAPPLY				= 25;
	public static final int			MAIL_INVITE_TELEPORT			= 26;
	public static final int			MAIL_ALLIANCE_KICKOUT			= 27;
	public static final int			MAIL_GIFT						= 28;
	public static final int			MAIL_DONATE						= 29;
	public static final int			MAIL_WORLD_BOSS					= 30;
	public static final int  MAIL_ALLIANCE_UNSIGN            =  31 ; //联盟邮件发给今天未签到的
	public static final int  MAIL_ALLIANCEOFFICEAPPLY        =  32  ;//联盟官职申请
	public static final int  MAIL_USERHERO  = 33 ;//用户英雄邮件
	public static final int  MAIL_ALLIANCE_PACKAGE = 38  ;//联盟礼包
	public static final int  CHAT_ROOM	  = 35 ;//联盟等级变化
	public static final int  MAIL_WORLD_BOSS_REWARD	= 36 ;
	public static final int  MAIL_REFUSE_ALL_APPLY	= 37;
	public static final int  MAIL_GIFT_BUY_EXCHANGE	= 34 ;
	public static final int  MAIL_ShaMoExplore	= 39 ;//沙漠探索
	public static final int  MAIL_MISSILE	= 40 ;
	public static final int  MAIL_GIVE_SOLDIER	= 41 ;
	public static final int  MAIL_ALLIANCE_DONATE	= 42 ;
	public static final int  MAIL_MOVE_FORTRESS	= 44 ;
	public static final int  MAIL_PERSIDENT_SEND = 45 ;
	public static final int  MAIL_GIFT_RECEIVE = 46;
	public static final int  MAIL_VOTE		 = 47;
	public static final int  Mail_CASTLE_ACCOUNT		 = 48;// 副堡结算邮件
	public static final int  Mail_BORDERFIGHT		 = 49;// 边境战邮件


	public static final int  Mail_BATTLEGAMEFIGHT		 = 50;// 攻城略地(战争游戏)战斗邮件

	public static final int  Mail_SHAMOGAMEFIGHT		 = 51;// 车轮战战斗邮件

	public static final int  Mail_CASTLEGAMEFIGHT		 = 52;// 副堡战斗邮件

	public static final int  Mail_ARENAGAMEFIGHT		 = 53;// 竞技场邮件

	public static final int  Mail_KNIGHTGAMEFIGHT		 = 54;// 丧尸来袭邮件

	public static final int  Mail_SHAMOKNIGHTGAMEFIGHT		 = 55;// 沙漠丧尸来袭(木乃伊归来)邮件

	public static final int  Mail_GAMEFIGHTACTIVITY		 = 56;// 活动分类中的战报类型邮件(丧尸来袭/木乃伊来袭活动结束邮件)

	public static final int  Mail_ACTIVITY_NEW		 = 57;//  活动分类邮件

	public static final int  Mail_DETECT_ARENA		 = 58;//  竞技场被侦查邮件

	public static final int  Mail_DETECT_REPORT_ARENA		 = 59;//  竞技场侦查邮件

	public static final int  MAIL_ALLIANCE_COMMON		 = 60;//  联盟通用邮件类型

	public static final int  MAIL_SHAMOGOLDDIGEGER		 = 61;//  沙漠淘金者

	public static final int  MAIL_MOBILIZATION_CENTER		= 62;// 动员中心

	public static final int  MAIL_NEWVERSION_BATTLEREPORT_JYDJ		= 63;// 精英对决

	public static final int  MAIL_NEWVERSION_BATTLEREPORT_PLAYERCITY		= 64;// 玩家城市

	public static final int  MAIL_NEWVERSION_BATTLEREPORT_PLAYER_OCCUPYCITY		= 65;// 扎营地

	public static final int  MAIL_NEWVERSION_BATTLEREPORT_PLAYER_RESOURCE		= 66;// 资源战

	/** 67 新版战斗邮件 坐标类型 6 地宫 废墟探险不是打怪*/
	public static final int   MAIL_NEWVERSION_BATTLEREPORT_MONSTERTILE = 67;
	/** 68 新版战斗邮件 坐标类型 9 野怪*/
	public static final int   MAIL_NEWVERSION_BATTLEREPORT_FIELDMONSTER= 68;
	/** 69 新版战斗邮件 坐标类型 10 王座：发射中心*/
	public static final int   MAIL_NEWVERSION_BATTLEREPORT_THRONE = 69;
	/** 70 新版战斗邮件 坐标类型 12 投石机:巨型巨炮*/
	public static final int   MAIL_NEWVERSION_BATTLEREPORT_TREBUCHET = 70;
	/** 71 新版战斗邮件 坐标类型 14 联盟哨塔 包含主堡与副堡 两种建筑*/
	public static final int   MAIL_NEWVERSION_BATTLEREPORT_ALLIANCETOWER = 71;
	/** 72 新版战斗邮件 坐标类型 15 世界BOSS*/
	public static final int   MAIL_NEWVERSION_BATTLEREPORT_WORLDBOSS = 72;
	/** 73 新版战斗邮件 坐标类型 26 联盟资源站*/
	public static final int   MAIL_NEWVERSION_BATTLEREPORT_ALIIANCERESOURCESTATION = 73;
	/** 74 新版战斗邮件 坐标类型 28 探索点占领形*/
	public static final int   MAIL_NEWVERSION_BATTLEREPORT_EXPLORATIONPOINT = 74;
	/** 75 新版战斗邮件 坐标类型 29 攻城略地建筑点*/
	public static final int   MAIL_NEWVERSION_BATTLEREPORT_BATTLEGAMEFIGHT = 75;
	/** 76 新版战斗邮件 坐标类型 31 NPC城点 最强要塞物资站*/
	public static final int   MAIL_NEWVERSION_BATTLEREPORT_FORTRESSMATERIALSTATION = 76;
	/** 77 新版战斗邮件 坐标类型 33 联盟兵营*/
	public static final int   MAIL_NEWVERSION_BATTLEREPORT_ALLIANCEBARRACKS = 77;

	public static final int  MAIL_NEWVERSION_BATTLEREPORT_DESERTGRAVE		= 78;// 沙漠建筑 古代墓穴

	public static final int  MAIL_NEWVERSION_BATTLEREPORT_PYRAMID		= 79;// 金字塔争夺战

	public static final int  MAIL_NEWVERSION_BATTLEREPORT_CASTLE		= 80;// 战争武器(世界堡垒） 副堡



	public static final int  MAIL_NEWVERSION_BATTLEREPORT_EXPEDITION		= 83;// 联盟远征（边境战）

	public static final int MAIL_NEWVERSION_BATTLEREPORT_NOTCOVERDOMAIN = 84; //不能压地块

	public static final int MAIL_NEWVERSION_BATTLEREPORT_FIGHTDOMAINMARL = 85; //地块争夺标记

	public static final int  MAIL_NEWACTIVITY_PERSONALARM		= 86;// 个人军备邮件新分类(活动)

	public static final int  MAIL_NEWACTIVITY_ALLIANCERAM		= 87;// 联盟军备邮件新分类(活动)

	public static final int  MAIL_NEWACTIVITY_GREATKING		= 88;// 最强要塞邮件新分类(活动)

	public static final int  MAIL_NEWACTIVITY_DESERT		= 89;// 沙漠活动邮件新分类(活动)

	public static final int  MAIL_NEWVERSION_BATTLEREPORT_MOBILIZATION		= 90;// 新版战斗邮件 坐标类型 51 动员中心战斗

	public static final int  MAIL_COMBOTFACTORY_FIRE = 91;//战备工厂 (射击邮件)

	public static final int  MAIL_WORLD_MONSTER_ELITE = 92;  //3d怪物邮件
	public static final int	 MAIL_SC_FINGHT_RUINS = 93;
	public static final int  MAIL_SC_FINGHT_CITY = 94;
	public static final int  MAIL_NEWVERSION_ARENA_THRONE = 95;	    //新版战斗邮件竞技场 坐标类型 10 王座：发射中心
	public static final int  MAIL_NEWVERSION_ARENA_TREBUCHET = 96;  //新版战斗邮件竞技场 坐标类型 12 投石机:巨型巨炮
	public static final int  MAIL_TYPE_ALLIANCE_BOSS = 97 ;         //联盟BOSS打血量
	public static final int  MAIL_TYPE_ALLIANCE_BOSS_TREASURE = 98; //联盟BOSS采集宝藏 = 97;
	public static final int  MAIL_NEWACTIVITY_WORLDCUP = 99; //联盟BOSS采集宝藏 = 97;
	public static final int  MAIL_TYPE_COUNT   = 100; //邮件类型总数,增加邮件需修改

	// 世界建筑
	public static final int			OriginTile						= 0;
	public static final int			CityTile						= 1;
	public static final int			CampTile						= 2;	// 扎营地
	public static final int			ResourceTile					= 3;	// 资源
	public static final int			KingTile						= 4;	// 遗迹
	public static final int			BattleTile						= 5;	// 塔
	public static final int			MonsterTile						= 6;	// 地宫
	public static final int			MonsterRange					= 7;
	public static final int			CityRange						= 8;	// 玩家周边
	public static final int			FieldMonster					= 9;	// 野怪
	public static final int			Throne							= 10;	// 王座
	public static final int			ThroneRange						= 11;	// 王座周边
	public static final int			Trebuchet						= 12;	// 投石机
	public static final int			TrebuchetRange					= 13;	// 投石机周边
	public static final int			Tile_allianceArea				= 14;
	public static final int			ActBossTile						= 15;	// 活动怪物boss
	public static final int			Tile_allianceRange				= 16;	// 领地周边16
	public static final int			ActBossTileRange				= 17;
	public static final int			tile_superMine					= 18;
	public static final int			tile_superMineRange				= 19;
	public static final int			tile_tower						= 20;
	public static final int			tile_wareHouse					= 21;
	public static final int			tile_wareHouseRange				= 22;
	public static final int			tile_banner						= 23;	// 联盟国旗
	public static final int			Crystal							= 24;	// 水晶
	public static final int			Crystal_Range					= 25;	// 水晶周边
	public static final int			Armory							= 26;	// 军械库
	public static final int			Armory_Range					= 27;	// 军械库周边
	public static final int			TrainingField					= 28;	// 训练场
	public static final int			TrainingField_Range				= 29;	// 训练场周边
	public static final int			SupplyPoint						= 30;	// 供给点
	public static final int			BessingTower					= 31;	// 祝福塔
	public static final int			MedicalTower					= 32;	// 医疗塔
	public static final int			DragonTower						= 33;	// 龙塔
	public static final int			Barracks						= 34;	// 兵营,骑士大厅
	public static final int			Barracks_Range					= 35;	// 兵营周边
	public static final int			TransferPoint					= 36;	// 传送点

	public static final int			SecondCityPoint					= 58;	// 第二基地

	private static MailManager		_instance						= null;
	public static IAnalyticTracker	tracker							= null;

	public int						leastestUserMailCreateTime		= 0;
	public String					leastestUserMailUid				= "";
	public int						leastestSystemMailCreateTime	= 0;
	public String					leastestSystemMailUid			= "";
	private List<String>			transportedMailUidList			= null;
	private String 					showingMailUid					= "";
	public static boolean			hasMoreNewMailToGet				= false;
	public static String			latestMailUidFromGetNew			= "";

	//个人邮件使用
	public static boolean			hasMoreNewPersonMailToGet				= false;
	public static String			latestPersonMailUidFromGetNew			= "";

	private static ExecutorService		executorService			= null;
	public static ExecutorService      chatMailExecutorService = null;


	private MailManager()
	{
		transportedMailUidList = new ArrayList<String>();
		executorService = Executors.newFixedThreadPool(4);
		chatMailExecutorService = Executors.newFixedThreadPool(1);
	}

	public void clearData(){
		transportedMailUidList.clear();
	}

	public void runOnExecutorService(Runnable runnable)
	{
		if(runnable!=null)
			executorService.execute(runnable);
	}

	public void runOnExecutorChatMailService(Runnable runnable)
	{
		if(runnable!=null) {
			chatMailExecutorService.execute(runnable);
		}
	}

	public String getShowingMailUid()
	{
		return showingMailUid;
	}



	public void setShowingMailUid(String showingMailUid)
	{
		this.showingMailUid = showingMailUid;
	}



	public static MailManager getInstance()
	{
		if (_instance == null)
		{
			synchronized (MailManager.class)
			{
				if (_instance == null)
					_instance = new MailManager();
			}
		}
		return _instance;
	}

	public void addMailInTransportedList(String mailUid)
	{
		if(transportedMailUidList == null)
			return;
		if(!isInTransportedMailList(mailUid))
			transportedMailUidList.add(mailUid);
	}

	public boolean isInTransportedMailList(String mailUid)
	{
		if(transportedMailUidList == null)
			return false;
		boolean ret = transportedMailUidList.contains(mailUid);
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "isInTranslatedMailList ret", ret);
		return ret;
	}

	public void transportMailInfo(String jsonStr, boolean isShowDetectMail,boolean isForViewChange)
	{

		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "jsonStr", jsonStr);
		if (tracker != null)
		{
			System.out.println("tracker.transportMail not isInTransportedMailList:"+jsonStr);
			tracker.transportMail(jsonStr, isShowDetectMail,isForViewChange);
		}
	}

	public String getPublishChannelName()
	{
		if (tracker != null)
			return tracker.getPublishChannelName();
		return "";
	}

	public List<Integer> getChannelTypeArrayByChannel(String channelId)
	{
		List<Integer> typeArray = new ArrayList<Integer>();
		if (channelId.equals(MailManager.CHANNELID_FIGHT))
		{
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
			typeArray.add(MailManager.MAIL_DETECT);
			typeArray.add(MailManager.MAIL_DETECT_REPORT);
			typeArray.add(MailManager.MAIL_ENCAMP);
			typeArray.add(MailManager.WORLD_NEW_EXPLORE);
			typeArray.add(MailManager.WORLD_MONSTER_SPECIAL);
			typeArray.add(MailManager.Mail_CASTLE_ACCOUNT);
			typeArray.add(MailManager.Mail_CASTLEGAMEFIGHT);

			typeArray.add(MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYER_RESOURCE);
			typeArray.add(MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYERCITY);
			typeArray.add(MailManager.MAIL_NEWVERSION_BATTLEREPORT_THRONE);
			typeArray.add(MailManager.MAIL_NEWVERSION_BATTLEREPORT_ALLIANCETOWER);
			typeArray.add(MailManager.MAIL_NEWVERSION_BATTLEREPORT_DESERTGRAVE);
			typeArray.add(MailManager.MAIL_NEWVERSION_BATTLEREPORT_CASTLE);
			typeArray.add(MailManager.MAIL_SC_FINGHT_RUINS);
			typeArray.add(MailManager.MAIL_SC_FINGHT_CITY);
		}
		else if (channelId.equals(MailManager.CHANNELID_ALLIANCE))
		{
			typeArray.add(MailManager.MAIL_SYSTEM);
			typeArray.add(MailManager.MAIL_DONATE);
			typeArray.add(MailManager.MAIL_ALLIANCEINVITE);
			typeArray.add(MailManager.MAIL_Alliance_ALL);
			typeArray.add(MailManager.MAIL_ALLIANCEAPPLY);
			typeArray.add(MailManager.MAIL_ALLIANCE_KICKOUT);
			typeArray.add(MailManager.MAIL_INVITE_TELEPORT);
			typeArray.add(MailManager.MAIL_REFUSE_ALL_APPLY);
			typeArray.add(MailManager.MAIL_RESOURCE_HELP);
			typeArray.add(MailManager.MAIL_ALLIANCE_PACKAGE);
			typeArray.add(MailManager.MAIL_ALLIANCEOFFICEAPPLY);
			typeArray.add(MailManager.MAIL_ALLIANCE_UNSIGN);
			typeArray.add(MailManager.MAIL_ALLIANCE_DONATE);
			typeArray.add(MailManager.MAIL_ALLIANCE_COMMON);
			typeArray.add(MailManager.MAIL_TYPE_ALLIANCE_BOSS);
			typeArray.add(MailManager.MAIL_TYPE_ALLIANCE_BOSS_TREASURE);
		}
		else if (channelId.equals(MailManager.CHANNELID_STUDIO))
		{
			typeArray.add(MailManager.ALL_SERVICE);
			typeArray.add(MailManager.MAIL_SYSUPDATE);
			typeArray.add(MailManager.MAIL_VOTE);
		}
		else if (channelId.equals(MailManager.CHANNELID_SYSTEM))
		{
			typeArray.add(MailManager.MAIL_SYSNOTICE);
			typeArray.add(MailManager.MAIL_SYSTEM);
			typeArray.add(MailManager.MAIL_SERVICE);
			typeArray.add(MailManager.MAIL_FRESHER);
			typeArray.add(MailManager.MAIL_WOUNDED);
			typeArray.add(MailManager.MAIL_GIFT);
			typeArray.add(MailManager.MAIL_USERHERO);
			typeArray.add(MailManager.MAIL_MOVE_FORTRESS);
			typeArray.add(MailManager.MAIL_PERSIDENT_SEND);
			typeArray.add(MailManager.MAIL_GIVE_SOLDIER);
			typeArray.add(MailManager.MAIL_GIFT_RECEIVE);
		}
		else if (channelId.equals(MailManager.CHANNELID_RESOURCE))
		{
			typeArray.add(MailManager.MAIL_RESOURCE);
		}
		else if (channelId.equals(MailManager.CHANNELID_KNIGHT))
		{
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
			typeArray.add(MailManager.Mail_KNIGHTGAMEFIGHT);
			typeArray.add(MailManager.Mail_SHAMOKNIGHTGAMEFIGHT);
		}
		else if (channelId.equals(MailManager.CHANNELID_MONSTER))
		{
			typeArray.add(MailManager.MAIL_ATTACKMONSTER);
		}
		else if (channelId.equals(MailManager.CHANNELID_GIFT))
		{
			typeArray.add(MailManager.MAIL_GIFT_BUY_EXCHANGE);
		}
		else if (channelId.equals(MailManager.CHANNELID_MISSILE))
		{
			typeArray.add(MailManager.MAIL_MISSILE);
		}
		else if (channelId.equals(MailManager.CHANNELID_EVENT_NORMAL))
		{
			typeArray.add(MailManager.MAIL_WORLD_BOSS);
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
			typeArray.add(MailManager.Mail_GAMEFIGHTACTIVITY);
			typeArray.add(MailManager.Mail_ACTIVITY_NEW);
			typeArray.add(MailManager.MAIL_WORLD_MONSTER_ELITE);
			typeArray.add(MailManager.MAIL_NEWACTIVITY_WORLDCUP);
		}
		else if (channelId.equals(MailManager.CHANNELID_EVENT_PERSONALARM))
		{
			typeArray.add(MailManager.MAIL_NEWACTIVITY_PERSONALARM);
		}
		else if (channelId.equals(MailManager.CHANNELID_EVENT_ALLIANCERAM))
		{
			typeArray.add(MailManager.MAIL_NEWACTIVITY_ALLIANCERAM);
		}
		else if (channelId.equals(MailManager.CHANNELID_EVENT_GREATKING))
		{
			typeArray.add(MailManager.MAIL_NEWACTIVITY_GREATKING);
		}
		else if (channelId.equals(MailManager.CHANNELID_EVENT_DESERT))
		{
			typeArray.add(MailManager.MAIL_NEWACTIVITY_DESERT);
		}
		else if (channelId.equals(MailManager.CHANNELID_BATTLEGAME))
		{
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
			typeArray.add(MailManager.Mail_BATTLEGAMEFIGHT);
		}
		else if (channelId.equals(MailManager.CHANNELID_ARENAGAME))
		{
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
			typeArray.add(MailManager.MAIL_DETECT);
			typeArray.add(MailManager.MAIL_DETECT_REPORT);
			typeArray.add(MailManager.Mail_DETECT_ARENA);
			typeArray.add(MailManager.Mail_DETECT_REPORT_ARENA);
			typeArray.add(MailManager.Mail_ARENAGAMEFIGHT);
			typeArray.add(MailManager.MAIL_NEWVERSION_ARENA_THRONE);
			typeArray.add(MailManager.MAIL_NEWVERSION_ARENA_TREBUCHET);

		}
		else if (channelId.equals(MailManager.CHANNELID_SHAMOGAME))
		{
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
			typeArray.add(MailManager.Mail_SHAMOGAMEFIGHT);
			typeArray.add(MailManager.MAIL_NEWVERSION_BATTLEREPORT_PYRAMID);

		}
		else if (channelId.equals(MailManager.CHANNELID_SHAMOEXPLORE))
		{
			typeArray.add(MailManager.MAIL_ShaMoExplore);
		}
		else if (channelId.equals(MailManager.CHANNELID_BORDERFIGHT))
		{
			typeArray.add(MailManager.Mail_BORDERFIGHT);
			typeArray.add(MailManager.MAIL_NEWVERSION_BATTLEREPORT_EXPEDITION);
		}
		else if (channelId.equals(MailManager.CHANNELID_SHAMOGOLDDIGGER))
		{
			typeArray.add(MailManager.MAIL_SHAMOGOLDDIGEGER);
			typeArray.add(MailManager.MAIL_NEWVERSION_BATTLEREPORT_NOTCOVERDOMAIN);
			typeArray.add(MailManager.MAIL_NEWVERSION_BATTLEREPORT_FIGHTDOMAINMARL);
		}
		else if (channelId.equals(MailManager.CHANNELID_MOBILIZATION_CENTER))
		{
			typeArray.add(MailManager.MAIL_MOBILIZATION_CENTER);
		}
		else if (channelId.equals(MailManager.CHANNELID_COMBOTFACTORY_FIRE)){
			typeArray.add(MailManager.MAIL_COMBOTFACTORY_FIRE);
			typeArray.add(MailManager.MAIL_NEWVERSION_BATTLEREPORT_MOBILIZATION);
		}
		return typeArray;
	}

	public MailData parseMailDataContent(MailData mailData)
	{
		MailData mail = null;
		switch (mailData.getType())
		{
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
			{
				mail = new BattleMailData();
			}
			break;
			case MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYER_RESOURCE:
			case MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYERCITY:
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
				mail = new NewVersionBattleMailData();
				break;
			case MailManager.MAIL_RESOURCE:
				mail = new ResourceMailData();
				break;
			case MailManager.MAIL_DETECT_REPORT:
			case MailManager.Mail_DETECT_REPORT_ARENA:
				mail = new DetectReportMailData();
				break;
			case MailManager.MAIL_ENCAMP:
				mail = new OcupyMailData();
				break;
			case MailManager.WORLD_NEW_EXPLORE:
				mail = new WorldExploreMailData();
				break;
			case MailManager.MAIL_ALLIANCEINVITE:
				mail = new AllianceInviteMailData();
				break;
			case MailManager.MAIL_ALLIANCEAPPLY:
				mail = new AllianceApplyMailData();
				break;
			case MailManager.MAIL_ATTACKMONSTER:
				mail = new MonsterMailData();
				break;
			case MailManager.MAIL_GIFT_BUY_EXCHANGE:
				mail = new GiftMailData();
				break;
			case MailManager.MAIL_MISSILE:
				mail = new MissileMailData();
				break;
			case MailManager.MAIL_RESOURCE_HELP:
				mail = new ResourceHelpMailData();
				break;
			case MailManager.MAIL_INVITE_TELEPORT:
				mail = new InviteTeleportMailData();
				break;
			case MailManager.MAIL_ALLIANCE_KICKOUT:
				mail = new AllianceKickOutMailData();
				break;
			case MailManager.MAIL_WORLD_BOSS:
			case MailManager.MAIL_WORLD_MONSTER_ELITE:
			case MailManager.MAIL_TYPE_ALLIANCE_BOSS:
			case MailManager.MAIL_TYPE_ALLIANCE_BOSS_TREASURE:
				if (mailData.isWorldBossKillRewardMail())
					mail = new MailData();
				else
					mail = new WorldBossMailData();
				break;
			case MailManager.MAIL_REFUSE_ALL_APPLY:
				mail = new RefuseAllReplyMailData();
				break;
			case MailManager.Mail_CASTLE_ACCOUNT:
				mail = new BattleEndMailData();
				break;
			case MailManager.MAIL_MOBILIZATION_CENTER:
				mail = new MobilizationMailData();
				break;
			default:
				mail = new MailData();
				break;
		}
		mail.setMailData(mailData);
		mail.parseContents();
		mail.setNeedParseByForce(false);
		return mail;
	}

	public String getMailIconByName(String name)
	{
		return MailNewUI.getInstance().getIconByName(name);
	}

	public String transportNeiberMailData(String channelId,String mailUid, boolean needEarly, boolean needNext)
	{
		ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
		if (channel == null || channel.mailDataList == null || channel.mailDataList.size() <= 0 || !(needEarly || needNext))
			return "";

		for (int i = 0; i < channel.mailDataList.size(); i++)
		{
			MailData mail_cur = channel.mailDataList.get(i);
			if (mail_cur != null && mail_cur.getUid().equals(mailUid))
			{
				String uid = "";
				MailData checkMail=null;

				if (needEarly && i - 1 >= 0)
				{
					checkMail = channel.mailDataList.get(i - 1);
					if (checkMail != null)
					{
						transportMailData(checkMail,true);
						uid = checkMail.getUid();
					}
				}
				if (needNext && i + 1 < channel.mailDataList.size())
				{
					checkMail = channel.mailDataList.get(i + 1);
					if (checkMail != null)
					{
						transportMailData(checkMail,true);
						uid = checkMail.getUid();
					}
				}

				if(checkMail!=null&&StringUtils.isNotEmpty(uid))
				{
					if (checkMail.isUnread())
					{
						checkMail.setStatus(1);
						JniController.getInstance().excuteJNIVoidMethod("readMail", new Object[] { uid, Integer.valueOf(checkMail.getType()) });
						DBManager.getInstance().updateMail(checkMail);

						if (channel.unreadCount > 0)
						{
							channel.unreadCount--;
							ChannelManager.getInstance().calulateAllChannelUnreadNum();
						}
						channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
						DBManager.getInstance().updateChannel(channel);
					}
					return uid;
				}

				break;
			}
		}
		return "";
	}

	public void transportMailData(MailData mailData,boolean isForChangeView)
	{
		LogUtil.trackPageView("transportMailData1-" + mailData.getUid());
		if (mailData != null)
		{
			LogUtil.trackPageView("transportMailData2-" + mailData.getUid());
			if (!MailManager.getInstance().isInTransportedMailList(mailData.getUid()))
			{
				LogUtil.trackPageView("transportMailData3-" + mailData.getUid());
				try
				{
					String jsonStr = "";
					if (!mailData.isChannelMail()
							&& ((mailData.isComplexMail() && !mailData.hasMailOpend) || ((mailData.getType() == MailManager.MAIL_BATTLE_REPORT||mailData.getType() == MailManager.Mail_BORDERFIGHT||mailData.getType() == MailManager.Mail_BATTLEGAMEFIGHT
							||mailData.getType() == MailManager.Mail_SHAMOGAMEFIGHT||mailData.getType() == MailManager.Mail_CASTLEGAMEFIGHT||mailData.getType() == MailManager.Mail_ARENAGAMEFIGHT
							||mailData.getType() == MailManager.Mail_GAMEFIGHTACTIVITY||mailData.getType() == MailManager.MAIL_SHAMOGOLDDIGEGER||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYER_RESOURCE
							||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYERCITY || mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_THRONE || mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_ALLIANCETOWER
							||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_EXPEDITION||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_PYRAMID
							||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_MOBILIZATION||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_DESERTGRAVE||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_CASTLE
							||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_NOTCOVERDOMAIN||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_FIGHTDOMAINMARL
							|| mailData.getType() == MailManager.MAIL_NEWVERSION_ARENA_THRONE || mailData.getType() == MailManager.MAIL_NEWVERSION_ARENA_TREBUCHET
							|| mailData.getType() == MailManager.MAIL_SC_FINGHT_RUINS || mailData.getType() == MailManager.MAIL_SC_FINGHT_CITY
					) && !mailData.getChannelId().equals(MailManager.CHANNELID_KNIGHT))))
					{

						mailData.setNeedParseByForce(true);
						MailData mail = MailManager.getInstance().parseMailDataContent(mailData);
						if (mailData.getType() == MailManager.MAIL_BATTLE_REPORT||mailData.getType() == MailManager.Mail_BORDERFIGHT||mailData.getType() == MailManager.Mail_BATTLEGAMEFIGHT
								||mailData.getType() == MailManager.Mail_SHAMOGAMEFIGHT||mailData.getType() == MailManager.Mail_CASTLEGAMEFIGHT||mailData.getType() == MailManager.Mail_ARENAGAMEFIGHT
								||mailData.getType() == MailManager.Mail_KNIGHTGAMEFIGHT||mailData.getType() == MailManager.Mail_SHAMOKNIGHTGAMEFIGHT||mailData.getType() == MailManager.Mail_GAMEFIGHTACTIVITY
								||mailData.getType() == MailManager.MAIL_SHAMOGOLDDIGEGER||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYER_RESOURCE || mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_THRONE
								||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_PLAYERCITY || mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_ALLIANCETOWER ||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_EXPEDITION||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_PYRAMID
								||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_MOBILIZATION||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_DESERTGRAVE||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_CASTLE
								||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_NOTCOVERDOMAIN||mailData.getType() == MailManager.MAIL_NEWVERSION_BATTLEREPORT_FIGHTDOMAINMARL
								|| mailData.getType() == MailManager.MAIL_NEWVERSION_ARENA_THRONE || mailData.getType() == MailManager.MAIL_NEWVERSION_ARENA_TREBUCHET
								|| mailData.getType() == MailManager.MAIL_SC_FINGHT_RUINS || mailData.getType() == MailManager.MAIL_SC_FINGHT_CITY)
							mail.setContents("");
						jsonStr = JSON.toJSONString(mail);
						LogUtil.trackPageView("transportMailData4-" +jsonStr);
					}
					else
					{
						jsonStr = JSON.toJSONString(mailData);
						LogUtil.trackPageView("transportMailData5-" + jsonStr);
					}
					System.out.println("transportMailInfo not isInTransportedMailList:"+jsonStr);
					MailManager.getInstance().transportMailInfo(jsonStr, false,isForChangeView);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
