package com.chatsdk.util;

import com.alibaba.fastjson.JSON;

public class JsonDeserializeUtil
{

	public static <T> T jsonToMailData(String jsonStr, Class<T> cls)
	{
		T mailData = null;
		try
		{
			mailData = JSON.parseObject(jsonStr, cls);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return mailData;
	}
}
