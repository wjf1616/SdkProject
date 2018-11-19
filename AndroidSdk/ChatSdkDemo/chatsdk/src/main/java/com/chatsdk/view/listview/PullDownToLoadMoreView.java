package com.chatsdk.view.listview;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.chatsdk.R;
import com.chatsdk.util.LogUtil;

public class PullDownToLoadMoreView extends FrameLayout
{
	private static int				timeInterval					= 400;
	private GestureDetector			mGestureDetector;
	private Scroller				mScroller;
	private int						scrollType;
	private int						bottomViewInitializeVisibility	= View.INVISIBLE;
	private int						topViewInitializeVisibility		= View.INVISIBLE;
	private boolean					hasAddTopAndBottom				= false;
	private int						scrollDeltaY					= 0;
	private int						topViewHeight					= 0;
	private int						bottomViewHeight;
	private boolean					isScrollToTop					= false;
	private boolean					isScrollFarTop					= false;
	private boolean					isMoveDown						= false;
	private boolean					isScrollToDownStoped			= false;
	private boolean					isFristTouch					= true;
	private boolean					isHideTopView					= false;
	private boolean					isCloseTopAllowRefersh			= true;
	private boolean					isBottomWithOutScroll			= true;
	private ListViewLoadListener	mListViewLoadListener;
	private OnTouchListener			mTouchListener;
	private View					topView;
	private View					bottomView;
	private Context					context;
	private boolean					isProgressBarShowed				= false;
	private long					mStartTime						= 0;

