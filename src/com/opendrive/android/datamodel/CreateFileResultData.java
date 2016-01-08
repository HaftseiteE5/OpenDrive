package com.opendrive.android.datamodel;

public class CreateFileResultData {

	public CreateFileResultData() {
		// TODO Auto-generated constructor stub
	}
	
	private String mID = "";
	private String mSize = "";
	private String mDateModified = "";
	private String mDateAccessed = "";
	private String mLink = "";
	private String mDirectLink = "";
	private String mStreamLink = "";
	private String mAccess = "";
	private String mPrice = "";
	private String mDateDeleted = "";
	private String mTempLocation = "";
	private String mSpeedLimit = "";
	
	private String mType = "";
	private String mName = "";
	private String mDescription = "";
	
	public String getID(){
		return mID;
	}
	
	public void setID(String ID){
		mID = ID;
	}

	public String getSize(){
		return mSize;
	}
	
	public void setSize(String Size){
		mSize = Size;
	}

	public String getDateModified(){
		return mDateModified;
	}
	
	public void setDateModified(String DateModified){
		mDateModified = DateModified;
	}

	public String getDateAccessed(){
		return mDateAccessed;
	}
	
	public void setDateAccessed(String DateAccessed){
		mDateAccessed = DateAccessed;
	}

	public String getLink(){
		return mLink;
	}
	
	public void setLink(String Link){
		mLink = Link;
	}

	public String getDirectLink(){
		return mDirectLink;
	}
	
	public void setDirectLink(String DirectLink){
		mDirectLink = DirectLink;
	}


	public String getStreamLink(){
		return mStreamLink;
	}
	
	public void setStreamLink(String StreamLink){
		mStreamLink = StreamLink;
	}

	public String getPrice(){
		return mPrice;
	}
	
	public void setPrice(String Price){
		mPrice = Price;
	}

	public String getAccess(){
		return mAccess;
	}
	
	public void setAccess(String Access){
		mAccess = Access;
	}
	
	public String getDateDeleted(){
		return mDateDeleted;
	}
	
	public void setDateDeleted(String DateDeleted){
		mDateDeleted = DateDeleted;
	}

	public String getTempLocation(){
		return mTempLocation;
	}
	
	public void setTempLocation(String TempLocation){
		mTempLocation = TempLocation;
	}

	public String getSpeedLimit(){
		return mSpeedLimit;
	}
	
	public void setSpeedLimit(String SpeedLimit){
		mSpeedLimit = SpeedLimit;
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
