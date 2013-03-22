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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package com.tegik.ventas.module.actionHandler;
 
import java.util.Map;
import java.util.HashMap;
import org.codehaus.jettison.json.JSONException;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
 
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.ApplicationConstants;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import com.tegik.ventas.module.data.ventaspickexecute;
import org.openbravo.model.common.plm.AttributeSetInstance;

/**
 * @author iperdomo
 * 
 */
public class reservarCotizacion extends BaseProcessActionHandler {
 
  private static final Logger log = Logger.getLogger(reservarCotizacion.class);
 
  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
      OBContext.setAdminMode();
      JSONObject jsonRequest = null;
    try {
      jsonRequest = new JSONObject(content);
      final String pickId = jsonRequest.getString("Ventas_Pickexecute_ID");
      log.info(">> pickId: " + pickId);
 
      log.info(">> parameters: " + parameters);
      // log.info(">> content:" + content);
 
      // _selection contains the rows that the user selected.
      JSONArray selection = new JSONArray(
          jsonRequest.getString(ApplicationConstants.SELECTION_PROPERTY));
 
      log.info(">> selected: " + selection);
 
      // _allRows contains all the rows available in the grid
      JSONArray allRows = new JSONArray(jsonRequest.getString(ApplicationConstants.ALL_ROWS_PARAM));
 
      log.info(">> allRows: " + allRows);
 
      // A Pick and Execute process can have several buttons (buttonList)
      // You can know which button was clicked getting the value of _buttonValue
      log.info(">> clicked button: " + jsonRequest.getString(ApplicationConstants.BUTTON_VALUE));

      ventaspickexecute pickObject = OBDal.getInstance().get(ventaspickexecute.class,pickId);

      //String accion = pickObject.getAccion();

      HashMap<String, String> map = reservarPiezas(jsonRequest, pickObject);
      log.info(map.get("Count"));
 
      jsonRequest = new JSONObject();

      JSONObject errorMessage = new JSONObject();
      errorMessage.put("severity", "success");
      errorMessage.put("text", OBMessageUtils.messageBD("Success"));
      if (map.get("Error").equals("true")) {
        errorMessage.put("severity", "error");
        errorMessage.put("text", OBMessageUtils.messageBD(map.get("Mensaje")));
      }
      jsonRequest.put("message", errorMessage);

      return jsonRequest;

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error(e.getMessage(), e);
      try {
        jsonRequest = new JSONObject();
        JSONObject errorMessage = new JSONObject();

        errorMessage.put("severity", "error");
        errorMessage.put("text", OBMessageUtils.messageBD(e.getMessage()));
        jsonRequest.put("message", errorMessage);
	return jsonRequest;
      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
        // do nothing, give up
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    return jsonRequest;
  }

  private HashMap<String, String> reservarPiezas(JSONObject jsonRequest, ventaspickexecute pickObject) throws JSONException, OBException {

    HashMap<String, String> map = new HashMap<String, String>();
    map.put("Error", "false");
    map.put("Mensaje", "");
    map.put("Count", "0");
    
    String accion = pickObject.getAccion();

    JSONArray selectedLines = jsonRequest.getJSONArray("_selection");
    
    if (selectedLines.length() == 0) {
      return map;
    }
    int cont = 0;

    for (int i = 0; i < selectedLines.length(); i++) {
	    JSONObject selectedLine = selectedLines.getJSONObject((int) i);
	    log.info(selectedLine);
	    log.info ("**************************");
	    String asi = selectedLine.getString("attributeSetValue");
	    AttributeSetInstance asiObject = OBDal.getInstance().get(AttributeSetInstance.class,asi);
	    if (accion.equals("CO"))
	    {
		  asiObject.setAlmacReservacotizacion(true);
	    }
	    
	    if (accion.equals("PE"))
	    {
		  asiObject.setAlmacReservacotizacion(false);
		  asiObject.setAlmacReservapedido(true);
	    }
  
	    asiObject.setVentasPickexecute(pickObject);

	    OBDal.getInstance().save(asiObject);
	    OBDal.getInstance().flush();
	    cont++;
      }
      map.put("Count", Integer.toString(cont));
      return map;
    }
    

}

