package com.lvxudong.tabactivity;

import android.content.Context;
import android.view.View;

public interface IFileInteractionListener {
	
	public View getViewById(int id);
	
	public Context getContext();
	
	public boolean onRefreshFileList(String path, FileSortHelper sortHelper);
	
	public FileItem getItem(int pos);
	
	public void sortCurrentList(FileSortHelper sort);
	
	public void onDataChanged();
	
	public void runOnUiThread(Runnable r);

}
