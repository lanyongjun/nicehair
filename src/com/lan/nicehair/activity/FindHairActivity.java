package com.lan.nicehair.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.lan.nicehair.R;
import com.lan.nicehair.adapter.FindHairFragmentAdapter;

public class FindHairActivity extends FragmentActivity{

	@InjectView(R.id.hair_radioGroup) RadioGroup mRadioGroup;
	@InjectView(R.id.viewpager) ViewPager mViewPager;
	private FindHairFragmentAdapter mAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_findhair);
		ButterKnife.inject(this);
		mAdapter=new FindHairFragmentAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mAdapter);
	    mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch(checkedId) {
				case R.id.btn_recomm_tag:
					mViewPager.setCurrentItem(0);
					break;
				case R.id.btn_new_tag:	
					mViewPager.setCurrentItem(1);
					break;
				case R.id.btn_main_topic_tag:
					mViewPager.setCurrentItem(2);
					break;
				}
			}
		});
	    mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				switch(position) {
				case 0:
					mRadioGroup.check(R.id.btn_recomm_tag);
					break;
				case 1:
					mRadioGroup.check(R.id.btn_new_tag);
					break;
				case 2:
					mRadioGroup.check(R.id.btn_main_topic_tag);
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
	}
	
}
