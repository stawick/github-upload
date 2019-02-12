package com.ibm.sport.rtc.common;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import com.ibm.ArtifactTechnology.ABS.ArtifactBridge.ArtifactBridgeInterface;
import com.ibm.ArtifactTechnology.ABS.common.net.ABSComponentProxyUtil;
import com.ibm.ArtifactTechnology.ABS.common.net.ABSComponentRequest;
import com.ibm.ArtifactTechnology.ABS.common.net.ABSComponentResponse;
import com.ibm.ArtifactTechnology.common.Artifact;
import com.ibm.ArtifactTechnology.common.ArtifactProvider;
import com.ibm.ArtifactTechnology.common.ArtifactRecordField;
import com.ibm.ArtifactTechnology.common.ArtifactRecordPart;
import com.ibm.ArtifactTechnology.common.ArtifactResponse;
import com.ibm.ArtifactTechnology.common.Artifacts;
import com.ibm.ArtifactTechnology.common.Constants;
import com.ibm.ArtifactTechnology.common.XMLEncodingType;
import com.ibm.ArtifactTechnology.common.security.AuthenticationRequestArtifactElement;
import com.ibm.ArtifactTechnology.common.security.SessionIdentifierPropertyElement;
import com.ibm.sport.rtc.common.process.config.ConditionsElementData.ActionInitiator;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

public class SbsUtils
{
   public static final String AUTH_USERNAME_FIELD = new String(
         "abs.auth.username" );

   public static final String PROVIDER_KEY_PROPERTY = new String(
         "abs.provider.key" );

   public static final String SPECIAL_FIELDS_RECORD = new String(
         "abs.record.special_fields" );

   public static final String STATUSMESSAGE_FIELD = new String(
         "statusMessage" );

   // _F80509A Set defaults in artifact record for RETAIN action
   // Map <action>_<attribute> to <default value>
   public static final String actionAttrKeySeparator = "_";
   public static final HashMap<String, String> unspecifiedAttrDefaultMap = new HashMap<String, String>();
   static
   {
      // Initialize the default values for any action/attr pairs.
      // Make sure the value is valid input for RETAIN (upper case, etc).
      // No further processing is done, other than XML encoding set.
      // _F80509A Default Modules/Macros to NONE
      // Not required for FIN close code, just optional, so no default.
      /*
       * unspecifiedAttrDefaultMap.put( AparConstants.SBS_CLOSE_FIN_ACTION +
       * actionAttrKeySeparator +
       * AparConstants.DRAFT_MODULES_MACROS_ATTRIBUTE_ID, "NONE" );
       */
      unspecifiedAttrDefaultMap
            .put( AparConstants.SBS_CLOSE_OEM_ACTION + actionAttrKeySeparator
                  + AparConstants.DRAFT_MODULES_MACROS_ATTRIBUTE_ID, "NONE" );
      unspecifiedAttrDefaultMap
            .put( AparConstants.SBS_CLOSE_PER_ACTION + actionAttrKeySeparator
                  + AparConstants.DRAFT_MODULES_MACROS_ATTRIBUTE_ID, "NONE" );
      unspecifiedAttrDefaultMap
            .put( AparConstants.SBS_CLOSE_PRS_ACTION + actionAttrKeySeparator
                  + AparConstants.DRAFT_MODULES_MACROS_ATTRIBUTE_ID, "NONE" );
      // Modules/Macros only required for UR1 / UR2 code, not all URX
      unspecifiedAttrDefaultMap
            .put( AparConstants.SBS_CLOSE_UR12_ACTION + actionAttrKeySeparator
                  + AparConstants.DRAFT_MODULES_MACROS_ATTRIBUTE_ID, "NONE" );
      // UpdateClosingText based on close code set - OEM, PER, PRS, UR1, UR2
      unspecifiedAttrDefaultMap
            .put( AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_ACTION + "OEM"
                  + actionAttrKeySeparator
                  + AparConstants.DRAFT_MODULES_MACROS_ATTRIBUTE_ID, "NONE" );
      unspecifiedAttrDefaultMap
            .put( AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_ACTION + "PER"
                  + actionAttrKeySeparator
                  + AparConstants.DRAFT_MODULES_MACROS_ATTRIBUTE_ID, "NONE" );
      unspecifiedAttrDefaultMap
            .put( AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_ACTION + "PRS"
                  + actionAttrKeySeparator
                  + AparConstants.DRAFT_MODULES_MACROS_ATTRIBUTE_ID, "NONE" );
      unspecifiedAttrDefaultMap
            .put( AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_ACTION + "UR12"
                  + actionAttrKeySeparator
                  + AparConstants.DRAFT_MODULES_MACROS_ATTRIBUTE_ID, "NONE" );
      unspecifiedAttrDefaultMap
            .put( AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_ACTION + "UR1"
                  + actionAttrKeySeparator
                  + AparConstants.DRAFT_MODULES_MACROS_ATTRIBUTE_ID, "NONE" );
      unspecifiedAttrDefaultMap
            .put( AparConstants.SBS_UPDATECLOSINGTEXT_FOLLOWUP_ACTION + "UR2"
                  + actionAttrKeySeparator
                  + AparConstants.DRAFT_MODULES_MACROS_ATTRIBUTE_ID, "NONE" );
      // _F80509A PTF Close COR or PER, Modules/Macros defaults to NONE.
      unspecifiedAttrDefaultMap.put(
            PtfConstants.SBS_CLOSE_CORPER_ACTION + actionAttrKeySeparator
                  + PtfConstants.DRAFT_MODULES_MACROS_LIST_ATTRIBUTE_ID,
            "NONE" );
   }

