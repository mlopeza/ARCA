package com.tegik.facelectr.ad_actionButton;

import java.io.IOException;
import java.util.Date;
import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

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

import mx.bigdata.sat.cfdi.v32.schema.Comprobante;
import mx.bigdata.sat.cfdi.CFDv32;
import mx.bigdata.sat.security.KeyLoader;
import mx.bigdata.sat.cfdi.TFDv1;


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
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.*;

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

import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import org.openbravo.erpCommon.utility.JRFormatFactory;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.PrintJRData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FileUtility;
import org.openbravo.utils.Replace;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.JRFieldProviderDataSource;

import org.openbravo.dal.service.OBDal;

import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.enterprise.DocumentTemplate;
import org.openbravo.base.session.OBPropertiesProvider;


/**
 * Creando una factura electrónica para México.
 *
 * @author Tegik
 */
public class facturaElectronica extends HttpSecureAppServlet {

private static final Logger log = Logger.getLogger(facturaElectronica.class);


    private static final long serialVersionUID = 1L;

    private static String rutaAttach = "";
    private static String NumFac = "";
    private static String ruta = "";
    private static String Servidor = "";
    private static String Puerto = "";
    private static String Correo_servidor = "";
    private static String Contra_Servidor = "";
    private static String Correo_origen = "";
    private static String Correo_destino = "";
    private static String Correo_alternativo = "";
    private static String Asunto = "";
    private static String MensajeCorreo = "";
    private static String strTimbrar = "";
    private static String Statuscorreo = "OK";
    //private static String attachFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("attach.path");
    private static String attachFolder = "";
    private static String Separador = "/";
    private static Boolean banderaSeguir = true;
    private static String timbradoTestStatus = "NE";

    public void init(ServletConfig config) {
        super.init(config);
        boolHist = false;
    }

    

    // main HTTP call handler
   synchronized public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
	VariablesSecureApp vars = new VariablesSecureApp(request);

	log.info("dopost -- 1");

