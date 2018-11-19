package com.pullrefresh;

import com.pullrefresh.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 这个类封装了下拉加载更多的布局
 * 
 * @author Li Hong
 * @since 2013-7-30
 */
public class HeaderLoadingLayout2 extends LoadingLayout {
    /**进度条*/
    private ProgressBar mProgressBar;
    /** 显示的文本 */
    private TextView mHintView;
    private String loadingText="";
    private double loadingTextRatio;
    private boolean adjustSizeCompleted = false;
    
    /**
     * 构造方法
     * 
     * @param context context
     */
    public HeaderLoadingLayout2(Context context) {
        super(context);
        init(context);
    }

    /**
     * 构造方法
     * 
     * @param context context
     * @param attrs attrs
     */
    public HeaderLoadingLayout2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * 初始化
     * 
     * @param context context
     */
    private void init(Context context) {
        mProgressBar = (ProgressBar) findViewById(R.id.pull_to_load_footer_progressbar);
        mHintView = (TextView) findViewById(R.id.pull_to_load_footer_hint_textview);
        
        setState(State.RESET);
    }
    
    @Override
    public void setLoadingText(String text)
    {
    	loadingText=text;
    }
    
    public void setLoadingTextSizeRatio(double textRatio)
    {
    	loadingTextRatio = textRatio;
    }
    
    @Override
    protected View createLoadingView(Context context, AttributeSet attrs) {
        View container = LayoutInflater.from(context).inflate(R.layout.pull_to_load_footer, null);
        return container;
    }

    @Override
    public void setLastUpdatedLabel(CharSequence label) {
    }

    @Override
    public int getContentSize() {
        View view = findViewById(R.id.pull_to_load_footer_content);
        if (null != view) {
            return view.getHeight();
        }
        
        return (int) (getResources().getDisplayMetrics().density * 40);
    }
    
    @Override
    protected void onStateChanged(State curState, State oldState) {
        mProgressBar.setVisibility(View.GONE);
        mHintView.setVisibility(View.INVISIBLE);
        
        super.onStateChanged(curState, oldState);
    }
    
    @Override
    protected void onReset() {
        mHintView.setText(loadingText);
    }

    @Override
    protected void onPullToRefresh() {
//        mHintView.setVisibility(View.VISIBLE);
//        mHintView.setText(R.string.pull_to_refresh_header_hint_normal2);
    }

    @Override
    protected void onReleaseToRefresh() {
//        mHintView.setVisibility(View.VISIBLE);
//        mHintView.setText(R.string.pull_to_refresh_header_hint_ready);
    }

    @Override
    protected void onRefreshing() {
    	if(loadingText.equals(""))
    	{
    		mProgressBar.setVisibility(View.GONE);
            mHintView.setVisibility(View.INVISIBLE);
    	}
    	else
    	{
    		mProgressBar.setVisibility(View.VISIBLE);
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(loadingText);
    	}
        
        if(!adjustSizeCompleted){
			float newTextSize = (int) (mHintView.getTextSize() * loadingTextRatio);
			mHintView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
			adjustSizeCompleted = true;
        }
    }
    
    @Override
    protected void onNoMoreData() {
        mHintView.setVisibility(View.VISIBLE);
        mHintView.setText(R.string.pushmsg_center_no_more_msg);
    }
}
