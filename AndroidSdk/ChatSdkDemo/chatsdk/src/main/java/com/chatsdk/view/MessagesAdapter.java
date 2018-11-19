package com.chatsdk.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.controller.MenuController;
import com.chatsdk.controller.ServiceInterface;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MsgItem;
import com.chatsdk.model.StickManager;
import com.chatsdk.model.TranslateManager;
import com.chatsdk.model.UserInfo;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.viewholder.MessageViewHolder;
import com.chatsdk.model.viewholder.ViewHolderHelper;
import com.chatsdk.net.WebSocketManager;
import com.chatsdk.util.AllianceTaskInfo;
import com.chatsdk.util.CompatibleApiUtil;
import com.chatsdk.util.FilterWordsManager;
import com.chatsdk.util.ImageUtil;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.ResUtil;
import com.chatsdk.util.ScaleUtil;
import com.chatsdk.util.TranslateListener;
import com.chatsdk.util.gif.GifMovieView;

import com.quickaction3d.QuickAction;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.chatsdk.util.FilterWordsManager.replaceSensitiveWord;

public final class MessagesAdapter extends BaseAdapter
{
	private Context				c;
	private List<MsgItem>		items;
	private ArrayList<MsgItem>		itemsBackup;
	private LayoutInflater		inflater;
	private QuickAction			quickAction;

	private static final int	ITEM_MESSAGE_SEND		= 0;
	private static final int	ITEM_MESSAGE_RECEIVE	= 1;
	private static final int	ITEM_GIF_SEND			= 2;
	private static final int	ITEM_GIF_RECEIVE		= 3;
	private static final int	ITEM_PIC_SEND			= 4;
	private static final int	ITEM_PIC_RECEIVE		= 5;
	private static final int	ITEM_REDPACKAGE_SEND	= 6;
	private static final int	ITEM_REDPACKAGE_RECEIVE	= 7;
	private static final int	ITEM_CHATROOM_TIP		= 8;
	private static final int	ITEM_NEWMESSAGE_TIP		= 9;
	private static final int	ITEM_MSG_COMMENT_SEND		= 10;
	private static final int	ITEM_MSG_COMMENT_RECEIVE	= 11;
	private static final int	ITEM_TYPE_TOTAL_COUNT	= 12;