        if (vars.commandIn("DEFAULT")) {
        
	    rutaAttach = "";
	    NumFac = "";
	    ruta = "";
	    Servidor = "";
	    Puerto = "";
	    Correo_servidor = "";
	    Contra_Servidor = "";
	    Correo_origen = "";
	    Correo_destino = "";
	    Correo_alternativo = "";
	    Asunto = "";
	    MensajeCorreo = "";
	    strTimbrar = "";
	    Statuscorreo = "OK";
	    Separador = "/";
	    banderaSeguir = true;
	    timbradoTestStatus = "NE";
	    attachFolder = "/opt/OpenbravoERP-3.0/attachments";
	    attachFolder = attachFolder + Separador.substring(0,1);
	    log.info("CSM> rutaAttach -- " + rutaAttach);
	    log.info("CSM> NumFac -- " + NumFac);
	    log.info("CSM> ruta -- " + ruta);
	    log.info("CSM> Servidor -- " + Servidor);
	    log.info("CSM> Puerto -- " + Puerto);
	    log.info("CSM> Correo_servidor -- " + Correo_servidor);
	    log.info("CSM> Contra_Servidor -- " + Contra_Servidor);
	    log.info("CSM> Correo_origen -- " + Correo_origen);
	    log.info("CSM> Correo_destino -- " + Correo_destino);
	    log.info("CSM> Correo_alternativo -- " + Correo_alternativo);
	    log.info("CSM> Asunto -- " + Asunto);
	    log.info("CSM> MensajeCorreo -- " + MensajeCorreo);
	    log.info("CSM> strTimbrar -- " + strTimbrar);
	    log.info("CSM> Statuscorreo -- " + Statuscorreo);
	    log.info("CSM> attachFolder -- " + attachFolder);
	    log.info("CSM> banderaSeguir -- " + banderaSeguir);
	    log.info("CSM> timbradoTestStatus -- " + timbradoTestStatus);
	    log.info("CSM> attachFolderInicial -- " + attachFolder);
	    

	    log.info("CSM> Separador -- " + Separador);

	    log.info("CSM> attachFolder -- " + attachFolder);
  
            // parse required Guest ID parameter to be processed
	    String strInvoiceId = vars.getStringParameter("inpcInvoiceId");
	    log.info("CSM //  strInvoiceId // " + strInvoiceId);
	    Invoice facturaTest = OBDal.getInstance().get(Invoice.class,strInvoiceId);

	    log.info("dopost -- 2");
           
            // construct the reload path so that upon completion of the process
            // the main editing window is reloaded and the resulting message is
            // shown
            String strWindow = vars.getStringParameter("inpwindowId");
            String strTab = vars.getStringParameter("inpTabId");
            String strWindowPath = Utility.getTabURL(this, strTab, "R");
            if (strWindowPath.equals(""))
              strWindowPath = strDefaultServlet;
              
              //Busca el archivo de timbrado

	    log.info("dopost -- 3");
	    OBError myMessage2 = new OBError();
	    //  myMessage2.setMessage("Aquí se debe de terminar el proceso");
	    //  myMessage2.setType("Error");
	    //  myMessage2.setTitle("Facturación");
	    String rutaTimbradoTest = attachFolder + "318-" + strInvoiceId + Separador.substring(0,1) + "Timbrado" + facturaTest.getDocumentNo() + ".xml";
	    log.info("CSM> rutaTimbradoTest -- " + rutaTimbradoTest);
	    String rutaRequestTest = attachFolder + "318-" + strInvoiceId + Separador.substring(0,1) + "requestTimbrado" + facturaTest.getDocumentNo() + ".xml";
	    log.info("CSM> rutaRequestTest -- " + rutaRequestTest);
	    String rutaXMLTest = attachFolder + "318-" + strInvoiceId + Separador.substring(0,1) + facturaTest.getDocumentNo() + ".xml";
	    log.info("CSM> rutaXMLTest -- " + rutaXMLTest);
	    
	    File timbradoTest = new File(rutaTimbradoTest);
	    File requestTest = new File(rutaRequestTest);
	    File XMLTest = new File(rutaXMLTest);

	    if (!timbradoTest.exists()){
		banderaSeguir = true;
		timbradoTestStatus = "NE";
	    }else
	    {	
		  FileInputStream fis = new FileInputStream(timbradoTest);  
		  int b = fis.read();  
		  if (b == -1)  
		  {  
		      banderaSeguir = false;
		      timbradoTestStatus = "AV";
		  }else
		  {
		      banderaSeguir = false;
		      timbradoTestStatus = "AT";
		  }
		  fis.close();
	    }

	    if (timbradoTestStatus == "AV"){
		 String banderaTestStatus = "N";

		 if (timbradoTest.exists()) {
		      if (timbradoTest.delete()){
			  banderaTestStatus = "Y";
		      }else{
			  banderaTestStatus = "N";
		      }
		 }else{
		    banderaTestStatus = "Y";
		 }
		
		 if (banderaTestStatus == "Y") {
			  if (requestTest.exists()) {
				if (requestTest.delete()){
				    banderaTestStatus = "Y";
				}else{
				    banderaTestStatus = "N";
				}
			  }else{
			      banderaTestStatus = "Y";
			  }
		 }

		 if (banderaTestStatus == "Y") {
			  if (XMLTest.exists()) {
				if (XMLTest.delete()){
				    banderaTestStatus = "Y";
				}else{
				    banderaTestStatus = "N";
				}
			  }else{
			      banderaTestStatus = "Y";
			  }
		 }


		 if (banderaTestStatus == "Y") {
		     banderaSeguir = true;
		     //myMessage2.setMessage("Se eliminaron correctamente los archivos. Se procederá al timbrado");
		     //myMessage2.setType("Success");
		     //myMessage2.setTitle("Factura electrónica");
		     //banderaSeguir = false;
		 }else{
		      myMessage2.setMessage("El archivo de timbrado se encuentra vacío. Contacte a soporte");
		      myMessage2.setType("Error");
		      myMessage2.setTitle("Error al leer archivo de timbrado");
		      banderaSeguir = false;
		 }
	    }

	    if (timbradoTestStatus == "AT"){
		  facturaTest.setFetSellosat(getValuefromXML(timbradoTest, "selloSAT"));
		  log.info("CSM > SelloSat -- " + getValuefromXML(timbradoTest, "selloSAT"));
		  //Guarda el selloCFD en un Campo de Openbravo
		  facturaTest.setFetSellocfd(getValuefromXML(timbradoTest, "selloCFD"));
		  log.info("CSM > selloCFD -- " + getValuefromXML(timbradoTest, "selloCFD"));
		  //Guarda la cadena Original del SAT en un Campo de Openbravo
		  facturaTest.setFetCadenaoriginalSat(getValuefromXML(timbradoTest, "version") + getValuefromXML(timbradoTest, "UUID") + getValuefromXML(timbradoTest, "FechaTimbrado") + getValuefromXML(timbradoTest, "selloSAT") + getValuefromXML(timbradoTest, "noCertificadoSAT"));
		  log.info("CSM > CadenaOriginal -- " + getValuefromXML(timbradoTest, "version") + getValuefromXML(timbradoTest, "UUID") + getValuefromXML(timbradoTest, "FechaTimbrado") + getValuefromXML(timbradoTest, "selloSAT") + getValuefromXML(timbradoTest, "noCertificadoSAT"));
		  //Guarda la fecha de timbrado en base de datos
		  facturaTest.setFetFechaTimbre(getValuefromXML(timbradoTest, "FechaTimbrado"));
		  log.info("CSM > FechaTimbrado -- " + getValuefromXML(timbradoTest, "FechaTimbrado"));
		  //Guarda el Numero de Certificado en base de datos
		  facturaTest.setFetCertificadoSat(getValuefromXML(timbradoTest, "noCertificadoSAT"));
		  log.info("CSM > noCertificadoSAT -- " + getValuefromXML(timbradoTest, "noCertificadoSAT"));
		  //Guarda el uuid o folio fiscal en base de datos
		  facturaTest.setFetFoliofiscal(getValuefromXML(timbradoTest, "UUID"));
		  log.info("CSM > UUID -- " + getValuefromXML(timbradoTest, "UUID"));

		  String rutaComprobanteTest = attachFolder + "318-" + strInvoiceId + Separador.substring(0,1) + facturaTest.getDocumentNo() + ".xml";
		  log.info("CSM> rutaComprobanteTest -- " + rutaComprobanteTest);
		  FileInputStream fisComprobante = new FileInputStream(new File(rutaComprobanteTest)); 
		  try{
		      Comprobante comprobanteTest = CFDv32.newComprobante(fisComprobante);
		      facturaTest.setFetNumcertificado(comprobanteTest.getNoCertificado());
		      log.info("CSM > NumCertificado -- " + comprobanteTest.getNoCertificado());
		      OBDal.getInstance().save(facturaTest); // Guarda el attachment
		      OBDal.getInstance().flush();
		      OBDal.getInstance().commitAndClose();
		  }catch (Exception e)
		  {
			myMessage2.setMessage("No se pudo obtener la información del comprobante. Contacte al equipo de soporte");
			myMessage2.setType("Error");
			myMessage2.setTitle("Error al leear archivo de comprobante");
		  }
		  
		  //Guarda el NoCertificado de la cadena original en base de datos
		  fisComprobante.close();
		  

		  myMessage2.setMessage("");
		  myMessage2.setType("Success");
		  myMessage2.setTitle("Se creó con éxito la factura electrónica");
	    }

	    if (timbradoTestStatus == "NE"){
		 //myMessage2.setMessage("Se creará la nueva factura electrónica");
		 //myMessage2.setType("Success");
		 //myMessage2.setTitle("Cambiar bool");
		 facturaTest = null;
		 banderaSeguir = true;
	    }

	    if (!banderaSeguir){
		vars.setMessage(strTab, myMessage2);
		printPageClosePopUp(response, vars, strWindowPath);
		System.runFinalization();
		System.gc();
	    }
            // run the calculation
            if (banderaSeguir){
		  OBError myMessage = creaFacturaElectronica(vars, strInvoiceId, strTab);
		  log.info(myMessage.getType());
		  log.info(myMessage.getMessage());
		
		  log.info("dopost -- 4");

		  //Guardar el el PDF en los attachments
		  if(strTimbrar.equals("OK") && myMessage.getType() == "Success"){
		  log.info("dopost -- 5");
		  HashMap<String, Object> parameters = new HashMap<String, Object>();
		  parameters.put("DOCUMENT_ID", strInvoiceId);
		  //String strReportName = "@basedesign@/com/tegik/facelectr/ad_actionButton/reports/EM_FET_Plantilla_Factura_VentaJR.jrxml";
		  log.info("dopost -- 6");
		  //String rutaPDF = renderJRPiso(vars, strReportName, "pdf", parameters, null, null);
		  log.info("dopost -- 7");
		  //Termina Guardar el el PDF en los attachments
		  //Enviar correo Electronico con los attachments
		  log.info("CSM STRTIMBRAR>" + strTimbrar);
		  if(strTimbrar == "OK") {
		  log.info("dopost -- 8");
                  
                  
                  try {
                   OBContext.setAdminMode(true); 
                  // OBError myMessagew = new OBError();       
                //   Invoice factura = OBDal.getInstance().get(Invoice.class,strInvoiceId); IGUALES
                   Invoice facturaParaCorreo = OBDal.getInstance().get(Invoice.class,strInvoiceId);  
                   enviadorCorreos enviador = new enviadorCorreos(); // clase existente en el package
                   enviador.init(getServletConfig());
                   String respuestaEnvio = enviador.solicitarEnvio(vars,facturaParaCorreo, "Y", "Y");
                    log.info("CSM>CORREOS -- " + respuestaEnvio);
                             if (respuestaEnvio == "OK"){
                               // se creo la factura y se envio correctamente al correo electronico.
                                    myMessage.setType("Success");
                                    myMessage.setTitle("Se ha creado existosamente la Factura Electrónica");
                                    OBContext.restorePreviousMode();
                                    
                               }else
                               {
                                  // se creo la factura pero no se envio al correo electronico.
                                    myMessage.setType("Success");
                                    myMessage.setMessage("Se ha creado existosamente la Factura Electrónica: No se pudo enviar el correo el electrónico");
				    myMessage.setTitle("Se ha creado existosamente la Factura Electrónica");
                                    OBContext.restorePreviousMode();
                                }
                                }catch(Exception e) {
                                    // se creo la factura, pero hubo un error en ella.
                                             log.info("CSM>CORREOS -- " + e.toString());
                                             StringWriter w = new StringWriter();
                                             e.printStackTrace(new PrintWriter(w));
                                             String errorfactura = w.toString();
                                             log.info("CSM>CORREOS -- " + errorfactura);
                                             myMessage.setType("Success");
                                             OBContext.restorePreviousMode();
					     myMessage.setTitle("Se ha creado existosamente la Factura Electrónica");
                                             myMessage.setMessage("Se ha creado existosamente la Factura Electrónica: No se pudo enviar el correo el electrónico");
                                }
		     
		  } else  {
		   // myMessage.setMessage("Se ha creado existosamente la Factura Electrónica: No se pudo enviar el correo el electrónico");
		   // myMessage.setType("Success");
                    myMessage.setMessage("Error en el timbrado. No se ha creado la Factura Electrónica: No se pudo enviar el correo el electrónico");
		    myMessage.setType("Error");
		   
		    myMessage.setTitle("Titulo");
		    }
		  //Termina Enviar correo Electronico con los attachments
		log.info("dopost -- 8");
		log.info("Se creo con éxito la factura: "+NumFac);
		}

      //////////////////// MENSAJE PARA FACTURAR JCI PRIMER DIA --- BORRAR/////////////////////////////
      /*myMessage.setMessage("Se ha creado la Factura Electrónica");
      myMessage.setType("Success");
      myMessage.setTitle("Titulo");*/
      /////////////////////////////////////////////////////////////////
	
	// set resulting message and reload main editing window
		  vars.setMessage(strTab, myMessage);
		  printPageClosePopUp(response, vars, strWindowPath);
		  System.runFinalization();
		  System.gc();
          }

        } else {
            pageErrorPopUp(response);
	    System.runFinalization();
	    System.gc();
	}
    }

   synchronized OBError creaFacturaElectronica(VariablesSecureApp vars, String strInvoiceId, String strTab) throws IOException, ServletException {
	try
	{

	      log.info("cfe -- 1");
	      OBError myMessage = new OBError();
	      //Cargas la factura del DAL de Openbravo
	      OBContext.setAdminMode(true);
	      Invoice factura = OBDal.getInstance().get(Invoice.class,strInvoiceId);
	      Tab tabFactura = OBDal.getInstance().get(Tab.class,strTab);
	      myMessage.setMessage("Mi mensaje de prueba");
	      myMessage.setType("Error");
	      myMessage.setTitle("Error en la creación de la factura electrónica");
	      log.info("cfe -- 2");
	      String tipoDoc = "";  
	      log.info("CSM // tipoDoc // " + tipoDoc);
	      String nombreCer="", nombreKey="", nombrePfx=""; //Inicializacion archivo .cer .key .pfx
	      log.info("CSM // nombreCer // " + nombreCer);
	      log.info("CSM // nombreKey // " + nombreKey);
	      log.info("CSM // nombrePfx // " + nombrePfx);
	      String archivoPac = ""; //Inicializacion ruta de archivo .pfx
	      log.info("CSM // archivoPac // " + archivoPac);
	      log.info("cfe -- 3");
	      String PasswordFiel = factura.getOrganization().getFetPassfiel();  //password de fiel
	      log.info("CSM // PasswordFiel // " + PasswordFiel);
	      String PasswordPAC = factura.getOrganization().getFetPasspac(); //password de pac
	      log.info("CSM // PasswordPAC // " + PasswordPAC);
	      File archivo = new File(attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId + Separador.substring(0,1) + factura.getDocumentNo() + ".xml"); //Liga del archivo en tipo File
	      log.info("CSM> archivo -- " + attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId + Separador.substring(0,1) + factura.getDocumentNo() + ".xml");
	      log.info("cfe -- 4");
	      File path = new File(attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId + Separador.substring(0,1));  //Ruta de la factura en tipo File
	      log.info("CSM> path -- " + attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId + Separador.substring(0,1));
	      //Checa si el directorio existe antes de guardar el comprobante.
	      boolean exists = path.exists();
	      if (!exists) {
		// Si no existe, creamos el directorio.
		path.mkdirs();
	      }

	      String archivobase = attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId + Separador.substring(0,1) + factura.getDocumentNo() + ".xml";  //Liga del archivo en tipo String
	      log.info("CSM> archivobase -- " + archivobase);
	      ruta = attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId + Separador.substring(0,1); //Ruta de la factura en tipo String
	      log.info("CSM> ruta -- " + ruta);
	      log.info("cfe -- 5");
	      NumFac = factura.getDocumentNo();
	      log.info("CSM // NumFac // " + NumFac);
	      String Documento = factura.getTransactionDocument().getDocumentCategory(); //Tipo de documento de openbravo
	      log.info("CSM // Documento // " + Documento);
	      rutaAttach = tabFactura.getTable().getId() + "-" + strInvoiceId;
	      log.info("cfe -- 6");
	      File archivoTimbrado = new File(attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId + Separador.substring(0,1)+ "Timbrado" + factura.getDocumentNo() + ".xml"); //Liga del archivo en tipo File	      
	      log.info("CSM> archivoTimbrado -- " + attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId + Separador.substring(0,1)+ "Timbrado" + factura.getDocumentNo() + ".xml");
      	      File archivoResquet = new File(attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId + Separador.substring(0,1)+ "requestTimbrado" + factura.getDocumentNo() + ".xml"); //Liga del archivo en tipo File	      
	      log.info("CSM> archivoResquet -- " + attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId + Separador.substring(0,1)+ "requestTimbrado" + factura.getDocumentNo() + ".xml");
	      log.info("cfe -- 7");

	      log.info("cfe -- 8");
	      Statuscorreo = "OK";
	      if(Statuscorreo == "OK")  {
	      //Verifica que la año de facturacion sea igual que el año actual
	      Date Fecha_factura = factura.getInvoiceDate();
	      int Ano_factura = Fecha_factura.getYear()+1900;
	      Date Fecha_Creacion= factura.getCreationDate();
	      int Ano_creacion = Fecha_Creacion.getYear()+1900;
	      log.info("cfe -- 9");

	      if(Ano_factura==Ano_creacion) {

	      //Obtiene el RFC del emisor y del receptor
	      log.info("cfe -- 10");
	      String RFC_Emisor = factura.getOrganization().getOrganizationInformationList().get(0).getTaxID();
	      log.info("CSM // RFC_Emisor // " + RFC_Emisor);
	      String RFC_Receptor = factura.getBusinessPartner().getTaxID();
	      log.info("CSM // RFC_Receptor // " + RFC_Receptor);
	      //Crea el comprobante y el cfdi a partir de la factura que estamos utilizando.
	      log.info("cfe -- 11");
	      //ARI Factura. ARC Nota de credito
	      //Obtiene los archivos para utilizar desde los attachmentes.
	      
	      final OBCriteria<Attachment> attachmentList = OBDal.getInstance().createCriteria(Attachment.class);
	      attachmentList.add(Expression.eq(Attachment.PROPERTY_TABLE, OBDal.getInstance().get(Table.class,"155")));
	      attachmentList.add(Expression.eq(Attachment.PROPERTY_RECORD, factura.getOrganization().getId()));

	      log.info("CSM > Id de la organización de la factura -- " + factura.getOrganization().getId());

	      log.info("CSM > Lista de attachments tamaño-- " + attachmentList.list().size());

	      log.info("cfe -- 12");
	      for (Attachment attachmentUd : attachmentList.list()) {
		  log.info("CSM > Entro a la lista -- ");
		  if (attachmentUd.getName().indexOf(".cer") != -1) {
			nombreCer = attachmentUd.getName();
		  }

		  if (attachmentUd.getName().indexOf(".key") != -1) {
			nombreKey = attachmentUd.getName();
		  }

		  if (attachmentUd.getName().indexOf(".pfx") != -1) {
			nombrePfx = attachmentUd.getName();
		  }
  
	      }

	      log.info("CSM > nombreCer -- " + nombreCer);
	      log.info("CSM > nombreKey -- " + nombreKey);
	      log.info("CSM > nombrePfx -- " + nombrePfx);

	      log.info("cfe -- 13");
	      //Se cierra Obtiene los archivos para utilizar desde los attachmentes.

	      //Busca el archivo .cer y .key
	      File archivoCer = new File(attachFolder + "155" + "-" + factura.getOrganization().getId() + Separador.substring(0,1) + nombreCer);
	      log.info("CSM > archivoCer -- " + attachFolder + "155" + "-" + factura.getOrganization().getId() + Separador.substring(0,1) + nombreCer);

	      log.info("cfe -- 14");

	      if (!archivoCer.exists() || nombreCer == ""){
		  myMessage.setMessage("No se encontró el archivo .cer");
		  myMessage.setType("Error");
		  myMessage.setTitle("Error en la creación de la factura electrónica");
	      }

	      log.info("cfe -- 15");

	      File archivoKey = new File(attachFolder + "155" + "-" + factura.getOrganization().getId() + Separador.substring(0,1) + nombreKey);
	      log.info("CSM > archivoKey -- " + attachFolder + "155" + "-" + factura.getOrganization().getId() + Separador.substring(0,1) + nombreKey);

	      if (!archivoKey.exists() || nombreKey == ""){
		  myMessage.setMessage("No se encontró el archivo .key");
		  myMessage.setType("Error");
		  myMessage.setTitle("Error en la creación de la factura electrónica");
	      }

	      log.info("cfe -- 16");
	      //Se cierra Busca el archivo .cer y .key

	      //Busca el archivo .pfx para timbrar
		if(nombrePfx!="")
		  {
		    archivoPac = attachFolder + "155" + "-" + factura.getOrganization().getId() + Separador.substring(0,1) + nombrePfx;
		    log.info("CSM > archivoPac -- " + attachFolder + "155" + "-" + factura.getOrganization().getId() + Separador.substring(0,1) + nombrePfx);
		  }
		else { 
		  myMessage.setMessage("No se encontró el archivo .pfx para Timbrar");
		  myMessage.setType("Error");
		  myMessage.setTitle("Error en la creación de la factura electrónica");
		      }
	      //Se cierra Busca el archivo .pfx para timbrar

	      log.info("cfe -- 17");

	      //Verifica si es una factura o una nota de credito
	      if(Documento.equals("ARC"))
	      tipoDoc="N";
	      else tipoDoc = "F";
	      log.info("cfe -- 18");

	      log.info("CSM // tipoDoc // " + tipoDoc);

	      //termina verifica si es una factura o una nota de credito

	      Comprobante comp = creadorFacturas.createComprobante(strInvoiceId,tipoDoc);	//Crea la Cadena Original de la Factura
	      CFDv32 cfd = new CFDv32(comp);			

	      log.info("cfe -- 19");

	      PrivateKey key = KeyLoader.loadPKCS8PrivateKey(new FileInputStream(archivoKey),PasswordFiel);	//Carga la llave privada
	      X509Certificate cert = KeyLoader.loadX509Certificate(new FileInputStream(archivoCer));		//Carga el certificado
	      Comprobante sellado = cfd.sellarComprobante(key, cert);			//Sella la cadena original

	      log.info("CSM // cfdV3.2 // " + cfd.getComprobante().getComprobante().toString());
	      log.info("CSM // sellado // " + sellado.toString());
  
	   
	      cfd.validar();		//Valida la factura
	      log.info("CSM // cfdV3.2 // Después de validar" + cfd.getComprobante().getComprobante().toString());
	      cfd.verificar();		//verifica la factura
	      log.info("CSM // cfdV3.2 // Después de verificar" + cfd.getComprobante().getComprobante().toString());
	      cfd.guardar(System.out);	//guarda la factura
	      log.info("CSM // cfdV3.2 // Después de guardar" + cfd.getComprobante().getComprobante().toString());

	      log.info("cfe -- 20");

	      log.info("cfe -- 21");
	      //Guardar el comprobante y adjuntar el archivo al registro de Openbravo
	      factura.setFetCadenaoriginal(cfd.getCadenaOriginal()); // Pone la cadena original en el campo descripción de la factura.
	      OBDal.getInstance().save(factura); // Guarda los nuevos datos de la factura
	      cfd.guardar(new FileOutputStream(archivo)); // Guarda el archivo
	      log.info("cfe -- 22");
	      //Convertir el xml a un string para enviarlo en el SOAP
	      String codigo = convertXMLFileToString(ruta+NumFac+".xml");
	      log.info("CSM > codigo > " + codigo);
	      String FacturaString = Base64Coder.encodeString(codigo);
	      log.info("CSM > FacturaString > " + FacturaString);
	      log.info("cfe -- 23");
	      NumFac = cambiarCaracteres(NumFac);
	      RFC_Emisor = cambiarCaracteres(RFC_Emisor);
	      RFC_Receptor = cambiarCaracteres(RFC_Receptor);
	      log.info("CSM > NumFac > " + NumFac);
	      log.info("CSM > RFC_Emisor > " + RFC_Emisor);
	      log.info("CSM > RFC_Receptor > " + RFC_Receptor);
	      //Empieza Crear el encapsualado SOAP para la conexion a WS del PAC
	      OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(ruta+"requestTimbrado"+NumFac+".xml", true), "UTF-8");
              BufferedWriter encabezado = new BufferedWriter(writer);
	      encabezado.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	      encabezado.write("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tim=\"http://www.buzonfiscal.com/ns/xsd/bf/TimbradoCFD\" xmlns:req=\"http://www.buzonfiscal.com/ns/xsd/bf/RequestTimbraCFDI\"> \n");
	      encabezado.write("   <soapenv:Header/>\n");
	      encabezado.write("   <soapenv:Body>\n");
	      encabezado.write("    <tim:RequestTimbradoCFD req:RefID=\""+NumFac+"\"> \n");
	      encabezado.write("       <req:Documento Archivo=\""+FacturaString+"\" NombreArchivo=\""+NumFac+".xml\" Tipo=\"XML\" Version=\"32\"/>  \n");
	      encabezado.write("      <req:InfoBasica RfcEmisor=\""+RFC_Emisor+"\" RfcReceptor=\""+RFC_Receptor+"\"/> \n");	      
	      encabezado.write("    </tim:RequestTimbradoCFD>\n");
	      encabezado.write("   </soapenv:Body>\n");
	      encabezado.write("</soapenv:Envelope>\n");
	      encabezado.close();
	      //Termina crear el encapsualado SOAP
	      log.info("CSM // RutaRequestTimbrado // " + ruta+"requestTimbrado"+NumFac+".xml");
	      log.info("CSM // ArchivoRequestTimbrado // " + fileToString(ruta+"requestTimbrado"+NumFac+".xml"));
	      log.info("cfe -- 24");
	      //Se sube el archiv que se creo
	      String strSubirArchivo = subirArchivo(vars, strTab, factura, tabFactura);
	      log.info("CSM // strSubirArchivo // " + strSubirArchivo);
	      log.info("cfe -- 25");
	      //Llama a la funcion timbrar para hacer la conexion al PAC
	   //   strTimbrar = timbrar("https://tf.buzonfiscal.com/timbrado",ruta, NumFac, PasswordPAC, archivoPac);
	      strTimbrar = timbrar("https://tf.buzonfiscal.com/timbrado",ruta, NumFac, PasswordPAC, archivoPac);
	      String mensaje = strSubirArchivo + " -- " + strTimbrar;
	      log.info("CSM // strSubirArchivo // " + strSubirArchivo);
	      log.info("cfe -- 26");
	      if(strTimbrar=="OK") {

	      //Se manda a llamar a la funcion extraeTimbrado para que extraiga del archivo Timbrado los attributos
	      extraeTimbrado.timbra(ruta,NumFac);
	      //Se manda a llamar a la funcion extraeCER para que extraiga del archivo Original los attributos
	      extraeCER.extrae(ruta,NumFac);
	      log.info("cfe -- 27");
	      String rutaArchivoTimbrado = attachFolder + "318-" + strInvoiceId + Separador.substring(0,1) + "Timbrado" + factura.getDocumentNo() + ".xml";
	      File archivoTimbradoNuevo = new File(rutaArchivoTimbrado);
	      //Guarda el selloSAT en un Campo de Openbravo
	      factura.setFetSellosat(getValuefromXML(archivoTimbradoNuevo, "selloSAT"));
	      //Guarda el selloCFD en un Campo de Openbravo
	      factura.setFetSellocfd(getValuefromXML(archivoTimbradoNuevo, "selloCFD"));
	      //Guarda la cadena Original del SAT en un Campo de Openbravo
 	      factura.setFetCadenaoriginalSat(getValuefromXML(archivoTimbradoNuevo, "version") + getValuefromXML(archivoTimbradoNuevo, "UUID") + getValuefromXML(archivoTimbradoNuevo, "FechaTimbrado") + getValuefromXML(archivoTimbradoNuevo, "selloSAT") + getValuefromXML(archivoTimbradoNuevo, "noCertificadoSAT"));
	      //Guarda la fecha de timbrado en base de datos
	      factura.setFetFechaTimbre(getValuefromXML(archivoTimbradoNuevo, "FechaTimbrado"));
	      //Guarda el Numero de Certificado en base de datos
	      factura.setFetCertificadoSat(getValuefromXML(archivoTimbradoNuevo, "noCertificadoSAT"));
	      //Guarda el uuid o folio fiscal en base de datos
	      factura.setFetFoliofiscal(getValuefromXML(archivoTimbradoNuevo, "UUID"));
	      //Guarda el NoCertificado de la cadena original en base de datos
	       String rutaComprobanteNueva = attachFolder + "318-" + strInvoiceId + Separador.substring(0,1) + factura.getDocumentNo() + ".xml";
	       log.info("CSM> rutaComprobanteNueva -- " + rutaComprobanteNueva);
	       FileInputStream fisComprobanteNuevo = new FileInputStream(new File(rutaComprobanteNueva)); 

	       Comprobante nuevoComprobanteTest = CFDv32.newComprobante(fisComprobanteNuevo);
	       factura.setFetNumcertificado(nuevoComprobanteTest.getNoCertificado());
	       log.info("CSM > NumCertificado -- " + nuevoComprobanteTest.getNoCertificado());

	      log.info("cfe -- 28");
	      //Guarda la cantidad total en letra en base de datos
	      double cantidadnumero = Double.parseDouble((factura.getGrandTotalAmount()).toString());
	      String cantidadletra = convertir.Numeroaletra(cantidadnumero);
	      factura.setFetCantidadenletras(cantidadletra);
	      log.info("cfe -- 29");
	     if(factura.getFetFacturaadendaList()!=null){
	      String statusaddenda = addenda.agregaAddenda(strInvoiceId,ruta,NumFac);
	      } 
	      
	     
	      log.info("cfe -- 35");
	      // Pone la factura en status de facturada.
		  factura.setFetDocstatus("Facturado");
	      } else {
		  //factura.setFetDocstatus("Facturado");
	      }

	      OBContext.restorePreviousMode();

	      log.info("CSM -- strSubirArchivo -- " + strSubirArchivo);
	      log.info("CSM -- strTimbrar -- " + strTimbrar);

		//Mensajes de suceso o error.
		if (strSubirArchivo.equals("OK") && strTimbrar.equals("OK")){
	      //myMessage.setMessage("TEGIK-PRUEBA: La factura " + factura.getDocumentNo() + " fue creada exitosamente");
	      myMessage.setMessage("Se ha creado existosamente la Factura Electrónica");
	      myMessage.setType("Success");
	      myMessage.setTitle("Titulo");
	      OBDal.getInstance().commitAndClose();
	      return myMessage;
		}
		else {
		      if(strSubirArchivo.equals("OK"))
		      myMessage.setMessage("Error timbrado: "+strTimbrar);
		      else if (strTimbrar.equals("OK"))
			   myMessage.setMessage("Error Subir archivo: "+strSubirArchivo);
			    else  myMessage.setMessage(mensaje);
	      myMessage.setType("Error");
	      myMessage.setTitle("Error en la creación de la factura electrónica: ");
	      OBDal.getInstance().commitAndClose();
	      return myMessage;
		}
	    }

	else  {
	      myMessage.setMessage("Error en la creación de la factura electrónica: La fecha de la factura no es válida");
	      myMessage.setType("Error");
	      myMessage.setTitle("Titulo");
	      OBDal.getInstance().commitAndClose();
	      return myMessage;
	}

	}
	else  {
	      myMessage.setMessage("Error en la creación de la factura electrónica: El correo electronico no es válido");
	      myMessage.setType("Error");
	      myMessage.setTitle("Titulo");
	      OBDal.getInstance().commitAndClose();
	      return myMessage;
	      }
  
	}
	catch(Exception e){
	      StringWriter w = new StringWriter();
	      e.printStackTrace(new PrintWriter(w));
	      String errorfactura = w.toString();
	      OBError myMessage = new OBError();
	      if(errorfactura.indexOf("rfc")!=-1 && errorfactura.indexOf("Receptor")!=-1)
	      myMessage.setMessage("El RFC del Receptor no es válido");
	      else if(errorfactura.indexOf("t_RFC") != -1)
	      myMessage.setMessage("El RFC no es válido");
	      else if(errorfactura.indexOf("BadPaddingException")!=-1 && errorfactura.indexOf("PKCS8Key")!=-1)
	      myMessage.setMessage("La contraseña FIEL no es válida");
	      else if(errorfactura.indexOf("Traslados")!=-1 && errorfactura.indexOf("not complete")!=-1)
	      myMessage.setMessage("El impuesto no es válido");
	      else myMessage.setMessage(w.toString());
	      myMessage.setType("Error");
	      myMessage.setTitle("Error en la creación de la factura electrónica");
	      return myMessage;
	}
      
    }


   synchronized String subirArchivo(VariablesSecureApp vars, String strTab, Invoice factura, Tab tabFactura) throws IOException, ServletException {
	try
	{
	      OBContext.setAdminMode(true); // Para poder crear el Attachment

	      final Attachment archivoDAL = OBProvider.getInstance().get(Attachment.class);
	      //Se agregan las propiedades del attachment
	      archivoDAL.setClient(factura.getClient());
	      archivoDAL.setOrganization(factura.getOrganization());	
	      archivoDAL.setActive(true);
	      archivoDAL.setCreationDate(new Date());
	      archivoDAL.setUpdated(new Date());
	      archivoDAL.setCreatedBy(factura.getCreatedBy());
	      archivoDAL.setUpdatedBy(factura.getUpdatedBy());
	      archivoDAL.setName(factura.getDocumentNo()+".xml");
	      //archivoDAL.setDataType("103");
	      long secuencia = 10; // Falta saber como sacar la secuencia correcta, siempre se están subiendo con la secuencia 10.
	      archivoDAL.setSequenceNumber(secuencia);
	      archivoDAL.setText("Factura electrónica validada correctamente");
	      archivoDAL.setTable(tabFactura.getTable());
	      archivoDAL.setRecord(factura.getId());
	      //Se guarda el attachment
	      OBDal.getInstance().save(archivoDAL); // Guarda el attachment
	      OBDal.getInstance().flush();
	      //OBDal.getInstance().commitAndClose();

	      return "OK";
	}
	catch(Exception e){
	      StringWriter w = new StringWriter();
	      e.printStackTrace(new PrintWriter(w));
	      return w.toString();
	}
	finally {
	      OBContext.restorePreviousMode();
	}
    }

