/*
 *  Copyright 2010 BigData.mx
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.tegik.facelectr.ad_actionButton;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.io.IOException;



import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_actionButton.ActionButtonDefaultData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.dal.core.OBContext;

import mx.bigdata.sat.cfdi.v32.schema.Adapter1;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Addenda;
import mx.bigdata.sat.cfdi.v32.schema.ObjectFactory;
import mx.bigdata.sat.cfdi.v32.schema.TUbicacion;
import mx.bigdata.sat.cfdi.v32.schema.TUbicacionFiscal;
import mx.bigdata.sat.cfdi.v32.schema.TInformacionAduanera;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Conceptos;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Emisor;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Impuestos;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Receptor;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Conceptos.Concepto;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Impuestos.Traslados;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Impuestos.Traslados.Traslado;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Impuestos.Retenciones;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Impuestos.Retenciones.Retencion;
import mx.bigdata.sat.cfdi.v32.schema.TimbreFiscalDigital;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Emisor.RegimenFiscal;

import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceDiscount;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.invoice.InvoiceTax;

import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "emisor",
    "receptor",
    "conceptos",
    "impuestos",
    "complemento",
    "addenda"
})
@XmlRootElement(name = "creadorFacturas")


public final class creadorFacturas {

    private static final Logger log = Logger.getLogger(creadorFacturas.class);

    

    
    public static Comprobante createComprobante(String strInvoiceId, String tipoDoc) throws Exception {
    Invoice factura = OBDal.getInstance().get(Invoice.class,strInvoiceId);
    ObjectFactory of = new ObjectFactory();
    Comprobante comp = of.createComprobante();
    
    //DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date = new Date();

    
    comp.setLugarExpedicion(factura.getOrganization().getOrganizationInformationList().get(0).getTdirmDirfiscal().getCityName()+", "+factura.getOrganization().getOrganizationInformationList().get(0).getTdirmDirfiscal().getRegion().getName());
    
    comp.setVersion("3.2");
    //comp.setNumCtaPago("4567");  //Campo opcional
    //comp.setSerie("Pendiente"); //Como enviar facturas con un numero de serie.
    comp.setFolio(factura.getDocumentNo());
    comp.setFecha(date);
    comp.setFechaFolioFiscalOrig(date);
    if(factura.getFetFormadepago().equals("1")){
    comp.setFormaDePago("Pago en una sola exhibición");
    }else if(factura.getFetFormadepago().equals("2")){comp.setFormaDePago("Parcialidad "+factura.getFetParcialidad()+" de "+factura.getFetParcialidadtotal());}

    String referencia = factura.getBusinessPartner().getReferenceNo();
    if (referencia != null && !referencia.equals("")) {
	comp.setMetodoDePago(factura.getPaymentMethod().getName() + " - " + referencia);
    }else {
	comp.setMetodoDePago(factura.getPaymentMethod().getName());
    }
    //comp.setCondicionesDePago(factura.getPaymentTerms().getName()); //No es obligatorio
    comp.setSubTotal(factura.getSummedLineAmount());
    comp.setTotal(factura.getGrandTotalAmount());
    if(tipoDoc=="N")
    comp.setTipoDeComprobante("egreso");
    else comp.setTipoDeComprobante("ingreso");
    comp.setMoneda(factura.getCurrency().getISOCode());

//Cambiar el tipocambio ponerle un string direto
     final OBCriteria<ConversionRate> rateList = OBDal.getInstance().createCriteria(ConversionRate.class);
	      rateList.add(Expression.eq(ConversionRate.PROPERTY_TOCURRENCY, OBDal.getInstance().get(Currency.class,"130")));
	      rateList.add(Expression.eq(ConversionRate.PROPERTY_CURRENCY, factura.getCurrency()));
	      rateList.add(Expression.eq(ConversionRate.PROPERTY_VALIDFROMDATE, date));
	      rateList.add(Expression.eq(ConversionRate.PROPERTY_VALIDTODATE, date));

    for (ConversionRate CurrencyRate : rateList.list()) {
    comp.setTipoCambio(CurrencyRate.getMultipleRateBy().toString());
    }



    //Saca una lista de todos los descuentos y los suma.
    for (InvoiceDiscount descuento : factura.getInvoiceDiscountList()) {
	//descuento.
    }
OBContext.setAdminMode(true);

    comp.setEmisor(createEmisor(of, factura));
    comp.setReceptor(createReceptor(of, factura));
    comp.setConceptos(createConceptos(of, factura));
    comp.setImpuestos(createImpuestos(of, factura));

    OBContext.restorePreviousMode();

    return comp;
  }



  
  private static Emisor createEmisor(ObjectFactory of, Invoice factura) {
    Emisor emisor = of.createComprobanteEmisor();
    emisor.setNombre(factura.getOrganization().getSocialName());
    emisor.setRfc(factura.getOrganization().getOrganizationInformationList().get(0).getTaxID());
    TUbicacionFiscal uf = of.createTUbicacionFiscal();
    uf.setCalle(factura.getOrganization().getOrganizationInformationList().get(0).getTdirmDirfiscal().getAddressLine1());	
    uf.setCodigoPostal(factura.getOrganization().getOrganizationInformationList().get(0).getTdirmDirfiscal().getPostalCode());	
    uf.setColonia(factura.getOrganization().getOrganizationInformationList().get(0).getTdirmDirfiscal().getAddressLine2()); 
    uf.setEstado(factura.getOrganization().getOrganizationInformationList().get(0).getTdirmDirfiscal().getRegion().getName()); 
    uf.setMunicipio(factura.getOrganization().getOrganizationInformationList().get(0).getTdirmDirfiscal().getCityName()); 
    uf.setNoExterior(factura.getOrganization().getOrganizationInformationList().get(0).getTdirmDirfiscal().getTdirmNumex()); 
    uf.setNoInterior(factura.getOrganization().getOrganizationInformationList().get(0).getTdirmDirfiscal().getTdirmNumin()); 
    uf.setPais(factura.getOrganization().getOrganizationInformationList().get(0).getTdirmDirfiscal().getCountry().getName());     
    emisor.setDomicilioFiscal(uf);

 /*   TUbicacion u = of.createTUbicacion();
    u.setCalle(factura.getOrganization().getOrganizationInformationList().get(0).getLocationAddress().getAddressLine1());	
    u.setCodigoPostal(factura.getOrganization().getOrganizationInformationList().get(0).getLocationAddress().getPostalCode());  
    u.setColonia(factura.getOrganization().getOrganizationInformationList().get(0).getLocationAddress().getAddressLine2()); 
    u.setEstado(factura.getOrganization().getOrganizationInformationList().get(0).getLocationAddress().getRegion().getName()); 
    u.setMunicipio(factura.getOrganization().getOrganizationInformationList().get(0).getLocationAddress().getCityName()); 
    u.setNoExterior(factura.getOrganization().getOrganizationInformationList().get(0).getLocationAddress().getTdirmNumex()); 
    u.setNoInterior(factura.getOrganization().getOrganizationInformationList().get(0).getLocationAddress().getTdirmNumin()); 
    u.setPais(factura.getOrganization().getOrganizationInformationList().get(0).getLocationAddress().getCountry().getName());     
    emisor.setExpedidoEn(u); 
  */  

    Comprobante.Emisor.RegimenFiscal re = of.createComprobanteEmisorRegimenFiscal();
    if(factura.getOrganization().getOrganizationInformationList().get(0).isFetPersonamoral()){
    re.setRegimen("Persona Moral Regimen General");
    }
    else { re.setRegimen("PERSONA FISICA");
    }
    emisor.getRegimenFiscal().add(re);

    



    return emisor;
  }

  private static Receptor createReceptor(ObjectFactory of, Invoice factura) {
    Receptor receptor = of.createComprobanteReceptor();
    receptor.setNombre(factura.getBusinessPartner().getName2());
    receptor.setRfc(factura.getBusinessPartner().getTaxID());
    TUbicacion uf = of.createTUbicacion();
    uf.setCalle(factura.getPartnerAddress().getLocationAddress().getAddressLine1());
    uf.setCodigoPostal(factura.getPartnerAddress().getLocationAddress().getPostalCode());
    uf.setColonia(factura.getPartnerAddress().getLocationAddress().getAddressLine2()); 
    uf.setEstado(factura.getPartnerAddress().getLocationAddress().getRegion().getName()); 
    uf.setMunicipio(factura.getPartnerAddress().getLocationAddress().getCityName()); 
    uf.setNoExterior(factura.getPartnerAddress().getLocationAddress().getTdirmNumex()); 
    uf.setNoInterior(factura.getPartnerAddress().getLocationAddress().getTdirmNumin()); 
    uf.setPais(factura.getPartnerAddress().getLocationAddress().getCountry().getName()); 
    receptor.setDomicilio(uf);
    return receptor;
  }

  private static Conceptos createConceptos(ObjectFactory of, Invoice factura) {
    Conceptos cps = of.createComprobanteConceptos();
    List<Concepto> listaConceptos = cps.getConcepto(); 
    for (InvoiceLine linea : factura.getInvoiceLineList()) {
	 if (linea.getProduct() != null){
	      Concepto c = of.createComprobanteConceptosConcepto();
	      c.setUnidad(linea.getUOM().getName());
	      c.setImporte(linea.getLineNetAmount());
	      c.setCantidad(linea.getInvoicedQuantity());
	      /*if(linea.isVentasIsmascara())
	      c.setDescripcion(linea.getVentasMascara());
	      else c.setDescripcion(linea.getProduct().getName());*/
		c.setDescripcion(linea.getProduct().getName());	 
		c.setValorUnitario(linea.getUnitPrice());
	      //c.setNoIdentificacion(linea.getProduct().getValue());
	      listaConceptos.add(c);
	 }
    }
    return cps;
  }

  private static Impuestos createImpuestos(ObjectFactory of, Invoice factura) {
    Impuestos imps = of.createComprobanteImpuestos();
    Traslados trs = of.createComprobanteImpuestosTraslados();
    List<Traslado> listaImpuestos = trs.getTraslado();

    for (InvoiceTax lineaImpuesto : factura.getInvoiceTaxList()) {
	 if (lineaImpuesto.getTaxAmount().compareTo(new BigDecimal (0.0)) > 0)
	 {
	    Traslado t1 = of.createComprobanteImpuestosTrasladosTraslado();
	    t1.setImporte(lineaImpuesto.getTaxAmount());
	    t1.setImpuesto(lineaImpuesto.getTax().getName());
	    t1.setTasa(lineaImpuesto.getTax().getRate());
	    listaImpuestos.add(t1);
	 }
    }

    Retenciones retenciones = of.createComprobanteImpuestosRetenciones();
    List<Retencion> listaRetenciones = retenciones.getRetencion();
    for (InvoiceTax lineaImpuesto : factura.getInvoiceTaxList()) {
	 if (lineaImpuesto.getTaxAmount().compareTo(new BigDecimal (0.0)) < 0)
	 {
	    Retencion r1 = of.createComprobanteImpuestosRetencionesRetencion();
	    r1.setImporte(lineaImpuesto.getTaxAmount());
	    r1.setImpuesto(lineaImpuesto.getTax().getName());
	    listaRetenciones.add(r1);
	 }
    }

    imps.setTraslados(trs);
   // imps.setRetenciones(retenciones);
    return imps;
  }


}