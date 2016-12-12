package com.isp.wsrr.lookup.rest;

import com.isp.wsrr.lookup.commons.Messages;
import com.isp.wsrr.lookup.exception.LIBLKPWSRRTException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

/**
 * 
 * The BPM connection credential are passed inside an object of type : ConnectionDataBean <p>
 * that contains this member variables:
 * <p>
 * BPM server name
 * <p>
 * port
 * <p>
 * user
 * <p>
 * password
 *<p>
 *this information are present in a property file passed to the jvm by the option:
 *<p><h4>-Dbpm.connection.data</h4>
 *<p>
 *example:
 *<p>
 *-Dbpm.connection.data=c:\bpmconnection.property
 *
 * @author Primeur
 * @version 1.0
 * 
 *  */

public class ConnectionDataBeanSingleton {

	private static ConnectionDataBeanSingleton instance=null; 

	private  String url;
	private  String user;
	private  String password;
	private  String serverType;


	private  boolean trace;
    private  boolean traceODM; //added for ODM
	private Cache cache;
	
	private ConnectionDataBeanSingleton() throws LIBLKPWSRRTException {

		String url=System.getProperty("LIBLKPWSRRURL");
		String user=System.getProperty("LIBLKPWSRRUSER");
		String password=System.getProperty("LIBLKPWSRRPASSWORD");
		
		String cache=System.getProperty("LIBLKPWSRRCACHECONFIG");
		String heap=System.getProperty("LIBLKPWSRRHEAPENTRIES");
		String ttl=System.getProperty("LIBLKPWSRRCACHETTLIVE");
		String tti=System.getProperty("LIBLKPWSRRCACHETTIDLE");
		String serverType=System.getProperty("LIBLKPWSRRSERVERTYPE");
		
		CacheManager lkpWsrrCacheManager;
		Cache lkpWsrrCache=null;
		
		String errorurl="";
		String erroruser="";
		String errorpassword="";
	
		boolean error=false;
		
		StringBuffer sb=new StringBuffer();

		if (url == null ) {		   
			errorurl=" NO value found for LIBLKPWSRRURL environment variable ";
			error=true;
			
		} else {

			if (url.contains("https://")) {
				if (user == null ) {		   
					erroruser=" NO value found for LIBLKPWSRRUSER environment variable ";
					error=true;
				}
				if (password== null ) {		   
					errorpassword=" NO value found for LIBLKPWSRRPASSWORD environment variable ";
					error=true;
				}
			}
		}

		if (!error) {
			
			this.setUrl(url);
			this.setUser(user);
			this.setPassword(password);		
			
			if (System.getProperty("LIBLKPWSRRTRACE") != null && ((String)System.getProperty("LIBLKPWSRRTRACE")).equalsIgnoreCase("N")) this.setTrace(false);
			else this.setTrace(true);
			
			if (System.getProperty("LIBODMTRACE") != null && ((String)System.getProperty("LIBODMTRACE")).equalsIgnoreCase("N")) this.setTraceODM(false);
			else this.setTraceODM(true);			
			
		} else {
			throw new LIBLKPWSRRTException(sb.append(errorurl).append(erroruser).append(errorpassword).toString());
		}
		
		//set cache default info
		int  heapCurrentDepth=1000;
		long ttlCurrent=600L;
		long ttiCurrent=300L;
			
		if (ttl !=null && ttl.length()!=0) {
			
			try {
				ttlCurrent=Long.parseLong(ttl);
			}catch(NumberFormatException ex) {
				//nothing todo
			}			
		}
		
		if (tti !=null && tti.length()!=0) {
			
			try {
				ttiCurrent=Long.parseLong(tti);
			}catch(NumberFormatException ex) {
				//nothing todo
			}			
		}

		if (heap !=null && heap.length()!=0) {
			
			try {
				heapCurrentDepth=Integer.parseInt(heap);
			}catch(NumberFormatException ex) {
				//
			}			
		}
		
		try {
		
		if (cache !=null && cache.length() !=0) {
			
			lkpWsrrCacheManager = CacheManager.newInstance(cache);
			lkpWsrrCache = lkpWsrrCacheManager.getCache("wsrrlkpcache");		
			
		} else {
			
			 //create local cache configuration
		     lkpWsrrCacheManager = CacheManager.create();
		     lkpWsrrCache = new Cache("wsrrlkpcache", heapCurrentDepth, false, false, ttlCurrent, ttiCurrent);
		     lkpWsrrCacheManager.addCache(lkpWsrrCache);
		     lkpWsrrCache = lkpWsrrCacheManager.getCache("wsrrlkpcache");
		     lkpWsrrCache.getCacheConfiguration().setMemoryStoreEvictionPolicy("LRU");
		}

		} catch (Exception ex) {
			
			throw new LIBLKPWSRRTException(Messages.ERROR_11 +" > "+cache);
		}

		if (lkpWsrrCache != null) {

			this.setCache(lkpWsrrCache);
			
		} else {
			
			throw new LIBLKPWSRRTException(Messages.ERROR_12);
		}
				
		if (serverType==null) serverType="BEA";
		else 
		serverType="OTHER";
		
		this.serverType=serverType;
		
	}

	/**
	 * Return the instance with BPM connection data
	 *
	 *@throws Exception ( "Error in reading data from the property -Dbpm.connection.data )
	 *  */
	public static synchronized ConnectionDataBeanSingleton setData() throws Exception {

		if (instance == null) {
			instance = new ConnectionDataBeanSingleton();
		}
		return instance;	
	}

	public  String getUrl() {
		return url;
	}
	protected  void setUrl(String url) {
		this.url = url;
	}

	public  String getUser() {
		return user;
	}
	protected void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	protected   void setPassword(String password) {
		this.password = password;
	}

	public boolean getTrace() {
		return trace;
	}

	protected void setTrace(boolean trace) {
		this.trace = trace;
	}
	public boolean getTraceODM() {
		return traceODM;
	}

	protected void setTraceODM(boolean traceODM) {
		this.traceODM = traceODM;
	}
	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}
	
    public String getServerType() {
		return serverType;
	}

	protected void setServerType(String serverType) {
		this.serverType = serverType;
	}

}