/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package com.tegik.dirmexico.erpCommon.info;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class LocationTegik extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      vars.getRequestGlobalVariable("inpIDValue", "Location.inpcLocationId");
      // String strcLocationId =
      // vars.getRequestGlobalVariable("inpNameValue",
      // "Location.inpcLocationId");
      vars.getRequestGlobalVariable("inpwindowId", "Location.inpwindowId");
      printPageFS(response, vars);
    } else if (vars.commandIn("KEY")) {
      String strcLocationId = vars.getStringParameter("inpIDValue");
      // String strcLocationId = vars.getStringParameter("inpNameValue");
      if (log4j.isDebugEnabled())
        log4j.debug("1 Location: " + strcLocationId);
      LocationSearchDataTegik[] data = LocationSearchDataTegik.select(this, vars.getLanguage(),
          strcLocationId);
      if (data != null && data.length == 1) {
        printPageKey(response, vars, data[0]);
      } else {
        vars.setSessionValue("Location.inpcLocationId", strcLocationId);
        vars.getRequestGlobalVariable("inpwindowId", "Location.inpwindowId");
        printPageFS(response, vars);
      }
    } else if (vars.commandIn("FRAME1")) {
      String strcLocationId = vars.getSessionValue("Location.inpcLocationId");
      vars.removeSessionValue("Location.inpcLocationId");
      String strWindow = vars.getSessionValue("Location.inpwindowId");
      vars.removeSessionValue("Location.inpwindowId");
      printPageSheet(response, vars, strcLocationId, strWindow);
    } else if (vars.commandIn("SAVE_NEW")) {
      LocationSearchDataTegik data = getEditVariables(vars);
      String strSequence = SequenceIdData.getUUID();
      data.cLocationId = strSequence;
      data.insert(this);
      data.name = LocationSearchDataTegik.locationAddress(this, vars.getLanguage(), data.cLocationId);
      printPageKey(response, vars, data);
    } else if (vars.commandIn("SAVE_EDIT")) {
      LocationSearchDataTegik data = getEditVariables(vars);
      data.update(this);
      data.name = LocationSearchDataTegik.locationAddress(this, vars.getLanguage(), data.cLocationId);
      printPageKey(response, vars, data);
    } else if (vars.commandIn("OBTENER_ARRAY")) {
      String strcCountryId = vars.getStringParameter("inpcCountryId");
      String strWindow = vars.getSessionValue("Location.inpwindowId");
      printPageF2(response, vars, strcCountryId, strWindow);
    } else
      pageError(response);
  }

  private LocationSearchDataTegik getEditVariables(VariablesSecureApp vars) {
    LocationSearchDataTegik data = new LocationSearchDataTegik();
    data.cLocationId = vars.getStringParameter("inpCLocationId");
    data.adClientId = vars.getClient();
    data.adOrgId = vars.getOrg();
    data.createdby = vars.getUser();
    data.updatedby = vars.getUser();
    data.cCountryId = vars.getStringParameter("inpcCountryId");
    data.cRegionId = vars.getStringParameter("inpCRegionId");
    data.address1 = vars.getStringParameter("inpAddress1");
    data.emTdirmNumex = vars.getStringParameter("EM_Tdirm_Numex");
    data.emTdirmNumin = vars.getStringParameter("EM_Tdirm_Numin");
    data.address2 = vars.getStringParameter("inpAddress2");
    data.postal = vars.getStringParameter("inpPostal");
    data.city = vars.getStringParameter("inpCity");
    return data;
  }

  private void printPageKey(HttpServletResponse response, VariablesSecureApp vars,
      LocationSearchDataTegik data) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Location seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "com/tegik/dirmexico/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String generateResult(LocationSearchDataTegik data) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();

    html.append("\nfunction validateSelector() {\n");
    html.append("var key = \"" + data.cLocationId + "\";\n");
    html.append("var text = \""
        + Replace.replace(Replace.replace(data.name, "\\", "\\\\"), "\"", "\\\\\\\"") + "\";\n");
    html.append("parent.opener.closeSearch(\"SAVE\", key, text);\n");
    html.append("}\n");
    return html.toString();
  }

  private void printPageFS(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: FS Locations seeker");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("com/tegik/dirmexico/erpCommon/info/Location_FS")
        .createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strcLocationId, String strWindow) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: F1 Locations seeker");
    XmlDocument xmlDocument;

    LocationSearchDataTegik[] data;

    if (strcLocationId.equals("")) {
      xmlDocument = xmlEngine.readXmlTemplate("com/tegik/dirmexico/erpCommon/info/Location_F1")
          .createXmlDocument();
      xmlDocument.setParameter("Command", "NEW");
      // data = LocationSearchData.set(Utility.getDefault(this, vars, "C_Country_ID", "",
      // strWindow,""));

      // Set default country
      String strDefaultCountry = LocationSearchDataTegik.selectDefaultCountry(this, vars.getOrg(),
          Utility.getContext(this, vars, "#User_Client", strWindow));
      if (strDefaultCountry.equals("")) {
        strDefaultCountry = "106";
      }
      data = LocationSearchDataTegik.set(strDefaultCountry);
    } else {
      xmlDocument = xmlEngine.readXmlTemplate("com/tegik/dirmexico/erpCommon/info/Location_F1")
          .createXmlDocument();
      xmlDocument.setParameter("Command", "EDIT");
      if (log4j.isDebugEnabled())
        log4j.debug("2 Location: " + strcLocationId);
      data = LocationSearchDataTegik.select(this, vars.getLanguage(), strcLocationId);
    }
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    // Get the country id; default
    xmlDocument.setData("structure1", data);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Country_ID",
          "", "", Utility.getReferenceableOrg(vars, vars.getOrg()), Utility.getContext(this, vars,
              "#User_Client", strWindow), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindow, data[0].cCountryId);
      xmlDocument.setData("reportCountry", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Region_ID", "",
          "C_Region of Country", Utility.getReferenceableOrg(vars, vars.getOrg()),
          Utility.getContext(this, vars, "#User_Client", strWindow), 0);
      Utility.fillSQLParameters(this, vars, data[0], comboTableData, strWindow, data[0].cRegionId);
      xmlDocument.setData("reportRegion", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageF2(HttpServletResponse response, VariablesSecureApp vars,
      String strcCountryId, String strWindow) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: F2 Locations seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("com/tegik/dirmexico/erpCommon/info/Location_F2")
        .createXmlDocument();
    // Getcountry; default
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_Region_ID", "",
          "C_Region of Country", Utility.getContext(this, vars, "#AccessibleOrgTree", strWindow),
          Utility.getContext(this, vars, "#User_Client", strWindow), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindow, "");
      xmlDocument.setParameter("array",
          Utility.arrayEntradaSimple("regions", comboTableData.select(false)));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents the Locations seeker";
  } // end of getServletInfo() method
}
