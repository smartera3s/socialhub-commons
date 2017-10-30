package com.smartera.socialhub;


public class ErrorHandler {

	public static void handleError(String message, String errorMessage, Exception ex) {
			
		Logger.error(message);
		Logger.error(errorMessage);
		ex.printStackTrace();
	
	}

}
