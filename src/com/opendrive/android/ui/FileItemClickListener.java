package com.opendrive.android.ui;

import com.opendrive.android.datamodel.FileData;

/**
 * Created with IntelliJ IDEA.
 * User: dsurrea
 * Date: 3/2/13
 * Time: 7:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface FileItemClickListener {
    void onClick(FileData data);
    void onBottomButtonClick(int id);
}
