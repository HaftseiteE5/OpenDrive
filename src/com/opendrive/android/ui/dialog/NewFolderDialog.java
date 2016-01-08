package com.opendrive.android.ui.dialog;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.datamodel.FileData;
import com.opendrive.android.request.Request;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class NewFolderDialog extends DialogFragment {

    public static interface onCreateFolderListener {
	public void onCreateFolder();
    }
    
    private onCreateFolderListener mOnCreateFolderListener;
    
    public void setOnCreateFolderListener(onCreateFolderListener onCreateFolderListener) {
        mOnCreateFolderListener = onCreateFolderListener;
    }

    static public FileData mCurrentFolder = null;

    private ProgressDialog LoadingDialog;
    private AlertDialog.Builder validationAlertDialog = null;

    private EditText etNewFolderName;
    private Button btnNewFolderCancel;
    private Button btnNewFolderOk;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	View view = inflater.inflate(R.layout.dialog_new_folder, null, false);
	getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
	getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
	initLoadingDialog();
	initValidationAlertDialog();
	
	setRetainInstance(true);
	
	etNewFolderName = (EditText) view.findViewById(R.id.etNewFolderName);
	btnNewFolderCancel = (Button) view.findViewById(R.id.btnNewFolderCancel);
	btnNewFolderOk = (Button) view.findViewById(R.id.btnNewFolderOk);

	btnNewFolderCancel.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		dismiss();
	    }
	});

	btnNewFolderOk.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		onOK();
	    }
	});

	return view;

    }

    void onOK() {
	if (etNewFolderName.getText().toString().trim().length() == 0) {
	    NeweFolderMessageHandler.sendMessage(NeweFolderMessageHandler.obtainMessage());
	    return;
	}

	LoadingDialog.show();
	new Thread(new Runnable() {
	    public void run() {

		try {
		    Thread.sleep(100);

		    String resultXML = "";
		    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
		    Request getFileListRequest = new Request();

		    String getFilListUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
		    postData = new ArrayList<NameValuePair>();

		    postData.add(new BasicNameValuePair("action", "create_dir"));

		    if (mCurrentFolder == null) {
			postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
			postData.add(new BasicNameValuePair("share_user_id", ""));
			postData.add(new BasicNameValuePair("share_id", ""));
			postData.add(new BasicNameValuePair("access_dir_id", "0"));
			postData.add(new BasicNameValuePair("dir_id", "0"));
		    } else {
			postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
			postData.add(new BasicNameValuePair("share_user_id", ""));
			postData.add(new BasicNameValuePair("share_id", ""));
			postData.add(new BasicNameValuePair("access_dir_id", mCurrentFolder.getAccessDirID()));
			postData.add(new BasicNameValuePair("dir_id", mCurrentFolder.getID()));
		    }
		    postData.add(new BasicNameValuePair("name", etNewFolderName.getText().toString().trim()));
		    resultXML = getFileListRequest.httpPost(getFilListUrlString, postData);
		    LoadingDialog.dismiss();
		    if (resultXML.equals(Constant.ErrorMessage)) {
			dismiss();
			return;
		    }

		    FinishHandler.sendMessage(FinishHandler.obtainMessage());
		} catch (Exception e) {
		    dismiss();
		}
	    }
	}).start();
    }

    Handler NeweFolderMessageHandler = new Handler() {
	public void handleMessage(Message msg) {
	    showValidationAlertDialog(getString(R.string.txt_warning), getString(R.string.emptyfolder));
	}
    };

    public void initValidationAlertDialog() {

	if (validationAlertDialog == null)
	    validationAlertDialog = new AlertDialog.Builder(getActivity());
	validationAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
	validationAlertDialog.setTitle(R.string.txt_warning);
	validationAlertDialog.setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface arg0, int arg1) {

		return;
	    }
	});
    }

    public void showValidationAlertDialog(String title, String errorString) {
	validationAlertDialog.setTitle(title);
	validationAlertDialog.setMessage(errorString);
	validationAlertDialog.show();
    }

    Handler FinishHandler = new Handler() {
	public void handleMessage(Message msg) {
	    mOnCreateFolderListener.onCreateFolder();
	    dismiss();
	}
    };

    public void initLoadingDialog() {

	if (LoadingDialog == null)
	    LoadingDialog = new ProgressDialog(getActivity());

	LoadingDialog.setMessage(getString(R.string.txt_loading));
	/*
	 * LoadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() { public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) { if (keyCode == KeyEvent.KEYCODE_BACK) { LoadingDialog.cancel();
	 * 
	 * } return false; } });
	 */

    }

}
