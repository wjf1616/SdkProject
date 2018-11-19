package org;

import android.util.Log;

import com.chatsdk.net.IWebSocketStatusListener;
import com.chatsdk.net.WebSocketManager;
import com.chatsdk.util.LogUtil;

import org.apache.commons.lang.StringUtils;
import org.java_websocket.drafts.Draft_10;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by kaiguo on 2018/7/17.
 */

public class MqttClient {

    ExecutorService serviceCallback = Executors.newSingleThreadExecutor();

    private static MqttClient client = null;
    private static ReentrantLock lock = new ReentrantLock();
    public static  MqttClient getInstance() {
        lock.lock();
        try {
            if(client == null) {
                client = new MqttClient();
            }
        } finally {
            lock.unlock();
        }
        return client;
    }

    private native void setServerPort(String server, short  port);
    public void setServerAndPort(final String server, final short  port) {
        serviceCallback.execute(new Runnable() {
            @Override
            public void run() {
                setServerPort(server,port);
            }
        });
    }

    private native void sendData(String cmd, String data);
    public void sendDataToServer(final String cmd, final String data) {
        serviceCallback.execute(new Runnable() {
            @Override
            public void run() {
                sendData(cmd,data);
            }
        });
    }

    private native void connect();

    private native void  disconnect();
    public void disconnectWithServer() {
        serviceCallback.execute(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        });
    }

    public native void setUid(String uid);
    public native String getUid();
    public native int getStatus();

    public interface MqttClientCallBack {
        public void onRecvMessage(String data, String topic, String srcTopic );
        public void onOpen(int status);
        public void onClose();
        public void onError(int code, String reason);
    }
    public native void setCallBack( MqttClientCallBack callback );

    @Override
    protected void finalize() throws Throwable {
        try {
            setCallBack(null);
        } finally {
            super.finalize();
        }
    }


    public boolean						isOpen		= false;
    public boolean						isClose		= false;
    private IWebSocketStatusListener statusListener;
    private WebSocketManager webSocketManager;
    private ScheduledExecutorService heartbeatService;
    private String						clientID;
    private int							pingCnt	= 0;
    private int							pongCnt	= 0;



    public void SetClientData(String server, short port, Map<String, String> header, WebSocketManager webSocketManager, IWebSocketStatusListener statusListener) throws URISyntaxException {
        this.webSocketManager = webSocketManager;
        this.statusListener = statusListener;
        setServerPort(server,port);
   }

    public void setWebSocketManager( WebSocketManager socketManager) {
        this.webSocketManager = socketManager;
    }
    public void setStatusListener( IWebSocketStatusListener statusListener) {
        this.statusListener = statusListener;
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
        Log.i("MqttClient","gkjni clientID isMyMessage: " + getClientID(json));
        return StringUtils.isNotEmpty(clientID) && !clientID.equals(getClientID(json));
    }

    public void setClientID(JSONObject json)
    {
        clientID = getClientID(json);
        Log.i("MqttClient","gkjni clientID setClientID:" + clientID);
    }

    private void resetClientID()
    {
        clientID = null;
    }

    public void clearData()
    {
        setCallBack(null);
        setWebSocketManager(null);
        setStatusListener(null);
        resetClientID();
    }

    public void init() {

    }

    public void connectToServer() {
        setCallBack(new MqttClientCallBack() {
            @Override
            public void onRecvMessage(final String data, final String topic, final String srcTopic) {
                serviceCallback.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("MqClient","gkjni onRecvMessage " + data);
                        if(MqttClient.this.webSocketManager != null ) MqttClient.this.webSocketManager.handleMessage(data);
                    }
                });
            }

            @Override
            public void onOpen(final int status) {
                serviceCallback.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("MqClient","gkjni onOpen " + status);
                        if(LogUtil.nativeIsFLOG()) {
                            LogUtil.nativeFLOG("CS WSClients onOpen" + "_" + this.hashCode());
                        }

                        isOpen = true;

                        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "Connected");
                        if(MqttClient.this.statusListener != null ) MqttClient.this.statusListener.onConsoleOutput("Connected");
                        if(MqttClient.this.webSocketManager != null ) MqttClient.this.webSocketManager.resetReconnectInterval();
                        if(MqttClient.this.webSocketManager != null ) MqttClient.this.webSocketManager.onOpen();
                        if(MqttClient.this.webSocketManager != null ) MqttClient.this.webSocketManager.startKeepAlive();
                    }
                });
           }

            @Override
            public void onClose() {
                serviceCallback.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("MqClient","gkjni onClose" );
                        isClose = true;
                        isOpen = false;

                        if(LogUtil.nativeIsFLOG()) {
                            LogUtil.nativeFLOG("CS WSClients onClose" + "_" + this.hashCode());
                        }

                        String errorInfo = String.format(Locale.US, "WSClient.onClose Code:%d Reason:%s Remote:%b", 0, "noreaseon", "");
                        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, errorInfo);
                        if(MqttClient.this.statusListener != null ) MqttClient.this.statusListener.onConsoleOutput(errorInfo);
                        LogUtil.trackMessage(errorInfo);
                        if(MqttClient.this.webSocketManager != null ) MqttClient.this.webSocketManager.handleDisconnect();
                        if(MqttClient.this.webSocketManager != null ) MqttClient.this.webSocketManager.onConnectClose();
                    }
                });
            }

            @Override
            public void onError(final int code, final String reason) {
                serviceCallback.execute(new Runnable() {
                    @Override
                    public void run() {
                        isClose = true;
                        isOpen = false;
                        if(LogUtil.nativeIsFLOG()) {
                            LogUtil.nativeFLOG("CS WSClients onError:" + reason + "_" + this.hashCode());
                        }
                        if(MqttClient.this.statusListener != null ) MqttClient.this.statusListener.onConsoleOutput("Error:" + code);
                        LogUtil.trackMessage("WSClient.onError msg:" + code + " reason");
                        if(MqttClient.this.statusListener != null ) MqttClient.this.statusListener.onConnectError();                    }
                });

            }
        });
        serviceCallback.execute(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        });

    }
}
