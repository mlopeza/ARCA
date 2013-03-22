package com.tegik.facelectr.ad_actionButton;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import org.openbravo.model.common.invoice.Invoice;
import org.apache.log4j.Logger;
import org.openbravo.erpCommon.utility.poc.EmailManager;
import org.openbravo.model.common.enterprise.EmailServerConfiguration;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.model.common.enterprise.DocumentTemplate;

import org.hibernate.criterion.Expression;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;

import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FileUtility;
import javax.servlet.ServletException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.model.common.enterprise.EmailServerConfiguration;
import org.openbravo.model.common.enterprise.EmailTemplate;
import org.openbravo.model.common.enterprise.DocumentTemplate;

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
import org.openbravo.erpCommon.utility.JRFieldProviderDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import org.openbravo.utils.FileUtility;
import org.openbravo.utils.Replace;
import org.openbravo.data.FieldProvider;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;
import java.io.File;
import org.openbravo.erpCommon.utility.PrintJRData;
import javax.servlet.ServletOutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import org.apache.commons.codec.binary.Base64;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.base.ConfigParameters;
import org.openbravo.dal.core.OBContext;

import java.util.HashMap;  


public class enviadorCorreos extends HttpSecureAppServlet {

    public void init(ServletConfig config) {
        super.init(config);
        boolHist = false;
    }

