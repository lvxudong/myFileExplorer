package com.lvxudong.tabactivity;

import java.util.List;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileItemListAdapter extends ArrayAdapter {
	
	private LayoutInflater mInflater;
	private Context mContext;
	FileViewInteractionHub mFileViewInteractionHub;

	public FileItemListAdapter(Context context,int resource, List<FileItem> obj ,FileViewInteractionHub f) {
		super(context,resource,obj);
		mContext = context;
		mFileViewInteractionHub = f;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(R.layout.file_item, parent, false);
        }
		
        FileItem fileItem = mFileViewInteractionHub.getItem(position);
		
        ImageView fileImage = (ImageView) view.findViewById(R.id.file_image);
        if(fileItem.isDir){
        	fileImage.setImageResource(R.drawable.ex_folder);
        }else{
        //	mFileIconHelper.setIcon(fileItem, fileImage, null);
        	fileImage.setImageResource(R.drawable.ex_doc);
        }
		((TextView) view.findViewById(R.id.modified_time)).setText(Util
				.formatDateString(mContext, fileItem.modifyDate));
		
		((TextView) view.findViewById(R.id.file_size)).setText(Util
				.convertStorage(fileItem.fileSize));
		
		((TextView)view.findViewById(R.id.file_name)).setText(fileItem.fileName);

		return view;
	}

}
