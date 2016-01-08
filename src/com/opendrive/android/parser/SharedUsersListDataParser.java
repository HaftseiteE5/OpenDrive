package com.opendrive.android.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.FileData;


import android.util.Xml;

public class SharedUsersListDataParser {

	private FileData mFileData = null;
	private ArrayList<FileData> mFileList= null;
	
	public SharedUsersListDataParser() {
		// TODO Auto-generated constructor stub
	}

	public ArrayList<FileData> parseResponse(String responseXml) throws IOException {

		XmlPullParser parser = Xml.newPullParser();

		responseXml = Utils.getConvertedString(responseXml);
		
		try {
			// auto-detect the encoding from the stream
			parser.setInput(new StringReader(responseXml));
       
			int eventType = parser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {

				String name = "";

				switch (eventType) {

				case XmlPullParser.START_DOCUMENT:
					mFileList = new ArrayList<FileData>();
					break;

				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase("UserSharedFolder")) {
						mFileData = new FileData();
						mFileData.setIsFolder(true);
						mFileData.setID("Shared Folders");
					}else if (name.equalsIgnoreCase("UserID")) {
						mFileData.setUserID(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("Name")){
						mFileData.setName(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("Description")){
						mFileData.setDescription(Utils.getRevertedString(parser.nextText()));
					}
					break;

				case XmlPullParser.END_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase("UserSharedFolder")) {
						mFileList.add(mFileData);
					}
					break;
				}

				eventType = parser.next();
			}
			
			return mFileList;
		}
		catch (XmlPullParserException e) {
			throw new IOException(e.toString());
		}
	}
}
