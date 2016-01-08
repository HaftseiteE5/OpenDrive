package com.opendrive.android.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.opendrive.android.OpenDriveApplication;
import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.FileData;
import com.opendrive.android.ui.fragment.FilesFragment;

public class MoveActivity extends FragmentActivity implements FileItemClickListener {

    FilesFragment mFilesFragment;
    private boolean isBackPressed = false;
    private static boolean mWasScreenOff = false;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.move);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        Intent intent = getIntent();


        if (intent != null) {

            FileData fileData = intent.getParcelableExtra("fileItem");
            mFilesFragment = new FilesFragment();
            mFilesFragment.setItemForMove(fileData);
            mFilesFragment.setMode(FilesFragment.MODE_MOVE_FILE);
            ft.add(R.id.container, mFilesFragment);

            ft.commit();
            /*mMode = MODE_MOVE_FILE;
		    tvActionBarTitle.setText(getString(R.string.move_title));
		    setItemForMove(fileItem);
		    mFileListadapter.notifyDataSetChanged();
		    ivRootLogo.setVisibility(View.GONE);*/
        } else {
            finish();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(MainActivity.mFileUploaded) {
            sendBroadcast(new Intent(Constant.ACTION_REFRESH));
        }
    }

    @Override
    public void onClick(FileData data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBottomButtonClick(int id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBackPressed() {
        isBackPressed = true;
        OpenDriveApplication.setPasscodeEntered(true);
        if (mFilesFragment != null) {
            mFilesFragment.onBackPressed();
        } else {
            super.onBackPressed();
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
        mWasScreenOff = false;
        OpenDriveApplication.applicationResumed();
        super.onResume();
    }

    @Override
    protected void onPause() {
        PowerManager powermanager;
        powermanager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        super.onPause();
        if (!isBackPressed && !powermanager.isScreenOn()) {
            OpenDriveApplication.setPasscodeEntered(false);
        }
        else {
            OpenDriveApplication.setPasscodeEntered(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.PASSCODE_CHECK_REQUEST_CODE) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            } else if (resultCode == RESULT_OK) {
                OpenDriveApplication.setPasscodeEntered(true);
            }
        }
    }

}
