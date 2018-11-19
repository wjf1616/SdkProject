package com.chatsdk.view.actionbar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class ActionBarHelper
{
	public static final int	NAVIGATION_MODE_STANDARD	= 0;
	protected Activity		mActivity;

	public ActionBarHelper(Activity activity)
	{
		this.mActivity = activity;
	}

	public static ActionBarHelper createInstance(Activity activity)
	{
		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			return new ActionBarHelperNative(activity);
		}
		return new ActionBarHelperBase(activity);
	}

	public void onCreate(Bundle savedInstanceState)
	{
	}

	public void onPostCreate(Bundle savedInstanceState)
	{
	}

	public MenuInflater getMenuInflater(MenuInflater superMenuInflater)
	{
		return superMenuInflater;
	}

	public abstract void setDisplayHomeAsUpEnabled(boolean paramBoolean);

	public abstract void setTitle(String paramString);

	public abstract Context getThemedContext();

	public abstract void supportRequestWindowFeature(int paramInt);

	public abstract void setSupportProgressBarIndeterminateVisibility(boolean paramBoolean);

	public abstract void setNavigationMode(int paramInt);

	public abstract void setOnQueryTextListener(MenuItem paramMenuItem, SimpleMenuItemCompat.QueryTextActions paramQueryTextActions);

	public abstract void setOnActionExpandListener(MenuItem paramMenuItem, SimpleMenuItemCompat.MenuItemActions paramMenuItemActions);

	public abstract void collapseActionView(MenuItem paramMenuItem);

	public void setQueryHint(MenuItem menuItem, String hint)
	{
	}

	public abstract String getQuery(MenuItem paramMenuItem);

	public abstract void clearFocus(MenuItem paramMenuItem);
}