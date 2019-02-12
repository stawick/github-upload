package com.ibm.sport.rtc.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.ArtifactTechnology.common.Artifact;
import com.ibm.ArtifactTechnology.common.ArtifactFieldValue;
import com.ibm.ArtifactTechnology.common.ArtifactRecordField;
import com.ibm.ArtifactTechnology.common.ArtifactRecordPart;
import com.ibm.ArtifactTechnology.common.Constants;
import com.ibm.team.repository.common.LogFactory;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.internal.attributeValueProviders.IConfiguration;
import com.ibm.team.workitem.common.internal.attributeValueProviders.IValueSetProvider;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

/** 
 * _T21519A RETAIN Component browse to obtain dynamic choices
 * for release, change team, and applicable releases (with some
 * result formatting - <release> + <Y|N>)
 * @author stawick
 *
 */
public class ComponentBrowseValueSetUtils_SaveE255566
      // implements IFilteredValueSetProvider<Object>
      implements IValueSetProvider<Object>
{
   protected static String noComponentErrMsg1 = "*** Values to select are derived from the component.";
   protected static String noComponentErrMsg2 = "*** Please specify a component and try again.";
   protected static String notifySPoRTExcpMsg = "*** SPoRT SBS error obtaining values! Check SPoRT SBS logs. Try again later.";
   protected static String notifyOtherExcpMsg = "*** Error encountered obtaining values! Check logs. Try again later.";
   // _F206987A Exclude invalid releases as choices.
   protected final static ArrayList<String> INCLUDE_INVALID_RELEASE_ATTRIBUTE_IDS = new ArrayList<String>();
   /*
   // For future enhancement. Currently all attributes require ONLY
   // VALID releases. Add here any attributes that should include
   // invalid releases (for whatever reasons). 
   static
   {
      INCLUDE_INVALID_RELEASE_ATTRIBUTE_IDS.add( "" );
   }
   */
   // _T257292A Enable APAR default system level determination.
   protected final static ArrayList<String> INCLUDE_SREL_RELEASE_ATTRIBUTE_IDS = new ArrayList<String>();
   static
   {
      INCLUDE_SREL_RELEASE_ATTRIBUTE_IDS
            .add( AparConstants.SYSTEM_RELEASE_LEVEL_ATTRIBUTE_ID );
   }

   /** 
    * These next 2 methods are derived from the implemented class.
    * We must provide these.
    * TODO Not sure whether we intend to filter the results? (TBD)
    * 
    * @param attribute
    * @param workItem
    * @param workItemCommon
    * @param configuration
    * @param filter
    * @param monitor
    * @return
    * @throws TeamRepositoryException
    */
   @SuppressWarnings("unchecked")
   public List getFilteredValueSet( IAttribute attribute, IWorkItem workItem,
         IWorkItemCommon workItemCommon, IConfiguration configuration,
         String filter, IProgressMonitor monitor )
         throws TeamRepositoryException
   {
      // Get initial values without filter
      List returnValues = getValueSet( attribute, workItem, workItemCommon,
            configuration, monitor );
      // TODO Filter values to be returned tbd? ...
      // return null;
      return returnValues;
   }

   @SuppressWarnings("unchecked")
   public List getValueSet( IAttribute attribute, IWorkItem workItem,
         IWorkItemCommon workItemCommon, IConfiguration configuration,
         IProgressMonitor monitor )
         throws TeamRepositoryException
   {
      List<String> returnValues = null;

      SportCommonData sportCommonData = getSPoRTDataObject( workItem,
            workItemCommon, configuration, monitor );
      String populateChoiceAttrId = attribute.getIdentifier();
      String browseComponentValue = getComponentValue( populateChoiceAttrId,
            workItem, sportCommonData );
      
      // When requesting component list, might not have value yet - OK
      if ((browseComponentValue == null || browseComponentValue.length() == 0)
            && !componentsNeeded( populateChoiceAttrId ))
      {
         // No component to use (ex. not specified yet on Create)
         returnValues = new ArrayList<String>();
         returnValues.add( noComponentErrMsg1 );
         returnValues.add( noComponentErrMsg2 );
         return returnValues;
      }

      // Attempt to find in cache 1st
      // Using browseComponentValue as part of the key
      // _T24561C
      returnValues = obtainCachedList( browseComponentValue,
            populateChoiceAttrId, sportCommonData );
      
      // Only call component browse for release or change team attributes
      if ((returnValues == null || returnValues.isEmpty())
            && !componentsNeeded( populateChoiceAttrId ))
      {
         // Do the SBS component browse
         try
         {
            // SBS call - (via AparUtils)
            String componentReplacement = null;
            // Obtaining choices for Sysroute attributes -
            // (Reroute and close both use the base component attr)
            if (sysrouteProcessing( populateChoiceAttrId ))
            {
               // Need to set component to different value (Sysroute action)
               // Get base component field in artifact, 
               // used by component browse
               componentReplacement = browseComponentValue;
            }
            Artifact componentBrowseResults = AparUtils.componentBrowse(
                  workItem, sportCommonData, componentReplacement );
            // Call method to extract desired attribute values after
            // successful SBS call -
            // Cache these results (complete set), only on success
            // Using browseComponentValue as part of the key
            cacheComponentData( componentBrowseResults, browseComponentValue,
                  sportCommonData ); // _T24561C
            // Extract correct data from newly formed cache values
            returnValues = obtainCachedList( browseComponentValue,
                  populateChoiceAttrId,
                  sportCommonData ); // _T24561C
         }
         catch (SportUserActionFailException e)
         {
            // Handle SBS exception - 
            // Return error msg as result value
            // Allows primary function to continue, while indicating error 
            // within this peripheral feature. 
            if (returnValues == null)
               returnValues = new ArrayList<String>();
            returnValues.add( notifySPoRTExcpMsg );
            returnValues.add( e.getMessage() );
            // Attempt to log the exception at least
            Log log = sportCommonData.getLog();
            String errMsg = "SPoRT Component Browse error encountered. "
               + "Problem encountered obtaining RETAIN data for component "
               + browseComponentValue
               + Constants.NEWLINE
               + e.getMessage();
            log.warn( errMsg );
         }
         catch (Exception e)
         {
            // Handle other exception - 
            // Return error msg as result value
            // Allows primary function to continue, while indicating error 
            // within this peripheral feature. 
            if (returnValues == null)
               returnValues = new ArrayList<String>();
            returnValues.add( notifyOtherExcpMsg );
            returnValues.add( e.getMessage() );
            // Attempt to log the exception at least
            Log log = sportCommonData.getLog();
            String errMsg = "SPoRT Component Browse error encountered. "
               + "Problem encountered obtaining RETAIN data for component "
               + browseComponentValue
               + Constants.NEWLINE
               + e.getMessage();
            log.warn( errMsg );
         }
      }

      // Now remove any choices already selected in a "collector" 
      // attribute to prevent duplicate selections.
      returnValues = preventDuplicateChoices( returnValues,
            populateChoiceAttrId, workItem, sportCommonData );
      // Note: RTC requirement open to use a string list type attribute
      // plus multi-select presentation.

      // Prevent null pointer exception in value-set picker presentation
      // when list returned from cache is empty.
      // Return empty list in this case.
      // return returnValues;
      return (returnValues == null ? new ArrayList<String>() : returnValues);
   }

   /** 
    * Method to get the data object for SBS usage.
    * 
    * @param workItem
    * @param workItemCommon
    * @param configuration
    * @param monitor
    * @return
    */
   public SportCommonData getSPoRTDataObject( IWorkItem workItem,
         IWorkItemCommon workItemCommon, IConfiguration configuration,
         IProgressMonitor monitor )
   {
      // Trying to reuse SPoRT operation participant server utilities
      // in this function to do SBS calls for RETAIN component browse.
      // Prepare the SportCommonData definition needed to use SPoRT utilities.
      SportCommonData sportCommonData = new SportCommonData();
      // AuditableCommon - from WorkItemCommon
      sportCommonData
            .setAuditableCommon( workItemCommon.getAuditableCommon() );
      // Just get the log from the class / package name
      Log log = LogFactory.getLog( this.getClass().getPackage().getName() );
      sportCommonData.setLog( log );
      // Monitor - from argument
      sportCommonData.setMonitor( monitor );
      // ProjectArea - from workItem
      sportCommonData.setProjectArea( workItem.getProjectArea() );

      // N/A QueryCommon - 
      // This is used to query RTC.
      // In the context we're using it - ie ComponentBrowse - 
      // we could probably get away with an empty object. 
      
      // WorkItemCommon - from argument
      sportCommonData.setWorkItemCommon(workItemCommon);
      
      // N/A WorkItemServer - NOT USED by SPoRTCommonData
      
      // N/A SbsInterfaceConfigurationData - 
      // It appears in the latest SPoRT internals, RtcUtils now obtains
      // the needed SPoRT configuration info from the process configuration.
      // It uses the common data auditable-common and project area components.
      // No need to gather that specific data here anymore.

      return sportCommonData;
   }
   
   /** 
    * Method to obtain the component used based on attribute processing.
    * Returns the value upper cased in case user input is in lower case.
    * 
    * @param populateChoiceAttrId
    * @param workItem
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static String getComponentValue(
         String populateChoiceAttrId, 
         IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String componentValue = "";
      // Obtaining choices for Sysroute attributes -
      // (Reroute and close both use the base component attr)
      if (sysrouteProcessing( populateChoiceAttrId ))
      {
         // Get Sysroute component attr from workItem argument
         componentValue = RtcUtils.getAttributeValueAsString(
               workItem, AparConstants.TO_COMPONENT_ATTRIBUTE_ID,
               sportCommonData );
      }
      else
      {
         // Get base component attr from workItem argument
         componentValue = RtcUtils.getAttributeValueAsString(
               workItem, AparConstants.COMPONENT_ATTRIBUTE_ID,
               sportCommonData );
      }
      // In case value is from user-input, ensure upper case used
      if (componentValue != null && componentValue.length() > 0)
      {
         componentValue = componentValue.toUpperCase();
      }

      return componentValue;
   }

   /** 
    * Method to get the specific attribute-related info.
    * Use attribute to determine data to extract from cache.
    * 
    * @param browseComponentValue
    * @param populateChoiceAttrId
    * @param sportCommonData _T24561A
    * @return
    */
   public static List<String> obtainCachedList( String browseComponentValue,
         String populateChoiceAttrId,
         SportCommonData sportCommonData )
   {
      List<String> cachedValues = null;
      // Attempt to find in cache 1st
      // Using browseComponentValue as part of the key

      // Requesting component releases - 
      if (releasesNeeded( populateChoiceAttrId ))
      {
         cachedValues = ComponentBrowseCache
               .obtainCachedReleaseList( browseComponentValue );
         if (cachedValues != null && !cachedValues.isEmpty())
         {
            // Might data need formatting?
            // Since we may be altering values, we dont want to modify
            // the cached values - so need new list
            List<String> formattedValues = new ArrayList<String>();
            for (String releaseValue : cachedValues)
            {
               // _F206987A Exclude invalid releases as choices.
               // Skip release choice if invalid and should NOT be
               // included.
               if (!validRelease( releaseValue, populateChoiceAttrId )
                     && !includeInvalidReleases( populateChoiceAttrId ))
                  continue;
               // Strip off any potential invalid indicator.
               // Note Strings are immutable, so this should return a
               // new String object with the modifications requested.
               // On the next iteration we start with a handle to the
               // next cached release.
               // _T257292A When "S" SREL type release is valid for
               // selection, strip that indicator off as well.
               releaseValue = cleanRelease( releaseValue );
               /*
               releaseValue = releaseValue.replaceAll(
                     AparConstants.SBS_COMPONENT_BROWSE_INVALID_RELEASETYPE_INDICATOR,
                     "" );
               // _T257292C "S" SREL type also not valid for attributes
               // that utilize release selection choices. Discard that
               // entry as well.
               if (srelRelease( releaseValue )
                     && !includeSRELReleases( populateChoiceAttrId ))
                  continue;
               // When requesting SREL releases specifically, and this
               // is not one, discard it. However, if it is to be used,
               // strip that indicator off as well.
               if (!srelRelease( releaseValue )
                     && includeSRELReleases( populateChoiceAttrId ))
                  continue;
               // _T257292A When "S" SREL type release is valid for
               // selection, strip that indicator off as well.
               releaseValue = releaseValue.replaceAll(
                     AparConstants.SBS_COMPONENT_BROWSE_SREL_RELEASETYPE_INDICATOR,
                     "" );
               */
               if (releaseYNType( populateChoiceAttrId ))
               {
                  // Release + Y, N
                  formattedValues.add( releaseValue + "Y" );
                  formattedValues.add( releaseValue + "N" );
               }
               else
               {
                  formattedValues.add( releaseValue );
               }
            }
            // Return the reformatted release choices
            return formattedValues;
         }

      }
      // Requesting component change teams -
      if (changeTeamsNeeded( populateChoiceAttrId ))
      {
         cachedValues = ComponentBrowseCache
               .obtainCachedChangeTeamList( browseComponentValue );
      }
      
      // Alternate use - just get components cached
      if (componentsNeeded( populateChoiceAttrId ))
      {
         // _T24561C Get components specific to project area
         // Returned as a basic list, but sorted
         cachedValues = ComponentBrowseCache
               .obtainCachedComponentNamesList( sportCommonData ); 
      }

      return cachedValues;
   }

   /** 
    * Method to cache SBS component browse results 
    * from SBS result artifact.
    * 
    * @param componentBrowseResults
    * @param browseComponentValue
    * @param sportCommonData _T24561A
    */
   public static void cacheComponentData( Artifact componentBrowseResults,
         String browseComponentValue,
         SportCommonData sportCommonData )
   {
      // SBS component browse result artifact will contain BOTH 
      // release AND change team data. Cache both sets of data.
      List<String> cachedData = new ArrayList<String>();

      ArtifactRecordPart cbMainRecord = componentBrowseResults
            .findRecord( 
                  AparConstants.SBS_COMPONENT_BROWSE_MAIN_RECORD_NAME );
      // Obtain releases AND change teams from component browse to save 
      // in cache.
      // Obtain Release data from Component Browse results
      for (ArtifactRecordPart componentReleaseRecord : cbMainRecord
            .findRecords( 
                  AparConstants.SBS_COMPONENT_BROWSE_RELEASE_RECORD_NAME ))
      {
         // Process each release value
         String releaseValue = componentReleaseRecord
               .findFieldValue( 
                     AparConstants.SBS_COMPONENT_BROWSE_RELEASE_FIELD_NAME );
         if (releaseValue != null)
         {
            // _F206987A Exclude invalid releases as choices.
            // See if the release is valid, append a <RETAINinvalid>
            // indicator but store the release anyway. Choice
            // functions can optionally pull all values or sort out
            // (and trim) these invalid choices.
            // _T257292A Identify an "S" type SREL default release type
            // and append that release with <RETAINsrel>. If needed,
            // can test for this indicator as a suffix.
            String releaseType = componentReleaseRecord.findFieldValue(
                  AparConstants.SBS_COMPONENT_BROWSE_RELEASETYPE_FIELD_NAME );
            /*
            if (releaseType != null
                  && releaseType
                        .equalsIgnoreCase( AparConstants.SBS_COMPONENT_BROWSE_INVALID_RELEASETYPE_VALUE ))
            */
            if (releaseType != null)
            {
               // _T257292C Fix invalid release identification, per RETAIN SSF doc.
            	// Set delimiter for content test
               String testReleaseType = " " + releaseType + " ";
               if (!(AparConstants.SBS_COMPONENT_BROWSE_VALID_RELEASETYPE_VALUES
                     .contains( testReleaseType )))
               {
                  // _T257292A Enable APAR default system level determination.
                  // An SREL "S" type release gets into here.
                  if (releaseType.equalsIgnoreCase(
                        AparConstants.SBS_COMPONENT_BROWSE_SREL_RELEASETYPE_VALUE ))
                  {
                     // Mark as SREL, but store in cache.
                     releaseValue += AparConstants.SBS_COMPONENT_BROWSE_SREL_RELEASETYPE_INDICATOR;
                  }
                  else
                  {
                     // Mark as invalid, but store in cache.
                     releaseValue += AparConstants.SBS_COMPONENT_BROWSE_INVALID_RELEASETYPE_INDICATOR;
                  }
               }
            }
            cachedData.add( releaseValue );
         }
      }

      // Cache releases - 
      // Cache these results (complete set), only on success
      // Using browseComponentValue as part of the key
      ComponentBrowseCache.cacheComponentReleaseData(
            browseComponentValue, cachedData );

      // Obtain change team data from Component Browse results
      // Since we are defining new values, we dont want to modify the 
      // cached release values - so need new list
      // cachedData.clear(); // Reset internal list
      cachedData = new ArrayList<String>();
      ArtifactRecordField chgTeamField = cbMainRecord
            .findField( 
                  AparConstants.SBS_COMPONENT_BROWSE_CHANGE_TEAM_FIELD_NAME );
      if (chgTeamField != null)
      {
         for (ArtifactFieldValue chgTeamValue : chgTeamField
               .getFieldValues())
         {
            String chgTeamStr = chgTeamValue.getValue();
            cachedData.add( chgTeamStr );
         }
      }

      // Cache change teams 
      // Cache these results (complete set), only on success
      // Using browseComponentValue as part of the key
      ComponentBrowseCache.cacheComponentChangeTeamData(
            browseComponentValue, cachedData );
      
      // _T24561A
      // Now cache the use of this component for this project area
      // for component list retrieval.
      // In the off chance that a component browse is done before
      // project area components are retrieved, this will retrieve
      // the project area components from the process configuration.
      ComponentBrowseCache.recordComponentUse( sportCommonData,
            browseComponentValue );
   }
   
   /** 
    * Method to remove choices already in some other "collector" attribute.
    * Use attribute to determine data to check for duplicates.
    * @param initialComponentBrowseChoices
    * @param populateChoiceAttrId
    * @param workItem
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static List<String> preventDuplicateChoices(
         List<String> initialComponentBrowseChoices,
         String populateChoiceAttrId, IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // Strip out choices already in some "collector" attribute
      // Initially all are returned
      List<String> returnValues = initialComponentBrowseChoices;
      String valueSeparator = " "; // Blank separated values within collector
      String opt1 = "";
      String opt2 = "";
      
      String collectorValues = null;
      // Attribute indicates it is multi-value capable
      // TODO use REAL selector attribute when implemented
      if (populateChoiceAttrId
            .equals( AparConstants_saveT266500.DRAFT_APPLICABLE_COMPONENT_LVLS_SELECTOR_ATTRIBUTE_ID )
            // .equals( AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID )
         )
      {
         // Get collector attribute value
         collectorValues = RtcUtils.getAttributeValueAsString(
               // workItem, AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_SELECTOR_ATTRIBUTE_ID,
               workItem, 
               AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID,
               sportCommonData );
      }
      // TODO use REAL selector attribute when implemented
      if (populateChoiceAttrId
            .equals( AparConstants_saveT266500.TO_APPLICABLE_COMPONENT_LEVELS_SELECTOR_ATTRIBUTE_ID )
            // .equals( AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID )
         )
      {
         // Get collector attribute value
         collectorValues = RtcUtils.getAttributeValueAsString(
               // workItem, AparConstants_saveT266500.TO_APPLICABLE_COMPONENT_LEVELS_SELECTOR_ATTRIBUTE_ID,
               workItem, 
               AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID,
               sportCommonData );
      }
      // If we have values already chosen
      if (collectorValues != null && collectorValues.length() > 0)
      {
         // _B233387A Handle alternate nbsp space chars.
         // Scrub and convert to blanks. Allows for correct duplicate
         // recognition in possible manual-entry existing value.
         collectorValues = RtcUtils.scrubChars( collectorValues );
         // New set to be determined
         // Since we are altering values, we dont want to modify the 
         // cached values - so need new list
         returnValues = new ArrayList<String>();
         // Determine if blank (default) or comma separated values
         // Assumes comma not valid content in key
         valueSeparator = (collectorValues.indexOf( "," ) >= 0) ? ","
               : valueSeparator;
         // Insure all values separated (first and last too)
         // Upper case for non-case-sensitive search.
         collectorValues = valueSeparator + collectorValues.toUpperCase()
               + valueSeparator;
         // Remove values already chosen
         for (String returnValue : initialComponentBrowseChoices)
         {
            // Once a rrrY or rrrN is selected, neither rrr choice 
            // should be present (even though RETAIN SDI allows both
            // rrrY AND rrrN to be set - but it doesnt make sense)
            opt1 = returnValue.substring( 0, returnValue.length() - 1 ) + "Y";
            opt2 = returnValue.substring( 0, returnValue.length() - 1 ) + "N";
            /*
            if (collectorValues.indexOf( valueSeparator + returnValue
                  + valueSeparator ) < 0)
            */
            if (collectorValues.indexOf( valueSeparator + opt1
                  + valueSeparator ) < 0
                  && collectorValues.indexOf( valueSeparator + opt2
                        + valueSeparator ) < 0)
            {
               // rrr{Y|N} Not yet selected, add it to new set
               returnValues.add( returnValue );
            }
         }
      }
      
      return returnValues;
   }

   /**
    * Return status based on selector attribute being processed 
    * @return
    */
   public static boolean changeTeamsNeeded( String populateChoiceAttrId )
   {
      // Requesting component change teams - 
      return ( populateChoiceAttrId
            .equals( AparConstants.CURRENT_CHANGE_TEAM_ATTRIBUTE_ID ) 
            || populateChoiceAttrId
            .equals( AparConstants.TO_CHANGE_TEAM_ATTRIBUTE_ID )
            );
   }

   /**
    * Return status based on selector attribute being processed 
    * @return
    */
   public static boolean componentsNeeded( String populateChoiceAttrId )
   {
      // Requesting choices for component attributes -
      return (populateChoiceAttrId
            .equals( AparConstants.COMPONENT_ATTRIBUTE_ID ) 
            || populateChoiceAttrId
            .equals( AparConstants.TO_COMPONENT_ATTRIBUTE_ID )
            );
   }

   /**
    * Return status based on selector attribute being processed 
    * @return
    */
   public static boolean releasesNeeded( String populateChoiceAttrId )
   {
      // Requesting component releases -
      return (populateChoiceAttrId
            .equals( AparConstants.RELEASE_ATTRIBUTE_ID )
            || populateChoiceAttrId
                  .equals( AparConstants.TO_RELEASE_ATTRIBUTE_ID )
            || populateChoiceAttrId.equals(
                  AparConstants_saveT266500.DRAFT_APPLICABLE_COMPONENT_LVLS_SELECTOR_ATTRIBUTE_ID )
                  // .equals( AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID )
            || populateChoiceAttrId.equals(
                  AparConstants_saveT266500.TO_APPLICABLE_COMPONENT_LEVELS_SELECTOR_ATTRIBUTE_ID )
                  // .equals( AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID )
            // _T257292C Enable APAR default system level determination
            || populateChoiceAttrId
                  .equals( AparConstants.SYSTEM_RELEASE_LEVEL_ATTRIBUTE_ID )
      );
   }

   /**
    * Return status based on selector attribute being processed 
    * @return
    */
   public static boolean releaseYNType( String populateChoiceAttrId )
   {
      // Obtaining choices for applicable levels attributes -
      // These will need Y/N appended to the releases.
      return ( populateChoiceAttrId
            .equals( AparConstants_saveT266500.DRAFT_APPLICABLE_COMPONENT_LVLS_SELECTOR_ATTRIBUTE_ID )
            // .equals( AparConstants.DRAFT_APPLICABLE_COMPONENT_LVLS_ATTRIBUTE_ID )
            || populateChoiceAttrId
            .equals( AparConstants_saveT266500.TO_APPLICABLE_COMPONENT_LEVELS_SELECTOR_ATTRIBUTE_ID )
            // .equals( AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID )
      );
   }

   // _F206987A Exclude invalid releases as choices.
   /**
    * Return status based on selector attribute being processed 
    * @return
    */
   public static boolean includeInvalidReleases( String populateChoiceAttrId )
   {
      // For future enhancement. Currently all attributes require ONLY
      // VALID releases. Add here a test for any attributes that
      // should include invalid releases (for whatever reasons). 
      // Update the static ArrayList with such attribute IDs.
      return (INCLUDE_INVALID_RELEASE_ATTRIBUTE_IDS != null
            && !(INCLUDE_INVALID_RELEASE_ATTRIBUTE_IDS.isEmpty()) && INCLUDE_INVALID_RELEASE_ATTRIBUTE_IDS
               .contains( populateChoiceAttrId ));
   }

   // _F206987A Exclude invalid releases as choices.
   /**
    * Invalid releases have an indicator appended (which needs to be
    * trimmed).
    * <br>This must now test for both invalid indicator, or whether
    * the SREL is indicated and it is not requested, or NOT indicated
    * and it IS requested.
    * 
    * @param releaseValue
    * @param populateChoiceAttrId
    * 
    * @return
    */
   public static boolean validRelease( String releaseValue,
         String populateChoiceAttrId )
   {
      // Return if invalid indicator set.
      if (releaseValue
            .endsWith( AparConstants.SBS_COMPONENT_BROWSE_INVALID_RELEASETYPE_INDICATOR ))
         return false;

      // _T257292A "S" SREL type also not valid for attributes that
      // utilize release selection choices.
      if (srelRelease( releaseValue )
            && !includeSRELReleases( populateChoiceAttrId ))
         return false;
      // When requesting SREL releases specifically, and this
      // is not one, also invalid.
      if (!srelRelease( releaseValue )
            && includeSRELReleases( populateChoiceAttrId ))
         return false;

      // Otherwise a valid release.
      return true;
      /*
      return (!(releaseValue
            .endsWith( AparConstants.SBS_COMPONENT_BROWSE_INVALID_RELEASETYPE_INDICATOR )
            )
      );
      */
   }

   // _T257292A Enable APAR default system level determination.
   /**
    * Return status based on selector attribute being processed.
    * Currently, MOST attributes do not include the SREL type of
    * release as a possible choice.
    * 
    * @return
    */
   public static boolean includeSRELReleases( String populateChoiceAttrId )
   {
      // Update the static ArrayList with applicable attribute IDs.
      return (INCLUDE_SREL_RELEASE_ATTRIBUTE_IDS != null
            && !(INCLUDE_SREL_RELEASE_ATTRIBUTE_IDS.isEmpty())
            && INCLUDE_SREL_RELEASE_ATTRIBUTE_IDS
                  .contains( populateChoiceAttrId )
      );
   }

   // _T257292A Enable APAR default system level determination.
   /**
    * SREL releases have an indicator appended (which needs to be
    * trimmed)
    *  
    * @return
    */
   public static boolean srelRelease( String releaseValue )
   {
      // Return if SREL indicator set.
      // _T257292C "S" type also not valid for attributes that utilize
      // release selection choices. System level allows "S" type and
      // must explicitly inspect for it.
      return (releaseValue.endsWith(
            AparConstants.SBS_COMPONENT_BROWSE_SREL_RELEASETYPE_INDICATOR )
      );
   }

   // _T257292A Enable APAR default system level determination.
   /**
    * Invalid or SREL releases have an indicator appended (which needs
    * to be trimmed).
    *  
    * @return
    */
   public static String cleanRelease( String releaseValue )
   {
      String returnValue = releaseValue;
      // Strip off any potential invalid indicator.
      // Note Strings are immutable, so this should return a
      // new String object with the modifications requested.
      // On the next iteration we start with a handle to the
      // next cached release.
      returnValue = returnValue.replaceAll(
            AparConstants.SBS_COMPONENT_BROWSE_INVALID_RELEASETYPE_INDICATOR,
            "" );
      // _T257292A When "S" SREL type release is valid for
      // selection, strip that indicator off as well.
      returnValue = returnValue.replaceAll(
            AparConstants.SBS_COMPONENT_BROWSE_SREL_RELEASETYPE_INDICATOR,
            "" );

      return returnValue;
   }

   /**
    * Return status based on selector attribute being processed 
    * @return
    */
   public static boolean sysrouteProcessing( String populateChoiceAttrId )
   {
      // Obtaining choices for Sysroute attributes -
      // (Reroute and close both use the base component attr)
      return ( populateChoiceAttrId
            .equals( AparConstants.TO_RELEASE_ATTRIBUTE_ID )
            || populateChoiceAttrId
                  .equals( AparConstants.TO_CHANGE_TEAM_ATTRIBUTE_ID ) 
            || populateChoiceAttrId
                  .equals( AparConstants_saveT266500.TO_APPLICABLE_COMPONENT_LEVELS_SELECTOR_ATTRIBUTE_ID )
                  // .equals( AparConstants.TO_APPLICABLE_COMPONENT_LEVELS_ATTRIBUTE_ID )
      );
   }

}