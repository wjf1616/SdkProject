package com.chatsdk.view.actionbar;

import android.content.Context;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.chatsdk.util.ResUtil;

public class SimpleSearchView extends LinearLayout
{
	private Context										mContext;
	private SearchView.OnQueryTextListener	mQueryTextListener;
	private MenuItemCompat.OnActionExpandListener		mOnActionExpandListener;
	private EditText									searchQuery;
	private ImageButton									searchButton;
	private ImageButton									clearButton;
	private InputMethodManager							imm;

	public SimpleSearchView(Context context)
	{
		super(context);
		this.mContext = context;
		this.imm = ((InputMethodManager) this.mContext.getSystemService("input_method"));
	}

	public SimpleSearchView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.mContext = context;
		this.imm = ((InputMethodManager) this.mContext.getSystemService("input_method"));
	}

	protected void onFinishInflate()
	{
		super.onFinishInflate();
		this.searchQuery = ((EditText) findViewById(ResUtil.getId(this.mContext, "id", "hs__search_query")));
		this.searchButton = ((ImageButton) findViewById(ResUtil.getId(this.mContext, "id", "hs__search_button")));
		this.clearButton = ((ImageButton) findViewById(ResUtil.getId(this.mContext, "id", "hs__search_query_clear")));

		this.searchButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				menuItemExpanded();
			}
		});
		this.searchQuery.addTextChangedListener(new TextWatcher()
		{
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
			{
			}

			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
			{
				if (charSequence.length() > 0)
					clearButton.setVisibility(0);
				else
				{
					clearButton.setVisibility(8);
				}
				mQueryTextListener.onQueryTextChange(charSequence.toString());
			}

			public void afterTextChanged(Editable editable)
			{
			}
		});
		this.clearButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				searchQuery.setText("");
				showKeyBoard();
			}
		});
	}

	private void menuItemExpanded()
	{
		showKeyBoard();
		this.searchQuery.setVisibility(0);
		this.searchButton.setVisibility(8);
		this.searchQuery.requestFocus();
		mOnActionExpandListener.onMenuItemActionExpand(null);
	}

	private void showKeyBoard()
	{
		this.searchQuery.requestFocus();

		this.searchQuery.postDelayed(new Runnable()
		{
			public void run()
			{
				imm.showSoftInput(searchQuery, 0);
			}
		}, 200L);
	}

	private void hideKeyboard()
	{
		this.searchQuery.clearFocus();
		this.imm.hideSoftInputFromWindow(getWindowToken(), 0);
	}

	public void setQueryTextListener(SearchView.OnQueryTextListener queryTextListener)
	{
		this.mQueryTextListener = queryTextListener;
	}

	public void setOnActionExpandListener(MenuItemCompat.OnActionExpandListener onActionExpandListener)
	{
		this.mOnActionExpandListener = onActionExpandListener;
	}

	public void collapseActionView()
	{
		hideKeyboard();
		this.searchQuery.setVisibility(8);
		this.clearButton.setVisibility(8);
		this.searchButton.setVisibility(0);
		this.searchQuery.setText("");
		this.mOnActionExpandListener.onMenuItemActionCollapse(null);
	}

	public String getQuery()
	{
		return this.searchQuery.getText().toString();
	}

	public void clearFocus()
	{
		hideKeyboard();
	}
}