package com.chatsdk.net;

import android.os.Build;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.controller.MenuController;
import com.chatsdk.controller.ServiceInterface;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.LatestHornMessage;
import com.chatsdk.model.MsgItem;
import com.chatsdk.model.TimeManager;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.db.DBManager;
import com.chatsdk.util.HeadPicUtil.MD5;
import com.chatsdk.util.HttpRequestUtil;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.NetworkUtil;

import org.MqttClient;
import org.WsClient;
import org.apache.commons.lang.StringUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.FramedataImpl1;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.chatsdk.model.MsgItem.MSG_TYPE_NEWS_CENTER_SHARE;

//import com.longtech.chatservicev2.utils.AZMessageStoreController;

public class WebSocketManager {
    private final static String APP_ID = "100001";
    public final static String WS_SERVER_LIST_URL = "http://107.178.245.119/server/links";
    public final static String WS_ALL_SERVER_LIST_URL = "http://107.178.245.119/server/all";
    private final static WSServerInfo DEFAULT_SERVER = new WSServerInfo("ws", "169.44.70.39", "80");

    private static WebSocketManager instance;

    static boolean isUseMqtt = false;
    private WsClient client;
    private MqttClient mqclient = MqttClient.getInstance();
    private IWebSocketStatusListener statusListener;
    private ScheduledExecutorService getServerListService;
    private ArrayList<WSServerInfo> serversInfos;
    public boolean enableNetworkOptimization = false;
    public long networkOptimizationTimeout = 0;
    public long networkOptimizationTestDelay = 0;

    private ScheduledExecutorService    heartbeatService;
    private Boolean                     isForeground = true;


    private static ExecutorService closeSocket = Executors.newSingleThreadExecutor();


    public static WebSocketManager getInstance() {
        if (instance == null)
        {
            synchronized (WebSocketManager.class)
            {
                if (instance == null)
                {
                    instance = new WebSocketManager();
//                    WebSocketHelper.getInstance();
                }
            }
        }
        return instance;
    }

    protected WebSocketManager() {
    }

    public void setStatusListener(IWebSocketStatusListener listener) {
        statusListener = listener;
    }

    /**
     * 应该只调一次，以后断线会自动触发重连
     */
    public void connect() {

        isUseMqtt = ChatServiceController.chat_mqtt;

        isClearSocket = false ;

        if(mqclient != null && mqclient.getStatus() > 0 ) {
            if( LogUtil.nativeIsFLOG()) {
                LogUtil.nativeFLOG("CS WS:OPEN sendKeepAlive");
            }
            //发个心跳试试是不是真的连上的
            sendKeepAlive();
        }
        boolean isSocketConnect = false;
        if(isUseMqtt) {
            isSocketConnect = mqclient.getStatus() > 0;
        }
        else {
            isSocketConnect = client != null &&  WebSocket.READYSTATE.OPEN == client.getReadyState();
        }

        //如果已经是连接状态
        if( isSocketConnect) {

            if( LogUtil.nativeIsFLOG()) {
                LogUtil.nativeFLOG("CS WS:OPEN sendKeepAlive");
            }
            //发个心跳试试是不是真的连上的
            sendKeepAlive();

            return;
        }

        //如果正在连接，则直接返回,等待连接
        if(client != null && WebSocket.READYSTATE.CONNECTING == client.getReadyState()){

            if( LogUtil.nativeIsFLOG()) {
                LogUtil.nativeFLOG("CS WS:CONNECTING");
            }
            return;
        }

        if ( !isUseMqtt && client != null && WebSocket.READYSTATE.CLOSING == client.getReadyState()){
            if( LogUtil.nativeIsFLOG()) {
                LogUtil.nativeFLOG("CS WS:CLOSING");
            }
            return;
        }
        else{
            synchronized (this) {
                reConnectCount = 0;
            }
            if (statusListener == null) {
                statusListener = WebSocketStatusHandler.getInstance();
            }
            if( LogUtil.nativeIsFLOG()) {
                LogUtil.nativeFLOG("CS WS:startGetServerList");
            }
            startGetServerList();
        }

        loadInitMsgs();
    }

    private void loadInitMsgs() {
        ArrayList<ChatChannel> channels = new ArrayList<ChatChannel>();
        channels.add(ChannelManager.getInstance().getCountryChannel());
        channels.add(ChannelManager.getInstance().getAllianceChannel());
        for (int i = 0; i < channels.size(); i++) {
            ChatChannel channel = channels.get(i);
            if (channel != null && !channel.hasInitLoaded()) {
                channel.loadMoreMsg();
            }
        }
    }

    private JSONObject roomsParams;

