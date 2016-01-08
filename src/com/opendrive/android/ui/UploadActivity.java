package com.opendrive.android.ui;

import java.util.ArrayList;

import com.opendrive.android.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class UploadActivity extends Activity {

    UploadActivity instance;
    private int REQUEST_GALLERY = 0;
    private int REQUEST_EXPLORER = 1;

    private Button btnGallery;
    private Button btnOthers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploadfile);

        instance = this;
        btnGallery = (Button) findViewById(R.id.btn_gallery);
        btnGallery.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_GALLERY);
            }
        });

        btnOthers = (Button) findViewById(R.id.btn_others);
        btnOthers.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (EventHandler.mMultiSelectData == null)
                    EventHandler.mMultiSelectData = new ArrayList<String>();

                MainActivity.m_bLoading = true;
                EventHandler.mMultiSelectData.clear();
                Intent intent = new Intent(instance, ExplorerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, REQUEST_EXPLORER);
                startActivity(intent);
                finish();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY) {

            if (data == null)
                return;

            Uri uri = data.getData();

            String imagePath = getRealPathFromURI(uri);

            if (imagePath.length() > 0)
                MainActivity.Instance.uploadImage(imagePath);
            finish();
        } else {

        }
    }

    public String getRealPathFromURI(Uri contentUri) {

        // can post image
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, // Which columns to return
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }
}
