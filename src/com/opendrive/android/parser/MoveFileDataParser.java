package com.opendrive.android.parser;

import java.io.IOException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.MoveFileData;

public class MoveFileDataParser {

    private MoveFileData mMoveFileData = null;
    private boolean mIsError = false;

    public MoveFileDataParser() {
	// TODO Auto-generated constructor stub
    }

    public MoveFileData parseResponse(String responseXml) throws IOException {

	XmlPullParser parser = Xml.newPullParser();

	try {
	    // auto-detect the encoding from the stream
	    parser.setInput(new StringReader(responseXml));

	    int eventType = parser.getEventType();

	    while (eventType != XmlPullParser.END_DOCUMENT) {

		String name = null;

		switch (eventType) {

		case XmlPullParser.START_DOCUMENT:
		    mMoveFileData = new MoveFileData();
		    break;

		case XmlPullParser.START_TAG:
		    name = parser.getName();
		    if (name.equalsIgnoreCase("Error")) {
			mIsError = true;
		    } else if (name.equalsIgnoreCase("Name")) {
			mMoveFileData.setName(Utils.getRevertedString(parser.nextText()));
		    } else if (name.equalsIgnoreCase("Description")) {
			mMoveFileData.setDescription(Utils.getRevertedString(parser.nextText()));
			if (mIsError) {
			    mMoveFileData.setErrorMessage(mMoveFileData.getDescription());
			}
		    } else if (name.equalsIgnoreCase("Access")) {
			mMoveFileData.setAccess(Utils.getRevertedString(parser.nextText()));
		    } else if (name.equalsIgnoreCase("Link")) {
			mMoveFileData.setLink(Utils.getRevertedString(parser.nextText()));
		    } else if (name.equalsIgnoreCase("DateModified")) {
			mMoveFileData.setDateModified(Utils.getRevertedString(parser.nextText()));
		    } else if (name.equalsIgnoreCase("DirectLink")) {
			mMoveFileData.setDirectLink(Utils.getRevertedString(parser.nextText()));
		    } else if (name.equalsIgnoreCase("StreamLink")) {
			mMoveFileData.setStreamLink(Utils.getRevertedString(parser.nextText()));
		    } else if (name.equalsIgnoreCase("DirectLinkPublic")) {
			mMoveFileData.setDirectLinkPublic(Utils.getRevertedString(parser.nextText()));
		    }
		    break;

		case XmlPullParser.END_TAG:
		    break;
		}

		eventType = parser.next();
	    }

	    return mMoveFileData;
	} catch (XmlPullParserException e) {
	    throw new IOException(e.toString());
	}
    }

}
