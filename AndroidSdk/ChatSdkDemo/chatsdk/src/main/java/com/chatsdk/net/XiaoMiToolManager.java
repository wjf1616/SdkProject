package com.chatsdk.net;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.mi.milink.sdk.client.ClientLog;
import com.mi.mimsgsdk.AudioRecordListener;
import com.mi.mimsgsdk.MsgSdkManager;
import com.mi.mimsgsdk.message.AudioBody;
import com.mi.mimsgsdk.message.CustomBody;
import com.mi.mimsgsdk.message.MiMsgBody;
import com.mi.mimsgsdk.message.TextBody;
import com.mi.mimsgsdk.service.aidl.IMessageListener;
import com.mi.mimsgsdk.service.aidl.MiMessage;
import com.mi.mimsgsdk.service.aidl.RetValue;

public class XiaoMiToolManager {
	private String TAG = "xiaomi ";
	private static XiaoMiToolManager  	instance;

	/**
	 * 点击事件定义
	 */
	private static final int ACTION_CLICK_SENDER = 100;
	private static final int ACTION_CLICK_SENDER_ROOM = 101;
	private static final int ACTION_CLICk_SENDER_GROUP = 102;

	/**
	 * 消息来源渠道，即单聊，群组聊天，或房间
	 */
	public static final int CHANNEL_USER = 1; // 单聊
	public static final int CHANNEL_ROOM = 2; // 房间
	public static final int CHANNEL_GROUP = 3; // 群组

	/**
	 * bodyType
	 */
	public static final int BODY_TYPE_CUSTOM = 0;
	public static final int BODY_TYPE_TEXT = 1;
	public static final int BODY_TYPE_AUDIO = 2;

	/**
	 * 测试帐号定义
	 */

	private String pSkey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCyyJogBGwLOXYp71IhCQuS0dqkrT0B62qat/P2kB/vdIC+eS0egYcGIvh73VQB6nyc4/jb5pdLeMyImf64EIwIfMqa6iWl0MBI9oXwjs9Dct70PE8FvtcwjUz/1ts0nBF/0vkll8f2W5XlcciEHuXBvBjOcshL866/w3f+Bg5woQIDAQAB";
	private String appId = "100000001";
	private String pId = "0";

	// 用户1测试帐号
	private String gUid1 = "andriodTestSDK_guid1";
	private String b2Token1 = "PuFPm40MJpb9z/EHrruWzYZvwPCkBmmWOXWRl2PzWZs=";

	// 用户2测试帐号
	private String gUid2 = "andriodTestSDK_guid2";
	private String b2Token2 = "0+nFzqHrOGTepefYE1/Gudd7RRTz2y5zYwmsXpDGgcU=";

	private String gRid = "roomtest_1";
	private String gGid = "grouptest_1";

	/**
	 * 临时变量定义
	 */
	public String urlSaved = null;
	//保存到小米服务器这边的url
	public String urlServerVoice = null;
	public long mLength;
	Random rand = new Random();
	int randNum = rand.nextInt(3);
	private int msgId = randNum * 10000;
	private int TIME_OUT = 3000;

	private MsgSdkManager msgSdkManager = null;

	public static XiaoMiToolManager getInstance()
	{
		if (instance == null)
		{
			instance = new XiaoMiToolManager();
		}
		return instance;
	}

	private XiaoMiToolManager()
	{
		
	}
	
	public static void initActivity(Activity activity,String appId, String appKey,String pid, String pkey,String guid, String b2token){
		getInstance().initSDK(activity,appId,appKey,pid,pkey,guid,b2token);
	}
	
