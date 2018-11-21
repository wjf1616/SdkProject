package com.chatsdk.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.MathUtil;
import com.chatsdk.util.NetworkUtil;

public class WSServerInfoManager
{
	private static WSServerInfoManager	instance;
	private ConcurrentHashMap<String, ArrayList<WSServerInfo>>	serverInfoMap;
	private ConcurrentHashMap<String, ArrayList<WSServerInfo>>  failConnectServerInfoMap;
	private String failkey = "";

	public static WSServerInfoManager getInstance()
	{
		if (instance == null)
		{
			synchronized (WSServerInfoManager.class)
			{
				if (instance == null)
				{
					instance = new WSServerInfoManager();
				}
			}
		}
		return instance;
	}

	private WSServerInfoManager()
	{
		serverInfoMap = new ConcurrentHashMap<String, ArrayList<WSServerInfo>>();
		failConnectServerInfoMap = new ConcurrentHashMap<String, ArrayList<WSServerInfo>>();
		load();
	}

	private String serialize(ConcurrentHashMap<String, ArrayList<WSServerInfo>>	map) throws IOException {
		long startTime = System.currentTimeMillis();
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				byteArrayOutputStream);
		objectOutputStream.writeObject(map);
		String serStr = byteArrayOutputStream.toString("ISO-8859-1");
		serStr = java.net.URLEncoder.encode(serStr, "UTF-8");
		objectOutputStream.close();
		byteArrayOutputStream.close();
		
