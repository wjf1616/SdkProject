package org;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import android.util.Log;

import com.chatsdk.net.IWebSocketStatusListener;
import com.chatsdk.net.WebSocketManager;
import com.chatsdk.util.LogUtil;

public class WsClient extends WebSocketClient {
	public boolean						isOpen		= false;
	public boolean						isClose		= false;
	private IWebSocketStatusListener	statusListener;
	private WebSocketManager			webSocketManager;
	private ScheduledExecutorService	heartbeatService;
	private String						clientID;
	private int							pingCnt	= 0;
	private int							pongCnt	= 0;

    public WsClient(String serverURI, Map<String, String> header, WebSocketManager webSocketManager, IWebSocketStatusListener statusListener) throws URISyntaxException {
        super(new URI(serverURI), new Draft_10(), header, 5000);
        
        this.webSocketManager = webSocketManager;
        this.statusListener = statusListener;
    }
    public WsClient(String serverURI, Map<String, String> header, WebSocketManager webSocketManager, IWebSocketStatusListener statusListener, Draft draft) throws URISyntaxException {
        super(new URI(serverURI), draft, header, 5000);
        
        this.webSocketManager = webSocketManager;
        this.statusListener = statusListener;
    }

    public void setWebSocketManager( WebSocketManager socketManager) {
		this.webSocketManager = socketManager;
	}
	public void setStatusListener( IWebSocketStatusListener statusListener) {
		this.statusListener = statusListener;
	}
    private void startKeepAlive()
    {
    	if(heartbeatService != null) return;
        heartbeatService = Executors.newSingleThreadScheduledExecutor();
        TimerTask timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					sendKeepAlive();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		};
		
		heartbeatService.scheduleWithFixedDelay(timerTask, 10000, 15 * 1000, TimeUnit.MILLISECONDS);
    }
	
	private void sendKeepAlive()
	{
		if(isOpen()){
			pingCnt++;
			LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_WS_SEND, "ping " + pingCnt);
			sendFrame(new FramedataImpl1(Framedata.Opcode.PING));
		}
	}
	@Override
	public void onWebsocketPong( WebSocket conn, Framedata f ) {
		super.onWebsocketPong(conn, f);
		pongCnt++;
		LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_WS_RECIEVE, "pong " + pongCnt);
	}

    @Override
    public void onOpen(ServerHandshake serverHandshake) {

		if(LogUtil.nativeIsFLOG()) {
			LogUtil.nativeFLOG("CS WSClients onOpen" + "_" + this.hashCode());
		}

        isOpen = true;
        
    	LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "Connected");
    	if(this.statusListener != null ) statusListener.onConsoleOutput("Connected");
		if(this.webSocketManager != null ) webSocketManager.resetReconnectInterval();
		if(this.webSocketManager != null ) webSocketManager.onOpen();
		if(this.webSocketManager != null ) webSocketManager.startKeepAlive();
    }

    @Override
    public void onMessage(String s) {
		if(this.webSocketManager != null ) webSocketManager.handleMessage(s);
    }

    @Override
    public void onClose(int code, String reason, boolean b) {
        isClose = true;
        isOpen = false;

		if(LogUtil.nativeIsFLOG()) {
			LogUtil.nativeFLOG("CS WSClients onClose" + "_" + this.hashCode());
		}
        
        String errorInfo = String.format(Locale.US, "WSClient.onClose Code:%d Reason:%s Remote:%b", code, reason, b);
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, errorInfo);
		if(this.statusListener != null ) statusListener.onConsoleOutput(errorInfo);
		LogUtil.trackMessage(errorInfo);
		if(this.webSocketManager != null ) webSocketManager.handleDisconnect();
		if(this.webSocketManager != null ) webSocketManager.onConnectClose();
    }

    @Override
    public void onError(Exception e) {
        isClose = true;
        isOpen = false;
		if(LogUtil.nativeIsFLOG()) {
			LogUtil.nativeFLOG("CS WSClients onError:" + e.getMessage() + "_" + this.hashCode());
		}
    	LogUtil.printException(e);
		LogUtil.printVariablesWithFuctionName(Log.WARN, LogUtil.TAG_WS_STATUS, "error", e.getMessage());
		if(this.statusListener != null ) statusListener.onConsoleOutput("Error:" + e.getMessage());
		LogUtil.trackMessage("WSClient.onError msg:" + e.getMessage());
		if(this.webSocketManager != null ) webSocketManager.onConnectError();
    }

	private String getClientID(JSONObject json)
	{
		try
		{
			return json.getString("server") + json.getString("clientid");
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		
		return null;
	}
    
    public boolean isMyMessage(JSONObject json)
    {
    	return StringUtils.isNotEmpty(clientID) && !clientID.equals(getClientID(json));
    }
	
	public void setClientID(JSONObject json)
	{
		clientID = getClientID(json);
	}
	
	private void resetClientID()
	{
		clientID = null;
	}
    
    public void destroy()
    {
    	heartbeatService.shutdown();
    	heartbeatService = null;
    }
}
