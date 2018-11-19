package com.chatsdk.util.emoji;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class EmojiNoMoveViewPager extends ViewPager
{

	private boolean mDisableSroll = true;

    public EmojiNoMoveViewPager (Context context) {
        super(context);
    }

    public EmojiNoMoveViewPager (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDisableScroll(boolean bDisable)
    {
        mDisableSroll = bDisable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(mDisableSroll)
        {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mDisableSroll)
        {
            return false;
        }
        return super.onTouchEvent(ev);
    }
}
