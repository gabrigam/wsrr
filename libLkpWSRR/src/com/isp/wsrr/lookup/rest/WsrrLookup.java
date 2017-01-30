package com.isp.wsrr.lookup.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.isp.wsrr.lookup.commons.Messages;
import com.isp.wsrr.lookup.exception.LIBLKPWSRRAException;
import com.isp.wsrr.lookup.exception.LIBLKPWSRRTException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

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
 * @version 2.5
 *
 * 
 * 
 */

public class WsrrLookup {

	private ConnectionDataBeanSingleton cdb;

	@SuppressWarnings("unused")

	private WsrrLookup() {

	}

	public WsrrLookup(ConnectionDataBeanSingleton cdb) {

		super();

		this.cdb = cdb;

	}

	private String formatXmltraceData(String ispHeader, String catalogVersion, String serviceType,
			String serviceSubType, String serviceAcronym, String executionTS, String error) {

		StringBuffer sb = new StringBuffer();
		sb.append("<trace>").append("<header>").append(ispHeader).append("</header>");
		sb.append("<otherDataInput>").append("<serviceVersion>").append(catalogVersion).append("</serviceVersion>")
				.append("</otherDataInput>");
		sb.append("<output><serviceType>").append(serviceType).append("</serviceType>").append("<serviceSubType>")
				.append(serviceSubType).append("</serviceSubType>").append("<serviceAcronimo>").append(serviceAcronym)
				.append("</serviceAcronimo>");
		if (error != null && error.length() != 0)
			sb.append("<errorMessage><![CDATA[").append(error).append("]]></errorMessage>");
		sb.append("</output>");
		sb.append("<returnCode>KO</returnCode>");
		sb.append("<messageType>LKPWSRR</messageType>");
		sb.append("<hostName>").append(WsrrLookup.getHostName()).append("</hostName>")
				.append("<environment>todo</environment>");
		sb.append("<initialtimeStamp>").append(executionTS).append("</initialtimeStamp>");
		sb.append("<finaltimeStamp>").append(new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").format(new Date()))
				.append("</finaltimeStamp>");
		sb.append("</trace>");

		return sb.toString();

	}

	private String checkISPHeaderReturnMessage(String ispHeader) {

		String result = "";

		if (checkISPHeaderValidity(ispHeader)) {

			if (!isTagServiceIDValid(ispHeader) || !isTagApplicationIDValid(ispHeader)
					|| !isTagTimestampValid(ispHeader)) {

				result = Messages.ERROR_1;

			} else {

				if (getServiceIDTagValue(ispHeader).length() != 0
						&& getTagApplicationIDValue(ispHeader).length() != 0) {

					if (getTagTimestampCheckAndNormalize(ispHeader) == null)
						result = Messages.ERROR_10;
				} else {
					result = Messages.ERROR_2;
				}
			}

		} else {
			result = Messages.ERROR_1;
		}

		return result;

	}

	private static boolean checkISPHeaderValidity(String ispHeader) {

		boolean result = true;

		if (ispHeader == null || ispHeader == "" || ispHeader.indexOf("<ServiceID>") == -1

				|| ispHeader.indexOf("</ServiceID>") == -1 || ispHeader.indexOf("<ApplicationID>") == -1

				|| ispHeader.indexOf("</ApplicationID>") == -1 || ispHeader.indexOf("<Timestamp>") == -1
				|| ispHeader.indexOf("</Timestamp") == -1) {

			result = false;
		}

		return result;

	}

	private static String getHostName() {

		String hostName = "";
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// donothing
		}
		return hostName;

	}

	private static boolean isTagServiceIDValid(String ispHeader) {

		boolean result = false;

		int startindex = ispHeader.indexOf("<ServiceID>");

		int endindex = ispHeader.indexOf("</ServiceID>");

		if (startindex < endindex)
			result = true;

		return result;

	}

	private static String getServiceIDTagValue(String ispHeader) {

		String response = "";

		int startindex = ispHeader.indexOf("<ServiceID>");

		int endindex = ispHeader.indexOf("</ServiceID>");

		response = ispHeader.substring(startindex + 11, endindex);

		return response;

	}

	private static boolean isTagApplicationIDValid(String ispHeader) {

		boolean result = false;

		int startindex = ispHeader.indexOf("<ApplicationID>");

		int endindex = ispHeader.indexOf("</ApplicationID>");

		if (startindex < endindex)
			result = true;

		return result;

	}

	private static String getTagApplicationIDValue(String ispHeader) {

		String response = "";

		int startindex = ispHeader.indexOf("<ApplicationID>");

		int endindex = ispHeader.indexOf("</ApplicationID>");

		response = ispHeader.substring(startindex + 15, endindex);

		return response;

	}

	private static boolean isTagTimestampValid(String ispHeader) {

		boolean result = false;

		int startindex = ispHeader.indexOf("<Timestamp>");

		int endindex = ispHeader.indexOf("</Timestamp>");

		if (startindex < endindex)
			result = true;

		return result;

	}

