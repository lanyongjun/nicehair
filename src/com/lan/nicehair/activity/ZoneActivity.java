package com.lan.nicehair.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.lan.nicehair.R;
import com.lan.nicehair.adapter.ZoneFragmentAdapter;

public class ZoneActivity extends FragmentActivity {

	@InjectView(R.id.vp_zone_view) ViewPager mViewPager;
	@InjectView(R.id.zone_radioGroup) RadioGroup mRadioGroup;
	@InjectView(R.id.layout_loading) LinearLayout mLayoutLoading;
	@OnClick(R.id.iv_zone_search)
	public void onSearcchClicked(View v) {
		startActivity(new Intent(ZoneActivity.this, TopicSearchActivity.class));
	}
	@OnClick(R.id.iv_zone_publish)
	public void onPublishClicked(View v) {
		startActivity(new Intent(ZoneActivity.this, PublishActivity.class));
	}
	private ZoneFragmentAdapter mFragmentAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zone);
		ButterKnife.inject(this);
		mLayoutLoading.setVisibility(View.GONE);
		mFragmentAdapter = new ZoneFragmentAdapter(getSupportFragmentManager(), this);
		mViewPager.setAdapter(mFragmentAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				switch(position) {
				case 0:
					mRadioGroup.check(R.id.btn_zone_topic_all);
					break;
				case 1:
					mRadioGroup.check(R.id.btn_zone_hot_tag);
					break;
				}
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
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch(checkedId) {
				case R.id.btn_zone_topic_all:
					mViewPager.setCurrentItem(0);
					break;
				case R.id.btn_zone_hot_tag:
					mViewPager.setCurrentItem(1);
					break;
				}
			}
		});
	}
	
	protected void replaceFragment(int viewId, Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(viewId, fragment).commitAllowingStateLoss();
    }
}
