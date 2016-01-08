package com.opendrive.android.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.FileData;
import com.opendrive.android.db.DataBaseAdapter;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;

public class SyncService extends IntentService {

    private static final String TAG = "SyncService";

    private DataBaseAdapter mDataBaseAdapter;

    public SyncService() {
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
        ArrayList<FileData> allData = mDataBaseAdapter.selectAll();
        for (FileData data : allData) {
            if (!data.getIsFolder()) {
                try{
                    Log.d(TAG, "file " + data.getName() + " modified at " + Utils.getDateFromTimeStamp(data.getDateModified()) + " timestamp = " + data.getDateModified() + " is local file newer = " + Utils.isLocalFileNewer(data));
                }
                catch (Exception e){
                    Log.d(TAG, "Error get date modified for file " + data.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDataBaseAdapter.closeDatabase();
    }

}
