package com.opendrive.android.ui.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.db.DataBaseAdapter;
import com.opendrive.android.ui.SelectFolderActivity;

import org.jraf.android.backport.switchwidget.Switch;

public class AutoUploadFragment extends Fragment {

    private Tracker mGaTracker;
    private GoogleAnalytics mGaInstance;

    private SharedPreferences settingPrefer;
    private Switch switchAutoUpload;
    private RelativeLayout rlSelectFolder;
    private RadioButton rbWiFiOnly;
    private RadioButton rbWiFiCell;
    private TextView tvFolder;

    private RelativeLayout rlActionBarItems;
    private ImageButton ibHome;
    private TextView tvActionBarTitle;

    private DataBaseAdapter mDataBaseAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBaseAdapter = new DataBaseAdapter(getActivity());
        mDataBaseAdapter.openDatabase();
        mGaInstance = GoogleAnalytics.getInstance(getActivity());
        mGaTracker = mGaInstance.getTracker(Constant.GA_ID);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGaTracker.sendView(Constant.GA_AUTO_UPLOAD_SETTINGS_VIEW);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_auto_upload, container, false);
        settingPrefer = getActivity().getSharedPreferences(Constant.PREFS_NAME, 0);

        tvActionBarTitle = (TextView) v.findViewById(R.id.tvActionBarTitle);
        tvActionBarTitle.setText(getString(R.string.auto_upload));
        rlActionBarItems = (RelativeLayout) v.findViewById(R.id.rlActionBarItems);
        ibHome = (ImageButton) v.findViewById(R.id.ibHome);
        rlActionBarItems.setVisibility(View.GONE);
        ibHome.setVisibility(View.GONE);

        switchAutoUpload = (Switch) v.findViewById(R.id.switchAutoUpload);
        switchAutoUpload.setChecked(Utils.getAutoUploadIsEnabled(getActivity()));
        switchAutoUpload.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Utils.saveAutoUploadIsEnabled(getActivity(), isChecked);
                Utils.saveLastUploadedPhotoId(getActivity(), Utils.getLastDevicePhotoId(getActivity()));
                Utils.saveLastUploadedVideoId(getActivity(), Utils.getLastDeviceVideoId(getActivity()));
            }
        });

        rlSelectFolder = (RelativeLayout) v.findViewById(R.id.rlSelectFolder);
        rlSelectFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivityForResult(new Intent(getActivity(), SelectFolderActivity.class), Constant.REQUEST_CODE_SELECT_FOLDER);
            }
        });

        rbWiFiOnly = (RadioButton) v.findViewById(R.id.rbWiFiOnly);
        rbWiFiCell = (RadioButton) v.findViewById(R.id.rbWiFiCell);

        int option = Utils.getAutoUploadOption(getActivity());
        switch (option) {
            case Constant.WI_FI_ONLY:
                rbWiFiOnly.setChecked(true);
                rbWiFiCell.setChecked(false);
                break;
            case Constant.WI_FI_CELL:
                rbWiFiOnly.setChecked(false);
                rbWiFiCell.setChecked(true);
                break;
        }

        tvFolder = (TextView) v.findViewById(R.id.tvFolder);
        String uploadFolderId = Utils.getUploadFolderId(getActivity());
        if (uploadFolderId != null) {
            if (!uploadFolderId.equals(Utils.getDefaultAutoUploadFolderId(getActivity()))) {
                tvFolder.setText(mDataBaseAdapter.getFolderNameById(uploadFolderId));
            } else {
                tvFolder.setText(Utils.getDefaultAutoUploadFolderName(getActivity()));
            }
        }

        Utils.overrideFonts(getActivity(), v);
        Typeface typefaceRobotoBold = Utils.getTypeface(getActivity(), "fonts/roboto_bold.ttf");
        ((TextView)v.findViewById(R.id.tvAutoUpload)).setTypeface(typefaceRobotoBold);
        ((TextView)v.findViewById(R.id.tvFolder)).setTypeface(typefaceRobotoBold);
        ((RadioButton)v.findViewById(R.id.rbWiFiOnly)).setTypeface(typefaceRobotoBold);
        ((RadioButton)v.findViewById(R.id.rbWiFiCell)).setTypeface(typefaceRobotoBold);



        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (rbWiFiOnly.isChecked()) {
            Utils.saveAutoUploadOption(getActivity(), Constant.WI_FI_ONLY);
        } else {
            Utils.saveAutoUploadOption(getActivity(), Constant.WI_FI_CELL);
        }
    }

    public void setFolderName(String name) {
        tvFolder.setText(name);
        tvFolder.invalidate();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.REQUEST_CODE_SELECT_FOLDER:
                String folderName = data.getStringExtra(Constant.EXTRA_SELECTED_FOLDER_NAME);
                if (folderName != null) {
                    setFolderName(folderName);
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDataBaseAdapter != null) {
            mDataBaseAdapter.closeDatabase();
        }
    }
}
