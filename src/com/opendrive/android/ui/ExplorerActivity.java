package com.opendrive.android.ui;

import android.app.Activity;

import java.io.File;

import com.opendrive.android.OpenDriveApplication;
import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;

import android.app.Dialog;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.StatFs;
import android.os.Environment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

/**
 * This is the main activity. The activity that is presented to the user as the application launches. This class is, and expected not to be, instantiated. <br>
 * <p/>
 * This class handles creating the buttons and text views. This class relies on the class EventHandler to handle all button press logic and to control the data displayed on its ListView. This class also relies on the FileManager class to handle all file operations such as copy/paste zip/unzip etc. However most interaction with the FileManager class is done via the EventHandler class. Also the SettingsMangager class to load and save user settings. <br>
 * <p/>
 * The design objective with this class is to control only the look of the GUI (option menu, context menu, ListView, buttons and so on) and rely on other supporting classes to do the heavy lifting.
 *
 * @author Joe Berria
 */
public final class ExplorerActivity extends ListActivity {
    @Override
    protected void onResume() {
        OpenDriveApplication.applicationResumed();
        super.onResume();
    }

    public static final String ACTION_WIDGET = "com.nexes.manager.Main.ACTION_WIDGET";

    private static final String PREFS_NAME = "ManagerPrefsFile"; // user preference file name
    private static final String PREFS_HIDDEN = "hidden";
    private static final String PREFS_COLOR = "color";
    private static final String PREFS_THUMBNAIL = "thumbnail";
    private static final String PREFS_SORT = "sort";
    private static final String PREFS_STORAGE = "sdcard space";

    private static final int SEARCH_B = 0x09;
    private static final int SETTING_REQ = 0x10; // request code for intent

    private FileManager mFileMag;
    private EventHandler mHandler;
    private EventHandler.TableRow mTable;

    private SharedPreferences mSettings;
    private boolean mReturnIntent = false;
    private boolean mHoldingFile = false;
    private boolean mHoldingZip = false;
    private boolean mUseBackKey = true;
    private String mCopiedTarget;
    private String mZippedTarget;
    private String mSelectedListItem; // item from context menu
    private TextView mPathLabel, mDetailLabel;// , mStorageLabel;

    private Button m_btnCancel;
    private Button m_btnUpload;

