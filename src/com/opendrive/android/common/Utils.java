package com.opendrive.android.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.DrawableContainer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.opendrive.android.R;
import com.opendrive.android.datamodel.CreateFileResultData;
import com.opendrive.android.datamodel.FileData;
import com.opendrive.android.db.DataBaseAdapter;
import com.opendrive.android.service.AutoUploadService;
import com.opendrive.android.ui.LogIn;
import com.opendrive.android.ui.fragment.FilesFragment;

public class Utils {

    public static Context appContext = null;
    private static AlertDialog.Builder validationAlertDialog = null;

    public static String urlEncode(String url) {
        String encodedURL = null;
        try {
            encodedURL = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return encodedURL;
    }

    public static void initAppContext(Context context) {
        appContext = context;
    }

    public static void initValidationAlertDialog() {

        if (validationAlertDialog == null)
            validationAlertDialog = new AlertDialog.Builder(appContext);
        validationAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
        validationAlertDialog.setTitle(R.string.txt_warning);
        validationAlertDialog.setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

                return;
            }
        });
    }

    public static void showValidationAlertDialog(String errorString) {
        validationAlertDialog.setMessage(errorString);
        validationAlertDialog.show();
    }

    public static void showValidationAlertDialog(String title, String errorString) {
        validationAlertDialog.setTitle(title);
        validationAlertDialog.setMessage(errorString);
        validationAlertDialog.show();
    }

    public static String getStringFileSize(long lFileSize) {
        String strFileSize = lFileSize + " Byte";

        if (lFileSize > 1024) {
            double dFileSize = (double) lFileSize / 1024;

            if ((dFileSize / 1024) >= 1024) {
                dFileSize = dFileSize / (1024 * 1024);
                String strSize = String.valueOf(Math.round(dFileSize * 100));

                int dotPos = strSize.length() - 2;
                strFileSize = strSize.substring(0, dotPos) + "." + strSize.substring(dotPos) + " GB";
            } else if (dFileSize > 1024) {
                dFileSize = dFileSize / 1024;
                String strSize = String.valueOf(Math.round(dFileSize * 100));

                int dotPos = strSize.length() - 2;
                strFileSize = strSize.substring(0, dotPos) + "." + strSize.substring(dotPos) + " MB";
            } else {
                String strSize = String.valueOf(Math.round(dFileSize * 100));

                int dotPos = strSize.length() - 2;
                strFileSize = strSize.substring(0, dotPos) + "." + strSize.substring(dotPos) + " KB";
            }
        }

        return strFileSize;
    }

    public static String getDateFromTimeStamp(String timeStamp) {

        long longTimeStmp = Long.parseLong(timeStamp);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(longTimeStmp);
        Date d = new Date(longTimeStmp * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        return sdf.format(d);
    }

    public static String getTimeStamp() {
        Date now = new Date();
        long currentTime = now.getTime();
        String strCurrentTime = currentTime / 1000 + "";
        return strCurrentTime;
    }

    public static long fromDateToMilliseconds(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
        Date parseDate = formatter.parse(date);
        return parseDate.getTime();
    }

    public static void saveIcon(String iconName, byte[] iconContent) {
        try {
            String iconFullPath = Constant.ICON_THUMBNAIL_PATH + iconName;

            File iconFile = new File(iconFullPath);

            if (iconFile.exists())
                iconFile.delete();

            iconFile.createNewFile();

            FileOutputStream fos = new FileOutputStream(iconFile, true);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            bos.write(iconContent, 0, iconContent.length);
            bos.flush();
            fos.flush();

            bos.close();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    int IMAGE_MAX_SIZE = 800;

    public static Bitmap getIconFromFileID(String fileID, boolean compressed) {
        File iconFile = new File(Constant.ICON_THUMBNAIL_PATH + fileID + ".jpg");

        if (iconFile.exists()) {

            long len_kb = iconFile.length() / 1024;
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inDither = true;
            if (compressed) {
                if (len_kb > 1000) {
                    opt.inSampleSize = 32;
                } else if (len_kb > 500) {
                    opt.inSampleSize = 16;
                } else {
                    opt.inSampleSize = 4;
                }
            }
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap iconBitmap = BitmapFactory.decodeFile(Constant.ICON_THUMBNAIL_PATH + fileID + ".jpg", opt);
            return iconBitmap;
        } else {
            return null;
        }
    }

    public static void deleteFilesAndDB(Context context) {

        DataBaseAdapter mDataBaseAdapter = null;
        mDataBaseAdapter = new DataBaseAdapter(context);
        if (mDataBaseAdapter.openDatabase()) {
            mDataBaseAdapter.drop();
        }

        SharedPreferences settingPrefer = context.getSharedPreferences(Constant.PREFS_NAME, 0);
        final SharedPreferences.Editor editor = settingPrefer.edit();
        editor.putString("IsExistDB", "0");
        editor.commit();

        Utils.deleteFolders(Constant.FOLDER_PATH);
    }

    public static void deleteFolders(String dirPath) {
        File deletedFolder = new File(dirPath);

        if (!deletedFolder.exists())
            return;

        File[] fileList = deletedFolder.listFiles();

        if (fileList != null) {
            int fileCount = fileList.length;
            for (int i = 0; i < fileCount; i++) {
                File tmpFile = fileList[i];
                if (tmpFile.isDirectory())
                    deleteFolders(tmpFile.getAbsolutePath());
                else
                    tmpFile.delete();
            }

        }

        deletedFolder.delete();
    }

    public static void makeFolders(FileData fileItem) {

        String folderFullPath = getFolderFullPath(fileItem);

        makeFolders(folderFullPath);

    }

    public static void makeFolders(String folderFullPath) {

        String[] folders = folderFullPath.split("/");
        String folderPath = "/sdcard";
        for (int i = 2; i < folders.length; i++) {
            if (folders[i].length() > 0) {
                folderPath = folderPath + "/" + folders[i];
                File tmpFile = new File(folderPath);
                tmpFile.mkdir();
            }
        }

    }

    public static String getFolderFullPath(FileData fileItem) {
        String fileFullPath = "";

        FileData parentFileItem = fileItem.getParent();

        if (parentFileItem != null) {
            if (fileFullPath.length() == 0)
                fileFullPath = getFolderFullPath(parentFileItem) + fileItem.getName();
            else
                fileFullPath = getFolderFullPath(parentFileItem) + fileItem.getName() + "/" + fileFullPath;
        } else {
            fileFullPath = Constant.FOLDER_PATH + fileItem.getName();
        }
        System.out.println("fileFullPath" + fileFullPath);
        String dir = "";
        if (fileItem.getIsFolder()) {
            dir = "/";
        }

        return fileFullPath + dir;

    }

    public static String getFolderFullPath(ArrayList<FileData> allFileData, FileData fileItem) {

        String fileFullPath = "";

        FileData parentFileItem = null;
        FileData tempFileItem = null;

        if (!fileItem.getID().equals("0") && !fileItem.getID().equals("")) {

            int count = allFileData.size();

            for (int i = 0; i < count; i++) {

                tempFileItem = allFileData.get(i);

                if (tempFileItem.getID().equals(fileItem.getParentID())) {
                    parentFileItem = tempFileItem;
                    break;
                }
            }
        }

        if (parentFileItem != null) {

            if (fileFullPath.length() == 0)
                fileFullPath = getFolderFullPath(allFileData, parentFileItem) + fileItem.getName() + "/";
            else
                fileFullPath = getFolderFullPath(allFileData, parentFileItem) + fileItem.getName() + "/" + fileFullPath;
        } else {
            fileFullPath = Constant.FOLDER_PATH + fileItem.getName() + "/" + fileFullPath;
        }

        return fileFullPath;
    }

    /*
     * Method to implement checking disk free space
     * 
     * @param no
     * 
     * @return 0 if SDcard is unmounted, 1 if free space is less than file size(byte), 2 if free space is enough.
     */

    public static int checkStorage(long fileSize) {

        // check available sdcard
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // get free size from sdcard
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long bytesAvailable = (long) stat.getFreeBlocks() * (long) stat.getBlockSize();
            // long megAvailable = bytesAvailable / 1048576;
            if (bytesAvailable < fileSize) {
                return 1;
            }

            return 2;
        }
        return 0;
    }

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension; null if uri was null.
     */
    public static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    public static String getFileName(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(0, dot);
        } else {
            return uri;
        }
    }

    public static String getConvertedString(String normalString) {
        String convertedString = normalString.replace("&", Constant.AMPString);
        convertedString = convertedString.replace("\"", Constant.QUOTString);
        convertedString = convertedString.replace("'", Constant.APOSString);
        return convertedString;
    }

    public static String getRevertedString(String convertedString) {
        String revertedString = convertedString.replace(Constant.AMPString, "&");
        revertedString = revertedString.replace(Constant.QUOTString, "\"");
        revertedString = revertedString.replace(Constant.APOSString, "'");
        return revertedString;
    }

    public static boolean checkIsIntentCanBeHandled(Context context, Intent intent) {
        if (context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNetConeccted(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static boolean isWifiConnected(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
    }

    public static void unbindDrawables(View view) {
        if (view == null) {
            return;
        }

        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }

	/*
     * if(view instanceof ImageView) { ((BitmapDrawable) ((ImageView) view).getDrawable()).getBitmap().recycle(); }
	 */

        try {
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                ((ViewGroup) view).removeAllViews();
            }
        } catch (Exception e) {

        }
    }

    public static void showSoftInput(Context context, EditText editText) {
        if (context != null) {
            InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(editText, 0);
        }
    }

    public static void hideSoftInput(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static String getAccessStringByCode(Context context, String accessCode) {

        String result = "";

        if (accessCode.equals(Constant.ACCESS_PUBLIC)) {
            result = context.getString(R.string.access_public);
        } else if (accessCode.equals(Constant.ACCESS_HIDDEN)) {
            result = context.getString(R.string.access_hidden);
        } else if (accessCode.equals(Constant.ACCESS_PRIVATE)) {
            result = context.getString(R.string.access_private);
        }

        return result;
    }

    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    public static final Typeface getTypeface(Context c, String assetPath) {
        synchronized (cache) {
            if (!cache.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(c.getAssets(), assetPath);
                    cache.put(assetPath, t);
                } catch (Exception e) {
                    return null;
                }
            }
            return cache.get(assetPath);
        }
    }

    public static final void overrideFonts(Context context, View v) {
        Typeface typefaceRobotoReg = getTypeface(context, "fonts/roboto_regular.ttf");

        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child);
                }
            } else if (v instanceof EditText) {
                ((EditText) v).setTypeface(typefaceRobotoReg);
            } else if (v instanceof TextView) {
                ((TextView) v).setTypeface(typefaceRobotoReg);
            }
        } catch (Exception e) {
        }
    }

    public static void saveUploadFolderId(Context context, String id) {
        context.getSharedPreferences(Constant.PREFS_NAME, 0).edit().putString(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.SELECTED_FOLDER_ID, id).commit();
    }

    public static String getUploadFolderId(Context context) {
        return context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.SELECTED_FOLDER_ID, getDefaultAutoUploadFolderId(context));
    }

    public static int getLastDevicePhotoId(Context context) {
        final String[] columns = {MediaStore.Images.Media._ID};
        final String orderBy = MediaStore.Images.Media._ID + " DESC";
        final String where = null;
        final String[] whereArguments = null;
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, where, whereArguments, orderBy);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            cursor.close();
            return id;
        } else {
            return 0;
        }
    }

    public static int getLastDeviceVideoId(Context context) {
        final String[] columns = {MediaStore.Video.Media._ID};
        final String orderBy = MediaStore.Video.Media._ID + " DESC";
        final String where = null;
        final String[] whereArguments = null;
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, where, whereArguments, orderBy);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
            cursor.close();
            return id;
        } else {
            return 0;
        }
    }

    public static int getLastUploadedPhotoId(Context context) {
        return context.getSharedPreferences(Constant.PREFS_NAME, 0).getInt(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.LAST_UPLOADED_PHOTO_ID, getLastDevicePhotoId(context));
    }

    public static void saveLastUploadedPhotoId(Context context, int id) {
        context.getSharedPreferences(Constant.PREFS_NAME, 0).edit().putInt(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.LAST_UPLOADED_PHOTO_ID, id).commit();
    }

    public static int getLastUploadedVideoId(Context context) {
        return context.getSharedPreferences(Constant.PREFS_NAME, 0).getInt(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.LAST_UPLOADED_VIDEO_ID, getLastDeviceVideoId(context));
    }

    public static void saveLastUploadedVideoId(Context context, int id) {
        context.getSharedPreferences(Constant.PREFS_NAME, 0).edit().putInt(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.LAST_UPLOADED_VIDEO_ID, id).commit();
    }

    public static int getAutoUploadOption(Context context) {
        return context.getSharedPreferences(Constant.PREFS_NAME, 0).getInt(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.AUTO_UPLOAD_OPTION, Constant.WI_FI_ONLY);
    }

    public static void saveAutoUploadOption(Context context, int option) {
        context.getSharedPreferences(Constant.PREFS_NAME, 0).edit().putInt(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.AUTO_UPLOAD_OPTION, option).commit();
    }

    public static boolean getAutoUploadIsEnabled(Context context) {
        return context.getSharedPreferences(Constant.PREFS_NAME, 0).getBoolean(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.AUTO_UPLOAD_ENABLED, false);
    }

    public static void saveAutoUploadIsEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(Constant.PREFS_NAME, 0).edit().putBoolean(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.AUTO_UPLOAD_ENABLED, enabled).commit();
    }

    public static HashMap<Integer, String> getAllLastPhotoPaths(Context context, int lastUploadedPhotoId) {
        HashMap<Integer, String> allPhotoPaths = new HashMap<Integer, String>();
        final String[] columns = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
        final String orderBy = MediaStore.Images.Media._ID;
        final String where = MediaStore.Images.Media._ID + " > ?";
        final String[] whereArguments = {String.valueOf(lastUploadedPhotoId)};
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, where, whereArguments, orderBy);
        while (cursor.moveToNext()) {
            allPhotoPaths.put(cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID)), cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
        }
        cursor.close();
        return allPhotoPaths;
    }

    public static HashMap<Integer, String> getAllLastVideoPaths(Context context, int lastUploadedVideoId) {
        HashMap<Integer, String> allVideoPaths = new HashMap<Integer, String>();
        final String[] columns = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA};
        final String orderBy = MediaStore.Video.Media._ID;
        final String where = MediaStore.Video.Media._ID + " > ?";
        final String[] whereArguments = {String.valueOf(lastUploadedVideoId)};
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, where, whereArguments, orderBy);
        while (cursor.moveToNext()) {
            allVideoPaths.put(cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID)), cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
        }
        cursor.close();
        return allVideoPaths;
    }

    public static boolean getPassCodeTurned(Context context) {
        return context.getSharedPreferences(Constant.PREFS_NAME, 0).getBoolean(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.PASS_CODE_TURNED, false);
    }

    public static void savePassCodeTurned(Context context, boolean turned) {
        context.getSharedPreferences(Constant.PREFS_NAME, 0).edit().putBoolean(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.PASS_CODE_TURNED, turned).commit();
    }

    public static boolean getKeepLoggedIn(Context context) {
        return context.getSharedPreferences(Constant.PREFS_NAME, 0).getBoolean(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.KEEP_LOGGED_IN, true);
    }

    public static void saveKeepLoggedIn(Context context, boolean keep) {
        context.getSharedPreferences(Constant.PREFS_NAME, 0).edit().putBoolean(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.KEEP_LOGGED_IN, keep).commit();
    }

    public static boolean getEraseDataTurned(Context context) {
        return context.getSharedPreferences(Constant.PREFS_NAME, 0).getBoolean(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.ERASE_DATA, false);
    }

    public static void saveEraseDataTurned(Context context, boolean turned) {
        context.getSharedPreferences(Constant.PREFS_NAME, 0).edit().putBoolean(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.ERASE_DATA, turned).commit();
    }

    public static void savePassCode(Context context, String code) {
        context.getSharedPreferences(Constant.PREFS_NAME, 0).edit().putString(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.PASS_CODE, code).commit();
    }

    public static String getPassCode(Context context) {
        return context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(context.getSharedPreferences(Constant.PREFS_NAME, 0).getString(Constant.UserName_Key, "") + Constant.PASS_CODE, "");
    }

    public static boolean isServerErrorDirNotExists(CreateFileResultData data) {
        return data.getType().equals(Constant.DIR_NOT_EXISTS_ERROR_TYPE) && data.getName().equals(Constant.DIR_NOT_EXISTS_ERROR_NAME) && data.getDescription().equals(Constant.DIR_NOT_EXISTS_ERROR_DESC);
    }

    public static String getDefaultAutoUploadFolderId(Context context){
        if (LogIn.mLoginData != null && LogIn.mLoginData.getIsAccessUser().equals("False")){
            return "0";
        }
        else{
            DataBaseAdapter adapter = new DataBaseAdapter(context);
            adapter.openDatabase();
            String folderId = adapter.getFirstRootChildFolderId();
            adapter.closeDatabase();
            return folderId;
        }
    }

    public static String getDefaultAutoUploadFolderName(Context context){
        if (LogIn.mLoginData != null && LogIn.mLoginData.getIsAccessUser().equals("False")){
            return context.getString(R.string.folder_default);
        }
        else{
            DataBaseAdapter adapter = new DataBaseAdapter(context);
            adapter.openDatabase();
            String folderName = adapter.getFolderNameById(getDefaultAutoUploadFolderId(context));
            adapter.closeDatabase();
            return folderName;
        }
    }


    public static void longLog(String tag, String str) {
        if(str.length() > 4000) {
            Log.d(tag, str.substring(0, 4000));
            longLog(tag, str.substring(4000));
        } else
            Log.d(tag, str);
    }

    public static boolean isLocalFileNewer(FileData fileData){

        String fileFullPath = Utils.getFolderFullPath(fileData);
        File tmpFile = new File(fileFullPath);
        if (tmpFile.exists()) {
            long lastModified = tmpFile.lastModified();
            long serverModified = Long.parseLong(fileData.getDateModified()) * 1000;
            Log.d("test", fileData.getName() + " lastModified = " + lastModified + " serverModified = " + serverModified);
            return lastModified > serverModified;
        }

        return false;
    }
}
