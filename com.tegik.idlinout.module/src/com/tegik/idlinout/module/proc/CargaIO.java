/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */


package com.tegik.idlinout.module.proc;

//Importaciones
import com.tegik.idlinout.module.proc.Levenshtein;
import org.openbravo.idl.proc.Parameter;
import org.openbravo.idl.proc.Validator;
import org.openbravo.idl.proc.IdlService;
import au.com.bytecode.opencsv.CSVReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Date;
import java.util.List;
import java.util.Hashtable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.hibernate.criterion.*;
import java.lang.String;
import java.sql.Timestamp;
import java.text.Normalizer;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.idl.proc.DALUtils;
import org.openbravo.idl.proc.Value;
import org.openbravo.dal.*;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.core.SessionHandler;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import java.sql.Connection;

//Tablas de Entradas y Salidas
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import com.tegik.almac.module.data.almacdistrib;
import com.tegik.dmprod.module.data.dmprodcolor;
//Organizacion
import org.openbravo.model.common.enterprise.Organization;
//Producto
import org.openbravo.model.common.plm.Product;
//Locator
import org.openbravo.model.common.enterprise.Locator;
//Attribute Instance
import org.openbravo.model.common.plm.AttributeSetInstance;
//Attribute Set
import org.openbravo.model.common.plm.AttributeSet;
//Contenedores
import com.tegik.compra.module.data.compracontenedor;
//Moneda
import org.openbravo.model.common.currency.Currency;
//Pais
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.CountryTrl;
import org.openbravo.model.common.geography.City;

//Catgeoria del Producto
import org.openbravo.model.common.plm.ProductCategory;

//Campos de Producto
import org.openbravo.model.financialmgmt.tax.TaxCategory;
import com.tegik.almac.module.data.almacCatPiedra;
import com.tegik.almac.module.data.almacConexProd;
import com.tegik.almac.module.data.almacConsTon;
import com.tegik.almac.module.data.almacConsVeta;
import com.tegik.almac.module.data.almacDispProd;
import com.tegik.almac.module.data.almacEstilo;
import com.tegik.almac.module.data.almacPuertos;
import com.tegik.almac.module.data.almacPuertosCon;
import com.tegik.almac.module.data.almacQProduction;
import com.tegik.almac.module.data.almacSegRecom;
import com.tegik.almac.module.data.almacTipoMedida;
import com.tegik.almac.module.data.almacUsoRecom;
import com.tegik.almac.module.data.almac_aplicacion;
import com.tegik.dmprod.module.data.dmprodareauso;
import com.tegik.almac.module.data.almacCalidadProducto;
import com.tegik.dmprod.module.data.dmprodtipopiedra;
//Business Partner
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;

//Tipos de documento
import org.openbravo.model.common.enterprise.DocumentType;

//Almacen
import org.openbravo.model.common.enterprise.Warehouse;

//Unidades de medida
import org.openbravo.model.common.uom.UOM;

//Procesos para completar
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.ConnectionProviderImpl;
import org.openbravo.model.ad.ui.Process;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Connection;
import org.openbravo.service.db.DalConnectionProvider;
import java.sql.PreparedStatement;
import java.sql.ResultSet;



public class CargaIO extends IdlService {

	//Variables y Estructuras usadas
	private Hashtable<String,Product> pHash = new Hashtable();
	private Hashtable<String,ShipmentInOut> headerHash = new Hashtable();
	private Hashtable<String,compracontenedor> containerHash = new Hashtable();
	private Hashtable<List<Object>,ShipmentInOutLine> linesHash = new Hashtable();
	private Date dTemp;
	private AttributeSet tile,slab; 
	private SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy");
	private BusinessPartner arcamexico;
	private Location bpLocation;
	private Organization org;
	private DocumentType dt;
	private Warehouse wh;
	private UOM uom;
	private static final Logger log = Logger.getLogger(CargaIO.class);
	private OBDal obdal = OBDal.getInstance();
	private boolean error = false;
	public String getEntityName() {
		return "Carga las lineas de entradas y salidas.";
	}

	@Override
		protected boolean executeImport(String filename, boolean insert) throws Exception {
			//OBContext old = OBContext.getOBContext();
			//OBContext.setOBContext("0");
			CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"),
					',', '\"', '\\', 0, false, true);

			String[] nextLine;

			// Check header
			nextLine = reader.readNext();
			if (nextLine == null) {
				throw new OBException(Utility.messageBD(conn, "IDLJAVA_HEADER_MISSING", vars.getLanguage()));
			}
			Parameter[] parameters = getParameters();
			if (parameters.length > nextLine.length) {
				throw new OBException(Utility.messageBD(conn, "IDLJAVA_HEADER_BAD_LENGTH", vars.getLanguage()));
			}

			Validator validator;

