package com.chatsdk.view.actionbar;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chatsdk.util.ResUtil;

public class ActionBarHelperBase extends ActionBarHelper
{
	private static final String									TAG										= ActionBarHelper.class.getName();
	private static final String									MENU_RES_NAMESPACE						= "http://schemas.android.com/apk/res/android";
	private static final String									MENU_ATTR_ID							= "id";
	private static final String									MENU_ATTR_SHOW_AS_ACTION				= "showAsAction";
	private static final String									MENU_ATTR_ACTION_VIEW_CLASS				= "actionViewClass";
	private static final String									MENU_ATTR_ACTION_LAYOUT					= "actionLayout";
	private Set<Integer>										actionItemIds							= new HashSet();
	private Map<Integer, String>								actionItemIdsToActionViewClassLookup	= new HashMap();
	private Map<Integer, Integer>								actionItemIdsToActionLayoutLookup		= new HashMap();
	private Map<Integer, SimpleMenuItemCompat.QueryTextActions>	queryTextActionsMap						= new HashMap();
	private Map<Integer, SimpleMenuItemCompat.MenuItemActions>	menuItemActionsMap						= new HashMap();
	private LayoutInflater										mInflater;
	private boolean												actionBarInitialised					= false;
	private boolean												progressVisible							= false;
	private ContextThemeWrapper									actionBarThemedContext					= null;
	protected boolean											viewExpanded							= false;

	public ActionBarHelperBase(Activity activity)
	{
		super(activity);
	}

	public void onCreate(Bundle savedInstanceState)
	{
		this.mActivity.requestWindowFeature(7);
		this.mInflater = ((LayoutInflater) this.mActivity.getSystemService("layout_inflater"));
	}

	public void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		int actionbarCompatLayoutId = ResUtil.getId(this.mActivity, "layout", "hs__actionbar_compat");
		this.mActivity.getWindow().setFeatureInt(7, actionbarCompatLayoutId);

		setupActionBar();

		SimpleMenu menu = new SimpleMenu(this.mActivity);
		this.mActivity.onCreatePanelMenu(0, menu);
		this.mActivity.onPrepareOptionsMenu(menu);

		for (int i = 0; i < menu.size(); i++)
		{
			MenuItem menuItem = menu.getItem(i);
			if (this.actionItemIds.contains(Integer.valueOf(menuItem.getItemId())))
			{
				addActionItemCompatFromMenuItem(menuItem);
			}

		}

