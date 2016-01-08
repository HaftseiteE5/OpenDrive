package com.opendrive.android.ui;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.LoginData;
import com.opendrive.android.datamodel.SignUpData;
import com.opendrive.android.parser.LoginDataParser;
import com.opendrive.android.parser.SignUpDataParser;
import com.opendrive.android.request.Request;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class LoginActivity extends Activity {

    private RelativeLayout layout = null;
    private LinearLayout loginLayout = null;
    private LinearLayout signupLayout = null;
    private Button loginButton = null;
    private Button signupButton = null;
    private EditText mEditTxt_EmailAddress = null;
    private EditText mEditTxt_Password = null;
    private SignUpData mSignData = null;
    private SignUpDataParser mSignUpDataParser = null;
    private LoginData mLoginData = null;
    private LoginDataParser mLoginDataParser = null;
    private AlertDialog.Builder validationAlertDialog = null;
    private ProgressDialog LoadingDialog = null;
    private boolean bIsSearchStop = false;
    private SharedPreferences settingPrefer;
    private EditText signup_userName = null;
    private EditText signup_firstName = null;
    private EditText signup_lastName = null;
    private EditText signup_email = null;
    private EditText signup_password = null;
    private EditText signup_verifyPassword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        initValidationAlertDialog();
        initLoadingDialog();

        settingPrefer = getSharedPreferences(Constant.PREFS_NAME, 0);

        mEditTxt_EmailAddress = (EditText) findViewById(R.id.editText_EmailAddress);
        mEditTxt_Password = (EditText) findViewById(R.id.editText_Password);
        layout = (RelativeLayout) findViewById(R.id.relativeLayout_Login);
        loginLayout = (LinearLayout) findViewById(R.id.linearLayout_Login);
        signupLayout = (LinearLayout) findViewById(R.id.linearLayout_Signup);
        loginButton = (Button) findViewById(R.id.button_Login);
        signupButton = (Button) findViewById(R.id.button_Signup);

        loginButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                login();
            }
        });

        signupButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                //layout.setBackgroundResource(R.drawable.bg_signup);

                loginLayout.setVisibility(View.GONE);

                if (signupLayout.getVisibility() != View.VISIBLE) {
                    signupLayout.setVisibility(View.VISIBLE);

                    Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
                    animation.setDuration(200);
                    signupLayout.setAnimation(animation);
                } else {
                    signup();
                }

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

    public void login() {

        layout.setBackgroundResource(R.drawable.bg_login);
        signupLayout.setVisibility(View.GONE);

        int visible = loginLayout.getVisibility();

        if (visible == View.VISIBLE) {
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

        } else {
            loginLayout.setVisibility(View.VISIBLE);
        }

    }

    Handler LoginHandler = new Handler() {
        public void handleMessage(Message msg) {

            mEditTxt_EmailAddress.setText(mSignData.getUserName());
            mEditTxt_Password.setText(signup_password.getText().toString().trim());

            login();
        }
    };

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

    public void signup() {
        signup_userName = (EditText) findViewById(R.id.editText_Username);
        signup_firstName = (EditText) findViewById(R.id.editText_Firstname);
        signup_lastName = (EditText) findViewById(R.id.editText_Lastname);
        signup_email = (EditText) findViewById(R.id.editText_emailAddress_signup);
        signup_password = (EditText) findViewById(R.id.editText_password_signup);
        signup_verifyPassword = (EditText) findViewById(R.id.editText_verify_password);

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

    public void showDeleteButton() {

        layout.setVisibility(View.VISIBLE);
        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(300);
        layout.setAnimation(animation);
    }

    public void hideDeleteButton() {

        layout.setVisibility(View.GONE);
        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        animation.setDuration(300);
        layout.setAnimation(animation);

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
            validationAlertDialog = new AlertDialog.Builder(LoginActivity.this);
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
