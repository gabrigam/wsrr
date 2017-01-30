package com.isp.wsrr.lookup.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.isp.wsrr.lookup.exception.LIBLKPWSRRTException;
import com.isp.wsrr.lookup.rest.WsrrLookup;

/**
 * 
 * <h4>The class contains a series of methods that wraps specific api rest to
 * 
 * interact with the IBM BPM product</h4>
 * 
 * <p>
 * 
 * The BPM connection credential are passed inside an object of type :
 * 
 * ConnectionDataBeanSingleton
 * 
 * <p>
 *
 *
 * 
 * 
 * 
 * @author Primeur
 * 
 * @version 1.0
 *
 * 
 * 
 */

public class WsrrUtility {

	private String endpoint = "";
	private String timeout = "";
	

	public String[] getTraceCatalogEndpointAndTimeout(String ispHeader, String serverType,String wsrrUrl, String wsrruser, String wsrrpassword) {

		String theQuery = "Metadata/JSON/PropertyQuery?query=/WSRR/GenericObject[@name='NBPLIBTRC'%20and%20@version='00']/gep63_provides(.)/gep63_availableEndpoints(.)[exactlyClassifiedByAllOf(.,'http://www.ibm.com/xmlns/prod/serviceregistry/profile/v8r0/RESTModel%23RESTServiceEndpoint')]&p1=name&p2=sm63_Timeout";

		JSONArray result;
		
		String[] enpointAndTimeout= new String[2];

		if (wsrrUrl != null && wsrrUrl.startsWith("https://")) {

			result = this.executeHttpsQuery(theQuery, ispHeader, serverType,wsrrUrl, wsrruser, wsrrpassword);

		} else {

			result = this.executeHttpQuery(theQuery, ispHeader, serverType,wsrrUrl, wsrruser, wsrrpassword);

		}
 
		try {
			
	

			JSONObject jso = (JSONObject) ((JSONArray) result.get(0)).get(0);

			endpoint = 		WsrrUtility

					.getObjectValueFromJSONArrayEndpointData(

							((JSONArray) result.get(0)), "name");
			
			if (((JSONArray)result.get(0)).length() == 2) {
				

				
				timeout=		WsrrUtility

						.getObjectValueFromJSONArrayEndpointData(

								((JSONArray) result.get(0)), "sm63_Timeout");
				
				if (timeout != null ) {
					
					try {
					
						Integer.parseInt(timeout);
					
						} catch (Exception ex) { //bad format 
						
						timeout="3";
						}	
					
				} else timeout="3";
				
			} else 
				timeout="3000";


		} catch (Exception e) {

			enpointAndTimeout=null;
		}

		if (enpointAndTimeout !=null) {
		enpointAndTimeout[0]=endpoint;
		enpointAndTimeout[1]=timeout;
		}
		
		return enpointAndTimeout;

	}
	
	public int getTraceCatalogTimeout(String ispHeader, String serverType, String wsrrUrl, String wsrruser, String wsrrpassword) {

		String theQuery = "Metadata/JSON/PropertyQuery?query=/WSRR/GenericObject[@name='NBPLIBTRC'%20and%20@version='00']/gep63_provides(.)/gep63_availableEndpoints(.)[exactlyClassifiedByAllOf(.,'http://www.ibm.com/xmlns/prod/serviceregistry/profile/v8r0/RESTModel%23RESTServiceEndpoint')]&p1=name&p2=sm63_Timeout";

		JSONArray result;

		if (wsrrUrl != null && wsrrUrl.startsWith("https://")) {

			result = this.executeHttpsQuery(theQuery, ispHeader,serverType, wsrrUrl, wsrruser, wsrrpassword);

		} else {

			result = this.executeHttpQuery(theQuery, ispHeader,serverType, wsrrUrl, wsrruser, wsrrpassword);

		}

		try {

			JSONObject jso = (JSONObject) ((JSONArray) result.get(0)).get(1);

			timeout = this.getValueFromJsonObject(jso, "value");

		} catch (Exception e) {

			e.printStackTrace();
		}
		
		//check timeout Validity
		
		if (timeout != null ) {
			
			try {
			
				Integer.parseInt(timeout);
			
				} catch (Exception ex) { //bad format 
				
				timeout="3000";
				}	
			
		} else timeout="3000";
		
		
		return Integer.parseInt(timeout);

	}
	
	

	private JSONArray executeHttpsQuery(String theQuery, String ispHeader,String serverType, String wsrrUrl, String wsrruser,
			String wsrrpassword) {

		URL url = null;

		JSONArray jsa = null;

		StringBuffer stb = new StringBuffer();

		HttpsURLConnection con = null;

		try {

			if (serverType.equals("BEA")) {
				 
				//added in version 2.4 
				url = new URL(null, stb.append(wsrrUrl).append(theQuery).toString() ,new sun.net.www.protocol.https.Handler());
				
			} else {
				
				url = new URL(stb.append(wsrrUrl).append(theQuery).toString());
				
			}

			con = (HttpsURLConnection) url.openConnection();

			stb.delete(0, stb.length());

			String userPassword = stb.append(wsrruser).append(":").append(wsrrpassword).toString();

			String encoding = new String(Base64.encodeBase64(userPassword.getBytes()));

			con.setRequestProperty("Authorization", "Basic " + encoding);

			con.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");

			con.setRequestProperty("Accept", "application/xml");

			con.setRequestProperty("Isp-Header", ispHeader);

			con.setRequestMethod("GET");

			con.connect();

			String res=print_content(con);
			if (res!=null && res.length() !=0) {
				jsa = new JSONArray(res);
				
			} else {
				jsa = new JSONArray("[]");
			}
				

		} catch (Exception e) {

			e.printStackTrace();

		} finally {

			if (con != null)
				con.disconnect();

		}

		return jsa;

	}

