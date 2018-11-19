package com.chatsdk.controller.inputfield;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.util.LogUtil;
import com.chatsdk.view.inputfield.ChatInputView;

public class InputViewServiceInterface {
	private static ChatInputView chatInputView;
	
	public static void initChatInputView(Activity activity)
	{
		if(!ConfigManager.getInstance().enableChatInputField) return;
		
		try{
			getChatInputView(activity);
			activity.addContentView(chatInputView, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		} catch (Exception e) {
			LogUtil.printException(e);
		}
	}
	
	public static ChatInputView getChatInputView(Activity activity) {
		if(!ConfigManager.getInstance().enableChatInputField)
			return null;
		
		int visibility = View.GONE;
		if (chatInputView != null) {
			visibility = chatInputView.getVisibility();
			ViewGroup parent = (ViewGroup) chatInputView.getParent();
			parent.removeView(chatInputView);
		}
		
		chatInputView = new ChatInputView(activity);
		chatInputView.setVisibility(visibility);
		
		return chatInputView;
	}

	public static String getChatInputText(){
		if(!ConfigManager.getInstance().enableChatInputField)
			return "";
		
		if(chatInputView != null){
			try{
				return chatInputView.getInputText();
			}catch(Exception e)
			{
			    LogUtil.printException(e);
			}
		}
		return "";
	}
	
	public static void disableChatInputView(){
		if(!ConfigManager.getInstance().enableChatInputField) return;
		
		setChatInputViewEnabled(false);
	}
	
	public static void enableChatInputView(){
		if(!ConfigManager.getInstance().enableChatInputField) return;
		
		setChatInputViewEnabled(true);
	}

	private static void setChatInputViewEnabled(final boolean enabled){
		if(!ConfigManager.getInstance().enableChatInputField) return;
		
		ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	  		  try{
					if(chatInputView != null && chatInputView.getVisibility() == View.VISIBLE){
						chatInputView.setEnabled(enabled);
					}
			  }catch(Exception e)
			  {
				  LogUtil.printException(e);
			  }
	        }
	    });
	}
	
	public static void showChatInputView() {
		if(!ConfigManager.getInstance().enableChatInputField) return;
		
		if (chatInputView != null) {
			ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
				public void run() {
					try {
						chatInputView.setVisibility(View.VISIBLE);
					} catch (Exception e) {
						// reportException(Main.getInstance(), e);
					}
				}
			});
		}
	}

	public static void hideChatInputView() {
		if(!ConfigManager.getInstance().enableChatInputField) return;
		
		if (chatInputView != null) {
			ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
				public void run() {
					try {
						chatInputView.setVisibility(View.GONE);
						chatInputView.hideKeyboard();
					} catch (Exception e) {
						// reportException(Main.getInstance(), e);
					}
				}
			});
		}
	}

	public static void setSendButtonText(final String text) {
		if(!ConfigManager.getInstance().enableChatInputField) return;
		
		if (chatInputView != null) {
			ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
				public void run() {
					try {
						chatInputView.setSendButtonText(text);
					} catch (Exception e) {
						// reportException(Main.getInstance(), e);
					}
				}
			});
		}
	}

	public static void setEditTextHintText(final String hint) {
		if(!ConfigManager.getInstance().enableChatInputField) return;
		
		if (chatInputView != null) {
			ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
				public void run() {
					try {
						chatInputView.setEditTextHintText(hint);
					} catch (Exception e) {
						// reportException(Main.getInstance(), e);
					}
				}
			});
		}
	}

	private static boolean isVisiableForLast = false;
	private static ViewTreeObserver.OnGlobalLayoutListener m_onGlobalLayoutListener= new ViewTreeObserver.OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			Rect rect = new Rect();
			View decorView = ChatServiceController.hostActivity.getWindow().getDecorView();
			decorView.getWindowVisibleDisplayFrame(rect);
			//计算出可见屏幕的高度
			int displayHight = rect.bottom - rect.top;
			//获得屏幕整体的高度
			int height = decorView.getHeight();
			//获得键盘高度
			boolean virtualIsHide = JniController.getInstance().excuteJNIMethod("getNativeGetIsShowStatusBar",new Object[]{});

			int virtualheight = height - ChatServiceController.hostActivity.getWindowManager().getDefaultDisplay().getHeight();
			int keyboardHeight = 0;
			if(virtualIsHide){
				keyboardHeight = height-displayHight;
			}else{
				keyboardHeight = height-displayHight - virtualheight;
			}
			float rate = Math.abs((float) keyboardHeight / height);
			boolean visible =rate > 0.2;
			if(visible != isVisiableForLast){
				JniController.getInstance().excuteJNIVoidMethod("updatePopLayout", new Object[]{rate});
			}
			isVisiableForLast = visible;
		}
	};

	public static void addInputListener(){
		if (ChatServiceController.hostActivity != null) {
			ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
				public void run() {
					try {
						View decorView = ChatServiceController.hostActivity.getWindow().getDecorView();
						decorView.getViewTreeObserver().addOnGlobalLayoutListener(m_onGlobalLayoutListener);
					} catch (Exception e) {
						// reportException(Main.getInstance(), e);
					}
				}
			});
		}
	}

	public static void removeInputListener(){
		if (ChatServiceController.hostActivity != null) {
			ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
				@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
				public void run() {
					final View decorView = ChatServiceController.hostActivity.getWindow().getDecorView();
					decorView.getViewTreeObserver().removeOnGlobalLayoutListener(m_onGlobalLayoutListener);
				}
			});
		}
	}
}