// Web Service Call -- Carlos Salinas
synchronized String timbrar(String endpointAddress, String ruta, String NumFac, String PasswordPAC, String archivoPac) throws SOAPException, IOException, ParserConfigurationException, SAXException, KeyManagementException, NoSuchAlgorithmException, ServletException {
try
{
	log.info("timbrar -- 1");
	/*System.setProperty("javax.net.debug", "all");
	System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
	System.setProperty("javax.net.ssl.keyStore", archivoPac);  //Aqui va la ruta del archivo PAC (.pfx) en tipo String
	System.setProperty("javax.net.ssl.keyStorePassword", PasswordPAC); //Aqui va el password del archivo .pfx
	System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");*/

	//QName serviceName = new QName("www.buzonfiscal.com/TimbradoCFDI/", "TimbradoCFDI");
	log.info("timbrar -- 2");

	//QName for Port As defined in wsdl.
	//QName portName = new QName("www.buzonfiscal.com/TimbradoCFDI/", "TimbradoCFDISOAP");


	// Create a dynamic Service instance
	//Service service = Service.create(serviceName);
	log.info("timbrar -- 3");
	// Add a port to the Service
	//service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);

	//Create a dispatch instance
	//Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
	//dispatch.getRequestContext().put(Dispatch.SOAPACTION_USE_PROPERTY, new Boolean(true));
	//dispatch.getRequestContext().put(Dispatch.SOAPACTION_URI_PROPERTY,"www.buzonfiscal.com/TimbradoCFDI/TimbradoCFDISOAP");
	//doTrustToCertificates();
	//MessageFactory factory = MessageFactory.newInstance();
	log.info("timbrar -- 4");

	//SOAPMessage request = factory.createMessage();

	// Object for message parts
	//SOAPPart sp = request.getSOAPPart();

	//StreamSource prepMsg = new StreamSource(new FileInputStream(ruta+"requestTimbrado"+NumFac+".xml"));	//Se abre el archivo necesario para crear la conexion
	log.info("CSM // RutaArchivoParaTimbrado // " + ruta+"requestTimbrado"+NumFac+".xml");
	log.info("CSM // ArchivoParaTimbrado // " + fileToString(ruta+"requestTimbrado"+NumFac+".xml"));
	//sp.setContent(prepMsg);
	// Save message
	//request.saveChanges();
	log.info("timbrar -- 5");
    
      //FileOutputStream salida = new FileOutputStream(ruta+"Timbrado"+ NumFac +".xml", true);	//Se crea el archivo donde se guardará la informacion que se reciba del PAC
      //SOAPMessage reply = null;  

      // Invoke the endpoint synchronously
      // disableCertificateValidation();

      Client client = new Client();
      System.setProperty("javax.net.debug", "all");
      return client.call(endpointAddress, ruta, NumFac, PasswordPAC, archivoPac);
     
      }     
      catch (Exception e) {
	e.printStackTrace();
	String mensajetimbrado = e.toString();
	log.error("CSM // ERROR EN EL TIMBRAR()// "+mensajetimbrado);
	if(mensajetimbrado.indexOf("facturaElectronica.java:448") != -1)//Linea de Password PAC en este documento la 448 --> System.setProperty("javax.net.ssl.keyStorePassword", PasswordPAC)
	  return "La contraseña del archivo PAC (.pfx) no es válida";
	else return mensajetimbrado;
      }
      //Este metodo puede sustituir a la llamada a la clase RunWS
      //return "OK";   
    }




