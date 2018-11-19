package com.chatsdk.view.actionbar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.controller.ServiceInterface;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.FlyMutiRewardInfo;
import com.chatsdk.model.FlyRewardInfo;
import com.chatsdk.model.LanguageKeys;
import com.chatsdk.model.LanguageManager;
import com.chatsdk.model.MsgItem;
import com.chatsdk.model.UserInfo;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.util.ImageUtil;
import com.chatsdk.util.LogUtil;
import com.chatsdk.util.NetworkUtil;
import com.chatsdk.util.ResUtil;
import com.chatsdk.util.ScaleUtil;
import com.chatsdk.view.ChatActivity;
import com.chatsdk.view.ChatFragment;
import com.chatsdk.view.ICocos2dxScreenLockListener;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

//import com.chatsdk.view.ChannelListFragment;


public abstract class MyActionBarActivity extends FragmentActivity {
    public Timer sendTimer;
    public TimerTask sendTimerTask;
    public Button backButton;
    public TextView titleLabel;
    public Button optionButton;
    public Button editButton;
    public Button writeButton;
    public Button contactsButton;
    public Button returnButton;
    public Button returnGameUIButton;
//    public Button showFriend;
    public Button createChatRoomButton;
    public Button redPackageButton;
    public Button goLiveListButton;//点击进入直播
    public RelativeLayout actionbarLayout;
    protected int fragmentHolderId;
    public ActionBarFragment fragment;
    protected Bundle bundle;
    protected RelativeLayout fragmentLayout;
    protected RelativeLayout fontImg_fragment_layout;
    public FrameLayout fragment_holder;
    public ProgressBar activityProgressBar;
    public LinearLayout reward_loading_layout;
    //public TextView loading_textview;
    public FrameLayout reward_fly_layout;
    private LayoutInflater inflater;
    // 红包面板
    private FrameLayout red_package_root_layout;
    private TextView red_package_sendername;
    private TextView red_package_sendertip;
    // private TextView red_package_msg;
    private ImageView red_package_senderHeaderPic;
    private ImageView red_package_HeaderPicContainer;
    private TextView red_package_detail;
    private TextView red_package_warning;
    private LinearLayout red_package_warning_layout;
    private ImageView red_package_open_btn;
    private LinearLayout red_package_unhandlelayout;
    private View red_package_background_layout;
    private MsgItem redPackageItem;

    private ImageView           frame_eff_view;
    private ImageView			network_state_view;
    private FrameLayout			network_state_layout;
    private FrameLayout			frame_eff_layout;
    private AnimationDrawable	network_state_animation	= null;
    private AnimatorSet			networkErrorAnimSet;

    public Button			    red_package_Sensor_text;
    public Button				red_package_Sensor_btn;
    private FrameLayout		    red_package_Sensor_shark;
    private ImageView			red_package_Sensor_sharkimg;
    private AnimatorSet         frameEffAnimSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //处理系统字体缩放导致布局错乱情况，这里设置缩放比例始终保持为1
        Resources resource = getResources();
        Configuration configuration =resource.getConfiguration();
        configuration.fontScale = 1.0f;//设置字体的缩放比例
        resource.updateConfiguration(configuration , resource.getDisplayMetrics());
        super.onCreate(savedInstanceState);
        ChatServiceController.setCurrentActivity(this);
        ServiceInterface.pushActivity(this);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // actionBarHelper.onCreate(savedInstanceState);
        // actionBarHelper.setDisplayHomeAsUpEnabled(true);
        if (ConfigManager.getInstance().scaleFontandUI) {
            ConfigManager.calcScale(this);
        }
//        try{
//            setContentView(R.layout.cs__chat_activity);
//        } catch (Exception e) {
//            System.gc();
//            System.runFinalization();
//            System.gc();
//            setContentView(R.layout.cs__chat_activity);
//        }
        setContentView(R.layout.cs__chat_activity);
        getWindow().setBackgroundDrawable(null);
//
        backButton = (Button) findViewById(R.id.cs__actionbar_backButton);
        titleLabel = (TextView) findViewById(R.id.cs__actionbar_titleLabel);
        optionButton = (Button) findViewById(R.id.cs__actionbar_optionButton);
        editButton = (Button) findViewById(R.id.cs__actionbar_editButton);
        writeButton = (Button) findViewById(R.id.cs__actionbar_writeButton);
        contactsButton= (Button) findViewById(R.id.cs__actionbar_contactsButton);
        returnButton = (Button) findViewById(R.id.cs__actionbar_returnButton);
        returnGameUIButton = (Button)findViewById(R.id.cs__actionbar_returnGameUIButton);//add at 20171102 for returnGameUIButton
//        showFriend = (Button) findViewById(R.id.cs__actionbar_showFriendButton);
        createChatRoomButton = (Button) findViewById(R.id.cs__actionbar_createChatRoom);
        redPackageButton = (Button) findViewById(R.id.cs__actionbar_redPackageButton);
        goLiveListButton = (Button) findViewById(R.id.cs__actionbar_gotLiveListButton);
        actionbarLayout = (RelativeLayout) findViewById(R.id.cs__actionbar_layout);
        fragmentLayout = (RelativeLayout) findViewById(R.id.cs__activity_fragment_layout);
        fontImg_fragment_layout = (RelativeLayout) findViewById(R.id.fontImg_fragment_layout);


        network_state_view = (ImageView) findViewById(R.id.network_state_view);
        frame_eff_view = (ImageView) findViewById(R.id.frame_eff_view);
        network_state_layout = (FrameLayout) findViewById(R.id.network_state_layout);
        frame_eff_layout = (FrameLayout) findViewById(R.id.frame_eff_layout);

        red_package_Sensor_text = (Button) findViewById(R.id.red_package_sensor_text);
        red_package_Sensor_btn = (Button) findViewById(R.id.red_package_sensor_btn);
        red_package_Sensor_shark = (FrameLayout) findViewById(R.id.red_package_sensor_shark);
        red_package_Sensor_sharkimg = (ImageView) findViewById(R.id.red_package_sensor_sharkimg);

