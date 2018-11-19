package com.chatsdk.view.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.model.ChannelListItem;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.ScaleUtil;

public class CategoryViewHolder
{
	public LinearLayout		item_checkbox_layout;
	public LinearLayout		item_divider_title_layout;
	public View				item_leading_space;
	public RelativeLayout	item_pic_layout;
	public ImageView		item_icon;
	public TextView			item_title;

	public TextView			unread_text;
	public int				unreadCount;

	public CheckBox			item_checkBox;
	public boolean			adjustSizeCompleted	= false;
	public boolean			showUreadAsText;
	public TextView	        Report_text;

	public CategoryViewHolder(View view)
	{
		item_leading_space = view.findViewById(R.id.channel_leading_space);
		item_checkbox_layout = (LinearLayout) view.findViewById(R.id.channel_item_checkbox_layout);
		item_divider_title_layout = (LinearLayout) view.findViewById(R.id.divider_title_layout);
		unread_text = (TextView) view.findViewById(R.id.channel_unread_count);
		item_icon = (ImageView) view.findViewById(R.id.channel_icon);
		item_title = (TextView) view.findViewById(R.id.channel_name);
		item_checkBox = (CheckBox) view.findViewById(R.id.channel_checkBox);
		item_checkBox.setClickable(false);
		item_checkBox.setFocusable(false);
		Report_text = (TextView) view.findViewById(R.id.divideText);
		if(Report_text!=null){
			Report_text.setText(LanguageManager.getLangByKey("105568"));}
	}

	public void setContent(Context context, ChannelListItem item, boolean showUreadAsText, Drawable drawable, String title, String summary,
			long time, boolean isInEditMode, int position, int bgColor,long failTime)
	{
		if (item instanceof MailData)
		{
			this.unreadCount = item.isUnread() ? 1 : 0;
		}
		else
		{
			this.unreadCount = item.unreadCount;
		}
		this.showUreadAsText = showUreadAsText;

		showUnreadCountText(context);

		if (drawable != null)
		{
			item_icon.setImageDrawable(drawable);
		}

		if (item_divider_title_layout != null && item instanceof ChatChannel && (((ChatChannel) item).isDialogChannel() || ((ChatChannel) item).channelID.equals(MailManager.CHANNELID_MOD))
				&& ((ChatChannel) item).channelID.equals(ChannelManager.getInstance().getFirstChannelID()))
		{
			item_divider_title_layout.setVisibility(View.VISIBLE);
		}
		else if (item_divider_title_layout != null)
		{
			item_divider_title_layout.setVisibility(View.GONE);
		}

		item_title.setText(title);
		
		showIcon(item.isLock(), item.hasReward(), item, isInEditMode);
	}

	private boolean			enteredEditMode	= false;
	protected final boolean	enableAnimation	= false;

	protected void showIcon(boolean isLock, boolean reward, ChannelListItem item, boolean isInEditMode)
	{
		if (isInEditMode)
		{
			// 新版UI才有item_checkbox_layout
			if (item_checkbox_layout != null)
			{
				item_checkbox_layout.setVisibility(View.VISIBLE);
				item_leading_space.setVisibility(View.GONE);
				item_checkBox.setChecked(item.checked);

				// 若不判断enteredEditMode，上下滑时，边界外的cell都将再放一次滑入效果
				if (enableAnimation && !enteredEditMode)
				{
					playShowAnimation();
				}
			}
			else
			{
				item_checkBox.setVisibility(View.VISIBLE);
				item_checkBox.setChecked(item.checked);
			}
		}
		else
		{
			if (item_checkbox_layout != null)
			{
				if (!enteredEditMode)
				{
					hideCheckboxLayout();
				}
				else if (enableAnimation)
				{
					playHideAnimation();
				}
			}
			else
			{
				item_checkBox.setVisibility(View.GONE);
			}
		}
	}

	protected void setChatRoomColor(){
	}

	private void playShowAnimation()
	{
		float delta = item_icon.getWidth() / 2;
		int time = 600;

		enteredEditMode = true;

		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, -delta, Animation.ABSOLUTE, 0f,
				Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, 0f);
		translateAnimation.setDuration(time);
		animationSet.addAnimation(translateAnimation);
		item_checkBox.startAnimation(animationSet);
	}

	private void playHideAnimation()
	{
		float delta = item_icon.getWidth() / 2;
		int time = 600;

		enteredEditMode = false;
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, -delta,
				Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, 0f);
		translateAnimation.setDuration(time);
		animationSet.addAnimation(translateAnimation);
		translateAnimation.setAnimationListener(new AnimationListener()
		{
			public void onAnimationStart(Animation animation)
			{
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				onHideAnimationEnd();
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}
		});
		item_checkBox.startAnimation(animationSet);
	}

	private void hideCheckboxLayout()
	{
		item_checkbox_layout.setVisibility(View.GONE);
		item_leading_space.setVisibility(View.VISIBLE);
	}

	protected void onHideAnimationEnd()
	{
		hideCheckboxLayout();
	}

	protected void showUnreadCountText(Context context)
	{
		unread_text.setVisibility(View.VISIBLE);
		String unread = Integer.toString(unreadCount);
		if (unreadCount > 99)
		{
			unread = "99+";
		}
		else if (unreadCount <= 0)
		{
			unread = "";

		}
		unread_text.setText(unread);
	}

	private static final int	DEFAULT_CORNER_RADIUS_DIP	= 8;
	private final int			DEFAULT_BADGE_COLOR			= Color.parseColor("#CCFF0000");

	protected ShapeDrawable getDefaultBackground(Context context)
	{

		int r = dipToPixels(DEFAULT_CORNER_RADIUS_DIP, context);
		float[] outerR = new float[] { r, r, r, r, r, r, r, r };

		RoundRectShape rr = new RoundRectShape(outerR, null, null);
		ShapeDrawable drawable = new ShapeDrawable(rr);
		drawable.getPaint().setColor(DEFAULT_BADGE_COLOR);

		return drawable;
	}

	private int dipToPixels(int dip, Context context)
	{
		Resources r = context.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
		return (int) px;
	}

	private void adjustTextSize()
	{
		// ScaleUtil.adjustTextSize(item_title, ConfigManager.scaleRatio *
		// getFontScreenCorrectionFactor());
		// ScaleUtil.adjustTextSize(item_latest_msg, ConfigManager.scaleRatio *
		// getFontScreenCorrectionFactor());
		// ScaleUtil.adjustTextSize(item_time, ConfigManager.scaleRatio *
		// getFontScreenCorrectionFactor());
	}

	protected void adjustSizeExtend(Context context)
	{
	}

	/**
	 * 高ppi手机的缩放修正因子
	 */
	protected double getScreenCorrectionFactor(Context context)
	{
		int density = context.getResources().getDisplayMetrics().densityDpi;

		return density == DisplayMetrics.DENSITY_XXHIGH ? 0.8 : 1.0;
	}

	protected double getFontScreenCorrectionFactor(Context context)
	{
		int density = context.getResources().getDisplayMetrics().densityDpi;

		return density >= DisplayMetrics.DENSITY_XHIGH ? 0.8 : 1.0;
	}

	protected void finalize()
	{
		item_checkbox_layout = null;
		item_divider_title_layout = null;
		item_leading_space = null;
		item_pic_layout = null;
		item_icon = null;
		item_title = null;
		unread_text = null;
		item_checkBox = null;
		Report_text = null;
	}
}
