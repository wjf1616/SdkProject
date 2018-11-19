package com.chatsdk.view.dialog;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chatsdk.R;
import com.chatsdk.controller.ServiceInterface;
import com.chatsdk.util.ResUtil;

/**
 * TODO 删除无用类
 */
public class YesOrNoDialog extends Activity
{
	private LinearLayout	layout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		//处理系统字体缩放导致布局错乱情况，这里设置缩放比例始终保持为1
		Resources resource = getResources();
		Configuration configuration =resource.getConfiguration();
		configuration.fontScale = 1.0f;//设置字体的缩放比例
		resource.updateConfiguration(configuration , resource.getDisplayMetrics());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cs__confirm_dialog);
		layout = ((LinearLayout) findViewById(ResUtil.getId(this, "id", "exit_layout")));

		layout.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Activity activity = (Activity) getApplicationContext();
				if (activity != null && !activity.isFinishing()) {
					ServiceInterface.safeMakeText(activity,"提示：点击窗口外部关闭窗口！", Toast.LENGTH_SHORT);
				}
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		finish();
		return true;
	}

	public void exitbutton1(View v)
	{
		this.finish();
	}

	public void exitbutton0(View v)
	{
		this.finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//处理系统字体缩放导致布局错乱情况，这里设置缩放比例始终保持为1
		Resources resource = getResources();
		Configuration configuration =resource.getConfiguration();
		configuration.fontScale = 1.0f;//设置字体的缩放比例
		resource.updateConfiguration(configuration , resource.getDisplayMetrics());
	}
}
