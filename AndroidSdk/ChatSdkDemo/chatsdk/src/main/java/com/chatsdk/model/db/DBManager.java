package com.chatsdk.model.db;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.util.Log;
import android.util.Pair;

import com.alibaba.fastjson.JSON;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.DetectMailInfo;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.MsgItem;
import com.chatsdk.model.TimeManager;
import com.chatsdk.model.UserInfo;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.mail.MailData;
import com.chatsdk.model.mail.detectreport.DetectReportMailContents;
import com.chatsdk.model.mail.fbscoutreport.FBDetectReportMailContents;
import com.chatsdk.net.WebSocketManager;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.PermissionManager;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DBManager
{
	public static final String	TABEL_PERSON		= "person";

	public static final int		CONFIG_TYPE_READ	= 1;
	public static final int		CONFIG_TYPE_SAVE	= 2;
	public static final int		CONFIG_TYPE_DELETE	= 3;
	public static final int		CONFIG_TYPE_REWARD	= 4;

	private DBHelper			helper;
	private SQLiteDatabase		db;

	private static DBManager	instance;
	private Map<String, String>	mDetectInfoMap		= null;

	public static DBManager getInstance()
	{
		if (instance == null)
		{
			synchronized (DBManager.class)
			{
				if (instance == null)
				{
					instance = new DBManager();
				}
			}
		}
		return instance;
	}

	public DBHelper getHelper()
	{
		return helper;
	}

	/**
	 * 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
	 * mFactory); 所以要确保context已初始化，可以把实例化DBManager的步骤放在Activity的onCreate里。
	 */
	private DBManager()
	{
		mDetectInfoMap = new HashMap<String, String>();
	}

	/**
	 * 若isAccountChanged，则强制重新初始化db
	 */
	public static void initDatabase(boolean isAccountChanged, boolean isNewUser)
	{
		isIniting = true;
		String isPermissionsStr = "0";
		String isNewUserStr = "0";
		if (needGetStoragePermissions()) isPermissionsStr = "1";
		if (isNewUser) isNewUserStr = "1";
		Log.d("DBManager-initDatabase ", "isPermissionsStr = "+isPermissionsStr);
		Log.d("DBManager-initDatabase ", "isNewUserStr = " + isNewUserStr);

		if (!isNewUser && needGetStoragePermissions())
		{
			getStoragePermissions(isAccountChanged);
			isIniting = false;
			return;
		}
		if (isAccountChanged && DBManager.getInstance().isDBAvailable())
		{
			DBManager.getInstance().closeDB();
		}
		if (!DBManager.getInstance().isDBAvailable() && instance != null)
		{
			DBManager.getInstance().initDB(ChatServiceController.hostActivity);
			LogUtil.init(ChatServiceController.hostActivity);
		}

		if (isAccountChanged)
		{
			ChatServiceController.getInstance().reset();
		}

		//清空过期邮件
		DBManager.getInstance().deleteSysMailFromDBWithFailTime();

		JniController.getInstance().excuteJNIVoidMethod("completeInitDatabase", null);


//		ChatServiceController.getInstance().host.completeInitDatabase();
		isIniting = false;
	}

	public static boolean needGetStoragePermissions()
	{
		return !PermissionManager.isExternalStoragePermissionsAvaiable(ChatServiceController.hostActivity) && !attemptedGetStoragePermissionsBefore;
	}

	private static final int	MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE	= 1;
	private static String[]		PERMISSIONS_STORAGE						= {
																		// Manifest.permission.READ_EXTERNAL_STORAGE,
																		Manifest.permission.WRITE_EXTERNAL_STORAGE };

	private static boolean		initDatabaseParam						= false;
	private static boolean		attemptedGetStoragePermissionsBefore	= false;

	private static void getStoragePermissions(boolean isAccountChanged)
	{
		attemptedGetStoragePermissionsBefore = true;
		initDatabaseParam = isAccountChanged;
		
		PermissionManager.getExternalStoragePermission();
	}


	public void onRequestPermissionsResult()
	{		
		initDatabase(initDatabaseParam, false);
	}

	public void initDB(Context context)
	{
		try
		{
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE,"content",context);
			helper = new DBHelper(context);
			db = helper.getWritableDatabase();
			isInited = true;
			getDetectMailInfo();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void rmDatabaseFile()
	{
		if (helper != null)
		{
			LogUtil.trackMessage("delete database file by user");
			helper.rmDirectory();
			isInited = false;
			ChannelManager.getInstance().reset();
		}
	}

	public void closeDB()
	{
		if (!isDBAvailable())
			return;

		try
		{
			if (helper != null)
			{
				helper.close();
				helper = null;
			}
			if (db != null)
			{
				if (db.isOpen())
					db.close();
				db = null;
			}
			isInited = false;
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	private static boolean	isInited	= false;
	private static boolean	isIniting		= false;
	private static boolean	tracked		= false;

	public boolean isDBAvailable()
	{
		if (!isInited)
			return false;

		boolean result = db != null && db.isOpen();
		if (!result && !tracked)
		{
			if (db == null)
			{
				LogUtil.trackMessage("database is unavailable (db is null)");
			}
			else
			{
				LogUtil.trackMessage("database is unavailable (db is not open)");
			}
			tracked = true;
		}
		return result;
	}

	public ArrayList<UserInfo> getAllianceMembers(String allianceID)
	{
		ArrayList<UserInfo> result = new ArrayList<UserInfo>();
		if (StringUtils.isEmpty(allianceID) || !isDBAvailable())
			return result;

		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = '%s'", DBDefinition.TABEL_USER,
					DBDefinition.USER_COLUMN_ALLIANCE_ID, allianceID);
			c = db.rawQuery(sql, null);
			if (c != null)
			{
				while (c.moveToNext())
				{
					UserInfo user = new UserInfo(c);
					if (user != null)
						result.add(user);
				}
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return result;
	}

	public UserInfo getUser(String userID)
	{
		if (StringUtils.isEmpty(userID) || !isDBAvailable())
			return null;

		UserInfo result = null;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = '%s'", DBDefinition.TABEL_USER,
					DBDefinition.USER_COLUMN_USER_ID, userID);
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return null;
			}
			result = new UserInfo(c);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return result;
	}

	public void insertUser(UserInfo user)
	{
		if (user == null || !isDBAvailable())
			return;

		try
		{
			db.insert(DBDefinition.TABEL_USER, null, user.getContentValues());
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void updateUser(UserInfo user)
	{
		if (user == null || !isDBAvailable())
			return;

		try
		{
			if (getUser(user.uid) != null)
			{
				String where = String.format(Locale.US, "%s = '%s'", DBDefinition.USER_COLUMN_USER_ID, user.uid);
				db.update(DBDefinition.TABEL_USER, user.getContentValues(), where, null);
			}
			else
			{
				insertUser(user);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void updateMyMessageStatus(MsgItem msg, ChatTable chatTable)
	{
		if (msg == null || StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return;

		try
		{
			String where = String.format(Locale.US, "%s = %s AND %s = '%s'", DBDefinition.CHAT_COLUMN_LOCAL_SEND_TIME, msg.sendLocalTime,
					DBDefinition.CHAT_COLUMN_USER_ID, msg.uid);
			db.update(chatTable.getTableNameAndCreate(), msg.getContentValues(), where, null);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void updateMessage(MsgItem msg, ChatTable chatTable)
	{
		if (msg == null || StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return;

		try
		{
			String where = "";
			if (msg.channelType != DBDefinition.CHANNEL_TYPE_USER)
			{
				where = String.format(Locale.US, "%s = %s AND %s = '%s' AND %s = %s", 
						DBDefinition.CHAT_COLUMN_CREATE_TIME, msg.createTime,
						DBDefinition.CHAT_COLUMN_USER_ID, msg.uid,
						DBDefinition.CHAT_COLUMN_TYPE, msg.post);
			}
			else
			{
				where = String.format(Locale.US, "%s = '%s' AND %s = '%s'", DBDefinition.CHAT_COLUMN_MAIL_ID, msg.mailId,
						DBDefinition.CHAT_COLUMN_USER_ID, msg.uid);
			}

			db.update(chatTable.getTableNameAndCreate(), msg.getContentValues(), where, null);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void insertMessages(ArrayList<MsgItem> msgs, ChatTable chatTable)
	{
		insertMessages((MsgItem[]) msgs.toArray(new MsgItem[0]), chatTable);
	}

	public void insertMailData(MailData mailData, ChatChannel channel)
	{
		mailData.channelId = mailData.getChannelId();
		if (!isDBAvailable())
			return;

		try
		{
			db.beginTransaction();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
			return;
		}

		try
		{
			try
			{
				if (!isMailDataExists(mailData.getUid()))
				{
					db.insert(DBDefinition.TABEL_MAIL, DBDefinition.MAIL_ID, mailData.getContentValues());	
				}else{
					String where = String.format(Locale.US, "%s = '%s'", DBDefinition.MAIL_ID, mailData.getUid());
					db.update(DBDefinition.TABEL_MAIL, mailData.getContentValues(), where, null);
				}
				checkChannel(mailData, channel);
			}
			catch (Exception e)
			{
				LogUtil.trackMessage("mailData is not Valid uid=" + mailData.getUid());
				LogUtil.printException(e);
			}
			db.setTransactionSuccessful();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			try
			{
				db.endTransaction();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}
	
	
	public void updateMail(MailData mailData, ChatChannel channel)
	{
		if (mailData == null || !isDBAvailable())
			return;

		try
		{
			String where = String.format(Locale.US, "%s = '%s'", DBDefinition.MAIL_ID, mailData.getUid());

			db.update(DBDefinition.TABEL_MAIL, mailData.getContentValues(), where, null);
			checkChannel(mailData, channel);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void updateMail(MailData mailData)
	{
		if (mailData == null || !isDBAvailable())
			return;

		try
		{
			String where = String.format(Locale.US, "%s = '%s'", DBDefinition.MAIL_ID, mailData.getUid());

			db.update(DBDefinition.TABEL_MAIL, mailData.getContentValues(), where, null);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	/**
	 * 邮件现在有表，但没有插入，应该是因为seqId不符合约束条件
	 */
	public void insertMessages(MsgItem[] msgs, ChatTable chatTable)
	{
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return;

		String tableName = chatTable.getTableNameAndCreate();

		try
		{
			db.beginTransaction();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
			return;
		}

		try
		{
			for (MsgItem msg : msgs)
			{
				try
				{
					if ((chatTable.channelType != DBDefinition.CHANNEL_TYPE_USER && !isMsgExistsNew(chatTable, msg.uid, msg.createTime))
							|| (chatTable.channelType == DBDefinition.CHANNEL_TYPE_USER && !isUserMailExists(chatTable, msg.mailId)))
					{
						db.insert(tableName, null, msg.getContentValues());
						checkChannel(msg, chatTable);
					}

//					Map<String, MsgItem> map = ChannelManager.getInstance().getUnHandleRedPackageMap();
//					if (map != null && msg.isRedPackageMessage() && msg.sendState == MsgItem.UNHANDLE
//							&& StringUtils.isNotEmpty(msg.attachmentId))
//					{
//						String[] redPackageInfoArr = msg.attachmentId.split("\\|");
//						if(!map.containsKey(redPackageInfoArr[0])){
//							map.put(redPackageInfoArr[0], msg);
//						}
//					}
				}
				catch (Exception e)
				{
					LogUtil.trackMessage("MsgItem is not Valid sequenceId=" + msg.sequenceId + " channelType=" + msg.channelType);
					LogUtil.printException(e);
				}
			}
			db.setTransactionSuccessful();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			try
			{
				db.endTransaction();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	private void checkChannel(MsgItem msg, ChatTable chatTable)
	{
		ChatChannel channel = ChannelManager.getInstance().getChannel(chatTable);
		if (channel == null)
			return;

		if (msg.isNewMsg && !msg.isSelfMsg() && !ChatServiceController.isInTheSameChannel(chatTable.channelID))
		{
			channel.unreadCount++;
			updateChannel(channel);
		}

		if (channel.channelType != DBDefinition.CHANNEL_TYPE_USER)
		{
			// 新后台中seqId可能为-1
			if (msg.sequenceId > 0)
			{
				if (channel.dbMinSeqId == -1)
				{
					channel.dbMinSeqId = msg.sequenceId;
				}
				if (channel.dbMaxSeqId == -1)
				{
					channel.dbMaxSeqId = msg.sequenceId;
				}
				if (msg.sequenceId < channel.dbMinSeqId)
				{
					channel.dbMinSeqId = msg.sequenceId;
				}
				if (msg.sequenceId > channel.dbMaxSeqId)
				{
					channel.dbMaxSeqId = msg.sequenceId;
				}
			}

			// TODO 未兼容ws；不让latestId为"-1"
			if (msg.createTime > channel.latestTime
					|| (msg.createTime == channel.latestTime && StringUtils.isNotEmpty(channel.latestId) && msg.sequenceId > Integer
							.parseInt(channel.latestId)))
			{
				channel.latestTime = msg.createTime;
				if(msg.sequenceId > 0)
				{
					channel.latestId = "" + msg.sequenceId;
				}
			}
			updateChannel(channel);
		}
		else
		{
			if (msg.createTime > channel.latestTime)
			{
				channel.latestTime = msg.createTime;
				channel.latestId = msg.mailId;
				updateChannel(channel);
			}
		}
	}

	private void checkChannel(MailData mailData, ChatChannel channel)
	{
		if (channel == null)
			return;

		if (mailData.isUnread() && !ChatServiceController.isInTheSameChannel(mailData.getChannelId()))
		{
			channel.unreadCount++;
		}

		if (mailData.getCreateTime() > channel.latestTime)
		{
			channel.latestTime = mailData.getCreateTime();
			channel.latestId = mailData.getUid();
		}
		updateChannel(channel);
	}

	public void updateChannel(ChatChannel channel)
	{
		if (channel == null || StringUtils.isEmpty(channel.channelID) || !isDBAvailable())
			return;

		try
		{
			if (!isChannelExists(channel.channelID))
				insertChannel(channel);
			String where = String.format(Locale.US, "%s = '%s'", DBDefinition.CHANNEL_CHANNEL_ID, channel.channelID);
			db.update(DBDefinition.TABEL_CHANNEL, channel.getContentValues(), where, null);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public ArrayList<ChatChannel> getAllChannel()
	{
		ArrayList<ChatChannel> result = new ArrayList<ChatChannel>();

		if (!isDBAvailable() || !isTableExists(DBDefinition.TABEL_CHANNEL))
			return result;

		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s", DBDefinition.TABEL_CHANNEL);
			c = db.rawQuery(sql, null);
			if (c != null)
			{
				while (c.moveToNext())
				{
					ChatChannel channel = new ChatChannel(c);
					if (channel != null && !channel.channelID.equals(""))
					{
						result.add(channel);
					}
				}
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return result;
	}

	public ChatChannel getChannel(ChatTable chatTable)
	{
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return null;

		ChatChannel result = null;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = '%s'", DBDefinition.TABEL_CHANNEL,
					DBDefinition.CHANNEL_CHANNEL_ID, chatTable.channelID);
			c = db.rawQuery(sql, null);

			if (!c.moveToFirst())
			{
				return null;
			}

			result = new ChatChannel(c);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return result;
	}

	public void deleteChannel(ChatTable chatTable)
	{
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return;

		db.beginTransaction();
		try
		{
			//if (chatTable.channelType != DBDefinition.CHANNEL_TYPE_CHATROOM)
			//{
				String where = String.format(Locale.US, "%s = %d AND %s = '%s'", DBDefinition.CHANNEL_TYPE, chatTable.channelType,
						DBDefinition.CHANNEL_CHANNEL_ID, chatTable.channelID);
				db.delete(DBDefinition.TABEL_CHANNEL, where, null);
			//}


			db.execSQL("DROP TABLE IF EXISTS '" + chatTable.getTableName() + "'");

			db.setTransactionSuccessful();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			try
			{
				db.endTransaction();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	public void dropTableWithTableName(String tableName)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "tableName",tableName);
		if (StringUtils.isEmpty(tableName) || !isDBAvailable())
			return;

		db.beginTransaction();
		try
		{
			db.execSQL("DROP TABLE IF EXISTS '" + tableName + "'");
			db.setTransactionSuccessful();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			try
			{
				db.endTransaction();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	public void deleteDialogMailChannel(ChatTable chatTable)
	{
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return;
		Cursor c = null;
		db.beginTransaction();
		try
		{
			String where = String.format(Locale.US, "%s = %d AND %s = '%s'", DBDefinition.CHANNEL_TYPE, chatTable.channelType,
					DBDefinition.CHANNEL_CHANNEL_ID, chatTable.channelID);
			db.delete(DBDefinition.TABEL_CHANNEL, where, null);

			String where2 = getSqlByChannelId(chatTable.channelID) + " AND " + DBDefinition.MAIL_REWARD_STATUS + " <> 0 AND "
					+ DBDefinition.MAIL_SAVE_FLAG + " <> 1";
			where2 = where2.replace("WHERE", "");
			db.delete(DBDefinition.TABEL_MAIL, where2, null);

			db.setTransactionSuccessful();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
			try
			{
				db.endTransaction();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	public void deleteSysMailChannel(ChatTable chatTable)
	{
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return;
		Cursor c = null;
		db.beginTransaction();
		try
		{
			String where = String.format(Locale.US, "%s = %d AND %s = '%s'", DBDefinition.CHANNEL_TYPE, chatTable.channelType,
					DBDefinition.CHANNEL_CHANNEL_ID, chatTable.channelID);
			db.delete(DBDefinition.TABEL_CHANNEL, where, null);

			String where2 = String.format(Locale.US, "%s = '%s' AND %s <> %d AND %s <> %d", DBDefinition.MAIL_CHANNEL_ID,
					chatTable.channelID, DBDefinition.MAIL_REWARD_STATUS, 0, DBDefinition.MAIL_SAVE_FLAG, 1);
			db.delete(DBDefinition.TABEL_MAIL, where2, null);

			db.setTransactionSuccessful();

			if (chatTable.channelID.equals(MailManager.CHANNELID_FIGHT)||chatTable.channelID.equals(MailManager.CHANNELID_ARENAGAME))
			{
				getDetectMailInfo();
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
			try
			{
				db.endTransaction();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	public void deleteSysMailFromDB(String mailId)
	{
		if (StringUtils.isEmpty(mailId) || !isDBAvailable())
			return;
		db.beginTransaction();
		try
		{
			String where = String.format(Locale.US, "%s = '%s'", DBDefinition.MAIL_ID, mailId);
			db.delete(DBDefinition.TABEL_MAIL, where, null);

			db.setTransactionSuccessful();

			MailData deleteMail = getSysMailByID(mailId);
			if (deleteMail != null && (deleteMail.getType() == MailManager.MAIL_DETECT_REPORT||deleteMail.getType() == MailManager.Mail_NEW_SCOUT_REPORT_FB))
			{
				getDetectMailInfo();
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			try
			{
				db.endTransaction();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	//清空系统系统邮件
	public void clearSysMailFromDB()
	{
		if (!isDBAvailable())
			return;
		db.beginTransaction();
		try
		{
			//db.delete(DBDefinition.TABEL_MAIL, null, null);
			db.execSQL("DELETE FROM Mail ");

			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.printException(e);
		}
		finally
		{
			try
			{
				db.endTransaction();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}



	public void deleteSysMailFromDBWithFailTime()
	{
		if (!isDBAvailable())
			return;
		db.beginTransaction();
		try
		{
			long time = TimeManager.getInstance().getCurrentTimeMS();
			String where = String.format(Locale.US, "%s <> 0 AND %s < %s ", DBDefinition.MAIL_FAIL_TIME,DBDefinition.MAIL_FAIL_TIME, time);
			int count = db.delete(DBDefinition.TABEL_MAIL, where, null);
			db.setTransactionSuccessful();
			Log.d("deleteSysMailFromDBWithFailTime ", "deleteSysMail-delete = " + count);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			try
			{
				db.endTransaction();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	public void deleteSysMail(ChatChannel channel, String mailId)
	{
		if (StringUtils.isEmpty(mailId) || !isDBAvailable())
			return;

		db.beginTransaction();
		try
		{
			String where2 = String.format(Locale.US, "%s = '%s'", DBDefinition.MAIL_ID, mailId);
			db.delete(DBDefinition.TABEL_MAIL, where2, null);

			if (channel.latestId.equals(mailId) && channel.mailDataList != null && channel.mailDataList.size() > 0)
			{
				String latestMailId = "";
				int latestMailCreateTime = 0;
				for (int i = 0; i < channel.mailDataList.size(); i++)
				{
					MailData mailData = channel.mailDataList.get(i);
					if (mailData.getCreateTime() > latestMailCreateTime)
					{
						latestMailCreateTime = mailData.getCreateTime();
						latestMailId = mailData.getUid();
					}
				}
				if (!latestMailId.equals("") && latestMailCreateTime != 0)
				{
					channel.latestId = latestMailId;
					channel.latestTime = latestMailCreateTime;
					String where = String.format(Locale.US, "%s = '%s'", DBDefinition.CHANNEL_CHANNEL_ID, channel.channelID);
					db.update(DBDefinition.TABEL_CHANNEL, channel.getContentValues(), where, null);
					channel.refreshRenderData();
				}
			}

			db.setTransactionSuccessful();

			MailData deleteMail = getSysMailByID(mailId);
			if (deleteMail != null && (deleteMail.getType() == MailManager.MAIL_DETECT_REPORT||deleteMail.getType() == MailManager.Mail_NEW_SCOUT_REPORT_FB))
			{
				getDetectMailInfo();
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			try
			{
				db.endTransaction();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}
	
	// 先不对msg判断，因为可能包含了"'"，还需要对字符串进行转义
	public boolean isMsgExistsNew(ChatTable chatTable, String uid, long createTime)
	{
		if (chatTable == null || StringUtils.isEmpty(chatTable.getTableNameAndCreate()) || !isDBAvailable())
			return false;

		int count = 0;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = '%s' AND %s = %d", chatTable.getTableNameAndCreate(),
					DBDefinition.CHAT_COLUMN_USER_ID, uid, DBDefinition.CHAT_COLUMN_CREATE_TIME, createTime);
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return false;
			}
			count = c.getInt(0);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return count > 0;
	}

	public boolean isMsgExists(ChatTable chatTable, int seqId, long createTime)
	{
		if (chatTable == null || StringUtils.isEmpty(chatTable.getTableNameAndCreate()) || !isDBAvailable())
			return false;

		int count = 0;
		Cursor c = null;
		try
		{
			String sql;
			if (createTime != -1)
			{
				sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = %d AND %s = %d", chatTable.getTableNameAndCreate(),
						DBDefinition.CHAT_COLUMN_SEQUENCE_ID, seqId, DBDefinition.CHAT_COLUMN_CREATE_TIME, createTime);
			}
			else
			{
				sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = %d", chatTable.getTableNameAndCreate(),
						DBDefinition.CHAT_COLUMN_SEQUENCE_ID, seqId);
			}
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return false;
			}
			count = c.getInt(0);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return count > 0;
	}

	public boolean isMsgExistsLoad(ChatTable chatTable, int seqId,int maxSeqId, long createTime)
	{
		if (chatTable == null || StringUtils.isEmpty(chatTable.getTableNameAndCreate()) || !isDBAvailable())
			return false;

		int count = 0;
		Cursor c = null;
		try
		{
			String sql;
//			sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = %d", chatTable.getTableNameAndCreate(),
//					DBDefinition.CHAT_COLUMN_SEQUENCE_ID, seqId);
			sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s >= %d AND %s < %d ORDER BY %s DESC",
					chatTable.getTableNameAndCreate(), DBDefinition.CHAT_COLUMN_SEQUENCE_ID,
					seqId, DBDefinition.CHAT_COLUMN_SEQUENCE_ID,maxSeqId,DBDefinition.CHAT_COLUMN_SEQUENCE_ID);
			c = db.rawQuery(sql, null);
			while (c != null && c.moveToNext())
			{
				long createTimeDB = c.getLong(c.getColumnIndex(DBDefinition.CHAT_COLUMN_CREATE_TIME));
				if(createTimeDB + ChannelManager.DURATIONTIME >= createTime){
					count += 1;
				}
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
			if (count >=18 && count <= 20 ) {
				return true;//从数据库拉取数据
			}else{
				return false;//从服务器拉取数据
			}
		}
	}

	public boolean isMessageChannelExist()
	{
		if (!isDBAvailable())
			return false;

		int count = 0;
		Cursor c = null;
		try
		{
			String sql = "SELECT * FROM Channel WHERE (ChannelType = 2 AND ChannelID NOT LIKE '%@mod' AND ChannelID <> 'mod' AND ChannelID <> 'message') OR ChannelType = 3";
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return false;
			}
			count = c.getInt(0);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return count > 0;
	}

	public boolean isModChannelExist()
	{
		if (!isDBAvailable())
			return false;

		int count = 0;
		Cursor c = null;
		try
		{
			String sql = "SELECT * FROM Channel WHERE ChannelType = 2 AND ChannelID LIKE '%@mod' AND ChannelID <> 'mod'";
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return false;
			}
			count = c.getInt(0);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return count > 0;
	}

	public boolean isChannelExists(String channelID)
	{
		if (StringUtils.isEmpty(channelID) || !isDBAvailable())
			return false;

		int count = 0;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = '%s'", DBDefinition.TABEL_CHANNEL,
					DBDefinition.CHANNEL_CHANNEL_ID, channelID);
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return false;
			}
			count = c.getInt(0);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return count > 0;
	}

	public boolean isUserMailExists(ChatTable chatTable, String mailId)
	{
		if (chatTable == null || StringUtils.isEmpty(chatTable.getTableNameAndCreate()) || !isDBAvailable())
			return false;

		int count = 0;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = '%s'", chatTable.getTableNameAndCreate(),
					DBDefinition.CHAT_COLUMN_MAIL_ID, mailId);
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return false;
			}
			count = c.getInt(0);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return count > 0;
	}

	public boolean isMailDataExists(String mailId)
	{
		if (!isDBAvailable())
			return false;

		int count = 0;
		Cursor c = null;
		try
		{
			String sql = String
					.format(Locale.US, "SELECT * FROM %s WHERE %s = '%s'", DBDefinition.TABEL_MAIL, DBDefinition.MAIL_ID, mailId);
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return false;
			}
			count = c.getInt(0);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return count > 0;
	}

	public String getSystemMailLatestId()
	{
		if (!isDBAvailable())
			return "";
		String latestId = "";
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = (SELECT %s(%s) FROM %s where Flag = 1)", DBDefinition.TABEL_MAIL,
					DBDefinition.MAIL_CREATE_TIME, "max", DBDefinition.MAIL_CREATE_TIME, DBDefinition.TABEL_MAIL);
			c = db.rawQuery(sql, null);
			if (c == null || (c != null && !c.moveToFirst()))
			{
				return latestId;
			}
			latestId = c.getString(c.getColumnIndex(DBDefinition.MAIL_ID));
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return latestId;
	}

	public long getSystemMailLatestModifyTime()
	{
		if (!isDBAvailable())
			return -1;
		long latestModifyTime = -1;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = (SELECT %s(%s) FROM %s)", DBDefinition.TABEL_CHANNEL,
					DBDefinition.CHANNEL_LATEST_MODIFY_TIME, "max", DBDefinition.CHANNEL_LATEST_MODIFY_TIME, DBDefinition.TABEL_CHANNEL);
			c = db.rawQuery(sql, null);
			if (c == null || (c != null && !c.moveToFirst()))
			{
				return latestModifyTime;
			}
			latestModifyTime = c.getLong(c.getColumnIndex(DBDefinition.CHANNEL_LATEST_MODIFY_TIME));
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return latestModifyTime;
	}

	public long getChatLatestTime(ChatTable chatTable)
	{
		if (chatTable == null || StringUtils.isEmpty(chatTable.getTableNameAndCreate()) || !isDBAvailable())
			return 0;
		String chatTableName = chatTable.getTableNameAndCreate();
		long latestTime = 0;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT max(%s),%s FROM %s%s", DBDefinition.CHAT_COLUMN_CREATE_TIME,
					DBDefinition.CHAT_COLUMN_CREATE_TIME, chatTableName, getDoNotIncludeWSMsgSQL(chatTable.channelType, true));
			c = db.rawQuery(sql, null);
			if (c == null || (c != null && !c.moveToFirst()))
			{
				return latestTime;
			}
			if (c.getColumnIndex(DBDefinition.CHAT_COLUMN_CREATE_TIME) >= 0)
			{
				latestTime = c.getLong(c.getColumnIndex(DBDefinition.CHAT_COLUMN_CREATE_TIME));
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return latestTime;
	}
	
	private String getDoNotIncludeWSMsgSQL(int channelType, boolean isWhere)
	{
		String claus = (isWhere ? " WHERE " : " AND ") + DBDefinition.CHAT_COLUMN_SEQUENCE_ID + " > 0";
		return (!WebSocketManager.isWebSocketEnabled() && WebSocketManager.isSupportedType(channelType)) ? claus : "";
	}

	public MsgItem getChatLatestMsg(ChatTable chatTable)
	{
		if (chatTable == null || StringUtils.isEmpty(chatTable.getTableNameAndCreate()) || !isDBAvailable())
			return null;
		String chatTableName = chatTable.getTableNameAndCreate();
		
		MsgItem latestMsgItem = null;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = (SELECT max(%s) FROM %s%s)", chatTableName,
					DBDefinition.CHAT_COLUMN_CREATE_TIME, DBDefinition.CHAT_COLUMN_CREATE_TIME, chatTableName,
					getDoNotIncludeWSMsgSQL(chatTable.channelType, true));
			c = db.rawQuery(sql, null);

			if(!WebSocketManager.isWebSocketEnabled()){
				int maxSequeueId = 0;
				while (c != null && c.moveToNext())
				{
					MsgItem msg = new MsgItem(c);
					if (msg != null && msg.sequenceId > maxSequeueId)
					{
						latestMsgItem = msg;
						maxSequeueId = msg.sequenceId;
					}
				}
			}else{
				int id = 0;
				while (c != null && c.moveToNext())
				{
					MsgItem msg = new MsgItem(c);
					if (msg != null && msg._id > id)
					{
						latestMsgItem = msg;
						id = msg._id;
					}
				}
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return latestMsgItem;
	}

	public long getSysMailChannelLatestTime(String channelId)
	{
		if (!isDBAvailable())
			return 0;
		long latestTime = 0;
		Cursor c = null;
		try
		{
			String where = getSqlByChannelId(channelId);
			if (StringUtils.isEmpty(where))
				return 0;
			String sql = String.format(Locale.US, "SELECT max(%s),%s FROM %s %s", DBDefinition.MAIL_CREATE_TIME,
					DBDefinition.MAIL_CREATE_TIME, DBDefinition.TABEL_MAIL, where);
			c = db.rawQuery(sql, null);
			if (c == null || (c != null && !c.moveToFirst()))
			{
				return latestTime;
			}
			if (c.getColumnIndex(DBDefinition.MAIL_CREATE_TIME) >= 0)
				latestTime = c.getLong(c.getColumnIndex(DBDefinition.MAIL_CREATE_TIME));
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return latestTime;
	}

	public int getMaxDBSeqId(ChatTable chatTable)
	{
		if (chatTable == null || StringUtils.isEmpty(chatTable.getTableNameAndCreate()) || !isDBAvailable())
			return 0;

		int maxSeqId = 0;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT max(%s),%s FROM %s WHERE %s >= 0", DBDefinition.CHAT_COLUMN_CREATE_TIME,
					DBDefinition.CHAT_COLUMN_SEQUENCE_ID, chatTable.getTableNameAndCreate(), DBDefinition.CHAT_COLUMN_SEQUENCE_ID);
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return 0;
			}
			if (c.getColumnIndex(DBDefinition.CHAT_COLUMN_SEQUENCE_ID) > 0)
				maxSeqId = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_SEQUENCE_ID));
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return maxSeqId;
	}

	public int getMinDBSeqId(ChatTable chatTable)
	{
		if (chatTable == null || StringUtils.isEmpty(chatTable.getTableNameAndCreate()) || !isDBAvailable())
			return 0;

		int minSeqId = 0;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT min(%s),%s FROM %s WHERE %s >= 0", DBDefinition.CHAT_COLUMN_CREATE_TIME,
					DBDefinition.CHAT_COLUMN_SEQUENCE_ID, chatTable.getTableNameAndCreate(), DBDefinition.CHAT_COLUMN_SEQUENCE_ID);
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return 0;
			}
			if (c.getColumnIndex(DBDefinition.CHAT_COLUMN_SEQUENCE_ID) > 0)
				minSeqId = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_SEQUENCE_ID));
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return minSeqId;
	}

	public boolean hasMailDataInDB(String channelId)
	{
		if (!isDBAvailable())
			return false;
		int count = 0;
		Cursor c = null;
		try
		{
			String where = getSqlByChannelId(channelId);
			if (StringUtils.isEmpty(where))
				return false;

			String sql = String.format(Locale.US, "SELECT * FROM %s %s", DBDefinition.TABEL_MAIL, where);
			c = db.rawQuery(sql, null);
			if (c == null || !c.moveToFirst())
			{
				return false;
			}
			count = c.getInt(0);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return count > 0;
	}

	public String getSysMailChannelLatestId(String channelId)
	{
		if (!isDBAvailable())
			return "";
		String latestId = "";
		Cursor c = null;
		try
		{
			String where = getSqlByChannelId(channelId);
			if (StringUtils.isEmpty(where))
				return "";

			String sql = String.format(Locale.US, "SELECT max(%s),%s FROM %s %s", DBDefinition.MAIL_CREATE_TIME, DBDefinition.MAIL_ID,
					DBDefinition.TABEL_MAIL, where);
			c = db.rawQuery(sql, null);
			if (c == null || (c != null && !c.moveToFirst()))
			{
				return latestId;
			}
			latestId = c.getString(c.getColumnIndex(DBDefinition.MAIL_ID));
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return latestId;
	}

	public String getLatestId(ChatTable chatTable)
	{
		if (chatTable == null || StringUtils.isEmpty(chatTable.getTableNameAndCreate()) || !isDBAvailable())
			return "";

		String latestId = "";
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = (SELECT %s(%s) FROM %s WHERE Type <> 150)", chatTable.getTableNameAndCreate(),
					DBDefinition.MAIL_CREATE_TIME, "max", DBDefinition.MAIL_CREATE_TIME, chatTable.getTableNameAndCreate());
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return latestId;
			}
			if (chatTable.channelType == DBDefinition.CHANNEL_TYPE_USER)
				latestId = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_MAIL_ID));
			else if (chatTable.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
				latestId = c.getString(c.getColumnIndex(DBDefinition.MAIL_ID));
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return latestId;
	}

	/**
	 * 个人信息邮件专用
	 * @author lzh
	 * @time 17/3/3 下午4:21
	 */
	public String getUserMsgLatestId(ChatTable chatTable)
	{
		if (chatTable == null || StringUtils.isEmpty(chatTable.getTableNameAndCreate()) || !isDBAvailable())
			return "";

		String latestId = "";
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = (SELECT %s(%s) FROM %s WHERE Type <> 150)", chatTable.getTableNameAndCreate(),
					DBDefinition.MAIL_CREATE_TIME, "max", DBDefinition.MAIL_CREATE_TIME, chatTable.getTableNameAndCreate());
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return latestId;
			}
			if (chatTable.channelType == DBDefinition.CHANNEL_TYPE_USER)
				latestId = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_MAIL_ID));
			else if (chatTable.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
				latestId = c.getString(c.getColumnIndex(DBDefinition.MAIL_ID));
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return latestId;
	}

//	public int getMarginalSequenceNumber(String chatTable, boolean isUpper)
//	{
//		if (StringUtils.isEmpty(chatTable) || !isDBAvailable())
//			return 0;
//
//		int sequenceNumber = 0;
//		Cursor c = null;
//		try
//		{
//			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = (SELECT %s(%s) FROM %s)", chatTable,
//					DBDefinition.CHAT_COLUMN_SEQUENCE_ID, isUpper ? "max" : "min", DBDefinition.CHAT_COLUMN_SEQUENCE_ID, chatTable);
//			c = db.rawQuery(sql, null);
//			if (!c.moveToFirst())
//			{
//				return sequenceNumber;
//			}
//			sequenceNumber = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_SEQUENCE_ID));
//		}
//		catch (Exception e)
//		{
//			LogUtil.printException(e);
//		}
//		finally
//		{
//			if (c != null)
//				c.close();
//		}
//		return sequenceNumber;
//	}

	public MailData getSysMailByID(String mailID)
	{
		MailData mailData = null;
		if (StringUtils.isEmpty(mailID) || !isDBAvailable())
			return mailData;

		Cursor c = null;
		try
		{
			String sql = String
					.format(Locale.US, "SELECT * FROM %s WHERE %s = '%s'", DBDefinition.TABEL_MAIL, DBDefinition.MAIL_ID, mailID);
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return mailData;
			}
			else
			{
				mailData = new MailData(c);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return mailData;
	}

	public MsgItem getUserMailByID(ChatTable chatTable, String mailID)
	{
		MsgItem msg = null;
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return msg;

		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = '%s'", chatTable.getTableNameAndCreate(),
					DBDefinition.CHAT_COLUMN_MAIL_ID, mailID);
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return msg;
			}
			else
			{
				msg = new MsgItem(c);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return msg;
	}

	private boolean isUserMailExistByType(ChatTable chatTable, int type)
	{
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return false;

		boolean result = false;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = %d LIMIT 1", chatTable.getTableNameAndCreate(),
					DBDefinition.CHAT_COLUMN_TYPE, type);
			c = db.rawQuery(sql, null);
			if (c.moveToFirst())
			{
				result = true;
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return result;
	}

	public boolean isUserMailExistDifferentType(ChatTable chatTable, int type)
	{
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return false;

		boolean result = false;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s <> %d LIMIT 1", chatTable.getTableNameAndCreate(),
					DBDefinition.CHAT_COLUMN_TYPE, type);
			c = db.rawQuery(sql, null);
			if (c.moveToFirst())
			{
				result = true;
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return result;
	}

	public MsgItem getChatBySequeueId(ChatTable chatTable, int seqId)
	{
		MsgItem msg = null;
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable() || seqId < 1)
			return msg;

		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = %d", chatTable.getTableNameAndCreate(),
					DBDefinition.CHAT_COLUMN_SEQUENCE_ID, seqId);
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return msg;
			}
			else
			{
				msg = new MsgItem(c);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return msg;
	}

	public MsgItem getChatBySequeueIdAndCreatTime(ChatTable chatTable, int seqId ,int minCreatTime)
	{
		MsgItem msg = null;
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable() || seqId < 1)
			return msg;

		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s = %d AND %s >= %d AND %s <= %d", chatTable.getTableNameAndCreate(),
					DBDefinition.CHAT_COLUMN_SEQUENCE_ID, seqId,DBDefinition.CHAT_COLUMN_CREATE_TIME,minCreatTime - ChannelManager.DURATIONTIME,DBDefinition.CHAT_COLUMN_CREATE_TIME,minCreatTime);
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return msg;
			}
			else
			{
				msg = new MsgItem(c);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return msg;
	}

	public List<MsgItem> getMsgsByTime(ChatTable chatTable, int createTime, int countLimit)
	{
		ArrayList<MsgItem> msgs = new ArrayList<MsgItem>();
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return msgs;

		Cursor c = null;
		try
		{
			c = queryMsgsByCreateTime(chatTable, createTime, countLimit);
			while (c != null && c.moveToNext())
			{
				MsgItem msg = new MsgItem(c);
				msgs.add(msg);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return msgs;
	}

	public boolean hasMsgItemInTable(ChatTable chatTable)
	{
		if (!isDBAvailable())
			return false;
		Cursor c = null;
		int count = 0;
		try
		{
			String sql = String.format(Locale.US, "SELECT * FROM %s%s", chatTable.getTableNameAndCreate(), getDoNotIncludeWSMsgSQL(chatTable.channelType, true));
			c = db.rawQuery(sql, null);
			if (c == null || !c.moveToFirst())
			{
				return false;
			}
			count = c.getInt(0);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return count > 0;
	}

	private Cursor queryMsgsByCreateTime(ChatTable chatTable, int createTime, int countLimit)
	{
		if (!isDBAvailable())
			return null;
		String sql = "";
		if (createTime > 0)
		{
			sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s < %d%s ORDER BY %s DESC", chatTable.getTableNameAndCreate(),
					DBDefinition.CHAT_COLUMN_CREATE_TIME, createTime, getDoNotIncludeWSMsgSQL(chatTable.channelType, false), DBDefinition.CHAT_COLUMN_CREATE_TIME);
		}
		else
		{
			sql = String.format(Locale.US, "SELECT * FROM %s%s ORDER BY %s DESC", chatTable.getTableNameAndCreate(), getDoNotIncludeWSMsgSQL(chatTable.channelType, true),
					DBDefinition.CHAT_COLUMN_CREATE_TIME);
		}

		if (countLimit > 0)
		{
			sql += " LIMIT " + countLimit;
		}

		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public List<MsgItem> getChatMsgBySection(ChatTable chatTable, int upperId, int lowerId,int minCreateTime)
	{
		ArrayList<MsgItem> msgs = new ArrayList<MsgItem>();
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return msgs;

		Cursor c = null;
		try
		{
			c = queryChatBySection(chatTable, upperId, lowerId,minCreateTime);
			while (c != null && c.moveToNext())
			{
				MsgItem msg = new MsgItem(c);
				msgs.add(msg);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return msgs;
	}

	/**
	 * 在db中查找SequenceID小于upperSeqId的至多count条消息，返回这些消息的最小和最大SequenceID值，
	 * 如果找不到则返回null
	 */
	public Point getHistorySeqIdRange(ChatTable chatTable, int upperSeqId, int count)
	{
		Point result = null;
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return result;

		Cursor c = null;
		try
		{

			String sql = String.format(Locale.US, "SELECT %s FROM %s WHERE %s < %d AND %s > 0 ORDER BY %s DESC LIMIT %d",
					DBDefinition.CHAT_COLUMN_SEQUENCE_ID, chatTable.getTableNameAndCreate(), DBDefinition.CHAT_COLUMN_SEQUENCE_ID,
					upperSeqId, DBDefinition.CHAT_COLUMN_SEQUENCE_ID, DBDefinition.CHAT_COLUMN_SEQUENCE_ID, count);
			c = db.rawQuery(sql, null);
			while (c != null && c.moveToNext())
			{
				int seqId = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_SEQUENCE_ID));
				if (result == null)
				{
					result = new Point(seqId, seqId);
				}
				else
				{
					result.x = seqId;
				}
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return result;
	}

	/**
	 * 在db中查找SequenceID小于upperSeqId的至多count条消息，返回这些消息的最小和最大SequenceID值，
	 * 如果找不到则返回null
	 */
	public Point getHistorySeqIAndTimedRange(ChatTable chatTable, int upperSeqId, int minCreatTime,int count)
	{
		Point result = null;
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return result;

		Cursor c = null;
		try
		{

			String sql = String.format(Locale.US, "SELECT %s FROM %s WHERE %s < %d AND %s > 0 AND %s >= %d AND %s <= %d ORDER BY %s DESC LIMIT %d",
					DBDefinition.CHAT_COLUMN_SEQUENCE_ID, chatTable.getTableNameAndCreate(), DBDefinition.CHAT_COLUMN_SEQUENCE_ID,
					upperSeqId, DBDefinition.CHAT_COLUMN_SEQUENCE_ID,DBDefinition.CHAT_COLUMN_CREATE_TIME,minCreatTime - ChannelManager.DURATIONTIME,
					DBDefinition.CHAT_COLUMN_CREATE_TIME,minCreatTime, DBDefinition.CHAT_COLUMN_SEQUENCE_ID, count);
			c = db.rawQuery(sql, null);
			while (c != null && c.moveToNext())
			{
				int seqId = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_SEQUENCE_ID));
				if (result == null)
				{
					result = new Point(seqId, seqId);
				}
				else
				{
					result.x = seqId;
				}
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return result;
	}

	public Pair<Long, Long> getHistoryTimeRange(ChatTable chatTable, int upperTime, int count)
	{
		Pair<Long, Long> result = null;
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return result;

		Cursor c = null;
		try
		{

			String sql = String.format(Locale.US, "SELECT %s FROM %s WHERE %s < %d%s ORDER BY %s DESC LIMIT %d",
					DBDefinition.CHAT_COLUMN_CREATE_TIME, chatTable.getTableNameAndCreate(), DBDefinition.CHAT_COLUMN_CREATE_TIME,
					upperTime, getDoNotIncludeWSMsgSQL(chatTable.channelType, false), DBDefinition.CHAT_COLUMN_CREATE_TIME, count);
			c = db.rawQuery(sql, null);
			while (c != null && c.moveToNext())
			{
				long createTime = c.getLong(c.getColumnIndex(DBDefinition.CHAT_COLUMN_CREATE_TIME));
				if (result == null)
				{
					result = new Pair<Long, Long>(createTime, createTime);
				}
				else
				{
					result = new Pair<Long, Long>(createTime, result.second);
				}
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return result;
	}

	private Cursor queryChatBySection(ChatTable chatTable, int upperId, int lowerId, int minCreateTime)
	{
		if (!isDBAvailable())
			return null;
		int minId = Math.min(upperId, lowerId);
		int maxId = Math.max(upperId, lowerId);
		String sql = String.format(Locale.US, "SELECT * FROM %s WHERE %s >= %d AND %s <= %d AND %s >=%d AND %s < %d ORDER BY %s DESC",
				chatTable.getTableNameAndCreate(), DBDefinition.CHAT_COLUMN_SEQUENCE_ID, minId, DBDefinition.CHAT_COLUMN_SEQUENCE_ID,
				maxId, DBDefinition.CHAT_COLUMN_CREATE_TIME,minCreateTime - ChannelManager.DURATIONTIME,DBDefinition.CHAT_COLUMN_CREATE_TIME,minCreateTime,DBDefinition.CHAT_COLUMN_SEQUENCE_ID);
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public int getUnreadCountOfSysMail(ChatTable chatTable)
	{
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return 0;

		int count = 0;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT COUNT(*) FROM %s %s AND %s = %s", DBDefinition.TABEL_MAIL,
					getSqlByChannelId(chatTable.channelID), DBDefinition.MAIL_STATUS, 0);
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return 0;
			}
			count = c.getInt(0);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return count;
	}

	public int getAllCountOfSysMailInChannel(ChatTable chatTable)
	{
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return 0;

		int count = 0;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT COUNT(*) FROM %s %s ", DBDefinition.TABEL_MAIL,
					getSqlByChannelId(chatTable.channelID));
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return 0;
			}
			count = c.getInt(0);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return count;
	}

	public ArrayList<MailData> getSysMailByTime(ChatTable chatTable, int createTime, int limitCount)
	{
		ArrayList<MailData> mails = new ArrayList<MailData>();
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return mails;

		Cursor c = null;
		try
		{
            c = queryMailByCreateTime(chatTable.channelID, createTime, limitCount, false);

			while (c != null && c.moveToNext())
			{
				MailData mail = new MailData(c);
				mails.add(mail);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return mails;
	}

	public int getSysMailDBCountByTime(ChatTable chatTable, int createTime)
	{
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return 0;
		int count = 0;
		Cursor c = null;
		try
		{
            c = queryMailByCreateTime(chatTable.channelID, createTime, -1, true);
			if (!c.moveToFirst())
			{
				return 0;
			}
			count = c.getInt(0);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return count;
	}
	
	public int getUnreadSysMailDBCountByTime(ChatTable chatTable, int createTime)
	{
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return 0;
		int count = 0;
		Cursor c = null;
		try
		{
			c = queryUnreadMailByCreateTime(chatTable.channelID, createTime, -1, true);

			if (c != null && c.moveToFirst())
			{
				count = c.getInt(0);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			closeCursor(c);
		}

		return count;
	}
	
	public int getLoadMoreMaxSeqId(ChatTable chatTable ,int endSeqId)
	{
		if (chatTable == null || StringUtils.isEmpty(chatTable.getTableNameAndCreate()) || !isDBAvailable())
			return endSeqId - 1;

		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT max(%s),%s FROM %s WHERE %s < %d AND %s > 0 %s",
					DBDefinition.CHAT_COLUMN_SEQUENCE_ID, DBDefinition.CHAT_COLUMN_SEQUENCE_ID, chatTable.getTableNameAndCreate(),
					DBDefinition.CHAT_COLUMN_SEQUENCE_ID, endSeqId, DBDefinition.CHAT_COLUMN_SEQUENCE_ID, 
					UserManager.getInstance().getShieldSql());
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst() || c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_SEQUENCE_ID)) <=0)
			{
				String sql2 = String.format(Locale.US, "SELECT min(%s),%s FROM %s WHERE %s < %d AND %s > 0",
						DBDefinition.CHAT_COLUMN_SEQUENCE_ID, DBDefinition.CHAT_COLUMN_SEQUENCE_ID, chatTable.getTableNameAndCreate(),
						DBDefinition.CHAT_COLUMN_SEQUENCE_ID, endSeqId, DBDefinition.CHAT_COLUMN_SEQUENCE_ID);
				Cursor c2 = db.rawQuery(sql2, null);
				if (c2.moveToFirst() && c2.getInt(c2.getColumnIndex(DBDefinition.CHAT_COLUMN_SEQUENCE_ID)) > 0)
				{
					return c2.getInt(c2.getColumnIndex(DBDefinition.CHAT_COLUMN_SEQUENCE_ID)) - 1;
				}
				return endSeqId - 1;
			}
			else
			{
				return c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_SEQUENCE_ID));
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return endSeqId - 1;
	}

	public ArrayList<MailData> getSysMailFromDB(String channelId, int configType)
	{
		ArrayList<MailData> mails = new ArrayList<MailData>();
		if (StringUtils.isEmpty(channelId) || !isDBAvailable())
			return mails;

		Cursor c = null;
		try
		{
			c = queryMail(channelId, configType);

			while (c != null && c.moveToNext())
			{
				MailData mail = new MailData(c);
				mails.add(mail);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return mails;
	}

	public int getSysMailCountByTypeInDB(String channelId)
	{
		if (StringUtils.isEmpty(channelId) && !isDBAvailable() || StringUtils.isEmpty(getSqlByChannelId(channelId)))
			return 0;

		Cursor c = null;
		try
		{
			String sql = "SELECT count(*) FROM " + DBDefinition.TABEL_MAIL + getSqlByChannelId(channelId);
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return 0;
			}
			return c.getInt(0);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return 0;
	}

	private Cursor queryMailByCreateTime(ChatTable chatTable, int createTime, int limitCount, boolean needCount)
	{
		if (!isDBAvailable())
			return null;
		String sql = "";

		String selectObject = "*";
		String descStr = " ORDER BY " + DBDefinition.MAIL_CREATE_TIME + " DESC";
		if (needCount)
		{
			selectObject = "COUNT(*)";
			descStr = "";
		}
		if (createTime == -1)
		{
			if (chatTable.isChannelType())
			{
				int type = chatTable.getMailTypeByChannelId();
				if (type > 0)
				{
					sql = String.format(Locale.US, "SELECT %s FROM %s WHERE %s='%s' OR %s=%d " + descStr, selectObject,
							DBDefinition.TABEL_MAIL, DBDefinition.MAIL_CHANNEL_ID, chatTable.channelID, DBDefinition.MAIL_TYPE, type);
				}
			}
			else
			{
				sql = String.format(Locale.US, "SELECT %s FROM %s WHERE %s='%s' AND %s<>%d AND %s<>%d AND %s<>%d AND %s<>%d" + descStr, selectObject,
						DBDefinition.TABEL_MAIL, DBDefinition.MAIL_CHANNEL_ID, chatTable.channelID, DBDefinition.MAIL_TYPE,
						MailManager.MAIL_RESOURCE, DBDefinition.MAIL_TYPE, MailManager.MAIL_ATTACKMONSTER,
						DBDefinition.MAIL_TYPE, MailManager.MAIL_MISSILE, DBDefinition.MAIL_TYPE,
						MailManager.MAIL_RESOURCE_HELP,DBDefinition.MAIL_TYPE,
						MailManager.MAIL_GIFT_BUY_EXCHANGE);
			}

		}
		else
		{
			if (chatTable.isChannelType())
			{
				int type = chatTable.getMailTypeByChannelId();
				if (type > 0)
				{
					sql = String.format(Locale.US, "SELECT %s FROM %s WHERE ( %s='%s' OR %s=%d ) AND %s <= %d " + descStr, selectObject,
							DBDefinition.TABEL_MAIL, DBDefinition.MAIL_CHANNEL_ID, chatTable.channelID, DBDefinition.MAIL_TYPE, type,
							DBDefinition.MAIL_CREATE_TIME, createTime);
				}
			}
			else
			{
				sql = String.format(Locale.US, "SELECT %s FROM %s WHERE %s='%s' AND %s<>%d AND %s<>%d AND %s<>%d AND %s<>%d AND %s <= %d " + descStr,
						selectObject, DBDefinition.TABEL_MAIL, DBDefinition.MAIL_CHANNEL_ID, chatTable.channelID, DBDefinition.MAIL_TYPE,
						MailManager.MAIL_RESOURCE, DBDefinition.MAIL_TYPE, MailManager.MAIL_RESOURCE_HELP, DBDefinition.MAIL_TYPE,
						MailManager.MAIL_ATTACKMONSTER,DBDefinition.MAIL_TYPE,
						MailManager.MAIL_MISSILE, DBDefinition.MAIL_TYPE,
						MailManager.MAIL_GIFT_BUY_EXCHANGE,DBDefinition.MAIL_CREATE_TIME, createTime);
			}

		}

		if (limitCount > 0)
			sql += " LIMIT " + limitCount;
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	private String getSqlByChannelId(String channelId)
	{
		String sql = "";
		List<Integer> typeArray = MailManager.getInstance().getChannelTypeArrayByChannel(channelId);
		if (typeArray == null || typeArray.size() <= 0)
			return "";
		String temp = "";

		String[] specailTitle = { "114111", "105726", "105727", "105728", "105729", "105730", "115429","87003105","90500285","77000157","77000158"};

		String allianceSpecialSql = "";
		String systemSpecialSql = "";
		String knightSpecialSql = DBDefinition.MAIL_CONTENTS + " LIKE '%\"battleType\":6%'";
		String fightSpecialSql = DBDefinition.MAIL_CONTENTS + " NOT LIKE '%\"battleType\":6%' AND " +DBDefinition.MAIL_CONTENTS
				+ " NOT LIKE '%\"battleType\":10%' AND "+ DBDefinition.MAIL_CONTENTS
				+ " NOT LIKE '%\"msReport\":1%' AND "+ DBDefinition.MAIL_CONTENTS
				+ " NOT LIKE '%\"battleMailType\":9%' AND "+ DBDefinition.MAIL_CONTENTS
				+ " NOT LIKE '%\"isBattlefieldServer\":1%'AND "+ DBDefinition.MAIL_CONTENTS
				+ " NOT LIKE '%BattlefieldServerFlag%'";
		String knightActivitySpecialSql = DBDefinition.MAIL_CONTENTS + " LIKE '%\"msReport\":1%'";

		String battlegameSpecialSql = DBDefinition.MAIL_CONTENTS + " LIKE '%\"battleMailType\":9%'";

		String arenagameSpecialSql = DBDefinition.MAIL_CONTENTS + " LIKE '%\"isBattlefieldServer\":1%' OR "+DBDefinition.MAIL_CONTENTS + " LIKE '%BattlefieldServerFlag%'";

		String shamoSpecialSql = DBDefinition.MAIL_CONTENTS + " LIKE '%\"battleType\":10%'";

		for (int i = 0; i < specailTitle.length; i++)
		{
			if (i > 0)
			{
				allianceSpecialSql += " OR ";
				systemSpecialSql += " AND ";
			}
			allianceSpecialSql += DBDefinition.MAIL_TITLE + " = " + specailTitle[i];
			systemSpecialSql += DBDefinition.MAIL_TITLE + " <> " + specailTitle[i];
		}
		if (StringUtils.isNotEmpty(systemSpecialSql))
			systemSpecialSql += (" AND " + DBDefinition.MAIL_TITLE + " <> '137460' AND " + DBDefinition.MAIL_TITLE + " <> '133270' AND " + DBDefinition.MAIL_TITLE + " <> '150335'");
		else
			systemSpecialSql += (DBDefinition.MAIL_TITLE + " <> '137460' AND " + DBDefinition.MAIL_TITLE + " <> '133270' AND " + DBDefinition.MAIL_TITLE + " <> '150335'");

		for (int i = 0; i < typeArray.size(); i++)
		{
			int type = typeArray.get(i).intValue();
			if (type > 0)
			{
				if (i > 0)
					temp += " OR ";
				if (channelId.equals(MailManager.CHANNELID_ALLIANCE) && type == MailManager.MAIL_SYSTEM
						&& StringUtils.isNotEmpty(allianceSpecialSql))
					temp += "(" + DBDefinition.MAIL_TYPE + " = " + type + " AND (" + allianceSpecialSql + "))";
				else if (channelId.equals(MailManager.CHANNELID_SYSTEM) && type == MailManager.MAIL_SYSTEM
						&& StringUtils.isNotEmpty(systemSpecialSql))
					temp += "(" + DBDefinition.MAIL_TYPE + " = " + type + " AND (" + systemSpecialSql + "))";
				else if (channelId.equals(MailManager.CHANNELID_FIGHT) && (type == MailManager.MAIL_BATTLE_REPORT||type == MailManager.MAIL_DETECT||type == MailManager.MAIL_DETECT_REPORT||type == MailManager.Mail_CASTLE_ACCOUNT)
						&& StringUtils.isNotEmpty(fightSpecialSql))
					temp += "(" + DBDefinition.MAIL_TYPE + " = " + type + " AND (" + fightSpecialSql + "))";
				else if (channelId.equals(MailManager.CHANNELID_KNIGHT) && type == MailManager.MAIL_BATTLE_REPORT
						&& StringUtils.isNotEmpty(knightSpecialSql))
					temp += "(" + DBDefinition.MAIL_TYPE + " = " + type + " AND (" + knightSpecialSql + "))";
				else if (channelId.equals(MailManager.CHANNELID_BATTLEGAME) && type == MailManager.MAIL_BATTLE_REPORT
						&& StringUtils.isNotEmpty(battlegameSpecialSql))
					temp += "(" + DBDefinition.MAIL_TYPE + " = " + type + " AND (" + battlegameSpecialSql + "))";
				else if (channelId.equals(MailManager.CHANNELID_EVENT) && type == MailManager.MAIL_BATTLE_REPORT
						&& StringUtils.isNotEmpty(knightActivitySpecialSql))
					temp += "(" + DBDefinition.MAIL_TYPE + " = " + type + " AND (" + knightActivitySpecialSql + "))";
				else if (channelId.equals(MailManager.CHANNELID_ARENAGAME) && (type == MailManager.MAIL_BATTLE_REPORT||type == MailManager.MAIL_DETECT||type == MailManager.MAIL_DETECT_REPORT)
						&& StringUtils.isNotEmpty(arenagameSpecialSql))
					temp += "(" + DBDefinition.MAIL_TYPE + " = " + type + " AND (" + arenagameSpecialSql + "))";
				else if (channelId.equals(MailManager.CHANNELID_SHAMOGAME) && type == MailManager.MAIL_BATTLE_REPORT
						&& StringUtils.isNotEmpty(shamoSpecialSql))
					temp += "(" + DBDefinition.MAIL_TYPE + " = " + type + " AND (" + shamoSpecialSql + "))";
				else
					temp += DBDefinition.MAIL_TYPE + " = " + type;
			}
		}

		if (channelId.equals(MailManager.CHANNELID_EVENT))
		{
			if (StringUtils.isNotEmpty(temp))
				temp += (" OR (" + DBDefinition.MAIL_TYPE + " = " + MailManager.MAIL_SYSTEM + " AND (" + DBDefinition.MAIL_TITLE
						+ " = '137460' OR " + DBDefinition.MAIL_TITLE + " = '133270' OR " + DBDefinition.MAIL_TITLE + " = '150335'))");
			else
				temp += (DBDefinition.MAIL_TYPE + " = " + MailManager.MAIL_SYSTEM + " AND (" + DBDefinition.MAIL_TITLE + " = '137460' OR "
						+ DBDefinition.MAIL_TITLE + " = '133270' OR " + DBDefinition.MAIL_TITLE + " = '150335'))");
		}

		if (StringUtils.isNotEmpty(temp))
		{
			temp += ")";
			sql = " WHERE (" + temp;
		}

		long time = TimeManager.getInstance().getCurrentTimeMS();
		sql += " AND ((FailTime <> 0 AND FailTime > "+time+") OR (FailTime = 0)) ";
		return sql;
	}

	private Cursor queryMailByCreateTime(String channelId, int createTime, int limitCount, boolean needCount)
	{
		if (!isDBAvailable())
			return null;
		if (StringUtils.isEmpty(channelId) || StringUtils.isEmpty(getSqlByChannelId(channelId)))
			return null;
		String selectObject = "*";
		String descStr = " ORDER BY " + DBDefinition.MAIL_CREATE_TIME + " DESC";
		if (needCount)
		{
			selectObject = "COUNT(*)";
			descStr = "";
		}
		String sql = "SELECT " + selectObject + " FROM " + DBDefinition.TABEL_MAIL + getSqlByChannelId(channelId);

		if (!needCount)
		{
			String sql2 = "SELECT COUNT(*) FROM " + DBDefinition.TABEL_MAIL + getSqlByChannelId(channelId) + " AND "
					+ DBDefinition.MAIL_CREATE_TIME + " = " + createTime;
			Cursor c2 = db.rawQuery(sql2, null);
			
			int count = 0;
			if (c2.moveToFirst())
				count = c2.getInt(0);
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "相同createTime的邮件数量", count);
			
			if (count >= 20)
				limitCount = count + 1;
		}

		if (createTime == -1)
		{
			sql += descStr;
		}
		else
		{
			sql += " AND " + DBDefinition.MAIL_CREATE_TIME + " <= " + createTime + descStr;
		}
		if (limitCount > 0)
			sql += " LIMIT " + limitCount;
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "queryMailByCreateTime sql:",sql);
		Cursor c = db.rawQuery(sql, null);
		return c;
	}
	
	private Cursor queryUnreadMailByCreateTime(String channelId, int createTime, int limitCount, boolean needCount)
	{
		if (!isDBAvailable())
			return null;
		if (StringUtils.isEmpty(channelId) || StringUtils.isEmpty(getSqlByChannelId(channelId)))
			return null;
		String selectObject = "*";
		String descStr = " ORDER BY " + DBDefinition.MAIL_CREATE_TIME + " DESC";
		if (needCount)
		{
			selectObject = "COUNT(*)";
			descStr = "";
		}
		String sql = "SELECT " + selectObject + " FROM " + DBDefinition.TABEL_MAIL + getSqlByChannelId(channelId) + " AND Status=0 ";

		if (!needCount)
		{
			String sql2 = "SELECT COUNT(*) FROM " + DBDefinition.TABEL_MAIL + getSqlByChannelId(channelId) + " AND "
					+ DBDefinition.MAIL_CREATE_TIME + " = " + createTime + " AND Status=0 ";
			Cursor c2 = db.rawQuery(sql2, null);
			
			int count = 0;
			if (c2.moveToFirst())
			{
				count = c2.getInt(0);
			}
			closeCursor(c2);
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "相同createTime的邮件数量", count);
			
			if (count >= 20)
				limitCount = count + 1;
		}

		if (createTime == -1)
		{
			sql += descStr;
		}
		else
		{
			sql += " AND " + DBDefinition.MAIL_CREATE_TIME + " <= " + createTime + descStr;
		}
		if (limitCount > 0)
			sql += " LIMIT " + limitCount;
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	private Cursor queryMail(String channelId, int configType)
	{
		if (!isDBAvailable())
			return null;
		if (StringUtils.isEmpty(channelId) || StringUtils.isEmpty(getSqlByChannelId(channelId)))
			return null;
		String sql = "SELECT * FROM " + DBDefinition.TABEL_MAIL + getSqlByChannelId(channelId);
		if (configType == CONFIG_TYPE_READ)
			sql += " AND " + DBDefinition.MAIL_STATUS + " = 0";
		else if (configType == CONFIG_TYPE_SAVE)
			sql += " AND " + DBDefinition.MAIL_SAVE_FLAG + " = 0";
		else if (configType == CONFIG_TYPE_REWARD)
			sql += " AND " + DBDefinition.MAIL_REWARD_STATUS + " = 0";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public void prepareChatTable(ChatTable chatTable)
	{
		if (StringUtils.isEmpty(chatTable.channelID) || !isDBAvailable())
			return;

		if (!isTableExists(chatTable.getTableName()))
		{
			if (chatTable.channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL)
				createChatTable(chatTable.getTableName());
			else
				createMailTable();

			// 邮件时不应该插入频道user，会与玩家user冲突
			if (chatTable.channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL)
			{
				UserInfo user = new UserInfo();
				user.uid = chatTable.getChannelName();
				insertUser(user);
			}

			if (getChannel(chatTable) == null)
			{
				ChatChannel channel = ChannelManager.getInstance().getChannel(chatTable);
				insertChannel(channel);
			}
		}
	}

	public void insertChannel(ChatChannel channel)
	{
		if (channel == null || !isDBAvailable())
			return;
		try
		{
			db.insert(DBDefinition.TABEL_CHANNEL, null, channel.getContentValues());
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public boolean isTableExists(String tableName)
	{
		if (StringUtils.isEmpty(tableName) || !isDBAvailable())
			return false;

		int count = 0;
		Cursor c = null;
		try
		{
			String sql = String.format(Locale.US, "SELECT COUNT(*) FROM %s WHERE type = '%s' AND name = '%s'",
					DBDefinition.TABLE_SQLITE_MASTER, "table", tableName);
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return false;
			}
			count = c.getInt(0);
		}
		catch (Exception e)
		{
			// LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
		return count > 0;
	}

	private void createChatTable(String tableName)
	{
		if (StringUtils.isEmpty(tableName) || !isDBAvailable())
			return;

		try
		{
			db.execSQL(DBDefinition.CREATE_TABLE_CHAT.replace(DBDefinition.CHAT_TABLE_NAME_PLACEHOLDER, tableName));
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	private void createMailTable()
	{
		if (!isDBAvailable())
			return;

		try
		{
			db.execSQL(DBDefinition.CREATE_TABEL_MAIL);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public static String sqliteEscape(String keyWord)
	{
		keyWord = keyWord.replace("/", "//");
		keyWord = keyWord.replace("'", "''");
		keyWord = keyWord.replace("[", "/[");
		keyWord = keyWord.replace("]", "/]");
		keyWord = keyWord.replace("%", "/%");
		keyWord = keyWord.replace("&", "/&");
		keyWord = keyWord.replace("_", "/_");
		keyWord = keyWord.replace("(", "/(");
		keyWord = keyWord.replace(")", "/)");
		return keyWord;
	}

	private void addDetectInMap(Map<String, DetectMailInfo> detectMap, String name, String mailUid, int createTime)
	{
		if (detectMap.containsKey(name))
		{
			DetectMailInfo mailInfo = detectMap.get(name);
			if (mailInfo != null && mailInfo.getCreateTime() < createTime)
			{
				DetectMailInfo detectInfo = new DetectMailInfo();
				detectInfo.setName(name);
				detectInfo.setMailUid(mailUid);
				detectInfo.setCreateTime(createTime);
				detectMap.remove(name);
				detectMap.put(name, detectInfo);
			}
		}
		else
		{
			DetectMailInfo detectInfo = new DetectMailInfo();
			detectInfo.setName(name);
			detectInfo.setMailUid(mailUid);
			detectInfo.setCreateTime(createTime);
			detectMap.put(name, detectInfo);
		}
	}

	public void getDetectMailInfo()
	{
		// if (!ChatServiceController.isDetectInfoEnable)
		// 	return;
		String jsonStr = "";
		if (!isDBAvailable())
			return;
		Cursor c = null;
		try
		{
			String sql = "SELECT * FROM " + DBDefinition.TABEL_MAIL + " WHERE " + DBDefinition.MAIL_TYPE + " = "
					+ MailManager.MAIL_DETECT_REPORT;
			if (ChatServiceController.scoutmail)
			{
				 sql = "SELECT * FROM " + DBDefinition.TABEL_MAIL + " WHERE " + DBDefinition.MAIL_TYPE + " = "
						+ MailManager.MAIL_DETECT_REPORT +" OR "+ DBDefinition.MAIL_TYPE + " = " + MailManager.Mail_NEW_SCOUT_REPORT_FB;
			}
			c = db.rawQuery(sql, null);
			List<DetectMailInfo> changedDetectMailArr = new ArrayList<DetectMailInfo>();
			List<DetectMailInfo> deleteDetectMailArr = new ArrayList<DetectMailInfo>();
			Map<String, DetectMailInfo> detectMap = new HashMap<String, DetectMailInfo>();
			while (c != null && c.moveToNext())
			{
				MailData mail = new MailData(c);
				if (mail != null && StringUtils.isNotEmpty(mail.getContents()))
				{
					if (mail.getContents().contains("user"))
					{
						try
						{
							DetectReportMailContents detail = JSON.parseObject(mail.getContents(), DetectReportMailContents.class);
							if (detail != null && detail.getUser() != null && StringUtils.isNotEmpty(detail.getUser().getName())
									&& detail.getPointType() == MailManager.CityTile)
							{
								String name = detail.getUser().getName();
								int creatTime = mail.getCreateTime();
								addDetectInMap(detectMap, name, mail.getUid(), creatTime);
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else if (mail.getContents().contains("userInfo"))
					{
						try
						{
							FBDetectReportMailContents detail = JSON.parseObject(mail.getContents(), FBDetectReportMailContents.class);
							if (detail != null && detail.getUserInfo() != null && StringUtils.isNotEmpty(detail.getUserInfo().getName())
									&& detail.getPointType() == MailManager.CityTile)
							{
								String name = detail.getUserInfo().getName();
								int creatTime = mail.getCreateTime();
								addDetectInMap(detectMap, name, mail.getUid(), creatTime);
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else if (StringUtils.isNotEmpty(mail.getFromUid()))
					{
						String name = mail.getFromUid();
						int creatTime = mail.getCreateTime();
						addDetectInMap(detectMap, name, mail.getUid(), creatTime);
					}

				}
			}

			boolean hasOldMap = false;
			if (mDetectInfoMap == null)
				mDetectInfoMap = new HashMap<String, String>();
			if (mDetectInfoMap != null && mDetectInfoMap.size() > 0)
				hasOldMap = true;

			if (detectMap != null && detectMap.size() > 0)
			{
				Set<String> keySet = detectMap.keySet();
				if (keySet != null)
				{
					for (String key : keySet)
					{
						if (StringUtils.isNotEmpty(key))
						{
							DetectMailInfo detectInfo = detectMap.get(key);
							if (detectInfo != null)
							{
								if (hasOldMap
										&& ((mDetectInfoMap.containsKey(key) && StringUtils.isNotEmpty(mDetectInfoMap.get(key)) && !mDetectInfoMap
												.get(key).equals(detectInfo.getMailUid())) || !mDetectInfoMap.containsKey(key)))
									changedDetectMailArr.add(detectMap.get(key));
								else if (!hasOldMap)
								{
									mDetectInfoMap.put(key, detectInfo.getMailUid());
									changedDetectMailArr.add(detectMap.get(key));
								}
							}

						}
					}

					if (changedDetectMailArr != null && changedDetectMailArr.size() > 0)
					{
						jsonStr = JSON.toJSONString(changedDetectMailArr);
						if (StringUtils.isNotEmpty(jsonStr))
						{
							if (hasOldMap)
							{
								JniController.getInstance().excuteJNIVoidMethod("postChangedDetectMailInfo", new Object[] { jsonStr });
							}
							else
							{
								JniController.getInstance().excuteJNIVoidMethod("postDetectMailInfo", new Object[] { jsonStr });
							}
						}
					}

					if (hasOldMap)
					{
						Set<String> oldKeySet = mDetectInfoMap.keySet();
						for (String oldKey : oldKeySet)
						{
							if (StringUtils.isNotEmpty(oldKey) && !detectMap.containsKey(oldKey))
							{
								DetectMailInfo detectInfo = new DetectMailInfo();
								detectInfo.setName(oldKey);
								deleteDetectMailArr.add(detectInfo);
							}
						}

						if (deleteDetectMailArr != null && deleteDetectMailArr.size() > 0)
						{
							jsonStr = JSON.toJSONString(deleteDetectMailArr);
							if (StringUtils.isNotEmpty(jsonStr))
							{
								JniController.getInstance().excuteJNIVoidMethod("postDeletedDetectMailInfo", new Object[] { jsonStr });
							}
						}

						mDetectInfoMap.clear();
						for (String key : keySet)
						{
							if (StringUtils.isNotEmpty(key))
							{
								DetectMailInfo detectInfo = detectMap.get(key);
								if (detectInfo != null)
								{
									mDetectInfoMap.put(key, detectInfo.getMailUid());
								}

							}
						}
					}

				}

			}
			else if (hasOldMap)
			{
				Set<String> oldKeySet = mDetectInfoMap.keySet();
				for (String oldKey : oldKeySet)
				{
					if (StringUtils.isNotEmpty(oldKey))
					{
						DetectMailInfo detectInfo = new DetectMailInfo();
						detectInfo.setName(oldKey);
						deleteDetectMailArr.add(detectInfo);
					}
				}

				if (deleteDetectMailArr != null && deleteDetectMailArr.size() > 0)
				{
					jsonStr = JSON.toJSONString(deleteDetectMailArr);
					if (StringUtils.isNotEmpty(jsonStr))
					{
						JniController.getInstance().excuteJNIVoidMethod("postDeletedDetectMailInfo", new Object[] { jsonStr });
					}
				}

				mDetectInfoMap.clear();
			}
			// LogUtil.printVariablesWithFuctionName("jsonStr",jsonStr);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}
	}

	public List<MsgItem> getUnHandleRedPackage(ChatTable chatTable)
	{
		List<MsgItem> array = new ArrayList<MsgItem>();
		if (!isDBAvailable())
			return array;

		Cursor c = null;
		try
		{
			String sql = "SELECT * FROM " + chatTable.getTableNameAndCreate() + " WHERE " + DBDefinition.CHAT_COLUMN_TYPE + " = 12"
					+ getDoNotIncludeWSMsgSQL(chatTable.channelType, false) + " AND (" + DBDefinition.CHAT_COLUMN_STATUS + " = "
					+ MsgItem.UNHANDLE + " OR " + DBDefinition.CHAT_COLUMN_STATUS + " < 0)";
			c = db.rawQuery(sql, null);

			while (c != null && c.moveToNext())
			{
				MsgItem item = new MsgItem(c);
				array.add(item);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return array;
	}
	
	private void closeCursor(Cursor c)
	{
		if (c != null)
		{
			try
			{
				c.close();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	public int getSaveMailCountInDB()
	{
		if (!isDBAvailable())
			return 0;

		Cursor c = null;
		try
		{
			String sql = "SELECT count(*) FROM " + DBDefinition.TABEL_MAIL + " WHERE " + DBDefinition.MAIL_SAVE_FLAG + " = 1";
			c = db.rawQuery(sql, null);
			if (!c.moveToFirst())
			{
				return 0;
			}
			return c.getInt(0);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (c != null)
				c.close();
		}

		return 0;
	}
}
