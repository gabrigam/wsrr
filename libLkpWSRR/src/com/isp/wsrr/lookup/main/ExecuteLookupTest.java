package com.isp.wsrr.lookup.main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.isp.wsrr.lookup.exception.LIBLKPWSRRAException;
import com.isp.wsrr.lookup.exception.LIBLKPWSRRTException;
import com.isp.wsrr.lookup.rest.ConnectionDataBeanSingleton;
import com.isp.wsrr.lookup.rest.WsrrLookup;

public class ExecuteLookupTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ConnectionDataBeanSingleton cdb = null;


			try {
				cdb = ConnectionDataBeanSingleton.setData();
				
			} catch (Exception e1) {
              e1.printStackTrace();
              System.exit(0);
			}
			
			WsrrLookup ra = new WsrrLookup(cdb);
			System.out.println(ra.aboutLibVersion());
			HashMap data = new HashMap();

			String headerISP_Base="<ISPWebservicesHeader><RequestInfo><TransactionId/><Timestamp>%TIMESTAMP%</Timestamp><ServiceID>%SERVICE%</ServiceID><ServiceVersion/><Language/></RequestInfo><OperatorInfo/><CompanyInfo><ISPCallerCompanyIDCode/><ISPServiceCompanyIDCode/><ISPBranchCode/><NotISPCompanyIDCode/></CompanyInfo><BusinessInfo><CustomerID/><BusinessProcessName/><BusinessProcessID>61619</BusinessProcessID><BusinessOperation/><BusinessFileID/></BusinessInfo><TechnicalInfo><ChannelIDCode/><ApplicationID>MUPR0</ApplicationID><CallerServerName/><CallerProgramName/></TechnicalInfo><AdditionalBusinessInfo><Param/><Param/><Param/><Param/><Param/><Param/></AdditionalBusinessInfo></ISPWebservicesHeader>";
			String headerISP_Current="<ISPWebservicesHeader><RequestInfo><TransactionId/><Timestamp>%TIMESTAMP%</Timestamp><ServiceID>%SERVICE%</ServiceID><ServiceVersion/><Language/></RequestInfo><OperatorInfo/><CompanyInfo><ISPCallerCompanyIDCode/><ISPServiceCompanyIDCode/><ISPBranchCode/><NotISPCompanyIDCode/></CompanyInfo><BusinessInfo><CustomerID/><BusinessProcessName/><BusinessProcessID>61619</BusinessProcessID><BusinessOperation/><BusinessFileID/></BusinessInfo><TechnicalInfo><ChannelIDCode/><ApplicationID>MUPR0</ApplicationID><CallerServerName/><CallerProgramName/></TechnicalInfo><AdditionalBusinessInfo><Param/><Param/><Param/><Param/><Param/><Param/></AdditionalBusinessInfo></ISPWebservicesHeader>";
			
			try {
				String current=new SimpleDateFormat("yyyyMMddHHmmssSSSSSS").format(new Date());
				current=new SimpleDateFormat("yyyyMMddHHmmssSSSSSS").format(new Date());	
				headerISP_Current=headerISP_Base.replaceAll("%TIMESTAMP%", current);
				headerISP_Current=headerISP_Current.replaceAll("%SERVICE%", "LOOKUPWSRR");

                data = ra.getServiceData(headerISP_Current, "00","REST");
					
				if (data==null) { 
					System.out.println("Censimento LOOKUPWSRR (REST) versione 00  non Trovato");
				}else System.out.println("Dati del servizio : LOOKUPWSRR (REST) versione 00 - "+data.toString());
			    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

				current=new SimpleDateFormat("yyyyMMddHHmmssSSSSSS").format(new Date());
				headerISP_Current=headerISP_Base.replaceAll("%TIMESTAMP%", current);
				headerISP_Current=headerISP_Current.replaceAll("%SERVICE%", "LKPWSRRTPS");

                data = ra.getServiceData(headerISP_Current, "00","REST");
					
				if (data==null) { 
					System.out.println("Censimento LKPWSRRTPS (REST) versione 00  non Trovato");
				}else System.out.println("Dati del servizio : LKPWSRRTPS (REST) versione 00 - "+data.toString());
				
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
								
				current=new SimpleDateFormat("yyyyMMddHHmmssSSSSSS").format(new Date());
				headerISP_Current=headerISP_Base.replaceAll("%TIMESTAMP%", current);
				headerISP_Current=headerISP_Current.replaceAll("%SERVICE%", "LKPWSRRTPS");

                data = ra.getServiceData(headerISP_Current, "00","SOAP");
					
				if (data==null) { 
					System.out.println("Censimento LKPWSRRTPS (SOAP) versione 00  non Trovato");
				}else System.out.println("Dati del servizio : LKPWSRRTPS (SOAP) versione 00 - "+data.toString());
				
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				current=new SimpleDateFormat("yyyyMMddHHmmssSSSSSS").format(new Date());
				headerISP_Current=headerISP_Base.replaceAll("%TIMESTAMP%", current);
				headerISP_Current=headerISP_Current.replaceAll("%SERVICE%", "WS00S00");

                data = ra.getServiceData(headerISP_Current, "00","SOAP");
					
				if (data==null) { 
					System.out.println("Censimento WS00S00     (SOAP) versione 00  non Trovato");
				}else System.out.println("Dati del servizio : WS00S00    (SOAP) versione 00 - "+data.toString());
				
				
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				current=new SimpleDateFormat("yyyyMMddHHmmssSSSSSS").format(new Date());
				headerISP_Current=headerISP_Base.replaceAll("%TIMESTAMP%", current);
				headerISP_Current=headerISP_Current.replaceAll("%SERVICE%", "SGRESTSOAP");

                data = ra.getServiceData(headerISP_Current, "00");
					
				if (data==null) { 
					System.out.println("Censimento SGRESTSOAP versione 00  non Trovato");
				}else 
				{
					System.out.println("Dati del servizio : SGRESTSOAP versione 00 - "+data.toString());
					ArrayList<HashMap<String, String>> ListRestEndpoint= ra.getEndpointRestData(data);
					System.out.println("Endpoint rest "+ListRestEndpoint.toString());
					ArrayList<HashMap<String, String>> ListSOAPEndpoint= ra.getEndPointSoapData(data);
					System.out.println("Endpoint soap "+ListSOAPEndpoint.toString());
				
				}
				
				///////////////////////////////////////////////////
				
				current=new SimpleDateFormat("yyyyMMddHHmmssSSSSSS").format(new Date());
				headerISP_Current=headerISP_Base.replaceAll("%TIMESTAMP%", current);
				headerISP_Current=headerISP_Current.replaceAll("%SERVICE%", "NOTFOUNDPROVA");

                data = ra.getServiceData(headerISP_Current, "00","REST");
					
				if (data==null) { 
					System.out.println("Censimento NOTFOUNDPROVA     (REST) versione 00  non Trovato");
				}else System.out.println("Dati del servizio : NOTFOUNDPROVA    (REST) versione 00 - "+data.toString());
				

			} catch (LIBLKPWSRRAException | LIBLKPWSRRTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (Exception e1) {
				
				e1.printStackTrace();
			}
		 
	}

}