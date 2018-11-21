package com.chatsdk.model;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class MailNewUI
{
	private static MailNewUI	_instance	= null;
	private Map<String, String>	newUIMap	= null;

	public static MailNewUI getInstance()
	{
		if (_instance == null)
			_instance = new MailNewUI();
		return _instance;
	}

	public String getIconByName(String name)
	{
		if (newUIMap != null && StringUtils.isNotEmpty(name) && newUIMap.containsKey(name))
			return newUIMap.get(name);
		return "";
	}

	private MailNewUI()
	{
		newUIMap = new HashMap<String, String>();
		newUIMap.put("CHANNEL_ICON_FIGHT", "mail_list_main_icon_battle");
		newUIMap.put("CHANNEL_ICON_STUDIO", "mail_list_main_icon_studio");
		newUIMap.put("CHANNEL_ICON_SYSTEM", "mail_list_main_icon_system");
		newUIMap.put("CHANNEL_ICON_ALLIANCE", "mail_list_main_icon_invite");
		newUIMap.put("CHANNEL_ICON_MOD", "mail_list_main_icon_mod");
		newUIMap.put("CHANNEL_ICON_MESSAGE", "mail_list_main_icon_chat");
		newUIMap.put("CHANNEL_ICON_ANNOUNCEMENT", "mail_list_main_icon_announcement");
		newUIMap.put("CHANNEL_ICON_RESOURCE", "mail_list_main_icon_resource");
		newUIMap.put("CHANNEL_ICON_MONSTER", "mail_list_main_icon_monster");
		newUIMap.put("CHANNEL_ICON_GIFT", "mail_list_main_icon_gift");
		newUIMap.put("CHANNEL_ICON_KNIGHT", "mail_list_main_icon_knight");
		newUIMap.put("CHANNEL_ICON_MISSILE", "mail_list_main_icon_missile");
		newUIMap.put("CHANNEL_ICON_EXPLORE", "mail_list_main_icon_explore");
		newUIMap.put("CHANNEL_ICON_FAVORITE", "mail_list_main_icon_favorite");
		newUIMap.put("CHANNEL_ICON_EVENT", "mail_list_main_icon_activity");

		// 新邮件UI邮件ICON
		newUIMap.put("MAIL_ICON_ANNOUNCEMENT", "mail_list_icon_announcement");
		newUIMap.put("MAIL_ICON_BATTLE_EXPLORE", "mail_list_icon_battle_explore");
		newUIMap.put("MAIL_ICON_BATTLE_CAMP", "mail_list_icon_battle_camp");
		newUIMap.put("MAIL_ICON_BATTLE_CAPTURE", "mail_list_icon_battle_capture");
		newUIMap.put("MAIL_ICON_BATTLE_CITY_DEFENT_FAILURE", "mail_list_icon_battle_city_defent_failure");
		newUIMap.put("MAIL_ICON_BATTLE_CITY_DEFENT_VICTORY", "mail_list_icon_battle_city_defent_victory");
		newUIMap.put("MAIL_ICON_BATTLE_CITY_FAILURE", "mail_list_icon_battle_city_failure");
		newUIMap.put("MAIL_ICON_BATTLE_CITY_VICTORY", "mail_list_icon_battle_city_victory");
		newUIMap.put("MAIL_ICON_BATTLE_ALLIANCE_CENTER", "mail_list_icon_battle_alliance_center");
		newUIMap.put("MAIL_ICON_BATTLE_ALLIANCE_OUTPOST", "mail_list_icon_battle_alliance_outpost");
		newUIMap.put("MAIL_ICON_BATTLE_PRESSIDENT", "mail_list_icon_battle_pressident");
		newUIMap.put("MAIL_ICON_BATTLE_DETECT", "mail_list_icon_battle_detect");
		newUIMap.put("MAIL_ICON_BATTLE_KNIGHT", "mail_list_icon_battle_knight");
		newUIMap.put("MAIL_ICON_BATTLE_MONSTER", "mail_list_icon_battle_monster");
		newUIMap.put("MAIL_ICON_BATTLE_REPORT", "mail_list_icon_battle_report");
		newUIMap.put("MAIL_ICON_INVITE_JOIN_ALLIANCE", "mail_list_icon_invite_join_alliance");
		newUIMap.put("MAIL_ICON_INVITE_JOIN", "mail_list_icon_invite_join");
		newUIMap.put("MAIL_ICON_INVITE_KICKEDOUT", "mail_list_icon_invite_kickedout");
		newUIMap.put("MAIL_ICON_INVITE_MOVE", "mail_list_icon_invite_move");
		newUIMap.put("MAIL_ICON_INVITE_REJECTED", "mail_list_icon_invite_rejected");
		newUIMap.put("MAIL_ICON_STUDIO", "mail_list_icon_studio");
		newUIMap.put("MAIL_ICON_REWARD", "mail_list_icon_reward");
		newUIMap.put("MAIL_ICON_SEARCH", "mail_list_icon_search");
		newUIMap.put("MAIL_ICON_ID_BINGING", "mail_list_icon_id_binding");
		newUIMap.put("MAIL_ICON_SYSTEM", "mail_list_icon_system_other");
		newUIMap.put("MAIL_ICON_SYSTEM_VIP", "mail_list_icon_system_vip");
		newUIMap.put("MAIL_ICON_CHAT_ROOM", "mail_pic_flag_31");
		newUIMap.put("CHANNEL_ICON_BATTLEGAME", "mail_list_main_icon_battlegame");
		newUIMap.put("CHANNEL_ICON_ARENAGAME", "mail_list_main_icon_arenagame");
		newUIMap.put("CHANNEL_ICON_SHAMOGAME", "mail_list_main_icon_shamogame");

		newUIMap.put("CHANNEL_ICON_MANOR", "mail_list_main_icon_shamogame");

		newUIMap.put("MAIL_ICON_BATTLE_FUBAO_WIN", "mail_list_icon_battle_fubao_win");
		newUIMap.put("MAIL_ICON_BATTLE_FUBAO_FAIL", "mail_list_icon_battle_fubao_fail");

		newUIMap.put("CHANNEL_ICON_MONSTER_FAIL", "fb_monster_fu");
		newUIMap.put("CHANNEL_ICON_MONSTER_VICTOR", "fb_monster_sheng");

	}

}
