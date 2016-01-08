package com.opendrive.android.ui;

import java.io.File;
import java.util.ArrayList;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.PathData;
import com.opendrive.android.parser.LoginDataParser;
import com.opendrive.android.parser.PathDataParser;
import com.opendrive.android.parser.SignUpDataParser;
import com.opendrive.android.request.Request;
import com.opendrive.android.ui.fragment.FilesFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.Window;

public class SplashActivity extends Activity {
    private final static int SPLASH_TIME = 3000;

    private AlertDialog.Builder validationAlertDialog = null;

    private AlertDialog.Builder alertDialog = null;

    private AlertDialog.Builder storageErrorDialog = null;

    private PathData mPathData = null;

    private PathDataParser mPathDataParser = null;

    private SharedPreferences settingPrefer;

    // private LoginData mLoginData = null;
    private LoginDataParser mLoginDataParser = null;

    public boolean m_bRun = false;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);

        settingPrefer = getSharedPreferences(Constant.PREFS_NAME, 0);

        if (settingPrefer.getBoolean(Constant.IS_FIRST_LOUNCH, true)) {
            Utils.deleteFolders(Constant.FOLDER_PATH);
            Utils.deleteFolders(Constant.ICON_THUMBNAIL_PATH);
            SharedPreferences.Editor editor = settingPrefer.edit();
            editor.putBoolean(Constant.IS_FIRST_LOUNCH, false);
            editor.commit();
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            FilesFragment.sOfflineMode = false;
        } else {
            FilesFragment.sOfflineMode = true;
        }

        initWelcomeMsg();
        initStorageErrorDialog();
        initAPP();
        initValidationAlertDialog();

        int storageState = Utils.checkStorage(1024 * 1024 * 2);

        if (storageState == 0) {
            UnmountSDCardHandler.sendMessage(UnmountSDCardHandler.obtainMessage());

            return;
        } else if (storageState == 1) {
            FreeSpaceNotEnoughHandler.sendMessage(FreeSpaceNotEnoughHandler.obtainMessage());

            return;
        }

        if (!Utils.getKeepLoggedIn(this)) {
            showLoginActivity();
        } else {
        	new DoBackgroundWork().execute(new Integer(Constant.BACKGROUND_SPLASH_CREATE));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initWelcomeMsg() {
        TextView textMsg = (TextView) findViewById(R.id.textWelcome);
        String strMsg = getString(R.string.txt_welcome_splash);
        final SpannableString text = new SpannableString(strMsg);
        text.setSpan(new RelativeSizeSpan(1f), 14, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new StyleSpan(Typeface.BOLD), 14, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new RelativeSizeSpan(0.6f), 30, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textMsg.setText(text);

    }

    public void showLoginActivity() {
        // Intent intent = new Intent(this, LoginActivity.class);
        Intent intent = new Intent(SplashActivity.this, LogIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void showMainActivity() {
        if (Utils.getPassCodeTurned(this)) {
            Intent intent = new Intent(this, EnterPassCodeActivity.class);
            intent.putExtra(Constant.ENTER_PASS_CODE_MODE, Constant.MODE_ENTER_PASSCODE);
            startActivityForResult(intent, 123);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 123) {

            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    Handler GetPathConnectionerrorHandler = new Handler() {
        public void handleMessage(Message msg) {
            showAlertDialog(getString(R.string.connection_error));
        }
    };

    Handler GetPathErrorResponseHandler = new Handler() {
        public void handleMessage(Message msg) {
            showAlertDialog(mPathData.getName(), mPathData.getDescription());
        }
    };

    Handler ConnectionerrorMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            showValidationAlertDialog(getString(R.string.connection_error));
        }
    };

    Handler ErrorResponseMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            showValidationAlertDialog(mPathData.getName(), mPathData.getDescription());
        }
    };

    Handler LoginFailedMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            showValidationAlertDialog(LogIn.mLoginData.getName(), LogIn.mLoginData.getDescription());
        }
    };

    Handler UnmountSDCardHandler = new Handler() {
        public void handleMessage(Message msg) {
            showStorageErrorDialog(getString(R.string.sdcard_unmount));
        }
    };

    Handler FreeSpaceNotEnoughHandler = new Handler() {
        public void handleMessage(Message msg) {
            showStorageErrorDialog(getString(R.string.freespace_not_enough));
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // _active = false;
        }
        return true;
    }

    public void initAPP() {

        Utils.initAppContext(getApplicationContext());
        Utils.initValidationAlertDialog();

        Utils.makeFolders(Constant.APPPath);
        Utils.makeFolders(Constant.FOLDER_PATH);
        Utils.makeFolders(Constant.ICON_THUMBNAIL_PATH);
    }

    public void initAlertDialog() {

        if (alertDialog == null)
            alertDialog = new AlertDialog.Builder(SplashActivity.this);
        alertDialog.setIcon(R.drawable.ic_dialog_alarm);
        alertDialog.setTitle(R.string.txt_warning);
        alertDialog.setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
                return;
            }
        });
    }

    public void showAlertDialog(String errorString) {
        alertDialog.setMessage(errorString);
        alertDialog.show();
    }

    public void showAlertDialog(String title, String errorString) {
        alertDialog.setTitle(title);
        alertDialog.setMessage(errorString);
        alertDialog.show();
    }

    public void initValidationAlertDialog() {

        if (validationAlertDialog == null)
            validationAlertDialog = new AlertDialog.Builder(SplashActivity.this);
        validationAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
        validationAlertDialog.setTitle(R.string.txt_warning);
        validationAlertDialog.setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                showLoginActivity();
                finish();
                arg0.cancel();
                return;
            }
        });
    }

    public void showValidationAlertDialog(String errorString) {
        validationAlertDialog.setMessage(errorString);
        validationAlertDialog.show();
        m_bRun = true;
    }

    public void showValidationAlertDialog(String title, String errorString) {
        validationAlertDialog.setTitle(title);
        validationAlertDialog.setMessage(errorString);
        validationAlertDialog.show();
        m_bRun = true;
    }

    public void initStorageErrorDialog() {

        if (storageErrorDialog == null)
            storageErrorDialog = new AlertDialog.Builder(SplashActivity.this);
        storageErrorDialog.setIcon(R.drawable.ic_dialog_alarm);
        storageErrorDialog.setTitle(R.string.txt_warning);
        storageErrorDialog.setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
                return;
            }
        });
    }

    public void showStorageErrorDialog(String errorString) {
        storageErrorDialog.setMessage(errorString);
        storageErrorDialog.show();
    }
    
    private class DoBackgroundWork extends AsyncTask<Object, Integer, Integer> {
        protected Integer doInBackground(Object... params) {
			Integer backgroundTaskID = (Integer) params[0];
			
    		switch (backgroundTaskID) {
        		case Constant.BACKGROUND_SPLASH_CREATE:	
                    try {
                        File file = new File(Environment.getExternalStorageDirectory() + "/OpenDrive/");

                        File fileIcon = new File(Constant.ICON_THUMBNAIL_PATH);
                        File fileFolder = new File(Constant.FOLDER_PATH);
                        file.mkdirs();
                        fileFolder.mkdirs();
                        fileIcon.mkdirs();
                        File noMedia1 = new File(Constant.ICON_THUMBNAIL_PATH + ".nomedia");
                        noMedia1.createNewFile();
                        File noMedia = new File(Environment.getExternalStorageDirectory() + "/OpenDrive/.nomedia");
                        noMedia.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    long beforeTime = System.currentTimeMillis();

                    try {
                        Request getAPIPathRequest = new Request();

                        String urlString = Constant.ServerURL + Constant.APIName_GetPath;
                        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
                        postData.add(new BasicNameValuePair("version", Constant.ApiVersion));
                        String resultXML = getAPIPathRequest.httpPost(urlString, postData);

                        if (resultXML.equals(Constant.ErrorMessage)) {
                            GetPathConnectionerrorHandler.sendMessage(GetPathConnectionerrorHandler.obtainMessage());
                            return Constant.BACKGROUND_PROCESS_ERROR;
                        } else {
                            mPathDataParser = new PathDataParser();
                            mPathData = mPathDataParser.parseResponse(resultXML);

                            if (mPathData != null) {
                                if (mPathData.getPath().length() == 0) {
                                    GetPathErrorResponseHandler.sendMessage(GetPathErrorResponseHandler.obtainMessage());
                                    return Constant.BACKGROUND_PROCESS_ERROR;
                                } else {
                                    Constant.APIPath = mPathData.getPath();
                                }
                            } else {
                                GetPathConnectionerrorHandler.sendMessage(GetPathConnectionerrorHandler.obtainMessage());
                                return Constant.BACKGROUND_PROCESS_ERROR;
                            }
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                        if (FilesFragment.sOfflineMode) {

                            File dbFile = new File(Constant.DATABASE_NAME);
                            if (dbFile.exists())
                                showMainActivity();
                            else
                                ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        } else {

                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());

                            e.printStackTrace();
                        }
                        return Constant.BACKGROUND_PROCESS_ERROR;
                    }

                    Constant.UserName = settingPrefer.getString(Constant.UserName_Key, "");
                    Constant.Password = settingPrefer.getString(Constant.Password_Key, "");

                    if (Constant.UserName.length() != 0 || Constant.Password.length() != 0) {

                        new Thread(new Runnable() {
                            public void run() {

                                try {
                                    Thread.sleep(100);

                                    Request loginRequest = new Request();

                                    String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                                    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                                    postData.add(new BasicNameValuePair("action", "login"));
                                    postData.add(new BasicNameValuePair("user", Constant.UserName));
                                    postData.add(new BasicNameValuePair("pass", Constant.Password));
                                    postData.add(new BasicNameValuePair("version", Constant.AppVersion));
                                    postData.add(new BasicNameValuePair("pro_version", ""));
                                    postData.add(new BasicNameValuePair("pcid", "AA2C6A20-E619-51B7-BDF7-7217EC25930E"));
                                    String resultXML = loginRequest.httpPost(loginUrlString, postData);

                                    if (resultXML.equals(Constant.ErrorMessage)) {
                                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                                        return;
                                    } else {
                                        mLoginDataParser = new LoginDataParser();
                                        LogIn.mLoginData = mLoginDataParser.parseResponse(resultXML);
                                        if (LogIn.mLoginData != null) {
                                            if (LogIn.mLoginData.getSessionID().length() == 0) {
                                                LoginFailedMessageHandler.sendMessage(LoginFailedMessageHandler.obtainMessage());
                                                return;
                                            } else {
                                                Constant.SessionID = LogIn.mLoginData.getSessionID();
                                                if (LogIn.mLoginData.getIsAccessUser().compareToIgnoreCase("true") == 0)
                                                	Constant.accessUser = true; 
                                                showMainActivity();
                                            }
                                        } else {
                                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                                            return;
                                        }
                                    }
                                } catch (Exception e) {
                                    String exceptionString = e.getMessage();
                                    if (FilesFragment.sOfflineMode) {

                                        File dbFile = new File(Constant.DATABASE_NAME);
                                        if (dbFile.exists())
                                            showMainActivity();
                                        else
                                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                                    } else {
                                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                                        e.printStackTrace();
                                    }
                                    return;
                                }
                            }
                        }).start();

                        return Constant.BACKGROUND_PROCESS_OK;
                    }

                    long afterTime = System.currentTimeMillis();
                    long passedTime = afterTime - beforeTime;

                    if (SPLASH_TIME > passedTime) {
                        while (SPLASH_TIME > passedTime) {
						    afterTime = System.currentTimeMillis();
						    passedTime = afterTime - beforeTime;
						}
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // kkhLogIn
                            if (m_bRun)
                                return;

                            Intent intent = new Intent(SplashActivity.this, LogIn.class);
                            startActivity(intent);
                        }
                    });
                    finish();
                    
                    return Constant.BACKGROUND_PROCESS_OK;
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
