package com.chatsdk.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;

public class LanguageManager
{
	private static LanguagePack	chatText;

	private static String getLangKey(String originKey)
	{
		String result = originKey;
		if (originKey.equals("ara"))
			result = "ar";
		else if (originKey.equals("bs-Latn"))
			result = "bs";
		else if (originKey.equals("cht") || originKey.equals("zh-CHT") || originKey.equals("zh-TW") || originKey.equals("zh_TW") || originKey.equals("zh-Hant"))
			result = "zh-TW";
		else if (originKey.equals("zh-CHS") || originKey.equals("zh") || originKey.equals("zh-CN") || originKey.equals("zh_CN") || originKey.equals("zh-Hans"))
			result = "zh-CN";
		else if (originKey.equals("dan"))
			result = "da";
		else if (originKey.equals("bul"))
			result = "bg";
		else if (originKey.equals("est"))
			result = "et";
		else if (originKey.equals("fin"))
			result = "fi";
		else if (originKey.equals("fra"))
			result = "fr";
		else if (originKey.equals("jp"))
			result = "ja";
		else if (originKey.equals("kor"))
			result = "ko";
		else if (originKey.equals("slo"))
			result = "sl";
		else if (originKey.equals("sr-Cyrl") || originKey.equals("sr-Latn"))
			result = "sr";
		else if (originKey.equals("rom"))
			result = "ro";
		result = "lang_" + result;
		return result;
	}
	
	public static String getOriginalLangByKey(String originalKey)
	{
		String key = getLangKey(originalKey);
		String result = chatText != null ? chatText.getTextByKey(key) : key;
		if ((StringUtils.isEmpty(result) || result.equals(key)) && ChatServiceController.getInstance().host != null)
		{
			result = ChatServiceController.getInstance().host.getLang(key);
//			result = JniController.getInstance().excuteJNIMethod("getLang", new Object[] { key });
			if (StringUtils.isNotEmpty(result) && chatText != null)
			{
				chatText.putLangInMap(key, result);
			}
		}
		if (StringUtils.isEmpty(result))
		{
			//result = "lang." + originalKey;
		}
		return result;
	}

	public static String getLangByKey(String key)
	{
		String result = chatText != null ? chatText.getTextByKey(key) : key;
		if ((StringUtils.isEmpty(result) || result.equals(key)) && ChatServiceController.getInstance().host != null)
		{
			result = ChatServiceController.getInstance().host.getLang(key);
//			result = JniController.getInstance().excuteJNIMethod("getLang", new Object[] { key });
			if (StringUtils.isNotEmpty(result) && chatText != null)
			{
				chatText.putLangInMap(key, result);
			}
		}
		if (StringUtils.isEmpty(result))
		{
			//result = "lang." + key;
		}
		return result;
	}

	public static String getLangByKey(String key, String replaceStr)
	{
		String mapKey = getLangByKey(key);
		String result = (chatText != null && StringUtils.isNotEmpty(mapKey) ? chatText.getTextByKey(mapKey, replaceStr) : key);
		if ((StringUtils.isEmpty(result) || result.equals(key)) && ChatServiceController.getInstance().host != null)
		{
			result = ChatServiceController.getInstance().host.getLang1ByKey(key, replaceStr);
//			result = JniController.getInstance().excuteJNIMethod("getLang1ByKey", new Object[] { key, replaceStr });
		}
		if (StringUtils.isEmpty(result))
		{
			//result = "lang." + key;
		}
		return result;
	}

	public static String getLangByKey(String key, String replaceStr1, String replaceStr2)
	{
		String mapKey = getLangByKey(key);
		String result = (chatText != null && StringUtils.isNotEmpty(mapKey) ? chatText.getTextByKey(mapKey, replaceStr1, replaceStr2) : key);
		if ((StringUtils.isEmpty(result) || result.equals(key)) && ChatServiceController.getInstance().host != null)
		{
			result = ChatServiceController.getInstance().host.getLang2ByKey(key, replaceStr1, replaceStr2);
//			result = JniController.getInstance().excuteJNIMethod("getLang2ByKey", new Object[] { key, replaceStr1, replaceStr2 });
		}
		if (StringUtils.isEmpty(result))
		{
			//result = "lang." + key;
		}
		return result;
	}

	public static String getLangByKey(String key, String replaceStr1, String replaceStr2, String replaceStr3)
	{
		String mapKey = getLangByKey(key);
		String result = (chatText != null && StringUtils.isNotEmpty(mapKey) ? chatText.getTextByKey(mapKey, replaceStr1, replaceStr2,
				replaceStr3) : key);
		if ((StringUtils.isEmpty(result) || result.equals(key)) && ChatServiceController.getInstance().host != null)
		{
			result = ChatServiceController.getInstance().host.getLang3ByKey(key, replaceStr1, replaceStr2, replaceStr3);
//			result = JniController.getInstance().excuteJNIMethod("getLang3ByKey",
//					new Object[] { key, replaceStr1, replaceStr2, replaceStr3 });
		}
		if (StringUtils.isEmpty(result))
		{
			//result = "lang." + key;
		}
		return result;
	}

