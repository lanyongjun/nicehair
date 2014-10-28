package com.lan.nicehair.adapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.lan.nicehair.R;
import com.lan.nicehair.activity.PostInfoActivity;
import com.lan.nicehair.common.CircleImageView;
import com.lan.nicehair.common.model.Comment;
import com.lan.nicehair.utils.ImageFetcher;
import com.lan.nicehair.utils.Utils;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PostAdapter extends BaseAdapter {

	private List<Comment> mListComment;
	private Context mContext;
	private ImageFetcher mImageFetcher;
	private int color;
	public PostAdapter(Context context,List<Comment> ListComment) {
		this.mContext=context;
		this.mListComment=ListComment;
		mImageFetcher=new ImageFetcher(mContext, 200);
		mImageFetcher.setLoadingImage(R.drawable.zone_list_profile);
		color=context.getResources().getColor(R.color.zone_list_username);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListComment==null?1:mListComment.size();
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder=null;
		if(convertView==null) {
			convertView=LayoutInflater.from(mContext).inflate(R.layout.post_info_comment_item, null);
			holder=new ViewHolder(convertView);
			convertView.setTag(holder);
		}else {
			holder=(ViewHolder) convertView.getTag();
		}
		final Comment com=mListComment.get(position);
		mImageFetcher.loadImage(com.getHeadUrl(), holder.mPostHeadIv);
		holder.mPostNameTv.setText(com.getName());
		holder.mPostTimeTv.setText(com.getTime());
		String content=com.getContent();
		holder.mPostContent.setText(content);
		Utils.addLinks(color,"",holder.mPostContent);
		holder.mPostChat.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(PostInfoActivity.POST_RECEIVED_ACTION);
				intent.putExtra("uid", com.getUid());
				intent.putExtra("name", com.getName());
				mContext.sendBroadcast(intent);
			}
		});
		return convertView;
	}

	class ViewHolder{
		@InjectView(R.id.post_info_head) CircleImageView mPostHeadIv;
		@InjectView(R.id.post_info_username) TextView mPostNameTv;
		@InjectView(R.id.post_addtime) TextView mPostTimeTv;
		@InjectView(R.id.post_comment_huifu) TextView mPostChat;
		@InjectView(R.id.post_content) TextView mPostContent;
		public ViewHolder(View view) {
			ButterKnife.inject(this,view);
		}
	}
}