synchronized public static String convertXMLFileToString(String fileName)
        {
          try{
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            InputStream inputStream = new FileInputStream(new File(fileName));
            org.w3c.dom.Document doc = documentBuilderFactory.newDocumentBuilder().parse(inputStream);
            StringWriter stw = new StringWriter();
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.transform(new DOMSource(doc), new StreamResult(stw));
            return stw.toString();
          }
          catch (Exception e) {
            e.printStackTrace();
          }
            return null;
        }

synchronized  public String cambiarCaracteres (String cadena) {
    cadena = cadena.replace("&", "&amp;");
    cadena = cadena.replace("<", "&lt;");
    cadena = cadena.replace(">", "&gt;");
    cadena = cadena.replace("\"", "&quot;");
    cadena = cadena.replace("'", "&#39;");
    return cadena;
  }
  
   public static String fileToString(String file) {
        String result = null;
        DataInputStream in = null;

        try {
            File f = new File(file);
            byte[] buffer = new byte[(int) f.length()];
            in = new DataInputStream(new FileInputStream(f));
            in.readFully(buffer);
            result = new String(buffer);
        } catch (IOException e) {
            throw new RuntimeException("IO problem in fileToString", e);
        } finally {
            try {
                in.close();
            } catch (IOException e) { /* ignore it */
            }
        }
        return result;
    }


