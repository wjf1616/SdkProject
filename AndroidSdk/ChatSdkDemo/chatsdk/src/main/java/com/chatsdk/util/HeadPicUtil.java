package com.chatsdk.util;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.model.db.DBHelper;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HeadPicUtil
{
	/**
	 * 形如http://cok.eleximg.com/cok/img/710001/bd65ca6d7d40cf95a3fe6b7fbb5cef7c.jpg
	 */
	public static String getCustomPicUrl(String uid, int picVer)
	{
		if (uid.length() == 0)
		{
			return "";
		}
		String url = "http://img.im30app.com/az/img/";
		if(picVer > 100000)
			url = "http://img.im30app.com/az/photo/";
		String md5Str = uid + "_" + picVer;

		String tempStr = uid;
		url += tempStr.substring(tempStr.length() - 6, tempStr.length()) + "/" + MD5.getMD5Str(md5Str) + ".jpg";

		return url;
	}

	public static String getCustomPic(String url)
	{
		if( ChatServiceController.hostActivity != null ) {
			try
			{
				String path = DBHelper.getHeadDirectoryPath(ChatServiceController.hostActivity);
				path += "cache_" + MD5.getMD5Str(url) + ".png";
				return path;
			}
			catch (Exception e){

			}
		}
		return "";
	}

	public static class MD5
	{
		public static String getMD5Str(String str)
		{
			MessageDigest messageDigest = null;

			try
			{
				messageDigest = MessageDigest.getInstance("MD5");

				messageDigest.reset();

				messageDigest.update(str.getBytes("UTF-8"));
			}
			catch (NoSuchAlgorithmException e)
			{
				throw new RuntimeException(e);
			}
			catch (UnsupportedEncodingException e)
			{
				throw new RuntimeException(e);
			}

			byte[] byteArray = messageDigest.digest();

			StringBuffer md5StrBuff = new StringBuffer();

			for (int i = 0; i < byteArray.length; i++)
			{
				if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
					md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
				else
					md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
			}

			return md5StrBuff.toString();
		}

		public static String getMD5Str2(String str)
		{
			String hashtext = "";
			try
			{
				MessageDigest m = MessageDigest.getInstance("MD5");
				m.reset();
				m.update(str.getBytes());
				byte[] digest = m.digest();
				BigInteger bigInt = new BigInteger(1, digest);
				hashtext = bigInt.toString(16);
			}
			catch (NoSuchAlgorithmException e)
			{
				e.printStackTrace();
			}
			return hashtext;
		}
		public static String stringMD5(String input)
		{
			try
			{
				// 拿到一个MD5转换器（如果想要SHA1参数换成”SHA1”）
				MessageDigest messageDigest = MessageDigest.getInstance("MD5");
				// 输入的字符串转换成字节数组
				byte[] inputByteArray = input.getBytes();
				// inputByteArray是输入字符串转换得到的字节数组
				messageDigest.update(inputByteArray);
				byte[] resultByteArray = messageDigest.digest();
				// 字符数组转换成字符串返回
				return byteArrayToHex(resultByteArray);
			}
			catch (NoSuchAlgorithmException e)
			{
				return null;
			}
		}
		public static String byteArrayToHex(byte[] byteArray)
		{
			// 首先初始化一个字符数组，用来存放每个16进制字符
			char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
			// new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））
			char[] resultCharArray = new char[byteArray.length * 2];
			// 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
			int index = 0;
			for (byte b : byteArray)
			{
				resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
				resultCharArray[index++] = hexDigits[b & 0xf];
			}
			return new String(resultCharArray).toLowerCase();
		}
	}
}
