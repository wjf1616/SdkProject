package com.chatsdk.model.viewholder;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.view.View;

public class HashMapViewHolder extends MessageViewHolder{
	
	public HashMap<Integer,View> viewHolderHashMap = null;
	
	@SuppressLint("UseSparseArrays")
	public HashMapViewHolder()
	{
		viewHolderHashMap = new HashMap<Integer, View>();
	}
	
	@Override
	public View findViewById(View convertView,int id)
	{
		if(viewHolderHashMap == null)
			return null;
		View childView = viewHolderHashMap.get(Integer.valueOf(id));
		if (childView == null)
		{
			childView = convertView.findViewById(id);
			viewHolderHashMap.put(Integer.valueOf(id), childView);
		}
		return childView;
	}

	protected void finalize()
	{
		viewHolderHashMap.clear();
		viewHolderHashMap = null;
	}
}
