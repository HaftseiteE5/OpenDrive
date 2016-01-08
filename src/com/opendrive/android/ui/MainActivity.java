package com.opendrive.android.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.opendrive.android.OpenDriveApplication;
import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.CreateFileResultData;
import com.opendrive.android.datamodel.FileData;
import com.opendrive.android.parser.CreateFileResultDataParser;
import com.opendrive.android.request.Request;
import com.opendrive.android.service.AutoUploadService;
import com.opendrive.android.service.SyncService;
import com.opendrive.android.ui.dialog.UploadDialog;
import com.opendrive.android.ui.fragment.FilesFragment;
import com.opendrive.android.ui.fragment.SettingsFragment;

public class MainActivity extends FragmentActivity implements FileItemClickListener, HomeBtnClickListener {

    private static String TAG = "MainActivity";

    private Tracker mGaTracker;
    private GoogleAnalytics mGaInstance;

    public static final int VIEW_FILES = 0;
    public static final int VIEW_SETTING = 1;
    public static final int VIEW_UPLOAD = 2;

    private int viewSequence[] = {-1, -1, -1};
    private int mCurrentViewId = 0;

    private FragmentTransaction mFragmentTransaction;
    private FilesFragment mFilesFragment;
    private SettingsFragment mSettingsFragment;

    private Button filesButton;
    private Button uploadButton;
    private Button requestButton;
    private Button settingsButton;

    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_EXPLORER = 1;
    public static final int REQUEST_CREATE_FOLDER = 2;

    private boolean bIsSearchStop = false;
    private AlertDialog.Builder validationAlertDialog = null;
    private CreateFileResultData mCreatFileResultData = null;
    private CreateFileResultDataParser mCreatFileResultDataParser = null;
    private ProgressDialog LoadingDialog = null;
    public static MainActivity Instance;
    public static FilesActivity fileActivityInstance = null;
    public static boolean mFileUploaded = false;
    public static boolean mContentOpened = false;

    private AlertDialog.Builder switchOfflineModeAlertDialog = null;

    static public boolean m_bLoading = false;

    private static boolean mWasScreenOff = false;
    BroadcastReceiver mReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        mGaInstance = GoogleAnalytics.getInstance(this);
        mGaTracker = mGaInstance.getTracker(Constant.GA_ID);

        if (savedInstanceState == null) {
            checkFilesForAutoUpload();
            sendSomeStat();
            //startService(new Intent(MainActivity.this, SyncService.class));
        }

        // bodyLayout = (LinearLayout) findViewById(R.id.activity_view_LinearLayout);
        // bodyLayout.setBackgroundResource(R.color.WHITE_TEXTCOLOR);
        if (savedInstanceState != null) {
            mFilesFragment = (FilesFragment) getSupportFragmentManager().getFragment(savedInstanceState, FilesFragment.class.getName());
        }
        if (mFilesFragment == null) {
            mFilesFragment = new FilesFragment();
        }
        mSettingsFragment = new SettingsFragment();

        filesButton = (Button) findViewById(R.id.button_files);
        uploadButton = (Button) findViewById(R.id.button_upload);
        // requestButton = (Button) findViewById(R.id.button_request);
        settingsButton = (Button) findViewById(R.id.button_settings);

        Instance = this;
        initView();
        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.main_content_fragment, mFilesFragment);
            ft.commit();
        }

