package com.opendrive.android.ui.dialog;

import java.util.ArrayList;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.ui.EventHandler;
import com.opendrive.android.ui.ExplorerActivity;
import com.opendrive.android.ui.MainActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

public class UploadDialog extends DialogFragment{

    private Tracker mGaTracker;
    private GoogleAnalytics mGaInstance;

    private int REQUEST_GALLERY = 0;
    private int REQUEST_EXPLORER = 1;
    
    private Button btnGalleryFiles;
    private Button btnOtherFiles;
    
    public static interface OnUploadFilesListener {
	public void onUploadGalleryFiles();
	public void onUploadOtherFiles();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGaInstance = GoogleAnalytics.getInstance(getActivity());
        mGaTracker = mGaInstance.getTracker(Constant.GA_ID);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGaTracker.sendView(Constant.GA_UPLOAD_VIEW);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	View view = inflater.inflate(R.layout.dialog_uploadfile, container);
	getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
	getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
	
	btnGalleryFiles = (Button) view.findViewById(R.id.btnGalleryFiles);
	btnOtherFiles = (Button) view.findViewById(R.id.btnOtherFiles);
	
	btnGalleryFiles.setOnClickListener(new View.OnClickListener() {
	    
	    @Override
	    public void onClick(View v) {
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		getActivity().startActivityForResult(intent, REQUEST_GALLERY);
		dismiss();
	    }
	});
	
	btnOtherFiles.setOnClickListener(new View.OnClickListener() {
	    
	    @Override
	    public void onClick(View v) {
		if (EventHandler.mMultiSelectData == null)
		    EventHandler.mMultiSelectData = new ArrayList<String>();

		MainActivity.m_bLoading = true;
		EventHandler.mMultiSelectData.clear();
		Intent intent = new Intent(getActivity(), ExplorerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		getActivity().startActivityForResult(intent, REQUEST_EXPLORER);
		//startActivity(intent);
		dismiss();
	    }
	});
	
	return view;
    }
    
    

}
