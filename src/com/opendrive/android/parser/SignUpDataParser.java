package com.opendrive.android.parser;

import java.io.IOException;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.opendrive.android.datamodel.SignUpData;

import android.util.Xml;

public class SignUpDataParser {

	private SignUpData _signupData = null;

	public SignUpDataParser() {
		// TODO Auto-generated constructor stub
	}

	public SignUpData parseResponse(String responseXml) throws IOException {

		XmlPullParser parser = Xml.newPullParser();

		try {
			// auto-detect the encoding from the stream
			parser.setInput(new StringReader(responseXml));

			int eventType = parser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {

				String name = null;

				switch (eventType) {

				case XmlPullParser.START_DOCUMENT:
					_signupData = new SignUpData();
					break;

				case XmlPullParser.START_TAG:
					name = parser.getName();
					if(name.equalsIgnoreCase("UserName")){
						_signupData.setUserName(parser.nextText());
					}else if(name.equalsIgnoreCase("PrivateKey")){
						_signupData.setPrivateKey(parser.nextText());
					}else if(name.equalsIgnoreCase("Type")){
						_signupData.setType(parser.nextText());
					}else if(name.equalsIgnoreCase("Name")){
						_signupData.setName(parser.nextText());
					}else if(name.equalsIgnoreCase("Description")){
						_signupData.setDescription(parser.nextText());
					}
					break;

				case XmlPullParser.END_TAG:
					break;
				}

				eventType = parser.next();
			}
			
			return _signupData;
		}
		catch (XmlPullParserException e) {
			throw new IOException(e.toString());
		}
	}
}
