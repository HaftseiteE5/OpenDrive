package com.opendrive.android.ui.fragment;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opendrive.android.OpenDriveApplication;
import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;

public class EnterPassCodeFragment extends Fragment {

    private SharedPreferences mPreferences;

    public int mMode;

    private TextView tvEnterPassCode;
    private EditText etPass1;
    private EditText etPass2;
    private EditText etPass3;
    private EditText etPass4;

    private boolean mFirstInput = true;
    private String mFirstPass = "";
    private String mSecondPass = "";

    private int mTriesCount = 0;
    private boolean mIsEraseDataEnabled;

    private RelativeLayout rlActionBarItems;
    private ImageButton ibHome;
    private TextView tvActionBarTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	final View v = inflater.inflate(R.layout.fragment_enter_pass_code, container, false);
	mMode = getArguments().getInt(Constant.ENTER_PASS_CODE_MODE, Constant.MODE_SET_NEW_PASSCODE);

	mPreferences = getActivity().getSharedPreferences(Constant.PREFS_NAME, 0);
	mIsEraseDataEnabled = Utils.getEraseDataTurned(getActivity());

	tvEnterPassCode = (TextView) v.findViewById(R.id.tvEnterPassCode);
	if (mMode == Constant.MODE_CHANGE_PASSCODE) {
	    tvEnterPassCode.setText(getString(R.string.enter_your_old_pass_code));
	} else {
	    tvEnterPassCode.setText(Html.fromHtml(getString(R.string.enter_your_pass_code)));
	}

	tvActionBarTitle = (TextView) v.findViewById(R.id.tvActionBarTitle);
	tvActionBarTitle.setText(getString(R.string.enter_passcode_title));
	rlActionBarItems = (RelativeLayout) v.findViewById(R.id.rlActionBarItems);
	ibHome = (ImageButton) v.findViewById(R.id.ibHome);
	rlActionBarItems.setVisibility(View.GONE);
	ibHome.setVisibility(View.GONE);

	etPass1 = (EditText) v.findViewById(R.id.etPass1);
	etPass1.requestFocus();
	etPass2 = (EditText) v.findViewById(R.id.etPass2);
	etPass3 = (EditText) v.findViewById(R.id.etPass3);
	etPass4 = (EditText) v.findViewById(R.id.etPass4);

	etPass1.addTextChangedListener(new TextWatcher() {

	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (etPass1.getText().toString().length() == 1) {
		    Handler handler = new Handler();
		    handler.postDelayed(new Runnable() {
			public void run() {
			    etPass2.requestFocus();
			}
		    }, 10);
		}
	    }

	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	    }