    private boolean isBackPressed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.explorer);

	/* read settings */
        mSettings = getSharedPreferences(PREFS_NAME, 0);
        boolean hide = mSettings.getBoolean(PREFS_HIDDEN, false);
        boolean thumb = mSettings.getBoolean(PREFS_THUMBNAIL, true);
        int space = mSettings.getInt(PREFS_STORAGE, View.VISIBLE);
        int color = mSettings.getInt(PREFS_COLOR, -1);
        int sort = mSettings.getInt(PREFS_SORT, 3);

        mFileMag = new FileManager();
        mFileMag.setShowHiddenFiles(hide);
        mFileMag.setSortType(sort);

        if (savedInstanceState != null)
            mHandler = new EventHandler(ExplorerActivity.this, mFileMag, savedInstanceState.getString("location"));
        else
            mHandler = new EventHandler(ExplorerActivity.this, mFileMag);

        mHandler.setMultiSelecte(true);
        mHandler.setTextColor(color);
        mHandler.setShowThumbnails(thumb);
        mTable = mHandler.new TableRow();

	/*
     * sets the ListAdapter for our ListActivity andgives our EventHandler class the same adapter
	 */
        mHandler.setListAdapter(mTable);
        setListAdapter(mTable);

	/* register context menu for our list view */
        registerForContextMenu(getListView());

        // mStorageLabel = (TextView)findViewById(R.id.storage_label);
        mDetailLabel = (TextView) findViewById(R.id.detail_label);
        mPathLabel = (TextView) findViewById(R.id.path_label);
        mPathLabel.setText("path: /sdcard");

        updateStorageLabel();
        // mStorageLabel.setVisibility(space);

        mHandler.setUpdateLabels(mPathLabel, mDetailLabel);

	/* setup buttons */
        int[] img_button_id = {R.id.home_button, R.id.back_button};

        ImageButton[] bimg = new ImageButton[img_button_id.length];

        for (int i = 0; i < img_button_id.length; i++) {
            bimg[i] = (ImageButton) findViewById(img_button_id[i]);
            bimg[i].setOnClickListener(mHandler);
        }

        m_btnCancel = (Button) findViewById(R.id.btn_cancel1);
        m_btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                isBackPressed = true;
                OpenDriveApplication.setPasscodeEntered(true);
                finish();
            }
        });

        m_btnUpload = (Button) findViewById(R.id.btn_upload1);
        m_btnUpload.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // ExplorerActivity.this.setResult(1, new Intent());
                isBackPressed = true;
                OpenDriveApplication.setPasscodeEntered(true);
                finish();
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("location", mFileMag.getCurrentDir());
    }

    /*
     * (non Java-Doc) Returns the file that was selected to the intent that called this activity. usually from the caller is another application.
     */
    private void returnIntentResults(File data) {
        mReturnIntent = false;

        Intent ret = new Intent();
        ret.setData(Uri.fromFile(data));
        setResult(RESULT_OK, ret);

        finish();
    }

    private void updateStorageLabel() {
        long total, aval;
        int kb = 1024;

        StatFs fs = new StatFs(Environment.getExternalStorageDirectory().getPath());

        total = fs.getBlockCount() * (fs.getBlockSize() / kb);
        aval = fs.getAvailableBlocks() * (fs.getBlockSize() / kb);

        // mStorageLabel.setText(String.format("sdcard: Total %.2f GB " +
        // "\t\tAvailable %.2f GB",
        // (double)total / (kb * kb), (double)aval / (kb * kb)));
    }

    /**
     * To add more functionality and let the user interact with more file types, this is the function to add the ability.
     * <p/>
     * (note): this method can be done more efficiently
     */
    @Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        final String item = mHandler.getData(position);
        boolean multiSelect = mHandler.isMultiSelected();
        File file = new File(mFileMag.getCurrentDir() + "/" + item);
        String item_ext = null;

        try {
            item_ext = item.substring(item.lastIndexOf("."), item.length());

        } catch (IndexOutOfBoundsException e) {
            item_ext = "";
        }

	/*
	 * If the user has multi-select on, we just need to record the file not make an intent for it.
	 */
        // if(multiSelect) {
        // mTable.addMultiPosition(position, file.getPath());

        // } else {
        if (file.isDirectory()) {
            if (file.canRead()) {
                mHandler.stopThumbnailThread();
                mHandler.updateDirectory(mFileMag.getNextDir(item, false));
                mPathLabel.setText(mFileMag.getCurrentDir());

		/*
		 * set back button switch to true (this will be better implemented later)
		 */
                if (!mUseBackKey)
                    mUseBackKey = true;

            } else {
                Toast.makeText(this, "Can't read folder due to permissions", Toast.LENGTH_SHORT).show();
            }
        } else {
            mTable.addMultiPosition(position, file.getPath());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.PASSCODE_CHECK_REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_CANCELED) {
                finish();
            } else if (resultCode == RESULT_OK) {
                OpenDriveApplication.setPasscodeEntered(true);
            }
        } else {
            super.onActivityResult(1, resultCode, data);
        }
	/*
	 * SharedPreferences.Editor editor = mSettings.edit(); boolean check; boolean thumbnail; int color, sort, space;
	 * 
	 * if(requestCode == SETTING_REQ && resultCode == RESULT_CANCELED) { //save the information we get from settings activity check = data.getBooleanExtra("HIDDEN", false); thumbnail = data.getBooleanExtra("THUMBNAIL", true); color = data.getIntExtra("COLOR", -1); sort = data.getIntExtra("SORT", 0); space = data.getIntExtra("SPACE", View.VISIBLE);
	 * 
	 * editor.putBoolean(PREFS_HIDDEN, check); editor.putBoolean(PREFS_THUMBNAIL, thumbnail); editor.putInt(PREFS_COLOR, color); editor.putInt(PREFS_SORT, sort); editor.putInt(PREFS_STORAGE, space); editor.commit();
	 * 
	 * mFileMag.setShowHiddenFiles(check); mFileMag.setSortType(sort); mHandler.setTextColor(color); mHandler.setShowThumbnails(thumbnail); //mStorageLabel.setVisibility(space); mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(), true)); }
	 */
    }

    /* ================Menus, options menu and context menu end here================= */

    @Override
    protected Dialog onCreateDialog(int id) {
        return super.onCreateDialog(id);
    }

    /*
     * (non-Javadoc) This will check if the user is at root directory. If so, if they press back again, it will close the application.
     * 
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        String current = mFileMag.getCurrentDir();

        if (keycode == KeyEvent.KEYCODE_SEARCH) {
            showDialog(SEARCH_B);

            return true;

        } else if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey && !current.equals("/")) {
            // if(mHandler.isMultiSelected()) {
            // mTable.killMultiSelect(true);
            // Toast.makeText(ExplorerActivity.this, "Multi-select is now off", Toast.LENGTH_SHORT).show();

            // } else {
            if (mHandler.isMultiSelected())
                mTable.killMultiSelect(true);
            mHandler.stopThumbnailThread();
            mHandler.updateDirectory(mFileMag.getPreviousDir());
            mPathLabel.setText(mFileMag.getCurrentDir());
            // }
            return true;

        } else if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey && current.equals("/")) {
            // Toast.makeText(ExplorerActivity.this, "Press back again to quit.", Toast.LENGTH_SHORT).show();

            if (mHandler.isMultiSelected()) {
                mTable.killMultiSelect(true);
                // Toast.makeText(ExplorerActivity.this, "Multi-select is now off", Toast.LENGTH_SHORT).show();
            }

            mUseBackKey = false;
            mPathLabel.setText(mFileMag.getCurrentDir());

            return false;

        } else if (keycode == KeyEvent.KEYCODE_BACK && !mUseBackKey && current.equals("/")) {
            isBackPressed = true;
            OpenDriveApplication.setPasscodeEntered(true);
            finish();

            return false;
        }
        return false;
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
    protected void onPause() {
        super.onPause();
        if (!isBackPressed) {
            OpenDriveApplication.setPasscodeEntered(false);
        }
    }
}