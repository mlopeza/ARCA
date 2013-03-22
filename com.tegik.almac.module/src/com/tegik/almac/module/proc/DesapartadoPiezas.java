// assign the background process to a package that belongs to the 
// main package of the module this custom development belongs to  
package com.tegik.almac.module.proc;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.quartz.JobExecutionException;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.hibernate.criterion.*;
import org.apache.log4j.Logger;
public class DesapartadoPiezas extends DalBaseProcess {

	static int counter = 0;

	private ProcessLogger logger;
	private static final Logger log = Logger.getLogger(DesapartadoPiezas.class);
	public void doExecute(ProcessBundle bundle) throws Exception {

		logger = bundle.getLogger(); // this logger logs into the LOG column of
		// the AD_PROCESS_RUN database table
		BigDecimal sumAmount = new BigDecimal(0);
		log.info("Proceso de Desapartado de Piezas por Organizació. Ciclo " + counter + "\n");
		logger.log("Proceso de Desapartado de Piezas por Organizació. Ciclo " + counter + "\n");

		try {
			//Tiempo del calendario
			Calendar calendar;
			OBCriteria<Organization> orgList = OBDal.getInstance().createCriteria(Organization.class);
			List<Organization> orgL = orgList.list();
			List<AttributeSetInstance> asiL = null;
			//El total de Organizaciones Encontradas
			log.info("Numero de Organizaciones:"+orgL.size()+"\n");

			for(Organization org : orgL){
				log.info("Organizacion:"+org);
				//El tiempo que se les dio a los apartados
				calendar = Calendar.getInstance();
				calendar.add(Calendar.DAY_OF_MONTH,(-1)*(new BigDecimal(org.getAlmacDesapartado()).intValueExact()));

				//Busca las piezas por organizacion que cumplen con los criterios
				OBCriteria<AttributeSetInstance> asiList = OBDal.getInstance().createCriteria(AttributeSetInstance.class);
				asiList.add(
						Expression.or(
							Expression.eq(AttributeSetInstance.PROPERTY_ALMACRESERVAPEDIDO,true),
							Expression.eq(AttributeSetInstance.PROPERTY_ALMACRESERVACOTIZACION,true)));
				asiList.add(Expression.eq(AttributeSetInstance.PROPERTY_ORGANIZATION,org));
				asiList.add(Expression.lt(AttributeSetInstance.PROPERTY_UPDATED,calendar.getTime()));
				asiL = asiList.list();

				for(AttributeSetInstance asi : asiL){
					asi.setAlmacReservapedido(false);
					asi.setAlmacReservacotizacion(false);
					asi.setVentasPickexecute(null);
					OBDal.getInstance().save(asi);
					log.info(asi);
				}
			}
		} catch (Exception e) {
			OBDal.getInstance().rollbackAndClose();
			throw new JobExecutionException(e.getMessage(), e);
		}

		OBDal.getInstance().flush();
	}
}
