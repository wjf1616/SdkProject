package com.chatsdk.view.inputfield;

import org.json.JSONArray;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chatsdk.controller.JniController;
import com.chatsdk.util.CompatibleApiUtil;
import com.chatsdk.util.ResUtil;
import com.chatsdk.view.MessagesAdapter;

public class ChatInputFragment extends Fragment
{
	private final BroadcastReceiver	failedMessageRequestChecker;
	private final BroadcastReceiver	connChecker;
	private Activity				activity;
	private Bundle					extras;
	private String					issueId;
	private MessagesAdapter			adapter;
	private String					chatLaunchSource;
	private Thread					pollerThread;

	private EditText				replyField;
	private LinearLayout			messageBox;
	private TextView				wordCount;

	private boolean					persistMessageBox;
	private boolean					newActivity;
	private Handler					fetchMessagesSuccess;
	private Handler					fetchMessagesFailure;
	private Handler					replyHandler;
	private Handler					replyFailHandler;

	public ChatInputFragment()
	{
		this.failedMessageRequestChecker = new BroadcastReceiver()
		{
			public void onReceive(Context context, Intent intent)
			{
				refreshMessages();
			}
		};
		this.connChecker = new BroadcastReceiver()
		{
			public void onReceive(Context context, Intent intent)
			{
				startPoller();
			}
		};

		this.persistMessageBox = false;
		this.newActivity = true;

		this.replyHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				renderReplyMsg(msg);
			}
		};
		this.replyFailHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				super.handleMessage(msg);
				refreshMessages();
			}
		};
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.activity = ((Activity) getActivity());

		this.extras = getArguments();
		this.extras.remove("message");

		this.chatLaunchSource = this.extras.getString("chatLaunchSource");

		return inflater.inflate(ResUtil.getId(this, "layout", "cs__chat_input_fragment"), container, false);
	}

	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		this.messageBox = ((LinearLayout) view.findViewById(ResUtil.getId(this.activity, "id", "cs__chat_input_relativeLayout")));
		this.replyField = ((EditText) view.findViewById(ResUtil.getId(this.activity, "id", "cs__messageText")));
		this.wordCount = ((TextView) view.findViewById(ResUtil.getId(this.activity, "id", "wordCountTextView")));
		final Button addReply = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "hs__sendMessageBtn"));

		refreshStatus();

		if (this.replyField.getText().length() == 0)
		{
			addReply.setEnabled(false);
			CompatibleApiUtil.getInstance().setButtonAlpha(addReply, false);
		}
		else
		{
			addReply.setEnabled(true);
			CompatibleApiUtil.getInstance().setButtonAlpha(addReply, true);
		}

		addReply.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
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
				if (s.length() == 0)
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
		});
	}

	private void sendMessage(String messageText)
	{
		JniController.getInstance().excuteJNIVoidMethod("sendMessage", new Object[] { messageText });
	}

	private void renderReplyMsg(Message msg)
	{
	}

	private void refreshStatus()
	{
		showMessageBox();
	}

	private void showMessageBox()
	{
		this.messageBox.setVisibility(0);
		refreshWordCount();
	}

	private void showKeyboard(View v)
	{
		v.requestFocus();
		InputMethodManager imm = (InputMethodManager) this.activity.getSystemService("input_method");
		imm.showSoftInput(v, 0);
	}

	private void hideKeyboard(View v)
	{
		InputMethodManager imm = (InputMethodManager) this.activity.getSystemService("input_method");
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	private void refreshMessages()
	{
	}

	public void onPause()
	{
		super.onPause();
	}

	public void startPoller()
	{
	}

	public void onResume()
	{
		super.onResume();
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
		wordCount.setText(replyField.getText().length() + "/500");
	}

	public void onStart()
	{
		super.onStart();
	}

	public void onStop()
	{
		super.onStop();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent)
	{
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
	}
}