    /**
     * 为了防止在发送getNewMsg请求前收到push、改变时间戳，在连接成功后马上设置参数
     */
    public void onOpen() {
        statusListener.onStatus("");
        ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_NETWORK_CONNECTED);
        JniController.getInstance().excuteJNIVoidMethod("notifyWebSocketStatus", new Object[]{Boolean.valueOf(true)});
        roomsParams = getRoomsParams();
//        synchronized (this) {
//            reConnectCount = 0;
//        }
        if( currentServer != null ) {
            JniController.getInstance().excuteJNIVoidMethod("sendServerStatus", new Object[]{currentServer.address,
                    currentServer.port, currentServer.protocol, 1});
        }

    }

    private void startGetServerList() {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
        serversInfos = new ArrayList<WSServerInfo>();

        if(getServerListService != null && !getServerListService.isShutdown()) return;
        getServerListService = Executors.newSingleThreadScheduledExecutor();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    getServerList();
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        };

        getServerListService.scheduleWithFixedDelay(timerTask, 100, 5000, TimeUnit.MILLISECONDS);
    }

    int _serverlist_retry = 0;
    private void getServerList() {
        statusListener.onStatus(LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_GET_SERVERLIST));
        ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_NETWORK_CONNECTING);
        JniController.getInstance().excuteJNIVoidMethod("notifyWebSocketStatus", new Object[]{Boolean.valueOf(false)});

        if(ChatServiceController.isCafebazaar()){
            serversInfos = new ArrayList<WSServerInfo>();
            WSServerInfo server = new WSServerInfo("ws", "app1.im.medrickgames.com", "8180");
            serversInfos.add(server);
        }else{
            String timeStr = Integer.toString(TimeManager.getInstance().getCurrentTime());
            String secret = MD5.stringMD5(MD5.stringMD5(timeStr.substring(0, 3)) + MD5.stringMD5(timeStr.substring(timeStr.length() - 3, timeStr.length())));
            String sign = MD5.stringMD5(APP_ID + UserManager.getInstance().getCurrentUserId() + secret);
            String param = "t=" + timeStr + "&s=" + sign + "&a=" + APP_ID + "&u=" + UserManager.getInstance().getCurrentUserId();
            boolean isChina =  JniController.getInstance().excuteJNIMethod("isChina", new Object[]{});
            if( isChina ){
                param += "&f=cn";
            }
            if(isUseMqtt ) {
                param += "$m=1";
            }

            String serverListUrl =WS_SERVER_LIST_URL;
            if(LogUtil.isDebug && !ChatServiceController.isxternaletworkebug){
                serverListUrl = "http://10.0.0.19:81/server/links";
            }
            if( LogUtil.nativeIsFLOG()) {
                LogUtil.nativeFLOG("CS GetServerList:" + serverListUrl + "?" + param);
            }
            LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "getServerUrl", serverListUrl + "?" + param);
            statusListener.onConsoleOutput("getServerList: " + serverListUrl + "?" + param);
            String serverlistJson = HttpRequestUtil.sendGet(serverListUrl, param);
            long callTime = TimeManager.getInstance().getCurrentTimeMS();
            if( LogUtil.nativeIsFLOG()) {
                LogUtil.nativeFLOG("CS GetServerListResult:" + serverlistJson);
            }
            if (!StringUtils.isEmpty(serverlistJson)) {
                LogUtil.trackGetServerListTime(TimeManager.getInstance().getCurrentTimeMS() - callTime);
                onGetServerList(serverlistJson);
            } else {
                _serverlist_retry++;
                LogUtil.trackGetServerListTime(-1);
            }
        }

        if (serversInfos.size() == 0) {
            statusListener.onStatus(LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_GET_SERVERLIST_ERROR));
            ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_NETWORK_CONNECTE_FAILED);
            JniController.getInstance().excuteJNIVoidMethod("notifyWebSocketStatus", new Object[]{Boolean.valueOf(false)});

            if( LogUtil.nativeIsFLOG()) {
                LogUtil.nativeFLOG("CS getServerListFailed:" + _serverlist_retry);
            }
            if ( _serverlist_retry >= 1) {
                if( !LogUtil.isDebug ){
                    getServerListService.shutdown();
                    if( LogUtil.nativeIsFLOG()) {
                        LogUtil.nativeFLOG("CS start connect default");
                    }
                    connect2ws();
                    _serverlist_retry = 0;
                }
            }

        } else {
            serverListLoaded = true;
            if (canTestServer()) {
                testServers();
            }
            getServerListService.shutdown();
            connect2ws();
            _serverlist_retry = 0;
        }
    }


    private boolean testConfigLoaded = false;
    private boolean serverListLoaded = false;
    private int connectMode = 0;
    private  long screenLockTime;
    private boolean canTestServer() {
        return testConfigLoaded && serverListLoaded;
    }

    private static String getDeviceName() {
        final String deviceName = Build.MODEL;
        return deviceName;
    }

    private void connect2ws() {
        initWebSocketWithServierInfo();
    }

    public void connect2ws(String server, String port, String protocol) {
        try {
            connectMode = 1;
            isClearSocket = false;
            currentServer = new WSServerInfo(protocol, server, port);
            Log.d("WebSocketManager", "PrimaryServer:" + currentServer.address);
            statusListener.onConsoleOutput("Connecting server: " + currentServer);
            if( LogUtil.nativeIsFLOG()) {
                LogUtil.nativeFLOG("CS initWebSocketWithServierInfo:" + currentServer);
            }
            webSocketStart();
        } catch (Exception e) {
            connectMode = 0;
            e.printStackTrace();
        }
    }



    WSServerInfo currentServer;

    public WSServerInfo getCurrentServer() {
        return currentServer;
    }

    private void initWebSocketWithServierInfo() {
        try {
            if( connectMode == 0 ) {
                //设置当前连接的服务器
                currentServer = WSServerInfoManager.getInstance().selectPrimaryServer(serversInfos);

                if (currentServer == null || !currentServer.isValid()) {
                    currentServer = DEFAULT_SERVER;
                }
            }
            else if( connectMode == 1) {

            }

            Log.d("WebSocketManager", "PrimaryServer:" + currentServer.address);
            statusListener.onConsoleOutput("Connecting server: " + currentServer);
            if( LogUtil.nativeIsFLOG()) {
                LogUtil.nativeFLOG("CS initWebSocketWithServierInfo:" + currentServer);
            }
            webSocketStart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public String getCurrIPAndPortPriority(){
        String  result = " #";

        //获取优先级
        String priority = "";
        String commonPort = "8088";
        String backupPort = "80";
        ArrayList<WSServerInfo> serverList = WSServerInfoManager.getInstance().getPriorityServerArray();
        int count = serverList.size();//减少调用次数
        for( int i=0; i<count; i++){
            WSServerInfo wssi = serverList.get(i);
            if (currentServer.address.equals(wssi.address) && currentServer.port.equals(wssi.port)) {
                priority =  String.valueOf(i);
                break;
            }
        }

        if (!priority.equals("")){
            priority = "#"+priority;
        }

        if (currentServer.address.equals("130.211.142.173") && currentServer.port.equals(commonPort)) {
            result = "(a" + priority +")";
        }else if (currentServer.address.equals("104.199.222.82") && currentServer.port.equals(commonPort)) {
            result = "(b" + priority +")";
        }else if (currentServer.address.equals("130.211.108.199") && currentServer.port.equals(commonPort)) {
            result = "(c" + priority +")";
        }else if (currentServer.address.equals("104.196.115.113") && currentServer.port.equals(commonPort)) {
            result = "(d" + priority +")";
        }else if (currentServer.address.equals("112.74.137.206") && currentServer.port.equals(commonPort)) {
            result = "(e" + priority +")";
        }else if (currentServer.address.equals("112.74.137.206") && currentServer.port.equals(backupPort)) {
            result = "(f" + priority +")";
        }else if(currentServer.address.equals("120.76.84.159") && currentServer.port.equals(backupPort)){
            result = "(g" + priority +")";
        }else if(currentServer.address.equals("169.44.70.39") && currentServer.port.equals(backupPort)){
            result = "(h" + priority +")";
        }
        return result;
    }

    private void webSocketStart()
    {
        if( !currentServer.protocol.equals("tcp") ) {
            isUseMqtt = false;
        }
        if( isUseMqtt ) {
            short port = 1883;
            try {
                port = (short) Integer.parseInt(currentServer.port);
            } catch (Exception ex) {

            }

            mqclient.setServerAndPort(currentServer.address, port);
            statusListener.onConsoleOutput("Connecting");
            String statusWord = LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_CONNECTING);
            statusWord = statusWord + getCurrIPAndPortPriority();
            statusListener.onStatus(statusWord);
            ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_NETWORK_CONNECTING);
            JniController.getInstance().excuteJNIVoidMethod("notifyWebSocketStatus", new Object[]{Boolean.valueOf(false)});
            if( LogUtil.nativeIsFLOG()) {
                LogUtil.nativeFLOG("CS webSocketStart:" + currentServer.address + ":" + currentServer.port);
            }
            mqclient.setUid(UserManager.getInstance().getCurrentUserId());
            mqclient.setWebSocketManager(this);
            mqclient.setStatusListener(statusListener);
            mqclient.connectToServer();
            return;
        }
        try {
            WsClient  wsClient = new WsClient(currentServer.protocol + "://" + currentServer.address + ":" + currentServer.port, getHeader(), this, statusListener);
            client = wsClient;
            if (client != null) {
                statusListener.onConsoleOutput("Connecting");
                String statusWord = LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_CONNECTING);
                statusWord = statusWord + getCurrIPAndPortPriority();
                statusListener.onStatus(statusWord);
                ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_NETWORK_CONNECTING);
                JniController.getInstance().excuteJNIVoidMethod("notifyWebSocketStatus", new Object[]{Boolean.valueOf(false)});
                if( LogUtil.nativeIsFLOG()) {
                    LogUtil.nativeFLOG("CS webSocketStart:" + currentServer.address + ":" + currentServer.port + "_" + wsClient.hashCode());
                }
                client.connect();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getHeader() {
        Map<String, String> header = new HashMap<String, String>();
        long time = TimeManager.getInstance().getCurrentTimeMS();
        header.put("APPID", APP_ID);
        header.put("TIME", String.valueOf(TimeManager.getInstance().getCurrentTimeMS()));
        header.put("UID", UserManager.getInstance().getCurrentUserId());
        header.put("SIGN", calcSign(APP_ID, UserManager.getInstance().getCurrentUserId(), time));
        return header;
    }

    public void closeSocket() {
        if( LogUtil.nativeIsFLOG()) {
            LogUtil.nativeFLOG("CS closeSocket");
        }
        if(isUseMqtt) {
            mqclient.clearData();
            mqclient.disconnectWithServer();
            return;
        }

        if( client == null) {
            return;
        }
        final WsClient closeClient = client;
        client.setStatusListener(null);
        client.setWebSocketManager(null);
        client = null;
        closeSocket.execute( new Runnable()  {
            @Override
            public void run() {
                synchronized (this) {
                    if (closeClient != null) {
                        try {
                            closeClient.closeBlocking();
                        } catch (InterruptedException e) {
                            // TODO do something
                            LogUtil.printException(e);
                        }
                    }
                }
            }
        });
    }


   public void clearSocket(){
       if( LogUtil.nativeIsFLOG()) {
           LogUtil.nativeFLOG("CS clearSocket");
       }
       isClearSocket = true;
       synchronized (this) {
           reconnectCountDown = 0;
           reConnectCount = 0;
       }
       closeSocket();
    }


    private int reConnectCount = 0;
    private boolean isClearSocket = false; //是否清除socket状态
    private final static int RECONNECT_INTERVAL = 5;
    private int reconnectCountDown = 0;
    private int reconnectAdditionalInterval = -5;

    public void resetReconnectInterval() {
        reconnectAdditionalInterval = -5;
    }

    private final static int RECONNECT_MAX_RETRY = 99;

    private ScheduledExecutorService reconnectService;
    private TimerTask reconnectTimerTask;

    public synchronized void onConnectClose() {

        if(isUseMqtt) {
            synchronized (this) {
                //重置socket
                if(mqclient != null) mqclient.setStatusListener(null);
                if(mqclient != null) mqclient.setWebSocketManager(null);
                mqclient.clearData();
                mqclient.disconnectWithServer();


                if (isClearSocket == false && isForeground) {
                    startReconnect();
                }
            }
            return;
        }
        synchronized (this) {
            //重置socket
            if(client != null) client.setStatusListener(null);
            if(client != null) client.setWebSocketManager(null);
            client = null;

            if (isClearSocket == false && isForeground) {
                startReconnect();
            }
        }


    }

    public synchronized void onConnectError() {
        if(isUseMqtt) {
            synchronized (this) {
                //重置socket
                if(mqclient != null) mqclient.setStatusListener(null);
                if(mqclient != null) mqclient.setWebSocketManager(null);
                mqclient.clearData();
                mqclient.disconnectWithServer();


                if (isClearSocket == false && isForeground) {
                    startReconnect();
                }
            }
        }
        else {
            synchronized (this) {
                //重置socket
                if(client != null) client.setStatusListener(null);
                if(client != null) client.setWebSocketManager(null);
                client = null;

                if (isClearSocket == false && isForeground) {
                    startReconnect();
                }
            }
        }

        connectMode = 0;

        if( currentServer != null ) {
            JniController.getInstance().excuteJNIVoidMethod("sendServerStatus", new Object[]{currentServer.address,
                    currentServer.port, currentServer.protocol,2});
        }

    }

    /**
     * 游戏回到前台调用
     * @author lzh
     * @time 17/3/16 下午1:16
     */
    public synchronized void onEnterForeground() {
        isForeground = true;
    }

    /**
     * 游戏锁屏或回到后台调用
     * @author lzh
     * @time 17/3/16 下午1:17
     */
    public synchronized void onEnterBackground() {
        isForeground = false;
    }

    /**
     * 只会执行一次
     */
    private synchronized void startReconnect() {
        if( LogUtil.nativeIsFLOG()) {
            LogUtil.nativeFLOG("CS startReconnect");
        }
        if (reConnectCount == 0){
            reconnectCountDown = 1;
        }else if (reConnectCount == 1){
            reconnectCountDown = 2;
        } else if (reConnectCount == 2){
            reconnectCountDown = 2;
        }
        else if(reConnectCount >=3){
            reConnectCount=0;
            reconnectCountDown = 2;
            ChatServiceController.isNeedJoinLive = true;
            WSServerInfoManager.getInstance().addFailConnectServerInfoMap(currentServer);
        }

        if (reconnectService != null) {
            if( LogUtil.nativeIsFLOG()) {
                LogUtil.nativeFLOG("CS startReconnect:reconnectService != null");
            }
            return;
        }

        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
        reconnectService = Executors.newSingleThreadScheduledExecutor();
        reconnectTimerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    checkReconnect();
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        };

        reconnectService.scheduleWithFixedDelay(reconnectTimerTask, 100, 1000, TimeUnit.MILLISECONDS);
    }

    private synchronized void checkReconnect() {
        synchronized (this) {
            if (reconnectCountDown <= 0){
                return;
            }

            if( LogUtil.nativeIsFLOG()) {
                LogUtil.nativeFLOG("CS checkReconnect:" + reconnectCountDown);
            }
            reconnectCountDown--;
            if (reconnectCountDown <= 0) {
                resetState();
                reConnectCount++;
                if (serversInfos!=null&&serversInfos.size() > 0){
                    if( LogUtil.nativeIsFLOG()) {
                        LogUtil.nativeFLOG("CS checkReconnect:connect2ws");
                    }
                    connect2ws();
                }else {
                    if( LogUtil.nativeIsFLOG()) {
                        LogUtil.nativeFLOG("CS checkReconnect:startGetServerList");
                    }
                    startGetServerList();
                }
            }

            String statusWord = LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_CONNECTING);
            statusWord = statusWord + getCurrIPAndPortPriority();
            statusListener.onStatus(statusWord);
            ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_NETWORK_CONNECTING);
            JniController.getInstance().excuteJNIVoidMethod("notifyWebSocketStatus", new Object[]{Boolean.valueOf(false)});
        }
    }

    public void startPingCurrentServer()
    {
        if(currentServer == null)
        {
            return;
        }

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    double ping = NetworkUtil.testCurrentServer(currentServer.address);
                    JniController.getInstance().excuteJNIVoidMethod("csPingBack", new Object[] {Boolean.valueOf(isConnected()), Double.valueOf(ping) });
                }
                catch (Exception  e)
                {
                    LogUtil.printException(e);
                }
            }
        }).start();
    }

    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    public void onLoginSuccess(JSONObject json) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
        statusListener.onConsoleOutput("Login success");
        synchronized (this) {
            reConnectCount = 0;
        }
        try {
            if (json.optBoolean("enableNetworkOptimization")) {
                enableNetworkOptimization = json.getBoolean("enableNetworkOptimization");
            }
            if (json.opt("networkOptimizationTimeout") != null) {
               // networkOptimizationTimeout = json.optLong("networkOptimizationTimeout");
            }
            if (json.opt("networkOptimizationTestDelay") != null) {
                networkOptimizationTestDelay = json.optLong("networkOptimizationTestDelay");
            }
        } catch (Exception e) {
            LogUtil.printException(e);
        }

        testConfigLoaded = true;

        setUserInfo();
        joinRoom();

        if(ChatServiceController.getInstance().standalone_chat_room){
            getChatRoomList();
        }

        //聊天v2 加载聊天室-人数
        if (ChatServiceController.chat_v2_on){
            if (ServiceInterface.getServiceDelegate() != null) {
                ServiceInterface.getServiceDelegate().loadChatResources();
            }

            //加载多语言聊天室配置
            if (ChatServiceController.chat_language_on){
                ChatServiceController.initLanguageChatRoomConfig();

                //获取语言聊天室 - 人数
                if (ServiceInterface.getServiceDelegate() != null){
                    ServiceInterface.getServiceDelegate().loadChatRoomMembers();
                }
            }
        }
    }

    private void testServerAsSupervisor() {
        enableNetworkOptimization = true;
        networkOptimizationTestDelay = 3;
        testServers();
    }

    private static String calcSign(String appid, String uid, long time) {
        return MD5.stringMD5(MD5.stringMD5(appid + uid) + time);
    }

    private final static String SEND_USER_MSG_COMMAND = "chat.user";
    private final static String LOGIN_SUCCESS_COMMAND = "login.success";
    private final static String RECIEVE_USER_MSG_COMMAND = "push.chat.user";
    private final static String JOIN_ROOM_MULTI_COMMAND = "room.joinMulti";
    private final static String LEAVE_ROOM_COMMAND = "room.leave";
    private final static String SEND_ROOM_MSG_COMMAND = "chat.room";
    private final static String RECIEVE_ROOM_MSG_COMMAND = "push.chat.room";
    private final static String SET_USER_INFO_COMMAND = "user.setInfo";
    //  private final static String GET_NEW_MSGS_COMMAND                = "history.rooms";
    private final static String GET_NEW_MSGS_BY_TIME_COMMAND = "history.roomsv2";   //刷新最新消息
    //  private final static String GET_HISTORY_MSGS_COMMAND            = "history.room";
    private final static String GET_HISTORY_MSGS_BY_TIME_COMMAND = "history.roomv2";    //刷新历史消息
    private final static String ANOTHER_LOGIN_COMMAND = "another.login";

    //"chat.room.mk.v2"          // 创建聊天室(sfs)
    //"chat.room.dismiss"        // 群主退出解散房间(sfs)

    //cmd
    private final static String CMD_CHATROOM_INVITE     = "room.inviteCustomRoom";      // 邀请玩家加入
    private final static String CMD_CHATROOM_QUIT       = "room.quitCustomRoom";        // 非群主退出房间
    private final static String CMD_CHATROOM_KICK       = "room.kickCustomRoom";        // 聊天室踢人
    private final static String CMD_CHATOOM_CHANGENAME  = "room.changeCustomRoomName";  // 更改聊天室名称
    private final static String CMD_CHATROOM_GET_LIST   = "room.getCustomRoomList";     // 获取自己的房间信息，独立聊天接受|即使返回
    private final static String CMD_CHATROOM_BATTLE_QUIT       = "room.quitBattleFieldCustomRoom";        // 退出竞技场房间
    private final static String CMD_CHATROOM_GETROOMMEMBERCOUNT = "room.getRoomMembersCount";
    private final static String CMD_CHATROOM_BANUSERFORLIVE ="user.banUserForRoom";
    private final static String CMD_CHATROOM_UNBANUSERFORLIVE ="user.unbanUserForRoom";
    private final static String CMD_CHATROOM_GETBANUSERFORLIVE ="user.getBanUserForRoom";
    //push
    private final static String PUSH_CHATROOM_CREATE    = "push.chatroom.create";       // 创建聊天室,独立聊天push
    private final static String PUSH_CHATROOM_INVITE    = "push.room.invite";           // 邀请玩家加入,独立聊天push
    private final static String PUSH_CHATROOM_QUIT      = "push.room.quit";             // 非群主退出房间,独立聊天push
    private final static String PUSH_CHATROOM_DISMISS   = "push.room.dismiss";          // 群主解散房间,独立聊天push
    private final static String PUSH_CHATROOM_KICK      = "push.room.kick";             // 聊天室踢人,独立聊天push
    private final static String PUSH_CHATOOM_CHANGENAME = "push.room.changename";       // 更改聊天室名称,独立聊天push
    private final static String PUSH_CHATROOM_GET_LIST  = "room.getCustomRoomList";     // 获取自己的房间信息，独立聊天接受|即使返回


    private final static String CMD_LANGUAGE_CHATROOM_JOIN     = "room.joinLangRoom";           // 玩家加入语言聊天室
    private final static String PUSH_LANGUAGE_CHATROOM_JOIN     = "push.room.join";             // 玩家加入语言聊天室
    private final static String CMD_CHATROOMS_GETROOMMEMBERCOUNT = "room.getRoomsMembersCount"; //获取聊天室人数

    public void setUserInfo() {
        sendCommand(SET_USER_INFO_COMMAND, "info", getUserInfo());
    }

    private JSONObject getUserInfo() {
        try {
            JSONObject params = new JSONObject();
            params.put("userName", UserManager.getInstance().getCurrentUser().userName);
            params.put("lastUpdateTime", UserManager.getInstance().getCurrentUser().lastUpdateTime);
//          params.put("sendTime", Integer.toString(TimeManager.getInstance().getCurrentTime()));
//          params.put("lang", ConfigManager.getInstance().gameLang);

            LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_VIEW, "userName", UserManager.getInstance().getCurrentUser().userName);
            return params;
        } catch (JSONException e) {
            LogUtil.printException(e);
        }
        return null;
    }

    public void sendUserMsg() {
        sendCommand(SEND_USER_MSG_COMMAND, "uid", UserManager.getInstance().getCurrentUserId(), "msg", "test msg");
    }

    private String rooms = "";
    private boolean roomsChanged;

    public void joinRoom() {
        JSONArray roomsArr = getJoinRoomsArray();
        if (!rooms.equals(roomsArr.toString())) {
            rooms = roomsArr.toString();
            roomsChanged = true;
        } else {
            roomsChanged = false;
        }
        sendCommand(JOIN_ROOM_MULTI_COMMAND, "rooms", roomsArr);
    }

    public void joinLiveRoom(String roomId) {
        JSONArray roomsArr = null;
        try {
            roomsArr = new JSONArray();
            JSONObject live = new JSONObject();
            live.put("id", roomId);
            live.put("group", "custom");
            roomsArr.put(live);
        }catch (Exception e){
            return;
        }
        sendCommand(JOIN_ROOM_MULTI_COMMAND, "rooms", roomsArr);
    }

    private void onJoinRoom() {
        statusListener.onStatus("");
        ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_NETWORK_CONNECTED);
        JniController.getInstance().excuteJNIVoidMethod("notifyWebSocketStatus", new Object[] {Boolean.valueOf(true) });
        getNewMsgs();
    }

    private void resetState() {
        rooms = "";
    }

    private JSONArray getJoinRoomsArray() {
        JSONArray array = null;
        try {
            array = new JSONArray();
            if (!ConfigManager.isEnterArena) {//竞技场不加入国家聊天房间
                JSONObject country = new JSONObject();
                country.put("id", getCountryRoomId());
                country.put("group", "country");
                array.put(country);
            }
            if (UserManager.getInstance().isCurrentUserInAlliance()) {
                JSONObject alliance = new JSONObject();
                alliance.put("id", getAllianceRoomId());
                alliance.put("group", "alliance");
                array.put(alliance);
            }
        } catch (JSONException e) {
            LogUtil.printException(e);
        }
        return array;
    }

    public void leaveAllianceRoom() {
        if (UserManager.getInstance().getCurrentUser().allianceId == "") {
            return;
        }
        sendCommand(LEAVE_ROOM_COMMAND, "roomId", getAllianceRoomId());
    }

    public void leaveCountryRoom() {
        sendCommand(LEAVE_ROOM_COMMAND, "roomId", getCountryRoomId());
    }

    public void leaveLiveRoom(){
        sendCommand(LEAVE_ROOM_COMMAND, "roomId", getLiveRoomId());
    }

    // 邀请
    public void chatRoomInvite(String roomId, String members) {
        sendCommand(CMD_CHATROOM_INVITE, "roomId", roomId, "members", members, "group", "custom");
    }

    // 获取自己的房间信息，登陆时调用
    public void getChatRoomList(){
        sendCommand(CMD_CHATROOM_GET_LIST, "group", "custom");
    }

    public void getRoomMembersCount(String roomId){
        sendCommand(CMD_CHATROOM_GETROOMMEMBERCOUNT, "roomId", roomId, "group", "custom");
        LogUtil.printVariables(100,"WB",roomId);
    }

    //roomId|roomId|...
    public void getRoomsMembersCount(String roomids){
        sendCommand(CMD_CHATROOMS_GETROOMMEMBERCOUNT, "roomids", roomids, "group", "custom");
    }

    public void banLiveMember(String roomId, String memberUid, long time){
        sendCommand(CMD_CHATROOM_BANUSERFORLIVE, "roomId", roomId,"targetUid",memberUid, "time", time, "group", "custom");
    }

    public void unBanLiveMember(String roomId, String memberUid){
        sendCommand(CMD_CHATROOM_UNBANUSERFORLIVE, "roomId", roomId,"targetUid",memberUid, "group", "custom");
    }

    public void getBanUserForRoom(String roomId){
        sendCommand(CMD_CHATROOM_GETBANUSERFORLIVE, "roomId", roomId, "group", "custom");
    }
    // 成员退出聊天室
    public void chatRoomQuit(String roomId) {
        sendCommand(CMD_CHATROOM_QUIT, "roomId", roomId, "group", "custom");
    }

    // 退出竞技场聊天室
    public void arenaChatRoomQuit(String roomId) {
        sendCommand(CMD_CHATROOM_BATTLE_QUIT, "roomId", roomId, "group", "custom");
    }

    // 聊天室改名
    public void chatRoomChangeName(String roomId, String roomName){
        sendCommand(CMD_CHATOOM_CHANGENAME, "roomId", roomId, "name", roomName, "group", "custom");
    }

    // 聊天室踢人
    public void chatRoomKick(String roomId, String members) {
        sendCommand(CMD_CHATROOM_KICK, "roomId", roomId, "members", members, "group", "custom");
    }


    // 进入语言聊天室
    public void languageChatRoomInvite(String roomId, String roomName) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "send joinLangRoom succeed:", roomId);
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "send joinLangRoom time:", TimeManager.getInstance().getCurrentTimeMS());
        sendCommand(CMD_LANGUAGE_CHATROOM_JOIN, "roomId", roomId, "name", roomName, "group", "custom");
    }

    public void sendChatRoomMsg(String messageText, int sendLocalTime, ChatChannel channel, int post, String media, int isLiveChat) {
        sendCommand(SEND_ROOM_MSG_COMMAND, "roomId", channel.channelID, "msg", messageText, "sendTime", sendLocalTime, "isLiveChat",isLiveChat, "extra", getMsgExtra(post, media));
    }

    public void sendRoomMsg(String messageText, int sendLocalTime, ChatChannel channel) {
        sendRoomMsg(messageText, sendLocalTime, channel, MsgItem.MSGITEM_TYPE_MESSAGE, null);
    }

    public void sendRoomMsg(String messageText, int sendLocalTime, ChatChannel channel, int post, String media) {
        String roomId = channel.isCountryChannel() ? getCountryRoomId() : getAllianceRoomId();
        if (channel.channelType == 0){
            JniController.getInstance().excuteJNIVoidMethod("nativeFbEventDone", new Object[]{"social_chat_alliance", ""});
        }else if (channel.channelType == 1){
            JniController.getInstance().excuteJNIVoidMethod("nativeFbEventDone", new Object[]{"social_chat_country", ""});
        }
        sendCommand(SEND_ROOM_MSG_COMMAND, "roomId", roomId, "msg", messageText, "sendTime", sendLocalTime, "extra", getMsgExtra(post, media));
    }

    public void sendRoomMsg(String messageText, int sendLocalTime, String roomId, int post, String media) {
//        String roomId = channel.isCountryChannel() ? getCountryRoomId() : getAllianceRoomId();
//        if (channel.channelType == 0){
//            JniController.getInstance().excuteJNIVoidMethod("nativeFbEventDone", new Object[]{"social_chat_alliance", ""});
//        }else if (channel.channelType == 1){
//            JniController.getInstance().excuteJNIVoidMethod("nativeFbEventDone", new Object[]{"social_chat_country", ""});
//        }
        sendCommand(SEND_ROOM_MSG_COMMAND, "roomId", getCountryRoomId(), "msg", messageText, "sendTime", sendLocalTime, "extra", getMsgExtra(post, media));
    }

    private JSONObject getMsgExtra(int post, String media) {
        JSONObject extra = null;
        try {
            extra = new JSONObject();
            if (post != MsgItem.MSGITEM_TYPE_MESSAGE) extra.put("post", post);
            if (StringUtils.isNotEmpty(media)) extra.put("media", media);
        } catch (Exception e) {
            LogUtil.printException(e);
        }
        return extra;
    }

    /**
     * 后台返回包括db最后时间在内（可能有时间一样的新消息）的所有新数据（或至多N条数据）
     */
    public void getNewMsgs() {
        if (roomsParams != null) {
            sendCommand(GET_NEW_MSGS_BY_TIME_COMMAND, "rooms", roomsParams);
            roomsParams = null;
        } else {
            sendCommand(GET_NEW_MSGS_BY_TIME_COMMAND, "rooms", getRoomsParams());
        }
    }

    private JSONObject getRoomsParams() {
        JSONObject params = null;
        try {
            params = new JSONObject();
            ArrayList<ChatChannel> channels = new ArrayList<ChatChannel>(); // ChannelManager.getInstance().getNewServerChannels()
            channels.add(ChannelManager.getInstance().getCountryChannel());
            channels.add(ChannelManager.getInstance().getAllianceChannel());
            if(!ChatServiceController.curLiveRoomId.equals("")){
                channels.clear();
                ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_CHATROOM, getLiveRoomId());
                if(channel != null){
                    channels.add(channel);
                }
            }
            for (int i = 0; i < channels.size(); i++) {
                ChatChannel channel = channels.get(i);
                if (channel == null) continue;
                if (channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY) {
                    if (!ConfigManager.isEnterArena) {
                        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_SEND, "latestTime", TimeManager.getTimeInMS(channel.getLatestTime()));
                        statusListener.onConsoleOutput("latestTime = " + channel.getLatestTime());
                        statusListener.onConsoleOutput("latestTime = " + TimeManager.getTimeInMS(channel.getLatestTime()));
                        params.put(getCountryRoomId(), TimeManager.getTimeInMS(channel.getLatestTime()));
                    }
                } else if (channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE) {
                    params.put(getAllianceRoomId(), TimeManager.getTimeInMS(channel.getLatestTime()));
                } else if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM) {
                    params.put(getLiveRoomId(), TimeManager.getTimeInMS(channel.getLatestTime()));
                }
            }
        } catch (JSONException e) {
            LogUtil.printException(e);
        }
        return params;
    }

    public void getHistoryMsgs(String roomId, long startTime, long endTime) {
        sendCommand(GET_HISTORY_MSGS_BY_TIME_COMMAND, "roomId", roomId, "start", 0, "end", endTime);
    }

    public static boolean isWebSocketEnabled() {
        // || ChatServiceController.isBetaVersion()
        return ConfigManager.useWebSocketServer;// && (ChatServiceController.isInnerVersion() || ChatServiceController.getInstance().isUsingDummyHost());
    }

    public static boolean isRecieveFromWebSocket(int channelType) {
        return isWebSocketEnabled() && ConfigManager.isRecieveFromWebSocket
                && isSupportedType(channelType);
    }

    public static boolean isSendFromWebSocket(int channelType) {
        return isWebSocketEnabled() && (ConfigManager.isSendFromWebSocket || ChatServiceController.getInstance().isUsingDummyHost())
                && isSupportedType(channelType);
    }

    public static boolean isSupportedType(int channelType) {
        if(ChatServiceController.getInstance().standalone_chat_room){
            return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE || channelType == DBDefinition.CHANNEL_TYPE_CHATROOM;
        }else {
            return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE;
        }

    }

    public boolean isConnected() {
        if(isUseMqtt) {
            return mqclient.getStatus() > 0;
        }
        return client != null && client.isOpen();
    }

    public void sendCommand(String command, Object... args) {
        if (!isConnected()) return;

        statusListener.onConsoleOutput("send: " + command);
        try {
            JSONObject params = new JSONObject();
            for (int i = 0; i < args.length; i += 2) {
                if ((i + 1) < args.length) {
                    params.put((String) args[i], args[i + 1]);
                }
            }
            actualSendCommand(command, params);
        } catch (JSONException e) {
            LogUtil.printException(e);
        }
    }

    private void actualSendCommand(String command, JSONObject params) throws JSONException {
        if (!isConnected()) return;

        JSONObject jsonobj = new JSONObject();
        jsonobj.put("cmd", command);
        if (params.has("sendTime")) {
            jsonobj.put("sendTime", params.getInt("sendTime"));
            params.remove("sendTime");
        } else {
            long time = TimeManager.getInstance().getCurrentTimeMS();
            jsonobj.put("sendTime", time);
        }

        jsonobj.put("params", params);

        String output = String.format("%s: %s", command, jsonobj.toString());
        LogUtil.printVariables(Log.INFO, LogUtil.TAG_WS_SEND, output);
        statusListener.onConsoleOutput(output);

        if(isUseMqtt) {
            mqclient.sendDataToServer(command,jsonobj.toString());
        }
        else {
            if (client != null) {
                client.send(jsonobj.toString());
            }
        }

    }

    public void handleMessage(String message) {
        if (message.equals("heartbeat")) {
            LogUtil.printVariables(Log.INFO, LogUtil.TAG_WS_RECIEVE, "heartbeat");
            return;
        }

        try {
            JSONObject json = new JSONObject(message);
            String command = json.getString("cmd");

            String output = String.format("%s: %s", command, message);
            LogUtil.printVariables(Log.INFO, LogUtil.TAG_WS_RECIEVE, output);
//          statusListener.onConsoleOutput(output);

            if(isUseMqtt ) {
                if ( mqclient.isMyMessage(json)) {
                    return;
                }
            }
            else {
                if (client != null && client.isMyMessage(json)) {
                    return;
                }
            }

            if (json.has("data")) // 由服务端主动推送的数据
            {
                statusListener.onConsoleOutput("push: " + command);
                onRecieveMessage(message);
            } else
            // 客户端发送命令时，服务端处理完命令推送的数据
            {
                if (json.has("result")) {
                    statusListener.onConsoleOutput("send success: " + command);
                    onCommandSuccess(message);
                } else if (json.has("error")) // 发生错误
                {
//                  ServiceInterface.flyHint(null, "", command + " error: " + message, 0, 0, false);
                    statusListener.onConsoleOutput("send error: " + command);
                }
            }
        } catch (JSONException e) {
            LogUtil.printVariables(Log.INFO, LogUtil.TAG_WS_RECIEVE, "JSONException: " + message);
            LogUtil.printException(e);
        }
    }

    private void onCommandSuccess(String message) {
        try {

            JSONObject json = new JSONObject(message);
            String command = json.getString("cmd");
            JSONObject result = json.getJSONObject("result");

            if (command.equals(JOIN_ROOM_MULTI_COMMAND)) {
                if (result.optBoolean("status") && result.getBoolean("status")) {
                    onJoinRoom();
                }
            } else if (command.equals(GET_NEW_MSGS_BY_TIME_COMMAND)) {
                onGetNewMsg(result);
            }else  if(command.equals(GET_HISTORY_MSGS_BY_TIME_COMMAND)){
                onGetOldMsg(result);
            }else if (command.equals(PUSH_CHATROOM_GET_LIST)) {
                updateChatRoomList(result);
            }else if (command.equals(CMD_CHATROOM_GETROOMMEMBERCOUNT)){
                onRefreshLiveMember(result);
            }else if (command.equals(CMD_CHATROOMS_GETROOMMEMBERCOUNT)){
                onRefreshChatRoomMember(result);
            }else if(command.equals(CMD_CHATROOM_GETBANUSERFORLIVE)
                    || command.equals(CMD_CHATROOM_BANUSERFORLIVE)
                    || command.equals(CMD_CHATROOM_UNBANUSERFORLIVE)){
                onGetLiveBanUserData(result ,command);
            }else if(command.equals(SEND_ROOM_MSG_COMMAND)){
                onRecieveRoomMessage(result);
            }

        } catch (JSONException e) {
            LogUtil.printException(e);
        }
    }

    private void onGetServerList(String serverlist) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "serverlist", serverlist);
        statusListener.onConsoleOutput("onGetServerList:" + serverlist);

        try {
            JSONObject json = new JSONObject(serverlist);

            if (json.opt("data") != null && json.getJSONArray("data") instanceof JSONArray) {
                serversInfos = new ArrayList<WSServerInfo>();
                JSONArray datas = json.getJSONArray("data");
                for (int i = 0; i < datas.length(); i++) {
                    JSONObject data = datas.getJSONObject(i);
                    if (isStringExist(data, "protocol") && isStringExist(data, "ip") && isStringExist(data, "port")) {
                        if( data.getString("ip").equals("120.76.84.159")) {
                            boolean isChina =  JniController.getInstance().excuteJNIMethod("isChina", new Object[]{});
                            if(!isChina) {
                                continue;
                            }
                        }
                        WSServerInfo server = new WSServerInfo(data.getString("protocol"), data.getString("ip"), data.getString("port"));
                        serversInfos.add(server);
                    }
                }
            }
        } catch (JSONException e) {
            LogUtil.printException(e);
        }
    }

    private void onRecieveMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String command = json.getString("cmd");

            if (command.equals(PUSH_CHATROOM_DISMISS)) {
                String roomId = json.getString("data");
                ServiceInterface.updateChannelMemberArray(DBDefinition.CHANNEL_TYPE_CHATROOM, roomId, "", 3);
                ChatServiceController.curAreaRoomId = "";
            }
            else{
                JSONObject data = json.getJSONObject("data");
                if (command.equals(RECIEVE_USER_MSG_COMMAND)) {

                } else if (command.equals(RECIEVE_ROOM_MSG_COMMAND)) {
                    onRecieveRoomMessage(data);
                } else if (command.equals(ANOTHER_LOGIN_COMMAND)) {
                    // 同一个uid在不同地方登陆会这样，发生这种情况游戏应该就不让登陆了
                    statusListener.onStatus(LanguageManager.getLangByKey(LanguageKeys.ANOTHER_LOGIN));
                    ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_SERVER_DISCONNECTED);
                    clearSocket();
                } else if (command.equals(LOGIN_SUCCESS_COMMAND)) {
                    if(isUseMqtt) {
                        mqclient.setClientID(json);
                    }
                    else {
                        client.setClientID(json);
                    }


                    this.onLoginSuccess(json);
                } else if (command.equals(PUSH_CHATROOM_CREATE)
                        || command.equals(PUSH_CHATOOM_CHANGENAME)
                        || command.equals(PUSH_CHATROOM_INVITE)
                        || command.equals(PUSH_CHATROOM_KICK)
                        || command.equals(PUSH_CHATROOM_QUIT)
                        || command.equals(PUSH_LANGUAGE_CHATROOM_JOIN)) {
                    setChatRoomChannel(json);
                }
            }

        } catch (JSONException e) {
            LogUtil.printException(e);
        }
    }

    /**
     * {"cmd":"push.chat.room","serverTime":1447749281156,
     * "data":{"appId":"100001"
     * ,"seqId":2,"sender":"909504798000489","roomId":"country_1",
     * "msg":"vvvb","sendTime":1447749243505,"serverTime":1447749281155} }
     */
    private void onRecieveRoomMessage(JSONObject data) {
        try {

            if(data.getString("roomId").contains("live_") && data.has("error")){
                long banTime = data.getLong("expireTime");
                String tipStr = "";
                if (banTime > 3600*24){
                    tipStr = LanguageManager.getLangByKey("171307");
                }else{
                    tipStr = LanguageManager.getLangByKey("105201", "", TimeManager.getInstance().getTimeFormatWithRemainTime((int)banTime + TimeManager.getInstance().getCurrentTime()));
                }
                ServiceInterface.flyHint("","",tipStr,3,0,false);
                return;
            }

            // 聊天室消息单独处理
            if ((data.getString("group").equals("custom") || data.getString("group").equals("live"))){
                formatMsgForChatRoom(data, -1);
                return;
            }

            MsgItem item = parseMsgItem(data,true);
            if (item != null) {
                MsgItem[] dbItemsArray = {item};
                String customName = "";
                ChatChannel channel = getChannel(data.getString("group"));
                if(channel!=null)
                {
                    ServiceInterface.handleMessage(dbItemsArray, channel.channelID, customName, false, true);
                }


                if(item.isHornMessage())
                {
                    try
                    {
                        LatestHornMessage hornMsg = new LatestHornMessage();
                        hornMsg.setAsn(item.getASN());
                        hornMsg.setChannelType("" + item.channelType);
                        hornMsg.setMsg(item.msg);
                        hornMsg.setName(item.getName());
                        hornMsg.setUid(item.uid);
                        String latestHornMsg = JSON.toJSONString(hornMsg);
                        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "latestHornMsg", latestHornMsg);
                        if (StringUtils.isNotEmpty(latestHornMsg))
                            JniController.getInstance().excuteJNIVoidMethod("postNewHornMessage", new Object[] { latestHornMsg });
                    }
                    catch (com.alibaba.fastjson.JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.printException(e);
        }
    }

    public static void onRefreshLiveMember(JSONObject data){
        try {
            if(data.getInt("rooms") >= 0){
                ChatServiceController.listenRoomNumber = data.getInt("rooms");
                ChatServiceController.postAndRefreshLiveNumber();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //聊天室-人数
    public static void onRefreshChatRoomMember(JSONObject data){
        try {
            JSONArray array = data.getJSONArray("rooms");
            if(array != null){
                //加载聊天室-人数
                if (ServiceInterface.getServiceDelegate() != null){
                    ServiceInterface.getServiceDelegate().pushChatRoomMembers(array);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void onGetLiveBanUserData(JSONObject data ,String command){
        try {
            Object result = null;
            if(data.has("result"))
                result = data.get("result");
            if(result == null)
                return;
            if(result instanceof Boolean) {
                return;
            }
            if(result instanceof String) {
                if (command.equals(CMD_CHATROOM_BANUSERFORLIVE)) {
                    UserManager.getInstance().addLiveBanUser(String.valueOf(result));
                    ServiceInterface.flyHint("","",LanguageManager.getLangByKey(LanguageKeys.MENU_UNBAN),2,0,false);
                } else if (command.equals(CMD_CHATROOM_UNBANUSERFORLIVE)) {
                    UserManager.getInstance().removeLiveBanUser(String.valueOf(result));
                    ServiceInterface.flyHint("","",LanguageManager.getLangByKey(LanguageKeys.TIP_UNBAN),2,0,false);
                }
            }
             if (command.equals(CMD_CHATROOM_GETBANUSERFORLIVE)) {
                 if(result instanceof JSONObject){
                     JSONArray array = ((JSONObject)result).names();
                    UserManager.getInstance().initLiveBanUserUid(array);
                }
            }
            ChatServiceController.refreshBanListData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static boolean isStringExist(JSONObject obj, String key) {
        try {
            return obj.opt(key) != null && StringUtils.isNotEmpty(obj.getString(key));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private MsgItem parseMsgItem(JSONObject msg,boolean isNewMsg) {
        try {
            MsgItem item = new MsgItem();

            // 除了从db获取，都为true
            item.isNewMsg = isNewMsg;

            item.sequenceId = -1;

            item.uid = msg.getString("sender");
            item.msg = msg.getString("msg");

            item.roomId = msg.getString("roomId");
            //联盟三部曲暂时处理
            int findI = item.msg.indexOf("_sanbuqujp20150921", 0);
            if (findI > 0) {
                String dialog = item.msg.substring(0, findI);
                item.msg = LanguageManager.getLangByKey(dialog);
            }


            try {
                // sendTime可能为字符串或long，不会出错，但预防性加个try
                if (isStringExist(msg, "sendTime")) {
                    item.sendLocalTime = TimeManager.getTimeInS(msg.getLong("sendTime"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            item.createTime = TimeManager.getTimeInS(msg.getLong("serverTime"));
            item.channelType = group2channelType(msg.getString("group"));

            if (msg.opt("originalLang") != null) {
                item.originalLang = msg.getString("originalLang");
            }
            if (msg.opt("translationMsg") != null) {
                item.translateMsg = msg.getString("translationMsg");
            }

            item.post = 0;
            item.mailId = "";

            if (msg.optJSONObject("senderInfo") != null) {
                JSONObject senderInfo = msg.getJSONObject("senderInfo");
                if (isStringExist(senderInfo, "lastUpdateTime")) {
                    try {
                        item.lastUpdateTime = Integer.parseInt(senderInfo.getString("lastUpdateTime"));
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
                if (senderInfo.opt("userName") != null) {
                    item.name = senderInfo.getString("userName");
                }
            }
            JSONObject extra = null;
            if (msg.optJSONObject("extra") != null) {
                extra = msg.getJSONObject("extra");

                if (extra.opt("seqId") != null && extra.getInt("seqId") > 0) {
                    item.sequenceId = extra.getInt("seqId");
                }

                if (isStringExist(extra, "lastUpdateTime")) {
                    try {
                        item.lastUpdateTime = Integer.parseInt(extra.getString("lastUpdateTime"));
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
                if (extra.opt("post") != null) {
                    item.post = extra.getInt("post");
                } else {
                    item.post = 0;
                }
                if (item.isWoundedShare() && StringUtils.isNotEmpty(item.msg))
                {
                    item.msg = LanguageManager.getLangByKey(item.msg);
                }
                if (extra.opt("shareComment") != null)
                {
                    item.shareComment = extra.getString("shareComment");
                }
                if (extra.opt("scienceType") != null)
                {
                    item.shareComment = extra.getString("scienceType");
                }
                if (extra.opt("media") != null) {
                    item.media = extra.getString("media");
                }
                try {
                    //对extra 进行解析获得attachment
                    if(item.isSystemMessageByKey()) {
                        parseAttachmentWithExtra(extra, item);
                    }else{
                        parseAttachment(extra, item);
                    }
                } catch (Exception e) {
                    LogUtil.printException(e);
                }

                if (extra.opt("dialog") != null) {
                    String dialog = extra.getString("dialog");
                }
                if (extra.opt("inviteAlliance") != null) {
                    String inviteAlliance = extra.getString("inviteAlliance");
                }
                if (extra.optJSONArray("msgarr") != null) {
                    JSONArray msgarr = extra.getJSONArray("msgarr");
                }
                if (extra.opt("reportDef") != null) {
                    String reportDef = extra.getString("reportDef");
                }
                if (extra.opt("reportAtt") != null) {
                    String reportAtt = extra.getString("reportAtt");
                }
            }
            item.sequenceId = msg.getInt("seqId");
            if (!item.isRedPackageMessage() && !(!item.isSelfMsg() && item.isAudioMessage()))
                item.sendState = MsgItem.SEND_SUCCESS;

            if (item.sequenceId == -1) {
                LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "item.sequenceId", item.sequenceId);
            }

            return item;
        } catch (Exception e) {
            LogUtil.printException(e);
        }
        return null;
    }

    private void parseAttachment(JSONObject extra, MsgItem item) throws JSONException {
        //疯狂科学家：
        parseAttachmentWithDialog(extra, item, "dialog");
        // 联盟加入：allianceId
        parseAttachment(extra, item, "allianceId");
        // 战报：reportUid
        parseAttachment(extra, item, "reportUid");
        // 侦察战报：detectReportUid
        parseAttachment(extra, item, "detectReportUid");
        // 装备：equipId
        parseAttachment(extra, item, "equipId");
        // 集结：teamUid
        parseAttachment(extra, item, "teamUid");
        // 转盘：lotteryInfo
        parseAttachment(extra, item, "lotteryInfo");
        // 地块分享：attachmentId
        parseAttachment(extra, item, "attachmentId");
        // 沙漠天赋：shamoInfo
        parseAttachment(extra, item, "shamoInfo");

        // 注意：android和ios做法不一样, status 用sendState保存！！！！！！！！！！！！！！！！！
        // 系统红包：红包id_serverid|redPackectSysType
        // 玩家红包：红包id_serverid
        // "redPackectSysType" 0 普通 1大本升级  2节日定时
        if (extra.opt("redPackets") != null && extra.opt("server") != null) {
            item.attachmentId = extra.getString("redPackets") + "_" + extra.getString("server") + "";
            item.sendState = MsgItem.UNHANDLE;
            if (extra.opt("dialog") != null && extra.opt("msgarr") != null) {
                if(extra.opt("redPackectSysType") != null) {
                    item.attachmentId += "|";
                    item.attachmentId += extra.getString("redPackectSysType");
                }else{
                    item.attachmentId += "|1";//兼容老版本
                }
            }
            if(item.msg.equals("90100001")){
                item.msg = LanguageManager.getLangByKey("90100001");
            }
        }

        if(item.post == MSG_TYPE_NEWS_CENTER_SHARE && extra.opt("msgarr") != null){
            JSONArray paramsArray = extra.getJSONArray("msgarr");
            String nameString = "";
            String attachmentID = "";
            int count = paramsArray.length();
            for (int i = 0; i< count ; i++) {
                String namesStr = paramsArray.getString(i);
                if (nameString.length() > 0) {
                    if(i < count-2){
                        nameString = nameString.concat("||");
                        nameString = nameString.concat(namesStr);
                    }else if(i == count-2){
                        attachmentID = attachmentID.concat(namesStr);
                    }else if(i == count-1){
                        attachmentID = attachmentID.concat("_");
                        attachmentID = attachmentID.concat(namesStr);
                    }

                }else{
                    nameString = nameString.concat(namesStr);
                }
            }
            attachmentID = attachmentID + "_";
            attachmentID = attachmentID + nameString;
            item.attachmentId = attachmentID;
        }
        if (item.isFavourPointShare() && StringUtils.isNotEmpty(item.attachmentId))
        {
            String attachmentIdStr = item.attachmentId;
            if (StringUtils.isNotEmpty(attachmentIdStr))
            {
                String[] taskInfo = attachmentIdStr.split("\\|");
                if (StringUtils.isNotEmpty(taskInfo[0]))
                {
                    String nomalStr="(#"+taskInfo[1]+" "+taskInfo[2]+":"+taskInfo[3]+")";
                    if(taskInfo[0].equals("81000312") || taskInfo[0].equals("81000327")){
                        String msgStr = "";
                        if(item.msg.equals("145538")){
                            msgStr = LanguageManager.getLangByKey(item.msg);
                        }else{
                            msgStr = item.msg;
                        }
                        item.msg = LanguageManager.getLangByKey(taskInfo[0], msgStr, nomalStr);
                    }else if(taskInfo[0].equals("81000326")){
                        item.msg = LanguageManager.getLangByKey(taskInfo[0], taskInfo[4], taskInfo[5], nomalStr);
                    }else if(taskInfo[0].equals("81000324") || taskInfo[0].equals("81000325")){
                        String msgStr = LanguageManager.getLangByKey(taskInfo[5]);
                        item.msg = LanguageManager.getLangByKey(taskInfo[0], taskInfo[4], msgStr, nomalStr);
                    }
                    else if (taskInfo[0].equals("99010063")) {
                        item.msg = LanguageManager.getLangByKey(taskInfo[0], taskInfo[4], taskInfo[5], taskInfo[2],taskInfo[3]);
                    }
                    else if (taskInfo[0].equals("99010079")) {
                        item.msg = LanguageManager.getLangByKey(taskInfo[0],taskInfo[2],taskInfo[3]);
                    }
                }
            }
        }
    }

    private void parseAttachment(JSONObject extra, MsgItem item, String propName) throws JSONException {
        if (extra.opt(propName) != null) {
            if(StringUtils.isNotEmpty(item.attachmentId) && item.isNeedParseAttachmentId()) {
                item.attachmentId = extra.getString(propName).concat("__").concat(item.attachmentId);
                ChatServiceController.isNewMsg = true;
            }else if(StringUtils.isNotEmpty(item.attachmentId) && item.isShamoInhesionShare())
            {
                item.attachmentId = extra.getString(propName);
                ChatServiceController.isNewMsg = false;
            }
            else if(StringUtils.isEmpty(item.attachmentId)){
                item.attachmentId = extra.getString(propName);
                ChatServiceController.isNewMsg = false;
            }
        }
    }

    /**
     * @param extra extra中的参数用于拼接attachment
     * @param item
     * @throws JSONException
     */
    private void parseAttachmentWithExtra(JSONObject extra, MsgItem item) throws JSONException {
        //暂时先处理有问题的分享,
        //坐标分享、装备分享
        if ( extra.opt("allianceId") != null){
            //加入联盟
            item.attachmentId =extra.getString("allianceId");
        }
        else if( extra.opt("reportUid") != null){
            //侦查分享
            item.attachmentId =  extra.getString("reportUid");
        }
        else if( extra.opt("detectReportUid") != null){
            item.attachmentId =  extra.getString("detectReportUid");
        }
        else if( extra.opt("equipId") != null){
            //装备分享
            item.attachmentId =  extra.getString("equipId");
        }
        else if( extra.opt("teamUuid") != null){
            //集结消息
            item.attachmentId =  extra.getString("teamUuid");
        }
        else if( extra.opt("lotteryInfo") != null){
            //转盘分享
            item.attachmentId =  extra.getString("lotteryInfo");
        }
        else if( extra.opt("attachmentId") != null){
            item.attachmentId =  extra.getString("attachmentId");
        }else if( extra.opt("shamoInfo") != null){
            //沙漠分享
            item.attachmentId =  extra.getString("shamoInfo");
        }else if(extra.opt("scienceType") != null){
            //沙漠分享
            item.attachmentId =  extra.getString("scienceType");
        }
        if(item.post == MsgItem.MSG_TYPE_FAVOUR_POINT_SHARE && extra.opt("shareComment") != null){
            //分享文字
            item.shareComment =  extra.getString("shareComment");
        }

        //类型全部列出来,防止遗漏
//        if(item.isBattleReport() || item.isDetectReport() || item.isRallyMessage()){
//        }
//        else if(item.isLotteryMessage()){ //射击训练场
//        }
//        else if (item.isSevenDayMessage()){
//            //  转盘分享
//
//        }
//        else if(item.isFavourPointShare() || item.isAllianceTreasureMessage()){
//        }
        if (extra.opt("redPackets") != null && extra.opt("server") != null) {
            item.attachmentId = extra.getString("redPackets") + "_" + extra.getString("server") + "";
            item.sendState = MsgItem.UNHANDLE;
            if (extra.opt("dialog") != null && extra.opt("msgarr") != null) {
                if(extra.opt("redPackectSysType") != null) {
                    item.attachmentId += "|";
                    item.attachmentId += extra.getString("redPackectSysType");
                }else{
                    item.attachmentId += "|1";//兼容老版本
                }
                JSONArray paramsArray = extra.getJSONArray("msgarr");
                if(paramsArray.length() == 2){
                    String param1 = "";
                    String param2 = "";
                    item.msg = LanguageManager.getLangByKey(extra.getString("dialog"),paramsArray.getString(0),paramsArray.getString(1));
                }else{
                    item.msg = LanguageManager.getLangByKey(extra.getString("dialog"));
                }
            }
            if(item.msg.equals("90100001")){
                item.msg = LanguageManager.getLangByKey("90100001");
            }

        }else if (item.isAllianceHelpMessage()){
            if (extra.opt("attachmentId") != null){
                item.attachmentId =extra.getString("attachmentId");
                String attachmentString =String.format("%s|%s","102528",item.attachmentId);//102528=我的城堡正在遭受攻击，请大家援助
                item.attachmentId = attachmentString;
            }else{
                item.attachmentId = "102528";
            }
        }
//        else if(item.isAlllianceMessage() || item.isAnnounceInvite()
//                || item.isAllianceCreate() || item.isViewQuestionActivity()){
//        }else if(item.isShamoInhesionShare()){
//        }
        else if(item.isGWSysTips() || item.isGwSysNewTips()){
            item.attachmentId = "GWSys";
            item.attachmentId = item.attachmentId.concat("__").concat(extra.getString("dialog"));
            if (extra.opt("msgarr") != null) {
                JSONArray paramsArray = extra.getJSONArray("msgarr");
                for(int i = 0;i< paramsArray.length() ; i++){
                    item.attachmentId = item.attachmentId.concat("|").concat(paramsArray.getString(i));
                }
            }
        }
        else if(item.isNewsCenterShare()){
            if ( extra.opt("dialog") != null &&  extra.opt("msgarr") != null) {
                JSONArray paramsArray = extra.getJSONArray("msgarr");
                String nameString = "";
                String attachmentID = "";
                int count = paramsArray.length();
                for (int i = 0; i< count ; i++) {
                    String namesStr = paramsArray.getString(i);
                    if (nameString.length() > 0) {
                        if(i < count-2){
                            nameString = nameString.concat("||");
                            nameString = nameString.concat(namesStr);
                        }else if(i == count-2){
                            attachmentID = attachmentID.concat(namesStr);
                        }else if(i == count-1){
                            attachmentID = attachmentID.concat("_");
                            attachmentID = attachmentID.concat(namesStr);
                        }

                    }else{
                        nameString = nameString.concat(namesStr);
                    }
                }
                attachmentID = attachmentID + "_";
                attachmentID = attachmentID + nameString;
                item.attachmentId = attachmentID;
            }
        }
//        else if(item.isScienceMaxShare()){
//
//        }else if(item.isTurntableNewShare() || item.isSevenDayNewShare()){
//
//        }
        else{
            if (extra.opt("dialog") != null) {
                String dialogKey = extra.getString("dialog");
                if(StringUtils.isNotEmpty(item.attachmentId)) {
                    item.attachmentId = item.attachmentId.concat("__").concat(dialogKey);
                }else{
                    item.attachmentId = dialogKey;
                }
                if (extra.opt("msgarr") != null) {
                    JSONArray paramsArray = extra.getJSONArray("msgarr");
                    for(int i = 0;i< paramsArray.length() ; i++){
                        item.attachmentId = item.attachmentId.concat("|").concat(paramsArray.getString(i));
                    }
                }
            }
        }
    }

    private void parseAttachmentWithDialog(JSONObject extra, MsgItem item, String propName) throws JSONException {
        if (extra.opt(propName) != null && extra.opt("msgarr") != null) {
            JSONArray paramsArray = extra.getJSONArray("msgarr");
            String msg = "";
            if (paramsArray.length() == 0) {
                msg = LanguageManager.getLangByKey(extra.getString(propName));
            } else if (paramsArray.length() == 1) {
                msg = LanguageManager.getLangByKey(extra.getString(propName), paramsArray.getString(0));
            } else if (paramsArray.length() == 2) {
                if(item.isGWSysTips()){
                    item.attachmentId = "GWSys";
                    item.attachmentId = item.attachmentId.concat("__").concat(extra.getString(propName));
                    for(int i = 0;i< 2 ; i++){
                        item.attachmentId = item.attachmentId.concat("|").concat(paramsArray.getString(i));
                    }
                    msg = LanguageManager.getLangByKey(extra.getString(propName), paramsArray.getString(0), LanguageManager.getLangByKey("82000992",paramsArray.getString(1)));
                }else {
                    msg = LanguageManager.getLangByKey(extra.getString(propName), paramsArray.getString(0), paramsArray.getString(1));
                }
            } else if (paramsArray.length() == 3) {
                if(item.isGWSysTips()){
                    item.attachmentId = "GWSys";
                    item.attachmentId = item.attachmentId.concat("__").concat(extra.getString(propName));
                    for(int i = 0;i< 3 ; i++){
                        item.attachmentId = item.attachmentId.concat("|").concat(paramsArray.getString(i));
                    }
                    String cityId = paramsArray.getString(1);
                    String name = "";
                    if(Integer.parseInt(cityId) >1000){
                        msg = LanguageManager.getLangByKey(extra.getString(propName), paramsArray.getString(0), LanguageManager.getLangByKey("82000992", String.valueOf(Integer.parseInt(cityId) - 1000)), paramsArray.getString(2));
                    }else{
                        name = "";
                        msg = LanguageManager.getLangByKey(extra.getString(propName), paramsArray.getString(0), name, paramsArray.getString(2));
                    }
                }else {
                    msg = LanguageManager.getLangByKey(extra.getString(propName), paramsArray.getString(0), paramsArray.getString(1), paramsArray.getString(2));
                }
            } else if (paramsArray.length() == 4) {
                msg = LanguageManager.getLangByKey(extra.getString(propName), paramsArray.getString(0), paramsArray.getString(1), paramsArray.getString(2), paramsArray.getString(3));
            } else if (paramsArray.length() == 5) {
                msg = LanguageManager.getLangByKey(extra.getString(propName), paramsArray.getString(0), paramsArray.getString(1), paramsArray.getString(2), paramsArray.getString(3), paramsArray.getString(4));
            }
            if(item.isNeedParseAttachmentId()) {
                item.attachmentId = extra.getString(propName);
                for(int i = 0;i< paramsArray.length() ; i++){
                    item.attachmentId = item.attachmentId.concat("|").concat(paramsArray.getString(i));
                }
            }
            item.msg = msg;
            LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_CORE, "msg_dialog", msg);
        }else if(extra.opt(propName) != null){
            item.attachmentId = extra.getString(propName);
        }
    }

    private ChatChannel getChannel(String group) {
        return ChannelManager.getInstance().getChannel(group2channelType(group));
    }

    private void onGetNewMsg(JSONObject result) {
        try {
            JSONArray rooms = result.getJSONArray("rooms");

            for (int i = 0; i < rooms.length(); i++) {
                JSONObject room = rooms.getJSONObject(i);

                // 聊天室消息单独处理
                String roomId = room.getString("roomId");
                if (room.getString("group").equals("custom")){
                    JSONArray msgs = room.getJSONArray("msgs");
                    for (int j=0; j<msgs.length(); j++) {
                        JSONObject msg = msgs.getJSONObject(j);
                        formatMsgForChatRoom(msg, -1);
                    }

                    ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_CHATROOM, roomId);
                    if (channel != null) {
                        channel.serverMaxSeqId = room.getInt("lastSeqId");
                        channel.serverMinSeqId = room.getInt("firstSeqId");
                        channel.serverMaxTime = room.getLong("lastMsgTime");
                        channel.serverMinTime = room.getLong("firstMsgTime");
                        channel.wsNewMsgCount = msgs.length();
                        DBManager.getInstance().updateChannel(channel);
                    }
                    return;
                }
                if(roomId.contains("live_") && StringUtils.isEmpty(room.getString("group"))) {
                    return;
                }

                ChatChannel channel = getChannel(room.getString("group"));
                if (channel == null) {
                    continue;
                }

                long firstMsgTime = room.getLong("firstMsgTime");
                long lastMsgTime = room.getLong("lastMsgTime");
                int firstSeqId = room.getInt("firstSeqId");
                int lastSeqId = room.getInt("lastSeqId");
                channel.serverMaxSeqId = lastSeqId;
                channel.serverMinSeqId = firstSeqId;

                channel.serverMaxTime = lastMsgTime;
                channel.serverMinTime = firstMsgTime;

                JSONArray msgs = room.getJSONArray("msgs");

                channel.wsNewMsgCount = msgs.length();

                if (msgs.length() == 0) {
                    channel.loadMoreMsg();
                    continue;
                }

                MsgItem[] msgArr = new MsgItem[msgs.length()];
                MsgItem firstMsg = null;
                for (int j = 0; j < msgs.length(); j++) {
                    JSONObject msg = msgs.getJSONObject(j);
                    MsgItem item = parseMsgItem(msg,true);
                    if (item != null) {
                        msgArr[j] = item;

                        if (firstMsg == null || item.createTime < firstMsg.createTime) {
                            firstMsg = item;
                        }
                    }
                }

                if (msgs.length() > 1) {
                    if (channel.wsNewMsgCount < ChannelManager.LOAD_ALL_MORE_MAX_COUNT) {
                        firstMsg.firstNewMsgState = 1;
                    } else {
                        firstMsg.firstNewMsgState = 2;
                    }
                }

                ServiceInterface.handleMessage(msgArr, roomId2channelId(roomId), "", true, true);
            }
        } catch (JSONException e) {
            LogUtil.printException(e);
        }

        testServers();
    }

    private void onGetOldMsg(JSONObject result) {
        try {
            JSONArray msgs = result.getJSONArray("msg");

            MsgItem[] msgArr = new MsgItem[msgs.length()];
            MsgItem firstMsg = null;
            for (int j = 0; j < msgs.length(); j++) {
                JSONObject msg = msgs.getJSONObject(j);
                MsgItem item = parseMsgItem(msg,false);
                if (item != null) {
                    msgArr[j] = item;

                    if (firstMsg == null || item.createTime < firstMsg.createTime) {
                        firstMsg = item;
                    }
                }
            }

            // 聊天室消息单独处理
            if (firstMsg!= null && firstMsg.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM){
                for (int i=0; i<msgs.length(); i++) {
                    JSONObject msg = msgs.getJSONObject(i);
                    formatMsgForChatRoom(msg, -1);
                }

                return;
            }
            ServiceInterface.handleMessage(msgArr, roomId2channelId(firstMsg.roomId), "", false, true);
        } catch (JSONException e) {
            LogUtil.printException(e);
        }
    }

    public void updateChatRoomList(JSONObject result) {
        try {
            // 服务器传过来的聊天室id
            String chatRooms = "";
            String removeArenaRooms = "";
            String members = "";
            Class<?> resultClass = result.get("rooms").getClass();
            if (JSONObject.class.isAssignableFrom(resultClass)) {
                JSONObject roomObject = result.getJSONObject("rooms");
                JSONArray keys = roomObject.names();

                for (int i=0; i<keys.length(); i++){
                    String key = keys.getString(i);
                    Class<?> valueClass = roomObject.get(key).getClass();
                    if (!String.class.isAssignableFrom(valueClass)) {
                        continue;
                    }
                    String jsonRoom = roomObject.getString(key);
                    JSONObject roomInfo = new JSONObject(jsonRoom);
                    if(roomInfo.isNull("roomId")){
//                        result =     {
//                            rooms =         {
//                                "custombattlefield_6edb1a66ade942eb8ea491f7bfb8b762" = "{\"members\":\"[\\\"716671615000291\\\",\\\"1170227292000291\\\",\\\"2222678940000273\\\"\"}";
//                            };
//                        };
                        // 如果没有roomId返回，可能服务器数据有问题，客户端发送退出
                        arenaChatRoomQuit(key);
                        continue;
                    }

                    String roomId = roomInfo.getString("roomId");

                    // 非debug包过滤含有test的聊天室, debug包过滤非test聊天室
                    if(!LogUtil.isDebug && roomId.contains("test")
                            || LogUtil.isDebug && !roomId.contains("test")){
                        continue;
                    }

                    // 竞技场聊天室容错处理，只保留跟队伍id一致的竞技场聊天室，其他发送退出消息给独立聊天服务器
                    if(ChannelManager.getInstance().isArenaChatRoom(roomId)
                    && ChatServiceController.isNeedRemoveArenaRoom(roomId)){
                        if (removeArenaRooms.length() > 0) {
                            removeArenaRooms += "#";
                        }
                        removeArenaRooms += roomId;
                        continue;
                    }

                    if (chatRooms.length() > 0) {
                        chatRooms += "#";
                    }
                    chatRooms += roomId;

                    if(ChannelManager.getInstance().isArenaChatRoom(roomId)){
                        ChatServiceController.curAreaRoomId = roomId;
                    }
//                JSONArray uids = roomInfo.getJSONArray(uidStr);
//                for(int j=0; j<uids.length(); j++){
//                    if(members.length() > 0){
//                        members = members.concat("_");
//                    }
//                    members = members.concat(uids.getString(j));
//                }
                    members = roomInfo.getString("members");
                    members = members.substring(1, members.length()-1);
                    members = members.replaceAll(",", "_");
                    members = members.replaceAll("\"", "");
                    // 更新聊天室
                    ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_CHATROOM, roomId);
                    if (channel == null) {
                        ServiceInterface.setChannelMemberArray(DBDefinition.CHANNEL_TYPE_CHATROOM, roomId, members, roomInfo.getString("name"));
                        channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_CHATROOM, roomId);
                    }

                    if (ChannelManager.getInstance().isLanguageChatRoom(roomId)){
                        //语言聊天室
                        channel.customName = ChannelManager.getInstance().getLanguageChatRoomName(roomId);
                        channel.roomOwner = LanguageManager.getLangByKey(LanguageKeys.TIP_SYSTEM_PLAYER_NAME);// 170145=玛姬·格林
                    }
                    else if (ChannelManager.getInstance().isArenaChatRoom(roomId)) {
                        // 竞技场聊天室
                        channel.customName = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_ARENA_NAME);//172559=角斗场队伍聊天竞技场组队专用
                        channel.roomOwner = LanguageManager.getLangByKey(LanguageKeys.TIP_SYSTEM_PLAYER_NAME);// 170145=玛姬·格林
                    }
                    else
                    {
                        channel.roomOwner = roomInfo.getString("owner");
                        channel.customName = roomInfo.getString("name");
                    }

                    channel.memberUidArray.clear();
                    String[] uidArr = members.split("_");
                    for (int index = 0; index < uidArr.length; index++)
                    {
                        if (!uidArr[index].equals(""))
                            channel.memberUidArray.add(uidArr[index]);
                    }
                    DBManager.getInstance().updateChannel(channel);

                    ServiceInterface.postIsChatRoomMemberFlag(roomId, true);

                    long latestTime = channel.latestTime>0 ? channel.latestTime : 0;
                    // 请求聊天室的消息
                    JSONObject params = new JSONObject();
                    params.put(channel.channelID, latestTime);
                    sendCommand(GET_NEW_MSGS_BY_TIME_COMMAND, "rooms", params);
                }
            }

            // 移除指定聊天室以外的其他聊天室
            ServiceInterface.removeExceptChatRoom(chatRooms);

            // 通知服务器退出其他竞技场聊天室
            if(ChatServiceController.battlefield_chat_room && removeArenaRooms.length() > 0){
                String[] uidArr = removeArenaRooms.split("#");
                for (int i=0; i<uidArr.length; i++) {
                    arenaChatRoomQuit(uidArr[i]);
                }
            }

        } catch (JSONException e) {
            LogUtil.printException(e);
        }
    }

    private void setChatRoomChannel(JSONObject json) {
        try {
            String command = json.getString("cmd");
            JSONObject data = json.getJSONObject("data");

            LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "push chatroom cmd:", command);

            JSONObject msgs = null;
            if (command.equals(PUSH_CHATROOM_CREATE) || command.equals(PUSH_CHATROOM_INVITE) || command.equals(PUSH_LANGUAGE_CHATROOM_JOIN)) {
                if(data.isNull("roomInfo")) {
                    return;// 创建和邀请时必须有房间信息
                }
            }

            String jsonMsgs = data.getString("msgs");
            msgs = new JSONObject(jsonMsgs);
            String roomId = msgs.getString("roomId");
            if(roomId != null && ChannelManager.getInstance().isArenaChatRoom(roomId)){
                ChatServiceController.curAreaRoomId = roomId;
            }
            int opType = -1;
            boolean updateMsg = true;
            if(command.equals(PUSH_CHATOOM_CHANGENAME)) {
                opType = 5;
                ServiceInterface.notifyChatRoomNameChanged(msgs.getString("msg"));
                ServiceInterface.setChannelMemberArray(DBDefinition.CHANNEL_TYPE_CHATROOM, roomId, "", msgs.getString("msg"));
            }
            else
            {
                String memberUids = "";
                String jsonMsg = msgs.getString("msg");
                if(!jsonMsg.equals("") && !jsonMsg.equals(roomId)){
                    // 竞技场创建时只有队长进入
                    if(jsonMsg.equals(UserManager.getInstance().getCurrentUserId())){
                        memberUids = jsonMsg;
                    }else{
                        JSONObject userList = new JSONObject(jsonMsg);
                        JSONArray keys = userList.names();
                        for (int i = 0; i < keys.length(); i++) {
                            String key = keys.getString(i);
                            JSONObject user = userList.getJSONObject(key);
                            if (user == null) {
                                continue;
                            }
                            if (memberUids.length() > 0) {
                                memberUids = memberUids.concat("_");
                            }
                            memberUids = memberUids.concat(key);
                            UserManager.checkUser(key,
                                    user.getString("userName"),
                                    Integer.parseInt(user.getString("lastUpdateTime")));
                        }
                    }
                }

                if (command.equals(PUSH_CHATROOM_CREATE) || command.equals(PUSH_CHATROOM_INVITE) || command.equals(PUSH_LANGUAGE_CHATROOM_JOIN)) {
                    ChatServiceController.isShowProgressBar = false;
                    String jsonRoom = data.getString("roomInfo");
                    JSONObject roomInfo = new JSONObject(jsonRoom);
                    if(command.equals(PUSH_CHATROOM_CREATE)){
                        ChatServiceController.isCreateChatRoom = false;
                        opType = 1;
                        ServiceInterface.setChannelMemberArray(DBDefinition.CHANNEL_TYPE_CHATROOM, roomId, memberUids, roomInfo.getString("name"));
                    }else{
                        opType = 2;
                        ChatServiceController.isNeedJoinLive = false;
                        if(!roomInfo.isNull("members")){//如果有这个字段，表示本地没有这个聊天室，需要把所有成员发过来
                            memberUids = roomInfo.getString("members");
                            memberUids = memberUids.substring(1, memberUids.length()-1);
                            memberUids = memberUids.replaceAll(",", "_");
                            memberUids = memberUids.replaceAll("\"", "");
                        }
                        ServiceInterface.updateChannelMemberArray(DBDefinition.CHANNEL_TYPE_CHATROOM, roomId, memberUids, opType);
                    }

                    ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_CHATROOM, roomId);
                    if (channel != null) {
                        if (ChannelManager.getInstance().isLanguageChatRoom(roomId)){
                            LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "push joinLangRoom succeed:", roomId);
                            LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "push joinLangRoom time:", TimeManager.getInstance().getCurrentTimeMS());

                            //语言聊天室
                            channel.customName = ChannelManager.getInstance().getLanguageChatRoomName(roomId);
                            channel.roomOwner = LanguageManager.getLangByKey(LanguageKeys.TIP_SYSTEM_PLAYER_NAME);// 170145=玛姬·格林

                            //获取语言聊天室 - 人数
                            if (ServiceInterface.getServiceDelegate() != null){
                                ServiceInterface.getServiceDelegate().loadChatRoomMembers();
                            }

                            //语言聊天室,拉取一下历史消息
                            getHistoryMsgs(roomId, 0, TimeManager.getTimeInMS(channel.getLatestTime()));
                        }
                        else if (ChannelManager.getInstance().isArenaChatRoom(roomId)) {// 竞技场聊天室
                            channel.customName = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_ARENA_NAME);//172559=角斗场队伍聊天竞技场组队专用
                            channel.roomOwner = LanguageManager.getLangByKey(LanguageKeys.TIP_SYSTEM_PLAYER_NAME);// 170145=玛姬·格林
                            channel.settings = "2";
                            if (ChatServiceController.battlefield_chat_room) {
                                ChatServiceController.topChatRoomUid = roomId;
                            }
                        }
                        else {
                            channel.roomOwner = roomInfo.getString("owner");
                            channel.customName = roomInfo.getString("name");
                        }

                        DBManager.getInstance().updateChannel(channel);
                    }
                    ServiceInterface.postIsChatRoomMemberFlag(roomId, true);

                }
                else if(command.equals(PUSH_CHATROOM_QUIT)) {
                    opType = 3;
                    if(memberUids == ""){
                        memberUids = msgs.getString("sender");

                        //语言聊天室退出,不发系统提示消息
                        if (ChannelManager.getInstance().isLanguageChatRoom(roomId)){
                            updateMsg = false;
                        }
                        else {
                            updateMsg = memberUids.indexOf(UserManager.getInstance().getCurrentUserId()) == -1;
                            if(!updateMsg) {
                                ServiceInterface.postIsChatRoomMemberFlag(roomId, false);
                                if(roomId.equals(ChatServiceController.topChatRoomUid)) {
                                    ChatServiceController.topChatRoomUid="";
                                    JniController.getInstance().excuteJNIVoidMethod("postChatLatestInfo", new Object[] {""});
                                }
                            }
                        }
                    }
                    ServiceInterface.updateChannelMemberArray(DBDefinition.CHANNEL_TYPE_CHATROOM, roomId, memberUids, opType);
                }
                else if(command.equals(PUSH_CHATROOM_KICK)) {
                    opType = 4;
                    updateMsg = memberUids.indexOf(UserManager.getInstance().getCurrentUserId())==-1;
                    if(!updateMsg){
                        ServiceInterface.postIsChatRoomMemberFlag(roomId, false);
                        if(roomId.equals(ChatServiceController.topChatRoomUid)) {
                            ChatServiceController.topChatRoomUid="";
                            JniController.getInstance().excuteJNIVoidMethod("postChatLatestInfo", new Object[] {""});
                        }
                    }
                    ServiceInterface.updateChannelMemberArray(DBDefinition.CHANNEL_TYPE_CHATROOM, roomId, memberUids, opType);

                }
            }
            
            if(updateMsg) {
                formatMsgForChatRoom(msgs, opType);
            }

            ChatServiceController.hostActivity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        //聊天v2
                        if(ChatServiceController.chat_v2_on) {
                            ServiceInterface.getServiceDelegate().updateDialogs();
                        }
                        else
                        {
                            ChannelManager.getInstance().calulateAllChannelUnreadNum();
                            if (ChatServiceController.getChatFragment() != null) {
                                ChatServiceController.getChatFragment().reload();
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        LogUtil.printException(e);
                    }
                }
            });


        } catch (JSONException e) {
            LogUtil.printException(e);
        }
    }

    public void formatMsgForChatRoom(JSONObject msgs, int opType) {
        try {
            // 格式化多语言
            String tipMsg = "";
            String operatorName = LanguageManager.getLangByKey(LanguageKeys.TIP_YOU);// 你
            String operatorUid = msgs.getString("sender");

            JSONObject senderInfo = msgs.getJSONObject("senderInfo");
            String sendName = senderInfo.getString("userName");

            // 当前玩家是否是操作者
            Boolean isOperator = operatorUid.equals(UserManager.getInstance().getCurrentUser().uid);
            if(!isOperator){
                operatorName = sendName;
            }

            int op = opType;
            if(op == -1){
                op = Integer.parseInt(msgs.getString("type"));
            }

            // 是否在成员数组里
            Boolean isInMemberArray = false;
            String memberName = "";
            if(op!=5 && op!=0 && op != 120) {
                String msgStr = msgs.getString("msg");
                // 竞技场创建时只有队长进入
                if(msgStr.equals(UserManager.getInstance().getCurrentUserId())){
                    memberName = sendName;
                }else{
                    if (!msgStr.equals(msgs.getString("roomId"))) {
                        JSONObject msg = new JSONObject(msgStr);
                        JSONArray keys = msg.names();
                        for (int i = 0; i < keys.length(); i++) {
                            String key = keys.getString(i);
                            String userStr = msg.getString(key);
                            JSONObject userInfo = new JSONObject(userStr);
                            String name = userInfo.getString("userName");
                            if (key.equals(UserManager.getInstance().getCurrentUser().uid)) {
                                name = LanguageManager.getLangByKey(LanguageKeys.TIP_YOU);// 你
                                isInMemberArray = true;
                            }

                            if (!key.equals(operatorUid)) {
                                if (i < memberName.length()) {
                                    memberName = memberName.concat("、");
                                }
                                memberName = memberName.concat(name);
                                UserManager.checkUser(key,
                                        name,
                                        Integer.parseInt(userInfo.getString("lastUpdateTime")));
                            }
                        }
                    }
                }
            }

            int post = 100;
            String roomId = msgs.getString("roomId");
            if(ChannelManager.getInstance().isArenaChatRoom(roomId))
            {
                switch (op) {
                    case 1: //创建
                    case 2: //邀请
                        tipMsg = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_ARENA_INVITE, memberName);// 172560=欢迎指挥官{0}成功加入了队伍
                        if(opType != -1){
                            // 主ui聊天条提示标记
                            ChatServiceController.updateArenaChatRoomAni(true);
                        }
                        break;
                    case 3: //退出
                        tipMsg = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_ARENA_QUIT, operatorName);// 172562={0}退出了队伍。
                        break;
                    case 4: //踢人
                        tipMsg = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_ARENA_KICK, operatorName, memberName);// 172563={0}将{1}踢出了队伍。
                        break;
                    case 5: //
                        tipMsg = msgs.getString("msg");// 特殊的聊天消息提醒，如竞技场进入提示
                        post = 180;
                        break;
                    default:
                        tipMsg = msgs.getString("msg");
                        post = 0;
                        break;
                }
            }
            else if(roomId.contains("live_")){//直播间特殊消息处理
                switch (op) {
                    case 1: //创建
//                        tipMsg = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_INVITE_2, memberName, operatorName);// {1}将{0}加入聊天
//                        break;
                    case 2: //邀请
//                        tipMsg = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_INVITE_2, memberName, operatorName);// {1}将{0}加入聊天
//                        break;
                    case 3: //退出
//                        tipMsg = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_LOGOUT, operatorName);// {0}退出了聊天室。
//                        break;
                    case 4: //踢人
//                        if (isInMemberArray && !isOperator) {
//                            tipMsg = LanguageManager.getLangByKey("105340", operatorName);// 您已被{0}移出聊天
//                        } else {
//                            tipMsg = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_REMOVE_2, memberName, operatorName);// {1}将{0}移出聊天
//                        }
//                        break;
                    case 5: //改名
//                        tipMsg = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_CHANGE_NAME, operatorName, msgs.getString("msg"));// {0}将聊天室名称修改为{1}
//                        break;
                        return;
                    case 120:
                        if(isOperator) {
                            if(ChatServiceController.getCurrentActivity() != null){
                                ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MenuController.showContentConfirm(LanguageKeys.TIP_LIVE_BAN_ANCHOR_SELF);
                                    }
                                });
                            }
                            return;
                        }else{
                            tipMsg = LanguageManager.getLangByKey(LanguageKeys.TIP_LIVE_BAN_ANCHOR_MSG);// 特殊的聊天消息提醒，如竞技场进入提示
                            post = 120;
                        }
                        break;
                    default:
                        tipMsg = msgs.getString("msg");
                        post = 0;
                        break;
                }
            }else{

                switch (op) {
                    case 1: //创建
                        tipMsg = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_INVITE_2, memberName, operatorName);// {1}将{0}加入聊天
                        break;
                    case 2: //邀请
                        tipMsg = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_INVITE_2, memberName, operatorName);// {1}将{0}加入聊天
                        break;
                    case 3: //退出
                        tipMsg = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_LOGOUT, operatorName);// {0}退出了聊天室。
                        break;
                    case 4: //踢人
                        if (isInMemberArray && !isOperator) {
                            tipMsg = LanguageManager.getLangByKey("105340", operatorName);// 您已被{0}移出聊天
                        } else {
                            tipMsg = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_REMOVE_2, memberName, operatorName);// {1}将{0}移出聊天
                        }
                        break;
                    case 5: //改名
                        tipMsg = LanguageManager.getLangByKey(LanguageKeys.TIP_CHATROOM_CHANGE_NAME, operatorName, msgs.getString("msg"));// {0}将聊天室名称修改为{1}
                        break;
                    default:
                        tipMsg = msgs.getString("msg");
                        post = 0;
                        break;
                }
            }

            //过滤改名等聊天室操作向聊天室内发消息
            if(tipMsg.equals(roomId)){
                return;
            }

            //操作权限 - 系统消息 不显示
            if (roomId.contains("custom_LanguageChatRoom_") && op < 5 && op > 0){
                return;
            }

            MsgItem item = parseMsgItem(msgs, true);
            if (item != null) {
                item.msg = tipMsg;
                item.channelType = DBDefinition.CHANNEL_TYPE_CHATROOM;
                item.post = post;
                if(item.post == MsgItem.MSG_TYPE_LIVEROOM_SYS){//直播间禁止主播特殊处理
                    item.uid = "";
                }
                MsgItem[] dbItemsArray = {item};
                ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_CHATROOM, roomId);
                if (channel != null)
                    ServiceInterface.handleMessage(dbItemsArray, roomId, channel.customName, false, true);
            }

        } catch (JSONException e) {
            LogUtil.printException(e);
        }
    }

    private boolean isTestingServers = false;
    private Timer testServerTimer;

    private synchronized void testServers() {
        if (!enableNetworkOptimization || isTestingServers) return;
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
        if (testServerTimer != null) {
            testServerTimer.cancel();
            testServerTimer.purge();
            testServerTimer = null;
        }
        isTestingServers = true;
        testServerTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    NetworkUtil.testServerAndSaveResult(serversInfos, statusListener);
                    isTestingServers = false;
                    statusListener.onConsoleOutput("Ping complete");
                    statusListener.onTestComplete();
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        };
        long delayMS = networkOptimizationTestDelay * 1000;
        testServerTimer.schedule(timerTask, delayMS == 0 ? (20 * 1000) : delayMS);
    }

    private static String roomId2channelId(String roomId) {
        return roomId.substring(roomId.lastIndexOf("_") + 1);
    }

    private static int group2channelType(String group) {
        if(group.equals("custom")) {
            return DBDefinition.CHANNEL_TYPE_CHATROOM;
        }else if(group.equals("country")){
            return DBDefinition.CHANNEL_TYPE_COUNTRY;
        }else{
            return DBDefinition.CHANNEL_TYPE_ALLIANCE;
        }
    }

    public void handleDisconnect() {
//      ServiceInterface.flyHint(null, "", "disconnect", 0, 0, false);
    }

    /**
     * id格式：<p>
     * country_1<p>
     * alliance_1_c79be2b653224cb4b1aeb5138ad15118<p>
     * <p/>
     * test_country_1<p>
     * test_alliance_1_c79be2b653224cb4b1aeb5138ad15118<p>
     * <p/>
     * beta_country_107<p>
     * beta_alliance_107_c79be2b653224cb4b1aeb5138ad15118<p>
     */
    public static String getCountryRoomId() {
        int sid = UserManager.getInstance().getCurrentUser().serverId;
        return getRoomIdPrefix() + "country_" + sid;
    }

    public static String getAllianceRoomId() {
        int sid = UserManager.getInstance().getCurrentUser().crossFightSrcServerId > 0 ? UserManager.getInstance().getCurrentUser().crossFightSrcServerId
                : UserManager.getInstance().getCurrentUser().serverId;
        if(ConfigManager.isEnterArena || ConfigManager.isIndependentLeague){
            return getRoomIdPrefix() + "alliance_" + UserManager.getInstance().getCurrentUser().allianceId;
        }else {
            return getRoomIdPrefix() + "alliance_" + sid + "_" + UserManager.getInstance().getCurrentUser().allianceId;
        }
    }

    public static String getLiveRoomId(){
        if(ChatServiceController.curLiveRoomId.equals(""))
            return "";
        return ChatServiceController.curLiveRoomId;
    }

    //请求进入默认语言聊天室
    public void joinLanguageRoom() {
        String roomId = getLanguageRoomId();
        String roomName = getLanguageRoomName();
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "joinLanguageRoom:", roomId);

        languageChatRoomInvite(roomId,roomName);
    }

    //获取默认语言聊天室
    public static String getLanguageRoomId(){
        String langRoomId = getChatRoomChannelId("1");
        try {
            Map<String, String> languageRoomMap = new HashMap<String, String>(){
                {
                    put("en",getChatRoomChannelId("1"));
                    put("fr",getChatRoomChannelId("11"));
                    put("de",getChatRoomChannelId("21"));
                    put("ru",getChatRoomChannelId("31"));
                    put("ko",getChatRoomChannelId("41"));
                    put("th",getChatRoomChannelId("51"));
                    put("ja",getChatRoomChannelId("61"));
                    put("pt",getChatRoomChannelId("71"));
                    put("es",getChatRoomChannelId("81"));
                    put("tr",getChatRoomChannelId("91"));
                    put("id",getChatRoomChannelId("101"));
                    put("zh_TW",getChatRoomChannelId("111"));
                    put("zh_CN",getChatRoomChannelId("121"));
                    put("it",getChatRoomChannelId("131"));
                    put("ar",getChatRoomChannelId("141"));
                }
            };

            langRoomId = languageRoomMap.get(ConfigManager.getInstance().gameLang);
            if (langRoomId.isEmpty()){
                langRoomId = getChatRoomChannelId("1");
            }
        }
        catch (Exception e) {
            LogUtil.printException(e);
        }

        return langRoomId;
    }

    public static String getLanguageRoomName() {
        String langRoomName = "English";
        try {
            Map<String, String> languageRoomMap = new HashMap<String, String>(){
                {
                    put("en","English");
                    put("fr","Français");
                    put("de","Deutsch");
                    put("ru","Pусский");
                    put("ko","한국어");
                    put("th","ไทย");
                    put("ja","日本語");
                    put("pt","Português");
                    put("es","Español");
                    put("tr","Türkçe");
                    put("id","Indonesia");
                    put("zh_TW","繁體中文");
                    put("zh_CN","简体中文");
                    put("it","Italiano");
                    put("ar","العربية");
                }
            };

            langRoomName = languageRoomMap.get(ConfigManager.getInstance().gameLang);
            if (langRoomName.isEmpty()){
                langRoomName = "English";
            }
        }
        catch (Exception e)
        {
            LogUtil.printException(e);
        }

        return langRoomName + "-" + "1";
    }

    public static String getChatRoomChannelId(String xmlId) {
        return getRoomIdPrefix() + "custom_LanguageChatRoom_" + xmlId;
    }


    public static String getRoomIdPrefix() {
        if (LogUtil.isDebug && !ChatServiceController.isxternaletworkebug){
            return "test_";
        }

        return "";
    }

    public synchronized void startKeepAlive()
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

        heartbeatService.scheduleWithFixedDelay(timerTask, 5 * 1000, 60 * 1000, TimeUnit.MILLISECONDS);
    }


    private synchronized void sendKeepAlive()
    {
        if(isUseMqtt) {
            synchronized (this) {
                if (mqclient != null) {
                    if (mqclient.getStatus() > 0 && isForeground) {
                        mqclient.sendDataToServer("","heartbeat");
                    }
                }
            }
        }
        else {
            synchronized (this) {
                if (client != null) {
                    if (client.isOpen() && isForeground) {
                        client.sendFrame(new FramedataImpl1(Framedata.Opcode.PING));
                    }
                }
            }
        }


    }

    public void connectToWSManully(String server, String port, String protocol) {
        if (WebSocketManager.isWebSocketEnabled())
        {
            try {
                this.clearSocket();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                this.connect2ws( server,  port,  protocol);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public ArrayList<WSServerInfo> getServersInfos(){
        return serversInfos;
    }
}
