package com.opendrive.android.utils;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.AsyncTaskLoader;


public abstract class ArrayListAsyncLoader<T> extends AsyncTaskLoader<ArrayList<T>> {

	// type of the published values
	public static int MSGCODE_PROGRESS = 1;
	public static int MSGCODE_MESSAGE = 2;

	ArrayList<T> mFeatures = null;
	

	private Handler handler;

	public ArrayListAsyncLoader(Context context) {
		super(context);
	}
    
	@Override
	public abstract ArrayList<T> loadInBackground();

	/**
	 * Starts an asynchronous load of the contacts list data. When the result is
	 * ready the callbacks will be called on the UI thread. If a previous load
	 * has been completed and is still valid the result may be passed to the
	 * callbacks immediately.
	 *
	 * Must be called from the UI thread
	 */
	@Override
	protected void onStartLoading() {
		if (mFeatures != null) {
			deliverResult(mFeatures);
		}
		if (takeContentChanged() || mFeatures == null) {
			forceLoad();
		}
	}

	/**
	 * Must be called from the UI thread
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		mFeatures = null;
	}

	/* Runs on the UI thread */
	@Override
	public void deliverResult(ArrayList<T> items) {
		if (isStarted()) {
			super.deliverResult(items);
		}

	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	protected void publishMessage(String value) {

		if (handler != null) {

			Bundle data = new Bundle();
			data.putString("message", value);

			/* Creating a message */
			Message msg = new Message();
			msg.setData(data);
			msg.what = MSGCODE_MESSAGE;

			/* Sending the message */
			handler.sendMessage(msg);

		}

	}

}
