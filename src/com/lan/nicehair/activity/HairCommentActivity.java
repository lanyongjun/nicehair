package com.lan.nicehair.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.lan.nicehair.R;
import com.lan.nicehair.adapter.HairCommentAdapter;
import com.lan.nicehair.common.CircleImageView;
import com.lan.nicehair.common.json.HairCommentParser.HairComResult;
import com.lan.nicehair.common.model.Comment;
import com.lan.nicehair.common.model.FindHairItem;
import com.lan.nicehair.common.net.ActionOfUrl.JsonAction;
import com.lan.nicehair.common.net.NetAsyncTask;
import com.lan.nicehair.common.refresh_list.RefreshListView;
import com.lan.nicehair.common.refresh_list.RefreshListView.IHListViewListener;
import com.lan.nicehair.utils.AppToast;
import com.lan.nicehair.utils.HProgress;
import com.lan.nicehair.utils.ImageFetcher;

public class HairCommentActivity extends BaseActivity implements IHListViewListener {

	@OnClick(R.id.scan_info_back_btn) 
	public void onBackClicked(View v) {
		finish();
	}
	@OnClick(R.id.scan_info_share_btn) 
	public void onShareClicked(View v) {
		
	}
	@OnClick(R.id.scan_info_comment_btn) 
	public void onCommentClicked(View v) {
		
	}
	@OnClick(R.id.scan_info_collect_btn) 
	public void onCollectClicked(View v) {
		
	}
	@OnClick(R.id.scan_info_zan_btn) 
	public void onZanClicked(View v) {
		
	}
	@InjectView(R.id.loading_layout) RelativeLayout mLayoutLoading;
	@InjectView(R.id.hair_list) RefreshListView mListView;
	@InjectView(R.id.layout_comment) LinearLayout mLayoutComment;
	@InjectView(R.id.post_comment_input) EditText mEditInput;
	private ViewHeadHolder mHolder;
	private ImageFetcher mImageFetcher;
	private List<Comment> mListComment=new ArrayList<Comment>();
	private FindHairItem mInfo;
	private HairCommentAdapter mAdapter;
	private int currentPage = 1;
	public static final String HAIR_COMMENT_ACTION="hair_comment_action";
	private InputMethodManager mInputManager;
	private ActionClickReceiver mActionClickReceiver;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case R.id.ui_show_dialog:
				//dialog.show();
				HProgress.show(HairCommentActivity.this, null);
				break;
			case R.id.ui_dismiss_dialog:
				//dialog.dismiss();
				HProgress.dismiss();
				break;
			case R.id.ui_show_text://网络超时和数据解析异常
				AppToast.showShortText(HairCommentActivity.this, msg.arg1);
				break;
			case R.id.ui_update_ui:
				break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hair_scan_info_activity);
		ButterKnife.inject(this);
		mImageFetcher=new ImageFetcher(this, 800);
		mInfo=(FindHairItem) getIntent().getSerializableExtra("hairInfo");
		addHeadView();
		mAdapter = new HairCommentAdapter(this, mListComment, mImageFetcher);
		mListView.setAdapter(mAdapter);
		mListView.setPullLoadEnable(true);	
		mListView.setHListViewListener(this);
		mInputManager=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
		registerMessageReceiver();
		AddItemToContainer(currentPage, 1);
	}
	void addHeadView() {
		View headView=getLayoutInflater().inflate(R.layout.hair_scan_info_top_view, null);
		mHolder = new ViewHeadHolder(headView);
		mListView.addHeaderView(headView);
		if(mInfo!=null) {
			mImageFetcher.loadImage(mInfo.getPicUrl(), mHolder.mHairIv);
			mHolder.mContentTv.setText(mInfo.getTitle());
			//top3布局
			mHolder.mTop1Layout1.setVisibility(View.VISIBLE);
			mHolder.mTop2Layout1.setVisibility(View.VISIBLE);
			mHolder.mTop3Layout1.setVisibility(View.VISIBLE);			
			mHolder.mTop1Layout2.setVisibility(View.VISIBLE);
			mHolder.mTop2Layout2.setVisibility(View.VISIBLE);
			mHolder.mTop3Layout2.setVisibility(View.VISIBLE);			
			mHolder.mTop1Layout3.setVisibility(View.VISIBLE);
			mHolder.mTop2Layout3.setVisibility(View.VISIBLE);
			mHolder.mTop3Layout3.setVisibility(View.VISIBLE);
			mHolder.mLineIv1.setVisibility(View.VISIBLE);
			mHolder.mLineIv2.setVisibility(View.VISIBLE);
			mImageFetcher.setLoadingImage(R.drawable.zone_list_profile);
			mImageFetcher.loadImage(mInfo.getHeadUrl(), mHolder.mHeadIv1);
			mImageFetcher.loadImage(mInfo.getHeadUrl(), mHolder.mHeadIv2);
			mImageFetcher.loadImage(mInfo.getHeadUrl(), mHolder.mHeadIv3);
			mHolder.mNameTv1.setText(mInfo.getName());
			mHolder.mNameTv2.setText(mInfo.getName());
			mHolder.mNameTv3.setText(mInfo.getName());
			mHolder.mLevelTv1.setText("LV"+mInfo.getLevel());
			mHolder.mLevelTv2.setText("LV"+mInfo.getLevel());
			mHolder.mLevelTv3.setText("LV"+mInfo.getLevel());
			mImageFetcher.loadImage(mInfo.getPicUrl(), mHolder.mTopImg1);
			mImageFetcher.loadImage(mInfo.getPicUrl(), mHolder.mTopImg2);
			mImageFetcher.loadImage(mInfo.getPicUrl(), mHolder.mTopImg3);
		}
	}
	class ViewHeadHolder{
		@InjectView(R.id.hair_iv) ImageView mHairIv;
		@InjectView(R.id.hair_content_tv) TextView mContentTv;
		@InjectView(R.id.scan_info_top1_layout1) LinearLayout mTop1Layout1;
		@InjectView(R.id.scan_info_top2_layout1) LinearLayout mTop2Layout1;
		@InjectView(R.id.scan_info_top3_layout1) LinearLayout mTop3Layout1;
		@InjectView(R.id.scan_info_top1_layout2) RelativeLayout mTop1Layout2;
		@InjectView(R.id.scan_info_top2_layout2) RelativeLayout mTop2Layout2;
		@InjectView(R.id.scan_info_top3_layout2) RelativeLayout mTop3Layout2;
		@InjectView(R.id.scan_info_top1_layout3) RelativeLayout mTop1Layout3;
		@InjectView(R.id.scan_info_top2_layout3) RelativeLayout mTop2Layout3;
		@InjectView(R.id.scan_info_top3_layout3) RelativeLayout mTop3Layout3;
		@InjectView(R.id.scan_top_head1) CircleImageView mHeadIv1;
		@InjectView(R.id.scan_top_head2) CircleImageView mHeadIv2;
		@InjectView(R.id.scan_top_head3) CircleImageView mHeadIv3;
		@InjectView(R.id.scan_top_username1) TextView mNameTv1;
		@InjectView(R.id.scan_top_username2) TextView mNameTv2;
		@InjectView(R.id.scan_top_username3) TextView mNameTv3;
		@InjectView(R.id.scan_top_lv1) TextView mLevelTv1;
		@InjectView(R.id.scan_top_lv2) TextView mLevelTv2;
		@InjectView(R.id.scan_top_lv3) TextView mLevelTv3;
		@InjectView(R.id.scan_top_img1) ImageView mTopImg1;
		@InjectView(R.id.scan_top_img2) ImageView mTopImg2;
		@InjectView(R.id.scan_top_img3) ImageView mTopImg3;
		@InjectView(R.id.line_1) ImageView mLineIv1;
		@InjectView(R.id.line_2) ImageView mLineIv2;
		public ViewHeadHolder(View view) {
			ButterKnife.inject(this,view);
		}
	}
	public void registerMessageReceiver() {
		mActionClickReceiver = new ActionClickReceiver();
		IntentFilter filter = new IntentFilter();
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction(HAIR_COMMENT_ACTION);
		registerReceiver(mActionClickReceiver, filter);
	}
	public class ActionClickReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (HAIR_COMMENT_ACTION.equals(intent.getAction())) {
				int uid=intent.getIntExtra("uid", 0);
				String name=intent.getStringExtra("name");
				mLayoutComment.setVisibility(View.VISIBLE);
				mInputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				mEditInput.requestFocus();
				mEditInput.setHint("回复 "+name);
			}
		}
	}
	public class NetCotnent extends NetAsyncTask{

		private HairComResult result;
		private int mType = 1;
		public NetCotnent(Handler uiHandler,int type) {
			super(uiHandler);
			this.mType=type;
			if(mListComment.size()==0)
				setDialogId(1);
		}
		@Override
		protected void handlePreExecute() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected String handleNetworkProcess(String... arg0) throws Exception {
			// TODO Auto-generated method stub
			result=(HairComResult) httptask.getRequestbyPOST(JsonAction.HAIR_COMMENT,arg0[0], null);
			return result!=null? HANDLE_SUCCESS : HANDLE_FAILED;
		}

		@Override
		protected void handleResult() {
			// TODO Auto-generated method stub
			if (result!=null&&result.isSuccess()) {
				mListComment=result.getListComment();				
				mListView.stopLoadMore();
				mListView.stopRefresh();
				if (mType == 1&&mListComment.size()>0) {
					mAdapter.mListInfo=mListComment;
					mAdapter.notifyDataSetChanged();
				} else if (mType == 2&&mListComment.size()>0) {
					mAdapter.addItemLast(mListComment);
					mAdapter.notifyDataSetChanged();
					mListView.setSelection(mListView.getLastVisiblePosition());
				}
				mImageFetcher.loadImage(mInfo.getHeadUrl(), mHolder.mHeadIv1);
				mImageFetcher.loadImage(mInfo.getHeadUrl(), mHolder.mHeadIv2);
				mImageFetcher.loadImage(mInfo.getHeadUrl(), mHolder.mHeadIv3);
			}
			 	            
		}
		
	}
	private void AddItemToContainer(int pageindex, int type) {
		String url = "album/1733789/masn/p/" + pageindex + "/24/";
		new NetCotnent(mHandler,type).execute(url);
	}
	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mActionClickReceiver);
	}
	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		currentPage=1;
		AddItemToContainer(currentPage, 1);
	}
	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		AddItemToContainer(++currentPage, 2);
	}
}
