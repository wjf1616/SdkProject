package com.chatsdk.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.util.HeadPicUtil;
import com.chatsdk.util.ImageUtil;
import com.chatsdk.util.LogUtil;

public class UserInfo implements Cloneable
{
	public int		_id;
	public int		tableVer;
	public String	uid						= "";
	public String	userName				= "";
	public String	allianceId				= "";
	public String	asn						= "";
	public int		allianceRank			= -1;
	/** 加入联盟的时间 */
	public int		joinAllianceTime		= 0;
	/** 国家号，只有本玩家有 */
	public int		serverId				= -1;
	/** 跨服战时的原服id，若为-1表示没有跨服 */
	public int		crossFightSrcServerId	= -2;
	/** 玩家类型，尚未使用；会被C++初始化且不赋初值 */
	public int		type					= 0;
	public String	headPic					= "";
	/** 头像id */
	public int		headPicVer				= -1;
	/** gm和mod信息，如果为"2"、"4"、"5"表示为mod，如果为“3”表示为gm */
	public int		mGmod					= -1;
	/** vip等级，至少为1，由vip points决定，只升不降 */
	public int		vipLevel				= -1;
	public int		svipLevel				= -1;
	/** vip时间，单位为s，有时区，过期则vip暂时失效（等级保留 */
	public int		vipEndTime				= 0;
	/** 上次更新时间 */
	public int		lastUpdateTime			= 0;
	/** 上次聊天时间 */
	public int		lastChatTime			= 0;
	public String	lang					= "";
	/** 月卡 */
	public int      monthCard               = 0;
	/** 月卡 */
	public int      lastAnimateTime         = 0;

	// 运行时
	public boolean	isSelected				= false;
	public boolean	isDummy					= false;

	public int      btnType					= 0;
	public int      chatShowServerId        = 0;
	public long     gold                    = 0;
	public int      level                   = 0;
	/**
	 * 用于初始化时创建当前玩家，以及代表国家、联盟的UserInfo
	 */
	public UserInfo()
	{

	}

