package com.opendrive.android;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey = "dE9qd2FNaEIxZWFwblFBcmRSUXdKeXc6MA") 

public class OpenDriveApplication extends Application {

    private static boolean applicationVisible;
    private static boolean passcodeEntered;

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
    
    public static boolean isPasscodeEntered() {
	return passcodeEntered;
    }

    public static void setPasscodeEntered(boolean passcodeEntered) {
	OpenDriveApplication.passcodeEntered = passcodeEntered;
    }

    public static boolean isApplicationVisible() {
	return applicationVisible;
    }

    public static void applicationResumed() {
	applicationVisible = true;
    }

    public static void applicationPaused() {
	applicationVisible = false;
    }

}
