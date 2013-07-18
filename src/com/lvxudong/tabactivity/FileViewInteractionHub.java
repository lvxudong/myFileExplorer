package com.lvxudong.tabactivity;

import java.io.File;
import java.util.ArrayList;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lvxudong.tabactivity.FileOperationHelper.IOperationProgressListener;


public class FileViewInteractionHub implements IOperationProgressListener {

	private IFileInteractionListener mFileView;
	
	private ArrayList<FileItem> mSelectedFileList = new ArrayList<FileItem>();
	
	private ListView mFileListView;
	
	private View mConfirmOperationBar;

	private String currentPath;
	
	private String rootPath;

	private FileSortHelper mFileSortHelper;
	
	private FileOperationHelper mFileOperationHelper;
	
	private Context mContext;
	
	
	public static final int MENU_COPY = 1;
	
	public static final int MENU_MOVE = 2;
	
	public static final int MENU_RENAME = 3;
	
	public static final int MENU_DELETE = 4;
	
	public static final int MENU_INFO = 5;
	

	public FileViewInteractionHub(IFileInteractionListener fileViewListener) {

		mFileView = fileViewListener;
		mFileSortHelper = new FileSortHelper();
		mFileOperationHelper = new FileOperationHelper(this);
		setupFileListView();
		setupOperationPane();
		mContext = mFileView.getContext();

	}
	
