package com.chatsdk.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.TextView;

public class ScaleUtil
{
	private static int screenWidth = 0;
    private static int screenHeight = 0;
    private static float screenDensity = 0;
    private static int densityDpi = 0;

    public static void initialize(Context context){
        if (context == null || screenWidth > 0)
            return;
        
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;     // 屏幕宽度
        screenHeight = metrics.heightPixels;   // 屏幕高度
        screenDensity = metrics.density;      // 0.75 / 1.0 / 1.5 / 2.0 / 3.0
        densityDpi = metrics.densityDpi;  //120 160 240 320 480
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "screenDensity", screenDensity, "densityDpi", densityDpi);
    }

    public static int dip2px(float dipValue){
        return (int)(dipValue * screenDensity + 0.5f);
    }

    public static int px2dip(float pxValue){

        return (int)(pxValue / screenDensity + 0.5f);
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }
	
	public static int getAdjustTextSize(float size, double textRatio)
	{
		int newTextSize = (int) (size * textRatio);
		return newTextSize;
	}

	public static void adjustTextSize(TextView textView, double textRatio)
	{
		float newTextSize = (int) (textView.getTextSize() * textRatio);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
	}

	public static int dip2px(Context context, float dipValue)
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue)
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static float dipToPixels(Context context, float dipValue)
	{
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, context.getResources().getDisplayMetrics());
	}
}