   public static void checkForModfyAndChangeStateAction( String[] actions,
         String modifyActionAttributeId, SportCommonData sportCommonData )
   {
      boolean changeStateActionFound = false;
      boolean modifyActionFound = false;
      for (String action : actions)
      {
         String[] actionParts = action.split( "/" );
         if ((actionParts.length == 2) && actionParts[0].equals( "action" ))
            changeStateActionFound = true;
         else if ((actionParts.length == 2)
               && actionParts[0].equals( "modify" )
               && actionParts[1].equals( modifyActionAttributeId ))
            modifyActionFound = true;
      }
      if (changeStateActionFound && modifyActionFound)
      {
         String message = "Both a change state action and a modify action were encountered";
         throw new SportRuntimeException( message );
      }
   }

   public static Artifact createArtifact( IWorkItem workItem,
         String actionName, String[] reqAttributeIds,
         String[] optAttributeIdsNoBlanks,
         String[] optAttributeIdsBlanksAllowed,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      List<String> allAttrs = new ArrayList<String>();

      // Initialize to list of required attributes
      for (String reqAttr : reqAttributeIds)
      {
         allAttrs.add( reqAttr );
      }

      // Add optional attributes that are not null or ""
      if (optAttributeIdsNoBlanks != null)
      {
         for (String optAttr : optAttributeIdsNoBlanks)
         {
            String attrValue = RtcUtils.getAttributeValueAsString( workItem,
                  optAttr, sportCommonData );
            if (attrValue != null && attrValue.trim().length() > 0)
               allAttrs.add( optAttr );
         }
      }

      if (optAttributeIdsBlanksAllowed != null)
      {
         // Add optional attributes that are not null but can be ""
         for (String optAttr : optAttributeIdsBlanksAllowed)
         {
            String attrValue = RtcUtils.getAttributeValueAsString( workItem,
                  optAttr, sportCommonData );
            if (attrValue != null)
               allAttrs.add( optAttr );
         }
      }

      // Convert to array
      String[] allAttrsArray = new String[allAttrs.size()];
      allAttrsArray = allAttrs.toArray( allAttrsArray );

      return createArtifact( workItem, actionName, allAttrsArray,
            sportCommonData );
   }

   public static Artifact createArtifact( IWorkItem workItem,
         String actionName, String[] attributeIds,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String recordName = workItem.getWorkItemType();
      if (recordName.equals( AparConstants.WORK_ITEM_TYPE_ID ))
      {
         return createAPARArtifact( workItem, actionName, attributeIds,
               sportCommonData );
      }
      else if (recordName.equals( PtfConstants.WORK_ITEM_TYPE_ID ))
      {
         return createPTFArtifact( workItem, actionName, attributeIds,
               sportCommonData );
      }
      else if (recordName.equals( PmrConstants.WORK_ITEM_TYPE_ID ))
      {
         return createPMRArtifact( workItem, actionName, attributeIds,
               sportCommonData );
      }

      ArtifactRecordPart record = createRecord( recordName, workItem,
            attributeIds, sportCommonData );
      Artifact artifact = new Artifact();
      artifact.addAction( actionName );
      artifact.setArtifactClass( recordName );
      artifact.setArtifactID( Integer.toString( workItem.getId() ) );
      artifact.setProvider( createProvider( sportCommonData ) );
      artifact.addRecord( record );
      return artifact;
   }

   public static Artifact createAPARArtifact( IWorkItem workItem,
         String actionName, String[] attributeIds,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String recordName = workItem.getWorkItemType();
      ArtifactRecordPart record = createRecord( recordName, workItem,
            attributeIds, sportCommonData );
      Artifact artifact = new Artifact();
      artifact.addAction( actionName );
      artifact.setArtifactClass( AparConstants.APAR_CLASS );
      artifact.setArtifactID( Integer.toString( workItem.getId() ) );
      IAttribute componentAttr = RtcUtils.findAttribute( workItem,
            AparConstants.COMPONENT_ATTRIBUTE_ID, sportCommonData );
      String componentVal = RtcUtils.getAttributeValueAsString( workItem,
            componentAttr, sportCommonData );
      IAttribute changeTeamAttr = RtcUtils.findAttribute( workItem,
            AparConstants.CURRENT_CHANGE_TEAM_ATTRIBUTE_ID, sportCommonData );
      String changeTeam = RtcUtils.getAttributeValueAsString( workItem,
            changeTeamAttr, sportCommonData );
      artifact.setProvider(
            createProvider( sportCommonData, componentVal, changeTeam ) );
      artifact.addRecord( record );
      return artifact;
   }

   public static Artifact createPTFArtifact( IWorkItem workItem,
         String actionName, String[] attributeIds,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      String recordName = workItem.getWorkItemType();
      ArtifactRecordPart record = createRecord( recordName, workItem,
            attributeIds, sportCommonData );
      Artifact artifact = new Artifact();
      artifact.addAction( actionName );
      artifact.setArtifactClass( PtfConstants.PTF_CLASS );
      artifact.setArtifactID( Integer.toString( workItem.getId() ) );
      IAttribute componentAttr = RtcUtils.findAttribute( workItem,
            PtfConstants.REPORTED_COMP_ATTRIBUTE_ID, sportCommonData );
      String componentVal = RtcUtils.getAttributeValueAsString( workItem,
            componentAttr, sportCommonData );
      IAttribute changeTeamAttr = RtcUtils.findAttribute( workItem,
            PtfConstants.CURRENT_CHANGE_TEAM_ATTRIBUTE_ID, sportCommonData );
      String changeTeam = RtcUtils.getAttributeValueAsString( workItem,
            changeTeamAttr, sportCommonData );
      artifact.setProvider(
            createProvider( sportCommonData, componentVal, changeTeam ) );
      artifact.addRecord( record );
      return artifact;
   }

