package com.opendrive.android.ui.fragment;

import java.io.File;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.StorageInfo;
import com.opendrive.android.parser.StorageInfoParser;
import com.opendrive.android.request.Request;
import com.opendrive.android.ui.AutouploadActivity;
import com.opendrive.android.ui.HomeBtnClickListener;
import com.opendrive.android.ui.LogIn;
import com.opendrive.android.ui.PassCodeActivity;

public class SettingsFragment extends Fragment {

    private Tracker mGaTracker;
    private GoogleAnalytics mGaInstance;

    private SharedPreferences settingPrefer;
    private RelativeLayout btnDeleteOfflineFiles = null;
    private RelativeLayout btnLogOut = null;
    private RelativeLayout btnTellFriend = null;

    private StorageInfoParser mStorageInfoParser = null;
    private TextView mUserName = null;
    private TextView mStorageInfo = null;
    private Button mPlanInfo = null;

    private org.jraf.android.backport.switchwidget.Switch switchKeepLoggedIn;
    private Button btnPassCode;
    private Button btnAutoupload;

    private AlertDialog.Builder validationAlertDialog = null;

    private RelativeLayout rlActionBarItems;
    private RelativeLayout rlSearchView;
    private ImageButton ibHome;
    private ImageButton ibSearch;
    private ImageButton ibNew;
    private ImageButton ibRefresh;
    private ImageButton ibGoSearch;
    private EditText etSearchView;
    private TextView tvActionBarTitle;

    private TextView userTextView;
    private TextView spaceTextView;
    private TextView planTextView;
    private TextView tvKeepLoggedIn;
    private TextView tvPassCode;
    private TextView tvBtnTellFriend;
    private TextView tvBtnLogOut;
    private TextView tvBtnDeleteOfflineFiles;
    private TextView tvAppVersion;
    private TextView tvAutoUpload;

    private static boolean isFragmentVisible = false;

    public boolean isFragmentVisible() {
        return isFragmentVisible;
    }

    private HomeBtnClickListener mHomeBtnClickListener;

