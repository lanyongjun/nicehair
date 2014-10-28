package com.lan.nicehair.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.lan.nicehair.R;
import com.lan.nicehair.widget.TopBarView;

/**
 * 发现
 * @author lanyj
 *
 */
public class DisCoverActivity extends BaseActivity implements OnClickListener{

	@InjectView(R.id.topbar) TopBarView mTopBar;
	@OnClick(R.id.discover_changeHairRl) 
	public void onChangeHairClicked(View v){
        startActivity(new Intent(DisCoverActivity.this, ChangeHairActivity.class));
    }
	@OnClick(R.id.discover_diagnoseRl) 
	public void onDiagnoseClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discover_fragment_layout);
		ButterKnife.inject(this);
		mTopBar.mTvBack.setVisibility(View.GONE);
		mTopBar.setTitle(getResources().getString(R.string.main_discover));
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
