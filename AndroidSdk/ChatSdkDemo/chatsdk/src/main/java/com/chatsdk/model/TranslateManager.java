package com.chatsdk.model;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.db.DBManager;
import com.chatsdk.util.HeadPicUtil.MD5;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.TranslateListener;
import com.chatsdk.util.TranslateNewParams;
import com.chatsdk.util.TranslateUtil;
import com.chatsdk.util.TranslatedByLuaResult;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import com.chatsdk.BuildConfig;

public class TranslateManager
{

	private static TranslateManager		_instance				= null;
	private List<MsgItem>				translateQueue			= null;
	private Map<String, List<MsgItem>>	translateQueueLua		= null;
	private Timer						timer					= null;
	private TimerTask					timerTask				= null;
	private long						tranlateStartTime		= 0;
	private boolean						isTranlating			= false;
	public String						disableLang				= "";
	public static boolean				isTranslatedByLuaStart	= false;
	public static boolean				isUIShow				= false;
	private static ExecutorService		executorService			= null;

	public static TranslateManager getInstance()
	{
		if (_instance == null)
			_instance = new TranslateManager();
		return _instance;
	}

	private TranslateManager()
	{
		executorService = Executors.newFixedThreadPool(5);
		reset();
	}

	public void reset()
	{
		stopTimer();
		translateQueue = new ArrayList<MsgItem>();
		translateQueueLua = new HashMap<String, List<MsgItem>>();
	}

	private boolean isNeedTranslateChar(String str)
	{
		if (StringUtils.isNotEmpty(str) && StringUtils.isNumeric(str))
			return false;
		char[] chars = str.toCharArray();
		if (chars.length == 1)
		{
			int code = (int) chars[0];
			if (code >= 0 && code <= 255)
				return false;
		}
		return true;
	}

	public boolean isTranslateMsgValid(MsgItem msgItem)
	{
		if (StringUtils.isEmpty(msgItem.translateMsg)
				|| msgItem.translateMsg.startsWith("{\"code\":{")
				|| StringUtils.isEmpty(msgItem.translatedLang)
				|| !isLangSameAsTargetLang(msgItem.translatedLang))
			return false;
		return true;
	}

	public boolean isLangSameAsTargetLang(String lang)
	{
		boolean isSame = false;
		if (StringUtils.isNotEmpty(lang) && StringUtils.isNotEmpty(ConfigManager.getInstance().gameLang)
				&& (ConfigManager.getInstance().gameLang.equals(lang) || isSameZhLang(lang, ConfigManager.getInstance().gameLang)))
			isSame = true;
		return isSame;
	}

	public boolean isSameZhLang(String originalLang, String targetLang)
	{
		if (StringUtils.isNotEmpty(originalLang) && StringUtils.isNotEmpty(targetLang)
				&& ((isZh_CN(originalLang) && isZh_CN(targetLang)) || (isZh_TW(originalLang) && isZh_TW(targetLang))))
			return true;
		return false;
	}

	public boolean isZh_CN(String lang)
	{
		if (StringUtils.isNotEmpty(lang) && lang.equals("zh-CN") || lang.equals("zh_CN") || lang.equals("zh-Hans") || lang.equals("zh-CHS"))
			return true;
		return false;
	}

	public boolean isZh_TW(String lang)
	{
		if (StringUtils.isNotEmpty(lang) && lang.equals("zh-TW") || lang.equals("zh_TW") || lang.equals("zh-Hant") || lang.equals("zh-CHT"))
			return true;
		return false;
	}

	private boolean isOriginalLangValid(MsgItem msgItem)
	{
		if (msgItem.isOriginalSameAsTargetLang() && msgItem.translateMsg.equals(""))
			return true;
		return false;
	}

