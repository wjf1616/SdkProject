package com.chatsdk.model.viewholder;

import android.util.SparseArray;
import android.view.View;

public class SparseArrayViewHolder extends MessageViewHolder{
	
	public SparseArray<View> viewHolderSparseArray = null;
	
	public SparseArrayViewHolder()
	{
		viewHolderSparseArray = new SparseArray<View>();
	}
	
	@Override
	public View findViewById(View convertView,int id)
	{
		if(viewHolderSparseArray == null)
			return null;
		View childView = viewHolderSparseArray.get(id);
		if (childView == null)
		{
			childView = convertView.findViewById(id);
			viewHolderSparseArray.put(id, childView);
		}
		return childView;
	}

	protected void finalize()
	{
		viewHolderSparseArray.clear();
		viewHolderSparseArray = null;
	}
}
