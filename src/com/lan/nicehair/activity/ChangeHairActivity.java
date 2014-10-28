package com.lan.nicehair.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.lan.nicehair.R;
import com.lan.nicehair.common.HorizontalListView;
import com.lan.nicehair.common.MultiTouchImageView;
import com.lan.nicehair.utils.AppLog;
import com.lan.nicehair.utils.AppToast;
import com.lan.nicehair.utils.ImageUtil;
import com.lan.nicehair.utils.LPhone;
import com.lan.nicehair.widget.ModelPopup;
import com.lan.nicehair.widget.TopBarView;
import com.lan.nicehair.widget.ModelPopup.OnDialogListener;

/**
 * 换发型
 * @author lanyj
 *
 */
public class ChangeHairActivity extends BaseActivity implements OnDialogListener {

	@InjectView(R.id.topbar) TopBarView mTopBar;
	@InjectView(R.id.hairIv) MultiTouchImageView mHairIv;
	@InjectView(R.id.listview) HorizontalListView mListView;
	@InjectView(R.id.slideMenu) ViewPager slideMenu;
	@InjectView(R.id.modelIv) ImageView mModeIv;
	@InjectView(R.id.model_radioGroup) RadioGroup mRadioGroup;
	@InjectView(R.id.responsetouchContainer) RelativeLayout layout_root;
	@OnClick(R.id.takephotoIv)
	public void onTakePhotoClicked(View v) {
		mPopup.showAtLocation(layout_root, Gravity.BOTTOM, 0, 0);
	}
	@OnClick(R.id.shake_imageIv)
	public void onShakeClicked(View v) {
		
	}
	@OnClick(R.id.one_key_changeIv)
	public void onOneChangeClicked(View v) {
		
	}
	@OnClick(R.id.open_more_screen)
	public void onOpenMoreClicked(View v) {
		
	}
	/***
	 * 使用照相机拍照获取图片
	 */
	public static final int SELECT_PIC_BY_TACK_PHOTO = 1;
	/***
	 * 使用相册中的图片
	 */
	public static final int SELECT_PIC_BY_PICK_PHOTO = 2;
	private Hairs mHairs;
	private AssetManager assetManager;
	private ImageAdapter adapter;
	private boolean isMale;
	private String mRightText;
	private ModelPopup mPopup;
	private int selectPos=0;
	private float mDpWidth=140f;
	private float mDpHeight=140f;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changehair_activity_layout);
		ButterKnife.inject(this);
		mTopBar.setActivity(this);
		mTopBar.setTitle("换发型");
		mRightText="<font color='white'><big>女/</big></font><font color='#808080'>男</font>";
		mTopBar.mTvRight.setText(Html.fromHtml(mRightText));
		mHairs=new Hairs();
		loadAssetsRes();
		mPopup = new ModelPopup(this, this);
		adapter=new ImageAdapter(mHairs.female_show); 
		mListView.setAdapter(adapter);
		String fileName=mHairs.female_try.get(0);
		showZoomImage(fileName);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				List<String> listInfo;
				selectPos=position;
				if(mRadioGroup.getCheckedRadioButtonId()==R.id.radio_fmale) {
					listInfo=mHairs.female_try;
				}else {
					listInfo=mHairs.male_try;
				}
				if(position>listInfo.size()-1)return;
				String fileName=listInfo.get(position);
				showZoomImage(fileName);
				adapter.notifyDataSetChanged();
			}
		});
		mTopBar.mTvRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!isMale) {
					isMale=true;
					mModeIv.setImageResource(R.drawable.modelsecond);
					mRightText="<font color='white'><big>男/</big></font><font color='#808080'>女</font>";
				}else {
					isMale=false;
					mModeIv.setImageResource(R.drawable.modelfourth);
					mRightText="<font color='white'><big>女/</big></font><font color='#808080'>男</font>";
				}
				mTopBar.mTvRight.setText(Html.fromHtml(mRightText));
			}
		});
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch(checkedId) {
				case R.id.radio_fmale:
					adapter.mListInfo=mHairs.female_show;
					adapter.notifyDataSetChanged();
					break;
				case R.id.radio_male:
					adapter.mListInfo=mHairs.male_show;
					adapter.notifyDataSetChanged();
					break;
				}
			}
		});
	}
	private void showZoomImage(String fileName) {
		Bitmap bm=getResBitmap(fileName);
		int w=LPhone.dp2px(ChangeHairActivity.this, mDpWidth);
		int h=LPhone.dp2px(ChangeHairActivity.this, mDpHeight);
		if(bm!=null) {
			Bitmap b=ImageUtil.resizeBitmap(w, h, bm);
			mHairIv.setImageBitmap(b);
		}
	}
	private void loadAssetsRes() {
		assetManager = getAssets();     
		try {
			//hairs目录
			String[] files = assetManager.list("hairs");
			for(int i=0;i<files.length;i++) {
				String fileName="hairs/"+files[i];
				if(files[i].startsWith("female_show"))
					mHairs.female_show.add(fileName);
				else if(files[i].startsWith("female_try"))
					mHairs.female_try.add(fileName);
				else if(files[i].startsWith("male_show"))
					mHairs.male_show.add(fileName);
				else if(files[i].startsWith("male_try"))
					mHairs.male_try.add(fileName);
			}
			//hairshake目录
			files = assetManager.list("hairshake");
			for(int i=0;i<files.length;i++) {
				mHairs.hairshake.add("hairshake/"+files[i]);
			}
			//hairshake_male目录
			files = assetManager.list("hairshake_male");
			for(int i=0;i<files.length;i++) {
				mHairs.hairshake_male.add("hairshake_male/"+files[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AppLog.e("loadAssetsRes Fail", e.getMessage());
		}     
	        
	}
	public class Hairs{
		public List<String> female_show=new ArrayList<String>();
		public List<String> female_try=new ArrayList<String>();
		public List<String> male_show=new ArrayList<String>();
		public List<String> male_try=new ArrayList<String>();
		public List<String> hairshake=new ArrayList<String>();	
		public List<String> hairshake_male=new ArrayList<String>();
	}
	private class ImageAdapter extends BaseAdapter{
		private List<String> mListInfo;
		public ImageAdapter(List<String> mListInfo) {
			this.mListInfo=mListInfo;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mListInfo==null?0:mListInfo.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mListInfo.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder=null;
			if(convertView==null) {
				holder=new ViewHolder();
				convertView=getLayoutInflater().inflate(R.layout.item_image, null);
				holder.imageView=(ImageView)convertView.findViewById(R.id.iv_item_hair);
				convertView.setTag(holder);
			}else {
				holder=(ViewHolder) convertView.getTag();
			}			
			String fileName=mListInfo.get(position);
			Bitmap bm=getResBitmap(fileName);	    
			holder.imageView.setImageBitmap(ImageUtil.getRoundedCornerBitmap(bm, 20));
			if(selectPos==position) {
				holder.imageView.setBackgroundResource(R.drawable.hairview_pressed);
			}else {
				holder.imageView.setBackgroundResource(R.drawable.transparent);
			}
			return convertView;
		}
		
		public class ViewHolder{
			private ImageView imageView;
			
		}
	}
	private Bitmap getResBitmap(String fileName) {
		InputStream ims;
		try {
			ims = assetManager.open(fileName);
			return BitmapFactory.decodeStream(ims);   
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * 系统相机拍照
	 */
	private void takePhoto() {
		// TODO Auto-generated method stub
		//执行拍照前，应该先判断SD卡是否存在
		String SDState = Environment.getExternalStorageState();
		if(!SDState.equals(Environment.MEDIA_MOUNTED))
		{
			AppToast.showShortText(ChangeHairActivity.this, "内存卡不存在");
			return;
		}
		try {				
			Uri photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					new ContentValues());
			if (photoUri != null) {
				Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
				startActivityForResult(i, SELECT_PIC_BY_TACK_PHOTO);
				
			} else {					
				AppToast.showShortText(ChangeHairActivity.this, "发生意外，无法写入相册");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			AppToast.showShortText(ChangeHairActivity.this, "发生意外，无法写入相册");
		}
	}
	 protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	        super.onActivityResult(requestCode, resultCode, intent);
	        if (resultCode == RESULT_OK) {
	        	switch (requestCode) {
	        	case SELECT_PIC_BY_TACK_PHOTO:	 
	        		//选择自拍结果
	        		String picPath=intent.getStringExtra("path");
	        		Bitmap bitmap=ImageUtil.getLocalThumbImg(picPath, 320, 640,"jpg");
	        		if(bitmap!=null)
	        		mModeIv.setImageBitmap(bitmap);
	        		break;
	        	case SELECT_PIC_BY_PICK_PHOTO:	    
	        		//选择图库图片结果
	        		picPath = ImageUtil.getPicPathFromUri(intent.getData(), this);
	        		bitmap=ImageUtil.getLocalThumbImg(picPath, 320, 640,"jpg");
	        		if(bitmap!=null)
	        			mModeIv.setImageBitmap(bitmap);
	        		break;
	        	}
	        		        	
	        }
	    }
	@Override
	public void onChoosePhoto() {
		// TODO Auto-generated method stub
		//从相册中取图片
		Intent choosePictureIntent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(choosePictureIntent, SELECT_PIC_BY_PICK_PHOTO);
	}
	@Override
	public void onTakePhoto() {
		// TODO Auto-generated method stub
		//takePhoto();
		Intent intent=new Intent(ChangeHairActivity.this,CameraActivity.class);
		startActivityForResult(intent, SELECT_PIC_BY_TACK_PHOTO);
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
