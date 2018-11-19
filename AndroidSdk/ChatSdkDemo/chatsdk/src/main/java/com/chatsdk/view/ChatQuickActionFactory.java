package com.chatsdk.view;

import android.app.Activity;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MsgItem;
import com.chatsdk.model.UserManager;

import com.quickaction3d.ActionItem;
import com.quickaction3d.QuickAction;

import org.apache.commons.lang.StringUtils;

public class ChatQuickActionFactory
{
	public static final int	ID_JOIN_ALLIANCE			= 1;
	public static final int	ID_COPY						= 2;
	public static final int	ID_SEND_MAIL				= 3;
	public static final int	ID_VIEW_PROFILE				= 4;
	public static final int	ID_BAN						= 5;
	public static final int	ID_UNBAN					= 8;
	public static final int	ID_TRANSLATE				= 6;
	public static final int	ID_ORIGINAL_LANGUAGE		= 7;
	public static final int	ID_VIEW_BATTLE_REPORT		= 9;
	public static final int	ID_INVITE					= 10;
	public static final int	ID_BLOCK					= 11;
	public static final int	ID_UNBLOCK					= 12;
	public static final int	ID_VIEW_DETECT_REPORT		= 13;
	public static final int	ID_REPORT_HEAD_IMG			= 14;
	public static final int	ID_VIEW_EQUIPMENT			= 15;
	public static final int	ID_REPORT_PLAYER_CHAT		= 16;
	public static final int	ID_TRANSLATE_NOT_UNDERSTAND	= 17;
	public static final int	ID_SAY_HELLO				= 18;
	public static final int	ID_VIEW_RALLY_INFO			= 19;
	public static final int	ID_VIEW_LOTTERY_SHARE		= 20;
	public static final int	ID_VIEW_ALLIANCETASK_SHARE	= 21;
	public static final int	ID_VIEW_RED_PACKAGE			= 22;
	public static final int	ID_VIEW_ALLIANCE_TREASURE	= 23;
	public static final int	ID_VIEW_SEVEN_DAY_SHARE		= 24;
	public static final int	ID_VIEW_MISSILE_REPORT		= 25;
	public static final int	ID_VIEW_ALLIANCE_GROUP_BUY	= 26;
	public static final int	ID_VIEW_GIFT_MAIL_SHARE 	= 27;
	public static final int	ID_LOGIC_FAVOUR_POINT_SHARE 	= 28; //收藏坐标分享
	public static final int	ID_VIEW_WOUNDED_SHARE 		= 29; //联盟庇护所伤兵分享
	public static final int	ID_VIEW_MEDAL_SHARE 		= 30; //联盟庇护所伤兵分享
	public static final int	ID_VIEW_SHAMO_INHESIONs_SHARE = 31; //沙漠天赋分享
	public static final int	ID_BAN_HEAD_IMG				 = 32; //GM封禁玩家头像
	public static final int	ID_VIEW_QUESTION_ACTIVITY	= 33; //查看答题活动界面
	public static final int	ID_VIEW_NWS_CENTER_SHARE	= 34; //查看要塞新闻中心
	public static final int	ID_VIEW_SCIENCE_MAX_SHARE	= 35; //查看科技中心

