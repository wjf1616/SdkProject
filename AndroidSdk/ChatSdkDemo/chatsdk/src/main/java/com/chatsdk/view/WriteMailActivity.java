package com.chatsdk.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.util.ImageUtil;
import com.chatsdk.view.actionbar.MyActionBarActivity;

public class WriteMailActivity extends MyActionBarActivity
{
	public String	roomName;
	public String	memberUids;
	public String	memberNames;
	public int   consumeGold = 0;
	public int   remainNum = 0;
	public boolean isFromCpp = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		fragmentClass = WriteMailFragment.class;

		ChatServiceController.toggleFullScreen(true, true, this);

		super.onCreate(savedInstanceState);

		//新页面接收数据
		Bundle bundle = this.getIntent().getExtras();
		//接收name值
		if(bundle!=null) {
			consumeGold = bundle.getInt("consumeGold",0);
			remainNum = bundle.getInt("remainNum",0);
			isFromCpp = bundle.getBoolean("isFromCpp",false);

		}
		Log.i("获取到的gold值为",String.valueOf(consumeGold));
	}
	
	protected void showBackground()
	{
		ImageUtil.setYRepeatingBG(this, fragmentLayout, R.drawable.mail_list_bg);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK)
		{
			roomName = data.getStringExtra("roomName");
			memberUids = data.getStringExtra("uidStr");
			memberNames = data.getStringExtra("nameStr");
		}
	}
}