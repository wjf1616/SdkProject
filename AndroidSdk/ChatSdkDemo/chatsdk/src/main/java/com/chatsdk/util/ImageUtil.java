package com.chatsdk.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.ETC1Util;
import android.opengl.ETC1Util.ETC1Texture;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.image.AsyncImageLoader;
import com.chatsdk.image.ImageLoaderListener;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.UserInfo;
import com.chatsdk.model.db.DBHelper;
import com.chatsdk.util.HeadPicUtil.MD5;

public class ImageUtil
{
	public static int getHeadResId(Context c, String headPic)
	{
			int idFlag = ResUtil.getId(c, "drawable", headPic);
		return idFlag;
	}

	/**
	 * 如果默认的g026图片不存在，则不会设置imageView
	 * 
	 * @param headPic
	 *            可为空，使用默认头像g026
	 */
	public static void setPredefinedHeadImage(final Context c, String headPic, final ImageView imageView,
			UserInfo userInfo) {
		final int resId = getHeadResId(c, headPic);
		try {
			if (resId != 0) {
				//setImageOnUiThread(c, imageView, BitmapFactory.decodeResource(c.getResources(), resId));
				setImageOnUiThread(c, imageView, c.getResources().getDrawable(resId));
			} else {
				int defaultId = ResUtil.getId(c, "drawable", "g026");
				if (defaultId != 0){
					//setImageOnUiThread(c, imageView, BitmapFactory.decodeResource(c.getResources(), defaultId));
					setImageOnUiThread(c, imageView, c.getResources().getDrawable(defaultId));
				}
				if (userInfo != null) {
					if (userInfo.isCustomHeadImage())
						return;
					String fileName = userInfo.headPic.endsWith(".png") ? userInfo.headPic
							: (userInfo.headPic + ".png");
					setDynamicImage(c, fileName, imageView);
				}
			}
		} catch (Exception e) {
			LogUtil.printException(e);
		}
	}

