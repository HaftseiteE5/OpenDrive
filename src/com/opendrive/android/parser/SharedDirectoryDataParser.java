package com.opendrive.android.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.FileData;


import android.util.Xml;

public class SharedDirectoryDataParser {

	private FileData mFileData = null;
	private ArrayList<FileData> mFileList= null;
	
	public SharedDirectoryDataParser() {
		// TODO Auto-generated constructor stub
	}

	public ArrayList<FileData> parseResponse(String responseXml, String sharedUserID) throws IOException {

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
					if (name.equalsIgnoreCase("SharedDirectory")) {
						mFileData = new FileData();
						mFileData.setShareUserID(sharedUserID);
						mFileData.setIsFolder(true);
					}else if (name.equalsIgnoreCase("ID")) {
						mFileData.setID(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("FolderID")){
						mFileData.setFolderID(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("Name")){
						mFileData.setName(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("Description")){
						mFileData.setDescription(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("IsHomeFolder")){
						mFileData.setIsHomeFolder(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("IsDeleteable")){
						mFileData.setIsDeleteable(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("IsRenameable")){
						mFileData.setIsRenameable(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("IsGallery")){
						mFileData.setIsGallery(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("IsMP3")){
						mFileData.setIsMP3(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("Permission")){
						mFileData.setPermission(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("Access")){
						mFileData.setAccess(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("HasSubfolders")){
						mFileData.setHasSubfolders(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("Shared")){
						mFileData.setShared(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("Link")){
						mFileData.setLink(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("DateCreated")){
						mFileData.setDateCreated(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("DateDeleted")){
						mFileData.setDateDeleted(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("FileId")){
						mFileData.setFileId(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("Size")){
						mFileData.setSize(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("Date")){
						mFileData.setDate(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("DateModified")){
						mFileData.setDateModified(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("DateAccessed")){
						mFileData.setDateAccessed(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("DirectLink")){
						mFileData.setDirectLink(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("DirectLinkPublic")){
						mFileData.setDirectLinkPublic(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("StreamLink")){
						mFileData.setStreamLink(Utils.getRevertedString(parser.nextText()));
					}else if(name.equalsIgnoreCase("Price")){
						mFileData.setPrice(Utils.getRevertedString(parser.nextText()));
					}
					break;

				case XmlPullParser.END_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase("SharedDirectory")) {
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
