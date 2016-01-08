package com.opendrive.android.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParserException;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.MimeTypeParser;
import com.opendrive.android.common.MimeTypes;
import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.DeleteFileData;
import com.opendrive.android.datamodel.FileData;
import com.opendrive.android.parser.DeletFileDataParser;
import com.opendrive.android.request.Request;
import com.opendrive.android.service.AutoUploadService;
import com.opendrive.android.ui.fragment.FilesFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FilePropertyActivity extends Activity {

    private ImageView mFileThumb = null;
    private TextView mFileName = null;
    private TextView mFileSize = null;
    private TextView mModifiedDate = null;
    private LinearLayout mPreview = null;
    private LinearLayout mEmailLink = null;
    private RelativeLayout mCopyLink = null;
    private LinearLayout mDeleteOfflineFile = null;
    private LinearLayout mDelete = null;
    private TextView mTxtDeleteOfflineFile = null;

    public static FileData fileItem = null;

    private MimeTypes mtMimeTypes;

    private AlertDialog.Builder validationAlertDialog = null;
    private ProgressDialog LoadingDialog = null;
    private boolean bIsLoadingStop = false;

    private ImageView mCheckCopyLink = null;

    private DeletFileDataParser mDeletFileDataParser = null;
    private DeleteFileData mDeleteFileData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fileproperty);

        initValidationAlertDialog();
        initLoadingDialog();

        getMimeTypes();

