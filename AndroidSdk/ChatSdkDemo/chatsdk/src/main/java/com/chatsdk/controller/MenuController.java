package com.chatsdk.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chatsdk.R;
import com.chatsdk.model.ChannelListItem;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.MsgItem;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.db.ChatTable;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.db.DBManager;
import com.chatsdk.net.WebSocketManager;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.PermissionManager;
import com.chatsdk.util.ScaleUtil;
import com.chatsdk.view.ChatQuickActionFactory;
import com.chatsdk.view.MemberSelectorFragment;
import com.chatsdk.view.MessagesAdapter;
import com.chatsdk.view.actionbar.MyActionBarActivity;

import com.quickaction3d.QuickAction;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MenuController
{
	public static void handleItemClick(final MessagesAdapter adapter, QuickAction source, int pos, int actionId)
	{
		final MsgItem item = MessagesAdapter.getMsgItemFromQuickAction(source);
		if(item == null)
			return;
		String attachment = "";
		String[] attachmentIds = item.attachmentId.split("__");
		if(attachmentIds.length > 1){
			attachment = attachmentIds[0];
		}else {
			attachment = item.attachmentId;
		}
		switch (actionId)
		{
			case ChatQuickActionFactory.ID_INVITE:
				ChatServiceController.doHostAction("inviteJoinAlliance", item.uid, item.getName(), "", true);
				break;
			case ChatQuickActionFactory.ID_JOIN_ALLIANCE:
				ChatServiceController.doHostAction("joinAlliance", item.uid, item.getName(), "", true);
				break;
			case ChatQuickActionFactory.ID_COPY:
				adapter.copyToClipboard(source);
				break;
			case ChatQuickActionFactory.ID_SEND_MAIL:
					ChatServiceController.isCreateChatRoom = false;
					ServiceInterface.setMailInfo(item.uid, "", item.getName(), MailManager.MAIL_USER);
					ServiceInterface.showChatActivity(ChatServiceController.getCurrentActivity(), DBDefinition.CHANNEL_TYPE_USER, false);
				break;
			case ChatQuickActionFactory.ID_VIEW_PROFILE:
				if (ChatServiceController.isContactMod)
					ChatServiceController.doHostAction("showPlayerInfo@mod", item.uid, item.getName(), "", true);
				else
					ChatServiceController.doHostAction("showPlayerInfo", item.uid, item.getName(), "", true);
				break;
			case ChatQuickActionFactory.ID_BLOCK:
				showConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_SHIELD_PLAYER, item.getName()), item, UserManager.BLOCK_LIST);
				break;
			case ChatQuickActionFactory.ID_UNBLOCK:
				JniController.getInstance().excuteJNIVoidMethod("unShieldPlayer", new Object[] { item.uid, item.getName() });
				UserManager.getInstance().removeRestrictUser(item.uid, UserManager.BLOCK_LIST);
				break;
			case ChatQuickActionFactory.ID_BAN:
				showBanConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_BAN, item.getName()), item);
				break;
			case ChatQuickActionFactory.ID_UNBAN:
				if(item.isHornMessage())
				{
					JniController.getInstance().excuteJNIVoidMethod("unBanPlayerNotice", new Object[] { item.uid });
					UserManager.getInstance().removeRestrictUser(item.uid, UserManager.BAN_NOTICE_LIST);
				}
				else
				{
					JniController.getInstance().excuteJNIVoidMethod("unBanPlayer", new Object[] { item.uid });
					UserManager.getInstance().removeRestrictUser(item.uid, UserManager.BAN_LIST);
				}
				break;
			case ChatQuickActionFactory.ID_TRANSLATE:
//				final MessageHolder holder = (MessageHolder) source.currentTextView.getTag();
				adapter.showTranslatedLanguage((TextView)source.currentTextView, item);
				break;
			case ChatQuickActionFactory.ID_ORIGINAL_LANGUAGE:
//				final MessageHolder holder2 = (MessageHolder) source.currentTextView.getTag();
				adapter.showOriginalLanguage((TextView)source.currentTextView, item);
				break;
			case ChatQuickActionFactory.ID_VIEW_BATTLE_REPORT:
				ChatServiceController.doHostAction("viewBattleReport", item.uid, "", attachment, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_FORMATION_BATTLE_REPORT:
				ChatServiceController.doHostAction("viewFormationBattleReport", item.uid, "", item.attachmentId, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_DETECT_REPORT:
				ChatServiceController.doHostAction("viewDetectReport", item.uid, "", attachment, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_FB_SCOUT_REPORT:
				ChatServiceController.doHostAction("viewScoutReport", item.uid, "", item.attachmentId, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_MISSILE_REPORT:
				ChatServiceController.doHostAction("viewMissileReport", item.uid, "", attachment, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_EQUIPMENT:
				ChatServiceController.doHostAction("showEquipment", "", "", attachment, true);
				break;
			case ChatQuickActionFactory.ID_REPORT_PLAYER_CHAT:
				if (ChatServiceController.oldReportContentTime == 0
						|| System.currentTimeMillis() - ChatServiceController.oldReportContentTime >= ChatServiceController.REPORT_CONTENT_TIME_INTERVAL)
				{
					if (UserManager.getInstance().isInReportContentList(item, UserManager.REPORT_CONTETN_LIST))
					{
						showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CONTETN_REPORTED));
					}
					else
					{
						showConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_CONTENT), item, UserManager.REPORT_CONTETN_LIST);
					}
				}
				else
				{
					showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_CONTENT_INTERVAL));
				}
				break;
			case ChatQuickActionFactory.ID_REPORT_HEAD_IMG:
				if (UserManager.getInstance().isInRestrictList(item.uid, UserManager.REPORT_LIST))
				{
					showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_HEADIMG_REPORTED));
				}
				else
				{
					showConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_HEADIMG, item.getName()), item,
							UserManager.REPORT_LIST);
				}
				break;
			case ChatQuickActionFactory.ID_TRANSLATE_NOT_UNDERSTAND:
				if (UserManager.getInstance().isInReportContentList(item, UserManager.REPORT_TRANSLATION_LIST))
				{
					showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_TRANSLATION_REPORTED));
				}
				else
				{
					showConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_TRASNALTION), item,
							UserManager.REPORT_TRANSLATION_LIST);
				}
				break;
			case ChatQuickActionFactory.ID_SAY_HELLO:
				sayHello(item);
				break;
			case ChatQuickActionFactory.ID_VIEW_RALLY_INFO:
				ChatServiceController.doHostAction("viewRallyInfo", "", "", attachment, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_LOTTERY_SHARE:
				ChatServiceController.doHostAction("viewLotteryShare", "", "", attachment, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_GIFT_MAIL_SHARE:
				ChatServiceController.doHostAction("viewGiftShareMail", item.uid, item.getName(), attachment, true);
				break;
			case ChatQuickActionFactory.ID_LOGIC_FAVOUR_POINT_SHARE:
				ChatServiceController.doHostAction("logicFavourPointShare", "", "", item.attachmentId, false);
				break;
			case ChatQuickActionFactory.ID_VIEW_WOUNDED_SHARE:
				ChatServiceController.doHostAction("viewGotoWoundedShare", "", "", "", true);
				break;
			case ChatQuickActionFactory.ID_VIEW_MEDAL_SHARE:
				ChatServiceController.doHostAction("viewEquipmentmedalShareLogic", "", item.getName(), attachment, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_SEVEN_DAY_SHARE:
				ChatServiceController.doHostAction("viewSevenDayShare", "", "", attachment, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_ALLIANCETASK_SHARE:
				ChatServiceController.doHostAction("viewAllianceTaskShare", "", "", "", true);
				break;
			case ChatQuickActionFactory.ID_VIEW_ALLIANCE_GROUP_BUY:
				ChatServiceController.doHostAction("viewAllianceGroupBuyShare", "", "", attachment, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_RED_PACKAGE:
				String[] redPackageInfoArr = item.attachmentId.split("\\|");
				ChatServiceController.doHostAction("viewRedPackage", "", item.msg, redPackageInfoArr[0], true);
				break;
			case ChatQuickActionFactory.ID_VIEW_ALLIANCE_TREASURE:
				ChatServiceController.doHostAction("viewAllianceTreasure", "", "", item.getAllianceTreasureInfo(2), true);
				break;
			case ChatQuickActionFactory.ID_VIEW_SHAMO_INHESIONs_SHARE:
				ChatServiceController.doHostAction("viewShamoInhesionShare", "",item.getName(), attachment, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_FB_ACTIVITYHERO:
				ChatServiceController.doHostAction("viewActivityHeroShareLogic", "", "", "", true);
				break;
			case ChatQuickActionFactory.ID_VIEW_FB_FORMATIONSHARE:
				ChatServiceController.doHostAction("viewFBFormationShare", "", "", attachment, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_FB_ALLIANCE_ATTACT:
				LogUtil.trackMessage("ViewOpenAllianceWarUI touch");
				ChatServiceController.doHostAction("ViewOpenAllianceWarUI", "", "", attachment, true);
				break;

				//在这加
			default:
				break;
		}
	}

	private static void sayHello(MsgItem msgItem)
	{
		String[] dialogs = { LanguageKeys.SAY_HELLO1, LanguageKeys.SAY_HELLO2, LanguageKeys.SAY_HELLO3, LanguageKeys.SAY_HELLO4 };
		String helloText = "Welcome!";
		if (dialogs.length > 0)
		{
			long randomNum = Math.round(Math.random() * dialogs.length);
			int index = (int) randomNum;
			if (index >= 0 && index < dialogs.length)
			{
				if (StringUtils.isNotEmpty(dialogs[index]) && StringUtils.isNumeric(dialogs[index]))
				{
					String key = LanguageManager.getLangByKey(dialogs[index]);
					if (key.contains("{0}"))
					{
						if (key.contains("{1}"))
						{
							helloText = LanguageManager.getLangByKey(dialogs[index], msgItem.getASN(), msgItem.getName());
						}
						else
						{
							helloText = LanguageManager.getLangByKey(dialogs[index], msgItem.getASN());
						}
					}
					else
					{
						helloText = key;
					}
				}
			}
			else
			{
				helloText = LanguageManager.getLangByKey(LanguageKeys.SAY_HELLO1);
			}

		}
		else
		{
			helloText = LanguageManager.getLangByKey(LanguageKeys.SAY_HELLO1);
		}
		if (StringUtils.isEmpty(helloText))
			helloText = "Welcome!";
		final String text = helloText;
		ChatServiceController.hostActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (ChatServiceController.getChatFragment() != null)
					{
						ChatServiceController.getChatFragment().setEditText(text);
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	private static Window initAlertDialog(AlertDialog dlg, int id)
	{
		if(ChatServiceController.getInstance().host.getNativeGetIsShowStatusBar()) {
			dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		}
		dlg.show();
		Window window = dlg.getWindow();
		if(ChatServiceController.getInstance().host.getNativeGetIsShowStatusBar()) {
			window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		}
		window.setContentView(id);
		window.setBackgroundDrawable(new ColorDrawable());
		window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		if(ChatServiceController.getInstance().host.getNativeGetIsShowStatusBar()) {
			if (Build.VERSION.SDK_INT >= 19) {
				window.getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE
								| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
								| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_FULLSCREEN
								| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

				dlg.getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE
								| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
								| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_FULLSCREEN
								| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			}

			dlg.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			dlg.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		return window;
	}

	private static void setDismissListener(View frame, final Dialog dlg)
	{
		frame.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				dlg.dismiss();
				return true;
			}
		});
	}

	private static void setDismissListener(View frame, final Dialog dlg, final OnClickListener onOKClickListener)
	{
		frame.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				onOKClickListener.onClick(v);
				dlg.dismiss();
				return true;
			}
		});
	}

	private static void showBanConfirm(String content, final MsgItem item)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;

		Window window = initAlertDialog(dlg, R.layout.cs__ban_confirm_dialog);
		setDismissListener((FrameLayout) window.findViewById(R.id.banConfirmFrameLayout), dlg);

		TextView alertTextView = (TextView) window.findViewById(R.id.textView1);
		alertTextView.setText(content);

		final ArrayList<CheckBox> checkBoxs = new ArrayList<CheckBox>();
		int[] checkBoxIds = { R.id.checkBox1, R.id.checkBox2, R.id.checkBox3, R.id.checkBox4 };
		String[] timeValues = { "24", "72", "168", "-1" };
		String[] banTimeArr = ChatServiceController.banTime.split("\\|");
		if (banTimeArr.length == 4)
		{
			timeValues[0] = banTimeArr[0];
			timeValues[1] = banTimeArr[1];
			timeValues[2] = banTimeArr[2];
			timeValues[3] = banTimeArr[3];
		}
		String timeStr = LanguageManager.getLangByKey(LanguageKeys.TIP_TIME);
		String foreverStr = LanguageManager.getLangByKey(LanguageKeys.TIP_FOREVER);

		OnClickListener checkOnClickListener = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				for (int i = 0; i < checkBoxs.size(); i++)
				{
					checkBoxs.get(i).setChecked((i + 1) == ((Integer) (v.getTag())).intValue());
				}
			}
		};

		for (int i = 0; i < checkBoxIds.length; i++)
		{
			CheckBox checkBox = (CheckBox) window.findViewById(checkBoxIds[i]);
			if(i == checkBoxIds.length-1){
				checkBox.setText(foreverStr);
			}
			else {
				checkBox.setText(" " + timeValues[i] + timeStr);
			}
			checkBox.setTag(Integer.valueOf(i + 1));
			checkBox.setOnClickListener(checkOnClickListener);
			checkBoxs.add(checkBox);
		}

		// 为确认按钮添加事件,执行退出应用操作
		Button ok = (Button) window.findViewById(R.id.okBanBtn);
		ok.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CONFIRM));
		ok.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();

				int selectIndex = 0;
				for (int i = 0; i < checkBoxs.size(); i++)
				{
					if (checkBoxs.get(i).isChecked())
					{
						selectIndex = i;
						break;
					}
				}

				if (item.isHornMessage())
				{
					JniController.getInstance().excuteJNIVoidMethod("banPlayerNoticeByIndex",
							new Object[] { item.uid, Integer.valueOf(selectIndex) });
					UserManager.getInstance().addRestrictUser(item.uid, UserManager.BAN_NOTICE_LIST);
				}
				else
				{
					JniController.getInstance().excuteJNIVoidMethod("banPlayerByIndex",
							new Object[] { item.uid, Integer.valueOf(selectIndex) });
					UserManager.getInstance().addRestrictUser(item.uid, UserManager.BAN_LIST);
				}

			}
		});
		// 关闭alert对话框架
		Button cancel = (Button) window.findViewById(R.id.cancelBanBtn);
		cancel.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CANCEL));
		cancel.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
			}
		});

		adjustBanDialog(alertTextView, ok, cancel, checkBoxs);
	}

	private static void adjustBanDialog(TextView alertTextView, Button ok, Button cancel, final ArrayList<CheckBox> checkBoxs)
	{
		if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			ScaleUtil.adjustTextSize(alertTextView, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(ok, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(cancel, ConfigManager.scaleRatio);
			for (int i = 0; i < checkBoxs.size(); i++)
			{
				ScaleUtil.adjustTextSize(checkBoxs.get(i), ConfigManager.scaleRatio);
			}
		}
	}

	private static void showConfirm(String content, final MsgItem item, final int type)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				if(v  instanceof Button) {
					if (type == UserManager.BLOCK_LIST) {
						JniController.getInstance().excuteJNIVoidMethod("shieldPlayer", new Object[]{item.uid});
						UserManager.getInstance().addRestrictUser(item.uid, type);
					} else if (type == UserManager.REPORT_LIST) {
						JniController.getInstance().excuteJNIVoidMethod("reportCustomHeadImg", new Object[]{item.uid});

						MyActionBarActivity activity = ChatServiceController.getCurrentActivity();
						if (activity != null) {
							ServiceInterface.safeGravityMakeText(activity,LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_HEADIMG_SUCCESS), Toast.LENGTH_SHORT,Gravity.TOP, 0, activity.getToastPosY());
						}

						UserManager.getInstance().addRestrictUser(item.uid, type);
					} else if (type == UserManager.REPORT_CONTETN_LIST) {
						ChatServiceController.oldReportContentTime = System.currentTimeMillis();
						JniController.getInstance().excuteJNIVoidMethod("reportPlayerChatContent", new Object[]{item.uid, item.msg});

						MyActionBarActivity activity = ChatServiceController.getCurrentActivity();
						if (activity != null) {
							ServiceInterface.safeGravityMakeText(activity,LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_CONTENT_SUCCESS), Toast.LENGTH_SHORT,Gravity.TOP, 0, activity.getToastPosY());
						}

						UserManager.getInstance().addReportContent(item, type);
					} else if (type == UserManager.REPORT_TRANSLATION_LIST) {

						JniController.getInstance().excuteJNIVoidMethod("translateOptimize",
								new Object[]{"notunderstand", item.originalLang, item.translatedLang, item.msg, item.translateMsg});

						MyActionBarActivity activity = ChatServiceController.getCurrentActivity();
						if (activity != null) {
							ServiceInterface.safeGravityMakeText(activity,LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_TRANSLATION_SUCCESS), Toast.LENGTH_SHORT,Gravity.TOP, 0, activity.getToastPosY());
						}
						UserManager.getInstance().addReportContent(item, type);
					}
				}

			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	private static void adjustConfirmDialog(TextView alertTextView, Button ok, Button cancel)
	{
		if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			ScaleUtil.adjustTextSize(alertTextView, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(ok, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(cancel, ConfigManager.scaleRatio);
		}
	}

	private static void adjustConfirmCoinDialog(TextView alertTextView, TextView okTextView, TextView coinTextView, Button cancel)
	{
		if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			ScaleUtil.adjustTextSize(alertTextView, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(okTextView, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(coinTextView, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(cancel, ConfigManager.scaleRatio);
		}
	}

	public static void showReSendConfirm(String content, final MsgItem msgItem)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				if (msgItem.isHornMessage())
				{
					int hornBanedTime = JniController.getInstance().excuteJNIMethod("getHornBanedTime", null);
					if (hornBanedTime == 0)
					{
						int price = JniController.getInstance().excuteJNIMethod("isHornEnough", null);
						String horn = LanguageManager.getLangByKey(LanguageKeys.TIP_HORN);
						if (price == 0)
						{
							showResendHornMessageConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_USEITEM, horn), msgItem);
						}
						else if (price > 0)
						{
							showResendHornWithCornConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_ITEM_NOT_ENOUGH, horn), msgItem,
									price);
						}
					}
					else
					{
						Activity activity = ChatServiceController.getCurrentActivity();
						if (activity != null) {
							ServiceInterface.safeMakeText(activity, "you have been baned!", Toast.LENGTH_SHORT);
						}
					}
				}
				else
				{
					ChatServiceController.getInstance().resendMsg(msgItem, false, false);
				}

			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showResendHornMessageConfirm(String content, final MsgItem msgItem)
	{
		final int price = JniController.getInstance().excuteJNIMethod("isHornEnough", null);
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				ChatServiceController.resendMsg(msgItem, true, false);
			}
		};
		setDialogView(dlg, content, okOnlickListener, price, true);
	}

	public static void showResendHornWithCornConfirm(String content, final MsgItem msgItem, final int price)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				boolean isCornEnough = JniController.getInstance().excuteJNIMethod("isCornEnough", new Object[] { Integer.valueOf(price) });