   // Create a PMR Artifact
   public static Artifact createPMRArtifact( IWorkItem workItem,
         String actionName, String[] attributeIds,
         SportCommonData sportCommonData )
         throws TeamRepositoryException, SportUserActionFailException
   {
      String componentVal = "";
      String recordName = workItem.getWorkItemType();
      ArtifactRecordPart record = createRecord( recordName, workItem,
            attributeIds, sportCommonData );
      Artifact artifact = new Artifact();
      artifact.addAction( actionName );
      artifact.setArtifactClass( PmrConstants.PMR_CLASS );
      artifact.setArtifactID( Integer.toString( workItem.getId() ) );
      IAttribute componentAttr = RtcUtils.findAttribute( workItem,
            PmrConstants.COMPONENT_ID_ATTRIBUTE_ID, sportCommonData );
      if (componentAttr != null)
         componentVal = RtcUtils.getAttributeValueAsString( workItem,
               componentAttr, sportCommonData );
      artifact.setProvider(
            createProvider( sportCommonData, componentVal, "" ) );
      artifact.addRecord( record );
      return artifact;

   }

   // Create a PMR Artifact - Used for creating a PMR Artifact from an APAR
   // workItem.
   public static Artifact createPMRArtifact( IWorkItem workItem,
         String actionName, String[] attributeIds,
         SportCommonData sportCommonData, String pmrName )
         throws TeamRepositoryException
   {
      String componentVal = "";
      String recordName = PmrConstants.WORK_ITEM_TYPE_ID;
      ArtifactRecordPart record = createPMRRecord( recordName, workItem,
            attributeIds, pmrName, sportCommonData );
      Artifact artifact = new Artifact();
      artifact.addAction( actionName );
      artifact.setArtifactClass( PmrConstants.PMR_CLASS );
      artifact.setArtifactID( Integer.toString( workItem.getId() ) );
      IAttribute componentAttr = RtcUtils.findAttribute( workItem,
            PmrConstants.COMPONENT_ID_ATTRIBUTE_ID, sportCommonData );
      if (componentAttr != null)
         componentVal = RtcUtils.getAttributeValueAsString( workItem,
               componentAttr, sportCommonData );
      artifact.setProvider(
            createProvider( sportCommonData, componentVal, "" ) );
      artifact.addRecord( record );

      return artifact;
   }

   public static ArtifactProvider createProvider(
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      SbsConfigurationData configData = RtcUtils
            .getSbsConfigurationData( sportCommonData );
      String sbsProviderName = configData.getSbsProviderNameNonNull();
      String sbsProviderClass = configData.getSbsProviderClassNonNull();
      String sbsProviderType = configData.getSbsProviderTypeNonNull();
      String userId = RtcUtils.getUser( sportCommonData ).getUserId();
      ArtifactProvider provider = new ArtifactProvider();
      provider.setName( sbsProviderName );
      provider.setTypeName( sbsProviderType );
      provider.setClassName( sbsProviderClass );
      provider.setUserName( userId );
      return provider;
   }

   public static ArtifactProvider createProvider(
         SportCommonData sportCommonData, String component,
         String changeTeam )
         throws TeamRepositoryException
   {
      SbsConfigurationData configData = RtcUtils
            .getSbsConfigurationData( sportCommonData );
      String sbsProviderName = configData.getSbsProviderNameNonNull();
      String sbsProviderClass = configData.getSbsProviderClassNonNull();
      String sbsProviderType = configData.getSbsProviderTypeNonNull();
      String userId = RtcUtils.getUser( sportCommonData ).getUserId();
      ArtifactProvider provider = new ArtifactProvider();
      provider.setName( sbsProviderName );
      provider.setTypeName( sbsProviderType );
      provider.setClassName( sbsProviderClass );
      provider.setUserName( userId );

      if (component == null)
         component = "";
      if (changeTeam == null)
         changeTeam = "";

      // Update the provider user based on the component
      Hashtable<String, String[]> ht = configData.getRtcIdsToCompCTs();
      boolean useMapping = false;
      if ((ht != null) && !ht.isEmpty())
         useMapping = true;
      if (useMapping)
      {
         Hashtable<String, String> newht = new Hashtable<String, String>();
         Set<String> keys = ht.keySet();
         if (keys != null)
         {
            for (String id : keys)
            {
               String[] values = ht.get( id );
               StringTokenizer st = new StringTokenizer( values[0], "," );
               while (st.hasMoreTokens())
                  newht.put( st.nextToken().trim(), id );
               st = new StringTokenizer( values[1], "," );
               while (st.hasMoreTokens())
                  newht.put( st.nextToken().trim(), id );
            }
         }

         // First, try to set the user based on the component. If the
         // component is not found, then look for change
         // team. If change team isn't found, then check if a default is
         // defined.
         if (newht.containsKey( component.toUpperCase().trim() ))
            provider
                  .setUserName( newht.get( component.toUpperCase().trim() ) );
         else if (newht.containsKey( changeTeam.toUpperCase().trim() ))
            provider.setUserName(
                  newht.get( changeTeam.toUpperCase().trim() ) );
         else if (newht.containsKey( "*" ))
            provider.setUserName( newht.get( "*" ) );
      }

      return provider;
   }

   public static ArtifactRecordPart createPMRRecord( String recordName,
         IWorkItem workItem, String[] attributeIds, String pmrName,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      ArtifactRecordPart record = new ArtifactRecordPart();
      record.setName( recordName );
      ArrayList<String> pmrFields = null;

      if (pmrName != null)
      {
         pmrFields = PmrUtils.splitPmrName( pmrName );
      }

      for (String attributeId : attributeIds)
      {
         String fieldValue = null;
         if (attributeId.equals( PmrConstants.PMR_NO_ATTRIBUTE_ID ))
         {
            fieldValue = pmrFields.get( 0 );
         }
         else if (attributeId.equals( PmrConstants.BRANCH_ATTRIBUTE_ID ))
         {
            fieldValue = pmrFields.get( 1 );
         }
         else if (attributeId.equals( PmrConstants.COUNTRY_ATTRIBUTE_ID ))
         {
            fieldValue = pmrFields.get( 2 );
         }
         else
         {
            fieldValue = RtcUtils.getAttributeValueAsString( workItem,
                  attributeId, sportCommonData );
         }
         record.addField( attributeId, fieldValue, XMLEncodingType.XML );
      }

      return record;
   }

