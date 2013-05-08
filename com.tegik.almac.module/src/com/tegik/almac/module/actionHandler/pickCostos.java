
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
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.base.provider.OBProvider;
import java.math.BigDecimal;
import com.tegik.almac.module.data.almacCalidadProducto;
public class pickCostos extends BaseProcessActionHandler {
 
  private static final Logger log = Logger.getLogger(pickCostos.class);
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
      
      //log.info("Error 6");
      HashMap<String, String> map = insertarPiezas(jsonRequest);
      //log.info("Error 5");
      jsonRequest = new JSONObject();
      //log.info("Error 4");
      JSONObject errorMessage = new JSONObject();
      
      
      //Hubo errores
      if ((map.get("Error")).equals("true")) {
        //log.info("Error 1");
        errorMessage.put("severity", "warning");
        errorMessage.put("text", "Mensaje");
        jsonRequest.put("message", errorMessage);
      }else{
        //log.info("Error 2");
        errorMessage.put("severity", "success");
        errorMessage.put("text", "Las piezas han sido cambiadas. Favor de actualizar la ventana para verlo reflejado.");  
        jsonRequest.put("message", errorMessage);
      }
      //log.info("Error 3");
      return jsonRequest;

    } catch (Exception e) {
      obdal.getInstance().rollbackAndClose();
      try{
        jsonRequest = new JSONObject();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", e.getMessage());
        jsonRequest.put("message", errorMessage);
        OBContext.restorePreviousMode();
        return jsonRequest;
      }catch(Exception e2){
        //log.info(e2.getMessage());
        return null;
      }
    }

  }

  private HashMap<String, String> insertarPiezas(JSONObject jsonRequest) throws JSONException, OBException {
    // Toma todos los atributos seleccionados.
    JSONArray selection = new JSONArray(jsonRequest.getString(ApplicationConstants.SELECTION_PROPERTY));
    
    HashMap<String, String> map = new HashMap<String, String>();
    String error = "false";
    String errores = "";
    
    JSONArray selectedLines = jsonRequest.getJSONArray("_selection");
    //log.info(selectedLines.toString());
    if (selectedLines.length() == 0) {
      return map;
    }
    
    //Trae la pieza de la BD
    

    for (int i = 0; i < selectedLines.length(); i++) {
	    try {
	        JSONObject selectedLine = selectedLines.getJSONObject((int) i);
	        
	        //Trae el objeto de la Base de Datos
	        String pieza = selectedLine.getString("attributeSetValue");
	        //log.info(selectedLine.getString("attributeSetValue"));
	        //log.info(selectedLine.getString("costoReal"));
	        //log.info(selectedLine.getString("calidadDeLaPieza"));
	        AttributeSetInstance p = obdal.get(AttributeSetInstance.class,pieza);
	        
	        //El costo del producto
	        if(selectedLine.getString("costoReal") != null)
	          p.setAlmacCostoRealusd(new BigDecimal(selectedLine.getString("costoReal")));
	        //La calidad de la pieza
	        if(selectedLine.getString("calidadDeLaPieza") != null)
	          p.setAlmacCalidadP(obdal.get(almacCalidadProducto.class,selectedLine.getString("calidadDeLaPieza")));
	        //log.info("Error 10");
	        obdal.save(p);
	        //log.info("Error 1");
	        
	    } catch (Exception e3) {
	      //log.info("Error 22");
	      error = "true";
	      errores = " /// " + e3.getMessage() + " /// ";
	      break;
	    }
	    //log.info("Error 23");
	    obdal.flush();
      }

      //Guarda los errores
      map.put("Error", error);
      map.put("Message", errores);
      return map;
    }  
}

