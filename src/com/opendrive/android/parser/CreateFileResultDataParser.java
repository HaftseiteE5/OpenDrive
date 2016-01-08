package com.opendrive.android.parser;

import java.io.IOException;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.opendrive.android.datamodel.CreateFileResultData;

import android.util.Xml;

public class CreateFileResultDataParser {

	private CreateFileResultData mCreateFileResultData = null;

	public CreateFileResultDataParser() {
		// TODO Auto-generated constructor stub
	}

	public CreateFileResultData parseResponse(String responseXml) throws IOException {

		XmlPullParser parser = Xml.newPullParser();

		try {
			// auto-detect the encoding from the stream
			parser.setInput(new StringReader(responseXml));

			int eventType = parser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {

				String name = null;

				switch (eventType) {

				case XmlPullParser.START_DOCUMENT:
					mCreateFileResultData = new CreateFileResultData();
					break;

				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase("ID")) {
						mCreateFileResultData.setID(parser.nextText());
					}else if(name.equalsIgnoreCase("Name")){
						mCreateFileResultData.setName(parser.nextText());
                    }else if(name.equalsIgnoreCase("Type")){
                        mCreateFileResultData.setType(parser.nextText());
					}else if(name.equalsIgnoreCase("Description")){
						mCreateFileResultData.setDescription(parser.nextText());
					}else if(name.equalsIgnoreCase("Size")){
						mCreateFileResultData.setSize(parser.nextText());
					}else if(name.equalsIgnoreCase("DateModified")){
						mCreateFileResultData.setDateModified(parser.nextText());
					}else if(name.equalsIgnoreCase("DateAccessed")){
						mCreateFileResultData.setDateAccessed(parser.nextText());
					}else if(name.equalsIgnoreCase("Link")){
						mCreateFileResultData.setLink(parser.nextText());
					}else if(name.equalsIgnoreCase("DirectLink")){
						mCreateFileResultData.setDirectLink(parser.nextText());
					}else if(name.equalsIgnoreCase("StreamLink")){
						mCreateFileResultData.setStreamLink(parser.nextText());
					}else if(name.equalsIgnoreCase("Access")){
						mCreateFileResultData.setAccess(parser.nextText());
					}else if(name.equalsIgnoreCase("Price")){
						mCreateFileResultData.setPrice(parser.nextText());
					}else if(name.equalsIgnoreCase("DateDeleted")){
						mCreateFileResultData.setDateDeleted(parser.nextText());
					}else if(name.equalsIgnoreCase("TempLocation")){
						mCreateFileResultData.setTempLocation(parser.nextText());
					}else if(name.equalsIgnoreCase("SpeedLimit")){
						mCreateFileResultData.setSpeedLimit(parser.nextText());
					}
					break;

				case XmlPullParser.END_TAG:
					break;
				}

				eventType = parser.next();
			}
			
			return mCreateFileResultData;
		}
		catch (XmlPullParserException e) {
			throw new IOException(e.toString());
		}
	}
}
