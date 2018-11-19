package com.chatsdk.util;

public class TranslatedByLuaResult
{
	private String	originalLang;
	private String	originalMsg;
	private String	translatedMsg;

	public String getOriginalLang()
	{
		return originalLang;
	}

	public void setOriginalLang(String originalLang)
	{
		this.originalLang = originalLang;
	}

	public String getOriginalMsg()
	{
		return originalMsg;
	}

	public void setOriginalMsg(String originalMsg)
	{
		this.originalMsg = originalMsg;
	}

	public String getTranslatedMsg()
	{
		return translatedMsg;
	}

	public void setTranslatedMsg(String translatedMsg)
	{
		this.translatedMsg = translatedMsg;
	}
}
