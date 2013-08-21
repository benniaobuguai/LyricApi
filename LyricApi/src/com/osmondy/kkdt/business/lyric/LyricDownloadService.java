package com.osmondy.kkdt.business.lyric;

import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.osmondy.framework.common.log.Log;
import com.osmondy.framework.common.util.http.HttpRequestException;
import com.osmondy.kkdt.business.lyric.gecime.AlbumInfo;
import com.osmondy.kkdt.business.lyric.gecime.GeCiMeHelper;
import com.osmondy.kkdt.business.lyric.qq.QQLyricHelper;
import com.osmondy.kkdt.model.Music;

/**
 * 歌词下载的后台服务
 * 
 * @author Osmondy
 * 
 */
public class LyricDownloadService extends IntentService
{
	
	private static final String TAG = "LyricDownloadService";
	
	public static final int RC_START = 0x1;
	
	public static final int RC_RUN = 0x2;
	
	public static final int RC_PAUSE = 0x3;
	
	public static final int RC_FINISH = 0x4;
	
	public static final int RC_ERROR = 0x5;
	
	private static byte[] mLock = new byte[0];
	
	private LyricLoader mLyricHelper;
	
	private List<DownloadTask<Music, ResultReceiver>> mDownloadTask;
	
	/**
	 * 必须要有无参的构造函数, 否则会无法正常启动.
	 */
	public LyricDownloadService()
	{
		this("LyricDownloadService");
	}
	
	public LyricDownloadService(String name)
	{
		super(name);
		mDownloadTask = new ArrayList<LyricDownloadService.DownloadTask<Music, ResultReceiver>>();
		
		mLyricHelper = new QQLyricHelper();
		// mLyricHelper = new BaiduLyricHelper();
		// mLyricHelper = new GeCiMeHelper();
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.I(TAG, "LyricDownloadService--->onCreate()");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.D(TAG, "LyricDownloadService--->onStartCommand()");
		synchronized (mLock)
		{
			mDownloadTask.clear();
			
			ResultReceiver receiver = intent.getParcelableExtra("result_receiver");
			Music music = intent.getParcelableExtra("music");
			
			LyricDownloadService.DownloadTask<Music, ResultReceiver> task = new DownloadTask<Music, ResultReceiver>(
					music, receiver);
			
			mDownloadTask.add(task);
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	protected void onHandleIntent(Intent intent)
	{
		Log.D(TAG, "LyricDownloadService--->Start.");
		
		ResultReceiver receiver = null;
		Music music = null;
		synchronized (mLock)
		{
			if (!mDownloadTask.isEmpty())
			{
				DownloadTask<Music, ResultReceiver> downloadTask = mDownloadTask.get(0);
				receiver = downloadTask.getK();
				music = downloadTask.getT();
			}
		}
		
		if (receiver == null || music == null)
		{
			return;
		}
		
		Log.E(TAG, music.toString());
		Bundle bundle = new Bundle();
		
		// receiver.send(RC_START, bundle);
		// SystemClock.sleep(5 * 1000);
		
		Lyric lyric = mLyricHelper.loadLyric(music);
		
		try
		{
			// 临时添加一个专辑封面
			GeCiMeHelper gcmHelper = new GeCiMeHelper();
			List<AlbumInfo> albumInfos = gcmHelper.getAlbumInfo(music);
			if (albumInfos != null && !albumInfos.isEmpty())
			{
				ArrayList<AlbumInfo> arrayList = new ArrayList<AlbumInfo>();
				arrayList.addAll(albumInfos);
				bundle.putParcelableArrayList("album_list", arrayList);
			}
		}
		catch (HttpRequestException e)
		{
			e.printStackTrace();
		}
		
		// -----------------------------------------------------------------------
		// -----------------------------------------------------------------------
		// 如果下载到的lyric为空, 可在此处使用QQLyricHelper/BaiduLyricHelper请求歌词.
		// 暂不需要.
		// if (lyric == null)
		// {
		// LyricHelper tempLyricHelper = new BaiduLyricHelper();
		// lyric = tempLyricHelper.loadLyric(music);
		// }
		// -----------------------------------------------------------------------
		
		bundle.putSerializable("lyric", lyric);
		receiver.send(RC_FINISH, bundle);
		
		Log.D(TAG, "LyricDownloadService--->End.");
	}
	
	private class DownloadTask<T, K>
	{
		private T t;
		
		private K k;
		
		public DownloadTask()
		{
			
		}
		
		public DownloadTask(T t, K k)
		{
			this.t = t;
			this.k = k;
		}
		
		public T getT()
		{
			return t;
		}
		
		public void setT(T t)
		{
			this.t = t;
		}
		
		public K getK()
		{
			return k;
		}
		
		public void setK(K k)
		{
			this.k = k;
		}
		
	}
	
}
