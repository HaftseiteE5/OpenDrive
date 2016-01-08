package com.opendrive.android.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.CreateFileResultData;
import com.opendrive.android.parser.CreateFileResultDataParser;
import com.opendrive.android.parser.LoginDataParser;
import com.opendrive.android.parser.SignUpDataParser;
import com.opendrive.android.request.Request;


public class MainActivityBackup extends ActivityGroup {
	public static final int VIEW_FILES = 0;
	public static final int VIEW_SETTING = 1;
	public static final int VIEW_UPLOAD = 2;

	private int viewSequence[] = {-1, -1, -1};
	private int mCurrentViewId = 0;

	private LinearLayout bodyLayout;
//	private LinearLayout btnFiles;
//	private LinearLayout btnUpload;
//	private LinearLayout btnSetting;
//	private ImageView	imgFiles;
//	private ImageView	imgUpload;
//	private ImageView	imgSetting;
	private int REQUEST_GALLERY = 0;
	private int REQUEST_EXPLORER = 1;
	
	private boolean bIsSearchStop = false;
	private AlertDialog.Builder validationAlertDialog = null;
	private CreateFileResultData mCreatFileResultData = null;
	private CreateFileResultDataParser mCreatFileResultDataParser = null;
	private ProgressDialog LoadingDialog = null;
	public static MainActivityBackup Instance;
	public static FilesActivity fileActivityInstance = null;
	public static boolean mFileUploaded = false;

	private AlertDialog.Builder switchOfflineModeAlertDialog = null;
	
	static public boolean m_bLoading = false;
	
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
		initLoadingDialog();
		initValidationAlertDialog();

		bodyLayout = (LinearLayout) findViewById(R.id.activity_view_LinearLayout);
		bodyLayout.setBackgroundResource(R.color.WHITE_TEXTCOLOR);

