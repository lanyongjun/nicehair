package com.lan.nicehair.common.model;

import java.io.Serializable;

public class FindHairItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int width;
	private int height=300;
	private String picUrl;
	private String hid;
	private int lookCount;
	private int pariseCount;
	private int chatCount;
	private String title;
	private String name;
	private int level;
	private String headUrl;
	public String getHeadUrl() {
		return headUrl;
	}
	public void setHeadUrl(String headUrl) {
		this.headUrl = headUrl;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public String getHid() {
		return hid;
	}
	public void setHid(String hid) {
		this.hid = hid;
	}
	public int getLookCount() {
		return lookCount;
	}
	public void setLookCount(int lookCount) {
		this.lookCount = lookCount;
	}
	public int getPariseCount() {
		return pariseCount;
	}
	public void setPariseCount(int pariseCount) {
		this.pariseCount = pariseCount;
	}
	public int getChatCount() {
		return chatCount;
	}
	public void setChatCount(int chatCount) {
		this.chatCount = chatCount;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
