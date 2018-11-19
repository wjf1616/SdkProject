package com.chatsdk.image;

public class MemoryCache<K, V> extends InAbsCache<K, V>
{
	public MemoryCache(int cacheSize)
	{
		super(cacheSize);
	}

	@Override
	public void cache(K localUrl, V value)
	{
		cacheToMemory(localUrl, value);
	}

	@Override
	public V get(K key)
	{
		return getFromMemory(key);
	}

	@Override
	public boolean containsKey(K key)
	{
		return memoryCacheContainsKey(key);
	}

	@Override
	public void removeCache(K key)
	{
		removeMemoryCache(key);
	}
}
