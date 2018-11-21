package com.chatsdk.model.db;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.model.UserManager;
import com.chatsdk.util.LogUtil;

import org.apache.commons.lang.StringUtils;

import java.io.File;

public class DBHelper extends SQLiteOpenHelper
{
	private static final String	DATABASE_NAME							= "chat_service.db";
	private static final int	VERSION_BASIS							= 1;
	private static final int	VERSION_ADD_CROSS_FIGHT_SRC_SERVER_ID	= 2;						// 6.11提交内网，外网尚未发布过
	private static final int	VERSION_ADD_MAIL_TABLE					= 3;
	private static final int	VERSION_ADD_TITLE_AND_SUMMARY			= 4;
	private static final int	VERSION_ADD_PARSE_VERSION				= 5;
	private static final int	VERSION_ADD_REWARD_LEVEL				= 6;
	private static final int	VERSION_ADD_USER_LANG					= 7;
	private static final int	VERSION_ADD_USER_SVIP					= 8;
	private static final int	VERSION_ADD_USER_MONTHCARD				= 9;
	private static final int	VERSION_ADD_MAIL_FAILTIME				= 10;
	private static final int	VERSION_UPDATE_USER_SVIP				= 12;


	private static final int	VERSION_UPDATE_CHANNEL_FIGHT_TO_BATTLEGAME				= 13;       //战争报告Channel中提出战争游戏相关的Channel 更新数据库channelId
	private static final int	VERSION_UPDATE_USER_CAREERNAME				= 14;       //战争报告Channel中提出战争游戏相关的Channel 更新数据库channelId
	private static final int	VERSION_UPDATE_MAIL_REPLYTEXT				= 15;       //
	private static final int	VERSION_UPDATE_USER_VIPFRAME				= 16;       //


	public static final int		CURRENT_DATABASE_VERSION				= VERSION_UPDATE_USER_VIPFRAME;

	public DBHelper(Context context)
	{
		// CursorFactory设置为null,使用默认值
		super(context, getDBFilePath(context), null, CURRENT_DATABASE_VERSION);
	}

