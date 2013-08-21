package com.osmondy.kkdt.business.lyric.qq;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
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
import com.osmondy.kkdt.model.lyric.qq.SongInfo;

/**
 * 歌词来源于QQ
 * 
 * @author Osmondy
 * 
 */
public class QQLyricHelper extends LyricLoader
{
	
	private static final String TAG = "QQLyric";
	
	/**
	 * 歌曲信息请求地址
	 */
	protected static final String SONGINFO_BASE_URL = "http://qqmusic.qq.com/fcgi-bin/qm_getLyricId.fcg";
	
	/**
	 * 歌词文件请求地址
	 */
	protected static final String LYRIC_BASE_URL = "http://music.qq.com/miniportal/static/lyric";

	public QQLyricHelper()
	{
		super("QQ");
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
				savePath = getLocalLyricPath(songInfo.getName(), songInfo.getSingername());
				Log.I(TAG, "Create a new savePath by [songInfo], savePath=" + savePath);
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
					// String lyricContent = EntityUtils.toString(response.getEntity(), "gbk");
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
			
			//String localPath = getLocalLyricPath(songInfo.getName(), songInfo.getSingername());
			Lyric lyric = loadLocalLyric(savePath);
			if (lyric != null)
			{
				lyric.setSongname(songInfo.getName());
				lyric.setSingername(songInfo.getSingername());
				
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
			
			return QQLyricParser.parseSongInfo(content);
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
	 * 请求地址格式: http://music.qq.com/miniportal/static/lyric/67/183767.xml
	 * 
	 * @param songInfo
	 * @return
	 */
	protected String getServerLyricUrlBySongInfo(SongInfo songInfo)
	{
		String id = songInfo.getId();
		
		if (!StringUtils.isNumeric(id))
		{
			return null;
		}
		int postfix = Integer.parseInt(id) % 100;
		
		StringBuffer sb = new StringBuffer();
		sb.append(LYRIC_BASE_URL);
		sb.append("/");
		sb.append(postfix);
		sb.append("/");
		sb.append(id);
		sb.append(".xml");
		
		return sb.toString();
	}
	

	/**
	 * 返回请求歌曲信息的地址.
	 * 请求地址格式: http://qqmusic.qq.com/fcgi-bin/qm_getLyricId.fcg?name=连哭都是我的错&singer=东来东往&from=qqplayer
	 */
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
			sb.append(SONGINFO_BASE_URL);
			sb.append("?");
			sb.append("name=" + URLEncoder.encode(music.getTitle(), "gbk"));
			sb.append("&");
			sb.append("singer=" + URLEncoder.encode(music.getArtist(), "gbk"));
			sb.append("&");
			sb.append("from=qqplayer");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		
		return sb.toString();
	}
	
}