//				if (isCornEnough)
//				{
//					ChatServiceController.resendMsg(msgItem, true, true);
//				}
//				else
//				{
					showCornNotEnoughConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CORN_NOT_ENOUGH));
//				}
			}
		};

//		setDialogViewWithCoin(dlg, content, okOnlickListener, price, true);
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showSendHornMessageConfirm(String content, final String message)
	{
		final int price = JniController.getInstance().excuteJNIMethod("isHornEnough", null);
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				ChatServiceController.sendMsg(message, true, false, null);
			}
		};
		setDialogView(dlg, content, okOnlickListener, price, true);
	}

	public static void showSendHornWithCornConfirm(String content, final String message, final int price)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
//				boolean isCornEnough = JniController.getInstance().excuteJNIMethod("isCornEnough", new Object[] { Integer.valueOf(price) });
//				if (isCornEnough)
//				{
//					ChatServiceController.sendMsg(message, true, true, null);
//				}
//				else
//				{
//					showCornNotEnoughConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CORN_NOT_ENOUGH));
//				}
			}
		};
//		setDialogViewWithCoin(dlg, content, okOnlickListener, price, true);
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showCornNotEnoughConfirm(String content)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, false);
	}

	public static void showChatRestrictConfirm(String content)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				ChatServiceController.doHostAction("changeNickName", "", "", "", false);
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, false).setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CHANGE_NAME));
	}

	public static void showCreateChatRoomConfirm(final MyActionBarActivity activity, String content, final ArrayList<String> memberUidAdd)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				JniController.getInstance().excuteJNIVoidMethod(
						"createChatRoom",
						new Object[] {
								UserManager.getInstance().createNameStr(memberUidAdd),
								UserManager.getInstance().createUidStr(memberUidAdd),
								MemberSelectorFragment.roomName,
								"" });
				activity.exitActivity();
				ChatServiceController.isShowProgressBar = true;
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showChatRoomManagerConfirm(final MyActionBarActivity activity, String content, final ArrayList<String> memberUidAdd,
			final ArrayList<String> memberUidRemoved, final String roomName)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();

				boolean isShow = false;
				if (ChatServiceController.isCreateChatRoom)
				{
                    ChatServiceController.isCreateChatRoom = true;
                    Intent intent = new Intent();
                    intent.putExtra("roomName", MemberSelectorFragment.roomName);
                    intent.putExtra("uidStr", UserManager.getInstance().createUidStr(memberUidAdd));
                    intent.putExtra("nameStr", UserManager.getInstance().createNameStr(memberUidAdd));
                    activity.setResult(Activity.RESULT_OK, intent);
                    activity.exitActivity();
					isShow = true;
				}
				else
				{
					if (memberUidAdd != null && memberUidAdd.size() > 0)
					{
						isShow = true;
						String uidStr = UserManager.getInstance().createUidStr(memberUidAdd);
						if(ChatServiceController.getInstance().standalone_chat_room){
							WebSocketManager.getInstance().chatRoomInvite(UserManager.getInstance().getCurrentMail().opponentUid, uidStr);
						}else {
							JniController.getInstance().excuteJNIVoidMethod(
									"inviteChatRoomMember",
									new Object[]{
											UserManager.getInstance().getCurrentMail().opponentUid,
											UserManager.getInstance().createNameStr(memberUidAdd),
											uidStr});
						}
					}
					if (memberUidRemoved != null && memberUidRemoved.size() > 0)
					{
						String uidStr = UserManager.getInstance().createUidStr(memberUidRemoved);
						if(ChatServiceController.getInstance().standalone_chat_room){
							WebSocketManager.getInstance().chatRoomKick(UserManager.getInstance().getCurrentMail().opponentUid, uidStr);
						}else {
							JniController.getInstance().excuteJNIVoidMethod(
									"kickChatRoomMember",
									new Object[]{
											UserManager.getInstance().getCurrentMail().opponentUid,
											UserManager.getInstance().createNameStr(memberUidRemoved),
											uidStr});
						}
					}
					if (roomName != null && !roomName.equals(""))
					{
						String uidStr = UserManager.getInstance().createUidStr(memberUidAdd);
						if(ChatServiceController.getInstance().standalone_chat_room){
							WebSocketManager.getInstance().chatRoomChangeName(UserManager.getInstance().getCurrentMail().opponentUid, roomName);
						}else {
							JniController.getInstance().excuteJNIVoidMethod("modifyChatRoomName",
									new Object[]{UserManager.getInstance().getCurrentMail().opponentUid, roomName});
						}
					}

					activity.exitActivity();
					if(isShow)
						ChatServiceController.isShowProgressBar = true;
				}
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showInviteChatRoomMemberConfirm(final MyActionBarActivity activity, String content,
			final ArrayList<String> memberUidAdd)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();

				String nameStr= UserManager.getInstance().createNameStr(memberUidAdd);
				String uidStr = UserManager.getInstance().createUidStr(memberUidAdd);
				if (ChatServiceController.isCreateChatRoom)
				{
                    // 创建聊天室
                    LogUtil.trackPageView("CreateChatRoom");
                    JniController.getInstance().excuteJNIVoidMethod("createChatRoom",
                            new Object[] { nameStr, uidStr, MemberSelectorFragment.roomName, "" });
                    Intent intent = new Intent();
                    intent.putExtra("roomName", MemberSelectorFragment.roomName);
                    intent.putExtra("uidStr", uidStr);
                    intent.putExtra("nameStr", nameStr);
                    activity.setResult(Activity.RESULT_OK, intent);
                    activity.exitActivity();
                    ChatServiceController.isCreateChatRoom = false;
					ChatServiceController.isShowProgressBar = true;
				}
				else
				{
					if(ChatServiceController.getInstance().standalone_chat_room){
						WebSocketManager.getInstance().chatRoomInvite(UserManager.getInstance().getCurrentMail().opponentUid, uidStr);
					}else{
						JniController.getInstance().excuteJNIVoidMethod(
								"inviteChatRoomMember",
								new Object[] {
										UserManager.getInstance().getCurrentMail().opponentUid,
										nameStr,
										uidStr });
					}

					activity.exitActivity();
					ChatServiceController.isShowProgressBar = true;
				}
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void quitChatRoomConfirm(final MyActionBarActivity activity, String content)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();

				ChatChannel channel = ChannelManager.getInstance().getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, UserManager.getInstance().getCurrentMail().opponentUid));
				if (channel == null) {
					return;
				}

				if(ChatServiceController.getInstance().standalone_chat_room){
					if(channel.roomOwner!= null && UserManager.getInstance().getCurrentUser() != null
							&& !channel.roomOwner.equals(UserManager.getInstance().getCurrentUser().uid)){
						WebSocketManager.getInstance().chatRoomQuit(UserManager.getInstance().getCurrentMail().opponentUid);
					}else{
						JniController.getInstance().excuteJNIVoidMethod("quitChatRoom",
								new Object[] { UserManager.getInstance().getCurrentMail().opponentUid });
					}
				}else{
					// 群主退出聊天室，则聊天室解散
					if (channel.memberUidArray != null && channel.memberUidArray.size() > 0) {
						ArrayList<String> memberUidRemoved = channel.memberUidArray;
						memberUidRemoved.remove(UserManager.getInstance().getCurrentUser().uid);
						if (channel.roomOwner.equals(UserManager.getInstance().getCurrentUser().uid) && memberUidRemoved.size() > 0) {
							JniController.getInstance().excuteJNIVoidMethod(
									"kickChatRoomMember",
									new Object[]{
											UserManager.getInstance().getCurrentMail().opponentUid,
											UserManager.getInstance().createNameStr(memberUidRemoved),
											UserManager.getInstance().createUidStr(memberUidRemoved)});
						}
					}
					JniController.getInstance().excuteJNIVoidMethod("quitChatRoom",
							new Object[] { UserManager.getInstance().getCurrentMail().opponentUid });
				}

				//activity.exitActivity();
				ServiceInterface.popTopActivity();
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showNoSearchedUserConfirm(String content)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, false);
	}

	private static AlertDialog createAlertDialog()
	{
		if (ChatServiceController.getCurrentActivity() == null)
			return null;
		return createAlertDialog(ChatServiceController.getCurrentActivity());
	}

	private static AlertDialog createAlertDialog(Context context)
	{
		try
		{
			return new AlertDialog.Builder(context).create();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
			return null;
		}
	}

	private static Button setDialogView(final AlertDialog dlg, String content, OnClickListener onOKClickListener, int corn,
			boolean showCancelBtn)
	{
		return setDialogView(dlg, content, onOKClickListener, corn, showCancelBtn, false);
	}

	private static Button setDialogView(final AlertDialog dlg, String content, OnClickListener onOKClickListener, int corn,
			boolean showCancelBtn, boolean takeDismissAsOK)
	{
		try
		{
			Window window = initAlertDialog(dlg, R.layout.cs__confirm_dialog);
			setDismissListener((FrameLayout) window.findViewById(R.id.confirmFrameLayout), dlg, onOKClickListener);

			TextView alertTextView = (TextView) window.findViewById(R.id.alertTextView);
			Button ok = (Button) window.findViewById(R.id.exitBtn0);
			Button cancel = (Button) window.findViewById(R.id.exitBtn1);
			cancel.setVisibility(showCancelBtn ? View.VISIBLE : View.GONE);

			adjustConfirmDialog(alertTextView, ok, cancel);

			alertTextView.setText(content);

			ok.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CONFIRM));

			ok.setOnClickListener(onOKClickListener);

			cancel.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CANCEL));
			cancel.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					dlg.cancel();
				}
			});
			return ok;
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		return null;
	}

	private static void setDialogViewWithCoin(final AlertDialog dlg, String content, OnClickListener onOKClickListener, int coin,
			boolean cancelBtnShow)
	{
		Window window = initAlertDialog(dlg, R.layout.cs__gold_confirm_dialog);
		setDismissListener((FrameLayout) window.findViewById(R.id.goldConfirmFrameLayout), dlg);

		TextView alertTextView = (TextView) window.findViewById(R.id.alertTextView);
		LinearLayout confirm_layout = (LinearLayout) window.findViewById(R.id.confirm_layout);
		TextView coin_count_text = (TextView) window.findViewById(R.id.confirm_coin_count);
		TextView ok_btn_text = (TextView) window.findViewById(R.id.ok_btn_text);
		Button cancel = (Button) window.findViewById(R.id.exitBtn1);
		cancel.setVisibility(cancelBtnShow ? View.VISIBLE : View.GONE);

		adjustConfirmCoinDialog(alertTextView, ok_btn_text, coin_count_text, cancel);

		alertTextView.setText(content);
		coin_count_text.setText("" + coin);

		ok_btn_text.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CONFIRM));

		confirm_layout.setOnClickListener(onOKClickListener);

		cancel.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CANCEL));
		cancel.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
			}
		});
	}

	public static void showContentConfirm(String content)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
			}
		};

		try
		{
			Window window = initAlertDialog(dlg, R.layout.cs__confirm_dialog);

			setDismissListener((FrameLayout) window.findViewById(R.id.confirmFrameLayout), dlg);

			TextView alertTextView = (TextView) window.findViewById(R.id.alertTextView);
			Button ok = (Button) window.findViewById(R.id.exitBtn0);
			Button cancel = (Button) window.findViewById(R.id.exitBtn1);
			cancel.setVisibility(View.GONE);
			adjustConfirmDialog(alertTextView, ok, cancel);
			alertTextView.setText(content);
			ok.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CONFIRM));
			ok.setOnClickListener(okOnlickListener);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public static void showDeleteChannelConfirm(String content, final ChatChannel channel)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(ChatServiceController.getChannelListFragment() != null) {
					ChatServiceController.getChannelListFragment().actualDeleteSingleChannel(channel);
					dlg.cancel();
				}
			}
		};

		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showOperateMutiMail(String content, final List<ChannelListItem> list, final int type)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;

		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(ChatServiceController.getChannelListFragment() != null){
					ChatServiceController.getChannelListFragment().comfirmOperateMutiMail(list, type);
					dlg.cancel();
				}
			}
		};

		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showAllowPermissionConfirm(final Activity activity, final String content, final String permissionKey)
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final AlertDialog dlg = createAlertDialog(activity);
					if (dlg == null)
						return;
					OnClickListener okOnlickListener = new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							PermissionManager.onNotifyPermissionConfirm(permissionKey);
							dlg.cancel();
						}
					};
					setDialogView(dlg, content, okOnlickListener, 0, false, true);
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public static void topChatRoomConfirm(final MyActionBarActivity activity, String content, final ChatChannel curChannel)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				if (curChannel != null) {

					List<ChatChannel> channelList = ChannelManager.getInstance().getAllChatRoomChannel();
					Iterator<ChatChannel> it = channelList.iterator();
					while(it.hasNext()) {
						ChatChannel channel = it.next();
						if (channel.settings!=null && (channel.settings.equals("1") || channel.settings.equals("2")) ) {
							channel.settings = "0";
							if(channel.channelID.equals(ChatServiceController.topChatRoomUid)) {
								ChatServiceController.topChatRoomUid = "";
							}
							DBManager.getInstance().updateChannel(channel);
						}
					}

					curChannel.settings = "1";
					ChatServiceController.topChatRoomUid = curChannel.channelID;
					JniController.getInstance().excuteJNIVoidMethod("getLatestChatMessage", null);
					DBManager.getInstance().updateChannel(curChannel);

					if (ChatServiceController.getChatRoomSettingActivity() != null) {
						ChatServiceController.getChatRoomSettingActivity().refreshData();
					}
					else if (ChatServiceController.getChatFragment() != null) {
						ChatServiceController.getChatFragment().reload();
					}
				}
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}
}