   public static ArtifactRecordPart createRecord( String recordName,
         IWorkItem workItem, String[] attributeIds,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      ArtifactRecordPart record = new ArtifactRecordPart();
      record.setName( recordName );
      for (String attributeId : attributeIds)
      {
         String fieldValue = null;
         if (attributeId
               .equals( AparConstants.TO_APARCANCELFLAG_ATTRIBUTE_ID ))
         {
            fieldValue = "true"; // constant, not in process template
         }
         else
         {
            fieldValue = RtcUtils.getAttributeValueAsString( workItem,
                  attributeId, sportCommonData );
            if (fieldValue != null)
            {
               // flip to upper case for RETAIN
               if (attributeId.equals( AparConstants.COMPONENT_ATTRIBUTE_ID )
                     || attributeId
                           .equals( AparConstants.RELEASE_ATTRIBUTE_ID )
                     || attributeId.equals(
                           AparConstants.CURRENT_CHANGE_TEAM_ATTRIBUTE_ID )
                     || attributeId.equals(
                           AparConstants.SYSTEM_RELEASE_LEVEL_ATTRIBUTE_ID )
                     || attributeId.equals(
                           AparConstants.CUSTOMER_NUMBER_ATTRIBUTE_ID )
                     || attributeId
                           .equals( AparConstants.TO_COMPONENT_ATTRIBUTE_ID )
                     || attributeId
                           .equals( AparConstants.TO_RELEASE_ATTRIBUTE_ID )
                     || attributeId.equals(
                           AparConstants.TO_SCP_RELEASE_ATTRIBUTE_ID )
                     || attributeId.equals(
                           AparConstants.TO_CHANGE_TEAM_ATTRIBUTE_ID )
                     || attributeId
                           .equals( PmrConstants.SUB_QUEUE_ATTRIBUTE_ID )
                     || attributeId
                           .equals( PmrConstants.SUB_CENTER_ATTRIBUTE_ID )
                     || attributeId
                           .equals( AparConstants.PMR_NAME_ATTRIBUTE_ID )
                     || attributeId.equals(
                           PtfConstants.TRANSFER_ORDER_TO_ATTRIBUTE_ID )
                     || attributeId.equals(
                           PtfConstants.CURRENT_CHANGE_TEAM_ATTRIBUTE_ID )
                     // _B128651A When alphanumeric releases are used as
                     // as input on APAR Close, RETAIN requires upper case
                     // for matching (reported) release.
                     || attributeId.equals(
                           AparConstants.DRAFT_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID )
                     || attributeId.equals(
                           AparConstants.DRAFT_FAILING_LVL_ATTRIBUTE_ID )
                     || attributeId.equals(
                           AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID )
                     || attributeId
                           .equals( AparConstants.DRAFT_TREL_ATTRIBUTE_ID ))
               {
                  fieldValue = fieldValue.toUpperCase( Locale.ENGLISH );
               }
            }
         }
         // _B233387C Cleanse transmission of undesired values to
         // RETAIN.
         record.addField( attributeId, RtcUtils.scrubChars( fieldValue ),
               XMLEncodingType.XML );
      } // end for
      return record;
   }

   // _T155539A Unique PMR by name and RETAIN create date(/time)
   /**
    * Construct an artifact when there is no work item available to derive
    * elements from. Everything is explicitly passed. Fields and their String
    * values are passed as a map.
    * 
    * @param recordName
    * @param actionName
    * @param recordFields
    * @param artifactClassName
    * @param artifactID
    * @param sportCommonData
    * @return
    * @throws SportUserActionFailException
    * @throws TeamRepositoryException
    */
   public static Artifact createArtifactFromScratch( String recordName,
         String actionName, HashMap<String, String> recordFields,
         String artifactClassName, String artifactID,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      ArtifactRecordPart record = createRecordFromScratch( recordName,
            recordFields, sportCommonData );
      Artifact artifact = new Artifact();
      artifact.addAction( actionName );
      artifact.setArtifactClass( artifactClassName );
      // Set provider based on record name.
      if (recordName.equals( AparConstants.WORK_ITEM_TYPE_ID ))
      {
         // Assume these fields are passed
         artifact.setProvider( createProvider( sportCommonData,
               recordFields.get( AparConstants.COMPONENT_ATTRIBUTE_ID ),
               recordFields.get(
                     AparConstants.CURRENT_CHANGE_TEAM_ATTRIBUTE_ID ) ) );
      }
      else if (recordName.equals( PtfConstants.WORK_ITEM_TYPE_ID ))
      {
         // Assume these fields are passed
         artifact.setProvider( createProvider( sportCommonData,
               recordFields.get( PtfConstants.REPORTED_COMP_ATTRIBUTE_ID ),
               recordFields.get(
                     PtfConstants.CURRENT_CHANGE_TEAM_ATTRIBUTE_ID ) ) );
      }
      /*
       * else if (recordName.equals( PmrConstants.WORK_ITEM_TYPE_ID ))
       * artifact.setArtifactClass( PmrConstants.PMR_CLASS );
       */
      else
         artifact.setProvider( createProvider( sportCommonData ) );

      // artifact.setArtifactID( Integer.toString( workItem.getId() ) );
      if (artifactID != null && artifactID.length() > 0)
         artifact.setArtifactID( artifactID );
      else
         artifact.setArtifactID(
               "SPoRT RTC internal " + recordName + " " + actionName );
      artifact.addRecord( record );
      return artifact;
   }

