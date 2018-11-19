package com.chatsdk.view;

import org.apache.commons.lang.StringUtils;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.controller.ServiceInterface;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.util.CompatibleApiUtil;
import com.chatsdk.util.ImageUtil;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.ResUtil;
import com.chatsdk.view.actionbar.ActionBarFragment;

public class WriteMailFragment extends ActionBarFragment
{
	private ImageButton		sendMailButton;
	private Button          presidentMailButton;
	private ImageView		addPeopleButton;
	private EditText		recieverEditText;
	private EditText		contentEditText;
	private TextView        cs_textPromptLabel;
	private TextView        wordCountText;
	private TextView 		mailTipText;
	private LinearLayout	fragmentContentLayout;
	private RelativeLayout	relativeLayout2;
	private int				curMaxInputLength			= 500;

	private String			roomName	= "";
	private String			memberUids	= "";
	private String			memberNames	= "";
	private int          consumeGold = 0;
	private int          remainNum = 0;
	private boolean      isFromCpp = false;


	public WriteMailFragment()
	{
	}

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		((WriteMailActivity) getActivity()).fragment = this;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.activity = ((WriteMailActivity) getActivity());

		return inflater.inflate(ResUtil.getId(this, "layout", "cs__write_mail_fragment"), container, false);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (StringUtils.isNotEmpty(getWriteMailActivity().roomName))
			roomName = getWriteMailActivity().roomName;
		if (StringUtils.isNotEmpty(getWriteMailActivity().memberUids))
			memberUids = getWriteMailActivity().memberUids;
		if (StringUtils.isNotEmpty(getWriteMailActivity().memberNames))
		{
			memberNames = getWriteMailActivity().memberNames;
			recieverEditText.setText(memberNames);
		}

