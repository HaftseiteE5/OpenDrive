package com.opendrive.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Window;

import com.opendrive.android.OpenDriveApplication;
import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.ui.fragment.EnterPassCodeFragment;

public class EnterPassCodeActivity extends FragmentActivity {

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }

    private FragmentTransaction mFragmentTransaction;
    private EnterPassCodeFragment mEnterPassCodeFragment;
    private boolean isBackPressed = false;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_enter_pass_code);
        mEnterPassCodeFragment = new EnterPassCodeFragment();
        mEnterPassCodeFragment.setArguments(getIntent().getExtras());

        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        mFragmentTransaction.add(R.id.enter_passcode_fragment, mEnterPassCodeFragment);
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
    public void onBackPressed() {
        if (mEnterPassCodeFragment.mMode == Constant.MODE_ENTER_PASSCODE) {
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            OpenDriveApplication.setPasscodeEntered(false);
            finish();
        } else {
            OpenDriveApplication.setPasscodeEntered(true);
            super.onBackPressed();
        }
    }

}
