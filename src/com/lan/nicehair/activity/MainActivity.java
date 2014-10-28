package com.lan.nicehair.activity;

import android.app.ActivityGroup;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TabWidget;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.lan.nicehair.R;
import com.lan.nicehair.R.id;
import com.lan.nicehair.R.layout;


@SuppressWarnings("deprecation")
public class MainActivity extends ActivityGroup {
	@InjectView(R.id.tabhost) TabHost mTabHost;
	@InjectView(R.id.radiogroup) RadioGroup mRadioGroup;
	@InjectView(R.id.layout_comment) LinearLayout mLayoutComment;
	@InjectView(R.id.post_comment_input) EditText mEditInput;
	@OnClick(R.id.post_input_camera)
	public void onPostCameraClicked(View v) {
		
	}
	@OnClick(R.id.post_input_send)
	public void onPostSendClicked(View v) {
		
	}
	public static final String CLICK_RECEIVED_ACTION="click_action";
	private ActionClickReceiver mActionClickReceiver;
	private InputMethodManager mInputManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ButterKnife.inject(this);
		mTabHost.setup(getLocalActivityManager());
		final TabWidget tabWidget = mTabHost.getTabWidget(); 
        tabWidget.setStripEnabled(false);// 圆角边线不启用 
		addTabIntent();
		mTabHost.setCurrentTab(0);
		mInputManager=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch(checkedId) {
				case R.id.radio_choumeibang:
					mTabHost.setCurrentTab(0);
					break;
				case R.id.radio_hairscan:
					mTabHost.setCurrentTab(1);
					break;
				case R.id.radio_discover:
					mTabHost.setCurrentTab(2);
					break;
				case R.id.radio_me:
					mTabHost.setCurrentTab(3);
					break;
				}
			}
		});
		registerMessageReceiver();
	}
	public void registerMessageReceiver() {
		mActionClickReceiver = new ActionClickReceiver();
		IntentFilter filter = new IntentFilter();
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction(CLICK_RECEIVED_ACTION);
		registerReceiver(mActionClickReceiver, filter);
	}
	public class ActionClickReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (CLICK_RECEIVED_ACTION.equals(intent.getAction())) {
				int uid=intent.getIntExtra("uid", 0);
				mLayoutComment.setVisibility(View.VISIBLE);
				mInputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				mEditInput.requestFocus();
			}
		}
	}
	@Override  
	public boolean dispatchKeyEvent(KeyEvent event) {  
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN ) {
			if(mLayoutComment.isShown()) {
				mLayoutComment.setVisibility(View.GONE);
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mActionClickReceiver!=null)
			unregisterReceiver(mActionClickReceiver);
	}
	private TabHost.TabSpec buildTabSpec(String tag,String m,
			final Intent content) {
		return this.mTabHost
				.newTabSpec(tag).setIndicator(m)
				.setContent(content);
	}
	private void addTabIntent() {
		this.mTabHost.addTab(buildTabSpec("tab1","0",new Intent(this,ZoneActivity.class)));
		this.mTabHost.addTab(buildTabSpec("tab2","1",new Intent(this,FindHairActivity.class)));
		this.mTabHost.addTab(buildTabSpec("tab3","2",new Intent(this,DisCoverActivity.class)));
		this.mTabHost.addTab(buildTabSpec("tab4","3",new Intent(this,MeActivity.class)));
	}
}