	public static void setImageOnUiThread(final Context c, final ImageView imageView, final Bitmap bitmap)
	{
		if (c != null && c instanceof Activity)
		{
			((Activity) c).runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						imageView.setImageBitmap(bitmap);
//						if(ChatServiceController.getImageDetailFragment()!=null)
//				    		ChatServiceController.getImageDetailFragment().updateAttacher();
					}
					catch (OutOfMemoryError e)
					{
						e.printStackTrace();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public static void setImageOnUiThread(final Context c, final ImageView imageView, final Drawable drawable)
	{
		if (c != null && c instanceof Activity)
		{
			((Activity) c).runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						imageView.setImageDrawable(drawable);
					}
					catch (OutOfMemoryError e)
					{
						e.printStackTrace();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public static void setCustomHeadImage(final Context c, final ImageView imageView, final UserInfo user)
	{
		if (user != null && StringUtils.isNotEmpty(user.uid))
			imageView.setTag(user.uid);
		getDynamicPic(user.getCustomHeadPicUrl(), user.getCustomHeadPic(), new ImageLoaderListener()
		{
			@Override
			public void onImageLoaded(final Bitmap bitmap)
			{
				String uid = (String)imageView.getTag();
				if((StringUtils.isNotEmpty(uid) && !uid.equals(user.uid)) || bitmap == null)
					return;
				setImageOnUiThread(c, imageView, bitmap);
			}
		});
	}

	public static void getCustomHeadImage(final UserInfo user, final ImageLoaderListener listener)
	{
		if (ConfigManager.enableCustomHeadImg && user != null && user.isCustomHeadImage() && listener != null)
		{
			String locaPath = user.getCustomHeadPic();
			if (AsyncImageLoader.getInstance().isCacheExistForKey(locaPath))
			{
				Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(locaPath);
				listener.onImageLoaded(bitmap);
//				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "bitmap from cache is not null", bitmap != null,
//						"isListFilling", ChatServiceController.isListViewFling, "user", user.userName);
			}
			else /*if (!ChatServiceController.isListViewFling)*/
			{
				if (user.isCustomHeadPicExist())
				{
					AsyncImageLoader.getInstance().loadBitmapFromStore(user.getCustomHeadPic(), new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							listener.onImageLoaded(bitmap);
//							LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "bitmap from sdcard is not null",
//									bitmap != null, "isListFilling", ChatServiceController.isListViewFling, "user", user.userName);
						}
					});
				}
				else
				{
					AsyncImageLoader.getInstance().loadBitmapFromUrl(user.getCustomHeadPicUrl(), user.getCustomHeadPic(),
							new ImageLoaderListener()
							{
								@Override
								public void onImageLoaded(Bitmap bitmap)
								{
									listener.onImageLoaded(bitmap);
//									LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "bitmap from http is not null",
//											bitmap != null, "isListFilling", ChatServiceController.isListViewFling, "user", user.userName);
								}
							});
				}
			}
		}
	}
	
	public static String getCommonPicLocalPath(String fileName)
	{
		String path = DBHelper.getLocalDirectoryPath(ChatServiceController.hostActivity,"common_pic");
		path += "cache_" + MD5.getMD5Str(fileName) + ".png";
		return path;
	}
	
	public static boolean isUpdateImageExist(String imageName)
	{
		return isFileExist(getCommonPicLocalPath(imageName));
	}
	public static void getDynamicPic(final String url,final String localPath, final ImageLoaderListener listener)
	{
		if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(localPath) && listener != null)
		{
			if (AsyncImageLoader.getInstance().isCacheExistForKey(localPath))
			{
				Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(localPath);
				listener.onImageLoaded(bitmap);
			}
			else
			{
				if (isFileExist(localPath))
				{
					AsyncImageLoader.getInstance().loadBitmapFromStore(localPath, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							listener.onImageLoaded(bitmap);
						}
					});
				}
				else
				{
					AsyncImageLoader.getInstance().loadBitmapFromUrl(url, localPath, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							listener.onImageLoaded(bitmap);
						}
					});
				}
			}
		}
	}
	public static void getCommonPic(final String fileName, final ImageLoaderListener listener)
	{
		if (StringUtils.isNotEmpty(fileName) && listener != null)
		{
			String locaPath = getCommonPicLocalPath(fileName);
			if (AsyncImageLoader.getInstance().isCacheExistForKey(locaPath))
			{
				Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(locaPath);
				listener.onImageLoaded(bitmap);
//				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "bitmap from cache is not null", bitmap != null,
//						"isListFilling", ChatServiceController.isListViewFling, "user", user.userName);
			}
			else
			{
				if (isPicExist(locaPath))
				{
					AsyncImageLoader.getInstance().loadBitmapFromStore(locaPath, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							listener.onImageLoaded(bitmap);
//							LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "bitmap from sdcard is not null",
//									bitmap != null, "isListFilling", ChatServiceController.isListViewFling, "user", user.userName);
						}
					});
				}
				else
				{
					AsyncImageLoader.getInstance().loadBitmapFromCocos2dx(fileName, locaPath, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							listener.onImageLoaded(bitmap);
						}
					});
				}
			}
		}
	}
	public static void getXiaoMiPic(final String fileName, final ImageLoaderListener listener)
							{
		if (StringUtils.isNotEmpty(fileName) && listener != null)
		{
			String locaPath = fileName;
			if (AsyncImageLoader.getInstance().isCacheExistForKey(locaPath))
			{
				Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(locaPath);
				listener.onImageLoaded(bitmap);
			}
			else
			{
				if (isPicExist(locaPath))
				{
					AsyncImageLoader.getInstance().loadBitmapFromStore(locaPath, new ImageLoaderListener()
					{
								@Override
								public void onImageLoaded(Bitmap bitmap)
								{
									listener.onImageLoaded(bitmap);
//									LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "bitmap from http is not null",
//											bitmap != null, "isListFilling", ChatServiceController.isListViewFling, "user", user.userName);
						}
					});
				}
				else
				{
					AsyncImageLoader.getInstance().loadBitmapFromCocos2dx(fileName, locaPath, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							listener.onImageLoaded(bitmap);
								}
							});
				}
			}
		}
	}

	public static void setHeadImage(Context c, String headPic, final ImageView imageView, UserInfo user)
	{
		setPredefinedHeadImage(c, headPic,imageView, user);
		if(ConfigManager.enableCustomHeadImg && user != null && user.isCustomHeadImage())
		setCustomHeadImage(c, imageView, user);
	}
	
	public static void setCommonImage(final Context c, final String fileName, final ImageView imageView)
	{
		getCommonPic(fileName, new ImageLoaderListener()
		{
			@Override
			public void onImageLoaded(final Bitmap bitmap)
			{
				String picName = (String) imageView.getTag();
				if ((StringUtils.isNotEmpty(picName) && !picName.equals(fileName)) || bitmap == null)
					return;
				setImageOnUiThread(c, imageView, bitmap);
			}
		});
	}
	public static void setDynamicImage(final Context c, final String fileName, final ImageView imageView)
	{
		imageView.setTag(fileName);
		String localPath = getCommonPicLocalPath(fileName);
		String url = ConfigManager.getCDNUrl(fileName);
		getDynamicPic(url,localPath, new ImageLoaderListener()
		{
			@Override
			public void onImageLoaded(final Bitmap bitmap)
			{
				String picName = (String) imageView.getTag();
				if ((StringUtils.isNotEmpty(picName) && !picName.equals(fileName)) || bitmap == null)
					return;
				setImageOnUiThread(c, imageView, bitmap);
			}
		});
	}
	public static void setXiaomiImage(final Context c, final String fileName, final ImageView imageView)
	{
		getXiaoMiPic(fileName, new ImageLoaderListener()
		{
			@Override
			public void onImageLoaded(final Bitmap bitmap)
			{
				String picName = (String)imageView.getTag();
				if((StringUtils.isNotEmpty(picName) && !picName.equals(fileName)) || bitmap == null)
					return;
				setImageOnUiThread(c, imageView, bitmap);
			}
		});
	}

	/**
	 * x匹配宽度，y按tile重复
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void setYRepeatingBG(Activity activity, View view, int id)
	{
		Drawable d = ImageUtil.getRepeatingBG(activity, R.drawable.mail_list_bg);
		if (d == null)
			return;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
		{
			view.setBackgroundDrawable(null);
			view.setBackgroundDrawable(d);
		}
		else
		{
			view.setBackground(null);
			view.setBackground(d);
		}
	}

	public static Drawable getRepeatingBG(Activity activity, int center)
	{
		try
		{
			DisplayMetrics dm = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = true;
			Bitmap center_bmp = BitmapFactory.decodeResource(activity.getResources(), center, options);
			if (center_bmp == null)
				return null;
			center_bmp.setDensity(Bitmap.DENSITY_NONE);
			center_bmp = Bitmap.createScaledBitmap(center_bmp, dm.widthPixels, center_bmp.getHeight(), true);
			if (center_bmp == null)
				return null;
			BitmapDrawable center_drawable = new BitmapDrawable(activity.getResources(), center_bmp);
			// change here setTileModeY to setTileModeX if you want to repear in X
			center_drawable.setTileModeY(Shader.TileMode.REPEAT);
			return center_drawable;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean isPicExist(String path)
	{
		String fileName = path;
		if (StringUtils.isEmpty(fileName))
			return false;

		try
		{
			File file = new File(fileName);
			if (file.exists())
			{
				return true;
			}
			if (!path.endsWith(".png") && !path.endsWith(".jpg"))
			{
				fileName = path + ".png";
				file = new File(fileName);
				if (file.exists())
				{
					return true;
				}
				else
				{
					fileName = path + ".jpg";
					file = new File(fileName);
					if (file.exists())
					{
						return true;
					}
				}
			}
			else
			{
				file = new File(path);
				if (file.exists())
				{
					return true;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean isFileExist(String path)
	{
		if (StringUtils.isEmpty(path))
			return false;
		try
		{
			File file = new File(path);
			if (file.exists())
			{
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
	{
        // Raw height and width of image  
        final int height = options.outHeight;  
        final int width = options.outWidth;  
        int inSampleSize = 1;  
  
		if (height > reqHeight || width > reqWidth)
		{
			if (width > height)
			{
                inSampleSize = Math.round((float) height / (float) reqHeight);  
			}
			else
			{
                inSampleSize = Math.round((float) width / (float) reqWidth);  
            }  
        }
        return inSampleSize;  
    } 
       
	public static Bitmap decodeSampledBitmapFromFile(byte[] data, int reqWidth, int reqHeight)
	{
  
        // First decode with inJustDecodeBounds=true to check dimensions  
        final BitmapFactory.Options options = new BitmapFactory.Options();  
        options.inJustDecodeBounds = true;  
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
  
        // Calculate inSampleSize  
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);  
  
        // Decode bitmap with inSampleSize set  
        options.inJustDecodeBounds = false;  
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);  
    } 
	private static File saveImage(String fileName, String compressFileName,BitmapFactory.Options options)
	{
		Bitmap bitmap = BitmapFactory.decodeFile(fileName, options);
    
		System.out.println("compressImage compressFileName:" + compressFileName);
		FileOutputStream fos = null;
		File outputFile = new File(compressFileName);
		try
		{
			if (!outputFile.exists())
				outputFile.createNewFile();
			fos = new FileOutputStream(outputFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
			fos.close();
		}
		catch (IOException e)
		{
			if (outputFile.exists())
			{
				outputFile.delete();
			}
			e.printStackTrace();
		}
		if (!bitmap.isRecycled())
		{
			bitmap.recycle();
		}
		return outputFile;
	}
	public static File compressImage(String fileName, String compressFileName)
	{
		File outputFile = new File(fileName);
		long fileSize = outputFile.length();
		final long fileMaxSize = 200 * 1024;
		if (fileSize >= fileMaxSize)
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(fileName, options);
			int height = options.outHeight;
			int width = options.outWidth;
			double scale = Math.sqrt((float) fileSize / fileMaxSize);
			options.outHeight = (int) (height / scale);
			options.outWidth = (int) (width / scale);
			options.inSampleSize = (int) (scale + 0.5);
			options.inJustDecodeBounds = false;
			outputFile = saveImage(fileName, compressFileName, options);
		}
		else
		{
			outputFile = saveImage(fileName, compressFileName, null);
		}
		return outputFile;
	}
}
