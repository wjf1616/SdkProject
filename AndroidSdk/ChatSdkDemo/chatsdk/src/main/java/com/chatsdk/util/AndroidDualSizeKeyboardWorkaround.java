package com.chatsdk.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

public class AndroidDualSizeKeyboardWorkaround {

    public static void assistView (final View view, final Activity activity) {
        new AndroidDualSizeKeyboardWorkaround(view, activity);
    }
    
    private Activity activity;
    private View view;
    private int usableHeightPrevious;
	private int tmpHeight=0;

    private ViewGroup.LayoutParams layoutParams;
    private int firstDifference = 0;
    private int secondDifference = 0;
    private int previousHeightDifference = 0;
    
    private AndroidDualSizeKeyboardWorkaround(final View view, final Activity activity) {
    	this.view = view;
    	this.activity = activity;

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
	        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	        public void onGlobalLayout() {
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
					activity.runOnUiThread(new Runnable() {
						public void run() {
							try {
								possiblyResizeChildOfContent();
							} catch (Exception e) {
								// reportException(Main.getInstance(), e);
							}
						}
					});
				}
	        }
	    });
	    
        layoutParams = view.getLayoutParams();
    }
    
	private void possiblyResizeChildOfContent() {
    	//初始化、软键盘关闭1280 打开软键盘780，输入文字679 发出文字780
        int usableHeightNow = computeUsableHeight();
        
    	//初始化起一直是1280
        int usableHeightSansKeyboard = view.getRootView().getHeight();

        //不修改的话一直是-1
        
        //进入chatView之后就一直是1280
		final int actualHeight = view.getHeight();
		
		//view.Y一直为0
		//replyField.getY()初始化为0，进入chatView后一直是16
		
        //初始化、软键盘关闭0 打开软键盘500，输入文字601 发出文字500
        int heightDifference = usableHeightSansKeyboard - usableHeightNow;
        
        if (usableHeightNow != usableHeightPrevious) {
            //>320
            if (heightDifference > (usableHeightSansKeyboard/4)) {
	            if(firstDifference == 0){
	            	firstDifference = heightDifference;
	            }
	            if(secondDifference == 0 && firstDifference !=0 && firstDifference != heightDifference){
	            	secondDifference = heightDifference;
	            }
            	int newHeight = usableHeightSansKeyboard;
            	//能推下去，但是边缘紊乱的绘制区域依然还在，且盖住了输入框，无法选中
            	//对长按选中文字有影响（因为多了一个系统粘贴栏，影响了高度）
            	//而且会导致小米pad弹出一次键盘后输入框高度变窄
//            	if(firstDifference!=0 && secondDifference!=0 && secondDifference > firstDifference && heightDifference == firstDifference && previousHeightDifference > heightDifference)
//            	{
//            		newHeight += secondDifference - firstDifference;
//            	}
            	//这个workground是解决全屏下软键盘遮挡内容问题的，2dx中已经顶上去了（可能是2dx自己干的，或者某个我加的代码），就多减去了
            	//此时这个逻辑可以让软键盘尺寸调整时也调整内容区高度
//            	int newHeight = usableHeightSansKeyboard - heightDifference;
            	
//            	tmpHeight++;
//            	layoutParams.height = newHeight - tmpHeight;
//            	view.requestLayout();
            	
            	previousHeightDifference = heightDifference;
            } else {
//            	layoutParams.height = usableHeightSansKeyboard;
//            	view.requestLayout();
            }
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        //In effect, this tells you the available area where content can be placed and remain visible to users.
        view.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }

}