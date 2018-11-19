package com.chatsdk.image;

import java.util.LinkedHashMap;

@SuppressWarnings("serial")
public class LRUMap<K, V> extends LinkedHashMap<K, V>
{
	private int	mCapacity;

	public LRUMap(int initialCapacity)
	{
		super(initialCapacity, 0.75F, true);
		mCapacity = initialCapacity;
	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest)
	{
		return size() > mCapacity;
	}
}
