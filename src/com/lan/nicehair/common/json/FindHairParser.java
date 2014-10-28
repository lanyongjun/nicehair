package com.lan.nicehair.common.json;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.lan.nicehair.common.model.FindHairItem;

public class FindHairParser implements JsonI{

	@Override
	public JsonResult parse(String json) throws JSONException {
		// TODO Auto-generated method stub
		HairResult result = new HairResult();
		LinkedList<FindHairItem> mListInfo= new LinkedList<FindHairItem>();
		if(!TextUtils.isEmpty(json)) {
			result.setSuccess(true);
			JSONObject newsObject = new JSONObject(json);
            JSONObject jsonObject = newsObject.getJSONObject("data");
            JSONArray blogsJson = jsonObject.getJSONArray("blogs");
            for (int i = 0; i < blogsJson.length(); i++) {
                JSONObject jsonObj = blogsJson.getJSONObject(i);
                FindHairItem info = new FindHairItem();
                info.setHid(jsonObj.optString("albid"));
                info.setPicUrl(jsonObj.optString("isrc"));
                info.setTitle(jsonObj.optString("msg"));
                info.setPariseCount(jsonObj.optInt("favc"));
                info.setLookCount(jsonObj.optInt("iht"));
                info.setName(jsonObj.optString("unm"));
				info.setHeadUrl(jsonObj.optString("ava"));	
				info.setLevel(10);
                //info.setHeight(jsonObj.optInt("iht"));
                mListInfo.add(info);
            }
		}
		result.setmListInfo(mListInfo);
		return result;
	}

	public class HairResult extends JsonResult{
		private LinkedList<FindHairItem> mListInfo;

		public LinkedList<FindHairItem> getmListInfo() {
			return mListInfo;
		}

		public void setmListInfo(LinkedList<FindHairItem> mListInfo) {
			this.mListInfo = mListInfo;
		}
	}
}
