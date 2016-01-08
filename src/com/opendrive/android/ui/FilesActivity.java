package com.opendrive.android.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParserException;

import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.MimeTypeParser;
import com.opendrive.android.common.MimeTypes;
import com.opendrive.android.common.Utils;
import com.opendrive.android.custom.ActionItem;
import com.opendrive.android.custom.QuickAction;
import com.opendrive.android.datamodel.DeleteFileData;
import com.opendrive.android.datamodel.FileData;
import com.opendrive.android.datamodel.LoginData;
import com.opendrive.android.db.DataBaseAdapter;
import com.opendrive.android.parser.DeletFileDataParser;
import com.opendrive.android.parser.FileDataParser;
import com.opendrive.android.parser.LoginDataParser;
import com.opendrive.android.parser.SharedDirectoryDataParser;
import com.opendrive.android.parser.SharedFileDataParser;
import com.opendrive.android.parser.SharedFolderDataParser;
import com.opendrive.android.parser.SharedUsersListDataParser;
import com.opendrive.android.request.Request;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

@SuppressLint("NewApi")
public class FilesActivity extends Activity {

    private FileListAdapter mFileListadapter;
    private ListView mFileListView = null;
    private ProgressDialog LoadingDialog = null;
    private boolean bIsLoadingStop = false;
    ArrayList<FileData> mFileListValues = new ArrayList<FileData>();
    private AlertDialog.Builder validationAlertDialog = null;
    private AlertDialog.Builder switchOfflineModeAlertDialog = null;
    private AlertDialog.Builder NetworkErrorAlertDialog = null;
    private FileDataParser mFileDataParser = null;
    // private Button btnBack;
    private FrameLayout layoutRefresh;
    private FrameLayout layoutNewfolder;
    private Button btnRefresh;
    private TextView btnOffline;
    private Button btnNewFolder;
    private TextView btnOfflineFolder;

    private int mFolderDeep = -1;
    private boolean mBackKeyPressed = false;
    private boolean mRefresh = false;
    public static FileData mCurrentFolderData = null;
    private MimeTypes mtMimeTypes;

    private SharedFolderDataParser mSharedFolderDataParser = null;
    private boolean mHasSharedFolers = false;
    private FileData mSharedFolers = null;
    private boolean mIsExploreSharedFolder = false;

    private SharedUsersListDataParser mSharedUsersListDataParser = null;
    private String mShareUserID = "";

    private SharedDirectoryDataParser mSharedDirectoryDataParser = null;
    private String mDirID = "";

    private SharedFileDataParser mSharedFileDataParser = null;

    private static final int ID_SHARE = 1;
    private static final int ID_COPYLINK = 2;
    private static final int ID_DELETE = 3;
    private static final int ID_TRASH = 4;

    // private ImageView accessoryView = null;
    private QuickAction mQuickAction = null;
    private int mSelectedItem = 0;
    private TextView mtxtSaveForOffline = null;
    private ImageView mImgSaveForOffline = null;
    private TextView pathView = null;

    private DeletFileDataParser mDeletFileDataParser = null;
    private DeleteFileData mDeleteFileData = null;

    private DataBaseAdapter mDataBaseAdapter = null;
    boolean m_bRefresh = true;
    static boolean offlineMode = false;
    boolean m_bFirstFlag = false;

