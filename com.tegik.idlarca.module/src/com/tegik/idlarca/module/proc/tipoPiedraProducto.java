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
import com.tegik.dmprod.module.data.dmprodtipopiedra;

/**
 *
 * @author Carlos Salinas
 */
public class tipoPiedraProducto extends IdlServiceJava {

    @Override
    public String getEntityName() {
        return "Tipo de Piedra del Producto";
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter("Organization", Parameter.STRING),
            new Parameter("SearchKey", Parameter.STRING)};
    }

    @Override
    protected Object[] validateProcess(Validator validator, String... values) throws Exception {
        validator.checkOrganization(values[0]);
        validator.checkNotNull(validator.checkString(values[1], 40), "SearchKey");
        return values;
    }

  @Override
  public BaseOBObject internalProcess(Object... values) throws Exception {
    return crearTipoPiedraProducto((String) values[0], (String) values[1]);
  }

  public BaseOBObject crearTipoPiedraProducto(final String Organization, final String searchkey) throws Exception {

    dmprodtipopiedra tipopiedraexists = findDALInstance(false, dmprodtipopiedra.class, new Value("name", searchkey));

    dmprodtipopiedra tipopiedra = OBProvider.getInstance().get(dmprodtipopiedra.class);

    if (tipopiedraexists == null) {
      tipopiedra.setActive(true);
      tipopiedra.setOrganization(rowOrganization);
      tipopiedra.setName(searchkey);
      //usoprod.setSearchKey(searchkey);
      OBDal.getInstance().save(tipopiedra);
      OBDal.getInstance().flush();
      
    }
    else
    {
      throw new OBException(Utility.messageBD(conn, "El tipo de piedra del producto ya existe:", vars.getLanguage()) + searchkey);
    }
    
    // End process
    OBDal.getInstance().commitAndClose();

    return tipopiedra;
  }
}