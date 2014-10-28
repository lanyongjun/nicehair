package com.lan.nicehair.adapter;

import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.lan.nicehair.R;
import com.lan.nicehair.common.ScaleImageView;
import com.lan.nicehair.common.model.FindHairItem;
import com.lan.nicehair.utils.ImageFetcher;
import com.lan.nicehair.utils.ImageWorker;
import com.lan.nicehair.waterfall.widget.XListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FindHairAdapter extends BaseAdapter {

	 private Context mContext;
     public LinkedList<FindHairItem> mListInfos;
     private ImageFetcher mImageFetcher;

     public FindHairAdapter(Context context, ImageFetcher imageFetcher) {
         mContext = context;
         mListInfos = new LinkedList<FindHairItem>();
         mImageFetcher = imageFetcher;
     }

     @Override
     public int getCount() {
         return mListInfos.size();
     }

     @Override
     public Object getItem(int arg0) {
         return mListInfos.get(arg0);
     }

     @Override
     public long getItemId(int arg0) {
         return 0;
     }

     public void addItemLast(List<FindHairItem> datas) {
    	 mListInfos.addAll(datas);
     }

     public void addItemTop(List<FindHairItem> datas) {
         for (FindHairItem info : datas) {
        	 mListInfos.addFirst(info);
         }
     }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder=null;
		if(convertView==null) {
			convertView=LayoutInflater.from(mContext).inflate(R.layout.poster_item_two, null);
			holder=new ViewHolder(convertView);
			convertView.setTag(holder);
		}else {
			holder=(ViewHolder) convertView.getTag();
		}
		FindHairItem info=mListInfos.get(position);
		holder.poster_scanNumTv.setText(String.valueOf(info.getLookCount()));
		holder.poster_praiseNumTv.setText(String.valueOf(info.getPariseCount()));
		holder.poster_commentNumTv.setText(String.valueOf(info.getChatCount()));
		holder.poster_nameTv.setText(info.getTitle());
        mImageFetcher.loadImage(info.getPicUrl(), holder.imageView);
		return convertView;
	}
	public class ViewHolder{
		@InjectView(R.id.poster_item_displayIv) ImageView imageView;
		@InjectView(R.id.poster_scanNumTv) TextView poster_scanNumTv;
		@InjectView(R.id.poster_praiseNumTv) TextView poster_praiseNumTv;
		@InjectView(R.id.poster_commentNumTv) TextView poster_commentNumTv;
		@InjectView(R.id.poster_nameTv) TextView poster_nameTv;
		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

}
