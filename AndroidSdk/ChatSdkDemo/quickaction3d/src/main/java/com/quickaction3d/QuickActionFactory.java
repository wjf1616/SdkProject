package com.quickaction3d;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;

public class QuickActionFactory {
	//action id
	public static final int ID_JOIN_ALLIANCE   = 1;
	public static final int ID_COPY     = 2;
	public static final int ID_SEND_MAIL = 3;
	public static final int ID_VIEW_PROFILE   = 4;
	public static final int ID_BAN  = 5;	

	private static Activity activity;
	
	public static QuickAction createQuickAction(final Activity activity, boolean hasLeague,int orientation)
	{
		QuickActionFactory.activity = activity;

        //orientation
		final QuickAction quickAction = new QuickAction(activity, orientation);
		
		if(hasLeague){
			ActionItem nextItem 	= new ActionItem(ID_JOIN_ALLIANCE, "加入");
	        quickAction.addActionItem(nextItem);
		}
		ActionItem prevItem 	= new ActionItem(ID_COPY, "复制");
        ActionItem searchItem 	= new ActionItem(ID_SEND_MAIL, "发送邮件");
        ActionItem infoItem 	= new ActionItem(ID_VIEW_PROFILE, "查看玩家");
        ActionItem eraseItem 	= new ActionItem(ID_BAN, "屏蔽");
		
		//add action items into QuickAction
		quickAction.addActionItem(prevItem);
        quickAction.addActionItem(searchItem);
        quickAction.addActionItem(infoItem);
        quickAction.addActionItem(eraseItem);

		//by clicking the area outside the dialog.
		quickAction.setOnDismissListener(new QuickAction.OnDismissListener() {			
			@Override
			public void onDismiss() {

			}
		});
		
		return quickAction;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void copyToClipboard(final Activity activity)
	{
		int sdk = android.os.Build.VERSION.SDK_INT;
		if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
		    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
		    clipboard.setText("text to clip");
		} else {
		    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE); 
		    android.content.ClipData clip = android.content.ClipData.newPlainText("text label","text to clip");
		    clipboard.setPrimaryClip(clip);
		}
	}
}
