package com.chatsdk.util;

import com.nineoldandroids.view.ViewHelper;

import android.view.View;

public class CompatibleApiUtil
{
	private static CompatibleApiUtil	instance;

	public static CompatibleApiUtil getInstance()
	{
		if (instance == null)
		{
			instance = new CompatibleApiUtil();
		}
		return instance;
	}

	private CompatibleApiUtil()
	{

	}

	public void setButtonAlpha(View button, boolean enabled)
	{
		float alpha = enabled ? 1.0f : 0.4f;
		ViewHelper.setAlpha(button, alpha);

		// HS的实现方法
		// addReply.setAlpha(64);
		// HSIcons.applyTextColorPrimaryFilter(activity,
		// addReply.getDrawable());
		// addReply.setAlpha(255);
		// HSIcons.applyAttachFilter(activity, addReply.getDrawable());
	}
}
