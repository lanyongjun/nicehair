package com.lan.nicehair.activity;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.lan.nicehair.R;
import com.lan.nicehair.activity.MainActivity.ActionClickReceiver;
import com.lan.nicehair.adapter.PostAdapter;
import com.lan.nicehair.common.CircleImageView;
import com.lan.nicehair.common.model.Comment;
import com.lan.nicehair.common.model.ZoneAllItem;
import com.lan.nicehair.common.refresh_list.RefreshListView;
import com.lan.nicehair.common.refresh_list.RefreshListView.IHListViewListener;
import com.lan.nicehair.utils.ImageFetcher;
import com.lan.nicehair.widget.NoScrollListView;
import com.lan.nicehair.widget.TopBarView;

public class PostInfoActivity extends BaseActivity implements IHListViewListener{

	@InjectView(R.id.topbar) TopBarView mTopBar;
	@InjectView(R.id.post_tag_tv) TextView mPostTagTv;
	@InjectView(R.id.post_comment_input) EditText mEditInput;
	@InjectView(R.id.centerlist) RefreshListView mListView;
	@OnClick(R.id.fromLayout) 
	public void onFromClicked(View v) {
		
	}
	@OnClick(R.id.post_input_send)
	public void onPostSendClicked(View v) {
		
	}
	private ZoneAllItem mInfo;
	private ImageFetcher mImageFetcher;
	private String[] picArray;
	private List<Comment> mListComment;
	private PostAdapter mAdapter;
	private ViewHeadHolder mHolder;
	private InputMethodManager mInputManager;
	private ActionClickReceiver mActionClickReceiver;
	public static final String POST_RECEIVED_ACTION="post_comment_action";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_info_activity);
		ButterKnife.inject(this);
		mTopBar.setActivity(this);
		mTopBar.setTitle("");
		mImageFetcher=new ImageFetcher(this, 800);
		mInfo=(ZoneAllItem) getIntent().getSerializableExtra("zoneAll");
		addHeadView();
		mAdapter = new PostAdapter(this, mListComment);
		mListView.setAdapter(mAdapter);
		mListView.setPullLoadEnable(true);	
		mListView.setHListViewListener(this);
		mInputManager=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
		registerMessageReceiver();
	}
	private void addHeadView() {
		View headView=getLayoutInflater().inflate(R.layout.post_info_item, null);
		mHolder = new ViewHeadHolder(headView);
		if(mInfo!=null) {
			picArray=mInfo.getPicArray();
			mListComment=mInfo.getListComment();
			mPostTagTv.setText(mInfo.getTitle());
			mImageFetcher.loadImage(mInfo.getHeadUrl(), mHolder.mPostHeadIv);
			mHolder.mPostNameTv.setText(mInfo.getName());
			mHolder.mUserLevelTv.setText("LV"+mInfo.getLevel());
			mHolder.mPostTimeTv.setText(mInfo.getTime());
			mHolder.mPostContent.setText(mInfo.getContent());
			ImageAdapter adapter = new ImageAdapter();
			mHolder.mPostList.setAdapter(adapter);
			if(mListComment!=null&&mListComment.size()>0) {
				mHolder.mPostBottomLayout.setVisibility(View.VISIBLE);
			}
		}
		mListView.addHeaderView(headView);
	}
	public void registerMessageReceiver() {
		mActionClickReceiver = new ActionClickReceiver();
		IntentFilter filter = new IntentFilter();
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction(POST_RECEIVED_ACTION);
		registerReceiver(mActionClickReceiver, filter);
	}
	public class ActionClickReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (POST_RECEIVED_ACTION.equals(intent.getAction())) {
				int uid=intent.getIntExtra("uid", 0);
				String name=intent.getStringExtra("name");
				mInputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				mEditInput.requestFocus();
				mEditInput.setHint("回复 "+name);
			}
		}
	}
	class ViewHeadHolder{
		@InjectView(R.id.post_info_head) CircleImageView mPostHeadIv;
		@InjectView(R.id.post_info_username) TextView mPostNameTv;
		@InjectView(R.id.post_user_grade) TextView mUserLevelTv;
		@InjectView(R.id.post_addtime) TextView mPostTimeTv;
		@InjectView(R.id.post_info_item_content) TextView mPostContent;
		@InjectView(R.id.post_info_item_list) NoScrollListView mPostList;
		@InjectView(R.id.post_info_share_btn) LinearLayout mLayoutShare;
		@InjectView(R.id.post_info_collect_btn) LinearLayout mLayoutCollect;
		@InjectView(R.id.iv_zone_item_collection) ImageView mCollectTag;
		@InjectView(R.id.iv_zone_item_like) ImageView mLikeTag;
		@InjectView(R.id.post_info_zan_btn) LinearLayout mLayoutLike;
		@InjectView(R.id.post_info_comment_btn) LinearLayout mLayoutComment;
		@InjectView(R.id.post_info_share_layout) LinearLayout mPostBottomLayout;
		public ViewHeadHolder(View view) {
			ButterKnife.inject(this,view);
		}
	}
	private class ImageAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return picArray==null?0:picArray.length;
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
			convertView=getLayoutInflater().inflate(R.layout.post_info_img_item, null);
			ImageView iv=(ImageView)convertView.findViewById(R.id.post_info_img_iv);
			mImageFetcher.loadImage(picArray[position], iv);
			mImageFetcher.loadImage(mInfo.getHeadUrl(), mHolder.mPostHeadIv);
			return convertView;
		}
		
	}
	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		mListView.stopRefresh();
	}
	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		mListView.stopLoadMore();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mActionClickReceiver);
	}
}
