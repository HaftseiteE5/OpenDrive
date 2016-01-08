package com.opendrive.android.ui.fragment;

import com.opendrive.android.R;
import com.opendrive.android.common.Utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PlanFragment extends Fragment{

    private RelativeLayout rlActionBarItems;
    private ImageButton ibHome;
    private TextView tvActionBarTitle;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	final View v = inflater.inflate(R.layout.fragment_plan, container, false);
	
	tvActionBarTitle = (TextView) v.findViewById(R.id.tvActionBarTitle);
	tvActionBarTitle.setText(getString(R.string.plan_title));
	rlActionBarItems = (RelativeLayout) v.findViewById(R.id.rlActionBarItems);
	ibHome = (ImageButton) v.findViewById(R.id.ibHome);
	rlActionBarItems.setVisibility(View.GONE);
	ibHome.setVisibility(View.GONE);
	
	Utils.overrideFonts(getActivity(), v);
	
	return v;
    }

}
