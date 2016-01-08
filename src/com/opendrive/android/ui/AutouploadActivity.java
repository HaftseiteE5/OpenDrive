package com.opendrive.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.opendrive.android.OpenDriveApplication;
import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.ui.fragment.AutoUploadFragment;

import javax.microedition.khronos.opengles.GL10;

public class AutouploadActivity extends FragmentActivity {

    private FragmentTransaction mFragmentTransaction;
    private AutoUploadFragment mAutoUploadFragment;
    private boolean isBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autoupload);
        mAutoUploadFragment = new AutoUploadFragment();

        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        mFragmentTransaction.add(R.id.autoupload_fragment, mAutoUploadFragment);
        mFragmentTransaction.commit();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.REQUEST_CODE_SELECT_FOLDER:
                String folderName = null;
                if(data != null) {
                    folderName = data.getStringExtra(Constant.EXTRA_SELECTED_FOLDER_NAME);
                }
                if(folderName!=null) {
                    mAutoUploadFragment.setFolderName(folderName);
                }
                break;
            case Constant.PASSCODE_CHECK_REQUEST_CODE:
                if (resultCode == RESULT_CANCELED) {
                    finish();
                } else if (resultCode == RESULT_OK) {
                    OpenDriveApplication.setPasscodeEntered(true);
                }
            break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (Utils.getPassCodeTurned(this) && !OpenDriveApplication.isPasscodeEntered()) {
            Intent intent = new Intent(this, EnterPassCodeActivity.class);
            intent.putExtra(Constant.ENTER_PASS_CODE_MODE, Constant.MODE_ENTER_PASSCODE);
            startActivityForResult(intent, Constant.PASSCODE_CHECK_REQUEST_CODE);
        }
    }

    @Override
    protected void onResume() {
        OpenDriveApplication.applicationResumed();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        isBackPressed = true;
        OpenDriveApplication.setPasscodeEntered(true);
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isBackPressed) {
            OpenDriveApplication.setPasscodeEntered(false);
        }
    }
}