		if (getWriteMailActivity().isFromCpp)
		{
			consumeGold = getWriteMailActivity().consumeGold;
			remainNum = getWriteMailActivity().remainNum;
            isFromCpp = getWriteMailActivity().isFromCpp;
			cs_textPromptLabel.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_WRITEMAIL_REMAINNUM, String.valueOf(remainNum)));
			recieverEditText.setEnabled(false);
			recieverEditText.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_WRITEMAIL_PRESIDENT));
			if(consumeGold == 0){
				presidentMailButton.setText(LanguageManager.getLangByKey("81000197")); //免费
			}else{
				presidentMailButton.setText(String.valueOf(consumeGold));
			}
			relativeLayout2.setVisibility(View.VISIBLE);
			presidentMailButton.setVisibility(View.VISIBLE);
			sendMailButton.setVisibility(View.GONE);
			contentEditText.setHint(LanguageManager.getLangByKey("81000093")); //81000093 = 请输入邮件内容
			setMaxInputLength(isFromCpp);
			wordCountText.setVisibility(View.VISIBLE);
			mailTipText.setVisibility(View.VISIBLE);
			mailTipText.setText(LanguageManager.getLangByKey("81000199")); // 81000199 = 提示：禁止在总统邮件中发送资源买卖、种族歧视、政治倾向、带有侮辱性的信息；如有举报，我们将会进行严肃处理。
		}else{
			relativeLayout2.setVisibility(View.GONE);
			presidentMailButton.setVisibility(View.GONE);
			sendMailButton.setVisibility(View.VISIBLE);
			wordCountText.setVisibility(View.GONE);
			mailTipText.setVisibility(View.GONE);
	    }
	}

	private void setMaxInputLength(boolean fromCpp)
	{
		curMaxInputLength = fromCpp && ConfigManager.maxPersidentInputLength > 0 ? ConfigManager.maxPersidentInputLength : 500;
		contentEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(curMaxInputLength) });
	}

	private void refreshWordCount()
	{
		if (contentEditText == null || wordCountText == null)
			return;
		wordCountText.setText(contentEditText.getText().length() + "/" + curMaxInputLength);
	}

	public WriteMailActivity getWriteMailActivity()
	{
		return (WriteMailActivity) activity;
	}

	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		getTitleLabel().setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_MAIL));

		fragmentContentLayout = (LinearLayout) view.findViewById(R.id.fragmentContentLayout);
		relativeLayout2 = (RelativeLayout) view.findViewById(R.id.relativeLayout2);

		//ImageUtil.setYRepeatingBG(activity, fragmentContentLayout, R.drawable.mail_list_bg);
		//ImageUtil.setYRepeatingBG(activity, relativeLayout2, R.drawable.mail_list_bg);
		cs_textPromptLabel = (TextView) view.findViewById(R.id.cs_textPromptLabel);
		sendMailButton = (ImageButton) view.findViewById(R.id.sendMailButton);
		presidentMailButton = (Button) view.findViewById(R.id.presidentMailButton);
		addPeopleButton = (ImageView) view.findViewById(R.id.addPeopleButton);
		addPeopleButton.setVisibility(View.GONE);
		recieverEditText = (EditText) view.findViewById(R.id.recieverEditText);
		contentEditText = (EditText) view.findViewById(R.id.contentEditText);
		wordCountText = (TextView) view.findViewById(R.id.wordCountText);
		mailTipText= (TextView)view.findViewById(R.id.mailTipText);

		refreshSendButton();

		recieverEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				refreshSendButton();
			}
		});

		contentEditText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void afterTextChanged(Editable s)
			{
				contentEditText.post(new Runnable()
				{
					@Override
					public void run()
					{
						refreshWordCount();
					}
				});
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				refreshSendButton();
			}
		});

		addPeopleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ChatServiceController.isCreateChatRoom = true;
				ServiceInterface.showMemberSelectorActivity(activity, true);
			}
		});

		sendMailButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				sendMail();
			}
		});
		presidentMailButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				sendMail();
			}
		});
		fragmentLayout.setOnTouchListener(new OnTouchListener()
		{
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				hideSoftKeyBoard();
				return false;
			}
		});
	}

	private void refreshSendButton()
	{
		sendMailButton.setEnabled(canSend());
		presidentMailButton.setEnabled(canSend());
		if(canSend()==true){
		    sendMailButton.setBackgroundResource(R.drawable.btn_green3);
			presidentMailButton.setBackgroundResource(R.drawable.btn_green3);
		}else{
			sendMailButton.setBackgroundResource(R.drawable.button_gray);
			presidentMailButton.setBackgroundResource(R.drawable.button_gray);
		}
		//CompatibleApiUtil.getInstance().setButtonAlpha(sendMailButton, canSend());
	}
	
	private boolean canSend()
	{
		return recieverEditText.getText().length() != 0 && (isMultiReceiver() || contentEditText.getText().length() != 0);
	}
	
	private boolean isMultiReceiver()
	{
		if (StringUtils.isNotEmpty(memberUids))
		{
			String[] uidArr = memberUids.split("\\|");
			if (uidArr.length > 1)
				return true;
		}
		return false;
	}

	private void sendMail()
	{
		boolean isOnlyOneReceiver = !isMultiReceiver();

		int type = 0;//单人邮件的
		if(isFromCpp){
			type = MailManager.MAIL_PERSIDENT_SEND;
			if (remainNum <=0 ) { //总统发送邮件次数不足
				String tipStr = LanguageManager.getLangByKey("81000112");
				ServiceInterface.flyHint("","",tipStr,2,0,false);
				return;
			}

			if(UserManager.getInstance().getCurrentUser().gold < consumeGold){//钻石不足
				ChatServiceController.doHostAction("popUpCppWin", String.valueOf(type), "", "", false, false);
				return;
			}
		}
		
		if (memberUids.equals("") || isOnlyOneReceiver)
		{
			String content = contentEditText.getText().toString();
			String title = content;
			if (content.length() > 30)
				title = content.substring(0, 29);

			String allianceMailId = "";
			// 如果填自己的名字则发联盟邮件
			if (recieverEditText.getText().toString().equals(UserManager.getInstance().getCurrentUser().userName)
					&& StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUser().allianceId))
			{
				allianceMailId = UserManager.getInstance().getCurrentUser().allianceId;
			}

			JniController.getInstance().excuteJNIVoidMethod(
					"sendMailMsg",
					new Object[] {
							recieverEditText.getText().toString(),
							title,
							content,
							allianceMailId,
							"",
							Boolean.valueOf(false),
							type,
							"",
							"" });
			if (isOnlyOneReceiver && StringUtils.isNotEmpty(memberUids))
			{
				ServiceInterface.setMailInfo(memberUids, "", memberNames, MailManager.MAIL_USER);
				ServiceInterface.showChatActivity(ChatServiceController.getCurrentActivity(), DBDefinition.CHANNEL_TYPE_USER, false);
			}
		}
		else
		{
			// 写邮件界面的聊天室入口目前不会进入
			LogUtil.trackPageView("CreateChatRoom");
			JniController.getInstance().excuteJNIVoidMethod("createChatRoom",
					new Object[] { memberNames, memberUids, roomName, contentEditText.getText().toString() });
		}
		activity.exitActivity();
		ChatServiceController.isCreateChatRoom = false;
	}
}
