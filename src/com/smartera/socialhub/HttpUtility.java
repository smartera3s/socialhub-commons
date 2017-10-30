package com.smartera.socialhub;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.goebl.david.Request;
import com.goebl.david.Response;
import com.goebl.david.Webb;
import com.goebl.david.WebbException;

public class HttpUtility {

	private  static String logServicePath =  "http://localhost:8186/MonitoringService/storeLog";

	public void sendHttpResponse(Object data, ResponseState result, String reason, HttpServletResponse response) throws IOException{
		
		JSONObject responseBody = new JSONObject();
		
	    try {
			responseBody.put("data", data);
			JSONObject metaData = new JSONObject();
			metaData.put("result", result.getStateMsg());
			metaData.put("reason", reason);
			responseBody.put("metadata", metaData);
		} catch (JSONException e) {
			ErrorHandler.handleError("json object in response can't be created", e.getMessage(), e);
		}
			
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");

		switch(result){
			case FAILED: 
			    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			    break;
			case SUCCESS:
				response.setStatus(HttpServletResponse.SC_OK);
				break;
		}
		
		PrintWriter out;
		try {
			out = response.getWriter();
			out.print(responseBody);
			out.flush();
		} catch (IOException e) {
			ErrorHandler.handleError("response writer can't be generated", e.getMessage(), e);
		}		
	}
	
	

