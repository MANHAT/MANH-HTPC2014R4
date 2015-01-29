/*
gen\ * Copyright � 2009 Manhattan Associates All rights reserved. Do not copy,
 * modify or redistribute this file without specific written permission from
 * Manhattan Associates.
 */
package com.manh.te.manifest.application.service;

import com.logistics.javalib.util.Misc;
import com.manh.te.common.validator.Validator;
import com.manh.te.common.validator.util.TEValidationFactory;
import com.manh.te.manifest.application.service.HTPCIManifestLpnProcessor;
import com.manh.te.manifest.application.service.ManifestLpnProcessor;
import com.manh.te.manifest.domain.Manifest;
import com.manh.te.manifest.domain.ManifestInputData;

public class HTPCManifestLpnProcessor  extends ManifestLpnProcessor implements
		HTPCIManifestLpnProcessor {
	
	public void validate(Manifest manifest) {
        // get validator
        Validator lpnValidator = TEValidationFactory
                                        .getValidator(Validator.LPN_VALIDATOR);
        
        //EX02 - get the tracking from Lpn
        String trackingNbr = Misc.EMPTY_STRING;
        if(manifest.getManifestInputData().getLpn() != null && !Misc.isNullTrimmedString(manifest.getManifestInputData().getLpn().getTrackingNbr()))
        	trackingNbr = manifest.getManifestInputData().getLpn().getTrackingNbr();

        // validate lpn, shipvia, order, and parcelOriginAttr, shipment
        lpnValidator.performValidation(manifest.getTcCompanyId(), manifest,
                                        true);
        
        //EX02 - set it back to manifest.
        if(!Misc.isNullTrimmedString(trackingNbr))
        {
        	manifest.getManifestInputData().getLpn().setTrackingNbr(trackingNbr);
        }
                        
	}

}