		Instance = this;
		initView();

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_GALLERY) {

			if (data == null)
				return;

			Uri uri = data.getData();

			String imagePath = getRealPathFromURI(uri);

			if (imagePath.length() > 0)
				uploadImage(imagePath);
		}
		else if (requestCode == REQUEST_EXPLORER){
			if(EventHandler.mMultiSelectData.size()>0){
				UploadHandler.sendMessage(UploadHandler.obtainMessage());
			}
		}
	}

	public void uploadImage(final String imageFullPath){					
		LoadingDialog.show();
        new DoBackgroundWork().execute(new Integer(Constant.BACKGROUND_UPLOAD_IMAGE_FILE), imageFullPath);  
	}

	public Handler ReadyUploadHandler = new Handler() {
		public void handleMessage(Message msg) {
			LoadingDialog.show();
			UploadHandler.sendMessage(UploadHandler.obtainMessage());
		}
	};
	
	public Handler UploadHandler = new Handler() {
		public void handleMessage(Message msg) {
			m_bLoading = false;
			if(EventHandler.mMultiSelectData.size()>0){
				String path = EventHandler.mMultiSelectData.get(0).trim();
				EventHandler.mMultiSelectData.remove(0);
				if(path.length()>0)
					uploadImage(path);
			}
		}
	};
	
	public Handler FileActivityRefreshHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(mFileUploaded && mCurrentViewId == VIEW_FILES){
				fileActivityInstance.refresh();
				mFileUploaded = false;
			}
		}
	};

	public Handler FileActivityBackPressedHandler = new Handler() {
		public void handleMessage(Message msg) {
				fileActivityInstance.getBack();
		}
	};

	
	private boolean createFileRequest(File file, String destFileName){

		try {
			Request createFileRequest = new Request();

			String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
			ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

			postData.add(new BasicNameValuePair("action", "create_and_open_file"));
			postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
			postData.add(new BasicNameValuePair("is_pro", ""));
			postData.add(new BasicNameValuePair("share_user_id", ""));
			postData.add(new BasicNameValuePair("share_id", ""));
			if(FilesActivity.mCurrentFolderData == null){
				postData.add(new BasicNameValuePair("access_dir_id", "0"));
				postData.add(new BasicNameValuePair("dir_id", "0"));

			}else{
				postData.add(new BasicNameValuePair("access_dir_id", FilesActivity.mCurrentFolderData.getAccessDirID()));
				postData.add(new BasicNameValuePair("dir_id", FilesActivity.mCurrentFolderData.getID()));
			}
			postData.add(new BasicNameValuePair("name", destFileName));
			postData.add(new BasicNameValuePair("size", file.length() + ""));

			String resultXML =  createFileRequest.httpPost(loginUrlString, postData);

			if(bIsSearchStop){
				bIsSearchStop = false;
				return false;
			}

			if(resultXML.equals(Constant.ErrorMessage)){
				LoadingDialog.dismiss();
				ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
				return false;

			}else{
				mCreatFileResultDataParser = new CreateFileResultDataParser();
				mCreatFileResultData = mCreatFileResultDataParser.parseResponse(resultXML);

				if(mCreatFileResultData != null){
					if(mCreatFileResultData.getID().length() == 0){
						LoadingDialog.dismiss();
						CreateFileErrorHandler.sendMessage(CreateFileErrorHandler.obtainMessage());
						return false;
					}else{
						return true;
					}
				}else{
					LoadingDialog.dismiss();
					ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
					return false;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LoadingDialog.dismiss();
			String exceptionString = e.getMessage();
			if(exceptionString.contains("Network unreachable") || exceptionString.contains("Host is unresolved")){
				//Network unreachable
				FilesActivity.offlineMode = true;
				SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
			}else
				ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
			return false;
		}

	}

	private boolean closeFileUpload(File file){

		try {
			Request closeFileUploadRequest = new Request();

			String closeFileUploadUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;

			ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

			postData.add(new BasicNameValuePair("action", "close_file_upload"));
			postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
			postData.add(new BasicNameValuePair("is_pro", ""));
			postData.add(new BasicNameValuePair("share_user_id", ""));
			postData.add(new BasicNameValuePair("share_id", ""));
			postData.add(new BasicNameValuePair("access_dir_id", ""));
			postData.add(new BasicNameValuePair("file_id", mCreatFileResultData.getID()));
			postData.add(new BasicNameValuePair("temp_location", mCreatFileResultData.getTempLocation()));
			postData.add(new BasicNameValuePair("file_time", Utils.getTimeStamp()));
			postData.add(new BasicNameValuePair("file_size", file.length() + ""));

			String resultXML =  closeFileUploadRequest.httpPost(closeFileUploadUrlString, postData);

			if(bIsSearchStop){
				bIsSearchStop = false;
				return false;
			}

			if(resultXML.contains(Constant.ErrorMessage)){
				LoadingDialog.dismiss();
				ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
				return false;

			}else{
				LoadingDialog.dismiss();
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LoadingDialog.dismiss();
			String exceptionString = e.getMessage();
			if(exceptionString.contains("Network unreachable") || exceptionString.contains("Host is unresolved")){
				//Network unreachable
				FilesActivity.offlineMode = true;
				SwitchOfflineMessageHandler.sendMessage(SwitchOfflineMessageHandler.obtainMessage());
			}else
				ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
			return false;
		}

	}

	private void initView() {
		createChildLayout(VIEW_FILES);
		SetTabTitleBackground(VIEW_FILES);
	}

	private View createChildLayout(int viewId)
	{
		View childView = getChildLayout(viewId);
		if(childView != null)
			return childView;

		switch (viewId) {
		case VIEW_FILES:
			bodyLayout.setBackgroundResource(R.color.WHITE_TEXTCOLOR);

			childView = getLocalActivityManager().startActivity("Files", new
					Intent(getApplicationContext(),FilesActivity.class)
			.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
			.getDecorView();
			addChildLayout(childView, viewId);

			break;
		case VIEW_SETTING:

			bodyLayout.setBackgroundResource(R.drawable.bg);

			childView = getLocalActivityManager().startActivity("Setting", new
					Intent(getApplicationContext(), SettingActivity.class)
			.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
			.getDecorView();
			addChildLayout(childView, viewId);
			break;
		case VIEW_UPLOAD:

			bodyLayout.setBackgroundResource(R.drawable.bg_newfile);

			childView = getLocalActivityManager().startActivity("Upload", new
					Intent(getApplicationContext(), UploadActivity.class)
			.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
			.getDecorView();
			addChildLayout(childView, viewId);
			break;
		default:
			break;
		}

		return childView;
	}

	private View getChildLayout(int viewId)
	{
		if (viewSequence[viewId] == -1)
			return null;

		return bodyLayout.getChildAt(viewSequence[viewId]);
	}
//
	private synchronized void addChildLayout(View view, int viewId)
	{
		if(viewSequence[viewId] > -1)
			return;

		viewSequence[viewId] = bodyLayout.getChildCount();
		bodyLayout.addView(view);
	}

	protected void showChildLayout(int viewId) {

		for(int i = 0; i < bodyLayout.getChildCount(); i++)
			bodyLayout.getChildAt(i).setVisibility((viewSequence[viewId] == i) ? View.VISIBLE : View.GONE);

		SetTabTitleBackground(viewId);
		mCurrentViewId = viewId;

		if(mFileUploaded && mCurrentViewId == VIEW_FILES){
			fileActivityInstance.refresh();
			mFileUploaded = false;
		}
	}

	public Handler receiveHandler = new Handler() {
		public void handleMessage(Message msg) {

			int viewId = Integer.parseInt(msg.obj.toString());

			showChildLayout(viewId);

			mCurrentViewId = viewId;

			SetTabTitleBackground(viewId);
		}
	};

	private void SetTabTitleBackground(int selTabIndex) {

		switch (selTabIndex) {
		case VIEW_FILES:
			bodyLayout.setBackgroundResource(R.color.WHITE_TEXTCOLOR);


			break;
		case VIEW_SETTING:
			bodyLayout.setBackgroundResource(R.drawable.bg);

			break;
		default:
			break;
		}
	}


	public void onBackPressed() {
		switch (mCurrentViewId) {
		case VIEW_FILES:
			FileActivityBackPressedHandler.sendMessage(FileActivityBackPressedHandler.obtainMessage());

			break;

		case VIEW_SETTING:
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if( m_bLoading && EventHandler.mMultiSelectData!=null && EventHandler.mMultiSelectData.size()>0){
			UploadHandler.sendMessage(UploadHandler.obtainMessage());
		}else{
			FileActivityRefreshHandler.sendMessage(FileActivityRefreshHandler.obtainMessage());
		}
		super.onResume();
	}
	
	public String getRealPathFromURI(Uri contentUri) {

		// can post image
		String [] proj={MediaStore.Images.Media.DATA};
		Cursor cursor = managedQuery( contentUri,
				proj,      	// Which columns to return
				null,       // WHERE clause; which rows to return (all rows)
				null,       // WHERE clause selection arguments (none)
				null); 		// Order-by clause (ascending by name)
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();

		return cursor.getString(column_index);
	}

	public void initLoadingDialog(){

		if (LoadingDialog == null)
			LoadingDialog = new ProgressDialog(this);
		
        LoadingDialog.setCancelable(false);
        LoadingDialog.setCanceledOnTouchOutside(false);
               
		LoadingDialog.setMessage(getString(R.string.txt_uploadding));
		LoadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					LoadingDialog.cancel();
					bIsSearchStop = true;
				}
				return false;
			}
		});
	}

	public void initValidationAlertDialog(){

		if (validationAlertDialog == null)
			validationAlertDialog = new AlertDialog.Builder(MainActivityBackup.this);
		validationAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
		validationAlertDialog.setTitle(R.string.txt_warning);
		validationAlertDialog.setPositiveButton(R.string.txt_ok,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {

				return;
			}
		});
		
		if (switchOfflineModeAlertDialog == null)
			switchOfflineModeAlertDialog = new AlertDialog.Builder(MainActivityBackup.this);

		switchOfflineModeAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
		switchOfflineModeAlertDialog.setTitle(R.string.txt_warning);
		switchOfflineModeAlertDialog.setPositiveButton(R.string.txt_ok,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {

				FilesActivity.offlineMode = true;
				return;
			}
		});
	}

	public void showValidationAlertDialog(String errorString){
		validationAlertDialog.setMessage(errorString);
		validationAlertDialog.show();
	}

	public void showValidationAlertDialog(String title, String errorString){
		validationAlertDialog.setTitle(title);
		validationAlertDialog.setMessage(errorString);
		validationAlertDialog.show();
	}

	Handler ConnectionerrorMessageHandler = new Handler() {
		public void handleMessage(Message msg) {
			showValidationAlertDialog(getString(R.string.connection_error));
		}
	};

	Handler CreateFileErrorHandler = new Handler() {
		public void handleMessage(Message msg) {
			showValidationAlertDialog(mCreatFileResultData.getName(), mCreatFileResultData.getDescription());
		}
	};
	
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
//        case R.id.files:
//
//			Message msg = new Message();
//			msg.obj = VIEW_FILES;
////			createChildLayout(VIEW_FILES);
////			receiveHandler.sendMessage(msg);
//
//            return true;
//
//        case R.id.upload:
//			//Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//			//startActivityForResult(intent, REQUEST_GALLERY);
//
//        	Intent intent = new Intent(this, UploadActivity.class);
//    		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//    		startActivity(intent);
//
//        	/*
//			Message msg2 = new Message();
//			msg2.obj = VIEW_UPLOAD;
//			createChildLayout(VIEW_UPLOAD);
//			receiveHandler.sendMessage(msg2);
//			*/
//        	return true;
//
//        case R.id.settings:
//
//			Message msg1 = new Message();
//			msg1.obj = VIEW_SETTING;
////			createChildLayout(VIEW_SETTING);
////			receiveHandler.sendMessage(msg1);
//
//        	return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    Handler SwitchOfflineMessageHandler = new Handler() {
		public void handleMessage(Message msg) {
			switchOfflineModeAlertDialog.setMessage(R.string.switch_offline);
			switchOfflineModeAlertDialog.show();
		}
	};

    private class DoBackgroundWork extends AsyncTask<Object, Integer, Integer> {
        protected Integer doInBackground(Object... params) {
			Integer backgroundTaskID = (Integer) params[0];
			
    		switch (backgroundTaskID) {
        		case Constant.BACKGROUND_UPLOAD_IMAGE_FILE:	
        			String imageFullPath = (String) params[1];
					Date now = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
					String destFileName = sdf.format(now) + ".jpg";

					if(imageFullPath.contains(".png"))
						destFileName = destFileName.replace(".jpg", ".png");
					
					File tmpFile = new File(imageFullPath);

					if(!imageFullPath.trim().contains("png") && !imageFullPath.trim().contains("jpg")){
						String [] paths = imageFullPath.split("/");
						if(paths.length>0)
							destFileName = paths[paths.length-1];
					}
					
					if(!createFileRequest(tmpFile, destFileName))
						return Constant.BACKGROUND_PROCESS_ERROR;

//    					if(!uploadImageRequest(imageFullPath, destFileName))
//    						return;

					closeFileUpload(tmpFile);

					if(EventHandler.mMultiSelectData != null && EventHandler.mMultiSelectData.size()>0){
						ReadyUploadHandler.sendMessage(ReadyUploadHandler.obtainMessage());
					}else{
						mFileUploaded = true;
						FileActivityRefreshHandler.sendMessage(FileActivityRefreshHandler.obtainMessage());
					}
					
					return Constant.BACKGROUND_PROCESS_OK;
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