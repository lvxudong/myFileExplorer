package com.lvxudong.tabactivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.media.audiofx.BassBoost.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lvxudong.tabactivity.FileOperationHelper.IOperationProgressListener;
import com.lvxudong.tabactivity.MainActivity.IBackPressedListener;

public class FileExpoloerActivity extends Fragment implements
		 IBackPressedListener, IOperationProgressListener,IFileInteractionListener {

	

	private View mRootView;
	private ListView mFileListView;
	
	private Activity mActivity;
	
	FileItemListAdapter mAdapter;
	
	private String rootPath;
	private static final String sdDir = Util.getSdDirectory();
	private String currentPath;
	private String mPreviousPath;
	
	private ArrayList <FileItem> mCheckedFileList = new ArrayList<FileItem>();
	private ArrayList<FileItem> mFileList = new ArrayList<FileItem>();
	
	private FileOperationHelper mFileOperationHelper;
	private FileSortHelper mFileSortHelper;
	private FileViewInteractionHub mFileViewInteractionHub;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.activity_main, container, false);
		mActivity = getActivity();
		
		//初始化帮助类
		mFileOperationHelper = new FileOperationHelper(this);
		mFileSortHelper = new FileSortHelper();
		mFileViewInteractionHub = new FileViewInteractionHub(this);
		
		mFileViewInteractionHub.setRootPath(sdDir);
		
		currentPath = sdDir;
		
		mFileViewInteractionHub.setCurrentPath(currentPath);
	
		//初始化mFileListView
		mFileListView = (ListView) mRootView.findViewById(R.id.file_path_list);
		mAdapter = new FileItemListAdapter(mActivity,R.layout.file_item,mFileList,mFileViewInteractionHub);
		mFileListView.setAdapter(mAdapter);
		mFileViewInteractionHub.refreshFileList();
		setHasOptionsMenu(true);
		
		return mRootView;
	}
	

	
    public void refresh() {
        if (mFileViewInteractionHub != null) {
            mFileViewInteractionHub.refreshFileList();
        }
    }
	
	private boolean isInSelection(){
		
		return false;
		
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFileChanged(String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Context getContext() {
		return mActivity;
	}

	@Override
	public boolean onRefreshFileList(String path, FileSortHelper sort) {
		File file = new File(path);
		if(!file.exists() || !file.isDirectory()){
			return false;
		}
		
       // final int pos = computeScrollPosition(path); 
		
        ArrayList<FileItem> fileList = mFileList;
        fileList.clear();

        File[] listFiles = file.listFiles();
        if (listFiles == null)
            return true;

        for (File child : listFiles) {
            // do not show selected file if in move state
        	//在移动文件状态下，不显示被移动的文件
            if (mFileViewInteractionHub.isMoveState() && mFileViewInteractionHub.isFileSelected(child.getPath()))
                continue;

            String absolutePath = child.getAbsolutePath();
            if (Util.isNormalFile(absolutePath)) {
                FileItem lFileItem = Util.GetFileInfo(child);
                if (lFileItem != null) {
                    fileList.add(lFileItem);
                }
            }
        }
        //对当前 mFileNameList 进行排序，同时通知  适配器  数据改变，重新显示
        sortCurrentList(sort);
//        showEmptyView(fileList.size() == 0);
        mFileListView.post(new Runnable() { //在 ListView 的UI 线程上运行
            @Override
            public void run() {
                //mFileListView.setSelection(pos);
            }
        });
        return true;
		
	}


	@Override
	public FileItem getItem(int pos) {
		if (pos < 0 || pos > mFileList.size() - 1)
            return null;

        return mFileList.get(pos);
	}


	@Override
	public void sortCurrentList(FileSortHelper sort) {
		Collections.sort(mFileList, sort.getComparator());
        onDataChanged();
	}


	@Override
	public void onDataChanged() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }

        });
	}


	@Override
	public void runOnUiThread(Runnable r) {
		mActivity.runOnUiThread(r);
	}


	@Override
	public boolean onBack() {
		
		return mFileViewInteractionHub.onBackPressed();
	}


	@Override
	public View getViewById(int id) {
		return mRootView.findViewById(id);
	}
		
	}
