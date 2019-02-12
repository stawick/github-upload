package com.ibm.sport.rtc.common;

import org.apache.commons.logging.Log;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.sport.rtc.common.process.ISportReportInfoCollector;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.workitem.common.IAuditableCommon;
import com.ibm.team.workitem.common.IQueryCommon;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.links.common.service.ILinkService;
import java.util.ArrayList;
import java.util.HashMap;

public class SportCommonData
{
   public static final String SPORT_PROCESS_ID = "com.ibm.sport.process.sport";
   private IAuditableCommon auditableCommon;
   private Log log;
   private IProgressMonitor monitor;
   private IProjectAreaHandle projectArea;
   private IQueryCommon queryCommon;
   private IWorkItemCommon workItemCommon;
   private IWorkItem workItemForUpdate;
   private ILinkService linkService;
   private ArrayList<SysrouteAparInfo> newAparNames2; //apars for new apar work items
   private String aparToAutoReceipt;             //for sysroute
   private ISportReportInfoCollector reportInfoCollector;
   private Boolean firstTime;
   private Boolean ignorePmr256;
   // _T155539A Map of PMR name to another multi-purpose map of key
   // attribute to object value for RTC PMR query use.
   // With special entries (keys start with ***) -
   // a. PMR work item (once found) - to prevent redundant
   // RTC queries (not used on create function)
   // b. PMR browse artifact - to prevent redundant PMR
   // browses
   // c. active saves key
   // d. error message (String) key
   // etc ...
   private HashMap<String, HashMap<String, Object>> referencedPMRs;

   public SportCommonData()
   {
      auditableCommon = null;
      log = null;
      monitor = null;
      projectArea = null;
      queryCommon = null;
      workItemCommon = null;
      workItemForUpdate = null;
      linkService = null;
      aparToAutoReceipt = null;                //sysroute
      newAparNames2 = new ArrayList<SysrouteAparInfo>();  //sysroute with link
      reportInfoCollector = null;
      firstTime = false;
      ignorePmr256 = false;
      // _T155539A Unique PMR by name and RETAIN create date(/time)
      referencedPMRs = null;
   }


   public void addNewAparName2 (SysrouteAparInfo newAparInfo)   
   {
      if (newAparNames2 == null)
         this.newAparNames2 = new ArrayList<SysrouteAparInfo>();
      this.newAparNames2.add( newAparInfo );
   }


   public IAuditableCommon getAuditableCommon()
   {
      return auditableCommon;
   }

   public String getAparToAutoReceipt()  
   {
      return (aparToAutoReceipt);
   }

   
   public Boolean getIgnorePmr256()  
   {
      return ignorePmr256;
   }
   
   public ILinkService getLinkService()
   {
      return linkService;
   }
   
   public Log getLog()
   {
      return log;
   }

   public IProgressMonitor getMonitor()
   {
      return monitor;
   }
   
   public ArrayList<SysrouteAparInfo> getNewAparNames2()  
   {
      return newAparNames2;
   }
   

   public IProjectAreaHandle getProjectArea()
   {
      return projectArea;
   }

   public IQueryCommon getQueryCommon()
   {
      return queryCommon;
   }

   public IWorkItemCommon getWorkItemCommon()
   {
      return workItemCommon;
   }

   public IWorkItem getWorkItemForUpdate()
   {
      return workItemForUpdate;
   }
   
   public Boolean isFirstTime()
   {
      return firstTime;
   }
   
   public void setAuditableCommon( IAuditableCommon auditableCommon )
   {
      this.auditableCommon = auditableCommon;
   }
   
   public void setAparToAutoReceipt( String name ) 
   {
      this.aparToAutoReceipt = name;
   }
   
   public void setFirstTime( Boolean isFirstTime)
   {
      this.firstTime = isFirstTime;
   }

   
   public void setIgnorePmr256(Boolean newVal)  
   {
      this.ignorePmr256 = newVal;
   }
   
   public void setLinkService(ILinkService ls)
   {
      this.linkService = ls;
   }
   
   public void setLog( Log log )
   {
      this.log = log;
   }

   public void setMonitor( IProgressMonitor monitor )
   {
      this.monitor = monitor;
   }