			while ((nextLine = reader.readNext()) != null) {

				if (nextLine.length > 1 || nextLine[0].length() > 0) {
					// It is not an empty line

					// Validate types
					if (parameters.length > nextLine.length) {
						throw new OBException(Utility.messageBD(conn, "IDLJAVA_LINE_BAD_LENGTH", vars.getLanguage()));
					}

					validator = getValidator(getEntityName());
					Object[] result = validateProcess(validator, nextLine);
					if ("0".equals(validator.getErrorCode())) {
						finishRecordProcess(result);
					} else {
						OBDal.getInstance().rollbackAndClose();
						logRecordError(validator.getErrorMessage(), result);
						error = true;
					}
				}

			}	
			obdal.getInstance().flush();
			//Llama el proceso de completado para las cabeceras
			// Y limpia el hash para liberar memoria
                        pHash.clear();
                        containerHash.clear();
                        linesHash.clear();
			completaCabeceras();
			headerHash.clear();
			return true;
		}



	@Override
		public Parameter[] getParameters() {
			return new Parameter[] {
				new Parameter("PRODUCTO - NOMBRE CONCATENADO", Parameter.STRING),		//0 
				    new Parameter("SKU Arca", Parameter.STRING),				//1
				    new Parameter("SKU Actual Arca DF",Parameter.STRING),			//2
				    new Parameter("Nombre Comercial Material",Parameter.STRING),		//3
				    new Parameter("Nombre Origen Material",Parameter.STRING),			//4
				    new Parameter("Categoria",Parameter.STRING),				//5
				    new Parameter("# Contenedor",Parameter.STRING),				//6  Antes 13
				    new Parameter("# Pedimento",Parameter.STRING),				//7  Antes 14
				    new Parameter("# Atado o Guacal",Parameter.STRING),				//8
				    new Parameter("#Lamina o #Guacal",Parameter.STRING),			//9  Antes 6
				    new Parameter("Cantidad de Piezas", Parameter.STRING),  			//10 Antes 9
				    new Parameter("M2", Parameter.STRING), 				 	//11 Antes 10
				    new Parameter("Unidad de medida", Parameter.STRING),			//12
				    new Parameter("Tipo de Piedra", Parameter.STRING),				//13
				    new Parameter("Formato", Parameter.STRING),					//14  Antes 2
				    new Parameter("Acabado", Parameter.STRING),					//15
				    new Parameter("Categoria Piedra", Parameter.STRING),			//16
				    new Parameter("Calidad Producto", Parameter.STRING),			//17
				    new Parameter("Tipo Medida", Parameter.STRING),				//18
				    new Parameter("Largo", Parameter.STRING),					//19  Antes 3
				    new Parameter("Ancho", Parameter.STRING),					//20  Antes 4
				    new Parameter("Espesor", Parameter.STRING),					//21  Antes 5
				    new Parameter("Costo FOB Pais de Origen", Parameter.STRING),		//22
				    new Parameter("Moneda Compra Material FOB", Parameter.STRING),		//23
				    new Parameter("TC EUR/USD de Referencia", Parameter.STRING),		//24
				    new Parameter("Costo FOB Pais Origen en USD", Parameter.STRING),		//25
				    new Parameter("Pais Origen", Parameter.STRING),				//26
				    new Parameter("Arancel", Parameter.STRING),					//27
				    new Parameter("Puerto Embarque Pais Origen", Parameter.STRING),		//28
				    new Parameter("Indirecto Flete Maritimo DF", Parameter.STRING),		//29
				    new Parameter("Indirecto Flete Maritimo MTY", Parameter.STRING),		//30
				    new Parameter("Otros Indirectos Importacion DF", Parameter.STRING),		//31
				    new Parameter("Otros Indirectos Importacion MTY", Parameter.STRING),	//32
				    new Parameter("Dias Transito DF", Parameter.STRING),			//33
				    new Parameter("Dias Transito MTY", Parameter.STRING),			//34
				    new Parameter("Costo Real USD LAB DF", Parameter.STRING),			//35
				    new Parameter("Costo USD LAB DF", Parameter.STRING),			//36
				    new Parameter("Costo USD LAB MTY", Parameter.STRING),			//37
				    new Parameter("Margen Minimo", Parameter.STRING),				//38
				    new Parameter("Margen Ideal", Parameter.STRING),				//39
				    new Parameter("% Comision a Pagar", Parameter.STRING),			//40
				    new Parameter("Precio MIN USD DF", Parameter.STRING),			//41
				    new Parameter("Precio IDEAL USD DF", Parameter.STRING),			//42
				    new Parameter("Precio MIN USD MTY", Parameter.STRING),			//43
				    new Parameter("Precio IDEAL USD MTY", Parameter.STRING),			//44
				    new Parameter("Organizacion", Parameter.STRING),				//45
				    new Parameter("Almacen", Parameter.STRING)					//46

			};
		}

	protected Object[] validateProcess(Validator validator, String... values) throws Exception {
		//Ejemplos
		//Se validan cada uno de los campos
		//Los campos comentados son dle producto, que pudieron no venir en la carga
		validator.checkNotNull(validator.checkString(values[0], 60), "PRODUCTO - NOMBRE CONCATENADO");
		//validator.checkNotNull(validator.checkString(values[3], 60), "Nombre Comercial Material");
		//validator.checkNotNull(validator.checkString(values[4], 60), "Nombre Origen Material");
		//validator.checkNotNull(validator.checkString(values[5], 60), "Categoria");
		validator.checkNotNull(validator.checkString(values[6], 60), "# Contenedor");
		validator.checkNotNull(validator.checkString(values[9], 60), "#Lamina o #Guacal");
		validator.checkNotNull(validator.checkString(values[11], 60), "M2");
		//validator.checkNotNull(validator.checkString(values[14], 60), "Formato");
		//validator.checkNotNull(validator.checkString(values[15], 60), "Acabado");
		//validator.checkNotNull(validator.checkString(values[16], 60), "Categoria Piedra");
		//validator.checkNotNull(validator.checkString(values[17], 60), "Calidad Producto");
		//validator.checkNotNull(validator.checkString(values[18], 60), "Tipo Medida");
		//validator.checkNotNull(validator.checkString(values[19], 60), "Largo");
		//validator.checkNotNull(validator.checkString(values[20], 60), "Ancho");
		//validator.checkNotNull(validator.checkString(values[21], 60), "Espesor");
		//validator.checkNotNull(validator.checkString(values[22], 60), "Costo FOB Pais de Origen");
		//validator.checkNotNull(validator.checkString(values[23], 60), "Moneda Compra Material FOB");
		//validator.checkNotNull(validator.checkString(values[26], 60), "Pais Origen");
		//validator.checkNotNull(validator.checkString(values[27], 60), "Arancel");
		//validator.checkNotNull(validator.checkString(values[28], 60), "Puerto Embarque Pais Origen");
		validator.checkNotNull(validator.checkString(values[45], 60), "Organizacion");
		validator.checkNotNull(validator.checkString(values[46], 60), "Almacen");	
		//validator.checkBigDecimal(values[25]); //Largo
		//validator.checkBigDecimal(values[26]); //Ancho
		//validator.checkBigDecimal(values[27]); //Espesor
		validator.checkBigDecimal(values[11]); //M2
		//validator.checkBigDecimal(((String)values[27]).replaceAll("[^0-9]","")); //Arancel
		return values;
	}


	@Override
		public BaseOBObject internalProcess(Object... values) throws Exception {
			//Verifica si se esta iniciando una nueva transaccion
			if(getRecordsProcessed() == 0){
				//Se vacia la tabla de Hash de todos los objetos
				pHash.clear();
				headerHash.clear();
				containerHash.clear();
				linesHash.clear();
				//La hora de inicio del proceso
				dTemp = new Date();
				//Busca ARCA MEXICO en la BD
				arcamexico = findArca();
				//Busca los objetos tile y slab
				getAttributeSet();
				//Busca la organizacion actual
				org = findOrg();
				//Busca el tipo de documento Que se necesita
				dt = findDocType();
				//Se busca el almacen que se necesita
				//Se busca la unidad de medida que sera usada
				uom = findUOM("Unit");

				//Se obtiene la primera direccion disponible del Business Partner
				if((arcamexico.getBusinessPartnerLocationList()).isEmpty()){
					throw new OBException(Utility.messageBD(conn,
								"El tercero no tiene ninguna direccion asociada." , 
								vars.getLanguage()
								)
							);
				}
				bpLocation = (arcamexico.getBusinessPartnerLocationList()).get(0);
			}

			wh = findWarehouse((String)values[46]);
			//Busca l acabecera de las lineas que se van a insertar
			//Como se debe de hacer una cabecera por cada contenedor diferente
			//Nuestro difereneciado debe ser el contenedor en si.
			//Tambien se envia el Numero de Pedimento
			ShipmentInOut InOut = findInOut((String)values[6],(String)values[7],(String)values[45],(String)values[46]);
			//Decide si es tile o slab
			AttributeSet ts = null;
			String res = (((String)values[14]).trim().toUpperCase());
			Product p = null;
			if(res.equals("SLAB")){
				ts = slab;
			}else if(res.equals("TILE")){
				ts = tile;
			}else{
			        p = findProduct(ts,values);
			        if(p == null)
			          throw new OBException(Utility.messageBD(conn, "Error de Formato, no es TILE ni SLAB linea: <"+(getRecordsProcessed()+2)+">" , vars.getLanguage()));
			}
			
			if(p==null)
			  p = findProduct(ts,values);
			
			BigDecimal arancel,espesor,costoFOB,largo,ancho;
			largo = (values[19]==null || (((String)values[19]).replaceAll("[^\\d|^\\.]","").trim()).equals("") )? null : new BigDecimal((String)values[19]);
			ancho = (values[20]==null ||(((String)values[20]).replaceAll("[^\\d|^\\.]","").trim()).equals(""))? null : new BigDecimal((String)values[20]);
			if(values[27]==null || (((String)values[27]).replaceAll("[^\\d|^\\.]","").trim()).equals("")){
				arancel = null;
			}else if((((String)values[27]).replaceAll("[^0-9]","")).trim().equals("")){
				arancel = null;
			}else {
				arancel = new BigDecimal(((String)values[27]).replaceAll("[^0-9]",""));
			}
			espesor = (values[21]==null || (((String)values[21]).replaceAll("[^\\d|^\\.]","").trim()).equals(""))? null : new BigDecimal((String)values[21]);
			costoFOB = (values[22]==null || (((String)values[22]).replaceAll("[^\\d|^\\.]","").trim()).equals(""))? null : new BigDecimal((String)values[22]);
			String embarque;
			if(values[28] == null || (((String)values[28]).trim()).equals("")){
				embarque = null;
			}else{
				try{
					embarque = (((String)values[28]).split("-")[1]).trim();
				}catch(Exception e){
					embarque = null;
				}
			}
			
			
			//Crea una instancia que describe al producto
			AttributeSetInstance asiTemp =  createASI(
					(String)values[9],
					InOut.getOrganization(),
					p.getAttributeSet(),
					//alto,ancho,espesor(por si se necesita despues),(se removio fecha de entrada)
					largo,
					ancho,
					espesor,
					(String) values[17],			//Calidad de la pieza
					res,		//Tile o Slab
					p,
					(String)values[36]
					);


			//Busca el producto
			//Se tomaran el mismo largo, ancho y espesor del archivo dado
			//largo = p.getDmprodHeight();
			//ancho = p.getDmprodWidth();
			//espesor = p.getDmprodDepth();
			//This was hard coded because this field is not in the new sample file
			//If it needs to be changed we nee dto choose a new locator or make explicit the place
			//Where we want to put the slab or tile
			Locator hueco = findHueco("OK");
			//Crea la linea y se la asigna
			//Se busca la linea que pertenece a un producto para una cabecera en especifico.
			//Se utiliza la cabecera y el producto para encontrar la linea que se desea
			ShipmentInOutLine InOutLine = findInOutLine(InOut,p,InOut.getCompraContenedor(),asiTemp,hueco);
			largo = (largo == null)? null: largo;
                        ancho = (ancho == null)? null: ancho;
                        espesor = (espesor == null)? null: espesor;
                    
			//largo = (largo == null)? null: largo.divide(new BigDecimal("10"),5,BigDecimal.ROUND_CEILING);
			//ancho = (ancho == null)? null: ancho.divide(new BigDecimal("10"),5,BigDecimal.ROUND_CEILING);
			//espesor = (espesor == null)? null: espesor.divide(new BigDecimal("10"),5,BigDecimal.ROUND_CEILING);
			if(largo == null || ancho == null){
				throw new OBException(Utility.messageBD(conn, "Ancho y/o Alto no pueden ser nulos linea: <"+(getRecordsProcessed()+2)+">" , vars.getLanguage()));

			}
			log.info("Linea:"+getRecordsProcessed());
			return createLine(
					InOutLine,					//Linea que se usara como base
					asiTemp,					//Atributos
					p,						//Producto
					(String)values[11],				//Cantidad (Metros Cuadrados)
					hueco,						//Hueco (Locator)
					largo,
					ancho,
					espesor
					);
		}


	//Crea la linea que se guardara en la base de datos
	public BaseOBObject createLine(
			final ShipmentInOutLine linea,
			final AttributeSetInstance ASID,
			final Product product,
			final String c,
			final Locator hueco,
			final BigDecimal alto,
			final BigDecimal ancho,
			final BigDecimal espesor
			)throws Exception {

		//Inicia la creacion de las lineas en almacdistrib
		almacdistrib ad = OBProvider.getInstance().get(almacdistrib.class);
		BigDecimal cantidad = new BigDecimal(c);
		if(ad == null){
			throw new OBException(Utility.messageBD(conn, "No se pudo crear una nueva linea de la distribucion del producto" , vars.getLanguage()));
		}else if(cantidad.compareTo(BigDecimal.ZERO)==0){
			throw new OBException(Utility.messageBD(conn, "La cantidad de m2 no debe de ser \"0\" linea: <"+(getRecordsProcessed()+2)+">" , vars.getLanguage()));
		}
		//Asigna el precio a la entrada también
		linea.setAlmacPreciom2(ASID.getAlmacCostoRealusd());
		linea.setAlmacMovementqty((linea.getAlmacMovementqty()).add(cantidad));
		//Se va sumando a la linea las cantidades
		obdal.save(linea);
		ad.setCompraContenedor(linea.getCompraContenedor());
		ad.setClient(OBContext.getOBContext().getCurrentClient());
		ad.setOrganization(org);
		ad.setActive(true);
		ad.setCreationDate(dTemp);
		ad.setCreatedBy(OBContext.getOBContext().getUser());
		ad.setUpdated(dTemp);
		ad.setUpdatedBy(OBContext.getOBContext().getUser());
		ad.setMovementQuantity(cantidad);
		ad.setAttributeSetValue(ASID);
		ad.setGoodsShipmentLine(linea);
		ad.setProduct(product);
		ad.setStorageBin(hueco);
		ad.setAlto(alto);
		ad.setAncho(ancho);
		ad.setName(c);
		obdal.save(ad);

		//
		//
		//
		obdal.flush();
		//obdal.getSession().clear();
		return ad;
	}

	//Crea un Attribute Instance
	private AttributeSetInstance createASI(String description,Organization org, AttributeSet attr, BigDecimal alto, BigDecimal ancho, BigDecimal espesor, String calidad, String tileoslab, Product p,String costo){
		//Verifica el Costo del Producto
	        BigDecimal costoReal = BigDecimal.ZERO;
	        if(costo == null || costo.trim().equals("")){
	            costoReal = p.getAlmacFobUsd();
	            log.info("Costo del Producto="+costoReal);
	        }else{
	            log.info("Costo del Archivo = "+costo);
	            costoReal = new BigDecimal(costo);
	        }
	        
	        //Verificas que exist ael atributo en la base de datos
		StringBuilder wC = new StringBuilder();
		wC.append(" as dt ");
		wC.append(" where  organization='"+org.getId()+"'");
		wC.append(" AND  attributeSet='"+attr.getId()+"'");
		wC.append(" AND  trim(upper(description))=trim(upper('"+description+"'))");
		wC.append(" AND  round(almacAlto,4)=round('"+alto.toString()+"',4)");
		wC.append(" AND  round(almacAncho,4)=round('"+ancho.toString()+"',4)");
		wC.append(" AND client = '"+OBContext.getOBContext().getCurrentClient().getId()+"' ");

		//Encontro el Guacal o Slab en la Base de Datos
		OBQuery<AttributeSetInstance> aQ = obdal.createQuery(AttributeSetInstance.class,wC.toString());
		List<AttributeSetInstance> asiList = aQ.list();
		if(!asiList.isEmpty()){
			return asiList.get(0);
		}

		//Si no lo encontro, entonces creas el atributo
		AttributeSetInstance asi = OBProvider.getInstance().get(AttributeSetInstance.class);
		//Asignas los atributos
		asi.setAlmacCostoRealusd(costoReal);
		asi.setClient(OBContext.getOBContext().getCurrentClient());
		asi.setOrganization(org);
		asi.setActive(true);
		asi.setCreationDate(dTemp);
		asi.setCreatedBy(OBContext.getOBContext().getUser());
		asi.setUpdated(dTemp);
		asi.setUpdatedBy(OBContext.getOBContext().getUser());
		asi.setAttributeSet(attr);
		if(description.toUpperCase().charAt(0) != 'G' && attr.equals(tile) ){
		  description = "G"+(description.trim());
		}else if(description.toUpperCase().charAt(0) != 'S' && attr.equals(slab)){
		  description = "S"+(description.trim());
		}
		asi.setDescription(description);
		asi.setAlmacAlto(alto);
		asi.setAlmacAncho(ancho);
		if(calidad != null && !(calidad.trim()).equals(""))
			asi.setAlmacCalidadP(findCalidadPieza(calidad));
		obdal.save(asi);
		obdal.flush();
		return asi;
	}

	//Busca los Attribute Set Tile o Slab para este cliente en la base de datos
	private void getAttributeSet(){
		StringBuilder wC = new StringBuilder();
		wC.append(" as dt");
		wC.append(" where (name = 'TILE' or name = 'SLAB')  ");
		wC.append(" AND client = '"+OBContext.getOBContext().getCurrentClient().getId()+"' ");
		wC.append(" order by name");
		OBQuery<AttributeSet> aQ = obdal.createQuery(AttributeSet.class,wC.toString());
		List<AttributeSet> attrList = aQ.list();
		if(attrList.size() < 2){
			throw new OBException(Utility.messageBD(conn, "No existe el conjunto de atributos TILE y SLAB para el cliente" , vars.getLanguage()));
		}

		slab = attrList.get(0);
		tile = attrList.get(1);
	}

	// Busca un producto en la base de datos
	// O en la tabla de Hash en caso de que ya haya sido buscado anteriormente
	private Product findProduct(AttributeSet ts, Object... v){
		Product p = null;
		String embarque = "";
		//Busca en la tabla de HASH el nombre o valor del producto
		String value = ((String)v[0]).trim();
		if(pHash.containsKey(value))
			return pHash.get(value);

		try{
			if(!(((String)v[26]).trim().toUpperCase()).equals("NACIONAL") && !(((String)v[28]).trim().equals("")) && v[28]!=null)
				embarque = (((String)v[28]).split("-")[1]).trim();
		}catch(Exception e){
			throw new OBException(Utility.messageBD(conn, "El puerto de embarque no se encuentra en el formato Pais - puerto <"+((String)v[28])+"> linea: <"+(getRecordsProcessed()+2)+">" , vars.getLanguage()));
		}

		//En caso de que no lo encuentre lo busca en la base de datos
		if(p == null){
			OBCriteria<Product> pQ = obdal.createCriteria(Product.class);
			pQ.add(Expression.or(Expression.ilike("name",value.trim()),Expression.ilike("searchKey",value.trim())));
			List<Product> pList = pQ.list();
			//log.info("Query producto:"+pQ);
			//Si no crea un producto completamente nuevo
			if(pList.isEmpty()){
			        log.info("Creando Producto:  "+value);
			        if(ts == null){
			           throw new OBException(Utility.messageBD(conn, "Error de Formato, no es TILE ni SLAB linea: <"+(getRecordsProcessed()+2)+">" , vars.getLanguage()));
			        }
				p = OBProvider.getInstance().get(Product.class);
				p.setClient(OBContext.getOBContext().getCurrentClient());
				p.setOrganization(org);
				p.setActive(true);
				p.setCreationDate(dTemp);
				p.setCreatedBy(OBContext.getOBContext().getUser());
				p.setUpdated(dTemp);
				p.setUpdatedBy(OBContext.getOBContext().getUser());
				p.setTaxCategory(findTax("IVA 16"));
				p.setProductType("I");
				p.setAttributeSet(ts);
				p.setPrintPrice(true);
				p.setCostType("AV");
				p.setEnforceAttribute(false);
				p.setCalculated(false);
				p.setProduction(false);
				p.setQuantityType(false);
				p.setQuantityvariable(false);
				p.setDescription("Creado por Carga Automática");
				p.setUOM(uom);
				p.setSummaryLevel(false);
				p.setStocked(true);
				p.setPurchase(true);
				p.setSale(true);
				p.setBillOfMaterials(false);
				p.setPrintDetailsOnInvoice(false);
				p.setPrintDetailsOnPickList(false);
				p.setBOMVerified(false);
				p.setProcessNow(false);
			}else{
				p = pList.get(0);
			}
		}
		//log.info("Product:"+p);
		p.setName(((String)v[0]).toUpperCase());
		if(v[1] != null && !(((String)v[1]).trim()).equals(""))
			p.setSKU(((String)v[1]).toUpperCase());
		if(v[16] != null && !(((String)v[16]).trim()).equals("")){
			p.setProductCategory(findProductCategory(((String)v[16]).trim()));
		}else{
			p.setProductCategory(findProductCategory("Normal"));
		}
		if(v[13] != null && !(((String)v[13]).trim()).equals(""))
			p.setDmprodTipopiedra(findTipoPiedra((String)v[13]));
		if(v[0] != null && !(((String)v[0]).trim()).equals(""))
			p.setSearchKey(((String)v[0]).trim().toUpperCase());
		if(v[3] != null && !(((String)v[3]).trim()).equals(""))
			p.setName2(((String)v[3]).trim().toUpperCase());
		if(!(((String)v[26]).trim().toUpperCase()).equals("NACIONAL") && !(((String)v[28]).trim().equals("")) && v[28]!=null)
			p.setAlmacPuertos(findPuerto2(embarque));
		if(v[4] != null && !(((String)v[4]).trim()).equals(""))
			p.setAlmacNombreOrigen(((String)v[4]).trim().toUpperCase());
		if(v[5] != null && !(((String)v[5]).trim()).equals(""))
			p.setAlmacCatPiedra(findCategoria(((String)v[5]).trim())); 
		if(v[18] != null && !(((String)v[18]).trim()).equals(""))
			p.setAlmacTipoMedida(findTipoMedida(((String)v[18]).trim()));
		if(v[21] != null && !(((String)v[21]).trim()).equals(""))
			p.setDmprodDepth((new BigDecimal(((String)v[21]).trim())));
		if(v[22] != null && !(((String)v[22]).trim()).equals(""))
			p.setDmprodPreciofob(new BigDecimal(((String)v[22]).trim()));
		if(v[23] != null && !(((String)v[23]).trim()).equals(""))
			p.setAlmacCCurrency(findMoneda((String)v[23]));
		if(v[26] != null && !(((String)v[26]).trim()).equals(""))
			p.setDmprodCountry(findCountry(((String)v[26]).trim(),1));
		if(v[27] != null && !(((String)v[27]).replaceAll("[^0-9|^\\.]","")).equals(""))
			p.setDmprodArancel(new BigDecimal(((String)v[27]).replaceAll("[^0-9|^\\.]","")));

		if(p.getDmprodArancel() != null && (p.getDmprodArancel()).compareTo(BigDecimal.ZERO) != 0){
			p.setDmprodIsarancel(true);	
		}else{
			p.setDmprodIsarancel(false);
		}
		if(v[19] != null && !(((String)v[19]).trim()).equals(""))
			p.setDmprodHeight((new BigDecimal(((String)v[19]).trim())));
		if(v[20] != null && !(((String)v[20]).trim()).equals(""))
			p.setDmprodWidth((new BigDecimal(((String)v[20]).trim())));

		if(v[23] != null && !(((String)v[16]).trim()).equals("")){
			if(((((String)v[23]).trim()).trim().toUpperCase()).equals("EUR")){
				p.setAlmacFobUsd((p.getDmprodPreciofob()).multiply((OBContext.getOBContext().getCurrentClient().getAlmacCrateUsdeur())));
			}else if(((((String)v[23]).trim()).trim().toUpperCase()).equals("USD")){
				p.setAlmacFobUsd(p.getDmprodPreciofob());			
			}else{
				p.setAlmacFobUsd((p.getDmprodPreciofob()).divide((OBContext.getOBContext().getCurrentClient().getAlmacCrateUsdmxn()),5,BigDecimal.ROUND_CEILING));
			}
		}
		if(v[15] != null && !(((String)v[15]).trim()).equals(""))
			p.setDmprodAcabado(Levenshtein.execute((String)v[15]));

		obdal.save(p);
		obdal.flush();
		pHash.put(value,p);
		if(v[26] != null && !(((String)v[26]).trim()).equals("") && p.getAlmacPuertos() != null){
			if(!(((String)v[26]).trim().toUpperCase()).equals("NACIONAL") && !(((String)v[28]).trim().equals("")) && v[28]!=null)
				updateRoutes(p,(String)v[28]);
		}
		obdal.flush();
		return p;
	}

	//Busca la cabecera de InOut en la base de datos
	private ShipmentInOut findInOut(String c, String p,String organizacion,String almacen){
		c = c.trim();
		p =  p.trim();

		//Busca en la tabla de HASH el nombre o valor del producto
		if(headerHash.containsKey(c)){
			return headerHash.get(c);
		}else{
			//Busca el contenedor, en caso de que no exista lo crea
			compracontenedor contenedor = findContainer(c,p);
			//En caso de que no lo encuentre lo busca en la base de datos
			OBCriteria<ShipmentInOut> hQ = obdal.createCriteria(ShipmentInOut.class);

			//Inicia creacion de cabecera
			ShipmentInOut header = OBProvider.getInstance().get(ShipmentInOut.class);
			if(header == null){
				throw new OBException(Utility.messageBD(conn,
							"No se pudo crear el registro de la cabecera para el contenedor: <"+contenedor+"> linea: <"+(getRecordsProcessed()+2)+">" , 
							vars.getLanguage()
							)
						);
			}
			//Finaliza creacion de cabecera
			header.setOrganization(findOrgArca(organizacion));
			header.setActive(true);
			header.setCreationDate(dTemp);
			header.setCreatedBy(OBContext.getOBContext().getUser());           
			header.setUpdated(dTemp);  
			header.setUpdatedBy(OBContext.getOBContext().getUser());

			//Asignacion del contenedor
			//header.setCompraContenedor(contenedor);
			header.setCompraContenedordf(contenedor);

			header.setBusinessPartner(arcamexico);
			header.setMovementDate(dTemp);
			header.setAccountingDate(dTemp);
			header.setDescription("Proceso de Carga de Inventario. Pedido: <"+p+">");
			header.setDocumentType(dt);
			header.setOrderReference(p);
			header.setWarehouse(wh);
			header.setPartnerAddress(bpLocation);
			header.setSalesTransaction(false);
			header.setDocumentNo(c);
			header.setDocumentAction("CO");
			header.setPosted("N");
			header.setProcessNow(false);
			header.setProcessed(false);
			header.setMovementType("V+");
			header.setDeliveryMethod("D");
			header.setLogistic(false);
			header.setCompraIsnacional(false);
			header.setDocumentStatus("DR");
			header.setDescription("Carga Automática de Inventario. Pedimento: <"+p+">");
			header.setPrint(false);
			//Inserta el elemento en la tabla de hash
			obdal.save(header);
			obdal.flush();
			headerHash.put(c,header);

			//Regresa el objeto
			return header;
		}
	}


	//Busca el contenedor en la base de datos y en el hash, en caso de que no exista, lo crea
	private compracontenedor findContainer(String c,String p){
		c = c.trim();
		p = p.trim();

		//En caso de que no lo encuentre lo busca en la base de datos
		OBCriteria<compracontenedor> cQ = obdal.createCriteria(compracontenedor.class);
		cQ.add(Expression.eq("documentNo",c));
		cQ.add(Expression.eq("client",OBContext.getOBContext().getCurrentClient()));
		List<compracontenedor> cList = cQ.list();

		//El contenedor se crea exclusivamente en el proceso, si este se llega a encontrar, significa que ya existe un contenedor
		// llamado igual y por lo tanto el proceso se cancela.
		if(!cList.isEmpty()){
			return cList.get(0);
			//throw new OBException(Utility.messageBD(conn, "Ya existe un contenedor con el mismo nombre. Contenedor: <"+c+">" , vars.getLanguage()));		
		}

		//Se crea el nuevo contenedor
		compracontenedor contenedor = OBProvider.getInstance().get(compracontenedor.class);

		if(contenedor == null){
			throw new OBException(Utility.messageBD(conn, "No se pudo crear un registro de Contenedor nuevo." , vars.getLanguage()));
		}
		//Asigan los atributos del contenedor
		contenedor.setClient(OBContext.getOBContext().getCurrentClient());

		contenedor.setOrganization(org);
		contenedor.setActive(true);
		contenedor.setCreationDate(dTemp);
		contenedor.setCreatedBy(OBContext.getOBContext().getUser());           
		contenedor.setUpdated(dTemp);  
		contenedor.setUpdatedBy(OBContext.getOBContext().getUser());
		contenedor.setDescription("Contenedor creado por carga automática. Pedido:"+p);
		contenedor.setPedimento(p);
		contenedor.setAlertStatus("Cargado en planta");
		contenedor.setEscontenedordf(true);
		contenedor.setDocumentNo(c);
		contenedor.setBusinessPartner(arcamexico);
		contenedor.setName(c);
		//Se guarda el contenedor en la base de datos
		obdal.save(contenedor);	
		obdal.flush();
		return contenedor;
	}


	//Busca Arca Mexico en la base de datos para mantenerla como referencia durante el proceso de carga
	private BusinessPartner findArca(){
		//Se hace una busqueda por value en la base de datos
		OBCriteria<BusinessPartner> bpQ = obdal.createCriteria(BusinessPartner.class);
		bpQ.add(Expression.eq("searchKey","ARCA MEXICO"));
		bpQ.add(Expression.eq("client",OBContext.getOBContext().getCurrentClient()));
		List<BusinessPartner> bpList = bpQ.list();
		//Si no encuentra el Business Partner
		if(bpList.isEmpty()){
			throw new OBException(Utility.messageBD(conn, "No se encontró el Tercero: <ARCA MEXICO>. No se puede inciar el proceso." , vars.getLanguage()));
		}
		return bpList.get(0);
	}

	//Busca la organizacion que se utilizara para hacer la carga
	private Organization findOrg(){
		return obdal.get(Organization.class,"0");
		//return OBContext.getOBContext().getCurrentOrganization();
	}

	//Busca el tipo de documento que se utilizara, que en este caso es  > Recepción de mercancía de proveedor extranjero
	private DocumentType findDocType(){
		StringBuilder wC = new StringBuilder(); 
		wC.append(" as dt");                    
		wC.append(" where upper(name) like  'RECEPCI%N DE MERCANC%A DE PROVEEDOR EXTRANJERO%' ");
		wC.append(" AND client = '"+OBContext.getOBContext().getCurrentClient().getId()+"' ");
		wC.append(" order by name");
		OBQuery<DocumentType> aQ = obdal.createQuery(DocumentType.class,wC.toString());
		List<DocumentType> docList = aQ.list();
		if(docList.isEmpty()){
			throw new OBException(Utility.messageBD(conn, "No existe el tipo de documento <Recepción de mercancía de proveedor extranjero>" , vars.getLanguage()));
		}

		return docList.get(0);
	}


	//Busca el almacen donde se van a almacenar
	private Warehouse findWarehouse(String name){
		name = name.trim().toUpperCase();
		StringBuilder wC = new StringBuilder(); 
		wC.append(" as dt");                    
		wC.append(" where (trim(upper(name)) = '"+name+"' or trim(upper(value)) = '"+name+"')");
		wC.append(" order by name");
		OBQuery<Warehouse> aQ = obdal.createQuery(Warehouse.class,wC.toString());
		List<Warehouse> whList = aQ.list();
		if(whList.isEmpty()){
			throw new OBException(Utility.messageBD(conn, "No existe el almacen que se intenat buscar <"+name+">" , vars.getLanguage()));
		}

		return whList.get(0);
	}

	private ShipmentInOutLine findInOutLine(ShipmentInOut InOut, Product product,compracontenedor contenedor,AttributeSetInstance asi,Locator hueco){
		//Busca en la tabla de HASH el nombre o valor del producto
		List<Object> value = Arrays.asList((Object)InOut,(Object)product); 
		if(linesHash.containsKey(value)){
			return linesHash.get(value);
		}else{
			//Si no existe entonces crea la linea
			ShipmentInOutLine iol = OBProvider.getInstance().get(ShipmentInOutLine.class);
			if(iol == null){
				throw new OBException(Utility.messageBD(conn, "No se pudo crear una nueva linea de Entradas y Salidas" , vars.getLanguage()));
			}
			iol.setClient(OBContext.getOBContext().getCurrentClient());
			iol.setOrganization(InOut.getOrganization());
			iol.setActive(true);
			iol.setCreationDate(dTemp);
			iol.setCreatedBy(OBContext.getOBContext().getUser());
			iol.setUpdated(dTemp);
			iol.setUpdatedBy(OBContext.getOBContext().getUser());
			iol.setDescription("Linea creada por carga automática.");
			iol.setShipmentReceipt(InOut);
			iol.setProduct(product);
			iol.setUOM(uom);
			iol.setStorageBin(hueco);
			iol.setLineNo(getLineNo(InOut));
			iol.setCompraContenedor(contenedor);
			//Se debe poner como 0 ya que se va a ir sumando la cantidad
			// Y el campo por default pone como cantidad asignada 1
			iol.setAlmacMovementqty(new BigDecimal("0"));
			//iol.setMovementQuantity(new BigDecimal("0"));
			iol.setReinvoice(false);
			iol.setDescriptionOnly(false);
			//iol.setOrderQuantity(new BigDecimal("0"));
			iol.setAttributeSetValue(asi);

			//Guarda el elemento en la base de datos
			obdal.save(iol);	
			obdal.flush();
			//obdal.getSession().refresh(iol);
			//Inserta el elemento en la tabla de hash
			linesHash.put(value,iol);

			//Regresa el objeto
			return iol;
		}
	}

	//FUncion utilizada para buscar la unidad de medida en la base de datos
	private UOM findUOM(String name){
		name = name.trim();
		//Se hace una busqueda por name ne la base de datos
		OBCriteria<UOM> bpQ = obdal.createCriteria(UOM.class);
		bpQ.add(Expression.eq("name",name));
		List<UOM> bpList = bpQ.list();
		//Si no encuentra la unidad de medida
		if(bpList.isEmpty()){
			throw new OBException(Utility.messageBD(conn, "No se enccontró la Unidad de medida <"+name+">" , vars.getLanguage()));
		}
		return bpList.get(0);
	}


	//Busca el Numero mas grande de LineNo para un conjunto de lineas y regresa + 10
	private Long getLineNo(ShipmentInOut InOut){
		OBCriteria<ShipmentInOutLine>iolQ = obdal.createCriteria(ShipmentInOutLine.class);
		iolQ.add(Expression.eq("shipmentReceipt",InOut));
		iolQ.add(Expression.eq("client",OBContext.getOBContext().getCurrentClient()));
		int total = iolQ.count();
		if(total < 1){
			return new Long("10");		
		}else{
			return new Long((total*10)+10);
		}
	}

	//Guarda los Huecos en el hash
	private Locator findHueco(String value){
		value = value.trim();

		//Busca en la base de datos el hueco
		OBCriteria<Locator> hQ = obdal.createCriteria(Locator.class);
		hQ.add(Expression.eq("searchKey",value));
		hQ.add(Expression.eq("warehouse",wh));
		List<Locator> hList = hQ.list();
		//Si no encuentra el producto
		if(hList.isEmpty()){
			throw new OBException(Utility.messageBD(conn, "No se encontró el hueco: <"+value+"> Almacen <"+wh+">" , vars.getLanguage()));
		}

		return hList.get(0);
	}

	//Completa las cabeceras
	private void completaCabeceras(){
		//Obtiene todas las cabeceras del hash
		//headerHash
		OBContext old = OBContext.getOBContext(); 
		OBContext.setOBContext("0");
		for (Iterator iter = (headerHash.values()).iterator(); iter.hasNext();) {
			ShipmentInOut header = (ShipmentInOut) iter.next();
			//Crea un proceso nuevo
			Process process = obdal.get(Process.class, "51F2AF9E31FD628F0131FD6F6D0B005C");
			ProcessInstance pInstance = OBProvider.getInstance().get(ProcessInstance.class);
			//Asigna el proceso al pInstance
			pInstance.setProcess(process);
			//Debe ponerse como activo
			pInstance.setActive(true);
			log.info(header.getDocumentNo());
			pInstance.setRecordID(header.getId());
			//Obtiene el usuario del contexto
			pInstance.setUserContact(OBContext.getOBContext().getUser());
			//Lo guarda ne la base de datos
			obdal.save(pInstance);

			//Hace un flush para que quede el registro en la base de datos
			obdal.flush();

			//Llama al Stored Procedure
			try {
				// Se obtiene la conexion
				Connection connection = obdal.getConnection();

				//El formato de postgres par ahacer la llamada
				PreparedStatement ps = connection.prepareStatement("SELECT * FROM dtt_inout_post(?)");

				//Se pone el id del PInstance
				ps.setString(1, pInstance.getId());
				ps.execute();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}

			//Actualiza el registro para ver los cambios
			obdal.getSession().refresh(pInstance);
			//log.info("\n============================");
			//log.info(header.getDocumentNo());
			//log.info(pInstance.getResult());
			if(!((pInstance.getResult()).equals(new Long(1)))){
			        
				log.info(pInstance.getErrorMsg());
				error = true;
				OBDal.getInstance().rollbackAndClose();
				return;
			}
		}
		//Termina la transaccion
		obdal.commitAndClose();
		//Regresa al ultimo contexto
		OBContext.setOBContext(old);
	}


	private almacCatPiedra findCategoria(String name){
		//En caso de que no lo encuentre lo busca en la base de datos
		name = name.toUpperCase();
		OBCriteria<almacCatPiedra> pQ = obdal.createCriteria(almacCatPiedra.class);
		pQ.add(Expression.eq("commercialName", name.trim()));
		List<almacCatPiedra> pList = pQ.list();
		//Si no encuentra la categoria, esta se crea en l abase de datos
		if(pList.isEmpty()){
			almacCatPiedra ac = OBProvider.getInstance().get(almacCatPiedra.class);
			ac.setCommercialName(name.trim());
			ac.setClient(OBContext.getOBContext().getCurrentClient());
			ac.setOrganization(org);
			ac.setActive(true);
			ac.setCreationDate(dTemp);
			ac.setCreatedBy(OBContext.getOBContext().getUser());
			ac.setUpdated(dTemp);
			ac.setUpdatedBy(OBContext.getOBContext().getUser());
			ac.setDescription("Carga Automática");
			obdal.save(ac);
			obdal.flush();
			return ac;
		}

		return pList.get(0);
	}

	private almacTipoMedida findTipoMedida(String name){
		//En caso de que no lo encuentre lo busca en la base de datos
		name = name.toUpperCase();
		OBCriteria<almacTipoMedida> pQ = obdal.createCriteria(almacTipoMedida.class);
		pQ.add(Expression.eq("commercialName", name.trim()));
		List<almacTipoMedida> pList = pQ.list();
		//Si no encuentra la categoria, esta se crea en l abase de datos
		if(pList.isEmpty()){
			almacTipoMedida atm = OBProvider.getInstance().get(almacTipoMedida.class);
			atm.setCommercialName(name.trim());
			atm.setClient(OBContext.getOBContext().getCurrentClient());
			atm.setOrganization(org);
			atm.setActive(true);
			atm.setCreationDate(dTemp);
			atm.setCreatedBy(OBContext.getOBContext().getUser());
			atm.setUpdated(dTemp);
			atm.setUpdatedBy(OBContext.getOBContext().getUser());
			atm.setDescription("Carga Automática");
			obdal.save(atm);
			obdal.flush();
			return atm;
		}

		return pList.get(0);
	}


	private Currency findMoneda(String name){
		//En caso de que no lo encuentre lo busca en la base de datos
		if((name.trim().toUpperCase()).equals("PESOS")){
			name = "MXN";
		}
		OBCriteria<Currency> pQ = obdal.createCriteria(Currency.class);
		pQ.add(Expression.ilike("iSOCode", name.trim().toUpperCase(), MatchMode.ANYWHERE));
		List<Currency> pList = pQ.list();
		//Si no encuentra la moneda regresa un error y se interrumpe el proceso
		if(pList.isEmpty()){
			throw new OBException(Utility.messageBD(conn, "No existe la moneda <"+name+"> linea: <"+(getRecordsProcessed()+2)+">" , vars.getLanguage()));
		}
		return pList.get(0);
	}


	private Country findCountry(String name,int choice){

		if((name.trim().toUpperCase()).equals("NACIONAL")) name = "MEXICO";
		//Busca aunque tenga acentos
		String name2 = Normalizer.normalize(name.toLowerCase(), Normalizer.Form.NFKD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

		String id = specialQuery(
				"SELECT c_country_id from C_Country_trl WHERE TRIM(LOWER(ALMAC_UNACCENT(name))) like LOWER('%"+name2.trim()+"%') AND ad_language like 'es%'",
				"No se pudo encontrar el país: <"+name+">"
				);

		//Para evitar el comportamiento de actualizar forzosamente el producto se pone esta decision
		if(id == null){
			if(choice == 2){
				throw new OBException(Utility.messageBD(conn, "No se encontró el país <"+name+"> linea: <"+(getRecordsProcessed()+2)+">" , vars.getLanguage()));
			}else{
				return null;
			}
		}	

		Country pais = obdal.get(Country.class,id);

		if(pais == null){
			throw new OBException(Utility.messageBD(conn, "No se pudo recuperar el objeto del país <"+name+"> linea: <"+(getRecordsProcessed()+2)+">" , vars.getLanguage()));
		}
		return pais;
	}


	private almacSegRecom findSegmento(String name){
		if(name == null || name.trim() == ""){
			return null;
		}
		//En caso de que no lo encuentre lo busca en la base de datos
		String name2 = (name.toLowerCase().replaceAll("[á|é|í|ó|ú]", "_").trim());
		OBCriteria<almacSegRecom> pQ = obdal.createCriteria(almacSegRecom.class);
		pQ.add(Expression.ilike("commercialName", name2.trim(), MatchMode.ANYWHERE));
		List<almacSegRecom> pList = pQ.list();
		if(pList.isEmpty()){
			almacSegRecom sr = OBProvider.getInstance().get(almacSegRecom.class);
			sr.setCommercialName(name.trim());
			sr.setClient(OBContext.getOBContext().getCurrentClient());
			sr.setOrganization(org);
			sr.setActive(true);
			sr.setCreationDate(dTemp);
			sr.setCreatedBy(OBContext.getOBContext().getUser());
			sr.setUpdated(dTemp);
			sr.setUpdatedBy(OBContext.getOBContext().getUser());
			sr.setDescription("Carga Automática");
			obdal.save(sr);
			obdal.flush();
			return sr;
		}
		return pList.get(0);
	}

	private almacUsoRecom findUso(String name){
		if(name == null || name.trim() == ""){
			return null;
		}
		//En caso de que no lo encuentre lo busca en la base de datos
		String name2 = (name.toLowerCase().replaceAll("[á|é|í|ó|ú]", "_").trim());
		OBCriteria<almacUsoRecom> pQ = obdal.createCriteria(almacUsoRecom.class);
		pQ.add(Expression.ilike("commercialName", name2.trim(), MatchMode.ANYWHERE));
		List<almacUsoRecom> pList = pQ.list();
		if(pList.isEmpty()){
			almacUsoRecom sr = OBProvider.getInstance().get(almacUsoRecom.class);
			sr.setCommercialName(name.trim());
			sr.setClient(OBContext.getOBContext().getCurrentClient());
			sr.setOrganization(org);
			sr.setActive(true);
			sr.setCreationDate(dTemp);
			sr.setCreatedBy(OBContext.getOBContext().getUser());
			sr.setUpdated(dTemp);
			sr.setUpdatedBy(OBContext.getOBContext().getUser());
			sr.setDescription("Carga Automática");
			obdal.save(sr);
			obdal.flush();
			return sr;
		}
		return pList.get(0);
	}


	private almac_aplicacion findAplicacion(String name){
		if(name == null || name.trim() == ""){
			return null;
		}
		//En caso de que no lo encuentre lo busca en la base de datos
		String name2 = (name.toLowerCase().replaceAll("[á|é|í|ó|ú]", "_").trim());
		OBCriteria<almac_aplicacion> pQ = obdal.createCriteria(almac_aplicacion.class);
		pQ.add(Expression.ilike("commercialName", name2.trim(), MatchMode.ANYWHERE));
		List<almac_aplicacion> pList = pQ.list();
		if(pList.isEmpty()){
			almac_aplicacion sr = OBProvider.getInstance().get(almac_aplicacion.class);
			sr.setCommercialName(name.trim());
			sr.setClient(OBContext.getOBContext().getCurrentClient());
			sr.setOrganization(org);
			sr.setActive(true);
			sr.setCreationDate(dTemp);
			sr.setCreatedBy(OBContext.getOBContext().getUser());
			sr.setUpdated(dTemp);
			sr.setUpdatedBy(OBContext.getOBContext().getUser());
			sr.setDescription("Carga Automática");
			obdal.save(sr);
			obdal.flush();
			return sr;
		}
		return pList.get(0);
	}


	private dmprodcolor findColor(String name){
		if(name == null || name.trim() == ""){
			return null;
		}
		//En caso de que no lo encuentre lo busca en la base de datos
		String name2 = (name.toLowerCase().replaceAll("[á|é|í|ó|ú]", "_").trim());
		OBCriteria<dmprodcolor> pQ = obdal.createCriteria(dmprodcolor.class);
		pQ.add(Expression.ilike("name", name2.trim(), MatchMode.ANYWHERE));
		List<dmprodcolor> pList = pQ.list();
		if(pList.isEmpty()){
			dmprodcolor sr = OBProvider.getInstance().get(dmprodcolor.class);
			sr.setName(name.trim());
			sr.setClient(OBContext.getOBContext().getCurrentClient());
			sr.setOrganization(org);
			sr.setActive(true);
			sr.setCreationDate(dTemp);
			sr.setCreatedBy(OBContext.getOBContext().getUser());
			sr.setUpdated(dTemp);
			sr.setUpdatedBy(OBContext.getOBContext().getUser());
			obdal.save(sr);
			obdal.flush();
			return sr;
		}
		return pList.get(0);
	}

	private almacEstilo findEstilo(String name){
		if(name == null || name.trim() == ""){
			return null;
		}
		//En caso de que no lo encuentre lo busca en la base de datos
		String name2 = (name.toLowerCase().replaceAll("[á|é|í|ó|ú]", "_").trim());
		OBCriteria<almacEstilo> pQ = obdal.createCriteria(almacEstilo.class);
		pQ.add(Expression.ilike("commercialName", name2.trim(), MatchMode.ANYWHERE));
		List<almacEstilo> pList = pQ.list();
		if(pList.isEmpty()){
			almacEstilo sr = OBProvider.getInstance().get(almacEstilo.class);
			sr.setCommercialName(name.trim());
			sr.setClient(OBContext.getOBContext().getCurrentClient());
			sr.setOrganization(org);
			sr.setActive(true);
			sr.setCreationDate(dTemp);
			sr.setCreatedBy(OBContext.getOBContext().getUser());
			sr.setUpdated(dTemp);
			sr.setUpdatedBy(OBContext.getOBContext().getUser());
			sr.setDescription("Carga Automática");
			obdal.save(sr);
			obdal.flush();
			return sr;
		}
		return pList.get(0);
	}

	private dmprodareauso findArea(String name){
		if(name == null || name.trim() == ""){
			return null;
		}
		//En caso de que no lo encuentre lo busca en la base de datos
		String name2 = (name.toLowerCase().replaceAll("[á|é|í|ó|ú]", "_").trim());
		OBCriteria<dmprodareauso> pQ = obdal.createCriteria(dmprodareauso.class);
		pQ.add(Expression.ilike("name", name2.trim(), MatchMode.ANYWHERE));
		List<dmprodareauso> pList = pQ.list();
		if(pList.isEmpty()){
			dmprodareauso sr = OBProvider.getInstance().get(dmprodareauso.class);
			sr.setName(name.trim());
			sr.setClient(OBContext.getOBContext().getCurrentClient());
			sr.setOrganization(org);
			sr.setActive(true);
			sr.setCreationDate(dTemp);
			sr.setCreatedBy(OBContext.getOBContext().getUser());
			sr.setUpdated(dTemp);
			sr.setUpdatedBy(OBContext.getOBContext().getUser());
			obdal.save(sr);
			obdal.flush();
			return sr;
		}
		return pList.get(0);
	}


	private almacCalidadProducto findCalidadPieza(String name){
		if(name == null || name.trim() == ""){
			return null;
		}
		//En caso de que no lo encuentre lo busca en la base de datos
		String name2 = (name.toLowerCase().replaceAll("[á|é|í|ó|ú]", "_").trim());
		OBCriteria<almacCalidadProducto> pQ = obdal.createCriteria(almacCalidadProducto.class);
		pQ.add(Expression.ilike("commercialName", name2.trim(), MatchMode.ANYWHERE));
		List<almacCalidadProducto> pList = pQ.list();
		if(pList.isEmpty()){
			almacCalidadProducto sr = OBProvider.getInstance().get(almacCalidadProducto.class);
			sr.setCommercialName(name.trim());
			sr.setClient(OBContext.getOBContext().getCurrentClient());
			sr.setOrganization(org);
			sr.setActive(true);
			sr.setCreationDate(dTemp);
			sr.setCreatedBy(OBContext.getOBContext().getUser());
			sr.setUpdated(dTemp);
			sr.setUpdatedBy(OBContext.getOBContext().getUser());
			sr.setDescription("Carga Automática");
			obdal.save(sr);
			obdal.flush();
			return sr;
		}
		return pList.get(0);
	}

	private void updateRoutes(Product p, String puerto){
		String splits[] = new String[2];
		try{
			splits = puerto.split("-");
			splits[0] = splits[0].trim();
			splits[1] = splits[1].trim();
		}catch (Exception e){
			log.info("No se pudo hacer split en:"+puerto);
			return;
		}

		Country x = findCountry(splits[0],1);
		if( x == null){
			return;
		}
		iuConexion(p,x,splits[1],"DF");
		iuConexion(p,x,splits[1],"MTY");
	}

	private void iuConexion(Product p, Country c, String puerto, String destino){
		//Busca la Conexion entre una ciudad final y un puerto salida
		almacPuertosCon conexion = findConexion(findPuerto(c,puerto),findCiudad(destino));
		//Si la conexion es nula significa que no se han dado de lata los indirectos
		//Pero como es carga de Inventario no importa.
		if(conexion == null)
			return;
		OBCriteria<almacConexProd> pQ = obdal.createCriteria(almacConexProd.class);
		pQ.add(Expression.eq("almacPuertosConexion",conexion));
		pQ.add(Expression.eq("product",p));
		List<almacConexProd> pList = pQ.list();
		if(pList.isEmpty()){
			almacConexProd ac = OBProvider.getInstance().get(almacConexProd.class);
			ac.setProduct(p);
			ac.setCommercialName("--");
			ac.setAlmacPuertosConexion(conexion);
			ac.setClient(OBContext.getOBContext().getCurrentClient());
			ac.setOrganization(org);
			ac.setActive(true);
			ac.setCreationDate(dTemp);
			ac.setCreatedBy(OBContext.getOBContext().getUser());
			ac.setUpdated(dTemp);
			ac.setUpdatedBy(OBContext.getOBContext().getUser());
			ac.setDescription("Carga Automática");
			obdal.save(ac);
			obdal.flush();
		}
	}

	private City findCiudad(String name){
		//En caso de que no lo encuentre lo busca en la base de datos
		String name2 = (name.toLowerCase().replaceAll("[á|é|í|ó|ú|\\.+]", "_").trim());
		OBCriteria<City> pQ = obdal.createCriteria(City.class);
		pQ.add(Expression.ilike("abbreviation", name2.trim(), MatchMode.ANYWHERE));
		List<City> pList = pQ.list();
		if(pList.isEmpty()){
			throw new OBException(Utility.messageBD(conn, "No se encontró la ciudad:<"+name+">  Linea<"+(getRecordsProcessed()+2)+">" , vars.getLanguage()));
		}
		return pList.get(0);
	}

	private almacPuertos findPuerto(Country c, String name){
		//En caso de que no lo encuentre lo busca en la base de datos
		String name2 = (name.toLowerCase().replaceAll("[á|é|í|ó|ú]", "_").trim());
		OBCriteria<almacPuertos> pQ = obdal.createCriteria(almacPuertos.class);
		pQ.add(Expression.ilike("commercialName", name2.trim(), MatchMode.ANYWHERE));
		pQ.add(Expression.eq("country",c));
		List<almacPuertos> pList = pQ.list();
		if(pList.isEmpty()){
			throw new OBException(Utility.messageBD(conn, "No se encontró el puerto:<"+name+"/"+c.getName()+">  Linea<"+(getRecordsProcessed()+2)+">" , vars.getLanguage()));
		}
		return pList.get(0);
	}

	private almacPuertos findPuerto2(String name){
		//En caso de que no lo encuentre lo busca en la base de datos
		String name2 = (name.toLowerCase().replaceAll("[á|é|í|ó|ú]", "_").trim());
		OBCriteria<almacPuertos> pQ = obdal.createCriteria(almacPuertos.class);
		pQ.add(Expression.ilike("commercialName", name2.trim(), MatchMode.ANYWHERE));
		List<almacPuertos> pList = pQ.list();
		if(pList.isEmpty()){
			return null;
		}
		return pList.get(0);
	}

	private almacPuertosCon findConexion(almacPuertos p, City c){
		//En caso de que no lo encuentre lo busca en la base de datos
		OBCriteria<almacPuertosCon> pQ = obdal.createCriteria(almacPuertosCon.class);
		pQ.add(Expression.eq("desdePuerto",p));
		pQ.add(Expression.eq("city",c));
		List<almacPuertosCon> pList = pQ.list();
		if(pList.isEmpty()){
			return null;
			//throw new OBException(Utility.messageBD(conn, "No se encontró la conexión:<"+p.getCommercialName()+"/"+c.getAbbreviation()+">  Linea<"+(getRecordsProcessed()+2)+">" , vars.getLanguage()));
		}
		return pList.get(0);
	}

	//Este codigo sirve para hacer busquedas sin el DAL
	private String specialQuery(String q, String e){
		String id = null;
		try {
			//Consulta para encontrar al usuario
			PreparedStatement sqlQuery = new DalConnectionProvider(false).getPreparedStatement(q);
			sqlQuery.execute();
			ResultSet rs = sqlQuery.getResultSet();
			while(rs.next()){
				id=rs.getString(1);
			}
			return id;
		} catch (Exception ee) {
			throw new OBException(Utility.messageBD(conn, e +"  Error:"+ee.getMessage() , vars.getLanguage()));
		}
	}


	//Busca un esquema de impuestos
	private TaxCategory findTax(String value){
		//Busca en la base de datos el hueco
		OBCriteria<TaxCategory> hQ = obdal.createCriteria(TaxCategory.class);
		hQ.add(Expression.ilike("name",value.trim(), MatchMode.ANYWHERE));
		List<TaxCategory> hList = hQ.list();
		//Si no encuentra el producto
		if(hList.isEmpty()){
			throw new OBException(Utility.messageBD(conn, "No se encontró la categoria de impuestos: <"+value+">" , vars.getLanguage()));
		}

		return hList.get(0);
	}

	private Organization findOrgArca(String name){
		String id = specialQuery(
				"SELECT AD_ORG_ID"+
				" FROM AD_ORG "+
				"WHERE TRIM(LOWER(ALMAC_UNACCENT(name))) LIKE LOWER('%"+name+"%')"+
				" AND AD_CLIENT_ID IN ('0','"+
				OBContext.getOBContext().getCurrentClient().getId()+
				"')",
				"No se encontró la organización: <"+name+">"
				);

		if(id == null){
			throw new OBException(Utility.messageBD(conn, "No se encontró la organización <"+name+"> linea: <"+(getRecordsProcessed()+2)+">" , vars.getLanguage()));
		}	

		Organization organizacion = obdal.get(Organization.class,id);

		if(organizacion == null){
			throw new OBException(Utility.messageBD(conn, "No se pudo recuperar el objeto de la organización <"+name+"> linea: <"+(getRecordsProcessed()+2)+">" , vars.getLanguage()));
		}
		return organizacion;
	}

	private ProductCategory findProductCategory(String name){
		//En caso de que no lo encuentre lo busca en la base de datos
		String name2 = (name.toLowerCase().replaceAll("[á|é|í|ó|ú|\\.+]", "_").trim());
		OBCriteria<ProductCategory> pQ = obdal.createCriteria(ProductCategory.class);
		pQ.add(Expression.ilike("name", name2.trim()));
		List<ProductCategory> pList = pQ.list();
		if(pList.isEmpty()){
			throw new OBException(Utility.messageBD(conn, "No se encontró la categoría:<"+name+">  Linea<"+(getRecordsProcessed()+2)+">" , vars.getLanguage()));
		}
		return pList.get(0);
	}


	private dmprodtipopiedra findTipoPiedra(String name){
		//En caso de que no lo encuentre lo busca en la base de datos
		String name2 = (name.toLowerCase().replaceAll("[á|é|í|ó|ú|\\.+]", "_").trim());
		OBCriteria<dmprodtipopiedra> pQ = obdal.createCriteria(dmprodtipopiedra.class);
		pQ.add(Expression.ilike("name", name2.trim()));
		List<dmprodtipopiedra> pList = pQ.list();
		if(pList.isEmpty()){
			dmprodtipopiedra ac = OBProvider.getInstance().get(dmprodtipopiedra.class);
			ac.setName(name);
			ac.setClient(OBContext.getOBContext().getCurrentClient());
			ac.setOrganization(org);
			ac.setActive(true);
			ac.setCreationDate(dTemp);
			ac.setCreatedBy(OBContext.getOBContext().getUser());
			ac.setUpdated(dTemp);
			ac.setUpdatedBy(OBContext.getOBContext().getUser());
			ac.setDescription("Carga Automática");
			obdal.save(ac);
			obdal.flush();
			return ac;
		}
		return pList.get(0);
	}

}
