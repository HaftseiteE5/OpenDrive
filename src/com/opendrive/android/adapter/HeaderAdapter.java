package com.opendrive.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.opendrive.android.R;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: dsurrea
 * Date: 3/7/13
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class HeaderAdapter extends ArrayAdapter<String> {

    private final int mLayoutRes;
    private final LayoutInflater mInflater;

    public HeaderAdapter(Context context, int resource
                        ) {
        super(context, resource);
        this.mLayoutRes = resource;

        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(mLayoutRes, parent, false);

        String title = getItem(position);
        TextView textView = (TextView) convertView.findViewById(R.id.title);
        textView.setText(title);


        return convertView;
    }


}
