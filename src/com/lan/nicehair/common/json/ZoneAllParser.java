package com.lan.nicehair.common.json;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.lan.nicehair.common.model.Comment;
import com.lan.nicehair.common.model.ZoneAllItem;

public class ZoneAllParser implements JsonI{

	@Override
	public JsonResult parse(String json) throws JSONException {
		// TODO Auto-generated method stub
		ZoneResult result = new ZoneResult();
		LinkedList<ZoneAllItem> mListInfo=new LinkedList<ZoneAllItem>();
		if(!TextUtils.isEmpty(json)) {
			result.setSuccess(true);
			JSONObject newsObject = new JSONObject(json);
			JSONObject jsonObject = newsObject.getJSONObject("data");
			JSONArray blogsJson = jsonObject.getJSONArray("blogs");	            
			for (int i = 0; i < blogsJson.length(); i++) {
				JSONObject jsonObj = blogsJson.getJSONObject(i);
				ZoneAllItem info=new ZoneAllItem();
				info.setTitle(jsonObj.optString("albnm"));
				info.setName(jsonObj.optString("unm"));
				info.setHeadUrl(jsonObj.optString("ava"));	                        
				info.setUid(jsonObj.optInt("uid"));
				info.setContent(jsonObj.optString("msg"));
				info.setLevel(10);
				info.setTime((i+1)+"分钟前");
				info.setPariseNum(jsonObj.optInt("favc"));
				info.setChatNum(3+i);
				info.setId(jsonObj.optInt("id"));
				String[] picArray=new String[3];
				List<Comment> listCom=new ArrayList<Comment>();
				for(int j=0;j<3;j++) {
					Comment com=new Comment();
					i++;
					if(i < blogsJson.length()) {
						jsonObj = blogsJson.getJSONObject(i);
						String url=jsonObj.optString("isrc");
						picArray[j]=url;
						com.setContent(jsonObj.optString("msg"));
						com.setHeadUrl(jsonObj.optString("ava"));
						com.setName(jsonObj.optString("unm"));
						com.setToUId(jsonObj.optInt("uid"));
						com.setId(jsonObj.optInt("id"));
						com.setTime((i+1)+"分钟前");
						listCom.add(com);
					}
				}
				info.setPicArray(picArray);
				info.setListComment(listCom);
				mListInfo.add(info);
			}
		}
		result.setListInfo(mListInfo);
		return result;
	}

	public class ZoneResult extends JsonResult{
		private LinkedList<ZoneAllItem> listInfo;

		public LinkedList<ZoneAllItem> getListInfo() {
			return listInfo;
		}

		public void setListInfo(LinkedList<ZoneAllItem> listInfo) {
			this.listInfo = listInfo;
		}
	}
}
