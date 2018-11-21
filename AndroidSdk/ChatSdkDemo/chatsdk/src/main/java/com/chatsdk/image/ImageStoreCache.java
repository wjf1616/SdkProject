package com.chatsdk.image;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.chatsdk.util.ImageUtil;
import com.chatsdk.util.LogUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ImageStoreCache extends MemoryCache<String, Bitmap>
{
	private static final int	JPG_FILE_FORMAT	= 1;

	private static final int	PNG_FILE_FORMAT	= 2;

	public ImageStoreCache(int cacheSize)
	{
		super(cacheSize);
	}

	@Override
	public void cache(String localUrl, Bitmap value)
	{
		if (value == null || isStringInvalid(localUrl))
		{
			return;
		}
		cacheToMemory(localUrl, value);
		cacheToStore(localUrl, value);
	}

	public Bitmap cache(String localUrl, byte[] value)
	{
		if (value == null || value.length < 1 || StringUtils.isEmpty(localUrl))
		{
			return null;
		}

		cacheRawData(localUrl, value);

		Bitmap ret = null;
		try
		{
			ret = ImageUtil.decodeImg(value);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}

		if (ret != null)
		{
			cache(localUrl, ret);
		}
		return ret;
	}

	public static void cacheRawData(String localUrl, byte[] value)
	{
		if (value == null || value.length < 1 || StringUtils.isEmpty(localUrl))
		{
			return;
		}

		FileOutputStream fOut = null;
		File f = new File(localUrl);
		try
		{
			if (!f.exists())
			{
				f.createNewFile();
			}
			fOut = new FileOutputStream(f);
			fOut.write(value, 0, value.length);
			fOut.flush();
		}
		catch (IOException e)
		{
			if (f.exists())
			{
				f.delete();
			}
			Log.e("cacheRawData", "store bitmap to store device failed.");
		}
		finally
		{
			try
			{
				if (fOut != null)
				{
					fOut.close();
					fOut = null;
				}
			}
			catch (Exception e)
			{
			}
		}
	}

	public static String cacheToStore(String localUrl, Bitmap value)
	{
		if (value == null || isStringInvalid(localUrl))
		{
			return null;
		}
		FileOutputStream fOut = null;
		ByteArrayOutputStream stream = null;
		File f = new File(localUrl);
		try
		{
			if (!f.exists())
			{
				f.createNewFile();
			}

			stream = new ByteArrayOutputStream();

			int format = getFileFormat(localUrl);
			if (format == JPG_FILE_FORMAT)
			{
				value.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			}
			else if (format == PNG_FILE_FORMAT)
			{
				value.compress(Bitmap.CompressFormat.PNG, 100, stream);
			}
			fOut = new FileOutputStream(f);
			byte[] byteArray = stream.toByteArray();
			BufferedOutputStream bStream = new BufferedOutputStream(fOut);
			bStream.write(byteArray);
			if (bStream != null)
			{
				bStream.close();
			}

			fOut.flush();
			return f.getAbsolutePath();
		}
		catch (IOException e)
		{
			if (f.exists())
			{
				f.delete();
			}
			Log.e("cacheToStore", "store raw data to store device failed.");
		}
		finally
		{
			try
			{
				if (fOut != null)
				{
					fOut.close();
					fOut = null;
				}

			}
			catch (Exception e)
			{
			}
		}
		return null;
	}

	public static byte[] getRawCacheData(String localPath)
	{
		FileInputStream fIn = null;
		File f = new File(localPath);
		ByteArrayOutputStream fout = null;
		try
		{
			fIn = new FileInputStream(f);
			fout = new ByteArrayOutputStream(fIn.available());

			byte[] buffer = new byte[1024];
			int readed = 0;
			while ((readed = fIn.read(buffer, 0, buffer.length - 1)) != -1)
			{
				fout.write(buffer, 0, readed);
			}
			byte[] ret = fout.toByteArray();
			return ret;
		}
		catch (Exception e)
		{
			Log.e("getRawCacheData", "get raw data from store device failed.");
		}
		finally
		{
			try
			{
				if (fIn != null)
				{
					fIn.close();
				}
				if (fout != null)
				{
					fout.close();
				}
			}
			catch (Exception e2)
			{
			}
		}
		return null;
	}

	@Override
	public Bitmap get(String key)
	{
		if (StringUtils.isEmpty(key))
			return null;
		Bitmap ret = getFromMemory(key);
		return ret;
	}

	public Bitmap getFromLocalPath(String path)
	{
		if (StringUtils.isEmpty(path))
			return null;
		try
		{
			Bitmap ret = BitmapFactory.decodeFile(path);
			if (ret != null)
			{
				cacheToMemory(path, ret);
			}
			else
			{
				String fileName = path + ".png";
				File file = new File(fileName);
				if (file.exists())
				{
					ret = BitmapFactory.decodeFile(path);
					if (ret != null)
					{
						cacheToMemory(path, ret);
					}
				}
				else
				{
					fileName = path + ".jpg";
					file = new File(fileName);
					if (file.exists())
					{
						ret = BitmapFactory.decodeFile(path);
						if (ret != null)
						{
							cacheToMemory(path, ret);
						}
					}
				}
			}
			return ret;
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		return null;
	}

	@Override
	public void removeCache(String localPath)
	{
		if (isStringInvalid(localPath))
		{
			return;
		}

		removeMemoryCache(localPath);

		File file = new File(localPath);
		if (file.exists())
		{
			file.delete();
		}
	}

	private static int getFileFormat(String filename)
	{
		if (filename.toUpperCase(Locale.getDefault()).endsWith(".JPG"))
		{
			return JPG_FILE_FORMAT;
		}
		return PNG_FILE_FORMAT;
	}

	public static boolean isStringInvalid(String str)
	{
		if (str == null || str.length() < 1)
		{
			return true;
		}
		return false;
	}
}
