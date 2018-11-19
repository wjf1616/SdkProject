package com.chatsdk.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.chatsdk.controller.ChatServiceController;

public class HtmlTextUtil
{
	public static void setResourceHtmlText(TextView textView, String str) 
	{
		// 将html特殊符号进行转义，否则"<"后面的内容会被Html.fromHtml吞掉
		str = TextUtils.htmlEncode(str);
		Spanned spannedText = Html.fromHtml(str);
		textView.setText(spannedText);
		textView.setMovementMethod(LinkMovementMethod.getInstance());

		if (!(textView.getText() instanceof Spannable)) return;

		int textLineHeight = measureTextSingleLineHeight(textView);
		
		SpannableStringBuilder style = new SpannableStringBuilder(textView.getText());
		style.clearSpans();
		
		String copy = str.toString();
        Pattern pattern = Pattern.compile("\\[[^\\]]+\\]");
        Matcher matcher = pattern.matcher(copy);
        while(matcher.find()){
        	String matchStr = matcher.group();
            copy = matcher.replaceFirst("");
        	
            int start = style.toString().indexOf(matchStr);
    		style.replace(start, start + matchStr.length(), "");
    		int id = ResUtil.getId(ChatServiceController.hostActivity, "drawable", trimResourceIcon(matchStr));
    		if(id != 0){
    			style.insert(start, Html.fromHtml("<img src='" + id + "'/>", getResourceIconImageGetter(id, textLineHeight), null));
    		}else{
    			style.insert(start, trimResourceIcon(matchStr));
    		}
        	matcher.reset(copy);
        }

        textView.setFocusable(false);
        textView.setEnabled(false);
        textView.setClickable(false);
        textView.setLongClickable(false);
		textView.setText(style);
	}
	
	private static ImageGetter getResourceIconImageGetter(final int id, final int textLineHeight)
	{
		ImageGetter result = new ImageGetter()
		{
			@Override
			public Drawable getDrawable(String source)
			{
				Drawable d = ChatServiceController.hostActivity.getResources().getDrawable(id);
				d.setBounds(0, 0, (int) (textLineHeight*0.8), (int)(textLineHeight*0.8));
				return d;
			}
		};
		return result;
	}
	
	/**
	 * [ui_silver.png] 转化为 ui_silver
	 */
	private static String trimResourceIcon(String image)
	{
		String result = image.replaceAll("[\\[\\]]", "");
		result = result.replace(".png", "");
		return result;
	}

	/**
	 * 高ppi手机的缩放修正因子
	 */
	public static double getScreenFactor()
	{
		int density = ChatServiceController.hostActivity.getResources().getDisplayMetrics().densityDpi;

		return density == DisplayMetrics.DENSITY_XXHIGH ? 0.8 : 1.0;
	}
	
	/**
	 * 同样条件下，与measureTextSingleLineHeight返回结果一样
	 */
	public static int measureTextHeight(Context context, String text, int textSize, int deviceWidth) {
	    TextView textView = new TextView(context);
	    textView.setText(text);
	    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
	    int widthMeasureSpec = MeasureSpec.makeMeasureSpec(deviceWidth, MeasureSpec.AT_MOST);
	    int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
	    textView.measure(widthMeasureSpec, heightMeasureSpec);
	    return textView.getMeasuredHeight();
	}
	
	public static int measureTextSingleLineHeight(TextView textView) {
		CharSequence originalText = textView.getText();
	    textView.setText("A");
	    int widthSpec = MeasureSpec.makeMeasureSpec(LayoutParams.MATCH_PARENT, MeasureSpec.EXACTLY);
	    int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
	    textView.measure(widthSpec, heightSpec);
	    textView.setText(originalText);
	    return textView.getMeasuredHeight();
	}
	
	public static int measureTextHeight(TextView textView, String text, int width){
		CharSequence originalText = textView.getText();
	    textView.setText(text);
		int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
	    int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
	    textView.measure(widthSpec, heightSpec);
	    textView.setText(originalText);
	    return textView.getMeasuredHeight();
	}
}
