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
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;


/**
 *
 * @author adrian
 */
public class FacturasProcess extends IdlServiceJava {

    @Override
    public String getEntityName() {
        return "Facturas Tegik";
    }

    @Override
    public Parameter[] getParameters() {
//CLIENTE NUMERODEPEDIDO/REFERENCIA FOLIO FECHADEFACTURA TERMINO DE PAGO SALDOPENDIENTE MONEDA
        return new Parameter[] {
            new Parameter("Cliente", Parameter.STRING),
            new Parameter("Referencia", Parameter.STRING),
            new Parameter("Folio", Parameter.STRING),
            new Parameter("Fecha", Parameter.STRING),
	    new Parameter("Termino", Parameter.STRING),
	    new Parameter("Saldo", Parameter.STRING),
	    new Parameter("Moneda", Parameter.STRING),
	    new Parameter("ListaPrecios", Parameter.STRING),
	    new Parameter("Venta", Parameter.STRING)};
    }

    @Override
    protected Object[] validateProcess(Validator validator, String... values) throws Exception {
        validator.checkNotNull(validator.checkString(values[0], 40), "BusinessPartner");
	//validator.checkNotNull(validator.checkString(values[1], 40), "Product");
	//validator.checkBigDecimal(values[2]);
	//validator.checkBigDecimal(values[3]);
	//validator.checkNotNull(validator.checkString(values[4], 32), "Order");
        return values;
    }

  @Override
  public BaseOBObject internalProcess(Object... values) throws Exception {

    return createFactura((String) values[0], (String) values[1], (String) values[2],
        (String) values[3], (String) values[4],(String) values[5],(String) values[6],(String) values[7],(String) values[8]);
  }

