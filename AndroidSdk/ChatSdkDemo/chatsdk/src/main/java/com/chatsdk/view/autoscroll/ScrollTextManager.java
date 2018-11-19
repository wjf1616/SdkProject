package com.chatsdk.view.autoscroll;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chatsdk.model.MsgItem;

public class ScrollTextManager
{
	private static ScrollTextManager	_instance			= null;
	private List<MsgItem>				mScrollQueue		= null;
	private TextView					mHornNameTextView	= null;
	private RelativeLayout				horn_layout			= null;

	private ScrollTextManager()
	{
		mScrollQueue = new ArrayList<MsgItem>();
	}

	public void setHornLayout(RelativeLayout layout, TextView view)
	{
		horn_layout = layout;
		mHornNameTextView = view;
	}

	public static ScrollTextManager getInstance()
	{
		if (_instance == null)
		{
			synchronized (ScrollTextManager.class)
			{
				if (_instance == null)
				{
					_instance = new ScrollTextManager();
				}
			}
		}
		return _instance;
	}

	public void showScrollText(MsgItem msgItem, ScrollText scrollTextView, TextView hornNameTextView, RelativeLayout layout)
	{
		setHornLayout(layout, hornNameTextView);
		if (mScrollQueue != null)
		{
			mScrollQueue.add(msgItem);
			scrollTextView.scrollNext();
		}
	}

	public int getScrollQueueLength()
	{
		if (mScrollQueue != null)
			return mScrollQueue.size();
		return 0;
	}

	public MsgItem getNextText()
	{
		if (mScrollQueue != null && !mScrollQueue.isEmpty())
		{
			return mScrollQueue.get(0);
		}
		return null;
	}

	public void pop()
	{
		if (mScrollQueue != null && !mScrollQueue.isEmpty() && mScrollQueue.size() > 1)
		{
			mScrollQueue.remove(0);
		}
	}

	public void clear()
	{
		if (mScrollQueue != null && !mScrollQueue.isEmpty())
		{
			mScrollQueue.clear();
		}
	}

	public void push(MsgItem msgItem)
	{
		if (mScrollQueue != null && !mScrollQueue.contains(msgItem))
			mScrollQueue.add(msgItem);
	}

	public void shutDownScrollText(ScrollText scrollTextView)
	{
		if (mScrollQueue != null && !mScrollQueue.isEmpty())
		{
			mScrollQueue.clear();
		}
		scrollTextView.stopScroll();
	}

	public void hideScrollLayout()
	{
		if (horn_layout != null)
		{
			horn_layout.setVisibility(View.GONE);
		}

	}

	public void setHornName(String name)
	{
		if (mHornNameTextView != null)
		{
			mHornNameTextView.setText(name);
		}
	}
}
