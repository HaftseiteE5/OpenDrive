package com.opendrive.android.parser;

import java.io.IOException;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.opendrive.android.datamodel.LoginData;



import android.util.Log;
import android.util.Xml;

public class LoginDataParser {

	private LoginData _loginData = null;

	public LoginDataParser() {
		// TODO Auto-generated constructor stub
	}

	public LoginData parseResponse(String responseXml) throws IOException {

		//if(!responseXml.contains("&amp;"))
		//	responseXml = responseXml.replace("&", "&amp;");
		XmlPullParser parser = Xml.newPullParser();

		try {
			// auto-detect the encoding from the stream
			parser.setInput(new StringReader(responseXml));

			int eventType = parser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {

				String name = null;

				switch (eventType) {

				case XmlPullParser.START_DOCUMENT:
					_loginData = new LoginData();
					break;

				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase("SessionID")) {
						_loginData.setSessionID(parser.nextText());
					}else if(name.equalsIgnoreCase("IsAccessUser")){
						_loginData.setIsAccessUser(parser.nextText());
					}else if(name.equalsIgnoreCase("UserLang")){
						_loginData.setUserLang(parser.nextText());
					}else if(name.equalsIgnoreCase("UserLevel")){
						_loginData.setUserLevel(parser.nextText());
					}else if(name.equalsIgnoreCase("UserName")){
						_loginData.setUserName(parser.nextText());
					}else if(name.equalsIgnoreCase("UploadSpeedLimit")){
						_loginData.setUploadSpeedLimit(parser.nextText());
					}else if(name.equalsIgnoreCase("DownloadSpeedLimit")){
						_loginData.setDownloadSpeedLimit(parser.nextText());
					}else if(name.equalsIgnoreCase("Encoding")){
						_loginData.setEncoding(parser.nextText());
					}else if(name.equalsIgnoreCase("LinkUpgrade")){
						_loginData.setLinkUpgrade(parser.nextText());
					}else if(name.equalsIgnoreCase("Type")){
						_loginData.setType(parser.nextText());
					}else if(name.equalsIgnoreCase("Name")){
						_loginData.setName(parser.nextText());
					}else if(name.equalsIgnoreCase("Description")){
						_loginData.setDescription(parser.nextText());
					}else if(name.equalsIgnoreCase("UserFirstName")) {
						_loginData.setUserFirstName(parser.nextText());
					}
					break;

				case XmlPullParser.END_TAG:
					break;
				}

				eventType = parser.next();
			}
			
			return _loginData;
		}
		catch (XmlPullParserException e) {
			Log.i("ExeptionString", e.toString());
			throw new IOException(e.toString());
		}
	}
}
