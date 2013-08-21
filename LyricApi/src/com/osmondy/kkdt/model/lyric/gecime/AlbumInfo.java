package com.osmondy.kkdt.model.lyric.gecime;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 歌曲专辑信息. 专辑信息如下:</br> { "count": 1, "code": 0, "result": { "cover":
 * "http://s.geci.me/album-cover/157/1573814.jpg", "thumb":
 * "http://s.geci.me/album-cover/157/1573814-thumb.jpg" } }
 * 
 * @author Osmondy
 * 
 */
public class AlbumInfo implements Parcelable
{
	
	public static final String KEY_COVER = "album_cover";
	
	public static final String KEY_THUMB = "album_thumb";
	
	/**
	 * 封面
	 */
	private String cover;
	
	/**
	 * 缩略图
	 */
	private String thumb;
	
	public AlbumInfo()
	{
		this.cover = null;
		this.thumb = null;
	}
	
	public String getCover()
	{
		return cover;
	}
	
	public void setCover(String cover)
	{
		this.cover = cover;
	}
	
	public String getThumb()
	{
		return thumb;
	}
	
	public void setThumb(String thumb)
	{
		this.thumb = thumb;
	}
	
	@Override
	public int describeContents()
	{
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		Bundle bundle = new Bundle();
		bundle.putString(KEY_COVER, cover);
		bundle.putString(KEY_THUMB, thumb);
		
		dest.writeBundle(bundle);
	}
	
	public static final Parcelable.Creator<AlbumInfo> CREATOR = new Parcelable.Creator<AlbumInfo>()
	{
		
		@Override
		public AlbumInfo createFromParcel(Parcel source)
		{
			AlbumInfo albumInfo = new AlbumInfo();
			Bundle bundle = new Bundle();
			bundle = source.readBundle();
			albumInfo.setCover(bundle.getString(KEY_COVER));
			albumInfo.setThumb(bundle.getString(KEY_THUMB));
			
			return albumInfo;
		}
		
		@Override
		public AlbumInfo[] newArray(int size)
		{
			return new AlbumInfo[size];
		}
	};
	
}