	public static QuickAction createQuickAction(final Activity activity, MsgItem item)
	{
		boolean canCopy = (!item.isSystemMessage());
		boolean canTranlate = (!item.isSystemMessage() || item.isHornMessage()|| item.isShareCommentMsg()) && !item.isSelfMsg();
		boolean canViewEquip = item.isEquipMessage();
		boolean hasTranslated = item.canShowTranslateMsg() || (item.isTranslatedByForce && !item.isOriginalLangByForce);
		boolean canJoinAlliance = item.isInAlliance() && !item.isSelfMsg() && !item.isTipMsg() &&!item.isUserADMsg()
				&& UserManager.getInstance().getCurrentUser().allianceId.equals("") && !ChatServiceController.isInMailDialog()
				&& !item.isSystemHornMsg();
		boolean canBlock = !item.isSystemMessage() && !item.isHornMessage() && !item.isSelfMsg() && !item.isTipMsg() && !item.isUserADMsg()
				&& !UserManager.getInstance().isInRestrictList(item.uid, UserManager.BLOCK_LIST);
		boolean canUnBlock = !item.isSystemMessage() && !item.isHornMessage() && !item.isSelfMsg() && !item.isTipMsg() && !item.isUserADMsg()
				&& UserManager.getInstance().isInRestrictList(item.uid, UserManager.BLOCK_LIST);
		boolean canBan = item.isNotInRestrictList() && !item.isSelfMsg() && !item.isTipMsg() && !item.isUserADMsg()
				&& (UserManager.getInstance().getCurrentUser().mGmod >= 1 && UserManager.getInstance().getCurrentUser().mGmod <= 9)
				&& !ChatServiceController.isInMailDialog()
				|| (!item.isSelfMsg() && ChatServiceController.isInLiveRoom() &&ChatServiceController.isAnchorHost && ChatServiceController.isInSelfLiveRoom()
				&& UserManager.getInstance().isNotInLiveBanUser(item.uid));//是主播切在直播间才能禁言
		boolean canUnBan = item.isInRestrictList() && !item.isSelfMsg() && !item.isTipMsg() && !item.isUserADMsg()
				&& (UserManager.getInstance().getCurrentUser().mGmod >= 1 && UserManager.getInstance().getCurrentUser().mGmod <= 9) && !ChatServiceController.isInMailDialog()
				|| (!item.isSelfMsg() && ChatServiceController.isInLiveRoom() &&ChatServiceController.isAnchorHost && ChatServiceController.isInSelfLiveRoom()
				&& !UserManager.getInstance().isNotInLiveBanUser(item.uid));//是主播切在直播间才能禁言
		boolean canViewBattleReport = !item.isHornMessage()
				&& item.isBattleReport() && !ChatServiceController.isInMailDialog();
		boolean canViewDetectReport = !item.isHornMessage()
				&& (item.isDetectReport() && !UserManager.getInstance().getCurrentUser().allianceId.equals(""))
				&& !ChatServiceController.isInMailDialog();
		boolean canInvite = !item.isHornMessage() && item.getASN().equals("")
				&& !UserManager.getInstance().getCurrentUser().allianceId.equals("")
				&& UserManager.getInstance().getCurrentUser().allianceRank >= 3 && !item.isSelfMsg() && !item.isTipMsg() && !item.isUserADMsg()
				&& !ChatServiceController.isInMailDialog();
		boolean canReportHeadImg = !item.isSystemMessage() && !item.isSelfMsg() && item.isCustomHeadImage();
		boolean canReportContent = !item.isSystemMessage() && !item.isSelfMsg();
		boolean canSayHello = item.isAllianceJoinMessage() && !item.isSelfMsg();
		boolean canViewRallyInfo = item.isRallyMessage();
		boolean canViewLotteryShare = item.isLotteryMessage();
		boolean canViewAllianceTaskShare = item.isAllianceTaskMessage() && item.msg.contains(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_1);
		boolean canViewAllianceTreasure = item.isAllianceTreasureMessage() && !item.isSelfMsg();
		// canTranlate && hasTranslated &&
											// !item.isSystemMessage();
		boolean canViewSevenDayShare = item.isSevenDayMessage() || item.isSevenDayNewShare();
		boolean canViewMissileReport = item.isMissleReport();
		boolean canViewAllianceGroupBuyShare = !UserManager.getInstance().getCurrentUser().allianceId.equals("")&&item.isAllianceGroupBuyMessage();
		boolean canVIewGiftMailShare = item.isGiftMailShare();
		boolean canLogicFavourPointShare = item.isFavourPointShare();
		boolean canVIewWoundedShare = item.isWoundedShare();

		boolean canVIewMedalShare = item.isEquipmentMedalShare();
		boolean canViewShamoInhesion = item.isShamoInhesionShare();
		boolean canViewQuestionActivity = item.isViewQuestionActivity();
		boolean canViewNewsCneterShare = item.isNewsCenterShare();
		boolean canViewScienceMaxShare = item.isScienceMaxShare();
		boolean canBanPlayerPic = !item.isSelfMsg() && UserManager.getInstance().getCurrentUser().mGmod == 1 && ChatServiceController.gm_closed_avatar && item.getHeadPicVer() > 0;
		boolean canSendMail = false;
		QuickAction quickAction = ChatQuickActionFactory.getQuickAction(activity, QuickAction.HORIZONTAL, canCopy,canJoinAlliance, canSendMail,
				canViewBattleReport, canViewDetectReport, canTranlate, hasTranslated, canBlock, canUnBlock, canBan, canUnBan, canInvite,
				canReportHeadImg, canViewEquip, canReportContent, canSayHello, canViewRallyInfo, canViewLotteryShare,canVIewGiftMailShare,canLogicFavourPointShare,canVIewWoundedShare,canVIewMedalShare,
				canViewAllianceTaskShare, canViewAllianceTreasure, canViewSevenDayShare, canViewMissileReport,canViewAllianceGroupBuyShare,canViewShamoInhesion,canViewQuestionActivity,canViewNewsCneterShare,canViewScienceMaxShare,canBanPlayerPic);

		return quickAction;
	}