	private static String getDBFilePath(Context context)
	{
		String result = getDBDirectoryPath(context, false) + getDBFileName(context);
		
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "directory", result);
		return result;
	}

	public static String getDBFileAbsolutePath(Context context)
	{
		String result = getDBDirectoryPath(context, true) + getDBFileName(context);
		
		return result;
	}
	
	private static String getDBFileName(Context context)
	{
		if(isSDCardWritable(context)){
			return DATABASE_NAME;
		}else{
			String user = StringUtils.isEmpty(UserManager.getInstance().getCurrentUserId()) ? "unknownUser" : UserManager.getInstance()
					.getCurrentUserId();
			return user + ".db";
		}
	}

	public static String getDBDirectoryPath(Context context, boolean returnAbsolutePath)
	{
		if (isSDCardWritable(context))
		{
			String user = StringUtils.isEmpty(UserManager.getInstance().getCurrentUserId()) ? "unknownUser" : UserManager.getInstance()
					.getCurrentUserId();
			// SD卡应用目录（卸载会删除）：context.getExternalFilesDir(null) + File.separator
			// + user + "/database/"
			String directory = Environment.getExternalStorageDirectory() + "/data/data/" + context.getPackageName() + File.separator + user
					+ "/database/";
			if (prepareDirectory(directory))
			{
				return directory;
			}
		}
		
		if(returnAbsolutePath){
			// 用于复制预设db到内存、删除db（不通过DBHelper，无法使用预设目录）
			File files = context.getCacheDir();
			String directory = files.getParentFile().getAbsolutePath() + "/databases/";
			prepareDirectory(directory);
			return directory;
		}else{
			return "";
		}
	}
	
	public static boolean isSDCardWritable(Context context)
	{
		return isSDcardAvaiable() && isExternalStoragePermissionsAvaiable(context);
	}

	public static String getHeadDirectoryPath(Context context)
	{
		if (isSDCardWritable(context))
		{
			String directory = Environment.getExternalStorageDirectory() + "/data/data/" + context.getPackageName() + "/head/";
			if (prepareDirectory(directory))
			{
				return directory;
			}
		}
		File files = context.getCacheDir();
		String directory = files.getAbsolutePath() + "/head/";
		prepareDirectory(directory);
		return directory;
	}

	/**
	 * S4上，有user时，要手动创建目录才行
	 */
	private static boolean prepareDirectory(String path)
	{
		File file = new File(path);
		if (file.exists() && file.isDirectory())
		{
			return true;
		}
		else
		{
			boolean result = file.mkdirs();
			return result;
		}
	}

	/**
	 * db在内存中时，getDBFilePath()和getDatabaseName()都只得到"chat_service.db"，这个文件找不到，需要加上包内的路径
	 * SQLiteDatabaseConfiguration.MEMORY_DB_PATH（":memory:"）不行
	 */
	public boolean rmDirectory()
	{
		String dataBaseName = getDBFileAbsolutePath(ChatServiceController.hostActivity);
		if (StringUtils.isNotEmpty(dataBaseName))
		{
			File file = new File(dataBaseName);
			if (file.exists())
			{
				return file.delete();
			}
			else
			{
				return false;
			}
		}
		return false;
	}

	/**
	 * 数据库第一次被创建时onCreate会被调用
	 */
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		createBasicTable(db, DBDefinition.TABEL_USER, DBDefinition.CREATE_TABEL_USER);
		createBasicTable(db, DBDefinition.TABEL_CHANNEL, DBDefinition.CREATE_TABEL_CHANNEL);
		createBasicTable(db, DBDefinition.TABEL_MAIL, DBDefinition.CREATE_TABEL_MAIL);
	}

	private void createBasicTable(SQLiteDatabase db, String tableName, String tableDef)
	{
		try
		{
			if (!isTableExists(db, tableName))
			{
				db.execSQL(tableDef);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE,"oldVersion",oldVersion,"newVersion",newVersion);
		db.beginTransaction();
		try
		{
			switch (oldVersion)
			{
				case VERSION_BASIS:
					if (!existsColumnInTable(db, DBDefinition.TABEL_USER, DBDefinition.USER_CROSS_FIGHT_SRC_SERVER_ID))
					{
						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_USER + " ADD " + DBDefinition.USER_CROSS_FIGHT_SRC_SERVER_ID
								+ " INTEGER DEFAULT -2");
					}
					upgradeTableVersion(db, DBDefinition.TABEL_USER, VERSION_ADD_CROSS_FIGHT_SRC_SERVER_ID);
				case VERSION_ADD_CROSS_FIGHT_SRC_SERVER_ID:
					// 扩展channel表
					if (!existsColumnInTable(db, DBDefinition.TABEL_CHANNEL, DBDefinition.CHANNEL_UNREAD_COUNT))
					{
						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_CHANNEL + " ADD " + DBDefinition.CHANNEL_UNREAD_COUNT
								+ " INTEGER DEFAULT 0");
						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_CHANNEL + " ADD " + DBDefinition.CHANNEL_LATEST_ID + " TEXT");
						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_CHANNEL + " ADD " + DBDefinition.CHANNEL_LATEST_TIME
								+ " INTEGER DEFAULT -1");
						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_CHANNEL + " ADD " + DBDefinition.CHANNEL_LATEST_MODIFY_TIME
								+ " INTEGER DEFAULT -1");
						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_CHANNEL + " ADD " + DBDefinition.CHANNEL_SETTINGS + " TEXT");
					}
					upgradeTableVersion(db, DBDefinition.TABEL_CHANNEL, VERSION_ADD_MAIL_TABLE);

					// 新增mail表
					createBasicTable(db, DBDefinition.TABEL_MAIL, DBDefinition.CREATE_TABEL_MAIL);

					// 重建chat表
					recreateAllChatTables(db);
				case VERSION_ADD_MAIL_TABLE:
					// 扩展mail表
					if (!existsColumnInTable(db, DBDefinition.TABEL_MAIL, DBDefinition.MAIL_TITLE_TEXT))
					{
						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_MAIL + " ADD " + DBDefinition.MAIL_TITLE_TEXT + " TEXT");
						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_MAIL + " ADD " + DBDefinition.MAIL_SUMMARY + " TEXT");
						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_MAIL + " ADD " + DBDefinition.MAIL_LANGUAGE + " TEXT");
					}
					upgradeTableVersion(db, DBDefinition.TABEL_MAIL, VERSION_ADD_TITLE_AND_SUMMARY);
				case VERSION_ADD_TITLE_AND_SUMMARY:
					// 扩展mail表
					if (!existsColumnInTable(db, DBDefinition.TABEL_MAIL, DBDefinition.PARSE_VERSION))
					{
						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_MAIL + " ADD " + DBDefinition.PARSE_VERSION + " INTEGER DEFAULT -1");
					}
					upgradeTableVersion(db, DBDefinition.TABEL_MAIL, VERSION_ADD_PARSE_VERSION);
				case VERSION_ADD_PARSE_VERSION:
					upgradeTableVersion(db, DBDefinition.TABEL_MAIL, VERSION_ADD_REWARD_LEVEL);
				case VERSION_ADD_REWARD_LEVEL:
					if (!existsColumnInTable(db, DBDefinition.TABEL_MAIL, DBDefinition.MAIL_REWARD_LEVEL))
					{
						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_MAIL + " ADD " + DBDefinition.MAIL_REWARD_LEVEL
								+ " INTEGER DEFAULT 0");
					}
					upgradeTableVersion(db, DBDefinition.TABEL_MAIL, VERSION_ADD_REWARD_LEVEL);
				case VERSION_ADD_USER_LANG:
					if (!existsColumnInTable(db, DBDefinition.TABEL_USER, DBDefinition.USER_COLUMN_LANG))
					{
						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_USER + " ADD " + DBDefinition.USER_COLUMN_LANG
								+ " TEXT");
					}
					upgradeTableVersion(db, DBDefinition.TABEL_USER, VERSION_ADD_USER_LANG);
				case VERSION_ADD_USER_SVIP:
					if (!existsColumnInTable(db, DBDefinition.TABEL_USER, DBDefinition.USER_COLUMN_SVIP_LEVEL))
					{
						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_USER + " ADD " + DBDefinition.USER_COLUMN_SVIP_LEVEL
								+ " INTEGER DEFAULT -1");
					}
					upgradeTableVersion(db, DBDefinition.TABEL_USER, VERSION_ADD_USER_SVIP);
				case VERSION_ADD_USER_MONTHCARD:
					if (!existsColumnInTable(db, DBDefinition.TABEL_USER, DBDefinition.USER_COLUMN_MONTHCARD))
					{
						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_USER + " ADD " + DBDefinition.USER_COLUMN_MONTHCARD
								+ " INTEGER DEFAULT 0");
					}
					upgradeTableVersion(db, DBDefinition.TABEL_USER, VERSION_ADD_USER_MONTHCARD);
				case VERSION_ADD_MAIL_FAILTIME:
					// 扩展mail表
					if (!existsColumnInTable(db, DBDefinition.TABEL_MAIL, DBDefinition.MAIL_FAIL_TIME))
					{
						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_MAIL + " ADD " + DBDefinition.MAIL_FAIL_TIME
								+ " INTEGER DEFAULT 0");
					}
					upgradeTableVersion(db, DBDefinition.TABEL_MAIL, VERSION_ADD_MAIL_FAILTIME);
				case VERSION_UPDATE_USER_SVIP:
					// 更新svip默认值
					if (!existsColumnInTable(db, DBDefinition.TABEL_USER, DBDefinition.USER_COLUMN_SVIP_LEVEL))
					{
						db.execSQL("UPDATE " + DBDefinition.TABEL_USER + " SET " + DBDefinition.USER_COLUMN_SVIP_LEVEL + " = -1");
						upgradeTableVersion(db, DBDefinition.TABEL_USER, VERSION_UPDATE_USER_SVIP);
					}
