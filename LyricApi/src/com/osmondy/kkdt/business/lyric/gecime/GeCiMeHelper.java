package com.osmondy.kkdt.business.lyric.gecime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import android.text.TextUtils;

import com.osmondy.framework.common.extra.ResultSet;
import com.osmondy.framework.common.log.Log;
import com.osmondy.framework.common.util.http.HttpClientUtils;
import com.osmondy.framework.common.util.http.HttpRequestException;
import com.osmondy.framework.common.util.http.HttpRequestResult;
import com.osmondy.kkdt.business.lyric.LyricLoader;
import com.osmondy.kkdt.model.Music;
import com.osmondy.kkdt.model.lyric.Lyric;
import com.osmondy.kkdt.model.lyric.gecime.AlbumInfo;
import com.osmondy.kkdt.model.lyric.gecime.GCMLyricInfo;

/**
 * 歌词迷API
 * 
 * @author Osmondy
 * 
 */
public class GeCiMeHelper extends LyricLoader
{
	
	private static final String TAG = "GeCiMeHelper";
	
	/**
	 * 根据歌曲名获取歌词: http://geci.me/api/lyric/:song</br>
	 * 根据歌曲名和歌手名获取歌词: http://geci.me/api/lyric/:song/:artist</br> 
	 * 返回数据格式: JSON</br> 
	 * 请求方法: GET</br>
	 */
	protected static final String LYRIC_BASE_PATH = "http://geci.me/api/lyric";
	
	/**
	 * 根据歌曲编号获取歌词URL: http://geci.me/api/lrc/:song_id
	 */
	protected static final String LYRIC_BY_SONGID = "http://geci.me/api/lrc";
	
	/**
	 * 根据专辑编号获取专辑封面URL: http://geci.me/api/cover/:album_id
	 */
	protected static final String ALBUM_BASE_PATH = "http://geci.me/api/cover";
	
	
	public GeCiMeHelper()
	{
		super("GeciMe");
		setCharset("utf-8");
	}

	/**
	 * 下载的歌词格式部分与Baidu/QQ的歌词格式对应不上, 存在格式： [01:43.00][00:19.00]今天我寒夜里看雪飘过.
	 * 还没有进行解析的适配, 不建议使用歌词迷下载的歌词.
	 */
	@Override
	public Lyric download(String requestUrl, String savePath) throws HttpRequestException
	{
		if (TextUtils.isEmpty(savePath))
		{
			Log.W(TAG, "Invalid savePath, savePath=" + savePath);
			return null;
		}
		
		ResultSet<GCMLyricInfo> lyricInfos = doGetLyricInfos(requestUrl);
		GCMLyricInfo lyricInfo = null;
		if (lyricInfos != null && !lyricInfos.isEmpty())
		{
			// 取歌词的第一个源
			lyricInfo = lyricInfos.getData().get(0);
		}
		
		if (lyricInfo == null)
		{
			return null;
		}
		
		HttpClient httpClient = HttpClientUtils.getDefaultHttpClient();
		boolean isDownload = false;
		try
		{
			String lyricUrl = lyricInfo.getLyricPath();
			Log.D(TAG, "Request lyric url: " + lyricUrl);
			
			HttpResponse response = doGetLyric(httpClient, lyricUrl);
			if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
			{
				// String lyricContent =
				// EntityUtils.toString(response.getEntity(), "gbk");
				isDownload = saveLyric(response.getEntity().getContent(), savePath + ".tmp");
			}
			else if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND)
			{
				throw new HttpRequestException("Lyric not found.");
			}
			else
			{
				return null;
			}
			
		}
		catch (HttpRequestException e)
		{
			e.printStackTrace();
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (httpClient != null)
			{
				httpClient.getConnectionManager().shutdown();
			}
		}
		
