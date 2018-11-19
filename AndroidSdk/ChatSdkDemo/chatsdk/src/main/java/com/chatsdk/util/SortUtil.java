package com.chatsdk.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.model.ChannelListItem;
import com.chatsdk.model.ChannelManager;
import com.chatsdk.model.ChatChannel;
import com.chatsdk.model.MailManager;
import com.chatsdk.model.mail.MailData;

public class SortUtil
{
	private static SortUtil	instance;

	public static SortUtil getInstance()
	{
		if (instance == null)
		{
			instance = new SortUtil();
		}
		return instance;
	}

	public <T> void refreshListOrder(List<T> target, Class<T> type)
	{
		switch (ChatServiceController.sortType)
		{
			case 0:
//				sortByPriority(target, type);
//				break;

			case 1:
				sortByTime(target, type);
				break;

			default:
				sortByTime(target, type);
				break;
		}
	}

	public <T> void sortByTime(List<T> target, Class<T> type)
	{
		List<ChannelListItem> sortedList = new ArrayList<ChannelListItem>();

		for (Iterator<T> iterator = target.iterator(); iterator.hasNext();)
		{
			T t = iterator.next();
			if (t instanceof ChannelListItem)
			{
				ChannelListItem item = (ChannelListItem) t;
				sortedList.add(item);
			}
		}

		Collections.sort(sortedList, new SortByTime());

		target.clear();
		addAll(target, type, sortedList);
	}

	private <T> void sortStudioMail(ArrayList<T> target, Class<T> type)
	{
		List<ChannelListItem> updateMailList = new ArrayList<ChannelListItem>();
		List<ChannelListItem> otherMailList = new ArrayList<ChannelListItem>();

		for (Iterator<T> iterator = target.iterator(); iterator.hasNext();)
		{
			T item = iterator.next();
			if (item != null && item instanceof MailData)
			{
				MailData mail = (MailData) item;
				if (mail.getType() == MailManager.MAIL_SYSUPDATE && mail.hasReward())
				{
					updateMailList.add((ChannelListItem) item);
				}
				else
				{
					otherMailList.add((ChannelListItem) item);
				}
			}
		}
		Collections.sort(updateMailList, new SortByTime());
		Collections.sort(otherMailList, new SortByTime());

		target.clear();
		addAll(target, type, updateMailList);
		addAll(target, type, otherMailList);
	}

	public <T> void sortByPriority(ArrayList<T> target, Class<T> type)
	{
		ArrayList<ChannelListItem> unreadRewardList = new ArrayList<ChannelListItem>();
		ArrayList<ChannelListItem> unreadList = new ArrayList<ChannelListItem>();
		ArrayList<ChannelListItem> readedRewardList = new ArrayList<ChannelListItem>();
		ArrayList<ChannelListItem> readedList = new ArrayList<ChannelListItem>();

		for (Iterator<T> iterator = target.iterator(); iterator.hasNext();)
		{
			T t = iterator.next();
			if (t instanceof ChannelListItem)
			{
				ChannelListItem item = (ChannelListItem) t;
				if (item.isUnread())
				{
					if (item.hasReward())
					{
						unreadRewardList.add(item);
					}
					else
					{
						unreadList.add(item);
					}
				}
				else
				{
					if (item.hasReward())
					{
						readedRewardList.add(item);
					}
					else
					{
						readedList.add(item);
					}
				}
			}
		}

		Collections.sort(unreadRewardList, new SortByTime());
		Collections.sort(unreadList, new SortByTime());
		Collections.sort(readedRewardList, new SortByTime());
		Collections.sort(readedList, new SortByTime());

		target.clear();
		addAll(target, type, unreadRewardList);
		addAll(target, type, unreadList);
		addAll(target, type, readedRewardList);
		addAll(target, type, readedList);
	}

	@SuppressWarnings("unchecked")
	private <T> void addAll(List<T> target, Class<T> type, List<ChannelListItem> childList)
	{
		try
		{
			for (ChannelListItem channelListItem : childList)
			{
				if (type.isInstance(channelListItem))
				{
					target.add((T) channelListItem);
				}
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void refreshNewMailListOrder(List<MailData> target)
	{
		Collections.sort(target, new SortByTime());
	}

	public <T> void refreshChatRoomListOrder(List<T> target, Class<T> type)
	{
		List<ChannelListItem> sortedList = new ArrayList<ChannelListItem>();
		List<ChannelListItem> delArenaArray = new ArrayList<ChannelListItem>();
		for (Iterator<T> iterator = target.iterator(); iterator.hasNext();)
		{
			T t = iterator.next();
			if (t instanceof ChatChannel)
			{
				final ChatChannel item = (ChatChannel) t;
				String channelUserUID = ((ChatChannel) t).channelID;
				String curArenaRoomId = ChatServiceController.curAreaRoomId;

				if(curArenaRoomId!=null && ChannelManager.getInstance().isArenaChatRoom(channelUserUID)&&!curArenaRoomId.equals(channelUserUID)){
					if(ChatServiceController.battlefield_chat_room&& delArenaArray.size() > 0){
						ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								ChannelManager.getInstance().deleteChannel(item);
							}
						});
					}
//					delArenaArray.add(item);
				}else{
					sortedList.add(item);
				}
			}
//            }

		}

		Collections.sort(sortedList, new SortBySetting());

		target.clear();
		addAll(target, type, sortedList);
	}

	private class SortBySetting implements Comparator<ChannelListItem>
	{
		@Override
		public int compare(ChannelListItem o1, ChannelListItem o2)
		{
			if(o1.getSetting()!=null && o1.getSetting().equals("1")) {
				return -1;
			}else if(o2.getSetting()!=null && o2.getSetting().equals("1")) {
				return 1;
			}else{
				if (o1.getChannelTime() < o2.getChannelTime()) {
					return 1;
				} else if (o1.getChannelTime() == o2.getChannelTime()) {
					return 0;
				}
			}

			return -1;
		}
	}

	private class SortByTime implements Comparator<ChannelListItem>
	{
		@Override
		public int compare(ChannelListItem o1, ChannelListItem o2)
		{
			if (o1.getChannelTime() < o2.getChannelTime())
			{
				return 1;
			}
			else if (o1.getChannelTime() == o2.getChannelTime())
			{
				return 0;
			}

			return -1;
		}
	}
}
