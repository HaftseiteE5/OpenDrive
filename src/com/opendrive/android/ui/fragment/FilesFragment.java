package com.opendrive.android.ui.fragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.opendrive.android.OpenDriveApplication;
import com.opendrive.android.R;
import com.opendrive.android.adapter.FileListAdapter;
import com.opendrive.android.adapter.FileListAdapter.IItemListener;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.MimeTypeParser;
import com.opendrive.android.common.MimeTypes;
import com.opendrive.android.common.Utils;
import com.opendrive.android.custom.ActionItem;
import com.opendrive.android.custom.QuickAction;
import com.opendrive.android.datamodel.CreateFileResultData;
import com.opendrive.android.datamodel.DeleteFileData;
import com.opendrive.android.datamodel.FileData;
import com.opendrive.android.datamodel.LoginData;
import com.opendrive.android.datamodel.MoveFileData;
import com.opendrive.android.datamodel.RenameFileData;
import com.opendrive.android.db.DataBaseAdapter;
import com.opendrive.android.parser.DeletFileDataParser;
import com.opendrive.android.parser.FileDataParser;
import com.opendrive.android.parser.LoginDataParser;
import com.opendrive.android.parser.MoveFileDataParser;
import com.opendrive.android.parser.RenameFileDataParser;
import com.opendrive.android.parser.SharedDirectoryDataParser;
import com.opendrive.android.parser.SharedFileDataParser;
import com.opendrive.android.parser.SharedFolderDataParser;
import com.opendrive.android.parser.SharedUsersListDataParser;
import com.opendrive.android.player.PlayerActivity;
import com.opendrive.android.request.Request;
import com.opendrive.android.service.AutoUploadService;
import com.opendrive.android.ui.FileItemClickListener;
import com.opendrive.android.ui.FilePropertyActivity;
import com.opendrive.android.ui.LogIn;
import com.opendrive.android.ui.MainActivity;
import com.opendrive.android.ui.MoveActivity;
import com.opendrive.android.ui.dialog.NewFolderDialog;
import com.opendrive.android.ui.dialog.NewFolderDialog.onCreateFolderListener;
import com.opendrive.android.utils.ArrayListAsyncLoader;
import com.opendrive.android.utils.FileUtils;
import com.opendrive.android.utils.SystemUtils;

@SuppressWarnings("deprecation")
public class FilesFragment extends Fragment implements IItemListener {

    private static String TAG = "FilesFragment";

    private Tracker mGaTracker;
    private GoogleAnalytics mGaInstance;

    private FileListAdapter mFileListAdapter;
    private ProgressDialog mLoadingDialog = null;
    private boolean mIsLoadingStop = false;
    ArrayList<FileData> mFileListValues = new ArrayList<FileData>();
    private AlertDialog.Builder mValidationAlertDialog = null;
    private AlertDialog.Builder mSwitchOfflineModeAlertDialog = null;
    private AlertDialog.Builder mNetworkErrorAlertDialog = null;
    private FileDataParser mFileDataParser = null;

    private int mFolderDeep = 0;
    private boolean mBackKeyPressed = false;
    private boolean mRefresh = false;
    private FileData mCurrentFolderData = null;

    private MimeTypes mMimeTypes;

    private boolean mIsFragmentVisible = false;
    public boolean isFragmentVisible() {
        return mIsFragmentVisible;
    }

    private SharedFolderDataParser mSharedFolderDataParser = null;
    private boolean mHasSharedFolders = false;
    private FileData mSharedFolders = null;
    private boolean mIsExploreSharedFolder = false;

    private SharedUsersListDataParser mSharedUsersListDataParser = null;
    private String mShareUserID = "";

    private SharedDirectoryDataParser mSharedDirectoryDataParser = null;
    private String mSharedDirectoryID = "";

    private SharedFileDataParser mSharedFileDataParser = null;

    private TextView mTVAutoUploadStatus;

    private DeletFileDataParser mDeleteFileDataParser = null;
    private DeleteFileData mDeleteFileData = null;

    private RenameFileDataParser mRenameFileDataParser = null;
    private RenameFileData mRenameFileData = null;

    private MoveFileDataParser mMoveFileDataParser = null;
    private MoveFileData mMoveFileData = null;

    private DataBaseAdapter mDataBaseAdapter = null;
    boolean mOpenOnlyFromDB = false;
    public static boolean sOfflineMode = false;
    private static boolean sIsOfflineModeSwitched = false;
    private boolean mIsFirstFlag = false;

    private ProgressBar mProgressBar;
    private ListView mFileListView;

    private int mMode;
    public static int MODE_VIEW = 0;
    public static int MODE_VIEW_SEARCH_RESULTS = 1;
    public static int MODE_MOVE_FILE = 2;
    public static int MODE_SELECT_AUTO_UPLOAD_FOLDER = 3;

    private FileData mItemForMove = null;

    private ArrayList<FileData> mCurrentShowingFiles;

    private Vector<FileData> mLogData = new Vector<FileData>();

    private RelativeLayout mNavigatorBar;
    private LinearLayout mSubMenuDetails;
    private ImageView mPanelBgImage;
    private FrameLayout mPanelDetails;

    private TextView mFileNameDetail;
    private TextView mFileSizeDetail;
    private ImageView mImageThumbDetail;

    LinearLayout mPanel;

    private ImageButton mBtnShare;
    private ImageButton mBtnCopyLink;
    private ImageButton mBtnTrash;
    private Button mBtnRename;
    private Button mBtnMoveToFolder;
    private Button mBtnSendEmail;
    private Button mBtnSaveForOffline;

    private ImageButton mNavArrowLeft;
    private ImageButton mNavArrowRight;

    private RelativeLayout rlActionBarItems;
    private RelativeLayout rlSearchView;
    private ImageButton mBtnHome;
    private ImageView mImageRootLogo;
    private ImageButton mBtnSearch;
    private ImageButton mBtnNew;
    private ImageButton mBtnRefresh;
    private ImageButton mBtnGoSearch;
    private EditText mEditSearch;
    private View borderActiveSearch;
    private View borderActiveNew;
    private View borderActiveRefresh;

    private TextView mTextActionBarTitle;

    private View mListFooter;

    private boolean isSearchResultShown = false;

    private float mSlidingXDiff;
    private Animation mAnimationDetailsRightFirst;
    private Animation mAnimationDetailsRightSecond;
    private Animation mAnimationDetailsLeftFirst;
    private Animation mAnimationDetailsLeftSecond;
    private Animation mAnimationListViewTopFirst;
    private Animation mAnimationListViewTopSecond;

    private LinearLayout llDetailsSlide;

    private static final int DIRECTION_MOVE_TOP = 0;
    private static final int DIRECTION_MOVE_BOTTOM = 1;

    private UploadStartedReceiver mUploadStartedReceiver = new UploadStartedReceiver();
    private UploadProgressReceiver mUploadProgressReceiver = new UploadProgressReceiver();
    private UploadFinishedReceiver mUploadFinishedReceiver = new UploadFinishedReceiver();
    private boolean mIsAutoUploading = false;
    private int mFilesCount = 0;
    private int mFilesUploaded = 0;
    private String mAutoUploadFolderId;

    @Override
    public View getView() {
        return super.getView();
    }

    public static final String ACTION_EDITED_FILE = "action_edited_file";

