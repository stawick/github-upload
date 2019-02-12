package com.ibm.sport.rtc.common;

import org.apache.commons.logging.Log;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.ArtifactTechnology.common.Constants;
import com.ibm.team.repository.common.LogFactory;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.model.IWorkItem;

public class CommonUtils
{
   public static String getSbsSavedAttributeId( String workItemType )
   {
      return CommonConstants.SBS_SAVE_ATTRIBUTE_ID.replace(
            "${workItemType}", workItemType.toLowerCase() );
   }
   
   public static String getActionInitiatorAttributeId( String workItemType )
   {
      return CommonConstants.ACTION_INITIATOR_ATTRIBUTE_ID.replace(
            "${workItemType}", workItemType.toLowerCase() );
   }
   
   // _T26063A - Workaround for Required/Read-Only bypass
   public static String getSPoRTInternalBypassAttributeId( String workItemType )
   {
      return CommonConstants.SPORT_INTERNAL_BYPASS_ATTRIBUTE_ID.replace(
            "${workItemType}", workItemType.toLowerCase() );
   }

   // _B93726A Get initial work item state additional save parameter name.
   public static String getRTCAdditionalSaveInitialWorkItemStateParmAttributeId( String workItemType )
   {
      return CommonConstants.RTCSPORT_ADDITIONAL_SAVE_INITIAL_STATE_ATTRIBUTE_ID.replace(
            "${workItemType}", workItemType.toLowerCase() );
   }

   /**
    * _T24561A 
    * Record the use of the component in the work item for the project area. 
    * 
    * @param sportCommonData
    * @param sportWorkItem
    */
   public static void registerComponentUse( 
         SportCommonData sportCommonData,
         IWorkItem sportWorkItem )
   {
      // _T24561A
      // Now cache the use of this component for this project area
      // for component list retrieval.
      // In the off chance that a component browse is done before
      // project area components are retrieved, this will retrieve
      // the project area components from the process configuration.
      String browseComponentValue = getWorkItemComponent( sportWorkItem,
            sportCommonData );
      if (browseComponentValue != null && browseComponentValue.length() > 0)
      {
         ComponentBrowseCache.recordComponentUse( sportCommonData,
               browseComponentValue );
      }
   }
   
   /**
    * _T24561A 
    * Based on work item type, return component. 
    * Use the (updated) working copy.
    * 
    * @param sportWorkItem
    * @param sportCommonData
    * @return
    */
   public static String getWorkItemComponent( 
         IWorkItem sportWorkItem,
         SportCommonData sportCommonData )
   {
      String browseComponentValue = null;
      IWorkItem workingCopy = sportCommonData.getWorkItemForUpdate();
      try
      {
         if (workingCopy.getWorkItemType()
               .equals( AparConstants.WORK_ITEM_TYPE_ID ))
         {
            // SPoRT APAR work item, get its component
            browseComponentValue = RtcUtils.getAttributeValueAsString( workingCopy,
                  AparConstants.COMPONENT_ATTRIBUTE_ID, sportCommonData );
         }
         if (workingCopy.getWorkItemType()
               .equals( PmrConstants.WORK_ITEM_TYPE_ID ))
         {
            // SPoRT PMR work item, get its component
            browseComponentValue = RtcUtils.getAttributeValueAsString( workingCopy,
                  PmrConstants.COMPONENT_ID_ATTRIBUTE_ID, sportCommonData );
         }
         if (workingCopy.getWorkItemType()
               .equals( PtfConstants.WORK_ITEM_TYPE_ID ))
         {
            // SPoRT PTF work item, get its component
            // Component might not be used on create
            browseComponentValue = RtcUtils.getAttributeValueAsString( workingCopy,
                  PtfConstants.REPORTED_COMP_CHAR_ATTRIBUTE_ID, sportCommonData );
         }
      }
      catch (Exception e)
      {
         // On any exception, simply return empty
         // Attempt to log the exception at least
         Log log = sportCommonData.getLog();
         String errMsg = "Problem encountered obtaining component attribute "
            + "from work item. "
            + Constants.NEWLINE
            + e.getMessage();
         log.warn( errMsg );

         return null;
      }
      return browseComponentValue;
   }

   /** 
    * Method to get the data object for SBS usage in a limited context.
    * This omits these elements, which could be set later...<br>
    * - QueryCommon<br>
    * - Link Service<br>
    * - Apar names<br>
    * - Auto Receipt<br>
    * - Report Info Collector<br>
    * etc.
    * 
    * @param workItem
    * @param workItemCommon
    * @param packageNameForLog
    * @param monitor
    * @return
    */
   public static SportCommonData getLimitedUseSPoRTDataObject( IWorkItem workItem,
         IWorkItemCommon workItemCommon,
         String packageNameForLog,
         // IConfiguration configuration,
         IProgressMonitor monitor )
   {
      // Trying to reuse SPoRT operation participant server utilities
      // in this function to do SBS calls for RETAIN component browse
      // and other similar functions.
      // Prepare the SportCommonData definition needed to use SPoRT utilities.
      SportCommonData sportCommonData = new SportCommonData();
      // AuditableCommon - from WorkItemCommon
      sportCommonData
            .setAuditableCommon( workItemCommon.getAuditableCommon() );
      // Just get the log from the class / package name
      // Log log = LogFactory.getLog( this.getClass().getPackage().getName() );
      if (packageNameForLog != null && packageNameForLog.length() > 0)
      {
         Log log = LogFactory.getLog( packageNameForLog );
         sportCommonData.setLog( log );
      }
      // Monitor - from argument
      sportCommonData.setMonitor( monitor );
      // ProjectArea - from workItem
      sportCommonData.setProjectArea( workItem.getProjectArea() );

      // N/A QueryCommon - 
      // This is used to query RTC.
      // Assumption: In the context this will be used, 
      // could probably get away with an empty object, or could be set
      // later on by user.
      
      // WorkItemCommon - from argument
      sportCommonData.setWorkItemCommon(workItemCommon);
      
      // N/A WorkItemServer - NOT USED by SPoRTCommonData
      /*
      // Also N/A - 
      ILinkService linkService
      ArrayList<SysrouteAparInfo> newAparNames2
      String aparToAutoReceipt
      ISportReportInfoCollector reportInfoCollector
      */

      // N/A SbsInterfaceConfigurationData - 
      // It appears in the latest SPoRT internals, RtcUtils now obtains
      // the needed SPoRT configuration info from the process
      // configuration. It uses the common data auditable-common and
      // project area components. No need to gather that specific data
      // here anymore. Its no longer a part of this object.

      return sportCommonData;
   }

