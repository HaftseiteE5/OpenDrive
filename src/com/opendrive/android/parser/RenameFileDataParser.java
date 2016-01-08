package com.opendrive.android.parser;

import java.io.IOException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.DeleteFileData;
import com.opendrive.android.datamodel.RenameFileData;

public class RenameFileDataParser {

    private RenameFileData mRenameFileData = null;
    private boolean mIsError = false;

    public RenameFileDataParser() {
	// TODO Auto-generated constructor stub
    }

    public RenameFileData parseResponse(String responseXml) throws IOException {

	XmlPullParser parser = Xml.newPullParser();

	try {
	    // auto-detect the encoding from the stream
	    parser.setInput(new StringReader(responseXml));

	    int eventType = parser.getEventType();

	    while (eventType != XmlPullParser.END_DOCUMENT) {

		String name = null;

		switch (eventType) {

		case XmlPullParser.START_DOCUMENT:
		    mRenameFileData = new RenameFileData();
		    break;

		case XmlPullParser.START_TAG:
		    name = parser.getName();
		    if (name.equalsIgnoreCase("Error")) {
			mIsError = true;
		    } else if (name.equalsIgnoreCase("Name")) {
			mRenameFileData.setName(Utils.getRevertedString(parser.nextText()));
		    } else if (name.equalsIgnoreCase("Description")) {
			mRenameFileData.setDescription(Utils.getRevertedString(parser.nextText()));
			if (mIsError) {
			    mRenameFileData.setErrorMessage(mRenameFileData.getDescription());
			}
		    } else if (name.equalsIgnoreCase("Link")) {
			mRenameFileData.setLink(Utils.getRevertedString(parser.nextText()));
		    } else if (name.equalsIgnoreCase("DateModified")) {
			mRenameFileData.setDateModified(Utils.getRevertedString(parser.nextText()));
		    } else if (name.equalsIgnoreCase("DirectLink")) {
			mRenameFileData.setDirectLink(Utils.getRevertedString(parser.nextText()));
		    } else if (name.equalsIgnoreCase("StreamLink")) {
			mRenameFileData.setStreamLink(Utils.getRevertedString(parser.nextText()));
		    } else if (name.equalsIgnoreCase("DirectLinkPublic")) {
			mRenameFileData.setDirectLinkPublic(Utils.getRevertedString(parser.nextText()));
		    } else if (name.equalsIgnoreCase("Access")) {
			mRenameFileData.setAccess(Utils.getRevertedString(parser.nextText()));
		    }

		    break;

		case XmlPullParser.END_TAG:
		    break;
		}

		eventType = parser.next();
	    }

	    return mRenameFileData;
	} catch (XmlPullParserException e) {
	    throw new IOException(e.toString());
	}
    }

}
