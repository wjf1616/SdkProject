package com.chatsdk.model;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.image.AsyncImageLoader;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.db.DBHelper;
import com.chatsdk.util.ImageUtil;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.ScaleUtil;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 全局配置
 */
public class ConfigManager
{
	private static ConfigManager	instance;

	/** 只对当前用户有意义，不存数据库 */
	public boolean					scaleFontandUI			= true;
	public boolean					enableChatInputField	= false;
	public boolean					isFirstJoinAlliance		= true;
	/** 用于论坛判定默认显示语言，尚未使用，与游戏进程相关 */
	public String					gameLang				= "zh-CN";
	public String					translateURL			= "https://translate.im30app.com/v2/client.php";

	/** 后台可配置，会被复写 */
	public static int				sendInterval			= 1000;
	/** 自定义头像开关 */
	public static boolean			enableCustomHeadImg		= true;
	public static int				autoTranlateMode		= 0;
	public static boolean			enableChatHorn			= true;
	/** 喇叭消息最大输入长度 */
	public static int				maxHornInputLength		= 140;
	/** 总统邮件最大输入长度 */
	public static int				maxPersidentInputLength	= 3000;
	public static boolean			isFirstUserHorn			= true;
	public static boolean			isFirstUserCornForHorn	= true;
	public static boolean			isHornBtnEnable			= false;
	public static boolean			useWebSocketServer		= false;
	public static boolean			isRecieveFromWebSocket	= true;
	public static boolean			isSendFromWebSocket		= true;
	public static boolean			isXMEnabled							= false;
	public static boolean			isXMAudioEnabled					= false;
	public static boolean			isXMVedioEnabled					= false;
	public static boolean           isEnterArena           = false;     //是否进入了竞技场
	public static boolean           isIndependentLeague    = false;     //是否开启独立联盟
	public static boolean           isBackCloseSocket      = false;     //测试用
	public static boolean           isSpeechRecognition    = false;     //是否支持google语音输入

	// 红包
	public static boolean			isRedPackageEnabled					= false;
	public static boolean			isRedPackageShakeEnabled			= false;
	public static boolean			isNewShieldingEnabled				= false;//是否开启了新的语言过滤配置开关

	private Map<String, String>		dynamicImageMap			= null;
	private boolean 				hasMergered				= false;
	public static boolean			playAudioBySpeaker					= true;
	public static final int			NETWORK_DISCONNECTED				= 1;							// 网络信号断开
	public static final int			NETWORK_OPENED						= 2;							// 网络信号打开
	public static final int			SERVER_DISCONNECTED					= 3;							// 服务器断开
	public static final int			NETWORK_CONNECTING					= 4;							// 正在连接
	public static final int			NETWORK_CONNECTED					= 5;							// 连接成功
	public static final int			NETWORK_CONNECTE_FAILED				= 6;							// 连接失败
	public static final int			WEBSOCKET_NETWORK_CONNECTING		= 7;							// websocket网络正在连接
	public static final int			WEBSOCKET_NETWORK_CONNECTED			= 8;							// websocket网络连接成功
	public static final int			WEBSOCKET_NETWORK_CONNECTE_FAILED	= 9;							// websocket网络连接失败
	public static final int			WEBSOCKET_SERVER_DISCONNECTED		= 10;							// websocket网络服务器断开
	public static final int			ACTIVITY_STATE_NORMAL				= 0;
	public static final int			MAIL_PULLING						= 1;
	public static int				network_state						= NETWORK_CONNECTED;
	public static int				websocket_network_state				= WEBSOCKET_NETWORK_CONNECTED;
	public static int				mail_pull_state						= ACTIVITY_STATE_NORMAL;
	public static int 			 	localUpdateConfigVersion			= -1 ;
	public static final String		DYNAMIC_IMAGE_CDN_URL				= "http://cok.eleximg.com/cok/config/chat_service_res/";
	public static boolean isNetWorkConnecting()
	{
		return network_state == NETWORK_CONNECTING;
	}
	public static String getCDNUrl(String fileName)
	{
		return DYNAMIC_IMAGE_CDN_URL + fileName;
	}
	public static int               activityType                        = -1;//0：聊天 1:邮件
	public static boolean isNetWorkError()
	{
		if (network_state == NETWORK_DISCONNECTED || network_state == NETWORK_OPENED || network_state == SERVER_DISCONNECTED
				|| network_state == NETWORK_CONNECTE_FAILED)
			return true;
		return false;
	}
	public static boolean isWebSocketNetWorkConnecting()
	{
		return websocket_network_state == WEBSOCKET_NETWORK_CONNECTING
				&& (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY || ChatServiceController
						.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_ALLIANCE);
	}
	public static boolean isWebSocketNetWorkError()
	{
		if ((websocket_network_state == WEBSOCKET_NETWORK_CONNECTE_FAILED || websocket_network_state == WEBSOCKET_SERVER_DISCONNECTED)
				&& (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY || ChatServiceController
						.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_ALLIANCE))
			return true;
		return false;
	}
	public static boolean isWebSocketNetWorkNormal()
	{
		return websocket_network_state == WEBSOCKET_NETWORK_CONNECTED;
	}
	public static boolean isNetWorkNormal()
	{
		return network_state == NETWORK_CONNECTED;
	}

