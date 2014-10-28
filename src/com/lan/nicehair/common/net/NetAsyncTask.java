package com.lan.nicehair.common.net;


import android.os.AsyncTask;
import android.os.Handler;

import com.lan.nicehair.R;
import com.lan.nicehair.utils.AppLog;
import com.lan.nicehair.utils.AppManager;

 

/**
 * AsyncTask 使用线程池控制线程数量，提高线程利用率 提供统一的网络访问接口 Response 提供Loading时的Dialog
 * 提供网络资源的清除 提供默认的Exception处理 运行环境 Android 1.6 TargetID >= 4
 * 
 * @author lanyj
 */
public abstract class NetAsyncTask extends AsyncTask<String, Integer, String> {
	
	private static final String TAG = "NetThread";
	public static final String HANDLE_SUCCESS = "0";
	public static final String HANDLE_FAILED = "1";

	private int dialogId=0;			//默认的预处理
	
	public int getDialogId() {
		return dialogId;
	}
	public void setDialogId(int dialogId) {
		this.dialogId = dialogId;
	}
	
	public NetAsyncTask(){}
	
	public NetAsyncTask(Handler uiHandler){
		this.uiHandler = uiHandler;
	}
	public NetAsyncTask(Handler uiHandler,int id){
		this.uiHandler = uiHandler;
	}
	
	public Handler uiHandler;                           					// 向Activity反馈相关UI更新
	public void setUiHandler(Handler uiHandler) {
		this.uiHandler = uiHandler;
	}

	public HttpTask httptask;
	
	/**
	 * 自定义预处理信息
	 */
	abstract protected void handlePreExecute();
	/**
	 * 相当于线程中的run函数，在线程中运行比较耗时的动作，比如访问网络，解析等
	 * @param arg0
	 * @return
	 * @throws Exception
	 */
	abstract protected String handleNetworkProcess(String... arg0) throws Exception;
	/**
	 * 对返回结果的处理
	 */
	abstract protected void handleResult();

	/** 
	 * 网络请求前的预处理
	 */
	@Override
	protected void onPreExecute() {
		//设置使用哪种进度条
		//1是默认，弹出框等待，2是重写handlePreExecute，进行自定义；其他均为不显示
		showDialog(dialogId);
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		 String result = null;
		try {
			httptask=new HttpTask(uiHandler);

			result = handleNetworkProcess(arg0);
		} catch (Exception e) {
			result = null;
			AppLog.d(TAG, e.getMessage());
		} finally {
			//closeConnection();
		}
		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		if ("0".equals(result)) {
			handleResult();
		}else if("1".equals(result)){
			//没有的得到数据
			//new Exception("网络异常!");
			handleResult();
			
		}else {
			new Exception("网络异常!");
			handleResult();
		}
		dismissDialog(getDialogId());
	}
	
	/**
	 * 显示进度
	 * @param dialogId
	 */
	private void showDialog(int dialogId) {
		switch (dialogId) {
		case 1:
			AppManager.updateUI(uiHandler, R.id.ui_show_dialog, R.id.dialog_waiting);
			break;
		case 2:
			 handlePreExecute();
			break;
		default:
			break;
		}
	}

	/**
	 * 取消进度显示
	 * @param dialogId
	 */
	private void dismissDialog(int dialogId) {
	
		switch (dialogId) {
		case 1:
			AppManager.updateUI(uiHandler, R.id.ui_dismiss_dialog, R.id.dialog_waiting);
			break;
		default:
			break;
		}
	}
	
	/**
	 * Positive close the running AsyncTask
	 * @param asyncTask
	 */
	public static void closeTask(final AsyncTask<?, ?, ?> asyncTask) {
		if (taskIsRunning(asyncTask)) {
			asyncTask.cancel(true);
		}
	}

	public static boolean taskIsRunning(final AsyncTask<?, ?, ?> asyncTask) {
		if (asyncTask != null&& (asyncTask.getStatus() == AsyncTask.Status.RUNNING)) {
			
			return true;
			
		} else {
			return false;
		}
	}
}
