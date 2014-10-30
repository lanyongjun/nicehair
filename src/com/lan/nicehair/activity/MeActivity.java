package com.lan.nicehair.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.lan.nicehair.R;
import com.lan.nicehair.common.CircleImageView;
import com.lan.nicehair.utils.AppToast;
import com.lan.nicehair.widget.ModelPopup;
import com.lan.nicehair.widget.ModelPopup.OnDialogListener;

public class MeActivity extends BaseActivity implements OnDialogListener {

	@OnClick(R.id.my_messageRl)
	public void onMessageClicked(View v) {
		// startActivity(new Intent(DisCoverActivity.this,
		// DisCoverActivity.class));
	}

	@OnClick(R.id.my_topicRl)
	public void onTopicClicked(View v) {
		// startActivity(new Intent(DisCoverActivity.this,
		// DisCoverActivity.class));
	}

	@OnClick(R.id.my_collectionRl)
	public void onCollectionClicked(View v) {
		// startActivity(new Intent(DisCoverActivity.this,
		// DisCoverActivity.class));
	}

	@OnClick(R.id.my_hairhistoryRl)
	public void onHairHistoryClicked(View v) {
		// startActivity(new Intent(DisCoverActivity.this,
		// DisCoverActivity.class));
	}

	@OnClick(R.id.my_albumRl)
	public void onAlbumClicked(View v) {
		// startActivity(new Intent(DisCoverActivity.this,
		// DisCoverActivity.class));
	}

	@OnClick(R.id.my_userinfoRl)
	public void onUserInfoClicked(View v) {
		// startActivity(new Intent(DisCoverActivity.this,
		// DisCoverActivity.class));
	}

	@OnClick(R.id.my_setting)
	public void onSettingClicked(View v) {
		startActivity(new Intent(MeActivity.this, SettingActivity.class));
	}

	@OnClick(R.id.miv_user_header)
	public void onUserHeadClicked(View v) {
		mPopup.showAtLocation(layout_root, Gravity.BOTTOM, 0, 0);
	}

	@InjectView(R.id.layout_root)
	LinearLayout layout_root;
	@InjectView(R.id.miv_user_header)
	CircleImageView mHeadIv;
	private ModelPopup mPopup;
	/***
	 * 使用照相机拍照获取图片
	 */
	public static final int SELECT_PIC_BY_TACK_PHOTO = 1;
	/***
	 * 使用相册中的图片
	 */
	public static final int SELECT_PIC_BY_PICK_PHOTO = 2;

	private static final int CUT_PHOTO = 3;

	private Uri photoUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me);
		ButterKnife.inject(this);
		mPopup = new ModelPopup(this, this, false);
	}

	/**
	 * 
	 * 系统相机拍照
	 */
	private void takePhoto() {
		// TODO Auto-generated method stub
		// 执行拍照前，应该先判断SD卡是否存在
		String SDState = Environment.getExternalStorageState();
		if (!SDState.equals(Environment.MEDIA_MOUNTED)) {
			AppToast.showShortText(MeActivity.this, "内存卡不存在");
			return;
		}
		try {
			photoUri = getContentResolver().insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					new ContentValues());
			if (photoUri != null) {
				Intent i = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
				startActivityForResult(i, SELECT_PIC_BY_TACK_PHOTO);

			} else {
				AppToast.showShortText(MeActivity.this, "发生意外，无法写入相册");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			AppToast.showShortText(MeActivity.this, "发生意外，无法写入相册");
		}
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case SELECT_PIC_BY_TACK_PHOTO:
				// 选择自拍结果
				beginCrop(photoUri);
				break;
			case SELECT_PIC_BY_PICK_PHOTO:
				// 选择图库图片结果
				beginCrop(intent.getData());
				break;
			case CUT_PHOTO:
				handleCrop(intent);
				break;
			}

		}
	}

	/**
	 * 裁剪图片方法实现
	 * 
	 * @param uri
	 */
	public void beginCrop(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高，注意如果return-data=true情况下,其实得到的是缩略图，并不是真实拍摄的图片大小，
		// 而原因是拍照的图片太大，所以这个宽高当你设置很大的时候发现并不起作用，就是因为返回的原图是缩略图，但是作为头像还是够清晰了
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		//返回图片数据
		intent.putExtra("return-data", true);
		startActivityForResult(intent, CUT_PHOTO);
	}

	/**
	 * 保存裁剪之后的图片数据
	 * 
	 * @param result
	 */
	private void handleCrop(Intent result) {
		Bundle extras = result.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			mHeadIv.setImageBitmap(photo);
		}
	}

	@Override
	public void onChoosePhoto() {
		// TODO Auto-generated method stub
		// 从相册中取图片
		Intent choosePictureIntent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(choosePictureIntent, SELECT_PIC_BY_PICK_PHOTO);
	}

	@Override
	public void onTakePhoto() {
		// TODO Auto-generated method stub
		takePhoto();
	}

	@Override
	public void onModel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub

	}
}
