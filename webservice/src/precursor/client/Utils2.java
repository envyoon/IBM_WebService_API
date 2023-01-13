/************************************************************************
* IBM Confidential
* OCO Source Materials
* *** IBM Security Identity Manager ***
*
* (C) Copyright IBM Corp. 2015  All Rights Reserved.
*
* The source code for this program is not published or otherwise  
* divested of its trade secrets, irrespective of what has been 
* deposited with the U.S. Copyright Office.
*************************************************************************/

package precursor.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.ws.model.WSAttribute;
import com.ibm.itim.ws.model.WSRequest;
import com.ibm.itim.ws.services.ArrayOfXsdString;

import java.text.ParseException;

public class Utils2 {
	private static final String MODIFY_OPERATION_TYPE_DELIMITER = ":";

	public static final String TENANT_ID = "enrole.defaulttenant.id";

	public static final String LDAP_SERVER_ROOT = "enrole.ldapserver.root";

	public static final String TRUST_STORE = "javax.net.ssl.trustStore";

	public static final String TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";

	public static final String TRUST_STORE_TYPE = "javax.net.ssl.trustStoreType";

	public static final String SSL_CONFIG_URL = "com.ibm.SSL.ConfigURL";

	private static final String LOGIN_CONTEXT = "ITIM";

	private static final String ITIM_HOME = "itim.home";

	private static final String ENROLE_PROPS = "/data/enRole.properties";
	private static final String COMMA_DELIM = ",";
	/**
	 * erglobalid=00000000000000000000 - The rest of the DN must be appended.
	 */
	public static final String DEFAULT_ORG_ID = "erglobalid=00000000000000000000";

	/**
	 * Should the Utils class print out messages about what it is doing?
	 */
	private boolean verbose;

	private String[][] required;

	private Properties props;
	
	public static enum MODIFY_OPERATION_TYPE{
		REPLACE,
		ADD,
		REMOVE
	};

	/**
	 * Create a new Utils object to help with processing.
	 * 
	 * @param requiredParams
	 *            A 2D String Array where required[x][0] is the name of a
	 *            required parameter, and required[x][1] is the error message to
	 *            present if the parameter is missing. requiredParams must not
	 *            be null.
	 * @param isVerbose
	 *            Should the Utils class print out messages about what it is
	 *            doing? <code>true</code> if Utils should be verbose,
	 *            <code>false</code> otherwise.
	 */
	public Utils2(String[][] requiredParams, boolean isVerbose) {
		if (requiredParams == null) {
			throw new IllegalArgumentException(
					"Required parameter requiredParams cannot be null.");
		}

		required = requiredParams;
		verbose = isVerbose;
	}

	/**
	 * Parses the argument list from the command-line
	 */
	public Hashtable<String, Object> parseArgs(String[] args) {
		Hashtable<String, Object> arguments = new Hashtable<String, Object>();
		String argumentList = "";
		for (int i = 0; i < args.length; i++) {
			argumentList += args[i];
		}

		StringTokenizer tokenizer = new StringTokenizer(argumentList, "-");
		while (tokenizer.hasMoreTokens()) {
			String token = (String) tokenizer.nextToken();
			int delim = token.indexOf("?");
			String name = token.substring(0, delim);
			String value = token.substring(delim + 1, token.length());
			if (arguments.get(name) != null) {
				// arg name used previous
				Object vals = arguments.get(name);
				if (vals instanceof String) {
					// convert to String[]
					Vector<String> values = new Vector<String>(2);
					values.add((String) vals);
					values.add(value);

					arguments.put(name, values);
				} else if (vals instanceof Vector) {
					// add new element to String[]
					Vector<String> values = (Vector<String>) vals;
					values.add(value);
					arguments.put(name, vals);
				}
			} else {
				arguments.put(name, value);
			}
		}

		checkArguments(arguments);
		return arguments;
	}

	/**
	 * Creates an AttributeValue from the given name=value pair.
	 * 
	 * @param nameValuePair
	 *            String in the format of string=value.
	 * @return An AttributeValue object that holds the data in nameValuePair
	 */
	public static AttributeValue createAttributeValue(String nameValuePair) {
		String name = nameValuePair.substring(0, nameValuePair.indexOf("="));
		String value = nameValuePair.substring(nameValuePair.indexOf("=") + 1,
				nameValuePair.length());
		AttributeValue attrVal = new AttributeValue(name, value);
		return attrVal;
	}

	/**
	 * Creates an AttributeValueMap from the given Vector attributes.
	 * 
	 * @param attributes
	 *            Vector Form Commandline argument.
	 * 
	 */
	public static Map<String, AttributeValue> createAttributeValueMap(
			Vector attributes) {
		Iterator it = attributes.iterator();
		Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();
		while (it.hasNext()) {
			String nameValuePair = (String) it.next();
			String name = nameValuePair
					.substring(0, nameValuePair.indexOf("="));
			String value = nameValuePair.substring(
					nameValuePair.indexOf("=") + 1, nameValuePair.length());

			if (!map.containsKey(name)) {
				AttributeValue attrVal = new AttributeValue(name, value);
				map.put(name, attrVal);
			} else {
				map.get(name).addValue(value);
			}
		}
		return map;
	}

	private boolean checkArguments(Hashtable<String, Object> arguments) {
		for (int i = 0; i < required.length; i++) {
			if (!arguments.containsKey(required[i][0])) {
				throw new IllegalArgumentException(required[i][1]);
			}
		}

		return true;
	}

