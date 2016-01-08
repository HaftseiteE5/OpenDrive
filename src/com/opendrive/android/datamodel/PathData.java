package com.opendrive.android.datamodel;

public class PathData {

	public PathData() {
		// TODO Auto-generated constructor stub
	}
	
	private String mPath = "";
	private String mName = "";
	private String mDescription = "";
	
	public String getPath(){
		return mPath;
	}
	
	public void setPath(String path){
		mPath = path;
	}
	
	public String getName(){
		return mName;
	}
	
	public void setName(String name){
		mName = name;
	}
	
	public String getDescription(){
		return mDescription;
	}
	
	public void setDescription(String description){
		mDescription = description;
	}
}
