package com.lan.nicehair.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

/**
 * 文件处理
 * @author lanyj
 *
 */
public class FileUtil {

	private static final String TAG = "FileUtil";
	/**
	 * 根据文件路径 递归创建文件
	 * 
	 * @param file
	 */
	public static void createDipPath(String file) {
		String parentFile = file.substring(0, file.lastIndexOf("/"));
		File file1 = new File(file);
		File parent = new File(parentFile);
		if (!file1.exists()) {
			parent.mkdirs();
			try {
				file1.createNewFile();
				AppLog.i(TAG,"Create new file :" + file);
			} catch (IOException e) {
				AppLog.e(TAG, e.getMessage());
			}
		}
	}
	/**
	 * 删除文件
	 * 
	 * @param path
	 */
	public static boolean deleteFile(String path) {
		boolean bl;
		File file = new File(path);
		if (file.exists()) {
			bl = file.delete();
		} else {
			bl = false;
		}
		return bl;
	}
	/**
	 * 将bitmap保存到本地
	 * 
	 * @param mBitmap
	 * @param imagePath
	 */
	@SuppressLint("NewApi")
	public static void saveBitmap(Bitmap bitmap, String imagePath,int s) {
		File file = new File(imagePath);
		createDipPath(imagePath);
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(imagePath.toLowerCase().endsWith(".png")){
			bitmap.compress(Bitmap.CompressFormat.PNG, s, fOut);
		}else if(imagePath.toLowerCase().endsWith(".jpg")){
			bitmap.compress(Bitmap.CompressFormat.JPEG, s, fOut);
		}else{
			bitmap.compress(Bitmap.CompressFormat.WEBP, s, fOut);
		}
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// 复制文件
		public static void copyFile(String sourcePath, String toPath) {
			File sourceFile = new File(sourcePath);
			File targetFile = new File(toPath);
			createDipPath(toPath);
			try {
				BufferedInputStream inBuff = null;
				BufferedOutputStream outBuff = null;
				try {
					// 新建文件输入流并对它进行缓冲
					inBuff = new BufferedInputStream(
							new FileInputStream(sourceFile));

					// 新建文件输出流并对它进行缓冲
					outBuff = new BufferedOutputStream(new FileOutputStream(
							targetFile));

					// 缓冲数组
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = inBuff.read(b)) != -1) {
						outBuff.write(b, 0, len);
					}
					// 刷新此缓冲的输出流
					outBuff.flush();
				} finally {
					// 关闭流
					if (inBuff != null)
						inBuff.close();
					if (outBuff != null)
						outBuff.close();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// 复制文件
		public static void copyFile(File sourceFile, File targetFile) {

			try {
				BufferedInputStream inBuff = null;
				BufferedOutputStream outBuff = null;
				try {
					// 新建文件输入流并对它进行缓冲
					inBuff = new BufferedInputStream(
							new FileInputStream(sourceFile));

					// 新建文件输出流并对它进行缓冲
					outBuff = new BufferedOutputStream(new FileOutputStream(
							targetFile));

					// 缓冲数组
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = inBuff.read(b)) != -1) {
						outBuff.write(b, 0, len);
					}
					// 刷新此缓冲的输出流
					outBuff.flush();
				} finally {
					// 关闭流
					if (inBuff != null)
						inBuff.close();
					if (outBuff != null)
						outBuff.close();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

}