	public String getProperty(String propName) {
		if (props == null) {
			props = new Properties();

			String itimHome = System.getProperty(ITIM_HOME);
			try {
				props.load(new FileInputStream(itimHome + ENROLE_PROPS));
			} catch (FileNotFoundException ex) {
				throw new RuntimeException(ex);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}

		String value = System.getProperty(propName);

		if (value == null) {
			value = props.getProperty(propName);
		}

		return value;
	}

	public void print(String msg) {
		if (verbose) {
			System.out.println(msg);
		}
	}

	public static XMLGregorianCalendar long2Gregorian(long date) {
		DatatypeFactory dataTypeFactory;
		try {
			dataTypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(date);

		return dataTypeFactory.newXMLGregorianCalendar(gc);
	}

	/**
	 * The method will convert a String specifying date in mm/dd/yyyy format to
	 * a java.util.Date object.
	 * 
	 * @param sDate
	 *            String specifying date in mm/dd/yyyy format.
	 * @return
	 * @throws java.text.ParseException
	 */
	public static Date convertStringToDate(String sDate) throws ParseException {
		DateFormat df = new SimpleDateFormat("mm/dd/yyyy");
		Date date = df.parse(sDate);

		return date;
	}

	public static void printWSRequestDetails(String operationName,
			WSRequest wsRequest) {
		System.out.println(" Submitted request for " + operationName
				+ ". Request Details : " + "\n Request Id: "
				+ wsRequest.getRequestId() + "\n Request Status : "
				+ wsRequest.getStatusString() + "\n Request Owner : "
				+ wsRequest.getOwner() + "\n Requestee : "
				+ wsRequest.getRequestee());
	}

	public static void printMsg(String className, String methodName,
			String enumName, String message) {

		System.out.println("CLASSNAME: " + className + "\nMETHODNAME: "
				+ methodName + "\nENUMNAME: " + enumName + "\nMESSAGE: "
				+ message + "\n");

	}
	// Guarantees List return , while Utils.parse return list if size > 1
	public static List<String> commaSpliter(final Object ansObject){
		
		List<String> answers = null; 
		if(ansObject instanceof String){
			answers = new ArrayList<String>(){{
				add((String) ansObject);
			}};
		}else if(ansObject instanceof List ){
			answers = (List<String>)ansObject;
		}
		
		List<String> separatedList = new ArrayList<String>();
		
		for (String answer : answers){
			String[] answersList = answer.split(COMMA_DELIM);
			for(String ans : answersList){
				separatedList.add(ans);
			}
			
		}	 
		return separatedList;
	}
	
	
	/**
	 * The method returns a list of WSAttribute data objects from the input map.
	 * 
	 * @param mpParams
	 * @return
	 */
	public static List<WSAttribute> getWSAttributesList(Map<String, Object> mpParams, String operationName){
		java.util.Iterator<String> itrParams = mpParams.keySet().iterator();
		WSAttribute wsAttr = null;
		List<WSAttribute> lstWSAttributes = new ArrayList<WSAttribute>();

		while (itrParams.hasNext()) {
			String paramName = itrParams.next();
			Object paramValue = mpParams.get(paramName);
			wsAttr = new WSAttribute();
			wsAttr.setName(paramName);
			ArrayOfXsdString arrStringValues = new ArrayOfXsdString();
			
			List lstAttrValues = parseWSModifyArgs(paramValue, wsAttr, operationName);
			arrStringValues.getItem().addAll(lstAttrValues);
			wsAttr.setValues(arrStringValues);
			lstWSAttributes.add(wsAttr);
		}
		
		return lstWSAttributes;
	}
	
	
	public static List parseWSModifyArgs(Object paramValue, WSAttribute wsAttr, String operationName){
		List lstAttrValues = null;
		if(paramValue instanceof String){
			String sParamValue = (String)paramValue;
			if(sParamValue.indexOf(MODIFY_OPERATION_TYPE_DELIMITER) >= 0){
				sParamValue = setModOperationTypeAndReturnAttrValue(wsAttr, sParamValue);
			}
			lstAttrValues = Arrays.asList(sParamValue);
		}else if(paramValue instanceof Vector){
			Vector<String> paramValues = (Vector<String>)paramValue;
			lstAttrValues = new ArrayList();
			for(String sParamVal : paramValues){
				if(sParamVal.indexOf(MODIFY_OPERATION_TYPE_DELIMITER) >= 0)
					lstAttrValues.add(setModOperationTypeAndReturnAttrValue(wsAttr, sParamVal));
			}
		}else {
			Utils2.printMsg(Utils2.class.getName(), "parseWSModifyArgs", operationName, "The parameter value datatype is not supported.");
		}
		
		return lstAttrValues;
	}

	public static String setModOperationTypeAndReturnAttrValue(WSAttribute wsAttr, String sParamValue) {
		int operationType = 0;
		int delimiterIndex = sParamValue.indexOf(MODIFY_OPERATION_TYPE_DELIMITER);
		String attrValue = null;
		if(delimiterIndex >=0){
			String[] args = sParamValue.split(MODIFY_OPERATION_TYPE_DELIMITER);
			String opType = args[1];
			Utils2.MODIFY_OPERATION_TYPE enumValue = Utils2.MODIFY_OPERATION_TYPE.valueOf(opType.trim());
			operationType = enumValue.ordinal();
			attrValue = args[0];
		}
		wsAttr.setOperation(operationType);
		return attrValue;
	}

}
