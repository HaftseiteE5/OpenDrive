package com.opendrive.android.ui;

import java.io.File;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.db.DataBaseAdapter;
import com.opendrive.android.parser.StorageInfoParser;
import com.opendrive.android.request.Request;

public class SettingActivity extends FragmentActivity {

    public static SettingActivity instance;
    private SharedPreferences settingPrefer;
    private LinearLayout btnDeleteOfflineFiles = null;
    private RelativeLayout btnLogOut = null;
    private LinearLayout btnTellFriend = null;

    private StorageInfoParser mStorageInfoParser = null;
    private TextView mUserName = null;
    private TextView mStorageInfo = null;
    private TextView mPlanInfo = null;

    private AlertDialog.Builder validationAlertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        initValidationAlertDialog();

        mUserName = (TextView) findViewById(R.id.userEditText);
        mStorageInfo = (TextView) findViewById(R.id.spaceEditText);
        // mPlanInfo = (TextView)findViewById(R.id.planEditText);
        if (Constant.UserName != null)
            mUserName.setText(Constant.UserName);

        if (LogIn.mLoginData != null && LogIn.mLoginData.getUserLevel() != null)
            mPlanInfo.setText(LogIn.mLoginData.getUserLevel());

        // btnDeleteOfflineFiles = (LinearLayout)findViewById(R.id.deleteOfflineFilesButton);
        btnDeleteOfflineFiles.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showValidationAlertDialog(getString(R.string.delete_db));
            }
        });

        // btnTellFriend = (LinearLayout)findViewById(R.id.tellFriend);
        btnTellFriend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                tellFriend();

            }
        });

        if (checkExistDeleteFiles())
            btnDeleteOfflineFiles.setEnabled(true);
        else
            btnDeleteOfflineFiles.setEnabled(false);

        btnLogOut = (RelativeLayout) findViewById(R.id.rlLogOut);
        btnLogOut.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                logOut();
            }
        });

        getStorageInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void getStorageInfo() {
        new DoBackgroundWork().execute(new Integer(Constant.BACKGROUND_GET_STORAGE_INFO));  
    }

    Handler LosSessionHandler = new Handler() {
        public void handleMessage(Message msg) {
            logOut();
        }
    };

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    public boolean checkExistDeleteFiles() {
        File filesFolder = new File(Constant.FOLDER_PATH);

        if (filesFolder.exists()) {
            File[] fileList = filesFolder.listFiles();

            if (fileList.length > 0)
                return true;
            else
                return false;
        } else {
            return false;
        }
    }

    public void initValidationAlertDialog() {

        if (validationAlertDialog == null)
            validationAlertDialog = new AlertDialog.Builder(this);
        validationAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
        validationAlertDialog.setTitle(R.string.txt_warning);
        validationAlertDialog.setPositiveButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

                return;
            }
        });
        validationAlertDialog.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                btnDeleteOfflineFiles.setEnabled(false);
                deleteDB();
                return;
            }
        });

    }

    public void showValidationAlertDialog(String errorString) {
        validationAlertDialog.setMessage(errorString);
        validationAlertDialog.show();
    }

    public void deleteDB() {

        DataBaseAdapter mDataBaseAdapter = null;
        mDataBaseAdapter = new DataBaseAdapter(this);
        mDataBaseAdapter.openDatabase();
        mDataBaseAdapter.drop();

        SharedPreferences settingPrefer = getSharedPreferences(Constant.PREFS_NAME, 0);
        final SharedPreferences.Editor editor = settingPrefer.edit();
        editor.putString("IsExistDB", "0");
        editor.commit();

        Utils.deleteFolders(Constant.FOLDER_PATH);
        btnDeleteOfflineFiles.setEnabled(false);
    }

    public void tellFriend() {

        String userFirstName = LogIn.mLoginData.getUserFirstName();
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/html");
        // i.putExtra(Intent.EXTRA_EMAIL , nul);
        i.putExtra(Intent.EXTRA_SUBJECT, userFirstName + " invites you to OpenDrive");
        i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml("<p>Hey there!</p><p>" + userFirstName + " wants you to use OpenDrive to store, backup, sync and share your files online.</p><p><a href='http://market.android.com/details?id=com.opendrive.android'>Start here.</p><p><a href='http://www.opendrive.com'>www.opendrive.com</p>"));
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void logOut() {
        settingPrefer = getSharedPreferences(Constant.PREFS_NAME, 0);
        final SharedPreferences.Editor editor = settingPrefer.edit();
        editor.putString(Constant.UserName_Key, "");
        editor.putString(Constant.Password_Key, "");
        editor.putString("initDataBase", "0");
        editor.commit();

        settingPrefer.edit().clear();

        Constant.SessionID = "";
        Constant.UserName = "";
        Constant.Password = "";

        showLoginActivity();
        finish();

    }

    public void showLoginActivity() {

        Intent intent = new Intent(this, LogIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    void saveInfo(String key, String Info) {
        settingPrefer = getSharedPreferences(Constant.PREFS_NAME, 0);
        final SharedPreferences.Editor editor = settingPrefer.edit();
        editor.putString(key, Info);
        editor.commit();
    }

    void loadInfoe() {
        // sadfsd
    }
    
    private class DoBackgroundWork extends AsyncTask<Object, Integer, Integer> {
        protected Integer doInBackground(Object... params) {
			Integer backgroundTaskID = (Integer) params[0];
			
    		switch (backgroundTaskID) {
        		case Constant.BACKGROUND_GET_STORAGE_INFO:
                    try {
                        Request loginRequest = new Request();

                        String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;

                        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                        postData.add(new BasicNameValuePair("action", "get_storage_info"));
                        postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                        String resultXML = loginRequest.httpPost(loginUrlString, postData);

                        if (resultXML.equals(Constant.ErrorMessage)) {
                            LosSessionHandler.sendMessage(LosSessionHandler.obtainMessage());
                            return Constant.BACKGROUND_PROCESS_ERROR;
                        } else {

                            mStorageInfoParser = new StorageInfoParser();

                            //final String storageInfo = mStorageInfoParser.parseResponse(resultXML);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //	mStorageInfo.setText(Utils.getStringFileSize(Long.parseLong(storageInfo)));
                                }
                            });
                        }
                        
                        return Constant.BACKGROUND_PROCESS_OK;
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return Constant.BACKGROUND_PROCESS_ERROR;
                    }
                        			
        		default:
        			break;
    		}
    		
    		return Constant.BACKGROUND_PROCESS_ERROR;
        }
        
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(Integer result) {
                    	
    		switch (result) {
    		case Constant.BACKGROUND_PROCESS_OK:    						
				break;
    		case Constant.BACKGROUND_PROCESS_ERROR:
    			break;
    		default:
    			break;
    		}
        }
    } 	        
}
