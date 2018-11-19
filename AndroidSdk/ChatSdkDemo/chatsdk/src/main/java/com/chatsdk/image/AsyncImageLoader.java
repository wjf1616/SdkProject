package com.chatsdk.image;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;

import com.chatsdk.controller.JniController;
import com.chatsdk.model.ConfigManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AsyncImageLoader
{
	private final static int		HTTP_STATE_OK	= 200;
	private final static int		BUFFER_SIZE		= 1024 * 4;
	private final static int		DEFAULT_TIMEOUT	= 30 * 1000;

	private static ImageStoreCache	mImageCache		= null;
	private static ExecutorService	executorService	= null;

	private static AsyncImageLoader	mInstance		= null;

	private AsyncImageLoader()
	{
		mImageCache = new ImageStoreCache(16);
		executorService = Executors.newFixedThreadPool(5);
	}

	public static AsyncImageLoader getInstance()
	{
		if (mInstance == null)
		{
			synchronized (AsyncImageLoader.class)
			{
				if (mInstance == null)
					mInstance = new AsyncImageLoader();
			}
		}
		return mInstance;
	}

	public boolean isCacheExistForKey(String key)
	{
		return mImageCache.containsKey(key);
	}

	public Bitmap loadBitmapFromCache(String key)
	{
		return mImageCache.get(key);
	}

	public static void removeMemoryCache(String key)
	{
		if(mImageCache!=null && StringUtils.isNotEmpty(key) && mImageCache.containsKey(key))
		mImageCache.removeMemoryCache(key);
	}

	class ImageLoaderRunnable implements Runnable
	{
		private String	url;
		private String	localUrl;
		private Handler	handler;
		private boolean	isFromHttp;

		ImageLoaderRunnable(String localUrl, Handler handler)
		{
			this.url = "";
			this.localUrl = localUrl;
			this.handler = handler;
			this.isFromHttp = false;
		}

		ImageLoaderRunnable(String url, String localUrl, Handler handler)
		{
			this.url = url;
			this.localUrl = localUrl;
			this.handler = handler;
			this.isFromHttp = true;
		}

		@Override
		public void run()
		{
//			//System.out.println("ImageLoaderRunnable  "+Thread.currentThread().getName()+ " is running.");
			Bitmap ret = mImageCache.getFromLocalPath(localUrl);
			if (isFromHttp && ret == null)
			{
				ret = mImageCache.cache(localUrl, getHttpBitmap(url, DEFAULT_TIMEOUT));
			}
			Message message = handler.obtainMessage(0, ret);
			message.sendToTarget();
		}

	}

	public void loadBitmapFromStore(final String localUrl, final ImageLoaderListener listener)
	{
		Handler handler = new Handler()
		{
			public void handleMessage(Message message)
			{
				if (listener != null)
				{
					listener.onImageLoaded((Bitmap) message.obj);
				}
			}
		};

		if (executorService != null)
			executorService.execute(new ImageLoaderRunnable(localUrl, handler));
	}
	class ImageDownLoadRunnable implements Runnable
	{
		private String	url;
		private String	localUrl;
		ImageDownLoadRunnable(String url, String localUrl)
		{
			this.url = url;
			this.localUrl = localUrl;
		}
		@Override
		public void run()
		{
			ImageStoreCache.cacheRawData(localUrl, getHttpBitmap(url, DEFAULT_TIMEOUT));
		}
	}
	public void downLoadUpdateImage(final String imageUrl,final String imageLocalUrl)
	{
		if (executorService != null)
			executorService.execute(new ImageDownLoadRunnable(imageUrl, imageLocalUrl));
	}

	class ImageLoaderFromCocos2dRunnable implements Runnable
	{
		private String	picName;
		private String	localPath;
		private Handler	handler;

		ImageLoaderFromCocos2dRunnable(String picName, String localPath, Handler handler)
		{
			this.picName = picName;
			this.localPath = localPath;
			this.handler = handler;
		}

		@Override
		public void run()
		{
//			//System.out.println("ImageLoaderFromCocos2dRunnable  "+Thread.currentThread().getName()+ " is running.");
			Bitmap ret = mImageCache.getFromLocalPath(localPath);
			if (ret == null)
			{
				//System.out.println("setCommonImage 4");
				if (picName.length() <= 4)
					return;
				String picFileName = picName.substring(0, picName.length() - 4);
				Map<String, String> map = ConfigManager.getInstance().getLocalDynamicImageMap();
				if (map == null || !map.containsKey(picFileName))
					return;
				String plistName = map.get(picFileName);
				if (StringUtils.isEmpty(plistName))
					return;
				plistName += ".plist";
				//System.out.println("plistName:" + plistName + "   picName:" + picFileName);
				byte[] picByteArr = JniController.getInstance().excuteJNIMethod("getCommonPic", new Object[] { plistName, picName });
				ret = mImageCache.cache(localPath, picByteArr);
			}
			Message message = handler.obtainMessage(0, ret);
			message.sendToTarget();
		}

	}

	public void loadBitmapFromCocos2dx(final String picName, final String localPath, final ImageLoaderListener listener)
	{
		Handler handler = new Handler()
		{
			public void handleMessage(Message message)
			{
				if (listener != null)
				{
					listener.onImageLoaded((Bitmap) message.obj);
				}
			}
		};

		if (executorService != null)
			executorService.execute(new ImageLoaderFromCocos2dRunnable(picName, localPath, handler));
	}

	public void loadBitmapFromUrl(final String url, final String localUrl, final ImageLoaderListener listener)
	{
		Handler handler = new Handler()
		{
			public void handleMessage(Message message)
			{
				if (listener != null)
				{
					listener.onImageLoaded((Bitmap) message.obj);
				}
			}
		};

		if (executorService != null)
			executorService.execute(new ImageLoaderRunnable(url, localUrl, handler));
	}

	public static byte[] getHttpBitmap(String bitmapPath, int timeout)
	{
		InputStream is = null;
		HttpURLConnection conn = null;
		BufferedInputStream bis = null;
		ByteArrayOutputStream out = null;
		try
		{
			out = new ByteArrayOutputStream();
			URL url = new URL(bitmapPath);
			conn = (HttpURLConnection) url.openConnection();
			if (timeout > 0)
			{
				conn.setConnectTimeout(timeout);
				conn.setReadTimeout(timeout);
			}
			conn.setRequestProperty("Connection", "close");
			conn.connect();
			is = conn.getInputStream();
			url = null;
			if (conn.getResponseCode() == HTTP_STATE_OK)
			{
				bis = new BufferedInputStream(is, BUFFER_SIZE);
				int i = -1;
				byte buf[] = new byte[4 * 1024];
				while ((i = bis.read(buf)) != -1)
				{
					out.write(buf, 0, i);
				}
				byte imgData[] = out.toByteArray();

				return imgData;
			}
		}
		catch (Exception e)
		{
			Log.e("getHttpBitmap", "load image from url failed");
		}
		finally
		{
			try
			{
				if (is != null)
					is.close();
				if (bis != null)
					bis.close();
				if (out != null)
					out.close();
				if (conn != null)
					conn.disconnect();
				is = null;
				bis = null;
				out = null;
				conn = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}
	public static String getHttpString(String fileUrl)
	{
		InputStream is = null;
		HttpURLConnection conn = null;
		InputStreamReader inputRead = null;
		BufferedReader reader = null;
		StringBuilder resultBuilder = new StringBuilder();
		try
		{
			URL url = new URL(fileUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(DEFAULT_TIMEOUT);
			conn.setReadTimeout(DEFAULT_TIMEOUT);
			conn.setRequestProperty("Connection", "close");
			conn.connect();
			is = conn.getInputStream();
			inputRead = new InputStreamReader(is);
			if (conn.getResponseCode() == HTTP_STATE_OK)
			{
				reader = new BufferedReader(inputRead);
				String readLine = "";
				while ((readLine = reader.readLine()) != null)
					resultBuilder.append(readLine);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (is != null)
					is.close();
				if (inputRead != null)
					inputRead.close();
				if (reader != null)
					reader.close();
				if (conn != null)
					conn.disconnect();
				is = null;
				inputRead = null;
				reader = null;
				conn = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return resultBuilder.toString();
	}
}
