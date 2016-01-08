package com.opendrive.android.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.FileData;
import com.opendrive.android.player.PlayerActivity;
import com.opendrive.android.player.Song;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

public class DataBaseAdapter extends SQLiteOpenHelper {

    public final static String TAG="DataBaseAdapter";

    public SQLiteDatabase mDatabase;

    public ArrayList<FileData> allFileDataList = null;

    private final static String CREATE_TABLE_QUERY = "create table " + Constant.TABLE_NAME + " (" + "id varchar(64) NOT NULL, " + "parent_id varchar(64), " + "AccessDirID text, " + "IsSharedFolders integer, " + "UserID text, " + "ShareUserID text, " + "IsFolder integer, " + "FolderID text, " + "Name text, " + "Description text, " + "IsHomeFolder integer, " + "IsDeleteable integer, " + "IsRenameable integer, " + "IsGallery integer, " + "IsMP3 integer, " + "Permission text, " + "Access text, " + "HasSubfolders integer, " + "Shared integer, " + "Link text, " + "DateCreated text, " + "DateDeleted text, " + "FileId text, " + "Size text, " + "Date text, " + "DateModified text, " + "DateAccessed text, " + "DirectLink text, " + "StreamLink text, " + "Price text, " + "DirectLinkPublic text, " + "primary key(id, parent_id)" + ")";

    private final static String SELECT_ALL_QUERY = "select * from " + Constant.TABLE_NAME;

    private final Cursor getSelectByNameStatement(String name, ArrayList<String> allParentIDs) {
        String[] args = allParentIDs.toArray(new String[allParentIDs.size()]);
        Cursor cursor = mDatabase.query(Constant.TABLE_NAME, null, "Name like '%" + name + "%' and DateDeleted = '0' and parent_id in(" + makePlaceholders(allParentIDs.size()) + ")", args, null, null, null);
        return cursor;

    }

    private String makePlaceholders(int len) {
        StringBuilder sb = new StringBuilder(len * 2 - 1);
        sb.append("?");
        for (int i = 1; i < len; i++) {
            sb.append(",?");
        }
        return sb.toString();

    }

    private final static String getAllChildrenFoldersIDsStatement(String parentID) {
        return "select id from " + Constant.TABLE_NAME + " where parent_id='" + parentID + "' and IsFolder = '1';";
    }

    private final static String INSERT_SQL = "insert into " + Constant.TABLE_NAME + " (id, parent_id, AccessDirID, IsSharedFolders, UserID, " + "ShareUserID, IsFolder, FolderID, Name, Description, " + "IsHomeFolder, IsDeleteable, IsRenameable, IsGallery, IsMP3," + "Permission, Access, HasSubfolders, Shared, Link,  DateCreated, " + "DateDeleted, FileId, Size, Date, DateModified, DateAccessed, DirectLink, " + "StreamLink, Price, DirectLinkPublic) " + "values ('%s', '%s', '%s', %d, '%s', '%s', %d, '%s', '%s', '%s', %d, %d, %d, %d, %d, '%s', '%s'," + " %d, %d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')";

    private final static String UPDATE_SQL = "update " + Constant.TABLE_NAME + " set id = '%s'," + "   parent_id = '%s'," + " 	AccessDirID = '%s'," + "	IsSharedFolders =  %d," + "	UserID = '%s'," + "	ShareUserID = '%s'," + " 	IsFolder = %d," + "	FolderID = '%s'," + "	Name = '%s'," + "	Description = '%s'," + "	IsHomeFolder = %d," + "	IsDeleteable = %d," + "	IsRenameable = %d," + "	IsGallery = %d," + "	IsMP3 = %d," + "	Permission = '%s'," + "	Access = '%s'," + "	HasSubfolders = %d," + "	Shared = %d," + "	Link = '%s'," + "	DateCreated = '%s'," + "	DateDeleted = '%s'," + "	FileId = '%s'," + "	Size = '%s'," + "	Date = '%s'," + "	DateModified = '%s'," + "	DateAccessed = '%s'," + "	DirectLink = '%s'," + "	StreamLink = '%s'," + "	Price = '%s'," + "	DirectLinkPublic = '%s' where %s = '%s' and %s = '%s'";