	// TODO 参数过于重复
	private static QuickAction getQuickAction(final Activity activity, int orientation,boolean canCopy, boolean canJoinAlliance, boolean canSendMail,
			boolean canViewBattleReport, boolean canViewDetectReport, boolean canTranlate, boolean hasTranslated, boolean canBlock,
			boolean canUnBlock, boolean canBan, boolean canUnBan, boolean canInvite, boolean canReportHeadImg, boolean canViewEquip,
			boolean canReportContent, boolean canSayHello, boolean canViewRallyInfo, boolean canViewLotteryShare,boolean canVIewGiftMailShare,boolean canLogicFavourPointShare,boolean canVIewWoundedShare,boolean canVIewMedalShare,
			boolean canViewAllianceTaskShare, boolean canViewAllianceTreasure, boolean canViewSevenDayShare, boolean canViewMissileReport,
											  boolean canViewAllianceGroupBuyShare,boolean canViewShamoInhesion,boolean canViewQuestionActivity, boolean canViewNewsCneterShare, boolean canViewScienceMaxShare, boolean canBanPlayerPic)
	{
		QuickAction quickAction = actuallyCreateQuickAction(activity, orientation, canCopy, canJoinAlliance, canSendMail, canViewBattleReport,
				canViewDetectReport, canTranlate, hasTranslated, canBlock, canUnBlock, canBan, canUnBan, canInvite, canReportHeadImg,
				canViewEquip, canReportContent, canSayHello, canViewRallyInfo, canViewLotteryShare,canVIewGiftMailShare,canLogicFavourPointShare,canVIewWoundedShare,canVIewMedalShare,canViewAllianceTaskShare,
				canViewAllianceTreasure, canViewSevenDayShare, canViewMissileReport, canViewAllianceGroupBuyShare,canViewShamoInhesion,canViewQuestionActivity,canViewNewsCneterShare, canViewScienceMaxShare, canBanPlayerPic,0);

		if (orientation == QuickAction.HORIZONTAL && quickAction.isWiderThanScreen())
		{
			quickAction = ChatQuickActionFactory.actuallyCreateQuickAction(activity, QuickAction.VERTICAL, canCopy,canJoinAlliance, canSendMail,
					canViewBattleReport, canViewDetectReport, canTranlate, hasTranslated, canBlock, canUnBlock, canBan, canUnBan,
					canInvite, canReportHeadImg, canViewEquip, canReportContent, canSayHello, canViewRallyInfo, canViewLotteryShare,canVIewGiftMailShare,canLogicFavourPointShare,canVIewWoundedShare,canVIewMedalShare,
					canViewAllianceTaskShare, canViewAllianceTreasure, canViewSevenDayShare, canViewMissileReport,canViewAllianceGroupBuyShare,canViewShamoInhesion,canViewQuestionActivity,canViewNewsCneterShare,canViewScienceMaxShare,canBanPlayerPic, quickAction.getMaxItemWidth());
		}

		return quickAction;
	}

