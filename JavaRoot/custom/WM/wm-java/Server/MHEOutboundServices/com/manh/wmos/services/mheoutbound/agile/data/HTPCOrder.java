package com.manh.wmos.services.mheoutbound.agile.data;

import com.logistics.javalib.util.Misc;

public class HTPCOrder {

	private String shipViaCode = Misc.EMPTY_STRING;

	public String getShipViaCode() {
		return shipViaCode;
	}

	public void setShipViaCode(String shipViaCode) {
		this.shipViaCode = Misc.replaceNullWithEmptyString(shipViaCode);
	}
	
	
}
