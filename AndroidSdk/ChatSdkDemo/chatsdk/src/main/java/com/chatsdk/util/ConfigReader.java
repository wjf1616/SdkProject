package com.chatsdk.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.chatsdk.model.LanguageItem;
 
/**
 * ini配置读取<p>
 * ini格式如下<p>
 * 
 * [section1]<p>
 * key1=value1<p>
 * 
 * [section2]<p>
 * key2=value2<p>
 * ...
 */
public class ConfigReader
{
	private Map<String, Map<String, List<String>>>	map				= null;
	private String									currentSection	= null;
	private ArrayList<LanguageItem>					items;

	public ConfigReader(String path)
	{
		init();

		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(path));
			read(reader);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException("IO Exception:" + e);
		}
	}

	public ConfigReader(InputStream in)
	{
		init();

		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			read(reader);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException("IO Exception:" + e);
		}
	}

	private void init()
	{
		map = new HashMap<String, Map<String, List<String>>>();
		items = new ArrayList<LanguageItem>();
		currentSection = "default";
		addSection(map, currentSection);
	}

	private void read(BufferedReader reader) throws IOException
	{
		String line = null;
		while ((line = reader.readLine()) != null)
		{
			parseLine(line);
		}
	}

	private void parseLine(String line)
	{
		line = line.trim();
		// comment
		if (line.matches("^\\#.*$"))
		{
			return;
		}
		else if (line.matches("^\\[\\S+\\]$"))
		{
			// section
			String section = line.replaceFirst("^\\[(\\S+)\\]$", "$1");
			addSection(map, section);
		}
		else if (line.matches("^\\S+=.*$"))
		{
			// key ,value
			int i = line.indexOf("=");
			String key = line.substring(0, i).trim();
			String value = line.substring(i + 1).trim();
			addKeyValue(map, currentSection, key, value);
		}
	}

	private void addKeyValue(Map<String, Map<String, List<String>>> map, String currentSection, String key, String value)
	{
		if (!map.containsKey(currentSection))
		{
			return;
		}

		Map<String, List<String>> childMap = map.get(currentSection);

		if (!childMap.containsKey(key))
		{
			List<String> list = new ArrayList<String>();
			list.add(value);
			childMap.put(key, list);

			items.add(new LanguageItem(key, value));
		}
		else
		{
			childMap.get(key).add(value);
		}
	}

	private void addSection(Map<String, Map<String, List<String>>> map, String section)
	{
		if (!map.containsKey(section))
		{
			currentSection = section;
			Map<String, List<String>> childMap = new HashMap<String, List<String>>();
			map.put(section, childMap);
		}
	}

	public List<String> getValue(String section, String key)
	{
		if (map.containsKey(section))
		{
			return getSection(section).containsKey(key) ? getSection(section).get(key) : null;
		}
		return null;
	}

	public Map<String, List<String>> getSection(String section)
	{
		return map.containsKey(section) ? map.get(section) : null;
	}

	public Map<String, List<String>> getAllSection()
	{
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		Iterator<?> sectionIter = map.entrySet().iterator();
		while (sectionIter.hasNext())
		{
			try
			{
				Map.Entry sectionEntry = (Map.Entry) sectionIter.next();
				Map<String, List<String>> section = (Map<String, List<String>>) sectionEntry.getValue();

				Iterator<?> kvIter = section.entrySet().iterator();
				while (kvIter.hasNext())
				{
					Map.Entry kvEntry = (Map.Entry) kvIter.next();
					String key = (String) kvEntry.getKey();
					List<String> val = (List<String>) kvEntry.getValue();
					result.put(key, val);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return result;
	}

	public ArrayList<LanguageItem> getAllItems()
	{
		return items;
	}
}