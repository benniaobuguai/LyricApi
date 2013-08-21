package com.osmondy.kkdt.model.lyric.baidu;

/**
 * 歌曲信息</br>
 * <url>
 * <encode>
 * <![CDATA[http://zhangmenshiting.baidu.com/data2/music/4722177/$$]]>
 * </encode>
 * <decode>
 * <![CDATA[4722177.mp3?xcode=f5d77352f60aaa5a1b34f5ea0f19069d48cc280362890cc2
 * &mid=0.33352287572173]]>
 * </decode>
 * <type> 1 </type>
 * <lrcid> 38284 </lrcid>
 * <flag> 0 </flag>
 * </url>
 * 
 * @author Osmondy
 * 
 */
public class SongInfo
{
	
	private String encode;
	
	private String decode;
	
	private int type;
	
	private int lrcid;
	
	private int flag;
	
	public String getEncode()
	{
		return encode;
	}
	
	public void setEncode(String encode)
	{
		this.encode = encode;
	}
	
	public String getDecode()
	{
		return decode;
	}
	
	public void setDecode(String decode)
	{
		this.decode = decode;
	}
	
	public int getType()
	{
		return type;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public int getLrcid()
	{
		return lrcid;
	}
	
	public void setLrcid(int lrcid)
	{
		this.lrcid = lrcid;
	}
	
	public int getFlag()
	{
		return flag;
	}
	
	public void setFlag(int flag)
	{
		this.flag = flag;
	}
	
}
