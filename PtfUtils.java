package com.ibm.sport.rtc.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.ArtifactTechnology.common.Artifact;
import com.ibm.ArtifactTechnology.common.ArtifactRecordField;
import com.ibm.ArtifactTechnology.common.ArtifactRecordPart;
import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.expression.IQueryableAttribute;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.query.IQueryResult;
import com.ibm.team.workitem.common.query.IResolvedResult;

public class PtfUtils
{

   private static Map<String, Long> activePtfSaves = new HashMap<String, Long>();
   private final static long PTF_SAVE_TIMEOUT = 15 * 60 * 1000; // 15 minutes

   // Create thread for cleaning up active PTF saves that fail in a non-SPoRT
   // advisor or participant.
   static
   {
      Thread thread = new Thread( new Runnable()
      {
         public void run()
         {
            Log log = LogFactory.getLog( PtfUtils.class );
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
               synchronized (activePtfSaves)
               {
                  boolean notifyAllNeeded = false;
                  Iterator<Map.Entry<String, Long>> iterator = activePtfSaves
                        .entrySet().iterator();
                  while (iterator.hasNext())
                  {
                     Map.Entry<String, Long> activePtfSave = iterator.next();
                     long createTime = activePtfSave.getValue().longValue();
                     if ((System.currentTimeMillis()
                           - createTime) >= PTF_SAVE_TIMEOUT)
                     {
                        iterator.remove();
                        notifyAllNeeded = true;
                        String ptfNum = activePtfSave.getKey();
                        String message = "Removing " + ptfNum
                              + " from active PTF saves due to timeout";
                        log.warn( message );
                     }
                  }
                  if (notifyAllNeeded)
                  {
                     activePtfSaves.notifyAll();
                  }
               }
            }
         }
      } );
      thread.start();
   }

   public static void performDuplicatePtfCheck( String[] actions,
         IWorkItem ptf, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      SbsUtils.checkForModfyAndChangeStateAction( actions,
            PtfConstants.MODIFY_ACTION_ATTRIBUTE_ID, sportCommonData );
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         if ((actionParts.length == 2) && actionParts[0].equals( "action" ))
         {
            String actionName = actionParts[1];
            if (actionName.equals( PtfConstants.RTC_CREATE_ACTION ))
            {
               String ptfNum = RtcUtils.getAttributeValueAsString( ptf,
                     PtfConstants.PTF_NUM_ATTRIBUTE_ID, sportCommonData );
               if ((ptfNum != null) && !ptfNum.equals( "" ))
               {
                  performDuplicatePtfCheck( ptfNum, sportCommonData );
               }
            }
         }
      }
   }

   public static void performDuplicatePtfCheck( String ptfNum,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      ptfNum = ptfNum.toUpperCase( Locale.ENGLISH );
      synchronized (activePtfSaves)
      {
         if (findPtf( ptfNum, sportCommonData ) != null)
         {
            String message = ptfNum + " already exists in the RTC repository";
            throw new SportUserActionFailException( message );
         }
         else
         {
            if (activePtfSaves.containsKey( ptfNum ))
            {
               String message = ptfNum + " save currently in progress";
               throw new SportUserActionFailException( message );
            }
            else
            {
               activePtfSaves.put( ptfNum,
                     new Long( System.currentTimeMillis() ) );
            }
         }
      }
   }

   public static void removeActivePtfSave( String ptfNum )
   {
      ptfNum = ptfNum.toUpperCase( Locale.ENGLISH );
      synchronized (activePtfSaves)
      {
         Long value = activePtfSaves.remove( ptfNum );
         if (value != null)
         {
            activePtfSaves.notifyAll();
         }
      }
   }

   public static void performPtfSavePostconditionActions( String[] actions,
         IWorkItem ptf, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         String actionType = actionParts[0];
         if ((actionParts.length == 2) && actionType.equals( "action" ))
         {
            String actionName = actionParts[1];
            if (actionName.equals( PtfConstants.RTC_CREATE_ACTION ))
            {
               createPtf( ptf, sportCommonData );
               // This allows the Summary to initially be set from abstract
               // Draft, Both RTC and RETAIN, RTC-only (in RETAIN)
               setPtfSummaryOnCreate( ptf, sportCommonData );
            }
            if (actionName.equals( PtfConstants.RTC_CREATE_IN_RETAIN_ACTION ))
            {
               createPtfInRetain( ptf, sportCommonData );
               // This allows the Summary to initially be set from abstract
               unsetPtfDraftIndicator( ptf, sportCommonData );
               // Draft, Both RTC and RETAIN, RTC-only (in RETAIN)
               setPtfSummaryOnCreate( ptf, sportCommonData );
            }
            else if (actionName.equals( PtfConstants.RTC_RECEIPT_ACTION ))
            {
               receiptPtf( ptf, sportCommonData );
            }
            else if (actionName.equals( PtfConstants.RTC_TOTEST_ACTION ))
            {
               toTestPtf( ptf, sportCommonData );
            }
            else if (actionName.equals( PtfConstants.RTC_ENDTEST_ACTION ))
            {
               endTestPTF( ptf, sportCommonData );
            }
            else if (actionName.equals( PtfConstants.RTC_ATCONTROL_ACTION ))
            {
               transAT( ptf, sportCommonData );
            }
            else if (actionName.equals( PtfConstants.RTC_CLOSE_ACTION ))
            {
               closePtf( ptf, sportCommonData );
            }
            else if (actionName.equals( PtfConstants.RTC_REOPEN_ACTION ))
            {
               reopenPtf( ptf, sportCommonData );
            }
         }
         else if ((actionParts.length == 2) && actionType.equals( "modify" ))
         {
            String fieldName = actionParts[1];
            if (fieldName.equals( PtfConstants.MODIFY_ACTION_ATTRIBUTE_ID ))
               performModifyAction( ptf, sportCommonData, actions );
         }
      }
   }

   public static void performModifyAction( IWorkItem ptf,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Log log = sportCommonData.getLog();
      String modifyAction = RtcUtils.getAttributeValueAsString( ptf,
            PtfConstants.MODIFY_ACTION_ATTRIBUTE_ID, sportCommonData );
      if (modifyAction.equals( PtfConstants.RTC_ENTER_MODIFY_ACTION_ACTION ))
      {
         String message = "Unexpected \""
               + PtfConstants.RTC_ENTER_MODIFY_ACTION_ACTION
               + "\" action encountered";
         log.warn( message );
         return;
      }
      if (modifyAction.equals( PtfConstants.RTC_REFRESH_FROM_RETAIN_ACTION ))
         updatePtfFromRetain( ptf, sportCommonData );
      else if (modifyAction.equals( PtfConstants.RTC_ASSIGN_ACTION ))
         assignPtf( ptf, sportCommonData );
      else if (modifyAction.equals( PtfConstants.RTC_COPYSENT_ACTION ))
         copySentPtf( ptf, sportCommonData );
      else if (modifyAction.equals( PtfConstants.RTC_REROUTE_ACTION ))
         reroutePtf( ptf, actions, sportCommonData );
      else if (modifyAction.equals( PtfConstants.RTC_TRANSFERORDER_ACTION ))
         transferPsppPendingOrder( ptf, sportCommonData );
      else if (modifyAction.equals( PtfConstants.RTC_EDIT_ACTION ))
         editPtf( ptf, actions, sportCommonData );
      else if (modifyAction
            .equals( PtfConstants.RTC_MARKPTFASUNAVAILABLE_ACTION ))
         closeNAVPtf( ptf, sportCommonData );
      else if (modifyAction.equals( PtfConstants.RTC_MODIFY_DRAFT_ACTION ))
      {
      }
      else if (modifyAction
            .equals( PtfConstants.RTC_MARK_AS_SANITIZED_ACTION ))
         markAsSanitized( ptf, sportCommonData );
      else if (modifyAction
            .equals( PtfConstants.RTC_MARK_AS_NOT_SANITIZED_ACTION ))
         markAsNotSanitized( ptf, sportCommonData );
      else if (modifyAction.equals( PtfConstants.RTC_CLEAR_PI_DATA_ACTION ))
         clearPIDataInPtf( ptf, sportCommonData );
      resetModifyAction( ptf, sportCommonData );
   }

   public static void setPtfSummaryOnCreate( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // Need to use working copy of work item here if it exists,
      // as Create PTF From RETAIN initial work item will NOT have
      // the CommentLine attribute set (only has ptfNum).
      IWorkItem ptfForUpdate = sportCommonData.getWorkItemForUpdate();
      boolean createAsDraft = RtcUtils.getAttributeValueAsBoolean(
            ptfForUpdate, PtfConstants.CREATE_AS_DRAFT_ATTRIBUTE_ID,
            sportCommonData );
      if (createAsDraft)
      {
         // Summary = Draft PTF <id>
         setPtfSummaryToValue(
               "Draft PTF " + Integer.toString( ptfForUpdate.getId() ),
               ptfForUpdate, sportCommonData );
      }
      else
      {
         String[] ptfAttributes = { PtfConstants.PTF_NUM_ATTRIBUTE_ID,
               PtfConstants.ABSTRACT_TEXT_ATTRIBUTE_ID };
         // Summary = ptfNum + abstract
         setPtfSummaryFromAttribute( ptfAttributes, ptfForUpdate,
               sportCommonData );
      }
   }

   public static void setPtfSummaryFromAttribute( String[] fromAttributeIds,
         IWorkItem ptf, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String ptfSummaryValue = "";
      for (int i = 0; i < fromAttributeIds.length; i++)
      {
         IAttribute ptfAttr = RtcUtils.findAttribute( ptf,
               fromAttributeIds[i], sportCommonData );
         // Test for null attribute to avoid exception
         if (ptfAttr != null)
         {
            ptfSummaryValue = ptfSummaryValue + ((i > 0) ? " " : "")
                  + RtcUtils.getAttributeValueAsString( ptf,
                        ptfAttr.getIdentifier(), sportCommonData );
         }
      }
      setPtfSummaryToValue( ptfSummaryValue, ptf, sportCommonData );
   }

   public static void setPtfSummaryToValue( String ptfSummaryValue,
         IWorkItem ptf, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      if (ptfSummaryValue != null && ptf != null)
      {
         // Get or reuse working copy
         IWorkItem ptfToUpdate = sportCommonData.getWorkItemForUpdate();
         // Set HTML built-in Summary from string
         ptfToUpdate.setHTMLSummary(
               XMLString.createFromPlainText( ptfSummaryValue ) );
         // _T1083A To reuse working copy, save done at end of Participant
      }
   }

   public static IWorkItem findPtf( String ptfNum,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IQueryableAttribute ptfNumAttribute = RtcUtils.findQueryableAttribute(
            PtfConstants.PTF_NUM_ATTRIBUTE_ID, sportCommonData );
      IQueryableAttribute workItemTypeAttribute = RtcUtils
            .findQueryableAttribute( IWorkItem.TYPE_PROPERTY,
                  sportCommonData );
      Map<IQueryableAttribute, Object> attributeValues = new HashMap<IQueryableAttribute, Object>();
      attributeValues.put( ptfNumAttribute, ptfNum );
      attributeValues.put( workItemTypeAttribute,
            PtfConstants.WORK_ITEM_TYPE_ID );
      IQueryResult<IResolvedResult<IWorkItem>> ptfs = RtcUtils
            .queryWorkItemsByAttributes( attributeValues, sportCommonData );
      int ptfCount = ptfs.getResultSize( monitor ).getTotalAvailable();
      if (ptfCount > 1)
         throw new SportRuntimeException(
               "More than one PTF named \"" + ptfNum + "\" found in RTC" );
      return (ptfCount == 0) ? null : ptfs.next( monitor ).getItem();
   }

   public static IWorkItem findPtf( String ptfNum, boolean willCreatePtf,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      ptfNum = ptfNum.toUpperCase( Locale.ENGLISH );
      synchronized (activePtfSaves)
      {
         IWorkItem ptf = findPtf( ptfNum, sportCommonData );
         if (ptf == null)
         {
            while (activePtfSaves.containsKey( ptfNum ))
            {
               try
               {
                  activePtfSaves.wait();
               }
               catch (InterruptedException e)
               {
                  String message = "Unexpected interruption waiting on active save of "
                        + ptfNum;
                  throw new SportRuntimeException( message, e );
               }
            }
            ptf = findPtf( ptfNum, sportCommonData );
         }
         if ((ptf == null) && willCreatePtf)
         {
            activePtfSaves.put( ptfNum,
                  new Long( System.currentTimeMillis() ) );
         }
         return ptf;
      }
   }

   public static void updatePtfFromRetain( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_BROWSE_ACTION,
            PtfConstants.SBS_BROWSE_ACTION_ATTRIBUTE_IDS, sportCommonData );
      Artifact sbsResponse = SbsUtils.sendArtifacts( artifact,
            sportCommonData );
      ArtifactRecordPart ptfRecord = sbsResponse
            .findRecord( ptf.getWorkItemType() );
      String sysrouteNum = ptfRecord
            .findField( PtfConstants.SYSROUTE_NUM_ATTRIBUTE_ID )
            .getFirstValueAsString();
      if ((sysrouteNum == null) || sysrouteNum.equals( "" ))
      {
         String ptfNum = ptfRecord
               .findField( PtfConstants.PTF_NUM_ATTRIBUTE_ID )
               .getFirstValueAsString();
         throw new SportUserActionFailException( ptfNum + " is not a PTF" );
      }

      IWorkItem ptfToUpdate = sportCommonData.getWorkItemForUpdate();
      // Get the sanitized attribute
      IAttribute sanitizedAttr = RtcUtils.findAttribute( ptf,
            PtfConstants.SANITIZED_ATTRIBUTE_ID, sportCommonData );
      Boolean sanitized = (Boolean)ptfToUpdate.getValue( sanitizedAttr );
      for (ArtifactRecordField field : ptfRecord.getFields())
      {
         String fieldName = field.getName();
         String fieldValue = field.getAllValuesAsString();
         // this is because FeedbackResultText attribute has a default text
         // value
         // and we don't want to loose it if the field comes empty for new ptf
         // that
         // did not fill the feedback page yet.
         if (fieldName
               .equals( PtfConstants.FEEDBACK_RESULT_TEXT_ATTRIBUTE_ID )
               && fieldValue == null)
            continue;
         // _T1083C Use new common utility method to set customized
         // attributes. Reuses working copy if obtained.
         // If sanitized, don't update RETAIN identified PI data fields
         if (!sanitized)
         {
            RtcUtils.setWorkItemAttributeToValue( fieldName, fieldValue, ptf,
                  sportCommonData );
         }
         else
         {
            if (!fieldName.equals( PtfConstants.CONTACT_PHONE_ATTRIBUTE_ID )
                  && (!fieldName
                        .equals( PtfConstants.BO_ADDRESS_1_ATTRIBUTE_ID ))
                  && (!fieldName
                        .equals( PtfConstants.BO_ADDRESS_2_ATTRIBUTE_ID ))
                  && (!fieldName
                        .equals( PtfConstants.BO_CITY_STATE_ATTRIBUTE_ID ))
                  && (!fieldName.equals( PtfConstants.BO_ZIP_ATTRIBUTE_ID ))
                  && (!fieldName
                        .equals( PtfConstants.SUBMITTER_ATTRIBUTE_ID ))
                  && (!fieldName.equals(
                        PtfConstants.CUSTOMER_COUNTRY_ATTRIBUTE_ID )))
            {
               {
                  RtcUtils.setWorkItemAttributeToValue( fieldName, fieldValue,
                        ptf, sportCommonData );
               }
            }
         }
         // _T1083C Built-in attributes require using their own access methods
         // (Work flow state method gets working copy explicitly passed in.)
         if (fieldName.equals( PtfConstants.RETAIN_STATUS_ATTRIBUTE_ID ))
            RtcUtils.setWorkflowState( ptfToUpdate, fieldValue,
                  sportCommonData );
         if (fieldName.equals( SbsUtils.STATUSMESSAGE_FIELD ))
         {
            RtcUtils.setWorkItemAttributeToValue(
                  PtfConstants.STATUS_MESSAGE_ATTRIBUTE_ID, fieldValue, ptf,
                  sportCommonData );
         }
      }
   }

   // _T1334A UnSet PTF in RETAIN indicator.
   // Called when creating an PTF already in RETAIN is successful.
   public static void unsetPtfInRETAINIndicator( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // Obtain indicator in source work item
      boolean inRETAIN = RtcUtils.getAttributeValueAsBoolean( ptf,
            PtfConstants.PTF_IN_RETAIN_ATTRIBUTE_ID, sportCommonData );
      if (inRETAIN)
      {
         // Need to use working copy of work item here
         IWorkItem ptfToUpdate = sportCommonData.getWorkItemForUpdate();
         // Unset PTF In RETAIN
         RtcUtils.setWorkItemAttributeToValue(
               PtfConstants.PTF_IN_RETAIN_ATTRIBUTE_ID, "FALSE", ptfToUpdate,
               sportCommonData );
         // Save done later at end of participant processing
      }
   }

   // _T1083A Check if doing an PTF create
   public static boolean containsPtfCreateAction( String[] actions,
         String workItemType )
   {
      // Initially not a create action
      boolean containsPtfCreateAction = false;
      // Determine if PTF create (possibly from SBS)
      if (workItemType.equals( PtfConstants.WORK_ITEM_TYPE_ID ))
      {
         // Now know its an PTF
         for (String action : actions)
         {
            String[] actionParts = action.split( "/" );
            String actionType = actionParts[0];
            if ((actionParts.length == 2) && actionType.equals( "action" ))
            {
               String actionName = actionParts[1];
               if (actionName.equals( PtfConstants.RTC_CREATE_ACTION )
                     || actionName.equals(
                           PtfConstants.RTC_CREATE_IN_RETAIN_ACTION ))
               {
                  // Processing an PTF create
                  containsPtfCreateAction = true;
                  break;
               }
            }
         }
      }
      return containsPtfCreateAction;
   }

   public static void copySentPtf( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      List<String> copySentAttrs = new ArrayList<String>();

      // Add the attributes to the list that can be sent every time
      for (int i = 0; i < PtfConstants.SBS_COPYSENT_ACTION_ATTRIBUTE_IDS.length; i++)
      {
         copySentAttrs
               .add( PtfConstants.SBS_COPYSENT_ACTION_ATTRIBUTE_IDS[i] );
      }

      // Add environment if specified by user
      String env = RtcUtils.getAttributeValueAsString( ptf,
            PtfConstants.COVER_LETTER_ENVIRONMENT_LIST_ATTRIBUTE_ID,
            sportCommonData );
      if (env != null && env.trim().length() > 0)
         copySentAttrs.add(
               PtfConstants.COVER_LETTER_ENVIRONMENT_LIST_ATTRIBUTE_ID );

      // Convert to array for artifact
      String[] copySentAttrsArray = new String[copySentAttrs.size()];
      copySentAttrsArray = copySentAttrs.toArray( copySentAttrsArray );

      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_COPYSENT_ACTION, copySentAttrsArray,
            sportCommonData );
      artifact.setArtifactClass( PtfConstants.PTF_CLASS );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updatePtfFromRetain( ptf, sportCommonData );
   }

   public static void editPtf( IWorkItem ptf, String[] actions,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      List<String> editAttrs = new ArrayList<String>();
      List<String> coverLetterAttrs = new ArrayList<String>();
      // determine which fields were changed
      boolean edUpdate = false;
      boolean clUpdate = false;
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         String actionType = actionParts[0];
         if ((actionParts.length == 2) && actionType.equals( "modify" ))
         {
            String fieldName = actionParts[1];
            for (String edfield : PtfConstants.SBS_EDIT_ACTION_ATTRIBUTE_IDS)
            {
               if (fieldName.equals( edfield )) // if modified
               { // attribute changed
                  edUpdate = true;
                  editAttrs.add( fieldName );
               }
            }
            for (String edfield : PtfConstants.SBS_EDIT_COVERLETTER_ACTION_ATTRIBUTE_IDS)
            {
               if (fieldName.equals( edfield )) // if modified
               { // attribute changed
                  clUpdate = true;
                  coverLetterAttrs.add( fieldName );
               }
            }
         }
      }
      if (edUpdate == false && clUpdate == false)
      {
         String message = "No attribute changed for Update PTF Information. Change at least one attribute and try again.";
         throw new SportUserActionFailException( message );
      }

      if (edUpdate == true)
      {
         // make sure ptfNum and reportedComp are set
         editAttrs.add( PtfConstants.PTF_NUM_ATTRIBUTE_ID );
         editAttrs.add( PtfConstants.REPORTED_COMP_CHAR_ATTRIBUTE_ID );
         // convert to array
         String[] editArray = new String[editAttrs.size()];
         editArray = editAttrs.toArray( editArray );
         Artifact artifact = SbsUtils.createArtifact( ptf,
               PtfConstants.SBS_EDIT_ACTION, editArray, sportCommonData );
         artifact.setArtifactClass( PtfConstants.PTF_CLASS );
         SbsUtils.sendArtifacts( artifact, sportCommonData );
      }
      if (clUpdate == true)
      {
         // make sure ptfNum is sent
         coverLetterAttrs.add( PtfConstants.PTF_NUM_ATTRIBUTE_ID );
         // convert to array
         String[] clArray = new String[coverLetterAttrs.size()];
         clArray = coverLetterAttrs.toArray( clArray );
         Artifact artifact = SbsUtils.createArtifact( ptf,
               PtfConstants.SBS_EDIT_COVERLETTER_ACTION, clArray,
               sportCommonData );
         artifact.setArtifactClass( PtfConstants.PTF_CLASS );
         SbsUtils.sendArtifacts( artifact, sportCommonData );
      }
      updatePtfFromRetain( ptf, sportCommonData );
   }

   public static void createPtf( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      boolean ptfInRetain = RtcUtils.getAttributeValueAsBoolean( ptf,
            PtfConstants.PTF_IN_RETAIN_ATTRIBUTE_ID, sportCommonData );
      boolean createAsDraft = RtcUtils.getAttributeValueAsBoolean( ptf,
            PtfConstants.CREATE_AS_DRAFT_ATTRIBUTE_ID, sportCommonData );
      if (ptfInRetain && !createAsDraft)
      {
         updatePtfFromRetain( ptf, sportCommonData );
         // _T1334A - Unset indicator on successful create
         unsetPtfInRETAINIndicator( ptf, sportCommonData );
         CommonUtils.registerComponentUse( sportCommonData, ptf );
      }
      else if (createAsDraft)
      {
         IWorkItem ptfToUpdate = sportCommonData.getWorkItemForUpdate();
         String ptfNum = "*DRAFT*";
         RtcUtils.setWorkItemAttributeToValue(
               PtfConstants.PTF_NUM_ATTRIBUTE_ID, ptfNum, ptfToUpdate,
               sportCommonData );
      }
      else
         createPtfInRetain( ptf, sportCommonData );
   }

   public static String createPtfInRetain( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_CREATE_ACTION,
            PtfConstants.SBS_CREATE_ACTION_ATTRIBUTE_IDS, sportCommonData );
      artifact.setArtifactClass( AparConstants.APAR_CLASS );
      Artifact sbsResponse = SbsUtils.sendArtifacts( artifact,
            sportCommonData );
      String ptfNum = sbsResponse
            .findFieldValue( AparConstants.APAR_NUM_ATTRIBUTE_ID );
      IAttribute ptfNumAttribute = RtcUtils.findAttribute( ptf,
            PtfConstants.PTF_NUM_ATTRIBUTE_ID, sportCommonData );
      RtcUtils.setAttributeValue( ptf, ptfNumAttribute, ptfNum,
            sportCommonData );
      updatePtfFromRetain( ptf, sportCommonData );
      CommonUtils.registerComponentUse( sportCommonData, ptf );
      return ptfNum;
   }

   public static void receiptPtf( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_RECEIPT_ACTION,
            PtfConstants.SBS_RECEIPT_ACTION_ATTRIBUTE_IDS, sportCommonData );
      artifact.setArtifactClass( PtfConstants.PTF_CLASS );
      SbsUtils.sendArtifacts( artifact, sportCommonData );

      updatePtfFromRetain( ptf, sportCommonData );
   }

   public static void reopenPtf( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_REOPEN_ACTION,
            PtfConstants.SBS_REOPEN_ACTION_ATTRIBUTE_IDS, sportCommonData );
      artifact.setArtifactClass( PtfConstants.PTF_CLASS );
      SbsUtils.sendArtifacts( artifact, sportCommonData );

      updatePtfFromRetain( ptf, sportCommonData );
   }

   public static void toTestPtf( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      List<String> toTestAttrs = new ArrayList<String>();

      // Add the attributes to the list that can be sent every time
      for (int i = 0; i < PtfConstants.SBS_TOTEST_ACTION_ATTRIBUTE_IDS.length; i++)
      {
         toTestAttrs.add( PtfConstants.SBS_TOTEST_ACTION_ATTRIBUTE_IDS[i] );
      }

      // Check if we should add the applicable Rels and APARs fixed
      String applicableRels = RtcUtils.getAttributeValueAsString( ptf,
            PtfConstants.COVER_LETTER_APPLICABLE_RELEASE_LIST_ATTRIBUTE_ID,
            sportCommonData );
      if (applicableRels != null && applicableRels.trim().length() > 0)
         toTestAttrs.add(
               PtfConstants.COVER_LETTER_APPLICABLE_RELEASE_LIST_ATTRIBUTE_ID );
      String aparsFixed = RtcUtils.getAttributeValueAsString( ptf,
            PtfConstants.COVER_LETTER_APARS_LIST_ATTRIBUTE_ID,
            sportCommonData );
      if (aparsFixed != null && aparsFixed.trim().length() > 0)
         toTestAttrs.add( PtfConstants.COVER_LETTER_APARS_LIST_ATTRIBUTE_ID );

      // Convert the attribute ID list to an array
      String[] toTestAttrsArray = new String[toTestAttrs.size()];
      toTestAttrsArray = toTestAttrs.toArray( toTestAttrsArray );

      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_TOTEST_ACTION, toTestAttrsArray,
            sportCommonData );
      artifact.setArtifactClass( PtfConstants.PTF_CLASS );
      SbsUtils.sendArtifacts( artifact, sportCommonData );

      updatePtfFromRetain( ptf, sportCommonData );
   }

   public static void reroutePtf( IWorkItem ptf, String[] actions,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      boolean rrUpdate = false;
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         String actionType = actionParts[0];
         if ((actionParts.length == 2) && actionType.equals( "modify" ))
         {
            String fieldName = actionParts[1];
            for (String rrfield : PtfConstants.SBS_REROUTE_ACTION_ATTRIBUTE_IDS)
            {
               if (fieldName.equals( rrfield )) // if modified
               { // attribute changed
                  rrUpdate = true;
                  break; // bail out of for loop
               }
            }
         }
      }
      if (rrUpdate == false)
      {
         String message = "Specify New Component and/or New Change Team on the Reroute tab for PTF Reroute action";
         throw new SportUserActionFailException( message );
      }

      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_REROUTE_ACTION,
            PtfConstants.SBS_REROUTE_ACTION_ATTRIBUTE_IDS, sportCommonData );
      artifact.setArtifactClass( PtfConstants.PTF_CLASS );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updatePtfFromRetain( ptf, sportCommonData );
   }

   public static void assignPtf( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_ASSIGN_ACTION,
            PtfConstants.SBS_ASSIGN_ACTION_ATTRIBUTE_IDS, sportCommonData );
      artifact.setArtifactClass( PtfConstants.PTF_CLASS );
      SbsUtils.sendArtifacts( artifact, sportCommonData );

      updatePtfFromRetain( ptf, sportCommonData );
   }

   public static void transferPsppPendingOrder( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_TRANSFERORDER_ACTION,
            PtfConstants.SBS_TRANSFERORDER_ACTION_ATTRIBUTE_IDS,
            sportCommonData );
      artifact.setArtifactClass( PtfConstants.PTF_CLASS );
      SbsUtils.sendArtifacts( artifact, sportCommonData );

      updatePtfFromRetain( ptf, sportCommonData );
   }

   public static void endTestPTF( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      // don't want to send an empty appliedDate if not provided
      ArrayList<String> endTestAttributeArray = new ArrayList<String>();
      for (int i = 0; i < PtfConstants.SBS_ENDTEST_ACTION_ATTRIBUTE_IDS.length; i++)
         endTestAttributeArray
               .add( PtfConstants.SBS_ENDTEST_ACTION_ATTRIBUTE_IDS[i] );

      IAttribute appliedDateAttribute = RtcUtils.findAttribute( ptf,
            PtfConstants.FEEDBACK_APPLIED_DATE_ATTRIBUTE_ID,
            sportCommonData );
      String appliedDateString = RtcUtils.getAttributeValueAsString( ptf,
            appliedDateAttribute, sportCommonData );
      if (appliedDateString == null || appliedDateString.length() == 0)
         endTestAttributeArray
               .remove( PtfConstants.FEEDBACK_APPLIED_DATE_ATTRIBUTE_ID );

      String[] endTestAttributesStrings = new String[endTestAttributeArray
            .size()];

      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_ENDTEST_ACTION,
            endTestAttributeArray.toArray( endTestAttributesStrings ),
            sportCommonData );
      artifact.setArtifactClass( PtfConstants.PTF_CLASS );
      SbsUtils.sendArtifacts( artifact, sportCommonData );

      updatePtfFromRetain( ptf, sportCommonData );
   }

   public static void transAT( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      List<String> atControlAttrs = new ArrayList<String>();

      // Add the attributes to the list that can be sent every time
      for (int i = 0; i < PtfConstants.SBS_ATCONTROL_ACTION_ATTRIBUTE_IDS.length; i++)
      {
         atControlAttrs
               .add( PtfConstants.SBS_ATCONTROL_ACTION_ATTRIBUTE_IDS[i] );
      }

      // Check if we should add the applicable Rels and APARs fixed
      String applicableRels = RtcUtils.getAttributeValueAsString( ptf,
            PtfConstants.COVER_LETTER_APPLICABLE_RELEASE_LIST_ATTRIBUTE_ID,
            sportCommonData );
      if (applicableRels != null && applicableRels.trim().length() > 0)
         atControlAttrs.add(
               PtfConstants.COVER_LETTER_APPLICABLE_RELEASE_LIST_ATTRIBUTE_ID );
      String aparsFixed = RtcUtils.getAttributeValueAsString( ptf,
            PtfConstants.COVER_LETTER_APARS_LIST_ATTRIBUTE_ID,
            sportCommonData );
      if (aparsFixed != null && aparsFixed.trim().length() > 0)
         atControlAttrs
               .add( PtfConstants.COVER_LETTER_APARS_LIST_ATTRIBUTE_ID );

      // Convert the attribute ID list to an array
      String[] atControlAttrsArray = new String[atControlAttrs.size()];
      atControlAttrsArray = atControlAttrs.toArray( atControlAttrsArray );

      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_ATCONTROL_ACTION, atControlAttrsArray,
            sportCommonData );
      artifact.setArtifactClass( PtfConstants.PTF_CLASS );
      SbsUtils.sendArtifacts( artifact, sportCommonData );

      updatePtfFromRetain( ptf, sportCommonData );
   }

   public static void resetModifyAction( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IWorkItem ptfToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute modifyActionAttribute = RtcUtils.findAttribute( ptfToUpdate,
            PtfConstants.MODIFY_ACTION_ATTRIBUTE_ID, sportCommonData );
      RtcUtils.setEnumerationAttributeValue( ptfToUpdate,
            modifyActionAttribute,
            PtfConstants.RTC_ENTER_MODIFY_ACTION_ACTION, sportCommonData );
   }

   // _B166164A UnSet Ptf Create As Draft indicator.
   // Called when Create in RETAIN action is successful.
   public static void unsetPtfDraftIndicator( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // Obtain indicator in source work item
      boolean createAsDraft = RtcUtils.getAttributeValueAsBoolean( ptf,
            PtfConstants.CREATE_AS_DRAFT_ATTRIBUTE_ID, sportCommonData );
      if (createAsDraft)
      {
         // Need to use working copy of work item here
         IWorkItem ptfToUpdate = sportCommonData.getWorkItemForUpdate();
         // Unset Create As Draft
         RtcUtils.setWorkItemAttributeToValue(
               PtfConstants.CREATE_AS_DRAFT_ATTRIBUTE_ID, "FALSE",
               ptfToUpdate, sportCommonData );
         // Save done later at end of participant processing
      }
   }

   public static void closePtf( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String closeCode = RtcUtils.getAttributeValueAsString( ptf,
            PtfConstants.DRAFT_PTF_CLOSE_CODE_ATTRIBUTE_ID, sportCommonData );

      // Call the appropriate method based on close code
      if (closeCode.equals( "COR" ) || closeCode.equals( "PER" ))
         closeCorPerPtf( ptf, sportCommonData );
      else if (closeCode.equals( "DUP" ))
         closeDupPtf( ptf, sportCommonData );
      else if (closeCode.equals( "REJ" ))
         closeRejPtf( ptf, sportCommonData );
      else if (closeCode.equals( "CAN" ))
         closeCanPtf( ptf, sportCommonData, false );
      else if (closeCode.equals( "CAN Restricted" ))
         closeCanPtf( ptf, sportCommonData, true );
      else if (closeCode.equals( "ACL" ))
         closeAclPtf( ptf, sportCommonData );
      else
      {
         String message = "Unknown close code \"" + closeCode
               + "\" specified.";
         throw new SportUserActionFailException( message );
      }

      // Reset the draft close code
      resetPtfDraftCloseCode( ptf, sportCommonData );
   }

   public static void closeCorPerPtf( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      List<String> corPerAttrs = new ArrayList<String>();

      // Add the attributes to the list that can be sent every time
      for (int i = 0; i < PtfConstants.SBS_CLOSE_CORPER_ACTION_ATTRIBUTE_IDS.length; i++)
      {
         corPerAttrs
               .add( PtfConstants.SBS_CLOSE_CORPER_ACTION_ATTRIBUTE_IDS[i] );
      }

      // Add close date if specified
      String closeDate = RtcUtils.getAttributeValueAsString( ptf,
            PtfConstants.DRAFT_CLOSE_DATE_ATTRIBUTE_ID, sportCommonData );
      if (closeDate != null && !closeDate.equals( "" )
            && !(closeDate.toUpperCase().equals( "NONE" )))
      {
         corPerAttrs.add( PtfConstants.DRAFT_CLOSE_DATE_ATTRIBUTE_ID );
      }

      // Convert to array for artifact
      String[] corPerAttrsArray = new String[corPerAttrs.size()];
      corPerAttrsArray = corPerAttrs.toArray( corPerAttrsArray );
      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_CLOSE_CORPER_ACTION, corPerAttrsArray,
            sportCommonData );
      // _F80509A Set defaults in primary artifact record for RETAIN action
      SbsUtils.setUnspecifiedRequiredActionFieldsToDefault( artifact, ptf,
            PtfConstants.SBS_CLOSE_CORPER_ACTION, corPerAttrsArray,
            sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updatePtfFromRetain( ptf, sportCommonData );
   }

   public static void closeDupPtf( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_CLOSE_DUP_ACTION,
            PtfConstants.SBS_CLOSE_DUP_ACTION_ATTRIBUTE_IDS,
            sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updatePtfFromRetain( ptf, sportCommonData );
   }

   public static void closeRejPtf( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_CLOSE_REJ_ACTION,
            PtfConstants.SBS_CLOSE_REJ_CAN_ACL_ACTION_ATTRIBUTE_IDS,
            sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updatePtfFromRetain( ptf, sportCommonData );
   }

   public static void closeCanPtf( IWorkItem ptf,
         SportCommonData sportCommonData, boolean restricted )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String[] attributeIDs;
      if (restricted)
      {
         IWorkItem ptfToUpdate = sportCommonData.getWorkItemForUpdate();
         IAttribute restrictedAttribute = RtcUtils.findAttribute( ptfToUpdate,
               PtfConstants.RESTRICTED_ATTRIBUTE_ID, sportCommonData );
         RtcUtils.setBooleanAttributeValue( ptfToUpdate, restrictedAttribute,
               "true", sportCommonData );
         attributeIDs = PtfConstants.SBS_CLOSE_REJ_CAN_CANRES_ACL_ACTION_ATTRIBUTE_IDS;
      }
      else
         attributeIDs = PtfConstants.SBS_CLOSE_REJ_CAN_ACL_ACTION_ATTRIBUTE_IDS;
      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_CLOSE_CAN_ACTION, attributeIDs,
            sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updatePtfFromRetain( ptf, sportCommonData );
   }

   public static void closeAclPtf( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_CLOSE_ACL_ACTION,
            PtfConstants.SBS_CLOSE_REJ_CAN_ACL_ACTION_ATTRIBUTE_IDS,
            sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updatePtfFromRetain( ptf, sportCommonData );
   }

   /*
    * Reset the Draft Close Code of a PTF after the Close or else the Reopen
    * might fail due to an invalid draft close code for the REOP state. We
    * need to be able to check workflow actions in our conditions, but until
    * RTC allows that, this is a workaround.
    */
   public static void resetPtfDraftCloseCode( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IWorkItem ptfToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute draftPtfCloseCodeAttribute = RtcUtils.findAttribute(
            ptfToUpdate, PtfConstants.DRAFT_PTF_CLOSE_CODE_ATTRIBUTE_ID,
            sportCommonData );
      RtcUtils.setEnumerationAttributeValue( ptfToUpdate,
            draftPtfCloseCodeAttribute, PtfConstants.UNASSIGNED,
            sportCommonData );
   }

   public static void closeNAVPtf( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( ptf,
            PtfConstants.SBS_MARK_PTF_AS_UNAVAILABLE_ACTION,
            PtfConstants.SBS_MARK_PTF_AS_UNAVAILABLE_ACTION_ATTRIBUTE_IDS,
            sportCommonData );
      artifact.setArtifactClass( PtfConstants.PTF_CLASS );
      SbsUtils.sendArtifacts( artifact, sportCommonData );

      updatePtfFromRetain( ptf, sportCommonData );
   }

   public static void markAsSanitized( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {

      IWorkItem ptfToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute sanitizedAttribute = RtcUtils.findAttribute( ptfToUpdate,
            PtfConstants.SANITIZED_ATTRIBUTE_ID, sportCommonData );
      RtcUtils.setBooleanAttributeValue( ptfToUpdate, sanitizedAttribute,
            "true", sportCommonData );

   }

   public static void markAsNotSanitized( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {

      IWorkItem ptfToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute sanitizedAttribute = RtcUtils.findAttribute( ptfToUpdate,
            PtfConstants.SANITIZED_ATTRIBUTE_ID, sportCommonData );
      RtcUtils.setBooleanAttributeValue( ptfToUpdate, sanitizedAttribute,
            "false", sportCommonData );

   }

   public static void clearPIDataInPtf( IWorkItem ptf,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {

      // Set the PI data attributes to ""
      // See
      // https://apps.na.collabserv.com/wikis/home?lang=en-us#!/wiki/W69c7d6df7474_4a9a_bb93_2bb3c7736996/page/SDI%20Interface%20Requirements
      // for list of PI data fields
      IWorkItem ptfToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute contactPhoneAttribute = RtcUtils.findAttribute( ptfToUpdate,
            PtfConstants.CONTACT_PHONE_ATTRIBUTE_ID, sportCommonData );
      if (contactPhoneAttribute != null)
         RtcUtils.setAttributeValue( ptfToUpdate, contactPhoneAttribute, "",
               sportCommonData );
      IAttribute boAddress1Attribute = RtcUtils.findAttribute( ptfToUpdate,
            PtfConstants.BO_ADDRESS_1_ATTRIBUTE_ID, sportCommonData );
      if (boAddress1Attribute != null)
         RtcUtils.setAttributeValue( ptfToUpdate, boAddress1Attribute, "",
               sportCommonData );
      IAttribute boAddress2Attribute = RtcUtils.findAttribute( ptfToUpdate,
            PtfConstants.BO_ADDRESS_2_ATTRIBUTE_ID, sportCommonData );
      if (boAddress2Attribute != null)
         RtcUtils.setAttributeValue( ptfToUpdate, boAddress2Attribute, "",
               sportCommonData );
      IAttribute boCityStateAttribute = RtcUtils.findAttribute( ptfToUpdate,
            PtfConstants.BO_CITY_STATE_ATTRIBUTE_ID, sportCommonData );
      if (boCityStateAttribute != null)
         RtcUtils.setAttributeValue( ptfToUpdate, boCityStateAttribute, "",
               sportCommonData );
      IAttribute boZipAttribute = RtcUtils.findAttribute( ptfToUpdate,
            PtfConstants.BO_ZIP_ATTRIBUTE_ID, sportCommonData );
      if (boZipAttribute != null)
         RtcUtils.setAttributeValue( ptfToUpdate, boZipAttribute, "",
               sportCommonData );
      IAttribute submitterAttribute = RtcUtils.findAttribute( ptfToUpdate,
            PtfConstants.SUBMITTER_ATTRIBUTE_ID, sportCommonData );
      if (submitterAttribute != null)
         RtcUtils.setAttributeValue( ptfToUpdate, submitterAttribute, "",
               sportCommonData );
      IAttribute customerCountryAttribute = RtcUtils.findAttribute(
            ptfToUpdate, PtfConstants.CUSTOMER_COUNTRY_ATTRIBUTE_ID,
            sportCommonData );
      if (customerCountryAttribute != null)
         RtcUtils.setAttributeValue( ptfToUpdate, customerCountryAttribute,
               "", sportCommonData );

      // Data has been cleared, now mark it as sanitized
      markAsSanitized( ptf, sportCommonData );

   }

}
