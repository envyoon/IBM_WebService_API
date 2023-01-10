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

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.itim.ws.model.WSRequest;

public class Utils
{
	public static void printMsg(String className, String methodName,
			String enumName, String message) {

		System.out.println("CLASSNAME: " + className + "\nMETHODNAME: "
				+ methodName + "\nENUMNAME: " + enumName + "\nMESSAGE: "
				+ message + "\n");

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
}
