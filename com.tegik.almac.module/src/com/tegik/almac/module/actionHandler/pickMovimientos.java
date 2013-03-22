
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

public class pickMovimientos extends BaseProcessActionHandler {
 
  private static final Logger log = Logger.getLogger(pickMovimientos.class);
  OBDal dalCarlos = OBDal.getInstance();
 
  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
      OBContext.setAdminMode();
      JSONObject jsonRequest = null;
    try {
      jsonRequest = new JSONObject(content);
      final String movementId = jsonRequest.getString("M_Movement_ID");
      //log.info(">> movementId: " + movementId);
 
      // _selection contains the rows that the user selected.
      JSONArray selection = new JSONArray(
          jsonRequest.getString(ApplicationConstants.SELECTION_PROPERTY));
 
      //log.info(">> selected: " + selection);
 
      // _allRows contains all the rows available in the grid
      JSONArray allRows = new JSONArray(jsonRequest.getString(ApplicationConstants.ALL_ROWS_PARAM));
 
      //log.info(">> allRows: " + allRows);
 
      // A Pick and Execute process can have several buttons (buttonList)
      // You can know which button was clicked getting the value of _buttonValue
      //log.info(">> clicked button: " + jsonRequest.getString(ApplicationConstants.BUTTON_VALUE));

      HashMap<String, String> map = insertarPiezas(jsonRequest, movementId);
      //log.info(map.get("Count"));
 
      jsonRequest = new JSONObject();

      JSONObject errorMessage = new JSONObject();
      errorMessage.put("severity", "success");
      errorMessage.put("text", OBMessageUtils.messageBD("Success"));
      if (map.get("Error").equals("true")) {
        //log.info("Se encontró un error");
        errorMessage.put("severity", "warning");
        errorMessage.put("text", map.get("Message"));
      }
      jsonRequest.put("message", errorMessage);

      return jsonRequest;

    } catch (Exception e) {
      dalCarlos.getInstance().rollbackAndClose();
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

  private HashMap<String, String> insertarPiezas(JSONObject jsonRequest, String movementId) throws JSONException, OBException {

    HashMap<String, String> map = new HashMap<String, String>();
    String error = "false";
    String errores = "";
    map.put("Mensaje", "");
    map.put("Count", "0");
    
    JSONArray selectedLines = jsonRequest.getJSONArray("_selection");
    
    if (selectedLines.length() == 0) {
      return map;
    }
    int cont = 0;

    InternalMovement movementObject = dalCarlos.getInstance().get(InternalMovement.class,movementId);

    long lineno = 10;

    for (int i = 0; i < selectedLines.length(); i++) {
		Product productObject = null;
		AttributeSetInstance asiObject = null;
		UOM uomObject = null;
	    try {
		InternalMovementLine mlObject = OBProvider.getInstance().get(InternalMovementLine.class);
		JSONObject selectedLine = selectedLines.getJSONObject((int) i);
		//log.info(selectedLine);
		//log.info("VARIABLE ERROR" + error);
		//log.info ("**************************");
		String asi = selectedLine.getString("attributeSetValue");
		String product = selectedLine.getString("product");
		BigDecimal cantidad = new BigDecimal(selectedLine.getString("cantidad"));

		productObject = dalCarlos.getInstance().get(Product.class,product);
		asiObject = dalCarlos.getInstance().get(AttributeSetInstance.class,asi);
		uomObject = dalCarlos.getInstance().get(UOM.class,"100");

		if(productObject.getDmprodTileoslab().equals("Slab"))
		{
			BigDecimal v_reservadas = new BigDecimal(PickMovimientosData.sacaReservasSlab(new DalConnectionProvider(), asi));
			//log.info("V_RESERVADAS SLABS /// " + v_reservadas.toString());
			if (v_reservadas.compareTo(BigDecimal.ZERO) == 1)
			{	
				//log.info("CARLOS SALINAS // ERROR EN LOS SLABS");
				error = "true";
				errores = errores + " /// " + productObject.getName() + " /// " + asiObject.getDescription() + " /// " + "La pieza ya se encuentra reservada en una órden." + " /// ";
				continue;
			}
		}
		else
		{
			BigDecimal v_reservadas = new BigDecimal(PickMovimientosData.sacaReservasTile(new DalConnectionProvider(), asi, movementObject.getAlmacLocator().getId()));
			//log.info("V_RESERVADAS TILE /// " + v_reservadas.toString());
			BigDecimal v_inventario = new BigDecimal(PickMovimientosData.sacaInventarioTile(new DalConnectionProvider(), asi, movementObject.getAlmacLocator().getId()));
			//log.info("V_INVENTARIO TILE /// " + v_inventario.toString());
			BigDecimal v_disponibles = v_inventario.subtract(v_reservadas);
			//log.info("V_RESERVADAS TILE /// " + v_disponibles.toString());
			if (cantidad.compareTo(v_disponibles) == 1)
			{	
				//log.info("CARLOS SALINAS // ERROR EN LOS TILES");
				error = "true";
				errores = errores + " /// " + productObject.getName() + " /// " + asiObject.getDescription() + " /// " + "No cuenta con suficiente material disponible " + v_disponibles.toString() + "en el huacal." + " /// ";
				continue;
			}
		}

		mlObject.setStorageBin(movementObject.getAlmacLocator());
		mlObject.setNewStorageBin(movementObject.getAlmacLocatorto());
		mlObject.setProduct(productObject);
		mlObject.setOrganization(movementObject.getOrganization());
		mlObject.setMovement(movementObject);
		mlObject.setAttributeSetValue(asiObject);
		mlObject.setMovementQuantity(cantidad);
		mlObject.setLineNo(lineno);
		mlObject.setUOM(uomObject);
		dalCarlos.getInstance().save(mlObject);
		dalCarlos.getInstance().flush();
		lineno = lineno + 10;
		cont++;
	    } catch (Exception e3) {
	      error = "true";
	      //log.info("CARLOS -- Mensaje de error de DBE" + e3.getMessage());
	      errores = errores + " /// " + productObject.getName() + " /// " + asiObject.getDescription() + " /// " + e3.getMessage() + " /// ";
	      //log.info("CARLOS -- Mensaje que contiene la variable de errores" + errores);
	      break;
	      // do nothing, give up
	    }
	      
      }

      map.put("Error", error);
      //log.info ("30");
      map.put("Message", errores);
      //log.info ("31");
      map.put("Count", Integer.toString(cont));
      //log.info ("32");
      return map;
    }  
}

