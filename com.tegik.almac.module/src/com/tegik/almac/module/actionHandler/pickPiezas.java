
package com.tegik.almac.module.actionHandler;
 
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
import org.openbravo.service.db.DalConnectionProvider;

import com.tegik.ventas.module.data.ventaspickexecute;
import org.openbravo.model.materialmgmt.transaction.InternalMovement;
import org.openbravo.model.materialmgmt.transaction.InternalMovementLine;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.uom.UOM;
import java.math.BigDecimal;

import com.tegik.almac.module.data.almacCatPiedra;
import com.tegik.almac.module.data.almac_sizechange;
import com.tegik.almac.module.data.almac_sizechangeline;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.plm.ProductUOM;
public class pickPiezas extends BaseProcessActionHandler {
 
  private static final Logger log = Logger.getLogger(pickPiezas.class);
  OBDal obdal = OBDal.getInstance();
 
  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
      //log.info(">> parameters: " + parameters);
      OBContext.setAdminMode();
      JSONObject jsonRequest = null;
    try {
      jsonRequest = new JSONObject(content); 
      //log.info(jsonRequest.toString());
      // _allRows contains all the rows available in the grid
      JSONArray allRows = new JSONArray(jsonRequest.getString(ApplicationConstants.ALL_ROWS_PARAM));
      
      HashMap<String, String> map = insertarPiezas(jsonRequest);
      //log.info(map.get("Count"));
 
      jsonRequest = new JSONObject();

      JSONObject errorMessage = new JSONObject();
      errorMessage.put("severity", "success");
      errorMessage.put("text", OBMessageUtils.messageBD("Success"));
      if ((map.get("Error")).equals("true")) {
        //log.info("Se encontró un error");
        errorMessage.put("severity", "warning");
        errorMessage.put("text", "Mensaje");
      }
      jsonRequest.put("message", errorMessage);

      return jsonRequest;

    } catch (Exception e) {
      obdal.getInstance().rollbackAndClose();
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

  private HashMap<String, String> insertarPiezas(JSONObject jsonRequest) throws JSONException, OBException {
    //Trae el Identificador de la cabecera
    final String sizeChange = jsonRequest.getString("Almac_Sizechange_ID");
    almac_sizechange header = obdal.get(almac_sizechange.class,sizeChange);
    
    // Toma todos los atributos seleccionados.
    JSONArray selection = new JSONArray(jsonRequest.getString(ApplicationConstants.SELECTION_PROPERTY));
    
    HashMap<String, String> map = new HashMap<String, String>();
    String error = "false";
    String errores = "";
    map.put("Mensaje", "");
    map.put("Count", "0");
    
    JSONArray selectedLines = jsonRequest.getJSONArray("_selection");
    //log.info(selectedLines.toString());
    if (selectedLines.length() == 0) {
      return map;
    }
    int cont = 0;

    

    long lineno = 10;

    for (int i = 0; i < selectedLines.length(); i++) {
	    try {
	        JSONObject selectedLine = selectedLines.getJSONObject((int) i);
	        almac_sizechangeline linea = OBProvider.getInstance().get(almac_sizechangeline.class);
	        linea.setAlmacSizechange(header);
	        linea.setAttributeSetValue(obdal.get(AttributeSetInstance.class,selectedLine.getString("attributeSetValue")));
	        linea.setHeight(new BigDecimal(selectedLine.getString("nEWHeight")));
	        linea.setWidth(new BigDecimal(selectedLine.getString("nEWWidth")));
	        //linea.setLineNo(lineno);
	        linea.setQTYM2(new BigDecimal(selectedLine.getString("quantityOnHand")));
	        linea.setUOM(obdal.get(UOM.class,selectedLine.getString("uOM")));
	        linea.setStorageBin(obdal.get(Locator.class,selectedLine.getString("storageBin")));
	        linea.setProduct(obdal.get(Product.class,selectedLine.getString("product")));
	        if(selectedLine.getString("nEWQty") != null){
	          linea.setQuantity(new BigDecimal(selectedLine.getString("nEWQty")));
	        }else{
	          linea.setQuantity(BigDecimal.ZERO);
	        }
	        if(selectedLine.getString("orderUOM") != null)
	          linea.setOrderUOM(obdal.get(ProductUOM.class,selectedLine.getString("orderUOM")));
	        obdal.save(linea);
		lineno = lineno + 10;
		cont++;
	    } catch (Exception e3) {
	      error = "true";
	      errores = " /// " + e3.getMessage() + " /// ";
	      break;
	      // do nothing, give up
	    }
	      
      }

      map.put("Error", error);
      map.put("Message", errores);
      map.put("Count", Integer.toString(cont));
      return map;
    }  
}