	    @Override
	    public void afterTextChanged(Editable s) {
	    }
	});

	etPass2.addTextChangedListener(new TextWatcher() {

	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (etPass2.getText().toString().length() == 1) {
		    Handler handler = new Handler();
		    handler.postDelayed(new Runnable() {
			public void run() {
			    etPass3.requestFocus();
			}
		    }, 10);
		} else {
		    etPass1.requestFocus();
		}
	    }

	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	    }

	    @Override
	    public void afterTextChanged(Editable s) {
	    }
	});

	etPass3.addTextChangedListener(new TextWatcher() {

	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (etPass3.getText().toString().length() == 1) {
		    Handler handler = new Handler();
		    handler.postDelayed(new Runnable() {
			public void run() {
			    etPass4.requestFocus();
			}
		    }, 10);
		} else {
		    etPass2.requestFocus();
		}
	    }

	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	    }

	    @Override
	    public void afterTextChanged(Editable s) {
	    }
	});

	etPass4.addTextChangedListener(new TextWatcher() {

	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (etPass4.getText().toString().length() == 1) {
		    switch (mMode) {

		    case Constant.MODE_ENTER_PASSCODE:
			onEnterPasscodeMode();
			break;

		    case Constant.MODE_SET_NEW_PASSCODE:
			onSetNewPasscodeMode();
			break;

		    case Constant.MODE_TURN_PASSCODE_OFF:
			onTurnPasscodeOffMode();
			break;

		    case Constant.MODE_CHANGE_PASSCODE:
			onChangePasscodeMode();
			break;

		    default:
			break;
		    }
		} else {
		    etPass3.requestFocus();
		}
	    }

	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	    }

	    @Override
	    public void afterTextChanged(Editable s) {
	    }
	});

	Utils.overrideFonts(getActivity(), v);

	return v;
    }

    @Override
    public void onResume() {
	super.onResume();
	if (getActivity() != null) {
	    etPass1.postDelayed(new Runnable() {
		@Override
		public void run() {
		    Utils.showSoftInput(getActivity(), etPass1);
		}
	    }, 200);
	}
    }

    private void onEnterPasscodeMode() {
	if (isPassFull()) {
	    mFirstPass = getStringFromEditTexts();
	    if (mFirstPass.equals(Utils.getPassCode(getActivity()))) {
		Intent returnToSplashIntent = new Intent();
		getActivity().setResult(Activity.RESULT_OK, returnToSplashIntent);
		OpenDriveApplication.setPasscodeEntered(true);
		getActivity().finish();
	    } else {
		if (mIsEraseDataEnabled && ++mTriesCount == 10) {
		    new Thread(new Runnable() {

			@Override
			public void run() {
			    Utils.deleteFilesAndDB(getActivity());
			}
		    }).start();
		    Toast.makeText(getActivity(), getString(R.string.all_content_erased), Toast.LENGTH_LONG).show();
		    getActivity().finish();
		}
		mFirstPass = "";
		mSecondPass = "";
		mFirstInput = true;
		clearAllEditTexts();
		etPass1.requestFocus();
		Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.passcodes_not_correct), Toast.LENGTH_SHORT).show();
	    }
	}
    }

    private void onChangePasscodeMode() {
	if (isPassFull()) {
	    mFirstPass = getStringFromEditTexts();
	    if (mFirstPass.equals(Utils.getPassCode(getActivity()))) {
		mMode = Constant.MODE_SET_NEW_PASSCODE;
		tvEnterPassCode.setText(Html.fromHtml(getString(R.string.enter_your_pass_code)));
		mFirstPass = "";
		mSecondPass = "";
		mFirstInput = true;
		clearAllEditTexts();
		etPass1.requestFocus();

	    } else {
		mFirstPass = "";
		mSecondPass = "";
		mFirstInput = true;
		clearAllEditTexts();
		etPass1.requestFocus();
		Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.passcodes_not_correct), Toast.LENGTH_SHORT).show();
	    }
	}
    }

    private void onTurnPasscodeOffMode() {
	if (isPassFull()) {
	    mFirstPass = getStringFromEditTexts();
	    if (mFirstPass.equals(Utils.getPassCode(getActivity()))) {
        Utils.savePassCode(getActivity(), "");
		Utils.savePassCodeTurned(getActivity(),false);
		OpenDriveApplication.setPasscodeEntered(true);
		getActivity().finish();
	    } else {
		mFirstPass = "";
		mSecondPass = "";
		mFirstInput = true;
		clearAllEditTexts();
		etPass1.requestFocus();
		Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.passcodes_not_correct), Toast.LENGTH_SHORT).show();
	    }
	}
    }

    private void onSetNewPasscodeMode() {
	if (mFirstInput && isPassFull()) {
	    mFirstPass = getStringFromEditTexts();
	    mFirstInput = false;

	    Handler handler = new Handler();
	    handler.postDelayed(new Runnable() {
		public void run() {
		    clearAllEditTexts();
		    etPass1.requestFocus();
		}
	    }, 200);
	} else if (isPassFull()) {
	    mSecondPass = getStringFromEditTexts();
	    if (mFirstPass.equals(mSecondPass)) {
		Utils.savePassCode(getActivity(), mFirstPass);
		Utils.savePassCodeTurned(getActivity(),true);
		OpenDriveApplication.setPasscodeEntered(true);
		getActivity().finish();
	    } else {
		mFirstPass = "";
		mSecondPass = "";
		mFirstInput = true;
		clearAllEditTexts();
		etPass1.requestFocus();
		Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.passcodes_not_match), Toast.LENGTH_SHORT).show();
	    }
	}
    }

    private String getStringFromEditTexts() {
	StringBuilder sb = new StringBuilder();
	sb.append(etPass1.getText().toString());
	sb.append(etPass2.getText().toString());
	sb.append(etPass3.getText().toString());
	sb.append(etPass4.getText().toString());
	return sb.toString();
    }

    private void clearAllEditTexts() {
	etPass1.setText("");
	etPass2.setText("");
	etPass3.setText("");
	etPass4.setText("");
    }

    private boolean isPassFull() {
	return etPass1.getText().toString().length() > 0 && etPass2.getText().toString().length() > 0 && etPass3.getText().toString().length() > 0 && etPass4.getText().toString().length() > 0;
    }

}
