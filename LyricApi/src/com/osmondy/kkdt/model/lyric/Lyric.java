package com.osmondy.kkdt.model.lyric;

import java.io.Serializable;
import java.util.List;


/**
 * 歌词信息
 * 
 * @author Osmondy
 * 
 */
public class Lyric implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 歌曲名称
	 */
	private String songname;
	
	/**
	 * 歌手名称
	 */
	private String singername;
	
	/**
	 * 歌词项目信息
	 */
	private List<LyricItem> lyricItems;
	
	/**
	 * 歌词总行数, 不包括无效歌词行.
	 */
	private int lines;
	
	/**
	 * 时长
	 */
	private long duration;
	
	public String getSongname()
	{
		return songname;
	}
	
	public void setSongname(String songname)
	{
		this.songname = songname;
	}
	
	public String getSingername()
	{
		return singername;
	}
	
	public void setSingername(String singername)
	{
		this.singername = singername;
	}
	
	public List<LyricItem> getLyricItems()
	{
		return lyricItems;
	}
	
	public void setLyricItems(List<LyricItem> lyricItems)
	{
		this.lyricItems = lyricItems;
	}
	
	public int getLines()
	{
		return lines;
	}
	
	public void setLines(int lines)
	{
		this.lines = lines;
	}
	
	public long getDuration()
	{
		return duration;
	}
	
	public void setDuration(long duration)
	{
		this.duration = duration;
	}
	
	public boolean isEmpty()
	{
		if (getLyricItems() == null || getLyricItems().isEmpty())
		{
			return true;
		}
		
		return false;
	}

	@Override
	public String toString()
	{
		return "[songname=" + songname + ", singername=" + singername + ", lines=" + lines + ", duration="
				+ duration + "]";
	}
	
}
