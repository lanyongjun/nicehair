package com.lan.nicehair.adapter;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.lan.nicehair.activity.MainActivity;
import com.lan.nicehair.activity.PhotoPagerActivity;
import com.lan.nicehair.common.CircleImageView;
import com.lan.nicehair.common.SquareImageView;
import com.lan.nicehair.common.model.Comment;
import com.lan.nicehair.common.model.ZoneAllItem;
import com.lan.nicehair.common.refresh_list.RefreshListView;
import com.lan.nicehair.utils.ImageFetcher;
import com.lan.nicehair.utils.Utils;

public class ZoneAllAdapter extends BaseAdapter {

	private Context mContext;
	public LinkedList<ZoneAllItem> mListInfo;
	private ImageFetcher mImageFetcher;
	private ImageFetcher mImagePic;
	private RefreshListView mListView;
	private int color;
	public ZoneAllAdapter(Context context,LinkedList<ZoneAllItem> listInfo,ImageFetcher imageFetcher,RefreshListView mListView) {
		this.mContext=context;
		this.mListInfo=listInfo;
		this.mImageFetcher = imageFetcher;
		mImagePic=new ImageFetcher(context, 400);
		mImagePic.setLoadingImage(R.drawable.hairscan_loading);
		color=context.getResources().getColor(R.color.zone_list_username);
		this.mListView=mListView;
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
	public void addItemLast(List<ZoneAllItem> datas) {
   	 	mListInfo.addAll(datas);
    }
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		if(convertView==null) {
			convertView=LayoutInflater.from(mContext).inflate(R.layout.item_topic, null);
			holder=new ViewHolder(convertView);
			convertView.setTag(holder);
		}else {
			holder=(ViewHolder) convertView.getTag();
		}
		final ZoneAllItem info=mListInfo.get(position);
		if(position==0) {
			holder.mTvTop.setVisibility(View.VISIBLE);
		}else {
			holder.mTvTop.setVisibility(View.GONE);
		}
		mImageFetcher.loadImage(info.getHeadUrl(), holder.mIvHead);
		holder.mTvName.setText(info.getName());
		holder.mTvLevel.setText("LV"+info.getLevel());
		holder.mTvTime.setText(info.getTime());
		holder.mTvTitle.setText(info.getTitle());
		holder.mTvContent.setText(info.getContent());
		Utils.addLinks(color,"",holder.mTvContent);
		if(info.getPicArray()!=null&&info.getPicArray().length>0) {
			holder.mLayoutImg.setVisibility(View.VISIBLE);
			mImagePic.loadImage(info.getPicArray()[0], holder.mIvPic1);
			mImagePic.loadImage(info.getPicArray()[1], holder.mIvPic2);
			mImagePic.loadImage(info.getPicArray()[2], holder.mIvPic3);
		}else {
			holder.mLayoutImg.setVisibility(View.GONE);
		}
		holder.mTvLikeNum.setText(String.valueOf(info.getPariseNum()));
		holder.mTvCommentNum.setText(String.valueOf(info.getChatNum()));
		if(info.getListComment()!=null&&info.getListComment().size()>0) {
			holder.mLayoutCommentDetail.setVisibility(View.VISIBLE);
			for(int i=0;i<info.getListComment().size();i++) {
				Comment com=info.getListComment().get(i);
				String content=com.getName()+":"+com.getContent();
				if(i==0) {
					mImageFetcher.loadImage(com.getHeadUrl(), holder.mIvHead1);
					holder.mTvComment1.setText(content);
					Utils.addLinks(color,com.getName(),holder.mTvComment1);
				}else if(i==1) {
					mImageFetcher.loadImage(com.getHeadUrl(), holder.mIvHead2);
					holder.mTvComment2.setText(content);
					Utils.addLinks(color,com.getName(),holder.mTvComment2);
				}else if(i==2) {
					mImageFetcher.loadImage(com.getHeadUrl(), holder.mIvHead3);
					holder.mTvComment3.setText(content);
					Utils.addLinks(color,com.getName(),holder.mTvComment3);
				}
			}
		}else {
			holder.mLayoutCommentDetail.setVisibility(View.GONE);
		}
		holder.mLayoutShare.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String text=info.getContent();
				Intent intent=new Intent(Intent.ACTION_SEND);
				if(info.getHeadUrl()!=null) {
					String path=mImagePic.getImageCache().getPathFromDiskCache(info.getHeadUrl());
					Uri imageFileUri = Uri.parse(path);
					intent.putExtra(Intent.EXTRA_STREAM, imageFileUri);
				}
				// intent.setType("text/plain");
				intent.setType("image/*"); 
				intent.putExtra(Intent.EXTRA_TEXT, text);
				intent.putExtra(Intent.EXTRA_TITLE, info.getTitle());
				mContext.startActivity(Intent.createChooser(intent, info.getTitle()));
			}
		});
		holder.mLayoutImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext,PhotoPagerActivity.class);
				intent.putExtra("array", info.getPicArray());
				mContext.startActivity(intent);
			}
		});
		holder.mLayoutComment.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.CLICK_RECEIVED_ACTION);
				intent.putExtra("uid", info.getUid());
				mContext.sendBroadcast(intent);
			}
		});
		holder.mLayoutCollect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				info.setCollect(true);
				mListInfo.get(position).setCollect(true);
				holder.mCollectTag.setBackgroundResource(R.drawable.collection_press);
			}
		});
		holder.mLayoutLike.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!info.isLike()) {
					int currentNum=info.getPariseNum()+1;
					info.setLike(true);
					mListInfo.get(position).setLike(true);
					mListInfo.get(position).setPariseNum(currentNum);
					holder.mTvLikeNum.setText(String.valueOf(currentNum));
					holder.mLikeTag.setBackgroundResource(R.drawable.like_press);
				}
			}
		});
		if(info.isCollect()) {
			holder.mCollectTag.setBackgroundResource(R.drawable.collection_press);
		}else {
			holder.mCollectTag.setBackgroundResource(R.drawable.collection_icon);
		}
		if(info.isLike()) {
			holder.mLikeTag.setBackgroundResource(R.drawable.like_press);
		}else {
			holder.mLikeTag.setBackgroundResource(R.drawable.like_icon);
		}
		return convertView;
	}
	//更新指定item的数据
    private void updateView(int index)
    {
        int visiblePos = mListView.getFirstVisiblePosition();
        int offset = index - visiblePos;              
        // 只有在可见区域才更新
        if(offset < 0) return;
        View view = mListView.getChildAt(offset);
        ViewHolder holder = (ViewHolder)view.getTag();
    }
	public class ViewHolder{
		@InjectView(R.id.iv_zone_item_profile) CircleImageView mIvHead;
		@InjectView(R.id.tv_zone_item_username) TextView mTvName;
		@InjectView(R.id.tv_zone_item_level) TextView mTvLevel;
		@InjectView(R.id.tv_zone_item_time) TextView mTvTime;
		@InjectView(R.id.tv_zone_item_top) TextView mTvTop;
		@InjectView(R.id.tv_zone_item_tab) TextView mTvTitle;
		@InjectView(R.id.tv_zone_item_content) TextView mTvContent;
		@InjectView(R.id.iv_zone_item_authority_icon) ImageView mIconTag;
		@InjectView(R.id.iv_zone_item_img1) SquareImageView mIvPic1;
		@InjectView(R.id.iv_zone_item_img2) SquareImageView mIvPic2;
		@InjectView(R.id.iv_zone_item_img3) SquareImageView mIvPic3;
		@InjectView(R.id.layout_zone_item_img) LinearLayout mLayoutImg;
		@InjectView(R.id.tv_zone_item_like) TextView mTvLikeNum;
		@InjectView(R.id.tv_zone_item_comment) TextView mTvCommentNum;
		@InjectView(R.id.layout_zone_item_comm) LinearLayout mLayoutCommentDetail;
		@InjectView(R.id.iv_topic_item_profile1) CircleImageView mIvHead1;
		@InjectView(R.id.iv_topic_item_profile2) CircleImageView mIvHead2;
		@InjectView(R.id.iv_topic_item_profile3) CircleImageView mIvHead3;
		@InjectView(R.id.tv_topic_item_comment1) TextView mTvComment1;
		@InjectView(R.id.tv_topic_item_comment2) TextView mTvComment2;
		@InjectView(R.id.tv_topic_item_comment3) TextView mTvComment3;
		@InjectView(R.id.layout_zone_item_share) LinearLayout mLayoutShare;
		@InjectView(R.id.layout_zone_item_collection) LinearLayout mLayoutCollect;
		@InjectView(R.id.iv_zone_item_collection) ImageView mCollectTag;
		@InjectView(R.id.iv_zone_item_like) ImageView mLikeTag;
		@InjectView(R.id.layout_zone_item_like) LinearLayout mLayoutLike;
		@InjectView(R.id.layout_zone_item_comment) LinearLayout mLayoutComment;
		
		public ViewHolder(View view) {
			ButterKnife.inject(this,view);
		}
	}
}