synchronized  public static String getValuefromXML(File f, String str)
	{
		int ch;
		String xml1;
		StringBuffer strContent = new StringBuffer("");
	    FileInputStream fin = null;
	    try {
	      fin = new FileInputStream(f);
	      while ((ch = fin.read()) != -1)
	        strContent.append((char) ch);
	      fin.close();
	    } catch (Exception e) {
	    	return "";
	    }
	    xml1 = strContent.toString();
		try
		{
			String[] ret = xml1.split(str+"=\"");
			if (ret.length >=2)
				return ret[1].substring(0,ret[1].indexOf("\""));
			else
				return "";
		}
		catch (Exception e)
		{
			return "";
		}
	}


  private void saveReport(VariablesSecureApp vars, JasperPrint jp,Map<Object,Object> exportParameters, String fileName) throws JRException {

	  final String outputFile = globalParameters.strFTPDirectory + Separador.substring(0,1)+ rutaAttach + Separador.substring(0,1) + NumFac + ".pdf";
	  log.info("CSM > outputFile > " + outputFile);
	  final String reportType = fileName.substring(fileName.lastIndexOf(".") + 1);
	  if (reportType.equalsIgnoreCase("pdf")) {
	    JasperExportManager.exportReportToPdfFile(jp, outputFile);
	  } else if (reportType.equalsIgnoreCase("xls")) {
	    JExcelApiExporter exporter = new JExcelApiExporter();
	    exportParameters.put(JRExporterParameter.JASPER_PRINT, jp);
	    exportParameters.put(JRExporterParameter.OUTPUT_FILE_NAME, outputFile);
	    exportParameters.put(JExcelApiExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
	    exportParameters.put(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,
		Boolean.TRUE);
	    exportParameters.put(JExcelApiExporterParameter.IS_DETECT_CELL_TYPE, true);
	    exporter.setParameters(exportParameters);
	    exporter.exportReport();
	  } else {
	    throw new JRException("Report type not supported");
	  }

  }

protected String renderJRPiso(VariablesSecureApp variables, String strReportName, String strOutputType, HashMap<String, Object> designParameters, FieldProvider[] data, Map<Object, Object> exportParameters) throws IOException, ServletException {	    

	  if (strReportName == null || strReportName.equals(""))
	    strReportName = PrintJRData.getReportName(this, classInfo.id);

	  final String strAttach = globalParameters.strFTPDirectory + "/284-" + classInfo.id;

	  final String strLanguage = variables.getLanguage();
	  final Locale locLocale = new Locale(strLanguage.substring(0, 2), strLanguage.substring(3, 5));

	  final String strBaseDesign = getBaseDesignPath(strLanguage);

	  strReportName = Replace.replace(Replace.replace(strReportName, "@basedesign@", strBaseDesign), "@attach@", strAttach);
	  final String strFileName = strReportName.substring(strReportName.lastIndexOf(Separador.substring(0,1)) + 1);
	  log.info("CSM > strFileName > " + strFileName);

	  ServletOutputStream os = null;
	  UUID reportId = null;

	    try {
	    final JasperReport jasperReport = Utility.getTranslatedJasperReport(this, strReportName,strLanguage, strBaseDesign);
	    if (designParameters == null)
	      designParameters = new HashMap<String, Object>();

	    Boolean pagination = true;
	    if (strOutputType.equals("pdf"))
	      pagination = false;

	    designParameters.put("IS_IGNORE_PAGINATION", pagination);
	    designParameters.put("BASE_WEB", strReplaceWithFull);
	    designParameters.put("BASE_DESIGN", strBaseDesign);
	    designParameters.put("ATTACH", strAttach);
	    designParameters.put("USER_CLIENT", Utility.getContext(this, variables, "#User_Client", ""));
	    designParameters.put("USER_ORG", Utility.getContext(this, variables, "#User_Org", ""));
	    designParameters.put("LANGUAGE", strLanguage);
	    designParameters.put("LOCALE", locLocale);
	    designParameters.put("REPORT_TITLE",
	    PrintJRData.getReportTitle(this, variables.getLanguage(), classInfo.id));

	    final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
	    dfs.setDecimalSeparator(variables.getSessionValue("#AD_ReportDecimalSeparator").charAt(0));
	    dfs.setGroupingSeparator(variables.getSessionValue("#AD_ReportGroupingSeparator").charAt(0));
	    final DecimalFormat numberFormat = new DecimalFormat(variables.getSessionValue("#AD_ReportNumberFormat"), dfs);
	    designParameters.put("NUMBERFORMAT", numberFormat);

	    if (log4j.isDebugEnabled())
	      log4j.debug("creating the format factory: " + variables.getJavaDateFormat());
	    final JRFormatFactory jrFormatFactory = new JRFormatFactory();
	    jrFormatFactory.setDatePattern(variables.getJavaDateFormat());
	    designParameters.put(JRParameter.REPORT_FORMAT_FACTORY, jrFormatFactory);

	    JasperPrint jasperPrint;
	    Connection con = null;
	    try {
	      con = getTransactionConnection();
	      if (data != null) {
		designParameters.put("REPORT_CONNECTION", con);
		jasperPrint = JasperFillManager.fillReport(jasperReport, designParameters,new JRFieldProviderDataSource(data, variables.getJavaDateFormat()));
	      } else {
		jasperPrint = JasperFillManager.fillReport(jasperReport, designParameters, con);
	      }
	    } catch (final Exception e) {
              log4j.error("OBSUPPORT : __________________________ ERROR: CATCH CONNECTION");
	      throw new ServletException(e.getCause() instanceof SQLException ? e.getCause().getMessage(): e.getMessage(), e);
	    } finally {
              log4j.error("OBSUPPORT: : _________________________ ERROR: FINALY CLOSE CONECTION");
	      releaseRollbackConnection(con);
	    }
	    if (exportParameters == null)
	      exportParameters = new HashMap<Object, Object>();
	    if (strOutputType == null || strOutputType.equals(""))
	      strOutputType = "pdf";
	    if (strOutputType.equals("pdf")) {
	      reportId = UUID.randomUUID();
	      saveReport(variables, jasperPrint, exportParameters, strFileName + "-" + (reportId) + "." + strOutputType);
	    } else {
	      throw new ServletException("Output format no supported");
	    }
	  } catch (final JRException e) {
	    log4j.error("JR: Error: ", e);
	    throw new ServletException(e.getMessage(), e);
	  } catch (Exception ioe) {
	    try {
	      FileUtility f = new FileUtility(globalParameters.strFTPDirectory, strFileName + "-"
		  + (reportId) + "." + strOutputType, false, true);
	      if (f.exists())
		f.deleteFile();
	    } catch (IOException ioex) {
	      log4j.error("Error trying to delete temporary report file " + strFileName + "-"
		  + (reportId) + "." + strOutputType + " : " + ioex.getMessage());
	    }
	  } 
	  return globalParameters.strFTPDirectory + Separador.substring(0,1) + strFileName + "-" + (reportId) + "." + strOutputType;
      
  }

}