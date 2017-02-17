package com.isp.wsrr.lookup.commons;


 public final class Messages {
	
	
	public static String QUERY_GET_SERVICEVERSION_URI_JSON = "Metadata/JSON/PropertyQuery?query=/WSRR/GenericObject[@name='%CATALOGNAME%'%20and%20@version='%VERSION%']&p1=bsrURI";
	
	public static String QUERY_GET_CLASSIFICATION_JSON = "Metadata/JSON/%BSRURI%/classifications";
	public static String QUERY_GET_SOAP_ENDPOINT_JSON = "Metadata/JSON/PropertyQuery?query=/WSRR/GenericObject[@name='%CATALOGNAME%'%20and%20@version='%VERSION%']/gep63_provides(.)/gep63_availableEndpoints(.)[exactlyClassifiedByAllOf(.,'http://www.ibm.com/xmlns/prod/serviceregistry/v6r3/ServiceModel%23SOAPServiceEndpoint')]&p1=name&p2=sm63_Timeout&p3=sm63_ISPHEADER_FLAG";
	public static String QUERY_GET_REST_ENDPOINT_JSON = "Metadata/JSON/PropertyQuery?query=/WSRR/GenericObject[@name='%CATALOGNAME%'%20and%20@version='%VERSION%']/gep63_provides(.)/gep63_availableEndpoints(.)[exactlyClassifiedByAllOf(.,'http://www.ibm.com/xmlns/prod/serviceregistry/profile/v8r0/RESTModel%23RESTServiceEndpoint')]&p1=name&p2=sm63_Timeout&p3=rest80_ISPHEADER_FLAG";
	public static String QUERY_GET_MQ_ENDPOINT_JSON = "Metadata/JSON/PropertyQuery?query=/WSRR/GenericObject[@name='%CATALOGNAME%'%20and%20@version='%VERSION%']/gep63_provides(.)/gep63_availableEndpoints(.)[exactlyClassifiedByAllOf(.,'http://www.ibm.com/xmlns/prod/serviceregistry/profile/v8r0/RESTModel%23RESTServiceEndpoint')]&p1=name&p2=sm63_Timeout&p3=rest80_ISPHEADER_FLAG";
	public static String QUERY_GET_ALL_ENDPOINT_JSON = "Metadata/JSON/PropertyQuery?query=/WSRR/GenericObject[@name='%CATALOGNAME%'%20and%20@version='%VERSION%']/gep63_provides(.)/gep63_availableEndpoints(.)&p1=name&p2=sm63_Timeout&p3=rest80_ISPHEADER_FLAG";
	
	
	public static String QUERY_GET_ORGANIZATION_JSON = "Metadata/JSON/PropertyQuery?query=/WSRR/GenericObject[@name='%CATALOGNAME%'%20and%20@version='%VERSION%']/ale63_owningOrganization(.)&p1=name";

	public static String QUERY_OBJECT_DESCRIPTION_JSON = "Metadata/JSON/PropertyQuery?query=/WSRR/GenericObject[@name='%CATALOGNAME%'%20and%20@version='%VERSION%']&p1=description&p2=gep63_DESC_ESTESA";
	
	public static String ERROR_1 = "Errore #1 : tags <ServiceID></ServiceID> e/o <ApplicationID></ApplicationID> e/o <Timestamp></Timestamp> non trovati nel Header ISP";
	public static String ERROR_2 = "Errore #2 : tags <ServiceID></ServiceID> e/o <ApplicationID></ApplicationID> e/o <Timestamp></Timestamp> non hanno un valore associato nel Header ISP";
	public static String ERROR_3 = "Errore #3 : Classificazione non definita per il Catalogo : ";
	public static String ERROR_4 = "Errore #4 : Tipo di Oggetto non supportato per il Catalogo : ";
	public static String ERROR_5 = "Errore #5 : Organizzazione non presente  per il Catalogo : ";
	public static String ERROR_6 = "Errore #6 : Trovati piu' Endpoint di tipo REST associati al Catalogo : ";
	public static String ERROR_7 = "Errore #7 : Trovati piu' Endpoint di tipo SOAP associati al Catalogo : ";
	public static String ERROR_8 = "Errore #8 : Trovati piu' Endpoint di tipo REST e SOAP : utilizzare getEndpointRestData o getEnpointSOAPData al posto di getEndpointData";
	public static String ERROR_9 = "Errore #9 : Specificare un input diverso da null";
	public static String ERROR_10 = "Errore #10 : Tag <Timestamp> nell HeaderISP non ha un valore valido";
	public static String ERROR_11 = "Errore #11 : Il file di configurazione per il gestore cache (EHCACHE) non e' valido"; 
	public static String ERROR_12 = "Errore #12 : Il file di configurazione per il gestore cache (EHCACHE) non contiene una definizione di cache con nome : wsrrlkpcache"; 
	public static String ERROR_13 = "Errore #13 : Endpoint del servizio di tracciatura non specificato"; 

    public static String REST_INTERFACE="REST";
    public static String SOAP_INTERFACE="SOAP";
    public static String MQ_INTERFACE="MQ";
    public static String ALL_INTERFACE="ALL";
}
