package com.opendrive.android.ui;

import java.io.File;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.FileData;
import com.opendrive.android.datamodel.LoginData;
import com.opendrive.android.datamodel.SignUpData;
import com.opendrive.android.parser.DeletFileDataParser;
import com.opendrive.android.parser.LoginDataParser;
import com.opendrive.android.parser.SignUpDataParser;
import com.opendrive.android.request.Request;
import com.opendrive.android.ui.fragment.FilesFragment;

public class LogIn extends Activity {

    private SignUpData mSignData = null;
    private SignUpDataParser mSignUpDataParser = null;
    public static LoginData mLoginData = null;
    private LoginDataParser mLoginDataParser = null;
    private AlertDialog.Builder validationAlertDialog = null;
    private ProgressDialog LoadingDialog = null;
    private boolean bIsSearchStop = false;
    private SharedPreferences settingPrefer;

    private EditText mEditTxt_EmailAddress;
    private EditText mEditTxt_Password;
    private Button loginButton;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);

        mEditTxt_EmailAddress = (EditText) findViewById(R.id.address);
        mEditTxt_Password = (EditText) findViewById(R.id.password);

        mEditTxt_Password.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login();
                }
                return false;
            }
        });

        loginButton = (Button) findViewById(R.id.button_Login);
        signupButton = (Button) findViewById(R.id.button_Signup);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            FilesFragment.sOfflineMode = false;
        } else {
            FilesFragment.sOfflineMode = true;
        }

        initWelcomeMsg();
        initValidationAlertDialog();
        initLoadingDialog();

        settingPrefer = getSharedPreferences(Constant.PREFS_NAME, 0);

        loginButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                login();
                // Utils.hideSoftInput(LogIn.this, mEditTxt_EmailAddress);
                // Utils.hideSoftInput(LogIn.this, mEditTxt_Password);
            }
        });

        signupButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoSignup();
            }
        });

        Utils.overrideFonts(this, findViewById(R.id.llLoginControls));

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initWelcomeMsg() {
        TextView textMsg = (TextView) findViewById(R.id.textWelcome);
        String strMsg = getString(R.string.txt_welcome_splash);
        final SpannableString text = new SpannableString(strMsg);
        text.setSpan(new RelativeSizeSpan(1f), 14, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new StyleSpan(Typeface.BOLD), 14, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new RelativeSizeSpan(0.6f), 30, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textMsg.setText(text);

    }

    @Override
    public void onBackPressed() {
        this.fileList();
        super.onBackPressed();
    }

    void gotoSignup() {
        Intent intent = new Intent(this, SignUp.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void login() {
        if (mEditTxt_EmailAddress.getText().toString().trim().length() == 0) {
            EmailErrorMessageHandler.sendMessage(EmailErrorMessageHandler.obtainMessage());
            return;
        }
        if (mEditTxt_Password.getText().toString().trim().length() == 0) {
            PasswordErrorMessageHandler.sendMessage(PasswordErrorMessageHandler.obtainMessage());
            return;
        }

        LoadingDialog.show();
        new DoBackgroundWork().execute(new Integer(Constant.BACKGROUND_LOGIN));  
    }

    Handler ConnectionerrorMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            showValidationAlertDialog(getString(R.string.connection_error));
        }
    };

    Handler LoginFailedMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            showValidationAlertDialog(mLoginData.getName(), mLoginData.getDescription());
        }
    };

    public void saveUserInfo(String Username, String Password) {

        final SharedPreferences.Editor editor = settingPrefer.edit();

        Constant.UserName = Username;
        Constant.Password = Password;

        editor.putString(Constant.UserName_Key, Username);
        editor.putString(Constant.Password_Key, Password);

        editor.commit();

    }

    public void showMainActivity() {

        if (Utils.getPassCodeTurned(this)) {
            Intent intent = new Intent(this, EnterPassCodeActivity.class);
            intent.putExtra(Constant.ENTER_PASS_CODE_MODE, Constant.MODE_ENTER_PASSCODE);
            startActivityForResult(intent, 123);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 123) {

            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
            if (resultCode == RESULT_CANCELED) {
            }
        }
    }

    Handler EmailErrorMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            showValidationAlertDialog(getString(R.string.txt_notification), getString(R.string.emptyUsername));
        }
    };

    Handler PasswordErrorMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            showValidationAlertDialog(getString(R.string.txt_notification), getString(R.string.emptyPassword));
        }
    };

    public void initValidationAlertDialog() {

        if (validationAlertDialog == null)
            validationAlertDialog = new AlertDialog.Builder(LogIn.this);
        validationAlertDialog.setIcon(R.drawable.ic_dialog_alarm);
        validationAlertDialog.setTitle(R.string.txt_warning);
        validationAlertDialog.setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

                return;
            }
        });
    }

    public void showValidationAlertDialog(String errorString) {
        validationAlertDialog.setMessage(errorString);
        validationAlertDialog.show();
    }

    public void showValidationAlertDialog(String title, String errorString) {
        validationAlertDialog.setTitle(title);
        validationAlertDialog.setMessage(errorString);
        validationAlertDialog.show();
    }

    public void initLoadingDialog() {

        if (LoadingDialog == null)
            LoadingDialog = new ProgressDialog(this);
        
        LoadingDialog.setCancelable(false);
        LoadingDialog.setCanceledOnTouchOutside(false);
        
        LoadingDialog.setMessage(getString(R.string.txt_login));
        LoadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    LoadingDialog.cancel();
                    bIsSearchStop = true;
                }
                return false;
            }
        });

    }
    
    
    private class DoBackgroundWork extends AsyncTask<Object, Integer, Integer> {
        protected Integer doInBackground(Object... params) {
			Integer backgroundTaskID = (Integer) params[0];
			
    		switch (backgroundTaskID) {
        		case Constant.BACKGROUND_LOGIN:	
                    try {
                        Request loginRequest = new Request();

                        String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;
                        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                        postData.add(new BasicNameValuePair("action", "login"));
                        postData.add(new BasicNameValuePair("user", mEditTxt_EmailAddress.getText().toString().trim()));
                        postData.add(new BasicNameValuePair("pass", mEditTxt_Password.getText().toString().trim()));
                        postData.add(new BasicNameValuePair("version", Constant.AppVersion));
                        postData.add(new BasicNameValuePair("pro_version", ""));
                        postData.add(new BasicNameValuePair("pcid", "AA2C6A20-E619-51B7-BDF7-7217EC25930E"));
                        String resultXML = loginRequest.httpPost(loginUrlString, postData);

                        if (bIsSearchStop) {
                            bIsSearchStop = false;
                            return Constant.BACKGROUND_PROCESS_OK;
                        }

                        if (resultXML.equals(Constant.ErrorMessage)) {
                            LoadingDialog.dismiss();
                            ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                            return Constant.BACKGROUND_PROCESS_ERROR;
                        } else {
                            mLoginDataParser = new LoginDataParser();
                            mLoginData = mLoginDataParser.parseResponse(resultXML);

                            LoadingDialog.dismiss();

                            if (mLoginData != null) {
                                if (mLoginData.getUserName().length() == 0) {
                                    LoginFailedMessageHandler.sendMessage(LoginFailedMessageHandler.obtainMessage());
                                    return Constant.BACKGROUND_PROCESS_ERROR;
                                } else {
                                    Constant.SessionID = mLoginData.getSessionID();
                                    saveUserInfo(mEditTxt_EmailAddress.getText().toString().trim(), mEditTxt_Password.getText().toString().trim());
                                    showMainActivity();
                                }
                            } else {
                                ConnectionerrorMessageHandler.sendMessage(ConnectionerrorMessageHandler.obtainMessage());
                                return Constant.BACKGROUND_PROCESS_ERROR;
                            }
                        }
                        
                        return Constant.BACKGROUND_PROCESS_OK;
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        LoadingDialog.dismiss();
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
