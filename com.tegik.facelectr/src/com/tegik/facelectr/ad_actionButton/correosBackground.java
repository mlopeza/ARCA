// assign the background process to a package that belongs to the 
// main package of the module this custom development belongs to  
package com.tegik.facelectr.ad_actionButton;
 
import java.math.BigDecimal;
import java.util.Calendar;
 
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
import org.openbravo.model.common.invoice.Invoice;
import org.hibernate.criterion.Expression;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
 
// the background process needs to extend DalBaseProcess since
// we will be using DAL objects to perform database operations
public class correosBackground extends DalBaseProcess {
 
  static int counter = 0;
 
  private ProcessLogger logger;
 
  // abstract method doExecute needs to be implemented and carries
  // with itself the ProcessBundle object deriving from Openbravo Quartz
  // scheduler
  public void doExecute(ProcessBundle bundle) throws Exception {
  
    HttpSecureAppServlet serv = new HttpSecureAppServlet();
 
    logger = bundle.getLogger(); // this logger logs into the LOG column of
    // the AD_PROCESS_RUN database table
    //BigDecimal sumAmount = new BigDecimal(0);
 
    logger.log("Envío de correos Loop " + counter + "\n");
 
    try {
      final OBCriteria<Invoice> facturasList = OBDal.getInstance().createCriteria(Invoice.class);
      facturasList.add(Expression.eq(Invoice.PROPERTY_FETCORREOENVIADO, false));
      facturasList.add(Expression.eq(Invoice.PROPERTY_FETDOCSTATUS, "Facturado"));
      

      for (Invoice factura : facturasList.list()) {
         if (counter < 100){
	      enviadorCorreos enviador = new enviadorCorreos();
	      //ServletConfig config2 = ;
	      enviador.init(serv.getServletConfig());
	      String respuestaEnvio = enviador.solicitarEnvio(bundle.getContext().toVars(), factura, "Y", "Y");
	      logger.log("Envío de correos Loop " + factura.getDocumentNo() + " // " + respuestaEnvio + "\n");
	      counter ++;
	 }
      }
 
    } catch (Exception e) {
      // catch any possible exception and throw it as a Quartz
      // JobExecutionException
      throw new JobExecutionException(e.getMessage(), e);
    }
  }
}