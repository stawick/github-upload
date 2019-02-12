package com.ibm.sport.rtc.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.ArtifactTechnology.common.Artifact;
import com.ibm.ArtifactTechnology.common.ArtifactFieldValue;
import com.ibm.ArtifactTechnology.common.ArtifactRecordField;
import com.ibm.ArtifactTechnology.common.ArtifactRecordPart;
import com.ibm.ArtifactTechnology.common.Artifacts;
import com.ibm.ArtifactTechnology.common.XMLEncodingType;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.expression.IQueryableAttribute;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.query.IQueryResult;
import com.ibm.team.workitem.common.query.IResolvedResult;

public class CallQueueUtils
{
   private static Map<String, Long> activeCallQueueSaves = new HashMap<String, Long>();
   private final static long CALL_QUEUE_SAVE_TIMEOUT = 15 * 60 * 1000;

   // Create thread for cleaning up active Call Queue saves that fail in a
   // non-SPoRT advisor or participant.
   static
   {
      Thread thread = new Thread( new Runnable()
      {
         public void run()
         {
            Log log = LogFactory.getLog( CallQueueUtils.class );
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
               synchronized (activeCallQueueSaves)
               {
                  boolean notifyAllNeeded = false;
                  Iterator<Map.Entry<String, Long>> iterator = activeCallQueueSaves
                        .entrySet().iterator();
                  while (iterator.hasNext())
                  {
                     Map.Entry<String, Long> activeCallQueueSave = iterator
                           .next();
                     long createTime = activeCallQueueSave.getValue()
                           .longValue();
                     if ((System.currentTimeMillis() - createTime) >= CALL_QUEUE_SAVE_TIMEOUT)
                     {
                        iterator.remove();
                        notifyAllNeeded = true;
                        String callqueueName = activeCallQueueSave.getKey();
                        String message = "Removing "
                              + callqueueName
                              + " from active Call Queue saves due to timeout";
                        log.warn( message );
                     }
                  }
                  if (notifyAllNeeded)
                  {
                     activeCallQueueSaves.notifyAll();
                  }
               }
            }
         }
      } );
      thread.start();
   }

   public static void callQueueCallComplete( IWorkItem callQueue,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String[] attrsToReset = {
            CallQueueConstants.SERVICE_GIVEN_ATTRIBUTE_ID,
            CallQueueConstants.FOLLOWUP_INFO_ATTRIBUTE_ID,
            CallQueueConstants.PRIMARY_FUP_QUEUE_ATTRIBUTE_ID,
            CallQueueConstants.PRIMARY_FUP_CENTER_ATTRIBUTE_ID };
      Artifact artifact = createCallCompleteArtifact( callQueue,
            sportCommonData, actions );
      boolean dispatchAlreadyTried = false;
      try
      {
         CallInfo selectedCall = getSelectedCall( callQueue, sportCommonData );
         if (selectedCall.getDispatchedTo().equals( "" ))
         {
            callQueueCallDispatch( callQueue, sportCommonData );
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
            callQueueCallDispatch( callQueue, sportCommonData );
            SbsUtils.sendArtifacts( artifact, sportCommonData );
         }
         // If the call is already dispatched, try the complete in case the
         // call is already dispatched to the user
         else if (message.contains( "CALL IS ALREADY DISPATCHED" ))
         {
            SbsUtils.sendArtifacts( artifact, sportCommonData );
         }
         else
         {
            // Reset Service Given and followup attributes
            blankOutAttrs( attrsToReset, callQueue, sportCommonData );
            throw x;
         }
      }
      // Reset Service Given and followup attributes
      blankOutAttrs( attrsToReset, callQueue, sportCommonData );

      updateCallQueueFromRetain( callQueue, sportCommonData );
   }

   public static void callQueueCallConvert( IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = createCallConvertArtifact( callQueue,
            sportCommonData );
      boolean dispatchAlreadyTried = false;
      try
      {
         CallInfo selectedCall = getSelectedCall( callQueue, sportCommonData );
         if (selectedCall.getDispatchedTo().equals( "" ))
         {
            callQueueCallDispatch( callQueue, sportCommonData );
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
            callQueueCallDispatch( callQueue, sportCommonData );
            SbsUtils.sendArtifacts( artifact, sportCommonData );
         }
         // If the call is already dispatched, try the convert in case the
         // call is already dispatched to the user
         else if (message.contains( "CALL IS ALREADY DISPATCHED" ))
         {
            SbsUtils.sendArtifacts( artifact, sportCommonData );
         }
         else
            throw x;
      }
      updateCallQueueFromRetain( callQueue, sportCommonData );
   }

   public static void callQueueCallCreateForExistingPmr( IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = createCallCreateArtifactForExistingPmr( callQueue,
            sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateCallQueueFromRetain( callQueue, sportCommonData );
   }

   public static void callQueueCallCreateWithNewPmr( IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = createCallCreateArtifactWithNewPmr( callQueue,
            sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateCallQueueFromRetain( callQueue, sportCommonData );
   }

   public static void callQueueCallDispatch( IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = createCallDispatchArtifact( callQueue,
            sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateCallQueueFromRetain( callQueue, sportCommonData );
   }

   public static void callQueueCallRequeue( IWorkItem callQueue,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String[] attrsToReset = {
            CallQueueConstants.SERVICE_GIVEN_ATTRIBUTE_ID,
            CallQueueConstants.FOLLOWUP_INFO_ATTRIBUTE_ID,
            CallQueueConstants.PRIMARY_FUP_QUEUE_ATTRIBUTE_ID,
            CallQueueConstants.PRIMARY_FUP_CENTER_ATTRIBUTE_ID };
      Artifact artifact = createCallRequeueArtifact( callQueue,
            sportCommonData, actions );
      boolean dispatchAlreadyTried = false;
      try
      {
         CallInfo selectedCall = getSelectedCall( callQueue, sportCommonData );
         if (selectedCall.getDispatchedTo().equals( "" ))
         {
            callQueueCallDispatch( callQueue, sportCommonData );
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
            callQueueCallDispatch( callQueue, sportCommonData );
            SbsUtils.sendArtifacts( artifact, sportCommonData );
         }
         // If the call is already dispatched, try the requeue in case the
         // call is already dispatched to the user
         else if (message.contains( "CALL IS ALREADY DISPATCHED" ))
         {
            SbsUtils.sendArtifacts( artifact, sportCommonData );
         }
         else
         {
            // Reset Service Given and followup attributes
            blankOutAttrs( attrsToReset, callQueue, sportCommonData );
            throw x;
         }
      }
      // Reset Service Given and followup attributes
      blankOutAttrs( attrsToReset, callQueue, sportCommonData );
      updateCallQueueFromRetain( callQueue, sportCommonData );
   }

   public static Artifact createCallCompleteArtifact( IWorkItem callQueue,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {
      boolean serviceGivenUpdated = false;
      boolean followupInfoUpdated = false;
      boolean fupQOrCenterUpdated = false;

      String center = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.CENTER_ATTRIBUTE_ID, sportCommonData );
      String queue = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.QUEUE_ATTRIBUTE_ID, sportCommonData );
      String serviceGiven = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.SERVICE_GIVEN_ATTRIBUTE_ID, sportCommonData );
      String followupInfo = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.FOLLOWUP_INFO_ATTRIBUTE_ID, sportCommonData );
      String primaryFupQueue = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.PRIMARY_FUP_QUEUE_ATTRIBUTE_ID,
            sportCommonData );
      String primaryFupCenter = RtcUtils.getAttributeValueAsString(
            callQueue, CallQueueConstants.PRIMARY_FUP_CENTER_ATTRIBUTE_ID,
            sportCommonData );
      CallInfo selectedCall = getSelectedCall( callQueue, sportCommonData );

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
                     .equals( CallQueueConstants.SERVICE_GIVEN_ATTRIBUTE_ID ))
                  serviceGivenUpdated = true;
            }
            else
            {
               if (fieldName
                     .equals( CallQueueConstants.SERVICE_GIVEN_ATTRIBUTE_ID ))
                  serviceGivenUpdated = true;
               if (fieldName
                     .equals( CallQueueConstants.FOLLOWUP_INFO_ATTRIBUTE_ID ))
                  followupInfoUpdated = true;
               if (fieldName
                     .equals( CallQueueConstants.PRIMARY_FUP_QUEUE_ATTRIBUTE_ID )
                     || fieldName
                           .equals( CallQueueConstants.PRIMARY_FUP_CENTER_ATTRIBUTE_ID ))
                  fupQOrCenterUpdated = true;
            }

         }
      }

      Artifact artifact = new Artifact();
      artifact.addAction( PmrConstants.SBS_CALL_COMPLETE_ACTION );
      artifact.setArtifactClass( PmrConstants.CALL_CLASS );
      artifact.setArtifactID( queue + "," + center );
      artifact.setProvider( SbsUtils.createProvider( sportCommonData ) );
      ArtifactRecordPart record = new ArtifactRecordPart();
      record.setName( PmrConstants.WORK_ITEM_TYPE_ID );
      record.addField( PmrConstants.QUEUE_ATTRIBUTE_ID, queue,
            XMLEncodingType.XML );
      record.addField( PmrConstants.CENTER_ATTRIBUTE_ID, center,
            XMLEncodingType.XML );
      ArtifactRecordField pPGField = record
            .addField( PmrConstants.PPG_ATTRIBUTE_ID );
      pPGField.addValue( selectedCall.getPPG().substring( 0, 1 ) );
      pPGField.addValue( selectedCall.getPPG().substring( 1, 3 ) );
      if ((serviceGiven != null) && (!serviceGiven.equals( "" ))
            && (serviceGivenUpdated))
      {
         record.addField( PmrConstants.SERVICE_GIVEN_ATTRIBUTE_ID,
               serviceGiven, XMLEncodingType.XML );
      }
      if ((followupInfo != null) && (!followupInfo.equals( "" ))
            && (followupInfoUpdated))
      {
         record.addField( PmrConstants.FOLLOWUP_INFO_ATTRIBUTE_ID,
               followupInfo, XMLEncodingType.XML );
      }
      if ((primaryFupQueue != null) && (fupQOrCenterUpdated))
      {
         record.addField( PmrConstants.PRIMARY_FUP_QUEUE_ATTRIBUTE_ID,
               primaryFupQueue, XMLEncodingType.XML );
      }
      if ((primaryFupCenter != null) && (fupQOrCenterUpdated))
      {
         record.addField( PmrConstants.PRIMARY_FUP_CENTER_ATTRIBUTE_ID,
               primaryFupCenter, XMLEncodingType.XML );
      }

      artifact.addRecord( record );
      return artifact;
   }

   public static Artifact createCallConvertArtifact( IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String center = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.CENTER_ATTRIBUTE_ID, sportCommonData );
      String queue = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.QUEUE_ATTRIBUTE_ID, sportCommonData );
      CallInfo selectedCall = getSelectedCall( callQueue, sportCommonData );
      Artifact artifact = new Artifact();
      artifact.addAction( PmrConstants.SBS_CALL_CONVERT_ACTION );
      artifact.setArtifactClass( PmrConstants.CALL_CLASS );
      artifact.setArtifactID( queue + "," + center );
      artifact.setProvider( SbsUtils.createProvider( sportCommonData ) );
      ArtifactRecordPart record = new ArtifactRecordPart();
      record.setName( PmrConstants.WORK_ITEM_TYPE_ID );
      record.addField( PmrConstants.QUEUE_ATTRIBUTE_ID, queue,
            XMLEncodingType.XML );
      record.addField( PmrConstants.CENTER_ATTRIBUTE_ID, center,
            XMLEncodingType.XML );
      ArtifactRecordField pPGField = record
            .addField( PmrConstants.PPG_ATTRIBUTE_ID );
      pPGField.addValue( selectedCall.getPPG().substring( 0, 1 ) );
      pPGField.addValue( selectedCall.getPPG().substring( 1, 3 ) );
      artifact.addRecord( record );
      return artifact;
   }

   public static Artifact createCallCreateArtifactForExistingPmr(
         IWorkItem callQueue, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String center = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.CENTER_ATTRIBUTE_ID, sportCommonData );
      String customerNumber = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.CUSTOMER_NUMBER_ATTRIBUTE_ID, sportCommonData );
      String pmrName = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.PMR_NAME_ATTRIBUTE_ID, sportCommonData );
      String queue = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.QUEUE_ATTRIBUTE_ID, sportCommonData );
      Artifact artifact = new Artifact();
      artifact.addAction( PmrConstants.SBS_CALL_CREATE_ACTION );
      artifact.setArtifactClass( PmrConstants.CALL_CLASS );
      artifact.setArtifactID( queue + "," + center );
      artifact.setProvider( SbsUtils.createProvider( sportCommonData ) );
      ArtifactRecordPart record = new ArtifactRecordPart();
      record.setName( PmrConstants.WORK_ITEM_TYPE_ID );
      record.addField( PmrConstants.QUEUE_ATTRIBUTE_ID, queue,
            XMLEncodingType.XML );
      record.addField( PmrConstants.CENTER_ATTRIBUTE_ID, center,
            XMLEncodingType.XML );
      record.addField( PmrConstants.CUSTOMER_NUMBER_ATTRIBUTE_ID,
            customerNumber, XMLEncodingType.XML );
      String[] pmrNameArray = pmrName.split( ",|\\s+" );
      if (pmrNameArray.length == 3)
      {
         String pmrNo = pmrNameArray[0];
         String branch = pmrNameArray[1];
         String country = pmrNameArray[2];
         record.addField( PmrConstants.PMR_NO_ATTRIBUTE_ID, pmrNo,
               XMLEncodingType.XML );
         record.addField( PmrConstants.BRANCH_ATTRIBUTE_ID, branch,
               XMLEncodingType.XML );
         record.addField( PmrConstants.COUNTRY_ATTRIBUTE_ID, country,
               XMLEncodingType.XML );
      }
      artifact.addRecord( record );
      return artifact;
   }

   public static Artifact createCallCreateArtifactWithNewPmr(
         IWorkItem callQueue, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String branch = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.BRANCH_ATTRIBUTE_ID, sportCommonData );
      String center = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.CENTER_ATTRIBUTE_ID, sportCommonData );
      String country = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.COUNTRY_ATTRIBUTE_ID, sportCommonData );
      String customerNumber = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.CUSTOMER_NUMBER_ATTRIBUTE_ID, sportCommonData );
      String queue = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.QUEUE_ATTRIBUTE_ID, sportCommonData );
      Artifact artifact = new Artifact();
      artifact.addAction( CallQueueConstants.SBS_CALL_CREATE_ACTION );
      artifact.setArtifactClass( PmrConstants.CALL_CLASS );
      artifact.setArtifactID( queue + "," + center );
      artifact.setProvider( SbsUtils.createProvider( sportCommonData ) );
      ArtifactRecordPart record = new ArtifactRecordPart();
      record.setName( PmrConstants.WORK_ITEM_TYPE_ID );
      record.addField( PmrConstants.QUEUE_ATTRIBUTE_ID, queue,
            XMLEncodingType.XML );
      record.addField( PmrConstants.CENTER_ATTRIBUTE_ID, center,
            XMLEncodingType.XML );
      record.addField( PmrConstants.CUSTOMER_NUMBER_ATTRIBUTE_ID,
            customerNumber, XMLEncodingType.XML );
      record.addField( PmrConstants.BRANCH_ATTRIBUTE_ID, branch,
            XMLEncodingType.XML );
      record.addField( PmrConstants.COUNTRY_ATTRIBUTE_ID, country,
            XMLEncodingType.XML );
      artifact.addRecord( record );
      return artifact;
   }

   public static Artifact createCallDispatchArtifact( IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String center = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.CENTER_ATTRIBUTE_ID, sportCommonData );
      String queue = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.QUEUE_ATTRIBUTE_ID, sportCommonData );
      CallInfo selectedCall = getSelectedCall( callQueue, sportCommonData );
      Artifact artifact = new Artifact();
      artifact.addAction( PmrConstants.SBS_CALL_DISPATCH_ACTION );
      artifact.setArtifactClass( PmrConstants.CALL_CLASS );
      artifact.setArtifactID( queue + "," + center );
      artifact.setProvider( SbsUtils.createProvider( sportCommonData ) );
      ArtifactRecordPart record = new ArtifactRecordPart();
      record.setName( PmrConstants.WORK_ITEM_TYPE_ID );
      record.addField( PmrConstants.QUEUE_ATTRIBUTE_ID, queue,
            XMLEncodingType.XML );
      record.addField( PmrConstants.CENTER_ATTRIBUTE_ID, center,
            XMLEncodingType.XML );
      ArtifactRecordField pPGField = record
            .addField( PmrConstants.PPG_ATTRIBUTE_ID );
      pPGField.addValue( selectedCall.getPPG().substring( 0, 1 ) );
      pPGField.addValue( selectedCall.getPPG().substring( 1, 3 ) );
      artifact.addRecord( record );
      return artifact;
   }

   public static void createCallQueue( IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String callqueueName = getNormalizedCallqueueName( callQueue,
            sportCommonData );
      String[] queueCenterArray = callqueueName.split( "," );
      if (queueCenterArray.length == 2)
      {
         IAttribute queueAttribute = RtcUtils.findAttribute( callQueue,
               CallQueueConstants.QUEUE_ATTRIBUTE_ID, sportCommonData );
         RtcUtils.setAttributeValue( callQueue, queueAttribute,
               queueCenterArray[0], sportCommonData );
         IAttribute centerAttribute = RtcUtils.findAttribute( callQueue,
               CallQueueConstants.CENTER_ATTRIBUTE_ID, sportCommonData );
         RtcUtils.setAttributeValue( callQueue, centerAttribute,
               queueCenterArray[1], sportCommonData );
      }
      updateCallQueueFromRetain( callQueue, sportCommonData );
   }

   public static Artifact createCallRequeueArtifact( IWorkItem callQueue,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {
      boolean serviceGivenUpdated = false;
      boolean followupInfoUpdated = false;
      boolean fupQOrCenterUpdated = false;

      String center = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.CENTER_ATTRIBUTE_ID, sportCommonData );
      String queue = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.QUEUE_ATTRIBUTE_ID, sportCommonData );
      String targetQueueCenter = RtcUtils.getAttributeValueAsString(
            callQueue, CallQueueConstants.TARGET_QUEUE_CENTER_ATTRIBUTE_ID,
            sportCommonData );
      String serviceGiven = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.SERVICE_GIVEN_ATTRIBUTE_ID, sportCommonData );
      String followupInfo = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.FOLLOWUP_INFO_ATTRIBUTE_ID, sportCommonData );
      String primaryFupQueue = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.PRIMARY_FUP_QUEUE_ATTRIBUTE_ID,
            sportCommonData );
      String primaryFupCenter = RtcUtils.getAttributeValueAsString(
            callQueue, CallQueueConstants.PRIMARY_FUP_CENTER_ATTRIBUTE_ID,
            sportCommonData );
      CallInfo selectedCall = getSelectedCall( callQueue, sportCommonData );

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
                     .equals( CallQueueConstants.SERVICE_GIVEN_ATTRIBUTE_ID ))
                  serviceGivenUpdated = true;
            }
            else
            {
               if (fieldName
                     .equals( CallQueueConstants.SERVICE_GIVEN_ATTRIBUTE_ID ))
                  serviceGivenUpdated = true;
               if (fieldName
                     .equals( CallQueueConstants.FOLLOWUP_INFO_ATTRIBUTE_ID ))
                  followupInfoUpdated = true;
               if (fieldName
                     .equals( CallQueueConstants.PRIMARY_FUP_QUEUE_ATTRIBUTE_ID )
                     || fieldName
                           .equals( CallQueueConstants.PRIMARY_FUP_CENTER_ATTRIBUTE_ID ))
                  fupQOrCenterUpdated = true;
            }

         }
      }
      Artifact artifact = new Artifact();
      artifact.addAction( PmrConstants.SBS_CALL_REQUEUE_ACTION );
      artifact.setArtifactClass( PmrConstants.CALL_CLASS );
      artifact.setArtifactID( queue + "," + center );
      artifact.setProvider( SbsUtils.createProvider( sportCommonData ) );
      ArtifactRecordPart record = new ArtifactRecordPart();
      record.setName( PmrConstants.WORK_ITEM_TYPE_ID );
      record.addField( PmrConstants.QUEUE_ATTRIBUTE_ID, queue,
            XMLEncodingType.XML );
      record.addField( PmrConstants.CENTER_ATTRIBUTE_ID, center,
            XMLEncodingType.XML );
      ArtifactRecordField pPGField = record
            .addField( PmrConstants.PPG_ATTRIBUTE_ID );
      pPGField.addValue( selectedCall.getPPG().substring( 0, 1 ) );
      pPGField.addValue( selectedCall.getPPG().substring( 1, 3 ) );
      String[] targetQueueCenterArray = targetQueueCenter.split( ",|\\s+" );
      if (targetQueueCenterArray.length == 2)
      {
         record.addField( PmrConstants.TARGET_QUEUE_ATTRIBUTE_ID,
               targetQueueCenterArray[0], XMLEncodingType.XML );
         record.addField( PmrConstants.TARGET_CENTER_ATTRIBUTE_ID,
               targetQueueCenterArray[1], XMLEncodingType.XML );
      }
      if ((serviceGiven != null) && (!serviceGiven.equals( "" ))
            && (serviceGivenUpdated))
      {
         record.addField( PmrConstants.SERVICE_GIVEN_ATTRIBUTE_ID,
               serviceGiven, XMLEncodingType.XML );
      }
      if ((followupInfo != null) && (!followupInfo.equals( "" ))
            && (followupInfoUpdated))
      {
         record.addField( PmrConstants.FOLLOWUP_INFO_ATTRIBUTE_ID,
               followupInfo, XMLEncodingType.XML );
      }
      if ((primaryFupQueue != null) && (fupQOrCenterUpdated))
      {
         record.addField( PmrConstants.PRIMARY_FUP_QUEUE_ATTRIBUTE_ID,
               primaryFupQueue, XMLEncodingType.XML );
      }
      if ((primaryFupCenter != null) && (fupQOrCenterUpdated))
      {
         record.addField( PmrConstants.PRIMARY_FUP_CENTER_ATTRIBUTE_ID,
               primaryFupCenter, XMLEncodingType.XML );
      }
      artifact.addRecord( record );
      return artifact;
   }

   public static Artifact createCallSearchArtifact( String queue,
         String center, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      Artifact artifact = new Artifact();
      artifact.addAction( PmrConstants.SBS_CALL_SEARCH_ACTION );
      artifact.setArtifactClass( PmrConstants.CALL_CLASS );
      artifact.setArtifactID( queue + "," + center );
      artifact.setProvider( SbsUtils.createProvider( sportCommonData ) );
      ArtifactRecordPart record = new ArtifactRecordPart();
      record.setName( PmrConstants.WORK_ITEM_TYPE_ID );
      record.addField( PmrConstants.QUEUE_ATTRIBUTE_ID, queue,
            XMLEncodingType.XML );
      record.addField( PmrConstants.CENTER_ATTRIBUTE_ID, center,
            XMLEncodingType.XML );
      artifact.addRecord( record );
      return artifact;
   }

   public static IWorkItem findCallQueue( String callqueueName,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IQueryableAttribute callqueueNameAttribute = RtcUtils
            .findQueryableAttribute(
                  CallQueueConstants.CALLQUEUE_NAME_ATTRIBUTE_ID,
                  sportCommonData );
      IQueryableAttribute workItemTypeAttribute = RtcUtils
            .findQueryableAttribute( IWorkItem.TYPE_PROPERTY, sportCommonData );
      Map<IQueryableAttribute, Object> attributeValues = new HashMap<IQueryableAttribute, Object>();
      attributeValues.put( callqueueNameAttribute, callqueueName );
      attributeValues.put( workItemTypeAttribute,
            CallQueueConstants.WORK_ITEM_TYPE_ID );
      IQueryResult<IResolvedResult<IWorkItem>> callQueues = RtcUtils
            .queryWorkItemsByAttributes( attributeValues, sportCommonData );
      int callQueueCount = callQueues.getResultSize( monitor )
            .getTotalAvailable();
      if (callQueueCount > 1)
         throw new SportRuntimeException( "More than one Call Queue named \""
               + callqueueName + "\" found in RTC" );
      return (callQueueCount == 0) ? null : callQueues.next( monitor )
            .getItem();
   }

   // _B68102C
   /**
    * Typically used for RTC Call Queue actions. Call Queue is browsed in RTC
    * and resulting Call artifacts are used to populate the Call Queue work
    * item Call List attribute.
    * 
    * @param callArtifacts
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    * @throws SportUserActionFailException 
    */
   public static String formatCallList( Artifacts callArtifacts,
         SportCommonData sportCommonData )
         throws TeamRepositoryException, SportUserActionFailException
   {
      // _B68102C
      return (formatWIKICallList( callArtifacts, sportCommonData ));
      // return (formatHTMLCallList( callArtifacts, sportCommonData ));
   }

   // _B68102A
   /**
    * Format the Call List given the artifacts from a call browse. Used for an
    * HTML type attribute which may get sanitized internally by RTC and may
    * grow > 32K in size. We cannot detect and handle this.
    * 
    * @param callArtifacts
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    * @throws SportUserActionFailException 
    */
   public static String formatHTMLCallList( Artifacts callArtifacts,
         SportCommonData sportCommonData )
         throws TeamRepositoryException, SportUserActionFailException
   {
      String[] htmlFormat = getHTMLFormat();
      return (formatCallListTable( callArtifacts, htmlFormat[0],
            htmlFormat[1], htmlFormat[2], sportCommonData ) + "</pre>");
   }

   // _B68102A
   /**
    * Format the Call List given the artifacts from a call browse. Used for an
    * Wiki type attribute which will build a table in wiki format. Cell
    * formatting is handled by RTC and the wiki attribute.
    * 
    * @param callArtifacts
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    * @throws SportUserActionFailException 
    */
   public static String formatWIKICallList( Artifacts callArtifacts,
         SportCommonData sportCommonData )
         throws TeamRepositoryException, SportUserActionFailException
   {
      String[] wikiFormat = getWIKIFormat();
      return (formatCallListTable( callArtifacts, wikiFormat[0],
            wikiFormat[1], wikiFormat[2], sportCommonData ));
   }

   // _B68102A
   /**
    * Format the Call List given the artifacts from a call browse.
    * 
    * @param callArtifacts
    * @param tableHeader
    * @param entryFormat
    * @param pmrCellLinkFormat - When formatting, 1st argument will be the PMR
    *        name [ppppp,bbb,ccc] string, second will be the PMR work item ID
    *        number.
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    * @throws SportUserActionFailException 
    */
   public static String formatCallListTable( Artifacts callArtifacts,
         String tableHeader, String entryFormat, String pmrCellLinkFormat,
         SportCommonData sportCommonData )
         throws TeamRepositoryException, SportUserActionFailException
   {
      StringBuilder callList = new StringBuilder();
      // Header format: as passed in
      callList.append( tableHeader );

      // Each table call entry format:
      // Call Queue:
      // | <selection#>
      // | <pPG>
      // | <Primary, Secondary, Backup>
      // | <RETAIN-id dispatched>
      // | <PMR # {with link}>
      // PMR data defined as
      // {"[[PMR" [work-item-ID]|} [PMR name = ppppp,bbb,ccc] {"]]"}
      // where data in {} is only used when a link is available.
      // Old HTML PMR data format =
      // ppppp,bbb,ccc{ (PMR [work-item-ID])}
      if (callArtifacts.hasArtifacts())
      {
         // _T155539A Reduce redundant RTC queries. Once queried,
         // contains (active) PMR work item found or null if not.
         HashMap<String, IWorkItem> encounteredPmrWorkItems = new HashMap<String, IWorkItem>();
         IWorkItem pmr = null;
         // Only need to get work item type once.
         IWorkItemType pmrWIType = sportCommonData
               .getWorkItemCommon().findWorkItemType(
                     sportCommonData.getProjectArea(),
                     PmrConstants.WORK_ITEM_TYPE_ID,
                     sportCommonData.getMonitor() );

         int i = 1;
         for (Artifact callArtifact : callArtifacts.getArtifacts())
         {
            ArtifactRecordPart callRecord = callArtifact.getPrimaryRecord();
            if (callRecord == null)
               continue;
            String callPPG = getCallPPG( callRecord );
            String callType = PmrUtils.getCallType( callRecord,
                  sportCommonData );
            String callDispatchedTo = getCallDispatchedTo( callRecord );
            String callPmr = getCallPmr( callRecord, sportCommonData );
            if (!callPmr.isEmpty())
            {
               // | <PMR # {with link}>
               // PMR data defined as
               // {"[[PMR" [work-item-ID]|} [PMR name = ppppp,bbb,ccc] {"]]"}
               // where data in {} is only used when a link is available.
               // Old HTML PMR data format =
               // ppppp,bbb,ccc{ (PMR [work-item-ID])}
               // _T155539C Need to add PMR browse to get
               // PMR Open Date here to add as a key.
               pmr = null;
               if (encounteredPmrWorkItems.containsKey( callPmr ))
               {
                  // If already queried, get it, null or not.
                  pmr = encounteredPmrWorkItems.get( callPmr );
               }
               else
               {
                  pmr = PmrUtils.findPMRWorkItemInRTC( callPmr, pmrWIType,
                        false, sportCommonData );
                  encounteredPmrWorkItems.put( callPmr, pmr );
               }
               if (pmr != null)
               {
                  callPmr = String.format( pmrCellLinkFormat, callPmr,
                        pmr.getId() );
               }
            }
            callList.append( String.format( entryFormat, i, callPPG,
                  callType, callDispatchedTo, callPmr ) );
            ++i;
         }
      }

      return (callList.toString());
   }

   // _B68102C
   /**
    * Typically involves bridged Call Queue work items. SBS sets the
    * unformatted call list data when the call queue is browsed.
    * 
    * @param callQueue
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    * @throws SportUserActionFailException 
    */
   public static String formatCallList( IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws TeamRepositoryException, SportUserActionFailException
   {
      // _B68102C
      return (formatWIKICallList( callQueue, sportCommonData ));
      // return (formatHTMLCallList( callQueue, sportCommonData ));
   }

   // _B68102A
   /**
    * Generate a formatted Call List from the unformatted call data stored in
    * the work item. Used for an HTML type attribute which may get sanitized
    * internally by RTC and may grow > 32K in size. We cannot detect and
    * handle this.
    * 
    * @param callQueue
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    * @throws SportUserActionFailException 
    */
   public static String formatHTMLCallList( IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws TeamRepositoryException, SportUserActionFailException
   {
      String[] htmlFormat = getHTMLFormat();
      return (formatCallListTable( callQueue, htmlFormat[0], htmlFormat[1],
            htmlFormat[2], sportCommonData ) + "</pre>");
   }

   // _B68102A
   /**
    * Generate a formatted call list from the unformatted call data stored in
    * the work item. Used for an Wiki type attribute which will build a table
    * in wiki format. Cell formatting is handled by RTC and the wiki
    * attribute.
    * 
    * @param callQueue
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    * @throws SportUserActionFailException 
    */
   public static String formatWIKICallList( IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws TeamRepositoryException, SportUserActionFailException
   {
      String[] wikiFormat = getWIKIFormat();
      return (formatCallListTable( callQueue, wikiFormat[0], wikiFormat[1],
            wikiFormat[2], sportCommonData ));
   }

   // _B68102A
   /**
    * Generate a formatted call list from the unformatted call data stored in
    * the work item.
    * 
    * @param callQueue
    * @param tableHeader
    * @param entryFormat
    * @param pmrCellLinkFormat - When formatting, 1st argument will be the PMR
    *        name [ppppp,bbb,ccc] string, second will be the PMR work item ID
    *        number.
    * @param sportCommonData
    * 
    * @return
    * @throws TeamRepositoryException
    * @throws SportUserActionFailException 
    */
   public static String formatCallListTable( IWorkItem callQueue,
         String tableHeader, String entryFormat, String pmrCellLinkFormat,
         SportCommonData sportCommonData )
         throws TeamRepositoryException, SportUserActionFailException
   {
      StringBuilder callList = new StringBuilder();
      // Header format: as passed in
      callList.append( tableHeader );

      String unformattedCallList = RtcUtils.getAttributeValueAsString(
            callQueue, CallQueueConstants.UNFORMATTED_CALL_LIST_ATTRIBUTE_ID,
            sportCommonData );
      if (!unformattedCallList.isEmpty())
      {
         // _T155539A Reduce redundant RTC queries. Once queried,
         // contains (active) PMR work item found or null if not.
         HashMap<String, IWorkItem> encounteredPmrWorkItems = new HashMap<String, IWorkItem>();
         IWorkItem pmr = null;
         // Only need to get work item type once.
         IWorkItemType pmrWIType = sportCommonData
               .getWorkItemCommon().findWorkItemType(
                     sportCommonData.getProjectArea(),
                     PmrConstants.WORK_ITEM_TYPE_ID,
                     sportCommonData.getMonitor() );

         int i = 1;
         // Raw input format:
         // <pPG><space><Call Type><space><RETAIN Dispatched ID><space><PMR>
         // <newline>
         // If any value is missing, an "<UNSPECIFIED>" placeholder is used.
         String[] unformattedCallListArray = unformattedCallList.split( "\n" );
         for (String unformattedCallListArrayEntry : unformattedCallListArray)
         {
            String[] unformattedCallListArrayEntryColumns = unformattedCallListArrayEntry
                  .split( "\\s+" );
            if (unformattedCallListArrayEntryColumns.length == 4)
            {
               String callPPG = unformattedCallListArrayEntryColumns[0];
               String callTypeValue = unformattedCallListArrayEntryColumns[1];
               String callType = (callTypeValue.equals( "<UNSPECIFIED>" ))
                     ? "" : PmrUtils.getCallType( callTypeValue,
                           sportCommonData );
               String callDispatchedTo = unformattedCallListArrayEntryColumns[2];
               if (callDispatchedTo.equals( "<UNSPECIFIED>" ))
                  callDispatchedTo = "";
               String callPmr = unformattedCallListArrayEntryColumns[3];
               if (callPmr.equals( "<UNSPECIFIED>" ))
                  callPmr = "";
               else
               {
                  // | <PMR # {with link}>
                  // PMR data defined as
                  // {"[[PMR" [work-item-ID]|} [PMR name = ppppp,bbb,ccc]
                  // {"]]"}
                  // where data in {} is only used when a link is available.
                  // Old HTML PMR data format =
                  // ppppp,bbb,ccc{ (PMR [work-item-ID])}
                  // _T155539C Need to add PMR browse to get
                  // PMR Open Date here to add as a key.
                  pmr = null;
                  if (encounteredPmrWorkItems.containsKey( callPmr ))
                  {
                     // If already queried, get it, null or not.
                     pmr = encounteredPmrWorkItems.get( callPmr );
                  }
                  else
                  {
                     pmr = PmrUtils.findPMRWorkItemInRTC( callPmr, pmrWIType,
                           false, sportCommonData );
                     encounteredPmrWorkItems.put( callPmr, pmr );
                  }
                  if (pmr != null)
                  {
                     callPmr = String.format( pmrCellLinkFormat, callPmr,
                           pmr.getId() );
                  }
               }
               // Each table call entry format:
               // Call Queue:
               // | <selection#>
               // | <pPG>
               // | <Primary, Secondary, Backup>
               // | <RETAIN-id dispatched>
               // | <PMR # {with link}>
               // PMR data defined as
               // {"[[PMR" [work-item-ID]|} [PMR name = ppppp,bbb,ccc] {"]]"}
               // where data in {} is only used when a link is available.
               // Old HTML PMR data format =
               // ppppp,bbb,ccc{ (PMR [work-item-ID])}
               callList.append( String.format( entryFormat, i, callPPG,
                     callType, callDispatchedTo, callPmr ) );
               ++i;
            }
         }
      }

      return (callList.toString());
   }

   // _B68102A
   /**
    * Define the formats for an HTML call list.
    * 
    * @return - {table header format, table entry format, PMR cell Link
    *         format}
    */
   public static String[] getHTMLFormat()
   {
      String[] htmlFormat = new String[3];
      // Header : (2 lines)
      // Selection | pPG | Call Type | Dispatched To | PMR
      // ----------+-----+-----------+---------------+------------------------
      String callListTableHeader = "";
      callListTableHeader += "<pre>";
      callListTableHeader += "Selection";
      callListTableHeader += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
      callListTableHeader += "pPG";
      callListTableHeader += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
      callListTableHeader += "Call Type";
      callListTableHeader += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
      callListTableHeader += "Dispatched To";
      callListTableHeader += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
      callListTableHeader += "PMR";
      callListTableHeader += "\n";
      callListTableHeader += StringUtils.repeat(
            StringUtils.BOX_DRAWING_HORIZONTAL, 9 );
      callListTableHeader += StringUtils.BOX_DRAWING_HORIZONTAL
            + StringUtils.BOX_DRAWING_VERTICAL_HORIZONTAL
            + StringUtils.BOX_DRAWING_HORIZONTAL;
      callListTableHeader += StringUtils.repeat(
            StringUtils.BOX_DRAWING_HORIZONTAL, 3 );
      callListTableHeader += StringUtils.BOX_DRAWING_HORIZONTAL
            + StringUtils.BOX_DRAWING_VERTICAL_HORIZONTAL
            + StringUtils.BOX_DRAWING_HORIZONTAL;
      callListTableHeader += StringUtils.repeat(
            StringUtils.BOX_DRAWING_HORIZONTAL, 9 );
      callListTableHeader += StringUtils.BOX_DRAWING_HORIZONTAL
            + StringUtils.BOX_DRAWING_VERTICAL_HORIZONTAL
            + StringUtils.BOX_DRAWING_HORIZONTAL;
      callListTableHeader += StringUtils.repeat(
            StringUtils.BOX_DRAWING_HORIZONTAL, 13 );
      callListTableHeader += StringUtils.BOX_DRAWING_HORIZONTAL
            + StringUtils.BOX_DRAWING_VERTICAL_HORIZONTAL
            + StringUtils.BOX_DRAWING_HORIZONTAL;
      callListTableHeader += StringUtils.repeat(
            StringUtils.BOX_DRAWING_HORIZONTAL, 26 );
      callListTableHeader += "\n";
      htmlFormat[0] = callListTableHeader;

      // Entries :
      // [Call selection #] |
      // [pPG] |
      // [Call Type = Primary, Secondary, Backup] |
      // [RETAIN dispatched ID] |
      // [PMR data]
      // Where [PMR data] is of the form
      // Old HTML PMR data format =
      // ppppp,bbb,ccc{ (PMR [work-item-ID])}
      // With the PMR link info in "{}" set when the work item exists
      // in RTC.
      String callListEntryFormat = "";
      callListEntryFormat += "%1$9d";
      callListEntryFormat += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
      callListEntryFormat += "%2$3s";
      callListEntryFormat += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
      callListEntryFormat += "%3$9s";
      callListEntryFormat += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
      callListEntryFormat += "%4$13s";
      callListEntryFormat += " " + StringUtils.BOX_DRAWING_VERTICAL + " ";
      callListEntryFormat += "%5$-26s";
      callListEntryFormat += "\n";
      htmlFormat[1] = callListEntryFormat;

      // PMR entry, format with link =
      // <PMR # {with link}>
      // PMR data defined as
      // Old HTML PMR data format =
      // ppppp,bbb,ccc{ (PMR [work-item-ID])}
      // With the PMR link info in "{}" set when the work item exists
      // in RTC.
      // When formatting, 1st argument will be the PMR name
      // [ppppp,bbb,ccc], second will be the PMR work item ID number.
      String pmrCellLinkFormat = "%1$s (PMR %2$d)";
      htmlFormat[2] = pmrCellLinkFormat;

      return htmlFormat;
   }

   // _B68102A
   /**
    * Define the formats for a Wiki call list.
    * 
    * @return - {table header format, table entry format, PMR cell Link
    *         format}
    */
   public static String[] getWIKIFormat()
   {
      String[] wikiFormat = new String[3];
      // Header format:
      // |= Selection |= pPG |= Call Type |= Dispatched To |= PMR
      // Also added some Web browser display usability aids
      String callListTableHeader = "";
      callListTableHeader += StringUtils.WIKI_TABLE_HEADER_CELL;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += " Selection ";
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
      callListTableHeader += " Call Type ";
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_TABLE_HEADER_CELL;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += " Dispatched To ";
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_TABLE_HEADER_CELL;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += " PMR ";
      callListTableHeader += StringUtils.WIKI_FORMAT_UNDERLINETAG;
      callListTableHeader += StringUtils.WIKI_FORMAT_BOLDTAG;
      callListTableHeader += "\n";
      wikiFormat[0] = callListTableHeader;

      // Each table call entry format:
      // Call Queue:
      // | [Call selection #]
      // | [pPG]
      // | [Call Type = Primary, Secondary, Backup]
      // | [RETAIN dispatched ID]
      // | [PMR data]
      // Where [PMR data] is of the form
      // {"[[PMR" [work-item-ID]|} [PMR name = ppppp,bbb,ccc] {"]]"}
      // With the PMR link info in "{}" set when the work item exists
      // in RTC.
      String callListEntryFormat = "";
      callListEntryFormat = StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
      callListEntryFormat += "%1$d";
      callListEntryFormat += " " + StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
      callListEntryFormat += "%2$s";
      callListEntryFormat += " " + StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
      callListEntryFormat += "%3$s";
      callListEntryFormat += " " + StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
      callListEntryFormat += "%4$s";
      callListEntryFormat += " " + StringUtils.WIKI_TABLE_ENTRY_CELL + " ";
      callListEntryFormat += "%5$s";
      callListEntryFormat += "\n";
      wikiFormat[1] = callListEntryFormat;

      // PMR entry, format with link =
      // | <PMR # {with link}>
      // PMR data defined as
      // {"[[PMR" [work-item-ID]|} [PMR name = ppppp,bbb,ccc] {"]]"}
      // where data in {} is only used when a link is available.
      // Old HTML PMR data format =
      // ppppp,bbb,ccc{ (PMR [work-item-ID])}
      // When formatting, 1st argument will be the PMR name
      // [ppppp,bbb,ccc], second will be the PMR work item ID number.
      String pmrCellLinkFormat = "[[PMR %2$d| %1$s ]]";
      wikiFormat[2] = pmrCellLinkFormat;

      return wikiFormat;
   }

   // _B68102A
   /**
    * Convert a Call Queue Call List from HTML format to WIKI table format.
    * 
    * <br>
    * HTML format = <br>
    * Header : (2 lines) <br>
    * Selection | pPG | Call Type | Dispatched To | PMR <br>
    * ----------+-----+-----------+---------------+------------------------
    * 
    * <br>
    * Entries : <br>
    * [Call selection #] | [pPG] | [Call Type = Primary, Secondary, Backup] |
    * [RETAIN dispatched ID] | [PMR data] <br>
    * Where [PMR data] is of the form <br>
    * [PMR name = ppppp,bbb,ccc] {(PMR [work-item-ID])} <br>
    * The PMR work item ID data is optional and exists for a work item in RTC.
    * 
    * <p>
    * WIKI format = <br>
    * Header : (1 line) <br>
    * |= Selection |= pPG |= Call Type |= Dispatched To |= PMR
    * 
    * <br>
    * Entries : <br>
    * | [Call selection #] | [pPG] | [Call Type = Primary, Secondary, Backup]
    * | [RETAIN dispatched ID] | [PMR data] <br>
    * Where [PMR data] is of the form <br>
    * {"[[PMR" [work-item-ID]|} [PMR name = ppppp,bbb,ccc] {"]]"} With the PMR
    * link info in "{}" set when the work item exists in RTC.
    * 
    * @param htmlCallList = The HTML format table header, and any call entries
    *        for a Call Queue work item call list.
    * @return The WIKI format table header, and any call entries in WIKI table
    *         format for a Call Queue work item call list.
    * @throws SportRuntimeException
    * @throws SportUserActionFailException
    */
   public static String convertHtmlToWIKICallList( String htmlCallList )
         throws SportRuntimeException, SportUserActionFailException
   {
      String[] wikiFormats = getWIKIFormat();
      // First define the header (has newline)
      String wikiCallList = wikiFormats[0];
      if (htmlCallList == null || htmlCallList.isEmpty())
      {
         // Null or empty work item value, still return header
         return wikiCallList;
      }
      // Beyond this point, HTML value might just be the header
      // or have calls in the content.

      String[] callListRows = null;
      try
      {
         // Entries here will have PMR data with optional link info
         // The called method is a bit "zealous" in error reporting
         // throwing exceptions. But that suits its original purpose.
         callListRows = extractCallEntries( htmlCallList );
      }
      catch (SportRuntimeException e)
      {
         // On an exception where the call list content was empty,
         // other than just a header, just use null.
         // SportUserActionFailException getting any entry will percolate.
         // Similarly any other SportRuntimeException will percolate.
         if (!(e.getMessage().contains( "Missing required Call List" ) || e
               .getMessage().contains( "Call List has no calls in it" )))
         {
            throw e;
         }
      }
      if (callListRows == null || callListRows.length == 0)
      {
         // No calls in work item attribute value, still return header
         return wikiCallList;
      }
      // Beyond this point there should be calls to gather

      // Gather any calls in work item attribute.
      /**
       * Since this needs to process an optional PMR link here, need to obtain
       * the call entries in raw form, then process each entry individually to
       * combine PMR name and any associated work item link.
       */
      CallInfo callInfo;
      String[] selectedCallColumns;
      String pmrWorkItemID;
      String callPmr;
      int pmrCellColumn = 4;
      // HTML cell divider to parse out the HTML call entry data.
      String cellDivider = StringUtils.BOX_DRAWING_VERTICAL;
      // Even if originally in unicode, the cell divider may now be the
      // string equivalent at this point.
      String cellDivRegExpr = cellDivider;
      if (cellDivider.length() == 1)
      {
         // Some String methods require regular expression.
         // Single character, should be a special character, so escape it.
         cellDivRegExpr = "\\" + cellDivider;
      }
      for (int selectedCall = 1; selectedCall <= callListRows.length; selectedCall++)
      {
         // Get homogenized form for HTML entry (object components)
         // Called method may throw some exception gathering call list
         // data
         callInfo = getCallEntry( selectedCall, callListRows, cellDivider );
         // Need to get to PMR link, rest can come from Call Info object
         // HTML format does not start with divider
         selectedCallColumns = callListRows[selectedCall - 1]
               .split( cellDivRegExpr );
         callPmr = callInfo.getPmrName();
         if (selectedCallColumns.length > pmrCellColumn)
         {
            // Determine and format PMR work item link
            pmrWorkItemID = extractPmrWorkItemID( selectedCallColumns[pmrCellColumn]
                  .trim() );
            if (pmrWorkItemID != null)
            {
               // Format the PMR with link
               // | [PMR data]
               // Where [PMR data] is of the form
               // {"[[PMR" [work-item-ID]|} [PMR name = ppppp,bbb,ccc] {"]]"}
               // With the PMR link info in "{}" set when the work item exists
               // in RTC.
               callPmr = String.format( wikiFormats[2], callPmr,
                     Integer.parseInt( pmrWorkItemID ) );
            }
         }
         // Add the call entry (has newline)
         // Each table call entry format:
         // Call Queue:
         // | [Call selection #]
         // | [pPG]
         // | [Call Type = Primary, Secondary, Backup]
         // | [RETAIN dispatched ID]
         // | [PMR data]
         // Where [PMR data] is of the form
         // {"[[PMR" [work-item-ID]|} [PMR name = ppppp,bbb,ccc] {"]]"}
         // With the PMR link info in "{}" set when the work item exists
         // in RTC.
         wikiCallList += String.format( wikiFormats[1], selectedCall,
               callInfo.getPPG(), callInfo.getCallTypeAsString(),
               callInfo.getDispatchedTo(), callPmr );
      }

      // Table header + any calls listed in WIKI format.
      return (wikiCallList);
   }

   public static String getCallDispatchedTo( ArtifactRecordPart callRecord )
   {
      return PmrUtils.getCallDispatchedTo( callRecord );
   }

   public static String getCallPmr( ArtifactRecordPart callRecord,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String callPmr = "";
      String pmrNo = callRecord
            .findFieldValue( PmrConstants.PMR_NO_ATTRIBUTE_ID );
      String branch = callRecord
            .findFieldValue( PmrConstants.BRANCH_ATTRIBUTE_ID );
      String country = callRecord
            .findFieldValue( PmrConstants.COUNTRY_ATTRIBUTE_ID );
      if ((pmrNo != null) && !pmrNo.isEmpty() && (branch != null)
            && !branch.isEmpty() && (country != null) && !country.isEmpty())
      {
         callPmr = pmrNo + "," + branch + "," + country;
      }
      return callPmr;
   }

   public static String getCallPPG( ArtifactRecordPart callRecord )
   {
      String callPPG = "";
      ArtifactRecordField callPPGField = callRecord
            .findField( PmrConstants.PPG_ATTRIBUTE_ID );
      if (callPPGField != null)
      {
         ArtifactFieldValue callPriorityValue = callPPGField
               .getFieldValue( 0 );
         String callPriority = null;
         if (callPriorityValue != null)
            callPriority = callPriorityValue.getValue();
         if (callPriority != null)
         {
            ArtifactFieldValue callPageValue = callPPGField.getFieldValue( 1 );
            String callPage = null;
            if (callPageValue != null)
               callPage = callPageValue.getValue();
            if ((callPage != null) && (callPage.length() == 1))
               callPage = "0" + callPage;
            if ((callPriority != null) && (callPage != null))
               callPPG = callPriority + callPage;
         }
      }
      return callPPG;
   }

   // _B68102C
   /**
    * Return a non-formatted CallInfo object representing the selected call.
    * Throws exceptions when no calls to select, invalid selection number,
    * etc.
    * 
    * @param callQueue
    * @param sportCommonData
    * @return - Unformatted CallInfo object representing the selected call
    *         entry.
    * @throws SportRuntimeException
    * @throws SportUserActionFailException
    * @throws TeamRepositoryException
    */
   public static CallInfo getSelectedCall( IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws SportRuntimeException, SportUserActionFailException,
         TeamRepositoryException
   {
      // _B68102C
      String[] callListRows = extractCallEntries( RtcUtils
            .getAttributeValueAsString( callQueue,
                  CallQueueConstants.CALL_LIST_ATTRIBUTE_ID, sportCommonData ) );

      // _B68102C callListRows should now only have call entries
      String selectedCallValue = RtcUtils.getAttributeValueAsString(
            callQueue, CallQueueConstants.SELECTED_CALL_ATTRIBUTE_ID,
            sportCommonData );
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

   // _B68102A get CallInfo
   /**
    * Returns just the call entries from the Call Queue attribute value.
    * Should work for HTML and Wiki formats.
    * 
    * @param callList - Call List value from work item
    * @return - String array representing the calls in the list.
    * @throws SportRuntimeException
    */
   public static String[] extractCallEntries( String callList )
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
      end = (callList.endsWith( "<br/>" )) ? callList.length() - 5 : callList
            .length();
      callList = callList.substring( start, end );
      // RTC may also return NBSP tags for whitespace - remove them here
      callList = callList.replaceAll( "&nbsp;", "" );
      // RTC gives back the value with either newlines or BR tags?
      String rowSeparator = callList.contains( "<br/>" ) ? "<br/>" : "\n";
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

   // _B68102A get CallInfo
   /**
    * Given the extracted Call Queue attribute value calls, return the
    * selected entry in internal homogenized format. Assumes Wiki format used.
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
      // Default is to get the wiki form data
      return (getCallEntry( selectedCall, callListRows,
            StringUtils.WIKI_TABLE_ENTRY_CELL ));
      // StringUtils.BOX_DRAWING_VERTICAL ));
   }

   // _B68102A get CallInfo
   /**
    * Given the extracted Call Queue attribute value calls, return the
    * selected entry in internal homogenized format. Should work for both HTML
    * and Wiki formats.
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
         throw new SportUserActionFailException( "Selected Call is too small" );
      }
      if (selectedCall > (callListRows.length))
      {
         throw new SportUserActionFailException(
               "Selected Call is too large " );
      }
      // _B68102C Wiki call entry form =
      // Call Queue:
      // | <Call selection#>
      // | <pPG>
      // | <Primary, Secondary, Backup>
      // | <RETAIN-id dispatched>
      // | <PMR # {with link}>
      // PMR data defined as
      // {"[["PMR [work-item-ID]|} ppppp,bbb,ccc {"]]"}
      // where data in {} is only used when a link is available.
      // Old HTML PMR data format =
      // ppppp,bbb,ccc{ (PMR [work-item-ID])}

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
            .startsWith( cellDivider ) ? callListRows[selectedCall - 1]
            .replaceFirst( cellDivRegExpr, "" )
            : callListRows[selectedCall - 1]);
      // _B68102C
      // Parse out call entry data for HTML or Wiki form as indicated.
      String[] selectedCallColumns = callEntry.split( cellDivRegExpr );
      // Unfortunately, the Wiki PMR work item ID data also has the divider
      // in it. This means the PMR data might be in array entries 4 *and* 5
      // after the split.
      if (selectedCallColumns.length < 5)
      {
         throw new SportRuntimeException(
               "Selected Call does not have five columns" );
      }
      else
      {
         // Re-constitute the Wiki PMR data back into array entry 4
         if (selectedCallColumns.length > 5
               && selectedCallColumns[4].trim().startsWith( "[[" )
               && selectedCallColumns[5].trim().endsWith( "]]" ))
         {
            // "[["PMR [work-item-ID]| ppppp,bbb,ccc "]]"
            // Became :
            // [entry-4] === "[["PMR [work-item-ID]
            // [entry-5] === ppppp,bbb,ccc "]]"
            selectedCallColumns[4] += cellDivider + selectedCallColumns[5];
            selectedCallColumns[5] = "";
         }
      }
      CallInfo callInfo = new CallInfo();
      callInfo.setPPG( selectedCallColumns[1].trim() );
      String callTypeValue = selectedCallColumns[2].trim();
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
         throw new SportUserActionFailException( "Unexpected Call Type of \""
               + callTypeValue + "\"" );
      }
      callInfo.setDispatchedTo( selectedCallColumns[3].trim() );
      // | <PMR # {with link}>
      // PMR data defined as
      // {"[["PMR [work-item-ID]|} ppppp,bbb,ccc {"]]"}
      // where data in {} is only used when a link is available.
      // Old HTML PMR data format =
      // ppppp,bbb,ccc{ (PMR [work-item-ID])}
      String pmrName = extractPmrName( selectedCallColumns[4].trim() );
      callInfo.setPmrName( pmrName );
      return callInfo;
   }

   // _B68102A
   /**
    * Given the raw PMR data with optional link info, return the PMR name
    * portion [ppppp,bbb,ccc]. Works for both HTML and Wiki formats.
    * 
    * @param pmrCellData = <br>
    *        PMR # {with link} <br>
    *        PMR data defined as {"[[PMR" [work-item-ID]|} [PMR name =
    *        ppppp,bbb,ccc] {"]]"} <br>
    *        where data in {} is only used when a link is available. <br>
    *        Old HTML PMR data format = <br>
    *        ppppp,bbb,ccc{ (PMR [work-item-ID])}
    * @return PMR name [ppppp,bbb,ccc]
    */
   public static String extractPmrName( String pmrCellData )
   {
      // | <PMR # {with link}>
      // PMR data defined as
      // {"[[PMR" [work-item-ID]|} [PMR name = ppppp,bbb,ccc] {"]]"}
      // where data in {} is only used when a link is available.
      // Old HTML PMR data format =
      // ppppp,bbb,ccc{ (PMR <work-item-ID>)}
      String pmrNameValue = pmrCellData;
      int i = -1;
      if ((pmrCellData.indexOf( " (PMR " ) >= 0)
            && pmrCellData.endsWith( ")" ))
      {
         // HTML format with link
         // Old HTML PMR data format =
         // ppppp,bbb,ccc{ (PMR <work-item-ID>)}
         i = pmrCellData.indexOf( ' ' );
         if (i != -1)
         {
            pmrNameValue = pmrCellData.substring( 0, i );
         }
      }
      else if (pmrCellData.startsWith( "[[" ) && pmrCellData.endsWith( "]]" ))
      {
         // Wiki format with link
         // PMR data defined as
         // {"[[PMR" [work-item-ID]|} [PMR name = ppppp,bbb,ccc] {"]]"}
         // where data in {} is only used when a link is available.
         String pmrLinkDivider = "| "; // Separates link from name
         // Remove any link end brackets and trim the end
         // RegExpr requires right bracket to be escaped - special character.
         // Brackets used for character classes in RegExpr.
         pmrCellData = pmrCellData.replaceAll( "\\]", "" ).trim();
         i = pmrCellData.indexOf( pmrLinkDivider );
         if (i != -1)
         {
            pmrNameValue = pmrCellData
                  .substring( i + pmrLinkDivider.length() );
         }
      }
      return (pmrNameValue.trim());
   }

   // _B68102A
   /**
    * Given the raw PMR data with optional link info, return the work item ID
    * if a link exists or null. If the determined work item ID cannot convert
    * to an integer, null is also returned in this case. Works for both HTML
    * and Wiki formats.
    * 
    * @param pmrCellData <br>
    *        PMR # {with link} <br>
    *        PMR data defined as {"[[PMR" [work-item-ID]|} [PMR name =
    *        ppppp,bbb,ccc] {"]]"} <br>
    *        where data in {} is only used when a link is available. <br>
    *        Old HTML PMR data format = <br>
    *        ppppp,bbb,ccc{ (PMR [work-item-ID])}
    * @return Work item ID portion as string, convertible to integer or null
    *         if no PMR link or it cannot be converted.
    */
   public static String extractPmrWorkItemID( String pmrCellData )
   {
      // | <PMR # {with link}>
      // PMR data defined as
      // {"[[PMR" [work-item-ID]|} [PMR name = ppppp,bbb,ccc] {"]]"}
      // where data in {} is only used when a link is available.
      // Old HTML PMR data format =
      // ppppp,bbb,ccc{ (PMR <work-item-ID>)}
      String pmrIDValue = null; // ID is optional
      int i = -1;
      String pmrLinkStart = "";
      if ((pmrCellData.indexOf( " (PMR " ) >= 0)
            && pmrCellData.endsWith( ")" ))
      {
         // HTML format with link
         // Old HTML PMR data format =
         // ppppp,bbb,ccc{ (PMR <work-item-ID>)}
         pmrLinkStart = "(PMR ";
         // Remove any link end brackets and trim the end
         // RegExpr requires right paren to be escaped - special character
         // Parens used for grouping in RegExpr.
         pmrCellData = pmrCellData.replaceAll( "\\)", "" ).trim();
         i = pmrCellData.indexOf( pmrLinkStart );
         if (i != -1)
         {
            pmrIDValue = pmrCellData.substring( i + pmrLinkStart.length() )
                  .trim();
         }
      }
      else if (pmrCellData.startsWith( "[[" ) && pmrCellData.endsWith( "]]" ))
      {
         // Wiki format with link
         // PMR data defined as
         // {"[[PMR" [work-item-ID]|} [PMR name = ppppp,bbb,ccc] {"]]"}
         // where data in {} is only used when a link is available.
         pmrLinkStart = "[[PMR ";
         String pmrLinkDivider = "| "; // Separates link from name
         i = pmrCellData.indexOf( pmrLinkDivider );
         // ">" is the proper test, it means there is something between
         // the start and divider.
         if (pmrCellData.startsWith( pmrLinkStart )
               && (i > pmrLinkStart.length()))
         {
            // Gather just the PMR ID portion
            // After "[[PMR ", Before "|"
            pmrIDValue = pmrCellData.substring( pmrLinkStart.length(), i )
                  .trim();
         }
      }
      if (pmrIDValue != null)
      {
         // Attempt to convert to integer
         try
         {
            @SuppressWarnings("unused")
            int pmrIDnumber = Integer.parseInt( pmrIDValue );
         }
         catch (NumberFormatException e)
         {
            // Could not convert work item ID extracted, return null
            pmrIDValue = null;
         }
      }
      return (pmrIDValue);
   }

   // _B68102A get CallInfo on all calls in Call Queue
   /**
    * Return all calls from Call Queue as CallInfo array. Assumes input in
    * Wiki format of data.
    * 
    * @param callList - Call List value from work item (Wiki format)
    * @return - ArrayList of unformatted CallInfo objects representing the
    *         calls in the list, or null if no call content in input (ie empty
    *         or just the table header)
    * @throws SportRuntimeException
    * @throws SportUserActionFailException
    */
   public static ArrayList<CallInfo> getAllCalls( String callList )
         throws SportRuntimeException, SportUserActionFailException
   {
      return (getAllCalls( callList, StringUtils.WIKI_TABLE_ENTRY_CELL ));
   }

   // _B68102A get CallInfo on all calls in Call Queue
   /**
    * Return all calls from Call Queue as CallInfo array. Should work for HTML
    * and Wiki formats.
    * 
    * @param callList - Call List value from work item
    * @param cellDivider - determines HTML or Wiki format support
    * @return - ArrayList of unformatted CallInfo objects representing the
    *         calls in the list, or null if no call content in input (ie empty
    *         or just the table header)
    * @throws SportRuntimeException
    * @throws SportUserActionFailException
    */
   public static ArrayList<CallInfo> getAllCalls( String callList,
         String cellDivider )
         throws SportRuntimeException, SportUserActionFailException
   {
      try
      {
         ArrayList<CallInfo> allCalls = new ArrayList<CallInfo>();
         String[] callListRows = extractCallEntries( callList );

         // _B68102C callListRows should now only have call entries
         // Selected call is the call entry number, 1-indexed
         for (int selectedCall = 1; selectedCall <= callListRows.length; selectedCall++)
         {
            allCalls.add( getCallEntry( selectedCall, callListRows,
                  cellDivider ) );
         }

         return (allCalls);
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
            throw e;
         }
      }
   }

   public static String getNormalizedCallqueueName( IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String callqueueName = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.CALLQUEUE_NAME_ATTRIBUTE_ID, sportCommonData );
      callqueueName = callqueueName.replaceFirst( "\\s+", "," );
      callqueueName = callqueueName.toUpperCase( Locale.ENGLISH );
      return callqueueName;
   }

   public static void performCallQueueSavePostconditionActions(
         String[] actions, IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         String actionType = actionParts[0];
         if ((actionParts.length == 2) && actionType.equals( "action" ))
         {
            String actionName = actionParts[1];
            if (actionName.equals( CallQueueConstants.RTC_CREATE_ACTION ))
            {
               createCallQueue( callQueue, sportCommonData );
            }
         }
         else if ((actionParts.length == 2) && actionType.equals( "modify" ))
         {
            String fieldName = actionParts[1];
            if (fieldName
                  .equals( CallQueueConstants.MODIFY_ACTION_ATTRIBUTE_ID ))
               performModifyAction( callQueue, sportCommonData, actions );
         }
      }
   }

   public static void performDuplicateCallQueueCheck(
         String normalalizedCallqueueName, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      synchronized (activeCallQueueSaves)
      {
         if (findCallQueue( normalalizedCallqueueName, sportCommonData ) != null)
         {
            String message = normalalizedCallqueueName
                  + " already exists in the RTC repository";
            throw new SportUserActionFailException( message );
         }
         else
         {
            if (activeCallQueueSaves.containsKey( normalalizedCallqueueName ))
            {
               String message = normalalizedCallqueueName
                     + " save currently in progress";
               throw new SportUserActionFailException( message );
            }
            else
            {
               activeCallQueueSaves.put( normalalizedCallqueueName, new Long(
                     System.currentTimeMillis() ) );
            }
         }
      }
   }

   public static void performDuplicateCallQueueCheck( String[] actions,
         IWorkItem callQueue, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      SbsUtils.checkForModfyAndChangeStateAction( actions,
            CallQueueConstants.MODIFY_ACTION_ATTRIBUTE_ID, sportCommonData );
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         if ((actionParts.length == 2) && actionParts[0].equals( "action" ))
         {
            String actionName = actionParts[1];
            if (actionName.equals( CallQueueConstants.RTC_CREATE_ACTION ))
            {
               String callqueueName = getNormalizedCallqueueName( callQueue,
                     sportCommonData );
               if ((callqueueName != null) && !callqueueName.equals( "" ))
               {
                  performDuplicateCallQueueCheck( callqueueName,
                        sportCommonData );
               }
            }
         }
      }
   }

   public static void performModifyAction( IWorkItem callQueue,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Log log = sportCommonData.getLog();
      String modifyAction = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.MODIFY_ACTION_ATTRIBUTE_ID, sportCommonData );
      if (modifyAction
            .equals( CallQueueConstants.RTC_ENTER_MODIFY_ACTION_ACTION ))
      {
         String message = "Unexpected \""
               + CallQueueConstants.RTC_ENTER_MODIFY_ACTION_ACTION
               + "\" action encountered";
         log.warn( message );
         return;
      }
      if (modifyAction
            .equals( CallQueueConstants.RTC_REFRESH_FROM_RETAIN_ACTION ))
         updateCallQueueFromRetain( callQueue, sportCommonData );
      else if (modifyAction
            .equals( CallQueueConstants.RTC_CALL_CREATE_ACTION_FOR_EXISTING_PMR ))
         callQueueCallCreateForExistingPmr( callQueue, sportCommonData );
      else if (modifyAction
            .equals( CallQueueConstants.RTC_CALL_CREATE_ACTION_WITH_NEW_PMR ))
         callQueueCallCreateWithNewPmr( callQueue, sportCommonData );
      else if (modifyAction
            .equals( CallQueueConstants.RTC_CALL_COMPLETE_ACTION ))
         callQueueCallComplete( callQueue, sportCommonData, actions );
      else if (modifyAction
            .equals( CallQueueConstants.RTC_CALL_CONVERT_ACTION ))
         callQueueCallConvert( callQueue, sportCommonData );
      else if (modifyAction
            .equals( CallQueueConstants.RTC_CALL_DISPATCH_ACTION ))
         callQueueCallDispatch( callQueue, sportCommonData );
      else if (modifyAction
            .equals( CallQueueConstants.RTC_CALL_REQUEUE_ACTION ))
         callQueueCallRequeue( callQueue, sportCommonData, actions );
      else
      {
         String message = "Unexpected modify action \"" + modifyAction
               + "\" specified";
         throw new SportRuntimeException( message );
      }
      resetModifyAction( callQueue, sportCommonData );
   }

   public static void removeActiveCallQueueSave(
         String normalizedCallqueueName )
   {
      synchronized (activeCallQueueSaves)
      {
         Long value = activeCallQueueSaves.remove( normalizedCallqueueName );
         if (value != null)
         {
            activeCallQueueSaves.notifyAll();
         }
      }
   }

   public static void resetModifyAction( IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IWorkItem callQueueToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute modifyActionAttribute = RtcUtils.findAttribute(
            callQueueToUpdate, CallQueueConstants.MODIFY_ACTION_ATTRIBUTE_ID,
            sportCommonData );
      RtcUtils.setEnumerationAttributeValue( callQueueToUpdate,
            modifyActionAttribute,
            CallQueueConstants.RTC_ENTER_MODIFY_ACTION_ACTION,
            sportCommonData );
   }

   public static void updateCallQueueFromRetain( IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String queue = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.QUEUE_ATTRIBUTE_ID, sportCommonData )
            .toUpperCase( Locale.ENGLISH );
      RtcUtils.setWorkItemAttributeToValue(
            CallQueueConstants.QUEUE_ATTRIBUTE_ID, queue, callQueue,
            sportCommonData );
      String center = RtcUtils.getAttributeValueAsString( callQueue,
            CallQueueConstants.CENTER_ATTRIBUTE_ID, sportCommonData )
            .toUpperCase( Locale.ENGLISH );
      RtcUtils.setWorkItemAttributeToValue(
            CallQueueConstants.CENTER_ATTRIBUTE_ID, center, callQueue,
            sportCommonData );
      RtcUtils.setWorkItemAttributeToValue(
            CallQueueConstants.CALLQUEUE_NAME_ATTRIBUTE_ID, queue + ","
                  + center, callQueue, sportCommonData );
      Artifact artifact = createCallSearchArtifact( queue, center,
            sportCommonData );
      Artifacts sbsResponse = SbsUtils.sendSearchArtifact( artifact,
            sportCommonData );
      String formattedCallList = formatCallList( sbsResponse, sportCommonData );
      RtcUtils.setWorkItemAttributeToValue(
            CallQueueConstants.CALL_LIST_ATTRIBUTE_ID, formattedCallList,
            callQueue, sportCommonData );
   }

   public static void blankOutAttrs( String[] attrs, IWorkItem callQueue,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {

      // Loop through the array of attributes, setting each one to ""
      if ((attrs != null) && (attrs.length > 0))
      {
         for (int i = 0; i < attrs.length; i++)
         {
            RtcUtils.setWorkItemAttributeToValue( attrs[i], "", callQueue,
                  sportCommonData );
         }
      }

   }
}
