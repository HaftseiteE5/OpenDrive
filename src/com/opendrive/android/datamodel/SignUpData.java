package com.opendrive.android.datamodel;

public class SignUpData {

	public SignUpData() {
		// TODO Auto-generated constructor stub
	}
	
	private String mUserName = "";
	private String mPrivateKey = "";
	
	private String mType = "";
	private String mName = "";
	private String mDescription = "";
	
	public String getUserName(){
		return mUserName;
	}
	
	public void setUserName(String UserName){
		mUserName = UserName;
	}

	public String getPrivateKey(){
		return mPrivateKey;
	}
	
	public void setPrivateKey(String PrivateKey){
		mPrivateKey = PrivateKey;
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
}
