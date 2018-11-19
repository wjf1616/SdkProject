package com.chatsdk.model.viewholder;

import android.view.View;

@SuppressWarnings("unchecked")
public class ViewHolderHelper
{
	public static <T extends View> T get(View convertView, int id)
	{
		if (convertView == null)
			return null;
		int sdk = android.os.Build.VERSION.SDK_INT;
		
		View childView = null;
		MessageViewHolder viewHolder = null;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB)
		{
			viewHolder = (HashMapViewHolder) convertView.getTag();
			if(viewHolder == null)
			{
				viewHolder = new HashMapViewHolder();
				convertView.setTag(viewHolder);
			}
		}
		else
		{
			viewHolder = (SparseArrayViewHolder) convertView.getTag();
			if(viewHolder == null)
			{
				viewHolder = new SparseArrayViewHolder();
				convertView.setTag(viewHolder);
			}
		}
		if(viewHolder!=null)
			childView = viewHolder.findViewById(convertView, id);
		return (T) childView;
	}
	
	public static MessageViewHolder getViewHolder(View convertView)
	{
		if (convertView == null)
			return null;
		MessageViewHolder viewHolder = (MessageViewHolder)convertView.getTag();
		return viewHolder;
	}
}
