package com.lan.nicehair.activity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.lan.nicehair.R;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class TopicSearchActivity extends BaseActivity {

	@OnClick(R.id.btn_right)
	public void onCancelClicked(View v) {
		finish();
	}
	@InjectView(R.id.iv_search_clear) ImageView mIvClear;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_topic_search);
		ButterKnife.inject(this);
	}
}