	public void enterTranlateQueue(MsgItem msgItem)
	{
		if (ConfigManager.autoTranlateMode <= 0)
			return;
		try
		{
			if (msgItem != null && !msgItem.isSelfMsg() && !msgItem.isNewMsg && !msgItem.isEquipMessage() && !msgItem.msg.equals("")
					&& isNeedTranslateChar(msgItem.msg) && !isOriginalLangValid(msgItem) && !isTranslateMsgValid(msgItem))
			{
				if (translateQueue != null && !translateQueue.contains(msgItem))
				{
					translateQueue.add(msgItem);
				}
				if (translateQueueLua != null)
				{
					List<MsgItem> list = null;
					if (translateQueueLua.containsKey(msgItem.msg))
					{
						list = translateQueueLua.get(msgItem.msg);
						if (list != null && !list.contains(msgItem))
						{
							list.add(msgItem);
						}
						else if (list == null)
						{
							list = new ArrayList<MsgItem>();
							list.add(msgItem);
							translateQueueLua.put(msgItem.msg, list);
						}
					}
					else
					{
						list = new ArrayList<MsgItem>();
						list.add(msgItem);
						translateQueueLua.put(msgItem.msg, list);
					}
				}

				if (timer == null)
					createTimer();
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	private boolean canTranslateByLua()
	{
		if (isTranslatedByLuaStart && isUIShow)
			return true;
		isTranslatedByLuaStart = JniController.getInstance().excuteJNIMethod("canTransalteByLua", null);
		return isTranslatedByLuaStart;
	}

	public void handleTranslateResult(TranslatedByLuaResult result)
	{
		List<MsgItem> list = translateQueueLua.get(result.getOriginalMsg());
		if (list != null && list.size() > 0)
		{
			for (int i = 0; i < list.size(); i++)
			{
				MsgItem msgItem = list.get(i);
				if (msgItem != null)
				{
					msgItem.translateMsg = result.getTranslatedMsg();
					msgItem.originalLang = result.getOriginalLang();
					msgItem.translatedLang = ConfigManager.getInstance().gameLang;
					
					if (TranslateManager.getInstance().hasTranslated(msgItem))
						msgItem.hasTranslated = true;
					else
						msgItem.hasTranslated = false;
					
					ChatChannel channel = null;
					if ((msgItem.channelType == DBDefinition.CHANNEL_TYPE_USER || msgItem.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
							&& msgItem.chatChannel != null)
					{
						channel = ChannelManager.getInstance().getChannel(msgItem.channelType, msgItem.chatChannel.channelID);
					}
					else if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY
							|| msgItem.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
					{
						channel = ChannelManager.getInstance().getChannel(msgItem.channelType);
					}
					if (channel != null)
					{
						DBManager.getInstance().updateMessage(msgItem, channel.getChatTable());
					}
				}
			}
			translateQueueLua.remove(result.getOriginalMsg());
			isTranlating = false;
		}
	}

	public boolean isInTranslateQueue(String msg)
	{
		if (translateQueueLua != null && translateQueueLua.containsKey(msg))
			return true;
		return false;
	}

	private void createTimer()
	{
		if (ConfigManager.autoTranlateMode <= 0)
			return;
		timer = new Timer();
		timerTask = new TimerTask()
		{

			@Override
			public void run()
			{
				if (System.currentTimeMillis() - tranlateStartTime >= 5000 || !isTranlating)
				{
					if (ConfigManager.autoTranlateMode == 2 && canTranslateByLua() && !translateQueueLua.isEmpty())
					{

						Set<String> msgKeySet = translateQueueLua.keySet();
						if (msgKeySet.size() > 0)
						{
							String msg = msgKeySet.toArray()[0].toString();
							if (StringUtils.isNotEmpty(msg))
							{
								if (System.currentTimeMillis() - tranlateStartTime >= 5000 && isTranlating)
								{
									translateQueueLua.remove(msg);
									isTranlating = false;
								}
								else
								{
									tranlateStartTime = System.currentTimeMillis();
									isTranlating = true;
									JniController.getInstance().excuteJNIVoidMethod("translateMsgByLua",
											new Object[] { msg, ConfigManager.getInstance().gameLang });
								}
							}
						}
						if (translateQueueLua.isEmpty())
							stopTimer();
					}
					else if (ConfigManager.autoTranlateMode == 1 && !translateQueue.isEmpty())
					{
						final MsgItem msgItem = translateQueue.get(0);
						if (msgItem != null)
						{
							tranlateStartTime = System.currentTimeMillis();
							isTranlating = true;
							try
							{
								String ret = TranslateUtil.translateNew(msgItem.msg, msgItem.getLang(),msgItem.channelType);
								TranslateNewParams param = JSON.parseObject(ret, TranslateNewParams.class);
								String translateMsg = param.getTranslateMsg();
								if (StringUtils.isNotEmpty(translateMsg) && !translateMsg.startsWith("{\"code\":{"))
								{
									msgItem.translateMsg = translateMsg;
									msgItem.originalLang = param.getOriginalLang();
									msgItem.translatedLang = ConfigManager.getInstance().gameLang;
									
									if (TranslateManager.getInstance().hasTranslated(msgItem))
										msgItem.hasTranslated = true;
									else
										msgItem.hasTranslated = false;
									
									ChatChannel channel = null;
									if ((msgItem.channelType == DBDefinition.CHANNEL_TYPE_USER || msgItem.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
											&& msgItem.chatChannel != null)
									{
										channel = ChannelManager.getInstance().getChannel(msgItem.channelType,
												msgItem.chatChannel.channelID);
									}
									else if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY
											|| msgItem.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
									{
										channel = ChannelManager.getInstance().getChannel(msgItem.channelType);
									}
									if (channel != null)
									{
										DBManager.getInstance().updateMessage(msgItem, channel.getChatTable());
									}
									translateQueue.remove(msgItem);
									isTranlating = false;
								}
							}
							catch (Exception e)
							{
								translateQueue.remove(msgItem);
								// LogUtil.trackMessage("JSON.parseObject exception on server"
								// +
								// UserManager.getInstance().getCurrentUser().serverId);
							}
						}
						if (translateQueue.isEmpty())
							stopTimer();
					}
				}

			}
		};
		timer.schedule(timerTask, 0, 100);
	}

	private void stopTimer()
	{
		try
		{
			if (timerTask != null)
			{
				timerTask.cancel();
				timerTask = null;
			}

			if (timer != null)
			{
				timer.cancel();
				timer.purge();
				timer = null;
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	class TranslateRunnable implements Runnable
	{
		private MsgItem	msgItem;
		private Handler	handler;

		public TranslateRunnable(MsgItem item, Handler handler)
		{
			this.msgItem = item;
			this.handler = handler;
		}

		@Override
		public void run()
		{
			try
			{
				//System.out.println("TranslateRunnable  "+Thread.currentThread().getName()+ " is running.");
				String origStr = msgItem.msg;
				if(msgItem.msg.contains(LanguageManager.getLangByKey(LanguageKeys.TIP_TO_ALL))){
					origStr = origStr.replace(LanguageManager.getLangByKey(LanguageKeys.TIP_TO_ALL),"");
				}
			    String ret = translateNew(origStr, msgItem.getLang(),msgItem.channelType);
				if(msgItem.isShareCommentMsg()){
					ret = translateNew(msgItem.shareComment, msgItem.getLang(),msgItem.channelType);
				}
				TranslateNewParams params = JSON.parseObject(ret, TranslateNewParams.class);
				String translateMsg = params.getTranslateMsg();
				String originalLang = params.getOriginalLang();

				if (StringUtils.isEmpty(translateMsg) || translateMsg.startsWith("{\"code\":{"))
					return;

				if (!msgItem.isTranlateDisable() && !msgItem.isOriginalSameAsTargetLang())
					msgItem.hasTranslated = true;
				else
					msgItem.hasTranslated = false;

				msgItem.translateMsg = translateMsg;
				msgItem.originalLang = originalLang;
				msgItem.translatedLang = ConfigManager.getInstance().gameLang;

				ChatChannel channel = null;
				if ((msgItem.channelType == DBDefinition.CHANNEL_TYPE_USER || msgItem.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
						&& msgItem.chatChannel != null)
				{
					channel = ChannelManager.getInstance().getChannel(msgItem.channelType, msgItem.chatChannel.channelID);
				}
				else if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY
						|| msgItem.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
				{
					channel = ChannelManager.getInstance().getChannel(msgItem.channelType);
				}
				if (channel != null)
				{
					DBManager.getInstance().updateMessage(msgItem, channel.getChatTable());
				}

				if (handler != null)
				{
					msgItem.hasTranslated = true;
					msgItem.isTranslatedByForce = true;
					msgItem.hasTranslatedByForce = true;

					Message msg = new Message();
					Bundle data = new Bundle();
					data.putString("translateMsg", translateMsg);
					msg.setData(data);
					handler.sendMessage(msg);
				}

			}
			catch (Exception e)
			{
				// LogUtil.trackMessage("JSON.parseObject exception on server" +
				// UserManager.getInstance().getCurrentUser().serverId);
			}
		}

	}

	public String translateNew(final String srcMsg, final String orginalLang,int channelType)
	{
		try
		{
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
			HttpConnectionParams.setSoTimeout(httpParams, 20000);
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpPost post = new HttpPost(ConfigManager.getInstance().translateURL);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			BasicNameValuePair sc = new BasicNameValuePair("sc", srcMsg);
			String originalLangStr = TranslateManager.getInstance().getTranslateLang(orginalLang);
			BasicNameValuePair sf = new BasicNameValuePair("sf", originalLangStr);
			String key = TranslateManager.getInstance().getTranslateLang(ConfigManager.getInstance().gameLang);
			//String translateLang = "[\"" + key + "\"]";
			String translateLang =  key;
			BasicNameValuePair tf = new BasicNameValuePair("tf", translateLang);
			BasicNameValuePair ch = new BasicNameValuePair("ch", "warz");
			String currentTime = Long.toString(System.currentTimeMillis());
			BasicNameValuePair t = new BasicNameValuePair("t", currentTime);
			//String md5 = MD5.getMD5Str(srcMsg + originalLangStr + translateLang + "warz" + currentTime + "4cP_sdf3se&gcxN-NDb5__Y$%d3fz_-ZF3");
			String md5 = MD5.getMD5Str(originalLangStr + translateLang + "warz" + currentTime + "4cP_sdf3se&gcxN-NDb5__Y$%d3fz_-ZF3");
			BasicNameValuePair sig = new BasicNameValuePair("sig", md5);

			UserInfo userInfo = UserManager.getInstance().getUser(UserManager.getInstance().getCurrentUserId());
			String uiStr = "";
			//ui += userInfo.uid+","+userInfo.serverId+","+ BuildConfig.VERSION_NAME;
			uiStr += userInfo.uid+","+userInfo.serverId;
			String sceneStr = "";
			if (channelType==0){
				sceneStr = "country";
			}else if (channelType==1){
				sceneStr = "alliance";
			}else{
				sceneStr = "private";
			}
			BasicNameValuePair ui = new BasicNameValuePair("ui", uiStr);
			BasicNameValuePair scene = new BasicNameValuePair("sig", sceneStr);

			params.add(sc);
			params.add(sf);
			params.add(tf);
			params.add(ch);
			params.add(ui);
			params.add(scene);
			params.add(t);
			params.add(sig);
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpResponse httpResponse = httpClient.execute(post);

			String responseStr = EntityUtils.toString(httpResponse.getEntity());
			return responseStr;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return srcMsg;
	}

	public void loadTranslation(final MsgItem msgItem, final TranslateListener translateListener)
	{
		if (!(msgItem != null && !msgItem.isSelfMsg() && !msgItem.isEquipMessage() && StringUtils.isNotEmpty(msgItem.msg)
				&& isNeedTranslateChar(msgItem.msg) && !isOriginalLangValid(msgItem) && !isTranslateMsgValid(msgItem)))
			return;

		Handler handler = null;
		if (translateListener != null)
		{
			handler = new Handler()
			{
				@Override
				public void handleMessage(Message msg)
				{
					super.handleMessage(msg);
					Bundle data = msg.getData();
					String translateMsg = data.getString("translateMsg");

					if (translateListener != null)
					{
						translateListener.onTranslateFinish(translateMsg);
					}

				}
			};
		}

		if (executorService != null)
			executorService.execute(new TranslateRunnable(msgItem, handler));
	}

	public String getTranslateLang(String originalLang)
	{
		if (isZh_CN(originalLang))
			return "zh-Hans";
		else if (isZh_TW(originalLang))
			return "zh-Hant";
		return originalLang;
	}
	
	public boolean hasTranslated(MsgItem msgItem)
	{
		return isTranslateMsgValid(msgItem) && !msgItem.isTranlateDisable() && !msgItem.isOriginalSameAsTargetLang() && (ChatServiceController.isDefaultTranslateEnable || (!ChatServiceController.isDefaultTranslateEnable && msgItem.hasTranslatedByForce));
	}
}
