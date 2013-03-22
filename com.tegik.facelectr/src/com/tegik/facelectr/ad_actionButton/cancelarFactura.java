package com.tegik.facelectr.ad_actionButton;

import java.io.IOException;
import java.util.Date;
import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_actionButton.ActionButtonDefaultData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.dal.core.OBContext;

//import mx.bigdata.cfdi.CFDv3;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante;
import mx.bigdata.sat.cfdi.CFDv32;
import mx.bigdata.sat.security.KeyLoader;
import mx.bigdata.sat.cfdi.TFDv1;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.io.StringWriter;
import java.io.PrintWriter;

import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.common.enterprise.EmailServerConfiguration;
import org.openbravo.model.common.enterprise.EmailTemplate;
import org.openbravo.model.common.enterprise.DocumentTemplate;


import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;

import org.xml.sax.SAXException;

import java.io.*;
import java.io.FileInputStream;
import java.io.FileOutputStream; 
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

//import java.net.*;
import java.util.Calendar;

import javax.mail.*;
import javax.mail.Authenticator;
import javax.mail.internet.*;
    
import java.security.Security;  
import java.util.Properties;  
import javax.activation.*;

import org.openbravo.utils.CryptoUtility;

import java.util.regex.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpSession;
public class cancelarFactura  {


String cancelarFac (String ruta, String NumFac,String RFC_Emisor, String RFC_Receptor, String uuid,String refID, String PasswordPAC, String archivoPac) throws IOException {
 String strTimbrar = "";

try {
	      //Empieza Crear el encapsualado SOAP para la conexion a WS del PAC
	      OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(ruta+"Request_Cancela"+NumFac+".xml", true), "UTF-8");
              BufferedWriter encabezado = new BufferedWriter(writer);
	      encabezado.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	      encabezado.write("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://www.buzonfiscal.com/ns/xsd/bf/bfcorp/3\">\"\n");
	      encabezado.write("  <soapenv:Header/>\n");
	      encabezado.write("  <soapenv:Body>\n");
	      encabezado.write("      <ns:RequestCancelaCFDi RfcEmisor=\""+RFC_Emisor+"\" RfcReceptor=\""+RFC_Receptor+"\" uuid=\""+uuid+"\" refID=\""+refID+"\"/>\n");
	      encabezado.write("  </soapenv:Body>\n");
	      encabezado.write("</soapenv:Envelope>\n");
	      encabezado.close();
	      //Termina crear el encapsualado SOAP
	      

	      strTimbrar = timbra_cancela("https://cancelacion.facturaelectronica.sat.gob.mx/Cancelacion/CancelaCFDService.svc?wsdl",ruta, NumFac, PasswordPAC, archivoPac);

      }     
      catch (Exception e) {
	e.printStackTrace();
	 }
      //Este metodo puede sustituir a la llamada a la clase RunWS
      return strTimbrar;   
    }


String timbra_cancela(String endpointAddress, String ruta, String NumFac, String PasswordPAC, String archivoPac) throws SOAPException, IOException, ParserConfigurationException, SAXException, KeyManagementException, NoSuchAlgorithmException, ServletException {
try
{
	System.setProperty("javax.net.debug", "all");
	System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
	System.setProperty("javax.net.ssl.keyStore", archivoPac);  //Aqui va la ruta del archivo PAC (.pfx) en tipo String
	System.setProperty("javax.net.ssl.keyStorePassword", PasswordPAC); //Aqui va el password del archivo .pfx
	System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");

	QName serviceName = new QName("www.buzonfiscal.com/TimbradoCFDI/", "TimbradoCFDI");

	//QName for Port As defined in wsdl.
	QName portName = new QName("www.buzonfiscal.com/TimbradoCFDI/", "TimbradoCFDISOAP");

	// Create a dynamic Service instance
	Service service = Service.create(serviceName);

	// Add a port to the Service
	service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);

	//Create a dispatch instance
	Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
	dispatch.getRequestContext().put(Dispatch.SOAPACTION_USE_PROPERTY, new Boolean(true));
	dispatch.getRequestContext().put(Dispatch.SOAPACTION_URI_PROPERTY,"www.buzonfiscal.com/TimbradoCFDI/TimbradoCFDISOAP");
	//doTrustToCertificates();
	MessageFactory factory = MessageFactory.newInstance();

	SOAPMessage request = factory.createMessage();

	// Object for message parts
	SOAPPart sp = request.getSOAPPart();

	StreamSource prepMsg = new StreamSource(new FileInputStream(ruta+"Request_Cancela"+NumFac+".xml"));	//Se abre el archivo necesario para crear la conexion
	sp.setContent(prepMsg);
	// Save message
	request.saveChanges();
    
      FileOutputStream salida = new FileOutputStream(ruta+"ResponseCancela"+ NumFac +".xml", true);	//Se crea el archivo donde se guardará la informacion que se reciba del PAC
      SOAPMessage reply = null;  

      // Invoke the endpoint synchronously
      // disableCertificateValidation();
      try {
	  //Invoke Endpoint Operation and read response
	  reply = dispatch.invoke(request);    //Crea la conexion y recibe un archivo de regreso del PAC
          reply.writeTo(salida);       //GUarda la respuesta del servidor en el archivo creado anteriormente
	  salida.close(); 
	    }
      catch (WebServiceException wse){
	wse.printStackTrace();
	  }
     
      }     
      catch (Exception e) {
	e.printStackTrace();
      }
      //Este metodo puede sustituir a la llamada a la clase RunWS
      return "OK";   
    }


}