        red_package_Sensor_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                red_package_Sensor_shark.setVisibility(View.VISIBLE);
                showRedPackageShakeAnimation();
            }
        });


        if (red_package_root_layout != null)
            red_package_root_layout.setVisibility(View.GONE);
        redPackageItem = null;

        // 点击发送红包
        redPackageButton.setVisibility(View.GONE);
        redPackageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatServiceController.getInstance().setSendRedPackage();
            }
        });

        // 创建聊天室
        createChatRoomButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 购买vip特权才能看到创建按钮，创建的数量达到上限也不能在创建
                int vipPrivilege = JniController.getInstance().excuteJNIMethod("getCanCreateChatRoomNum", null);
                int createCount = ChannelManager.getInstance().gettingOwnerRoomCount();
                if(createCount>=vipPrivilege){
                    ServiceInterface.flyHint("", "", LanguageManager.getLangByKey("170919"), 0, 0, false);
                    return;
                }
                ChatServiceController.isCreateChatRoom = true;
                ServiceInterface.showMemberSelectorActivity(MyActionBarActivity.this, true);
            }
        });
        goLiveListButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatServiceController.getInstance().showLiveListView();
            }
        });
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackButtonClick();
            }
        });
        onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                adjustSize();
            }
        };
        actionbarLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

        this.fragmentHolderId = R.id.cs__activity_fragment_holder;
        fragment_holder = (FrameLayout) findViewById(fragmentHolderId);

        activityProgressBar = (ProgressBar) findViewById(R.id.cs__activity_progress_bar);
        hideProgressBar();

        reward_loading_layout = (LinearLayout) findViewById(R.id.reward_loading_layout);
        ViewHelper.setAlpha(reward_loading_layout, 0.4f);
        hideRewardLoadingPopup();

        //loading_textview = (TextView) findViewById(R.id.loading_textview);
        // loading_textview.setText(LanguageManager.getLangByKey(LanguageKeys.));

        reward_fly_layout = (FrameLayout) findViewById(R.id.reward_fly_layout);

        if (fragmentClass != null)
            showFragment(fragmentClass.getName());

        showBackground();
        ScaleUtil.initialize(this);
        this.hideSystemUI();

        startListenSysUIStatus();

    }


    //正对Out Of Memory ————————start————————————————————//
    private void releaseImageViews() {
        Log.d("MyActionBarActivity-releaseImageViews ", "releaseImageViews = " + "releaseImageViews");
        releaseImageView(red_package_senderHeaderPic);
        releaseImageView(red_package_HeaderPicContainer);
        releaseImageView(red_package_open_btn);
        releaseImageView(network_state_view);
        releaseImageView(frame_eff_view);
        releaseImageView(red_package_Sensor_sharkimg);

    }

    private void releaseImageView(ImageView imageView) {
        if(imageView == null) return;
        Drawable d = imageView.getDrawable();
        if (d != null)
            d.setCallback(null);
        imageView.setImageDrawable(null);
        imageView.setBackgroundDrawable(null);
    }
    //正对Out Of Memory ————————end————————————————————//

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startListenSysUIStatus() {
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
                    hideSystemUI();
                }
            }
        });
    }


    // 隐藏Navigation Bar mengchuining
    public void hideSystemUI() {
        try {
            if (!ChatServiceController.getInstance().host.getNativeGetIsShowStatusBar())
                return;
            if (Build.VERSION.SDK_INT >= 19) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }

            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }catch (Exception e){
            LogUtil.printException(e);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            this.hideSystemUI();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (intent == null) {
            intent = new Intent();
        }
        super.startActivityForResult(intent, requestCode);
    }

    /**
     * 在加载fragment前先在fragment容器上显示背景
     * <p/>
     * 以防止因为fragment初始化较慢，内容区出现黑屏
     */
    protected void showBackground() {
    }

    public void showRewardLoadingPopup() {
        reward_loading_layout.setVisibility(View.VISIBLE);
    }

    public void hideRewardLoadingPopup() {
        if (reward_loading_layout != null && reward_loading_layout.getVisibility() != View.GONE)
            reward_loading_layout.setVisibility(View.GONE);
    }

    private OnClickListener createOnClickLinstener(final String actionName, final MsgItem msgItem) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] redPackageInfoArr = msgItem.attachmentId.split("\\|");
                if (actionName.equals("pickRedPackage")) {
                    msgItem.handleRedPackageFinishState();
                    if (msgItem.sendState == MsgItem.FINISH) {
                        if (ChatServiceController.getChatFragment() != null)
                            ChatServiceController.getChatFragment().showRedPackageConfirm(msgItem);
                        // ServiceInterface.notifyDataSetChangedChatFragment();
                    } else {
                        JniController.getInstance().excuteJNIVoidMethod("getRedPackageStatus", new Object[]{redPackageInfoArr[0]});
                    }
                } else {
                    hideRedPackagePopup();
                    ChatServiceController.doHostAction(actionName, "", msgItem.msg, redPackageInfoArr[0], true);
                }
            }
        };
    }

    public MsgItem getRedPackagePopItem() {
        return redPackageItem;
    }

    private void showRedPackageShakeAnimation()
    {
        if (red_package_Sensor_sharkimg == null)
            return;
        final RotateAnimation rotate = new RotateAnimation(-15, 15, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.6f);
        rotate.setRepeatCount(3);
        rotate.setDuration(100);
        rotate.setRepeatMode(Animation.RESTART);

        final RotateAnimation rotate2 = new RotateAnimation(0, 0, Animation.RELATIVE_TO_SELF, 0.43f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate2.setDuration(2000);
        rotate2.setAnimationListener(new AnimationListener()
        {

            @Override
            public void onAnimationStart(Animation animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                rotate.reset();
                red_package_Sensor_sharkimg.setAnimation(rotate);
                rotate.startNow();
                rotate2.cancel();
                red_package_Sensor_shark.setVisibility(View.GONE);
            }
        });

        rotate.setAnimationListener(new AnimationListener()
        {

            @Override
            public void onAnimationStart(Animation animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                rotate2.reset();
                red_package_Sensor_sharkimg.setAnimation(rotate2);
                rotate2.startNow();
                rotate.cancel();
            }
        });

        red_package_Sensor_sharkimg.setAnimation(rotate);
        rotate.startNow();
    }



    private void showRedPackageBtnAnimation() {
        if (red_package_open_btn == null)
            return;
        final RotateAnimation rotate = new RotateAnimation(-15, 15, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.6f);
        rotate.setRepeatCount(3);
        rotate.setDuration(100);
        rotate.setRepeatMode(Animation.RESTART);

        final RotateAnimation rotate2 = new RotateAnimation(0, 0, Animation.RELATIVE_TO_SELF, 0.43f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate2.setDuration(2000);
        rotate2.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rotate.reset();
                red_package_open_btn.setAnimation(rotate);
                rotate.startNow();
                rotate2.cancel();
            }
        });

        rotate.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rotate2.reset();
                red_package_open_btn.setAnimation(rotate2);
                rotate2.startNow();
                rotate.cancel();
            }
        });

        red_package_open_btn.setAnimation(rotate);
        rotate.startNow();
    }

    private void setHeadImageLayoutParams() {
        int headWidthDP = 42;
        int headWidthDP2 = 39;
        int length1 = (int) (ScaleUtil.dip2px(this, headWidthDP) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
        int length2 = (int) (ScaleUtil.dip2px(this, headWidthDP2) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
        if (red_package_senderHeaderPic != null) {
            FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) red_package_senderHeaderPic.getLayoutParams();
            layoutParams2.width = length2;
            layoutParams2.height = length2;
            red_package_senderHeaderPic.setLayoutParams(layoutParams2);
        }

        if (red_package_HeaderPicContainer != null) {
            FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) red_package_HeaderPicContainer.getLayoutParams();
            layoutParams1.width = length1;
            layoutParams1.height = length1;
            red_package_HeaderPicContainer.setLayoutParams(layoutParams1);
        }

    }

    public void showRedPackagePopup(MsgItem msgItem) {
        // 节日红包开关开启
        if(ChatServiceController.isFestivalRedPackageEnable){
            LinearLayout red_package_bg = (LinearLayout) findViewById(R.id.bg);
            red_package_bg.setBackgroundResource(R.drawable.red_package_bg_festival);
        }

        if (red_package_root_layout == null)
            red_package_root_layout = (FrameLayout) findViewById(R.id.red_package_root_layout);
        if (red_package_background_layout == null)
            red_package_background_layout = findViewById(R.id.red_package_background_layout);
        if (red_package_sendername == null)
            red_package_sendername = (TextView) findViewById(R.id.red_package_sendername);
        if (red_package_sendertip == null)
            red_package_sendertip = (TextView) findViewById(R.id.red_package_sendertip);
        // red_package_msg = (TextView) findViewById(R.id.red_package_msg);
        if (red_package_senderHeaderPic == null)
            red_package_senderHeaderPic = (ImageView) findViewById(R.id.red_package_senderHeaderPic);
        if (red_package_HeaderPicContainer == null)
            red_package_HeaderPicContainer = (ImageView) findViewById(R.id.red_package_HeaderPicContainer);
        if (red_package_detail == null)
            red_package_detail = (TextView) findViewById(R.id.red_package_detail);
        if (red_package_warning == null)
            red_package_warning = (TextView) findViewById(R.id.red_package_warning);
        if (red_package_warning_layout == null)
            red_package_warning_layout = (LinearLayout) findViewById(R.id.red_package_warning_layout);
        if (red_package_open_btn == null)
            red_package_open_btn = (ImageView) findViewById(R.id.red_package_open_btn);
        if (red_package_unhandlelayout == null)
            red_package_unhandlelayout = (LinearLayout) findViewById(R.id.red_package_unhandlelayout);
        if (red_package_root_layout != null)
            red_package_root_layout.setVisibility(View.GONE);
        adjustRedPackageSize();

        if (red_package_background_layout != null) {
            red_package_background_layout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    hideRedPackagePopup();
                }
            });
            red_package_background_layout.setVisibility(View.VISIBLE);
        }

        if (red_package_root_layout != null && red_package_root_layout.getVisibility() != View.VISIBLE)
            red_package_root_layout.setVisibility(View.VISIBLE);
        if (red_package_root_layout != null) {
            ViewHelper.setAlpha(red_package_root_layout, 1.0f);
            ViewHelper.setScaleX(red_package_root_layout, 1.0f);
            ViewHelper.setScaleY(red_package_root_layout, 1.0f);
        }

        showRedPackageBtnAnimation();
        redPackageItem = msgItem;
        if (red_package_open_btn != null)
            red_package_open_btn.setOnClickListener(createOnClickLinstener("pickRedPackage", msgItem));
        if (red_package_detail != null)
            red_package_detail.setOnClickListener(createOnClickLinstener("viewRedPackage", msgItem));

        String headPic = msgItem.getHeadPic();
        String name = msgItem.getName();
        UserInfo user = msgItem.getUser();
        if(msgItem.post == 12) {
            user = null;
            String[] redPackageInfoArr = msgItem.attachmentId.split("\\|");
            if (redPackageInfoArr.length == 2) {
                if (redPackageInfoArr[1].equals("1")) {
                    headPic = "guide_player_icon";
                    name = LanguageManager.getLangByKey(LanguageKeys.TIP_SYSTEM_PLAYER_NAME);
                } else if (redPackageInfoArr[1].equals("2")) {
                    headPic = "festival_system_icon";
                    name = LanguageManager.getLangByKey(LanguageKeys.TIP_FESTIVAl_PLAYER_NAME);
                }
            }
        }

        if (red_package_senderHeaderPic != null){
            ImageUtil.setHeadImage(this, headPic, red_package_senderHeaderPic, user);
        }

        setHeadImageLayoutParams();

        if (red_package_sendername != null)
            red_package_sendername.setText(name);
        if (red_package_sendertip != null)
            red_package_sendertip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_SEND_RED_PACKAGE,
                    LanguageManager.getLangByKey(LanguageKeys.ITEM_RED_PACKAGE)));

        if (red_package_detail != null)
            red_package_detail.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RED_PACKAGE_DETAIL));

        if (red_package_unhandlelayout != null && red_package_warning_layout != null)
            if (msgItem.sendState == MsgItem.UNHANDLE) {
                red_package_unhandlelayout.setVisibility(View.VISIBLE);
                red_package_warning_layout.setVisibility(View.GONE);
                // 当红包未领取,不是自己发的红包不能看细节
                red_package_detail.setVisibility(msgItem.isSelfMsg() ? View.VISIBLE : View.GONE);
            } else if (msgItem.sendState == MsgItem.NONE_MONEY) {
                red_package_unhandlelayout.setVisibility(View.GONE);
                red_package_warning_layout.setVisibility(View.VISIBLE);
                if (red_package_warning != null)
                    red_package_warning.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RED_PACKAGE_NO_MONEY));
                red_package_detail.setVisibility(View.VISIBLE);
            } else if (msgItem.sendState == MsgItem.FINISH) {
                red_package_unhandlelayout.setVisibility(View.GONE);
                red_package_warning_layout.setVisibility(View.VISIBLE);
                if (red_package_warning != null) {
                    red_package_warning.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RED_PACKAGE_FINISH,
                            LanguageManager.getLangByKey(LanguageKeys.ITEM_RED_PACKAGE)));
                    // 当红包过期时,都不能看细节
                    red_package_detail.setVisibility(View.GONE);
                }
            }
    }

    public void hideRedPackagePopup() {
        Animator scaleX = ObjectAnimator.ofFloat(red_package_root_layout, "scaleX", 0);
        Animator scaleY = ObjectAnimator.ofFloat(red_package_root_layout, "scaleY", 0);
        Animator alpha = ObjectAnimator.ofFloat(red_package_root_layout, "alpha", 0);
        red_package_background_layout.setVisibility(View.GONE);
        AnimatorSet animator = new AnimatorSet();
        animator.playTogether(scaleX, scaleY, alpha);
        animator.setInterpolator(new AnticipateInterpolator(0.8f));
        animator.setDuration(300);
        animator.start();
        animator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (red_package_root_layout != null)
                    red_package_root_layout.setVisibility(View.GONE);
                redPackageItem = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });

    }

    public void showProgressBar() {
        activityProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        if (activityProgressBar != null && activityProgressBar.getVisibility() != View.GONE)
            activityProgressBar.setVisibility(View.GONE);
    }

    // ---------------------------------------------------
    // Chat Activity相关
    // ---------------------------------------------------

    protected Class<?> fragmentClass;

    public void showFragment(String className) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(this.fragmentHolderId, Fragment.instantiate(this, className, this.bundle));
        ft.commitAllowingStateLoss();
    }

    /**
     * 会先于fragment的onDestroy调用
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void onDestroy() {
        beforeExit();
        super.onDestroy();

        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            actionbarLayout.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }

        releaseImageViews();

        onGlobalLayoutListener = null;
        hideProgressBar();
        hideRewardLoadingPopup();

        if(backButton != null){
            backButton.setOnClickListener(null);
            backButton = null;
        }

        if(red_package_Sensor_btn != null){
            red_package_Sensor_btn.setOnClickListener(null);
            red_package_Sensor_btn = null;
        }

        if(redPackageButton != null){
            redPackageButton.setOnClickListener(null);
            redPackageButton = null;
        }

        if(goLiveListButton != null){
            goLiveListButton.setOnClickListener(null);
            goLiveListButton = null;
        }
        if(createChatRoomButton != null){
            createChatRoomButton.setOnClickListener(null);
            createChatRoomButton = null;
        }

        if(red_package_background_layout != null){
            red_package_background_layout.setOnClickListener(null);
            red_package_background_layout = null;
        }

        if(red_package_open_btn != null){
            red_package_open_btn.setOnClickListener(null);
            red_package_open_btn = null;
        }
        if(red_package_detail != null){
            red_package_detail.setOnClickListener(null);
            red_package_detail = null;
        }

        if(activityProgressBar != null){
            activityProgressBar.getIndeterminateDrawable().setCallback(null);
            activityProgressBar.setBackgroundResource(0);
            activityProgressBar.clearAnimation();
            activityProgressBar = null;
        }
        if(sendTimer !=null && sendTimerTask!=null){
            removeSendTimer();
        }
        titleLabel = null;
        optionButton = null;
        editButton = null;
        writeButton = null;
        contactsButton = null;
        returnButton = null;
        returnGameUIButton = null;
//        showFriend = null;
        actionbarLayout = null;
        fragment = null;
        bundle = null;
        fragmentLayout = null;
        fragment_holder = null;

        reward_loading_layout = null;
        //loading_textview = null;
        reward_fly_layout = null;
        red_package_root_layout = null;
        red_package_sendername = null;
        red_package_sendertip = null;
        red_package_senderHeaderPic = null;
        red_package_HeaderPicContainer = null;
        red_package_warning = null;
        red_package_warning_layout = null;
        red_package_unhandlelayout = null;
        redPackageItem = null;

        network_state_view = null;
        network_state_layout = null;
        frame_eff_layout = null;
        frame_eff_view = null;
        network_state_animation	= null;
        networkErrorAnimSet = null;

        red_package_Sensor_text = null;
        red_package_Sensor_shark = null;
        red_package_Sensor_sharkimg = null;
        fragmentClass = null;
        fontImg_fragment_layout = null;
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(null);

//        LinearLayout red_package_bg = (LinearLayout) findViewById(R.id.bg);
//        BitmapDrawable bd = (BitmapDrawable)red_package_bg.getBackground();
//        red_package_bg.setBackgroundResource(0);
//        bd.setCallback(null);
//        bd.getBitmap().recycle();
    }

    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;
    private boolean adjustSizeCompleted = false;
    private boolean adjustRedPackageSizeCompleted = false;

    protected void adjustSize() {
        if (!ConfigManager.getInstance().scaleFontandUI) {
            if (backButton.getWidth() != 0 && !adjustSizeCompleted) {
                adjustSizeCompleted = true;
            }
            return;
        }

        if (backButton.getWidth() != 0 && !adjustSizeCompleted) {
            actionbarLayout.setLayoutParams(new RelativeLayout.LayoutParams((int) actionbarLayout.getWidth(),
                    (int) (76 * ConfigManager.scaleRatioButton)));
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams((int) (76 * ConfigManager.scaleRatioButton),
                    (int) (76 * ConfigManager.scaleRatioButton));
            param.setMargins(ScaleUtil.dip2px(this, -4), 0, 0, 0);
            backButton.setLayoutParams(param);
            RelativeLayout.LayoutParams returnGameUIparam = new RelativeLayout.LayoutParams((int) (76 * ConfigManager.scaleRatioButton),
                    (int) (76 * ConfigManager.scaleRatioButton));
            param.setMargins(ScaleUtil.dip2px(this, -4), 0, 0, 0);
            returnGameUIparam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            returnGameUIparam.addRule(RelativeLayout.CENTER_VERTICAL);
            returnGameUIButton.setLayoutParams(returnGameUIparam);

            RelativeLayout.LayoutParams param2 = new RelativeLayout.LayoutParams((int) (80 * ConfigManager.scaleRatioButton),
                    (int) (80 * ConfigManager.scaleRatioButton));
            param2.setMargins(0, 0, ScaleUtil.dip2px(this, -4), 0);
            param2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            optionButton.setLayoutParams(param2);

            RelativeLayout.LayoutParams param3 = new RelativeLayout.LayoutParams((int) (54 * ConfigManager.scaleRatioButton),
                    (int) (49 * ConfigManager.scaleRatioButton));
            param3.setMargins(0, 0, 25, 0);
            param3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            param3.addRule(RelativeLayout.CENTER_VERTICAL);
            editButton.setLayoutParams(param3);
            returnButton.setLayoutParams(param3);
//            returnGameUIButton.setLayoutParams(param3);//add at 20171102 by returnGameUIButtonParam

            RelativeLayout.LayoutParams param4 = new RelativeLayout.LayoutParams((int) (54 * ConfigManager.scaleRatioButton),
                    (int) (49 * ConfigManager.scaleRatioButton));
            param4.setMargins(0, 0, 25, 0);
            param4.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            param4.addRule(RelativeLayout.CENTER_VERTICAL);
            writeButton.setLayoutParams(param4);

            RelativeLayout.LayoutParams param5 = new RelativeLayout.LayoutParams((int) (54 * ConfigManager.scaleRatioButton),
                    (int) (49 * ConfigManager.scaleRatioButton));
            param5.setMargins(0, 0, (int) (60 * ConfigManager.scaleRatioButton)+25, 0);
            param5.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            param5.addRule(RelativeLayout.CENTER_VERTICAL);
            contactsButton.setLayoutParams(param5);

//            RelativeLayout.LayoutParams param5 = new RelativeLayout.LayoutParams((int) (88 * ConfigManager.scaleRatioButton),
//                    (int) (88 * ConfigManager.scaleRatioButton));
//            param5.setMargins(0, 0, 0, 0);
//            param5.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            param5.addRule(RelativeLayout.CENTER_VERTICAL);
//            showFriend.setLayoutParams(param5);

            RelativeLayout.LayoutParams param6 = new RelativeLayout.LayoutParams((int) (88 * ConfigManager.scaleRatioButton),
                    (int) (80 * ConfigManager.scaleRatioButton));
            param6.setMargins(0, 0, 2, 0);
            param6.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            param6.addRule(RelativeLayout.CENTER_VERTICAL);
            // 节日红包开关开启
            if(ChatServiceController.isFestivalRedPackageEnable){
                redPackageButton.setBackgroundResource(R.drawable.red_package_icon_festival);
            }
            redPackageButton.setLayoutParams(param6);

            RelativeLayout.LayoutParams param7 = new RelativeLayout.LayoutParams((int) (63 * ConfigManager.scaleRatioButton),
                    (int) (80 * ConfigManager.scaleRatioButton));
            param7.setMargins(0, 0, (int) (90 * ConfigManager.scaleRatioButton), 0);
            param7.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            param7.addRule(RelativeLayout.CENTER_VERTICAL);
            red_package_Sensor_btn.setLayoutParams(param7);

            RelativeLayout.LayoutParams param8 = new RelativeLayout.LayoutParams((int) (26 * ConfigManager.scaleRatioButton),
                    (int) (26 * ConfigManager.scaleRatioButton));
            param8.setMargins(0, 0, (int) (92 * ConfigManager.scaleRatioButton), 0);
            param8.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            param8.addRule(RelativeLayout.ALIGN_TOP);
            red_package_Sensor_text.setLayoutParams(param8);

            final float scale = this.getResources().getDisplayMetrics().density;
            if(scale < 3){
                ScaleUtil.adjustTextSize(red_package_Sensor_text, ConfigManager.scaleRatioButton);
            }


            RelativeLayout.LayoutParams param10 = new RelativeLayout.LayoutParams((int) (88 * ConfigManager.scaleRatioButton),
                    (int) (88 * ConfigManager.scaleRatioButton));
            param10.setMargins(0, 0, 0, 0);
            param10.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            param10.addRule(RelativeLayout.CENTER_VERTICAL);
            createChatRoomButton.setLayoutParams(param10);
            createChatRoomButton.setVisibility(View.GONE);


            RelativeLayout.LayoutParams param11 = new RelativeLayout.LayoutParams((int) (46 * ConfigManager.scaleRatioButton),
                    (int) (54 * ConfigManager.scaleRatioButton));
            if(ChatServiceController.isAnchorHost) {
                param11.setMargins(0, 0, (int) (70 * ConfigManager.scaleRatioButton), 0);
            }else{
                param11.setMargins(0, 0, (int) (26 * ConfigManager.scaleRatioButton), 0);
            }
            param11.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            param11.addRule(RelativeLayout.CENTER_VERTICAL);
            goLiveListButton.setLayoutParams(param11);
            goLiveListButton.setVisibility(View.GONE);

            ScaleUtil.adjustTextSize(titleLabel, ConfigManager.scaleRatio);

            adjustSizeCompleted = true;
        }
    }

    protected void adjustRedPackageSize() {
        if (!ConfigManager.getInstance().scaleFontandUI || adjustRedPackageSizeCompleted)
            return;
        ScaleUtil.adjustTextSize(red_package_sendername, ConfigManager.scaleRatio);
        ScaleUtil.adjustTextSize(red_package_sendertip, ConfigManager.scaleRatio);
        // ScaleUtil.adjustTextSize(red_package_msg,
        // ConfigManager.scaleRatio);
        ScaleUtil.adjustTextSize(red_package_detail, ConfigManager.scaleRatio);
        ScaleUtil.adjustTextSize(red_package_warning, ConfigManager.scaleRatio);
        adjustRedPackageSizeCompleted = true;
    }

    /**
     * 高ppi手机的缩放修正因子
     */
    public double getScreenCorrectionFactor() {
        int density = getResources().getDisplayMetrics().densityDpi;

        if (density >= DisplayMetrics.DENSITY_XXHIGH) {
            // 小米note3是640，大于DENSITY_XXHIGH
            return 0.8;
        } else {
            return 1.0;
        }
    }

    public int getToastPosY() {
        int[] location = {0, 0};
        fragment_holder.getLocationOnScreen(location);
        return location[1] + ScaleUtil.dip2px(this, 5);
    }

    // ---------------------------------------------------
    // 锁屏超过1分钟，返回后自动退出
    // ---------------------------------------------------

    public static ICocos2dxScreenLockListener previousActivity;
    private long screenLockTime;
    /**
     * TODO 无用变量 是否是主动触发的退出（否则可能是锁屏）
     */
    protected boolean isExiting = false;
    private boolean isScreenLocked = false;

    @Override
    protected void onResume() {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "this", this);

        super.onResume();

        try {
            ChatServiceController.setCurrentActivity(this);
            if (isScreenLocked) {
                isScreenLocked = false;
                // 锁屏返回，超时，退出聊天界面
                // 仅调用2dx的onResume
                if ((System.currentTimeMillis() - screenLockTime) > (1000 * 62))
                {
                    ChatServiceController.isReturnFromScreenLock = true;
                    ChatServiceController.showGameActivity(ChatServiceController.getCurrentActivity());
                }
                // 锁屏返回，未超时，不退出聊天界面
                else
                {
                    if (previousActivity != null && (previousActivity instanceof ICocos2dxScreenLockListener))
                    {
                        previousActivity.handle2dxResume();
                    }
                }

            } else if (!isJumpToInnerActivity()) {
                if (previousActivity != null && (previousActivity instanceof ICocos2dxScreenLockListener)) {
                    LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "previousActivity.handle2dxResume()");
                    previousActivity.handle2dxResume();
                }
            }

            ChatServiceController.isNativeStarting = false;
            refreshNetWorkState();
            refreshFrameEffState();


