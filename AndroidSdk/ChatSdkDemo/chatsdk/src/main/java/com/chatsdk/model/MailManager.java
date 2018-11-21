package com.chatsdk.model;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.model.mail.allianceapply.AllianceApplyMailData;
import com.chatsdk.model.mail.allianceinvite.AllianceInviteMailData;
import com.chatsdk.model.mail.alliancekickout.AllianceKickOutMailData;
import com.chatsdk.model.mail.battle.BattleMailData;
import com.chatsdk.model.mail.battleend.BattleEndMailData;
import com.chatsdk.model.mail.detectreport.DetectReportMailData;
import com.chatsdk.model.mail.fbbattle.FBNewVersionBattleMailData;
import com.chatsdk.model.mail.fbscoutreport.FBDetectReportMailData;
import com.chatsdk.model.mail.gift.GiftMailData;
import com.chatsdk.model.mail.inviteteleport.InviteTeleportMailData;
import com.chatsdk.model.mail.missile.MissileMailData;
import com.chatsdk.model.mail.monster.MonsterMailData;
import com.chatsdk.model.mail.ocupy.OcupyMailData;
import com.chatsdk.model.mail.refuseallreply.RefuseAllReplyMailData;
import com.chatsdk.model.mail.resouce.ResourceMailData;
import com.chatsdk.model.mail.resourcehelp.ResourceHelpMailData;
import com.chatsdk.model.mail.worldboss.WorldBossMailData;
import com.chatsdk.model.mail.worldexplore.WorldExploreMailData;
import com.chatsdk.model.mail.seasonWarZone.SeasonWarZoneMailData;
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

	public static final String	CHANNELID_BATTLEGAME	= "battle_game";  //战争游戏

	public static final String	CHANNELID_ARENAGAME	    = "arena_game";  //末日竞技场

	public static final String	CHANNELID_SHAMOGAME	    = "shamo_game";  //沙漠

	public static final String	CHANNELID_SHAMOEXPLORE	    = "shamo_explore";  //沙漠探索

	public static final String	CHANNELID_BORDERFIGHT	    = "border_fight";  //边境战

	public static final String	CHANNELID_MANORFIGHT	    = "fb_manor_fight";  //属地争夺战


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
//	public static final int			CHAT_ROOM						= 31;
//	public static final int			MAIL_ACTIVITY					= 32;
//	public static final int			MAIL_REFUSE_ALL_APPLY			= 33;
//	public static final int			MAIL_ALLIANCE_PACKAGE			= 34;
//	public static final int			MAIL_ALLIANCE_RANKCHANGE		= 35;
//	/** 邮件类型总数,增加邮件需修改 */
//	public static final int			MAIL_TYPE_COUNT					= 36;
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

	public static final int  Mail_CITY_FIGHT_FB		 = 50;// 自由城建战报
	public static final int  Mail_MONSTER_FIGHT_FB		 = 51;// 城市丧尸攻城
	public static final int  Mail_RESOURCE_FIGHT_FB		 = 52;// 资源战斗
	public static final int  Mail_SPACE_FIGHT_FB		 = 53;// 空地战斗

	public static final int  Mail_ATTACKMONSTER_FIGHT_FB		 = 54;// /*54 普通打怪 **/

	public static final int  Mail_MANOR_FIGHT_FB		 = 55;// /*54 普通打怪 **/

	public static final int  Mail_USER_WORLD_BUILDING_FIGHT_FB		 = 57;// /*57 玩家世界建筑 **/

	public static final int  Mail_NEW_SCOUT_REPORT_FB		 = 58;// /*58 自由城建侦查邮件 **/

	public static final int  Mail_CONTRAST_WORLD_BOSS		 = 59;// /*59 集结世界boss **/

	public static final int  MAIL_ALLIANCE_RECOMMEND		 = 60;// /*60 联盟推荐 **/

	public static final int  MAIL_WARZONE_FB		 = 61;// /*61 最强战区 **/

	public static final int  MAIL_SEASONWARZONE_FB		 = 62;// /*62 最强战区赛季 **/

	public static final int  MAIL_HEROBATTLE_FB		 = 63;// /*63 英雄对决 保存邮件 **/
    
    public static final int  MAIL_CITYLV_UP                  = 64; //基地升级提示

    public static final int  MAIL_CLONE_SOLDIERS                  = 65; //克隆中心复活兵

	public static final int  MAIL_TYPE_COUNT   = 66; //邮件类型总数,增加邮件需修改

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

	public void transportMailInfo(String jsonStr, boolean isShowDetectMail)
	{
		
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "jsonStr", jsonStr);
		if (tracker != null)
		{
			System.out.println("tracker.transportMail not isInTransportedMailList:"+jsonStr);
			tracker.transportMail(jsonStr, isShowDetectMail);
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
			typeArray.add(MailManager.MAIL_HEROBATTLE_FB);

			if (ChatServiceController.new_battlemail)
			{
				typeArray.add(MailManager.Mail_CITY_FIGHT_FB);
				typeArray.add(MailManager.Mail_USER_WORLD_BUILDING_FIGHT_FB);
			}
			if (ChatServiceController.new_resourcebattlemail)
			{
				typeArray.add(MailManager.Mail_RESOURCE_FIGHT_FB);
				typeArray.add(MailManager.Mail_SPACE_FIGHT_FB);
			}
			if (ChatServiceController.scoutmail)
			{
				typeArray.add(MailManager.Mail_NEW_SCOUT_REPORT_FB);
			}
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
			typeArray.add(MailManager.MAIL_ALLIANCE_RECOMMEND);
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
			typeArray.add(MailManager.MAIL_WARZONE_FB);
			typeArray.add(MailManager.MAIL_SEASONWARZONE_FB);
            typeArray.add(MailManager.MAIL_CITYLV_UP);
            typeArray.add(MailManager.MAIL_CLONE_SOLDIERS);
		}
		else if (channelId.equals(MailManager.CHANNELID_RESOURCE))
		{
			typeArray.add(MailManager.MAIL_RESOURCE);
		}
		else if (channelId.equals(MailManager.CHANNELID_KNIGHT))
		{
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
		}
		else if (channelId.equals(MailManager.CHANNELID_MONSTER))
		{
			if (ChatServiceController.monster_mail)
			{
				typeArray.add(MailManager.Mail_ATTACKMONSTER_FIGHT_FB);
			}
			else
			{
				typeArray.add(MailManager.MAIL_ATTACKMONSTER);
			}
			if(ChatServiceController.mass_boss) {
				typeArray.add(MailManager.Mail_CONTRAST_WORLD_BOSS);
			}

		}
		else if (channelId.equals(MailManager.CHANNELID_GIFT))
		{
			typeArray.add(MailManager.MAIL_GIFT_BUY_EXCHANGE);
		}
		else if (channelId.equals(MailManager.CHANNELID_MISSILE))
		{
			typeArray.add(MailManager.MAIL_MISSILE);
		}
		else if (channelId.equals(MailManager.CHANNELID_EVENT))
		{
			typeArray.add(MailManager.MAIL_WORLD_BOSS);
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
		}
		else if (channelId.equals(MailManager.CHANNELID_BATTLEGAME))
		{
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
		}
		else if (channelId.equals(MailManager.CHANNELID_ARENAGAME))
		{
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
			typeArray.add(MailManager.MAIL_DETECT);
			typeArray.add(MailManager.MAIL_DETECT_REPORT);
		}
		else if (channelId.equals(MailManager.CHANNELID_SHAMOGAME))
		{
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
		}
		else if (channelId.equals(MailManager.CHANNELID_SHAMOEXPLORE))
		{
			typeArray.add(MailManager.MAIL_ShaMoExplore);
		}
		else if (channelId.equals(MailManager.CHANNELID_BORDERFIGHT))
		{
			typeArray.add(MailManager.Mail_BORDERFIGHT);
		}
		else if (channelId.equals(MailManager.CHANNELID_MANORFIGHT))
		{
			typeArray.add(MailManager.Mail_MANOR_FIGHT_FB);
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
				mail = new BattleMailData();
				break;
			case MailManager.Mail_CITY_FIGHT_FB:
			case MailManager.Mail_MANOR_FIGHT_FB:
			case MailManager.Mail_USER_WORLD_BUILDING_FIGHT_FB:
			case MailManager.MAIL_HEROBATTLE_FB:
			{
				if (ChatServiceController.new_battlemail)
				{
					mail = new FBNewVersionBattleMailData();
				}else{
					mail = new MailData();
				}
			}
				break;
			case MailManager.Mail_RESOURCE_FIGHT_FB:
			case MailManager.Mail_SPACE_FIGHT_FB:
			{
				if (ChatServiceController.new_resourcebattlemail)
				{
					mail = new FBNewVersionBattleMailData();
				}else{
					mail = new MailData();
				}
			}
                break;
            case MailManager.Mail_ATTACKMONSTER_FIGHT_FB:
            {
                if (ChatServiceController.monster_mail)
                {
                    mail = new FBNewVersionBattleMailData();
                }
				else{
					mail = new MailData();
				}
            }
                break;
			case MailManager.Mail_CONTRAST_WORLD_BOSS:
			{
				if (ChatServiceController.mass_boss) {
					mail = new FBNewVersionBattleMailData();
				} else {
					mail = new MailData();
				}
			}
				break;
			case MailManager.MAIL_RESOURCE:
				mail = new ResourceMailData();
				break;
			case MailManager.MAIL_DETECT_REPORT:
				mail = new DetectReportMailData();
				break;
			case MailManager.Mail_NEW_SCOUT_REPORT_FB:
			{
				if (ChatServiceController.scoutmail)
				{
					mail = new FBDetectReportMailData();
				}else{
					mail = new MailData();
				}
			}
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
			case MailManager.MAIL_ALLIANCE_RECOMMEND:
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
			case MailManager.MAIL_SEASONWARZONE_FB:
				mail = new SeasonWarZoneMailData();
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
	
	public String transportNeiberMailData(MailData mailData, boolean needEarly, boolean needNext)
	{
		ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData.getChannelId());
		if (channel == null || channel.mailDataList == null || channel.mailDataList.size() <= 0 || !(needEarly || needNext))
			return "";

		for (int i = 0; i < channel.mailDataList.size(); i++)
		{
			MailData mail = channel.mailDataList.get(i);
			if (mail != null && mail.getUid().equals(mailData.getUid()))
			{
				String uid = "";
				if (needEarly && i - 1 >= 0)
				{
					MailData earilyMail = channel.mailDataList.get(i - 1);
					if (earilyMail != null)
					{
						transportMailData(earilyMail);
						uid = earilyMail.getUid();
					}
				}
				if (needNext && i + 1 < channel.mailDataList.size())
				{
					MailData nextMail = channel.mailDataList.get(i + 1);
					if (nextMail != null)
					{
						transportMailData(nextMail);
						uid = nextMail.getUid();
					}
				}
				if(StringUtils.isNotEmpty(uid))
					return uid;
				break;
			}
		}
		return "";
	}

	public void transportMailData(MailData mailData)
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
							&& ((mailData.isComplexMail() && !mailData.hasMailOpend) || (mailData.getType() == MailManager.MAIL_BATTLE_REPORT && !mailData
									.getChannelId().equals(MailManager.CHANNELID_KNIGHT))))
					{
						
						mailData.setNeedParseByForce(true);
						MailData mail = MailManager.getInstance().parseMailDataContent(mailData);
						if (mailData.getType() == MailManager.MAIL_BATTLE_REPORT||mailData.getType() == MailManager.Mail_BORDERFIGHT||mailData.getType() == MailManager.Mail_CITY_FIGHT_FB
								||mailData.getType() == MailManager.Mail_MONSTER_FIGHT_FB||mailData.getType() == MailManager.Mail_RESOURCE_FIGHT_FB||mailData.getType() == MailManager.Mail_SPACE_FIGHT_FB
								||mailData.getType() == MailManager.Mail_ATTACKMONSTER_FIGHT_FB||mailData.getType() == MailManager.Mail_MANOR_FIGHT_FB||mailData.getType() == MailManager.Mail_USER_WORLD_BUILDING_FIGHT_FB
								||mailData.getType() == MailManager.Mail_CONTRAST_WORLD_BOSS||mailData.getType() == MailManager.MAIL_HEROBATTLE_FB)
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
					MailManager.getInstance().transportMailInfo(jsonStr, false);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