	public MessagesAdapter(Fragment f, int textViewResourceId, ArrayList<MsgItem> objects)
	{
		this.c = f.getActivity();
		this.itemsBackup = objects;
		this.items = (ArrayList<MsgItem>)itemsBackup.clone();
		this.inflater = ((LayoutInflater) this.c.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
	}

	private QuickAction.OnActionItemClickListener	actionClickListener	= new QuickAction.OnActionItemClickListener()
																		{

																			@Override
																			public void onItemClick(QuickAction source, int pos,
																					int actionId)
																			{
																				handleItemClick(source, pos, actionId);
																			}
																		};

	private void handleItemClick(final QuickAction source, final int pos, final int actionId)
	{
		final MessagesAdapter adapter = this;
		try
		{
			MenuController.handleItemClick(adapter, source, pos, actionId);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public static MsgItem getMsgItemFromQuickAction(QuickAction quickAction)
	{
		// return ((MessageHolder)
		// quickAction.currentTextView.getTag()).msgItem;
		if (quickAction.currentTextView != null && quickAction.currentTextView.getTag() != null)
			return ((MessageViewHolder) quickAction.currentTextView.getTag()).currentMsgItem;
		return null;
	}

	public void showTranslatedLanguage(final TextView textView, final MsgItem item)
	{
		if (TranslateManager.getInstance().isTranslateMsgValid(item))
		{
			item.hasTranslated = true;
			item.isTranslatedByForce = true;
			item.hasTranslatedByForce = true;
			item.isOriginalLangByForce = false;
			setText(textView, item.translateMsg, item, true);
		}
		else
		{
			TranslateManager.getInstance().loadTranslation(item, new TranslateListener() {
				@Override
				public void onTranslateFinish(final String translateMsg) {
					if (!item.canShowTranslateMsg() || StringUtils.isEmpty(translateMsg) || translateMsg.startsWith("{\"code\":{"))
						return;
					item.isOriginalLangByForce = false;
					String toAll = LanguageManager.getLangByKey(LanguageKeys.TIP_TO_ALL);
					if(item.msg.contains(toAll)){
						final String resultStr = toAll.concat(translateMsg);
						item.translateMsg = resultStr;
						setTextOnUIThread(textView, resultStr, item);
					}else {
						setTextOnUIThread(textView, translateMsg, item);
					}
				}
			});
		}
	}

	private void setTextOnUIThread(final TextView textView, final String translateMsg, final MsgItem item)
	{
		if (c != null && c instanceof Activity)
		{
			((Activity) c).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						setText(textView, translateMsg, item, true);
					} catch (Exception e) {
						LogUtil.printException(e);
					}
				}
			});
		}
	}

	public void showOriginalLanguage(TextView textView, MsgItem item)
	{
		item.hasTranslated = false;
		item.isTranslatedByForce = false;
		item.isOriginalLangByForce = true;
		setText(textView, item.msg, item, false);
	}

	// TODO 返回值是否正确？
	public boolean isEnabled(int position)
	{
		return false;
	}

	@Override
	public int getViewTypeCount()
	{
		return ITEM_TYPE_TOTAL_COUNT;
	}

	@Override
	public int getItemViewType(int position)
	{
		if (position >= 0 && position < items.size())
		{
			MsgItem item = items.get(position);
			if (item != null)
			{
				int type = item.getMsgItemType(c);
				boolean isSelfMsg = item.isSelfMsg();
				if (type == MsgItem.MSGITEM_TYPE_MESSAGE)
				{
					return isSelfMsg ? ITEM_MESSAGE_SEND : ITEM_MESSAGE_RECEIVE;
				}
				else if (type == MsgItem.MSGITEM_TYPE_MESSAGE_COMMENT)
				{
					return isSelfMsg ? ITEM_MSG_COMMENT_SEND : ITEM_MSG_COMMENT_RECEIVE;
				}
				else if (type == MsgItem.MSGITEM_TYPE_GIF)
				{
					return isSelfMsg ? ITEM_GIF_SEND : ITEM_GIF_RECEIVE;
				}
				else if (type == MsgItem.MSGITEM_TYPE_PIC)
				{
					return isSelfMsg ? ITEM_PIC_SEND : ITEM_PIC_RECEIVE;
				}
				else if (type == MsgItem.MSGITEM_TYPE_REDPACKAGE)
				{
					return isSelfMsg ? ITEM_REDPACKAGE_SEND : ITEM_REDPACKAGE_RECEIVE;
				}
				else if (type == MsgItem.MSGITEM_TYPE_CHATROM_TIP)
				{
					return ITEM_CHATROOM_TIP;
				}
				else if (type == MsgItem.MSGITEM_TYPE_NEW_MESSAGE_TIP)
				{
					return ITEM_NEWMESSAGE_TIP;
				}
			}
		}
		return -1;
	}

	private boolean needShowTime(int position)
	{
		int itemType = getItemViewType(position);
		if (itemType == ITEM_NEWMESSAGE_TIP)
			return false;
		return true;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (position < 0 || position >= items.size())
			return null;

		final MsgItem item = (MsgItem) this.items.get(position);
		if (item == null)
		{
			return null;
		}
		int type = item.getMsgItemType(c);

		if (convertView == null)
		{
			convertView = createViewByMessage(item, position, type);
			adjustSize(convertView, type);
		}

		MessageViewHolder holder = ViewHolderHelper.getViewHolder(convertView);
		if (holder != null)
			holder.currentMsgItem = item;

//		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "position", position, "msg", item.msg, "items.size()",
//				items.size(), "item.uid", item.uid);

		if (type == MsgItem.MSGITEM_TYPE_NEW_MESSAGE_TIP)
		{
			ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
			if (channel != null && WebSocketManager.isRecieveFromWebSocket(channel.channelType) && channel.wsNewMsgCount > 0)
			{
				channel.wsNewMsgCount = 0;
				if (getChatFragment() != null)
				{
					getChatFragment().refreshToolTip();
				}
			}
			setNewMsgTipData(convertView, item);
		}
		else
		{
			if (needShowTime(position))
				setSendTimeData(convertView, item);

			if (type == MsgItem.MSGITEM_TYPE_CHATROM_TIP)
			{
				setChatRoomTipData(convertView, item);
			}
			else
			{
				adjustHeadImageContainerSize(convertView, item);
				setPlayerData(convertView, item);
				addHeadImageOnClickAndTouchListener(convertView, item);
				addSendStatusTimerAndRefresh(convertView, item);
				addOnClickSendStateListener(convertView, item);

				int sdk = android.os.Build.VERSION.SDK_INT;
				setHeadImageBackground(convertView, item, sdk);

				if (type == MsgItem.MSGITEM_TYPE_MESSAGE || type == MsgItem.MSGITEM_TYPE_MESSAGE_COMMENT)
				{
					setMessageTextBackground(convertView, item, sdk);
					setMessageData(convertView, item);

					if(item.isShareCommentMsg()){
						TextView messageText = (TextView) convertView.findViewById(R.id.messageText);
						setText(messageText, item.msg, item, false);
					}

				}
				else if (type == MsgItem.MSGITEM_TYPE_REDPACKAGE)
				{
					setRedPackageBackground(convertView, item, sdk);
					setRedPackageData(convertView, item);
				}
				else if (type == MsgItem.MSGITEM_TYPE_GIF)
				{
					setGifData(convertView, item);
				}
				else if (type == MsgItem.MSGITEM_TYPE_PIC)
				{
					setPicData(convertView, item);
				}
			}
		}
		return convertView;
	}

	private void addItemOnClickListener(final View convertView)
	{
		MessageViewHolder holder = ViewHolderHelper.getViewHolder(convertView);
		if (holder == null)
			return;
		final MsgItem item = holder.currentMsgItem;
		if (item == null)
			return;
		OnClickListener onClickListener = new View.OnClickListener()
		{
			public void onClick(View view)
			{
				if (c == null || ((ChatActivity) c).fragment == null || ((ChatFragment) ((ChatActivity) c).fragment).isKeyBoradShowing)
				{
					return;
				}
				if (ignoreClick)
				{
					ignoreClick = false;
					return;
				}

				if (item.isRedPackageMessage())
				{
					String[] redPackageInfoArr = item.attachmentId.split("\\|");
					if(redPackageInfoArr.length == 2){
						if(ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_ALLIANCE
								&& !item.isGetSystemRedPackage() ){
                            ServiceInterface.flyHint("", "", LanguageManager.getLangByKey("79011055"), 0, 0, false);
							return;
						}
					}

					if (item.sendState == MsgItem.HANDLED) {
						if (ChatServiceController.isNeedReplaceBadWords()) {
							item.msg = FilterWordsManager.replaceSensitiveWord(item.msg, 1, "*");
						}
						ChatServiceController.doHostAction("viewRedPackage", "", item.msg, redPackageInfoArr[0], true);
					}else
					{
						if (ChatServiceController.getChatFragment() != null)
							ChatServiceController.getChatFragment().showRedPackageConfirm(item);

					}
				}
				else
				{
					if (quickAction != null)
					{
						quickAction.setOnActionItemClickListener(null);
					}

					quickAction = ChatQuickActionFactory.createQuickAction((Activity) c, item);
					if(quickAction.getMaxItemWidth() == 0)
						return;
					quickAction.setOnActionItemClickListener(actionClickListener);
					quickAction.currentTextView = (TextView) view;
					boolean isFous = false;
					if(ChatServiceController.getInstance().host.getNativeGetIsShowStatusBar()) {
						isFous = true;
					}
					View showView;
					if(item.isShareCommentMsg()) {
						showView = (LinearLayout)view.getParent();
					}else{
						showView = (TextView) view;
					}
					quickAction.show(showView,isFous);
				}
			}
		};

		if (item.isRedPackageMessage())
		{
			LinearLayout red_package_top_layout = ViewHolderHelper.get(convertView, R.id.red_package_top_layout);
			if (red_package_top_layout != null)
				red_package_top_layout.setOnClickListener(onClickListener);

			LinearLayout red_package_bottom_layout = ViewHolderHelper.get(convertView, R.id.red_package_bottom_layout);
			if (red_package_bottom_layout != null)
				red_package_bottom_layout.setOnClickListener(onClickListener);
		}
		else
		{
			TextView messageText = ViewHolderHelper.get(convertView, R.id.messageText);
			if (messageText != null)
			{
				messageText.setTag(holder);
				messageText.setOnClickListener(onClickListener);
			}
			if(item.isShareCommentMsg()) {
				TextView commentText = ViewHolderHelper.get(convertView, R.id.commentText);
				if (commentText != null) {
					commentText.setTag(holder);
					commentText.setOnClickListener(onClickListener);
				}
			}

		}
	}

	private void adjustSize(View convertView, int type)
	{
		if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			adjustTextSize(convertView);

			if (type != MsgItem.MSGITEM_TYPE_CHATROM_TIP && type != MsgItem.MSGITEM_TYPE_NEW_MESSAGE_TIP)
			{
				int length = (int) (ScaleUtil.dip2px(c, 50) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
				ImageView headImage = ViewHolderHelper.get(convertView, R.id.headImage);
				if (headImage != null)
				{
					FrameLayout.LayoutParams headImageLayoutParams = (FrameLayout.LayoutParams) headImage.getLayoutParams();
					headImageLayoutParams.width = length;
					headImageLayoutParams.height = length;
					headImage.setLayoutParams(headImageLayoutParams);
				}
			}

			if (type == MsgItem.MSGITEM_TYPE_REDPACKAGE)
			{
				int headImageContainerWidth = (int) (ScaleUtil.dip2px(c, 60) * ConfigManager.scaleRatio * getScreenCorrectionFactor());

				LinearLayout redpackage_layout = ViewHolderHelper.get(convertView, R.id.redpackage_layout);
				if (redpackage_layout != null)
				{
					int redPackageWidth = ScaleUtil.getScreenWidth() - headImageContainerWidth;
					LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) redpackage_layout.getLayoutParams();
					int targetWidht = redPackageWidth * 70 / 100;//0.75
//					if (redPackageWidth > 600)
//						targetWidht = 600;
					linearParams.width = targetWidht;
					redpackage_layout.setLayoutParams(linearParams);
				}

				ImageView red_package_image = ViewHolderHelper.get(convertView, R.id.red_package_image);
				if (red_package_image != null)
				{
					// 节日红包开关开启
					if(ChatServiceController.isFestivalRedPackageEnable){
						red_package_image.setImageResource(R.drawable.red_package_icon_festival);
					}
					int originalHeight = 36;
					int targetRedPackageHeight = (int) (ScaleUtil.dip2px(c, originalHeight) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
					LinearLayout.LayoutParams redPackageLayout = (LinearLayout.LayoutParams) red_package_image.getLayoutParams();
					redPackageLayout.width = targetRedPackageHeight;
					redPackageLayout.height = targetRedPackageHeight;
					red_package_image.setLayoutParams(redPackageLayout);
				}
			}
		}
	}

	private void adjustHeadImageContainerSize(View convertView, MsgItem item)
	{
		if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			FrameLayout headImageContainer = ViewHolderHelper.get(convertView, R.id.headImageContainer);
			if (headImageContainer != null)
			{
				int width = (int) (ScaleUtil.dip2px(c, 50) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
				int height = (int) (ScaleUtil.dip2px(c, 50) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
				LinearLayout.LayoutParams headImageContainerLayoutParams = (LinearLayout.LayoutParams) headImageContainer.getLayoutParams();
				headImageContainerLayoutParams.width = width;
				headImageContainerLayoutParams.height = height;
				headImageContainer.setLayoutParams(headImageContainerLayoutParams);
			}
			
			ImageView headImageKingIcon = ViewHolderHelper.get(convertView, R.id.headImageKingIcon);
			if(headImageKingIcon!=null)
			{  
				int width = (int) (ScaleUtil.dip2px(c, 40) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
				int height = (int) (ScaleUtil.dip2px(c, 22) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
				LinearLayout.LayoutParams headImageKingIconLayoutParams = (LinearLayout.LayoutParams) headImageKingIcon.getLayoutParams();
				headImageKingIconLayoutParams.width = width;
				headImageKingIconLayoutParams.height = height;
				headImageKingIcon.setLayoutParams(headImageKingIconLayoutParams);
			}
		}
	}

	private void adjustTextSize(View convertView)
	{
		TextView newMsgLabel = ViewHolderHelper.get(convertView, R.id.newMsgLabel);
		if (newMsgLabel != null)
			ScaleUtil.adjustTextSize(newMsgLabel, ConfigManager.scaleRatio);

		TextView sendDateLabel = ViewHolderHelper.get(convertView, R.id.sendDateLabel);
		if (sendDateLabel != null)
			ScaleUtil.adjustTextSize(sendDateLabel, ConfigManager.scaleRatio);

		TextView messageText_center = ViewHolderHelper.get(convertView, R.id.messageText_center);
		if (messageText_center != null)
			ScaleUtil.adjustTextSize(messageText_center, ConfigManager.scaleRatio);

		TextView messageText = ViewHolderHelper.get(convertView, R.id.messageText);
		if (messageText != null)
			ScaleUtil.adjustTextSize(messageText, ConfigManager.scaleRatio);

		TextView commentText = ViewHolderHelper.get(convertView, R.id.commentText);
		if (commentText != null)
			ScaleUtil.adjustTextSize(commentText, ConfigManager.scaleRatio);
//		TextView vipLabel = ViewHolderHelper.get(convertView, R.id.vipLabel);
//		if (vipLabel != null)
//			ScaleUtil.adjustTextSize(vipLabel, ConfigManager.scaleRatio);

		TextView allianceLabel = ViewHolderHelper.get(convertView, R.id.allianceLabel);
		if (allianceLabel != null)
			ScaleUtil.adjustTextSize(allianceLabel, ConfigManager.scaleRatio);

		TextView nameLabel = ViewHolderHelper.get(convertView, R.id.nameLabel);
		if (nameLabel != null)
			ScaleUtil.adjustTextSize(nameLabel, ConfigManager.scaleRatio);

		TextView red_package_title = ViewHolderHelper.get(convertView, R.id.red_package_title);
		
		if (red_package_title != null)
			ScaleUtil.adjustTextSize(red_package_title, ConfigManager.scaleRatio);

		TextView red_package_content = ViewHolderHelper.get(convertView, R.id.red_package_content);
		if (red_package_content != null)
			ScaleUtil.adjustTextSize(red_package_content, ConfigManager.scaleRatio);

		TextView redpackage_time = ViewHolderHelper.get(convertView, R.id.redpackage_time);
		if (redpackage_time != null)
			ScaleUtil.adjustTextSize(redpackage_time, ConfigManager.scaleRatio);
	}

	public void refreshSendState(View convertView, final MsgItem msgItem)
	{
		final ProgressBar send_progressbar = ViewHolderHelper.get(convertView, R.id.send_progressbar);
		final ImageView sendFail_image = ViewHolderHelper.get(convertView, R.id.sendFail_image);
		if (send_progressbar == null || sendFail_image == null)
			return;

		if (msgItem.sendState == MsgItem.SENDING)
		{
			if (send_progressbar.getVisibility() != View.VISIBLE)
				send_progressbar.setVisibility(View.VISIBLE);
			if (sendFail_image.getVisibility() != View.GONE)
				sendFail_image.setVisibility(View.GONE);
		}
		else if (msgItem.sendState == MsgItem.SEND_FAILED)
		{

			if (send_progressbar.getVisibility() != View.GONE)
				send_progressbar.setVisibility(View.GONE);
			if (send_progressbar.getVisibility() != View.VISIBLE)
				sendFail_image.setVisibility(View.VISIBLE);
		}
		else if (msgItem.sendState == MsgItem.SEND_SUCCESS)
		{
			if (send_progressbar.getVisibility() != View.GONE)
				send_progressbar.setVisibility(View.GONE);
			if (sendFail_image.getVisibility() != View.GONE)
				sendFail_image.setVisibility(View.GONE);
		}
	}

	private void addSendStatusTimerAndRefresh(final View convertView, final MsgItem item)
	{
		if (!item.isSelfMsg())
			return;
		if (!item.isRedPackageMessage())
			refreshSendState(convertView, item);
		else
		{
			final ProgressBar send_progressbar = ViewHolderHelper.get(convertView, R.id.send_progressbar);
			final ImageView sendFail_image = ViewHolderHelper.get(convertView, R.id.sendFail_image);
			if (send_progressbar != null && send_progressbar.getVisibility() != View.GONE)
				send_progressbar.setVisibility(View.GONE);
			if (sendFail_image != null && sendFail_image.getVisibility() != View.GONE)
				sendFail_image.setVisibility(View.GONE);
		}

		final MessageViewHolder holder = ViewHolderHelper.getViewHolder(convertView);
		if (holder == null)
			return;
		if ((!item.isSystemMessage() || item.isHornMessage()))
		{
			if (item.sendState == MsgItem.SENDING)
			{
				if (holder.sendTimer == null)
				{
					holder.sendTimer = new Timer();
				}
				if (holder.sendTimerTask != null)
					return;
				holder.sendTimerTask = new TimerTask()
				{
					@Override
					public void run()
					{
						if (item.sendState == MsgItem.SENDING)
							item.sendState = MsgItem.SEND_FAILED;
						boolean isForbidden = FilterWordsManager.containsForbiddenWords(item.msg);
						if(isForbidden&&ConfigManager.isNewShieldingEnabled){
							item.sendState = MsgItem.SEND_SUCCESS;
						}
						if (c == null)
							return;

						((Activity) c).runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								if(item.sendState != MsgItem.SEND_SUCCESS){
									JniController.getInstance().excuteJNIVoidMethod("recordChat",new Object[]{});
									if(ChatServiceController.getChatFragment() != null){
										ChatServiceController.getChatFragment().refreshNetState(ChatServiceController.getCurrentChannelType(),false);//显示WiFi图标
									}
								}
								refreshSendState(convertView, item);
								holder.removeSendTimer();
							}
						});
					}
				};

				holder.sendTimer.schedule(holder.sendTimerTask, 15000);
			}
			else
			{
				if (holder.sendTimer == null)
					return;
				holder.removeSendTimer();
			}
		}
	}

	private void setGifData(View convertView, MsgItem item)
	{
		GifMovieView gifMovieView = ViewHolderHelper.get(convertView, R.id.gifMovieView);
		if (gifMovieView == null)
			return;
		String replacedEmoj = StickManager.getPredefinedEmoj(item.msg);
		if (replacedEmoj != null)
		{
			int resId = ResUtil.getId(c, "drawable", replacedEmoj);
			if (resId != 0 && c.getString(resId).endsWith(".gif"))
				gifMovieView.setMovieResource(resId);
		}
	}

	private void setPicData(View convertView, MsgItem item)
	{
		ImageView picImageView = ViewHolderHelper.get(convertView, R.id.picImageView);
		if (picImageView == null)
			return;
		String replacedEmoj = StickManager.getPredefinedEmoj(item.msg);
		if (replacedEmoj != null)
		{
			int resId = ResUtil.getId(c, "drawable", replacedEmoj);
			if (resId != 0)
				picImageView.setImageResource(resId);
		}
	}

	private void setMessageData(final View convertView, final MsgItem item)
	{
		int id = R.id.messageText;
		if(item.isShareCommentMsg()){
			id = R.id.commentText;
		}
		final TextView messageText = ViewHolderHelper.get(convertView, id);
		if (messageText == null)
			return;
		if (item.isSelfMsg())
		{
			setText(messageText, item.msg, item, false);
		}
		else
		{
			if (item.canShowTranslateMsg())
			{
				if (!item.isTipMsg() && !item.isUserADMsg())
					setText(messageText, item.translateMsg, item, true);
				else
					messageText.setText(item.translateMsg);
				TranslateManager.getInstance().enterTranlateQueue(item);
			}
			else
			{
				if (!item.isTipMsg() && !item.isUserADMsg())
					setText(messageText, item.msg, item, false);
				else
					messageText.setText(item.msg);
				if (ConfigManager.autoTranlateMode > 0)
				{
					TranslateManager.getInstance().loadTranslation(item, new TranslateListener()
					{
						@Override
						public void onTranslateFinish(final String translateMsg)
						{
							MessageViewHolder holder = ViewHolderHelper.getViewHolder(convertView);
							if (holder != null)
							{
								MsgItem msgItem = holder.currentMsgItem;
								if ((msgItem != null && !msgItem.equals(item)) || !item.canShowTranslateMsg()
										|| StringUtils.isEmpty(translateMsg) || translateMsg.startsWith("{\"code\":{"))
									return;
								String toAll = LanguageManager.getLangByKey(LanguageKeys.TIP_TO_ALL);
								if(item.msg.contains(toAll)){
									final String resultStr = toAll.concat(translateMsg);
									item.translateMsg = resultStr;
									setTextOnUIThread(messageText, resultStr, item);
								}else {
									setTextOnUIThread(messageText, translateMsg, item);
								}
							}
						}
					});
				}
			}
		}

		addItemOnClickListener(convertView);
		// addOnClickItemListener(convertView, item);
	}

	private void setRedPackageData(View convertView, MsgItem item)
	{
		item.handleRedPackageFinishState();

		TextView red_package_title = ViewHolderHelper.get(convertView, R.id.red_package_title);
		if (red_package_title != null)
		{
			String title = "";
			if (StringUtils.isNotEmpty(item.translateMsg)) {
				title = item.translateMsg;
			}
			else {
				title = item.msg;
			}

			if(ChatServiceController.isNeedReplaceBadWords()){
				title = FilterWordsManager.replaceSensitiveWord(title,1,"*");
			}
			// 大于10后尾省略号
			if (title.length()>30) {
				title = title.substring(0, 30) + "...";
			}
			red_package_title.setText(title);

			// 系统红包多一行
			String[] redPackageInfoArr = item.attachmentId.split("\\|");
			if (redPackageInfoArr.length == 2 && (redPackageInfoArr[1].equals("1") || redPackageInfoArr[1].equals("2"))) {
				red_package_title.setLines(3);
			}
		}

		TextView redpackage_time = ViewHolderHelper.get(convertView, R.id.redpackage_time);
		if (redpackage_time != null)
		{
			redpackage_time.setText(item.getSendTimeHM());

		}

		TextView red_package_content = ViewHolderHelper.get(convertView, R.id.red_package_content);
		if (red_package_content != null)
		{
			if (item.isSelfMsg())
				red_package_content.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RED_PACKAGE_CONTENT_2));
			else
				red_package_content.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RED_PACKAGE_CONTENT));
		}
		addItemOnClickListener(convertView);
		// addOnClickItemListener(convertView, item);
	}

	private void setPlayerData(View convertView, MsgItem item)
	{
//		TextView vipLabel = ViewHolderHelper.get(convertView, R.id.vipLabel);
//		if (vipLabel != null)
//			vipLabel.setText(item.getVipLabel());
		
		LinearLayout vipLayout = (LinearLayout) ViewHolderHelper.get(convertView,R.id.vip_layout);

		if(item.isSVIPMsg() && (ChatServiceController.isSVIPStyleMsg && item.isSelfMsg() || !item.isSelfMsg()) ){
			int [] imgId1 = {   //ImageView显示的图片数组
					R.drawable.svip1,
					R.drawable.svip2,
					R.drawable.svip3,
					R.drawable.svip4,
					R.drawable.svip5,
					R.drawable.svip6,
					R.drawable.svip7,
					R.drawable.svip8,
					R.drawable.svip9,
					R.drawable.svip10,
			};

			ImageView vip_image = ViewHolderHelper.get(convertView, R.id.vip_image);
			if(vip_image != null&&vipLayout != null){
				vip_image.setImageResource(R.drawable.svip_back);
			}

			ImageView vip_value_image = ViewHolderHelper.get(convertView, R.id.vip_valueimage);
			if(vip_value_image != null&&vipLayout != null){
				if(item.getSVipLevel()>0){
					vip_value_image.setImageResource(imgId1[item.getSVipLevel()-1]);
					vipLayout.setVisibility(View.VISIBLE);
					vipLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

				}else{
					vipLayout.setVisibility(View.INVISIBLE);
					vipLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
				}
			}
		}else{
			int [] imgId = {   //ImageView显示的图片数组
					R.drawable.vip1,
					R.drawable.vip2,
					R.drawable.vip3,
					R.drawable.vip4,
					R.drawable.vip5,
					R.drawable.vip6,
					R.drawable.vip7,
					R.drawable.vip8,
					R.drawable.vip9,
					R.drawable.vip10,
			};
			ImageView vip_value_image = ViewHolderHelper.get(convertView, R.id.vip_valueimage);

			ImageView vip_image = ViewHolderHelper.get(convertView, R.id.vip_image);
			if(vip_image != null&&vipLayout != null){
				vip_image.setImageResource(R.drawable.vip_back);
			}

			if(vip_value_image != null&&vipLayout != null){
				if(item.getVipLevel()>0){
					vip_value_image.setImageResource(imgId[item.getVipLevel()-1]);
					vipLayout.setVisibility(View.VISIBLE);
					vipLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

				}else{
					vipLayout.setVisibility(View.INVISIBLE);
					vipLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
				}
			}
		}

		
		TextView allianceLabel = ViewHolderHelper.get(convertView, R.id.allianceLabel);
		if (allianceLabel != null)
		{
			if (ChatServiceController.getCurrentChannelType() != DBDefinition.CHANNEL_TYPE_ALLIANCE)
				allianceLabel.setText(item.getAllianceLabel());
			else
				allianceLabel.setText("");
		}

		String headPic = item.getHeadPic();
		String name = item.getName();
		UserInfo user = item.getUser();
		if(item.post == MsgItem.MSG_TYPE_RED_PACKAGE) {
			String[] redPackageInfoArr = item.attachmentId.split("\\|");
			if (redPackageInfoArr.length == 2) {
				user = null;
				if (redPackageInfoArr[1].equals("1")) {
					headPic = "guide_player_icon";
					name = LanguageManager.getLangByKey(LanguageKeys.TIP_SYSTEM_PLAYER_NAME);
				} else if (redPackageInfoArr[1].equals("2")) {
					headPic = "festival_system_icon";
					name = LanguageManager.getLangByKey(LanguageKeys.TIP_FESTIVAl_PLAYER_NAME);
				}
			}
			if(item.msg.equals(LanguageManager.getLangByKey("90100001"))){
				headPic = "guide_player_icon";
				name = LanguageManager.getLangByKey(LanguageKeys.TIP_SYSTEM_PLAYER_NAME);
			}
		}else if(item.post == MsgItem.MSG_TYPE_USE_ITEM_SHARE || item.post == MsgItem.MSG_TYPE_AREA_MSG_TIP || item.post == MsgItem.MSG_TYPE_LIVEROOM_SYS || item.post == MsgItem.MSG_TYPE_GW_SYS){
			headPic = "guide_player_icon";
			name = LanguageManager.getLangByKey(LanguageKeys.TIP_SYSTEM_PLAYER_NAME);
		}


	TextView nameLabel = ViewHolderHelper.get(convertView, R.id.nameLabel);
		if (nameLabel != null)
		{
			if(UserManager.getInstance().getCurrentUser() != null){
				nameLabel.setText(name + ((item.getSrcServerId() > 0 && UserManager.getInstance().getCurrentUser().chatShowServerId > 0 && item.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY ) ? "#" + item.getSrcServerId() : ""));
			}
		}

		if( UserManager.getInstance().getCurrentUser() != null) {
			if (UserManager.getInstance().getCurrentUser().chatShowServerId > 0) {
				if (UserManager.getInstance().getCurrentUser().crossFightSrcServerId == item.getSrcServerId()) {
					nameLabel.setTextColor(Color.rgb(213, 220, 168));
					allianceLabel.setTextColor(Color.rgb(213, 220, 168));
				} else {
					if (item.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY && item.getSrcServerId() > 0) {//国家频道来自不同服颜色区分对待
						nameLabel.setTextColor(Color.rgb(255, 0, 0));
						allianceLabel.setTextColor(Color.rgb(255, 0, 0));
					} else {
						nameLabel.setTextColor(Color.rgb(213, 220, 168));
						allianceLabel.setTextColor(Color.rgb(213, 220, 168));
					}
				}
			}
		}


		ImageView headImage = ViewHolderHelper.get(convertView, R.id.headImage);
		if (headImage != null)
		{
			headImage.setTag(item.uid);
			ImageUtil.setHeadImage(c, headPic, headImage, user);
		}
		
		
		ImageView headImageKingIcon = ViewHolderHelper.get(convertView, R.id.headImageKingIcon);
		if(headImageKingIcon!=null)
		{
			if(item.isKingMsg() && headImageKingIcon.getVisibility()!=View.VISIBLE)
				headImageKingIcon.setVisibility(View.VISIBLE);
			else if(!item.isKingMsg() && headImageKingIcon.getVisibility()!=View.GONE)
				headImageKingIcon.setVisibility(View.GONE);
		}

		ImageView privilegeImage = ViewHolderHelper.get(convertView, R.id.privilegeImage);
		if (privilegeImage != null)
		{
			int idPrivilegeImage = getGmodResourceId(item.getGmod());
			if (idPrivilegeImage != 0)
			{
				privilegeImage.setImageResource(idPrivilegeImage);
			}
			else
			{
				privilegeImage.setImageDrawable(null);
			}
		}

	}

	@SuppressLint("ClickableViewAccessibility")
	private void addHeadImageOnClickAndTouchListener(View convertView, final MsgItem item)
	{
		FrameLayout headImageContainer = ViewHolderHelper.get(convertView, R.id.headImageContainer);
		if (headImageContainer == null)
			return;
		headImageContainer.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				int iAction = event.getAction();
				if (iAction == MotionEvent.ACTION_DOWN || iAction == MotionEvent.ACTION_MOVE)
				{
					CompatibleApiUtil.getInstance().setButtonAlpha(v, false);
				}
				else
				{
					CompatibleApiUtil.getInstance().setButtonAlpha(v, true);
				}
				return false;
			}
		});

		headImageContainer.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				if(item.post == 12)
				{
					// 系统红包头像不能点
					String[] redPackageInfoArr = item.attachmentId.split("\\|");
					if (redPackageInfoArr.length == 2) {
						return;
					}
				}
				if (!item.isSystemHornMsg())
				{
					ChatServiceController.isFromLiveExist = true;
					if (ChatServiceController.isContactMod)
						ChatServiceController.doHostAction("showPlayerInfo@mod", item.uid, item.getName(), "", true);
					else
						ChatServiceController.doHostAction("showPlayerInfo", item.uid, item.getName(), "", true);
				}
			}
		});
	}

	private void addOnClickItemListener(View convertView, final MsgItem item)
	{
		OnClickListener onClickListener = new View.OnClickListener()
		{
			public void onClick(View view)
			{
				if (c == null || ((ChatActivity) c).fragment == null || ((ChatFragment) ((ChatActivity) c).fragment).isKeyBoradShowing)
				{
					return;
				}
				if (ignoreClick)
				{
					ignoreClick = false;
					return;
				}

				if (item.isRedPackageMessage())
				{
					String[] redPackageInfoArr = item.attachmentId.split("\\|");
					if (item.sendState == MsgItem.HANDLED || item.isSelfMsg()){
						if(ChatServiceController.isNeedReplaceBadWords()){
							item.msg = FilterWordsManager.replaceSensitiveWord(item.msg,1,"*");
						}
						ChatServiceController.doHostAction("viewRedPackage", "", item.msg, redPackageInfoArr[0], true);
					}
					else
					{
						if (ChatServiceController.getChatFragment() != null)
							ChatServiceController.getChatFragment().showRedPackageConfirm(item);

					}
				}
				else
				{
					if (quickAction != null)
					{
						quickAction.setOnActionItemClickListener(null);
					}

					quickAction = ChatQuickActionFactory.createQuickAction((Activity) c, item);
					quickAction.setOnActionItemClickListener(actionClickListener);

					quickAction.currentTextView = (TextView) view;
					quickAction.show(view);
				}
			}
		};

		if (item.isRedPackageMessage())
		{
			LinearLayout red_package_top_layout = ViewHolderHelper.get(convertView, R.id.red_package_top_layout);
			if (red_package_top_layout != null)
				red_package_top_layout.setOnClickListener(onClickListener);
			LinearLayout red_package_bottom_layout = ViewHolderHelper.get(convertView, R.id.red_package_bottom_layout);
			if (red_package_bottom_layout != null)
				red_package_bottom_layout.setOnClickListener(onClickListener);
		}
		else
		{
			TextView messageText = ViewHolderHelper.get(convertView, R.id.messageText);
			if (messageText != null)
			{
				messageText.setOnClickListener(onClickListener);
			}
			if(item.isShareCommentMsg()){
				TextView commentText = ViewHolderHelper.get(convertView, R.id.commentText);
				if (commentText != null)
				{
					commentText.setOnClickListener(onClickListener);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setHeadImageBackground(View convertView, MsgItem msgItem, int sdk)
	{
		String headImageBg = "icon_kuang";
//		if (msgItem.isKingMsg())
//		{
//			headImageBg = "king_head_container";
//		}

		FrameLayout headImageContainer = ViewHolderHelper.get(convertView, R.id.headImageContainer);
		if (headImageContainer == null)
			return;
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			headImageContainer.setBackgroundDrawable(c.getResources().getDrawable(ResUtil.getId(c, "drawable", headImageBg)));
		}
		else
		{
			headImageContainer.setBackground(c.getResources().getDrawable(ResUtil.getId(c, "drawable", headImageBg)));
		}

	}

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setRedPackageBackground(View convertView, MsgItem msgItem, int sdk)
	{
		LinearLayout red_package_top_layout = ViewHolderHelper.get(convertView, R.id.red_package_top_layout);
		LinearLayout red_package_bottom_layout = ViewHolderHelper.get(convertView, R.id.red_package_bottom_layout);
		String topbackground = "redpackage_left_bg";
		String bottombackground = "redpackage_left_time_bg";

		if ((msgItem.isSelfMsg()&&!ChatServiceController.getInstance().isArOrPrGameLang())||(!msgItem.isSelfMsg()&&ChatServiceController.getInstance().isArOrPrGameLang()))
		{
			topbackground = "redpackage_right_bg";
			bottombackground = "redpackage_right_time_bg";
		}
		else
		{
			topbackground = "redpackage_left_bg";
			bottombackground = "redpackage_left_time_bg";
		}

		// 节日红包开关开启
		if(ChatServiceController.isFestivalRedPackageEnable){
			topbackground += "_festival";
			bottombackground += "_festival";
		}

		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			red_package_top_layout.setBackgroundDrawable(c.getResources().getDrawable(ResUtil.getId(c, "drawable", topbackground)));
			red_package_bottom_layout.setBackgroundDrawable(c.getResources().getDrawable(ResUtil.getId(c, "drawable", bottombackground)));
		}
		else
		{
			red_package_top_layout.setBackground(c.getResources().getDrawable(ResUtil.getId(c, "drawable", topbackground)));
			red_package_bottom_layout.setBackground(c.getResources().getDrawable(ResUtil.getId(c, "drawable", bottombackground)));
		}
	}

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setMessageTextBackground(View convertView, MsgItem msgItem, int sdk)
	{
		TextView messageText = ViewHolderHelper.get(convertView, R.id.messageText);
		LinearLayout messageLayout = ViewHolderHelper.get(convertView, R.id.messageLayout);
		if (messageText == null || (msgItem.isShareCommentMsg()&&messageLayout == null))
			return;

		boolean isLelfHorn = false;
		boolean isRightHorn = false;

		String background = "chatfrom_bg";
		messageText.setTextColor(Color.WHITE);
		if (msgItem.isSystemMessage())
		{
			if ((msgItem.isSelfMsg()&&!ChatServiceController.getInstance().isArOrPrGameLang())||(!msgItem.isSelfMsg()&&ChatServiceController.getInstance().isArOrPrGameLang()))
			{
				if (msgItem.isHornMessage()) {
					background = "horn_msg_right_bg";
					isRightHorn = true;
				}
				else {
					background = "chatsystem_right_bg";
				}
			}
			else
			{
				if (msgItem.isHornMessage()) {
					background = "horn_msg_left_bg";
					isLelfHorn = true;
				}
				else {
					background = "chatsystem_left_bg";
				}
			}
		}
		else
		{
			if ((msgItem.isSelfMsg()&&!ChatServiceController.getInstance().isArOrPrGameLang())||(!msgItem.isSelfMsg()&&ChatServiceController.getInstance().isArOrPrGameLang()))
			{
				if (msgItem.isKingMsg())
					background = "king_msg_right_bg";
				else
				{
//					if(ChatServiceController.isSVIPStyleMsg && msgItem.isSVIPMsg())
//					{
//						messageText.setTextColor(0xff4e2c0b);
//						background = "chatfrom_svip";
//					}
//					else
//					{
						if(msgItem.getMonthCard()>0)
						{
							background = "chatfrom_monthcard";
						}else {
							background = "chatfrom_bg";
						}
//					}
				}
			}
			else
			{
				if (msgItem.isKingMsg())
					background = "king_msg_left_bg";
				else
				{
//					if(msgItem.isSVIPMsg())
//					{
//						messageText.setTextColor(0xff4e2c0b);
//						background = "chatto_svip";
//					}
//					else
//					{
						if(msgItem.getMonthCard()>0)
						{
							background = "chatto_monthcard";
						}else {
							background = "chatto_bg";
						}
//					}
				}
			}
		}

		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			if(msgItem.isShareCommentMsg()) {
				messageLayout.setBackgroundDrawable(c.getResources().getDrawable(ResUtil.getId(c, "drawable", background)));
				return;
			}else {
				messageText.setBackgroundDrawable(c.getResources().getDrawable(ResUtil.getId(c, "drawable", background)));
			}
		}
		else
		{
			if(msgItem.isShareCommentMsg()) {
				messageLayout.setBackground(c.getResources().getDrawable(ResUtil.getId(c, "drawable", background)));
				return;
			}else {
				messageText.setBackground(c.getResources().getDrawable(ResUtil.getId(c, "drawable", background)));
			}
		}

		// 根据左右喇叭设置padding,color(必须在setbackground后执行)
		if (isLelfHorn) {
			int paddingLeft = ScaleUtil.dip2px(c, 20);
			int paddingTop = messageText.getPaddingTop();
			int paddingRight = ScaleUtil.dip2px(c, 26);
			int paddingBottom = messageText.getPaddingBottom();
			messageText.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
			messageText.setTextColor(Color.rgb(79, 48, 17));
		}
		if (isRightHorn) {
			int paddingLeft = ScaleUtil.dip2px(c, 26);
			int paddingTop = messageText.getPaddingTop();
			int paddingRight = ScaleUtil.dip2px(c, 22);
			int paddingBottom = messageText.getPaddingBottom();
			messageText.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
			messageText.setTextColor(Color.rgb(79, 48, 17));
		}
	}

	private void setSendTimeData(View convertView, MsgItem item)
	{
		TextView sendDateLabel = ViewHolderHelper.get(convertView, R.id.sendDateLabel);
		if (sendDateLabel == null)
			return;
		if (ChatServiceController.getInstance().isDifferentDate(item,items))
		{
			if (sendDateLabel.getVisibility() == View.GONE)
				sendDateLabel.setVisibility(View.VISIBLE);
			sendDateLabel.setText(item.getSendTimeToShow());
			if(ChatServiceController.sendTimeTextHeight==0)
				ChatServiceController.sendTimeTextHeight = sendDateLabel.getHeight();
		}
		else
		{
			if (sendDateLabel.getVisibility() == View.VISIBLE)
				sendDateLabel.setVisibility(View.GONE);
		}
	}

	private void setNewMsgTipData(View convertView, MsgItem item)
	{
		TextView newMsgLabel = ViewHolderHelper.get(convertView, R.id.newMsgLabel);
		if (newMsgLabel == null)
			return;
		if (item.firstNewMsgState == 1)
		{
			newMsgLabel.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NEW_MESSAGE_BELOW));
		}
		else
		{
			newMsgLabel.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_N_NEW_MESSAGE_BELOW, ChannelManager.LOAD_ALL_MORE_MAX_COUNT
					+ ""));
		}
	}

	private void setChatRoomTipData(View convertView, MsgItem item)
	{
		TextView messageText_center = ViewHolderHelper.get(convertView, R.id.messageText_center);
		if(item.isUserADMsg()){
			messageText_center.setTextAppearance(c, R.style.chat_text_ad_style);
		}
		if (messageText_center == null)
			return;
		if (item.canShowTranslateMsg())
		{
			messageText_center.setText(item.translateMsg);
			TranslateManager.getInstance().enterTranlateQueue(item);
		}
		else
		{
			messageText_center.setText(item.msg);
			TranslateManager.getInstance().enterTranlateQueue(item);
		}
	}

	private void addOnClickSendStateListener(View convertView, final MsgItem item)
	{
		if (!item.isSelfMsg())
			return;
		ImageView sendFail_image = ViewHolderHelper.get(convertView, R.id.sendFail_image);
		if (sendFail_image == null)
			return;
		sendFail_image.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (item.sendState != MsgItem.SEND_SUCCESS)
					MenuController.showReSendConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_RESEND), item);
			}
		});
	}

	public void setProgressBarState(boolean showProgressBar, boolean showSendFailImage)
	{

	}

	private int getGmodResourceId(int gmod)
	{
		int idPrivilegeImage = 0;
		switch (gmod)
		{
			case 2:
				idPrivilegeImage = R.drawable.mod;
				break;
			case 4:
				idPrivilegeImage = R.drawable.smod;
				break;
			case 5:
				idPrivilegeImage = R.drawable.tmod;
				break;
			case 3:
				idPrivilegeImage = R.drawable.gm;
				break;
			case 11:
				idPrivilegeImage = R.drawable.vip_certification;
				break;
			default:
				break;
		}
		return idPrivilegeImage;
	}

	@SuppressLint("InflateParams")
	private View createViewByMessage(MsgItem msgItem, int position, int type)
	{
		int itemType = getItemViewType(position);
		if(ChatServiceController.getInstance().isArOrPrGameLang()){
			if (type == MsgItem.MSGITEM_TYPE_MESSAGE) {
				if (itemType == ITEM_MESSAGE_SEND)
					return inflater.inflate(R.layout.ar_msgitem_message_send, null);
				else if (itemType == ITEM_MESSAGE_RECEIVE)
					return inflater.inflate(R.layout.ar_msgitem_message_receive, null);
			}
			else if(type == MsgItem.MSGITEM_TYPE_MESSAGE_COMMENT){
				if (itemType == ITEM_MSG_COMMENT_SEND)
					return inflater.inflate(R.layout.ar_msgitem_msg_comment_send, null);
				else if (itemType == ITEM_MSG_COMMENT_RECEIVE)
					return inflater.inflate(R.layout.ar_msgitem_msg_cooment_receive, null);
			}
			else if (type == MsgItem.MSGITEM_TYPE_GIF)
			{
				if (itemType == ITEM_GIF_SEND)
					return inflater.inflate(R.layout.ar_msgitem_gif_recieve, null);
				else if (itemType == ITEM_GIF_RECEIVE)
					return inflater.inflate(R.layout.ar_msgitem_gif_send, null);
			}
			else if (type == MsgItem.MSGITEM_TYPE_PIC)
			{
				if (itemType == ITEM_PIC_SEND)
					return inflater.inflate(R.layout.ar_msgitem_pic_send, null);
				else if (itemType == ITEM_PIC_RECEIVE)
					return inflater.inflate(R.layout.ar_msgitem_pic_receive, null);
			}
			else if (type == MsgItem.MSGITEM_TYPE_REDPACKAGE)
			{
				if (itemType == ITEM_REDPACKAGE_SEND)
					return inflater.inflate(R.layout.ar_msgitem_redpackage_send, null);
				else if (itemType == ITEM_REDPACKAGE_RECEIVE)
					return inflater.inflate(R.layout.ar_msgitem_redpackage_receive, null);
			}
			else if (type == MsgItem.MSGITEM_TYPE_CHATROM_TIP )
			{
				if (itemType == ITEM_CHATROOM_TIP)
					return inflater.inflate(R.layout.msgitem_chatroom_tip, null);
			}
			else if (type == MsgItem.MSGITEM_TYPE_NEW_MESSAGE_TIP)
			{
				return itemType == ITEM_NEWMESSAGE_TIP ? inflater.inflate(R.layout.msgitem_newmsg_tip, null) : null;
			}
		}else {
			if (type == MsgItem.MSGITEM_TYPE_MESSAGE) {
				if (itemType == ITEM_MESSAGE_SEND)
					return inflater.inflate(R.layout.msgitem_message_send, null);
				else if (itemType == ITEM_MESSAGE_RECEIVE)
					return inflater.inflate(R.layout.msgitem_message_receive, null);
			} else if(type == MsgItem.MSGITEM_TYPE_MESSAGE_COMMENT){
				if (itemType == ITEM_MSG_COMMENT_SEND)
					return inflater.inflate(R.layout.msgitem_msg_comment_send, null);
				else if (itemType == ITEM_MSG_COMMENT_RECEIVE)
					return inflater.inflate(R.layout.msgitem_msg_comment_receive, null);
			} else if (type == MsgItem.MSGITEM_TYPE_GIF) {
				if (itemType == ITEM_GIF_SEND)
					return inflater.inflate(R.layout.msgitem_gif_send, null);
				else if (itemType == ITEM_GIF_RECEIVE)
					return inflater.inflate(R.layout.msgitem_gif_receive, null);
			} else if (type == MsgItem.MSGITEM_TYPE_PIC) {
				if (itemType == ITEM_PIC_SEND)
					return inflater.inflate(R.layout.msgitem_pic_send, null);
				else if (itemType == ITEM_PIC_RECEIVE)
					return inflater.inflate(R.layout.msgitem_pic_receive, null);
			} else if (type == MsgItem.MSGITEM_TYPE_REDPACKAGE) {
				if (itemType == ITEM_REDPACKAGE_SEND)
					return inflater.inflate(R.layout.msgitem_redpackage_send, null);
				else if (itemType == ITEM_REDPACKAGE_RECEIVE)
					return inflater.inflate(R.layout.msgitem_redpackage_receive, null);
			} else if (type == MsgItem.MSGITEM_TYPE_CHATROM_TIP) {
				if (itemType == ITEM_CHATROOM_TIP)
					return inflater.inflate(R.layout.msgitem_chatroom_tip, null);
			} else if (type == MsgItem.MSGITEM_TYPE_NEW_MESSAGE_TIP) {
				return itemType == ITEM_NEWMESSAGE_TIP ? inflater.inflate(R.layout.msgitem_newmsg_tip, null) : null;
			}
		}
		return null;
	}

	private ChatFragment getChatFragment()
	{
		if (c != null && c instanceof ChatActivity && ((ChatActivity) c).fragment != null
				&& ((ChatActivity) c).fragment instanceof ChatFragment)
		{
			return ((ChatFragment) ((ChatActivity) c).fragment);
		}
		else
		{
			return null;
		}
	}

	private static final String	JOIN_NOW_URL	= "JoinNow";
	private static final String	SHOW_EQUIP_URL	= "ShowEquip";

	private String convertLineBreak(String input)
	{
		return input.replace("\n", "<br/>");
	}

	private int getColorByIndex(int index)
	{
		int color = 0;
		switch (index)
		{
			case 1:
				color = 0xFFFFFFFF;
				break;
			case 2:
				color = 0xFF24FF00;
				break;
			case 3:
				color = 0xFF0084FF;
				break;
			case 4:
				color = 0xFFF600FF;
				break;
			case 5:
				color = 0xFFFF8A00;
				break;
			case 6:
				color = 0xFFFFD200;
				break;
			case 7:
				color = 0xFFFF0000;
				break;
			case 8:
				color = 0xFFE7B554;
				break;
			case 9:
				color = 0xFFC4C4C4;
				break;
			default:
				color = 0xFFFFFFFF;
				break;
		}
		return color;
	}

	private void setText(TextView textView, String str, MsgItem item, boolean isTranslated)
	{
		if(item.isSystemMessageByKey()) {
			str = item.parseAttachmentId(textView,"",isTranslated,false);
			try {
				settingImagesAndColors(textView,str, item, isTranslated);
			}catch (Exception e){
			}
		}else {
			String equipName = "";
			String taskName = "";
			String allianceTreasureName = "";
			String pngName = "u_gwislands_camp"; //全面战争旗帜图标
			int cityOrFlagId = 0;
			List<String> gwChangeColorStr = new ArrayList<String>();
			int pngIndex = 0; //中间图标插入的位置
			int colorIndex = -1;
			if (item.isEquipMessage() || item.isNewCreateEquipMessage()) {
				String msgStr = item.attachmentId;
				if (StringUtils.isNotEmpty(msgStr)) {
					String[] equipInfo = msgStr.split("\\|");
					if (equipInfo.length == 2) {
						equipName = LanguageManager.getLangByKey(equipInfo[1]);
						if (StringUtils.isNumeric(equipInfo[0]))
							colorIndex = Integer.parseInt(equipInfo[0]);
					}
				}
				str = LanguageManager.getLangByKey(LanguageKeys.TIP_EQUIP_SHARE, equipName);
			} else if (item.isEquipmentMedalShare()) {
				String msgStr = item.msg;
				if (StringUtils.isNotEmpty(msgStr)) {
					String[] equipInfo = msgStr.split("\\;");
					if (equipInfo.length == 2) {
						equipName = LanguageManager.getLangByKey(equipInfo[0]);
						if (StringUtils.isNumeric(equipInfo[1]))
							colorIndex = Integer.parseInt(equipInfo[1]);

						str = "[" + equipName + "]";
					} else if (equipInfo.length == 3) {

						if (StringUtils.isNumeric(equipInfo[2]) && Integer.parseInt(equipInfo[2]) == 1) {
							equipName = LanguageManager.getLangByKey(equipInfo[0]);
							if (StringUtils.isNumeric(equipInfo[1]))
								colorIndex = Integer.parseInt(equipInfo[1]);

							str = LanguageManager.getLangByKey(LanguageKeys.TIP_EQUIP_SHARE, equipName);
						} else {
							equipName = LanguageManager.getLangByKey(equipInfo[0]);
							if (StringUtils.isNumeric(equipInfo[1]))
								colorIndex = Integer.parseInt(equipInfo[1]);

							str = "[" + equipName + "]";
						}
					}
				}

			} else if (item.isAllianceTaskMessage()) {
				String msgStr = item.msg;
				if (StringUtils.isNotEmpty(msgStr)) {
					String[] taskInfo = msgStr.split("\\|");
					if (taskInfo.length >= 4) {
						taskName = LanguageManager.getLangByKey(taskInfo[2]);
						if (StringUtils.isNumeric(taskInfo[0]))
							colorIndex = Integer.parseInt(taskInfo[0]);
						String taskPlayerName = taskInfo[3];
						if (taskInfo.length > 4) {
							for (int i = 4; i < taskInfo.length; i++) {
								taskPlayerName += "|" + taskInfo[i];
							}
						}
						if (StringUtils.isNotEmpty(taskPlayerName)) {
							try {
								List<AllianceTaskInfo> taskInfoArr = JSON.parseArray(taskPlayerName, AllianceTaskInfo.class);
								if (taskInfoArr != null && taskInfoArr.size() >= 1 && taskInfoArr.get(0) != null) {
									String publisher = taskInfoArr.get(0).getName();
									if (taskInfoArr.size() == 1 && taskInfo[1].equals(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_1)) {
										str = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_1, publisher, taskName);
									} else if (taskInfoArr.size() == 2 && taskInfo[1].equals(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_2)) {
										AllianceTaskInfo taskInfo2 = taskInfoArr.get(1);
										if (taskInfo2 != null) {
											str = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_2, publisher, taskName,
													taskInfo2.getName());
										}
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}

			} else if (item.isAllianceTreasureMessage()) {
				String name = item.getAllianceTreasureInfo(1);
				if (StringUtils.isNotEmpty(name) && StringUtils.isNumeric(name))
					allianceTreasureName = LanguageManager.getLangByKey(name);
				if (StringUtils.isNotEmpty(allianceTreasureName))
					str = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_TREASURE_SHARE, allianceTreasureName);
				String colorStr = item.getAllianceTreasureInfo(0);
				if (StringUtils.isNotEmpty(colorStr) && StringUtils.isNumeric(colorStr))
					colorIndex = Integer.parseInt(colorStr);
			} else if (item.isGWSysTips()) {
				String attachmentId = item.attachmentId;
				String[] attachments = attachmentId.split("__");
				if (attachments.length == 0)
					return;
				String[] attachmentIds = attachments[1].split("\\|");
				String dialogKey = attachmentIds[0];
				gwChangeColorStr.add(attachmentIds[1]);
				if (attachmentIds.length == 3) {
					if (NumberUtils.isNumber(attachmentIds[2])) {
						cityOrFlagId = Integer.parseInt(attachmentIds[2]);
						pngName = pngName.concat(String.valueOf(cityOrFlagId));
					}
					item.msg = LanguageManager.getLangByKey(dialogKey, attachmentIds[1], pngName);
					pngIndex = item.msg.indexOf(pngName) + 1;
					item.msg = item.msg.replace(pngName, "");
				} else if (attachmentIds.length == 4) {
					if (NumberUtils.isNumber(attachmentIds[2])) {
						cityOrFlagId = Integer.parseInt(attachmentIds[2]);
					}
					if (cityOrFlagId > 1000) {
						String cityName = LanguageManager.getLangByKey("82000992", String.valueOf(cityOrFlagId - 1000));
						gwChangeColorStr.add(cityName);
						gwChangeColorStr.add("Lv.".concat(attachmentIds[3]));
						item.msg = LanguageManager.getLangByKey(dialogKey, attachmentIds[1], cityName, attachmentIds[3]);
					} else {
						pngName = pngName.concat(String.valueOf(cityOrFlagId));
						gwChangeColorStr.add(attachmentIds[3]);
						item.msg = LanguageManager.getLangByKey(dialogKey, attachmentIds[1], pngName, attachmentIds[3]);
						pngIndex = item.msg.indexOf(pngName) + 1;
						item.msg = item.msg.replace(pngName, "");
					}
				}

				str = item.msg;
			} else if (item.isNewsCenterShare()) {
				String newsIdStr = "";
				String titleParams = "";
				String[] attachmentIDArray = item.attachmentId.split("_", 3); //只分割两次,防止将名字分割了
				if (attachmentIDArray.length == 3) {
					newsIdStr = attachmentIDArray[1];
					titleParams = attachmentIDArray[2];
				}
				String []titleParamsArr =titleParams.split("\\|\\|");
				String typeName = JniController.getInstance().excuteJNIMethod("getPropByIdType",new Object[]{newsIdStr,"name","newscontent",ConfigManager.LocalController.NewsCenterXml});
				typeName = LanguageManager.getLangByKey(typeName).concat("\n");
				String dialogKey = JniController.getInstance().excuteJNIMethod("getPropByIdType",new Object[]{newsIdStr,"title","newscontent",ConfigManager.LocalController.NewsCenterXml});
				String title1s = JniController.getInstance().excuteJNIMethod("getPropByIdType",new Object[]{newsIdStr,"title1","newscontent",ConfigManager.LocalController.NewsCenterXml});
				String []title1Arr = title1s.split("\\|");

				ArrayList<String> paramsArr = new ArrayList<>();
				int i = 0;
				for (String params : titleParamsArr){
					String type = title1Arr[i];
					String outMsg = "";
					if (type.equals("1")){//名称，直接使用后台
						outMsg = params;
					} else if (type.equals("2")){//官职名称(官职id)
						outMsg = JniController.getInstance().excuteJNIMethod("getPropByIdGroup",new Object[]{params,"name","office"});
					}else if (type.equals("6")){//大帝官职(官职id)
						outMsg = JniController.getInstance().excuteJNIMethod("getPropByIdGroup",new Object[]{params,"name","great_office"});
					} else if (type.equals("9")){//段位(段位id)
						outMsg = JniController.getInstance().excuteJNIMethod("getPropByIdType",new Object[]{newsIdStr,"name","newscontent", ConfigManager.LocalController.ArenaRankXml});
					}
					paramsArr.add(outMsg);
				}
				if (titleParamsArr.length == 0) {
					str = LanguageManager.getLangByKey(dialogKey);
				} else if (titleParamsArr.length == 1) {
					str = LanguageManager.getLangByKey(dialogKey,paramsArr.get(0));
				} else if (titleParamsArr.length == 2) {
					str = LanguageManager.getLangByKey(dialogKey,paramsArr.get(0),paramsArr.get(1));
				} else if (titleParamsArr.length == 3) {
					str = LanguageManager.getLangByKey(dialogKey,paramsArr.get(0),paramsArr.get(1),paramsArr.get(2));
				} else if (titleParamsArr.length == 4) {
					str = LanguageManager.getLangByKey(dialogKey,paramsArr.get(0),paramsArr.get(1),paramsArr.get(2),paramsArr.get(3));
				}
				str = typeName.concat(str);
				item.msg = str;
				str = item.msg;

			} else if (item.isViewQuestionActivity()) {
				colorIndex = 8;
				str = item.msg;
			} else if (item.isFavourPointShare()) {
				str = item.msg;
			} else if (item.isVersionInvalid()) {
				str = LanguageManager.getLangByKey(LanguageKeys.MSG_VERSION_NO_SUPPORT);
			} else if (item.isWoundedShare()) {
				str = item.msg;
			} else if (item.isShamoInhesionShare()) {
				str = item.msg;
			}

			item.currentText = str;
			// 将html特殊符号进行转义，否则"<"后面的内容会被Html.fromHtml吞掉
			//str = TextUtils.htmlEncode(str);
			// 转化坐标为链接

			String htmlLinkText = str;
			htmlLinkText = insertCoordinateLink(convertLineBreak(str));
			// annouce invite的链接，玩家不在联盟中才可见
//		if (item.isAnnounceInvite() && UserManager.getInstance().getCurrentUser().allianceId.equals(""))
//		{
//			htmlLinkText += "<a href='" + JOIN_NOW_URL + "," + item.attachmentId + "'> <u>"
//					+ LanguageManager.getLangByKey(LanguageKeys.BTN_JOIN_NOW) + " </u></a>";
//		}
			if (item.isCreateEquipMessage()) {
				htmlLinkText = "";
			}
			if (item.isFavourPointShare()) {
				htmlLinkText = str;
			}
			if (item.isShareCommentMsg() && textView.getId() == R.id.commentText) {
				htmlLinkText = "";
			}
			Spanned spannedText = Html.fromHtml(htmlLinkText);
			textView.setText(spannedText);

			textView.setMovementMethod(LinkMovementMethod.getInstance());


			CharSequence text = textView.getText();
			if (text instanceof Spannable) {
				int end = text.length();
				CharSequence text2 = textView.getText();

				SpannableStringBuilder style = new SpannableStringBuilder(text2);
				style.clearSpans();

				if (item.isShareCommentMsg() && textView.getId() == R.id.commentText) {
					String commentStr = "";
					if (isTranslated) {
						if (item.translateMsg.equals("90200021")) {
							commentStr = LanguageManager.getLangByKey("90200021");
						} else {
							commentStr = item.translateMsg;
						}
					} else {

						if (item.shareComment.equals("90200021")) {
							commentStr = LanguageManager.getLangByKey("90200021");
						} else {
							commentStr = item.shareComment;
						}
					}
					SpannableString styledResultText1 = new SpannableString(Html.fromHtml("<font color='#FFFFFF'>" + commentStr + "</font>"));
					style.append(styledResultText1);
					textView.setText(style);
					return;
				}
				if ((ChatServiceController.getCurrentChannelType() < DBDefinition.CHANNEL_TYPE_USER && item.isSystemMessage()
						&& !item.isHornMessage()) || item.isLiveRoomSys()) {
					ImageGetter imageGetter = new ImageGetter() {
						@Override
						public Drawable getDrawable(String source) {
							if (c == null)
								return null;

							Drawable d = c.getResources().getDrawable(R.drawable.sys);
							if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0) {
								d.setBounds(0, -10,
										(int) (d.getIntrinsicWidth() * 0.8f * ConfigManager.scaleRatio * getScreenCorrectionFactor()),
										(int) (d.getIntrinsicHeight() * 0.9f * ConfigManager.scaleRatio * getScreenCorrectionFactor()) - 10);
							} else {
								d.setBounds(0, -10, (int) (d.getIntrinsicWidth() * 0.8f), (int) (d.getIntrinsicHeight() * 0.9f) - 10);
							}
							// ((BitmapDrawable) d).setGravity(Gravity.TOP);
							return d;
						}
					};

					style.insert(0, Html.fromHtml("<img src='" + R.drawable.sys + "'/>", imageGetter, null));
				}

				//添加中间的图片
				if (item.isGWSysTips()) {

					{
//					style = new SpannableStringBuilder(text2);
//					style.clearSpans();
						String txt = text.toString();
						style.setSpan(new ForegroundColorSpan(getColorByIndex(9)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//					String regexStr ="/"; ;
//					regexStr.concat(LanguageManager.getLangByKey("82000992","[0-9]+")).concat("|Lv.[0-9]+".concat("/ig"));
//					Pattern p = Pattern
//							.compile(regexStr);
//					Matcher m = p.matcher(str);
						for (Iterator<String> tempStr = gwChangeColorStr.iterator(); tempStr.hasNext(); ) {
							String temp = (String) tempStr.next();
							int color = 0;
							if (temp.contains("No.") || temp.contains("Lv.")) {
								color = getColorByIndex(8);
							} else {
								color = getColorByIndex(0);
							}
							int start = txt.indexOf(temp) + 1;
							style.setSpan(new ForegroundColorSpan(color), start, start + temp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
						final String pngNameStr = pngName;
						if (cityOrFlagId >= 1 && cityOrFlagId <= 13 && pngIndex > 1) {
							ImageGetter imageGetter = new ImageGetter() {
								@Override
								public Drawable getDrawable(String source) {
									if (c == null)
										return null;

									Drawable d = c.getResources().getDrawable(ResUtil.getId(c, "drawable", pngNameStr));
									if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0) {
										d.setBounds(0, 0, (int) (d.getIntrinsicWidth() * 0.5 * ConfigManager.scaleRatio * getScreenCorrectionFactor()),
												(int) (d.getIntrinsicHeight() * 0.5 * ConfigManager.scaleRatio * getScreenCorrectionFactor()));
									} else {
										d.setBounds(0, 0, (int) (d.getIntrinsicWidth() * 0.5), (int) (d.getIntrinsicHeight() * 0.5));
									}
									return d;
								}
							};
							style.insert(pngIndex, Html.fromHtml("<img src='" + c.getResources().getDrawable(ResUtil.getId(c, "drawable", pngNameStr)) + "'/>", imageGetter, null));
						}
					}
				}
				// 添加末尾的战报图标
				boolean canViewBattleReport = ((item.isBattleReport() || item.isDetectReport()) && !UserManager.getInstance().getCurrentUser().allianceId
						.equals(""));
				if (canViewBattleReport) {
					ImageGetter imageGetter = new ImageGetter() {
						@Override
						public Drawable getDrawable(String source) {
							if (c == null)
								return null;

							Drawable d = c.getResources().getDrawable(R.drawable.mail_battlereport);
							if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0) {
								d.setBounds(0, 0, (int) (d.getIntrinsicWidth() * 0.5 * ConfigManager.scaleRatio * getScreenCorrectionFactor()),
										(int) (d.getIntrinsicHeight() * 0.5 * ConfigManager.scaleRatio * getScreenCorrectionFactor()));
							} else {
								d.setBounds(0, 0, (int) (d.getIntrinsicWidth() * 0.5), (int) (d.getIntrinsicHeight() * 0.5));
							}
							return d;
						}
					};

					style.append(Html.fromHtml("<img src='" + R.drawable.mail_battlereport + "'/>", imageGetter, null));
				}

				if (item.isFavourPointShare()) {
					ImageGetter imageGetter = new ImageGetter() {
						@Override
						public Drawable getDrawable(String source) {
							if (c == null)
								return null;

							Drawable d = c.getResources().getDrawable(R.drawable.point_share);
							if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0) {
								d.setBounds(0, 0, (int) (d.getIntrinsicWidth() * 0.5 * ConfigManager.scaleRatio * getScreenCorrectionFactor()),
										(int) (d.getIntrinsicHeight() * 0.5 * ConfigManager.scaleRatio * getScreenCorrectionFactor()));
							} else {
								d.setBounds(0, 0, (int) (d.getIntrinsicWidth() * 0.5), (int) (d.getIntrinsicHeight() * 0.5));
							}
							return d;
						}
					};

					style.append(Html.fromHtml("<img src='" + R.drawable.point_share + "'/>", imageGetter, null));
				}

				//打造装备分享
				if (item.isCreateEquipMessage()) {
					SpannableString styledResultText1 = new SpannableString(Html.fromHtml(item.msg));
					style.append(styledResultText1);
				}

				// 添加末尾的装备分享`
				if (item.isEquipMessage() || item.isNewCreateEquipMessage()) {

					int color = getColorByIndex(colorIndex);
					String txt = text.toString();
					int start = txt.indexOf(equipName) + 1;
					style.setSpan(new ForegroundColorSpan(color), start, start + equipName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

					ImageGetter imageGetter = new ImageGetter() {
						@Override
						public Drawable getDrawable(String source) {
							if (c == null)
								return null;

							Drawable d = c.getResources().getDrawable(R.drawable.equip_share);
							if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0) {
								d.setBounds(0, 0, (int) (d.getIntrinsicWidth() * 0.8 * ConfigManager.scaleRatio * getScreenCorrectionFactor()),
										(int) (d.getIntrinsicHeight() * 0.8 * ConfigManager.scaleRatio * getScreenCorrectionFactor()));
							} else {
								d.setBounds(0, 0, (int) (d.getIntrinsicWidth() * 0.8), (int) (d.getIntrinsicHeight() * 0.8));
							}
							return d;
						}
					};

					style.append(Html.fromHtml("<img src='" + R.drawable.equip_share + "'/>", imageGetter, null));
				} else if (item.isAllianceTaskMessage()) {

					int color = getColorByIndex(colorIndex);
					String txt = text.toString();
					int start = txt.indexOf(taskName) + 1;
					style.setSpan(new ForegroundColorSpan(color), start, start + taskName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} else if (item.isEquipmentMedalShare()) {
					int color = getColorByIndex(colorIndex);
					String txt = text.toString();
					int start = txt.indexOf(equipName) + 1;
					style.setSpan(new ForegroundColorSpan(color), start, start + equipName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


					String msgStr = item.msg;
					if (StringUtils.isNotEmpty(msgStr)) {
						String[] equipInfo = msgStr.split("\\;");
						if (equipInfo.length == 3) {
							if (StringUtils.isNumeric(equipInfo[2]) && Integer.parseInt(equipInfo[2]) == 1) {

								ImageGetter imageGetter = new ImageGetter() {
									@Override
									public Drawable getDrawable(String source) {
										if (c == null)
											return null;

										Drawable d = c.getResources().getDrawable(R.drawable.equip_share);
										if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0) {
											d.setBounds(0, 0, (int) (d.getIntrinsicWidth() * 0.8 * ConfigManager.scaleRatio * getScreenCorrectionFactor()),
													(int) (d.getIntrinsicHeight() * 0.8 * ConfigManager.scaleRatio * getScreenCorrectionFactor()));
										} else {
											d.setBounds(0, 0, (int) (d.getIntrinsicWidth() * 0.8), (int) (d.getIntrinsicHeight() * 0.8));
										}
										return d;
									}
								};

								style.append(Html.fromHtml("<img src='" + R.drawable.equip_share + "'/>", imageGetter, null));
							}
						}
					}
				} else if (item.isAllianceTreasureMessage()) {
					if (StringUtils.isNotEmpty(allianceTreasureName)) {
						int color = getColorByIndex(colorIndex);
						String txt = text.toString();
						int start = txt.indexOf(allianceTreasureName) + 1;
						style.setSpan(new ForegroundColorSpan(color), start, start + allianceTreasureName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				} else if (item.isViewQuestionActivity()) {
					int color = getColorByIndex(colorIndex);
					String txt = text.toString();
					int start = 1;
					style.setSpan(new ForegroundColorSpan(color), start, start + txt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				// 添加时间、翻译信息
				String time = item.getSendTimeHM();
				if (!item.isSelfMsg()) {
					if (isTranslated) {
						String originalLang = item.originalLang == null ? "" : item.originalLang;
						String[] originalLangArr = originalLang.split(",");
						String lang = originalLang;
						for (int i = 0; i < originalLangArr.length; i++) {
							if (StringUtils.isNotEmpty(originalLangArr[i])) {
								lang = LanguageManager.getOriginalLangByKey(originalLangArr[i]);
								if (!lang.startsWith("lang."))
									break;
							}
						}
						if(lang.length() <=0){
							lang = originalLang;
						}
						time += " " + LanguageManager.getLangByKey(LanguageKeys.TIP_TRANSLATED_BY, lang);
					} else if (!isTranslated
							&& TranslateManager.getInstance().isTranslateMsgValid(item)
							&& !item.isTranlateDisable()
							&& !item.isOriginalSameAsTargetLang()
							&& (ChatServiceController.isDefaultTranslateEnable || (!ChatServiceController.isDefaultTranslateEnable && item.hasTranslatedByForce))) {
						time += " " + LanguageManager.getLangByKey(LanguageKeys.MENU_ORIGINALLAN);
					}
				}

				String brStr = "";
				if (StringUtils.isNotEmpty(time))
					brStr = "<br/>";
				SpannableString styledResultText = new SpannableString(Html.fromHtml(brStr + "<small><font color='#9c9ea0'>" + time
						+ "</font></small>"));
				//if (!item.isSystemMessage() && (item.isKingMsg() /*|| item.isSVIPMsg() && (ChatServiceController.isSVIPStyleMsg && item.isSelfMsg() || !item.isSelfMsg()) */ ))
				if (!item.isSystemMessage() && item.isKingMsg())
					styledResultText = new SpannableString(Html.fromHtml(brStr + "<small><font color='#FFFFFF'>" + time + "</font></small>"));
				styledResultText.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_OPPOSITE), 0, styledResultText.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				style.append(styledResultText);

				Spannable sp = (Spannable) textView.getText();
				URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
				for (URLSpan url : urls) {
					MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
					if (item.isSystemMessage()) {
						int endPos = sp.getSpanEnd(url) + 1 <= end - 1 ? sp.getSpanEnd(url) + 1 : end;
						if (item.isCordinateShareMessage())
							endPos = sp.getSpanEnd(url) + 1 <= end ? sp.getSpanEnd(url) + 1 : end;
						style.setSpan(myURLSpan, sp.getSpanStart(url) + 1, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} else {
						style.setSpan(myURLSpan, sp.getSpanStart(url), sp.getSpanEnd(url) - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}

				textView.setText(style);
			}
		}
	}

	/**
	 * @param str
	 * @param item
	 * @param isTranslated
	 * @return
	 */
	private SpannableStringBuilder settingImagesAndColors(TextView textView,String str, MsgItem item,boolean isTranslated) {
		Color color;
		String tempContent = "";
		String pngName = "u_gwislands_camp"; //全面战争旗帜图标
		int cityOrFlagId = 0;
		List<String> gwChangeColorStr  = new ArrayList<String>();
		int pngIndex = 0; //中间图标插入的位置
		int colorIndex = -1;

		item.currentText = str;
		String htmlLinkText = insertCoordinateLink(convertLineBreak(str));
		Spanned spannedText = Html.fromHtml(htmlLinkText);
		textView.setText(spannedText);

		textView.setMovementMethod(LinkMovementMethod.getInstance());


		/***********添加图片和改变文字颜色。*******************************/
		if(item.isAllianceCreate() || item.isAlllianceMessage() || item.isAnnounceInvite()
				){
		}else if(item.isBattleReport() || item.isDetectReport()) {
		}
		//6,7,8,9无用
		else if(item.isLotteryMessage()){

		}else if(item.isAllianceTaskMessage() || item.isAllianceTreasureMessage()){

		}else if(item.isAllianceMonthCardBoxMessage()){

		}else if(item.isSevenDayMessage()){

		}else if(item.isMissleReport()){

		}else if(item.isAllianceGroupBuyMessage()){

		}else if(item.isGiftMailShare()){

		}else if (item.isEquipMessage() || item.isNewCreateEquipMessage()) {
			String msgStr = item.attachmentId;
			String equipName = "";
			if (StringUtils.isNotEmpty(msgStr)) {
				String[] equipInfo = msgStr.split("\\|");
				if (equipInfo.length == 2) {
					equipName = LanguageManager.getLangByKey(equipInfo[1]);
					if (StringUtils.isNumeric(equipInfo[0]))
						colorIndex = Integer.parseInt(equipInfo[0]);
				}
			}
			tempContent = equipName;
		}else if (item.isFavourPointShare()) {

		}else if(item.isWoundedShare()){

		}else if(item.isEquipmentMedalShare()){
			String msgStr = item.msg;
			String equipName = "";
			if (StringUtils.isNotEmpty(msgStr)) {
				String[] equipInfo = msgStr.split("\\;");
				if (equipInfo.length>= 2) {
					equipName = LanguageManager.getLangByKey(equipInfo[0]);
					if (StringUtils.isNumeric(equipInfo[1]))
						colorIndex = Integer.parseInt(equipInfo[1]);
				}
			}
			tempContent = equipName;
		}else if(item.isShamoInhesionShare()){

		}else if (item.isGWSysTips()) {
			String attachmentId = item.attachmentId;
			String[] attachments = attachmentId.split("__");
			String msg = "";
			if (attachments.length == 0)
				return null;
			String[] attachmentIds = attachments[1].split("\\|");
			String dialogKey = attachmentIds[0];
			gwChangeColorStr.add(attachmentIds[1]);
			if (attachmentIds.length == 3) {
				if (NumberUtils.isNumber(attachmentIds[2])) {
					cityOrFlagId = Integer.parseInt(attachmentIds[2]);
					pngName = pngName.concat(String.valueOf(cityOrFlagId));
				}
				msg = LanguageManager.getLangByKey(dialogKey, attachmentIds[1], pngName);
				pngIndex = msg.indexOf(pngName) + 1;
			} else if (attachmentIds.length == 4) {
				if (NumberUtils.isNumber(attachmentIds[2])) {
					cityOrFlagId = Integer.parseInt(attachmentIds[2]);
				}
				if (cityOrFlagId > 1000) {
					String cityName = LanguageManager.getLangByKey("82000992", String.valueOf(cityOrFlagId - 1000));
					gwChangeColorStr.add(cityName);
					gwChangeColorStr.add("Lv.".concat(attachmentIds[3]));
				} else {
					pngName = pngName.concat(String.valueOf(cityOrFlagId));
					gwChangeColorStr.add(attachmentIds[3]);
					msg = LanguageManager.getLangByKey(dialogKey, attachmentIds[1], pngName, attachmentIds[3]);
					pngIndex = msg.indexOf(pngName) + 1;
				}
			}
		}else if(item.isViewQuestionActivity()){

		}else if (item.isNewsCenterShare()) {
		}

		CharSequence text = textView.getText();
		if (text instanceof Spannable) {
			int end = text.length();
			CharSequence text2 = textView.getText();

			SpannableStringBuilder style = new SpannableStringBuilder(text2);
			style.clearSpans();

			int ResId = 0;
			int insertIndex = 0;
			String txt = text.toString();
			if (item.isShareCommentMsg() && textView.getId() == R.id.commentText) {
				setTextColors(style, txt, new String[]{txt}, new int[]{colorIndex});
				textView.setText(style);
				return style;
			}
			//添加系统图片
			if ((ChatServiceController.getCurrentChannelType() < DBDefinition.CHANNEL_TYPE_USER && item.isSystemMessage()
					&& !item.isHornMessage()) || item.isLiveRoomSys()) { //1
				ResId = ResUtil.getId(c, "drawable", "sys");
				setInsertImage(style,item, 0,ResId);
			}

			//添加中间的图片
			if (item.isGWSysTips()) { //2
				{
					style.setSpan(new ForegroundColorSpan(getColorByIndex(9)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					for (Iterator<String> tempStr = gwChangeColorStr.iterator(); tempStr.hasNext(); ) {
						String temp = (String) tempStr.next();
						if (temp.contains("No.") || temp.contains("Lv.")) {
							colorIndex = 8;
						} else {
							colorIndex = 0;
						}
						setTextColors(style, txt, new String[]{temp}, new int[]{colorIndex});
					}
					final String pngNameStr = pngName;
					if (cityOrFlagId >= 1 && cityOrFlagId <= 13 && pngIndex > 1) {
						ResId = ResUtil.getId(c, "drawable", pngNameStr);
						setInsertImage(style,item, pngIndex,ResId);
					}
				}
			}


			// 添加末尾的各种图片
			insertIndex = txt.length()+1;
			boolean canViewBattleReport = ((item.isBattleReport() || item.isDetectReport()) && !UserManager.getInstance().getCurrentUser().allianceId
					.equals(""));
			if (canViewBattleReport) { //1
				ResId = ResUtil.getId(c, "drawable", "mail_battlereport");
			}

			if (item.isFavourPointShare()) { //1
				ResId = ResUtil.getId(c, "drawable", "point_share");
			}

			if(item.isNewCreateEquipMessage() || item.isEquipmentMedalShare() || item.isEquipmentMedalShare()){
				ResId = ResUtil.getId(c, "drawable", "equip_share");
			}

			setTextColors(style, txt, new String[]{tempContent}, new int[]{colorIndex});

			setInsertImage(style,item, insertIndex,ResId);
			/***********添加时间和翻译提示。*******************************/

			setTime(textView,item,style,isTranslated, end);

			/***********添加时间和翻译提示结束*******************************/
			return style;
		}
		/***********添加图片和改变文字颜色结束*******************************/
		return null;
	}

	private void setTime(TextView textView, MsgItem item, SpannableStringBuilder style, boolean isTranslated, int end) {
		// 添加时间、翻译信息
		String time = item.getSendTimeHM();
		if (!item.isSelfMsg()) {
			if (isTranslated) {
				String originalLang = item.originalLang == null ? "" : item.originalLang;
				String[] originalLangArr = originalLang.split(",");
				String lang = originalLang;
				for (int i = 0; i < originalLangArr.length; i++) {
					if (StringUtils.isNotEmpty(originalLangArr[i])) {
						lang = LanguageManager.getOriginalLangByKey(originalLangArr[i]);
						if (!lang.startsWith("lang."))
							break;
					}
				}
				if(lang.length() <=0){
					lang = originalLang;
				}
				time += " " + LanguageManager.getLangByKey(LanguageKeys.TIP_TRANSLATED_BY, lang);
			} else if (!isTranslated
					&& TranslateManager.getInstance().isTranslateMsgValid(item)
					&& !item.isTranlateDisable()
					&& !item.isOriginalSameAsTargetLang()
					&& (ChatServiceController.isDefaultTranslateEnable || (!ChatServiceController.isDefaultTranslateEnable && item.hasTranslatedByForce)))
			{
				time += " " + LanguageManager.getLangByKey(LanguageKeys.MENU_ORIGINALLAN);
			}
		}
		String brStr = "";
		if (StringUtils.isNotEmpty(time))
			brStr = "<br/>";
		SpannableString styledResultText = new SpannableString(Html.fromHtml(brStr + "<small><font color='#9c9ea0'>" + time
				+ "</font></small>"));
		//if (!item.isSystemMessage() && (item.isKingMsg() /*|| item.isSVIPMsg() && (ChatServiceController.isSVIPStyleMsg && item.isSelfMsg() || !item.isSelfMsg()) */ ))
		if (!item.isSystemMessage() && item.isKingMsg())
			styledResultText = new SpannableString(Html.fromHtml(brStr + "<small><font color='#FFFFFF'>" + time + "</font></small>"));
		styledResultText.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_OPPOSITE), 0, styledResultText.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		style.append(styledResultText);

		Spannable sp = (Spannable) textView.getText();
		URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
		for (URLSpan url : urls)
		{
			MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
			if (item.isSystemMessage() && !item.isFavourPointShare())
			{
				int endPos = sp.getSpanEnd(url) + 1 <= end - 1 ? sp.getSpanEnd(url) + 1 : end;
				if(item.isCordinateShareMessage())
					endPos = sp.getSpanEnd(url) + 1 <= end  ? sp.getSpanEnd(url) + 1 : end;
				style.setSpan(myURLSpan, sp.getSpanStart(url) + 1, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			else
			{
				style.setSpan(myURLSpan, sp.getSpanStart(url), sp.getSpanEnd(url) - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}

		textView.setText(style);
	}

	/**
	 * 设置文本的某些特定文字的颜色
	 *
	 * @param style
	 * @param text				全部文本
	 * @param changeColoStrs	特定字符串
	 * @param colorIndex		颜色类型
	 * @return
	 */
	private SpannableStringBuilder setTextColors(SpannableStringBuilder style, String text,String[] changeColoStrs,int[] colorIndex){
		for(int i= 0;i<colorIndex.length;i++){
			int color = getColorByIndex(colorIndex[i]);
			int start = 0;
			if(!text.equals(changeColoStrs[i])){
				start = text.indexOf(changeColoStrs[i]) + 1;
			}
			int end = start + changeColoStrs[i].length();
			if(style.length() >= end) {
				style.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return style;
	}

	/**
	 * 设置插入的图片
	 *
	 * @param style
	 * @param item			聊天消息
	 * @param insertIndex	插入的位置
	 * @param ResId			插入图片的资源Id
	 * @return
	 */
	private SpannableStringBuilder setInsertImage(SpannableStringBuilder style,final MsgItem item,final int insertIndex,int ResId){
		if(ResId == 0)
			return style;
		final Drawable d = c.getResources().getDrawable(ResId);
		ImageGetter imageGetter = new ImageGetter()
		{
			@Override
			public Drawable getDrawable(String source)
			{
				if (c == null)
					return null;

				if(insertIndex == 0){
					d.setBounds(getImageBounds(item, d,0));
				}else{
					d.setBounds(getImageBounds(item, d,1));
				}
				return d;
			}
		};

		style.insert(insertIndex, Html.fromHtml("<img src='" + ResId + "'/>", imageGetter, null));
		return style;
	}

	/**
	 * 获取插入图片bounds大小
	 *
	 * @param item   聊天消息
	 * @param d		 图像drawable资源
	 * @return
	 */
	private Rect getImageBounds(MsgItem item,Drawable d,int type){
		Rect rect= new Rect();
		if(type == 0) {
			if ((ChatServiceController.getCurrentChannelType() < DBDefinition.CHANNEL_TYPE_USER && item.isSystemMessage()
					&& !item.isHornMessage()) || item.isLiveRoomSys()) {
				if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0) {
					rect.set(0, -10,
							(int) (d.getIntrinsicWidth() * 0.8f * ConfigManager.scaleRatio * getScreenCorrectionFactor()),
							(int) (d.getIntrinsicHeight() * 0.9f * ConfigManager.scaleRatio * getScreenCorrectionFactor()) - 10);
				} else {
					rect.set(0, -10, (int) (d.getIntrinsicWidth() * 0.8f), (int) (d.getIntrinsicHeight() * 0.9f) - 10);
				}
			}
		}else if(type == 1) {

			//添加中间的图片
			// 添加末尾的战报图标
			boolean canViewBattleReport = ((item.isBattleReport() || item.isDetectReport()) && !UserManager.getInstance().getCurrentUser().allianceId
					.equals(""));
			if (item.isGWSysTips() || item.isFavourPointShare() || canViewBattleReport) {
				if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0) {
					rect.set(0, 0, (int) (d.getIntrinsicWidth() * 0.5 * ConfigManager.scaleRatio * getScreenCorrectionFactor()),
							(int) (d.getIntrinsicHeight() * 0.5 * ConfigManager.scaleRatio * getScreenCorrectionFactor()));
				} else {
					rect.set(0, 0, (int) (d.getIntrinsicWidth() * 0.5), (int) (d.getIntrinsicHeight() * 0.5));
				}
			}

			// 添加末尾的装备分享`
			if (item.isEquipMessage() || item.isNewCreateEquipMessage() || item.isEquipmentMedalShare()) {
				if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0) {
					rect.set(0, 0, (int) (d.getIntrinsicWidth() * 0.8 * ConfigManager.scaleRatio * getScreenCorrectionFactor()),
							(int) (d.getIntrinsicHeight() * 0.8 * ConfigManager.scaleRatio * getScreenCorrectionFactor()));
				} else {
					rect.set(0, 0, (int) (d.getIntrinsicWidth() * 0.8), (int) (d.getIntrinsicHeight() * 0.8));
				}
			}
		}
		return rect;
	}


	private String insertCoordinateLink(String htmlLinkText)
	{
		// (1200|[1][0-1][0-9]{2}|[1-9][0-9]{2}|[1-9][0-9]|[0-9])
		// 逆序可贪婪匹配，遇到不合法数字时可能只匹配合法部分
		// htmlLinkText = htmlLinkText.replaceAll("\\(([0-9]+),([0-9]+)\\)",
		// "<a href='$1,$2'><u>($1,$2)</u></a>");
		htmlLinkText = htmlLinkText.replaceAll(
				"(1200|[1][0-1][0-9]{2}|[1-9][0-9]{2}|[1-9][0-9]|[0-9])(:|：|: |： )(1200|[1][0-1][0-9]{2}|[1-9][0-9]{2}|[1-9][0-9]|[0-9])",
				"<a href='$1,$3'><u> $1:$3 </u></a>");
		return htmlLinkText;
	}

	private TextView	joinAnnounceTextView;
	private MsgItem		joinAnnounceItem;

	public void onJoinAnnounceInvitationSuccess()
	{
		if (joinAnnounceTextView != null && joinAnnounceItem != null)
		{
			if (joinAnnounceItem.hasTranslated)
				setText(joinAnnounceTextView, joinAnnounceItem.currentText, joinAnnounceItem, true);
			else
				setText(joinAnnounceTextView, joinAnnounceItem.currentText, joinAnnounceItem, false);
		}
		joinAnnounceItem = null;
		joinAnnounceTextView = null;
	}

	private void onURLClick(View widget, String url)
	{
		ignoreClick = true;
		final String[] coords = url.split(",");

		if (coords[0].equals(JOIN_NOW_URL))
		{
			try
			{
				joinAnnounceTextView = (TextView) widget;
				MessageViewHolder holder = (MessageViewHolder) widget.getTag();
				if (holder != null)
				{
					joinAnnounceItem = holder.currentMsgItem;

					if (coords.length == 2)
					{
						ServiceInterface.allianceIdJoining = coords[1];
						JniController.getInstance().excuteJNIVoidMethod("joinAnnounceInvitation", new Object[] { coords[1] });
					}
				}

			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
			return;
		}
		else if (coords[0].equals(SHOW_EQUIP_URL))
		{
			try
			{
				if (coords.length == 2 && StringUtils.isNotEmpty(coords[1]))
				{
					ChatServiceController.doHostAction("showEquipment", "", "", coords[1], true);
				}
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
			return;
		}
		else
		{
			if (!isCoordinateValid(coords[0]) || !isCoordinateValid(coords[1]))
			{
				Activity activity = (Activity)c;
				if (activity != null) {
					ServiceInterface.safeMakeText(activity,"coordinate (" + coords[0] + "," + coords[1] + ") is invalid!", Toast.LENGTH_LONG);
				}
				return;
			}
			ChatServiceController.doHostAction("gotoCoordinate", coords[0], coords[1], "", false);
		}
	}

	public class MyURLSpan extends ClickableSpan
	{
		private String	mUrl;

		MyURLSpan(String url)
		{
			mUrl = url;
		}

		@Override
		public void onClick(View widget)
		{
			onURLClick(widget, mUrl);
		}
	}

	// 合法坐标[0,1200]
	public boolean isCoordinateValid(String coord)
	{
		return Integer.parseInt(coord) >= 0 && Integer.parseInt(coord) <= 1200;
	}

	/**
	 * 高ppi手机的缩放修正因子
	 */
	public double getScreenCorrectionFactor()
	{
		int density = c.getResources().getDisplayMetrics().densityDpi;

		if (density >= DisplayMetrics.DENSITY_XXHIGH)
		{
			// 小米note3是640，大于DENSITY_XXHIGH
			return 0.8;
		}
		else
		{
			return 1.0;
		}
	}

	private boolean	ignoreClick	= false;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void copyToClipboard(QuickAction source)
	{
		if (source.currentTextView == null || !(source.currentTextView instanceof TextView))
			return;

		MsgItem item = getMsgItemFromQuickAction(source);
		if (item == null)
			return;
		String text = item.canShowTranslateMsg() ? item.translateMsg : item.msg;

		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB)
		{
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) ((Activity) c)
					.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
		}
		else
		{
			// 一个label对应一个clipboard slot
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ((Activity) c)
					.getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData.newPlainText("cok_" + item.getName() + "_" + item.getSendTime(), text);
			clipboard.setPrimaryClip(clip);
		}
	}

	// 去掉复制后文本中的[obj]块（图片导致）
	// http://stackoverflow.com/questions/8560045/android-getting-obj-using-textview-settextcharactersequence
	private CharSequence stripHtml(String s)
	{
		String result = s.substring(0, s.lastIndexOf("\n"));
		result = result.replaceAll("\n", "#linebreak#");
		result = Html.fromHtml(result).toString().replace((char) 65532, (char) 32);
		result = result.replaceAll("#linebreak#", "\n");
		result = result.trim();
		return result;
	}

	public void destroy()
	{
		c = null;
		inflater = null;
		items = null;

		if (quickAction != null)
		{
			quickAction.currentTextView = null;
			quickAction.setOnActionItemClickListener(null);
		}
		actionClickListener = null;
	}

	@Override
	public int getCount()
	{
		if (items != null)
			return items.size();
		return 0;
	}

	@Override
	public Object getItem(int position)
	{
		if (position >= 0 && position < items.size())
			return items.get(position);
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	public void notifyDataSetChanged() {
		this.items = (ArrayList<MsgItem>)this.itemsBackup.clone();
		super.notifyDataSetChanged();
	}
}
