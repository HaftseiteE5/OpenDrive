package com.opendrive.android.utils;

public class FileUtils {
	public static String getExtention(String name) {

		String[] array = name.split("\\.");
		if (array.length != 0) {
			return array[array.length - 1];
		} else {
			return "";
		}

	}
}