    private static final Logger log = Logger.getLogger(enviadorCorreos.class);
    protected ConfigParameters globalParameters;

public String enviaCorreo(VariablesSecureApp vars, Invoice factura, String enviarPDF, String enviarXML) {
    try{  
	  //VariablesSecureApp vars = new VariablesSecureApp(request);
	  OBContext.setAdminMode(true);

	  String correoAlternativo = factura.getFetCorreoalternativo();
	  log.info("CSM: -- correoAlternativo -- " + correoAlternativo);
	  String correoCliente =factura.getBusinessPartner().getFetEmail();
	  log.info("CSM: -- correoCliente -- " + correoCliente);
	  String Correo = "";
	  log.info("CSM: -- Correo -- " + Correo);

	  log.info("CSM: -- CorreoCliente -- " + correoCliente);
	  log.info("CSM: -- correoAlternativo -- " + correoAlternativo);

	  if (correoCliente == null){
	      correoCliente = "";
	  }

	  if (correoAlternativo == null){
	      correoAlternativo = "";
	  }


	  if (correoCliente != null || !correoCliente.equals("")){
	    if (checaFormatoEmail(correoCliente).equals("OK")){
	      Correo = correoCliente;
	    }
	  }

	  if (correoAlternativo != null || !correoAlternativo.equals("")){
	    if (checaFormatoEmail(correoAlternativo).equals("OK")){
	      if (Correo == null || Correo.equals("")){
		 Correo = correoAlternativo;
	      }else
	      {
		 Correo = Correo + ", " + correoAlternativo;
	      }
	    }
	  }
	  
	  if (Correo.equals("")){
	    return "El correo electrónico del receptor no es válido";
	  }

	  log.info("CSM>CORREOS -- " + Correo);


	  final OBCriteria<EmailServerConfiguration> configList = OBDal.getInstance().createCriteria(EmailServerConfiguration.class);
		    configList.add(Expression.eq(EmailServerConfiguration.PROPERTY_CLIENT, factura.getClient()));

	  EmailServerConfiguration emailConfig = null;
	  
	  for (EmailServerConfiguration emailConfigUd : configList.list()) {
	    emailConfig = emailConfigUd;
	  }

	  long LongPuerto = emailConfig.getSmtpPort();
	  int Puerto = (int) LongPuerto;
	  String Servidor = emailConfig.getSmtpServer();
	  log.info("CSM: -- Servidor -- " + Servidor);
	  boolean Auth = emailConfig.isSMTPAuthentification();
	  String Cuenta = emailConfig.getSmtpServerAccount();
	  log.info("CSM: -- Cuenta -- " + Cuenta);
	  String Password = FormatUtilities.encryptDecrypt(emailConfig.getSmtpServerPassword(), false);
	  log.info("CSM: -- Password -- " + Password);
	  String CuentaEnvio = emailConfig.getSmtpServerSenderAddress();
	  log.info("CSM: -- CuentaEnvio -- " + CuentaEnvio);
	  String Asunto = "Factura " + factura.getDocumentNo() + " correspondiente a su compra en " + factura.getOrganization().getSocialName();
	  log.info("CSM: -- Asunto -- " + Asunto);
	  String Mensaje = "Buen día. Le hacemos llegar sus archivos .xml y .pdf correspondientes a su factura #" + factura.getDocumentNo() + ". Gracias.";
	  log.info("CSM: -- Mensaje -- " + Mensaje);
	  String idTabla = "318";
	  log.info("CSM: -- idTabla -- " + idTabla);
	  String Separador = System.getProperty("file.separator");
	  log.info("CSM: -- Separador -- " + Separador);
	  String attachFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("attach.path");
	  log.info("CSM: -- attachFolder -- " + attachFolder);
	  String seguridad = emailConfig.getSmtpConnectionSecurity();
	  log.info("CSM: -- seguridad -- " + seguridad);
	  String rutaAttach = idTabla + "-" + factura.getId();
	  String NumFac = factura.getDocumentNo();
	  String invoiceID = factura.getId();

	  HashMap<String, Object> parameters = new HashMap<String, Object>();
	  parameters.put("DOCUMENT_ID", factura.getId());

	  /*final OBCriteria<DocumentTemplate> templateList = OBDal.getInstance().createCriteria(DocumentTemplate.class);
		  templateList.add(Expression.eq(DocumentTemplate.PROPERTY_DOCUMENTTYPE, factura.getDocumentType()));

	  for (DocumentTemplate templateImpresiones : templateList.list()) {
	    if(!templateImpresiones.isDttIssalidaalmacen() && !templateImpresiones.isDttIscopia()){
			  String templateId = templateImpresiones.getId();
			  //strReportName = templateImpresiones.
		  }
	  }*/

	  String strReportName = "@basedesign@/com/tegik/facelectr/ad_actionButton/reports/EM_FET_Plantilla_Factura_VentaJR.jrxml";
	  facturaElectronica facelectr = new facturaElectronica();
 	  String rutaPDF = renderJRPiso(vars, strReportName, "pdf", parameters, null, null, attachFolder, NumFac, invoiceID);

	  log.info("CSM>CORREOS" + rutaPDF);

	  File archivoPDF = new File(rutaPDF);

	  if (!archivoPDF.exists()){
	    return "No se puede enviar el correo ya que no se encuentra el archivo pdf";
	  }

	  File archivoXML = new File(attachFolder + Separador + idTabla + "-" + factura.getId() + Separador + factura.getDocumentNo() + ".xml");
	  log.info(attachFolder + Separador + idTabla + "-" + factura.getId() + Separador + factura.getDocumentNo() + ".xml");

	  if (!archivoXML.exists()){
	    return "No se puede enviar el correo ya que no se encuentra el archivo xml";
	  }
	  
	  log.info("CSM -- Archivo -- " + archivoXML.toString());

 	  List<File> listaArchivos = new ArrayList();
	  listaArchivos.add(archivoPDF);
	  listaArchivos.add(archivoXML);

	  EmailManager correo = new EmailManager();
	  //correo.sendEmail(Servidor, Auth, Cuenta, Password, seguridad,Puerto, CuentaEnvio, Correo, "", "csalinas@tegik.com", CuentaEnvio, Asunto, Mensaje, null, listaArchivos, new Date(), null);
	  correo.sendEmail(Servidor, Auth, Cuenta, Password, seguridad,Puerto, CuentaEnvio, Correo, "", "", CuentaEnvio, Asunto, Mensaje, null, listaArchivos, new Date(), null);
	  OBContext.restorePreviousMode();
	  
	  return "OK";
    }
    catch(Exception e) {
	StringWriter w = new StringWriter();
	e.printStackTrace(new PrintWriter(w));
	String errorfactura = w.toString();
	log.info("CSM>CORREOS --ENVIACORREO-- " + errorfactura);
	return e.toString();
      }
  }


public String solicitarEnvio(VariablesSecureApp vars, Invoice factura, String enviarPDF, String enviarXML) {
    try{
 	  String statusEnvio = enviaCorreo(vars, factura, enviarPDF, enviarXML);
	  log.error(statusEnvio);
	  if (statusEnvio == "OK"){
	      OBContext.setAdminMode(true); 
	      factura.setFetCorreoenviado(true);
	      factura.setFetStatuscorreo("Correo enviado exitosamente");
	      long intento = 0;
              factura.setFetIntento(intento);
	      OBDal.getInstance().save(factura);
	      OBDal.getInstance().flush();
	      OBContext.restorePreviousMode();
	      return "OK";
	  }
	  else{
	      OBContext.setAdminMode(true); 
	      factura.setFetCorreoenviado(false);
	      factura.setFetStatuscorreo("statusEnvio");
	      Long intento = factura.getFetIntento();
              long uno = 1;
	      if (intento!=null){
		  
		  factura.setFetIntento(intento + uno);
	      }else{
		  factura.setFetIntento(uno);
	      }
	      OBDal.getInstance().save(factura);
	      OBDal.getInstance().flush();
	      OBContext.restorePreviousMode();
	      return statusEnvio;
	  }
	  
    }
    catch(Exception e) {
	StringWriter w = new StringWriter();
	e.printStackTrace(new PrintWriter(w));
	String errorfactura = w.toString();
	log.info("CSM>CORREOS --SOLICITARENVIO-- " + errorfactura);
	return e.toString();
      }
  }


public String checaFormatoEmail(String correo) { 
  Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
  Matcher m = p.matcher(correo);
  boolean matchFound = m.matches();
  
  if(matchFound){
  return "OK";
  }else{
  return"ERROR";
  }
}