//		Button btnBack = (Button)findViewById(R.id.button_back);
//		btnBack.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				finish();
//			}
//		});

        mFileThumb = (ImageView) findViewById(R.id.imageView_thumb);

        Bitmap iconBitmap = null;

        iconBitmap = Utils.getIconFromFileID(fileItem.getID(), true);

        if (iconBitmap != null)
            mFileThumb.setImageBitmap(iconBitmap);

        mFileName = (TextView) findViewById(R.id.textView_fileName);
        mFileName.setText(fileItem.getName());

        mFileSize = (TextView) findViewById(R.id.textView_fileSize);
        mFileSize.setText("Size: " + fileItem.getSize());

        mModifiedDate = (TextView) findViewById(R.id.textView_modifiedDate);
        mModifiedDate.setText("modified: " + fileItem.getDateModified());

        mPreview = (LinearLayout) findViewById(R.id.linearLayout_preview);
        mPreview.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                String fileFullPath = Utils.getFolderFullPath(fileItem);
                File tmpFile = new File(fileFullPath);

                if (FilesActivity.offlineMode) {
                    if (tmpFile.exists()) {
                        openContents(tmpFile);
                    } else {
                        Toast.makeText(FilePropertyActivity.this, getString(R.string.msg_non_exist_file), Toast.LENGTH_LONG).show();
                    }
                    return;
                } else {
                    if (tmpFile.exists()) {
                        openContents(tmpFile);
                    } else {
                        downloadFile(fileItem, true);
                    }
                }
            }
        });

        mEmailLink = (LinearLayout) findViewById(R.id.linearLayout_email);
        mEmailLink.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                emailLink();
            }
        });

        mCopyLink = (RelativeLayout) findViewById(R.id.linearLayout_copyLink);
        mCopyLink.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                mCheckCopyLink.setVisibility(View.VISIBLE);
                String publicLink = fileItem.getDirectLinkPublic();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(publicLink);

            }
        });

        mCheckCopyLink = (ImageView) findViewById(R.id.imageView_checkedCopyLink);
        mCheckCopyLink.setVisibility(View.GONE);

        mDeleteOfflineFile = (LinearLayout) findViewById(R.id.linearLayout_deleteOfflineFile);
        mDeleteOfflineFile.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                String fileFullPath = Utils.getFolderFullPath(fileItem);

                final File offlineFile = new File(fileFullPath);

                if (FilesActivity.offlineMode) {
                    Toast.makeText(FilePropertyActivity.this, getString(R.string.msg_network_unreachable), Toast.LENGTH_LONG).show();
                    return;
                }

                if (offlineFile.exists()) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(FilePropertyActivity.this);

                    alertDialog.setIcon(R.drawable.ic_dialog_alarm);
                    alertDialog.setTitle(R.string.app_name);
                    alertDialog.setMessage(getString(R.string.msg_confirm_deleteoffline, fileItem.getName()));
                    alertDialog.setPositiveButton(R.string.txt_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {

                                    return;
                                }
                            });
                    alertDialog.setNegativeButton(R.string.txt_delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {

                            offlineFile.delete();
                            mTxtDeleteOfflineFile.setText(R.string.label_saveforoffline);

                            return;
                        }
                    });
                    alertDialog.show();
                } else {
                    downloadFile(fileItem, false);
                }
            }
        });

        mTxtDeleteOfflineFile = (TextView) findViewById(R.id.textView_deleteOfflineFile);

        String fileFullPath = Utils.getFolderFullPath(fileItem);

        File file = new File(fileFullPath);

        if (file.exists())
            mTxtDeleteOfflineFile.setText(R.string.label_deleteoffline);
        else
            mTxtDeleteOfflineFile.setText(R.string.label_saveforoffline);

        mDelete = (LinearLayout) findViewById(R.id.linearLayout_delete);
        mDelete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (FilesActivity.offlineMode) {
                    Toast.makeText(FilePropertyActivity.this, getString(R.string.msg_network_unreachable), Toast.LENGTH_LONG).show();
                    return;
                }

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(FilePropertyActivity.this);
                alertDialog.setIcon(R.drawable.ic_dialog_alarm);
                alertDialog.setTitle(R.string.app_name);
                alertDialog.setMessage(getString(R.string.msg_confirm_deleteoffline, fileItem.getName()));
                alertDialog.setPositiveButton(R.string.txt_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {

                                return;
                            }
                        });
                alertDialog.setNegativeButton(R.string.txt_delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                        deleteFile();
                        return;
                    }
                });
                alertDialog.show();
            }
        });

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
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    public void showLoginActivity() {

        Intent intent = new Intent(this, LogIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void openContents(File file) {
        if (!file.exists()) {
            return;
        }

        if (mtMimeTypes == null)
            getMimeTypes();

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

        Uri data = Uri.fromFile(file);
        String type = mtMimeTypes.getMimeType(file.getName());
        intent.setDataAndType(data, type);

        startActivity(intent);
    }

    private void getMimeTypes() {
        MimeTypeParser mtp = new MimeTypeParser();
        XmlResourceParser in = getResources().getXml(R.xml.mimetypes);

        try {
            mtMimeTypes = mtp.fromXmlResource(in);
        } catch (XmlPullParserException e) {
            Log.e("tmRecordListView", "PreselectedChannelsActivity: XmlPullParserException", e);
            throw new RuntimeException("PreselectedChannelsActivity: XmlPullParserException");
        } catch (IOException e) {
            Log.e("tmRecordListView", "PreselectedChannelsActivity: IOException", e);
            throw new RuntimeException("PreselectedChannelsActivity: IOException");
        }
    }

    public void downloadFile(final FileData fileData, final boolean isOpen) {
        LoadingDialog.setMessage(getString(R.string.txt_downloading));
        LoadingDialog.show();
        
        new DoBackgroundWork().execute(new Integer(Constant.BACKGROUND_DOWNLOAD_FILE), fileData, new Boolean(isOpen));  
    }

    public void initValidationAlertDialog() {

        if (validationAlertDialog == null)
            validationAlertDialog = new AlertDialog.Builder(FilePropertyActivity.this);

        validationAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
        validationAlertDialog.setTitle(R.string.txt_warning);
        validationAlertDialog.setPositiveButton(R.string.txt_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        loseSession();
                        return;
                    }
                });
    }

    public void initLoadingDialog() {

        if (LoadingDialog == null)
            LoadingDialog = new ProgressDialog(this);

        LoadingDialog.setCancelable(false);
        LoadingDialog.setCanceledOnTouchOutside(false);
        
        LoadingDialog.setMessage(getString(R.string.txt_loading));
        LoadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    LoadingDialog.cancel();
                    bIsLoadingStop = true;
                }
                return false;
            }
        });
    }

    Handler ConnectionerrorMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            showValidationAlertDialog(getString(R.string.connection_error));
        }
    };

    public void showValidationAlertDialog(String errorString) {
        validationAlertDialog.setMessage(errorString);
        validationAlertDialog.show();
    }

    public void deleteFile() {
        LoadingDialog.setMessage(getString(R.string.txt_deleting));
        LoadingDialog.show();

        new DoBackgroundWork().execute(new Integer(Constant.BACKGROUND_DELETE_FILE));  
    }

    public void emailLink() {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

		/* Fill it with Data */
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"to@email.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.msg_emailsubject,  "this is for fun"/*fileItem.getName()*/));
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.msg_emailtext, fileItem.getName(), fileItem.getDirectLink()));

		/* Send it off to the Activity-Chooser */
        startActivity(Intent.createChooser(emailIntent, "Send mail"));
    }

    Handler DeleteFileFailedMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (mDeleteFileData.getName().length() > 0)
                validationAlertDialog.setTitle(mDeleteFileData.getName());
            else
                validationAlertDialog.setTitle("Unknow Error");

            if (mDeleteFileData.getDescription().length() > 0)
                validationAlertDialog.setMessage(mDeleteFileData.getDescription());
            else
                validationAlertDialog.setMessage("Unknow Error");

            validationAlertDialog.show();
        }
    };

    public void loseSession() {

        Constant.SessionID = "";
        Constant.UserName = "";
        Constant.Password = "";

        showLoginActivity();
        finish();

    }
    
    private class DoBackgroundWork extends AsyncTask<Object, Integer, Integer> {
        protected Integer doInBackground(Object... params) {
			Integer backgroundTaskID = (Integer) params[0];
			
    		switch (backgroundTaskID) {
        		case Constant.BACKGROUND_DOWNLOAD_FILE:	
                    try {
            			FileData fileData = (FileData) params[1];
            			final String fileFullPath = Utils.getFolderFullPath(fileData);
            			final Boolean isOpen = (Boolean) params[2];
                        Request downloadRequest = new Request();

                        String downloadRequestUrl = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;

                        ArrayList<NameValuePair> postDataArrayList = new ArrayList<NameValuePair>();

                        postDataArrayList.add(new BasicNameValuePair("action", "open_file_download"));
                        postDataArrayList.add(new BasicNameValuePair("session_id", Constant.SessionID));
                        postDataArrayList.add(new BasicNameValuePair("is_pro", ""));
                        postDataArrayList.add(new BasicNameValuePair("share_user_id", ""));
                        postDataArrayList.add(new BasicNameValuePair("share_id", ""));
                        postDataArrayList.add(new BasicNameValuePair("access_dir_id", ""));
                        postDataArrayList.add(new BasicNameValuePair("file_id", fileData.getID()));
                        postDataArrayList.add(new BasicNameValuePair("file_offset", ""));

                        boolean result = downloadRequest.downloadFile(downloadRequestUrl, postDataArrayList, fileFullPath, fileData);

                        if (bIsLoadingStop) {
                            bIsLoadingStop = false;
                            return Constant.BACKGROUND_PROCESS_OK;
                        }

                        LoadingDialog.dismiss();

                        if (!result) {
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            return Constant.BACKGROUND_PROCESS_ERROR;
                        } else {
                            runOnUiThread(new Runnable() {

                                public void run() {

                                    mTxtDeleteOfflineFile.setText(R.string.label_deleteoffline);

                                    if (isOpen)
                                        openContents(new File(fileFullPath));
                                    else {
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FilePropertyActivity.this);

                                        alertDialog.setIcon(R.drawable.ic_dialog_alarm);
                                        alertDialog.setTitle(R.string.app_name);
                                        alertDialog.setMessage(R.string.msg_confirm_successdownload);
                                        alertDialog.setPositiveButton(R.string.txt_close,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface arg0, int arg1) {

                                                        return;
                                                    }
                                                });
                                        alertDialog.show();
                                    }
                                }
                            });
                        }
                        
                        return Constant.BACKGROUND_PROCESS_OK;
                        
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        LoadingDialog.dismiss();
                        FilesActivity.offlineMode = true;
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        e.printStackTrace();
                        
                        return Constant.BACKGROUND_PROCESS_ERROR;
                    }
                   
        		case Constant.BACKGROUND_DELETE_FILE:
                    try {
                        Request loginRequest = new Request();

                        String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                        postData.add(new BasicNameValuePair("action", "move_file_to_trash"));
                        postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                        postData.add(new BasicNameValuePair("share_user_id", ""));
                        postData.add(new BasicNameValuePair("share_id", ""));
                        postData.add(new BasicNameValuePair("access_dir_id", ""));
                        postData.add(new BasicNameValuePair("file_id", fileItem.getID()));

                        String resultXML = loginRequest.httpPost(loginUrlString, postData);

                        if (bIsLoadingStop) {
                            bIsLoadingStop = false;
                            return Constant.BACKGROUND_PROCESS_OK;
                        }

                        if (resultXML.equals(Constant.ErrorMessage)) {
                            LoadingDialog.dismiss();
                            FilesActivity.offlineMode = true;
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            return Constant.BACKGROUND_PROCESS_ERROR;
                        } else {
                            mDeletFileDataParser = new DeletFileDataParser();
                            mDeleteFileData = mDeletFileDataParser.parseResponse(resultXML);

                            LoadingDialog.dismiss();

                            if (mDeleteFileData != null) {
                                if (mDeleteFileData.getName().length() > 0) {
                                    DeleteFileFailedMessageHandler.sendMessage(DeleteFileFailedMessageHandler.obtainMessage());
                                    return Constant.BACKGROUND_PROCESS_ERROR;
                                } else {
                                    MainActivity.mFileUploaded = true;
                                    finish();
                                }
                            } else {
                                ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                                return Constant.BACKGROUND_PROCESS_ERROR;
                            }
                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        LoadingDialog.dismiss();
                        FilesActivity.offlineMode = true;
                        ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                        e.printStackTrace();
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
