package com.lan.nicehair.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.lan.nicehair.R;
import com.lan.nicehair.widget.TopBarView;

public class SettingActivity extends BaseActivity {

	@InjectView(R.id.topbar) TopBarView mTopBar;
	@OnClick(R.id.setting_change_secretRl)
	public void onChangeSecretClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@OnClick(R.id.setting_bindRl)
	public void onBindClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@OnClick(R.id.setting_recommand_wxRl)
	public void onRecommandClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@OnClick(R.id.setting_attention_wxRl)
	public void onAttentionClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@OnClick(R.id.setting_feedbackRl)
	public void onFeedBackClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@OnClick(R.id.setting_checkversionRl)
	public void onCheckVersionClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@OnClick(R.id.setting_aboutRl)
	public void onAboutClicked(View v){
       
    }
	@OnClick(R.id.setting_helpRl)
	public void onHelpClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@OnClick(R.id.setting_clearCacheRl)
	public void onClearCacheClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@OnClick(R.id.setting_sign_outTv)
	public void onLoginOutClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_settings);
		ButterKnife.inject(this);
		mTopBar.setActivity(this);
		mTopBar.setTitle("设置");
	}
}
