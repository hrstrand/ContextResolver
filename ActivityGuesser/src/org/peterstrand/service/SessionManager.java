package org.peterstrand.service;

public class SessionManager {


	
	public static MovementSession createNewSession( SensorService service) {
		MovementSession result = MovementSession.createNewSession( service);
		return result;
	}
	
}
