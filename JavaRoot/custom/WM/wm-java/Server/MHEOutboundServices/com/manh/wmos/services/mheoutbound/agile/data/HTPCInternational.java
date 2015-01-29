package com.manh.wmos.services.mheoutbound.agile.data;

import com.logistics.javalib.util.Misc;

public class HTPCInternational {
	
	private String countryOfOrigin = Misc.EMPTY_STRING;

	public String getCountryOfOrigin() {
		return countryOfOrigin;
	}

	public void setCountryOfOrigin(String countryOfOrigin) {
		this.countryOfOrigin = Misc.replaceNullWithEmptyString(countryOfOrigin);
	}
	
	

}
