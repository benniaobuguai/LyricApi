package com.osmondy.kkdt.business.lyric.gecime;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.osmondy.framework.common.extra.ResultSet;
import com.osmondy.framework.common.log.Log;

/**
 * 歌词迷数据解析器
 * 
 * @author Osmondy
 * 
 */
public class GeCiMeLyricParser
{
	
	private static final String TAG = "GeCiMeLyricParser";
	
	/**
	 * 解析歌词信息
	 * 
	 * @param jsonData
	 * @return
	 * @throws JSONException
	 */
	public static ResultSet<GCMLyricInfo> parseGeCiMeLyric(String jsonData)
	{
		try
		{
			JSONObject jsonObject = new JSONObject(jsonData);
			ResultSet<GCMLyricInfo> resultSet = new ResultSet<GCMLyricInfo>();
			
			// 总数
			int count = jsonObject.optInt("count");
			// 响应码
			int code = jsonObject.optInt("code");
			// 歌词结果集
			JSONArray jsonArray = jsonObject.optJSONArray("result");
			
			resultSet.setStartIndex(0);
			resultSet.setTotal(count);
			
			if (jsonArray == null || jsonArray.length() == 0)
			{
				resultSet.setData(null);
				return resultSet;
			}
			
			List<GCMLyricInfo> lyricInfos = new ArrayList<GCMLyricInfo>();
			
			int length = jsonArray.length();
			JSONObject lyricObject = null;
			GCMLyricInfo lyricInfo = null;
			for (int i = 0; i < length; i++)
			{
				lyricInfo = new GCMLyricInfo();
				lyricObject = jsonArray.getJSONObject(i);
				lyricInfo.setSongId(lyricObject.optString("sid"));
				lyricInfo.setSongName(lyricObject.optString("song"));
				lyricInfo.setArtist(lyricObject.optString("artist"));
				lyricInfo.setAlbumId(lyricObject.optString("aid"));
				lyricInfo.setLyricPath(lyricObject.optString("lrc"));
				
				lyricInfos.add(lyricInfo);
			}
			resultSet.setData(lyricInfos);
			
			return resultSet;
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			Log.E(TAG, "GeCiMe's json has been changed. jsonData: " + jsonData);
			
			return null;
		}
		
	}
	
	/**
	 * 解析专辑信息, 服务器返回的结果只有一个专辑封面.
	 * 
	 * @param jsonData
	 * @return
	 */
	public static ResultSet<AlbumInfo> parseAlbumInfo(String jsonData)
	{
		try
		{
			JSONObject jsonObject = new JSONObject(jsonData);
			ResultSet<AlbumInfo> resultSet = new ResultSet<AlbumInfo>();
			
			// 总数
			int count = jsonObject.optInt("count");
			// 响应码
			int code = jsonObject.optInt("code");
			
			// 歌词结果集
			JSONObject albumObject = jsonObject.optJSONObject("result");
			
			resultSet.setStartIndex(0);
			resultSet.setTotal(count);
			
			List<AlbumInfo> albumInfos = new ArrayList<AlbumInfo>();
			
			AlbumInfo albumInfo = new AlbumInfo();
			albumInfo.setCover(albumObject.optString("cover"));
			albumInfo.setThumb(albumObject.optString("thumb"));
			
			albumInfos.add(albumInfo);
			resultSet.setData(albumInfos);
			
			return resultSet;
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			Log.E(TAG, "GeCiMe's json has been changed. jsonData: " + jsonData);
			
			return null;
		}
		
	}
	
}