//				case VERSION_UPDATE_CHANNEL_FIGHT_TO_BATTLEGAME:
//				{
//					// 战争报告Channel中提出战争游戏相关的Channel 更新数据库channelId
//					if (existsColumnInTable(db, DBDefinition.TABEL_MAIL, DBDefinition.MAIL_CHANNEL_ID))
//					{
//						db.execSQL("UPDATE " + DBDefinition.TABEL_MAIL + " SET " + DBDefinition.MAIL_CHANNEL_ID + " ='battle_game'"+" WHERE "+DBDefinition.MAIL_CHANNEL_ID+" = 'fight' AND"+DBDefinition.MAIL_CONTENTS + " LIKE '%\"battleMailType\":9%'");
//
//						upgradeTableVersion(db, DBDefinition.TABEL_MAIL, VERSION_UPDATE_CHANNEL_FIGHT_TO_BATTLEGAME);
//					}
//				}
				case VERSION_UPDATE_CHANNEL_FIGHT_TO_BATTLEGAME:
				{
					// 战争报告Channel中提出战争游戏相关的Channel 更新数据库channelId
					if (!existsColumnInTable(db, DBDefinition.TABEL_USER, DBDefinition.USER_COLUMN_CAREER_NAME))
					{
						db.execSQL("UPDATE " + DBDefinition.TABEL_USER + " SET " + DBDefinition.USER_COLUMN_CAREER_NAME + " = -1");
						upgradeTableVersion(db, DBDefinition.TABEL_USER, VERSION_UPDATE_USER_CAREERNAME);
					}
				}
				case VERSION_UPDATE_USER_CAREERNAME:
				{

					if (!existsColumnInTable(db, DBDefinition.TABEL_MAIL, DBDefinition.MAIL_REPLY_TEXT))
					{

						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_MAIL + " ADD " + DBDefinition.MAIL_REPLY_TEXT
								+ " TEXT");
						upgradeTableVersion(db, DBDefinition.TABEL_MAIL, VERSION_UPDATE_MAIL_REPLYTEXT);
					}
				}
				case VERSION_UPDATE_MAIL_REPLYTEXT:
				{
					if (!existsColumnInTable(db, DBDefinition.TABEL_USER, DBDefinition.USER_COLUMN_VIP_FRAME)) {

						db.execSQL("ALTER TABLE " + DBDefinition.TABEL_USER + " ADD " + DBDefinition.USER_COLUMN_VIP_FRAME
								+ " TEXT");
						upgradeTableVersion(db, DBDefinition.TABEL_USER, VERSION_UPDATE_USER_VIPFRAME);
					}
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
			db.endTransaction();
		}
	}

	/**
	 * 修改表的版本号
	 */
	public void upgradeTableVersion(SQLiteDatabase db, String tableName, int newVersion)
	{
		ContentValues cv = new ContentValues();
		cv.put(DBDefinition.COLUMN_TABLE_VER, newVersion);
		db.update(tableName, cv, null, null);
	}

	private void recreateAllChatTables(SQLiteDatabase db)
	{
		String sql = String.format("SELECT * FROM %s WHERE type = '%s' AND name LIKE '%s%%'", DBDefinition.TABLE_SQLITE_MASTER, "table",
				DBDefinition.TABEL_CHAT);
		Cursor cursor = db.rawQuery(sql, null);
		while (cursor != null && cursor.moveToNext())
		{
			String tableName = cursor.getString(cursor.getColumnIndex("name"));
			recreateChatTable(db, tableName);
		}

		if (cursor != null)
		{
			cursor.close();
		}
	}

	private void recreateChatTable(SQLiteDatabase db, String tableName)
	{
		String columns = DBDefinition.CHAT_COLUMN_SEQUENCE_ID + "," + DBDefinition.CHAT_COLUMN_USER_ID + ","
				+ DBDefinition.CHAT_COLUMN_CHANNEL_TYPE + "," + DBDefinition.CHAT_COLUMN_CREATE_TIME + ","
				+ DBDefinition.CHAT_COLUMN_LOCAL_SEND_TIME + "," + DBDefinition.CHAT_COLUMN_TYPE + "," + DBDefinition.CHAT_COLUMN_MSG + ","
				+ DBDefinition.CHAT_COLUMN_TRANSLATION + "," + DBDefinition.CHAT_COLUMN_ORIGINAL_LANGUAGE + ","
				+ DBDefinition.CHAT_COLUMN_TRANSLATED_LANGUAGE + "," + DBDefinition.CHAT_COLUMN_STATUS + ","
				+ DBDefinition.CHAT_COLUMN_ATTACHMENT_ID + "," + DBDefinition.CHAT_COLUMN_MEDIA;

		db.execSQL("ALTER TABLE " + tableName + " RENAME TO TempOldTable");
		db.execSQL(DBDefinition.CREATE_TABLE_CHAT.replace(DBDefinition.CHAT_TABLE_NAME_PLACEHOLDER, tableName));
		db.execSQL("INSERT INTO " + tableName + "(" + columns + ") SELECT " + columns + " FROM TempOldTable");
		db.execSQL("DROP TABLE TempOldTable");
	}

	public boolean isTableExists(SQLiteDatabase db, String tableName)
	{
		if (StringUtils.isEmpty(tableName) || db == null || !db.isOpen())
		{
			return false;
		}

		int count = 0;
		Cursor cursor = null;
		try
		{
			cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBDefinition.TABLE_SQLITE_MASTER + " WHERE type = ? AND name = ?", new String[] {
					"table",
					tableName });
			if (!cursor.moveToFirst())
			{
				return false;
			}
			count = cursor.getInt(0);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		finally
		{
			if (cursor != null)
				cursor.close();
		}
		return count > 0;
	}

	private boolean existsColumnInTable(SQLiteDatabase db, String tableName, String columnToCheck)
	{
		if (StringUtils.isEmpty(tableName) || StringUtils.isEmpty(columnToCheck) || db == null || !db.isOpen())
		{
			return false;
		}
		boolean result = false;
		Cursor c = null;
		try
		{
			c = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0", null);

			if (c != null && c.getColumnIndex(columnToCheck) != -1)
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

	public static boolean isSDcardAvaiable()
	{
		String sdCardState = Environment.getExternalStorageState();
		return sdCardState.equals(Environment.MEDIA_MOUNTED);
	}

	public static boolean isExternalStoragePermissionsAvaiable(Context context) {
		if ((android.os.Build.VERSION.SDK_INT >= 23)) {
			int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
			return permission == PackageManager.PERMISSION_GRANTED;
		} else {
			try {
				String sdCardState = android.os.Environment.getExternalStorageState();
				return android.os.Environment.MEDIA_MOUNTED.equals(sdCardState);
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	public static String getLocalDirectoryPath(Context context,String directoryName)
	{
		if (isSDCardWritable(context))
		{
			String directory = Environment.getExternalStorageDirectory() + "/data/data/" + context.getPackageName() + "/"+directoryName+"/";
			if (prepareDirectory(directory))
			{
				return directory;
			}
		}
		File files = context.getCacheDir();
		String directory = files.getAbsolutePath() + "/"+directoryName+"/";
		prepareDirectory(directory);
		return directory;
	}
	
	public static String getLocalDirectoryPathWithOutSDCard(Context context,String directoryName)
	{
		File files = context.getFilesDir();
		String directory = files.getAbsolutePath() + "/"+directoryName+"/";
		prepareDirectory(directory);
		return directory;
	}
}
