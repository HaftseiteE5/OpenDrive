package com.opendrive.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.opendrive.android.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: dsurrea
 * Date: 3/7/13
 * Time: 4:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SettingsItemAdapter extends ArrayAdapter<HashMap<String, String>> {

    private final int mLayoutRes;
    private final ArrayList<HashMap<String, String>> mList;
    private final LayoutInflater mInflater;

    public SettingsItemAdapter(Context context, int resource,
                         ArrayList<HashMap<String, String>> objects) {
        super(context, resource, objects);
        this.mLayoutRes = resource;
        mList = objects;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(mLayoutRes, parent, false);

        HashMap title = mList.get(position);
        TextView textView = (TextView) convertView.findViewById(R.id.title);
        textView.setText((String)title.get("title"));
        TextView valueView = (TextView) convertView.findViewById(R.id.value);
        valueView.setText((String)title.get("value"));


        return convertView;
    }
}
