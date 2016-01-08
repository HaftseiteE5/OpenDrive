package com.opendrive.android.datamodel;

public class DeleteFileData {

	public DeleteFileData() {
		// TODO Auto-generated constructor stub
	}
	
	private String mName = "";
	private String mDescription = "";
	
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
