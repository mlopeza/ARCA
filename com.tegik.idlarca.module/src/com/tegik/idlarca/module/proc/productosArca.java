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
import org.openbravo.model.common.geography.Country;

/**
 *
 * @author Carlos Salinas
 */
public class productosArca extends IdlServiceJava {

    @Override
    public String getEntityName() {
        return "Productos Arca";
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter("SearchKey", Parameter.STRING),
	    new Parameter("tileoslab", Parameter.STRING),
	    new Parameter("uso", Parameter.STRING),
	    new Parameter("areauso", Parameter.STRING),
	    new Parameter("color", Parameter.STRING),
	    new Parameter("tipopiedra", Parameter.STRING),
	    new Parameter("paisorigen", Parameter.STRING),
	    new Parameter("acabado", Parameter.STRING),
	    new Parameter("esoutlet", Parameter.STRING)};

    }

    @Override
    protected Object[] validateProcess(Validator validator, String... values) throws Exception {
	validator.checkNotNull(validator.checkString(values[0], 40), "SearchKey");
	validator.checkBoolean(validator.checkString(values[8], 1));
        return values;
    }

  @Override
  public BaseOBObject internalProcess(Object... values) throws Exception {
    return actualizaProductoArca((String) values[0], (String) values[1], (String) values[2], (String) values[3], (String) values[4], (String) values[5], (String) values[6], (String) values[7], (String) values[8]);
  }

  public BaseOBObject actualizaProductoArca(final String searchkey, 
    final String tileoslab, 
    final String uso, 
    final String areauso, 
    final String color, 
    final String tipopiedra, 
    final String paisorigen, 
    final String acabado, 
    final String esoutlet) throws Exception {

    Product productExist = findDALInstance(false, Product.class, new Value("searchKey", searchkey));
    if (productExist == null) {
      throw new OBException(Utility.messageBD(conn, "El producto no existe -- ", vars.getLanguage()) + searchkey);
    }
    else
    {
      productExist.setDmprodTileoslab(getReferenceValue("Tile o Slab", tileoslab));

     if (productExist.getDmprodTileoslab() == null) {
	  throw new OBException(Utility.messageBD(conn, "El producto no tiene definido si es Tile o Slab -- ", vars.getLanguage()) + tileoslab);
      }

      if (uso != "") {
	dmproduso usoexists = findDALInstance(false, dmproduso.class, new Value("name", uso));
	if (usoexists == null) {
	    throw new OBException(Utility.messageBD(conn, "El uso del producto no existe -- ", vars.getLanguage()) + uso);
	}else
	{
	    productExist.setDmprodUso(usoexists);
	}
      }

      if (areauso != "") {
	dmprodareauso areausoexists = findDALInstance(false, dmprodareauso.class, new Value("name", areauso));
	if (areausoexists == null) {
	    throw new OBException(Utility.messageBD(conn, "El área de uso del producto no existe -- ", vars.getLanguage()) + areauso);
	}else
	{
	    productExist.setDmprodAreauso(areausoexists);
	}
      }

      if (color != "") {
	dmprodcolor colorexists = findDALInstance(false, dmprodcolor.class, new Value("name", color));
	if (colorexists == null) {
	    throw new OBException(Utility.messageBD(conn, "El color del producto no existe -- ", vars.getLanguage()) + color);
	}else
	{
	    productExist.setDmprodColor(colorexists);
	}
      }

      if (tipopiedra != "") {
	dmprodtipopiedra tipopiedraexists = findDALInstance(false, dmprodtipopiedra.class, new Value("name", tipopiedra));
	if (tipopiedraexists == null) {
	    throw new OBException(Utility.messageBD(conn, "El tipo de piedra del producto no existe -- ", vars.getLanguage()) + tipopiedra);
	}else
	{
	    productExist.setDmprodTipopiedra(tipopiedraexists);
	}
      }

      if (paisorigen != "") {
	Country paisexists = findDALInstance(false, Country.class, new Value("iSOCountryCode", paisorigen));
	if (paisexists == null) {
	    throw new OBException(Utility.messageBD(conn, "El país de origen del producto no existe -- ", vars.getLanguage()) + paisorigen);
	}else
	{
	    productExist.setDmprodCountry(paisexists);
	}
      }

      if (acabado != "") {
	productExist.setDmprodAcabado(getReferenceValue("Acabado", acabado));
	 if (productExist.getDmprodAcabado() == null && acabado != null) {
	    throw new OBException(Utility.messageBD(conn, "El acabado del producto no existe -- ", vars.getLanguage()) + acabado);
	 }
      }

      productExist.setDmprodIsoutlet(Parameter.BOOLEAN.parse(esoutlet));

      productExist.setDmprodWidth(productExist.getShelfWidth());
      productExist.setDmprodHeight(productExist.getShelfHeight());

      if (tileoslab.equals("Tile")) {
	AttributeSet attset = findDALInstance(false, AttributeSet.class, new Value("name", "TILE"));
	if (attset == null) {
	    throw new OBException(Utility.messageBD(conn, "No existe un atributo llamado TILE para asignarle a este producto", vars.getLanguage()));
	}else
	{
	  productExist.setAttributeSet(attset);
	}
      }

      if (tileoslab.equals("Slab")) {
	AttributeSet attset = findDALInstance(false, AttributeSet.class, new Value("name", "SLAB"));
	if (attset == null) {
	    throw new OBException(Utility.messageBD(conn, "No existe un atributo llamado SLAB para asignarle a este producto", vars.getLanguage()));
	}else
	{
	    productExist.setAttributeSet(attset);
	}
      }
      

      OBDal.getInstance().save(productExist);
      OBDal.getInstance().flush();
      
    }



    // End process
    OBDal.getInstance().commitAndClose();

    return productExist;
  }
}