	private Handler					mHandler						= new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			super.handleMessage(msg);
			switch (scrollType)
			{
				case 0:
					if (topView.getVisibility() != View.VISIBLE)
					{
						break;
					}

					if (mListViewLoadListener != null)
					{
						scrollDeltaY = Math.abs(getScrollY());
						mListViewLoadListener.refreshData();
					}

					break;
				case 1:
					if (bottomView.getVisibility() != View.VISIBLE)
					{
						break;
					}
					scrollTo(0, bottomViewHeight);
					break;
			}

		};
	};

	public void hideProgressBar()
	{
		if (isProgressBarShowed)
		{
			scrollTo(0, getPullDownHeight());
			isProgressBarShowed = false;
			mStartTime = System.currentTimeMillis();
		}
	}

	public boolean getIsProgressBarShowed()
	{
		return isProgressBarShowed;
	}

	public PullDownToLoadMoreView(Context context)
	{
		this(context, null);
	}

	public PullDownToLoadMoreView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public PullDownToLoadMoreView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs);
		this.mScroller = new Scroller(context, new AccelerateInterpolator());
		this.mGestureDetector = new GestureDetector(context, onGestureListener);
		this.context = context;
	}

	private OnGestureListener	onGestureListener	= new OnGestureListener()
	{
		@Override
		public boolean onSingleTapUp(MotionEvent e)
		{
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e)
		{

		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			int i = -1;
			int j = 1;
			if (distanceY <= 0.0F)
			{
				isMoveDown = false;
			}
			else
			{
				isMoveDown = true;
			}
			int k;

			if (((!isMoveDown) || (!isScrollFarTop))
					&& ((isMoveDown) || (getScrollY() - topViewHeight <= 0) || (!isScrollFarTop)))
			{

				if (((isMoveDown) || (!isScrollToTop))
						&& ((!isMoveDown) || (getScrollY() - topViewHeight >= 0) || (!isScrollToTop)))
				{
					j = 0;
				}
				else
				{
					k = (int) (0.5D * distanceY);
					if (k != 0)
					{
						i = k;
					}
					else if (distanceY > 0.0F)
					{
						i = j;
					}
					if (i + getScrollY() > topViewHeight)
					{
						i = topViewHeight - getScrollY();
					}
					if ((getScrollY() + topViewHeight <= 0) || getScrollY() > 0)
						scrollBy(0, i);
					return true;
				}
			}
			else
			{
				k = (int) (0.5D * distanceY);
				if (k != 0)
				{
					i = k;
				}
				else if (distanceY > 0.0F)
				{
					i = j;
				}

				if ((i + getScrollY() < topViewHeight) && (!isMoveDown))
				{
					i = topViewHeight - getScrollY();
				}
				scrollBy(0, i);
				return true;
			}
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e)
		{

		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e)
		{
			if (!mScroller.isFinished())
			{
				mScroller.abortAnimation();
			}
			return false;
		}
	};

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		this.setBackgroundColor(Color.TRANSPARENT);
		if (!this.hasAddTopAndBottom)
		{
			View topView = inflate(this.context, R.layout.loading_view, null);
			View bottomView = inflate(this.context, R.layout.loading_view, null);
			addView(topView, 0, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			addView(bottomView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			this.hasAddTopAndBottom = true;
		}
		int childrenCount = getChildCount();
		int index = 0;
		int topValue = 0;
		int screenWidth = 0;
		boolean hasMeasuredWidth = true;
		int topViewHeight = 0;
		while (true)
		{
			if (index >= childrenCount)
			{
				this.topView = getChildAt(0);
				this.bottomView = getChildAt(-1 + getChildCount());
				if (topView != null)
				{
					this.topView.setVisibility(View.VISIBLE);
					if (!hasMeasuredWidth && screenWidth != 0)
					{
						topView.layout(0, 0, screenWidth, topViewHeight);
					}
					this.topViewHeight = this.topView.getHeight();
				}
				
				if(bottomView!=null)
				{
					this.bottomView.setVisibility(View.INVISIBLE);
					this.bottomViewHeight = this.bottomView.getHeight();
				}

				if ((!this.isHideTopView) && (this.topViewHeight != 0))
				{
					this.isHideTopView = true;
					scrollTo(0, this.topViewHeight);
				}
				return;
			}
			View view = getChildAt(index);
			try
			{
				int height = view.getMeasuredHeight();
				int width = view.getMeasuredWidth();
				if (width != 0)
					screenWidth = width;
				else
					hasMeasuredWidth = false;
				if (height == 0 || width == 0)
				{
					int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
					int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
					view.measure(w, h);
					height = view.getMeasuredHeight();
					width = view.getMeasuredWidth();
				}

				if (index == 0)
					topViewHeight = height;
				if (view.getVisibility() != View.GONE)
				{
					// 可能在进一步调用obtainView时出异常 NullPointerException 或
					// IllegalStateException
					view.layout(0, topValue, width, topValue + height);
					topValue += height;
				}
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
			index++;
		}
	}

	public final int getTopViewHeight()
	{
		return this.topViewHeight;
	}

	public int getPullDownHeight()
	{
		int scrollValue = (getScrollY() < topViewHeight && getScrollY() > 0) ? getScrollY() : topViewHeight;
		return scrollDeltaY > topViewHeight ? scrollDeltaY : scrollValue;
	}

	public boolean isTopViewShowed()
	{
		return topView.getVisibility() == View.VISIBLE;
	}

	public final void startTopScroll()
	{
		if (!this.isCloseTopAllowRefersh)
		{
			if (this.topView.getVisibility() == View.INVISIBLE)
			{
				this.mScroller.startScroll(0, getScrollY(), 0, -getScrollY() + this.topViewHeight, 200);
			}
			else if (this.topView.getVisibility() == View.VISIBLE)
			{
				this.mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), 200);
			}

			if (!isProgressBarShowed)
			{
				this.isScrollToDownStoped = true;
			}

			this.scrollType = 0;
			this.isFristTouch = false;
		}
		else
		{
			this.mScroller.startScroll(0, getScrollY(), 0, -getScrollY() + this.topViewHeight, 200);
		}
		postInvalidate();
	}

	public void setListViewLoadListener(ListViewLoadListener listener)
	{
		this.mListViewLoadListener = listener;
	}
	
	public void setListViewTouchListener(OnTouchListener listener)
	{
		this.mTouchListener = listener;
	}

	public final void setAllowPullDownRefersh(boolean paramBoolean)
	{
		this.isCloseTopAllowRefersh = paramBoolean;
	}

	public final void setBottomViewWithoutScroll(boolean isBottomWithOutScroll)
	{
		this.isBottomWithOutScroll = isBottomWithOutScroll;
	}

	public final void setTopViewInitialize(boolean isInitialize)
	{
		this.topViewInitializeVisibility = isInitialize ? View.VISIBLE : View.INVISIBLE;
		if (this.topView != null)
		{
			this.topView.setVisibility(this.topViewInitializeVisibility);
		}
	}

	@Override
	protected void onScrollChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
	{
		super.onScrollChanged(paramInt1, paramInt2, paramInt3, paramInt4);
	}

	private void startScroll()
	{
		if (getScrollY() - this.topViewHeight < 0)
		{
			if (!this.isCloseTopAllowRefersh && getScrollY() + topViewHeight > 0)
			{
				if (this.topView.getVisibility() == View.INVISIBLE)
				{
					this.mScroller.startScroll(0, getScrollY(), 0, -getScrollY() + this.topViewHeight, 200);
				}
				else if (this.topView.getVisibility() == View.VISIBLE)
				{
					if (getScrollY() < 0)
						scrollBy(0, -getScrollY());
				}
				this.scrollType = 0;
				if (!isProgressBarShowed)
				{
					if (System.currentTimeMillis() - mStartTime >= 2000)
					{
						this.isScrollToDownStoped = true;
					}
					else
					{
						scrollTo(0, topViewHeight);
					}
				}
				this.isFristTouch = false;
			}
			else
			{
				this.mScroller.startScroll(0, getScrollY(), 0, -getScrollY() + this.topViewHeight, 200);
			}
			postInvalidate();
		}
		else if (getScrollY() > this.bottomViewHeight)
		{
			if (!this.isBottomWithOutScroll)
			{
				if (this.bottomView.getVisibility() == View.INVISIBLE)
				{
					this.mScroller.startScroll(0, getScrollY(), 0, this.bottomViewHeight - getScrollY(), 200);
				}
				else if (this.bottomView.getVisibility() == View.VISIBLE)
				{
					this.mScroller.startScroll(0, getScrollY(), 0, this.bottomViewHeight - getScrollY() + this.bottomViewHeight, 200);
				}
				this.scrollType = 1;
//				this.isScrollStoped = true;
				this.isFristTouch = false;
			}
			else
			{
				this.mScroller.startScroll(0, getScrollY(), 0, this.bottomViewHeight - getScrollY(), 200);
			}
			postInvalidate();
		}
		this.isMoveDown = false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent paramMotionEvent)
	{
		boolean bool = true;
		if (this.isFristTouch)
		{
			if (this.mListViewLoadListener != null)
			{
				this.isScrollToTop = this.mListViewLoadListener.getIsListViewToTop();
				this.isScrollFarTop = this.mListViewLoadListener.getIsListViewToBottom();
			}
			else
			{
				this.isScrollToTop = false;
				this.isScrollFarTop = false;
			}

			if (this.topViewInitializeVisibility == View.VISIBLE)
			{
				if (!this.isCloseTopAllowRefersh)
				{
					this.topView.setVisibility(View.VISIBLE);
				}
				else
				{
					this.topView.setVisibility(View.INVISIBLE);
				}
			}

			if (this.bottomViewInitializeVisibility == View.VISIBLE)
			{
				if (!this.isBottomWithOutScroll)
				{
					this.bottomView.setVisibility(View.VISIBLE);
				}
				else
				{
					this.bottomView.setVisibility(View.INVISIBLE);
				}
			}

			if (paramMotionEvent.getAction() != MotionEvent.ACTION_UP)
			{
				if (paramMotionEvent.getAction() != MotionEvent.ACTION_CANCEL)
				{
					if (!this.mGestureDetector.onTouchEvent(paramMotionEvent))
					{
						// 可能在进一步调到obtainView后出异常
						try
						{
							bool = super.dispatchTouchEvent(paramMotionEvent);
						}
						catch (Exception e)
						{
							LogUtil.printException(e);
						}
					}
					else
					{
						paramMotionEvent.setAction(MotionEvent.ACTION_CANCEL);
						try
						{
							bool = super.dispatchTouchEvent(paramMotionEvent);
						}
						catch (Exception e)
						{
							LogUtil.printException(e);
						}
					}
				}
				else
				{
					startScroll();
				}
			}
			else
			{
				mTouchListener.onTouch(this, paramMotionEvent);
//				InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//				inputManager.hideSoftInputFromWindow(getWindowToken(), 0);
				startScroll();
				try
				{
					bool = super.dispatchTouchEvent(paramMotionEvent);
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		}
		return bool;

	}

	@Override
	public void computeScroll()
	{
		super.computeScroll();
		if (!this.mScroller.computeScrollOffset())
		{
			if (this.isScrollToDownStoped)
			{
				this.isScrollToDownStoped = false;
				this.mHandler.sendEmptyMessageDelayed(0, timeInterval);
				isProgressBarShowed = true;
			}
		}
		else
		{
			scrollTo(this.mScroller.getCurrX(), this.mScroller.getCurrY());
			postInvalidate();
		}
		isFristTouch = this.mScroller.isFinished();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getAction())
		{
			case MotionEvent.ACTION_UP:
				if (getScrollY() - this.topViewHeight < 0)
				{
					this.isScrollToTop = true;
				}
				if (getScrollY() > this.bottomViewHeight)
				{
					this.isScrollFarTop = true;
				}
				startScroll();
		}
		return true;
	}
}
