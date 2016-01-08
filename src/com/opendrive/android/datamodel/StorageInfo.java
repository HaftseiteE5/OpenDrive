package com.opendrive.android.datamodel;

public class StorageInfo {
    public String space;

    public String getSpace() {
	return space;
    }

    public void setSpace(String space) {
	this.space = space;
    }

    public int getSpaceType() {
	return spaceType;
    }

    public void setSpaceType(int spaceType) {
	this.spaceType = spaceType;
    }

    public int spaceType;
}