	@SuppressWarnings("unchecked")
	public static JSONObject doGetRequest(String url,String distinationServiceName, Map<String,Object> params, JSONObject headers, Object body, Class responseClass) {
 
		JSONObject response = new JSONObject();
		JSONObject metadata = new JSONObject();	
		 
		//log related parameters
		HashMap<String, Object> logParams = new HashMap<>();
		logParams.put("service_name", params.get("service_name"));
	    logParams.put("http_url", url);
	    logParams.put("http_request_params", params);
	    logParams.put("http_request_type", "GET");
	    logParams.put("user_id", "");
	    logParams.put("client_ip", "");   	
	    
		Webb webb = Webb.create();
		
		try{
			
			Request request =  webb.get(url);
		    Iterator<?> keys = headers.keys();
		    
		    while( keys.hasNext() ) {
		    	
		         String key = (String)keys.next();
		         
		         try {
		        	 
				    request.header(key, headers.get(key));
				    
			     } catch (JSONException e) {
			    	 
				    e.printStackTrace();
			     }
		    }
		    
		    //this could be replaced by if it doesn't has request Id, we add one
		    // if it has request Id, make it the ParentRequestId and generate a new one for this request
		    if(!headers.has("request-id")){
		    	request.header("request-id", UUID.randomUUID().toString());
		    }  
		    
		    request.params(params);
		    
		    if(body != null) request.body(body);
		    
		    request.useCaches(false);
		         		
			if(responseClass.equals(String.class)){
			
				Response<String> responseData = request
						 .asString();	
				try {
					metadata.put("statusCode", 1);
					metadata.put("reason", "SUCCESS");
					response.put("metadata",metadata);
					response.put("http_response", responseData);
					
				} catch (JSONException e) {				
					e.printStackTrace();
				}	
				try{
					
					log(logParams, distinationServiceName , metadata);	
				}catch (Exception e) {
					
				}					
			}else if(responseClass.equals(JSONObject.class)){
				
				Response<JSONObject> responseData = request
						 .asJsonObject();	

				try {
					metadata.put("statusCode", 1);
					metadata.put("reason", "SUCCESS");
					response.put("metadata", metadata);
					response.put("http_response", responseData);
				} catch (JSONException e) {
					
					e.printStackTrace();
				}
				try{
					log(logParams, distinationServiceName, metadata);	
				}catch (Exception e) {
					
				}	
			}else if (responseClass.equals(JSONArray.class)){
				
				Response<JSONArray> responseData = request
						 .asJsonArray();	
				try {
					metadata.put("statusCode", 1);
					metadata.put("reason", "SUCCESS");
					response.put("metadata",metadata);
					response.put("http_response", responseData);
				} catch (JSONException e) {
					
					e.printStackTrace();
				}
				try{
					log(logParams, distinationServiceName, metadata);	
				}catch (Exception e) {
					
				}	
			}	
		}catch (WebbException webbException){
			
			try {		
				metadata.put("statusCode", -1);
				metadata.put("reason", "FAILED : "+ webbException.getMessage());
				response.put("metadata",metadata);
				response.put("http_response", new JSONObject());
			} catch (JSONException e) {			
				e.printStackTrace();
			}			
			try{
				log(logParams, distinationServiceName, metadata);	
			}catch (Exception e) {			
			}			
		}			
		return response ;
	}
	
	
	@SuppressWarnings("unchecked")
	public static JSONObject doPostRequest(String url, String distinationServiceName, Map<String,Object> params, JSONObject headers, Object body, Class responseClass) {
		JSONObject response = new JSONObject();
		JSONObject metadata = new JSONObject();	
		HashMap<String, Object> logParams = new HashMap<>();
		logParams.put("service_name", params.get("service_name"));
	    logParams.put("http_url", url);
	    logParams.put("http_request_params", params);
	    logParams.put("http_request_type", "POST");
	    logParams.put("user_id", "");
	    logParams.put("client_ip", "");   		
		Webb webb = Webb.create();
		
		try{
			Request request =  webb.post(url);
		    Iterator<?> keys = headers.keys();
			 
		    while( keys.hasNext() ) {
		         String key = (String)keys.next();
		         try {
				    request.header(key, headers.get(key));
			     } catch (JSONException e) {
				    e.printStackTrace();
			     }
		    }
		    
		    if(!headers.has("request-id")){
		    	request.header("request-id", UUID.randomUUID().toString());
		    }		        
		    request.params(params);
		    if(body != null)request.body(body);
		    request.useCaches(false);
		         		
			if(responseClass.equals(String.class)){
			
				Response<String> responseData = request
						 .asString();	
				try {
					metadata.put("statusCode", 1);
					metadata.put("reason", "SUCCESS");
					response.put("metadata",metadata);
					response.put("http_response", responseData);
				} catch (JSONException e) {				
					e.printStackTrace();
				}	
				try{
					
					log(logParams, distinationServiceName , metadata);	
				}catch (Exception e) {
					
				}					
			}else if(responseClass.equals(JSONObject.class)){
				
				Response<JSONObject> responseData = request
						 .asJsonObject();	
				try {
					metadata.put("statusCode", 1);
					metadata.put("reason", "SUCCESS");
					response.put("metadata", metadata);
					response.put("http_response", responseData);
				} catch (JSONException e) {
					
					e.printStackTrace();
				}
				try{
					log(logParams, distinationServiceName, metadata);	
				}catch (Exception e) {
					
				}	
			}else if (responseClass.equals(JSONArray.class)){
				
				Response<JSONArray> responseData = request
						 .asJsonArray();	
				try {
					metadata.put("statusCode", 1);
					metadata.put("reason", "SUCCESS");
					response.put("metadata",metadata);
					response.put("http_response", responseData);
				} catch (JSONException e) {
					
					e.printStackTrace();
				}
				try{
					log(logParams, distinationServiceName, metadata);	
				}catch (Exception e) {
					
				}	
			}	
		}catch (WebbException webbException){
			
			try {		
				metadata.put("statusCode", -1);
				metadata.put("reason", "FAILED : "+ webbException.getMessage());
				response.put("metadata",metadata);
				response.put("http_response", new JSONObject());
			} catch (JSONException e) {			
				e.printStackTrace();
			}			
			try{
				log(logParams, distinationServiceName, metadata);	
			}catch (Exception e) {			
			}			
		}			
		return response ;
	}
	
	private static void log(HashMap<String, Object> params, String distinationServiceName, JSONObject responseMetaData){
		
		try {
			if(responseMetaData.getInt("statusCode") == 1){
				params.put("log_message", "Successed to get Http Response from " + distinationServiceName);
				params.put("http_response", "SUCCESS");
			}else if(responseMetaData.getInt("statusCode") == -1){
				params.put("log_message", "Failed to get Http Response from " +  distinationServiceName);
				params.put("http_response", "FAILED");
				params.put("http_response_reason", responseMetaData.getString("reason"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		params.put("log_time" , dateFormat.format(date));
	    params.put("log_time_unix", date.getTime()/1000);
	    
	    
		Webb webb =  Webb.create();		
		Response<JSONObject>res = webb.post(logServicePath)
		    .useCaches(false)
		    .params(params)
		    .asJsonObject();
		

		
	}
	
}
