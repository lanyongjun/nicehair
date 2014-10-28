package com.lan.nicehair.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

public class ImageUtil {

	/**
	 * 把图片转成圆角
	 * @param bitmap
	 * @param angle 图角角度 建议0~90
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap,float angle) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		//final float roundPx = 90;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, angle, angle, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}
	/**
     * 处理图片 放大、缩小到合适位置
     * 
     * @param newWidth
     * @param newHeight
     * @param bitmap
     * @return
     */
    public static Bitmap resizeBitmap(float newWidth, float newHeight, Bitmap bitmap) {
    	Matrix matrix = new Matrix();
    	matrix.postScale(newWidth / bitmap.getWidth(), newHeight / bitmap.getHeight());
    	Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    	return newBitmap;
    }
    /**旋转图片
	 * @param source
	 * @return
	 *
	 * @version:v1.0
	 * @author:lanyj
	 * @date:2014-7-8 上午11:58:22
	 */
	public static Bitmap changeRoate(Bitmap source,boolean isHeadCamera) {
		int orientation=90;
		Bitmap bMapRotate=null;
        if(source.getHeight() < source.getWidth()){
            	orientation = 90;
            if(isHeadCamera)
            	orientation=-90;
        } else {
            orientation = 0;
        }
        if (orientation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            bMapRotate = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
            		source.getHeight(), matrix, true);
        } else {
            return source;
        }
        return bMapRotate;
	}
	/**
	 * 获取图片路径
	 */
	public static String getPicPathFromUri(Uri uri, Activity activity) {
        String value = uri.getPath();

        if (value.startsWith("/external")) {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = activity.managedQuery(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else {
            return value;
        }
    }
	/**
	 * 读取本地的图片得到缩略图，如图片需要旋转则旋转。
	 * @param path
	 * @param width
	 * @param height
	 * @return
	 */
	public static  Bitmap getLocalThumbImg(String path,float width,float height,String imageType){
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(path,newOpts);//此时返回bm为空
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;//be=1表示不缩放
		if (w > h && w > width) {//如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / width);
		} else if (w < h && h > height) {//如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / height);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//设置缩放比例
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(path, newOpts);
		bitmap = compressImage(bitmap,100,imageType);//压缩好比例大小后再进行质量压缩
		int degree = readPictureDegree(path);
		bitmap = rotaingImageView(degree, bitmap);
		return bitmap;
	}
	/**
	 * 图片质量压缩
	 * @param image
	 * @size 图片大小（kb）
	 * @return
	 */
	public static Bitmap compressImage(Bitmap image, int size,String imageType) {
		try{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if(imageType.equalsIgnoreCase("png")) {
			image.compress(Bitmap.CompressFormat.PNG, 100, baos);
		}else {
			image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		}
		int options = 100;
		while ( baos.toByteArray().length / 1024 > size) {	//循环判断如果压缩后图片是否大于100kb,大于继续压缩		
			baos.reset();//重置baos即清空baos
			if(imageType.equalsIgnoreCase("png")) {
				image.compress(Bitmap.CompressFormat.PNG, options, baos);
			}else {
				image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
			}
			options -= 10;//每次都减少10
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
		return bitmap;
		}catch(Exception e){
			return null;
		}
	}
	/**
	 * 读取图片属性：旋转的角度
	 * 
	 * @param path
	 *            图片绝对路径
	 * @return degree旋转的角度
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}
	/**
	 * 旋转图片
	 * @param angle
	 * @param bitmap
	 * @return Bitmap
	 */
	public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		if(bitmap == null)
			return null;
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}
	 /**
     * 获取适应屏幕大小的图 
     */
    public static Bitmap sacleBitmap(Context context, Bitmap bitmap) {
        // 适配屏幕大小
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        float aspectRatio = (float) screenWidth / (float) width;
        int scaledHeight = (int) (height * aspectRatio);
        Bitmap scaledBitmap = null;
        try {
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, scaledHeight, false);
        } catch (OutOfMemoryError e) {
        }
        return scaledBitmap;
    }
}
