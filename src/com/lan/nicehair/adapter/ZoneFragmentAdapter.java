package com.lan.nicehair.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.lan.nicehair.fragment.ZoneAllFagment;
import com.lan.nicehair.fragment.ZoneHotFragment;

public class ZoneFragmentAdapter extends FragmentPagerAdapter {

	private int pageCount=2;
	public ZoneFragmentAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public ZoneFragmentAdapter(FragmentManager fm, Context context) {
		super(fm);
	}
	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		switch (position) {
		case 0:
			return ZoneAllFagment.newInstance();
		case 1:
			return ZoneHotFragment.newInstance();
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
