package com.chatsdk.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.support.v4.app.Fragment;
import android.util.Log;
import java.lang.reflect.Field;

public final class ResUtil
{
	private static String		TAG			= "HelpShiftDebug";
	private static String		packageName	= null;
	private static Class		r			= null;
	private static Resources	resources	= null;

	public static String getString(Context c, String name)
	{
		return c.getString(getId(c, "string", name));
	}

	public static String getString(Fragment f, String name)
	{
		Context c = f.getActivity();
		return c.getString(getId(c, "string", name));
	}

	public static int getColor(Context c, String name)
	{
		return c.getResources().getColor(getId(c, "color", name));
	}

	public static int getAttrId(Context c, String name)
	{
		return getId(c, "attr", name);
	}

	public static int getDimenId(Context c, String name)
	{
		return getId(c, "dimen", name);
	}

	public static String getQuantityString(Context c, String name, int count, Object[] formatArgs)
	{
		return c.getResources().getQuantityString(getId(c, "plurals", name), count, formatArgs);
	}

	public static int[] getIdArray(Context c, String className, String name)
	{
		int[] id = null;

		if (packageName == null)
		{
			packageName = c.getPackageName();
		}

		if (r == null)
		{
			try
			{
				r = Class.forName(packageName + ".R");
			}
			catch (ClassNotFoundException e)
			{
				Log.d(TAG, "ClassNotFoundException", e);
			}
		}
		try
		{
			Class[] classes = r.getClasses();
			Class desireClass = null;

			for (int i = 0; i < classes.length; i++)
			{
				if (classes[i].getName().split("\\$")[1].equals(className))
				{
					desireClass = classes[i];
					break;
				}
			}

			if (desireClass != null)
				id = (int[]) desireClass.getField(name).get(desireClass);
		}
		catch (IllegalArgumentException e)
		{
			Log.d(TAG, "IllegalArgumentException", e);
		}
		catch (SecurityException e)
		{
			Log.d(TAG, "SecurityException", e);
		}
		catch (IllegalAccessException e)
		{
			Log.d(TAG, "IllegalAccessException", e);
		}
		catch (NoSuchFieldException e)
		{
			Log.d(TAG, "NoSuchFieldException", e);
		}
		return id;
	}

	public static int getId(Context c, String className, String name)
	{
		int id = 0;

		if (packageName == null)
		{
			packageName = c.getPackageName();
		}

		if (resources == null)
		{
			resources = c.getResources();
		}
		try
		{
			id = resources.getIdentifier(name, className, packageName);
		}
		catch (Resources.NotFoundException e)
		{
			Log.d(TAG, "Resources.NotFoundException", e);
		}
		return id;
	}

	public static int getId(Fragment f, String className, String name)
	{
		Context c = f.getActivity();
		return getId(c, className, name);
	}
}