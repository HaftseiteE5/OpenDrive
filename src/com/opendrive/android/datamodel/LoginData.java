package com.opendrive.android.datamodel;

public class LoginData {

	public LoginData() {
		// TODO Auto-generated constructor stub
	}
	
	private String mSessionID = "";
	private String mIsAccessUser = "";
	private String mUserLang = "";
	private String mUserLevel = "";
	private String mUserName = "";
	private String mUploadSpeedLimit = "";
	private String mDownloadSpeedLimit = "";
	private String mEncoding = "";
	private String mLinkUpgrade = "";
	private String mUserFirstName = "";
	private String mType = "";
	private String mName = "";
	private String mDescription = "";
	
	public String getSessionID(){
		return mSessionID;
	}
	
	public void setSessionID(String SessionID){
		mSessionID = SessionID;
	}

	public String getIsAccessUser(){
		return mIsAccessUser;
	}
	
	public void setIsAccessUser(String IsAccessUser){
		mIsAccessUser = IsAccessUser;
	}

	public String getUserLang(){
		return mUserLang;
	}
	
	public void setUserLang(String UserLang){
		mUserLang = UserLang;
	}

	public String getUserLevel(){
		return mUserLevel;
	}
	
	public void setUserLevel(String UserLevel){
		mUserLevel = UserLevel;
	}

	public String getUserName(){
		return mUserName;
	}
	
	public void setUserName(String UserName){
		mUserName = UserName;
	}

	public String getUploadSpeedLimit(){
		return mUploadSpeedLimit;
	}
	
	public void setUploadSpeedLimit(String UploadSpeedLimit){
		mUploadSpeedLimit = UploadSpeedLimit;
	}

	public String getDownloadSpeedLimit(){
		return mDownloadSpeedLimit;
	}
	
	public void setDownloadSpeedLimit(String DownloadSpeedLimit){
		mDownloadSpeedLimit = DownloadSpeedLimit;
	}

	public String getEncoding(){
		return mEncoding;
	}
	
	public void setEncoding(String Encoding){
		mEncoding = Encoding;
	}

	public String getLinkUpgrade(){
		return mLinkUpgrade;
	}
	
	public void setLinkUpgrade(String LinkUpgrade){
		mLinkUpgrade = LinkUpgrade;
	}

	public String getType(){
		return mType;
	}
	
	public void setType(String Type){
		mType = Type;
	}
	
	public String getName(){
		return mName;
	}
	
	public void setName(String Name){
		mName = Name;
	}
	
	public String getDescription(){
		return mDescription;
	}
	
	public void setDescription(String Description){
		mDescription = Description;
	}

	public String getUserFirstName() {
		return mUserFirstName;
	}

	public void setUserFirstName(String mUserFirstName) {
		this.mUserFirstName = mUserFirstName;
	}
}
