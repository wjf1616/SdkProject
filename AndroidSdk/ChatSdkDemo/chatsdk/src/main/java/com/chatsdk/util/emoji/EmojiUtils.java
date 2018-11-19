package com.chatsdk.util.emoji;

import android.graphics.Typeface;
import android.os.Build;
import android.widget.TextView;

public class EmojiUtils
{
	private static Typeface typeFace;
	public static void setTypeface(TextView textView)
	{
		if (typeFace == null)
		{
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
//				typeFace = Typeface.createFromAsset(textView.getContext().getAssets(), "fonts/OpenSansEmoji.ttf");
			}else{
				typeFace = Typeface.createFromAsset(textView.getContext().getAssets(), "fonts/SamsungEmoji.ttf");
			}
		}
		textView.setTypeface(typeFace);
	}
	
	public static String stringToUnicode(String string)
	{
		StringBuffer unicode = new StringBuffer();

		for (int i = 0; i < string.length(); i++)
		{
			// 取出每一个字符
			char c = string.charAt(i);

			// 转换为unicode
			unicode.append("\\u" + Integer.toHexString(c));
		}

		return unicode.toString();
	}

	public static String unicodeToString(String unicode)
	{
		StringBuffer string = new StringBuffer();

		String[] hex = unicode.split("\\\\u");

		for (int i = 1; i < hex.length; i++)
		{
			// 转换出每一个代码点
			int data = Integer.parseInt(hex[i], 16);

			// 追加成string
			string.append((char) data);
		}

		return string.toString();
	}

}
