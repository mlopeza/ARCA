/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package com.tegik.idl.module.proc;


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
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.plm.Attribute;
import org.openbravo.model.common.plm.AttributeInstance;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.AttributeValue;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCategory;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.financialmgmt.tax.TaxCategory;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListSchema;
import org.openbravo.model.pricing.pricelist.PriceListSchemeLine;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.module.idljava.proc.IdlServiceJava;

/**
 *
 * @author adrian
 */
public class productosProcess extends IdlServiceJava {

    @Override
    public String getEntityName() {
        return "Productos Tegik";
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter("Organization", Parameter.STRING),
            new Parameter("SearchKey", Parameter.STRING),
            new Parameter("Name", Parameter.STRING),
            new Parameter("Description", Parameter.STRING),
            //new Parameter("UPCEAN", Parameter.STRING),
            new Parameter("ProductCategory", Parameter.STRING),
           // new Parameter("UOM", Parameter.STRING),
            new Parameter("ProductType", Parameter.STRING),
            //new Parameter("Production", Parameter.STRING),
            //new Parameter("BillOfMaterial", Parameter.STRING),
            //new Parameter("Discontinued", Parameter.STRING),
            //new Parameter("CostType", Parameter.STRING),
            //new Parameter("AttributeSet", Parameter.STRING),
            //new Parameter("AttributeValue", Parameter.STRING),
            new Parameter("Stocked", Parameter.STRING),
            new Parameter("Purchase", Parameter.STRING),
            new Parameter("Sale", Parameter.STRING),
	    new Parameter("Width", Parameter.STRING),
            new Parameter("Heigth", Parameter.STRING),
            new Parameter("Depth", Parameter.STRING),
            new Parameter("TaxCategory", Parameter.STRING)};
    }

    @Override
    protected Object[] validateProcess(Validator validator, String... values) throws Exception {
        validator.checkOrganization(values[0]);
        validator.checkNotNull(validator.checkString(values[1], 60), "SearchKey");
        validator.checkNotNull(validator.checkString(values[2], 60), "Name");
        validator.checkString(values[3], 255);
	//validator.checkString(values[4], 30);
        validator.checkNotNull(validator.checkString(values[4], 40, "Category"), "ProductCategory");
        //validator.checkNotNull(validator.checkString(values[6], 32, "UOM"), "UOM");
	validator.checkNotNull(validator.checkString(values[5], 60, "ProductType"), "ProductType");
        //validator.checkNotNull(validator.checkBoolean(values[8], "Production"), "Production");
        //validator.checkNotNull(validator.checkBoolean(values[9], "BillOfMaterials"), "BillOfMaterial");
	//validator.checkNotNull(validator.checkBoolean(values[10], "Discontinued"), "Discontinued");
        //validator.checkString(values[11], 32, "CostType");
	//validator.checkString(values[12], 60, "AttributeSet");
        //validator.checkString(values[13], 60, "AttributeSetValue");
        validator.checkNotNull(validator.checkBoolean(values[6], "Stocked"), "Stocked");
        validator.checkNotNull(validator.checkBoolean(values[7], "Purchase"), "Purchase");
        validator.checkNotNull(validator.checkBoolean(values[8], "Sale"), "Sale");
        //validator.checkBigDecimal(values[9]);
	//validator.checkBigDecimal(values[10]);
        //validator.checkBigDecimal(values[11]);
        validator.checkNotNull(validator.checkString(values[12], 32, "TaxCategory"), "TaxCategory");
        //validator.checkBigDecimal(values[18]);
        //validator.checkBigDecimal(values[19]);
        //validator.checkBigDecimal(values[20]);
        return values;
    }

  @Override
  public BaseOBObject internalProcess(Object... values) throws Exception {

    return createProduct((String) values[0], (String) values[1], (String) values[2],
        (String) values[3], (String) values[4], (String) values[5], (String) values[6],
        (String) values[7], (String) values[8], (String) values[9], (String) values[10],
        (String) values[11], (String) values[12]);
  }

  public BaseOBObject createProduct(final String Organization, final String searchkey,
      final String name, final String description, final String productcategory, final String producttype,
      final String stocked, final String purchase, final String sale, 
      final String width, final String heigth, final String depth,
      final String taxcategory)
      throws Exception {

    // INITIAL AND DEFAULT VALUES

    // Product Category
    // + By default could be "Standard Category"

    // UOM:
    // + UOM EDICode
    // + Standard precision
    // + Costing precision

    // Product type
    // + By default is Item

    // Tax Category
    // + Name

    // Price List
    // + Name for PriceList

    // PRODUCT
    Product productExist = findDALInstance(false, Product.class, new Value("searchKey", searchkey));
    if (productExist != null) {
      throw new OBException(Utility.messageBD(conn, "IDL_PR_EXISTS", vars.getLanguage()) + searchkey);
    }

    String uom = "Unit";

    Product product = OBProvider.getInstance().get(Product.class);
    product.setActive(true);
    product.setOrganization(rowOrganization);
    product.setSearchKey(searchkey);
    product.setName(name);
    product.setDescription(description);
    //product.setUPCEAN(upcean);

    // Search Default Category
    ProductCategory productCategory = findDALInstance(false, ProductCategory.class, new Value(
        "searchKey", productcategory));
    if (productCategory == null) {
      /*
      ProductCategory proCat = OBProvider.getInstance().get(ProductCategory.class);
      proCat.setActive(true);
      proCat.setOrganization(rowOrganization);
      proCat.setName(productcategory);
      proCat.setSearchKey(productcategory);
      proCat.setPlannedMargin(BigDecimal.ZERO);
      OBDal.getInstance().save(proCat);
      OBDal.getInstance().flush();
      product.setProductCategory(proCat);
      */
      //Mensaje de error

      throw new OBException(Utility.messageBD(conn, "No existe la categoría " + productcategory, vars.getLanguage()));

    } else {
      product.setProductCategory(productCategory);
    }

    // Search Default UOM
    UOM unitOfMeasure = findDALInstance(false, UOM.class, new Value("name", uom));
    // Search Default Standard and Costing precision
    //String defStdPrecision = searchDefaultValue("Product", "StandardPrecision", null);
    //String defCostingPrecision = searchDefaultValue("Product", "CostingPrecision", null);

    if (unitOfMeasure == null) {
      /*
      UOM uomCreated = OBProvider.getInstance().get(UOM.class);
      uomCreated.setActive(true);
      uomCreated.setOrganization(DALUtils.findOrganization("0"));
      uomCreated.setName(uom);
      uomCreated.setEDICode(uom.length() > 2 ? uom.substring(0, 2) : uom);
      uomCreated.setStandardPrecision(Parameter.LONG.parse(defStdPrecision));
      uomCreated.setCostingPrecision(Parameter.LONG.parse(defCostingPrecision));
      OBDal.getInstance().save(uomCreated);
      OBDal.getInstance().flush();
      product.setUOM(uomCreated);
      */
      //Mensaje de error
      throw new OBException(Utility.messageBD(conn, "No existe la unidad de medida " + uom, vars.getLanguage()));

    } else {
      product.setUOM(unitOfMeasure);
    }

    product.setProductType(getReferenceValue("M_Product_ProductType", producttype));

    // Search Default value for stocked, purchase and sale
    product.setStocked(Parameter.BOOLEAN.parse(stocked));
    product.setPurchase(Parameter.BOOLEAN.parse(purchase));
    product.setSale(Parameter.BOOLEAN.parse(sale));
    /*
    product.setProduction(Parameter.BOOLEAN.parse(production));
    product.setBillOfMaterials(Parameter.BOOLEAN.parse(billofmaterial));
    product.setDiscontinued(Parameter.BOOLEAN.parse(discontinued));
    */
    product.setCostType("AV");
    //product.setShelfWidth(Parameter.BIGDECIMAL.parse(width));
    //product.setShelfHeight(Parameter.BIGDECIMAL.parse(heigth));
    //product.setShelfDepth(Parameter.BIGDECIMAL.parse(depth));
    

    // Search Default value for Tax Category
    TaxCategory taxCategory = findDALInstance(false, TaxCategory.class, new Value("name",
        taxcategory));
    if (taxCategory == null) {
      /*
      TaxCategory taxCatCreated = OBProvider.getInstance().get(TaxCategory.class);
      taxCatCreated.setActive(true);
      taxCatCreated.setOrganization(rowOrganization);
      taxCatCreated.setName(taxcategory);
      taxCatCreated.setDescription("Created using default values");
      OBDal.getInstance().save(taxCatCreated);
      OBDal.getInstance().flush();
      product.setTaxCategory(taxCatCreated);
      */
      //Mensaje de error
      throw new OBException(Utility.messageBD(conn,  "No existe la categoría de impuestos " + taxcategory, vars.getLanguage()));

    } else {
      product.setTaxCategory(taxCategory);
    }

    OBDal.getInstance().save(product);
    OBDal.getInstance().flush();

    // End process
    OBDal.getInstance().commitAndClose();

    return product;
  }
}
