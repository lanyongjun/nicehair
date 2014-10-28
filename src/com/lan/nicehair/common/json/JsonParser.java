package com.lan.nicehair.common.json;

import org.json.JSONException;

import com.lan.nicehair.common.json.FindHairParser.HairResult;
import com.lan.nicehair.common.json.HairCommentParser.HairComResult;
import com.lan.nicehair.common.json.ZoneAllParser.ZoneResult;
import com.lan.nicehair.common.net.ActionOfUrl.JsonAction;

public class JsonParser {

	synchronized public static JsonResult parse(String response, JsonAction act)
			throws JSONException {
		switch(act) {
		case FINDHAIR:
			return getFindHair(response);
		case ZONEALL:
			return getZoneAll(response);
		case HAIR_COMMENT:
			return getHairComment(response);
		default :
			return null;
			
		}
		
	}
	public static HairResult getFindHair(String json) throws JSONException {
		FindHairParser parser=new FindHairParser();
		return (HairResult) parser.parse(json);
	}
	public static ZoneResult getZoneAll(String json) throws JSONException {
		ZoneAllParser parser=new ZoneAllParser();
		return (ZoneResult) parser.parse(json);
	}
	public static HairComResult getHairComment(String json) throws JSONException {
		HairCommentParser parser=new HairCommentParser();
		return (HairComResult) parser.parse(json);
	}
}