		for (int i = 0; i < menu.size(); i++)
		{
			MenuItem menuItem = menu.getItem(i);
			if (this.actionItemIds.contains(Integer.valueOf(menuItem.getItemId())))
			{
				addActionItemCompatOnTextListener(menuItem);
				addActionItemCompatExpandListener(menuItem);
			}
		}
	}

	private void setupActionBar()
	{
		ViewGroup actionBarCompat = getActionBarCompat();
		if (actionBarCompat == null)
		{
			return;
		}

		ApplicationInfo appInfo = this.mActivity.getApplicationInfo();

		SimpleMenu tempMenu = new SimpleMenu(this.mActivity);
		SimpleMenuItem homeItem = new SimpleMenuItem(tempMenu, 16908332, 0, appInfo.name);

		homeItem.setIcon(appInfo.icon);
		addActionItemCompatFromMenuItem(homeItem);

		LinearLayout.LayoutParams springLayoutParams = new LinearLayout.LayoutParams(0, -1);
		springLayoutParams.weight = 1.0F;

		TextView titleView = new TextView(this.mActivity, null, ResUtil.getId(this.mActivity, "attr", "cs__actionbarCompatTitleStyle"));
		titleView.setLayoutParams(springLayoutParams);
		titleView.setText(this.mActivity.getTitle());
		titleView.setId(16908310);
		actionBarCompat.addView(titleView);

		SimpleMenuItem progressItem = new SimpleMenuItem(tempMenu, 16908301, 0, appInfo.name);
		addActionItemCompatFromMenuItem(progressItem);

		this.actionBarInitialised = true;
	}

	private void addActionItemCompatFromMenuItem(MenuItem item)
	{
		ViewGroup actionBarCompat = getActionBarCompat();
		if (actionBarCompat == null)
		{
			return;
		}

		View actionView = ((SimpleMenuItem) item).getActionView();
		if (actionView != null)
		{
			actionBarCompat.addView(actionView);
			return;
		}

		switch (item.getItemId())
		{
			case 16908332:
				addHomeActionItem(actionBarCompat, item);
				break;
			case 16908301:
				addProgressActionItem(actionBarCompat);
				break;
			default:
				addActionItem(actionBarCompat, item);
		}
	}

	private void addHomeActionItem(ViewGroup actionBarCompat, final MenuItem item)
	{
		HomeView homeView = (HomeView) this.mInflater.inflate(ResUtil.getId(this.mActivity, "layout", "hs__actionbar_compat_home"),
				actionBarCompat, false);

		if (homeView == null)
		{
			return;
		}

		homeView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				if (viewExpanded)
					collapseActionView(null);
				else
					mActivity.onMenuItemSelected(0, item);
			}
		});
		homeView.setClickable(true);
		homeView.setFocusable(true);
		homeView.setIcon(item.getIcon());
		actionBarCompat.addView(homeView);
	}

	private void addActionItem(ViewGroup actionBarCompat, final MenuItem item)
	{
		int buttonWidthId = ResUtil.getDimenId(this.mActivity, "hs__actionbar_compat_button_width");
		int actionButtonWidth = (int) this.mActivity.getResources().getDimension(buttonWidthId);
		int itemStyleId = ResUtil.getAttrId(this.mActivity, "cs__actionbarCompatItemBaseStyle");

		final ImageButton actionButton = new ImageButton(this.mActivity, null, itemStyleId);
		actionButton.setLayoutParams(new ViewGroup.LayoutParams(actionButtonWidth, -1));

		actionButton.setImageDrawable(item.getIcon());
		actionButton.setScaleType(ImageView.ScaleType.CENTER);
		actionButton.setContentDescription(item.getTitle());
		actionButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				mActivity.onMenuItemSelected(0, item);
			}
		});
		actionBarCompat.addView(actionButton);

		((SimpleMenuItem) item).setOnMenuItemChangedListener(new SimpleMenuItemCompat.MenuItemChangedListener()
		{
			public void visibilityChanged(boolean visible)
			{
				if (visible)
					actionButton.setVisibility(0);
				else
					actionButton.setVisibility(8);
			}
		});
	}

	private void addProgressActionItem(ViewGroup actionBarCompat)
	{
		int progressStyleId = ResUtil.getAttrId(this.mActivity, "cs__actionbarCompatProgressIndicatorStyle");
		int buttonWidthId = ResUtil.getDimenId(this.mActivity, "hs__actionbar_compat_button_width");
		int buttonHeightId = ResUtil.getDimenId(this.mActivity, "hs__actionbar_compat_height");

		ProgressBar indicator = new ProgressBar(this.mActivity, null, progressStyleId);

		int buttonWidth = this.mActivity.getResources().getDimensionPixelSize(buttonWidthId);
		int buttonHeight = this.mActivity.getResources().getDimensionPixelSize(buttonHeightId);
		int progressIndicatorWidth = buttonWidth / 2;

		LinearLayout.LayoutParams indicatorLayoutParams = new LinearLayout.LayoutParams(progressIndicatorWidth, progressIndicatorWidth);

		indicatorLayoutParams.setMargins((buttonWidth - progressIndicatorWidth) / 2, (buttonHeight - progressIndicatorWidth) / 2,
				(buttonWidth - progressIndicatorWidth) / 2, 0);

		indicator.setLayoutParams(indicatorLayoutParams);
		if (this.progressVisible)
			indicator.setVisibility(0);
		else
		{
			indicator.setVisibility(8);
		}
		indicator.setId(ResUtil.getId(this.mActivity, "id", "hs__actionbar_compat_item_refresh_progress"));
		actionBarCompat.addView(indicator);
	}

	public ViewGroup getActionBarCompat()
	{
		int actionbarCompatViewGroupId = ResUtil.getId(this.mActivity, "id", "hs__actionbar_compat");
		return (ViewGroup) this.mActivity.findViewById(actionbarCompatViewGroupId);
	}

	public MenuInflater getMenuInflater(MenuInflater superMenuInflater)
	{
		return new WrappedMenuInflater(this.mActivity, superMenuInflater);
	}

	public void setDisplayHomeAsUpEnabled(boolean b)
	{
	}

	public void setTitle(String title)
	{
		this.mActivity.setTitle(title);
		if (this.actionBarInitialised)
		{
			TextView titleText = (TextView) getActionBarCompat().findViewById(16908310);
			if (titleText != null)
				titleText.setText(title);
		}
	}

	public Context getThemedContext()
	{
		if (this.actionBarThemedContext == null)
		{
			this.actionBarThemedContext = new ContextThemeWrapper(this.mActivity, ResUtil.getId(this.mActivity, "style",
					"HSActionBarThemedContext"));
		}

		return this.actionBarThemedContext;
	}

	public void supportRequestWindowFeature(int featureId)
	{
	}

	public void setSupportProgressBarIndeterminateVisibility(boolean visible)
	{
		if (this.actionBarInitialised)
		{
			View progress = getActionBarCompat().findViewById(
					ResUtil.getId(this.mActivity, "id", "hs__actionbar_compat_item_refresh_progress"));
			if (visible)
				progress.setVisibility(0);
			else
			{
				progress.setVisibility(8);
			}
		}
		this.progressVisible = visible;
	}

	public void setNavigationMode(int navigationMode)
	{
	}

	public void setOnQueryTextListener(MenuItem menuItem, SimpleMenuItemCompat.QueryTextActions queryTextActions)
	{
		this.queryTextActionsMap.put(Integer.valueOf(menuItem.getItemId()), queryTextActions);
	}

	public void setOnActionExpandListener(MenuItem menuItem, SimpleMenuItemCompat.MenuItemActions menuItemActions)
	{
		this.menuItemActionsMap.put(Integer.valueOf(menuItem.getItemId()), menuItemActions);
	}

	private void addActionItemCompatExpandListener(MenuItem menuItem)
	{
		int menuItemId = menuItem.getItemId();
		int searchId = ResUtil.getId(this.mActivity, "id", "hs__action_search");
		int conversationId = ResUtil.getId(this.mActivity, "id", "hs__action_report_issue");

		final View title = getActionBarCompat().findViewById(16908310);
		final View conversation = getActionBarCompat().findViewById(conversationId);

		if ((this.menuItemActionsMap.containsKey(Integer.valueOf(menuItemId))) && (menuItemId == searchId))
		{
			final SimpleMenuItemCompat.MenuItemActions itemActions = (SimpleMenuItemCompat.MenuItemActions) this.menuItemActionsMap
					.get(Integer.valueOf(menuItemId));
			SimpleSearchView simpleSearchView = (SimpleSearchView) getActionBarCompat().findViewById(searchId);
			simpleSearchView.setOnActionExpandListener(new MenuItemCompat.OnActionExpandListener()
			{
				public boolean onMenuItemActionExpand(MenuItem item)
				{
					title.setVisibility(8);
					if (conversation != null)
					{
						conversation.setVisibility(8);
					}
					viewExpanded = true;
					return itemActions.menuItemExpanded();
				}

				public boolean onMenuItemActionCollapse(MenuItem item)
				{
					title.setVisibility(0);
					if (conversation != null)
					{
						conversation.setVisibility(0);
					}
					viewExpanded = false;
					return itemActions.menuItemCollapsed();
				}
			});
		}
	}

	protected boolean isViewExpanded()
	{
		return this.viewExpanded;
	}

	private void addActionItemCompatOnTextListener(MenuItem menuItem)
	{
		int menuItemId = menuItem.getItemId();
		int searchId = ResUtil.getId(this.mActivity, "id", "hs__action_search");

		if ((this.queryTextActionsMap.containsKey(Integer.valueOf(menuItemId))) && (menuItemId == searchId))
		{
			final SimpleMenuItemCompat.QueryTextActions queryTextActions = (SimpleMenuItemCompat.QueryTextActions) this.queryTextActionsMap
					.get(Integer.valueOf(menuItemId));
			SimpleSearchView simpleSearchView = (SimpleSearchView) getActionBarCompat().findViewById(searchId);
			simpleSearchView.setQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextSubmit(String query) {
					return queryTextActions.queryTextSubmitted(query);
				}

				@Override
				public boolean onQueryTextChange(String newText) {
					return queryTextActions.queryTextChanged(newText);
				}
			});
		}
	}

	public void collapseActionView(MenuItem menuItem)
	{
		int searchId = ResUtil.getId(this.mActivity, "id", "hs__action_search");
		SimpleSearchView simpleSearchView = (SimpleSearchView) getActionBarCompat().findViewById(searchId);
		simpleSearchView.collapseActionView();
	}

	public String getQuery(MenuItem menuItem)
	{
		int menuItemId = menuItem.getItemId();
		int searchId = ResUtil.getId(this.mActivity, "id", "hs__action_search");

		String query = "";

		if (menuItemId == searchId)
		{
			SimpleSearchView simpleSearchView = (SimpleSearchView) getActionBarCompat().findViewById(searchId);
			query = simpleSearchView.getQuery();
		}

		return query;
	}

	public void clearFocus(MenuItem menuItem)
	{
		int menuItemId = menuItem.getItemId();
		int searchId = ResUtil.getId(this.mActivity, "id", "hs__action_search");

		if (menuItemId == searchId)
		{
			SimpleSearchView simpleSearchView = (SimpleSearchView) getActionBarCompat().findViewById(searchId);
			simpleSearchView.clearFocus();
		}
	}

	private static class HomeView extends LinearLayout
	{
		private ImageView	mIconView;
		private Context		mContext;

		public HomeView(Context context)
		{
			super(context);
			this.mContext = context;
		}

		public HomeView(Context context, AttributeSet attrs)
		{
			super(context, attrs);
			this.mContext = context;
		}

		public void setIcon(Drawable icon)
		{
			this.mIconView.setImageDrawable(icon);
		}

		protected void onFinishInflate()
		{
			super.onFinishInflate();
			this.mIconView = ((ImageView) findViewById(ResUtil.getId(this.mContext, "id", "hs__actionbar_compat_home")));
		}
	}

	private class WrappedMenuInflater extends MenuInflater
	{
		MenuInflater	inflater;

		public WrappedMenuInflater(Context context, MenuInflater superMenuInflater)
		{
			super(context);
			this.inflater = superMenuInflater;
		}

		public void inflate(int menuRes, Menu menu)
		{
			loadActionBarMetadata(menuRes);
			this.inflater.inflate(menuRes, menu);

			for (int i = 0; i < menu.size(); i++)
				try
				{
					SimpleMenuItem item = (SimpleMenuItem) menu.getItem(i);
					int itemId = item.getItemId();
					if (actionItemIdsToActionViewClassLookup.containsKey(Integer.valueOf(itemId)))
					{
						String viewClass = (String) actionItemIdsToActionViewClassLookup.get(Integer.valueOf(itemId));
						if (viewClass.equals("android.widget.SearchView"))
						{
							SimpleSearchView searchView = (SimpleSearchView) mInflater.inflate(
									ResUtil.getId(mActivity, "layout", "hs__simple_search_view"), null);

							searchView.setId(itemId);
							item.setActionView(searchView);
						}
					}

					if (actionItemIdsToActionLayoutLookup.containsKey(Integer.valueOf(item.getItemId())))
					{
						View view = mInflater.inflate(
								((Integer) actionItemIdsToActionLayoutLookup.get(Integer.valueOf(item.getItemId()))).intValue(), null);
						view.setId(itemId);
						item.setActionView(view);
					}
				}
				catch (ClassCastException e)
				{
					Log.v(ActionBarHelperBase.TAG, "ClassCastException on hardware menu button click", e);
				}
		}

		private void loadActionBarMetadata(int menuRes)
		{
			XmlResourceParser parser = null;
			try
			{
				parser = mActivity.getResources().getXml(menuRes);

				int eventType = parser.getEventType();
				boolean eof = false;

				while (!eof)
				{
					switch (eventType)
					{
						case 2:
							if (parser.getName().equals("item"))
							{
								int itemId = parser.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "id", 0);

								if (itemId != 0)
								{
									int showAsAction = parser.getAttributeIntValue("http://schemas.android.com/apk/res/android",
											"showAsAction", -1);

									if (((showAsAction & 0x2) != 0) || ((showAsAction & 0x1) != 0))
									{
										int actionLayout = parser.getAttributeResourceValue("http://schemas.android.com/apk/res/android",
												"actionLayout", 0);
										if (actionLayout != 0)
										{
											actionItemIdsToActionLayoutLookup.put(Integer.valueOf(itemId), Integer.valueOf(actionLayout));
										}
										actionItemIds.add(Integer.valueOf(itemId));
									}

									String actionViewClass = parser.getAttributeValue("http://schemas.android.com/apk/res/android",
											"actionViewClass");

									if (actionViewClass != null)
										actionItemIdsToActionViewClassLookup.put(Integer.valueOf(itemId), actionViewClass);
								}
							}
							break;
						case 1:
							eof = true;
					}

					eventType = parser.next();
				}
			}
			catch (XmlPullParserException e)
			{
				throw new InflateException("Error inflating menu XML", e);
			}
			catch (IOException e)
			{
				throw new InflateException("Error inflating menu XML", e);
			}
			finally
			{
				if (parser != null)
					parser.close();
			}
		}
	}
}