package com.opendrive.android.adapter;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.opendrive.android.R;
import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.FileData;
import com.opendrive.android.request.Request;
import com.opendrive.android.ui.fragment.FilesFragment;

public class FileListAdapter extends BaseAdapter {

    LruCache<String, Bitmap> mLruCache;

    public ArrayList<FileData> items;
    private Context context;
    private FilesFragment mFilesFragment;
    private LayoutInflater inflater;

    private IItemListener mIItemListener;

    public FileListAdapter(Context context, int textViewResourceId, ArrayList<FileData> items, FilesFragment fragment, IItemListener itemListener) {
        super();
        mFilesFragment = fragment;
        this.context = context;
        this.items = items;
        this.mIItemListener = itemListener;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = 1024 * 1024 * memClass / 8;

        mLruCache = new LruCache<String, Bitmap>(cacheSize) {

            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in bytes rather than number
                // of items.

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    return bitmap.getByteCount();
                } else {
                    return bitmap.getRowBytes() * bitmap.getHeight();
                }

            }
        };

    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;

        final FileData fileItem = items.get(position);

        if (convertView == null || convertView.getTag() == null) {
            convertView = inflater.inflate(R.layout.fileitem, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.copyLink = (LinearLayout) convertView.findViewById(R.id.copylink_view);
            viewHolder.share = (LinearLayout) convertView.findViewById(R.id.share_view);
            viewHolder.offline = (LinearLayout) convertView.findViewById(R.id.save_offline_view);
            viewHolder.rename = (LinearLayout) convertView.findViewById(R.id.rename_view);
            viewHolder.move = (LinearLayout) convertView.findViewById(R.id.move_view);
            viewHolder.trash = (LinearLayout) convertView.findViewById(R.id.trash_view);

            viewHolder.menuitem = (LinearLayout) convertView.findViewById(R.id.menuitem);

            viewHolder.txt_save_for_offline = (TextView) viewHolder.offline.findViewById(R.id.txt_save_for_offline);
            viewHolder.img_save_for_offline = (ImageView) viewHolder.offline.findViewById(R.id.img_save_for_offline);

            viewHolder.imageView_fileThumb = (ImageView) convertView.findViewById(R.id.imageView_fileThumb);
            viewHolder.txtView_fileName = (TextView) convertView.findViewById(R.id.txtView_fileName);
            viewHolder.fileProperty = (TextView) convertView.findViewById(R.id.txtView_fileProperty);
            viewHolder.filePropertyButton = (LinearLayout) convertView.findViewById(R.id.linearLayout_fileProperty);
            viewHolder.accessoryView = (ImageView) viewHolder.filePropertyButton.findViewById(R.id.imageView1);

            viewHolder.cbMove = (CheckBox) convertView.findViewById(R.id.cbMove);
            viewHolder.ibConfirmMove = (ImageButton) convertView.findViewById(R.id.ibConfirmMove);
            if (mFilesFragment.getMode() == FilesFragment.MODE_SELECT_AUTO_UPLOAD_FOLDER) {
                viewHolder.ibConfirmMove.setImageDrawable(context.getResources().getDrawable(R.drawable.btn_select));
            }

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

	/*
     * if (fileItem.isShowActionMenu()) { viewHolder.menuitem.setVisibility(View.VISIBLE); } else { viewHolder.menuitem.setVisibility(View.GONE); }
	 */

        if (fileItem != null) {

            // String fileFullPath = Utils.getFolderFullPath(fileItem);
            // File file1 = new File(fileFullPath);
            //
            // if (file1.exists()) {
            //
            // viewHolder.txt_save_for_offline.setText(R.string.label_deleteoffline);
            // viewHolder.img_save_for_offline.setImageResource(R.drawable.ic_delete);
            //
            // } else {
            // viewHolder.txt_save_for_offline.setText(R.string.label_saveforoffline);
            // viewHolder.img_save_for_offline.setImageResource(R.drawable.ic_save);
            // }
            //
            // viewHolder.copyLink.setOnClickListener(new View.OnClickListener() {
            // @Override
            // public void onClick(View arg0) {
            // mFilesFragment.actionHandler(arg0, fileItem, items.indexOf(fileItem));
            // }
            // });
            // viewHolder.share.setOnClickListener(new View.OnClickListener() {
            // @Override
            // public void onClick(View arg0) {
            // mFilesFragment.actionHandler(arg0, fileItem, items.indexOf(fileItem));
            // }
            // });
            // viewHolder.offline.setOnClickListener(new View.OnClickListener() {
            // @Override
            // public void onClick(View arg0) {
            // mFilesFragment.actionHandler(arg0, fileItem, items.indexOf(fileItem));
            // }
            // });
            // viewHolder.trash.setOnClickListener(new View.OnClickListener() {
            // @Override
            // public void onClick(View arg0) {
            // mFilesFragment.actionHandler(arg0, fileItem, items.indexOf(fileItem));
            // }
            // });
            // viewHolder.move.setOnClickListener(new View.OnClickListener() {
            // @Override
            // public void onClick(View v) {
            // mFilesFragment.actionHandler(v, fileItem, items.indexOf(fileItem));
            // }
            // });
            // viewHolder.rename.setOnClickListener(new View.OnClickListener() {
            // @Override
            // public void onClick(View v) {
            // mFilesFragment.actionHandler(v, fileItem, items.indexOf(fileItem));
            // }
            // });

            viewHolder.txtView_fileName.setTextColor(Color.BLACK);

            if ((mFilesFragment.getMode() == FilesFragment.MODE_MOVE_FILE || mFilesFragment.getMode() == FilesFragment.MODE_SELECT_AUTO_UPLOAD_FOLDER) && fileItem.getIsFolder()) {
                viewHolder.cbMove.setVisibility(View.VISIBLE);
                if (fileItem.isCheckedForMove()) {
                    viewHolder.cbMove.setChecked(true);
                    viewHolder.ibConfirmMove.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.cbMove.setChecked(false);
                    viewHolder.ibConfirmMove.setVisibility(View.GONE);
                }

                viewHolder.cbMove.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        for (FileData item : items) {
                            item.setCheckedForMove(false);
                        }
                        if (viewHolder.cbMove.isChecked()) {
                            fileItem.setCheckedForMove(true);
                        }
                        notifyDataSetChanged();
                    }
                });

                viewHolder.ibConfirmMove.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (fileItem.isCheckedForMove()) {
                            mIItemListener.onItemClick(position, fileItem, v.getTop(), v.getHeight(), mFilesFragment.getMode());
                        }
                    }
                });

            } else {
                viewHolder.ibConfirmMove.setVisibility(View.GONE);
                viewHolder.cbMove.setVisibility(View.GONE);
            }

            if (fileItem.isShowActionMenu())
                viewHolder.accessoryView.setImageResource(R.drawable.less);
            else
                viewHolder.accessoryView.setImageResource(R.drawable.more);

            if (fileItem.getIsFolder()) {

                viewHolder.fileProperty.setVisibility(View.GONE);
                if (fileItem.getID().equals("0")) {
                    viewHolder.imageView_fileThumb.setImageResource(R.drawable.icon);
                } else {
                    viewHolder.imageView_fileThumb.setImageResource(R.drawable.folder);
                }
                viewHolder.txtView_fileName.setText(fileItem.getName());
                viewHolder.filePropertyButton.setVisibility(View.GONE);

            } else {

                viewHolder.txtView_fileName.setText(fileItem.getName());

                // if (!fileItem.isDownloaded()) {
                // if (isEnable(fileItem)) {
                // fileItem.setDownloaded(true);
                // viewHolder.txtView_fileName.setTextColor(Color.BLACK);
                // } else {
                // fileItem.setDownloaded(false);
                // viewHolder.txtView_fileName.setTextColor(Color.GRAY);
                // }
                // }
                viewHolder.fileProperty.setVisibility(View.VISIBLE);
                viewHolder.fileProperty.setText(fileItem.getSize() + " | " + Utils.getDateFromTimeStamp(fileItem.getDateModified()) + " | " + Utils.getAccessStringByCode(context, fileItem.getAccess()));
                viewHolder.filePropertyButton.setVisibility(View.VISIBLE);

                final View v = convertView;
                viewHolder.filePropertyButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {

                        mIItemListener.onItemClick(position, fileItem, v.getTop(), v.getHeight(), FilesFragment.MODE_VIEW);

			/*
             * if (viewHolder.menuitem.getVisibility() == View.VISIBLE) { viewHolder.menuitem.setVisibility(View.GONE); fileItem.setShowActionMenu(false); viewHolder.accessoryView .setImageResource(R.drawable.more);
			 * 
			 * 
			 * } else { viewHolder.menuitem.setVisibility(View.VISIBLE); fileItem.setShowActionMenu(true); viewHolder.accessoryView .setImageResource(R.drawable.less); }
			 */
                        notifyDataSetChanged();

                    }
                });

            }

            if (fileItem.getID().equals("0")) {
                viewHolder.imageView_fileThumb.setImageResource(R.drawable.icon);
            } else {
                Bitmap mIcon = mLruCache.get(fileItem.getID());
                if (mIcon == null) {
                    loadBitmap(position, viewHolder.imageView_fileThumb, fileItem, true);
                } else {
                    viewHolder.imageView_fileThumb.setImageBitmap(mIcon);
                }
            }
        }

        convertView.setBackgroundColor(position % 2 == 0 ? context.getResources().getColor(R.color.list_item_1) : context.getResources().getColor(R.color.list_item_2));

        return convertView;
    }

    public void setImage(int position, ImageView imageView, FileData fileData, boolean compressed) {
        Bitmap mIcon = mLruCache.get(fileData.getID());
        if (mIcon == null) {
            loadBitmap(position, imageView, fileData, compressed);
        } else {
            imageView.setImageBitmap(mIcon);
        }
    }

    private static class ViewHolder {
        LinearLayout copyLink;
        LinearLayout share;
        LinearLayout offline;
        LinearLayout trash;
        LinearLayout move;
        LinearLayout rename;

        LinearLayout menuitem;

        TextView txt_save_for_offline;
        ImageView img_save_for_offline;

        ImageView imageView_fileThumb;
        TextView txtView_fileName;
        TextView fileProperty;
        LinearLayout filePropertyButton;
        ImageView accessoryView;
        CheckBox cbMove;
        ImageButton ibConfirmMove;
    }

    public void loadBitmap(int resId, ImageView imageView, FileData filedate, boolean compressed) {
        if (cancelPotentialWork(resId, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView, filedate, compressed);
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.placeholder);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), icon, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(resId);
        }
    }

    public boolean isEnable(FileData fileItem) {
        String fileFullPath = "";
        if (FilesFragment.sOfflineMode) {
            fileFullPath = mFilesFragment.getItemFullPathIfOfflineMode(fileItem);
        } else {
            fileFullPath = Utils.getFolderFullPath(fileItem);
        }
        File file1 = new File(fileFullPath);
        return file1.exists();
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;
        private final FileData fileData;
        private final boolean compressed = true;

        public BitmapWorkerTask(ImageView imageView, FileData fileItem, boolean compressed) {
            // Use a WeakReference to ensure the ImageView can be garbage
            // collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            fileData = fileItem;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            data = params[0];

            if (fileData.getIsFolder()) {
                Utils.makeFolders(fileData);
            }

            Bitmap iconBitmap = null;
            if (fileData.getIsFolder())
                iconBitmap = Utils.getIconFromFileID(fileData.getID(), compressed);
            else
                iconBitmap = Utils.getIconFromFileID(fileData.getID(), compressed);

            if (iconBitmap == null) {
                Request getIconRequest = new Request();

                String getIconUrlString = Constant.ServerURL + Constant.APIPath + Constant.APIName_OperationPath;

                ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

                postData.add(new BasicNameValuePair("action", "get_thumbnail"));
                postData.add(new BasicNameValuePair("session_id", Constant.SessionID));
                postData.add(new BasicNameValuePair("shared_user_id", ""));

                if (fileData.getIsFolder()) {
                    postData.add(new BasicNameValuePair("file_id", ""));
                    postData.add(new BasicNameValuePair("dir_id", fileData.getID()));
                } else {
                    postData.add(new BasicNameValuePair("file_id", fileData.getID()));
                    postData.add(new BasicNameValuePair("dir_id", ""));
                }

                try {
                    if (fileData.getIsFolder())
                        iconBitmap = getIconRequest.getFileIcon(getIconUrlString, postData, fileData.getID());
                    else
                        iconBitmap = getIconRequest.getFileIcon(getIconUrlString, postData, fileData.getID());

                    fileData.setIcon(iconBitmap);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            return iconBitmap;

        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap.recycle();
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                mLruCache.put(fileData.getID(), bitmap);
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(int data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final int bitmapData = bitmapWorkerTask.data;
            if (bitmapData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was
        // cancelled
        return true;
    }

    public interface IItemListener {

        public void onItemClick(int position, FileData fileData, int posY, int height, int mode);

    }

    @Override
    public void notifyDataSetChanged() {
//        if ((mFilesFragment.getMode() == FilesFragment.MODE_MOVE_FILE || mFilesFragment.getMode() == FilesFragment.MODE_SELECT_AUTO_UPLOAD_FOLDER)) {
//            for(FileData data : items) {
//                if(data.getID().equals("0")) {
//                    return;
//                }
//            }
//            if (LogIn.mLoginData != null && LogIn.mLoginData.getIsAccessUser().equals("False")) {
//                FileData rootFileData = new FileData();
//                rootFileData.setIsFolder(true);
//                rootFileData.setName(context.getString(R.string.app_name));
//                rootFileData.setID("0");
//                items.add(0, rootFileData);
//            }
//        }
        super.notifyDataSetChanged();
    }
}
