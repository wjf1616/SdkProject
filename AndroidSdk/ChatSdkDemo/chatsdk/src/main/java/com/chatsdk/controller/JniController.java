package com.chatsdk.controller;

import com.chatsdk.util.IJniCallHelper;

public class JniController
{
	private static JniController	_instance	= null;
	public static IJniCallHelper	jniHelper		= null;

	private JniController()
	{
	}

	public static JniController getInstance()
	{
		if (_instance == null)
		{
			synchronized (JniController.class)
			{
				if (_instance == null)
					_instance = new JniController();
			}
		}
		return _instance;
	}

	public <T> T excuteJNIMethod(final String methodName, final Object[] params)
	{
		if (jniHelper != null)
		{
			return jniHelper.excuteJNIMethod(methodName, params);
		}
		return null;
	}

	public void excuteJNIVoidMethod(final String methodName, final Object[] params)
	{
		if (jniHelper != null)
		{
			jniHelper.excuteJNIVoidMethod(methodName, params);
		}
	}
}
