/**
 * ITEService.java
 * Created on June 23, 2008
 * Copyright � 2008 Manhattan Associates All rights reserved.
 * Do not copy, modify or redistribute this file without specific
 * written permission from Manhattan Associates.
 */

package com.manh.wmos.services.outbound.service;

import com.manh.ils.wmservices.domain.pk.UserWarehouseInfo;
import com.manh.te.manifest.domain.Manifest;

/**
 * The Interface exposing TE methods.
 * @author Adarsh Kesharwani
 */

public interface IHTPCTEService extends ITEService
{

	/**
	 * EX-02
	 * Weigh LPN. MHE Mode with tracking number update as per EX-02
	 * @param userInfo
	 * @param lpnNbr
	 * @param isAsynchronous
	 * @param lpnWeight
	 * @param trackingNbr
	 * @return
	 * @throws Exception
	 */
	public Manifest weighLPN(UserWarehouseInfo userInfo,String lpnNbr,
			boolean isAsynchronous, double lpnWeight,String trackingNbr)
					throws Exception;

}
