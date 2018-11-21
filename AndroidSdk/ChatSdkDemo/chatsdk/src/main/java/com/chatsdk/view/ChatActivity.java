package com.chatsdk.view;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.ServiceInterface;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MsgItem;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.view.actionbar.MyActionBarActivity;

import java.util.Iterator;
import java.util.Map;

public final class ChatActivity extends MyActionBarActivity
{
	public int	channelType;
	private SensorManager sensorManager;
	private static final String TAG = "Sensor";
	private static final int SENSOR_REDPACKAGE = 10;
	private static String attachmentId = "";
	private static boolean noRedPackage = true;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		ChatServiceController.isRunning = true;
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			this.bundle = new Bundle(extras);
			if (extras.getInt("channelType") >= 0)
			{
				channelType = extras.getInt("channelType");
				if(!ChatServiceController.isFromBd){
					ChatServiceController.setCurrentChannelType(channelType);
				}
			}
		}

		fragmentClass = ChatFragment.class;

		if(ConfigManager.isRedPackageShakeEnabled) {
			sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
		}

//		ChatServiceController.toggleFullScreen(false, true, this);
		ChatServiceController.toggleFullScreen(true, true, this);
		super.onCreate(savedInstanceState);
	}

	protected void showBackground()
	{
		fragmentLayout.setBackgroundResource(R.drawable.ui_paper_3c);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		//TODO -----------
		// 这里以前代码是ChatServiceController.setCurrentChannelType(channelType);
		// 如果在切换频道后，游戏失去焦点，再恢复焦点的后会重新把创建面板时channelType再重新赋值，但当前的频道已经切换了，会导致频道错误
		if(ChatServiceController.getCurrentChannelType() == -1)
		{
			ChatServiceController.setCurrentChannelType(channelType);
		}else{
			channelType = ChatServiceController.getCurrentChannelType();
		}
		//------------------
		ChatServiceController.isRunning = true;
		//channelType,解决聊天室刷新未读书问题
		if(ChatServiceController.isTabRoom){
			ChatServiceController.getChatFragment().refreshUnreadCount();
			ChatServiceController.getChatFragment().notifyDataSetChanged();
		}
		if (ChatServiceController.getChatFragment() != null) {
			if(channelType == DBDefinition.CHANNEL_TYPE_CHATROOM) {
				ChatServiceController.getChatFragment().changeChatRoomName(UserManager.getInstance().getCurrentMail().opponentName);
				ChatServiceController.getChatFragment().notifyDataSetChanged();
			}else{
				ChatServiceController.getChatFragment().notifyDataSetChanged(channelType, true);
			}

			ChatServiceController.getChatFragment().refreshMemberSelectBtn();
			ChatServiceController.getChatFragment().setSelectMemberBtnState();

			if (ChatServiceController.isShowProgressBar) {
				showProgressBar();
			}
		}

		if (sensorManager != null) {// 注册监听器
			// 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率
			sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		ChatServiceController.isRunning = false;

		if (sensorManager != null) {// 取消监听器
			sensorManager.unregisterListener(sensorEventListener);
		}
	}

	@Override
	public void exitActivity()
	{
		super.exitActivity();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		if (sensorManager != null) {// 取消监听器
			sensorManager.unregisterListener(sensorEventListener);
			sensorManager = null;
			sensorEventListener = null;
		}
//		System.gc();
//		System.runFinalization();
	}

	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus)
		{
			// 这里调onBecomeVisible()与adjustHeight中调差不多
			// showProgressBar();
			// ((ChatFragment) fragment).onBecomeVisible();
		}
		else
		{
		}
	}

	/**
	 * 重力感应监听
	 */
	private SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			// 传感器信息改变时执行该方法
			float[] values = event.values;
			float x = values[0]; // x轴方向的重力加速度，向右为正
			float y = values[1]; // y轴方向的重力加速度，向前为正
			float z = values[2]; // z轴方向的重力加速度，向上为正
			//Log.d(TAG, "x轴方向的重力加速度" + x +  "；y轴方向的重力加速度" + y +  "；z轴方向的重力加速度" + z);
			// 一般在这三个方向的重力加速度达到40就达到了摇晃手机的状态。
			int medumValue = 17;
			if (Math.abs(x) > medumValue || Math.abs(y) > medumValue || Math.abs(z) > medumValue) {
				//Log.d(TAG, "检测到摇晃，执行操作！");
				Map<String, MsgItem> map = ChannelManager.getInstance().getUnHandleRedPackageMap();
				if(map == null)return;
				Iterator<String> it = map.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					MsgItem msgItem = map.get(key);
					if (msgItem != null && msgItem.sendState == MsgItem.UNHANDLE) {
						if (msgItem.channelType == ChatServiceController.getCurrentChannelType()
								&& !(msgItem.isRedPackageFinish() || attachmentId.equals(key)) ) {
							String[] redPackageInfoArr = msgItem.attachmentId.split("\\|");
							ChatServiceController.doHostAction("pickRedPackage", "", msgItem.msg, redPackageInfoArr[0], true);
							attachmentId = redPackageInfoArr[0];
							noRedPackage = true;
							//Log.d(TAG, "查看红包!");
							return;
						}
					}
				}

				if(noRedPackage){
					//Log.d(TAG, "没有可以领取的红包！");
					ServiceInterface.flyHint("", "", LanguageManager.getLangByKey("79011070"), 0, 0, false);
					noRedPackage = false;
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

}