//        if (ChatServiceController.getMainListFragment() != null )
//        {
//            ChatServiceController.getMainListFragment().reload();
//        }
//        ChannelListFragment.onChannelRefresh();

            //处理系统字体缩放导致布局错乱情况，这里设置缩放比例始终保持为1
            Resources resource = getResources();
            Configuration configuration =resource.getConfiguration();
            configuration.fontScale = 1.0f;//设置字体的缩放比例
            resource.updateConfiguration(configuration , resource.getDisplayMetrics());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isJumpToInnerActivity() {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "ChatServiceController.isNativeStarting",
                ChatServiceController.isNativeStarting, "ChatServiceController.isReturningToGame",
                (ChatServiceController.isNativeStarting || ChatServiceController.isReturningToGame));
        return ChatServiceController.isNativeStarting || ChatServiceController.isReturningToGame;
    }

    @Override
    protected void onPause() {
        // 当打开其它原生activity时，会出现并非锁屏的onPause，需要直接判断是否锁屏
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();

        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "this", this, "isScreenOn", isScreenOn, "previousActivity", previousActivity);

        super.onPause();
        removeSendTimer();
        if(ChatServiceController.isFromBd){
            ChatServiceController.isFromLiveExist = true;
        }else{
            ChatServiceController.isFromLiveExist = false;
        }
        if (!isScreenOn) // !isExiting &&
        {
            // 聊天界面锁屏
            isScreenLocked = true;
            ChatServiceController.isGoHomeOrLockScreen = true;
            screenLockTime = System.currentTimeMillis();
        }

        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "isJumpToInnerActivity_", isJumpToInnerActivity());
        // 仅调用2dx的onPause
        if (!isJumpToInnerActivity()) {
            if (previousActivity != null && (previousActivity instanceof ICocos2dxScreenLockListener)) {
                isScreenLocked = true;
                ChatServiceController.isGoHomeOrLockScreen = true;
                screenLockTime = System.currentTimeMillis();
                LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "previousActivity.handle2dxPause()");
                previousActivity.handle2dxPause();
            }
        }
        hideAllStateAnimation();
    }

    private void beforeExit() {
        if (fragment != null) {
            fragment.saveState();
        }

        isExiting = true;
        ServiceInterface.stopFlyHintTimer();
        // 极少情况fragment为null
        if (this instanceof ChatActivity && fragment != null && fragment instanceof ChatFragment) {
            ((ChatFragment) fragment).resetChannelView();
        }
    }

    public void onBackButtonClick() {
        exitActivity();
    }

    public void onBackPressed() {
        exitActivity();
    }

    public void exitActivity() {
        ServiceInterface.popActivity(this);
        if ( ServiceInterface.getNativeActivityCount() == 0 ) {
            if(ChatServiceController.isCurrentSecondList ) {
                ServiceInterface.showChannelListFrom2dx(false);
            }else {
                ChatServiceController.isReturningToGame = true;
            }
        } else {
            ChatServiceController.isCurrentSecondList = false;
            ChatServiceController.isNativeStarting = true;
        }

        try {
            // 从onResume()调用时，可能在FragmentManagerImpl.checkStateLoss()出异常
            // java.lang.RuntimeException Unable to resume activity
            // {com.hcg.cok.gp/com.chatsdk.view.ChatActivity}:
            // java.lang.IllegalStateException: Can not perform this action
            // after onSaveInstanceState
            super.onBackPressed();
        } catch (Exception e) {
            LogUtil.printException(e);
        }
        finish();
    }

    /**
     * 因退栈被销毁的话，不会被调用
     */
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    public void showFlyMutiReward(FlyMutiRewardInfo mutiRewardInfo) {
        reward_fly_layout.removeAllViews();
        reward_fly_layout.setVisibility(View.VISIBLE);
        if (mutiRewardInfo != null) {
            List<FlyRewardInfo> flyToolRewardArray = mutiRewardInfo.getFlyToolReward();
            createFlyNodeByType(flyToolRewardArray, 1);
            List<FlyRewardInfo> flyRewardArray = mutiRewardInfo.getFlyReward();
            createFlyNodeByType(flyRewardArray, 2);
            hideRewardLoadingPopup();
            createFlyAnimationForAllNode();
        }
    }

    public void createFlyAnimationForAllNode() {
        if (reward_fly_layout.getChildCount() <= 0)
            return;
        int flyToolRewardIndex = 0;
        int flyRewardIndex = 0;
        int totalFlyToolRewardNum = getChildNumByType("1");
        int totalFlyRewardNum = getChildNumByType("2");
        for (int i = 0; i < reward_fly_layout.getChildCount(); i++) {
            View view = reward_fly_layout.getChildAt(i);
            String tag = view.getTag().toString();
            if (tag.equals("1")) {
                createFlyToolAnimationForSingleNode(view, totalFlyToolRewardNum, flyToolRewardIndex, 0);
                flyToolRewardIndex++;
            } else if (tag.equals("2")) {
                createFlyToolAnimationForSingleNode(view, totalFlyRewardNum, flyRewardIndex, 1600);
                flyRewardIndex++;
            }
        }
    }

    private int getChildNumByType(String type) {
        int num = 0;
        for (int i = 0; i < reward_fly_layout.getChildCount(); i++) {
            View view = reward_fly_layout.getChildAt(i);
            String tag = view.getTag().toString();
            if (tag.equals(type))
                num++;
        }
        return num;
    }

    private Animator createflyOutAnimation(View view, int totalNum, int index, float centerY, long durationTime, long delayTime) {
        float scaleFactor = ((ScaleUtil.getScreenWidth() - ScaleUtil.dip2px(70)) / 4.0f - ScaleUtil.dip2px(20)) / ScaleUtil.dip2px(45);
        scaleFactor = scaleFactor > 2 ? 2 : scaleFactor;
        float cellW = ScaleUtil.dip2px(45) * scaleFactor + ScaleUtil.dip2px(20);
        float lineH = ScaleUtil.dip2px(60) * scaleFactor + ScaleUtil.dip2px(20);
        int lineCount = totalNum / 4 + 1;
        float delta = lineCount * lineH - ScaleUtil.getScreenHeight() / 2.0f;

        // LogUtil.printVariablesWithFuctionName("lineCount",lineCount,"scaleFactor",scaleFactor,"delta",delta,"ScaleUtil.getScreenHeight()",ScaleUtil.getScreenHeight());
        if (delta > 0) {
            centerY = -delta;
        } else {
            centerY = -ScaleUtil.dip2px(50);
        }

        int line = totalNum / 4 - index / 4;
        float endX = 0;
        if (line > 0) {
            endX = (index % 4 - 2) * cellW + cellW / 2;
        } else {
            if (totalNum % 4 == 1) {
                endX = 0;
            } else if (totalNum % 4 == 2) {
                endX = (index % 2 - 1) * cellW + cellW / 2;
            } else if (totalNum % 4 == 3) {
                endX = ((index % 3 == 0 ? 3 : index % 3) - 2) * cellW;
            } else {
                endX = (index % 4 - 2) * cellW + cellW / 2;
            }
        }
        AnimatorSet animSet = new AnimatorSet();
        Animator transAnimatorX = ObjectAnimator.ofFloat(view, "translationX", 0, endX);
        Animator transAnimatorY = ObjectAnimator.ofFloat(view, "translationY", centerY, -(line * lineH + centerY));

        if (delayTime > 0) {
            ViewHelper.setScaleX(view, 0);
            ViewHelper.setScaleY(view, 0);
            animSet.setStartDelay(delayTime);
        }

        Animator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0, scaleFactor);
        Animator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0, scaleFactor);
        animSet.playTogether(transAnimatorX, transAnimatorY, scaleX, scaleY);
        animSet.setDuration(durationTime);
        animSet.setInterpolator(new DecelerateInterpolator());
        return animSet;
    }

    private Animator createFlyToolAnimationForSingleNode(final View view, int totalNum, int index, int delayTime) {
        AnimatorSet animSet2 = new AnimatorSet();
        Animator transAnimatorX2 = ObjectAnimator.ofFloat(view, "translationX", 0);
        Animator transAnimatorY2 = ObjectAnimator.ofFloat(view, "translationY", ScaleUtil.getScreenHeight() / 2.0f);
        Animator scaleX2 = ObjectAnimator.ofFloat(view, "scaleX", 0);
        Animator scaleY2 = ObjectAnimator.ofFloat(view, "scaleY", 0);
        animSet2.playTogether(transAnimatorX2, transAnimatorY2, scaleX2, scaleY2);
        animSet2.setInterpolator(new AnticipateInterpolator(0.5f));
        animSet2.setDuration(600);
        animSet2.setStartDelay(400);

        AnimatorSet animator = new AnimatorSet();
        animator.playSequentially(createflyOutAnimation(view, totalNum, index, 0, 500, delayTime), animSet2);
        animator.start();
        animator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                reward_fly_layout.removeView(view);
                if (reward_fly_layout.getChildCount() <= 0)
                    hideFlyReward();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        return animator;
    }

    private void createFlyAnimationForSingleNode(final View view, int totalNum, int index) {
        Animator animator = createflyOutAnimation(view, totalNum, index, 100, 1200, 0);
        animator.start();
        animator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                reward_fly_layout.removeView(view);
                if (reward_fly_layout.getChildCount() <= 0)
                    hideFlyReward();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
    }

    public void createFlyNodeByType(List<FlyRewardInfo> flyRewardArray, int type) {
        if (flyRewardArray != null) {
            for (int i = 0; i < flyRewardArray.size(); i++) {
                FlyRewardInfo rewardInfo = flyRewardArray.get(i);
                createSingleRewardNode(rewardInfo, "" + type);
            }
        }
    }

    public void hideFlyReward() {
        reward_fly_layout.removeAllViews();
        reward_fly_layout.setVisibility(View.GONE);
    }

    private void createSingleRewardNode(FlyRewardInfo rewardInfo, String type) {
        if (rewardInfo == null)
            return;
        String iconName = rewardInfo.getItemPic();
        String pic = iconName.toLowerCase();
        if (StringUtils.isNotEmpty(pic) && pic.endsWith(".png")) {
            pic = pic.substring(0, pic.indexOf(".png"));
        }
        int num = rewardInfo.getItemNum();
        boolean isNotExistIcon = false;
        int picId = ResUtil.getId(this, "drawable", pic);
        if (picId == 0) {
            picId = R.drawable.no_iconflag;
            if (picId == 0)
                return;
            isNotExistIcon = true;
        }
        View view = inflater.inflate(R.layout.item_reward, null);
        if (view != null) {
            ImageView rewardImage = (ImageView) view.findViewById(R.id.reward_img);
            TextView rewardNum = (TextView) view.findViewById(R.id.reward_num);
            if (rewardImage != null) {
                rewardImage.setImageDrawable(getResources().getDrawable(picId));
                if (isNotExistIcon) {
                    rewardImage.setTag(iconName);
                    ImageUtil.setCommonImage(this, iconName, rewardImage);
                }
            }
            if (rewardNum != null) {
                rewardNum.setText("+" + num);
            }
            view.setTag(type);
            addViewOnParent(view);
        }
    }

    public void addViewOnParent(View view) {
        android.widget.FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        view.setLayoutParams(layoutParams);
        reward_fly_layout.addView(view);
    }

    public boolean isSoftKeyBoardVisibile = false;

    public void hideSoftKeyBoard() {
//		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//		if (inputManager != null && fragmentLayout != null && fragmentLayout.getWindowToken() != null)
//		{
//			inputManager.hideSoftInputFromWindow(fragmentLayout.getWindowToken(), 0);
//			isSoftKeyBoardVisibile = false;
//		}
    }

    public void showSoftKeyBoard(View view) {
//		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//		inputManager.showSoftInput(view, 0);
//		isSoftKeyBoardVisibile = true;
    }

    public void setSendBtnEnable(View view, boolean isEnable) {
//		view.setEnabled(isEnable);
//		CompatibleApiUtil.getInstance().setButtonAlpha(view, isEnable);
    }

    public void showNetworkConnectAnimation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(network_state_layout == null){
                    return;
                }
                hideMailStateAnimation();
                if (network_state_layout.getVisibility() != View.VISIBLE)
                    network_state_layout.setVisibility(View.VISIBLE);
                stopNetworkConnectAnimation();
                stopNetworkErrorAnimation();
                network_state_view.setImageResource(R.drawable.network_connect_anim);
                startNetworkConnectAnimation();
            }
        });
    }

    public void showNetwrokErrorAnimation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(network_state_layout == null){
                    return;
                }
                hideMailStateAnimation();
                if (network_state_layout.getVisibility() != View.VISIBLE)
                    network_state_layout.setVisibility(View.VISIBLE);
                stopNetworkConnectAnimation();
                stopNetworkErrorAnimation();
                network_state_view.setImageResource(R.drawable.network_error);
                startNetworkErrorAnimation();
            }
        });
    }
    //战斗红屏闪动
    public void showFrameEffAnimation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(frame_eff_layout == null){
                    return;
                }
                hideFrameEffAnimation();
                if (frame_eff_layout.getVisibility() != View.VISIBLE)
                    frame_eff_layout.setVisibility(View.VISIBLE);
                stopFrameEffAnimation();
                frame_eff_view.setImageResource(R.drawable.red_frame_test);
                startFrameEffAnimation();
            }
        });
    }

    public void startMailStateAnimation() {
//        System.out.println("startMailStateAnimation");
//        Drawable drawable = mail_pull1.getDrawable();
//        if (drawable != null && drawable instanceof AnimationDrawable) {
//            AnimationDrawable mail_state_2Animation = (AnimationDrawable) mail_pull1.getDrawable();
//            if (mail_state_2Animation != null && !mail_state_2Animation.isRunning())
//                mail_state_2Animation.start();
//        }
//        if (mail_state_anim2 == null)
//            mail_state_anim2 = AnimationUtils.loadAnimation(this, R.anim.mail_state_anim1);
//        else
//            mail_state_anim2.reset();
//        mail_pull2.startAnimation(mail_state_anim2);
    }

    //战斗红屏闪动开启
    public void startFrameEffAnimation(){
        if(frame_eff_view == null){
            return;
        }
        frame_eff_view.setVisibility(View.VISIBLE);
        ViewHelper.setAlpha(frame_eff_view, 0);
        frameEffAnimSet = new AnimatorSet();
        Animator showAnim = ObjectAnimator.ofFloat(frame_eff_view, "alpha", 0, 0.6f);
        showAnim.setDuration(1000);
        Animator delayAnimator = ObjectAnimator.ofFloat(frame_eff_view, "alpha", 0.6f, 0);
        delayAnimator.setDuration(1000);
        frameEffAnimSet.playSequentially(showAnim,delayAnimator);
        frameEffAnimSet.start();
        frameEffAnimSet.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animation.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animation.removeAllListeners();
            }
        });
    }
    //战斗红屏闪动结束
    public void stopFrameEffAnimation(){
        if (frameEffAnimSet != null)
            frameEffAnimSet.cancel();
    }

    public void hideFrameEffAnimation(){
        stopFrameEffAnimation();
        if (frame_eff_layout != null && frame_eff_layout.getVisibility() != View.GONE)
            frame_eff_layout.setVisibility(View.GONE);
    }
    public void startNetworkErrorAnimation() {
        if(network_state_view == null){
            return;
        }
        ViewHelper.setAlpha(network_state_view, 0);
        if (networkErrorAnimSet == null) {
            networkErrorAnimSet = new AnimatorSet();
            Animator showAnim = ObjectAnimator.ofFloat(network_state_view, "alpha", 0, 1.0f);
            showAnim.setDuration(500);
            Animator hideAnimator = ObjectAnimator.ofFloat(network_state_view, "alpha", 1.0f, 0);
            hideAnimator.setStartDelay(500);
            Animator delayAnimator = ObjectAnimator.ofFloat(network_state_view, "alpha", 0);
            delayAnimator.setDuration(300);
            networkErrorAnimSet.playSequentially(showAnim, hideAnimator, delayAnimator);
        }
        networkErrorAnimSet.start();
        networkErrorAnimSet.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animation.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animation.removeAllListeners();
            }
        });
    }

    public void startNetworkConnectAnimation() {
        if(network_state_view == null){
            return;
        }
        Drawable drawable = network_state_view.getDrawable();
        if (drawable != null && drawable instanceof AnimationDrawable) {
            network_state_animation = (AnimationDrawable) network_state_view.getDrawable();
            if (network_state_animation != null && !network_state_animation.isRunning())
                network_state_animation.start();
        }
    }

    public void stopNetworkErrorAnimation() {
        if (networkErrorAnimSet != null)
            networkErrorAnimSet.cancel();
    }

    public void stopNetworkConnectAnimation() {
        if(network_state_view == null){
            return;
        }
        Drawable drawable = network_state_view.getDrawable();
        if (drawable != null && drawable instanceof AnimationDrawable) {
            network_state_animation = (AnimationDrawable) network_state_view.getDrawable();
            if (network_state_animation != null && network_state_animation.isRunning())
                network_state_animation.stop();
        }
    }

    public void hideNetworkStateAnimation() {
        stopNetworkConnectAnimation();
        stopNetworkErrorAnimation();
        if (network_state_layout != null && network_state_layout.getVisibility() != View.GONE)
            network_state_layout.setVisibility(View.GONE);
    }

    public void hideAllStateAnimation() {
        hideNetworkStateAnimation();
        hideFrameEffAnimation(); //隐藏战斗红屏闪动
        hideMailStateAnimation();
    }

    public void showMailStateAnimation() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                hideNetworkStateAnimation();
