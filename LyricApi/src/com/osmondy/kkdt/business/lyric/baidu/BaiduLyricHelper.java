package com.osmondy.kkdt.business.lyric.baidu;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParserException;

import android.text.TextUtils;

import com.osmondy.framework.common.extra.ResultSet;
import com.osmondy.framework.common.log.Log;
import com.osmondy.framework.common.util.http.HttpClientUtils;
import com.osmondy.framework.common.util.http.HttpRequestException;
import com.osmondy.kkdt.business.lyric.LyricLoader;
import com.osmondy.kkdt.model.Music;
import com.osmondy.kkdt.model.lyric.Lyric;
import com.osmondy.kkdt.model.lyric.baidu.SongInfo;

/**
 * 歌词来源于Baidu
 * 
 * @author Osmondy
 * 
 */
public class BaiduLyricHelper extends LyricLoader
{
	
	private static final String TAG = "BaiduLyricHelper";
	
	/**
	 * 歌曲信息请求地址
	 */
	protected static final String SONGINFO_BASE_URL = "http://box.zhangmen.baidu.com/x";
	
	/**
	 * 歌词文件请求地址
	 */
	protected static final String LYRIC_BASE_URL = "http://box.zhangmen.baidu.com/bdlrc";
	
	public BaiduLyricHelper()
	{
		super("BaiDu");
	}
	
	@Override
	public Lyric download(String requestUrl, String savePath) throws HttpRequestException
	{
		try
		{
			ResultSet<SongInfo> songInfos = doGetSongInfo(requestUrl);
			SongInfo songInfo = null;
			if (songInfos != null && !songInfos.isEmpty())
			{
				// 取歌词的第一个源
				songInfo = songInfos.getData().get(0);
			}
			
			if (songInfo == null)
			{
				return null;
			}
			
			if (TextUtils.isEmpty(savePath))
			{
				Log.W(TAG, "Invalid savePath, savePath=" + savePath);
				return null;
			}
			
			HttpClient httpClient = HttpClientUtils.getDefaultHttpClient();
			boolean isDownload = false;
			try
			{
				String lyricUrl = getServerLyricUrlBySongInfo(songInfo);
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
			finally
			{
				if (httpClient != null)
				{
					httpClient.getConnectionManager().shutdown();
				}
			}
			
			Lyric lyric = loadLocalLyric(savePath);
			if (lyric != null)
			{
				lyric.setSongname(null);
				lyric.setSingername(null);
				
				Log.I(TAG, "Load server lyric finished. Lyric: " + lyric);
			}
			
			return lyric;
		}
		catch (XmlPullParserException e)
		{
			e.printStackTrace();
			
			throw new HttpRequestException();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 获取歌词对应的歌曲信息
	 * 
	 * @param requestUrl
	 * @return
	 * @throws HttpRequestException
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	protected ResultSet<SongInfo> doGetSongInfo(String requestUrl) throws HttpRequestException, XmlPullParserException,
			IOException
	{
		HttpClient httpClient = HttpClientUtils.getDefaultHttpClient();
		HttpUriRequest httpUriRequest = new HttpGet(requestUrl);
		
		HttpResponse response = httpClient.execute(httpUriRequest);
		
		if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
		{
			String content = EntityUtils.toString(response.getEntity(), "gbk");
			
			return BaiduLyricParser.parseSongInfo(content);
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
	 * 返回请求歌词的地址, 通过 SongInfo生成最终可请求到歌词文件的地址. </br>
	 * 
	 * @param songInfo
	 * @return
	 */
	protected String getServerLyricUrlBySongInfo(SongInfo songInfo)
	{
		int lrcid = songInfo.getLrcid();
		int postfix = lrcid / 100;
		
		StringBuffer sb = new StringBuffer();
		sb.append(LYRIC_BASE_URL);
		sb.append("/");
		sb.append(postfix);
		sb.append("/");
		sb.append(lrcid);
		sb.append(".lrc");
		
		return sb.toString();
	}

	@Override
	public String getServerLyricUrl(Music music)
	{
		if (TextUtils.isEmpty(music.getTitle()) || TextUtils.isEmpty(music.getArtist()))
		{
			return null;
		}
		//protected static final String SONGINFO_BASE_URL = "http://box.zhangmen.baidu.com/x?op=12&count=1&title=";
		Log.D(TAG, "Songname: " + music.getTitle() + ", Singername: " + music.getArtist());
		StringBuffer sb = new StringBuffer();
		try
		{
			sb.append(SONGINFO_BASE_URL);
			sb.append("?");
			sb.append("op=12");
			sb.append("&");
			sb.append("count=1");
			sb.append("&");
			sb.append("title=");
			sb.append(URLEncoder.encode(music.getTitle(), "utf-8"));
			sb.append("$$");
			sb.append(URLEncoder.encode(music.getArtist(), "utf-8"));
			sb.append("$$$$");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		
		return sb.toString();
		
	}
	
}
