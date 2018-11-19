package com.chatsdk.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.ConfigManager;
import com.chatsdk.model.UserInfo;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.db.ChatTable;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.viewholder.ViewHolderHelper;
import com.chatsdk.net.WebSocketManager;
import com.chatsdk.util.ImageUtil;
import com.chatsdk.util.RoundImageView;
import com.chatsdk.util.ScaleUtil;
import com.chatsdk.view.ChatRoomSettingActivity;
import com.chatsdk.view.MemberSelectorActivity;
import com.chatsdk.view.MemberSelectorFragment;
import com.chatsdk.view.actionbar.MyActionBarActivity;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.chatsdk.R.id.top_button;
import static com.mi.milink.sdk.base.Global.getApplicationContext;

/**
 * Created by with on 17/6/12.
 */

public class MemberGridAdapter extends BaseAdapter
{
    private MyActionBarActivity activity;
    public static MemberSelectorFragment fragment;
    private LayoutInflater inflater;
    private List<UserInfo> mDataList			= null;
    private static final int		ITEM_TYPE_NORMAL	= 0;
    private static final int		ITEM_TYPE_ADD		= 1;
    private static final int		ITEM_TYPE_DEL		= 2;
    private boolean					isDelState			= false;

    public MemberGridAdapter(MyActionBarActivity activity)
    {
        this.activity = activity;
        this.inflater = (LayoutInflater) (this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        refreshData();
    }

    public void destroy()
    {
        if(mDataList != null){
            mDataList.clear();
            mDataList = null;
        }
        activity = null;
        inflater = null;
    }

    public void refreshData()
    {
        //isDelState = false;
        if (mDataList == null)
            mDataList = new ArrayList<UserInfo>();
        else
            mDataList.clear();
        ArrayList<String> topMemUidArr= new ArrayList<String>();
        if(activity instanceof ChatRoomSettingActivity) {
            ChatChannel channel = ChannelManager.getInstance().getChannel(
                    ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, UserManager.getInstance().getCurrentMail().opponentUid));
            topMemUidArr = channel.memberUidArray;
        }else if(activity instanceof MemberSelectorActivity && fragment !=null){
            try{
                topMemUidArr = fragment.topMemberUidArray;
            }catch (Exception e){
            }
        }
        String founderUid = ChannelManager.getInstance()
                .getChatRoomFounderByKey(UserManager.getInstance().getCurrentMail().opponentUid);

        if (topMemUidArr != null && topMemUidArr.size() > 0)
        {
            for (String uid : topMemUidArr)
            {
                if (StringUtils.isNotEmpty(uid) && !uid.equals(founderUid))
                {
                    UserManager.checkUser(uid, "", 0);
                    UserInfo user = UserManager.getInstance().getUser(uid);
                    if (user != null)
                        mDataList.add(user);
                }
            }
        }
        if(activity instanceof ChatRoomSettingActivity) {
            // 将群主放在第一个位置
            if (StringUtils.isNotEmpty(founderUid)) {
                UserManager.checkUser(founderUid, "", 0);
                UserInfo user = UserManager.getInstance().getUser(founderUid);
                if (user != null)
                    mDataList.add(0, user);
            }

            if (mDataList.size() == 0 && UserManager.getInstance().getCurrentUser() != null)
                mDataList.add(UserManager.getInstance().getCurrentUser());

            UserInfo addUser = new UserInfo();
            addUser.btnType = ITEM_TYPE_ADD;
            mDataList.add(addUser);

            if (mDataList.size() > 2 && StringUtils.isNotEmpty(founderUid)
                    && StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUserId())
                    && UserManager.getInstance().getCurrentUserId().equals(founderUid)) {
                UserInfo delUser = new UserInfo();
                delUser.btnType = ITEM_TYPE_DEL;
                mDataList.add(delUser);
            }
        }
    }

    @Override
    public int getCount()
    {
        if (mDataList != null)
        {
            return mDataList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position)
    {
        if (mDataList != null && position >= 0 && position < mDataList.size())
            return mDataList.get(position);
        return null;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (position < 0 || position >= mDataList.size())
            return -1;
        UserInfo item = mDataList.get(position);
        if (item != null)
        {
            return item.btnType;
        }
        return -1;
    }

    @Override
    public int getViewTypeCount()
    {
        return 3;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    private void adjustTextSize(View convertView)
    {
        TextView name = ViewHolderHelper.get(convertView, R.id.name);
        if (name != null)
            ScaleUtil.adjustTextSize(name, ConfigManager.scaleRatio);
    }

    private void adjustSize(View convertView, int type)
    {
        if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
        {
            adjustTextSize(convertView);

            int length = (int) (ScaleUtil.dip2px(activity, 50) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
            if (type == ITEM_TYPE_NORMAL)
            {
                FrameLayout member_head_layout = ViewHolderHelper.get(convertView, R.id.member_head_layout);
                if (member_head_layout != null)
                {
                    LinearLayout.LayoutParams headImageLayoutParams = (LinearLayout.LayoutParams) member_head_layout.getLayoutParams();
                    headImageLayoutParams.width = length;
                    headImageLayoutParams.height = length;
                    member_head_layout.setLayoutParams(headImageLayoutParams);
                }

                int width = (int) (ScaleUtil.dip2px(activity, 20) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
                ImageView member_single_del_btn = ViewHolderHelper.get(convertView, R.id.member_single_del_btn);
                if (member_single_del_btn != null)
                {
                    FrameLayout.LayoutParams headImageLayoutParams = (FrameLayout.LayoutParams) member_single_del_btn.getLayoutParams();
                    headImageLayoutParams.width = width;
                    headImageLayoutParams.height = width;
                    member_single_del_btn.setLayoutParams(headImageLayoutParams);
                }
            }
            else if (type == ITEM_TYPE_ADD || type == ITEM_TYPE_DEL)
            {
                ImageView member_btn = ViewHolderHelper.get(convertView, R.id.member_btn);
                if (member_btn != null)
                {
                    LinearLayout.LayoutParams headImageLayoutParams = (LinearLayout.LayoutParams) member_btn.getLayoutParams();
                    headImageLayoutParams.width = length;
                    headImageLayoutParams.height = length;
                    member_btn.setLayoutParams(headImageLayoutParams);
                }
            }

        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final UserInfo userInfo = (UserInfo) getItem(position);
        if (userInfo == null)
            return convertView;

        int type = getItemViewType(position);
        if (convertView == null)
        {
            if (type == ITEM_TYPE_NORMAL)
                convertView = inflater.inflate(R.layout.item_chat_room_member, parent, false);
            else if (type == ITEM_TYPE_ADD || type == ITEM_TYPE_DEL)
                convertView = inflater.inflate(R.layout.item_chat_room_member_btn, parent, false);
            adjustSize(convertView, type);
        }

        if (type == ITEM_TYPE_NORMAL)
        {
            RoundImageView headImage = ViewHolderHelper.get(convertView, R.id.headImage);
            ImageUtil.setHeadImage(activity, userInfo.headPic, headImage, userInfo);

            ImageView member_single_del_btn = ViewHolderHelper.get(convertView, R.id.member_single_del_btn);
            if (isDelState && !userInfo.uid.equals(UserManager.getInstance().getCurrentUserId()))
                member_single_del_btn.setVisibility(View.VISIBLE);
            else
                member_single_del_btn.setVisibility(View.GONE);
            member_single_del_btn.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    if(ChatServiceController.getInstance().standalone_chat_room){
                        WebSocketManager.getInstance().chatRoomKick(UserManager.getInstance().getCurrentMail().opponentUid, userInfo.uid);
                    }else {
                        JniController.getInstance().excuteJNIVoidMethod("kickChatRoomMember",
                                new Object[]{UserManager.getInstance().getCurrentMail().opponentUid, userInfo.userName, userInfo.uid});
                    }
                }
            });

            TextView name = ViewHolderHelper.get(convertView, R.id.name);
            name.setText(userInfo.userName);
        }
        else if (type == ITEM_TYPE_ADD || type == ITEM_TYPE_DEL)
        {
            ImageView member_btn = ViewHolderHelper.get(convertView, R.id.member_btn);
            if (member_btn != null)
            {
                if (type == ITEM_TYPE_ADD)
                {
                    member_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.member_add));
                    member_btn.setOnClickListener(new View.OnClickListener()
                    {

                        @Override
                        public void onClick(View v)
                        {
                            isDelState = false;
                            if(activity instanceof ChatRoomSettingActivity)
                                ((ChatRoomSettingActivity)activity).showMemberSelectActivity();
                        }
                    });
                }
                else if (type == ITEM_TYPE_DEL)
                {
                    if(!isDelState)
                        member_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.member_del));
                    else
                        member_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.btn_comfirm));
                    member_btn.setOnClickListener(new View.OnClickListener()
                    {

                        @Override
                        public void onClick(View v)
                        {
                            isDelState = !isDelState;
                            notifyDataSetChanged();
                        }
                    });
                }
            }
        }

        return convertView;
    }

}
