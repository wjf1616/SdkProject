package com.chatsdk.view.adapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chatsdk.R;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.ServiceInterface;
import com.chatsdk.image.AsyncImageLoader;
import com.chatsdk.image.ImageLoaderListener;
import com.chatsdk.model.ChannelListItem;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.UserInfo;
import com.chatsdk.model.UserManager;
import com.chatsdk.model.db.DBDefinition;
import com.chatsdk.model.db.DBHelper;
import com.chatsdk.util.BitmapUtil;
import com.chatsdk.util.CombineBitmapManager;
import com.chatsdk.util.ImageUtil;
import com.chatsdk.util.LogUtil;
import com.chatsdk.view.ChatActivity;
import com.chatsdk.view.ChatFragment;

public class ChatRoomChannelAdapter extends ChatRoomListAdapter
{
	private ConcurrentHashMap<String, Bitmap>	chatroomHeadImages;
	private int									customPicLoadingCnt;
	private boolean								chatroomHeadImagesLoading	= false;


	public ChatRoomChannelAdapter(ChatActivity context, ChatFragment fragment)
	{
		super(context, fragment);
		reloadData();
	}

	public boolean hasMoreData()
	{
		if (ServiceInterface.isHandlingGetNewMailMsg)
		{
			return false;
		}
		else
		{
			int count = ChannelManager.getInstance().getAllChatRoomChannel().size();

			return count > list.size();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		try
		{
			ChatChannel channel = (ChatChannel) getItem(position);
			if (channel == null)
				return null;

			channel.refreshRenderData();
//			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channelAdapter", "channelID nameText", channel.nameText,
//					"position", position);
			// channel.refreshRenderData();
			convertView = super.getView(position, convertView, parent);

			CategoryViewHolder holder = (CategoryViewHolder) convertView.getTag();
			int bgColor = 0;
			holder.setContent(context, channel, true, null, channel.nameText, channel.contentText, channel.getChannelTime(),
					false, position, bgColor,0);

			holder.item_icon.setTag(channel.channelID);
			setIcon(channel, holder.item_icon);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return convertView;
	}

	public void reloadData()
	{
		refreshAdapterList();
	}

	private synchronized void setIcon(final ChatChannel channel, final ImageView imageView)
	{
		if (channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			if (chatroomHeadImagesLoading)
				return;

			if (channel.memberUidArray == null || channel.memberUidArray.size() == 0)
			{
				imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mail_pic_flag_31));
				return;
			}

			String fileName = getChatroomHeadPicPath() + getChatroomHeadPicFile(channel.channelID);
			if (!channel.isMemberUidChanged)
			{
				if (AsyncImageLoader.getInstance().isCacheExistForKey(fileName))
				{
					Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(fileName);
					ImageUtil.setImageOnUiThread(context, imageView, bitmap);
					return;
				}
				else if (isChatroomHeadPicExist(channel.channelID))
				{
					AsyncImageLoader.getInstance().loadBitmapFromStore(fileName, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							String groupId = (String) imageView.getTag();
							if ((StringUtils.isNotEmpty(groupId) && !groupId.equals(channel.channelID)) || bitmap == null)
								return;
							ImageUtil.setImageOnUiThread(context, imageView, bitmap);
						}
					});
					return;
				}
			}

			chatroomHeadImages = new ConcurrentHashMap<String, Bitmap>();
			customPicLoadingCnt = 0;
			chatroomHeadImagesLoading = true;

			ArrayList<UserInfo> users = new ArrayList<UserInfo>();
			for (int i = 0; i < channel.memberUidArray.size(); i++)
			{
				UserInfo user = UserManager.getInstance().getUser(channel.memberUidArray.get(i));
				if (user != null)
				{
					users.add(user);
				}
				if (users.size() >= 9)
					break;
			}

