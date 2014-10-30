package com.lan.nicehair.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

import com.lan.nicehair.R;

public class ModelPopup extends PopupWindow implements android.view.View.OnClickListener{

	private OnDialogListener listener;
	private View mPopView;
	public ModelPopup(Context context, OnDialogListener listener,boolean isShowMd) {  
        super(context);  
        this.listener=listener;
        LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPopView= inflater.inflate(R.layout.dialog_get_model, null);
        this.setContentView(mPopView);
        this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
        Button btn_choose_photo=(Button)mPopView.findViewById(R.id.btn_choose_photo);
        Button btn_take_photo=(Button)mPopView.findViewById(R.id.btn_take_photo);
        Button btn_model=(Button)mPopView.findViewById(R.id.btn_model);
        Button btn_cancel=(Button)mPopView.findViewById(R.id.btn_cancel);
        if(!isShowMd) {
        	btn_model.setVisibility(View.GONE);
        }
        btn_choose_photo.setOnClickListener(this);
        btn_take_photo.setOnClickListener(this);
        btn_model.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 点击外面的控件也可以使得PopUpWindow dimiss
        this.setOutsideTouchable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.PopupAnimation);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);//0xb0000000
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);//半透明颜色
    } 
	/**
	 * Dialog按钮回调接口
	 *
	 */
	public interface OnDialogListener {

		public void onChoosePhoto();//选择本地照片
		
		public void onTakePhoto();//照相
		
		public void onModel();//照相

		public void onCancel();//取消

	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.btn_choose_photo:
			listener.onChoosePhoto();
			break;
		case R.id.btn_take_photo:
			listener.onTakePhoto();
			break;
		case R.id.btn_model:
			listener.onModel();
			break;
		case R.id.btn_cancel:
			listener.onCancel();
			break;
		}
		dismiss();
	}
}
