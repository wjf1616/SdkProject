package com.chatsdk.view.actionbar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.support.v4.view.MenuItemCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

@TargetApi(14)
public class ActionBarHelperNative extends ActionBarHelper
{
	public ActionBarHelperNative(Activity activity)
	{
		super(activity);
	}

	public void setDisplayHomeAsUpEnabled(boolean b)
	{
		this.mActivity.getActionBar().setDisplayHomeAsUpEnabled(b);
	}

	public void setTitle(String title)
	{
		this.mActivity.getActionBar().setTitle(title);
	}

	public Context getThemedContext()
	{
		return this.mActivity.getActionBar().getThemedContext();
	}

	public void supportRequestWindowFeature(int featureId)
	{
		this.mActivity.requestWindowFeature(featureId);
	}

	public void setSupportProgressBarIndeterminateVisibility(boolean visible)
	{
		this.mActivity.setProgressBarIndeterminateVisibility(visible);
	}

	public void setNavigationMode(int navigationMode)
	{
		this.mActivity.getActionBar().setNavigationMode(navigationMode);
	}

	public void setOnQueryTextListener(MenuItem menuItem, final SimpleMenuItemCompat.QueryTextActions queryTextActions)
	{
		View actionView = menuItem.getActionView();

		if ((actionView instanceof SearchView))
			((SearchView) actionView).setOnQueryTextListener(new SearchView.OnQueryTextListener()
			{
				public boolean onQueryTextSubmit(String query)
				{
					return queryTextActions.queryTextSubmitted(query);
				}

				public boolean onQueryTextChange(String newText)
				{
					return queryTextActions.queryTextChanged(newText);
				}
			});
	}

	public void setOnActionExpandListener(MenuItem menuItem, final SimpleMenuItemCompat.MenuItemActions itemActions)
	{
		menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener()
		{
			public boolean onMenuItemActionExpand(MenuItem item)
			{
				return itemActions.menuItemExpanded();
			}

			public boolean onMenuItemActionCollapse(MenuItem item)
			{
				return itemActions.menuItemCollapsed();
			}
		});
	}

	public void collapseActionView(MenuItem menuItem)
	{
		View actionView = menuItem.getActionView();

		if ((actionView instanceof SearchView))
			MenuItemCompat.collapseActionView(menuItem);
	}

	public void setQueryHint(MenuItem menuItem, String hint)
	{
		View actionView = menuItem.getActionView();

		if ((actionView instanceof SearchView))
			((SearchView) actionView).setQueryHint(hint);
	}

	public String getQuery(MenuItem menuItem)
	{
		View actionView = menuItem.getActionView();
		String query = "";

		if ((actionView instanceof SearchView))
		{
			query = ((SearchView) actionView).getQuery().toString();
		}

		return query;
	}

	public void clearFocus(MenuItem menuItem)
	{
		View actionView = menuItem.getActionView();

		if (actionView != null)
			menuItem.getActionView().clearFocus();
	}
}