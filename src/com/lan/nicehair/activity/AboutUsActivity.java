package com.lan.nicehair.activity;

import android.os.Bundle;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.lan.nicehair.R;
import com.lan.nicehair.widget.TopBarView;

/**
 * 关于我们
 * @author lanyj
 *
 */
public class AboutUsActivity extends BaseActivity {

	@InjectView(R.id.topbar) TopBarView mTopBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aboutus);
		ButterKnife.inject(this);
		mTopBar.setActivity(this);
		mTopBar.setTitle("关于我们");
	}
}
