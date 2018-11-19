package com.chatsdk.view.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.controller.ServiceInterface;
import com.chatsdk.model.ChannelListItem;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.TimeManager;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.FilterWordsManager;
import com.chatsdk.util.HtmlTextUtil;
import com.chatsdk.util.ScaleUtil;
import com.chatsdk.util.animation.SwingAnimation;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class MailViewHolder extends CategoryViewHolder
	{
	public TextView		item_latest_msg,item_latest_msg_top;
	public TextView		item_time;
	public TextView     item_fail_time;
	public LinearLayout	item_icon_layout;
	public ImageView	item_lock_icon;
	public ImageView	item_reward_icon;
	public LinearLayout	contentLinearLayout;

	public MailViewHolder(View view)
	{
		super(view);

		item_latest_msg = (TextView) view.findViewById(R.id.channel_latest_msg);
		item_latest_msg_top = (TextView) view.findViewById(R.id.channel_latest_msg_top);
		item_time = (TextView) view.findViewById(R.id.channel_item_time);
		item_fail_time = (TextView) view.findViewById(R.id.channel_fail_time);
		item_icon_layout = (LinearLayout) view.findViewById(R.id.channel_icon_layout);
		item_lock_icon = (ImageView) view.findViewById(R.id.channel_lock_icon);
		item_reward_icon = (ImageView) view.findViewById(R.id.channel_reward_icon);
		contentLinearLayout = (LinearLayout) view.findViewById(R.id.content_linear_layout);

	}

	public void setContent(final Context context, ChannelListItem item, boolean showUreadAsText, Drawable drawable, String title, final String summary,
			long time, boolean isInEditMode, int position, int bgColor,long failTime)
	{
		super.setContent(context, item, showUreadAsText, drawable, title, summary, time, isInEditMode, position, bgColor, failTime);//隐藏了 调用了showIcon

		if (StringUtils.isNotEmpty(summary) && summary.contains(".png"))
		{
			HtmlTextUtil.setResourceHtmlText(item_latest_msg, summary);
		}
		else
		{
			if(ChannelManager.getInstance().currChoseChannel!=null&&ChannelManager.getInstance().currChoseChannel.channelID!=null&&
			ChannelManager.getInstance().currChoseChannel.channelID.equals(MailManager.CHANNELID_FIGHT)&&summary.contains("\n")){//add at 20171121 for 战斗报告页面的分行处理
				item_latest_msg_top.setVisibility(View.VISIBLE);
				item_latest_msg_top.setText(summary.substring(0,summary.lastIndexOf("\n")).trim());
				item_latest_msg.setText(summary.substring(summary.lastIndexOf("\n")).trim());
				item_latest_msg.setMaxLines(1);
			}else {
				if(ChatServiceController.isNeedReplaceBadWords() && ChannelManager.getInstance().currChoseChannel!=null&&ChannelManager.getInstance().currChoseChannel.channelID!=null&&
						ChannelManager.getInstance().currChoseChannel.channelID.equals(MailManager.CHANNELID_MESSAGE)) {
					ChatServiceController.services.execute(new Runnable() {
						@Override
						public void run() {
							final String summaryText = FilterWordsManager.replaceSensitiveWord(summary, 1, "*");
							ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									item_latest_msg_top.setVisibility(View.GONE);
									item_latest_msg.setMaxLines(context.getResources().getInteger(R.integer.cs__textSummaryMaxLine));
									item_latest_msg.setText(summaryText);
								}
							});
						}
					});
				}else {
					item_latest_msg_top.setVisibility(View.GONE);
					item_latest_msg.setMaxLines(context.getResources().getInteger(R.integer.cs__textSummaryMaxLine));
					item_latest_msg.setText(summary);
				}

			}
		}
		item_time.setText(TimeManager.getReadableTime(time));
		item_fail_time.setText(TimeManager.getTimeFormatWithFailTime(failTime));
		if(failTime==0){
			item_fail_time.setVisibility(View.INVISIBLE);
		}else{
			item_fail_time.setVisibility(View.VISIBLE);
		}
		if (bgColor != 0)
		{
			GradientDrawable bgShape = (GradientDrawable) item_icon.getBackground();
			bgShape.setColor(bgColor);
		}
	}

	protected void setChatRoomColor(){
		item_title.setTextColor(Color.rgb(213, 220, 168));
		item_latest_msg.setTextColor(Color.rgb(200, 200, 200));
		item_time.setTextColor(Color.rgb(200, 200, 200));
	}

	protected void showUnreadCountText(Context context)
	{
		unread_text.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);

		if (unreadCount <= 0)
			return;

		if (!showUreadAsText)
		{
			unread_text.setText("");
			unread_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, 1);
			unread_text.setBackgroundResource(R.drawable.channel_red_dot);
		}
		else
		{
			unread_text.setText(Integer.toString(unreadCount));
			Resources r = context.getResources();
			unread_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, r.getDimension(R.dimen.cs__textSummary));
			unread_text.setBackgroundDrawable(getDefaultBackground(context));
		}
	}

	protected void showIcon(boolean isLock, boolean reward,final ChannelListItem item, boolean isInEditMode)
	{
		item_lock_icon.setVisibility(isLock ? View.VISIBLE : View.GONE);
		//item_reward_icon.setVisibility(reward ? View.VISIBLE : View.GONE);

		if(reward){
			//add at 20171102
			item_reward_icon.setVisibility(View.VISIBLE);
//			SwingAnimation giftAnimation = new SwingAnimation(0f,-7f,7f, Animation.RELATIVE_TO_SELF,
//				0.5f,Animation.RELATIVE_TO_SELF,0.5f);
//			giftAnimation.setDuration(800);//设置动画持续时间
//			giftAnimation.setRepeatCount(-1);//设置重复次数
//			item_reward_icon.startAnimation(giftAnimation);
		}else {
			item_reward_icon.clearAnimation();
			item_reward_icon.setVisibility(View.GONE);
		}

		item_reward_icon.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				readAndRewardMail(item);
			}
		});

		if (isInEditMode)
		{
			adjustContentLinearLayout(false);
		}
		else if (!enableAnimation)
		{
			adjustContentLinearLayout(true);
		}
		super.showIcon(isLock, reward, item, isInEditMode);
	}


		private void adjustContentLinearLayout(boolean isExpand)
	{
		// 旧版UI没有contentLinearLayout
		if (contentLinearLayout == null)
			return;

		if (isExpand)
		{
			LinearLayout.LayoutParams lay = (LinearLayout.LayoutParams) contentLinearLayout.getLayoutParams();
			lay.weight = 504;
		}
		else
		{
			LinearLayout.LayoutParams lay = (LinearLayout.LayoutParams) contentLinearLayout.getLayoutParams();
			// 多了item_checkbox_layout，少了item_leading_space
			lay.weight = 504 - 64 + 16;
		}
	}

	protected void onHideAnimationEnd()
	{
		adjustContentLinearLayout(true);

		super.onHideAnimationEnd();
	}

	protected void adjustSizeExtend(Context context)
	{
		int muteIconWidthDP = 15;
		LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams((int) (ScaleUtil.dip2px(context, muteIconWidthDP)
				* ConfigManager.scaleRatio * getScreenCorrectionFactor(context)), (int) (ScaleUtil.dip2px(context, muteIconWidthDP)
				* ConfigManager.scaleRatio * getScreenCorrectionFactor(context)));
		param1.setMargins(0, 0, 3, 0);
		item_reward_icon.setLayoutParams(param1);

		item_lock_icon.setLayoutParams(param1);
	}

	protected void finalize()
	{
		item_latest_msg = null;
		item_time = null;
		item_fail_time = null;
		item_icon_layout = null;
		item_lock_icon = null;
		item_reward_icon = null;
		contentLinearLayout = null;
	}


		/**
		 * @param item 对该条目进行领取并已读操作
		 */
	private void readAndRewardMail(ChannelListItem item) {
		MailData mailData = (MailData)item;
		if (mailData != null && mailData.channel != null && mailData.channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if (mailData.hasReward())
			{
				String uids = mailData.getUid();
				if (!(uids.equals("")))
				{
					JniController.getInstance().excuteJNIVoidMethod("rewardMutiMail", new Object[] { uids, "", false });
					ServiceInterface.setMutiMailRewardStatus(uids);//surprise
					if(item_reward_icon!=null){
						item_reward_icon.clearAnimation();
						item_reward_icon.setVisibility(View.GONE);
					}
				}
			}

			if (mailData.isUnread())
			{
				String readUids = mailData.getUid();
				if(!(readUids.equals(""))){
					JniController.getInstance().excuteJNIVoidMethod("readMutiMail", new Object[] { readUids });
				}
			}
		}

	}

	private String appendStr(String originStr, String appendStr)
	{
		String ret = originStr;
		if (StringUtils.isNotEmpty(appendStr) && !ret.contains(appendStr))
		{
			if (ret.equals(""))
				ret = appendStr;
			else
				ret += "," + appendStr;
		}
		return ret;
	}
}
