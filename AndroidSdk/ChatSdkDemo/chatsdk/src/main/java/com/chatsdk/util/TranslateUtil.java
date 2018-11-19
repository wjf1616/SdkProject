package com.chatsdk.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSON;
//import com.chatsdk.BuildConfig;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.MsgItem;
import com.chatsdk.model.TranslateManager;
import com.chatsdk.model.UserInfo;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.db.DBManager;
import com.chatsdk.util.HeadPicUtil.MD5;

public class TranslateUtil
{
	public static void loadTranslate(final MsgItem msgItem, final TranslateListener translateListener)
	{
		final Handler handler = new Handler()
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

		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				String ret = translateNew(msgItem.msg,msgItem.getLang(),msgItem.channelType);
				
				try
				{
					TranslateNewParams params = JSON.parseObject(ret, TranslateNewParams.class);
					String translateMsg = params.getTranslateMsg();
					String originalLang = params.getOriginalLang();
					
					if (StringUtils.isNotEmpty(translateMsg) && !translateMsg.startsWith("{\"code\":{"))
					{

						msgItem.translateMsg = translateMsg;
						msgItem.originalLang = originalLang;
						msgItem.translatedLang = ConfigManager.getInstance().gameLang;
						
						msgItem.hasTranslated = true;
						msgItem.isTranslatedByForce = true;
						msgItem.hasTranslatedByForce = true;
						
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
					}
					
					Message msg = new Message();
					Bundle data = new Bundle();
					data.putString("translateMsg", translateMsg);
					msg.setData(data);
					handler.sendMessage(msg);
				}
				catch (Exception e)
				{
//					LogUtil.trackMessage("JSON.parseObject exception on server" + UserManager.getInstance().getCurrentUser().serverId);
				}
			}
		};
		new Thread(runnable).start();
	}

	/**
	 * 将文本进行URL编码
	 */
	private static String encodeText(String text)
	{
		String str = text;
		try
		{
			str = URLEncoder.encode(text, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		return str;
	}

	public static String translate(String text)
	{
		InputStream inputStream = null;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		BufferedInputStream bufferInputStream = null;
		String engineUrl2 = "https://translate.google.com/translate_a/t?client=x&text=";
		String langParam = "&sl=auto&tl=" + ConfigManager.getInstance().gameLang + "&ie=UTF-8&oe=UTF-8";

		String urlstr = engineUrl2 + encodeText(text) + langParam;
		URL url = null;
		String res = "";
		HttpURLConnection connection = null;
		try
		{
			url = new URL(urlstr);
			connection = (HttpURLConnection) url.openConnection();
			String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.71 Safari/534.24";
			long randomType = Math.round(Math.random() * 3 + 1);
			switch ((int) randomType)
			{
				case 1:
					userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.71 Safari/534.24";
					break;
				case 2:
					userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
					break;
				case 3:
					userAgent = "Mozilla/5.0 (Linux; U; Android 4.1.1; fr-fr; MB525 Build/JRO03H; CyanogenMod-10) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
					break;
				case 4:
					userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_4) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30";
					break;
				default:
					userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
					break;

			}
			connection.setRequestProperty("User-Agent", userAgent);
			connection.connect();
			inputStream = connection.getInputStream();
			bufferInputStream = new BufferedInputStream(inputStream, 4096);
			int i = -1;
			byte buf[] = new byte[4 * 1024];
			while ((i = bufferInputStream.read(buf)) != -1)
			{
				output.write(buf, 0, i);
			}
			res = new String(output.toByteArray(), "UTF-8");
			inputStream.close();
			output.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return res;
	}

	public static String getOriginalLang(String text)
	{
		if (text.lastIndexOf(",,,,") < 4 || text.lastIndexOf("\"]],,") < 0 || text.lastIndexOf(",,[[\"") < 0
				|| text.lastIndexOf("\"],,") < 0 || text.lastIndexOf(",,[[\"") + 5 > text.lastIndexOf("\"],,"))
			return "";
		String ret = text.substring(text.lastIndexOf(",,[[\"") + 5, text.lastIndexOf("\"],,"));
		if (ret.equals("zh-CN"))
			ret = "zh_CN";
		else if (ret.equals("zh-TW"))
			ret = "zh_TW";
		return ret;
	}

	public static String getTranslateText(String text)
	{
		if (text.lastIndexOf(",,,,") < 4)
			return text;
		String temp = text.substring(4, text.lastIndexOf(",,,,"));
		if (temp.lastIndexOf("\"]],,") < 0)
			return text;
		temp = temp.substring(0, temp.lastIndexOf("\"]],,"));
		String replaceStr = TranslateUtil.class.getSimpleName() + System.currentTimeMillis();
		String temp2 = temp.replace("\"],[\"", replaceStr);
		String[] testArr = temp2.split(replaceStr);
		String result = "";
		for (int i = 0; i < testArr.length; i++)
		{
			if (i < testArr.length - 1 && testArr[i].endsWith("\\"))
				testArr[i] += "\"],[\"";
			else
			{
				if (testArr[i].indexOf("\",\"") < 0)
					return text;
				testArr[i] = testArr[i].substring(0, testArr[i].indexOf("\",\""));
			}
			testArr[i] = testArr[i].replace("\\\"", "\"");

			result += testArr[i];
		}
		return decodeUnicode(result).toString();
	}

	public static String translateNew(final String srcMsg, final String orginalLang,int channelType)
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
			//uiStr += userInfo.uid+","+userInfo.serverId+","+ BuildConfig.VERSION_NAME;

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

	public static StringBuffer decodeUnicode(final String dataStr)
	{
		final StringBuffer buffer = new StringBuffer();
		String tempStr = "";
		String operStr = dataStr;

		if (operStr != null && operStr.indexOf("\\u") == -1)
			return buffer.append(operStr); //
		if (operStr != null && !operStr.equals("") && !operStr.startsWith("\\u"))
		{
			tempStr = operStr.substring(0, operStr.indexOf("\\u"));
			operStr = operStr.substring(operStr.indexOf("\\u"), operStr.length()); // operStr字符一定是以unicode编码字符打头的字符串
		}
		buffer.append(tempStr);
		while (operStr != null && !operStr.equals("") && operStr.startsWith("\\u"))
		{
			// 循环处理,处理对象一定是以unicode编码字符打头的字符串
			tempStr = operStr.substring(0, 6);
			operStr = operStr.substring(6, operStr.length());
			String charStr = "";
			charStr = tempStr.substring(2, tempStr.length());
			char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
			buffer.append(new Character(letter).toString());
			if (operStr.indexOf("\\u") == -1)
			{
				buffer.append(operStr);
			}
			else
			{
				// 处理operStr使其打头字符为unicode字符
				tempStr = operStr.substring(0, operStr.indexOf("\\u"));
				operStr = operStr.substring(operStr.indexOf("\\u"), operStr.length());
				buffer.append(tempStr);
			}
		}
		return buffer;
	}
}
