/*
 * Copyright &#169; 2009 Manhattan Associates All rights reserved. Do not copy,
 * modify or redistribute this file without specific written permission from
 * Manhattan Associates.
 */
package com.manh.te.manifest.application.service;

import com.manh.te.manifest.application.service.IManifestLpnProcessor;
import com.manh.te.manifest.domain.Manifest;

/**
 * The Interface HTPCIManifestLpnProcessor.
 */
public interface HTPCIManifestLpnProcessor extends IManifestLpnProcessor {
	
	public void validate(Manifest manifest);
	
	
}