	private ConfigManager()
	{

	}

	public static ConfigManager getInstance()
	{
		if (instance == null)
		{
			instance = new ConfigManager();
		}
		return instance;
	}

	private static boolean		calcSizeCompleted	= false;
	public static double		scaleRatio			= 0;
	public static double		scaleRatioButton	= 0;
	private final static double	designWidth			= 640;
	private final static double	designHeight		= 852;

	public static void calcScale(Context context)
	{
		if (calcSizeCompleted || context == null)
			return;

		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		double screenWidth = windowManager.getDefaultDisplay().getWidth();
		double screenHeight = windowManager.getDefaultDisplay().getHeight();
		double scaleX = screenWidth / designWidth;
		double scaleY = screenHeight / designHeight;
		double minScale = Math.min(scaleX, scaleY);
		scaleRatio = minScale;
		scaleRatioButton = minScale;
		// 在大屏上字体可能会偏大，可能需要用dp计算才行,先加个修正因子
		scaleRatio = scaleRatio > (1 / 0.84390234277028) ? scaleRatio * 0.84390234277028 : scaleRatio;
		if (scaleRatio > 1)
		{
			// 小米pad是1.873170518056575
			scaleRatio = 1 + (scaleRatio - 1) * 0.5;
		}
		else
		{
			// 小屏幕不要缩放，否则太小（华为 U8800Pro 800x480）
			scaleRatio = 1 - (1 - scaleRatio) * 0.5;
		}
		calcSizeCompleted = true;
	}

	public boolean needScaleInputPanel()
	{
		int density = ChatServiceController.hostActivity.getResources().getDisplayMetrics().densityDpi;
		return density >= DisplayMetrics.DENSITY_XHIGH && ScaleUtil.getScreenWidth() > 1280;
	}
	private String getLocalDynamicConfigPath()
	{
		return DBHelper.getLocalDirectoryPath(ChatServiceController.hostActivity, "config");
	}