//                if (mail_state_layout.getVisibility() != View.VISIBLE)
//                    mail_state_layout.setVisibility(View.VISIBLE);
//                mail_pull1.setImageResource(R.drawable.mail_pull_anim);
//                startMailStateAnimation();
//            }
//        });
    }

    public void stopMailStateAnimation() {
//        Drawable drawable = mail_pull1.getDrawable();
//        if (drawable != null && drawable instanceof AnimationDrawable) {
//            AnimationDrawable pull_anim1 = (AnimationDrawable) drawable;
//            if (pull_anim1 != null && pull_anim1.isRunning())
//                pull_anim1.stop();
//        }
//        if (mail_state_anim2 != null)
//            mail_state_anim2.cancel();
    }

    public void hideMailStateAnimation() {
//        stopMailStateAnimation();
//        if (mail_state_layout.getVisibility() != View.GONE)
//            mail_state_layout.setVisibility(View.GONE);
    }

    public void refreshFrameEffView(){
        if(ChatServiceController.isShowFrameEffNativeView){
            showFrameEffAnimation();
        }else{
            hideFrameEffAnimation();
        }
    }

    public void refreshFrameEffState(){
        refreshFrameEffView();
        final MyActionBarActivity c = ChatServiceController.getCurrentActivity();
            if (sendTimer == null)
            {
                sendTimer = new Timer();
            }
            if (sendTimerTask != null)
                return;

            sendTimerTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    int isShowFrameEff = 0;
                    Object returObj = null;
                    if(JniController.getInstance() != null) {
                        returObj = JniController.getInstance().excuteJNIMethod("getFrameState",new Object[]{});
                    }
                    if(returObj != null){
                        isShowFrameEff = (int)returObj;
                    }else{
                        return;
                    }
//                    isShowFrameEff = JniController.getInstance().excuteJNIMethod("getFrameState",new Object[]{});
//                    int isShowFrameEff = ChatServiceController.getInstance().host.getFrameState();
                    if(isShowFrameEff==0){
                        if(!ChatServiceController.isShowFrameEffNativeView)
                            return ;
                        ChatServiceController.isShowFrameEffNativeView = false;
                    }else if(isShowFrameEff==1){
                        if(ChatServiceController.isShowFrameEffNativeView)
                            return ;
                    ChatServiceController.isShowFrameEffNativeView = true;
                    }else if (isShowFrameEff == 2){
                        return ;
                    }
                    if(c == null){
                        return ;
                    }
                    c.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            refreshFrameEffView();
                        }
                    });
                }


            };

            sendTimer.schedule(sendTimerTask, 1000,1000);
    }

    public void stopSendTimer()
    {
        if (sendTimer != null)
        {
            if (sendTimerTask != null)
            {
                sendTimerTask.cancel();
            }
            sendTimer.cancel();
            sendTimer.purge();
        }
    }

    public void removeSendTimer()
    {
        stopSendTimer();
        sendTimer = null;
        sendTimerTask = null;
    }
    public void refreshNetWorkState()
    {
        if (!NetworkUtil.isNetworkAvailable() && ConfigManager.useWebSocketServer && ConfigManager.activityType == 0)
            showNetwrokErrorAnimation();
        else if (ConfigManager.isNetWorkConnecting() || ConfigManager.isWebSocketNetWorkConnecting())
            showNetworkConnectAnimation();
        else if (ConfigManager.mail_pull_state == ConfigManager.MAIL_PULLING
                && ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_OFFICIAL)
            showMailStateAnimation();
        else
            hideAllStateAnimation();
    }

    private int getUnHandleRedPackage(int channelType)
    {
        int num = 0;
        Map<String, MsgItem> map = ChannelManager.getInstance().getUnHandleRedPackageMap();
        if(map == null)return num;
        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            MsgItem msgItem = map.get(key);
            if(msgItem != null){
                String[] redPackageInfoArr = msgItem.attachmentId.split("\\|");
                if(redPackageInfoArr.length == 2
                        && channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE
                        && !msgItem.isGetSystemRedPackage())
                {
                    it.remove();
                }else if(msgItem.sendState == MsgItem.UNHANDLE){
                    if (msgItem.channelType == channelType && !msgItem.isRedPackageFinish()) {
                        num++;
                    }
                }
            }
        }

        return num;
    }

    public void refreshRedPackageNum(){
        if(red_package_Sensor_text != null && red_package_Sensor_btn != null) {
            int num = getUnHandleRedPackage(ChatServiceController.getCurrentChannelType());
            if (num == 0) {
                red_package_Sensor_text.setVisibility(View.GONE);
                red_package_Sensor_btn.setVisibility(View.GONE);
            } else {
                red_package_Sensor_text.setVisibility(View.VISIBLE);
                red_package_Sensor_btn.setVisibility(View.VISIBLE);
                String strNum = "";
                if (num > 9) {
                    strNum = "N";
                } else {
                    strNum = num + "";
                }
                red_package_Sensor_text.setText(strNum);
            }
        }
    }

}