    private BroadcastReceiver editFileReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ACTION_EDITED_FILE)) {
                SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                String uploadedPath = intent.getStringExtra("file");
                if(pref.contains(uploadedPath)){
                        editor.remove(uploadedPath);
                        editor.commit();
                }
            }


        }
    };

    private RefreshReceiver mRefreshReceiver = new RefreshReceiver();

    private LoaderManager.LoaderCallbacks<ArrayList<FileData>> loaderFromCloud = new LoaderManager.LoaderCallbacks<ArrayList<FileData>>() {
        @Override
        public Loader<ArrayList<FileData>> onCreateLoader(int id, Bundle data) {
            return new ArrayListAsyncLoader<FileData>(FilesFragment.this.getActivity()) {
        /*
         * @Override protected void onStartLoading() { forceLoad(); }
		 */

                @Override
                public ArrayList<FileData> loadInBackground() {
                    try {
                        final FileData fileItem = mCurrentFolderData;

                        ArrayList<FileData> fileListValues = new ArrayList<FileData>();
                        ArrayList<FileData> endfileListValues = new ArrayList<FileData>();
                        Request loginRequest = new Request();

                        String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                        postData.add(new BasicNameValuePair("action", "list_dir"));
                        postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                        postData.add(new BasicNameValuePair("share_user_id", ""));
                        postData.add(new BasicNameValuePair("share_id", ""));
                        if (fileItem == null) {
                            postData.add(new BasicNameValuePair("access_dir_id", "0"));
                            postData.add(new BasicNameValuePair("dir_id", "0"));
                        } else {
                        	if (Constant.accessUser)
                        		postData.add(new BasicNameValuePair("access_dir_id", fileItem.getAccessDirID()));
                            postData.add(new BasicNameValuePair("dir_id", fileItem.getID()));
                        }
                        String resultXML = loginRequest.httpPost(loginUrlString, postData);

                        Utils.longLog(TAG, "result xml = " + resultXML);

                        if (resultXML.equals(Constant.ErrorMessage)) {
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        } else {
                            mFileDataParser = new FileDataParser();

                            if (fileItem == null)
                                fileListValues = mFileDataParser.parseResponse(resultXML, "0", null);
                            else
                            {
                            	
                            	if (fileItem.getAccessDirID().compareTo("0") == 0)
                            		fileListValues = mFileDataParser.parseResponse(resultXML, fileItem.getID(), fileItem);
                            	else
                            		fileListValues = mFileDataParser.parseResponse(resultXML, fileItem.getAccessDirID(), fileItem);
                            		
                            }

                            Collections.sort(fileListValues, new Comparator<FileData>() {
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

                            if (mMode == MODE_MOVE_FILE || mMode == MODE_SELECT_AUTO_UPLOAD_FOLDER) {

                                for (FileData fileData : fileListValues) {
                                    if (fileData.getIsFolder()) {
                                        endfileListValues.add(fileData);
                                    }
                                }
                                try {
                                    mDataBaseAdapter.insert(fileListValues);
                                } catch (Exception e) {
                                }
                                return endfileListValues;
                            }
                            try {
                                mDataBaseAdapter.insert(fileListValues);
                            } catch (Exception e) {
                            }
                            // LoadingDialog.dismiss();
                        }

                        return fileListValues;

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        mLoadingDialog.dismiss();
                        if (sOfflineMode) {
                            sOfflineMode = true;
                            if (!sIsOfflineModeSwitched) {
                                SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
                            } else {
                                showFileListInOfflineMode();
                            }
                        } else {
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            e.printStackTrace();
                        }

                        return null;
                    }
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<FileData>> arrayListLoader, ArrayList<FileData> fileDatas) {
            if (fileDatas != null) {
                if (mCurrentFolderData == null || mFolderDeep == 0) {
                    if ((mMode == MODE_MOVE_FILE || mMode == MODE_SELECT_AUTO_UPLOAD_FOLDER)) {
                        if (LogIn.mLoginData != null && LogIn.mLoginData.getIsAccessUser().equals("False")) {
                            FileData rootFileData = new FileData();
                            rootFileData.setIsFolder(true);
                            rootFileData.setName(getString(R.string.app_name));
                            rootFileData.setID("0");
                            fileDatas.add(0, rootFileData);
                        }
                    }
                }

                if (mFileListAdapter == null) {
                    mFileListAdapter = new FileListAdapter(getActivity(), R.layout.fileitem, fileDatas, FilesFragment.this, FilesFragment.this);
                    mFileListView.setAdapter(mFileListAdapter);
                } else {
                    mFileListAdapter.items.clear();
                    mFileListAdapter.items = fileDatas;
                    mFileListAdapter.notifyDataSetChanged();
                }


                SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
                for(FileData fileData : fileDatas) {
                    if(pref.contains(Utils.getFolderFullPath(fileData))) {
                        reSaveFile(fileData);
                    }
                }


            }
            mFileListView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);

            if (isSearchResultShown) {
                mOnGoSearchClickListener.onClick(null);
            }

            if (mCurrentFolderData == null || mFolderDeep == 0) {
                mBtnHome.setVisibility(View.GONE);
                if (mMode != MODE_MOVE_FILE && mMode != MODE_VIEW_SEARCH_RESULTS) {
                    mImageRootLogo.setVisibility(View.VISIBLE);
                }
                if (mMode == MODE_MOVE_FILE) {
                    mImageRootLogo.setVisibility(View.VISIBLE);
                }

            } else {
                mBtnHome.setVisibility(View.VISIBLE);

                mImageRootLogo.setVisibility(View.GONE);
            }
            mLoadingDialog.dismiss();


            if (!sOfflineMode && sIsOfflineModeSwitched) {
                sIsOfflineModeSwitched = false;
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.network_restored), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<FileData>> arrayListLoader) {
            // To change body of implemented methods use File | Settings | File
            // Templates.
        }

    };

    private void showFileListInOfflineMode() {
        if (mCurrentFolderData == null) {
            getFileListFromDB("0");
        } else {
            getFileListFromDB(mCurrentFolderData.getID());
        }
    }

    public void updateFileList(ArrayList<FileData> fileData, int mode) {
        mMode = mode;
        if (mode == MODE_VIEW_SEARCH_RESULTS) {
            if (mCurrentShowingFiles != null) {
                mCurrentShowingFiles.clear();
            }
            if (mFileListAdapter.items != null) {
                mCurrentShowingFiles = new ArrayList<FileData>(mFileListAdapter.items);
            }
            mFileListAdapter.items.clear();
            mFileListAdapter.items.addAll(fileData);
            mFileListAdapter.notifyDataSetChanged();
        } else if (mode == MODE_VIEW) {
            mFileListAdapter.items.clear();
            mFileListAdapter.items.addAll(fileData);
            mFileListAdapter.notifyDataSetChanged();
        }
        mFileListView.setSelection(0);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    /*
     * filesButton.setOnClickListener(bottomBarButtonClickListener); uploadButton.setOnClickListener(bottomBarButtonClickListener); requestButton.setOnClickListener(bottomBarButtonClickListener); settingsButton.setOnClickListener(bottomBarButtonClickListener);
	 */

        // filesButton.setPressed(true);

        mFileListView.setOnItemClickListener(new DataItemClickListener());
        if (android.os.Build.VERSION.SDK_INT >= 9) {
            mFileListView.setOverscrollFooter(null);
        }

    }

    /**
     * Reload date from current folder
     */
    public void reloadData() {
        mFileListView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        getLoaderManager().restartLoader(0, null, loaderFromCloud);
        mMode = MODE_VIEW;
        isSearchResultShown = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.files, container, false);


        initValidationAlertDialog();
        initLoadingDialog();
        initNetworkErrorAlertDialog();

        setHasOptionsMenu(true);
        getMimeTypes();

        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        mFileListView = (ListView) v.findViewById(R.id.listView_files);
        mFileListView.setScrollbarFadingEnabled(true);
        mListFooter = new View(getActivity());
        mFileListView.addFooterView(mListFooter);

        mNavigatorBar = (RelativeLayout) v.findViewById(R.id.navigation_bar);
        mSubMenuDetails = (LinearLayout) v.findViewById(R.id.sub_menu_detail);
        mPanelBgImage = (ImageView) v.findViewById(R.id.panel_image_bg);
        mPanel = (LinearLayout) v.findViewById(R.id.panel);
        mPanelDetails = (FrameLayout) v.findViewById(R.id.panel_file_detail);

        mPanelDetails.setOnTouchListener(panelDetailTouchListener);

        mFileNameDetail = (TextView) v.findViewById(R.id.file_name_detail);
        mFileSizeDetail = (TextView) v.findViewById(R.id.file_size_detail);
        mImageThumbDetail = (ImageView) v.findViewById(R.id.imageThumbDetail);

        mBtnShare = (ImageButton) v.findViewById(R.id.btnShare);
        mBtnCopyLink = (ImageButton) v.findViewById(R.id.btnCopyLink);
        mBtnTrash = (ImageButton) v.findViewById(R.id.btnTrash);

        mBtnRename = (Button) v.findViewById(R.id.btnRename);
        mBtnMoveToFolder = (Button) v.findViewById(R.id.btnMoveToFolder);
        mBtnSendEmail = (Button) v.findViewById(R.id.btnSendEmail);
        mBtnSaveForOffline = (Button) v.findViewById(R.id.btnSaveForOffline);
        mNavArrowLeft = (ImageButton) v.findViewById(R.id.nav_arrow_left);
        mNavArrowRight = (ImageButton) v.findViewById(R.id.nav_arrow_right);
        llDetailsSlide = (LinearLayout) v.findViewById(R.id.llDetailsSlide);

        mTVAutoUploadStatus = (TextView) v.findViewById(R.id.tvAutoUploadStatus);

        mBtnShare.setOnClickListener(menuDetailListener);
        mBtnCopyLink.setOnClickListener(menuDetailListener);
        mBtnTrash.setOnClickListener(menuDetailListener);
        mBtnRename.setOnClickListener(menuDetailListener);
        mBtnMoveToFolder.setOnClickListener(menuDetailListener);
        mBtnSendEmail.setOnClickListener(menuDetailListener);
        mBtnSaveForOffline.setOnClickListener(menuDetailListener);

        rlActionBarItems = (RelativeLayout) v.findViewById(R.id.rlActionBarItems);
        rlSearchView = (RelativeLayout) v.findViewById(R.id.rlSearchView);
        mBtnHome = (ImageButton) v.findViewById(R.id.ibHome);
        mImageRootLogo = (ImageView) v.findViewById(R.id.ivRootLogo);
        mBtnSearch = (ImageButton) v.findViewById(R.id.ibSearch);
        mBtnNew = (ImageButton) v.findViewById(R.id.ibNew);
        mBtnRefresh = (ImageButton) v.findViewById(R.id.ibRefresh);
        mBtnGoSearch = (ImageButton) v.findViewById(R.id.ibGoSearch);
        mEditSearch = (EditText) v.findViewById(R.id.etSearchView);
        mEditSearch.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mOnGoSearchClickListener.onClick(null);
                }
                return false;
            }
        });

        borderActiveSearch = (View) v.findViewById(R.id.borderActiveSearch);
        borderActiveNew = (View) v.findViewById(R.id.borderActiveNew);
        borderActiveRefresh = (View) v.findViewById(R.id.borderActiveRefresh);

        mBtnHome.setOnClickListener(actionBarItemClickListener);
        mBtnSearch.setOnClickListener(actionBarItemClickListener);
        mBtnNew.setOnClickListener(actionBarItemClickListener);
        mBtnRefresh.setOnClickListener(actionBarItemClickListener);

        mBtnSearch.setOnTouchListener(actionBarItemTouchListener);
        mBtnNew.setOnTouchListener(actionBarItemTouchListener);
        mBtnRefresh.setOnTouchListener(actionBarItemTouchListener);

        mTextActionBarTitle = (TextView) v.findViewById(R.id.tvActionBarTitle);

        mBtnGoSearch.setOnClickListener(mOnGoSearchClickListener);

        mNavigatorBar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mSubMenuDetails.getVisibility() == View.GONE) {
                    mSubMenuDetails.setVisibility(View.VISIBLE);

                } else {

                    mSubMenuDetails.setVisibility(View.GONE);
                    mPanel.setVisibility(View.GONE);
                    mPanelDetails.setVisibility(View.GONE);
                    mPanelBgImage.setVisibility(View.GONE);
                    mListFooter.setLayoutParams(new android.widget.AbsListView.LayoutParams(0, 0));

                    mListFooter.invalidate();
                    mPanelDetails.invalidate();
                    mPanelBgImage.invalidate();
                    mPanel.invalidate();
                    mFileListView.invalidate();
                    mFileListAdapter.notifyDataSetInvalidated();
                }

            }
        });

        mNavArrowLeft.setOnClickListener(navArrowLeftClickListener);
        mNavArrowRight.setOnClickListener(navArrowRightClickListener);

        if (mMode == MODE_MOVE_FILE) {
            // llConfirmCancelMove.setVisibility(View.VISIBLE);
        }

        mAnimationDetailsLeftFirst = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_details_left_first);
        mAnimationDetailsLeftSecond = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_details_left_second);
        mAnimationDetailsRightFirst = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_details_right_first);
        mAnimationDetailsRightSecond = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_details_right_second);
        mAnimationListViewTopFirst = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_listview_top_first);
        mAnimationListViewTopSecond = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_listview_top_second);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        mSlidingXDiff = display.getWidth() / 100 * 10;

        Utils.overrideFonts(getActivity(), v);
        Typeface typefaceRobotoLight = Utils.getTypeface(getActivity(), "fonts/roboto_light.ttf");
        mFileNameDetail.setTypeface(typefaceRobotoLight);
        Typeface typefaceRobotoBold = Utils.getTypeface(getActivity(), "fonts/roboto_bold.ttf");
        mTVAutoUploadStatus.setTypeface(typefaceRobotoBold);

        if (mMode == MODE_MOVE_FILE || mMode == MODE_SELECT_AUTO_UPLOAD_FOLDER) {
            v.findViewById(R.id.rlActionBarItems).setVisibility(View.GONE);
            mBtnHome.setVisibility(View.GONE);
            mImageRootLogo.setVisibility(View.VISIBLE);
        }

        if (savedInstanceState != null) {
            mCurrentFolderData = savedInstanceState.getParcelable("current_folder");
            mIsAutoUploading = savedInstanceState.getBoolean("isautouploading");
            if (mIsAutoUploading) {
                mFilesCount = savedInstanceState.getInt("filescount");
                mFilesUploaded = savedInstanceState.getInt("filesuploaded");
                showAutoUploadStatus(mFilesUploaded, mFilesCount);
            }
        }


        return v;
    }

    private View.OnClickListener navArrowLeftClickListener = new OnClickListener() {

        @TargetApi(Build.VERSION_CODES.FROYO)
        @Override
        public void onClick(View v) {

            if (mFileListAdapter.getCount() == 1) {
                return;
            }

            mCurrentPosition--;

            if (mCurrentPosition < 0 || ((FileData) mFileListAdapter.getItem(mCurrentPosition)).getIsFolder()) {
                mCurrentPosition = mFileListAdapter.getCount() - 1;

                if (mFileListView.getChildAt(0) != null) {
                    int itemsToEndCount = mFileListAdapter.getCount() - mCurrentPosition;
                    int itemsToEndHeight = itemsToEndCount * mFileListView.getChildAt(0).getMeasuredHeight();
                    int listFooterHeight = mFileListView.getMeasuredHeight() - itemsToEndHeight;
                    if (listFooterHeight > 0) {
                        mListFooter.setLayoutParams(new android.widget.AbsListView.LayoutParams(0, listFooterHeight));
                        mListFooter.invalidate();
                    }

                    mFileListView.requestFocusFromTouch();
                    if (mIsFroyoOrLater) {
                        mFileListView.smoothScrollBy(mFileListView.getChildAt(0).getMeasuredHeight(), 1000);
                        mFileListView.setSelectionFromTop(mCurrentPosition - 1, 0);
                    } else {
                        mFileListView.setSelectionFromTop(mCurrentPosition, 0);
                    }
                    mPanelDetails.requestFocusFromTouch();
                }

            } else {

                if (mFileListView.getChildAt(0) != null) {
                    int currentListFooterHeight = mListFooter.getMeasuredHeight();
                    int itemHeight = mFileListView.getChildAt(0).getMeasuredHeight();
                    int newListFooterHeight = 0;
                    if (currentListFooterHeight > 0) {
                        if (currentListFooterHeight - itemHeight > 0) {
                            newListFooterHeight = currentListFooterHeight - itemHeight;
                        } else {
                            newListFooterHeight = 0;
                        }
                        mListFooter.setLayoutParams(new android.widget.AbsListView.LayoutParams(0, newListFooterHeight + mFileListView.getChildAt(0).getMeasuredHeight()));
                        mListFooter.invalidate();
                    }
                    mFileListView.requestFocusFromTouch();
                    if (mCurrentPosition > 0) {
                        if (mIsFroyoOrLater) {
                            mFileListView.smoothScrollBy(-mFileListView.getChildAt(0).getMeasuredHeight(), 1000);
                            mFileListView.setSelectionFromTop(mCurrentPosition + 1, 0);
                        } else {
                            mFileListView.setSelectionFromTop(mCurrentPosition, 0);
                        }
                    } else {
                        if (mIsFroyoOrLater) {
                            mFileListView.smoothScrollBy(-mFileListView.getChildAt(0).getMeasuredHeight(), 1000);
                        }
                        mFileListView.setSelectionFromTop(mCurrentPosition, 0);
                    }
                    mPanelDetails.requestFocusFromTouch();
                }
            }

            mCurrentFileData = (FileData) mFileListAdapter.getItem(mCurrentPosition);
            llDetailsSlide.startAnimation(mAnimationDetailsRightFirst);
            mAnimationDetailsRightFirst.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    initDetailView(mCurrentPosition, mCurrentFileData);
                    llDetailsSlide.startAnimation(mAnimationDetailsRightSecond);
                }
            });
        }
    };

    private View.OnClickListener navArrowRightClickListener = new OnClickListener() {

        @TargetApi(Build.VERSION_CODES.FROYO)
        @Override
        public void onClick(View v) {

            boolean isFirstNonFolderPosition = false;

            if (mFileListAdapter.getCount() == 1) {
                return;
            }

            mCurrentPosition++;

            if (mCurrentPosition >= mFileListAdapter.getCount()) {
                mCurrentPosition = getFirstNonFolderPosition(mFileListAdapter.items);
                isFirstNonFolderPosition = true;
            }
            if (mFileListView.getChildAt(0) != null) {
                int itemsToEndCount = mFileListAdapter.getCount() - mCurrentPosition;
                int itemsToEndHeight = itemsToEndCount * mFileListView.getChildAt(0).getMeasuredHeight();
                int listFooterHeight = mFileListView.getMeasuredHeight() - itemsToEndHeight;
                if (listFooterHeight > 0) {
                    mListFooter.setLayoutParams(new android.widget.AbsListView.LayoutParams(0, listFooterHeight + mFileListView.getChildAt(0).getMeasuredHeight()));
                    mListFooter.invalidate();
                } else {
                }
                mFileListView.requestFocusFromTouch();
                if (mCurrentPosition > 0) {
                    if (mIsFroyoOrLater) {
                        if (isFirstNonFolderPosition) {
                            mFileListView.setSelectionFromTop(mCurrentPosition + 1, 0);
                            mFileListView.smoothScrollBy(-mFileListView.getChildAt(0).getMeasuredHeight(), 1000);
                        } else {
                            mFileListView.smoothScrollBy(mFileListView.getChildAt(0).getMeasuredHeight(), 1000);
                            mFileListView.setSelectionFromTop(mCurrentPosition - 1, 0);
                        }
                    } else {

                        mFileListView.setSelectionFromTop(mCurrentPosition, 0);
                        // mAnimationListViewTopFirst.setAnimationListener(new AnimationListener() {
                        //
                        // @Override
                        // public void onAnimationStart(Animation animation) {
                        // }
                        //
                        // @Override
                        // public void onAnimationRepeat(Animation animation) {
                        // }
                        //
                        // @Override
                        // public void onAnimationEnd(Animation animation) {
                        // mFileListView.setSelectionFromTop(mCurrentPosition, 0);
                        // mFileListView.startAnimation(mAnimationListViewTopSecond);
                        // }
                        // });
                        // mFileListView.startAnimation(mAnimationListViewTopFirst);
                        // final Handler handler = new Handler();
                        // Runnable moveListView = new Runnable() {
                        // private int moveCount = 0;
                        // @Override
                        // public void run() {
                        // if(moveCount == 6) {
                        // return;
                        // }
                        // //mFileListView.setPadding(mFileListView.getPaddingLeft(), mFileListView.getPaddingTop() - 8, mFileListView.getPaddingRight(), mFileListView.getPaddingBottom());
                        // mFileListView.scrollTo(mFileListView.getScrollX(), mFileListView.getScrollY() + 8);
                        // //mFileListView.scrollBy(mFileListView.getScrollX(), 8);
                        // handler.postDelayed(this, 100);
                        // moveCount++;
                        // }
                        // };
                        // handler.post(moveListView);
                    }
                } else {
                    if (mIsFroyoOrLater) {
                        mFileListView.smoothScrollBy(-mFileListView.getChildAt(0).getMeasuredHeight(), 1000);
                    }
                    mFileListView.setSelectionFromTop(mCurrentPosition, 0);
                }

                mPanelDetails.requestFocusFromTouch();

            }

            mCurrentFileData = (FileData) mFileListAdapter.getItem(mCurrentPosition);
            llDetailsSlide.startAnimation(mAnimationDetailsLeftFirst);
            mAnimationDetailsLeftFirst.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    initDetailView(mCurrentPosition, mCurrentFileData);
                    llDetailsSlide.startAnimation(mAnimationDetailsLeftSecond);
                }
            });
        }
    };

    private OnClickListener mOnGoSearchClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            Utils.hideSoftInput(getActivity(), mEditSearch);
            if (mMode == MODE_MOVE_FILE) {
                onCancelMove();
            }
            String query = mEditSearch.getText().toString();
            if (query.length() != 0) {
                searchForFile(query);
                isSearchResultShown = true;
            }
            rlSearchView.setVisibility(View.GONE);
            rlActionBarItems.setVisibility(View.VISIBLE);
            mTextActionBarTitle.setVisibility(View.VISIBLE);

            if (mCurrentFolderData == null || mFolderDeep == 0) {
                mImageRootLogo.setVisibility(View.VISIBLE);
            }

        }
    };

    private boolean mIsFroyoOrLater;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("open_from_db", true);
        outState.putInt("folder_deep", mFolderDeep);
        outState.putInt("mode", mMode);
        outState.putBoolean("isautouploading", mIsAutoUploading);
        outState.putInt("filescount", mFilesCount);
        outState.putInt("filesuploaded", mFilesUploaded);
        if (mMode == MODE_VIEW_SEARCH_RESULTS) {
            outState.putString("search_query", mEditSearch.getText().toString());
            outState.putBoolean("is_search_result_shown", isSearchResultShown);
        }
        if (mMode == MODE_MOVE_FILE) {
            outState.putParcelable("item_for_move", mItemForMove);
        }
        outState.putParcelable("current_folder", mCurrentFolderData);
    }

    private int getFirstNonFolderPosition(ArrayList<FileData> items) {
        for (FileData item : items) {
            if (!item.getIsFolder()) {
                return items.indexOf(item);
            }
        }
        return items.size() - 1;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mOpenOnlyFromDB = savedInstanceState.getBoolean("open_from_db");
            mFolderDeep = savedInstanceState.getInt("folder_deep");
            mMode = savedInstanceState.getInt("mode");
            if (mMode == MODE_VIEW_SEARCH_RESULTS) {
                mEditSearch.setText(savedInstanceState.getString("search_query"));
                isSearchResultShown = savedInstanceState.getBoolean("is_search_result_shown");
                if (!isSearchResultShown) {
                    openSearchView();
                }
            }
            if (mMode == MODE_MOVE_FILE) {
                mItemForMove = savedInstanceState.getParcelable("item_for_move");
            }
            mCurrentFolderData = savedInstanceState.getParcelable("current_folder");
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        mIsFragmentVisible = false;
        
    }

    @Override
    public void onStart() {
        super.onStart();
        mGaTracker.sendView(Constant.GA_FILES_VIEW);
        getActivity().registerReceiver(mUploadStartedReceiver, new IntentFilter(AutoUploadService.ACTION_UPLOAD_STARTED));
        getActivity().registerReceiver(mUploadProgressReceiver, new IntentFilter(AutoUploadService.ACTION_UPLOAD_PROGRESS));
        getActivity().registerReceiver(mUploadFinishedReceiver, new IntentFilter(AutoUploadService.ACTION_UPLOAD_FINISHED));
        getActivity().registerReceiver(mRefreshReceiver, new IntentFilter(Constant.ACTION_REFRESH));
        getActivity().registerReceiver(editFileReceiver, new IntentFilter(ACTION_EDITED_FILE));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            mGaTracker.sendEvent(Constant.GA_EVENT_CAT_ROTATING, Constant.GA_EVENT_ACTION_ROTATE_LANDSCAPE_FILES,"",0L);
        }else{
            mGaTracker.sendEvent(Constant.GA_EVENT_CAT_ROTATING, Constant.GA_EVENT_ACTION_ROTATE_PORTRAIT_FILES,"",0L);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MainActivity.REQUEST_CREATE_FOLDER) {

            if (resultCode == Activity.RESULT_OK) {
                reloadData();
            }

        } else if(requestCode == REQUEST_DOC) {
            SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
            long lastUpdated = preferences.getLong(editedFile.getAbsolutePath(),0);
            Log.i("test","onActivityTextResult " + editedFile.lastModified() + " before = " + lastUpdated);
            if(editedFile.lastModified() == lastUpdated) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove(editedFile.getAbsolutePath());
                editor.commit();
            } else {
                mLoadingDialog.setMessage("Updating file");
                mLoadingDialog.show();
            }
        }
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sOfflineMode = !Utils.isNetConeccted(getActivity());
        getLoaderManager().initLoader(0, null, loaderFromCloud);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    protected void onConfirmMove() {
        mMode = MODE_VIEW;
        isSearchResultShown = false;
        mTextActionBarTitle.setText("");
        final boolean removeListItemAfterMove;
        final String moveToFullPath;
        final String moveToID;
        boolean tempRemoveListItemAfterMove = false;
        String tempID = "";
        String tempMoveToFullPath = "";
        boolean isChekedForMove = false;
        for (final FileData fileData : mFileListAdapter.items) {
            if (fileData.isCheckedForMove()) {
                fileData.setCheckedForMove(false);
                tempID = fileData.getID();
                isChekedForMove = true;
                if (tempID.equals("0")) {
                    tempMoveToFullPath = Constant.FOLDER_PATH;
                    if (mCurrentFolderData != null) {
                        tempRemoveListItemAfterMove = true;
                    } else {
                        tempRemoveListItemAfterMove = false;
                    }
                } else {
                    tempMoveToFullPath = Utils.getFolderFullPath(fileData);
                    tempRemoveListItemAfterMove = true;
                }
                break;
            }
        }

        if (!isChekedForMove) {
            tempRemoveListItemAfterMove = false;
            if (mCurrentFolderData != null) {
                if (mItemForMove.getParentID().equals(mCurrentFolderData.getID())) {
                    onCancelMove();
                    return;
                }
                tempID = mCurrentFolderData.getID();
                tempMoveToFullPath = Utils.getFolderFullPath(mCurrentFolderData);
            } else {
                if (mItemForMove.getParentID().equals("0")) {
                    onCancelMove();
                    return;
                }
                tempID = "0";
                tempMoveToFullPath = Constant.FOLDER_PATH;
            }
        }

        removeListItemAfterMove = tempRemoveListItemAfterMove;
        moveToID = tempID;
        moveToFullPath = tempMoveToFullPath;

        final String oldFileFullPath = Utils.getFolderFullPath(mItemForMove);

        mLoadingDialog.setMessage(getString(R.string.txt_moving));
        mLoadingDialog.show();

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10);

                    Request moveFileRequest = new Request();

                    String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                    postData.add(new BasicNameValuePair("action", "copy_file"));
                    postData.add(new BasicNameValuePair("dst_access_dir_id", moveToID));
                    postData.add(new BasicNameValuePair("dst_dir_id", moveToID));
                    postData.add(new BasicNameValuePair("move", "1"));
                    postData.add(new BasicNameValuePair("overwrite_if_exists", "1"));
                    postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                    postData.add(new BasicNameValuePair("src_access_dir_id", mItemForMove.getParentID()));
                    postData.add(new BasicNameValuePair("src_file_id", mItemForMove.getID()));

                    String resultXML = moveFileRequest.httpPost(loginUrlString, postData);
                    if (mIsLoadingStop) {
                        mIsLoadingStop = false;
                        getActivity().finish();
                        return;
                    }

                    if (resultXML.equals(Constant.ErrorMessage)) {
                        mLoadingDialog.dismiss();
                        mLoadingDialog.setMessage(getActivity().getString(R.string.txt_loading));
                        getActivity().finish();
                        return;
                    } else {
                        mMoveFileDataParser = new MoveFileDataParser();
                        mMoveFileData = mMoveFileDataParser.parseResponse(resultXML);

                        mLoadingDialog.dismiss();

                        if (mMoveFileData != null) {
                            if (TextUtils.isEmpty(mMoveFileData.getName()) || !TextUtils.isEmpty(mMoveFileData.getErrorMessage())) {
                                moveFileFailedMessageHandler.sendMessage(moveFileFailedMessageHandler.obtainMessage());
                                getActivity().finish();
                                return;
                            } else {

                                mItemForMove.setAccess(mMoveFileData.getAccess());
                                mItemForMove.setDescription(mMoveFileData.getDescription());
                                mItemForMove.setLink(mMoveFileData.getLink());
                                mItemForMove.setDateModified(mMoveFileData.getDateModified());
                                mItemForMove.setDirectLink(mMoveFileData.getDirectLink());
                                mItemForMove.setStreamLink(mMoveFileData.getStreamLink());
                                mItemForMove.setDirectLinkPublic(mMoveFileData.getDirectLinkPublic());
                                mDataBaseAdapter.updateWithNewParent(mItemForMove, moveToID);

                                File from = new File(oldFileFullPath);
                                File to = new File(moveToFullPath + mItemForMove.getName());
                                if (from.exists()) {
                                    from.renameTo(to);
                                }

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (removeListItemAfterMove) {
                                            mFileListValues.remove(mItemForMove);
                                            mFileListAdapter.items.remove(mItemForMove);

                                        } else {
                                            mItemForMove.setParent(mCurrentFolderData);
                                            mItemForMove.setParentID(moveToID);
                                            mItemForMove.setDownloaded(true);
                                            mFileListAdapter.items.add(mItemForMove);

                                        }
                                        mFileListAdapter.notifyDataSetChanged();
                                    }
                                });

                                MainActivity.mFileUploaded = true;
                                getActivity().finish();
                                return;
                            }
                        } else {
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            getActivity().finish();
                            return;
                        }

                    }

                } catch (Exception e) {
                    mLoadingDialog.dismiss();
                    ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                    e.printStackTrace();
                    getActivity().finish();
                    return;
                }
		/*
		 * runOnUiThread(new Runnable() {
		 * 
		 * @Override public void run() {
		 * 
		 * } });
		 */
            }

        }).start();

    }

    protected void onCancelMove() {
        mMode = MODE_VIEW;
        setItemForMove(null);
        for (FileData item : mFileListAdapter.items) {
            item.setCheckedForMove(false);
        }
        mFileListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // To change body of overridden

        mGaInstance = GoogleAnalytics.getInstance(getActivity());
        mGaTracker = mGaInstance.getTracker(Constant.GA_ID);
        // methods use File | Settings |
        // File Templates.
        Bundle b = getArguments();

        if (b != null) {
            mCurrentFolderData = b.getParcelable("data");
        }

        sOfflineMode = !Utils.isNetConeccted(getActivity());

        mDataBaseAdapter = new DataBaseAdapter(getActivity());

        SharedPreferences settingPrefer = getActivity().getSharedPreferences(Constant.PREFS_NAME, 0);
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

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.FROYO) {
            mIsFroyoOrLater = true;
        } else {
            mIsFroyoOrLater = false;
        }
        mAutoUploadFolderId = Utils.getUploadFolderId(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsFragmentVisible = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAutoUploadFolderId = Utils.getUploadFolderId(getActivity());
            }
        }).start();
        if (mOpenOnlyFromDB) {
            mOpenOnlyFromDB = false;
            if (mCurrentFolderData == null) {
                getFileListFromDB("0");
            } else {
                getFileListFromDB(mCurrentFolderData.getID());
            }
        } else {
        }

        if (mMode == MODE_MOVE_FILE) {
            mImageRootLogo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mUploadStartedReceiver);
        getActivity().unregisterReceiver(mUploadProgressReceiver);
        getActivity().unregisterReceiver(mUploadFinishedReceiver);
        getActivity().unregisterReceiver(mRefreshReceiver);
        getActivity().unregisterReceiver(editFileReceiver);
    }

    public void newfolder() {
        sOfflineMode = !Utils.isNetConeccted(getActivity());
        if (sOfflineMode) {
            return;
        }

        if (mMode == MODE_MOVE_FILE) {
            onCancelMove();
        }

        mIsFirstFlag = true;

        if (getActivity() != null) {

            NewFolderDialog newFolderDialog = new NewFolderDialog();
            NewFolderDialog.mCurrentFolder = mCurrentFolderData;
            newFolderDialog.setOnCreateFolderListener(new onCreateFolderListener() {

                @Override
                public void onCreateFolder() {
                    reloadData();
                }
            });
            newFolderDialog.show(getActivity().getSupportFragmentManager(), "");
        }

        // NewFileActivity.mCurrentFolder = mCurrentFolderData;
        // if (getActivity() != null) {
        // Intent intent = new Intent(getActivity(), NewFileActivity.class);
        // startActivityForResult(intent, MainActivity.REQUEST_CREATE_FOLDER);
        // }
    }

    public void initValidationAlertDialog() {

        if (mValidationAlertDialog == null)
            mValidationAlertDialog = new AlertDialog.Builder(getActivity());

        mValidationAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
        mValidationAlertDialog.setTitle(R.string.txt_warning);
        mValidationAlertDialog.setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

                return;
            }
        });

        if (mSwitchOfflineModeAlertDialog == null)
            mSwitchOfflineModeAlertDialog = new AlertDialog.Builder(getActivity());

        mSwitchOfflineModeAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
        mSwitchOfflineModeAlertDialog.setTitle(R.string.txt_warning);
        mSwitchOfflineModeAlertDialog.setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                sIsOfflineModeSwitched = true;
                if (mCurrentFolderData == null) {
                    getFileListFromDB("0");
                } else {
                    getFileListFromDB(mCurrentFolderData.getID());
                }

                return;
            }
        });
    }

    public void initNetworkErrorAlertDialog() {
        if (mNetworkErrorAlertDialog == null)
            mNetworkErrorAlertDialog = new AlertDialog.Builder(getActivity());

        mNetworkErrorAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
        mNetworkErrorAlertDialog.setTitle(R.string.txt_warning);
        mNetworkErrorAlertDialog.setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
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
        Intent intent = new Intent(getActivity(), LogIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        // finish();
    }

    public void initLoadingDialog() {

        if (mLoadingDialog == null)
            mLoadingDialog = new ProgressDialog(getActivity());

        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        
        mLoadingDialog.setMessage(getActivity().getString(R.string.txt_loading));
        mLoadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mLoadingDialog.cancel();
                    mIsLoadingStop = true;
                }
                return false;
            }
        });

    }

    Handler ConnectionerrorMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (getActivity() != null) {
                showNetworkErrorAlertDialog(getActivity().getString(R.string.connection_error));
            }
        }
    };

    public void showNetworkErrorAlertDialog(String errorString) {
        mNetworkErrorAlertDialog.setMessage(errorString);
        mNetworkErrorAlertDialog.show();
    }

    Handler SwitchOfflineMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            mSwitchOfflineModeAlertDialog.setMessage(R.string.switch_offline);
            mSwitchOfflineModeAlertDialog.show();
        }
    };

    public void showValidationAlertDialog(String errorString) {
        mValidationAlertDialog.setMessage(errorString);
        mValidationAlertDialog.show();
    }

    public void showValidationAlertDialog(String title, String errorString) {
        mValidationAlertDialog.setTitle(title);
        mValidationAlertDialog.setMessage(errorString);
        mValidationAlertDialog.show();
    }

    public class DataItemClickListener implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            onDataListItemClick(position);
        }
    }

    public void onDataListItemClick(int position) {
        sOfflineMode = !Utils.isNetConeccted(getActivity());
        if (sOfflineMode && !sIsOfflineModeSwitched) {
            SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
            return;
        }

        long itemID = mFileListAdapter.getItemId(position);
        if (itemID == -1)
            return;

        FileData fileItem = (FileData) mFileListAdapter.getItem(position);

        mLogData.add(fileItem);
        // kkh
        if (sOfflineMode) {
            if (fileItem.getIsFolder()) {
                mCurrentFolderData = fileItem;
                getFileListFromDB(fileItem.getID());
                if (mCurrentFolderData == null || mFolderDeep == 0) {
                    mBtnHome.setVisibility(View.GONE);
                    if (mMode == MODE_MOVE_FILE) {
                        mImageRootLogo.setVisibility(View.VISIBLE);
                    }
                } else {
                    mBtnHome.setVisibility(View.VISIBLE);
                    if (mMode != MODE_MOVE_FILE)
                        mImageRootLogo.setVisibility(View.GONE);
                }
                return;
            } else {

                String fileFullPath = Utils.getFolderFullPath(mDataBaseAdapter.allFileDataList, fileItem);
                // String fileFullPath = Utils.getFolderFullPath(fileItem);
                File tmpFile = new File(fileFullPath);
                if (tmpFile.exists()) {
                    openContents(tmpFile);
                } else
                    Toast.makeText(this.getActivity(), getString(R.string.msg_non_exist_file), Toast.LENGTH_LONG).show();
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
                mSharedDirectoryID = fileItem.getID();
                mCurrentFolderData = fileItem;
                getSharedFileList(fileItem);
            } else if (mFolderDeep > 2) {
                getSharedFileList(fileItem);
                mCurrentFolderData = fileItem;
            }
        } else {

            if (fileItem.getIsFolder()) {
                // mListener.onClick(fileItem);
                mLoadingDialog.setMessage(getString(R.string.txt_loading));
                mLoadingDialog.show();
                mCurrentFolderData = fileItem;
                mFolderDeep++;
                getLoaderManager().restartLoader(0, null, loaderFromCloud);

            } else {

                if(fileItem.getName().endsWith(".mp3")){
                    Intent audioIntent = new Intent(getActivity(), PlayerActivity.class);
                    audioIntent.putExtra(PlayerActivity.EXTRA_CURRENT_FILE_NAME, fileItem.getName());
                    audioIntent.putExtra(PlayerActivity.EXTRA_CURRENT_FILE_PATH, Utils.getFolderFullPath(fileItem));
                    if(fileItem.getParent() == null){
                        audioIntent.putExtra(PlayerActivity.EXTRA_CURRENT_FILE_FOLDER_ID, "0");
                    }else{
                        audioIntent.putExtra(PlayerActivity.EXTRA_CURRENT_FILE_FOLDER_ID, fileItem.getParent().getID());
                    }
                    audioIntent.putExtra(PlayerActivity.EXTRA_CURRENT_FILE_URL, fileItem.getStreamLink());
                    audioIntent.putExtra(PlayerActivity.EXTRA_CURRENT_FILE_ID, fileItem.getID());
                    startActivity(audioIntent);
                }
                else {
                    openOrDownloadThenOpen(fileItem, false);
                }
            }
        }
        showAutoUploadStatus();
    }

    private void openOrDownloadThenOpen(final FileData fileItem, boolean isNeedConfirm) {

        String fileFullPath = Utils.getFolderFullPath(fileItem);
        File tmpFile = new File(fileFullPath);
        if (tmpFile.exists()) {
            openContents(tmpFile);
        } else {
            if (!mFileListAdapter.isEnable(fileItem)) {

                if (isNeedConfirm) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

                    alertDialog.setIcon(R.drawable.ic_dialog_alarm);
                    alertDialog.setTitle(R.string.app_name);
                    alertDialog.setMessage(getString(R.string.label_file_download, fileItem.getName()));

                    alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            downloadFile(fileItem);
                            fileItem.setDownloaded(true);
                            mFileListAdapter.notifyDataSetChanged();
                            return;
                        }
                    });

                    alertDialog.setNegativeButton(R.string.txt_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            return;
                        }
                    });
                    alertDialog.show();
                } else {
                    downloadFile(fileItem);
                    fileItem.setDownloaded(true);
                    mFileListAdapter.notifyDataSetChanged();
                }

            }
        }
    }

    public String getItemFullPathIfOfflineMode(FileData fileItem) {
        return Utils.getFolderFullPath(mDataBaseAdapter.allFileDataList, fileItem);
    }

    public void downloadFile(final FileData fileData) {
        mLoadingDialog.setMessage(getString(R.string.txt_downloading));
        mLoadingDialog.show();

        final String fileFullPath = Utils.getFolderFullPath(fileData);

        new Thread(new Runnable() {
            public void run() {

                try {
                    // Thread.sleep(100);
                    String fullPath = fileFullPath;
                    Request downloadRequest = new Request();

                    String downloadRequestUrl = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;

                    if (FileUtils.getExtention(fullPath).equalsIgnoreCase("dcm")) {
                        downloadRequestUrl = fileData.getStreamLink();
                        fullPath += ".jpg";
                    }

                    ArrayList<NameValuePair> postDataArrayList = new ArrayList<NameValuePair>();

                    postDataArrayList.add(new BasicNameValuePair("action", "open_file_download"));
                    postDataArrayList.add(new BasicNameValuePair("session_id", Constant.SessionID));
                    postDataArrayList.add(new BasicNameValuePair("is_pro", ""));
                    postDataArrayList.add(new BasicNameValuePair("share_user_id", ""));
                    postDataArrayList.add(new BasicNameValuePair("share_id", ""));
                    postDataArrayList.add(new BasicNameValuePair("access_dir_id", ""));
                    postDataArrayList.add(new BasicNameValuePair("file_id", fileData.getID()));
                    postDataArrayList.add(new BasicNameValuePair("file_offset", ""));

                    final boolean result = downloadRequest.downloadFile(downloadRequestUrl, postDataArrayList, fullPath, fileData);


                    if (mIsLoadingStop) {
                        mIsLoadingStop = false;
                        return;
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mLoadingDialog.dismiss();
                                // Toast.makeText(getActivity(), "result: "  + result, Toast.LENGTH_LONG).show();
                            }
                        });
                    }


                    if (!result) {
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        return;
                    } else {

                        openContents(new File(fullPath));
                    }
                } catch (final Exception e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mLoadingDialog.dismiss();


                            }
                        });
                    }
                    // ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void downloadFile(final FileData fileData, final boolean isOpen) {
        mLoadingDialog.setMessage(getString(R.string.txt_downloading));
        mLoadingDialog.show();

        final String fileFullPath = Utils.getFolderFullPath(fileData);

        new Thread(new Runnable() {
            public void run() {

                try {
                    Thread.sleep(100);

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

                    if (mIsLoadingStop) {
                        mIsLoadingStop = false;
                        return;
                    }

                    mLoadingDialog.dismiss();

                    if (!result) {
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        return;
                    } else {
                        getActivity().runOnUiThread(new Runnable() {

                            public void run() {

				/*
				 * mtxtSaveForOffline.setText(R.string.label_deleteoffline); mImgSaveForOffline.setImageResource(R.drawable.ic_delete);
				 */

                                if (isOpen)
                                    openContents(new File(fileFullPath));
                                else {
                                    Toast.makeText(getActivity(), getString(R.string.msg_confirm_successdownload), Toast.LENGTH_LONG).show();
                                    // AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                                    // alertDialog.setIcon(R.drawable.ic_dialog_alarm);
                                    // alertDialog.setTitle(R.string.app_name);
                                    // alertDialog.setMessage(R.string.msg_confirm_successdownload);
                                    // alertDialog.setPositiveButton(R.string.txt_close, new DialogInterface.OnClickListener() {
                                    // public void onClick(DialogInterface arg0, int arg1) {
                                    //
                                    // return;
                                    // }
                                    // });
                                    // alertDialog.show();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    mLoadingDialog.dismiss();
                    ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void moveFile(FileData fileItem) {
        if (sOfflineMode) {
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getActivity(), MoveActivity.class);
            intent.putExtra("fileItem", fileItem);
            getActivity().startActivity(intent);

            mSubMenuDetails.setVisibility(View.GONE);
            mPanel.setVisibility(View.GONE);
            mPanelDetails.setVisibility(View.GONE);
            mPanelBgImage.setVisibility(View.GONE);
            mListFooter.setLayoutParams(new android.widget.AbsListView.LayoutParams(0, 0));

            mListFooter.invalidate();
            mPanelDetails.invalidate();
            mPanelBgImage.invalidate();
            mPanel.invalidate();
            mFileListView.invalidate();
            mFileListAdapter.notifyDataSetInvalidated();
        }
    }

    private void renameFile(final FileData fileItem) {
        if (sOfflineMode) {
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        } else {

            final String oldFileFullPath = Utils.getFolderFullPath(fileItem);

            final String oldFileNameFull = fileItem.getName();
            final String oldFileName = Utils.getFileName(fileItem.getName());
            final String oldFileExtension = Utils.getExtension(fileItem.getName());

            AlertDialog.Builder renameAlert = new AlertDialog.Builder(getActivity());

            renameAlert.setTitle(getActivity().getResources().getString(R.string.rename_alert_title));
            renameAlert.setMessage(getActivity().getResources().getString(R.string.rename_alert_message));

            final EditText etRenameFIle = new EditText(getActivity());
            etRenameFIle.setText(oldFileNameFull);
            renameAlert.setView(etRenameFIle);

            renameAlert.setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    final String newFileName = etRenameFIle.getText().toString();
                    if (TextUtils.isEmpty(newFileName)) {
                        return;
                    }

                    if (newFileName.equals(oldFileNameFull)) {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.same_name), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //final String newFileNameFull = newFileName + oldFileExtension;
                    final String newFileNameFull = newFileName;

                    mLoadingDialog.setMessage(getString(R.string.txt_renaming));
                    mLoadingDialog.show();

                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                Thread.sleep(10);

                                Request renameRequest = new Request();

                                String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                                ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                                postData.add(new BasicNameValuePair("action", "rename_file"));
                                if (mCurrentFolderData == null) {
                                    postData.add(new BasicNameValuePair("access_dir_id", "0"));
                                } else {
                                    postData.add(new BasicNameValuePair("access_dir_id", mCurrentFolderData.getID()));
                                }
                                postData.add(new BasicNameValuePair("file_id", fileItem.getID()));
                                postData.add(new BasicNameValuePair("new_name", newFileNameFull));
                                postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                                postData.add(new BasicNameValuePair("share_id", ""));
                                postData.add(new BasicNameValuePair("share_user_id", ""));

                                String resultXML = renameRequest.httpPost(loginUrlString, postData);
                                if (mIsLoadingStop) {
                                    mIsLoadingStop = false;
                                    return;
                                }

                                if (resultXML.equals(Constant.ErrorMessage)) {
                                    mLoadingDialog.dismiss();
                                    ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                                    return;
                                } else {
                                    mRenameFileDataParser = new RenameFileDataParser();
                                    mRenameFileData = mRenameFileDataParser.parseResponse(resultXML);

                                    mLoadingDialog.dismiss();

                                    if (mRenameFileData != null) {
                                        if (TextUtils.isEmpty(mRenameFileData.getName()) || !TextUtils.isEmpty(mRenameFileData.getErrorMessage())) {
                                            renameFileFailedMessageHandler.sendMessage(renameFileFailedMessageHandler.obtainMessage());
                                            return;
                                        } else {

                                            fileItem.setName(newFileNameFull);

                                            File from = new File(oldFileFullPath);
                                            final File to = new File(Utils.getFolderFullPath(fileItem));
                                            if (from.exists()) {
                                                from.renameTo(to);
                                            }
                                            fileItem.setDescription(mRenameFileData.getDescription());
                                            fileItem.setLink(mRenameFileData.getLink());
                                            fileItem.setDateModified(mRenameFileData.getDateModified());
                                            fileItem.setDirectLink(mRenameFileData.getDirectLink());
                                            fileItem.setStreamLink(mRenameFileData.getStreamLink());
                                            fileItem.setDirectLinkPublic(mRenameFileData.getDirectLinkPublic());
                                            fileItem.setAccess(mRenameFileData.getAccess());
                                            mDataBaseAdapter.update(fileItem);

                                            MainActivity.mFileUploaded = true;
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mFileListAdapter.notifyDataSetChanged();
                                                    mFileNameDetail.setText(to.getName());
                                                }
                                            });

                                            return;
                                        }
                                    } else {
                                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                                        return;
                                    }
                                }

                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                mLoadingDialog.dismiss();
                                ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                                e.printStackTrace();
                                return;
                            }

			    /*
			     * runOnUiThread(new Runnable() {
			     * 
			     * @Override public void run() {
			     * 
			     * } });
			     */
                        }

                    }).start();

                }
            });

            renameAlert.setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            renameAlert.show();
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

        mFileListAdapter.notifyDataSetChanged();
    }

    public void showFilePropertyActivity() {
        Intent intent = new Intent(getActivity(), FilePropertyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void openContents(File file) {

        if (!file.exists()) {
            return;
        }

        if (mMimeTypes == null)
            getMimeTypes();

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

        Uri data = Uri.fromFile(file);

        String type = mMimeTypes.getMimeType(file.getName());

        if (type != null) {
            intent.setDataAndType(data, type);
            if(type.startsWith("image") || type.startsWith("application/msword") || type.startsWith("application/vnd.openxmlformats-officedocument") || type.startsWith("application/vnd.ms-excel") || type.startsWith("application/vnd.ms-powerpoint") || type.startsWith("application/pdf")) {
                mGaTracker.sendEvent(Constant.GA_EVENT_CAT_PREVIEW, Constant.GA_EVENT_ACTION_IMG_DOC_FILE_WAS_OPENED,"",0L);
            } else if (type.startsWith("video")) {
                mGaTracker.sendEvent(Constant.GA_EVENT_CAT_PLAYER, Constant.GA_EVENT_ACTION_VIDEO_FILE_WAS_OPENED,"",0L);
            } else if (type.startsWith("audio")) {
                mGaTracker.sendEvent(Constant.GA_EVENT_CAT_PLAYER, Constant.GA_EVENT_ACTION_AUDIO_FILE_WAS_OPENED,"",0L);
            }
        }

        if(file.getName().endsWith(".dcm.jpg")) {
            mGaTracker.sendEvent(Constant.GA_EVENT_CAT_PREVIEW, Constant.GA_EVENT_ACTION_DCM_FILE_WAS_OPENED,"",0L);
        }

        if (Utils.checkIsIntentCanBeHandled(getActivity(), intent)) {
            MainActivity.mContentOpened = true;
            //startActivity(intent);
            editedFile = file;
            saveFileToPref(file);
            startActivityForResult(intent,REQUEST_DOC);

        } else {
            Toast.makeText(getActivity(), "No one application to open this file", Toast.LENGTH_SHORT).show();
        }

    }

    private void saveFileToPref(File file) {
        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(file.getAbsolutePath(), file.lastModified());
        editor.commit();
    }


    private static final int REQUEST_DOC = 25;
    private File editedFile;

    private void getMimeTypes() {
        MimeTypeParser mtp = new MimeTypeParser();
        XmlResourceParser in = getResources().getXml(R.xml.mimetypes);

        try {
            mMimeTypes = mtp.fromXmlResource(in);
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
        mLoadingDialog.setMessage(getString(R.string.txt_loading));
        mLoadingDialog.show();

        mShareUserID = "";

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10);

                    Request request = new Request();

                    String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                    postData.add(new BasicNameValuePair("action", "list_shared_users"));
                    postData.add(new BasicNameValuePair("session_id", Constant.SessionID));

                    String resultXML = request.httpPost(loginUrlString, postData);

                    if (mIsLoadingStop) {
                        mLoadingDialog.dismiss();
                        mIsLoadingStop = false;
                        mBackKeyPressed = false;
                        mRefresh = false;
                        return;
                    }

                    if (resultXML.equals(Constant.ErrorMessage)) {
                        mLoadingDialog.dismiss();
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        return;
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

                        Log.d(TAG, "insert to DB from getSharedUsersList");
                        mDataBaseAdapter.insert(mFileListValues);
                        mLoadingDialog.dismiss();

                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    mLoadingDialog.dismiss();
                    String exceptionString = e.getMessage();
                    if (sOfflineMode) {
                        sOfflineMode = true;
                        if (!sIsOfflineModeSwitched) {
                            SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
                        } else {
                            showFileListInOfflineMode();
                        }
                    } else {
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        e.printStackTrace();
                    }
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if ((mMode == MODE_MOVE_FILE || mMode == MODE_SELECT_AUTO_UPLOAD_FOLDER)) {
                            ArrayList<FileData> folders = new ArrayList<FileData>();
                            for (FileData file : mFileListValues) {
                                if (file.getIsFolder()) {
                                    folders.add(file);
                                }
                            }

                            if (mCurrentFolderData == null || mFolderDeep == 0) {
                                if (LogIn.mLoginData != null && LogIn.mLoginData.getIsAccessUser().equals("False")) {
                                    FileData rootFileData = new FileData();
                                    rootFileData.setIsFolder(true);
                                    rootFileData.setName(getString(R.string.app_name));
                                    rootFileData.setID("0");
                                    folders.add(0, rootFileData);
                                }

                            }

                            mFileListAdapter = new FileListAdapter(getActivity(), R.layout.fileitem, folders, FilesFragment.this, FilesFragment.this);
                        } else {
                            mFileListAdapter = new FileListAdapter(getActivity(), R.layout.fileitem, mFileListValues, FilesFragment.this, FilesFragment.this);
                        }
                        mFileListView.setAdapter(mFileListAdapter);
                        mFileListAdapter.notifyDataSetChanged();

                        // if(mFolderDeep > 0)
                        // btnBack.setVisibility(View.VISIBLE);
                        // else
                        // btnBack.setVisibility(View.GONE);

                    }
                });

            }
        }).start();

    }

    public void getSharedFoldersList(final String shareUserID) {
        mLoadingDialog.setMessage(getString(R.string.txt_loading));
        mLoadingDialog.show();

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10);

                    Request request = new Request();

                    String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                    postData.add(new BasicNameValuePair("action", "list_shared_directories"));
                    postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                    postData.add(new BasicNameValuePair("share_user_id", shareUserID));

                    String resultXML = request.httpPost(loginUrlString, postData);

                    if (mIsLoadingStop) {
                        mLoadingDialog.dismiss();
                        mIsLoadingStop = false;
                        mRefresh = false;
                        return;
                    }

                    if (resultXML.equals(Constant.ErrorMessage)) {
                        mLoadingDialog.dismiss();
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        return;
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

                        Log.d(TAG, "insert to DB from getSharedFoldersList");
                        mDataBaseAdapter.insert(mFileListValues);
                        mLoadingDialog.dismiss();

                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    mLoadingDialog.dismiss();

                    String exceptionString = e.getMessage();
                    if (sOfflineMode) {
                        sOfflineMode = true;
                        if (!sIsOfflineModeSwitched) {
                            SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
                        } else {
                            showFileListInOfflineMode();
                        }
                    } else {
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        e.printStackTrace();
                    }
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if ((mMode == MODE_MOVE_FILE || mMode == MODE_SELECT_AUTO_UPLOAD_FOLDER)) {
                            ArrayList<FileData> folders = new ArrayList<FileData>();
                            for (FileData file : mFileListValues) {
                                if (file.getIsFolder()) {
                                    folders.add(file);
                                }
                            }

                            if (mCurrentFolderData == null || mFolderDeep == 0) {
                                if (LogIn.mLoginData != null && LogIn.mLoginData.getIsAccessUser().equals("False")) {
                                    FileData rootFileData = new FileData();
                                    rootFileData.setIsFolder(true);
                                    rootFileData.setName(getString(R.string.app_name));
                                    rootFileData.setID("0");
                                    folders.add(0, rootFileData);
                                }

                            }

                            mFileListAdapter = new FileListAdapter(getActivity(), R.layout.fileitem, folders, FilesFragment.this, FilesFragment.this);
                        } else {
                            mFileListAdapter = new FileListAdapter(getActivity(), R.layout.fileitem, mFileListValues, FilesFragment.this, FilesFragment.this);
                        }
                        mFileListView.setAdapter(mFileListAdapter);
                        mFileListAdapter.notifyDataSetChanged();

                        // if(mFolderDeep > 0)
                        // btnBack.setVisibility(View.VISIBLE);
                        // else
                        // btnBack.setVisibility(View.GONE);
                        //
                    }
                });

            }
        }).start();

    }

    public void getSharedFileList(final FileData parentFileItem) {
        mLoadingDialog.setMessage(getString(R.string.txt_loading));
        mLoadingDialog.show();

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10);

                    Request request = new Request();

                    String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                    postData.add(new BasicNameValuePair("action", "list_dir"));
                    postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                    postData.add(new BasicNameValuePair("share_user_id", parentFileItem.getShareUserID()));
                    postData.add(new BasicNameValuePair("share_id", parentFileItem.getID()));
                    postData.add(new BasicNameValuePair("access_dir_id", parentFileItem.getShareUserID()));
                    postData.add(new BasicNameValuePair("dir_id", mSharedDirectoryID));

                    String resultXML = request.httpPost(loginUrlString, postData);

                    if (mIsLoadingStop) {
                        mLoadingDialog.dismiss();
                        mIsLoadingStop = false;
                        mRefresh = false;
                        return;
                    }

                    if (resultXML.equals(Constant.ErrorMessage)) {
                        mLoadingDialog.dismiss();
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        return;
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

                        Log.d(TAG, "insert to DB from getSharedFileList");
                        mDataBaseAdapter.insert(mFileListValues);
                        mLoadingDialog.dismiss();

                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    mLoadingDialog.dismiss();

                    String exceptionString = e.getMessage();
                    if (sOfflineMode) {
                        sOfflineMode = true;
                        if (!sIsOfflineModeSwitched) {
                            SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
                        } else {
                            showFileListInOfflineMode();
                        }
                    } else {
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        e.printStackTrace();
                    }
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if ((mMode == MODE_MOVE_FILE || mMode == MODE_SELECT_AUTO_UPLOAD_FOLDER)) {
                            ArrayList<FileData> folders = new ArrayList<FileData>();
                            for (FileData file : mFileListValues) {
                                if (file.getIsFolder()) {
                                    folders.add(file);
                                }
                            }

                            if (mCurrentFolderData == null || mFolderDeep == 0) {
                                if (LogIn.mLoginData != null && LogIn.mLoginData.getIsAccessUser().equals("False")) {
                                    FileData rootFileData = new FileData();
                                    rootFileData.setIsFolder(true);
                                    rootFileData.setName(getString(R.string.app_name));
                                    rootFileData.setID("0");
                                    folders.add(0, rootFileData);
                                }

                            }

                            mFileListAdapter = new FileListAdapter(getActivity(), R.layout.fileitem, folders, FilesFragment.this, FilesFragment.this);
                        } else {
                            mFileListAdapter = new FileListAdapter(getActivity(), R.layout.fileitem, mFileListValues, FilesFragment.this, FilesFragment.this);
                        }
                        mFileListView.setAdapter(mFileListAdapter);
                        mFileListAdapter.notifyDataSetChanged();

                        // if(mFolderDeep > 0)
                        // btnBack.setVisibility(View.VISIBLE);
                        // else
                        // btnBack.setVisibility(View.GONE);

                    }
                });

            }
        }).start();

    }

    public void shareFile(final FileData fileItem) {
        String fileFullPath;
        if (sOfflineMode) {
            fileFullPath = getItemFullPathIfOfflineMode(fileItem);
        } else {
            fileFullPath = Utils.getFolderFullPath(fileItem);
        }
        File file = new File(fileFullPath);

        if (file.exists()) {

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            shareIntent.setType("*/*");
            startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_file_chooser_title)));
        } else {

            if (sOfflineMode) {
                Toast.makeText(getActivity(), getString(R.string.msg_non_exist_file) + "\n" + getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

            alertDialog.setIcon(R.drawable.ic_dialog_alarm);
            alertDialog.setTitle(R.string.app_name);
            alertDialog.setMessage(getString(R.string.label_file_download, fileItem.getName()));

            alertDialog.setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    downloadFile(fileItem);
                    return;
                }
            });
            alertDialog.setNegativeButton(R.string.txt_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    return;
                }
            });
            alertDialog.show();

        }
    }

    public void deleteFile(final FileData fileItem) {
        mLoadingDialog.setMessage(getString(R.string.txt_deleting));
        mLoadingDialog.show();

        final FileData item = fileItem;

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10);

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

                    Log.i("test", "delete result xml = " + resultXML);

                    if (mIsLoadingStop) {
                        mIsLoadingStop = false;
                        return;
                    }

                    if (resultXML.equals(Constant.ErrorMessage)) {
                        mLoadingDialog.dismiss();
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        return;
                    } else {
                        mDeleteFileDataParser = new DeletFileDataParser();
                        mDeleteFileData = mDeleteFileDataParser.parseResponse(resultXML);

                        mLoadingDialog.dismiss();

                        if (mDeleteFileData != null) {
                            if (mDeleteFileData.getName().length() > 0) {
                                DeleteFileFailedMessageHandler.sendMessage(DeleteFileFailedMessageHandler.obtainMessage());
                                return;
                            } else {

                                MainActivity.mFileUploaded = true;
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int position = mFileListAdapter.items.indexOf(item);
                                        if (position == mFileListAdapter.items.size() - 1) {
                                            position--;
                                        }
                                        mFileListValues.remove(item);
                                        mFileListAdapter.items.remove(item);
                                        mFileListAdapter.notifyDataSetChanged();
                                        if (mFileListAdapter.items.isEmpty()) {
                                            mSubMenuDetails.setVisibility(View.GONE);
                                            mPanel.setVisibility(View.GONE);
                                            mPanelDetails.setVisibility(View.GONE);
                                            mPanelBgImage.setVisibility(View.GONE);
                                            mListFooter.setLayoutParams(new android.widget.AbsListView.LayoutParams(0, 0));
                                            mListFooter.invalidate();
                                        } else {
                                            if (mListFooter.getMeasuredHeight() > 0) {
                                                if (mListFooter.getMeasuredHeight() > mFileListView.getChildAt(0).getMeasuredHeight()) {
                                                    mListFooter.setLayoutParams(new android.widget.AbsListView.LayoutParams(0, mListFooter.getMeasuredHeight() - mFileListView.getChildAt(0).getMeasuredHeight()));
                                                } else {
                                                    mListFooter.setLayoutParams(new android.widget.AbsListView.LayoutParams(0, 0));
                                                }
                                                mListFooter.invalidate();
                                            }
                                            if (mSubMenuDetails.getVisibility() == View.VISIBLE) {
                                                onItemClick(position + 1, mFileListAdapter.items.get(position), 0, 0, MODE_VIEW);
                                                mSubMenuDetails.setVisibility(View.VISIBLE);
                                            } else {
                                                onItemClick(position + 1, mFileListAdapter.items.get(position), 0, 0, MODE_VIEW);
                                            }
                                            mCurrentPosition = position;
                                        }
                                    }
                                });

                                fileItem.setDateDeleted(Calendar.getInstance().getTime().toString());
                                mDataBaseAdapter.update(fileItem);

                                return;
                            }
                        } else {
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            return;
                        }
                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    mLoadingDialog.dismiss();
                    ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                    e.printStackTrace();
                    return;
                }

		/*
		 * runOnUiThread(new Runnable() {
		 * 
		 * @Override public void run() {
		 * 
		 * } });
		 */
            }

        }).start();
    }


    public void reSaveFile(final FileData fileItem) {

        final FileData item = fileItem;

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10);

                    Request loginRequest = new Request();

                    String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                    postData.add(new BasicNameValuePair("action", "remove_file"));
                    postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                    postData.add(new BasicNameValuePair("share_user_id", ""));
                    postData.add(new BasicNameValuePair("share_id", ""));
                    postData.add(new BasicNameValuePair("access_dir_id", ""));
                    postData.add(new BasicNameValuePair("file_id", item.getID()));

                    String resultXML = loginRequest.httpPost(loginUrlString, postData);

                    Log.i("test", "delete result xml = " + resultXML);

                    if (mIsLoadingStop) {
                        mIsLoadingStop = false;
                        return;
                    }

                    if (resultXML.equals(Constant.ErrorMessage)) {
                        mLoadingDialog.dismiss();
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        return;
                    } else {
                        mDeleteFileDataParser = new DeletFileDataParser();
                        mDeleteFileData = mDeleteFileDataParser.parseResponse(resultXML);

                        mLoadingDialog.dismiss();

                        if (mDeleteFileData != null) {
                            if (mDeleteFileData.getName().length() > 0) {
                                DeleteFileFailedMessageHandler.sendMessage(DeleteFileFailedMessageHandler.obtainMessage());
                                return;
                            } else {

                                MainActivity.mFileUploaded = true;
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((MainActivity) getActivity()).uploadImage(Utils.getFolderFullPath(fileItem));
                                    }
                                });

                                return;
                            }
                        } else {
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            return;
                        }
                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    mLoadingDialog.dismiss();
                    ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                    e.printStackTrace();
                    return;
                }

		/*
		 * runOnUiThread(new Runnable() {
		 *
		 * @Override public void run() {
		 *
		 * } });
		 */
            }

        }).start();
    }

    Handler DeleteFileFailedMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (mDeleteFileData.getName().length() > 0)
                mValidationAlertDialog.setTitle(mDeleteFileData.getName());
            else
                mValidationAlertDialog.setTitle("Unknow Error");

            if (mDeleteFileData.getDescription().length() > 0)
                mValidationAlertDialog.setMessage(mDeleteFileData.getDescription());
            else
                mValidationAlertDialog.setMessage("Unknow Error");

            mValidationAlertDialog.show();
        }
    };

    Handler renameFileFailedMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (TextUtils.isEmpty(mRenameFileData.getName())) {
                mValidationAlertDialog.setTitle("Unknow Error");
            } else if (!TextUtils.isEmpty(mRenameFileData.getErrorMessage())) {
                mValidationAlertDialog.setTitle(mRenameFileData.getErrorMessage());
            }
            mValidationAlertDialog.show();
        }
    };

    Handler moveFileFailedMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (TextUtils.isEmpty(mMoveFileData.getName())) {
                mValidationAlertDialog.setTitle("Unknow Error");
            } else if (!TextUtils.isEmpty(mMoveFileData.getErrorMessage())) {
                mValidationAlertDialog.setTitle(mMoveFileData.getErrorMessage());
            }
            mValidationAlertDialog.show();
        }
    };

    // kkh
    public void getFileListFromDB(final String parentID) {
        new Thread(new Runnable() {
            public void run() {

                ArrayList<FileData> fileDataList = new ArrayList<FileData>();

                if (mDataBaseAdapter.allFileDataList == null)
                    mDataBaseAdapter.selectAll();

                if (mDataBaseAdapter.allFileDataList == null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.offline_first_time), Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                        }
                    });
                    return;
                }

                int fileCount = mDataBaseAdapter.allFileDataList.size();

                for (int i = 0; i < fileCount; i++) {
                    FileData fileData = mDataBaseAdapter.allFileDataList.get(i);

                    if (fileData.getParentID().equals(parentID)) {
                        fileDataList.add(fileData);
                    }
                }

                mFileListValues = fileDataList;

                if (mBackKeyPressed) {
                    mFolderDeep--;
                    mBackKeyPressed = false;
                } else if (mRefresh) {
                    mRefresh = false;
                } else if (mCurrentFolderData != null) {
                    mFolderDeep++;
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mFileListAdapter = new FileListAdapter(getActivity(), R.layout.fileitem, mFileListValues, FilesFragment.this, FilesFragment.this);
                            mFileListView.setAdapter(mFileListAdapter);
                            mFileListAdapter.notifyDataSetChanged();
                        }
                    });
                }

            }
        }).start();

    }

    public void login() {

        mLoadingDialog.show();

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
                        mLoadingDialog.dismiss();
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        return;
                    } else {
                        LoginDataParser mLoginDataParser = null;
                        LoginData mLoginData = null;
                        mLoginDataParser = new LoginDataParser();
                        mLoginData = mLoginDataParser.parseResponse(resultXML);

                        mLoadingDialog.dismiss();

                        if (mLoginData != null) {
                            if (mLoginData.getUserName().length() == 0) {
                                LoginFailedMessageHandler.sendMessage(LoginFailedMessageHandler.obtainMessage());
                                return;
                            } else {
                                Constant.SessionID = mLoginData.getSessionID();
                            }
                        } else {
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            return;
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    mLoadingDialog.dismiss();
                    ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                    e.printStackTrace();
                }
            }
        }).start();
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

    public void refresh() {
        mRefresh = true;
        sOfflineMode = !Utils.isNetConeccted(getActivity());
        if (sOfflineMode) {
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
            if (mFolderDeep == 0) {
                checkSharedFolder();
            }
            else {
                if (mCurrentFolderData != null) {
                } else {
                }
                getFileList(mCurrentFolderData);
            }
        }
        showAutoUploadStatus();
    }

    public void checkSharedFolder() {
        if (mLoadingDialog == null) {
            initLoadingDialog();
        }
        mLoadingDialog.setMessage(getActivity().getResources().getString(R.string.txt_loading));
        mLoadingDialog.show();

        new Thread(new Runnable() {
            public void run() {

                try {
                    Thread.sleep(100);

                    Request loginRequest = new Request();

                    String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                    postData.add(new BasicNameValuePair("action", "has_share_folders"));
                    postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                    String resultXML = loginRequest.httpPost(loginUrlString, postData);

                    if (resultXML.equals(Constant.ErrorMessage)) {
                        mLoadingDialog.dismiss();
                        return;
                    } else {
                        mSharedFolderDataParser = new SharedFolderDataParser();
                        mHasSharedFolders = mSharedFolderDataParser.parseResponse(resultXML);
                        if (mHasSharedFolders && LogIn.mLoginData.getIsAccessUser().equals("False")) {
                            mSharedFolders = new FileData("0", null);
                            mSharedFolders.setID("Shared Folders");
                            mSharedFolders.setName("Shared Folders");
                            mSharedFolders.setIsFolder(true);
                            mSharedFolders.setIsSharedFolders(true);
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

                    if (mIsLoadingStop) {
                        mLoadingDialog.dismiss();
                        mIsLoadingStop = false;
                        mBackKeyPressed = false;
                        mRefresh = false;
                        return;
                    }

                    if (resultXML.equals(Constant.ErrorMessage)) {
                        mLoadingDialog.dismiss();
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        return;
                    } else {
                        mFileDataParser = new FileDataParser();
                        mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);

                        if (mHasSharedFolders && mSharedFolders != null)
                            mFileListValues.add(mSharedFolders);

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

                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if ((mMode == MODE_MOVE_FILE || mMode == MODE_SELECT_AUTO_UPLOAD_FOLDER)) {
                                ArrayList<FileData> folders = new ArrayList<FileData>();
                                for (FileData file : mFileListValues) {
                                    if (file.getIsFolder()) {
                                        folders.add(file);
                                    }
                                }

                                if (mCurrentFolderData == null || mFolderDeep == 0) {
                                    if (LogIn.mLoginData != null && LogIn.mLoginData.getIsAccessUser().equals("False")) {
                                        FileData rootFileData = new FileData();
                                        rootFileData.setIsFolder(true);
                                        rootFileData.setName(getString(R.string.app_name));
                                        rootFileData.setID("0");
                                        folders.add(0, rootFileData);
                                    }

                                }

                                mFileListAdapter = new FileListAdapter(getActivity(), R.layout.fileitem, folders, FilesFragment.this, FilesFragment.this);
                            } else {
                                mFileListAdapter = new FileListAdapter(getActivity(), R.layout.fileitem, mFileListValues, FilesFragment.this, FilesFragment.this);
                            }
                            mFileListView.setAdapter(mFileListAdapter);
                            mFileListAdapter.notifyDataSetChanged();

                            mFolderDeep = 0;
                            mRefresh = false;
                            mBackKeyPressed = false;
                            mLoadingDialog.dismiss();
                        }
                    });

                    getIconHandler.sendMessage(getIconHandler.obtainMessage());
                } catch (Exception e) {
                    mLoadingDialog.dismiss();

                    String exceptionString = e.getMessage();
                    if (sOfflineMode) {
                        sOfflineMode = true;
                        if (!sIsOfflineModeSwitched) {
                            SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
                        } else {
                            showFileListInOfflineMode();
                        }
                    } else {
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        e.printStackTrace();
                    }
                }
            }
        }).start();

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

                        if (iconBitmap != null && getActivity() != null) {
                            fileData.setIcon(iconBitmap);

                            getActivity().runOnUiThread(new Runnable() {

                                public void run() {
                                    if (mFileListAdapter != null)
                                        mFileListAdapter.notifyDataSetChanged();
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

    public void getFileList(final FileData fileItem) {
        mLoadingDialog.setMessage(getActivity().getResources().getString(R.string.txt_loading));
        mLoadingDialog.show();

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10);
                    Request loginRequest = new Request();

                    String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                    postData.add(new BasicNameValuePair("action", "list_dir"));
                    postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                    postData.add(new BasicNameValuePair("share_user_id", ""));
                    postData.add(new BasicNameValuePair("share_id", ""));
                    if (fileItem == null) {
                        postData.add(new BasicNameValuePair("access_dir_id", "0"));
                        postData.add(new BasicNameValuePair("dir_id", "0"));
                    } else {
                        postData.add(new BasicNameValuePair("access_dir_id", fileItem.getAccessDirID()));
                        postData.add(new BasicNameValuePair("dir_id", fileItem.getID()));
                    }
                    String resultXML = loginRequest.httpPost(loginUrlString, postData);

                    if (mIsLoadingStop) {
                        mLoadingDialog.dismiss();
                        mIsLoadingStop = false;
                        mBackKeyPressed = false;
                        mRefresh = false;
                        return;
                    }

                    if (resultXML.equals(Constant.ErrorMessage)) {
                        mLoadingDialog.dismiss();
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        return;
                    } else {
                        mFileDataParser = new FileDataParser();
                        if (mBackKeyPressed) {
                            if (mFolderDeep == 1) {
                                mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);

                                if (mHasSharedFolders && mSharedFolders != null)
                                    mFileListValues.add(mSharedFolders);

                            } else
                                mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem.getAccessDirID(), fileItem.getParent());
                        } else {
                            if (mFolderDeep == -1) {
                                mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);

                                if (mHasSharedFolders && mSharedFolders != null)
                                    mFileListValues.add(mSharedFolders);

                            } else if (mFolderDeep == 0) {
                                if (fileItem == null)
                                    mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);
                                else
                                    mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem.getID(), fileItem);

                            } else if (mFolderDeep == 1) {
                                mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem.getID(), fileItem);
                            } else {
                                mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem.getAccessDirID(), fileItem);
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

                        Log.d(TAG, "insert to DB from getFileList");
                        mDataBaseAdapter.insert(mFileListValues);
                    }

                } catch (Exception e) {
                    mLoadingDialog.dismiss();

                    String exceptionString = e.getMessage();
                    if (sOfflineMode) {
                        sOfflineMode = true;
                        if (!sIsOfflineModeSwitched) {
                            SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
                        } else {
                            showFileListInOfflineMode();
                        }
                    } else {
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        e.printStackTrace();
                    }

                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (mBackKeyPressed) {
                            mFolderDeep--;
                            mCurrentFolderData = fileItem;
                        } else if (mRefresh) {
                            mRefresh = false;
                        } else {
                            mCurrentFolderData = fileItem;
                            mFolderDeep++;
                        }

                        if ((mMode == MODE_MOVE_FILE || mMode == MODE_SELECT_AUTO_UPLOAD_FOLDER)) {
                            ArrayList<FileData> folders = new ArrayList<FileData>();
                            for (FileData file : mFileListValues) {
                                if (file.getIsFolder()) {
                                    folders.add(file);
                                }
                            }

                            if (mCurrentFolderData == null || mFolderDeep == 0) {
                                if (LogIn.mLoginData != null && LogIn.mLoginData.getIsAccessUser().equals("False")) {
                                    FileData rootFileData = new FileData();
                                    rootFileData.setIsFolder(true);
                                    rootFileData.setName(getString(R.string.app_name));
                                    rootFileData.setID("0");
                                    folders.add(0, rootFileData);
                                }

                            }

                            mFileListAdapter = new FileListAdapter(getActivity(), R.layout.fileitem, folders, FilesFragment.this, FilesFragment.this);
                        } else {
                            mFileListAdapter = new FileListAdapter(getActivity(), R.layout.fileitem, mFileListValues, FilesFragment.this, FilesFragment.this);
                        }
                        mFileListView.setAdapter(mFileListAdapter);
                        mFileListAdapter.notifyDataSetChanged();

                        mBackKeyPressed = false;
                        mRefresh = false;
                        mLoadingDialog.dismiss();
                    }
                });

                getIconHandler.sendMessage(getIconHandler.obtainMessage());
            }
        }).start();
    }

    public FileData getCurrentFolderData() {
        return mCurrentFolderData;
    }

    public void onBackPressed() {
        if (mPanelDetails.getVisibility() == View.VISIBLE) {
            mSubMenuDetails.setVisibility(View.GONE);
            mPanel.setVisibility(View.GONE);
            mPanelDetails.setVisibility(View.GONE);
            mPanelBgImage.setVisibility(View.GONE);
            mListFooter.setLayoutParams(new android.widget.AbsListView.LayoutParams(0, 0));
            mListFooter.invalidate();
        } else if (mMode == MODE_VIEW_SEARCH_RESULTS) {
            // updateFileList(mCurrentShowingFiles, MODE_VIEW);
            reloadData();
            rlSearchView.setVisibility(View.GONE);
            rlActionBarItems.setVisibility(View.VISIBLE);
            mTextActionBarTitle.setVisibility(View.VISIBLE);
            mEditSearch.setText("");
            if (mCurrentFolderData == null || mFolderDeep == 0) {
                mImageRootLogo.setVisibility(View.VISIBLE);
            }
        } else {
            getBack();
        }
    }

    public void getBack() {
        mBackKeyPressed = true;
        // kkh
        if (sOfflineMode) {
            if (mFolderDeep == 1) {
                mCurrentFolderData = null;
                getFileListFromDB("0");
            } else if (mFolderDeep > 1) {

                FileData fileItem = mDataBaseAdapter.selectFileData(mCurrentFolderData.getParentID());
                mCurrentFolderData = fileItem;
                getFileListFromDB(fileItem.getID());
            } else {
                getActivity().finish();
            }

            if (mCurrentFolderData == null || mFolderDeep == 0) {
                mBtnHome.setVisibility(View.GONE);
                if (mMode != MODE_MOVE_FILE) {
                    mImageRootLogo.setVisibility(View.VISIBLE);
                }
            } else {
                mBtnHome.setVisibility(View.VISIBLE);
                if (mMode != MODE_MOVE_FILE)
                    mImageRootLogo.setVisibility(View.GONE);
            }

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
            } else {
                getActivity().finish();
            }
        }
        showAutoUploadStatus();
    }

    public void getBackFileList(final FileData fileItem) {
        mLoadingDialog.setMessage(getString(R.string.txt_loading));
        mLoadingDialog.show();

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10);

                    Request loginRequest = new Request();

                    String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                    postData.add(new BasicNameValuePair("action", "list_dir"));
                    postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                    postData.add(new BasicNameValuePair("share_user_id", ""));
                    postData.add(new BasicNameValuePair("share_id", ""));
                    if (fileItem == null) {
                        postData.add(new BasicNameValuePair("access_dir_id", "0"));
                        postData.add(new BasicNameValuePair("dir_id", "0"));
                    } else {
                        postData.add(new BasicNameValuePair("access_dir_id", fileItem.getAccessDirID()));
                        postData.add(new BasicNameValuePair("dir_id", fileItem.getID()));
                    }
                    String resultXML = loginRequest.httpPost(loginUrlString, postData);

                    if (mIsLoadingStop) {
                        mLoadingDialog.dismiss();
                        mIsLoadingStop = false;
                        mBackKeyPressed = false;
                        mRefresh = false;
                        return;
                    }

                    if (resultXML.equals(Constant.ErrorMessage)) {
                        mLoadingDialog.dismiss();
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        return;
                    } else {
                        mFileDataParser = new FileDataParser();

                        if (mBackKeyPressed) {
                            if (mFolderDeep == 1) {
                                mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);

                                if (mHasSharedFolders && mSharedFolders != null)
                                    mFileListValues.add(mSharedFolders);

                            } else {
                                if (fileItem == null)
                                    mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);
                                else
                                    mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem.getAccessDirID(), fileItem.getParent());
                            }
                        } else {
                            if (mFolderDeep == -1) {
                                mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);

                                if (mHasSharedFolders && mSharedFolders != null)
                                    mFileListValues.add(mSharedFolders);

                            } else if (mFolderDeep == 0) {
                                if (fileItem == null)
                                    mFileListValues = mFileDataParser.parseResponse(resultXML, "0", null);
                                else
                                    mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem.getID(), fileItem);

                            } else if (mFolderDeep == 1) {
                                mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem.getID(), fileItem);
                            } else {
                                mFileListValues = mFileDataParser.parseResponse(resultXML, fileItem.getAccessDirID(), fileItem);
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
                            if (fileItem == null)
                                fileData.setParentID("0");
                            else {
                                fileData.setParentID(fileItem.getID());
                                fileData.setParent(fileItem);
                            }
                        }

                        for (FileData fd : mFileListValues) {
                        }
                        Log.d(TAG, "insert to DB from getBackFileList");
                        mDataBaseAdapter.insert(mFileListValues);
                        mLoadingDialog.dismiss();
                    }

                    if (mBackKeyPressed) {
                        mFolderDeep--;
                        mLogData.remove(mCurrentFolderData);
                        mCurrentFolderData = fileItem;
                    } else if (mRefresh) {
                        mRefresh = false;
                    } else {
                        mLogData.remove(mCurrentFolderData);
                        mCurrentFolderData = fileItem;
                        mFolderDeep++;
                    }

                } catch (Exception e) {
                    mLoadingDialog.dismiss();
                    sOfflineMode = !Utils.isNetConeccted(getActivity());
                    String exceptionString = e.getMessage();
                    if (sOfflineMode) {
                        sOfflineMode = true;
                        if (!sIsOfflineModeSwitched) {
                            SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
                        } else {
                            showFileListInOfflineMode();
                        }
                    } else {
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        e.printStackTrace();
                    }

                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

			/*
			 * if(mBackKeyPressed){ mFolderDeep--; mCurrentFolderData = fileItem; } else if(mRefresh){ mRefresh = false; } else{ mCurrentFolderData = fileItem; mFolderDeep++; }
			 */

                        if (mCurrentFolderData == null || mFolderDeep == 0) {
                            mBtnHome.setVisibility(View.GONE);
                            mImageRootLogo.setVisibility(View.VISIBLE);
                        } else {
                            mBtnHome.setVisibility(View.VISIBLE);
                            mImageRootLogo.setVisibility(View.GONE);
                        }


                        if ((mMode == MODE_MOVE_FILE || mMode == MODE_SELECT_AUTO_UPLOAD_FOLDER)) {
                            ArrayList<FileData> folders = new ArrayList<FileData>();
                            for (FileData file : mFileListValues) {
                                if (file.getIsFolder()) {
                                    folders.add(file);
                                }
                            }

                            if (mCurrentFolderData == null || mFolderDeep == 0) {
                                if (LogIn.mLoginData != null && LogIn.mLoginData.getIsAccessUser().equals("False")) {
                                    FileData rootFileData = new FileData();
                                    rootFileData.setIsFolder(true);
                                    rootFileData.setName(getString(R.string.app_name));
                                    rootFileData.setID("0");
                                    folders.add(0, rootFileData);
                                }

                            }

                            mFileListAdapter = new FileListAdapter(getActivity(), R.layout.fileitem, folders, FilesFragment.this, FilesFragment.this);
                        } else {
                            mFileListAdapter = new FileListAdapter(getActivity(), R.layout.fileitem, mFileListValues, FilesFragment.this, FilesFragment.this);
                        }
                        mFileListView.setAdapter(mFileListAdapter);
                        mFileListAdapter.notifyDataSetChanged();

                        mBackKeyPressed = false;
                        mRefresh = false;

                    }
                }

                );

                getIconHandler.sendMessage(getIconHandler.obtainMessage());
            }

        }

        ).

                start();

    }

    public void goDeeper() {
        mFolderDeep++;
    }

    public int getFolderDeep() {
        return mFolderDeep;
    }

    public void setFolderDeep(int mFolderDeep) {
        this.mFolderDeep = mFolderDeep;
    }

    /**
     * Search files and folders
     *
     * @param query - part of name
     */
    public void searchForFile(final String query) {

        new AsyncTask<Void, Void, ArrayList<FileData>>() {

            private ProgressDialog pr_dialog;

            @Override
            protected void onPreExecute() {
                pr_dialog = ProgressDialog.show(getActivity(), "Searching", "Searching for files...", true, true);
            }

            @Override
            protected ArrayList<FileData> doInBackground(Void... params) {
                DataBaseAdapter adapter = new DataBaseAdapter(getActivity());
                adapter.openDatabase();
                String parentID = "0";
                if (getCurrentFolderData() != null) {
                    parentID = getCurrentFolderData().getID();
                }
                ArrayList<FileData> fileData = adapter.selectFileDataByFileName(query, parentID);
                // close db
                adapter.closeDatabase();
                return fileData;
            }

            @Override
            protected void onPostExecute(final ArrayList<FileData> fileData) {
                int len = fileData != null ? fileData.size() : 0;
                pr_dialog.dismiss();
                if (len == 0) {
                    Toast.makeText(getActivity(), "Couldn't find " + query, Toast.LENGTH_SHORT).show();
                    mMode = MODE_VIEW;
                    mEditSearch.setText("");
                    isSearchResultShown = false;
                } else {
                    updateFileList(fileData, FilesFragment.MODE_VIEW_SEARCH_RESULTS);
                }
            }
        }.execute();
    }

    public int getMode() {
        return mMode;
    }

    public void setMode(int mode) {
        mMode = mode;
    }

    @Override
    public void onDestroy() {
        if (mDataBaseAdapter != null) {
            mDataBaseAdapter.closeDatabase();
        }
        super.onDestroy();

    }

    public void freeResources() {
        Utils.unbindDrawables(mFileListView);
        System.gc();
    }

    public FileData getItemForMove() {
        return mItemForMove;
    }

    public void setItemForMove(FileData mItemForMove) {
        this.mItemForMove = mItemForMove;
    }

    private FileData mCurrentFileData;
    int mCurrentPosition;
    int mCurrentPositionY;
    int mViewHeight;

    @Override
    public void onItemClick(final int position, FileData fileData, int posY, int height, int mode) {
        if (mode == MODE_VIEW) {
            mCurrentFileData = fileData;
            mCurrentPosition = position;
            mCurrentPositionY = posY;
            mViewHeight = height;

            // mFileListView.setPadding(mFileListView.getPaddingLeft(), mFileListView.getPaddingTop(), mFileListView.getPaddingRight(), mFileListView.getChildAt(0).getHeight());
            showPanelDetails();
            initDetailView(position, fileData);

            mPanelDetails.invalidate();
            mPanelBgImage.invalidate();
            mPanel.invalidate();
            mFileListView.invalidate();
            mFileListAdapter.notifyDataSetInvalidated();

            // mFileListView.measure(mFileListView.getMeasuredWidth(), mFileListView.getMeasuredHeight());

            // mFileListView.measure(0, 0);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (mFileListView.getChildAt(0) != null) {
                        int itemsToEndCount = mFileListAdapter.getCount() - position;
                        int itemsToEndHeight = itemsToEndCount * mFileListView.getChildAt(0).getMeasuredHeight();
                        int listFooterHeight = mFileListView.getMeasuredHeight() - itemsToEndHeight;
                        if (listFooterHeight > 0) {
                            mListFooter.setLayoutParams(new android.widget.AbsListView.LayoutParams(0, listFooterHeight));
                            mListFooter.invalidate();
                        }
                    }
                    mFileListView.requestFocusFromTouch();
                    if (position > 0) {
                        if (mIsFroyoOrLater) {
                            if (mFileListView.getChildAt(0) != null) {
                                mFileListView.smoothScrollBy(mFileListView.getChildAt(0).getMeasuredHeight(), 1000);
                            }
                            mFileListView.setSelectionFromTop(position - 1, 0);
                        } else {
                            mFileListView.setSelectionFromTop(position, 0);
                            // mAnimationListViewTopFirst.setAnimationListener(new AnimationListener() {
                            //
                            // @Override
                            // public void onAnimationStart(Animation animation) {
                            // }
                            //
                            // @Override
                            // public void onAnimationRepeat(Animation animation) {
                            // }
                            //
                            // @Override
                            // public void onAnimationEnd(Animation animation) {
                            // mFileListView.setSelectionFromTop(position, 0);
                            // mFileListView.startAnimation(mAnimationListViewTopSecond);
                            // }
                            // });
                            // mFileListView.startAnimation(mAnimationListViewTopFirst);
                        }
                    } else {
                        if (mIsFroyoOrLater) {
                            mFileListView.smoothScrollBy(-mFileListView.getChildAt(0).getMeasuredHeight(), 1000);
                        }
                        mFileListView.setSelectionFromTop(position, 0);
                    }
                    mPanelDetails.requestFocusFromTouch();
                }
            }, 10);
        } else if (mode == MODE_MOVE_FILE) {
            onConfirmMove();
        } else if (mode == MODE_SELECT_AUTO_UPLOAD_FOLDER) {
            onSelectUploadFolder();
        }
    }

    private void onSelectUploadFolder() {
        OpenDriveApplication.setPasscodeEntered(true);
        for (final FileData fileData : mFileListAdapter.items) {
            if (fileData.isCheckedForMove()) {
                String selectedFolderName;
                if (fileData.getID().equals("0")) {
                    selectedFolderName = Utils.getDefaultAutoUploadFolderName(getActivity());
                } else {
                    selectedFolderName = mDataBaseAdapter.getFolderNameById(fileData.getID());
                }
                Utils.saveUploadFolderId(getActivity(), fileData.getID());
                Intent intent = new Intent();
                intent.putExtra(Constant.EXTRA_SELECTED_FOLDER_NAME, selectedFolderName);
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
                break;
            }
        }
    }

    private void showPanelDetails() {
        mGaTracker.sendEvent(Constant.GA_EVENT_CAT_DETAILS, Constant.GA_EVENT_ACTION_USER_VIEWED_DETAILS,"",0L);
        mPanelDetails.setVisibility(View.VISIBLE);
        mPanelBgImage.setVisibility(View.VISIBLE);
        mSubMenuDetails.setVisibility(View.GONE);
        mPanel.setVisibility(View.VISIBLE);
    }

    private void initDetailView(int position, final FileData fileData) {
        mFileNameDetail.setText(fileData.getName());
        mFileSizeDetail.setText(fileData.getSize());
        Bitmap preview = Utils.getIconFromFileID(fileData.getID(), false);
        mImageThumbDetail.setImageBitmap(preview);
        if (preview == null) {
            mImageThumbDetail.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.empty));
        } else {
            mImageThumbDetail.setBackgroundDrawable(null);
        }

        String fileFullPath;

        if (sOfflineMode) {
            fileFullPath = getItemFullPathIfOfflineMode(fileData);
        } else {
            fileFullPath = Utils.getFolderFullPath(fileData);
        }
        final File offlineFile = new File(fileFullPath);

        if (offlineFile.exists()) {
            mBtnSaveForOffline.setText(getString(R.string.btn_delete_offline));
        } else {
            mBtnSaveForOffline.setText(getString(R.string.btn_save_offline));
        }

        mImageThumbDetail.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openOrDownloadThenOpen(fileData, false);
            }
        });

        mImageThumbDetail.setOnTouchListener(new OnTouchListener() {

            private float startMoveX;
            private float xDiff;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startMoveX = event.getRawX();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    xDiff = calculateDistanceX(event);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (xDiff > 0 && xDiff >= mSlidingXDiff) {
                        navArrowLeftClickListener.onClick(null);
                        xDiff = 0;
                    } else if (xDiff < 0 && Math.abs(xDiff) >= mSlidingXDiff) {
                        navArrowRightClickListener.onClick(null);
                        xDiff = 0;
                    } else if (Math.abs(xDiff) < mSlidingXDiff) {
                        openOrDownloadThenOpen(fileData, false);
                        xDiff = 0;
                    }
                }

                return true;
            }

            private float calculateDistanceX(MotionEvent event) {
                return event.getRawX() - startMoveX;
            }
        });

    }

    View.OnClickListener menuDetailListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // mSubMenuDetails.setVisibility(View.GONE);
            // mPanel.setVisibility(View.GONE);
            // mPanelDetails.setVisibility(View.GONE);
            // mPanelBgImage.setVisibility(View.GONE);
            // mListFooter.setLayoutParams(new android.widget.AbsListView.LayoutParams(0, 0));
            // mListFooter.invalidate();

            switch (v.getId()) {

                case R.id.btnShare:
                    shareFile(mCurrentFileData);
                    break;
                case R.id.btnCopyLink:
                    String publicLink = mCurrentFileData.getDirectLinkPublic();
                    SystemUtils.copyToClipboard(getActivity(), publicLink);
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.link_copied, mCurrentFileData.getName()), Toast.LENGTH_SHORT).show();
                    mGaTracker.sendEvent(Constant.GA_EVENT_CAT_COPY_LINK, Constant.GA_EVENT_ACTION_USER_USED_COPY_LNK_BTN,"",0L);
                    break;
                case R.id.btnTrash:
                    if (sOfflineMode) {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    final FileData item = mCurrentFileData;

                    alertDialog.setIcon(R.drawable.ic_dialog_alarm);
                    alertDialog.setTitle(R.string.app_name);
                    alertDialog.setMessage(getString(R.string.msg_confirm_trashoffline, mCurrentFileData.getName()));

                    alertDialog.setPositiveButton(R.string.txt_trash, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            deleteFile(item);
                            mGaTracker.sendEvent(Constant.GA_EVENT_CAT_DELETING, Constant.GA_EVENT_ACTION_WAS_USED_TRASH_BUTTON,"",0L);
                            return;
                        }
                    });

                    alertDialog.setNegativeButton(R.string.txt_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            return;
                        }
                    });
                    alertDialog.show();
                    break;
                case R.id.btnRename:
                    renameFile(mCurrentFileData);
                    mGaTracker.sendEvent(Constant.GA_EVENT_CAT_DELETING, Constant.GA_EVENT_ACTION_WAS_USED_RENAME_BTN,"",0L);
                    break;
                case R.id.btnMoveToFolder:
                    moveFile(mCurrentFileData);
                    mGaTracker.sendEvent(Constant.GA_EVENT_CAT_MOVE_TO_FOLDER, Constant.GA_EVENT_ACTION_USER_USED_MOVE_TO_FOLDER_BTN,"",0L);

                    break;
                case R.id.btnSaveForOffline:
                    saveForOffline(mCurrentFileData);
                    break;

                case R.id.btnSendEmail:
                    sendLinkByEmail(mCurrentFileData);
                    break;

                default:
                    break;
            }

        }
    };

    private void saveForOffline(final FileData fileItem) {

        String fileFullPath;

        if (sOfflineMode) {
            fileFullPath = getItemFullPathIfOfflineMode(fileItem);
        } else {
            fileFullPath = Utils.getFolderFullPath(fileItem);
        }
        final File offlineFile = new File(fileFullPath);

        if (offlineFile.exists()) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

            alertDialog.setIcon(R.drawable.ic_dialog_alarm);
            alertDialog.setTitle(R.string.app_name);
            alertDialog.setMessage(getString(R.string.msg_confirm_deleteoffline, fileItem.getName()));

            alertDialog.setPositiveButton(R.string.txt_delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    offlineFile.delete();
                    fileItem.setDownloaded(false);
                    mFileListAdapter.notifyDataSetChanged();
                    mBtnSaveForOffline.setText(getString(R.string.btn_save_offline));
                    return;
                }
            });

            alertDialog.setNegativeButton(R.string.txt_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    return;
                }
            });
            alertDialog.show();
        } else {

            if (sOfflineMode) {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                return;
            }


            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

            alertDialog.setIcon(R.drawable.ic_dialog_alarm);
            alertDialog.setTitle(R.string.app_name);
            alertDialog.setMessage(getString(R.string.label_file_download, fileItem.getName()));

            alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    downloadFile(fileItem);
                    fileItem.setDownloaded(true);
                    mFileListAdapter.notifyDataSetChanged();
                    mBtnSaveForOffline.setText(getString(R.string.btn_delete_offline));
                    return;
                }
            });

            alertDialog.setNegativeButton(R.string.txt_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    return;
                }
            });
            alertDialog.show();

        }
    }

    private void sendLinkByEmail(FileData fileData) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_link_subject) + " " + fileData.getName());
        i.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_link_text) + " " + fileData.getName() + "\n\n" + fileData.getDirectLink());
        try {
            startActivity(Intent.createChooser(i, getString(R.string.send_mail_chooser_title)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), getString(R.string.no_email_clients), Toast.LENGTH_SHORT).show();
        }
    }

    View.OnClickListener actionBarItemClickListener = new View.OnClickListener() {

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ibHome:
                    onBtnHomeClick();
                    break;
                case R.id.ibRefresh:
                    refresh();
                    break;

                case R.id.ibNew:
                    newfolder();
                    break;

                case R.id.ibSearch:
                    mEditSearch.setText("");
                    openSearchView();
                    break;

                default:
                    break;
            }
        }

    };

    public void onBtnHomeClick() {
        mSubMenuDetails.setVisibility(View.GONE);
        mPanel.setVisibility(View.GONE);
        mPanelDetails.setVisibility(View.GONE);
        mPanelBgImage.setVisibility(View.GONE);
        mListFooter.setLayoutParams(new android.widget.AbsListView.LayoutParams(0, 0));
        mListFooter.invalidate();

        if (mMode == MODE_VIEW_SEARCH_RESULTS) {
            mMode = MODE_VIEW;
            isSearchResultShown = false;
            openRootFolder();

            rlSearchView.setVisibility(View.GONE);
            rlActionBarItems.setVisibility(View.VISIBLE);
            mTextActionBarTitle.setVisibility(View.VISIBLE);
            mEditSearch.setText("");

            if (mCurrentFolderData == null || mFolderDeep == 0) {
                mImageRootLogo.setVisibility(View.VISIBLE);
            }

        } else {
            if (mCurrentFolderData != null) {
                openRootFolder();
            }
        }
    }

    View.OnTouchListener actionBarItemTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.ibHome:
                    break;

                case R.id.ibRefresh:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        borderActiveRefresh.setVisibility(View.VISIBLE);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        borderActiveRefresh.setVisibility(View.GONE);
                    }
                    break;

                case R.id.ibNew:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        borderActiveNew.setVisibility(View.VISIBLE);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        borderActiveNew.setVisibility(View.GONE);
                    }
                    break;

                case R.id.ibSearch:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        borderActiveSearch.setVisibility(View.VISIBLE);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        borderActiveSearch.setVisibility(View.GONE);
                    }
                    break;

                default:
                    break;
            }
            return false;
        }
    };

    private void openRootFolder() {

        sOfflineMode = !Utils.isNetConeccted(getActivity());

        if (sOfflineMode && !sIsOfflineModeSwitched) {
            SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
            return;
        }

        mCurrentFolderData = null;
        mFolderDeep = 0;

        if (sOfflineMode) {
            getFileListFromDB("0");
            return;
        } else {
            mLoadingDialog.setMessage(getString(R.string.txt_loading));
            mLoadingDialog.show();
            getLoaderManager().restartLoader(0, null, loaderFromCloud);
        }
    }

    protected void openSearchView() {
        mEditSearch.requestFocus();
        mMode = MODE_VIEW_SEARCH_RESULTS;
        rlActionBarItems.setVisibility(View.GONE);
        rlSearchView.setVisibility(View.VISIBLE);
        mTextActionBarTitle.setText("");
        mTextActionBarTitle.setVisibility(View.GONE);

        if (mCurrentFolderData == null || mFolderDeep == 0) {
            if (mMode != MODE_MOVE_FILE)
                mImageRootLogo.setVisibility(View.GONE);
        }

        Utils.showSoftInput(getActivity(), mEditSearch);
    }

    View.OnTouchListener panelDetailTouchListener = new OnTouchListener() {

        private float startMoveX;
        private float xDiff;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                startMoveX = event.getRawX();
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                xDiff = calculateDistanceX(event);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (xDiff > 0 && xDiff >= mSlidingXDiff) {
                    navArrowLeftClickListener.onClick(null);
                    xDiff = 0;
                } else if (xDiff < 0 && Math.abs(xDiff) >= mSlidingXDiff) {
                    navArrowRightClickListener.onClick(null);
                    xDiff = 0;
                } else if (xDiff == 0) {
                    xDiff = 0;
                    return false;
                }
            }

            return true;
        }

        private float calculateDistanceX(MotionEvent event) {
            return event.getRawX() - startMoveX;
        }
    };

    private class UploadStartedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mIsAutoUploading = true;
            mFilesCount = intent.getIntExtra(AutoUploadService.EXTRA_FILES_COUNT, 0);
            showAutoUploadStatus(0, mFilesCount);
        }
    }

    private class UploadProgressReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mIsAutoUploading = true;
            mFilesCount = intent.getIntExtra(AutoUploadService.EXTRA_FILES_COUNT, 0);
            mFilesUploaded = intent.getIntExtra(AutoUploadService.EXTRA_FILES_UPLOADED, 0);
            showAutoUploadStatus(mFilesUploaded, mFilesCount);
        }
    }

    private class UploadFinishedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mIsAutoUploading = false;
            mFilesCount = 0;
            mFilesUploaded = 0;
            mTVAutoUploadStatus.setVisibility(View.GONE);
            refresh();
        }
    }

    private void showAutoUploadStatus(int filesUploaded, int filesCount) {
        if (!mIsAutoUploading) {
            mTVAutoUploadStatus.setVisibility(View.GONE);
            return;
        }
        if (mCurrentFolderData == null) {
            if (!mAutoUploadFolderId.equals(Utils.getDefaultAutoUploadFolderId(getActivity()))) {
                mTVAutoUploadStatus.setVisibility(View.GONE);
                return;
            }
        } else {
            if (!mCurrentFolderData.getID().equals(mAutoUploadFolderId)) {
                mTVAutoUploadStatus.setVisibility(View.GONE);
                return;
            }
        }
        mTVAutoUploadStatus.setText(getString(R.string.auto_upload) + " " + filesUploaded + "/" + filesCount);
        mTVAutoUploadStatus.setVisibility(View.VISIBLE);
    }

    private void showAutoUploadStatus() {
        showAutoUploadStatus(mFilesUploaded, mFilesCount);
    }

    private class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    }

    public void openOnlyFromDB(){
        mOpenOnlyFromDB = true;
    }
}