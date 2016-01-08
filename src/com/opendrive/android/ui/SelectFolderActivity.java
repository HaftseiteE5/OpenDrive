package com.opendrive.android.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.opendrive.android.OpenDriveApplication;
import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.FileData;
import com.opendrive.android.ui.fragment.FilesFragment;

public class SelectFolderActivity  extends FragmentActivity implements FileItemClickListener {

    FilesFragment mFilesFragment;
    private boolean isBackPressed = false;

    BroadcastReceiver mReceiver;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.move);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            mFilesFragment = new FilesFragment();
            mFilesFragment.setMode(FilesFragment.MODE_SELECT_AUTO_UPLOAD_FOLDER);
            ft.add(R.id.container, mFilesFragment);

            ft.commit();

//        Intent intent = getIntent();
//
//        if (intent != null) {
//
//            FileData fileData = intent.getParcelableExtra("fileItem");
//            FilesFragment filesFragment = new FilesFragment();
//            filesFragment.setItemForMove(fileData);
//            filesFragment.setMode(FilesFragment.MODE_MOVE_FILE);
//            ft.add(R.id.container, filesFragment);
//
//            ft.commit();
//            /*mMode = MODE_MOVE_FILE;
//		    tvActionBarTitle.setText(getString(R.string.move_title));
//		    setItemForMove(fileItem);
//		    mFileListadapter.notifyDataSetChanged();
//		    ivRootLogo.setVisibility(View.GONE);*/
//        } else {
//            finish();
//        }


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
    public void onClick(FileData data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBottomButtonClick(int id) {
        // TODO Auto-generated method stub

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
        if (mFilesFragment != null) {
            mFilesFragment.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        PowerManager powermanager;
        powermanager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        super.onPause();
        if (!isBackPressed && !powermanager.isScreenOn()) {
            OpenDriveApplication.setPasscodeEntered(false);
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
