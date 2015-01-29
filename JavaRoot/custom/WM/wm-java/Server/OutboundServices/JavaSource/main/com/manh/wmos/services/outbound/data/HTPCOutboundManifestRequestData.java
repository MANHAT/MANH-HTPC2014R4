package com.manh.wmos.services.outbound.data;


/**
 * Contains EX-02 custom input parameters for Lpn on Manifest Request
 * @author psindhu
 *
 */
public class HTPCOutboundManifestRequestData extends OutboundManifestRequestData
{

	/** The Tracking Number. */
	public String trackingNbr;

	/**
	 * @return
	 */
	public String getTrackingNbr() {
		return trackingNbr;
	}

	/**
	 * @param trackingNbr
	 */
	public void setTrackingNbr(String trackingNbr) {
		this.trackingNbr = trackingNbr;
	}

}
