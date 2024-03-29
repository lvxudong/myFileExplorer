/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lvxudong.tabactivity;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

public class InformationDialog extends AlertDialog {
    protected static final int ID_USER = 100;
    private FileItem mFileInfo;
    private Context mContext;
    private View mView;

    public InformationDialog(Context context, FileItem f) {
        super(context);
        mFileInfo = f;
        mContext = context;
    }

    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.information_dialog, null);
        setTitle(mFileInfo.fileName);

        ((TextView) mView.findViewById(R.id.information_size))
                .setText(formatFileSizeString(mFileInfo.fileSize));
        ((TextView) mView.findViewById(R.id.information_location))
                .setText(mFileInfo.filePath);
        ((TextView) mView.findViewById(R.id.information_modified)).setText(Util
                .formatDateString(mContext, mFileInfo.modifyDate));
        ((TextView) mView.findViewById(R.id.information_canread))
                .setText(mFileInfo.canRead ? "是" : "否");
        ((TextView) mView.findViewById(R.id.information_canwrite))
                .setText(mFileInfo.canWrite ? "是" : "否");

        setView(mView);
        setButton(BUTTON_NEGATIVE,"确定", (DialogInterface.OnClickListener) null);

        super.onCreate(savedInstanceState);
    }

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ID_USER:
                    Bundle data = msg.getData();
                    long size = data.getLong("SIZE");
                    ((TextView) mView.findViewById(R.id.information_size)).setText(formatFileSizeString(size));
            }
        };
    };

    private AsyncTask task;

    @SuppressWarnings("unchecked")
    private void asyncGetSize() {
        task = new AsyncTask() {
            private long size;

            @Override
            protected Object doInBackground(Object... params) {
                String path = (String) params[0];
                size = 0;
                getSize(path);
                task = null;
                return null;
            }

            private void getSize(String path) {
                if (isCancelled())
                    return;
                File file = new File(path);
                if (file.isDirectory()) {
                    File[] listFiles = file.listFiles();
                    if (listFiles == null)
                        return;

                    for (File f : listFiles) {
                        if (isCancelled())
                            return;

                        getSize(f.getPath());
                    }
                } else {
                    size += file.length();
                    onSize(size);
                }
            }

        }.execute(mFileInfo.filePath);
    }

    private void onSize(final long size) {
        Message msg = new Message();
        msg.what = ID_USER;
        Bundle bd = new Bundle();
        bd.putLong("SIZE", size);
        msg.setData(bd);
        mHandler.sendMessage(msg); // 向Handler发送消息,更新UI
    }

    private String formatFileSizeString(long size) {
        String ret = "";
        if (size >= 1024) {
            ret = Util.convertStorage(size);
            ret += (" (" + mContext.getResources().getString(R.string.file_size, size) + ")");
        } else {
            ret = mContext.getResources().getString(R.string.file_size, size);
        }

        return ret;
    }
}
