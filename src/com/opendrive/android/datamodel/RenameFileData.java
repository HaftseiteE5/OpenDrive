package com.opendrive.android.datamodel;

public class RenameFileData {

    public RenameFileData() {
    }

    private String mName = "";
    private String mDescription = "";
    private String mErrorMessage = "";
    private String mLink = "";
    private String mDateModified = "";
    private String mDirectLink = "";
    private String mStreamLink = "";
    private String mDirectLinkPublic = "";
    private String mAccess = "";
    

    public String getName() {
	return mName;
    }

    public void setName(String Name) {
	mName = Name;
    }

    public String getDescription() {
	return mDescription;
    }

    public void setDescription(String Description) {
	mDescription = Description;
    }

    public String getErrorMessage() {
	return mErrorMessage;
    }

    public void setErrorMessage(String mErrorMessage) {
	this.mErrorMessage = mErrorMessage;
    }

    public String getLink() {
	return mLink;
    }

    public void setLink(String mLink) {
	this.mLink = mLink;
    }

    public String getDateModified() {
	return mDateModified;
    }

    public void setDateModified(String mDateModified) {
	this.mDateModified = mDateModified;
    }

    public String getDirectLink() {
	return mDirectLink;
    }

    public void setDirectLink(String mDirectLink) {
	this.mDirectLink = mDirectLink;
    }

    public String getStreamLink() {
	return mStreamLink;
    }

    public void setStreamLink(String mStreamLink) {
	this.mStreamLink = mStreamLink;
    }

    public String getDirectLinkPublic() {
	return mDirectLinkPublic;
    }

    public void setDirectLinkPublic(String mDirectLinkPublic) {
	this.mDirectLinkPublic = mDirectLinkPublic;
    }

    public String getAccess() {
	return mAccess;
    }

    public void setAccess(String mAccess) {
	this.mAccess = mAccess;
    }
}