	public void initSDK(Activity activity,String aId, String appKey,String publicId, String pkey,String guid, String b2token){
		pSkey = appKey;
		appId = aId;
		pId = publicId;

		gUid1 = guid;
		b2Token1 = b2token;
		if(activity!=null && msgSdkManager==null){
			msgSdkManager = new MsgSdkManager(activity);
			msgSdkManager.audioMsgSdkInit(activity, audioManagerListener);
			initMiMsgSDK();
		}
	}
	/**
	 * 消息回调函数
	 */
	private IMessageListener mMessageListener = new IMessageListener.Stub() {

		@Override
		public boolean onReceiveOldUserMessage(List<MiMessage> arg0)
				throws RemoteException {
			Log.d(TAG, "onReceiveOldUserGameMessage size = " + arg0.size());
			return false;
		}

		@Override
		public boolean onReceiveOldGroupMessage(String arg0,
				List<MiMessage> arg1) throws RemoteException {
			Log.d(TAG, "onReceiveOldGroupGameMessage size = " + arg1.size());
			return false;
		}

		//发送成功后的回调
		@Override 
		public boolean onReceiveMessage(int channel, MiMessage arg0)
				throws RemoteException {
			ClientLog.d(TAG, "tuning test parseMessage ,MainActivity callback");
			final MiMsgBody receivedMsg = arg0.body;
			String channelInfomation = "";
			switch (channel) {
			case CHANNEL_USER:
				channelInfomation = "user";
				break;
			case CHANNEL_ROOM:
				channelInfomation = "room";
				break;
			case CHANNEL_GROUP:
				channelInfomation = "group";
				break;
			default:
				Log.w(TAG,
						"your sdk verson is not new ,please download the new one");
				break;
			}
			Log.d(TAG, "onReceiveGameMessage,channel =  " + channelInfomation
					+ ",value : " + receivedMsg.toString());

			String prex = "receive message from:" + arg0.from + " to:"
					+ arg0.to;
			switch (arg0.bodyType) {
			case 0:// 默认,传输的普通的二进制， 传入是怎么数据，接收到就怎么解析
				updateTvShow(prex + "  custom body size "
						+ ((CustomBody) receivedMsg).getData().length);
				break;
			case 1:// 普通文本
				TextBody mTextBody = (TextBody) receivedMsg;
				try {
					updateTvShow(prex + " text body " + mTextBody.getText()
							+ ";extra data = "
							+ new String(mTextBody.getContent(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				break;
			case 2:// 语音消息
				updateUIByAudioMessage(prex, receivedMsg);
				break;
			default:
				Log.w(TAG,
						"your sdk verson is not new ,please download the new one");
				break;
			}
			return false;
		}

		@Override
		public void onInitResult(RetValue arg0) throws RemoteException {
			Log.d(TAG, "onInitResult value : " + arg0.retMsg);
		}

		@Override
		public void onDataSendResponse(int channel, RetValue arg0,
				MiMessage arg1) throws RemoteException {
			Log.d(TAG, "onDataSendResponse  message send result  : "
					+ arg0.retCode + ",from channel = " + channel);
		}

		@Override
		public void onConnectionStateChanged(int arg0) throws RemoteException {
			Log.d(TAG, "onConnectionStateChanged  value  : " + arg0);
		}
	};

	
	/**
	 * 语音录制回调
	 */
	AudioRecordListener audioManagerListener = new AudioRecordListener() {
		@Override
		public void onRecordStart() {
			Log.d(TAG, "onRecordStart");
		}

		@Override
		public void onRecordInitializationSucceed() {
			Log.d(TAG, "onRecordInitializationSucceed");
		}

		@Override
		public void onRecordInitializationFailed() {
			Log.d(TAG, "onRecordInitializationFailed");
		}

		@Override
		public void onRecordInitializationCancelled() {
			Log.d(TAG, "onRecordInitializationCancelled");
		}

		@Override
		public void onRecordFinished(String localPath, long length) {
			String tips = "record finish, local path is " + localPath
					+ " length is " + length;
			Log.d(TAG, tips);
			updateTvShow(tips);
			urlSaved = localPath;
			mLength = length;
			//mSendAudio.setClickable(true);
		}

		@Override
		public void onRecordFailed() {
			Log.d(TAG, "onRecordFailed");
		}

		@Override
		public void onEndingRecord() {
			// TODO Auto-generated method stub
			Log.d(TAG, "onEndingRecord");
		}

		@Override
		public void onPlayEnd(String arg0, boolean arg1) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onPlayEnd");
		}

		@Override
		public void onPlayBegin(String arg0) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onPlayBegin");
		}
	};

	/**
	 * 初始化SDK回调控件,此事件为耗时操作，请用异步线程处理，或者如果需要同步则需要等待 界面
	 */
	public void initMiMsgSDK() {

		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected Void doInBackground(Void... params) {
				msgSdkManager.init(gUid1, pSkey, appId, b2Token1, pId,
						mMessageListener);
				return null;
			}
		};
		task.execute(null, null, null);
	}

	/**
	 * 更新收到的语音信息到界面
	 * 
	 * @param receivedMsg
	 */
	public void updateUIByAudioMessage(final String prex,
			final MiMsgBody receivedMsg) {
		Log.d(TAG,
				"xiaomi updateUIByAudioMessage");
		AudioBody mAudioBody = null;
		if (null != receivedMsg && receivedMsg instanceof AudioBody) {
			mAudioBody = (AudioBody) receivedMsg;
		} else {
			Log.d(TAG,"receive a old message ,please update your sdk version");
		}
		if(mAudioBody!=null){
			final String url = mAudioBody.getUrl();
			byte[] extraData = mAudioBody.getContent();
			try {
				updateTvShow(prex + " audio body url " + url + " length "
						+ mAudioBody.getLength() + ",audio extra =:"
						+ new String(extraData, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d(TAG, "xiaomi receive an audio message, url= " + url);
			urlSaved = url;
			urlServerVoice = url;
			mLength = mAudioBody.getLength();
		}
	}

	public MiMessage getMessage(String fromId, String toId, MiMsgBody body,
			long msgId, int bodyType) {
		MiMessage mMiMessage = new MiMessage();
		mMiMessage.from = fromId;
		mMiMessage.to = toId;
		mMiMessage.body = body;
		mMiMessage.msgId = msgId;
		mMiMessage.bodyType = bodyType;
		mMiMessage.sendTime = (int) (System.currentTimeMillis() / 1000);
		return mMiMessage;
	}
	//开始录语音
	public void startRecord() {
		if(msgSdkManager!=null){
			msgSdkManager.startRecord();
		}
	}
	//停止录语音
	public void stopRecord() {
		if(msgSdkManager!=null){
			msgSdkManager.stopRecord();
		}
	}
	//播放语音
	public void playVoice() {
		if(msgSdkManager!=null){
			msgSdkManager.playVoiceWithUrl(urlSaved);
		}
	}
	//停止播放语音
	public void stopPlayVoice() {
		if(msgSdkManager!=null){
			msgSdkManager.stopPlayVoice();
		}
	}
	//发送语音
	public void sendAudio() {
		if(msgSdkManager!=null){
			if (!TextUtils.isEmpty(urlSaved) && mLength != 0) {
				AudioBody mAudioBody = new AudioBody();
				mAudioBody.setLength(mLength);
				mAudioBody.setUrl(urlSaved);
				mAudioBody
						.setContent("Tuning test send audio with extra data"
								.getBytes());
				MiMessage message = getMessage(gUid1, gUid2, mAudioBody,
						msgId++, BODY_TYPE_AUDIO);
				updateTvShow("send message from: " + message.from + " to: "
						+ message.to + " message type audio, url "
						+ urlSaved + " length " + mLength);
				msgSdkManager.sendMessage(CHANNEL_USER, message, TIME_OUT);
			} else {
				Log.d(TAG,"no record audio");
			}
		}else{
			Log.d(TAG,"xiaomi msgSdkManager is null");
		}
	}
	
	private void updateTvShow(final String tips) {
		Log.d(TAG, "updateTvShow"+tips);
//		runOnUiThread(new Runnable() {
//			public void run() {
//				//mPushTextView.append(tips + "\n");
//			}
//		});
	}
}
