/*
 * 
 * The contents of this file are subject to the Openbravo Public License Version
 * 1.0 (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SLU All
 * portions are Copyright (C) 2001-2009 Openbravo SLU All Rights Reserved.
 * Contributor(s): ______________________________________.
 */
package com.tegik.facelectr.ad_actionButton;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;

public class EM_FET_Plantilla_Factura_Venta extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      //String strcInvoiceId = "9970F030CC6A4BA18F84242330F5AEBC";
      String strcInvoiceId = vars.getSessionValue("EM_FET_Plantilla_Factura_Venta.inpcInvoiceId_R");
      log4j.info("LA~ strcInvoiceId = " +strcInvoiceId);
  
      if (strcInvoiceId.equals(""))
        strcInvoiceId = vars.getSessionValue("EM_FET_Plantilla_Factura_Venta.inpcInvoiceId");
  
        
      log4j.info("LA~ strcInvoiceId = " +strcInvoiceId);
      if (log4j.isDebugEnabled())
        log4j.debug("strcInvoiceId: " + strcInvoiceId);
        
      printPagePartePDF(response, vars, strcInvoiceId);
      
    } else
      pageError(response);
  }

  private void printPagePartePDF(HttpServletResponse response, VariablesSecureApp vars, String strcInvoiceId) throws IOException, ServletException {

    if (log4j.isDebugEnabled())
    log4j.debug("Output: EM_FET_Plantilla_Factura_Ventas - pdf");

    String strLanguage = vars.getLanguage();
    String strBaseDesign = getBaseDesignPath(strLanguage);

	log4j.error("strcInvoiceId: " + strcInvoiceId);
	strcInvoiceId=strcInvoiceId.replace("(", "");
	strcInvoiceId=strcInvoiceId.replace(")", "");
	strcInvoiceId=strcInvoiceId.replace("'", "");

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    log4j.error("strcInvoiceId: " + strcInvoiceId);
        parameters.put("DOCUMENT_ID", strcInvoiceId);
    

    response.setHeader("Content-disposition", "inline; filename=EM_FET_Plantilla_Factura_Ventas.pdf");
    String strReportName = "@basedesign@/com/tegik/facelectr/ad_actionButton/reports/EM_FET_Plantilla_Factura_VentaJR.jrxml";
    renderJR(vars, response, strReportName, "pdf", parameters, null, null);
  }

  public String getServletInfo() {
    return "Servlet that print Movements between warehouses document";
  } // End of getServletInfo() method
}
