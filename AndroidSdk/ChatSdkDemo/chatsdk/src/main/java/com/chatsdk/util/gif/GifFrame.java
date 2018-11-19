package com.chatsdk.util.gif;

import android.graphics.Bitmap;

public class GifFrame
{
	/**
	 * @param im
	 *            图片
	 * @param del
	 *            延时
	 */
	public GifFrame(Bitmap im, int del)
	{
		image = im;
		delay = del;
	}

	/** 图片 */
	public Bitmap	image;
	/** 延时 */
	public int		delay;
	/** 下一帧 */
	public GifFrame	nextFrame	= null;
}