	public static String getLangByKey(String key, String replaceStr1, String replaceStr2, String replaceStr3,String replaceStr4)
	{
		String mapKey = getLangByKey(key);
		String result = (chatText != null && StringUtils.isNotEmpty(mapKey) ? chatText.getTextByKey(mapKey, replaceStr1, replaceStr2,
				replaceStr3,replaceStr4) : key);
		if ((StringUtils.isEmpty(result) || result.equals(key)) && ChatServiceController.getInstance().host != null)
		{
			result = ChatServiceController.getInstance().host.getLang4ByKey(key, replaceStr1, replaceStr2, replaceStr3, replaceStr4);
//			result = JniController.getInstance().excuteJNIMethod("getLang3ByKey",
//					new Object[] { key, replaceStr1, replaceStr2, replaceStr3 });
		}
		if (StringUtils.isEmpty(result))
		{
			//result = "lang." + key;
		}
		return result;
	}

	public static String getLangByKey(String key, String replaceStr1, String replaceStr2, String replaceStr3,String replaceStr4,String replaceStr5)
	{
		String mapKey = getLangByKey(key);
		String result = (chatText != null && StringUtils.isNotEmpty(mapKey) ? chatText.getTextByKey(mapKey, replaceStr1, replaceStr2,
				replaceStr3,replaceStr4,replaceStr5) : key);
		if ((StringUtils.isEmpty(result) || result.equals(key)) && ChatServiceController.getInstance().host != null)
		{
			result = ChatServiceController.getInstance().host.getLang5ByKey(key, replaceStr1, replaceStr2, replaceStr3,replaceStr4,replaceStr5);
//			result = JniController.getInstance().excuteJNIMethod("getLang3ByKey",
//					new Object[] { key, replaceStr1, replaceStr2, replaceStr3 });
		}
		if (StringUtils.isEmpty(result))
		{
			//result = "lang." + key;
		}
		return result;
	}

	public static void initChatLanguage(LanguageItem[] chatLangArray)
	{
		chatText = new LanguagePack(chatLangArray);
	}

	public static void initChatLanguage(ArrayList<LanguageItem> chatLangList)
	{
		chatText = new LanguagePack(chatLangList);
	}

	private static class LanguagePack
	{
		private Map<String, String>	textMap;

		public LanguagePack(LanguageItem[] chatLangArray)
		{
			textMap = new HashMap<String, String>();
			for (int i = 0; i < chatLangArray.length; i++)
			{
				textMap.put(chatLangArray[i].key, chatLangArray[i].langValue);
			}
		}

		public LanguagePack(ArrayList<LanguageItem> chatLangList)
		{
			textMap = new HashMap<String, String>();
			for (int i = 0; i < chatLangList.size(); i++)
			{
				textMap.put(chatLangList.get(i).key, chatLangList.get(i).langValue);
			}
		}

		public void putLangInMap(String key, String value)
		{
			if (textMap != null && StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value))
				textMap.put(key, value);
		}

		public String getTextByKey(String key)
		{
			if (StringUtils.isEmpty(key) || textMap == null || textMap.size() == 0 || !textMap.containsKey(key))
			{
				return "";
			}
			else if (textMap.get(key) == null || textMap.get(key).equals(""))
			{
				return "";
			}
			else
			{
				return textMap.get(key);
			}
		}

		public String getTextByKey(String s, String replaceStr)
		{
			if (s.contains("{0}"))
			{
				s = s.replace("{0}", replaceStr);
			}
			return s;
		}

		public String getTextByKey(String s, String replaceStr1, String replaceStr2)
		{
			if (s.contains("{0}"))
			{
				s = s.replace("{0}", replaceStr1);
			}
			if (s.contains("{1}"))
			{
				s = s.replace("{1}", replaceStr2);
			}
			return s;
		}

		public String getTextByKey(String s, String replaceStr1, String replaceStr2, String replaceStr3)
		{
			if (s.contains("{0}"))
			{
				s = s.replace("{0}", replaceStr1);
			}
			if (s.contains("{1}"))
			{
				s = s.replace("{1}", replaceStr2);
			}
			if (s.contains("{2}"))
			{
				s = s.replace("{2}", replaceStr3);
			}
			return s;
		}

		public String getTextByKey(String s, String replaceStr1, String replaceStr2, String replaceStr3, String replaceStr4)
		{
			if (s.contains("{0}"))
			{
				s = s.replace("{0}", replaceStr1);
			}
			if (s.contains("{1}"))
			{
				s = s.replace("{1}", replaceStr2);
			}
			if (s.contains("{2}"))
			{
				s = s.replace("{2}", replaceStr3);
			}
			if (s.contains("{3}"))
			{
				s = s.replace("{3}", replaceStr4);
			}
			return s;
		}

		public String getTextByKey(String s, String replaceStr1, String replaceStr2, String replaceStr3, String replaceStr4,String replaceStr5)
		{
			if (s.contains("{0}"))
			{
				s = s.replace("{0}", replaceStr1);
			}
			if (s.contains("{1}"))
			{
				s = s.replace("{1}", replaceStr2);
			}
			if (s.contains("{2}"))
			{
				s = s.replace("{2}", replaceStr3);
			}
			if (s.contains("{3}"))
			{
				s = s.replace("{3}", replaceStr4);
			}
			if (s.contains("{4}"))
			{
				s = s.replace("{4}", replaceStr4);
			}
			return s;
		}
	}
}