	// added in version 2.4
	private static String getTagTimestampCheckAndNormalize(String ispHeader) {

		String ts = "";

		int startindex = ispHeader.indexOf("<Timestamp>");

		int endindex = ispHeader.indexOf("</Timestamp>");

		ts = ispHeader.substring(startindex + 11, endindex);

		return WsrrLookup.checkTimestamp(ts);

	}

	private static String getTagTimestamp(String ispHeader) {

		String ts = "";

		int startindex = ispHeader.indexOf("<Timestamp>");

		int endindex = ispHeader.indexOf("</Timestamp>");

		ts = ispHeader.substring(startindex + 11, endindex);

		return ts;

	}

	private boolean isErrorPresent(JSONObject jso) {

		boolean error = false;

		if (!jso.isNull("errore_LIBWSRRLKP"))

			error = true;

		return error;

	}

	private String concatenateString(String[] tokens) {

		String result = "";

		StringBuffer sb = new StringBuffer();

		int i = 0;

		while (i < tokens.length) {

			if (tokens[i] != null)

				sb.append(tokens[i]);

			i++;

		}

		if (sb.length() != 0)

			result = sb.toString();

		return result;

	}

	private static String getData(String input) {

		return input.substring(input.indexOf("#", 0) + 1, input.length());

	}

	private static String getObjectValueFromJSONArrayClassification(JSONArray jsa, String key, String field)
			throws LIBLKPWSRRTException {

		int i = 0;

		int elements = jsa.length();

		String current;

		JSONObject jso;

		String result = "";

		while (i < elements) {

			jso = jsa.getJSONObject(i);

			try {

				current = ((String) jso.get(key));

			} catch (Exception ex) {

				current = "";
			}

			if (current.startsWith(field)) {

				result = WsrrLookup.getData(current);

				break;

			}

			i++;

		}

		return result;

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

					current = "";
				}

				result = WsrrLookup.getData(current);

