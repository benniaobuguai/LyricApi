package com.osmondy.kkdt.business.lyric.qq;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.osmondy.framework.common.extra.ResultSet;
import com.osmondy.kkdt.model.lyric.qq.SongInfo;


/**
 * QQ歌词解析器
 * 
 * @author Osmondy
 * 
 */
public class QQLyricParser
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
					if ("songcount".equalsIgnoreCase(tag))
					{
						resultSet.setTotal(Integer.valueOf(parser.nextText()));
					}
					else if ("songinfo".equalsIgnoreCase(tag))
					{
						songInfo = new SongInfo();
						String id = parser.getAttributeValue(null, "id");
						int scroll = 0;
						String scrollString = parser.getAttributeValue(null, "scroll");
						if (NumberUtils.isDigits(scrollString))
						{
							scroll = Integer.parseInt(scrollString);
						}
						
						songInfo.setId(id);
						songInfo.setScroll(scroll);
					}
					else if ("seqnum".equalsIgnoreCase(tag))
					{
						songInfo.setSeqnum(Integer.parseInt(parser.nextText()));
					}
					else if ("name".equalsIgnoreCase(tag))
					{
						songInfo.setName(parser.nextText());
					}
					else if ("singername".equalsIgnoreCase(tag))
					{
						songInfo.setSingername(parser.nextText());
					}
					
					break;
				}
				case XmlPullParser.END_TAG:
				{
					if ("songinfo".equalsIgnoreCase(tag) && songInfo != null)
					{
						songInfos.add(songInfo);
						resultSet.setData(songInfos);
					}
					
					break;
				}
			}
			
			eventType = parser.next();
		}
		
		return resultSet;
	}
	
}
