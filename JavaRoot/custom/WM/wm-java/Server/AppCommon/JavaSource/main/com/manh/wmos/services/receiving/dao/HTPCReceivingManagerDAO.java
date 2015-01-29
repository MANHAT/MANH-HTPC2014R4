package com.manh.wmos.services.receiving.dao;

import java.util.HashMap;
import java.util.List;

import com.logistics.javalib.persistence.jdbc.SQLQuery;

public interface HTPCReceivingManagerDAO extends ReceivingManagerDAO {
	
	
	public HashMap getSerialNbrMap(Long lpnId);
	
	public HashMap getItemStyleAndSuffix(Long lpnId);

}
