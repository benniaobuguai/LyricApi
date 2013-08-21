package com.osmondy.kkdt.business.lyric.baidu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.osmondy.framework.common.extra.ResultSet;
import com.osmondy.kkdt.model.lyric.baidu.SongInfo;

public class BaiduLyricParser
{
	
	/**
	 * 解析歌词信息
	 * 
	 * @param content
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static ResultSet<SongInfo> parseSongInfo(String content) throws XmlPullParserException, IOException
	{
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser parser = factory.newPullParser();
		
		InputStream is = new ByteArrayInputStream(content.getBytes());
		parser.setInput(is, "UTF-8");
		
		ResultSet<SongInfo> resultSet = new ResultSet<SongInfo>();
		List<SongInfo> songInfos = new ArrayList<SongInfo>();
		SongInfo songInfo = null;
		
		int eventType = parser.getEventType();
		String tag = null;
		while (eventType != XmlPullParser.END_DOCUMENT)
		{
			tag = parser.getName();
			switch (eventType)
			{
				case XmlPullParser.START_TAG:
				{
					if ("result".equalsIgnoreCase(tag))
					{
						songInfo = new SongInfo();
					}
					else if ("count".equalsIgnoreCase(tag))
					{
						resultSet.setTotal(Integer.valueOf(parser.nextText()));
					}
					//else if ("durl".equalsIgnoreCase(tag))
					else if ("encode".equalsIgnoreCase(tag))
					{
						songInfo.setEncode(parser.nextText());
					}
					else if ("decode".equalsIgnoreCase(tag))
					{
						songInfo.setDecode(parser.nextText());
					}
					else if ("type".equalsIgnoreCase(tag))
					{
						String text = parser.nextText();
						if (StringUtils.isNumeric(text))
						{
							songInfo.setType(Integer.valueOf(text));
						}
						
					}
					else if ("lrcid".equalsIgnoreCase(tag))
					{
						String text = parser.nextText();
						if (StringUtils.isNumeric(text))
						{
							songInfo.setLrcid(Integer.valueOf(text));
						}
					}
					else if ("flag".equalsIgnoreCase(tag))
					{
						String text = parser.nextText();
						if (StringUtils.isNumeric(text))
						{
							songInfo.setFlag(Integer.valueOf(text));
						}
					}
					
					break;
				}
				case XmlPullParser.END_TAG:
				{
					if ("durl".equalsIgnoreCase(tag) && songInfo != null)
					{
						songInfos.add(songInfo);
						resultSet.setData(songInfos);
					}
					else if ("result".equalsIgnoreCase(tag) && songInfo != null)
					{
						
					}
					
					break;
				}
			}
			
			eventType = parser.next();
		}
		
		return resultSet;
	}
	
}
