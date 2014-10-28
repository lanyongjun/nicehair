package com.lan.nicehair.activity;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.lan.nicehair.R;
import com.lan.nicehair.widget.TopBarView;

import android.os.Bundle;
import android.widget.GridView;

public class PublishActivity extends BaseActivity {

	@InjectView(R.id.topbar) TopBarView mTopBar;
	@InjectView(R.id.zone_topic_publishButtomGv) GridView mGridView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zone_topic_activity);
		ButterKnife.inject(this);
		mTopBar.setActivity(this);
		mTopBar.setTitle(getResources().getString(R.string.topic_title));
	}
}
