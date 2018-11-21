package com.chatsdk.util;

public interface IAnalyticTracker
{
	public void trackException(String exceptionType, String funcInfo, String cause, String message);

	public void transportMail(String jsonStr, boolean isShowDetectMail);
	public String getPublishChannelName();

	public void trackMessage(String messageType, String... args);

	/**
	 * 值可以是int、long、String三种类型
	 */
	public void trackValue(String messageType, Object... args);
}
