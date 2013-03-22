/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package com.tegik.idlarca.module.proc;


import org.openbravo.idl.proc.Parameter;
import org.openbravo.idl.proc.Validator;
import java.math.BigDecimal;
import java.util.Calendar;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.idl.proc.DALUtils;
import org.openbravo.idl.proc.Value;
import org.openbravo.model.common.plm.ProductCategory;
import org.openbravo.module.idljava.proc.IdlServiceJava;
import com.tegik.dmprod.module.data.dmprodcolor;
import com.tegik.dmprod.module.data.dmproduso;
import com.tegik.dmprod.module.data.dmprodareauso;
import com.tegik.dmprod.module.data.dmprodtipopiedra;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.geography.Country;

/**
 *
 * @author Carlos Salinas
 */
public class fixMedidasFechas extends IdlServiceJava {

    @Override
    public String getEntityName() {
        return "Productos Arca";
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter("SearchKey", Parameter.STRING),
	    new Parameter("alto", Parameter.STRING),
	    new Parameter("ancho", Parameter.STRING),
	    new Parameter("fecha", Parameter.STRING)};

    }

    @Override
    protected Object[] validateProcess(Validator validator, String... values) throws Exception {
	validator.checkNotNull(validator.checkString(values[0], 40), "SearchKey");
	validator.checkBigDecimal(values[1]);
	validator.checkBigDecimal(values[2]);
        return values;
    }

  @Override
  public BaseOBObject internalProcess(Object... values) throws Exception {
    return actualizaProductoArca((String) values[0], (String) values[1], (String) values[2], (String) values[3]);
  }

  public BaseOBObject actualizaProductoArca(final String searchkey, 
    final String alto, 
    final String ancho, 
    final String fecha) throws Exception {

    OBContext.setAdminMode(true);

    AttributeSetInstance asiExist = findDALInstance(false, AttributeSetInstance.class, new Value("description", searchkey));
    if (asiExist == null) {
      throw new OBException(Utility.messageBD(conn, "El set de atributos no existe -- ", vars.getLanguage()) + searchkey);
    }
    else
    {
	asiExist.setAlmacFechaentrada(Parameter.DATE.parse(fecha));
	asiExist.setAlmacAlto(Parameter.BIGDECIMAL.parse(alto));
	asiExist.setAlmacAncho(Parameter.BIGDECIMAL.parse(ancho));
      

      OBDal.getInstance().save(asiExist);
      OBDal.getInstance().flush();
      
    }



    // End process
    OBDal.getInstance().commitAndClose();

    OBContext.restorePreviousMode();

    return asiExist;
  }
}