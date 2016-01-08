package com.opendrive.android.parser;

import java.io.IOException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.opendrive.android.datamodel.PathData;



import android.util.Xml;

public class PathDataParser {

	private PathData _pathData = null;

	public PathDataParser() {
		// TODO Auto-generated constructor stub
	}

	public PathData parseResponse(String responseXml) throws IOException {

		XmlPullParser parser = Xml.newPullParser();

		try {
			// auto-detect the encoding from the stream
			parser.setInput(new StringReader(responseXml));

			int eventType = parser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {

				String name = null;

				switch (eventType) {

				case XmlPullParser.START_DOCUMENT:
					_pathData = new PathData();
					break;

				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase("Path")) {
						_pathData.setPath(parser.nextText());
					}else if(name.equalsIgnoreCase("Name")){
						_pathData.setName(parser.nextText());
					}else if(name.equalsIgnoreCase("Description")){
						_pathData.setDescription(parser.nextText());
					}
					break;

				case XmlPullParser.END_TAG:
					break;
				}

				eventType = parser.next();
			}
			
			return _pathData;
		}
		catch (XmlPullParserException e) {
			throw new IOException(e.toString());
		}
	}
}
