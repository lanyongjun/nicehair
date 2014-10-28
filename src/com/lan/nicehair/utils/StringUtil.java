package com.lan.nicehair.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.text.format.DateFormat;

/**
 * 字符串处理类
 * @author lanyj
 *
 */
public class StringUtil {
	private static String key2 = "Aedse_!#@..";
	private static String key1 = "13245";
	/** 年月日时分秒 */
	public final static String FORMAT_YMDHMS = "yyyy-MM-dd kk:mm:ss";
	/** 获得当前时间 */
	public static CharSequence currentTime(CharSequence inFormat) {
		return DateFormat.format(inFormat, System.currentTimeMillis());
	}

	public static String getWebCon(String domain) {
		// System.out.println("开始读取内容...("+domain+")");
		StringBuffer sb = new StringBuffer();
		try {
			java.net.URL url = new java.net.URL(domain);
			BufferedReader in = new BufferedReader(new InputStreamReader(url
					.openStream()));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			in.close();
		} catch (Exception e) { // Report any errors that arise
			sb.append(e.toString());
			System.err.println(e);
			System.err
					.println("Usage:   java   HttpClient   <URL>   [<filename>]");
		}
		return sb.toString();
	}

	/**
	 * 加密
	 * @param password
	 * @return
	 */
	public static String encryptionKey(String password) {
		byte[] keyByte1 = key1.getBytes();
		byte[] keyByte2 = key2.getBytes();
		byte[] pwdByte = password.getBytes();
		for (int i = 0; i < pwdByte.length; i++) {
			pwdByte[i] = (byte) (pwdByte[i] ^ keyByte1[i % keyByte1.length]);
		}
		byte[] countByte = new byte[pwdByte.length + keyByte1.length];
		for (int i = 0; i < countByte.length; i++) {
			if (i < pwdByte.length)
				countByte[i] = pwdByte[i];
			else
				countByte[i] = keyByte1[i - pwdByte.length];
		}
		for (int i = 0; i < countByte.length; i++) {
			countByte[i] = (byte) (countByte[i] ^ keyByte2[i % keyByte2.length]);
		}
		return bytesToHexString(countByte);
	}
	
	/**
	 * 解密
	 * @param password
	 * @return
	 */
	public static String decryptionKey(String password){
		byte[] keyByte1 = key1.getBytes();
		byte[] keyByte2 = key2.getBytes();
		//password = hexStr2Str(password);
		byte[] pwdByte = hexStr2Bytes(password);
		
		for (int i = 0; i < pwdByte.length; i++) {
			pwdByte[i] = (byte) (pwdByte[i] ^ keyByte2[i % keyByte2.length]);
		}
		
		byte[] lastByte = new byte[pwdByte.length - keyByte1.length];
		for (int i = 0; i < lastByte.length; i++) {
			lastByte[i] = pwdByte[i];
		}
		for (int i = 0; i < lastByte.length; i++) {
			lastByte[i] = (byte) (lastByte[i] ^ keyByte1[i % keyByte1.length]);
		}
		
		return new String(lastByte);
	}
	
	

	/**
	 * 把字节数组转换成16进制字符串
	 * @param bArray
	 * @return
	 */
	public static final String bytesToHexString(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}
	/**   
	 * 十六进制转换字符串  
	 * @param String str Byte字符串(Byte之间无分隔符 如:[616C6B])  
	 * @return String 对应的字符串  
	 */      
	public static String hexStr2Str(String hexStr)    
	{      
	    String str = "0123456789ABCDEF";      
	    char[] hexs = hexStr.toCharArray();      
	    byte[] bytes = new byte[hexStr.length() / 2];      
	    int n;      
	  
	    for (int i = 0; i < bytes.length; i++)    
	    {      
	        n = str.indexOf(hexs[2 * i]) * 16;      
	        n += str.indexOf(hexs[2 * i + 1]);      
	        bytes[i] = (byte) (n & 0xff);      
	    }      
	    return new String(bytes);      
	}  
	/**  
	 * bytes字符串转换为Byte值  
	 * @param String src Byte字符串，每个Byte之间没有分隔符  
	 * @return byte[]  
	 */    
	public static byte[] hexStr2Bytes(String src)    
	{    
	    int m=0,n=0;    
	    int l=src.length()/2;    
	    byte[] ret = new byte[l];    
	    for (int i = 0; i < l; i++)    
	    {    
	        m=i*2+1;    
	        n=m+1;    
	        ret[i] = Byte.decode("0x" + src.substring(i*2, m) + src.substring(m,n));    
	    }    
	    return ret;    
	}   
	
	
}
