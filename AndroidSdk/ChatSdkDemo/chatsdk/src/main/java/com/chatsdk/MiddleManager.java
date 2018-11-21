package com.chatsdk;

import android.app.Activity;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.ServiceInterface;
import com.chatsdk.net.WebSocketManager;

import com.alibaba.fastjson.JSONObject;

public class MiddleManager {

    //初始化聊天
    public static void init(){
        WebSocketManager.getInstance().onEnterBackground();

        //连接服务器
        ServiceInterface.connect2WS();
    }

    //设置host-从游戏中获取数据
    public static void initHost(Activity activity, IHost host){
        ChatServiceController.init(activity,host);

    }

    //游戏->聊天
    public static void GameToChatFunction(String typeName, JSONObject object){
        if (object != null){
            //带数据的方法
            if (typeName.equals("setPlayerInfo")){
                try {
                    ServiceInterface.setPlayerInfo(object.getIntValue("country"),object.getIntValue("worldTime"),object.getIntValue("gmod"),object.getIntValue("headPicVer"),
                            object.getString("name"),object.getString("careerName"),object.getString("uidStr"),object.getString("picStr"),object.getIntValue("vipLevel"),
                            object.getIntValue("svipLevel"),object.getIntValue("vipEndTime"),object.getIntValue("lastUpdateTime"),object.getIntValue("crossFightSrcServerId"),
                            object.getIntValue("monthCard"),object.getIntValue("isShowServerId"),object.getIntValue("level"),object.getLongValue("gold"),object.getIntValue("vipframe"));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else if (typeName.equals("setPlayerAllianceInfo")){
                try {
                    ServiceInterface.setPlayerAllianceInfo(object.getString("asnStr"),object.getString("allianceIdStr"),object.getIntValue("alliancerank"),
                            object.getBooleanValue("isFirstJoinAlliance"),object.getIntValue("createServer"));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else if (typeName.equals("showChatActivityFrom2dx")){
                try {
                    ServiceInterface.showChatActivityFrom2dx(object.getIntValue("maxHornInputCount"),object.getIntValue("chatType"),object.getIntValue("sendInterval"),object.getBooleanValue("rememberPosition"),object.getBooleanValue("enableCustomHeadImg"),object.getBooleanValue("isNoticeItemUsed"));
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
        else
        {
            //不带数据的方法

        }
    }

}
