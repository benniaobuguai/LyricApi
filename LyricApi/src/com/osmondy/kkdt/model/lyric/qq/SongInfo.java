package com.osmondy.kkdt.model.lyric.qq;

/**
 * 歌曲信息</br>
 * 
 * <code>
 * <songinfo id="183767" scroll="1">
 * 	<seqnum>0</seqnum>
 * 	<name>
 * 	</name>
 * 	<singername>
 * 		<![CDATA[东来东往]]>
 * </singername>
 * </songinfo>
 * </code>
 * 
 * @author Osmondy
 * 
 */
public class SongInfo
{
	
	private String id;
	
	private int scroll;
	
	private int seqnum;
	
	private String name;
	
	private String singername;
	
	public String getId()
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public int getScroll()
	{
		return scroll;
	}
	
	public void setScroll(int scroll)
	{
		this.scroll = scroll;
	}
	
	public int getSeqnum()
	{
		return seqnum;
	}
	
	public void setSeqnum(int seqnum)
	{
		this.seqnum = seqnum;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getSingername()
	{
		return singername;
	}
	
	public void setSingername(String singername)
	{
		this.singername = singername;
	}

	@Override
	public String toString()
	{
		return "SongInfo [id=" + id + ", name=" + name + "]";
	}
	
}
