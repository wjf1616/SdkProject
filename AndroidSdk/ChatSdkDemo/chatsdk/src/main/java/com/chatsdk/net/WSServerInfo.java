package com.chatsdk.net;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.chatsdk.model.TimeManager;
import com.chatsdk.util.NetworkUtil;

public class WSServerInfo implements Serializable
{
	private static final long	serialVersionUID	= -8642074625225319216L;
	/* 测试结果使用的有效时间 */ 
	public static final int TEST_RESULT_VALID_TIME = 5 * 24 * 3600 * 1000;
	
	public String				protocol;
	public String				address;
	public String				port;
	public double				loss;
	public double				delay;
	/* GMT格式 */
	public long					firstTestTime;
	public long					lastTestTime;
	public int					testCount;
	public long					lastErrorTime;
	public int 					pingValue;

	public WSServerInfo(String protocol, String address, String port)
	{
		this.protocol = protocol;
		this.address = address;
		this.port = port;
	}

	/**
	 * 如果以前没有测试过，就直接插入测试结果集
	 * 如果测试过，这里的信息就无用，转而更新旧的测试记录
	 */
	public void initTestInfo()
	{
		firstTestTime = TimeManager.getInstance().getCurrentTimeMS();
		lastTestTime = TimeManager.getInstance().getCurrentTimeMS();
		testCount = 1;
	}

	public boolean equalTo(WSServerInfo info)
	{
		return info.address.equals(address) && info.protocol.equals(protocol) && info.port.equals(port);
	}

	public void updateTestResult(WSServerInfo newInfo)
	{
		lastTestTime = TimeManager.getInstance().getCurrentTimeMS();
		if(!isTestTooOld())
		{
			// 求均值
			loss = (loss * testCount + newInfo.loss) / (testCount + 1);
			
			if(delay == NetworkUtil.DELAY_OF_ALL_LOST && newInfo.delay == NetworkUtil.DELAY_OF_ALL_LOST)
			{
				delay = NetworkUtil.DELAY_OF_ALL_LOST;
			}else if(delay == NetworkUtil.DELAY_OF_ALL_LOST)
			{
				delay = newInfo.delay;
			}else if(newInfo.delay == NetworkUtil.DELAY_OF_ALL_LOST)
			{
				// delay = delay;
			}else{
				delay = (delay * testCount + newInfo.delay) / (testCount + 1);
			}
			
			testCount++;
		}
		else
		{
			loss = newInfo.loss;
			delay = newInfo.delay;
			testCount = 1;
			firstTestTime = TimeManager.getInstance().getCurrentTimeMS();
		}
	}
	
	public boolean isTestTooOld()
	{
		long timeoutMS = WebSocketManager.getInstance().networkOptimizationTimeout * 1000;
		return (TimeManager.getInstance().getCurrentTimeMS() - firstTestTime) > (timeoutMS == 0 ? TEST_RESULT_VALID_TIME : timeoutMS);
	}
	
	/**
	 * 判断上次断线时间，小于一定时间则不再连（这个时间取决于重连间隔时间、有多少个服可选，大致定为2分钟）
	 */
	public boolean isConnectionErrorRecently()
	{
		long timePassed = TimeManager.getInstance().getCurrentTimeMS() - lastErrorTime;
		return timePassed > 0 && timePassed <= (2 * 60 * 1000);
	}

	public String toString()
	{
		if (delay == 0 && firstTestTime == 0 && lastTestTime == 0)
		{
			return address + " " + protocol + ":" + port + (lastErrorTime > 0 ? (" lastErrorTime=" + lastErrorTime) : "");
		}
		else if (testCount == 0 && firstTestTime == 0 && lastTestTime == 0)
		{
			return address + " " + protocol + ":" + port + " loss=" + Math.round(loss) + "% avgDelay=" + Math.round(delay) + "ms"
					+ (lastErrorTime > 0 ? (" lastErrorTime=" + lastErrorTime) : "");
		}
		else
		{
			return address + " " + protocol + ":" + port + " loss=" + Math.round(loss) + "% avgDelay=" + Math.round(delay) + "ms"
					+ " testCnt=" + testCount + " firstTestTime=" + firstTestTime + " lastTestTime=" + lastTestTime
					+ (lastErrorTime > 0 ? (" lastErrorTime=" + lastErrorTime) : "");
		}
	}

	public boolean isValid()
	{
		return StringUtils.isNotEmpty(protocol) && StringUtils.isNotEmpty(address) && StringUtils.isNotEmpty(port);
	}
}