	private String getRemoteDynamicConfigPath()
	{
		return DBHelper.getLocalDirectoryPathWithOutSDCard(ChatServiceController.hostActivity, "dresource");
	}
	public int getLocalUpdateConfigVersion()
	{
		if (localUpdateConfigVersion == -1)
		{
			String localJsonPath = getLocalDynamicConfigPath() + "pic_update_local.json";
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "pic_update_local", localJsonPath);
			String json = readJsonFile(localJsonPath);
			if (StringUtils.isNotEmpty(json))
			{
				try
				{
					PicUpdateConfig config = JSON.parseObject(localJsonPath, PicUpdateConfig.class);
					if(config!=null)
						localUpdateConfigVersion = config.getVersion();
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		return localUpdateConfigVersion;
	}
	public void updateLocalPicConfig()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				int version = getLocalUpdateConfigVersion();
				String updateJson = AsyncImageLoader.getHttpString(DYNAMIC_IMAGE_CDN_URL+"pic_update_remote.json");
				if (StringUtils.isNotEmpty(updateJson))
				{
					try
					{
						PicUpdateConfig config = JSON.parseObject(updateJson, PicUpdateConfig.class);
						if(config!=null && config.getVersion() > version)
						{
							String localJsonPath = getLocalDynamicConfigPath() + "pic_update_local.json";
							writeConfig(updateJson, localJsonPath);
							List<String> updateImageList = config.getUpdate();
							for(String imageName : updateImageList)
							{
								System.out.println("updateLocalPicConfig imageName:"+imageName);
								if(ImageUtil.isUpdateImageExist(imageName))
								{
									System.out.println("updateLocalPicConfig needUpdate imageName:"+imageName);
									AsyncImageLoader.getInstance().downLoadUpdateImage(DYNAMIC_IMAGE_CDN_URL+imageName, ImageUtil.getCommonPicLocalPath(imageName));
								}
							}
						}
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public Map<String, String> getLocalDynamicImageMap()
	{
		if (dynamicImageMap == null)
		{
			String localJsonPath = getLocalDynamicConfigPath() + "image_config_local.json";
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "localJsonPath", localJsonPath);
			dynamicImageMap = readLocalImageConfig(localJsonPath);
			Set<String> keySet = dynamicImageMap.keySet();
			for (String key : keySet)
			{
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "local json key", key,"value",dynamicImageMap.get(key));
			}
		}
		return dynamicImageMap;
	}

	public void mergeRemoteDynamicImageMap()
	{
		if(hasMergered)
			return;
		String remoteJsonPath = getRemoteDynamicConfigPath() + "image_config_remote.json";
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "remoteJsonPath", remoteJsonPath);
		Map<String, String> map = readRemoteImageConfig(remoteJsonPath);
		if (map == null || map.size() <=0)
			return;
		int remoteVersion = getConfigFileVersion(map);
		if (remoteVersion < 1)
			return;
		Map<String, String> localConfigMap = getLocalDynamicImageMap();
		int localVersion = getConfigFileVersion(localConfigMap);
		if (remoteVersion <= localVersion)
			return;

		Set<String> keySet = map.keySet();

		for (String key : keySet)
		{
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "remote json key", key,"value",map.get(key));
		}
		localConfigMap.putAll(map);
		writeDynamicImageConfig(localConfigMap);
		Set<String> keySet2 = localConfigMap.keySet();
		hasMergered = true;
		for (String key : keySet2)
		{
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "after merge local json key", key, "value",
					localConfigMap.get(key));
		}
	}

	private int getConfigFileVersion(Map<String, String> map)
	{
		if (map == null)
			return 0;
		String remoteVersion = map.get("version");
		int version = 0;
		if (StringUtils.isNumeric(remoteVersion) && !remoteVersion.contains("."))
			version = Integer.parseInt(remoteVersion);
		return version;
	}
	
	private Map<String, String> readLocalImageConfig(String jsonPath)
	{
		String json = readJsonFile(jsonPath);
		System.out.println("json:" + json);
		Map<String, String> configMap = new HashMap<String, String>();
		if (StringUtils.isNotEmpty(json))
		{
			try
			{
				JSONObject jsonObject = JSON.parseObject(json);
				if(jsonObject!=null)
				{
					Set<String> keySet = jsonObject.keySet();

					for (String key : keySet)
					{
						configMap.put(key, jsonObject.getString(key));
					}
				}
			}
			catch (Exception e)
			{
			}
		}
		return configMap;
	}

	private Map<String, String> readRemoteImageConfig(String jsonPath)
	{
		String json = readJsonFile(jsonPath);
		System.out.println("json:" + json);
		Map<String, String> configMap = new HashMap<String, String>();
		if (StringUtils.isNotEmpty(json))
		{
			try
			{
				JSONObject jsonObject = JSON.parseObject(json);
				Set<String> keySet = jsonObject.keySet();

				for (String key : keySet)
				{
					if(key.equals("version"))
						configMap.put(key, jsonObject.getString(key));
					else
					{
						JSONArray jsonArray = jsonObject.getJSONArray(key);
						if(jsonArray!=null)
						{
							for(int i = 0;i<jsonArray.size();i++)
							{
								configMap.put(jsonArray.getString(i), key);
							}
						}
					}
				}
			}
			catch (Exception e)
			{
			}
		}
		return configMap;
	}

	public String readJsonFile(String path)
	{
		BufferedReader reader = null;
		StringBuffer jsonStrBuffer = new StringBuffer();
		File file = new File(path);
		try
		{
			if (!file.exists())
				file.createNewFile();
			FileReader fileRead = new FileReader(file);
			reader = new BufferedReader(fileRead);
			String tempString = null;
			while ((tempString = reader.readLine()) != null)
			{
				jsonStrBuffer.append(tempString);
			}
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return jsonStrBuffer.toString();
	}

	private void writeConfig(String json,String savePath)
	{
		if (StringUtils.isEmpty(json) || StringUtils.isEmpty(savePath))
			return;
		File file = new File(savePath);
		FileWriter fileWriter = null;
		BufferedWriter bufferWriter = null;
		try
		{

			if (!file.exists())
				file.createNewFile();

			fileWriter = new FileWriter(file);
			bufferWriter = new BufferedWriter(fileWriter);
			bufferWriter.write(json);
			bufferWriter.flush();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (bufferWriter != null)
					bufferWriter.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void writeDynamicImageConfig(Map<String, String> map)
	{
		if (map == null)
			return;
		String mergeJson = JSON.toJSONString(map);
		String localJsonPath = getLocalDynamicConfigPath() + "image_config_local.json";
		writeConfig(mergeJson, localJsonPath);
	}
	public static void setMailPullState(boolean isPulling)
	{
		if (isPulling)
			mail_pull_state = MAIL_PULLING;
		else
			mail_pull_state = ACTIVITY_STATE_NORMAL;
		if(ChatServiceController.getCurrentActivity()!=null && ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "isPulling",isPulling);
			ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					if(ChatServiceController.getCurrentActivity()!=null)
						ChatServiceController.getCurrentActivity().refreshNetWorkState();
				}
			});
		}
	}

	public static class LocalController{
		public static final int DBXml=0;//默认的database
		public static final int TransXml=1;//兵种
		public static final int EventXml=2;//事件
		public static final int DiscoveryXml=3;//
		public static final int NpcBuildingXml=4;//
		public static final int MissileXml=5;//
		public static final int SoldierIntroXml=6;//士兵介绍
		public static final int PlayerGuideXml=7;//新手引导部分介绍
		public static final int MergeShopXml=8;//合服
		public static final int OneYearXml=9;//一周年
		public static final int SevenDayXml=10;//七日礼包
		public static final int DeBuffXml=11;//军官debuff
		public static final int ScoreFixXml=12;//要塞杀敌积分衰减xml
		public static final int FlexDownXml=13;//资源更新xml
		public static final int ExplainXml=14;//描述多语言
		public static final int GreatLaunchCenterXml=15;//大王座特权 xml
		public static final int HeroInfomationXml=16;//heroinfo
		public static final int SiegeFunctionXml=17 ;//
		public static final int SkinInfomationXml=18;
		public static final int HeroExpXml=19;
		public static final int HeroSkillLvUpXml=20;
		public static final int ArenaMapInfoXml = 21;//竞技场地图信息
		public static final int ArenaRankXml = 22;//竞技场rank信息 name pic等
		public static final int HeFuTalentXml=23;
		public static final int HeFuTalentShowXml=24;
		public static final int ExchangePageXml = 25;
		public static final int AllianceEventXml = 26;
		public static final int EquipInfoXml = 27;
		public static final int HeroActivityNextShowInfo = 29;
		public static final int DesertSkillXml = 30;//新天赋技能
		public static final int WorldMineXml = 31;//地雷
		public static final int RankShowOrederXml = 32;//排行榜显示
		public static final int QuickClearCdXml = 33;//
		public static final int PackFiltrateXml = 35;//
		public static final int GoldmineWarXml = 34;//淘金者营地
		public static final int Mail_ChannelIDXml = 36;//报告类邮件是排列顺序
		public static final int President_PolicyXml = 37;//总统特权
		public static final int Battle_AnimationXml = 38;//战斗动画
		public static final int Task_ChapterXml = 39;//章节任务表
		public static final int SiegeCrest_Xml = 40;//战争游戏 军官军徽属性
		public static final int ResourcePointXml = 41;//世界资源田
		public static final int World_Event_Xml = 42;//世界事件
		public static final int Recharge_Xml = 43;//福利中心
		public static final int QueuePush_Xml = 44;
		public static final int Officer_BaSkill_Xml = 45 ;//军官技能表 -栏位表
		public static final int Status_Battle_Xml = 46;//军官军团战技能效果
		public static final int MobilizationCenterXml = 47 ;// 最强要塞 动员中心
		public static final int TotalWarXml = 48 ;//无敌的全面战争的前台表（180个城市名字的基本信息）
		public static final int GiftPosXml = 49 ;// 人物信息界面的礼物和粒子位置
		public static final int MobilizationShopXml = 50 ;// 最强要塞 战备商店
		public static final int QuestionBanXml = 51 ;//答题活动
		public static final int FateXml = 52 ;//军官缘分
		public static final int NewsCenterXml = 53 ;//新闻中心
		public static final int Monster_Model_Xml = 56 ;//3d怪物动作配置表
		public static final int Resolve_Xml = 57 ;//分解道具配置表
		public static final int Second_City_Xml = 59 ;//分城
		public static final int Master_Factory_Xml = 60 ;//万能工厂
		public static final int AllianceBossXml = 61 ;//联盟BOSS
		public static final int TimeQuestXml = 62 ;//限时任务活动 byanning
		public static final int Active_Quest_reward = 63 ;//任务奖励活动 anning
		public static final int AllianceApprenticeXml = 64 ;//联盟学徒
		public static final int WorldCup_ActivityXml = 65;//世界杯活动竞猜总表
		public static final int WorldCup_ActivityOptionXml = 66;//竞猜项
		public static final int LanguageChatRoomXml = 67   ;//多语言聊天室配置表
	}
}
