package com.lan.nicehair.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lan.nicehair.R;

/**
 * 标题导航栏 actionbar
 * @author lanyj
 *
 */
public class TopBarView extends LinearLayout{

	private LinearLayout mTopBack;
	public TextView mTvBack;
	public TextView mTvTitle;
	public ImageView mIvRight;
	public TextView mTvRight;
	private Activity mActivity;
	public TopBarView(Context context) {
		super(context);
	}
	public TopBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.top_bar_view, this, true);
		mTopBack=(LinearLayout)this.findViewById(R.id.top_back_btn);
		mTvBack=(TextView)this.findViewById(R.id.top_back_tv);
		mTvTitle=(TextView)this.findViewById(R.id.top_title);
		mTvRight=(TextView)this.findViewById(R.id.top_right_tv);
		mIvRight=(ImageView)this.findViewById(R.id.top_right_btn);
		mTopBack.setOnClickListener(onClickListener);
		mTvRight.setOnClickListener(onClickListener);
		mIvRight.setOnClickListener(onClickListener);
	}
	private OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()) {
			case R.id.top_back_btn:
				if(mActivity!=null)
					mActivity.finish();
				break;
			case R.id.top_right_tv:
				
				break;
			case R.id.top_right_btn:
				
				break;
			}
		}
	};
	public void setActivity(Activity activity) {
		this.mActivity=activity;
	}
	public void setTitle(String title) {
		mTvTitle.setText(title);
	}
	public void setRightText(String text) {
		mTvRight.setText(text);
	}
}
