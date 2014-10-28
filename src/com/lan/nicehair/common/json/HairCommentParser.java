package com.lan.nicehair.common.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.lan.nicehair.common.model.Comment;
import com.lan.nicehair.common.model.ZoneAllItem;

public class HairCommentParser implements JsonI{

	
	@Override
	public JsonResult parse(String json) throws JSONException {
		// TODO Auto-generated method stub
		HairComResult result = new HairComResult();
		List<Comment> listComment = new ArrayList<Comment>();
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
				for(int j=0;j<10;j++) {
					i++;
					if(i < blogsJson.length()) {
						Comment com=new Comment();
						jsonObj = blogsJson.getJSONObject(i);
						String url=jsonObj.optString("isrc");
						if(i%2==0) {
							com.setPicUrl(url);
						}
						com.setContent(jsonObj.optString("msg"));
						com.setHeadUrl(jsonObj.optString("ava"));
						com.setName(jsonObj.optString("unm"));
						com.setToUId(jsonObj.optInt("uid"));
						com.setId(jsonObj.optInt("id"));
						com.setTime((i+1)+"分钟前");
						listComment.add(com);
					}
				}
			}
		}
		result.setListComment(listComment);
		return result;
	}

	public class HairComResult extends JsonResult{
		private List<Comment> listComment;

		public List<Comment> getListComment() {
			return listComment;
		}

		public void setListComment(List<Comment> listComment) {
			this.listComment = listComment;
		}
	}
}
