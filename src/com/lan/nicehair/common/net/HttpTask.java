package com.lan.nicehair.common.net;

import java.util.Map;

import org.json.JSONException;

import android.os.Handler;

import com.lan.nicehair.R;
import com.lan.nicehair.common.json.JsonParser;
import com.lan.nicehair.common.json.JsonResult;
import com.lan.nicehair.common.net.ActionOfUrl.JsonAction;
import com.lan.nicehair.utils.AppLog;
import com.lan.nicehair.utils.AppManager;


public class HttpTask {
	
	//private static final String FILE_NAME = "/sdcard/testrequest.json";
	//private static final int MaxBufferSize = 8 * 1024;
	
	private static final String TAG="HttpTask";
	private Handler uiHandler;
	HHttp hhttp=null;
	public HttpTask(Handler uiHandler) {
		this.uiHandler = uiHandler;
		hhttp = new HHttp();
	}
	
	public HttpTask(){
		hhttp = new HHttp();
	}

	
	
	
	/**
	 * 上传单个文件
	 * Date:2014-6-10
	 * @author lanyj
	 * @param fun
	 * @param mapstr
	 * @param formFile
	 * @return 
	 * @return JsonResult
	 */
	/*public JsonResult getReqPOSTForFile(JsonAction act,String url,Map<String, String> mapstr,FormFile formFile){
		String urlrequest=null;
		JsonResult requestResult=null;
		
		try {
			urlrequest = HHttp.post(ActionOfUrl.getURL(act, url), mapstr, formFile);
			//toFile(urlrequest.getBytes());
			requestResult=JsonParser.parse(urlrequest, act);
		} catch (JSONException e) {
			e.printStackTrace();
			AppLog.d(TAG, e.getMessage());
			ScreenManager.updateUI(uiHandler, R.id.ui_show_text,R.string.exception_parse);
		} catch (Exception e) {
			e.printStackTrace();
			AppLog.d(TAG, e.getMessage());
			ScreenManager.updateUI(uiHandler, R.id.ui_show_text,R.string.exception_timeout);
		}		
		return requestResult;
	}*/
	

	public JsonResult getRequestbyPOST(JsonAction act,String url,Map<String, String> mapstr){
		String urlrequest=null; 
		JsonResult requestResult=null;
		try {
			urlrequest = hhttp.doPost(ActionOfUrl.getURL(act, url), mapstr);
			requestResult=JsonParser.parse(urlrequest, act);
		} catch (JSONException e) {
			e.printStackTrace();
			AppLog.d(TAG, e.getMessage());
			AppManager.updateUI(uiHandler, R.id.ui_show_text,R.string.exception_parse);
		} catch (Exception e) {
			e.printStackTrace();
			AppLog.d(TAG, e.getMessage());
			AppManager.updateUI(uiHandler, R.id.ui_show_text,R.string.exception_timeout);
		}	
		return requestResult;
	}
	
	public JsonResult getRequestbyGET(JsonAction act,String url,Map<String, String> mapstr){
		String urlrequest=null; 
		JsonResult requestResult=null;
		try {
			urlrequest = hhttp.doGet(ActionOfUrl.getURL(act, url), mapstr);
			requestResult=JsonParser.parse(urlrequest, act);
		} catch (JSONException e) {
			e.printStackTrace();
			AppLog.d(TAG, e.getMessage());
			AppManager.updateUI(uiHandler, R.id.ui_show_text,R.string.exception_parse);
		} catch (Exception e) {
			e.printStackTrace();
			AppLog.d(TAG, e.getMessage());
			AppManager.updateUI(uiHandler, R.id.ui_show_text,R.string.exception_timeout);
		}	
		return requestResult;
	}

}
