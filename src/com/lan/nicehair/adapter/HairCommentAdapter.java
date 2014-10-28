package com.lan.nicehair.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.lan.nicehair.R;
import com.lan.nicehair.activity.HairCommentActivity;
import com.lan.nicehair.activity.PostInfoActivity;
import com.lan.nicehair.common.CircleImageView;
import com.lan.nicehair.common.model.Comment;
import com.lan.nicehair.utils.ImageFetcher;

public class HairCommentAdapter extends BaseAdapter {

	private Context mContext;
	public List<Comment> mListInfo;
	private ImageFetcher mImageFetcher;
	public HairCommentAdapter(Context context,List<Comment> listInfo,ImageFetcher imageFetcher) {
		this.mContext=context;
		this.mListInfo=listInfo;
		this.mImageFetcher=imageFetcher;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListInfo==null?1:mListInfo.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	public void addItemLast(List<Comment> datas) {
   	 	mListInfo.addAll(datas);
    }
	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder holder=null;
		if(convertView==null) {
			convertView=LayoutInflater.from(mContext).inflate(R.layout.hair_scan_comment_item, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}else {
			holder=(ViewHolder) convertView.getTag();
		}
		final Comment com=mListInfo.get(position);
		mImageFetcher.loadImage(com.getHeadUrl(), holder.mHeadIv);	
		holder.mContentTv.setText(com.getContent());
		holder.mTimeTv.setText(com.getTime());
		if(!TextUtils.isEmpty(com.getPicUrl())) {
			holder.mLayout.setVisibility(View.VISIBLE);
			holder.mScanIv.setVisibility(View.VISIBLE);
			mImageFetcher.loadImage(com.getPicUrl(), holder.mScanIv);
			holder.mRightTv.setText(String.valueOf(position));
			holder.mNoRightTv.setText("0");
		}else {
			holder.mLayout.setVisibility(View.GONE);
			holder.mScanIv.setVisibility(View.GONE);
		}
		holder.mSendIv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(HairCommentActivity.HAIR_COMMENT_ACTION);
				intent.putExtra("uid", com.getUid());
				intent.putExtra("name", com.getName());
				mContext.sendBroadcast(intent);
			}
		});
		return convertView;
	}
	class ViewHolder{
		@InjectView(R.id.scan_item_head) CircleImageView mHeadIv;
		@InjectView(R.id.scan_comment_content) TextView mContentTv;
		@InjectView(R.id.scan_item_img) ImageView mScanIv;
		@InjectView(R.id.scan_comment_time) TextView mTimeTv;
		@InjectView(R.id.scan_comment_send) ImageView mSendIv;
		@InjectView(R.id.replay_layout) LinearLayout mLayout;
		@InjectView(R.id.right_num_tv) TextView mRightTv;
		@InjectView(R.id.no_right_num_tv) TextView mNoRightTv;
		@InjectView(R.id.reply_comment_layout) LinearLayout mLayoutMore;
		@InjectView(R.id.reply_more_tv) TextView mMoreTv;
		public ViewHolder(View view) {
			ButterKnife.inject(this,view);
		}
	}
}