	private static QuickAction actuallyCreateQuickAction(final Activity activity, int orientation,boolean canCopy, boolean canJoinAlliance,
			boolean canSendMail, boolean canViewBattleReport, boolean canViewDetectReport, boolean canTranlate, boolean hasTranslated,
			boolean canBlock, boolean canUnBlock, boolean canBan, boolean canUnBan, boolean canInvite, boolean canReportHeadImg,
			boolean canViewEquip, boolean canReportContent, boolean canSayHello, boolean canViewRallyInfo, boolean canViewLotteryShare,boolean canVIewGiftMailShare,boolean canLogicFavourPointShare,boolean canVIewWoundedShare,boolean canVIewMedalShare,
			boolean canViewAllianceTaskShare, boolean canViewAllianceTreasure, boolean canViewSevenDayShare, boolean canViewMissileReport,
														 boolean canViewAllianceGroupBuyShare,boolean canViewShamoInhesionShare,boolean canViewQuestionActivity, boolean canViewNewsCneterShare, boolean canViewScienceMaxShare, boolean canBanPlayerPic, int maxItemWidth)
	{
		// create QuickAction. Use QuickAction.VERTICAL or
		// QuickAction.HORIZONTAL param to define layout orientation
		final QuickAction quickAction = new QuickAction(activity, orientation);

		if (orientation == QuickAction.VERTICAL && maxItemWidth > 0)
		{
			quickAction.maxItemWidth = maxItemWidth;
		}

		if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			quickAction.scaleRatio = ConfigManager.scaleRatio;
		}

