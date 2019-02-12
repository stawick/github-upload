package com.ibm.sport.rtc.common;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.ArtifactTechnology.common.Artifact;
import com.ibm.ArtifactTechnology.common.ArtifactFieldValue;
import com.ibm.ArtifactTechnology.common.ArtifactRecordField;
import com.ibm.ArtifactTechnology.common.ArtifactRecordPart;
import com.ibm.ArtifactTechnology.common.Constants;
import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.expression.IQueryableAttribute;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.query.IQueryResult;
import com.ibm.team.workitem.common.query.IResolvedResult;

public class PmrUtils
{

   private static Map<String, Long> activePmrSaves = new HashMap<String, Long>();
   private final static long PMR_SAVE_TIMEOUT = 15 * 60 * 1000; // 15 minutes

   // Create thread for cleaning up active PMR saves that fail in a non-SPoRT
   // advisor or participant.
   static
   {
      Thread thread = new Thread( new Runnable()
      {
         public void run()
         {
            Log log = LogFactory.getLog( PmrUtils.class );
            while (true)
            {
               try
               {
                  Thread.sleep( 1000 );
               }
               catch (InterruptedException e)
               {
                  // do nothing
               }
               synchronized (activePmrSaves)
               {
                  boolean notifyAllNeeded = false;
                  Iterator<Map.Entry<String, Long>> iterator = activePmrSaves
                        .entrySet().iterator();
                  while (iterator.hasNext())
                  {
                     Map.Entry<String, Long> activePmrSave = iterator.next();
                     long createTime = activePmrSave.getValue().longValue();
                     if ((System.currentTimeMillis()
                           - createTime) >= PMR_SAVE_TIMEOUT)
                     {
                        iterator.remove();
                        notifyAllNeeded = true;
                        String pmrName = activePmrSave.getKey();
                        String message = "Removing " + pmrName
                              + " from active PMR saves due to timeout";
                        log.warn( message );
                     }
                  }
                  if (notifyAllNeeded)
                  {
                     activePmrSaves.notifyAll();
                  }
               }
            }
         }
      } );
      thread.start();
   }