   public void setNewAparNames2( ArrayList<SysrouteAparInfo> newlist )  
   {
      this.newAparNames2 = newlist;
   } 

   
   public void setProjectArea( IProjectAreaHandle projectArea )
   {
      this.projectArea = projectArea;
   }

   public void setQueryCommon( IQueryCommon queryCommon )
   {
      this.queryCommon = queryCommon;
   }

   public void setWorkItemCommon( IWorkItemCommon workItemCommon )
   {
      this.workItemCommon = workItemCommon;
   }

   public void setWorkItemForUpdate( IWorkItem workItemForUpdate )
   {
      this.workItemForUpdate = workItemForUpdate;
   }

   public ISportReportInfoCollector getReportInfoCollector()
   {
      return reportInfoCollector;
   }

   public void setReportInfoCollector(
         ISportReportInfoCollector reportInfoCollector )
   {
      this.reportInfoCollector = reportInfoCollector;
   }

   // _T155539A Unique PMR by name and RETAIN create date(/time)
   /**
    * Used during APAR subscribed-PMR processing to communicate unique
    * PMR reference data across various phases of operation participant
    * processing.
    * Returns a map of unique PMR name references to another multi-
    * purpose map of attribute keys used for RTC querying; the result
    * artifact of a pre-browse of a PMR - to omit redundant PMR browse
    * calls to SBS / RETAIN; and other various specialized keys.
    * Note: This WILL be set (not null) during this APAR function, and
    * will NOT be set for the "create PMR that exists in RETAIN" action
    * issued from the RTC UI.
    * 
    * @return
    */
   public HashMap<String, HashMap<String, Object>> getReferencedPMRs()
   {
      return referencedPMRs;
   }

   // _T155539A Unique PMR by name and RETAIN create date(/time)
   /**
    * Used during APAR subscribed-PMR processing to communicate unique
    * PMR reference data across various phases of operation participant
    * processing.
    * Returns a map of unique PMR name references to another multi-
    * purpose map of attribute keys used for RTC querying; the result
    * artifact of a pre-browse of a PMR - to omit redundant PMR browse
    * calls to SBS / RETAIN; and other various specialized keys.
    * Note: This WILL be set (not null) during this APAR function, and
    * will NOT be set for the "create PMR that exists in RETAIN" action
    * issued from the RTC UI.
    * 
    * @param referencedPMRs
    */
   public void setReferencedPMRs(
         HashMap<String, HashMap<String, Object>> referencedPMRs )
   {
      this.referencedPMRs = referencedPMRs;
   }

   // _T155539A Unique PMR by name and RETAIN create date(/time)
   /**
    * Used for nested PMR handling to propagate PMR references from a
    * deeper level back to the references of a higher level to attempt
    * to prevent redundancy at all levels of an RTC action.
    * 
    * @param sourceCommonData - to get PMR references FROM.
    * @param replaceExistingEntry - true to overwrite a PMR reference
    *        that already exists.
    */
   public void propagateReferencedPMRs(
         SportCommonData sourceCommonData,
         Boolean replaceExistingEntry )
   {
      HashMap<String, HashMap<String, Object>> sourceReferencedPMRs = sourceCommonData
            .getReferencedPMRs();
      HashMap<String, HashMap<String, Object>> targetReferencedPMRs = this
            .getReferencedPMRs();
      if (sourceReferencedPMRs != null)
      {
         // Copy any PMR unique key data that may have been set -
         for (String pmrReferenceKey : sourceReferencedPMRs.keySet())
         {
            HashMap<String, Object> sourcePMRUniqueKeys = sourceReferencedPMRs
                  .get( pmrReferenceKey );
            if (sourcePMRUniqueKeys != null)
            {
               // Source entry defined, check target
               if (targetReferencedPMRs == null
                     || targetReferencedPMRs.get( pmrReferenceKey ) == null
                     || replaceExistingEntry)
               {
                  if (targetReferencedPMRs == null)
                  {
                     // First reference at higher level -
                     targetReferencedPMRs = new HashMap<String, HashMap<String, Object>>();
                     this.setReferencedPMRs( targetReferencedPMRs );
                  }
                  // Propagate source reference data to target -
                  targetReferencedPMRs.put( pmrReferenceKey, sourcePMRUniqueKeys );
               }
            }
         }
      }
   }
}
