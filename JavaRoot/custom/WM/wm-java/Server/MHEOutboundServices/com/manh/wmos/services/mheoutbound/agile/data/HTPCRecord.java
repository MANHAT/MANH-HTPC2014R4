package com.manh.wmos.services.mheoutbound.agile.data;

import com.logistics.javalib.util.Misc;

public class HTPCRecord {
	
	private String keyThree = Misc.EMPTY_STRING;

	public String getKeyThree() {
		return keyThree;
	}

	public void setKeyThree(String keyThree) {
		this.keyThree = Misc.replaceNullWithEmptyString(keyThree);
	}
	
	

}