   /**
    * This duplicate check seems to be involved on a PMR Create action when
    * the PMR name was supplied for the work item in the RTC UI. One place
    * this applies is a create of an RTC work item for a PMR that exists in
    * RETAIN. In this case, this needs to call RETAIN via the SBS and
    * pre-browse and pre-fill in the work item for other elements to be used
    * as the key to whether this RTC work item already exists. So it incurs
    * the overhead up front in the work item save operation advisor, which
    * might ultimately be disposed. <br>
    * The overhead also involves constructing the call list, which involves
    * call browse overhead for each call. A thought was given to reduce
    * overhead and extract this and leave it until the create was allowed.
    * However, that requires use of a RETAIN PMR artifact record, which means
    * another SBS call, which would save nothing. So it remains included here,
    * up-front.
    * 
    * @param actions
    * @param pmr
    * @param sportCommonData
    * @throws SportUserActionFailException
    * @throws TeamRepositoryException
    */
   public static void performDuplicatePmrCheck( String[] actions,
         IWorkItem pmr, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      // _T155539A Unique PMR by name and RETAIN create date(/time)
      // Map of attribute name : value keys (for findPmr RTC query).
      // Values need to be in generic Object super class format for
      // RTC query conditions.
      // Also store 2 special KEY String values for error messaging
      // and activePmrSaves map indexing.
      HashMap<String, Object> pmrUniqueKeys = new HashMap<String, Object>();
      // _T155539A Should use working copy, which may get modified...
      IWorkItem pmrToUpdate = sportCommonData.getWorkItemForUpdate();
      pmrToUpdate = (pmrToUpdate == null) ? pmr : pmrToUpdate;

      SbsUtils.checkForModfyAndChangeStateAction( actions,
            PmrConstants.MODIFY_ACTION_ATTRIBUTE_ID, sportCommonData );
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         if ((actionParts.length == 2) && actionParts[0].equals( "action" ))
         {
            String actionName = actionParts[1];
            if (actionName.equals( PmrConstants.RTC_CREATE_ACTION ))
            {
               // _T155539A Unique PMR by name and RETAIN create
               // date(/time)
               boolean pmrInRetain = RtcUtils.getAttributeValueAsBoolean(
                     pmrToUpdate, PmrConstants.PMR_IN_RETAIN_ATTRIBUTE_ID,
                     sportCommonData );
               String pmrName = RtcUtils.getAttributeValueAsString(
                     pmrToUpdate, PmrConstants.PMR_NAME_ATTRIBUTE_ID,
                     sportCommonData );

               /*
                * Note: When SPoRT creates and saves an internal PMR work item
                * for op-participant APAR subscribed PMR function, the PMR
                * work item has already gone thru the browse / update process
                * and is populated (and has a pmrOpenDate). The in-RETAIN
                * indicator gets cleared prior to an internal save. A
                * pre-browse here becomes redundant. Also, the common data
                * here is a fresh new object and has no stored PMR reference
                * data. Perhaps assume having more than just the PMR name
                * indicates a browse was done. Since the initiation of the PMR
                * internal save is from op-participant function, that internal
                * save might skip additional RTC (create) action handling and
                * just do a basic save to the RTC project area - thus avoiding
                * yet another PMR browse. However, this op-advisor duplicate
                * check might still be in effect, depending on stack-trace
                * content. (*TBD via UT*) Unsure if there is even a CREATE
                * action in this case, or a basic work item save / update?
                * (*TBD via UT*) If it is a CREATE, might be a known internal
                * save and bypass op-participant RTC action processing? (*TBD
                * via UT, my suspicion = this is the case*) For SBS-bridging,
                * the action performer will create a PMR work item, prime it
                * with bridged artifact data, and save with a Create action.
                * In this case, the in-RETAIN indicator will NOT be set, but
                * the create date will be in the work item (as well as PMR
                * name) for duplicate checking.
                */

               // Get from possibly updated working copy ...
               // String form of value, if defined -
               String retainCreateDate = RtcUtils.getAttributeValueAsString(
                     pmrToUpdate, PmrConstants.OPEN_DATE_ATTRIBUTE_ID,
                     sportCommonData );
               if ((pmrName != null) && !pmrName.trim().equals( "" ))
               {
                  ArrayList<String> pmrIds = splitPmrName( pmrName );
                  String cleanPmrName = new String();
                  if (pmrIds.size() == 3)
                  {
                     cleanPmrName = cleanPmrName + pmrIds.get( 0 );
                     cleanPmrName = cleanPmrName + ",";
                     cleanPmrName = cleanPmrName + pmrIds.get( 1 );
                     cleanPmrName = cleanPmrName + ",";
                     cleanPmrName = cleanPmrName + pmrIds.get( 2 );
                     // _T155539A Prime PMR work item for RETAIN browse
                     // if internal PMR create did not prime with
                     // create date yet (ie create via RTC UI).
                     // Will have to presume if create date is set,
                     // work item already populated (ie for internal
                     // create or SBS bridging).
                     // Might skip RTC op-participant create action for
                     // internal create or SBS save due to internal
                     // save or SBS recognition and interception.
                     if (pmrInRetain && (retainCreateDate == null
                           || retainCreateDate.length() == 0))
                     {
                        // We have to set up the PMR work item to do a
                        // browse from.
                        IAttribute pmrNoAttribute = RtcUtils.findAttribute(
                              pmrToUpdate, PmrConstants.PMR_NO_ATTRIBUTE_ID,
                              sportCommonData );
                        RtcUtils.setAttributeValue( pmrToUpdate,
                              pmrNoAttribute, (String)pmrIds.get( 0 ),
                              sportCommonData );
                        IAttribute branchAttribute = RtcUtils.findAttribute(
                              pmrToUpdate, PmrConstants.BRANCH_ATTRIBUTE_ID,
                              sportCommonData );
                        RtcUtils.setAttributeValue( pmrToUpdate,
                              branchAttribute, (String)pmrIds.get( 1 ),
                              sportCommonData );
                        IAttribute countryAttribute = RtcUtils.findAttribute(
                              pmrToUpdate, PmrConstants.COUNTRY_ATTRIBUTE_ID,
                              sportCommonData );
                        RtcUtils.setAttributeValue( pmrToUpdate,
                              countryAttribute, (String)pmrIds.get( 2 ),
                              sportCommonData );
                        // _T155539A In this case - create where we
                        // have a PMR name, it should be a Create in
                        // RETAIN action from within RTC. Browse and
                        // update the PMR work item here to make the
                        // decision whether to continue the save of the
                        // action (if it is a duplicate or not).
                        // This call will invoke RETAIN via SBS and
                        // populate the PMR with the RETAIN results.
                        // The work item can then be used to cull
                        // RETAIN values for any decision making
                        // whether to continue. This can either be
                        // discarded (wasted SBS action), or continue,
                        // but a redundant RETAIN call should be
                        // bypassed.
                        // For performance, a consideration was given
                        // to skip call list generation here
                        // (call browses to RETAIN), but do them later
                        // if work item save not bypassed. However,
                        // this would require a RETAIN PMR artifact
                        // record to construct call list from, which
                        // would necessitate a redundant SBS RETAIN
                        // call.
                        // There is no Draft PMR to be concerned about!

                        // Send working copy to be updated.
                        updatePmrFromRetain( pmrToUpdate, sportCommonData );

                        // Get from possibly updated working copy ...
                        // String form of value, if defined -
                        retainCreateDate = RtcUtils.getAttributeValueAsString(
                              pmrToUpdate,
                              PmrConstants.OPEN_DATE_ATTRIBUTE_ID,
                              sportCommonData );
                     }
                  }
                  else
                     cleanPmrName = pmrName;
                  // _T155539A Unique PMR by name and RETAIN
                  // create date(/time)
                  // Add PMR name as key 1. Upper cased form for
                  // correct RTC query match.
                  pmrUniqueKeys.put( PmrConstants.PMR_NAME_ATTRIBUTE_ID,
                        cleanPmrName.toUpperCase( Locale.ENGLISH ) );
                  // _B200155C Improve duplicate PMR error msg.
                  setActivePMRSaveKey( pmrUniqueKeys,
                        // PmrConstants.PMR_NAME_ATTRIBUTE_ID,
                        "Name", cleanPmrName );

                  if (retainCreateDate != null
                        && retainCreateDate.length() > 0)
                  {
                     // Set create-date/time as key 2.
                     // Values need to be in generic Object super
                     // class format for RTC query. Further depth
                     // methods will not have work item to get String
                     // values from. Construct special String keys.
                     IAttribute pmrAttr = RtcUtils.findAttribute( pmrToUpdate,
                           PmrConstants.OPEN_DATE_ATTRIBUTE_ID,
                           sportCommonData );
                     if (pmrAttr != null)
                     {
                        pmrUniqueKeys.put(
                              PmrConstants.OPEN_DATE_ATTRIBUTE_ID,
                              pmrToUpdate.getValue( pmrAttr ) );
                        // _B200155C Improve duplicate PMR error msg.
                        setActivePMRSaveKey( pmrUniqueKeys,
                              // PmrConstants.OPEN_DATE_ATTRIBUTE_ID,
                              "Created Date", retainCreateDate );
                     }
                  }
                  // _T155539C Send name, date/time as keys
                  // Work item no longer referenced in call-hierarchy
                  // that follows.
                  performDuplicatePmrCheck( pmrUniqueKeys, sportCommonData );
               }
            }
         }
      }
   }

   // _T155539A Unique PMR by name and RETAIN create date(/time)
   /**
    * Builds a special pair of keys in hash map. Adds entries as specified:
    * <br>
    * {@code
    * ***PMRMessagingKey*** : 
    * <(external)attribute-name1> <space> <attribute-value1>[, <space>
    * <(external)attribute-name2> <space> <attribute-value2>] ...
    * } <br>
    * - suitable for a message insert. <br>
    * {@code
    * ***ActivePMRSaveKey*** : <attr-value1>[_<attr-value2>] ...
    * }
    * 
    * @param pmrUniqueKeys
    * @param attributeName - User can specify an externalized name for
    *        messaging if necessary.
    * @param attributeValueAsString
    */
   public static void setActivePMRSaveKey(
         HashMap<String, Object> pmrUniqueKeys, String attributeName,
         String attributeValueAsString )
   {
      String pmrMsgKey = "***PMRMessagingKey***";
      String activePmrSaveKey = "***ActivePMRSaveKey***";
      // Messaging collector. Assume a String value.
      String pmrMsgKeyValue = getPMRMessageKeyValue( pmrUniqueKeys );
      // _B200155C Improve duplicate PMR error msg.
      if (pmrMsgKeyValue.length() > 0)
         pmrMsgKeyValue += ", "; // Separator
      // <attribute-name> <value> for messaging
      pmrMsgKeyValue += attributeName + " " + attributeValueAsString;
      pmrUniqueKeys.put( pmrMsgKey, pmrMsgKeyValue );

      // Key collector. Assume a String value.
      String activePmrSaveKeyValue = getActivePMRSaveKeyValue(
            pmrUniqueKeys );
      if (activePmrSaveKeyValue.length() > 0)
         activePmrSaveKeyValue += "_"; // Simple separator
      // <value1>_<value2>... as a key.
      activePmrSaveKeyValue += attributeValueAsString;
      pmrUniqueKeys.put( activePmrSaveKey, activePmrSaveKeyValue );
   }

   // _T155539A Unique PMR by name and RETAIN create date(/time)
   /**
    * Returns a special String value in hash map. <br>
    * {@code
    * ***PMRMessagingKey*** : 
    * <(external)attribute-name1> <space> <attribute-value1>[, <space>
    * <(external)attribute-name2> <space> <attribute-value2>] ...
    * } <br>
    * - suitable for a message insert.
    * 
    * @param pmrUniqueKeys
    */
   public static String getPMRMessageKeyValue(
         HashMap<String, Object> pmrUniqueKeys )
   {
      String pmrMsgKey = "***PMRMessagingKey***";
      // Messaging collector. Assume a String value.
      String pmrMsgKeyValue = (String)(pmrUniqueKeys.get( pmrMsgKey ));
      // <attribute-name> <value> elements for messaging
      return ((pmrMsgKeyValue == null) ? "" : pmrMsgKeyValue);
   }

   // _T155539A Unique PMR by name and RETAIN create date(/time)
   /**
    * Returns a special String value in hash map. <br>
    * {@code
    * ***ActivePMRSaveKey*** : <attr-value1>[_<attr-value2>] ...
    * }
    * 
    * @param pmrUniqueKeys
    */
   public static String getActivePMRSaveKeyValue(
         HashMap<String, Object> pmrUniqueKeys )
   {
      String activePmrSaveKey = "***ActivePMRSaveKey***";
      // Key collector. Assume a String value.
      String activePmrSaveKeyValue = (String)(pmrUniqueKeys
            .get( activePmrSaveKey ));
      // <value1>_<value2>... as a key.
      return ((activePmrSaveKeyValue == null) ? "" : activePmrSaveKeyValue);
   }

   /**
    * Extract the pmrOpenDate attribute value (as String) from the work item
    * (if provided), or from the referenced PMR structure if it is there. <br>
    * - Stored work item <br>
    * - Stored PMR browse artifact
    * 
    * @param pmrName
    * @param workItem
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static String extractRETAINCreateDate( String pmrName,
         IWorkItem workItem, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String retainCreateDate = null;
      if (workItem != null)
      {
         retainCreateDate = RtcUtils.getAttributeValueAsString( workItem,
               PmrConstants.OPEN_DATE_ATTRIBUTE_ID, sportCommonData );
      }
      // Found in work item, return it.
      if (retainCreateDate != null && retainCreateDate.length() > 0)
      {
         return retainCreateDate;
      }

      // Not in work item argument, see if we stored it already
      HashMap<String, HashMap<String, Object>> referencedPMRs = sportCommonData
            .getReferencedPMRs();
      if (referencedPMRs == null || pmrName == null || pmrName.length() == 0)
      {
         // Not going to find it elsewhere -
         return retainCreateDate;
      }
      HashMap<String, Object> pmrUniqueKeys = referencedPMRs
            .get( pmrName.toUpperCase( Locale.ENGLISH ) );
      if (pmrUniqueKeys == null)
      {
         // Not going to find it elsewhere -
         return retainCreateDate;
      }
      // See if we already did a query for this work item in the
      // project area -
      IWorkItem rtcPMRworkitem = (IWorkItem)pmrUniqueKeys
            .get( "***RTCPMRWorkItem***" );
      if (rtcPMRworkitem != null)
      {
         retainCreateDate = RtcUtils.getAttributeValueAsString(
               rtcPMRworkitem, PmrConstants.OPEN_DATE_ATTRIBUTE_ID,
               sportCommonData );
      }
      // Found in stored work item, return it.
      if (retainCreateDate != null && retainCreateDate.length() > 0)
      {
         return retainCreateDate;
      }

      // Not found in stored work item, try a browsed artifact -
      // See if we already browsed this PMR in RETAIN via SBS -
      Artifact browsedPMR = (Artifact)pmrUniqueKeys
            .get( "***PMRBrowseSBSResultArtifact***" );
      if (browsedPMR != null)
      {
         // Obtain values from Artifact -
         ArtifactRecordPart pmrRecord = browsedPMR
               .findRecord( PmrConstants.WORK_ITEM_TYPE_ID );
         if (pmrRecord != null)
         {
            retainCreateDate = pmrRecord
                  .findFieldValue( PmrConstants.OPEN_DATE_ATTRIBUTE_ID );
         }
      }

      return retainCreateDate;
   }

   // _T155539C Unique PMR by name and RETAIN create date(/time)
   // Changed argument to be a map of key elements, such as PMR name
   // and create date (ex ppppp,bbb,ccc and YYYY-MM-DD{ HH:mm:SS.s}?).
   /**
    * Given a map of attribute names : values, determine if a PMR work item
    * already exists that matches those parameters. Also, determine if an
    * existing save is in progress for such a work item. Indicate an active
    * save is pending if this is a new work item.
    * 
    * @param pmrUniqueKeys
    * @param sportCommonData
    * @throws SportUserActionFailException
    * @throws TeamRepositoryException
    */
   public static void performDuplicatePmrCheck(
         HashMap<String, Object> pmrUniqueKeys,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      // _T155539A Unique PMR by name and RETAIN create date(/time)
      // Messaging collector. Assume a String value.
      String pmrMsgKeyValue = getPMRMessageKeyValue( pmrUniqueKeys );
      // Key collector. Assume a String value.
      String activePmrSaveKeyValue = getActivePMRSaveKeyValue(
            pmrUniqueKeys );

      // Attribute name : value entries in map.
      synchronized (activePmrSaves)
      {
         if (findPmr( pmrUniqueKeys, sportCommonData ) != null)
         {
            /*
             * String message = pmrName +
             * " already exists in the RTC repository";
             */
            // _T155539C Unique PMR by name and RETAIN create
            // date(/time)
            String message = "PMR " + pmrMsgKeyValue
                  + " already exists in the RTC repository.";
            throw new SportUserActionFailException( message );
         }
         else
         {
            // _T155539C Unique PMR by name and RETAIN create
            // date(/time)
            if (activePmrSaves.containsKey( activePmrSaveKeyValue ))
            {
               // String message = pmrName + " save currently in progress";
               // _T155539C Unique PMR by name and RETAIN create
               // date(/time)
               String message = "PMR " + pmrMsgKeyValue
                     + " save currently in progress.";
               throw new SportUserActionFailException( message );
            }
            else
            {
               // _T155539C Unique PMR by name and RETAIN create
               // date(/time)
               activePmrSaves.put( activePmrSaveKeyValue,
                     new Long( System.currentTimeMillis() ) );
            }
         }
      }
   }

   // _T155539C Unique PMR by name and RETAIN create date(/time)
   // Key may now include PMR name and create date.
   // Format = <name>_<date>.
   public static void removeActivePmrSave( String pmrKey )
   {
      synchronized (activePmrSaves)
      {
         Long value = activePmrSaves.remove( pmrKey );
         if (value != null)
         {
            activePmrSaves.notifyAll();
         }
      }
   }

   // _T155546A Test if action is to be performed on a PMR ACTIVE in
   // RETAIN.
   /**
    * Test if primary action (state-changing or modify) is to be performed on
    * a PMR ACTIVE in RETAIN. Pre-browse (overhead) PMR in RETAIN to get
    * pmrOpenDate (create). Test against work item pmrOpenDate. <br>
    * Utilizes {@link PmrConstants.ACTIVE_PMR_ACTIONS} list.
    * 
    * @param actions
    * @param pmr
    * @param sportCommonData
    * @throws SportUserActionFailException
    * @throws TeamRepositoryException
    */
   public static void activePmrCheck( String[] actions, IWorkItem pmr,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      boolean activePmrRequired = false;
      String actionName = "";
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         String actionType = actionParts[0];
         // There may be many "modify's", just want the actual SPoRT
         // modify action chosen - or state-changing "action".
         if ((actionParts.length == 2) && (actionType.equals( "action" )
               || actionType.equals( "modify" )))
         {
            actionName = actionParts[1];
            // Modify action? Grab SPoRT action name actually chosen.
            if (actionName.equals( PmrConstants.MODIFY_ACTION_ATTRIBUTE_ID ))
            {
               actionName = RtcUtils.getAttributeValueAsString( pmr,
                     PmrConstants.MODIFY_ACTION_ATTRIBUTE_ID,
                     sportCommonData );
            }
            // Stop when action found that requires an active PMR.
            if (PmrConstants.ACTIVE_PMR_ACTIONS.contains( actionName ))
            {
               activePmrRequired = true;
               // Exit with the determined "target" action name set.
               break;
            }
         }
      }

      if (activePmrRequired)
      {
         // For any subject action, PMR name ought to be set in work
         // item - use it for active PMR lookup.
         // 1st Pre-Browse to see if this PMR is ACTIVE in RETAIN.
         // Only allow if RTC PMR work item is the currently active PMR
         // in RETAIN.
         Artifact retainPmrArtifact = findPmrInRETAIN( pmr, sportCommonData );
         // Get PMR name if needed for log messages.
         String pmrName = RtcUtils.getAttributeValueAsString( pmr,
               PmrConstants.PMR_NAME_ATTRIBUTE_ID, sportCommonData );
         if (retainPmrArtifact != null)
         {
            ArtifactRecordPart pmrRecord = retainPmrArtifact
                  .findRecord( pmr.getWorkItemType() );
            // Only allow action on current work item if it is the
            // currently active PMR in RETAIN.
            String retainCreateDate = null;
            // Get the browsed RETAIN create date if PMR exists.
            ArtifactRecordField pmrArtifactOpenDate = pmrRecord
                  .findField( PmrConstants.OPEN_DATE_ATTRIBUTE_ID );
            if (pmrArtifactOpenDate != null
                  && pmrArtifactOpenDate.hasValues())
            {
               retainCreateDate = pmrArtifactOpenDate.getAllValuesAsString();
            }
            String rtcPmrOpenDate = RtcUtils.getAttributeValueAsString( pmr,
                  PmrConstants.OPEN_DATE_ATTRIBUTE_ID, sportCommonData );
            if (rtcPmrOpenDate == null || retainCreateDate == null)
            {
               // Could not make a determination.
               String errMsg = "Could not determine if PMR is currently active in RETAIN."
                     + Constants.NEWLINE + " Cannot perform " + actionName
                     + " action on PMR " + pmrName + " (work item "
                     + String.valueOf( pmr.getId() ) + ").";
               throw new SportUserActionFailException( errMsg );
            }
            // if (!(rtcPmrOpenDate.equals( retainCreateDate )))
            if (!(CommonUtils.dateTimesMatch( rtcPmrOpenDate,
                  retainCreateDate )))
            {
               // Create date does NOT match PMR found in RETAIN.
               String errMsg = "PMR work item is not the currently active PMR in RETAIN."
                     + Constants.NEWLINE + " Cannot perform " + actionName
                     + " action on PMR " + pmrName + " (work item "
                     + String.valueOf( pmr.getId() ) + ").";
               throw new SportUserActionFailException( errMsg );
            }
            // If no exception thrown, the PMR is the currently active PMR in
            // RETAIN. Action may proceed.
         }
         else
         {
            // PMR was NOT found in RETAIN.
            String errMsg = "PMR work item is not the currently active PMR in RETAIN."
                  + Constants.NEWLINE + " Cannot perform " + actionName
                  + " action on PMR " + pmrName + " (work item "
                  + String.valueOf( pmr.getId() ) + ").";
            throw new SportUserActionFailException( errMsg );
         }
      } // End active PMR required.

   }

   public static void performPmrSavePostconditionActions( String[] actions,
         IWorkItem pmr, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         String actionType = actionParts[0];
         if ((actionParts.length == 2) && actionType.equals( "action" ))
         {
            String actionName = actionParts[1];
            if (actionName.equals( PmrConstants.RTC_CREATE_ACTION ))
            {
               createPmr( pmr, sportCommonData );
               // _T1083A Set summary - Handles RTC creates
               // Draft, Both RTC and RETAIN, RTC-only (in RETAIN)
               setPmrSummaryOnCreate( pmr, sportCommonData );
            }
            else if (actionName
                  .equals( PmrConstants.RTC_CREATE_IN_RETAIN_ACTION ))
            {
               createPmrInRetain( pmr, sportCommonData );
               // _T1083A Set summary - Handles RTC creates
               // Draft, Both RTC and RETAIN, RTC-only (in RETAIN)
               setPmrSummaryOnCreate( pmr, sportCommonData );
            }
            else if (actionName.equals( PmrConstants.RTC_REOPEN_ACTION ))
            {
               pmrReopen( pmr, sportCommonData );
            }
            else if (actionName.equals( PmrConstants.RTC_CLOSE_ACTION ))
            {
               pmrClose( pmr, sportCommonData );
            }
            else if (actionName.equals( PmrConstants.RTC_ARCHIVE_ACTION ))
            {
               // _T155551A ARCHIVED state
               pmrArchive( pmr, sportCommonData );
            }
         }
         else if ((actionParts.length == 2) && actionType.equals( "modify" ))
         {
            String fieldName = actionParts[1];
            if (fieldName.equals( PmrConstants.MODIFY_ACTION_ATTRIBUTE_ID ))
               performModifyAction( pmr, sportCommonData, actions );
         }
      }
   }

   public static void performModifyAction( IWorkItem pmr,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Log log = sportCommonData.getLog();
      String modifyAction = RtcUtils.getAttributeValueAsString( pmr,
            PmrConstants.MODIFY_ACTION_ATTRIBUTE_ID, sportCommonData );
      if (modifyAction.equals( PmrConstants.RTC_ENTER_MODIFY_ACTION_ACTION ))
      {
         String message = "Unexpected \""
               + PmrConstants.RTC_ENTER_MODIFY_ACTION_ACTION
               + "\" action encountered";
         log.warn( message );
         return;
      }

      if (modifyAction.equals( PmrConstants.RTC_REFRESH_FROM_RETAIN_ACTION ))
         updatePmrFromRetain( pmr, sportCommonData );
      else if (modifyAction.equals( PmrConstants.RTC_ADD_TEXT_ACTION ))
      {
         pmrAddText( pmr, sportCommonData );
      }
      else if (modifyAction.equals( PmrConstants.RTC_CALL_CREATE_ACTION ))
      {
         pmrCallCreate( pmr, sportCommonData );
      }
      else if (modifyAction.equals( PmrConstants.RTC_CALL_COMPLETE_ACTION ))
      {
         pmrCallComplete( pmr, sportCommonData, actions );
      }
      else if (modifyAction.equals( PmrConstants.RTC_CALL_DISPATCH_ACTION ))
      {
         // _T64529A PMR Call Dispatch
         pmrCallDispatch( pmr, sportCommonData );
      }
      else if (modifyAction.equals( PmrConstants.RTC_CALL_REQUEUE_ACTION ))
      {
         pmrCallRequeue( pmr, sportCommonData, actions );
      }
      else if (modifyAction.equals( PmrConstants.RTC_EDIT_FIELDS_ACTION ))
      {
         pmrEditFields( pmr, sportCommonData, actions );
      }
      else if (modifyAction.equals( PmrConstants.RTC_CALL_CONVERT_ACTION ))
      {
         pmrCallConvert( pmr, sportCommonData );
      }
      else if (modifyAction.equals( PmrConstants.RTC_UNARCHIVE_ACTION ))
      {
         // _T155551A ARCHIVED state
         // pmrUnarchive( pmr, sportCommonData );

         // _T155546M Active PMR test moved to a common pre-check.
         // This function will only be called for an active PMR.
         // (Although there may be a redundant browse here, but it is
         // not intended as a workload sensitive function. Pre-check
         // browse may have been done in operation advisor and cannot
         // be passed here.)
         // If no exception already thrown, the PMR is the currently
         // active PMR in RETAIN. Set state from RETAIN. (Modify
         // action.)
         // In the end a simple refresh is all that is needed.
         updatePmrFromRetain( pmr, sportCommonData );
      }
      else if (modifyAction
            .equals( PmrConstants.RTC_MARK_AS_SANITIZED_ACTION ))
      {
         markAsSanitized( pmr, sportCommonData );
      }
      else if (modifyAction
            .equals( PmrConstants.RTC_MARK_AS_NOT_SANITIZED_ACTION ))
      {
         markAsNotSanitized( pmr, sportCommonData );
      }
      else if (modifyAction.equals( PmrConstants.RTC_CLEAR_PI_DATA_ACTION ))
      {
         clearPIDataInPmr( pmr, sportCommonData );
      }
      else
      {
         String message = "Unexpected modify action \"" + modifyAction
               + "\" specified";
         throw new SportRuntimeException( message );
      }

      resetModifyAction( pmr, sportCommonData );
   }

   // PMR Add Text function
   public static void pmrAddText( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      // Create PMR Artifact
      Artifact artifact = SbsUtils.createArtifact( pmr,
            PmrConstants.SBS_ADD_TEXT_ACTION,
            PmrConstants.SBS_ADD_TEXT_ACTION_ATTRIBUTE_IDS, sportCommonData );

      SbsUtils.sendArtifacts( artifact, sportCommonData );

      IWorkItem pmrToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute attribute = RtcUtils.findAttribute( pmrToUpdate,
            PmrConstants.INPUT_TEXT_ATTRIBUTE_ID, sportCommonData );
      RtcUtils.setAttributeValue( pmrToUpdate, attribute, "",
            sportCommonData );

      updatePmrFromRetain( pmr, sportCommonData );
   }

   // _T155551A PMR Archive function
   /**
    * See if the PMR is really archived, or the current work item does not
    * match the active PMR in RETAIN. Allow archive transition if so.
    * 
    * @param pmr
    * @param sportCommonData
    * @throws SportUserActionFailException
    * @throws TeamRepositoryException
    */
   public static void pmrArchive( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      // 1st Browse PMR to see if it really is archived.
      // Only allow if error is thrown that it does not exist,
      // or not the currently active PMR in RETAIN.
      Artifact retainPmrArtifact = findPmrInRETAIN( pmr, sportCommonData );
      if (retainPmrArtifact != null)
      {
         ArtifactRecordPart pmrRecord = retainPmrArtifact
               .findRecord( pmr.getWorkItemType() );
         // Only allow ARCHIVE of current work item if NOT the
         // currently active PMR in RETAIN.
         String retainCreateDate = null;
         // Get the browsed RETAIN create date if PMR exists.
         ArtifactRecordField pmrArtifactOpenDate = pmrRecord
               .findField( PmrConstants.OPEN_DATE_ATTRIBUTE_ID );
         if (pmrArtifactOpenDate != null && pmrArtifactOpenDate.hasValues())
         {
            retainCreateDate = pmrArtifactOpenDate.getAllValuesAsString();
         }
         String rtcPmrOpenDate = RtcUtils.getAttributeValueAsString( pmr,
               PmrConstants.OPEN_DATE_ATTRIBUTE_ID, sportCommonData );
         if (rtcPmrOpenDate == null || retainCreateDate == null)
         {
            // Could not make a determination.
            String errMsg = "Could not determine if PMR is currently active in RETAIN. Cannot archive the PMR.";
            throw new SportUserActionFailException( errMsg );
         }
         // if (rtcPmrOpenDate.equals( retainCreateDate ))
         if (CommonUtils.dateTimesMatch( rtcPmrOpenDate, retainCreateDate ))
         {
            // Create date matches PMR found in RETAIN.
            String errMsg = "PMR is currently active in RETAIN. Cannot archive an active PMR.";
            throw new SportUserActionFailException( errMsg );
         }
      }
      // If no exception thrown, the PMR is NOT currently active in
      // RETAIN. Set state to ARCHIVED. Done by work flow.
   }

   // _T155551A ARCHIVED state
   /**
    * Browse RETAIN to find the PMR. Return the PMR artifact, if found. Return
    * null on the RETAIN not-found return code. Throw / percolate any other
    * exception.
    * 
    * @param pmr
    * @param sportCommonData
    * @return
    * @throws SportUserActionFailException
    * @throws TeamRepositoryException
    */
   public static Artifact findPmrInRETAIN( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Boolean saveIgnorePmr256 = sportCommonData.getIgnorePmr256();
      Artifact sbsResponse = null;
      try
      {
         // We want to know if the PMR exists or not.
         sportCommonData.setIgnorePmr256( Boolean.FALSE );
         Artifact artifact = SbsUtils.createArtifact( pmr,
               PmrConstants.SBS_BROWSE_ACTION,
               PmrConstants.SBS_BROWSE_ACTION_ATTRIBUTE_IDS,
               sportCommonData );
         sbsResponse = SbsUtils.sendArtifacts( artifact, sportCommonData );
         // When the PMR does not exist, sendArtifacts probes the
         // result artifact's response segment for a non-success.
         // If not successful, throws an SbsErrorException (subclass
         // of SportUserActionFailException). This can be caught and
         // tested for not-found RC=256.
         sportCommonData.setIgnorePmr256( saveIgnorePmr256 );
         return sbsResponse;
      }
      catch (SportUserActionFailException suafExcp)
      {
         // When the PMR does not exist, sendArtifacts probes the
         // result artifact's response segment for a non-success.
         // If not successful, throws an SbsErrorException (subclass
         // of SportUserActionFailException). This can be caught and
         // tested for not-found RC=256.
         // Handle the not-found error and return null.
         // Otherwise throw / percolate the exception.
         String response = suafExcp.getMessage();
         if (response.contains( "Return Code (256) " ))
         {
            // ignore and not throw exception
            sportCommonData.setIgnorePmr256( saveIgnorePmr256 );
            return null;
         }
         // Any other error - throw / percolate the exception.
         sportCommonData.setIgnorePmr256( saveIgnorePmr256 );
         throw suafExcp;
      }
      finally
      {
         // To be safe, restore original setting -
         sportCommonData.setIgnorePmr256( saveIgnorePmr256 );
      }
   }

   // _T155539A Unique PMR by name and RETAIN create date(/time)
   /**
    * Browse RETAIN to find the PMR, when only the PMR name is available.
    * Return the PMR artifact, if found. Return null on the RETAIN not-found
    * return code. Throw / percolate any other exception.
    * 
    * @param pmrName (PMR work item not utilized here)
    * @param sportCommonData
    * @return
    * @throws SportUserActionFailException
    * @throws TeamRepositoryException
    */
   public static Artifact findPmrInRETAIN( String pmrName,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Boolean saveIgnorePmr256 = sportCommonData.getIgnorePmr256();
      Artifact sbsResponse = null;
      try
      {
         // We want to know if the PMR exists or not.
         sportCommonData.setIgnorePmr256( Boolean.FALSE );
         // Construct explicit elements to pass.
         HashMap<String, String> pmrBrowseFields = new HashMap<String, String>();
         pmrBrowseFields.put( PmrConstants.PMR_NAME_ATTRIBUTE_ID,
               pmrName.toUpperCase( Locale.ENGLISH ) );
         if ((pmrName != null) && !pmrName.equals( "" ))
         {
            // Make sure we have a valid PMR name in expected format -
            ArrayList<String> pmrIds = PmrUtils
                  .extractNormalizedPmrName( pmrName );
            if (pmrIds != null)
            {
               pmrBrowseFields.put( PmrConstants.PMR_NO_ATTRIBUTE_ID,
                     pmrIds.get( 0 ) );
               pmrBrowseFields.put( PmrConstants.BRANCH_ATTRIBUTE_ID,
                     pmrIds.get( 1 ) );
               pmrBrowseFields.put( PmrConstants.COUNTRY_ATTRIBUTE_ID,
                     pmrIds.get( 2 ) );
            }
         }
         String artifactID = "SPoRT RTC internal "
               + PmrConstants.WORK_ITEM_TYPE_ID + " "
               + PmrConstants.SBS_BROWSE_ACTION + " for "
               + pmrBrowseFields.get( PmrConstants.PMR_NAME_ATTRIBUTE_ID );
         Artifact artifact = SbsUtils.createArtifactFromScratch(
               PmrConstants.WORK_ITEM_TYPE_ID, PmrConstants.SBS_BROWSE_ACTION,
               pmrBrowseFields, PmrConstants.PMR_CLASS, artifactID,
               sportCommonData );
         sbsResponse = SbsUtils.sendArtifacts( artifact, sportCommonData );
         // When the PMR does not exist, sendArtifacts probes the
         // result artifact's response segment for a non-success.
         // If not successful, throws an SbsErrorException (subclass
         // of SportUserActionFailException). This can be caught and
         // tested for not-found RC=256.
         sportCommonData.setIgnorePmr256( saveIgnorePmr256 );
         return sbsResponse;
      }
      catch (SportUserActionFailException suafExcp)
      {
         // When the PMR does not exist, sendArtifacts probes the
         // result artifact's response segment for a non-success.
         // If not successful, throws an SbsErrorException (subclass
         // of SportUserActionFailException). This can be caught and
         // tested for not-found RC=256.
         // Handle the not-found error and return null.
         // Otherwise throw / percolate the exception.
         String response = suafExcp.getMessage();
         if (response.contains( "Return Code (256) " ))
         {
            // ignore and not throw exception
            sportCommonData.setIgnorePmr256( saveIgnorePmr256 );
            return null;
         }
         // Any other error - throw / percolate the exception.
         sportCommonData.setIgnorePmr256( saveIgnorePmr256 );
         throw suafExcp;
      }
      finally
      {
         // To be safe, restore original setting -
         sportCommonData.setIgnorePmr256( saveIgnorePmr256 );
      }
   }

   // _T155539A Unique PMR by name and RETAIN create date(/time)
   /**
    * Given a (unique) PMR name, while not yet having a PMR work item in hand,
    * determine if the PMR exists in this RTC project area. An SBS call to
    * RETAIN is necessary to get the create date to query the specific PMR
    * work item in this project area. The SBS PMR browse result artifact will
    * be maintained to avoid a redundant PMR browse if/when the PMR work item
    * may need to be created later on. Redundancy insurance is also built in
    * for RTC queries for the PMR work item.
    * <p>
    * Moved to PmrUtils for reuse. (Note: Some of the arguments were copied
    * from the original caller and may be for future expansion.)
    * 
    * @param pmrName
    * @param pmrWIType // @param workItemForUpdate - not available
    * @param willCreatePmr - Is a PMR work item to be created by caller if not
    *        found in project area. (Active Saves Map entry "reserved" with
    *        timestamp.)
    * @param sportCommonData - Collects PMR query and SBS result data.
    *        // @param workItemServer - not used
    * @return
    * @throws TeamRepositoryException
    * @throws SportUserActionFailException
    */
   public static IWorkItem findPMRWorkItemInRTC( String pmrName,
         IWorkItemType pmrWIType, boolean willCreatePmr,
         SportCommonData sportCommonData )
         throws TeamRepositoryException, SportUserActionFailException
   {
      // Need to add a PMR browse to get the RETAIN PMR Open Date and
      // pass as a key.
      HashMap<String, HashMap<String, Object>> referencedPMRs = sportCommonData
            .getReferencedPMRs();
      // _T155539A Map of key attribute to object value for RTC query
      // for each PMR. With special entries -
      // a. PMR work item (once found) - to prevent redundant RTC
      // queries
      // b. PMR browse artifact - to prevent redundant PMR browses
      // c. active saves key
      // d. error message (String) key
      HashMap<String, Object> pmrUniqueKeys = null;
      IWorkItem rtcPMRworkitem = null;
      Artifact browsedPMR = null;

      if (referencedPMRs == null)
      {
         // Map of PMR name to multi-purpose map of PMR browse result
         // artifact, RTC query conditions, etc.
         referencedPMRs = new HashMap<String, HashMap<String, Object>>();
         // May need to be a globally referenced object. So save it.
         sportCommonData.setReferencedPMRs( referencedPMRs );
      }
      else
      {
         // See if entry already exists -
         pmrUniqueKeys = referencedPMRs
               .get( pmrName.toUpperCase( Locale.ENGLISH ) );
      }

      if (pmrUniqueKeys == null)
      {
         // 1st PMR reference
         pmrUniqueKeys = new HashMap<String, Object>();
         // Make a common practice to use the upper case PMR name as key.
         referencedPMRs.put( pmrName.toUpperCase( Locale.ENGLISH ),
               pmrUniqueKeys );
      }
      else
      {
         // See if we already did a query for this work item in the
         // project area -
         rtcPMRworkitem = (IWorkItem)pmrUniqueKeys
               .get( "***RTCPMRWorkItem***" );
      }
      // Wily PMR work item already "captured" / "tagged" ...
      // Release back into the wild - (prevents redundant RTC queries)
      // This should represent the active PMR in RETAIN.
      if (rtcPMRworkitem != null)
         return rtcPMRworkitem;

      // Did not query RTC for work item yet, need to do so -
      if (pmrUniqueKeys.get( PmrConstants.PMR_NAME_ATTRIBUTE_ID ) == null)
      {
         // PMR name not set yet
         pmrUniqueKeys.put( PmrConstants.PMR_NAME_ATTRIBUTE_ID,
               pmrName.toUpperCase( Locale.ENGLISH ) );
         // _B200155C Improve duplicate PMR error msg.
         PmrUtils.setActivePMRSaveKey( pmrUniqueKeys,
               // PmrConstants.PMR_NAME_ATTRIBUTE_ID,
               "Name", pmrName.toUpperCase( Locale.ENGLISH ) );
      }

      if (pmrUniqueKeys.get( PmrConstants.OPEN_DATE_ATTRIBUTE_ID ) == null)
      {
         // RETAIN create date not set yet, need to get it for query -

         // See if we already browsed this PMR in RETAIN via SBS -
         browsedPMR = (Artifact)pmrUniqueKeys
               .get( "***PMRBrowseSBSResultArtifact***" );

         /**
          * We have an APAR work item and a PMR name. We are looking for an
          * existing PMR work item. We also need the PMR Open Date, requiring
          * an SBS PMR browse, but do not have such a work item to drive reuse
          * of existing function to accomplish this. We have to hand-crank the
          * SBS calls. Later we may wish to create a PMR work item for an
          * existing RETAIN record if it does not exist in RTC. That function
          * was just changed to rely on a pre-browse and work item population
          * in the operation advisor, and bypass the operation participant
          * browse / fill-in. IDEA: Save PMR browse result artifact here, and
          * pass to PMR create (via sportServiceData?) and have that function
          * utilize the pre-browse result artifact? Save this along with the
          * unique PMR reference?
          */

         if (browsedPMR == null)
         {
            // Do a PMR browse if needed -
            browsedPMR = PmrUtils.findPmrInRETAIN( pmrName, sportCommonData );
            // May need to add to special keys? (TBD)
            // "findPmr" skips anything that starts with *** -
            pmrUniqueKeys.put( "***PMRBrowseSBSResultArtifact***",
                  browsedPMR );
         }
         // Successful PMR browse from RETAIN -
         if (browsedPMR != null)
         {
            // Need to obtain values from Artifact! -
            ArtifactRecordPart pmrRecord = browsedPMR
                  .findRecord( PmrConstants.WORK_ITEM_TYPE_ID );
            if (pmrRecord != null)
            {
               String retainCreateDate = pmrRecord
                     .findFieldValue( PmrConstants.OPEN_DATE_ATTRIBUTE_ID );
               if (retainCreateDate != null && retainCreateDate.length() > 0)
               {
                  // Need to get an RTC-format object value from an
                  // artifact field String value.
                  // RtcUtils has a way to copy a work item attribute
                  // object to an artifact.
                  // SBS RTC action performer has toRTCValue to copy
                  // artifact fields to a work item.
                  // Set create-date/time as key 2.
                  // Values need to be in generic Object super class
                  // format for RTC query. Further depth methods will
                  // not have work item to get String values from.
                  // Construct special String keys.
                  // Assume this field converts to a Timestamp attr as
                  // defined in our SPoRT template.
                  // This will bomb if we ever change the attr type.
                  Timestamp rtcCreateDateTSObject = RtcUtils
                        .toTimestamp( retainCreateDate, sportCommonData );
                  pmrUniqueKeys.put( PmrConstants.OPEN_DATE_ATTRIBUTE_ID,
                        rtcCreateDateTSObject );
                  // _B200155C Improve duplicate PMR error msg.
                  PmrUtils.setActivePMRSaveKey( pmrUniqueKeys,
                        // PmrConstants.OPEN_DATE_ATTRIBUTE_ID,
                        "Created Date", retainCreateDate );
               }
            }
         }
         else
         {
            // _B212962A
            // PMR does not exist in RETAIN, so it *IS* ARCHIVED!
            // Will not have a RETAIN create date key for RTC query.
            // Then NO active PMR will exist in RTC, return null
            // for the RTC work item.
            // Prevent redundant RTC queries -
            // "Capture" / "tag" this (null) PMR work item caught for
            // later "release" into the wild.
            pmrUniqueKeys.put( "***RTCPMRWorkItem***", rtcPMRworkitem );
            return rtcPMRworkitem;
         }
      }

      // IWorkItem pmr = PmrUtils.findPmr( pmrName, true,
      if (willCreatePmr)
      {
         // If a new work item is going to get created, check if any
         // pending saves are in progress first and wait.
         rtcPMRworkitem = PmrUtils.findPmr( pmrUniqueKeys, willCreatePmr,
               sportCommonData );
      }
      else
      {
         // Will only need a single simple RTC query for work item.
         rtcPMRworkitem = PmrUtils.findPmr( pmrUniqueKeys, sportCommonData );
      }
      // Prevent redundant RTC queries -
      // "Capture" / "tag" this PMR work item caught for later
      // "release" into the wild.
      pmrUniqueKeys.put( "***RTCPMRWorkItem***", rtcPMRworkitem );
      return rtcPMRworkitem;

   }

   // _T155551A ARCHIVED state
   /**
    * Back end of an updatePmrFromRetain action. Refresh RTC work item with
    * browsed RETAIN data from artifact.
    * 
    * @param pmr
    * @param sportCommonData
    * @return
    * @throws SportUserActionFailException
    * @throws TeamRepositoryException
    */
   public static void populateWorkItemFromArtifact( IWorkItem pmr,
         Artifact retainArtifact, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      ArtifactRecordPart pmrRecord = retainArtifact
            .findRecord( pmr.getWorkItemType() );
      IWorkItem pmrToUpdate = sportCommonData.getWorkItemForUpdate();
      // Get the sanitized attribute
      IAttribute sanitizedAttr = RtcUtils.findAttribute( pmr,
            PmrConstants.SANITIZED_ATTRIBUTE_ID, sportCommonData );
      Boolean sanitized = (Boolean)pmrToUpdate.getValue( sanitizedAttr );
      // Perform the rest of Update From RETAIN ...
      // _T155539C All below should be updating the working copy.
      // Sandbox working copy to be committed later on save.
      for (ArtifactRecordField field : pmrRecord.getFields())
      {
         String fieldName = field.getName();
         String fieldValue = field.getAllValuesAsString();

         // If sanitized, don't update RETAIN identified PI data fields
         if (!sanitized)
         {
            RtcUtils.setWorkItemAttributeToValue( fieldName, fieldValue, pmr,
                  sportCommonData );
         }
         else
         {
            if (!fieldName.equals( PmrConstants.CONTACT_NAME_ATTRIBUTE_ID )
                  && (!fieldName
                        .equals( PmrConstants.CONTACT_PHONE_1_ATTRIBUTE_ID ))
                  && (!fieldName
                        .equals( PmrConstants.CONTACT_PHONE_2_ATTRIBUTE_ID ))
                  && (!fieldName
                        .equals( PmrConstants.EMAIL_ADDRESS_ATTRIBUTE_ID ))
                  && (!fieldName.equals( PmrConstants.CITY_ATTRIBUTE_ID )))
            {
               {
                  RtcUtils.setWorkItemAttributeToValue( fieldName, fieldValue,
                        pmr, sportCommonData );
               }
            }
         }

         // _T1083C Built-in attributes require using their own access methods
         // (Work flow state method gets working copy explicitly passed in.)
         if (fieldName.equals( PmrConstants.RETAIN_STATUS_ATTRIBUTE_ID ))
            RtcUtils.setWorkflowState( pmrToUpdate, fieldValue,
                  sportCommonData );
      }
      // _T155539C Modify working copy.
      String formattedPmrCallList = formatPmrCallList( pmrToUpdate,
            getPmrCallData( pmrRecord ), sportCommonData );
      RtcUtils.setWorkItemAttributeToValue(
            PmrConstants.CALL_LIST_ATTRIBUTE_ID, formattedPmrCallList,
            pmrToUpdate, sportCommonData );
   }

   public static void pmrClose( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      // check format of close code
      String closecode = RtcUtils.getAttributeValueAsString( pmr,
            PmrConstants.PROBLEM_STATUS_ATTRIBUTE_ID, sportCommonData );
      // _B67576C Handle short input - just set to invalid elements
      String ss = (closecode.length() >= 2 ? closecode.substring( 0, 2 )
            : "");
      char n = (closecode.length() >= 3 ? closecode.charAt( 2 ) : ' ');
      char l = (closecode.length() >= 4 ? closecode.charAt( 3 ) : ' ');
      // This last test handles values > 5 chars (ie too long)
      char m = (closecode.length() == 5 ? closecode.charAt( 4 ) : ' ');
      if ((!ss.equalsIgnoreCase( "CL" ) && !ss.equalsIgnoreCase( "CQ" )
            && !ss.equalsIgnoreCase( "CV" ) && !ss.equalsIgnoreCase( "CW" )
            && !ss.equalsIgnoreCase( "CA" )) || !Character.isDigit( n )
            || (l != 'L' && l != 'l') || (m != '1' && m != '2'))
      {
         // incorrect format for close code
         String message = "Incorrect Problem Status entered. The correct format is "
               + "SSnLm where SS = CL, CQ, CV, CW or CA n = a number set "
               + "by the support center Lm = L1 or L2 to represent the level of the owner";

         throw new SportUserActionFailException( message );

      }

      // Create Artifact
      Artifact artifact = SbsUtils.createArtifact( pmr,
            PmrConstants.SBS_CLOSE_ACTION,
            PmrConstants.SBS_CLOSE_ATTRIBUTE_IDS, sportCommonData );

      SbsUtils.sendArtifacts( artifact, sportCommonData );

      // if there is text, then issue addText
      String closetext = RtcUtils.getAttributeValueAsString( pmr,
            PmrConstants.PMR_CLOSETEXT_ATTRIBUTE_ID, sportCommonData );
      if (closetext != null && closetext.trim().length() > 0)
      {

         artifact = SbsUtils.createArtifact( pmr,
               PmrConstants.SBS_ADD_TEXT_ACTION,
               PmrConstants.SBS_ADDCLOSETEXT_ACTION_ATTRIBUTE_IDS,
               sportCommonData );
         SbsUtils.sendArtifacts( artifact, sportCommonData );
         // reset New Text
         RtcUtils.setWorkItemAttributeToValue(
               PmrConstants.PMR_CLOSETEXT_ATTRIBUTE_ID, "", pmr,
               sportCommonData );

      }
      updatePmrFromRetain( pmr, sportCommonData );

      // update the Close Code with actual status before returning
      // IWorkItem pmrToUpdate = sportCommonData.getWorkItemForUpdate();
      // String newStatus = RtcUtils.getAttributeValueAsString( pmrToUpdate,
      // PmrConstants.PROBLEM_STATUS_ATTRIBUTE_ID, sportCommonData );
      // RtcUtils.setWorkItemAttributeToValue(
      // PmrConstants.PMR_CLOSECODE_ATTRIBUTE_ID, newStatus, pmr,
      // sportCommonData );

   }

   // PMR Call Browse function
   public static ArtifactRecordPart pmrCallBrowse( IWorkItem pmr,
         CallInfo callInfo, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( pmr,
            PmrConstants.SBS_CALL_BROWSE_ACTION,
            PmrConstants.SBS_CALL_BROWSE_ACTION_ATTRIBUTE_IDS,
            sportCommonData );
      ArtifactRecordPart pmrRecord = artifact
            .findRecord( pmr.getWorkItemType() );
      addCallInfoToPmrRecord( pmrRecord, callInfo );
      artifact.setArtifactClass( PmrConstants.CALL_CLASS );
      Artifact sbsResponse = SbsUtils.sendArtifacts( artifact,
            sportCommonData );
      return sbsResponse.findRecord( PmrConstants.WORK_ITEM_TYPE_ID );
   }

   // PMR Call Create function
   public static void pmrCallCreate( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( pmr,
            PmrConstants.SBS_CALL_CREATE_ACTION,
            PmrConstants.SBS_CALL_CREATE_ACTION_ATTRIBUTE_IDS,
            sportCommonData );
      artifact.setArtifactClass( PmrConstants.CALL_CLASS );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updatePmrFromRetain( pmr, sportCommonData );
   }

   public static void pmrCallComplete( IWorkItem pmr,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {

      List<String> fieldsList1 = new ArrayList<String>(
            PmrConstants.SBS_CALL_REQUEUECOMPLETE_ACTION_PRIMARY_OPT_NOBLANK_ATTRIBUTE_IDS.length );
      List<String> fieldsList2 = new ArrayList<String>(
            PmrConstants.SBS_CALL_REQUEUECOMPLETE_ACTION_PRIMARY_OPT_CANBEBLANK_ATTRIBUTE_IDS.length );
      CallInfo selectedCall = getSelectedCall( pmr, sportCommonData );
      boolean dispatchAlreadyTried = false;

      // Add any optional attributes which the user has changed
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         String actionType = actionParts[0];
         if ((actionParts.length == 2) && actionType.equals( "modify" ))
         {
            String fieldName = actionParts[1];
            if (selectedCall.getCallType() != CallType.PRIMARY)
            {
               if (fieldName
                     .equals( PmrConstants.SERVICE_GIVEN_ATTRIBUTE_ID ))
                  fieldsList1.add( fieldName );
            }
            else
            {
               if (fieldName.equals( PmrConstants.SERVICE_GIVEN_ATTRIBUTE_ID )
                     || fieldName
                           .equals( PmrConstants.FOLLOWUP_INFO_ATTRIBUTE_ID ))
                  fieldsList1.add( fieldName );
               if (fieldName
                     .equals( PmrConstants.PRIMARY_FUP_QUEUE_ATTRIBUTE_ID )
                     || fieldName.equals(
                           PmrConstants.PRIMARY_FUP_CENTER_ATTRIBUTE_ID ))
                  fieldsList2.add( fieldName );
            }

         }
      }

      // If primary FUP queue or center has changed, then both need to be
      // passed in
      boolean containsFupQ = fieldsList2
            .contains( PmrConstants.PRIMARY_FUP_QUEUE_ATTRIBUTE_ID );
      boolean containsFupC = fieldsList2
            .contains( PmrConstants.PRIMARY_FUP_CENTER_ATTRIBUTE_ID );
      if (containsFupQ && !containsFupC)
         fieldsList2.add( PmrConstants.PRIMARY_FUP_CENTER_ATTRIBUTE_ID );
      if (containsFupC && !containsFupQ)
         fieldsList2.add( PmrConstants.PRIMARY_FUP_QUEUE_ATTRIBUTE_ID );

      // Convert the lists to arrays
      String[] optionalAttrs1 = null;
      String[] optionalAttrs2 = null;
      if (fieldsList1.size() > 0)
      {
         optionalAttrs1 = new String[fieldsList1.size()];
         optionalAttrs1 = fieldsList1.toArray( optionalAttrs1 );
      }
      if (fieldsList2.size() > 0)
      {
         optionalAttrs2 = new String[fieldsList2.size()];
         optionalAttrs2 = fieldsList2.toArray( optionalAttrs2 );
      }

      Artifact artifact = SbsUtils.createArtifact( pmr,
            PmrConstants.SBS_CALL_COMPLETE_ACTION,
            PmrConstants.SBS_CALL_COMPLETE_ACTION_REQ_ATTRIBUTE_IDS,
            optionalAttrs1, optionalAttrs2, sportCommonData );
      ArtifactRecordPart pmrRecord = artifact
            .findRecord( pmr.getWorkItemType() );
      addCallInfoToPmrRecord( pmrRecord, selectedCall );
      artifact.setArtifactClass( PmrConstants.CALL_CLASS );

      try
      {
         // If the selected call is not currently dispatched, then
         // first try to automatically dispatch it
         if (selectedCall.getDispatchedTo().equals( "" ))
         {
            pmrCallDispatch( pmr, sportCommonData );
            dispatchAlreadyTried = true;
         }
         SbsUtils.sendArtifacts( artifact, sportCommonData );
      }
      catch (SportUserActionFailException x)
      {
         String message = x.getMessage();
         message = message.toUpperCase();
         // If the user is not dispatched to the call, try to do the dispatch
         if (message.contains( "NOT DISPATCHED" ) && !dispatchAlreadyTried)
         {
            pmrCallDispatch( pmr, sportCommonData );
            SbsUtils.sendArtifacts( artifact, sportCommonData );
         }
         // If the call is already dispatched, try the action again in case
         // the
         // call is already dispatched to the user
         else if (message.contains( "CALL IS ALREADY DISPATCHED" ))
         {
            SbsUtils.sendArtifacts( artifact, sportCommonData );
         }
         else
            throw x;
      }
      // Reset Service Given because that field is not returned from the Call
      // browse
      RtcUtils.setWorkItemAttributeToValue(
            PmrConstants.SERVICE_GIVEN_ATTRIBUTE_ID, "", pmr,
            sportCommonData );
      updatePmrFromRetain( pmr, sportCommonData );
   }

   // _T64529A PMR Call Dispatch function
   public static void pmrCallDispatch( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      // Need to obtain selected call data here, throw any errors
      CallInfo selectedCall = getSelectedCall( pmr, sportCommonData );

      // Create artifact with PMR record
      Artifact artifact = SbsUtils.createArtifact( pmr,
            PmrConstants.SBS_CALL_DISPATCH_ACTION,
            PmrConstants.SBS_CALL_DISPATCH_ACTION_ATTRIBUTE_IDS,
            sportCommonData );

      // Set Call info fields
      // Mapping will create callId subrecord from these fields
      // Doesnt like a null attribute list, try empty list
      ArtifactRecordPart pmrRecord = artifact
            .findRecord( pmr.getWorkItemType() );
      addCallInfoToPmrRecord( pmrRecord, selectedCall );

      // Set CALL class artifact with PMR record
      artifact.setArtifactClass( PmrConstants.CALL_CLASS );
      // Operand will default to CD in SBS for callUpdate in RETAIN

      // Shuffle off to SBS, then browse for latest updates
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updatePmrFromRetain( pmr, sportCommonData );
   }

   // PMR Call Requeue function
   public static void pmrCallRequeue( IWorkItem pmr,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {

      List<String> fieldsList1 = new ArrayList<String>(
            PmrConstants.SBS_CALL_REQUEUECOMPLETE_ACTION_PRIMARY_OPT_NOBLANK_ATTRIBUTE_IDS.length );
      List<String> fieldsList2 = new ArrayList<String>(
            PmrConstants.SBS_CALL_REQUEUECOMPLETE_ACTION_PRIMARY_OPT_CANBEBLANK_ATTRIBUTE_IDS.length );
      CallInfo selectedCall = getSelectedCall( pmr, sportCommonData );
      boolean dispatchAlreadyTried = false;

      // Add any optional attributes which the user has changed
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         String actionType = actionParts[0];
         if ((actionParts.length == 2) && actionType.equals( "modify" ))
         {
            String fieldName = actionParts[1];
            if (selectedCall.getCallType() != CallType.PRIMARY)
            {
               if (fieldName
                     .equals( PmrConstants.SERVICE_GIVEN_ATTRIBUTE_ID ))
                  fieldsList1.add( fieldName );
            }
            else
            {
               if (fieldName.equals( PmrConstants.SERVICE_GIVEN_ATTRIBUTE_ID )
                     || fieldName
                           .equals( PmrConstants.FOLLOWUP_INFO_ATTRIBUTE_ID ))
                  fieldsList1.add( fieldName );
               if (fieldName
                     .equals( PmrConstants.PRIMARY_FUP_QUEUE_ATTRIBUTE_ID )
                     || fieldName.equals(
                           PmrConstants.PRIMARY_FUP_CENTER_ATTRIBUTE_ID ))
                  fieldsList2.add( fieldName );
            }

         }
      }

      // If primary FUP queue or center has changed, then both need to be
      // passed in
      boolean containsFupQ = fieldsList2
            .contains( PmrConstants.PRIMARY_FUP_QUEUE_ATTRIBUTE_ID );
      boolean containsFupC = fieldsList2
            .contains( PmrConstants.PRIMARY_FUP_CENTER_ATTRIBUTE_ID );
      if (containsFupQ && !containsFupC)
         fieldsList2.add( PmrConstants.PRIMARY_FUP_CENTER_ATTRIBUTE_ID );
      if (containsFupC && !containsFupQ)
         fieldsList2.add( PmrConstants.PRIMARY_FUP_QUEUE_ATTRIBUTE_ID );

      // Convert the lists to arrays
      String[] optionalAttrs1 = null;
      String[] optionalAttrs2 = null;
      if (fieldsList1.size() > 0)
      {
         optionalAttrs1 = new String[fieldsList1.size()];
         optionalAttrs1 = fieldsList1.toArray( optionalAttrs1 );
      }
      if (fieldsList2.size() > 0)
      {
         optionalAttrs2 = new String[fieldsList2.size()];
         optionalAttrs2 = fieldsList2.toArray( optionalAttrs2 );
      }

      Artifact artifact = SbsUtils.createArtifact( pmr,
            PmrConstants.SBS_CALL_REQUEUE_ACTION,
            PmrConstants.SBS_CALL_REQUEUE_ACTION_REQ_ATTRIBUTE_IDS,
            optionalAttrs1, optionalAttrs2, sportCommonData );
      ArtifactRecordPart pmrRecord = artifact
            .findRecord( pmr.getWorkItemType() );
      addCallInfoToPmrRecord( pmrRecord, selectedCall );
      artifact.setArtifactClass( PmrConstants.CALL_CLASS );
      try
      {
         // If the selected call is not currently dispatched, then
         // first try to automatically dispatch it
         if (selectedCall.getDispatchedTo().equals( "" ))
         {
            pmrCallDispatch( pmr, sportCommonData );
            dispatchAlreadyTried = true;
         }
         SbsUtils.sendArtifacts( artifact, sportCommonData );
      }
      catch (SportUserActionFailException x)
      {
         String message = x.getMessage();
         message = message.toUpperCase();
         // If the user is not dispatched to the call, try to do the dispatch
         if (message.contains( "NOT DISPATCHED" ) && !dispatchAlreadyTried)
         {
            pmrCallDispatch( pmr, sportCommonData );
            SbsUtils.sendArtifacts( artifact, sportCommonData );
         }
         // If the call is already dispatched, try the requeue in case the
         // call is already dispatched to the user
         else if (message.contains( "CALL IS ALREADY DISPATCHED" ))
         {
            SbsUtils.sendArtifacts( artifact, sportCommonData );
         }
         else
            throw x;
      }
      // Reset Service Given because that field is not returned from the Call
      // browse
      RtcUtils.setWorkItemAttributeToValue(
            PmrConstants.SERVICE_GIVEN_ATTRIBUTE_ID, "", pmr,
            sportCommonData );
      updatePmrFromRetain( pmr, sportCommonData );
   }

   public static void pmrCallConvert( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      CallInfo selectedCall = getSelectedCall( pmr, sportCommonData );
      boolean dispatchAlreadyTried = false;
      Artifact artifact = SbsUtils.createArtifact( pmr,
            PmrConstants.SBS_CALL_CONVERT_ACTION,
            PmrConstants.SBS_CALL_CONVERT_ACTION_ATTRIBUTE_IDS,
            sportCommonData );
      ArtifactRecordPart pmrRecord = artifact
            .findRecord( pmr.getWorkItemType() );
      addCallInfoToPmrRecord( pmrRecord, selectedCall );
      artifact.setArtifactClass( PmrConstants.CALL_CLASS );
      try
      {
         // If the selected call is not currently dispatched, then
         // first try to automatically dispatch it
         if (selectedCall.getDispatchedTo().equals( "" ))
         {
            pmrCallDispatch( pmr, sportCommonData );
            dispatchAlreadyTried = true;
         }
         SbsUtils.sendArtifacts( artifact, sportCommonData );
      }
      catch (SportUserActionFailException x)
      {
         String message = x.getMessage();
         message = message.toUpperCase();
         if (message.contains( "NOT DISPATCHED" ) && !dispatchAlreadyTried)
         {
            pmrCallDispatch( pmr, sportCommonData );
            SbsUtils.sendArtifacts( artifact, sportCommonData );
         }
         // If the call is already dispatched, try the action again in case
         // the
         // call is already dispatched to the user
         else if (message.contains( "CALL IS ALREADY DISPATCHED" ))
         {
            SbsUtils.sendArtifacts( artifact, sportCommonData );
         }
         else
            throw x;
      }
      updatePmrFromRetain( pmr, sportCommonData );
   }

   // _B68102C PMR get CallInfo
   /**
    * Return a non-formatted CallInfo object representing the selected call.
    * Throws exceptions when no calls to select, invalid selection number,
    * etc.
    * 
    * @param pmr
    * @param sportCommonData
    * @return - Unformatted CallInfo object representing the selected call
    *         entry.
    * @throws SportRuntimeException
    * @throws SportUserActionFailException
    * @throws TeamRepositoryException
    */
   public static CallInfo getSelectedCall( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws SportRuntimeException, SportUserActionFailException,
         TeamRepositoryException
   {
      // _B68102C
      String[] callListRows = extractPMRCallEntries(
            RtcUtils.getAttributeValueAsString( pmr,
                  PmrConstants.CALL_LIST_ATTRIBUTE_ID, sportCommonData ) );

      // _B68102C callListRows should now only have call entries
      String selectedCallValue = RtcUtils.getAttributeValueAsString( pmr,
            PmrConstants.SELECTED_CALL_ATTRIBUTE_ID, sportCommonData );
      if ((selectedCallValue == null) || (selectedCallValue.equals( "" )))
      {
         throw new SportUserActionFailException(
               "Missing required Selected Call" );
      }
      int selectedCall;
      try
      {
         selectedCall = Integer.parseInt( selectedCallValue );
      }
      catch (NumberFormatException e)
      {
         throw new SportUserActionFailException(
               "Selected Call is not a valid integer" );
      }

      return (getCallEntry( selectedCall, callListRows ));
   }

   // _B68102A PMR get CallInfo
   /**
    * Returns just the call entries from the PMR attribute value. Should work
    * for HTML and Wiki format data.
    * 
    * @param callList - Call List value from work item
    * @param cellDivider - Allows for HTML or Wiki format data.
    * @return - String array representing the calls in the list.
    * @throws SportRuntimeException
    */
   public static String[] extractPMRCallEntries( String callList )
         throws SportRuntimeException
   {
      if (callList == null)
      {
         throw new SportRuntimeException( "Missing required Call List" );
      }
      // Need to potentially fix up the HTML value as returned by RTC
      // Remove possible PRE tags
      // _B68102C Old HTML format may have pre tags. New wiki format will not.
      // But this code handles both forms. A no-op for wiki.
      int start = (callList.startsWith( "<pre>" )) ? 5 : 0;
      int end = (callList.endsWith( "</pre>" )) ? callList.length() - 6
            : callList.length();
      callList = callList.substring( start, end );
      // Remove potential trailing BR tag (empty last line)
      start = 0;
      end = (callList.endsWith( "<br/>" )) ? callList.length() - 5
            : callList.length();
      callList = callList.substring( start, end );
      // RTC may also return NBSP tags for whitespace - remove them here
      callList = callList.replaceAll( "&nbsp;", "" );
      // RTC gives back the value with either newlines or BR tags?
      String rowSeparator = callList.contains( "<br/>" ) ? "<br/>" : "\n";
      // String[] callListRows = callList.split( "\n" );
      String[] callListRows = callList.split( rowSeparator );
      // Rows may now contain 1 header row, then any calls
      // _B68102C Wiki format only uses 1 row for the header.
      // The wiki attribute takes care of formatting.
      // _B68102A Wiki or HTML header
      int headerRows = (callList
            .startsWith( StringUtils.WIKI_TABLE_HEADER_CELL ) ? 1 : 2);
      if (callListRows.length <= headerRows)
      {
         // throw new SportRuntimeException(
         // "Call List has less than two rows" );
         throw new SportRuntimeException( "Call List has no calls in it" );
      }
      // Copy only the calls - skip the header stuff
      String[] callListClone = callListRows.clone();
      // _B68102C
      callListRows = new String[callListClone.length - headerRows];
      for (int ix = 0; ix < callListClone.length - headerRows; ix++)
      {
         callListRows[ix] = callListClone[ix + headerRows];
      }
      return (callListRows);
   }

   // _B68102A PMR get CallInfo
   /**
    * Given the extracted PMR attribute value calls, return the selected entry
    * in internal homogenized format. Assumes the attribute value is in Wiki
    * format.
    * 
    * @param selectedCall - the call selection number, starting at 1. Array
    *        indexing is performed within.
    * @param callListRows - processed call list data from RTC attribute.
    *        Represents JUST the call entries.
    * @return - Unformatted CallInfo object representing the indicated call
    *         entry.
    * @throws SportRuntimeException
    * @throws SportUserActionFailException
    */
   public static CallInfo getCallEntry( int selectedCall,
         String[] callListRows )
         throws SportRuntimeException, SportUserActionFailException
   {
      return (getCallEntry( selectedCall, callListRows,
            StringUtils.WIKI_TABLE_ENTRY_CELL ));
      // StringUtils.BOX_DRAWING_VERTICAL ));
   }

   // _B68102A PMR get CallInfo
   /**
    * Given the extracted PMR attribute value calls, return the selected entry
    * in internal homogenized format. Should work for HTML or Wiki format
    * data.
    * 
    * @param selectedCall - the call selection number, starting at 1. Array
    *        indexing is performed within.
    * @param callListRows - processed call list data from RTC attribute.
    *        Represents JUST the call entries.
    * @param cellDivider - Allows for HTML or Wiki format data.
    * @return - Unformatted CallInfo object representing the indicated call
    *         entry.
    * @throws SportRuntimeException
    * @throws SportUserActionFailException
    */
   public static CallInfo getCallEntry( int selectedCall,
         String[] callListRows, String cellDivider )
         throws SportRuntimeException, SportUserActionFailException
   {
      if (selectedCall < 1)
      {
         throw new SportUserActionFailException(
               "Selected Call is too small" );
      }
      // if (selectedCall >= (callListRows.length - 1))
      if (selectedCall > (callListRows.length))
      {
         throw new SportUserActionFailException(
               "Selected Call is too large " );
      }
      // _B68102C Wiki call entry form =
      // | <call selection#>
      // | <Call Queue> | <Call Center>
      // | <Primary, Secondary, Backup>
      // | <pPG>
      // | <RETAIN-id dispatched>
      // String[] selectedCallColumns = callListRows[selectedCall + 1]
      // Proper array indexing -
      // String[] selectedCallColumns = callListRows[selectedCall - 1]
      // .split( StringUtils.BOX_DRAWING_VERTICAL );
      // _B68102C Wiki form will start with a cell indicator, remove it
      // Even if originally in unicode, the cell divider is now passed as the
      // string equivalent at this point.
      String cellDivRegExpr = cellDivider;
      if (cellDivider.length() == 1)
      {
         // Some String methods require regular expression.
         // Single character, should be a special character, so escape it.
         cellDivRegExpr = "\\" + cellDivider;
      }
      String callEntry = (callListRows[selectedCall - 1]
            .startsWith( cellDivider )
                  ? callListRows[selectedCall - 1]
                        .replaceFirst( cellDivRegExpr, "" )
                  : callListRows[selectedCall - 1]);
      // _B68102C
      // Parse out call entry data for HTML or Wiki form as indicated.
      String[] selectedCallColumns = callEntry.split( cellDivRegExpr );
      if (selectedCallColumns.length != 6)
      {
         throw new SportRuntimeException(
               "Selected Call does not have six columns" );
      }
      CallInfo callInfo = new CallInfo();
      String callTypeValue = selectedCallColumns[1].trim();
      if (callTypeValue.equalsIgnoreCase( "Backup" ))
      {
         callInfo.setCallType( CallType.BACKUP );
      }
      else if (callTypeValue.equalsIgnoreCase( "Primary" ))
      {
         callInfo.setCallType( CallType.PRIMARY );
      }
      else if (callTypeValue.equalsIgnoreCase( "Secondary" ))
      {
         callInfo.setCallType( CallType.SECONDDARY );
      }
      else
      {
         throw new SportUserActionFailException(
               "Unexpected Call Type of \"" + callTypeValue + "\"" );
      }
      callInfo.setQueue( selectedCallColumns[2].trim() );
      callInfo.setCenter( selectedCallColumns[3].trim() );
      callInfo.setPPG( selectedCallColumns[4].trim() );
      callInfo.setDispatchedTo( selectedCallColumns[5].trim() );
      return callInfo;
   }

   // _B68102A PMR get CallInfo on all calls in PMR
   /**
    * Return all calls from PMR as CallInfo array. Assumes input in Wiki
    * format of data.
    * 
    * @param callList - Call List value from work item with header info (Wiki
    *        format)
    * @return - ArrayList of unformatted CallInfo objects representing the
    *         calls in the list, or null if no call content in input (ie empty
    *         or just the table header)
    * @throws SportRuntimeException
    * @throws SportUserActionFailException
    */
   public static ArrayList<CallInfo> getAllPMRCalls( String callList )
         throws SportRuntimeException, SportUserActionFailException
   {
      return (getAllPMRCalls( callList, StringUtils.WIKI_TABLE_ENTRY_CELL ));
   }

   // _B68102A PMR get CallInfo on all calls in PMR
   /**
    * Return all calls from PMR as CallInfo array. Should work for HTML and
    * Wiki format data.
    * 
    * @param callList - Call List value from work item with header info
    * @param cellDivider - Allows for HTML or Wiki format data.
    * @return - ArrayList of unformatted CallInfo objects representing the
    *         calls in the list, or null if no call content in input (ie empty
    *         or just the table header)
    * @throws SportRuntimeException
    * @throws SportUserActionFailException
    */
   public static ArrayList<CallInfo> getAllPMRCalls( String callList,
         String cellDivider )
         throws SportRuntimeException, SportUserActionFailException
   {
      if (callList == null || callList.isEmpty())
      {
         // If not even a header, return null
         return null;
      }
      try
      {
         ArrayList<CallInfo> pmrCalls = new ArrayList<CallInfo>();
         // May have just the header, but no other content -
         // The called method is a bit "zealous" about error reporting
         // throwing exceptions. But that suits its original purpose.
         String[] callListRows = extractPMRCallEntries( callList );

         // _B68102C callListRows should now only have call entries
         // Selected call is the call entry number, 1-indexed
         for (int selectedCall = 1; selectedCall <= callListRows.length; selectedCall++)
         {
            // The called method may throw exceptions when extracting the
            // call list entry data. But it should be unlikely to incur an
            // indexing exception.
            pmrCalls.add(
                  getCallEntry( selectedCall, callListRows, cellDivider ) );
         }

         return (pmrCalls);
      }
      catch (SportRuntimeException e)
      {
         // On an exception where the call list content was empty, other than
         // just a header, just return null.
         // SportUserActionFailException getting any entry will percolate.
         // Similarly any other SportRuntimeException will percolate.
         if (e.getMessage().contains( "Missing required Call List" )
               || e.getMessage().contains( "Call List has no calls in it" ))
         {
            return null;
         }
         else
         {
            // Some other issue with a particular call list entry, perhaps
            throw e;
         }
      }
   }

   // PMR Edit Fields function
   public static void pmrEditFields( IWorkItem pmr,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {
      List<String> fieldsList = new ArrayList<String>(
            PmrConstants.SBS_BROWSE_ACTION_ATTRIBUTE_IDS.length );

      // go thru list of actions, and add those which are in the optional
      // attribute list
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         String actionType = actionParts[0];
         if ((actionParts.length == 2) && actionType.equals( "modify" ))
         {
            String fieldName = actionParts[1];
            for (String field : PmrConstants.SBS_EDIT_FIELDS_ACTION_ATTRIBUTE_IDS)
            {
               if (fieldName.equals( field )) // if modified
               { // add to list
                  fieldsList.add( fieldName );
                  break; // bail out of for loop
               }
            }
         }
      }

      if (fieldsList.size() > 0)
      {
         // Add the attributes needed for browsing a PMR
         for (String fieldName : PmrConstants.SBS_BROWSE_ACTION_ATTRIBUTE_IDS)
         {
            fieldsList.add( fieldName );
         }

         String[] fieldsArray = new String[fieldsList.size()];
         fieldsArray = fieldsList.toArray( fieldsArray );

         // Create PMR Artifact
         Artifact artifact = SbsUtils.createArtifact( pmr,
               PmrConstants.SBS_UPDATE_PMR_ACTION, fieldsArray,
               sportCommonData );

         SbsUtils.sendArtifacts( artifact, sportCommonData );

         updatePmrFromRetain( pmr, sportCommonData );
      }
      else
      { // Display error message if no fields have been changed.
         String message = "Please specify at least one changed attribute for Edit Fields";
         throw new SportUserActionFailException( message );
      }
   }

   public static void pmrReopen( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      // Create Artifact for PMR CALL
      Artifact artifact = SbsUtils.createArtifact( pmr,
            PmrConstants.SBS_CREATE_SECONDARY_ACTION,
            PmrConstants.PMR_REOPEN_ATTRIBUTE_IDS, sportCommonData );
      artifact.setArtifactClass( PmrConstants.CALL_CLASS );

      SbsUtils.sendArtifacts( artifact, sportCommonData );

      updatePmrFromRetain( pmr, sportCommonData );
   }

   public static void setPmrSummaryOnCreate( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // Need to use working copy of work item here if it exists,
      // as Create PMR From RETAIN initial work item will NOT have
      // the CommentLine attribute set (only has aparNum).
      IWorkItem pmrForUpdate = sportCommonData.getWorkItemForUpdate();
      // Summary = CommentLine
      setPmrSummaryFromAttribute( PmrConstants.COMMENT_LINE_ATTRIBUTE_ID,
            pmrForUpdate, sportCommonData );

   }

   public static void setPmrSummaryFromAttribute( String fromAttributeId,
         IWorkItem pmr, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String pmrSummaryValue = null;
      IAttribute pmrAttr = RtcUtils.findAttribute( pmr, fromAttributeId,
            sportCommonData );
      // Test for null attribute to avoid exception
      if (pmrAttr != null)
      {
         pmrSummaryValue = RtcUtils.getAttributeValueAsString( pmr,
               pmrAttr.getIdentifier(), sportCommonData );
      }
      setPmrSummaryToValue( pmrSummaryValue, pmr, sportCommonData );
   }

   public static void setPmrSummaryToValue( String pmrSummaryValue,
         IWorkItem pmr, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      if (pmrSummaryValue != null && pmr != null)
      {
         // Get or reuse working copy
         IWorkItem pmrToUpdate = sportCommonData.getWorkItemForUpdate();
         // Set HTML built-in Summary from string
         pmrToUpdate.setHTMLSummary(
               XMLString.createFromPlainText( pmrSummaryValue ) );
         // _T1083A To reuse working copy, save done at end of Participant
      }
   }

   // _T155539C Unique PMR by name and RETAIN create date(/time)
   // Change argument to be map of attribute name : value entries.
   /**
    * Given a map of attribute names : values, query RTC for a PMR work item
    * that matches that set of attributes / values. The values are set as raw
    * Objects to satisfy proper attribute type handling in RTC query.
    * 
    * @param pmrUniqueKeys
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static IWorkItem findPmr( HashMap<String, Object> pmrUniqueKeys,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      Map<IQueryableAttribute, Object> attributeValues = new HashMap<IQueryableAttribute, Object>();
      // _T155539C Add all of the keys passed in the argument.
      // Attribute name : value entries in map.
      for (String pmrKey : pmrUniqueKeys.keySet())
      {
         // Skip any ignored special keys
         // Special keys start with ***, skip 'em -
         if (pmrKey.startsWith( "***" ))
            continue;
         // *AND* attribute matching query conditions ...
         IQueryableAttribute pmrAttribute = RtcUtils
               .findQueryableAttribute( pmrKey, sportCommonData );
         attributeValues.put( pmrAttribute, pmrUniqueKeys.get( pmrKey ) );
      }
      // Now add the work item type.
      IQueryableAttribute workItemTypeAttribute = RtcUtils
            .findQueryableAttribute( IWorkItem.TYPE_PROPERTY,
                  sportCommonData );
      attributeValues.put( workItemTypeAttribute,
            PmrConstants.WORK_ITEM_TYPE_ID );
      IQueryResult<IResolvedResult<IWorkItem>> pmrs = RtcUtils
            .queryWorkItemsByAttributes( attributeValues, sportCommonData );
      int pmrCount = pmrs.getResultSize( monitor ).getTotalAvailable();
      if (pmrCount > 1)
      {
         // _T155539C Unique PMR by name and RETAIN create date/time
         // Messaging collector. Assume a String value.
         // _B200155C Improve duplicate PMR error msg.
         throw new SportRuntimeException( "More than one PMR with \""
               + getPMRMessageKeyValue( pmrUniqueKeys )
               + "\" found in RTC." );
         /*
          * throw new SportRuntimeException( "More than one PMR named \"" +
          * pmrName + "\" found in RTC" );
          */
      }
      return (pmrCount == 0) ? null : pmrs.next( monitor ).getItem();
   }

   // _T155539C Unique PMR by name and RETAIN create date(/time)
   // Change argument to be map of attribute name : value entries.
   /**
    * Given a map of attribute names : values, query RTC for a PMR work item
    * that matches that set of attributes / values. If the work item is not
    * found, wait on any active saves of a matching PMR. If the PMR is new and
    * will get created by the caller, indicate an active save is imminent.
    * 
    * @param pmrUniqueKeys
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static IWorkItem findPmr( HashMap<String, Object> pmrUniqueKeys,
         boolean willCreatePmr, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // _T155539A Unique PMR by name and RETAIN create date(/time)
      // Messaging collector. Assume a String value.
      String pmrMsgKeyValue = getPMRMessageKeyValue( pmrUniqueKeys );
      // Key collector. Assume a String value.
      String activePmrSaveKeyValue = getActivePMRSaveKeyValue(
            pmrUniqueKeys );

      synchronized (activePmrSaves)
      {
         // _T155539C Unique PMR by name and RETAIN create date/time
         IWorkItem pmr = findPmr( pmrUniqueKeys, sportCommonData );
         if (pmr == null)
         {
            while (activePmrSaves.containsKey( activePmrSaveKeyValue ))
            {
               try
               {
                  activePmrSaves.wait();
               }
               catch (InterruptedException e)
               {
                  // _T155539C Unique PMR by name and RETAIN create date/time
                  String message = "Unexpected interruption waiting on active save of "
                        + "PMR " + pmrMsgKeyValue + ".";
                  throw new SportRuntimeException( message, e );
               }
            }
            // Go fetch the work item now the pending create / save
            // has completed. If there was no pending save, another
            // RTC query is done anyway just in case.
            pmr = findPmr( pmrUniqueKeys, sportCommonData );
         }
         if ((pmr == null) && willCreatePmr)
         {
            activePmrSaves.put( activePmrSaveKeyValue,
                  new Long( System.currentTimeMillis() ) );
         }
         return pmr;
      }
   }

   public static void updatePmrFromRetain( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( pmr,
            PmrConstants.SBS_BROWSE_ACTION,
            PmrConstants.SBS_BROWSE_ACTION_ATTRIBUTE_IDS, sportCommonData );
      Artifact sbsResponse = SbsUtils.sendArtifacts( artifact,
            sportCommonData );
      // _T155551C Reuse new common method.
      populateWorkItemFromArtifact( pmr, sbsResponse, sportCommonData );
   }

   /**
    * Generate a formatted PMR call list from PMR call data.
    * 
    * @param pmr PMR work item used to generate the formated PMR call list
    * @param pmrCallData a {@code String} containing the PMR call data
    * @param sportCommonData common data for obtaining and processing SBS data
    * @return a {@code String} containing the formatted PMR call list
    *         generated from {@code pmrCallData}
    * @throws TeamRepositoryException
    */
   // _B68102C
   public static String formatPmrCallList( IWorkItem pmr, String pmrCallData,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // _B68102C
      return (formatWIKIPmrCallList( pmr, pmrCallData, sportCommonData ));
      // return (formatHTMLPmrCallList( pmr, pmrCallData, sportCommonData ));
   }

   /**
    * Generate a formatted PMR call list from PMR call data. Used for an HTML
    * type attribute which may get sanitized internally by RTC and may grow >
    * 32K in size. We cannot detect and handle this.
    * 
    * @param pmr PMR work item used to generate the formated PMR call list
    * @param pmrCallData a {@code String} containing the PMR call data
    * @param sportCommonData common data for obtaining and processing SBS data
    * @return a {@code String} containing the formatted PMR call list
    *         generated from {@code pmrCallData}
    * @throws TeamRepositoryException
    */
   // _B68102C
   public static String formatHTMLPmrCallList( IWorkItem pmr,
         String pmrCallData, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String callListTableHeader = "";
      callListTableHeader += "<pre>";
      callListTableHeader += "Selection";
      callListTableHeader += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
      callListTableHeader += "Call Type";
      callListTableHeader += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
      callListTableHeader += " Queue";
      callListTableHeader += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
      callListTableHeader += "Center";
      callListTableHeader += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
      callListTableHeader += "pPG";
      callListTableHeader += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
      callListTableHeader += "Dispatched To";
      callListTableHeader += "\n";
      callListTableHeader += StringUtils
            .repeat( StringUtils.BOX_DRAWING_HORIZONTAL, 9 );
      callListTableHeader += StringUtils.BOX_DRAWING_HORIZONTAL
            + StringUtils.BOX_DRAWING_VERTICAL_HORIZONTAL
            + StringUtils.BOX_DRAWING_HORIZONTAL;
      callListTableHeader += StringUtils
            .repeat( StringUtils.BOX_DRAWING_HORIZONTAL, 9 );
      callListTableHeader += StringUtils.BOX_DRAWING_HORIZONTAL
            + StringUtils.BOX_DRAWING_VERTICAL_HORIZONTAL
            + StringUtils.BOX_DRAWING_HORIZONTAL;
      callListTableHeader += StringUtils
            .repeat( StringUtils.BOX_DRAWING_HORIZONTAL, 6 );
      callListTableHeader += StringUtils.BOX_DRAWING_HORIZONTAL
            + StringUtils.BOX_DRAWING_VERTICAL_HORIZONTAL
            + StringUtils.BOX_DRAWING_HORIZONTAL;
      callListTableHeader += StringUtils
            .repeat( StringUtils.BOX_DRAWING_HORIZONTAL, 6 );
      callListTableHeader += StringUtils.BOX_DRAWING_HORIZONTAL
            + StringUtils.BOX_DRAWING_VERTICAL_HORIZONTAL
            + StringUtils.BOX_DRAWING_HORIZONTAL;
      callListTableHeader += StringUtils
            .repeat( StringUtils.BOX_DRAWING_HORIZONTAL, 3 );
      callListTableHeader += StringUtils.BOX_DRAWING_HORIZONTAL
            + StringUtils.BOX_DRAWING_VERTICAL_HORIZONTAL
            + StringUtils.BOX_DRAWING_HORIZONTAL;
      callListTableHeader += StringUtils
            .repeat( StringUtils.BOX_DRAWING_HORIZONTAL, 13 );
      callListTableHeader += "\n";

      String callListEntryFormat = "";
      if (!pmrCallData.isEmpty())
      {
         callListEntryFormat += "%1$9d";
         callListEntryFormat += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
         callListEntryFormat += "%2$9s";
         callListEntryFormat += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
         callListEntryFormat += "%3$6s";
         callListEntryFormat += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
         callListEntryFormat += "%4$6s";
         callListEntryFormat += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
         callListEntryFormat += "%5$3s";
         callListEntryFormat += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
         callListEntryFormat += "%6$13s";
         callListEntryFormat += "\n";
      }

      // _B68102C Need to close the "pre" HTML tag.
      return (formatPmrCallListTable( pmr, pmrCallData, callListTableHeader,
            callListEntryFormat, sportCommonData ) + "</pre>");
   }

   // _B68102A
   /**
    * Generate a formatted PMR call list from PMR call data. Used for an Wiki
    * type attribute which will build a table in wiki format. Cell formatting
    * is handled by RTC and the wiki attribute.
    * 
    * @param pmr PMR work item used to generate the formated PMR call list
    * @param pmrCallData a {@code String} containing the PMR call data
    * @param sportCommonData common data for obtaining and processing SBS data
    * @return a {@code String} containing the formatted PMR call list
    *         generated from {@code pmrCallData}
    * @throws TeamRepositoryException
    */
   public static String formatWIKIPmrCallList( IWorkItem pmr,
         String pmrCallData, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String callListTableHeader = "";
      // Header format:
      // |= Selection |= Call Type |= Queue |= Center |= pPG |= Dispatched To
      // Also added some Web browser display usability aids
      callListTableHeader += StringUtils.WIKI_TABLE_HEADER_CELL;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += " Selection ";
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_TABLE_HEADER_CELL;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += " Call Type ";
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_TABLE_HEADER_CELL;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += " Queue ";
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_TABLE_HEADER_CELL;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += " Center ";
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_TABLE_HEADER_CELL;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += " pPG ";
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_TABLE_HEADER_CELL;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += " Dispatched To ";
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += "\n";

      // Each table call entry format:
      // PMR:
      // | <call selection#>
      // | <Call Queue> | <Call Center>
      // | <Primary, Secondary, Backup>
      // | <pPG>
      // | <RETAIN-id dispatched>
      String callListEntryFormat = "";
      if (!pmrCallData.isEmpty())
      {
         callListEntryFormat += StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
         callListEntryFormat += "%1$d";
         callListEntryFormat += " " + StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
         callListEntryFormat += "%2$s";
         callListEntryFormat += " " + StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
         callListEntryFormat += "%3$s";
         callListEntryFormat += " " + StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
         callListEntryFormat += "%4$s";
         callListEntryFormat += " " + StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
         callListEntryFormat += "%5$s";
         callListEntryFormat += " " + StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
         callListEntryFormat += "%6$s";
         callListEntryFormat += "\n";
      }

      return (formatPmrCallListTable( pmr, pmrCallData, callListTableHeader,
            callListEntryFormat, sportCommonData ));
   }

   // _B68102A
   /**
    * Generate a formatted PMR call list from PMR call data.
    * 
    * @param pmr PMR work item used to generate the formated PMR call list
    * @param pmrCallData a {@code String} containing the PMR call data
    * @param tableHeader - assumed to match entry definition
    * @param entryFormat - <br>
    *        Expected to handle entries with the following elements: [call
    *        selection#] [Call Queue] [Call Center] [Primary, Secondary,
    *        Backup] [pPG] [RETAIN-id dispatched]
    * @param sportCommonData common data for obtaining and processing SBS data
    * @return a {@code String} containing the formatted PMR call list
    *         generated from {@code pmrCallData}
    * @throws TeamRepositoryException
    */
   public static String formatPmrCallListTable( IWorkItem pmr,
         String pmrCallData, String tableHeader, String entryFormat,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      StringBuilder callList = new StringBuilder();
      // Header format: as passed in
      callList.append( tableHeader );

      // Each table call entry format:
      // PMR:
      // | <call selection#>
      // | <Call Queue> | <Call Center>
      // | <Primary, Secondary, Backup>
      // | <pPG>
      // | <RETAIN-id dispatched>
      if (!pmrCallData.isEmpty())
      {
         int i = 1;
         String pmrName = getPmrName( pmr, sportCommonData );
         // Raw input format:
         // <Call Queue><space><Call Center><space><pPG><newline>
         // _B117320C SBS bridging may also contain ...
         // <space><Call Type><space><DispatchID> after <pPG>
         for (String pmrCallEntry : pmrCallData.split( "\n" ))
         {
            String[] pmrCallEntryFields = pmrCallEntry.split( " " );
            // _B117320C 3 or 5 items per line
            if (pmrCallEntryFields.length < 3
                  || pmrCallEntryFields.length > 5)
            {
               String message = "PMR call entry \"" + pmrCallEntry
                     + "\" for PMR " + pmrName
                     + " contains invalid number of fields";
               sportCommonData.getLog().warn( message );
               continue;
            }
            String callQueue = pmrCallEntryFields[0];
            String callCenter = pmrCallEntryFields[1];
            String callPPG = pmrCallEntryFields[2];
            // _B117320A SBS delivered Type and DispatchID?
            String callType = pmrCallEntryFields.length >= 4
                  ? pmrCallEntryFields[3] : null;
            String callDispatchID = pmrCallEntryFields.length >= 5
                  ? pmrCallEntryFields[4] : null;
            // _B117320A Placeholders may be set in entries.
            // SBS may pass type as <unknown> to specifically set that value.
            callType = (callType != null
                  && callType.equals( "<UNSPECIFIED>" )) ? "" : callType;
            if (callDispatchID != null
                  && callDispatchID.equals( "<UNSPECIFIED>" ))
               callDispatchID = "";
            try
            {
               // _B117320C Do a call browse if call data not supplied
               if (callType == null || callDispatchID == null)
               {
                  ArtifactRecordPart callRecord = browseCall( callQueue,
                        callCenter, callPPG, pmr, sportCommonData );
                  // _B117320C Get from call browse if not supplied
                  callType = callType == null
                        ? getCallType( callRecord, sportCommonData )
                        : callType;
                  callDispatchID = callDispatchID == null
                        ? getCallDispatchedTo( callRecord ) : callDispatchID;
               }
               // Append each call entry - use format passed in
               // _B117320C Use SBS supplied or RTC obtained values.
               callList.append( String.format( entryFormat, i, callType,
                     callQueue, callCenter, callPPG, callDispatchID ) );
               ++i;
            }
            catch (Exception e)
            {
               boolean unexpectedException = true;
               // _B100422 Fix potential infinite loop when not counter
               // other than error explicitly searching for.
               HashSet<Throwable> causeSet = new HashSet<Throwable>();
               Throwable cause = e;
               // _B100422C Not sure if getCause may dig deeper in exception
               // tree. Might just hang out at current exception element.
               // At any rate, stop when re-encounter same cause.
               while ((cause != null) && !causeSet.contains( cause ))
               {
                  if (cause.getMessage()
                        .contains( "The requested Call is not found." ))
                  {
                     unexpectedException = false;
                     String message = "Call " + callPPG + " on queue "
                           + callQueue + "," + callCenter
                           + " associated with PMR " + pmrName
                           + " not found in RETAIN";
                     sportCommonData.getLog().warn( message );
                     break;
                  }
                  causeSet.add( cause ); // _B100422A Save cause encountered
                  cause = e.getCause();
               }
               if (unexpectedException)
               {
                  String message = "Unexpected exception browsing call "
                        + callPPG + " on queue " + callQueue + ","
                        + callCenter + " for PMR " + pmrName;
                  sportCommonData.getLog().warn( message, e );
               }
            }
         }
      }

      return callList.toString();
   }

   // _B68102A
   /**
    * Convert a PMR Call List from HTML format to WIKI table format.
    * 
    * <br>
    * HTML format = <br>
    * Header : (2 lines) <br>
    * Selection | Call Type | Queue | Center | pPG | Dispatched To <br>
    * ----------+-----------+-------+--------+-----+-----------------------
    * 
    * <br>
    * Entries : <br>
    * [Call selection #] | [Call Type = Primary, Secondary, Backup] | [Queue]
    * | [Center] | [pPG] | [RETAIN dispatched ID]
    * 
    * <p>
    * WIKI format = <br>
    * Header : (1 line) <br>
    * |= Selection |= Call Type |= Queue |= Center |= pPG |= Dispatched To
    * 
    * <br>
    * Entries : <br>
    * | [Call selection #] | [Call Type = Primary, Secondary, Backup] |
    * [Queue] | [Center] | [pPG] | [RETAIN dispatched ID]
    * 
    * @param htmlCallList = The HTML format table header, and any call entries
    *        for a PMR work item call list.
    * @return The WIKI format table header, and any call entries in WIKI table
    *         format for a PMR work item call list
    * @throws SportRuntimeException
    * @throws SportUserActionFailException
    */
   public static String convertHtmlToWIKICallList( String htmlCallList )
         throws SportRuntimeException, SportUserActionFailException
   {
      // PMR does not have such a nice method to get the formatting strings
      String callListTableHeader = "";
      // Header format:
      // |= Selection |= Call Type |= Queue |= Center |= pPG |= Dispatched To
      // Also added some Web browser display usability aids
      callListTableHeader += StringUtils.WIKI_TABLE_HEADER_CELL;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += " Selection ";
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_TABLE_HEADER_CELL;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += " Call Type ";
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_TABLE_HEADER_CELL;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += " Queue ";
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_TABLE_HEADER_CELL;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += " Center ";
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_TABLE_HEADER_CELL;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += " pPG ";
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_TABLE_HEADER_CELL;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += " Dispatched To ";
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += "\n";
      if (htmlCallList == null || htmlCallList.isEmpty())
      {
         // Null or empty work item value, still return header
         return callListTableHeader;
      }
      // Beyond this point, HTML value might just be the header
      // or have calls in the content.

      // Each table call entry format:
      // PMR:
      // | <call selection#>
      // | <Primary, Secondary, Backup>
      // | <Call Queue> | <Call Center>
      // | <pPG>
      // | <RETAIN-id dispatched>
      String callListEntryFormat = "";
      callListEntryFormat += StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
      callListEntryFormat += "%1$d";
      callListEntryFormat += " " + StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
      callListEntryFormat += "%2$s";
      callListEntryFormat += " " + StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
      callListEntryFormat += "%3$s";
      callListEntryFormat += " " + StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
      callListEntryFormat += "%4$s";
      callListEntryFormat += " " + StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
      callListEntryFormat += "%5$s";
      callListEntryFormat += " " + StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
      callListEntryFormat += "%6$s";
      callListEntryFormat += "\n";

      // Start with the header (has newline)
      String wikiCallList = callListTableHeader;
      // Gather any call entries in the work item attribute content
      // Get homogenized form for HTML entries (object components)
      // Called method may throw some exception gathering call list data
      ArrayList<CallInfo> allCalls = getAllPMRCalls( htmlCallList,
            StringUtils.BOX_DRAWING_VERTICAL );
      if (allCalls != null)
      {
         int selectedCall = 1;
         for (CallInfo callInfo : allCalls)
         {
            // Add the call entry (has newline)
            // Each table call entry format:
            // PMR:
            // | <call selection#>
            // | <Primary, Secondary, Backup>
            // | <Call Queue> | <Call Center>
            // | <pPG>
            // | <RETAIN-id dispatched>
            wikiCallList += String.format( callListEntryFormat, selectedCall,
                  callInfo.getCallTypeAsString(), callInfo.getQueue(),
                  callInfo.getCenter(), callInfo.getPPG(),
                  callInfo.getDispatchedTo() );
            selectedCall++;
         }
      }

      // Table header + any calls listed in WIKI format.
      return (wikiCallList);
   }

   /**
    * Gets PMR call data from a given SBS PMR record.
    * 
    * @param pmrRecord the SBS PMR record used to generate the formatted PMR
    *        call list
    * @param sportCommonData common data for obtaining and processing SBS data
    * @return a {@code String} containing the PMR call data
    * @throws TeamRepositoryException
    */
   public static String getPmrCallData( ArtifactRecordPart pmrRecord )
   {
      StringBuilder callList = new StringBuilder();
      for (ArtifactRecordPart pmrCallRecord : pmrRecord
            .findRecords( PmrConstants.PMR_CALL_RECORD_NAME ))
      {
         String callQueue = null;
         ArtifactRecordField callQueueField = pmrCallRecord
               .findField( PmrConstants.PMR_CALL_QUEUE_ATTRIBUTE_ID );
         if (callQueueField != null)
         {
            callQueue = callQueueField.getFirstValueAsString();
         }
         if (callQueue == null)
            continue;
         String callCenter = null;
         ArtifactRecordField callCenterField = pmrCallRecord
               .findField( PmrConstants.PMR_CALL_CENTER_ATTRIBUTE_ID );
         if (callCenterField != null)
         {
            callCenter = callCenterField.getFirstValueAsString();
         }
         if (callCenter == null)
            continue;
         String callPriority = null;
         String callPage = null;
         ArtifactRecordField callPPGField = pmrCallRecord
               .findField( PmrConstants.PMR_CALL_PPG_ATTRIBUTE_ID );
         if (callPPGField == null)
            continue;
         ArtifactFieldValue callPriorityValue = callPPGField
               .getFieldValue( 0 );
         if (callPriorityValue != null)
         {
            callPriority = callPriorityValue.getValue();
         }
         if (callPriority == null)
            continue;
         ArtifactFieldValue callPageValue = callPPGField.getFieldValue( 1 );
         if (callPageValue != null)
         {
            callPage = callPageValue.getValue();
         }
         if (callPage == null)
            continue;
         if (callPage.length() == 1)
            callPage = "0" + callPage;
         String callPPG = callPriority + callPage;
         if (callList.length() != 0)
            callList.append( '\n' );
         callList.append( callQueue );
         callList.append( ' ' );
         callList.append( callCenter );
         callList.append( ' ' );
         callList.append( callPPG );
      }
      return callList.toString();
   }

   //
   // _T1334A UnSet PMR in RETAIN indicator.
   // Called when creating an PMR already in RETAIN is successful.
   public static void unsetPmrInRETAINIndicator( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // Obtain indicator in source work item
      boolean inRETAIN = RtcUtils.getAttributeValueAsBoolean( pmr,
            PmrConstants.PMR_IN_RETAIN_ATTRIBUTE_ID, sportCommonData );
      if (inRETAIN)
      {
         // Need to use working copy of work item here
         IWorkItem pmrToUpdate = sportCommonData.getWorkItemForUpdate();
         // Unset PMR In RETAIN
         RtcUtils.setWorkItemAttributeToValue(
               PmrConstants.PMR_IN_RETAIN_ATTRIBUTE_ID, "FALSE", pmrToUpdate,
               sportCommonData );
         // Save done later at end of participant processing
      }
   }

   public static ArrayList<String> splitPmrName( String pmrName )
   {
      StringTokenizer st;
      ArrayList<String> arrayList = new ArrayList<String>();
      if (pmrName.indexOf( "," ) != -1)
      {
         st = new StringTokenizer( pmrName, "," );
         while (st.hasMoreTokens())
         {
            arrayList.add( st.nextToken() );
         }
      }
      else
      {
         st = new StringTokenizer( pmrName, " " );
         while (st.hasMoreTokens())
         {
            arrayList.add( st.nextToken() );
         }
      }
      return arrayList;
   }

   // _T155539A Fix bug in PMR name test.
   /**
    * Reuse the method that splits a PMR name at commas or blanks. Only return
    * a value if it conforms to the format, when split - <br>
    * ppppp (NOT NONE or a default 9X999 or default 99999) <br>
    * bbb <br>
    * ccc <br>
    * This is to return a value that is "expected" to be a valid PMR name in
    * RETAIN. ALWAYS a 3 part value; 5 chars + 3 chars + 3 chars. Otherwise a
    * null is returned.
    * 
    * @param pmrName
    * @return
    */
   public static ArrayList<String> extractNormalizedPmrName( String pmrName )
   {
      if (pmrName == null || pmrName.trim().length() == 0)
         return null;

      // Reuse the method that seems to split at comma or blank.
      // Use an upper case version for ALPHA-numeric values.
      ArrayList<String> normalizedPmrValues = splitPmrName(
            pmrName.toUpperCase( Locale.ENGLISH ) );
      if (normalizedPmrValues == null || normalizedPmrValues.size() != 3)
         return null;

      String pmrNumber = normalizedPmrValues.get( 0 );
      String pmrBranch = normalizedPmrValues.get( 1 );
      String pmrCountry = normalizedPmrValues.get( 2 );
      if (pmrNumber == null || pmrBranch == null || pmrCountry == null
            || pmrNumber.trim().length() != 5
            || pmrNumber.startsWith( "NONE" )
            || pmrNumber.startsWith( "9X999" )
            || pmrNumber.startsWith( "99999" )
            || pmrNumber.startsWith( "LOST" )
            || PmrUtils.hasMoreThanOneAlphabeticCharacter( pmrNumber )
            || PmrUtils.hasOneAlphaCharandFourNines( pmrNumber )
            || PmrUtils.hasInvalidChars( pmrNumber )
            || pmrBranch.trim().length() != 3
            || pmrCountry.trim().length() != 3)
         return null;
      else
         return normalizedPmrValues;
   }

   // _T1083A Check if doing an PMR create
   public static boolean containsPmrCreateAction( String[] actions,
         String workItemType )
   {
      // Initially not a create action
      boolean containsPmrCreateAction = false;
      // Determine if PMR create (possibly from SBS)
      if (workItemType.equals( PmrConstants.WORK_ITEM_TYPE_ID ))
      {
         // Now know its an PMR
         for (String action : actions)
         {
            String[] actionParts = action.split( "/" );
            String actionType = actionParts[0];
            if ((actionParts.length == 2) && actionType.equals( "action" ))
            {
               String actionName = actionParts[1];
               if (actionName.equals( PmrConstants.RTC_CREATE_ACTION )
                     || actionName.equals(
                           PmrConstants.RTC_CREATE_IN_RETAIN_ACTION ))
               {
                  // Processing an PMR create
                  containsPmrCreateAction = true;
                  break;
               }
            }
         }
      }
      return containsPmrCreateAction;
   }

   public static void createPmr( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      boolean pmrInRetain = RtcUtils.getAttributeValueAsBoolean( pmr,
            PmrConstants.PMR_IN_RETAIN_ATTRIBUTE_ID, sportCommonData );
      String pmrName = RtcUtils.getAttributeValueAsString( pmr,
            PmrConstants.PMR_NAME_ATTRIBUTE_ID, sportCommonData );
      ArrayList<String> pmrIds = splitPmrName( pmrName );
      if (pmrIds.size() == 3)
      {
         IAttribute pmrNoAttribute = RtcUtils.findAttribute( pmr,
               PmrConstants.PMR_NO_ATTRIBUTE_ID, sportCommonData );
         RtcUtils.setAttributeValue( pmr, pmrNoAttribute,
               (String)pmrIds.get( 0 ), sportCommonData );
         IAttribute branchAttribute = RtcUtils.findAttribute( pmr,
               PmrConstants.BRANCH_ATTRIBUTE_ID, sportCommonData );
         RtcUtils.setAttributeValue( pmr, branchAttribute,
               (String)pmrIds.get( 1 ), sportCommonData );
         IAttribute countryAttribute = RtcUtils.findAttribute( pmr,
               PmrConstants.COUNTRY_ATTRIBUTE_ID, sportCommonData );
         RtcUtils.setAttributeValue( pmr, countryAttribute,
               (String)pmrIds.get( 2 ), sportCommonData );
      }

      if (pmrInRetain)
      {
         // _T155539C A PMR browse is now done up-front in the
         // operation advisor in order to determine the RETAIN create
         // date for proper uniqueness testing.
         // That advisor browse also primes a working copy of the PMR
         // work item with that RETAIN data. So it is redundant to do
         // that again here. This operation participant function only
         // needs to do any follow-up work.
         // A consideration was made to delay the call list
         // construction until this point, but that would need the
         // RETAIN PMR record, requiring a redundant RETAIN SBS call.
         //
         // NOTE: This may also be called from Service Utils with
         // "In RETAIN" flag set true for APAR subscribed-PMR handling.
         // In which case a "pre-browse" SBS artifact may be saved in
         // the common data.
         // Map of PMR name to multi-purpose map of PMR browse result
         // artifact, RTC query conditions, etc. Not utilized in RTC
         // UI PMR-in-RETAIN Create action.
         HashMap<String, HashMap<String, Object>> referencedPMRs = sportCommonData
               .getReferencedPMRs();
         if (referencedPMRs != null)
         {
            Artifact pmrBrowseArtifact = null;
            // _T155539A Map of key attribute to object value for RTC
            // query for each PMR. With special entries -
            // a. PMR work item (once found) - to prevent redundant
            // RTC queries (not used on create function)
            // b. PMR browse artifact - to prevent redundant PMR
            // browses
            // c. active saves key
            // d. error message (String) key
            // Make a common practice to use the upper case PMR name as key.
            HashMap<String, Object> pmrUniqueKeys = referencedPMRs
                  .get( pmrName.toUpperCase( Locale.ENGLISH ) );
            if (pmrUniqueKeys != null)
            {
               // Get the pre-browse artifact if it exists.
               // Known to be an Artifact object.
               pmrBrowseArtifact = (Artifact)pmrUniqueKeys
                     .get( "***PMRBrowseSBSResultArtifact***" );
            }
            if (pmrBrowseArtifact == null)
            {
               // Might not have a pre-browse artifact, or PMR does
               // not exist. Let RETAIN tell us (via SBS call)...
               updatePmrFromRetain( pmr, sportCommonData );
            }
            else
            {
               // No redundant PMR browse required - reuse browse
               // already done.
               populateWorkItemFromArtifact( pmr, pmrBrowseArtifact,
                     sportCommonData );
            }
         }
         // else if null, RTC UI function that already primed working
         // copy of work item in progress. Also internally created PMR
         // (with in-RETAIN set) for APAR subscribe purposes will
         // already have been populated.

         // updatePmrFromRetain( pmr, sportCommonData );
         // _T1334A - Unset indicator on successful create
         unsetPmrInRETAINIndicator( pmr, sportCommonData );
         CommonUtils.registerComponentUse( sportCommonData, pmr );
      }
      else
      {
         createPmrInRetain( pmr, sportCommonData );
         // IAttribute inputTextAttribute = RtcUtils.findAttribute( pmr,
         // PmrConstants.INPUT_TEXT_ATTRIBUTE_ID, sportCommonData );
         // RtcUtils.setAttributeValue( pmr, inputTextAttribute, "",
         // sportCommonData );
      }

   }

   public static String createPmrInRetain( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( pmr,
            PmrConstants.SBS_CREATE_ACTION,
            PmrConstants.SBS_CREATE_ACTION_ATTRIBUTE_IDS, sportCommonData );
      Artifact sbsResponse = SbsUtils.sendArtifacts( artifact,
            sportCommonData );
      String pmrNo = sbsResponse
            .findFieldValue( PmrConstants.PMR_NO_ATTRIBUTE_ID );
      IAttribute pmrNoAttribute = RtcUtils.findAttribute( pmr,
            PmrConstants.PMR_NO_ATTRIBUTE_ID, sportCommonData );
      RtcUtils.setAttributeValue( pmr, pmrNoAttribute, pmrNo,
            sportCommonData );
      updatePmrFromRetain( pmr, sportCommonData );
      CommonUtils.registerComponentUse( sportCommonData, pmr );
      return pmrNo;
   }

   public static void resetModifyAction( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IWorkItem pmrToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute modifyActionAttribute = RtcUtils.findAttribute( pmrToUpdate,
            PmrConstants.MODIFY_ACTION_ATTRIBUTE_ID, sportCommonData );
      RtcUtils.setEnumerationAttributeValue( pmrToUpdate,
            modifyActionAttribute,
            PmrConstants.RTC_ENTER_MODIFY_ACTION_ACTION, sportCommonData );
   }

   public static void addCallInfoToPmrRecord( ArtifactRecordPart pmrRecord,
         CallInfo callInfo )
   {
      pmrRecord.addField( PmrConstants.QUEUE_ATTRIBUTE_ID,
            callInfo.getQueue() );
      pmrRecord.addField( PmrConstants.CENTER_ATTRIBUTE_ID,
            callInfo.getCenter() );
      ArtifactRecordField pPGField = pmrRecord
            .addField( PmrConstants.PPG_ATTRIBUTE_ID );
      pPGField.addValue( callInfo.getPPG().substring( 0, 1 ) );
      pPGField.addValue( callInfo.getPPG().substring( 1, 3 ) );
   }

   public static ArtifactRecordPart browseCall( String callQueue,
         String callCenter, String callPPG, IWorkItem pmr,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      try
      {
         CallInfo callInfo = new CallInfo();
         callInfo.setQueue( callQueue );
         callInfo.setCenter( callCenter );
         callInfo.setPPG( callPPG );
         return pmrCallBrowse( pmr, callInfo, sportCommonData );
      }
      catch (SportUserActionFailException e)
      {
         throw new SportRuntimeException(
               "Unexpected SportUserActionFailException encountered:", e );
      }
   }

   public static String getCallType( ArtifactRecordPart callRecord,
         SportCommonData sportCommonData )
   {
      String callTypeValue = callRecord.findFieldValue(
            PmrConstants.CALL_PRIMARY_SECONDARY_ATTRIBUTE_ID );
      if (callTypeValue == null)
      {
         sportCommonData.getLog()
               .warn( PmrConstants.CALL_PRIMARY_SECONDARY_ATTRIBUTE_ID
                     + "field not found in call record." );
         return "";
      }
      return getCallType( callTypeValue, sportCommonData );
   }

   public static String getCallType( String callTypeValue,
         SportCommonData sportCommonData )
   {
      if (callTypeValue.equals( "B" ))
         return "Backup";
      else if (callTypeValue.equals( "P" ))
         return "Primary";
      else if (callTypeValue.equals( "S" ))
         return "Secondary";
      else
      {
         sportCommonData.getLog()
               .warn( "Unexpected "
                     + PmrConstants.CALL_PRIMARY_SECONDARY_ATTRIBUTE_ID
                     + " value of \"" + callTypeValue
                     + "\" found in call browse response." );
         return callTypeValue;
      }
   }

   public static String getCallDispatchedTo( ArtifactRecordPart callRecord )
   {
      String callDispatched = callRecord
            .findFieldValue( PmrConstants.CALL_DISPATCHED_ATTRIBUTE_ID );
      if ((callDispatched != null) && callDispatched.equals( "Y" ))
      {
         String serialDispatched = callRecord
               .findFieldValue( PmrConstants.SERIAL_DISPATCHED_ATTRIBUTE_ID );
         return (serialDispatched == null) ? "" : serialDispatched;
      }
      else
      {
         return "";
      }
   }

   public static String getPmrName( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String pmrName = RtcUtils.getAttributeValueAsString( pmr,
            PmrConstants.PMR_NAME_ATTRIBUTE_ID, sportCommonData );
      if (pmrName == null)
      {
         String pmrNo = RtcUtils.getAttributeValueAsString( pmr,
               PmrConstants.PMR_NO_ATTRIBUTE_ID, sportCommonData );
         String branch = RtcUtils.getAttributeValueAsString( pmr,
               PmrConstants.BRANCH_ATTRIBUTE_ID, sportCommonData );
         String country = RtcUtils.getAttributeValueAsString( pmr,
               PmrConstants.COUNTRY_ATTRIBUTE_ID, sportCommonData );
         pmrName = pmrNo + "," + branch + "," + country;
      }
      return pmrName;
   }

   public static void markAsSanitized( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {

      IWorkItem pmrToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute sanitizedAttribute = RtcUtils.findAttribute( pmrToUpdate,
            PmrConstants.SANITIZED_ATTRIBUTE_ID, sportCommonData );
      RtcUtils.setBooleanAttributeValue( pmrToUpdate, sanitizedAttribute,
            "true", sportCommonData );

   }

   public static void markAsNotSanitized( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {

      IWorkItem pmrToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute sanitizedAttribute = RtcUtils.findAttribute( pmrToUpdate,
            PmrConstants.SANITIZED_ATTRIBUTE_ID, sportCommonData );
      RtcUtils.setBooleanAttributeValue( pmrToUpdate, sanitizedAttribute,
            "false", sportCommonData );

   }

   public static void clearPIDataInPmr( IWorkItem pmr,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {

      // Set the PI data attributes to ""
      // See
      // https://apps.na.collabserv.com/wikis/home?lang=en-us#!/wiki/W69c7d6df7474_4a9a_bb93_2bb3c7736996/page/SDI%20Interface%20Requirements
      // for list of PI data fields
      IWorkItem pmrToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute cityAttribute = RtcUtils.findAttribute( pmrToUpdate,
            PmrConstants.CITY_ATTRIBUTE_ID, sportCommonData );
      if (cityAttribute != null)
         RtcUtils.setAttributeValue( pmrToUpdate, cityAttribute, "",
               sportCommonData );
      IAttribute contactNameAttribute = RtcUtils.findAttribute( pmrToUpdate,
            PmrConstants.CONTACT_NAME_ATTRIBUTE_ID, sportCommonData );
      if (contactNameAttribute != null)
         RtcUtils.setAttributeValue( pmrToUpdate, contactNameAttribute, "",
               sportCommonData );
      IAttribute contactPhone1Attribute = RtcUtils.findAttribute( pmrToUpdate,
            PmrConstants.CONTACT_PHONE_1_ATTRIBUTE_ID, sportCommonData );
      if (contactPhone1Attribute != null)
         RtcUtils.setAttributeValue( pmrToUpdate, contactPhone1Attribute, "",
               sportCommonData );
      IAttribute contactPhone2Attribute = RtcUtils.findAttribute( pmrToUpdate,
            PmrConstants.CONTACT_PHONE_2_ATTRIBUTE_ID, sportCommonData );
      if (contactPhone2Attribute != null)
         RtcUtils.setAttributeValue( pmrToUpdate, contactPhone2Attribute, "",
               sportCommonData );
      IAttribute emailAttribute = RtcUtils.findAttribute( pmrToUpdate,
            PmrConstants.EMAIL_ADDRESS_ATTRIBUTE_ID, sportCommonData );
      if (emailAttribute != null)
         RtcUtils.setAttributeValue( pmrToUpdate, emailAttribute, "",
               sportCommonData );

      // Data has been cleared, now mark it as sanitized
      markAsSanitized( pmr, sportCommonData );

   }

   /**
    * Determine if a PMR number has more than one alphabetic character
    * 
    * @param pmrName
    * @return true if pmrName contains more than one alphabetic character
    */
   public static boolean hasMoreThanOneAlphabeticCharacter( String pmrName )
   {
      boolean moreThanOneAlpha = false;

      char c;
      int numAlphabetic = 0;

      for (int i = 0; i < pmrName.length(); i++)
      {
         c = pmrName.charAt( i );
         if (Character.isAlphabetic( c ))
            numAlphabetic++;
      }

      if (numAlphabetic > 1)
         moreThanOneAlpha = true;

      return moreThanOneAlpha;
   }

   /**
    * Determine if a PMR number has one alphabetic character and four nines
    * 
    * @param pmrName
    * @return true if pmrName contains one alphabetic character and four nines
    */
   public static boolean hasOneAlphaCharandFourNines( String pmrName )
   {
      boolean oneAlphaFourNines = false;

      char c;
      int numAlphabetic = 0;
      int numNines = 0;

      for (int i = 0; i < pmrName.length(); i++)
      {
         c = pmrName.charAt( i );
         if (Character.isAlphabetic( c ))
            numAlphabetic++;
         else if (Character.isDigit( c ))
         {
            if (c == '9')
               numNines++;
         }
      }

      if ((numAlphabetic == 1) && (numNines == 4))
         oneAlphaFourNines = true;

      return oneAlphaFourNines;
   }

   /**
    * Determine if a PMR number has invalid characters
    * 
    * @param pmrName
    * @return true if pmrName contains invalid characters
    */
   public static boolean hasInvalidChars( String pmrName )
   {

      boolean containsInvalidChars = false;
      char c;

      for (int i = 0; i < pmrName.length(); i++)
      {
         c = pmrName.charAt( i );
         if (Character.isAlphabetic( c ))
         {
            if ((c != 'X') && (c != 'A') && (c != 'B') && (c != 'E')
                  && (c != 'x') && (c != 'a') && (c != 'b') && (c != 'e'))
               containsInvalidChars = true;
         }
         else if (!Character.isDigit( c ))
            containsInvalidChars = true;
      }

      return containsInvalidChars;

   }

}