   // _T155539A Unique PMR by name and RETAIN create date(/time)
   /**
    * Construct an artifact record when there is no work item available to
    * derive elements from. Everything is explicitly passed. Fields and their
    * values are passed as a map.
    * 
    * @param recordName
    * @param recordFields
    * @param sportCommonData
    * @return
    * @throws SportUserActionFailException
    * @throws TeamRepositoryException
    */
   public static ArtifactRecordPart createRecordFromScratch(
         String recordName, HashMap<String, String> recordFields,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      ArtifactRecordPart record = new ArtifactRecordPart();
      record.setName( recordName );
      for (String attributeId : recordFields.keySet())
      {
         String fieldValue = recordFields.get( attributeId );
         if (attributeId
               .equals( AparConstants.TO_APARCANCELFLAG_ATTRIBUTE_ID ))
         {
            fieldValue = "true"; // constant, not in process template
         }
         else
         {
            /*
             * fieldValue = RtcUtils.getAttributeValueAsString( workItem,
             * attributeId, sportCommonData );
             */
            if (fieldValue != null)
            {
               // flip to upper case for RETAIN
               if (attributeId.equals( AparConstants.COMPONENT_ATTRIBUTE_ID )
                     || attributeId
                           .equals( AparConstants.RELEASE_ATTRIBUTE_ID )
                     || attributeId.equals(
                           AparConstants.CURRENT_CHANGE_TEAM_ATTRIBUTE_ID )
                     || attributeId.equals(
                           AparConstants.SYSTEM_RELEASE_LEVEL_ATTRIBUTE_ID )
                     || attributeId.equals(
                           AparConstants.CUSTOMER_NUMBER_ATTRIBUTE_ID )
                     || attributeId
                           .equals( AparConstants.TO_COMPONENT_ATTRIBUTE_ID )
                     || attributeId
                           .equals( AparConstants.TO_RELEASE_ATTRIBUTE_ID )
                     || attributeId.equals(
                           AparConstants.TO_SCP_RELEASE_ATTRIBUTE_ID )
                     || attributeId.equals(
                           AparConstants.TO_CHANGE_TEAM_ATTRIBUTE_ID )
                     || attributeId
                           .equals( PmrConstants.SUB_QUEUE_ATTRIBUTE_ID )
                     || attributeId
                           .equals( PmrConstants.SUB_CENTER_ATTRIBUTE_ID )
                     || attributeId
                           .equals( AparConstants.PMR_NAME_ATTRIBUTE_ID )
                     || attributeId.equals(
                           PtfConstants.TRANSFER_ORDER_TO_ATTRIBUTE_ID )
                     || attributeId.equals(
                           PtfConstants.CURRENT_CHANGE_TEAM_ATTRIBUTE_ID )
                     // _B128651A When alphanumeric releases are used as
                     // as input on APAR Close, RETAIN requires upper case
                     // for matching (reported) release.
                     || attributeId.equals(
                           AparConstants.DRAFT_COMMITTED_FIX_RELEASE_ATTRIBUTE_ID )
                     || attributeId.equals(
                           AparConstants.DRAFT_FAILING_LVL_ATTRIBUTE_ID )
                     || attributeId.equals(
                           AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID )
                     || attributeId
                           .equals( AparConstants.DRAFT_TREL_ATTRIBUTE_ID ))
               {
                  fieldValue = fieldValue.toUpperCase( Locale.ENGLISH );
               }
            }
         }
         // _B233387C Cleanse transmission of undesired values to
         // RETAIN.
         record.addField( attributeId, RtcUtils.scrubChars( fieldValue ),
               XMLEncodingType.XML );
      } // end for
      return record;
   }

   /**
    * _F80509A Supply default value for required primary artifact fields for
    * specific actions when no value specified on input. This assumes
    * createArtifact and createRecord were already called to transform the
    * work item to an artifact record based on data specified. This will be
    * passed to RETAIN for the action, and should be reflected in the work
    * item on a follow-up browse.
    * 
    * Refer to unspecifiedAttrDefaultMap hash map in this class to determine
    * default values.
    */
   public static void setUnspecifiedRequiredActionFieldsToDefault(
         Artifact artifact, IWorkItem workItem, String actionName,
         String[] attributeIds, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      if (artifact != null && workItem != null)
      {
         // Given an artifact, process the fields of the primary record
         String recordName = workItem.getWorkItemType();
         ArtifactRecordPart record = artifact.findRecord( recordName );
         setUnspecifiedRequiredActionFieldsToDefault( record, workItem,
               actionName, attributeIds, sportCommonData );
      }
   }

   /**
    * _F80509A Supply default value for required artifact fields for specific
    * actions when no value specified on input. This assumes createRecord was
    * already called to transform the work item to an artifact record based on
    * data specified. This will be passed to RETAIN for the action, and should
    * be reflected in the work item on a follow-up browse.
    * 
    * Refer to unspecifiedAttrDefaultMap hash map in this class to determine
    * default values.
    */
   public static void setUnspecifiedRequiredActionFieldsToDefault(
         ArtifactRecordPart record, IWorkItem workItem, String actionName,
         String[] attributeIds, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      if (record != null && actionName != null && attributeIds != null)
      {
         // Process work item attributes used for action
         for (String attrId : attributeIds)
         {
            // Quicker to check if there may need to be a default
            if (unspecifiedAttrDefaultMap.containsKey(
                  actionName + actionAttrKeySeparator + attrId ))
            {
               // Set default if no value
               ArtifactRecordField requiredField = record.findField( attrId );
               String requiredValue = (requiredField != null
                     ? requiredField.getFirstValueAsString() : null);
               if (requiredValue == null || requiredValue.length() == 0)
               {
                  if (requiredField == null)
                  {
                     // Field not yet in artifact record, add it
                     record.addField( attrId, unspecifiedAttrDefaultMap.get(
                           actionName + actionAttrKeySeparator + attrId ) );
                  }
                  else
                  {
                     // Set value for existing field
                     requiredField.setValue(
                           unspecifiedAttrDefaultMap.get( actionName
                                 + actionAttrKeySeparator + attrId ),
                           XMLEncodingType.XML );
                  }
               }
            }
         }
      }
   }

