package com.lan.nicehair.common.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.lan.nicehair.utils.AppLog;


/**
 * lanyj
 * @date 2014-6-6
 */
public class HHttp {
	private String encode;							//编码格式,统一使用UFT-8
	private HttpClient httpClient;
	private HttpParams httpParams;
	private static int timeout;							//请求超时
	private int bufferSize;							//缓存大小
	private static final String TAG = "HHttp";

	public HHttp() {
		timeout = 30 * 10000;
		bufferSize = 8192;
		encode = "UTF-8";
		httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
		HttpConnectionParams.setSoTimeout(httpParams, timeout);
		HttpConnectionParams.setSocketBufferSize(httpParams, bufferSize);
		HttpClientParams.setRedirecting(httpParams, true);
		httpClient = new DefaultHttpClient(httpParams);
	}

	/**
	 * GET请求，无需参数
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public String doGet(String url) throws Exception {
		return doGet(url, null);
	}

	public String doPost(String url) throws Exception{
		return doPost(url, null);
	}

	/**
	 * GET请求，添加参数
	 * @param url
	 * @param params
	 * @return 请求的结果 String类型
	 * @throws Exception
	 */
	public String doGet(String url, Map<String,String> params) throws Exception {
		// 添加QueryString
		String paramStr = "";
		if (params != null) {
			Iterator<Entry<String, String>> iter = params.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, String> entry = (Entry<String, String>) iter.next();
				paramStr += "&" + entry.getKey() + "="+ URLEncoder.encode(entry.getValue(), encode);
			}
			if (paramStr.length() > 0)
				//把第一个”&“转为”？“
				paramStr=paramStr.substring(1, paramStr.length());
				url += "?"+paramStr;
			}
		// 创建HttpGet对象
		HttpGet get = new HttpGet(url);
		try {
			String strResp = "";
			// 发起请求
			AppLog.i(TAG, "doGet:" + url);
			HttpResponse resp = httpClient.execute(get);
			if (resp.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK)
				strResp = EntityUtils.toString(resp.getEntity());
			else
				// 如果返回的StatusCode不是OK则抛异常
				throw new Exception("Error Response:"
						+ resp.getStatusLine().toString());
			return strResp;
		} finally {
			get.abort();
		}
	}

	/**
	 * Post请求
	 * Date:2012-6-28
	 * @param url
	 * @param params
	 * @return 请求的结果 String类型
	 * @throws Exception 
	 */
	public String doPost(String url, Map<String, String> params) throws Exception{
		// POST参数组装
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		if (params != null) {
			Iterator<Entry<String, String>> iter = params.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, String> entry = (Entry<String, String>) iter.next();
				data.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
			}
			AppLog.i("params", data.toString());
		}
		HttpPost post = new HttpPost(url);
		try {
			// 添加请求参数到请求对象
			if (params != null)
				post.setEntity(new UrlEncodedFormEntity(data, HTTP.UTF_8));
				// 发起请求
				AppLog.i(TAG, "doPost:" + url);
				HttpResponse resp = httpClient.execute(post);
				String strResp = "";
				if (resp.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK)
					strResp = EntityUtils.toString(resp.getEntity());
				else if(resp.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_INTERNAL_ERROR){
					throw new Exception("Error JSON:"+ resp.getStatusLine().toString());
				}else
					// 如果返回的StatusCode不是OK则抛异常
					throw new Exception("Error Response:" + resp.getStatusLine().toString());
				AppLog.i(TAG, "result:"+strResp);
			return strResp;
		} finally {
			post.abort();
		}
	}
	
	public String doPostList(String url, List<String[]> params){
		// POST参数组装
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		if(params != null){
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < params.size(); i++) {
				String[] value = params.get(i);
				try {
					if(value[1] != null && value[1].length() > 0){
						sb.append(value[0] + "=" + value[1] + " ");
						data.add(new BasicNameValuePair(value[0], URLEncoder.encode(value[1],"utf-8")));
					}else{
						data.add(new BasicNameValuePair(value[0],value[1]));
					}
					
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			AppLog.i(TAG,"加密前:" + sb.toString());
			AppLog.i(TAG,"params:" + data.toString());
		}
		String strResp = null;
		HttpPost post = new HttpPost(url);
		try {			
			// 添加请求参数到请求对象
			if (params != null)
			post.setEntity(new UrlEncodedFormEntity(data, HTTP.UTF_8));
			// 发起请求
			AppLog.i(TAG, "doPost:" + url);
			HttpResponse resp = httpClient.execute(post);
			strResp = "";
			if (resp.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK)
				strResp = EntityUtils.toString(resp.getEntity());
			else if(resp.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_INTERNAL_ERROR){
				throw new Exception("Error JSON:"+ resp.getStatusLine().toString());
			}else{
				// 如果返回的StatusCode不是OK则抛异常
				throw new Exception("Error Response:" + resp.getStatusLine().toString());
			}
			AppLog.i(TAG, "result:"+strResp);
		}catch (Exception e) {
			//e.printStackTrace();
			AppLog.e(TAG, e.getMessage());
		}finally {
			post.abort();
		}
		return strResp;
		
	}

	/**
	 * @param url
	 *            - 需要访问的address
	 * @param data
	 *            - Request的内容字符串
	 * @param contentType
	 *            - Request的ContentType
	 * @return Response的字符串
	 * @throws Exception
	 */
	public String doPost(String url, String data, String contentType)throws Exception {
		HttpPost post = new HttpPost(url);
		try {
			// 添加请求参数到请求对象
			StringEntity se = new StringEntity(data, HTTP.UTF_8);
			se.setContentType(contentType);
			post.setEntity(se);
			// 发起请求
			AppLog.i(TAG, "doPost:" + url);
			HttpResponse resp = httpClient.execute(post);
			String strResp = "";
			if (resp.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK)
				strResp = EntityUtils.toString(resp.getEntity());
			else
				// 如果返回的StatusCode不是OK则抛异常
				throw new Exception("Error Response:"+ resp.getStatusLine().toString());
			return strResp;
		} finally {
			post.abort();
		}
	}
    
    /**
     * 提交数据到服务器
     * @param actionUrl 上传路径(注：避免使用localhost或127.0.0.1这样的路径测试，因为它会指向手机模拟器，你可以使用http://www.itcast.cn或http://192.168.1.10:8080这样的路径测试)
     * @param params 请求参数 key为参数名,value为参数值
     */
	public String post(String actionUrl, Map<String, String> params) {
		HttpPost httpPost = new HttpPost(actionUrl);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : params.entrySet()) {// 构建表单字段内容
			list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(list, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				return EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	public static String getWebContent(String httpUrl) {
		// System.out.println("开始读取内容...("+domain+")");
		StringBuffer sb = new StringBuffer();
		try {
			java.net.URL url = new java.net.URL(httpUrl);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			in.close();
		} catch (Exception e) { // Report any errors that arise
			AppLog.e(TAG, e.getMessage());
		}
		return sb.toString();
	}
	

	/**
	 * 模拟Web端Form表单提交，实现文件及参数上传
	 * 上传文件 要载入commons-httpclient-3.1.jar commons-codec.jar
	 * commons-logging.jar这三个包
	 * @param context
	 * @param url 
	 * @param params
	 * @param fileLocalPath
	 * @return
	 */
//	public static  String uploadFile(String url,List<String[]> params , String fileLocalPath) {
//		// 指定上传文件
//		//boolean result = false;
//		String result = null;
//		File targetFile = new File(fileLocalPath);
//		if(!targetFile.exists()){
//			AppLog.e(TAG, "文件不存在");
//			return null;
//		}
//		AppLog.e(TAG, "url:" + url);
//		PostMethod filePost = new PostMethod(url);
//		for(String[] array: params){
//			//filePost.addParameter(array[0],array[1]);
//			try {
//				filePost.setParameter(array[0],URLEncoder.encode(array[1], "utf-8"));
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
//		}
//		try {
//			Part[] parts = { new FilePart("headImg", targetFile),new FilePart("homeImg", targetFile) };
//			filePost.setRequestEntity(new MultipartRequestEntity(parts,filePost.getParams()));
//			org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
//			client.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
//			int status = client.executeMethod(filePost);
//			if (status == HttpStatus.SC_OK) {
//				//result = true;
//				// 上传成功
//				result = filePost.getResponseBodyAsString();
//			} else {
//				// 上传失败
//				AppLog.e(TAG, "ResultCode:" + status);
//			}
//			
//		} catch (Exception ex) {
//			AppLog.e(TAG, ex.getMessage());
//		} finally {
//			filePost.releaseConnection();
//		}
//		AppLog.e(TAG, "result:" + result);
//
//		return result;
//	}
//	
	
}