	private JSONArray executeHttpQuery(String theQuery, String ispHeader,String serverType, String wsrrUrl, String wsrruser,
			String wsrrpassword) {

		URL url = null;

		JSONArray jsa = null;

		StringBuffer stb = new StringBuffer();

		HttpURLConnection con = null;

		try {

			if (serverType.equals("BEA")) {
								 
				//added in version 2.4 
				url = new URL(null, stb.append(wsrrUrl).append(theQuery).toString() ,new sun.net.www.protocol.http.Handler());
				
			} else {
				url = new URL(stb.append(wsrrUrl).append(theQuery).toString());
				
			}
			
			con = (HttpURLConnection) url.openConnection();

			/*
			String userPassword = stb.append(wsrruser).append(":").append(wsrrpassword).toString();

			String encoding = new String(Base64.encodeBase64(userPassword.getBytes()));

			con.setRequestProperty("Authorization", "Basic " + encoding);
			
			*/

			con.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");

			con.setRequestProperty("Accept", "application/xml");

			con.setRequestProperty("Isp-Header", ispHeader.replaceAll("(\\r|\\n)", "").toString());

			con.setRequestMethod("GET");

			con.connect();

			jsa = new JSONArray(print_content(con));

		} catch (Exception e) {

			e.printStackTrace();

		} finally {

			if (con != null)
				con.disconnect();

		}

		return jsa;

	}

	private String getValueFromJsonObject(JSONObject jso, String key) {

		String result = "";

		try {

			result = jso.getString(key);

		} catch (JSONException e) {

			e.printStackTrace();

		}

		return result;

	}

	private String print_content(Object conObj) {

		StringBuffer out = new StringBuffer();

		BufferedReader br = null;

		try {

			if (conObj instanceof HttpsURLConnection) {

				br = new BufferedReader(new InputStreamReader(((HttpsURLConnection) conObj).getInputStream()));

			} else {

				br = new BufferedReader(new InputStreamReader(((HttpURLConnection) conObj).getInputStream()));

			}

			String input;

			while ((input = br.readLine()) != null) {

				out.append(input);

			}

			if (br != null)
				br.close();

		} catch (IOException e) {

			out.delete(0, out.length());

		} finally {

			try {

				if (br != null)

					br.close();

			} catch (IOException e) {

				out.delete(0, out.length());

			}

		}

		return out.toString();

	}

	public void restClientTracingServiceGet(String serviceurl, int timeout,String data) {

		HttpURLConnection con = null;

		try {
			
			//System.out.println(data);

			byte[] encoded = Base64.encodeBase64(data.getBytes());

			String sb64 = new String(encoded);

			URL url = new URL(serviceurl + sb64);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			con.setConnectTimeout(timeout);							// it!!
			con.setReadTimeout(timeout);

			con.getResponseCode();

		} catch (Exception e) {

			// todo logging
			e.printStackTrace();
			
		} finally {

			if (con != null)
				con.disconnect();
		}

	}

	public void restClientTracingServicePost(String serviceurl,int timeout, String data) {

		HttpURLConnection con = null;

		try {

			//System.out.println(data);
			//byte[] encoded = Base64.encodeBase64(data.getBytes());

			//String sb64 = new String(encoded);

			URL url = new URL(serviceurl);
			con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");

			con.setConnectTimeout(timeout); 
			con.setReadTimeout(timeout);

			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(con.getOutputStream());
			outputStreamWriter.write(data);
			outputStreamWriter.flush();
			con.getResponseCode();
			
			

		} catch (Exception e) {

			// todo logging
			//e.printStackTrace();
			
		} finally {

			if (con != null)
				con.disconnect();
		}

	}
	
	private static String getObjectValueFromJSONArrayEndpointData(JSONArray jsa, String key)
			throws LIBLKPWSRRTException {

		int i = 0;

		int elements = jsa.length();

		String current;

		JSONObject jso;

		String result = "";

		while (i < elements) {

			jso = jsa.getJSONObject(i);

			current = ((String) jso.get("name"));

			if (current.equals(key)) {

				try {

					current = (String) jso.get("value");

				} catch (Exception ex) {

					current = ""; // @TODO eccezione?

					// throw new LIBLKPWSRRTEException(new
					// StringBuffer().append(Messages.ERROR_10).append(key).toString());

				}

				result = WsrrUtility.getData(current);

				break;

			}

			i++;

		}

		return result;

	}
	
	private static String getData(String input) {

		return input.substring(input.indexOf("#", 0) + 1, input.length());

	}

}