package com.blacklabelops.crow.application.config;

public class Environment implements IConfigModel {

	private String key;

	private String value;

	public Environment() {
		super();
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
