package com.chatsdk.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.MenuController;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.db.DBManager;
import com.chatsdk.net.XiaoMiToolManager;

public class PermissionManager
{
	private static final int	MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE	= 1;
	private static final int	MY_PERMISSIONS_REQUEST_XM_RECORD		= 2;
	private static final int	MY_PERMISSIONS_REQUEST_XM_VIDEO			= 3;
	private static final int	MY_PERMISSIONS_REQUEST_ALLIANCE_SHARE	= 4;

	private static String[]		PERMISSIONS_STORAGE						= { Manifest.permission.WRITE_EXTERNAL_STORAGE };
	private static String[]		PERMISSIONS_XM_RECORD					= {
			Manifest.permission.RECORD_AUDIO,
			Manifest.permission.READ_PHONE_STATE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE					};
	private static String[]		PERMISSIONS_XM_VIDEO					= {
			Manifest.permission.CAMERA,
			Manifest.permission.RECORD_AUDIO,
			Manifest.permission.READ_PHONE_STATE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE					};
	private static String[]		PERMISSIONS_ALLIANCE_SHARE				= {
			Manifest.permission.CAMERA,
			Manifest.permission.WRITE_EXTERNAL_STORAGE					};

	public static boolean isExternalStoragePermissionsAvaiable(Context context)
	{
		return ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
	}

	public static void getExternalStoragePermission()
	{
		if (ActivityCompat.shouldShowRequestPermissionRationale(ChatServiceController.hostActivity,
				Manifest.permission.WRITE_EXTERNAL_STORAGE))
		{
			String notify = LanguageManager.getLangByKey(LanguageKeys.PERMISSION_REQUEST_WRITE_SD_CARD);
			MenuController.showAllowPermissionConfirm(ChatServiceController.hostActivity, notify,
					LanguageKeys.PERMISSION_REQUEST_WRITE_SD_CARD);
		}
		else
		{
			actualGetStoragePermissions();
		}
	}