  public void saveReport(VariablesSecureApp vars, JasperPrint jp,Map<Object,Object> exportParameters, String fileName, String rutaAttach, String NumFac) throws JRException {

	  OBContext.setAdminMode(true);

	  //final String outputFile = globalParameters.strFTPDirectory + "/"+ rutaAttach + "/" + NumFac + ".pdf";
	  final String outputFile = fileName;
	  log.info("CSM > outputFile > " + outputFile);
	  final String reportType = fileName.substring(fileName.lastIndexOf(".") + 1);
	  log.info("CSM > reportType > " + reportType);
	  if (reportType.equalsIgnoreCase("pdf")) {
	    JasperExportManager.exportReportToPdfFile(jp, outputFile);
	    OBContext.restorePreviousMode();
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
	    OBContext.restorePreviousMode();
	  } else {
	    OBContext.restorePreviousMode();
	    throw new JRException("Report type not supported");
	  }

  }

public String renderJRPiso(VariablesSecureApp variables, String strReportName, String strOutputType, HashMap<String, Object> designParameters, FieldProvider[] data, Map<Object, Object> exportParameters, String rutaAttach, String NumFac, String invoiceID) throws IOException, ServletException {	    
      
	  OBContext.setAdminMode(true); 

	  if (strReportName == null || strReportName.equals(""))
	    strReportName = PrintJRData.getReportName(this, classInfo.id);
	    
	    
	  log.info("CSM > strReportName > " + strReportName);

	  final String strAttach = rutaAttach + "/284-" + invoiceID;
	  log.info("CSM > strAttach > " + strAttach);

	  final String strLanguage = variables.getLanguage();
	  log.info("CSM > strLanguage > " + strLanguage);
	  
	  final Locale locLocale = new Locale(strLanguage.substring(0, 2), strLanguage.substring(3, 5));
	  log.info("CSM > locLocale > " + locLocale);

	  final String strBaseDesign = getBaseDesignPath(strLanguage);
	  log.info("CSM > strBaseDesign > " + strBaseDesign);	  

	  strReportName = Replace.replace(Replace.replace(strReportName, "@basedesign@", strBaseDesign), "@attach@", strAttach);
	  log.info("CSM > strReportName > " + strReportName);
	  final String strFileName = strReportName.substring(strReportName.lastIndexOf("/") + 1);
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
	    PrintJRData.getReportTitle(this, variables.getLanguage(), invoiceID));

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
	      throw new ServletException(e.getCause() instanceof SQLException ? e.getCause().getMessage(): e.getMessage(), e);
	    } finally {
	      releaseRollbackConnection(con);
	    }
	    if (exportParameters == null)
	      exportParameters = new HashMap<Object, Object>();
	    if (strOutputType == null || strOutputType.equals(""))
	      strOutputType = "pdf";
	    if (strOutputType.equals("pdf")) {
	      reportId = UUID.randomUUID();
	      saveReport(variables, jasperPrint, exportParameters, rutaAttach + "/" + strFileName + "-" + (reportId) + "." + strOutputType, rutaAttach, NumFac);
	    } else {
	      throw new ServletException("Output format no supported");
	    }
	  } catch (final JRException e) {
	    log4j.error("JR: Error: ", e);
	    throw new ServletException(e.getMessage(), e);
	  } catch (Exception ioe) {
	    try {
	      FileUtility f = new FileUtility(rutaAttach, strFileName + "-"
		  + (reportId) + "." + strOutputType, false, true);
	      if (f.exists())
		f.deleteFile();
	    } catch (IOException ioex) {
	      log4j.error("Error trying to delete temporary report file " + strFileName + "-"
		  + (reportId) + "." + strOutputType + " : " + ioex.getMessage());
	    }
	  } 
	  OBContext.restorePreviousMode();
	  log.info(rutaAttach + "/" + strFileName + "-" + (reportId) + "." + strOutputType);
	  return rutaAttach + "/" + strFileName + "-" + (reportId) + "." + strOutputType;
      
  }
  
}
