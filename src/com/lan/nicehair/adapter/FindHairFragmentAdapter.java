package com.lan.nicehair.adapter;

import com.lan.nicehair.fragment.FindHairFragment;
import com.lan.nicehair.fragment.MainTopicFragment;
import com.lan.nicehair.fragment.ZoneAllFagment;
import com.lan.nicehair.fragment.ZoneHotFragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FindHairFragmentAdapter extends FragmentPagerAdapter {

	private int pageCount=3;
	public FindHairFragmentAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public FindHairFragmentAdapter(FragmentManager fm, Context context) {
		super(fm);
	}
	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		switch (position) {
		case 0:
		case 1:
			return FindHairFragment.newInstance(position);
		case 2:
			return MainTopicFragment.newInstance();
		}
		return null;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getItemPosition(Object object) {
		// TODO Auto-generated method stub
		return POSITION_NONE;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return pageCount;
	}

}