    /*
     * @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) { inflater.inflate(R.menu.menu, menu); menu.findItem(R.id.menu_search).setVisible(false); menu.findItem(R.id.menu_new_folder).setVisible(false); }
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGaInstance = GoogleAnalytics.getInstance(getActivity());
        mGaTracker = mGaInstance.getTracker(Constant.GA_ID);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGaTracker.sendView(Constant.GA_SETTINGS_VIEW);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            mGaTracker.sendEvent(Constant.GA_EVENT_CAT_ROTATING, Constant.GA_EVENT_ACTION_ROTATE_LANDSCAPE_SETTINGS,"",0L);
        }else{
            mGaTracker.sendEvent(Constant.GA_EVENT_CAT_ROTATING, Constant.GA_EVENT_ACTION_ROTATE_PORTRAIT_SETTINGS,"",0L);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.setting, container, false);

        settingPrefer = getActivity().getSharedPreferences(Constant.PREFS_NAME, 0);

        initValidationAlertDialog();

        mUserName = (TextView) v.findViewById(R.id.userEditText);
        mStorageInfo = (TextView) v.findViewById(R.id.spaceEditText);
        mPlanInfo = (Button) v.findViewById(R.id.btnPlan);

        if (Constant.UserName != null) {
            mUserName.setText(Constant.UserName);
        }

        if (LogIn.mLoginData != null && LogIn.mLoginData.getUserLevel() != null) {
            mPlanInfo.setText(LogIn.mLoginData.getUserLevel());
        }

        // mPlanInfo.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // Intent intent = new Intent(getActivity(), PlanActivity.class);
        // startActivity(intent);
        // }
        // });

        switchKeepLoggedIn = (org.jraf.android.backport.switchwidget.Switch) v.findViewById(R.id.switchKeepLoggedIn);
        switchKeepLoggedIn.setChecked(Utils.getKeepLoggedIn(getActivity()));

        switchKeepLoggedIn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Utils.saveKeepLoggedIn(getActivity(), isChecked);
            }
        });

        btnPassCode = (Button) v.findViewById(R.id.btnPassCode);
        btnPassCode.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PassCodeActivity.class);
                startActivity(intent);
            }
        });

        btnAutoupload = (Button) v.findViewById(R.id.btnAutoUpload);
        btnAutoupload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AutouploadActivity.class);
                startActivity(intent);
            }
        });

        btnDeleteOfflineFiles = (RelativeLayout) v.findViewById(R.id.rlDeleteOffline);
        btnDeleteOfflineFiles.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showValidationAlertDialog(getString(R.string.delete_db));
            }
        });

        btnTellFriend = (RelativeLayout) v.findViewById(R.id.rlTellFriend);
        btnTellFriend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                tellFriend();

            }
        });

        if (checkExistDeleteFiles()) {
            btnDeleteOfflineFiles.setEnabled(true);
        } else {
            btnDeleteOfflineFiles.setEnabled(false);
        }

        btnLogOut = (RelativeLayout) v.findViewById(R.id.rlLogOut);
        btnLogOut.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                logOut();
            }
        });

        tvActionBarTitle = (TextView) v.findViewById(R.id.tvActionBarTitle);
        tvActionBarTitle.setText(getString(R.string.setting_title));
        rlActionBarItems = (RelativeLayout) v.findViewById(R.id.rlActionBarItems);
        rlSearchView = (RelativeLayout) v.findViewById(R.id.rlSearchView);
        ibHome = (ImageButton) v.findViewById(R.id.ibHome);
        ibSearch = (ImageButton) v.findViewById(R.id.ibSearch);
        ibNew = (ImageButton) v.findViewById(R.id.ibNew);
        ibRefresh = (ImageButton) v.findViewById(R.id.ibRefresh);
        ibGoSearch = (ImageButton) v.findViewById(R.id.ibGoSearch);
        etSearchView = (EditText) v.findViewById(R.id.etSearchView);

        ibHome.setOnClickListener(actionBarItemClickListener);
        ibSearch.setOnClickListener(actionBarItemClickListener);
        ibNew.setOnClickListener(actionBarItemClickListener);
        ibRefresh.setOnClickListener(actionBarItemClickListener);

        ibGoSearch.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });

        rlActionBarItems.setVisibility(View.GONE);

        getStorageInfo();

        setHasOptionsMenu(true);

        Utils.overrideFonts(getActivity(), v);

        userTextView = (TextView) v.findViewById(R.id.userTextView);
        spaceTextView = (TextView) v.findViewById(R.id.spaceTextView);
        planTextView = (TextView) v.findViewById(R.id.planTextView);
        tvKeepLoggedIn = (TextView) v.findViewById(R.id.tvKeepLoggedIn);
        tvPassCode = (TextView) v.findViewById(R.id.tvPassCode);
        tvBtnTellFriend = (TextView) v.findViewById(R.id.tvBtnTellFriend);
        tvBtnLogOut = (TextView) v.findViewById(R.id.tvBtnLogOut);
        tvBtnDeleteOfflineFiles = (TextView) v.findViewById(R.id.tvBtnDeleteOfflineFiles);
        tvAppVersion = (TextView) v.findViewById(R.id.tvAppVersion);
        tvAppVersion.setText(getString(R.string.app_version_suffix) + " " + Constant.AppVersion);
        tvAutoUpload = (TextView) v.findViewById(R.id.tvAutoUpload);

        Typeface typefaceRobotoBold = Utils.getTypeface(getActivity(), "fonts/roboto_bold.ttf");
        userTextView.setTypeface(typefaceRobotoBold);
        spaceTextView.setTypeface(typefaceRobotoBold);
        planTextView.setTypeface(typefaceRobotoBold);
        tvKeepLoggedIn.setTypeface(typefaceRobotoBold);
        tvPassCode.setTypeface(typefaceRobotoBold);
        tvBtnTellFriend.setTypeface(typefaceRobotoBold);
        tvBtnLogOut.setTypeface(typefaceRobotoBold);
        tvBtnDeleteOfflineFiles.setTypeface(typefaceRobotoBold);
        tvAutoUpload.setTypeface(typefaceRobotoBold);

        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        isFragmentVisible = false;
    }

    View.OnClickListener actionBarItemClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.ibHome:
                    ((HomeBtnClickListener) getActivity()).onHomeBtnClick();
                    break;

                case R.id.ibRefresh:
                    break;

                case R.id.ibNew:
                    break;

                case R.id.ibSearch:
                    break;

                default:
                    break;
            }
        }

    };

    @Override
    public void onResume() {
        super.onResume();
        isFragmentVisible = true;
        boolean isPassCodeOn = Utils.getPassCodeTurned(getActivity());
        boolean isAutoUploadEnabled = Utils.getAutoUploadIsEnabled(getActivity());
        if (isPassCodeOn) {
            btnPassCode.setText(getActivity().getResources().getString(R.string.on));
        } else {
            btnPassCode.setText(getActivity().getResources().getString(R.string.off));
        }
        if (isAutoUploadEnabled) {
            btnAutoupload.setText(getActivity().getResources().getString(R.string.on));
        } else {
            btnAutoupload.setText(getActivity().getResources().getString(R.string.off));
        }
    }

    public void getStorageInfo() {

        new Thread(new Runnable() {
            public void run() {

                try {
                    Thread.sleep(100);

                    Request loginRequest = new Request();

                    String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;

                    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                    postData.add(new BasicNameValuePair("action", "get_storage_info"));
                    postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                    String resultXML = loginRequest.httpPost(loginUrlString, postData);
                    if (resultXML.equals(Constant.ErrorMessage)) {
                        LosSessionHandler.sendMessage(LosSessionHandler.obtainMessage());
                        return;
                    } else {

                        mStorageInfoParser = new StorageInfoParser();

                        final StorageInfo storageInfo = mStorageInfoParser.parseResponse(resultXML);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String suffix = "";
                                if (storageInfo.getSpaceType() == Constant.SPACE_USED) {
                                    suffix = " " + getString(R.string.used_suffix);
                                }
                                mStorageInfo.setText(Utils.getStringFileSize(Long.parseLong(storageInfo.getSpace())) + suffix);
                            }
                        });
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    Handler LosSessionHandler = new Handler() {
        public void handleMessage(Message msg) {
            logOut();
        }
    };

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
            validationAlertDialog = new AlertDialog.Builder(getActivity());
        validationAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
        validationAlertDialog.setTitle(R.string.txt_warning);
        validationAlertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        Utils.deleteFilesAndDB(getActivity());
                    }
                }).start();
                btnDeleteOfflineFiles.setEnabled(false);
                return;
            }
        });
        validationAlertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

                return;
            }
        });

    }

    public void showValidationAlertDialog(String errorString) {
        validationAlertDialog.setMessage(errorString);
        validationAlertDialog.show();
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
            Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void logOut() {
        settingPrefer = getActivity().getSharedPreferences(Constant.PREFS_NAME, 0);
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
        getActivity().finish();

    }

    public void showLoginActivity() {

        Intent intent = new Intent(getActivity(), LogIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    void saveInfo(String key, String Info) {
        settingPrefer = getActivity().getSharedPreferences(Constant.PREFS_NAME, 0);
        final SharedPreferences.Editor editor = settingPrefer.edit();
        editor.putString(key, Info);
        editor.commit();
    }

    void loadInfoe() {
        // sadfsd
    }

}
