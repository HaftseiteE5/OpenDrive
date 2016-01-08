package com.opendrive.android.utils;

import android.app.Activity;
import android.text.ClipboardManager;

public class SystemUtils {
	
	public static final void copyToClipboard(Activity activity, String text) {
		ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);
		clipboard.setText(text);
	}

}
