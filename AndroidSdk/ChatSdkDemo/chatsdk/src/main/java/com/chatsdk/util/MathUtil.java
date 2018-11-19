package com.chatsdk.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MathUtil
{
	/**
	 * 返回[min, max]
	 */
	public static int random(int min, int max)
	{
		return (int) Math.round((double) min + (double) (Math.random() * (max - min)));
	}

	/**
	 * 对[0,1)进行乘方，使得分布更靠近0
	 */
	public static int randomPow(int min, int max, int pow)
	{
		return (int) Math.round((double) min + (double) (Math.pow(Math.random(), pow) * (max - min)));
	}

	public static String getFormatNumber(int num)
	{
		NumberFormat formatter = new DecimalFormat("###,###");
		return formatter.format(num);
	}

	public static String getRoundFormatNumber(int num)
	{
		String result = "";
		if (num < 0)
		{
			num = -num;
			result = "-";
		}

		if (num >= 0 && num < 1000)
		{
			return result + num;
		}
		else if (num >= 1000 && num < 1000000)
		{
			int number = Math.round((float) (num / 1000.0));
			if (number > 0)
				return result + number + "K";
		}
		else if (num >= 1000000 && num < 1000000000)
		{
			int number = Math.round((float) (num / 1000000.0));
			if (number > 0)
				return result + number + "M";
		}
		else if (num >= 1000000000)
		{
			int number = Math.round((float) (num / 1000000000.0));
			if (number > 0)
				return result + number + "G";
		}
		return result + num;
	}

	public static String md5(String password)
	{
		try
		{
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] result = messageDigest.digest(password.getBytes()); // 得到加密后的字符组数

			StringBuffer sb = new StringBuffer();

			for (byte b : result)
			{
				int num = b & 0xff; // 这里的是为了将原本是byte型的数向上提升为int型，从而使得原本的负数转为了正数
				String hex = Integer.toHexString(num); // 这里将int型的数直接转换成16进制表示
				// 16进制可能是为1的长度，这种情况下，需要在前面补0，
				if (hex.length() == 1)
				{
					sb.append(0);
				}
				sb.append(hex);
			}

			return sb.toString();
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