   public static String getSbsErrorMessage( Artifact sbsResponseArtifact )
   {
      String errorMessage = null;
      ArtifactResponse response = sbsResponseArtifact.getResponse();
      ArtifactRecordField field = sbsResponseArtifact
            .findField( STATUSMESSAGE_FIELD );
      if (field != null)
         errorMessage = field.getAllValuesAsString();
      if ((errorMessage == null) || errorMessage.trim().equals( "" ))
      {
         errorMessage = response.getDetails();
         if ((errorMessage != null) && errorMessage
               .indexOf( "java.lang.UnsupportedOperationException:" ) != -1)
         {
            int start = errorMessage.lastIndexOf( "Caused by:" );
            if (start == -1)
               start = 0;
            int end = StringUtils.indexOfNewline( errorMessage, start );
            StringBuilder error = new StringBuilder();
            error.append( "SBS failed with an exception: " );
            error.append( (end == -1) ? errorMessage.substring( start )
                  : errorMessage.substring( start, end ) );
            errorMessage = error.toString();
         }
      }
      return errorMessage;
   }

   public static boolean getSbsSave( IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      boolean sbsSave = false;
      String workItemType = RtcUtils.getWorkItemTypeName( workItem,
            sportCommonData );
      // _T97645C Some work item types use multiple words - so fuse together.
      // For instance, Call Queue -> CallQueue.
      String sbsSavedAttributeId = CommonUtils
            .getSbsSavedAttributeId( workItemType.replace( " ", "" ) );
      IAttribute sbsSaveAttribute = RtcUtils.findAttribute( workItem,
            sbsSavedAttributeId, sportCommonData );
      if (sbsSaveAttribute != null)
         sbsSave = (Boolean)(workItem.getValue( sbsSaveAttribute ));
      return sbsSave;
   }

   public static void resetSbsSave( IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String workItemType = RtcUtils.getWorkItemTypeName( workItem,
            sportCommonData );
      // _T97645C Some work item types use multiple words - so fuse together.
      // For instance, Call Queue -> CallQueue.
      String sbsSavedAttributeId = CommonUtils
            .getSbsSavedAttributeId( workItemType.replace( " ", "" ) );
      IWorkItem workItemToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute sbsSaveAttribute = RtcUtils.findAttribute( workItemToUpdate,
            sbsSavedAttributeId, sportCommonData );
      if ((sbsSaveAttribute != null)
            && (Boolean)(workItemToUpdate.getValue( sbsSaveAttribute )))
      {
         // _T1083 No need to call common method to set attribute.
         // Attribute availability already tested, working copy already used.
         workItemToUpdate.setValue( sbsSaveAttribute, Boolean.FALSE );
         // _T1083M To reuse working copy, save moved to end of Participant.
         // RtcUtils.saveWorkItem( workItem, sportCommonData );
      }
   }

   /**
    * Return the value from the <code>actionInitiator</code> attribute of the
    * work item, or <code>null</code> if the work item does not have an
    * <code>actionInitiator</code> attribute.
    * 
    * @param workItem the {@link IWorkItem} object containing the
    *        <code>actionInitiator</code> attribute value.
    * 
    * @param sportCommonData a {@link SportCommonData} object
    * 
    * @return the <code>String</code> value of the
    *         <code>actionInitiator</code> attribute.
    * @throws TeamRepositoryException
    */
   public static String getActionInitiator( IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String actionInitiator = null;
      String workItemType = RtcUtils.getWorkItemTypeName( workItem,
            sportCommonData );
      // _T97645C Some work item types use multiple words - so fuse together.
      // For instance, Call Queue -> CallQueue.
      String actionInitiatorAttributeId = CommonUtils
            .getActionInitiatorAttributeId( workItemType.replace( " ", "" ) );
      IAttribute actionInitiatorAttribute = RtcUtils.findAttribute( workItem,
            actionInitiatorAttributeId, sportCommonData );

      if (actionInitiatorAttribute != null)
      {
         actionInitiator = RtcUtils.getAttributeValueAsString( workItem,
               actionInitiatorAttributeId, sportCommonData );
      }

      return actionInitiator;
   }

   /**
    * Reset the value of the <code>actionInitiator</code> attribute of a work
    * item to RTC if it has an <code>actionInitiator</code> attribute.
    * 
    * @param workItem the {@link IWorkItem} object containing the
    *        <code>actionInitiator</code> attribute value.
    * 
    * @param sportCommonData a {@link SportCommonData} object
    * @throws TeamRepositoryException
    */
   public static void resetActionInitiator( IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String workItemType = RtcUtils.getWorkItemTypeName( workItem,
            sportCommonData );
      // _T97645C Some work item types use multiple words - so fuse together.
      // For instance, Call Queue -> CallQueue.
      String actionInitiatorAttributeId = CommonUtils
            .getSbsSavedAttributeId( workItemType.replace( " ", "" ) );
      IWorkItem workItemToUpdate = sportCommonData.getWorkItemForUpdate();
      IAttribute actionInitiatorAttribute = RtcUtils.findAttribute(
            workItemToUpdate, actionInitiatorAttributeId, sportCommonData );
      if (actionInitiatorAttribute != null)
      {
         // _T1083 No need to call common method to set attribute.
         // Attribute availability already tested, working copy already used.
         RtcUtils.setEnumerationAttributeValue( workItemToUpdate,
               actionInitiatorAttribute, ActionInitiator.RTC.toString(),
               sportCommonData );
         // _T1083M To reuse working copy, save moved to end of Participant.
         // RtcUtils.saveWorkItem( workItem, sportCommonData );
      }
   }

   public static Artifact sendArtifacts( Artifact sbsRequestArtifact,
         SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      return sendArtifacts( sbsRequestArtifact, sportCommonData, true );
   }