		long endTime = System.currentTimeMillis();
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "序列化耗时", endTime - startTime);

		return serStr;
	}

	private ConcurrentHashMap<String, ArrayList<WSServerInfo>> deSerialization(String str) throws IOException,
			ClassNotFoundException {
		long startTime = System.currentTimeMillis();
		String redStr = java.net.URLDecoder.decode(str, "UTF-8");
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				redStr.getBytes("ISO-8859-1"));
		ObjectInputStream objectInputStream = new ObjectInputStream(
				byteArrayInputStream);
		ConcurrentHashMap<String, ArrayList<WSServerInfo>> map = (ConcurrentHashMap<String, ArrayList<WSServerInfo>>) objectInputStream.readObject();
		objectInputStream.close();
		byteArrayInputStream.close();
		long endTime = System.currentTimeMillis();
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "反序列化耗时", endTime - startTime);
		return map;
	}

	void saveObject(String strObject) {
		SharedPreferences sp = ChatServiceController.hostActivity.getSharedPreferences("wsServerInfo", 0);
		Editor edit = sp.edit();
		edit.putString("wsServerInfo", strObject);
		edit.commit();
	}

	String getObject() {
		SharedPreferences sp = ChatServiceController.hostActivity.getSharedPreferences("wsServerInfo", 0);
		return sp.getString("wsServerInfo", null);
	}
	
	/**
	 * @return 未联网时，返回null
	 */
	private String getNetworkKey()
	{
		return NetworkUtil.getNetworkSummary();
	}

	public void save(String key, ArrayList<WSServerInfo> infos) {
		if (StringUtils.isEmpty(key))
			return;
		
		try
		{
			if ( infos.size() > 0 ) {
				serverInfoMap.put(key, infos);
			}
			saveObject(serialize(serverInfoMap));
		}
		catch (IOException e)
		{
			LogUtil.printException(e);
		}
	}
	
	private void updateMap(String key, ArrayList<WSServerInfo> newInfos)
	{
		ArrayList<WSServerInfo> oldInfos = serverInfoMap.get(key);
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "oldTestResults", servers2str(oldInfos), "newTestResults", servers2str(newInfos));
		for (WSServerInfo newInfo : newInfos)
		{
			boolean hasTestedBefore = false;
			for (WSServerInfo oldInfo : oldInfos)
			{
				if(oldInfo.equalTo(newInfo))
				{
					hasTestedBefore = true;
					if(oldInfo.isTestTooOld())
					{
						LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "result is out of time");
					}
					oldInfo.updateTestResult(newInfo);
				}
			}
			if(!hasTestedBefore)
			{
				oldInfos.add(newInfo);
			}
		}
		LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "updatedTestResults", servers2str(oldInfos));
	}
	
	public void updateLastErrorTime(WSServerInfo newInfo)
	{
		String key = getNetworkKey();
		if(key != null && serverInfoMap.containsKey(key))
		{
			ArrayList<WSServerInfo> savedInfos = serverInfoMap.get(key);

			for (int i = 0; i < savedInfos.size(); i++)
			{
				WSServerInfo testedServer = savedInfos.get(i);
				if(newInfo.equalTo(testedServer))
				{
					testedServer.lastErrorTime = newInfo.lastErrorTime;
					
					try
					{
						saveObject(serialize(serverInfoMap));
					}
					catch (IOException e)
					{
						LogUtil.printException(e);
					}
					
					return;
				}
			}
		}
	}

	public void load()
	{
		try
		{
			String savedValue = getObject();
			if (StringUtils.isNotEmpty(savedValue))
			{
				serverInfoMap = deSerialization(savedValue);

				String key = getNetworkKey();
				failkey = key;
				if(key != null && serverInfoMap.containsKey(key))
				{
					ArrayList<WSServerInfo> savedInfos = serverInfoMap.get(key);
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "network", key, "savedInfos", servers2str(savedInfos));
				}
			}
		}
		catch (ClassNotFoundException e)
		{
			LogUtil.printException(e);
		}
		catch (IOException e)
		{
			LogUtil.printException(e);
		}
	}
	
	public static String servers2str(ArrayList<WSServerInfo> loadedInfos)
	{
		String result = "";
		for (int i = 0; i < loadedInfos.size(); i++)
		{
			result += "\n[" + i + "] " + loadedInfos.get(i);
		}
		return result;
	}
	
	public static void sortServers(ArrayList<WSServerInfo> servers)
	{
		if (servers != null && servers.size() > 0)
		{
			try
			{
				Collections.sort(servers, new SortByLossAndLatency());
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	private static class SortByLossAndLatency implements Comparator<WSServerInfo>
	{
		@Override
		public int compare(WSServerInfo o1, WSServerInfo o2)
		{
			if (o1.loss > 10 || o2.loss > 10){
				if (o1.loss > o2.loss
						|| (o1.loss == o2.loss && o1.delay > o2.delay)) {
					return 1;
				} else if (o1.loss == o2.loss && o1.delay == o2.delay) {
					return 0;
				}
				return -1;
			}else {
				if (o1.delay > o2.delay
						|| (o1.delay == o2.delay && o1.loss > o2.loss)) {
					return 1;
				} else if (o1.delay == o2.delay && o1.loss == o2.loss) {
					return 0;
				}
				return -1;
			}
		}
	}

	public ArrayList<WSServerInfo> getPriorityServerArray(){
		ArrayList<WSServerInfo> priorityList = null;
		String key = getNetworkKey();
		if (key == null) key = failkey;

		if(key != null && serverInfoMap.containsKey(key)) {
			priorityList = serverInfoMap.get(key);
		}
		if(priorityList == null){
			priorityList = new ArrayList<WSServerInfo>();
		}

		return priorityList;
	}

	public WSServerInfo selectPrimaryServer(ArrayList<WSServerInfo> servers)
	{
		if(servers == null || servers.size() == 0) return null;
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_STATUS, "allowedServers", servers2str(servers));

		String key = getNetworkKey();
		if (key == null) key = failkey;
		if(key != null && serverInfoMap.containsKey(key))
		{
			ArrayList<WSServerInfo> testedServers = serverInfoMap.get(key);
			sortServers(testedServers);
			for (int i = 0; i < testedServers.size(); i++)
			{
				WSServerInfo testedServer = testedServers.get(i);
				if(testedServer.isTestTooOld()||isContainFailConnectServerInfoMap(testedServer))
				{
					continue;
				}
				
				for (WSServerInfo server : servers)
				{
					if(server.equalTo(testedServer))
						return testedServer;
				}
			}
		}

		//如果再找不到最优的，再从noConnectServerInfoMap中选择一个
		if(key != null && failConnectServerInfoMap.containsKey(key)){
			ArrayList<WSServerInfo> noServers = failConnectServerInfoMap.get(key);
			sortServers(noServers);
			if (noServers.size()>0){
				return noServers.get(0);
			}
		}

		return servers.get(MathUtil.random(0, servers.size() - 1));
	}


	/**
	 * 判断si是否包含中noConnectServerInfoMap中
	 * @author lzh
	 * @time 17/3/17 上午10:27
	 */
	private boolean isContainFailConnectServerInfoMap(WSServerInfo si)
	{
		boolean isExist = false;
		String key = failkey;
		if (key == ""|| key==null) return isExist;

		ArrayList<WSServerInfo> noServers = failConnectServerInfoMap.get(key);
		if (noServers==null) return isExist;
		Iterator it = noServers.iterator();
		while(it.hasNext()){
			WSServerInfo tmpSI= (WSServerInfo)it.next();
			if (tmpSI.equalTo(si)){
				isExist = true;
				break;
			}
		}
		return  isExist;
	}

	
	/**
	 * 把本次登陆重连次数大于3次的连接存入noConnectServerInfoMap
	 * @author lzh
	 * @time 17/3/17 上午10:20
	 */
	public void addFailConnectServerInfoMap(WSServerInfo si)
	{
		String key = failkey;
		if (key == ""|| key==null) return;
		if(failConnectServerInfoMap.containsKey(key))
		{
			ArrayList<WSServerInfo> noServers = failConnectServerInfoMap.get(key);
			boolean isExist = isContainFailConnectServerInfoMap(si);
			if (!isExist) {
				noServers.add(si);
				failConnectServerInfoMap.put(key,noServers);
			}
		}else{
			ArrayList<WSServerInfo> tmpArrayList = new ArrayList<WSServerInfo>();
			tmpArrayList.add(si);
			failConnectServerInfoMap.put(key, tmpArrayList);
		}
	}
}