			for (int i = 0; i < users.size(); i++)
			{
				final UserInfo user = users.get(i);

				Bitmap predefinedHeadImage = BitmapFactory.decodeResource(context.getResources(),
						ImageUtil.getHeadResId(context, user.headPic));
				// 少数情况可能为null，20 crashes 3 users
				if (predefinedHeadImage != null)
				{
					chatroomHeadImages.put(user.uid, predefinedHeadImage);
				}

				if (user.isCustomHeadImage())
				{
					customPicLoadingCnt++;
					ImageUtil.getCustomHeadImage(user, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(final Bitmap bitmap)
						{
							onCustomImageLoaded(channel, user.uid, bitmap, imageView);
						}
					});
				}
			}
			if (customPicLoadingCnt == 0)
			{
				generateCombinePic(channel, imageView);
			}
		}
		else
		{
			UserInfo user = null;
			if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
			{
				user = channel.channelShowUserInfo;
			}
			else if (channel.showItem != null)
			{
				user = channel.showItem.getUser();
			}
			if (user != null && StringUtils.isNotEmpty(user.uid))
				imageView.setTag(user.uid);
			ImageUtil.setHeadImage(context, channel.channelIcon, imageView, user);
		}
	}

	private synchronized void onCustomImageLoaded(ChatChannel channel, String uid, final Bitmap bitmap, ImageView imageView)
	{
		if(chatroomHeadImages == null)
			return;

		if (bitmap != null)
		{
			chatroomHeadImages.put(uid, bitmap);
		}
		customPicLoadingCnt--;
		if (customPicLoadingCnt == 0)
		{
			generateCombinePic(channel, imageView);
		}
	}

	private void generateCombinePic(ChatChannel channel, ImageView imageView)
	{
		chatroomHeadImagesLoading = false;

		ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
		Set<String> keySet = chatroomHeadImages.keySet();
		for (String key : keySet)
		{
			if (StringUtils.isNotEmpty(key) && chatroomHeadImages.get(key) != null)
			{
				bitmaps.add(chatroomHeadImages.get(key));
			}
		}

		Bitmap bitmap = CombineBitmapManager.getInstance().getCombinedBitmap(bitmaps);
		try
		{
			if (bitmap != null && StringUtils.isNotEmpty(channel.channelID) && getChatroomHeadPicPath() != null)
			{
				BitmapUtil.saveMyBitmap(bitmap, getChatroomHeadPicPath(), getChatroomHeadPicFile(channel.channelID));

				if (channel.isMemberUidChanged)
				{
					channel.isMemberUidChanged = false;
					String fileName = getChatroomHeadPicPath() + getChatroomHeadPicFile(channel.channelID);
					AsyncImageLoader.removeMemoryCache(fileName);
				}
			}
		}
		catch (IOException e)
		{
			LogUtil.printException(e);
		}

		String groupId = (String) imageView.getTag();
		if ((StringUtils.isNotEmpty(groupId) && !groupId.equals(channel.channelID)) || bitmap == null)
			return;
		ImageUtil.setImageOnUiThread(context, imageView, bitmap);
	}

	public String getChatroomHeadPicPath()
	{
		if (context == null)
			return null;

		return DBHelper.getHeadDirectoryPath(context) + "chatroom/";
	}

	public String getChatroomHeadPicFile(String channelId)
	{
		return channelId;
	}

	public String getOldChatroomHeadPicFile(String channelId)
	{
		return channelId + ".png";
	}

	public boolean isChatroomHeadPicExist(String channelId)
	{
		try
		{
			String fileName = getChatroomHeadPicPath() + getOldChatroomHeadPicFile(channelId);
			File oldfile = new File(fileName);
			if (oldfile.exists())
			{
				oldfile.delete();
			}

		}
		catch (Exception e)
		{
		}

		String fileName = getChatroomHeadPicPath() + getChatroomHeadPicFile(channelId);
		File file = new File(fileName);
		if (file.exists())
		{
			return true;
		}
		return false;
	}

	@Override
	public int getItemViewType(int position)
	{
		ChannelListItem item = getItem(position);
		if (item != null)
		{
			ChatChannel channel = (ChatChannel)item;
			if (channel.settings!=null && channel.settings.equals("1"))
				return VIEW_TYPE_UNTOP;
			else
				return VIEW_TYPE_TOP;
		}
		return VIEW_TYPE_TOP;
	}

	public void refreshAdapterList()
	{
		list.clear();
		List<ChatChannel> messageChannelArr = ChannelManager.getInstance().getAllChatRoomChannel();
		list.addAll(messageChannelArr);
		refreshOrder();
		fragment.setNoMailTipVisible(list.size() <= 0);
	}

	public void destroy()
	{
		super.destroy();
		if(chatroomHeadImages != null){
			chatroomHeadImages.clear();
			chatroomHeadImages = null;
		}
	}
}
