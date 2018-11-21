package com.quickaction3d;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

public class QuickActionFactory {
	//action id
	public static final int ID_JOIN_ALLIANCE   = 1;
	public static final int ID_COPY     = 2;
	public static final int ID_SEND_MAIL = 3;
	public static final int ID_VIEW_PROFILE   = 4;
	public static final int ID_BAN  = 5;	
//	public static final int ID_OK     = 6;
	
	private static Activity activity;
	
	public static QuickAction createQuickAction(final Activity activity, boolean hasLeague,int orientation)
	{
		QuickActionFactory.activity = activity;
		
		//create QuickAction. Use QuickAction.VERTICAL or QuickAction.HORIZONTAL param to define layout 
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
//        ActionItem okItem 		= new ActionItem(ID_OK, "OK");

//		ActionItem nextItem 	= new ActionItem(ID_DOWN, "Next", getResources().getDrawable(R.drawable.menu_down_arrow));
//		ActionItem prevItem 	= new ActionItem(ID_UP, "Prev", getResources().getDrawable(R.drawable.menu_up_arrow));
//        ActionItem searchItem 	= new ActionItem(ID_SEARCH, "Find", getResources().getDrawable(R.drawable.menu_search));
//        ActionItem infoItem 	= new ActionItem(ID_INFO, "Info", getResources().getDrawable(R.drawable.menu_info));
//        ActionItem eraseItem 	= new ActionItem(ID_ERASE, "Clear", getResources().getDrawable(R.drawable.menu_eraser));
//        ActionItem okItem 		= new ActionItem(ID_OK, "OK", getResources().getDrawable(R.drawable.menu_ok));
        
        //use setSticky(true) to disable QuickAction dialog being dismissed after an item is clicked
//        prevItem.setSticky(true);
//        nextItem.setSticky(true);
		
		//add action items into QuickAction
		quickAction.addActionItem(prevItem);
        quickAction.addActionItem(searchItem);
        quickAction.addActionItem(infoItem);
        quickAction.addActionItem(eraseItem);
//        quickAction.addActionItem(okItem);
        
        //Set listener for action item clicked
//		quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {			
//			@Override
//			public void onItemClick(QuickAction source, int pos, int actionId) {				
//				ActionItem actionItem = quickAction.getActionItem(pos);
//                 
//				//here we can filter which action item was clicked with pos or actionId parameter
//				if (actionId == ID_COPY) {
//					ToastCompat.makeText(activity.getApplicationContext(), "Let's do some search action", Toast.LENGTH_SHORT).show();
//				} else if (actionId == ID_SEND_MAIL) {
//					ToastCompat.makeText(activity.getApplicationContext(), "I have no info this time", Toast.LENGTH_SHORT).show();
//				} else {
//					ToastCompat.makeText(activity.getApplicationContext(), actionItem.getTitle() + " selected", Toast.LENGTH_SHORT).show();
//				}
//			}
//		});
		
		//set listnener for on dismiss event, this listener will be called only if QuickAction dialog was dismissed
		//by clicking the area outside the dialog.
		quickAction.setOnDismissListener(new QuickAction.OnDismissListener() {			
			@Override
			public void onDismiss() {
//				ToastCompat.makeText(activity.getApplicationContext(), "Dismissed", Toast.LENGTH_SHORT).show();
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
