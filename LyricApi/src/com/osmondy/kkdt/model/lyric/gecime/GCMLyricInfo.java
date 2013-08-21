package com.osmondy.kkdt.model.lyric.gecime;

/**
 * 歌词迷的歌词信息
 * 
 * @author Osmondy
 * 
 */
public class GCMLyricInfo
{
	
	private String songId;
	
	private String songName;
	
	private String artist;
	
	private String albumId;
	
	private String lyricPath;
	
	public String getSongId()
	{
		return songId;
	}
	
	public void setSongId(String songId)
	{
		this.songId = songId;
	}
	
	public String getSongName()
	{
		return songName;
	}
	
	public void setSongName(String songName)
	{
		this.songName = songName;
	}
	
	public String getArtist()
	{
		return artist;
	}
	
	public void setArtist(String artist)
	{
		this.artist = artist;
	}
	
	public String getAlbumId()
	{
		return albumId;
	}
	
	public void setAlbumId(String albumId)
	{
		this.albumId = albumId;
	}
	
	public String getLyricPath()
	{
		return lyricPath;
	}
	
	public void setLyricPath(String lyricPath)
	{
		this.lyricPath = lyricPath;
	}
	
	@Override
	public String toString()
	{
		return "GCMLyricInfo [songId=" + songId + ", songName=" + songName + ", artist=" + artist + ", albumId="
				+ albumId + ", lyricPath=" + lyricPath + "]";
	}
	
}