    public DataBaseAdapter(Context context) {
        super(context, Constant.DATABASE_NAME, null, Constant.DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mDatabase.execSQL(CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constant.TABLE_NAME);
        onCreate(db);
    }

    public void createDB() {

        mDatabase.execSQL(CREATE_TABLE_QUERY);
    }

    public void drop() {
        mDatabase.execSQL("DROP TABLE IF EXISTS " + Constant.TABLE_NAME);
        mDatabase.execSQL(CREATE_TABLE_QUERY);
    }

    public void update(FileData fileData) {

        String query = String.format(UPDATE_SQL, fileData.getID(), fileData.getParentID(), fileData.getAccessDirID(), fileData.getIsSharedFolders() ? 1 : 0, fileData.getUserID(), fileData.getShareUserID(), fileData.getIsFolder() ? 1 : 0, fileData.getFolderID(), fileData.getName().replace("'", "####"), fileData.getDescription().replace("'", "####"), fileData.getIsHomeFolder() ? 1 : 0, fileData.getIsDeleteable() ? 1 : 0, fileData.getIsRenameable() ? 1 : 0, fileData.getIsGallery() ? 1 : 0, fileData.getIsMP3() ? 1 : 0, fileData.getPermission(), fileData.getAccess(), fileData.getHasSubfolders() ? 1 : 0, fileData.getShared() ? 1 : 0, fileData.getLink(), fileData.getDateCreated(), fileData.getDateDeleted(), fileData.getFileId(), fileData.getSize(), fileData.getDate(), fileData.getDateModified(), fileData.getDateAccessed(), fileData.getDirectLink().replace("'", "####"), fileData.getStreamLink().replace("'", "####"), fileData.getPrice(), fileData.getDirectLinkPublic().replace("'", "####"), "id", fileData.getID(), "parent_id", fileData.getParentID());
        try {
            mDatabase.execSQL(query);
        } catch (SQLiteConstraintException e1) {

        }
    }

    public void updateWithNewParent(FileData fileData, String newParentID) {

        String query = String.format(UPDATE_SQL, fileData.getID(), newParentID, fileData.getAccessDirID(), fileData.getIsSharedFolders() ? 1 : 0, fileData.getUserID(), fileData.getShareUserID(), fileData.getIsFolder() ? 1 : 0, fileData.getFolderID(), fileData.getName().replace("'", "####"), fileData.getDescription().replace("'", "####"), fileData.getIsHomeFolder() ? 1 : 0, fileData.getIsDeleteable() ? 1 : 0, fileData.getIsRenameable() ? 1 : 0, fileData.getIsGallery() ? 1 : 0, fileData.getIsMP3() ? 1 : 0, fileData.getPermission(), fileData.getAccess(), fileData.getHasSubfolders() ? 1 : 0, fileData.getShared() ? 1 : 0, fileData.getLink(), fileData.getDateCreated(), fileData.getDateDeleted(), fileData.getFileId(), fileData.getSize(), fileData.getDate(), fileData.getDateModified(), fileData.getDateAccessed(), fileData.getDirectLink().replace("'", "####"), fileData.getStreamLink().replace("'", "####"), fileData.getPrice(), fileData.getDirectLinkPublic().replace("'", "####"), "id", fileData.getID(), "parent_id", fileData.getParentID());
        try {
            mDatabase.execSQL(query);
        } catch (SQLiteConstraintException e1) {

        }
    }

    public void insert(ArrayList<FileData> fileDataArray) {

        for (int i = 0; i < fileDataArray.size(); i++) {

            FileData fileData = null;
            fileData = fileDataArray.get(i);

            String query1 = String.format("select * From %s where %s='%s' and %s='%s'", Constant.TABLE_NAME, "id", fileData.getID(), "parent_id", fileData.getParentID());

            Cursor cursor = mDatabase.rawQuery(query1, null);

            if (cursor != null && cursor.moveToFirst()) {
                cursor.moveToFirst();
                update(fileData);
                cursor.close();
                continue;
            }

            Log.i("insert", fileData.getID() + ": " + fileData.getParentID());
            String query = String.format(INSERT_SQL, fileData.getID(), fileData.getParentID(), fileData.getAccessDirID(), fileData.getIsSharedFolders() ? 1 : 0, fileData.getUserID(), fileData.getShareUserID(), fileData.getIsFolder() ? 1 : 0, fileData.getFolderID(), fileData.getName().replace("'", "####"), fileData.getDescription().replace("'", "####"), fileData.getIsHomeFolder() ? 1 : 0, fileData.getIsDeleteable() ? 1 : 0, fileData.getIsRenameable() ? 1 : 0, fileData.getIsGallery() ? 1 : 0, fileData.getIsMP3() ? 1 : 0, fileData.getPermission(), fileData.getAccess(), fileData.getHasSubfolders() ? 1 : 0, fileData.getShared() ? 1 : 0, fileData.getLink(), fileData.getDateCreated(), fileData.getDateDeleted(), fileData.getFileId(), fileData.getSize(), fileData.getDate(), fileData.getDateModified(), fileData.getDateAccessed(), fileData.getDirectLink().replace("'", "####"), fileData.getStreamLink().replace("'", "####"), fileData.getPrice(), fileData.getDirectLinkPublic().replace("'", "####"));

            try {
                mDatabase.execSQL(query);
            } catch (SQLiteConstraintException e1) {

            }
        }
    }

    public ArrayList<FileData> selectAll() {

        ArrayList<FileData> result = new ArrayList<FileData>();

        Cursor cursor = mDatabase.rawQuery(SELECT_ALL_QUERY, null);

        if (cursor == null || !cursor.moveToFirst()) {
            return result;
        }

        boolean bNextResult = false;

        do {
            FileData fileData = new FileData();

            fileData.setID(cursor.getString(0));
            fileData.setParentID(cursor.getString(1));
            fileData.setAccessDirID(cursor.getString(2));
            Log.i("allData", fileData.getID() + ": " + fileData.getParentID());
            if (cursor.getInt(3) == 1)
                fileData.setIsSharedFolders(true);
            else
                fileData.setIsSharedFolders(false);

            fileData.setUserID(cursor.getString(4));
            fileData.setShareUserID(cursor.getString(5));

            if (cursor.getInt(6) == 1)
                fileData.setIsFolder(true);
            else
                fileData.setIsFolder(false);

            fileData.setFolderID(cursor.getString(7));
            fileData.setName(cursor.getString(8).replace("####", "'"));
            fileData.setDescription(cursor.getString(9).replace("####", "'"));

            if (cursor.getInt(10) == 1)
                fileData.setIsHomeFolder("True");
            else
                fileData.setIsHomeFolder("False");

            if (cursor.getInt(11) == 1)
                fileData.setIsDeleteable("True");
            else
                fileData.setIsDeleteable("False");

            if (cursor.getInt(12) == 1)
                fileData.setIsRenameable("True");
            else
                fileData.setIsRenameable("False");

            if (cursor.getInt(13) == 1)
                fileData.setIsGallery("True");
            else
                fileData.setIsGallery("False");

            if (cursor.getInt(14) == 1)
                fileData.setIsMP3("True");
            else
                fileData.setIsMP3("False");

            fileData.setPermission(cursor.getString(15));
            fileData.setAccess(cursor.getString(16));

            if (cursor.getInt(17) == 1)
                fileData.setHasSubfolders("True");
            else
                fileData.setHasSubfolders("False");

            if (cursor.getInt(18) == 1)
                fileData.setShared("True");
            else
                fileData.setShared("False");

            fileData.setLink(cursor.getString(19));
            fileData.setDateCreatedFromDB(cursor.getString(20));
            fileData.setDateDeleted(cursor.getString(21));
            fileData.setFileId(cursor.getString(22));
            fileData.setSizeFromDB(cursor.getString(23));
            fileData.setDateFromDB(cursor.getString(24));
            fileData.setDateModified(cursor.getString(25));
            fileData.setDateAccessed(cursor.getString(26));
            fileData.setDirectLink(cursor.getString(27).replace("####", "'"));
            fileData.setStreamLink(cursor.getString(28).replace("####", "'"));
            fileData.setPrice(cursor.getString(29));
            fileData.setDirectLinkPublic(cursor.getString(30).replace("####", "'"));

            result.add(fileData);

            bNextResult = cursor.moveToNext();

        } while (bNextResult);

        cursor.close();

        allFileDataList = result;

        return result;

    }

    public FileData selectFileData(String id) {

        String query = String.format("select * From %s where %s='%s'", Constant.TABLE_NAME, "id", id);

        Cursor cursor = mDatabase.rawQuery(query, null);

        FileData fileData = new FileData();

        if (cursor == null || !cursor.moveToFirst()) {
            return fileData;
        }

        fileData.setID(cursor.getString(0));
        fileData.setParentID(cursor.getString(1));
        fileData.setAccessDirID(cursor.getString(2));

        if (cursor.getInt(3) == 1)
            fileData.setIsSharedFolders(true);
        else
            fileData.setIsSharedFolders(false);

        fileData.setUserID(cursor.getString(4));
        fileData.setShareUserID(cursor.getString(5));

        if (cursor.getInt(6) == 1)
            fileData.setIsFolder(true);
        else
            fileData.setIsFolder(false);

        fileData.setFolderID(cursor.getString(7));
        fileData.setName(cursor.getString(8).replace("####", "'"));
        fileData.setDescription(cursor.getString(9).replace("####", "'"));

        if (cursor.getInt(10) == 1)
            fileData.setIsHomeFolder("True");
        else
            fileData.setIsHomeFolder("False");

        if (cursor.getInt(11) == 1)
            fileData.setIsDeleteable("True");
        else
            fileData.setIsDeleteable("False");

        if (cursor.getInt(12) == 1)
            fileData.setIsRenameable("True");
        else
            fileData.setIsRenameable("False");

        if (cursor.getInt(13) == 1)
            fileData.setIsGallery("True");
        else
            fileData.setIsGallery("False");

        if (cursor.getInt(14) == 1)
            fileData.setIsMP3("True");
        else
            fileData.setIsMP3("False");

        fileData.setPermission(cursor.getString(15));
        fileData.setAccess(cursor.getString(16));

        if (cursor.getInt(17) == 1)
            fileData.setHasSubfolders("True");
        else
            fileData.setHasSubfolders("False");

        if (cursor.getInt(18) == 1)
            fileData.setShared("True");
        else
            fileData.setShared("False");

        fileData.setLink(cursor.getString(19));
        fileData.setDateCreatedFromDB(cursor.getString(20));
        fileData.setDateDeleted(cursor.getString(21));
        fileData.setFileId(cursor.getString(22));
        fileData.setSizeFromDB(cursor.getString(23));
        fileData.setDateFromDB(cursor.getString(24));
        fileData.setDateModified(cursor.getString(25));
        fileData.setDateAccessed(cursor.getString(26));
        fileData.setDirectLink(cursor.getString(27).replace("####", "'"));
        fileData.setStreamLink(cursor.getString(28).replace("####", "'"));
        fileData.setPrice(cursor.getString(29));
        fileData.setDirectLinkPublic(cursor.getString(30).replace("####", "'"));
        cursor.close();

        return fileData;
    }

    public ArrayList<FileData> selectFileDataByFileName(String fileName, String parentID) {

        fileName = fileName.trim();

        ArrayList<FileData> result = new ArrayList<FileData>();

        ArrayList<String> allParentIDs = new ArrayList<String>();
        allParentIDs.add(parentID);
        ArrayList<String> childsIDs = getChildFoldersIDs(parentID);
        ArrayList<String> temp = new ArrayList<String>();
        ArrayList<String> childsOfChildIDs;

        if (!childsIDs.isEmpty()) {
            boolean hasChilds = !childsIDs.isEmpty();
            while (hasChilds) {
                for (String childID : childsIDs) {
                    allParentIDs.add(childID);
                    childsOfChildIDs = getChildFoldersIDs(childID);
                    if (!childsOfChildIDs.isEmpty()) {
                        temp.addAll(childsOfChildIDs);
                        childsOfChildIDs.clear();
                    }
                }
                childsIDs.clear();
                if (!temp.isEmpty()) {
                    childsIDs.addAll(temp);
                    temp.clear();
                }
                hasChilds = !childsIDs.isEmpty();
            }
        }

        Cursor cursor = getSelectByNameStatement(fileName, allParentIDs);

        if (cursor == null || !cursor.moveToFirst()) {
            return result;
        }

        boolean bNextResult = false;

        do {
            FileData fileData = new FileData();

            fileData.setID(cursor.getString(0));
            fileData.setParentID(cursor.getString(1));
            fileData.setAccessDirID(cursor.getString(2));
            Log.i("allData", fileData.getID() + ": " + fileData.getParentID());
            if (cursor.getInt(3) == 1)
                fileData.setIsSharedFolders(true);
            else
                fileData.setIsSharedFolders(false);

            fileData.setUserID(cursor.getString(4));
            fileData.setShareUserID(cursor.getString(5));

            if (cursor.getInt(6) == 1)
                fileData.setIsFolder(true);
            else
                fileData.setIsFolder(false);

            fileData.setFolderID(cursor.getString(7));
            fileData.setName(cursor.getString(8).replace("####", "'"));
            fileData.setDescription(cursor.getString(9).replace("####", "'"));

            if (cursor.getInt(10) == 1)
                fileData.setIsHomeFolder("True");
            else
                fileData.setIsHomeFolder("False");

            if (cursor.getInt(11) == 1)
                fileData.setIsDeleteable("True");
            else
                fileData.setIsDeleteable("False");

            if (cursor.getInt(12) == 1)
                fileData.setIsRenameable("True");
            else
                fileData.setIsRenameable("False");

            if (cursor.getInt(13) == 1)
                fileData.setIsGallery("True");
            else
                fileData.setIsGallery("False");

            if (cursor.getInt(14) == 1)
                fileData.setIsMP3("True");
            else
                fileData.setIsMP3("False");

            fileData.setPermission(cursor.getString(15));
            fileData.setAccess(cursor.getString(16));

            if (cursor.getInt(17) == 1)
                fileData.setHasSubfolders("True");
            else
                fileData.setHasSubfolders("False");

            if (cursor.getInt(18) == 1)
                fileData.setShared("True");
            else
                fileData.setShared("False");

            fileData.setLink(cursor.getString(19));
            fileData.setDateCreatedFromDB(cursor.getString(20));
            fileData.setDateDeleted(cursor.getString(21));
            fileData.setFileId(cursor.getString(22));
            fileData.setSizeFromDB(cursor.getString(23));
            fileData.setDateFromDB(cursor.getString(24));
            fileData.setDateModified(cursor.getString(25));
            fileData.setDateAccessed(cursor.getString(26));
            fileData.setDirectLink(cursor.getString(27).replace("####", "'"));
            fileData.setStreamLink(cursor.getString(28).replace("####", "'"));
            fileData.setPrice(cursor.getString(29));
            fileData.setDirectLinkPublic(cursor.getString(30).replace("####", "'"));

            result.add(fileData);

            bNextResult = cursor.moveToNext();

        } while (bNextResult);

        cursor.close();

        allFileDataList = result;

        return result;

    }

    private ArrayList<String> getChildFoldersIDs(String parentID) {
        ArrayList<String> result = new ArrayList<String>();

        Cursor childFoldersCursor = mDatabase.rawQuery(getAllChildrenFoldersIDsStatement(parentID), null);

        while (childFoldersCursor != null && childFoldersCursor.moveToNext()) {
            String childFolderID = childFoldersCursor.getString(0);
            result.add(childFolderID);
        }
        return result;
    }

    public boolean openDatabase() {
        if (mDatabase == null) {
            mDatabase = SQLiteDatabase.openDatabase(Constant.DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
        }
        else if(mDatabase != null && !mDatabase.isOpen()){
            mDatabase = SQLiteDatabase.openDatabase(Constant.DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
        }

        if (mDatabase == null)
            return false;

        return true;
    }

    public void closeDatabase() {
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    public String getFolderNameById(String id) {
        String folderName = null;
        Cursor cursor = null;
        try {
            cursor = mDatabase.rawQuery("SELECT Name FROM " + Constant.TABLE_NAME + " WHERE id  = " + id, null);
            if (cursor != null && cursor.moveToFirst()) {
                folderName = cursor.getString(cursor.getColumnIndex("Name"));
            }
        } catch (Exception e) {
        } finally {
            try {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
            } catch (Exception ex) {
            }
        }
        return folderName;
    }

    public String getFirstRootChildFolderId() {
        String folderId = null;
        Cursor cursor = null;
        try {
            cursor = mDatabase.rawQuery("SELECT id FROM " + Constant.TABLE_NAME + " WHERE parent_id  = 0", null);
            if (cursor != null && cursor.moveToFirst()) {
                folderId = cursor.getString(cursor.getColumnIndex("id"));
            }
        } catch (Exception e) {
        } finally {
            try {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
            } catch (Exception ex) {
            }
        }
        return folderId;
    }


    public ArrayList<Song> getPlayList(String fileId, String folderId) {
        ArrayList<Song> allSongs = new ArrayList<Song>();
        Cursor cursor = null;
        try {
            cursor = mDatabase.rawQuery("SELECT id, Name, StreamLink FROM " + Constant.TABLE_NAME + " WHERE parent_id  = " + folderId + " AND id != " + fileId + " AND Name LIKE '%.mp3'", null);
            while (cursor != null && cursor.moveToNext()) {
                Song song = new Song();
                String id = cursor.getString(cursor.getColumnIndex("id"));
                song.setTitle(cursor.getString(cursor.getColumnIndex("Name")));
                song.setUrl(cursor.getString(cursor.getColumnIndex("StreamLink")));
                FileData fileData = selectFileData(id);
                song.setFilePath(Utils.getFolderFullPath(fileData));
                allSongs.add(song);
            }
        } catch (Exception e) {
        } finally {
            try {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
            } catch (Exception ex) {
            }
        }
        return allSongs;
    }
}
