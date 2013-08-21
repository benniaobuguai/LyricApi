package com.osmondy.kkdt.business.lyric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.osmondy.framework.common.log.Log;
import com.osmondy.framework.common.online.IDownload;
import com.osmondy.framework.common.util.http.HttpRequestException;
import com.osmondy.kkdt.global.config.AppConfig;
import com.osmondy.kkdt.model.Music;

/**
 * 歌词助手
 * 
 * @author Osmondy
 * 
 */
public abstract class LyricLoader implements IDownload<Lyric>
{
	
	private static final String TAG = "LyricHelper";
	
	private String name;
	
	private String charset;
	
	public LyricLoader(String name)
	{
		this.name = name;
		this.charset = "gbk";
	}
	
	/**
	 * 获取网络请求歌词地址
	 * 
	 * @param music
	 * @return
	 */
	public abstract String getServerLyricUrl(Music music);
	
	/**
	 * 返回本地存储歌词的路径
	 * 
	 * @param music
	 * @return
	 */
	protected String getLocalLyricPath(String songname, String singername)
	{
		if (TextUtils.isEmpty(songname) || TextUtils.isEmpty(singername))
		{
			return null;
		}
		
		return AppConfig.DIRECTORY_LYRIC + singername + " - " + songname + ".lrc";
	}
	
	/**
	 * 返回歌词, Step1: 本地歌词目录加载; Step2: 网络下载.
	 * 
	 * @param music
	 * @return
	 */
	public Lyric loadLyric(Music music)
	{
		if (TextUtils.isEmpty(music.getArtist()) || TextUtils.isEmpty(music.getTitle()))
		{
			Log.W(TAG, "Empty aritst or title, can't find lyric.");
			
			return null;
		}
		
		Lyric lyric = null;
		String localPath = getLocalLyricPath(music.getTitle(), music.getArtist());
		
		File file = new File(localPath);
		if (file.exists())
		{
			// 本地存在歌词文件, 直接加载此歌词.
			Log.D(TAG, "Loading lyric from local path.");
			try
			{
				lyric = loadLocalLyric(localPath);
				if (lyric != null)
				{
					lyric.setSongname(music.getTitle());
					lyric.setSingername(music.getArtist());
					
					Log.I(TAG, "Load local lyric finished. Lyric: " + lyric);
				}
			}
			catch (IOException e)
			{
				if (e instanceof FileNotFoundException)
				{
					Log.W(TAG, "Lyric not found.");
				}
				else
				{
					e.printStackTrace();
				}
			}
			
			return lyric;
		}

		String requestUrl = getServerLyricUrl(music);
		
		if (!TextUtils.isEmpty(requestUrl))
		{
			Log.D(TAG, "---------- Download lyric start ----------");
			try
			{
				lyric = download(requestUrl, localPath);
			}
			catch (HttpRequestException e)
			{
				e.printStackTrace();
			}
			
			Log.D(TAG, "---------- Download lyric end ----------");
			
			return lyric;
		}
		
		Log.W(TAG, "Not found a correct server lyric path.");
		
		return null;
	}
	
	/**
	 * 保存歌曲文件, 默认保存至{@link AppConfig#DIRECTORY_LYRIC}, 子类可自行重写保存至其它路径.
	 * 保存时, 先保存成*.lrc.tmp, 下载及保存成功后, 再重命名为*.lrc. 防止异常或停止下载歌词, 
	 * 下次无法再次下载.
	 * 
	 * @param is
	 * @param music
	 * @return
	 */
	protected boolean saveLyric(InputStream is, String savePath)
	{
		File file = new File(savePath);
		File dir = new File(file.getParent());
		
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		
		FileOutputStream fos = null;
		try
		{
			if (file.exists())
			{
				// 上次下载没完成, 暂时性策略为直接删除, 再次下载.
				if (!file.delete())
				{
					// 删除失败直接返回.
					return false;
				}
			}
			
			if (!file.exists())
			{
				if (!file.createNewFile())
				{
					// 创建失败直接返回.
					return false;
				}
			}
			
			fos = new FileOutputStream(savePath);
			
			byte[] buffer = new byte[1024];
			int len = -1;
			while ((len = is.read(buffer)) != -1)
			{
				fos.write(buffer, 0, len);
			}
			fos.flush();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		if (file.exists())
		{
			// *.lrc.tmp重命名为*.lrc
			String newPath = savePath.substring(0, savePath.lastIndexOf("."));
			return file.renameTo(new File(newPath));
		}
		
		return false;
	}

	/**
	 * 返回指定地址的歌词文件
	 * 
	 * @param path
	 * @return
	 * @throws IOException 
	 */
	public Lyric loadLocalLyric(String path) throws IOException
	{
		FileInputStream fis = null;
		InputStreamReader reader = null;
		BufferedReader bfReader = null;
		try
		{
			fis = new FileInputStream(path);
			reader = new InputStreamReader(fis, getCharset());
			bfReader = new BufferedReader(reader);
			
			List<LyricItem> lyricItems = new ArrayList<LyricItem>();
			int lines = 0;
			int duration = 0;
			
			LyricItem item = null;
			String line = null;
			while ((line = bfReader.readLine()) != null)
			{
				if (line.indexOf("[ti:") == 0 || line.indexOf("[ar:") == 0 || line.indexOf("[al:") == 0
						|| line.indexOf("[by:") == 0 || line.indexOf("[offset:") == 0)
				{
					continue;
				}
				if (line.indexOf("[") == 0 && line.indexOf("]") == 9)
				{
					item = parseLyricItem(line);
					lyricItems.add(item);
					lines++;
					duration = item.getTimestamp();
					
				}
			}
			
			if (lyricItems == null || lyricItems.isEmpty())
			{
				return null;
			}
			
			Lyric lyric = new Lyric();
			lyric.setLyricItems(lyricItems);
			lyric.setDuration(duration);
			lyric.setLines(lines);
			
			return lyric;
		}
		catch (FileNotFoundException e)
		{
			throw e;
		}
		catch (UnsupportedEncodingException e)
		{
			throw e;
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			if (bfReader != null)
			{
				try
				{
					bfReader.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 解析歌词行
	 * 
	 * @param lyricItem
	 * @return
	 */
	protected LyricItem parseLyricItem(String lyricItem)
	{
		int tl = lyricItem.indexOf("[");
		int tr = lyricItem.indexOf("]");
		String timestampString = lyricItem.substring(tl + 1, tr);
		String text = lyricItem.substring(tr + 1, lyricItem.length());
		
		// 格式化时间戳
		int timestamp = formatTimestamp(timestampString);
		
		return new LyricItem(timestamp, text);
	}
	
	/**
	 * 格式化时间戳
	 * 
	 * @param time
	 * @return
	 */
	private int formatTimestamp(String timestamp)
	{
		if (TextUtils.isEmpty(timestamp))
		{
			return 0;
		}

		String[] array = timestamp.replace(".", ":").split(":");
		
		return Integer.valueOf(array[0]) * 60 * 1000 + 
				Integer.valueOf(array[1]) * 1000 + 
				Integer.valueOf(array[2]) * 10;
	}
	
	//----------------------------------------------------------------------
	//----------------------------------------------------------------------
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getCharset()
	{
		return charset;
	}
	
	public void setCharset(String charset)
	{
		this.charset = charset;
	}
	
}