				break;

			}

			i++;

		}

		return result;

	}

	private static String formatError(Exception e) {

		StringWriter sw = new StringWriter();

		e.printStackTrace(new PrintWriter(sw));

		String error = "[[{\"errore_LIBWSRRLKP\": " + "\"" + sw.toString() + "\"}]]";

		error = error.replaceAll("(\\r|\\n)", "");

		return error;

	}

	private static String getValueFromJsonObject(JSONObject jso, String key) {

		String result = "";

		try {

			result = jso.getString(key);

		} catch (JSONException e) {
			// nothing todo
		}

		return result;

	}

	private static String specializeQuery(String theQuery, String catalogName, String catalogVersion) {

		String specializedQuery = theQuery.replaceAll("%CATALOGNAME%", catalogName);

		if (catalogVersion == null || catalogVersion == "")

			catalogVersion = "00";

		specializedQuery = specializedQuery.replaceAll("%VERSION%", catalogVersion);

		return specializedQuery;

	}

	private static String specializeQueryForClassification(String theQuery, String bsrUri) {

		String specializedQuery = theQuery.replaceAll("%BSRURI%", bsrUri);

		return specializedQuery;

	}

	private JSONArray queryExecutor(String theQuery, String ispHeader) {

		JSONArray result;

		if (cdb.getUrl() != null && cdb.getUrl().startsWith("https://")) {

			result = this.executeHttpsQuery(theQuery, ispHeader);

		} else {

			result = this.executeHttpQuery(theQuery, ispHeader);

		}

		return result;

	}

	private JSONArray executeHttpsQuery(String theQuery, String ispHeader) {

		String wsrrurl = cdb.getUrl();

		URL url = null;

		JSONArray jsa = null;

		StringBuffer stb = new StringBuffer();

		HttpsURLConnection con = null;

		try {

			if (cdb.getServerType().equals("BEA")) {
				// added in version 2.4

				url = new URL(null, stb.append(wsrrurl).append(theQuery).toString(),
						new sun.net.www.protocol.https.Handler());

			} else {

				url = new URL(stb.append(wsrrurl).append(theQuery).toString());

			}

			con = (HttpsURLConnection) url.openConnection();

			stb.delete(0, stb.length());

			String userPassword = stb.append(cdb.getUser()).append(":").append(cdb.getPassword()).toString();

			String encoding = new String(Base64.encodeBase64(userPassword.getBytes()));

			con.setRequestProperty("Authorization", "Basic " + encoding);

			con.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");

			con.setRequestProperty("Accept", "application/xml");

			con.setRequestProperty("Isp-Header", ispHeader);

			con.setRequestMethod("GET");

			con.connect();

			jsa = new JSONArray(print_content(con));

		} catch (Exception e) {

			jsa = new JSONArray(WsrrLookup.formatError(e));

		} finally {

			if (con != null)
				con.disconnect();

		}

		return jsa;

	}

	private JSONArray executeHttpQuery(String theQuery, String ispHeader) {

		String wsrrurl = cdb.getUrl();

		URL url = null;

		JSONArray jsa = null;

		StringBuffer stb = new StringBuffer();

		HttpURLConnection con = null;

		try {

			if (cdb.getServerType().equals("BEA")) {

				// added in version 2.4
				url = new URL(null, stb.append(wsrrurl).append(theQuery).toString(),
						new sun.net.www.protocol.http.Handler());

			} else {
				url = new URL(stb.append(wsrrurl).append(theQuery).toString());

			}

			con = (HttpURLConnection) url.openConnection();

			// String userPassword =
			// stb.append(cdb.getUser()).append(":").append(cdb.getPassword()).toString();

			// String encoding = new
			// String(Base64.encodeBase64(userPassword.getBytes()));

			// con.setRequestProperty("Authorization", "Basic " + encoding);

			con.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");

			con.setRequestProperty("Accept", "application/xml");

			con.setRequestProperty("Isp-Header", ispHeader.replaceAll("(\\r|\\n)", "").toString());

			con.setRequestMethod("GET");

			con.connect();

			jsa = new JSONArray(print_content(con));

		} catch (Exception e) {

			jsa = new JSONArray(WsrrLookup.formatError(e));

		} finally {

			if (con != null)
				con.disconnect();

		}

		return jsa;

	}

	private static String print_content(Object conObj) {

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

			out.append(WsrrLookup.formatError(e));

		} finally {

			try {

				if (br != null)

					br.close();

			} catch (IOException e) {

				out.delete(0, out.length());

				out.append(WsrrLookup.formatError(e));

			}

		}

		return out.toString();

	}

	private static ArrayList<HashMap<String, String>> searchInterface(ArrayList<HashMap<String, String>> interfaceList,

			String interfaceType) {

		int i = 0;

		int size = interfaceList.size();

		String currentInterfaceType = null;

		HashMap<String, String> currentMap = null;
		
		ArrayList<HashMap<String, String>> endpointArrayList = new ArrayList<HashMap<String, String>>();

		while (i < size) {

			currentMap = (HashMap<String, String>) interfaceList.get(i);

			currentInterfaceType = (String) currentMap.get("INTERFACETYPE");

			if (currentInterfaceType != null && currentInterfaceType.equals(interfaceType)) {
				
				endpointArrayList.add(currentMap);

			} 
			i++;
		}

		return endpointArrayList;

	}

	public String aboutLibVersion() {

		return "liblKpWSRR Version 2.6 Gennaio 2017";

	}

	public long elementsInCache() {

		if (cdb.getCache().getSize() == 0)
			return 0;
		return cdb.getCache().getSize() - 1;

	}

	@SuppressWarnings("unused")

	private HashMap<String, Object> getCatalogData(String ispHeader, String catalogVersion, String interfaceType)

			throws LIBLKPWSRRAException, LIBLKPWSRRTException {

		HashMap<String, Object> catalogHashMap = new HashMap<String, Object>();

		ArrayList<HashMap<String, String>> endpointArrayList = new ArrayList<HashMap<String, String>>();

		ArrayList<HashMap<String, String>> endpointArrayListREST = new ArrayList<HashMap<String, String>>();

		ArrayList<HashMap<String, String>> endpointArrayListSOAP= new ArrayList<HashMap<String, String>>();

		String query = "";

		String serviceType = "";

		String serviceSubType = "";

		String serviceAcronym = "";

		String endpointUrlREST = "";

		String endpointTimeoutREST = "";

		String endpointUrlSOAP = "";

		String endpointTimeoutSOAP = "";

		String flagISPHeaderSOAP = "";

		String flagISPHeaderREST = "";

		String organizationQuery = "";

		JSONObject jso = null;

		JSONArray result = null;

		JSONArray resultREST = null;

		JSONArray resultSOAP = null;

		JSONArray resultMQ = null;

		JSONArray jsa = null;

		String endpointQuery = "";

		String endpointQueryREST = "";

		String endpointQuerySOAP = "";

		String endpointQueryMQ = "";

		String catalog = null;

		String applicationId = null;

		String timestamp = null;

		String executionTS = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").format(new Date());

		boolean RESTInterfaceFound = false;
		boolean SOAPInterfaceFound = false;

		// normalize Ts in ispHeader
		ispHeader = ispHeader.replaceAll("<Timestamp>" + WsrrLookup.getTagTimestamp(ispHeader) + "</Timestamp>",
				"<Timestamp>" + WsrrLookup.getTagTimestampCheckAndNormalize(ispHeader) + "</Timestamp>");

		if (catalogVersion == null || catalogVersion.length() == 0)

			catalogVersion = "00";

		catalog = getServiceIDTagValue(ispHeader);
		applicationId = getTagApplicationIDValue(ispHeader);
		timestamp = getTagTimestampCheckAndNormalize(ispHeader);

		query = specializeQuery(Messages.QUERY_GET_SERVICEVERSION_URI_JSON, catalog, catalogVersion);

		result = queryExecutor(query, ispHeader);

		if (result.length() == 0) {

			catalogHashMap = null;

			trace(applicationId, executionTS,
					formatXmltraceData(ispHeader, catalogVersion, "", "", "", executionTS, ""));

		} else {

			jso = ((JSONArray) result.get(0)).getJSONObject(0);

			if (isErrorPresent(jso)) {

				trace(applicationId, executionTS, formatXmltraceData(ispHeader, catalogVersion, "", "", "", executionTS,
						WsrrLookup.getValueFromJsonObject(jso, "errore_LIBWSRRLKP")));
				if (!WsrrLookup.getValueFromJsonObject(jso, "errore_LIBWSRRLKP").contains("java.net.SocketException")
						&& !WsrrLookup.getValueFromJsonObject(jso, "errore_LIBWSRRLKP")
								.contains("java.net.UnknownHostException") &&  !WsrrLookup.getValueFromJsonObject(jso, "errore_LIBWSRRLKP")
								.contains("Server returned HTTP response code: 400"))
					throw new LIBLKPWSRRAException(WsrrLookup.getValueFromJsonObject(jso, "errore_LIBWSRRLKP"));
				else
					throw new LIBLKPWSRRTException(WsrrLookup.getValueFromJsonObject(jso, "errore_LIBWSRRLKP"));

			} else {

				query = specializeQueryForClassification(Messages.QUERY_GET_CLASSIFICATION_JSON,

						WsrrLookup.getValueFromJsonObject(jso, "value"));

				result = queryExecutor(query, ispHeader);

				if (result.length() == 0) {

					String[] message = new String[10];

					message[0] = Messages.ERROR_3;

					message[1] = catalog;

					trace(applicationId, executionTS, formatXmltraceData(ispHeader, catalogVersion, "", "", "",
							executionTS, concatenateString(message)));

					throw new LIBLKPWSRRTException(concatenateString(message));

				} else {

					// last fixed inserted in version 2.1
					// jso = (JSONObject) result.getJSONObject(0);

					if (!result.toString().contains("errore_LIBWSRRLKP")) {

						serviceType = WsrrLookup.getObjectValueFromJSONArrayClassification(result, "uri",

								"http://www.ibm.com/xmlns/prod/serviceregistry/profile/v6r3/GovernanceEnablementModel");

						try {
							serviceType = serviceType.substring(0, serviceType.indexOf("ServiceVersion"));

							serviceSubType = WsrrLookup.getObjectValueFromJSONArrayClassification(result, "uri",

									"http://isp/");
						} catch (Exception ex) {
							// added in version 2.4
							// if classification is not present error Exception
							// in thread "main"
							// java.lang.StringIndexOutOfBoundsException: String
							// index out of range: -1
							// for example if object is of type
							// ApplicationVersion
							// WsrrLookup.getObjectValueFromJSONArrayClassification(result,
							// "uri","http://isp/");
							// dont generate error but the ERROR_4 appears and
							// this is ok
						}

						if (serviceType != null

								&& (!serviceType.equals("SOPEN") && !serviceType.equals("SCOPEN"))) {

							String[] message = new String[10];

							message[0] = Messages.ERROR_4;

							message[1] = catalog;

							message[2] = " trovato : ";

							message[3] = serviceType;

							trace(applicationId, executionTS, formatXmltraceData(ispHeader, catalogVersion, serviceType,
									serviceSubType, "", executionTS, concatenateString(message)));

							throw new LIBLKPWSRRAException(concatenateString(message));

						} else {


								if (interfaceType.equals(Messages.REST_INTERFACE)
										|| interfaceType.equals(Messages.ALL_INTERFACE)) {

									endpointQueryREST = specializeQuery(Messages.QUERY_GET_REST_ENDPOINT_JSON, catalog,

											catalogVersion);

									resultREST = queryExecutor(endpointQueryREST, ispHeader);

								}

								if (interfaceType.equals(Messages.SOAP_INTERFACE)
										|| interfaceType.equals(Messages.ALL_INTERFACE)) {

									endpointQuerySOAP = specializeQuery(Messages.QUERY_GET_SOAP_ENDPOINT_JSON, catalog,

											catalogVersion);

									resultSOAP = queryExecutor(endpointQuerySOAP, ispHeader);

								}

									if (resultREST!= null && resultREST.length() != 0) {


										jso = ((JSONArray) resultREST.get(0)).getJSONObject(0);

										if (isErrorPresent((jso))) {

											trace(applicationId, executionTS, formatXmltraceData(ispHeader,
													catalogVersion, serviceType, serviceSubType, "", executionTS,
													WsrrLookup.getValueFromJsonObject(jso, "errore_LIBWSRRLKP")));

											throw new LIBLKPWSRRAException(

													WsrrLookup.getValueFromJsonObject(jso, "errore_LIBWSRRLKP"));

										}

										else {

											int i = 0;
											while (i < resultREST.length()) {

												endpointUrlREST = WsrrLookup

														.getObjectValueFromJSONArrayEndpointData(

																((JSONArray) resultREST.get(i)), "name");

												endpointTimeoutREST = WsrrLookup

														.getObjectValueFromJSONArrayEndpointData(

																((JSONArray) resultREST.get(i)), "sm63_Timeout");

												// added in version 2.4
												flagISPHeaderREST = WsrrLookup

														.getObjectValueFromJSONArrayEndpointData(

																((JSONArray) resultREST.get(i)),
																"rest80_ISPHEADER_FLAG");

												HashMap<String, String> localHashMap = new HashMap<String, String>();

												localHashMap.put("INTERFACETYPE", Messages.REST_INTERFACE);

												localHashMap.put("URL", endpointUrlREST);

												localHashMap.put("TIMEOUT", endpointTimeoutREST);

												localHashMap.put("FLAGISPHEADER", flagISPHeaderREST);

												endpointArrayList.add(localHashMap);

												RESTInterfaceFound = true;

												i++;
											}

										}

									}
									
									if (resultSOAP != null && resultSOAP.length() != 0) {

										jso = ((JSONArray) resultSOAP.get(0)).getJSONObject(0);

										if (isErrorPresent((jso))) {

											trace(applicationId, executionTS, formatXmltraceData(ispHeader,
													catalogVersion, serviceType, serviceSubType, "", executionTS,
													WsrrLookup.getValueFromJsonObject(jso, "errore_LIBWSRRLKP")));

											throw new LIBLKPWSRRAException(

													WsrrLookup.getValueFromJsonObject(jso, "errore_LIBWSRRLKP"));

										}

										else {

											int i = 0;
											while (i < resultSOAP.length()) {

												endpointUrlSOAP = WsrrLookup

														.getObjectValueFromJSONArrayEndpointData(

																((JSONArray) resultSOAP.get(i)), "name");

												endpointTimeoutSOAP = WsrrLookup

														.getObjectValueFromJSONArrayEndpointData(

																((JSONArray) resultSOAP.get(i)), "sm63_Timeout");

												// added in version 2.4
												flagISPHeaderSOAP = WsrrLookup

														.getObjectValueFromJSONArrayEndpointData(

																((JSONArray) resultSOAP.get(i)),
																"sm63_ISPHEADER_FLAG");

												HashMap<String, String> localHashMap = new HashMap<String, String>();

												localHashMap.put("INTERFACETYPE", Messages.SOAP_INTERFACE);

												localHashMap.put("URL", endpointUrlSOAP);

												localHashMap.put("TIMEOUT", endpointTimeoutSOAP);

												localHashMap.put("FLAGISPHEADER", flagISPHeaderSOAP);

												endpointArrayList.add(localHashMap);

												SOAPInterfaceFound = true;

												i++;
											}

										}

									}


							if (SOAPInterfaceFound || RESTInterfaceFound) {

								organizationQuery = specializeQuery(Messages.QUERY_GET_ORGANIZATION_JSON, catalog,

										catalogVersion);

								result = queryExecutor(organizationQuery, ispHeader);

								if (result.length() != 0) {

									jso = ((JSONArray) result.get(0)).getJSONObject(0);

									if (isErrorPresent(jso)) {

										trace(applicationId, executionTS,
												formatXmltraceData(ispHeader, catalogVersion, serviceType,
														serviceSubType, "", executionTS,
														WsrrLookup.getValueFromJsonObject(jso, "errore_LIBWSRRLKP")));

										throw new LIBLKPWSRRAException(
												WsrrLookup.getValueFromJsonObject(jso, "errore_LIBWSRRLKP"));

									} else {

										serviceAcronym = WsrrLookup.getValueFromJsonObject(jso, "value");

										catalogHashMap.put("SERVICETYPE", serviceType);

										catalogHashMap.put("SERVICESUBTYPE", serviceSubType);

										catalogHashMap.put("SERVICEACRONIMO", serviceAcronym);

										catalogHashMap.put("ENDPOINTARRAY", endpointArrayList);
										
										
										StringBuffer sb = new StringBuffer();

										sb.append("<trace>");
										sb.append("<header>").append(ispHeader).append("</header>");
										sb.append("<otherDataInput>").append("<serviceVersion>").append(catalogVersion).append("</serviceVersion>")
												.append("</otherDataInput>");
										sb.append("<output><serviceType>").append(serviceType).append("</serviceType>").append("<serviceSubType>")
												.append(serviceSubType).append("</serviceSubType>").append("<serviceAcronimo>").append(serviceAcronym)
												.append("</serviceAcronimo>");
										// fix inserted catalogHashMap!= null &&
										if (catalogHashMap != null
												&& (getEndpointRestData(catalogHashMap) != null || getEndPointSoapData(catalogHashMap) != null)) {
											sb.append("<endPointArray><![CDATA[");
											if (getEndpointRestData(catalogHashMap) != null)
												sb.append(getEndpointRestData(catalogHashMap).toString());
											if (getEndPointSoapData(catalogHashMap) != null)
												sb.append(getEndPointSoapData(catalogHashMap).toString());
											sb.append("]]>").append("</endPointArray>");
										}
										else {
											
											sb.append("<endPointArray></endPointArray>");
											
										}
										
										sb.append("</output>");
										sb.append("<returnCode>OK</returnCode>");
										sb.append("<messageType>LKPWSRR</messageType>");
										sb.append("<hostName>").append(WsrrLookup.getHostName()).append("</hostName>")
												.append("<environment>todo</environment>");
										sb.append("<initialtimeStamp>").append(executionTS).append("</initialtimeStamp>");
										sb.append("<finaltimeStamp>").append(new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").format(new Date()))
												.append("</finaltimeStamp>");
										// sb.append("</input>");
										sb.append("</trace>");

										trace(applicationId, executionTS, sb.toString());

									}

								} else {

									String[] message = new String[10];

									message[0] = Messages.ERROR_5;

									message[1] = catalog;

									
									trace(applicationId, executionTS, formatXmltraceData(ispHeader, catalogVersion,
											serviceType, serviceSubType, "", executionTS, concatenateString(message)));

									throw new LIBLKPWSRRTException(concatenateString(message));

								}

							} else {
								catalogHashMap = null;
							
							StringBuffer sb = new StringBuffer();

							sb.append("<trace>");
							sb.append("<header>").append(ispHeader).append("</header>");
							sb.append("<otherDataInput>").append("<serviceVersion>").append(catalogVersion).append("</serviceVersion>")
									.append("</otherDataInput>");
							sb.append("<output><serviceType>").append(serviceType).append("</serviceType>").append("<serviceSubType>")
									.append(serviceSubType).append("</serviceSubType>").append("<serviceAcronimo>").append(serviceAcronym)
									.append("</serviceAcronimo>");
							// fix inserted catalogHashMap!= null &&
							if (catalogHashMap != null
									&& (getEndpointRestData(catalogHashMap) != null || getEndPointSoapData(catalogHashMap) != null)) {
								sb.append("<endPointArray><![CDATA[");
								if (getEndpointRestData(catalogHashMap) != null)
									sb.append(getEndpointRestData(catalogHashMap).toString());
								if (getEndPointSoapData(catalogHashMap) != null)
									sb.append(getEndPointSoapData(catalogHashMap).toString());
								sb.append("]]>").append("</endPointArray>");
							}
							else {								
								sb.append("<endPointArray></endPointArray>");								
							}							
							sb.append("</output>");
							sb.append("<returnCode>KO</returnCode>");
							sb.append("<messageType>LKPWSRR</messageType>");
							sb.append("<hostName>").append(WsrrLookup.getHostName()).append("</hostName>")
									.append("<environment>todo</environment>");
							sb.append("<initialtimeStamp>").append(executionTS).append("</initialtimeStamp>");
							sb.append("<finaltimeStamp>").append(new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").format(new Date()))
									.append("</finaltimeStamp>");
							// sb.append("</input>");
							sb.append("</trace>");

							trace(applicationId, executionTS, sb.toString());
							
						}

						}

					} else {

						trace(applicationId, executionTS, formatXmltraceData(ispHeader, catalogVersion, serviceType,
								serviceSubType, "", executionTS, (result.toString())));

						throw new LIBLKPWSRRTException(result.toString());

					}

				}
			}

		}


		return catalogHashMap;

	}

	@SuppressWarnings("unchecked")
	protected void trace(String applicationId, String executionTS, String data)
			throws LIBLKPWSRRAException, LIBLKPWSRRTException {

		String url = "";
		int timeout = 0;
		String beforeTraceTs = "";
		StringBuffer sb = new StringBuffer();
		String[] endpointAndTimeout = null;

		if (cdb.getTrace()) {

			if (applicationId == null)
				applicationId = "?"; // if header is invalid for example

			Cache cache = cdb.getCache();
			Element element = cache.get("NBPLIBTRC");

			if (element == null) {

				sb.append("<ServiceID>").append("NBPLIBTRC").append("</ServiceID>").append("<ApplicationID>")
						.append(applicationId).append("</ApplicationID>").append("<Timestamp>").append(executionTS)
						.append("</Timestamp>");

				endpointAndTimeout = new com.isp.wsrr.lookup.utility.WsrrUtility().getTraceCatalogEndpointAndTimeout(
						sb.toString(), cdb.getServerType(), cdb.getUrl(), cdb.getUser(), cdb.getPassword());

				cache.put(new Element("NBPLIBTRC", endpointAndTimeout));

			} else {

				endpointAndTimeout = (String[]) element.getObjectValue();
			}

			if (endpointAndTimeout != null) {

				url = endpointAndTimeout[0];

				try {
					timeout = Integer.parseInt(endpointAndTimeout[1]);
				} catch (NumberFormatException ex) {
					timeout = 3;
				}

				if (url != null && url.length() != 0) {

					if (!url.contains("void://")) {

						new com.isp.wsrr.lookup.utility.WsrrUtility().restClientTracingServicePost(url, timeout, data);
					}

				} else
					throw new LIBLKPWSRRAException(Messages.ERROR_13);
			}

		}
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getServiceData(String ispHeader, String catalogVersion, String interfaceType)

			throws LIBLKPWSRRAException, LIBLKPWSRRTException {

		@SuppressWarnings("rawtypes")
		HashMap current = null;

		String headerValidationErrorMessage = checkISPHeaderReturnMessage(ispHeader);

		if (headerValidationErrorMessage.length() == 0) {

			// check first in cache

			Cache cache = cdb.getCache();

			String catalog = getServiceIDTagValue(ispHeader);

			Element element = cache.get(catalog+interfaceType);

			if (element == null) {

				current = getCatalogData(ispHeader, catalogVersion, interfaceType);

				// System.out.println("NO IN CACHE");
				if (current != null)
					cache.put(new Element(catalog+interfaceType, current));

			} else {

				String executionTS = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").format(new Date());
				current = (HashMap<String, Object>) element.getObjectValue();

				//
				StringBuffer sb = new StringBuffer();
				String applicationId = getTagApplicationIDValue(ispHeader);
				String serviceSubType = (String) current.get("SERVICESUBTYPE");
				String serviceType = (String) current.get("SERVICETYPE");
				String serviceAcronym = (String) current.get("SERVICEACRONIMO");
				ArrayList endpointData = (ArrayList) current.get("ENDPOINTARRAY");
				if (endpointData != null)
					endpointData.add("empty");

				sb.append("<trace>");
				sb.append("<header>").append(ispHeader).append("</header>");
				sb.append("<otherDataInput>").append("<serviceVersion>").append(catalogVersion)
						.append("</serviceVersion>").append("</otherDataInput>");
				sb.append("<output><serviceType>").append(serviceType).append("</serviceType>")
						.append("<serviceSubType>").append(serviceSubType).append("</serviceSubType>")
						.append("<serviceAcronimo>").append(serviceAcronym).append("</serviceAcronimo>");
				sb.append("<endPointArray><![CDATA[");
				sb.append(endpointData.toString()).toString();
				sb.append("]]>").append("</endPointArray>");
				sb.append("</output>");
				sb.append("<returnCode>OK</returnCode>");
				sb.append("<messageType>LKPWSRR</messageType>");
				sb.append("<hostName>").append(WsrrLookup.getHostName()).append("</hostName>")
						.append("<environment>todo</environment>");
				sb.append("<initialtimeStamp>").append(executionTS).append("</initialtimeStamp>");
				sb.append("<finaltimeStamp>")
						.append(new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").format(new Date()))
						.append("</finaltimeStamp>");
				sb.append("</trace>");

				trace(applicationId, executionTS, sb.toString());

			}

		} else {
	        //no trace this invalidate the message :)
			throw new LIBLKPWSRRTException(headerValidationErrorMessage);

		}
		return current;

	}

	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getServiceData(String ispHeader, String catalogVersion)

			throws LIBLKPWSRRAException, LIBLKPWSRRTException {

		HashMap current = null;

		String headerValidationErrorMessage = checkISPHeaderReturnMessage(ispHeader);

		if (headerValidationErrorMessage.length() == 0) {

			// check first in cache
			Cache cache = cdb.getCache();

			@SuppressWarnings("rawtypes")

			String catalog = getServiceIDTagValue(ispHeader);

			Element element = cache.get(catalog);

			if (element == null) {

				current = getCatalogData(ispHeader, catalogVersion, Messages.ALL_INTERFACE);

				// System.out.println("NO IN CACHE");
				if (current != null)
					cache.put(new Element(catalog, current));

			} else {

				String executionTS = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").format(new Date());
				current = (HashMap<String, Object>) element.getObjectValue();

				//
				StringBuffer sb = new StringBuffer();
				String applicationId = getTagApplicationIDValue(ispHeader);
				String serviceSubType = (String) current.get("SERVICESUBTYPE");
				String serviceType = (String) current.get("SERVICETYPE");
				String serviceAcronym = (String) current.get("SERVICEACRONIMO");
				ArrayList endpointData = (ArrayList) current.get("ENDPOINTARRAY");
				if (endpointData != null)
					endpointData.add("empty");

				sb.append("<trace>");
				sb.append("<header>").append(ispHeader).append("</header>");
				sb.append("<otherDataInput>").append("<serviceVersion>").append(catalogVersion)
						.append("</serviceVersion>").append("</otherDataInput>");
				sb.append("<output><serviceType>").append(serviceType).append("</serviceType>")
						.append("<serviceSubType>").append(serviceSubType).append("</serviceSubType>")
						.append("<serviceAcronimo>").append(serviceAcronym).append("</serviceAcronimo>");
				sb.append("<endPointArray><![CDATA[");
				sb.append(endpointData.toString()).toString();
				sb.append("]]>").append("</endPointArray>");
				sb.append("</output>");
				sb.append("<returnCode>OK</returnCode>");
				sb.append("<messageType>LKPWSRR</messageType>");
				sb.append("<hostName>").append(WsrrLookup.getHostName()).append("</hostName>")
						.append("<environment>todo</environment>");
				sb.append("<initialtimeStamp>").append(executionTS).append("</initialtimeStamp>");
				sb.append("<finaltimeStamp>")
						.append(new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").format(new Date()))
						.append("</finaltimeStamp>");
				sb.append("</trace>");

				trace(applicationId, executionTS, sb.toString());
			}

		} else {
			throw new LIBLKPWSRRTException(headerValidationErrorMessage);

		}

		return current;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })

	public HashMap<String, String> getEndpointData(HashMap<String, Object> dataMap)

			throws LIBLKPWSRRAException, LIBLKPWSRRTException {

		HashMap<String, String> resultHashMap = null;

		ArrayList<HashMap<String, String>> endPointArrayList = null;

		try {

			if (dataMap != null) {

				if (dataMap.get("ENDPOINTARRAY") != null) {

					endPointArrayList = (ArrayList) dataMap.get("ENDPOINTARRAY");

					if (endPointArrayList.size() > 1) {

						throw new LIBLKPWSRRTException(Messages.ERROR_8);

					} else {

						resultHashMap = (HashMap) endPointArrayList.get(0);

					}

				}

			} else {

				throw new LIBLKPWSRRTException(Messages.ERROR_9);

			}

		} catch (Exception ex) {

			throw new LIBLKPWSRRAException(WsrrLookup.formatError(ex));

		}

		return resultHashMap;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })

	public ArrayList<HashMap<String, String>> getEndpointRestData(HashMap<String, Object> dataMap)

			throws LIBLKPWSRRTException, LIBLKPWSRRAException {

		ArrayList<HashMap<String, String>> resultList = null;

		try {

			if (dataMap != null) {

				if (dataMap.get("ENDPOINTARRAY") != null) {

					resultList = searchInterface((ArrayList) dataMap.get("ENDPOINTARRAY"), Messages.REST_INTERFACE);

				}

			} else {

				throw new LIBLKPWSRRTException(Messages.ERROR_9);

			}

		} catch (Exception ex) {

			throw new LIBLKPWSRRAException(WsrrLookup.formatError(ex));

		}

		return resultList;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })

	public ArrayList<HashMap<String, String>>  getEndPointSoapData(HashMap<String, Object> dataMap)

			throws LIBLKPWSRRTException, LIBLKPWSRRAException {

		ArrayList<HashMap<String, String>> resultList = null;

		try {

			if (dataMap != null) {

				if (dataMap.get("ENDPOINTARRAY") != null) {

					resultList = searchInterface((ArrayList) dataMap.get("ENDPOINTARRAY"), Messages.SOAP_INTERFACE);

				}

			} else {

				throw new LIBLKPWSRRTException(Messages.ERROR_9);

			}

		} catch (Exception ex) {

			throw new LIBLKPWSRRAException(WsrrLookup.formatError(ex));

		}

		return resultList;

	}

	@SuppressWarnings("rawtypes")

	public int getAssociatedEndpointCount(HashMap<String, Object> dataMap)

			throws LIBLKPWSRRTException, LIBLKPWSRRAException {

		int result = 0;

		try {

			if (dataMap != null) {

				if (dataMap.get("ENDPOINTARRAY") != null) {

					result = ((ArrayList) dataMap.get("ENDPOINTARRAY")).size();

				}

			} else {

				throw new LIBLKPWSRRTException(Messages.ERROR_9);

			}

		} catch (Exception ex) {

			throw new LIBLKPWSRRAException(WsrrLookup.formatError(ex));

		}

		return result;

	}

	private static String checkTimestamp(String input) {

		boolean iscorrect = false;

		String ts = null;

		String other = null;

		if (input != null && input.length() >= 17 & input.length() <= 20) {

			String zero = "00000000";

			ts = input.substring(0, 14);

			other = input.substring(14, input.length());

			DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

			formatter.setLenient(false);

			try {

				formatter.parse(ts);

				Integer.parseInt(other);

				ts = ts + other;

				int difflen = 20 - ts.length();

				ts = ts.concat(zero.substring(0, difflen));

				iscorrect = true;

			} catch (Exception e) {

				iscorrect = false;

			}

		}

		if (iscorrect)
			return ts;

		else
			return null;

	}

}