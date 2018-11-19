package com.chatsdk.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;

import com.chatsdk.R;

public class CombineBitmapManager
{
	private List<MyBitmapEntity>		entityList;

	int[] resIDs = {
					R.drawable.g026,
					R.drawable.g044,
					R.drawable.g061,
					R.drawable.g062,
					R.drawable.g063,
					R.drawable.guide_player_icon};

	private static CombineBitmapManager	instance;

	private CombineBitmapManager()
	{
	}

	public static CombineBitmapManager getInstance()
	{
		if (instance == null)
		{
			instance = new CombineBitmapManager();
		}
		return instance;
	}

	public Bitmap getCombinedBitmap(ArrayList<Bitmap> bitmapArray)
	{
		Bitmap[] bitmaps = (Bitmap[]) bitmapArray.toArray(new Bitmap[0]);
		entityList = getBitmapEntitys(bitmaps.length, 200, 5);
		for (int i = 0; i < bitmaps.length; i++)
		{
			bitmaps[i] = ThumbnailUtils.extractThumbnail(bitmaps[i], (int) entityList.get(0).width, (int) entityList.get(0).width);
		}
		Bitmap combineBitmap = BitmapUtil.getCombineBitmaps(entityList, bitmaps);

		return combineBitmap;
	}

	private int	count	= 0;

	public Bitmap getCombinedBitmap(Context context)
	{
		count = count % 9 + 1;
		entityList = getBitmapEntitys(count, 200, 10);
		Bitmap[] mBitmaps = new Bitmap[count];
		for (int i = 0; i < count; i++)
		{
			mBitmaps[i] = ThumbnailUtils.extractThumbnail(
					BitmapUtil.getScaleBitmap(context.getResources(), resIDs[MathUtil.random(0, resIDs.length - 1)]),
					(int) entityList.get(0).width, (int) entityList.get(0).width);
		}
		Bitmap combineBitmap = BitmapUtil.getCombineBitmaps(entityList, mBitmaps);
		return combineBitmap;
	}

	protected class MyBitmapEntity
	{
		double				x;
		double				y;
		double				width;
		double				height;
		static final int	devide	= 1;
		int					index	= -1;

		@Override
		public String toString()
		{
			return "MyBitmap [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", devide=" + devide + ", index=" + index
					+ "]";
		}
	}

	private List<MyBitmapEntity> getBitmapEntitys(int count, double totalWidth, double gap)
	{
		List<MyBitmapEntity> mList = new LinkedList<MyBitmapEntity>();
		if (count <= 0 || totalWidth <= 0)
			return mList;

		double width;
		int columnCount;
		if (count <= 4)
		{
			columnCount = count >= 3 ? 2 : count;
		}
		else
		{
			columnCount = 3;
		}
		int rowCount = (int) Math.ceil((double) count / columnCount);

		width = (totalWidth - gap * (columnCount + 1)) / columnCount;

		if (width <= 0)
			return mList;

		double[] yPoses = getYPoses(rowCount, totalWidth, gap, width);

		int index = 0;
		for (int i = 0; i < rowCount; i++)
		{
			for (int j = 0; j < columnCount; j++)
			{
				if (index > (count - 1))
					break;
				MyBitmapEntity entity = new MyBitmapEntity();
				entity.x = getXPos(index, count, rowCount, columnCount, gap, width, totalWidth);
				entity.y = yPoses[i];
				entity.width = width;
				entity.height = width;
				mList.add(entity);
				index++;
			}
		}
		return mList;
	}

	/**
	 * @return 各行的y坐标，从下往上排
	 */
	private static double[] getYPoses(int rowCount, double totalWidth, double gap, double width)
	{
		double[] y = new double[rowCount];
		if (rowCount == 1)
		{
			y[0] = totalWidth / 2 - width / 2;
		}
		else if (rowCount == 2)
		{
			y[0] = totalWidth / 2 + gap / 2;
			y[1] = totalWidth / 2 - gap / 2 - width;
		}
		else if (rowCount == 3)
		{
			y[0] = totalWidth - (gap + width);
			y[1] = totalWidth - (gap + width) * 2;
			y[2] = gap;
		}
		return y;
	}

	/**
	 * @param index
	 *            0-base
	 */
	private static double getXPos(int index, int count, int rowCount, int columnCount, double gap, double width, double totalWidth)
	{
		int columnIndex = index % columnCount;
		// 0-base
		int rowIndex = (int) Math.floor((double) index / columnCount);
		int rowColumnCnt = count - rowIndex * columnCount;
		if (rowColumnCnt > columnCount)
			rowColumnCnt = columnCount;

		double x = 0;
		if (rowColumnCnt == 1)
		{
			x = totalWidth / 2 - width / 2;
		}
		else if (rowColumnCnt == 2)
		{
			x = totalWidth / 2 - gap / 2 - width + (gap + width) * columnIndex;
		}
		else if (rowColumnCnt == 3)
		{
			x = gap + (gap + width) * columnIndex;
		}
		return x;
	}
}