	private static void actualGetStoragePermissions()
	{
		ActivityCompat.requestPermissions(ChatServiceController.hostActivity, PERMISSIONS_STORAGE, MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
	}

	public static boolean checkXMRecordPermission()
	{
		if (!isXMRecordPermissionsAvaiable(ChatServiceController.getCurrentActivity()))
		{
			if (shouldShowXMRecordRequestPermissionRationale(ChatServiceController.getCurrentActivity()))
			{
				String notify = LanguageManager.getLangByKey(LanguageKeys.PERMISSION_REQUEST_RECORD_VOICE);
				MenuController.showAllowPermissionConfirm(ChatServiceController.getCurrentActivity(), notify,
						LanguageKeys.PERMISSION_REQUEST_RECORD_VOICE);
			}
			else
			{
				actualGetXMRecordPermissions();
			}

			return false;
		}

		return true;
	}

	private static boolean shouldShowXMRecordRequestPermissionRationale(Activity activity)
	{
		for (String permission : PERMISSIONS_XM_RECORD)
		{
			if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean isXMRecordPermissionsAvaiable(Context context)
	{
		for (String permission : PERMISSIONS_XM_RECORD)
		{
			if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
			{
				return false;
			}
		}
		return true;
	}

	private static void actualGetXMRecordPermissions()
	{
		ActivityCompat.requestPermissions(ChatServiceController.getCurrentActivity(), PERMISSIONS_XM_RECORD,
				MY_PERMISSIONS_REQUEST_XM_RECORD);
	}

	public static void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		switch (requestCode)
		{
			case MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE:
			{
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					// If request is cancelled, the result arrays are empty.
					DBManager.getInstance().onRequestPermissionsResult();
				}
				else
				{
					// permission denied, disable the functionality that depends
					// on this permission.
					DBManager.getInstance().onRequestPermissionsResult();
				}
				return;
			}
			case MY_PERMISSIONS_REQUEST_XM_RECORD:
			{
				return;
			}
		}
	}

	public static void onNotifyPermissionConfirm(String permissionKey)
	{
		if (permissionKey.equals(LanguageKeys.PERMISSION_REQUEST_WRITE_SD_CARD))
		{
			actualGetStoragePermissions();
		}
		else if (permissionKey.equals(LanguageKeys.PERMISSION_REQUEST_RECORD_VOICE))
		{
			actualGetXMRecordPermissions();
		}
		else if (permissionKey.equals(LanguageKeys.PERMISSION_REQUEST_RECORD_VIDEO))
		{
//			if (XiaoMiToolManager.getInstance().getCurrentRecordActivity() != null)
//			{
//				actualGetXMVideoPermissions(XiaoMiToolManager.getInstance().getCurrentRecordActivity());
//			}
		}
	}

	public static boolean checkXMVideoPermission()
	{
//		if (XiaoMiToolManager.getInstance().getCurrentRecordActivity() == null)
//		{
//			return false;
//		}
//		if (!isXMVideoPermissionsAvaiable(XiaoMiToolManager.getInstance().getCurrentRecordActivity()))
//		{
//			if (shouldShowXMVideoRequestPermissionRationale(XiaoMiToolManager.getInstance().getCurrentRecordActivity()))
//			{
//				String notify = LanguageManager.getLangByKey(LanguageKeys.PERMISSION_REQUEST_RECORD_VIDEO);
//				MenuController.showAllowPermissionConfirm(XiaoMiToolManager.getInstance().getCurrentRecordActivity(), notify,
//						LanguageKeys.PERMISSION_REQUEST_RECORD_VIDEO);
//			}
//			else
//			{
//				actualGetXMVideoPermissions(XiaoMiToolManager.getInstance().getCurrentRecordActivity());
//			}
//
//			return false;
//		}

		return true;
	}

	private static boolean isXMVideoPermissionsAvaiable(Context context)
	{
		if (context != null)
		{
			for (String permission : PERMISSIONS_XM_VIDEO)
			{
				if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
				{
					return false;
				}
			}
		}
		return true;
	}

	private static boolean shouldShowXMVideoRequestPermissionRationale(Activity activity)
	{
		if (activity != null)
		{
			for (String permission : PERMISSIONS_XM_VIDEO)
			{
				if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
				{
					return true;
				}
			}
		}
		return false;
	}

	private static void actualGetXMVideoPermissions(Activity activity)
	{
		if (activity != null)
		{
			ActivityCompat.requestPermissions(activity, PERMISSIONS_XM_VIDEO, MY_PERMISSIONS_REQUEST_XM_VIDEO);
		}
	}

	public static boolean checkVideoPermissions()
	{
		boolean flag = false;
//		if (XiaoMiToolManager.getInstance().getCurrentRecordActivity() != null)
//		{
//			flag = isXMVideoPermissionsAvaiable(XiaoMiToolManager.getInstance().getCurrentRecordActivity());
//		}
		return flag;
	}
	
	private static boolean isAllianceSharePermissionsAvaiable(Context context)
	{
		if (context != null)
		{
			for (String permission : PERMISSIONS_ALLIANCE_SHARE)
			{
				if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
				{
					return false;
				}
			}
		}
		return true;
	}

	private static boolean shouldShowAllianceShareRequestPermissionRationale(Activity activity)
	{
		if (activity != null)
		{
			for (String permission : PERMISSIONS_ALLIANCE_SHARE)
			{
				if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
				{
					return true;
				}
			}
		}
		return false;
	}

	private static void actualGetAllianceSharePermissions(Activity activity)
	{
		if (activity != null)
		{
			ActivityCompat.requestPermissions(activity, PERMISSIONS_ALLIANCE_SHARE, MY_PERMISSIONS_REQUEST_ALLIANCE_SHARE);
		}
	}

	public static boolean checkAllianceSharePermissions()
	{
		if (!isAllianceSharePermissionsAvaiable(ChatServiceController.getCurrentActivity()))
		{
//			if (shouldShowAllianceShareRequestPermissionRationale(ChatServiceController.getCurrentActivity()))
//			{
//				MenuController.showAllowPermissionConfirm(ChatServiceController.getCurrentActivity(), notify,
//						LanguageKeys.PERMISSION_REQUEST_RECORD_VOICE);
//			}
//			else
//			{
			actualGetAllianceSharePermissions(ChatServiceController.getCurrentActivity());
//			}

			return false;
		}

		return true;
	}
	
	public static boolean isWifiStatePermissionsAvaiable(Context context)
	{
		if (context != null)
		{
			return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
		}
		return false;
	}
	
	public static boolean isNetworkStatePermissionsAvaiable(Context context)
	{
		if (context != null)
		{
			return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
		}
		return false;
	}
	
	public static void getExternalStoragePermissionForPNG()
	{
		if (ActivityCompat.shouldShowRequestPermissionRationale(ChatServiceController.hostActivity,
				Manifest.permission.WRITE_EXTERNAL_STORAGE))
		{
			String param1 = LanguageManager.getLangByKey(LanguageKeys.PERMISSION_REQUEST_PNP_TO_ALBUM2);
			String notify = LanguageManager.getLangByKey(LanguageKeys.PERMISSION_REQUEST_PNP_TO_ALBUM,param1);
			MenuController.showAllowPermissionConfirm(ChatServiceController.hostActivity, notify,
					LanguageKeys.PERMISSION_REQUEST_PNP_TO_ALBUM);
		}
		else
		{
			actualGetStoragePermissions();
		}
	}
}
