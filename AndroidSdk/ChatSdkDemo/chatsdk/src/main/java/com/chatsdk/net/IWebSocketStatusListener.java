package com.chatsdk.net;

public interface IWebSocketStatusListener
{
	public void onConsoleOutput(String message);
	public void onStatus(String message);
	public void onStremInput();
	public void onTestComplete();
	public void onConnectError();
}
