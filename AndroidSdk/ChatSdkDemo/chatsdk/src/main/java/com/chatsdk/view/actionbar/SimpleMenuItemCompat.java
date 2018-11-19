package com.chatsdk.view.actionbar;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Build.VERSION;
import android.support.v4.view.MenuItemCompat;
import android.view.MenuItem;
import android.view.View;

@TargetApi(14)
public class SimpleMenuItemCompat
{
	private static boolean	isAboveICS	= Build.VERSION.SDK_INT >= 14;

	public static View getActionView(MenuItem menuItem)
	{
		if ((menuItem instanceof SimpleMenuItem))
		{
			return ((SimpleMenuItem) menuItem).getActionView();
		}
		return MenuItemCompat.getActionView(menuItem);
	}

	public static abstract interface MenuItemActions
	{
		public abstract boolean menuItemExpanded();

		public abstract boolean menuItemCollapsed();
	}

	public static abstract interface QueryTextActions
	{
		public abstract boolean queryTextSubmitted(String paramString);

		public abstract boolean queryTextChanged(String paramString);
	}

	public static abstract interface MenuItemChangedListener
	{
		public abstract void visibilityChanged(boolean paramBoolean);
	}
}