package com.opendrive.android.ui.fragment;

import org.jraf.android.backport.switchwidget.Switch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.ui.EnterPassCodeActivity;

public class PassCodeFragment extends Fragment {

    private SharedPreferences mPreferences;

    private Tracker mGaTracker;
    private GoogleAnalytics mGaInstance;

    private RelativeLayout btnTurnPassCode;
    private TextView tvTurnPassCode;
    private RelativeLayout btnChangePassCode;
    private org.jraf.android.backport.switchwidget.Switch switchEraseData;

    private RelativeLayout rlActionBarItems;
    private ImageButton ibHome;
    private TextView tvActionBarTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGaInstance = GoogleAnalytics.getInstance(getActivity());
        mGaTracker = mGaInstance.getTracker(Constant.GA_ID);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGaTracker.sendView(Constant.GA_PASS_CODE_SETTINGS_VIEW);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	final View v = inflater.inflate(R.layout.fragment_passcode, container, false);

	tvActionBarTitle = (TextView) v.findViewById(R.id.tvActionBarTitle);
	tvActionBarTitle.setText(getString(R.string.passcode_title));
	rlActionBarItems = (RelativeLayout) v.findViewById(R.id.rlActionBarItems);
	ibHome = (ImageButton) v.findViewById(R.id.ibHome);
	rlActionBarItems.setVisibility(View.GONE);
	ibHome.setVisibility(View.GONE);

	mPreferences = getActivity().getSharedPreferences(Constant.PREFS_NAME, 0);
	btnTurnPassCode = (RelativeLayout) v.findViewById(R.id.rlTurnPassCode);
	btnTurnPassCode.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		Intent intent = new Intent(getActivity(), EnterPassCodeActivity.class);

		if (Utils.getPassCodeTurned(getActivity())) {
		    intent.putExtra(Constant.ENTER_PASS_CODE_MODE, Constant.MODE_TURN_PASSCODE_OFF);
		} else {
		    intent.putExtra(Constant.ENTER_PASS_CODE_MODE, Constant.MODE_SET_NEW_PASSCODE);
		}

		startActivity(intent);
	    }
	});

    tvTurnPassCode = (TextView) v.findViewById(R.id.tvTurnPassCode);

	btnChangePassCode = (RelativeLayout) v.findViewById(R.id.rlChangePasscode);

	btnChangePassCode.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		Intent intent = new Intent(getActivity(), EnterPassCodeActivity.class);
		intent.putExtra(Constant.ENTER_PASS_CODE_MODE, Constant.MODE_CHANGE_PASSCODE);
		startActivity(intent);

	    }
	});

	switchEraseData = (Switch) v.findViewById(R.id.switchEraseData);
	switchEraseData.setChecked(Utils.getEraseDataTurned(getActivity()));

	switchEraseData.setOnCheckedChangeListener(new OnCheckedChangeListener() {

	    @Override
	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Utils.saveEraseDataTurned(getActivity(), isChecked);
	    }
	});

	Utils.overrideFonts(getActivity(), v);
    Typeface typefaceRobotoBold = Utils.getTypeface(getActivity(), "fonts/roboto_bold.ttf");
    tvTurnPassCode.setTypeface(typefaceRobotoBold);
        ((TextView)v.findViewById(R.id.tvEraseData)).setTypeface(typefaceRobotoBold);
        ((TextView)v.findViewById(R.id.tvChangePasscode)).setTypeface(typefaceRobotoBold);

	
	return v;
    }

    @Override
    public void onResume() {
	super.onResume();
	checkIsPasscodeTurned();
    }

    private void checkIsPasscodeTurned() {
	if (Utils.getPassCodeTurned(getActivity())) {
	    tvTurnPassCode.setText(getActivity().getResources().getString(R.string.turn_pass_code_off));
	    btnChangePassCode.setEnabled(true);
	} else {
	    tvTurnPassCode.setText(getActivity().getResources().getString(R.string.turn_pass_code_on));
	    btnChangePassCode.setEnabled(false);
	}

    }

}
