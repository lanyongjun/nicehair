package com.lan.nicehair.activity;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.lan.nicehair.R;
import com.lan.nicehair.utils.ImageFetcher;

public class PhotoPagerActivity extends BaseActivity {

	@InjectView(R.id.adpager) ViewPager mViewPager;
	@InjectView(R.id.rl_layout) RelativeLayout mLayoutBar;
	@InjectView(R.id.tv_title) TextView mTvTitle;
	@InjectView(R.id.share) ImageButton mBtnRight;
	@OnClick(R.id.btn_back) 
	public void onBackClicked(View v) {
		finish();
	}
	private String[] picArray=null;
	private ImageFetcher mImageFetcher;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_try_image_pager);
		ButterKnife.inject(this);
		mBtnRight.setVisibility(View.GONE);
		int color=getResources().getColor(R.color.toast_background_color);
		mLayoutBar.setBackgroundColor(color);
		mImageFetcher=new ImageFetcher(this, 600);
		picArray=getIntent().getStringArrayExtra("array");
		PhotoAdapter adapter=new PhotoAdapter();
		mViewPager.setAdapter(adapter);
		mTvTitle.setText("1/"+picArray.length);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				mTvTitle.setText((position+1)+"/"+picArray.length);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	public class PhotoAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return picArray==null?0:picArray.length;
		}
		@Override
		public Object instantiateItem(ViewGroup collection, int position){
			View view=getLayoutInflater().inflate(R.layout.item_pager_image, null);
			ImageView iv=(ImageView)view.findViewById(R.id.image);
			ProgressBar pBar=(ProgressBar)view.findViewById(R.id.loading);
			mImageFetcher.loadImage(picArray[position], iv,pBar,0);
			collection.addView(view);
			iv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(mLayoutBar.isShown())
						mLayoutBar.setVisibility(View.INVISIBLE);
					else
						mLayoutBar.setVisibility(View.VISIBLE);
				}
			});
			return view;			
		}
		@Override
		public boolean isViewFromObject(View view, Object object) {
			// TODO Auto-generated method stub
			return view.equals(object);
		}
		@Override
		public void destroyItem(ViewGroup collection, int position, Object view){
			collection.removeView((View) view);
		}
				
	}
}