    private Vector<FileData> m_pLogData = new Vector<FileData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.files);

        MainActivity.fileActivityInstance = this;

        mDataBaseAdapter = new DataBaseAdapter(this);

        SharedPreferences settingPrefer = getSharedPreferences(Constant.PREFS_NAME, 0);
        String strIsDB = settingPrefer.getString("initDataBase", "0");

        if (strIsDB.equals("0")) {
            mDataBaseAdapter.openDatabase();
            mDataBaseAdapter.drop();
            // mDataBaseAdapter.createDB();

            final SharedPreferences.Editor editor = settingPrefer.edit();
            editor.putString("initDataBase", "1");
            editor.commit();
        } else {
            mDataBaseAdapter.openDatabase();
        }

        initValidationAlertDialog();
        initLoadingDialog();
        initNetworkErrorAlertDialog();

        getMimeTypes();

        mFileListView = (ListView) findViewById(R.id.listView_files);
        mFileListView.setOnItemClickListener(new DataItemClickListener());

        // pathView = (TextView)findViewById(R.id.folder_path);

        if (android.os.Build.VERSION.SDK_INT >= 10) {
            mFileListView.setOverscrollFooter(null);
        }

        ActionItem shareItem = new ActionItem(ID_SHARE, "Share", getResources().getDrawable(R.drawable.ic_share));
        ActionItem copyItem = new ActionItem(ID_COPYLINK, "Copy link", getResources().getDrawable(R.drawable.ic_copylink));
        ActionItem deleteItem = new ActionItem(ID_DELETE, "Save for offline", getResources().getDrawable(R.drawable.ic_delete));
        ActionItem trashItem = new ActionItem(ID_TRASH, "Trash", getResources().getDrawable(R.drawable.ic_trash));

        shareItem.setSticky(true);
        copyItem.setSticky(true);
        deleteItem.setSticky(true);
        trashItem.setSticky(true);

        mQuickAction = new QuickAction(this);
        mQuickAction.addActionItem(shareItem);
        mQuickAction.addActionItem(copyItem);
        mQuickAction.addActionItem(deleteItem);
        mQuickAction.addActionItem(trashItem);

        // setup the action item click listener
        mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction quickAction, int pos, int actionId) {
                ActionItem actionItem = quickAction.getActionItem(pos);

                if (actionId == ID_SHARE) {
                    Toast.makeText(getApplicationContext(), "Add item selected", Toast.LENGTH_SHORT).show();
                } else if (actionId == ID_COPYLINK) {

                } else if (actionId == ID_DELETE) {
                    // Toast.makeText(getApplicationContext(), actionItem.getTitle() + " selected", Toast.LENGTH_SHORT).show();
                } else if (actionId == ID_TRASH) {
                    // Toast.makeText(getApplicationContext(), actionItem.getTitle() + " selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mQuickAction.setOnDismissListener(new QuickAction.OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });

        // btnRefresh = (Button)findViewById(R.id.button_refresh);
        // btnRefresh.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View arg0) {
        //
        // }
        // });
        //
        // btnNewFolder= (Button)findViewById(R.id.button_folder);
        // btnNewFolder.setOnClickListener(new OnClickListener() {
        // @Override
        // public void onClick(View arg0) {
        //
        // }
        // });
        //
        // layoutRefresh = (FrameLayout)findViewById(R.id.layout_refresh);
        // layoutRefresh.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View arg0) {
        // // TODO Auto-generated method stub
        // boolean bState = isNetConeccted();
        // if(bState){
        // btnOfflineFolder.setVisibility(View.INVISIBLE);
        // btnNewFolder.setVisibility(View.VISIBLE);
        // btnOffline.setVisibility(View.INVISIBLE);
        // btnRefresh.setVisibility(View.VISIBLE);
        // refresh();
        // }else{
        // btnOfflineFolder.setVisibility(View.VISIBLE);
        // btnNewFolder.setVisibility(View.INVISIBLE);
        // btnOffline.setVisibility(View.VISIBLE);
        // btnRefresh.setVisibility(View.INVISIBLE);
        // }
        // }
        // });
        //
        // layoutNewfolder = (FrameLayout)findViewById(R.id.layout_newfolder);
        // layoutNewfolder.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View arg0) {
        // // TODO Auto-generated method stub
        // boolean bState = isNetConeccted();
        // if(bState){
        // btnOfflineFolder.setVisibility(View.INVISIBLE);
        // btnNewFolder.setVisibility(View.VISIBLE);
        // btnOffline.setVisibility(View.INVISIBLE);
        // btnRefresh.setVisibility(View.VISIBLE);
        // newfolder();
        // }else{
        // btnOfflineFolder.setVisibility(View.VISIBLE);
        // btnNewFolder.setVisibility(View.INVISIBLE);
        // btnOffline.setVisibility(View.VISIBLE);
        // btnRefresh.setVisibility(View.INVISIBLE);
        // }
        // }
        // });
        //
        // btnOffline = (TextView)this.findViewById(R.id.btnOffline);
        // btnOfflineFolder = (TextView)this.findViewById(R.id.text_folder_offline);

        mFileListadapter = new FileListAdapter(this, R.layout.fileitem, mFileListValues);
        mFileListView.setAdapter(mFileListadapter);
        mFileListView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        mFileListadapter.notifyDataSetChanged();

        offlineMode = !isNetConeccted();
        creatCheckNetwork();

        if (!offlineMode) {
            btnOffline.setVisibility(View.INVISIBLE);
            btnRefresh.setVisibility(View.VISIBLE);
            btnOfflineFolder.setVisibility(View.INVISIBLE);
            btnNewFolder.setVisibility(View.VISIBLE);
        } else {
            btnOffline.setVisibility(View.VISIBLE);
            btnRefresh.setVisibility(View.INVISIBLE);
            btnOfflineFolder.setVisibility(View.VISIBLE);
            btnNewFolder.setVisibility(View.INVISIBLE);
        }

        checkSharedFolder();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public boolean isNetConeccted() {

        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        boolean bState = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
        boolean bState3g = conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
        if (bState || bState3g)
            return true;

        return false;
    }

    void creatCheckNetwork() {

        m_bRefresh = true;
        new DoBackgroundWork().execute(Constant.BACKGROUND_CREATE_CHECK_NETWORK);   
    }

    Handler refreshListHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (!offlineMode) {
                btnOffline.setVisibility(View.INVISIBLE);
                btnRefresh.setVisibility(View.VISIBLE);
                btnOfflineFolder.setVisibility(View.INVISIBLE);
                btnNewFolder.setVisibility(View.VISIBLE);
            } else {
                btnOffline.setVisibility(View.VISIBLE);
                btnRefresh.setVisibility(View.INVISIBLE);
                btnOfflineFolder.setVisibility(View.VISIBLE);
                btnNewFolder.setVisibility(View.INVISIBLE);
            }
            mFileListadapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (m_bRefresh == false)
            creatCheckNetwork();

        if (EventHandler.mMultiSelectData == null || EventHandler.mMultiSelectData.size() < 1) {
            boolean bState = isNetConeccted();
            if (bState && m_bFirstFlag) {
                refresh();
            }
        } else {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        m_bRefresh = false;
    }

    public void checkSharedFolder() {
        LoadingDialog.show();

        new DoBackgroundWork().execute(Constant.BACKGROUND_CHECK_SHARE_FOLDER);   
    }

    public void refresh() {
        mRefresh = true;

        if (offlineMode) {
            return;
        }

        if (mIsExploreSharedFolder) {
            if (mFolderDeep > 2) {
                getSharedFileList(mCurrentFolderData);
            } else if (mFolderDeep == 2) {
                getSharedFoldersList(mShareUserID);
            } else if (mFolderDeep == 1) {
                getSharedUsersList();
            }
        } else {
            if (mFolderDeep == 0)
                checkSharedFolder();
            else
                getFileList(mCurrentFolderData);
        }
    }

    public void newfolder() {

        m_bFirstFlag = true;
        NewFileActivity.mCurrentFolder = mCurrentFolderData;
        Intent intent = new Intent(this, NewFileActivity.class);
        startActivity(intent);
    }

    public void initValidationAlertDialog() {

        if (validationAlertDialog == null)
            validationAlertDialog = new AlertDialog.Builder(FilesActivity.this);

        validationAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
        validationAlertDialog.setTitle(R.string.txt_warning);
        validationAlertDialog.setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

                return;
            }
        });

        if (switchOfflineModeAlertDialog == null)
            switchOfflineModeAlertDialog = new AlertDialog.Builder(FilesActivity.this);

        switchOfflineModeAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
        switchOfflineModeAlertDialog.setTitle(R.string.txt_warning);
        switchOfflineModeAlertDialog.setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

                if (mCurrentFolderData == null)
                    getFileListFromDB("0");
                else
                    getFileListFromDB(mCurrentFolderData.getID());

                return;
            }
        });
    }

    public void initNetworkErrorAlertDialog() {
        if (NetworkErrorAlertDialog == null)
            NetworkErrorAlertDialog = new AlertDialog.Builder(FilesActivity.this);

        NetworkErrorAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
        NetworkErrorAlertDialog.setTitle(R.string.txt_warning);
        NetworkErrorAlertDialog.setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

                LoginHandler.sendMessage(LoginHandler.obtainMessage());
                // Constant.SessionID = "";
                // Constant.UserName = "";
                // Constant.Password = "";

                // showLoginActivity();
                // finish();
            }
        });
    }

    public void showLoginActivity() {
        Constant.SessionID = "";
        Constant.UserName = "";
        Constant.Password = "";
        Intent intent = new Intent(this, LogIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void initLoadingDialog() {

        if (LoadingDialog == null)
            LoadingDialog = new ProgressDialog(this);

        LoadingDialog.setCancelable(false);
        LoadingDialog.setCanceledOnTouchOutside(false);
        
        LoadingDialog.setMessage(getString(R.string.txt_loading));
        LoadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    LoadingDialog.cancel();
                    bIsLoadingStop = true;
                }
                return false;
            }
        });

    }

    public void getFileList(final FileData fileItem) {
        LoadingDialog.setMessage(getString(R.string.txt_loading));
        LoadingDialog.show();
        new DoBackgroundWork().execute(Constant.BACKGROUND_GET_FILE_LIST, fileItem);   
    }

    public void getBackFileList(final FileData fileItem) {
        LoadingDialog.setMessage(getString(R.string.txt_loading));
        LoadingDialog.show();

        new DoBackgroundWork().execute(Constant.BACKGROUND_GET_BACK_FILE_LIST, fileItem);   
    }

    Handler ConnectionerrorMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            showNetworkErrorAlertDialog(getString(R.string.connection_error));
        }
    };

    public void showNetworkErrorAlertDialog(String errorString) {
        NetworkErrorAlertDialog.setMessage(errorString);
        NetworkErrorAlertDialog.show();
    }

    Handler SwitchOfflineMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            switchOfflineModeAlertDialog.setMessage(R.string.switch_offline);
            switchOfflineModeAlertDialog.show();
        }
    };

    public void showValidationAlertDialog(String errorString) {
        validationAlertDialog.setMessage(errorString);
        validationAlertDialog.show();
    }

    public void showValidationAlertDialog(String title, String errorString) {
        validationAlertDialog.setTitle(title);
        validationAlertDialog.setMessage(errorString);
        validationAlertDialog.show();
    }

    public class DataItemClickListener implements OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            onDataListItemClick(position);
        }
    }

    public void onDataListItemClick(int position) {

        long itemID = mFileListadapter.getItemId(position);
        if (itemID == -1)
            return;

        FileData fileItem = (FileData) mFileListadapter.getItem(position);
        m_pLogData.add(fileItem);
        // kkh
        if (offlineMode) {
            if (fileItem.getIsFolder()) {
                mCurrentFolderData = fileItem;
                getFileListFromDB(fileItem.getID());
                return;
            } else {

                String fileFullPath = Utils.getFolderFullPath(mDataBaseAdapter.allFileDataList, fileItem);
                File tmpFile = new File(fileFullPath);

                if (tmpFile.exists()) {
                    openContents(tmpFile);
                } else
                    Toast.makeText(this, getString(R.string.msg_non_exist_file), Toast.LENGTH_LONG).show();
                return;

            }
        }

        if (fileItem.getIsSharedFolders() && mFolderDeep == 0) {

            mIsExploreSharedFolder = true;
            getSharedUsersList();

        } else if (mIsExploreSharedFolder) {
            if (mFolderDeep == 1) {
                mShareUserID = fileItem.getUserID();
                getSharedFoldersList(fileItem.getUserID());

            } else if (mFolderDeep == 2) {
                mDirID = fileItem.getID();
                mCurrentFolderData = fileItem;
                getSharedFileList(fileItem);
            } else if (mFolderDeep > 2) {
                getSharedFileList(fileItem);
                mCurrentFolderData = fileItem;
            }
        } else {

            if (fileItem.getIsFolder()) {

                if (mFolderDeep == 0) {
                    getFileList(fileItem);
                } else {
                    getFileList(fileItem);
                }
            } else {

                String fileFullPath = Utils.getFolderFullPath(fileItem);
                File tmpFile = new File(fileFullPath);

                if (tmpFile.exists()) {
                    openContents(tmpFile);
                } else {
                    if (mFileListadapter.isEnable(fileItem))
                        downloadFile(fileItem);
                }
            }
        }
    }

    public void downloadFile(final FileData fileData) {
        LoadingDialog.setMessage(getString(R.string.txt_downloading));
        LoadingDialog.show();
        
        new DoBackgroundWork().execute(Constant.BACKGROUND_DOWNLOAD_FILE, fileData);   
    }

    public void downloadFile(final FileData fileData, final boolean isOpen) {
        LoadingDialog.setMessage(getString(R.string.txt_downloading));
        LoadingDialog.show();

        new DoBackgroundWork().execute(Constant.BACKGROUND_DOWNLOAD_FILE_WITH_OPEN, fileData, new Boolean(isOpen));   
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        getBack();
    }

    public void getBack() {
        // TODO Auto-generated method stub
        mBackKeyPressed = true;

        // kkh
        if (offlineMode) {

            if (mFolderDeep == 1) {
                mCurrentFolderData = null;
                getFileListFromDB("0");
            } else if (mFolderDeep > 1) {

                FileData fileItem = mDataBaseAdapter.selectFileData(mCurrentFolderData.getParentID());
                mCurrentFolderData = fileItem;
                getFileListFromDB(fileItem.getID());
            } else
                finish();

            return;
        }
        if (mIsExploreSharedFolder) {
            if (mFolderDeep > 3) {
                getSharedFileList(mCurrentFolderData.getParent());
            } else if (mFolderDeep == 3) {
                getSharedFoldersList(mShareUserID);
            } else if (mFolderDeep == 2) {
                getSharedUsersList();
            } else {
                getFileList(null);
                mIsExploreSharedFolder = false;
            }

        } else {
            if (mFolderDeep > 0 && mCurrentFolderData != null) {
                getBackFileList(mCurrentFolderData.getParent());
            } else
                finish();
        }
    }

    private class FileListAdapter extends BaseAdapter {

        public ArrayList<FileData> items;

        public FileListAdapter(Context handler, int textViewResourceId, ArrayList<FileData> items) {
            super();
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            int j = 0;
            int isActionMenu = -1;
            FileData file = null;
            for (int i = 0; i <= position; i++) {
                file = items.get(j);
                if (file.getIsFolder() || (!file.isShowActionMenu())) {
                    j++;
                    isActionMenu = -1;
                } else {

                    if (isActionMenu == -1)
                        isActionMenu = 0;
                    else if (isActionMenu == 0) {
                        j++;
                        isActionMenu = 1;
                    } else if (isActionMenu == 1) {
                        isActionMenu = 0;
                    }
                }
            }

            final FileData fileItem = file;
            View v = null;// convertView;
            // if (v == null) {

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (isActionMenu == 1) {
                v = inflater.inflate(R.layout.menuitem, null);
            } else {
                v = inflater.inflate(R.layout.fileitem, null);
            }
            // }

            if (fileItem != null) {

                if (isActionMenu == 1) {

                    LinearLayout copyLink = (LinearLayout) v.findViewById(R.id.copylink_view);
                    LinearLayout share = (LinearLayout) v.findViewById(R.id.share_view);
                    LinearLayout offline = (LinearLayout) v.findViewById(R.id.save_offline_view);
                    LinearLayout trash = (LinearLayout) v.findViewById(R.id.trash_view);

                    String fileFullPath = Utils.getFolderFullPath(fileItem);
                    File file1 = new File(fileFullPath);
                    if (file1.exists()) {
                        ((TextView) (offline.findViewById(R.id.txt_save_for_offline))).setText(R.string.label_deleteoffline);
                        ((ImageView) (offline.findViewById(R.id.img_save_for_offline))).setImageResource(R.drawable.ic_delete);
                    } else {
                        ((TextView) (offline.findViewById(R.id.txt_save_for_offline))).setText(R.string.label_saveforoffline);
                        ((ImageView) (offline.findViewById(R.id.img_save_for_offline))).setImageResource(R.drawable.ic_save);
                    }

                    copyLink.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            actionHandler(arg0, fileItem, items.indexOf(fileItem));
                        }
                    });
                    share.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            actionHandler(arg0, fileItem, items.indexOf(fileItem));
                        }
                    });
                    offline.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            actionHandler(arg0, fileItem, items.indexOf(fileItem));
                        }
                    });
                    trash.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            actionHandler(arg0, fileItem, items.indexOf(fileItem));
                        }
                    });
                } else {

                    ImageView iconImageView = (ImageView) v.findViewById(R.id.imageView_fileThumb);
                    TextView fileName = (TextView) v.findViewById(R.id.txtView_fileName);
                    TextView fileProperty = (TextView) v.findViewById(R.id.txtView_fileProperty);
                    LinearLayout filePropertyButton = (LinearLayout) v.findViewById(R.id.linearLayout_fileProperty);
                    ImageView accessoryView = (ImageView) filePropertyButton.findViewById(R.id.imageView1);
                    if (fileItem.isShowActionMenu())
                        accessoryView.setImageResource(R.drawable.less);
                    else
                        accessoryView.setImageResource(R.drawable.more);

                    if (fileItem.getIsFolder()) {
                        if (fileItem.getDateModified() == null) {
                            fileProperty.setVisibility(View.GONE);
                        } else {
                            fileProperty.setVisibility(View.VISIBLE);
                            fileProperty.setText(fileItem.getDateModified());
                        }

                        iconImageView.setImageResource(R.drawable.folder);
                        fileName.setText(fileItem.getName());
                        filePropertyButton.setVisibility(View.GONE);

                    } else {

                        fileProperty.setVisibility(View.VISIBLE);
                        fileName.setText(fileItem.getName());
                        fileProperty.setText(fileItem.getSize() + " | " + fileItem.getDateModified());
                        filePropertyButton.setVisibility(View.VISIBLE);

                        if (isEnable(fileItem))
                            fileName.setTextColor(Color.BLACK);
                        else
                            fileName.setTextColor(Color.GRAY);

                        filePropertyButton.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                // TODO Auto-generated method stub
                                // FilePropertyActivity.fileItem = fileItem;
                                if (isEnable(fileItem))
                                    showSubMenu(arg0, fileItem, items.indexOf(fileItem));
                            }
                        });
                    }

                    Bitmap mIcon = fileItem.getIcon();

                    if (mIcon == null)
                        iconImageView.setImageResource(R.drawable.placeholder);
                    else
                        iconImageView.setImageBitmap(mIcon);
                }
            }
            return v;
        }

        boolean isEnable(FileData fileItem) {

            // ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Activity.CONNECTIVITY_SERVICE);
            // boolean wifi = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
            boolean wifi = isNetConeccted();
            String fileFullPath = Utils.getFolderFullPath(fileItem);
            File file1 = new File(fileFullPath);
            if (!file1.exists() && !wifi)
                return false;
            return true;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            int action_menu_count = 0;
            for (int i = 0; i < this.items.size(); i++) {

                FileData file = this.items.get(i);
                if (file.isShowActionMenu())
                    action_menu_count++;
            }

            return this.items.size() + action_menu_count;
        }

        @Override
        public Object getItem(int position) {

            int j = 0;
            int isActionMenu = -1;
            FileData file = null;
            for (int i = 0; i <= position; i++) {
                file = items.get(j);
                if (file.getIsFolder() || (!file.isShowActionMenu())) {
                    j++;
                    isActionMenu = -1;
                } else {

                    if (isActionMenu == -1)
                        isActionMenu = 0;
                    else if (isActionMenu == 0) {
                        j++;
                        isActionMenu = 1;
                    } else if (isActionMenu == 1) {
                        isActionMenu = 0;
                    }
                }
            }

            return file;
        }

        @Override
        public long getItemId(int position) {

            int j = 0;
            int isActionMenu = -1;
            FileData file = null;
            for (int i = 0; i <= position; i++) {
                file = items.get(j);
                if (file.getIsFolder() || (!file.isShowActionMenu())) {
                    j++;
                    isActionMenu = -1;
                } else {

                    if (isActionMenu == -1)
                        isActionMenu = 0;
                    else if (isActionMenu == 0) {
                        j++;
                        isActionMenu = 1;
                    } else if (isActionMenu == 1) {
                        isActionMenu = 0;
                    }
                }
            }

            if (isActionMenu == 1)
                return -1;

            return items.indexOf(file);
        }
    }

    public void actionHandler(View view, FileData fileItem, int position) {

        switch (view.getId()) {

            case R.id.copylink_view:
                String publicLink = fileItem.getDirectLinkPublic();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(publicLink);
                break;

            case R.id.share_view:
                emailLink(fileItem);
                break;

            case R.id.save_offline_view:

                mtxtSaveForOffline = (TextView) view.findViewById(R.id.txt_save_for_offline);
                mImgSaveForOffline = (ImageView) view.findViewById(R.id.img_save_for_offline);
                String fileFullPath = Utils.getFolderFullPath(fileItem);
                final File offlineFile = new File(fileFullPath);

                if (offlineFile.exists()) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                    alertDialog.setIcon(R.drawable.ic_dialog_alarm);
                    alertDialog.setTitle(R.string.app_name);
                    alertDialog.setMessage(getString(R.string.msg_confirm_deleteoffline, fileItem.getName()));
                    alertDialog.setPositiveButton(R.string.txt_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {

                            return;
                        }
                    });
                    alertDialog.setNegativeButton(R.string.txt_delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {

                            offlineFile.delete();
                            mtxtSaveForOffline.setText(R.string.label_saveforoffline);
                            mImgSaveForOffline.setImageResource(R.drawable.ic_save);
                            return;
                        }
                    });
                    alertDialog.show();
                } else {
                    downloadFile(fileItem, false);
                }
                break;

            case R.id.trash_view:
                // TODO Auto-generated method stub
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                final FileData item = fileItem;

                alertDialog.setIcon(R.drawable.ic_dialog_alarm);
                alertDialog.setTitle(R.string.app_name);
                alertDialog.setMessage(getString(R.string.msg_confirm_trashoffline, fileItem.getName()));
                alertDialog.setPositiveButton(R.string.txt_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                        return;
                    }
                });
                alertDialog.setNegativeButton(R.string.txt_trash, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                        deleteFile(item);
                        return;
                    }
                });
                alertDialog.show();
                break;

        }
    }

    public void showSubMenu(View view, FileData fileItem, int position) {

        if (fileItem.isShowActionMenu() == false) {
            ImageView accessoryView = (ImageView) view.findViewById(R.id.imageView1);
            accessoryView.setImageResource(R.drawable.less);
            fileItem.setShowActionMenu(true);
        } else {
            ImageView accessoryView = (ImageView) view.findViewById(R.id.imageView1);
            accessoryView.setImageResource(R.drawable.more);
            fileItem.setShowActionMenu(false);
        }

        mFileListadapter.notifyDataSetChanged();
    }

    public void showFilePropertyActivity() {
        Intent intent = new Intent(this, FilePropertyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    Handler getIconHandler = new Handler() {

        public void handleMessage(Message msg) {

            new Thread(new Runnable() {

                public void run() {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    // int fileCount = mFileListValues.size();

                    for (int i = 0; i < mFileListValues.size(); i++) {

                        FileData fileData = mFileListValues.get(i);

                        if (fileData.getIsFolder()) {
                            Utils.makeFolders(fileData);
                        }

                        Bitmap mIcon = fileData.getIcon();

                        if (mIcon != null)
                            continue;

                        Bitmap iconBitmap = null;

                        if (fileData.getIsFolder())
                            iconBitmap = Utils.getIconFromFileID(fileData.getID(), true);
                        else
                            iconBitmap = Utils.getIconFromFileID(fileData.getID(), true);

                        if (iconBitmap == null) {
                            Request getIconRequest = new Request();

                            String getIconUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;

                            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                            postData.add(new BasicNameValuePair("action", "get_thumbnail"));
                            postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                            postData.add(new BasicNameValuePair("shared_user_id", ""));

                            if (fileData.getIsFolder()) {
                                postData.add(new BasicNameValuePair("file_id", ""));
                                postData.add(new BasicNameValuePair("dir_id", fileData.getID()));
                            } else {
                                postData.add(new BasicNameValuePair("file_id", fileData.getID()));
                                postData.add(new BasicNameValuePair("dir_id", ""));
                            }

                            try {
                                if (fileData.getIsFolder())
                                    iconBitmap = getIconRequest.getFileIcon(getIconUrlString, postData, fileData.getID());
                                else
                                    iconBitmap = getIconRequest.getFileIcon(getIconUrlString, postData, fileData.getID());
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        if (iconBitmap != null) {
                            fileData.setIcon(iconBitmap);

                            runOnUiThread(new Runnable() {

                                public void run() {
                                    if (mFileListadapter != null)
                                        mFileListadapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            fileData.setIcon(null);
                        }
                    }
                }
            }).start();
        }
    };

    Handler getIconHandlerFromLocal = new Handler() {

        public void handleMessage(Message msg) {

            new Thread(new Runnable() {

                public void run() {

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    for (int i = 0; i < mFileListValues.size(); i++) {

                        FileData fileData = mFileListValues.get(i);

                        if (fileData.getIsFolder()) {
                            Utils.makeFolders(fileData);
                        }

                        Bitmap mIcon = fileData.getIcon();

                        if (mIcon != null)
                            continue;

                        Bitmap iconBitmap = null;

                        iconBitmap = Utils.getIconFromFileID(fileData.getID(), true);

                        if (iconBitmap != null) {
                            fileData.setIcon(iconBitmap);

                            runOnUiThread(new Runnable() {

                                public void run() {
                                    if (mFileListadapter != null)
                                        mFileListadapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            fileData.setIcon(null);
                        }
                    }
                }
            }).start();
        }
    };

    public void openContents(File file) {
        if (!file.exists()) {
            return;
        }

        if (mtMimeTypes == null)
            getMimeTypes();

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

        Uri data = Uri.fromFile(file);
        String type = mtMimeTypes.getMimeType(file.getName());
        intent.setDataAndType(data, type);

        startActivity(intent);
    }

    private void getMimeTypes() {
        MimeTypeParser mtp = new MimeTypeParser();
        XmlResourceParser in = getResources().getXml(R.xml.mimetypes);

        try {
            mtMimeTypes = mtp.fromXmlResource(in);
        } catch (XmlPullParserException e) {
            Log.e("tmRecordListView", "PreselectedChannelsActivity: XmlPullParserException", e);
            throw new RuntimeException("PreselectedChannelsActivity: XmlPullParserException");
        } catch (IOException e) {
            Log.e("tmRecordListView", "PreselectedChannelsActivity: IOException", e);
            throw new RuntimeException("PreselectedChannelsActivity: IOException");
        }
    }

    public void playAudio(String streamingUrl) {

        final MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(streamingUrl);
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void getSharedUsersList() {
        LoadingDialog.setMessage(getString(R.string.txt_loading));
        LoadingDialog.show();

        mShareUserID = "";

        new DoBackgroundWork().execute(Constant.BACKGROUND_GET_SHARED_USERS_LIST);   
    }

    public void getSharedFoldersList(final String shareUserID) {

        LoadingDialog.setMessage(getString(R.string.txt_loading));
        LoadingDialog.show();
   
        new DoBackgroundWork().execute(Constant.BACKGROUND_GET_SHARED_FOLDERS_LIST, shareUserID);   
    }

    public void getSharedFileList(final FileData parentFileItem) {

        LoadingDialog.setMessage(getString(R.string.txt_loading));
        LoadingDialog.show();

        new DoBackgroundWork().execute(Constant.BACKGROUND_GET_SHARED_FILE_LIST, parentFileItem);   
    }

    public void emailLink(FileData fileItem) {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

	/* Fill it with Data */
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"to@email.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.msg_emailsubject,/* fileItem.getName()*/ "this is for fun"));
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.msg_emailtext, fileItem.getName(), fileItem.getDirectLink()));

	/* Send it off to the Activity-Chooser */
        startActivity(Intent.createChooser(emailIntent, "Send mail"));
    }

    public void deleteFile(FileData fileItem) {
        LoadingDialog.setMessage(getString(R.string.txt_deleting));
        LoadingDialog.show();

        new DoBackgroundWork().execute(Constant.BACKGROUND_DELETE_FILE, fileItem);
    }

    Handler DeleteFileFailedMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (mDeleteFileData.getName().length() > 0)
                validationAlertDialog.setTitle(mDeleteFileData.getName());
            else
                validationAlertDialog.setTitle("Unknow Error");

            if (mDeleteFileData.getDescription().length() > 0)
                validationAlertDialog.setMessage(mDeleteFileData.getDescription());
            else
                validationAlertDialog.setMessage("Unknow Error");

            validationAlertDialog.show();
        }
    };

    // kkh
    public void getFileListFromDB(final String parentID) {
    	new DoBackgroundWork().execute(Constant.BACKGROUND_GET_FILE_LIST_FROM_DB, parentID);
    }

    public void login() {
        LoadingDialog.show();
  
        new DoBackgroundWork().execute(Constant.BACKGROUND_LOGIN);
    }

    Handler LoginHandler = new Handler() {
        public void handleMessage(Message msg) {
            login();
        }
    };

    Handler LoginFailedMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            showLoginActivity();
        }
    };
    
    private class DoBackgroundWork extends AsyncTask<Object, Integer, Integer> {
        protected Integer doInBackground(Object... params) {
			Integer backgroundTaskID = (Integer) params[0];
			
    		switch (backgroundTaskID) {
        		case Constant.BACKGROUND_CREATE_CHECK_NETWORK:	                  
					while (m_bRefresh) {
					    boolean bState = !isNetConeccted();
					    if (offlineMode != bState) {
					        offlineMode = bState;
					        refreshListHandler.sendMessage(refreshListHandler.obtainMessage());
					    }
					}
					
					return Constant.BACKGROUND_PROCESS_OK;
        		case Constant.BACKGROUND_CHECK_SHARE_FOLDER:
	                try {

	                    Request loginRequest = new Request();

	                    String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
	                    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

	                    postData.add(new BasicNameValuePair("action", "has_share_folders"));
	                    postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
	                    String resultXML = loginRequest.httpPost(loginUrlString, postData);

	                    if (resultXML.equals(Constant.ErrorMessage)) {
	                        LoadingDialog.dismiss();
	                        return Constant.BACKGROUND_PROCESS_ERROR;
	                    } else {
	                        mSharedFolderDataParser = new SharedFolderDataParser();
	                        mHasSharedFolers = mSharedFolderDataParser.parseResponse(resultXML);

	                        LoadingDialog.dismiss();

	                        if (mHasSharedFolers && LogIn.mLoginData.getIsAccessUser().equals("False")) {
	                            mSharedFolers = new FileData("0", null);
	                            mSharedFolers.setID("Shared Folders");
	                            mSharedFolers.setName("Shared Folders");
	                            mSharedFolers.setIsFolder(true);
	                            mSharedFolers.setIsSharedFolders(true);
	                        }
	                    }

	                    Request getFileListRequest = new Request();

	                    String getFilListUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
	                    postData = new ArrayList<NameValuePair>();

	                    postData.add(new BasicNameValuePair("action", "list_dir"));
	                    postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
	                    postData.add(new BasicNameValuePair("share_user_id", ""));
	                    postData.add(new BasicNameValuePair("share_id", ""));
	                    postData.add(new BasicNameValuePair("access_dir_id", "0"));
	                    postData.add(new BasicNameValuePair("dir_id", "0"));
	                    resultXML = getFileListRequest.httpPost(getFilListUrlString, postData);

	                    if (bIsLoadingStop) {
	                        LoadingDialog.dismiss();
	                        bIsLoadingStop = false;
	                        mBackKeyPressed = false;
	                        mRefresh = false;
	                        return Constant.BACKGROUND_PROCESS_OK;
	                    }

	                    if (resultXML.equals(Constant.ErrorMessage)) {
	                        LoadingDialog.dismiss();
	                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
	                        return Constant.BACKGROUND_PROCESS_ERROR;
	                    } else {
	                        mFileDataParser = new FileDataParser();
	                        mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);

	                        if (mHasSharedFolers && mSharedFolers != null)
	                            mFileListValues.add(mSharedFolers);

	                        Collections.sort(mFileListValues, new Comparator<FileData>() {
	                            @Override
	                            public int compare(FileData object1, FileData object2) {
	                                if (object1.getIsFolder() && !object2.getIsFolder())
	                                    return -1;
	                                else if (!object1.getIsFolder() && object2.getIsFolder())
	                                    return 1;
	                                else
	                                    return object1.getName().toLowerCase().compareTo(object2.getName().toLowerCase());
	                            }
	                        });

	                        mDataBaseAdapter.insert(mFileListValues);

	                        LoadingDialog.dismiss();
	                    }

	                    runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {

	                            mFileListadapter = new FileListAdapter(FilesActivity.this, R.layout.fileitem, mFileListValues);
	                            mFileListView.setAdapter(mFileListadapter);
	                            mFileListadapter.notifyDataSetChanged();

	                            mFolderDeep = 0;
	                            mRefresh = false;
	                            mBackKeyPressed = false;
	                        }
	                    });

	                    getIconHandler.sendMessage(getIconHandler.obtainMessage());
	                    
	                    return Constant.BACKGROUND_PROCESS_OK;	                    
	                    
	                } catch (Exception e) {
	                    // TODO Auto-generated catch block
	                    LoadingDialog.dismiss();

	                    String exceptionString = e.getMessage();
	                    if (FilesActivity.offlineMode) {
	                        // Network unreachable
	                        offlineMode = true;
	                        SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
	                    } else {
	                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
	                        e.printStackTrace();
	                    }
	                    
	                    return Constant.BACKGROUND_PROCESS_ERROR;	                    
	                }
        		
        		case Constant.BACKGROUND_GET_FILE_LIST:
        			final FileData fileItem1 = (FileData) params[1];
        			
                    try {
                        Request loginRequest = new Request();

                        String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                        postData.add(new BasicNameValuePair("action", "list_dir"));
                        postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                        postData.add(new BasicNameValuePair("share_user_id", ""));
                        postData.add(new BasicNameValuePair("share_id", ""));
                        if (fileItem1 == null) {
                            postData.add(new BasicNameValuePair("access_dir_id", "0"));
                            postData.add(new BasicNameValuePair("dir_id", "0"));
                        } else {
                            postData.add(new BasicNameValuePair("access_dir_id", fileItem1.getAccessDirID()));
                            postData.add(new BasicNameValuePair("dir_id", fileItem1.getID()));
                        }
                        String resultXML = loginRequest.httpPost(loginUrlString, postData);

                        if (bIsLoadingStop) {
                            LoadingDialog.dismiss();
                            bIsLoadingStop = false;
                            mBackKeyPressed = false;
                            mRefresh = false;
                            return Constant.BACKGROUND_PROCESS_OK;
                        }

                        if (resultXML.equals(Constant.ErrorMessage)) {
                            LoadingDialog.dismiss();
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            return Constant.BACKGROUND_PROCESS_ERROR;
                        } else {
                            mFileDataParser = new FileDataParser();

                            if (mBackKeyPressed) {
                                if (mFolderDeep == 1) {
                                    mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);

                                    if (mHasSharedFolers && mSharedFolers != null)
                                        mFileListValues.add(mSharedFolers);

                                } else
                                    mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem1.getAccessDirID(), fileItem1.getParent());
                            } else {
                                if (mFolderDeep == -1) {
                                    mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);

                                    if (mHasSharedFolers && mSharedFolers != null)
                                        mFileListValues.add(mSharedFolers);

                                } else if (mFolderDeep == 0) {
                                    if (fileItem1 == null)
                                        mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);
                                    else
                                        mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem1.getID(), fileItem1);

                                } else if (mFolderDeep == 1) {
                                    mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem1.getID(), fileItem1);
                                } else {
                                    mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem1.getAccessDirID(), fileItem1);
                                }
                            }

                            Collections.sort(mFileListValues, new Comparator<FileData>() {
                                @Override
                                public int compare(FileData object1, FileData object2) {
                                    if (object1.getIsFolder() && !object2.getIsFolder())
                                        return -1;
                                    else if (!object1.getIsFolder() && object2.getIsFolder())
                                        return 1;
                                    else
                                        return object1.getName().toLowerCase().compareTo(object2.getName().toLowerCase());
                                }
                            });

                            mDataBaseAdapter.insert(mFileListValues);
                            LoadingDialog.dismiss();
                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        LoadingDialog.dismiss();

                        String exceptionString = e.getMessage();
                        if (FilesActivity.offlineMode) {
                            // Network unreachable
                            offlineMode = true;
                            SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
                        } else {
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            e.printStackTrace();
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mBackKeyPressed) {
                                mFolderDeep--;
                                mCurrentFolderData = fileItem1;
                            } else if (mRefresh) {
                                mRefresh = false;
                            } else {
                                mCurrentFolderData = fileItem1;
                                mFolderDeep++;
                            }

                            if (mCurrentFolderData != null) {
                                String path = mCurrentFolderData.getPath();
                                pathView.setText("Root > " + path);
                            } else {
                                pathView.setText("Root");
                            }
                            // if(mFolderDeep >= 1)
                            // btnBack.setVisibility(View.VISIBLE);
                            // else
                            // btnBack.setVisibility(View.GONE);

                            mFileListadapter = new FileListAdapter(FilesActivity.this, R.layout.fileitem, mFileListValues);
                            mFileListView.setAdapter(mFileListadapter);
                            mFileListadapter.notifyDataSetChanged();

                            mBackKeyPressed = false;
                            mRefresh = false;

                        }
                    });

                    getIconHandler.sendMessage(getIconHandler.obtainMessage());
                        			
                    return Constant.BACKGROUND_PROCESS_OK;
        		case Constant.BACKGROUND_GET_BACK_FILE_LIST:
        			final FileData fileItem2 = (FileData) params[1];
        			
                    try {
                        Request loginRequest = new Request();

                        String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                        postData.add(new BasicNameValuePair("action", "list_dir"));
                        postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                        postData.add(new BasicNameValuePair("share_user_id", ""));
                        postData.add(new BasicNameValuePair("share_id", ""));
                        if (fileItem2 == null) {
                            postData.add(new BasicNameValuePair("access_dir_id", "0"));
                            postData.add(new BasicNameValuePair("dir_id", "0"));
                        } else {
                            postData.add(new BasicNameValuePair("access_dir_id", fileItem2.getAccessDirID()));
                            postData.add(new BasicNameValuePair("dir_id", fileItem2.getID()));
                        }
                        String resultXML = loginRequest.httpPost(loginUrlString, postData);

                        if (bIsLoadingStop) {
                            LoadingDialog.dismiss();
                            bIsLoadingStop = false;
                            mBackKeyPressed = false;
                            mRefresh = false;
                            return Constant.BACKGROUND_PROCESS_OK;
                        }

                        if (resultXML.equals(Constant.ErrorMessage)) {
                            LoadingDialog.dismiss();
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            return Constant.BACKGROUND_PROCESS_ERROR;
                        } else {
                            mFileDataParser = new FileDataParser();

                            if (mBackKeyPressed) {
                                if (mFolderDeep == 1) {
                                    mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);

                                    if (mHasSharedFolers && mSharedFolers != null)
                                        mFileListValues.add(mSharedFolers);

                                } else {
                                    Log.i("dsfsdfdfgdgfdg", fileItem2 + ":" + mFileDataParser);
                                    if (fileItem2 == null)
                                        mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);
                                    else
                                        mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem2.getAccessDirID(), fileItem2.getParent());
                                }
                            } else {
                                if (mFolderDeep == -1) {
                                    mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);

                                    if (mHasSharedFolers && mSharedFolers != null)
                                        mFileListValues.add(mSharedFolers);

                                } else if (mFolderDeep == 0) {
                                    if (fileItem2 == null)
                                        mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);
                                    else
                                        mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem2.getID(), fileItem2);

                                } else if (mFolderDeep == 1) {
                                    mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem2.getID(), fileItem2);
                                } else {
                                    mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem2.getAccessDirID(), fileItem2);
                                }
                            }

                            Collections.sort(mFileListValues, new Comparator<FileData>() {
                                @Override
                                public int compare(FileData object1, FileData object2) {
                                    if (object1.getIsFolder() && !object2.getIsFolder())
                                        return -1;
                                    else if (!object1.getIsFolder() && object2.getIsFolder())
                                        return 1;
                                    else
                                        return object1.getName().toLowerCase().compareTo(object2.getName().toLowerCase());
                                }
                            });

                            for (int i = 0; i < mFileListValues.size(); i++) {
                                FileData fileData = null;
                                fileData = mFileListValues.get(i);
                                if (fileItem2 == null)
                                    fileData.setParentID("0");
                                else
                                    fileData.setParentID(fileItem2.getID());
                            }
                            mDataBaseAdapter.insert(mFileListValues);

                            LoadingDialog.dismiss();
                        }

                        if (mBackKeyPressed) {
                            mFolderDeep--;
                            m_pLogData.remove(mCurrentFolderData);
                            mCurrentFolderData = fileItem2;
                        } else if (mRefresh) {
                            mRefresh = false;
                        } else {
                            m_pLogData.remove(mCurrentFolderData);
                            mCurrentFolderData = fileItem2;
                            mFolderDeep++;
                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        LoadingDialog.dismiss();

                        String exceptionString = e.getMessage();
                        if (FilesActivity.offlineMode) {
                            // Network unreachable
                            offlineMode = true;
                            SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
                        } else {
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            e.printStackTrace();
                        }

                        return Constant.BACKGROUND_PROCESS_ERROR;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

    			/*
                 * if(mBackKeyPressed){ mFolderDeep--; mCurrentFolderData = fileItem; } else if(mRefresh){ mRefresh = false; } else{ mCurrentFolderData = fileItem; mFolderDeep++; }
    			 */

                            if (mCurrentFolderData != null) {
                                String path = mCurrentFolderData.getPath();
                                pathView.setText("Root > " + path);
                            } else {
                                pathView.setText("Root");
                            }

                            mFileListadapter = new FileListAdapter(FilesActivity.this, R.layout.fileitem, mFileListValues);
                            mFileListView.setAdapter(mFileListadapter);
                            mFileListadapter.notifyDataSetChanged();

                            mBackKeyPressed = false;
                            mRefresh = false;

                        }
                    });

                    getIconHandler.sendMessage(getIconHandler.obtainMessage());
                        			
        			return Constant.BACKGROUND_PROCESS_OK; 
        		case Constant.BACKGROUND_DOWNLOAD_FILE:
                    try {
                    	FileData fileData = (FileData) params[1];
                        String fileFullPath = Utils.getFolderFullPath(fileData);                    	
                        Request downloadRequest = new Request();

                        String downloadRequestUrl = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;

                        ArrayList<NameValuePair> postDataArrayList = new ArrayList<NameValuePair>();

                        postDataArrayList.add(new BasicNameValuePair("action", "open_file_download"));
                        postDataArrayList.add(new BasicNameValuePair("session_id", Constant.SessionID));
                        postDataArrayList.add(new BasicNameValuePair("is_pro", ""));
                        postDataArrayList.add(new BasicNameValuePair("share_user_id", ""));
                        postDataArrayList.add(new BasicNameValuePair("share_id", ""));
                        postDataArrayList.add(new BasicNameValuePair("access_dir_id", ""));
                        postDataArrayList.add(new BasicNameValuePair("file_id", fileData.getID()));
                        postDataArrayList.add(new BasicNameValuePair("file_offset", ""));

                        boolean result = downloadRequest.downloadFile(downloadRequestUrl, postDataArrayList, fileFullPath, fileData);

                        if (bIsLoadingStop) {
                            bIsLoadingStop = false;
                            return Constant.BACKGROUND_PROCESS_OK;
                        }

                        LoadingDialog.dismiss();

                        if (!result) {
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            return Constant.BACKGROUND_PROCESS_ERROR;
                        } else {
                            openContents(new File(fileFullPath));
                        }
                        
                        return Constant.BACKGROUND_PROCESS_OK;
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        LoadingDialog.dismiss();
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        e.printStackTrace();
                        
                        return Constant.BACKGROUND_PROCESS_ERROR;
                    }
        		case Constant.BACKGROUND_DOWNLOAD_FILE_WITH_OPEN:
        			FileData fileData = (FileData) params[1];
        			final Boolean isOpen = (Boolean) params[2];
        			final String fileFullPath = Utils.getFolderFullPath(fileData);
        			
                    try {
                        Request downloadRequest = new Request();

                        String downloadRequestUrl = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;

                        ArrayList<NameValuePair> postDataArrayList = new ArrayList<NameValuePair>();

                        postDataArrayList.add(new BasicNameValuePair("action", "open_file_download"));
                        postDataArrayList.add(new BasicNameValuePair("session_id", Constant.SessionID));
                        postDataArrayList.add(new BasicNameValuePair("is_pro", ""));
                        postDataArrayList.add(new BasicNameValuePair("share_user_id", ""));
                        postDataArrayList.add(new BasicNameValuePair("share_id", ""));
                        postDataArrayList.add(new BasicNameValuePair("access_dir_id", ""));
                        postDataArrayList.add(new BasicNameValuePair("file_id", fileData.getID()));
                        postDataArrayList.add(new BasicNameValuePair("file_offset", ""));

                        boolean result = downloadRequest.downloadFile(downloadRequestUrl, postDataArrayList, fileFullPath, fileData);

                        if (bIsLoadingStop) {
                            bIsLoadingStop = false;
                            return Constant.BACKGROUND_PROCESS_OK;
                        }

                        LoadingDialog.dismiss();

                        if (!result) {
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            return Constant.BACKGROUND_PROCESS_ERROR;
                        } else {
                            runOnUiThread(new Runnable() {

                                public void run() {

                                    mtxtSaveForOffline.setText(R.string.label_deleteoffline);
                                    mImgSaveForOffline.setImageResource(R.drawable.ic_delete);

                                    if (isOpen)
                                        openContents(new File(fileFullPath));
                                    else {

                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FilesActivity.this);
                                        alertDialog.setIcon(R.drawable.ic_dialog_alarm);
                                        alertDialog.setTitle(R.string.app_name);
                                        alertDialog.setMessage(R.string.msg_confirm_successdownload);
                                        alertDialog.setPositiveButton(R.string.txt_close, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface arg0, int arg1) {

                                                return;
                                            }
                                        });
                                        alertDialog.show();
                                    }
                                }
                            });
                        }
                        
                        return Constant.BACKGROUND_PROCESS_OK;
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        LoadingDialog.dismiss();
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        e.printStackTrace();
                        
                        return Constant.BACKGROUND_PROCESS_ERROR;
                    }
        		case Constant.BACKGROUND_GET_SHARED_USERS_LIST:	
	                try {
	                    Request request = new Request();
	
	                    String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
	                    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
	
	                    postData.add(new BasicNameValuePair("action", "list_shared_users"));
	                    postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
	
	                    String resultXML = request.httpPost(loginUrlString, postData);
	
	                    if (bIsLoadingStop) {
	                        LoadingDialog.dismiss();
	                        bIsLoadingStop = false;
	                        mBackKeyPressed = false;
	                        mRefresh = false;
	                        return Constant.BACKGROUND_PROCESS_OK;
	                    }
	
	                    if (resultXML.equals(Constant.ErrorMessage)) {
	                        LoadingDialog.dismiss();
	                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
	                        return Constant.BACKGROUND_PROCESS_ERROR;
	                    } else {
	                        mSharedUsersListDataParser = new SharedUsersListDataParser();
	
	                        mFileListValues = mSharedUsersListDataParser.parseResponse(resultXML);
	
	                        if (mBackKeyPressed) {
	                            mFolderDeep--;
	                            mBackKeyPressed = false;
	                        } else if (mRefresh)
	                            mRefresh = false;
	                        else
	                            mFolderDeep++;
	
	                        Collections.sort(mFileListValues, new Comparator<FileData>() {
	                            @Override
	                            public int compare(FileData object1, FileData object2) {
	                                if (object1.getIsFolder() && !object2.getIsFolder())
	                                    return -1;
	                                else if (!object1.getIsFolder() && object2.getIsFolder())
	                                    return 1;
	                                else
	                                    return object1.getName().toLowerCase().compareTo(object2.getName().toLowerCase());
	                            }
	                        });
	
	                        mDataBaseAdapter.insert(mFileListValues);
	
	                        LoadingDialog.dismiss();
	
	                    }
	
	                } catch (Exception e) {
	                    // TODO Auto-generated catch block
	                    LoadingDialog.dismiss();
	                    String exceptionString = e.getMessage();
	                    if (FilesActivity.offlineMode) {
	                        // Network unreachable
	                        SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
	                        offlineMode = true;
	                    } else {
	                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
	                        e.printStackTrace();
	                    }
	                    return Constant.BACKGROUND_PROCESS_ERROR;
	                }
	
	                runOnUiThread(new Runnable() {
	                    @Override
	                    public void run() {
	
	                        mFileListadapter = new FileListAdapter(FilesActivity.this, R.layout.fileitem, mFileListValues);
	                        mFileListView.setAdapter(mFileListadapter);
	                        mFileListadapter.notifyDataSetChanged();
	
	                        // if(mFolderDeep > 0)
	                        // btnBack.setVisibility(View.VISIBLE);
	                        // else
	                        // btnBack.setVisibility(View.GONE);
	
	                    }
	                });
	
	                getIconHandler.sendMessage(getIconHandler.obtainMessage());
	                
	                return Constant.BACKGROUND_PROCESS_OK;
        		case Constant.BACKGROUND_GET_SHARED_FOLDERS_LIST:
        			final String shareUserID = (String) params[1];
        			
                    try {
                        Request request = new Request();

                        String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                        postData.add(new BasicNameValuePair("action", "list_shared_directories"));
                        postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                        postData.add(new BasicNameValuePair("share_user_id", shareUserID));

                        String resultXML = request.httpPost(loginUrlString, postData);

                        if (bIsLoadingStop) {
                            LoadingDialog.dismiss();
                            bIsLoadingStop = false;
                            mBackKeyPressed = false;
                            mRefresh = false;
                            return Constant.BACKGROUND_PROCESS_OK;
                        }

                        if (resultXML.equals(Constant.ErrorMessage)) {
                            LoadingDialog.dismiss();
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            return Constant.BACKGROUND_PROCESS_ERROR;
                        } else {
                            mSharedDirectoryDataParser = new SharedDirectoryDataParser();

                            mFileListValues = mSharedDirectoryDataParser.parseResponse(resultXML, mShareUserID);

                            if (mBackKeyPressed) {
                                mFolderDeep--;
                                mBackKeyPressed = false;
                            } else if (mRefresh)
                                mRefresh = false;
                            else
                                mFolderDeep++;

                            Collections.sort(mFileListValues, new Comparator<FileData>() {
                                @Override
                                public int compare(FileData object1, FileData object2) {
                                    if (object1.getIsFolder() && !object2.getIsFolder())
                                        return -1;
                                    else if (!object1.getIsFolder() && object2.getIsFolder())
                                        return 1;
                                    else
                                        return object1.getName().toLowerCase().compareTo(object2.getName().toLowerCase());
                                }
                            });

                            mDataBaseAdapter.insert(mFileListValues);

                            LoadingDialog.dismiss();
                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        LoadingDialog.dismiss();

                        String exceptionString = e.getMessage();
                        if (FilesActivity.offlineMode) {
                            // Network unreachable
                            offlineMode = true;
                            SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
                        } else {
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            e.printStackTrace();
                        }
                        return Constant.BACKGROUND_PROCESS_ERROR;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mFileListadapter = new FileListAdapter(FilesActivity.this, R.layout.fileitem, mFileListValues);
                            mFileListView.setAdapter(mFileListadapter);
                            mFileListadapter.notifyDataSetChanged();

                            // if(mFolderDeep > 0)
                            // btnBack.setVisibility(View.VISIBLE);
                            // else
                            // btnBack.setVisibility(View.GONE);
                            //
                        }
                    });

                    getIconHandler.sendMessage(getIconHandler.obtainMessage());
                    return Constant.BACKGROUND_PROCESS_OK;	
        		case Constant.BACKGROUND_GET_SHARED_FILE_LIST:
        			final FileData parentFileItem = (FileData) params[1];
        			
                    try {
                        Request request = new Request();

                        String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                        postData.add(new BasicNameValuePair("action", "list_dir"));
                        postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                        postData.add(new BasicNameValuePair("share_user_id", parentFileItem.getShareUserID()));
                        postData.add(new BasicNameValuePair("share_id", parentFileItem.getID()));
                        postData.add(new BasicNameValuePair("access_dir_id", parentFileItem.getShareUserID()));
                        postData.add(new BasicNameValuePair("dir_id", mDirID));

                        String resultXML = request.httpPost(loginUrlString, postData);

                        if (bIsLoadingStop) {
                            LoadingDialog.dismiss();
                            bIsLoadingStop = false;
                            mBackKeyPressed = false;
                            mRefresh = false;
                            return Constant.BACKGROUND_PROCESS_OK;
                        }

                        if (resultXML.equals(Constant.ErrorMessage)) {
                            LoadingDialog.dismiss();
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            return Constant.BACKGROUND_PROCESS_ERROR;
                        } else {
                            mSharedFileDataParser = new SharedFileDataParser();

                            mFileListValues = mSharedFileDataParser.parseResponse(resultXML, parentFileItem);

                            if (mBackKeyPressed) {
                                mFolderDeep--;
                                mBackKeyPressed = false;
                            } else if (mRefresh)
                                mRefresh = false;
                            else
                                mFolderDeep++;

                            Collections.sort(mFileListValues, new Comparator<FileData>() {
                                @Override
                                public int compare(FileData object1, FileData object2) {
                                    if (object1.getIsFolder() && !object2.getIsFolder())
                                        return -1;
                                    else if (!object1.getIsFolder() && object2.getIsFolder())
                                        return 1;
                                    else
                                        return object1.getName().toLowerCase().compareTo(object2.getName().toLowerCase());
                                }
                            });

                            mDataBaseAdapter.insert(mFileListValues);

                            LoadingDialog.dismiss();

                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        LoadingDialog.dismiss();

                        String exceptionString = e.getMessage();
                        if (FilesActivity.offlineMode) {
                            // Network unreachable
                            offlineMode = true;
                            SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
                        } else {
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            e.printStackTrace();
                        }
                        return Constant.BACKGROUND_PROCESS_ERROR;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mFileListadapter = new FileListAdapter(FilesActivity.this, R.layout.fileitem, mFileListValues);
                            mFileListView.setAdapter(mFileListadapter);
                            mFileListadapter.notifyDataSetChanged();

                            // if(mFolderDeep > 0)
                            // btnBack.setVisibility(View.VISIBLE);
                            // else
                            // btnBack.setVisibility(View.GONE);

                        }
                    });

                    getIconHandler.sendMessage(getIconHandler.obtainMessage());
                    
                    return Constant.BACKGROUND_PROCESS_OK;
        		case Constant.BACKGROUND_DELETE_FILE:
        			final FileData item = (FileData) params[1];    
        			
                    try {
                        Request loginRequest = new Request();

                        String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                        postData.add(new BasicNameValuePair("action", "move_file_to_trash"));
                        postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                        postData.add(new BasicNameValuePair("share_user_id", ""));
                        postData.add(new BasicNameValuePair("share_id", ""));
                        postData.add(new BasicNameValuePair("access_dir_id", ""));
                        postData.add(new BasicNameValuePair("file_id", item.getID()));

                        String resultXML = loginRequest.httpPost(loginUrlString, postData);

                        if (bIsLoadingStop) {
                            bIsLoadingStop = false;
                            return Constant.BACKGROUND_PROCESS_OK;
                        }

                        if (resultXML.equals(Constant.ErrorMessage)) {
                            LoadingDialog.dismiss();
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            return Constant.BACKGROUND_PROCESS_ERROR;
                        } else {
                            mDeletFileDataParser = new DeletFileDataParser();
                            mDeleteFileData = mDeletFileDataParser.parseResponse(resultXML);

                            LoadingDialog.dismiss();

                            if (mDeleteFileData != null) {
                                if (mDeleteFileData.getName().length() > 0) {
                                    DeleteFileFailedMessageHandler.sendMessage(DeleteFileFailedMessageHandler.obtainMessage());
                                    return Constant.BACKGROUND_PROCESS_ERROR;
                                } else {

                                    MainActivity.mFileUploaded = true;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mFileListValues.remove(item);
                                            mFileListadapter.items.remove(item);
                                            mFileListadapter.notifyDataSetChanged();
                                        }
                                    });

                                    return Constant.BACKGROUND_PROCESS_OK;
                                }
                            } else {
                                ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                                return Constant.BACKGROUND_PROCESS_ERROR;
                            }
                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        LoadingDialog.dismiss();
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        e.printStackTrace();
                        return Constant.BACKGROUND_PROCESS_ERROR;
                    }
        		case Constant.BACKGROUND_GET_FILE_LIST_FROM_DB:
        			final String parentID = (String) params[1];
        			
                    ArrayList<FileData> fileDataList = new ArrayList<FileData>();

                    if (mDataBaseAdapter.allFileDataList == null)
                        mDataBaseAdapter.selectAll();

                    int fileCount = mDataBaseAdapter.allFileDataList.size();

                    for (int i = 0; i < fileCount; i++) {
                        FileData fileData1 = mDataBaseAdapter.allFileDataList.get(i);

                        if (fileData1.getParentID().equals(parentID)) {
                            fileDataList.add(fileData1);
                            Log.i("getData", fileData1.getID() + ": " + fileData1.getParentID());
                        }
                    }

                    mFileListValues = fileDataList;

                    if (mBackKeyPressed) {
                        mFolderDeep--;
                        mBackKeyPressed = false;
                    } else if (mRefresh)
                        mRefresh = false;
                    else
                        mFolderDeep++;

                    Collections.sort(mFileListValues, new Comparator<FileData>() {
                        @Override
                        public int compare(FileData object1, FileData object2) {
                            if (object1.getIsFolder() && !object2.getIsFolder())
                                return -1;
                            else if (!object1.getIsFolder() && object2.getIsFolder())
                                return 1;
                            else
                                return object1.getName().toLowerCase().compareTo(object2.getName().toLowerCase());
                        }
                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mFileListadapter = new FileListAdapter(FilesActivity.this, R.layout.fileitem, mFileListValues);
                            mFileListView.setAdapter(mFileListadapter);
                            mFileListadapter.notifyDataSetChanged();
                        }
                    });

                    getIconHandlerFromLocal.sendMessage(getIconHandlerFromLocal.obtainMessage());
                    
                    return Constant.BACKGROUND_PROCESS_OK;
        		case Constant.BACKGROUND_LOGIN:
                    try {

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
                            LoadingDialog.dismiss();
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            return Constant.BACKGROUND_PROCESS_ERROR;
                        } else {
                            LoginDataParser mLoginDataParser = null;
                            LoginData mLoginData = null;
                            mLoginDataParser = new LoginDataParser();
                            mLoginData = mLoginDataParser.parseResponse(resultXML);

                            LoadingDialog.dismiss();

                            if (mLoginData != null) {
                                if (mLoginData.getUserName().length() == 0) {
                                    LoginFailedMessageHandler.sendMessage(LoginFailedMessageHandler.obtainMessage());
                                    return Constant.BACKGROUND_PROCESS_ERROR;
                                } else {
                                    Constant.SessionID = mLoginData.getSessionID();
                                }
                            } else {
                                ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                                return Constant.BACKGROUND_PROCESS_ERROR;
                            }
                        }
                        
                        return Constant.BACKGROUND_PROCESS_OK;
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        LoadingDialog.dismiss();
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
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