   private static Artifact sendArtifacts( Artifact sbsRequestArtifact,
         SportCommonData sportCommonData, boolean resend )
         throws TeamRepositoryException, SbsErrorException
   {
      SbsConfigurationData configData = RtcUtils
            .getSbsConfigurationData( sportCommonData );
      SbsUrlComponents sbsUrlComponents = new SbsUrlComponents( configData );
      Artifact authArtifact = prepareForAuthentication( sbsRequestArtifact,
            configData );
      Artifacts sbsRequestArtifacts = new Artifacts();
      sbsRequestArtifacts.addArtifact( sbsRequestArtifact );
      sbsRequestArtifacts.addArtifact( authArtifact );

      ABSComponentResponse sbsResponse;
      sbsResponse = sendArtifacts( "bridge", sportCommonData,
            sbsUrlComponents, sbsRequestArtifacts );
      Object sbsResponseObject = sbsResponse.getResponseObject();
      if (!(sbsResponseObject instanceof Artifacts))
      {
         String message = "The SBS response object is of type \""
               + sbsResponseObject.getClass().getName()
               + "\" rather than the expected type of \""
               + Artifacts.class.getName() + "\"";
         if (sbsResponseObject instanceof Throwable)
         {
            throw SportRuntimeException.WrapInSportRtcException(
                  (Throwable)sbsResponseObject, message );
         }
         else
         {
            throw new SportRuntimeException( message );
         }
      }
      Artifacts sbsResponseArtifacts = (Artifacts)sbsResponseObject;
      if (sbsResponseArtifacts.getNumArtifacts() == 0)
      {
         throw new SportRuntimeException( "No Artifact in response from "
               + sbsUrlComponents.toString() );
      }

      processAuthenticationResponse( sbsResponseArtifacts, sportCommonData,
            sbsUrlComponents );

      if (sbsResponseArtifacts.getNumArtifacts() > 1)
      {
         throw new SportRuntimeException(
               "More than one Artifact in response from "
                     + sbsUrlComponents.toString() );
      }
      Artifact sbsResponseArtifact = sbsResponseArtifacts.getArtifacts()
            .iterator().next();
      if (!sbsResponseArtifact.hasSuccessResponse())
      {
         // The session ID can be expired, or SBS could have been restarted
         // which would cause the cached session to be terminated
         if (sbsResponseArtifact.getResponseValue()
               .equals( ArtifactResponse.EXPIRED_SESSION_ID_ERROR_VALUE )
               || sbsResponseArtifact.getResponseValue().equals(
                     ArtifactResponse.INVALID_SESSION_ID_ERROR_VALUE )
               || sbsResponseArtifact.getResponseValue().equals(
                     ArtifactResponse.MISSING_SESSION_ID_ERROR_VALUE ))
         {
            resendArtifact( sbsRequestArtifact, sportCommonData );
         }

         if (sportCommonData.getIgnorePmr256())
         {
            // ignore if the RC is 256
            String response = sbsResponseArtifact.getResponse().getDetails();
            if (response.contains( "Return Code (256) " ))
            {
               // ignore and not throw exception
               throw new SbsErrorException( "256" );
            }

         }
         String sbsErrorMessage = getSbsErrorMessage( sbsResponseArtifact );
         String message = "Request to " + sbsUrlComponents.toString()
               + " failed:\n" + sbsErrorMessage;
         String repsonseDetails = sbsResponseArtifact.getResponse()
               .getDetails();
         if (sbsErrorMessage.equals( repsonseDetails ))
            throw new SbsErrorException( message );
         else
            throw new SbsErrorException( message, repsonseDetails );

      }

      SessionIdentifierPropertyElement
            .removeSessionIdentifier( sbsResponseArtifact );

      return sbsResponseArtifact;
   }

   private static ABSComponentResponse sendArtifacts( String methodName,
         SportCommonData sportCommonData, SbsUrlComponents sbsUrlComponents,
         Artifacts sbsRequestArtifacts )
   {
      ABSComponentResponse sbsResponse = null;
      try
      {
         if (sportCommonData.isFirstTime())
            sportCommonData.setFirstTime( false );
         else
            Thread.sleep( 1000 );
         URL senderURL = sbsUrlComponents.toSenderUrl();
         URL receiverURL = sbsUrlComponents.toReceiverUrl();
         ABSComponentProxyUtil proxyUtil = new ABSComponentProxyUtil(
               senderURL, receiverURL );
         Method bridgeMethod = ArtifactBridgeInterface.class
               .getMethod( methodName, Artifacts.class );
         ABSComponentRequest request = new ABSComponentRequest( bridgeMethod,
               new Object[] { sbsRequestArtifacts } );
         if (sportCommonData.getLog().isDebugEnabled())
            sportCommonData.getLog().debug(
                  "Sending request to " + receiverURL.toExternalForm() );
         sbsResponse = proxyUtil.sendRequest( request );
      }
      catch (Exception e)
      {
         throw SportRuntimeException.WrapInSportRtcException( e,
               "Sending Artifact to " + sbsUrlComponents.toString()
                     + " failed" );
      }
      return sbsResponse;
   }