   /**
    * _F155269A SPA FIXCAT, Preventative Service Program (PSP)
    * For any java extension filtered value set provider.
    * Insure filter is in usable regex format.
    * Eliminate need for ^ and $, and allow simpler "*" usage.
    * (ie sole "*" === ".*")
    * Do the same for optional characters (ie sole "?" === ".?")
    * Always add ".*" after input filter to allow a type-ahead kind of
    * usability feature. Unless user adds $ for regex end of string.
    * Those who know regex can use those features.
    * Assumption: Values for choices will not contain special regex
    * characters, so users should not be filtering those kinds of
    * values (ex NOT searching for "*" or "?", etc in content).
    * 
    * @param filter
    * @return
    */
   public static String processFilterToRegex( String filter )
   {
      String processedFilter = filter.trim();
      // Insure filter -> "^<regex>$"
      if (processedFilter.length() == 0)
         return processedFilter;
      if (!(filter.trim().startsWith( "^" )))
         processedFilter = "^" + filter;

      // Some people may shortcut and just use *<content>*,
      // convert to .* for regex use (just this case)
      // Note: Component can only be 8 chars, so placeholder
      // of 8 "@"'s are used.
      // For those that know regex ... use a placeholder
      processedFilter = processedFilter.replaceAll( "\\.\\*", "@@@@@@@@" );
      // For those that dont - substitute value that'll work
      processedFilter = processedFilter.replaceAll( "\\*", ".*" );
      // Replace correct regex value
      processedFilter = processedFilter.replaceAll( "@@@@@@@@", ".*" );
      // Some people may shortcut and just use ?<content>?,
      // convert to .? for regex use (just this case)
      // For those that know regex ... use a placeholder
      processedFilter = processedFilter.replaceAll( "\\.\\?", "@@@@@@@@" );
      // For those that dont - substitute value that'll work
      processedFilter = processedFilter.replaceAll( "\\?", ".?" );
      // Replace correct regex value
      processedFilter = processedFilter.replaceAll( "@@@@@@@@", ".?" );
      // User didnt specify regex end of string then
      // modify filter to match input + anything after - a kind of
      // type-ahead (for usability perhaps).

      // Insure filter -> "^<regex>$"
      if (!(filter.trim().endsWith( "$" )))
         processedFilter += ".*$";

      return processedFilter;
   }

   // _T15551A PMR ARCHIVED state
   /**
    * Each date time argument of the form:<br>
    * yyyy{"/"|"-"}MM{"/"|"-"}dd[{space}HH":"mm[":"ss.s]]<br>
    * Homogenize the formats, then test. May need to drop the final
    * seconds test, or even time test if not in both arguments.
    *   
    * @param dateTime1
    * @param dateTime2
    * @return
    */
   public static Boolean dateTimesMatch( String dateTime1, String dateTime2 )
   {
      // Knock out indeterminable stuff -
      if (dateTime1 == null || dateTime1.length() == 0 || dateTime2 == null
            || dateTime2.length() == 0)
         return false;
      String processedDT1 = dateTime1;
      String processedDT2 = dateTime2;
      // Simply remove the date element separators.
      processedDT1 = processedDT1.replaceAll( "/", "" );
      processedDT1 = processedDT1.replaceAll( "-", "" );
      processedDT2 = processedDT2.replaceAll( "/", "" );
      processedDT2 = processedDT2.replaceAll( "-", "" );
      String dtSplit1[] = processedDT1.split( " " );
      String dtSplit2[] = processedDT2.split( " " );
      // Optional time -
      String time1 = dtSplit1.length > 1 ? dtSplit1[1] : "";
      String time2 = dtSplit2.length > 1 ? dtSplit2[1] : "";
      // Optional seconds -
      String timeSegments1[] = time1.split( ":" );
      String timeSegments2[] = time2.split( ":" );
      // Reconstruct test date times - start with date
      processedDT1 = dtSplit1[0];
      processedDT2 = dtSplit2[0];
      // Append the common time elements
      for (int timeIx = 0; (timeIx < timeSegments1.length && timeIx < timeSegments2.length); timeIx++)
      {
         processedDT1 += timeSegments1[timeIx];
         processedDT2 += timeSegments2[timeIx];
      }
      // Now tests yyyyMMdd[HHmm[ss.s]].
      // Optional parts tested only if both have them.
      return processedDT1.equals( processedDT2 );
   }
}
