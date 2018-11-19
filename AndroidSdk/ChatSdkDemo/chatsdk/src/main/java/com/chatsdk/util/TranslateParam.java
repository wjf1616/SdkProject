package com.chatsdk.util;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;

public class TranslateParam
{
	private String				sentences	= "";
	private String				src			= "";
	private int					server_time	= 0;
	private List<TranlateText>	translateTextArr;

	public TranslateParam()
	{
		translateTextArr = new ArrayList<TranlateText>();
	}

	@Override
	public String toString()
	{

		return "[TranslateParam:]  src: " + src + " server_time: " + server_time;
	}

	public String getTranslatedMsg()
	{
		String translateMsg = "";
		if (!sentences.equals(""))
		{
			translateTextArr = JSON.parseArray(sentences, TranlateText.class);
			for (int i = 0; i < translateTextArr.size(); i++)
			{
				TranlateText transText = translateTextArr.get(i);
				translateMsg += transText.getTrans();

			}
		}
		return translateMsg;
	}

	public String getSentences()
	{
		return sentences;
	}

	public void setSentences(String sentences)
	{
		this.sentences = sentences;
	}

	public String getSrc()
	{
		return src;
	}

	public void setSrc(String src)
	{
		this.src = src;
	}

	public int getServer_time()
	{
		return server_time;
	}

	public void setServer_time(int server_time)
	{
		this.server_time = server_time;
	}
}
