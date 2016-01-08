package com.opendrive.android.parser;

import java.io.IOException;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.opendrive.android.common.Constant;
import com.opendrive.android.datamodel.StorageInfo;

import android.util.Xml;

public class StorageInfoParser {

    private String space = "Loading...";
    private StorageInfo msStorageInfo = new StorageInfo();
    private String spaceFree = "";
    private String spaceUsed = "";
    private String unlimitedPlan = "";

    public StorageInfoParser() {
	// TODO Auto-generated constructor stub
    }

    public StorageInfo parseResponse(String responseXml) throws IOException {

	XmlPullParser parser = Xml.newPullParser();

	try {
	    // auto-detect the encoding from the stream
	    parser.setInput(new StringReader(responseXml));

	    int eventType = parser.getEventType();

	    while (eventType != XmlPullParser.END_DOCUMENT) {

		String name = null;

		switch (eventType) {

		case XmlPullParser.START_DOCUMENT:
		    break;

		case XmlPullParser.START_TAG:
		    name = parser.getName();
		    if (name.equalsIgnoreCase("SpaceFree")) {
			spaceFree = parser.nextText();
		    } else if (name.equalsIgnoreCase("UnlimitedPlan")) {
			unlimitedPlan = parser.nextText();
		    } else if (name.equalsIgnoreCase("SpaceUsed")) {
			spaceUsed = parser.nextText();
		    }
		    break;

		case XmlPullParser.END_TAG:
		    break;
		}

		eventType = parser.next();
	    }

	    if (unlimitedPlan.equals("False")) {
		space = spaceFree;
		msStorageInfo.setSpace(space);
		msStorageInfo.setSpaceType(Constant.SPACE_FREE);
	    } else {
		space = spaceUsed;
		msStorageInfo.setSpace(space);
		msStorageInfo.setSpaceType(Constant.SPACE_USED);
	    }

	    return msStorageInfo;
	} catch (XmlPullParserException e) {
	    throw new IOException(e.toString());
	}
    }
}
