package com.ibm.sport.rtc.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.ArtifactTechnology.common.Artifact;
import com.ibm.ArtifactTechnology.common.ArtifactRecordField;
import com.ibm.ArtifactTechnology.common.ArtifactRecordPart;
import com.ibm.ArtifactTechnology.common.Constants;
import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.expression.IQueryableAttribute;
import com.ibm.team.workitem.common.model.AttributeTypes;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IEnumeration;
import com.ibm.team.workitem.common.model.ILiteral;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.model.Identifier;
import com.ibm.team.workitem.common.model.ItemProfile;
import com.ibm.team.workitem.common.query.IQueryResult;
import com.ibm.team.workitem.common.query.IResolvedResult;

public class AparUtils
{

   private static Map<String, Long> activeAparSaves = new HashMap<String, Long>();
   private final static long APAR_SAVE_TIMEOUT = 15 * 60 * 1000; // 15 minutes
   public final static String NOT_AN_APAR_MESSAGE = " is not an APAR";
   private static String initialWorkItemState = null;

   // Create thread for cleaning up active APAR saves that fail in a non-SPoRT
   // advisor or participant.
   static
   {
      Thread thread = new Thread( new Runnable()
      {
         public void run()
         {
            Log log = LogFactory.getLog( AparUtils.class );
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
               synchronized (activeAparSaves)
               {
                  boolean notifyAllNeeded = false;
                  Iterator<Map.Entry<String, Long>> iterator = activeAparSaves
                        .entrySet().iterator();
                  while (iterator.hasNext())
                  {
                     Map.Entry<String, Long> activeAparSave = iterator.next();
                     long createTime = activeAparSave.getValue().longValue();
                     if ((System.currentTimeMillis() - createTime) >= APAR_SAVE_TIMEOUT)
                     {
                        iterator.remove();
                        notifyAllNeeded = true;
                        String aparNum = activeAparSave.getKey();
                        String message = "Removing " + aparNum
                              + " from active APAR saves due to timeout";
                        log.warn( message );
                     }
                  }
                  if (notifyAllNeeded)
                  {
                     activeAparSaves.notifyAll();
                  }
               }
            }
         }
      } );
      thread.start();
   }

   public static void assignApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_ASSIGN_ACTION,
            AparConstants.SBS_ASSIGN_ACTION_ATTRIBUTE_IDS, sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );

   }

   public static boolean checkForAttributeChange( String attributeId,
         String[] actions, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      boolean attributeChange = false;

      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         String actionType = actionParts[0];
         if ((actionParts.length == 2) && actionType.equals( "modify" ))
         {
            String fieldName = actionParts[1];

            if (fieldName.equals( attributeId )) // if modified
            { // mark boolean true and break from loop
               attributeChange = true;

               break;
            }

         }
      }
      return attributeChange;
   }

   public static void closeApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String closeCode = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.DRAFT_CLOSE_CODE_ATTRIBUTE_ID, sportCommonData );
      // _B162199A The draft close code attribute is now enumerated
      if (closeCode != null && closeCode.length() >= 3)
      {
         // _B162199A Use 1st 3 chars of full enumerated value
         closeCode = closeCode.substring( 0, 3 );
      }
      if (closeCode.equals( "CAN" ))
         closeCanApar( apar, sportCommonData );
      else if (closeCode.equals( "DOC" ))
         closeDocApar( apar, sportCommonData );
      else if (closeCode.equals( "DUP" ))
         closeDupApar( apar, sportCommonData );
      else if (closeCode.equals( "FIN" ))
         closeFinApar( apar, sportCommonData );
      else if (closeCode.equals( "ISV" ))
         closeIsvApar( apar, sportCommonData );
      else if (closeCode.equals( "MCH" ))
         closeMchApar( apar, sportCommonData );
      else if (closeCode.equals( "OEM" ))
         closeOemApar( apar, sportCommonData );
      else if (closeCode.equals( "PER" ))
         closePerApar( apar, sportCommonData );
      else if (closeCode.equals( "PRS" ))
         closePrsApar( apar, sportCommonData );
      else if (closeCode.equals( "REJ" ))
         closeRejApar( apar, sportCommonData );
      else if (closeCode.equals( "REQ" ))
         closeReqApar( apar, sportCommonData );
      else if (closeCode.equals( "RET" ))
         closeRetApar( apar, sportCommonData );
      else if (closeCode.equals( "STD" ))
         closeStdApar( apar, sportCommonData );
      else if (closeCode.equals( "SUG" ))
         closeSugApar( apar, sportCommonData );
      else if (closeCode.equals( "URX" ))
         closeUrxApar( apar, sportCommonData );
      else if (closeCode.equals( "USE" ))
         closeUseApar( apar, sportCommonData );
      else
      {
         String message = "Unknown close code \"" + closeCode
               + "\" specified";
         throw new SportUserActionFailException( message );
      }
   }

   public static void closeCanApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_CLOSE_CAN_ACTION,
                  AparConstants.SBS_CLOSE_CAN_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   public static void closeDocApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_CLOSE_DOC_ACTION,
                  AparConstants.SBS_CLOSE_DOC_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   public static void closeDupApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_CLOSE_DUP_ACTION,
                  AparConstants.SBS_CLOSE_DUP_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   protected static String[] checkFailingLevel( IWorkItem apar,
         SportCommonData sportCommonData, String[] attrs )
         throws TeamRepositoryException
   {

      // Do not include failingLvl if blank
      List<String> closeAttrs = new ArrayList<String>();

      // If the failingLvl is blank, don't add it
      for (String closeAttr : attrs)
      {
         if (closeAttr.equals( AparConstants.DRAFT_FAILING_LVL_ATTRIBUTE_ID ))
         {
            String attrValue = RtcUtils.getAttributeValueAsString( apar,
                  AparConstants.DRAFT_FAILING_LVL_ATTRIBUTE_ID,
                  sportCommonData );
            if (attrValue != null && attrValue.trim().length() > 0)
            {
               closeAttrs.add( closeAttr );
            }
         }
         else
         {
            closeAttrs.add( closeAttr );
         }
      }

      // convert to array
      String[] closeAttrsArray = new String[closeAttrs.size()];
      closeAttrsArray = closeAttrs.toArray( closeAttrsArray );
      return closeAttrsArray;

   }

   protected static void ifHiperAddHiperBannerToTemporaryFix( IWorkItem apar,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {

      // If the APAR is marked hiper, make sure the TemporaryFix banner is set
      String hiperFlag = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.HIPERY_ATTRIBUTE_ID, sportCommonData );
      if (hiperFlag.equals( "true" ))
         addHiperBannerToTemporaryFix( apar, sportCommonData );
   }

   protected static void addHiperBannerToTemporaryFix( IWorkItem apar,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String hiperBanner = AparConstants.HIPER_BANNER;
      String tempFix = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID, sportCommonData );
      if (tempFix.contains( "* HIPER *" ))
         tempFix = removeHiperBanner( tempFix );
      if (tempFix.trim().length() == 0)
      {
         tempFix = RtcUtils.getAttributeValueAsString( apar,
               AparConstants.TEMPORARY_FIX_ATTRIBUTE_ID, sportCommonData );
      }
      if (tempFix.contains( "* HIPER *" ))
         tempFix = removeHiperBanner( tempFix );

      String newTempFix = hiperBanner.concat( tempFix );
      IAttribute tempFixAttribute = RtcUtils.findAttribute( apar,
            AparConstants.DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID, sportCommonData );
      RtcUtils.setAttributeValue( apar, tempFixAttribute, newTempFix,
            sportCommonData );
   }

   protected static void cleanHiperBannerInTemporaryFixAndMessageToSubmitter(
         IWorkItem apar, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String tempFix = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.TEMPORARY_FIX_ATTRIBUTE_ID, sportCommonData );

      if ((tempFix.length() > 0) && (tempFix.contains( "* HIPER *" )))
      {
         String newTempFix = removeHiperBanner( tempFix );
         IAttribute tempFixAttribute = RtcUtils.findAttribute( apar,
               AparConstants.DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID,
               sportCommonData );
         RtcUtils.setAttributeValue( apar, tempFixAttribute, newTempFix,
               sportCommonData );
         String messageToSubmitter = RtcUtils.getAttributeValueAsString(
               apar, AparConstants.DRAFT_MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID,
               sportCommonData );
         String newMessage = "Updated Temporary Fix information in APAR closing text."
               + "\n";
         if (!messageToSubmitter.isEmpty()
               && (!messageToSubmitter.equals( newMessage )))
            newMessage = messageToSubmitter;
         IAttribute messageToSubmitterAttribute = RtcUtils.findAttribute(
               apar, AparConstants.DRAFT_MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID,
               sportCommonData );
         RtcUtils.setAttributeValue( apar, messageToSubmitterAttribute,
               newMessage, sportCommonData );
         Artifact artifact = SbsUtils
               .createArtifact(
                     apar,
                     AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_ACTION,
                     AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_HIPER_ACTION_ATTRIBUTE_IDS,
                     sportCommonData );
         SbsUtils.sendArtifacts( artifact, sportCommonData );
         updateAparFromRetain( apar, sportCommonData );
      }
   }

   protected static String removeHiperBanner( String tempFixString )
   {
      String hiperBanner = AparConstants.HIPER_BANNER;

      String endString = "";
      if (tempFixString.length() > hiperBanner.length())
      {
         endString = tempFixString.substring( hiperBanner.length() );

         // If we didn't get enough of the hiper banner, take off a little
         // more - crlf issue, and migration from initial banner format

         while ((endString.startsWith( "*" ) || endString.startsWith( "\n" ) || endString
               .startsWith( "\r" )) && (endString.length() >= 1))
         {
            endString = endString.substring( 1 );
            if (!endString.isEmpty())
               endString.trim();
            else
               endString = "";
         }
      }

      return (endString);
   }

   public static void closeFinApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {

      // Do not include failingLvl if blank
      String[] closeFINAttrs = checkFailingLevel( apar, sportCommonData,
            AparConstants.SBS_CLOSE_FIN_ACTION_ATTRIBUTE_IDS );
      ifHiperAddHiperBannerToTemporaryFix( apar, sportCommonData );
      Artifact artifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_CLOSE_FIN_ACTION, closeFINAttrs,
            sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );

   }

   public static void closeIsvApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_CLOSE_ISV_ACTION,
                  AparConstants.SBS_CLOSE_ISV_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   public static void closeMchApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_CLOSE_MCH_ACTION,
                  AparConstants.SBS_CLOSE_MCH_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   public static void closeOemApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      ifHiperAddHiperBannerToTemporaryFix( apar, sportCommonData );
      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_CLOSE_OEM_ACTION,
                  AparConstants.SBS_CLOSE_OEM_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      // _F80509A Set defaults in primary artifact record for RETAIN action
      SbsUtils
            .setUnspecifiedRequiredActionFieldsToDefault( artifact, apar,
                  AparConstants.SBS_CLOSE_OEM_ACTION,
                  AparConstants.SBS_CLOSE_OEM_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   public static void closePerApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      ifHiperAddHiperBannerToTemporaryFix( apar, sportCommonData );
      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_CLOSE_PER_ACTION,
                  AparConstants.SBS_CLOSE_PER_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      // _F80509A Set defaults in primary artifact record for RETAIN action
      SbsUtils
            .setUnspecifiedRequiredActionFieldsToDefault( artifact, apar,
                  AparConstants.SBS_CLOSE_PER_ACTION,
                  AparConstants.SBS_CLOSE_PER_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   public static void closePrsApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_CLOSE_PRS_ACTION,
                  AparConstants.SBS_CLOSE_PRS_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      // _F80509A Set defaults in primary artifact record for RETAIN action
      SbsUtils
            .setUnspecifiedRequiredActionFieldsToDefault( artifact, apar,
                  AparConstants.SBS_CLOSE_PRS_ACTION,
                  AparConstants.SBS_CLOSE_PRS_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   public static void closeRejApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_CLOSE_REJ_ACTION,
                  AparConstants.SBS_CLOSE_REJ_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   public static void closeReqApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_CLOSE_REQ_ACTION,
                  AparConstants.SBS_CLOSE_REQ_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   public static void closeRetApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_CLOSE_RET_ACTION,
                  AparConstants.SBS_CLOSE_RET_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   public static void closeStdApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_CLOSE_STD_ACTION,
                  AparConstants.SBS_CLOSE_STD_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   public static void closeSugApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_CLOSE_SUG_ACTION,
                  AparConstants.SBS_CLOSE_SUG_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   public static void closeUrxApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      ifHiperAddHiperBannerToTemporaryFix( apar, sportCommonData );
      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_CLOSE_URX_ACTION,
                  AparConstants.SBS_CLOSE_URX_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      // _F80509A Set defaults in primary artifact record for RETAIN action
      SbsUtils
            .setUnspecifiedRequiredActionFieldsToDefault(
                  artifact,
                  apar,
                  determineURXorUR12CLoseAction( apar,
                        AparConstants.SBS_CLOSE_URX_ACTION, sportCommonData ),
                  AparConstants.SBS_CLOSE_URX_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   // _F80509A Use draft test release to determine UR1 or UR2 in RETAIN.
   // When draft test release = 999, RETAIN will use UR1 or UR2.
   // Some attributes behave differently depending on this detail.
   // This is used on input to CLOSE action. RETAIN fields not yet determined.
   public static String determineURXorUR12CLoseAction( IWorkItem workItem,
         String actionName, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      if (actionName == null
            || actionName.length() == 0
            || !(actionName
                  .equalsIgnoreCase( AparConstants.SBS_CLOSE_URX_ACTION )))
      {
         return (actionName);
      }
      String resultAction = actionName;
      String testRelease = RtcUtils.getAttributeValueAsString( workItem,
            AparConstants.DRAFT_TREL_ATTRIBUTE_ID, sportCommonData );
      if (testRelease != null && testRelease.length() > 0
            && testRelease.equals( AparConstants.SBS_CLOSE_UR12_TEST_RELEASE ))
      {
         resultAction = AparConstants.SBS_CLOSE_UR12_ACTION;
      }
      return resultAction;
   }

   // _F80509A Use RETAIN test release to determine UR1 or UR2 in RETAIN.
   // When RETAIN test release = 999, RETAIN will use UR1 or UR2.
   // URX seems to be the result, however.
   // This is used when updating a CLOSED APAR in RETAIN, so use the RETAIN
   // attributes for decision making.
   public static String determineURXorUR12CLoseCode( IWorkItem workItem,
         String retainCloseCode, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      if (retainCloseCode == null || retainCloseCode.length() == 0
            || !(retainCloseCode.equalsIgnoreCase( "URX" )))
      {
         return (retainCloseCode);
      }
      String resultCode = retainCloseCode;
      String testRelease = RtcUtils.getAttributeValueAsString( workItem,
            AparConstants.TREL_ATTRIBUTE_ID, sportCommonData );
      if (testRelease != null && testRelease.length() > 0
            && testRelease.equals( AparConstants.SBS_CLOSE_UR12_TEST_RELEASE ))
      {
         resultCode = "UR12";
      }
      return resultCode;
   }

   public static void closeUseApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_CLOSE_USE_ACTION,
                  AparConstants.SBS_CLOSE_USE_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   /**
    * _T21519A Method to do the component browse to obtain result artifact
    * from SBS. Alter the incoming APAR work item and derive the needed fields
    * for the SBS input artifact. In this case, return the resulting artifact
    * for inspection.
    * 
    * @param apar
    * @param sportCommonData
    * @param componentReplacement
    * @return
    * @throws SportUserActionFailException
    * @throws TeamRepositoryException
    */
   public static Artifact componentBrowse( IWorkItem apar,
         SportCommonData sportCommonData, String componentReplacement )
         throws SportUserActionFailException, TeamRepositoryException
   {
      // Create a RETAIN component browse artifact.
      // The action should be for a component browse (ie "browseGroup"?).
      // The attribute should be the component to be browsed.
      // (I believe CQ sends it as an APAR class artifact.)

      // Build primary Artifact from APAR work item
      Artifact componentBrowseArtifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_COMPONENT_BROWSE_ACTION,
            AparConstants.SBS_COMPONENT_BROWSE_ACTION_ATTRIBUTE_IDS,
            sportCommonData );

      // Post-process artifact to turn APAR into recognized component browse
      ArtifactRecordPart cbRecord = componentBrowseArtifact.findRecord( apar
            .getWorkItemType() );
      // Reset intermediate record name
      cbRecord.setName( AparConstants.SBS_COMPONENT_BROWSE_MAIN_RECORD_NAME );
      // Obtaining choices for Sysroute attributes -
      // (Reroute and close both use the base component attr)
      if (componentReplacement != null && componentReplacement.length() > 0)
      {
         // Need to set component to different value (Sysroute action)
         // Get base component field in artifact, used by component browse
         ArtifactRecordField componentField = cbRecord
               .findField( AparConstants.COMPONENT_ATTRIBUTE_ID );
         // Sysroute value will already be set in argument
         // Copy Sysroute value to artifact field used by RETAIN action
         componentField.setValue( componentReplacement );
      }

      // Artifact now properly set up for APAR class component browse
      return (SbsUtils.sendArtifacts( componentBrowseArtifact,
            sportCommonData ));
   }

   // _T1083A Check if doing an APAR create
   public static boolean containsAparCreateAction( String[] actions,
         String workItemType )
   {
      // Initially not a create action
      boolean containsAparCreateAction = false;
      // Determine if APAR create (possibly from SBS)
      if (workItemType.equals( AparConstants.WORK_ITEM_TYPE_ID ))
      {
         // Now know its an APAR
         for (String action : actions)
         {
            String[] actionParts = action.split( "/" );
            String actionType = actionParts[0];
            if ((actionParts.length == 2) && actionType.equals( "action" ))
            {
               String actionName = actionParts[1];
               if (actionName.equals( AparConstants.RTC_CREATE_ACTION )
                     || actionName
                           .equals( AparConstants.RTC_CREATE_IN_RETAIN_ACTION ))
               {
                  // Processing an APAR create
                  containsAparCreateAction = true;
                  break;
               }
            }
         }
      }
      return containsAparCreateAction;
   }

   public static boolean containsAparCloseAction( String[] actions,
         String workItemType )
   {
      // Initially not a create action
      boolean containsAparCloseAction = false;
      // Determine if APAR create (possibly from SBS)
      if (workItemType.equals( AparConstants.WORK_ITEM_TYPE_ID ))
      {
         // Now know its an APAR
         for (String action : actions)
         {
            String[] actionParts = action.split( "/" );
            String actionType = actionParts[0];
            if ((actionParts.length == 2) && actionType.equals( "action" ))
            {
               String actionName = actionParts[1];
               if (actionName.equals( AparConstants.RTC_CLOSE_ACTION ))
               {
                  // Processing an APAR close
                  containsAparCloseAction = true;
                  break;
               }
            }
         }
      }
      return containsAparCloseAction;
   }

   public static void createApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      boolean aparInRetain = RtcUtils.getAttributeValueAsBoolean( apar,
            AparConstants.APAR_IN_RETAIN_ATTRIBUTE_ID, sportCommonData );
      if (aparInRetain)
      {
         updateAparFromRetain( apar, sportCommonData );
         // _T1334A - Unset indicator on successful create
         unsetAparInRETAINIndicator( apar, sportCommonData );
         CommonUtils.registerComponentUse( sportCommonData, apar );
      }
      else
      {
         IWorkItem aparToUpdate = sportCommonData.getWorkItemForUpdate();
         boolean populateFromPmr = RtcUtils.getAttributeValueAsBoolean( apar,
               AparConstants.POPULATE_FROM_PMR_ATTRIBUTE_ID, sportCommonData );
         if (populateFromPmr)
         {
            populateAparFromPmr( apar, aparToUpdate, sportCommonData );
         }
         boolean createAsDraft = RtcUtils.getAttributeValueAsBoolean( apar,
               AparConstants.CREATE_AS_DRAFT_ATTRIBUTE_ID, sportCommonData );
         if (createAsDraft)
         {
            // if aparNum is not specified, then give it the name *DRAFT*
            // otherwise check that the name does not already exist in the
            // project area
            String userSpecifiedAPARName = RtcUtils
                  .getAttributeValueAsString( aparToUpdate,
                        AparConstants.APAR_NUM_ATTRIBUTE_ID, sportCommonData );
            String aparNameUpper = userSpecifiedAPARName
                  .toUpperCase( Locale.ENGLISH );
            int maxMatchAllowed = 0;
            if (userSpecifiedAPARName.equals( aparNameUpper ))
            { // if name is entered in upper case,
              // then numberOfAparsNamed() returns at least 1 for the current
              // WI
               maxMatchAllowed = 1;
            }
            else
               userSpecifiedAPARName = aparNameUpper;

            if (userSpecifiedAPARName == null
                  || userSpecifiedAPARName.length() == 0)
               userSpecifiedAPARName = "*DRAFT*";

            if (!userSpecifiedAPARName.equals( "*DRAFT*" ))
            { // check for dup
               int count = numberOfAparsNamed( userSpecifiedAPARName,
                     sportCommonData );
               if (count > maxMatchAllowed)
               {
                  throw new SportUserActionFailException(
                        "The APAR "
                              + userSpecifiedAPARName
                              + " already exists in this project area, choose a different APAR Name. " );
               }
            }
            RtcUtils.setWorkItemAttributeToValue(
                  AparConstants.APAR_NUM_ATTRIBUTE_ID, userSpecifiedAPARName,
                  aparToUpdate, sportCommonData );

         }
         else
         {
            createAparInRetain( aparToUpdate, sportCommonData );
         }
      }
   }

   public static String createAparInRetain( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {

      String[] attributeList = setAttrs(
            AparConstants.SBS_CREATE_ACTION_ATTRIBUTE_IDS, apar,
            sportCommonData );

      Artifact artifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_CREATE_ACTION, attributeList, sportCommonData );
      Artifact sbsResponse = SbsUtils.sendArtifacts( artifact,
            sportCommonData );
      String aparNum = sbsResponse
            .findFieldValue( AparConstants.APAR_NUM_ATTRIBUTE_ID );
      IAttribute aparNumAttribute = RtcUtils.findAttribute( apar,
            AparConstants.APAR_NUM_ATTRIBUTE_ID, sportCommonData );
      RtcUtils.setAttributeValue( apar, aparNumAttribute, aparNum,
            sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
      CommonUtils.registerComponentUse( sportCommonData, apar );
      return aparNum;
   }

   private static String[] setAttrs( String[] createAparAttributes,
         IWorkItem apar, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      List<String> allAttrs = new ArrayList<String>();

      for (String attr : createAparAttributes)
      {
         allAttrs.add( attr );
      }

      // Only add the symptom codes if the value true
      for (String symptomAttr : AparConstants.SBS_FLAGSET_HIPER_SYMPTOM_CODES_ATTRIBUTE_IDS)
      {
         String attrValue = RtcUtils.getAttributeValueAsString( apar,
               symptomAttr, sportCommonData );
         if (attrValue != null && attrValue.trim().equals( "true" ))
            allAttrs.add( symptomAttr );
      }
      // convert to array
      String[] allAttrsArray = new String[allAttrs.size()];
      allAttrsArray = allAttrs.toArray( allAttrsArray );
      return allAttrsArray;
   }

   // Manjusha
   public static void flagsetApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {

      IAttribute operandAttr = RtcUtils.findAttribute( apar,
            AparConstants.OPERAND_ATTRIBUTE_ID, sportCommonData );
      String operand = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.OPERAND_ATTRIBUTE_ID, sportCommonData );
      IAttribute pEPTFAttr = RtcUtils.findAttribute( apar,
            AparConstants.PEPTF_ATTRIBUTE_ID, sportCommonData );

      String myPEPTF = "";
      String mySupportCode = "";
      String reasonCode = "";
      String closeCode = "";

      closeCode = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.CLOSE_CODE_ATTRIBUTE_ID, sportCommonData );
      if (operandAttr != null && operand.length() >= 1)
      {

         operand = operand.substring( 0, 1 );

      }

      String zEZ = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.ZEZ_ID, sportCommonData );

      String zEC = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.ZEC_ID, sportCommonData );

      if (operand.equals( "A" ))
      {
         myPEPTF = RtcUtils.getAttributeValueAsString( apar,
               AparConstants.PEPTF_ATTRIBUTE_ID, sportCommonData );
         if (myPEPTF != null)
            myPEPTF = myPEPTF.toUpperCase();
         apar = sportCommonData.getWorkItemForUpdate();

         if (pEPTFAttr != null)

            RtcUtils.setAttributeValue( apar, pEPTFAttr, myPEPTF,
                  sportCommonData );

         if ((closeCode.length() != 0) && !(closeCode.equals( "CAN" )))
         {
            IAttribute reasonCodeAttr = RtcUtils.findAttribute( apar,
                  AparConstants.FLAG_SET_REASON_CODE_ATTRIBUTE_ID,
                  sportCommonData );
            IAttribute supportCodeAttr = RtcUtils.findAttribute( apar,
                  AparConstants.FLAG_SET_SUPPORT_CODE_ATTRIBUTE_ID,
                  sportCommonData );

            // This flagset action works only if the PTF is in closed status
            // and is closed 'COR' or 'PER'
            mySupportCode = RtcUtils.getAttributeValueAsString( apar,
                  AparConstants.FLAG_SET_SUPPORT_CODE_ATTRIBUTE_ID,
                  sportCommonData );
            if (mySupportCode != null)
            {
               mySupportCode = mySupportCode.toUpperCase();
               apar = sportCommonData.getWorkItemForUpdate();
               if (supportCodeAttr != null)

                  RtcUtils.setAttributeValue( apar, supportCodeAttr,
                        mySupportCode, sportCommonData );

            }
            reasonCode = RtcUtils.getAttributeValueAsString( apar,
                  AparConstants.FLAG_SET_REASON_CODE_ATTRIBUTE_ID,
                  sportCommonData );
            if (reasonCodeAttr != null && reasonCode.length() >= 1)
            {
               reasonCode = reasonCode.substring( 0, 1 );
               apar = sportCommonData.getWorkItemForUpdate();
               RtcUtils.setAttributeValue( apar, reasonCodeAttr, reasonCode,
                     sportCommonData );
            }
            flagsetPEYForMostClosedApars( apar, sportCommonData );
         }
         else
            flagsetPEYApar( apar, sportCommonData );
      }
      else if (operand.equals( "B" ))
         flagsetPENApar( apar, sportCommonData );
      else if ((operand.equals( "C" )))

      {

         if ((zEC.equals( "false" )) && (zEZ.equals( "false" )))
            flagsetForZAPApar( apar, sportCommonData );
         else
            throw new SportUserActionFailException(
                  "The APAR is already marked as ZE.Plz choose another FlagSet option that is appropriate. " );

      }
      else if ((operand.equals( "D" )))

      {

         if ((zEC.equals( "false" )) && (zEZ.equals( "false" )))
            flagsetForZAPApar( apar, sportCommonData );
         else
            throw new SportUserActionFailException(
                  "The APAR is already marked as ZE.Plz choose another FlagSet option that is appropriate." );
      }

      else if (operand.equals( "E" ))

         flagsetForZAPApar( apar, sportCommonData );

      else if (operand.equals( "F" ))
      {
         if (zEC.equals( "true" ))
            flagsetForZAPApar( apar, sportCommonData );
         else if (zEC.equals( "false" ))
            throw new SportUserActionFailException(
                  "The APAR is not marked as ZE/C." );

      }
      else if (operand.equals( "G" ))
      {
         if (zEZ.equals( "true" ))
            flagsetForZAPApar( apar, sportCommonData );
         else if (zEZ.equals( "false" ))
            throw new SportUserActionFailException(
                  "The APAR is not marked as ZE/Z." );

      }
      else if ((operand.equals( "H" )))
      {
         // Intentionally update the hiper symptom flags to the desired
         // settings because the flagset for hiper will
         // not un-set flags
         List<String> allAttrs = new ArrayList<String>();
         for (String symptomAttr : AparConstants.SBS_FLAGSET_HIPER_SYMPTOM_CODES_ATTRIBUTE_IDS)
         {
            allAttrs.add( "modify/" + symptomAttr );
         }
         // convert to array
         String[] modifyActions = new String[allAttrs.size()];
         modifyActions = allAttrs.toArray( modifyActions );
         updateAparInformation( apar, sportCommonData, modifyActions );

         if ((closeCode.length() == 0))
            flagsetHiperYForNonClosedApars( apar, sportCommonData );
         else
            flagsetHiperYForClosedApars( apar, sportCommonData );
      }
      else if ((operand.equals( "I" )))

         flagsetHiperNForApars( apar, sportCommonData );

      else if ((operand.equals( "J" )) || (operand.equals( "K" )))
      {
         // if ((closeCode.length() == 0))
         // throw new SportUserActionFailException(
         // "The APAR is not closed." );
         // else
         flagsetForSpecialAttentionApar( apar, sportCommonData );

      }
      else
      {
         String message = "Unknown operand \"" + operand + "\" specified";
         throw new SportUserActionFailException( message );
      }
      // _F17138A Need to clear the operand after a successful Flagset
      // to allow enumeration filtering with valid values in the operand.

      // (This is the way this should've been done throughout this method,
      // with a new object. Need to insure we obtain a working copy, and not
      // reset the argument passed in. But the above works, so leave it as-is.
      // I believe "apar" is passed by value, so we dont un-do anything for
      // the caller. It is just that it becomes unpredictable whether "apar"
      // references the original argument or the working copy.)
      // The operand attribute originally obtained points to the original
      // argument workitem, need to re-acquire in working copy.
      // The empty string forces the lookup of the unspecified enumeration.
      IWorkItem aparToUpdate = sportCommonData.getWorkItemForUpdate();
      RtcUtils.setWorkItemAttributeToValue(
            AparConstants.OPERAND_ATTRIBUTE_ID, "", aparToUpdate,
            sportCommonData );

   }

   private static void flagsetHiperYForNonClosedApars( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      List<String> flagsetAttrs = new ArrayList<String>();

      // initialize to list of required attributes
      for (String hiperAttr : AparConstants.SBS_FLAGSET_HIPERY_NONCLOSEDAPARS_ACTION_ATTRIBUTE_IDS)
      {
         flagsetAttrs.add( hiperAttr );
      }

      // Only add the symptom codes if the value true
      for (String symptomAttr : AparConstants.SBS_FLAGSET_HIPER_SYMPTOM_CODES_ATTRIBUTE_IDS)
      {
         String attrValue = RtcUtils.getAttributeValueAsString( apar,
               symptomAttr, sportCommonData );
         if (attrValue != null && attrValue.trim().equals( "true" ))
            flagsetAttrs.add( symptomAttr );
      }
      // convert to array
      String[] flagsetAttrsArray = new String[flagsetAttrs.size()];
      flagsetAttrsArray = flagsetAttrs.toArray( flagsetAttrsArray );
      Artifact artifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_FLAGSET_HIPER_ACTION, flagsetAttrsArray,
            sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );

   }

   private static void flagsetHiperYForClosedApars( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      List<String> flagsetAttrs = new ArrayList<String>();

      // initialize to list of required attributes
      for (String hiperAttr : AparConstants.SBS_FLAGSET_HIPERY_CLOSEDAPARS_ACTION_ATTRIBUTE_IDS)
      {
         flagsetAttrs.add( hiperAttr );
      }

      // Only add the symptom codes if the value true
      for (String symptomAttr : AparConstants.SBS_FLAGSET_HIPER_SYMPTOM_CODES_ATTRIBUTE_IDS)
      {
         String attrValue = RtcUtils.getAttributeValueAsString( apar,
               symptomAttr, sportCommonData );
         if (attrValue != null && attrValue.trim().equals( "true" ))
            flagsetAttrs.add( symptomAttr );
      }
      // convert to array
      String[] flagsetAttrsArray = new String[flagsetAttrs.size()];
      flagsetAttrsArray = flagsetAttrs.toArray( flagsetAttrsArray );
      Artifact artifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_FLAGSET_HIPER_ACTION, flagsetAttrsArray,
            sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );

      // Set the hiper banner in the temporary fix and
      // set the required field for APAR FOLLOWUP, MessageToSubmitter
      addHiperBannerToTemporaryFix( apar, sportCommonData );
      String messageToSubmitter = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.DRAFT_MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID,
            sportCommonData );
      String newMessage = "Updated Temporary Fix information in APAR closing text."
            + "\n";
      if (!messageToSubmitter.contains( newMessage ))
         messageToSubmitter = newMessage.concat( messageToSubmitter );
      IAttribute messageToSubmitterAttribute = RtcUtils.findAttribute( apar,
            AparConstants.DRAFT_MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID,
            sportCommonData );
      RtcUtils.setAttributeValue( apar, messageToSubmitterAttribute,
            messageToSubmitter, sportCommonData );
      artifact = SbsUtils
            .createArtifact(
                  apar,
                  AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_ACTION,
                  AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_HIPER_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );

   }

   private static void flagsetHiperNForApars( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_FLAGSET_HIPER_ACTION,
            AparConstants.SBS_FLAGSET_HIPERN_ACTION_ATTRIBUTE_IDS,
            sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );

      // Clean up the hiper banner in the temporary fix and the message to
      // submitter, if the APAR is closed and going from hiper to non-hiper
      String closeCode = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.CLOSE_CODE_ATTRIBUTE_ID, sportCommonData );
      if (closeCode.length() > 0)
         cleanHiperBannerInTemporaryFixAndMessageToSubmitter( apar,
               sportCommonData );
   }

   private static void flagsetPEYApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils
            .createArtifact(
                  apar,
                  AparConstants.SBS_FLAGSET_PE_ACTION,
                  AparConstants.SBS_FLAGSET_PEY_FOR_OTHER_APARS_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );

   }

   private static void flagsetPEYForMostClosedApars( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils
            .createArtifact(
                  apar,
                  AparConstants.SBS_FLAGSET_PE_ACTION,
                  AparConstants.SBS_FLAGSET_PEY_FOR_MOSTCLOSEDAPARS_ACTION_ATTRIBUTE_IDS,
                  sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );

   }

   private static void flagsetPENApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_FLAGSET_PE_ACTION,
            AparConstants.SBS_FLAGSET_PEN_ACTION_ATTRIBUTE_IDS,
            sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );

   }

   private static void flagsetForSpecialAttentionApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      List<String> flagsetAttrs = new ArrayList<String>();

      // initialize to list of required attributes
      for (String specAttnAttr : AparConstants.SBS_FLAGSET_SPECIALATTENTION_ACTION_ATTRIBUTE_IDS)
      {
         flagsetAttrs.add( specAttnAttr );
      }

      // Only add the symptom codes if the value is true
      for (String symptomAttr : AparConstants.SBS_FLAGSET_SPECIALATTENTION_SYMPTOM_FLAGS_ATTRIBUTE_IDS)
      {
         String attrValue = RtcUtils.getAttributeValueAsString( apar,
               symptomAttr, sportCommonData );
         if (attrValue != null && attrValue.trim().equals( "true" ))
            flagsetAttrs.add( symptomAttr );
      }
      // convert to array
      String[] flagsetAttrsArray = new String[flagsetAttrs.size()];
      flagsetAttrsArray = flagsetAttrs.toArray( flagsetAttrsArray );
      Artifact artifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_FLAGSET_SPECIALATTENTION_ACTION,
            flagsetAttrsArray, sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );

   }

   private static void flagsetForZAPApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_FLAGSET_ZAP_ACTION,
            AparConstants.SBS_FLAGSET_ZAP_ACTION_ATTRIBUTE_IDS,
            sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );

   }

   public static IWorkItem findApar( String aparNum,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IQueryableAttribute aparNumAttribute = RtcUtils.findQueryableAttribute(
            AparConstants.APAR_NUM_ATTRIBUTE_ID, sportCommonData );
      IQueryableAttribute workItemTypeAttribute = RtcUtils
            .findQueryableAttribute( IWorkItem.TYPE_PROPERTY, sportCommonData );
      Map<IQueryableAttribute, Object> attributeValues = new HashMap<IQueryableAttribute, Object>();
      attributeValues.put( aparNumAttribute, aparNum );
      attributeValues.put( workItemTypeAttribute,
            AparConstants.WORK_ITEM_TYPE_ID );
      IQueryResult<IResolvedResult<IWorkItem>> apars = RtcUtils
            .queryWorkItemsByAttributes( attributeValues, sportCommonData );
      int aparCount = apars.getResultSize( monitor ).getTotalAvailable();
      if (aparCount > 1)
         throw new SportRuntimeException( "More than one APAR named \""
               + aparNum + "\" found in RTC" );
      return (aparCount == 0) ? null : apars.next( monitor ).getItem();
   }

   public static IWorkItem findApar( String aparNum, boolean willCreateApar,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      aparNum = aparNum.toUpperCase( Locale.ENGLISH );
      synchronized (activeAparSaves)
      {
         IWorkItem apar = findApar( aparNum, sportCommonData );
         if (apar == null)
         {
            while (activeAparSaves.containsKey( aparNum ))
            {
               try
               {
                  activeAparSaves.wait();
               }
               catch (InterruptedException e)
               {
                  String message = "Unexpected interruption waiting on active save of "
                        + aparNum;
                  throw new SportRuntimeException( message, e );
               }
            }
            apar = findApar( aparNum, sportCommonData );
         }
         if ((apar == null) && willCreateApar)
         {
            activeAparSaves.put( aparNum,
                  new Long( System.currentTimeMillis() ) );
         }
         return apar;
      }
   }

   public static void fixTestApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_FIXTEST_ACTION,
            AparConstants.SBS_FIXTEST_ACTION_ATTRIBUTE_IDS, sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   public static String[] getAttributeIdsForSBSAction( String sbsAction,
         HashMap<String, String> h )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String[] sbsActionAttributeIds = null;
      String attributeIdsForEachSBSAction = (String)h.get( sbsAction );
      if (attributeIdsForEachSBSAction != null)
      {
         StringTokenizer st = new StringTokenizer(
               attributeIdsForEachSBSAction, "," );
         sbsActionAttributeIds = new String[st.countTokens()];
         int i = 0;
         while (st.hasMoreTokens())
         {
            sbsActionAttributeIds[i] = st.nextToken();

            i++;
         }
      }

      return sbsActionAttributeIds;
   }

   public static void modifyDraftApar( IWorkItem apar,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {
      // if aparNum has been updated, check for uniqueness

      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         String actionType = actionParts[0];
         if ((actionParts.length == 2) && actionType.equals( "modify" ))
         {
            String fieldName = actionParts[1];

            if (fieldName.equals( AparConstants.APAR_NUM_ATTRIBUTE_ID ))
            { // check for uniqueness
               String userSpecifiedAPARName = RtcUtils
                     .getAttributeValueAsString( apar,
                           AparConstants.APAR_NUM_ATTRIBUTE_ID,
                           sportCommonData );
               String aparNameUpper = userSpecifiedAPARName
                     .toUpperCase( Locale.ENGLISH );
               int maxMatchAllowed = 0;
               if (userSpecifiedAPARName.equals( aparNameUpper ))
               { // if name is entered in upper case,
                 // then numberOfAparsNamed() returns at least 1
                  maxMatchAllowed = 1;
               }
               else
                  userSpecifiedAPARName = aparNameUpper;
               // if blanked out, set back to default name
               if (userSpecifiedAPARName == null
                     || userSpecifiedAPARName.length() == 0)
                  userSpecifiedAPARName = "*DRAFT*";
               if (!userSpecifiedAPARName.equals( "*DRAFT*" ))
               { // check for dup
                  int count = numberOfAparsNamed( userSpecifiedAPARName,
                        sportCommonData );
                  if (count > maxMatchAllowed)
                  {
                     throw new SportUserActionFailException(
                           "The APAR "
                                 + userSpecifiedAPARName
                                 + " already exists in the project area, choose a different APAR Name. " );
                  }
               }
               // save upper cased name
               RtcUtils.setWorkItemAttributeToValue(
                     AparConstants.APAR_NUM_ATTRIBUTE_ID,
                     userSpecifiedAPARName, apar, sportCommonData );
               break; // bail out of for loop
            }
         }
      }
      // _B104230A
      // In case any draft close attrs are set, also save them in store attrs.
      // If create in RETAIN done, the draft attrs remain unchanged since the
      // RETAIN status has not been set yet. The result state is INTRAN.
      // However, if the APAR is receipted to OPEN state, the draft attrs can
      // get "restored", so dont want them to be empty.
      restoreAparDraftAttributes( apar, sportCommonData, false, false );

   }

   /*
    * numberOfAparsNamed is copied from findApar() but does not throw an
    * exception if more than one work item is found, it simply returns the
    * number of work items found
    */
   public static int numberOfAparsNamed( String aparNum,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IQueryableAttribute aparNumAttribute = RtcUtils.findQueryableAttribute(
            AparConstants.APAR_NUM_ATTRIBUTE_ID, sportCommonData );
      IQueryableAttribute workItemTypeAttribute = RtcUtils
            .findQueryableAttribute( IWorkItem.TYPE_PROPERTY, sportCommonData );
      Map<IQueryableAttribute, Object> attributeValues = new HashMap<IQueryableAttribute, Object>();
      attributeValues.put( aparNumAttribute, aparNum );
      attributeValues.put( workItemTypeAttribute,
            AparConstants.WORK_ITEM_TYPE_ID );
      IQueryResult<IResolvedResult<IWorkItem>> apars = RtcUtils
            .queryWorkItemsByAttributes( attributeValues, sportCommonData );
      int aparCount = apars.getResultSize( monitor ).getTotalAvailable();
      return aparCount;
   }

   public static void performAparSavePostconditionActions( String[] actions,
         IWorkItem apar, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      // _B93726A In order to test if a state changed in an APAR work item,
      // we need the original state. Easy for a modify action, but for a
      // state change action, work flow resolution takes place prior to
      // the operation participant, and the state has already been updated.
      // As an alternate solution, use the retainStatus attribute which
      // will not reflect an update until a follow-up browse. This should work
      // for modify and state-change actions.
      initialWorkItemState = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.RETAIN_STATUS_ATTRIBUTE_ID, sportCommonData );
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         String actionType = actionParts[0];
         if ((actionParts.length == 2) && actionType.equals( "action" ))
         {
            String actionName = actionParts[1];
            if (actionName.equals( AparConstants.RTC_CLOSE_ACTION ))
               closeApar( apar, sportCommonData );
            else if (actionName.equals( AparConstants.RTC_CREATE_ACTION ))
            {
               createApar( apar, sportCommonData );
               // _T1083A Set summary - Handles RTC creates
               // Draft, Both RTC and RETAIN, RTC-only (in RETAIN)
               setAparSummaryOnCreate( apar, sportCommonData );
            }
            else if (actionName
                  .equals( AparConstants.RTC_CREATE_IN_RETAIN_ACTION ))
            {
               // This is used when creating a RETAIN APAR from a saved Draft
               // Create the RETAIN APAR
               createAparInRetain( apar, sportCommonData );
               // _B166164A Unset Draft indicator once created in RETAIN
               // This allows the Summary to initially be set from abstract
               unsetAparDraftIndicator( apar, sportCommonData );
               // _T1083A Set summary
               setAparSummaryOnCreate( apar, sportCommonData );
            }
            else if (actionName.equals( AparConstants.RTC_FIXTEST_ACTION ))
               fixTestApar( apar, sportCommonData );
            else if (actionName.equals( AparConstants.RTC_RECEIPT_ACTION ))
               receiptApar( apar, sportCommonData );
            else if (actionName
                  .equals( AparConstants.RTC_RECEIPT_ASSIGN_ACTION ))
            {
               receiptApar( apar, sportCommonData );
               assignApar( apar, sportCommonData );
               // _B93726C Either of the above action code will already have a
               // RETAIN refresh performed upon successful completion. So no
               // need for a redundant refresh here.
               // updateAparFromRetain( apar, sportCommonData );
            }
            else if (actionName.equals( AparConstants.RTC_REOPEN_ACTION ))
               reopenApar( apar, sportCommonData );
            else if (actionName.equals( AparConstants.RTC_REACTIVATE_ACTION ))
               reActivateApar( apar, sportCommonData );

         }
         else if ((actionParts.length == 2) && actionType.equals( "modify" ))
         {
            String fieldName = actionParts[1];
            if (fieldName.equals( AparConstants.MODIFY_ACTION_ATTRIBUTE_ID ))
               performModifyAction( apar, sportCommonData, actions );
         }
      }
   }

   public static void performDuplicateAparCheck( String[] actions,
         IWorkItem apar, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      SbsUtils.checkForModfyAndChangeStateAction( actions,
            AparConstants.MODIFY_ACTION_ATTRIBUTE_ID, sportCommonData );
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         if ((actionParts.length == 2) && actionParts[0].equals( "action" ))
         {
            String actionName = actionParts[1];
            if (actionName.equals( AparConstants.RTC_CREATE_ACTION ))
            {
               String aparNum = RtcUtils.getAttributeValueAsString( apar,
                     AparConstants.APAR_NUM_ATTRIBUTE_ID, sportCommonData );
               if ((aparNum != null) && !aparNum.equals( "" ))
               {
                  performDuplicateAparCheck( aparNum, sportCommonData );
               }
            }
         }
      }
   }

   public static void performDuplicateAparCheck( String aparNum,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      aparNum = aparNum.toUpperCase( Locale.ENGLISH );
      synchronized (activeAparSaves)
      {
         if (findApar( aparNum, sportCommonData ) != null)
         {
            String message = aparNum
                  + " already exists in the RTC repository";
            throw new SportUserActionFailException( message );
         }
         else
         {
            if (activeAparSaves.containsKey( aparNum ))
            {
               String message = aparNum + " save currently in progress";
               throw new SportUserActionFailException( message );
            }
            else
            {
               activeAparSaves.put( aparNum,
                     new Long( System.currentTimeMillis() ) );
            }
         }
      }
   }

   public static void performModifyAction( IWorkItem apar,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Log log = sportCommonData.getLog();
      String modifyAction = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.MODIFY_ACTION_ATTRIBUTE_ID, sportCommonData );
      if (modifyAction.equals( AparConstants.RTC_ENTER_MODIFY_ACTION_ACTION ))
      {
         // This shouldn't really happen, so we simply log a warning and
         // ignore the action.
         String message = "Unexpected \""
               + AparConstants.RTC_ENTER_MODIFY_ACTION_ACTION
               + "\" action encountered";
         log.warn( message );
         return;
      }
      if (modifyAction
            .equals( AparConstants.RTC_MODIFY_CLOSING_TEXT_IN_RTC_ONLY_ACTION_ACTION ))
      {
         // Modify Closing Text in RTC Only will be skipped.
         resetModifyAction( apar, sportCommonData );
         // _B104230C Check if DRAFT or INTRAN state
         String currentWorkItemState = RtcUtils.getWorkflowStateName(
               apar.getState2(), apar, sportCommonData );
         // _B104230A
         // In case any draft close attrs are set, also save them in store
         // attrs.
         // If create in RETAIN done, the draft attrs remain unchanged since
         // the
         // RETAIN status has not been set yet. The result state is INTRAN.
         // However, if the APAR is receipted to OPEN state, the draft attrs
         // can
         // get "restored", so dont want them to be empty.
         if (currentWorkItemState != null
               && currentWorkItemState.length() > 0
               && (currentWorkItemState.toUpperCase().equals( "DRAFT" ) || currentWorkItemState
                     .toUpperCase().equals( "INTRAN" )))
         {
            restoreAparDraftAttributes( apar, sportCommonData, false, false );
         }
         return;
      }
      if (modifyAction.equals( AparConstants.RTC_REFRESH_FROM_RETAIN_ACTION ))
         updateAparFromRetain( apar, sportCommonData );
      else if (modifyAction
            .equals( AparConstants.RTC_GET_CLOSING_TEXT_FROM_PARENT_ACTION ))
      {
         getClosingTextFromParent( apar, sportCommonData );
      }
      else if (modifyAction
            .equals( AparConstants.RTC_FETCH_PMR_TEXT_FROM_RETAIN_ACTION ))
      {
         fetchPmrTextFromRetain( apar, sportCommonData );
      }
      else if (modifyAction.equals( AparConstants.RTC_ASSIGN_ACTION ))
         assignApar( apar, sportCommonData );
      else if (modifyAction.equals( AparConstants.RTC_CLEAR_PI_DATA_ACTION ))
         clearPIDataInApar( apar, sportCommonData );
      else if (modifyAction.equals( AparConstants.RTC_FLAGSET_ACTION ))
         flagsetApar( apar, sportCommonData );
      else if (modifyAction.equals( AparConstants.RTC_REROUTE_ACTION ))
         rerouteApar( apar, sportCommonData );
      else if (modifyAction.equals( AparConstants.RTC_S2UPDATE_ACTION ))
         s2UpdateApar( apar, sportCommonData, actions );
      else if (modifyAction.equals( AparConstants.RTC_UPDATEAPARINFO_ACTION ))
         updateAparInformation( apar, sportCommonData, actions );
      else if (modifyAction.equals( AparConstants.RTC_SYSROUTE_ACTION ))
         sysrouteApar( apar, sportCommonData, actions );
      else if (modifyAction
            .equals( AparConstants.RTC_UPDATECLOSINGTEXT_ACTION ))
         updateClosingTextApar( apar, sportCommonData );

      // else if (modifyAction.equals( AparConstants.RTC_SECINT_ACTION ))
      // updateSecIntApar( apar, sportCommonData );
      else if (modifyAction
            .equals( AparConstants.RTC_UPDATESUBSCRIBEDPMRS_ACTION ))
         updateSubscribedPmr( apar, sportCommonData );
      else if (modifyAction.equals( AparConstants.RTC_REVIEW_ACTION ))
         ; // Do nothing, the reviewStatus and reviewNotes are RTC-only
      // attributes
      else if (modifyAction
            .equals( AparConstants.RTC_MODIFY_DRAFT_APAR_ACTION ))
         modifyDraftApar( apar, sportCommonData, actions );
      else if (modifyAction
            .equals( AparConstants.RTC_RESET_DRAFT_CLOSING_INFORMATION_ACTION ))
         restoreAparDraftAttributes( apar, sportCommonData, false, true );
      // _F234119A Do an APAR browse, then copy Draft Close values
      // from the result artifact.
      else if (modifyAction
            .equals( AparConstants.RTC_POPULATE_DRAFT_CLOSING_INFORMATION_ACTION ))
         populateDraftCloseInfoFromRETAIN( apar, sportCommonData );
      else if (modifyAction
            .equals( AparConstants.RTC_MARK_AS_SANITIZED_ACTION ))
         markAsSanitized( apar, sportCommonData );
      else if (modifyAction
            .equals( AparConstants.RTC_MARK_AS_NOT_SANITIZED_ACTION ))
         markAsNotSanitized( apar, sportCommonData );
      else
      {
         String message = "Unexpected modify action \"" + modifyAction
               + "\" specified";
         throw new SportRuntimeException( message );
      }
      resetModifyAction( apar, sportCommonData );
   }

   private static void populateAparFromPmr( IWorkItem apar,
         IWorkItem aparToUpdate, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      final String pmrDataFields[][] = {
            { AparConstants.COMPONENT_ATTRIBUTE_ID,
                  PmrConstants.COMPONENT_ID_ATTRIBUTE_ID },
            { AparConstants.CUSTOMER_NUMBER_ATTRIBUTE_ID,
                  PmrConstants.CUSTOMER_NUMBER_ATTRIBUTE_ID },
            { AparConstants.RELEASE_ATTRIBUTE_ID,
                  PmrConstants.RELEASE_ATTRIBUTE_ID },
            { AparConstants.RETAIN_SEVERITY_ATTRIBUTE_ID,
                  PmrConstants.RETAIN_SEVERITY_ATTRIBUTE_ID },
            { AparConstants.SYSTEM_RELEASE_LEVEL_ATTRIBUTE_ID,
                  PmrConstants.RELEASE_ATTRIBUTE_ID } };
      final String pmrSubmitterDataFields[][] = {
            { AparConstants.CONTACT_PHONE_ATTRIBUTE_ID,
                  PmrConstants.CONTACT_PHONE_1_ATTRIBUTE_ID },
            { AparConstants.SUBMITTER_ATTRIBUTE_ID,
                  PmrConstants.CONTACT_NAME_ATTRIBUTE_ID } };
      String pmrName = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.PMR_NAME_ATTRIBUTE_ID, sportCommonData );
      Artifact pmrBrowse = SbsUtils.createPMRArtifact( apar,
            PmrConstants.SBS_BROWSE_ACTION,
            PmrConstants.SBS_BROWSE_ACTION_ATTRIBUTE_IDS, sportCommonData,
            pmrName );
      Artifact sbsResponse = SbsUtils.sendArtifacts( pmrBrowse,
            sportCommonData );
      ArtifactRecordPart pmrRecord = sbsResponse
            .findRecord( PmrConstants.WORK_ITEM_TYPE_ID );
      for (String pmrDataField[] : pmrDataFields)
      {
         populateAparFieldFromPmr( pmrDataField[0], pmrDataField[1], apar,
               aparToUpdate, pmrRecord, sportCommonData );
      }
      boolean updateSubmitterFromPmr = RtcUtils.getAttributeValueAsBoolean(
            apar, AparConstants.UPDATE_SUBMITTER_FROM_PMR_ATTRIBUTE_ID,
            sportCommonData );
      if (updateSubmitterFromPmr)
      {
         for (String pmrSubmitterDataField[] : pmrSubmitterDataFields)
         {
            populateAparFieldFromPmr( pmrSubmitterDataField[0],
                  pmrSubmitterDataField[1], apar, aparToUpdate, pmrRecord,
                  sportCommonData );
         }
      }
   }

   private static void populateAparFieldFromPmr( String aparAttributeId,
         String pmrAttributeId, IWorkItem apar, IWorkItem aparToUpdate,
         ArtifactRecordPart pmrRecord, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String aparAttributeValue = RtcUtils.getAttributeValueAsString( apar,
            aparAttributeId, sportCommonData );
      if ((aparAttributeValue == null)
            || aparAttributeValue.trim().equals( "" )
            || (aparAttributeId
                  .equals( AparConstants.CUSTOMER_NUMBER_ATTRIBUTE_ID ) && aparAttributeValue
                  .equals( "9999999" ))
            || (aparAttributeId
                  .equals( AparConstants.RETAIN_SEVERITY_ATTRIBUTE_ID ) && aparAttributeValue
                  .equals( "3" )))
      {
         String pmrAttributeValue = pmrRecord.findField( pmrAttributeId )
               .getFirstValueAsString();
         RtcUtils.setWorkItemAttributeToValue( aparAttributeId,
               pmrAttributeValue, aparToUpdate, sportCommonData );
      }
   }

   /*
    * processes the sbsResponse artifact from sysroute request
    */
   public static String processSysrouteResponse( IWorkItem apar,
         Artifact sbsResponse, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // the response artifact contains minimal data, most important is the
      // newly
      // created apar number
      ArtifactRecordPart aparRecord = sbsResponse.findRecord( apar
            .getWorkItemType() );

      String aparNum = aparRecord
            .findFieldValue( AparConstants.APAR_NUM_ATTRIBUTE_ID );
      // .getFirstValueAsString();

      if (aparNum != null)
      { // save the new apar number to be added to RTC
        // sportCommonData.addNewAparName( aparNum );
         sportCommonData.addNewAparName2( new SysrouteAparInfo( aparNum,
               RtcUtils.getAttributeValueAsString( apar,
                     AparConstants.APAR_NUM_ATTRIBUTE_ID, sportCommonData ),
               SysrouteAparInfo.relationship.SYSROUTED_TO ) );
      }
      return aparNum;
   }

   public static void reActivateApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_REACTIVATE_ACTION,
            AparConstants.SBS_REACTIVATE_ACTION_ATTRIBUTE_IDS,
            sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );

   }

   public static void receiptApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_RECEIPT_ACTION,
            AparConstants.SBS_RECEIPT_ACTION_ATTRIBUTE_IDS, sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   public static void reopenApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {

      // Get the existing RETAIN closing information and save it before the
      // reopen
      // happens because the reopen will wipe out the RETAIN closing
      // information
      Hashtable<String, String> retainClosingInfo = getRETAINClosingInfo(
            apar, sportCommonData );

      Artifact artifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_REOPEN_ACTION,
            AparConstants.SBS_REOPEN_ACTION_ATTRIBUTE_IDS, sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );

      // If the reopen was successful, then copy or merge the RETAIN closing
      // information with the draft closing information
      prepareToCopyClosingInfo( apar, sportCommonData, retainClosingInfo );

   }

   public static void removeActiveAparSave( String aparNum )
   {
      aparNum = aparNum.toUpperCase( Locale.ENGLISH );
      synchronized (activeAparSaves)
      {
         Long value = activeAparSaves.remove( aparNum );
         if (value != null)
         {
            activeAparSaves.notifyAll();
         }
      }
   }

   public static void rerouteApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      /*
       * _B93726C Now done within APAR refresh. String initialState =
       * RtcUtils.getWorkflowStateName( apar.getState2(), apar,
       * sportCommonData ); // _B48060A Did state change?
       */
      Artifact artifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_REROUTE_ACTION,
            AparConstants.SBS_REROUTE_ACTION_ATTRIBUTE_IDS, sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
      /*
       * _B93726C Now done within APAR refresh. // _B48060A If RR new CT
       * changed state to INTRAN, // where residual draft close attrs might
       * have invalid data now, // clear the draft close attrs. if
       * (stateChangedAfterUpdate( apar, initialState, sportCommonData )) {
       * resetWorkItemAttrs( apar,
       * AparConstants.SBS_REROUTE_ACTION_RESET_DRAFT_ATTRIBUTE_IDS,
       * sportCommonData ); }
       */
   }

   public static void resetModifyAction( IWorkItem apar,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // _T1083C (Re)uses working copy
      IWorkItem aparToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute modifyActionAttribute = RtcUtils.findAttribute(
            aparToUpdate, AparConstants.MODIFY_ACTION_ATTRIBUTE_ID,
            sportCommonData );
      RtcUtils.setEnumerationAttributeValue( aparToUpdate,
            modifyActionAttribute,
            AparConstants.RTC_ENTER_MODIFY_ACTION_ACTION, sportCommonData );
      // _T1083M To reuse working copy, save moved to end of Participant
      // RtcUtils.saveWorkItem( aparToUpdate, sportCommonData );
   }

   public static void markAsSanitized( IWorkItem apar,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {

      IWorkItem aparToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute sanitizedAttribute = RtcUtils.findAttribute( aparToUpdate,
            AparConstants.SANITIZED_ATTRIBUTE_ID, sportCommonData );
      RtcUtils.setBooleanAttributeValue( aparToUpdate, sanitizedAttribute,
            "true", sportCommonData );

   }

   public static void markAsNotSanitized( IWorkItem apar,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {

      IWorkItem aparToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute sanitizedAttribute = RtcUtils.findAttribute( aparToUpdate,
            AparConstants.SANITIZED_ATTRIBUTE_ID, sportCommonData );
      RtcUtils.setBooleanAttributeValue( aparToUpdate, sanitizedAttribute,
            "false", sportCommonData );

   }

   public static void clearPIDataInApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {

      // Set the Submitter and Contact Phone attributes to ""
      IWorkItem aparToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute submitterAttribute = RtcUtils.findAttribute( aparToUpdate,
            AparConstants.SUBMITTER_ATTRIBUTE_ID, sportCommonData );
      RtcUtils.setAttributeValue( aparToUpdate, submitterAttribute, "",
            sportCommonData );
      IAttribute contactPhoneAttribute = RtcUtils.findAttribute(
            aparToUpdate, AparConstants.CONTACT_PHONE_ATTRIBUTE_ID,
            sportCommonData );
      RtcUtils.setAttributeValue( aparToUpdate, contactPhoneAttribute, "",
            sportCommonData );

      // Data has been cleared, now mark it as sanitized
      markAsSanitized( apar, sportCommonData );

   }

   /*
    * _B48060C A common method to reset the passed in set of custom attr IDs.
    * Derived from previous resetSysrouteAttrs.
    */
   public static void resetWorkItemAttrs( IWorkItem apar,
         String[] attrsToReset, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IWorkItem aparToUpdate = sportCommonData.getWorkItemForUpdate();
      // Go thru every attribute requested by caller
      for (String attributeId : attrsToReset)
      {
         IAttribute attribute = RtcUtils.findAttribute( aparToUpdate,
               attributeId, sportCommonData );
         if (attribute == null)
         {
            throw new SportRuntimeException( "Work item type \""
                  + aparToUpdate.getWorkItemType()
                  + "\" does not have the attribute \"" + attributeId + "\"" );
         }
         String attributeType = attribute.getAttributeType();

         if (attributeType.equals( AttributeTypes.BOOLEAN ))
            RtcUtils.setBooleanAttributeValue( aparToUpdate, attribute,
                  "false", sportCommonData );
         else if (RtcUtils.attributeIsEnumeration( attributeType ))
         {
            RtcUtils.setEnumerationAttributeValue( aparToUpdate, attribute,
                  null, sportCommonData );
         }
         else
         { // let RTCUtils figure the type
            if (attributeId
                  .equals( AparConstants.DRAFT_SUPPORT_CODE_ATTRIBUTE_ID ))
               RtcUtils.setAttributeValue( aparToUpdate, attribute,
                     AparConstants.UNKNOWN, sportCommonData );
            else
               RtcUtils.setAttributeValue( aparToUpdate, attribute, null,
                     sportCommonData );
         }
      } // end for
   }

   /**
    * _B93726A draft-attrs = restore-attrs, clear restore-attributes <br>
    * or when restore is false : restore-attrs = draft-attrs, clear draft
    * attrs.
    * 
    * @param apar
    * @param sportCommonData
    * @param restore <br>
    *        clearSourceAttrs === true
    * @throws TeamRepositoryException
    */
   public static void restoreAparDraftAttributes( IWorkItem apar,
         SportCommonData sportCommonData, boolean restore )
         throws TeamRepositoryException
   {
      // _B104230C Default to clear the source attrs
      restoreAparDraftAttributes( apar, sportCommonData, restore, true );
   }

   /**
    * _B93726A draft-attrs = restore-attrs, optionally clear
    * restore-attributes <br>
    * or when restore is false : restore-attrs = draft-attrs, optionally clear
    * draft attrs.
    * 
    * @param apar
    * @param sportCommonData
    * @param restore
    * @param clearSourceAttrs
    * @throws TeamRepositoryException
    */
   public static void restoreAparDraftAttributes( IWorkItem apar,
         SportCommonData sportCommonData, boolean restore,
         boolean clearSourceAttrs )
         throws TeamRepositoryException
   {
      IWorkItem aparToUpdate = sportCommonData.getWorkItemForUpdate();
      // In the following Hash Map the keys are the draft attributes,
      // and the values are the corresponding restoration attributes.
      for (String draftAttr : AparConstants.SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS
            .keySet())
      {
         // Get the corresponding restoration attribute
         String restoreAttr = AparConstants.SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS
               .get( draftAttr );
         IAttribute restoreAttribute = RtcUtils.findAttribute( aparToUpdate,
               restoreAttr, sportCommonData );
         // Note: This validation may just be for UT.
         if (restoreAttribute == null)
         {
            // <TBD> Log info and break (set flag)?
            throw new SportRuntimeException( "Work item type \""
                  + aparToUpdate.getWorkItemType()
                  + "\" does not have the attribute \"" + restoreAttr + "\"" );
         }
         String restoreAttributeType = restoreAttribute.getAttributeType();
         // Get the DRAFT attribute
         IAttribute draftAttribute = RtcUtils.findAttribute( aparToUpdate,
               draftAttr, sportCommonData );
         if (draftAttribute == null)
         {
            // <TBD> Log info and break (set flag)?
            throw new SportRuntimeException( "Work item type \""
                  + aparToUpdate.getWorkItemType()
                  + "\" does not have the attribute \"" + draftAttr + "\"" );
         }
         String draftAttributeType = draftAttribute.getAttributeType();
         if (!(draftAttributeType.equals( restoreAttributeType )))
         {
            throw new SportRuntimeException(
                  "Attribute types do not match. \""
                        + draftAttributeType
                        + "\" draft attribute does not match restore attribute type \""
                        + restoreAttributeType + "\"" );
         }

         // Can now store the draft close attributes.
         // Let RTCUtils figure the type
         if (restore)
         {
            // draft = restore
            RtcUtils.setAttributeValue( aparToUpdate, draftAttribute,
                  RtcUtils.getAttributeValueAsString( aparToUpdate,
                        restoreAttribute, sportCommonData ), sportCommonData );
         }
         else
         {
            // restore = draft
            RtcUtils.setAttributeValue( aparToUpdate, restoreAttribute,
                  RtcUtils.getAttributeValueAsString( aparToUpdate,
                        draftAttribute, sportCommonData ), sportCommonData );
         }

      } // End draft-attr set
        // _B104230C Need to clear the source attrs
      if (clearSourceAttrs)
      {
         if (restore)
         {
            // Clear the current restore attributes
            resetWorkItemAttrs(
                  apar,
                  AparConstants.SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS
                        .values().toArray( new String[0] ), sportCommonData );
         }
         else
         {
            // Clear the current draft attributes
            resetWorkItemAttrs(
                  apar,
                  AparConstants.SBS_INTRAN_APAR_CLOSE_SAVERESTORE_DRAFT_ATTRIBUTE_IDS
                        .keySet().toArray( new String[0] ), sportCommonData );
         }
      }
   }

   public static void s2UpdateApar( IWorkItem apar,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {

      List<String> s2attrs = new ArrayList<String>();
      // initialize to list of required attributes

      for (String reqAttr : AparConstants.SBS_S2UPDATE_ACTION_REQ_ATTRIBUTE_IDS)
      {
         s2attrs.add( reqAttr );
      }
      // go thru list of actions, and add those which are in the optional
      // attribute list
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         String actionType = actionParts[0];
         if ((actionParts.length == 2) && actionType.equals( "modify" ))
         {
            String fieldName = actionParts[1];
            for (String s2field : AparConstants.SBS_S2UPDATE_ACTION_OPT_ATTRIBUTE_IDS)
            {
               if (fieldName.equals( s2field )) // if modified
               { // add to list
                  s2attrs.add( fieldName );
                  break; // bail out of for loop
               }
            }
         }
      }

      if (s2attrs.size() == (AparConstants.SBS_S2UPDATE_ACTION_REQ_ATTRIBUTE_IDS).length)
      {
         String message = "Please specify at least one changed attribute for S2Update";
         throw new SportUserActionFailException( message );
      }
      else
      {
         // convert to array
         String[] s2attrsArray = new String[s2attrs.size()];
         s2attrsArray = s2attrs.toArray( s2attrsArray );
         Artifact artifact = SbsUtils.createArtifact( apar,
               AparConstants.SBS_S2UPDATE_ACTION, s2attrsArray,
               sportCommonData );
         SbsUtils.sendArtifacts( artifact, sportCommonData );
         updateAparFromRetain( apar, sportCommonData );
      }
   }

   // _T1083A Set APAR Summary for Draft APAR or from Abstract
   // Input APAR work item is the initial work item passed to Save Participant
   public static void setAparSummaryOnCreate( IWorkItem apar,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // Need to use working copy of work item here if it exists,
      // as Create APAR From RETAIN initial work item will NOT have
      // the Abstract attribute set (only has aparNum).
      IWorkItem aparWithAbstract = sportCommonData.getWorkItemForUpdate();
      boolean createAsDraft = RtcUtils.getAttributeValueAsBoolean(
            aparWithAbstract, AparConstants.CREATE_AS_DRAFT_ATTRIBUTE_ID,
            sportCommonData );
      if (createAsDraft)
      {
         // Summary = Draft APAR <id>
         setAparSummaryToValue(
               "Draft APAR " + Integer.toString( aparWithAbstract.getId() ),
               aparWithAbstract, sportCommonData );
      }
      else
      {
         // Summary = aparNum & Abstract
         setAparSummaryFromAttribute( AparConstants.APAR_NUM_ATTRIBUTE_ID,
               AparConstants.ABSTRACT_TEXT_ATTRIBUTE_ID, aparWithAbstract,
               sportCommonData );
      }
   }

   // _T1083A Set APAR Summary from given attribute
   public static void setAparSummaryFromAttribute( String aparNumAttrId,
         String abstractAttrId, IWorkItem apar,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      StringBuffer aparSummaryValue = new StringBuffer( "" );
      IAttribute aparNumAttr = RtcUtils.findAttribute( apar, aparNumAttrId,
            sportCommonData );
      IAttribute abstractAttr = RtcUtils.findAttribute( apar, abstractAttrId,
            sportCommonData );
      if (aparNumAttr != null)
         aparSummaryValue.append( RtcUtils.getAttributeValueAsString( apar,
               aparNumAttr.getIdentifier(), sportCommonData ) );
      if (abstractAttr != null)
      {
         // If the abstract is multiple lines, simply concatenate them into
         // one line with a space in between
         String abstractVal = RtcUtils.getAttributeValueAsString( apar,
               abstractAttr.getIdentifier(), sportCommonData );
         String[] splitStrings = abstractVal.split( "(\r\n|\r|\n)" );
         for (int j = 0; j < splitStrings.length; j++)
         {
            aparSummaryValue.append( " " + splitStrings[j].trim() );
         }
      }
      setAparSummaryToValue( aparSummaryValue.toString(), apar,
            sportCommonData );
   }

   // _T1083A Set APAR Summary to given string
   public static void setAparSummaryToValue( String aparSummaryValue,
         IWorkItem apar, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      if (aparSummaryValue != null && apar != null)
      {
         // Get or reuse working copy
         IWorkItem aparToUpdate = sportCommonData.getWorkItemForUpdate();
         // Set HTML built-in Summary from string
         aparToUpdate.setHTMLSummary( XMLString
               .createFromPlainText( aparSummaryValue ) );
         // _T1083A To reuse working copy, save done at end of Participant
      }
   }

   /*
    * _B48060A Check if the state has changed in the work item. User must save
    * the state from the work item prior to the update from a browse. This
    * should only be called after the browse to update the work item from
    * RETAIN.
    */
   public static boolean stateChangedAfterUpdate( IWorkItem apar,
         String initialState, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // Get the working copy with the latest state
      IWorkItem aparToUpdate = sportCommonData.getWorkItemForUpdate();
      String stateAfterAction = initialState;
      if (aparToUpdate != null)
      {
         stateAfterAction = RtcUtils.getWorkflowStateName(
               aparToUpdate.getState2(), aparToUpdate, sportCommonData );
      }
      return (!(initialState.equals( stateAfterAction )));
   }

   public static void sysrouteApar( IWorkItem apar,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String routeSelection = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.SYSROUTE_TYPE_ATTRIBUTE_ID, sportCommonData );
      if (routeSelection.equals( "New INTRAN APAR" ))
         sysrouteAparA( apar, sportCommonData );
      else if (routeSelection.equals( "New CLOSED APAR" ))
         sysrouteAparB( apar, sportCommonData );
      else if (routeSelection
            .equals( "New INTRAN APAR, cancel original APAR" ))
         sysrouteAparC( apar, sportCommonData, actions );
      else
      {
         String message = "Unknown sysroute type \"" + routeSelection
               + "\" specified";
         throw new SportUserActionFailException( message );
      }
      // reset attributes on sysroute tab
      // resetSysrouteAttrs( apar, sportCommonData );
      // _B48060C - made a common method
      resetWorkItemAttrs( apar, AparConstants.SBS_ROUTE_ACTION_ATTRIBUTE_IDS,
            sportCommonData );

   }

   public static void sysrouteAparA( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      List<String> sysrouteAttrs = new ArrayList<String>();

      // initialize to list of required attributes
      for (String routeAAttr : AparConstants.SBS_ROUTEA_ACTION_ATTRIBUTE_IDS)
      {
         sysrouteAttrs.add( routeAAttr );
      }
      for (String optAttr : AparConstants.SBS_ROUTE_ACTION_OPTATTRIBUTE_IDS)
      {
         String attrValue = RtcUtils.getAttributeValueAsString( apar,
               optAttr, sportCommonData );
         if (attrValue != null && attrValue.trim().length() > 0)
            sysrouteAttrs.add( optAttr );
      }
      // convert to array
      String[] routeAttrsArray = new String[sysrouteAttrs.size()];
      routeAttrsArray = sysrouteAttrs.toArray( routeAttrsArray );

      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_ROUTEA_ACTION,
                  routeAttrsArray, sportCommonData );
      Artifact sbsResponse = SbsUtils.sendArtifacts( artifact,
            sportCommonData );

      String newApar = processSysrouteResponse( apar, sbsResponse,
            sportCommonData );
      // save apar name to auto receipt
      if (RtcUtils.getAttributeValueAsBoolean( apar,
            AparConstants.TO_AUTO_RECEIPT_ATTRIBUTE_ID, sportCommonData ))
      {
         sportCommonData.setAparToAutoReceipt( newApar );
      }
      try
      {
         Thread.sleep( 1000 ); // wait for Retain to finish
      }
      catch (InterruptedException e)
      {
         Log log = sportCommonData.getLog();
         String message = "InterruptedException caught while "
               + "sleep/waiting for sysroute action";
         log.warn( message, e );
      }
      updateAparFromRetain( apar, sportCommonData );
   }

   public static void sysrouteAparB( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      // do not include ApplicableComponentLevels if closeCode in
      // {DOC,OEM,PRS}
      List<String> sysrouteAttrs = new ArrayList<String>();

      // initialize to list of required attributes
      for (String routeBAttr : AparConstants.SBS_ROUTEB_ACTION_ATTRIBUTE_IDS)
      {
         sysrouteAttrs.add( routeBAttr );
      }
      String closeCode = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.CLOSE_CODE_ATTRIBUTE_ID, sportCommonData );
      if (!"DOC".equals( closeCode ) && !"OEM".equals( closeCode )
            && !"PRS".equals( closeCode ))
      { // include ApplicableComponentLevels
         String attrValue = RtcUtils.getAttributeValueAsString( apar,
               AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID,
               sportCommonData );
         if (attrValue != null && attrValue.trim().length() > 0)
            sysrouteAttrs
                  .add( AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID );

      }

      // Make sure to remove the Hiper banner text in TEMPORARY_FIX so it
      // doesn't get copied to the new CLOSED APAR that will not be Hiper
      // upon creation. The fix fields in the originating APAR will be copied.
      cleanHiperBannerInTemporaryFixAndMessageToSubmitter( apar,
            sportCommonData );

      // optional parameters: changeTeam
      for (String optAttr : AparConstants.SBS_ROUTEB_ACTION_OPTATTRIBUTE_IDS)
      {
         String attrValue = RtcUtils.getAttributeValueAsString( apar,
               optAttr, sportCommonData );
         if (attrValue != null && attrValue.trim().length() > 0)
            sysrouteAttrs.add( optAttr );
      }

      // convert to array
      String[] routeAttrsArray = new String[sysrouteAttrs.size()];
      routeAttrsArray = sysrouteAttrs.toArray( routeAttrsArray );

      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_ROUTEB_ACTION,
                  routeAttrsArray, sportCommonData );
      Artifact sbsResponse = SbsUtils.sendArtifacts( artifact,
            sportCommonData );

      processSysrouteResponse( apar, sbsResponse, sportCommonData );

      // Add the Hiper banner back to the Temporary Fix for the original APAR,
      // if hiper
      ifHiperAddHiperBannerToTemporaryFix( apar, sportCommonData );
      String tempFix = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.DRAFT_TEMPORARY_FIX_ATTRIBUTE_ID, sportCommonData );
      if (tempFix.contains( "* HIPER *" ))
      {
         String messageToSubmitter = RtcUtils.getAttributeValueAsString(
               apar, AparConstants.DRAFT_MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID,
               sportCommonData );
         String newMessage = "Updated Temporary Fix information in APAR closing text."
               + "\n";
         if (!messageToSubmitter.contains( newMessage ))
            messageToSubmitter = newMessage.concat( messageToSubmitter );
         IAttribute messageToSubmitterAttribute = RtcUtils.findAttribute(
               apar, AparConstants.DRAFT_MESSAGE_TO_SUBMITTER_ATTRIBUTE_ID,
               sportCommonData );
         RtcUtils.setAttributeValue( apar, messageToSubmitterAttribute,
               messageToSubmitter, sportCommonData );
         artifact = SbsUtils
               .createArtifact(
                     apar,
                     AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_ACTION,
                     AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_HIPER_ACTION_ATTRIBUTE_IDS,
                     sportCommonData );
         SbsUtils.sendArtifacts( artifact, sportCommonData );
         updateAparFromRetain( apar, sportCommonData );
      }
      try
      {
         Thread.sleep( 1000 ); // wait for Retain to finish
      }
      catch (InterruptedException e)
      {
         Log log = sportCommonData.getLog();
         String message = "InterruptedException caught while "
               + "sleep/waiting for sysroute action";
         log.warn( message, e );
      }
      updateAparFromRetain( apar, sportCommonData );

   }

   public static void sysrouteAparC( IWorkItem apar,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {
      List<String> sysrouteAttrs = new ArrayList<String>();

      // initialize to list of required attributes
      for (String routeAAttr : AparConstants.SBS_ROUTEC_ACTION_ATTRIBUTE_IDS)
      {
         sysrouteAttrs.add( routeAAttr );
      }
      for (String optAttr : AparConstants.SBS_ROUTE_ACTION_OPTATTRIBUTE_IDS)
      {
         String attrValue = RtcUtils.getAttributeValueAsString( apar,
               optAttr, sportCommonData );
         if (attrValue != null && attrValue.trim().length() > 0)
            sysrouteAttrs.add( optAttr );
      }
      // convert to array
      String[] routeAttrsArray = new String[sysrouteAttrs.size()];
      routeAttrsArray = sysrouteAttrs.toArray( routeAttrsArray );

      Artifact artifact = SbsUtils
            .createArtifact( apar, AparConstants.SBS_ROUTEC_ACTION,
                  routeAttrsArray, sportCommonData );
      Artifact sbsResponse = SbsUtils.sendArtifacts( artifact,
            sportCommonData );

      String newApar = processSysrouteResponse( apar, sbsResponse,
            sportCommonData );
      // save auto receipt
      if (RtcUtils.getAttributeValueAsBoolean( apar,
            AparConstants.TO_AUTO_RECEIPT_ATTRIBUTE_ID, sportCommonData ))
      {
         sportCommonData.setAparToAutoReceipt( newApar );
      }
      try
      {
         Thread.sleep( 1000 ); // wait for Retain to finish
      }
      catch (InterruptedException e)
      {
         Log log = sportCommonData.getLog();
         String message = "InterruptedException caught while "
               + "sleep/waiting for sysroute action";
         log.warn( message, e );
      }
      updateAparFromRetain( apar, sportCommonData );

   }

   // _T1334A UnSet APAR in RETAIN indicator.
   // Called when creating an APAR already in RETAIN is successful.
   public static void unsetAparInRETAINIndicator( IWorkItem apar,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // Obtain indicator in source work item
      boolean inRETAIN = RtcUtils.getAttributeValueAsBoolean( apar,
            AparConstants.APAR_IN_RETAIN_ATTRIBUTE_ID, sportCommonData );
      if (inRETAIN)
      {
         // Need to use working copy of work item here
         IWorkItem aparToUpdate = sportCommonData.getWorkItemForUpdate();
         // Unset APAR In RETAIN
         RtcUtils.setWorkItemAttributeToValue(
               AparConstants.APAR_IN_RETAIN_ATTRIBUTE_ID, "FALSE",
               aparToUpdate, sportCommonData );
         // Save done later at end of participant processing
      }
   }

   public static void updateAparInformation( IWorkItem apar,
         SportCommonData sportCommonData, String[] actions )
         throws SportUserActionFailException, TeamRepositoryException
   {

      int size = AparConstants.SBS_UPDATEAPARINFO_ACTIONS.size();
      Iterator<String> t = AparConstants.SBS_UPDATEAPARINFO_ACTIONS.keySet()
            .iterator();

      ArrayList<String> editFixedFields = new ArrayList<String>();
      ArrayList<String> editSubmitterText = new ArrayList<String>();
      // this field 'attributeChanged' will be true or false for each
      // attribute
      boolean attributeChanged = false;
      // counter for counting the number of attributes changed
      int attributesChanged = 0;
      String sbsAction = "";
      String[] sbsActionAttributeIds = null;

      for (int x = 0; x < size; x++)
      // loop through the attribute,sbsAction key value pairs in the
      // SBS_UPDATEAPARINFO_ACTIONS hashmap,the hashmap
      // covers all 14 attributes that could be edited by Update APAR
      // Information
      {
         String attributeId = "";
         if (t.hasNext())
            attributeId = (String)t.next();

         sbsAction = AparConstants.SBS_UPDATEAPARINFO_ACTIONS
               .get( attributeId );

         attributeChanged = checkForAttributeChange( attributeId, actions,
               sportCommonData );

         if (attributeChanged)

         {
            // Increment counter since attribute is changed

            attributesChanged++;

            // mark 'editFixedFields' field true you can call SBS action once
            // for all attributes whose sbsAction is same
            if (sbsAction
                  .equals( AparConstants.SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION ))
            {
               editFixedFields.add( attributeId );
            }
            // mark 'editSubmitterText' field true you can call SBS action
            // once for all attributes whose sbsAction is same
            else if (sbsAction
                  .equals( AparConstants.SBS_UPDATEAPARINFO_UPDATESUBMITTERTEXT_ACTION ))
            {
               editSubmitterText.add( attributeId );
            }
            // update work item since you know ahead that this severityChange
            // sbsAction is not
            // called multiple times
            else if ((sbsAction
                  .equals( AparConstants.SBS_UPDATEAPARINFO_UPDATESEVERITY_ACTION )))
            {
               sbsActionAttributeIds = getAttributeIdsForSBSAction(
                     sbsAction,
                     AparConstants.SBS_UPDATEAPARINFO_ATTRIBUTE_IDS );
               if ((sbsActionAttributeIds != null)
                     && (sbsActionAttributeIds.length != 0))
                  updateAPARWorkItem( apar, sbsAction, sbsActionAttributeIds,
                        sportCommonData );
               else
                  throw new SportUserActionFailException(
                        "Update APAR Information action failed" );
            }
         } // end of attribute changed loop, check next attribute
         else
            // attribute not changed,continue and check next attribute
            continue;
      } // for loop ends here

      // No attributes changed,throw message

      if (attributesChanged == 0)
      {
         // we dont want to throw an error if the only thing changed was
         // security integrity rating
         if (!checkForAttributeChange(
               AparConstants.SECURITY_INTEGRITY_RATING_ATTRIBUTE_ID, actions,
               sportCommonData ))
         {
            String message = "Please try 'Refresh From RETAIN' action and/or update at least one attribute for 'Update APAR Information' action";
            throw new SportUserActionFailException( message );
         }
      }
      else
      {
         // you can call SBS action once for attributes whose sbsAction is
         // same instead of calling multiple times in case more than one got
         // updated

         if (editFixedFields.size() > 0)// if any of the fixed fields are
         // updated
         {

            editFixedFields.add( AparConstants.APAR_NUM_ATTRIBUTE_ID );
            editFixedFields.add( AparConstants.COMPONENT_ATTRIBUTE_ID );

            sbsAction = AparConstants.SBS_UPDATEAPARINFO_UPDATEFIXEDFIELDS_ACTION;
            sbsActionAttributeIds = getStringArrayFromArrayList( editFixedFields );

            // if ((sbsActionAttributeIds != null)
            // && (sbsActionAttributeIds.length != 0))
            updateAPARWorkItem( apar, sbsAction, sbsActionAttributeIds,
                  sportCommonData );
            // else
            // throw new SportUserActionFailException(
            // "Update APAR Information action failed" );

         }
         if (editSubmitterText.size() > 0)// if error description and/or local
         // fix is updated
         {
            editSubmitterText.add( AparConstants.APAR_NUM_ATTRIBUTE_ID );
            sbsAction = AparConstants.SBS_UPDATEAPARINFO_UPDATESUBMITTERTEXT_ACTION;
            sbsActionAttributeIds = getStringArrayFromArrayList( editSubmitterText );
            // if ((sbsActionAttributeIds != null)
            // && (sbsActionAttributeIds.length != 0))
            updateAPARWorkItem( apar, sbsAction, sbsActionAttributeIds,
                  sportCommonData );
            // else
            // throw new SportUserActionFailException(
            // "Update APAR Information action failed" );
         }
      }
   }

   public static String[] getStringArrayFromArrayList(
         ArrayList<String> arrayList )
         throws SportUserActionFailException, TeamRepositoryException
   {

      String[] stringArray = new String[arrayList.size()];
      for (int i = 0; i < arrayList.size(); i++)
         stringArray[i] = arrayList.get( i );

      return stringArray;
   }

   public static void updateClosingTextApar( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {

      String closeCode = "";

      closeCode = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.CLOSE_CODE_ATTRIBUTE_ID, sportCommonData );
      // for closed APARs
      if ((closeCode != null) && (closeCode.length() > 0))
      {
         Artifact artifact = SbsUtils
               .createArtifact(
                     apar,
                     AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_ACTION,
                     AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_ACTION_ATTRIBUTE_IDS,
                     sportCommonData );
         // _F80509A Set defaults in primary artifact record for RETAIN action
         // Will be granular based on specific close code set.
         // Determine if updating a UR1 or UR2 closed APAR based on RETAIN
         // attributes resulting from close.
         SbsUtils
               .setUnspecifiedRequiredActionFieldsToDefault(
                     artifact,
                     apar,
                     AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_ACTION
                           + determineURXorUR12CLoseCode( apar, closeCode,
                                 sportCommonData ),
                     AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_ACTION_ATTRIBUTE_IDS,
                     sportCommonData );
         SbsUtils.sendArtifacts( artifact, sportCommonData );
         updateAparFromRetain( apar, sportCommonData );

      }
      else
      {// for open Apars
         Artifact artifact = SbsUtils
               .createArtifact(
                     apar,
                     AparConstants.SBS_UPDATECLOSINGTEXT_EDITRESPONDERPAGE_ACTION,
                     AparConstants.SBS_UPDATECLOSINGTEXT_EDITRESPONDERPAGE_ACTION_ATTRIBUTE_IDS,
                     sportCommonData );
         SbsUtils.sendArtifacts( artifact, sportCommonData );
         updateAparFromRetain( apar, sportCommonData );

      }
   }

   // _B166164A UnSet APAR Create As Draft indicator.
   // Called when Create in RETAIN action is successful.
   public static void unsetAparDraftIndicator( IWorkItem apar,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // Obtain indicator in source work item
      boolean createAsDraft = RtcUtils.getAttributeValueAsBoolean( apar,
            AparConstants.CREATE_AS_DRAFT_ATTRIBUTE_ID, sportCommonData );
      if (createAsDraft)
      {
         // Need to use working copy of work item here
         IWorkItem aparToUpdate = sportCommonData.getWorkItemForUpdate();
         // Unset Create As Draft
         RtcUtils.setWorkItemAttributeToValue(
               AparConstants.CREATE_AS_DRAFT_ATTRIBUTE_ID, "FALSE",
               aparToUpdate, sportCommonData );
         // Save done later at end of participant processing
      }
   }

   public static void updateAparFromRetain( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( apar,
            AparConstants.SBS_BROWSE_ACTION,
            AparConstants.SBS_BROWSE_ACTION_ATTRIBUTE_IDS, sportCommonData );
      Artifact sbsResponse = SbsUtils.sendArtifacts( artifact,
            sportCommonData );
      ArtifactRecordPart aparRecord = sbsResponse.findRecord( apar
            .getWorkItemType() );
      String aparPtf = aparRecord.findField(
            AparConstants.APAR_PTF_ATTRIBUTE_ID ).getFirstValueAsString();
      if (!aparPtf.equals( "A" ))
      {
         String aparNum = aparRecord.findField(
               AparConstants.APAR_NUM_ATTRIBUTE_ID ).getFirstValueAsString();
         throw new SportUserActionFailException( aparNum
               + NOT_AN_APAR_MESSAGE );
      }
      IWorkItem aparToUpdate = sportCommonData.getWorkItemForUpdate();
      // Get the sanitized attribute
      IAttribute sanitizedAttr = RtcUtils.findAttribute( apar,
            AparConstants.SANITIZED_ATTRIBUTE_ID, sportCommonData );
      Boolean sanitized = (Boolean)aparToUpdate.getValue( sanitizedAttr );
      for (ArtifactRecordField field : aparRecord.getFields())
      {
         String fieldName = field.getName();
         // Manjusha-changing method to getAllValuesAsString() to get multiple
         // lines from RETAIN
         String fieldValue = field.getAllValuesAsString();
         // _T1083C Use new common utility method to set customized
         // attributes. Reuses working copy if obtained.
         // If sanitized, don't update submitter name or contact phone
         if (!sanitized)
         {
            RtcUtils.setWorkItemAttributeToValue( fieldName, fieldValue,
                  apar, sportCommonData );
         }
         else
         {
            if (!fieldName.equals( AparConstants.SUBMITTER_ATTRIBUTE_ID )
                  && (!fieldName
                        .equals( AparConstants.CONTACT_PHONE_ATTRIBUTE_ID )))
            {
               RtcUtils.setWorkItemAttributeToValue( fieldName, fieldValue,
                     apar, sportCommonData );
            }
         }

         // _T1083C Built-in attributes require using their own access methods
         // (Work flow state method gets working copy explicitly passed in.)
         if (fieldName.equals( AparConstants.RETAIN_STATUS_ATTRIBUTE_ID ))
            RtcUtils.setWorkflowState( aparToUpdate, fieldValue,
                  sportCommonData );

         // bring in sysrouted from/to apars if not already exist in RTC
         if ((fieldValue != null)
               && (fieldValue.length() > 0)
               && (fieldName
                     .equals( AparConstants.SYSROUTED_FROM_ATTRIBUTE_ID ) || fieldName
                     .equals( AparConstants.SYSROUTED_TO_ATTRIBUTE_ID )))
         {
            // make sure name is not PTF, get ptfRequested attribute
            String ptfRequested = RtcUtils.getAttributeValueAsString(
                  aparToUpdate, AparConstants.PTF_REQUESTED_ATTRIBUTE_ID,
                  sportCommonData );
            String[] ptfNames = ptfRequested.split( "," );

            boolean isPtf = false;

            String[] aparNames = fieldValue.split( "," );
            for (String aparName : aparNames)
            {
               isPtf = false;
               // check if sysroutedTo is a PTF name

               for (String ptfName : ptfNames)
               {
                  if (aparName.equals( ptfName ))
                  {
                     isPtf = true;
                     break;
                  }
               }

               if ( // regardless whether apar already exists in RTC
               !isPtf) // process only if not ptf
               {
                  SysrouteAparInfo newAparInfo = new SysrouteAparInfo(
                        aparName, // target
                        RtcUtils.getAttributeValueAsString(
                              // source
                              apar, AparConstants.APAR_NUM_ATTRIBUTE_ID,
                              sportCommonData ),
                        fieldName
                              .equals( AparConstants.SYSROUTED_FROM_ATTRIBUTE_ID )
                              ? SysrouteAparInfo.relationship.SYSROUTED_FROM
                              : SysrouteAparInfo.relationship.SYSROUTED_TO ); // relation
                  sportCommonData.addNewAparName2( newAparInfo );

               }
            } // for aparName

         } // endif fieldName is sysroutedFrom or sysroutedTo

      }
      // _B93726A Handle draft close attributes if state changes.
      // Note: Done here after ANY refresh since state changing actions may
      // take place outside of RTC and be refreshed into RTC.
      processAparStateChange( apar, aparToUpdate, initialWorkItemState,
            sportCommonData );
      // _B93726A In the case where multiple actions are employed, and each
      // executes its own retain refresh, reset the initial work item state to
      // prevent erroneous draft close attribute processing. The new state is
      // always derived from the work item built in state attribute, which is
      // in turn set from the retainStatus on a refresh.
      /*
       * initialWorkItemState = RtcUtils.getWorkflowStateName(
       * aparToUpdate.getState2(), aparToUpdate, sportCommonData );
       */
      initialWorkItemState = RtcUtils.getAttributeValueAsString(
            aparToUpdate, AparConstants.RETAIN_STATUS_ATTRIBUTE_ID,
            sportCommonData );

      // _T1083M To reuse working copy, save moved to end of Participant
      // RtcUtils.saveWorkItem( aparToUpdate, sportCommonData );
   }

   /**
    * _B93726A Process any attribute changes due to a state change. Known
    * conditions include the following.<br>
    * If Reroute New Change Team changed state to INTRAN, where residual draft
    * close attrs might have invalid data now, store and clear the draft close
    * attrs.<br>
    * If move out of INTRAN state, restore any draft close attrs that may have
    * been saved.<br>
    * Note: Done after ANY refresh since state changing actions may take place
    * outside of RTC and be refreshed into RTC.<br>
    * Also used for SBS bridged APARs where the working copy is modified in
    * SBS, including the state. The initial state is passed via additional
    * save parameters in this case.
    * 
    * @param apar - Initial work item being saved
    * @param aparToUpdate - Working copy of saved work item updates are
    *        applied to
    * @param initialState - Initial work item state
    * @param sportCommonData
    * @return indication of whether the work item is updated
    * @throws TeamRepositoryException
    */
   public static boolean processAparStateChange( IWorkItem apar,
         IWorkItem aparToUpdate, String initialState,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      boolean workItemUpdated = false;

      // If RR new CT changed the state to INTRAN,
      // where residual draft close attrs might have invalid data now,
      // store and clear the draft close attrs.
      // Note: Done here after ANY refresh since state changing actions may
      // take place outside of RTC and be refreshed into RTC.
      if (initialState != null && initialState.length() > 0)
      {
         if (stateChangedAfterUpdate( apar, initialState, sportCommonData ))
         {
            String stateAfterAction = RtcUtils.getWorkflowStateName(
                  aparToUpdate.getState2(), aparToUpdate, sportCommonData );
            if (initialState.toUpperCase().equals( "INTRAN" ))
            {
               // Changed FROM INTRAN to anything else (OPEN, etc).
               // Restore draft close attributes from any internally stored
               // attributes as these may be valid now.
               // NOTE: If action was Close, leave the draft attributes as-is.
               // Otherwise valid draft attributes may get cleared.
               // (Resulting state will be Closed.)
               if (!(stateAfterAction.toUpperCase().equals( "CLOSED" )))
               {
                  restoreAparDraftAttributes( apar, sportCommonData, true );
                  workItemUpdated = true;
               }
            }
            else
            {
               // Check if changed TO INTRAN state
               if (stateAfterAction.toUpperCase().equals( "INTRAN" ))
               {
                  // Changed TO INTRAN, draft close attributes may no longer
                  // be valid. Store and clear draft close attributes until
                  // some action, in RTC or RETAIN, changes out of INTRAN.
                  restoreAparDraftAttributes( apar, sportCommonData, false );
                  workItemUpdated = true;
               }
            }
         }
      }
      return workItemUpdated;
   }

   public static void updateAPARWorkItem( IWorkItem apar, String sbsAction,
         String[] sbsActionAttributeIds, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {

      Artifact artifact = SbsUtils.createArtifact( apar, sbsAction,
            sbsActionAttributeIds, sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      updateAparFromRetain( apar, sportCommonData );
   }

   public static void updateSubscribedPmr( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      // init
      ArrayList<IWorkItem> subList = new ArrayList<IWorkItem>();
      ArrayList<IWorkItem> unsubList = new ArrayList<IWorkItem>();

      String subQueue = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.SUBSCRIBE_QUEUE_ATTRIBUTE_ID, sportCommonData );
      subQueue = subQueue.toUpperCase( Locale.ENGLISH ); // ensure upper case
      String subCenter = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.SUBSCRIBE_CENTER_ATTRIBUTE_ID, sportCommonData );
      String subAparNum = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.APAR_NUM_ATTRIBUTE_ID, sportCommonData );
      IAttribute pmrSubQueueAttribute = null;
      IAttribute pmrSubCenterAttribute = null;
      IAttribute pmrSubAparNumAttribute = null;

      // figure out which pmrs to add and which ones to remove
      // by comparing the ipList from Retain with current item list content
      String origIpList = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.IPLIST_ATTRIBUTE_ID, sportCommonData );
      ArrayList<String> pmrNames = null;
      if (origIpList != null && origIpList.length() > 0)
      {
         String[] ipLines = origIpList.split( Constants.LINEFEED );
         // parse to get list of pmr names in the APAR IPlist from
         // RETAIN. Its what RETAIN thinks the subscribed PMRs set is.
         pmrNames = parseIpList( ipLines, sportCommonData );
      }
      // _T155546A
      // Unsubscribe needs to re-find an active PMR work item in RTC.
      // Subscribe needs to be done against an active PMR.
      // If not found, collect to report.
      // _B199954C Really only need to report subscribing archived
      // PMRs.
      ArrayList<String> subPmrNamesNotFoundinRTC = new ArrayList<String>();
      // _B199954A Can still collect unsubscribe attempts for archived
      // PMRs.
      ArrayList<String> unsubPmrNamesNotFoundinRTC = new ArrayList<String>();
      IWorkItemType pmrWIType = null;

      IAttribute subscribedPmrsAttribute = RtcUtils.findAttribute( apar,
            AparConstants.PMRSUBSCRIBERS_ATTRIBUTE_ID, sportCommonData );

      // editList is the APAR UI PMR links attribute which the user
      // alters to subscribe (add) or unsubscribe (remove). This needs
      // to be resolved to each PMR work item in the project area.
      // Unsubscribe removes original PMR work item reference
      // unfortunately.
      List<IWorkItemHandle> editlist = (List<IWorkItemHandle>)apar
            .getValue( subscribedPmrsAttribute );
      if (!editlist.isEmpty())
      {
         // listPmrs is a list of the resolved PMR work items from the
         // PMR links attribute in the UI.
         ArrayList<IWorkItem> listPmrs = new ArrayList<IWorkItem>();
         IProjectArea currentProjectArea = (IProjectArea)sportCommonData
               .getAuditableCommon().resolveAuditable(
                     sportCommonData.getProjectArea(),
                     ItemProfile.createFullProfile( IProjectArea.ITEM_TYPE ),
                     sportCommonData.getMonitor() );
         String currentProjectAreaName = currentProjectArea.getName();
         for (IWorkItemHandle pmrHandle : editlist)
         {

            IWorkItem pmr = sportCommonData.getAuditableCommon()
                  .resolveAuditable( pmrHandle, IWorkItem.FULL_PROFILE,
                        sportCommonData.getMonitor() );

            listPmrs.add( pmr ); // populate arrayList

            // check if the item in the list is a PMR
            if (!pmr.getWorkItemType()
                  .equals( PmrConstants.WORK_ITEM_TYPE_ID ))
            {
               throw new SportUserActionFailException( "Work Item "
                     + pmr.getId() + " is not a PMR, only PMRs "
                     + "can be subscribed to an APAR. Please remove "
                     + "the work item and retry the action again." );
            }
            else
            {
               // check if the item is in the current project area

               IProjectArea pmrProjectArea = (IProjectArea)sportCommonData
                     .getAuditableCommon()
                     .resolveAuditable(
                           pmr.getProjectArea(),
                           ItemProfile
                                 .createFullProfile( IProjectArea.ITEM_TYPE ),
                           sportCommonData.getMonitor() );
               String pmrProjectAreaName = pmrProjectArea.getName();
               if (!pmrProjectAreaName.equals( currentProjectAreaName ))
               {
                  throw new SportUserActionFailException( "Work Item "
                        + pmr.getId() + " is not in the " + "project area "
                        + currentProjectAreaName
                        + ". Only PMRs in this project area may be "
                        + "added as a subscribed " + "PMR to the APAR." );
               }
            }
         } // for each pmrHandle in editlist
           // if we are here, all in editlist are valid pmr work items,
           // find ones to sub/unsub by checking difference with RETAIN
           // interested party list vs altered UI attribute PMR links.
           // Again, pmrNames is what RETAIN thinks the subscribed PMRs
           // set is.
         if (pmrNames == null)
         {
            // editlist is not empty and original ipList is empty
            // all entries in work item list should be in sub list
            for (IWorkItem pmr : listPmrs)
            {
               // _T155546A Check if PMR work item is active in RETAIN.
               // Do not attempt subscribe if not active PMR.
               // Only need to get PMR type once.
               if (pmrWIType == null)
                  pmrWIType = sportCommonData.getWorkItemCommon()
                        .findWorkItemType( sportCommonData.getProjectArea(),
                              PmrConstants.WORK_ITEM_TYPE_ID,
                              sportCommonData.getMonitor() );
               // Attempt to find the active PMR in RTC.
               String nameInList = RtcUtils.getAttributeValueAsString( pmr,
                     PmrConstants.PMR_NAME_ATTRIBUTE_ID, sportCommonData );
               IWorkItem subpmr = PmrUtils.findPMRWorkItemInRTC( nameInList,
                     pmrWIType, false, sportCommonData );
               if (subpmr == null || subpmr.getId() != pmr.getId())
               {
                  // _T155546A Selected PMR is not the active PMR in
                  // RETAIN. Set up for selection error to RTC user.
                  if (!subPmrNamesNotFoundinRTC.contains( nameInList ))
                     subPmrNamesNotFoundinRTC.add( nameInList );
                  // Do not set up for subscribe action.
                  continue;
               }
               if (pmrSubAparNumAttribute == null)
               {
                  // only need to get attributes once
                  pmrSubQueueAttribute = RtcUtils.findAttribute( pmr,
                        PmrConstants.SUB_QUEUE_ATTRIBUTE_ID, sportCommonData );
                  pmrSubCenterAttribute = RtcUtils
                        .findAttribute( pmr,
                              PmrConstants.SUB_CENTER_ATTRIBUTE_ID,
                              sportCommonData );
                  pmrSubAparNumAttribute = RtcUtils.findAttribute( pmr,
                        PmrConstants.SUB_APARNUM_ATTRIBUTE_ID,
                        sportCommonData );
               }
               // set the PMR attributes needed for subscribe action
               pmr = (IWorkItem)pmr.getWorkingCopy(); // immutable false

               RtcUtils.setAttributeValue( pmr, pmrSubQueueAttribute,
                     subQueue, sportCommonData );
               RtcUtils.setAttributeValue( pmr, pmrSubCenterAttribute,
                     subCenter, sportCommonData );
               RtcUtils.setAttributeValue( pmr, pmrSubAparNumAttribute,
                     subAparNum, sportCommonData );
               // add to subscribe list
               subList.add( pmr );
            } // for listPmrs

         } // endif pmrNames == null
         else
         {
            // pmrNames is not null (current RETAIN subscribed list)
            // go thru ipList pmrNames
            // *** NOTE *** The inner list is repeated for EVERY
            // existing subscribed PMR name IPList entry. Only need
            // to save new subscribed PMRs ONCE!
            ArrayList<IWorkItem> subscribedUIPMRsProcessed = new ArrayList<IWorkItem>();
            for (String pmrName : pmrNames)
            {
               Boolean found = false;
               // Loop through PMR work items remaining in altered UI
               // (obtained from editList).
               for (IWorkItem pmr : listPmrs)
               {
                  String nameInList = RtcUtils.getAttributeValueAsString(
                        pmr, PmrConstants.PMR_NAME_ATTRIBUTE_ID,
                        sportCommonData );
                  if (pmrName.equals( nameInList ))
                  {
                     // This work item matches existing RETAIN
                     // subscribed PMRs. Already subscribed.
                     // Used to determine if unsubscribe needed.
                     found = true;
                     // Has already been subscribed and needs no
                     // further handling.
                     subscribedUIPMRsProcessed.add( pmr );
                  }
                  // add to subscribe list if nameInList is not in original
                  // ipList, and not already dealt with.
                  if (!(pmrNames.contains( nameInList ))
                        && !(subscribedUIPMRsProcessed.contains( pmr )))
                  {
                     // After processed will need no further handling.
                     // Will either be placed on not-found list or
                     // added to subscribed list (ONCE!).
                     subscribedUIPMRsProcessed.add( pmr );
                     // _T155546A Check if PMR work item is active in
                     // RETAIN. Do not attempt subscribe if not active.
                     // Only need to get PMR type once.
                     if (pmrWIType == null)
                        pmrWIType = sportCommonData.getWorkItemCommon()
                              .findWorkItemType(
                                    sportCommonData.getProjectArea(),
                                    PmrConstants.WORK_ITEM_TYPE_ID,
                                    sportCommonData.getMonitor() );
                     // Attempt to find the active PMR in RTC.
                     IWorkItem subpmr = PmrUtils.findPMRWorkItemInRTC(
                           nameInList, pmrWIType, false, sportCommonData );
                     if (subpmr == null || subpmr.getId() != pmr.getId())
                     {
                        // _T155546A Selected PMR is not the active PMR in
                        // RETAIN. Set up for selection error to RTC user.
                        if (!subPmrNamesNotFoundinRTC.contains( nameInList ))
                           subPmrNamesNotFoundinRTC.add( nameInList );
                        // Do not set up for subscribe action.
                        continue;
                     }
                     if (pmrSubAparNumAttribute == null)
                     {
                        // only need to get attributes once
                        pmrSubQueueAttribute = RtcUtils.findAttribute( pmr,
                              PmrConstants.SUB_QUEUE_ATTRIBUTE_ID,
                              sportCommonData );
                        pmrSubCenterAttribute = RtcUtils.findAttribute( pmr,
                              PmrConstants.SUB_CENTER_ATTRIBUTE_ID,
                              sportCommonData );
                        pmrSubAparNumAttribute = RtcUtils.findAttribute( pmr,
                              PmrConstants.SUB_APARNUM_ATTRIBUTE_ID,
                              sportCommonData );
                     }
                     // This work item was added to altered UI
                     // attribute, needs to be subscribed then.
                     // set the PMR attributes needed for subscribe action
                     // Get a new working copy (sandbox) PMR object.
                     pmr = (IWorkItem)pmr.getWorkingCopy(); // immutable false

                     RtcUtils.setAttributeValue( pmr, pmrSubQueueAttribute,
                           subQueue, sportCommonData );
                     RtcUtils.setAttributeValue( pmr, pmrSubCenterAttribute,
                           subCenter, sportCommonData );
                     RtcUtils.setAttributeValue( pmr, pmrSubAparNumAttribute,
                           subAparNum, sportCommonData );
                     // Store working copy to get updated by subscribe.
                     subList.add( pmr );
                  }

               } // for listPmrs
               if (!found)
               {
                  // Current IPList entry not found in edited UI list.
                  // Add to unsubscribe list if it was in original
                  // ipList but not in altered UI attribute item list.
                  // Since PMR work item was removed from UI links,
                  // need to re-find a work item to use for unsubscribe
                  // function.
                  // _B199954C There is a case where RETAIN allows an
                  // archived PMR to be subscribed with no error
                  // thrown. This would add an APAR IPList entry, with
                  // NO (active) PMR link now. So this induces an error
                  // in RTC now for an apparent attempt to unsubscribe
                  // an archived PMR. SPoRT will have to ignore any
                  // attempts to unsubscribe archived PMRs.
                  // _T155539C Need to get PMR Open Date here to add
                  // as a key to query RTC for work item.
                  // Only need to get PMR type once.
                  if (pmrWIType == null)
                     pmrWIType = sportCommonData.getWorkItemCommon()
                           .findWorkItemType(
                                 sportCommonData.getProjectArea(),
                                 PmrConstants.WORK_ITEM_TYPE_ID,
                                 sportCommonData.getMonitor() );
                  IWorkItem unsubpmr = PmrUtils.findPMRWorkItemInRTC(
                        pmrName, pmrWIType, false, sportCommonData );
                  if (unsubpmr != null)
                  {
                     // will not be put into unsublist if
                     // pmr wi does not exist
                     unsubpmr = (IWorkItem)unsubpmr.getWorkingCopy();
                     if (pmrSubAparNumAttribute == null)
                     {
                        // only need to get attributes once
                        pmrSubAparNumAttribute = RtcUtils.findAttribute(
                              unsubpmr,
                              PmrConstants.SUB_APARNUM_ATTRIBUTE_ID,
                              sportCommonData );
                     }
                     RtcUtils.setAttributeValue( unsubpmr,
                           pmrSubAparNumAttribute, subAparNum,
                           sportCommonData );
                     // set apar name in pmr
                     unsubList.add( unsubpmr );
                  }
                  else
                  {
                     // _T155546A
                     // Unsubscribe needs to re-find an active PMR work
                     // item in RTC. If not found, collect to report.
                     // Else log / warn active PMR NOT found in RTC
                     // (or collect for nuke 'em msg).
                     // _B199954C Now just save to log a warning later
                     // and proceed to be compatible with previous
                     // function.
                     if (!unsubPmrNamesNotFoundinRTC.contains( pmrName ))
                        unsubPmrNamesNotFoundinRTC.add( pmrName );
                  }
               }
            } // for pmrNames
         } // end else pmrNames not null
      }
      else
      {
         // editlist is empty, unsubscribe all pmrs
         // ie Altered UI attribute has all links removed.
         // Again, pmrNames is what RETAIN thinks the subscribed PMRs
         // set is.
         if (pmrNames != null)
         {
            for (String pmrName : pmrNames)
            {
               // Since PMR work item was removed from UI links,
               // need to re-find a work item to use for unsubscribe
               // function.
               // _T155539C Need to get PMR Open Date here to add as a
               // key to query RTC for work item.
               // Only need to get PMR type once.
               if (pmrWIType == null)
                  pmrWIType = sportCommonData.getWorkItemCommon()
                        .findWorkItemType( sportCommonData.getProjectArea(),
                              PmrConstants.WORK_ITEM_TYPE_ID,
                              sportCommonData.getMonitor() );
               IWorkItem unsubpmr = PmrUtils.findPMRWorkItemInRTC( pmrName,
                     pmrWIType, false, sportCommonData );
               if (unsubpmr != null)
               {
                  // will not be put into unsublist if pmr
                  // wi does not exist
                  unsubpmr = (IWorkItem)unsubpmr.getWorkingCopy();
                  if (pmrSubAparNumAttribute == null)
                  {
                     // only need to get attributes once
                     pmrSubAparNumAttribute = RtcUtils.findAttribute(
                           unsubpmr, PmrConstants.SUB_APARNUM_ATTRIBUTE_ID,
                           sportCommonData );
                  }
                  RtcUtils.setAttributeValue( unsubpmr,
                        pmrSubAparNumAttribute, subAparNum, sportCommonData );
                  unsubList.add( unsubpmr );
               }
               else
               {
                  // _T155546A
                  // Unsubscribe needs to re-find an active PMR work
                  // item in RTC. If not found, collect to report.
                  // Else log / warn active PMR NOT found in RTC
                  // (or collect for nuke 'em msg).
                  // _B199954C Now just save to log a warning later and
                  // proceed to be compatible with previous function.
                  if (!unsubPmrNamesNotFoundinRTC.contains( pmrName ))
                     unsubPmrNamesNotFoundinRTC.add( pmrName );
               }
            }
         }
      } // endif editlist is empty

      // finalized subList and unsubList
      // if nothing to do, throw error
      if (subList.size() == 0 && unsubList.size() == 0)
      {
         // _B199954C May have omitted all archived PMRs.
         String excpMsg = "No Subscribed PMR updates were specified.";
         if (!(subPmrNamesNotFoundinRTC.isEmpty())
               || !(unsubPmrNamesNotFoundinRTC.isEmpty()))
         {
            // _F207239C Error message usability enhancement.
            excpMsg += Constants.NEWLINE
                  + "The action against the following PMR(s) is not allowed."
                  + " Either the PMR is archived or it does not exist in RTC.";
            if (!(subPmrNamesNotFoundinRTC.isEmpty()))
            {
               excpMsg += Constants.NEWLINE + "Subscribe:";
               for (String pmrNameNotFound : subPmrNamesNotFoundinRTC)
               {
                  excpMsg += Constants.NEWLINE + " - " + pmrNameNotFound;
               }
               excpMsg += ".";
            }
            if (!(unsubPmrNamesNotFoundinRTC.isEmpty()))
            {
               excpMsg += Constants.NEWLINE + "Unsubscribe:";
               for (String pmrNameNotFound : unsubPmrNamesNotFoundinRTC)
               {
                  excpMsg += Constants.NEWLINE + " - " + pmrNameNotFound;
               }
               excpMsg += ".";
            }
         }
         throw new SportUserActionFailException( excpMsg );
      }

      // Subscribe >>>

      // _T155546A Report attempt to use archived PMRs (or where active
      // PMR work item not found in RTC).
      // _B199954M Only subscribe attempts reported.
      if (!(subPmrNamesNotFoundinRTC.isEmpty()))
      {
         String excpMsg = "The following PMRs did not have active work items"
               + " selected and could not be processed." + Constants.NEWLINE
               + "You need to select a PMR work item that is"
               + " active in RETAIN.";
         for (String pmrNameNotFound : subPmrNamesNotFoundinRTC)
         {
            excpMsg += Constants.NEWLINE + " - " + pmrNameNotFound;
         }
         excpMsg += ".";
         throw new SportUserActionFailException( excpMsg );
      }

      // throw error if subList is not empty and either queue and center
      // are not provided
      if (subList.size() > 0
            &&

            (RtcUtils.getAttributeValueAsString( apar,
                  AparConstants.SUBSCRIBE_CENTER_ATTRIBUTE_ID,
                  sportCommonData ) == null
                  || RtcUtils.getAttributeValueAsString( apar,
                        AparConstants.SUBSCRIBE_QUEUE_ATTRIBUTE_ID,
                        sportCommonData ) == null
                  || RtcUtils
                        .getAttributeValueAsString( apar,
                              AparConstants.SUBSCRIBE_CENTER_ATTRIBUTE_ID,
                              sportCommonData ).trim().length() == 0 || RtcUtils
                  .getAttributeValueAsString( apar,
                        AparConstants.SUBSCRIBE_QUEUE_ATTRIBUTE_ID,
                        sportCommonData ).trim().length() == 0))
      {
         throw new SportUserActionFailException(
               "Both a Queue and Center is required for adding "
                     + "a subscribed PMR. " );
      }

      for (IWorkItem pmrwi : subList)
      {
         // subscribe all requested

         // Do the subscribe from PMR wi, ensure wi has correct attr values
         // for subscribe request
         // String test1 = RtcUtils.getAttributeValueAsString( pmrwi,
         // PmrConstants.SUB_APARNUM_ATTRIBUTE_ID, sportCommonData );
         // String test2 = RtcUtils.getAttributeValueAsString( pmrwi,
         // PmrConstants.SUB_QUEUE_ATTRIBUTE_ID, sportCommonData );
         // String test3 = RtcUtils.getAttributeValueAsString( pmrwi,
         // PmrConstants.SUB_CENTER_ATTRIBUTE_ID, sportCommonData );
         // log.warn(test1+test2+test3);

         subscribePmrToApar( apar, pmrwi, sportCommonData );

      } // for subList

      // Unsubscribe >>>

      // _B199954A Now just log an unsubscribe warning and proceed to
      // be compatible with previous function.
      if (!(unsubPmrNamesNotFoundinRTC.isEmpty())
            && sportCommonData.getLog() != null)
      {
         String logWarnMsg = "The following archived PMRs cannot be"
               + " unsubscribed from APAR " + subAparNum;
         for (String pmrNameNotFound : unsubPmrNamesNotFoundinRTC)
         {
            logWarnMsg += Constants.NEWLINE + " - " + pmrNameNotFound;
         }
         logWarnMsg += ".";
         sportCommonData.getLog().warn( logWarnMsg );
      }

      sportCommonData.setIgnorePmr256( true ); // ignore pmrs not found in
      // Retain
      for (IWorkItem pmrwi : unsubList)
      {
         // unsubscribe all requested
         Artifact artifact = SbsUtils.createArtifact( pmrwi,
               PmrConstants.SBS_UNSUBSCRIBE_ACTION,
               PmrConstants.SBS_UNSUBSCRIBE_ACTION_ATTRIBUTE_IDS,
               sportCommonData );
         SbsUtils.sendArtifacts( artifact, sportCommonData );
      }
      sportCommonData.setIgnorePmr256( false ); // stop ignore
      // Subscribe winds up doing an APAR refresh after each action.
      // Need to refresh with any unsubscribe actions now.
      updateAparFromRetain( apar, sportCommonData );
   }

   // _T155539C Attempt to share / reuse this parsing method with PMR
   // utilities.
   public static ArrayList<String> parseIpList( String[] lines,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      ArrayList<String> pmrNames = new ArrayList<String>();

      // parsing logic extracted from SPoRTUtils.convertIPPage()
      for (String line : lines)
      {
         // parse each line to find pmrName
         String pmrName = extractPMRNameFromAPARIPlistEntry( line );
         // No more entries indicated, terminate loop.
         if (pmrName == null)
            break;
         // if (unformattedData.length() == 0) // correctly formatted
         // IPList entry contains correctly formatted PMR name -
         if (!pmrName.equals( "unknown" ))
         {
            /*
             * pmrName = addnPmrNumber + "," + addnBranchOffice + "," +
             * addnCountry;
             */
            // ignore if pmrName is a repeat
            if (!pmrNames.contains( pmrName ))
            {
               pmrNames.add( pmrName );
            } // endif pmrName is repeat
         }
      } // endfor

      return pmrNames;
   }

   // _T155539A Unique PMR by name and RETAIN create date(/time)
   // Attempt to de-clutter the parseIPList method a bit -
   /**
    * Given a single line entry from an APAR interested party attribute value,
    * extract the PMR name or "unknown". Returns null if there are no more
    * interested party entries to process. This was added to AparUtils, since
    * it is based on an APAR field interested party list value specific
    * format.
    * <p>
    * Parsing logic extracted from SPoRTUtils.convertIPPage()
    * 
    * @param iplistLine
    * @return
    */
   public static String extractPMRNameFromAPARIPlistEntry( String iplistLine )
   {
      String pmrName = "unknown";

      // parsing logic extracted from SPoRTUtils.convertIPPage()
      String addnPmrNumber = "";
      String addnBranchOffice = "";
      String addnCountry = "";
      String addnCustomerId = "";
      String addnCustomerName = "";
      String unformattedData = "";

      String customerString;
      // parse each line to find pmrName
      // parse only the CustomerId/PmrNumber/CustomerName part of line
      customerString = iplistLine.substring( 0, 30 );
      // break if there are no more records
      if (customerString.equals( "" ))
      {
         // break;
         return null; // Force an abort of IPList looping
      }
      // validate the length of customer id and pmr number
      StringTokenizer st = new StringTokenizer( customerString, "/" );
      // The customerString should be in
      // CustomerId/PmrNumber/CustomerName.
      // Sometimes we can have a blank Customer Name in which case the
      // customerString will contain only one forward slash. if it does
      // not have any forward slash, then discard the record. Also, if
      // the CustomerId and Pmr Number are not of proper length then
      // discard the record.
      if (st.countTokens() < 2)
      {
         // The data is unformatted since it has no "/" characters
         // in the customer id/ pmr number/ customer name section
         // So it needs to be stored in unformattedData
         unformattedData = customerString;
      }
      // if the 24th & 25th characters in the customerString
      // are "/ ", then it is a record created by subscribe command
      else if ((st.countTokens() == 2)
            && (customerString.substring( 23, 25 ).equals( "/ " )))
      {
         addnCustomerName = st.nextToken().trim();
         addnPmrNumber = st.nextToken().trim();
         // if pmr number is not of proper length then this
         // record belongs to unformatted category.
         if (addnPmrNumber.length() != 5)
         {
            addnPmrNumber = "";
            addnCustomerName = "";
            unformattedData = customerString;
         }
      }
      else
      {
         // this record is created in the format recommended by RETAIN
         addnCustomerId = st.nextToken();
         addnPmrNumber = st.nextToken();
         if (st.hasMoreTokens())
         {
            addnCustomerName = st.nextToken();
            addnCustomerName = addnCustomerName.trim();
         }
         if (addnCustomerId.length() != 7 || addnPmrNumber.length() != 5)
         {
            // since the customerId or pmrNumber are not of valid
            // length, this record will be in the unformatted category
            addnCustomerName = "";
            addnCustomerId = "";
            addnPmrNumber = "";
            unformattedData = customerString;
         }
      }
      addnBranchOffice = iplistLine.substring( 32, 35 );
      addnCountry = iplistLine.substring( 47, 50 );
      if (unformattedData.length() == 0) // correctly formatted
      {
         pmrName = addnPmrNumber + "," + addnBranchOffice + "," + addnCountry;
      }

      return pmrName;
   }

   public static void getClosingTextFromParent( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      // Find the parent APAR name (current APAR's sysroutedFrom field)
      String parentApar = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.SYSROUTED_FROM_ATTRIBUTE_ID, sportCommonData );

      // If the parent APAR name is blank, issue error message
      if (parentApar == null || parentApar.equals( "" ))
      {
         throw new SportUserActionFailException(
               "This is not a Sysrouted APAR or this APAR in RTC "
                     + "is out of sync with this APAR in RETAIN. If you "
                     + "believe the latter is true, then please cancel this "
                     + "action, run the \"Refresh From RETAIN\" action, and "
                     + "then retry this action." );
      }
      else
      {
         // find the parent APAR workItem
         IWorkItem parentWorkItem = findApar( parentApar, sportCommonData );
         // If the parent APAR workItem is null, issue error message
         if (parentWorkItem == null)
         {
            throw new SportUserActionFailException(
                  "The parent APAR (Sysroute From) for this APAR does not "
                        + "exist in RTC. Please pull into RTC the parent APAR from "
                        + "RETAIN and then retry the action." );
         }
         else
         {
            // find the parent APAR workItem state
            String state = RtcUtils.getAttributeValueAsString(
                  parentWorkItem, AparConstants.RETAIN_STATUS_ATTRIBUTE_ID,
                  sportCommonData );

            IWorkItem workItemToUpdate = sportCommonData
                  .getWorkItemForUpdate();

            // Set "to" and "from" field arrays.
            // TO === Always DRAFT attributes.
            // From === DRAFT when parent APAR not CLOSED,
            // RETAIN attributes when parent APAR is CLOSED.
            // (Perhaps a mapping should have been used instead.)
            String[] toFields = AparConstants.RTC_GET_CLOSING_TEXT_FROM_PARENT_DRAFT_FIELDS;
            String[] fromFields;
            // _F155269A FIXCAT, Preventative Service Program (PSP)
            // PSP Keywords / APAR Close
            // Set up for multiple attribute mapping -
            HashMap<String, String> extractedValues = new HashMap<String, String>();

            if (state.equals( "CLOSED" ))
            {
               fromFields = AparConstants.RTC_GET_CLOSING_TEXT_FROM_PARENT_RETAIN_FIELDS;
            }
            else
            {
               fromFields = AparConstants.RTC_GET_CLOSING_TEXT_FROM_PARENT_DRAFT_FIELDS;
            }

            // Populate draftClosingInformation tab and
            // Update work item
            // _F155269C FIXCAT, Preventative Service Program (PSP)
            // PSP Keywords / APAR Close
            // Attribute value collection phase -
            // May expand 1 to 1 attribute mapping to 1 to >1 mapping.
            // Works for Draft - Draft with RETAIN "placeholder" attrs.
            for (int i = 0; i < fromFields.length; i++)
            {
               // _F155269A FIXCAT, Preventative Service Program (PSP)
               // PSP Keywords / APAR Close
               // A (RETAIN) attribute with a null ID is considered a
               // placeholder to keep the arrays in sync.
               // Skip placeholder.
               if (fromFields[i] == null || fromFields[i].length() == 0)
                  continue;
               // get the value of the current field
               // Prime Map with the initial data, Draft or RETAIN.
               extractedValues.put( toFields[i], RtcUtils
                     .getAttributeValueAsString( parentWorkItem,
                           fromFields[i], sportCommonData ) );
               if (state.equals( "CLOSED" ))
               {
                  // From RETAIN attributes indicated.
                  // Parse out the value from appropriate RETAIN
                  // attributes.
                  extractRETAINCloseData( parentWorkItem, apar,
                        fromFields[i], toFields[i], extractedValues,
                        sportCommonData );
               }
            }

            // _F155269C FIXCAT, Preventative Service Program (PSP)
            // PSP Keywords / APAR Close
            // Now actually make any updates determined -
            for (String toAttrID : extractedValues.keySet())
            {
               // Go through any attributes and values collected
               // and (re)formatted.
               String value = extractedValues.get( toAttrID );
               if (value != null)
               {
                  IAttribute attribute = RtcUtils.findAttribute(
                        parentWorkItem, toAttrID, sportCommonData );

                  String attributeType = attribute.getAttributeType();

                  boolean searchOnFirstStringOnly = RtcUtils.SEARCH_ON_FIRST_STRING_ONLY_DEFAULT;

                  // if the field is an enumeration, we need to use
                  // only the first string (up to the first " ") when
                  // checking for the value in the enumeration list
                  if (RtcUtils.attributeIsEnumeration( attributeType ))
                  {
                     searchOnFirstStringOnly = RtcUtils.SEARCH_ON_FIRST_STRING_ONLY_TRUE;
                  }

                  // Update work item
                  RtcUtils.setWorkItemAttributeToValue( toAttrID, value,
                        workItemToUpdate, sportCommonData,
                        searchOnFirstStringOnly );
               }
            }
         }
      }
   }

   // _F155269A FIXCAT, Preventative Service Program (PSP)
   // PSP Keywords / APAR Close
   /**
    * Extracts data from a RETAIN Close tab attribute into a form for Draft
    * Close tab attribute use. *MOST* attributes are a 1-1 copy. However,
    * something like Comments or Problem Conclusion may contain appended data
    * from another Draft attribute, such as PSP Keyword choices (based on a
    * specific format).<br>
    * 
    * The RETAIN re-formatted data (if necessary) will be returned in the Map
    * for the initial Draft attribute ID, and any other extracted data will be
    * returned in the Map for the alternate Draft attribute ID(s). This could
    * apply to any general non 1-1 (ie, RETAIN to Draft1, Draft2, ...)
    * extraction case, as the Map can specify several attributes by ID (Map
    * key) and their values.
    * 
    * @param parentWorkItem - Parent of Sysrouted APAR, or original work item
    *        without updates to use initial RETAIN State for REOPEN.
    * @param aparWorkItem
    * @param fromRETAINAttributeID - FROM attribute.
    * @param toDRAFTAttributeID - Initial *TO* attribute, map will be primed
    *        with initial (raw, RETAIN) value. This may get re-formatted
    *        within this function.
    * @param extractedValues - INPUT: RETAIN data for intended Draft attribute
    *        (initial key). OUTPUT: Extracts input RETAIN attribute data and
    *        any other Draft attribute data extracted to Map entries. Initial
    *        entry data may get reformatted.
    * @param sportCommonData
    * 
    * @throws TeamRepositoryException
    */
   public static void extractRETAINCloseData( IWorkItem parentWorkItem,
         IWorkItem aparWorkItem, String fromRETAINAttributeID,
         String toDRAFTAttributeID, HashMap<String, String> extractedValues,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // _F155269A FIXCAT, Preventative Service Program (PSP)
      // PSP Keywords / APAR Close
      // Search string - PSP-related attributes
      String pspKeywordRETAINAttributes = AparConstants.COMMENTS_ATTRIBUTE_ID
            + AparConstants.PROBLEM_CONCLUSION_ATTRIBUTE_ID;
      // find the parent APAR workItem state (RETAIN format)
      // In the case of a REOPEN, the working copy has already been
      // updated / refreshed with RETAIN values, including the STATE.
      // Rely on the parent being the original work item.
      String retainState = RtcUtils.getAttributeValueAsString(
            parentWorkItem, AparConstants.RETAIN_STATUS_ATTRIBUTE_ID,
            sportCommonData );
      if (retainState != null
            && retainState.length() > 0
            && retainState.equals( "CLOSED" )
            && pspKeywordRETAINAttributes.indexOf( fromRETAINAttributeID ) >= 0)
      {
         // From RETAIN PSP-related attributes indicated.
         // Parse out the value from appropriate RETAIN
         // attributes.
         extractPSPKeywordData( parentWorkItem, aparWorkItem,
               fromRETAINAttributeID, toDRAFTAttributeID, extractedValues,
               sportCommonData );
      }

   }

   // _F155269A FIXCAT, Preventative Service Program (PSP)
   // PSP Keywords / APAR Close
   /**
    * Extracts data from RETAIN Close tab attributes Comments or Problem
    * Conclusion which may contain appended data from another Draft attribute,
    * such as PSP Keyword choices (based on a specific format).<br>
    * 
    * In the case of PSP Keywords, the RETAIN fields contain the form <br>
    * " KEYWORDS: kw1 kw ... up to 4 (64 character limit)"(newline?) <br>
    * That has to be extracted and copied to the draft form <br>
    * (PSP Keyword) - (Description defined in the project area customization
    * via file import -OR- "Description not found for component in project
    * area") <br>
    * The RETAIN re-formatted data (if necessary) will be returned in the Map
    * for the initial Draft attribute ID, and any extracted PSP data will be
    * returned in the Map for the alternate (PSP) draft attribute ID.
    * 
    * @param parentWorkItem
    * @param aparWorkItem
    * @param fromRETAINAttributeID - FROM attribute.
    * @param toDRAFTAttributeID - Initial *TO* attribute, map will be primed
    *        with initial (raw, RETAIN) value. This may get re-formatted
    *        within this function.
    * @param extractedValues - INPUT: RETAIN data for intended Draft attribute
    *        (initial key). OUTPUT: Extracts input RETAIN attribute data and
    *        any other Draft attribute data extracted to Map entries. Initial
    *        entry data may get reformatted.
    * @param sportCommonData
    * 
    * @throws TeamRepositoryException
    */
   public static void extractPSPKeywordData( IWorkItem parentWorkItem,
         IWorkItem aparWorkItem, String fromRETAINAttributeID,
         String toDRAFTAttributeID, HashMap<String, String> extractedValues,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // From RETAIN PSP-related attributes indicated.
      // Parse out the value from appropriate RETAIN
      // attributes.
      String parsedPSPKeywords = parsePSPKeywords( parentWorkItem,
            aparWorkItem, fromRETAINAttributeID, toDRAFTAttributeID,
            extractedValues, true, sportCommonData );
      // The values were just constructed with a simple line-feed
      // delimiter, so can also pass that along here.
      parsedPSPKeywords = obtainPSPKeywordDescriptions( parsedPSPKeywords,
            Constants.LINEFEED,
            "Description not found for component in project area",
            aparWorkItem, sportCommonData );
      // Store value in new draft attr(s) in Map.
      if (parsedPSPKeywords != null && parsedPSPKeywords.length() > 0)
      {
         // Save additional draft attribute(s) and value(s)
         // (Un)Selector attributes do not apply. Input-only.
         // Display attribute -
         extractedValues.put( AparConstants.DRAFT_PSP_KEYWORDS_ATTRIBUTE_ID,
               parsedPSPKeywords );
         // (Master) PSP choice collector attribute -
         extractedValues.put(
               AparConstants.DRAFT_PSP_KEYWORDS_EDITABLE_ATTRIBUTE_ID,
               parsedPSPKeywords );
      }
   }

   // _F155269A FIXCAT, Preventative Service Program (PSP)
   // PSP Keywords / APAR Close
   /**
    * PSP Keywords get appended to the following RETAIN attributes for these
    * specified close codes:<br>
    * PER = Problem Conclusion<br>
    * URX = Comments<br>
    * Otherwise null.<br>
    * The RETAIN fields typically only have the single keywords and each PSP
    * Keyword will be returned on a separate line for DRAFT attribute use.<br>
    * 
    * Format - (example) PROBLEM CONCLUSION: (or Comments) (...data...)
    * (blank)KEYWORDS: HYPERPAV/K ... up to 4 per line, up to 2 lines.
    * TEMPORARY FIX:
    * 
    * @param parentWorkItem
    * @param aparWorkItem
    * @param fromRETAINAttributeID - FROM attribute.
    * @param toDRAFTAttributeID - Initial *TO* attribute, map will be primed
    *        with initial (raw, RETAIN) value. This may get re-formatted
    *        within this function.
    * @param extractedValues - INPUT: RETAIN data for intended Draft attribute
    *        (initial key). OUTPUT: Extracts input RETAIN attribute data and
    *        any other Draft attribute data extracted to Map entries. Initial
    *        entry data may get reformatted.
    * @param removeFromAttr - Extracted lines to be removed from source data?
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static String parsePSPKeywords( IWorkItem parentWorkItem,
         IWorkItem aparWorkItem, String fromRETAINAttributeID,
         String toDRAFTAttributeID, HashMap<String, String> extractedValues,
         boolean removeFromAttr, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String pspKWValues = null;
      String keywordLine = " KEYWORDS:";
      // Delimiter to construct results with -
      String pspKWLineSplit = Constants.LINEFEED;
      String wordSplit = " ";
      String extractFromValue = null;
      ArrayList<String> removeLines = null;
      // 1st get the (RETAIN) close code
      String retainCloseCode = RtcUtils.getAttributeValueAsString(
            parentWorkItem, AparConstants.CLOSE_CODE_ATTRIBUTE_ID,
            sportCommonData );
      if (retainCloseCode != null
            && ((retainCloseCode.equals( "PER" ) && fromRETAINAttributeID
                  .equals( AparConstants.PROBLEM_CONCLUSION_ATTRIBUTE_ID )) || (retainCloseCode
                  .startsWith( "UR" ) && fromRETAINAttributeID
                  .equals( AparConstants.COMMENTS_ATTRIBUTE_ID ))))
      {
         // Raw value from Problem Conclusion for PER, or Comments
         // for URX,1-5 (assume preset in Map).
         extractFromValue = extractedValues.get( toDRAFTAttributeID );
      }
      // Check for PSP line starter WITH blank.
      if (extractFromValue != null && extractFromValue.trim().length() > 0
            && extractFromValue.indexOf( keywordLine ) >= 0)
      {
         // PSP keywords found, parse out -
         // Split into lines - removes newlines!!
         // Handle optional \r - (ie "(\r?\n)")
         String[] pspKWLines = extractFromValue
               .split( Constants.LINE_SPLIT_PATTERN );
         // May need to handle truncation by "split"?
         if (pspKWLines != null && pspKWLines.length > 0)
         {
            if (removeFromAttr)
               removeLines = new ArrayList<String>();
            for (String pspKWLine : pspKWLines)
            {
               // Note: May start with 1 or more spaces, however that
               // is handled here via use of "trim".
               // Also want to make sure if the value is used WITHIN
               // a line by chance, it is not recognized as a PSP
               // value. That is handled by the split at a line.
               // ie "KEYWORDS: ...etc" starts with "KEYWORDS:"
               if (pspKWLine != null && pspKWLine.trim().length() > 0
                     && pspKWLine.trim().startsWith( keywordLine.trim() ))
               {
                  // This is a PSP keyword line, collect all words after
                  // 1st indicator
                  if (removeFromAttr)
                     removeLines.add( pspKWLine );
                  pspKWLine = pspKWLine.trim();
                  // Split into words
                  String[] pspKWs = pspKWLine.trim().split( wordSplit );
                  if (pspKWs != null && pspKWs.length > 0)
                  {
                     for (String pspKW : pspKWs)
                     {
                        if (pspKW != null && pspKW.trim().length() > 0
                              && !(pspKW.trim().equals( keywordLine.trim() )))
                        {
                           // Not the 1st indicator, collect the keywords
                           pspKWValues = (pspKWValues == null) ? pspKW.trim()
                                 : pspKWValues + pspKWLineSplit
                                       + pspKW.trim();
                        }
                     }
                  }
               }
            }
         }
      }

      if (removeFromAttr && removeLines != null && !removeLines.isEmpty())
      {
         String alteredValue = extractFromValue;
         for (String removeLine : removeLines)
         {
            // Should remove line, including spaces and newlines,
            // including preceding newlines.
            // A simple line-feed pattern splits at the \n but removes
            // that from the value. (Value may still contain \r
            // carriage return inserted?)
            // Line may or may not have ended with a newline.
            // Only line or last line will not have newline.
            if (alteredValue.endsWith( removeLine ))
            {
               // Remove optional-newline + current line.
               // Handles newline from previous non-PSP line.
               alteredValue = alteredValue.replaceAll(
                     Constants.LINE_SPLIT_PATTERN + "?" + removeLine, "" );
            }
            else
            {
               // 1st or middle line -
               // Remove current line + optional-newline.
               alteredValue = alteredValue.replaceAll( removeLine
                     + Constants.LINE_SPLIT_PATTERN + "?", "" );
            }
         }
         extractedValues.put( toDRAFTAttributeID, alteredValue );
      }

      return pspKWValues;
   }

   // _F155269A FIXCAT, Preventative Service Program (PSP)
   // PSP Keywords / APAR Close
   /**
    * Attempt to look up the project area PSP Keyword definitions associated
    * with the component to grab any defined Description and return the
    * "(KeyWord) - (Description)" value. If the keyword was not defined for
    * the component, it will be left as-is or an optional default value will
    * be included. This may be called after parsePSPKeywords on RETAIN
    * attribute(s) to get the same values the DRAFT attributes would typically
    * get within RTC.
    * 
    * @param pspKWValues - Individual PSP Keywords per line.
    * @param valueSplitter
    * @param defaultPSPKWDescription - Default description if PSP Keyword not
    *        defined to component. (If null, just the keyword is returned on
    *        its own.)
    * @param aparWorkItem
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static String obtainPSPKeywordDescriptions( String pspKWValues,
         String valueSplitter, String defaultPSPKWDescription,
         IWorkItem aparWorkItem, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String resultPSPKWValue = null;
      if (pspKWValues != null && pspKWValues.trim().length() > 0)
      {
         // PSP keywords obtained as words
         String[] pspKWs = pspKWValues.trim().split( valueSplitter );
         if (pspKWs != null && pspKWs.length > 0)
         {
            List<String> componentKWs = getPSPKeywordsForComponent(
                  aparWorkItem, sportCommonData );
            for (String pspKW : pspKWs)
            {
               if (pspKW != null && pspKW.trim().length() > 0)
               {
                  String pspKWtoReturn = pspKW.trim();
                  if (componentKWs != null && !componentKWs.isEmpty())
                  {
                     // Find PSP Keyword with Description
                     for (String pspKWDescr : componentKWs)
                     {
                        // Keywords are assumed unique.
                        if (pspKWDescr.startsWith( pspKWtoReturn ))
                        {
                           pspKWtoReturn = pspKWDescr;
                           break;
                        }
                     }
                  }
                  // Convert to PSP Keyword ( - Description |
                  // [ - Default description]) entry.
                  pspKWtoReturn = (pspKWtoReturn != null
                        && pspKWtoReturn.equals( pspKW.trim() ) && (defaultPSPKWDescription != null && defaultPSPKWDescription
                        .length() > 0)) ? pspKWtoReturn + " - "
                        + defaultPSPKWDescription : pspKWtoReturn;
                  // Now add to returned values
                  resultPSPKWValue = (resultPSPKWValue == null)
                        ? pspKWtoReturn : resultPSPKWValue + valueSplitter
                              + pspKWtoReturn;
               }
            }
         }
      }
      return resultPSPKWValue;
   }

   // _F155269A FIXCAT, Preventative Service Program (PSP)
   // PSP Keywords / APAR Close
   /**
    * Compact method to grab PSP Keywords from project area configuration
    * based on APAR work item Component, reusing PSP Keyword utility.
    * 
    * @param aparWorkItem
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static List<String> getPSPKeywordsForComponent(
         IWorkItem aparWorkItem, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      List<String> configPSPKeywords = null;
      String aparComponentValue = RtcUtils.getAttributeValueAsString(
            aparWorkItem, AparConstants.COMPONENT_ATTRIBUTE_ID,
            sportCommonData );
      if (aparComponentValue == null
            || aparComponentValue.trim().length() == 0)
      {
         // No Component to get descriptions from, return values
         // as-is.
         return configPSPKeywords;
      }

      // Lets attempt to reuse utility object functions,
      // shall we?
      PSPKeywordComponentMapValueSetUtils pspKWUtil = new PSPKeywordComponentMapValueSetUtils();
      // @formatter:off;
      // attribute and configuration arguments not currently in use -
      configPSPKeywords = pspKWUtil.getPSPSelectorValueSet(
            null, // attribute, - not used
            aparWorkItem,
            sportCommonData.getWorkItemCommon(),
            null, // configuration, - not used
            aparComponentValue,
            sportCommonData,
            sportCommonData.getMonitor() // monitor
            );
      // @formatter:on;

      return configPSPKeywords;
   }

   // Update APAR with the PMR - QUEUE, CENTER, OPEN DATE, and TEXT
   public static void fetchPmrTextFromRetain( IWorkItem apar,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      // Get PMR name
      String pmrName = RtcUtils.getAttributeValueAsString( apar,
            AparConstants.PMR_NAME_ATTRIBUTE_ID, sportCommonData );

      IWorkItem workItemToUpdate = sportCommonData.getWorkItemForUpdate();

      // Create PMR Artifact
      Artifact artifact = SbsUtils.createPMRArtifact( apar,
            PmrConstants.SBS_BROWSE_ACTION,
            PmrConstants.SBS_BROWSE_ACTION_ATTRIBUTE_IDS, sportCommonData,
            pmrName );

      Artifact sbsResponse = SbsUtils.sendArtifacts( artifact,
            sportCommonData );
      ArtifactRecordPart pmrRecord = sbsResponse
            .findRecord( PmrConstants.WORK_ITEM_TYPE_ID );

      // List of PMR fields to get
      String[] pmrFieldArray = { PmrConstants.CENTER_ATTRIBUTE_ID,
            PmrConstants.QUEUE_ATTRIBUTE_ID,
            PmrConstants.OPEN_DATE_ATTRIBUTE_ID,
            PmrConstants.TEXT_ATTRIBUTE_ID };

      // List of APAR fields to update
      String[] aparFieldArray = { AparConstants.PMR_CENTER_ATTRIBUTE_ID,
            AparConstants.PMR_QUEUE_ATTRIBUTE_ID,
            AparConstants.PMR_OPEN_DATE_ATTRIBUTE_ID,
            AparConstants.PMR_TEXT_ATTRIBUTE_ID };

      for (int index = 0; index < aparFieldArray.length; index++)
      {
         String value = pmrRecord.findField( pmrFieldArray[index] )
               .getAllValuesAsString();
         if (value != null)
         {
            RtcUtils.setWorkItemAttributeToValue( aparFieldArray[index],
                  value, workItemToUpdate, sportCommonData );
         }
      }
   }

   // Method to subscribe PMRs to APARs
   public static void subscribePmrToApar( IWorkItem aparwi, IWorkItem pmrwi,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      Artifact artifact = SbsUtils.createArtifact( pmrwi,
            PmrConstants.SBS_SUBSCRIBE_ACTION,
            PmrConstants.SBS_SUBSCRIBE_ACTION_ATTRIBUTE_IDS, sportCommonData );
      SbsUtils.sendArtifacts( artifact, sportCommonData );
      sportCommonData.setIgnorePmr256( true );
      updateAparFromRetain( aparwi, sportCommonData );
      return;
   }

   /**
    * Get all of the closing information from the RETAIN Closing Information
    * tab of the given APAR work item.
    * 
    * @param apar
    * @param sportCommonData
    */
   public static Hashtable<String, String> getRETAINClosingInfo(
         IWorkItem apar, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {

      Hashtable<String, String> retainClosingInfo = new Hashtable<String, String>();
      String[] retainFields = AparConstants.RTC_RETAIN_CLOSING_FIELDS;
      for (int i = 0; i < retainFields.length; i++)
      {
         // _F155269A FIXCAT, Preventative Service Program (PSP)
         // PSP Keywords / APAR Close
         // A (RETAIN) attribute with a null ID is considered a
         // placeholder to keep the arrays in sync.
         // Skip placeholder.
         if (retainFields[i] == null || retainFields[i].length() == 0)
            continue;
         String value = RtcUtils.getAttributeValueAsString( apar,
               retainFields[i], sportCommonData );
         retainClosingInfo.put( retainFields[i], value );
      }

      return retainClosingInfo;

   }

   // _F234119A
   /**
    * Perform APAR browse. Use result artifact to populate APAR Draft Close
    * attributes from RETAIN Close attirubtes. Merge Modules/Macros and
    * Applicable Releases with data already in APAR work item Draft
    * attributes. If the APAR is NOT closed in RETAIN, throw error to
    * indicate. Do not copy empty Close values to Draft attributes.
    * 
    * @param apar
    * @param sportCommonData
    * @throws TeamRepositoryException
    * @throws SportUserActionFailException
    */
   public static void populateDraftCloseInfoFromRETAIN( IWorkItem apar,
         SportCommonData sportCommonData )
         throws TeamRepositoryException, SportUserActionFailException
   {
      // Extract result artifact fields to "retainInfo" argument for
      // shared code -
      Hashtable<String, String> retainClosingInfo = getRETAINClosingInfo(
            apar, sportCommonData );
      // _F234119A Try to share common code.
      prepareToCopyClosingInfo( apar, sportCommonData, retainClosingInfo,
            true );
   }

   /**
    * Prepare to copy the RETAIN closing information to the DRAFT Closing
    * information after merging any applicable values.
    * 
    * @param apar
    * @param sportCommonData
    * @param retainInfo
    * @throws TeamRepositoryException
    */
   public static void prepareToCopyClosingInfo( IWorkItem apar,
         SportCommonData sportCommonData, Hashtable<String, String> retainInfo )
         throws TeamRepositoryException
   {
      SbsConfigurationData configData = RtcUtils
            .getSbsConfigurationData( sportCommonData );
      boolean merge = configData.getReopenConfig().booleanValue();
      // _F234119C Try to share common code.
      prepareToCopyClosingInfo( apar, sportCommonData, retainInfo, merge );
   }

   // _F234119C
   /**
    * Prepare to copy the RETAIN closing information to the DRAFT Closing
    * information after merging any applicable values.
    * 
    * @param apar
    * @param sportCommonData
    * @param retainInfo
    * @param merge = merge relevant attribute values. Non-duplicate
    *        (blank-separated) entries. This currently includes: - Applicable
    *        Releases - Modules/Macros
    * @throws TeamRepositoryException
    */
   public static void prepareToCopyClosingInfo( IWorkItem apar,
         SportCommonData sportCommonData,
         Hashtable<String, String> retainInfo, boolean merge )
         throws TeamRepositoryException
   {
      if (merge)
      {
         // Get the applicable rels and mod/macs from the draft closing
         // information tab

         // Merge the applicable rels
         retainInfo
               .put( AparConstants.APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID,
                     RtcUtils
                           .getUniqueValues(
                                 apar,
                                 AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID,
                                 AparConstants.APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID,
                                 retainInfo
                                       .get( AparConstants.APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID ),
                                 null, sportCommonData ) );

         // Merge the mod/macs
         retainInfo.put( AparConstants.MODULES_MACROS_ATTRIBUTE_ID, RtcUtils
               .getUniqueValues( apar,
                     AparConstants.DRAFT_MODULES_MACROS_ATTRIBUTE_ID,
                     AparConstants.MODULES_MACROS_ATTRIBUTE_ID, retainInfo
                           .get( AparConstants.MODULES_MACROS_ATTRIBUTE_ID ),
                     "NONE", sportCommonData ) );

         copyClosingInfo( apar, sportCommonData, retainInfo );
         restoreAparDraftAttributes( apar, sportCommonData, false, false );

      }
      else
      {

         IAttribute draftCloseCodeAttr = RtcUtils.findAttribute( apar,
               AparConstants.DRAFT_CLOSE_CODE_ATTRIBUTE_ID, sportCommonData );
         if (draftCloseCodeAttr != null)
         {
            String draftCloseCode = RtcUtils.getAttributeValueAsString( apar,
                  draftCloseCodeAttr, sportCommonData );
            if (draftCloseCode.equals( AparConstants.UNASSIGNED ))
            {
               copyClosingInfo( apar, sportCommonData, retainInfo );
               restoreAparDraftAttributes( apar, sportCommonData, false,
                     false );
            }
         }

      }

   }

   /**
    * Copy the RETAIN closing information to the DRAFT Closing information.
    * Obtain the correct return and / or reason code based on the close code
    * set. Handle the case where the RETAIN value may result in multiple DRAFT
    * attribute(s) / value(s) extracted.
    * 
    * @param apar
    * @param sportCommonData
    * @throws TeamRepositoryException
    */
   public static void copyClosingInfo( IWorkItem apar,
         SportCommonData sportCommonData, Hashtable<String, String> retainInfo )
         throws TeamRepositoryException
   {

      // In order to correctly set the Reason Code, the close code has to be
      // set first
      copyCloseCode( apar, sportCommonData, retainInfo );

      IWorkItem aparToUpdate = sportCommonData.getWorkItemForUpdate();

      // _F155269A FIXCAT, Preventative Service Program (PSP)
      // PSP Keywords / APAR Close
      // Set up for multiple attribute mapping -
      HashMap<String, String> extractedValues = new HashMap<String, String>();
      // _F155269C FIXCAT, Preventative Service Program (PSP)
      // PSP Keywords / APAR Close
      // (PSP keywords may come from 2 RETAIN attrs.)
      // In the following Hashtable, the keys are the draft attributes,
      // and the values are the corresponding RETAIN attributes.
      // This Map handles 1-1 RETAIN --- DRAFT attributes.
      // Need to extract 1 - >1 RETAIN --- DRAFT attribute(s), where
      // applicable. (Reuse Get Closing Text From Parent, Sysroute
      // related function.)
      // Draft attribute(s) / value(s) collection phase -
      // The result is ALL of the DRAFT attributes that need to be set
      // and their raw RETAIN values stored in the extraction Map.
      for (String draftAttr : AparConstants.SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS
            .keySet())
      {
         // Get the corresponding (1-1) RETAIN attribute value
         String retainAttrName = AparConstants.SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS
               .get( draftAttr );
         String retainAttrValue = retainInfo.get( retainAttrName );
         // _F155269A FIXCAT, Preventative Service Program (PSP)
         // PSP Keywords / APAR Close
         // (PSP keywords may come from 2 RETAIN attributes.)
         // Get the value of the current field.
         // Prime Map with the initial (raw) RETAIN data.
         extractedValues.put( draftAttr, retainAttrValue );
         // Reuse common method, "parent" work item is the current
         // APAR in its inital state (no updates) to get initial
         // RETAIN STATE. Extracts to as many DRAFT attributes as
         // necessary. Processes "raw" RETAIN data, which may later
         // be further formatted.
         extractRETAINCloseData( apar, aparToUpdate, retainAttrName,
               draftAttr, extractedValues, sportCommonData );
      }

      // _F155269C FIXCAT, Preventative Service Program (PSP)
      // PSP Keywords / APAR Close
      // (PSP keywords may come from 2 RETAIN attrs.)
      // In the following Hashtable, the keys are the draft attributes,
      // and the values are the corresponding raw RETAIN VALUES
      // gathered.
      // This Map handles 1 - >1 RETAIN --- Draft attributes.
      // Draft attribute(s) / value(s) processing phase -
      for (String draftAttr : extractedValues.keySet())
      {
         // Get the DRAFT attribute
         IAttribute draftAttribute = RtcUtils.findAttribute( aparToUpdate,
               draftAttr, sportCommonData );

         if (draftAttribute != null)
         {
            String retainAttrValue = extractedValues.get( draftAttr );
            // Special handling for close code, reason code, and return codes
            if (draftAttr
                  .equals( AparConstants.DRAFT_CLOSE_CODE_ATTRIBUTE_ID )
                  || draftAttr
                        .equals( AparConstants.DRAFT_REASON_CODE_ATTRIBUTE_ID )
                  || draftAttr
                        .equals( AparConstants.DRAFT_RETURN_CODES_ATTRIBUTE_ID ))
            {
               boolean searchOnFirst = true;
               if (draftAttr
                     .equals( AparConstants.DRAFT_CLOSE_CODE_ATTRIBUTE_ID )
                     && retainAttrValue.startsWith( "UR" ))
                  retainAttrValue = "URX";
               if (draftAttr
                     .equals( AparConstants.DRAFT_REASON_CODE_ATTRIBUTE_ID ))
               {
                  Object[] validValues = draftAttribute.getValueSet(
                        sportCommonData.getAuditableCommon(), aparToUpdate,
                        sportCommonData.getMonitor() );
                  for (int i = 0; i < validValues.length; i++)
                  {
                     Identifier<? extends ILiteral> identifier = (Identifier<? extends ILiteral>)(validValues[i]);
                     IEnumeration<?> enumeration = sportCommonData
                           .getWorkItemCommon()
                           .resolveEnumeration( draftAttribute,
                                 sportCommonData.getMonitor() );
                     ILiteral literal = enumeration
                           .findEnumerationLiteral( (Identifier<? extends ILiteral>)identifier );
                     String validValue = literal.getName();
                     if (validValue.startsWith( retainAttrValue ))
                     {
                        retainAttrValue = validValue;
                        searchOnFirst = false;
                        break;
                     }
                  }
               }
               RtcUtils.setEnumerationAttributeValue( aparToUpdate,
                     draftAttribute, retainAttrValue, sportCommonData,
                     searchOnFirst );
            }
            else
            {
               // Set draft attribute to value of RETAIN attribute
               RtcUtils.setAttributeValue( aparToUpdate, draftAttribute,
                     retainAttrValue, sportCommonData );
            }
         }
      }
   }

   /**
    * Copy the RETAIN closing code to the DRAFT Closing information
    * 
    * @param apar
    * @param sportCommonData
    * @throws TeamRepositoryException
    */
   public static void copyCloseCode( IWorkItem apar,
         SportCommonData sportCommonData, Hashtable<String, String> retainInfo )
         throws TeamRepositoryException
   {

      IWorkItem aparToUpdate = sportCommonData.getWorkItemForUpdate();
      String draftAttr = AparConstants.DRAFT_CLOSE_CODE_ATTRIBUTE_ID;
      // Get the corresponding RETAIN attribute value

      String retainAttrName = AparConstants.SBS_DRAFT_RETAIN_CLOSE_ATTRIBUTE_IDS
            .get( draftAttr );
      String retainAttrValue = retainInfo.get( retainAttrName );

      // Get the DRAFT attribute
      IAttribute draftAttribute = RtcUtils.findAttribute( aparToUpdate,
            draftAttr, sportCommonData );

      if (draftAttribute != null)
      {

         // _B238449C
         if (draftAttr.equals( AparConstants.DRAFT_CLOSE_CODE_ATTRIBUTE_ID ))
            retainAttrValue = translateCloseCode( retainAttrValue );

         RtcUtils.setEnumerationAttributeValue( aparToUpdate, draftAttribute,
               retainAttrValue, sportCommonData, true );
      }
   }

   // _B238449A
   /**
    * Translate different RETAIN close codes to known RTC draft enumeration
    * values. <br>
    * UR{1-5} -> <b>URX</b>. <br>
    * {DUA, DUB, DUU} -> <b>DUP</b>.
    * 
    * @param retainCloseCode
    * 
    * @return translated RTC draft enumeration value, or original value.
    */
   public static String translateCloseCode( String retainCloseCode )
   {
      // Default = input value
      String translatedCloseCode = retainCloseCode;
      if (translatedCloseCode != null)
      {
         // UR{1-5} -> URX.
         if (translatedCloseCode.startsWith( "UR" ))
            translatedCloseCode = "URX";
         // {DUA, DUB, DUU} -> DUP.
         else if (translatedCloseCode.startsWith( "DU" ))
            translatedCloseCode = "DUP";
         // xxx -> yyy.
         // else if (translatedCloseCode.equalsIgnoreCase( "" ))
         // translatedCloseCode = "";
      }
      return translatedCloseCode;
   }
}