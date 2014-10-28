package com.lan.nicehair.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.lan.nicehair.R;

public class MeActivity extends BaseActivity {

	@OnClick(R.id.my_messageRl)
	public void onMessageClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@OnClick(R.id.my_topicRl)
	public void onTopicClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@OnClick(R.id.my_collectionRl)
	public void onCollectionClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@OnClick(R.id.my_hairhistoryRl)
	public void onHairHistoryClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@OnClick(R.id.my_albumRl)
	public void onAlbumClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@OnClick(R.id.my_userinfoRl)
	public void onUserInfoClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@OnClick(R.id.my_setting)
	public void onSettingClicked(View v){
        startActivity(new Intent(MeActivity.this, SettingActivity.class));
    }
	@OnClick(R.id.miv_user_header)
	public void onUserHeadClicked(View v){
        //startActivity(new Intent(DisCoverActivity.this, DisCoverActivity.class));
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me);
		ButterKnife.inject(this);
	}
}