	public UserInfo(Cursor c)
	{
		try
		{
			_id = c.getInt(c.getColumnIndex(BaseColumns._ID));
			tableVer = c.getInt(c.getColumnIndex(DBDefinition.COLUMN_TABLE_VER));
			uid = c.getString(c.getColumnIndex(DBDefinition.USER_COLUMN_USER_ID));
			userName = c.getString(c.getColumnIndex(DBDefinition.USER_COLUMN_NICK_NAME));
			allianceId = c.getString(c.getColumnIndex(DBDefinition.USER_COLUMN_ALLIANCE_ID));
			asn = c.getString(c.getColumnIndex(DBDefinition.USER_COLUMN_ALLIANCE_NAME));
			allianceRank = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_ALLIANCE_RANK));
			serverId = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_SERVER_ID));
			crossFightSrcServerId = c.getInt(c.getColumnIndex(DBDefinition.USER_CROSS_FIGHT_SRC_SERVER_ID));
			type = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_TYPE));
			headPic = c.getString(c.getColumnIndex(DBDefinition.USER_COLUMN_HEAD_PIC));
			headPicVer = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_CUSTOM_HEAD_PIC));
			mGmod = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_PRIVILEGE));
			vipLevel = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_VIP_LEVEL));
			svipLevel = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_SVIP_LEVEL));
			vipEndTime = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_VIP_END_TIME));
			lastUpdateTime = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_LAST_UPDATE_TIME));
			lastAnimateTime = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_LAST_ANIMATE_TIME));
			lastChatTime = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_LAST_CHAT_TIME));
			lang = c.getString(c.getColumnIndex(DBDefinition.USER_COLUMN_LANG));
			monthCard = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_MONTHCARD));
			initNullField();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public ContentValues getContentValues()
	{
		ContentValues cv = new ContentValues();
		cv.put(DBDefinition.COLUMN_TABLE_VER, 9);
		cv.put(DBDefinition.USER_COLUMN_USER_ID, uid);
		cv.put(DBDefinition.USER_COLUMN_NICK_NAME, userName);
		cv.put(DBDefinition.USER_COLUMN_ALLIANCE_ID, allianceId);
		cv.put(DBDefinition.USER_COLUMN_ALLIANCE_NAME, asn);
		cv.put(DBDefinition.USER_COLUMN_ALLIANCE_RANK, allianceRank);
		cv.put(DBDefinition.USER_COLUMN_SERVER_ID, serverId);
		cv.put(DBDefinition.USER_CROSS_FIGHT_SRC_SERVER_ID, crossFightSrcServerId);
		cv.put(DBDefinition.USER_COLUMN_TYPE, type);
		cv.put(DBDefinition.USER_COLUMN_HEAD_PIC, headPic);
		cv.put(DBDefinition.USER_COLUMN_CUSTOM_HEAD_PIC, headPicVer);
		cv.put(DBDefinition.USER_COLUMN_PRIVILEGE, mGmod);
		cv.put(DBDefinition.USER_COLUMN_VIP_LEVEL, vipLevel);
		cv.put(DBDefinition.USER_COLUMN_SVIP_LEVEL, svipLevel);
		cv.put(DBDefinition.USER_COLUMN_VIP_END_TIME, vipEndTime);
		cv.put(DBDefinition.USER_COLUMN_LAST_UPDATE_TIME, lastUpdateTime);
		cv.put(DBDefinition.USER_COLUMN_LAST_ANIMATE_TIME, lastAnimateTime);
		cv.put(DBDefinition.USER_COLUMN_LAST_CHAT_TIME, lastChatTime);
		cv.put(DBDefinition.USER_COLUMN_LANG, lang);
		cv.put(DBDefinition.USER_COLUMN_MONTHCARD, monthCard);
		return cv;
	}

	/**
	 * 用于wrapper假消息
	 */
	public UserInfo(int gmod, int allianceRank, int headPicVer, int vipLv, String uidStr, String nameStr, String asnStr, String headPicStr,
			int lastUpdateTime)
	{
		this.vipLevel = vipLv;
		this.vipEndTime = TimeManager.getInstance().getCurrentTime() + 60;
		this.userName = nameStr;
		this.headPic = headPicStr;
		this.uid = uidStr;
		this.asn = asnStr;
		this.mGmod = gmod;
		this.allianceRank = allianceRank;
		this.headPicVer = headPicVer;
		this.lastUpdateTime = lastUpdateTime;
	}

	/**
	 * 用于接受到的消息时，在本地找不到用户信息，临时创建dummy UserInfo
	 */
	public UserInfo(String uidStr)
	{
		uid = uidStr;
		headPic = "g026";
		userName = "";
		isDummy = true;
	}

	public boolean equalsLogically(Object o)
	{
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		if (o == this)
		{
			return true;
		}

		UserInfo user = (UserInfo) o;
		boolean result;
		result = uid.equals(user.uid) && userName.equals(user.userName) && allianceId.equals(user.allianceId) && asn.equals(user.asn)
				&& allianceRank == user.allianceRank && serverId == user.serverId && crossFightSrcServerId == user.crossFightSrcServerId
				&& type == user.type && headPic.equals(user.headPic) && headPicVer == user.headPicVer && mGmod == user.mGmod
				&& vipLevel == user.vipLevel && svipLevel == user.svipLevel && vipEndTime == user.vipEndTime
				&& lastUpdateTime == user.lastUpdateTime && lastChatTime == user.lastChatTime
				&& (lang != null && user.lang != null && lang.equals(user.lang) && monthCard == user.monthCard );

		return result;
	}

	@Override
	public Object clone()
	{
		UserInfo o = null;
		try
		{
			o = (UserInfo) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return o;
	}

	public String getVipInfo()
	{
		int dtime = vipEndTime - TimeManager.getInstance().getCurrentTime();
		String vipInfo = "";
		if (dtime > 0)
		{
			if (svipLevel > 0)
				vipInfo = LanguageManager.getLangByKey(LanguageKeys.SVIP_INFO, String.valueOf(svipLevel));
			else if (vipLevel > 0)
				vipInfo = LanguageManager.getLangByKey(LanguageKeys.VIP_INFO, String.valueOf(vipLevel));
		}
		return vipInfo;
	}
	
	public boolean isSVIP()
	{
		int dtime = vipEndTime - TimeManager.getInstance().getCurrentTime();
		if (dtime > 0 && svipLevel > 0)
			return true;
		return false;
	}
	public boolean isVIP()
	{
		int dtime = vipEndTime - TimeManager.getInstance().getCurrentTime();
		if (dtime > 0 && vipLevel > 0)
			return true;
		return false;
	}

	public int getVipLevel()
	{
		int dtime = vipEndTime - TimeManager.getInstance().getCurrentTime();
		int vipInfo = (vipLevel > 0 && dtime > 0) ? vipLevel : 0;
		return vipInfo;
	}

	public int getMonthCard()
	{
		return this.monthCard;
	}
	
	public int getSVipLevel()
	{
		int dtime = vipEndTime - TimeManager.getInstance().getCurrentTime();
		int vipInfo = (svipLevel > 0 && dtime > 0) ? svipLevel : 0;
		return vipInfo;
	}

	public boolean isCustomHeadImage()
	{
		if (isDummy)
			return false;
		return headPicVer > 0 && headPicVer < 1000000;
	}

	/**
	 * 自定义头像网络URL
	 */
	public String getCustomHeadPicUrl()
	{
		String result = HeadPicUtil.getCustomPicUrl(uid, headPicVer);

		return result;
	}

	/**
	 * 自定义头像本地路径
	 */
	public String getCustomHeadPic()
	{
		String result = HeadPicUtil.getCustomPic(getCustomHeadPicUrl());

		return result;
	}

	public int getHeadPicVer()
	{
		return headPicVer;
	}

	/**
	 * 自定义头像是否存在
	 */
	public boolean isCustomHeadPicExist()
	{
		String path = getCustomHeadPic();
		return ImageUtil.isPicExist(path);
	}

	/**
	 * C++创建的对象可能没有默认值赋值，需要补上
	 */
	public void initNullField()
	{
		if (userName == null)
		{
			userName = "";
		}
		if (allianceId == null)
		{
			allianceId = "";
		}
		if (asn == null)
		{
			asn = "";
		}
		if (headPic == null)
		{
			headPic = "";
		}
	}

	/**
	 * 判断是否是dummy user，特征为：
	 * 不是频道user，且privilege（或AllianceRank、ServerId、CustomHeadPic、VipLevel）为-1
	 * dummy user有两种来源： 1.故意创建的临时dummy user 2.以前曾将dummy user存入了db
	 */
	public boolean isValid()
	{
		boolean result = isChannelUser() || this.mGmod != -1;
		return result;
	}

	public String getNameWithServerId(String senderAlAbbr,String fromName,int senderServerId){
		String senderInfo = fromName;
		if(senderAlAbbr!=null&&senderAlAbbr.length()>0){
			senderInfo = "("+senderAlAbbr+")" + fromName;
		}
		//加服务器编号
		if(UserManager.getInstance().getCurrentUser()!=null) {
            int selfServerId = UserManager.getInstance().getCurrentUser().crossFightSrcServerId >0 ?
                               UserManager.getInstance().getCurrentUser().crossFightSrcServerId:UserManager.getInstance().getCurrentUser().serverId;
			if (senderServerId>0&&senderServerId != selfServerId){
				senderInfo += "#" + senderServerId;
			}
		}
		return senderInfo;
	}
	public boolean isChannelUser()
	{
		return this.uid.contains("@");
	}
}
