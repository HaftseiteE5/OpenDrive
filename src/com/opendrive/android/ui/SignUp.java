package com.opendrive.android.ui;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.opendrive.android.datamodel.SignUpData;
import com.opendrive.android.parser.LoginDataParser;
import com.opendrive.android.parser.SignUpDataParser;
import com.opendrive.android.request.Request;
import com.opendrive.android.ui.fragment.FilesFragment;

public class SignUp extends Activity {

    private SignUpData mSignData = null;
    private SignUpDataParser mSignUpDataParser = null;
    // public static LoginData mLoginData = null;
    private LoginDataParser mLoginDataParser = null;
    private AlertDialog.Builder validationAlertDialog = null;
    private ProgressDialog LoadingDialog = null;
    private boolean bIsSearchStop = false;
    private SharedPreferences settingPrefer;

    private EditText signup_userName;
    private EditText signup_firstName;
    private EditText signup_lastName;
    private EditText signup_email;
    private EditText signup_password;
    private EditText signup_verifyPassword;
    private Button loginButton;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.signup);

        signup_userName = (EditText) findViewById(R.id.username);
        signup_firstName = (EditText) findViewById(R.id.firstname);
        signup_lastName = (EditText) findViewById(R.id.lastname);
        signup_email = (EditText) findViewById(R.id.address);
        signup_password = (EditText) findViewById(R.id.password);
        signup_verifyPassword = (EditText) findViewById(R.id.repassword);

        signup_verifyPassword.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    signup();
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

        initValidationAlertDialog();
        initLoadingDialog();

        settingPrefer = getSharedPreferences(Constant.PREFS_NAME, 0);

        loginButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                gotoSign();
            }
        });

        signupButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                signup();
            }
        });

        Utils.overrideFonts(this, findViewById(R.id.llSignUpControls));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    void gotoSign() {
        Intent intent = new Intent(this, LogIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    Handler ConnectionerrorMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            showValidationAlertDialog(getString(R.string.connection_error));
        }
    };

    Handler SignUpFailedMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            showValidationAlertDialog(mSignData.getName(), mSignData.getDescription());
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

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void signup() {

        if (signup_userName.getText().toString().trim().length() == 0) {
            validationAlertDialog.setMessage(R.string.emptyUsername);
            validationAlertDialog.show();

            return;
        }
        if (signup_firstName.getText().toString().trim().length() == 0) {
            validationAlertDialog.setMessage(R.string.emptyfirstname);
            validationAlertDialog.show();
            return;
        }

        if (signup_lastName.getText().toString().trim().length() == 0) {
            validationAlertDialog.setMessage(R.string.emptylastname);
            validationAlertDialog.show();
            return;
        }

        if (signup_email.getText().toString().trim().length() == 0) {
            validationAlertDialog.setMessage(R.string.emptyemail);
            validationAlertDialog.show();
            return;
        }
        if (signup_password.getText().toString().trim().length() == 0) {
            validationAlertDialog.setMessage(R.string.emptyPassword);
            validationAlertDialog.show();
            return;
        }
        if (signup_verifyPassword.getText().toString().trim().length() == 0) {
            validationAlertDialog.setMessage(R.string.emptyverifypassword);
            validationAlertDialog.show();
            return;
        }
        if (!signup_password.getText().toString().trim().equals(signup_verifyPassword.getText().toString().trim())) {
            validationAlertDialog.setMessage(R.string.nomatchpassword);
            validationAlertDialog.show();
            return;
        }

        LoadingDialog.setMessage(getString(R.string.txt_signingup));

        LoadingDialog.show();
        new DoBackgroundWork().execute(new Integer(Constant.BACKGROUND_SIGNUP));  
    }

    Handler LoginHandler = new Handler() {
        public void handleMessage(Message msg) {

            strEmail = mSignData.getUserName();
            strPassword = signup_password.getText().toString().trim();
            // mEditTxt_EmailAddress.setText(mSignData.getUserName());
            // mEditTxt_Password.setText(signup_password.getText().toString().trim());
            login();
        }
    };

    String strEmail = "";
    String strPassword = "";

    public void login() {
        if (strEmail.trim().length() == 0) {
            EmailErrorMessageHandler.sendMessage(EmailErrorMessageHandler.obtainMessage());
            return;
        }
        if (strPassword.trim().length() == 0) {
            PasswordErrorMessageHandler.sendMessage(PasswordErrorMessageHandler.obtainMessage());
            return;
        }

        LoadingDialog.show();
        new DoBackgroundWork().execute(new Integer(Constant.BACKGROUND_LOGIN));  
    }

    // //////////////////LogIn/////////////////
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

    Handler LoginFailedMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            showValidationAlertDialog(LogIn.mLoginData.getName(), LogIn.mLoginData.getDescription());
        }
    };

    // //////////////////SignUp/////////////////////
    public void initValidationAlertDialog() {

        if (validationAlertDialog == null)
            validationAlertDialog = new AlertDialog.Builder(SignUp.this);
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
                        postData.add(new BasicNameValuePair("user", strEmail.trim()));
                        postData.add(new BasicNameValuePair("pass", strPassword.trim()));
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
                            LogIn.mLoginData = null;
                            mLoginDataParser = new LoginDataParser();
                            LogIn.mLoginData = mLoginDataParser.parseResponse(resultXML);

                            LoadingDialog.dismiss();

                            if (LogIn.mLoginData != null) {
                                if (LogIn.mLoginData.getUserName().length() == 0) {
                                    LoginFailedMessageHandler.sendMessage(LoginFailedMessageHandler.obtainMessage());
                                    return Constant.BACKGROUND_PROCESS_ERROR;
                                } else {
                                    Constant.SessionID = LogIn.mLoginData.getSessionID();
                                    saveUserInfo(strEmail.trim(), strPassword.trim());
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
                        			
        		case Constant.BACKGROUND_SIGNUP:
                    try {
                    	Request loginRequest = new Request();

                        String loginUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;

                        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                        postData.add(new BasicNameValuePair("action", "new_profile"));
                        postData.add(new BasicNameValuePair("user_name", signup_userName.getText().toString().trim()));
                        postData.add(new BasicNameValuePair("passwd", signup_password.getText().toString().trim()));
                        postData.add(new BasicNameValuePair("verify_passwd", signup_verifyPassword.getText().toString().trim()));
                        postData.add(new BasicNameValuePair("email", signup_email.getText().toString().trim()));
                        postData.add(new BasicNameValuePair("first_name", signup_firstName.getText().toString().trim()));
                        postData.add(new BasicNameValuePair("last_name", signup_lastName.getText().toString().trim()));
                        postData.add(new BasicNameValuePair("sharewithuserid", ""));
                        postData.add(new BasicNameValuePair("folder_id", ""));

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
                            mSignUpDataParser = new SignUpDataParser();
                            mSignData = mSignUpDataParser.parseResponse(resultXML);

                            LoadingDialog.dismiss();

                            if (mSignData != null) {
                                if (mSignData.getUserName().length() == 0) {
                                    SignUpFailedMessageHandler.sendMessage(SignUpFailedMessageHandler.obtainMessage());
                                    return Constant.BACKGROUND_PROCESS_ERROR;
                                } else {
                                    LoginHandler.sendMessage(LoginHandler.obtainMessage());
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
