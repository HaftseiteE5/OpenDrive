package com.opendrive.android.datamodel;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import com.opendrive.android.common.Utils;

public class FileData implements Parcelable {

    private String mAccessDirID = "0";
    private FileData mParent = null;

    private String mParentID = "";
    private boolean mIsSharedFolders = false;
    private String mUserID = "";
    private String mShareUserID = "";
    private boolean showActionMenu = false;
    private boolean mIsFolder = false;

    // 	For Directory
    private String mID = "";
    private String mFolderID = "";
    private String mName = "";
    private String mDescription = "";
    private boolean mIsHomeFolder = false;
    private boolean mIsDeleteable = false;
    private boolean mIsRenameable = true;
    private boolean mIsGallery = false;
    private boolean mIsMP3 = false;
    private String mPermission = "";
    private String mAccess = "";
    private boolean mHasSubfolders = false;
    private boolean mShared = false;
    private String mLink = "";
    private String mDateCreated = "";
    private String mDateDeleted = "";
    private Bitmap mIcon = null;
    private boolean mIsCheckedForMove = false;

    //	For File
    private String mFileId = "";
    private String mSize = "";
    private String mDate = "";
    private String mDateModified = "";
    private String mDateAccessed = "";
    private String mDirectLink = "";
    private String mStreamLink = "";
    private String mPrice = "";
    private String mDirectLinkPublic = "";
    private boolean mIsDownloaded = false;

    public FileData(Parcel in) {
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        mAccessDirID = in.readString();
        mParent = in.readParcelable(FileData.class.getClassLoader());
        mParentID = in.readString();
        mIsSharedFolders = in.readByte() == 1 ? true : false;
        mUserID = in.readString();
        mShareUserID = in.readString();
        showActionMenu = in.readByte() == 1 ? true : false;
        mIsFolder = in.readByte() == 1 ? true : false;
        mID = in.readString();
        mFolderID = in.readString();
        mName = in.readString();
        mDescription = in.readString();
        mIsHomeFolder = in.readByte() == 1 ? true : false;
        mIsDeleteable = in.readByte() == 1 ? true : false;
        mIsRenameable = in.readByte() == 1 ? true : false;
        mIsGallery = in.readByte() == 1 ? true : false;
        mIsMP3 = in.readByte() == 1 ? true : false;
        mPermission = in.readString();
        mAccess = in.readString();
        mHasSubfolders = in.readByte() == 1 ? true : false;
        mShared = in.readByte() == 1 ? true : false;
        mLink = in.readString();
        mDateCreated = in.readString();
        mDateDeleted = in.readString();
        //mIcon =       in.re();
        mFileId = in.readString();
        mSize = in.readString();
        mDate = in.readString();
        mDateModified = in.readString();
        mDateAccessed = in.readString();
        mDirectLink = in.readString();
        mStreamLink = in.readString();
        mPrice = in.readString();
        mDirectLinkPublic = in.readString();
    }

    public String getParentID() {
        return this.mParentID;
    }

    public void setParentID(String ParentID) {
        this.mParentID = ParentID;
    }


    public String getShareUserID() {
        return this.mShareUserID;
    }

    public void setShareUserID(String ShareUserID) {
        this.mShareUserID = ShareUserID;
    }

    public String getUserID() {
        return this.mUserID;
    }

    public void setUserID(String userID) {
        this.mUserID = userID;
    }

    public String getAccessDirID() {
        return mAccessDirID;
    }

    public void setAccessDirID(String accessDirID) {
        this.mAccessDirID = accessDirID;
    }

    public FileData getParent() {
        return mParent;
    }
    
    public void setParent(FileData parent) {
        this.mParent = parent;
    }

    public boolean getIsSharedFolders() {
        return this.mIsSharedFolders;
    }

    public void setIsSharedFolders(boolean isSharedFolders) {
        this.mIsSharedFolders = isSharedFolders;
    }

    public FileData(String accessDirID, FileData parent) {
        // TODO Auto-generated constructor stub
        mAccessDirID = accessDirID;
        mParent = parent;

        if (parent == null)
            mParentID = "0";
        else
            mParentID = parent.getID();
    }

    public FileData() {
        // TODO Auto-generated constructor stub
    }

    public boolean getIsFolder() {
        return mIsFolder;
    }

    public void setIsFolder(boolean IsFolder) {
        mIsFolder = IsFolder;
    }

    public String getID() {
        return mID;
    }

    public void setID(String ID) {
        mID = ID;
    }

    public String getFolderID() {
        return mFolderID;
    }

    public void setFolderID(String FolderID) {
        mFolderID = FolderID;
    }

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

    public boolean getIsHomeFolder() {
        return mIsHomeFolder;
    }

    public void setIsHomeFolder(String IsHomeFolder) {
        mIsHomeFolder = Boolean.parseBoolean(IsHomeFolder);
    }

    public boolean getIsDeleteable() {
        return mIsDeleteable;
    }

    public void setIsDeleteable(String IsDeleteable) {
        mIsDeleteable = Boolean.parseBoolean(IsDeleteable);
    }

    public boolean getIsRenameable() {
        return mIsRenameable;
    }

    public void setIsRenameable(String IsRenameable) {
        mIsRenameable = Boolean.parseBoolean(IsRenameable);
    }

    public boolean getIsGallery() {
        return mIsGallery;
    }

    public void setIsGallery(String IsGallery) {
        mIsGallery = Boolean.parseBoolean(IsGallery);
    }

    public boolean getIsMP3() {
        return mIsMP3;
    }