		if (canViewEquip)
		{
			ActionItem nextItem = new ActionItem(ID_VIEW_EQUIPMENT, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW_EQUIPMENT));
			quickAction.addActionItem(nextItem);
		}

		if (canViewBattleReport)
		{
			ActionItem battleMsgItem = new ActionItem(ID_VIEW_BATTLE_REPORT, LanguageManager.getLangByKey(LanguageKeys.MENU_BATTLEREPORT));
			quickAction.addActionItem(battleMsgItem);
		}

		if (canViewDetectReport)
		{
			ActionItem detectMsgItem = new ActionItem(ID_VIEW_DETECT_REPORT, LanguageManager.getLangByKey(LanguageKeys.MENU_DETECTREPORT));
			quickAction.addActionItem(detectMsgItem);
		}

		if (canViewMissileReport)
		{
			ActionItem missileMsgItem = new ActionItem(ID_VIEW_MISSILE_REPORT, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(missileMsgItem);
		}

		if (canViewRallyInfo)
		{
			ActionItem rallyMsgItem = new ActionItem(ID_VIEW_RALLY_INFO, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW_RALLY_INFO));
			quickAction.addActionItem(rallyMsgItem);
		}

		if (canViewLotteryShare)
		{
			ActionItem lotteryShareMsgItem = new ActionItem(ID_VIEW_LOTTERY_SHARE, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(lotteryShareMsgItem);
		}
		if (canVIewGiftMailShare){
			ActionItem giftMailShareMsgItem = new ActionItem(ID_VIEW_GIFT_MAIL_SHARE, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(giftMailShareMsgItem);
		}
		if (canLogicFavourPointShare){
			ActionItem favourPointShareMsgItem = new ActionItem(ID_LOGIC_FAVOUR_POINT_SHARE, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(favourPointShareMsgItem);
		}
		if (canVIewWoundedShare){
			ActionItem woundedShareMsgItem = new ActionItem(ID_VIEW_WOUNDED_SHARE, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(woundedShareMsgItem);
		}

		if(canVIewMedalShare) {
			ActionItem medalShareMsgItem = new ActionItem(ID_VIEW_MEDAL_SHARE, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(medalShareMsgItem);
		}

		if(canViewShamoInhesionShare){
			ActionItem shamoInhesionShareMsgItem = new ActionItem(ID_VIEW_SHAMO_INHESIONs_SHARE, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(shamoInhesionShareMsgItem);
		}

		if(canViewQuestionActivity){
			ActionItem shamoInhesionShareMsgItem = new ActionItem(ID_VIEW_QUESTION_ACTIVITY, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(shamoInhesionShareMsgItem);
		}

		if (canViewAllianceTaskShare)
		{
			ActionItem allianceTaskShareMsgItem = new ActionItem(ID_VIEW_ALLIANCETASK_SHARE,
					LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW_TASK));
			quickAction.addActionItem(allianceTaskShareMsgItem);
		}

		if (canViewSevenDayShare) {
			ActionItem sevenDayShareMsgItem = new ActionItem(ID_VIEW_SEVEN_DAY_SHARE,
					LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(sevenDayShareMsgItem);
		}

		if (canViewAllianceGroupBuyShare) {
			ActionItem allianceGroupBuyMsgItem = new ActionItem(ID_VIEW_ALLIANCE_GROUP_BUY,
					LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(allianceGroupBuyMsgItem);
		}

		if (canTranlate)
		{
			if (hasTranslated)
			{
				ActionItem originalItem = new ActionItem(ID_ORIGINAL_LANGUAGE, LanguageManager.getLangByKey(LanguageKeys.MENU_ORIGINALLAN));
				quickAction.addActionItem(originalItem);
			}
			else
			{
				ActionItem translateItem = new ActionItem(ID_TRANSLATE, LanguageManager.getLangByKey(LanguageKeys.MENU_TRANSLATE));
				quickAction.addActionItem(translateItem);
			}
		}

		if (canViewAllianceTreasure)
		{
			ActionItem viewAllianceTreasure = new ActionItem(ID_VIEW_ALLIANCE_TREASURE,
					LanguageManager.getLangByKey(LanguageKeys.MENU_ALLIANCE_TREASURE));
			quickAction.addActionItem(viewAllianceTreasure);
		}
		if (canCopy)
		{
			ActionItem prevItem = new ActionItem(ID_COPY, LanguageManager.getLangByKey(LanguageKeys.MENU_COPY));
			quickAction.addActionItem(prevItem);
		}

		if (canSendMail)
		{
			ActionItem searchItem = new ActionItem(ID_SEND_MAIL, LanguageManager.getLangByKey(LanguageKeys.MENU_SENDMSG));
			quickAction.addActionItem(searchItem);
		}

		if (canSayHello)
		{
			String content = LanguageManager.getLangByKey(LanguageKeys.MENU_SAY_HELLO);
			if (StringUtils.isEmpty(content) || content.equals(LanguageKeys.MENU_SAY_HELLO))
				content = "Say Hello";
			ActionItem sayHelloItem = new ActionItem(ID_SAY_HELLO, content);
			quickAction.addActionItem(sayHelloItem);
		}

		if (canBlock)
		{
			ActionItem blockItem = new ActionItem(ID_BLOCK, LanguageManager.getLangByKey(LanguageKeys.MENU_SHIELD));
			quickAction.addActionItem(blockItem);
		}
		if (canUnBlock)
		{
			ActionItem unBlockItem = new ActionItem(ID_UNBLOCK, LanguageManager.getLangByKey(LanguageKeys.MENU_UNSHIELD));
			quickAction.addActionItem(unBlockItem);
		}

		if (canBan)
		{
			ActionItem banItem = new ActionItem(ID_BAN, LanguageManager.getLangByKey(LanguageKeys.MENU_BAN));
			quickAction.addActionItem(banItem);
		}
		if (canUnBan)
		{
			ActionItem unBanItem = new ActionItem(ID_UNBAN, LanguageManager.getLangByKey("105208"));
			quickAction.addActionItem(unBanItem);
		}

		if (canInvite)
		{
			ActionItem actionItem = new ActionItem(ID_INVITE, LanguageManager.getLangByKey(LanguageKeys.MENU_INVITE));
			quickAction.addActionItem(actionItem);
		}

		if (canJoinAlliance)
		{
			ActionItem nextItem = new ActionItem(ID_JOIN_ALLIANCE, LanguageManager.getLangByKey(LanguageKeys.MENU_JOIN));
			quickAction.addActionItem(nextItem);
		}
		if (canReportHeadImg)
		{
			ActionItem nextItem = new ActionItem(ID_REPORT_HEAD_IMG, LanguageManager.getLangByKey(LanguageKeys.MENU_REPORT_HEADIMG));
			quickAction.addActionItem(nextItem);
		}
		if (canReportContent)
		{
			ActionItem nextItem = new ActionItem(ID_REPORT_PLAYER_CHAT, LanguageManager.getLangByKey(LanguageKeys.MENU_REPORT_PLAYER_CHAT));
			quickAction.addActionItem(nextItem);
		}
		if(canBanPlayerPic){
			ActionItem nextItem = new ActionItem(ID_BAN_HEAD_IMG, LanguageManager.getLangByKey(LanguageKeys.MENU_BAN_PLAYER_PIC));
			quickAction.addActionItem(nextItem);
		}

		if(canViewNewsCneterShare){
			ActionItem nextItem = new ActionItem(ID_VIEW_NWS_CENTER_SHARE, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(nextItem);
		}

		if(canViewScienceMaxShare){
			ActionItem nextItem = new ActionItem(ID_VIEW_SCIENCE_MAX_SHARE, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(nextItem);
		}

		return quickAction;
	}

}
