package com.lan.nicehair.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.lan.nicehair.R;
import com.lan.nicehair.utils.ImageUtil;
import com.lan.nicehair.widget.CameraPreview;
import com.lan.nicehair.widget.CameraPreview.OnCameraStatusListener;

/**
 * 自定义的拍照界面
 * @author lanyj
 *
 */
public class CameraActivity extends BaseActivity implements OnCameraStatusListener{

	public static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	public static final String PATH = Environment.getExternalStorageDirectory()
			.toString() + "/Camera/";//拍照完后图片保存目录

	@InjectView(R.id.camera_surfaceView) CameraPreview mCameraPreview;//拍照预览
	@OnClick(R.id.back)
	public void onBackClicked(View v) {
		finish();
	}
	@OnClick(R.id.change_camera)
	public void onChangeCameraClicked(View v) {
		//切换镜头
		mCameraPreview.changeCameraFacing();
	}
	@OnClick(R.id.take_pictureIv)
	public void onTakePhotoClicked(View v) {
		mCameraPreview.takePicture();
	}
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private static final long VIBRATE_DURATION = 200L;
	private boolean vibrate;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_camera);
		ButterKnife.inject(this);
		mCameraPreview.setOnCameraStatusListener(this);
	}
	/**
	 * 存储图像并将信息添加入媒体数据库
	 */
	private Uri insertImage(ContentResolver cr, String name, long dateTaken,
			String directory, String filename, Bitmap source, byte[] jpegData) {
		OutputStream outputStream = null;
		String filePath = directory + filename;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inSampleSize = 6;
	        options.inDither = false;
	        options.inPurgeable = true;
	        options.inInputShareable = true;
	        options.inTempStorage = new byte[32 * 1024];
	        options.inPreferredConfig = Bitmap.Config.RGB_565;
	        if(source==null&&jpegData!=null) {
	        	source = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
	        }
	        boolean isHeadCamera=false;
			if(mCameraPreview.cameraPosition==0) {
				isHeadCamera=true;
			}
	        Bitmap bMapRotate=ImageUtil.changeRoate(source,isHeadCamera);	       
	        File dir = new File(directory);
	        if (!dir.exists()) {
	        	dir.mkdirs();
	        }
	        File file = new File(directory, filename);
	        if (file.createNewFile()) {
	        	outputStream = new FileOutputStream(file);
	        	if (bMapRotate != null) {
	        		bMapRotate.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
	        		if (bMapRotate != null) {
	        			bMapRotate.recycle();
	        			bMapRotate = null;
	        		}
	        	}
//	        	else {
//					outputStream.write(jpegData);
//				}
	        }	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Throwable t) {
				}
			}
		}
		ContentValues values = new ContentValues(7);
		values.put(MediaStore.Images.Media.TITLE, name);
		values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
		values.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		values.put(MediaStore.Images.Media.DATA, filePath);
		return cr.insert(IMAGE_URI, values);
	}
	/**保存拍照后图像
	 */
	private Uri saveCameraMix(Bitmap newBitmap) {
		Uri uri=null;
		// 系统时间
		long dateTaken = System.currentTimeMillis();
		// 图像名称
		String filename = DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken)
				.toString() + ".jpg";
			// 存储图像（PATH目录）
		uri = insertImage(getContentResolver(), filename, dateTaken, PATH,
					filename, newBitmap, null);
		return uri;
	}
	/**初始化照相声音文件
	 */
	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnCompletionListener(beepListener);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			setMediaResorce();
		}
	}
	private void setMediaResorce() {
		try {
			mediaPlayer.reset();
			Uri cameraUri = Uri.parse("file:///system/media/audio/ui/camera_click.ogg");
			mediaPlayer.setDataSource(CameraActivity.this,cameraUri);
			mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
			mediaPlayer.prepare();
		} catch (Exception e) {
			mediaPlayer = null;
		}
	}
	/**播放声音
	 */
	private void playBeepSound() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}		
		playVibrate();
	}
	private void playVibrate() {
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}
	/**
	 * 监听声音播放完成
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);	
		}
	};

	@Override
	public void onCameraStopped(byte[] data) {
		// TODO Auto-generated method stub
		Log.e("onCameraStopped", "==onCameraStopped==");
		String picPath=null;
		try {
			// 创建图像
			BitmapFactory.Options newOpts = new BitmapFactory.Options();
			newOpts.inDither = false;
			newOpts.inPurgeable = true;
			newOpts.inInputShareable = true;
			newOpts.inTempStorage = new byte[100 * 1024];//100k缓存
			newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
			//newOpts.inSampleSize=3;
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,newOpts);
			boolean isHeadCamera=false;
			if(mCameraPreview.cameraPosition==0) {
				isHeadCamera=true;
			}
			Bitmap bitTemp=ImageUtil.changeRoate(bitmap,isHeadCamera);
			Uri uri=saveCameraMix(bitTemp);
			picPath=ImageUtil.getPicPathFromUri(uri, this);
			if(bitTemp!=null)
				bitTemp.recycle();
		} catch (Exception e) {
			// TODO: handle exception
		}
		Intent intent=getIntent();
		intent.putExtra("path", picPath);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	@Override
	public void onAutoFocus(boolean success) {
		// TODO Auto-generated method stub
		playBeepSound();		
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
	}
}
