package com.chatsdk.view;

import android.os.Bundle;

import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.view.actionbar.MyActionBarActivity;

public class MemberSelectorActivity extends MyActionBarActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		fragmentClass = MemberSelectorFragment.class;

		ChatServiceController.toggleFullScreen(true, true, this);

		super.onCreate(savedInstanceState);
	}
	
	protected void showBackground()
	{
		fragmentLayout.setBackgroundResource(R.drawable.ui_paper_3c);
	}

	public void onBackButtonClick()
	{
		((MemberSelectorFragment) fragment).onBackButtonClick();
		super.onBackButtonClick();
	}
}
