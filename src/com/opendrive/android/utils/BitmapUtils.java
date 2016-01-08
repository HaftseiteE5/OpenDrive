package com.opendrive.android.utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;

public class BitmapUtils {
	
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public static int sizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else {
            return data.getByteCount();
        }
    }
}