  public BaseOBObject createFactura(final String Cliente, final String Referencia,
                     final String Folio, final String Fecha, final String Termino, 
                     final String Saldo, final String Moneda,final String ListaPrecios, final String venta)
      throws Exception {

    // INITIAL AND DEFAULT VALUES

    // Crea la factura en blanco.
    Invoice facturaObject = OBProvider.getInstance().get(Invoice.class);

    facturaObject.setActive(Parameter.BOOLEAN.parse("Y"));
    facturaObject.setSalesTransaction(Parameter.BOOLEAN.parse(venta));
    
    Product productoObject = findDALInstance(false, Product.class, new Value("searchKey", "SALDOINICIAL"));
    if (productoObject == null) {
      throw new OBException(Utility.messageBD(conn, "El producto SALDOINICIAL no existe -- " , vars.getLanguage()) );
    }


   // Organization orgObject = findDALInstance(false, Organization.class, new Value("Name", "Monterrey"));
   // if (orgObject == null) {
   //   throw new OBException(Utility.messageBD(conn, "La organizacion Monterrey no existe." , vars.getLanguage()) );
   // }
   facturaObject.setOrganization(productoObject.getOrganization());

    if (venta == "Y"){
      DocumentType dtObject = findDALInstance(false, DocumentType.class, new Value("name", "AR Invoice"));
      if (dtObject == null) {
	throw new OBException(Utility.messageBD(conn, "El documento AR Invoice no existe. " , vars.getLanguage()) );
      }
      facturaObject.setDocumentType(dtObject);
      facturaObject.setTransactionDocument(dtObject);
    } else {
      DocumentType dtObject = findDALInstance(false, DocumentType.class, new Value("name", "AP Invoice"));
      if (dtObject == null) {
	throw new OBException(Utility.messageBD(conn, "El documento AP Invoice no existe. " , vars.getLanguage()) );
      }
      facturaObject.setDocumentType(dtObject);
      facturaObject.setTransactionDocument(dtObject);
    }
    
    facturaObject.setDocumentNo(Folio);

    facturaObject.setInvoiceDate(Parameter.DATE.parse(Fecha));

    facturaObject.setAccountingDate(Parameter.DATE.parse(Fecha));

    BusinessPartner bpObject = findDALInstance(false, BusinessPartner.class, new Value("searchKey", Cliente));
    if (bpObject == null) {
      throw new OBException(Utility.messageBD(conn, "El cliente no existe -- " , vars.getLanguage()) + Cliente);
    }
    facturaObject.setBusinessPartner(bpObject);

    //FALTA DIRECCION
    for (Location bplocation : bpObject.getBusinessPartnerLocationList()) {
	  facturaObject.setPartnerAddress(bplocation);
	}

    PaymentTerm ptObject = findDALInstance(false, PaymentTerm.class, new Value("searchKey", Termino));
    if (ptObject == null) {
      throw new OBException(Utility.messageBD(conn, "La Condición de pago no existe -- " , vars.getLanguage()) + Termino );
    }
    facturaObject.setPaymentTerms(ptObject);


    FIN_PaymentMethod pmObject = findDALInstance(false, FIN_PaymentMethod.class, new Value("name", "TRANSFERENCIA"));
    if (pmObject == null) {
      throw new OBException(Utility.messageBD(conn, "El método de pago TRANSFERENCIA no existe " , vars.getLanguage()));
    }
    facturaObject.setPaymentMethod(pmObject);

    Currency currObject = findDALInstance(false, Currency.class, new Value("iSOCode", Moneda));
    if (currObject == null) {
        throw new OBException(Utility.messageBD(conn, "La moneda no existe -- " , vars.getLanguage()) + Moneda);
    }
    facturaObject.setPaymentMethod(pmObject);

    PriceList plObject = findDALInstance(false, PriceList.class, new Value("name", ListaPrecios));
    if (plObject == null) {
       throw new OBException(Utility.messageBD(conn, "La Lista de Precios no existe -- " , vars.getLanguage()) + ListaPrecios);
    }

    facturaObject.setPriceList(plObject);

    facturaObject.setCurrency(plObject.getCurrency());
    facturaObject.setPaymentMethod(pmObject);
    facturaObject.setOrderReference(Referencia);

// //     bpObject.getBusinessPartnerLocationList

    
    OBDal.getInstance().save(facturaObject);
    OBDal.getInstance().flush();

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Crea la linea de factura en blanco.
    InvoiceLine facturalineObject = OBProvider.getInstance().get(InvoiceLine.class);

    facturalineObject.setActive(Parameter.BOOLEAN.parse("Y"));
    
    Long numLinea = new Long(10);
    facturalineObject.setLineNo(numLinea);
    facturalineObject.setOrganization(facturaObject.getOrganization());
    facturalineObject.setInvoice(facturaObject);


   // Product productoObject = findDALInstance(false, Product.class, new Value("searchKey", "SALDOINICIAL"));
    //if (productoObject == null) {
     // throw new OBException(Utility.messageBD(conn, "El producto SALDOINICIAL no existe -- " , vars.getLanguage()) );
   // }

    facturalineObject.setProduct(productoObject);
    facturalineObject.setUOM(productoObject.getUOM());

    facturalineObject.setUnitPrice(Parameter.BIGDECIMAL.parse(Saldo));
    facturalineObject.setInvoicedQuantity(Parameter.BIGDECIMAL.parse("1.00"));

    facturalineObject.setListPrice(facturalineObject.getUnitPrice());
    facturalineObject.setPriceLimit(facturalineObject.getUnitPrice());
    facturalineObject.setStandardPrice(facturalineObject.getUnitPrice());

    facturalineObject.setLineNetAmount(facturalineObject.getUnitPrice());
    facturalineObject.setTaxableAmount(facturalineObject.getUnitPrice());
    
    TaxRate taxObject = findDALInstance(false, TaxRate.class, new Value("name", "IVA 16%"));

     if (taxObject == null) {
      throw new OBException(Utility.messageBD(conn, "No existe un impuesto -- " , vars.getLanguage()) + "IVA 16%");
    }

    facturalineObject.setTax(taxObject);





    OBDal.getInstance().save(facturalineObject);
    OBDal.getInstance().flush();

    // End process
    OBDal.getInstance().commitAndClose();

    return facturalineObject;
  }
}
