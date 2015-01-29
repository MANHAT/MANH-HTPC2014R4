package com.manh.wmos.services.mheoutbound.agile.data;

public class HTPCLineItem {

	private HTPCItem item;
	private HTPCInternational international;

	public HTPCInternational getInternational() {
		return international;
	}

	public void setInternational(HTPCInternational international) {
		if(null != international)
			this.international = international;
	}

	public HTPCItem getItem() {
		return item;
	}

	public void setItem(HTPCItem item) {
		if(null != item)
			this.item = item;
	}
	
	
}
