package com.opendrive.android.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.CreateFileResultData;
import com.opendrive.android.datamodel.FileData;
import com.opendrive.android.db.DataBaseAdapter;
import com.opendrive.android.parser.CreateFileResultDataParser;
import com.opendrive.android.request.Request;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class AutoUploadService extends IntentService {

    private static final String TAG = "AutoUploadService";

    private int mFilesCount;
    private int mFilesUploaded;
    private String mUploadFolderId;

    public static final String ACTION_UPLOAD_STARTED = "com.opendrive.android.service.AutoUploadService.upload_started";
    public static final String ACTION_UPLOAD_PROGRESS = "com.opendrive.android.service.AutoUploadService.upload_progress";
    public static final String ACTION_UPLOAD_FINISHED = "com.opendrive.android.service.AutoUploadService.upload_finished";
    public static final String EXTRA_FILES_COUNT = "files_count";
    public static final String EXTRA_FILES_UPLOADED = "files_uploaded";

    private DataBaseAdapter mDataBaseAdapter;

    public AutoUploadService() {
        super("");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mDataBaseAdapter = new DataBaseAdapter(this);
        mDataBaseAdapter.openDatabase();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        mFilesUploaded = 0;
        uploadAllLastMedia();
    }

    private void uploadAllLastMedia() {
        int lastDevicePhotoId = Utils.getLastDevicePhotoId(this);
        Log.d(TAG, "Last device photo id = " + lastDevicePhotoId);
        int lastUploadedPhotoId = Utils.getLastUploadedPhotoId(this);
        Log.d(TAG, "Last uploaded photo id = " + lastUploadedPhotoId);

        int lastDeviceVideoId = Utils.getLastDeviceVideoId(this);
        Log.d(TAG, "Last device video id = " + lastDeviceVideoId);
        int lastUploadedVideoId = Utils.getLastUploadedVideoId(this);
        Log.d(TAG, "Last uploaded video id = " + lastUploadedVideoId);

        if (lastDevicePhotoId > lastUploadedPhotoId || lastDeviceVideoId > lastUploadedVideoId) {
            mUploadFolderId = Utils.getUploadFolderId(this);
            FileData fileData = mDataBaseAdapter.selectFileData(mUploadFolderId);
            String uploadFolderFullPath = Utils.getFolderFullPath(fileData);

            HashMap<Integer, String> allPhotoPaths = Utils.getAllLastPhotoPaths(this, lastUploadedPhotoId);
            HashMap<Integer, String> allVideoPaths = Utils.getAllLastVideoPaths(this, lastUploadedVideoId);
            mFilesCount = allPhotoPaths.size() + allVideoPaths.size();

            Intent uploadStartedIntent = new Intent(ACTION_UPLOAD_STARTED);
            uploadStartedIntent.putExtra(EXTRA_FILES_COUNT, mFilesCount);
            sendBroadcast(uploadStartedIntent);

            TreeMap<Integer, String> sortedAllPhotoPaths = new TreeMap<Integer, String>(allPhotoPaths);
            Log.d(TAG, "get bunch of photos to upload: ");
            for (int i : sortedAllPhotoPaths.keySet()) {
                Log.d(TAG, "id= " + i + " path = " + sortedAllPhotoPaths.get(i));
            }

            TreeMap<Integer, String> sortedAllVideoPaths = new TreeMap<Integer, String>(allVideoPaths);
            Log.d(TAG, "get bunch of videos to upload: ");
            for (int i : sortedAllVideoPaths.keySet()) {
                Log.d(TAG, "id= " + i + " path = " + sortedAllVideoPaths.get(i));
            }

            for (int i : sortedAllPhotoPaths.keySet()) {
                String path = sortedAllPhotoPaths.get(i);
                String destFileName = uploadFolderFullPath + path.substring(path.lastIndexOf("/") + 1);
                uploadPhoto(path, destFileName, i);
            }

            for (int i : sortedAllVideoPaths.keySet()) {
                String path = sortedAllVideoPaths.get(i);
                String destFileName = uploadFolderFullPath + path.substring(path.lastIndexOf("/") + 1);
                uploadVideo(path, destFileName, i);
            }

            sendBroadcast(new Intent(ACTION_UPLOAD_FINISHED));
        }
    }

    private void uploadPhoto(String fullPath, String destFileName, int photoId) {
        Log.d(TAG, "try to upload " + fullPath);
        try {

            CreateFileResultData data = createFileRequest(new File(fullPath), fullPath.substring(fullPath.lastIndexOf("/") + 1), mUploadFolderId);

            if (data == null) {
                return;
            }

            if(Utils.isServerErrorDirNotExists(data)) {
                Log.d(TAG, "Directory for auto upload not exists, so upload to default folder");
                Utils.saveUploadFolderId(this, Utils.getDefaultAutoUploadFolderId(this));
                mUploadFolderId = Utils.getDefaultAutoUploadFolderId(this);
                data = createFileRequest(new File(fullPath), fullPath.substring(fullPath.lastIndexOf("/") + 1), mUploadFolderId);
                if (data == null) {
                    return;
                }
            }

            Request uploadImageRequest = new Request();
            String imageUploadUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
            String resultXML = uploadImageRequest.uploadImage(imageUploadUrlString, fullPath, data, destFileName, mUploadFolderId);
            Log.d(TAG, "result for Upload Photo Request: " + resultXML);
            if (resultXML.contains("TotalWritten")) {
                if (closeFileUpload(new File(fullPath), data)) {
                    Log.d(TAG, fullPath + " uploaded successfully");
                    mFilesUploaded = mFilesUploaded + 1;
                    Utils.saveLastUploadedPhotoId(AutoUploadService.this, photoId);
                    Log.d(TAG, "save last uploaded Photo id = " + photoId);
                    Intent uploadProgressIntent = new Intent(ACTION_UPLOAD_PROGRESS);
                    uploadProgressIntent.putExtra(EXTRA_FILES_UPLOADED, mFilesUploaded);
                    uploadProgressIntent.putExtra(EXTRA_FILES_COUNT, mFilesCount);
                    sendBroadcast(uploadProgressIntent);
                }
            } else {
                Log.d(TAG, fullPath + " upload error: " + resultXML);
            }
        } catch (IOException e) {
            Log.d(TAG, fullPath + " upload error: " + e.toString());
        }

    }

    private void uploadVideo(String fullPath, String destFileName, int videoId) {
        Log.d(TAG, "try to upload " + fullPath);
        try {

            CreateFileResultData data = createFileRequest(new File(fullPath), fullPath.substring(fullPath.lastIndexOf("/") + 1), mUploadFolderId);

            if (data == null) {
                return;
            }

            if(Utils.isServerErrorDirNotExists(data)) {
                Log.d(TAG, "Directory for auto upload not exists, so upload to default folder");
                Utils.saveUploadFolderId(this, Utils.getDefaultAutoUploadFolderId(this));
                mUploadFolderId = Utils.getDefaultAutoUploadFolderId(this);
                data = createFileRequest(new File(fullPath), fullPath.substring(fullPath.lastIndexOf("/") + 1), mUploadFolderId);
                if (data == null) {
                    return;
                }
            }

            Request uploadVideoRequest = new Request();
            String videoUploadUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
            String resultXML = uploadVideoRequest.uploadImage(videoUploadUrlString, fullPath, data, destFileName, mUploadFolderId);
            Log.d(TAG, "result for Upload Video Request: " + resultXML);


            if (resultXML.contains("TotalWritten")) {
                if (closeFileUpload(new File(fullPath), data)) {
                    Log.d(TAG, fullPath + " uploaded successfully");
                    mFilesUploaded = mFilesUploaded + 1;
                    Utils.saveLastUploadedVideoId(AutoUploadService.this, videoId);
                    Log.d(TAG, "save last uploaded Video id = " + videoId);
                    Intent uploadProgressIntent = new Intent(ACTION_UPLOAD_PROGRESS);
                    uploadProgressIntent.putExtra(EXTRA_FILES_UPLOADED, mFilesUploaded);
                    uploadProgressIntent.putExtra(EXTRA_FILES_COUNT, mFilesCount);
                    sendBroadcast(uploadProgressIntent);
                }
            } else {
                Log.d(TAG, fullPath + " upload error: " + resultXML);
            }
        } catch (IOException e) {
            Log.d(TAG, fullPath + " upload error: " + e.toString());
        }

    }

    private CreateFileResultData createFileRequest(File file, String destFileName, String folderId) {

        try {
            Request createFileRequest = new Request();

            String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

            postData.add(new BasicNameValuePair("action", "create_and_open_file"));
            postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
            postData.add(new BasicNameValuePair("is_pro", ""));
            postData.add(new BasicNameValuePair("share_user_id", ""));
            postData.add(new BasicNameValuePair("share_id", ""));
            postData.add(new BasicNameValuePair("access_dir_id", folderId));
            postData.add(new BasicNameValuePair("dir_id", folderId));
            postData.add(new BasicNameValuePair("name", destFileName));
            postData.add(new BasicNameValuePair("size", file.length() + ""));

            String resultXML = createFileRequest.httpPost(loginUrlString, postData);
            Log.d(TAG, "result for Create File Request: " + resultXML);
            if (resultXML.equals(Constant.ErrorMessage)) {
                return null;

            } else {
                CreateFileResultDataParser parser = new CreateFileResultDataParser();
                CreateFileResultData data = parser.parseResponse(resultXML);

                if (data != null) {
                    if (data.getID().length() == 0 && !Utils.isServerErrorDirNotExists(data)) {
                        return null;
                    } else {
                        return data;
                    }
                } else {
                    return null;
                }
            }
        } catch (IOException e) {
            return null;
        }

    }

    private boolean closeFileUpload(File file, CreateFileResultData data) {

        try {
            Request closeFileUploadRequest = new Request();
            String closeFileUploadUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

            postData.add(new BasicNameValuePair("action", "close_file_upload"));
            postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
            postData.add(new BasicNameValuePair("is_pro", ""));
            postData.add(new BasicNameValuePair("share_user_id", ""));
            postData.add(new BasicNameValuePair("share_id", ""));
            postData.add(new BasicNameValuePair("access_dir_id", ""));
            postData.add(new BasicNameValuePair("file_id", data.getID()));
            postData.add(new BasicNameValuePair("temp_location", data.getTempLocation()));
            postData.add(new BasicNameValuePair("file_time", Utils.getTimeStamp()));
            postData.add(new BasicNameValuePair("file_size", file.length() + ""));
            String resultXML = closeFileUploadRequest.httpPost(closeFileUploadUrlString, postData);

            if (resultXML.contains(Constant.ErrorMessage)) {
                return false;

            } else {
                return true;
            }
        } catch (IOException e) {
            return false;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDataBaseAdapter.closeDatabase();
    }
}
