package com.opendrive.android.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.datamodel.FileData;
import com.opendrive.android.request.Request;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NewFileActivity extends Activity {

    static public FilesActivity m_rootActivity;

    static public FileData mCurrentFolder = null;

    private ProgressDialog LoadingDialog;
    private AlertDialog.Builder validationAlertDialog = null;
    private Button btnCancel;
    private Button btnOK;

    private EditText m_editNewFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newfile);

        initLoadingDialog();
        initValidationAlertDialog();

        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        btnOK = (Button) findViewById(R.id.btn_ok);
        btnOK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                onOK();
            }
        });

        m_editNewFile = (EditText) findViewById(R.id.editText1);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    void onOK() {
        if (m_editNewFile.getText().toString().trim().length() == 0) {
            NeweFolderMessageHandler.sendMessage(NeweFolderMessageHandler.obtainMessage());
            return;
        }

        LoadingDialog.show();
        new DoBackgroundWork().execute(new Integer(Constant.BACKGROUND_NEW_FILE));        
    }

    Handler NeweFolderMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            showValidationAlertDialog(getString(R.string.txt_warning), getString(R.string.emptyfolder));
        }
    };

    public void initValidationAlertDialog() {

        if (validationAlertDialog == null)
            validationAlertDialog = new AlertDialog.Builder(this);
        validationAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
        validationAlertDialog.setTitle(R.string.txt_warning);
        validationAlertDialog.setPositiveButton(R.string.txt_ok,
                new DialogInterface.OnClickListener() {
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
            setResult(RESULT_OK);
            finish();
        }
    };


    public void initLoadingDialog() {

        if (LoadingDialog == null)
            LoadingDialog = new ProgressDialog(this);

        LoadingDialog.setCancelable(false);
        LoadingDialog.setCanceledOnTouchOutside(false);
        
        LoadingDialog.setMessage(getString(R.string.txt_loading));
        /*LoadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					LoadingDialog.cancel();
					
				}
				return false;
			}
		});
		*/

    }

    private class DoBackgroundWork extends AsyncTask<Object, Integer, Integer> {
        protected Integer doInBackground(Object... params) {
			Integer backgroundTaskID = (Integer) params[0];
			
    		switch (backgroundTaskID) {
        		case Constant.BACKGROUND_NEW_FILE:
                    try {
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
                            postData.add(new BasicNameValuePair("share_user_id", mCurrentFolder.getShareUserID()));
                            postData.add(new BasicNameValuePair("share_id", mCurrentFolder.getID()));
                            postData.add(new BasicNameValuePair("access_dir_id", mCurrentFolder.getAccessDirID()));
                            postData.add(new BasicNameValuePair("dir_id", mCurrentFolder.getID()));
                        }
                        postData.add(new BasicNameValuePair("name", m_editNewFile.getText().toString().trim()));
                        resultXML = getFileListRequest.httpPost(getFilListUrlString, postData);
                        LoadingDialog.dismiss();
                        if (resultXML.equals(Constant.ErrorMessage)) {
                            setResult(RESULT_CANCELED);
                            finish();

                            return Constant.BACKGROUND_PROCESS_ERROR;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {


                            }
                        });

                        FinishHandler.sendMessage(FinishHandler.obtainMessage());
                        return Constant.BACKGROUND_PROCESS_OK;
                    } catch (Exception e) {
                        setResult(RESULT_CANCELED);
                        finish();
                        return Constant.BACKGROUND_PROCESS_ERROR;
                    }
                        			
        		default:
        			break;
    		}
    		
    		return Constant.BACKGROUND_PROCESS_ERROR;
        }
        
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(Integer result) {
            
            if (LoadingDialog != null) {
                try {
                	LoadingDialog.dismiss();
                } catch (Exception ignored) {
                }
            }
                    	
    		switch (result) {
    		case Constant.BACKGROUND_PROCESS_OK:    						
				break;
    		case Constant.BACKGROUND_PROCESS_ERROR:
    			break;
    		default:
    			break;
    		}
        }
    } 	    
}