		Lyric lyric = null;
		try
		{
			lyric = loadLocalLyric(savePath);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		if (lyric != null)
		{
			lyric.setSongname(lyricInfo.getSongName());
			lyric.setSingername(null);
			
			Log.I(TAG, "Load server lyric finished. Lyric: " + lyric);
		}
		
		return lyric;
	}
	
	/**
	 * 获取歌词信息
	 * 
	 * @param requestUrl
	 * @return
	 * @throws HttpRequestException
	 */
	protected ResultSet<GCMLyricInfo> doGetLyricInfos(String requestUrl) throws HttpRequestException
	{
		HttpRequestResult httpRequestResult = HttpClientUtils.doHttpGet(requestUrl);
		if (httpRequestResult != null && httpRequestResult.isOK())
		{
			return GeCiMeLyricParser.parseGeCiMeLyric(httpRequestResult.getContent());
		}
		
		return null;
	}

	/**
	 * 获取歌词
	 * 
	 * @param requestUrl
	 * @return
	 * @throws HttpRequestException
	 */
	protected HttpResponse doGetLyric(HttpClient httpClient, String requestUrl) throws HttpRequestException
	{
		HttpUriRequest httpUriRequest = new HttpGet(requestUrl);
		try
		{
			return httpClient.execute(httpUriRequest);
		}
		catch (Exception e) // IOException & ClientProtocolException
		{
			Log.E(TAG, "doHttpUriRequest: HTTP " + httpUriRequest.getMethod() + " failed.", e);
			
			throw new HttpRequestException("HTTP " + httpUriRequest.getMethod() + " failed.");
		}
	}
	
	/**
	 * 根据专辑id, 单个专辑可能会有多个封面.
	 * 
	 * @param albumId
	 * @return
	 * @throws HttpRequestException
	 */
	public List<AlbumInfo> getAlbumInfo(String albumId) throws HttpRequestException
	{
		String requestUrl = getServerAlbumUrl(albumId);
		HttpRequestResult httpRequestResult = HttpClientUtils.doHttpGet(requestUrl);
		if (httpRequestResult != null && httpRequestResult.isOK())
		{
			ResultSet<AlbumInfo> resultSet = GeCiMeLyricParser.parseAlbumInfo(httpRequestResult.getContent());
			if (resultSet == null || resultSet.isEmpty())
			{
				return null;
			}
			
			return resultSet.getData();
		}
		
		return null;
	}
	
	/**
	 * 根据歌曲信息获取歌曲封面, 单个专辑可能会有多个封面.
	 * 
	 * @param music
	 * @return
	 * @throws HttpRequestException
	 */
	public List<AlbumInfo> getAlbumInfo(Music music) throws HttpRequestException
	{
		String lyricRequestUrl = getServerLyricUrl(music);
		
		ResultSet<GCMLyricInfo> resultSet = doGetLyricInfos(lyricRequestUrl);
		if (resultSet == null || resultSet.isEmpty())
		{
			return null;
		}
		
		return getAlbumInfo(resultSet.getData().get(0).getAlbumId());
	}

	@Override
	public String getServerLyricUrl(Music music)
	{
		if (TextUtils.isEmpty(music.getTitle()) || TextUtils.isEmpty(music.getArtist()))
		{
			return null;
		}
		
		Log.D(TAG, "Songname: " + music.getTitle() + ", Singername: " + music.getArtist());
		StringBuffer sb = new StringBuffer();
		try
		{
			sb.append(LYRIC_BASE_PATH);
			sb.append("/");
			sb.append(URLEncoder.encode(music.getTitle(), "utf-8"));
			sb.append("/");
			sb.append(URLEncoder.encode(music.getArtist(), "utf-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		
		return sb.toString();
	}
	
	/**
	 * 返回歌曲专辑信息请求地址
	 * 
	 * @param albumId
	 * @return
	 */
	public String getServerAlbumUrl(String albumId)
	{
		return ALBUM_BASE_PATH + "/" + albumId;
	}
	
}