   public static Artifacts sendSearchArtifact( Artifact sbsSearchArtifact,
         SportCommonData sportCommonData )
         throws TeamRepositoryException, SbsErrorException
   {
      SbsConfigurationData configData = RtcUtils
            .getSbsConfigurationData( sportCommonData );
      SbsUrlComponents sbsUrlComponents = new SbsUrlComponents( configData );
      Artifact authArtifact = prepareForAuthentication( sbsSearchArtifact,
            configData );
      Artifacts sbsRequestArtifacts = new Artifacts();
      sbsRequestArtifacts.addArtifact( sbsSearchArtifact );
      sbsRequestArtifacts.addArtifact( authArtifact );
      ABSComponentResponse sbsResponse = sendArtifacts( "bridge",
            sportCommonData, sbsUrlComponents, sbsRequestArtifacts );
      Object sbsResponseObject = sbsResponse.getResponseObject();
      if (!(sbsResponseObject instanceof Artifacts))
      {
         String message = "The SBS response object is of type \""
               + sbsResponseObject.getClass().getName()
               + "\" rather than the expected type of \""
               + Artifacts.class.getName() + "\"";
         if (sbsResponseObject instanceof Throwable)
         {
            throw SportRuntimeException.WrapInSportRtcException(
                  (Throwable)sbsResponseObject, message );
         }
         else
         {
            throw new SportRuntimeException( message );
         }
      }
      Artifacts sbsResponseArtifacts = (Artifacts)sbsResponseObject;
      if (sbsResponseArtifacts.getNumArtifacts() == 0)
      {
         throw new SportRuntimeException( "No Artifact in response from "
               + sbsUrlComponents.toString() );
      }
      processAuthenticationResponse( sbsResponseArtifacts, sportCommonData,
            sbsUrlComponents );
      if (sbsResponseArtifacts.getNumArtifacts() == 1)
      {
         Artifact sbsResponseArtifact = sbsResponseArtifacts.getArtifacts()
               .iterator().next();
         if (!sbsResponseArtifact.hasSuccessResponse())
         {
            String sbsErrorMessage = getSbsErrorMessage(
                  sbsResponseArtifact );
            String message = "Request to " + sbsUrlComponents.toString()
                  + " failed:\n" + sbsErrorMessage;
            String repsonseDetails = sbsResponseArtifact.getResponse()
                  .getDetails();
            if (sbsErrorMessage.equals( repsonseDetails ))
               throw new SbsErrorException( message );
            else
               throw new SbsErrorException( message, repsonseDetails );
         }
      }
      return sbsResponseArtifacts;
   }

   private static Artifact resendArtifact( Artifact sbsRequestArtifact,
         SportCommonData sportCommonData )
         throws TeamRepositoryException, SbsErrorException
   {
      SbsSessionIdentifierCache.getInstance().removeSessionIdentifier(
            sbsRequestArtifact.getProvider().getUserName() );

      return sendArtifacts( sbsRequestArtifact, sportCommonData, false );
   }

   private static Artifact prepareForAuthentication(
         Artifact sbsRequestArtifact, SbsConfigurationData configData )
   {
      ArtifactProvider provider = sbsRequestArtifact.getProvider();
      String userName = configData.getSbsUserName();
      String password = configData.getSbsPassword();

      Artifact authArtifact = AuthenticationRequestArtifactElement
            .createAuthenticationRequestArtifact(
                  AuthenticationRequestArtifactElement.NO_SESSION_ACTION_NAME,
                  userName, password, XMLEncodingType.AES, provider );

      return authArtifact;
   }

   private static void processAuthenticationResponse(
         Artifacts responseArtifacts, SportCommonData sportCommonData,
         SbsUrlComponents sbsUrlComponents )
   {
      Artifact authArtifact = AuthenticationRequestArtifactElement
            .findAuthenticationArtifact( responseArtifacts );

      // if (authArtifact == null)
      // {
      // String msg =
      // "Authentication artifact not contained in response from "
      // + sbsUrlComponents.toString();
      // Log.defLogError( msg );
      // throw new SportRuntimeException( msg );
      // }

      if ((authArtifact != null) && authArtifact.hasErrorResponse())
      {
         throw new SportRuntimeException( authArtifact.getResponse()
               .getContentAsString( Constants.NEWLINE ) );
      }

      responseArtifacts.removeExactChild( authArtifact );
      // Artifact signoutArtifact = SignoutRequestArtifactElement
      // .createSignoutRequestArtifact( authArtifact );
      // Artifacts artifacts = new Artifacts();
      // artifacts.addArtifact( signoutArtifact );
      // ABSComponentResponse sbsResponse = sendArtifacts( "signout",
      // sportCommonData, sbsUrlComponents, artifacts );
      // Object sbsResponseObj = sbsResponse.getResponseObject();
      //
      // if (sbsResponseObj instanceof Artifacts)
      // {
      // signoutArtifact = SignoutRequestArtifactElement
      // .findSignoutRequestArtifact( artifacts );
      //
      // if (signoutArtifact != null)
      // {
      // if (signoutArtifact.hasSuccessResponse())
      // {
      // Log.defLogDebug( "Signed out of "
      // + sbsUrlComponents.toString() );
      // }
      // else
      // {
      // Log.defLogWarning( "Failed to sign out of "
      // + sbsUrlComponents.toString()
      // + ": "
      // + signoutArtifact.getResponse().getContentAsString(
      // Constants.NEWLINE ) );
      // }
      // }
      // }
      // else
      // {
      // Log
      // .defLogWarning( "Unexpected object returned from signout request "
      // + "sent to "
      // + sbsUrlComponents.toString()
      // + ": "
      // + sbsResponseObj.toString() );
      // }
   }

   static class SbsUrlComponents
   {
      String sbsServerHostName = null;
      int sbsServerPortNumber = -1;
      String protocol = null;
      String sbsServerContextRoot = null;

      SbsUrlComponents( SbsConfigurationData configData )
      {
         sbsServerHostName = configData.getSbsServerHostNameNonNull();
         sbsServerPortNumber = configData.getSbsServerSslPortNumberNonNull()
               .intValue();
         protocol = "https";
         sbsServerContextRoot = configData.getSbsServerContextRootNonNull();
      }

      public String toString()
      {
         return "SBS at " + protocol + "://" + sbsServerHostName + ":"
               + sbsServerPortNumber + "/" + sbsServerContextRoot;
      }

      public URL toReceiverUrl()
            throws MalformedURLException
      {
         return new URL( this.protocol, this.sbsServerHostName,
               this.sbsServerPortNumber, "/" + this.sbsServerContextRoot );
      }

      public URL toSenderUrl()
            throws MalformedURLException, UnknownHostException
      {
         return new URL( this.protocol,
               InetAddress.getLocalHost().getHostName(), "/sportrtc" );
      }
   }
}