    public void setIsMP3(String IsMP3) {
        mIsMP3 = Boolean.parseBoolean(IsMP3);
    }

    public String getPermission() {
        return mPermission;
    }

    public void setPermission(String Permission) {
        mPermission = Permission;
    }

    public String getAccess() {
        return mAccess;
    }

    public void setAccess(String Access) {
        mAccess = Access;
    }

    public boolean getHasSubfolders() {
        return mHasSubfolders;
    }

    public void setHasSubfolders(String HasSubfolders) {
        mHasSubfolders = Boolean.parseBoolean(HasSubfolders);
    }

    public boolean getShared() {
        return mShared;
    }

    public void setShared(String Shared) {
        mShared = Boolean.parseBoolean(Shared);
    }

    public String getLink() {
        return mLink;
    }

    public void setLink(String Link) {
        mLink = Link;
    }

    public String getDateCreated() {
        return mDateCreated;
    }

    public void setDateCreated(String DateCreated) {
        mDateCreated = Utils.getDateFromTimeStamp(DateCreated);
    }

    public void setDateCreatedFromDB(String DateCreated) {
        mDateCreated = DateCreated;
    }

    public String getDateDeleted() {
        return mDateDeleted;
    }

    public void setDateDeleted(String DateDeleted) {
        mDateDeleted = DateDeleted;
    }

    public String getFileId() {
        return mFileId;
    }

    public void setFileId(String FileId) {
        mFileId = FileId;
    }

    public String getSize() {
        return mSize;
    }

    public void setSize(String Size) {
        long fileSize = Long.parseLong(Size);
        mSize = Utils.getStringFileSize(fileSize);
    }

    public void setSizeFromDB(String Size) {
        mSize = Size;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String Date) {
        mDate = Utils.getDateFromTimeStamp(Date);
    }

    public void setDateFromDB(String Date) {
        mDate = Date;
    }

    public String getDateModified() {
        return mDateModified;
    }

    public void setDateModified(String DateModified) {
        mDateModified = DateModified;
    }

    public String getDateAccessed() {
        return mDateAccessed;
    }

    public void setDateAccessed(String DateAccessed) {
        mDateAccessed = DateAccessed;
    }

    public String getDirectLink() {
        return mDirectLink;
    }

    public void setDirectLink(String DirectLink) {
        mDirectLink = DirectLink;
    }

    public String getStreamLink() {
        return mStreamLink;
    }

    public void setStreamLink(String StreamLink) {
        mStreamLink = StreamLink;
    }

    public String getPrice() {
        return mPrice;
    }

    public void setPrice(String Price) {
        mPrice = Price;
    }

    public Bitmap getIcon() {
        return mIcon;
    }

    public void setIcon(Bitmap aIcon) {
        mIcon = aIcon;
    }

    public String getDirectLinkPublic() {
        return mDirectLinkPublic;
    }

    public void setDirectLinkPublic(String DirectLinkPublic) {
        mDirectLinkPublic = DirectLinkPublic;
    }

    public boolean isShowActionMenu() {
        return showActionMenu;
    }

    public void setShowActionMenu(boolean showActionMenu) {
        this.showActionMenu = showActionMenu;
    }

    public String getPath() {

        String path = "";
        if (this.getParent() != null) {
            path = this.getParent().getPath();
        }

        if (path.length() == 0)
            path += this.getName();
        else
            path += (" > " + this.getName());

        return path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(mAccessDirID);
        dest.writeParcelable(mParent, i);
        dest.writeString(mParentID);
        dest.writeByte(mIsSharedFolders ? (byte) 1 : (byte) 0);
        dest.writeString(mUserID);
        dest.writeString(mShareUserID);
        dest.writeByte(showActionMenu ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsFolder ? (byte) 1 : (byte) 0);
        dest.writeString(mID);
        dest.writeString(mFolderID);
        dest.writeString(mName);
        dest.writeString(mDescription);
        dest.writeByte(mIsHomeFolder ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsDeleteable ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsRenameable ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsGallery ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsMP3 ? (byte) 1 : (byte) 0);
        dest.writeString(mPermission);
        dest.writeString(mAccess);
        dest.writeByte(mHasSubfolders ? (byte) 1 : (byte) 0);
        dest.writeByte(mShared ? (byte) 1 : (byte) 0);
        dest.writeString(mLink);
        dest.writeString(mDateCreated);
        dest.writeString(mDateDeleted);
//mIcondest.writeString(=       i                    );
        dest.writeString(mFileId);
        dest.writeString(mSize);
        dest.writeString(mDate);
        dest.writeString(mDateModified);
        dest.writeString(mDateAccessed);
        dest.writeString(mDirectLink);
        dest.writeString(mStreamLink);
        dest.writeString(mPrice);
        dest.writeString(mDirectLinkPublic);
    }

    public boolean isCheckedForMove() {
	return mIsCheckedForMove;
    }

    public void setCheckedForMove(boolean mIsCheckedForMove) {
	this.mIsCheckedForMove = mIsCheckedForMove;
    }

    public boolean isDownloaded() {
	return mIsDownloaded;
    }

    public void setDownloaded(boolean mIsDownloaded) {
	this.mIsDownloaded = mIsDownloaded;
    }

    public static final Parcelable.Creator<FileData> CREATOR = new Parcelable.Creator<FileData>() {
        public FileData createFromParcel(Parcel in) {
            return new FileData(in);
        }

        public FileData[] newArray(int size) {
            return new FileData[size];
        }
    };
}
