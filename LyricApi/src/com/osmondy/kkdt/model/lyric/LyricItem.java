package com.osmondy.kkdt.model.lyric;

import java.io.Serializable;

/**
 * 歌词项目信息
 * 
 * @author Osmondy
 * 
 */
public class LyricItem implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 时间戳
	 */
	private int timestamp;
	
	/**
	 * 当前行歌词内容
	 */
	private String text;
	
	public LyricItem()
	{
		
	}
	
	public LyricItem(int timestamp, String text)
	{
		this.timestamp = timestamp;
		this.text = text;
	}
	
	public int getTimestamp()
	{
		return timestamp;
	}
	
	public void setTimestamp(int timestamp)
	{
		this.timestamp = timestamp;
	}
	
	public String getText()
	{
		return text;
	}
	
	public void setText(String text)
	{
		this.text = text;
	}
	
	@Override
	public String toString()
	{
		return "[timestamp=" + timestamp + ", text=" + text + "]";
	}
	
}
