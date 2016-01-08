package com.opendrive.android.parser;

import java.io.IOException;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.opendrive.android.datamodel.DeleteFileData;

import android.util.Xml;

public class DeletFileDataParser {

	private DeleteFileData mDeleteFileData = null;

	public DeletFileDataParser() {
		// TODO Auto-generated constructor stub
	}

	public DeleteFileData parseResponse(String responseXml) throws IOException {

		XmlPullParser parser = Xml.newPullParser();

		try {
			// auto-detect the encoding from the stream
			parser.setInput(new StringReader(responseXml));

			int eventType = parser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {

				String name = null;

				switch (eventType) {

				case XmlPullParser.START_DOCUMENT:
					mDeleteFileData = new DeleteFileData();
					break;

				case XmlPullParser.START_TAG:
					name = parser.getName();
					if(name.equalsIgnoreCase("Name")){
						mDeleteFileData.setName(parser.nextText());
					}else if(name.equalsIgnoreCase("Description")){
						mDeleteFileData.setDescription(parser.nextText());
					}
					break;

				case XmlPullParser.END_TAG:
					break;
				}

				eventType = parser.next();
			}
			
			return mDeleteFileData;
		}
		catch (XmlPullParserException e) {
			throw new IOException(e.toString());
		}
	}
}