//        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
//        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        mReceiver = new ScreenReceiver();
//        registerReceiver(mReceiver, filter);

    }

    private void sendSomeStat() {
        EasyTracker.getInstance().setContext(this);
        new DoBackgroundWork().execute(Constant.BACKGROUND_SEND_SOME_SEAT);   
    }

    private void checkFilesForAutoUpload() {
        EasyTracker.getInstance().setContext(this);
        new DoBackgroundWork().execute(Constant.BACKGROUND_CHECK_FILES_FOR_AUTO_UPLOAD);           
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (Utils.getPassCodeTurned(this) && !OpenDriveApplication.isPasscodeEntered()) {
            Intent intent = new Intent(this, EnterPassCodeActivity.class);
            intent.putExtra(Constant.ENTER_PASS_CODE_MODE, Constant.MODE_ENTER_PASSCODE);
            startActivityForResult(intent, Constant.PASSCODE_CHECK_REQUEST_CODE);
        }
//        else if (mWasScreenOff) {
//            checkFilesForAutoUpload();
//        }
        checkFilesForAutoUpload();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY) {
            OpenDriveApplication.setPasscodeEntered(true);
            if (data == null)
                return;

            Uri uri = data.getData();

            String imagePath = getRealPathFromURI(uri);

            if (imagePath.length() > 0) {
                uploadImage(imagePath);
                mGaTracker.sendEvent(Constant.GA_EVENT_CAT_UPLOAD, Constant.GA_EVENT_ACTION_USER_UPLOADING_FILES, "", 0L);
            }
        } else if (requestCode == REQUEST_EXPLORER) {
            if (EventHandler.mMultiSelectData.size() > 0) {
                UploadHandler.sendMessage(UploadHandler.obtainMessage());
                mGaTracker.sendEvent(Constant.GA_EVENT_CAT_UPLOAD, Constant.GA_EVENT_ACTION_USER_UPLOADING_FILES, "", 0L);
            }
        } else if (requestCode == Constant.PASSCODE_CHECK_REQUEST_CODE) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            } else if (resultCode == RESULT_OK) {
                OpenDriveApplication.setPasscodeEntered(true);
                checkFilesForAutoUpload();
            }
        }
    }

    @Override
    public void onHomeBtnClick() {
        mFragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (mFilesFragment != null) {
            mFilesFragment.freeResources();
            mFilesFragment = null;
        }
        mFilesFragment = new FilesFragment();

        mFragmentTransaction.replace(R.id.main_content_fragment, mFilesFragment);
        mFragmentTransaction.addToBackStack(null);
        mFragmentTransaction.commit();

    }

    public void uploadImage(final String imageFullPath) {
        if (LoadingDialog == null) {
            initLoadingDialog();
        }
        LoadingDialog.show();
        new DoBackgroundWork().execute(Constant.BACKGROUND_UPLOAD_IMAGE_FILE, imageFullPath);   
    }
    
    public Handler ReadyUploadHandler = new Handler() {
        public void handleMessage(Message msg) {
            LoadingDialog.show();
            UploadHandler.sendMessage(UploadHandler.obtainMessage());
        }
    };

    public Handler UploadHandler = new Handler() {
        public void handleMessage(Message msg) {
            m_bLoading = true;
            if (EventHandler.mMultiSelectData.size() > 0) {
                String path = EventHandler.mMultiSelectData.get(0).trim();
                EventHandler.mMultiSelectData.remove(0);
                if (path.length() > 0)
                    uploadImage(path);
            }
        }
    };

    public Handler FileActivityRefreshHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (mFileUploaded && mCurrentViewId == VIEW_FILES) {
                mFilesFragment.refresh();
                mFileUploaded = false;
            }
        }
    };

    public Handler FileActivityBackPressedHandler = new Handler() {
        public void handleMessage(Message msg) {
            // fileActivityInstance.getBack();
        }
    };

    private boolean createFileRequest(File file, String destFileName) {
        try {
            Request createFileRequest = new Request();

            String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

            postData.add(new BasicNameValuePair("action", "create_and_open_file"));
            postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
            postData.add(new BasicNameValuePair("is_pro", ""));
            postData.add(new BasicNameValuePair("share_user_id", ""));
            postData.add(new BasicNameValuePair("share_id", ""));
            if (mFilesFragment.getCurrentFolderData() == null) {
                postData.add(new BasicNameValuePair("access_dir_id", "0"));
                postData.add(new BasicNameValuePair("dir_id", "0"));

            } else {
                postData.add(new BasicNameValuePair("access_dir_id", mFilesFragment.getCurrentFolderData().getAccessDirID()));
                postData.add(new BasicNameValuePair("dir_id", mFilesFragment.getCurrentFolderData().getID()));
            }
            postData.add(new BasicNameValuePair("name", destFileName));
            postData.add(new BasicNameValuePair("size", file.length() + ""));

            String resultXML = createFileRequest.httpPost(loginUrlString, postData);

            if (bIsSearchStop) {
                bIsSearchStop = false;
                return false;
            }

            if (resultXML.equals(Constant.ErrorMessage)) {
                LoadingDialog.dismiss();
                ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                return false;

            } else {
                mCreatFileResultDataParser = new CreateFileResultDataParser();
                mCreatFileResultData = mCreatFileResultDataParser.parseResponse(resultXML);

                if (mCreatFileResultData != null) {
                    if (mCreatFileResultData.getID().length() == 0) {
                        LoadingDialog.dismiss();
                        CreateFileErrorHandler.sendMessage(CreateFileErrorHandler.obtainMessage());
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    LoadingDialog.dismiss();
                    ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                    return false;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LoadingDialog.dismiss();
            String exceptionString = e.getMessage();
            if (exceptionString.contains("Network unreachable") || exceptionString.contains("Host is unresolved")) {
                // Network unreachable
                FilesFragment.sOfflineMode = true;
                SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
            } else
                ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
            return false;
        }

    }

    private boolean openFileUpload(File file, String fileId) {

        try {
            Request openFileUpload = new Request();

            String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

            postData.add(new BasicNameValuePair("action", "open_file_upload"));
            postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
            postData.add(new BasicNameValuePair("is_pro", ""));
            postData.add(new BasicNameValuePair("share_user_id", ""));
            postData.add(new BasicNameValuePair("share_id", ""));
            if (mFilesFragment.getCurrentFolderData() == null) {
                postData.add(new BasicNameValuePair("access_dir_id", "0"));
                postData.add(new BasicNameValuePair("dir_id", "0"));

            } else {
                postData.add(new BasicNameValuePair("access_dir_id", mFilesFragment.getCurrentFolderData().getAccessDirID()));
                postData.add(new BasicNameValuePair("dir_id", mFilesFragment.getCurrentFolderData().getID()));
            }
            postData.add(new BasicNameValuePair("file_id", fileId));
            postData.add(new BasicNameValuePair("size", file.length() + ""));

            String resultXML = openFileUpload.httpPost(loginUrlString, postData);

            if (bIsSearchStop) {
                bIsSearchStop = false;
                return false;
            }

            if (resultXML.equals(Constant.ErrorMessage)) {
                LoadingDialog.dismiss();
                ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                return false;

            } else {

                return true;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LoadingDialog.dismiss();
            String exceptionString = e.getMessage();
            if (exceptionString.contains("Network unreachable") || exceptionString.contains("Host is unresolved")) {
                // Network unreachable
                FilesFragment.sOfflineMode = true;
                SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
            } else
                ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
            return false;
        }

    }

    private boolean uploadImageRequest(String imageFullPath, String destFileName, String accessDirId) {
        try {
            Request uploadImageRequest = new Request();

            String imageUploadUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;

            String resultXML = uploadImageRequest.uploadImage(imageUploadUrlString, imageFullPath, mCreatFileResultData, destFileName, accessDirId);

            if (bIsSearchStop) {
                bIsSearchStop = false;
                return false;
            }

            if (!resultXML.contains("TotalWritten")) {
                LoadingDialog.dismiss();
                ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                return false;

            } else {
                return true;

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LoadingDialog.dismiss();
            String exceptionString = e.getMessage();
            if (exceptionString.contains("Network unreachable") || exceptionString.contains("Host is unresolved")) {
                // Network unreachable
                FilesFragment.sOfflineMode = true;
                SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
            } else {
                ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
            }

            return false;
        }

    }

    private boolean closeFileUpload(File file) {

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
            postData.add(new BasicNameValuePair("file_id", mCreatFileResultData.getID()));
            postData.add(new BasicNameValuePair("temp_location", mCreatFileResultData.getTempLocation()));
            postData.add(new BasicNameValuePair("file_time", Utils.getTimeStamp()));
            postData.add(new BasicNameValuePair("file_size", file.length() + ""));

            String resultXML = closeFileUploadRequest.httpPost(closeFileUploadUrlString, postData);

            if (bIsSearchStop) {
                bIsSearchStop = false;
                return false;
            }

            if (resultXML.contains(Constant.ErrorMessage)) {
                LoadingDialog.dismiss();
                ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                return false;

            } else {
                LoadingDialog.dismiss();
                return true;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LoadingDialog.dismiss();
            String exceptionString = e.getMessage();
            if (exceptionString.contains("Network unreachable") || exceptionString.contains("Host is unresolved")) {
                // Network unreachable
                FilesFragment.sOfflineMode = true;
                SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
            } else
                ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
            return false;
        }

    }

    private void initView() {

        // createChildLayout(VIEW_FILES);
        // SetTabTitleBackground(VIEW_FILES);
    }

    // private View createChildLayout(int viewId)
    // {
    // View childView = getChildLayout(viewId);
    // if(childView != null)
    // return childView;
    //
    // switch (viewId) {
    // case VIEW_FILES:
    // bodyLayout.setBackgroundResource(R.color.WHITE_TEXTCOLOR);
    //
    // childView = getLocalActivityManager().startActivity("Files", new
    // Intent(getApplicationContext(),FilesActivity.class)
    // .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
    // .getDecorView();
    // addChildLayout(childView, viewId);
    //
    // break;
    // case VIEW_SETTING:
    //
    // bodyLayout.setBackgroundResource(R.drawable.bg);
    //
    // childView = getLocalActivityManager().startActivity("Setting", new
    // Intent(getApplicationContext(), SettingActivity.class)
    // .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
    // .getDecorView();
    // addChildLayout(childView, viewId);
    // break;
    // case VIEW_UPLOAD:
    //
    // bodyLayout.setBackgroundResource(R.drawable.bg_newfile);
    //
    // childView = getLocalActivityManager().startActivity("Upload", new
    // Intent(getApplicationContext(), UploadActivity.class)
    // .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
    // .getDecorView();
    // addChildLayout(childView, viewId);
    // break;
    // default:
    // break;
    // }
    //
    // return childView;
    // }

    // private View getChildLayout(int viewId)
    // {
    // if (viewSequence[viewId] == -1)
    // return null;
    //
    // return bodyLayout.getChildAt(viewSequence[viewId]);
    // }
    //
    // private synchronized void addChildLayout(View view, int viewId)
    // {
    // if(viewSequence[viewId] > -1)
    // return;
    //
    // viewSequence[viewId] = bodyLayout.getChildCount();
    // bodyLayout.addView(view);
    // }
    //
    // protected void showChildLayout(int viewId) {
    //
    // for(int i = 0; i < bodyLayout.getChildCount(); i++)
    // bodyLayout.getChildAt(i).setVisibility((viewSequence[viewId] == i) ? View.VISIBLE : View.GONE);
    //
    // SetTabTitleBackground(viewId);
    // mCurrentViewId = viewId;
    //
    // if(mFileUploaded && mCurrentViewId == VIEW_FILES){
    // fileActivityInstance.refresh();
    // mFileUploaded = false;
    // }
    // }

    // public Handler receiveHandler = new Handler() {
    // public void handleMessage(Message msg) {
    //
    // int viewId = Integer.parseInt(msg.obj.toString());
    //
    // showChildLayout(viewId);
    //
    // mCurrentViewId = viewId;
    //
    // SetTabTitleBackground(viewId);
    // }
    // };

    // private void SetTabTitleBackground(int selTabIndex) {
    //
    // switch (selTabIndex) {
    // case VIEW_FILES:
    // bodyLayout.setBackgroundResource(R.color.WHITE_TEXTCOLOR);
    //
    //
    // break;
    // case VIEW_SETTING:
    // bodyLayout.setBackgroundResource(R.drawable.bg);
    //
    // break;
    // default:
    // break;
    // }
    // }

    // public void onBackPressed() {
    // switch (mCurrentViewId) {
    // case VIEW_FILES:
    // FileActivityBackPressedHandler.sendMessage(FileActivityBackPressedHandler.obtainMessage());
    //
    // break;
    //
    // case VIEW_SETTING:
    // finish();
    // break;
    // default:
    // break;
    // }
    // }

    @Override
    protected void onResume() {
        mWasScreenOff = false;
        if (!m_bLoading && EventHandler.mMultiSelectData != null && EventHandler.mMultiSelectData.size() > 0) {
            UploadHandler.sendMessage(UploadHandler.obtainMessage());
        } else {
            FileActivityRefreshHandler.sendMessage(FileActivityRefreshHandler.obtainMessage());
        }
        OpenDriveApplication.applicationResumed();
        mContentOpened = false;
        
	    IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
	    filter.addAction(Intent.ACTION_SCREEN_OFF);
	    mReceiver = new ScreenReceiver();
	    registerReceiver(mReceiver, filter);
	    
        super.onResume();
    }

    public String getRealPathFromURI(Uri contentUri) {

        // can post image
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, // Which columns to return
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, FilesFragment.class.getName(), mFilesFragment);
    }

    public void initLoadingDialog() {

        if (LoadingDialog == null)
            LoadingDialog = new ProgressDialog(this);

        LoadingDialog.setCancelable(false);
        LoadingDialog.setCanceledOnTouchOutside(false);
        
        LoadingDialog.setMessage(getString(R.string.txt_uploadding));
        LoadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    LoadingDialog.cancel();
                    bIsSearchStop = true;
                }
                return false;
            }
        });
    }

    public void initValidationAlertDialog() {

        if (validationAlertDialog == null)
            validationAlertDialog = new AlertDialog.Builder(MainActivity.this);
        validationAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
        validationAlertDialog.setTitle(R.string.txt_warning);
        validationAlertDialog.setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

                return;
            }
        });

        if (switchOfflineModeAlertDialog == null)
            switchOfflineModeAlertDialog = new AlertDialog.Builder(MainActivity.this);

        switchOfflineModeAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
        switchOfflineModeAlertDialog.setTitle(R.string.txt_warning);
        switchOfflineModeAlertDialog.setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

                FilesFragment.sOfflineMode = true;
                return;
            }
        });
    }

    public void showValidationAlertDialog(String errorString) {
        if (validationAlertDialog == null) {
            initValidationAlertDialog();
        }
        validationAlertDialog.setMessage(errorString);
        validationAlertDialog.show();
    }

    public void showValidationAlertDialog(String title, String errorString) {
        if (validationAlertDialog == null) {
            initValidationAlertDialog();
        }
        validationAlertDialog.setTitle(title);
        validationAlertDialog.setMessage(errorString);
        validationAlertDialog.show();
    }

    Handler ConnectionerrorMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            showValidationAlertDialog(getString(R.string.connection_error));
        }
    };

    Handler CreateFileErrorHandler = new Handler() {
        public void handleMessage(Message msg) {
            showValidationAlertDialog(mCreatFileResultData.getName(), mCreatFileResultData.getDescription());
        }
    };

    /*
     * public boolean onCreateOptionsMenu(Menu menu) { MenuInflater inflater = getSupportMenuInflater(); inflater.inflate(R.menu.menu, menu); final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
     * 
     * searchView.setQueryHint("Search"); searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
     * 
     * @Override public boolean onQueryTextChange(String newText) { return false; }
     * 
     * @Override public boolean onQueryTextSubmit(String query) { if (query.length() != 0) {
     * 
     * //SharedPreferences mSettings = getSharedPreferences(Constant.MANAGER_PREFS_NAME, 0); boolean hide = mSettings.getBoolean(Constant.PREFS_HIDDEN, false); boolean thumb = mSettings.getBoolean(Constant.PREFS_THUMBNAIL, true); int sort = mSettings.getInt(Constant.PREFS_SORT, 3); FileManager mFileMag = new FileManager(); mFileMag.setShowHiddenFiles(hide); mFileMag.setSortType(sort); new EventHandler(MainActivity.this, mFileMag).searchForFile(query, EventHandler.SEARCH_TO_OPEN, mFilesFragment);
     * 
     * searchForFile(query); return true; } return false; } }); return true; }
     */

    // protected void searchForFile(final String query) {
    //
    // new AsyncTask<Void, Void, ArrayList<FileData>>() {
    //
    // private ProgressDialog pr_dialog;
    //
    // @Override
    // protected void onPreExecute() {
    // pr_dialog = ProgressDialog.show(MainActivity.this, "Searching", "Searching for files...", true, true);
    // }
    //
    // @Override
    // protected ArrayList<FileData> doInBackground(Void... params) {
    // DataBaseAdapter adapter = new DataBaseAdapter(MainActivity.this);
    // adapter.openDatabase();
    // String parentID = "0";
    // if (mFilesFragment.getCurrentFolderData() != null) {
    // parentID = mFilesFragment.getCurrentFolderData().getID();
    // }
    // ArrayList<FileData> fileData = adapter.selectFileDataByFileName(query, parentID);
    // return fileData;
    // }
    //
    // @Override
    // protected void onPostExecute(final ArrayList<FileData> fileData) {
    // int len = fileData != null ? fileData.size() : 0;
    // pr_dialog.dismiss();
    // if (len == 0) {
    // Toast.makeText(MainActivity.this, "Couldn't find " + query, Toast.LENGTH_SHORT).show();
    //
    // } else {
    // mFilesFragment.updateFileList(fileData, FilesFragment.MODE_VIEW_SEARCH_RESULTS);
    // }
    //
    // }
    // }.execute();
    // }

    /*
     * public boolean onOptionsItemSelected(MenuItem item) { // Handle item selection switch (item.getItemId()) {
     * 
     * case R.id.menu_search: onSearchRequested(); return true;
     * 
     * case R.id.menu_refresh: mFilesFragment.refresh(); return true;
     * 
     * case R.id.menu_new_folder: mFilesFragment.newfolder(); return true;
     * 
     * default: return super.onOptionsItemSelected(item); } }
     */

    public void onBottomButtonClick(View v) {

        switch (v.getId()) {
            case R.id.button_files:
                if (!mFilesFragment.isVisible()) {
                    mFilesFragment.openOnlyFromDB();
                    mFragmentTransaction = getSupportFragmentManager().beginTransaction();
//                    int folderDeep = mFilesFragment.getFolderDeep();
//                    FileData currentFolder = mFilesFragment.getCurrentFolderData();
//                    mFilesFragment = null;
//                    mFilesFragment = new FilesFragment();
//                    mFilesFragment.setFolderDeep(folderDeep);
//                    Bundle b = new Bundle();
//                    b.putParcelable("data", mFilesFragment.getCurrentFolderData());
//                    mFilesFragment.setArguments(b);
                    mFragmentTransaction.replace(R.id.main_content_fragment, mFilesFragment);
                    mFragmentTransaction.addToBackStack(null);
                    mFragmentTransaction.commit();
                } else {
                    mFilesFragment.onBtnHomeClick();
                }

                break;

            case R.id.button_upload:
                if (mFilesFragment.sOfflineMode) {
                    Toast.makeText(this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                    break;
                }
                UploadDialog uploadDialog = new UploadDialog();
                uploadDialog.show(getSupportFragmentManager(), "");

                break;

            case R.id.button_settings:

                // startActivity(new Intent(this, SettingActivity.class));
                if (!mSettingsFragment.isVisible()) {
                    mFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    mFragmentTransaction.replace(R.id.main_content_fragment, mSettingsFragment);
                    mFragmentTransaction.addToBackStack(null);
                    mFragmentTransaction.commit();
                }

               break;
            default:

                break;
        }
    }

    Handler SwitchOfflineMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            switchOfflineModeAlertDialog.setMessage(R.string.switch_offline);
            switchOfflineModeAlertDialog.show();
        }
    };

    @Override
    public void onClick(FileData data) {
        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        mFragmentTransaction.remove(mFilesFragment);

        int folderDeep = mFilesFragment.getFolderDeep();
        int mode = mFilesFragment.getMode();
        FileData itemForMove = mFilesFragment.getItemForMove();

        mFilesFragment.freeResources();
        mFilesFragment = null;
        mFilesFragment = new FilesFragment();

        mFilesFragment.setFolderDeep(folderDeep);
        mFilesFragment.setMode(mode);
        mFilesFragment.setItemForMove(itemForMove);

        Bundle b = new Bundle();
        b.putParcelable("data", data);
        mFilesFragment.setArguments(b);
        mFilesFragment.goDeeper();

        mFragmentTransaction.add(R.id.main_content_fragment, mFilesFragment);
        mFragmentTransaction.addToBackStack(null);
        mFragmentTransaction.commit();

    }

    @Override
    public void onBottomButtonClick(int id) {
        switch (id) {
            case R.id.button_files:
                break;
            case R.id.button_upload:
                break;

	/*
     * case R.id.button_request:
	 * 
	 * break;
	 */

            case R.id.button_settings:
        /*
         * SettingsFragment settings = new SettingsFragment(); ft.replace(R.id.main_content_fragment, settings); ft.addToBackStack(null); ft.commit();
	     */
                break;
            default:
                break;

        }
    }

    @Override
    public void onBackPressed() {
        if (mFilesFragment.isFragmentVisible()) {
            mFilesFragment.onBackPressed();
        } else if (mSettingsFragment.isFragmentVisible()) {
            mFragmentTransaction = getSupportFragmentManager().beginTransaction();
            mFilesFragment = null;
            mFilesFragment = new FilesFragment();
            mFragmentTransaction.replace(R.id.main_content_fragment, mFilesFragment);
            mFragmentTransaction.addToBackStack(null);
            mFragmentTransaction.commit();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            mFilesFragment.searchForFile(query);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mContentOpened) {
            OpenDriveApplication.setPasscodeEntered(false);
        }
        
        unregisterReceiver(mReceiver);
    }


    private static class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                mWasScreenOff = true;
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(mReceiver);
    }
    
    private class DoBackgroundWork extends AsyncTask<Object, Integer, Integer> {
        protected Integer doInBackground(Object... params) {
				Integer backgroundTaskID = (Integer) params[0];
	
        		switch (backgroundTaskID) {
	        		case Constant.BACKGROUND_UPLOAD_IMAGE_FILE:	
	                	try {
	                		String imageFullPath = (String) params[1];
		                    Date now = new Date();
		                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		                    String destFileName = sdf.format(now) + ".jpg";
	
		                    if (imageFullPath.contains(".png"))
		                        destFileName = destFileName.replace(".jpg", ".png");
	
		                    File tmpFile = new File(imageFullPath);
	
		                    if (!imageFullPath.trim().contains("png") && !imageFullPath.trim().contains("jpg")) {
		                        String[] paths = imageFullPath.split("/");
		                        if (paths.length > 0)
		                            destFileName = paths[paths.length - 1];
		                    }
	
		                    if (!createFileRequest(tmpFile, destFileName))
		                        return Constant.BACKGROUND_PROCESS_ERROR;
	
		                    // if (!openFileUpload(tmpFile, mCreatFileResultData.getID()))
		                    // return;
	
		                    if (mFilesFragment.getCurrentFolderData() == null) {
		                        if (!uploadImageRequest(imageFullPath, Constant.FOLDER_PATH + destFileName, ""))
		                            return Constant.BACKGROUND_PROCESS_ERROR;
	
		                    } else {
		                        String accessDirId = mFilesFragment.getCurrentFolderData().getID();
		                        if (!uploadImageRequest(imageFullPath, "/" + mFilesFragment.getCurrentFolderData().getPath() + "/" + destFileName, accessDirId))
		                            return Constant.BACKGROUND_PROCESS_ERROR;
		                    }
	
		                    closeFileUpload(tmpFile);
	
		                    if (EventHandler.mMultiSelectData != null && EventHandler.mMultiSelectData.size() > 0) {
		                        ReadyUploadHandler.sendMessage(ReadyUploadHandler.obtainMessage());
		                    } else {
		                        mFileUploaded = true;
		                        sendBroadcast(new Intent(FilesFragment.ACTION_EDITED_FILE).putExtra("file",tmpFile.getAbsolutePath()));
		                        m_bLoading = false;
		                        FileActivityRefreshHandler.sendMessage(FileActivityRefreshHandler.obtainMessage());
		                    }
		                
							return Constant.BACKGROUND_PROCESS_OK;
							
		            	} catch (Exception ex) {
		                    String exceptionString = ex.getMessage();
		                    if (exceptionString.contains("Network unreachable") || exceptionString.contains("Host is unresolved")) {
		                        // Network unreachable
		                        FilesFragment.sOfflineMode = true;
		                        SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
		                    } else
		                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
		                    
		                    return Constant.BACKGROUND_PROCESS_ERROR;
		                }
	                	
	        		case Constant.BACKGROUND_SEND_SOME_SEAT:
	        			
	                    Tracker tracker = EasyTracker.getTracker();
	                    if (Utils.getPassCodeTurned(MainActivity.this)) {
	                        tracker.setCustomDimension(4, Constant.GA_PASSCODE_ENABLED);
	                    } else {
	                        tracker.setCustomDimension(4, Constant.GA_PASSCODE_DISABLED);
	                    }
	                    if (Utils.getKeepLoggedIn(MainActivity.this)) {
	                        tracker.setCustomDimension(3, Constant.GA_AUTO_LOGIN_ENABLED);
	                    } else {
	                        tracker.setCustomDimension(3, Constant.GA_AUTO_LOGIN_DISABLED);
	                    }
	                    tracker.trackView(Constant.GA_FILES_VIEW);
	                    
	                    return Constant.BACKGROUND_PROCESS_OK;
	                    
	        		case Constant.BACKGROUND_CHECK_FILES_FOR_AUTO_UPLOAD:
	        			if (Utils.getAutoUploadIsEnabled(MainActivity.this)) {
	                        Tracker tracker1 = EasyTracker.getTracker();
	                        tracker1.setCustomDimension(2, Constant.GA_AUTO_UPLOAD_ENABLED);
	                        tracker1.trackView(Constant.GA_FILES_VIEW);
	                        Log.d(TAG, "Auto Upload enabled");
	                        int lastDevicePhotoId = Utils.getLastDevicePhotoId(MainActivity.this);
	                        int lastUploadedPhotoId = Utils.getLastUploadedPhotoId(MainActivity.this);
	                        int lastDeviceVideoId = Utils.getLastDeviceVideoId(MainActivity.this);
	                        int lastUploadedVideoId = Utils.getLastUploadedVideoId(MainActivity.this);
	                        if (lastDevicePhotoId > lastUploadedPhotoId || lastDeviceVideoId > lastUploadedVideoId) {
	                            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	                            builder.setTitle(getString(R.string.app_name));
	                            builder.setMessage(getString(R.string.auto_upload_confirm));
	                            builder.setPositiveButton(getString(R.string.txt_ok), new DialogInterface.OnClickListener() {
	                                @Override
	                                public void onClick(DialogInterface dialog, int which) {
	                                    startService(new Intent(MainActivity.this, AutoUploadService.class));
	                                }
	                            });
	                            builder.setNegativeButton(getString(R.string.remind_me), new DialogInterface.OnClickListener() {
	                                @Override
	                                public void onClick(DialogInterface dialog, int which) {

	                                }
	                            });
	                            switch (Utils.getAutoUploadOption(MainActivity.this)) {
	                                case Constant.WI_FI_ONLY:
	                                    Log.d(TAG, "Auto Upload option is Wi-Fi only, check for Wi-Fi");
	                                    if (Utils.isWifiConnected(MainActivity.this)) {
	                                        Log.d(TAG, "Wi-Fi is connected, start auto upload");
	                                        runOnUiThread(new Runnable() {
	                                            @Override
	                                            public void run() {
	                                                builder.show();
	                                            }
	                                        });
	                                    }
	                                    break;
	                                case Constant.WI_FI_CELL:
	                                    Log.d(TAG, "Auto Upload option is Wi-Fi + Cell, check for connection");
	                                    if (Utils.isNetConeccted(MainActivity.this)) {
	                                        Log.d(TAG, "There is connection, start auto upload");
	                                        runOnUiThread(new Runnable() {
	                                            @Override
	                                            public void run() {
	                                                builder.show();
	                                            }
	                                        });
	                                    }
	                                    break;
	                            }
	                        }
	                    } else {
	                        Log.d(TAG, "Auto Upload disabled");
	                        Tracker tracker1 = EasyTracker.getTracker();
	                        tracker1.setCustomDimension(2, Constant.GA_AUTO_UPLOAD_DISABLED);
	                        tracker1.trackView(Constant.GA_FILES_VIEW);
	                    }
	        			
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
            
            if (LoadingDialog != null) {
                try {
                	LoadingDialog.dismiss();
                } catch (Exception ignored) {
                }
            }
                    	
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