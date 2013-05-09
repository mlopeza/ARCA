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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package com.tegik.almac.module.actionHandler;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import java.util.Date;
import java.util.Map;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.materialmgmt.InventoryCountProcess;
import com.tegik.almac.module.data.almac_sizechange;
import com.tegik.almac.module.data.almac_sizechangeline;
import org.apache.log4j.Logger;
import org.openbravo.model.materialmgmt.transaction.InventoryCount;
import org.openbravo.model.materialmgmt.transaction.InventoryCountLine;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;


public class ChangeSize extends DalBaseProcess {
  private static final Logger log = Logger.getLogger(ChangeSize.class);
  private OBDal obdal = OBDal.getInstance();
  
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      
      //Imprime todos los parametros
      almac_sizechange header = obdal.get(almac_sizechange.class,(String) bundle.getParams().get("Almac_Sizechange_ID"));
      
      //Query para verificar que ninguno tenga medidas en 0
      OBCriteria<almac_sizechangeline> pQ = obdal.createCriteria(almac_sizechangeline.class);
      pQ.add(Restrictions.eq(almac_sizechangeline.PROPERTY_ALMACSIZECHANGE,header));
      pQ.add(Restrictions.or(Restrictions.isNull(almac_sizechangeline.PROPERTY_ALMACRAZONAJUSTE),Restrictions.or(Restrictions.le(almac_sizechangeline.PROPERTY_HEIGHT, BigDecimal.ZERO), Restrictions.le(almac_sizechangeline.PROPERTY_WIDTH, BigDecimal.ZERO))));
      List<almac_sizechangeline> sizeList = pQ.list();
      
      //Verifica que ningun producto tenga tamano menor a 0
      if(!sizeList.isEmpty()){
        final OBError msg = new OBError();
        msg.setType("Error");
        msg.setTitle("Error en las piezas.");
        msg.setMessage("Hay piezas con medidas menores o igual a cero o sin una razón de cambio.");
        bundle.setResult(msg);        
        return;
      }
      
    //Verifica que el inventario del producto no este en 0
      //Query para verificar que ninguno tenga medidas en 0
      pQ = obdal.createCriteria(almac_sizechangeline.class);
      pQ.add(Restrictions.eq(almac_sizechangeline.PROPERTY_QTYM2,BigDecimal.ZERO));
      sizeList = pQ.list();
      
      //Verifica que ningun producto tenga tamano menor a 0
      if(!sizeList.isEmpty()){
        final OBError msg = new OBError();
        msg.setType("Error");
        msg.setTitle("Error en inventario de productos.");
        msg.setMessage("Hay piezas con que no tienen inventario y no se les puede hacer el cambio de tamaño.");
        bundle.setResult(msg);        
        return;
      }
      
    //Query para traer todas la spiezas
      pQ = obdal.createCriteria(almac_sizechangeline.class);
      pQ.add(Restrictions.eq(almac_sizechangeline.PROPERTY_ALMACSIZECHANGE,header));
      sizeList = pQ.list();
      
      //Modifica el inventario por medio de Physical Inventory 
      InventoryCount inv = OBProvider.getInstance().get(InventoryCount.class);
      inv.setName(header.getCommercialName()+" "+header.getCreationDate().toString());
      inv.setDescription("Ajuste de Tamaño de Piezas  Ajuste:"+header.getCommercialName());
      inv.setMovementDate(new Date());
      inv.setUpdateQuantities(false);
      inv.setOrganization(header.getOrganization());
      inv.setWarehouse(header.getWarehouse());
      obdal.save(inv);
      //Ve4rifica que la lista no este vacía

      //Inicializacion de Variables 
      InventoryCountLine invline = null;
      BigDecimal qtyonhand = BigDecimal.ZERO;
      OBCriteria<StorageDetail> sdqty;
      List<StorageDetail> sdList;
      
      if(sizeList.isEmpty()){
        final OBError msg = new OBError();
        msg.setType("Error");
        msg.setTitle("Lista vacía.");
        msg.setMessage("No hay piezas asignadas al documento.");
        bundle.setResult(msg);        
        return;
      }
      
      for(almac_sizechangeline x :sizeList){
        //Busca la cantidad actual en el sistema y si ya ha cambaido lanza un error.
        sdqty = obdal.createCriteria(StorageDetail.class);
        sdqty.add(Restrictions.eq(StorageDetail.PROPERTY_PRODUCT,x.getProduct()));
        sdqty.add(Restrictions.eq(StorageDetail.PROPERTY_ATTRIBUTESETVALUE, x.getAttributeSetValue()));
        sdList = sdqty.list();
        
        invline = OBProvider.getInstance().get(InventoryCountLine.class);
        invline.setOrganization(inv.getOrganization());
        invline.setPhysInventory(inv);
        invline.setLineNo(x.getLineNo());
        invline.setProduct(x.getProduct());
        invline.setStorageBin(x.getStorageBin());
        invline.setQuantityCount(((x.getHeight().divide(new BigDecimal("100"),5,BigDecimal.ROUND_CEILING)).multiply(x.getWidth().divide(new BigDecimal("100"),5,BigDecimal.ROUND_CEILING))));
        //Pone la nueva cantidad en el sistema
        invline.setBookQuantity(sdList.get(0).getQuantityOnHand());
        invline.setUOM(x.getUOM());
        invline.setAttributeSetValue(x.getAttributeSetValue());
        obdal.save(invline);
      }
      //Se envía todo a la bsae de datos
      obdal.flush();
      obdal.refresh(inv);
      //Crea el proceso de Inventario
      InventoryCountProcess process = new InventoryCountProcess();
      //Se agrega como parametro el Id del Inventario
      Map<String,Object> param = bundle.getParams();
      param.put("M_Inventory_ID",(Object)inv.getId());
      bundle.setParams(param);
      
      //Se ejecuta el proceso
      process.execute(bundle);

      
      //Si no hay error continuará normalmente por lo que hay que cambiar de tamaño las piezas
      AttributeSetInstance y = null;
      //El documento fue modificado y ya nodeberia poder modificarse nuevamente
      header.setPosted(true);
      header.setPhysInventory(inv);
      //Cambia el tamaño de las piezas
      pQ = obdal.createCriteria(almac_sizechangeline.class);
      pQ.add(Restrictions.eq(almac_sizechangeline.PROPERTY_ALMACSIZECHANGE,header));
      sizeList = pQ.list();
      for(almac_sizechangeline x :sizeList){
        y = x.getAttributeSetValue();
        y.setAlmacAlto(x.getHeight());
        y.setAlmacAncho(x.getWidth());
        y.setAlmacDescriptionmedidas(y.getAlmacAlto().toString()+" X "+y.getAlmacAncho().toString());
        obdal.save(y);
      }
      obdal.save(header);
      obdal.flush();
      
      // OBError is also used for successful results
      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("Se ha cambiado de tamaño a las piezas.");
      msg.setMessage("Se creó el Documento <"+header.getCommercialName()+" "+header.getCreationDate().toString()+"> en Inventarío Físico");
      bundle.setResult(msg);
      
    } catch (final Exception e) {
      e.printStackTrace(System.err);
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("Error occurred");
      bundle.setResult(msg);
    }
  }
}
