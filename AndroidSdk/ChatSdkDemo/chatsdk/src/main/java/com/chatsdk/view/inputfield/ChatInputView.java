package com.chatsdk.view.inputfield;

import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chatsdk.controller.JniController;
import com.chatsdk.util.CompatibleApiUtil;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.ResUtil;

public class ChatInputView extends RelativeLayout
{
	private Activity		activity;
	private Bundle			extras;
	private String			issueId;
	private String			chatLaunchSource;
	private Thread			pollerThread;
	private Handler			pollerThreadHandler;

	private EditText		replyField;
	private LinearLayout	messageBox;
	private LinearLayout	messageLinearLayout;
	private TextView		wordCount;
	private Button			addReply;

	private boolean			persistMessageBox;
	private boolean			newActivity;
	private Handler			fetchMessagesSuccess;
	private Handler			fetchMessagesFailure;
	private Handler			replyHandler;
	private Handler			replyFailHandler;

	public ChatInputView(Activity context)
	{
		super(context);
		this.activity = context;

		View.inflate(context, ResUtil.getId(context, "layout", "cs__chat_input_fragment"), this);

		this.persistMessageBox = false;

		onViewCreated();

		// 在小米pad上，点击输入框、弹出软键盘后，输入框消失了，输入三个回车之后才能显示
		// replyField.setOnTouchListener(new View.OnTouchListener() {
		// public boolean onTouch(View view, MotionEvent motionEvent) {
		// activity.runOnUiThread(new Runnable() {
		// public void run() {
		// // boolean textChanged = false;
		// // if(getInputText()==""){
		// // replyField.setText("\n\n\n",
		// TextView.BufferType.EDITABLE);//replyField.setText("\n\n\n");
		// // textChanged = true;
		// // }
		//
		// //在键盘打开的情况下点击时，不能隐藏
		// // replyField.setVisibility(View.INVISIBLE);
		// activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		// // if(textChanged){
		// // replyField.setText("");
		// // }
		// // replyField.setVisibility(View.VISIBLE);
		//
		// // simulateTouchOnEditText();
		//
		// //无效
		// // setVisibility(View.VISIBLE);
		//
		// //会导致点击无法使输入框显形（回车可以）
		// // postInvalidate();
		//
		// //无效
		// // invalidate();
		//
		// // printControls();
		// }
		// });
		//
		// return false;
		// }
		// });

		// 按back键，有软键盘时会触发此函数一次，关闭后会触发两次
		replyField.setOnKeyListener(new View.OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if (keyCode == KeyEvent.KEYCODE_BACK)
				{
					if (!isKeyboardOpen && getVisibility() == View.VISIBLE)
					{
						JniController.getInstance().excuteJNIVoidMethod("onBackPressed", null);
					}
				}

				return false;
			}
		});

		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			public void onGlobalLayout()
			{
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				{
					activity.runOnUiThread(new Runnable()
					{
						public void run()
						{
							try
							{
								adjustHeight();

								Rect r = new Rect();
								getWindowVisibleDisplayFrame(r);
								int screenHeight = getRootView().getHeight();
								// r.bottom is the position above soft keypad or
								// device button.
								// if keypad is shown, the r.bottom is smaller
								// than that before.
								int keypadHeight = screenHeight - r.bottom;
								if (keypadHeight > screenHeight * 0.15)
								{
									isKeyboardOpen = true;
									printEnabeld();
								}
								else
								{
									// 用back键关闭软键盘，触发此函数后，马上会接着触发onKey，所以延迟改变isKeyboardOpen，避免触发退出界面逻辑
									new java.util.Timer().schedule(new TimerTask()
									{
										public void run()
										{
											isKeyboardOpen = false;
											printEnabeld();
											this.cancel();
										}
									}, 50);
								}
							}
							catch (Exception e)
							{
							}
						}
					});
				}
			}
		});
	}

	private void printEnabeld()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "getVisibility()", getVisibility(), "replyField.isEnabled()",
				replyField.isEnabled());
	}

	private boolean	isKeyboardOpen	= false;

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void simulateTouchOnEditText()
	{
		// 无效
		long downTime = SystemClock.uptimeMillis();
		long eventTime = SystemClock.uptimeMillis() + 100;
		// List of meta states found here:
		// developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
		int metaState = 0;
		MotionEvent evt = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 10.0f, 10.0f, metaState);
		replyField.onTouchEvent(evt);

		// 无效
		// Show the IME, except when selecting in read-only text.
		final InputMethodManager imm = (InputMethodManager) this.activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			imm.viewClicked(replyField);
		}
		imm.showSoftInput(replyField, 0);
		// The above condition ensures that the mEditor is not null
		// mEditor.onTouchUpEvent(event);
	}

	private void printControls()
	{
		try
		{
			printChildren(this, "");
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}

	}

	private void printChildren(ViewGroup parent, String tab)
	{
		printChild(parent, tab);
		for (int i = 0; i < parent.getChildCount(); i++)
		{
			if (parent.getChildAt(i) instanceof ViewGroup)
			{
				printChildren((ViewGroup) parent.getChildAt(i), tab + "	");
			}
			else if (parent.getChildAt(i) instanceof View)
			{
				printChild(parent.getChildAt(i), tab);
			}
			else
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "type error: " + parent.getChildAt(i).getClass().toString());
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void printChild(View child, String tab)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "child.getId()", child.getId(), "visibility",
					child.getVisibility(), "alpha", child.getAlpha(), "rect",
					"[" + child.getX() + ", " + child.getY() + ", " + child.getWidth() + ", " + child.getHeight() + "]");
		}
	}

	public void onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
	}

	public String getInputText()
	{
		return replyField.getText().toString().trim();
	}

	private void adjustTextSize(TextView textView, double textRatio)
	{
		float newTextSize = (int) (textView.getTextSize() * textRatio);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
	}

	public void adjustHeight()
	{
		// S3手机上的尺寸
		int originalW = 173;
		int originalH = 84;

		// 资源尺寸
		double ratio = (double) originalH / (double) originalW;

		if (addReply.getWidth() != 0 && ratio != 0 && addReply.getHeight() != (int) (addReply.getWidth() * ratio))
		{
			addReply.setLayoutParams(new LinearLayout.LayoutParams(addReply.getWidth(), (int) (addReply.getWidth() * ratio)));

			double textRatio = (double) addReply.getWidth() / (double) originalW;
			// 在大屏上字体可能会偏大，可能需要用dp计算才行
			// 先加个修正因子
			textRatio = textRatio > ((double) 1 / 0.84390234277028) ? textRatio * 0.84390234277028 : textRatio;
			adjustTextSize(addReply, textRatio);
			adjustTextSize(replyField, textRatio);
			adjustTextSize(wordCount, textRatio);
		}
	}

	public void onViewCreated()
	{
		this.messageBox = ((LinearLayout) this.findViewById(ResUtil.getId(this.activity, "id", "cs__chat_input_relativeLayout")));
		this.replyField = ((EditText) this.findViewById(ResUtil.getId(this.activity, "id", "cs__messageText")));
		this.wordCount = ((TextView) this.findViewById(ResUtil.getId(this.activity, "id", "wordCountTextView")));
		addReply = (Button) this.findViewById(ResUtil.getId(this.activity, "id", "hs__sendMessageBtn"));

		refreshStatus();

		refreshButton();

		addReply.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				String replyText = getInputText();

				if (!TextUtils.isEmpty(replyText))
				{
					replyField.setText("");
					try
					{
						sendMessage(replyText);
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			}
		});
		this.replyField.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if (actionId == 4)
				{
					addReply.performClick();
				}
				return false;
			}
		});
		this.replyField.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable s)
			{
				replyField.post(new Runnable()
				{
					@Override
					public void run()
					{
						refreshWordCount();
					}
				});
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				persistMessageBox = true;
				refreshButton();
				activity.runOnUiThread(new Runnable()
				{
					public void run()
					{
						try
						{
							JniController.getInstance().excuteJNIVoidMethod("onTextChanged", new Object[] { getInputText() });
						}
						catch (Exception e)
						{
						}
					}
				});
			}
		});
	}

	private void refreshButton()
	{
		if (getInputText().length() == 0)
		{
			addReply.setEnabled(false);
			CompatibleApiUtil.getInstance().setButtonAlpha(addReply, false);
		}
		else
		{
			addReply.setEnabled(true);
			CompatibleApiUtil.getInstance().setButtonAlpha(addReply, true);
		}
	}

	private void sendMessage(String messageText)
	{
		JniController.getInstance().excuteJNIVoidMethod("sendMessage", new Object[] { messageText });
	}

	public LayoutParams getContainerViewLayoutParams()
	{
		return (LayoutParams) this.getLayoutParams();
	}

	public void setContainerViewLayoutParams(LayoutParams containerViewLayoutParams)
	{
		this.setLayoutParams(containerViewLayoutParams);
	}

	private void refreshStatus()
	{
		showMessageBox();
	}

	private void showMessageBox()
	{
		this.messageBox.setVisibility(View.VISIBLE);
		refreshWordCount();
	}

	private void showKeyboard(View v)
	{
		v.requestFocus();
		InputMethodManager imm = (InputMethodManager) this.activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(v, 0);
	}

	public void setSendButtonText(String text)
	{
		this.addReply.setText(text);
	}

	public void setEditTextHintText(String hint)
	{
		this.replyField.setHint(hint);
	}

	public void hideKeyboard()
	{
		InputMethodManager inputmanger = (InputMethodManager) this.activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputmanger.hideSoftInputFromWindow(this.activity.getCurrentFocus().getWindowToken(), 0);
	}

	public void setEnabled(boolean enabled)
	{
		this.replyField.setEnabled(enabled);
		this.addReply.setEnabled(enabled);
		if (!enabled)
		{
			CompatibleApiUtil.getInstance().setButtonAlpha(addReply, enabled);
		}
		else
		{
			refreshButton();
		}
		super.setEnabled(enabled);

	}

	private void refreshWordCount()
	{
		if (replyField.getLineCount() > 2)
		{
			wordCount.setVisibility(View.VISIBLE);
		}
		else
		{
			wordCount.setVisibility(View.GONE);
		}
		wordCount.setText(getInputText().length() + "/500");
	}
}
