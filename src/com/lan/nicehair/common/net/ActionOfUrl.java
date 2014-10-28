package com.lan.nicehair.common.net;
public class ActionOfUrl {

	public static final String BASE_URL="http://www.duitang.com/";
	
	public enum JsonAction{
		FINDHAIR,ZONEALL,POST_COMMENT,
		HAIR_COMMENT,
	}
	public static String getURL(JsonAction act,String url) {
		switch(act) {
		case FINDHAIR:
		case ZONEALL:
		case POST_COMMENT:
		case HAIR_COMMENT:
			return BASE_URL+url;
		default:
			return "";
		}
	}
}