    private void setupFileListView() {
        mFileListView = (ListView) mFileView.getViewById(R.id.file_path_list);
        mFileListView.setLongClickable(true);
        //设置长按事件
        mFileListView.setOnCreateContextMenuListener(mListViewContextMenuListener);
        //设置ListItem单击事件
        mFileListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(parent, view, position, id);
            }
        });
    }
    
    
    
    private OnCreateContextMenuListener mListViewContextMenuListener = new OnCreateContextMenuListener() {
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View arg1,
				ContextMenuInfo menuInfo) {
            menu.add(0, MENU_COPY, 0, "复制").setOnMenuItemClickListener(mMenuItemClickListner);
            menu.add(0, MENU_MOVE, 0, "剪切").setOnMenuItemClickListener(mMenuItemClickListner);
            menu.add(0, MENU_RENAME, 0, "重命名").setOnMenuItemClickListener(mMenuItemClickListner);
            menu.add(0, MENU_DELETE, 0, "删除").setOnMenuItemClickListener(mMenuItemClickListner);
            menu.add(0, MENU_INFO, 0, "详细信息").setOnMenuItemClickListener(mMenuItemClickListner);
		}
	};
	
	private OnMenuItemClickListener mMenuItemClickListner = new OnMenuItemClickListener() {
		
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            int pos = info != null ? info.position : -1;
			if(pos != -1){
				FileItem fileItem = mFileView.getItem(pos);
				if(item != null){
					mSelectedFileList.add(fileItem);
				}
			}
			int id = item.getItemId();
			switch(id){
			
				case MENU_COPY:
					onOperationCopy(mSelectedFileList);
					break;
				case MENU_DELETE:
					onOperationDelete();
					break;
				case MENU_INFO:
					onOperationInfo();
					break;
				case MENU_MOVE:
					onOperationMove();
					break;
				case MENU_RENAME:
					onOperationRename();
					break;
			}
			mSelectedFileList.clear();
			
			return false;
		}
	};
	
	
    //单击ListView 中单个 Item 触发函数
    public void onListItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileItem fileItem = mFileView.getItem(position);

        if (fileItem == null) {
            return;
        }
        //选中的为单个文件
        if (!fileItem.isDir) {
            viewFile(fileItem);

        }
        //选中的为目录，就把当前目录 更改为 选择的目录，然后再刷新目录，以显示选中的目录
        currentPath =  getAbsoluteName(currentPath, fileItem.fileName);  
        refreshFileList();
    }
	

    private String getAbsoluteName(String path, String name) {
        return path.equals("/") ? path + name : path + File.separator + name;
    }
    
    private void viewFile(FileItem lFileInfo) {
        try {
            IntentBuilder.viewFile(mContext, lFileInfo.filePath);
        } catch (ActivityNotFoundException e) {
            Log.e("FileEX","fail to view file: " + e.toString());
        }
    }
    
	public void refreshFileList() {
		updateNavigationPane();
		mFileView.onRefreshFileList(currentPath, mFileSortHelper);
		// TODO 更新导航栏操作栏和其他一些UI

	}
	
    private void updateNavigationPane() {
        TextView tvPath = (TextView)mFileView.getViewById(R.id.current_path_view);
        tvPath.setText(currentPath);
    }
    
    public boolean onBackPressed() {
        if (!rootPath.equals(currentPath)) {
            currentPath = new File(currentPath).getParent();
            refreshFileList();
            return true;
        }

        return false;
    }
    
    
    public void onOperationCopy(ArrayList<FileItem> files) {
        mFileOperationHelper.Copy(files);

        showConfirmOperationBar(true);
        //View confirmButton = mConfirmOperationBar.findViewById(R.id.button_moving_confirm);
        //confirmButton.setEnabled(false);
        // refresh to hide selected files
        refreshFileList();
    }
    
    public void onOperationMove() {
        mFileOperationHelper.StartMove(mSelectedFileList);
        showConfirmOperationBar(true);
        refreshFileList();
    }
    
    public void onOperationDelete() {
        doOperationDelete(mSelectedFileList);
    }
    
    private void doOperationDelete(final ArrayList<FileItem> selectedFileList) {
        final ArrayList<FileItem> selectedFiles = new ArrayList<FileItem>(selectedFileList);
        Dialog dialog = new AlertDialog.Builder(mContext)
                .setMessage("确认删除")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mFileOperationHelper.Delete(selectedFiles);
                        }
                    }
                ).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();
        dialog.show();
    }
    
    public void onOperationInfo() {
        if (mSelectedFileList.size() == 0)
            return;

        FileItem file = mSelectedFileList.get(0);
        if (file == null)
            return;

        InformationDialog dialog = new InformationDialog(mContext, file);
        dialog.show();
    }
    
    
    
    public void onOperationRename() {

        if (mSelectedFileList.size() == 0)
            return;

        final FileItem f = mSelectedFileList.get(0);

        TextInputDialog dialog = new TextInputDialog(mContext, "重命名",
                "请输入文件名", f.fileName, new TextInputDialog.OnFinishListener() {
                    @Override
                    public boolean onFinish(String text) {
                        return doRename(f, text);
                    }

                });

        dialog.show();
    }

    private boolean doRename(final FileItem f, String text) {
        if (TextUtils.isEmpty(text))
            return false;

        if (mFileOperationHelper.Rename(f, text)) {
            f.fileName = text;
            mFileView.onDataChanged();
        } else {
            new AlertDialog.Builder(mContext).setMessage("重命名失败")
                    .setPositiveButton("确认", null).create().show();
            return false;
        }

        return true;
    }
    
    private void setupOperationPane() {
        mConfirmOperationBar = mFileView.getViewById(R.id.moving_operation_bar);
        ((Button)mConfirmOperationBar.findViewById(R.id.button_moving_confirm)).setOnClickListener(mButtonClickListner);
        ((Button)mConfirmOperationBar.findViewById(R.id.button_moving_cancel)).setOnClickListener(mButtonClickListner);
    }

    private OnClickListener mButtonClickListner = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.button_moving_confirm:
				onOperationButtonConfirm();
				break;
			case R.id.button_moving_cancel:
				onOperationButtonCancel();
				break;
			}
			
		}
	};
    
    public void onOperationButtonConfirm() {

        if (mFileOperationHelper.isMoveState()) {
            mFileOperationHelper.EndMove(currentPath);
        } else {
        	mFileOperationHelper.Paste(currentPath);
        }
    }
    
    public void onOperationButtonCancel() {
        mFileOperationHelper.clear();
        showConfirmOperationBar(false);
        if (mFileOperationHelper.isMoveState()) {
            mFileOperationHelper.EndMove(null);
            refreshFileList();
        } else {
            refreshFileList();
        }
    }
	
    private void showConfirmOperationBar(boolean enable){
    	mConfirmOperationBar.setVisibility(enable ? View.VISIBLE : View.GONE);
    }
    
    
	public boolean isMoveState() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isFileSelected(String path) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public void setCurrentPath(String currentPath2) {
		currentPath = currentPath2;
	}

	public FileItem getItem(int position) {
		return mFileView.getItem(position);
	}

	@Override
	public void onFinish() {
		//refreshFileList();
        mFileView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showConfirmOperationBar(false);
                refreshFileList();
            }
        });
	}

	@Override
	public void onFileChanged(String path) {
		// TODO Auto-generated method stub
		
	}
}
