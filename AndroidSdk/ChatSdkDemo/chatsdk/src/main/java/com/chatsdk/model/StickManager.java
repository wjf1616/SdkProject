package com.chatsdk.model;

import org.apache.commons.lang.StringUtils;

public class StickManager
{
	private static final String[]	emojsOriginal	= { };	// "️😀","😁","👍","👎","😤","😳"
	private static final String[]	emojsCustom		= { }; // "14","13","57","58","11","6"

	public static String getPredefinedEmoj(String content)
	{
		String result = null;

		if (StringUtils.isEmpty(content) || content.length() > 2)
			return result;

		for (int i = 0; i < emojsOriginal.length; i++)
		{
			if (content.equals(emojsOriginal[i]))
			{
				result = emojsCustom[i];
				break;
			}
		}
		if (result != null)
		{
			result = "emoj" + result;
		}
		return result;
	}

	public static String getAliEmoj(String content)
	{
		Integer emo = null;
		try
		{
			emo = Integer.decode(content);
		}
		catch (Exception e)
		{
		}
		if (emo != null && emo.intValue() >= 1 && emo.intValue() <= 50)
		{
			return "ali" + Integer.toString(emo.intValue());
		}
		return null;
	}
}
