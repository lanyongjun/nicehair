package com.lan.nicehair.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;

import com.lan.nicehair.R;

public class MainTopicFragment  extends Fragment{

	public static MainTopicFragment newInstance() {
		MainTopicFragment fragment = new MainTopicFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View contentView = inflater.inflate(R.layout.view_zone_hot_tag, container, false);
		ButterKnife.inject(this, contentView);
		return contentView;
	}
}
